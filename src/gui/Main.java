package gui;


import gui.wizard.comparison.ComparisonWizard;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private final double MINIMUM_WINDOW_WIDTH = 700.0;
    private final double MINIMUM_WINDOW_HEIGHT = 500.0;
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setTitle("FSDiff");
        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
        stage.setOnCloseRequest(e -> Platform.exit());
        gotoWelcomeScreen();
        primaryStage.show();
    }

    public void gotoWelcomeScreen() {
        try {
            WelcomeScreenController welcomeScreen = (WelcomeScreenController) replaceSceneContent("WelcomeScreen.fxml");
            welcomeScreen.setApplication(this);
            getStage().setTitle("FSDiff");
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public ComparisonWizard gotoComparisonWizard(String projectName, Path projectPath) {
        try {
            return new ComparisonWizard(this, projectName, projectPath);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void gotoHashWizard() {

    }

    public void gotoOpenProject() {

    }

    public Controller replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            page = loader.load(in);
        }
        Scene scene = new Scene(page, 700, 500);
        stage.setScene(scene);
        stage.sizeToScene();
        Controller controller = loader.getController();
        controller.setApplication(this);
        return controller;
    }

    public void replaceSceneContent(String fxml, Controller controller) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            loader.setController(controller);
            page = (AnchorPane) loader.load(in);
        }
        Scene scene = new Scene(page, 700, 500);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    public StageController openStage(String fxml) throws Exception {
        return openStage(fxml, 700, 500);
    }
    public StageController openStage(String fxml, double sceneWidth, double sceneHeight) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            page = loader.load(in);
        }
        Stage newStage = new Stage();
        Scene scene = new Scene(page, 700, 500);
        newStage.setScene(scene);
        newStage.sizeToScene();
        StageController controller = loader.getController();
        controller.setApplication(this);
        controller.setStage(newStage);
        controller.setScene(scene);
        return controller;
    }

    private Initializable switchStage(String fxml) throws Exception {
        Initializable initializable = openStage(fxml);
        stage.close();
        return initializable;
    }


    public Stage getStage() {
        return stage;
    }
}
