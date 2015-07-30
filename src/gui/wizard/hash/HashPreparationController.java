package gui.wizard.hash;

import core.FileCountCrawler;
import core.InputType;
import javafx.beans.binding.Bindings;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.DecimalFormat;

/**
 * Gather the total size of the blob to hash as well as the total number of files to hash
 * to provide progress information when hashing the files
 */
public class HashPreparationController extends HashWizardPane {

    /**
     * One kilobyte in bytes
     */
    private final long kilobyte = 1024;
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
        InputType fsInputType = wizard.getInputType();
        if(fsInputType == InputType.LOGICAL_DIRECTORY) {
            fileCountCrawler = new FileCountCrawler(
                    wizard.getFsPath());
        } else {
            //TODO log error and go back to welcome screen
        }
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
     * @param newValue
     */
    private void updateByteCount(Number newValue){
        double byteCount = newValue.doubleValue();
        double KBCount = byteCount/kilobyte;
        double displayedCount = KBCount;
        if(KBCount > 1024){
            double MBCount = KBCount/kilobyte;
            if(MBCount > 1024){
                displayedCount = MBCount/kilobyte;
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
