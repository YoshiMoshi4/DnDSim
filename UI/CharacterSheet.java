package UI;

import EntityRes.CharSheet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Standalone viewer window for a character sheet.
 * Wraps SheetPanel in a JFrame for individual character viewing.
 */
public class CharacterSheet extends JFrame implements ActionListener {

    private SheetPanel sheetPanel;
    private JButton closeBtn;

    public CharacterSheet(CharSheet sheet) {
        setTitle("Character Sheet - " + sheet.getName());
        setSize(750, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create and add the SheetPanel
        sheetPanel = new SheetPanel(sheet);
        add(sheetPanel, BorderLayout.CENTER);

        // Bottom panel with close button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        closeBtn = new JButton("Close");
        closeBtn.addActionListener(this);
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initial update
        sheetPanel.updateSheet();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeBtn) {
            dispose();
        }
    }

    public SheetPanel getSheetPanel() {
        return sheetPanel;
    }
}
