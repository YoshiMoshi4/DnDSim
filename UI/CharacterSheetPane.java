package UI;

import EntityRes.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class CharacterSheetPane extends BorderPane {

    private CharSheet sheet;
    private boolean updatingDisplay = false;
    
    private TextField nameField;
    private TextField classField;
    private Spinner<Integer> currentHpSpinner;
    private Spinner<Integer> maxHpSpinner;
    private ProgressBar hpBar;
    private MenuButton statusMenu;
    private Map<String, CheckMenuItem> statusMenuItems;
    private ComboBox<String> colorCombo;
    
    private ComboBox<String> primaryWeapon;
    private ComboBox<String> secondaryWeapon;
    private ComboBox<String> headArmor;
    private ComboBox<String> torsoArmor;
    private ComboBox<String> legsArmor;
    
    private Label strBase, strTemp, strTotal;
    private Label dexBase, dexTemp, dexTotal;
    private Label itvBase, itvTemp, itvTotal;
    private Label mobBase, mobTemp, mobTotal;
    
    private ListView<Item> consumablesList;
    private ListView<Item> craftingList;
    private ListView<Item> keyItemsList;
    private ObservableList<Item> consumablesData;
    private ObservableList<Item> craftingData;
    private ObservableList<Item> keyItemsData;

    public CharacterSheetPane(CharSheet sheet) {
        this.sheet = sheet;
        getStyleClass().addAll("card");
        setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d30, #252528);");
        setPadding(new Insets(15));
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        content.getChildren().addAll(
            createBasicInfoSection(),
            createEquipmentSection(),
            createAttributesSection(),
            createInventorySection()
        );
        
        scrollPane.setContent(content);
        setCenter(scrollPane);
        
        updateDisplay();
    }

    private TitledPane createBasicInfoSection() {
        HBox mainBox = new HBox(20);
        mainBox.setAlignment(Pos.TOP_LEFT);
        
        // Sprite display on the left
        VBox spriteBox = createSpriteSection();
        
        // Form fields on the right
        GridPane grid = FormUtils.createFormGrid(2);
        
        int row = 0;
        
        // Name
        Label nameLabel = new Label("Name:");
        nameLabel.getStyleClass().add("form-label");
        grid.add(nameLabel, 0, row);
        nameField = FormUtils.createStyledTextField("Character Name", null, 150);
        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) { sheet.setName(nameField.getText()); save(); }
        });
        grid.add(nameField, 1, row);
        
        // Class
        Label classLabel = new Label("Class:");
        classLabel.getStyleClass().add("form-label");
        grid.add(classLabel, 2, row);
        classField = FormUtils.createStyledTextField("Fighter, Mage...", null, 150);
        classField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) { sheet.setCharacterClass(classField.getText()); save(); }
        });
        grid.add(classField, 3, row++);
        
        // HP with spinners
        Label hpLabel = new Label("HP:");
        hpLabel.getStyleClass().add("form-label");
        grid.add(hpLabel, 0, row);
        HBox hpBox = new HBox(5);
        hpBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        currentHpSpinner = new Spinner<>(0, 9999, 0);
        currentHpSpinner.setEditable(true);
        currentHpSpinner.setPrefWidth(80);
        currentHpSpinner.getStyleClass().add("styled-spinner-fx");
        FormUtils.styleSpinner(currentHpSpinner);
        currentHpSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sheet.setCurrentHP(newVal);
                updateHpBar();
                sheet.save();
            }
        });
        
        Label slashLabel = new Label("/");
        slashLabel.setStyle("-fx-text-fill: #aaa; -fx-font-weight: bold;");
        
        maxHpSpinner = new Spinner<>(1, 9999, 1);
        maxHpSpinner.setEditable(true);
        maxHpSpinner.setPrefWidth(80);
        maxHpSpinner.getStyleClass().add("styled-spinner-fx");
        FormUtils.styleSpinner(maxHpSpinner);
        maxHpSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sheet.setTotalHP(newVal);
                if (sheet.getCurrentHP() > newVal) {
                    sheet.setCurrentHP(newVal);
                    currentHpSpinner.getValueFactory().setValue(newVal);
                }
                updateHpBar();
                sheet.save();
            }
        });
        
        hpBox.getChildren().addAll(currentHpSpinner, slashLabel, maxHpSpinner);
        grid.add(hpBox, 1, row);
        
        // HP bar
        hpBar = new ProgressBar(1.0);
        hpBar.setPrefWidth(150);
        hpBar.getStyleClass().add("hp-bar");
        hpBar.setStyle("-fx-accent: #4CAF50;");
        grid.add(hpBar, 2, row);
        GridPane.setColumnSpan(hpBar, 2);
        row++;
        
        // Status menu (supports multiple via checkable menu items)
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("form-label");
        grid.add(statusLabel, 0, row);
        
        statusMenu = new MenuButton("None");
        statusMenu.setPrefWidth(150);
        statusMenu.getStyleClass().add("styled-combo-box");
        statusMenuItems = new HashMap<>();
        
        for (String statusName : Status.AVAILABLE_STATUSES) {
            if (statusName.equals("Neutral")) continue; // Skip neutral - no selection means neutral
            
            CheckMenuItem item = new CheckMenuItem(statusName);
            item.setOnAction(e -> {
                if (updatingDisplay) return;
                syncStatusesToSheet();
                updateStatusMenuText();
            });
            statusMenuItems.put(statusName, item);
            statusMenu.getItems().add(item);
        }
        grid.add(statusMenu, 1, row++);
        
        // Color
        Label colorLabel = new Label("Color:");
        colorLabel.getStyleClass().add("form-label");
        grid.add(colorLabel, 0, row);
        colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(CharSheet.getColorNames());
        colorCombo.setPrefWidth(150);
        colorCombo.getStyleClass().add("styled-combo-box");
        colorCombo.setOnAction(e -> {
            if (updatingDisplay) return;
            sheet.setColor(colorCombo.getSelectionModel().getSelectedIndex());
            sheet.save();
        });
        grid.add(colorCombo, 1, row++);
        
        // Combine sprite and form
        mainBox.getChildren().addAll(spriteBox, grid);
        
        TitledPane pane = new TitledPane("Basic Info", mainBox);
        pane.getStyleClass().add("form-section");
        pane.setCollapsible(false);
        return pane;
    }
    
    /**
     * Create the sprite display section with the character's sprite or fallback avatar.
     * Uses SpritePickerUtils for browse button and auto-copy functionality.
     */
    private VBox createSpriteSection() {
        // Use the sprite picker utility with auto-copy to sprites/party/
        return SpritePickerUtils.createSpritePicker(
            sheet.getSpritePath(),
            "party",
            sheet.getColor(),
            true, // isParty
            newPath -> sheet.setSpritePath(newPath),
            () -> getScene() != null ? getScene().getWindow() : null
        );
    }
    
    private void updateHpBar() {
        double ratio = (double) sheet.getCurrentHP() / sheet.getTotalHP();
        double targetProgress = Math.max(0, Math.min(1, ratio));
        
        // Animate the progress bar change
        AnimationUtils.animateProgressBar(hpBar, targetProgress);
        
        // Change color based on HP
        if (ratio > 0.5) {
            hpBar.setStyle("-fx-accent: #4CAF50;"); // Green
        } else if (ratio > 0.25) {
            hpBar.setStyle("-fx-accent: #FF9800;"); // Orange
        } else {
            hpBar.setStyle("-fx-accent: #F44336;"); // Red
        }
    }

    private TitledPane createEquipmentSection() {
        GridPane grid = FormUtils.createFormGrid(2);
        
        int row = 0;
        
        // Weapons
        Label primaryLabel = new Label("Primary:");
        primaryLabel.getStyleClass().add("form-label");
        grid.add(primaryLabel, 0, row);
        primaryWeapon = new ComboBox<>();
        primaryWeapon.setPrefWidth(180);
        primaryWeapon.getStyleClass().add("styled-combo-box");
        primaryWeapon.setOnAction(e -> updatePrimary());
        grid.add(primaryWeapon, 1, row++);
        
        Label secondaryLabel = new Label("Secondary:");
        secondaryLabel.getStyleClass().add("form-label");
        grid.add(secondaryLabel, 0, row);
        secondaryWeapon = new ComboBox<>();
        secondaryWeapon.setPrefWidth(180);
        secondaryWeapon.getStyleClass().add("styled-combo-box");
        secondaryWeapon.setOnAction(e -> updateSecondary());
        grid.add(secondaryWeapon, 1, row++);
        
        // Armor
        Label headLabel = new Label("Head:");
        headLabel.getStyleClass().add("form-label");
        grid.add(headLabel, 2, 0);
        headArmor = new ComboBox<>();
        headArmor.setPrefWidth(180);
        headArmor.getStyleClass().add("styled-combo-box");
        headArmor.setOnAction(e -> updateHead());
        grid.add(headArmor, 3, 0);
        
        Label torsoLabel = new Label("Torso:");
        torsoLabel.getStyleClass().add("form-label");
        grid.add(torsoLabel, 2, 1);
        torsoArmor = new ComboBox<>();
        torsoArmor.setPrefWidth(180);
        torsoArmor.getStyleClass().add("styled-combo-box");
        torsoArmor.setOnAction(e -> updateTorso());
        grid.add(torsoArmor, 3, 1);
        
        Label legsLabel = new Label("Legs:");
        legsLabel.getStyleClass().add("form-label");
        grid.add(legsLabel, 2, 2);
        legsArmor = new ComboBox<>();
        legsArmor.setPrefWidth(180);
        legsArmor.getStyleClass().add("styled-combo-box");
        legsArmor.setOnAction(e -> updateLegs());
        grid.add(legsArmor, 3, 2);
        
        TitledPane pane = new TitledPane("Equipment", grid);
        pane.getStyleClass().add("form-section");
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createAttributesSection() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));
        grid.setStyle("-fx-background-color: transparent;");
        
        // Headers
        grid.add(new Label(""), 0, 0);
        Label baseHeader = new Label("Base");
        baseHeader.getStyleClass().add("label-header");
        baseHeader.setStyle("-fx-text-fill: #569cd6; -fx-font-weight: bold;");
        grid.add(baseHeader, 1, 0);
        Label tempHeader = new Label("Bonus");
        tempHeader.getStyleClass().add("label-header");
        tempHeader.setStyle("-fx-text-fill: #4ec9b0; -fx-font-weight: bold;");
        grid.add(tempHeader, 2, 0);
        Label totalHeader = new Label("Total");
        totalHeader.getStyleClass().add("label-header");
        totalHeader.setStyle("-fx-text-fill: #dcdcaa; -fx-font-weight: bold;");
        grid.add(totalHeader, 3, 0);
        
        // STR
        Label strLabel = new Label("STR");
        strLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #d75f5f;");
        grid.add(strLabel, 0, 1);
        strBase = new Label(); strBase.setMinWidth(40); grid.add(strBase, 1, 1);
        strTemp = new Label(); strTemp.setMinWidth(40); grid.add(strTemp, 2, 1);
        strTotal = new Label(); strTotal.setMinWidth(40); strTotal.setStyle("-fx-font-weight: bold;"); grid.add(strTotal, 3, 1);
        
        // DEX
        Label dexLabel = new Label("DEX");
        dexLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        grid.add(dexLabel, 0, 2);
        dexBase = new Label(); dexBase.setMinWidth(40); grid.add(dexBase, 1, 2);
        dexTemp = new Label(); dexTemp.setMinWidth(40); grid.add(dexTemp, 2, 2);
        dexTotal = new Label(); dexTotal.setMinWidth(40); dexTotal.setStyle("-fx-font-weight: bold;"); grid.add(dexTotal, 3, 2);
        
        // ITV
        Label itvLabel = new Label("ITV");
        itvLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #9c27b0;");
        grid.add(itvLabel, 0, 3);
        itvBase = new Label(); itvBase.setMinWidth(40); grid.add(itvBase, 1, 3);
        itvTemp = new Label(); itvTemp.setMinWidth(40); grid.add(itvTemp, 2, 3);
        itvTotal = new Label(); itvTotal.setMinWidth(40); itvTotal.setStyle("-fx-font-weight: bold;"); grid.add(itvTotal, 3, 3);
        
        // MOB
        Label mobLabel = new Label("MOB");
        mobLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
        grid.add(mobLabel, 0, 4);
        mobBase = new Label(); mobBase.setMinWidth(40); grid.add(mobBase, 1, 4);
        mobTemp = new Label(); mobTemp.setMinWidth(40); grid.add(mobTemp, 2, 4);
        mobTotal = new Label(); mobTotal.setMinWidth(40); mobTotal.setStyle("-fx-font-weight: bold;"); grid.add(mobTotal, 3, 4);
        
        TitledPane pane = new TitledPane("Attributes", grid);
        pane.getStyleClass().add("form-section");
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createInventorySection() {
        VBox inventoryBox = new VBox(10);
        
        // Add Item button at top
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        Button addItemBtn = new Button("+ Add Item");
        addItemBtn.getStyleClass().add("styled-button");
        addItemBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addItemBtn.setOnAction(e -> showAddItemDialog());
        buttonBar.getChildren().add(addItemBtn);
        
        // Tab pane for item categories
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefHeight(180);
        tabs.getStyleClass().add("styled-tab-pane");
        
        // Consumables/Ammo tab
        consumablesData = FXCollections.observableArrayList();
        consumablesList = createItemListView(consumablesData);
        Tab consumablesTab = new Tab("Consumables/Ammo", consumablesList);
        
        // Crafting tab
        craftingData = FXCollections.observableArrayList();
        craftingList = createItemListView(craftingData);
        Tab craftingTab = new Tab("Crafting", craftingList);
        
        // Key Items tab
        keyItemsData = FXCollections.observableArrayList();
        keyItemsList = createItemListView(keyItemsData);
        Tab keyItemsTab = new Tab("Key Items", keyItemsList);
        
        tabs.getTabs().addAll(consumablesTab, craftingTab, keyItemsTab);
        
        inventoryBox.getChildren().addAll(buttonBar, tabs);
        
        TitledPane pane = new TitledPane("Inventory", inventoryBox);
        pane.getStyleClass().add("form-section");
        pane.setCollapsible(false);
        return pane;
    }

    private ListView<Item> createItemListView(ObservableList<Item> items) {
        ListView<Item> listView = new ListView<>(items);
        listView.setStyle("-fx-background-color: #2d2d30; -fx-background: #2d2d30;");
        listView.setPrefHeight(150);
        listView.setPlaceholder(new Label("No items"));
        
        listView.setCellFactory(lv -> new ListCell<Item>() {
            private final HBox container = new HBox(10);
            private final Label nameLabel = new Label();
            private final Label quantityLabel = new Label();
            private final Region spacer = new Region();
            private final Button removeBtn = new Button("×");
            
            {
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(5, 10, 5, 10));
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                nameLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold;");
                quantityLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
                
                removeBtn.setStyle("-fx-background-color: #d75f5f; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    Item item = getItem();
                    if (item != null) {
                        showRemoveItemDialog(item);
                    }
                });
                
                container.getChildren().addAll(nameLabel, quantityLabel, spacer, removeBtn);
            }
            
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    nameLabel.setText(item.getName());
                    quantityLabel.setText("×" + item.getQuantity());
                    setGraphic(container);
                    setStyle("-fx-background-color: #383838; -fx-background-radius: 5;");
                }
            }
        });
        
        return listView;
    }

    private void showAddItemDialog() {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Add Item");
        dialog.setHeaderText("Select an item to add to inventory");
        dialog.getDialogPane().getStylesheets().addAll(getScene().getStylesheets());
        dialog.getDialogPane().setStyle("-fx-background-color: #2d2d30;");
        
        // Category selection
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Consumable", "Ammunition", "Crafting Item", "Key Item");
        categoryCombo.setPromptText("Select category");
        categoryCombo.setPrefWidth(200);
        categoryCombo.getStyleClass().add("styled-combo-box");
        
        // Item selection
        ComboBox<String> itemCombo = new ComboBox<>();
        itemCombo.setPromptText("Select item");
        itemCombo.setPrefWidth(200);
        itemCombo.getStyleClass().add("styled-combo-box");
        itemCombo.setDisable(true);
        
        // Quantity spinner
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 999, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(100);
        FormUtils.styleSpinner(quantitySpinner);
        
        // Update item list when category changes
        categoryCombo.setOnAction(e -> {
            itemCombo.getItems().clear();
            ItemDatabase db = ItemDatabase.getInstance();
            String category = categoryCombo.getValue();
            if (category == null) return;
            
            switch (category) {
                case "Consumable":
                    itemCombo.getItems().addAll(db.getAllConsumables().keySet());
                    break;
                case "Ammunition":
                    itemCombo.getItems().addAll(db.getAllAmmunition().keySet());
                    break;
                case "Crafting Item":
                    itemCombo.getItems().addAll(db.getAllCraftingItems().keySet());
                    break;
                case "Key Item":
                    itemCombo.getItems().addAll(db.getAllKeyItems().keySet());
                    break;
            }
            itemCombo.setDisable(itemCombo.getItems().isEmpty());
        });
        
        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        Label catLabel = new Label("Category:");
        catLabel.setStyle("-fx-text-fill: #e0e0e0;");
        grid.add(catLabel, 0, 0);
        grid.add(categoryCombo, 1, 0);
        
        Label itemLabel = new Label("Item:");
        itemLabel.setStyle("-fx-text-fill: #e0e0e0;");
        grid.add(itemLabel, 0, 1);
        grid.add(itemCombo, 1, 1);
        
        Label qtyLabel = new Label("Quantity:");
        qtyLabel.setStyle("-fx-text-fill: #e0e0e0;");
        grid.add(qtyLabel, 0, 2);
        grid.add(quantitySpinner, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);
        
        // Disable Add button until item is selected
        Node addBtn = dialog.getDialogPane().lookupButton(addButton);
        addBtn.setDisable(true);
        itemCombo.valueProperty().addListener((obs, old, val) -> addBtn.setDisable(val == null));
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == addButton) {
                String category = categoryCombo.getValue();
                String itemName = itemCombo.getValue();
                int quantity = quantitySpinner.getValue();
                ItemDatabase db = ItemDatabase.getInstance();
                
                Item newItem = null;
                switch (category) {
                    case "Consumable":
                        Consumable c = db.getConsumable(itemName);
                        if (c != null) newItem = new Consumable(c.getName(), c.getType(), c.getColor(), c.getHealAmount(), c.getEffect());
                        break;
                    case "Ammunition":
                        Ammunition a = db.getAmmunition(itemName);
                        if (a != null) newItem = new Ammunition(a.getName(), a.getType(), a.getColor(), a.getDamageBonus(), a.getCompatibleWeaponType());
                        break;
                    case "Crafting Item":
                        CraftingItem cr = db.getCraftingItem(itemName);
                        if (cr != null) newItem = new CraftingItem(cr.getName(), cr.getType(), cr.getColor(), cr.getCraftingCategory(), cr.getDescription());
                        break;
                    case "Key Item":
                        KeyItem k = db.getKeyItem(itemName);
                        if (k != null) newItem = new KeyItem(k.getName(), k.getType(), k.getColor(), k.getDescription(), k.isQuestRelated());
                        break;
                }
                if (newItem != null) {
                    newItem.setQuantity(quantity);
                }
                return newItem;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(item -> {
            sheet.addItem(item);
            updateDisplay();
        });
    }

    private void showRemoveItemDialog(Item item) {
        if (item.getQuantity() == 1) {
            // Just remove the item directly
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Remove Item");
            confirm.setHeaderText("Remove " + item.getName() + "?");
            confirm.setContentText("This will remove the item from inventory.");
            confirm.getDialogPane().setStyle("-fx-background-color: #2d2d30;");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    sheet.removeItem(item);
                    updateDisplay();
                }
            });
        } else {
            // Show dialog to choose quantity to remove
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Remove Item");
            dialog.setHeaderText("Remove " + item.getName());
            dialog.getDialogPane().setStyle("-fx-background-color: #2d2d30;");
            
            Spinner<Integer> qtySpinner = new Spinner<>(1, item.getQuantity(), 1);
            qtySpinner.setEditable(true);
            qtySpinner.setPrefWidth(100);
            FormUtils.styleSpinner(qtySpinner);
            
            Button removeAllBtn = new Button("Remove All");
            removeAllBtn.setStyle("-fx-background-color: #d75f5f; -fx-text-fill: white;");
            removeAllBtn.setOnAction(e -> qtySpinner.getValueFactory().setValue(item.getQuantity()));
            
            HBox content = new HBox(15);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(20));
            Label qtyLabel = new Label("Quantity to remove:");
            qtyLabel.setStyle("-fx-text-fill: #e0e0e0;");
            content.getChildren().addAll(qtyLabel, qtySpinner, removeAllBtn);
            
            dialog.getDialogPane().setContent(content);
            
            ButtonType removeButton = new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(removeButton, ButtonType.CANCEL);
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == removeButton) {
                    return qtySpinner.getValue();
                }
                return null;
            });
            
            dialog.showAndWait().ifPresent(qty -> {
                sheet.removeItemQuantity(item, qty);
                updateDisplay();
            });
        }
    }

    public void updateDisplay() {
        if (updatingDisplay) return;
        updatingDisplay = true;
        try {
            nameField.setText(sheet.getName());
            classField.setText(sheet.getCharacterClass() != null ? sheet.getCharacterClass() : "");
            colorCombo.getSelectionModel().select(sheet.getColor());
        
        // Update HP spinners without triggering listeners
        currentHpSpinner.getValueFactory().setValue(sheet.getCurrentHP());
        maxHpSpinner.getValueFactory().setValue(sheet.getTotalHP());
        updateHpBar();
        
        // Status menu items - check items for current statuses
        Status[] statuses = sheet.getStatus();
        java.util.Set<String> activeStatuses = new java.util.HashSet<>();
        for (Status s : statuses) {
            activeStatuses.add(s.getName());
        }
        for (Map.Entry<String, CheckMenuItem> entry : statusMenuItems.entrySet()) {
            entry.getValue().setSelected(activeStatuses.contains(entry.getKey()));
        }
        updateStatusMenuText();
        
        // Update weapon combos
        primaryWeapon.getItems().clear();
        secondaryWeapon.getItems().clear();
        primaryWeapon.getItems().add("None");
        secondaryWeapon.getItems().add("None");
        for (Item item : sheet.getInventory()) {
            if (item instanceof Weapon) {
                primaryWeapon.getItems().add(item.getName());
                secondaryWeapon.getItems().add(item.getName());
            }
        }
        if (sheet.getEquippedWeapon() != null) {
            primaryWeapon.getSelectionModel().select(sheet.getEquippedWeapon().getName());
        } else {
            primaryWeapon.getSelectionModel().select("None");
        }
        if (sheet.getEquippedSecondary() != null) {
            secondaryWeapon.getSelectionModel().select(sheet.getEquippedSecondary().getName());
        } else {
            secondaryWeapon.getSelectionModel().select("None");
        }
        
        // Update armor combos
        headArmor.getItems().clear();
        torsoArmor.getItems().clear();
        legsArmor.getItems().clear();
        headArmor.getItems().add("None");
        torsoArmor.getItems().add("None");
        legsArmor.getItems().add("None");
        for (Item item : sheet.getInventory()) {
            if (item instanceof Armor armor) {
                if (armor.getArmorType() == 0) headArmor.getItems().add(item.getName());
                else if (armor.getArmorType() == 1) torsoArmor.getItems().add(item.getName());
                else if (armor.getArmorType() == 2) legsArmor.getItems().add(item.getName());
            }
        }
        if (sheet.getHead() != null) {
            headArmor.getSelectionModel().select(sheet.getHead().getName());
        } else {
            headArmor.getSelectionModel().select("None");
        }
        if (sheet.getTorso() != null) {
            torsoArmor.getSelectionModel().select(sheet.getTorso().getName());
        } else {
            torsoArmor.getSelectionModel().select("None");
        }
        if (sheet.getLegs() != null) {
            legsArmor.getSelectionModel().select(sheet.getLegs().getName());
        } else {
            legsArmor.getSelectionModel().select("None");
        }
        
        // Attributes
        strBase.setText(String.valueOf(sheet.getAttribute(0)));
        strTemp.setText(String.valueOf(sheet.getTempAttribute(0)));
        strTotal.setText(String.valueOf(sheet.getTotalAttribute(0)));
        dexBase.setText(String.valueOf(sheet.getAttribute(1)));
        dexTemp.setText(String.valueOf(sheet.getTempAttribute(1)));
        dexTotal.setText(String.valueOf(sheet.getTotalAttribute(1)));
        itvBase.setText(String.valueOf(sheet.getAttribute(2)));
        itvTemp.setText(String.valueOf(sheet.getTempAttribute(2)));
        itvTotal.setText(String.valueOf(sheet.getTotalAttribute(2)));
        mobBase.setText(String.valueOf(sheet.getAttribute(3)));
        mobTemp.setText(String.valueOf(sheet.getTempAttribute(3)));
        mobTotal.setText(String.valueOf(sheet.getTotalAttribute(3)));
        
        // Inventory - populate ListViews
        consumablesData.clear();
        craftingData.clear();
        keyItemsData.clear();
        
        for (Item item : sheet.getInventory()) {
            if (item instanceof Consumable || item instanceof Ammunition) {
                consumablesData.add(item);
            } else if (item instanceof CraftingItem) {
                craftingData.add(item);
            } else if (item instanceof KeyItem) {
                keyItemsData.add(item);
            }
        }
        } finally {
            updatingDisplay = false;
        }
    }

    private void updatePrimary() {
        if (updatingDisplay) return;
        String selected = primaryWeapon.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.equals("None")) {
                sheet.equipPrimaryWeapon(null);
                save();
            } else {
                for (Item item : sheet.getInventory()) {
                    if (item.getName().equals(selected) && item instanceof Weapon) {
                        sheet.equipPrimaryWeapon((Weapon) item);
                        save();
                        break;
                    }
                }
            }
        }
    }

    private void updateSecondary() {
        if (updatingDisplay) return;
        String selected = secondaryWeapon.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.equals("None")) {
                sheet.equipSecondaryWeapon(null);
                save();
            } else {
                for (Item item : sheet.getInventory()) {
                    if (item.getName().equals(selected) && item instanceof Weapon) {
                        sheet.equipSecondaryWeapon((Weapon) item);
                        save();
                        break;
                    }
                }
            }
        }
    }

    private void updateHead() {
        if (updatingDisplay) return;
        String selected = headArmor.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.equals("None")) {
                sheet.equipHead(null);
                save();
            } else {
                for (Item item : sheet.getInventory()) {
                    if (item.getName().equals(selected) && item instanceof Armor) {
                        sheet.equipHead((Armor) item);
                        save();
                        break;
                    }
                }
            }
        }
    }

    private void updateTorso() {
        if (updatingDisplay) return;
        String selected = torsoArmor.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.equals("None")) {
                sheet.equipTorso(null);
                save();
            } else {
                for (Item item : sheet.getInventory()) {
                    if (item.getName().equals(selected) && item instanceof Armor) {
                        sheet.equipTorso((Armor) item);
                        save();
                        break;
                    }
                }
            }
        }
    }

    private void updateLegs() {
        if (updatingDisplay) return;
        String selected = legsArmor.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.equals("None")) {
                sheet.equipLegs(null);
                save();
            } else {
                for (Item item : sheet.getInventory()) {
                    if (item.getName().equals(selected) && item instanceof Armor) {
                        sheet.equipLegs((Armor) item);
                        save();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Sync menu item states to the character sheet's status list
     */
    private void syncStatusesToSheet() {
        sheet.clearStatusWithoutSave();
        for (Map.Entry<String, CheckMenuItem> entry : statusMenuItems.entrySet()) {
            if (entry.getValue().isSelected()) {
                sheet.addStatusWithoutSave(new Status(entry.getKey()));
            }
        }
        sheet.save();
    }

    /**
     * Update the status menu button text to show active statuses
     */
    private void updateStatusMenuText() {
        java.util.List<String> active = new java.util.ArrayList<>();
        for (Map.Entry<String, CheckMenuItem> entry : statusMenuItems.entrySet()) {
            if (entry.getValue().isSelected()) {
                active.add(entry.getKey());
            }
        }
        if (active.isEmpty()) {
            statusMenu.setText("None");
        } else if (active.size() <= 2) {
            statusMenu.setText(String.join(", ", active));
        } else {
            statusMenu.setText(active.get(0) + " +" + (active.size() - 1));
        }
    }

    private void save() {
        sheet.save();
        updateDisplay();
    }

    public CharSheet getCharSheet() {
        return sheet;
    }
}
