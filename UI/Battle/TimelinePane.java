package UI.Battle;

import Objects.Entity;
import Objects.Enemy;
import Objects.GridObject;
import UI.AnimationUtils;
import UI.IconUtils;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelinePane extends HBox {

    private final TurnManager turnManager;
    private final Label roundLabel;
    private final HBox entitiesBox;
    private final Map<GridObject, VBox> combatantBoxes = new HashMap<>();
    private VBox currentHighlightedBox = null;
    private boolean initialBuild = true;

    public TimelinePane(TurnManager turnManager) {
        this.turnManager = turnManager;
        
        getStyleClass().add("timeline-pane");
        setSpacing(15);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(80);
        setMinHeight(80);
        setMaxHeight(80);

        // Round label with icon
        HBox roundBox = new HBox(8);
        roundBox.setAlignment(Pos.CENTER_LEFT);
        roundBox.getChildren().add(IconUtils.createIcon(IconUtils.Icon.CLOCK, 20, "#b8860b"));
        
        roundLabel = new Label("Round 0");
        roundLabel.getStyleClass().add("label-title");
        roundLabel.setMinWidth(80);
        roundBox.getChildren().add(roundLabel);

        // Entities container
        entitiesBox = new HBox(5);
        entitiesBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(entitiesBox, Priority.ALWAYS);

        getChildren().addAll(roundBox, entitiesBox);
        
        refresh();
    }

    public void refresh() {
        List<GridObject> turnOrder = turnManager.getTurnOrder();
        
        if (turnOrder.isEmpty()) {
            entitiesBox.getChildren().clear();
            combatantBoxes.clear();
            currentHighlightedBox = null;
            Label emptyLabel = new Label("No combatants on field. Add objects to begin.");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #808080;");
            entitiesBox.getChildren().add(emptyLabel);
            roundLabel.setText("Round 0");
            initialBuild = true;
            return;
        }

        int round = turnManager.getRound();
        int currentIndex = turnManager.getCurrentIndex();
        roundLabel.setText("Round " + round);

        // Check if we need to rebuild (combatants changed)
        boolean needsRebuild = combatantBoxes.size() != turnOrder.size();
        if (!needsRebuild) {
            for (GridObject combatant : turnOrder) {
                if (!combatantBoxes.containsKey(combatant)) {
                    needsRebuild = true;
                    break;
                }
            }
        }

        if (needsRebuild) {
            rebuildTimeline(turnOrder, currentIndex);
        } else {
            // Just update the highlight
            updateHighlight(turnOrder, currentIndex);
        }
    }

    private void rebuildTimeline(List<GridObject> turnOrder, int currentIndex) {
        entitiesBox.getChildren().clear();
        combatantBoxes.clear();
        currentHighlightedBox = null;

        for (int i = 0; i < turnOrder.size(); i++) {
            GridObject combatant = turnOrder.get(i);
            VBox combatantBox = createCombatantBox(combatant, i == currentIndex);
            combatantBoxes.put(combatant, combatantBox);
            entitiesBox.getChildren().add(combatantBox);
            
            if (i == currentIndex) {
                currentHighlightedBox = combatantBox;
            }
            
            // Only do slide-in animation on initial build
            if (initialBuild) {
                final int index = i;
                combatantBox.setOpacity(0);
                PauseTransition delay = new PauseTransition(Duration.millis(50 * index));
                delay.setOnFinished(e -> AnimationUtils.slideIn(combatantBox, AnimationUtils.SlideDirection.UP));
                delay.play();
            }
        }
        
        // Apply initial highlight
        if (currentHighlightedBox != null && initialBuild) {
            PauseTransition pulseDelay = new PauseTransition(Duration.millis(50 * turnOrder.size() + 200));
            pulseDelay.setOnFinished(e -> applyGlow(currentHighlightedBox));
            pulseDelay.play();
        } else if (currentHighlightedBox != null) {
            applyGlow(currentHighlightedBox);
        }
        
        initialBuild = false;
    }

    private void updateHighlight(List<GridObject> turnOrder, int currentIndex) {
        GridObject currentCombatant = turnOrder.get(currentIndex);
        VBox newHighlight = combatantBoxes.get(currentCombatant);
        
        if (newHighlight == currentHighlightedBox) {
            return; // No change needed
        }
        
        // Remove highlight from old box with fade
        if (currentHighlightedBox != null) {
            VBox oldBox = currentHighlightedBox;
            oldBox.getStyleClass().remove("timeline-entity-current");
            oldBox.getStyleClass().add("timeline-entity");
            
            // Fade out the glow
            if (oldBox.getEffect() instanceof DropShadow glow) {
                Timeline fadeOutGlow = new Timeline(
                    new KeyFrame(Duration.millis(200), 
                        new KeyValue(glow.radiusProperty(), 0, Interpolator.EASE_OUT))
                );
                fadeOutGlow.setOnFinished(e -> oldBox.setEffect(null));
                fadeOutGlow.play();
            }
        }
        
        // Apply highlight to new box
        if (newHighlight != null) {
            newHighlight.getStyleClass().remove("timeline-entity");
            newHighlight.getStyleClass().add("timeline-entity-current");
            
            // Smooth glow transition
            applyGlowAnimated(newHighlight);
            currentHighlightedBox = newHighlight;
        }
    }

    private void applyGlow(VBox box) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#b8860b"));
        glow.setRadius(8);
        glow.setSpread(0.4);
        box.setEffect(glow);
    }

    private void applyGlowAnimated(VBox box) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#b8860b"));
        glow.setRadius(0);
        glow.setSpread(0.4);
        box.setEffect(glow);
        
        Timeline fadeInGlow = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(glow.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(300), 
                new KeyValue(glow.radiusProperty(), 8, Interpolator.EASE_OUT))
        );
        fadeInGlow.play();
    }

    private VBox createCombatantBox(GridObject combatant, boolean isCurrent) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(90);
        box.setMinWidth(70);
        box.setMaxWidth(110);
        box.setPrefHeight(55);
        box.setPadding(new Insets(6));
        
        if (isCurrent) {
            box.getStyleClass().addAll("timeline-entity", "timeline-entity-current");
        } else {
            box.getStyleClass().add("timeline-entity");
        }

        String name;
        boolean isPartyMember = false;
        if (combatant instanceof Entity e) {
            name = e.getName();
            isPartyMember = e.isParty();
        } else if (combatant instanceof Enemy en) {
            name = en.getName();
            isPartyMember = false;
        } else {
            name = "Unknown";
        }

        // Icon based on type
        String iconColor = isPartyMember ? "#4CAF50" : "#d75f5f";
        IconUtils.Icon icon = isPartyMember ? IconUtils.Icon.PERSON : IconUtils.Icon.SKULL;
        box.getChildren().add(IconUtils.createIcon(icon, 16, iconColor));

        Label nameLabel = new Label(truncateName(name, 10));
        nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #dcdcdc;");
        
        box.getChildren().add(nameLabel);
        
        // Add subtle hover effect
        box.setOnMouseEntered(e -> {
            if (!box.getStyleClass().contains("timeline-entity-current")) {
                box.setStyle(box.getStyle() + "-fx-background-color: linear-gradient(to bottom, #404045, #353538);");
            }
        });
        box.setOnMouseExited(e -> {
            if (!box.getStyleClass().contains("timeline-entity-current")) {
                box.setStyle("");
            }
        });
        
        return box;
    }

    private String truncateName(String name, int maxLen) {
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 3) + "...";
    }
}
