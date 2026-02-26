package UI.Battle;

import Objects.*;
import java.util.*;

public class TurnManager {

    private final List<Entity> turnOrder;
    private int currentIndex = 0;
    private int round = 1;
    private boolean battleStarted = false;

    public TurnManager(List<Entity> entities) {
        this.turnOrder = new ArrayList<>(entities);
    }

    /**
     * Sort entities by initiative (INITIATIVE stat), with DEX as tiebreaker.
     * Higher initiative and DEX go first.
     */
    public void calculateInitiativeOrder() {
        turnOrder.sort((e1, e2) -> {
            int init1 = e1.getCharSheet().getTotalAttribute(2); // INITIATIVE = 2
            int init2 = e2.getCharSheet().getTotalAttribute(2);
            
            if (init1 != init2) {
                return Integer.compare(init2, init1); // Higher initiative first
            }
            
            // Tiebreaker: higher DEX goes first
            int dex1 = e1.getCharSheet().getTotalAttribute(1); // DEXTERITY = 1
            int dex2 = e2.getCharSheet().getTotalAttribute(1);
            return Integer.compare(dex2, dex1); // Higher DEX first
        });
        
        currentIndex = 0;
        round = 1;
    }

    public Entity getCurrent() {
        if (turnOrder.isEmpty()) {
            return null;
        }
        return turnOrder.get(currentIndex);
    }

    public void nextTurn() {
        if (turnOrder.isEmpty()) {
            return;
        }
        
        currentIndex++;
        if (currentIndex >= turnOrder.size()) {
            currentIndex = 0;
            round++;
        }
        
        // Process status effects for the entity whose turn is starting
        Entity current = getCurrent();
        if (current != null) {
            current.getCharSheet().procStatus();
        }
    }

    public void removeDeadEntities() {
        turnOrder.removeIf(Entity::isDead);
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
            currentIndex = 0;
            round++;
        }
    }

    public boolean isCurrent(Entity e) {
        Entity current = getCurrent();
        return current != null && current == e;
    }

    public List<Entity> getTurnOrder() {
        return new ArrayList<>(turnOrder);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getRound() {
        return round;
    }

    public void addEntity(Entity entity) {
        turnOrder.add(entity);
        if (battleStarted) {
            calculateInitiativeOrder();
        }
    }

    public void removeEntity(Entity entity) {
        turnOrder.remove(entity);
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
            currentIndex = 0;
        }
    }

    public void setBattleStarted(boolean started) {
        this.battleStarted = started;
    }

    public boolean isBattleStarted() {
        return battleStarted;
    }
}
