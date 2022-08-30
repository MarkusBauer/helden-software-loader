package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.EntryCreator.*;
import helden.Helden;

import javax.swing.*;

/**
 * Created by Markus on 19.03.2017.
 */
public class HeldenLauncher {

	public static void addExampleSpell(){
		EntryCreator sc = EntryCreator.getInstance();
		ZauberWrapper z = sc.createSpell("Inarcanitas", "F", new String[]{"Anti", "Krft", "Meta"}, new Probe("MU", "KL", "KL"), Quellenangabe.leereQuelle, "");
		z.addVerbreitung("Mag", 7);
		z.addVerbreitung("Elf", 5);
		z.addVerbreitung("Hex", "Geo", 2);
		z.setSpezialisierungen("Reichweite");
		z.addToAllSettings();
	}

	public static void main(String[] args) {
		try {
			// Register the plugin component
			PluginSideloader.addPlugin(CustomEntryLoaderPlugin.class);
			PluginSideloader.registerSideloader();
		} catch (RuntimeException e) {
			// Java 9+ module API prevents us from modifying the classloader.
			if (e.getClass().getName().contains("InaccessibleObjectException")) {
				JOptionPane.showMessageDialog(null,
						"Die Java-Konfiguration verbietet Modifikationen am Class-Loader. \n\n" +
						"Um Modifikationen zu erlauben, sind diese Start-Parameter notwendig (zwischen java und -jar):\n"+
						"--add-opens java.base/java.lang=ALL-UNNAMED\n\n"+
						"Alternativ können Sie eine ältere Java-Version einsetzen (bspw. Java 11).",
						"Java Configuration Error", JOptionPane.ERROR_MESSAGE);
			}
			throw e;
		}
		// Patch bugs
		ErrorHandler.patchHeldenErrorHandler();
		ModsDatenParserBugPatcher.patchModsDatenParser();
		// Resolve reflection references (after patches are deployed, before HeldenSoftware initializes anything)
		EntryCreator.getInstance();
		// Load the non-plugin component
		CustomEntryLoader.loadFiles();
		// Launch Helden-Software
		Helden.main(args);
	}

}
