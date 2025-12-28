package UI.Battle;

import EntityRes.*;
import Objects.*;
import UI.CharacterSheetMenu;
import UI.SheetButton;
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
        JDialog addObjDialog = new JDialog(this, "Add Objects to Battle");
        addObjDialog.setSize(400, 300);
        addObjDialog.setLocationRelativeTo(this);
        addObjDialog.setLayout(new java.awt.BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Party Character Sheets Tab
        JPanel partyPanel = new JPanel();
        partyPanel.setLayout(new BoxLayout(partyPanel, BoxLayout.Y_AXIS));
        ArrayList<SheetButton> sheets = sheetMenu.getSheets();
        for (SheetButton sheetBtn : sheets) {
            if (sheetBtn.getSheet().getCharSheet().getParty()) {
                JButton btn = new JButton(sheetBtn.getText());
                btn.addActionListener(e -> {
                    CharSheet charSheet = sheetBtn.getSheet().getCharSheet();
                    Entity newEntity = new Entity(0, 0, charSheet);
                    grid.addEntityAtNextAvailable(newEntity);
                    addEntity(newEntity);
                    gridPanel.repaint();
                });
                partyPanel.add(btn);
            }
        }
        JScrollPane partyScroll = new JScrollPane(partyPanel);
        tabbedPane.addTab("Party Characters", partyScroll);
        
        // Non-Party Character Sheets Tab (enemies)
        JPanel nonpartyPanel = new JPanel();
        nonpartyPanel.setLayout(new BoxLayout(nonpartyPanel, BoxLayout.Y_AXIS));
        for (SheetButton sheetBtn : sheets) {
            if (!sheetBtn.getSheet().getCharSheet().getParty()) {
                JButton btn = new JButton(sheetBtn.getText());
                String baseName = sheetBtn.getText();
                btn.addActionListener(e -> {
                    // Deep copy the CharSheet for this instance
                    CharSheet blueprintSheet = CharSheet.load(baseName, false);
                    if (blueprintSheet != null) {
                        CharSheet instanceSheet = blueprintSheet.deepCopy();
                        
                        // Assign instance number
                        int instanceNum = nonpartyInstanceCounts.getOrDefault(baseName, 0) + 1;
                        nonpartyInstanceCounts.put(baseName, instanceNum);
                        
                        Entity newEntity = new Entity(0, 0, instanceSheet);
                        newEntity.setInstanceNumber(instanceNum);
                        grid.addEntityAtNextAvailable(newEntity);
                        addEntity(newEntity);
                        gridPanel.repaint();
                    }
                });
                nonpartyPanel.add(btn);
            }
        }
        JScrollPane nonpartyScroll = new JScrollPane(nonpartyPanel);
        tabbedPane.addTab("Non-Party Characters", nonpartyScroll);

        // Terrain Tab
        JPanel terrainPanel = new JPanel();
        terrainPanel.setLayout(new BoxLayout(terrainPanel, BoxLayout.Y_AXIS));
        List<TerrainObject> availableTerrains = UI.TerrainDatabase.getInstance().getAllTerrains();
        for (TerrainObject terrain : availableTerrains) {
            JButton terrainBtn = new JButton(terrain.getType() + " (" + terrain.getHealth() + " HP)");
            terrainBtn.addActionListener(e -> {
                TerrainObject newTerrain = new TerrainObject(0, 0, terrain.getType(), terrain.getHealth());
                grid.addTerrainAtNextAvailable(newTerrain);
                gridPanel.repaint();
            });
            terrainPanel.add(terrainBtn);
        }
        JScrollPane terrainScroll = new JScrollPane(terrainPanel);
        tabbedPane.addTab("Terrain", terrainScroll);

        // Pickups Tab
        JPanel pickupPanel = new JPanel();
        pickupPanel.setLayout(new BoxLayout(pickupPanel, BoxLayout.Y_AXIS));
        
        // Add all weapons
        Map<String, Weapon> availableWeapons = ItemDatabase.getInstance().getAllWeapons();
        for (Weapon weapon : availableWeapons.values()) {
            JButton itemBtn = new JButton(weapon.getName() + " (Weapon)");
            itemBtn.addActionListener(e -> {
                Pickup itemPickup = new Pickup(0, 0, weapon);
                grid.addPickupAtNextAvailable(itemPickup);
                gridPanel.repaint();
            });
            pickupPanel.add(itemBtn);
        }
        
        // Add all armors
        Map<String, Armor> availableArmors = ItemDatabase.getInstance().getAllArmors();
        for (Armor armor : availableArmors.values()) {
            JButton itemBtn = new JButton(armor.getName() + " (Armor)");
            itemBtn.addActionListener(e -> {
                Pickup itemPickup = new Pickup(0, 0, armor);
                grid.addPickupAtNextAvailable(itemPickup);
                gridPanel.repaint();
            });
            pickupPanel.add(itemBtn);
        }
        
        // Add all consumables
        Map<String, Consumable> availableConsumables = ItemDatabase.getInstance().getAllConsumables();
        for (Consumable consumable : availableConsumables.values()) {
            JButton itemBtn = new JButton(consumable.getName() + " (Consumable)");
            itemBtn.addActionListener(e -> {
                Pickup itemPickup = new Pickup(0, 0, consumable);
                grid.addPickupAtNextAvailable(itemPickup);
                gridPanel.repaint();
            });
            pickupPanel.add(itemBtn);
        }
        JScrollPane pickupScroll = new JScrollPane(pickupPanel);
        tabbedPane.addTab("Pickups", pickupScroll);

        addObjDialog.add(tabbedPane, java.awt.BorderLayout.CENTER);

        // Cancel button
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> addObjDialog.dispose());
        addObjDialog.add(cancelBtn, java.awt.BorderLayout.SOUTH);

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
