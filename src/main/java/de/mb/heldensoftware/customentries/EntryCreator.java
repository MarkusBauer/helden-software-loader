package de.mb.heldensoftware.customentries;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import de.mb.heldensoftware.customentries.config.Loader;
import de.mb.heldensoftware.customentries.config.SonderfertigkeitConfig;
import helden.cloudinterface.HeldenContainerImpl;
import helden.framework.EigeneErweiterungenMoeglich;
import helden.framework.bedingungen.AbstraktBedingung;
import helden.framework.bedingungen.Bedingung;
import helden.framework.bedingungen.BedingungsVerknuepfung;
import helden.framework.held.persistenz.BasisXMLParser;
import helden.framework.held.persistenz.ModsDatenParser;
import helden.framework.held.persistenz.XMLParserKonverter;
import helden.framework.settings.Setting;
import helden.framework.settings.Settings;
import helden.framework.zauber.Zauber;
import helden.framework.zauber.ZauberFabrik;
import helden.framework.zauber.ZauberVerbreitung;
import helden.plugin.datenplugin.impl.DatenPluginHeldenWerkzeugImpl;
import org.w3c.dom.Element;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Markus on 19.03.2017.
 */
@SuppressWarnings({"WeakerAccess", "JavaDoc"})
public class EntryCreator {

	private static EntryCreator instance;

