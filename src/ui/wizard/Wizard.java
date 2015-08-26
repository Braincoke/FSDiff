package ui.wizard;


import ui.Main;

/**
 * A Wizard
 *
 * A Wizard is supposed to be a master controller that controls multiple WizardPanes as well as the flow between
 * these panes.
 * The Wizard should give himself thanks to the method setWizard() of the WizardPane class. From there, the WizardPane
 * can get access to the necessary data through the Wizard and can direct the flow thanks to the Wizard's "goto" methods
 * that you have to implement.
 */
public abstract class Wizard {

    protected Main application;

    public Main getApplication() {
        return application;
    }

    public void setApplication(Main application) {
        this.application = application;
    }

    /**
     * Navigate forward or backward to a specified WizardPane.
     * If the wizardPane passed as a parameter is not null,
     * the wizard will reload the saved data in the view.
     *
     * @param fxml          The fxml file holding the view to display
     * @param wizardPane    The controller for the fxml
     * @param title         The new title for the stage
     * @return              The controller for the fxml file if it is the first time the pane is visited
     *                      or else the wizardPane parameter
     */
    public WizardPane gotoWizardPane(String fxml, WizardPane wizardPane, String title){
        try{
            application.replaceSceneContent(fxml, wizardPane);
            wizardPane.setWizard(this);
            wizardPane.reload();
            application.getStage().setTitle(title);
        } catch (Exception e){
            e.printStackTrace();
        }
        return wizardPane;
    }

    /**
     * Navigate to a new WizardPane that has never been visited before.
     * The fxml has to define a fx:controller so that the FXMLLoader
     * loads the controller and the function returns the controller.
     *
     * There is no "previous" button on WizardPanes that define a fx:controller.
     *
     * @param fxml      The WizardPane to load
     * @param title     The new stage title
     * @return          The controller of the WizardPane defined in the fxml
     */
    public WizardPane gotoWizardPane(String fxml, String title){
        WizardPane wizardPane = null;
        try{
            wizardPane = (WizardPane) application.replaceSceneContent(fxml);
            wizardPane.setWizard(this);
            application.getStage().setTitle(title);
        } catch (Exception e){
            e.printStackTrace();
        }
        return wizardPane;
    }
}
