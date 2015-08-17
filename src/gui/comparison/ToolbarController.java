package gui.comparison;

import core.ComparisonStatus;
import gui.Controller;
import gui.Main;
import gui.components.CheckBoxMenuItem;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class ToolbarController extends Controller {

    public static final double TOOLBAR_HEIGHT = 28;
    public static final double SEARCHFIELD_HEIGHT = TOOLBAR_HEIGHT-4;

    /**
     * Used for the filter icon next to the search field
     */
    static {
        // Register a custom default font
        GlyphFontRegistry.register("FontAwesome", Main.class.getResourceAsStream("fontawesome.ttf"), 16);
    }

    protected GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private ComparisonWindowController windowController;

    /*******************************************************************************************************************
     * *
     * SEARCH FIELD                                                                                                    *
     * *
     ******************************************************************************************************************/
    @FXML
    private TextField searchField;
    @FXML
    private MenuButton filterDropDown;
    @FXML
    private FilterDropDownMenuItem filterMatched;
    @FXML
    private FilterDropDownMenuItem filterModified;
    @FXML
    private FilterDropDownMenuItem filterCreated;
    @FXML
    private FilterDropDownMenuItem filterDeleted;
    @FXML
    private CheckBoxMenuItem useRegex;

    public void setWindowController(ComparisonWindowController windowController){
        this.windowController = windowController;
    }

    /**
     * Search and filter files accordingly to the regex and the filter options
     * @return      The list of items filtered
     */
    public List<ComparisonTreeItem> search(){
        ComparisonTreeItem root = windowController.getRootTreeItem();
        HashMap<ComparisonStatus, Boolean> filterOptions = new HashMap<>();
        filterOptions.put(ComparisonStatus.MATCHED, filterMatched.isSelected());
        filterOptions.put(ComparisonStatus.MODIFIED, filterModified.isSelected());
        filterOptions.put(ComparisonStatus.CREATED, filterCreated.isSelected());
        filterOptions.put(ComparisonStatus.DELETED, filterDeleted.isSelected());
        return root.filterFiles(filterOptions, searchField.getText(), useRegex.isSelected());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterDropDown.setSkin(new NoDropDownArrowSkin(filterDropDown));
        filterDropDown.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILTER.getChar()));
    }


    /**
     * Trigger a search event
     */
    public void triggerSearch() {
        windowController.search();
    }



    /*******************************************************************************************************************
     *
     * TOOLBAR
     *
     ******************************************************************************************************************/

    /**
     * Open a FSCX file
     */
    public void openFSC() {
        windowController.openFSC();
    }

    /**
     * Save the comparison in the FSCX file
     */
    public void saveFSC() {
        windowController.saveFSC();
    }

}
