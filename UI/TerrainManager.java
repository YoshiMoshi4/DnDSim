package UI;

import Objects.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class TerrainManager extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JButton backBtn;
    private JButton addTerrainBtn;

    public TerrainManager() {
        setTitle("Terrain Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        backBtn = new JButton("Back to Main Menu");
        backBtn.addActionListener(this);
        topPanel.add(backBtn);

        addTerrainBtn = new JButton("Add New Terrain");
        addTerrainBtn.addActionListener(this);
        topPanel.add(addTerrainBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel with terrain list
        refreshDisplay();

        add(mainPanel);
        setVisible(true);
    }

    private void addNewTerrain() {
        JDialog dialog = new JDialog(this, "Add New Terrain", true);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));

        JTextField typeBox = new JTextField();
        JTextField hpField = new JTextField();
        JComboBox<String> colorDropdown = new JComboBox<>(EntityRes.CharSheet.getColorNames());
        JCheckBox blocksMovementBox = new JCheckBox("Blocks Movement", true);

        dialog.add(new JLabel("Type:"));
        dialog.add(typeBox);
        dialog.add(new JLabel("HP:"));
        dialog.add(hpField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);
        dialog.add(new JLabel(""));
        dialog.add(blocksMovementBox);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            String type = typeBox.getText();
            int hp = Integer.parseInt(hpField.getText());
            int color = colorDropdown.getSelectedIndex();
            boolean blocksMovement = blocksMovementBox.isSelected();
            TerrainObject newTerrain = new TerrainObject(0, 0, type, hp, color, blocksMovement);
            TerrainDatabase.getInstance().addTerrain(newTerrain);
            dialog.dispose();
            refreshDisplay();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void editTerrain(TerrainObject terrain) {
        JDialog dialog = new JDialog(this, "Edit Terrain", true);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));

        JTextField typeBox = new JTextField(terrain.getType());
        JTextField hpField = new JTextField(String.valueOf(terrain.getHealth()));
        JComboBox<String> colorDropdown = new JComboBox<>(EntityRes.CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(terrain.getColor());
        JCheckBox blocksMovementBox = new JCheckBox("Blocks Movement", terrain.blocksMovement());

        dialog.add(new JLabel("Type:"));
        dialog.add(typeBox);
        dialog.add(new JLabel("HP:"));
        dialog.add(hpField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);
        dialog.add(new JLabel(""));
        dialog.add(blocksMovementBox);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            terrain.setColor(colorDropdown.getSelectedIndex());
            terrain.setBlocksMovement(blocksMovementBox.isSelected());
            TerrainDatabase.getInstance().saveTerrain(terrain);
            dialog.dispose();
            refreshDisplay();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void deleteTerrain(TerrainObject terrain) {
        int result = JOptionPane.showConfirmDialog(this, "Delete this terrain?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            TerrainDatabase.getInstance().deleteTerrain(terrain);
            refreshDisplay();
        }
    }

    private void refreshDisplay() {
        // Clear existing center panel
        BorderLayout layout = (BorderLayout) mainPanel.getLayout();
        if (layout.getLayoutComponent(BorderLayout.CENTER) != null) {
            mainPanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));
        }

        // Center panel with terrain list
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(0, 2, 10, 10)); // 2 columns

        List<TerrainObject> terrains = TerrainDatabase.getInstance().getAllTerrains();
        for (TerrainObject terrain : terrains) {
            JPanel terrainPanel = new JPanel();
            terrainPanel.setLayout(new BoxLayout(terrainPanel, BoxLayout.Y_AXIS));
            terrainPanel.setBorder(BorderFactory.createTitledBorder(terrain.getType()));

            terrainPanel.add(new JLabel("HP: " + terrain.getHealth()));
            terrainPanel.add(new JLabel("Blocks Movement: " + terrain.blocksMovement()));

            JButton editBtn = new JButton("Edit");
            editBtn.addActionListener(e -> editTerrain(terrain));
            terrainPanel.add(editBtn);

            JButton deleteBtn = new JButton("Delete");
            deleteBtn.addActionListener(e -> deleteTerrain(terrain));
            terrainPanel.add(deleteBtn);

            centerPanel.add(terrainPanel);
        }

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backBtn) {
            new MainMenu();
            dispose();
        } else if (e.getSource() == addTerrainBtn) {
            addNewTerrain();
        }
    }
}