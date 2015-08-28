package com.erwandano.fsdiff.wizard.hash;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.controlsfx.control.MasterDetailPane;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

/**
 * Adds file system inputs to hash
 */
public class FSListController extends HashWizardPane {

    @FXML
    private MasterDetailPane masterDetailPane;

    @FXML
    private Button startButton;
    /**
     * The table holding the hash projects
     */
    @FXML
    private TableView<HashProject> tableView;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn inputTypeColumn;
    @FXML
    private TableColumn inputPathColumn;
    @FXML
    private TableColumn outputDirectoryColumn;
    /**
     * The detail pane
     */
    @FXML
    private AnchorPane fsChoice;
    @FXML
    private FSChoiceController fsChoiceController;

    /**
     * List of projects
     */
    private ObservableList<HashProject> projectList;

    public ObservableList<HashProject> getProjectList() {
        return projectList;
    }

    public void addProject(HashProject project) {
        projectList.add(project);
        if(startButton.isDisable())
            startButton.setDisable(false);
    }

    /**
     * Initialize
     */
    @Override
    public void init(){
        fsChoiceController.setListController(this);
    }

    /**
     * Show the form to add a new hash project
     */
    public void showAddForm() {
        if(!fsChoiceController.editMode())
            toggleDetailPane();
        else
            masterDetailPane.setShowDetailNode(true);
        fsChoiceController.activateAddMode();
    }

    /**
     * Show the form to edit an existing hash project
     */
    public void showEditForm() {
        if(tableView.getSelectionModel().getSelectedItem() != null) {
            if(fsChoiceController.editMode())
                toggleDetailPane();
            else
                masterDetailPane.setShowDetailNode(true);
            fsChoiceController.activateEditMode();
            fsChoiceController.edit(tableView.getSelectionModel().getSelectedItem());
        } else {
            //TODO Warn the user that he did not select anything
            masterDetailPane.setShowDetailNode(false);
        }
    }


    /**
     * Remove the selected file system inputs
     */
    public void removeFS() {
        if(tableView.getSelectionModel().getSelectedItem() != null) {
            int index = projectList.indexOf(tableView.getSelectionModel().getSelectedItem());
            projectList.remove(index);
        }
        if(projectList.size()<=0)
            startButton.setDisable(true);
    }

    /**
     * The tableView does not update its view when the items properties are modified
     * we have to force it to refresh the view this way.
     */
    public void reloadTable(){
        tableView.getColumns().get(0).setVisible(false);
        tableView.getColumns().get(0).setVisible(true);
    }

    public void toggleDetailPane() {
        masterDetailPane.setShowDetailNode(!masterDetailPane.isShowDetailNode());
    }

    public void next(){
        wizard.setHashProjectList(projectList);
        Queue<HashProject> queue = new LinkedList<>();
        projectList.stream().forEach(queue::add);
        wizard.setHashProjectQueue(queue);
        wizard.gotoHashPreparation();
    }

    public void cancel(){
        wizard.gotoWelcomeScreen();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectList = FXCollections.observableArrayList();
        tableView.setItems(projectList);
        /********** Setting cell value factories *****************/
        outputDirectoryColumn.setCellValueFactory(new CellValueFactory());
        inputPathColumn.setCellValueFactory(new CellValueFactory());
        inputTypeColumn.setCellValueFactory(new CellValueFactory());
        nameColumn.setCellValueFactory(new CellValueFactory());
        /*********** Setting cell factories ********************/
        outputDirectoryColumn.setCellFactory(new OutputDirCellFactory());
        inputPathColumn.setCellFactory(new InputPathCellFactory());
        inputTypeColumn.setCellFactory(new InputTypeCellFactory());
        nameColumn.setCellFactory(new NameCellFactory());
        outputDirectoryColumn.setVisible(false);
        inputTypeColumn.setVisible(false);
        tableView.setTableMenuButtonVisible(true);
        //tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        /************ Setting the context menu *****************/
        MenuItem menuDelete = new MenuItem("Delete");
        MenuItem menuEdit = new MenuItem("Edit");
        menuDelete.setOnAction(event -> {
            HashProject item = tableView.getSelectionModel().getSelectedItem();
            if(item!=null){
                projectList.remove(item);
            }
        });

        menuEdit.setOnAction(event -> {
            HashProject item = tableView.getSelectionModel().getSelectedItem();
            if (item != null) {
                showEditForm();
            }
        });

        tableView.setContextMenu(new ContextMenu(menuEdit, menuDelete));
    }

    class CellValueFactory implements Callback<TableColumn.CellDataFeatures<HashProject, HashProject>,
            ObservableValue<HashProject>> {

        @Override
        public ObservableValue<HashProject> call(TableColumn.CellDataFeatures<HashProject, HashProject> param) {
            return new ReadOnlyObjectWrapper<>(param.getValue());
        }
    }

    class NameCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell cell = new TableCell<HashProject, HashProject>() {
                @Override
                protected void updateItem(HashProject item, boolean empty){
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.getName());
                    }
                }
            };
            return cell;
        }
    }
    class OutputDirCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell cell = new TableCell<HashProject, HashProject>() {
                @Override
                protected void updateItem(HashProject item, boolean empty){
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.getOutputDirectory().toString());
                    }
                }
            };
            return cell;
        }
    }
    class InputTypeCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell cell = new TableCell<HashProject, HashProject>() {
                @Override
                protected void updateItem(HashProject item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Set the path as the text
                        setText(item.getFileSystemInput().getInputType().getDescription());
                    }
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getClickCount() > 1) {
                    TableCell c = (TableCell) event.getSource();
                    HashProject hashProject = (HashProject) c.getItem();
                }
            });
            return cell;
        }
    }

    class InputPathCellFactory implements Callback<TableColumn, TableCell> {

        @Override
        public TableCell call(TableColumn param) {
            TableCell cell = new TableCell<HashProject, HashProject>() {
                @Override
                protected void updateItem(HashProject item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Set the path as the text
                        setText(item.getFileSystemInput().getPath().toString());
                    }
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getClickCount() > 1) {
                    TableCell c = (TableCell) event.getSource();
                    HashProject hashProject = (HashProject) c.getItem();
                }
            });
            return cell;
        }
    }

}
