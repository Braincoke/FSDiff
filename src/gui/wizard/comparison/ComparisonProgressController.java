package gui.wizard.comparison;

import core.FileSystemComparison;
import core.FileSystemHash;
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

    private FileSystemComparison.Generate service;

    public void compare(){
        FileSystemHash refFSH = wizard.getReferenceFSH();
        FileSystemHash comFSH = wizard.getComparedFSH();
        service = new FileSystemComparison.Generate(wizard.getOutputFilePath().getFileName().toString(), refFSH, comFSH);
        progressIndicator.setProgress(-1f);
        loadingLabel.textProperty().bind(service.messageProperty());
        service.setOnSucceeded(event -> {
            wizard.setComparison(service.getValue());
            wizard.gotoComparisonInterface();
        });
        service.start();
    }

    public void cancel() {
        if(service !=null){
            if(service.cancel())
                wizard.gotoWelcomeScreen();
        }
    }
}
