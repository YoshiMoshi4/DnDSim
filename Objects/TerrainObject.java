package Objects;

import EntityRes.ColorUtils;

public class TerrainObject extends GridObject {

    protected String type;
    protected int health;
    protected String color;
    protected boolean blocksMovement = true;
    protected String spritePath; // Optional sprite image path

    public TerrainObject(int row, int col, String type, int health, String color, boolean blocksMovement) {
        super(row, col);
        this.type = type;
        this.health = health;
        this.color = ColorUtils.normalizeHex(color, ColorUtils.fromLegacyIndex(8));
        this.blocksMovement = blocksMovement;
    }

    public TerrainObject(int row, int col, String type, int health, int color, boolean blocksMovement) {
        this(row, col, type, health, ColorUtils.fromLegacyIndex(color), blocksMovement);
    }

    public TerrainObject(int row, int col, String type, int health, String color) {
        this(row, col, type, health, color, true); // Default blocks movement
    }

    public TerrainObject(int row, int col, String type, int health, int color) {
        this(row, col, type, health, ColorUtils.fromLegacyIndex(color), true);
    }

    public TerrainObject(int row, int col, String type, int health) {
        this(row, col, type, health, ColorUtils.fromLegacyIndex(8));
    }

    public TerrainObject(int row, int col, int health) {
        super(row, col);
        this.type = "Generic";
        this.health = health;
        this.color = ColorUtils.fromLegacyIndex(8);
    }

    public int getHealth() {
        return health;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null && !type.trim().isEmpty()) {
            this.type = type.trim();
        }
    }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    public boolean blocksMovement() {
        return blocksMovement;
    }

    public String getColor() {
        color = ColorUtils.normalizeHex(color, ColorUtils.fromLegacyIndex(8));
        return color;
    }

    public void setColor(String color) {
        this.color = ColorUtils.normalizeHex(color, ColorUtils.fromLegacyIndex(8));
    }

    public void setColor(int color) {
        setColor(ColorUtils.fromLegacyIndex(color));
    }

    public void setBlocksMovement(boolean blocksMovement) {
        this.blocksMovement = blocksMovement;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public void setSpritePath(String spritePath) {
        this.spritePath = spritePath;
    }

    public java.awt.Color getDisplayColor() {
        return ColorUtils.toAwtColor(color, ColorUtils.fromLegacyIndex(8));
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
