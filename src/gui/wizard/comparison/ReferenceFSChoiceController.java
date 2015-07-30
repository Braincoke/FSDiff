package gui.wizard.comparison;

/**
 * Created by Erwan Dano on 24/07/2015.
 */
public class ReferenceFSChoiceController extends ChoiceController {

    @Override
    public void next() {
        wizard.setReferenceFSInputType(this.inputType);
        wizard.setReferenceFSPath(this.input);
        wizard.gotoComparedChoice();
    }
}
