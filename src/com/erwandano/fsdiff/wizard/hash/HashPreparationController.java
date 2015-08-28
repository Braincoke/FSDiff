package com.erwandano.fsdiff.wizard.hash;

import com.erwandano.fsdiff.core.FileCountCrawler;
import com.erwandano.fsdiff.core.FileSystemInput;
import javafx.beans.binding.Bindings;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Gather the total size of the blob to hash as well as the total number of files to hash
 * to provide progress information when hashing the files
 */
public class HashPreparationController extends HashWizardPane {

    /**
     * The total number of file visited
     */
    @FXML
    private Label fileCountPreparationLabel;

    /**
     * The total number of bytes visited
     */
    @FXML
    private Label byteCountPreparationLabel;

    /**
     * The unit (KB, MB, GB) for the byteCountPreparationLabel
     */
    @FXML
    private Label byteUnitPreparationLabel;

    /**
     * The file visitor that crawls and counts the number of files
     */
    private FileCountCrawler fileCountCrawler;

    /**
     * A 2 decimals formatter
     */
    private DecimalFormat df = new DecimalFormat("#.##");

    /**
     * The current unit used for the byteCount
     */
    private String unit = "KB";

    @FXML
    public void cancel(){
        if(fileCountCrawler!=null) {
            if(fileCountCrawler.cancel()) {
                wizard.gotoWelcomeScreen();
            } else {
                //TODO use log instead of sout
            }
        }
    }

    /**
     * Count the number of files to hash in both file systems
     * and compute the sum of each file size in bytes
     */
    public void countFiles(){
        List<HashProject> hashProjectList = wizard.getHashProjectList();
        List<FileSystemInput> inputList = new ArrayList<>();
        hashProjectList.stream().forEach(hashProject -> inputList.add(hashProject.getFileSystemInput()));
        fileCountCrawler = new FileCountCrawler(inputList);
        //Bind file count
        fileCountPreparationLabel.textProperty().bind(Bindings.convert(
                fileCountCrawler.fileCountProperty()));
        //Bind byte count
        fileCountCrawler.getByteCountProperty().addListener((observable, oldValue, newValue) ->
                updateByteCount(newValue));
        //When this is done, hash the files
        fileCountCrawler.setOnSucceeded((WorkerStateEvent event) -> {
            wizard.setByteCount(fileCountCrawler.getByteCount());
            wizard.setFileCount(fileCountCrawler.getFileCount());
            wizard.gotoHashGeneration();
        });
        fileCountCrawler.start();
    }

    /**
     * Update the number of bytes crunched and adapt unit to the new value
     * @param newValue new byte count value
     */
    private void updateByteCount(Number newValue){
        double byteCount = newValue.doubleValue();
        long kilobyte = 1024;
        double KBCount = byteCount / kilobyte;
        double displayedCount = KBCount;
        if(KBCount > 1024){
            double MBCount = KBCount / kilobyte;
            if(MBCount > 1024){
                displayedCount = MBCount / kilobyte;
                if(unit.compareTo("GB")!=0){
                    unit = "GB";
                    byteUnitPreparationLabel.setText(unit);
                }
            } else {
                displayedCount = MBCount;
                if(unit.compareTo("MB")!=0){
                    unit = "MB";
                    byteUnitPreparationLabel.setText(unit);
                }
            }
        }
        byteCountPreparationLabel.setText(String.valueOf(df.format(displayedCount)));
    }

}
