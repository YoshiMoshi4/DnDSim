package UI;

import EntityRes.*;
import Objects.Enemy;
import UI.Battle.BattleView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

public class CharacterSheetView {

    private final AppController appController;
    private final BorderPane root;
    private final ArrayList<SheetButton> sheets = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private BattleView battleView;
    private TabPane listTabs;
    private StackPane displayPane;

    public CharacterSheetView(AppController appController) {
        this.appController = appController;
        
        root = new BorderPane();
        root.getStyleClass().add("panel-dark");
        root.setPadding(new Insets(10));
        
        // Top button panel
        HBox buttonPanel = createButtonPanel();
        root.setTop(buttonPanel);
        
        // Main split pane
        SplitPane splitPane = new SplitPane();
        
        // Left: tabbed list of characters
        listTabs = new TabPane();
        listTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        listTabs.setPrefWidth(200);
        
        // Right: sheet display area
        displayPane = new StackPane();
        displayPane.getStyleClass().add("panel");
        
        splitPane.getItems().addAll(listTabs, displayPane);
        splitPane.setDividerPositions(0.25);
        
        root.setCenter(splitPane);
        
        autoLoadSheets();
    }

    public BorderPane getRoot() {
        return root;
    }

    public void refresh() {
        // Reload sheets and enemies when navigating back to this view
        if (!sheets.isEmpty()) {
            showSheet(sheets.get(0));
        }
    }

    private HBox createButtonPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-border-color: #505052; -fx-border-width: 0 0 1 0;");
        
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("button");
        backBtn.setPrefSize(100, 35);
        backBtn.setOnAction(e -> handleBack());
        
        Button newCharBtn = new Button("+ Character");
        newCharBtn.getStyleClass().add("button-primary");
        newCharBtn.setMinWidth(140);
        newCharBtn.setPrefHeight(35);
        newCharBtn.setOnAction(e -> handleNew());
        
        Button newEnemyBtn = new Button("+ Enemy");
        newEnemyBtn.getStyleClass().add("button-primary");
        newEnemyBtn.setMinWidth(120);
        newEnemyBtn.setPrefHeight(35);
        newEnemyBtn.setOnAction(e -> handleNewEnemy());
        
