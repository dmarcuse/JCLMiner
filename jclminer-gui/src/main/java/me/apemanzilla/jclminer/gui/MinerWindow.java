package me.apemanzilla.jclminer.gui;

import javax.swing.*;
import java.awt.*;

public class MinerWindow extends JFrame implements Runnable {
    private static final String WINDOW_TITLE = "JCLMiner - GUI";
    private static final Dimension WINDOW_START_SIZE = new Dimension(350, 500);

    public MinerWindow() {
        super(WINDOW_TITLE);
    }

    private void initUI() {
        // make sure the process ends when closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPreferredSize(WINDOW_START_SIZE);
        setSize(WINDOW_START_SIZE);

        // centres the window
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void run() {
        initUI();
    }
}
