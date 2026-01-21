package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.config.Config;
import de.mb.heldensoftware.customentries.config.Verbreitungen;
import de.mb.heldensoftware.customentries.config.ZauberConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
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
        String Spezialisierungen = "Spezialisierungen";
        String Quelle = "Quelle";
    }

    public Config convertToConfig(String content) {
        final String[] columns = new String[]{
                Columns.Name, Columns.Kategorie,
                Columns.Merkmale, Columns.Probe,
                Columns.MODS_MR, Columns.Verbreitung,
                Columns.Settings, Columns.Spezialisierungen, Columns.Quelle
        };
        CSVFormat csvFormat = detectFormat(content).withHeader(columns).withSkipHeaderRecord();

        try (final Reader reader = new StringReader(content);
             final CSVParser parser = new CSVParser(reader, csvFormat)
        ) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();

            // Verify Existence of columns
            for (String headerName : columns) {
                if (!headerMap.containsKey(headerName) && !headerName.equals(Columns.Quelle) && !headerName.equals(Columns.Spezialisierungen)) {
                    throw new IllegalStateException("Es wurde keine Spalte mit der Bezeichnung '" + headerName + "' gefunden.");
                }
            }

            // Now try to convert
            final List<CSVRecord> records = parser.getRecords();
            Config config = new Config();
            for (CSVRecord record : records) {
                config.zauber.add(parseRecord(record));
            }
            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ZauberConfig parseRecord(CSVRecord record) {
        ZauberConfig zauber = new ZauberConfig();
        zauber.name = record.get(Columns.Name);
        zauber.kategorie = record.get(Columns.Kategorie);
        zauber.merkmale = parseList(record.get(Columns.Merkmale), ",");
        zauber.probe = record.get(Columns.Probe);
        zauber.mod = record.get(Columns.MODS_MR);
        zauber.settings = parseList(record.get(Columns.Settings), ",");
        zauber.verbreitung = parseVerbreitung(record.get(Columns.Verbreitung), ",");
        try {
            zauber.quelle = record.get(Columns.Quelle);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            zauber.spezialisierungen = parseList(record.get(Columns.Spezialisierungen), ",");
        } catch (IllegalArgumentException ignored) {
        }
        return zauber;
    }

    private static ArrayList<String> parseList(String input, String delimiter) {
        final String[] split = input.split(delimiter);
        final ArrayList<String> items = new ArrayList<>();
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

    private static Verbreitungen parseVerbreitung(String input, String delimiter) {
        final Verbreitungen verbreitungObj = new Verbreitungen();
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
                verbreitungObj.put(type.trim(), Integer.parseInt(numString.trim()));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Verbreitung '" + eachVerbreitung + "' enthält keine gültige Zahl. " +
                        "Erwartet wurde eine Zahl zwischen 1 und 7, erhaltener Wert war: " + numString);
            }
        }
        return verbreitungObj;
    }

    private static CSVFormat detectFormat(String content) {
        CSVFormat format = CSVFormat.DEFAULT;
        // detect separator: , or ;
        long count_comma = 0;
        long count_semicolon = 0;
        long count_tab = 0;
        for (int b : content.toCharArray()) {
            if (b == ',') count_comma++;
            else if (b == ';') count_semicolon++;
            else if (b == '\t') count_tab++;
            else if (b == '\n') break;
        }
        if (count_tab > count_semicolon && count_tab > count_comma)
            format = format.withDelimiter('\t');
        else if (count_semicolon > count_comma)
            format = format.withDelimiter(';');

        return format;
    }
}
