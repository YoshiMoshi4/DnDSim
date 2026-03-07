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

    private final AppController appController;
    private final BorderPane root;
    private TabPane mainTabs;
    private TabPane itemTabs;
    private HBox itemButtonPanel;
    private HBox terrainButtonPanel;
    private FlowPane terrainPane;
    
    // Item data
    private ArrayList<Weapon> weapons = new ArrayList<>();
    private ArrayList<Accessory> accessories = new ArrayList<>();
    private ArrayList<Consumable> consumables = new ArrayList<>();
    private ArrayList<Ammunition> ammunition = new ArrayList<>();
    private ArrayList<CraftingItem> craftingItems = new ArrayList<>();
    private ArrayList<KeyItem> keyItems = new ArrayList<>();
    
    private static final String[] ATTRIBUTE_NAMES = {"STR", "DEX", "MOB", "INT"};
    private static final String[] CRAFTING_CATEGORIES = {"Material", "Component", "Reagent", "Miscellaneous"};
    private static final String[] DICE_TYPES = {"d4", "d6", "d8", "d10", "d12", "d20"};
    private static final String[] STAT_TYPES = {"STRENGTH", "DEXTERITY", "INTELLIGENCE"};

    public AssetEditorView(AppController appController) {
        this.appController = appController;
        
        root = new BorderPane();
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
    }

    private HBox createItemButtonPanel() {
        HBox panel = new HBox(8);
        panel.setPadding(new Insets(5, 10, 10, 10));
        panel.setAlignment(Pos.CENTER_LEFT);
        
        Button addWeaponBtn = new Button("+ Weapon");
        addWeaponBtn.getStyleClass().add("button-primary");
        addWeaponBtn.setOnAction(e -> addNewWeapon());
        
        Button addAccessoryBtn = new Button("+ Accessory");
        addAccessoryBtn.getStyleClass().add("button-primary");
        addAccessoryBtn.setOnAction(e -> addNewAccessory());
        
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
        
        panel.getChildren().addAll(addWeaponBtn, addAccessoryBtn, addConsumableBtn, 
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
        accessories.clear();
        consumables.clear();
        ammunition.clear();
        craftingItems.clear();
        keyItems.clear();
        
        weapons.addAll(db.getAllWeapons().values());
        accessories.addAll(db.getAllAccessories().values());
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
        itemTabs.getTabs().add(new Tab("Accessories", createItemPanel(accessories, this::createAccessoryCard)));
        itemTabs.getTabs().add(new Tab("Consumables", createConsumablesPanel()));
        itemTabs.getTabs().add(new Tab("Key Items", createItemPanel(keyItems, this::createKeyItemCard)));
        
        itemTabs.getSelectionModel().select(selectedIndex);
    }

    private ScrollPane createConsumablesPanel() {
        FlowPane flowPane = new FlowPane(10, 10);
        flowPane.setPadding(new Insets(10));
        flowPane.getStyleClass().add("panel");
        
        for (Consumable c : consumables) {
            flowPane.getChildren().add(createConsumableCard(c));
        }
        for (Ammunition a : ammunition) {
            flowPane.getChildren().add(createAmmunitionCard(a));
        }
        for (CraftingItem ci : craftingItems) {
            flowPane.getChildren().add(createCraftingCard(ci));
        }
        
        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("panel");
        return scroll;
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
        
        // Display damage dice tiers
        String[] dice = weapon.getDamageDice();
        Label diceLabel = new Label("Dice: " + dice[0] + "/" + dice[1] + "/" + dice[2]);
        diceLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(diceLabel);
        
        Label statLabel = new Label("Stat: " + weapon.getStatType());
        statLabel.setStyle("-fx-text-fill: #aaaaaa;");
        card.getChildren().add(statLabel);
        
        // Show ammo type if ranged
        if (weapon.isRanged()) {
            Label ammoLabel = new Label("Ammo: " + weapon.getAmmoType());
            ammoLabel.setStyle("-fx-text-fill: #f39c12;");
            card.getChildren().add(ammoLabel);
        }
        
        int[] attrs = weapon.getModifiedAttributes();
        for (int i = 0; i < attrs.length && i < ATTRIBUTE_NAMES.length; i++) {
            if (attrs[i] != 0) {
                Label attrLabel = new Label(ATTRIBUTE_NAMES[i] + ": " + (attrs[i] > 0 ? "+" : "") + attrs[i]);
                attrLabel.setStyle("-fx-text-fill: #aaaaaa;");
                card.getChildren().add(attrLabel);
            }
        }
        
        // Display abilities
        if (weapon.hasAbilities()) {
            Label abilityHeader = new Label("Abilities:");
            abilityHeader.setStyle("-fx-text-fill: #dcdcaa; -fx-font-weight: bold;");
            card.getChildren().add(abilityHeader);
            for (ItemAbility ability : weapon.getAbilities()) {
                Label abilityLabel = new Label("  " + ability.getFormattedDescription());
                abilityLabel.setStyle("-fx-text-fill: #9cdcfe; -fx-font-size: 11px;");
                card.getChildren().add(abilityLabel);
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

    private VBox createAccessoryCard(Accessory accessory) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(180);
        card.setStyle(getCardStyle());
        
        Label nameLabel = new Label(accessory.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        
        Label defLabel = new Label("Defense: " + accessory.getDefense());
        defLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(defLabel);
        int[] attrs = accessory.getModifiedAttributes();
        for (int i = 0; i < attrs.length && i < ATTRIBUTE_NAMES.length; i++) {
            if (attrs[i] != 0) {
                Label attrLabel = new Label(ATTRIBUTE_NAMES[i] + ": " + (attrs[i] > 0 ? "+" : "") + attrs[i]);
                attrLabel.setStyle("-fx-text-fill: #aaaaaa;");
                card.getChildren().add(attrLabel);
            }
        }
        
        // Display abilities
        if (accessory.hasAbilities()) {
            Label abilityHeader = new Label("Abilities:");
            abilityHeader.setStyle("-fx-text-fill: #dcdcaa; -fx-font-weight: bold;");
            card.getChildren().add(abilityHeader);
            for (ItemAbility ability : accessory.getAbilities()) {
                Label abilityLabel = new Label("  " + ability.getFormattedDescription());
                abilityLabel.setStyle("-fx-text-fill: #9cdcfe; -fx-font-size: 11px;");
                card.getChildren().add(abilityLabel);
            }
        }
        
        HBox btnBox = new HBox(5);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> editAccessory(accessory));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteItem(accessory));
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
        
        Label typeLabel = new Label("Type: " + item.getAmmoType());
        typeLabel.setStyle("-fx-text-fill: #cccccc;");
        card.getChildren().add(typeLabel);
        
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
        Stage dialog = createDialog("Add Weapon", 400, 650);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        
        // Dice tier combos
        ComboBox<String> tier1Combo = new ComboBox<>();
        tier1Combo.getItems().addAll(DICE_TYPES);
        tier1Combo.getSelectionModel().select("d6");
        
        ComboBox<String> tier2Combo = new ComboBox<>();
        tier2Combo.getItems().addAll(DICE_TYPES);
        tier2Combo.getSelectionModel().select("d6");
        
        ComboBox<String> tier3Combo = new ComboBox<>();
        tier3Combo.getItems().addAll(DICE_TYPES);
        tier3Combo.getSelectionModel().select("d8");
        
        // Stat type combo
        ComboBox<String> statCombo = new ComboBox<>();
        statCombo.getItems().addAll(STAT_TYPES);
        statCombo.getSelectionModel().selectFirst();
        
        // Ammo type (empty for melee)
        TextField ammoTypeField = new TextField();
        ammoTypeField.setPromptText("Leave empty for melee");
        
        TextField strField = new TextField("0");
        TextField dexField = new TextField("0");
        TextField mobField = new TextField("0");
        TextField intField = new TextField("0");
        
        // Abilities list
        ArrayList<ItemAbility> weaponAbilities = new ArrayList<>();
        VBox abilityEditor = createAbilityEditorSection(weaponAbilities);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Tier 1 Die:"), 0, row); grid.add(tier1Combo, 1, row++);
        grid.add(new Label("Tier 2 Die:"), 0, row); grid.add(tier2Combo, 1, row++);
        grid.add(new Label("Tier 3 Die:"), 0, row); grid.add(tier3Combo, 1, row++);
        grid.add(new Label("Stat Type:"), 0, row); grid.add(statCombo, 1, row++);
        grid.add(new Label("Ammo Type:"), 0, row); grid.add(ammoTypeField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("INT Mod:"), 0, row); grid.add(intField, 1, row++);
        grid.add(abilityEditor, 0, row++, 2, 1);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            String[] damageDice = {tier1Combo.getValue(), tier2Combo.getValue(), tier3Combo.getValue()};
            String statType = statCombo.getValue();
            String ammoType = ammoTypeField.getText().trim();
            int[] attrs = {parseInt(strField), parseInt(dexField), parseInt(mobField), parseInt(intField)};
            if (!name.isEmpty()) {
                Weapon weapon = new Weapon(name, "Weapon", damageDice, statType, 
                        ammoType.isEmpty() ? null : ammoType, attrs);
                weapon.setAbilities(weaponAbilities);
                ItemDatabase.getInstance().saveItem(weapon);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 400, 650));
        dialog.showAndWait();
    }

    private void addNewAccessory() {
        Stage dialog = createDialog("Add Accessory", 380, 500);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField defenseField = new TextField("1");
        TextField strField = new TextField("0");
        TextField dexField = new TextField("0");
        TextField mobField = new TextField("0");
        TextField intField = new TextField("0");
        
        // Abilities list
        ArrayList<ItemAbility> accessoryAbilities = new ArrayList<>();
        VBox abilityEditor = createAbilityEditorSection(accessoryAbilities);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Defense:"), 0, row); grid.add(defenseField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("INT Mod:"), 0, row); grid.add(intField, 1, row++);
        grid.add(abilityEditor, 0, row++, 2, 1);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int defense = Integer.parseInt(defenseField.getText());
            int[] attrs = {parseInt(strField), parseInt(dexField), parseInt(mobField), parseInt(intField)};
            if (!name.isEmpty()) {
                Accessory accessory = new Accessory(name, "Accessory", defense, attrs);
                accessory.setAbilities(accessoryAbilities);
                ItemDatabase.getInstance().saveItem(accessory);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 380, 500));
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
        Stage dialog = createDialog("Add Ammunition", 320, 150);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField();
        TextField ammoTypeField = new TextField("12 Gauge");
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Ammo Type:"), 0, row); grid.add(ammoTypeField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                Ammunition item = new Ammunition(name, "Ammunition", 5, ammoTypeField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 150));
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
        Stage dialog = createDialog("Edit Weapon", 400, 650);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(weapon.getName());
        
        // Get existing dice values
        String[] dice = weapon.getDamageDice();
        
        // Dice tier combos
        ComboBox<String> tier1Combo = new ComboBox<>();
        tier1Combo.getItems().addAll(DICE_TYPES);
        tier1Combo.getSelectionModel().select(dice[0]);
        
        ComboBox<String> tier2Combo = new ComboBox<>();
        tier2Combo.getItems().addAll(DICE_TYPES);
        tier2Combo.getSelectionModel().select(dice[1]);
        
        ComboBox<String> tier3Combo = new ComboBox<>();
        tier3Combo.getItems().addAll(DICE_TYPES);
        tier3Combo.getSelectionModel().select(dice[2]);
        
        // Stat type combo
        ComboBox<String> statCombo = new ComboBox<>();
        statCombo.getItems().addAll(STAT_TYPES);
        statCombo.getSelectionModel().select(weapon.getStatType());
        
        // Ammo type
        TextField ammoTypeField = new TextField(weapon.getAmmoType() != null ? weapon.getAmmoType() : "");
        ammoTypeField.setPromptText("Leave empty for melee");
        
        int[] attrs = weapon.getModifiedAttributes();
        TextField strField = new TextField(String.valueOf(attrs[0]));
        TextField dexField = new TextField(String.valueOf(attrs[1]));
        TextField mobField = new TextField(String.valueOf(attrs[2]));
        TextField intField = new TextField(String.valueOf(attrs.length > 3 ? attrs[3] : 0));
        
        // Abilities list - copy existing abilities
        ArrayList<ItemAbility> weaponAbilities = new ArrayList<>(weapon.getAbilities());
        VBox abilityEditor = createAbilityEditorSection(weaponAbilities);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Tier 1 Die:"), 0, row); grid.add(tier1Combo, 1, row++);
        grid.add(new Label("Tier 2 Die:"), 0, row); grid.add(tier2Combo, 1, row++);
        grid.add(new Label("Tier 3 Die:"), 0, row); grid.add(tier3Combo, 1, row++);
        grid.add(new Label("Stat Type:"), 0, row); grid.add(statCombo, 1, row++);
        grid.add(new Label("Ammo Type:"), 0, row); grid.add(ammoTypeField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("INT Mod:"), 0, row); grid.add(intField, 1, row++);
        grid.add(abilityEditor, 0, row++, 2, 1);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            String[] damageDice = {tier1Combo.getValue(), tier2Combo.getValue(), tier3Combo.getValue()};
            String statType = statCombo.getValue();
            String ammoType = ammoTypeField.getText().trim();
            int[] newAttrs = {parseInt(strField), parseInt(dexField), parseInt(mobField), parseInt(intField)};
            if (!name.isEmpty()) {
                weapon.setName(name);
                weapon.setDamageDice(damageDice);
                weapon.setStatType(statType);
                weapon.setAmmoType(ammoType.isEmpty() ? null : ammoType);
                weapon.setModifiedAttributes(newAttrs);
                weapon.setAbilities(weaponAbilities);
                ItemDatabase.getInstance().saveItem(weapon);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 400, 650));
        dialog.showAndWait();
    }

    private void editAccessory(Accessory accessory) {
        Stage dialog = createDialog("Edit Accessory", 380, 500);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(accessory.getName());
        TextField defenseField = new TextField(String.valueOf(accessory.getDefense()));
        int[] attrs = accessory.getModifiedAttributes();
        TextField strField = new TextField(String.valueOf(attrs[0]));
        TextField dexField = new TextField(String.valueOf(attrs[1]));
        TextField mobField = new TextField(String.valueOf(attrs[2]));
        TextField intField = new TextField(String.valueOf(attrs.length > 3 ? attrs[3] : 0));
        
        // Abilities list - copy existing abilities
        ArrayList<ItemAbility> accessoryAbilities = new ArrayList<>(accessory.getAbilities());
        VBox abilityEditor = createAbilityEditorSection(accessoryAbilities);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Defense:"), 0, row); grid.add(defenseField, 1, row++);
        grid.add(new Label("STR Mod:"), 0, row); grid.add(strField, 1, row++);
        grid.add(new Label("DEX Mod:"), 0, row); grid.add(dexField, 1, row++);
        grid.add(new Label("MOB Mod:"), 0, row); grid.add(mobField, 1, row++);
        grid.add(new Label("INT Mod:"), 0, row); grid.add(intField, 1, row++);
        grid.add(abilityEditor, 0, row++, 2, 1);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            int defense = Integer.parseInt(defenseField.getText());
            int[] newAttrs = {parseInt(strField), parseInt(dexField), parseInt(mobField), parseInt(intField)};
            if (!name.isEmpty()) {
                accessory.setName(name);
                accessory.setDefense(defense);
                accessory.setModifiedAttributes(newAttrs);
                accessory.setAbilities(accessoryAbilities);
                ItemDatabase.getInstance().saveItem(accessory);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 380, 500));
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
        Stage dialog = createDialog("Edit Ammunition", 320, 150);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(item.getName());
        TextField ammoTypeField = new TextField(item.getAmmoType());
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Ammo Type:"), 0, row); grid.add(ammoTypeField, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                item.setName(name);
                item.setAmmoType(ammoTypeField.getText());
                ItemDatabase.getInstance().saveItem(item);
                return true;
            }
            return false;
        }, this::refreshItemDisplay);
        
        dialog.setScene(createDialogScene(grid, 320, 150));
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
        Stage dialog = createDialog("Add Terrain", 300, 250);
        GridPane grid = createDialogGrid();
        
        TextField typeField = new TextField();
        TextField hpField = new TextField("10");
        CheckBox blocksCheck = new CheckBox("Blocks Movement");
        blocksCheck.setSelected(true);
        
        // Sprite picker
        final String[] spritePath = { null };
        javafx.scene.layout.HBox spritePicker = SpritePickerUtils.createCompactSpritePicker(
            null,
            "terrain",
            0,
            false,
            newPath -> spritePath[0] = newPath,
            dialog
        );
        
        int row = 0;
        grid.add(new Label("Type:"), 0, row); grid.add(typeField, 1, row++);
        grid.add(new Label("HP:"), 0, row); grid.add(hpField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(blocksCheck, 1, row++);
        grid.add(new Label("Sprite:"), 0, row); grid.add(spritePicker, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            String type = typeField.getText();
            int hp = Integer.parseInt(hpField.getText());
            boolean blocks = blocksCheck.isSelected();
            if (!type.isEmpty()) {
                TerrainObject newTerrain = new TerrainObject(0, 0, type, hp, 0, blocks);
                newTerrain.setSpritePath(spritePath[0]);
                TerrainDatabase.getInstance().addTerrain(newTerrain);
                return true;
            }
            return false;
        }, this::refreshTerrainDisplay);
        
        dialog.setScene(createDialogScene(grid, 300, 250));
        dialog.showAndWait();
    }

    private void editTerrain(TerrainObject terrain) {
        Stage dialog = createDialog("Edit Terrain", 300, 250);
        GridPane grid = createDialogGrid();
        
        TextField typeField = new TextField(terrain.getType());
        TextField hpField = new TextField(String.valueOf(terrain.getHealth()));
        CheckBox blocksCheck = new CheckBox("Blocks Movement");
        blocksCheck.setSelected(terrain.blocksMovement());
        
        // Sprite picker
        final String[] spritePath = { terrain.getSpritePath() };
        javafx.scene.layout.HBox spritePicker = SpritePickerUtils.createCompactSpritePicker(
            spritePath[0],
            "terrain",
            terrain.getColor(),
            false,
            newPath -> spritePath[0] = newPath,
            dialog
        );
        
        int row = 0;
        grid.add(new Label("Type:"), 0, row); grid.add(typeField, 1, row++);
        grid.add(new Label("HP:"), 0, row); grid.add(hpField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(blocksCheck, 1, row++);
        grid.add(new Label("Sprite:"), 0, row); grid.add(spritePicker, 1, row++);
        
        addDialogButtons(grid, row, dialog, () -> {
            terrain.setBlocksMovement(blocksCheck.isSelected());
            terrain.setSpritePath(spritePath[0]);
            TerrainDatabase.getInstance().saveTerrain(terrain);
            return true;
        }, this::refreshTerrainDisplay);
        
        dialog.setScene(createDialogScene(grid, 300, 250));
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

    /**
     * Creates an ability editor section with a list of abilities and add/edit/remove buttons.
     * @param abilities The list of abilities to edit (will be modified in place)
     * @return A VBox containing the ability editor UI
     */
    private VBox createAbilityEditorSection(ArrayList<ItemAbility> abilities) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(5));
        
        Label header = new Label("Abilities");
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #dcdcaa;");
        
        ListView<ItemAbility> abilityList = new ListView<>();
        abilityList.setPrefHeight(100);
        abilityList.getItems().addAll(abilities);
        abilityList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ItemAbility item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        
        HBox buttons = new HBox(5);
        Button addBtn = new Button("+");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> {
            showAbilityDialog(null, ability -> {
                abilities.add(ability);
                abilityList.getItems().add(ability);
            });
        });
        
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> {
            ItemAbility selected = abilityList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int index = abilities.indexOf(selected);
                showAbilityDialog(selected, ability -> {
                    abilities.set(index, ability);
                    abilityList.getItems().set(index, ability);
                });
            }
        });
        
        Button removeBtn = new Button("-");
        removeBtn.getStyleClass().add("button");
        removeBtn.setOnAction(e -> {
            ItemAbility selected = abilityList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                abilities.remove(selected);
                abilityList.getItems().remove(selected);
            }
        });
        
        buttons.getChildren().addAll(addBtn, editBtn, removeBtn);
        container.getChildren().addAll(header, abilityList, buttons);
        
        return container;
    }
    
    /**
     * Shows a dialog to create or edit an ability.
     * @param existing The existing ability to edit, or null for a new ability
     * @param onSave Called when the ability is saved
     */
    private void showAbilityDialog(ItemAbility existing, java.util.function.Consumer<ItemAbility> onSave) {
        Stage dialog = createDialog(existing == null ? "Add Ability" : "Edit Ability", 350, 350);
        GridPane grid = createDialogGrid();
        
        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        TextField descField = new TextField(existing != null ? existing.getDescription() : "");
        
        ComboBox<String> triggerCombo = new ComboBox<>();
        triggerCombo.getItems().addAll(ItemAbility.TRIGGER_TYPES);
        triggerCombo.getSelectionModel().select(existing != null ? existing.getTriggerType() : ItemAbility.ON_TURN_START);
        
        ComboBox<String> effectCombo = new ComboBox<>();
        effectCombo.getItems().addAll(ItemAbility.EFFECT_TYPES);
        effectCombo.getSelectionModel().select(existing != null ? existing.getEffectType() : ItemAbility.EFFECT_HEAL);
        
        Spinner<Integer> magnitudeSpinner = new Spinner<>(-100, 100, existing != null ? existing.getMagnitude() : 1);
        magnitudeSpinner.setEditable(true);
        FormUtils.styleSpinner(magnitudeSpinner);
        
        ComboBox<String> targetAttrCombo = new ComboBox<>();
        targetAttrCombo.getItems().addAll("STR", "DEX", "MOB", "INT");
        targetAttrCombo.getSelectionModel().select(existing != null ? existing.getTargetAttribute() : 0);
        
        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Trigger:"), 0, row); grid.add(triggerCombo, 1, row++);
        grid.add(new Label("Effect:"), 0, row); grid.add(effectCombo, 1, row++);
        grid.add(new Label("Magnitude:"), 0, row); grid.add(magnitudeSpinner, 1, row++);
        grid.add(new Label("Target Attr:"), 0, row); grid.add(targetAttrCombo, 1, row++);
        
        HBox btnBox = new HBox(10);
        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("button-primary");
        okBtn.setOnAction(e -> {
            String name = nameField.getText().isEmpty() ? "Unnamed" : nameField.getText();
            ItemAbility ability = new ItemAbility(
                name,
                descField.getText(),
                triggerCombo.getValue(),
                effectCombo.getValue(),
                magnitudeSpinner.getValue(),
                targetAttrCombo.getSelectionModel().getSelectedIndex(),
                null
            );
            onSave.accept(ability);
            dialog.close();
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setOnAction(e -> dialog.close());
        
        btnBox.getChildren().addAll(okBtn, cancelBtn);
        grid.add(btnBox, 0, row);
        GridPane.setColumnSpan(btnBox, 2);
        
        dialog.setScene(createDialogScene(grid, 350, 350));
        dialog.showAndWait();
    }

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
        dialog.initOwner(appController.getPrimaryStage());
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
        appController.navigateToMainMenu();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
