package UI.Battle;

import Objects.Entity;
import Objects.GridObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Observable state container for battle system.
 * Uses JavaFX properties to enable automatic UI updates via bindings.
 */
public class BattleState {

    // Battle status
    private final BooleanProperty battleStarted = new SimpleBooleanProperty(false);
    private final BooleanProperty battleEnded = new SimpleBooleanProperty(false);
    
    // Current turn info
    private final IntegerProperty currentRound = new SimpleIntegerProperty(0);
    private final IntegerProperty currentTurnIndex = new SimpleIntegerProperty(0);
    private final ObjectProperty<GridObject> currentCombatant = new SimpleObjectProperty<>(null);
    
    // Mode tracking
    private final StringProperty currentMode = new SimpleStringProperty("SETUP");
    private final StringProperty statusMessage = new SimpleStringProperty("Add combatants to begin");
    
    // Selected entity for actions
    private final ObjectProperty<Entity> selectedEntity = new SimpleObjectProperty<>(null);
    private final BooleanProperty entitySelected = new SimpleBooleanProperty(false);
    
    // Action availability
    private final BooleanProperty canMove = new SimpleBooleanProperty(false);
    private final BooleanProperty canAttack = new SimpleBooleanProperty(false);
    private final BooleanProperty canEndTurn = new SimpleBooleanProperty(false);
    
    // Combatant lists (observable for automatic UI updates)
    private final ObservableList<Entity> partyMembers = FXCollections.observableArrayList();
    private final ObservableList<GridObject> turnOrder = FXCollections.observableArrayList();
    
    // Combat statistics
    private final IntegerProperty totalDamageDealt = new SimpleIntegerProperty(0);
    private final IntegerProperty totalDamageTaken = new SimpleIntegerProperty(0);
    private final IntegerProperty enemiesDefeated = new SimpleIntegerProperty(0);

    // ===== BATTLE STATUS =====
    
    public boolean isBattleStarted() { return battleStarted.get(); }
    public void setBattleStarted(boolean value) { 
        battleStarted.set(value);
        if (value) {
            currentMode.set("BATTLE");
            statusMessage.set("Battle in progress");
        }
    }
    public BooleanProperty battleStartedProperty() { return battleStarted; }
    
    public boolean isBattleEnded() { return battleEnded.get(); }
    public void setBattleEnded(boolean value) { 
        battleEnded.set(value);
        if (value) {
            currentMode.set("ENDED");
            statusMessage.set("Battle ended");
        }
    }
    public BooleanProperty battleEndedProperty() { return battleEnded; }

    // ===== TURN INFO =====
    
    public int getCurrentRound() { return currentRound.get(); }
    public void setCurrentRound(int value) { currentRound.set(value); }
    public IntegerProperty currentRoundProperty() { return currentRound; }
    
    public int getCurrentTurnIndex() { return currentTurnIndex.get(); }
    public void setCurrentTurnIndex(int value) { currentTurnIndex.set(value); }
    public IntegerProperty currentTurnIndexProperty() { return currentTurnIndex; }
    
    public GridObject getCurrentCombatant() { return currentCombatant.get(); }
    public void setCurrentCombatant(GridObject value) { currentCombatant.set(value); }
    public ObjectProperty<GridObject> currentCombatantProperty() { return currentCombatant; }

    // ===== MODE & STATUS =====
    
    public String getCurrentMode() { return currentMode.get(); }
    public void setCurrentMode(String value) { currentMode.set(value); }
    public StringProperty currentModeProperty() { return currentMode; }
    
    public String getStatusMessage() { return statusMessage.get(); }
    public void setStatusMessage(String value) { statusMessage.set(value); }
    public StringProperty statusMessageProperty() { return statusMessage; }

    // ===== SELECTION =====
    
    public Entity getSelectedEntity() { return selectedEntity.get(); }
    public void setSelectedEntity(Entity value) { 
        selectedEntity.set(value);
        entitySelected.set(value != null);
        updateActionAvailability();
    }
    public ObjectProperty<Entity> selectedEntityProperty() { return selectedEntity; }
    public BooleanProperty entitySelectedProperty() { return entitySelected; }

    // ===== ACTION AVAILABILITY =====
    
    public boolean canMove() { return canMove.get(); }
    public BooleanProperty canMoveProperty() { return canMove; }
    
    public boolean canAttack() { return canAttack.get(); }
    public BooleanProperty canAttackProperty() { return canAttack; }
    
    public boolean canEndTurn() { return canEndTurn.get(); }
    public BooleanProperty canEndTurnProperty() { return canEndTurn; }
    
    private void updateActionAvailability() {
        boolean inBattle = battleStarted.get() && !battleEnded.get();
        Entity selected = selectedEntity.get();
        
        canMove.set(inBattle && selected != null);
        canAttack.set(inBattle && selected != null);
        canEndTurn.set(inBattle);
    }

    // ===== COMBATANT LISTS =====
    
    public ObservableList<Entity> getPartyMembers() { return partyMembers; }
    public ObservableList<GridObject> getTurnOrder() { return turnOrder; }

    // ===== STATISTICS =====
    
    public int getTotalDamageDealt() { return totalDamageDealt.get(); }
    public void addDamageDealt(int damage) { totalDamageDealt.set(totalDamageDealt.get() + damage); }
    public IntegerProperty totalDamageDealtProperty() { return totalDamageDealt; }
    
    public int getTotalDamageTaken() { return totalDamageTaken.get(); }
    public void addDamageTaken(int damage) { totalDamageTaken.set(totalDamageTaken.get() + damage); }
    public IntegerProperty totalDamageTakenProperty() { return totalDamageTaken; }
    
    public int getEnemiesDefeated() { return enemiesDefeated.get(); }
    public void incrementEnemiesDefeated() { enemiesDefeated.set(enemiesDefeated.get() + 1); }
    public IntegerProperty enemiesDefeatedProperty() { return enemiesDefeated; }

    // ===== RESET =====
    
    public void reset() {
        battleStarted.set(false);
        battleEnded.set(false);
        currentRound.set(0);
        currentTurnIndex.set(0);
        currentCombatant.set(null);
        currentMode.set("SETUP");
        statusMessage.set("Add combatants to begin");
        selectedEntity.set(null);
        canMove.set(false);
        canAttack.set(false);
        canEndTurn.set(false);
        partyMembers.clear();
        turnOrder.clear();
        totalDamageDealt.set(0);
        totalDamageTaken.set(0);
        enemiesDefeated.set(0);
    }
}
