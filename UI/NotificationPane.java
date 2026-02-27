package UI;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * A toast notification system that shows non-blocking messages.
 * Messages appear at the top of the parent container and auto-dismiss.
 */
public class NotificationPane extends StackPane {

    private final VBox toastContainer;
    private static final int MAX_TOASTS = 4;
    private static final Duration TOAST_DURATION = Duration.seconds(3);
    private static final Duration FADE_DURATION = Duration.millis(300);

    public enum ToastType {
        INFO("#569cd6"),      // Blue
        SUCCESS("#4CAF50"),   // Green
        WARNING("#FF9800"),   // Orange
        DANGER("#F44336"),    // Red
        COMBAT("#b8860b");    // Gold

        private final String color;

        ToastType(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    public NotificationPane() {
        setPickOnBounds(false); // Allow clicks to pass through empty areas
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10, 0, 0, 0));

        toastContainer = new VBox(8);
        toastContainer.setAlignment(Pos.TOP_CENTER);
        toastContainer.setPickOnBounds(false);
        toastContainer.setMaxWidth(400);

        getChildren().add(toastContainer);
    }

    /**
     * Shows a toast notification with default INFO type.
     */
    public void showToast(String message) {
        showToast(message, ToastType.INFO);
    }

    /**
     * Shows a toast notification with the specified type.
     */
    public void showToast(String message, ToastType type) {
        showToast(message, type, TOAST_DURATION);
    }

    /**
     * Shows a toast notification with custom duration.
     */
    public void showToast(String message, ToastType type, Duration duration) {
        // Remove oldest toast if at max
        while (toastContainer.getChildren().size() >= MAX_TOASTS) {
            if (!toastContainer.getChildren().isEmpty()) {
                toastContainer.getChildren().remove(0);
            }
        }

        Label toast = createToast(message, type);
        toastContainer.getChildren().add(toast);

        // Animate in
        toast.setOpacity(0);
        toast.setTranslateY(-20);

        ParallelTransition showAnim = new ParallelTransition(
            createFadeIn(toast),
            createSlideDown(toast)
        );

        // Schedule auto-dismiss
        PauseTransition delay = new PauseTransition(duration);
        delay.setOnFinished(e -> dismissToast(toast));

        showAnim.setOnFinished(e -> delay.play());
        showAnim.play();
    }

    /**
     * Shows a combat message (attack result, damage, etc.)
     */
    public void showCombatMessage(String attacker, String action, String target, String result) {
        String message = String.format("%s %s %s - %s", attacker, action, target, result);
        showToast(message, ToastType.COMBAT);
    }

    /**
     * Shows a brief attack notification.
     */
    public void showAttackResult(String attacker, String target, int damage, int remainingHp, int maxHp) {
        String hpPercent = String.format("%.0f%%", (remainingHp / (double) maxHp) * 100);
        String message = String.format("⚔ %s → %s: %d damage (%s HP)", attacker, target, damage, hpPercent);
        
        ToastType type = remainingHp <= 0 ? ToastType.DANGER : 
                         remainingHp < maxHp * 0.25 ? ToastType.WARNING : ToastType.COMBAT;
        showToast(message, type);
    }

    /**
     * Shows a defeat notification.
     */
    public void showDefeat(String name) {
        showToast("💀 " + name + " has been defeated!", ToastType.DANGER);
    }

    /**
     * Shows a heal notification.
     */
    public void showHeal(String name, int amount) {
        showToast("💚 " + name + " healed " + amount + " HP!", ToastType.SUCCESS);
    }

    /**
     * Shows mode change notification (attack mode, move mode).
     */
    public void showModeChange(String message) {
        showToast(message, ToastType.INFO, Duration.seconds(2));
    }

    private Label createToast(String message, ToastType type) {
        Label toast = new Label(message);
        toast.setWrapText(true);
        toast.setMaxWidth(380);
        toast.setPadding(new Insets(12, 20, 12, 20));
        toast.setStyle(String.format(
            "-fx-background-color: #2d2d30;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: %s;" +
            "-fx-border-width: 0 0 0 4;" +
            "-fx-border-radius: 8 0 0 8;" +
            "-fx-text-fill: #dcdcdc;" +
            "-fx-font-size: 13px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 2, 2);",
            type.getColor()
        ));
        return toast;
    }

    private void dismissToast(Label toast) {
        FadeTransition fade = new FadeTransition(FADE_DURATION, toast);
        fade.setToValue(0);
        
        TranslateTransition slide = new TranslateTransition(FADE_DURATION, toast);
        slide.setByY(-10);

        ParallelTransition dismissAnim = new ParallelTransition(fade, slide);
        dismissAnim.setOnFinished(e -> toastContainer.getChildren().remove(toast));
        dismissAnim.play();
    }

    private FadeTransition createFadeIn(Label toast) {
        FadeTransition fade = new FadeTransition(FADE_DURATION, toast);
        fade.setFromValue(0);
        fade.setToValue(1);
        return fade;
    }

    private TranslateTransition createSlideDown(Label toast) {
        TranslateTransition slide = new TranslateTransition(FADE_DURATION, toast);
        slide.setFromY(-20);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        return slide;
    }
}
