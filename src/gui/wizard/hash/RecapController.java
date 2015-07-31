package gui.wizard.hash;

import core.FileSystemHash;
import gui.components.InfoItem;
import gui.components.InfoView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

public class RecapController extends HashWizardPane {

    @FXML
    private InfoView infoView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private InfoItem inputTypeInfo;
    @FXML
    private InfoItem inputPathInfo;
    @FXML
    private InfoItem outputFilePathInfo;
    @FXML
    private InfoItem elapsedInfo;
    @FXML
    private InfoItem fileCountInfo;
    @FXML
    private InfoItem byteCountInfo;

    public void recap(){
        FileSystemHash fsh = wizard.getFileSystemHash();
        infoView.prefWidthProperty().bind(scrollPane.widthProperty());
        infoView.resize(scrollPane.widthProperty());
        inputTypeInfo.setText(wizard.getFileSystemInput().getInputType().getDescription());
        inputPathInfo.setText(fsh.getRootPath().toString());
        outputFilePathInfo.setText(wizard.getOutputFilePath().toString());
        elapsedInfo.setText(fsh.formatDuration());
        fileCountInfo.setText(String.valueOf(fsh.getFileCount()));
        byteCountInfo.setText(String.valueOf(fsh.getByteCount()));
    }

    public void done(ActionEvent actionEvent) {
        application.gotoWelcomeScreen();
    }
}