        panel.getChildren().addAll(backBtn, newCharBtn, newEnemyBtn);
        return panel;
    }

    private void autoLoadSheets() {
        loadEntitiesFromFolder("party");
        loadEnemies();
        updateSheetLists();
        
        if (!sheets.isEmpty()) {
            showSheet(sheets.get(0));
        }
    }

    private void loadEntitiesFromFolder(String folder) {
        boolean isParty = folder.equals("party");
        File savesDir = new File("saves/entities/" + folder);
        if (savesDir.exists() && savesDir.isDirectory()) {
            File[] files = savesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String charName = fileName.substring(0, fileName.length() - 5);
                    CharSheet loaded = CharSheet.load(charName, isParty);
                    if (loaded != null) {
                        CharacterSheetPane pane = new CharacterSheetPane(loaded);
                        sheets.add(new SheetButton(pane, loaded.getName()));
                    }
                }
            }
        }
    }

    private void loadEnemies() {
        enemies.clear();
        String[] enemyNames = Enemy.listSavedEnemies();
        for (String name : enemyNames) {
            Enemy enemy = Enemy.load(name);
            if (enemy != null) {
                enemies.add(enemy);
            }
        }
    }

    private void updateSheetLists() {
        listTabs.getTabs().clear();
        
        // Party characters list
        VBox partyList = new VBox(5);
        partyList.setPadding(new Insets(5));
        for (SheetButton sheetBtn : sheets) {
            Button btn = createListButton(sheetBtn);
            partyList.getChildren().add(btn);
        }
        ScrollPane partyScroll = new ScrollPane(partyList);
        partyScroll.setFitToWidth(true);
        Tab partyTab = new Tab("Characters", partyScroll);
        listTabs.getTabs().add(partyTab);
        
        // Enemies list
        VBox enemyList = new VBox(5);
        enemyList.setPadding(new Insets(5));
        for (Enemy enemy : enemies) {
            Button btn = createEnemyListButton(enemy);
            enemyList.getChildren().add(btn);
        }
        ScrollPane enemyScroll = new ScrollPane(enemyList);
        enemyScroll.setFitToWidth(true);
        Tab enemyTab = new Tab("Enemies", enemyScroll);
        listTabs.getTabs().add(enemyTab);
    }

    private Button createListButton(SheetButton sheetBtn) {
        Button btn = new Button(sheetBtn.getText());
        btn.getStyleClass().add("button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showSheet(sheetBtn));
        return btn;
    }

    private Button createEnemyListButton(Enemy enemy) {
        Button btn = new Button(enemy.getName());
        btn.getStyleClass().add("button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showEnemyPane(enemy));
        return btn;
    }

    private void showSheet(SheetButton sheetBtn) {
        displayPane.getChildren().clear();
        sheetBtn.getSheet().updateDisplay();
        displayPane.getChildren().add(sheetBtn.getSheet());
    }

    private void showEnemyPane(Enemy enemy) {
        displayPane.getChildren().clear();
        displayPane.getChildren().add(createEnemyDisplayPane(enemy));
    }

    private Node createEnemyDisplayPane(Enemy enemy) {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        pane.getStyleClass().add("card");
        
        // Header with color indicator
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Color swatch
        javafx.scene.shape.Rectangle colorSwatch = new javafx.scene.shape.Rectangle(30, 30);
        String colorHex = CharSheet.getColorHex(enemy.getColor());
        colorSwatch.setFill(javafx.scene.paint.Color.web(colorHex));
        colorSwatch.setArcWidth(6);
        colorSwatch.setArcHeight(6);
        colorSwatch.setStroke(javafx.scene.paint.Color.web(brightenColor(colorHex, 30)));
        colorSwatch.setStrokeWidth(2);
        
        Label nameLabel = new Label(enemy.getName());
        nameLabel.getStyleClass().add("label-title");
        nameLabel.setStyle("-fx-font-size: 28px;");
        
        header.getChildren().addAll(colorSwatch, nameLabel);
        
        // Stats grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(12);
        statsGrid.setPadding(new Insets(15, 0, 15, 0));
        
        addStatRow(statsGrid, 0, "Health", enemy.getMaxHealth() + " HP", IconUtils.Icon.HEART, "#e74c3c");
        addStatRow(statsGrid, 1, "Mobility", String.valueOf(enemy.getMovement()), IconUtils.Icon.MOVE, "#3498db");
        addStatRow(statsGrid, 2, "Attack", String.valueOf(enemy.getAttackPower()), IconUtils.Icon.TARGET, "#e67e22");
        addStatRow(statsGrid, 3, "Initiative", String.valueOf(enemy.getInitiative()), IconUtils.Icon.LIGHTNING, "#9b59b6");
        
        // Action buttons
        HBox btnBox = new HBox(10);
        btnBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button editBtn = new Button("Edit");
        editBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.EDIT));
        editBtn.getStyleClass().add("button");
        editBtn.setOnAction(e -> showEnemyDialog(enemy));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setGraphic(IconUtils.smallIcon(IconUtils.Icon.DELETE));
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteEnemy(enemy));
        
        btnBox.getChildren().addAll(editBtn, deleteBtn);
        
        pane.getChildren().addAll(header, new Separator(), statsGrid, btnBox);
        return pane;
    }

    private void addStatRow(GridPane grid, int row, String label, String value, IconUtils.Icon icon, String iconColor) {
        HBox labelBox = new HBox(8);
        labelBox.setAlignment(Pos.CENTER_LEFT);
        labelBox.getChildren().addAll(
            IconUtils.createIcon(icon, 18, iconColor),
            new Label(label + ":")
        );
        labelBox.getChildren().get(1).getStyleClass().add("form-label");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("label");
        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        grid.add(labelBox, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private String brightenColor(String hex, int amount) {
        hex = hex.replace("#", "");
        int r = Math.max(0, Math.min(255, Integer.parseInt(hex.substring(0, 2), 16) + amount));
        int g = Math.max(0, Math.min(255, Integer.parseInt(hex.substring(2, 4), 16) + amount));
        int b = Math.max(0, Math.min(255, Integer.parseInt(hex.substring(4, 6), 16) + amount));
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private void deleteEnemy(Enemy enemy) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Enemy");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete " + enemy.getName() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            File file = new File("saves/entities/enemies/" + enemy.getName() + ".json");
            if (file.exists()) {
                file.delete();
            }
            loadEnemies();
            updateSheetLists();
            displayPane.getChildren().clear();
        }
    }

    private void handleBack() {
        if (battleView != null) {
            // Return to active battle - use stored battle view
            appController.navigateToCurrentBattle();
        } else {
            // Return to main menu
            appController.navigateToMainMenu();
        }
    }

    private void handleNew() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(appController.getPrimaryStage());
        dialog.setTitle("New Character");
        dialog.setResizable(false);
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("panel-dark");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        // Name
        grid.add(new Label("Name:"), 0, row);
        TextField nameField = new TextField("New Character");
        grid.add(nameField, 1, row++);
        
        // Color
        grid.add(new Label("Color:"), 0, row);
        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(CharSheet.getColorNames());
        colorCombo.getSelectionModel().selectFirst();
        grid.add(colorCombo, 1, row++);
        
        // HP
        grid.add(new Label("Total HP:"), 0, row);
        TextField hpField = new TextField("20");
        grid.add(hpField, 1, row++);
        
        // Attributes
        grid.add(new Label("STR:"), 0, row);
        TextField strField = new TextField("5");
        grid.add(strField, 1, row++);
        
        grid.add(new Label("DEX:"), 0, row);
        TextField dexField = new TextField("5");
        grid.add(dexField, 1, row++);
        
        grid.add(new Label("ITV:"), 0, row);
        TextField itvField = new TextField("5");
        grid.add(itvField, 1, row++);
        
        grid.add(new Label("MOB:"), 0, row);
        TextField mobField = new TextField("5");
        grid.add(mobField, 1, row++);
        
        // Equipment
        ItemDatabase db = ItemDatabase.getInstance();
        
        grid.add(new Label("Weapon:"), 0, row);
        ComboBox<String> weaponCombo = new ComboBox<>();
        weaponCombo.getItems().add("None");
        weaponCombo.getItems().addAll(db.getAllWeapons().keySet());
        weaponCombo.getSelectionModel().select("None");
        grid.add(weaponCombo, 1, row++);
        
        grid.add(new Label("Head:"), 0, row);
        ComboBox<String> headCombo = new ComboBox<>();
        headCombo.getItems().add("None");
        for (String name : db.getAllArmors().keySet()) {
            Armor armor = db.getArmor(name);
            if (armor != null && armor.getArmorType() == 0) headCombo.getItems().add(name);
        }
        headCombo.getSelectionModel().select("None");
        grid.add(headCombo, 1, row++);
        
        grid.add(new Label("Torso:"), 0, row);
        ComboBox<String> torsoCombo = new ComboBox<>();
        torsoCombo.getItems().add("None");
        for (String name : db.getAllArmors().keySet()) {
            Armor armor = db.getArmor(name);
            if (armor != null && armor.getArmorType() == 1) torsoCombo.getItems().add(name);
        }
        torsoCombo.getSelectionModel().select("None");
        grid.add(torsoCombo, 1, row++);
        
        grid.add(new Label("Legs:"), 0, row);
        ComboBox<String> legsCombo = new ComboBox<>();
        legsCombo.getItems().add("None");
        for (String name : db.getAllArmors().keySet()) {
            Armor armor = db.getArmor(name);
            if (armor != null && armor.getArmorType() == 2) legsCombo.getItems().add(name);
        }
        legsCombo.getSelectionModel().select("None");
        grid.add(legsCombo, 1, row++);
        
        // Buttons
        HBox btnBox = new HBox(10);
        Button createBtn = new Button("Create");
        createBtn.getStyleClass().add("button-primary");
        createBtn.setOnAction(e -> {
            try {
                int hp = Integer.parseInt(hpField.getText());
                int[] attr = {
                    Integer.parseInt(strField.getText()),
                    Integer.parseInt(dexField.getText()),
                    Integer.parseInt(itvField.getText()),
                    Integer.parseInt(mobField.getText())
                };
                
                CharSheet newSheet = new CharSheet(nameField.getText(), true, hp, attr,
                        colorCombo.getSelectionModel().getSelectedIndex());
                
                // Equip items (only if not "None")
                String weaponName = weaponCombo.getSelectionModel().getSelectedItem();
                if (weaponName != null && !weaponName.equals("None")) {
                    Weapon weapon = db.getWeapon(weaponName);
                    if (weapon != null) {
                        newSheet.equipPrimaryWeapon(weapon);
                        newSheet.getInventory().add(weapon);
                    }
                }
                
                String headName = headCombo.getSelectionModel().getSelectedItem();
                if (headName != null && !headName.equals("None")) {
                    Armor head = db.getArmor(headName);
                    if (head != null) {
                        newSheet.equipHead(head);
                        newSheet.getInventory().add(head);
                    }
                }
                
                String torsoName = torsoCombo.getSelectionModel().getSelectedItem();
                if (torsoName != null && !torsoName.equals("None")) {
                    Armor torso = db.getArmor(torsoName);
                    if (torso != null) {
                        newSheet.equipTorso(torso);
                        newSheet.getInventory().add(torso);
                    }
                }
                
                String legsName = legsCombo.getSelectionModel().getSelectedItem();
                if (legsName != null && !legsName.equals("None")) {
                    Armor legs = db.getArmor(legsName);
                    if (legs != null) {
                        newSheet.equipLegs(legs);
                        newSheet.getInventory().add(legs);
                    }
                }
                
                CharacterSheetPane pane = new CharacterSheetPane(newSheet);
                SheetButton sheetBtn = new SheetButton(pane, nameField.getText());
                sheets.add(sheetBtn);
                updateSheetLists();
                newSheet.save();
                showSheet(sheetBtn);
                dialog.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid number format.");
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setOnAction(e -> dialog.close());
        
        btnBox.getChildren().addAll(createBtn, cancelBtn);
        grid.add(btnBox, 0, row++);
        GridPane.setColumnSpan(btnBox, 2);
        
        Scene scene = new Scene(grid, 350, 520);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void handleNewEnemy() {
        showEnemyDialog(null);
    }

    private void showEnemyDialog(Enemy existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(appController.getPrimaryStage());
        dialog.setTitle(existing == null ? "New Enemy" : "Edit Enemy");
        dialog.setResizable(false);
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("panel-dark");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        grid.add(new Label("Name:"), 0, row);
        TextField nameField = new TextField(existing != null ? existing.getName() : "New Enemy");
        grid.add(nameField, 1, row++);
        
        grid.add(new Label("Health:"), 0, row);
        Spinner<Integer> hpSpinner = new Spinner<>(1, 9999, existing != null ? existing.getMaxHealth() : 10);
        hpSpinner.setEditable(true);
        grid.add(hpSpinner, 1, row++);
        
        grid.add(new Label("Mobility:"), 0, row);
        Spinner<Integer> mobSpinner = new Spinner<>(1, 99, existing != null ? existing.getMovement() : 3);
        mobSpinner.setEditable(true);
        grid.add(mobSpinner, 1, row++);
        
        grid.add(new Label("Attack:"), 0, row);
        Spinner<Integer> atkSpinner = new Spinner<>(0, 999, existing != null ? existing.getAttackPower() : 5);
        atkSpinner.setEditable(true);
        grid.add(atkSpinner, 1, row++);
        
        grid.add(new Label("Initiative:"), 0, row);
        Spinner<Integer> initSpinner = new Spinner<>(0, 99, existing != null ? existing.getInitiative() : 5);
        initSpinner.setEditable(true);
        grid.add(initSpinner, 1, row++);
        
        grid.add(new Label("Color:"), 0, row);
        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(CharSheet.getColorNames());
        colorCombo.getSelectionModel().select(existing != null ? existing.getColor() : 0);
        grid.add(colorCombo, 1, row++);
        
        HBox btnBox = new HBox(10);
        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                Enemy enemy = new Enemy(0, 0, name, hpSpinner.getValue(), mobSpinner.getValue(),
                        atkSpinner.getValue(), initSpinner.getValue(), colorCombo.getSelectionModel().getSelectedIndex());
                enemy.save();
                loadEnemies();
                updateSheetLists();
                showEnemyPane(enemy);
                dialog.close();
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setOnAction(e -> dialog.close());
        
        btnBox.getChildren().addAll(saveBtn, cancelBtn);
        grid.add(btnBox, 0, row++);
        GridPane.setColumnSpan(btnBox, 2);
        
        Scene scene = new Scene(grid);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public void setBattleView(BattleView battleView) {
        this.battleView = battleView;
    }

    public void endBattle() {
        battleView = null;
    }

    public ArrayList<SheetButton> getSheets() {
        return sheets;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
