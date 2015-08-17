package gui;


import gui.loaders.LoadingFSCXController;
import gui.wizard.hash.HashWizard;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Provides the user multiple choices to start using the software
 */
public class WelcomeScreenController extends Controller {

    public NewComparisonProjectController newComparisonProjectController = null;

    public void newComparison(ActionEvent actionEvent) {
        try {
            if(newComparisonProjectController==null)
                newComparisonProjectController = NewComparisonProjectController.init(application);
            else
                newComparisonProjectController.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newHash(){
        new HashWizard(application);
    }

    public void openComparison(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File system comparison", "*.fscx"));
        File file = fileChooser.showOpenDialog(application.getStage());
        if(file!=null) {
            LoadingFSCXController loadingController;
            try {
                loadingController = (LoadingFSCXController) application.replaceSceneContent("loaders/LoadingFSCX.fxml");
                loadingController.setApplication(application);
                loadingController.start(file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
