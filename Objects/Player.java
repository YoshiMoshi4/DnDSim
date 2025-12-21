package Objects;

import EntityRes.*;

// can be made abstract later and have subclasses for different players
public class Player extends Entity {

    public Player(int row, int col, CharSheet charSheet) {
        super(row, col, charSheet);
    }

    @Override
    public void attack(Entity target) {
        target.takeDamage(getAttackPower());
    }

    public void pickup(Pickup p) {
        charSheet.pickupItem(p.getItem());
    }

    public void addStatus(Status s) {
        charSheet.addStatus(s);
    }
}
