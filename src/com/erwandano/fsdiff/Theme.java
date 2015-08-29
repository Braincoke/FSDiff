package com.erwandano.fsdiff;

/**
 * List of available themes
 */
public enum Theme {

    CLASSIC("Classic.css"),
    DARK("Dark.css");

    private String filename;

    public String getFilename() {
        return filename;
    }

    Theme(String filename){
        this.filename = filename;
    }


}
