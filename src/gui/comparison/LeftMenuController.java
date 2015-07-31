package gui.comparison;

import core.PathComparison;
import gui.Controller;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;

/**
 * Controls the left side bar
 */
public class LeftMenuController extends Controller {
    static {
        // Register a custom default font
        GlyphFontRegistry.register("FontAwesome", Main.class.getResourceAsStream("fontawesome.ttf"), 16);
    }


    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    //Parent splitPane
    private SplitPane splitPane;

    //Left Menu - Tabs
    private boolean leftMenuHidden;
    @FXML
    private AnchorPane leftMenuPane;
    @FXML
    private TabPane leftMenuTabPane;
    @FXML
    private Tab projectTab;
    @FXML
    private ProjectTabController projectTabController;
    private Tab selectedTab;
    private double[] savedDividerPositions;
    private double dividerPosition;

    //Left Menu - Project
    private File referenceFSFile;
    private File comparedFSFile;
    @FXML
    private TextField referenceFSTextField;
    @FXML
    private TextField comparedFSTextField;
    @FXML
    private Button collapseLeftMenuButton1;


    //Left Menu - Explorer
    @FXML
    private Tab explorerTab;
    @FXML
    private ExplorerTabController explorerTabController;
    @FXML
    private MasterDetailPane explorerFilterPane;
    @FXML
    private Button collapseLeftMenuButton;
    @FXML
    private HBox explorerTabToolbar;
    @FXML
    private TreeView<PathComparison> comparisonTreeView;
    private ComparisonWindowController windowController;

    private double hiddenThreshold = 0.09;
    private double initialDividerPosition = 0.4;

    public void initialize() {
        selectedTab = explorerTab;
        leftMenuHidden = false;
    }

    /**
     * Handle tab selection and collapsing / showing the sidebar
     * @param event
     */
    @FXML
    public void leftMenuTabSelection(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String selectedTab = leftMenuTabPane.getSelectionModel().getSelectedItem().getId();
        String selectedBtn = btn.getId();
        int end = selectedBtn.length() - "Button".length();
        selectedBtn = selectedBtn.substring(0, end);
        //The user clicked on an already selected tab
        if(selectedBtn.compareTo(selectedTab) == 0) {
            if(leftMenuHidden)
                showLeftMenuTab();
            else
                collapseLeftMenuTab();
            //The user clicked to change tabs
        } else {
            switch(selectedBtn) {
                case "projectTab":
                    leftMenuTabPane.getSelectionModel().select(projectTab);
                    break;
                case "explorerTab":
                    leftMenuTabPane.getSelectionModel().select(explorerTab);
                    break;
            }
            showLeftMenuTab();
        }
    }

    @FXML
    public void collapseLeftMenuTab() {
        savedDividerPositions = splitPane.getDividerPositions();
        splitPane.setDividerPositions(0);
    }

    public void showLeftMenuTab() {
        if(leftMenuHidden && savedDividerPositions[0]>hiddenThreshold) {
            splitPane.setDividerPositions(savedDividerPositions);
        }else if (leftMenuHidden && savedDividerPositions[0]<hiddenThreshold) {
            splitPane.setDividerPositions(initialDividerPosition);
        }else {
            splitPane.setDividerPositions(dividerPosition);
        }
    }

    public ComparisonWindowController getWindowController() {
        return windowController;
    }

    public void setWindowController(ComparisonWindowController windowController) {
        this.windowController = windowController;
        this.explorerTabController.setLeftMenuController(this);
        this.splitPane = windowController.getSplitPane();
        splitPane.setDividerPositions(initialDividerPosition);
        //Save the divider position
        this.dividerPosition = initialDividerPosition;
        this.savedDividerPositions = splitPane.getDividerPositions();
        splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
                this.dividerPosition = newValue.doubleValue();
                if(this.dividerPosition<hiddenThreshold)
                    leftMenuHidden = true;
                else
                    leftMenuHidden = false;
        });
        this.projectTabController.setWindowController(this.windowController);
    }
}
