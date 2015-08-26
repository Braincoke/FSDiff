package ui;


import javafx.stage.FileChooser;
import loaders.FSCXLoader;
import ui.diff.DiffWindowController;
import ui.loaders.LoadingController;
import ui.wizard.hash.HashWizard;

import java.io.File;
import java.util.logging.Level;

/**
 * Provides the user multiple choices to start using the software
 */
public class WelcomeScreenController extends Controller {

    public NewDiffController newDiffController = null;

    public void newDiff() {
        try {
            if(newDiffController ==null)
                newDiffController = NewDiffController.init(application);
            else
                newDiffController.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newHash(){
        new HashWizard(application);
    }

    public void openDiff() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File system differential", "*.fscx"));
        File file = fileChooser.showOpenDialog(application.getStage());
        if(file!=null) {
            LoadingController loadingController = new LoadingController();
            try {
                application.replaceSceneContent("loaders/Loading.fxml", loadingController);
                loadingController.setApplication(application);
                FSCXLoader loader = loadingController.getFSCXLoader();
                //Go to diff interface when loaded
                loader.setOnSucceeded(event -> {
                    DiffWindowController diffWindowController;
                    try {
                        diffWindowController = (DiffWindowController) application.replaceSceneContent("comparison/DiffWindow.fxml");
                        application.getStage().setWidth(DiffWindowController.INTERFACE_WIDTH);
                        application.getStage().setHeight(DiffWindowController.INTERFACE_HEIGHT);
                        diffWindowController.setApplication(application);
                        diffWindowController.initWindow(loader.getValue(), file.getPath());
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
