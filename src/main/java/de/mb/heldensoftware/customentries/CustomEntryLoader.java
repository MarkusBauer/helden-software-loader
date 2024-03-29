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
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * Created by Markus on 20.03.2017.
 */
public class CustomEntryLoader {

	protected HashSet<String> newSFNames = new HashSet<>();

	protected void loadCustomEntries(Reader in) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject customEntries = (JSONObject) parser.parse(in);
		// build a list of new SF names
		if (customEntries.containsKey("sonderfertigkeiten")) {
			JSONArray sf = (JSONArray) customEntries.get("sonderfertigkeiten");
			for (Object o : sf) {
				String name = (String) ((JSONObject) o).get("name");
				newSFNames.add(name);
			}
		}

		// load all zauber
		if (customEntries.containsKey("zauber")) {
			JSONArray zauber = (JSONArray) customEntries.get("zauber");
			for (Object o : zauber) {
				loadZauber((JSONObject) o);
			}
		}

		// load all sonderfertigkeit
		if (customEntries.containsKey("sonderfertigkeiten")) {
			JSONArray sf = (JSONArray) customEntries.get("sonderfertigkeiten");
			for (Object o : sf) {
				loadSF((JSONObject) o);
			}
		}

		// load all repräsentationen
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
		int cat = getSFCategory(sf.containsKey("kategorie") ? (String) sf.get("kategorie") : "");
		if (kosten == null && cat != 10)
			throw new RuntimeException("Eigene Sonderfertigkeit: \"kosten\" fehlt.");
		BedingungsVerknuepfung bedingung = null;
		if (sf.get("bedingungen") != null && sf.get("bedingungen") instanceof JSONArray) {
			try {
				bedingung = loadBedingungen((JSONArray) sf.get("bedingungen"), false);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		// Liturgien
		if (cat == 10) {
			Long grad = (Long) sf.get("grad");
			if (grad == null)
				throw new RuntimeException("Eigene Liturgie: \"grad\" fehlt.");
			// list of gods that have access
			ArrayList<String> liturgiekenntnis = new ArrayList<>();
			if (sf.get("liturgiekenntnis") != null && sf.get("liturgiekenntnis") instanceof JSONArray) {
				for (Object o : (JSONArray) sf.get("liturgiekenntnis")) {
					if (o instanceof String) {
						String s = (String) o;
						if (!s.startsWith("Liturgiekenntnis ("))
							s = "Liturgiekenntnis (" + s + ")";
						liturgiekenntnis.add(s);
					}
				}
			}
			EntryCreator.getInstance().createLiturgieSonderfertigkeit(name, grad.intValue(), liturgiekenntnis, bedingung);
			return;
		}

		// normal SF
		Object sfname = EntryCreator.getInstance().createSonderfertigkeit(name, kosten.intValue(), cat, bedingung);

		// Liturgiekenntnis
		if (sf.get("liturgien") != null && sf.get("liturgien") instanceof JSONArray) {
			ArrayList<String> liturgien = new ArrayList<>();
			for (Object o: (JSONArray) sf.get("liturgien")) {
				if (o instanceof String) {
					String s = (String) o;
					if (!s.startsWith("Liturgie: "))
						s = "Liturgie: " + s;
					liturgien.add(s);
				}
			}
			EntryCreator.getInstance().addLiturgiekenntnisToLiturgien(sfname, liturgien);
		}
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

	protected BedingungsVerknuepfung loadBedingungen(JSONArray arr, boolean isOr) throws IllegalAccessException {
		ArrayList<AbstraktBedingung> lst = new ArrayList<>();
		for (Object o: arr) {
			if (!(o instanceof JSONObject)) throw new RuntimeException("Bedingung muss ein Objekt sein!");
			JSONObject jo = (JSONObject) o;
			int value = 0;
			if (jo.get("value") != null) {
				value = ((Long) jo.get("value")).intValue();
			}
			boolean negiere = jo.get("not") != null && (Boolean) jo.get("not");
			AbstraktBedingung b = null;
			switch (((String) jo.get("type")).toLowerCase()) {
				case "or":
					b = loadBedingungen((JSONArray) jo.get("bedingungen"), true);
					break;
				case "and":
					b = loadBedingungen((JSONArray) jo.get("bedingungen"), false);
					break;
				case "sonderfertigkeit":
				case "sf":
					String name = (String) jo.get("name");
					b = EntryCreator.getInstance().createBedingungSF(name, newSFNames.contains(name));
					break;
				case "zauber":
					Zauber zauber = ZauberFabrik.getInstance().getZauberfertigkeit((String) jo.get("name"));
					if (zauber == null) throw new RuntimeException("Zauber \"" + jo.get("name") + "\" nicht gefunden!");
					b = EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(zauber, value);
					break;
				case "talent":
					EntryCreator.getInstance().initTalentFactoryMap();
					Object talent = EntryCreator.getInstance().talentFactoryMap.get((String) jo.get("name"));
					if (talent == null) throw new RuntimeException("Talent \"" + jo.get("name") + "\" nicht gefunden!");
					b = EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(talent, value);
					break;
				case "eigenschaft":
					Object eigenschaft = EntryCreator.getInstance().alleEigenschaften.get((String) jo.get("name"));
					if (eigenschaft == null) throw new RuntimeException("Eigenschaft \"" + jo.get("name") + "\" nicht gefunden!");
					b = EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(eigenschaft, value);
					break;
				case "lkw":
					b = Bedingung.hatLkW(value);
					break;
				case "magielevel":
					Bedingung.MagieLevel level = EntryCreator.getInstance().alleMagielevel.get((String) jo.get("name"));
					if (level == null) throw new RuntimeException("MagieLevel \"" + jo.get("name") + "\" nicht gefunden!");
					b = Bedingung.istMindestens(level);
					break;
				default:
					System.err.println("[CustomEntryLoader] Ignorierte Bedingung: " + jo.toJSONString());
					throw new RuntimeException("Ungültige Bedingung: type \"" + ((String) jo.get("type")).toLowerCase() + "\" ist nicht bekannt!");
			}

			if (b != null) {
				if (negiere) {
					if (b instanceof Bedingung) {
						try {
							EntryCreator.getInstance().bedingungSetNegieren.invoke(b, true);
						} catch (InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					} else {
						throw new RuntimeException("Kann Bedingung nicht negieren: " + b);
					}
				}
				lst.add(b);
			}
		}
		if (isOr) {
			return BedingungsVerknuepfung.OR(lst.toArray(new AbstraktBedingung[0]));
		} else {
			return BedingungsVerknuepfung.AND(lst.toArray(new AbstraktBedingung[0]));
		}
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
