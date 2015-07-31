package gui;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private String projectName = "";
    private Path projectLocation = Paths.get("");
    private ValidationSupport validationSupport;

    public static NewComparisonProjectController init(Main application){
        String fxml = "NewComparisonProject.fxml";
        FXMLLoader loader = new FXMLLoader();
        InputStream in = Main.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page = null;
        try {
            page = (AnchorPane) loader.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Stage stage = new Stage();
        Scene scene = new Scene(page, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setMaxHeight(HEIGHT);
        stage.setTitle("New comparison project");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
        NewComparisonProjectController controller = loader.getController();
        controller.setStage(stage);
        controller.setScene(scene);
        controller.setApplication(application);
        return controller;
    }

    public void setProjectLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose the project location");
        File file = directoryChooser.showDialog(stage);
        projectLocation = file.toPath();
        projectLocationText.setText(projectLocation.toString());
    }

    public void cancel(ActionEvent actionEvent) {
        this.stage.close();
    }

    public void next(ActionEvent actionEvent) {
        projectName = projectNameText.getText();
        application.gotoComparisonWizard(projectName,projectLocation);
        stage.close();
    }

    public void typeProjectLocation(Event event) {
        projectLocation = Paths.get(projectLocationText.getText());
    }

    //TODO check if this works once closed at least once
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
}
