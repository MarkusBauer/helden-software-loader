package de.mb.heldensoftware.customentries;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
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

	public NewTalentDialog() {
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

	private void initComponents() {
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
		for (String s : new String[]{"MU", "KL", "IN", "CH", "FF", "GE", "KO", "KK", "**"}) {
			Object o = ec.alleEigenschaften.get(s);
			comboProbe1.addItem(o);
			comboProbe2.addItem(o);
			comboProbe3.addItem(o);
		}
		// Kategorien A-H
		for (Object o : ec.alleKategorien.values()) {
			comboKategorie.addItem(o);
		}
		// Arten
		for (Object o : ec.getAllStaticInstances(ec.TalentArtType)) {
			boolean b = ((Boolean) ec.talentArtIsPrimitive.invoke(o)).booleanValue();
			if ((b && !o.toString().equals("Kampf")) || o.toString().equals("Gaben"))
				comboArt.addItem(o); // "Kampf" is not importable, "Nahkampf"/"Fernkampf" is broken
		}
		// Sprachfamilien
		for (String s : ec.getAllStringConstants(ec.SprachFamilieType)) {
			comboSprachFamilie.addItem(s);
		}
	}

	private void onTalentArtChanged() {
		// Get information about talent
		String art = comboArt.getSelectedItem().toString();
		boolean isKampf = art.equals("Kampf") || art.equals("Nahkampf") || art.equals("Fernkampf");
		boolean isKoerper = art.equals("Körperlich");
		boolean isSprache = art.equals("Sprachen");
		boolean isSpracheSchrift = isSprache || art.equals("Schriften");
		boolean isRK = art.equals("Ritualkenntnis");
		boolean isF = art.equals("Liturgiekenntnis") || art.equals("Gaben");

		// Probe
		comboProbe1.setEnabled(!isKampf && !isSpracheSchrift);
		comboProbe2.setEnabled(!isKampf && !isSpracheSchrift);
		comboProbe3.setEnabled(!isKampf && !isSpracheSchrift);
		lblProbe.setEnabled(!isKampf && !isSpracheSchrift);
		if (isKampf) {
			comboProbe1.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("GE"));
			comboProbe2.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get(art.equals("Fernkampf") ? "FF" : "GE"));
			comboProbe3.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("KK"));
		} else if (isSprache) {
			comboProbe1.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("KL"));
			comboProbe2.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("IN"));
			comboProbe3.setSelectedItem(EntryCreator.getInstance().alleEigenschaften.get("CH"));
		} else if (isSpracheSchrift) {
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
		if (!isKampf && !isSpracheSchrift) {
			comboKategorie.setSelectedItem(EntryCreator.getInstance().alleKategorien.get(isKoerper ? "D" : isRK ? "E" : isF ? "F" : "B"));
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
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Sicher?
		if (JOptionPane.showConfirmDialog(this, "Möchten sie dieses Talent wirklich hinzufügen? Diese Aktion kann nicht rückgängig gemacht werden.", "Talent hinzufügen", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		try {
			Object talent = createTalentInstance();
			System.out.println(talent);
			if (newTalentCallback != null)
				newTalentCallback.talentCreated(talent, (Integer) spinnerStartwert.getValue());
		} catch (Exception e) {
			ErrorHandler.handleException(e);
		}


		dispose();
	}

	private void sanitizeInput() {
		if (textTalentName.getText().isEmpty()) throw new IllegalArgumentException("Bitte einen Talentnamen eingeben!");
		if (textTalentAbkuerzung.getText().isEmpty()) {
			String name = textTalentName.getText();
			if (name.startsWith("Sprachen kennen")) name = name.substring(16);
			if (name.startsWith("Lesen/Schreiben")) name = name.substring(16);
			textTalentAbkuerzung.setText(name);
		}
		if (comboArt.getSelectedItem().toString().equals("Sprachen")) {
			if (!textTalentName.getText().startsWith("Sprachen kennen"))
				textTalentName.setText("Sprachen kennen " + textTalentName.getText());
		}
		if (comboArt.getSelectedItem().toString().equals("Schriften")) {
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

		if (isKampf) {
			xmlec.setTalentBehinderung(xmlnode, (Integer) spinnerBehinderung.getValue());
			xmlec.setTalentParade(xmlnode, checkParadeMoeglich.isSelected());
		} else if (isSpracheSchrift) {
			xmlec.setTalentKomplexitaet(xmlnode, (Integer) spinnerSprachKomplex.getValue());
			if (isSprache) {
				xmlec.setTalentSprachfamilie(xmlnode, comboSprachFamilie.getSelectedItem().toString());
			}
		} else if (isKoerper) {
			xmlec.setTalentBehinderung(xmlnode, textBehinderung.getText());
		}
		return xmlec.talentNodeToObject(xmlnode);
	}


	private void onCancel() {
		// add your code here if necessary
		dispose();
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		contentPane = new JPanel();
		contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		buttonOK = new JButton();
		buttonOK.setText("OK");
		panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		paneFields = new JPanel();
		paneFields.setLayout(new GridLayoutManager(13, 4, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(paneFields, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		lblTalentname = new JLabel();
		lblTalentname.setText("Talentname");
		paneFields.add(lblTalentname, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		paneFields.add(spacer2, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		textTalentName = new JTextField();
		paneFields.add(textTalentName, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		lblTalentAbkuerzung = new JLabel();
		lblTalentAbkuerzung.setText("(Abkürzung)");
		paneFields.add(lblTalentAbkuerzung, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		textTalentAbkuerzung = new JTextField();
		paneFields.add(textTalentAbkuerzung, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		lblProbe = new JLabel();
		lblProbe.setText("Probe");
		paneFields.add(lblProbe, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		comboProbe1 = new JComboBox();
		paneFields.add(comboProbe1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		comboProbe2 = new JComboBox();
		paneFields.add(comboProbe2, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		comboProbe3 = new JComboBox();
		paneFields.add(comboProbe3, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		lblKategorie = new JLabel();
		lblKategorie.setText("Kategorie");
		paneFields.add(lblKategorie, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		comboKategorie = new JComboBox();
		paneFields.add(comboKategorie, new GridConstraints(6, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Talentart");
		paneFields.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		comboArt = new JComboBox();
		comboArt.setActionCommand("TalentArtChanged");
		paneFields.add(comboArt, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		lblSprachFamilie = new JLabel();
		lblSprachFamilie.setText("Sprachfamilie");
		paneFields.add(lblSprachFamilie, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		comboSprachFamilie = new JComboBox();
		paneFields.add(comboSprachFamilie, new GridConstraints(8, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		lblSprachKomplex = new JLabel();
		lblSprachKomplex.setText("Sprach-Komplexität");
		paneFields.add(lblSprachKomplex, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		spinnerSprachKomplex = new JSpinner();
		paneFields.add(spinnerSprachKomplex, new GridConstraints(9, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		lblBehinderungStr = new JLabel();
		lblBehinderungStr.setText("Behinderung");
		lblBehinderungStr.setToolTipText("Beispielsweise \"BE\", \"BEx2\", \"BE-2\", ...");
		paneFields.add(lblBehinderungStr, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		textBehinderung = new JTextField();
		textBehinderung.setToolTipText("Beispielsweise \"BE\", \"BEx2\", \"BE-2\", ...");
		paneFields.add(textBehinderung, new GridConstraints(7, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		spinnerBehinderung = new JSpinner();
		paneFields.add(spinnerBehinderung, new GridConstraints(10, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		lblBehinderungInt = new JLabel();
		lblBehinderungInt.setText("Behinderung");
		paneFields.add(lblBehinderungInt, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		checkParadeMoeglich = new JCheckBox();
		checkParadeMoeglich.setText("Parade möglich");
		paneFields.add(checkParadeMoeglich, new GridConstraints(11, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Eigene Talente kosten keine AP, und können nach Aktivierung nicht mehr geändert werden.");
		paneFields.add(label2, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Startwert");
		paneFields.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		spinnerStartwert = new JSpinner();
		paneFields.add(spinnerStartwert, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
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
