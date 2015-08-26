package core;

import java.nio.file.Path;
/**
 * Stores the comparison of to files or directories
 * Implements Comparable so that it has a natural order defined by the path compared
 */
public class PathComparison implements Comparable<PathComparison> {


    public static final ComparisonStatus MATCHED = ComparisonStatus.MATCHED;
    public static final ComparisonStatus MODIFIED = ComparisonStatus.MODIFIED;
    public static final ComparisonStatus CREATED = ComparisonStatus.CREATED;
    public static final ComparisonStatus DELETED = ComparisonStatus.DELETED;
    public static final ComparisonStatus ERROR = ComparisonStatus.ERROR;
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
    private PathComparison parent;

    public PathComparison getParent() {
        return parent;
    }

    public void setParent(PathComparison parent) {
        this.parent = parent;
    }

    public Path getParentPath() {
        return this.path.getParent();
    }

    /**
     * The status of the comparison
     */
    private ComparisonStatus status;

    public ComparisonStatus getStatus() {
        return status;
    }

    public void setStatus(ComparisonStatus status) {
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
     * The getIndex() method of the ComparisonStatus class indicates the index of the comparison status to correctly
     * store the data.
     */
    private int directoryStatus[];


    public PathComparison(){
        int size = ComparisonStatus.SIZE;
        this.path = null;
        this.isDirectory = false;
        directoryStatus = new int[size];
        for(int i = 0; i<size; i++) {
            directoryStatus[i] = 0;
        }
    }

    public PathComparison(Path path) {
        int size = ComparisonStatus.SIZE;
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
        if(directoryStatus.length <= ComparisonStatus.SIZE){
            this.directoryStatus = directoryStatus;
        }
    }

    public int getDirectoryStatus(ComparisonStatus comparisonStatus){
        return directoryStatus[comparisonStatus.getIndex()];
    }

    public void updateDirectoryStatus(PathComparison pathComparison) {
        switch (pathComparison.getStatus()) {
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
     * @param comparisonStatus  The comparison status to query
     * @return The count of the comparison status
     */
    public int getCount(ComparisonStatus comparisonStatus){
        if(isDirectory) {
            return directoryStatus[comparisonStatus.getIndex()];
        } else {
            return (status == comparisonStatus ? 1:0);
        }
    }

    @Override
    public int compareTo(PathComparison o) {
        return path.compareTo(o.getPath());
    }


    public int comparePath(PathComparison potentialParent) {
        return path.toString().compareTo(potentialParent.toString());
    }
}
