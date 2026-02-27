package UI;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.StackPane;

/**
 * Icon utility class providing SVG-based vector icons for the UI.
 * Uses Material Design and FontAwesome-style SVG paths.
 */
public class IconUtils {

    // Default icon styling
    private static final String DEFAULT_COLOR = "#dcdcdc";
    private static final double DEFAULT_SIZE = 16;

    // ===== SVG PATH DATA =====
    
    // Crossed swords - Battle/Combat
    private static final String SWORDS_PATH = "M6.92 5H5L14 14L15 13L6.92 5M19.96 19.12L19.12 19.96L16 16.84L15.09 17.75L16.17 18.83L15.07 19.93L13.97 18.83L12.87 19.93L11.75 18.81L11.77 18.8L10.67 17.7L9.57 18.8L8.47 17.7L9.57 16.6L6.04 13.07L4.04 15.07L2.93 13.96L4.93 11.96L5.64 12.67L9.17 9.14L8.11 8.08L9.17 7L10.23 8.06V8.07L11.35 9.19L11.36 9.18L12.46 10.28L13.57 9.18L14.67 10.28L13.59 11.36L16.12 13.89L17.23 12.78L18.33 13.89L17.25 14.97L20 17.72L21.07 16.65L18.33 13.91L17.29 12.87L19.93 10.23L21.03 11.33L19.53 12.83L19.96 19.12M14.97 17.25L15.07 17.15L14.05 16.13L13.96 16.22L14.97 17.25Z";
    
    // Person silhouette - Character
    private static final String PERSON_PATH = "M12 4C14.21 4 16 5.79 16 8C16 10.21 14.21 12 12 12C9.79 12 8 10.21 8 8C8 5.79 9.79 4 12 4M12 14C16.42 14 20 15.79 20 18V20H4V18C4 15.79 7.58 14 12 14Z";
    
    // Gear/Cog - Settings/Editor
    private static final String GEAR_PATH = "M12 15.5C10.07 15.5 8.5 13.93 8.5 12S10.07 8.5 12 8.5 15.5 10.07 15.5 12 13.93 15.5 12 15.5M19.43 12.97C19.47 12.65 19.5 12.33 19.5 12C19.5 11.67 19.47 11.34 19.43 11L21.54 9.37C21.73 9.22 21.78 8.95 21.66 8.73L19.66 5.27C19.54 5.05 19.27 4.96 19.05 5.05L16.56 6.05C16.04 5.66 15.5 5.32 14.87 5.07L14.5 2.42C14.46 2.18 14.25 2 14 2H10C9.75 2 9.54 2.18 9.5 2.42L9.13 5.07C8.5 5.32 7.96 5.66 7.44 6.05L4.95 5.05C4.73 4.96 4.46 5.05 4.34 5.27L2.34 8.73C2.21 8.95 2.27 9.22 2.46 9.37L4.57 11C4.53 11.34 4.5 11.67 4.5 12C4.5 12.33 4.53 12.65 4.57 12.97L2.46 14.63C2.27 14.78 2.21 15.05 2.34 15.27L4.34 18.73C4.46 18.95 4.73 19.03 4.95 18.95L7.44 17.94C7.96 18.34 8.5 18.68 9.13 18.93L9.5 21.58C9.54 21.82 9.75 22 10 22H14C14.25 22 14.46 21.82 14.5 21.58L14.87 18.93C15.5 18.67 16.04 18.34 16.56 17.94L19.05 18.95C19.27 19.03 19.54 18.95 19.66 18.73L21.66 15.27C21.78 15.05 21.73 14.78 21.54 14.63L19.43 12.97Z";
    
    // Plus sign - Add
    private static final String PLUS_PATH = "M19 13H13V19H11V13H5V11H11V5H13V11H19V13Z";
    
    // Crosshair/Target - Attack
    private static final String TARGET_PATH = "M12 8C14.21 8 16 9.79 16 12S14.21 16 12 16 8 14.21 8 12 9.79 8 12 8M12 2C12.55 2 13 2.45 13 3V4.07C16.39 4.56 19 7.47 19 11H20C20.55 11 21 11.45 21 12S20.55 13 20 13H19C19 16.53 16.39 19.44 13 19.93V21C13 21.55 12.55 22 12 22S11 21.55 11 21V19.93C7.61 19.44 5 16.53 5 13H4C3.45 13 3 12.55 3 12S3.45 11 4 11H5C5 7.47 7.61 4.56 11 4.07V3C11 2.45 11.45 2 12 2M12 6C8.69 6 6 8.69 6 12S8.69 18 12 18 18 15.31 18 12 15.31 6 12 6Z";
    
