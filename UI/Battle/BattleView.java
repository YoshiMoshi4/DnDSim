package UI.Battle;

import EntityRes.*;
import Objects.*;
import UI.AnimationUtils;
import UI.AppController;
import UI.CardUtils;
import UI.CharacterSheetView;
import UI.IconUtils;
import UI.SheetButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.Supplier;

public class BattleView {

    private final AppController appController;
    private final StackPane rootWrapper;
    private final BorderPane root;
    private final BattleGrid grid;
    private final TurnManager turnManager;
    private final CharacterSheetView sheetView;
    private final BattleGridCanvas gridCanvas;
    private final CombatLogPane combatLogPane;
    private final PartyHealthPane partyHealthPane;
    private final TimelinePane timelinePane;
    private final BattleState battleState;
    private GridObject selectedEntity;
    private Button nextTurnBtn;
    private Button battleToggleBtn;
    private Button addObjBtn;
    private VBox actionPanel;
    private Button moveBtn;
    private Button attackBtn;
    private Button useItemBtn;
    private Button swapBtn;
    private Button pickupBtn;
    private ProgressBar healthBar;
    private Label healthTextLabel;
    // Stat rows that only apply to party members (Entities have all 6 stats; Enemies only have Dexterity)
    private HBox strRow;
    private HBox conRow;
    private HBox intRow;
    private HBox wisRow;
    private HBox chaRow;
    private final Map<String, Integer> enemyInstanceCounts = new HashMap<>();
    
    // Dice roll panel for combat
    private DiceRollPanel diceRollPanel;
    
    // Pull-up panel
    private VBox addObjectsPanel;
    private boolean panelExpanded = false;
    private Label addStatusLabel;
    
    // Surprise round toggle
    private CheckBox surpriseRoundCheckbox;
    
    // Placement mode (party)
    private boolean placementMode = false;
    private final Queue<CharSheet> partyToPlace = new LinkedList<>();
    private CharSheet currentPlacing = null;
    private javafx.beans.property.BooleanProperty placementModeProperty = new javafx.beans.property.SimpleBooleanProperty(false);
    
    // Object placement mode (enemies, terrain, pickups)
    private boolean objectPlacementMode = false;
    private Supplier<Object> pendingObjectSupplier = null;
    private String pendingObjectKey = null;
    private String pendingObjectName = null;

