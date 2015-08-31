package com.erwandano.fsdiff.core;


import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.*;

/**
 * The result of the comparison of two FileSystemHash objects
 * Lists the matched, modified, created, and deleted files.
 */
public class FileSystemDiff {

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  CONSTRUCTORS                                                                                                   *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Create a FileSystemDiff
     * @param referenceFS   The metadata of the reference file system
     * @param comparedFS    The metadata of the compared file system
     * @param metadata      The metadata of the differential
     * @param datetime      The date and time the differential was generated
     * @param set           The set of compared paths
     * @param name          The name given to the differential
     */
    public FileSystemDiff(FileSystemHashMetadata referenceFS,
                          FileSystemHashMetadata comparedFS,
                          List<Integer> metadata,
                          Date datetime,
                          TreeSet<PathDiff> set,
                          String name) {
        this.referenceFS = referenceFS;
        this.comparedFS = comparedFS;
        this.name = name;
        this.diff = set;
        this.createdCount = metadata.get(DiffStatus.CREATED.getIndex());
        this.matchedCount = metadata.get(DiffStatus.MATCHED.getIndex());
        this.deletedCount = metadata.get(DiffStatus.DELETED.getIndex());
        this.modifiedCount = metadata.get(DiffStatus.MODIFIED.getIndex());
        this.errorCount = metadata.get(DiffStatus.ERROR.getIndex());
    }

    /**
     * Create a FileSystemDiff
     * @param referenceFS   The metadata of the reference file system
     * @param comparedFS    The metadata of the compared file system
     * @param metadata      The metadata of the comparison
     * @param set           The set of compared paths
     * @param name          The name given to the differential
     */
    public FileSystemDiff(FileSystemHashMetadata referenceFS,
                          FileSystemHashMetadata comparedFS,
                          List<Integer> metadata,
                          TreeSet<PathDiff> set,
                          String name) {
        this.referenceFS = referenceFS;
        this.comparedFS = comparedFS;
        this.createdCount = metadata.get(DiffStatus.CREATED.getIndex());
        this.matchedCount = metadata.get(DiffStatus.MATCHED.getIndex());
        this.deletedCount = metadata.get(DiffStatus.DELETED.getIndex());
        this.modifiedCount = metadata.get(DiffStatus.MODIFIED.getIndex());
        this.errorCount = metadata.get(DiffStatus.ERROR.getIndex());
        this.name = name;
        this.diff = new TreeSet<>();
        initDiffSet(set);
    }


    /**
     * The file system used as a reference in the differential
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
     * The number of created files in the differential
     */
    private int createdCount;

    public int getCreatedCount() {
        return createdCount;
    }

    /**
     * The number of deleted files in the differential
     */
    private int deletedCount;

    public int getDeletedCount() {
        return deletedCount;
    }

    /**
     * The number of modified files in the differential
     */
    private int modifiedCount;

    public int getModifiedCount() {
        return modifiedCount;
    }

    /**
     * The number of identical files in the differential
     */
    private int matchedCount;

    public int getMatchedCount() {
        return matchedCount;
    }

    /**
     * The number of error encountered when performing the differential
     */
    private int errorCount;

    public int getErrorCount() {
        return errorCount;
    }

    /**
     * The date and time the differential was generated
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
    private TreeSet<PathDiff> diff;

    public TreeSet<PathDiff> getDiff() {
        TreeSet<PathDiff> copy = new TreeSet<>();
        diff.forEach(copy::add);
        return copy;
    }

    /**
     * As a FileSystemHash object only contains the hashes of files
     * we need to recreate the directory tree from the paths of the files
     * @param set   The set of compared paths
     */
    private void initDiffSet(TreeSet<PathDiff> set) {
        List<Path> pathGenealogyList = new ArrayList<>();
        List<PathDiff> directoryDiffList = new ArrayList<>();
        directoryDiffList.add(0, null);
        pathGenealogyList.add(0, null);
        //For every hashed file we copy the path comparison into the diff
        //And we recreate its directory branch in the diff
        for(PathDiff pathDiff : set) {
            diff.add(pathDiff);
            Path filePath = pathDiff.getPath();
            //Verify that every parentList is consistent with the current branch in the file tree
            int end = filePath.getNameCount();
            for(int i = 1; i<end; i++) {
                //The FileSystemDiff only contains file paths
                //We have to recreate a parent directory as a PathDiff for every file
                Path iParent = filePath.subpath(0,i);
                PathDiff directoryDiff;
                if(!pathGenealogyList.contains(iParent)) {
                    //Add the directory as the parent directory of the current file
                    pathGenealogyList.add(i, iParent);
                    directoryDiff = new PathDiff(iParent);
                    directoryDiff.setIsDirectory(true);
                    directoryDiffList.add(i, directoryDiff);
                    directoryDiff.setParent(directoryDiffList.get(i - 1));
                    diff.add(directoryDiff);
                } else {
                    //The directory already exists as a PathDiff
                    directoryDiff = directoryDiffList.get(i);
                }
                //Update directory data
                directoryDiff.updateDirectoryStatus(pathDiff);
                pathDiff.setParent(directoryDiff);
            }
            //Trim the genealogy lists for the new branch
            trimList(pathGenealogyList, 0, end);
            trimList(directoryDiffList, 0, end);

        }
    }


