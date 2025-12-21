package Objects;

//not sure if enemies use the charSheet or are different or what, can be changed to mimic player
import EntityRes.CharSheet;

public class Enemy extends Entity {

    public Enemy(int row, int col, CharSheet charSheet) {
        super(row, col, charSheet);
    }

    @Override
    public void attack(Entity target) {
        if (target instanceof Player player) {
            // Implement enemy attack logic here
            // For example, reduce the player's health based on the enemy's attack power
            int attackPower = 10; // Example attack power
            player.takeDamage(attackPower);
        }
    }

    public boolean isAlive() {
        // Implement enemy alive check logic here
        // For example, return true if the enemy's health is greater than 0, otherwise return false
        return charSheet.getCurrentHP() > 0;
    }

}
