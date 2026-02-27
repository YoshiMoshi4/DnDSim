package UI.Battle;

import EntityRes.*;
import Objects.*;
import UI.CharacterSheetView;
import UI.SheetButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class BattleView {

    private final Stage stage;
    private final BattleGrid grid;
    private final TurnManager turnManager;
    private final CharacterSheetView sheetView;
    private final BattleGridCanvas gridCanvas;
    private final TimelinePane timelinePane;
    private Button nextTurnBtn;
    private Button beginBattleBtn;
    private Button addObjBtn;
    private boolean battleStarted;
    private final Map<String, Integer> enemyInstanceCounts = new HashMap<>();
    
    // Pull-up panel
    private VBox addObjectsPanel;
    private boolean panelExpanded = false;
    private Label addStatusLabel;

    public BattleView(int rows, int cols, CharacterSheetView sheetView) {
        this.stage = new Stage();
        this.sheetView = sheetView;
        this.battleStarted = false;

        List<Entity> entities = new ArrayList<>();
        List<TerrainObject> terrainObjects = new ArrayList<>();
        List<Pickup> pickups = new ArrayList<>();

        grid = new BattleGrid(rows, cols, entities, terrainObjects, pickups);
        turnManager = new TurnManager(entities);
        turnManager.setBattleStarted(false);

        gridCanvas = new BattleGridCanvas(grid, turnManager, this);
        timelinePane = new TimelinePane(turnManager);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.getStyleClass().add("panel-dark");

        // Timeline at top (hidden initially)
        timelinePane.setVisible(false);
        root.setTop(timelinePane);

        // Grid in center
        root.setCenter(gridCanvas);

        // Bottom area with controls and pull-up panel
        VBox bottomArea = new VBox(0);
        
        // Controls panel
        HBox controlPanel = createControlPanel();
        
        // Add objects pull-up panel
        addObjectsPanel = createAddObjectsPanel();
        addObjectsPanel.setMaxHeight(0);
        addObjectsPanel.setMinHeight(0);
        addObjectsPanel.setPrefHeight(0);
        addObjectsPanel.setVisible(false);
        
        bottomArea.getChildren().addAll(addObjectsPanel, controlPanel);
        root.setBottom(bottomArea);

        Scene scene = new Scene(root, 1000, 750);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());

        stage.setTitle("Battle System");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            e.consume();
            handleBack();
        });

        sheetView.setBattleView(this);
    }

    private VBox createAddObjectsPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #505052; -fx-border-width: 1 0 0 0; -fx-background-color: #2d2d30;");
        
        // Header with status and close button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        addStatusLabel = new Label("Click items to add them to the battlefield");
        addStatusLabel.getStyleClass().add("label-status");
        HBox.setHgrow(addStatusLabel, Priority.ALWAYS);
        
        Button collapseBtn = new Button("▼ Close");
        collapseBtn.getStyleClass().add("button");
        collapseBtn.setOnAction(e -> toggleAddObjectsPanel());
        
        header.getChildren().addAll(addStatusLabel, collapseBtn);
        
        // Tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefHeight(250);

        Tab partyTab = new Tab("Party");
        partyTab.setContent(createPartyPanel());
        tabPane.getTabs().add(partyTab);

        Tab enemiesTab = new Tab("Enemies");
        enemiesTab.setContent(createEnemiesPanel());
        tabPane.getTabs().add(enemiesTab);

        Tab terrainTab = new Tab("Terrain");
        terrainTab.setContent(createTerrainPanel());
        tabPane.getTabs().add(terrainTab);

        Tab pickupsTab = new Tab("Pickups");
        pickupsTab.setContent(createPickupsPanel());
        tabPane.getTabs().add(pickupsTab);

        panel.getChildren().addAll(header, tabPane);
        return panel;
    }
    
    private void toggleAddObjectsPanel() {
        if (panelExpanded) {
            // Collapse
            addObjectsPanel.setMaxHeight(0);
            addObjectsPanel.setMinHeight(0);
            addObjectsPanel.setPrefHeight(0);
            addObjectsPanel.setVisible(false);
            addObjBtn.setText("▲ Add Objects");
            panelExpanded = false;
        } else {
            // Expand
            addObjectsPanel.setVisible(true);
            addObjectsPanel.setMaxHeight(300);
            addObjectsPanel.setMinHeight(300);
            addObjectsPanel.setPrefHeight(300);
            addObjBtn.setText("▼ Add Objects");
            panelExpanded = true;
            refreshAddObjectsPanels();
        }
    }
    
    private void refreshAddObjectsPanels() {
        // Refresh the panel content when opened
        if (addObjectsPanel.getChildren().size() > 1) {
            TabPane tabPane = (TabPane) addObjectsPanel.getChildren().get(1);
            tabPane.getTabs().get(0).setContent(createPartyPanel());
            tabPane.getTabs().get(1).setContent(createEnemiesPanel());
            tabPane.getTabs().get(2).setContent(createTerrainPanel());
            tabPane.getTabs().get(3).setContent(createPickupsPanel());
        }
    }

    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15, 10, 10, 10));
        panel.setStyle("-fx-border-color: #505052; -fx-border-width: 1 0 0 0;");

        Button backBtn = new Button("Back to Sheets");
        backBtn.getStyleClass().add("button");
        backBtn.setPrefSize(140, 40);
        backBtn.setOnAction(e -> handleBack());

        addObjBtn = new Button("▲ Add Objects");
        addObjBtn.getStyleClass().add("button");
        addObjBtn.setPrefSize(140, 40);
        addObjBtn.setOnAction(e -> toggleAddObjectsPanel());

        beginBattleBtn = new Button("Begin Battle");
        beginBattleBtn.getStyleClass().add("button-primary");
        beginBattleBtn.setPrefSize(140, 40);
        beginBattleBtn.setOnAction(e -> handleBeginBattle());

        nextTurnBtn = new Button("Next Turn");
        nextTurnBtn.getStyleClass().add("button");
        nextTurnBtn.setPrefSize(140, 40);
        nextTurnBtn.setOnAction(e -> handleNextTurn());
        nextTurnBtn.setDisable(true);

        Button endBattleBtn = new Button("End Battle");
        endBattleBtn.getStyleClass().add("button-danger");
        endBattleBtn.setPrefSize(140, 40);
        endBattleBtn.setOnAction(e -> handleEndBattle());

        panel.getChildren().addAll(backBtn, addObjBtn, beginBattleBtn, nextTurnBtn, endBattleBtn);
        return panel;
    }

    private void handleBeginBattle() {
        if (grid.getEntities().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Entities",
                    "Please add at least one entity before beginning battle.");
            return;
        }

        battleStarted = true;
        turnManager.setBattleStarted(true);
        turnManager.calculateInitiativeOrder();

        timelinePane.setVisible(true);
        beginBattleBtn.setDisable(true);
        nextTurnBtn.setDisable(false);

        gridCanvas.setBattleStarted(true);
        timelinePane.refresh();
        gridCanvas.redraw();
    }

    private void handleNextTurn() {
        turnManager.nextTurn();
        timelinePane.refresh();
        gridCanvas.redraw();
    }

    public boolean isBattleStarted() {
        return battleStarted;
    }

    public void addEntity(Entity entity) {
        turnManager.addEntity(entity);
        if (battleStarted) {
            timelinePane.refresh();
        }
    }

    public void removeEntity(Entity entity) {
        turnManager.removeEntity(entity);
        if (battleStarted) {
            timelinePane.refresh();
        }
    }

    public void addEnemy(Enemy enemy) {
        turnManager.addEnemy(enemy);
        if (battleStarted) {
            timelinePane.refresh();
        }
    }

    public void removeEnemy(Enemy enemy) {
        turnManager.removeEnemy(enemy);
        if (battleStarted) {
            timelinePane.refresh();
        }
    }

    private void handleBack() {
        stage.hide();
        sheetView.show();
    }

    private void handleEndBattle() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm End Battle");
        confirm.setHeaderText(null);
        confirm.setContentText("End battle? Party entity states will be saved.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (Entity entity : grid.getEntities()) {
                if (entity.isParty()) {
                    entity.getCharSheet().save();
                }
            }
            sheetView.endBattle();
            stage.close();
            sheetView.show();
        }
    }

    private ScrollPane createPartyPanel() {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("panel");

        ArrayList<SheetButton> sheets = sheetView.getSheets();
        boolean hasParty = false;

        for (SheetButton sheetBtn : sheets) {
            if (sheetBtn.getSheet().getCharSheet().getParty()) {
                hasParty = true;
                CharSheet cs = sheetBtn.getSheet().getCharSheet();
                String btnText = String.format("%s  |  HP: %d/%d  |  Class: %s",
                        sheetBtn.getText(), cs.getCurrentHP(), cs.getTotalHP(),
                        cs.getCharacterClass() != null ? cs.getCharacterClass() : "None");

                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> {
                    CharSheet charSheet = sheetBtn.getSheet().getCharSheet();
                    Entity newEntity = new Entity(0, 0, charSheet);
                    grid.addEntityAtNextAvailable(newEntity);
                    addEntity(newEntity);
                    gridCanvas.redraw();
                    addStatusLabel.setText("Added: " + sheetBtn.getText());
                });
                content.getChildren().add(btn);
            }
        }

        if (!hasParty) {
            Label empty = new Label("No party characters available");
            empty.setStyle("-fx-text-fill: #808080;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private ScrollPane createEnemiesPanel() {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("panel");

        String[] enemyNames = Enemy.listSavedEnemies();
        boolean hasEnemies = enemyNames.length > 0;

        for (String enemyName : enemyNames) {
            Enemy template = Enemy.load(enemyName);
            if (template != null) {
                String btnText = String.format("%s  |  HP: %d  |  MOB: %d  |  ATK: %d",
                        template.getName(), template.getMaxHealth(), 
                        template.getMovement(), template.getAttackPower());

                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> {
                    Enemy newEnemy = Enemy.load(enemyName);
                    if (newEnemy != null) {
                        int instanceNum = enemyInstanceCounts.getOrDefault(enemyName, 0) + 1;
                        enemyInstanceCounts.put(enemyName, instanceNum);
                        newEnemy.setInstanceNumber(instanceNum);
                        grid.addEnemyAtNextAvailable(newEnemy);
                        addEnemy(newEnemy);
                        gridCanvas.redraw();
                        addStatusLabel.setText("Added: " + enemyName + " #" + instanceNum);
                    }
                });
                content.getChildren().add(btn);
            }
        }

        if (!hasEnemies) {
            Label empty = new Label("No enemies available");
            empty.setStyle("-fx-text-fill: #808080;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private ScrollPane createTerrainPanel() {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("panel");

        List<TerrainObject> availableTerrains = UI.TerrainDatabase.getInstance().getAllTerrains();

        if (availableTerrains.isEmpty()) {
            Label empty = new Label("No terrain objects available");
            empty.setStyle("-fx-text-fill: #808080;");
            content.getChildren().add(empty);
        } else {
            for (TerrainObject terrain : availableTerrains) {
                String btnText = String.format("%s  |  HP: %d", terrain.getType(), terrain.getHealth());
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> {
                    TerrainObject newTerrain = new TerrainObject(0, 0, terrain.getType(), terrain.getHealth());
                    grid.addTerrainAtNextAvailable(newTerrain);
                    gridCanvas.redraw();
                    addStatusLabel.setText("Added terrain: " + terrain.getType());
                });
                content.getChildren().add(btn);
            }
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private ScrollPane createPickupsPanel() {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("panel");

        Map<String, Weapon> weapons = ItemDatabase.getInstance().getAllWeapons();
        Map<String, Armor> armors = ItemDatabase.getInstance().getAllArmors();
        Map<String, Consumable> consumables = ItemDatabase.getInstance().getAllConsumables();

        if (!weapons.isEmpty()) {
            Label header = new Label("Weapons");
            header.getStyleClass().add("label-header");
            content.getChildren().add(header);

            for (Weapon weapon : weapons.values()) {
                String btnText = String.format("%s  |  DMG: %d", weapon.getName(), weapon.getDamage());
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> {
                    Pickup itemPickup = new Pickup(0, 0, weapon);
                    grid.addPickupAtNextAvailable(itemPickup);
                    gridCanvas.redraw();
                    addStatusLabel.setText("Added pickup: " + weapon.getName());
                });
                content.getChildren().add(btn);
            }
        }

        if (!armors.isEmpty()) {
            Label header = new Label("Armor");
            header.getStyleClass().add("label-header");
            content.getChildren().add(header);

            String[] armorSlotNames = {"Head", "Torso", "Legs"};
            for (Armor armor : armors.values()) {
                int slotIndex = armor.getArmorType();
                String slotName = (slotIndex >= 0 && slotIndex < armorSlotNames.length) ? armorSlotNames[slotIndex] : "?";
                String btnText = String.format("%s  |  DEF: %d  |  Slot: %s",
                        armor.getName(), armor.getDefense(), slotName);
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> {
                    Pickup itemPickup = new Pickup(0, 0, armor);
                    grid.addPickupAtNextAvailable(itemPickup);
                    gridCanvas.redraw();
                    addStatusLabel.setText("Added pickup: " + armor.getName());
                });
                content.getChildren().add(btn);
            }
        }

        if (!consumables.isEmpty()) {
            Label header = new Label("Consumables");
            header.getStyleClass().add("label-header");
            content.getChildren().add(header);

            for (Consumable consumable : consumables.values()) {
                Status effect = consumable.getEffect();
                String effectName = (effect != null) ? effect.getName() : "Heal";
                String btnText = String.format("%s  |  %s: %d",
                        consumable.getName(), effectName, consumable.getHealAmount());
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> {
                    Pickup itemPickup = new Pickup(0, 0, consumable);
                    grid.addPickupAtNextAvailable(itemPickup);
                    gridCanvas.redraw();
                    addStatusLabel.setText("Added pickup: " + consumable.getName());
                });
                content.getChildren().add(btn);
            }
        }

        if (weapons.isEmpty() && armors.isEmpty() && consumables.isEmpty()) {
            Label empty = new Label("No pickup items available");
            empty.setStyle("-fx-text-fill: #808080;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
    }

    public void show() {
        stage.show();
    }

    public void setBattleVisible(boolean visible) {
        if (visible) {
            stage.show();
            gridCanvas.redraw();
        } else {
            stage.hide();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
