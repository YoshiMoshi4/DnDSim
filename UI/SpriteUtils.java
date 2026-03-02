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
     * Sprites are scaled to fit the size while preserving aspect ratio.
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
            view.setPreserveRatio(true); // Keep aspect ratio, don't squish
            view.setSmooth(false); // Keep pixel art crisp
            return view;
        }
        
        // Fallback: colored circle
        return createFallbackAvatar(charSheet.getColor(), size, true);
    }
    
    /**
     * Create an ImageView for an enemy's sprite.
     * Falls back to a colored circle icon if no sprite is available.
     * Sprites are scaled to fit the size while preserving aspect ratio.
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
            view.setPreserveRatio(true); // Keep aspect ratio, don't squish
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
     * Sprites are scaled to fit the cell while preserving aspect ratio.
     * 
     * @param gc The graphics context
     * @param spritePath Path to the sprite
     * @param x X coordinate (center)
     * @param y Y coordinate (center)
     * @param size Cell size to fit within
     * @param fallbackColor Fallback color if sprite not found
     * @param isParty Whether this is a party member (affects fallback border)
     */
    public static void drawSpriteOnCanvas(GraphicsContext gc, String spritePath, 
            double x, double y, double size, Color fallbackColor, boolean isParty) {
        Image sprite = loadSprite(spritePath);
        
        if (sprite != null) {
            // Scale to fit cell while preserving aspect ratio
            double spriteWidth = sprite.getWidth();
            double spriteHeight = sprite.getHeight();
            double scale = Math.min(size / spriteWidth, size / spriteHeight);
            double drawWidth = spriteWidth * scale;
            double drawHeight = spriteHeight * scale;
            double drawX = x - drawWidth / 2;
            double drawY = y - drawHeight / 2;
            gc.drawImage(sprite, drawX, drawY, drawWidth, drawHeight);
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

    /**
     * Create a sprite node for a terrain object.
     * Falls back to a colored square if no sprite is available.
     * Sprites are scaled to fit while preserving aspect ratio.
     * 
     * @param terrain The terrain object
     * @param size The desired display size
     * @return A Node for display
     */
    public static javafx.scene.Node createTerrainSprite(Objects.TerrainObject terrain, int size) {
        Image sprite = loadSprite(terrain.getSpritePath());
        
        if (sprite != null) {
            ImageView view = new ImageView(sprite);
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setPreserveRatio(true); // Keep aspect ratio
            view.setSmooth(false);
            return view;
        }
        
        // Fallback: colored square
        return createTerrainFallback(terrain.getColor(), size);
    }

    /**
     * Create a fallback terrain display (colored square).
     */
    public static javafx.scene.Node createTerrainFallback(int colorIndex, int size) {
        javafx.scene.layout.StackPane square = new javafx.scene.layout.StackPane();
        square.setMinSize(size, size);
        square.setMaxSize(size, size);
        
        String hexColor = EntityRes.CharSheet.getColorHex(colorIndex);
        
        square.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 4; " +
            "-fx-border-color: #505052; " +
            "-fx-border-radius: 4; " +
            "-fx-border-width: 1;",
            hexColor
        ));
        
        return square;
    }

    /**
     * Draw a terrain sprite on a canvas GraphicsContext.
     * Sprites are scaled to fit the cell while preserving aspect ratio.
     */
    public static void drawTerrainSpriteOnCanvas(GraphicsContext gc, Objects.TerrainObject terrain,
            double x, double y, double cellSize) {
        Image sprite = loadSprite(terrain.getSpritePath());
        
        if (sprite != null) {
            // Scale to fit cell while preserving aspect ratio
            double spriteWidth = sprite.getWidth();
            double spriteHeight = sprite.getHeight();
            double scale = Math.min(cellSize / spriteWidth, cellSize / spriteHeight);
            double drawWidth = spriteWidth * scale;
            double drawHeight = spriteHeight * scale;
            double drawX = x + (cellSize - drawWidth) / 2;
            double drawY = y + (cellSize - drawHeight) / 2;
            gc.drawImage(sprite, drawX, drawY, drawWidth, drawHeight);
        } else {
            // Fallback to colored square
            java.awt.Color awtColor = terrain.getDisplayColor();
            Color fxColor = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
            gc.setFill(fxColor);
            gc.fillRoundRect(x + 2, y + 2, cellSize - 4, cellSize - 4, 4, 4);
            gc.setStroke(Color.web("#505052"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(x + 2, y + 2, cellSize - 4, cellSize - 4, 4, 4);
        }
    }
}
