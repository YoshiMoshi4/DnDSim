package UI;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Utility class for creating responsive, adaptive UI layouts.
 * Provides breakpoint management, adaptive containers, and dynamic sizing.
 */
public class ResponsiveUtils {

    // ==================== BREAKPOINTS ====================
    
    /**
     * Standard breakpoint sizes for responsive design.
     */
    public enum Breakpoint {
        XS(0, 400),      // Extra small
        SM(400, 600),    // Small
        MD(600, 900),    // Medium
        LG(900, 1200),   // Large
        XL(1200, Integer.MAX_VALUE);  // Extra large
        
        public final int minWidth;
        public final int maxWidth;
        
        Breakpoint(int min, int max) {
            this.minWidth = min;
            this.maxWidth = max;
        }
        
        public static Breakpoint fromWidth(double width) {
            for (Breakpoint bp : values()) {
                if (width >= bp.minWidth && width < bp.maxWidth) {
                    return bp;
                }
            }
            return XL;
        }
    }
    
    // ==================== OBSERVABLE BREAKPOINT ====================
    
    /**
     * Observable breakpoint that updates based on a Region's width.
     */
    public static class ResponsiveContext {
        private final ObjectProperty<Breakpoint> currentBreakpoint = new SimpleObjectProperty<>(Breakpoint.MD);
        private final DoubleProperty currentWidth = new SimpleDoubleProperty(600);
        
        public ResponsiveContext(Region target) {
            currentWidth.bind(target.widthProperty());
            currentWidth.addListener((obs, old, newVal) -> {
                currentBreakpoint.set(Breakpoint.fromWidth(newVal.doubleValue()));
            });
        }
        
        public ObjectProperty<Breakpoint> breakpointProperty() {
            return currentBreakpoint;
        }
        
        public Breakpoint getBreakpoint() {
            return currentBreakpoint.get();
        }
        
        public DoubleProperty widthProperty() {
            return currentWidth;
        }
        
        public double getWidth() {
            return currentWidth.get();
        }
        
        public boolean isAtLeast(Breakpoint bp) {
            return currentBreakpoint.get().ordinal() >= bp.ordinal();
        }
        
        public boolean isAtMost(Breakpoint bp) {
            return currentBreakpoint.get().ordinal() <= bp.ordinal();
        }
    }
    
    // ==================== ADAPTIVE CONTAINERS ====================
    
    /**
     * Creates a responsive FlowPane that adjusts gap and padding based on available width.
     */
    public static FlowPane createResponsiveFlowPane() {
        FlowPane flowPane = new FlowPane();
        flowPane.getStyleClass().add("responsive-flow-pane");
        
        flowPane.widthProperty().addListener((obs, old, newWidth) -> {
            Breakpoint bp = Breakpoint.fromWidth(newWidth.doubleValue());
            adaptFlowPane(flowPane, bp);
        });
        
        // Set initial values
        adaptFlowPane(flowPane, Breakpoint.MD);
        
        return flowPane;
    }
    
    private static void adaptFlowPane(FlowPane flowPane, Breakpoint bp) {
        switch (bp) {
            case XS -> {
                flowPane.setHgap(5);
                flowPane.setVgap(5);
                flowPane.setPadding(new Insets(5));
            }
            case SM -> {
                flowPane.setHgap(8);
                flowPane.setVgap(8);
                flowPane.setPadding(new Insets(8));
            }
            case MD -> {
                flowPane.setHgap(10);
                flowPane.setVgap(10);
                flowPane.setPadding(new Insets(10));
            }
            case LG -> {
                flowPane.setHgap(15);
                flowPane.setVgap(15);
                flowPane.setPadding(new Insets(15));
            }
            case XL -> {
                flowPane.setHgap(20);
                flowPane.setVgap(20);
                flowPane.setPadding(new Insets(20));
            }
        }
    }
    
