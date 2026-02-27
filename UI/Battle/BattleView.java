package UI.Battle;

import EntityRes.*;
import Objects.*;
import UI.AnimationUtils;
import UI.CardUtils;
import UI.CharacterSheetView;
import UI.IconUtils;
import UI.NotificationPane;
import UI.ResponsiveUtils;
import UI.SheetButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private final NotificationPane notificationPane;
    private final CombatLogPane combatLogPane;
    private final PartyHealthPane partyHealthPane;
    private final BattleState battleState;
    private Button nextTurnBtn;
    private Button beginBattleBtn;
    private Button addObjBtn;
    private Button endBattleBtn;
    private final Map<String, Integer> enemyInstanceCounts = new HashMap<>();
    
    // Pull-up panel
    private VBox addObjectsPanel;
    private boolean panelExpanded = false;
    private Label addStatusLabel;

    public BattleView(int rows, int cols, CharacterSheetView sheetView) {
        this.stage = new Stage();
        this.sheetView = sheetView;
        this.battleState = new BattleState();

        List<Entity> entities = new ArrayList<>();
        List<TerrainObject> terrainObjects = new ArrayList<>();
        List<Pickup> pickups = new ArrayList<>();

        grid = new BattleGrid(rows, cols, entities, terrainObjects, pickups);
        turnManager = new TurnManager(entities);
        turnManager.setBattleStarted(false);

        // Create notification system
        notificationPane = new NotificationPane();
        combatLogPane = new CombatLogPane();
        partyHealthPane = new PartyHealthPane();

        gridCanvas = new BattleGridCanvas(grid, turnManager, this, notificationPane, combatLogPane);
        timelinePane = new TimelinePane(turnManager);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.getStyleClass().add("panel-dark");

        // Timeline at top (hidden initially)
        timelinePane.setVisible(false);
        root.setTop(timelinePane);

        // Grid in center with party health on left and combat log on right
        BorderPane centerArea = new BorderPane();
        centerArea.setLeft(partyHealthPane);
        centerArea.setCenter(gridCanvas);
        centerArea.setRight(combatLogPane);
        BorderPane.setMargin(partyHealthPane, new Insets(0, 10, 0, 0));
        BorderPane.setMargin(combatLogPane, new Insets(0, 0, 0, 10));
        root.setCenter(centerArea);

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

        // Wrap in StackPane to overlay notifications
        StackPane rootStack = new StackPane(root, notificationPane);
        StackPane.setAlignment(notificationPane, Pos.TOP_CENTER);

        Scene scene = new Scene(rootStack, 1100, 750);
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
        
        Button collapseBtn = new Button("Close");
        collapseBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.CLOSE));
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
            // Collapse with animation
            panelExpanded = false;
            AnimationUtils.animateHeight(addObjectsPanel, 0, () -> addObjectsPanel.setVisible(false));
        } else {
            // Expand with animation
            addObjectsPanel.setVisible(true);
            panelExpanded = true;
            refreshAddObjectsPanels();
            AnimationUtils.animateHeight(addObjectsPanel, 300, null);
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

        Button backBtn = new Button("Back");
        backBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.BACK));
        backBtn.getStyleClass().add("button");
        backBtn.setPrefSize(120, 40);
        backBtn.setOnAction(e -> handleBack());

        addObjBtn = new Button("Add Objects");
        addObjBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.PLUS));
        addObjBtn.getStyleClass().add("button");
        addObjBtn.setPrefSize(140, 40);
        addObjBtn.setOnAction(e -> toggleAddObjectsPanel());
        // Disable add objects during battle
        addObjBtn.disableProperty().bind(battleState.battleStartedProperty());

        beginBattleBtn = new Button("Begin Battle");
        beginBattleBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.PLAY));
        beginBattleBtn.getStyleClass().add("button-primary");
        beginBattleBtn.setPrefSize(150, 40);
        beginBattleBtn.setOnAction(e -> handleBeginBattle());
        // Disable begin battle when already started
        beginBattleBtn.disableProperty().bind(battleState.battleStartedProperty());

        nextTurnBtn = new Button("Next Turn");
        nextTurnBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.FLAG));
        nextTurnBtn.getStyleClass().add("button");
        nextTurnBtn.setPrefSize(140, 40);
        nextTurnBtn.setOnAction(e -> handleNextTurn());
        // Enable next turn only when battle is active
        nextTurnBtn.disableProperty().bind(battleState.battleStartedProperty().not());

        endBattleBtn = new Button("End Battle");
        endBattleBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.STOP));
        endBattleBtn.getStyleClass().add("button-danger");
        endBattleBtn.setPrefSize(140, 40);
        endBattleBtn.setOnAction(e -> handleEndBattle());
        // Enable end battle only when battle is active
        endBattleBtn.disableProperty().bind(battleState.battleStartedProperty().not());

        panel.getChildren().addAll(backBtn, addObjBtn, beginBattleBtn, nextTurnBtn, endBattleBtn);
        return panel;
    }

    private void handleBeginBattle() {
        if (grid.getEntities().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Entities",
                    "Please add at least one entity before beginning battle.");
            return;
        }

        battleState.setBattleStarted(true);
        turnManager.setBattleStarted(true);
        turnManager.calculateInitiativeOrder();

        timelinePane.setVisible(true);
        combatLogPane.clear();
        combatLogPane.logRound(1);
        
        // Log first turn
        if (!turnManager.getTurnOrder().isEmpty()) {
            GridObject first = turnManager.getTurnOrder().get(0);
            String firstName = (first instanceof Entity e) ? e.getName() : 
                              (first instanceof Enemy en) ? en.getName() : "Unknown";
            combatLogPane.logTurnStart(firstName);
        }
        
        beginBattleBtn.setDisable(true);
        nextTurnBtn.setDisable(false);

        gridCanvas.setBattleStarted(true);
        timelinePane.refresh();
        gridCanvas.redraw();
        
        notificationPane.showToast("Battle started! Round 1", NotificationPane.ToastType.INFO);
    }

    private void handleNextTurn() {
        int prevRound = turnManager.getRound();
        turnManager.nextTurn();
        int newRound = turnManager.getRound();
        
        // Log new round if round changed
        if (newRound > prevRound) {
            combatLogPane.logRound(newRound);
        }
        
        // Log whose turn it is
        if (!turnManager.getTurnOrder().isEmpty()) {
            int idx = turnManager.getCurrentIndex();
            GridObject current = turnManager.getTurnOrder().get(idx);
            String name = (current instanceof Entity e) ? e.getName() : 
                         (current instanceof Enemy en) ? en.getName() : "Unknown";
            combatLogPane.logTurnStart(name);
        }
        
        timelinePane.refresh();
        gridCanvas.redraw();
    }

    public boolean isBattleStarted() {
        return battleState.isBattleStarted();
    }
    
    public BattleState getBattleState() {
        return battleState;
    }

    public void addEntity(Entity entity) {
        turnManager.addEntity(entity);
        partyHealthPane.updateParty(grid.getEntities());
        if (battleState.isBattleStarted()) {
            timelinePane.refresh();
        }
    }

    public void removeEntity(Entity entity) {
        turnManager.removeEntity(entity);
        partyHealthPane.updateParty(grid.getEntities());
        if (battleState.isBattleStarted()) {
            timelinePane.refresh();
        }
    }

    public void addEnemy(Enemy enemy) {
        turnManager.addEnemy(enemy);
        if (battleState.isBattleStarted()) {
            timelinePane.refresh();
        }
    }

    public void removeEnemy(Enemy enemy) {
        turnManager.removeEnemy(enemy);
        if (battleState.isBattleStarted()) {
            timelinePane.refresh();
        }
    }

    /**
     * Refreshes the party health display. Call after combat actions.
     */
    public void refreshPartyHealth() {
        partyHealthPane.refresh(grid.getEntities());
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
            battleState.setBattleStarted(false);
            battleState.setBattleEnded(true);
            
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
        VBox content = new VBox(8);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("panel");

        ArrayList<SheetButton> sheets = sheetView.getSheets();
        boolean hasParty = false;

        for (SheetButton sheetBtn : sheets) {
            if (sheetBtn.getSheet().getCharSheet().getParty()) {
                hasParty = true;
                CharSheet cs = sheetBtn.getSheet().getCharSheet();
                
                // Create a card-style button for party members
                HBox card = createEntitySelectionCard(cs, true);
                card.setOnMouseClicked(e -> {
                    CharSheet charSheet = sheetBtn.getSheet().getCharSheet();
                    Entity newEntity = new Entity(0, 0, charSheet);
                    grid.addEntityAtNextAvailable(newEntity);
                    addEntity(newEntity);
                    gridCanvas.redraw();
                    addStatusLabel.setText("Added: " + sheetBtn.getText());
                });
                content.getChildren().add(card);
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
    
    private HBox createEntitySelectionCard(CharSheet cs, boolean isParty) {
        CardUtils.CardStyle style = isParty ? CardUtils.CardStyle.PARTY : CardUtils.CardStyle.ENEMY;
        
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle(String.format(
            "-fx-background-color: linear-gradient(to right, %s, %s); " +
            "-fx-background-radius: 6; -fx-border-color: %s; -fx-border-radius: 6; -fx-border-width: 1;",
            style.bgColor, adjustBrightness(style.bgColor, -10), style.borderColor
        ));
        
        // Icon
        Node avatar = IconUtils.createIcon(isParty ? IconUtils.Icon.PERSON : IconUtils.Icon.SKULL, 24, style.accentColor);
        
        // Info section
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label nameLabel = new Label(cs.getName());
        nameLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13px; -fx-font-weight: bold;", style.accentColor));
        
        String classText = cs.getCharacterClass() != null ? cs.getCharacterClass() : "Unknown";
        Label classLabel = new Label(classText);
        classLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        
        info.getChildren().addAll(nameLabel, classLabel);
        
        // HP section
        VBox hpSection = new VBox(2);
        hpSection.setAlignment(Pos.CENTER_RIGHT);
        hpSection.setMinWidth(70);
        
        Label hpLabel = new Label(cs.getCurrentHP() + " / " + cs.getTotalHP());
        hpLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        
        ProgressBar miniHp = new ProgressBar((double) cs.getCurrentHP() / cs.getTotalHP());
        miniHp.setPrefWidth(70);
        miniHp.setPrefHeight(6);
        styleHpBar(miniHp, cs.getCurrentHP(), cs.getTotalHP());
        
        hpSection.getChildren().addAll(hpLabel, miniHp);
        
        card.getChildren().addAll(avatar, info, hpSection);
        
        // Hover effect
        final String normalStyle = card.getStyle();
        card.setOnMouseEntered(e -> {
            card.setStyle(normalStyle.replace(style.borderColor, style.accentColor));
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(normalStyle);
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        return card;
    }
    
    private void styleHpBar(ProgressBar bar, int current, int max) {
        double ratio = (double) current / max;
        String color = ratio > 0.5 ? "#4CAF50" : ratio > 0.25 ? "#FF9800" : "#F44336";
        bar.setStyle("-fx-accent: " + color + ";");
    }
    
    private String adjustBrightness(String hexColor, int amount) {
        String hex = hexColor.replace("#", "");
        int r = Math.max(0, Math.min(255, Integer.parseInt(hex.substring(0, 2), 16) + amount));
        int g = Math.max(0, Math.min(255, Integer.parseInt(hex.substring(2, 4), 16) + amount));
        int b = Math.max(0, Math.min(255, Integer.parseInt(hex.substring(4, 6), 16) + amount));
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private ScrollPane createEnemiesPanel() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("panel");

        String[] enemyNames = Enemy.listSavedEnemies();
        boolean hasEnemies = enemyNames.length > 0;

        for (String enemyName : enemyNames) {
            Enemy template = Enemy.load(enemyName);
            if (template != null) {
                HBox card = createEnemySelectionCard(template);
                final String name = enemyName;
                card.setOnMouseClicked(e -> {
                    Enemy newEnemy = Enemy.load(name);
                    if (newEnemy != null) {
                        int instanceNum = enemyInstanceCounts.getOrDefault(name, 0) + 1;
                        enemyInstanceCounts.put(name, instanceNum);
                        newEnemy.setInstanceNumber(instanceNum);
                        grid.addEnemyAtNextAvailable(newEnemy);
                        addEnemy(newEnemy);
                        gridCanvas.redraw();
                        addStatusLabel.setText("Added: " + name + " #" + instanceNum);
                    }
                });
                content.getChildren().add(card);
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
    
    private HBox createEnemySelectionCard(Enemy enemy) {
        CardUtils.CardStyle style = CardUtils.CardStyle.ENEMY;
        
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle(String.format(
            "-fx-background-color: linear-gradient(to right, %s, %s); " +
            "-fx-background-radius: 6; -fx-border-color: %s; -fx-border-radius: 6; -fx-border-width: 1;",
            style.bgColor, adjustBrightness(style.bgColor, -10), style.borderColor
        ));
        
        // Icon
        Node avatar = IconUtils.createIcon(IconUtils.Icon.SKULL, 24, style.accentColor);
        
        // Info section
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label nameLabel = new Label(enemy.getName());
        nameLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13px; -fx-font-weight: bold;", style.accentColor));
        
        Label statsLabel = new Label(String.format("ATK: %d  •  MOB: %d", enemy.getAttackPower(), enemy.getMovement()));
        statsLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        
        info.getChildren().addAll(nameLabel, statsLabel);
        
        // HP section
        VBox hpSection = new VBox(2);
        hpSection.setAlignment(Pos.CENTER_RIGHT);
        hpSection.setMinWidth(60);
        
        HBox hpRow = new HBox(4);
        hpRow.setAlignment(Pos.CENTER_RIGHT);
        hpRow.getChildren().addAll(
            IconUtils.createIcon(IconUtils.Icon.HEART, 12, "#F44336"),
            new Label(String.valueOf(enemy.getMaxHealth())) {{
                setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            }}
        );
        
        hpSection.getChildren().add(hpRow);
        
        card.getChildren().addAll(avatar, info, hpSection);
        
        // Hover effect
        final String normalStyle = card.getStyle();
        card.setOnMouseEntered(e -> {
            card.setStyle(normalStyle.replace(style.borderColor, style.accentColor));
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(normalStyle);
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        return card;
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
