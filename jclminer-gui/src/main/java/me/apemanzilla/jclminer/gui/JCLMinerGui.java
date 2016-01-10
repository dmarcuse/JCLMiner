package me.apemanzilla.jclminer.gui;

import javax.swing.*;

public class JCLMinerGui {
    private static void setSystemUIStyle() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set look and feel!!\n");
        }
    }

    public static void main(String[] args) {
        //setSystemUIStyle();

        MinerWindow window = new MinerWindow();
        window.run();
    }
}
