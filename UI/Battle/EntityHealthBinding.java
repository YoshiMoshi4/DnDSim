package UI.Battle;

import Objects.Entity;
import javafx.beans.property.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;

/**
 * Observable wrapper for Entity health to enable UI bindings.
 * Provides reactive properties for current HP, max HP, and HP ratio.
 */
public class EntityHealthBinding {

    private final Entity entity;
    private final IntegerProperty currentHP = new SimpleIntegerProperty();
    private final IntegerProperty maxHP = new SimpleIntegerProperty();
    private final DoubleProperty healthRatio = new SimpleDoubleProperty();
    private final StringProperty healthText = new SimpleStringProperty();
    private final StringProperty healthStatus = new SimpleStringProperty();

    public EntityHealthBinding(Entity entity) {
        this.entity = entity;
        refresh();
        
        // Health ratio binding
        healthRatio.bind(Bindings.createDoubleBinding(
            () -> maxHP.get() > 0 ? (double) currentHP.get() / maxHP.get() : 0,
            currentHP, maxHP
        ));
        
        // Health text binding (e.g., "25/50")
        healthText.bind(Bindings.createStringBinding(
            () -> currentHP.get() + "/" + maxHP.get(),
            currentHP, maxHP
        ));
        
        // Health status binding (HEALTHY, WOUNDED, CRITICAL, DEAD)
        healthStatus.bind(Bindings.createStringBinding(
            () -> {
                double ratio = healthRatio.get();
                if (ratio <= 0) return "DEAD";
                if (ratio <= 0.25) return "CRITICAL";
                if (ratio <= 0.5) return "WOUNDED";
                return "HEALTHY";
            },
            healthRatio
        ));
    }

    /**
     * Refresh values from the underlying entity.
     * Call this after any operation that changes entity HP.
     */
    public void refresh() {
        currentHP.set(entity.getHealth());
        maxHP.set(entity.getCharSheet().getTotalHP());
    }
    
    /**
     * Manually set current HP (also updates the underlying entity).
     */
    public void setCurrentHP(int hp) {
        entity.getCharSheet().setCurrentHP(hp);
        currentHP.set(hp);
    }
    
    /**
     * Apply damage to the entity.
     */
    public void applyDamage(int damage) {
        int newHP = Math.max(0, currentHP.get() - damage);
        setCurrentHP(newHP);
    }
    
    /**
     * Apply healing to the entity.
     */
    public void applyHealing(int healing) {
        int newHP = Math.min(maxHP.get(), currentHP.get() + healing);
        setCurrentHP(newHP);
    }

    // ===== PROPERTY ACCESSORS =====
    
    public Entity getEntity() { return entity; }
    
    public int getCurrentHP() { return currentHP.get(); }
    public IntegerProperty currentHPProperty() { return currentHP; }
    
    public int getMaxHP() { return maxHP.get(); }
    public IntegerProperty maxHPProperty() { return maxHP; }
    
    public double getHealthRatio() { return healthRatio.get(); }
    public ReadOnlyDoubleProperty healthRatioProperty() { return healthRatio; }
    
    public String getHealthText() { return healthText.get(); }
    public ReadOnlyStringProperty healthTextProperty() { return healthText; }
    
    public String getHealthStatus() { return healthStatus.get(); }
    public ReadOnlyStringProperty healthStatusProperty() { return healthStatus; }
    
    public boolean isDead() { return currentHP.get() <= 0; }
}
