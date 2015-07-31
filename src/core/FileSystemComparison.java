package core;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * The result of the comparison of two FileSystemHash objects
 * Lists the matched, modified, created, and deleted files.
 */
public class FileSystemComparison {

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  CONSTRUCTORS                                                                                                   *
     *                                                                                                                 *
     ******************************************************************************************************************/


    public FileSystemComparison(FileSystemHashMetadata referenceFS,
                                FileSystemHashMetadata comparedFS,
                                List<Integer> metadata,
                                Date datetime,
                                TreeSet<PathComparison> set,
                                String name) {
        this.referenceFS = referenceFS;
        this.comparedFS = comparedFS;
        this.name = name;
        this.comparisonSet = set;
        this.createdCount = metadata.get(ComparisonStatus.CREATED.getIndex());
        this.matchedCount = metadata.get(ComparisonStatus.MATCHED.getIndex());
        this.deletedCount = metadata.get(ComparisonStatus.DELETED.getIndex());
        this.modifiedCount = metadata.get(ComparisonStatus.MODIFIED.getIndex());
        this.errorCount = metadata.get(ComparisonStatus.ERROR.getIndex());
    }

    /**
     *
     * @param referenceFS
     * @param comparedFS
     * @param metadata
     * @param set
     * @param name
     */
    public FileSystemComparison(FileSystemHashMetadata referenceFS,
                                FileSystemHashMetadata comparedFS,
                                List<Integer> metadata,
                                TreeSet<PathComparison> set,
                                String name) {
        this.referenceFS = referenceFS;
        this.comparedFS = comparedFS;
        this.createdCount = metadata.get(ComparisonStatus.CREATED.getIndex());
        this.matchedCount = metadata.get(ComparisonStatus.MATCHED.getIndex());
        this.deletedCount = metadata.get(ComparisonStatus.DELETED.getIndex());
        this.modifiedCount = metadata.get(ComparisonStatus.MODIFIED.getIndex());
        this.errorCount = metadata.get(ComparisonStatus.ERROR.getIndex());
        this.name = name;
        this.comparisonSet = new TreeSet<>();
        initComparisonSet(set);
    }


    /**
     * The file system used as a reference in the comparison
     */
    private FileSystemHashMetadata referenceFS;

    public FileSystemHashMetadata getReferenceFS() {
        return referenceFS;
    }

    /**
     * The file system compared to the reference
     */
    private FileSystemHashMetadata comparedFS;

    public FileSystemHashMetadata getComparedFS() {
        return comparedFS;
    }

    /**
     * The number of created files in the comparison
     */
    private int createdCount;

    public int getCreatedCount() {
        return createdCount;
    }

    /**
     * The number of deleted files in the comparison
     */
    private int deletedCount;

    public int getDeletedCount() {
        return deletedCount;
    }

    /**
     * The number of modified files in the comparison
     */
    private int modifiedCount;

    public int getModifiedCount() {
        return modifiedCount;
    }

    /**
     * The number of identical files in the comparison
     */
    private int matchedCount;

    public int getMatchedCount() {
        return matchedCount;
    }

    /**
     * The number of error encountered when performing the comparison
     */
    private int errorCount;

    public int getErrorCount() {
        return errorCount;
    }

    /**
     * The date and time the comparison was generated
     */
    private Date datetime;

    public Date getDatetime() {
        return datetime;
    }

    /**
     * The project name
     */
    private String name;

    public String getName() {
        return name;
    }
    /**
     * The set of compared files and directories
     */
    private TreeSet<PathComparison> comparisonSet;

    public TreeSet<PathComparison> getComparison() {
        TreeSet<PathComparison> copy = new TreeSet<>();
        comparisonSet.forEach(copy::add);
        return copy;
    }

    /**
     * As a FileSystemHash object only contains the hashes of files
     * we need to recreate the directory tree from the paths of the files
     * @param set
     */
    private void initComparisonSet(TreeSet<PathComparison> set) {
        List<Path> pathGenealogyList = new ArrayList<>();
        List<PathComparison> directoryComparisonList = new ArrayList<>();
        directoryComparisonList.add(0, null);
        pathGenealogyList.add(0, null);
        //For every hashed file we copy the path comparison into the comparisonSet
        //And we recreate its directory branch in the comparisonSet
        for(PathComparison pathComparison : set) {
            comparisonSet.add(pathComparison);
            Path filePath = pathComparison.getPath();
            //Verify that every parentList is consistent with the current branch in the file tree
            int end = filePath.getNameCount();
            for(int i = 1; i<end; i++) {
                //The FileSystemComparison only contains file paths
                //We have to recreate a parent directory as a PathComparison for every file
                Path iParent = filePath.subpath(0,i);
                PathComparison directoryComparison;
                if(!pathGenealogyList.contains(iParent)) {
                    //Add the directory as the parent directory of the current file
                    pathGenealogyList.add(i, iParent);
                    directoryComparison = new PathComparison(iParent);
                    directoryComparison.setIsDirectory(true);
                    directoryComparisonList.add(i, directoryComparison);
                    directoryComparison.setParent(directoryComparisonList.get(i - 1));
                    comparisonSet.add(directoryComparison);
                } else {
                    //The directory already exists as a PathComparison
                    directoryComparison = directoryComparisonList.get(i);
                }
                //Update directory data
                directoryComparison.updateDirectoryStatus(pathComparison);
                pathComparison.setParent(directoryComparison);
            }
            //Trim the genealogy lists for the new branch
            //TODO prettify the code
            try {
                trimList(pathGenealogyList, 0, end);
                trimList(directoryComparisonList, 0, end);
            }catch (StackOverflowError e){
                e.printStackTrace();
                System.exit(1);
            }
        }
    }


    private void trimList(List list, int start, int end){
        int lastIndex = list.size()-1;
        int startIndex = start;
        int endIndex = end+1;
        //Basic verifications
        if(startIndex<0){
            startIndex = 0;
        }
        if(endIndex>lastIndex){
            endIndex = lastIndex;
        }
        int range = endIndex-startIndex+1;
        //Trimming
        if(range<list.size()){
            for(int i = 0; i<startIndex; i++){
                if(list.get(0) !=null){
                    list.remove(0);
                }
            }
            for(int i = endIndex; i<lastIndex; i++){
                lastIndex = list.size()-1;
                if(list.get(lastIndex) != null){
                    list.remove(lastIndex);
                }
            }
        }
    }

}
