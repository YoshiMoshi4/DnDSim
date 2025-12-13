package Objects;

public abstract class Entity extends GridObject {

    protected int movement;
    protected int health;

    public Entity(int row, int col, int movement, int health) {
        super(row, col);
        this.movement = movement;
        this.health = health;
    }

    public int getMovement() {
        return movement;
    }

    public int getHealth() {
        return health;
    }

    public void moveTo(int r, int c) {
        row = r;
        col = c;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
