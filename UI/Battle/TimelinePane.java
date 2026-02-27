package UI.Battle;

import Objects.Entity;
import Objects.Enemy;
import Objects.GridObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class TimelinePane extends HBox {

    private final TurnManager turnManager;
    private final Label roundLabel;
    private final HBox entitiesBox;

    public TimelinePane(TurnManager turnManager) {
        this.turnManager = turnManager;
        
        getStyleClass().add("timeline-pane");
        setSpacing(15);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(80);
        setMinHeight(80);
        setMaxHeight(80);

        // Round label
        roundLabel = new Label("Round 0");
        roundLabel.getStyleClass().add("label-title");
        roundLabel.setMinWidth(100);

        // Entities container
        entitiesBox = new HBox(5);
        entitiesBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(entitiesBox, Priority.ALWAYS);

        getChildren().addAll(roundLabel, entitiesBox);
        
        refresh();
    }

    public void refresh() {
        entitiesBox.getChildren().clear();

        List<GridObject> turnOrder = turnManager.getTurnOrder();
        
        if (turnOrder.isEmpty()) {
            Label emptyLabel = new Label("No combatants on field. Add objects to begin.");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #808080;");
            entitiesBox.getChildren().add(emptyLabel);
            roundLabel.setText("Round 0");
            return;
        }

        int round = turnManager.getRound();
        int currentIndex = turnManager.getCurrentIndex();
        roundLabel.setText("Round " + round);

        for (int i = 0; i < turnOrder.size(); i++) {
            GridObject combatant = turnOrder.get(i);
            VBox combatantBox = createCombatantBox(combatant, i == currentIndex);
            entitiesBox.getChildren().add(combatantBox);
        }
    }

    private VBox createCombatantBox(GridObject combatant, boolean isCurrent) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(80);
        box.setMinWidth(60);
        box.setMaxWidth(100);
        box.setPrefHeight(50);
        box.setPadding(new Insets(5));
        
        if (isCurrent) {
            box.getStyleClass().addAll("timeline-entity", "timeline-entity-current");
        } else {
            box.getStyleClass().add("timeline-entity");
        }

        String name;
        if (combatant instanceof Entity e) {
            name = e.getName();
        } else if (combatant instanceof Enemy en) {
            name = en.getName();
        } else {
            name = "Unknown";
        }

        Label nameLabel = new Label(truncateName(name, 12));
        nameLabel.setStyle("-fx-font-size: 11px;");
        
        box.getChildren().add(nameLabel);
        return box;
    }

    private String truncateName(String name, int maxLen) {
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 3) + "...";
    }
}
