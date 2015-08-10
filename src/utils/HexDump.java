package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generate a hexdump of a file
 */
public class HexDump {


    public static List<HexDumpLine> getLines(File file, long offset, int nbLines) {
        ArrayList<HexDumpLine> hexDumpLines = new ArrayList<>();
        int remainingLines = nbLines;
        long currentOffset = offset;
        try {
            InputStream is = new FileInputStream(file);
            long skipped = is.skip(offset);
            if(skipped!=offset){
                currentOffset = (int) skipped;
            }
            while(is.available()>0 && remainingLines>0){
                //Read 32 bytes to create a hex dump line
                hexDumpLines.add(new HexDumpLine(is, currentOffset));
                remainingLines--;
                currentOffset += 32;
            }
            is.close();
        } catch (IOException e){
            int size = hexDumpLines.size();
            int errorOffset = 1028;
            for(int i=size; i<nbLines; i++) {
                hexDumpLines.add(new HexDumpLine(errorOffset, "..IO.EXCEPTION..", "..IO.EXCEPTION.."));
            }
            Logger.getLogger(HexDump.class.getName()).log(Level.WARNING, "Error when loading the file : ", e);
        }
        return hexDumpLines;
    }

    /**
     * Reads a part of a file and outputs the formatted hex dump in a String
     * @param file      The file to read
     * @param offset    The byte from where to start reading (starts at 0)
     * @param lines     The number of lines to display. A line is composed of 16 hexadecimal chars (32 bytes)
     * @return          The formatted hex dump
     * @throws IOException
     */
    public static String getString(File file, int offset, int lines) throws IOException {
        String out = "";
        InputStream is = new FileInputStream(file);
        long skipped = is.skip(offset);
        int remaining = lines;

        int lineOffset = lines;
        while (is.available()>0 && remaining>0){
            out += formatLineOutput(is, lineOffset);
            lineOffset++;
            remaining--;
        }
        return out;
    }

    public static String getString(File file) throws IOException {
        String out = "";
        InputStream is = new FileInputStream(file);
        int i = 0;

        while (is.available() > 0) {
            out += formatLineOutput(is, i);
            i++;
        }
        is.close();
        return out;
    }


    public static void print(PrintStream out, File file) throws IOException {
        InputStream is = new FileInputStream(file);
        int i = 0;

        while (is.available() > 0) {
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder("   ");
            out.printf("%04X  ", i * 16);
            for (int j = 0; j < 16; j++) {
                if (is.available() > 0) {
                    int value = is.read();
                    sb1.append(String.format("%02X ", value));
                    if (!Character.isISOControl(value)) {
                        sb2.append((char)value);
                    }
                    else {
                        sb2.append(".");
                    }
                }
                else {
                    for (;j < 16;j++) {
                        sb1.append("   ");
                    }
                }
            }
            out.print(sb1);
            out.println(sb2);
            i++;
        }
        is.close();
    }

    public static String formatLineOutput(InputStream is, int lineOffset) throws IOException {
        String out = "";
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder("   ");
        out += String.format("%04X  ", lineOffset * 16);
        for (int j = 0; j < 16; j++) {
            if (j==8){
                sb1.append(" ");
            }
            if (is.available() > 0) {
                int value = is.read();
                sb1.append(String.format("%02X ", value));
                if (!Character.isISOControl(value)) {
                    sb2.append((char)value);
                }
                else {
                    sb2.append(".");
                }
            }
            else {
                for (;j < 16;j++) {
                    sb1.append("   ");
                }
            }
        }
        out += sb1;
        out += sb2 + "\n";
        return out;
    }
}
