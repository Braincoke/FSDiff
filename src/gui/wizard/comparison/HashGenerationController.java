package gui.wizard.comparison;

import core.FileSystemHash;
import core.FileSystemInput;
import core.HashCrawler;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;

import java.text.DecimalFormat;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Hash the reference and compared file systems and show the progress on the UI
 */
public class HashGenerationController extends ComparisonWizardPane {

    @FXML
    private Label percentageLabel;
    @FXML
    private Label fileSystemHashedName;
    @FXML
    private Text fileVisitedText;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label hashedFileCountLabel;
    @FXML
    private Label fileCountLabel;
    @FXML
    private Label errorCountLabel;
    @FXML
    private Label hashedByteCountLabel;
    @FXML
    private Label hashedByteCountUnit;
    @FXML
    private Label byteCountLabel;
    @FXML
    private Label byteCountUnit;

    //Main objects
    @FXML
    private Label elapsedTimeLabel;
    @FXML
    private Button cancel;


    //Local objects
    /**
     * The output of the hash generation for the reference file system
     */
    private FileSystemHash referenceFSH;
    /**
     * The output of the hash generation for the compared file system
     */
    private FileSystemHash comparedFSH;

    /**
     * The number of visited files
     */
    private int fileCount;
    /**
     * The sum of file size of visited files, in bytes
     */
    private double fileByteCount;
    /**
     * The number of files hashed in the reference file system so far
     */
    private int previousHashedFileCount = 0;
    /**
     * The count of bytes hashed in the reference file system so far
     */
    private double previousHashedByteCount = 0;
    /**
     * The hash crawler currently crawling
     */
    private HashCrawler currentCrawler;
    /**
     * Formatter to display only to decimals
     */
    private DecimalFormat df = new DecimalFormat("#.##");

    /**
     * The current unit of the hashed byte count
     */
    private String unit = "KB";

    /**
     * The timer counting the elapsed time
     */
    private Timer timer;

    /**
     * Number of seconds elapsed
     */
    private int seconds;

    /**
     * Number of minutes elapsed
     */
    private int minutes;

    /**
     * Number of hours elapsed
     */
    private int hours;


    /**
     * Cancel the hash generation
     */
    public void cancel(){
        if(currentCrawler!=null)
            currentCrawler.cancel();
        if(timer!=null)
            timer.cancel();
        wizard.gotoWelcomeScreen();
    }

    /**
     * Init UI for hash generation
     */
    private void initUI() {
        fileCount = wizard.getFileCount();
        fileByteCount = wizard.getByteCount();
        fileCountLabel.setText(String.valueOf(fileCount));
        updateByteCount(byteCountLabel, byteCountUnit, "KB", fileByteCount, 0);
        progressBar.setProgress(0);
    }


