package loaders;

import core.FileSystemComparison;
import javafx.concurrent.Task;

/**
 * Loads a FileSystemComparison from a .fscx file
 */
public class FscxLoader extends FileLoader<FileSystemComparison>{

    public FscxLoader(){}

    public FscxLoader(String fscxPath){
        this.path = fscxPath;
    }

    @Override
    protected Task<FileSystemComparison> createTask() {
        return new FSCXLoadTask(path);
    }


}
