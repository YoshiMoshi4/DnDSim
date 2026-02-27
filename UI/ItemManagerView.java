package UI;

import EntityRes.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

import static UI.CardUtils.*;

public class ItemManagerView {

    private final Stage stage;
    private TabPane tabbedPane;
    private ArrayList<Weapon> weapons = new ArrayList<>();
    private ArrayList<Armor> armors = new ArrayList<>();
    private ArrayList<Consumable> consumables = new ArrayList<>();
    private ArrayList<Ammunition> ammunition = new ArrayList<>();
    private ArrayList<CraftingItem> craftingItems = new ArrayList<>();
    private ArrayList<KeyItem> keyItems = new ArrayList<>();
    
    private static final String[] ATTRIBUTE_NAMES = {"STR", "DEX", "ITV", "MOB"};
    private static final String[] ARMOR_TYPES = {"Head", "Torso", "Legs"};
    private static final String[] CRAFTING_CATEGORIES = {"Material", "Component", "Reagent", "Miscellaneous"};

    public ItemManagerView() {
        stage = new Stage();
        stage.setTitle("Item Manager");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("panel-dark");
        root.setPadding(new Insets(10));
        
        // Top button panel
        HBox topPanel = createButtonPanel();
        root.setTop(topPanel);
        
        // Load items and create tabbed pane
        loadItems();
        tabbedPane = new TabPane();
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        refreshDisplay();
        
        root.setCenter(tabbedPane);
        
        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(400);
    }

    private HBox createButtonPanel() {
        HBox panel = new HBox(8);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-border-color: #505052; -fx-border-width: 0 0 1 0;");
        
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> handleBack());
        
        Button addWeaponBtn = new Button("+ Weapon");
        addWeaponBtn.getStyleClass().add("button-primary");
        addWeaponBtn.setOnAction(e -> addNewWeapon());
        
        Button addArmorBtn = new Button("+ Armor");
        addArmorBtn.getStyleClass().add("button-primary");
        addArmorBtn.setOnAction(e -> addNewArmor());
        
        Button addConsumableBtn = new Button("+ Consumable");
        addConsumableBtn.getStyleClass().add("button-primary");
        addConsumableBtn.setOnAction(e -> addNewConsumable());
        
        Button addAmmoBtn = new Button("+ Ammo");
        addAmmoBtn.getStyleClass().add("button-primary");
        addAmmoBtn.setOnAction(e -> addNewAmmunition());
        
        Button addCraftingBtn = new Button("+ Crafting");
        addCraftingBtn.getStyleClass().add("button-primary");
        addCraftingBtn.setOnAction(e -> addNewCraftingItem());
        
        Button addKeyItemBtn = new Button("+ Key Item");
        addKeyItemBtn.getStyleClass().add("button-primary");
        addKeyItemBtn.setOnAction(e -> addNewKeyItem());
        
