package gui.wizard.comparison;

import core.FileSystemInput;

/**
 * Choice of the reference file system
 */
public class ReferenceFSChoiceController extends ChoiceController {

    @Override
    public void next() {
        FileSystemInput ref = new FileSystemInput(this.inputType, this.input, true);
        wizard.setReferenceInput(ref);
        wizard.gotoComparedChoice();
    }
}
