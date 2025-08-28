package de.mb.heldensoftware.customentries;

import helden.framework.Einstellungen;
import helden.plugin.HeldenDatenPlugin;
import helden.plugin.datenplugin.DatenPluginHeldenWerkzeug;
import helden.plugin.werteplugin2.PluginHeld2;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class CreateConfigFilePlugin implements HeldenDatenPlugin {

    private static final ImageIcon PLUGINICON = new ImageIcon();
    private static final String VERSION = "1.0";

    @Override
    public int compareVersion(String s) {
        return String.CASE_INSENSITIVE_ORDER.compare(getVersion(), s);
    }

    @Override
    public void doWork(JFrame jFrame) {
    }

    @Override
    public ImageIcon getIcon() {
        return PLUGINICON;
    }

    @Override
    public String getMenuName() {
        return "Eigene Zauber etc bearbeiten...";
    }

    @Override
    public String getToolTipText() {
        return "Bearbeite die eigenen Zauber / Sonderfertigkeiten / Repräsentationen / ...";
    }

    @Override
    public String getType() {
        return HELDENDATEN;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void doWork(JFrame jFrame, PluginHeld2[] pluginHeld2s, DatenPluginHeldenWerkzeug datenPluginHeldenWerkzeug) {
        File f = CustomEntryLoader.getFirstConfigFile();
        if (f == null) {
            f = new File(new File(Einstellungen.getInstance().getPfade().getPfad("heldenPfad")).getParentFile(), "erweiterungen.yaml");
        }
        if (!f.exists()) {
            try (InputStream is = getClass().getResourceAsStream("/empty.yaml")) {
                Files.copy(is, f.toPath());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create initial config file " + f.getAbsolutePath(), e);
            }
            JOptionPane.showMessageDialog(jFrame, "Die Datei " + f.getAbsolutePath() + " wurde angelegt.");
        }

        try {
            Desktop.getDesktop().open(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initTab(DatenPluginHeldenWerkzeug datenPluginHeldenWerkzeug) {
    }
}