    public BattleView(int rows, int cols, CharacterSheetView sheetView, AppController appController) {
        this.appController = appController;
        this.sheetView = sheetView;
        this.battleState = new BattleState();

        List<Entity> entities = new ArrayList<>();
        List<TerrainObject> terrainObjects = new ArrayList<>();
        List<Pickup> pickups = new ArrayList<>();

        grid = new BattleGrid(rows, cols, entities, terrainObjects, pickups);
        turnManager = new TurnManager(entities);
        turnManager.setBattleStarted(false);
        
        // Set up tie resolution handler for new rounds
        turnManager.setTieResolutionHandler(this::handleTieResolutions);

        // Create UI components
        combatLogPane = new CombatLogPane();
        partyHealthPane = new PartyHealthPane();
        timelinePane = new TimelinePane(turnManager);
        timelinePane.setVisible(false);

        gridCanvas = new BattleGridCanvas(grid, turnManager, this, combatLogPane);

        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10));
        mainPane.getStyleClass().add("panel-dark");

        // Top: Timeline pane showing turn order
        mainPane.setTop(timelinePane);
        BorderPane.setMargin(timelinePane, new Insets(0, 0, 10, 0));

        // Left side: Party health stacked with combat log
        VBox leftPanel = new VBox(10);
        leftPanel.getChildren().addAll(partyHealthPane, combatLogPane);
        VBox.setVgrow(combatLogPane, Priority.ALWAYS);
        
        // Right side: Action panel and dice roll panel (swappable)
        actionPanel = createActionPanel();
        diceRollPanel = new DiceRollPanel();
        diceRollPanel.setVisible(false);
        diceRollPanel.setManaged(false);
        
        StackPane rightPanel = new StackPane();
        rightPanel.getChildren().addAll(actionPanel, diceRollPanel);
        rightPanel.setAlignment(Pos.TOP_CENTER);

        // Grid in center with left panel and action panel
        BorderPane centerArea = new BorderPane();
        centerArea.setLeft(leftPanel);
        centerArea.setCenter(gridCanvas);
        centerArea.setRight(rightPanel);
        BorderPane.setMargin(leftPanel, new Insets(0, 10, 0, 0));
        BorderPane.setMargin(rightPanel, new Insets(0, 0, 0, 10));
        mainPane.setCenter(centerArea);

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
        mainPane.setBottom(bottomArea);

        root = mainPane;
        rootWrapper = new StackPane(root);

        sheetView.setBattleView(this);
    }

    public StackPane getRoot() {
        return rootWrapper;
    }
    
    // ================== Placement Mode Methods ==================
    
    /**
     * Start the party placement phase, loading party members from PartyConfig.
     */
    public void startPlacementPhase() {
        List<String> partyNames = PartyConfig.getMembers();
        if (partyNames.isEmpty()) {
            combatLogPane.log("No party configured. Use Add Objects to add characters.", CombatLogPane.LogType.INFO);
            return;
        }
        
        partyToPlace.clear();
        
        // Load CharSheets for each party member
        for (String name : partyNames) {
            for (SheetButton sheetBtn : sheetView.getSheets()) {
                if (sheetBtn.getSheet().getCharSheet().getName().equals(name)) {
                    partyToPlace.add(sheetBtn.getSheet().getCharSheet());
                    break;
                }
            }
        }
        
        if (partyToPlace.isEmpty()) {
            combatLogPane.log("No valid party members found.", CombatLogPane.LogType.INFO);
            return;
        }
        
        placementMode = true;
        placementModeProperty.set(true);
        
        // Log placement start
        combatLogPane.log("=== Party Placement ===", CombatLogPane.LogType.ROUND);
        
        // Start with first party member
        advancePlacement();
        gridCanvas.redraw();
    }
    
    /**
     * Advance to the next party member to place.
     */
    private void advancePlacement() {
        if (partyToPlace.isEmpty()) {
            // Placement complete
            endPlacementPhase();
            return;
        }
        
        currentPlacing = partyToPlace.poll();
        combatLogPane.log("Place " + currentPlacing.getName() + " - click on a tile", CombatLogPane.LogType.INFO);
    }
    
    /**
     * End the placement phase and enable normal controls.
     */
    private void endPlacementPhase() {
        placementMode = false;
        placementModeProperty.set(false);
        currentPlacing = null;
        
        combatLogPane.log("All party members placed!", CombatLogPane.LogType.INFO);
        partyHealthPane.updateParty(grid.getEntities());
        gridCanvas.redraw();
    }
    
    /**
     * Called when a party member is placed on the grid during placement mode.
     */
    public void entityPlaced(Entity entity) {
        grid.addEntity(entity);
        addEntity(entity);
        advancePlacement();
        gridCanvas.redraw();
    }
    
    /**
     * Check if currently in placement mode.
     */
    public boolean isPlacementMode() {
        return placementMode;
    }
    
    /**
     * Get the currently placing CharSheet.
     */
    public CharSheet getCurrentPlacing() {
        return currentPlacing;
    }
    
    /**
     * Check if in object placement mode.
     */
    public boolean isObjectPlacementMode() {
        return objectPlacementMode;
    }
    
    /**
     * Check whether a specific add-objects menu item is currently selected for placement.
     */
    public boolean isPlacementSelectionActive(String key) {
        return objectPlacementMode && key != null && key.equals(pendingObjectKey);
    }
    
    /**
     * Start object placement mode with the given object.
     */
    public void startObjectPlacement(Supplier<Object> supplier, String key, String name) {
        // Clicking the same menu item toggles placement selection off.
        if (objectPlacementMode && key != null && key.equals(pendingObjectKey)) {
            cancelObjectPlacement();
            addStatusLabel.setText("Deselected: " + name);
            return;
        }

        objectPlacementMode = true;
        pendingObjectSupplier = supplier;
        pendingObjectKey = key;
        pendingObjectName = name;
        addStatusLabel.setText("Selected: " + name + " (click grid to place, click same item again to deselect)");
        refreshAddObjectsPanels();
        gridCanvas.redraw();
    }
    
    /**
     * Called when an object is placed on the grid.
     */
    public void objectPlaced(int row, int col) {
        if (pendingObjectSupplier == null) {
            return;
        }

        Object pendingObject = pendingObjectSupplier.get();
        if (pendingObject == null) {
            addStatusLabel.setText("Failed to create object for placement");
            cancelObjectPlacement();
            return;
        }

        if (pendingObject instanceof Entity entity) {
            entity.setRow(row);
            entity.setCol(col);
            grid.addEntity(entity);
            addEntity(entity);
            addStatusLabel.setText("Placed: " + entity.getName());
            // Party members are unique - stop the paint tool after one placement
            objectPlacementMode = false;
            pendingObjectSupplier = null;
            pendingObjectKey = null;
            pendingObjectName = null;
            refreshAddObjectsPanels();
        } else if (pendingObject instanceof Enemy enemy) {
            enemy.setRow(row);
            enemy.setCol(col);
            grid.addEnemy(enemy);
            addEnemy(enemy);
            addStatusLabel.setText("Placed: " + enemy.getName() + " (still placing " + pendingObjectName + ")");
        } else if (pendingObject instanceof TerrainObject terrain) {
            terrain.setRow(row);
            terrain.setCol(col);
            grid.addTerrain(terrain);
            addStatusLabel.setText("Placed: " + terrain.getType() + " (still placing " + pendingObjectName + ")");
        } else if (pendingObject instanceof Pickup pickup) {
            pickup.setRow(row);
            pickup.setCol(col);
            grid.addPickup(pickup);
            addStatusLabel.setText("Placed: " + pickup.getItem().getName() + " (still placing " + pendingObjectName + ")");
        }
        gridCanvas.redraw();
    }
    
    /**
     * Cancel object placement mode.
     */
    public void cancelObjectPlacement() {
        objectPlacementMode = false;
        pendingObjectSupplier = null;
        pendingObjectKey = null;
        pendingObjectName = null;
        addStatusLabel.setText("Placement cancelled");
        refreshAddObjectsPanels();
        gridCanvas.redraw();
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
            if (objectPlacementMode) {
                cancelObjectPlacement();
            }
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

    private VBox createActionPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(185);
        panel.setMinWidth(185);
        panel.getStyleClass().add("panel");
        panel.setStyle("-fx-background-color: #2d2d30; -fx-border-color: #505052; -fx-border-width: 0 0 0 1;");

        // Selected entity indicator
        Label selectLabel = new Label("Selected");
        selectLabel.getStyleClass().add("label-title");
        selectLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #daa520;");

        Label selectedEntityLabel = new Label("--");
        selectedEntityLabel.setId("selectedEntityLabel");
        selectedEntityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e0e0e0; -fx-font-weight: bold;");
        selectedEntityLabel.setWrapText(true);

        // Health section with progress bar and adjustment buttons
        Label healthLabel = new Label("Health");
        healthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #808080;");

        HBox healthRow = new HBox(5);
        healthRow.setAlignment(Pos.CENTER_LEFT);

        Button healthMinusBtn = new Button("-");
        healthMinusBtn.setStyle("-fx-min-width: 25px; -fx-min-height: 20px; -fx-font-size: 12px;");
        healthMinusBtn.setOnAction(e -> adjustHealth(-1));

        StackPane healthPane = new StackPane();
        healthPane.setAlignment(Pos.CENTER);

        healthBar = new ProgressBar(0);
        healthBar.setPrefWidth(100);
        healthBar.setPrefHeight(20);
        healthBar.setStyle("-fx-accent: #F44336;");

        healthTextLabel = new Label("-- / --");
        healthTextLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold;");

        healthPane.getChildren().addAll(healthBar, healthTextLabel);

        Button healthPlusBtn = new Button("+");
        healthPlusBtn.setStyle("-fx-min-width: 25px; -fx-min-height: 20px; -fx-font-size: 12px;");
        healthPlusBtn.setOnAction(e -> adjustHealth(1));

        healthRow.getChildren().addAll(healthMinusBtn, healthPane, healthPlusBtn);

        // Stats section - AC + all 6 basic stats, temporarily adjustable for this battle only
        Label statsLabel = new Label("Stats");
        statsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #808080;");

        HBox acRow = createStatRow("AC", "#FFD700", "acLabel", d -> adjustAC(d));
        HBox strRowLocal = createStatRow("STR", "#d75f5f", "strLabel", d -> adjustStat(CharSheet.STRENGTH, d));
        HBox dexRow = createStatRow("DEX", "#4CAF50", "dexLabel", d -> adjustStat(CharSheet.DEXTERITY, d));
        HBox conRowLocal = createStatRow("CON", "#2196F3", "conLabel", d -> adjustStat(CharSheet.CONSTITUTION, d));
        HBox intRowLocal = createStatRow("INT", "#FF9800", "intLabel", d -> adjustStat(CharSheet.INTELLIGENCE, d));
        HBox wisRowLocal = createStatRow("WIS", "#4ec9b0", "wisLabel", d -> adjustStat(CharSheet.WISDOM, d));
        HBox chaRowLocal = createStatRow("CHA", "#c586c0", "chaLabel", d -> adjustStat(CharSheet.CHARISMA, d));
        strRow = strRowLocal;
        conRow = conRowLocal;
        intRow = intRowLocal;
        wisRow = wisRowLocal;
        chaRow = chaRowLocal;

        Label mobLabel = new Label("MOB: --");
        mobLabel.setId("mobLabel");
        mobLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");

        // Weapon section - shows the currently equipped (primary) weapon only, with a small
        // swap button next to it to bring the secondary weapon forward instead.
        Label weapLabel = new Label("Weapon");
        weapLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #808080;");

        Label primaryLabel = new Label("--");
        primaryLabel.setId("primaryLabel");
        primaryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");
        primaryLabel.setWrapText(true);
        HBox.setHgrow(primaryLabel, Priority.ALWAYS);

        swapBtn = createSmallIconButton(IconUtils.Icon.UNDO, "Q");
        swapBtn.setDisable(true);
        swapBtn.setOnAction(e -> gridCanvas.triggerSwapForSelected());

        HBox weaponRow = new HBox(6, primaryLabel, swapBtn);
        weaponRow.setAlignment(Pos.CENTER_LEFT);

        // Damage dice section - one row per tier, temporarily cyclable through d4..d20
        Label diceLabel = new Label("Damage Dice");
        diceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #808080;");

        HBox tier1Row = createStatRow("T1", "#a0a0a0", "tier1Label", d -> adjustDice(0, d));
        HBox tier2Row = createStatRow("T2", "#a0a0a0", "tier2Label", d -> adjustDice(1, d));
        HBox tier3Row = createStatRow("T3", "#a0a0a0", "tier3Label", d -> adjustDice(2, d));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #505052;");

        Label actionsLabel = new Label("Actions");
        actionsLabel.getStyleClass().add("label-title");
        actionsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #808080;");

        // Action buttons - condensed into a numpad-style grid of square icon buttons
        moveBtn = createSquareActionButton(IconUtils.Icon.MOVE, "F");
        moveBtn.setDisable(true);
        moveBtn.setOnAction(e -> gridCanvas.startMoveMode());

        attackBtn = createSquareActionButton(IconUtils.Icon.TARGET, "E");
        attackBtn.setDisable(true);
        attackBtn.setOnAction(e -> gridCanvas.startAttackMode());

        useItemBtn = createSquareActionButton(IconUtils.Icon.POTION, "R");
        useItemBtn.setDisable(true);
        useItemBtn.setOnAction(e -> gridCanvas.triggerUseItemForSelected());

        pickupBtn = createSquareActionButton(IconUtils.Icon.POTION, "P");
        pickupBtn.setDisable(true);
        pickupBtn.setOnAction(e -> gridCanvas.startPickupMode());
        pickupBtn.setStyle("-fx-background-color: #8B4513;");

        GridPane actionsGrid = new GridPane();
        actionsGrid.setHgap(8);
        actionsGrid.setVgap(8);
        actionsGrid.setAlignment(Pos.CENTER_LEFT);
        actionsGrid.add(moveBtn, 0, 0);
        actionsGrid.add(attackBtn, 1, 0);
        actionsGrid.add(useItemBtn, 0, 1);
        actionsGrid.add(pickupBtn, 1, 1);

        // Round indicator
        Label roundLabel = new Label("Round: --");
        roundLabel.setId("roundLabel");
        roundLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        panel.getChildren().addAll(
            selectLabel, selectedEntityLabel,
            healthLabel, healthRow,
            statsLabel, acRow, strRow, dexRow, conRow, intRow, wisRow, chaRow, mobLabel,
            weapLabel, weaponRow, diceLabel, tier1Row, tier2Row, tier3Row, sep,
            actionsLabel, actionsGrid,
            roundLabel
        );

        return panel;
    }

    /**
     * Build a compact, color-coded stat row: an abbreviation label matching the character sheet's
     * stat colors, flanked by rounded spinner-style -/+ buttons around the live value.
     */
    private HBox createStatRow(String abbrev, String colorHex, String valueLabelId, java.util.function.IntConsumer onAdjust) {
        Label abbrevLabel = new Label(abbrev);
        abbrevLabel.setMinWidth(30);
        abbrevLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

        Button minusBtn = new Button("−");
        minusBtn.getStyleClass().addAll("spinner-button", "spinner-decrement");
        minusBtn.setMinSize(20, 20);
        minusBtn.setMaxSize(20, 20);
        minusBtn.setOnAction(e -> onAdjust.accept(-1));

        Label valueLabel = new Label("--");
        valueLabel.setId(valueLabelId);
        valueLabel.getStyleClass().add("spinner-value");
        valueLabel.setMinWidth(30);
        valueLabel.setAlignment(Pos.CENTER);
        valueLabel.setStyle("-fx-font-size: 12px;");

        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().addAll("spinner-button", "spinner-increment");
        plusBtn.setMinSize(20, 20);
        plusBtn.setMaxSize(20, 20);
        plusBtn.setOnAction(e -> onAdjust.accept(1));

        HBox spinnerBox = new HBox(minusBtn, valueLabel, plusBtn);
        spinnerBox.setAlignment(Pos.CENTER);

        HBox row = new HBox(6, abbrevLabel, spinnerBox);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Build a square icon button for the numpad-style actions grid, keeping the icon and the
     * keyboard-shortcut letter that used to appear in the full-width button's text label.
     */
    private Button createSquareActionButton(IconUtils.Icon icon, String shortcutLetter) {
        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        Label shortcutLabel = new Label(shortcutLetter);
        shortcutLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #909095;");
        content.getChildren().addAll(IconUtils.createIcon(icon, 20, "#dcdcdc"), shortcutLabel);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.getStyleClass().add("button");
        btn.setMinSize(56, 56);
        btn.setPrefSize(56, 56);
        btn.setMaxSize(56, 56);
        return btn;
    }

    /**
     * Build a small icon button for inline placement next to a label (e.g. the weapon swap
     * button), keeping the same icon + shortcut-letter convention as the numpad action buttons.
     */
    private Button createSmallIconButton(IconUtils.Icon icon, String shortcutLetter) {
        VBox content = new VBox(1);
        content.setAlignment(Pos.CENTER);
        Label shortcutLabel = new Label(shortcutLetter);
        shortcutLabel.setStyle("-fx-font-size: 7px; -fx-font-weight: bold; -fx-text-fill: #909095;");
        content.getChildren().addAll(IconUtils.createIcon(icon, 12, "#dcdcdc"), shortcutLabel);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.getStyleClass().add("button");
        btn.setMinSize(28, 28);
        btn.setPrefSize(28, 28);
        btn.setMaxSize(28, 28);
        return btn;
    }

    private void updateActionPanel() {
        Label roundLabel = (Label) actionPanel.lookup("#roundLabel");
        if (roundLabel != null) {
            roundLabel.setText("Round: " + turnManager.getRound());
        }
    }

    public void updateSelectedEntity(GridObject obj) {
        this.selectedEntity = obj;
        Label selectedLabel = (Label) actionPanel.lookup("#selectedEntityLabel");
        Label strLabel = (Label) actionPanel.lookup("#strLabel");
        Label dexLabel = (Label) actionPanel.lookup("#dexLabel");
        Label conLabel = (Label) actionPanel.lookup("#conLabel");
        Label intLabel = (Label) actionPanel.lookup("#intLabel");
        Label wisLabel = (Label) actionPanel.lookup("#wisLabel");
        Label chaLabel = (Label) actionPanel.lookup("#chaLabel");
        Label mobLabel = (Label) actionPanel.lookup("#mobLabel");
        Label acLabel = (Label) actionPanel.lookup("#acLabel");
        Label primaryLabel = (Label) actionPanel.lookup("#primaryLabel");
        Label tier1Label = (Label) actionPanel.lookup("#tier1Label");
        Label tier2Label = (Label) actionPanel.lookup("#tier2Label");
        Label tier3Label = (Label) actionPanel.lookup("#tier3Label");

        // Only party Entities have all 6 basic stats; Enemies only have Dexterity.
        boolean showFullStats = obj instanceof Entity;
        for (HBox row : new HBox[]{strRow, conRow, intRow, wisRow, chaRow}) {
            row.setVisible(showFullStats);
            row.setManaged(showFullStats);
        }

        if (obj == null) {
            if (selectedLabel != null) selectedLabel.setText("--");
            if (strLabel != null) strLabel.setText("--");
            if (dexLabel != null) dexLabel.setText("--");
            if (conLabel != null) conLabel.setText("--");
            if (intLabel != null) intLabel.setText("--");
            if (wisLabel != null) wisLabel.setText("--");
            if (chaLabel != null) chaLabel.setText("--");
            if (mobLabel != null) mobLabel.setText("MOB: --");
            if (acLabel != null) acLabel.setText("--");
            if (primaryLabel != null) primaryLabel.setText("--");
            if (tier1Label != null) tier1Label.setText("--");
            if (tier2Label != null) tier2Label.setText("--");
            if (tier3Label != null) tier3Label.setText("--");
            healthBar.setProgress(0);
            healthTextLabel.setText("-- / --");
            moveBtn.setDisable(true);
            attackBtn.setDisable(true);
            useItemBtn.setDisable(true);
            swapBtn.setDisable(true);
            pickupBtn.setDisable(true);
        } else if (obj instanceof Entity e) {
            if (selectedLabel != null) selectedLabel.setText(e.getName());
            styleAdjustedValue(strLabel, e.getStatAdjustment(CharSheet.STRENGTH));
            styleAdjustedValue(dexLabel, e.getStatAdjustment(CharSheet.DEXTERITY));
            styleAdjustedValue(conLabel, e.getStatAdjustment(CharSheet.CONSTITUTION));
            styleAdjustedValue(intLabel, e.getStatAdjustment(CharSheet.INTELLIGENCE));
            styleAdjustedValue(wisLabel, e.getStatAdjustment(CharSheet.WISDOM));
            styleAdjustedValue(chaLabel, e.getStatAdjustment(CharSheet.CHARISMA));
            styleAdjustedValue(acLabel, e.getAcAdjustment());
            if (strLabel != null) strLabel.setText(String.valueOf(e.getAdjustedAttribute(CharSheet.STRENGTH)));
            if (dexLabel != null) dexLabel.setText(String.valueOf(e.getAdjustedAttribute(CharSheet.DEXTERITY)));
            if (conLabel != null) conLabel.setText(String.valueOf(e.getAdjustedAttribute(CharSheet.CONSTITUTION)));
            if (intLabel != null) intLabel.setText(String.valueOf(e.getAdjustedAttribute(CharSheet.INTELLIGENCE)));
            if (wisLabel != null) wisLabel.setText(String.valueOf(e.getAdjustedAttribute(CharSheet.WISDOM)));
            if (chaLabel != null) chaLabel.setText(String.valueOf(e.getAdjustedAttribute(CharSheet.CHARISMA)));
            if (mobLabel != null) mobLabel.setText("MOB: " + e.getMovement());
            if (acLabel != null) acLabel.setText(String.valueOf(e.getAC()));

            Weapon primary = e.getCharSheet().getEquippedWeapon();
            if (primaryLabel != null) {
                primaryLabel.setText(formatWeaponWithAmmoCount(e, primary));
            }
            String[] dice = e.getDamageDice();
            String[] baseDice = e.getBaseDamageDice();
            if (tier1Label != null) tier1Label.setText(dice[0] != null ? dice[0] : "--");
            if (tier2Label != null) tier2Label.setText(dice[1] != null ? dice[1] : "--");
            if (tier3Label != null) tier3Label.setText(dice[2] != null ? dice[2] : "--");
            styleAdjustedDie(tier1Label, dice[0], baseDice[0]);
            styleAdjustedDie(tier2Label, dice[1], baseDice[1]);
            styleAdjustedDie(tier3Label, dice[2], baseDice[2]);

            // Update health bar
            int maxHP = e.getCharSheet().getTotalHP();
            int currentHP = e.getCharSheet().getCurrentHP();
            healthBar.setProgress(maxHP > 0 ? (double) currentHP / maxHP : 0);
            healthTextLabel.setText(currentHP + " / " + maxHP);

            boolean canAct = e.isParty() && battleState.isBattleStarted();
            moveBtn.setDisable(!canAct);
            attackBtn.setDisable(!canAct);
            useItemBtn.setDisable(!canAct);
            swapBtn.setDisable(!canAct);
            pickupBtn.setDisable(!canAct);
        } else if (obj instanceof Enemy en) {
            if (selectedLabel != null) selectedLabel.setText(en.getName());
            styleAdjustedValue(dexLabel, en.getDexAdjustment());
            styleAdjustedValue(acLabel, en.getAcAdjustment());
            if (dexLabel != null) dexLabel.setText(String.valueOf(en.getAdjustedDexterity()));
            if (mobLabel != null) mobLabel.setText("MOB: " + en.getMovement());
            if (acLabel != null) acLabel.setText(String.valueOf(en.getAC()));
            if (primaryLabel != null) primaryLabel.setText("ATK: " + en.getAttackModifier());
            String[] dice = en.getDamageDice();
            String[] baseDice = en.getBaseDamageDice();
            if (tier1Label != null) tier1Label.setText(dice[0] != null ? dice[0] : "--");
            if (tier2Label != null) tier2Label.setText(dice[1] != null ? dice[1] : "--");
            if (tier3Label != null) tier3Label.setText(dice[2] != null ? dice[2] : "--");
            styleAdjustedDie(tier1Label, dice[0], baseDice[0]);
            styleAdjustedDie(tier2Label, dice[1], baseDice[1]);
            styleAdjustedDie(tier3Label, dice[2], baseDice[2]);

            // Update health bar for enemy
            int maxHP = en.getMaxHealth();
            int currentHP = en.getHealth();
            healthBar.setProgress(maxHP > 0 ? (double) currentHP / maxHP : 0);
            healthTextLabel.setText(currentHP + " / " + maxHP);

            boolean canAct = battleState.isBattleStarted();
            moveBtn.setDisable(!canAct);
            attackBtn.setDisable(!canAct);
            useItemBtn.setDisable(true);
            swapBtn.setDisable(true);
            pickupBtn.setDisable(true);
        }

        // Keep focus on grid canvas for keyboard shortcuts
        if (obj != null) {
            gridCanvas.requestFocus();
        }
    }

    /**
     * Color a stat value label green if it's been temporarily boosted, red if reduced,
     * or the default white if unchanged from its base value.
     */
    private void styleAdjustedValue(Label label, int adjustment) {
        if (label == null) return;
        String color = adjustment > 0 ? "#4CAF50" : adjustment < 0 ? "#F44336" : "#ffffff";
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
    }

    /**
     * Color a damage-dice tier label green if it's been temporarily bumped up the d4..d20
     * progression, red if bumped down, or the default white if unchanged from the weapon's die.
     */
    private void styleAdjustedDie(Label label, String currentDie, String baseDie) {
        if (label == null) return;
        int diceIndexDelta = diceIndex(currentDie) - diceIndex(baseDie);
        String color = diceIndexDelta > 0 ? "#4CAF50" : diceIndexDelta < 0 ? "#F44336" : "#ffffff";
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
    }

    private int diceIndex(String die) {
        int index = java.util.Arrays.asList(DICE_PROGRESSION).indexOf(die);
        return index < 0 ? 0 : index;
    }

    /**
     * Format a weapon name with ammo count when the weapon is ranged.
     */
    private String formatWeaponWithAmmoCount(Entity entity, Weapon weapon) {
        if (weapon == null) {
            return "None";
        }

        if (!weapon.isRanged()) {
            return weapon.getName();
        }

        int ammoCount = 0;
        for (Item item : entity.getCharSheet().getInventory()) {
            if (item instanceof Ammunition ammo && ammo.isCompatibleWith(weapon)) {
                ammoCount += Math.max(0, ammo.getQuantity());
            }
        }

        return weapon.getName() + " (x" + ammoCount + ")";
    }
    
    private void adjustHealth(int delta) {
        if (selectedEntity == null) return;
        
        if (selectedEntity instanceof Entity e) {
            int maxHP = e.getCharSheet().getTotalHP();
            int currentHP = e.getCharSheet().getCurrentHP();
            int newHP = Math.max(0, Math.min(maxHP, currentHP + delta));
            e.getCharSheet().setCurrentHP(newHP);
            e.getCharSheet().save();
            refreshPartyHealth();
            gridCanvas.redraw();
            updateSelectedEntity(e);
        } else if (selectedEntity instanceof Enemy en) {
            int maxHP = en.getMaxHealth();
            int currentHP = en.getHealth();
            int newHP = Math.max(0, Math.min(maxHP, currentHP + delta));
            en.setHealth(newHP);
            gridCanvas.redraw();
            updateSelectedEntity(en);
        }
    }

    /**
     * Temporarily adjust the selected entity's/enemy's AC for this battle only.
     * Not persisted - it lives on the in-battle instance and resets next battle.
     */
    private void adjustAC(int delta) {
        if (selectedEntity == null) return;

        if (selectedEntity instanceof Entity e) {
            e.adjustAC(delta);
            updateSelectedEntity(e);
        } else if (selectedEntity instanceof Enemy en) {
            en.adjustAC(delta);
            updateSelectedEntity(en);
        }
    }

    /**
     * Temporarily adjust one of the selected entity's/enemy's basic stats for this battle only.
     * Not persisted - it lives on the in-battle instance and resets next battle. Enemies only
     * have a real Dexterity stat, so any other index is a no-op for them (their rows are hidden).
     */
    private void adjustStat(int attributeIndex, int delta) {
        if (selectedEntity == null) return;

        if (selectedEntity instanceof Entity e) {
            e.adjustStat(attributeIndex, delta);
            updateSelectedEntity(e);
        } else if (selectedEntity instanceof Enemy en && attributeIndex == CharSheet.DEXTERITY) {
            en.adjustDexterity(delta);
            updateSelectedEntity(en);
        }
    }

    private static final String[] DICE_PROGRESSION = {"d4", "d6", "d8", "d10", "d12", "d20"};

    /**
     * Step a die one size up or down the d4..d20 progression, clamped at both ends.
     */
    private String cycleDie(String currentDie, int direction) {
        int newIndex = Math.max(0, Math.min(DICE_PROGRESSION.length - 1, diceIndex(currentDie) + direction));
        return DICE_PROGRESSION[newIndex];
    }

    /**
     * Temporarily override one damage-dice tier for this battle only.
     * Not persisted - it lives on the in-battle instance and resets next battle.
     */
    private void adjustDice(int tier, int direction) {
        if (selectedEntity == null) return;

        if (selectedEntity instanceof Entity e) {
            String currentDie = e.getDamageDice()[tier];
            e.setDiceOverride(tier, cycleDie(currentDie, direction));
            updateSelectedEntity(e);
        } else if (selectedEntity instanceof Enemy en) {
            String currentDie = en.getDamageDice()[tier];
            en.setDiceOverride(tier, cycleDie(currentDie, direction));
            updateSelectedEntity(en);
        }
    }

    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15, 10, 10, 10));
        panel.setStyle("-fx-border-color: #505052; -fx-border-width: 1 0 0 0;");

        Button backBtn = new Button("Character Sheets");
        backBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.PERSON));
        backBtn.getStyleClass().add("button");
        backBtn.setPrefSize(160, 40);
        backBtn.setOnAction(e -> handleBack());

        addObjBtn = new Button("Add Objects");
        addObjBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.PLUS));
        addObjBtn.getStyleClass().add("button");
        addObjBtn.setPrefSize(140, 40);
        addObjBtn.setOnAction(e -> toggleAddObjectsPanel());
        addObjBtn.disableProperty().bind(placementModeProperty);
        // Objects can now be placed during battle

        battleToggleBtn = new Button();
        battleToggleBtn.setPrefSize(150, 40);
        battleToggleBtn.getStyleClass().clear();
        battleToggleBtn.getStyleClass().add(battleState.isBattleStarted() ? "button-danger" : "button-primary");
        battleToggleBtn.disableProperty().bind(placementModeProperty);
        battleState.battleStartedProperty().addListener((obs, oldVal, newVal) -> {
            battleToggleBtn.getStyleClass().clear();
            battleToggleBtn.getStyleClass().add(newVal ? "button-danger" : "button-primary");
        });
        battleToggleBtn.textProperty().bind(
            battleState.battleStartedProperty().map(started -> started ? "End Battle" : "Begin Battle")
        );
        battleToggleBtn.graphicProperty().bind(
            battleState.battleStartedProperty().map(started -> started ? IconUtils.smallIcon(IconUtils.Icon.STOP) : IconUtils.smallIcon(IconUtils.Icon.PLAY))
        );
        battleToggleBtn.setOnAction(e -> {
            if (battleState.isBattleStarted()) {
                handleEndBattle();
            } else {
                handleBeginBattle();
            }
        });

        nextTurnBtn = new Button("Next Turn");
        nextTurnBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.FAST_FORWARD));
        nextTurnBtn.getStyleClass().add("button");
        nextTurnBtn.setPrefSize(140, 40);
        nextTurnBtn.setOnAction(e -> handleNextTurn());
        // Enable next turn only when battle is active and not in placement mode
        nextTurnBtn.disableProperty().bind(
            battleState.battleStartedProperty().not().or(placementModeProperty)
        );
        
        // Surprise round toggle (only visible before battle)
        surpriseRoundCheckbox = new CheckBox("Surprise Round");
        surpriseRoundCheckbox.getStyleClass().add("check-box");
        surpriseRoundCheckbox.setTooltip(new Tooltip("Party members act first before normal turn order"));
        surpriseRoundCheckbox.visibleProperty().bind(battleState.battleStartedProperty().not());
        surpriseRoundCheckbox.managedProperty().bind(battleState.battleStartedProperty().not());

        panel.getChildren().addAll(backBtn, addObjBtn, surpriseRoundCheckbox, battleToggleBtn, nextTurnBtn);
        return panel;
    }

    private void handleBeginBattle() {
        if (grid.getEntities().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Entities",
                    "Please add at least one entity before beginning battle.");
            return;
        }

        Map<Entity, Integer> partyRollInputs = promptPartyInitiativeRolls();
        if (partyRollInputs == null) {
            // User cancelled manual initiative entry.
            return;
        }

        battleState.setBattleStarted(true);
        turnManager.setBattleStarted(true);
        
        // Set surprise round before calculating initiative
        turnManager.setSurpriseRound(surpriseRoundCheckbox.isSelected());
        
        // Calculate initiative and get log messages
        combatLogPane.clear();
        List<String> initiativeMessages = turnManager.calculateInitiativeOrder(partyRollInputs);
        
        // Log all initiative rolls
        for (String msg : initiativeMessages) {
            combatLogPane.log(msg, CombatLogPane.LogType.INFO);
        }
        
        combatLogPane.logRound(1);
        
        // Log first turn and process abilities
        if (!turnManager.getTurnOrder().isEmpty()) {
            GridObject first = turnManager.getTurnOrder().get(0);
            String firstName = (first instanceof Entity e) ? e.getName() : 
                              (first instanceof Enemy en) ? en.getName() : "Unknown";
            combatLogPane.logTurnStart(firstName);
            
            // Process ON_TURN_START abilities for first combatant
            if (first instanceof Entity e) {
                java.util.List<String> abilityMessages = e.getCharSheet().processEquippedAbilities(EntityRes.ItemAbility.ON_TURN_START);
                for (String msg : abilityMessages) {
                    combatLogPane.logAbility(firstName, msg);
                }
            }
        }
        
        // Check for pending tie resolutions
        if (turnManager.hasPendingTieResolutions()) {
            handleTieResolutions(turnManager.getPendingTieResolutions());
        }

        gridCanvas.setBattleStarted(true);
        timelinePane.setVisible(true);
        timelinePane.refresh();
        partyHealthPane.refresh(grid.getEntities());
        updateActionPanel();
        gridCanvas.redraw();
    }

    /**
     * Prompt the player for initiative d20 rolls for each party member currently on the field.
     * Enemy initiatives remain randomized.
     *
     * @return Map of party entity to manual d20 roll; null when cancelled.
     */
    private Map<Entity, Integer> promptPartyInitiativeRolls() {
        List<Entity> partyMembers = new ArrayList<>();
        for (Entity entity : grid.getEntities()) {
            if (entity.isParty()) {
                partyMembers.add(entity);
            }
        }

        if (partyMembers.isEmpty()) {
            return new HashMap<>();
        }

        Dialog<Map<Entity, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Party Initiative Rolls");
        dialog.setHeaderText("Enter each party member's d20 initiative roll (1-20)");

        VBox content = new VBox(8);
        content.setPadding(new Insets(10));

        Map<Entity, Spinner<Integer>> rollInputs = new LinkedHashMap<>();
        for (Entity member : partyMembers) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(member.getName() + ":");
            nameLabel.setMinWidth(140);

            Spinner<Integer> rollSpinner = new Spinner<>(1, 20, 10);
            rollSpinner.setEditable(true);
            rollSpinner.setPrefWidth(80);
            rollInputs.put(member, rollSpinner);

            row.getChildren().addAll(nameLabel, rollSpinner);
            content.getChildren().add(row);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Map<Entity, Integer> result = new HashMap<>();
                for (Map.Entry<Entity, Spinner<Integer>> entry : rollInputs.entrySet()) {
                    Integer value = entry.getValue().getValue();
                    int roll = value != null ? value : 1;
                    result.put(entry.getKey(), Math.max(1, Math.min(20, roll)));
                }
                return result;
            }
            return null;
        });

        Optional<Map<Entity, Integer>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void handleNextTurn() {
        int prevRound = turnManager.getRound();
        java.util.List<String> abilityMessages = turnManager.nextTurn();
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
            
            // Log any triggered abilities
            for (String msg : abilityMessages) {
                combatLogPane.logAbility(name, msg);
            }
        }
        
        updateActionPanel();
        timelinePane.refresh();
        partyHealthPane.refresh(grid.getEntities());
        gridCanvas.redraw();
    }
    
    /**
     * Handle tie resolution requests from TurnManager.
     * Shows dialog for each group of tied party members.
     */
    private void handleTieResolutions(List<List<GridObject>> tieGroups) {
        for (List<GridObject> tiedGroup : tieGroups) {
            if (tiedGroup.size() > 1) {
                showTieResolutionDialog(tiedGroup);
            }
        }
    }
    
    /**
     * Show a dialog for players to choose turn order among tied party members.
     */
    private void showTieResolutionDialog(List<GridObject> tiedGroup) {
        Dialog<List<GridObject>> dialog = new Dialog<>();
        dialog.setTitle("Initiative Tie");
        dialog.setHeaderText("These party members tied for initiative.\nClick to set turn order (first click goes first):");
        
        int initRoll = turnManager.getInitiativeRoll(tiedGroup.get(0));
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label rollLabel = new Label("Initiative Roll: " + initRoll);
        rollLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #569cd6;");
        content.getChildren().add(rollLabel);
        
        // Track selection order
        List<GridObject> selectionOrder = new ArrayList<>();
        Map<GridObject, Button> buttonMap = new HashMap<>();
        
        VBox buttonBox = new VBox(5);
        for (GridObject obj : tiedGroup) {
            String name = (obj instanceof Entity e) ? e.getName() : "Unknown";
            Button btn = new Button(name);
            btn.setPrefWidth(200);
            btn.getStyleClass().add("button");
            
            btn.setOnAction(e -> {
                if (!selectionOrder.contains(obj)) {
                    selectionOrder.add(obj);
                    btn.setText((selectionOrder.size()) + ". " + name);
                    btn.setDisable(true);
                    btn.setStyle("-fx-opacity: 0.7;");
                }
            });
            
            buttonMap.put(obj, btn);
            buttonBox.getChildren().add(btn);
        }
        
        content.getChildren().add(buttonBox);
        
        // Add reset button
        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().add("button");
        resetBtn.setOnAction(e -> {
            selectionOrder.clear();
            for (GridObject obj : tiedGroup) {
                Button btn = buttonMap.get(obj);
                String name = (obj instanceof Entity en) ? en.getName() : "Unknown";
                btn.setText(name);
                btn.setDisable(false);
                btn.setStyle("");
            }
        });
        content.getChildren().add(resetBtn);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Disable OK until all are selected
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDisable(true);
        
        // Check selection completeness on each click
        dialog.getDialogPane().addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (event.getTarget() != okBtn && event.getTarget() != resetBtn) {
                okBtn.setDisable(selectionOrder.size() < tiedGroup.size());
            }
        });
        
        // Also use a simple polling approach since the above may not catch everything
        javafx.animation.Timeline checker = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> {
                okBtn.setDisable(selectionOrder.size() < tiedGroup.size());
            })
        );
        checker.setCycleCount(javafx.animation.Animation.INDEFINITE);
        checker.play();
        
        dialog.setResultConverter(buttonType -> {
            checker.stop();
            if (buttonType == ButtonType.OK && selectionOrder.size() == tiedGroup.size()) {
                return selectionOrder;
            }
            // If cancelled or incomplete, use current order (random)
            return null;
        });
        
        dialog.showAndWait().ifPresent(chosenOrder -> {
            if (chosenOrder != null) {
                turnManager.applyTieResolution(tiedGroup, chosenOrder);
                timelinePane.refresh();
                
                // Log the chosen order
                StringBuilder orderStr = new StringBuilder("Turn order set: ");
                for (int i = 0; i < chosenOrder.size(); i++) {
                    if (i > 0) orderStr.append(" → ");
                    GridObject obj = chosenOrder.get(i);
                    orderStr.append((obj instanceof Entity e) ? e.getName() : "Unknown");
                }
                combatLogPane.log(orderStr.toString(), CombatLogPane.LogType.INFO);
            }
        });
    }

    public boolean isBattleStarted() {
        return battleState.isBattleStarted();
    }
    
    public BattleState getBattleState() {
        return battleState;
    }
    
    public DiceRollPanel getDiceRollPanel() {
        return diceRollPanel;
    }
    
    /**
     * Show the dice roll panel and hide the action panel
     */
    public void showDiceRollPanel() {
        actionPanel.setVisible(false);
        actionPanel.setManaged(false);
        diceRollPanel.setVisible(true);
        diceRollPanel.setManaged(true);
    }
    
    /**
     * Hide the dice roll panel and show the action panel
     */
    public void hideDiceRollPanel() {
        diceRollPanel.setVisible(false);
        diceRollPanel.setManaged(false);
        actionPanel.setVisible(true);
        actionPanel.setManaged(true);
    }

    public void addEntity(Entity entity) {
        turnManager.addEntity(entity);
        partyHealthPane.updateParty(grid.getEntities());
        if (battleState.isBattleStarted()) {
            updateActionPanel();
        }
    }

    public void removeEntity(Entity entity) {
        turnManager.removeEntity(entity);
        partyHealthPane.updateParty(grid.getEntities());
        if (battleState.isBattleStarted()) {
            updateActionPanel();
        }
    }

    public void addEnemy(Enemy enemy) {
        turnManager.addEnemy(enemy);
        if (battleState.isBattleStarted()) {
            updateActionPanel();
        }
    }

    public void removeEnemy(Enemy enemy) {
        turnManager.removeEnemy(enemy);
        if (battleState.isBattleStarted()) {
            updateActionPanel();
        }
    }

    /**
     * Refreshes the party health display. Call after combat actions.
     */
    public void refreshPartyHealth() {
        partyHealthPane.refresh(grid.getEntities());
    }

    private void handleBack() {
        appController.navigateToCharacterSheets();
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
            
            // Check if this is a victory (enemies were defeated or no enemies remain)
            boolean isVictory = grid.getEnemies().isEmpty() || battleState.getEnemiesDefeated() > 0;
            
            if (isVictory) {
                // Show victory screen
                showVictoryScreen();
            } else {
                // Direct return (retreat/cancel)
                sheetView.endBattle();
                appController.returnFromBattle();
            }
        }
    }
    
    /**
     * Display the Final Fantasy-style victory screen.
     */
    private void showVictoryScreen() {
        VictoryView victoryView = new VictoryView(grid.getEntities(), () -> {
            // On continue - remove victory screen and return from battle
            rootWrapper.getChildren().removeIf(n -> n instanceof VictoryView);
            sheetView.endBattle();
            appController.returnFromBattle();
        });
        
        rootWrapper.getChildren().add(victoryView);
        victoryView.playEntranceAnimation();
    }

    private ScrollPane createPartyPanel() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("panel");

        ArrayList<SheetButton> sheets = sheetView.getSheets();
        boolean hasAvailableParty = false;

        for (SheetButton sheetBtn : sheets) {
            if (sheetBtn.getSheet().getCharSheet().getParty()) {
                CharSheet cs = sheetBtn.getSheet().getCharSheet();
                
                // Skip characters already on the field
                boolean alreadyOnField = grid.getEntities().stream()
                    .anyMatch(entity -> entity.getCharSheet().getName().equals(cs.getName()));
                if (alreadyOnField) {
                    continue;
                }
                
                hasAvailableParty = true;

                // Create a card-style button for party members
                final String key = "party:" + cs.getName();
                HBox card = createEntitySelectionCard(cs, true, isPlacementSelectionActive(key));
                card.setOnMouseClicked(e -> {
                    startObjectPlacement(() -> new Entity(0, 0, sheetBtn.getSheet().getCharSheet()),
                        key, cs.getName());
                });
                content.getChildren().add(card);
            }
        }

        if (!hasAvailableParty) {
            Label empty = new Label("No party characters available");
            empty.setStyle("-fx-text-fill: #808080;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
    }
    
    private HBox createEntitySelectionCard(CharSheet cs, boolean isParty, boolean selected) {
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
        if (selected) {
            card.setStyle(card.getStyle().replace("-fx-border-color: " + style.borderColor,
                "-fx-border-color: " + style.accentColor + "; -fx-border-width: 2"));
        }
        
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
                final String key = "enemy:" + enemyName;
                final String displayName = template.getName();
                HBox card = createEnemySelectionCard(template, isPlacementSelectionActive(key));
                final String name = enemyName;
                card.setOnMouseClicked(e -> {
                    startObjectPlacement(() -> {
                        Enemy newEnemy = Enemy.load(name);
                        if (newEnemy == null) {
                            return null;
                        }
                        int instanceNum = enemyInstanceCounts.getOrDefault(name, 0) + 1;
                        enemyInstanceCounts.put(name, instanceNum);
                        newEnemy.setInstanceNumber(instanceNum);
                        return newEnemy;
                    }, key, displayName);
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
    
    private HBox createEnemySelectionCard(Enemy enemy, boolean selected) {
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
        if (selected) {
            card.setStyle(card.getStyle().replace("-fx-border-color: " + style.borderColor,
                "-fx-border-color: " + style.accentColor + "; -fx-border-width: 2"));
        }
        
        // Icon
        Node avatar = IconUtils.createIcon(IconUtils.Icon.SKULL, 24, style.accentColor);
        
        // Info section
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label nameLabel = new Label(enemy.getName());
        nameLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13px; -fx-font-weight: bold;", style.accentColor));
        
        Label statsLabel = new Label(String.format("ATK: %d  •  MOB: %d", enemy.getAttackModifier(), enemy.getMovement()));
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
                String key = "terrain:" + terrain.getType();
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                if (isPlacementSelectionActive(key)) {
                    btn.setStyle("-fx-border-color: #daa520; -fx-border-width: 2;");
                }
                btn.setOnAction(e -> {
                    startObjectPlacement(
                        () -> new TerrainObject(0, 0, terrain.getType(), terrain.getHealth()),
                        key,
                        terrain.getType()
                    );
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
        Map<String, Accessory> accessories = ItemDatabase.getInstance().getAllAccessories();
        Map<String, Consumable> consumables = ItemDatabase.getInstance().getAllConsumables();

        if (!weapons.isEmpty()) {
            Label header = new Label("Weapons");
            header.getStyleClass().add("label-header");
            content.getChildren().add(header);

            for (Weapon weapon : weapons.values()) {
                String btnText = String.format("%s  |  DMG: %s", weapon.getName(), String.join("/", weapon.getDamageDice()));
                String key = "pickup:weapon:" + weapon.getName();
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                if (isPlacementSelectionActive(key)) {
                    btn.setStyle("-fx-border-color: #daa520; -fx-border-width: 2;");
                }
                btn.setOnAction(e -> {
                    startObjectPlacement(
                        () -> new Pickup(0, 0, weapon),
                        key,
                        weapon.getName()
                    );
                });
                content.getChildren().add(btn);
            }
        }

        if (!accessories.isEmpty()) {
            Label header = new Label("Accessories");
            header.getStyleClass().add("label-header");
            content.getChildren().add(header);

            for (Accessory accessory : accessories.values()) {
                String btnText = String.format("%s  |  DEF: %d",
                        accessory.getName(), accessory.getDefense());
                String key = "pickup:accessory:" + accessory.getName();
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                if (isPlacementSelectionActive(key)) {
                    btn.setStyle("-fx-border-color: #daa520; -fx-border-width: 2;");
                }
                btn.setOnAction(e -> {
                    startObjectPlacement(
                        () -> new Pickup(0, 0, accessory),
                        key,
                        accessory.getName()
                    );
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
                String key = "pickup:consumable:" + consumable.getName();
                Button btn = new Button(btnText);
                btn.getStyleClass().add("button");
                btn.setMaxWidth(Double.MAX_VALUE);
                if (isPlacementSelectionActive(key)) {
                    btn.setStyle("-fx-border-color: #daa520; -fx-border-width: 2;");
                }
                btn.setOnAction(e -> {
                    startObjectPlacement(
                        () -> new Pickup(0, 0, consumable),
                        key,
                        consumable.getName()
                    );
                });
                content.getChildren().add(btn);
            }
        }

        if (weapons.isEmpty() && accessories.isEmpty() && consumables.isEmpty()) {
            Label empty = new Label("No pickup items available");
            empty.setStyle("-fx-text-fill: #808080;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
