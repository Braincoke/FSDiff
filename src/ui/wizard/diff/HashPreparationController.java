package ui.wizard.diff;

import core.FileCountCrawler;
import core.FileSystemInput;
import javafx.beans.binding.Bindings;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Gather the total size of the blob to hash as well as the total number of files to hash
 * to provide progress information when hashing the files
 */
public class HashPreparationController extends DiffWizardPane {

    @FXML
    private Label fileCountPreparationLabel;
    @FXML
    private Button cancel;
    @FXML
    private Label byteCountPreparationLabel;
    @FXML
    private Label byteUnitPreparationLabel;
    private FileCountCrawler fileCountCrawler;
    private DecimalFormat df = new DecimalFormat("#.##");
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
        List<FileSystemInput> hashList = wizard.getHashList();
        fileCountCrawler = new FileCountCrawler(hashList);
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
            wizard.gotoHashProgress();
        });
        fileCountCrawler.start();
    }

    /**
     * Update the number of bytes crunched and adapt unit to the new value
     * @param newValue The new byte count value
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
