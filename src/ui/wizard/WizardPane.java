package ui.wizard;

import ui.Controller;

/**
 * A component of a Wizard
 *
 * A WizardPane is supposed to be a FXML controller, controlling one window.
 * The WizardPane can access important data thanks to the wizard's methods and
 * can redirect the flow according to the user's input thanks to the wizard's
 * "goto" methods.
 */
public abstract class WizardPane extends Controller {

    /**
     * The Wizard the pane is a part of
     */
    protected Wizard wizard;

    public Wizard getWizard() {
        return wizard;
    }

    public void setWizard(Wizard wizard) {
        this.wizard = wizard;
        this.application = wizard.getApplication();
        init();
    }

    /**
     * Perform some initialization after the Wizard has been set.
     * Most of the time, the WizardPane will need to display some data
     * that will only be accessible through the Wizard. This method provides
     * a way to initialize the view only when the data is accessible.
     */
    public abstract void init();


    /**
     * Reload the information that was displayed in this pane.
     *
     * This method is supposed to be called whenever the user uses the
     * previous button (or the "previous", then "next" button) and the
     * user wishes that the information he previously entered has been
     * saved
     */
    public abstract void reload();
}
