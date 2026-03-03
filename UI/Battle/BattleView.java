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
    private Button endTurnBtn;
    private final Map<String, Integer> enemyInstanceCounts = new HashMap<>();
    
    // Dice roll panel for combat
    private DiceRollPanel diceRollPanel;
    
    // Pull-up panel
    private VBox addObjectsPanel;
    private boolean panelExpanded = false;
    private Label addStatusLabel;
    
    // Surprise round toggle
    private CheckBox surpriseRoundCheckbox;
    
    // Placement mode
    private boolean placementMode = false;
    private final Queue<CharSheet> partyToPlace = new LinkedList<>();
    private CharSheet currentPlacing = null;
    private javafx.beans.property.BooleanProperty placementModeProperty = new javafx.beans.property.SimpleBooleanProperty(false);

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

    private VBox createActionPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(170);
        panel.setMinWidth(170);
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
        
        // Attributes section
        Label attrLabel = new Label("Attributes");
        attrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #808080;");
        
        HBox attrRow1 = new HBox(8);
        Label strLabel = new Label("STR: --");
        strLabel.setId("strLabel");
        strLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");
        Label dexLabel = new Label("DEX: --");
        dexLabel.setId("dexLabel");
        dexLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");
        attrRow1.getChildren().addAll(strLabel, dexLabel);
        
        HBox attrRow2 = new HBox(8);
        Label itvLabel = new Label("ITV: --");
        itvLabel.setId("itvLabel");
        itvLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");
        Label mobLabel = new Label("MOB: --");
        mobLabel.setId("mobLabel");
        mobLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");
        attrRow2.getChildren().addAll(itvLabel, mobLabel);
        
        // Weapons section
        Label weapLabel = new Label("Weapons");
        weapLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #808080;");
        
        Label primaryLabel = new Label("1: --");
        primaryLabel.setId("primaryLabel");
        primaryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");
        primaryLabel.setWrapText(true);
        
        Label secondaryLabel = new Label("2: --");
        secondaryLabel.setId("secondaryLabel");
        secondaryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e0e0;");
        secondaryLabel.setWrapText(true);
        
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #505052;");
        
        Label actionsLabel = new Label("Actions");
        actionsLabel.getStyleClass().add("label-title");
        actionsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #808080;");
        
        // Action buttons
        moveBtn = new Button("Move (F)");
        moveBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.MOVE));
        moveBtn.getStyleClass().add("button");
        moveBtn.setMaxWidth(Double.MAX_VALUE);
        moveBtn.setDisable(true);
        moveBtn.setOnAction(e -> gridCanvas.startMoveMode());
        
        attackBtn = new Button("Attack (E)");
        attackBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.TARGET));
        attackBtn.getStyleClass().add("button");
        attackBtn.setMaxWidth(Double.MAX_VALUE);
        attackBtn.setDisable(true);
        attackBtn.setOnAction(e -> gridCanvas.startAttackMode());
        
        useItemBtn = new Button("Use Item (R)");
        useItemBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.POTION));
        useItemBtn.getStyleClass().add("button");
        useItemBtn.setMaxWidth(Double.MAX_VALUE);
        useItemBtn.setDisable(true);
        useItemBtn.setOnAction(e -> gridCanvas.triggerUseItemForSelected());
        
        swapBtn = new Button("Swap (Q)");
        swapBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.UNDO));
        swapBtn.getStyleClass().add("button");
        swapBtn.setMaxWidth(Double.MAX_VALUE);
        swapBtn.setDisable(true);
        swapBtn.setOnAction(e -> gridCanvas.triggerSwapForSelected());
        
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #505052;");
        
        endTurnBtn = new Button("End Turn");
        endTurnBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.FAST_FORWARD));
        endTurnBtn.getStyleClass().add("button");
        endTurnBtn.setMaxWidth(Double.MAX_VALUE);
        endTurnBtn.setDisable(true);
        endTurnBtn.setOnAction(e -> handleNextTurn());
        
        // Round indicator
        Label roundLabel = new Label("Round: --");
        roundLabel.setId("roundLabel");
        roundLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        
        panel.getChildren().addAll(
            selectLabel, selectedEntityLabel,
            attrLabel, attrRow1, attrRow2,
            weapLabel, primaryLabel, secondaryLabel, sep,
            actionsLabel, moveBtn, attackBtn, useItemBtn, swapBtn, sep2,
            endTurnBtn, roundLabel
        );
        
        return panel;
    }
    
    private void updateActionPanel() {
        Label roundLabel = (Label) actionPanel.lookup("#roundLabel");
        if (roundLabel != null) {
            roundLabel.setText("Round: " + turnManager.getRound());
        }
        // End turn is always available during battle
        endTurnBtn.setDisable(turnManager.getTurnOrder().isEmpty());
    }
    
    public void updateSelectedEntity(GridObject obj) {
        this.selectedEntity = obj;
        Label selectedLabel = (Label) actionPanel.lookup("#selectedEntityLabel");
        Label strLabel = (Label) actionPanel.lookup("#strLabel");
        Label dexLabel = (Label) actionPanel.lookup("#dexLabel");
        Label itvLabel = (Label) actionPanel.lookup("#itvLabel");
        Label mobLabel = (Label) actionPanel.lookup("#mobLabel");
        Label primaryLabel = (Label) actionPanel.lookup("#primaryLabel");
        Label secondaryLabel = (Label) actionPanel.lookup("#secondaryLabel");
        
        if (obj == null) {
            if (selectedLabel != null) selectedLabel.setText("--");
            if (strLabel != null) strLabel.setText("STR: --");
            if (dexLabel != null) dexLabel.setText("DEX: --");
            if (itvLabel != null) itvLabel.setText("ITV: --");
            if (mobLabel != null) mobLabel.setText("MOB: --");
            if (primaryLabel != null) primaryLabel.setText("1: --");
            if (secondaryLabel != null) secondaryLabel.setText("2: --");
            moveBtn.setDisable(true);
            attackBtn.setDisable(true);
            useItemBtn.setDisable(true);
            swapBtn.setDisable(true);
        } else if (obj instanceof Entity e) {
            if (selectedLabel != null) selectedLabel.setText(e.getName());
            if (strLabel != null) strLabel.setText("STR: " + e.getCharSheet().getTotalAttribute(0));
            if (dexLabel != null) dexLabel.setText("DEX: " + e.getCharSheet().getTotalAttribute(1));
            if (itvLabel != null) itvLabel.setText("ITV: " + e.getCharSheet().getTotalAttribute(2));
            if (mobLabel != null) mobLabel.setText("MOB: " + e.getCharSheet().getTotalAttribute(3));
            
            Weapon primary = e.getCharSheet().getEquippedWeapon();
            Weapon secondary = e.getCharSheet().getEquippedSecondary();
            if (primaryLabel != null) primaryLabel.setText("1: " + (primary != null ? primary.getName() : "None"));
            if (secondaryLabel != null) secondaryLabel.setText("2: " + (secondary != null ? secondary.getName() : "None"));
            
            boolean canAct = e.isParty() && battleState.isBattleStarted();
            moveBtn.setDisable(!canAct);
            attackBtn.setDisable(!canAct);
            useItemBtn.setDisable(!canAct);
            swapBtn.setDisable(!canAct);
        } else if (obj instanceof Enemy en) {
            if (selectedLabel != null) selectedLabel.setText(en.getName());
            if (strLabel != null) strLabel.setText("STR: --");
            if (dexLabel != null) dexLabel.setText("DEX: --");
            if (itvLabel != null) itvLabel.setText("ITV: --");
            if (mobLabel != null) mobLabel.setText("MOB: " + en.getMovement());
            if (primaryLabel != null) primaryLabel.setText("ATK: " + en.getAttackPower());
            if (secondaryLabel != null) secondaryLabel.setText("");
            
            boolean canAct = battleState.isBattleStarted();
            moveBtn.setDisable(!canAct);
            attackBtn.setDisable(!canAct);
            useItemBtn.setDisable(true);
            swapBtn.setDisable(true);
        }
        
        // Keep focus on grid canvas for keyboard shortcuts
        if (obj != null) {
            gridCanvas.requestFocus();
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

        battleState.setBattleStarted(true);
        turnManager.setBattleStarted(true);
        
        // Set surprise round before calculating initiative
        turnManager.setSurpriseRound(surpriseRoundCheckbox.isSelected());
        
        // Calculate initiative and get log messages
        combatLogPane.clear();
        List<String> initiativeMessages = turnManager.calculateInitiativeOrder();
        
        // Log all initiative rolls
        for (String msg : initiativeMessages) {
            combatLogPane.log(msg, CombatLogPane.LogType.INFO);
        }
        
        combatLogPane.logRound(1);
        
        // Log first turn
        if (!turnManager.getTurnOrder().isEmpty()) {
            GridObject first = turnManager.getTurnOrder().get(0);
            String firstName = (first instanceof Entity e) ? e.getName() : 
                              (first instanceof Enemy en) ? en.getName() : "Unknown";
            combatLogPane.logTurnStart(firstName);
        }
        
        // Check for pending tie resolutions
        if (turnManager.hasPendingTieResolutions()) {
            handleTieResolutions(turnManager.getPendingTieResolutions());
        }

        gridCanvas.setBattleStarted(true);
        timelinePane.setVisible(true);
        timelinePane.refresh();
        updateActionPanel();
        gridCanvas.redraw();
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
        
        updateActionPanel();
        timelinePane.refresh();
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
                HBox card = createEntitySelectionCard(cs, true);
                card.setOnMouseClicked(e -> {
                    CharSheet charSheet = sheetBtn.getSheet().getCharSheet();
                    Entity newEntity = new Entity(0, 0, charSheet);
                    grid.addEntityAtNextAvailable(newEntity);
                    addEntity(newEntity);
                    gridCanvas.redraw();
                    addStatusLabel.setText("Added: " + sheetBtn.getText());
                    // Refresh the panel to remove the added character
                    refreshAddObjectsPanels();
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
