package core;

import javafx.concurrent.WorkerStateEvent;

import java.nio.file.Path;
import java.util.*;

/**
 * Records the metadata and hashes of a performed file system hash
 * The MD5  hashes of each file in the file system are saved.
 */
public class FileSystemHash extends FileSystemHashMetadata {


    /*******************************************************************************************************************
     *                                                                                                                 *
     *  CONSTRUCTORS                                                                                                   *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Init a file system hash from the given information
     *
     * @param metadata  Metadata of the file system hash
     * @param hashes    The list of digests
     */
    public FileSystemHash(HashMap<String, String> metadata, TreeMap<Path, HashedFile> hashes) {
        super(metadata);
        fileHashes = hashes;
    }

    public FileSystemHash(FileSystemInput input) {
        this.fileSystemInput = input;
        hashCrawler = new HashCrawler(fileSystemInput.getPath());
    }


    public FileSystemHash(FileSystemInput input, String name) {
        this.fileSystemInput = input;
        this.name = name;
        hashCrawler = new HashCrawler(fileSystemInput.getPath());
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  ATTRIBUTES                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The set of file hashes
     */
    private TreeMap<Path, HashedFile> fileHashes;

    public TreeMap<Path, HashedFile> getFileHashes() {
        TreeMap<Path, HashedFile> copy = new TreeMap<>();
        for (Map.Entry<Path, HashedFile> entry : fileHashes.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    /**
     * The service responsible for finding and hashing the files
     */
    private HashCrawler hashCrawler;

    public HashCrawler getHashCrawler(){
        return hashCrawler;
    }



    /**
     * Compute the hashes of every file in the file system
     */
    public void computeHashes() {
        //Start by resetting the data
        fileCount = 0;
        errorCount = 0;
        fileHashes = new TreeMap<>();
        OS = System.getProperty("os.name");
        fileSystem = getRootPath().getFileSystem().toString();
        datetime = new Date();
        //When all the files have been hashed update metadata
        hashCrawler.setOnSucceeded((WorkerStateEvent e) -> {
            this.fileHashes = hashCrawler.getFileHashes();
            this.byteCount = hashCrawler.getHashedByteCount();
            this.fileCount = hashCrawler.getHashedFileCount();
            this.duration = hashCrawler.getDuration();
        });
        hashCrawler.start();
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     *  COMPARISON GENERATION                                                                                          *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Compare this file system hash to another one
     *
     * @param referenceFS The file system of reference
     * @return a file system comparison held in a FileSystemComparison object
     */
    public FileSystemComparison compareTo(FileSystemHash referenceFS, String comparisonName) {
        TreeMap<Path, HashedFile> referenceHashes = referenceFS.getFileHashes();
        TreeMap<Path, HashedFile> comparedHashes = this.getFileHashes();
        TreeSet<PathComparison> comparisonSet = new TreeSet<>();

        //FileSystemComparison metadata
        int deletedCount = 0;
        int matchedCount = 0;
        int createdCount = 0;
        int modifiedCount = 0;
        int comparisonErrorCount = 0;

        Path filePath;
        HashedFile referenceHashedFile;
        HashedFile comparedHashedFile;
        //Loop through every file of the reference file system or directory
        for (Map.Entry<Path, HashedFile> referenceEntry : referenceHashes.entrySet()) {
            referenceHashedFile = referenceEntry.getValue();
            filePath = referenceEntry.getKey();
            comparedHashedFile = comparedHashes.get(filePath);
            PathComparison pathComparison = new PathComparison(filePath);
            //File does not exist in the compared file system
            if (comparedHashedFile == null) {
                pathComparison.setStatus(PathComparison.DELETED);
                deletedCount++;
            } else {
                //Check if file has been modified
                if (referenceHashedFile.isEqual(comparedHashedFile)) {
                    pathComparison.setStatus(PathComparison.MATCHED);
                    matchedCount++;
                } else {
                    pathComparison.setStatus(PathComparison.MODIFIED);
                    modifiedCount++;
                }
            }
            comparisonSet.add(pathComparison);
            //Remove path from compared FS to find created files in the end
            comparedHashes.remove(filePath);

        }
        //The remaining hashed files in the compared FS must be created (or moved)
        for (Map.Entry<Path, HashedFile> comparedEntry : comparedHashes.entrySet()) {
            filePath = comparedEntry.getKey();
            PathComparison pathComparison = new PathComparison(filePath);
            pathComparison.setStatus(PathComparison.CREATED);
            comparisonSet.add(pathComparison);
            createdCount++;
        }

        //Build args to create FileSystemComparison
        FileSystemHashMetadata referenceFSMetadata = new FileSystemHashMetadata(referenceFS);
        FileSystemHashMetadata comparedFSMetadata = new FileSystemHashMetadata(this);
        List<Integer> comparisonMetadata = new Vector<>();
        comparisonMetadata.add(ComparisonStatus.MATCHED.getIndex(), matchedCount);
        comparisonMetadata.add(ComparisonStatus.MODIFIED.getIndex(), modifiedCount);
        comparisonMetadata.add(ComparisonStatus.CREATED.getIndex(), createdCount);
        comparisonMetadata.add(ComparisonStatus.DELETED.getIndex(), deletedCount);
        comparisonMetadata.add(ComparisonStatus.ERROR.getIndex(), comparisonErrorCount);

        return new FileSystemComparison(referenceFSMetadata,
                comparedFSMetadata,
                comparisonMetadata,
                comparisonSet,
                comparisonName);
    }

    /**
     * Generate a file system comparison with a default name
     *
     * @param referenceFS The file system used as a reference in the comparison
     * @return A FileSystemComparison object holding the results of the comparison
     */
    public FileSystemComparison compareTo(FileSystemHash referenceFS) {
        String referenceName = referenceFS.getName();
        String comparisonName = "Comparison_of_" + name + "_to_" + referenceName;
        return this.compareTo(referenceFS, comparisonName);
    }

}