    // Arrow/Movement - Move
    private static final String MOVE_PATH = "M13 6V11H18V7.75L22.25 12L18 16.25V13H13V18H16.25L12 22.25L7.75 18H11V13H6V16.25L1.75 12L6 7.75V11H11V6H7.75L12 1.75L16.25 6H13Z";
    
    // Flag - End Turn
    private static final String FLAG_PATH = "M14.4 6L14 4H5V21H7V14H12.6L13 16H20V6H14.4Z";
    
    // Heart - Health/HP
    private static final String HEART_PATH = "M12 21.35L10.55 20.03C5.4 15.36 2 12.27 2 8.5C2 5.41 4.42 3 7.5 3C9.24 3 10.91 3.81 12 5.08C13.09 3.81 14.76 3 16.5 3C19.58 3 22 5.41 22 8.5C22 12.27 18.6 15.36 13.45 20.03L12 21.35Z";
    
    // Shield - Defense/Armor
    private static final String SHIELD_PATH = "M12 1L3 5V11C3 16.55 6.84 21.74 12 23C17.16 21.74 21 16.55 21 11V5L12 1Z";
    
    // Skull - Enemy/Defeat
    private static final String SKULL_PATH = "M12 2C6.47 2 2 6.5 2 12C2 14.25 2.81 16.3 4.16 17.87L4 18C4 19.1 4.9 20 6 20L6 21C6 21.55 6.45 22 7 22H9C9.55 22 10 21.55 10 21V20H14V21C14 21.55 14.45 22 15 22H17C17.55 22 18 21.55 18 21V20C19.1 20 20 19.1 20 18L19.84 17.87C21.19 16.3 22 14.25 22 12C22 6.5 17.53 2 12 2M8.5 14C7.67 14 7 13.33 7 12.5S7.67 11 8.5 11 10 11.67 10 12.5 9.33 14 8.5 14M15.5 14C14.67 14 14 13.33 14 12.5S14.67 11 15.5 11 17 11.67 17 12.5 16.33 14 15.5 14Z";
    
    // Dice D20 - Roll/Random
    private static final String DICE_PATH = "M12 2L1.5 9.64L5.5 22H18.5L22.5 9.64L12 2M12 5.31L17.74 9.5H6.26L12 5.31M6.93 11.5H17.07L18.56 16.5H5.44L6.93 11.5M7 18.5H17L16 20H8L7 18.5Z";
    
    // Play arrow - Start/Begin
    private static final String PLAY_PATH = "M8 5V19L19 12L8 5Z";
    
    // Stop square - Stop/End
    private static final String STOP_PATH = "M6 6H18V18H6V6Z";
    
    // Undo arrow - Undo
    private static final String UNDO_PATH = "M12.5 8C9.85 8 7.45 9.01 5.6 10.6L2 7V16H11L7.38 12.38C8.77 11.22 10.54 10.5 12.5 10.5C16.04 10.5 19.05 12.81 20.1 16L22.47 15.22C21.08 11.03 17.15 8 12.5 8Z";
    
    // Save/Floppy disk
    private static final String SAVE_PATH = "M15 9H5V5H15M12 19C10.34 19 9 17.66 9 16S10.34 13 12 13 15 14.34 15 16 13.66 19 12 19M17 3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V7L17 3Z";
    
    // Trash/Delete
    private static final String DELETE_PATH = "M19 4H15.5L14.5 3H9.5L8.5 4H5V6H19M6 19C6 20.1 6.9 21 8 21H16C17.1 21 18 20.1 18 19V7H6V19Z";
    
    // Edit/Pencil
    private static final String EDIT_PATH = "M20.71 7.04C21.1 6.65 21.1 6 20.71 5.63L18.37 3.29C18 2.9 17.35 2.9 16.96 3.29L15.12 5.12L18.87 8.87M3 17.25V21H6.75L17.81 9.93L14.06 6.18L3 17.25Z";
    
