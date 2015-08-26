package ui.diff;

import core.DiffStatus;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import ui.Controller;
import ui.components.buttons.IconButton;
import ui.hexviewer.HexDiffBrowser;
import ui.hexviewer.HexDumpBrowser;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the search results pane and the content viewing pane
 */
public class DataPaneController extends Controller {


    public static final double STATUS_WIDTH = 100;
    private static final int HEXVIEWER_PAGE_LINES = 10;
    private DiffWindowController windowController;

    @FXML
    private SplitPane dataSplitPane;


    /*******************************************************************************************************************
     *                                                                                                                 *
     * HEX VIEWER                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/
    @FXML
    private Tab hexTab;

    @FXML
    private IconButton toggleExpandButton;

    private double[] dataDividerPositions;
    private double[] leftDividerPositions;

    private HexDumpBrowser hexDumpBrowser;
    private HexDiffBrowser hexDiffBrowser;
    /**
     * Loads the required hex view according to the item status
     * @param treeItem  The item holding the PathDiff
     */
    public void updateHexViewer(DiffTreeItem treeItem){
        Path refPath = windowController.getFileSystemDiff().getReferenceFS().getRootPath();
        Path comPath = windowController.getFileSystemDiff().getComparedFS().getRootPath();
        Path itemPath = treeItem.getPath();
        Path filePath;
        switch (treeItem.getStatus()){
            case MODIFIED:
                Path refFile = refPath.resolve(itemPath);
                Path comFile = comPath.resolve(itemPath);
                hexDiffBrowser.loadDiff(refFile.toFile(), comFile.toFile(), 0);
                hexTab.setContent(hexDiffBrowser);
                break;
            case MATCHED:
            case DELETED:
                hexDiffBrowser.cancel();
                filePath = refPath.resolve(itemPath);
                hexDumpBrowser.loadFile(filePath.toFile(), 0);
                hexTab.setContent(hexDumpBrowser);
                break;
            case CREATED:
                hexDiffBrowser.cancel();
                filePath = comPath.resolve(itemPath);
                hexDumpBrowser = new HexDumpBrowser();
                hexDumpBrowser.loadFile(filePath.toFile(), 0);
                hexTab.setContent(hexDumpBrowser);
                break;
        }
    }

    /**
     * Expand the hex view to the total size of the window or reduce it to its original size
     * //TODO Handle fullscreen event
     */
    public void toggleExpand() {
        if(toggleExpandButton.getIcon().compareTo("EXPAND")==0){
            dataDividerPositions = dataSplitPane.getDividerPositions();
            leftDividerPositions = windowController.getSplitPane().getDividerPositions();
            windowController.getSplitPane().setDividerPositions(0);
            dataSplitPane.setDividerPositions(0);
            toggleExpandButton.setIcon("COMPRESS");
        } else {
            toggleExpandButton.setIcon("EXPAND");
            dataSplitPane.setDividerPositions(dataDividerPositions);
            windowController.getSplitPane().setDividerPositions(leftDividerPositions);
        }
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * SEARCH RESULTS                                                                                                  *
     *                                                                                                                 *
     ******************************************************************************************************************/
    @FXML
    private TableView<DiffTreeItem> resultsTable;
    @FXML
    private TableColumn pathColumn;
    @FXML
    private TableColumn statusColumn;
    private ObservableList<DiffTreeItem> searchResults;

    public void setWindowController(DiffWindowController windowController){
        this.windowController = windowController;
        this.windowController.search();
    }


    /**
     * Initialise the tableView
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathColumn.setCellValueFactory(new CustomCellValueFactory());
        statusColumn.setCellValueFactory(new CustomCellValueFactory());
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

        hexDumpBrowser = new HexDumpBrowser();
        hexDiffBrowser = new HexDiffBrowser();
    }

    public void updateResults(List<DiffTreeItem> list){
        searchResults = FXCollections.observableList(list);
        resultsTable.setItems(searchResults);

    }

    /**
     * Custom cell value factory to be sure to have TreeItems in each cell
     */
    class CustomCellValueFactory implements  Callback<TableColumn.CellDataFeatures<DiffTreeItem, DiffTreeItem>,
            ObservableValue<DiffTreeItem>> {

        @Override
        public ObservableValue<DiffTreeItem> call(TableColumn.CellDataFeatures<DiffTreeItem, DiffTreeItem> param) {
            return new ReadOnlyObjectWrapper<>(param.getValue());
        }
    }

    /**
     * Custom Cell factory that displays the status of the item
     */
    class StatusCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell<DiffTreeItem, DiffTreeItem> cell = new TableCell<DiffTreeItem, DiffTreeItem>() {
                @Override
                protected void updateItem(DiffTreeItem item, boolean empty){
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Set the text
                        DiffStatus status = item.getStatus();
                        String text = status.name();
                        text = text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();
                        setText(text);
                    }
                }
            };

            return cell;
        }
    }

    /**
     * Custom cell factory that displays the path of the item and change the background color according to its status
     */
    class PathCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell<DiffTreeItem, DiffTreeItem> cell = new TableCell<DiffTreeItem, DiffTreeItem>() {
                @Override
                protected void updateItem(DiffTreeItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Set the path as the text
                        setText(item.getPath().toString());

                        // Style the path accordingly to its status
                        switch (item.getStatus()){
                            case MATCHED:
                                setStyle("");
                                break;
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

            //TODO add context menu (menus : compare to..., view hex dump, ...)
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getClickCount() > 1 && event.getButton()== MouseButton.PRIMARY) {
                    TableCell c = (TableCell) event.getSource();
                    DiffTreeItem diffTreeItem = (DiffTreeItem) c.getItem();
                    windowController.setSelectedPath(diffTreeItem);
                }
            });
            return cell;
        }
    }
}
