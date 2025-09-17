package de.mb.heldensoftware.shared;

import helden.plugin.HeldenPlugin;
import helden.plugin.HeldenPluginFactory;
import helden.plugin.HeldenXMLDatenPlugin3;
import helden.plugin.datenxmlplugin.DatenAustausch3Impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// One more round of hacking:
// We do NOT want a tab for this plugin. To archive that, we "remove" it from HeldenPluginFactory.tabplugins.
// We also lose the ChangeListener, but we don't really need them anyway.
// This fixes a known bug (https://forum.helden-software.de/viewtopic.php?f=43&t=4202).
public class TabPluginsPatch {
    private static boolean installed = false;

    public static void install() {
        if (installed) return;

        try {
            Field tabplugins = HeldenPluginFactory.class.getDeclaredField("tabplugins");
            tabplugins.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<HeldenPlugin, DatenAustausch3Impl> oldmap = (Map<HeldenPlugin, DatenAustausch3Impl>) tabplugins.get(null);
            tabplugins.set(null, new HashMap<HeldenPlugin, DatenAustausch3Impl>(oldmap) {
                public DatenAustausch3Impl put(HeldenPlugin plugin, DatenAustausch3Impl x) {
                    // drop all "put" that contain an instance of plugins without tabs
                    if (plugin instanceof HeldenXMLDatenPlugin3 && !((HeldenXMLDatenPlugin3) plugin).hatTab())
                        return null;
                    return super.put(plugin, x);
                }
            });

            installed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
