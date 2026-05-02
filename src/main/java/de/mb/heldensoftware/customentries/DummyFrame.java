package de.mb.heldensoftware.customentries;

import javax.swing.*;

public class DummyFrame extends JFrame implements AutoCloseable {

    public DummyFrame(String title) {
        super(title);
        setUndecorated(true);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    @Override
    public void close() {
        dispose();
    }
}
