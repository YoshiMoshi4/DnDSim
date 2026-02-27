package UI;

import EntityRes.CharSheet;
import Objects.TerrainObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class TerrainManagerView {

    private final Stage stage;
    private FlowPane terrainPane;

    public TerrainManagerView() {
        stage = new Stage();
        stage.setTitle("Terrain Manager");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("panel-dark");
        root.setPadding(new Insets(10));
        
        // Top button panel
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(10));
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setStyle("-fx-border-color: #505052; -fx-border-width: 0 0 1 0;");
        
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> handleBack());
        
        Button addBtn = new Button("+ Add Terrain");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> addNewTerrain());
        
        topPanel.getChildren().addAll(backBtn, addBtn);
        root.setTop(topPanel);
        
        // Center terrain display
        terrainPane = new FlowPane(10, 10);
        terrainPane.setPadding(new Insets(10));
        terrainPane.getStyleClass().add("panel");
        
        ScrollPane scroll = new ScrollPane(terrainPane);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("panel");
        root.setCenter(scroll);
        
        refreshDisplay();
        
        Scene scene = new Scene(root, 650, 450);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(300);
    }

    private void refreshDisplay() {
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
        
        java.awt.Color awtColor = terrain.getDisplayColor();
        String hex = String.format("#%02x%02x%02x", awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
        card.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 5; " +
                     "-fx-border-radius: 5; -fx-border-color: #505052;");
        
        Label nameLabel = new Label(terrain.getType());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        card.getChildren().add(nameLabel);
        
        card.getChildren().add(new Label("HP: " + terrain.getHealth()));
        card.getChildren().add(new Label("Blocks: " + (terrain.blocksMovement() ? "Yes" : "No")));
        
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

    private void addNewTerrain() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Add Terrain");
        dialog.setResizable(false);
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("panel-dark");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField typeField = new TextField();
        TextField hpField = new TextField("10");
        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(CharSheet.getColorNames());
        colorCombo.getSelectionModel().select(11); // Default gray
        CheckBox blocksCheck = new CheckBox("Blocks Movement");
        blocksCheck.setSelected(true);
        
        int row = 0;
        grid.add(new Label("Type:"), 0, row);
        grid.add(typeField, 1, row++);
        grid.add(new Label("HP:"), 0, row);
        grid.add(hpField, 1, row++);
        grid.add(new Label("Color:"), 0, row);
        grid.add(colorCombo, 1, row++);
        grid.add(new Label(""), 0, row);
        grid.add(blocksCheck, 1, row++);
        
        HBox btnBox = new HBox(10);
        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("button-primary");
        okBtn.setOnAction(e -> {
            try {
                String type = typeField.getText();
                int hp = Integer.parseInt(hpField.getText());
                int color = colorCombo.getSelectionModel().getSelectedIndex();
                boolean blocks = blocksCheck.isSelected();
                
                if (!type.isEmpty()) {
                    TerrainObject newTerrain = new TerrainObject(0, 0, type, hp, color, blocks);
                    TerrainDatabase.getInstance().addTerrain(newTerrain);
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
        
        Scene scene = new Scene(grid, 300, 230);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void editTerrain(TerrainObject terrain) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Edit Terrain");
        dialog.setResizable(false);
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("panel-dark");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField typeField = new TextField(terrain.getType());
        TextField hpField = new TextField(String.valueOf(terrain.getHealth()));
        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(CharSheet.getColorNames());
        colorCombo.getSelectionModel().select(terrain.getColor());
        CheckBox blocksCheck = new CheckBox("Blocks Movement");
        blocksCheck.setSelected(terrain.blocksMovement());
        
        int row = 0;
        grid.add(new Label("Type:"), 0, row);
        grid.add(typeField, 1, row++);
        grid.add(new Label("HP:"), 0, row);
        grid.add(hpField, 1, row++);
        grid.add(new Label("Color:"), 0, row);
        grid.add(colorCombo, 1, row++);
        grid.add(new Label(""), 0, row);
        grid.add(blocksCheck, 1, row++);
        
        HBox btnBox = new HBox(10);
        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("button-primary");
        okBtn.setOnAction(e -> {
            try {
                terrain.setColor(colorCombo.getSelectionModel().getSelectedIndex());
                terrain.setBlocksMovement(blocksCheck.isSelected());
                TerrainDatabase.getInstance().saveTerrain(terrain);
                dialog.close();
                refreshDisplay();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save terrain.");
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setOnAction(e -> dialog.close());
        
        btnBox.getChildren().addAll(okBtn, cancelBtn);
        grid.add(btnBox, 0, row);
        GridPane.setColumnSpan(btnBox, 2);
        
        Scene scene = new Scene(grid, 300, 230);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        dialog.setScene(scene);
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
                refreshDisplay();
            }
        });
    }

    private void handleBack() {
        stage.hide();
        AppController.getInstance().navigateToMainMenu();
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
