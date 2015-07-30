package gui.hexviewer;

import java.io.*;

/**
 * Created by Erwan Dano on 22/07/2015.
 */
public class HexDump {

    public static String getString(File file) throws IOException {
        String out = "";
        InputStream is = new FileInputStream(file);
        int i = 0;

        while (is.available() > 0) {
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder("   ");
            out += String.format("%04X  ", i * 16);
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
}
