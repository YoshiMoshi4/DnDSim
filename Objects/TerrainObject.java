package Objects;

public class TerrainObject extends GridObject {

    protected String type;
    protected int health;
    protected int color; // 0-15 representing different colors
    protected boolean blocksMovement = true;

    public TerrainObject(int row, int col, String type, int health, int color, boolean blocksMovement) {
        super(row, col);
        this.type = type;
        this.health = health;
        this.color = color;
        this.blocksMovement = blocksMovement;
    }

    public TerrainObject(int row, int col, String type, int health, int color) {
        this(row, col, type, health, color, true); // Default blocks movement
    }

    public TerrainObject(int row, int col, String type, int health) {
        this(row, col, type, health, 8); // Default to green
    }

    public TerrainObject(int row, int col, int health) {
        super(row, col);
        this.type = "Generic";
        this.health = health;
        this.color = 8; // Default to green
    }

    public int getHealth() {
        return health;
    }

    public String getType() {
        return type;
    }

    public boolean blocksMovement() {
        return blocksMovement;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = Math.max(0, Math.min(15, color));
    }

    public void setBlocksMovement(boolean blocksMovement) {
        this.blocksMovement = blocksMovement;
    }

    public java.awt.Color getDisplayColor() {
        switch (color) {
            case 0: return java.awt.Color.BLACK;
            case 1: return java.awt.Color.GRAY;
            case 2: return java.awt.Color.WHITE;
            case 3: return java.awt.Color.RED.darker();
            case 4: return java.awt.Color.RED;
            case 5: return java.awt.Color.ORANGE;
            case 6: return java.awt.Color.YELLOW;
            case 7: return java.awt.Color.GREEN.brighter();
            case 8: return java.awt.Color.GREEN;
            case 9: return java.awt.Color.BLUE;
            case 10: return java.awt.Color.BLUE.darker();
            case 11: return new java.awt.Color(200, 162, 200); // Lilac
            case 12: return new java.awt.Color(128, 0, 128); // Purple
            case 13: return java.awt.Color.PINK;
            case 14: return new java.awt.Color(245, 245, 220); // Beige
            case 15: return new java.awt.Color(139, 69, 19); // Brown
            default: return java.awt.Color.GREEN;
        }
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
