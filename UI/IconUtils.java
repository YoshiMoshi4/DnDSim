package UI;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;
import org.kordamp.ikonli.materialdesign2.MaterialDesignU;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;

/**
 * Icon utility class providing vector icons for the UI.
 * Backed by Ikonli (org.kordamp.ikonli) and the Material Design 2 icon pack.
 */
public class IconUtils {

    // Default icon styling
    private static final String DEFAULT_COLOR = "#dcdcdc";
    private static final double DEFAULT_SIZE = 16;

    public enum Icon {
        SWORDS, PERSON, GEAR, PLUS, MINUS, TARGET, MOVE, FLAG, HEART, SHIELD, SKULL,
        DICE, PLAY, STOP, UNDO, SAVE, DELETE, EDIT, VIEW, LIGHTNING, POTION,
        CHEST, MAP, PARTY, CLOCK, INFO, WARNING, CHECK, CLOSE, BACK, MENU, FAST_FORWARD,
        SCRIPT, WRENCH, HAND, STAR, SORT_ASCENDING, SORT_DESCENDING
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
        return fromIkon(getIkon(icon), size, colorHex);
    }

    /**
     * Renders any Ikonli icon directly (from Material Design 2 or any other
     * pack jar on the classpath) without needing an {@link Icon} enum entry.
     * Use this for a one-off icon; add an {@link Icon} enum case instead if
     * the same glyph will be reused across multiple call sites.
     *
     * Browse available icons at https://kordamp.org/ikonli/cheat-sheet-materialdesign2.html
     * then import e.g. {@code org.kordamp.ikonli.materialdesign2.MaterialDesignS}
     * and call {@code IconUtils.fromIkon(MaterialDesignS.SWORD_CROSS, 20, "#dcdcdc")}.
     */
    public static Node fromIkon(Ikon ikon, double size, String colorHex) {
        FontIcon fontIcon = FontIcon.of(ikon, (int) Math.round(size), Color.web(colorHex));

        // Wrap in StackPane for the same fixed-size layout behavior callers already rely on
        StackPane wrapper = new StackPane(fontIcon);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);

        return wrapper;
    }

    private static Ikon getIkon(Icon icon) {
        return switch (icon) {
            case SWORDS -> MaterialDesignS.SWORD_CROSS;
            case PERSON -> MaterialDesignA.ACCOUNT;
            case GEAR -> MaterialDesignC.COG;
            case PLUS -> MaterialDesignP.PLUS;
            case MINUS -> MaterialDesignM.MINUS;
            case TARGET -> MaterialDesignT.TARGET;
            case MOVE -> MaterialDesignA.ARROW_ALL;
            case FLAG -> MaterialDesignF.FLAG;
            case HEART -> MaterialDesignH.HEART;
            case SHIELD -> MaterialDesignS.SHIELD;
            case SKULL -> MaterialDesignS.SKULL;
            case DICE -> MaterialDesignD.DICE_D20;
            case PLAY -> MaterialDesignP.PLAY;
            case STOP -> MaterialDesignS.STOP;
            case UNDO -> MaterialDesignU.UNDO;
            case SAVE -> MaterialDesignC.CONTENT_SAVE;
            case DELETE -> MaterialDesignD.DELETE;
            case EDIT -> MaterialDesignP.PENCIL;
            case VIEW -> MaterialDesignE.EYE;
            case LIGHTNING -> MaterialDesignL.LIGHTNING_BOLT;
            case POTION -> MaterialDesignF.FLASK;
            case CHEST -> MaterialDesignT.TREASURE_CHEST;
            case MAP -> MaterialDesignM.MAP;
            case PARTY -> MaterialDesignA.ACCOUNT_GROUP;
            case CLOCK -> MaterialDesignC.CLOCK;
            case INFO -> MaterialDesignI.INFORMATION;
            case WARNING -> MaterialDesignA.ALERT;
            case CHECK -> MaterialDesignC.CHECK;
            case CLOSE -> MaterialDesignC.CLOSE;
            case BACK -> MaterialDesignA.ARROW_LEFT;
            case MENU -> MaterialDesignM.MENU;
            case FAST_FORWARD -> MaterialDesignF.FAST_FORWARD;
            case SCRIPT -> MaterialDesignS.SCRIPT_TEXT;
            case WRENCH -> MaterialDesignW.WRENCH;
            case HAND -> MaterialDesignH.HAND_BACK_RIGHT;
            case STAR -> MaterialDesignS.STAR;
            case SORT_ASCENDING -> MaterialDesignS.SORT_ASCENDING;
            case SORT_DESCENDING -> MaterialDesignS.SORT_DESCENDING;
        };
    }

    // ===== CONVENIENCE METHODS FOR COMMON USE CASES =====

    /**
     * Creates a small icon for inline use (16px).
     */
    public static Node smallIcon(Icon icon) {
        return createIcon(icon, 16);
    }
}
