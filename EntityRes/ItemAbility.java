package EntityRes;

/**
 * Represents a unique ability that can be attached to weapons or accessories.
 * Abilities trigger at specific game events and produce effects.
 */
public class ItemAbility {
    
    // Trigger type constants - when the ability activates
    public static final String PASSIVE = "PASSIVE";           // Always active (stat bonus, etc.)
    public static final String ON_TURN_START = "ON_TURN_START"; // At start of owner's turn
    public static final String ON_HIT = "ON_HIT";             // When owner hits with attack
    public static final String ON_EQUIP = "ON_EQUIP";         // When item is equipped
    public static final String ON_DAMAGED = "ON_DAMAGED";     // When owner takes damage
    
    // Effect type constants - what the ability does
    public static final String EFFECT_HEAL = "HEAL";          // Heal HP
    public static final String EFFECT_DAMAGE = "DAMAGE";      // Deal damage
    public static final String EFFECT_STAT_BOOST = "STAT_BOOST"; // Boost a stat
    public static final String EFFECT_STATUS = "STATUS";      // Apply a status effect
    
    public static final String[] TRIGGER_TYPES = {PASSIVE, ON_TURN_START, ON_HIT, ON_EQUIP, ON_DAMAGED};
    public static final String[] EFFECT_TYPES = {EFFECT_HEAL, EFFECT_DAMAGE, EFFECT_STAT_BOOST, EFFECT_STATUS};
    
    private String name;
    private String description;
    private String triggerType;
    private String effectType;
    private int magnitude;
    private int targetAttribute; // For STAT_BOOST: 0=STR, 1=DEX, 2=MOB, 3=INT
    private String statusName;   // For STATUS effect: name of status to apply
    
    // Default constructor for Gson
    public ItemAbility() {
        this.name = "Unnamed Ability";
        this.description = "";
        this.triggerType = ON_TURN_START;
        this.effectType = EFFECT_HEAL;
        this.magnitude = 0;
        this.targetAttribute = 0;
        this.statusName = null;
    }
    
    public ItemAbility(String name, String description, String triggerType, String effectType, int magnitude) {
        this.name = name;
        this.description = description;
        this.triggerType = triggerType;
        this.effectType = effectType;
        this.magnitude = magnitude;
        this.targetAttribute = 0;
        this.statusName = null;
    }
    
    // Full constructor
    public ItemAbility(String name, String description, String triggerType, String effectType, 
                       int magnitude, int targetAttribute, String statusName) {
        this.name = name;
        this.description = description;
        this.triggerType = triggerType;
        this.effectType = effectType;
        this.magnitude = magnitude;
        this.targetAttribute = targetAttribute;
        this.statusName = statusName;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTriggerType() {
        return triggerType;
    }
    
    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
    
    public String getEffectType() {
        return effectType;
    }
    
    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }
    
    public int getMagnitude() {
        return magnitude;
    }
    
    public void setMagnitude(int magnitude) {
        this.magnitude = magnitude;
    }
    
    public int getTargetAttribute() {
        return targetAttribute;
    }
    
    public void setTargetAttribute(int targetAttribute) {
        this.targetAttribute = targetAttribute;
    }
    
    public String getStatusName() {
        return statusName;
    }
    
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
    /**
     * Creates a copy of this ability
     */
    public ItemAbility copy() {
        return new ItemAbility(name, description, triggerType, effectType, magnitude, targetAttribute, statusName);
    }
    
    /**
     * Returns a formatted string describing this ability
     */
    public String getFormattedDescription() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(triggerType.replace("_", " ")).append(": ");
        
        switch (effectType) {
            case EFFECT_HEAL:
                sb.append("Heal ").append(magnitude).append(" HP");
                break;
            case EFFECT_DAMAGE:
                sb.append("Deal ").append(magnitude).append(" damage");
                break;
            case EFFECT_STAT_BOOST:
                String[] attrs = {"STR", "DEX", "MOB", "INT"};
                String attr = (targetAttribute >= 0 && targetAttribute < attrs.length) ? attrs[targetAttribute] : "?";
                sb.append(magnitude > 0 ? "+" : "").append(magnitude).append(" ").append(attr);
                break;
            case EFFECT_STATUS:
                sb.append("Apply ").append(statusName != null ? statusName : "status");
                break;
            default:
                sb.append(magnitude);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return name + " (" + getFormattedDescription() + ")";
    }
}
