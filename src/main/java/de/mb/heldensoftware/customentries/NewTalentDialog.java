package de.mb.heldensoftware.customentries;

import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

public class NewTalentDialog extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField textTalentName;
	private JTextField textTalentAbkuerzung;
	private JComboBox comboProbe1;
	private JComboBox comboProbe2;
	private JComboBox comboProbe3;
	private JComboBox comboKategorie;
	private JComboBox comboArt;
	private JComboBox comboSprachFamilie;
	private JSpinner spinnerSprachKomplex;
	private JTextField textBehinderung;
	private JSpinner spinnerBehinderung;
	private JCheckBox checkParadeMoeglich;
	private JLabel lblProbe;
	private JLabel lblBehinderungStr;
	private JLabel lblBehinderungInt;
	private JLabel lblSprachFamilie;
	private JLabel lblSprachKomplex;
	private JPanel paneFields;
	private JLabel lblTalentname;
	private JSpinner spinnerStartwert;
	private JLabel lblKategorie;
	private JLabel lblTalentAbkuerzung;

	public NewTalentDialog(){
		this(null);
	}

	public NewTalentDialog(JFrame frame) {
		super(frame, "Neues Talent hinzufügen");
		if (frame != null) setLocationRelativeTo(frame);
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		initComponents();

		try {
			initModels();
		} catch (Exception e) {
			ErrorHandler.handleException(e);
		}

		onTalentArtChanged();
	}

	private void initComponents(){
		spinnerSprachKomplex.setModel(new SpinnerNumberModel(18, 1, 99, 1));
		spinnerBehinderung.setModel(new SpinnerNumberModel(2, 0, 99, 1));
		spinnerStartwert.setModel(new SpinnerNumberModel(0, -10, 99, 1));
		comboArt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onTalentArtChanged();
			}
		});

		// Force initial size to fit all components
		int col1Width = lblSprachKomplex.getPreferredSize().width + 5;
		lblTalentname.setPreferredSize(new Dimension(col1Width, lblTalentname.getPreferredSize().height));
		Dimension d = contentPane.getPreferredSize();
		contentPane.setPreferredSize(new Dimension(d.width, d.height));
	}

	private void initModels() throws IllegalAccessException, InvocationTargetException {
		EntryCreator ec = EntryCreator.getInstance();
		// Probe / Eigenschaft
		for (String s: new String[]{"MU", "KL", "IN", "CH", "FF", "GE", "KO", "KK", "**"}){
			Object o = ec.alleEigenschaften.get(s);
			comboProbe1.addItem(o);
			comboProbe2.addItem(o);
			comboProbe3.addItem(o);
		}
		// Kategorien A-H
		for (Object o: ec.alleKategorien.values()){
			comboKategorie.addItem(o);
		}
		// Arten
		for (Object o: ec.getAllStaticInstances(ec.TalentArtType)){
			boolean b = ((Boolean) ec.talentArtIsPrimitive.invoke(o)).booleanValue();
			if (b && !o.toString().equals("Kampf")) comboArt.addItem(o); // "Kampf" is not importable, "Nahkampf"/"Fernkampf" is broken
		}
		// Sprachfamilien
		for (String s: ec.getAllStringConstants(ec.SprachFamilieType)){
			comboSprachFamilie.addItem(s);
		}
	}

	private void onTalentArtChanged(){
		// Get information about talent
		String art = comboArt.getSelectedItem().toString();
		boolean isKampf = art.equals("Kampf") || art.equals("Nahkampf") || art.equals("Fernkampf");
		boolean isKoerper = art.equals("Körperlich");
		boolean isSprache = art.equals("Sprachen");
		boolean isSpracheSchrift = isSprache || art.equals("Schriften");

		// Probe
		comboProbe1.setEnabled(!isKampf && !isSpracheSchrift);
		comboProbe2.setEnabled(!isKampf && !isSpracheSchrift);
		comboProbe3.setEnabled(!isKampf && !isSpracheSchrift);
		lblProbe.setEnabled(!isKampf && !isSpracheSchrift);
		if (isKampf){
			comboProbe1.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("GE"));
			comboProbe2.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get(art.equals("Fernkampf") ? "FF" : "GE"));
			comboProbe3.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("KK"));
		}else if (isSprache){
			comboProbe1.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("KL"));
			comboProbe2.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("IN"));
			comboProbe3.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("CH"));
		}else if (isSpracheSchrift){
			comboProbe1.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("KL"));
			comboProbe2.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("KL"));
			comboProbe3.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("FF"));
		}

		// Abkürzung
		lblTalentAbkuerzung.setVisible(isKampf);
		textTalentAbkuerzung.setVisible(isKampf);

		// Steigerung
		comboKategorie.setEnabled(isKampf || isSpracheSchrift);
		lblKategorie.setEnabled(isKampf || isSpracheSchrift);
		if (!isKampf && !isSpracheSchrift){
			comboKategorie.setSelectedItem(EntryCreator.getInstance().alleKategorien.get(isKoerper ? "D" : "B"));
		}

		// Sprachen
		lblSprachFamilie.setVisible(isSprache);
		comboSprachFamilie.setVisible(isSprache);
		lblSprachKomplex.setVisible(isSpracheSchrift);
		spinnerSprachKomplex.setVisible(isSpracheSchrift);

		// Behinderung
		lblBehinderungStr.setVisible(isKoerper);
		textBehinderung.setVisible(isKoerper);
		lblBehinderungInt.setVisible(isKampf);
		spinnerBehinderung.setVisible(isKampf);

		// Parade
		checkParadeMoeglich.setVisible(isKampf);
	}



	private void onOK() {
		// Cleanup
		try {
			sanitizeInput();
		}catch(IllegalArgumentException e){
			JOptionPane.showMessageDialog(this, e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Sicher?
		if (JOptionPane.showConfirmDialog(this, "Möchten sie dieses Talent wirklich hinzufügen? Diese Aktion kann nicht rückgängig gemacht werden.", "Talent hinzufügen", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		try {
			Object talent = createTalentInstance();
			System.out.println(talent);
			if (newTalentCallback != null) newTalentCallback.talentCreated(talent, (Integer) spinnerStartwert.getValue());
		} catch (Exception e) {
			ErrorHandler.handleException(e);
		}


		dispose();
	}

	private void sanitizeInput(){
		if (textTalentName.getText().isEmpty()) throw new IllegalArgumentException("Bitte einen Talentnamen eingeben!");
		if (textTalentAbkuerzung.getText().isEmpty()){
			String name = textTalentName.getText();
			if (name.startsWith("Sprachen kennen")) name = name.substring(16);
			if (name.startsWith("Lesen/Schreiben")) name = name.substring(16);
			textTalentAbkuerzung.setText(name);
		}
		if (comboArt.getSelectedItem().toString().equals("Sprachen")){
			if (!textTalentName.getText().startsWith("Sprachen kennen"))
				textTalentName.setText("Sprachen kennen " + textTalentName.getText());
		}
		if (comboArt.getSelectedItem().toString().equals("Schriften")){
			if (!textTalentName.getText().startsWith("Lesen/Schreiben"))
				textTalentName.setText("Lesen/Schreiben " + textTalentName.getText());
		}
		textBehinderung.setText(textBehinderung.getText().replace(" ", ""));
	}

	private Object createTalentInstance() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		// Get information about talent
		String art = comboArt.getSelectedItem().toString();
		boolean isKampf = art.equals("Kampf") || art.equals("Nahkampf") || art.equals("Fernkampf");
		boolean isKoerper = art.equals("Körperlich");
		boolean isSprache = art.equals("Sprachen");
		boolean isSpracheSchrift = isSprache || art.equals("Schriften");

		XmlEntryCreator xmlec = XmlEntryCreator.getInstance();
		Element xmlnode = xmlec.createBasicTalentNode(textTalentName.getText(), comboArt.getSelectedItem().toString(),
				textTalentAbkuerzung.getText(), comboKategorie.getSelectedItem().toString(),
				comboProbe1.getSelectedItem(), comboProbe2.getSelectedItem(), comboProbe3.getSelectedItem(),
				"", "CustomEntryLoader Plugin", "");

		if (isKampf){
			xmlec.setTalentBehinderung(xmlnode, (Integer) spinnerBehinderung.getValue());
			xmlec.setTalentParade(xmlnode, checkParadeMoeglich.isSelected());
		}else if (isSpracheSchrift){
			xmlec.setTalentKomplexitaet(xmlnode, (Integer) spinnerSprachKomplex.getValue());
			if (isSprache) {
				xmlec.setTalentSprachfamilie(xmlnode, comboSprachFamilie.getSelectedItem().toString());
			}
		}else if (isKoerper){
			xmlec.setTalentBehinderung(xmlnode, textBehinderung.getText());
		}
		return xmlec.talentNodeToObject(xmlnode);
	}


	private void onCancel() {
		// add your code here if necessary
		dispose();
	}


	public static interface TalentCallback {
		public void talentCreated(Object talent, int value);
	}


	private TalentCallback newTalentCallback = null;

	public TalentCallback getNewTalentCallback() {
		return newTalentCallback;
	}

	public void setNewTalentCallback(TalentCallback newTalentCallback) {
		this.newTalentCallback = newTalentCallback;
	}


	public static void main(String[] args) {
		NewTalentDialog dialog = new NewTalentDialog(null);
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}
}
