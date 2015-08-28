package com.erwandano.fsdiff.diffwindow.leftmenu;

/**
 * Status of a compared file or directory
 */
public enum DiffStatus {


    MATCHED(0),
    MODIFIED(1),
    CREATED(2),
    DELETED(3),
    ERROR(4);

    public static int SIZE = 5;

    private int index;

    DiffStatus(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public boolean isEqual(DiffStatus diffStatus) {
        return this.name().compareTo(diffStatus.name())==0;
    }
}
