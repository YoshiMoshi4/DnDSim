package UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

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

        Button battleBtn = createMenuButton("Start Battle System", IconUtils.Icon.SWORDS, "#b8860b");
        battleBtn.setOnAction(e -> handleBattleSystem());
        grid.add(battleBtn, 0, 0);

        Button characterBtn = createMenuButton("Character Sheets", IconUtils.Icon.SCRIPT, "#569cd6");
        characterBtn.setOnAction(e -> appController.navigateToCharacterSheets());
        grid.add(characterBtn, 1, 0);

        Button assetBtn = createMenuButton("Asset Editor", IconUtils.Icon.WRENCH, "#4ec9b0");
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
        GridSetupDialog.show(appController.getPrimaryStage())
            .ifPresent(r -> appController.navigateToBattle(r.rows, r.cols, r.mask, r.themeName));
    }
}