    /**
     * Creates a responsive GridPane that adjusts column count based on available width.
     */
    public static GridPane createResponsiveGrid(int baseColumns, double minColumnWidth) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("responsive-grid");
        grid.setHgap(10);
        grid.setVgap(10);
        
        grid.widthProperty().addListener((obs, old, newWidth) -> {
            int columns = Math.max(1, (int) (newWidth.doubleValue() / minColumnWidth));
            columns = Math.min(columns, baseColumns);
            updateGridColumns(grid, columns);
        });
        
        return grid;
    }
    
    private static void updateGridColumns(GridPane grid, int columns) {
        grid.getColumnConstraints().clear();
        double percentage = 100.0 / columns;
        for (int i = 0; i < columns; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(percentage);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
    }
    
    /**
     * Creates a card container that is responsive (uses FlowPane internally).
     */
    public static ScrollPane createResponsiveCardContainer() {
        FlowPane flowPane = createResponsiveFlowPane();
        flowPane.setStyle("-fx-background-color: transparent;");
        
        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("panel");
        
        return scroll;
    }
    
    /**
     * Gets the FlowPane from a responsive card container.
     */
    public static FlowPane getFlowPaneFromContainer(ScrollPane container) {
        return (FlowPane) container.getContent();
    }
    
    // ==================== ADAPTIVE SPLIT PANE ====================
    
    /**
     * Creates a SplitPane that adjusts divider position based on breakpoints.
     */
    public static SplitPane createResponsiveSplitPane(Region container) {
        SplitPane splitPane = new SplitPane();
        
        container.widthProperty().addListener((obs, old, newWidth) -> {
            Breakpoint bp = Breakpoint.fromWidth(newWidth.doubleValue());
            adaptSplitPane(splitPane, bp);
        });
        
        return splitPane;
    }
    
    private static void adaptSplitPane(SplitPane splitPane, Breakpoint bp) {
        switch (bp) {
            case XS, SM -> splitPane.setDividerPositions(0.35);
            case MD -> splitPane.setDividerPositions(0.28);
            case LG, XL -> splitPane.setDividerPositions(0.22);
        }
    }
    
    // ==================== ADAPTIVE SIZING ====================
    
    /**
     * Returns adaptive padding based on breakpoint.
     */
    public static Insets getAdaptivePadding(Breakpoint bp) {
        return switch (bp) {
            case XS -> new Insets(5);
            case SM -> new Insets(8);
            case MD -> new Insets(12);
            case LG -> new Insets(16);
            case XL -> new Insets(20);
        };
    }
    
    /**
     * Returns adaptive spacing based on breakpoint.
     */
    public static double getAdaptiveSpacing(Breakpoint bp) {
        return switch (bp) {
            case XS -> 5;
            case SM -> 8;
            case MD -> 10;
            case LG -> 15;
            case XL -> 20;
        };
    }
    
    /**
     * Returns adaptive font size multiplier based on breakpoint.
     */
    public static double getFontMultiplier(Breakpoint bp) {
        return switch (bp) {
            case XS -> 0.85;
            case SM -> 0.92;
            case MD -> 1.0;
            case LG -> 1.08;
            case XL -> 1.15;
        };
    }
    
    // ==================== VISIBILITY MANAGEMENT ====================
    
    /**
     * Makes a node visible only for certain breakpoints.
     */
    public static void showOnlyFor(Node node, ResponsiveContext ctx, Breakpoint... breakpoints) {
        ctx.breakpointProperty().addListener((obs, old, newBp) -> {
            boolean show = false;
            for (Breakpoint bp : breakpoints) {
                if (newBp == bp) {
                    show = true;
                    break;
                }
            }
            node.setVisible(show);
            node.setManaged(show);
        });
    }
    
    /**
     * Makes a node visible from a certain breakpoint and above.
     */
    public static void showFrom(Node node, ResponsiveContext ctx, Breakpoint minBreakpoint) {
        ctx.breakpointProperty().addListener((obs, old, newBp) -> {
            boolean show = newBp.ordinal() >= minBreakpoint.ordinal();
            node.setVisible(show);
            node.setManaged(show);
        });
    }
    
    /**
     * Makes a node visible up to a certain breakpoint (inclusive).
     */
    public static void showUntil(Node node, ResponsiveContext ctx, Breakpoint maxBreakpoint) {
        ctx.breakpointProperty().addListener((obs, old, newBp) -> {
            boolean show = newBp.ordinal() <= maxBreakpoint.ordinal();
            node.setVisible(show);
            node.setManaged(show);
        });
    }
    
    // ==================== ADAPTIVE LAYOUT SWITCH ====================
    
    /**
     * Interface for controlling adaptive layouts.
     */
    @FunctionalInterface
    public interface LayoutAdapter {
        void adapt(Breakpoint breakpoint);
    }
    
    /**
     * Registers a layout adapter that responds to breakpoint changes.
     */
    public static void registerLayoutAdapter(Region region, LayoutAdapter adapter) {
        ChangeListener<Number> listener = (obs, old, newWidth) -> {
            Breakpoint bp = Breakpoint.fromWidth(newWidth.doubleValue());
            adapter.adapt(bp);
        };
        region.widthProperty().addListener(listener);
        // Trigger initial layout
        adapter.adapt(Breakpoint.fromWidth(region.getWidth()));
    }
    
    // ==================== CARD SIZE HELPERS ====================
    
    /**
     * Returns the recommended card width for a breakpoint.
     */
    public static double getCardWidth(Breakpoint bp) {
        return switch (bp) {
            case XS -> 140;
            case SM -> 160;
            case MD -> 180;
            case LG -> 200;
            case XL -> 220;
        };
    }
    
    /**
     * Returns the recommended compact card height.
     */
    public static double getCompactCardHeight(Breakpoint bp) {
        return switch (bp) {
            case XS -> 50;
            case SM -> 55;
            case MD -> 60;
            case LG -> 65;
            case XL -> 70;
        };
    }
    
    // ==================== RESPONSIVE MENU BUTTONS ====================
    
    /**
     * Creates menu buttons that show icon-only on small screens, full text on larger screens.
     */
    public static Button createResponsiveButton(String text, IconUtils.Icon icon, ResponsiveContext ctx) {
        Button btn = new Button();
        Node iconNode = IconUtils.createIcon(icon, 16, "#dcdcdc");
        
        btn.setGraphic(iconNode);
        btn.getStyleClass().add("button");
        
        ctx.breakpointProperty().addListener((obs, old, newBp) -> {
            if (newBp.ordinal() >= Breakpoint.MD.ordinal()) {
                btn.setText(text);
                btn.setContentDisplay(ContentDisplay.LEFT);
            } else {
                btn.setText("");
                btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });
        
        // Initial state
        if (ctx.getBreakpoint().ordinal() >= Breakpoint.MD.ordinal()) {
            btn.setText(text);
            btn.setContentDisplay(ContentDisplay.LEFT);
        }
        
        return btn;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Creates a spacer that grows to fill available space.
     */
    public static Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
    
    /**
     * Creates a fixed-size spacer.
     */
    public static Region createFixedSpacer(double width, double height) {
        Region spacer = new Region();
        spacer.setMinWidth(width);
        spacer.setPrefWidth(width);
        spacer.setMaxWidth(width);
        spacer.setMinHeight(height);
        spacer.setPrefHeight(height);
        spacer.setMaxHeight(height);
        return spacer;
    }
    
    /**
     * Makes a node fill the available width.
     */
    public static void makeFullWidth(Node node) {
        if (node instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(r, Priority.ALWAYS);
        }
    }
    
    /**
     * Makes a node fill the available height.
     */
    public static void makeFullHeight(Node node) {
        if (node instanceof Region r) {
            r.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(r, Priority.ALWAYS);
        }
    }
}
