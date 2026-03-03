package Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class Enemy extends GridObject {

    private String name;
    private int health;
    private int maxHealth;
    private int mobility;
    private int armorClass;           // AC for attack rolls
    private int attackModifier;       // Modifier added to attack rolls
    private String[] damageDice;      // Damage dice per tier [tier1, tier2, tier3]
    private int initiative;
    private int dexterity;
    private int color;
    private int instanceNumber = 0;
    private String baseName;
    private String spritePath; // Path to enemy sprite image (e.g., "sprites/enemies/spider.png")
    
    // Legacy field for backward compatibility
    private int attackDamage;

    public Enemy(int row, int col, String name, int maxHealth, int mobility, int armorClass, 
                 int attackModifier, String[] damageDice, int initiative, int dexterity, int color) {
        super(row, col);
        this.name = name;
        this.baseName = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.mobility = mobility;
        this.armorClass = armorClass;
        this.attackModifier = attackModifier;
        this.damageDice = damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
        this.initiative = initiative;
        this.dexterity = dexterity;
        this.color = color;
    }
    
    // Legacy constructor for backward compatibility
    public Enemy(int row, int col, String name, int maxHealth, int mobility, int attackDamage, int initiative, int dexterity, int color) {
        super(row, col);
        this.name = name;
        this.baseName = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.mobility = mobility;
        this.attackDamage = attackDamage;
        this.armorClass = 10;  // Default AC
        this.attackModifier = attackDamage / 3;  // Derive modifier from legacy damage
        this.damageDice = convertLegacyDamage(attackDamage);
        this.initiative = initiative;
        this.dexterity = dexterity;
        this.color = color;
    }

    // Copy constructor for creating instances
    public Enemy(Enemy template, int row, int col) {
        super(row, col);
        this.name = template.name;
        this.baseName = template.baseName;
        this.maxHealth = template.maxHealth;
        this.health = template.maxHealth;
        this.mobility = template.mobility;
        this.armorClass = template.armorClass;
        this.attackModifier = template.attackModifier;
        this.damageDice = template.damageDice;
        this.attackDamage = template.attackDamage;
        this.initiative = template.initiative;
        this.dexterity = template.dexterity;
        this.color = template.color;
        this.spritePath = template.spritePath;
    }
    
    /**
     * Convert legacy flat damage to dice tiers (for migration)
     */
    private String[] convertLegacyDamage(int damage) {
        if (damage <= 2) return new String[]{"d4", "d4", "d4"};
        if (damage <= 4) return new String[]{"d4", "d4", "d6"};
        if (damage <= 6) return new String[]{"d6", "d6", "d8"};
        return new String[]{"d6", "d8", "d10"};
    }

    @Deprecated
    public void attack(Entity target) {
        int damage = getAttackPower() - target.getDefense();
        target.takeDamage(Math.max(0, damage));
    }

    @Deprecated
    public void attack(Enemy target) {
        int damage = getAttackPower();  // Enemies have no defense
        target.takeDamage(Math.max(0, damage));
    }

    @Deprecated
    public int getAttackPower() {
        // Legacy: return flat attack damage for backward compatibility
        if (attackDamage > 0) return attackDamage;
        return 3 + attackModifier;  // Derive from modifier
    }
    
    /**
     * Get Armor Class for attack roll calculations
     */
    public int getAC() {
        // Handle legacy enemies without explicit AC
        if (armorClass <= 0) {
            return 10;  // Default AC
        }
        return armorClass;
    }
    
    public void setAC(int ac) {
        this.armorClass = ac;
    }
    
    /**
     * Get modifier added to attack rolls
     */
    public int getAttackModifier() {
        // Handle legacy enemies
        if (attackModifier <= 0 && attackDamage > 0) {
            return attackDamage / 3;
        }
        return attackModifier;
    }
    
    public void setAttackModifier(int modifier) {
        this.attackModifier = modifier;
    }
    
    /**
     * Get damage dice per tier [tier1, tier2, tier3]
     */
    public String[] getDamageDice() {
        // Handle legacy enemies
        if (damageDice == null && attackDamage > 0) {
            damageDice = convertLegacyDamage(attackDamage);
        }
        return damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
    }
    
    public void setDamageDice(String[] dice) {
        this.damageDice = dice;
    }

    @Deprecated
    public int getDefense() {
        return 0;  // Legacy method - enemies now use AC
    }

    public int getMovement() {
        return mobility;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public String getName() {
        if (instanceNumber > 0) {
            return baseName + " #" + instanceNumber;
        }
        return name;
    }

    public String getBaseName() {
        return baseName;
    }

    public int getInitiative() {
        return initiative;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getColor() {
        return color;
    }

    public void setSpritePath(String spritePath) {
        this.spritePath = spritePath;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public void setInstanceNumber(int number) {
        this.instanceNumber = number;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void moveTo(int r, int c) {
        row = r;
        col = c;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
        if (health < 0) health = 0;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean isParty() {
        return false;
    }

    // Load enemy from JSON file
    public static Enemy load(String name) {
        try {
            File file = new File("saves/entities/enemies/" + name + ".json");
            if (!file.exists()) {
                return null;
            }
            Gson gson = new Gson();
            FileReader reader = new FileReader(file);
            Enemy enemy = gson.fromJson(reader, Enemy.class);
            reader.close();
            enemy.baseName = enemy.name;
            return enemy;
        } catch (Exception e) {
            System.out.println("Error loading enemy: " + e.getMessage());
            return null;
        }
    }

    // Save enemy to JSON file
    public void save() {
        try {
            File dir = new File("saves/entities/enemies");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter("saves/entities/enemies/" + baseName + ".json");
            gson.toJson(this, writer);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error saving enemy: " + e.getMessage());
        }
    }

    // List all saved enemies
    public static String[] listSavedEnemies() {
        File dir = new File("saves/entities/enemies");
        if (!dir.exists()) {
            return new String[0];
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) {
            return new String[0];
        }
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName().replace(".json", "");
        }
        return names;
    }
}
