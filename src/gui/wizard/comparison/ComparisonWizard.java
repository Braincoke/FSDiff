package gui.wizard.comparison;

import core.FSXmlHandler;
import core.FileSystemComparison;
import core.FileSystemHash;
import core.FileSystemInput;
import gui.Main;
import gui.Wizard;
import gui.comparison.ComparisonWindowController;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
    /**
     * The project name
     */
    private String projectName;


    /******************************************************************************************************************
     * PROJECT SETTINGS
     *****************************************************************************************************************/
    /**
     * The output directory for the project file
     */
    private Path outputDirectory;
    /**
     * The reference file system input
     */
    private FileSystemInput referenceInput;
    /**
     * The compared file system input
     */
    private FileSystemInput comparedInput;
    /**
     * The file system hash of the REFERENCE file system
     */
    private FileSystemHash referenceFSH;
    /**
     * The file system hash of the COMPARED file system
     */
    private FileSystemHash comparedFSH;
    /**
     * The result of the COMPARISON of the reference and compared file systems
     */
    private FileSystemComparison comparison;
    /**
     * The total number of bytes crunched when hashing the files
     * This is used to update the progress bar
     */
    private long byteCount;
    /**
     * The total number of files to hash
     * This is used to update the progress bar
     */
    private int fileCount;
    /**
     * The list of file system to hash
     */
    private List<FileSystemInput> hashList;
    /**
     * The list of file systems to load from a XML file
     */
    private List<FileSystemInput> fshxList;

    public ComparisonWizard(Main application, String projectName, Path outputDirectory) throws Exception {
        this.application = application;
        this.projectName = projectName;
        this.outputDirectory = outputDirectory;
        this.hashList = new ArrayList<>();
        this.fshxList = new ArrayList<>();
        gotoReferenceChoice();
    }

    /**
     * The output file path
     */
    public Path getOutputFilePath(){
        return outputDirectory.resolve(projectName+".fscx");
    }

    public FileSystemInput getReferenceInput() {
        return referenceInput;
    }

    public void setReferenceInput(FileSystemInput referenceInput) {
        this.referenceInput = referenceInput;
    }

    public FileSystemInput getComparedInput() {
        return comparedInput;
    }

    public void setComparedInput(FileSystemInput comparedInput) {
        this.comparedInput = comparedInput;
    }

    public FileSystemHash getReferenceFSH() {
        return referenceFSH;
    }

    public void setReferenceFSH(FileSystemHash referenceFSH) {
        this.referenceFSH = referenceFSH;
    }

    public FileSystemHash getComparedFSH() {
        return comparedFSH;
    }

    public void setComparedFSH(FileSystemHash comparedFSH) {
        this.comparedFSH = comparedFSH;
    }

    public FileSystemComparison getComparison() {
        return comparison;
    }

    public void setComparison(FileSystemComparison comparison) {
        this.comparison = comparison;
    }

    public long getByteCount() {
        return byteCount;
    }

    public void setByteCount(long byteCount) {
        this.byteCount = byteCount;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public List<FileSystemInput> getHashList() {
        return hashList;
    }

    public List<FileSystemInput> getFshxList() {
        return fshxList;
    }

    /******************************************************************************************************************
     *                                                                                                                *
     * GOTO NAVIGATION : First time navigating to the pane                                                            *
     *                                                                                                                *
     ******************************************************************************************************************/

    public void gotoReferenceChoice(){
        try{
            referenceChoiceController =
                    (ReferenceFSChoiceController) application.replaceSceneContent("wizard/comparison/ReferenceFSChoice.fxml");
            referenceChoiceController.setWizard(this);
            application.getStage().setTitle("Choose a reference FS");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void gotoComparedChoice(){
        try{
            comparedChoiceController =
                    (ComparedFSChoiceController) application.replaceSceneContent("wizard/comparison/ComparedFSChoice.fxml");
            comparedChoiceController.setWizard(this);
            application.getStage().setTitle("Choose a compared FS");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * When the file systems are selected, branch between different possibilities and choose the next UI to show.
     * Example :
     *  ref = Logical dir && comp == Logical dir -> gotoHashPreparation()
     */
    public void chooseComparisonPreparation(){
        enqueue();
        if (hashList.size() > 0) {
            gotoHashPreparation();
        } else {
            try {
                referenceFSH = FSXmlHandler.loadFileSystemHash(referenceInput.getPath().toString());
                comparedFSH = FSXmlHandler.loadFileSystemHash(comparedInput.getPath().toString());
                goToComparisonProgress();
            } catch (JDOMException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enqueue() {
        switch (referenceInput.getInputType()) {
            case LOGICAL_DIRECTORY:
                hashList.add(referenceInput);
                break;
            case FSHX:
                fshxList.add(referenceInput);
                break;
        }
        switch (comparedInput.getInputType()) {
            case LOGICAL_DIRECTORY:
                hashList.add(comparedInput);
                break;
            case FSHX:
                fshxList.add(comparedInput);
                break;
        }
    }

    public void gotoHashPreparation(){
        try {
            hashPreparationController =
                    (HashPreparationController) application.replaceSceneContent("wizard/comparison/HashPreparation.fxml");
            hashPreparationController.setWizard(this);
            application.getStage().setTitle("Preparing");
            hashPreparationController.countFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gotoHashProgress(){
        try {
            hashGenerationController =
                    (HashGenerationController) application.replaceSceneContent("wizard/comparison/HashGeneration.fxml");
            hashGenerationController.setWizard(this);
            application.getStage().setTitle("Generating hashes");
            hashGenerationController.hash();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToComparisonProgress(){
        try {
            comparisonProgressController =
                    (ComparisonProgressController) application.replaceSceneContent("wizard/comparison/ComparisonProgress.fxml");
            comparisonProgressController.setWizard(this);
            application.getStage().setTitle("Comparing the file systems");
            comparisonProgressController.compare();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            application.getStage().setHeight(ComparisonWindowController.INTERFACE_WIDTH);
            application.getStage().setTitle(projectName);
            comparisonWindowController.setApplication(application);
            comparisonWindowController.initFromWizard(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /*****************************************************************************************************************
     *  BACK TO NAVIGATION : returning to an already visited pane
     ****************************************************************************************************************/

    public void backToReferenceChoice() {

    }

}
