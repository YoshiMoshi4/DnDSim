import java.util.ArrayList;

public class CharSheet
{
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

    // Attributes
    private int[] baseAttributes;
    private int[] tempAttributes;
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
    public CharSheet(String name, boolean isParty, int totalHP, int[] baseAttributes)
    {
        this.name = name;
        cellColor = 0;
        this.isParty = isParty;

        this.totalHP = totalHP;
        currentHP = totalHP;
        status = new ArrayList<Status>();

        if (baseAttributes.length == this.baseAttributes.length)
        {
            for (int i = 0; i < baseAttributes.length; i++)
            {
                this.baseAttributes[i] = baseAttributes[i];
                this.tempAttributes[i] = 0;
            }
        }
        else
        {
            // Temp exception
            System.out.println("attributesLength Error");
        }

        weapons = new Weapon[2];
        armor = new Armor[3];
        inventory = new ArrayList<Item>();
    }

    // Methods
    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setParty(boolean isParty)
    {
        this.isParty = isParty;
    } 

    public boolean getParty()
    {
        return isParty;
    }

    public void setCellColor(int cellColor)
    {

        this.cellColor = cellColor % 16;
    }

    public int getCellColor()
    {
        return cellColor;
    }

    public void setTotalHP(int newHP)
    {
        totalHP = newHP;
    }

    public int getTotalHP()
    {
        return totalHP;
    }

    public void setCurrentHP(int newHP)
    {
        currentHP = newHP;
    }

    public void addCurrentHP(int HPtoAdd)
    {
        currentHP += HPtoAdd;
    }

    public void mulCurrentHP(double factorToMul)
    {
        int temp = (int) (Math.ceil(currentHP * factorToMul));
        if (temp < 1)
            currentHP = 1;
        else
            currentHP = temp;
    }

    public void addStatus(Status newStatus)
    {
        if (!isInStatus(newStatus))
            status.add(newStatus);
    }

    public Status removeStatus(Status statusToRemove)
    {
        Status ans = new Status("temp");
        if (isInStatus(statusToRemove))
        {
            ans = statusToRemove;
            status.remove(statusToRemove);
        }
        else
        {
             // Temp exception
             System.out.println("statusRemoval Error");
        }
        return ans;
    }

    public void clearStatus()
    {
        status.clear();
    }

    public Status[] getStatus()
    {
        Status[] ans = new Status[status.size()];
        for (int i = 0; i < ans.length; i++)
        {
            ans[i] = status.get(i);
        }
        return ans;
    }

    public void procStatus() // TBI
    {

    }

    public void setAttributes(int[] values, boolean isBase)
    {
        if (values.length != baseAttributes.length)
        {
            // Temp exception
            System.out.println("attributeLength Error");
        }
        else if (isBase)
        {
            for (int i = 0; i < baseAttributes.length; i++)
            {
                baseAttributes[i] = values[i];
            }
        }
        else
        {
            for (int i = 0; i < tempAttributes.length; i++)
                {
                    tempAttributes[i] = values[i];
                }
        }
    }

    public void setAttribute(int attribute, int value, boolean isBase)
    {
        if (isBase)
        {
            baseAttributes[attribute] = value;
        }
        else
        {
            tempAttributes[attribute] = value;
        }
        
    }

    public void incAttribute(int attribute, boolean isBase)
    {
        if (isBase)
        {
            baseAttributes[attribute] += 1;
        }
        else
        {
            tempAttributes[attribute] += 1;
        }
        
    }

    public int[] getAttributes(boolean isBase)
    {
        int[] ans = new int[baseAttributes.length];
        if (isBase)
        {
            for (int i = 0; i < ans.length; i++)
            {
                ans[i] = baseAttributes[i];
            }
        }
        else
        {
            for (int i = 0; i < ans.length; i++)
                {
                    ans[i] = tempAttributes[i];
                }
        }
        return ans;
    }

    public int getAttribute(int attribute)
    {
        return (baseAttributes[attribute] + tempAttributes[attribute]);
    }

    public void updateTempAttributes()
    {
        int[] headAttr = armor[0].getModifiedAttributes();
        int[] torsoAttr = armor[1].getModifiedAttributes();
        int[] legsAttr = armor[2].getModifiedAttributes();
        int[] weapAttr = weapons[0].getModifiedAttributes();
        for (int i = 0; i < headAttr.length; i++)
        {
            tempAttributes[i] = (headAttr[i] + torsoAttr[i] + legsAttr[i] + weapAttr[i]);
        }
    }

    public void equipPrimaryWeapon(Weapon newWeapon)
    {
        int temp = indexInInventory(newWeapon);
        if (temp == -1)
        {
            // Temp exception
            System.out.println("notInInventory Error");
        }
        else if (weapons[PRIMARY] == null)
        {
            weapons[PRIMARY] = (Weapon) inventory.get(temp);
            inventory.remove(temp);
        }
        else
        {
            inventory.add(weapons[PRIMARY]);
            weapons[PRIMARY] = (Weapon) inventory.get(temp);
            inventory.remove(temp);
        }
        updateTempAttributes();
    }

