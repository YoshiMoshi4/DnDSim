package EntityRes;

public class Item {

    // Fields
    private String name;
    private String type;
    private int quantity;

    // Constructor
    public Item(String name, String type) {
        this.name = name;
        this.type = type;
        this.quantity = 1;
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
}
