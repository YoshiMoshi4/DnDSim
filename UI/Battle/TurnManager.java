package UI.Battle;

import Objects.*;
import java.util.*;

public class TurnManager {

    private final List<GridObject> turnOrder;
    private int currentIndex = 0;
    private int round = 1;
    private boolean battleStarted = false;

    public TurnManager(List<Entity> entities) {
        this.turnOrder = new ArrayList<>(entities);
    }

    /**
     * Sort combatants by initiative, with random tiebreaker.
     * Higher initiative goes first.
     */
    public void calculateInitiativeOrder() {
        turnOrder.sort((o1, o2) -> {
            int init1 = getInitiative(o1);
            int init2 = getInitiative(o2);
            
            if (init1 != init2) {
                return Integer.compare(init2, init1); // Higher initiative first
            }
            
            // Tiebreaker: random
            return (Math.random() < 0.5) ? -1 : 1;
        });
        
        currentIndex = 0;
        round = 1;
    }

    private int getInitiative(GridObject obj) {
        if (obj instanceof Entity e) {
            return e.getCharSheet().getTotalAttribute(2); // INITIATIVE = 2
        } else if (obj instanceof Enemy enemy) {
            return enemy.getInitiative();
        }
        return 0;
    }

    public GridObject getCurrentCombatant() {
        if (turnOrder.isEmpty()) {
            return null;
        }
        return turnOrder.get(currentIndex);
    }

    public Entity getCurrent() {
        GridObject current = getCurrentCombatant();
        if (current instanceof Entity e) {
            return e;
        }
        return null;
    }

    public Enemy getCurrentEnemy() {
        GridObject current = getCurrentCombatant();
        if (current instanceof Enemy e) {
            return e;
        }
        return null;
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
        GridObject current = getCurrentCombatant();
        if (current instanceof Entity e) {
            e.getCharSheet().procStatus();
        }
    }

    public void removeDeadCombatants() {
        turnOrder.removeIf(obj -> {
            if (obj instanceof Entity e) return e.isDead();
            if (obj instanceof Enemy en) return en.isDead();
            return false;
        });
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
            currentIndex = 0;
            round++;
        }
    }

    public boolean isCurrent(GridObject obj) {
        GridObject current = getCurrentCombatant();
        return current != null && current == obj;
    }

    public List<GridObject> getTurnOrder() {
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
        int idx = turnOrder.indexOf(entity);
        turnOrder.remove(entity);
        if (idx != -1 && idx < currentIndex) {
            currentIndex--;
        }
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
            currentIndex = 0;
        }
    }

    public void addEnemy(Enemy enemy) {
        turnOrder.add(enemy);
        if (battleStarted) {
            calculateInitiativeOrder();
        }
    }

    public void removeEnemy(Enemy enemy) {
        int idx = turnOrder.indexOf(enemy);
        turnOrder.remove(enemy);
        if (idx != -1 && idx < currentIndex) {
            currentIndex--;
        }
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
