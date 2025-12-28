package UI.Battle;

import EntityRes.*;
import Objects.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BattleGridPanel extends JPanel {

    private final BattleGrid grid;
    private final TurnManager turnManager;
    private final BattleSystem battleSystem;
    private GridObject selectedObject;
    private boolean battleStarted;

    public BattleGridPanel(BattleGrid grid, TurnManager tm, BattleSystem battleSystem) {
        selectedObject = null;
        this.grid = grid;
        this.turnManager = tm;
        this.battleSystem = battleSystem;
        this.battleStarted = false;
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick(e);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    handleLeftClick(e);
                }
            }
        });

    }
    
    public void setBattleStarted(boolean started) {
        this.battleStarted = started;
    }

    private void handleRightClick(MouseEvent e) {
        int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
        if (cellInfo == null) {
            return;
        }

        int row = cellInfo[0];
        int col = cellInfo[1];

        if (!grid.inBounds(row, col)) {
            return;
        }

        GridObject obj = grid.getObjectAt(row, col);
        if (obj == null) {
            return;
        }

        showContextMenu(obj, e.getX(), e.getY());
    }

    private int[] getCellAtPoint(int mouseX, int mouseY) {
        int rows = grid.getRows();
        int cols = grid.getCols();
        int cellSize = Math.min(getWidth() / cols, getHeight() / rows);

        int gridWidth = cellSize * cols;
        int gridHeight = cellSize * rows;
        int offsetX = (getWidth() - gridWidth) / 2;
        int offsetY = (getHeight() - gridHeight) / 2;

        if (mouseX < offsetX || mouseY < offsetY || mouseX > offsetX + gridWidth || mouseY > offsetY + gridHeight) {
            return null;
        }

        int col = (mouseX - offsetX) / cellSize;
        int row = (mouseY - offsetY) / cellSize;
        return new int[]{row, col};
    }

    private void showContextMenu(GridObject obj, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        addInfoSection(menu, obj);
        menu.addSeparator();
        addActionSection(menu, obj);

        menu.show(this, x, y);
    }

    private void addInfoSection(JPopupMenu menu, GridObject obj) {

        if (obj instanceof Entity e) {
            menu.add(label("Name: " + e.getName()));
            menu.add(label("HP: " + e.getHealth() + "/" + e.getCharSheet().getTotalHP()));
            menu.add(label("Move: " + e.getMovement()));

            // Add attributes
            menu.addSeparator();
            menu.add(label("Attributes:"));
            menu.add(label("STR: " + e.getCharSheet().getTotalAttribute(0)));
            menu.add(label("DEX: " + e.getCharSheet().getTotalAttribute(1)));
            menu.add(label("ITV: " + e.getCharSheet().getTotalAttribute(2)));
            menu.add(label("MOB: " + e.getCharSheet().getTotalAttribute(3)));

            // Add status effects
            Status[] statuses = e.getCharSheet().getStatus();
            if (statuses.length > 0) {
                menu.addSeparator();
                menu.add(label("Status Effects:"));
                for (Status s : statuses) {
                    menu.add(label("- " + s.getName()));
                }
            }

            // Add equipment
            menu.addSeparator();
            menu.add(label("Equipment:"));
            Weapon primary = e.getCharSheet().getEquippedWeapon();
            Weapon secondary = e.getCharSheet().getEquippedSecondary();
            menu.add(label("Primary: " + (primary != null ? primary.getName() : "None")));
            menu.add(label("Secondary: " + (secondary != null ? secondary.getName() : "None")));

            Armor head = e.getCharSheet().getHead();
            Armor torso = e.getCharSheet().getTorso();
            Armor legs = e.getCharSheet().getLegs();
            menu.add(label("Head: " + (head != null ? head.getName() : "None")));
            menu.add(label("Torso: " + (torso != null ? torso.getName() : "None")));
            menu.add(label("Legs: " + (legs != null ? legs.getName() : "None")));

            // Add inventory count
            menu.addSeparator();
            menu.add(label("Inventory: " + e.getCharSheet().getInventory().size() + " items"));
            menu.add(label("Wallet: " + e.getCharSheet().getWallet() + " gold"));
        }

        if (obj instanceof TerrainObject t) {
            menu.add(label("Terrain"));
            menu.add(label("HP: " + t.getHealth()));
            menu.add(label("Blocks Movement: " + t.blocksMovement()));
        }

        if (obj instanceof Pickup p) {
            menu.add(label("Item: " + p.getItem().getName()));
            menu.add(label("Type: " + p.getItem().getType()));
            menu.add(label("Quantity: " + p.getItem().getQuantity()));
        }
    }

    private JMenuItem label(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setEnabled(false);
        return item;
    }

    private void addActionSection(JPopupMenu menu, GridObject obj) {

        if (obj instanceof Pickup p && selectedObject instanceof Entity entity && entity.isParty()) {
            JMenuItem pickup = new JMenuItem("Pick Up");
            pickup.addActionListener(ev -> {
                entity.pickup(p);
                grid.removePickup(p);
                repaint();
            });
            menu.add(pickup);
        }

        if (obj instanceof TerrainObject t && selectedObject instanceof Entity e) {
            JMenuItem attack = new JMenuItem("Attack Terrain");
            attack.addActionListener(ev -> {
                t.takeDamage(e.getAttackPower());
                if (t.isDestroyed()) {
                    grid.removeDestroyedTerrain();
                }
                repaint();
            });
            menu.add(attack);
        }

        if (obj instanceof Entity target
                && selectedObject instanceof Entity attacker
                && target != attacker) {

            JMenuItem attack = new JMenuItem("Attack");
            attack.addActionListener(ev -> {
                attacker.attack(target);
                if (target.isDead()) {
                    grid.removeEntity(target);
                }
                repaint();
            });
            menu.add(attack);
        }

        if (obj instanceof Entity e && selectedObject == e) {
            // Actions for the selected entity itself
            menu.addSeparator();

            JMenuItem swapWeapons = new JMenuItem("Swap Primary/Secondary Weapons");
            swapWeapons.addActionListener(ev -> {
                e.getCharSheet().swapWeapons();
                repaint();
            });
            menu.add(swapWeapons);

            // Use item submenu
            if (!e.getCharSheet().getInventory().isEmpty()) {
                JMenu useItemMenu = new JMenu("Use Item");
                for (Item item : e.getCharSheet().getInventory()) {
                    if (item.getType().equals("Consumable")) {
                        JMenuItem useItem = new JMenuItem(item.getName() + " (x" + item.getQuantity() + ")");
                        useItem.addActionListener(ev -> {
                            // For now, just remove one quantity. In a real game, you'd check what the item does
                            if (item.getName().equals("Health Potion")) {
                                e.getCharSheet().addCurrentHP(10); // Heal 10 HP
                                Consumable potion = new Consumable("Health Potion", "Consumable", 10, null);
                                e.getCharSheet().dropItem(potion);
                            }
                            repaint();
                        });
                        useItemMenu.add(useItem);
                    }
                }
                if (useItemMenu.getItemCount() > 0) {
                    menu.add(useItemMenu);
                }
            }
            
            // Remove entity option (only in pre-battle setup)
            if (!battleStarted) {
                menu.addSeparator();
                JMenuItem removeEntity = new JMenuItem("Remove Entity");
                removeEntity.addActionListener(ev -> {
                    grid.removeEntity(e);
                    battleSystem.removeEntity(e);
                    selectedObject = null;
                    repaint();
                });
                menu.add(removeEntity);
            }
        }
    }

    private void handleLeftClick(MouseEvent e) {
        int[] cellInfo = getCellAtPoint(e.getX(), e.getY());
        if (cellInfo == null) {
            return;
        }

        int row = cellInfo[0];
        int col = cellInfo[1];

        if (!grid.inBounds(row, col)) {
            return;
        }

        GridObject clicked = grid.getObjectAt(row, col);

        // 1. Selecting an object
        if (clicked != null) {
            selectedObject = clicked;
            repaint();
            return;
        }

        // 2. Moving selected object
        if (selectedObject != null) {
            attemptMove(selectedObject, row, col);
            repaint();
        }
    }

    private void attemptMove(GridObject obj, int row, int col) {

        // // Terrain objects usually shouldn't move
        // if (obj instanceof TerrainObject) {
        //     return;
        // }

        // Blocked destination
        if (grid.isBlocked(row, col)) {
            return;
        }

        // Movement rules apply only to entities
        if (obj instanceof Entity e) {
            int dist = Math.abs(e.getRow() - row)
                    + Math.abs(e.getCol() - col);

            // During battle, enforce mobility stat as movement limit
            if (battleStarted) {
                int mobilityLimit = e.getCharSheet().getTotalAttribute(3); // MOBILITY = 3
                if (dist > mobilityLimit) {
                    return;
                }
            } else {
                // Before battle, allow free movement
                // dist > e.getMovement() check removed for setup phase
            }
        }

        obj.setRow(row);
        obj.setCol(col);
    }

    public void pickupAction() {
        Entity current = turnManager.getCurrent();
        if (!current.isParty()) {
            return;
        }

        Pickup p = grid.getPickupAt(current.getRow(), current.getCol());
        if (p == null) {
            return;
        }

        current.pickup(p);
        grid.removePickup(p);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rows = grid.getRows();
        int cols = grid.getCols();

        // Calculate cell size as square, fitting to the smaller dimension
        int availWidth = getWidth();
        int availHeight = getHeight();
        int cellSize = Math.min(availWidth / cols, availHeight / rows);

        // Center the grid if there's extra space
        int gridWidth = cellSize * cols;
        int gridHeight = cellSize * rows;
        int offsetX = (availWidth - gridWidth) / 2;
        int offsetY = (availHeight - gridHeight) / 2;

        // Grid lines
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(1));
        for (int r = 0; r <= rows; r++) {
            g2.drawLine(offsetX, offsetY + r * cellSize, offsetX + gridWidth, offsetY + r * cellSize);
        }
        for (int c = 0; c <= cols; c++) {
            g2.drawLine(offsetX + c * cellSize, offsetY, offsetX + c * cellSize, offsetY + gridHeight);
        }

        // Terrain objects
        for (TerrainObject t : grid.getTerrainObjects()) {
            int x = offsetX + t.getCol() * cellSize;
            int y = offsetY + t.getRow() * cellSize;
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x + 2, y + 2, cellSize - 4, cellSize - 4);

            if (t == selectedObject) {
                g2.setColor(Color.ORANGE);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(1));
                g2.drawRect(x, y, cellSize, cellSize);
            }
        }

        // Pickups
        for (Pickup p : grid.getPickups()) {
            int x = offsetX + p.getCol() * cellSize;
            int y = offsetY + p.getRow() * cellSize;
            g2.setColor(Color.ORANGE);
            g2.fillOval(x + cellSize / 4, y + cellSize / 4, cellSize / 2, cellSize / 2);
        }

        // Entities
        for (Entity e : grid.getEntities()) {
            g2.setColor(e.getCharSheet().getDisplayColor());

            int x = offsetX + e.getCol() * cellSize;
            int y = offsetY + e.getRow() * cellSize;
            g2.fillOval(x + 4, y + 4, cellSize - 8, cellSize - 8);

            if (e == selectedObject) {
                g2.setColor(Color.ORANGE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(x + 2, y + 2, cellSize - 4, cellSize - 4);
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(1));
                g2.drawOval(x + 1, y + 1, cellSize - 2, cellSize - 2);
            }
        }
    }
}
