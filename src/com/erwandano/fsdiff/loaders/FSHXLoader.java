package com.erwandano.fsdiff.loaders;

import com.erwandano.fsdiff.core.FileSystemHash;
import javafx.concurrent.Task;

/**
 * Load a FSHX file
 */
public class FSHXLoader extends FileLoader<FileSystemHash> {

    public FSHXLoader(){}

    public FSHXLoader(String fshx){
        this.path = fshx;
    }

    @Override
    protected Task<FileSystemHash> createTask() {
        return new FSHXLoadTask(path);
    }
}
