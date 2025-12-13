public class Armor extends Item
{
    // Fields
    private int armorType;
        private final int HEAD = 0;
        private final int TORSO = 1;
        private final int LEGS = 2;
    private int defense;
    private int[] modifiedAttributes; 
    // Constructor
    public Armor(String name, String type, int armorType, int defense, int[] modifiedAttributes)
    {
        super(name, "Armor");
        this.armorType = armorType;
        this.defense = defense;
        this.modifiedAttributes = modifiedAttributes;
    }

    // Methods
    public void setArmorType(int armorType)
    {
        this.armorType = armorType;
    }

    public int getArmorType()
    {
        return armorType;
    }

    public void setDefense(int defense)
    {
        this.defense = defense;
    }

    public int getDefense()
    {
        return defense;
    }

    public void setModifiedAttributes(int [] modifiedAttributes)
    {
        if (modifiedAttributes.length == this.modifiedAttributes.length)
        {
            for (int i = 0; i < modifiedAttributes.length; i++)
            {
                this.modifiedAttributes[i] = modifiedAttributes[i];
            }
        }
        else
        {
            // Temp exception
            System.out.println("attributesLength Error");
        }
    }

    public int[] getModifiedAttributes()
    {
        int[] ans = new int[modifiedAttributes.length];
        for (int i = 0; i < modifiedAttributes.length; i++)
        {
            ans[i] = modifiedAttributes[i];
        }
        return ans;
    }
}
