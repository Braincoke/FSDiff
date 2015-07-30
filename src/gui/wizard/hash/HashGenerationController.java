package gui.wizard.hash;

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
 * Hash the file system and show the progress on the UI
 */
public class HashGenerationController extends HashWizardPane {

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
    @FXML
    private Label elapsedTimeLabel;
    @FXML
    private Button cancel;
    /**
     * The output of the hash generation
     */
    private FileSystemHash fsh;
    /**
     * The total number of files
     */
    private long fileCount;
    /**
     * The sum of file size of visited files, in bytes
     */
    private double fileByteCount;
    /**
     * The hash crawler currently crawling
     */
    private HashCrawler crawler;
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
        if(crawler !=null)
            crawler.cancel();
        if(timer!=null)
            timer.cancel();
        wizard.gotoWelcomeScreen();
    }

    /**
     * Start the hash generation
     */
    public void hash(){
        initUI();
        //Init the FileSystemHash objects
        fsh = new FileSystemHash(wizard.getFsPath(), wizard.getName());
        HashCrawler hashCrawler = fsh.getHashCrawler();
        //Set up a timer to show elapsed time
        seconds = 0;
        timer=new Timer();
        timer.scheduleAtFixedRate(new ElapsedTimeTask(), 1000, 1000);
        //When the hash generation is over, continue with the recap panel
        hashCrawler.stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue== Worker.State.SUCCEEDED){
                wizard.setFileSystemHash(fsh);
                timer.cancel();
                wizard.gotoRecap();
            }
        });
        //Start crawling
        bindProperties(hashCrawler);
        crawler = hashCrawler;
        fsh.computeHashes();
    }

    /**
     * Init progress information
     */
    private void initUI(){
        fileCount = wizard.getFileCount();
        fileByteCount = wizard.getByteCount();
        fileCountLabel.setText(String.valueOf(fileCount));
        //Init byte count
        updateByteCount(byteCountLabel, byteCountUnit, "KB", fileByteCount, 0);

    }


    /**
     * Init UI bindings
     */
    private void bindProperties(HashCrawler crawler){
        fileSystemHashedName.setText(wizard.getFsPath().toString());
        fileVisitedText.textProperty().bind(crawler.getVisitedFileProperty());
        hashedFileCountLabel.textProperty().bind(Bindings.convert(crawler.getHashedFileCountProperty()));
        crawler.getHashedByteCountProperty().addListener((observable, oldValue, newValue) -> {
            updateProgressBar(newValue, 0);
            updateHashedByteCount(newValue, 0);
        });
    }

    private void updateHashedByteCount(Number newValue, double offset){
        updateByteCount(hashedByteCountLabel, hashedByteCountUnit, unit, newValue, offset);
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
