package gui.wizard.comparison;

import gui.wizard.Wizard;
import gui.wizard.WizardPane;

/**
 * A pane of the comparison wizard
 */
public class ComparisonWizardPane extends WizardPane {

    protected ComparisonWizard wizard;

    public ComparisonWizard getWizard() {
        return wizard;
    }

    @Override
    public void setWizard(Wizard wizard){
        this.wizard = (ComparisonWizard) wizard;
        setApplication(wizard.getApplication());
        init();
    }

    @Override
    public void init() {}

    @Override
    public void reload(){}
}

