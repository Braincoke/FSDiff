package gui.wizard.hash;

import core.FileSystemHash;
import core.FileSystemInput;

import java.nio.file.Path;

/**
 * Defines the parameters of a hash project
 */
public class HashProject {

    public HashProject(FileSystemInput fileSystemInput, String name, Path outputDirectory){
        this.fileSystemInput = fileSystemInput;
        this.name = name;
        this.outputDirectory = outputDirectory;
    }

    /**
     * The file system to hash
     */
    private FileSystemInput fileSystemInput;

    public FileSystemInput getFileSystemInput() {
        return fileSystemInput;
    }

    public void setFileSystemInput(FileSystemInput fileSystemInput) {
        this.fileSystemInput = fileSystemInput;
    }

    /**
     * The name of the input file, which is also the name of the project
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The output directory where to save the file system hash
     */
    private Path outputDirectory;

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Output file path
     */
    public Path getOutputFilePath() {
        return outputDirectory.resolve(name);
    }

    /**
     * The result of the project : the hash
     */
    private FileSystemHash fileSystemHash;

    public FileSystemHash getFileSystemHash() {
        return fileSystemHash;
    }

    public void setFileSystemHash(FileSystemHash fileSystemHash) {
        this.fileSystemHash = fileSystemHash;
    }
}
