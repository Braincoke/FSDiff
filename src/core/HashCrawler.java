package core;

import gui.Main;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.TreeMap;
import java.util.logging.Level;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Crawls a directory and generate a hash of every file inside.
 * Extends the Service class to run as a background task of the FX Application Thread safely
 */
public class HashCrawler extends Service<Void> implements FileVisitor<Path>{

    /**
     * The path from where to crawl
     */
    private Path rootPath;

    public Path getRootPath(){
        return rootPath;
    }

    public void setRootPath(Path rootPath){
        this.rootPath = rootPath;
    }

    /**
     * The number of files hashed so far
     */
    private int hashedFileCount;

    public int getHashedFileCount() {
        return hashedFileCount;
    }

    /**
     * The number of errors encountered
     */
    private int errorCount;
    /**
     * The number of files visited so far
     */
    private int visitedCount;
    /**
     * The number of bytes hashed so far
     */
    private long hashedByteCount;

    public long getHashedByteCount() {
        return hashedByteCount;
    }

    /**
     * Property : number of files hashed so far
     * Useful to indicate progress to a UI
     */
    private IntegerProperty hashedFileCountProperty;

    public IntegerProperty getHashedFileCountProperty(){
        return hashedFileCountProperty;
    }

    /**
     * Property : number of errors encountered so far
     * Useful to indicate progress to a UI
     */
    private IntegerProperty errorCountProperty;

    public IntegerProperty getErrorCountProperty(){
        return errorCountProperty;
    }

    /**
     * Property : number of files visited so far
     * Useful to indicate progress to a UI
     */
    private IntegerProperty visitedCountProperty;

    public IntegerProperty getVisitedCountProperty(){
        return visitedCountProperty;
    }

    /**
     * Property : the file currently visited
     * Useful to indicate progress to a UI
     */
    private StringProperty visitedFileProperty;

    public StringProperty getVisitedFileProperty(){
        return visitedFileProperty;
    }

    /**
     * Property : number of bytes hashed so far
     * Useful to indicate progress to a UI
     */
    private LongProperty hashedByteCountProperty;

    public LongProperty getHashedByteCountProperty(){
        return hashedByteCountProperty;
    }

    /**
     * The time it took to generate the hashes in seconds
     */
    private Duration duration;

    public Duration getDuration(){
        return duration;
    }

    /**
     * The task that generate the hashes
     */
    private HashFilesTask task;

    public HashFilesTask getTask(){
        return task;
    }

    /**
     * Set of hashed files
     */
    private TreeMap<Path, HashedFile> fileHashes;

    public TreeMap<Path, HashedFile> getFileHashes() {
        return fileHashes;
    }
    public HashCrawler(Path rootPath){
        this.rootPath=rootPath;
        this.hashedFileCount = 0;
        this.errorCount = 0;
        this.visitedCount = 0;
        this.hashedByteCount = 0;
        this.hashedFileCountProperty = new SimpleIntegerProperty(0);
        this.errorCountProperty = new SimpleIntegerProperty(0);
        this.visitedCountProperty = new SimpleIntegerProperty(0);
        this.visitedFileProperty = new SimpleStringProperty("");
        this.hashedByteCountProperty = new SimpleLongProperty();
        this.fileHashes = new TreeMap<>();
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * FILE VISITOR IMPLEMENTATION                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        visitedCount++;
        try {
            fileHashes.put(stripRootPath(file), HashGenerator.generateHashedFile(file));
            hashedFileCount++;
            hashedByteCount += file.toFile().length();
            Platform.runLater(() -> {
                hashedByteCountProperty.set(hashedByteCount);
                hashedFileCountProperty.set(hashedFileCount);
                visitedCountProperty.set(visitedCount);
                visitedFileProperty.setValue(file.toString());
            });

        } catch (NoSuchAlgorithmException e) {
            Main.logger.log(Level.WARNING, "Tried to use an unknown algorithm to generate digest", e);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        visitedCount++;
        errorCount++;
        Platform.runLater(() -> {
            errorCountProperty.set(errorCount);
            visitedCountProperty.set(visitedCount);
        });
        Main.logger.log(Level.WARNING,"Failed to visit file: " + file.toString() + "\t" + exc.getMessage());
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return CONTINUE;
    }

    @Override
    protected Task<Void> createTask() {
        task = new HashFilesTask(this);
        return task;
    }

    /**
     * Strips the path of the root directory from a file path to save space
     *
     * @param filePath The path of the file
     * @return The path of the file without the path of the root directory
     */
    private Path stripRootPath(Path filePath) {
        if (filePath.startsWith(rootPath)) {
            return rootPath.relativize(filePath);
        } else {
            return filePath;
        }
    }


    /**
     * The task responsible for crawling and generating the hashes
     * This is the task run by the HashCrawler service
     */
    private class HashFilesTask extends Task<Void> {

        private HashCrawler crawler;
        public HashFilesTask(HashCrawler crawler){
            this.crawler = crawler;
        }

        @Override
        protected Void call() throws Exception {
            //Elapsed time
            LocalTime startTime = LocalTime.now();
            Main.logger.log(Level.INFO, "Hash generation started at " + startTime.toString() );
            try {
                //Start crawling
                Files.walkFileTree(rootPath, crawler);
            } catch (IOException e) {
                Main.logger.log(Level.SEVERE, "Error when hashing the files\t" + e.getMessage());
            }
            LocalTime endTime = LocalTime.now();
            Main.logger.log(Level.INFO, "Hash generation ended at " + startTime.toString() );
            duration = Duration.between(startTime, endTime);
            Main.logger.log(Level.INFO, "Duration of the hash generation is " + duration.toString());
            return null;
        }
    }
}
