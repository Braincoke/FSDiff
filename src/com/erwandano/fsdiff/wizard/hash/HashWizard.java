package com.erwandano.fsdiff.wizard.hash;

import com.erwandano.fsdiff.Main;
import com.erwandano.fsdiff.wizard.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class HashWizard extends Wizard {


    //Controllers
    private FSListController fsListController;
    private FSChoiceController fsChoiceController;
    private HashPreparationController hashPreparationController;
    private HashGenerationController hashGenerationController;
    private RecapController recapController;


    public HashWizard(Main application){
        this.application = application;
        gotoFileSystemChoice();
    }


    /**
     * List of file systems to hash
     */
    private List<HashProject> hashProjectList;

    public List<HashProject> getHashProjectList() {
        return hashProjectList;
    }

    public void setHashProjectList(List<HashProject> hashProjectList) {
        this.hashProjectList = hashProjectList;
    }

    private Queue<HashProject> hashProjectQueue;

    public Queue<HashProject> getHashProjectQueue() {
        return hashProjectQueue;
    }

    public void setHashProjectQueue(Queue<HashProject> hashProjectQueue) {
        this.hashProjectQueue = hashProjectQueue;
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

    /**
     * List of log files
     */
    private List<FileHandler> fileHandlers;

    public List<FileHandler> getFileHandlers() {
        return fileHandlers;
    }

    public void createFileHandlers(){
        fileHandlers = new ArrayList<>();
        try {
            for (int i = 0; i < hashProjectList.size(); i++) {
                HashProject currentProject = hashProjectList.get(i);
                String log = currentProject.getOutputDirectory().resolve(currentProject.getName() + ".log").toString();
                FileHandler fh = new FileHandler(log);
                fh.setFormatter(new SimpleFormatter());
                fileHandlers.add(i, fh);
            }
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "Error creating the log files\t" + e.getMessage());
        }
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
            fsListController =
                    (FSListController) application.replaceSceneContent("wizard/hash/FSList.fxml");
            fsListController.setWizard(this);
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
            createFileHandlers();
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
