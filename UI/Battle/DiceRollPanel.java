package UI.Battle;

import Objects.GridObject;
import UI.AnimationUtils;
import UI.IconUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Side panel for handling dice roll input during combat.
 * 
 * Flow:
 * 1. HIDDEN - Panel not visible
 * 2. D20_ROLL - Waiting for d20 attack roll input
 * 3. RESULT - Showing hit/miss result, if hit shows tier and awaits damage dice
 * 4. DAMAGE_ROLL - Waiting for damage dice input
 * 5. COMPLETE - Attack resolved, ready to hide
 */
public class DiceRollPanel extends VBox {

    public enum State {
        HIDDEN,
        D20_ROLL,
        RESULT_MISS,
        DAMAGE_ROLL,
        COMPLETE
    }
    
    private State currentState = State.HIDDEN;
    private GridObject attacker;
    private GridObject target;
    private int attackModifier;
    private int targetAC;
    private String[] damageDice;
    
    // Attack result
    private int d20Result;
    private int margin;
    private int tier;
    private List<String> diceToRoll;
    
    // UI Components
    private final Label titleLabel;
    private final Label attackerLabel;
    private final Label targetLabel;
    private final Label infoLabel;
    private final Label resultLabel;
    private final VBox inputArea;
    private final TextField d20Input;
    private final VBox damageInputArea;
    private final List<TextField> damageInputs = new ArrayList<>();
    private final Button submitBtn;
    private final Button cancelBtn;
    private final TextFlow diceListLabel;
    private final Label tierLabel;
    private final HBox tierBox;
    
    // Callbacks
    private Consumer<AttackOutcome> onAttackComplete;
    private Runnable onCancel;
    
    /**
     * Result of a completed attack
     */
    public static class AttackOutcome {
        public final GridObject attacker;
        public final GridObject target;
        public final boolean hit;
        public final int tier;
        public final int totalDamage;
        public final int margin;
        public final int d20Roll;
        public final int modifier;
        public final int targetAC;
        
        public AttackOutcome(GridObject attacker, GridObject target, boolean hit, 
                           int tier, int totalDamage, int margin, int d20Roll, 
                           int modifier, int targetAC) {
            this.attacker = attacker;
            this.target = target;
            this.hit = hit;
            this.tier = tier;
            this.totalDamage = totalDamage;
            this.margin = margin;
            this.d20Roll = d20Roll;
            this.modifier = modifier;
            this.targetAC = targetAC;
        }
    }

