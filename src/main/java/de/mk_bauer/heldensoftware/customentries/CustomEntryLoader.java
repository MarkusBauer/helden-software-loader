package de.mk_bauer.heldensoftware.customentries;

import helden.comm.CommUtilities;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

import de.mk_bauer.heldensoftware.customentries.SpellCreator.*;

import javax.swing.*;

import static org.json.simple.JSONValue.parse;

/**
 * Created by Markus on 20.03.2017.
 */
public class CustomEntryLoader {

	public void loadCustomEntries(Reader in) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject customEntries = (JSONObject) parser.parse(in);
		JSONArray zauber = (JSONArray) customEntries.get("zauber");
		for (Object o: zauber){
			loadZauber((JSONObject) o);
		}
	}

	protected void loadZauber(JSONObject zauber){
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
		ZauberWrapper zw = SpellCreator.getInstance().createSpell(name, kategorie, merkmale, probe, quelle, mod);

		// Settings
		Object settings = zauber.get("settings");
		if (settings != null){
			if (settings instanceof JSONArray){
				for (Object setting: (JSONArray) settings){
					zw.addToSetting((String) setting);
				}
			}else if (settings instanceof String){
				zw.addToSetting((String) settings);
			}
		}

		// Verbreitung
		Object objVerbreitungen = zauber.get("verbreitung");
		if (objVerbreitungen != null){
			JSONObject verbreitungen = (JSONObject) objVerbreitungen;
			for (Object key: verbreitungen.keySet()){
				Number n = (Number) verbreitungen.get(key);
				zw.addVerbreitung((String) key, n.intValue());
			}
		}

		// Spezialisierungen
		Object objSpezis = zauber.get("spezialisierungen");
		if (objSpezis != null){
			zw.setSpezialisierungen(toStringArray((JSONArray) objSpezis));
		}
	}


	private static String[] toStringArray(JSONArray arr){
		String[] result = new String[arr.size()];
		for (int i = 0; i < result.length; i++){
			result[i] = (String) arr.get(i);
		}
		return result;
	}



	public static void loadExampleFile(){
		try (Reader r = new InputStreamReader(CustomEntryLoader.class.getResourceAsStream("/examples/examples.json"))){
			new CustomEntryLoader().loadCustomEntries(r);
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Load first file from:
	 * - helden.jar directory
	 * - helden.zip.hld directory
	 */
	public static void loadFiles(){
		try {
			// Config files next to helden.jar
			File jarpath = (new CommUtilities()).getJarPath();
			if (loadFiles(jarpath)) return;
			if (jarpath.getName().toLowerCase().endsWith(".jar")){
				if (loadFiles(jarpath.getParentFile())) return;
			}

			// Config files next to helden.zip.hld
			File heldenPath = new File(new File(System.getProperty("user.home")), "helden");
			if (!loadFiles(heldenPath)) return;

		} catch (Throwable e){
			// Show message box and exit
			e.printStackTrace();
			String msg = e.getMessage();
			while ((e instanceof RuntimeException) && e.getCause() != null) e = e.getCause();
			JOptionPane.showMessageDialog(null, msg, e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	public static boolean loadFiles(File folder) throws ParseException {
		return
				loadFile(new File(folder, "customentries.json")) |
				loadFile(new File(folder, "erweiterungen.json"));
	}

	public static boolean loadFile(File f) throws ParseException {
		if (!f.exists()) {
			System.err.println("Keine Erweiterungen in "+f.getAbsolutePath());
			return false;
		}
		System.err.println("Lade Erweiterungen von \""+f.getAbsolutePath()+"\"");
		try (Reader r = new BufferedReader(new FileReader(f))){
			new CustomEntryLoader().loadCustomEntries(r);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

}
