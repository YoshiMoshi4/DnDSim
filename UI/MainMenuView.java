package UI;

import UI.Battle.BattleView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainMenuView {

    private final Stage stage;
    private CharacterSheetView characterSheetView;

    public MainMenuView(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("DnD Simulator");

        characterSheetView = new CharacterSheetView();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(40));
        grid.getStyleClass().add("panel-dark");

        Button battleBtn = createMenuButton("Start Battle System");
        battleBtn.setOnAction(e -> handleBattleSystem());
        grid.add(battleBtn, 0, 0);

        Button characterBtn = createMenuButton("Character Sheets");
        characterBtn.setOnAction(e -> handleCharacterSheet());
        grid.add(characterBtn, 1, 0);

        Button assetBtn = createMenuButton("Asset Editor");
        assetBtn.setOnAction(e -> handleAssetEditor());
        grid.add(assetBtn, 0, 1);
        GridPane.setColumnSpan(assetBtn, 2);

        Scene scene = new Scene(grid, 800, 700);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());

        stage.setScene(scene);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", "button-large");
        btn.setPrefSize(300, 200);
        return btn;
    }

    public void show() {
        stage.show();
    }

    private void handleBattleSystem() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Select Grid Size");
        dialog.setResizable(false);

        final int[] selectedRows = {0};
        final int[] selectedCols = {0};

        BorderPane root = new BorderPane();
        root.getStyleClass().add("panel-dark");
        root.setPadding(new Insets(10));

        // Title
        Label titleLabel = new Label("Hover to select grid size, click to confirm");
        titleLabel.getStyleClass().add("label");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setPadding(new Insets(10));
        root.setTop(titleLabel);

        // Size label
        Label sizeLabel = new Label("0 x 0");
        sizeLabel.getStyleClass().add("label-title");
        sizeLabel.setAlignment(Pos.CENTER);
        sizeLabel.setMaxWidth(Double.MAX_VALUE);
        sizeLabel.setPadding(new Insets(10));

        // Grid picker canvas
        final int MAX_DISPLAY = 25;
        final int CELL_SIZE = 10;

        Canvas canvas = new Canvas(MAX_DISPLAY * CELL_SIZE + 1, MAX_DISPLAY * CELL_SIZE + 1);
        final int[] hoverRow = {-1};
        final int[] hoverCol = {-1};

        Runnable drawGrid = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.web("#2d2d30"));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            for (int row = 0; row < MAX_DISPLAY; row++) {
                for (int col = 0; col < MAX_DISPLAY; col++) {
                    double x = col * CELL_SIZE;
                    double y = row * CELL_SIZE;

                    if (row <= hoverRow[0] && col <= hoverCol[0]) {
                        gc.setFill(Color.web("#569cd6"));
                    } else {
                        gc.setFill(Color.web("#464648"));
                    }
                    gc.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                }
            }
        };

        canvas.setOnMouseMoved(e -> {
            int col = Math.min((int) (e.getX() / CELL_SIZE), MAX_DISPLAY - 1);
            int row = Math.min((int) (e.getY() / CELL_SIZE), MAX_DISPLAY - 1);
            if (col != hoverCol[0] || row != hoverRow[0]) {
                hoverCol[0] = col;
                hoverRow[0] = row;
                sizeLabel.setText((row + 1) + " x " + (col + 1));
                drawGrid.run();
            }
        });

        canvas.setOnMouseExited(e -> {
            hoverRow[0] = -1;
            hoverCol[0] = -1;
            sizeLabel.setText("0 x 0");
            drawGrid.run();
        });

        final boolean[] confirmed = {false};
        canvas.setOnMouseClicked(e -> {
            if (hoverRow[0] >= 0 && hoverCol[0] >= 0) {
                selectedRows[0] = hoverRow[0] + 1;
                selectedCols[0] = hoverCol[0] + 1;
                confirmed[0] = true;
                dialog.close();
            }
        });

        drawGrid.run();

        StackPane canvasWrapper = new StackPane(canvas);
        canvasWrapper.setPadding(new Insets(10));
        root.setCenter(canvasWrapper);

        // Bottom panel
        VBox bottomPanel = new VBox(10);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setPadding(new Insets(10));

        bottomPanel.getChildren().add(sizeLabel);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setOnAction(e -> {
            selectedRows[0] = 0;
            selectedCols[0] = 0;
            dialog.close();
        });
        bottomPanel.getChildren().add(cancelBtn);

        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 320, 450);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());

        dialog.setScene(scene);
        dialog.showAndWait();

        System.out.println("Dialog closed. Confirmed: " + confirmed[0] + ", Selected: " + selectedRows[0] + " x " + selectedCols[0]);
        if (confirmed[0] && selectedRows[0] > 0 && selectedCols[0] > 0) {
            try {
                System.out.println("Creating BattleView...");
                BattleView battleView = new BattleView(selectedRows[0], selectedCols[0], characterSheetView);
                System.out.println("BattleView created, hiding main stage...");
                stage.hide();
                System.out.println("Showing BattleView...");
                battleView.show();
                System.out.println("BattleView shown.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleCharacterSheet() {
        characterSheetView.show();
        stage.hide();
    }

    private void handleAssetEditor() {
        AssetEditorView assetEditor = new AssetEditorView();
        assetEditor.show();
        stage.hide();
    }

    public CharacterSheetView getCharacterSheetView() {
        return characterSheetView;
    }
}
