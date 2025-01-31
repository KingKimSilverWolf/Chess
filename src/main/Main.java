package main;

import javax.swing.*;

public class Main {

    public static void main(String[]args){
        JFrame window = new JFrame("Chess");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // Add game panel to window
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // So window is correct size

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.launchGame();
    }
}
