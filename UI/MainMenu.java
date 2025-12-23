package UI;

import UI.Battle.BattleSystem;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class MainMenu extends JFrame implements ActionListener {

    private JButton battleButton;
    private JButton characterButton;
    private JPanel mainPanel;
    private CharacterSheetMenu characterSheetMenu;

    public MainMenu() {
        setTitle("Main Menu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Border outer = BorderFactory.createLineBorder(Color.BLACK);
        Border inner = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border compound = BorderFactory.createCompoundBorder(outer, inner);

        mainPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setPreferredSize(new Dimension(0, 120));
        add(mainPanel, BorderLayout.CENTER);

        battleButton = new JButton("Start Battle System");
        battleButton.setFont(new Font("Arial", Font.BOLD, 30));
        battleButton.setBorder(compound);
        battleButton.setFocusPainted(false);
        battleButton.addActionListener(this);
        mainPanel.add(battleButton);

        characterButton = new JButton("Open Character Sheet");
        characterButton.setFont(new Font("Arial", Font.BOLD, 30));
        characterButton.setBorder(compound);
        characterButton.setFocusPainted(false);
        characterButton.addActionListener(this);
        mainPanel.add(characterButton);

        characterSheetMenu = new CharacterSheetMenu();
        characterSheetMenu.setVisible(false);

        pack();
        setSize(400, 300);
        mainPanel.revalidate();
        mainPanel.repaint();
        setVisible(true);
    }

    public MainMenu(CharacterSheetMenu c)
    {
        this();
        this.characterSheetMenu = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == battleButton) {
            handleBattleSystem();
        } else if (e.getSource() == characterButton) {
            handleCharacterSheet();
        }
    }

    public void handleBattleSystem() {
        final int[] row = {0};
        final int[] column = {0};

        JDialog dialog = new JDialog(this, "Add Product", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel rowLabel = new JLabel("Number of rows:");
        JTextField rowField = new JTextField();
        panel.add(rowLabel);
        panel.add(rowField);

        JLabel columnLabel = new JLabel("Number of columns:");
        JTextField columnField = new JTextField();
        panel.add(columnLabel);
        panel.add(columnField);

        JButton submitBtn = new JButton("Begin Battle");
        JButton cancelBtn = new JButton("Cancel");

        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rowField.getText().isEmpty() || columnField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                row[0] = Integer.parseInt(rowField.getText());
                column[0] = Integer.parseInt(columnField.getText());
                dialog.dispose();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                new MainMenu(characterSheetMenu);
            }
        });

        panel.add(submitBtn);
        panel.add(cancelBtn);

        dialog.add(panel);
        dialog.setVisible(true);

        BattleSystem battleSystem = new BattleSystem(row[0], column[0]);
        battleSystem.setVisible(false);
        this.dispose();
    }

    public void handleCharacterSheet() {
        characterSheetMenu.setVisible(true);
        this.dispose();
    }

}
