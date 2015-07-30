package gui.wizard.comparison;

import core.FileSystemComparison;
import core.FileSystemHash;
import core.InputType;
import gui.Main;
import gui.Wizard;
import gui.comparison.ComparisonWindowController;

import java.nio.file.Path;

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
     * The path pointing to the COMPARED file system
     * It can be a file or a directory, for a list of acceptable
     * input types see the enum class InputType
     */
    private Path comparedFSPath;
    /**
     * The path pointing to the REFERENCE file system
     * It can be a file or a directory, for a list of acceptable
     * input types see the enum class InputType
     */
    private Path referenceFSPath;
    /**
     * The input type for the REFERENCE file system
     */
    private InputType referenceFSInputType;
    /**
     * The input type for the COMPARED file system
     */
    private InputType comparedFSInputType;
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

    public ComparisonWizard(Main application, String projectName, Path outputDirectory) throws Exception {
        this.application = application;
        this.projectName = projectName;
        this.outputDirectory = outputDirectory;
        gotoReferenceChoice();
    }

    /**
     * The output file path
     */
    public Path getOutputFilePath(){
        return outputDirectory.resolve(projectName+".fscx");
    }

    public Path getComparedFSPath() {
        return comparedFSPath;
    }

    public void setComparedFSPath(Path comparedFSPath) {
        this.comparedFSPath = comparedFSPath;
    }

    public Path getReferenceFSPath() {
        return referenceFSPath;
    }

    public void setReferenceFSPath(Path referenceFSPath) {
        this.referenceFSPath = referenceFSPath;
    }

    public InputType getReferenceFSInputType() {
        return referenceFSInputType;
    }

    public void setReferenceFSInputType(InputType referenceFSInputType) {
        this.referenceFSInputType = referenceFSInputType;
    }

    public InputType getComparedFSInputType() {
        return comparedFSInputType;
    }

    public void setComparedFSInputType(InputType comparedFSInputType) {
        this.comparedFSInputType = comparedFSInputType;
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
        if(     referenceFSInputType == InputType.LOGICAL_DIRECTORY
                && comparedFSInputType == InputType.LOGICAL_DIRECTORY){
            //We need to hash both file systems
            gotoHashPreparation();
        } else if ( referenceFSInputType == InputType.FSHX
                &&  comparedFSInputType == InputType.FSHX){
            //No need to hash anything, load the fshx files into FileSystemHash objects and produce the comparison

        } else if ( (referenceFSInputType == InputType.FSHX && comparedFSInputType == InputType.LOGICAL_DIRECTORY)
                ||  (referenceFSInputType == InputType.LOGICAL_DIRECTORY && comparedFSInputType == InputType.FSHX)
                ) {
            //We need to hash one of the file systems, go to the hash preparation
        } else {
            //TODO
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
