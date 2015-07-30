package gui.wizard.hash;

import core.InputType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * Choose the file system to hash and configure the save settings
 */
public class FSChoiceController extends HashWizardPane {

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
    private TextField locationTextField;
    /**
     * The path pointing to the input
     */
    private Path input;
    /**
     * Next button
     */
    @FXML
    private Button nextButton;
    /**
     * Cancel button
     */
    @FXML
    private Button cancelButton;

    /**
     * Updates the input file and the corresponding text field
     * @param file
     */
    public void setOutput(File file){
        if(file!=null) {
            outputDirectory = file.toPath();
            outputDirectoryTextField.setText(outputDirectory.toString());
        } else {
            input = null;
            outputDirectoryTextField.setText(null);
        }
    }

    /**
     * Updates the input file and the corresponding text field
     * @param file
     */
    public void setInput(File file){
        if(file!=null) {
            input = file.toPath();
            locationTextField.setText(input.toString());
        } else {
            input = null;
            locationTextField.setText(null);
        }
    }

    public void next(){
        wizard.setInputType(inputType);
        wizard.setFsPath(input);
        wizard.setOutputDirectory(outputDirectory);
        wizard.setName(outputNameTextField.getText());
        wizard.gotoHashPreparation();
    }

    public void cancel(){
        wizard.gotoWelcomeScreen();
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

    protected void browseLogical(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(application.getStage());
        setInput(file);
    }

    /**
     * Init input types and validation support
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for(InputType it : InputType.values()){
            if(it!=InputType.FSHX){
                inputTypeComboBox.getItems().add(it);
            }
        }
        inputTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> inputType = newValue);
        inputTypeComboBox.setCellFactory(new Callback<ListView<InputType>, ListCell<InputType>>() {
            @Override
            public ListCell<InputType> call(ListView<InputType> param) {
                final ListCell<InputType> cell = new ListCell<InputType>() {
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


                return cell;
            }
        });
        inputTypeComboBox.getSelectionModel().selectFirst();
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.registerValidator(locationTextField, Validator.createEmptyValidator("You must select a data input"));
        validationSupport.registerValidator(outputNameTextField, Validator.createEmptyValidator("You must specify a file name"));
        validationSupport.registerValidator(outputDirectoryTextField, Validator.createEmptyValidator("You must specify an output directory"));
        validationSupport.invalidProperty().addListener((observable, oldValue, newValue) -> nextButton.setDisable(newValue));
    }
}
