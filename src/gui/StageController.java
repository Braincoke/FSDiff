package gui;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Erwan Dano on 24/07/2015.
 */
public abstract class StageController extends Controller {

    protected Stage stage;
    protected Scene scene;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
