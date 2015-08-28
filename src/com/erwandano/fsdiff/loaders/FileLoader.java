package com.erwandano.fsdiff.loaders;

import javafx.concurrent.Service;

/**
 * Generic class for loaders
 */
public abstract class FileLoader<T> extends Service<T> {

    /**
     * Path to the file to load
     */
    protected String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
