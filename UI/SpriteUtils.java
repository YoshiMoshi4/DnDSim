package UI;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import EntityRes.CharSheet;
import Objects.Enemy;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and caching character/enemy sprites.
 * Provides fallback rendering when sprites are not available.
 */
public class SpriteUtils {
    
    private static final Map<String, Image> spriteCache = new HashMap<>();
    private static final String SPRITES_BASE_PATH = "resources/sprites/";
    
    // Standard sprite size (32x32)
    public static final int SPRITE_SIZE = 32;
    
    /**
     * Load a sprite image from the given path.
     * Caches images to avoid reloading.
     * 
     * @param spritePath Relative path from resources/sprites/ (e.g., "party/henry.png")
     * @return The loaded Image, or null if not found
     */
    public static Image loadSprite(String spritePath) {
        if (spritePath == null || spritePath.isEmpty()) {
            return null;
        }
        
        // Check cache first
        if (spriteCache.containsKey(spritePath)) {
            return spriteCache.get(spritePath);
        }
        
        // Build full path
        String fullPath = SPRITES_BASE_PATH + spritePath;
        File imageFile = new File(fullPath);
        
        if (!imageFile.exists()) {
            // Also try without the base path (in case spritePath is already full)
            imageFile = new File(spritePath);
            if (!imageFile.exists()) {
                System.out.println("Sprite not found: " + fullPath);
                spriteCache.put(spritePath, null); // Cache the miss to avoid repeated lookups
                return null;
            }
        }
        
        try {
            Image image = new Image(new FileInputStream(imageFile));
            spriteCache.put(spritePath, image);
            return image;
        } catch (Exception e) {
            System.out.println("Error loading sprite: " + e.getMessage());
            spriteCache.put(spritePath, null);
            return null;
        }
    }
    
    /**
     * Create an ImageView for a character's sprite.
     * Falls back to a colored circle icon if no sprite is available.
     * 
     * @param charSheet The character sheet
     * @param size The desired display size
     * @return An ImageView or fallback Node
     */
    public static javafx.scene.Node createCharacterSprite(CharSheet charSheet, int size) {
        Image sprite = loadSprite(charSheet.getSpritePath());
        
        if (sprite != null) {
            ImageView view = new ImageView(sprite);
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setPreserveRatio(true);
            view.setSmooth(false); // Keep pixel art crisp
            return view;
        }
        
        // Fallback: colored circle
        return createFallbackAvatar(charSheet.getColor(), size, true);
    }
    
    /**
     * Create an ImageView for an enemy's sprite.
     * Falls back to a colored circle icon if no sprite is available.
     * 
     * @param enemy The enemy
     * @param size The desired display size
     * @return An ImageView or fallback Node
     */
    public static javafx.scene.Node createEnemySprite(Enemy enemy, int size) {
        Image sprite = loadSprite(enemy.getSpritePath());
        
        if (sprite != null) {
            ImageView view = new ImageView(sprite);
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setPreserveRatio(true);
            view.setSmooth(false); // Keep pixel art crisp
            return view;
        }
        
        // Fallback: colored circle with enemy styling
        return createFallbackAvatar(enemy.getColor(), size, false);
    }
    
    /**
     * Create a fallback avatar when no sprite is available.
     * Uses a styled circular region with a color based on the entity's color index.
     */
    public static javafx.scene.Node createFallbackAvatar(int colorIndex, int size, boolean isParty) {
        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        avatar.setMinSize(size, size);
        avatar.setMaxSize(size, size);
        
        String hexColor = CharSheet.getColorHex(colorIndex);
        String borderColor = isParty ? "#4CAF50" : "#d75f5f";
        
        avatar.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: %d; " +
            "-fx-border-color: %s; " +
            "-fx-border-radius: %d; " +
            "-fx-border-width: 2;",
            hexColor, size / 2, borderColor, size / 2
        ));
        
        return avatar;
    }
    
    /**
     * Draw a sprite on a canvas GraphicsContext.
     * Used by BattleGridCanvas for rendering entities on the grid.
     * 
     * @param gc The graphics context
     * @param spritePath Path to the sprite
     * @param x X coordinate (center)
     * @param y Y coordinate (center)
     * @param size Size to draw
     * @param fallbackColor Fallback color if sprite not found
     * @param isParty Whether this is a party member (affects fallback border)
     */
    public static void drawSpriteOnCanvas(GraphicsContext gc, String spritePath, 
            double x, double y, double size, Color fallbackColor, boolean isParty) {
        Image sprite = loadSprite(spritePath);
        
        if (sprite != null) {
            // Draw the sprite centered at x, y
            double drawX = x - size / 2;
            double drawY = y - size / 2;
            gc.drawImage(sprite, drawX, drawY, size, size);
        } else {
            // Fallback to colored circle
            double radius = size / 2;
            gc.setFill(fallbackColor);
            gc.fillOval(x - radius, y - radius, size, size);
            
            // Add border based on entity type
            gc.setStroke(isParty ? Color.web("#4CAF50") : Color.web("#d75f5f"));
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, size, size);
        }
    }
    
    /**
     * Clear the sprite cache. Useful when sprites are updated externally.
     */
    public static void clearCache() {
        spriteCache.clear();
    }
    
    /**
     * Check if a sprite exists for the given path.
     */
    public static boolean spriteExists(String spritePath) {
        if (spritePath == null || spritePath.isEmpty()) {
            return false;
        }
        
        // Check cache first
        if (spriteCache.containsKey(spritePath)) {
            return spriteCache.get(spritePath) != null;
        }
        
        // Check file existence
        String fullPath = SPRITES_BASE_PATH + spritePath;
        File imageFile = new File(fullPath);
        return imageFile.exists();
    }
}
