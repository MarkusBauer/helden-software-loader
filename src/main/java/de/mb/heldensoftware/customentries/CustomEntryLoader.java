package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.EntryCreator.Probe;
import de.mb.heldensoftware.customentries.EntryCreator.Quellenangabe;
import de.mb.heldensoftware.customentries.EntryCreator.ZauberWrapper;
import helden.comm.CommUtilities;
import jdk.nashorn.internal.scripts.JS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Markus on 20.03.2017.
 */
public class CustomEntryLoader {

	protected void loadCustomEntries(Reader in) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject customEntries = (JSONObject) parser.parse(in);
		if (customEntries.containsKey("zauber")) {
			JSONArray zauber = (JSONArray) customEntries.get("zauber");
			for (Object o : zauber) {
				loadZauber((JSONObject) o);
			}
		}

		if (customEntries.containsKey("sonderfertigkeiten")) {
			JSONArray sf = (JSONArray) customEntries.get("sonderfertigkeiten");
			for (Object o : sf) {
				loadSF((JSONObject) o);
			}
		}

		if (customEntries.containsKey("repräsentationen")) {
			JSONArray reprs = (JSONArray) customEntries.get("repräsentationen");
			for (Object o : reprs) {
				loadRepresentation((JSONObject) o);
			}
		}
	}

	protected void loadZauber(JSONObject zauber) {
		// Read parameters
		String name = (String) zauber.get("name");
		String kategorie = (String) zauber.get("kategorie");
		String[] merkmale = toStringArray((JSONArray) zauber.get("merkmale"));
		Probe probe = new Probe((String) zauber.get("probe"));

		Quellenangabe quelle = Quellenangabe.leereQuelle;
		Object o = zauber.get("quelle");
		if (o != null) quelle = new Quellenangabe((String) o);

		String mod = "";
		o = zauber.get("mod");
		if (o != null) mod = (String) o;

		// Create spell
		ZauberWrapper zw = EntryCreator.getInstance().createSpell(name, kategorie, merkmale, probe, quelle, mod);

		// Settings
		Object settings = zauber.get("settings");
		if (settings != null) {
			if (settings instanceof JSONArray) {
				for (Object setting : (JSONArray) settings) {
					zw.addToSetting((String) setting);
				}
			} else if (settings instanceof String) {
				zw.addToSetting((String) settings);
			}
		}

		// Verbreitung
		Object objVerbreitungen = zauber.get("verbreitung");
		if (objVerbreitungen != null) {
			JSONObject verbreitungen = (JSONObject) objVerbreitungen;
			for (Object key : verbreitungen.keySet()) {
				Number n = (Number) verbreitungen.get(key);
				zw.addVerbreitung((String) key, n.intValue());
			}
		}

		// Spezialisierungen
		Object objSpezis = zauber.get("spezialisierungen");
		if (objSpezis != null) {
			zw.setSpezialisierungen(toStringArray((JSONArray) objSpezis));
		}
	}

	protected void loadSF(JSONObject sf) {
		String name = (String) sf.get("name");
		Long kosten = (Long) sf.get("kosten");
		String cat = sf.containsKey("kategorie") ? (String) sf.get("kategorie") : "";
		EntryCreator.getInstance().createSonderfertigkeit(name, kosten.intValue(), getSFCategory(cat));
	}

	protected int getSFCategory(String cat) {
		switch (cat) {
			case "":
			case "Allgemein":
				return 0;
			case "Geländekunde":
				return 1;
			case "Kampf: Nahkampf":
			case "Nahkampf":
				return 2;
			case "Kampf: Fernkampf":
			case "Fernkampf":
				return 3;
			case "Magisch":
				return 4;
			case "Magisch: Repräsentation":
			case "Repräsentation":
				return 5;
			case "Magisch: Merkmalskenntnis":
			case "Merkmalskenntnis":
				return 6;
			case "Magisch: Objektritual":
			case "Objektritual":
				return 7;
			case "Elfenlied":
				return 8;
			case "Kampf: Manöver":
			case "Manöver":
				return 9;
			case "Geweiht: Liturgie":
			case "Liturgie":
				return 10;
			case "Geweiht":
				return 11;
			case "Magisch: Schamanenritual":
			case "Schamanenritual":
				return 12;
			case "Magisch: Magische Lieder":
			case "Magische Lieder":
				return 13;
			default:
				System.err.println("[CustomEntryLoader] Unbekannte Kategorie: " + cat);
				return 0;
		}
	}

	protected void loadRepresentation(JSONObject o) {
		String name = (String) o.get("name");
		String shortname = (String) o.get("abkürzung");
		boolean rk = o.containsKey("ritualkenntnis") && (Boolean) o.get("ritualkenntnis");
		EntryCreator.RepresentationWrapper repr = EntryCreator.getInstance().createRepresentation(name, shortname, rk);
		if (o.containsKey("zauber")) {
			JSONObject zauber = (JSONObject) o.get("zauber");
			for (Object key : zauber.keySet()) {
				Number n = (Number) zauber.get(key);
				repr.addZauber((String) key, n.intValue());
			}
		}
	}


	private static String[] toStringArray(JSONArray arr) {
		String[] result = new String[arr.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String) arr.get(i);
		}
		return result;
	}


	private static boolean filesLoaded = false;

	/**
	 * Load first file from:
	 * - helden.jar directory
	 * - helden.zip.hld directory
	 */
	public static void loadFiles() {
		if (filesLoaded) return;
		filesLoaded = true;

		try {
			final List<CustomEntryHandler> customEntryHandler = new ArrayList<>();
			customEntryHandler.add(new JsonFileProvider());
			customEntryHandler.add(new CsvJsonProvider());
			for (CustomEntryHandler handler : customEntryHandler) {
				// Config files next to helden.jar
				File jarpath = new CommUtilities().getJarPath();
				if (handler.loadCustomEntries(jarpath)) continue;
				if (jarpath.getName().toLowerCase().endsWith(".jar")) {
					if (handler.loadCustomEntries(jarpath.getParentFile())) continue;
				}
				// Config files next to helden.zip.hld
				File heldenPath = new File(new File(System.getProperty("user.home")), "helden");
				handler.loadCustomEntries(heldenPath);
			}


		} catch (Throwable e) {
			// Show message box and exit
			e.printStackTrace();
			String msg = e.getMessage() + "\n" + e.toString();
			while ((e instanceof RuntimeException) && e.getCause() != null) e = e.getCause();
			JOptionPane.showMessageDialog(null, msg, e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

	}

	public interface CustomEntryHandler {
		boolean loadCustomEntries(File folder) throws ParseException;
	}

	private static class CsvJsonProvider implements CustomEntryHandler {

		@Override
		public boolean loadCustomEntries(File folder) throws ParseException {
			boolean result = false;
			for (Path p : new Path[]{
					new File(folder, "erweiterungen.csv").toPath(),
					new File(folder, "erweiterungen-libreoffice.csv").toPath(),
					new File(folder, "erweiterungen-excel.csv").toPath()
			}) {
				if (!Files.exists(p)) {
					System.err.println("[CustomEntryLoader] Keine Erweiterungen in " + p.toAbsolutePath());
					continue;
				}
				System.err.println("[CustomEntryLoader] Lade Erweiterungen von \"" + p.toAbsolutePath() + "\"");
				try (Reader r = new InputStreamReader(new CsvConverter().convertToJson(p), StandardCharsets.UTF_8)) {
					new CustomEntryLoader().loadCustomEntries(r);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				result = true;
			}
			return result;
		}
	}

	private static class JsonFileProvider implements CustomEntryHandler {

		@Override
		public boolean loadCustomEntries(File folder) throws ParseException {
			return loadFile(new File(folder, "customentries.json")) |
					loadFile(new File(folder, "erweiterungen.json")) |
					loadFile(new File(folder, "erweiterungen.json.txt")); // I know my dear Windows users...
		}

		private boolean loadFile(File f) throws ParseException {
			if (!f.exists()) {
				System.err.println("[CustomEntryLoader] Keine Erweiterungen in " + f.getAbsolutePath());
				return false;
			}
			System.err.println("[CustomEntryLoader] Lade Erweiterungen von \"" + f.getAbsolutePath() + "\"");
			try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(f))) {
				// Remove BOM - I know my Windows users
				byte[] buffer = new byte[3];
				input.mark(4);
				input.read(buffer);
				if (!(buffer[0] == (byte) 0xEF && buffer[1] == (byte) 0xBB && buffer[2] == (byte) 0xBF)) {
					input.reset();
				}
				// Read file as UTF-8
				try (Reader r = new InputStreamReader(input, StandardCharsets.UTF_8)) {
					new CustomEntryLoader().loadCustomEntries(r);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
	}

}
