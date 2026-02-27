package UI.Battle;

import UI.IconUtils;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A scrollable combat log that records all combat actions.
 */
public class CombatLogPane extends VBox {

    private final VBox logContent;
    private final ScrollPane scrollPane;
    private int entryCount = 0;

    public enum LogType {
        ATTACK("#b8860b"),    // Gold
        DAMAGE("#F44336"),    // Red
        HEAL("#4CAF50"),      // Green
        INFO("#569cd6"),      // Blue
        DEFEAT("#d75f5f"),    // Light red
        ROUND("#9c9cff");     // Purple

        private final String color;

        LogType(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    public CombatLogPane() {
        getStyleClass().addAll("card");
        setStyle("-fx-background-color: linear-gradient(to bottom, #2d2d30, #252528);");
        setPadding(new Insets(12));
        setSpacing(8);
        setPrefWidth(260);
        setMinWidth(200);
        setMaxWidth(300);

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().add(IconUtils.createIcon(IconUtils.Icon.SWORDS, 18, "#569cd6"));
        
        Label header = new Label("Combat Log");
        header.getStyleClass().add("label-header");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        
        // Add subtle glow to header
        DropShadow headerGlow = new DropShadow();
        headerGlow.setColor(Color.web("#569cd680"));
        headerGlow.setRadius(6);
        headerGlow.setSpread(0.2);
        header.setEffect(headerGlow);
        headerBox.getChildren().add(header);

        logContent = new VBox(4);
        logContent.setPadding(new Insets(8));
        logContent.setStyle("-fx-background-color: #1e1e20; -fx-background-radius: 6;");

        scrollPane = new ScrollPane(logContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(400);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().addAll(headerBox, scrollPane);
    }

    /**
     * Logs a general message.
     */
    public void log(String message, LogType type) {
        entryCount++;
        Label entry = new Label(message);
        entry.setWrapText(true);
        entry.setMaxWidth(Double.MAX_VALUE);
        entry.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-size: 11px; -fx-padding: 4 8 4 8;",
            type.getColor()
        ));

        // Alternate background with rounded corners
        if (entryCount % 2 == 0) {
            entry.setStyle(entry.getStyle() + "-fx-background-color: #2a2a2d; -fx-background-radius: 4;");
        }

        logContent.getChildren().add(entry);

        // Highlight animation for new entry
        highlightNewEntry(entry, type);

        // Auto-scroll to bottom
        scrollPane.applyCss();
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

    /**
     * Animates a brief highlight on new log entries.
     */
    private void highlightNewEntry(Label entry, LogType type) {
        String baseStyle = entry.getStyle();
        String highlightBg = "-fx-background-color: " + type.getColor() + "33; -fx-background-radius: 3;";
        
        entry.setStyle(baseStyle + highlightBg);
        
        PauseTransition hold = new PauseTransition(Duration.millis(600));
        hold.setOnFinished(e -> {
            // Fade back to normal
            Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(entry.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(100), evt -> entry.setStyle(baseStyle)),
                new KeyFrame(Duration.millis(300), new KeyValue(entry.opacityProperty(), 1.0))
            );
            fadeOut.play();
        });
        hold.play();
    }

    /**
     * Logs a new round starting.
     */
    public void logRound(int round) {
        Label divider = new Label("━━━ Round " + round + " ━━━");
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle("-fx-text-fill: #9c9cff; -fx-font-size: 12px; -fx-font-weight: bold; " +
                        "-fx-alignment: center; -fx-padding: 5 0 5 0;");
        logContent.getChildren().add(divider);
        scrollToBottom();
    }

    /**
     * Logs an attack action.
     */
    public void logAttack(String attacker, String target, int damage, int remainingHp, int maxHp) {
        String hpStatus = remainingHp + "/" + maxHp;
        String message = String.format("⚔ %s attacks %s for %d damage [%s HP]", 
            attacker, target, damage, hpStatus);
        
        LogType type = remainingHp <= maxHp * 0.25 ? LogType.DAMAGE : LogType.ATTACK;
        log(message, type);
    }

    /**
     * Logs a defeat.
     */
    public void logDefeat(String name) {
        log("💀 " + name + " defeated!", LogType.DEFEAT);
    }

    /**
     * Logs healing.
     */
    public void logHeal(String name, int amount) {
        log("💚 " + name + " healed " + amount + " HP", LogType.HEAL);
    }

    /**
     * Logs a turn start.
     */
    public void logTurnStart(String name) {
        log("► " + name + "'s turn", LogType.INFO);
    }

    /**
     * Logs terrain damage.
     */
    public void logTerrainDamage(String attacker, int damage, int remainingHp) {
        log(String.format("🪨 %s hits terrain for %d [%d HP left]", attacker, damage, remainingHp), LogType.ATTACK);
    }

    /**
     * Logs terrain destruction.
     */
    public void logTerrainDestroyed() {
        log("💥 Terrain destroyed!", LogType.DAMAGE);
    }

    /**
     * Logs item usage.
     */
    public void logItemUse(String user, String item) {
        log("🧪 " + user + " uses " + item, LogType.INFO);
    }

    /**
     * Clears the combat log.
     */
    public void clear() {
        logContent.getChildren().clear();
        entryCount = 0;
    }

    private void scrollToBottom() {
        scrollPane.applyCss();
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }
}
