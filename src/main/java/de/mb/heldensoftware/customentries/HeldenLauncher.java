package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.EntryCreator.*;
import helden.Helden;

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
		// Register the plugin component
		PluginSideloader.addPlugin(CustomEntryLoaderPlugin.class);
		PluginSideloader.registerSideloader();
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
