package EntityRes;

public class Ammunition extends Item {

    private int damageBonus;
    private String compatibleWeaponType;  // e.g., "Bow", "Crossbow", "Any"

    public Ammunition(String name, String type, int color, int damageBonus, String compatibleWeaponType) {
        super(name, type, color);
        this.damageBonus = damageBonus;
        this.compatibleWeaponType = compatibleWeaponType;
    }

    public Ammunition(String name, int damageBonus, String compatibleWeaponType) {
        super(name, "Ammunition", 5);  // Default orange color for ammo
        this.damageBonus = damageBonus;
        this.compatibleWeaponType = compatibleWeaponType;
    }

    public int getDamageBonus() {
        return damageBonus;
    }

    public void setDamageBonus(int damageBonus) {
        this.damageBonus = damageBonus;
    }

    public String getCompatibleWeaponType() {
        return compatibleWeaponType;
    }

    public void setCompatibleWeaponType(String compatibleWeaponType) {
        this.compatibleWeaponType = compatibleWeaponType;
    }

    public boolean isCompatibleWith(Weapon weapon) {
        if (compatibleWeaponType == null || compatibleWeaponType.equals("Any")) {
            return true;
        }
        return weapon.getType().equalsIgnoreCase(compatibleWeaponType);
    }
}
