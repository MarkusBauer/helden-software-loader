package de.mb.heldensoftware.customentries;

import helden.framework.held.persistenz.ModsDatenParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by markus on 28.03.17.
 */
public class XmlEntryCreator {
	private static XmlEntryCreator instance = new XmlEntryCreator();

	public static XmlEntryCreator getInstance() {
		return instance;
	}


	private Document xml;
	private Element root;
	private DocumentBuilder builder;

	private XmlEntryCreator() {
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			xml = builder.newDocument();
			root = xml.getDocumentElement();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}


	public Element createBasicTalentNode(String name, String talentart, String shortname, String steigerung, Object probe1, Object probe2, Object probe3, String beschreibung, String urheber, String kontakt) {
		Element talent = xml.createElement("eigenestalent");
		addValue(talent, "talentname", name);
		addValue(talent, "talentart", talentart);
		addValue(talent, "abkuerzung", shortname);
		addValue(talent, "steigerungskategorie", String.valueOf(steigerung.codePointAt(0) - "A".codePointAt(0)));
		addValue(talent, "urheber", urheber);
		addValue(talent, "kontakt", kontakt);
		Element b = xml.createElement("beschreibung");
		b.setTextContent(beschreibung);
		talent.appendChild(b);

		// Probe
		Map<String, Object> eigenschaften = EntryCreator.getInstance().alleEigenschaften;
		if (eigenschaften.containsKey(probe1)) probe1 = eigenschaften.get(probe1);
		if (eigenschaften.containsKey(probe2)) probe2 = eigenschaften.get(probe2);
		if (eigenschaften.containsKey(probe3)) probe3 = eigenschaften.get(probe3);
		addValue(talent, "probe1", probe1.toString());
		addValue(talent, "probe2", probe2.toString());
		addValue(talent, "probe3", probe3.toString());

		return talent;
	}

	public void setTalentBehinderung(Element talent, int behinderung){
		addValue(talent, "behinderungszahl", String.valueOf(behinderung));
	}

	public void setTalentBehinderung(Element talent, String behinderung){
		addValue(talent, "behinderungsstring", behinderung);
	}

	public void setTalentParade(Element talent, boolean parade){
		addValue(talent, "hatparade", parade ? "true" : "false");
	}

	public void setTalentKomplexitaet(Element talent, int komplex){
		addValue(talent, "komplexitaet", String.valueOf(komplex));
	}

	public void setTalentSprachfamilie(Element talent, String familie){
		addValue(talent, "sprachfamilie", familie);
	}

	private void addValue(Node n, String name, String value){
		Element n2 = xml.createElement(name);
		n2.setAttribute("value", value);
		n.appendChild(n2);
	}

	public Object talentNodeToObject(Element talent){
		try {
			Method einlesenTalent = ModsDatenParser.class.getMethod("einlesenTalent", Node.class);
			Object talent1 = einlesenTalent.invoke(ModsDatenParser.getInstance(), talent);
			String id = (String) talent1.getClass().getMethod("getID").invoke(talent1);
			talent.setAttribute("id", id);
			return einlesenTalent.invoke(ModsDatenParser.getInstance(), talent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
