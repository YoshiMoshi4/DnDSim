package UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainMenuView {

    private final AppController appController;
    private final VBox root;

    public MainMenuView(AppController appController) {
        this.appController = appController;

        root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("panel-dark");
        
        // Title with glow effect
        Label titleLabel = new Label("Cassandralis Combat Simulator");
        titleLabel.getStyleClass().add("label-title");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#569cd6"));
        titleGlow.setRadius(15);
        titleGlow.setSpread(0.3);
        titleLabel.setEffect(titleGlow);
        
        Label subtitleLabel = new Label("v4.7.2891-BETA.r38291.mil.gov.cass // BUILD 2170.11.07.1943-SEC3");
        subtitleLabel.getStyleClass().add("label-muted");
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-font-family: 'Consolas';");

        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);

        Button battleBtn = createMenuButton("Start Battle System", IconUtils.Icon.PLAY, "#b8860b");
        battleBtn.setOnAction(e -> handleBattleSystem());
        grid.add(battleBtn, 0, 0);

        Button characterBtn = createMenuButton("Character Sheets", IconUtils.Icon.PERSON, "#569cd6");
        characterBtn.setOnAction(e -> appController.navigateToCharacterSheets());
        grid.add(characterBtn, 1, 0);

        Button assetBtn = createMenuButton("Asset Editor", IconUtils.Icon.GEAR, "#4ec9b0");
        assetBtn.setOnAction(e -> appController.navigateToAssetEditor());
        grid.add(assetBtn, 0, 1);
        GridPane.setColumnSpan(assetBtn, 2);

        root.getChildren().addAll(titleBox, grid);
    }

    private Button createMenuButton(String text, IconUtils.Icon icon, String iconColor) {
        Button btn = new Button();
        btn.getStyleClass().addAll("button", "button-large");
        btn.setPrefSize(300, 200);
        
        // Create icon and label in a VBox
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        
        javafx.scene.Node iconNode = IconUtils.createIcon(icon, 48, iconColor);
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dcdcdc;");
        
        content.getChildren().addAll(iconNode, label);
        btn.setGraphic(content);
        
        AnimationUtils.addButtonHoverAnimation(btn);
        return btn;
    }

    public VBox getRoot() {
        return root;
    }

    private void handleBattleSystem() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(appController.getPrimaryStage());
        dialog.setTitle("Select Grid Size");
        dialog.setResizable(false);

        final int[] selectedRows = {0};
        final int[] selectedCols = {0};

        BorderPane dialogRoot = new BorderPane();
        dialogRoot.getStyleClass().add("panel-dark");
        dialogRoot.setPadding(new Insets(10));

        // Title
        Label titleLabel = new Label("Hover to select grid size, click to confirm");
        titleLabel.getStyleClass().add("label");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setPadding(new Insets(10));
        dialogRoot.setTop(titleLabel);

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
        dialogRoot.setCenter(canvasWrapper);

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

        dialogRoot.setBottom(bottomPanel);

        Scene scene = new Scene(dialogRoot, 320, 450);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());

        dialog.setScene(scene);
        dialog.showAndWait();

        if (confirmed[0] && selectedRows[0] > 0 && selectedCols[0] > 0) {
            appController.navigateToBattle(selectedRows[0], selectedCols[0]);
        }
    }
}
