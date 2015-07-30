package gui.wizard.comparison;

import javafx.event.ActionEvent;

public class ComparedFSChoiceController extends ChoiceController {

    @Override
    public void next() {
        wizard.setComparedFSInputType(this.inputType);
        wizard.setComparedFSPath(this.input);
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
