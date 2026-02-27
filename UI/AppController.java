package UI;

import UI.Battle.BattleView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Central controller managing the single application window and navigation between views.
 */
public class AppController {

    private static AppController instance;
    
    private final Stage primaryStage;
    private final StackPane rootContainer;
    private final Scene scene;
    
    // Views (lazily initialized)
    private MainMenuView mainMenuView;
    private CharacterSheetView characterSheetView;
    private AssetEditorView assetEditorView;
    private BattleView currentBattleView;

    private AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.rootContainer = new StackPane();
        this.rootContainer.getStyleClass().add("panel-dark");
        
        this.scene = new Scene(rootContainer, 1200, 800);
        scene.getStylesheets().add(new java.io.File("resources/styles/dark-theme.css").toURI().toString());
        
        primaryStage.setTitle("Cassandralis Combat Simulator");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
    }

    public static void initialize(Stage primaryStage) {
        if (instance == null) {
            instance = new AppController(primaryStage);
        }
    }

    public static AppController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AppController not initialized. Call initialize() first.");
        }
        return instance;
    }

    public void show() {
        navigateToMainMenu();
        primaryStage.show();
    }

    public void navigateToMainMenu() {
        if (mainMenuView == null) {
            mainMenuView = new MainMenuView(this);
        }
        setContent(mainMenuView.getRoot());
        primaryStage.setTitle("Cassandralis Combat Simulator");
    }

    public void navigateToCharacterSheets() {
        try {
            if (characterSheetView == null) {
                characterSheetView = new CharacterSheetView(this);
            }
            characterSheetView.refresh();
            setContent(characterSheetView.getRoot());
            primaryStage.setTitle("Character Sheets - Cassandralis");
        } catch (Exception e) {
            e.printStackTrace();
            // Reset to allow retry
            characterSheetView = null;
        }
    }

    public void navigateToBattle(int rows, int cols) {
        try {
            if (characterSheetView == null) {
                characterSheetView = new CharacterSheetView(this);
            }
            currentBattleView = new BattleView(rows, cols, characterSheetView, this);
            characterSheetView.setBattleView(currentBattleView);
            setContent(currentBattleView.getRoot());
            primaryStage.setTitle("Battle - Cassandralis");
        } catch (Exception e) {
            e.printStackTrace();
            // Reset to allow retry
            characterSheetView = null;
            currentBattleView = null;
        }
    }

    public void navigateToCurrentBattle() {
        if (currentBattleView != null) {
            setContent(currentBattleView.getRoot());
            primaryStage.setTitle("Battle - Cassandralis");
        } else {
            navigateToMainMenu();
        }
    }

    public void navigateToAssetEditor() {
        if (assetEditorView == null) {
            assetEditorView = new AssetEditorView(this);
        }
        setContent(assetEditorView.getRoot());
        primaryStage.setTitle("Asset Editor - Cassandralis");
    }

    public void returnFromBattle() {
        if (characterSheetView != null) {
            characterSheetView.endBattle();
        }
        currentBattleView = null;
        navigateToMainMenu();
    }

    public BattleView getCurrentBattleView() {
        return currentBattleView;
    }

    public CharacterSheetView getCharacterSheetView() {
        if (characterSheetView == null) {
            characterSheetView = new CharacterSheetView(this);
        }
        return characterSheetView;
    }

    private void setContent(Node content) {
        if (!rootContainer.getChildren().isEmpty()) {
            Node oldContent = rootContainer.getChildren().get(0);
            if (oldContent == content) {
                return; // Already showing this content
            }
        }
        
        // Immediate switch - no animation
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(content);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
