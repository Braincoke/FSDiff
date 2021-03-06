package com.erwandano.fsdiff.diffwindow.datapane;

import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.core.DiffStatus;
import com.erwandano.fsdiff.diffwindow.DiffTreeItem;
import com.erwandano.fsdiff.diffwindow.DiffWindowController;
import com.erwandano.fxcomponents.buttons.IconButton;
import com.erwandano.hexviewer.viewer.diffviewer.HexDiffBrowser;
import com.erwandano.hexviewer.viewer.dumpviewer.HexDumpBrowser;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

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

    private ChangeListener<Number> hexFullScreenListener;

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
     */
    public void toggleExpand() {
        if(toggleExpandButton.getIcon().compareTo("EXPAND")==0){
            dataDividerPositions = dataSplitPane.getDividerPositions();
            leftDividerPositions = windowController.getSplitTabPane().getSplitPane().getDividerPositions();
            windowController.getSplitTabPane().getSplitPane().setDividerPositions(0);
            dataSplitPane.setDividerPositions(0);
            toggleExpandButton.setIcon("COMPRESS");
            application.getStage().widthProperty().addListener(hexFullScreenListener);
            application.getStage().heightProperty().addListener(hexFullScreenListener);
        } else {
            toggleExpandButton.setIcon("EXPAND");
            dataSplitPane.setDividerPositions(dataDividerPositions);
            windowController.getSplitTabPane().getSplitPane().setDividerPositions(leftDividerPositions);
            application.getStage().widthProperty().removeListener(hexFullScreenListener);
            application.getStage().heightProperty().removeListener(hexFullScreenListener);
        }
    }



    /*******************************************************************************************************************
     *                                                                                                                 *
     * SEARCH RESULTS                                                                                                  *
     *                                                                                                                 *
     ******************************************************************************************************************/
    @FXML
    private Label matchedCount;
    @FXML
    private Label modifiedCount;
    @FXML
    private Label createdCount;
    @FXML
    private Label deletedCount;
    @FXML
    private TableView<DiffTreeItem> resultsTable;
    @FXML
    private TableColumn pathColumn;
    @FXML
    private TableColumn statusColumn;
    private ObservableList<DiffTreeItem> searchResults;

    public void setWindowController(DiffWindowController windowController){
        this.windowController = windowController;
        this.application = windowController.getApplication();
        this.windowController.search();
        this.hexFullScreenListener = (observable, oldValue, newValue) -> {
            windowController.getSplitTabPane().getSplitPane().setDividerPositions(0);
            dataSplitPane.setDividerPositions(0);
        };
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
        int matched = 0;
        int modified = 0;
        int created = 0;
        int deleted = 0;
        for(DiffTreeItem item: list){
            switch (item.getStatus()){
                case MATCHED:
                    matched++;
                    break;
                case MODIFIED:
                    modified++;
                    break;
                case CREATED:
                    created++;
                    break;
                case DELETED:
                    deleted++;
                    break;
            }
        }

        matchedCount.getStyleClass().remove("hidden");
        modifiedCount.getStyleClass().remove("hidden");
        createdCount.getStyleClass().remove("hidden");
        deletedCount.getStyleClass().remove("hidden");
        if(matched>0){
            matchedCount.setVisible(true);
        } else {
            matchedCount.setVisible(false);
            matchedCount.getStyleClass().add("hidden");
        }
        if(modified>0){
            modifiedCount.setVisible(true);
        } else {
            modifiedCount.setVisible(false);
            modifiedCount.getStyleClass().add("hidden");
        }
        if(created>0){
            createdCount.setVisible(true);
        } else {
            createdCount.setVisible(false);
            createdCount.getStyleClass().add("hidden");
        }
        if(deleted>0){
            deletedCount.setVisible(true);
        } else {
            deletedCount.setVisible(false);
            deletedCount.getStyleClass().add("hidden");
        }
        matchedCount.setText(String.valueOf(matched));
        modifiedCount.setText(String.valueOf(modified));
        createdCount.setText(String.valueOf(created));
        deletedCount.setText(String.valueOf(deleted));

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
                        for(DiffStatus diffStatus : DiffStatus.values()){
                            getStyleClass().remove(diffStatus.name().toLowerCase());
                        }
                    } else {
                        // Set the path as the text
                        setText(item.getPath().toString());

                        // Style the path accordingly to its status
                        for(DiffStatus diffStatus : DiffStatus.values()){
                            getStyleClass().remove(diffStatus.name().toLowerCase());
                        }
                        getStyleClass().remove("status-cell");
                        getStyleClass().add("status-cell");
                        getStyleClass().add(item.getStatus().name().toLowerCase());
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
