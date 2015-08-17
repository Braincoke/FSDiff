package core;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class holding the data about a file system that the user wants to hash.
 * If the InputType is FSHX then the hash are stored in a FSHX file.
 * If the InputType is LOGICAL then the user passed a path to a directory to hash.
 * TODO Add ability to use RAW files
 */
public class FileSystemInput {


    public FileSystemInput(InputType inputType, Path path, Boolean reference) {
        this.inputType = inputType;
        this.path = path;
        this.reference = reference;
    }

    public FileSystemInput(String inputTypeStr, Path path, Boolean reference) {
        for (InputType itype : InputType.values()) {
            if (itype.name().compareToIgnoreCase(inputTypeStr) == 0) {
                inputType = itype;
            }
        }
        this.path = path;
        this.reference = reference;
    }


    /**
     * The input type of the file system
     */
    private InputType inputType;

    public InputType getInputType() {
        return inputType;
    }

    /**
     * The path to the input
     */
    private Path path;

    public Path getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = Paths.get(path);
    }

    /**
     * Indicates if it is a reference file system (true) or a compared file system (false)
     */
    private Boolean reference;

    public Boolean isReference() {
        return reference;
    }


}
