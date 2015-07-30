package core;

import java.io.FileNotFoundException;

/**
 * Exception thrown when generating hashes
 */
public class HashGenerationException extends Exception {

    //TODO Delete this class
    public HashGenerationException(String s, FileNotFoundException e) {
        super(s,e);
    }
}
