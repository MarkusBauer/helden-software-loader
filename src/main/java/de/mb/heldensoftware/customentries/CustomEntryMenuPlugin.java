package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.shared.TabPluginsPatch;
import helden.framework.Einstellungen;
import helden.plugin.HeldenXMLDatenPlugin3;
import helden.plugin.datenxmlplugin.DatenAustausch3Interface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;

public class CustomEntryMenuPlugin implements HeldenXMLDatenPlugin3 {
    static {
        TabPluginsPatch.install();
    }

    private JFrame jFrame = null;

    @Override
    public void init(DatenAustausch3Interface datenAustausch3Interface, JFrame jFrame) {
        this.jFrame = jFrame;
    }

    @Override
    public ArrayList<JComponent> getUntermenus() {
        ArrayList<JComponent> menu = new ArrayList<>();
        menu.add(new JMenuItem(new NewTalentAction()));
        menu.add(new JMenuItem(new OpenSettingsAction()));
        menu.add(new JMenuItem(new OpenDocsAction()));
        return menu;
    }

    @Override
    public String getMenuName() {
        return "Eigene Zauber/Talente/etc";
    }


    public class NewTalentAction extends AbstractAction {
        public NewTalentAction() {
            super("Eigenes Talent hinzufügen...");
            putValue(Action.SHORT_DESCRIPTION, "Ein neues, inoffizielles Talent erstellen und dem aktuellen Helden hinzufügen");
        }


        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                EntryCreator ec = EntryCreator.getInstance();
                final Object held = ec.getCurrentHeld.invoke(ec.getMainWindowInstance.invoke(null));

                NewTalentDialog dialog = new NewTalentDialog(jFrame);
                dialog.setNewTalentCallback(new NewTalentDialog.TalentCallback() {
                    @Override
                    public void talentCreated(Object talent, int value) {
                        EntryCreator.getInstance().addTalentToHeld(held, talent, value);
                    }
                });
                dialog.pack();
                dialog.setVisible(true);

            } catch (InvocationTargetException | IllegalAccessException e) {
                ErrorHandler.handleException(e);
            }
        }
    }


    public class OpenSettingsAction extends AbstractAction {
        public OpenSettingsAction() {
            super("Eigene Zauber/SF/etc bearbeiten...");
            putValue(Action.SHORT_DESCRIPTION, "Bearbeite die eigenen Zauber / Sonderfertigkeiten / Repräsentationen / ...");
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
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
                ErrorHandler.handleException(e);
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
    }

    public static class OpenDocsAction extends AbstractAction {
        public OpenDocsAction() {
            super("Dokumentation...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://github.com/MarkusBauer/helden-software-loader/blob/master/README.md"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void click() {
    }

    @Override
    public JComponent getPanel() {
        return null;
    }

    @Override
    public boolean hatMenu() {
        return true;
    }

    @Override
    public boolean hatTab() {
        return false;
    }

    @Override
    public void doWork(JFrame jFrame) {
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return "";
    }

    @Override
    public String getType() {
        return DATEN;
    }
}
