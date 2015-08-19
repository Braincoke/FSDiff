package gui.wizard.comparison;

import core.FileSystemInput;

/**
 * Choice of the compared file system
 */
public class ComparedFSChoiceController extends ChoiceController {

    @Override
    public void next() {
        FileSystemInput com = new FileSystemInput(this.inputType, this.inputPath, false);
        wizard.setComparedInput(com);
        wizard.setCurrentPath(this.inputPath.getParent());
        wizard.enqueue();
        wizard.chooseComparisonPreparation();
    }

    @Override
    protected void privateInitialization() {
        headerText.setText("Compared File System");
        subheaderText.setText("Choose the File System that will be compared to the reference");
    }

    /**
     * Return to the previous window, in this case this is
     * the ReferenceFSChoice windows
     */
    public void previous() {
        wizard.gotoReferenceChoice();
    }


}
