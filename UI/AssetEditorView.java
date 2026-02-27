package UI;

import EntityRes.*;
import Objects.TerrainObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AssetEditorView {

    private final Stage stage;
    private TabPane mainTabs;
    private TabPane itemTabs;
    private HBox itemButtonPanel;
    private HBox terrainButtonPanel;
    private FlowPane terrainPane;
    
    // Item data
    private ArrayList<Weapon> weapons = new ArrayList<>();
    private ArrayList<Armor> armors = new ArrayList<>();
    private ArrayList<Consumable> consumables = new ArrayList<>();
    private ArrayList<Ammunition> ammunition = new ArrayList<>();
    private ArrayList<CraftingItem> craftingItems = new ArrayList<>();
    private ArrayList<KeyItem> keyItems = new ArrayList<>();
    
    private static final String[] ATTRIBUTE_NAMES = {"STR", "DEX", "ITV", "MOB"};
    private static final String[] ARMOR_TYPES = {"Head", "Torso", "Legs"};
    private static final String[] CRAFTING_CATEGORIES = {"Material", "Component", "Reagent", "Miscellaneous"};

    public AssetEditorView() {
        stage = new Stage();
        stage.setTitle("Asset Editor");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("panel-dark");
        root.setPadding(new Insets(10));
        
        // Top panel with back button and contextual add buttons
        VBox topContainer = new VBox();
        topContainer.setStyle("-fx-border-color: #505052; -fx-border-width: 0 0 1 0;");
        
        HBox backPanel = new HBox(10);
        backPanel.setPadding(new Insets(10, 10, 5, 10));
        backPanel.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> handleBack());
        backPanel.getChildren().add(backBtn);
        
        // Item add buttons (shown when Items tab selected)
        itemButtonPanel = createItemButtonPanel();
        
        // Terrain add button (shown when Terrain tab selected)
        terrainButtonPanel = createTerrainButtonPanel();
        
        topContainer.getChildren().addAll(backPanel, itemButtonPanel, terrainButtonPanel);
        root.setTop(topContainer);
        
        // Main tabs for Items vs Terrain
        mainTabs = new TabPane();
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Items tab
        Tab itemsTab = new Tab("Items");
        itemsTab.setContent(createItemsContent());
        mainTabs.getTabs().add(itemsTab);
        
        // Terrain tab
        Tab terrainTab = new Tab("Terrain");
        terrainTab.setContent(createTerrainContent());
        mainTabs.getTabs().add(terrainTab);
        
        // Show/hide contextual buttons based on selected tab
        mainTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            itemButtonPanel.setVisible(newTab == itemsTab);
            itemButtonPanel.setManaged(newTab == itemsTab);
            terrainButtonPanel.setVisible(newTab == terrainTab);
            terrainButtonPanel.setManaged(newTab == terrainTab);
        });
        
        // Initial visibility
        terrainButtonPanel.setVisible(false);
        terrainButtonPanel.setManaged(false);
        
        root.setCenter(mainTabs);
        
        Scene scene = new Scene(root, 950, 700);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        stage.setScene(scene);
        stage.setMinWidth(750);
        stage.setMinHeight(500);
    }

    private HBox createItemButtonPanel() {
        HBox panel = new HBox(8);
        panel.setPadding(new Insets(5, 10, 10, 10));
        panel.setAlignment(Pos.CENTER_LEFT);
        
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
        
        panel.getChildren().addAll(addWeaponBtn, addArmorBtn, addConsumableBtn, 
                                   addAmmoBtn, addCraftingBtn, addKeyItemBtn);
        return panel;
    }

    private HBox createTerrainButtonPanel() {
        HBox panel = new HBox(8);
        panel.setPadding(new Insets(5, 10, 10, 10));
        panel.setAlignment(Pos.CENTER_LEFT);
        
        Button addBtn = new Button("+ Add Terrain");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> addNewTerrain());
        
        panel.getChildren().add(addBtn);
        return panel;
    }

    // ==================== ITEMS ====================

    private VBox createItemsContent() {
        VBox content = new VBox();
        
        loadItems();
        itemTabs = new TabPane();
        itemTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        refreshItemDisplay();
        VBox.setVgrow(itemTabs, Priority.ALWAYS);
        
        content.getChildren().add(itemTabs);
        return content;
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

    private void refreshItemDisplay() {
        int selectedIndex = itemTabs.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) selectedIndex = 0;
        
        loadItems();
        itemTabs.getTabs().clear();
        
        itemTabs.getTabs().add(new Tab("Weapons", createItemPanel(weapons, this::createWeaponCard)));
        itemTabs.getTabs().add(new Tab("Armor", createItemPanel(armors, this::createArmorCard)));
        itemTabs.getTabs().add(new Tab("Consumables", createItemPanel(consumables, this::createConsumableCard)));
        itemTabs.getTabs().add(new Tab("Ammunition", createItemPanel(ammunition, this::createAmmunitionCard)));
        itemTabs.getTabs().add(new Tab("Crafting", createItemPanel(craftingItems, this::createCraftingCard)));
        itemTabs.getTabs().add(new Tab("Key Items", createItemPanel(keyItems, this::createKeyItemCard)));
        
        itemTabs.getSelectionModel().select(selectedIndex);
    }

    @FunctionalInterface
    private interface CardCreator<T> {
        VBox create(T item);
    }

    private <T> ScrollPane createItemPanel(ArrayList<T> items, CardCreator<T> cardCreator) {
        FlowPane flowPane = new FlowPane(10, 10);
        flowPane.setPadding(new Insets(10));
        flowPane.getStyleClass().add("panel");
        
        for (T item : items) {
            flowPane.getChildren().add(cardCreator.create(item));
        }
        
        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("panel");
        return scroll;
    }

    private VBox createWeaponCard(Weapon weapon) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(180);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(weapon.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label damageLabel = new Label("Damage: " + weapon.getDamage());
        damageLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(damageLabel);
        int[] attrs = weapon.getModifiedAttributes();
        for (int i = 0; i < attrs.length && i < ATTRIBUTE_NAMES.length; i++) {
            if (attrs[i] != 0) {
                Label attrLabel = new Label(ATTRIBUTE_NAMES[i] + ": " + (attrs[i] > 0 ? "+" : "") + attrs[i]);
                attrLabel.setStyle("-fx-text-fill: #aaaaaa;");
                card.getChildren().add(attrLabel);
            }
        }
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editWeapon(weapon));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteItem(weapon));
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(btnBox);
        
        return card;
    }

    private VBox createArmorCard(Armor armor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(180);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(armor.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label typeLabel = new Label("Type: " + ARMOR_TYPES[armor.getArmorType()]);
        typeLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(typeLabel);
        Label defLabel = new Label("Defense: " + armor.getDefense());
        defLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(defLabel);
        int[] attrs = armor.getModifiedAttributes();
        for (int i = 0; i < attrs.length && i < ATTRIBUTE_NAMES.length; i++) {
            if (attrs[i] != 0) {
                Label attrLabel = new Label(ATTRIBUTE_NAMES[i] + ": " + (attrs[i] > 0 ? "+" : "") + attrs[i]);
                attrLabel.setStyle("-fx-text-fill: #aaaaaa;");
                card.getChildren().add(attrLabel);
            }
        }
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editArmor(armor));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteItem(armor));
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(btnBox);
        
        return card;
    }

    private VBox createConsumableCard(Consumable item) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(150);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label healLabel = new Label("Heal: " + item.getHealAmount());
        healLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(healLabel);
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editConsumable(item));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteItem(item));
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(btnBox);
        
        return card;
    }

    private VBox createAmmunitionCard(Ammunition item) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(150);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label dmgLabel = new Label("Damage +" + item.getDamageBonus());
        dmgLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(dmgLabel);
        Label forLabel = new Label("For: " + item.getCompatibleWeaponType());
        forLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(forLabel);
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editAmmunition(item));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteItem(item));
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(btnBox);
        
        return card;
    }

    private VBox createCraftingCard(CraftingItem item) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(150);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label catLabel = new Label(item.getCraftingCategory());
        catLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(catLabel);
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editCraftingItem(item));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteItem(item));
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(btnBox);
        
        return card;
    }

    private VBox createKeyItemCard(KeyItem item) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(150);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label typeLabel = new Label(item.isQuestRelated() ? "Quest Item" : "Key Item");
        typeLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(typeLabel);
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editKeyItem(item));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteItem(item));
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(btnBox);
        
        return card;
    }

    private String getCardStyle() {
        return "-fx-background-color: #3a3a3c; -fx-background-radius: 8; -fx-border-radius: 8; " +
               "-fx-border-color: #505052; -fx-border-width: 1;";
    }

    // ==================== TERRAIN ====================

    private VBox createTerrainContent() {
        VBox content = new VBox();
        
        terrainPane = new FlowPane(10, 10);
        terrainPane.setPadding(new Insets(10));
        terrainPane.getStyleClass().add("panel");
        
        ScrollPane scroll = new ScrollPane(terrainPane);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("panel");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        refreshTerrainDisplay();
        
        content.getChildren().add(scroll);
        return content;
    }

    private void refreshTerrainDisplay() {
        terrainPane.getChildren().clear();
        
        List<TerrainObject> terrains = TerrainDatabase.getInstance().getAllTerrains();
        for (TerrainObject terrain : terrains) {
            terrainPane.getChildren().add(createTerrainCard(terrain));
        }
    }

    private VBox createTerrainCard(TerrainObject terrain) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(180);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(terrain.getType());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label hpLabel = new Label("HP: " + terrain.getHealth());
        hpLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(hpLabel);
        Label blocksLabel = new Label("Blocks: " + (terrain.blocksMovement() ? "Yes" : "No"));
        blocksLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(blocksLabel);
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editTerrain(terrain));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteTerrain(terrain));
        
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(btnBox);
        
        return card;
    }

    // ==================== ITEM DIALOGS ====================

    private void addNewWeapon() {
        Stage dialog = createDialog("Add Weapon", 350, 300);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField damageField = new TextField("1");
        TextField strField = new TextField("0");
        TextField dexField = new TextField("0");
        TextField itvField = new TextField("0");
        TextField mobField = new TextField("0");
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage:"), 0, row); grid.add(damageField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damage = Integer.parseInt(damageField.getText());
            int[] attrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                Weapon weapon = new Weapon(name, "Weapon", damage, attrs);
                ItemDatabase.getInstance().saveItem(weapon);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 350, 300));
        dialog.showAndWait();
    }

    private void addNewArmor() {
        Stage dialog = createDialog("Add Armor", 350, 350);
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
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Type:"), 0, row); grid.add(typeCombo, 1, row++);
        grid.add(new Label("Defense:"), 0, row); grid.add(defenseField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int armorType = typeCombo.getSelectionModel().getSelectedIndex();
            int defense = Integer.parseInt(defenseField.getText());
            int[] attrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                Armor armor = new Armor(name, "Armor", armorType, defense, attrs);
                ItemDatabase.getInstance().saveItem(armor);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 350, 350));
        dialog.showAndWait();
    }

    private void addNewConsumable() {
        Stage dialog = createDialog("Add Consumable", 300, 150);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField healField = new TextField("10");
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Heal Amount:"), 0, row); grid.add(healField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int healAmount = Integer.parseInt(healField.getText());
            if (!name.isEmpty()) {
                Consumable item = new Consumable(name, "Consumable", 0, healAmount, null);
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 300, 150));
        dialog.showAndWait();
    }

    private void addNewAmmunition() {
        Stage dialog = createDialog("Add Ammunition", 320, 180);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField damageBonusField = new TextField("0");
        TextField compatibleField = new TextField("Any");
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage Bonus:"), 0, row); grid.add(damageBonusField, 1, row++);
        grid.add(new Label("Compatible:"), 0, row); grid.add(compatibleField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damageBonus = Integer.parseInt(damageBonusField.getText());
            if (!name.isEmpty()) {
                Ammunition item = new Ammunition(name, "Ammunition", 0, 
                        damageBonus, compatibleField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 180));
        dialog.showAndWait();
    }

    private void addNewCraftingItem() {
        Stage dialog = createDialog("Add Crafting Item", 320, 180);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(CRAFTING_CATEGORIES);
        categoryCombo.getSelectionModel().selectFirst();
        TextField descField = new TextField();
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Category:"), 0, row); grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                CraftingItem item = new CraftingItem(name, "Crafting", 0,
                        categoryCombo.getSelectionModel().getSelectedItem(), descField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 180));
        dialog.showAndWait();
    }

    private void addNewKeyItem() {
        Stage dialog = createDialog("Add Key Item", 320, 180);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField descField = new TextField();
        CheckBox questCheck = new CheckBox("Quest Related");
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(questCheck, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                KeyItem item = new KeyItem(name, "Key Item", 0,
                        descField.getText(), questCheck.isSelected());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 180));
        dialog.showAndWait();
    }

    // ===== EDIT DIALOGS =====

    private void editWeapon(Weapon weapon) {
        Stage dialog = createDialog("Edit Weapon", 350, 300);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(weapon.getName());
        TextField damageField = new TextField(String.valueOf(weapon.getDamage()));
        int[] attrs = weapon.getModifiedAttributes();
        TextField strField = new TextField(String.valueOf(attrs[0]));
        TextField dexField = new TextField(String.valueOf(attrs[1]));
        TextField itvField = new TextField(String.valueOf(attrs[2]));
        TextField mobField = new TextField(String.valueOf(attrs[3]));
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage:"), 0, row); grid.add(damageField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damage = Integer.parseInt(damageField.getText());
            int[] newAttrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                weapon.setName(name);
                weapon.setDamage(damage);
                weapon.setModifiedAttributes(newAttrs);
                ItemDatabase.getInstance().saveItem(weapon);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 350, 300));
        dialog.showAndWait();
    }

    private void editArmor(Armor armor) {
        Stage dialog = createDialog("Edit Armor", 350, 350);
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
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Type:"), 0, row); grid.add(typeCombo, 1, row++);
        grid.add(new Label("Defense:"), 0, row); grid.add(defenseField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("ITV Mod:"), 0, row); grid.add(itvField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int defense = Integer.parseInt(defenseField.getText());
            int[] newAttrs = {parseInt(strField), parseInt(dexField), parseInt(itvField), parseInt(mobField)};
            if (!name.isEmpty()) {
                armor.setName(name);
                armor.setArmorType(typeCombo.getSelectionModel().getSelectedIndex());
                armor.setDefense(defense);
                armor.setModifiedAttributes(newAttrs);
                ItemDatabase.getInstance().saveItem(armor);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 350, 350));
        dialog.showAndWait();
    }

    private void editConsumable(Consumable item) {
        Stage dialog = createDialog("Edit Consumable", 300, 150);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        TextField healField = new TextField(String.valueOf(item.getHealAmount()));
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Heal Amount:"), 0, row); grid.add(healField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int healAmount = Integer.parseInt(healField.getText());
            if (!name.isEmpty()) {
                item.setName(name);
                item.setHealAmount(healAmount);
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 300, 150));
        dialog.showAndWait();
    }

    private void editAmmunition(Ammunition item) {
        Stage dialog = createDialog("Edit Ammunition", 320, 180);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        TextField damageBonusField = new TextField(String.valueOf(item.getDamageBonus()));
        TextField compatibleField = new TextField(item.getCompatibleWeaponType() != null ? item.getCompatibleWeaponType() : "Any");
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Damage Bonus:"), 0, row); grid.add(damageBonusField, 1, row++);
        grid.add(new Label("Compatible:"), 0, row); grid.add(compatibleField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int damageBonus = Integer.parseInt(damageBonusField.getText());
            if (!name.isEmpty()) {
                item.setName(name);
                item.setDamageBonus(damageBonus);
                item.setCompatibleWeaponType(compatibleField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 180));
        dialog.showAndWait();
    }

    private void editCraftingItem(CraftingItem item) {
        Stage dialog = createDialog("Edit Crafting Item", 320, 180);
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
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Category:"), 0, row); grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                item.setName(name);
                item.setCraftingCategory(categoryCombo.getSelectionModel().getSelectedItem());
                item.setDescription(descField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 180));
        dialog.showAndWait();
    }

    private void editKeyItem(KeyItem item) {
        Stage dialog = createDialog("Edit Key Item", 320, 180);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        TextField descField = new TextField(item.getDescription() != null ? item.getDescription() : "");
        CheckBox questCheck = new CheckBox("Quest Related");
        questCheck.setSelected(item.isQuestRelated());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(questCheck, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                item.setName(name);
                item.setDescription(descField.getText());
                item.setQuestRelated(questCheck.isSelected());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 180));
        dialog.showAndWait();
    }

    // ==================== TERRAIN DIALOGS ====================

    private void addNewTerrain() {
        Stage dialog = createDialog("Add Terrain", 300, 180);
        GridPane grid = createDialogGrid();
        
        TextField typeField = new TextField();
        TextField hpField = new TextField("10");
        CheckBox blocksCheck = new CheckBox("Blocks Movement");
        blocksCheck.setSelected(true);
        
        int row = 0;
        grid.add(new Label("Type:"), 0, row); grid.add(typeField, 1, row++);
        grid.add(new Label("HP:"), 0, row); grid.add(hpField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(blocksCheck, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String type = typeField.getText();
            int hp = Integer.parseInt(hpField.getText());
            boolean blocks = blocksCheck.isSelected();
            if (!type.isEmpty()) {
                TerrainObject newTerrain = new TerrainObject(0, 0, type, hp, 0, blocks);
                TerrainDatabase.getInstance().addTerrain(newTerrain);
                return true;
            }
            return false;
        }, this::refreshTerrainDisplay);
        
        dialog.setScene(createDialogScene(grid, 300, 180));
        dialog.showAndWait();
    }

    private void editTerrain(TerrainObject terrain) {
        Stage dialog = createDialog("Edit Terrain", 300, 180);
        GridPane grid = createDialogGrid();
        
        TextField typeField = new TextField(terrain.getType());
        TextField hpField = new TextField(String.valueOf(terrain.getHealth()));
        CheckBox blocksCheck = new CheckBox("Blocks Movement");
        blocksCheck.setSelected(terrain.blocksMovement());
        
        int row = 0;
        grid.add(new Label("Type:"), 0, row); grid.add(typeField, 1, row++);
        grid.add(new Label("HP:"), 0, row); grid.add(hpField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(blocksCheck, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            terrain.setBlocksMovement(blocksCheck.isSelected());
            TerrainDatabase.getInstance().saveTerrain(terrain);
            return true;
        }, this::refreshTerrainDisplay);
        
        dialog.setScene(createDialogScene(grid, 300, 180));
        dialog.showAndWait();
    }

    private void deleteTerrain(TerrainObject terrain) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Terrain");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete " + terrain.getType() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                TerrainDatabase.getInstance().deleteTerrain(terrain);
                refreshTerrainDisplay();
            }
        });
    }

    // ==================== HELPERS ====================

    private void deleteItem(Item item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Item");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete " + item.getName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ItemDatabase.getInstance().deleteItem(item);
                refreshItemDisplay();
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

    @FunctionalInterface
    private interface DialogAction {
        boolean execute() throws NumberFormatException;
    }

    private void addDialogButtons(GridPane grid, int row, Stage dialog, DialogAction action, Runnable refreshAction) {
        HBox btnBox = new HBox(10);
        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("button-primary");
        okBtn.setOnAction(e -> {
            try {
                if (action.execute()) {
                    dialog.close();
                    refreshAction.run();
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
