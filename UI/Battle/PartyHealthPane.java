package UI.Battle;

import Objects.Entity;
import UI.AnimationUtils;
import UI.IconUtils;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel showing HP bars for all party members during battle.
 * Uses property bindings for reactive updates.
 */
public class PartyHealthPane extends VBox {

    private final Map<Entity, EntityHealthBinding> healthBindings = new HashMap<>();
    private final Map<Entity, ProgressBar> healthBars = new HashMap<>();
    private final VBox membersContainer;

    public PartyHealthPane() {
        getStyleClass().addAll("card");
        setStyle("-fx-background-color: linear-gradient(to bottom, #2d2d30, #252528);");
        setPadding(new Insets(12));
        setSpacing(10);
        setPrefWidth(210);
        setMinWidth(180);
        setMaxWidth(230);

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().add(IconUtils.createIcon(IconUtils.Icon.HEART, 18, "#4CAF50"));
        
        Label header = new Label("Party Health");
        header.getStyleClass().add("label-header");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        
        // Add subtle glow to header
        DropShadow headerGlow = new DropShadow();
        headerGlow.setColor(Color.web("#4CAF5080"));
        headerGlow.setRadius(6);
        headerGlow.setSpread(0.2);
        header.setEffect(headerGlow);
        headerBox.getChildren().add(header);

        membersContainer = new VBox(8);
        membersContainer.setPadding(new Insets(8, 0, 0, 0));

        getChildren().addAll(headerBox, membersContainer);
    }

    /**
     * Updates the party health display with current party members.
     */
    public void updateParty(List<Entity> partyMembers) {
        membersContainer.getChildren().clear();
        healthBindings.clear();
        healthBars.clear();

        for (Entity member : partyMembers) {
            if (member.isParty()) {
                EntityHealthBinding binding = new EntityHealthBinding(member);
                healthBindings.put(member, binding);
                VBox memberBox = createMemberEntry(member, binding);
                membersContainer.getChildren().add(memberBox);
            }
        }
    }

    /**
     * Refreshes HP display for all party members (call after damage/healing).
     * With bindings, this simply refreshes the underlying data.
     */
    public void refresh(List<Entity> partyMembers) {
        for (Entity member : partyMembers) {
            if (!member.isParty()) continue;
            
            EntityHealthBinding binding = healthBindings.get(member);
            if (binding != null) {
                double oldRatio = binding.getHealthRatio();
                binding.refresh();
                double newRatio = binding.getHealthRatio();
                
                // Animate the progress bar if ratio changed
                ProgressBar bar = healthBars.get(member);
                if (bar != null && Math.abs(oldRatio - newRatio) > 0.001) {
                    AnimationUtils.animateProgressBar(bar, Math.max(0, Math.min(1, newRatio)));
                }
            }
        }
    }
    
    /**
     * Gets the health binding for an entity (for external use).
     */
    public EntityHealthBinding getHealthBinding(Entity entity) {
        return healthBindings.get(entity);
    }

    private VBox createMemberEntry(Entity member, EntityHealthBinding binding) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.getStyleClass().add("compact-card");
        box.setStyle("-fx-background-color: linear-gradient(to right, #2d3530, #283028); " +
                     "-fx-background-radius: 6; " +
                     "-fx-border-color: #3c4540; -fx-border-radius: 6; -fx-border-width: 1; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");

        // Name and HP numbers row
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Small icon indicating party member
        topRow.getChildren().add(IconUtils.createIcon(IconUtils.Icon.PERSON, 14, "#4CAF50"));

        Label nameLabel = new Label(truncateName(member.getName(), 12));
        nameLabel.setStyle("-fx-text-fill: #dcdcdc; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // HP label bound to the health binding
        Label hpLabel = new Label();
        hpLabel.textProperty().bind(binding.healthTextProperty());
        hpLabel.setStyle("-fx-text-fill: #909095; -fx-font-size: 11px;");

        topRow.getChildren().addAll(nameLabel, hpLabel);

        // HP bar bound to health ratio
        ProgressBar hpBar = new ProgressBar();
        hpBar.progressProperty().bind(binding.healthRatioProperty());
        hpBar.setPrefWidth(Double.MAX_VALUE);
        hpBar.setPrefHeight(12);
        hpBar.setMaxWidth(Double.MAX_VALUE);
        hpBar.getStyleClass().add("hp-bar");
        healthBars.put(member, hpBar);
        
        // Update bar color when health status changes
        binding.healthStatusProperty().addListener((obs, oldStatus, newStatus) -> {
            updateBarColor(hpBar, newStatus);
        });
        updateBarColor(hpBar, binding.getHealthStatus());

        box.getChildren().addAll(topRow, hpBar);
        
        // Add hover effect
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: linear-gradient(to right, #354038, #303530); " +
                         "-fx-background-radius: 6; " +
                         "-fx-border-color: #4CAF50; -fx-border-radius: 6; -fx-border-width: 1; " +
                         "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 6, 0, 0, 0);");
        });
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: linear-gradient(to right, #2d3530, #283028); " +
                         "-fx-background-radius: 6; " +
                         "-fx-border-color: #3c4540; -fx-border-radius: 6; -fx-border-width: 1; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");
        });
        
        return box;
    }

    private void updateBarColor(ProgressBar bar, String status) {
        switch (status) {
            case "HEALTHY" -> bar.setStyle("-fx-accent: #4CAF50;"); // Green
            case "WOUNDED" -> bar.setStyle("-fx-accent: #FF9800;"); // Orange
            case "CRITICAL", "DEAD" -> bar.setStyle("-fx-accent: #F44336;"); // Red
        }
    }

    private String truncateName(String name, int maxLen) {
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 2) + "..";
    }
}
