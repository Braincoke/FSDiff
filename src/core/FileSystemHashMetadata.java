package core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class only records the metadata of a performed file system hash
 */
public class FileSystemHashMetadata {
    protected static final Logger log = Logger.getLogger("cliLogger");

    /**
     * The name of the hash project
     */
    protected String name;
    /**
     * The date the file system hash was generated
     */
    protected Date datetime;
    /**
     * The duration of the hash generation
     */
    protected Duration duration;
    /**
     * The file system
     */
    protected String fileSystem;
    /**
     * The operating system used to compute the hashes
     * This might be important for conversion between path formats
     */
    protected String OS;
    /**
     * The path pointing to the root directory
     */
    protected Path rootPath;
    /**
     * The number of files hashed
     */
    protected int fileCount;
    /**
     * The number of bytes hashed
     */
    protected long byteCount;
    /**
     * The number of errors encountered
     */
    protected int errorCount;

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  COMPARISON GENERATION                                                                                          *
     *                                                                                                                 *
     ******************************************************************************************************************/

    FileSystemHashMetadata(HashMap<String, String> metadata) {
        name = metadata.get("name");
        String datetimeString = metadata.get("date") + "_" + metadata.get("time");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        try {
            datetime = dateFormat.parse(datetimeString);
        } catch (ParseException e) {
            log.log(Level.WARNING, "Could not retrieve date and time of creation", e);
        }
        fileSystem = metadata.get("fileSystem");
        OS = metadata.get("OS");
        rootPath = Paths.get(metadata.get("rootPath"));
        fileCount = Integer.parseInt(metadata.get("fileCount"));
        byteCount = Long.parseLong(metadata.get("byteCount"));
        errorCount = Integer.parseInt(metadata.get("errorCount"));
        duration = Duration.parse(metadata.get("duration"));
    }

    FileSystemHashMetadata(FileSystemHash fileSystemHash) {
        name = fileSystemHash.getName();
        datetime = fileSystemHash.getDatetime();
        fileSystem = fileSystemHash.getFileSystem();
        duration = fileSystemHash.getDuration();
        OS = fileSystemHash.getOS();
        rootPath = fileSystemHash.getRootPath();
        fileCount = fileSystemHash.getFileCount();
        byteCount = fileSystemHash.getByteCount();
        errorCount = fileSystemHash.getErrorCount();
    }

    public FileSystemHashMetadata() {
    }

    public String getName()  {
        return name;
    }

    public Date getDatetime() {
        return datetime;
    }

    public Duration getDuration() {
        return duration;
    }

    public String formatDuration() {
        long millis = duration.toMillis()%1000;
        long seconds = duration.getSeconds()%60;
        long minutes = duration.toMinutes()%60;
        long hours = duration.toHours()%24;
        long days = duration.toDays();
        String output ="";
        if(days>0)
            output += days + " days ";
        if(hours>0 || days >0)
            output += hours + "h";
        if(minutes>0 || days>0 || hours >0)
            output += minutes + "min";
        if(seconds>0 || days>0 || hours>0 || minutes>0)
            output += seconds + "s";
        if(millis>0 || days>0 || hours>0 || minutes>0 || seconds>0)
            output += millis + "ms";
        return output;
    }

    public String getFileSystem(){
        return fileSystem;
    }

    public String getOS() {
        return OS;
    }

    public Path getRootPath() {
        return rootPath;
    }

    public int getFileCount() {
        return fileCount;
    }

    public long getByteCount() {
        return byteCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public String formatByteCount() {
        String result = "";
        DecimalFormat df = new DecimalFormat("#.##");
        double kilobyte = 1024;
        double byteCount = this.byteCount;
        double KBCount = byteCount / kilobyte;
        double displayedCount = KBCount;
        String unit = "KB";
        //We are dealing with MB or more
        if (KBCount > 1024) {
            double MBCount = KBCount / kilobyte;
            unit = "MB";
            //We are dealing with GB or more
            if (MBCount > 1024) {
                displayedCount = MBCount / kilobyte;
                unit = "GB";
            } else {
                displayedCount = MBCount;
            }
        }
        result = String.valueOf(df.format(displayedCount)) + " " + unit;
        return result;
    }
}
