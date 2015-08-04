package gui.wizard.hash;

import core.FileSystemInput;
import core.InputType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Choose the file system to hash and configure the save settings
 */
public class FSChoiceController extends HashWizardPane {

    /**
     * Indicates if the user is editing an existing project
     * If editMode == false, then the user is adding a project
     */
    private boolean editMode;

    @FXML
    private Text editionMode;

    public boolean editMode() {
        return editMode;
    }

    public void activateAddMode() {
        editionMode.setText("Adding a new project");
        editMode = false;
    }

    public void activateEditMode() {
        editionMode.setText("Editing an existing project");
        editMode = true;
    }

    /**
     * The name given to the output file
     */
    @FXML
    private TextField outputNameTextField;

    /**
     * The output directory
     */
    @FXML
    private TextField outputDirectoryTextField;


    /**
     * The output directory
     */
    private Path outputDirectory;
    /**
     * The input type list
     */
    @FXML
    private ComboBox<InputType> inputTypeComboBox;
    /**
     * The selected input type
     */
    @FXML
    private InputType inputType;
    /**
     * The location of the file system
     */
    @FXML
    private TextField inputPathTextField;
    /**
     * The path pointing to the input
     */
    private Path inputPath;
    /**
     * Next button
     */
    @FXML
    private Button okButton;
    /**
     * Cancel button
     */
    @FXML
    private Button cancelButton;

    /**
     * List controller
     */
    private FSListController listController;

    public void setListController(FSListController listController) {
        this.listController = listController;
        setWizard(listController.getWizard());
    }

    /**
     * Selected item for editing in the table
     */
    private HashProject selectedItem;

    /**
     * Updates the input file and the corresponding text field
     * @param file
     */
    public void setOutput(File file){
        if(file!=null) {
            outputDirectory = file.toPath();
            outputDirectoryTextField.setText(outputDirectory.toString());
        } else {
            inputPath = null;
            outputDirectoryTextField.setText(null);
        }
    }

    /**
     * Updates the input file and the corresponding text field
     * @param file
     */
    public void setInputPath(File file) {
        if(file!=null) {
            inputPath = file.toPath();
            inputPathTextField.setText(inputPath.toString());
            if(outputNameTextField.getText()==null || outputNameTextField.getText().trim().isEmpty()) {
                String name = inputPath.getNameCount()==0 ? inputPath.toString() : inputPath.getFileName().toString();
                outputNameTextField.setText(makeFileNameValid(name));
            }
        } else {
            inputPath = null;
            inputPathTextField.setText(null);
        }
    }

    private String makeFileNameValid(String filename){
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public void browseOutput(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(application.getStage());
        setOutput(file);
    }

    public void browseInput(){
        switch (inputType){
            case LOGICAL_DIRECTORY:
                browseLogical();
                break;
        }
    }

    public void browseLogical(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(application.getStage());
        setInputPath(file);
    }


    public void applyChanges() {
        inputPath = Paths.get(inputPathTextField.getText());
        outputDirectory = Paths.get(outputDirectoryTextField.getText());
        if(editMode){
            saveEdit();
            toggleDetailPane();
        } else {
            addProject();
        }
    }

    public void addProject(){
        FileSystemInput fsi = new FileSystemInput(inputType, inputPath, false);
        HashProject project = new HashProject(fsi, outputNameTextField.getText(), outputDirectory);
        listController.addProject(project);
    }

    public void edit(HashProject selectedItem) {
        this.selectedItem = selectedItem;
        outputNameTextField.setText(selectedItem.getName());
        outputDirectory = selectedItem.getOutputDirectory();
        outputDirectoryTextField.setText(outputDirectory.toString());
        inputType = selectedItem.getFileSystemInput().getInputType();
        inputTypeComboBox.getSelectionModel().select(inputType);
        inputPath = selectedItem.getFileSystemInput().getPath();
        inputPathTextField.setText(inputPath.toString());
    }

    public void saveEdit(){
        FileSystemInput fsi = new FileSystemInput(inputType, inputPath, false);
        selectedItem.setFileSystemInput(fsi);
        selectedItem.setName(outputNameTextField.getText());
        selectedItem.setOutputDirectory(outputDirectory);
        listController.reloadTable();
    }

    public void toggleDetailPane() {
        listController.toggleDetailPane();
    }

    /**
     * Init input types and validation support
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* ***************** Init input type ComboBox *************************************/
        for(InputType it : InputType.values()){
            if(it!=InputType.FSHX){
                inputTypeComboBox.getItems().add(it);
            }
        }
        inputTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> inputType = newValue);
        inputTypeComboBox.setCellFactory(new Callback<ListView<InputType>, ListCell<InputType>>() {
            @Override
            public ListCell<InputType> call(ListView<InputType> param) {

                return new ListCell<InputType>() {
                    @Override
                    public void updateItem(InputType item,
                                           boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null || !empty)
                            this.setText(item.getDescription());
                        else
                            setText(null);
                    }
                };
            }
        });
        inputTypeComboBox.getSelectionModel().selectFirst();
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.registerValidator(inputPathTextField, Validator.createEmptyValidator("You must select a data input"));
        validationSupport.registerValidator(outputNameTextField, Validator.createEmptyValidator("You must specify a file name"));
        validationSupport.registerValidator(outputDirectoryTextField, Validator.createEmptyValidator("You must specify an output directory"));
        validationSupport.invalidProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(newValue));

        /* ****************** Init tableView **********************************************/
        //tableView.setItems(projectList);
    }

}
