package gui.wizard.comparison;

import core.FileSystemComparison;
import core.FileSystemHash;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Compare the two file systems and show progress of the comparison
 */
public class ComparisonProgressController extends ComparisonWizardPane {

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label loadingLabel;

    private FileSystemComparison.Generate task;

    public void compare(){
        FileSystemHash refFSH = wizard.getReferenceFSH();
        FileSystemHash comFSH = wizard.getComparedFSH();
        task = new FileSystemComparison.Generate(wizard.getOutputFilePath().getFileName().toString(), refFSH, comFSH);
        progressIndicator.progressProperty().bind(task.progressProperty());
        loadingLabel.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(event -> {
            wizard.setComparison(task.getValue());
            wizard.gotoComparisonInterface();
        });
        task.start();
    }

    public void cancel() {
        if(task!=null){
            if(task.cancel())
                wizard.gotoWelcomeScreen();
        }
    }
}
