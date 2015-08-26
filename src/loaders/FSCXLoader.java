package loaders;

import core.FileSystemComparison;
import javafx.concurrent.Task;

/**
 * Loads a FileSystemComparison from a .fscx file
 */
public class FSCXLoader extends FileLoader<FileSystemComparison>{

    public FSCXLoader(){}

    public FSCXLoader(String fscxPath){
        this.path = fscxPath;
    }

    @Override
    protected Task<FileSystemComparison> createTask() {
        return new FSCXLoadTask(path);
    }


}
