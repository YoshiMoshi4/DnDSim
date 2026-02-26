package EntityRes;

import java.util.ArrayList;
import com.google.gson.Gson;
import java.io.*;

public class CharSheet {

    // Meta
    private String name;
    private String characterClass;
    private int color; // 0-15 representing different colors
    private boolean isParty;

    // HP and Statuses
    private int totalHP;
    private int currentHP;
    private ArrayList<Status> status;

    // Attributes
    private int[] baseAttributes;
    private int[] tempAttributes;    // Equipment modifications
    private int[] totalAttributes;   // baseAttributes + tempAttributes
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
    public CharSheet(String name, boolean isParty, int totalHP, int[] baseAttributes, int color) {
        this.name = name;
        this.characterClass = "None";
        this.color = color;
        this.isParty = isParty;

        this.totalHP = totalHP;
        currentHP = totalHP;
        status = new ArrayList<Status>();

        this.baseAttributes = new int[baseAttributes.length];
        this.tempAttributes = new int[baseAttributes.length];
        this.totalAttributes = new int[baseAttributes.length];
        if (baseAttributes.length == this.baseAttributes.length) {
            for (int i = 0; i < baseAttributes.length; i++) {
                this.baseAttributes[i] = baseAttributes[i];
                this.tempAttributes[i] = 0;  // Start with no equipment bonuses
                this.totalAttributes[i] = baseAttributes[i];  // Initially equal to base
            }
        } else {
            // Temp exception
            System.out.println("attributesLength Error");
        }

        // Get default items from ItemDatabase
        ItemDatabase db = ItemDatabase.getInstance();
        Weapon fist = db.getWeapon("Fist");
        Armor bald = db.getArmor("Bald");
        Armor bareChest = db.getArmor("Bare Chest");
        Armor noPants = db.getArmor("No Pants");
        
        // Fallback to creating defaults if not in database
        if (fist == null) {
            fist = new Weapon("Fist", "Unarmed", 1, new int[]{0, 0, 0, 0});
        }
        if (bald == null) {
            bald = new Armor("Bald", "Armor", 0, 0, new int[]{0, 0, 0, 0});
        }
        if (bareChest == null) {
            bareChest = new Armor("Bare Chest", "Armor", 1, 0, new int[]{0, 0, 0, 0});
        }
        if (noPants == null) {
            noPants = new Armor("No Pants", "Armor", 2, 0, new int[]{0, 0, 0, 0});
        }

        weapons = new Weapon[2];
        weapons[0] = fist;
        weapons[1] = fist;

        armor = new Armor[3];
        armor[0] = bald;
        armor[1] = bareChest;
        armor[2] = noPants;
        
        // Initialize totalAttributes after equipment is equipped
        updateAttributes();
        inventory = new ArrayList<Item>();
    }

    // Methods
    public void setName(String name) {
        this.name = name;
        this.save();
    }

    public String getName() {
        return name;
    }

    public void setCharacterClass(String characterClass) {
        this.characterClass = characterClass;
        this.save();
    }

    public String getCharacterClass() {
        return characterClass != null ? characterClass : "None";
    }

    public void setParty(boolean isParty) {
        this.isParty = isParty;
        this.save();
    }

    public boolean getParty() {
        return isParty;
    }

    public void setColor(int color) {
        this.color = Math.max(0, Math.min(15, color)); // Ensure color is between 0-15
        this.save();
    }

    public int getColor() {
        return color;
    }

    public java.awt.Color getDisplayColor() {
        switch (color) {
            case 0:
                return java.awt.Color.BLACK;
            case 1:
                return java.awt.Color.GRAY;
            case 2:
                return java.awt.Color.WHITE;
            case 3:
                return java.awt.Color.RED.darker();
            case 4:
                return java.awt.Color.RED;
            case 5:
                return java.awt.Color.ORANGE;
            case 6:
                return java.awt.Color.YELLOW;
            case 7:
                return java.awt.Color.GREEN.brighter();
            case 8:
                return java.awt.Color.GREEN;
            case 9:
                return java.awt.Color.BLUE;
            case 10:
                return java.awt.Color.BLUE.darker();
            case 11:
                return new java.awt.Color(200, 162, 200); // Lilac
            case 12:
                return new java.awt.Color(128, 0, 128); // Purple
            case 13:
                return java.awt.Color.PINK;
            case 14:
                return new java.awt.Color(245, 245, 220); // Beige
            case 15:
                return new java.awt.Color(139, 69, 19); // Brown
            default:
                return java.awt.Color.GRAY;
        }
    }

