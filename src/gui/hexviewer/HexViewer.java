package gui.hexviewer;

/**
 * Created by Erwan Dano on 22/07/2015.
 */

import java.io.File;
import java.io.IOException;

public class HexViewer {




    public final static void main(String[] args) {
        String filePath = "test-resources/hexviewer/alphabet.txt";
        // dump to the console
        try {
            HexDump.print(System.out, new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
