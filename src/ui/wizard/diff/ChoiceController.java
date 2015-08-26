package ui.wizard.diff;

import core.InputType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public abstract class ChoiceController extends DiffWizardPane {

    @FXML
    protected Button previousButton;
    @FXML
    protected Button nextButton;
    @FXML
    protected ComboBox<InputType> inputTypeComboBox;
    @FXML
    protected TextField locationTextField;
    @FXML
    protected Button browseButton;
    @FXML
    protected Text headerText;
    @FXML
    protected Text subheaderText;

    protected InputType inputType;
    protected Path inputPath; //Whether it is a .fshx, .raw, $MFT, or a logical file, the input will be a path

    @Override
    public void reload(){
        if(inputPath != null){
            locationTextField.setText(inputPath.toString());
        }
        if(inputType != null){
            inputTypeComboBox.getSelectionModel().select(inputType);
        }
    }

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
        fileChooser.setInitialDirectory(wizard.getCurrentPath().toFile());
        File file = fileChooser.showOpenDialog(application.getStage());
        setInputPath(file);

    }

    protected void browseLogical(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(wizard.getCurrentPath().toFile());
        File file = directoryChooser.showDialog(application.getStage());
        setInputPath(file);
    }

    public abstract void next();

    public abstract void previous();

    public void cancel() {
        wizard.gotoWelcomeScreen();
    }

    protected void setInputPath(File file){
        if(file!=null) {
            inputPath = file.toPath();
            locationTextField.setText(inputPath.toString());
        } else {
            inputPath = null;
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
        if(inputType==null)
            inputTypeComboBox.getSelectionModel().selectFirst();
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.registerValidator(locationTextField, Validator.createEmptyValidator("You must select a data input"));
        validationSupport.invalidProperty().addListener((observable, oldValue, newValue) -> nextButton.setDisable(newValue));
        privateInitialization();
    }

    /**
     * Initialization for extended classes
     */
    protected abstract void privateInitialization();
}
