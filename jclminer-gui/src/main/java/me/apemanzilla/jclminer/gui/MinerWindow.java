package me.apemanzilla.jclminer.gui;

import javax.swing.*;
import java.awt.*;

public class MinerWindow extends JFrame implements Runnable {
    private static final String WINDOW_TITLE = "JCLMiner - GUI";
    private static final Dimension WINDOW_START_SIZE = new Dimension(400, 500);

    public MinerWindow() {
        super(WINDOW_TITLE);
    }

    private JPanel configurationPanel = new JPanel();
    private JPanel miningPanel = new JPanel();
    private JPanel creditsPanel = new JPanel();

    private JTextField kristAddrBox = new JTextField();
    private JSpinner worksizeBox = new JSpinner();
    private JButton profileButton = new JButton("Profile");

    private void initConfigPanel() {
        configurationPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        configurationPanel.add(new JLabel("Krist Address:"), c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 3;
        configurationPanel.add(kristAddrBox, c);

        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        configurationPanel.add(new JLabel("Worksize:"), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.5f;
        worksizeBox.setValue(65535);
        configurationPanel.add(worksizeBox, c);

        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 1.5f;
        configurationPanel.add(profileButton, c);
    }

    private void initMiningPanel() {

    }

    private void initCreditsPanel() {
        creditsPanel.setLayout(new BorderLayout());
        creditsPanel.add(new JLabel("JCLMiner by apemanzilla, GUI by Lignum"), BorderLayout.CENTER);
    }

    private void initUI() {
        // make sure the process ends when closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPreferredSize(WINDOW_START_SIZE);
        setSize(WINDOW_START_SIZE);

        // centres the window
        setLocationRelativeTo(null);

        configurationPanel.setBorder(BorderFactory.createTitledBorder("Config"));
        miningPanel.setBorder(BorderFactory.createTitledBorder("Mining"));

        setLayout(new BorderLayout());

        initConfigPanel();
        add(configurationPanel, BorderLayout.NORTH);

        initMiningPanel();
        add(miningPanel, BorderLayout.CENTER); // *centre

        initCreditsPanel();
        add(creditsPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void run() {
        initUI();
    }
}
