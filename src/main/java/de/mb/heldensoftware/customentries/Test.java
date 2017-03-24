package de.mb.heldensoftware.customentries;

import helden.Helden;
import helden.framework.held.persistenz.BasisXMLParser;
import helden.framework.held.persistenz.ModsDatenParser;
import helden.framework.settings.Setting;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by markus on 24.03.17.
 */
public class Test {

	static Map talentFactoryInternalMapRef;
	static ArrayList talentFactoryInternalListRef;

	public static void main(String[] args) throws Exception {
		Method einlesenTalent = ModsDatenParser.class.getMethod("einlesenTalent", File.class);
		Object talent = einlesenTalent.invoke(ModsDatenParser.getInstance(), new File("/home/markus/HaldanKeres.xml"));
		System.out.println(talent);

		Method getId = talent.getClass().getMethod("getID");
		Object id = getId.invoke(talent);
		System.out.println(id);

		Class talentType = einlesenTalent.getReturnType();
		System.out.println(talentType.getName());

		Class talentFactory = Class.forName("helden.framework.new.while");

		Method talentFactoryAdd = null;
		Object talentFactoryInstance = null;
		for (Method m: talentFactory.getMethods()){
			Class[] parameters = m.getParameterTypes();
			if (parameters.length == 1 && parameters[0].equals(talentType) && m.getReturnType().equals(Void.TYPE)){
				System.out.println(m.getName());
				talentFactoryAdd = m;
			}
			if (parameters.length == 0 && m.getReturnType().equals(talentFactory))
				talentFactoryInstance = m.invoke(null);
		}

		Field talentFactoryInternalListField = talentFactory.getDeclaredField ("Ô00000");
		talentFactoryInternalListField.setAccessible(true);
		talentFactoryInternalListRef = (ArrayList) talentFactoryInternalListField.get(talentFactoryInstance);
		Field talentFactoryInternalMapField = talentFactory.getDeclaredField ("super");
		talentFactoryInternalMapField.setAccessible(true);
		talentFactoryInternalMapRef = (Map) talentFactoryInternalMapField.get(talentFactoryInstance);
		Field talentFactoryInternalMap2Field = talentFactory.getDeclaredField ("class");
		talentFactoryInternalMap2Field.setAccessible(true);
		Map talentFactoryInternalMap2Ref = (Map) talentFactoryInternalMap2Field.get(talentFactoryInstance);

		// add #1
		talentFactoryAdd.invoke(talentFactoryInstance, talent);
		//add(talent);
		System.out.println(talentFactoryInternalMap2Ref.get(id));
		talentFactoryInternalMap2Ref.remove(id);
		System.out.println("Inserted "+"T"+talent.toString()+"...");

		EntryCreator ec = EntryCreator.getInstance();
		Class TalentArtType = talentType.getConstructors()[0].getParameterTypes()[1];
		Map<String, Object> talentArtMap = new HashMap<>();
		ec.createStringMap(talentArtMap, TalentArtType);

		Constructor newTalent = talentType.getConstructor(String.class, TalentArtType, Boolean.TYPE, ec.talentprobeType);
		talent = newTalent.newInstance("TestTalent", talentArtMap.get("Natur"), false, new EntryCreator.Probe("IN/GE/KK").getProbe());

		//talentFactoryAdd.invoke(talentFactoryInstance, talent);
		add(talent);
		System.out.println("Inserted "+"T"+talent.toString()+"...");

		// Recreate custom talent
		Class EigenesSprachTalentType = Class.forName("helden.framework.new.A");
		Constructor newEigenesSprachTalent = EigenesSprachTalentType.getConstructors()[0];
		// null = NODE
		talent = newEigenesSprachTalent.newInstance("Sprachen Kennen XXXXX", talentArtMap.get("Sprachen"), ec.alleKategorien.get("E"), 29, "Garethi", null, "Beschreibung", "Urheber", "Kontakt");
		add(talent);

		Helden.main(args);
	}


	public static void add(Object talent) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Object id = talent.getClass().getMethod("getID").invoke(talent);
		talentFactoryInternalListRef.add(talent);
		talentFactoryInternalMapRef.put(id, talent);
		Setting.getByName("DSA4.1").getIncluded().add("T"+talent.toString());
	}

}