    public DiceRollPanel() {
        setSpacing(10);
        setPadding(new Insets(12));
        setPrefWidth(210);
        setMinWidth(200);
        setMaxWidth(230);
        getStyleClass().addAll("card", "card-gold");

        // Header
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().add(IconUtils.createIcon(IconUtils.Icon.TARGET, 18, "#daa520"));

        titleLabel = new Label("Attack Roll");
        titleLabel.getStyleClass().add("section-header");
        titleLabel.setStyle("-fx-font-size: 15px;");
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#daa52080"));
        glow.setRadius(6);
        titleLabel.setEffect(glow);
        headerBox.getChildren().add(titleLabel);

        // Attacker/Target info
        attackerLabel = new Label("Attacker: --");
        attackerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dcdcdc;");

        targetLabel = new Label("Target: --");
        targetLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dcdcdc;");

        infoLabel = new Label("Modifier: +0 | AC: 10");
        infoLabel.getStyleClass().add("label-muted");

        Separator sep1 = new Separator();

        // Input area
        inputArea = new VBox(8);

        Label d20Label = new Label("Enter d20 roll:");
        d20Label.setStyle("-fx-font-size: 12px; -fx-text-fill: #dcdcdc;");

        d20Input = new TextField();
        d20Input.setPromptText("1-20");
        d20Input.setPrefWidth(80);
        d20Input.setOnAction(e -> handleSubmit());
        
        // Restrict to numbers only
        d20Input.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                d20Input.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        Slider d20Slider = UI.FormUtils.attachRollSlider(d20Input, 1, 20);
        d20Slider.setPrefWidth(170);

        inputArea.getChildren().addAll(d20Label, d20Slider, d20Input);
        
        // Result area (initially hidden) - a colored banner stating just the outcome, no math
        resultLabel = new Label("");
        resultLabel.setContentDisplay(ContentDisplay.LEFT);
        resultLabel.setGraphicTextGap(8);
        resultLabel.setMaxWidth(Double.MAX_VALUE);
        resultLabel.setAlignment(Pos.CENTER);
        resultLabel.setWrapText(true);

        // Small centered "TIER N" pill, shown only on a hit/crit
        tierLabel = new Label("");
        tierLabel.getStyleClass().addAll("badge", "badge-primary");
        tierLabel.setStyle("-fx-font-size: 11px;");
        tierBox = new HBox(tierLabel);
        tierBox.setAlignment(Pos.CENTER);

        // Damage input area
        damageInputArea = new VBox(6);

        diceListLabel = new TextFlow();

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #505052;");
        
        // Buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        
        submitBtn = new Button("Roll");
        submitBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.DICE));
        submitBtn.getStyleClass().add("button");
        // Text changes between "Roll"/"OK"/"Confirm" - never let it shrink below what the
        // longest label needs, or the text silently ellipsizes ("Conf...") instead of fitting.
        submitBtn.setMinWidth(Region.USE_PREF_SIZE);
        submitBtn.setOnAction(e -> handleSubmit());

        cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setMinWidth(Region.USE_PREF_SIZE);
        cancelBtn.setOnAction(e -> handleCancel());
        
        buttonBox.getChildren().addAll(submitBtn, cancelBtn);
        
        getChildren().addAll(headerBox, attackerLabel, targetLabel, infoLabel,
                            sep1, inputArea, resultLabel, tierBox,
                            damageInputArea, diceListLabel, sep2, buttonBox);
        
        // Start hidden
        setVisible(false);
        setManaged(false);
    }
    
    /**
     * Start a new attack sequence
     */
    public void startAttack(GridObject attacker, GridObject target, 
                           Consumer<AttackOutcome> onComplete, Runnable onCancel) {
        this.attacker = attacker;
        this.target = target;
        this.onAttackComplete = onComplete;
        this.onCancel = onCancel;
        
        // Get combat stats
        this.attackModifier = CombatManager.getAttackModifier(attacker);
        this.targetAC = CombatManager.getTargetAC(target);
        this.damageDice = CombatManager.getDamageDice(attacker);
        
        // Update labels
        attackerLabel.setText("Attacker: " + CombatManager.getAttackerName(attacker));
        targetLabel.setText("Target: " + CombatManager.getTargetName(target));
        
        String modSign = attackModifier >= 0 ? "+" : "";
        String statType = CombatManager.getStatTypeName(attacker);
        infoLabel.setText(statType.substring(0, 3) + ": " + modSign + attackModifier + " | AC: " + targetAC);
        
        // Reset state
        d20Input.clear();
        d20Input.setDisable(false);
        resultLabel.setText("");
        resultLabel.setGraphic(null);
        resultLabel.setStyle("");
        tierLabel.setText("");
        tierBox.setVisible(false);
        tierBox.setManaged(false);
        damageInputArea.getChildren().clear();
        damageInputs.clear();
        diceListLabel.getChildren().clear();
        submitBtn.setText("Roll");

        // Show panel
        currentState = State.D20_ROLL;
        titleLabel.setText("Attack Roll");
        inputArea.setVisible(true);
        inputArea.setManaged(true);
        damageInputArea.setVisible(false);
        damageInputArea.setManaged(false);
        diceListLabel.setVisible(false);
        
        setVisible(true);
        setManaged(true);
        AnimationUtils.slideIn(this, AnimationUtils.SlideDirection.RIGHT, AnimationUtils.MEDIUM);
        d20Input.requestFocus();
    }
    
    private void handleSubmit() {
        switch (currentState) {
            case D20_ROLL -> processD20Roll();
            case DAMAGE_ROLL -> processDamageRoll();
            case RESULT_MISS -> completeAttack(0);
            default -> {}
        }
    }
    
    private void processD20Roll() {
        String text = d20Input.getText().trim();
        if (text.isEmpty()) {
            showError("Please enter your d20 roll");
            return;
        }
        
        try {
            d20Result = Integer.parseInt(text);
            if (d20Result < 1 || d20Result > 20) {
                showError("d20 must be 1-20");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid number");
            return;
        }
        
        // Calculate margin and tier
        margin = CombatManager.calculateMargin(d20Result, attackModifier, targetAC);
        tier = CombatManager.getAttackTier(margin);

        // Display result
        d20Input.setDisable(true);

        if (margin < 0) {
            // Miss - just state the outcome, no breakdown
            currentState = State.RESULT_MISS;
            setResultBanner("MISS", IconUtils.Icon.CLOSE, "#d75f5f", "rgba(215, 95, 95, 0.15)");
            tierBox.setVisible(false);
            tierBox.setManaged(false);
            submitBtn.setText("OK");
        } else {
            // Hit - state the outcome and tier, then request damage dice
            currentState = State.DAMAGE_ROLL;
            boolean isCritical = d20Result == 20;
            if (isCritical) {
                setResultBanner("CRITICAL HIT", IconUtils.Icon.LIGHTNING, "#FFD54F", "rgba(255, 213, 79, 0.15)");
            } else {
                setResultBanner("HIT", IconUtils.Icon.CHECK, "#4CAF50", "rgba(76, 175, 80, 0.15)");
            }

            tierLabel.setText("TIER " + tier);
            tierBox.setVisible(true);
            tierBox.setManaged(true);

            // Get dice to roll, plus the flat ability-modifier bonus applied to the final total
            diceToRoll = CombatManager.getDiceForTier(damageDice, tier);
            String diceStr = CombatManager.formatDiceList(diceToRoll);
            updateDiceListDisplay(diceStr, getAbilityModifier());
            diceListLabel.setVisible(true);

            // Create damage input fields
            setupDamageInputs();

            submitBtn.setText("Confirm");
        }
    }

    /**
     * Show just the outcome (no d20/AC math) as a colored icon banner.
     */
    private void setResultBanner(String text, IconUtils.Icon icon, String color, String backgroundRgba) {
        resultLabel.setText(text);
        resultLabel.setGraphic(IconUtils.createIcon(icon, 18, color));
        resultLabel.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + "; " +
            "-fx-background-color: " + backgroundRgba + "; -fx-background-radius: 8; " +
            "-fx-border-color: " + color + "; -fx-border-width: 1.5; -fx-border-radius: 8; " +
            "-fx-padding: 8 12 8 12;"
        );
    }

    /**
     * The ability modifier damage bonus: floor((stat score + 2) / 2), using whatever stat the
     * attacker's weapon uses (or Strength if unarmed) - the same stat driving the attack roll.
     */
    private int getAbilityModifier() {
        return Math.floorDiv(attackModifier + 2, 2);
    }

    /**
     * Render "Roll: <dice> +<bonus>" with the bonus colored green/red/gray by sign.
     */
    private void updateDiceListDisplay(String diceStr, int abilityModifier) {
        diceListLabel.getChildren().clear();

        Text rollText = new Text("Roll: " + diceStr + "  ");
        rollText.setStyle("-fx-fill: #dcdcdc; -fx-font-size: 12px;");

        String bonusColor = abilityModifier > 0 ? "#4CAF50" : abilityModifier < 0 ? "#d75f5f" : "#808080";
        String bonusSign = abilityModifier >= 0 ? "+" : "";
        Text bonusText = new Text(bonusSign + abilityModifier + " dmg");
        bonusText.setStyle("-fx-fill: " + bonusColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        diceListLabel.getChildren().addAll(rollText, bonusText);
    }
    
    private void setupDamageInputs() {
        damageInputArea.getChildren().clear();
        damageInputs.clear();
        
        Label dmgLabel = new Label("Enter damage rolls:");
        dmgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dcdcdc;");
        damageInputArea.getChildren().add(dmgLabel);
        
        // Create a field for each die
        for (int i = 0; i < diceToRoll.size(); i++) {
            String die = diceToRoll.get(i);
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            
            Label dieLabel = new Label(die + ":");
            dieLabel.getStyleClass().add("label-muted");
            dieLabel.setPrefWidth(35);

            TextField input = new TextField();
            input.setPromptText(getMaxForDie(die));
            input.setPrefWidth(50);


            // Restrict to numbers
            input.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    input.setText(newVal.replaceAll("[^\\d]", ""));
                }
            });

            final int index = i;
            input.setOnAction(e -> {
                // Focus next field or submit
                if (index < damageInputs.size() - 1) {
                    damageInputs.get(index + 1).requestFocus();
                } else {
                    handleSubmit();
                }
            });

            damageInputs.add(input);
            Slider dieSlider = UI.FormUtils.attachRollSlider(input, 1, getMaxValue(die));
            dieSlider.setPrefWidth(90);
            row.getChildren().addAll(dieLabel, dieSlider, input);
            damageInputArea.getChildren().add(row);
        }
        
        damageInputArea.setVisible(true);
        damageInputArea.setManaged(true);
        
        if (!damageInputs.isEmpty()) {
            damageInputs.get(0).requestFocus();
        }
    }
    
    private String getMaxForDie(String die) {
        return switch (die.toLowerCase()) {
            case "d4" -> "1-4";
            case "d6" -> "1-6";
            case "d8" -> "1-8";
            case "d10" -> "1-10";
            case "d12" -> "1-12";
            case "d20" -> "1-20";
            default -> "1-6";
        };
    }
    
    private int getMaxValue(String die) {
        return switch (die.toLowerCase()) {
            case "d4" -> 4;
            case "d6" -> 6;
            case "d8" -> 8;
            case "d10" -> 10;
            case "d12" -> 12;
            case "d20" -> 20;
            default -> 6;
        };
    }
    
    private void processDamageRoll() {
        int totalDamage = 0;
        
        // Validate and sum all damage inputs
        for (int i = 0; i < damageInputs.size(); i++) {
            TextField input = damageInputs.get(i);
            String text = input.getText().trim();
            
            if (text.isEmpty()) {
                showError("Please enter all dice rolls");
                input.requestFocus();
                return;
            }
            
            try {
                int value = Integer.parseInt(text);
                int maxVal = getMaxValue(diceToRoll.get(i));
                if (value < 1 || value > maxVal) {
                    showError(diceToRoll.get(i) + " must be 1-" + maxVal);
                    input.requestFocus();
                    return;
                }
                totalDamage += value;
            } catch (NumberFormatException e) {
                showError("Invalid number");
                input.requestFocus();
                return;
            }
        }

        // Critical hit rule: natural 20 on d20 multiplies final rolled dice damage by 1.5.
        if (d20Result == 20) {
            totalDamage = (int) Math.ceil(totalDamage * 1.5);
        }

        // Ability modifier is a flat bonus added on top of the (possibly crit-multiplied) dice total.
        totalDamage = Math.max(0, totalDamage + getAbilityModifier());

        completeAttack(totalDamage);
    }
    
    private void completeAttack(int totalDamage) {
        currentState = State.COMPLETE;
        
        // Create outcome
        AttackOutcome outcome = new AttackOutcome(
            attacker, target, 
            tier > 0, tier, totalDamage, margin,
            d20Result, attackModifier, targetAC
        );
        
        // Hide panel
        hide();
        
        // Notify callback
        if (onAttackComplete != null) {
            onAttackComplete.accept(outcome);
        }
    }
    
    private void handleCancel() {
        hide();
        if (onCancel != null) {
            onCancel.run();
        }
    }
    
    private void showError(String message) {
        // Flash the result label with error
        String originalText = resultLabel.getText();
        String originalStyle = resultLabel.getStyle();
        
        resultLabel.setText(message);
        resultLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #d75f5f;");
        
        // Reset after delay
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
            javafx.util.Duration.millis(1500)
        );
        pause.setOnFinished(e -> {
            if (currentState != State.HIDDEN && currentState != State.COMPLETE) {
                resultLabel.setText(originalText);
                resultLabel.setStyle(originalStyle);
            }
        });
        pause.play();
    }
    
    public void hide() {
        currentState = State.HIDDEN;
        setVisible(false);
        setManaged(false);
        attacker = null;
        target = null;
    }
    
    public boolean isActive() {
        return currentState != State.HIDDEN && currentState != State.COMPLETE;
    }
    
    public State getState() {
        return currentState;
    }
}
