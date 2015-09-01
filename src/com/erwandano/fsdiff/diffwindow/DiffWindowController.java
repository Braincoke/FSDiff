package com.erwandano.fsdiff.diffwindow;

import com.erwandano.fsdiff.Main;
import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.core.DiffStatus;
import com.erwandano.fsdiff.core.FileSystemDiff;
import com.erwandano.fsdiff.core.InputType;
import com.erwandano.fsdiff.core.PathDiff;
import com.erwandano.fsdiff.diffwindow.bottompane.BottomPaneController;
import com.erwandano.fsdiff.diffwindow.datapane.DataPaneController;
import com.erwandano.fsdiff.diffwindow.leftmenu.explorertab.ExplorerTabController;
import com.erwandano.fsdiff.diffwindow.leftmenu.projecttab.ProjectTabController;
import com.erwandano.fsdiff.diffwindow.toppane.BreadcrumbsController;
import com.erwandano.fsdiff.diffwindow.toppane.MenuBarController;
import com.erwandano.fsdiff.diffwindow.toppane.ToolbarController;
import com.erwandano.fsdiff.loaders.FSCXLoader;
import com.erwandano.fsdiff.loaders.XMLHandler;
import com.erwandano.fsdiff.wizard.diff.DiffWizard;
import com.erwandano.fxcomponents.control.SplitTab;
import com.erwandano.fxcomponents.control.SplitTabPane;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ProgressDialog;
import org.jdom2.JDOMException;

import java.io.*;
import java.net.URL;
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

    public void setSelectedPath(DiffTreeItem item) {
        if(item!=null && item!=rootTreeItem) {
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
     * SplitTabPane                                                                                                    *
     *                                                                                                                 *
     ******************************************************************************************************************/

    @FXML
    private SplitTabPane splitTabPane;

    public SplitTabPane getSplitTabPane(){
        return this.splitTabPane;
    }

    /* Left menu */
    @FXML
    private SplitTab explorerTab;
    @FXML
    private ExplorerTabController explorerTabController;

    @FXML
    private SplitTab projectTab;
    @FXML
    private ProjectTabController projectTabController;

    private DiffTreeItem rootTreeItem;

    public DiffTreeItem getRootTreeItem() {
        return rootTreeItem;
    }


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

    public void search(DiffStatus status) {
        HashMap<DiffStatus, Boolean> filterOptions = new HashMap<>();
        filterOptions.put(DiffStatus.MATCHED, status==DiffStatus.MATCHED);
        filterOptions.put(DiffStatus.MODIFIED, status==DiffStatus.MODIFIED);
        filterOptions.put(DiffStatus.CREATED, status==DiffStatus.CREATED);
        filterOptions.put(DiffStatus.DELETED,status==DiffStatus.DELETED);
        List<DiffTreeItem> searchResult =  rootTreeItem.filterFiles(filterOptions, "", false);
        //Remove excluded files
        Iterator<DiffTreeItem> iterator = searchResult.iterator();
        while(iterator.hasNext()){
            DiffTreeItem item = iterator.next();
            excludedFiles.stream().forEach(path -> {
                if(path.compareTo(item.getPath())==0){
                    iterator.remove();
                }
            });
        }
        dataPaneController.updateResults(searchResult);
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * INITIALIZATION                                                                                                  *
     *                                                                                                                 *
     ******************************************************************************************************************/
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        splitTabPane.showTabPane();
    }


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
        projectTabController.setWindowController(this);
        explorerTabController.setWindowController(this);
        breadcrumbsController.setWindowController(this);
        bottomPaneController.setWindowController(this);
        toolbarController.setWindowController(this);
        dataPaneController.setWindowController(this);
        menuBarController.setWindowController(this);
        application.getStage().setTitle("FSDiff - " + outputFile.getFileName().toString());
        Task<Void> checkRoots = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(DiffWindowController.this::checkRootPaths);
                return null;
            }
        };
        Thread thread = new Thread(checkRoots);
        thread.setDaemon(true);
        progressIndicator.setProgress(-1f);
        checkRoots.setOnSucceeded(event -> progressIndicator.setProgress(0));
        thread.start();
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
            createNode(branchNodes, pathDiff);
        }
    }

    /**
     * Create a TreeItem node from a PathDiff and connect it to its branch
     * If the branch does not exist, it will be created and added to the list of
     * branchNodes.
     * @param branchNodes    The list of branch nodes
     * @param pathDiff The PathDiff object from which to create the node
     */
    private void createNode(HashMap<Path, DiffTreeItem> branchNodes,
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
                    createNode(branchNodes, pathDiff.getParent());
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
                    createNode(branchNodes, pathDiff.getParent());
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
        for(int i = 0; i<5; i++){
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
                File file = filePath.toFile();
                if(!file.isDirectory()) {
                    FileInputStream is = new FileInputStream(file);
                    is.close();
                }
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
                        + fileSystemPath + ")."
                        + "\nClick on the notification to rebase.";

        Notifications notificationBuilder = Notifications.create()
                .text(message)
                .hideAfter(Duration.seconds(30))
                .position(Pos.TOP_RIGHT)
                .owner(application.getStage());
        notificationBuilder.onAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Rebase the path for the " + fileSystem + " file system" );
            File file = directoryChooser.showDialog(application.getStage());
            if(file!=null){
                rebaseRootDirectory(isReference, file.getAbsolutePath());
            }
        });
        notificationBuilder.showWarning();
    }

    public void rebaseRootDirectory(boolean isReference, String newPath) {
        if(isReference){
            diff.getReferenceFS().setRootPath(newPath);
        } else {
            diff.getComparedFS().setRootPath(newPath);
        }
    }

    public void collapseLeftTab() {
        splitTabPane.collapseTabPane();
    }
}
