package gui.comparison;

import core.FSXmlHandler;
import core.FileSystemComparison;
import core.FileSystemHash;
import core.PathComparison;
import gui.Controller;
import gui.MenuBarController;
import gui.wizard.comparison.ComparisonWizard;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeSet;


public class ComparisonWindowController extends Controller {

    public static double INTERFACE_WIDTH = 1000;
    public static double INTERFACE_HEIGHT = 700;
    /**
     * The output file to save the file system comparison
     */
    private Path outputFile;
    /**
     * The comparison of file systems
     */
    private FileSystemComparison comparison;
    /**
     * The reference file system in the comparison
     */
    private FileSystemHash referenceFSH;
    /**
     * The compared file system in the comparison
     */
    private FileSystemHash comparedFSH;
    /**
     * The file selected by the user through the filter results or the file explorer
     */
    private PathComparison selectedPath;
    /**
     * The split pane separating the dataPane from the left menu
     */
    @FXML
    private SplitPane splitPane;
    /*******************************************************************************************************************
     *                                                                                                                 *
     * TOP PANE                                                                                                        *
     *                                                                                                                 *
     ******************************************************************************************************************/

    //Menubar
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuBarController menuBarController;
    //Toolbar
    @FXML
    private ToolBar toolbar;
    @FXML
    private ToolbarController toolbarController;
    //Breadcrumbs
    @FXML
    private BreadcrumbsController breadcrumbsController;
    @FXML
    private HBox breadcrumbs;
    /*******************************************************************************************************************
     *                                                                                                                 *
     * LEFT PANE                                                                                                       *
     *                                                                                                                 *
     ******************************************************************************************************************/

    //Left Menu
    @FXML
    private LeftMenuController leftMenuController;
    @FXML
    private AnchorPane leftMenu;
    private ComparisonTreeItem rootTreeItem;
    /*******************************************************************************************************************
     *                                                                                                                 *
     * CENTER PANE                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    //Data pane
    @FXML
    private AnchorPane dataPane;
    @FXML
    private DataPaneController dataPaneController;
    /*******************************************************************************************************************
     *                                                                                                                 *
     * BOTTOM PANE                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    //Bottom pane
    @FXML
    private HBox bottomPane;
    @FXML
    private BottomPaneController bottomPaneController;

    public Path getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(Path outputFile) {
        this.outputFile = outputFile;
    }

    public FileSystemComparison getFileSystemComparison() {
        return comparison;
    }

    public void setSelectedPath(ComparisonTreeItem item) {
        breadcrumbsController.updateBreadcrumbs(item);
        if (!item.isDirectory())
            dataPaneController.updateHexViewer(item);
    }

    public SplitPane getSplitPane() {
        return splitPane;
    }

    public ComparisonTreeItem getRootTreeItem() {
        return rootTreeItem;
    }

    public void search() {
        dataPaneController.updateResults(toolbarController.search());
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * INITIALIZATION                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Initialize the comparison window from the data collected from the wizard
     * @param wizard
     */
    public void initFromWizard(ComparisonWizard wizard){
        this.comparison = wizard.getComparison();
        this.outputFile = wizard.getOutputFilePath();
        this.initDirectoryTree();
        leftMenuController.setWindowController(this);
        breadcrumbsController.setWindowController(this);
        bottomPaneController.setWindowController(this);
        toolbarController.setWindowController(this);
        dataPaneController.setWindowController(this);
        menuBarController.setWindowController(this);
    }

    /**
     * Initialize the comparison window from a saved file system comparison
     * @param fscx Path to the saved comparison (in a XML format)
     */
    public void initFromXML(Path fscx){
        //TODO
        try {
            this.comparison = FSXmlHandler.loadFileSystemComparison(fscx.toString());
            this.outputFile = fscx;
            this.initDirectoryTree();
            leftMenuController.setWindowController(this);
            breadcrumbsController.setWindowController(this);
            bottomPaneController.setWindowController(this);
            toolbarController.setWindowController(this);
            dataPaneController.setWindowController(this);
            menuBarController.setWindowController(this);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Initialize the TreeView that serves as a file explorer of the comparison of the file system
     */
    public void initDirectoryTree() {
        TreeSet<PathComparison> treeSet = comparison.getComparison();
        rootTreeItem = new ComparisonTreeItem(new PathComparison(Paths.get("Root")));

        //List branch nodes
        HashMap<Path, ComparisonTreeItem> branchNodes = new HashMap<>();
        //Build the Tree
        for(PathComparison pathComparison : treeSet) {
            createNode(rootTreeItem, branchNodes, pathComparison);
        }
    }

    /**
     * Create a TreeItem node from a PathComparison and connect it to its branch
     * If the branch does not exist, it will be created and added to the list of
     * branchNodes.
     * @param root           The root node
     * @param branchNodes    The list of branch nodes
     * @param pathComparison The PathComparison object from which to create the node
     */
    private void createNode(ComparisonTreeItem root,
                            HashMap<Path, ComparisonTreeItem> branchNodes,
                            PathComparison pathComparison) {
        Path path = pathComparison.getPath();
        Path parentPath = pathComparison.getParentPath();
        ComparisonTreeItem treeItem = new ComparisonTreeItem(pathComparison);
        if(pathComparison.isDirectory()) {
            branchNodes.put(path, treeItem);
            //Is the branch node supposed to be connected to the root?
            if(parentPath==null){
                rootTreeItem.getChildren().add(treeItem);
            } else {
                //Connect to the parent directory
                if(branchNodes.get(parentPath)!=null){
                    branchNodes.get(parentPath).getChildren().add(treeItem);
                } else {
                    //The parent directory node does not exist yet, create it
                    createNode(root, branchNodes, pathComparison.getParent());
                    //Now connect to the parent directory node
                    branchNodes.get(parentPath).getChildren().add(treeItem);
                }
            }
        } else {
            //Is the leaf node supposed to be connected to the root?
            if(parentPath == null){
                rootTreeItem.getChildren().add(treeItem);
            } else {
                //Connect the leaf to the related branch
                //If the parent node does not exist yet, create it
                if(branchNodes.get(parentPath) == null) {
                    createNode(root, branchNodes, pathComparison.getParent());
                }
                //Then connect the leaf to the newly created branch
                branchNodes.get(parentPath).getChildren().add(treeItem);
            }
        }
    }


    /*******************************************************************************************************************
     * *
     * FILE IO                                                                                                         *
     * *
     ******************************************************************************************************************/

    public void openFSC() {
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

    public void saveFSC() {
        FSXmlHandler.saveToXML(comparison, getOutputFile());
    }

}
