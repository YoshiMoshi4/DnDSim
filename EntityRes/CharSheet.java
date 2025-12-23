package EntityRes;

import java.util.ArrayList;

public class CharSheet {

    // Meta
    private String name;
    private int cellColor;
    private final int BLACK = 0;
    private final int GRAY = 1;
    private final int WHITE = 2;
    private final int MAROON = 3;
    private final int RED = 4;
    private final int ORANGE = 5;
    private final int YELLOW = 6;
    private final int LIME = 7;
    private final int GREEN = 8;
    private final int BLUE = 9;
    private final int INDIGO = 10;
    private final int LILAC = 11;
    private final int PURPLE = 12;
    private final int PINK = 13;
    private final int BEIGE = 14;
    private final int BROWN = 15;
    private boolean isParty;

    // HP and Statuses
    private int totalHP;
    private int currentHP;
    private ArrayList<Status> status;
    private final Status poison = new Status("Poison");

    // Attributes
    private int[] baseAttributes;
    private final int STRENGTH = 0;
    private final int DEXTERITY = 1;
    private final int INITIATIVE = 2;
    private final int MOBILITY = 3;

    // Weapons
    private Weapon[] weapons;
    private final int PRIMARY = 0;
    private final int SECONDARY = 1;

    // Armor
    private Armor[] armor;
    private final int HEAD = 0;
    private final int TORSO = 1;
    private final int LEGS = 2;

    // Inventory
    private ArrayList<Item> inventory;
    private int wallet;

    //Constructor
    public CharSheet(String name, boolean isParty, int totalHP, int[] baseAttributes) {
        this.name = name;
        cellColor = 0;
        this.isParty = isParty;

        this.totalHP = totalHP;
        currentHP = totalHP;
        status = new ArrayList<Status>();

        this.baseAttributes = new int[baseAttributes.length];
        if (baseAttributes.length == this.baseAttributes.length) {
            for (int i = 0; i < baseAttributes.length; i++) {
                this.baseAttributes[i] = baseAttributes[i];
            }
        } else {
            // Temp exception
            System.out.println("attributesLength Error");
        }

        Weapon fist = new Weapon("Fist", "Unarmed", 1, new int[]{0, 0, 0, 0});

        Armor bald = new Armor("Naked", "Armor", 0, 0, new int[]{0, 0, 0, 0});
        Armor bareChest = new Armor("Bare Chest", "Armor", 1, 0, new int[]{0, 0, 0, 0});
        Armor noPants = new Armor("No Pants", "Armor", 2, 0, new int[]{0, 0, 0, 0});

        weapons = new Weapon[2];
        weapons[0] = fist;
        weapons[1] = fist;

        armor = new Armor[3];
        armor[0] = bald;
        armor[1] = bareChest;
        armor[2] = noPants;
        inventory = new ArrayList<Item>();
    }

    // Methods
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParty(boolean isParty) {
        this.isParty = isParty;
    }

    public boolean getParty() {
        return isParty;
    }

    public void setCellColor(int cellColor) {

        this.cellColor = cellColor % 16;
    }

    public int getCellColor() {
        return cellColor;
    }

    public void setTotalHP(int newHP) {
        totalHP = newHP;
    }

    public int getTotalHP() {
        return totalHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int newHP) {
        currentHP = newHP;
    }

    public void addCurrentHP(int HPtoAdd) {
        currentHP += HPtoAdd;
    }

    public void mulCurrentHP(double factorToMul) {
        int temp = (int) (Math.ceil(currentHP * factorToMul));
        if (temp < 1) {
            currentHP = 1;
        } else {
            currentHP = temp;
        }
    }

    public void addStatus(Status newStatus) {
        if (!isInStatus(newStatus)) {
            status.add(newStatus);
        }
    }

    public Status removeStatus(Status statusToRemove) {
        Status ans = new Status("temp");
        if (isInStatus(statusToRemove)) {
            ans = statusToRemove;
            status.remove(statusToRemove);
        } else {
            // Temp exception
            System.out.println("statusRemoval Error");
        }
        return ans;
    }

