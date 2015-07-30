package gui.comparison;

import core.ComparisonStatus;
import core.FSXmlHandler;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Erwan Dano on 10/07/2015.
 */
public class ToolbarController extends ToolBar implements Initializable{

    public static final double TOOLBAR_HEIGHT = 28;
    public static final double SEARCHFIELD_HEIGHT = TOOLBAR_HEIGHT-4;

    static {
        // Register a custom default font
        GlyphFontRegistry.register("FontAwesome", Main.class.getResourceAsStream("fontawesome.ttf"), 16);
    }

    protected GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private ComparisonWindowController windowController;
    /****************************************************
     *
     * Search field
     *
     ****************************************************/
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

    public void setWindowController(ComparisonWindowController windowController){
        this.windowController = windowController;
    }

    /**
     * Search and filter files accordingly to the regex and the filter options
     * @return
     */
    public List<ComparisonTreeItem> search(){
        ComparisonTreeItem root = windowController.getRootTreeItem();
        HashMap<ComparisonStatus, Boolean> filterOptions = new HashMap<>();
        filterOptions.put(ComparisonStatus.MATCHED, filterMatched.isSelected());
        filterOptions.put(ComparisonStatus.MODIFIED, filterModified.isSelected());
        filterOptions.put(ComparisonStatus.CREATED, filterCreated.isSelected());
        filterOptions.put(ComparisonStatus.DELETED, filterDeleted.isSelected());
        return root.filterFiles(filterOptions, searchField.getText());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterDropDown.setSkin(new NoDropDownArrowSkin(filterDropDown));
        filterDropDown.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILTER.getChar()));
    }

    public double getToolbarHeight(){
        return TOOLBAR_HEIGHT;
    }

    /**
     * Trigger a search event
     * @param actionEvent
     */
    public void triggerSearch(ActionEvent actionEvent) {
        windowController.search();
    }



    /*******************************************************************************************************************
     *
     * TOOLBAR
     *
     ******************************************************************************************************************/

    public void openFSC(ActionEvent actionEvent) {
        Main application = windowController.getApplication();
        double width = application.getStage().getWidth();
        double height = application.getStage().getHeight();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File system comparison", "*.fscx"));
        File file = fileChooser.showOpenDialog(application.getStage());
        if (file != null) {
            ComparisonWindowController comparisonWindowController;
            try {
                comparisonWindowController = (ComparisonWindowController) application.replaceSceneContent("comparison/ComparisonWindow.fxml");
                application.getStage().setWidth(width);
                application.getStage().setHeight(height);
                comparisonWindowController.setApplication(application);
                comparisonWindowController.initFromXML(file.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFSC(ActionEvent actionEvent) {
        FSXmlHandler.saveToXML(windowController.getFileSystemComparison(), windowController.getOutputFile());
    }

}
