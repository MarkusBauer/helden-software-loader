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
import java.net.URI;
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

        openConfigFile(jFrame, f);
    }

    private void openConfigFile(JFrame jFrame, File f) {
        try {
            if (isFileAssoicationMissing(".yaml")) {
                int result = JOptionPane.showOptionDialog(
                        jFrame,
                        "Erweiterungen werden in Yaml-Dateien konfiguriert. \n" +
                                "Auf ihrem System ist kein Editor für Yaml-Dateien verfügbar. \n" +
                                "Sie können beispielsweise VS Code herunterladen, oder die Einstellungsdatei mit Notepad bearbeiten.",
                        "Editor wählen",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[]{"VS Code herunterladen", "Notepad benutzen", "Abbrechen"},
                        "Notepad benutzen"
                );
                if (result == 0) {
                    Desktop.getDesktop().browse(URI.create("https://code.visualstudio.com/Download"));
                } else if (result == 1) {
                    Runtime.getRuntime().exec("notepad.exe \"" + f.getAbsolutePath() + "\"");
                }

            } else {
                Desktop.getDesktop().open(f);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isFileAssoicationMissing(String ext) {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return false;
        }

        try {
            // check for global assignment of extension
            Process p = Runtime.getRuntime().exec("reg query HKLM\\SOFTWARE\\Classes\\" + ext + "\\ /ve");
            if (p.waitFor() != 0) {
                // check for local assignment of extension
                p = Runtime.getRuntime().exec("reg query HKCU\\SOFTWARE\\Classes\\" + ext + "\\ /ve");
                if (p.waitFor() != 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void initTab(DatenPluginHeldenWerkzeug datenPluginHeldenWerkzeug) {
    }
}