    public void hash() {
        initUI();
        //Init list of input to hash
        Queue<FileSystemInput> hashQueue = wizard.getHashQueue();
        int queueLength = hashQueue.size();
        FileSystemInput[] fsiArray = new FileSystemInput[queueLength];
        FileSystemHash[] fshArray = new FileSystemHash[queueLength];
        HashCrawler[] hashCrawlerArray = new HashCrawler[queueLength];
        for (int i = 0; i < queueLength; i++) {
            FileSystemInput fsi = hashQueue.poll();
            fsiArray[i] = fsi;
            FileSystemHash fsh = new FileSystemHash(fsi);
            fshArray[i] = fsh;
            hashCrawlerArray[i] = fsh.getHashCrawler();
        }
        //Generate order of hash generation
        if (queueLength > 1) {
            //Link the crawlers between each other
            for (int i = 0; i < (queueLength - 1); i++) {
                final int index = i;
                hashCrawlerArray[i].stateProperty().addListener((observable, oldValue, newValue) -> {
                    //When a crawler has finished, update total file count and byte count and start the new crawler
                    if (newValue == Worker.State.SUCCEEDED) {
                        //Update the corresponding FS to the wizard
                        setFSHinWizard(fsiArray, fshArray, index);
                        previousHashedFileCount += hashCrawlerArray[index].getHashedFileCount();
                        previousHashedByteCount += hashCrawlerArray[index].getHashedByteCount();
                        if (index < (queueLength - 1)) {
                            bindUI(fshArray[index + 1]);
                            currentCrawler = hashCrawlerArray[index + 1];
                            fshArray[index + 1].computeHashes();
                        }
                    }
                });
            }
            //When the last crawler has finished, display the comparison interface
            hashCrawlerArray[queueLength - 1].stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    //Add the corresponding FS to the wizard
                    setFSHinWizard(fsiArray, fshArray, queueLength-1);
                    timer.cancel();
                    wizard.chooseComparisonPreparation();
                }
            });
        } else if (queueLength == 1){
            hashCrawlerArray[0].stateProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue == Worker.State.SUCCEEDED) {
                    //Add the corresponding FS to the wizard
                    setFSHinWizard(fsiArray, fshArray, queueLength-1);
                    timer.cancel();
                    wizard.chooseComparisonPreparation();
                }
            });
        }
        //Init for the first hash generation
        previousHashedFileCount = 0;
        previousHashedByteCount = 0;
        bindUI(fshArray[0]);
        currentCrawler = hashCrawlerArray[0];
        //Set up a timer to show elapsed time
        seconds = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new ElapsedTimeTask(), 1000, 1000);
        //Start the hash generation
        fshArray[0].computeHashes();
    }

    private void bindUI(FileSystemHash fsh) {
        HashCrawler crawler = fsh.getHashCrawler();
        fileSystemHashedName.setText(fsh.getRootPath().toString());
        fileVisitedText.textProperty().unbind();
        fileVisitedText.textProperty().bind(crawler.getVisitedFileProperty());
        //Bind hashed file count
        hashedFileCountLabel.textProperty().unbind();
        crawler.getHashedFileCountProperty().addListener((observable, oldValue, newValue) -> {
            hashedFileCountLabel.setText(String.valueOf(previousHashedFileCount + newValue.intValue()));
        });
        //Bind hashed byte count
        crawler.getHashedByteCountProperty().addListener((observable, oldValue, newValue) -> {
            updateProgressBar(newValue, previousHashedByteCount);
            updateHashedByteCount(newValue, previousHashedByteCount);
        });
    }

    private void setFSHinWizard(FileSystemInput[] fileSystemInputs, FileSystemHash[] fileSystemHashs, int index){
        if(fileSystemInputs[index].isReference()){
            wizard.setReferenceFSH(fileSystemHashs[index]);
        } else {
            wizard.setComparedFSH(fileSystemHashs[index]);
        }
    }

    private void updateHashedByteCount(Number newValue, double offset){
        updateByteCount(hashedByteCountLabel, hashedByteCountUnit, unit, newValue, offset);
    }

    /**
     * Update the number of bytes crunched and adapt unit to the new value
     * @param newByteCount The new byte count value
     */
    private void updateByteCount(Label byteCountLabel, Label byteCountUnit, String unit, Number newByteCount, double offset) {
        double byteCount = newByteCount.longValue() + offset;
        /*
      The size of a kilobyte in bytes
     */
        long kilobyte = 1024;
        double KBCount = byteCount / kilobyte;
        double displayedCount = KBCount;
        if(KBCount > 1024){
            double MBCount = KBCount / kilobyte;
            if(MBCount > 1024){
                displayedCount = MBCount / kilobyte;
                if(unit.compareTo("GB")!=0){
                    unit = "GB";
                    byteCountUnit.setText(unit);
                }
            } else {
                displayedCount = MBCount;
                if(unit.compareTo("MB")!=0){
                    unit = "MB";
                    byteCountUnit.setText(unit);
                }
            }
        }
        byteCountLabel.setText(String.valueOf(df.format(displayedCount)));
    }

    private void updateProgressBar(Number newValue, double offset){
        double percentage = (newValue.doubleValue()+offset) / fileByteCount;
        progressBar.setProgress(percentage);
        percentageLabel.setText(String.valueOf(df.format(percentage*100)));
    }

    private class ElapsedTimeTask extends TimerTask {
        @Override
        public void run() {
            Platform.runLater(() -> {
                seconds++;
                if(seconds%60==0){
                    minutes++;
                    seconds = 0;
                    if(minutes%60==0){
                        hours++;
                        minutes = 0;
                    }
                }
                elapsedTimeLabel.setText( hours + "h " + minutes + "min " + seconds + "s");
            });
        }
    }
}
