package ui.diff;

import core.DiffStatus;
import core.FileSystemDiff;
import core.InputType;
import core.PathDiff;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import loaders.FSCXLoader;
import loaders.XMLHandler;
import org.controlsfx.dialog.ProgressDialog;
import org.jdom2.JDOMException;
import ui.Controller;
import ui.Main;
import ui.wizard.diff.DiffWizard;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;


public class DiffWindowController extends Controller {

    public static double INTERFACE_WIDTH = 1000;
    public static double INTERFACE_HEIGHT = 700;




    /*******************************************************************************************************************
     *                                                                                                                 *
     * PARAMETERS                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/


    /**
     * The output file to save the file system comparison
     */
    private Path outputFile;

    public Path getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(Path outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * The comparison of file systems
     */
    private FileSystemDiff diff;

    public FileSystemDiff getFileSystemDiff() {
        return diff;
    }


    /**
     * The list of file to exclude from the display
     */
    private SortedSet<Path> excludedFiles;

    public SortedSet<Path> getExcludedFiles() {
        return excludedFiles;
    }

    /**
     * Reset the list of excluded files according to the list of exclusion files
     */
    public void resetExcludedFiles() {
        try {
            for (Path exclusionFile : exclusionFiles) {
                loadExclusionFile(exclusionFile);
            }
        } catch (IOException e){
            Main.logger.log(Level.WARNING, "Error loading exclusion file. " + e.getMessage());
        }
    }

    /**
     * The list of .txt files containing lists of files to exclude
     */
    private List<Path> exclusionFiles;

    public List<Path> getExclusionFiles() {
        return exclusionFiles;
    }

    public void loadExclusionFile(Path exclusionFile) throws IOException {
        File file = new File(exclusionFile.toUri());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                excludedFiles.add(Paths.get(line));
            }
        }
    }

    /**
     * The split pane separating the dataPane from the left menu
     */
    @FXML
    private SplitPane splitPane;

    public SplitPane getSplitPane() {
        return splitPane;
    }

    public void setSelectedPath(DiffTreeItem item) {
        if(item!=null) {
            breadcrumbsController.updateBreadcrumbs(item);
            if (!item.isDirectory())
                dataPaneController.updateHexViewer(item);
        }
    }


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
    private DiffTreeItem rootTreeItem;

    public DiffTreeItem getRootTreeItem() {
        return rootTreeItem;
    }

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

    public void search() {
        dataPaneController.updateResults(toolbarController.search());
    }
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

    private ProgressIndicator progressIndicator;

    public void setProgressIndicator(ProgressIndicator progressIndicator){
        this.progressIndicator = progressIndicator;
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * INITIALIZATION                                                                                                  *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Initialize the comparison window from the data collected from the wizard
     * @param wizard   The comparison wizard used to start the project
     */
    public void initFromWizard(DiffWizard wizard){
        this.diff = wizard.getDiff();
        this.outputFile = wizard.getOutputFilePath();
        endInit();
        saveFSC();
    }

    /**
     * Initialize the comparison window from a saved file system comparison
     * @param fscx Path to the saved comparison (in a XML format)
     */
    public void initFromXML(Path fscx){
        try {
            this.diff = XMLHandler.loadFileSystemComparison(fscx.toString());
            this.outputFile = fscx;
            endInit();
        } catch (JDOMException | IOException e) {
            Main.logger.log(Level.WARNING, "Could not load FSCX file", e);
        }
    }

    /**
     * Initialise the comparison window from a FileSystemDiff object
     * @param comparison    The object holding the comparison
     * @param fscx          Path to the saved comparison or the output file
     */
    public void initWindow(FileSystemDiff comparison, String fscx){
        this.diff = comparison;
        this.outputFile = Paths.get(fscx);
        endInit();
    }

    /**
     * End the window initialisation
     */
    public void endInit(){
        this.exclusionFiles = new ArrayList<>();
        this.excludedFiles = new TreeSet<>();
        this.initDirectoryTree();
        leftMenuController.setWindowController(this);
        breadcrumbsController.setWindowController(this);
        bottomPaneController.setWindowController(this);
        toolbarController.setWindowController(this);
        dataPaneController.setWindowController(this);
        menuBarController.setWindowController(this);
        application.getStage().setTitle("FSDiff - " + outputFile.getFileName().toString());
        //FIXME checkRootPaths(); Replace annoying pop up by discrete notification
    }


    /**
     * Initialize the TreeView that serves as a file explorer of the comparison of the file system
     */
    public void initDirectoryTree() {
        TreeSet<PathDiff> treeSet = diff.getDiff();
        rootTreeItem = new DiffTreeItem(new PathDiff(Paths.get("Root")));

        //List branch nodes
        HashMap<Path, DiffTreeItem> branchNodes = new HashMap<>();
        //Build the Tree
        for(PathDiff pathDiff : treeSet) {
            createNode(rootTreeItem, branchNodes, pathDiff);
        }
    }

    /**
     * Create a TreeItem node from a PathDiff and connect it to its branch
     * If the branch does not exist, it will be created and added to the list of
     * branchNodes.
     * @param root           The root node
     * @param branchNodes    The list of branch nodes
     * @param pathDiff The PathDiff object from which to create the node
     */
    private void createNode(DiffTreeItem root,
                            HashMap<Path, DiffTreeItem> branchNodes,
                            PathDiff pathDiff) {
        Path path = pathDiff.getPath();
        Path parentPath = pathDiff.getParentPath();
        DiffTreeItem treeItem = new DiffTreeItem(pathDiff);
        if(pathDiff.isDirectory()) {
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
                    createNode(root, branchNodes, pathDiff.getParent());
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
                    createNode(root, branchNodes, pathDiff.getParent());
                }
                //Then connect the leaf to the newly created branch
                branchNodes.get(parentPath).getChildren().add(treeItem);
            }
        }
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * FILE IO                                                                                                         *
     *                                                                                                                 *
     ******************************************************************************************************************/

    public void openFSC() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File system comparison", "*.fscx"));
        File file = fileChooser.showOpenDialog(application.getStage());
        if (file != null) {
            FSCXLoader loader = new FSCXLoader(file.getPath());
            ProgressDialog progressDialog = new ProgressDialog(loader);
            progressDialog.setTitle("Loading");
            progressDialog.setContentText("Please wait while your file is loading");
            progressIndicator.progressProperty().bind(loader.progressProperty());
            loader.setOnSucceeded(event -> {
                try {
                    this.initWindow(loader.getValue(), file.getPath());
                } catch (Exception e) {
                    Main.logger.log(Level.WARNING, "Failed to load the .fscx file : " + file.getPath());
                }
            });
            loader.start();
        }
    }

    public void saveFSC() {
        XMLHandler.saveToXML(diff, getOutputFile());
    }


    /**
     * Verify if the root paths for the reference file system and the compared one are still pointing to the correct
     * directory. If not the user will be asked to rebase the faulty root.
     */
    public void checkRootPaths(){
        Path referenceRootPath = diff.getReferenceFS().getRootPath();
        Path comparedRootPath = diff.getComparedFS().getRootPath();
        InputType referenceInputType = diff.getReferenceFS().getInputType();
        InputType comparedInputType = diff.getComparedFS().getInputType();

        //If the input type was a directory, check if that directory still exists and if we can find files in it
        if(referenceInputType==InputType.LOGICAL_DIRECTORY)
            checkDirectory(true);
        if(comparedInputType==InputType.LOGICAL_DIRECTORY)
            checkDirectory(false);
    }

    /**
     * Verify if a directory and its files are still readable
     * @param isReference   Indicate if we are performing the check for the reference file system
     */
    public void checkDirectory(boolean isReference){
        //Indicate if we have to rebase the directory
        boolean rebase = false;
        //To check if the directory is still present we try to open some files
        int length = diff.getDiff().size();
        ArrayList<PathDiff> list = new ArrayList<>(diff.getDiff());
        int randomFile;
        Path filePath;
        for(int i = 0; i<3; i++){
            do {
                randomFile = (int) Math.floor((Math.random() * length));
                filePath = list.get(randomFile).getPath();
            } while((isReference && list.get(randomFile).getStatus()== DiffStatus.CREATED)
                || (!isReference && list.get(randomFile).getStatus()== DiffStatus.DELETED)
                    );
            try {
                if(isReference){
                    filePath = diff.getReferenceFS().getRootPath().resolve(filePath);
                } else {
                    filePath = diff.getComparedFS().getRootPath().resolve(filePath);
                }
                //FIXME it appears the user may be asked to rebase the directory even if
                //FIXME it should not be the case. The error happens randomly (probably for restricted access files)
                FileInputStream is = new FileInputStream(filePath.toString());
                is.close();
            } catch (FileNotFoundException e) {
                rebase = true;
            } catch (IOException e) {
                Main.logger.log(Level.WARNING,"Error when closing the file : " + filePath.toString(),e);
            }
        }
        //Now we ask the user if he wants to rebase the directory
        if(rebase)
            rebaseDirectory(isReference);
    }

    /**
     * We prompt the user an interface to rebase the directory
     * @param isReference   Indicate which file system we have to rebase
     */
    private void rebaseDirectory(boolean isReference) {
        String fileSystem;
        String fileSystemPath;
        String message;
        if(isReference){
            fileSystem = "reference";
            fileSystemPath = diff.getReferenceFS().getRootPath().toString();

        } else {
            fileSystem = "compared";
            fileSystemPath = diff.getComparedFS().getRootPath().toString();
        }
        message ="The path to the " + fileSystem + " file system seems to be obsolete ("
                        + fileSystemPath + ")";
        message += "\nClick \"Rebase\" to rebase the root directory or \"Ignore\" to ignore.";
        message += "\n\nNote that you will not be able to load the hex dumps if you do not rebase the directory.";
        ButtonType rebase = new ButtonType("Rebase", ButtonBar.ButtonData.OK_DONE);
        ButtonType ignore = new ButtonType("Ignore", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert warningAlert = new Alert(Alert.AlertType.WARNING, message, ignore, rebase);
        Optional<ButtonType> result = warningAlert.showAndWait();
        if(result.get()==rebase){
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Rebase the path for the " + fileSystem + " file system" );
            File file = directoryChooser.showDialog(application.getStage());
            if(file!=null){
                if(isReference){
                    diff.getReferenceFS().setRootPath(file.getAbsolutePath());
                } else {
                    diff.getComparedFS().setRootPath(file.getAbsolutePath());
                }
            }
        }
    }

    public void rebaseRootDirectory(boolean isReference, String newPath) {
        if(isReference){
            diff.getReferenceFS().setRootPath(newPath);
        } else {
            diff.getComparedFS().setRootPath(newPath);
        }
    }
}
