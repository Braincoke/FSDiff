package gui.wizard.comparison;

import core.FileSystemComparison;
import core.FileSystemHash;
import core.FileSystemInput;
import gui.Main;
import gui.comparison.ComparisonWindowController;
import gui.loaders.LoadingController;
import gui.wizard.Wizard;
import loaders.FSHXLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

/**
 * A Wizard to guide the user in the project configuration.
 * It helps the user choose a reference and compared file system
 * and show progress of the hash generation when hashing.
 * The ComparisonWizard manages the general flow and keep the project settings up to date.
 */
public class ComparisonWizard extends Wizard{

    //Controllers
    private ReferenceFSChoiceController referenceChoiceController;
    private ComparedFSChoiceController comparedChoiceController;
    private HashPreparationController hashPreparationController;
    private HashGenerationController hashGenerationController;
    private ComparisonProgressController comparisonProgressController;
    private ComparisonWindowController comparisonWindowController;


    public ComparisonWizard(Main application, String projectName, Path outputDirectory) throws Exception {
        this.application = application;
        this.projectName = projectName;
        this.outputDirectory = outputDirectory;
        this.hashList = new ArrayList<>();
        this.hashQueue = new LinkedList<>();
        this.fshxList = new ArrayList<>();
        this.fshxQueue = new LinkedList<>();
        this.currentPath = outputDirectory;
        gotoReferenceChoice();
    }


    /******************************************************************************************************************
     *                                                                                                                *
     * PROJECT SETTINGS                                                                                               *
     *                                                                                                                *
     *****************************************************************************************************************/

    /**
     * The project name
     */
    private String projectName;

    /**
     * The output directory for the project file
     */
    private Path outputDirectory;

    /**
     * The output file path
     */
    public Path getOutputFilePath(){
        return outputDirectory.resolve(projectName+".fscx");
    }

    /**
     * The reference file system input
     */
    private FileSystemInput referenceInput;

    public FileSystemInput getReferenceInput() {
        return referenceInput;
    }

    public void setReferenceInput(FileSystemInput referenceInput) {
        this.referenceInput = referenceInput;
    }

    /**
     * The compared file system input
     */
    private FileSystemInput comparedInput;

    public FileSystemInput getComparedInput() {
        return comparedInput;
    }

    public void setComparedInput(FileSystemInput comparedInput) {
        this.comparedInput = comparedInput;
    }

    /**
     * The file system hash of the REFERENCE file system
     */
    private FileSystemHash referenceFSH;

    public FileSystemHash getReferenceFSH() {
        return referenceFSH;
    }

    public void setReferenceFSH(FileSystemHash referenceFSH) {
        this.referenceFSH = referenceFSH;
    }

    /**
     * The file system hash of the COMPARED file system
     */
    private FileSystemHash comparedFSH;

    public FileSystemHash getComparedFSH() {
        return comparedFSH;
    }

    public void setComparedFSH(FileSystemHash comparedFSH) {
        this.comparedFSH = comparedFSH;
    }

    /**
     * The result of the COMPARISON of the reference and compared file systems
     */
    private FileSystemComparison comparison;

    public FileSystemComparison getComparison() {
        return comparison;
    }

    public void setComparison(FileSystemComparison comparison) {
        this.comparison = comparison;
    }

    /**
     * The total number of bytes crunched when hashing the files
     * This is used to update the progress bar
     */
    private long byteCount;

    public long getByteCount() {
        return byteCount;
    }

    public void setByteCount(long byteCount) {
        this.byteCount = byteCount;
    }

    /**
     * The total number of files to hash
     * This is used to update the progress bar
     */
    private int fileCount;

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    /**
     * The list of file system to hash
     */
    private List<FileSystemInput> hashList;

    public List<FileSystemInput> getHashList() {
        return hashList;
    }

    private Queue<FileSystemInput> hashQueue;

    public Queue<FileSystemInput> getHashQueue() {
        return hashQueue;
    }

    /**
     * The list of file systems to load from a XML file
     */
    private List<FileSystemInput> fshxList;

    public List<FileSystemInput> getFshxList() {
        return fshxList;
    }

    private Queue<FileSystemInput> fshxQueue;

    public Queue<FileSystemInput> getFshxQueue() {
        return fshxQueue;
    }

