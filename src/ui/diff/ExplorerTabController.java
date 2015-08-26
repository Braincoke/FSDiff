package ui.diff;

import core.DiffStatus;
import core.PathDiff;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.MasterDetailPane;
import ui.Controller;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Controller for the tab showing the directory tree
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
    private TreeView<PathDiff> diffTreeView;
    private DiffTreeItem rootTreeItem;
    private DiffTreeItem filteredRootTreeItem;
    private DiffWindowController windowController;
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
        this.diffTreeView.setRoot(rootTreeItem);
        this.diffTreeView.setShowRoot(false);
        this.diffTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                DiffTreeItem treeItem = (DiffTreeItem) diffTreeView.getSelectionModel().getSelectedItem();
                windowController.setSelectedPath(treeItem);
            }
        });
        //Use the custom cell factory
        diffTreeView.setCellFactory(p -> new PathDiffTreeCell());
    }

    public void collapseDirectoryTree() {
        DiffTreeItem root = (DiffTreeItem) diffTreeView.getRoot();
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
        HashMap<DiffStatus, Boolean> filterOptions = new HashMap<>();
        filterOptions.put(DiffStatus.MATCHED, showMatched.isSelected());
        filterOptions.put(DiffStatus.MODIFIED, showModified.isSelected());
        filterOptions.put(DiffStatus.CREATED, showCreated.isSelected());
        filterOptions.put(DiffStatus.DELETED, showDeleted.isSelected());
        filteredRootTreeItem = rootTreeItem.filter(filterOptions);
        removeExcludedFiles(filteredRootTreeItem);
        diffTreeView.setRoot(filteredRootTreeItem);
        toggleFilterPanel();
    }

    private void removeExcludedFiles(DiffTreeItem root){
        if(root.getChildren().size()>0){
            Iterator<TreeItem<PathDiff>> iterator = root.getChildren().iterator();
            while(iterator.hasNext()){
                TreeItem<PathDiff> treeItem = iterator.next();
                DiffTreeItem item = (DiffTreeItem) treeItem;
                if(item.isDirectory()){
                    removeExcludedFiles(item);
                } else {
                    windowController.getExcludedFiles()
                            .stream()
                            .filter(excludedFile -> excludedFile.compareTo(item.getPath()) == 0)
                            .forEach(excludedFile -> iterator.remove());
                }
            }
        }
    }
}