package loaders;

import core.FileSystemDiff;
import javafx.concurrent.Task;

/**
 * Loads a FileSystemDiff from a .fscx file
 */
public class FSCXLoader extends FileLoader<FileSystemDiff>{

    public FSCXLoader(){}

    public FSCXLoader(String fscxPath){
        this.path = fscxPath;
    }

    @Override
    protected Task<FileSystemDiff> createTask() {
        return new FSCXLoadTask(path);
    }


}
