package core;

import java.nio.file.Path;

public class FileSystemInput {

    /**
     * The input type of the file system
     */
    private InputType inputType;
    /**
     * The path to the input
     */
    private Path path;
    /**
     * Indicates if it is a reference file system (true) or a compared file system (false)
     */
    private Boolean reference;

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

    public InputType getInputType() {
        return inputType;
    }

    public Path getPath() {
        return path;
    }

    public Boolean getReference() {
        return reference;
    }
}
