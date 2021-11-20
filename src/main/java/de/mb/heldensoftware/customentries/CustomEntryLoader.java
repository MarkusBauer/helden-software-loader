package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.EntryCreator.Probe;
import de.mb.heldensoftware.customentries.EntryCreator.Quellenangabe;
import de.mb.heldensoftware.customentries.EntryCreator.ZauberWrapper;
import helden.comm.CommUtilities;
import helden.framework.Einstellungen;
import helden.framework.bedingungen.AbstraktBedingung;
import helden.framework.bedingungen.Bedingung;
import helden.framework.bedingungen.BedingungsVerknuepfung;
import helden.framework.held.persistenz.XMLEinstellungenParser;
import helden.framework.zauber.Zauber;
import helden.framework.zauber.ZauberFabrik;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


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
		if (name == null)
			throw new RuntimeException("Eigene Sonderfertigkeit: \"name\" fehlt.");
		if (kosten == null)
			throw new RuntimeException("Eigene Sonderfertigkeit: \"kosten\" fehlt.");
		String cat = sf.containsKey("kategorie") ? (String) sf.get("kategorie") : "";
		BedingungsVerknuepfung bedingung = null;
		if (sf.get("bedingungen") != null && sf.get("bedingungen") instanceof JSONArray) {
			try {
				bedingung = loadBedingungen((JSONArray) sf.get("bedingungen"));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		EntryCreator.getInstance().createSonderfertigkeit(name, kosten.intValue(), getSFCategory(cat), bedingung);
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

	protected BedingungsVerknuepfung loadBedingungen(JSONArray arr) throws IllegalAccessException {
		ArrayList<AbstraktBedingung> lst = new ArrayList<>();
		for (Object o: arr) {
			if (!(o instanceof JSONObject)) throw new RuntimeException("Bedingung muss ein Objekt sein!");
			JSONObject jo = (JSONObject) o;
			int value = 0;
			if (jo.get("value") != null) {
				value = ((Long) jo.get("value")).intValue();
			}
			switch (((String) jo.get("type")).toLowerCase()) {
				case "sonderfertigkeit":
				case "sf":
					lst.add(EntryCreator.getInstance().createBedingungSF((String) jo.get("name")));
					break;
				case "zauber":
					Zauber zauber = ZauberFabrik.getInstance().getZauberfertigkeit((String) jo.get("name"));
					if (zauber == null) throw new RuntimeException("Zauber \"" + jo.get("name") + "\" nicht gefunden!");
					lst.add(EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(zauber, value));
					break;
				case "talent":
					EntryCreator.getInstance().initTalentFactoryMap();
					Object talent = EntryCreator.getInstance().talentFactoryMap.get((String) jo.get("name"));
					if (talent == null) throw new RuntimeException("Talent \"" + jo.get("name") + "\" nicht gefunden!");
					lst.add(EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(talent, value));
					break;
				case "eigenschaft":
					Object eigenschaft = EntryCreator.getInstance().alleEigenschaften.get((String) jo.get("name"));
					if (eigenschaft == null) throw new RuntimeException("Eigenschaft \"" + jo.get("name") + "\" nicht gefunden!");
					lst.add(EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(eigenschaft, value));
					break;
				case "lkw":
					lst.add(Bedingung.hatLkW(value));
					break;
				case "magielevel":
					Bedingung.MagieLevel level = EntryCreator.getInstance().alleMagielevel.get((String) jo.get("name"));
					if (level == null) throw new RuntimeException("MagieLevel \"" + jo.get("name") + "\" nicht gefunden!");
					lst.add(Bedingung.istMindestens(level));
				default:
					System.err.println("[CustomEntryLoader] Ignorierte Bedingung: " + jo.toJSONString());
					throw new RuntimeException("Ungültige Bedingung: type \"" + ((String) jo.get("type")).toLowerCase() + "\" ist nicht bekannt!");
			}
		}
		return BedingungsVerknuepfung.AND(lst.toArray(new AbstraktBedingung[0]));
	}

	protected void loadRepresentation(JSONObject o) {
		String name = (String) o.get("name");
		String shortname = (String) o.get("abkürzung");
		if (name == null)
			throw new RuntimeException("Eigene Repräsentation: \"name\" fehlt.");
		if (shortname == null)
			throw new RuntimeException("Eigene Repräsentation: \"shortname\" fehlt.");
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

		// Check for custom paths in settings
		File einstellungen = new File(Einstellungen.getInstance().getPfade().getPfad("einstellungsPfad"));
		if (einstellungen.exists()) {
			try {
				new XMLEinstellungenParser().ladeEinstellungen(einstellungen);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			final List<CustomEntryHandler> customEntryHandler = new ArrayList<>();
			customEntryHandler.add(new JsonFileProvider());
			customEntryHandler.add(new CsvJsonProvider());
			for (CustomEntryHandler handler : customEntryHandler) {
				// Config files next to helden.jar
				File jarpath = new CommUtilities().getJarPath();
				if (handler.loadCustomEntries(jarpath)) continue;
				if (handler.loadCustomEntries(new File(jarpath, "helden"))) continue; // hslocal
				if (jarpath.getName().toLowerCase().endsWith(".jar")) {
					if (handler.loadCustomEntries(jarpath.getParentFile())) continue;
					if (handler.loadCustomEntries(new File(jarpath.getParentFile(), "helden"))) continue; // hslocal
				}
				// Config files next to helden.zip.hld
				File heldenPath = new File(new File(System.getProperty("user.home")), "helden");
				handler.loadCustomEntries(heldenPath);

				File heldenPath2 = new File(Einstellungen.getInstance().getPfade().getPfad("heldenPfad")).getAbsoluteFile().getParentFile();
				if (!heldenPath2.equals(heldenPath)) {
					handler.loadCustomEntries(heldenPath2);
				}
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
				read(p);
				result = true;
			}
			if (folder.exists() && folder.isDirectory()) {
				File subfolder = new File(folder, "erweiterungen");
				if (subfolder.exists() && subfolder.isDirectory()) {
					for (File f : subfolder.listFiles()) {
						if (f.getName().endsWith(".csv")) {
							read(f.toPath());
							result = true;
						}
					}
				}
			}
			return result;
		}

		private void read(Path p) throws ParseException {
			System.err.println("[CustomEntryLoader] Lade Erweiterungen von \"" + p.toAbsolutePath() + "\"");
			try (Reader r = new InputStreamReader(new CsvConverter().convertToJson(p), StandardCharsets.UTF_8)) {
				new CustomEntryLoader().loadCustomEntries(r);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private static class JsonFileProvider implements CustomEntryHandler {

		@Override
		public boolean loadCustomEntries(File folder) throws ParseException {
			boolean result = loadFile(new File(folder, "customentries.json")) |
					loadFile(new File(folder, "erweiterungen.json")) |
					loadFile(new File(folder, "erweiterungen.json.txt")); // I know my dear Windows users...
			if (folder.exists() && folder.isDirectory()) {
				File subfolder = new File(folder, "erweiterungen");
				if (subfolder.exists() && subfolder.isDirectory()) {
					for (File f : subfolder.listFiles()) {
						if (f.getName().endsWith(".json") || f.getName().endsWith(".json.txt")) {
							result |= loadFile(f);
						}
					}
				} else {
					System.err.println("[CustomEntryLoader] Keine Erweiterungen in " + subfolder.getAbsolutePath() + "/");
				}
			}
			return result;
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
