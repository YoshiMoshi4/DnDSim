package UI.Battle;

import EntityRes.*;
import Objects.*;
import UI.CharacterSheetMenu;
import UI.SheetButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class BattleSystem extends JFrame implements ActionListener {

    BattleGrid grid;
    TurnManager turnManager;
    List<Entity> entities;
    List<TerrainObject> terrainObjects;
    List<Pickup> pickups;
    CharacterSheetMenu sheetMenu;
    BattleGridPanel gridPanel;
    TimelinePanel timelinePanel;
    JPanel controlPanel;
    JButton nextTurnBtn;
    JButton beginBattleBtn;
    JButton addObjBtn;
    boolean battleStarted;
    private java.util.Map<String, Integer> nonpartyInstanceCounts = new java.util.HashMap<>();  // Track instance numbers for nonparty entities

    public BattleSystem(int row, int column, CharacterSheetMenu sheetMenu) {
        setTitle("Battle System");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleBack();
            }
        });
        setLocationRelativeTo(null);
        setResizable(true);
        this.sheetMenu = sheetMenu;
        this.battleStarted = false;

        entities = new ArrayList<>(List.of(
                //new Player(0, 0, new CharSheet("Hero", true, 20, new int[]{5, 5, 5, 5})),
                //new Enemy(0, 1, new CharSheet("Goblin", false, 15, new int[]{3, 3, 3, 3}))
        ));

        // Weapon dagger = new Weapon("Dagger", "Weapon", 2, new int[]{1, 0, 0, 0});
        // Weapon sword = new Weapon("Sword", "Weapon", 4, new int[]{1, 0, 0, 0});

        // entities.get(0).getCharSheet().pickupItem(sword);
        // entities.get(0).getCharSheet().equipPrimaryWeapon(sword);

        // entities.get(1).getCharSheet().pickupItem(dagger);
        // entities.get(1).getCharSheet().equipPrimaryWeapon(dagger);

        terrainObjects = new ArrayList<>(List.of(
                //new Rock(2, 2, 10),
                //new Rock(3, 3, 10)
        ));

        pickups = new ArrayList<>(List.of(
                //new Pickup(1, 1, new Weapon("Sword", "Weapon", 4, new int[]{1, 0, 0})),
                //new Pickup(4, 4, new Item("Health Potion", "Consumable"))
        ));

        grid = new BattleGrid(row, column, entities, terrainObjects, pickups);
        turnManager = new TurnManager(entities);
        turnManager.setBattleStarted(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gridPanel = new BattleGridPanel(grid, turnManager, this);
        mainPanel.add(gridPanel, java.awt.BorderLayout.CENTER);

        // Timeline panel (will be shown after battle starts)
        timelinePanel = new TimelinePanel(turnManager);
        mainPanel.add(timelinePanel, java.awt.BorderLayout.NORTH);
        timelinePanel.setVisible(false);

        controlPanel = new JPanel();
        controlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        JButton backBtn = new JButton("Back to Sheets");
        backBtn.setPreferredSize(new java.awt.Dimension(140, 40));
        backBtn.addActionListener(e -> handleBack());
        controlPanel.add(backBtn);

        addObjBtn = new JButton("Add Objects");
        addObjBtn.setPreferredSize(new java.awt.Dimension(140, 40));
        addObjBtn.addActionListener(e -> handleAddObjects());
        controlPanel.add(addObjBtn);

        beginBattleBtn = new JButton("Begin Battle");
        beginBattleBtn.setPreferredSize(new java.awt.Dimension(140, 40));
        beginBattleBtn.addActionListener(e -> handleBeginBattle());
        controlPanel.add(beginBattleBtn);

        nextTurnBtn = new JButton("Next Turn");
        nextTurnBtn.setPreferredSize(new java.awt.Dimension(140, 40));
        nextTurnBtn.addActionListener(e -> handleNextTurn());
        nextTurnBtn.setEnabled(false);
        controlPanel.add(nextTurnBtn);

        JButton endBattleBtn = new JButton("End Battle");
        endBattleBtn.setPreferredSize(new java.awt.Dimension(140, 40));
        endBattleBtn.addActionListener(e -> handleEndBattle());
        controlPanel.add(endBattleBtn);

        mainPanel.add(controlPanel, java.awt.BorderLayout.SOUTH);
        add(mainPanel);
        sheetMenu.setBattleSystem(this);
        
        // Prompt to add objects at start
        handleAddObjects();
        setVisible(true);
    }
    
    private void handleBeginBattle() {
        if (grid.getEntities().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one entity before beginning battle.", "No Entities", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        battleStarted = true;
        turnManager.setBattleStarted(true);
        turnManager.calculateInitiativeOrder();
        
        timelinePanel.setVisible(true);
        beginBattleBtn.setEnabled(false);
        nextTurnBtn.setEnabled(true);
        
        gridPanel.setBattleStarted(true);
        timelinePanel.repaint();
        gridPanel.repaint();
    }
    
    private void handleNextTurn() {
        turnManager.nextTurn();
        timelinePanel.repaint();
        gridPanel.repaint();
    }
    
    public boolean isBattleStarted() {
        return battleStarted;
    }
    
    public void addEntity(Entity entity) {
        turnManager.addEntity(entity);
        if (battleStarted) {
            timelinePanel.repaint();
        }
    }
    
    public void removeEntity(Entity entity) {
        turnManager.removeEntity(entity);
        if (battleStarted) {
            timelinePanel.repaint();
        }
    }
    
    public void updateTimeline() {
        timelinePanel.repaint();
    }

    private void handleBack() {
        this.setVisible(false);
        sheetMenu.setVisible(true);
    }

    private void handleAddObjects() {
        JDialog addObjDialog = new JDialog(this, "Add Objects to Battle", false); // Non-modal to see grid
        addObjDialog.setSize(400, 500);
        
        // Position dialog to the right side of the main window
        Point mainLoc = this.getLocation();
        int dialogX = mainLoc.x + this.getWidth() - 10; // Slight overlap
        int dialogY = mainLoc.y;
        // Keep dialog on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (dialogX + 400 > screenSize.width) {
            dialogX = mainLoc.x - 400 + 10; // Put on left side instead
        }
        addObjDialog.setLocation(dialogX, dialogY);
        addObjDialog.setLayout(new BorderLayout(10, 10));
        addObjDialog.setAlwaysOnTop(true);
        
        // Style constants
        Color bgColor = new Color(45, 45, 48);
        Color tabBgColor = new Color(60, 60, 65);
        Color buttonBgColor = new Color(70, 70, 75);
        Color buttonHoverColor = new Color(90, 90, 95);
        Color textColor = new Color(220, 220, 220);
        Color accentColor = new Color(86, 156, 214);
        Font buttonFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
        
        addObjDialog.getContentPane().setBackground(bgColor);
        
        // Counter label for feedback
        JLabel statusLabel = new JLabel("Click items to add them to the battlefield");
        statusLabel.setForeground(textColor);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 5, 10));
        addObjDialog.add(statusLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(bgColor);
        tabbedPane.setForeground(textColor);
        tabbedPane.setFont(buttonFont);

        // Helper method to create styled buttons
        java.util.function.Function<String, JButton> createStyledButton = (text) -> {
            JButton btn = new JButton(text);
            btn.setFont(buttonFont);
            btn.setBackground(buttonBgColor);
            btn.setForeground(textColor);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBackground(buttonHoverColor);
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(buttonBgColor);
                }
            });
            return btn;
        };
        
        // Helper to create section headers
        java.util.function.Function<String, JLabel> createHeader = (text) -> {
            JLabel header = new JLabel(text);
            header.setFont(headerFont);
            header.setForeground(accentColor);
            header.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
            return header;
        };

        // Party Character Sheets Tab
        JPanel partyPanel = new JPanel();
        partyPanel.setLayout(new BoxLayout(partyPanel, BoxLayout.Y_AXIS));
        partyPanel.setBackground(tabBgColor);
        partyPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        ArrayList<SheetButton> sheets = sheetMenu.getSheets();
        int partyCount = 0;
        for (SheetButton sheetBtn : sheets) {
            if (sheetBtn.getSheet().getCharSheet().getParty()) {
                partyCount++;
            }
        }
        
        if (partyCount == 0) {
            JLabel emptyLabel = new JLabel("No party characters available");
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            partyPanel.add(emptyLabel);
        } else {
            for (SheetButton sheetBtn : sheets) {
                if (sheetBtn.getSheet().getCharSheet().getParty()) {
                    CharSheet cs = sheetBtn.getSheet().getCharSheet();
                    String btnText = String.format("%s  |  HP: %d/%d  |  Class: %s", 
                        sheetBtn.getText(), cs.getCurrentHP(), cs.getTotalHP(),
                        cs.getCharacterClass() != null ? cs.getCharacterClass() : "None");
                    JButton btn = createStyledButton.apply(btnText);
                    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                    btn.addActionListener(e -> {
                        CharSheet charSheet = sheetBtn.getSheet().getCharSheet();
                        Entity newEntity = new Entity(0, 0, charSheet);
                        grid.addEntityAtNextAvailable(newEntity);
                        addEntity(newEntity);
                        gridPanel.repaint();
                        statusLabel.setText("Added: " + sheetBtn.getText());
                    });
                    partyPanel.add(Box.createVerticalStrut(5));
                    partyPanel.add(btn);
                }
            }
        }
        JScrollPane partyScroll = new JScrollPane(partyPanel);
        partyScroll.setBorder(null);
        partyScroll.getViewport().setBackground(tabBgColor);
        tabbedPane.addTab("Party", partyScroll);
        
        // Non-Party Character Sheets Tab (enemies)
        JPanel nonpartyPanel = new JPanel();
        nonpartyPanel.setLayout(new BoxLayout(nonpartyPanel, BoxLayout.Y_AXIS));
        nonpartyPanel.setBackground(tabBgColor);
        nonpartyPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        int enemyCount = 0;
        for (SheetButton sheetBtn : sheets) {
            if (!sheetBtn.getSheet().getCharSheet().getParty()) {
                enemyCount++;
            }
        }
        
        if (enemyCount == 0) {
            JLabel emptyLabel = new JLabel("No enemies available");
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            nonpartyPanel.add(emptyLabel);
        } else {
            for (SheetButton sheetBtn : sheets) {
                if (!sheetBtn.getSheet().getCharSheet().getParty()) {
                    CharSheet cs = sheetBtn.getSheet().getCharSheet();
                    String baseName = sheetBtn.getText();
                    Weapon equipped = cs.getEquippedWeapon();
                    String btnText = String.format("%s  |  HP: %d  |  ATK: %d", 
                        baseName, cs.getTotalHP(), equipped != null ? equipped.getDamage() : 0);
                    JButton btn = createStyledButton.apply(btnText);
                    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                    btn.addActionListener(e -> {
                        CharSheet blueprintSheet = CharSheet.load(baseName, false);
                        if (blueprintSheet != null) {
                            CharSheet instanceSheet = blueprintSheet.deepCopy();
                            int instanceNum = nonpartyInstanceCounts.getOrDefault(baseName, 0) + 1;
                            nonpartyInstanceCounts.put(baseName, instanceNum);
                            Entity newEntity = new Entity(0, 0, instanceSheet);
                            newEntity.setInstanceNumber(instanceNum);
                            grid.addEntityAtNextAvailable(newEntity);
                            addEntity(newEntity);
                            gridPanel.repaint();
                            statusLabel.setText("Added: " + baseName + " #" + instanceNum);
                        }
                    });
                    nonpartyPanel.add(Box.createVerticalStrut(5));
                    nonpartyPanel.add(btn);
                }
            }
        }
        JScrollPane nonpartyScroll = new JScrollPane(nonpartyPanel);
        nonpartyScroll.setBorder(null);
        nonpartyScroll.getViewport().setBackground(tabBgColor);
        tabbedPane.addTab("Enemies", nonpartyScroll);

        // Terrain Tab
        JPanel terrainPanel = new JPanel();
        terrainPanel.setLayout(new BoxLayout(terrainPanel, BoxLayout.Y_AXIS));
        terrainPanel.setBackground(tabBgColor);
        terrainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        List<TerrainObject> availableTerrains = UI.TerrainDatabase.getInstance().getAllTerrains();
        if (availableTerrains.isEmpty()) {
            JLabel emptyLabel = new JLabel("No terrain objects available");
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            terrainPanel.add(emptyLabel);
        } else {
            for (TerrainObject terrain : availableTerrains) {
                String btnText = String.format("%s  |  HP: %d", terrain.getType(), terrain.getHealth());
                JButton terrainBtn = createStyledButton.apply(btnText);
                terrainBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                terrainBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                terrainBtn.addActionListener(e -> {
                    TerrainObject newTerrain = new TerrainObject(0, 0, terrain.getType(), terrain.getHealth());
                    grid.addTerrainAtNextAvailable(newTerrain);
                    gridPanel.repaint();
                    statusLabel.setText("Added terrain: " + terrain.getType());
                });
                terrainPanel.add(Box.createVerticalStrut(5));
                terrainPanel.add(terrainBtn);
            }
        }
        JScrollPane terrainScroll = new JScrollPane(terrainPanel);
        terrainScroll.setBorder(null);
        terrainScroll.getViewport().setBackground(tabBgColor);
        tabbedPane.addTab("Terrain", terrainScroll);

        // Pickups Tab
        JPanel pickupPanel = new JPanel();
        pickupPanel.setLayout(new BoxLayout(pickupPanel, BoxLayout.Y_AXIS));
        pickupPanel.setBackground(tabBgColor);
        pickupPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // Weapons section
        Map<String, Weapon> availableWeapons = ItemDatabase.getInstance().getAllWeapons();
        if (!availableWeapons.isEmpty()) {
            JLabel weaponHeader = createHeader.apply("Weapons");
            weaponHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            pickupPanel.add(weaponHeader);
            for (Weapon weapon : availableWeapons.values()) {
                String btnText = String.format("%s  |  DMG: %d", 
                    weapon.getName(), weapon.getDamage());
                JButton itemBtn = createStyledButton.apply(btnText);
                itemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                itemBtn.addActionListener(e -> {
                    Pickup itemPickup = new Pickup(0, 0, weapon);
                    grid.addPickupAtNextAvailable(itemPickup);
                    gridPanel.repaint();
                    statusLabel.setText("Added pickup: " + weapon.getName());
                });
                pickupPanel.add(Box.createVerticalStrut(3));
                pickupPanel.add(itemBtn);
            }
        }
        
        // Armors section
        Map<String, Armor> availableArmors = ItemDatabase.getInstance().getAllArmors();
        if (!availableArmors.isEmpty()) {
            JLabel armorHeader = createHeader.apply("Armor");
            armorHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            pickupPanel.add(armorHeader);
            String[] armorSlotNames = {"Head", "Torso", "Legs"};
            for (Armor armor : availableArmors.values()) {
                int slotIndex = armor.getArmorType();
                String slotName = (slotIndex >= 0 && slotIndex < armorSlotNames.length) ? armorSlotNames[slotIndex] : "?";
                String btnText = String.format("%s  |  DEF: %d  |  Slot: %s", 
                    armor.getName(), armor.getDefense(), slotName);
                JButton itemBtn = createStyledButton.apply(btnText);
                itemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                itemBtn.addActionListener(e -> {
                    Pickup itemPickup = new Pickup(0, 0, armor);
                    grid.addPickupAtNextAvailable(itemPickup);
                    gridPanel.repaint();
                    statusLabel.setText("Added pickup: " + armor.getName());
                });
                pickupPanel.add(Box.createVerticalStrut(3));
                pickupPanel.add(itemBtn);
            }
        }
        
        // Consumables section
        Map<String, Consumable> availableConsumables = ItemDatabase.getInstance().getAllConsumables();
        if (!availableConsumables.isEmpty()) {
            JLabel consumableHeader = createHeader.apply("Consumables");
            consumableHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            pickupPanel.add(consumableHeader);
            for (Consumable consumable : availableConsumables.values()) {
                Status effect = consumable.getEffect();
                String effectName = (effect != null) ? effect.getName() : "Heal";
                String btnText = String.format("%s  |  %s: %d", 
                    consumable.getName(), effectName, consumable.getHealAmount());
                JButton itemBtn = createStyledButton.apply(btnText);
                itemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                itemBtn.addActionListener(e -> {
                    Pickup itemPickup = new Pickup(0, 0, consumable);
                    grid.addPickupAtNextAvailable(itemPickup);
                    gridPanel.repaint();
                    statusLabel.setText("Added pickup: " + consumable.getName());
                });
                pickupPanel.add(Box.createVerticalStrut(3));
                pickupPanel.add(itemBtn);
            }
        }
        
        if (availableWeapons.isEmpty() && availableArmors.isEmpty() && availableConsumables.isEmpty()) {
            JLabel emptyLabel = new JLabel("No pickup items available");
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            pickupPanel.add(emptyLabel);
        }
        
        JScrollPane pickupScroll = new JScrollPane(pickupPanel);
        pickupScroll.setBorder(null);
        pickupScroll.getViewport().setBackground(tabBgColor);
        tabbedPane.addTab("Pickups", pickupScroll);

        addObjDialog.add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel with Done button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(bgColor);
        
        JButton doneBtn = new JButton("Done");
        doneBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        doneBtn.setBackground(accentColor);
        doneBtn.setForeground(Color.WHITE);
        doneBtn.setFocusPainted(false);
        doneBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        doneBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        doneBtn.addActionListener(e -> addObjDialog.dispose());
        bottomPanel.add(doneBtn);
        
        addObjDialog.add(bottomPanel, BorderLayout.SOUTH);

        addObjDialog.setVisible(true);
    }

    private void handleEndBattle() {
        int result = JOptionPane.showConfirmDialog(this, "End battle? Party entity states will be saved.", "Confirm End Battle", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            // Save all party entities
            for (Entity entity : grid.getEntities()) {
                if (entity.isParty()) {
                    entity.getCharSheet().save();
                }
            }
            
            // Non-party entities are not saved and their changes are discarded
            // (They maintain independent instance states during battle, but don't persist)
            
            sheetMenu.endBattle();
            this.dispose();
            sheetMenu.setVisible(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void setBattleVisible(boolean visible) {
        this.setVisible(visible);
        if (visible) {
            gridPanel.repaint(); // Refresh display when returning to battle
        }
    }

}
