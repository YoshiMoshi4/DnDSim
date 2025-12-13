package Objects;

import EntityRes.*;

// can be made abstract later and have subclasses for different players
public class Player extends Entity {

    protected CharSheet charSheet;

    public Player(int row, int col, int movement, CharSheet charSheet) {
        super(row, col, movement, charSheet.getCurrentHP());
        this.charSheet = charSheet;
    }

    public CharSheet getCharSheet() {
        return charSheet;
    }

    public void pickup(Pickup p) {
        charSheet.pickupItem(p.getItem());
    }

    public int getAttackDamage() {
        Weapon w = charSheet.getEquippedWeapon();
        if (w != null) {
            return w.getDamage();
        }
        return 1; // unarmed
    }

    public void addStatus(Status s) {
        charSheet.addStatus(s);
    }

    @Override
    public void takeDamage(int dmg) {
        charSheet.setCurrentHP(charSheet.getCurrentHP() - dmg);
    }

    @Override
    public boolean isDead() {
        return charSheet.getCurrentHP() <= 0;
    }
}
