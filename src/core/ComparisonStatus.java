package core;

/**
 * Status of a compared file or directory
 */
public enum ComparisonStatus {


    MATCHED(0),
    MODIFIED(1),
    CREATED(2),
    DELETED(3),
    ERROR(4);

    public static int SIZE = 5;

    private int index;

    ComparisonStatus(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public boolean isEqual(ComparisonStatus comparisonStatus) {
        return this.name().compareTo(comparisonStatus.name())==0;
    }
}
