package de.mb.heldensoftware.customentries;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvConverter {

	public interface Columns {
		String Name = "Name";
		String Kategorie = "Kategorie";
		String Merkmale = "Merkmale";
		String Probe = "Probe";
		String MODS_MR = "Mods/MR";
		String Verbreitung = "Verbreitung";
		String Settings = "Settings";
	}

	public InputStream convertToJson(Path p) throws IOException {
		return convertToJson(Files.readAllBytes(p));
	}

	@SuppressWarnings("unchecked")
	public InputStream convertToJson(byte[] content) throws IOException {
		final String[] columns = new String[]{
				Columns.Name, Columns.Kategorie,
				Columns.Merkmale, Columns.Probe,
				Columns.MODS_MR, Columns.Verbreitung,
				Columns.Settings
		};
		CSVFormat csvFormat = detectFormat(content).withHeader(columns).withSkipHeaderRecord();
		Charset charset = detectEncoding(content);

		try (final Reader reader = new StringReader(new String(content, charset));
			 final CSVParser parser = new CSVParser(reader, csvFormat)
		) {
			final Map<String, Integer> headerMap = parser.getHeaderMap();

			// Verify Existence of columns
			for (String headerName : columns) {
				if (!headerMap.containsKey(headerName)) {
					throw new IllegalStateException("Es wurde keine Spalte mit der Bezeichnung '" + headerName + "' gefunden.");
				}
			}

			// Now try to convert
			final List<CSVRecord> records = parser.getRecords();
			final JSONArray spells = new JSONArray();
			for (CSVRecord eachRecord : records) {
				final JSONObject spell = new JSONObject();
				spell.put("name", eachRecord.get(Columns.Name));
				spell.put("kategorie", eachRecord.get(Columns.Kategorie));
				spell.put("merkmale", parseList(eachRecord.get(Columns.Merkmale), ","));
				spell.put("probe", eachRecord.get(Columns.Probe));
				spell.put("mod", eachRecord.get(Columns.MODS_MR));
				spell.put("settings", parseList(eachRecord.get(Columns.Settings), ","));
				spell.put("verbreitung", parseVerbreitung(eachRecord.get(Columns.Verbreitung), ","));
				spells.add(spell);
			}

			final JSONObject root = new JSONObject();
			root.put("zauber", spells);
			return new ByteArrayInputStream(root.toJSONString().getBytes(StandardCharsets.UTF_8));
		}
	}

	private static List<String> parseList(String input, String delimiter) {
		final String[] split = input.split(delimiter);
		final List<String> items = new ArrayList<>();
		for (String eachItem : split) {
			// Some users enter "x, y, z", which results in some values containing a space
			// therefore we trim each value just to be sure
			final String trimmedItem = eachItem.trim();
			if (!trimmedItem.isEmpty()) {
				items.add(trimmedItem);
			}
		}
		return items;
	}

	private static JSONObject parseVerbreitung(String input, String delimiter) {
		final JSONObject verbreitungObj = new JSONObject();
		final List<String> verbreitungen = parseList(input, delimiter);
		for (String eachVerbreitung : verbreitungen) {

			// Determine numeric value
			int index = -1;
			for (int i = 0; i < eachVerbreitung.length(); i++) {
				if (Character.isDigit(eachVerbreitung.charAt(i))) {
					index = i;
					break;
				}
			}

			// if not found, bail
			if (index == -1) {
				throw new IllegalArgumentException("Verbreitung '" + eachVerbreitung + "' ist nicht gültig.");
			}

			// Otherwise separate values
			final String type = eachVerbreitung.substring(0, index);
			final String numString = eachVerbreitung.substring(index);
			try {
				verbreitungObj.put(type, Integer.parseInt(numString));
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Verbreitung '" + eachVerbreitung + "' enthält keine gültige Zahl. " +
						"Erwartet wurde eine Zahl zwischen 1 und 7, erhaltener Wert war: " + numString);
			}
		}
		return verbreitungObj;
	}

	private static CSVFormat detectFormat(byte[] content) {
		CSVFormat format = CSVFormat.DEFAULT;
		// detect separator: , or ;
		long count_comma = 0;
		long count_semicolon = 0;
		long count_tab = 0;
		for (byte b : content) {
			if (b == ',') count_comma++;
			else if (b == ';') count_semicolon++;
			else if (b == '\t') count_tab++;
			else if (b == '\n') break;
		}
		if (count_tab > count_semicolon && count_tab > count_comma)
			format = format.withDelimiter('\t');
		else if (count_semicolon > count_comma)
			format = format.withDelimiter(';');

		//TODO detect encoding + BOM

		return format;
	}

	/**
	 * Automatic charset detection, based on jchardet. Typical charsets are windows-1252 and UTF-8.
	 *
	 * @param content File content to perform detection on
	 * @return A charset, with UTF-8 being default.
	 */
	private static Charset detectEncoding(byte[] content) {
		// What an ugly library interface. Directly from hell...
		final Charset result[] = new Charset[]{StandardCharsets.UTF_8};
		nsDetector detector = new nsDetector();
		detector.Init(new nsICharsetDetectionObserver() {
			@Override
			public void Notify(String s) {
				result[0] = Charset.forName(s);
			}
		});
		try {
			detector.DoIt(content, content.length, false);
			detector.DataEnd();
			detector.Done();
		} catch (UnsupportedCharsetException e) {
			System.err.println(e.getMessage());
		}
		return result[0];
	}
}
