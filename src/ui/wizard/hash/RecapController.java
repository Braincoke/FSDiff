package ui.wizard.hash;

import core.FileSystemHash;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import ui.components.InfoItem;
import ui.components.InfoView;

import java.util.List;

public class RecapController extends HashWizardPane {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox infoViewList;

    public void recap(){
        List<HashProject> hashProjectList =  wizard.getHashProjectList();
        hashProjectList.stream().forEach(this::recap);

    }

    public void recap(HashProject hashProject){
        HBox nameBox = new HBox();
        Label name = new Label(hashProject.getName().toUpperCase());
        name.setFont(new Font(24));
        nameBox.getChildren().add(name);
        nameBox.setAlignment(Pos.CENTER);

        InfoView infoView = new InfoView();
        InfoItem inputTypeInfo = new InfoItem("Input type");
        InfoItem inputPathInfo= new InfoItem("Input path");
        InfoItem outputFilePathInfo= new InfoItem("Output file");
        InfoItem elapsedInfo= new InfoItem("Elapsed time");
        InfoItem fileCountInfo= new InfoItem("Number of files hashed");
        InfoItem byteCountInfo= new InfoItem("Total size");
        FileSystemHash fsh = hashProject.getFileSystemHash();
        inputTypeInfo.setText(hashProject.getFileSystemInput().getInputType().getDescription());
        inputPathInfo.setText(fsh.getRootPath().toString());
        outputFilePathInfo.setText(hashProject.getOutputFilePath().toString());
        elapsedInfo.setText(fsh.formatDuration());
        fileCountInfo.setText(String.valueOf(fsh.getFileCount()));
        byteCountInfo.setText(fsh.formatByteCount());
        /* Add infoItems */
        infoView.getChildren().addAll(
                inputTypeInfo,
                inputPathInfo,
                outputFilePathInfo,
                elapsedInfo,
                fileCountInfo,
                byteCountInfo
        );
        infoViewList.getChildren().addAll(nameBox, infoView);
        infoView.getChildren().stream().forEach(node -> {
            InfoItem item = (InfoItem) node;
            item.setPadding(new Insets(10, 20, 10, 10));
        });
        infoView.setPadding(new Insets(10,0,30,0));
        infoView.prefWidthProperty().bind(scrollPane.widthProperty());
        infoView.resize(scrollPane.widthProperty());
    }

    public void done(ActionEvent actionEvent) {
        application.gotoWelcomeScreen();
    }

    public void initialize(){
        scrollPane.setFitToWidth(true);
    }
}