    public void equipSecondaryWeapon(Weapon newWeapon)
    {
        int temp = indexInInventory(newWeapon);
        if (temp == -1)
        {
            // Temp exception
            System.out.println("notInInventory Error");
        }
        else if (weapons[SECONDARY] == null)
        {
            weapons[SECONDARY] = (Weapon) inventory.get(temp);
            inventory.remove(temp);
        }
        else
        {
            inventory.add(weapons[SECONDARY]);
            weapons[SECONDARY] = (Weapon) inventory.get(temp);
            inventory.remove(temp);
        }
    }

    public void unequipPrimaryWeapon()
    {
        inventory.add(weapons[PRIMARY]);
        weapons[PRIMARY] = null;
        updateTempAttributes();
    }
    
    public void unequipSecondaryWeapon()
    {
        inventory.add(weapons[SECONDARY]);
        weapons[SECONDARY] = null;
        updateTempAttributes();
    }

    public void swapWeapons()
    {
        Weapon temp = weapons[PRIMARY];
        weapons[PRIMARY] = weapons[SECONDARY];
        weapons[SECONDARY] = temp;
        updateTempAttributes();
    }

    public void equipHead(Armor newArmor)
    {
        int temp = indexInInventory(newArmor);
        if (temp == -1)
        {
            // Temp exception
            System.out.println("notInInventory Error");
        }
        if (newArmor.getArmorType() == HEAD)
        {
            if (armor[HEAD] == null)
            {
                armor[HEAD] = (Armor) inventory.get(temp);
                inventory.remove(temp);
            }
            else
            {
                inventory.add(armor[HEAD]);
                armor[HEAD] = (Armor) inventory.get(temp);
                inventory.remove(temp);
            }
            updateTempAttributes();
        }
    }

    public void equipTorso(Armor newArmor)
    {
        int temp = indexInInventory(newArmor);
        if (temp == -1)
        {
            // Temp exception
            System.out.println("notInInventory Error");
        }
        if (newArmor.getArmorType() == TORSO)
        {
            if (armor[TORSO] == null)
            {
                armor[TORSO] = (Armor) inventory.get(temp);
                inventory.remove(temp);
            }
            else
            {
                inventory.add(armor[TORSO]);
                armor[TORSO] = (Armor) inventory.get(temp);
                inventory.remove(temp);
            }
            updateTempAttributes();
        }
    }

    public void equipLegs(Armor newArmor)
    {
        int temp = indexInInventory(newArmor);
        if (temp == -1)
        {
            // Temp exception
            System.out.println("notInInventory Error");
        }
        if (newArmor.getArmorType() == LEGS)
        {
            if (armor[LEGS] == null)
            {
                armor[LEGS] = (Armor) inventory.get(temp);
                inventory.remove(temp);
            }
            else
            {
                inventory.add(armor[LEGS]);
                armor[LEGS] = (Armor) inventory.get(temp);
                inventory.remove(temp);
            }
            updateTempAttributes();
        }
    }

    public void unequipHead()
    {
        inventory.add(armor[HEAD]);
        armor[HEAD] = null;
        updateTempAttributes();
    }

    public void unequipTorso()
    {
        inventory.add(armor[TORSO]);
        armor[TORSO] = null;
        updateTempAttributes();
    }

    public void unequipLegs()
    {
        inventory.add(armor[LEGS]);
        armor[LEGS] = null;
        updateTempAttributes();
    }

    public void pickupItem(Item newItem)
    {
        int temp = indexInInventory(newItem);
        if (temp == -1)
        {
            inventory.add(newItem);
        }
        else
        {
            inventory.get(temp).addQuantity(newItem.getQuantity());
        }
    }

    public void dropItem(Item itemToDrop)
    {
        int temp = indexInInventory(itemToDrop);
        if (temp != -1)
        {
            if (inventory.get(temp).getQuantity() > itemToDrop.getQuantity())
            {
                inventory.get(temp).addQuantity(itemToDrop.getQuantity() * -1);
            }
            else if (inventory.get(temp).getQuantity() == itemToDrop.getQuantity())
            {
                inventory.remove(temp);
            }
            else
            {
                // Temp exception
                System.out.println("notInInventory Error");
            }
        }
        else
        {
            // Temp exception
            System.out.println("notInInventory Error");
        }
    }

    public void buyItem(Item itemToBuy, int cost)
    {
        if (wallet >= cost)
            pickupItem(itemToBuy);
    }

    public void sellItem(Item itemToSell, int price)
    {
        if (isInInventory(itemToSell))
        {
            dropItem(itemToSell);
            wallet += price;
        }
    }

    private int indexInInventory(Item item)
    {
        Item[] arr = (Item[]) inventory.toArray();
        for (int i = 0; i < arr.length; i++)
        {
            if (arr[i].getName().equals(item.getName()))
            {
                return i;
            }
        }
        return -1;
    }

    private boolean isInInventory(Item item)
    {
        if (indexInInventory(item) == -1)
            return false;
        return true;
    }

    private int indexInStatus(Status stat)
    {
        Status[] arr = (Status[]) status.toArray();
        for (int i = 0; i < arr.length; i++)
        {
            if (arr[i].getName().equals(stat.getName()))
            {
                return i;
            }
        }
        return -1;
    }

    private boolean isInStatus(Status stat)
    {
        if (indexInStatus(stat) == -1)
            return false;
        return true;
    }
}