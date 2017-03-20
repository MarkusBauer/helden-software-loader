package de.mk_bauer.heldensoftware.customentries;

import de.mk_bauer.heldensoftware.customentries.SpellCreator.*;
import helden.Helden;

import javax.swing.*;

/**
 * Created by Markus on 19.03.2017.
 */
public class HeldenLauncher {

	public static void addExampleSpell(){
		SpellCreator sc = SpellCreator.getInstance();
		ZauberWrapper z = sc.createSpell("Inarcanitas", "F", new String[]{"Anti", "Krft", "Meta"}, new Probe("MU", "KL", "KL"), Quellenangabe.leereQuelle, "");
		z.addVerbreitung("Mag", 7);
		z.addVerbreitung("Elf", 5);
		z.addVerbreitung("Hex", "Geo", 2);
		z.setSpezialisierungen("Reichweite");
		z.addToAllSettings();
	}

	public static void main(String[] args) {
		CustomEntryLoader.loadFiles();
		// Launch Helden-Software
		Helden.main(args);
	}

}
