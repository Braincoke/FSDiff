package gui.wizard.comparison;

import core.FileSystemInput;

/**
 * Choice of the reference file system
 */
public class ReferenceFSChoiceController extends ChoiceController {

    @Override
    public void next() {
        FileSystemInput ref = new FileSystemInput(this.inputType, this.inputPath, true);
        wizard.setReferenceInput(ref);
        wizard.setCurrentPath(this.inputPath.getParent());
        wizard.gotoComparedChoice();
    }

    @Override
    public void previous() {}

    @Override
    protected void privateInitialization() {
        headerText.setText("Reference File System");
        subheaderText.setText("Choose the File System that will serve as a reference in the comparison");
        previousButton.setDisable(true);
    }
}
