package ui.loaders;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import loaders.FSCXLoader;
import loaders.FSHXLoader;
import loaders.FileLoader;
import ui.Controller;

/**
 * Controls the loading
 */
public class LoadingController extends Controller {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label loadingLabel;

    private FSCXLoader fscxLoader;

    public FSCXLoader getFSCXLoader() {
        return fscxLoader;
    }

    private FSHXLoader fshxLoader;

    public FSHXLoader getFSHXLoader() {
        return fshxLoader;
    }

    public LoadingController(){
        this.fscxLoader = new FSCXLoader();
        this.fshxLoader = new FSHXLoader();
    }

    public void loadFSHX(String fshxPath){
        loadFile(fshxLoader, fshxPath);
    }


    public void loadFSCX(String fscxPath){
        loadFile(fscxLoader, fscxPath);
    }

    public void loadFile(FileLoader loader, String path){
        loadingLabel.textProperty().unbind();
        progressIndicator.progressProperty().unbind();
        progressIndicator.setCache(true);
        loader.setPath(path);
        loadingLabel.textProperty().bind(loader.messageProperty());
        progressIndicator.progressProperty().bind(loader.progressProperty());
        loader.reset();
        loader.start();

    }

    public void cancel() {
        if(fscxLoader !=null){
            fscxLoader.cancel();
        }
        if(fshxLoader !=null){
            fshxLoader.cancel();
        }
    }
}
