package de.mk_bauer.heldensoftware.customentries;

import helden.plugin.HeldenPlugin;

import javax.swing.*;

/**
 * Created by Markus on 19.03.2017.
 */
public class CustomEntryLoaderPlugin implements HeldenPlugin {

	static {
		System.err.println("STATIC CODE EXEC");
	}

	public CustomEntryLoaderPlugin(){
		System.err.println("CUSTOM LOADER loaded");
	}

	@Override
	public void doWork(JFrame jFrame) {
		System.out.println("doWork");
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getMenuName() {
		return "CustomEntryLoader";
	}

	@Override
	public String getToolTipText() {
		return "";
	}

	@Override
	public String getType() {
		new Exception("CALLED FROM").printStackTrace();
		return "";
	}
}
