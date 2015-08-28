package com.erwandano.fsdiff.core;

import com.erwandano.fsdiff.diffwindow.leftmenu.DiffStatus;

import java.nio.file.Path;
/**
 * Stores the differential of to files or directories
 * Implements Comparable so that it has a natural order defined by the path compared
 */
public class PathDiff implements Comparable<PathDiff> {


    public static final DiffStatus MATCHED = DiffStatus.MATCHED;
    public static final DiffStatus MODIFIED = DiffStatus.MODIFIED;
    public static final DiffStatus CREATED = DiffStatus.CREATED;
    public static final DiffStatus DELETED = DiffStatus.DELETED;
    public static final DiffStatus ERROR = DiffStatus.ERROR;
    /**
     * The path of the two compared files or directories
     */
    private Path path;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    /**
     * The parent directory of the compared path
     */
    private PathDiff parent;

    public PathDiff getParent() {
        return parent;
    }

    public void setParent(PathDiff parent) {
        this.parent = parent;
    }

    public Path getParentPath() {
        return this.path.getParent();
    }

    /**
     * The status of the differential
     */
    private DiffStatus status;

    public DiffStatus getStatus() {
        return status;
    }

    public void setStatus(DiffStatus status) {
        this.status = status;
    }

    /**
     * Indicates if the path is a directory
     */
    private boolean isDirectory;

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    /**
     * Only for directories. Stores the number of files in this directory according to their status.
     * Example :
     *  directoryStatus[0] = 29 => there are 29 matched files contained in this directory
     * The getIndex() method of the DiffStatus class indicates the index of the DiffStatus to correctly
     * store the data.
     */
    private int directoryStatus[];


    public PathDiff(){
        int size = DiffStatus.SIZE;
        this.path = null;
        this.isDirectory = false;
        directoryStatus = new int[size];
        for(int i = 0; i<size; i++) {
            directoryStatus[i] = 0;
        }
    }

    public PathDiff(Path path) {
        int size = DiffStatus.SIZE;
        this.path = path;
        directoryStatus = new int[size];
        for(int i = 0; i<size; i++) {
            directoryStatus[i] = 0;
        }
    }

    public int[] getDirectoryStatus() {
        return directoryStatus;
    }

    public void setDirectoryStatus(int[] directoryStatus) {
        if(directoryStatus.length <= DiffStatus.SIZE){
            this.directoryStatus = directoryStatus;
        }
    }

    public int getDirectoryStatus(DiffStatus diffStatus){
        return directoryStatus[diffStatus.getIndex()];
    }

    public void updateDirectoryStatus(PathDiff pathDiff) {
        switch (pathDiff.getStatus()) {
            case MATCHED:
                directoryStatus[MATCHED.getIndex()]++;
                break;
            case MODIFIED:
                directoryStatus[MODIFIED.getIndex()]++;
                break;
            case CREATED:
                directoryStatus[CREATED.getIndex()]++;
                break;
            case DELETED:
                directoryStatus[DELETED.getIndex()]++;
                break;
            case ERROR:
                directoryStatus[ERROR.getIndex()]++;
                break;
        }
    }

    /**
     * @return The name of the current file or directory
     */
    public String getName() {
        return path.getFileName().toString();
    }

    /**
     * @param diffStatus  The DiffStatus to query
     * @return The count for the DiffStatus
     */
    public int getCount(DiffStatus diffStatus){
        if(isDirectory) {
            return directoryStatus[diffStatus.getIndex()];
        } else {
            return (status == diffStatus ? 1:0);
        }
    }

    @Override
    public int compareTo(PathDiff o) {
        return path.compareTo(o.getPath());
    }


    public int comparePath(PathDiff potentialParent) {
        return path.toString().compareTo(potentialParent.toString());
    }
}
