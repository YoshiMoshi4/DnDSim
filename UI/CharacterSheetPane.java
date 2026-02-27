package UI;

import EntityRes.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class CharacterSheetPane extends BorderPane {

    private CharSheet sheet;
    private boolean updatingDisplay = false;
    
    private TextField nameField;
    private TextField classField;
    private Spinner<Integer> currentHpSpinner;
    private Spinner<Integer> maxHpSpinner;
    private ProgressBar hpBar;
    private ComboBox<String> statusCombo;
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
    
    private TextArea consumablesTab;
    private TextArea craftingTab;
    private TextArea keyItemsTab;

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
        
        // Status dropdown
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("form-label");
        grid.add(statusLabel, 0, row);
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Status.AVAILABLE_STATUSES);
        statusCombo.setPrefWidth(150);
        statusCombo.getStyleClass().add("styled-combo-box");
        statusCombo.setOnAction(e -> {
            String selected = statusCombo.getValue();
            sheet.clearStatus();
            if (selected != null && !selected.equals("Neutral")) {
                sheet.addStatus(new Status(selected));
            }
            sheet.save();
        });
        grid.add(statusCombo, 1, row++);
        
        // Color
        Label colorLabel = new Label("Color:");
        colorLabel.getStyleClass().add("form-label");
        grid.add(colorLabel, 0, row);
        colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(CharSheet.getColorNames());
        colorCombo.setPrefWidth(150);
        colorCombo.getStyleClass().add("styled-combo-box");
        colorCombo.setOnAction(e -> {
            sheet.setColor(colorCombo.getSelectionModel().getSelectedIndex());
            sheet.save();
        });
        grid.add(colorCombo, 1, row++);
        
        TitledPane pane = new TitledPane("Basic Info", grid);
        pane.getStyleClass().add("form-section");
        pane.setCollapsible(false);
        return pane;
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
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefHeight(200);
        tabs.getStyleClass().add("styled-tab-pane");
        
        consumablesTab = new TextArea();
        consumablesTab.setEditable(false);
        consumablesTab.getStyleClass().add("styled-text-area");
        Tab consumables = new Tab("Consumables/Ammo", new ScrollPane(consumablesTab));
        
        craftingTab = new TextArea();
        craftingTab.setEditable(false);
        craftingTab.getStyleClass().add("styled-text-area");
        Tab crafting = new Tab("Crafting", new ScrollPane(craftingTab));
        
        keyItemsTab = new TextArea();
        keyItemsTab.setEditable(false);
        keyItemsTab.getStyleClass().add("styled-text-area");
        Tab keyItems = new Tab("Key Items", new ScrollPane(keyItemsTab));
        
        tabs.getTabs().addAll(consumables, crafting, keyItems);
        
        TitledPane pane = new TitledPane("Inventory", tabs);
        pane.getStyleClass().add("form-section");
        pane.setCollapsible(false);
        return pane;
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
        
        // Status dropdown
        Status[] statuses = sheet.getStatus();
        if (statuses.length == 0) {
            statusCombo.getSelectionModel().select("Neutral");
        } else {
            // Select the first status (dropdown only supports one)
            statusCombo.getSelectionModel().select(statuses[0].getName());
        }
        
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
        
        // Inventory
        StringBuilder consumables = new StringBuilder();
        StringBuilder crafting = new StringBuilder();
        StringBuilder keyItems = new StringBuilder();
        
        for (Item item : sheet.getInventory()) {
            String entry = item.getName() + " (" + item.getQuantity() + ")\n";
            if (item instanceof Consumable || item instanceof Ammunition) {
                consumables.append(entry);
            } else if (item instanceof CraftingItem) {
                crafting.append(entry);
            } else if (item instanceof KeyItem) {
                keyItems.append(entry);
            }
        }
        
        consumablesTab.setText(consumables.toString());
        craftingTab.setText(crafting.toString());
        keyItemsTab.setText(keyItems.toString());
        } finally {
            updatingDisplay = false;
        }
    }

    private void updatePrimary() {
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

    private void save() {
        sheet.save();
        updateDisplay();
    }

    public CharSheet getCharSheet() {
        return sheet;
    }
}
