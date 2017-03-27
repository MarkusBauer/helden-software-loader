package de.mb.heldensoftware.customentries;

import helden.framework.held.persistenz.ModsDatenParser;
import helden.framework.settings.Setting;
import helden.framework.zauber.Zauber;
import helden.framework.zauber.ZauberFabrik;
import helden.framework.zauber.ZauberVerbreitung;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Markus on 19.03.2017.
 */
public class EntryCreator {

	private static EntryCreator instance;

	public static EntryCreator getInstance(){
		if (instance == null) instance = new EntryCreator();
		return instance;
	}


	// Methods of Zauber: String => Method
	Map<String, Method> methods = new HashMap<>();

	// Container für 3 Eigenschaften
	Class talentprobeType;

	// new Talentprobe: (Eigenschaft, Eigenschaft, Eigenschaft) -> Talentprobe
	Constructor talentprobeConstructor;

	// Zauber.getTalentprobe: () -> Talentprobe
	Method getTalentprobe;

	// Eigenschaft ("MU", "KL", ...)
	Class eigenschaftType;
	// Name => Eigenschaft
	Map<String, Object> alleEigenschaften = new HashMap<>();

	// Kategorie: "A", "B", ..., "H"
	Class kategorieType;

	// Zauber.getKategorie: boolean -> Kategorie (Parameter wird ignoriert?)
	Method getKategorie;
	// Spalte => Kategorie
	Map<String, Object> alleKategorien = new HashMap<>();

	// Merkmal ("Antimagie", ...)
	Class merkmalType;
	// Name => Merkmal (auch Kurzfassungen funktionieren)
	Map<String, Object> alleMerkmale = new HashMap<>();

	// QuellenObj := (String, int), z.B. ("LCD", 123)
	Class quellenObjType;

	// new QuellenObj: (String, int) -> QuellenObj
	Constructor quellenObjConstructor;

	// Repräsentation ("Mag", "Hexe", ...)
	Class representationType;
	// Name => Repräsentation
	Map<String, Object> alleRepresentationen = new HashMap<>();

	// new Zauber: (String name, Kategorie spalte, Merkmal[] merkmale, Probe probe, QuellenObj quellenangabe, String mod) -> Zauber
	Constructor<Zauber> newZauber = null;

	// new ZauberVerbreitung: (Repräsentation bekanntBei, Repräsentation bekanntIn, int verbreitung) -> ZauberVerbreitung
	Constructor<ZauberVerbreitung> newZauberVerbreitung;

	// Zauber.setSpezialisierungen: (ArrayList<String> spezialisierungen) -> void
	Method zauberSetSpezialisierungen;

	// Talente
	Class TalentType;

	// TalentArt (Natur, ...)
	Class TalentArtType;
	// TalentArt.isPrimitive: () -> boolean
	Method talentArtIsPrimitive;

