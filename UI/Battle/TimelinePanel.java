package UI.Battle;

import Objects.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TimelinePanel extends JPanel {

    private final TurnManager turnManager;
    private static final int TIMELINE_HEIGHT = 80;
    private static final int PADDING = 10;

    public TimelinePanel(TurnManager turnManager) {
        this.turnManager = turnManager;
        setPreferredSize(new Dimension(0, TIMELINE_HEIGHT));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Entity> turnOrder = turnManager.getTurnOrder();
        if (turnOrder.isEmpty()) {
            drawEmptyTimeline(g2);
            return;
        }

        int round = turnManager.getRound();
        int currentIndex = turnManager.getCurrentIndex();

        // Draw round number
        String roundText = "Round " + round;
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();
        int roundWidth = fm.stringWidth(roundText);
        g2.drawString(roundText, PADDING, PADDING + fm.getAscent());

        // Calculate available space for timeline
        int availableWidth = getWidth() - PADDING * 2 - roundWidth - PADDING;
        int entitySize = Math.max(80, availableWidth / Math.max(1, turnOrder.size()));
        int startX = PADDING + roundWidth + PADDING;
        int startY = PADDING + 5;

        // Draw timeline background
        g2.setColor(new Color(200, 200, 200));
        int timelineWidth = Math.min(entitySize * turnOrder.size(), availableWidth);
        g2.fillRect(startX - 2, startY - 2, timelineWidth + 4, 55);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(startX - 2, startY - 2, timelineWidth + 4, 55);

        // Draw entities in turn order
        for (int i = 0; i < turnOrder.size(); i++) {
            Entity entity = turnOrder.get(i);
            int x = startX + (i * entitySize);
            int y = startY;

            // Highlight current entity
            if (i == currentIndex) {
                g2.setColor(new Color(255, 215, 0)); // Gold
                g2.fillRect(x, y, entitySize, 50);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(x, y, entitySize, 50);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRect(x, y, entitySize, 50);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));
                g2.drawRect(x, y, entitySize, 50);
            }

            // Draw entity name with word wrapping
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            String name = entity.getName();
            FontMetrics fmName = g2.getFontMetrics();
            
            // Center name in box, with potential for two lines
            String[] words = name.split("\\s+");
            if (words.length == 1 && fmName.stringWidth(name) > entitySize - 4) {
                // Single word too long - truncate with ellipsis
                String truncated = name;
                while (truncated.length() > 1 && fmName.stringWidth(truncated + "...") > entitySize - 4) {
                    truncated = truncated.substring(0, truncated.length() - 1);
                }
                name = truncated + (truncated.length() < entity.getName().length() ? "..." : "");
            }
            
            int nameY = y + ((50 - fmName.getHeight()) / 2) + fmName.getAscent();
            int nameX = x + ((entitySize - fmName.stringWidth(name)) / 2);
            g2.drawString(name, nameX, nameY);
        }
    }

    private void drawEmptyTimeline(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.ITALIC, 14));
        String emptyText = "No entities on field. Add objects to begin.";
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(emptyText)) / 2;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(emptyText, textX, textY);
    }
}
