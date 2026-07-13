package UI.Battle;

import Objects.Entity;
import Objects.Enemy;
import Objects.GridObject;
import UI.AnimationUtils;
import UI.IconUtils;
import UI.SpriteUtils;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TimelinePane extends HBox {

    private final TurnManager turnManager;
    private final Label roundLabel;
    private final VBox roundBox;
    private final HBox entitiesBox;
    private javafx.scene.Node battleControls;
    private final Map<GridObject, VBox> combatantBoxes = new HashMap<>();
    private final Map<GridObject, javafx.scene.control.ProgressBar> hpBars = new HashMap<>();
    private final Map<GridObject, Label> hpLabels = new HashMap<>();
    private VBox currentHighlightedBox = null;
    private boolean initialBuild = true;
    private boolean setupMode = false;
    private List<Entity> rosterParty = java.util.Collections.emptyList();

    public TimelinePane(TurnManager turnManager) {
        this.turnManager = turnManager;
        
        getStyleClass().add("timeline-pane");
        setSpacing(15);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(110);
        setMinHeight(110);
        setMaxHeight(110);

        // Left column: round label with the battle controls slot below it
        HBox labelRow = new HBox(8);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        labelRow.getChildren().add(IconUtils.createIcon(IconUtils.Icon.CLOCK, 20, "#b8860b"));

        roundLabel = new Label("Round 0");
        roundLabel.getStyleClass().add("label-title");
        roundLabel.setMinWidth(80);
        labelRow.getChildren().add(roundLabel);

        roundBox = new VBox(6);
        roundBox.setAlignment(Pos.CENTER_LEFT);
        roundBox.setMinWidth(160);
        roundBox.setPrefWidth(160);
        roundBox.getChildren().add(labelRow);

        // Entities container
        entitiesBox = new HBox(5);
        entitiesBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(entitiesBox, Priority.ALWAYS);

        getChildren().addAll(roundBox, entitiesBox);

        refresh();
    }

    /**
     * Place the battle control buttons (Begin/End, Next Turn) under the
     * round label. The pane doesn't care what the controls do.
     */
    public void setBattleControls(javafx.scene.Node controls) {
        if (battleControls != null) {
            roundBox.getChildren().remove(battleControls);
        }
        battleControls = controls;
        if (controls != null) {
            roundBox.getChildren().add(controls);
        }
    }

    /**
     * Switch the strip into inline initiative entry: one portrait per party
     * member with a typed d20 value (1-20), plus Begin/Cancel. Begin stays
     * disabled until every value parses. Enemies roll automatically.
     */
    public void enterInitiativeSetup(List<Entity> partyMembers,
            Consumer<Map<Entity, Integer>> onConfirm, Runnable onCancel) {
        setupMode = true;
        entitiesBox.getChildren().clear();
        combatantBoxes.clear();
        currentHighlightedBox = null;
        roundLabel.setText("Initiative");

        Map<Entity, TextField> fields = new LinkedHashMap<>();

        Button confirmBtn = new Button("Begin");
        confirmBtn.getStyleClass().add("button-primary");
        confirmBtn.setDisable(true);
        confirmBtn.setOnAction(e -> {
            Map<Entity, Integer> rolls = new LinkedHashMap<>();
            for (Map.Entry<Entity, TextField> entry : fields.entrySet()) {
                rolls.put(entry.getKey(), Integer.parseInt(entry.getValue().getText().trim()));
            }
            onConfirm.accept(rolls);
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setOnAction(e -> onCancel.run());

        for (Entity member : partyMembers) {
            VBox box = new VBox(3);
            box.setAlignment(Pos.CENTER);
            box.setPrefWidth(90);
            box.setMinWidth(70);
            box.setMaxWidth(110);
            box.setPadding(new Insets(6));
            box.getStyleClass().add("timeline-entity");

            box.getChildren().add(SpriteUtils.createCharacterSprite(member.getCharSheet(), 28));

            Label nameLabel = new Label(truncateName(member.getName(), 10));
            nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #dcdcdc;");
            box.getChildren().add(nameLabel);

            TextField field = new TextField();
            field.setPrefWidth(44);
            field.setMaxWidth(44);
            field.setPromptText("d20");
            field.getStyleClass().add("timeline-init-field");
            field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,2}") ? change : null));
            field.textProperty().addListener((obs, o, n) ->
                confirmBtn.setDisable(!allFieldsValid(fields)));
            fields.put(member, field);
            box.getChildren().add(field);

            entitiesBox.getChildren().add(box);
        }

        HBox buttons = new HBox(6, confirmBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(buttons, new Insets(0, 0, 0, 8));
        entitiesBox.getChildren().add(buttons);

        if (!partyMembers.isEmpty()) {
            javafx.application.Platform.runLater(() -> fields.get(partyMembers.get(0)).requestFocus());
        }
    }

    private boolean allFieldsValid(Map<Entity, TextField> fields) {
        for (TextField f : fields.values()) {
            try {
                int v = Integer.parseInt(f.getText().trim());
                if (v < 1 || v > 20) return false;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    /** Leave initiative entry and return to the normal turn-order display. */
    public void exitInitiativeSetup() {
        if (!setupMode) return;
        setupMode = false;
        initialBuild = true; // replay the slide-in when the real order appears
        refresh();
    }

    /**
     * Pre-battle roster: show the party members on the field (with HP) while
     * there is no turn order yet.
     */
    public void showRoster(List<Entity> partyOnField) {
        rosterParty = partyOnField;
        if (!setupMode) {
            rebuildRoster();
        }
    }

    private void rebuildRoster() {
        entitiesBox.getChildren().clear();
        combatantBoxes.clear();
        hpBars.clear();
        hpLabels.clear();
        currentHighlightedBox = null;
        roundLabel.setText("Setup");
        initialBuild = true;

        if (rosterParty.isEmpty()) {
            Label emptyLabel = new Label("No combatants on field. Add objects to begin.");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #808080;");
            entitiesBox.getChildren().add(emptyLabel);
            return;
        }
        for (Entity member : rosterParty) {
            VBox box = createCombatantBox(member, false);
            combatantBoxes.put(member, box);
            entitiesBox.getChildren().add(box);
        }
        refreshHp();
    }

    /** Update every visible HP bar/label from the combatants' current HP. */
    public void refreshHp() {
        for (Map.Entry<GridObject, javafx.scene.control.ProgressBar> entry : hpBars.entrySet()) {
            GridObject combatant = entry.getKey();
            int hp;
            int maxHp;
            if (combatant instanceof Entity e) {
                hp = e.getCharSheet().getCurrentHP();
                maxHp = e.getCharSheet().getTotalHP();
            } else if (combatant instanceof Enemy en) {
                hp = en.getHealth();
                maxHp = en.getMaxHealth();
            } else {
                continue;
            }
            double pct = maxHp > 0 ? Math.max(0, Math.min(1, hp / (double) maxHp)) : 0;
            javafx.scene.control.ProgressBar bar = entry.getValue();
            bar.setProgress(pct);
            bar.setStyle("-fx-accent: " + (pct > 0.5 ? "#4CAF50" : pct > 0.25 ? "#e6b23c" : "#d75f5f") + ";");
            Label hpText = hpLabels.get(combatant);
            if (hpText != null) {
                hpText.setText(hp + "/" + maxHp);
            }
        }
    }

    public void refresh() {
        if (setupMode) return; // don't wipe the initiative fields

        List<GridObject> turnOrder = turnManager.getTurnOrder();

        // Pre-battle (turnOrder fills up as combatants are placed, but there
        // is no meaningful order yet) and empty states both show the roster
        if (!turnManager.isBattleStarted() || turnOrder.isEmpty()) {
            rebuildRoster();
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
        refreshHp();
    }

    private void rebuildTimeline(List<GridObject> turnOrder, int currentIndex) {
        entitiesBox.getChildren().clear();
        combatantBoxes.clear();
        hpBars.clear();
        hpLabels.clear();
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
        javafx.scene.Node portrait;
        boolean hasHp = true;
        if (combatant instanceof Entity e) {
            name = e.getName();
            portrait = SpriteUtils.createCharacterSprite(e.getCharSheet(), 28);
        } else if (combatant instanceof Enemy en) {
            name = en.getName();
            portrait = SpriteUtils.createEnemySprite(en, 28);
        } else {
            name = "Unknown";
            portrait = IconUtils.createIcon(IconUtils.Icon.SKULL, 16, "#d75f5f");
            hasHp = false;
        }
        box.getChildren().add(portrait);

        Label nameLabel = new Label(truncateName(name, 10));
        nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #dcdcdc;");

        box.getChildren().add(nameLabel);

        // Mini HP readout, values filled in by refreshHp()
        if (hasHp) {
            javafx.scene.control.ProgressBar hpBar = new javafx.scene.control.ProgressBar(1);
            hpBar.setPrefSize(64, 6);
            hpBar.setMinHeight(6);
            hpBar.setMaxHeight(6);
            hpBar.getStyleClass().add("hp-bar");
            Label hpText = new Label();
            hpText.setStyle("-fx-font-size: 9px; -fx-text-fill: #b8b8c0;");
            box.getChildren().addAll(hpBar, hpText);
            hpBars.put(combatant, hpBar);
            hpLabels.put(combatant, hpText);
        }
        
        // Add tooltip with initiative info
        int initRoll = turnManager.getInitiativeRoll(combatant);
        int[] breakdown = turnManager.getRollBreakdown(combatant);
        String tooltipText = name + "\nInitiative: " + initRoll;
        if (breakdown[0] > 0 || breakdown[1] != 0) {
            tooltipText += " (d20: " + breakdown[0] + " + DEX: " + breakdown[1] + ")";
        }
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(javafx.util.Duration.millis(200));
        Tooltip.install(box, tooltip);
        
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
