package UI.Battle;

import Objects.*;
import java.util.*;

public class TurnManager {

    private final List<Entity> turnOrder;
    private int currentIndex = 0;

    public TurnManager(List<Entity> entities) {
        this.turnOrder = new ArrayList<>(entities);
    }

    public Entity getCurrent() {
        return turnOrder.get(currentIndex);
    }

    public void nextTurn() {
        currentIndex = (currentIndex + 1) % turnOrder.size();
    }

    public void removeDeadEntities() {
        turnOrder.removeIf(Entity::isDead);
        if (currentIndex >= turnOrder.size()) {
            currentIndex = 0;
        }
    }

    public boolean isCurrent(Entity e) {
        return getCurrent() == e;
    }
}
