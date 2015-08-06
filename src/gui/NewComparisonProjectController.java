package gui;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Dialog fo
 */
public class NewComparisonProjectController extends StageController {

    private static int HEIGHT = 220;
    private static int WIDTH = 620;
    @FXML
    private TextField projectNameText;
    @FXML
    private TextField projectLocationText;
    @FXML
    private Button OKButton;
    @FXML
    private Label warningLabel;

    private String projectName = "";
    private Path projectLocation = Paths.get("");
    private ValidationSupport validationSupport;

    public static NewComparisonProjectController init(Main application){
        String fxml = "NewComparisonProject.fxml";
        NewComparisonProjectController controller = null;
        FXMLLoader loader = new FXMLLoader();
        InputStream in = Main.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page = null;
        try {
            page = loader.load(in);
            Stage stage = new Stage();
            Scene scene = new Scene(page, WIDTH, HEIGHT);
            stage.setScene(scene);
            stage.setMinWidth(WIDTH);
            stage.setMinHeight(HEIGHT);
            stage.setMaxHeight(HEIGHT);
            stage.setTitle("New comparison project");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            controller = loader.getController();
            controller.setStage(stage);
            controller.setScene(scene);
            controller.setApplication(application);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return controller;
    }

    public void setProjectLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose the project location");
        File file = directoryChooser.showDialog(stage);
        if(file!=null) {
            projectLocation = file.toPath();
            projectLocationText.setText(projectLocation.toString());
        }
    }

    public void cancel(ActionEvent actionEvent) {
        this.stage.close();
    }

    public void next(ActionEvent actionEvent) {
        if(continueToProject()) {
            application.gotoComparisonWizard(projectName, projectLocation);
        }
        stage.close();
    }

    public void typeProjectLocation(Event event) {
        projectLocation = Paths.get(projectLocationText.getText());
    }

    /**
     * Show the dialog
     * This method should be used to reopen a dialog that was opened earlier.
     * That way the information entered previously are still there.
     */
    public void show(){
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(projectNameText, Validator.createEmptyValidator("Project name cannot be empty"));
        validationSupport.registerValidator(projectLocationText, Validator.createEmptyValidator("Must specify a valid project location"));
        validationSupport.invalidProperty().addListener((observable, oldValue, newValue) -> OKButton.setDisable(newValue));
    }

    /**
     * Verify if the path specified for the creation of a new project file
     * will to override an old one.
     */
    public boolean continueToProject(){
        boolean continueProject;
        try {
            String fileName;
            if(projectNameText.getText().endsWith(".fscx")) {
                fileName = projectNameText.getText();
                projectName = fileName.substring(0, fileName.length()-".fscx".length());
            } else {
                projectName =  projectNameText.getText();
                fileName = projectNameText.getText() + ".fscx";
            }
            projectLocation = Paths.get(projectLocationText.getText());
            Path savePath = projectLocation.resolve(fileName);
            File saveFile = savePath.toFile();
            if(saveFile.exists()){
                Alert overrideWarning = new Alert(
                        Alert.AlertType.WARNING,
                        "The project specified already exists, are you sure you want to overwrite it?"
                );
                ButtonType ok = new ButtonType("Yes", ButtonBar.ButtonData.APPLY);
                ButtonType cancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                overrideWarning.getButtonTypes().setAll(cancel, ok);
                Optional<ButtonType> result = overrideWarning.showAndWait();
                continueProject = result.get() == ok;
            } else {
                continueProject = true;
            }
        } catch (InvalidPathException e){
            Alert pathErrorAlert = new Alert(
                    Alert.AlertType.ERROR,
                    "The path specified is incorrect : \n" + e.getMessage()
            );
            pathErrorAlert.showAndWait();
            continueProject = false;
        }
        return continueProject;
    }
}
