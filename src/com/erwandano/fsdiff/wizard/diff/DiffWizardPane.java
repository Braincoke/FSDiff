package com.erwandano.fsdiff.wizard.diff;

import com.erwandano.fsdiff.wizard.Wizard;
import com.erwandano.fsdiff.wizard.WizardPane;

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

