package com.erwandano.fsdiff.wizard.diff;

import com.erwandano.fsdiff.core.FileSystemDiff;
import com.erwandano.fsdiff.core.FileSystemHash;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Compare the two file systems and show progress of the comparison
 */
public class DiffProgressController extends DiffWizardPane {

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label loadingLabel;

    private FileSystemDiff.Generate service;

    public void compare(){
        FileSystemHash refFSH = wizard.getReferenceFSH();
        FileSystemHash comFSH = wizard.getComparedFSH();
        service = new FileSystemDiff.Generate(wizard.getOutputFilePath().getFileName().toString(), refFSH, comFSH);
        progressIndicator.setProgress(-1f);
        loadingLabel.textProperty().bind(service.messageProperty());
        service.setOnSucceeded(event -> {
            wizard.setDiff(service.getValue());
            wizard.gotoDiffInterface();
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
