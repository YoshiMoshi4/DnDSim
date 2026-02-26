package EntityRes;

public class KeyItem extends Item {

    private String description;
    private boolean questRelated;

    public KeyItem(String name, String type, int color, String description, boolean questRelated) {
        super(name, type, color);
        this.description = description;
        this.questRelated = questRelated;
    }

    public KeyItem(String name, String description, boolean questRelated) {
        super(name, "Key Item", 6);  // Default yellow color for key items
        this.description = description;
        this.questRelated = questRelated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isQuestRelated() {
        return questRelated;
    }

    public void setQuestRelated(boolean questRelated) {
        this.questRelated = questRelated;
    }
}
