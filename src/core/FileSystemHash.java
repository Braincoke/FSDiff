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

    public FileSystemHash(FileSystemHashMetadata metadata, TreeMap<Path, HashedFile> hashes){
        super(metadata);
        this.fileHashes = hashes;
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


    /******************************************************************************************************************
     *                                                                                                                *
     *  DIFFERENTIAL GENERATION                                                                                       *
     *                                                                                                                *
     ******************************************************************************************************************/

    /**
     * Compare this file system hash to another one
     *
     * @param referenceFS The file system of reference
     * @return a differential held in a FileSystemDiff object
     */
    public FileSystemDiff compareTo(FileSystemHash referenceFS, String diffName) {
        TreeMap<Path, HashedFile> referenceHashes = referenceFS.getFileHashes();
        TreeMap<Path, HashedFile> comparedHashes = this.getFileHashes();
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
        //Loop through every file of the reference file system or directory
        for (Map.Entry<Path, HashedFile> referenceEntry : referenceHashes.entrySet()) {
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
        //The remaining hashed files in the compared FS must be created (or moved)
        for (Map.Entry<Path, HashedFile> comparedEntry : comparedHashes.entrySet()) {
            filePath = comparedEntry.getKey();
            PathDiff pathDiff = new PathDiff(filePath);
            pathDiff.setStatus(PathDiff.CREATED);
            diffs.add(pathDiff);
            createdCount++;
        }

        //Build args to create FileSystemDiff
        FileSystemHashMetadata referenceFSMetadata = new FileSystemHashMetadata(referenceFS);
        FileSystemHashMetadata comparedFSMetadata = new FileSystemHashMetadata(this);
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

    /**
     * Generate a FileSystemDiff with a default name
     *
     * @param referenceFS The file system used as a reference in the differential
     * @return A FileSystemDiff object holding the results of the comparison
     */
    public FileSystemDiff compareTo(FileSystemHash referenceFS) {
        String referenceName = referenceFS.getName();
        String diffName = "<" + referenceName + " - " + name + ">" ;
        return this.compareTo(referenceFS, diffName);
    }

}
