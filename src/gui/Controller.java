package gui;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Erwan Dano on 24/07/2015.
 */
public abstract class Controller implements Initializable {

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
