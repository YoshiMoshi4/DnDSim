package UI;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

import EntityRes.CharSheet;
import java.util.ArrayList;
import UI.Battle.BattleSystem;
// import tools.jackson.jr.ob.impl.JSONReader;
// import tools.jackson.jr.ob.impl.JSONWriter;
// import tools.jackson.jr.ob.comp.ObjectComposer;

public class CharacterSheetMenu extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JButton backBtn;
    private JButton saveBtn;
    private JButton loadBtn;
    private JButton newBtn;
    private JButton returnToBattleBtn;
    private ArrayList<SheetButton> sheets = new ArrayList<SheetButton>();
    private BattleSystem battleSystem;

    public CharacterSheetMenu() {
        setTitle("Character Sheet");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new java.awt.Dimension(800, 400));

        // Additional UI components and layout setup can be added here
        mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        backBtn = new JButton("Back");
        backBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        backBtn.addActionListener(this);
        buttonPanel.add(backBtn);

        saveBtn = new JButton("Save");
        saveBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        saveBtn.addActionListener(this);
        buttonPanel.add(saveBtn);

        loadBtn = new JButton("Load");
        loadBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        loadBtn.addActionListener(this);
        buttonPanel.add(loadBtn);

        newBtn = new JButton("New");
        newBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        newBtn.addActionListener(this);
        buttonPanel.add(newBtn);

        returnToBattleBtn = new JButton("Return to Battle");
        returnToBattleBtn.setPreferredSize(new java.awt.Dimension(140, 35));
        returnToBattleBtn.addActionListener(this);
        returnToBattleBtn.setEnabled(false);
        buttonPanel.add(returnToBattleBtn);

        mainPanel.add(buttonPanel, java.awt.BorderLayout.NORTH);

        sheets = new ArrayList<SheetButton>();
        autoLoadSheets();
    }

    private void autoLoadSheets() {
        // Load party entities
        loadEntitiesFromFolder("party");
        
        // Load nonparty entities
        loadEntitiesFromFolder("nonparty");
        
        if (!sheets.isEmpty()) {
            sheets.get(0).getSheet().setVisible(true);
        }
        updateSheetButtons();
    }
    
    private void loadEntitiesFromFolder(String folder) {
        boolean isParty = folder.equals("party");
        java.io.File savesDir = new java.io.File("saves/entities/" + folder);
        if (savesDir.exists() && savesDir.isDirectory()) {
            java.io.File[] files = savesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (java.io.File file : files) {
                    String fileName = file.getName();
                    String charName = fileName.substring(0, fileName.length() - 5); // Remove .json
                    CharSheet loaded = CharSheet.load(charName, isParty);
                    if (loaded != null) {
                        SheetPanel sheetPanel = new SheetPanel(loaded);
                        sheetPanel.updateSheet();
                        sheetPanel.setVisible(false);
                        sheets.add(new SheetButton(sheetPanel, loaded.getName()));
                    }
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle action events for the Character Sheet here
        if (e.getSource() == backBtn) {
            handleBack();
        } else if (e.getSource() == saveBtn) {
            handleSave();
        } else if (e.getSource() == loadBtn) {
            handleLoad();
        } else if (e.getSource() == newBtn) {
            handleNew();
        } else if (e.getSource() == returnToBattleBtn) {
            handleReturnToBattle();
        }
    }

    public void handleBack() {
        this.setVisible(false);
        new MainMenu(this);
    }

    public void handleSave() {
        for (SheetButton sheetBtn : sheets) {
            sheetBtn.getSheet().getCharSheet().save();
        }
        JOptionPane.showMessageDialog(this, "All character sheets saved!");
    }

    public void handleLoad() {
        String name = JOptionPane.showInputDialog(this, "Enter character name to load:");
        if (name != null && !name.trim().isEmpty()) {
            // Try to load from party folder first
            CharSheet loaded = CharSheet.load(name.trim(), true);
            if (loaded == null) {
                // Try nonparty folder
                loaded = CharSheet.load(name.trim(), false);
            }
            if (loaded != null) {
                SheetPanel panel = new SheetPanel(loaded);
                panel.updateSheet();
                panel.setVisible(false);
                sheets.add(new SheetButton(panel, loaded.getName()));
                mainPanel.add(panel);
                updateSheetButtons();
                JOptionPane.showMessageDialog(this, "Character sheet loaded!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load character sheet.");
            }
        }
    }

    public void handleNew() {
        JDialog newSheetDialog = new JDialog(this, "New Sheet");
        newSheetDialog.setSize(450, 360);
        newSheetDialog.setLocationRelativeTo(this);
        newSheetDialog.setResizable(false);
        newSheetDialog.setLayout(null);
        newSheetDialog.setVisible(true);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(10, 10, 80, 25);
        newSheetDialog.add(nameLabel);
        JTextField name = new JTextField("New Character");
        name.setBounds(100, 10, 100, 25);
        newSheetDialog.add(name);

        JButton isParty = new JButton("Party");
        isParty.setBounds(10, 40, 100, 25);
        isParty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isParty.getText().equals("Party")) {
                    isParty.setText("Not Party");
                } else {
                    isParty.setText("Party");
                }
            }
        });
        newSheetDialog.add(isParty);

        JComboBox<String> colorDropdown = new JComboBox<>(EntityRes.CharSheet.getColorNames());
        colorDropdown.setBounds(120, 40, 120, 25);
        newSheetDialog.add(colorDropdown);

        JLabel hpLabel = new JLabel("Total HP:");
        hpLabel.setBounds(10, 70, 80, 25);
        newSheetDialog.add(hpLabel);
        JTextField totalHP = new JTextField("20");
        totalHP.setBounds(100, 70, 100, 25);
        newSheetDialog.add(totalHP);

        JLabel strLabel = new JLabel("STR:");
        strLabel.setBounds(10, 100, 80, 25);
        newSheetDialog.add(strLabel);
        JTextField str = new JTextField("5");
        str.setBounds(100, 100, 100, 25);
        newSheetDialog.add(str);
        
        JLabel dexLabel = new JLabel("DEX:");
        dexLabel.setBounds(10, 130, 80, 25);
        newSheetDialog.add(dexLabel);
        JTextField dex = new JTextField("5");
        dex.setBounds(100, 130, 100, 25);
        newSheetDialog.add(dex);
        
        JLabel itvLabel = new JLabel("ITV:");
        itvLabel.setBounds(10, 160, 80, 25);
        newSheetDialog.add(itvLabel);
        JTextField itv = new JTextField("5");
        itv.setBounds(100, 160, 100, 25);
        newSheetDialog.add(itv);
        
        JLabel mobLabel = new JLabel("MOB:");
        mobLabel.setBounds(10, 190, 80, 25);
        newSheetDialog.add(mobLabel);
        JTextField mob = new JTextField("5");
        mob.setBounds(100, 190, 100, 25);
        newSheetDialog.add(mob);
        
        // Weapon selection
        JLabel weaponLabel = new JLabel("Primary Weapon:");
        weaponLabel.setBounds(250, 10, 120, 25);
        newSheetDialog.add(weaponLabel);
        
        JComboBox<String> weaponDropdown = new JComboBox<>();
        EntityRes.ItemDatabase db = EntityRes.ItemDatabase.getInstance();
        for (String weaponName : db.getAllWeapons().keySet()) {
            weaponDropdown.addItem(weaponName);
        }
        if (weaponDropdown.getItemCount() > 0) {
            weaponDropdown.setSelectedIndex(0);
        }
        weaponDropdown.setBounds(250, 40, 150, 25);
        newSheetDialog.add(weaponDropdown);
        
        // Armor selection
        JLabel headLabel = new JLabel("Head Armor:");
        headLabel.setBounds(250, 70, 100, 25);
        newSheetDialog.add(headLabel);
        
        JComboBox<String> headDropdown = new JComboBox<>();
        for (String armorName : db.getAllArmors().keySet()) {
            EntityRes.Armor armor = db.getArmor(armorName);
            if (armor != null && armor.getArmorType() == 0) { // HEAD
                headDropdown.addItem(armorName);
            }
        }
        if (headDropdown.getItemCount() > 0) {
            headDropdown.setSelectedIndex(0);
        }
        headDropdown.setBounds(250, 100, 150, 25);
        newSheetDialog.add(headDropdown);
        
        JLabel torsoLabel = new JLabel("Torso Armor:");
        torsoLabel.setBounds(250, 130, 100, 25);
        newSheetDialog.add(torsoLabel);
        
        JComboBox<String> torsoDropdown = new JComboBox<>();
        for (String armorName : db.getAllArmors().keySet()) {
            EntityRes.Armor armor = db.getArmor(armorName);
            if (armor != null && armor.getArmorType() == 1) { // TORSO
                torsoDropdown.addItem(armorName);
            }
        }
        if (torsoDropdown.getItemCount() > 0) {
            torsoDropdown.setSelectedIndex(0);
        }
        torsoDropdown.setBounds(250, 160, 150, 25);
        newSheetDialog.add(torsoDropdown);
        
        JLabel legsLabel = new JLabel("Legs Armor:");
        legsLabel.setBounds(250, 190, 100, 25);
        newSheetDialog.add(legsLabel);
        
        JComboBox<String> legsDropdown = new JComboBox<>();
        for (String armorName : db.getAllArmors().keySet()) {
            EntityRes.Armor armor = db.getArmor(armorName);
            if (armor != null && armor.getArmorType() == 2) { // LEGS
                legsDropdown.addItem(armorName);
            }
        }
        if (legsDropdown.getItemCount() > 0) {
            legsDropdown.setSelectedIndex(0);
        }
        legsDropdown.setBounds(250, 220, 150, 25);
        newSheetDialog.add(legsDropdown);

        JButton submit = new JButton("Create");
        submit.setBounds(10, 290, 100, 30);
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean party = isParty.getText().equals("Party");
                    int totHP = Integer.parseInt(totalHP.getText());
                    int[] attr = new int[4];
                    attr[0] = Integer.parseInt(str.getText());
                    attr[1] = Integer.parseInt(dex.getText());
                    attr[2] = Integer.parseInt(itv.getText());
                    attr[3] = Integer.parseInt(mob.getText());

                    CharSheet newSheet = new CharSheet(name.getText(), party, totHP, attr, colorDropdown.getSelectedIndex());
                    
                    // Equip selected items
                    String selectedWeapon = (String) weaponDropdown.getSelectedItem();
                    if (selectedWeapon != null && !selectedWeapon.equals("Fist")) {
                        EntityRes.Weapon weapon = db.getWeapon(selectedWeapon);
                        if (weapon != null) {
                            newSheet.equipPrimaryWeapon(weapon);
                            newSheet.getInventory().add(weapon);
                        }
                    }
                    
                    String selectedHead = (String) headDropdown.getSelectedItem();
                    if (selectedHead != null && !selectedHead.equals("Bald")) {
                        EntityRes.Armor head = db.getArmor(selectedHead);
                        if (head != null) {
                            newSheet.equipHead(head);
                            newSheet.getInventory().add(head);
                        }
                    }
                    
                    String selectedTorso = (String) torsoDropdown.getSelectedItem();
                    if (selectedTorso != null && !selectedTorso.equals("Bare Chest")) {
                        EntityRes.Armor torso = db.getArmor(selectedTorso);
                        if (torso != null) {
                            newSheet.equipTorso(torso);
                            newSheet.getInventory().add(torso);
                        }
                    }
                    
                    String selectedLegs = (String) legsDropdown.getSelectedItem();
                    if (selectedLegs != null && !selectedLegs.equals("No Pants")) {
                        EntityRes.Armor legs = db.getArmor(selectedLegs);
                        if (legs != null) {
                            newSheet.equipLegs(legs);
                            newSheet.getInventory().add(legs);
                        }
                    }

                    SheetPanel panel = new SheetPanel(newSheet);
                    panel.updateSheet();
                    panel.setVisible(false);
                    sheets.add(new SheetButton(panel, name.getText()));
                    mainPanel.add(panel);
                    updateSheetButtons();
                    newSheet.save();
                    newSheetDialog.dispose();
                } catch (NumberFormatException ex) {
                    System.out.println("Error in character creation: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        newSheetDialog.add(submit);
        JButton cancel = new JButton("Cancel");
        cancel.setBounds(300, 290, 100, 30);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newSheetDialog.dispose();
            }
        });
        newSheetDialog.add(cancel);
    }

    public void updateSheetButtons() {
        // Create tabbed pane for party and nonparty entities
        JTabbedPane buttonTabbedPane = new JTabbedPane();
        
        // Party entities tab
        JPanel partyListPanel = new JPanel();
        partyListPanel.setLayout(new BoxLayout(partyListPanel, BoxLayout.Y_AXIS));
        partyListPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        for (SheetButton currSheet : sheets) {
            if (currSheet.getSheet().getCharSheet().getParty()) {
                addSheetButtonToPanel(partyListPanel, currSheet);
            }
        }
        
        JScrollPane partyScrollPane = new JScrollPane(partyListPanel);
        partyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        partyScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        buttonTabbedPane.addTab("Party", partyScrollPane);
        
        // Non-Party entities tab
        JPanel nonpartyListPanel = new JPanel();
        nonpartyListPanel.setLayout(new BoxLayout(nonpartyListPanel, BoxLayout.Y_AXIS));
        nonpartyListPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        for (SheetButton currSheet : sheets) {
            if (!currSheet.getSheet().getCharSheet().getParty()) {
                addSheetButtonToPanel(nonpartyListPanel, currSheet);
            }
        }
        
        JScrollPane nonpartyScrollPane = new JScrollPane(nonpartyListPanel);
        nonpartyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        nonpartyScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        buttonTabbedPane.addTab("Non-Party", nonpartyScrollPane);
        
        // Create a panel to hold all sheet panels
        JPanel sheetDisplayPanel = new JPanel();
        sheetDisplayPanel.setLayout(new java.awt.CardLayout());
        for (SheetButton currSheet : sheets) {
            sheetDisplayPanel.add(currSheet.getSheet(), currSheet.getText());
        }

        // Create a split pane to divide the list and display
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonTabbedPane, sheetDisplayPanel);
        splitPane.setDividerLocation(200);
        splitPane.setOneTouchExpandable(true);

        mainPanel.removeAll();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        topPanel.add(backBtn);
        topPanel.add(saveBtn);
        topPanel.add(loadBtn);
        topPanel.add(newBtn);
        topPanel.add(returnToBattleBtn);

        mainPanel.add(topPanel, java.awt.BorderLayout.NORTH);
        mainPanel.add(splitPane, java.awt.BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void addSheetButtonToPanel(JPanel panel, SheetButton currSheet) {
        JButton btn = new JButton(currSheet.getText());
        btn.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        btn.setPreferredSize(new java.awt.Dimension(150, 30));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (SheetButton sheet : sheets) {
                    sheet.getSheet().setVisible(false);
                }
                currSheet.getSheet().setVisible(true);
            }
        });
        panel.add(btn);
        panel.add(Box.createVerticalStrut(5));
    }

    public ArrayList<SheetButton> getSheets() {
        return sheets;
    }

    public void setBattleSystem(BattleSystem battleSystem) {
        this.battleSystem = battleSystem;
        returnToBattleBtn.setEnabled(true);
    }
    
    public void endBattle() {
        battleSystem = null;
        returnToBattleBtn.setEnabled(false);
    }

    public void handleReturnToBattle() {
        if (battleSystem != null) {
            this.setVisible(false);
            battleSystem.setBattleVisible(true);
        }
    }

}
