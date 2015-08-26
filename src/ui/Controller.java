package ui;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * An FXML Controller
 */
public abstract class Controller implements Initializable {

    /**
     * The application the Controller is a part of
     */
    protected Main application;

    public Main getApplication() {
        return application;
    }

    public void setApplication(Main application) {
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
