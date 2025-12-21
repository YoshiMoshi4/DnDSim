package Objects;

public abstract class TerrainObject extends GridObject {

    protected int health;
    protected boolean blocksMovement = true;

    public TerrainObject(int row, int col, int health) {
        super(row, col);
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public boolean blocksMovement() {
        return blocksMovement;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
        if (health <= 0) {
            onDestroyed();
        }
    }

    //Example override:
    // @Override
    // protected void onDestroyed() {
    //     grid.addPickup(new Pickup(row, col,
    //             new Weapon("Iron Axe", "Weapon", 6, new int[]{1, 0, 0})
    //     ));
    // }
    protected void onDestroyed() {
        // Default: do nothing
        // Subclasses override for effects

    }

    public boolean isDestroyed() {
        return health <= 0;
    }
}
