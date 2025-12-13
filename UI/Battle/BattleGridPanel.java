package UI.Battle;

import Objects.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BattleGridPanel extends JPanel {

    private final BattleGrid grid;
    private final TurnManager turnManager;

    public BattleGridPanel(BattleGrid grid, TurnManager tm) {
        this.grid = grid;
        this.turnManager = tm;
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
            }
        });
    }

    private void handleClick(MouseEvent e) {
        int cellW = getWidth() / grid.getCols();
        int cellH = getHeight() / grid.getRows();

        int col = e.getX() / cellW;
        int row = e.getY() / cellH;

        if (!grid.inBounds(row, col)) {
            return;
        }

        Entity current = turnManager.getCurrent();

        // Attack terrain if present
        TerrainObject terrain = grid.getTerrainAt(row, col);
        if (terrain != null && current instanceof Player c) {
            int damage = c.getAttackDamage();
            terrain.takeDamage(damage);
            grid.removeDestroyedTerrain();
            repaint();
            return;
        }

        // Movement
        if (!grid.isBlocked(row, col)) {
            int dist = Math.abs(current.getRow() - row)
                    + Math.abs(current.getCol() - col);

            if (dist <= current.getMovement()) {
                current.moveTo(row, col);
                turnManager.nextTurn();
                repaint();
            }
        }
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
        }
    }
}
