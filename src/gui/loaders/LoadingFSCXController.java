package gui.loaders;

import gui.Controller;
import gui.comparison.ComparisonWindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import loaders.FscxLoader;

/**
 * Controls the loading
 */
public class LoadingFSCXController extends Controller {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label loadingLabel;

    private FscxLoader loader;

    public void start(String fscxPath){
        progressIndicator.setCache(true);
        loader = new FscxLoader(fscxPath);
        loadingLabel.textProperty().bind(loader.messageProperty());
        progressIndicator.progressProperty().bind(loader.progressProperty());
        loader.setOnSucceeded(event -> {
            ComparisonWindowController comparisonWindowController;
            try {
                comparisonWindowController = (ComparisonWindowController) application.replaceSceneContent("comparison/ComparisonWindow.fxml");
                application.getStage().setWidth(ComparisonWindowController.INTERFACE_WIDTH);
                application.getStage().setHeight(ComparisonWindowController.INTERFACE_HEIGHT);
                comparisonWindowController.setApplication(application);
                comparisonWindowController.initWindow(loader.getValue(), fscxPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loader.start();

    }

    public void cancel(ActionEvent actionEvent) {
        if(loader!=null){
            loader.cancel();
        }
    }
}