    public void clearStatus() {
        status.clear();
    }

    public Status[] getStatus() {
        Status[] ans = new Status[status.size()];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = status.get(i);
        }
        return ans;
    }

    public void procStatus() // TBI
    {
        
    }

    public void setAttributes(int[] values) {
        if (values.length != baseAttributes.length) {
            // Temp exception
            System.out.println("attributeLength Error");
        } else {
            for (int i = 0; i < baseAttributes.length; i++) {
                baseAttributes[i] = values[i];
            }
        }
    }

    public void setAttribute(int attribute, int value) {
        baseAttributes[attribute] = value;
    }

    public void incAttribute(int attribute) {
        baseAttributes[attribute] += 1;
    }

    public int[] getAttributes() {
        int[] ans = new int[baseAttributes.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = baseAttributes[i];
        }
        return ans;
    }

    public int getAttribute(int attribute) {
        return baseAttributes[attribute];
    }

    public void updateAttributes() {
        int[] headAttr = armor[0].getModifiedAttributes();
        int[] torsoAttr = armor[1].getModifiedAttributes();
        int[] legsAttr = armor[2].getModifiedAttributes();
        int[] weapAttr = weapons[0].getModifiedAttributes();
        for (int i = 0; i < headAttr.length; i++) {
            baseAttributes[i] += (headAttr[i] + torsoAttr[i] + legsAttr[i] + weapAttr[i]);
        }
    }

    public void equipPrimaryWeapon(Weapon newWeapon) {
        weapons[PRIMARY] = newWeapon;
        // For when inventory is properly implemented
            // int temp = indexInInventory(newWeapon);
            // if (temp == -1) {
            //     // Temp exception
            //     System.out.println("notInInventory Error");
            // } else if (weapons[PRIMARY] == null) {
            //     weapons[PRIMARY] = (Weapon) inventory.get(temp);
            //     inventory.remove(temp);
            // } else {
            //     inventory.add(weapons[PRIMARY]);
            //     weapons[PRIMARY] = (Weapon) inventory.get(temp);
            //     inventory.remove(temp);
            // }
        updateAttributes();

    }

    public void equipSecondaryWeapon(Weapon newWeapon) {
        weapons[SECONDARY] = newWeapon;
        // For when inventory is properly implemented
            // int temp = indexInInventory(newWeapon);
            // if (temp == -1) {
            //     // Temp exception
            //     System.out.println("notInInventory Error");
            // } else if (weapons[SECONDARY] == null) {
            //     weapons[SECONDARY] = (Weapon) inventory.get(temp);
            //     inventory.remove(temp);
            // } else {
            //     inventory.add(weapons[SECONDARY]);
            //     weapons[SECONDARY] = (Weapon) inventory.get(temp);
            //     inventory.remove(temp);
            // }
    }

    public Weapon getEquippedWeapon() {
        return weapons[PRIMARY];
    }

    public Weapon getEquippedSecondary() {
        return weapons[SECONDARY];
    }

    public void unequipPrimaryWeapon() {
        inventory.add(weapons[PRIMARY]);
        weapons[PRIMARY] = null;
        updateAttributes();
    }

    public void unequipSecondaryWeapon() {
        inventory.add(weapons[SECONDARY]);
        weapons[SECONDARY] = null;
    }

    public void swapWeapons() {
        Weapon temp = weapons[PRIMARY];
        weapons[PRIMARY] = weapons[SECONDARY];
        weapons[SECONDARY] = temp;
        updateAttributes();
    }

    public void equipHead(Armor newArmor) {
        armor[HEAD] = newArmor;
        // For when inventory is properly implemented
            // int temp = indexInInventory(newArmor);
            // if (temp == -1) {
            //     // Temp exception
            //     System.out.println("notInInventory Error");
            // }
            // if (newArmor.getArmorType() == HEAD) {
            //     if (armor[HEAD] == null) {
            //         armor[HEAD] = (Armor) inventory.get(temp);
            //         inventory.remove(temp);
            //     } else {
            //         inventory.add(armor[HEAD]);
            //         armor[HEAD] = (Armor) inventory.get(temp);
            //         inventory.remove(temp);
            //     }
            // }
        updateAttributes();
    }

    public void equipTorso(Armor newArmor) {
        armor[TORSO] = newArmor;
        // For when inventory is properly implemented
            // int temp = indexInInventory(newArmor);
            // if (temp == -1) {
            //     // Temp exception
            //     System.out.println("notInInventory Error");
            // }
            // if (newArmor.getArmorType() == TORSO) {
            //     if (armor[TORSO] == null) {
            //         armor[TORSO] = (Armor) inventory.get(temp);
            //         inventory.remove(temp);
            //     } else {
            //         inventory.add(armor[TORSO]);
            //         armor[TORSO] = (Armor) inventory.get(temp);
            //         inventory.remove(temp);
            //     }
            // }
        updateAttributes();
    }

    public void equipLegs(Armor newArmor) {
        armor[LEGS] = newArmor;
        // For when inventory is properly implemented
            // int temp = indexInInventory(newArmor);
            // if (temp == -1) {
            //     // Temp exception
            //     System.out.println("notInInventory Error");
            // }
            // if (newArmor.getArmorType() == LEGS) {
            //     if (armor[LEGS] == null) {
            //         armor[LEGS] = (Armor) inventory.get(temp);
            //         inventory.remove(temp);
            //     } else {
            //         inventory.add(armor[LEGS]);
            //         armor[LEGS] = (Armor) inventory.get(temp);
            //         inventory.remove(temp);
            //     }
            // }
        updateAttributes();
    }

    public Armor getHead(){
        return armor[HEAD];
    }

    public Armor getTorso(){
        return armor[TORSO];
    }

    public Armor getLegs(){
        return armor[LEGS];
    }

    public void unequipHead() {
        inventory.add(armor[HEAD]);
        armor[HEAD] = null;
        updateAttributes();
    }

    public void unequipTorso() {
        inventory.add(armor[TORSO]);
        armor[TORSO] = null;
        updateAttributes();
    }

    public void unequipLegs() {
        inventory.add(armor[LEGS]);
        armor[LEGS] = null;
        updateAttributes();
    }

    public void pickupItem(Item newItem) {
        int temp = indexInInventory(newItem);
        if (temp == -1) {
            inventory.add(newItem);
        } else {
            inventory.get(temp).addQuantity(newItem.getQuantity());
        }
    }

    public void dropItem(Item itemToDrop) {
        int temp = indexInInventory(itemToDrop);
        if (temp != -1) {
            if (inventory.get(temp).getQuantity() > itemToDrop.getQuantity()) {
                inventory.get(temp).addQuantity(itemToDrop.getQuantity() * -1);
            } else if (inventory.get(temp).getQuantity() == itemToDrop.getQuantity()) {
                inventory.remove(temp);
            } else {
                // Temp exception
                System.out.println("notInInventory Error");
            }
        } else {
            // Temp exception
            System.out.println("notInInventory Error");
        }
    }

    public void buyItem(Item itemToBuy, int cost) {
        if (wallet >= cost) {
            pickupItem(itemToBuy);
        }
    }

    public void sellItem(Item itemToSell, int price) {
        if (isInInventory(itemToSell)) {
            dropItem(itemToSell);
            wallet += price;
        }
    }

    private int indexInInventory(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getName().equals(item.getName())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isInInventory(Item item) {
        if (indexInInventory(item) == -1) {
            return false;
        }
        return true;
    }

    private int indexInStatus(Status stat) {
        Status[] arr = (Status[]) status.toArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getName().equals(stat.getName())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isInStatus(Status stat) {
        if (indexInStatus(stat) == -1) {
            return false;
        }
        return true;
    }
}