    public static String[] getColorNames() {
        return new String[] {
            "Black", "Gray", "White", "Maroon", "Red", "Orange", "Yellow", "Lime",
            "Green", "Blue", "Indigo", "Lilac", "Purple", "Pink", "Beige", "Brown"
        };
    }

    public String getColorName() {
        String[] names = getColorNames();
        if (color >= 0 && color < names.length) {
            return names[color];
        }
        return "Gray";
    }

    public void setTotalHP(int newHP) {
        totalHP = newHP;
        this.save();
    }

    public int getTotalHP() {
        return totalHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int newHP) {
        currentHP = newHP;
        this.save();
    }

    public void addCurrentHP(int HPtoAdd) {
        currentHP = Math.min(currentHP + HPtoAdd, totalHP); // Don't exceed max HP
        this.save();
    }

    public void mulCurrentHP(double factorToMul) {
        int temp = (int) (Math.ceil(currentHP * factorToMul));
        if (temp < 1) {
            currentHP = 1;
        } else {
            currentHP = temp;
        }
        this.save();
    }

    public void addStatus(Status newStatus) {
        if (!isInStatus(newStatus)) {
            status.add(newStatus);
            this.save();
        }
    }

    public Status removeStatus(Status statusToRemove) {
        Status ans = new Status("temp");
        if (isInStatus(statusToRemove)) {
            ans = statusToRemove;
            status.remove(statusToRemove);
            this.save();
        } else {
            // Temp exception
            System.out.println("statusRemoval Error");
        }
        return ans;
    }

    public void clearStatus() {
        status.clear();
        this.save();
    }