    // Eye - View/Visibility
    private static final String VIEW_PATH = "M12 9C10.34 9 9 10.34 9 12S10.34 15 12 15 15 13.66 15 12 13.66 9 12 9M12 17C8.13 17 4.83 14.69 3 12C4.83 9.31 8.13 7 12 7S19.17 9.31 21 12C19.17 14.69 15.87 17 12 17M12 4.5C7 4.5 2.73 7.61 1 12C2.73 16.39 7 19.5 12 19.5S21.27 16.39 23 12C21.27 7.61 17 4.5 12 4.5Z";
    
    // Lightning bolt - Initiative/Speed
    private static final String LIGHTNING_PATH = "M11 15H6L13 1V9H18L11 23V15Z";
    
    // Potion/Flask - Consumable
    private static final String POTION_PATH = "M6 22C5.45 22 5 21.55 5 21V11.97C5 11.14 5.28 10.36 5.76 9.72L9 5V3H8V1H16V3H15V5L18.24 9.72C18.72 10.36 19 11.14 19 11.97V21C19 21.55 18.55 22 18 22H6M11 5V7L9.19 9.5H14.81L13 7V5H11Z";
    
    // Chest/Loot
    private static final String CHEST_PATH = "M5 4H19C20.1 4 21 4.9 21 6V10H3V6C3 4.9 3.9 4 5 4M3 12H21V18C21 19.1 20.1 20 19 20H5C3.9 20 3 19.1 3 18V12M11 14V16H13V14H11Z";
    
    // Map/Terrain
    private static final String MAP_PATH = "M15 5.1L9 3L3 5.02V20.79L9 18.9L15 21L21 18.98V3.21L15 5.1M15 18.9L9 16.98V5.1L15 7.02V18.9Z";
    
    // Users/Party
    private static final String PARTY_PATH = "M16 17V19H2V17S2 13 9 13 16 17 16 17M12.5 7.5C12.5 5.57 10.93 4 9 4S5.5 5.57 5.5 7.5 7.07 11 9 11 12.5 9.43 12.5 7.5M15.94 13C17.56 14.16 18.5 15.71 18.5 17.5V19H22V17S22 13.37 15.94 13M15 4C16.93 4 18.5 5.57 18.5 7.5S16.93 11 15 11C14.46 11 13.94 10.87 13.47 10.65C14.13 9.75 14.5 8.67 14.5 7.5S14.13 5.25 13.47 4.35C13.94 4.13 14.46 4 15 4Z";
    
    // Clock/Timer
    private static final String CLOCK_PATH = "M12 2C6.5 2 2 6.5 2 12S6.5 22 12 22 22 17.5 22 12 17.5 2 12 2M12.5 13H11V7H12.5V11.7L16.2 9.2L17 10.4L12.5 13Z";
    
    // Info circle
    private static final String INFO_PATH = "M12 2C6.48 2 2 6.48 2 12S6.48 22 12 22 22 17.52 22 12 17.52 2 12 2M13 17H11V11H13V17M13 9H11V7H13V9Z";
    
    // Warning triangle
    private static final String WARNING_PATH = "M13 14H11V10H13M13 18H11V16H13M1 21H23L12 2L1 21Z";
    
    // Check/Success
    private static final String CHECK_PATH = "M21 7L9 19L3.5 13.5L4.91 12.09L9 16.17L19.59 5.59L21 7Z";
    
    // X/Close/Cancel
    private static final String CLOSE_PATH = "M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12L19 6.41Z";
    
    // Arrow left - Back
    private static final String BACK_PATH = "M20 11H7.83L13.42 5.41L12 4L4 12L12 20L13.41 18.59L7.83 13H20V11Z";
    
    // Menu hamburger
    private static final String MENU_PATH = "M3 6H21V8H3V6M3 11H21V13H3V11M3 16H21V18H3V16Z";

    // ===== ICON FACTORY METHODS =====

    public enum Icon {
        SWORDS, PERSON, GEAR, PLUS, TARGET, MOVE, FLAG, HEART, SHIELD, SKULL,
        DICE, PLAY, STOP, UNDO, SAVE, DELETE, EDIT, VIEW, LIGHTNING, POTION,
        CHEST, MAP, PARTY, CLOCK, INFO, WARNING, CHECK, CLOSE, BACK, MENU
    }

