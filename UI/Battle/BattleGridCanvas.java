package UI.Battle;

import EntityRes.*;
import Objects.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
    private GridObject selectedObject;
    private boolean battleStarted;
    private boolean attackMode;
    private Entity attackingEntity;
    private Enemy attackingEnemy;
    private boolean moveMode;
    private Entity movingEntity;
    private Enemy movingEnemy;
    private ContextMenu currentMenu;
    private Entity currentMenuEntity;
    private Enemy currentMenuEnemy;

    public BattleGridCanvas(BattleGrid grid, TurnManager tm, BattleView battleView) {
        this.canvas = new Canvas();
        this.grid = grid;
        this.turnManager = tm;
        this.battleView = battleView;
        this.selectedObject = null;
        this.battleStarted = false;
        this.attackMode = false;
        this.attackingEntity = null;
        this.attackingEnemy = null;
        this.moveMode = false;
        this.movingEntity = null;
        this.movingEnemy = null;
        this.currentMenu = null;
        this.currentMenuEntity = null;
        this.currentMenuEnemy = null;

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
                        closeContextMenu();
                        showContextMenu(obj, e.getScreenX(), e.getScreenY());
                        return;
                    }
                }
            }
            closeContextMenu();
            handleLeftClick(e);
        }
    }

    private void closeContextMenu() {
        if (currentMenu != null) {
            currentMenu.hide();
            currentMenu = null;
            currentMenuEntity = null;
            currentMenuEnemy = null;
        }
    }

    private void handleKeyPressed(javafx.scene.input.KeyEvent e) {
        if (currentMenu != null && currentMenuEntity != null) {
            Entity entity = currentMenuEntity;
            switch (e.getCode()) {
                case F -> {
                    closeContextMenu();
                    triggerMove(entity);
                    e.consume();
                }
                case E -> {
                    closeContextMenu();
                    triggerAttack(entity);
                    e.consume();
                }
                case R -> {
                    closeContextMenu();
                    triggerUseItem(entity);
                    e.consume();
                }
                default -> {}
            }
        } else if (currentMenu != null && currentMenuEnemy != null) {
            Enemy enemy = currentMenuEnemy;
            switch (e.getCode()) {
                case F -> {
                    closeContextMenu();
                    triggerEnemyMove(enemy);
                    e.consume();
                }
                case E -> {
                    closeContextMenu();
                    triggerEnemyAttack(enemy);
                    e.consume();
                }
                default -> {}
            }
        }
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
        showAlert(Alert.AlertType.INFORMATION, "Attack Mode",
                "Left-click an enemy or terrain to attack with " + entity.getName() +
                "\nAttack Power: " + entity.getAttackPower());
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
            showAlert(Alert.AlertType.INFORMATION, "Item Used",
                    entity.getName() + " healed " + consumable.getHealAmount() + " HP!");
        }
        if (consumable.getEffect() != null) {
            entity.getCharSheet().addStatus(consumable.getEffect());
        }
        if (consumable.getQuantity() > 1) {
            consumable.decQuantity();
        } else {
            entity.getCharSheet().getInventory().remove(consumable);
        }
        entity.getCharSheet().save();
        redraw();
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
        showAlert(Alert.AlertType.INFORMATION, "Attack Mode",
                "Left-click a target to attack with " + enemy.getName() +
                "\nAttack Power: " + enemy.getAttackPower());
        redraw();
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

    private void showContextMenu(GridObject obj, double screenX, double screenY) {
        closeContextMenu();
        ContextMenu menu = new ContextMenu();
        currentMenu = menu;
        currentMenuEntity = (obj instanceof Entity e) ? e : null;
        currentMenuEnemy = (obj instanceof Enemy en) ? en : null;

        // Handle keyboard shortcuts on the menu
        menu.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, this::handleKeyPressed);

        addInfoSection(menu, obj);
        menu.getItems().add(new SeparatorMenuItem());
        addActionSection(menu, obj);

        menu.show(this, screenX, screenY);
    }

    private MenuItem createLabel(String text) {
        MenuItem item = new MenuItem(text);
        item.setDisable(true);
        return item;
    }

    private void addInfoSection(ContextMenu menu, GridObject obj) {
        if (obj instanceof Enemy enemy) {
            menu.getItems().add(createLabel("=== Enemy ==="));
            menu.getItems().add(createLabel("Name: " + enemy.getName()));
            menu.getItems().add(createLabel("HP: " + enemy.getHealth() + "/" + enemy.getMaxHealth()));
            menu.getItems().add(createLabel("Mobility: " + enemy.getMovement()));
            menu.getItems().add(createLabel("Attack: " + enemy.getAttackPower()));
        } else if (obj instanceof Entity e) {
            menu.getItems().add(createLabel("Name: " + e.getName()));
            menu.getItems().add(createLabel("HP: " + e.getHealth() + "/" + e.getCharSheet().getTotalHP()));
            menu.getItems().add(createLabel("Move: " + e.getMovement()));

            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().add(createLabel("Attributes:"));
            menu.getItems().add(createLabel("STR: " + e.getCharSheet().getTotalAttribute(0)));
            menu.getItems().add(createLabel("DEX: " + e.getCharSheet().getTotalAttribute(1)));
            menu.getItems().add(createLabel("ITV: " + e.getCharSheet().getTotalAttribute(2)));
            menu.getItems().add(createLabel("MOB: " + e.getCharSheet().getTotalAttribute(3)));

            Status[] statuses = e.getCharSheet().getStatus();
            if (statuses.length > 0) {
                menu.getItems().add(new SeparatorMenuItem());
                menu.getItems().add(createLabel("Status Effects:"));
                for (Status s : statuses) {
                    menu.getItems().add(createLabel("- " + s.getName()));
                }
            }

            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().add(createLabel("Equipment:"));
            Weapon primary = e.getCharSheet().getEquippedWeapon();
            Weapon secondary = e.getCharSheet().getEquippedSecondary();
            menu.getItems().add(createLabel("Primary: " + (primary != null ? primary.getName() : "None")));
            menu.getItems().add(createLabel("Secondary: " + (secondary != null ? secondary.getName() : "None")));

            Armor head = e.getCharSheet().getHead();
            Armor torso = e.getCharSheet().getTorso();
            Armor legs = e.getCharSheet().getLegs();
            menu.getItems().add(createLabel("Head: " + (head != null ? head.getName() : "None")));
            menu.getItems().add(createLabel("Torso: " + (torso != null ? torso.getName() : "None")));
            menu.getItems().add(createLabel("Legs: " + (legs != null ? legs.getName() : "None")));

            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().add(createLabel("Inventory: " + e.getCharSheet().getInventory().size() + " items"));
            menu.getItems().add(createLabel("Wallet: " + e.getCharSheet().getWallet() + " gold"));
        }

        if (obj instanceof TerrainObject t) {
            menu.getItems().add(createLabel("Terrain"));
            menu.getItems().add(createLabel("HP: " + t.getHealth()));
            menu.getItems().add(createLabel("Blocks Movement: " + t.blocksMovement()));
        }

        if (obj instanceof Pickup p) {
            menu.getItems().add(createLabel("Item: " + p.getItem().getName()));
            menu.getItems().add(createLabel("Type: " + p.getItem().getType()));
            menu.getItems().add(createLabel("Quantity: " + p.getItem().getQuantity()));
        }
    }

    private void addActionSection(ContextMenu menu, GridObject obj) {
        // Pickup actions
        if (obj instanceof Pickup p && selectedObject instanceof Entity entity && entity.isParty()) {
            MenuItem pickup = new MenuItem("Pick Up");
            pickup.setOnAction(ev -> {
                entity.pickup(p);
                grid.removePickup(p);
                redraw();
            });
            menu.getItems().add(pickup);
        }

        // Terrain actions
        if (obj instanceof TerrainObject t && selectedObject instanceof Entity e) {
            MenuItem attack = new MenuItem("Attack Terrain");
            attack.setOnAction(ev -> {
                t.takeDamage(e.getAttackPower());
                if (t.isDestroyed()) {
                    grid.removeDestroyedTerrain();
                }
                redraw();
            });
            menu.getItems().add(attack);
        }

        // Entity vs Entity actions
        if (obj instanceof Entity target && selectedObject instanceof Entity attacker && target != attacker) {
            MenuItem attack = new MenuItem("Attack");
            attack.setOnAction(ev -> {
                attacker.attack(target);
                if (target.isDead()) {
                    grid.removeEntity(target);
                    turnManager.removeEntity(target);
                }
                redraw();
            });
            menu.getItems().add(attack);
        }

        // Direct entity commands
        if (obj instanceof Entity e) {
            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().add(createLabel("=== Commands ==="));

            MenuItem moveCmd = new MenuItem("Move (F)");
            moveCmd.setOnAction(ev -> {
                triggerMove(e);
                closeContextMenu();
            });
            menu.getItems().add(moveCmd);

            MenuItem attackCmd = new MenuItem("Attack (E)");
            attackCmd.setOnAction(ev -> {
                triggerAttack(e);
                closeContextMenu();
            });
            menu.getItems().add(attackCmd);

            MenuItem itemCmd = new MenuItem("Item (R)");
            itemCmd.setOnAction(ev -> {
                closeContextMenu();
                triggerUseItem(e);
            });
            menu.getItems().add(itemCmd);

            MenuItem swapWeapons = new MenuItem("Swap Weapons");
            swapWeapons.setOnAction(ev -> {
                e.getCharSheet().swapWeapons();
                redraw();
            });
            menu.getItems().add(swapWeapons);

            Pickup standingOn = grid.getPickupAt(e.getRow(), e.getCol());
            if (standingOn != null && e.isParty()) {
                MenuItem pickupCmd = new MenuItem("Pick Up " + standingOn.getItem().getName());
                pickupCmd.setOnAction(ev -> {
                    e.pickup(standingOn);
                    grid.removePickup(standingOn);
                    redraw();
                });
                menu.getItems().add(pickupCmd);
            }

            if (!battleStarted) {
                menu.getItems().add(new SeparatorMenuItem());
                MenuItem removeEntity = new MenuItem("Remove Entity");
                removeEntity.setOnAction(ev -> {
                    grid.removeEntity(e);
                    battleView.removeEntity(e);
                    selectedObject = null;
                    redraw();
                });
                menu.getItems().add(removeEntity);
            }
        }

        // Direct enemy commands
        if (obj instanceof Enemy enemy) {
            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().add(createLabel("=== Commands ==="));

            MenuItem moveCmd = new MenuItem("Move (F)");
            moveCmd.setOnAction(ev -> {
                triggerEnemyMove(enemy);
                closeContextMenu();
            });
            menu.getItems().add(moveCmd);

            MenuItem attackCmd = new MenuItem("Attack (E)");
            attackCmd.setOnAction(ev -> {
                triggerEnemyAttack(enemy);
                closeContextMenu();
            });
            menu.getItems().add(attackCmd);

            if (!battleStarted) {
                menu.getItems().add(new SeparatorMenuItem());
                MenuItem removeEnemy = new MenuItem("Remove Enemy");
                removeEnemy.setOnAction(ev -> {
                    grid.removeEnemy(enemy);
                    battleView.removeEnemy(enemy);
                    selectedObject = null;
                    redraw();
                });
                menu.getItems().add(removeEnemy);
            }
        }
    }

    private void handleLeftClick(MouseEvent e) {
        int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
        if (cellInfo == null) return;

        int row = cellInfo[0];
        int col = cellInfo[1];

        if (!grid.inBounds(row, col)) return;

        GridObject clicked = grid.getObjectAt(row, col);

        // Handle move mode for Entity
        if (moveMode && movingEntity != null) {
            if (clicked == null && !grid.isBlocked(row, col)) {
                int dist = Math.abs(movingEntity.getRow() - row) + Math.abs(movingEntity.getCol() - col);
                int mobilityLimit = movingEntity.getCharSheet().getTotalAttribute(3);

                if (dist <= mobilityLimit) {
                    movingEntity.setRow(row);
                    movingEntity.setCol(col);
                    moveMode = false;
                    movingEntity = null;
                    selectedObject = null;
                    redraw();
                    return;
                }
            }
            if (clicked != null) {
                moveMode = false;
                movingEntity = null;
                selectedObject = clicked;
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
                    movingEnemy.setRow(row);
                    movingEnemy.setCol(col);
                    moveMode = false;
                    movingEnemy = null;
                    selectedObject = null;
                    redraw();
                    return;
                }
            }
            if (clicked != null) {
                moveMode = false;
                movingEnemy = null;
                selectedObject = clicked;
                redraw();
                return;
            }
        }

        // Handle attack mode for Entity
        if (attackMode && attackingEntity != null) {
            if (clicked instanceof Entity target && target != attackingEntity) {
                attackingEntity.attack(target);
                showAlert(Alert.AlertType.INFORMATION, "Attack",
                        attackingEntity.getName() + " attacks " + target.getName() + "!" +
                        "\nDamage dealt: " + Math.max(0, attackingEntity.getAttackPower() - target.getDefense()) +
                        "\n" + target.getName() + " HP: " + target.getHealth() + "/" + target.getCharSheet().getTotalHP());
                if (target.isDead()) {
                    grid.removeEntity(target);
                    turnManager.removeEntity(target);
                    showAlert(Alert.AlertType.WARNING, "Defeated", target.getName() + " has been defeated!");
                }
                attackMode = false;
                attackingEntity = null;
                redraw();
                return;
            } else if (clicked instanceof Enemy targetEnemy) {
                int damage = Math.max(0, attackingEntity.getAttackPower() - targetEnemy.getDefense());
                targetEnemy.takeDamage(damage);
                showAlert(Alert.AlertType.INFORMATION, "Attack",
                        attackingEntity.getName() + " attacks " + targetEnemy.getName() + "!" +
                        "\nDamage dealt: " + damage +
                        "\n" + targetEnemy.getName() + " HP: " + targetEnemy.getHealth() + "/" + targetEnemy.getMaxHealth());
                if (targetEnemy.isDead()) {
                    grid.removeEnemy(targetEnemy);
                    turnManager.removeEnemy(targetEnemy);
                    showAlert(Alert.AlertType.WARNING, "Defeated", targetEnemy.getName() + " has been defeated!");
                }
                attackMode = false;
                attackingEntity = null;
                redraw();
                return;
            } else if (clicked instanceof TerrainObject terrain) {
                terrain.takeDamage(attackingEntity.getAttackPower());
                showAlert(Alert.AlertType.INFORMATION, "Attack",
                        attackingEntity.getName() + " attacks the terrain!" +
                        "\nTerrain HP: " + terrain.getHealth());
                if (terrain.isDestroyed()) {
                    grid.removeDestroyedTerrain();
                }
                attackMode = false;
                attackingEntity = null;
                redraw();
                return;
            } else if (clicked == null) {
                attackMode = false;
                attackingEntity = null;
                redraw();
                return;
            }
        }

        // Handle attack mode for Enemy
        if (attackMode && attackingEnemy != null) {
            if (clicked instanceof Entity target) {
                attackingEnemy.attack(target);
                showAlert(Alert.AlertType.INFORMATION, "Attack",
                        attackingEnemy.getName() + " attacks " + target.getName() + "!" +
                        "\nDamage dealt: " + Math.max(0, attackingEnemy.getAttackPower() - target.getDefense()) +
                        "\n" + target.getName() + " HP: " + target.getHealth() + "/" + target.getCharSheet().getTotalHP());
                if (target.isDead()) {
                    grid.removeEntity(target);
                    turnManager.removeEntity(target);
                    showAlert(Alert.AlertType.WARNING, "Defeated", target.getName() + " has been defeated!");
                }
                attackMode = false;
                attackingEnemy = null;
                redraw();
                return;
            } else if (clicked instanceof Enemy targetEnemy && targetEnemy != attackingEnemy) {
                attackingEnemy.attack(targetEnemy);
                showAlert(Alert.AlertType.INFORMATION, "Attack",
                        attackingEnemy.getName() + " attacks " + targetEnemy.getName() + "!" +
                        "\nDamage dealt: " + attackingEnemy.getAttackPower() +
                        "\n" + targetEnemy.getName() + " HP: " + targetEnemy.getHealth() + "/" + targetEnemy.getMaxHealth());
                if (targetEnemy.isDead()) {
                    grid.removeEnemy(targetEnemy);
                    turnManager.removeEnemy(targetEnemy);
                    showAlert(Alert.AlertType.WARNING, "Defeated", targetEnemy.getName() + " has been defeated!");
                }
                attackMode = false;
                attackingEnemy = null;
                redraw();
                return;
            } else if (clicked instanceof TerrainObject terrain) {
                terrain.takeDamage(attackingEnemy.getAttackPower());
                showAlert(Alert.AlertType.INFORMATION, "Attack",
                        attackingEnemy.getName() + " attacks the terrain!" +
                        "\nTerrain HP: " + terrain.getHealth());
                if (terrain.isDestroyed()) {
                    grid.removeDestroyedTerrain();
                }
                attackMode = false;
                attackingEnemy = null;
                redraw();
                return;
            } else if (clicked == null) {
                attackMode = false;
                attackingEnemy = null;
                redraw();
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

            gc.setStroke(Color.rgb(0, 200, 0));
            gc.setLineWidth(2);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int dist = Math.abs(entityRow - r) + Math.abs(entityCol - c);
                    if (dist > 0 && dist <= mobilityLimit && !grid.isBlocked(r, c)) {
                        double x = offsetX + c * cellSize;
                        double y = offsetY + r * cellSize;
                        gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    }
                }
            }
            gc.setLineWidth(1);
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

            gc.setStroke(Color.rgb(0, 200, 0));
            gc.setLineWidth(2);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int dist = Math.abs(entityRow - r) + Math.abs(entityCol - c);
                    if (dist > 0 && dist <= mobilityLimit && !grid.isBlocked(r, c)) {
                        double x = offsetX + c * cellSize;
                        double y = offsetY + r * cellSize;
                        gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    }
                }
            }
            gc.setLineWidth(1);
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

        // Terrain objects - small black circles
        for (TerrainObject t : grid.getTerrainObjects()) {
            double x = offsetX + t.getCol() * cellSize;
            double y = offsetY + t.getRow() * cellSize;
            double circleSize = cellSize * 0.4;
            double offset = (cellSize - circleSize) / 2;
            gc.setFill(Color.BLACK);
            gc.fillOval(x + offset, y + offset, circleSize, circleSize);

            if (t == selectedObject) {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(2);
                gc.strokeOval(x + offset - 2, y + offset - 2, circleSize + 4, circleSize + 4);
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
            java.awt.Color awtColor = e.getCharSheet().getDisplayColor();
            gc.setFill(Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));

            double x = offsetX + e.getCol() * cellSize;
            double y = offsetY + e.getRow() * cellSize;
            gc.fillOval(x + 4, y + 4, cellSize - 8, cellSize - 8);

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
            Color enemyColor = getEnemyColor(en.getColor());
            gc.setFill(enemyColor);

            double x = offsetX + en.getCol() * cellSize;
            double y = offsetY + en.getRow() * cellSize;
            // Draw enemies as diamonds/squares rotated 45 degrees to distinguish from entities
            gc.fillRect(x + 4, y + 4, cellSize - 8, cellSize - 8);

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
