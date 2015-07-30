package gui.wizard.comparison;

import core.InputType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public abstract class ChoiceController extends  ComparisonWizardPane {

    @FXML
    protected Button nextButton;
    @FXML
    protected ComboBox<InputType> inputTypeComboBox;
    @FXML
    protected TextField locationTextField;
    @FXML
    protected Button browseButton;

    protected InputType inputType;
    protected Path input; //Whether it is a .fshx, .raw, $MFT, or a logical file, the input will be a path

    public void browse(){
        switch (inputType){
            case FSHX:
                browseFSHX();
                break;
            case LOGICAL_DIRECTORY:
                browseLogical();
                break;
        }
    }

    protected void browseFSHX(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Saved hashes", "*.fshx"));
        File file = fileChooser.showOpenDialog(application.getStage());

    }

    protected void browseLogical(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(application.getStage());
        setInput(file);
    }

    public abstract void next();

    protected void setInput(File file){
        if(file!=null) {
            input = file.toPath();
            locationTextField.setText(input.toString());
        } else {
            input = null;
            locationTextField.setText(null);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputTypeComboBox.getItems().addAll(InputType.values());
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
        validationSupport.invalidProperty().addListener((observable, oldValue, newValue) -> nextButton.setDisable(newValue));
    }
}
