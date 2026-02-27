package UI;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Utility class providing reusable animations for JavaFX UI components.
 */
public class AnimationUtils {

    // Default durations
    private static final Duration BUTTON_HOVER_DURATION = Duration.millis(100);
    private static final Duration PROGRESS_BAR_DURATION = Duration.millis(300);
    private static final Duration FADE_DURATION = Duration.millis(200);
    private static final Duration SLIDE_DURATION = Duration.millis(250);
    private static final Duration PULSE_DURATION = Duration.millis(150);

    /**
     * Adds hover animation to a button (scale up + glow effect).
     */
    public static void addButtonHoverAnimation(Button button) {
        addButtonHoverAnimation(button, Color.web("#569cd6"));
    }

    /**
     * Adds hover animation to a button with a custom glow color.
     */
    public static void addButtonHoverAnimation(Button button, Color glowColor) {
        ScaleTransition scaleUp = new ScaleTransition(BUTTON_HOVER_DURATION, button);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        ScaleTransition scaleDown = new ScaleTransition(BUTTON_HOVER_DURATION, button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        DropShadow glow = new DropShadow();
        glow.setColor(glowColor);
        glow.setRadius(15);
        glow.setSpread(0.3);

        button.setOnMouseEntered(e -> {
            button.setEffect(glow);
            scaleUp.playFromStart();
        });

        button.setOnMouseExited(e -> {
            button.setEffect(null);
            scaleDown.playFromStart();
        });
    }

    /**
     * Animates a progress bar to a target value smoothly.
     */
    public static void animateProgressBar(ProgressBar bar, double targetValue) {
        animateProgressBar(bar, targetValue, PROGRESS_BAR_DURATION);
    }

    /**
     * Animates a progress bar to a target value with custom duration.
     */
    public static void animateProgressBar(ProgressBar bar, double targetValue, Duration duration) {
        Timeline timeline = new Timeline(
            new KeyFrame(duration, 
                new KeyValue(bar.progressProperty(), targetValue, Interpolator.EASE_BOTH)
            )
        );
        timeline.play();
    }

    /**
     * Fades a node in (opacity 0 -> 1).
     */
    public static void fadeIn(Node node) {
        fadeIn(node, FADE_DURATION);
    }

    /**
     * Fades a node in with custom duration.
     */
    public static void fadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Fades a node out (opacity 1 -> 0).
     */
    public static FadeTransition fadeOut(Node node) {
        return fadeOut(node, FADE_DURATION);
    }

    /**
     * Fades a node out with custom duration.
     */
    public static FadeTransition fadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.play();
        return fade;
    }

    /**
     * Plays a quick pulse animation (scale up then down) to draw attention.
     */
    public static void pulse(Node node) {
        pulse(node, 1.1);
    }

    /**
     * Plays a pulse animation with custom scale factor.
     */
    public static void pulse(Node node, double scaleFactor) {
        ScaleTransition scaleUp = new ScaleTransition(PULSE_DURATION, node);
        scaleUp.setToX(scaleFactor);
        scaleUp.setToY(scaleFactor);

        ScaleTransition scaleDown = new ScaleTransition(PULSE_DURATION, node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        SequentialTransition pulse = new SequentialTransition(scaleUp, scaleDown);
        pulse.play();
    }

    /**
     * Slides a node in from a direction.
     */
    public static void slideIn(Node node, SlideDirection direction) {
        slideIn(node, direction, SLIDE_DURATION);
    }

    /**
     * Slides a node in from a direction with custom duration.
     */
    public static void slideIn(Node node, SlideDirection direction, Duration duration) {
        double startX = 0, startY = 0;
        
        switch (direction) {
            case LEFT -> startX = -50;
            case RIGHT -> startX = 50;
            case UP -> startY = -30;
            case DOWN -> startY = 30;
        }

        node.setTranslateX(startX);
        node.setTranslateY(startY);
        node.setOpacity(0);

        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setToX(0);
        translate.setToY(0);
        translate.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.play();
    }

    /**
     * Animates height expansion/collapse for a panel (for slide open/close effects).
     * @param node The node to animate
     * @param targetHeight The target height (0 to collapse, positive to expand)
     * @param onFinished Optional callback when animation completes
     */
    public static void animateHeight(javafx.scene.layout.Region node, double targetHeight, Runnable onFinished) {
        Timeline timeline = new Timeline(
            new KeyFrame(SLIDE_DURATION,
                new KeyValue(node.maxHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                new KeyValue(node.minHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                new KeyValue(node.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> onFinished.run());
        }
        
        timeline.play();
    }

    /**
     * Creates a highlight effect animation (for selected items, current turns, etc.)
     * Uses smooth easing for a polished look.
     */
    public static void highlightPulse(Node node, Color highlightColor) {
        DropShadow glow = new DropShadow();
        glow.setColor(highlightColor);
        glow.setRadius(0);
        glow.setSpread(0.4);
        
        node.setEffect(glow);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(glow.radiusProperty(), 0, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(400), 
                new KeyValue(glow.radiusProperty(), 12, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(800), 
                new KeyValue(glow.radiusProperty(), 8, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }

    /**
     * Direction enum for slide animations.
     */
    public enum SlideDirection {
        LEFT, RIGHT, UP, DOWN
    }
}
