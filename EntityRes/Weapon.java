package EntityRes;

public class Weapon extends Item {

    // Fields
    private String[] damageDice;  // Damage dice per tier [tier1, tier2, tier3], e.g. ["d6", "d6", "d10"]
    private String statType;      // "STRENGTH" or "DEXTERITY" - which stat modifier to use
    private int[] modifiedAttributes;
    
    // Legacy field for backward compatibility during migration
    private int damage;

    // Constructor
    public Weapon(String name, String type, String[] damageDice, String statType, int[] modifiedAttributes) {
        super(name, type);
        this.damageDice = damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
        this.statType = statType != null ? statType : "STRENGTH";
        this.modifiedAttributes = modifiedAttributes;
    }
    
    // Legacy constructor for backward compatibility
    public Weapon(String name, String type, int damage, int[] modifiedAttributes) {
        super(name, type);
        this.damage = damage;
        // Convert legacy damage to dice tiers (approximate)
        this.damageDice = convertLegacyDamage(damage);
        this.statType = "STRENGTH";
        this.modifiedAttributes = modifiedAttributes;
    }
    
    /**
     * Convert legacy flat damage to dice tiers (for migration)
     */
    private String[] convertLegacyDamage(int damage) {
        if (damage <= 3) return new String[]{"d4", "d4", "d6"};
        if (damage <= 6) return new String[]{"d6", "d6", "d8"};
        if (damage <= 10) return new String[]{"d6", "d6", "d10"};
        return new String[]{"d8", "d8", "d12"};
    }

    // Methods
    public void setDamageDice(String[] damageDice) {
        this.damageDice = damageDice;
    }

    public String[] getDamageDice() {
        // Handle legacy weapons that only have damage field
        if (damageDice == null && damage > 0) {
            damageDice = convertLegacyDamage(damage);
        }
        return damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
    }
    
    public String getStatType() {
        return statType != null ? statType : "STRENGTH";
    }
    
    public void setStatType(String statType) {
        this.statType = statType;
    }
    
    /**
     * Get the stat index for CharSheet (0=STR, 1=DEX)
     */
    public int getStatIndex() {
        if ("DEXTERITY".equalsIgnoreCase(statType)) {
            return 1; // DEX
        }
        return 0; // STR default
    }
    
    // Legacy method for backward compatibility
    @Deprecated
    public void setDamage(int damage) {
        this.damage = damage;
        this.damageDice = convertLegacyDamage(damage);
    }

    @Deprecated
    public int getDamage() {
        // For backward compatibility, calculate average damage from tier 1
        if (damageDice != null && damageDice.length > 0) {
            return getAverageDieValue(damageDice[0]);
        }
        return damage > 0 ? damage : 4;
    }
    
    private int getAverageDieValue(String die) {
        if (die == null) return 4;
        switch (die.toLowerCase()) {
            case "d4": return 2;
            case "d6": return 3;
            case "d8": return 4;
            case "d10": return 5;
            case "d12": return 6;
            case "d20": return 10;
            default: return 4;
        }
    }

    public void setModifiedAttributes(int[] modifiedAttributes) {
        if (modifiedAttributes.length == this.modifiedAttributes.length) {
            for (int i = 0; i < modifiedAttributes.length; i++) {
                this.modifiedAttributes[i] = modifiedAttributes[i];
            }
        } else {
            // Temp exception
            System.out.println("attributesLength Error");
        }
    }

    public int[] getModifiedAttributes() {
        int[] ans = new int[modifiedAttributes.length];
        for (int i = 0; i < modifiedAttributes.length; i++) {
            ans[i] = modifiedAttributes[i];
        }
        return ans;
    }

}
