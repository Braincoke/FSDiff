package utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates a hex diff of two files
 */
public class HexDiff {

    /**
     * Compare the hex dumps of two files
     * @param reference     the file used as a base in the comparison
     * @param compared      the compared file
     * @param offset        the starting point for reading the files
     * @param nbLines       the number of hex lines to read. A line is 32 bytes.
     *
     */
    public  HexDiff(File reference, File compared, long offset, int nbLines){
        this.offset = offset;
        oldBytes = new LinkedList<>();
        newBytes = new LinkedList<>();
        StringBuilder refStrings = new StringBuilder();
        StringBuilder comStrings = new StringBuilder();

        //TODO The user wants a comparison for nbLines but we look further for a more accurate comparison
        List<HexDumpLine> refLines = HexDump.getLines(reference, offset, nbLines);
        List<HexDumpLine> comLines = HexDump.getLines(compared, offset, nbLines);
        this.nbLines = Math.max(refLines.size(), comLines.size());
        //Get the strings and the hex in blocks
        StringBuilder refHex = new StringBuilder();
        StringBuilder comHex = new StringBuilder();
        refLines.stream().forEach(hexDumpLine -> {
            refStrings.append(hexDumpLine.getStrings());
            refHex.append(hexDumpLine.getHex());
        });
        comLines.stream().forEach(hexDumpLine -> {
            comStrings.append(hexDumpLine.getStrings());
            comHex.append(hexDumpLine.getHex());
        });
        this.oldStrings = refStrings.toString();
        this.newStrings = comStrings.toString();

        //Use the hex blocks to do the comparison
        DiffUtils diff = new DiffUtils();
        LinkedList<DiffUtils.Diff> diffList = diff.diff_main(refHex.toString(), comHex.toString());
        diff.diff_cleanupSemantic(diffList);
        //We check the differences with a byte granularity instead of a nibble granularity
        DiffUtils.Operation oldOperationBuffer = null;
        DiffUtils.Operation newOperationBuffer = null;
        String oldNibbleBuffer = null;
        String newNibbleBuffer = null;
        for(DiffUtils.Diff d : diffList){
            String text = d.text;
            int textLength = d.text.length();
            //First we need to check if the buffers need to be flushed
            if(oldNibbleBuffer!=null || newNibbleBuffer != null){
                textLength--;
                String nibble = d.text.substring(0,1);
                text = d.text.substring(1);
                switch (d.operation){
                    case DELETE:
                        if(oldOperationBuffer== DiffUtils.Operation.EQUAL
                                || oldOperationBuffer== DiffUtils.Operation.DELETE){
                            oldBytes.add(new ByteDiff(DiffUtils.Operation.DELETE, oldNibbleBuffer+nibble));
                            oldNibbleBuffer = null;
                            oldOperationBuffer = null;
                        }
                        break;
                    case INSERT:
                        if(newOperationBuffer== DiffUtils.Operation.EQUAL
                                || newOperationBuffer == DiffUtils.Operation.INSERT){
                            newBytes.add(new ByteDiff(DiffUtils.Operation.INSERT, newNibbleBuffer+nibble));
                            newOperationBuffer = null;
                            newNibbleBuffer = null;
                        }
                        break;
                    case EQUAL:
                        if(oldOperationBuffer== DiffUtils.Operation.EQUAL){
                            oldBytes.add(new ByteDiff(DiffUtils.Operation.EQUAL, oldNibbleBuffer+nibble));
                            oldNibbleBuffer = null;
                            oldOperationBuffer = null;
                        } else if (oldOperationBuffer == DiffUtils.Operation.DELETE){
                            oldBytes.add(new ByteDiff(DiffUtils.Operation.DELETE, oldNibbleBuffer+nibble));
                            oldNibbleBuffer = null;
                            oldOperationBuffer = null;
                        }
                        if(newOperationBuffer == DiffUtils.Operation.EQUAL){
                            newBytes.add(new ByteDiff(DiffUtils.Operation.EQUAL, newNibbleBuffer + nibble));
                            newOperationBuffer = null;
                            newNibbleBuffer = null;
                        } else if (newOperationBuffer == DiffUtils.Operation.INSERT){
                            newBytes.add(new ByteDiff(DiffUtils.Operation.INSERT, newNibbleBuffer + nibble));
                            newOperationBuffer = null;
                            newNibbleBuffer = null;
                        }
                        break;
                }
            }
            if(textLength%2==0){
                joinNibbles(text, d.operation, textLength);
            } else {
                //One nibble will be left, we put it in the appropriate buffer
                joinNibbles(text, d.operation, textLength - 1);
                String nibble = String.valueOf(text.charAt(textLength-1));
                switch (d.operation){
                    case DELETE:
                        oldNibbleBuffer = nibble;
                        oldOperationBuffer = DiffUtils.Operation.DELETE;
                        break;
                    case INSERT:
                        newNibbleBuffer = nibble;
                        newOperationBuffer = DiffUtils.Operation.INSERT;
                        break;
                    case EQUAL:
                        oldNibbleBuffer =  nibble;
                        newNibbleBuffer =  nibble;
                        newOperationBuffer = DiffUtils.Operation.EQUAL;
                        oldOperationBuffer = DiffUtils.Operation.EQUAL;
                        break;
                }
            }
        }

    }

    private void joinNibbles(String text, DiffUtils.Operation operation, int textLength){
        switch (operation){
            case DELETE:
                for(int i=0; i<=(textLength-2); i+=2){
                    oldBytes.add(new ByteDiff(DiffUtils.Operation.DELETE, text.substring(i,i+2)));
                }
                break;
            case INSERT:
                for(int i=0; i<=(textLength-2); i+=2){
                    newBytes.add(new ByteDiff(DiffUtils.Operation.INSERT, text.substring(i, i + 2)));
                }
                break;
            case EQUAL:
                for(int i=0; i<=(textLength-2); i+=2){
                    newBytes.add(new ByteDiff(DiffUtils.Operation.EQUAL, text.substring(i,i+2)));
                }
                for(int i=0; i<=(textLength-2); i+=2){
                    oldBytes.add(new ByteDiff(DiffUtils.Operation.EQUAL, text.substring(i,i+2)));
                }
                break;
        }
    }

    /**
     * The starting point of the line
     */
    private long offset;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * The number of lines the diff contains
     * One line is 32 bytes
     */
    private int nbLines;

    public int getNbLines() {
        return nbLines;
    }

    public void setNbLines(int nbLines) {
        this.nbLines = nbLines;
    }

    /**
     * The bytes of the old version, with their state
     */
    private LinkedList<ByteDiff> oldBytes;

    public LinkedList<ByteDiff> getOldBytes() {
        return oldBytes;
    }

    public void setOldBytes(LinkedList<ByteDiff> oldBytes) {
        this.oldBytes = oldBytes;
    }

    /**
     * The bytes of the new version, with their state
     */
    private LinkedList<ByteDiff> newBytes;

    public LinkedList<ByteDiff> getNewBytes() {
        return newBytes;
    }

    public void setNewBytes(LinkedList<ByteDiff> newBytes) {
        this.newBytes = newBytes;
    }


    /**
     * The strings of the reference file
     */
    private String oldStrings;

    public String getOldStrings() {
        return oldStrings;
    }

    public void setOldStrings(String oldStrings) {
        this.oldStrings = oldStrings;
    }

    /**
     * The string of the compared file
     */
    private String newStrings;

    public String getNewStrings() {
        return newStrings;
    }

    public void setNewStrings(String newStrings) {
        this.newStrings = newStrings;
    }
}
