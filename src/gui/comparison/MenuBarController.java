package gui.comparison;

import gui.Controller;
import gui.NewComparisonProjectController;
import gui.wizard.hash.HashWizard;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Menu bar for the comparison window
 */
public class MenuBarController extends Controller {

    private ComparisonWindowController windowController;

    public ComparisonWindowController getWindowController() {
        return windowController;
    }

    public void setWindowController(ComparisonWindowController windowController) {
        this.windowController = windowController;
        this.application = windowController.getApplication();
    }

    public void newProject() {
        //TODO find a way to open a new INDEPENDENT window instead of replacing this one
        NewComparisonProjectController.init(application);

    }

    public void newHash() {
        //TODO find a way to open a new INDEPENDENT window instead of replacing this one
        new HashWizard(application);
    }

    public void about() {
        //TODO write things about FSDiff here, add hyperlink to github page
    }

    public void open() {
        windowController.openFSC();
    }

    public void save() {
        windowController.saveFSC();
    }

    public void saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save comparison");
        fileChooser.setInitialDirectory(windowController.getOutputFile().toFile().getParentFile());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comparison file", "*.fscx"));
        fileChooser.setInitialFileName(windowController.getOutputFile().getFileName().toString());
        File file = fileChooser.showSaveDialog(application.getStage());
        if (file != null) {
            windowController.setOutputFile(file.toPath());
            windowController.saveFSC();
        }
    }

}
