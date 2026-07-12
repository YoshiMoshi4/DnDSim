package UI.Battle;

import EntityRes.*;
import Objects.*;
import UI.SpriteUtils;
import javafx.animation.PauseTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public class BattleGridCanvas extends Pane {

    private final Canvas canvas;
    private final BattleGrid grid;
    private final TurnManager turnManager;
    private final BattleView battleView;
    private final CombatLogPane combatLogPane;
    private GridObject selectedObject;
    private boolean battleStarted;
    private boolean attackMode;
    private Entity attackingEntity;
    private Enemy attackingEnemy;
    private boolean moveMode;
    private Entity movingEntity;
    private Enemy movingEnemy;
    private boolean pickupMode;
    private Entity pickupEntity;
    private final VBox infoPopup;
    private boolean infoPopupPinned;
    private GridObject infoPopupTarget;
    private final PauseTransition hoverDelay;
    private GridObject pendingHoverTarget;

    public BattleGridCanvas(BattleGrid grid, TurnManager tm, BattleView battleView,
                           CombatLogPane combatLogPane) {
        this.canvas = new Canvas();
        this.grid = grid;
        this.turnManager = tm;
        this.battleView = battleView;
        this.combatLogPane = combatLogPane;
        this.selectedObject = null;
        this.battleStarted = false;
        this.attackMode = false;
        this.attackingEntity = null;
        this.attackingEnemy = null;
        this.moveMode = false;
        this.movingEntity = null;
        this.movingEnemy = null;
        this.pickupMode = false;
        this.pickupEntity = null;
        this.infoPopup = new VBox(3);
        this.infoPopupPinned = false;
        this.infoPopupTarget = null;
        this.hoverDelay = new PauseTransition(Duration.seconds(1));
        this.pendingHoverTarget = null;

        getChildren().add(canvas);
        setStyle("-fx-background-color: white;");

        infoPopup.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
        infoPopup.setStyle("-fx-background-color: #2d2d30; -fx-border-color: #505052; " +
            "-fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;");
        infoPopup.setPrefWidth(160);
        infoPopup.setVisible(false);
        infoPopup.setManaged(false);
        getChildren().add(infoPopup);

        // Bind canvas size to pane size
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        // Redraw when size changes
        widthProperty().addListener((obs, oldVal, newVal) -> { hideInfoPopup(); redraw(); });
        heightProperty().addListener((obs, oldVal, newVal) -> { hideInfoPopup(); redraw(); });

        // Keyboard shortcuts for menu
        setFocusTraversable(true);
        setOnKeyPressed(this::handleKeyPressed);

        // Mouse handlers
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseExited(e -> {
            hoverDelay.stop();
            pendingHoverTarget = null;
            hideInfoPopupIfNotPinned();
        });
    }

    public void setBattleStarted(boolean started) {
        this.battleStarted = started;
    }

    private void handleMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            handleRightClick(e);
            return;
        }
        if (e.getButton() == MouseButton.PRIMARY) {
            if (infoPopupPinned) {
                hideInfoPopup();
            }
            int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
            if (cellInfo != null) {
                int row = cellInfo[0];
                int col = cellInfo[1];
                if (grid.inBounds(row, col)) {
                    GridObject obj = grid.getObjectAt(row, col);
                    if (obj != null && !moveMode && !attackMode && !pickupMode) {
                        // Select the entity and update action panel
                        selectedObject = obj;
                        battleView.updateSelectedEntity(obj);
                        requestFocus();
                        redraw();
                        return;
                    }
                }
            }
            handleLeftClick(e);
        }
    }

    private void handleRightClick(MouseEvent e) {
        int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
        if (cellInfo == null) return;
        int row = cellInfo[0];
        int col = cellInfo[1];
        if (!grid.inBounds(row, col)) return;

        GridObject obj = grid.getObjectAt(row, col);
        hoverDelay.stop();
        pendingHoverTarget = null;
        if (obj instanceof Entity || obj instanceof Enemy) {
            showInfoPopup(obj, e.getX(), e.getY(), true);
            requestFocus();
        } else {
            hideInfoPopup();
        }
    }

    /**
     * Track the entity/enemy under the cursor and, after it stays under the
     * cursor for a beat, show the (non-pinned) hover info popup.
     */
    private void handleMouseMoved(MouseEvent e) {
        if (infoPopupPinned) return;

        GridObject obj = null;
        int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
        if (cellInfo != null && grid.inBounds(cellInfo[0], cellInfo[1])) {
            GridObject candidate = grid.getObjectAt(cellInfo[0], cellInfo[1]);
            if (candidate instanceof Entity || candidate instanceof Enemy) {
                obj = candidate;
            }
        }

        if (obj == pendingHoverTarget) {
            // Still hovering the same target - let any pending timer keep running.
            return;
        }

        // Hover target changed - reset the delay and hide whatever was showing.
        hoverDelay.stop();
        pendingHoverTarget = obj;
        hideInfoPopupIfNotPinned();

        if (obj != null) {
            final GridObject hoverTarget = obj;
            final double mx = e.getX();
            final double my = e.getY();
            hoverDelay.setOnFinished(ev -> {
                if (pendingHoverTarget == hoverTarget && !infoPopupPinned) {
                    showInfoPopup(hoverTarget, mx, my, false);
                }
            });
            hoverDelay.playFromStart();
        }
    }

    /**
     * Show the hover/pinned info popup for an entity or enemy.
     * Pinned popups (right-click) include a Delete button and stay open until
     * the user left-clicks elsewhere on the grid.
     */
    private void showInfoPopup(GridObject obj, double mouseX, double mouseY, boolean pinned) {
        infoPopup.getChildren().clear();

        Label nameLabel = new Label(CombatManager.getTargetName(obj));
        nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        infoPopup.getChildren().add(nameLabel);

        int hp = CombatManager.getTargetHealth(obj);
        int maxHp = CombatManager.getTargetMaxHealth(obj);
        int ac = CombatManager.getTargetAC(obj);
        int mobility = 0;
        if (obj instanceof Entity entity) {
            mobility = entity.getMovement();
        } else if (obj instanceof Enemy enemy) {
            mobility = enemy.getMovement();
        }

        java.util.List<String> diceList = new java.util.ArrayList<>();
        String[] dice = CombatManager.getDamageDice(obj);
        if (dice != null) {
            for (String d : dice) {
                if (d != null) diceList.add(d);
            }
        }
        String diceText = String.join("/", diceList);

        for (String line : new String[]{
                "HP: " + hp + " / " + maxHp,
                "AC: " + ac,
                "MOB: " + mobility,
                "DMG: " + diceText}) {
            Label l = new Label(line);
            l.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
            infoPopup.getChildren().add(l);
        }

        if (pinned) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("button");
            deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 11px;");
            deleteBtn.setOnAction(ev -> deleteObject(obj));
            infoPopup.getChildren().add(deleteBtn);
        }

        infoPopupPinned = pinned;
        infoPopupTarget = obj;
        infoPopup.setVisible(true);
        // The popup is unmanaged, so its parent Pane never resizes it to fit its
        // content. applyCss() must run first so children's font/padding styles are
        // resolved before autosize() reads their preferred heights - otherwise the
        // computed height comes back too short and the background only covers part
        // of the content (e.g. just the name row).
        infoPopup.applyCss();
        infoPopup.autosize();

        double popupWidth = infoPopup.getWidth();
        double popupHeight = infoPopup.getHeight();

        double px = mouseX + 12;
        double py = mouseY + 12;
        if (px + popupWidth > getWidth()) px = mouseX - popupWidth - 12;
        if (py + popupHeight > getHeight()) py = mouseY - popupHeight - 12;
        if (px < 0) px = 4;
        if (py < 0) py = 4;

        infoPopup.setLayoutX(px);
        infoPopup.setLayoutY(py);
    }

    private void hideInfoPopupIfNotPinned() {
        if (!infoPopupPinned) {
            infoPopup.setVisible(false);
            infoPopupTarget = null;
        }
    }

    private void hideInfoPopup() {
        infoPopup.setVisible(false);
        infoPopupPinned = false;
        infoPopupTarget = null;
    }

    /**
     * Remove an entity or enemy from the board via the info popup's Delete button.
     */
    private void deleteObject(GridObject obj) {
        if (obj instanceof Entity entity) {
            grid.removeEntity(entity);
            turnManager.removeEntity(entity);
            combatLogPane.log(entity.getName() + " removed from the battlefield.", CombatLogPane.LogType.INFO);
        } else if (obj instanceof Enemy enemy) {
            grid.removeEnemy(enemy);
            turnManager.removeEnemy(enemy);
            combatLogPane.log(enemy.getName() + " removed from the battlefield.", CombatLogPane.LogType.INFO);
        }

        if (selectedObject == obj) {
            selectedObject = null;
            battleView.updateSelectedEntity(null);
        }

        hideInfoPopup();
        redraw();
        battleView.refreshPartyHealth();
    }

    private void handleKeyPressed(javafx.scene.input.KeyEvent e) {
        // Handle ESCAPE for object placement mode
        if (battleView.isObjectPlacementMode() && e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            battleView.cancelObjectPlacement();
            e.consume();
            return;
        }
        
        // Handle shortcuts when entity is selected
        if (battleStarted && selectedObject != null) {
            if (selectedObject instanceof Entity entity && entity.isParty()) {
                switch (e.getCode()) {
                    case F -> {
                        // Toggle move mode
                        if (moveMode && movingEntity == entity) {
                            cancelModes();
                        } else {
                            triggerMove(entity);
                        }
                        e.consume();
                    }
                    case E -> {
                        // Toggle attack mode
                        if (attackMode && attackingEntity == entity) {
                            cancelModes();
                        } else {
                            triggerAttack(entity);
                        }
                        e.consume();
                    }
                    case R -> {
                        triggerUseItem(entity);
                        e.consume();
                    }
                    case Q -> {
                        triggerSwapForSelected();
                        e.consume();
                    }
                    case P -> {
                        // Toggle pickup mode
                        if (pickupMode && pickupEntity == entity) {
                            cancelModes();
                        } else {
                            triggerPickup(entity);
                        }
                        e.consume();
                    }
                    case ESCAPE -> {
                        cancelModes();
                        e.consume();
                    }
                    default -> {}
                }
            } else if (selectedObject instanceof Enemy enemy) {
                switch (e.getCode()) {
                    case F -> {
                        // Toggle move mode
                        if (moveMode && movingEnemy == enemy) {
                            cancelModes();
                        } else {
                            triggerEnemyMove(enemy);
                        }
                        e.consume();
                    }
                    case E -> {
                        // Toggle attack mode
                        if (attackMode && attackingEnemy == enemy) {
                            cancelModes();
                        } else {
                            triggerEnemyAttack(enemy);
                        }
                        e.consume();
                    }
                    case ESCAPE -> {
                        cancelModes();
                        e.consume();
                    }
                    default -> {}
                }
            }
        }
    }

    private void cancelModes() {
        moveMode = false;
        movingEntity = null;
        movingEnemy = null;
        attackMode = false;
        attackingEntity = null;
        attackingEnemy = null;
        pickupMode = false;
        pickupEntity = null;
        redraw();
    }

    private void triggerMove(Entity entity) {
        selectedObject = entity;
        attackMode = false;
        attackingEntity = null;
        pickupMode = false;
        pickupEntity = null;
        moveMode = true;
        movingEntity = entity;
        redraw();
    }

    private void triggerAttack(Entity entity) {
        // Check if entity has ammo for ranged weapon
        if (!CombatManager.hasAmmoForWeapon(entity)) {
            Weapon weapon = entity.getCharSheet().getEquippedWeapon();
            String ammoType = weapon != null ? weapon.getAmmoType() : "ammo";
            showAlert(Alert.AlertType.WARNING, "No Ammunition", 
                "No " + ammoType + " ammo in inventory!");
            return;
        }
        selectedObject = entity;
        attackMode = true;
        attackingEntity = entity;
        pickupMode = false;
        pickupEntity = null;
        redraw();
    }

    private void triggerUseItem(Entity entity) {
        java.util.List<Consumable> consumables = new java.util.ArrayList<>();
        for (Item item : entity.getCharSheet().getInventory()) {
            if (item instanceof Consumable c) {
                consumables.add(c);
            }
        }
        if (consumables.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items", "No consumable items in inventory.");
            return;
        }
        // Show item selection dialog
        java.util.List<String> choices = new java.util.ArrayList<>();
        for (Consumable c : consumables) {
            choices.add(c.getName() + " (x" + c.getQuantity() + ")");
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Use Item");
        dialog.setHeaderText("Select an item to use:");
        dialog.showAndWait().ifPresent(choice -> {
            int idx = choices.indexOf(choice);
            if (idx >= 0) {
                Consumable consumable = consumables.get(idx);
                useConsumable(entity, consumable);
            }
        });
    }

    private void useConsumable(Entity entity, Consumable consumable) {
        // Show d10 roll dialog for efficacy
        showEfficacyRollDialog(entity, consumable);
    }
    
    /**
     * Show a dialog to roll d10 for item efficacy.
     * Roll determines healing percentage: 1-10 = 10%-100%
     */
    private void showEfficacyRollDialog(Entity entity, Consumable consumable) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Item Efficacy Roll");
        dialog.setHeaderText("Roll d10 for " + consumable.getName() + " efficacy");
        
        VBox content = new VBox(12);
        content.setPadding(new javafx.geometry.Insets(15));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Info about the item
        Label itemInfo = new Label("Base healing: " + consumable.getHealAmount() + " HP");
        itemInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0;");
        
        Label rollInfo = new Label("Roll determines efficacy (1=10%, 10=100%)");
        rollInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        
        // Input field
        TextField rollInput = new TextField();
        rollInput.setPromptText("Enter d10 roll (1-10)");
        rollInput.setPrefWidth(150);
        rollInput.setStyle("-fx-font-size: 14px; -fx-alignment: center;");
        
        // Preview label for showing calculated heal
        Label previewLabel = new Label("");
        previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
        
        // Update preview as user types
        rollInput.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int roll = Integer.parseInt(newVal.trim());
                if (roll >= 1 && roll <= 10) {
                    int efficacyPercent = roll * 10;
                    int healAmount = (int) Math.ceil(consumable.getHealAmount() * efficacyPercent / 100.0);
                    previewLabel.setText("Heals " + healAmount + " HP (" + efficacyPercent + "% efficacy)");
                    previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
                } else {
                    previewLabel.setText("Enter 1-10");
                    previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F44336;");
                }
            } catch (NumberFormatException e) {
                previewLabel.setText("");
            }
        });
        
        content.getChildren().addAll(itemInfo, rollInfo, rollInput, previewLabel);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Disable OK until valid input
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDisable(true);
        
        rollInput.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int roll = Integer.parseInt(newVal.trim());
                okBtn.setDisable(roll < 1 || roll > 10);
            } catch (NumberFormatException e) {
                okBtn.setDisable(true);
            }
        });
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return Integer.parseInt(rollInput.getText().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(roll -> {
            applyConsumableWithEfficacy(entity, consumable, roll);
        });
    }
    
    /**
     * Apply consumable effect with the rolled efficacy.
     */
    private void applyConsumableWithEfficacy(Entity entity, Consumable consumable, int roll) {
        int efficacyPercent = roll * 10;
        
        if (consumable.getHealAmount() > 0) {
            int healAmount = (int) Math.ceil(consumable.getHealAmount() * efficacyPercent / 100.0);
            entity.getCharSheet().addCurrentHP(healAmount);
            combatLogPane.log(entity.getName() + " used " + consumable.getName() + 
                " (d10=" + roll + ", " + efficacyPercent + "% efficacy)", CombatLogPane.LogType.INFO);
            combatLogPane.logHeal(entity.getName(), healAmount);
        }
        if (consumable.getEffect() != null) {
            entity.getCharSheet().addStatus(consumable.getEffect());
            combatLogPane.log("Applied effect: " + consumable.getEffect().getName(), CombatLogPane.LogType.INFO);
        }
        
        if (consumable.getQuantity() > 1) {
            consumable.decQuantity();
        } else {
            entity.getCharSheet().getInventory().remove(consumable);
        }
        entity.getCharSheet().save();
        redraw();
        battleView.refreshPartyHealth();
    }

    private void triggerPickup(Entity entity) {
        selectedObject = entity;
        attackMode = false;
        attackingEntity = null;
        moveMode = false;
        movingEntity = null;
        pickupMode = true;
        pickupEntity = entity;
        combatLogPane.log("Select an adjacent item to pick up...", CombatLogPane.LogType.INFO);
        redraw();
    }

    public void startPickupMode() {
        if (!battleStarted) return;
        if (selectedObject instanceof Entity entity && entity.isParty()) {
            triggerPickup(entity);
        }
    }

    private void triggerEnemyMove(Enemy enemy) {
        selectedObject = enemy;
        attackMode = false;
        attackingEntity = null;
        attackingEnemy = null;
        pickupMode = false;
        pickupEntity = null;
        moveMode = true;
        movingEntity = null;
        movingEnemy = enemy;
        redraw();
    }

    private void triggerEnemyAttack(Enemy enemy) {
        selectedObject = enemy;
        attackMode = true;
        attackingEntity = null;
        attackingEnemy = enemy;
        pickupMode = false;
        pickupEntity = null;
        moveMode = false;
        movingEntity = null;
        movingEnemy = null;
        redraw();
    }

    public void startMoveMode() {
        if (!battleStarted) return;
        if (selectedObject instanceof Entity entity && entity.isParty()) {
            triggerMove(entity);
        } else if (selectedObject instanceof Enemy enemy) {
            triggerEnemyMove(enemy);
        }
    }

    public void startAttackMode() {
        if (!battleStarted) return;
        if (selectedObject instanceof Entity entity && entity.isParty()) {
            triggerAttack(entity);
        } else if (selectedObject instanceof Enemy enemy) {
            triggerEnemyAttack(enemy);
        }
    }

    public void triggerUseItemForSelected() {
        if (!battleStarted) return;
        if (selectedObject instanceof Entity entity && entity.isParty()) {
            triggerUseItem(entity);
        }
    }

    public void triggerSwapForSelected() {
        if (!battleStarted) return;
        if (selectedObject instanceof Entity entity && entity.isParty()) {
            entity.getCharSheet().swapWeapons();
            // The newly-equipped weapon may have entirely different dice - drop any temporary
            // per-tier overrides so the display/combat math reflect its real dice, not leftovers.
            entity.resetDiceOverride();
            redraw();
            battleView.updateSelectedEntity(entity);
        }
    }

    private int[] getCellAtPoint(double mouseX, double mouseY) {
        int rows = grid.getRows();
        int cols = grid.getCols();
        int cellSize = (int) Math.min(canvas.getWidth() / cols, canvas.getHeight() / rows);

        int gridWidth = cellSize * cols;
        int gridHeight = cellSize * rows;
        int offsetX = (int) ((canvas.getWidth() - gridWidth) / 2);
        int offsetY = (int) ((canvas.getHeight() - gridHeight) / 2);

        if (mouseX < offsetX || mouseY < offsetY || mouseX > offsetX + gridWidth || mouseY > offsetY + gridHeight) {
            return null;
        }

        int col = (int) ((mouseX - offsetX) / cellSize);
        int row = (int) ((mouseY - offsetY) / cellSize);
        return new int[]{row, col};
    }

    private void handleLeftClick(MouseEvent e) {
        int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
        if (cellInfo == null) return;

        int row = cellInfo[0];
        int col = cellInfo[1];

        if (!grid.inBounds(row, col)) return;

        GridObject clicked = grid.getObjectAt(row, col);
        
        // Handle placement mode
        if (battleView.isPlacementMode()) {
            CharSheet placing = battleView.getCurrentPlacing();
            if (placing != null && clicked == null && !grid.isBlocked(row, col)) {
                // Place the entity at clicked location
                Entity newEntity = new Entity(row, col, placing);
                battleView.entityPlaced(newEntity);
                return;
            } else if (clicked != null || grid.isBlocked(row, col)) {
                // Invalid placement location - log message
                combatLogPane.log("Cannot place here - tile is occupied", CombatLogPane.LogType.INFO);
                return;
            }
            return;
        }
        
        // Handle object placement mode (enemies, terrain, pickups)
        if (battleView.isObjectPlacementMode()) {
            if (clicked == null && !grid.isBlocked(row, col)) {
                // Place the object at clicked location
                battleView.objectPlaced(row, col);
                return;
            } else {
                // Invalid placement location
                combatLogPane.log("Cannot place here - tile is occupied", CombatLogPane.LogType.INFO);
                return;
            }
        }

        // Handle pickup mode for Entity
        if (pickupMode && pickupEntity != null) {
            Pickup pickup = grid.getPickupAt(row, col);
            if (pickup != null) {
                // Check if adjacent to entity (within 1 tile)
                int dx = Math.abs(pickupEntity.getRow() - row);
                int dy = Math.abs(pickupEntity.getCol() - col);
                
                if (dx <= 1 && dy <= 1) {
                    // Pick up the item
                    Item item = pickup.getItem();
                    pickupEntity.getCharSheet().addItem(item);
                    pickupEntity.getCharSheet().save();
                    grid.removePickup(pickup);
                    
                    combatLogPane.log(pickupEntity.getName() + " picked up " + item.getName(), CombatLogPane.LogType.INFO);
                    
                    Entity entity = pickupEntity;
                    pickupMode = false;
                    pickupEntity = null;
                    selectedObject = entity;
                    redraw();
                    battleView.updateSelectedEntity(entity);
                    return;
                }
                // Too far away - fall through to cancel
            }
            // Clicked empty space, non-pickup, or too-far pickup - cancel mode
            Entity entity = pickupEntity;
            pickupMode = false;
            pickupEntity = null;
            selectedObject = entity;
            combatLogPane.log("Pickup cancelled.", CombatLogPane.LogType.INFO);
            redraw();
            battleView.updateSelectedEntity(entity);
            return;
        }

        // Handle move mode for Entity
        if (moveMode && movingEntity != null) {
            if (clicked == null && !grid.isBlocked(row, col)) {
                int dist = Math.abs(movingEntity.getRow() - row) + Math.abs(movingEntity.getCol() - col);
                int mobilityLimit = movingEntity.getMovement();

                if (dist <= mobilityLimit) {
                    Entity movedEntity = movingEntity;
                    movingEntity.setRow(row);
                    movingEntity.setCol(col);
                    moveMode = false;
                    movingEntity = null;
                    // Keep entity selected for additional actions
                    selectedObject = movedEntity;
                    redraw();
                    battleView.updateSelectedEntity(movedEntity);
                    return;
                } else {
                    // Clicked outside range - cancel mode
                    cancelModes();
                    return;
                }
            }
            if (clicked == null && grid.isBlocked(row, col)) {
                // Clicked on blocked space - cancel mode
                cancelModes();
                return;
            }
            if (clicked != null) {
                moveMode = false;
                movingEntity = null;
                selectedObject = clicked;
                battleView.updateSelectedEntity(clicked);
                redraw();
                return;
            }
        }

        // Handle move mode for Enemy
        if (moveMode && movingEnemy != null) {
            if (clicked == null && !grid.isBlocked(row, col)) {
                int dist = Math.abs(movingEnemy.getRow() - row) + Math.abs(movingEnemy.getCol() - col);
                int mobilityLimit = movingEnemy.getMovement();

                if (dist <= mobilityLimit) {
                    Enemy movedEnemy = movingEnemy;
                    movingEnemy.setRow(row);
                    movingEnemy.setCol(col);
                    moveMode = false;
                    movingEnemy = null;
                    // Keep enemy selected for additional actions
                    selectedObject = movedEnemy;
                    redraw();
                    battleView.updateSelectedEntity(movedEnemy);
                    return;
                } else {
                    // Clicked outside range - cancel mode
                    cancelModes();
                    return;
                }
            }
            if (clicked == null && grid.isBlocked(row, col)) {
                // Clicked on blocked space - cancel mode
                cancelModes();
                return;
            }
            if (clicked != null) {
                moveMode = false;
                movingEnemy = null;
                selectedObject = clicked;
                battleView.updateSelectedEntity(clicked);
                redraw();
                return;
            }
        }

        // Handle attack mode for Entity
        if (attackMode && attackingEntity != null) {
            if (clicked instanceof Entity target && target != attackingEntity) {
                // Start dice roll sequence for Entity attacking Entity
                startDiceRollAttack(attackingEntity, target);
                return;
            } else if (clicked instanceof Enemy targetEnemy) {
                // Start dice roll sequence for Entity attacking Enemy
                startDiceRollAttack(attackingEntity, targetEnemy);
                return;
            } else if (clicked instanceof TerrainObject terrain) {
                // Start dice roll sequence for Entity attacking Terrain
                startDiceRollAttack(attackingEntity, terrain);
                return;
            } else if (clicked == null) {
                Entity attacker = attackingEntity;
                attackMode = false;
                attackingEntity = null;
                selectedObject = attacker;
                redraw();
                battleView.updateSelectedEntity(attacker);
                return;
            }
        }

        // Handle attack mode for Enemy
        if (attackMode && attackingEnemy != null) {
            if (clicked instanceof Entity target) {
                // Start dice roll sequence for Enemy attacking Entity
                startDiceRollAttack(attackingEnemy, target);
                return;
            } else if (clicked instanceof Enemy targetEnemy && targetEnemy != attackingEnemy) {
                // Start dice roll sequence for Enemy attacking Enemy
                startDiceRollAttack(attackingEnemy, targetEnemy);
                return;
            } else if (clicked instanceof TerrainObject terrain) {
                // Start dice roll sequence for Enemy attacking Terrain
                startDiceRollAttack(attackingEnemy, terrain);
                return;
            } else if (clicked == null) {
                Enemy attacker = attackingEnemy;
                attackMode = false;
                attackingEnemy = null;
                selectedObject = attacker;
                redraw();
                battleView.updateSelectedEntity(attacker);
                return;
            }
        }
    }
    
    /**
     * Start the dice roll attack sequence via DiceRollPanel
     */
    private void startDiceRollAttack(GridObject attacker, GridObject target) {
        // Show dice panel, hide action panel
        battleView.showDiceRollPanel();
        
        DiceRollPanel dicePanel = battleView.getDiceRollPanel();
        dicePanel.startAttack(attacker, target, 
            this::handleAttackOutcome,
            this::handleAttackCancelled
        );
    }
    
    /**
     * Handle the outcome of a dice roll attack
     */
    private void handleAttackOutcome(DiceRollPanel.AttackOutcome outcome) {
        GridObject attacker = outcome.attacker;
        GridObject target = outcome.target;
        
        // Consume ammo for ranged weapons (regardless of hit/miss)
        String ammoConsumed = CombatManager.consumeAmmo(attacker);
        if (ammoConsumed != null) {
            combatLogPane.log(CombatManager.getAttackerName(attacker) + " used 1 " + ammoConsumed, CombatLogPane.LogType.INFO);
        }
        
        if (outcome.hit) {
            // Apply damage
            CombatManager.applyDamage(target, outcome.totalDamage);

            // Natural 20 critical tag in combat log.
            if (outcome.d20Roll == 20) {
                combatLogPane.log("CRIT! Natural 20 -> x1.5 damage", CombatLogPane.LogType.ATTACK);
            }
            
            // Log the attack with detailed info
            combatLogPane.logAttackRoll(
                CombatManager.getAttackerName(attacker),
                CombatManager.getTargetName(target),
                outcome.d20Roll, outcome.modifier, outcome.targetAC,
                outcome.margin, outcome.tier, outcome.totalDamage
            );
            
            // Check for defeat
            if (CombatManager.isTargetDead(target)) {
                if (target instanceof Entity e) {
                    grid.removeEntity(e);
                    turnManager.removeEntity(e);
                    combatLogPane.logDefeat(CombatManager.getTargetName(target));
                } else if (target instanceof Enemy en) {
                    grid.removeEnemy(en);
                    turnManager.removeEnemy(en);
                    battleView.getBattleState().incrementEnemiesDefeated();
                    combatLogPane.logDefeat(CombatManager.getTargetName(target));
                } else if (target instanceof TerrainObject) {
                    grid.removeDestroyedTerrain();
                    combatLogPane.logTerrainDestroyed();
                }
            }

            // Update stats (terrain damage doesn't count toward battle stats)
            if (!(target instanceof TerrainObject)) {
                battleView.getBattleState().addDamageDealt(outcome.totalDamage);
            }
            if (target instanceof Entity) {
                battleView.getBattleState().addDamageTaken(outcome.totalDamage);
            }
        } else {
            // Miss - log it
            combatLogPane.logMiss(
                CombatManager.getAttackerName(attacker),
                CombatManager.getTargetName(target),
                outcome.d20Roll, outcome.modifier, outcome.targetAC, outcome.margin
            );
        }
        
        // Clean up attack state
        finishAttack(attacker);
    }
    
    /**
     * Handle attack cancellation
     */
    private void handleAttackCancelled() {
        GridObject attacker = attackingEntity != null ? attackingEntity : attackingEnemy;
        finishAttack(attacker);
    }
    
    /**
     * Clean up after attack completes or is cancelled
     */
    private void finishAttack(GridObject attacker) {
        attackMode = false;
        attackingEntity = null;
        attackingEnemy = null;
        // Hide dice panel, show action panel
        battleView.hideDiceRollPanel();
        
        if (attacker != null) {
            selectedObject = attacker;
            battleView.updateSelectedEntity(attacker);
        }
        
        redraw();
        battleView.refreshPartyHealth();
    }

    public void pickupAction() {
        Entity current = turnManager.getCurrent();
        if (current == null || !current.isParty()) return;

        Pickup p = grid.getPickupAt(current.getRow(), current.getCol());
        if (p == null) return;

        current.pickup(p);
        grid.removePickup(p);
        redraw();
    }

    public void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Clear
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        if (width <= 0 || height <= 0) return;

        int rows = grid.getRows();
        int cols = grid.getCols();

        double cellSize = Math.min(width / cols, height / rows);
        double gridWidth = cellSize * cols;
        double gridHeight = cellSize * rows;
        double offsetX = (width - gridWidth) / 2;
        double offsetY = (height - gridHeight) / 2;

        // Grid lines
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        for (int r = 0; r <= rows; r++) {
            gc.strokeLine(offsetX, offsetY + r * cellSize, offsetX + gridWidth, offsetY + r * cellSize);
        }
        for (int c = 0; c <= cols; c++) {
            gc.strokeLine(offsetX + c * cellSize, offsetY, offsetX + c * cellSize, offsetY + gridHeight);
        }
        
        // Placement mode highlight - show valid placement cells
        if (battleView.isPlacementMode()) {
            gc.setFill(Color.rgb(100, 200, 255, 0.3));
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (grid.getObjectAt(r, c) == null && !grid.isBlocked(r, c)) {
                        double x = offsetX + c * cellSize;
                        double y = offsetY + r * cellSize;
                        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    }
                }
            }
        }

        // Movement range highlight for Entity
        if (moveMode && movingEntity != null) {
            int mobilityLimit = movingEntity.getMovement();
            int entityRow = movingEntity.getRow();
            int entityCol = movingEntity.getCol();

            gc.setFill(Color.rgb(0, 255, 0, 0.3));
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int dist = Math.abs(entityRow - r) + Math.abs(entityCol - c);
                    if (dist > 0 && dist <= mobilityLimit && !grid.isBlocked(r, c)) {
                        double x = offsetX + c * cellSize;
                        double y = offsetY + r * cellSize;
                        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    }
                }
            }
        }

        // Movement range highlight for Enemy
        if (moveMode && movingEnemy != null) {
            int mobilityLimit = movingEnemy.getMovement();
            int entityRow = movingEnemy.getRow();
            int entityCol = movingEnemy.getCol();

            gc.setFill(Color.rgb(0, 255, 0, 0.3));
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int dist = Math.abs(entityRow - r) + Math.abs(entityCol - c);
                    if (dist > 0 && dist <= mobilityLimit && !grid.isBlocked(r, c)) {
                        double x = offsetX + c * cellSize;
                        double y = offsetY + r * cellSize;
                        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    }
                }
            }
        }

        // Attack targets highlight for Entity
        if (attackMode && attackingEntity != null) {
            gc.setFill(Color.rgb(255, 0, 0, 0.3));

            for (Entity e : grid.getEntities()) {
                if (e != attackingEntity) {
                    double x = offsetX + e.getCol() * cellSize;
                    double y = offsetY + e.getRow() * cellSize;
                    gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    gc.setStroke(Color.RED);
                    gc.setLineWidth(2);
                    gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                }
            }
            for (Enemy en : grid.getEnemies()) {
                double x = offsetX + en.getCol() * cellSize;
                double y = offsetY + en.getRow() * cellSize;
                gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }
            for (TerrainObject t : grid.getTerrainObjects()) {
                double x = offsetX + t.getCol() * cellSize;
                double y = offsetY + t.getRow() * cellSize;
                gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }
            gc.setLineWidth(1);
        }

        // Attack targets highlight for Enemy
        if (attackMode && attackingEnemy != null) {
            gc.setFill(Color.rgb(255, 0, 0, 0.3));

            for (Entity e : grid.getEntities()) {
                double x = offsetX + e.getCol() * cellSize;
                double y = offsetY + e.getRow() * cellSize;
                gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }
            for (Enemy en : grid.getEnemies()) {
                if (en != attackingEnemy) {
                    double x = offsetX + en.getCol() * cellSize;
                    double y = offsetY + en.getRow() * cellSize;
                    gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    gc.setStroke(Color.RED);
                    gc.setLineWidth(2);
                    gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                }
            }
            for (TerrainObject t : grid.getTerrainObjects()) {
                double x = offsetX + t.getCol() * cellSize;
                double y = offsetY + t.getRow() * cellSize;
                gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }
            gc.setLineWidth(1);
        }

        // Terrain objects - render sprites or fallback
        for (TerrainObject t : grid.getTerrainObjects()) {
            double x = offsetX + t.getCol() * cellSize;
            double y = offsetY + t.getRow() * cellSize;
            
            // Use sprite if available, otherwise fallback to colored display
            SpriteUtils.drawTerrainSpriteOnCanvas(gc, t, x, y, cellSize);

            if (t == selectedObject) {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(2);
                double padding = 2;
                gc.strokeRect(x + padding, y + padding, cellSize - padding * 2, cellSize - padding * 2);
            }
        }

        // Pickups - small black circles
        for (Pickup p : grid.getPickups()) {
            double x = offsetX + p.getCol() * cellSize;
            double y = offsetY + p.getRow() * cellSize;
            double circleSize = cellSize * 0.35;
            double offset = (cellSize - circleSize) / 2;
            String pickupColor = p.getItem() != null ? p.getItem().getColor() : EntityRes.ColorUtils.DEFAULT_COLOR;
            gc.setFill(Color.web(pickupColor));
            gc.fillOval(x + offset, y + offset, circleSize, circleSize);
        }

        // Entities
        for (Entity e : grid.getEntities()) {
            double x = offsetX + e.getCol() * cellSize;
            double y = offsetY + e.getRow() * cellSize;
            double spriteSize = cellSize - 8;
            double centerX = x + cellSize / 2;
            double centerY = y + cellSize / 2;
            
            // Get fallback color
            java.awt.Color awtColor = e.getCharSheet().getDisplayColor();
            Color fallbackColor = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

            // Highlight whichever combatant's turn it currently is
            if (turnManager.isCurrent(e)) {
                gc.setFill(Color.rgb(255, 215, 0, 0.35));
                gc.fillOval(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }

            // Draw sprite or fallback
            SpriteUtils.drawSpriteOnCanvas(gc, e.getCharSheet().getSpritePath(),
                centerX, centerY, spriteSize, fallbackColor, true);

            if (e == selectedObject) {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(3);
                gc.strokeOval(x + 2, y + 2, cellSize - 4, cellSize - 4);
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(1);
                gc.strokeOval(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }
        }

        // Enemies
        for (Enemy en : grid.getEnemies()) {
            double x = offsetX + en.getCol() * cellSize;
            double y = offsetY + en.getRow() * cellSize;
            double spriteSize = cellSize - 8;
            double centerX = x + cellSize / 2;
            double centerY = y + cellSize / 2;
            
            // Get fallback color
            Color fallbackColor = Color.web(en.getColor());

            // Highlight whichever combatant's turn it currently is
            if (turnManager.isCurrent(en)) {
                gc.setFill(Color.rgb(255, 215, 0, 0.35));
                gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }

            // Draw sprite or fallback (enemies use squares as fallback)
            String spritePath = en.getSpritePath();
            if (spritePath != null && SpriteUtils.spriteExists(spritePath)) {
                SpriteUtils.drawSpriteOnCanvas(gc, spritePath, centerX, centerY, spriteSize, fallbackColor, false);
            } else {
                // Fallback to colored square for enemies
                gc.setFill(fallbackColor);
                gc.fillRect(x + 4, y + 4, cellSize - 8, cellSize - 8);
            }

            if (en == selectedObject) {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(3);
                gc.strokeRect(x + 2, y + 2, cellSize - 4, cellSize - 4);
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(1);
                gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }
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
