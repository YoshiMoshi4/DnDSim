package Objects;

import EntityRes.CharSheet;
import EntityRes.Status;

public class Entity extends GridObject {

    protected CharSheet charSheet;
    private int instanceNumber = 0;  // 0 = not set (party entity), >0 = non-party instance number
    private String baseName;  // Original name without instance number

    public Entity(int row, int col, CharSheet charSheet) {
        super(row, col);
        this.charSheet = charSheet;
        this.baseName = charSheet.getName();
    }

    public void attack(Entity target) {
        int damage = getAttackPower() - target.getDefense();
        target.takeDamage(Math.max(0, damage));  // Minimum 0 damage
    }

    public int getAttackPower() {
        // Attack damage = weapon damage + strength attribute
        int weaponDamage = charSheet.getEquippedWeapon().getDamage();
        int strength = charSheet.getAttribute(0); // STRENGTH = 0
        return weaponDamage + strength;
    }

    public int getDefense() {
        return charSheet.getTotalDefense();
    }

    public int getMovement() {
        return charSheet.getAttribute(3);
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
