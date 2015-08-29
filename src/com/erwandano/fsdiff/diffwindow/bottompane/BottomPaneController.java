package com.erwandano.fsdiff.diffwindow.bottompane;

import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.core.FileSystemDiff;
import com.erwandano.fsdiff.diffwindow.DiffWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controls the bottom side bar
 */
public class BottomPaneController extends Controller {


    //Circles
    @FXML
    private Circle matchedColor;
    @FXML
    private Circle modifiedColor;
    @FXML
    private Circle createdColor;
    @FXML
    private Circle deletedColor;
    //Bottom - Statuses
    @FXML
    private Label matchedCount;
    @FXML
    private Label modifiedCount;
    @FXML
    private Label createdCount;
    @FXML
    private Label deletedCount;
    @FXML
    private ProgressIndicator progressIndicator;

    private DiffWindowController windowController;

    public void refreshDiffMetadata(FileSystemDiff diff) {
        matchedCount.setText(String.valueOf(diff.getMatchedCount()));
        modifiedCount.setText(String.valueOf(diff.getModifiedCount()));
        createdCount.setText(String.valueOf(diff.getCreatedCount()));
        deletedCount.setText(String.valueOf(diff.getDeletedCount()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matchedColor.getStyleClass().add("matched");
        modifiedColor.getStyleClass().add("modified");
        createdColor.getStyleClass().add("created");
        deletedColor.getStyleClass().add("deleted");

        matchedColor.getStyleClass().add("color-indicator");
        modifiedColor.getStyleClass().add("color-indicator");
        createdColor.getStyleClass().add("color-indicator");
        deletedColor.getStyleClass().add("color-indicator");
    }

    public void setWindowController(DiffWindowController windowController) {
        this.windowController = windowController;
        windowController.setProgressIndicator(progressIndicator);
        refreshDiffMetadata(windowController.getFileSystemDiff());
    }
}
