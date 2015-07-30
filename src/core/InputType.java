package core;

/**
 * List available input types to load a FileSystemHash object
 */
public enum InputType {

    FSHX("Saved hashes (*.fshx)"),
    LOGICAL_DIRECTORY("Logical disk or directory");


    private String description;

    InputType(String description){
        this.description =description;
    }


    public String getDescription() {
        return description;
    }

    /**
     * This override sole purpose is to make InputType objects work with JavaFX ComboBox
     * When selecting an InputType in the ComboBox, the ComboBox will use toString() to display
     * the description of the selected InputType.
     * This is an ugly override but at the moment I could not find any other way of doing it.
     * @return
     */
    @Override
    public String toString(){
        return this.getDescription();
    }

}
