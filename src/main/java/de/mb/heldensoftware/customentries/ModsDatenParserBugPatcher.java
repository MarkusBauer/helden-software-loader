package de.mb.heldensoftware.customentries;

import java.util.HashMap;
import java.util.HashSet;

/**
 * ModsDatenParser.einlesenTalent does not add the new talent to TalentFactory.
 * But the editor expects this.
 * This is more a workaround than a fix (equal-named but inequal Talent break editor, but do not crash anymore)
 */
public class ModsDatenParserBugPatcher {

	public static void patchModsDatenParser(){
		try{
			InstrumentationEngine inst = new InstrumentationEngine("helden.framework.held.persistenz.ModsDatenParser");
			inst.addMethodInstrumentation("einlesenTalent",
					new InstrumentationEngine.MethodResultAModifier(ModsDatenParserBugPatcher.class, "afterEinlesenTalent", "Ljava/lang/Object"));
			inst.patchClass();
		}catch (Exception e){
			ErrorHandler.handleException(e);
		}
	}




	private static HashMap<String, Object> talentCache = new HashMap<>();
	private static HashSet<String> talentNames = new HashSet<>();

	public static Object afterEinlesenTalent(Object talent){
		try {
			String id = (String) talent.getClass().getMethod("getID").invoke(talent);
			// Talent bereits geladen?
			Object oldTalent = talentCache.get(id);
			if (oldTalent != null)
				return oldTalent;
			talentCache.put(id, talent);
			// Talent mit gleichem Namen bereits geladen
			if (talentNames.contains(talent.toString())){
				System.err.println("[WARNUNG] Zwei ungleiche eigene Talente mit gleichem Namen geladen: "+talent.toString());
				System.err.println("[WARNUNG] Eigenes Talent möglicherweise nicht mit dem Editor veränderbar!");
			}
			talentNames.add(talent.toString());
			// Talent registrieren
			System.out.println("Neues Talent geladen: "+talent+" ("+id+")");
			EntryCreator.getInstance().registerTalentForEditor(talent);
			return talent;
		} catch (Exception e) {
			throw ErrorHandler.handleException(e);
		}
	}

}