    /******************************************************************************************************************
     *                                                                                                                *
     * GOTO NAVIGATION : First time navigating to the pane                                                            *
     *                                                                                                                *
     ******************************************************************************************************************/

    /**
     * The last path used by the user
     */
    private Path currentPath;

    public Path getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath;
    }

    public void gotoReferenceChoice(){
        if(referenceChoiceController==null){
            referenceChoiceController = new ReferenceFSChoiceController();
        }
        referenceChoiceController = (ReferenceFSChoiceController) gotoWizardPane(
                "wizard/comparison/FSChoice.fxml",
                            referenceChoiceController,
                            "Choose a reference FS");
    }

    public void gotoComparedChoice(){
        if(comparedChoiceController==null)
            comparedChoiceController = new ComparedFSChoiceController();
        comparedChoiceController = (ComparedFSChoiceController) gotoWizardPane(
                "wizard/comparison/FSChoice.fxml",
                comparedChoiceController,
                "Choose a compared FS"
        );
    }

    /**
     * When the file systems are selected, branch between different possibilities and choose the next UI to show.
     * Example :
     *  ref = Logical dir && comp == Logical dir -> gotoHashPreparation()
     */
    public void chooseComparisonPreparation(){
        if (hashQueue.size() > 0) {
            gotoHashPreparation();
        } else if(fshxQueue.size() > 0) {
            loadFSHX();
        } else {
            goToComparisonProgress();
        }
    }

    public void loadFSHX(){
        LoadingController loadingController = new LoadingController();
        FSHXLoader loader = loadingController.getFSHXLoader();
        FileSystemInput fsi = fshxQueue.poll();
        try {
            application.replaceSceneContent("loaders/Loading.fxml", loadingController);
            loadingController.setApplication(application);
            //Go to comparison interface when loaded
            loader.setOnSucceeded(event -> {
                if (fsi.isReference()){
                    referenceFSH = loader.getValue();
                } else {
                    comparedFSH = loader.getValue();
                }
                if(fshxQueue.size()>0){
                    loadFSHX();
                } else {
                    chooseComparisonPreparation();
                }
            });
            loadingController.loadFSHX(fsi.getPath().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFileSystemHash(Path path, boolean isReference){
        try {
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, "Could not load the file : " + path.toString(),e);
        }
    }

    public void enqueue() {
        switch (referenceInput.getInputType()) {
            case LOGICAL_DIRECTORY:
                hashList.add(referenceInput);
                hashQueue.add(referenceInput);
                break;
            case FSHX:
                fshxList.add(referenceInput);
                fshxQueue.add(referenceInput);
                break;
        }
        switch (comparedInput.getInputType()) {
            case LOGICAL_DIRECTORY:
                hashList.add(comparedInput);
                hashQueue.add(comparedInput);
                break;
            case FSHX:
                fshxList.add(comparedInput);
                fshxQueue.add(comparedInput);
                break;
        }
    }

    public void gotoHashPreparation(){
        hashPreparationController = (HashPreparationController) gotoWizardPane(
                "wizard/comparison/HashPreparation.fxml",
                "Preparing"
        );
        hashPreparationController.countFiles();
    }

    public void gotoHashProgress(){

        hashGenerationController = (HashGenerationController) gotoWizardPane(
                "wizard/comparison/HashGeneration.fxml",
                "Generating hashes"
        );
        hashGenerationController.hash();
    }

    public void goToComparisonProgress(){
        comparisonProgressController = (ComparisonProgressController) gotoWizardPane(
                "wizard/comparison/ComparisonProgress.fxml",
                "Comparing the file systems"
                );
        comparisonProgressController.compare();
    }

    public void gotoWelcomeScreen() {
        try {
            application.gotoWelcomeScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gotoComparisonInterface() {
        try {
            comparisonWindowController =
                    (ComparisonWindowController) application.replaceSceneContent("comparison/ComparisonWindow.fxml");
            application.getStage().setWidth(ComparisonWindowController.INTERFACE_WIDTH);
            application.getStage().setHeight(ComparisonWindowController.INTERFACE_HEIGHT);
            application.getStage().setTitle(projectName);
            comparisonWindowController.setApplication(application);
            comparisonWindowController.initFromWizard(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
