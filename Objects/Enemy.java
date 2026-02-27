package Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class Enemy extends GridObject {

    private String name;
    private int health;
    private int maxHealth;
    private int mobility;
    private int attackDamage;
    private int initiative;
    private int color;
    private int instanceNumber = 0;
    private String baseName;

    public Enemy(int row, int col, String name, int maxHealth, int mobility, int attackDamage, int initiative, int color) {
        super(row, col);
        this.name = name;
        this.baseName = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.mobility = mobility;
        this.attackDamage = attackDamage;
        this.initiative = initiative;
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
        this.attackDamage = template.attackDamage;
        this.initiative = template.initiative;
        this.color = template.color;
    }

    public void attack(Entity target) {
        int damage = attackDamage - target.getDefense();
        target.takeDamage(Math.max(0, damage));
    }

    public void attack(Enemy target) {
        int damage = attackDamage;  // Enemies have no defense
        target.takeDamage(Math.max(0, damage));
    }

    public int getAttackPower() {
        return attackDamage;
    }

    public int getDefense() {
        return 0;  // Enemies have no defense
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

    public int getColor() {
        return color;
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
