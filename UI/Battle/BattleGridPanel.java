package UI.Battle;

import Objects.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BattleGridPanel extends JPanel {

    private final BattleGrid grid;
    private final TurnManager turnManager;
    private GridObject selectedObject;

    public BattleGridPanel(BattleGrid grid, TurnManager tm) {
        selectedObject = null;
        this.grid = grid;
        this.turnManager = tm;
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

    private void handleRightClick(MouseEvent e) {
        int cellW = getWidth() / grid.getCols();
        int cellH = getHeight() / grid.getRows();

        int col = e.getX() / cellW;
        int row = e.getY() / cellH;

        if (!grid.inBounds(row, col)) {
            return;
        }

        GridObject obj = grid.getObjectAt(row, col);
        if (obj == null) {
            return;
        }

        showContextMenu(obj, e.getX(), e.getY());
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
            menu.add(label("HP: " + e.getHealth()));
            menu.add(label("Move: " + e.getMovement()));
        }

        if (obj instanceof TerrainObject t) {
            menu.add(label("Terrain"));
            menu.add(label("HP: " + t.getHealth()));
        }

        if (obj instanceof Pickup p) {
            menu.add(label("Item: " + p.getItem().getName()));
        }
    }

    private JMenuItem label(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setEnabled(false);
        return item;
    }

    private void addActionSection(JPopupMenu menu, GridObject obj) {

        if (obj instanceof Pickup p && selectedObject instanceof Player player) {
            JMenuItem pickup = new JMenuItem("Pick Up");
            pickup.addActionListener(ev -> {
                player.pickup(p);
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
    }

    private void handleLeftClick(MouseEvent e) {
        int cellW = getWidth() / grid.getCols();
        int cellH = getHeight() / grid.getRows();

        int col = e.getX() / cellW;
        int row = e.getY() / cellH;

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

        // Terrain objects usually shouldn't move
        if (obj instanceof TerrainObject) {
            return;
        }

        // Blocked destination
        if (grid.isBlocked(row, col)) {
            return;
        }

        // Movement rules apply only to entities
        if (obj instanceof Entity e) {
            int dist = Math.abs(e.getRow() - row)
                    + Math.abs(e.getCol() - col);

            if (dist > e.getMovement()) {
                return;
            }
        }

        obj.setRow(row);
        obj.setCol(col);
    }

    public void pickupAction() {
        Entity current = turnManager.getCurrent();
        if (!(current instanceof Player c)) {
            return;
        }

        Pickup p = grid.getPickupAt(c.getRow(), c.getCol());
        if (p == null) {
            return;
        }

        c.pickup(p);
        grid.removePickup(p);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int rows = grid.getRows();
        int cols = grid.getCols();
        int w = getWidth() / cols;
        int h = getHeight() / rows;

        // Grid lines
        g2.setColor(Color.GRAY);
        for (int r = 0; r <= rows; r++) {
            g2.drawLine(0, r * h, getWidth(), r * h);
        }
        for (int c = 0; c <= cols; c++) {
            g2.drawLine(c * w, 0, c * w, getHeight());
        }

        // Terrain objects
        for (TerrainObject t : grid.getTerrainObjects()) {
            int x = t.getCol() * w;
            int y = t.getRow() * h;
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x + 2, y + 2, w - 4, h - 4);

            if (t == selectedObject) {
                g2.setColor(Color.YELLOW);
                g2.drawRect(x + 1, y + 1, w - 2, h - 2);
            }
        }

        // Pickups
        for (Pickup p : grid.getPickups()) {
            int x = p.getCol() * w;
            int y = p.getRow() * h;
            g2.setColor(Color.ORANGE);
            g2.fillOval(x + w / 4, y + h / 4, w / 2, h / 2);
        }

        // Entities
        for (Entity e : grid.getEntities()) {
            if (turnManager.isCurrent(e)) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.BLUE);
            }

            int x = e.getCol() * w;
            int y = e.getRow() * h;
            g2.fillOval(x + 4, y + 4, w - 8, h - 8);

            if (e == selectedObject) {
                g2.setColor(Color.YELLOW);
                g2.drawOval(x + 2, y + 2, w - 4, h - 4);
            }

        }
    }
}
