package ui.diff;

import com.sun.javafx.scene.control.skin.MenuButtonSkin;
import javafx.scene.control.MenuButton;

/**
 * A skin to hide the arrow in the MenuButton
 */
public class NoDropDownArrowSkin extends MenuButtonSkin {
    /**
     * Creates a new MenuButtonSkin for the given MenuButton
     *
     * @param menuButton the MenuButton
     */
    public NoDropDownArrowSkin(MenuButton menuButton) {
        super(menuButton);
        arrow.getStyleClass().clear();
        arrowButton.getStyleClass().clear();
    }
}