    public Status[] getStatus() {
        Status[] ans = new Status[status.size()];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = status.get(i);
        }
        return ans;
    }

    /**
     * Process all status effects at the start of a turn.
     * Applies DOT/HOT, ticks down durations, and removes expired effects.
     */
    public void procStatus() {
        if (status.isEmpty()) {
            return;
        }

        java.util.Iterator<Status> iterator = status.iterator();
        while (iterator.hasNext()) {
            Status s = iterator.next();
            
            // Apply effect based on type
            switch (s.getEffectType()) {
                case Status.DAMAGE_OVER_TIME:
                    // Apply damage (minimum HP is 1 to avoid instant death from DOT)
                    int newHP = Math.max(1, currentHP - s.getMagnitude());
                    currentHP = newHP;
                    break;
                    
                case Status.HEAL_OVER_TIME:
                    // Apply healing (capped at totalHP)
                    currentHP = Math.min(totalHP, currentHP + s.getMagnitude());
                    break;
                    
                case Status.STAT_MODIFIER:
                case Status.MOVEMENT_MODIFIER:
                    // Stat modifiers are applied during updateAttributes()
                    // They affect the temp attributes while the status is active
                    break;
            }
            
            // Tick duration and remove if expired
            if (s.tick()) {
                iterator.remove();
            }
        }
        
        // Recalculate attributes (in case stat modifiers were removed)
        updateAttributes();
        this.save();
    }

    /**
     * Get total stat modifiers from active status effects
     */
    public int getStatusAttributeModifier(int attribute) {
        int modifier = 0;
        for (Status s : status) {
            if ((s.getEffectType() == Status.STAT_MODIFIER || s.getEffectType() == Status.MOVEMENT_MODIFIER)
                    && s.getTargetAttribute() == attribute) {
                modifier += s.getMagnitude();
            }
        }
        return modifier;
    }

    public void updateAttributes() {
        // Calculate tempAttributes from equipped items (base + equipment modifications)
        int[] headAttr = armor[0].getModifiedAttributes();
        int[] torsoAttr = armor[1].getModifiedAttributes();
        int[] legsAttr = armor[2].getModifiedAttributes();
        int[] weapAttr = weapons[0].getModifiedAttributes();
        
        // Reset temp attributes to zero
        for (int i = 0; i < tempAttributes.length; i++) {
            tempAttributes[i] = 0;
        }
        
        // Sum all equipment modifications
        for (int i = 0; i < tempAttributes.length; i++) {
            tempAttributes[i] = headAttr[i] + torsoAttr[i] + legsAttr[i] + weapAttr[i];
        }
        
        // Calculate total attributes = base + equipment modifications + status modifiers
        for (int i = 0; i < totalAttributes.length; i++) {
            totalAttributes[i] = baseAttributes[i] + tempAttributes[i] + getStatusAttributeModifier(i);
        }
    }

    public void setAttributes(int[] values) {
        if (values.length != baseAttributes.length) {
            // Temp exception
            System.out.println("attributeLength Error");
        } else {
            for (int i = 0; i < baseAttributes.length; i++) {
                baseAttributes[i] = values[i];
            }
            this.save();
        }
    }

    public void setAttribute(int attribute, int value) {
        baseAttributes[attribute] = value;
        this.save();
    }

    public void incAttribute(int attribute) {
        baseAttributes[attribute] += 1;
        this.save();
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

    public int[] getTempAttributes() {
        int[] ans = new int[tempAttributes.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = tempAttributes[i];
        }
        return ans;
    }

    public int getTempAttribute(int attribute) {
        return tempAttributes[attribute];
    }

    public int[] getTotalAttributes() {
        int[] ans = new int[totalAttributes.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = totalAttributes[i];
        }
        return ans;
    }

    public int getTotalAttribute(int attribute) {
        return totalAttributes[attribute];
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
        this.save();

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
        // Secondary weapon doesn't affect primary stats, but keep for consistency
        this.save();
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
        this.save();
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
        this.save();
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
        this.save();
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
        this.save();
    }

    public Armor getHead() {
        return armor[HEAD];
    }

    public Armor getTorso() {
        return armor[TORSO];
    }

    public Armor getLegs() {
        return armor[LEGS];
    }

    public int getTotalDefense() {
        int totalDefense = 0;
        if (armor[HEAD] != null) totalDefense += armor[HEAD].getDefense();
        if (armor[TORSO] != null) totalDefense += armor[TORSO].getDefense();
        if (armor[LEGS] != null) totalDefense += armor[LEGS].getDefense();
        return totalDefense;
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
        this.save();
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
        this.save();
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
            this.save();
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

    public ArrayList<Item> getInventory() {
        return inventory;
    }

    public int getWallet() {
        return wallet;
    }

    public void save() {
        // Only save party entities
        if (!isParty) {
            return;
        }
        
        String dir = "saves/entities/party";
        new File(dir).mkdirs();
        String filePath = dir + "/" + name.replaceAll("[^a-zA-Z0-9]", "_") + ".json";
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CharSheet load(String name, boolean isParty) {
        String folder = isParty ? "party" : "nonparty";
        String dir = "saves/entities/" + folder;
        String filePath = dir + "/" + name.replaceAll("[^a-zA-Z0-9]", "_") + ".json";
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            CharSheet sheet = gson.fromJson(reader, CharSheet.class);
            // Initialize missing arrays for older save files
            if (sheet.tempAttributes == null) {
                sheet.tempAttributes = new int[sheet.baseAttributes.length];
            }
            if (sheet.totalAttributes == null) {
                sheet.totalAttributes = new int[sheet.baseAttributes.length];
            }
            // Recalculate attributes in case they were corrupted
            sheet.updateAttributes();
            return sheet;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Deep copy the CharSheet to create an independent instance for non-party entities
     */
    public CharSheet deepCopy() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        CharSheet copy = gson.fromJson(json, CharSheet.class);
        return copy;
    }
}