        panel.getChildren().addAll(backBtn, addWeaponBtn, addArmorBtn, addConsumableBtn, 
                                   addAmmoBtn, addCraftingBtn, addKeyItemBtn);
        return panel;
    }

    private void loadItems() {
        ItemDatabase db = ItemDatabase.getInstance();
        weapons.clear();
        armors.clear();
        consumables.clear();
        ammunition.clear();
        craftingItems.clear();
        keyItems.clear();
        
        weapons.addAll(db.getAllWeapons().values());
        armors.addAll(db.getAllArmors().values());
        consumables.addAll(db.getAllConsumables().values());
        ammunition.addAll(db.getAllAmmunition().values());
        craftingItems.addAll(db.getAllCraftingItems().values());
        keyItems.addAll(db.getAllKeyItems().values());
    }

    private void refreshDisplay() {
        int selectedIndex = tabbedPane.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) selectedIndex = 0;
        
        loadItems();
        tabbedPane.getTabs().clear();
        
        tabbedPane.getTabs().add(new Tab("Weapons", createItemPanel(weapons, this::createWeaponCard)));
        tabbedPane.getTabs().add(new Tab("Armor", createItemPanel(armors, this::createArmorCard)));
        tabbedPane.getTabs().add(new Tab("Consumables", createItemPanel(consumables, this::createConsumableCard)));
        tabbedPane.getTabs().add(new Tab("Ammunition", createItemPanel(ammunition, this::createAmmunitionCard)));
        tabbedPane.getTabs().add(new Tab("Crafting", createItemPanel(craftingItems, this::createCraftingCard)));
        tabbedPane.getTabs().add(new Tab("Key Items", createItemPanel(keyItems, this::createKeyItemCard)));
        
        tabbedPane.getSelectionModel().select(selectedIndex);
    }

    @FunctionalInterface
    private interface CardCreator<T> {
        VBox create(T item);
    }

    private <T> ScrollPane createItemPanel(ArrayList<T> items, CardCreator<T> cardCreator) {
        FlowPane flowPane = ResponsiveUtils.createResponsiveFlowPane();
        flowPane.getStyleClass().add("panel");
        
        for (T item : items) {
            flowPane.getChildren().add(cardCreator.create(item));
        }
        
        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("panel");
        return scroll;
    }

    private VBox createWeaponCard(Weapon weapon) {
        return CardUtils.createWeaponCard(weapon, () -> editWeapon(weapon), () -> deleteItem(weapon));
    }

    private VBox createArmorCard(Armor armor) {
        return CardUtils.createArmorCard(armor, () -> editArmor(armor), () -> deleteItem(armor));
    }

    private VBox createConsumableCard(Consumable item) {
        return CardUtils.createConsumableCard(item, () -> editConsumable(item), () -> deleteItem(item));
    }

    private VBox createAmmunitionCard(Ammunition item) {
        return CardUtils.createAmmunitionCard(item, () -> editAmmunition(item), () -> deleteItem(item));
    }

    private VBox createCraftingCard(CraftingItem item) {
        return CardUtils.createCraftingItemCard(item, () -> editCraftingItem(item), () -> deleteItem(item));
    }

    private VBox createKeyItemCard(KeyItem item) {
        return CardUtils.createKeyItemCard(item, () -> editKeyItem(item), () -> deleteItem(item));
    }

    // ===== ADD DIALOGS =====

    private void addNewWeapon() {
        Stage dialog = createDialog("Add Weapon", 350, 350);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField damageField = new TextField("1");
        TextField strField = new TextField("0");
        TextField dexField = new TextField("0");
        TextField itvField = new TextField("0");
        TextField mobField = new TextField("0");
        ComboBox<String> colorCombo = createColorCombo(4);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage:"), 0, row); grid.add(damageField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damage = Integer.parseInt(damageField.getText());
            int[] attrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                Weapon weapon = new Weapon(name, "Weapon", damage, attrs);
                weapon.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(weapon);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 350, 350));
        dialog.showAndWait();
    }

    private void addNewArmor() {
        Stage dialog = createDialog("Add Armor", 350, 400);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(ARMOR_TYPES);
        typeCombo.getSelectionModel().selectFirst();
        TextField defenseField = new TextField("1");
        TextField strField = new TextField("0");
        TextField dexField = new TextField("0");
        TextField itvField = new TextField("0");
        TextField mobField = new TextField("0");
        ComboBox<String> colorCombo = createColorCombo(9);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Type:"), 0, row); grid.add(typeCombo, 1, row++);
        grid.add(new Label("Defense:"), 0, row); grid.add(defenseField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int armorType = typeCombo.getSelectionModel().getSelectedIndex();
            int defense = Integer.parseInt(defenseField.getText());
            int[] attrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                Armor armor = new Armor(name, "Armor", armorType, defense, attrs);
                armor.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(armor);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 350, 400));
        dialog.showAndWait();
    }

    private void addNewConsumable() {
        Stage dialog = createDialog("Add Consumable", 300, 200);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField healField = new TextField("10");
        ComboBox<String> colorCombo = createColorCombo(7);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Heal Amount:"), 0, row); grid.add(healField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int healAmount = Integer.parseInt(healField.getText());
            if (!name.isEmpty()) {
                Consumable item = new Consumable(name, "Consumable", colorCombo.getSelectionModel().getSelectedIndex(), healAmount, null);
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 300, 200));
        dialog.showAndWait();
    }

    private void addNewAmmunition() {
        Stage dialog = createDialog("Add Ammunition", 320, 250);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField damageBonusField = new TextField("0");
        TextField compatibleField = new TextField("Any");
        ComboBox<String> colorCombo = createColorCombo(5);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage Bonus:"), 0, row); grid.add(damageBonusField, 1, row++);
        grid.add(new Label("Compatible:"), 0, row); grid.add(compatibleField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damageBonus = Integer.parseInt(damageBonusField.getText());
            if (!name.isEmpty()) {
                Ammunition item = new Ammunition(name, "Ammunition", colorCombo.getSelectionModel().getSelectedIndex(), 
                        damageBonus, compatibleField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 320, 250));
        dialog.showAndWait();
    }

    private void addNewCraftingItem() {
        Stage dialog = createDialog("Add Crafting Item", 320, 250);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(CRAFTING_CATEGORIES);
        categoryCombo.getSelectionModel().selectFirst();
        TextField descField = new TextField();
        ComboBox<String> colorCombo = createColorCombo(14);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Category:"), 0, row); grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                CraftingItem item = new CraftingItem(name, "Crafting", colorCombo.getSelectionModel().getSelectedIndex(),
                        categoryCombo.getSelectionModel().getSelectedItem(), descField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 320, 250));
        dialog.showAndWait();
    }

    private void addNewKeyItem() {
        Stage dialog = createDialog("Add Key Item", 320, 250);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField descField = new TextField();
        CheckBox questCheck = new CheckBox("Quest Related");
        ComboBox<String> colorCombo = createColorCombo(6);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(questCheck, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                KeyItem item = new KeyItem(name, "Key Item", colorCombo.getSelectionModel().getSelectedIndex(),
                        descField.getText(), questCheck.isSelected());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 320, 250));
        dialog.showAndWait();
    }

    // ===== EDIT DIALOGS =====

    private void editWeapon(Weapon weapon) {
        Stage dialog = createDialog("Edit Weapon", 350, 350);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(weapon.getName());
        TextField damageField = new TextField(String.valueOf(weapon.getDamage()));
        int[] attrs = weapon.getModifiedAttributes();
        TextField strField = new TextField(String.valueOf(attrs[0]));
        TextField dexField = new TextField(String.valueOf(attrs[1]));
        TextField itvField = new TextField(String.valueOf(attrs[2]));
        TextField mobField = new TextField(String.valueOf(attrs[3]));
        ComboBox<String> colorCombo = createColorCombo(weapon.getColor());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage:"), 0, row); grid.add(damageField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damage = Integer.parseInt(damageField.getText());
            int[] newAttrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                weapon.setName(name);
                weapon.setDamage(damage);
                weapon.setModifiedAttributes(newAttrs);
                weapon.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(weapon);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 350, 350));
        dialog.showAndWait();
    }

    private void editArmor(Armor armor) {
        Stage dialog = createDialog("Edit Armor", 350, 400);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(armor.getName());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(ARMOR_TYPES);
        typeCombo.getSelectionModel().select(armor.getArmorType());
        TextField defenseField = new TextField(String.valueOf(armor.getDefense()));
        int[] attrs = armor.getModifiedAttributes();
        TextField strField = new TextField(String.valueOf(attrs[0]));
        TextField dexField = new TextField(String.valueOf(attrs[1]));
        TextField itvField = new TextField(String.valueOf(attrs[2]));
        TextField mobField = new TextField(String.valueOf(attrs[3]));
        ComboBox<String> colorCombo = createColorCombo(armor.getColor());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Type:"), 0, row); grid.add(typeCombo, 1, row++);
        grid.add(new Label("Defense:"), 0, row); grid.add(defenseField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int defense = Integer.parseInt(defenseField.getText());
            int[] newAttrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                armor.setName(name);
                armor.setArmorType(typeCombo.getSelectionModel().getSelectedIndex());
                armor.setDefense(defense);
                armor.setModifiedAttributes(newAttrs);
                armor.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(armor);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 350, 400));
        dialog.showAndWait();
    }

    private void editConsumable(Consumable item) {
        Stage dialog = createDialog("Edit Consumable", 300, 200);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        TextField healField = new TextField(String.valueOf(item.getHealAmount()));
        ComboBox<String> colorCombo = createColorCombo(item.getColor());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Heal Amount:"), 0, row); grid.add(healField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int healAmount = Integer.parseInt(healField.getText());
            if (!name.isEmpty()) {
                item.setName(name);
                item.setHealAmount(healAmount);
                item.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 300, 200));
        dialog.showAndWait();
    }

    private void editAmmunition(Ammunition item) {
        Stage dialog = createDialog("Edit Ammunition", 320, 250);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        TextField damageBonusField = new TextField(String.valueOf(item.getDamageBonus()));
        TextField compatibleField = new TextField(item.getCompatibleWeaponType() != null ? item.getCompatibleWeaponType() : "Any");
        ComboBox<String> colorCombo = createColorCombo(item.getColor());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage Bonus:"), 0, row); grid.add(damageBonusField, 1, row++);
        grid.add(new Label("Compatible:"), 0, row); grid.add(compatibleField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damageBonus = Integer.parseInt(damageBonusField.getText());
            if (!name.isEmpty()) {
                item.setName(name);
                item.setDamageBonus(damageBonus);
                item.setCompatibleWeaponType(compatibleField.getText());
                item.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 320, 250));
        dialog.showAndWait();
    }

    private void editCraftingItem(CraftingItem item) {
        Stage dialog = createDialog("Edit Crafting Item", 320, 250);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(CRAFTING_CATEGORIES);
        for (int i = 0; i < CRAFTING_CATEGORIES.length; i++) {
            if (CRAFTING_CATEGORIES[i].equals(item.getCraftingCategory())) {
                categoryCombo.getSelectionModel().select(i);
                break;
            }
        }
        TextField descField = new TextField(item.getDescription() != null ? item.getDescription() : "");
        ComboBox<String> colorCombo = createColorCombo(item.getColor());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Category:"), 0, row); grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                item.setName(name);
                item.setCraftingCategory(categoryCombo.getSelectionModel().getSelectedItem());
                item.setDescription(descField.getText());
                item.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 320, 250));
        dialog.showAndWait();
    }

    private void editKeyItem(KeyItem item) {
        Stage dialog = createDialog("Edit Key Item", 320, 250);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        TextField descField = new TextField(item.getDescription() != null ? item.getDescription() : "");
        CheckBox questCheck = new CheckBox("Quest Related");
        questCheck.setSelected(item.isQuestRelated());
        ComboBox<String> colorCombo = createColorCombo(item.getColor());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(questCheck, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorCombo, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                item.setName(name);
                item.setDescription(descField.getText());
                item.setQuestRelated(questCheck.isSelected());
                item.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        });
        
        dialog.setScene(createDialogScene(grid, 320, 250));
        dialog.showAndWait();
    }

    // ===== HELPERS =====

    private void deleteItem(Item item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Item");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete " + item.getName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ItemDatabase.getInstance().deleteItem(item);
                refreshDisplay();
            }
        });
    }

    private Stage createDialog(String title, int width, int height) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle(title);
        dialog.setResizable(false);
        return dialog;
    }

    private GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("panel-dark");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        return grid;
    }

    private Scene createDialogScene(GridPane grid, int width, int height) {
        Scene scene = new Scene(grid, width, height);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        return scene;
    }

    private ComboBox<String> createColorCombo(int selectedIndex) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(CharSheet.getColorNames());
        combo.getSelectionModel().select(selectedIndex);
        return combo;
    }

    @FunctionalInterface
    private interface DialogAction {
        boolean execute() throws NumberFormatException;
    }

    private void addDialogButtons(GridPane grid, int row, Stage dialog, DialogAction action) {
        HBox btnBox = new HBox(10);
        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("button-primary");
        okBtn.setOnAction(e -> {
            try {
                if (action.execute()) {
                    dialog.close();
                    refreshDisplay();
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numbers.");
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setOnAction(e -> dialog.close());
        
        btnBox.getChildren().addAll(okBtn, cancelBtn);
        grid.add(btnBox, 0, row);
        GridPane.setColumnSpan(btnBox, 2);
    }

    private int parseInt(TextField field) {
        return Integer.parseInt(field.getText());
    }

    private void handleBack() {
        stage.hide();
        Stage mainStage = new Stage();
        new MainMenuView(mainStage).show();
    }

    public void show() {
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
