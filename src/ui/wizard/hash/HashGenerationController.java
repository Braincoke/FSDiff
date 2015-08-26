package ui.wizard.hash;

import core.FileSystemHash;
import core.HashCrawler;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import loaders.XMLHandler;
import ui.Main;
import ui.components.TextProgressBar;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Queue;
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
    private TextProgressBar progressBar;
    @FXML
    private Label fileSystemHashedName;
    @FXML
    private Text fileVisitedText;
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
     * The number of files hashed so far
     */
    private int previousHashedFileCount = 0;
    /**
     * The count of bytes hashed so far
     */
    private double previousHashedByteCount = 0;

    /**
     * The hash crawler currently crawling
     */
    private HashCrawler currentCrawler;

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

    public void hash() {
        initUI();
        //Init list of input to hash
        List<HashProject> hashProjectList = wizard.getHashProjectList();
        Queue<HashProject> hashProjectQueue = wizard.getHashProjectQueue();
        int listLength = hashProjectList.size();
        for (HashProject hashProject : hashProjectList) {
            FileSystemHash fsh = new FileSystemHash(hashProject.getFileSystemInput());
            hashProject.setFileSystemHash(fsh);
        }
        //Generate order of hash generation
        if (listLength > 1) {
            //Link the crawlers between each other
            for (int i = 0; i < (listLength - 1); i++) {
                final int index = i;
                HashProject currentProject = hashProjectList.get(i);
                HashCrawler hashCrawler = currentProject.getFileSystemHash().getHashCrawler();
                hashCrawler.stateProperty().addListener((observable, oldValue, newValue) -> {
                    //When a crawler has finished, update total file count and byte count and start the new crawler
                    if (newValue == Worker.State.SUCCEEDED) {
                        //Save the file system hash
                        XMLHandler.saveToXML(currentProject.getFileSystemHash(), currentProject.getOutputFilePath());
                        previousHashedFileCount += hashCrawler.getHashedFileCount();
                        previousHashedByteCount += hashCrawler.getHashedByteCount();
                        if (index < (listLength - 1)) {
                            //Branch the new log file and remove the old one
                            wizard.getFileHandlers().get(index).close();
                            Main.logger.removeHandler(wizard.getFileHandlers().get(index));
                            Main.logger.addHandler(wizard.getFileHandlers().get(index+1));
                            //Branch the new hash project
                            HashProject nextProject = hashProjectList.get(index+1);
                            FileSystemHash nextFSH = nextProject.getFileSystemHash();
                            bindUI(nextFSH);
                            currentCrawler = nextProject.getFileSystemHash().getHashCrawler();
                            nextFSH.computeHashes();
                        }
                    }
                });
            }
            //When the last crawler has finished, display the summary of the process
            HashProject lastProject = hashProjectList.get(listLength-1);
            HashCrawler lastCrawler = lastProject.getFileSystemHash().getHashCrawler();
            lastCrawler.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    XMLHandler.saveToXML(lastProject.getFileSystemHash(), lastProject.getOutputFilePath());
                    //Stop logging to the log file
                    wizard.getFileHandlers().get(listLength-1).close();
                    Main.logger.removeHandler(wizard.getFileHandlers().get(listLength-1));
                    timer.cancel();
                    wizard.gotoRecap();
                }
            });
        } else if (listLength == 1){
            HashProject onlyProject = hashProjectList.get(0);
            HashCrawler onlyCrawler = onlyProject.getFileSystemHash().getHashCrawler();
            onlyCrawler.stateProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue == Worker.State.SUCCEEDED) {
                    XMLHandler.saveToXML(onlyProject.getFileSystemHash(), onlyProject.getOutputFilePath());
                    //Stop logging to the log file
                    wizard.getFileHandlers().get(0).close();
                    Main.logger.removeHandler(wizard.getFileHandlers().get(0));
                    timer.cancel();
                    wizard.gotoRecap();
                }
            });
        }
        //Branch the log file
        Main.logger.addHandler(wizard.getFileHandlers().get(0));
        //Init for the first hash generation
        previousHashedFileCount = 0;
        previousHashedByteCount = 0;
        FileSystemHash firstFSH = hashProjectList.get(0).getFileSystemHash();
        bindUI(firstFSH);
        currentCrawler = firstFSH.getHashCrawler();
        //Set up a timer to show elapsed time
        seconds = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new ElapsedTimeTask(), 1000, 1000);
        //Start the hash generation
        firstFSH.computeHashes();
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

    private void updateHashedByteCount(Number newValue, double offset){
        updateByteCount(hashedByteCountLabel, hashedByteCountUnit, unit, newValue, offset);
    }

    /**
     * Update the number of bytes crunched and adapt unit to the new value
     * @param newValue     the new number of bytes hashed
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
        progressBar.setPercentage((int) Math.floor(percentage*100));
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
