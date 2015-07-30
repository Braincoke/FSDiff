package gui.comparison;

import core.FileSystemComparison;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controls the bottom side bar
 */
public class BottomPaneController extends HBox implements Initializable{


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

    private ComparisonWindowController windowController;

    public void refreshComparisonMetadata(FileSystemComparison comparison) {
        matchedCount.setText(String.valueOf(comparison.getMatchedCount()));
        modifiedCount.setText(String.valueOf(comparison.getModifiedCount()));
        createdCount.setText(String.valueOf(comparison.getCreatedCount()));
        deletedCount.setText(String.valueOf(comparison.getDeletedCount()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matchedColor.setFill(Paint.valueOf(CssColor.MATCHED.getBackgroundHexColor()));
        matchedColor.setOpacity(CssColor.MATCHED.getOpacity());
        modifiedColor.setFill(Paint.valueOf(CssColor.MODIFIED.getBackgroundHexColor()));
        modifiedColor.setOpacity(CssColor.MODIFIED.getOpacity());
        createdColor.setFill(Paint.valueOf(CssColor.CREATED.getBackgroundHexColor()));
        createdColor.setOpacity(CssColor.CREATED.getOpacity());
        deletedColor.setFill(Paint.valueOf(CssColor.DELETED.getBackgroundHexColor()));
        deletedColor.setOpacity(CssColor.DELETED.getOpacity());
    }

    public void setWindowController(ComparisonWindowController windowController) {
        this.windowController = windowController;
        refreshComparisonMetadata(windowController.getFileSystemComparison());
    }
}
