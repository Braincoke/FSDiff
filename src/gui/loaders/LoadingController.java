package gui.loaders;

import gui.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import loaders.FSHXLoader;
import loaders.FileLoader;
import loaders.FscxLoader;

/**
 * Controls the loading
 */
public class LoadingController extends Controller {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label loadingLabel;

    private FscxLoader fscxLoader;

    public FscxLoader getFSCXLoader() {
        return fscxLoader;
    }

    private FSHXLoader fshxLoader;

    public FSHXLoader getFSHXLoader() {
        return fshxLoader;
    }

    public LoadingController(){
        this.fscxLoader = new FscxLoader();
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
