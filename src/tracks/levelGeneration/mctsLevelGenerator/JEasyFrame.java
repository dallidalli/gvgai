package tracks.levelGeneration.mctsLevelGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JEasyFrame extends JFrame {
    public Component comp;
    public JEasyFrame(Component comp, String title) {
        super(title);
        this.comp = comp;
        getContentPane().add(BorderLayout.CENTER, comp);
        pack();
        this.setVisible(true);
        this.
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        repaint();
    }
}


