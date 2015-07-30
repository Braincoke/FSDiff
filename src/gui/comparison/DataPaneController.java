package gui.comparison;

import core.ComparisonStatus;
import gui.hexviewer.HexDump;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Erwan Dano on 10/07/2015.
 */
public class DataPaneController extends AnchorPane implements Initializable {


    public static final double STATUS_WIDTH = 100;
    private ComparisonWindowController windowController;
    /***********************************************************************
     * HEX VIEWER
     **********************************************************************/
    @FXML
    private TextArea hexViewer;
    @FXML
    private ProgressIndicator loadingIndicator;
    /***********************************************************************
     * SEARCH RESULTS
     **********************************************************************/
    @FXML
    private TableView<ComparisonTreeItem> resultsTable;
    @FXML
    private TableColumn pathColumn;
    @FXML
    private TableColumn statusColumn;
    private ObservableList<ComparisonTreeItem> searchResults;

    public void setWindowController(ComparisonWindowController windowController){
        this.windowController = windowController;
        this.windowController.search();
    }

    public void updateHexViewer(ComparisonTreeItem treeItem){
        Path refPath = windowController.getFileSystemComparison().getReferenceFS().getRootPath();
        Path comPath = windowController.getFileSystemComparison().getComparedFS().getRootPath();
        Path itemPath = treeItem.getPath();
        Path filePath;
        switch (treeItem.getStatus()){
            case MATCHED:
            case MODIFIED:
            case DELETED:
                filePath = refPath.resolve(itemPath);
                break;
            case CREATED:
                filePath = comPath.resolve(itemPath);
                break;
            default:
                filePath = refPath.resolve(itemPath);
                break;
        }
        loadingIndicator.setVisible(true);
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    String text = HexDump.getString(filePath.toFile());
                    hexViewer.setText(text);
                    return true;
                } catch (IOException e) {
                    hexViewer.setText("Could not load file : " + filePath +" \n" + e.getMessage());
                    return false;
                }
            }
        };
        task.setOnSucceeded(event -> loadingIndicator.setVisible(false));
        new Thread(task).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathColumn.setCellValueFactory(new PathCellValueFactory());
        statusColumn.setCellValueFactory(new StatusCellValueFactory());
        pathColumn.setCellFactory(new PathCellFactory());
        statusColumn.setCellFactory(new StatusCellFactory());
        //The path column automatically takes the remaining room to display its contents
        statusColumn.visibleProperty().addListener((observable1, oldValue1, newValue1) -> {
             if(newValue1){
                 pathColumn.prefWidthProperty().setValue(resultsTable.widthProperty().doubleValue()-STATUS_WIDTH);

             } else {
                 pathColumn.prefWidthProperty().setValue(resultsTable.widthProperty().doubleValue());
             }
        });
        resultsTable.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (statusColumn.isVisible()) {
                pathColumn.prefWidthProperty().setValue(newValue.doubleValue() - STATUS_WIDTH);
            } else {
                pathColumn.prefWidthProperty().setValue(newValue.doubleValue());
            }
        });
        statusColumn.setVisible(false);
        resultsTable.setTableMenuButtonVisible(true);
    }

    public void updateResults(List<ComparisonTreeItem> list){
        searchResults = FXCollections.observableList(list);
        resultsTable.setItems(searchResults);

    }

    class StatusCellValueFactory implements  Callback<TableColumn.CellDataFeatures<ComparisonTreeItem, ComparisonTreeItem>,
            ObservableValue<ComparisonTreeItem>> {

        @Override
        public ObservableValue<ComparisonTreeItem> call(TableColumn.CellDataFeatures<ComparisonTreeItem, ComparisonTreeItem> param) {
            return new ReadOnlyObjectWrapper<>(param.getValue());
        }
    }

    class PathCellValueFactory implements Callback<TableColumn.CellDataFeatures<ComparisonTreeItem, ComparisonTreeItem>,
            ObservableValue<ComparisonTreeItem>> {

        @Override
        public ObservableValue<ComparisonTreeItem> call(TableColumn.CellDataFeatures<ComparisonTreeItem, ComparisonTreeItem> param) {
            return new ReadOnlyObjectWrapper<>(param.getValue());
        }
    }

    class StatusCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell cell = new TableCell<ComparisonTreeItem, ComparisonTreeItem>() {
                @Override
                protected void updateItem(ComparisonTreeItem item, boolean empty){
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Set the text
                        ComparisonStatus status = item.getStatus();
                        String text = status.name();
                        text = text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();
                        setText(text);
                    }
                }
            };
            return cell;
        }
    }

    class PathCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell cell = new TableCell<ComparisonTreeItem, ComparisonTreeItem>() {
                @Override
                protected void updateItem(ComparisonTreeItem item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Set the path as the text
                        setText(item.getPath().toString());

                        // Style the path accordingly to its status
                        switch (item.getStatus()){
                            case MODIFIED:
                                setTextFill(CssColor.MODIFIED.getTextColor());
                                setStyle("-fx-background-color:" + CssColor.MODIFIED.getBackgroundRgba());
                                break;
                            case CREATED:
                                setTextFill(CssColor.CREATED.getTextColor());
                                setStyle("-fx-background-color:" + CssColor.CREATED.getBackgroundRgba());
                                break;
                            case DELETED:
                                setTextFill(CssColor.DELETED.getTextColor());
                                setStyle("-fx-background-color:" + CssColor.DELETED.getBackgroundRgba());
                                break;
                        }
                    }
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getClickCount() > 1) {
                    TableCell c = (TableCell) event.getSource();
                    ComparisonTreeItem comparisonTreeItem = (ComparisonTreeItem) c.getItem();
                    windowController.setSelectedPath(comparisonTreeItem);
                }
            });
            return cell;
        }
    }
}
