package Objects;

import EntityRes.ColorUtils;
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
    private String color;
    private int instanceNumber = 0;
    private String baseName;
    private String spritePath; // Path to enemy sprite image (e.g., "sprites/enemies/spider.png")
    private transient int acAdjustment = 0;  // Temporary in-battle AC tweak; not persisted, resets each battle
    private transient int dexAdjustment = 0;  // Temporary in-battle Dexterity tweak; not persisted, resets each battle
    private transient String[] diceOverride = new String[3];  // Temporary in-battle damage dice tweaks per tier; null = use real die

    // Legacy field for backward compatibility
    private int attackDamage;

    /**
     * No-arg constructor so Gson deserialization (used by Enemy.load()) goes through normal
     * reflective construction instead of falling back to UnsafeAllocator, which skips field
     * initializers entirely - that was leaving diceOverride null instead of new String[3] for
     * every enemy loaded from disk, causing an NPE the moment a loaded enemy's damage dice
     * were read (e.g. attacking, being attacked, or hovering it during battle).
     */
    public Enemy() {
        super(0, 0);
    }

    public Enemy(int row, int col, String name, int maxHealth, int mobility, int armorClass,
                 String[] damageDice, int initiative, int dexterity, String color) {
        super(row, col);
        this.name = name;
        this.baseName = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.mobility = mobility;
        this.armorClass = armorClass;
        this.damageDice = damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
        this.initiative = initiative;
        this.dexterity = dexterity;
        this.color = ColorUtils.normalizeHex(color, ColorUtils.DEFAULT_COLOR);
    }

    public Enemy(int row, int col, String name, int maxHealth, int mobility, int armorClass,
                 String[] damageDice, int initiative, int dexterity, int color) {
        this(row, col, name, maxHealth, mobility, armorClass, damageDice, initiative, dexterity,
                ColorUtils.fromLegacyIndex(color));
    }
    
    // Legacy constructor for backward compatibility
    public Enemy(int row, int col, String name, int maxHealth, int mobility, int attackDamage, int initiative,
                 int dexterity, String color) {
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
        this.color = ColorUtils.normalizeHex(color, ColorUtils.DEFAULT_COLOR);
    }

    public Enemy(int row, int col, String name, int maxHealth, int mobility, int attackDamage, int initiative,
                 int dexterity, int color) {
        this(row, col, name, maxHealth, mobility, attackDamage, initiative, dexterity,
                ColorUtils.fromLegacyIndex(color));
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
        int damage = getAttackDamage() - target.getDefense();
        target.takeDamage(Math.max(0, damage));
    }

    @Deprecated
    public void attack(Enemy target) {
        int damage = getAttackDamage();  // Enemies have no defense
        target.takeDamage(Math.max(0, damage));
    }

    /**
     * Get the direct damage value used by legacy terrain damage interactions.
     */
    public int getAttackDamage() {
        // Legacy: return flat attack damage for backward compatibility
        if (attackDamage > 0) return attackDamage;
        return 3 + attackModifier;  // Derive from modifier
    }

    @Deprecated
    public int getAttackPower() {
        return getAttackDamage();
    }
    
    /**
     * Get Armor Class for attack roll calculations
     */
    public int getAC() {
        // Handle legacy enemies without explicit AC
        int baseAC = armorClass <= 0 ? 10 : armorClass;
        return baseAC + acAdjustment;
    }

    public void setAC(int ac) {
        this.armorClass = ac;
    }

    public int getAcAdjustment() {
        return acAdjustment;
    }

    /**
     * Temporarily adjust this enemy's AC for the current battle only.
     * Not persisted (transient), so it resets whenever a fresh Enemy is loaded.
     */
    public void adjustAC(int delta) {
        acAdjustment += delta;
    }

    public int getDexAdjustment() {
        return dexAdjustment;
    }

    /**
     * Temporarily adjust this enemy's Dexterity for the current battle only.
     * Not persisted (transient), so it resets whenever a fresh Enemy is loaded.
     * Does not affect movement (flat `mobility` stat) or already-rolled initiative.
     */
    public void adjustDexterity(int delta) {
        dexAdjustment += delta;
    }

    public int getAdjustedDexterity() {
        return dexterity + dexAdjustment;
    }

    /**
     * Get modifier added to attack rolls.
     * Current enemy design uses dexterity for both attack rolls and initiative.
     */
    public int getAttackModifier() {
        return getAdjustedDexterity();
    }
    
    public void setAttackModifier(int modifier) {
        this.attackModifier = modifier;
    }
    
    /**
     * Get damage dice per tier [tier1, tier2, tier3], with any temporary battle adjustment applied.
     */
    public String[] getDamageDice() {
        ensureDiceOverride();
        String[] base = getBaseDamageDice();
        String[] result = new String[3];
        for (int i = 0; i < 3; i++) {
            result[i] = diceOverride[i] != null ? diceOverride[i] : base[i];
        }
        return result;
    }

    /**
     * Gson's deserializer (Enemy.load()) constructs instances via reflection without running
     * field initializers, so a freshly-loaded Enemy could have a null diceOverride array even
     * with the no-arg constructor in place. Guard against that defensively here too.
     */
    private void ensureDiceOverride() {
        if (diceOverride == null) {
            diceOverride = new String[3];
        }
    }

    /**
     * Get this enemy's real damage dice, ignoring any temporary battle override.
     */
    public String[] getBaseDamageDice() {
        // Handle legacy enemies
        if (damageDice == null && attackDamage > 0) {
            damageDice = convertLegacyDamage(attackDamage);
        }
        String[] source = damageDice != null ? damageDice : new String[]{"d4", "d4", "d6"};
        String[] base = new String[3];
        for (int i = 0; i < 3; i++) {
            base[i] = i < source.length ? source[i] : null;
        }
        return base;
    }

    public void setDamageDice(String[] dice) {
        this.damageDice = dice;
    }

    public String getDiceOverride(int tier) {
        ensureDiceOverride();
        return diceOverride[tier];
    }

    /**
     * Temporarily override one damage-dice tier for the current battle only.
     * Not persisted (transient), so it resets whenever a fresh Enemy is loaded.
     */
    public void setDiceOverride(int tier, String die) {
        ensureDiceOverride();
        diceOverride[tier] = die;
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
        // Initiative currently shares the same dexterity value as attack rolls.
        return dexterity;
    }

    public int getDexterity() {
        return dexterity;
    }

    public String getColor() {
        color = ColorUtils.normalizeHex(color, ColorUtils.DEFAULT_COLOR);
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

    public void setHealth(int hp) {
        this.health = Math.max(0, Math.min(hp, maxHealth));
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

    /**
     * Strip characters that are illegal in a Windows filename (or unmappable by the JVM's
     * native filename encoding, which Windows silently turns into '?' - itself illegal) so
     * the display name can contain any characters without breaking the save path.
     */
    private static String sanitizeFileName(String name) {
        String sanitized = name.replaceAll("[<>:\"/\\\\|?*\\x00-\\x1F]", "_")
                .replaceAll("[.\\s]+$", "")
                .trim();
        return sanitized.isEmpty() ? "Unnamed_Enemy" : sanitized;
    }

    // Save enemy to JSON file. Returns true on success; false if the write failed (e.g. an
    // invalid filename), leaving the caller's in-memory object as the only copy.
    public boolean save() {
        try {
            File dir = new File("saves/entities/enemies");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter("saves/entities/enemies/" + sanitizeFileName(baseName) + ".json");
            gson.toJson(this, writer);
            writer.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error saving enemy: " + e.getMessage());
            return false;
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
