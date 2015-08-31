package com.erwandano.fsdiff.diffwindow.toppane;

import com.erwandano.fsdiff.Main;
import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.diffwindow.DiffTreeItem;
import com.erwandano.fsdiff.diffwindow.DiffWindowController;
import com.erwandano.fsdiff.diffwindow.leftmenu.DiffStatus;
import com.erwandano.fxcomponents.CheckBoxMenuItem;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class ToolbarController extends Controller {

    public static final double TOOLBAR_HEIGHT = 28;
    public static final double SEARCHFIELD_HEIGHT = TOOLBAR_HEIGHT-4;

    /**
     * Used for the filter icon next to the search field
     */
    static {
        // Register a custom default font
        GlyphFontRegistry.register("FontAwesome", Main.class.getClassLoader().getResourceAsStream("com/erwandano/fsdiff/resources/fontawesome.ttf"), 16);
    }

    protected GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private DiffWindowController windowController;

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

    public void setWindowController(DiffWindowController windowController){
        this.windowController = windowController;
    }

    /**
     * Search and filter files accordingly to the regex and the filter options
     * @return      The list of items filtered
     */
    public List<DiffTreeItem> search(){
        DiffTreeItem root = windowController.getRootTreeItem();
        HashMap<DiffStatus, Boolean> filterOptions = new HashMap<>();
        filterOptions.put(DiffStatus.MATCHED, filterMatched.isSelected());
        filterOptions.put(DiffStatus.MODIFIED, filterModified.isSelected());
        filterOptions.put(DiffStatus.CREATED, filterCreated.isSelected());
        filterOptions.put(DiffStatus.DELETED, filterDeleted.isSelected());
        List<DiffTreeItem> searchResult =  root.filterFiles(filterOptions, searchField.getText(), useRegex.isSelected());
        //Remove excluded files
        SortedSet<Path> excludedFiles =  windowController.getExcludedFiles();
        Iterator<DiffTreeItem> iterator = searchResult.iterator();
        while(iterator.hasNext()){
            DiffTreeItem item = iterator.next();
            excludedFiles.stream().forEach(path -> {
                if(path.compareTo(item.getPath())==0){
                    iterator.remove();
                }
            });
        }
        return searchResult;
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
     * Save the differential in the FSCX file
     */
    public void saveFSC() {
        windowController.saveFSC();
    }

}
