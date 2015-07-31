package gui.comparison;

import core.ComparisonStatus;
import core.PathComparison;
import gui.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.MasterDetailPane;

import java.util.HashMap;


/**
 */
public class ExplorerTabController extends Controller {

    public static final double TOOLBAR_ICON_SIZE = 18;
    @FXML
    private MasterDetailPane explorerFilterPane;
    @FXML
    private Button collapseLeftMenuButton;
    @FXML
    private HBox explorerTabToolbar;
    //DetailPane
    @FXML
    private CheckBox showMatched;
    @FXML
    private CheckBox showModified;
    @FXML
    private CheckBox showCreated;
    @FXML
    private CheckBox showDeleted;
    @FXML
    private Button cancel;
    @FXML
    private Button apply;
    //MasterPane
    @FXML
    private TreeView<PathComparison> comparisonTreeView;
    private ComparisonTreeItem rootTreeItem;
    private ComparisonTreeItem filteredRootTreeItem;
    private ComparisonWindowController windowController;
    private LeftMenuController leftMenuController;
    private boolean filterPaneHidden = true;


    @FXML
    public void collapseLeftMenuTab() {
        leftMenuController.collapseLeftMenuTab();
    }

    public void setLeftMenuController(LeftMenuController leftMenuController) {
        this.leftMenuController = leftMenuController;
        this.windowController = leftMenuController.getWindowController();
        this.rootTreeItem = windowController.getRootTreeItem();
        this.comparisonTreeView.setRoot(rootTreeItem);
        this.comparisonTreeView.setShowRoot(false);
        this.comparisonTreeView.setOnMouseClicked(event -> {
            if(event.getClickCount()>1){
                ComparisonTreeItem treeItem = (ComparisonTreeItem) comparisonTreeView.getSelectionModel().getSelectedItem();
                windowController.setSelectedPath(treeItem);
            }
        });
        //Use the custom cell factory
        comparisonTreeView.setCellFactory(p -> new PathComparisonTreeCell());
    }

    public void collapseDirectoryTree() {
        ComparisonTreeItem root = (ComparisonTreeItem) comparisonTreeView.getRoot();
        root.collapseBranch();
    }

    public void toggleFilterPanel() {
        if (filterPaneHidden) {
            filterPaneHidden = false;
            explorerFilterPane.showDetailNodeProperty().setValue(true);
        } else {
            filterPaneHidden = true;
            explorerFilterPane.showDetailNodeProperty().setValue(false);
        }
    }

    /**
     * Modify the view of the file tree explorer according to the selected
     * values of the filter
     */
    public void filterDirectoryTree() {
        if(showMatched.isSelected()
                && showModified.isSelected()
                && showCreated.isSelected()
                && showDeleted.isSelected()){
            comparisonTreeView.setRoot(rootTreeItem);
        } else {
            HashMap<ComparisonStatus, Boolean> filterOptions = new HashMap<>();
            filterOptions.put(ComparisonStatus.MATCHED, showMatched.isSelected());
            filterOptions.put(ComparisonStatus.MODIFIED, showModified.isSelected());
            filterOptions.put(ComparisonStatus.CREATED, showCreated.isSelected());
            filterOptions.put(ComparisonStatus.DELETED, showDeleted.isSelected());
            filteredRootTreeItem = rootTreeItem.filter(filterOptions);
            comparisonTreeView.setRoot(filteredRootTreeItem);
        }
        toggleFilterPanel();
    }
}