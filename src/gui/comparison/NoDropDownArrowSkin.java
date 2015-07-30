package gui.comparison;

import com.sun.javafx.scene.control.skin.MenuButtonSkin;
import javafx.scene.control.MenuButton;

/**
 * Created by Erwan Dano on 21/07/2015.
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
