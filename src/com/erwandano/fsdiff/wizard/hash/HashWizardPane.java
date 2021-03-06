package com.erwandano.fsdiff.wizard.hash;

import com.erwandano.fsdiff.components.Controller;

/**
 * A pane of the hashWizard
 */
public class HashWizardPane extends Controller {

    protected HashWizard wizard;

    public HashWizard getWizard() {
        return wizard;
    }

    public void setWizard(HashWizard wizard) {
        this.wizard = wizard;
        this.application = wizard.getApplication();
        init();
    }

    /**
     * Allow init customization for extended classes
     */
    public void init() {}
}
