package Objects;

import EntityRes.CharSheet;
import EntityRes.Status;
import EntityRes.Weapon;

public class Entity extends GridObject {

    protected CharSheet charSheet;
    private int instanceNumber = 0;  // 0 = not set (party entity), >0 = non-party instance number
    private String baseName;  // Original name without instance number

    public Entity(int row, int col, CharSheet charSheet) {
        super(row, col);
        this.charSheet = charSheet;
        this.baseName = charSheet.getName();
    }

    @Deprecated
    public void attack(Entity target) {
        int damage = getAttackPower() - target.getDefense();
        target.takeDamage(Math.max(0, damage));  // Minimum 0 damage
    }

    @Deprecated
    public int getAttackPower() {
        // Legacy: Attack damage = weapon damage + strength attribute
        // Default unarmed damage is 1
        int weaponDamage = charSheet.getEquippedWeapon() != null ? charSheet.getEquippedWeapon().getDamage() : 1;
        int strength = charSheet.getAttribute(0); // STRENGTH = 0
        return weaponDamage + strength;
    }
    
    /**
     * Get Armor Class for attack roll calculations
     * AC is based on total defense from equipped armor
     */
    public int getAC() {
        return 10 + charSheet.getTotalDefense();  // Base AC 10 + armor bonus
    }

    @Deprecated
    public int getDefense() {
        return charSheet.getTotalDefense();
    }
    
    /**
     * Get the stat modifier used for attack rolls with current weapon
     */
    public int getAttackModifier() {
        Weapon weapon = charSheet.getEquippedWeapon();
        if (weapon != null) {
            int statIndex = weapon.getStatIndex();
            return charSheet.getTotalAttribute(statIndex);
        }
        // Unarmed uses STR
        return charSheet.getTotalAttribute(0);
    }
    
    /**
     * Get damage dice from equipped weapon
     */
    public String[] getDamageDice() {
        Weapon weapon = charSheet.getEquippedWeapon();
        if (weapon != null) {
            return weapon.getDamageDice();
        }
        // Unarmed dice
        return new String[]{"d4", "d4", "d4"};
    }

    public int getMovement() {
        return charSheet.getAttribute(2);
    }

    public int getHealth() {
        return charSheet.getCurrentHP();
    }

    public String getName() {
        if (instanceNumber > 0) {
            return baseName + " #" + instanceNumber;
        }
        return charSheet.getName();
    }

    public CharSheet getCharSheet() {
        return charSheet;
    }
    
    public void setInstanceNumber(int number) {
        this.instanceNumber = number;
    }
    
    public int getInstanceNumber() {
        return instanceNumber;
    }
    
    public String getBaseName() {
        return baseName;
    }

    public void moveTo(int r, int c) {
        row = r;
        col = c;
    }

    public void takeDamage(int dmg) {
        charSheet.setCurrentHP(charSheet.getCurrentHP() - dmg);
        charSheet.save();
    }

    public boolean isDead() {
        return charSheet.getCurrentHP() <= 0;
    }

    public void pickup(Pickup p) {
        charSheet.pickupItem(p.getItem());
        charSheet.save();
    }

    public void addStatus(Status s) {
        charSheet.addStatus(s);
        charSheet.save();
    }

    public boolean isParty() {
        return charSheet.getParty();
    }
}
