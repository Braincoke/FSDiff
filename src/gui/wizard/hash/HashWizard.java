package gui.wizard.hash;

import core.FSXmlHandler;
import core.FileSystemHash;
import core.FileSystemInput;
import gui.Main;
import gui.wizard.Wizard;

import java.nio.file.Path;

public class HashWizard extends Wizard {


    //Controllers
    private FSChoiceController fsChoiceController;
    private HashPreparationController hashPreparationController;
    private HashGenerationController hashGenerationController;
    private RecapController recapController;


    public HashWizard(Main application){
        this.application = application;
        gotoFileSystemChoice();
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     *  PROJECT SETTINGS                                                                                               *
     *                                                                                                                 *
     *******************************************************************************************************************/

    /**
     * The name given to the hashed file system
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The output directory
     */
    private Path outputDirectory;

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * The output file path
     */
    public Path getOutputFilePath(){
        if(name.endsWith(".fshx"))
            return outputDirectory.resolve(name);
        else
            return outputDirectory.resolve(name.concat(".fshx"));
    }

    /**
     * The file system input
     */
    private FileSystemInput fileSystemInput;

    public FileSystemInput getFileSystemInput() {
        return fileSystemInput;
    }

    public void setFileSystemInput(FileSystemInput fileSystemInput) {
        this.fileSystemInput = fileSystemInput;
    }

    /**
     * The result of the hash generation
     */
    private FileSystemHash fileSystemHash;

    public FileSystemHash getFileSystemHash() {
        return fileSystemHash;
    }

    public void setFileSystemHash(FileSystemHash fileSystemHash) {
        this.fileSystemHash = fileSystemHash;
    }

    /**
     * The total number of byte to hash
     * This is used to have the max value of the progress bar
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
     * This is used to have the max value of the progress bar
     */
    private long fileCount;

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * GOTO NAVIGATION : First time navigating to the pane                                                             *
     *                                                                                                                 *
     *******************************************************************************************************************/

    /**
     * Go to the panel that helps choosing the input
     */
    public void gotoFileSystemChoice() {
        try{
            fsChoiceController =
                    (FSChoiceController) application.replaceSceneContent("wizard/hash/FSChoice.fxml");
            fsChoiceController.setWizard(this);
            application.getStage().setTitle("Choose a file system to hash");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Go to the preparation panel that counts the number of files and bytes to hash
     */
    public void gotoHashPreparation(){
        try{
            hashPreparationController =
                    (HashPreparationController) application.replaceSceneContent("wizard/hash/HashPreparation.fxml");
            hashPreparationController.setWizard(this);
            application.getStage().setTitle("Preparing");
            hashPreparationController.countFiles();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Go to the hash generation panel that gives indications about the progress of the hash generation
     */
    public void gotoHashGeneration(){
        try{
            hashGenerationController =
                    (HashGenerationController) application.replaceSceneContent("wizard/hash/HashGeneration.fxml");
            hashGenerationController.setWizard(this);
            application.getStage().setTitle("Generating the hashes");
            hashGenerationController.hash();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Go to the last panel that gives a recap of the hash generation
     */
    public void gotoRecap(){
        try{
            recapController =
                    (RecapController) application.replaceSceneContent("wizard/hash/Recap.fxml");
            recapController.setWizard(this);
            application.getStage().setTitle("FSDiff");
            FSXmlHandler.saveToXML(fileSystemHash, getOutputFilePath());
            recapController.recap();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Return to the welcome screen
     */
    public void gotoWelcomeScreen() {
        try {
            application.gotoWelcomeScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
