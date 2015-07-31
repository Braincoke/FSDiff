package gui.comparison;

import core.FileSystemComparison;
import core.FileSystemHashMetadata;
import gui.Controller;
import gui.components.InfoItem;
import gui.components.InfoView;
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
    private InfoView comparisonView;

    @FXML
    private InfoView referenceView;

    @FXML
    private InfoView comparedView;

    //Comparison info
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
    private ComparisonWindowController windowController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
    }

    public void setWindowController(ComparisonWindowController windowController) {
        this.windowController = windowController;
        FileSystemComparison comparison = windowController.getFileSystemComparison();
        matchedItem.setText(String.valueOf(comparison.getMatchedCount()));
        modifiedItem.setText(String.valueOf(comparison.getModifiedCount()));
        createdItem.setText(String.valueOf(comparison.getCreatedCount()));
        deletedItem.setText(String.valueOf(comparison.getDeletedCount()));
        outputItem.setText(windowController.getOutputFile().toString());
        FileSystemHashMetadata reference = comparison.getReferenceFS();
        //TODO inputTypeRefItem
        pathRefItem.setText(reference.getRootPath().toString());
        filesRefItem.setText(String.valueOf(reference.getFileCount()));
        elapsedRefItem.setText(reference.formatDuration());
        sizeRefItem.setText(reference.formatByteCount());
        FileSystemHashMetadata compared = comparison.getComparedFS();
        //TODO inputTypeRefItem
        pathComItem.setText(compared.getRootPath().toString());
        filesComItem.setText(String.valueOf(compared.getFileCount()));
        elapsedComItem.setText(compared.formatDuration());
        sizeComItem.setText(compared.formatByteCount());
        comparisonView.resize(scrollPane.widthProperty());
        referenceView.resize(scrollPane.widthProperty());
        comparedView.resize(scrollPane.widthProperty());
    }
}