	public static EntryCreator getInstance() {
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
	Constructor merkmalConstructor;
	// Name => Merkmal (auch Kurzfassungen funktionieren)
	Map<String, Object> alleMerkmale = new HashMap<>();
	Map<String, Object> merkmalKinds = new HashMap<>();  // "MERKMAL", "QUELLE", "BEIDES"

	// QuellenObj := (String, int), z.B. ("LCD", 123)
	Class quellenObjType;

	// new QuellenObj: (String, int) -> QuellenObj
	Constructor quellenObjConstructor;

	// Repräsentation ("Mag", "Hexe", ...)
	Class representationType;
	Constructor<?> newRepresentation;
	// Name => Repräsentation
	Map<String, Object> alleRepresentationen = new HashMap<>();

	// Sonderfertigkeit
	public Class sonderfertigkeitNameType;
	public Constructor<?> newSonderfertigkeitName;
	public Class sonderfertigkeitType;
	public Constructor<?> newSonderfertigkeit;
	public Class sonderfertigkeitLiturgieType;
	public Constructor<?> newLiturgieSonderfertigkeit;
	public Class sonderfertigkeitWithParamsType;
	public Constructor<?> newSonderfertigkeitWithParams;
	public Method sonderfertigkeitSetCorrespondingTalent;
	public Class sonderfertigkeitRegistryType;
	public Class sonderfertigkeitListType;
	public Method sonderfertigkeitListAdd;
	public Method sonderfertigkeitListGet;
	public Method sonderfertigkeitRegistryGetList;
	public Method newMerkmalskenntnis;

	// new Zauber: (String name, Kategorie spalte, Merkmal[] merkmale, Probe probe, QuellenObj quellenangabe, String mod) -> Zauber
	Constructor<Zauber> newZauber = null;

	// new ZauberVerbreitung: (Repräsentation bekanntBei, Repräsentation bekanntIn, int verbreitung) -> ZauberVerbreitung
	Constructor<ZauberVerbreitung> newZauberVerbreitung;

	// Zauber.setSpezialisierungen: (ArrayList<String> spezialisierungen) -> void
	Method zauberSetSpezialisierungen;

	// Talente
	Class TalentType;
	Class SprachTalentType;
	Class KampfTalentType;
	Class RunenFertigkeitType;
	Method talentSetProbe; // (probe) -> void , works for both Talent and Zauber

	// TalentArt (Natur, ...)
	Class TalentArtType;
	// TalentArt.isPrimitive: () -> boolean
	Method talentArtIsPrimitive;
	// Name => Art
	Map<String, Object> alleTalentArten = new HashMap<>();

	Class SprachFamilieType;

	// Eigene Talente
	Class EigenesTalentType;
	Class EigenesSprachTalentType;
	Class EigenesKampfTalentType;

	// Eigene Talente - Konstruktoren
	// newEigenesTalent: (String name, R art, boolean basistalent, L probe, Node xmlnode, String behinderung, String beschreibung, String urheber, String kontakt) -> EigenesTalent
	Constructor newEigenesTalent;

	// TalentFactory (singleton)
	Class TalentFactoryType;
	Object talentFactoryInst;
	Field talentFactoryMapField;
	ArrayList<Method> talentFactoryAccessors = new ArrayList<>();
	// Internal map: Talent.toString() -> Talent
	HashMap<String, Object> talentFactoryMap;

	// Held
	public Class HeldType;
    public Class HeldInterfaceType;
	Method heldAddTalent;
    public Class<?> MainWindowType;
    public Method getMainWindowInstance;
    public Method getCurrentHeld;

	// Bedingung
	Method bedingungHatSonderfertigkeit;
	Method bedingungHatAbstrakteEigenschaft;
	Method bedingungSetNegieren;
	Method sonderfertigkeitSetBedingung;
	Method sonderfertigkeitGetBedingung;

	HashMap<String, Bedingung.MagieLevel> alleMagielevel = new HashMap<>();

	private int numRepresentations = 0; // number of representations already created

	/**
	 * Resolves all reflection references to helden-software
	 */
	private EntryCreator() {
		try {
			for (Method m : Zauber.class.getMethods()) {
				methods.put(m.getName(), m);
			}

			// Talentproben/Eigenschaften
			getTalentprobe = Zauber.class.getMethod("getTalentprobe");
			talentprobeType = getTalentprobe.getReturnType();
			talentprobeConstructor = talentprobeType.getConstructors()[0];
			for (Method m : talentprobeType.getDeclaredMethods()) {
				if (m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == int.class)
					eigenschaftType = m.getReturnType();
			}
			assert eigenschaftType != null;
			createStringMap(alleEigenschaften, eigenschaftType);

			// Steigerungsspalten
			getKategorie = Zauber.class.getMethod("getKategorie", boolean.class);
			kategorieType = getKategorie.getReturnType();
			for (Field f : kategorieType.getDeclaredFields()) {
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
			for (Constructor c: merkmalType.getDeclaredConstructors()) {
				if (c.getParameterCount() == 5) {
					merkmalConstructor = c;
					merkmalConstructor.setAccessible(true);
					break;
				}
			}
			assert merkmalConstructor != null;
			for (Class subclass: merkmalType.getDeclaredClasses()) {
				Object[] merkmalKinds = subclass.getEnumConstants();
				if (merkmalKinds != null && merkmalKinds.length > 0) {
					for (Object kind: merkmalKinds) {
						this.merkmalKinds.put(kind.toString(), kind);
					}
				}
			}


			// Quelle
			quellenObjType = Zauber.class.getMethod("getQuellenObj").getReturnType();
			quellenObjConstructor = quellenObjType.getConstructors()[0];

			// Repräsentation
			representationType = Zauber.class.getMethod("getRepraesentationen").getReturnType().getComponentType();
			newRepresentation = representationType.getDeclaredConstructors()[0];
			newRepresentation.setAccessible(true);
			createStringMap(alleRepresentationen, representationType);

			// Sonderfertigkeit (from Repräsentation)
			for (Method m : representationType.getMethods()) {
				if (Modifier.isStatic(m.getModifiers())) continue;
				if (m.getReturnType().equals(String.class)) continue;
				sonderfertigkeitNameType = m.getReturnType();
				break;
			}
			assert sonderfertigkeitNameType != null;
			for (Constructor<?> c : sonderfertigkeitNameType.getConstructors()) {
				if (c.getParameterTypes().length == 1) newSonderfertigkeitName = c;
			}
			assert newSonderfertigkeitName != null;

			sonderfertigkeitType = getMethodByName(BasisXMLParser.class, "getSonderfertigkeit").getReturnType();
			assert sonderfertigkeitType != null;
			newSonderfertigkeit = sonderfertigkeitType.getDeclaredConstructor(String.class, int.class, int.class);
			assert newSonderfertigkeit != null;
			newSonderfertigkeit.setAccessible(true);
			sonderfertigkeitSetCorrespondingTalent = getVoidMethodByParameterType(sonderfertigkeitType, Zauber.class.getSuperclass());
			assert sonderfertigkeitSetCorrespondingTalent != null;

			newMerkmalskenntnis = getMethodByParameterTypes(sonderfertigkeitType, String.class, merkmalType);
			assert newMerkmalskenntnis != null;

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
			talentSetProbe = getVoidMethodByParameterType(TalentType.getSuperclass(), talentprobeType);
			if (talentSetProbe == null) throw new RuntimeException("talentSetProbe is null");
			talentSetProbe.setAccessible(true);

			// TalentArt
			TalentArtType = TalentType.getConstructors()[0].getParameterTypes()[1];
			for (Method m : TalentArtType.getDeclaredMethods()) {
				if (m.getReturnType().equals(Boolean.TYPE) && m.getParameterTypes().length == 0) {
					if (talentArtIsPrimitive != null) throw new RuntimeException();
					talentArtIsPrimitive = m;
				}
			}
			createStringMap(alleTalentArten, TalentArtType);

			// Talent subclasses
			// init Pojo Library hotfix
			ClassLoader tcl = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(EntryCreator.class.getClassLoader());
			List<PojoClass> classes = PojoClassFactory.enumerateClassesByExtendingType(TalentType.getPackage().getName(), TalentType, null);
			Thread.currentThread().setContextClassLoader(tcl);
			for (PojoClass pojoClass : classes) {
				Class c = pojoClass.getClazz();
				if (!EigeneErweiterungenMoeglich.class.isAssignableFrom(c)) {
					// Basisklasse
					List<Object> instances = getAllStaticInstances(c);
					if (instances.size() > 100) {
						// Sprachen / Schriften
						SprachTalentType = c;
						SprachFamilieType = c.getDeclaredClasses()[0];
					} else if (instances.size() > 28) {
						// Kampftalent
						KampfTalentType = c;
					} else if (instances.size() > 14) {
						RunenFertigkeitType = c;
					}
				}
			}

			// Talent-Unterklassen - Eigene Erweiterungen Möglich
			for (PojoClass pojoClass : classes) {
				Class c = pojoClass.getClazz();
				if (EigeneErweiterungenMoeglich.class.isAssignableFrom(c)) {
					if (SprachTalentType.isAssignableFrom(c)) {
						EigenesSprachTalentType = c;
					} else if (KampfTalentType.isAssignableFrom(c)) {
						EigenesKampfTalentType = c;
					} else if (c.getSuperclass().equals(TalentType)) {
						EigenesTalentType = c;
					}
				}
			}

			// Alle belegt?
			if (SprachTalentType == null) throw new RuntimeException("SprachTalentType not found");
			if (SprachFamilieType == null) throw new RuntimeException("SprachFamilieType not found");
			if (KampfTalentType == null) throw new RuntimeException("KampfTalentType not found");
			if (RunenFertigkeitType == null) throw new RuntimeException("RunenFertigkeitType not found");
			if (EigenesTalentType == null) throw new RuntimeException("EigenesTalentType not found");
			if (EigenesSprachTalentType == null) throw new RuntimeException("EigenesSprachTalentType not found");
			if (EigenesKampfTalentType == null) throw new RuntimeException("EigenesKampfTalentType not found");

			for (Constructor c : EigenesTalentType.getConstructors()) {
				if (c.getParameterTypes().length == 9) newEigenesTalent = c;
			}

			// TalentFactory
			// final class with many private fields (ArrayList, HashMap), but no public fields
			for (PojoClass pojoClass : PojoClassFactory.getPojoClasses(TalentType.getPackage().getName())) {
				Class c = pojoClass.getClazz();
				if (Modifier.isFinal(c.getModifiers()) && c.getFields().length <= 2 && c.getDeclaredFields().length >= 7) {
					TalentFactoryType = c;
					break;
				}
			}
			for (Method m : TalentFactoryType.getMethods()) {
				if (Modifier.isStatic(m.getModifiers()) && m.getReturnType().equals(TalentFactoryType)) {
					talentFactoryInst = m.invoke(null);
				}
				if (m.getReturnType().equals(TalentType) && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(String.class)) {
					talentFactoryAccessors.add(m);
				}
			}
			// EntryCreator gets initialized before any calls to TalentFactory can happen
			// That means we exploit that the target map is null after initialization
			for (Field f : TalentFactoryType.getDeclaredFields()) {
				if (f.getType().equals(HashMap.class)) {
					f.setAccessible(true);
					if (f.get(talentFactoryInst) == null) {
						talentFactoryMapField = f;
						break;
					}
				}
			}
			if (talentFactoryInst == null) throw new RuntimeException("talentFactoryInst not found");
			if (talentFactoryMapField == null) throw new RuntimeException("talentFactoryMapField not found");

			// Sonderfertigkeiten - Registry
			// The registry class is fully static/final and has two private fields - a hashmap and a list of sonderfertigkeit.
			for (PojoClass pojoClass : PojoClassFactory.getPojoClasses(sonderfertigkeitType.getPackage().getName())) {
				Class c = pojoClass.getClazz();
				if (Modifier.isFinal(c.getModifiers()) && c.getDeclaredFields().length == 2
						&& c.getDeclaredConstructors().length == 1 && Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers())) {
					sonderfertigkeitRegistryType = c;
					break;
				}
			}
			assert sonderfertigkeitRegistryType != null;
			for (Field f: sonderfertigkeitRegistryType.getDeclaredFields()) {
				if (f.getType().getPackage().equals(sonderfertigkeitType.getPackage())) {
					sonderfertigkeitListType = f.getType();
					break;
				}
			}
			assert sonderfertigkeitListType != null;
			// for reasons I don't know, the add method has a type parameter. add should be o00000
			for (Method m : sonderfertigkeitListType.getDeclaredMethods()) {
				if (m.getReturnType().equals(void.class)
						&& m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(sonderfertigkeitType)
						&& m.getTypeParameters().length > 0) {
					sonderfertigkeitListAdd = m;
				} else if (m.getReturnType().equals(sonderfertigkeitType)
						&& m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(String.class)) {
					sonderfertigkeitListGet = m;
				}
			}
			assert sonderfertigkeitListAdd != null;
			assert sonderfertigkeitListGet != null;
			// Get the central SF list from the SF registry
			sonderfertigkeitRegistryGetList = getMethodByReturnType(sonderfertigkeitRegistryType, sonderfertigkeitListType);
			assert sonderfertigkeitRegistryGetList != null;

			// Liturgie is a special subclass
			sonderfertigkeitLiturgieType = getMethodByParameterTypes(sonderfertigkeitType, String.class, int.class, boolean.class).getReturnType();
			assert sonderfertigkeitLiturgieType != null;
			newLiturgieSonderfertigkeit = sonderfertigkeitLiturgieType.getDeclaredConstructors()[0];
			newLiturgieSonderfertigkeit.setAccessible(true);
			assert newLiturgieSonderfertigkeit != null;

			// SF with pre-selected parameters
			for (Method m: Settings.class.getDeclaredMethods()) {
				if (m.getName().equals("containsAuswahl") && sonderfertigkeitType.isAssignableFrom(m.getParameterTypes()[0])) {
					sonderfertigkeitWithParamsType = m.getParameterTypes()[0];
					break;
				}
			}
			assert sonderfertigkeitWithParamsType != null;
			for (Constructor<?> c: sonderfertigkeitWithParamsType.getDeclaredConstructors()) {
				if (c.getParameterTypes()[1].equals(Set.class)) {
					newSonderfertigkeitWithParams = c;
					break;
				}
			}
			assert newSonderfertigkeitWithParams != null;

			// Bedingung
			bedingungHatSonderfertigkeit = getStaticMethodByNameAndParameterType(Bedingung.class, "hat", sonderfertigkeitNameType);
			bedingungHatAbstrakteEigenschaft = getStaticMethodByNameAndParameterType(Bedingung.class, "hat", eigenschaftType.getSuperclass(), Integer.class);
			bedingungSetNegieren = getMethodByName(Bedingung.class, "setNegieren");
			bedingungSetNegieren.setAccessible(true);
			sonderfertigkeitSetBedingung = getVoidMethodByParameterType(sonderfertigkeitType, BedingungsVerknuepfung.class);
			sonderfertigkeitGetBedingung = getMethodByReturnType(sonderfertigkeitType, BedingungsVerknuepfung.class);
			if (bedingungHatSonderfertigkeit == null) throw new RuntimeException("bedingungHatSonderfertigkeit is null");
			if (bedingungHatAbstrakteEigenschaft == null) throw new RuntimeException("bedingungHatAbstrakteEigenschaft is null");
			if (bedingungSetNegieren == null) throw new RuntimeException("bedingungSetNegieren is null");
			if (sonderfertigkeitSetBedingung == null) throw new RuntimeException("sonderfertigkeitSetBedingung is null");
			if (sonderfertigkeitGetBedingung == null) throw new RuntimeException("sonderfertigkeitGetBedingung is null");

			createStringMap((Map) alleMagielevel, Bedingung.MagieLevel.class);
			assert !alleMagielevel.isEmpty();

            // Held
            HeldInterfaceType = DatenPluginHeldenWerkzeugImpl.class.getConstructors()[0].getParameterTypes()[0];
            HeldType = HeldenContainerImpl.class.getMethod("getHeld").getReturnType();
            MainWindowType = XMLParserKonverter.class.getConstructors()[0].getParameterTypes()[0];
            getMainWindowInstance = getMethodByReturnType(MainWindowType, MainWindowType);
            getCurrentHeld = getMethodByReturnType(MainWindowType, HeldInterfaceType);
            for (Method m : HeldType.getMethods()) {
                Class[] params = m.getParameterTypes();
                if (params.length == 2 && m.getReturnType().equals(Void.TYPE) && params[0].equals(TalentType.getSuperclass()) && params[1].equals(Integer.TYPE)) {
                    heldAddTalent = m;
                }
            }
            assert getMainWindowInstance != null;
            assert getCurrentHeld != null;
            assert heldAddTalent != null;

		} catch (Exception e) {
			ErrorHandler.handleException(e);
		}
	}

	private void debug() {
		try {
			for (Field f : this.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				if (f.getType() == Class.class) {
					Object o = f.get(this);
					System.out.println("Class " + f.getName() + " = " + (o != null ? ((Class<?>) o).getName() : "<null>"));
				} else if (f.getType() == Method.class) {
					Object o = f.get(this);
					System.out.println("Method " + f.getName() + " = " + (o != null ? ((Method) o).getDeclaringClass().getName() + " : " + ((Method) o).getName() : "<null>"));
				} else if (f.getType() == Field.class) {
					Object o = f.get(this);
					System.out.println("Method " + f.getName() + " = " + (o != null ? ((Field) o).getDeclaringClass().getName() + " : " + ((Field) o).getName() : "<null>"));
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Takes all static instances of this class, runs all "String _()" methods on them, and fills a dictionary
	 *
	 * @param map
	 * @param type
	 */
	protected void createStringMap(Map<String, Object> map, Class type) throws Exception {
		createStringMap(map, type, type);
	}

	/**
	 * Takes all static instances of this class, runs all "String _()" methods of stringBasisType on them, and fills a dictionary
	 *
	 * @param map
	 * @param type
	 * @param stringBasisType
	 */
	protected void createStringMap(Map<String, Object> map, Class type, Class stringBasisType) throws Exception {
		ArrayList<Method> stringMethods = new ArrayList<>();
		boolean foundToString = false;
		for (Method m : stringBasisType.getDeclaredMethods()) {
			if (m.getReturnType().equals(String.class) && m.getParameterTypes().length == 0) {
				stringMethods.add(m);
				if (m.getName().equals("toString")) foundToString = true;
			}
		}
		if (!foundToString) {
			stringMethods.add(type.getMethod("toString"));
		}

		for (Field f : type.getDeclaredFields()) {
			if (f.getType().equals(type)) {
				Object instance = f.get(null);
				for (Method m : stringMethods) {
					try {
						String s = (String) m.invoke(instance);
						if (!s.isEmpty()) map.put(s, instance);
					} catch (InvocationTargetException e) {
						if (e.getCause() instanceof IllegalArgumentException) {
						} else {
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

	protected List<String> getAllStringConstants(Class type) throws IllegalAccessException {
		ArrayList<String> result = new ArrayList<>();
		for (Field f : type.getDeclaredFields()) {
			if (f.getType().equals(String.class) && Modifier.isStatic(f.getModifiers())) {
				String instance = (String) f.get(null);
				result.add(instance);
			}
		}
		return result;
	}

	protected Method getStaticMapMethod(Class type) {
		for (Method m : type.getDeclaredMethods()) {
			if (Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0 && Map.class.isAssignableFrom(m.getReturnType())) {
				return m;
			}
		}
		return null;
	}

	protected Method getMethodByReturnType(Class type, Class returntype) {
		for (Method m : type.getDeclaredMethods()) {
			if (m.getParameterTypes().length == 0 && m.getReturnType().equals(returntype))
				return m;
		}
		return null;
	}

	protected Method getMethodByName(Class type, String name) {
		for (Method m : type.getDeclaredMethods()) {
			if (m.getName().equals(name))
				return m;
		}
		return null;
	}

	protected Method getMethodByParameterTypes(Class type, Class... params) {
		for (Method m : type.getDeclaredMethods()) {
			if (Arrays.equals(m.getParameterTypes(), params))
				return m;
		}
		return null;
	}

	protected Method getVoidMethodByParameterType(Class type, Class param1) {
		for (Method m : type.getDeclaredMethods()) {
			if (m.getReturnType().equals(void.class) && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(param1))
				return m;
		}
		return null;
	}

	protected Method getStaticMethodByNameAndParameterType(Class type, String name, Class... params) {
		outer:
		for (Method m : type.getDeclaredMethods()) {
			if (Modifier.isStatic(m.getModifiers()) && m.getName().equals(name) && m.getParameterTypes().length == params.length) {
				for (int i = 0; i < params.length; i++) {
					if (!m.getParameterTypes()[i].equals(params[i])) {
						continue outer;
					}
				}
				return m;
			}
		}
		return null;
	}

	/**
	 * Converts an array of Strings to an array of the corresponding "Merkmal" instances
	 *
	 * @param merkmale
	 * @return
	 */
	private Object getMerkmale(List<String> merkmale) {
		Object result = Array.newInstance(merkmalType, merkmale.size());
		for (int i = 0; i < merkmale.size(); i++) {
			Object mkml = alleMerkmale.get(merkmale.get(i));
			if (mkml == null) throw new IllegalArgumentException("Unbekanntes Merkmal: " + merkmale.get(i));
			Array.set(result, i, mkml);
		}
		return result;
	}


	/**
	 * Erstellt und registriert einen neuen Zauber
	 *
	 * @param name
	 * @param kategorie Steigerungsspalte ("A" - "H")
	 * @param merkmale  Als String-Array: ["Anti", "Einfluss", "Dämonisch (Blakharaz)"]
	 * @param probe
	 * @param q         Quellenangabe ("LCD: 123"), null ist erlaubt
	 * @param mod       Modifikationen der Probe ("", "+MR", "+Mod", ...), null ist erlaubt
	 * @return
	 */
	public ZauberWrapper createSpell(String name, String kategorie, List<String> merkmale, Probe probe, Quellenangabe q, String mod) {
		try {
			if (q == null)
				q = Quellenangabe.leereQuelle;
			if (mod == null)
				mod = "";
			Object kat = alleKategorien.get(kategorie);
			if (isSpellKnown(name))
				throw new IllegalArgumentException("Zauber \"" + name + "\" ist bereits bekannt!");
			Zauber newspell = newZauber.newInstance(name, kat, probe.getProbe(), getMerkmale(merkmale), q.getQuellenObj(), mod);
			return new ZauberWrapper(name, newspell);
		} catch (Exception e) {
			throw ErrorHandler.handleException(e);
		}
	}

	/**
	 * @param name
	 * @return <code>true</code> falls ein Zauber mit dem Namen <code>name</code> bekannt ist
	 * @see helden.framework.zauber.ZauberFabrik
	 */
	private boolean isSpellKnown(String name) {
		try {
			return ZauberFabrik.getInstance().getZauberfertigkeit(name) != null;
		} catch (RuntimeException e) {
			return false;
		}
	}

	private boolean isSonderfertigkeitKnown(String name) {
		for (Setting setting : Setting.getHauptSettings()) {
			if (setting.getIncluded().contains("S" + name))
				return true;
		}
		return false;
	}

	public Object createSonderfertigkeit(String name, int kosten, int category, BedingungsVerknuepfung bedingung) {
		/*
		0: Allgemein
		1: Geländekunde
		2: Kampf: Nahkampf
		3: Kampf: Fernkampf
		4: Magisch
		5: Magisch: Repräsentation
		6: Magisch: Merkmalskenntnis
		7: Magisch: Objektritual
		8: Elfenlied
		9: Kampf: Manöver
		10: Geweiht: Liturgie
		11: Geweiht
		12: Magisch: Schamanenritual
		13: Magisch: Magische Lieder
		 */
		try {
			if (isSonderfertigkeitKnown(name)) {
				throw new IllegalArgumentException("Sonderfertigkeit \"" + name + "\" ist bereits bekannt!");
			}
			Object sf = newSonderfertigkeit.newInstance(name, kosten, category);
			if (bedingung != null)
				sonderfertigkeitSetBedingung.invoke(sf, bedingung);
			return registerSonderfertigkeit(name, sf);
		} catch (Exception e) {
			ErrorHandler.handleException(e);
			return null;
		}
	}

	public Object createSonderfertigkeitWithParams(String name, int kosten, int category, BedingungsVerknuepfung bedingung, Collection<SonderfertigkeitConfig.SFVariante> variants) {
		try {
			if (isSonderfertigkeitKnown(name)) {
				throw new IllegalArgumentException("Sonderfertigkeit \"" + name + "\" ist bereits bekannt!");
			}

			HashMap<Object, Integer> kostenMap = new HashMap<>();
			HashSet<String> params = new HashSet<>();
			for (SonderfertigkeitConfig.SFVariante v: variants) {
				params.add(v.name);
                kostenMap.put(v.name, v.kosten == null ? kosten : v.kosten);
			}
			Object sf = newSonderfertigkeitWithParams.newInstance(name, params, kostenMap, category);
			if (bedingung != null)
				sonderfertigkeitSetBedingung.invoke(sf, bedingung);
			return registerSonderfertigkeit(name, sf);
		} catch (Exception e) {
			ErrorHandler.handleException(e);
			return null;
		}
	}

	private Object registerSonderfertigkeit(String name, Object sf) throws ReflectiveOperationException {
		Object otherList = sonderfertigkeitRegistryGetList.invoke(null);
		sonderfertigkeitListAdd.invoke(otherList, sf);
		Object sfname = newSonderfertigkeitName.newInstance(name);

		// Some SF activate a special talent when skilled:
		if (name.startsWith("Ritualkenntnis: ")) {
			XmlEntryCreator xmlec = XmlEntryCreator.getInstance();
			Element xmlnode = xmlec.createBasicTalentNode(name, "Ritualkenntnis",
					"RK " + name.substring(16), "E",
					//"--", "--", "--",
					"MU", "KL", "IN",  // parser does not support "none"
					"", "CustomEntryLoader Plugin", "");
			Object talent = xmlec.talentNodeToObject(xmlnode);
			talentSetProbe.invoke(talent, new Probe("--/--/--").getProbe());
			sonderfertigkeitSetCorrespondingTalent.invoke(sf, talent);
		} else if (name.startsWith("Liturgiekenntnis")) {
			XmlEntryCreator xmlec = XmlEntryCreator.getInstance();
			Element xmlnode = xmlec.createBasicTalentNode(name, "Liturgiekenntnis",
					"LK " + name.substring(17), "F",
					"MU", "IN", "CH",
					"", "CustomEntryLoader Plugin", "");
			Object talent = xmlec.talentNodeToObject(xmlnode);
			sonderfertigkeitSetCorrespondingTalent.invoke(sf, talent);
		}

		for (Setting setting : Setting.getHauptSettings()) {
			setting.getIncluded().add("S" + name);
			setting.getIncluded().add("S" + name + "#*");
		}
		return sfname;
	}

	public Object createMerkmal(String name, String shortname, String abkuerzung, int stufe) {
        try {
			if (alleMerkmale.containsKey(name)) {
				throw new IllegalArgumentException("Merkmal \"" + name + "\" ist bereits bekannt!");
			}
			if (alleMerkmale.containsKey(shortname)) {
				throw new IllegalArgumentException("Merkmal \"" + shortname + "\" ist bereits bekannt!");
			}

			Object merkmal = merkmalConstructor.newInstance(abkuerzung, name, shortname, stufe, merkmalKinds.get("MERKMAL"));
			alleMerkmale.put(name, merkmal);
			alleMerkmale.put(shortname, merkmal);

			String sfname = "Merkmalskenntnis: " + merkmal;
			Object sf = newMerkmalskenntnis.invoke(null, sfname, merkmal);
			registerSonderfertigkeit(sfname, sf);

			return merkmal;
		} catch (ReflectiveOperationException e) {
			ErrorHandler.handleException(e);
			return null;
        }
    }

	public RepresentationWrapper createRepresentation(String name, String shortname, boolean hasRitualkenntnis) {
		try {
			Object sf1 = createSonderfertigkeit("Repräsentation: " + name, 2000, 5, null);
			Object sf2 = hasRitualkenntnis ? createSonderfertigkeit("Ritualkenntnis: " + name, 250, 4, null) : null;
			Object repr = newRepresentation.newInstance("z" + (numRepresentations++), name, shortname, sf1, sf2);
			alleRepresentationen.put(name, repr);
			alleRepresentationen.put(shortname, repr);

			for (Setting setting : Setting.getHauptSettings()) {
				setting.getIncluded().add("R" + name);
			}

			return new RepresentationWrapper(name, repr);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			ErrorHandler.handleException(e);
			return null;
		}
	}

	public Object createLiturgieSonderfertigkeit(String name, int grad, List<String> liturgiekenntnis, BedingungsVerknuepfung bedingung) {
		try {
			Object sf = newLiturgieSonderfertigkeit.newInstance(name, grad, false);
			if (bedingung == null) {
				bedingung = Bedingung.AND(
						Bedingung.istMindestens(alleMagielevel.get("Geweihter")),
						Bedingung.hatLkW(grad * 3)
				);
				if (!liturgiekenntnis.isEmpty()) {
					BedingungsVerknuepfung liturgieBedingung = Bedingung.OR();
					for (String s : liturgiekenntnis) {
						liturgieBedingung.addBedingung(createBedingungSF(s, false));
					}
					bedingung.addBedingung(liturgieBedingung);
				}
			}
			if (bedingung != null)
				sonderfertigkeitSetBedingung.invoke(sf, bedingung);
			Object otherList = sonderfertigkeitRegistryGetList.invoke(null);
			sonderfertigkeitListAdd.invoke(otherList, sf);
			Object sfname = newSonderfertigkeitName.newInstance(name);

			for (Setting setting : Setting.getHauptSettings()) {
				setting.getIncluded().add("S" + name);
			}
			return sfname;
		} catch (Exception e) {
			ErrorHandler.handleException(e);
			return null;
		}
	}


	public void addTalentToHeld(Object held, Object talent, int value) {
		try {
			heldAddTalent.invoke(held, talent, value);
		} catch (Exception e) {
			ErrorHandler.handleException(e);
		}
	}

	/**
	 * Der Editor braucht Talente in einer speziellen Map, deren Key der Name ist...
	 *
	 * @param talent
	 * @throws IllegalAccessException
	 */
	public void registerTalentForEditor(Object talent) throws IllegalAccessException {
		initTalentFactoryMap();
		talentFactoryMap.put(talent.toString(), talent);
	}

	/**
	 * Ensure TalentFactoryMap is initialized properly
	 */
	public void initTalentFactoryMap() {
		if (talentFactoryMap == null) {
			try {
				talentFactoryMap = (HashMap<String, Object>) talentFactoryMapField.get(talentFactoryInst);
				if (talentFactoryMap == null) {
					// Force initialization of this map
					for (Method m : talentFactoryAccessors) {
						try {
							m.invoke(talentFactoryInst, "doesnotexist");
						} catch (Exception ignored) {
						}
					}
					// and retry
					talentFactoryMap = (HashMap<String, Object>) talentFactoryMapField.get(talentFactoryInst);
					if (talentFactoryMap == null)
						throw new RuntimeException("Map is null even after forced initialization!");
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public AbstraktBedingung createBedingungSF(String name, boolean allowNonExistentName) {
		try {
			if (!allowNonExistentName) {
				Object otherList = sonderfertigkeitRegistryGetList.invoke(null);
				try {
					sonderfertigkeitListGet.invoke(otherList, name);
				} catch (InvocationTargetException e) {
					ErrorHandler.handleException(e.getCause());
				}
			}

			return (AbstraktBedingung) bedingungHatSonderfertigkeit.invoke(null, newSonderfertigkeitName.newInstance(name));
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	public AbstraktBedingung createBedingungAbstrakteEigenschaft(Object eigenschaft, int value) {
		try {
			return (AbstraktBedingung) bedingungHatAbstrakteEigenschaft.invoke(null, eigenschaft, Integer.valueOf(value));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void addLiturgiekenntnisToLiturgien(Object sfname, List<String> liturgien) {
		try {
			Object otherList = sonderfertigkeitRegistryGetList.invoke(null);
			for (String name: liturgien) {
				Object sf = sonderfertigkeitListGet.invoke(otherList, name);
				Object bedingungObj = sonderfertigkeitGetBedingung.invoke(sf);
				if (bedingungObj != null) {
					BedingungsVerknuepfung v = (BedingungsVerknuepfung) bedingungObj;
					if (v.getBedingungen().size() >= 3 && v.getBedingungen().get(2) instanceof BedingungsVerknuepfung) {
						BedingungsVerknuepfung v2 = (BedingungsVerknuepfung) v.getBedingungen().get(2);
						if (v2.getVerknuepfungsArt().toString().equals("OR")) {
							v2.getBedingungen().add(createBedingungSF(sfname.toString(), false));
						}
					}
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			ErrorHandler.handleException(e);
		}
	}


	/**
	 * Eine Probe aus drei Eigenschaften. Bsp: "(MU/KL/KO)"
	 */
	public static class Probe {

		public final String p1, p2, p3;

		public Probe(String p1, String p2, String p3) {
			if (!getInstance().alleEigenschaften.containsKey(p1))
				throw new Loader.ConfigError("Ungültige Eigenschaft: " + p1);
			if (!instance.alleEigenschaften.containsKey(p2))
				throw new Loader.ConfigError("Ungültige Eigenschaft: " + p2);
			if (!instance.alleEigenschaften.containsKey(p3))
				throw new Loader.ConfigError("Ungültige Eigenschaft: " + p3);
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
		}

		public Probe(String probe) {
			if (probe.startsWith("(") && probe.endsWith(")"))
				probe = probe.substring(1, probe.length() - 1);
			String[] p = probe.split("/");
			if (p.length != 3)
				throw new Loader.ConfigError("Ungültige Probe: "+probe);
			for (String s : p) {
				if (!getInstance().alleEigenschaften.containsKey(s))
					throw new Loader.ConfigError("Ungültige Eigenschaft: " + p);
			}
			this.p1 = p[0];
			this.p2 = p[1];
			this.p3 = p[2];
		}

		public Object getProbe() throws ReflectiveOperationException {
			Object o1 = instance.alleEigenschaften.get(p1);
			if (o1 == null) throw new IllegalArgumentException("Unbekannte Eigenschaft: " + p1);
			Object o2 = instance.alleEigenschaften.get(p2);
			if (o2 == null) throw new IllegalArgumentException("Unbekannte Eigenschaft: " + p2);
			Object o3 = instance.alleEigenschaften.get(p3);
			if (o3 == null) throw new IllegalArgumentException("Unbekannte Eigenschaft: " + p3);
			return instance.talentprobeConstructor.newInstance(o1, o2, o3);
		}

	}


	/**
	 * Eine Quellenangabe, bestehend aus Buchkürzel und Seitenzahl ("LCD:123")
	 */
	public static class Quellenangabe {
		public final String book;
		public final int page;

		public static final Quellenangabe leereQuelle = new Quellenangabe("", 0);

		public Quellenangabe(String book, int page) {
			this.book = book;
			this.page = page;
		}

		public Quellenangabe(String quelle) {
			int p = quelle.lastIndexOf(':');
			if (p > 0) {
				this.book = quelle.substring(0, p);
				this.page = Integer.parseInt(quelle.substring(p + 1));
			} else {
				this.book = quelle;
				this.page = 0;
			}
		}

		public Object getQuellenObj() throws Exception {
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
		 *
		 * @param repr "Mag", "Hexe", "Dru(Elf)"
		 * @param num
		 */
		public void addVerbreitung(String repr, int num) {
			Matcher m = reprPattern.matcher(repr);
			if (m.matches()) {
				addVerbreitung(m.group(1), m.group(2), num);
			} else {
				addVerbreitung(repr, repr, num);
			}
		}

		private static final Pattern reprPattern = Pattern.compile("^(\\w{3})\\s?\\((\\w{3})\\)$");

		/**
		 * (Dru, Elf, 3) => Dru(Elf) 3
		 *
		 * @param repr
		 * @param bekanntIn
		 * @param num
		 * @see helden.framework.zauber.ZauberVerbreitung
		 */
		public void addVerbreitung(String repr, String bekanntIn, int num) {
			Object reprObj = instance.alleRepresentationen.get(repr);
			if (reprObj == null) throw new IllegalArgumentException("Unbekannte Repräsentation: " + repr);
			Object bekanntObj = instance.alleRepresentationen.get(bekanntIn);
			if (bekanntObj == null) throw new IllegalArgumentException("Unbekannte Repräsentation: " + bekanntIn);
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
		 *
		 * @param settingName
		 * @see helden.framework.settings.Setting
		 */
		public void addToSetting(String settingName) {
			if (settingName.equals("all") || settingName.equals("Alle")) {
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


	public static class RepresentationWrapper {
		public final String name;
		public final Object repr;

		public RepresentationWrapper(String name, Object repr) {
			this.name = name;
			this.repr = repr;
		}

		public void addZauber(String name, int verbreitung) {
			try {
				Zauber zauber = ZauberFabrik.getInstance().getZauberfertigkeit(name);
				zauber.getVerbreitung().add(instance.newZauberVerbreitung.newInstance(repr, repr, verbreitung));
			} catch (RuntimeException e) {
				ErrorHandler.handleException(new RuntimeException("Unbekannter Zauber: " + name, e));
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
				ErrorHandler.handleException(e);
			}
		}
	}

}
