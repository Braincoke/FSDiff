package gui.wizard.comparison;

import gui.Controller;

/**
 * Created by Erwan Dano on 24/07/2015.
 */
public class ComparisonWizardPane extends Controller {

    protected ComparisonWizard wizard;

    public ComparisonWizard getWizard() {
        return wizard;
    }

    public void setWizard(ComparisonWizard wizard) {
        this.wizard = wizard;
        setApplication(wizard.getApplication());
    }
}

