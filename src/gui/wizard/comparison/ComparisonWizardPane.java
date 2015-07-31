package gui.wizard.comparison;

import gui.Controller;

/**
 * A pane of the comparison wizard
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

