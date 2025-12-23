package EntityRes;

public class Weapon extends Item {

    // Fields
    private int damage;
    private int[] modifiedAttributes;

    // Constructor
    public Weapon(String name, String type, int damage, int[] modifiedAttributes) {
        super(name, type);
        this.damage = damage;
        this.modifiedAttributes = modifiedAttributes;
    }

    // Methods
    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public void setModifiedAttributes(int[] modifiedAttributes) {
        if (modifiedAttributes.length == this.modifiedAttributes.length) {
            for (int i = 0; i < modifiedAttributes.length; i++) {
                this.modifiedAttributes[i] = modifiedAttributes[i];
            }
        } else {
            // Temp exception
            System.out.println("attributesLength Error");
        }
    }

    public int[] getModifiedAttributes() {
        int[] ans = new int[modifiedAttributes.length];
        for (int i = 0; i < modifiedAttributes.length; i++) {
            ans[i] = modifiedAttributes[i];
        }
        return ans;
    }

}
