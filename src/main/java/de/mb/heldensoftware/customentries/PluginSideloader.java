package de.mb.heldensoftware.customentries;

import helden.plugin.HeldenDatenPlugin;
import helden.plugin.HeldenPlugin;
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

	private static Set<Class<? extends HeldenDatenPlugin>> sideloadedDatenPlugins = new HashSet<>();
	private static Set<Class<? extends HeldenPlugin>> sideloadedRawPlugins = new HashSet<>();

	public static void addPlugin(Class<? extends HeldenDatenPlugin> plugin){
		sideloadedDatenPlugins.add(plugin);
	}

	public static void addRawPlugin(Class<? extends HeldenPlugin> plugin){
		sideloadedRawPlugins.add(plugin);
	}

	public static void registerSideloader(){
		try {
			patchHeldenPluginFactory();
		} catch (Exception e) {
			ErrorHandler.handleException(e);
		}
	}


	private static void patchHeldenPluginFactory() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
		InstrumentationEngine inst = new InstrumentationEngine("helden.plugin.HeldenPluginFactory");
		inst.addMethodInstrumentation("getAlleHeldenDatenPlugins", new InstrumentationEngine.MethodResultAModifier(PluginSideloader.class, "afterGetAlleHeldenDatenPlugins"));
		inst.addMethodInstrumentation("getAlleHeldenPlugins", new InstrumentationEngine.MethodResultAModifier(PluginSideloader.class, "afterGetAlleHeldenPlugins"));
		inst.patchClass();
	}


	/**
	 * Patch for {@link HeldenPluginFactory#getAlleHeldenDatenPlugins()}
	 * Adds sideloaded plugins to list
	 * @param plugins
	 * @return
	 */
	public static List<HeldenDatenPlugin> afterGetAlleHeldenDatenPlugins(List<HeldenDatenPlugin> plugins){
		Set<Class<? extends HeldenDatenPlugin>> remainingPlugins = new HashSet<>(sideloadedDatenPlugins);
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

    /**
	 * Patch for {@link HeldenPluginFactory#getAlleHeldenPlugins()}
	 * Adds sideloaded plugins to list
	 * @param plugins
	 * @return
	 */
    public static Vector<HeldenPlugin> afterGetAlleHeldenPlugins(Vector<HeldenPlugin> plugins){
		Set<Class<? extends HeldenPlugin>> remainingPlugins = new HashSet<>(sideloadedRawPlugins);
		for (HeldenPlugin p: plugins){
			remainingPlugins.remove(p.getClass());
		}
		for (Class<? extends HeldenPlugin> plugin: remainingPlugins){
			try {
				plugins.add(plugin.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return plugins;
	}

}
