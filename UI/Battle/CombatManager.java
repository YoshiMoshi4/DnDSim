package UI.Battle;

import Objects.Entity;
import Objects.Enemy;
import Objects.GridObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages combat calculations for the margin-based attack tier system.
 * 
 * Attack Flow:
 * 1. Attacker rolls d20
 * 2. Calculate margin = (d20 + stat modifier) - target AC
 * 3. If margin < 0, attack misses
 * 4. Otherwise, determine tier: 0-3 = tier 1, 4-6 = tier 2, 7+ = tier 3
 * 5. For margin > 7, add bonus damage = margin - 7
 * 6. Roll damage dice based on tier (cumulative: tier 2 includes tier 1 dice, etc.)
 */
public class CombatManager {

    /**
     * Result of an attack roll calculation
     */
    public static class AttackResult {
        public final int d20Roll;
        public final int modifier;
        public final int targetAC;
        public final int margin;
        public final boolean hit;
        public final int tier;        // 0 = miss, 1-3 = hit tier
        public final int bonusDamage; // Extra damage for margin > 7
        public final String[] diceToRoll;
        
        public AttackResult(int d20Roll, int modifier, int targetAC) {
            this.d20Roll = d20Roll;
            this.modifier = modifier;
            this.targetAC = targetAC;
            this.margin = (d20Roll + modifier) - targetAC;
            this.hit = margin >= 0;
            this.tier = hit ? calculateTier(margin) : 0;
            this.bonusDamage = margin > 7 ? margin - 7 : 0;
            this.diceToRoll = new String[0]; // Set separately when weapon is known
        }
        
        private static int calculateTier(int margin) {
            if (margin < 0) return 0;
            if (margin <= 3) return 1;
            if (margin <= 6) return 2;
            return 3;
        }
    }
    
    /**
     * Calculate attack result given d20 roll, modifier, and target AC
     */
    public static AttackResult calculateAttack(int d20Roll, int modifier, int targetAC) {
        return new AttackResult(d20Roll, modifier, targetAC);
    }
    
    /**
     * Calculate margin from attack roll
     * @return margin value (negative = miss)
     */
    public static int calculateMargin(int d20Roll, int statModifier, int targetAC) {
        return (d20Roll + statModifier) - targetAC;
    }
    
    /**
     * Determine attack tier from margin
     * @return 0 = miss, 1-3 = hit tier
     */
    public static int getAttackTier(int margin) {
        if (margin < 0) return 0;
        if (margin <= 3) return 1;
        if (margin <= 6) return 2;
        return 3;
    }
    
    /**
     * Get bonus damage for margin > 7
     */
    public static int getBonusDamage(int margin) {
        return margin > 7 ? margin - 7 : 0;
    }
    
    /**
     * Get list of dice to roll for a given tier and weapon dice configuration.
     * Tiers are cumulative: tier 2 includes tier 1 dice, tier 3 includes tier 1 & 2.
     * 
     * @param weaponDice Array of 3 dice strings [tier1, tier2, tier3]
     * @param tier Attack tier (1, 2, or 3)
     * @return List of dice strings to roll
     */
    public static List<String> getDiceForTier(String[] weaponDice, int tier) {
        List<String> dice = new ArrayList<>();
        if (weaponDice == null || weaponDice.length < 3) {
            weaponDice = new String[]{"d4", "d4", "d6"}; // Default
        }
        
        if (tier >= 1 && weaponDice[0] != null) {
            dice.add(weaponDice[0]);
        }
        if (tier >= 2 && weaponDice[1] != null) {
            dice.add(weaponDice[1]);
        }
        if (tier >= 3 && weaponDice[2] != null) {
            dice.add(weaponDice[2]);
        }
        
        return dice;
    }
    
    /**
     * Format dice list for display (e.g., "2d6 + 1d10")
     */
    public static String formatDiceList(List<String> dice) {
        if (dice == null || dice.isEmpty()) return "0";
        
        // Count occurrences of each die type
        java.util.Map<String, Integer> diceCounts = new java.util.LinkedHashMap<>();
        for (String die : dice) {
            diceCounts.merge(die, 1, Integer::sum);
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (var entry : diceCounts.entrySet()) {
            if (!first) sb.append(" + ");
            if (entry.getValue() > 1) {
                sb.append(entry.getValue());
            }
            sb.append(entry.getKey());
            first = false;
        }
        
        return sb.toString();
    }
    
    /**
     * Get attack modifier for an attacker (Entity or Enemy)
     */
    public static int getAttackModifier(GridObject attacker) {
        if (attacker instanceof Entity e) {
            return e.getAttackModifier();
        } else if (attacker instanceof Enemy en) {
            return en.getAttackModifier();
        }
        return 0;
    }
    
    /**
     * Get AC for a target (Entity or Enemy)
     */
    public static int getTargetAC(GridObject target) {
        if (target instanceof Entity e) {
            return e.getAC();
        } else if (target instanceof Enemy en) {
            return en.getAC();
        }
        return 10; // Default AC
    }
    
    /**
     * Get damage dice for an attacker
     */
    public static String[] getDamageDice(GridObject attacker) {
        if (attacker instanceof Entity e) {
            return e.getDamageDice();
        } else if (attacker instanceof Enemy en) {
            return en.getDamageDice();
        }
        return new String[]{"d4", "d4", "d6"};
    }
    
    /**
     * Get name of attacker for display
     */
    public static String getAttackerName(GridObject attacker) {
        if (attacker instanceof Entity e) {
            return e.getName();
        } else if (attacker instanceof Enemy en) {
            return en.getName();
        }
        return "Unknown";
    }
    
    /**
     * Get name of target for display
     */
    public static String getTargetName(GridObject target) {
        if (target instanceof Entity e) {
            return e.getName();
        } else if (target instanceof Enemy en) {
            return en.getName();
        }
        return "Unknown";
    }
    
    /**
     * Apply damage to target
     */
    public static void applyDamage(GridObject target, int damage) {
        if (target instanceof Entity e) {
            e.takeDamage(damage);
        } else if (target instanceof Enemy en) {
            en.takeDamage(damage);
        }
    }
    
    /**
     * Check if target is dead
     */
    public static boolean isTargetDead(GridObject target) {
        if (target instanceof Entity e) {
            return e.isDead();
        } else if (target instanceof Enemy en) {
            return en.isDead();
        }
        return false;
    }
    
    /**
     * Get current health of target
     */
    public static int getTargetHealth(GridObject target) {
        if (target instanceof Entity e) {
            return e.getHealth();
        } else if (target instanceof Enemy en) {
            return en.getHealth();
        }
        return 0;
    }
    
    /**
     * Get max health of target
     */
    public static int getTargetMaxHealth(GridObject target) {
        if (target instanceof Entity e) {
            return e.getCharSheet().getTotalHP();
        } else if (target instanceof Enemy en) {
            return en.getMaxHealth();
        }
        return 1;
    }
    
    /**
     * Get stat type name for display
     */
    public static String getStatTypeName(GridObject attacker) {
        if (attacker instanceof Entity e) {
            var weapon = e.getCharSheet().getEquippedWeapon();
            if (weapon != null) {
                return weapon.getStatType();
            }
        }
        return "STRENGTH";
    }
}
