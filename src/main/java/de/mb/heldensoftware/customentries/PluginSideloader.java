package de.mb.heldensoftware.customentries;

import helden.plugin.HeldenDatenPlugin;
import helden.plugin.HeldenPluginFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * This class loads Helden-Software plugins without packing them to jar files lying in the plugin directory.
 * Call {@link #addPlugin(Class)} (possibly multiple times) followed by a call to {@link #registerSideloader()} -
 * before any reference to {@link HeldenPluginFactory} appears.
 */
public class PluginSideloader {

	private static Set<Class<? extends HeldenDatenPlugin>> sideloadedPlugins = new HashSet<>();

	public static void addPlugin(Class<? extends HeldenDatenPlugin> plugin){
		sideloadedPlugins.add(plugin);
	}

	public static void registerSideloader(){
		try {
			patchHeldenPluginFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private static void patchHeldenPluginFactory() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
		InstrumentationEngine inst = new InstrumentationEngine("helden.plugin.HeldenPluginFactory");
		inst.addMethodInstrumentation("getAlleHeldenDatenPlugins", new InstrumentationEngine.MethodResultAModifier(PluginSideloader.class, "afterGetAlleHeldenDatenPlugins"));
		inst.patchClass();
	}


	/**
	 * Patch for {@link HeldenPluginFactory#getAlleHeldenDatenPlugins()}
	 * Adds sideloaded plugins to list
	 * @param plugins
	 * @return
	 */
	public static List<HeldenDatenPlugin> afterGetAlleHeldenDatenPlugins(List<HeldenDatenPlugin> plugins){
		Set<Class<? extends HeldenDatenPlugin>> remainingPlugins = new HashSet<>(sideloadedPlugins);
		for (HeldenDatenPlugin p: plugins){
			remainingPlugins.remove(p.getClass());
		}
		for (Class<? extends HeldenDatenPlugin> plugin: remainingPlugins){
			try {
				plugins.add(plugin.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return plugins;
	}

}
