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

    private final Stage stage;
    private final ArrayList<SheetButton> sheets = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private BattleView battleView;
    private Button returnToBattleBtn;
    private TabPane listTabs;
    private StackPane displayPane;

    public CharacterSheetView() {
        stage = new Stage();
        stage.setTitle("Character Sheets");
        
        BorderPane root = new BorderPane();
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
        
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(400);
        
        autoLoadSheets();
    }

    private HBox createButtonPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-border-color: #505052; -fx-border-width: 0 0 1 0;");
        
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("button");
        backBtn.setPrefSize(100, 35);
        backBtn.setOnAction(e -> handleBack());
        
        Button saveBtn = new Button("Save All");
        saveBtn.getStyleClass().add("button");
        saveBtn.setPrefSize(100, 35);
        saveBtn.setOnAction(e -> handleSave());
        
        Button loadBtn = new Button("Load");
        loadBtn.getStyleClass().add("button");
        loadBtn.setPrefSize(100, 35);
        loadBtn.setOnAction(e -> handleLoad());
        
        Button newCharBtn = new Button("+ Character");
        newCharBtn.getStyleClass().add("button-primary");
        newCharBtn.setPrefSize(110, 35);
        newCharBtn.setOnAction(e -> handleNew());
        
        Button newEnemyBtn = new Button("+ Enemy");
        newEnemyBtn.getStyleClass().add("button-primary");
        newEnemyBtn.setPrefSize(100, 35);
        newEnemyBtn.setOnAction(e -> handleNewEnemy());
        
        returnToBattleBtn = new Button("Return to Battle");
        returnToBattleBtn.getStyleClass().add("button");
        returnToBattleBtn.setPrefSize(140, 35);
        returnToBattleBtn.setDisable(true);
        returnToBattleBtn.setOnAction(e -> handleReturnToBattle());
        
        panel.getChildren().addAll(backBtn, saveBtn, loadBtn, newCharBtn, newEnemyBtn, returnToBattleBtn);
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

    private VBox createEnemyDisplayPane(Enemy enemy) {
        CardUtils.CardStyle style = CardUtils.CardStyle.ENEMY;
        
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        pane.setStyle(String.format(
            "-fx-background-color: linear-gradient(to bottom right, %s, %s); " +
            "-fx-background-radius: 8;",
            style.bgColor, adjustBrightness(style.bgColor, -15)
        ));
        
        // Header with icon and name
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle(String.format("-fx-border-color: transparent transparent %s transparent; -fx-border-width: 0 0 1 0;", style.borderColor));
        
        Node avatar = IconUtils.createIcon(IconUtils.Icon.SKULL, 36, style.accentColor);
        
        Label titleLabel = new Label(enemy.getName());
        titleLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 24px; -fx-font-weight: bold;", style.accentColor));
        
        header.getChildren().addAll(avatar, titleLabel);
        
        // Stats cards in a grid
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER);
        
        statsRow.getChildren().addAll(
            createStatCard("Health", String.valueOf(enemy.getMaxHealth()), IconUtils.Icon.HEART, "#F44336"),
            createStatCard("Attack", String.valueOf(enemy.getAttackPower()), IconUtils.Icon.SWORDS, "#FF9800"),
            createStatCard("Mobility", String.valueOf(enemy.getMovement()), IconUtils.Icon.FLAG, "#4CAF50"),
            createStatCard("Initiative", String.valueOf(enemy.getInitiative()), IconUtils.Icon.CLOCK, "#9C27B0")
        );
        
        // Color indicator
        HBox colorRow = new HBox(10);
        colorRow.setAlignment(Pos.CENTER_LEFT);
        colorRow.setPadding(new Insets(10, 0, 0, 0));
        
        Label colorLabel = new Label("Display Color:");
        colorLabel.setStyle("-fx-text-fill: #888;");
        
        Label colorName = CardUtils.createBadge(CharSheet.getColorNames()[enemy.getColor()], style.accentColor);
        
        colorRow.getChildren().addAll(colorLabel, colorName);
        
        // Action buttons
        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(15, 0, 0, 0));
        
        Button editBtn = new Button("Edit");
        editBtn.setGraphic(IconUtils.createIcon(IconUtils.Icon.EDIT, 14, "#fff"));
        editBtn.getStyleClass().add("button-primary");
        editBtn.setOnAction(e -> showEnemyDialog(enemy));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setGraphic(IconUtils.createIcon(IconUtils.Icon.CLOSE, 14, "#fff"));
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> deleteEnemy(enemy));
        
        buttons.getChildren().addAll(editBtn, deleteBtn);
        
        pane.getChildren().addAll(header, statsRow, colorRow, buttons);
        return pane;
    }
    
    private VBox createStatCard(String label, String value, IconUtils.Icon icon, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setMinWidth(90);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2d2d30, #252528); " +
            "-fx-background-radius: 8; -fx-border-color: #3c3c3e; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        
        Node iconNode = IconUtils.createIcon(icon, 24, color);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 18px; -fx-font-weight: bold;", color));
        
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        
        card.getChildren().addAll(iconNode, valueLabel, labelNode);
        return card;
    }
    
    private String adjustBrightness(String hexColor, int amount) {
        String hex = hexColor.replace("#", "");
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
        stage.hide();
        // Return to main menu - MainMenuView needs to handle this
        Stage mainStage = new Stage();
        new MainMenuView(mainStage).show();
    }

    private void handleSave() {
        for (SheetButton sheetBtn : sheets) {
            sheetBtn.getSheet().getCharSheet().save();
        }
        showAlert(Alert.AlertType.INFORMATION, "Saved", "All character sheets saved!");
    }

    private void handleLoad() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Load Character");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter character name:");
        
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                CharSheet loaded = CharSheet.load(name.trim(), true);
                if (loaded == null) {
                    loaded = CharSheet.load(name.trim(), false);
                }
                if (loaded != null) {
                    CharacterSheetPane pane = new CharacterSheetPane(loaded);
                    SheetButton sheetBtn = new SheetButton(pane, loaded.getName());
                    sheets.add(sheetBtn);
                    updateSheetLists();
                    showSheet(sheetBtn);
                    showAlert(Alert.AlertType.INFORMATION, "Loaded", "Character sheet loaded!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load character sheet.");
                }
            }
        });
    }

    private void handleNew() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
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
        dialog.initOwner(stage);
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

    private void handleReturnToBattle() {
        if (battleView != null) {
            stage.hide();
            battleView.setBattleVisible(true);
        }
    }

    public void setBattleView(BattleView battleView) {
        this.battleView = battleView;
        returnToBattleBtn.setDisable(false);
    }

    public void endBattle() {
        battleView = null;
        returnToBattleBtn.setDisable(true);
    }

    public ArrayList<SheetButton> getSheets() {
        return sheets;
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
