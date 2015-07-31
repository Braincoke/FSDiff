package gui.wizard.comparison;

import core.FileSystemInput;
import javafx.event.ActionEvent;

/**
 * Choice of the compared file system
 */
public class ComparedFSChoiceController extends ChoiceController {

    @Override
    public void next() {
        FileSystemInput com = new FileSystemInput(this.inputType, this.input, false);
        wizard.setComparedInput(com);
        wizard.enqueue();
        wizard.chooseComparisonPreparation();
    }

    /**
     * Return to the previous window, in this case this is
     * the ReferenceFSChoice windows
     * @param actionEvent
     */
    public void previous(ActionEvent actionEvent) {
        wizard.backToReferenceChoice();
    }
}
