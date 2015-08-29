package com.erwandano.fsdiff.diffwindow.leftmenu;

import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.components.InfoItem;
import com.erwandano.fsdiff.components.InfoView;
import com.erwandano.fsdiff.core.FileSystemDiff;
import com.erwandano.fsdiff.core.FileSystemHashMetadata;
import com.erwandano.fsdiff.diffwindow.DiffWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Set up the recap info about the project
 */
public class ProjectTabController extends Controller {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox vBox;

    @FXML
    private InfoView diffView;

    @FXML
    private InfoView referenceView;

    @FXML
    private InfoView comparedView;

    //Diff info
    @FXML
    private InfoItem matchedItem;
    @FXML
    private InfoItem modifiedItem;
    @FXML
    private InfoItem createdItem;
    @FXML
    private InfoItem deletedItem;
    @FXML
    private InfoItem outputItem;

    //Reference info
    @FXML
    private InfoItem inputTypeRefItem;
    @FXML
    private InfoItem pathRefItem;
    @FXML
    private InfoItem filesRefItem;
    @FXML
    private InfoItem elapsedRefItem;
    @FXML
    private InfoItem sizeRefItem;

    //Compared FS info
    @FXML
    private InfoItem inputTypeComItem;
    @FXML
    private InfoItem pathComItem;
    @FXML
    private InfoItem filesComItem;
    @FXML
    private InfoItem elapsedComItem;
    @FXML
    private InfoItem sizeComItem;
    private DiffWindowController windowController;
    private LeftMenuController leftMenuController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
    }

    public void setLeftMenuController(LeftMenuController leftMenuController) {
        this.leftMenuController = leftMenuController;
        this.windowController = leftMenuController.getWindowController();
        FileSystemDiff diff = windowController.getFileSystemDiff();
        matchedItem.setText(String.valueOf(diff.getMatchedCount()));
        modifiedItem.setText(String.valueOf(diff.getModifiedCount()));
        createdItem.setText(String.valueOf(diff.getCreatedCount()));
        deletedItem.setText(String.valueOf(diff.getDeletedCount()));
        outputItem.setText(windowController.getOutputFile().toString());
        FileSystemHashMetadata reference = diff.getReferenceFS();
        inputTypeRefItem.setText(reference.getInputType().getDescription());
        pathRefItem.setText(reference.getRootPath().toString());
        filesRefItem.setText(String.valueOf(reference.getFileCount()));
        elapsedRefItem.setText(reference.formatDuration());
        sizeRefItem.setText(reference.formatByteCount());
        FileSystemHashMetadata compared = diff.getComparedFS();
        inputTypeComItem.setText(compared.getInputType().getDescription());
        pathComItem.setText(compared.getRootPath().toString());
        filesComItem.setText(String.valueOf(compared.getFileCount()));
        elapsedComItem.setText(compared.formatDuration());
        sizeComItem.setText(compared.formatByteCount());
        diffView.resize(scrollPane.widthProperty());
        referenceView.resize(scrollPane.widthProperty());
        comparedView.resize(scrollPane.widthProperty());
    }

    public void leftMenuTabSelection() {
        leftMenuController.leftMenuTabSelection("project-tab");
    }
}
