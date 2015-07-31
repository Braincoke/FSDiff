package core;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
 * Crawls one or multiple directories to recursively count the number of files it includes
 */
public class FileCountCrawler extends Service<Void> implements FileVisitor<Path>{

    /**
     * The list of root directory to crawl
     */
    private Path rootList[];
    /**
     * The total number of bytes visited
     */
    private LongProperty byteCountProperty;
    /**
     * The total number of files visited
     */
    private IntegerProperty fileCountProperty;
    /**
     * The file currently visited
     * Useful to show progress to UI
     */
    private StringProperty visitedFileProperty;
    /**
     * The total number of file visited
     */
    private int fileCount;
    /**
     * The total number of bytes visited = sum( filesize )
     */
    private long byteCount;
    /**
     * The task that crawls the directories and counts the number of files
     */
    private CountFilesTask task;
    /**
     * Indicates that the user wish to cancel the files
     */
    private boolean cancelled;

    public FileCountCrawler(List<FileSystemInput> list) {
        this.rootList = new Path[list.size()];
        for (int i = 0; i < list.size(); i++) {
            rootList[i] = list.get(i).getPath();
        }
        this.fileCountProperty = new SimpleIntegerProperty(0);
        this.visitedFileProperty = new SimpleStringProperty("");
        this.byteCountProperty = new SimpleLongProperty(0);
        this.fileCount = 0;
        this.byteCount = 0;
        this.cancelled = false;
        this.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == State.CANCELLED) {
                cancelled = true;
            }
        });
    }

    public Path[] getRootList() {
        return rootList;
    }

    public LongProperty getByteCountProperty(){
        return byteCountProperty;
    }

    public IntegerProperty fileCountProperty() {
        return fileCountProperty;
    }

    public int getFileCount() {
        return fileCount;
    }

    public String getVisitedFile() {
        return visitedFileProperty.get();
    }

    public StringProperty visitedFileProperty() {
        return visitedFileProperty;
    }

    public long getByteCount(){
        return byteCount;
    }

    public void setByteCount(Long byteCount){
        this.byteCount = byteCount;
    }

    public void addByteCount(Long add){
        this.byteCount += add;
    }

    public CountFilesTask getTask(){
        return task;
    }

    @Override
    protected Task<Void> createTask() {
        task = new CountFilesTask(this);
        return task;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * FILE VISITOR IMPLEMENTATION                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/



    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if(cancelled){
            return TERMINATE;
        } else {
            return CONTINUE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        VisitFileTask task = new VisitFileTask(file);
        new Thread(task).start();
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return CONTINUE;
    }

    private class CountFilesTask extends Task<Void> {

        private FileCountCrawler crawler;

        public CountFilesTask(FileCountCrawler crawler){
            this.crawler = crawler;
        }
        @Override
        protected Void call() throws Exception {
            try {
                for(Path rootPath : rootList) {
                    File rootFile = rootPath.toFile();
                    Files.walkFileTree(rootPath, crawler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class VisitFileTask extends Task<Void> {
        private Path file;

        public VisitFileTask(Path file){
            this.file = file;
        }
        @Override
        protected Void call() throws Exception {
            fileCount++;
            byteCount += file.toFile().length();
            if(fileCount%500==0) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        fileCountProperty.set(fileCount);
                        byteCountProperty.set(byteCount);
                    }
                });
            }
            return null;
        }
    }
}
