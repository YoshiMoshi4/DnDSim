package UI.Battle;

import EntityRes.*;
import Objects.*;
import UI.SpriteUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Optional;

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

        getChildren().add(canvas);
        setStyle("-fx-background-color: white;");

        // Bind canvas size to pane size
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        // Redraw when size changes
        widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        heightProperty().addListener((obs, oldVal, newVal) -> redraw());

        // Keyboard shortcuts for menu
        setFocusTraversable(true);
        setOnKeyPressed(this::handleKeyPressed);

        // Mouse handlers
        canvas.setOnMousePressed(this::handleMousePressed);
    }

    public void setBattleStarted(boolean started) {
        this.battleStarted = started;
    }

    private void handleMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
            if (cellInfo != null) {
                int row = cellInfo[0];
                int col = cellInfo[1];
                if (grid.inBounds(row, col)) {
                    GridObject obj = grid.getObjectAt(row, col);
                    if (obj != null && !moveMode && !attackMode) {
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

    private void handleKeyPressed(javafx.scene.input.KeyEvent e) {
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
                        entity.getCharSheet().swapWeapons();
                        redraw();
                        battleView.updateSelectedEntity(entity);
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
        GridObject current = selectedObject;
        moveMode = false;
        movingEntity = null;
        movingEnemy = null;
        attackMode = false;
        attackingEntity = null;
        attackingEnemy = null;
        redraw();
    }

    private void triggerMove(Entity entity) {
        selectedObject = entity;
        attackMode = false;
        attackingEntity = null;
        moveMode = true;
        movingEntity = entity;
        redraw();
    }

    private void triggerAttack(Entity entity) {
        selectedObject = entity;
        attackMode = true;
        attackingEntity = entity;
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
        if (consumable.getHealAmount() > 0) {
            entity.getCharSheet().addCurrentHP(consumable.getHealAmount());
            combatLogPane.logHeal(entity.getName(), consumable.getHealAmount());
        }
        if (consumable.getEffect() != null) {
            entity.getCharSheet().addStatus(consumable.getEffect());
        }
        combatLogPane.logItemUse(entity.getName(), consumable.getName());
        if (consumable.getQuantity() > 1) {
            consumable.decQuantity();
        } else {
            entity.getCharSheet().getInventory().remove(consumable);
        }
        entity.getCharSheet().save();
        redraw();
        battleView.refreshPartyHealth();
    }

    private void triggerEnemyMove(Enemy enemy) {
        selectedObject = enemy;
        attackMode = false;
        attackingEntity = null;
        attackingEnemy = null;
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

        // Handle move mode for Entity
        if (moveMode && movingEntity != null) {
            if (clicked == null && !grid.isBlocked(row, col)) {
                int dist = Math.abs(movingEntity.getRow() - row) + Math.abs(movingEntity.getCol() - col);
                int mobilityLimit = movingEntity.getCharSheet().getTotalAttribute(3);

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
                Entity attacker = attackingEntity;
                int damage = Math.max(0, attackingEntity.getAttackPower() - target.getDefense());
                attackingEntity.attack(target);
                combatLogPane.logAttack(attackingEntity.getName(), target.getName(), 
                    damage, target.getHealth(), target.getCharSheet().getTotalHP());
                if (target.isDead()) {
                    grid.removeEntity(target);
                    turnManager.removeEntity(target);
                    combatLogPane.logDefeat(target.getName());
                }
                attackMode = false;
                attackingEntity = null;
                selectedObject = attacker;
                redraw();
                battleView.refreshPartyHealth();
                battleView.updateSelectedEntity(attacker);
                return;
            } else if (clicked instanceof Enemy targetEnemy) {
                Entity attacker = attackingEntity;
                int damage = Math.max(0, attackingEntity.getAttackPower() - targetEnemy.getDefense());
                targetEnemy.takeDamage(damage);
                combatLogPane.logAttack(attackingEntity.getName(), targetEnemy.getName(), 
                    damage, targetEnemy.getHealth(), targetEnemy.getMaxHealth());
                if (targetEnemy.isDead()) {
                    grid.removeEnemy(targetEnemy);
                    turnManager.removeEnemy(targetEnemy);
                    combatLogPane.logDefeat(targetEnemy.getName());
                    battleView.getBattleState().incrementEnemiesDefeated();
                }
                attackMode = false;
                attackingEntity = null;
                selectedObject = attacker;
                redraw();
                battleView.updateSelectedEntity(attacker);
                return;
            } else if (clicked instanceof TerrainObject terrain) {
                Entity attacker = attackingEntity;
                int damage = attackingEntity.getAttackPower();
                terrain.takeDamage(damage);
                combatLogPane.logTerrainDamage(attackingEntity.getName(), damage, terrain.getHealth());
                if (terrain.isDestroyed()) {
                    grid.removeDestroyedTerrain();
                    combatLogPane.logTerrainDestroyed();
                }
                attackMode = false;
                attackingEntity = null;
                selectedObject = attacker;
                redraw();
                battleView.updateSelectedEntity(attacker);
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
                Enemy attacker = attackingEnemy;
                int damage = Math.max(0, attackingEnemy.getAttackPower() - target.getDefense());
                target.takeDamage(damage);
                combatLogPane.logAttack(attackingEnemy.getName(), target.getName(), 
                    damage, target.getHealth(), target.getCharSheet().getTotalHP());
                if (target.isDead()) {
                    grid.removeEntity(target);
                    turnManager.removeEntity(target);
                    combatLogPane.logDefeat(target.getName());
                }
                attackMode = false;
                attackingEnemy = null;
                selectedObject = attacker;
                redraw();
                battleView.refreshPartyHealth();
                battleView.updateSelectedEntity(attacker);
                return;
            } else if (clicked instanceof Enemy targetEnemy && targetEnemy != attackingEnemy) {
                Enemy attacker = attackingEnemy;
                int damage = attackingEnemy.getAttackPower();
                targetEnemy.takeDamage(damage);
                combatLogPane.logAttack(attackingEnemy.getName(), targetEnemy.getName(), 
                    damage, targetEnemy.getHealth(), targetEnemy.getMaxHealth());
                if (targetEnemy.isDead()) {
                    grid.removeEnemy(targetEnemy);
                    turnManager.removeEnemy(targetEnemy);
                    combatLogPane.logDefeat(targetEnemy.getName());
                    battleView.getBattleState().incrementEnemiesDefeated();
                }
                attackMode = false;
                attackingEnemy = null;
                selectedObject = attacker;
                redraw();
                battleView.updateSelectedEntity(attacker);
                return;
            } else if (clicked instanceof TerrainObject terrain) {
                Enemy attacker = attackingEnemy;
                int damage = attackingEnemy.getAttackPower();
                terrain.takeDamage(damage);
                combatLogPane.logTerrainDamage(attackingEnemy.getName(), damage, terrain.getHealth());
                if (terrain.isDestroyed()) {
                    grid.removeDestroyedTerrain();
                    combatLogPane.logTerrainDestroyed();
                }
                attackMode = false;
                attackingEnemy = null;
                selectedObject = attacker;
                redraw();
                battleView.updateSelectedEntity(attacker);
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

    private void attemptMove(GridObject obj, int row, int col) {
        if (grid.isBlocked(row, col)) return;

        if (obj instanceof Entity e) {
            int dist = Math.abs(e.getRow() - row) + Math.abs(e.getCol() - col);

            if (battleStarted) {
                int mobilityLimit = e.getCharSheet().getTotalAttribute(3);
                if (dist > mobilityLimit) return;
            }
        }

        obj.setRow(row);
        obj.setCol(col);
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
            int mobilityLimit = movingEntity.getCharSheet().getTotalAttribute(3);
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
            gc.setFill(Color.BLACK);
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
            Color fallbackColor = getEnemyColor(en.getColor());
            
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

    private Color getEnemyColor(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> Color.BLACK;
            case 1 -> Color.DARKBLUE;
            case 2 -> Color.DARKGREEN;
            case 3 -> Color.DARKCYAN;
            case 4 -> Color.DARKRED;
            case 5 -> Color.DARKMAGENTA;
            case 6 -> Color.OLIVE;
            case 7 -> Color.GRAY;
            case 8 -> Color.DARKGRAY;
            case 9 -> Color.BLUE;
            case 10 -> Color.GREEN;
            case 11 -> Color.CYAN;
            case 12 -> Color.RED;
            case 13 -> Color.MAGENTA;
            case 14 -> Color.YELLOW;
            case 15 -> Color.WHITE;
            default -> Color.PURPLE;
        };
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
