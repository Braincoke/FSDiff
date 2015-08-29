package com.erwandano.fsdiff.diffwindow.leftmenu;

import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.core.PathDiff;
import com.erwandano.fsdiff.diffwindow.DiffWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.MasterDetailPane;

/**
 * Controls the left side bar
 */
public class LeftMenuController extends Controller {

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
    private TreeView<PathDiff> diffTreeView;
    private DiffWindowController windowController;

    private double hiddenThreshold = 0.09;
    private double initialDividerPosition = 0.4;

    public void initialize() {
        selectedTab = explorerTab;
        leftMenuHidden = false;
    }

    /**
     * Handle tab selection and collapsing / showing the sidebar
     */
    @FXML
    public void leftMenuTabSelection(String tab) {
        //The user clicked on an already selected tab to hide or show the left menu
        if(selectedTab.getId().compareTo(tab)==0){
            if(leftMenuHidden) {
                leftMenuTabPane.getSelectionModel().getSelectedItem().getStyleClass().remove("hidden");
                showLeftMenuTab();
            } else {
                collapseLeftMenuTab();
                leftMenuTabPane.getSelectionModel().getSelectedItem().getStyleClass().add("hidden");
            }
        } else {
            //The user wants to change tabs
            switch (tab){
                case "explorer-tab":
                    leftMenuTabPane.getSelectionModel().getSelectedItem().getStyleClass().remove("hidden");
                    leftMenuTabPane.getSelectionModel().select(explorerTab);
                    selectedTab = explorerTab;
                    break;
                case "project-tab":
                    leftMenuTabPane.getSelectionModel().getSelectedItem().getStyleClass().remove("hidden");
                    leftMenuTabPane.getSelectionModel().select(projectTab);
                    selectedTab = projectTab;
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

    public DiffWindowController getWindowController() {
        return windowController;
    }

    public void setWindowController(DiffWindowController windowController) {
        this.selectedTab = this.explorerTab;
        this.leftMenuHidden = false;
        this.windowController = windowController;
        this.explorerTabController.setLeftMenuController(this);
        this.splitPane = windowController.getSplitPane();
        splitPane.setDividerPositions(initialDividerPosition);
        //Save the divider position
        this.dividerPosition = initialDividerPosition;
        this.savedDividerPositions = splitPane.getDividerPositions();
        splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
                this.dividerPosition = newValue.doubleValue();
            leftMenuHidden = this.dividerPosition < hiddenThreshold;
        });
        this.projectTabController.setLeftMenuController(this);
    }
}