	/**
	 * Resolves all reflection references to helden-software
	 */
	private EntryCreator(){
		try {
			for (Method m : Zauber.class.getMethods()) {
				methods.put(m.getName(), m);
			}

			// Talentproben/Eigenschaften
			getTalentprobe = Zauber.class.getMethod("getTalentprobe");
			talentprobeType = getTalentprobe.getReturnType();
			talentprobeConstructor = talentprobeType.getConstructors()[0];
			for (Method m: talentprobeType.getDeclaredMethods()){
				if (m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == int.class)
					eigenschaftType = m.getReturnType();
			}
			assert eigenschaftType != null;
			createStringMap(alleEigenschaften, eigenschaftType);

			// Steigerungsspalten
			getKategorie = Zauber.class.getMethod("getKategorie", boolean.class);
			kategorieType = getKategorie.getReturnType();
			for (Field f: kategorieType.getDeclaredFields()){
				if (f.getType().equals(kategorieType)) {
					Object kat = f.get(null);
					alleKategorien.put(kat.toString(), kat);
				}
			}

			// Merkmale
			Method getMerkmale = methods.get("getMerkmale");
			assert getMerkmale != null;
			merkmalType = getMerkmale.getReturnType().getComponentType();
			createStringMap(alleMerkmale, merkmalType);

			// Quelle
			quellenObjType = Zauber.class.getMethod("getQuellenObj").getReturnType();
			quellenObjConstructor = quellenObjType.getConstructors()[0];

			// Repräsentation
			representationType = Zauber.class.getMethod("getRepraesentationen").getReturnType().getComponentType();
			createStringMap(alleRepresentationen, representationType);

			// Zauber
			for (Constructor<?> c : Zauber.class.getConstructors()) {
				if (c.getParameterTypes().length == 6) newZauber = (Constructor<Zauber>) c;
			}
			assert newZauber != null;
			zauberSetSpezialisierungen = methods.get("setSpezialisierungen");
			newZauberVerbreitung = (Constructor<ZauberVerbreitung>) ZauberVerbreitung.class.getConstructors()[0];

			// Talent
			Method einlesenTalent = ModsDatenParser.class.getMethod("einlesenTalent", File.class);
			TalentType = einlesenTalent.getReturnType();

			// TalentArt
			TalentArtType = TalentType.getConstructors()[0].getParameterTypes()[1];
			for (Method m: TalentArtType.getDeclaredMethods()){
				if (m.getReturnType().equals(Boolean.TYPE) && m.getParameterTypes().length == 0){
					if (talentArtIsPrimitive != null) throw new RuntimeException();
					talentArtIsPrimitive = m;
				}
			}

		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}


	/**
	 * Takes all static instances of this class, runs all "String _()" methods on them, and fills a dictionary
	 * @param map
	 * @param type
	 */
	protected void createStringMap(Map<String, Object> map, Class type) throws Exception {
		ArrayList<Method> stringMethods = new ArrayList<>();
		for (Method m : type.getDeclaredMethods()) {
			if (m.getReturnType().equals(String.class) && m.getParameterTypes().length == 0) {
				stringMethods.add(m);
			}
		}

		for (Field f : type.getDeclaredFields()) {
			if (f.getType().equals(type)) {
				Object instance = f.get(null);
				for (Method m : stringMethods) {
					try {
						map.put((String) m.invoke(instance), instance);
					}catch(InvocationTargetException e){
						if (e.getCause() instanceof IllegalArgumentException){
						}else{
							throw e;
						}
					}
				}
			}
		}
	}

	protected List<Object> getAllStaticInstances(Class type) throws IllegalAccessException {
		ArrayList<Object> result = new ArrayList<>();
		for (Field f : type.getDeclaredFields()) {
			if (f.getType().equals(type)) {
				Object instance = f.get(null);
				result.add(instance);
			}
		}
		return result;
	}

	/**
	 * Converts an array of Strings to an array of the corresponding "Merkmal" instances
	 * @param merkmale
	 * @return
	 */
	private Object getMerkmale(String[] merkmale){
		Object result = Array.newInstance(merkmalType, merkmale.length);
		for (int i = 0; i < merkmale.length; i++){
			Object mkml = alleMerkmale.get(merkmale[i]);
			if (mkml == null) throw new IllegalArgumentException("Unbekanntes Merkmal: "+merkmale[i]);
			Array.set(result, i, mkml);
		}
		return result;
	}


	/**
	 * Erstellt und registriert einen neuen Zauber
	 * @param name
	 * @param kategorie Steigerungsspalte ("A" - "H")
	 * @param merkmale Als String-Array: ["Anti", "Einfluss", "Dämonisch (Blakharaz)"]
	 * @param probe
	 * @param q Quellenangabe ("LCD: 123"), null ist erlaubt
	 * @param mod Modifikationen der Probe ("", "+MR", "+Mod", ...), null ist erlaubt
	 * @return
	 */
	public ZauberWrapper createSpell(String name, String kategorie, String[] merkmale, Probe probe, Quellenangabe q, String mod){
		try {
			if (q == null)
				q = Quellenangabe.leereQuelle;
			if (mod == null)
				mod = "";
			Object kat = alleKategorien.get(kategorie);
			if (isSpellKnown(name))
				throw new IllegalArgumentException("Zauber \""+name+"\" ist bereits bekannt!");
			Zauber newspell = newZauber.newInstance(name, kat, probe.getProbe(), getMerkmale(merkmale), q.getQuellenObj(), mod);
			return new ZauberWrapper(name, newspell);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param name
	 * @return <code>true</code> falls ein Zauber mit dem Namen <code>name</code> bekannt ist
	 * @see helden.framework.zauber.ZauberFabrik
	 */
	private boolean isSpellKnown(String name){
		try {
			return ZauberFabrik.getInstance().getZauberfertigkeit(name) != null;
		} catch (RuntimeException e){
			return false;
		}
	}


	/**
	 * Eine Probe aus drei Eigenschaften. Bsp: "(MU/KL/KO)"
	 */
	public static class Probe{

		public final String p1, p2, p3;

		public Probe(String p1, String p2, String p3){
			assert getInstance().alleEigenschaften.containsKey(p1);
			assert instance.alleEigenschaften.containsKey(p2);
			assert instance.alleEigenschaften.containsKey(p3);
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
		}

		public Probe(String probe){
			if (probe.startsWith("(") && probe.endsWith(")"))
				probe = probe.substring(1, probe.length()-1);
			String[] p = probe.split("/");
			assert p.length == 3;
			for (String s: p) {
				assert getInstance().alleEigenschaften.containsKey(p[0]);
			}
			this.p1 = p[0];
			this.p2 = p[1];
			this.p3 = p[2];
		}

		public Object getProbe() throws Exception {
			Object o1 = instance.alleEigenschaften.get(p1);
			if (o1 == null) throw new IllegalArgumentException("Unbekannte Eigenschaft: "+p1);
			Object o2 = instance.alleEigenschaften.get(p2);
			if (o2 == null) throw new IllegalArgumentException("Unbekannte Eigenschaft: "+p2);
			Object o3 = instance.alleEigenschaften.get(p3);
			if (o3 == null) throw new IllegalArgumentException("Unbekannte Eigenschaft: "+p3);
			return instance.talentprobeConstructor.newInstance(o1, o2, o3);
		}

	}


	/**
	 * Eine Quellenangabe, bestehend aus Buchkürzel und Seitenzahl ("LCD:123")
	 */
	public static class Quellenangabe{
		public final String book;
		public final int page;

		public static final Quellenangabe leereQuelle = new Quellenangabe("", 0);

		public Quellenangabe(String book, int page){
			this.book = book;
			this.page = page;
		}

		public Quellenangabe(String quelle) {
			int p = quelle.lastIndexOf(':');
			if (p > 0){
				this.book = quelle.substring(0, p);
				this.page = Integer.parseInt(quelle.substring(p + 1));
			}else{
				this.book = quelle;
				this.page = 0;
			}
		}

		public Object getQuellenObj() throws Exception{
			return getInstance().quellenObjConstructor.newInstance(book, page);
		}
	}


	public static class ZauberWrapper {
		public final String name;
		public final Zauber zauber;

		public ZauberWrapper(String name, Zauber zauber) {
			this.name = name;
			this.zauber = zauber;
		}

		/**
		 * Verbreitung - notwendig zum Erlernen/Aktivieren
		 * @param repr "Mag", "Hexe", "Dru(Elf)"
		 * @param num
		 */
		public void addVerbreitung(String repr, int num) {
			Matcher m = reprPattern.matcher(repr);
			if (m.matches()){
				addVerbreitung(m.group(1), m.group(2), num);
			}else {
				addVerbreitung(repr, repr, num);
			}
		}
		private static final Pattern reprPattern = Pattern.compile("^(\\w{3})\\s?\\((\\w{3})\\)$");

		/**
		 * (Dru, Elf, 3) => Dru(Elf) 3
		 * @param repr
		 * @param bekanntIn
		 * @param num
		 * @see helden.framework.zauber.ZauberVerbreitung
		 */
		public void addVerbreitung(String repr, String bekanntIn, int num) {
			Object reprObj = instance.alleRepresentationen.get(repr);
			if (reprObj == null) throw new IllegalArgumentException("Unbekannte Repräsentation: "+repr);
			Object bekanntObj = instance.alleRepresentationen.get(bekanntIn);
			if (bekanntObj == null) throw new IllegalArgumentException("Unbekannte Repräsentation: "+bekanntIn);
			try {
				zauber.getVerbreitung().add(instance.newZauberVerbreitung.newInstance(reprObj, bekanntObj, num));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// Zauber.setSpezialisierungen(ArrayList[String])
		public void setSpezialisierungen(String... spezis) {
			setSpezialisierungen(new ArrayList<String>(Arrays.asList(spezis)));
		}

		public void setSpezialisierungen(ArrayList<String> spezis) {
			try {
				instance.zauberSetSpezialisierungen.invoke(zauber, spezis);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * "Aventurien", "DSA4.1", "Myranor", "Tharun", "Alle", ...
		 * @param settingName
		 * @see helden.framework.settings.Setting
		 */
		public void addToSetting(String settingName) {
			if (settingName.equals("all") || settingName.equals("Alle")){
				addToAllSettings();
			} else {
				if (settingName.equals("Aventurien")) settingName = "DSA4.1";
				Setting setting = Setting.getByName(settingName);
				setting.getIncluded().add("Z" + name);
			}
		}

		public void addToAllSettings() {
			Setting.initAll();
			for (Setting setting : Setting.getHauptSettings()) {
				setting.getIncluded().add("Z" + name);
			}
		}

	}

}
