package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.EntryCreator.Probe;
import de.mb.heldensoftware.customentries.EntryCreator.Quellenangabe;
import de.mb.heldensoftware.customentries.EntryCreator.ZauberWrapper;
import de.mb.heldensoftware.customentries.config.*;
import helden.comm.CommUtilities;
import helden.framework.Einstellungen;
import helden.framework.bedingungen.AbstraktBedingung;
import helden.framework.bedingungen.Bedingung;
import helden.framework.bedingungen.BedingungsVerknuepfung;
import helden.framework.held.persistenz.XMLEinstellungenParser;
import helden.framework.zauber.Zauber;
import helden.framework.zauber.ZauberFabrik;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


/**
 * Created by Markus on 20.03.2017.
 */
public class CustomEntryLoader {

    protected HashSet<String> newSFNames = new HashSet<>();

    protected void loadCustomEntries(Config config) throws IOException {
        // build a list of new SF names
        for (SonderfertigkeitConfig sf : config.sonderfertigkeiten) {
            newSFNames.add(sf.name);
        }

        // load all zauber
        for (ZauberConfig zauber : config.zauber) {
            loadZauber(zauber);
        }

        // load all sonderfertigkeit
        for (SonderfertigkeitConfig sf : config.sonderfertigkeiten) {
            loadSF(sf);
        }

        // load all repräsentationen
        for (RepraesentationConfig repr : config.repraesentationen) {
            loadRepresentation(repr);
        }
    }

    protected void loadZauber(ZauberConfig zauber) {
        // Read parameters
        Probe probe = new Probe(zauber.probe);
        Quellenangabe quelle = zauber.quelle.isEmpty() ? Quellenangabe.leereQuelle : new Quellenangabe(zauber.quelle);

        // Create spell
        ZauberWrapper zw = EntryCreator.getInstance().createSpell(zauber.name, zauber.kategorie, zauber.merkmale, probe, quelle, zauber.mod);

        // Settings
        for (String setting : zauber.settings) {
            zw.addToSetting(setting);
        }

        // Verbreitung
        for (Map.Entry<String, Integer> e : zauber.verbreitung.entrySet()) {
            zw.addVerbreitung(e.getKey(), e.getValue());
        }

        // Spezialisierungen
        if (!zauber.spezialisierungen.isEmpty()) {
            zw.setSpezialisierungen(zauber.spezialisierungen);
        }
    }

    protected void loadSF(SonderfertigkeitConfig sf) {
        sf.autoFixNames();

        int cat = getSFCategory(sf.kategorie);
        BedingungsVerknuepfung bedingung = sf.bedingungen.isEmpty() ? null : loadBedingungen(sf.bedingungen, false);

        // Liturgien
        if (cat == 10) {
            if (sf.grad == 0)
                throw new RuntimeException("Eigene Liturgie: \"grad\" fehlt.");
            EntryCreator.getInstance().createLiturgieSonderfertigkeit(sf.name, sf.grad, sf.liturgiekenntnis, bedingung);
            return;
        }

        Object sfname;
        if (!sf.varianten.isEmpty()) {
            // SF mit Varianten
            sfname = EntryCreator.getInstance().createSonderfertigkeitWithParams(sf.name, sf.kosten, cat, bedingung, sf.varianten);
        } else {
            // normal SF
            sfname = EntryCreator.getInstance().createSonderfertigkeit(sf.name, sf.kosten, cat, bedingung);
        }

        // Liturgiekenntnis
        if (!sf.liturgien.isEmpty()) {
            EntryCreator.getInstance().addLiturgiekenntnisToLiturgien(sfname, sf.liturgien);
        }
    }

