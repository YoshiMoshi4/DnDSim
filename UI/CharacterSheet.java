package UI;

import java.awt.event.*;
import javax.swing.*;

public class CharacterSheet extends JFrame implements ActionListener {

    public CharacterSheet() {
        setTitle("Character Sheet");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // Additional UI components and layout setup can be added here
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle action events for the Character Sheet here
    }

}
