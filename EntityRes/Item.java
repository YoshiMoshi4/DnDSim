package EntityRes;

public class Item {

    // Fields
    private String name;
    private String type;
    private int color; // 0-15 representing different colors
    private int quantity;

    // Constructor
    public Item(String name, String type, int color) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.quantity = 1;
    }

    // Backward compatibility constructor
    public Item(String name, String type) {
        this(name, type, getDefaultColorForType(type));
    }

    private static int getDefaultColorForType(String type) {
        switch (type.toLowerCase()) {
            case "weapon": return 4; // Red
            case "armor": return 9; // Blue
            case "consumable": return 7; // Lime
            case "unarmed": return 1; // Gray
            default: return 8; // Green
        }
    }

    // Methods
    public void setName(String newName) {
        name = newName;
    }

    public String getName() {
        return name;
    }

    public void setType(String newType) {
        type = newType;
    }

    public String getType() {
        return type;
    }

    public void setQuantity(int newQuantity) {
        quantity = newQuantity;
    }

    public void addQuantity(int quantityToAdd) {
        quantity += quantityToAdd;
    }

    public void incQuantity() {
        quantity++;
    }

    public void decQuantity() {
        quantity--;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = Math.max(0, Math.min(15, color));
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
}
