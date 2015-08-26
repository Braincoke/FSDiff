package gui;


import gui.comparison.ComparisonWindowController;
import gui.loaders.LoadingController;
import gui.wizard.hash.HashWizard;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import loaders.FSCXLoader;

import java.io.File;
import java.util.logging.Level;

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

    public void openComparison() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File system comparison", "*.fscx"));
        File file = fileChooser.showOpenDialog(application.getStage());
        if(file!=null) {
            LoadingController loadingController = new LoadingController();
            try {
                application.replaceSceneContent("loaders/Loading.fxml", loadingController);
                loadingController.setApplication(application);
                FSCXLoader loader = loadingController.getFSCXLoader();
                //Go to comparison interface when loaded
                loader.setOnSucceeded(event -> {
                    ComparisonWindowController comparisonWindowController;
                    try {
                        comparisonWindowController = (ComparisonWindowController) application.replaceSceneContent("comparison/ComparisonWindow.fxml");
                        application.getStage().setWidth(ComparisonWindowController.INTERFACE_WIDTH);
                        application.getStage().setHeight(ComparisonWindowController.INTERFACE_HEIGHT);
                        comparisonWindowController.setApplication(application);
                        comparisonWindowController.initWindow(loader.getValue(), file.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                loadingController.loadFSCX(file.getPath());
            } catch (Exception e) {
                Main.logger.log(Level.WARNING, "Could not load the file : " + file.toString(),e);
            }
        }
    }
}