    /**
     * Creates an icon node with default size and color.
     */
    public static Node createIcon(Icon icon) {
        return createIcon(icon, DEFAULT_SIZE, DEFAULT_COLOR);
    }

    /**
     * Creates an icon node with specified size and default color.
     */
    public static Node createIcon(Icon icon, double size) {
        return createIcon(icon, size, DEFAULT_COLOR);
    }

    /**
     * Creates an icon node with specified size and color.
     */
    public static Node createIcon(Icon icon, double size, String colorHex) {
        String pathData = getPathData(icon);
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(pathData);
        svgPath.setFill(Color.web(colorHex));
        
        // Scale to desired size (SVGs are typically 24x24)
        double scale = size / 24.0;
        svgPath.setScaleX(scale);
        svgPath.setScaleY(scale);
        
        // Wrap in StackPane for proper sizing
        StackPane wrapper = new StackPane(svgPath);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        
        return wrapper;
    }

    /**
     * Creates a colored icon with glow effect (for highlighted states).
     */
    public static Node createGlowIcon(Icon icon, double size, String colorHex) {
        String pathData = getPathData(icon);
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(pathData);
        svgPath.setFill(Color.web(colorHex));
        
        // Add glow effect
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(Color.web(colorHex));
        glow.setRadius(8);
        glow.setSpread(0.4);
        svgPath.setEffect(glow);
        
        double scale = size / 24.0;
        svgPath.setScaleX(scale);
        svgPath.setScaleY(scale);
        
        StackPane wrapper = new StackPane(svgPath);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        
        return wrapper;
    }

    private static String getPathData(Icon icon) {
        return switch (icon) {
            case SWORDS -> SWORDS_PATH;
            case PERSON -> PERSON_PATH;
            case GEAR -> GEAR_PATH;
            case PLUS -> PLUS_PATH;
            case TARGET -> TARGET_PATH;
            case MOVE -> MOVE_PATH;
            case FLAG -> FLAG_PATH;
            case HEART -> HEART_PATH;
            case SHIELD -> SHIELD_PATH;
            case SKULL -> SKULL_PATH;
            case DICE -> DICE_PATH;
            case PLAY -> PLAY_PATH;
            case STOP -> STOP_PATH;
            case UNDO -> UNDO_PATH;
            case SAVE -> SAVE_PATH;
            case DELETE -> DELETE_PATH;
            case EDIT -> EDIT_PATH;
            case VIEW -> VIEW_PATH;
            case LIGHTNING -> LIGHTNING_PATH;
            case POTION -> POTION_PATH;
            case CHEST -> CHEST_PATH;
            case MAP -> MAP_PATH;
            case PARTY -> PARTY_PATH;
            case CLOCK -> CLOCK_PATH;
            case INFO -> INFO_PATH;
            case WARNING -> WARNING_PATH;
            case CHECK -> CHECK_PATH;
            case CLOSE -> CLOSE_PATH;
            case BACK -> BACK_PATH;
            case MENU -> MENU_PATH;
        };
    }

    // ===== CONVENIENCE METHODS FOR COMMON USE CASES =====

    /**
     * Creates a button-sized icon (24px).
     */
    public static Node buttonIcon(Icon icon) {
        return createIcon(icon, 24);
    }

    /**
     * Creates a button-sized icon with custom color.
     */
    public static Node buttonIcon(Icon icon, String colorHex) {
        return createIcon(icon, 24, colorHex);
    }

    /**
     * Creates a small icon for inline use (16px).
     */
    public static Node smallIcon(Icon icon) {
        return createIcon(icon, 16);
    }

    /**
     * Creates a large icon for headers/titles (32px).
     */
    public static Node largeIcon(Icon icon) {
        return createIcon(icon, 32);
    }

    /**
     * Creates an extra-large icon for main menu (48px).
     */
    public static Node heroIcon(Icon icon) {
        return createIcon(icon, 48);
    }

    /**
     * Creates a hero icon with custom color.
     */
    public static Node heroIcon(Icon icon, String colorHex) {
        return createIcon(icon, 48, colorHex);
    }
}
