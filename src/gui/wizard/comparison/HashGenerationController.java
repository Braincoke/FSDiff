package gui.wizard.comparison;

import core.FileSystemHash;
import core.HashCrawler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Hash the reference and compared file systems and show the progress on the UI
 */
public class HashGenerationController extends ComparisonWizardPane {

    /**
     * A GigaByte in bytes (= 1024 * 1024 * 1024)
     */
    private final int gigabyte = 1073741824;
    /**
     * The size of a kilobyte in bytes
     */
    private final long kilobyte = 1024;
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
     * The sum of file size of visited files, in bytes
     */
    private double fileByteCount;
    /**
     * The number of files hashed in the reference file system so far
     */
    private int referenceHashedFileCount = 0;
    /**
     * The count of bytes hashed in the reference file system so far
     */
    private double referenceHashedByteCount = 0;
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
     * Start the hash generation
     */
    public void hash(){
        //Init generic UI nodes
        int fileCount = wizard.getFileCount();
        fileByteCount = wizard.getByteCount();
        fileCountLabel.setText(String.valueOf(fileCount));
        initByteCount(fileByteCount);
        progressBar.setProgress(0);
        //Init the FileSystemHash objects
        referenceFSH = new FileSystemHash(wizard.getReferenceFSPath());
        comparedFSH = new FileSystemHash(wizard.getComparedFSPath());
        HashCrawler referenceCrawler = referenceFSH.getHashCrawler();
        HashCrawler comparedCrawler = comparedFSH.getHashCrawler();
        //Set up a timer to show elapsed time
        seconds = 0;
        timer=new Timer();
        timer.scheduleAtFixedRate(new ElapsedTimeTask(), 1000, 1000);
        //When the first file system is hashed continue with the compared
        referenceCrawler.stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue== Worker.State.SUCCEEDED){
                wizard.setReferenceFSH(referenceFSH);
                referenceHashedFileCount = referenceCrawler.getHashedFileCount();
                referenceHashedByteCount = referenceCrawler.getHashedByteCount();
                //Bind UI for comparedCrawler
                bindForCompared(comparedCrawler);
                currentCrawler = comparedCrawler;
                //Start crawling when ready
                comparedFSH.computeHashes();
            }
        });
        //When the compared FS is hashed display the comparison interface
        comparedCrawler.stateProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue== Worker.State.SUCCEEDED){
                timer.cancel();
                wizard.setComparedFSH(comparedFSH);
                wizard.goToComparisonProgress();
            }
        }));
        //Start crawling
        bindForReference(referenceCrawler);
        currentCrawler = referenceCrawler;
        referenceFSH.computeHashes();
    }



    /**
     * Init UI binding for the reference FS crawler
     */
    private void bindForReference(HashCrawler referenceCrawler){
        fileSystemHashedName.setText(wizard.getReferenceFSPath().toString());
        fileVisitedText.textProperty().bind(referenceCrawler.getVisitedFileProperty());
        hashedFileCountLabel.textProperty().bind(Bindings.convert(referenceCrawler.getHashedFileCountProperty()));
        referenceCrawler.getHashedByteCountProperty().addListener((observable, oldValue, newValue) -> {
            updateProgressBar(newValue, 0);
            updateHashedByteCount(newValue, 0);
        });
    }

    /**
     * Init UI bindings for the compared FS crawler
     */
    private void bindForCompared(HashCrawler comparedCrawler){
        fileSystemHashedName.setText(wizard.getComparedFSPath().toString());
        fileVisitedText.textProperty().bind(comparedCrawler.getVisitedFileProperty());
        //Bind hashed file count
        hashedFileCountLabel.textProperty().unbind();
        comparedCrawler.getHashedFileCountProperty().addListener((observable, oldValue, newValue) -> {
            hashedFileCountLabel.setText(String.valueOf(referenceHashedFileCount + newValue.intValue()));
        });
        //Bind hashed byte count
        comparedCrawler.getHashedByteCountProperty().addListener((observable, oldValue, newValue) -> {
            updateProgressBar(newValue, referenceHashedByteCount);
            updateHashedByteCount(newValue, referenceHashedByteCount);
        });
    }

    private void updateHashedByteCount(Number newValue, double offset){
        updateByteCount(hashedByteCountLabel, hashedByteCountUnit, unit, newValue, offset);
    }

    private void initByteCount(Number byteCount){
        updateByteCount(byteCountLabel, byteCountUnit, "KB", byteCount, 0);
    }

    /**
     * Update the number of bytes crunched and adapt unit to the new value
     * @param newValue
     */
    private void updateByteCount(Label byteCountLabel, Label byteCountUnit, String unit, Number newValue, double offset){
        double byteCount = newValue.longValue() + offset;
        double KBCount = byteCount/kilobyte;
        double displayedCount = KBCount;
        if(KBCount > 1024){
            double MBCount = KBCount/kilobyte;
            if(MBCount > 1024){
                displayedCount = MBCount/kilobyte;
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
