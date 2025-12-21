package Objects;

import EntityRes.CharSheet;

public abstract class Entity extends GridObject {

    protected CharSheet charSheet;

    public Entity(int row, int col, CharSheet charSheet) {
        super(row, col);
        this.charSheet = charSheet;
    }

    public abstract void attack(Entity target);

    public int getAttackPower() {
        return charSheet.getEquippedWeapon().getDamage();
    }

    public int getMovement() {
        return charSheet.getAttribute(3);
    }

    public int getHealth() {
        return charSheet.getCurrentHP();
    }

    public String getName() {
        return charSheet.getName();
    }

    public CharSheet getCharSheet() {
        return charSheet;
    }

    public void moveTo(int r, int c) {
        row = r;
        col = c;
    }

    public void takeDamage(int dmg) {
        charSheet.setCurrentHP(charSheet.getCurrentHP() - dmg);
    }

    public boolean isDead() {
        return charSheet.getCurrentHP() <= 0;
    }
}
