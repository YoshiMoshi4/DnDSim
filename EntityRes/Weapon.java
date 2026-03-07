package EntityRes;

import java.util.ArrayList;

public class Weapon extends Item {

    // Fields
    private String[] damageDice;  // Damage dice per tier [tier1, tier2, tier3], e.g. ["d6", "d6", "d10"]
    private String statType;      // "STRENGTH" or "DEXTERITY" - which stat modifier to use
    private String ammoType;      // Ammo type required (null for melee weapons), e.g. "12 Gauge", "Arrow"
    private int[] modifiedAttributes;
    private ArrayList<ItemAbility> abilities;
    
    // Legacy field for backward compatibility during migration
    private int damage;

    // Constructor
    public Weapon(String name, String type, String[] damageDice, String statType, int[] modifiedAttributes) {
        super(name, type);
        this.damageDice = damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
        this.statType = statType != null ? statType : "STRENGTH";
        this.modifiedAttributes = modifiedAttributes;
        this.ammoType = null;  // Default: melee weapon
        this.abilities = new ArrayList<>();
    }
    
    // Constructor with ammo type
    public Weapon(String name, String type, String[] damageDice, String statType, String ammoType, int[] modifiedAttributes) {
        super(name, type);
        this.damageDice = damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
        this.statType = statType != null ? statType : "STRENGTH";
        this.ammoType = ammoType;
        this.modifiedAttributes = modifiedAttributes;
        this.abilities = new ArrayList<>();
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
    
    /**
     * Get the ammo type this weapon uses (null for melee weapons)
     */
    public String getAmmoType() {
        return ammoType;
    }
    
    public void setAmmoType(String ammoType) {
        this.ammoType = ammoType;
    }
    
    /**
     * Check if this weapon is ranged (requires ammo)
     */
    public boolean isRanged() {
        return ammoType != null && !ammoType.isEmpty();
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
        // Always store as 4-element array
        this.modifiedAttributes = new int[4];
        if (modifiedAttributes != null) {
            for (int i = 0; i < modifiedAttributes.length && i < 4; i++) {
                this.modifiedAttributes[i] = modifiedAttributes[i];
            }
        }
    }

    public int[] getModifiedAttributes() {
        // Always return 4-element array (padded with zeros for older items)
        int[] ans = new int[4];
        if (modifiedAttributes != null) {
            for (int i = 0; i < modifiedAttributes.length && i < 4; i++) {
                ans[i] = modifiedAttributes[i];
            }
        }
        return ans;
    }
    
    // Abilities
    public ArrayList<ItemAbility> getAbilities() {
        if (abilities == null) abilities = new ArrayList<>();
        return abilities;
    }
    
    public void setAbilities(ArrayList<ItemAbility> abilities) {
        this.abilities = abilities != null ? abilities : new ArrayList<>();
    }
    
    public void addAbility(ItemAbility ability) {
        if (abilities == null) abilities = new ArrayList<>();
        abilities.add(ability);
    }
    
    public void removeAbility(ItemAbility ability) {
        if (abilities != null) abilities.remove(ability);
    }
    
    public void clearAbilities() {
        if (abilities != null) abilities.clear();
    }
    
    public boolean hasAbilities() {
        return abilities != null && !abilities.isEmpty();
    }

}