    protected int getSFCategory(String cat) {
        switch (cat) {
            case "":
            case "Allgemein":
                return 0;
            case "Geländekunde":
                return 1;
            case "Kampf: Nahkampf":
            case "Nahkampf":
                return 2;
            case "Kampf: Fernkampf":
            case "Fernkampf":
                return 3;
            case "Magisch":
                return 4;
            case "Magisch: Repräsentation":
            case "Repräsentation":
                return 5;
            case "Magisch: Merkmalskenntnis":
            case "Merkmalskenntnis":
                return 6;
            case "Magisch: Objektritual":
            case "Objektritual":
                return 7;
            case "Elfenlied":
                return 8;
            case "Kampf: Manöver":
            case "Manöver":
                return 9;
            case "Geweiht: Liturgie":
            case "Liturgie":
                return 10;
            case "Geweiht":
                return 11;
            case "Magisch: Schamanenritual":
            case "Schamanenritual":
                return 12;
            case "Magisch: Magische Lieder":
            case "Magische Lieder":
                return 13;
            default:
                System.err.println("[CustomEntryLoader] Unbekannte Kategorie: " + cat);
                return 0;
        }
    }

    protected BedingungsVerknuepfung loadBedingungen(ArrayList<BedingungConfig> bedingungen, boolean isOr) {
        ArrayList<AbstraktBedingung> lst = new ArrayList<>();
        for (BedingungConfig bc : bedingungen) {
            AbstraktBedingung b = null;
            switch (bc.type) {
                case or:
                    b = loadBedingungen(bc.bedingungen, true);
                    break;
                case and:
                    b = loadBedingungen(bc.bedingungen, false);
                    break;
                case Sonderfertigkeit:
                case SF:
                    b = EntryCreator.getInstance().createBedingungSF(bc.name, newSFNames.contains(bc.name));
                    break;
                case Zauber:
                    Zauber zauber = ZauberFabrik.getInstance().getZauberfertigkeit(bc.name);
                    if (zauber == null) throw new RuntimeException("Zauber \"" + bc.name + "\" nicht gefunden!");
                    b = EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(zauber, bc.value);
                    break;
                case Talent:
                    EntryCreator.getInstance().initTalentFactoryMap();
                    Object talent = EntryCreator.getInstance().talentFactoryMap.get(bc.name);
                    if (talent == null) throw new RuntimeException("Talent \"" + bc.name + "\" nicht gefunden!");
                    b = EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(talent, bc.value);
                    break;
                case Eigenschaft:
                    Object eigenschaft = EntryCreator.getInstance().alleEigenschaften.get(bc.name);
                    if (eigenschaft == null)
                        throw new RuntimeException("Eigenschaft \"" + bc.name + "\" nicht gefunden!");
                    b = EntryCreator.getInstance().createBedingungAbstrakteEigenschaft(eigenschaft, bc.value);
                    break;
                case LKW:
                    b = Bedingung.hatLkW(bc.value);
                    break;
                case MagieLevel:
                    Bedingung.MagieLevel level = EntryCreator.getInstance().alleMagielevel.get(bc.name);
                    if (level == null)
                        throw new RuntimeException("MagieLevel \"" + bc.name + "\" nicht gefunden!");
                    b = Bedingung.istMindestens(level);
                    break;
                default:
                    System.err.println("[CustomEntryLoader] Ignorierte Bedingung: " + bc.type);
                    throw new RuntimeException("Ungültige Bedingung: type \"" + bc.type + "\" ist nicht bekannt!");
            }

            if (b != null) {
                if (bc.not) {
                    if (b instanceof Bedingung) {
                        try {
                            EntryCreator.getInstance().bedingungSetNegieren.invoke(b, true);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new RuntimeException("Kann Bedingung nicht negieren: " + b);
                    }
                }
                lst.add(b);
            }
        }
        if (isOr) {
            return BedingungsVerknuepfung.OR(lst.toArray(new AbstraktBedingung[0]));
        } else {
            return BedingungsVerknuepfung.AND(lst.toArray(new AbstraktBedingung[0]));
        }
    }

    protected void loadRepresentation(RepraesentationConfig cfg) {
        EntryCreator.RepresentationWrapper repr = EntryCreator.getInstance().createRepresentation(cfg.name, cfg.abkuerzung, cfg.ritualkenntnis);
        for (Map.Entry<String, Integer> e: cfg.zauber.entrySet()) {
            repr.addZauber(e.getKey(), e.getValue());
        }
    }


    private static boolean filesLoaded = false;

    /**
     * Load first file from:
     * - helden.jar directory
     * - helden.zip.hld directory
     */
    public static void loadFiles() {
        if (filesLoaded) return;
        filesLoaded = true;

        // Check for custom paths in settings
        File einstellungen = new File(Einstellungen.getInstance().getPfade().getPfad("einstellungsPfad"));
        if (einstellungen.exists()) {
            try {
                new XMLEinstellungenParser().ladeEinstellungen(einstellungen);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            ArrayList<Config> configs = new ArrayList<>();

            // Config files next to helden.jar
            File jarpath = new CommUtilities().getJarPath();
            configs.addAll(loadFilesFromPath(jarpath));
            configs.addAll(loadFilesFromPath(new File(jarpath, "helden"))); // hslocal
            if (jarpath.getName().toLowerCase().endsWith(".jar")) {
                configs.addAll(loadFilesFromPath(jarpath.getParentFile()));
                configs.addAll(loadFilesFromPath(new File(jarpath.getParentFile(), "helden"))); // hslocal
            }
            // Config files next to helden.zip.hld
            File heldenPath = new File(new File(System.getProperty("user.home")), "helden");
            configs.addAll(loadFilesFromPath(heldenPath));

            File heldenPath2 = new File(Einstellungen.getInstance().getPfade().getPfad("heldenPfad")).getAbsoluteFile().getParentFile();
            if (!heldenPath2.equals(heldenPath)) {
                configs.addAll(loadFilesFromPath(heldenPath2));
            }

            CustomEntryLoader entryLoader = new CustomEntryLoader();
            ConfigMerger.ConfigMergeResult config = ConfigMerger.mergeAll(configs);
            if (!config.duplicates.isEmpty()){
                JOptionPane.showMessageDialog(null, String.join("\n", config.duplicates),
                        "Helden-Software-Loader: Eigene Einträge mehrfach vorhanden!", JOptionPane.WARNING_MESSAGE);
            }
            entryLoader.loadCustomEntries(config.config);

        } catch (Throwable e) {
            // Show message box and exit
            e.printStackTrace();
            String msg = e.getMessage() + "\n" + e.toString();
            while ((e instanceof RuntimeException) && e.getCause() != null && !(e instanceof Loader.ConfigError))
                e = e.getCause();
            JOptionPane.showMessageDialog(null, msg, e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

    }

    public static List<Config> loadFilesFromPath(File path) throws IOException {
        ArrayList<Config> configs = new ArrayList<>();

        // json/yaml/csv files
        for (String f : new String[]{
                "customentries.yaml",
                "erweiterungen.yaml",
                "erweiterungen.yaml.txt", // I know my dear Windows users...

                "customentries.json",
                "erweiterungen.json",
                "erweiterungen.json.txt", // I know my dear Windows users...

                "erweiterungen.csv",
                "erweiterungen-libreoffice.csv",
                "erweiterungen-excel.csv"
        }) {
            tryLoad(configs, new File(path, f));
        }

        // Subdir "erweiterungen"
        if (path.exists() && path.isDirectory()) {
            File subfolder = new File(path, "erweiterungen");
            if (subfolder.exists() && subfolder.isDirectory()) {
                for (File f : subfolder.listFiles()) {
                    tryLoad(configs, f);
                }
            }
        }

        return configs;
    }

    private static void tryLoad(List<Config> configs, File f) throws IOException {
        if (f.exists()) {
            Config c = Loader.load(f.toPath());
            if (c != null) {
                configs.add(c);
                System.err.println("[CustomEntryLoader] Lade " + f.getAbsolutePath());
            } else {
                System.err.println("[CustomEntryLoader] Unbekanntes Format: " + f.getAbsolutePath());
            }
        } else {
            System.err.println("[CustomEntryLoader] Keine Erweiterungen in " + f.getAbsolutePath());
        }
    }

}
