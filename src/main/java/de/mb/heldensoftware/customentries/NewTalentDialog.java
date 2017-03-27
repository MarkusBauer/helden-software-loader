package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.EntryCreator;

import javax.swing.*;
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
	private JCheckBox paradeMöglichCheckBox;

	public NewTalentDialog() {
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

		try {
			initModels();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initModels() throws IllegalAccessException, InvocationTargetException {
		EntryCreator ec = EntryCreator.getInstance();
		// Probe / Eigenschaft
		for (Object o: ec.getAllStaticInstances(ec.eigenschaftType)){
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
			if (b) comboArt.addItem(o);
		}
		// Sprachfamilien
		for (String s: ec.getAllStringConstants(ec.SprachFamilieType)){
			comboSprachFamilie.addItem(s);
		}
	}

	private void onOK() {
		// add your code here
		dispose();
	}

	private void onCancel() {
		// add your code here if necessary
		dispose();
	}

	public static void main(String[] args) {
		NewTalentDialog dialog = new NewTalentDialog();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

}
