package ui.wizard.diff;

import ui.wizard.Wizard;
import ui.wizard.WizardPane;

/**
 * A pane of the DiffWizard
 */
public class DiffWizardPane extends WizardPane {

    protected DiffWizard wizard;

    public DiffWizard getWizard() {
        return wizard;
    }

    @Override
    public void setWizard(Wizard wizard){
        this.wizard = (DiffWizard) wizard;
        setApplication(wizard.getApplication());
        init();
    }

    @Override
    public void init() {}

    @Override
    public void reload(){}
}

