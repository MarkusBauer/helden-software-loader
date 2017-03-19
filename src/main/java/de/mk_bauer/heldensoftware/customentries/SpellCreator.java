package de.mk_bauer.heldensoftware.customentries;

import helden.framework.settings.Setting;
import helden.framework.zauber.Zauber;
import helden.framework.zauber.ZauberVerbreitung;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static de.mk_bauer.heldensoftware.customentries.SpellCreator.Quellenangabe.leereQuelle;

/**
 * Created by Markus on 19.03.2017.
 */
public class SpellCreator {

	private static SpellCreator instance;

	public static SpellCreator getInstance(){
		if (instance == null) instance = new SpellCreator();
		return instance;
	}


	// Methods of Zauber
	Map<String, Method> methods = new HashMap<>();
	Method getTalentprobe;
	Class talentprobeType;
	Constructor talentprobeConstructor;
	Class eigenschaftType;
	Map<String, Object> alleEigenschaften = new HashMap<>();
	Method getKategorie;
	Map<String, Object> alleKategorien = new HashMap<>();
	Class merkmalType;
	Map<String, Object> alleMerkmale = new HashMap<>();
	Class quellenObjType;
	Constructor quellenObjConstructor;
	Class representationType;
	Map<String, Object> alleRepresentationen = new HashMap<>();
	Constructor<Zauber> newZauber = null;
	Constructor<ZauberVerbreitung> newZauberVerbreitung;
	Method zauberSetSpezialisierungen;

	private SpellCreator(){
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
			Class kategorieType = getKategorie.getReturnType();
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
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}


	/**
	 * Takes all static instances of this class, runs all "String _()" methods on them, and fills a dictionary
	 * @param map
	 * @param type
	 */
	private void createStringMap(Map<String, Object> map, Class type) throws Exception {
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

	private Object getMerkmale(String[] merkmale){
		Object result = Array.newInstance(merkmalType, merkmale.length);
		for (int i = 0; i < merkmale.length; i++){
			Object mkml = alleMerkmale.get(merkmale[i]);
			assert mkml != null;
			Array.set(result, i, mkml);
		}
		return result;
	}


	public ZauberWrapper createSpell(String name, String kategorie, String[] merkmale, Probe probe, Quellenangabe q, String mod){
		try {
			if (q == null)
				q = Quellenangabe.leereQuelle;
			if (mod == null)
				mod = "";
			Object kat = alleKategorien.get(kategorie);
			Zauber newspell = newZauber.newInstance(name, kat, probe.getProbe(), getMerkmale(merkmale), q.getQuellenObj(), mod);
			return new ZauberWrapper(name, newspell);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}







	public static class Probe{

		private String p1, p2, p3;

		public Probe(String p1, String p2, String p3){
			assert getInstance().alleEigenschaften.containsKey(p1);
			assert instance.alleEigenschaften.containsKey(p2);
			assert instance.alleEigenschaften.containsKey(p3);
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
		}

		public Object getProbe() throws Exception {
			return instance.talentprobeConstructor.newInstance(instance.alleEigenschaften.get(p1), instance.alleEigenschaften.get(p2), instance.alleEigenschaften.get(p3));
		}

	}


	public static class Quellenangabe{
		private String book;
		private int page;

		public static final Quellenangabe leereQuelle = new Quellenangabe("", 0);

		public Quellenangabe(String book, int page){
			this.book = book;
			this.page = page;
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

		public void addVerbreitung(String repr, int num) {
			addVerbreitung(repr, repr, num);
		}

		public void addVerbreitung(String repr, String bekanntIn, int num) {
			Object reprObj = instance.alleRepresentationen.get(repr);
			assert reprObj != null;
			Object bekanntObj = instance.alleRepresentationen.get(bekanntIn);
			assert bekanntObj != null;
			try {
				System.out.println(zauber.getVerbreitung());
				zauber.getVerbreitung().add(instance.newZauberVerbreitung.newInstance(reprObj, bekanntObj, num));
				System.out.println(zauber.getVerbreitung());
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

		public void addToSetting(String settingName) {
			if (settingName.equals("Aventurien")) settingName = "DSA4.1";
			Setting setting = Setting.getByName(settingName);
			setting.getIncluded().add("Z" + name);
		}

		public void addToAllSettings() {
			Setting.initAll();
			for (Setting setting : Setting.getHauptSettings()) {
				setting.getIncluded().add("Z" + name);
			}
		}

	}

}