    /**
     * Trim the given list from "start" to "end".
     * Example: If the list is [ A, B, C, D, E , F]
     * then trim(list, 1, 4) will output [B, C, D, E]
     * @param list      The list to trim
     * @param start     The index indicating the beginning of the trimmed list (included)
     * @param end       The index indicating the end of the trimmed list (included)
     */
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

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  GENERATION                                                                                                   *
     *                                                                                                                 *
     ******************************************************************************************************************/

    public static class Generate extends Service<FileSystemDiff> {

        private String diffName;
        private FileSystemHash referenceFS;
        private FileSystemHash comparedFS;

        public Generate(String name, FileSystemHash referenceFS, FileSystemHash comparedFS){
            this.diffName = name;
            this.referenceFS = referenceFS;
            this.comparedFS = comparedFS;
        }

        @Override
        protected Task<FileSystemDiff> createTask() {
            return new CompareTask(diffName, referenceFS, comparedFS);
        }
    }

    public static class CompareTask extends Task<FileSystemDiff> {

        private String diffName;
        private FileSystemHash referenceFS;
        private FileSystemHash comparedFS;

        public CompareTask(String name, FileSystemHash referenceFS, FileSystemHash comparedFS){
            this.diffName = name;
            this.referenceFS = referenceFS;
            this.comparedFS = comparedFS;
        }

        @Override
        protected FileSystemDiff call() throws Exception {
            TreeMap<Path, HashedFile> referenceHashes = this.referenceFS.getFileHashes();
            TreeMap<Path, HashedFile> comparedHashes = this.comparedFS.getFileHashes();
            TreeSet<PathDiff> diffs = new TreeSet<>();

            //FileSystemDiff metadata
            int deletedCount = 0;
            int matchedCount = 0;
            int createdCount = 0;
            int modifiedCount = 0;
            int errorCount = 0;

            Path filePath;
            HashedFile referenceHashedFile;
            HashedFile comparedHashedFile;

            int workMax = referenceHashes.size();
            int workDone = 0;

            //Loop through every file of the reference file system or directory
            updateMessage("Comparing the digests");
            for (Map.Entry<Path, HashedFile> referenceEntry : referenceHashes.entrySet()) {
                updateProgress(workDone, workMax);
                referenceHashedFile = referenceEntry.getValue();
                filePath = referenceEntry.getKey();
                comparedHashedFile = comparedHashes.get(filePath);
                PathDiff pathDiff = new PathDiff(filePath);
                //File does not exist in the compared file system
                if (comparedHashedFile == null) {
                    pathDiff.setStatus(PathDiff.DELETED);
                    deletedCount++;
                } else {
                    //Check if file has been modified
                    if (referenceHashedFile.isEqual(comparedHashedFile)) {
                        pathDiff.setStatus(PathDiff.MATCHED);
                        matchedCount++;
                    } else {
                        pathDiff.setStatus(PathDiff.MODIFIED);
                        modifiedCount++;
                    }
                }
                diffs.add(pathDiff);
                //Remove path from compared FS to find created files in the end
                comparedHashes.remove(filePath);

            }
            updateMessage("Identifying created files");
            //The remaining hashed files in the compared FS must be created (or moved)
            for (Map.Entry<Path, HashedFile> comparedEntry : comparedHashes.entrySet()) {
                filePath = comparedEntry.getKey();
                PathDiff pathDiff = new PathDiff(filePath);
                pathDiff.setStatus(PathDiff.CREATED);
                diffs.add(pathDiff);
                createdCount++;
            }

            //Build args to create FileSystemDiff
            FileSystemHashMetadata referenceFSMetadata = new FileSystemHashMetadata(this.referenceFS);
            FileSystemHashMetadata comparedFSMetadata = new FileSystemHashMetadata(this.comparedFS);
            List<Integer> diffMetadata = new Vector<>();
            diffMetadata.add(DiffStatus.MATCHED.getIndex(), matchedCount);
            diffMetadata.add(DiffStatus.MODIFIED.getIndex(), modifiedCount);
            diffMetadata.add(DiffStatus.CREATED.getIndex(), createdCount);
            diffMetadata.add(DiffStatus.DELETED.getIndex(), deletedCount);
            diffMetadata.add(DiffStatus.ERROR.getIndex(), errorCount);

            return new FileSystemDiff(referenceFSMetadata,
                    comparedFSMetadata,
                    diffMetadata,
                    diffs,
                    diffName);
        }
    }

}
