package core;

import gui.Main;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * This class only records the metadata of a performed file system hash
 */
public class FileSystemHashMetadata {

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  CONSTRUCTORS                                                                                                   *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Create a FileSystemHash metadata from the given metadata
     * @param metadata  Metadata of the file system hash
     */
    public FileSystemHashMetadata(HashMap<String, String> metadata) {
        name = metadata.get("name");
        String datetimeString = metadata.get("date") + "_" + metadata.get("time");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        try {
            datetime = dateFormat.parse(datetimeString);
        } catch (ParseException e) {
            Main.logger.log(Level.WARNING, "Could not retrieve date and time of creation when creating file system hash" +
                    "metadata", e);
        }
        fileSystem = metadata.get("fileSystem");
        OS = metadata.get("OS");
        Path rootPath = Paths.get(metadata.get("rootPath"));
        String inputTypeStr = metadata.get("inputType");
        fileSystemInput = new FileSystemInput(inputTypeStr, rootPath, false);
        fileCount = Integer.parseInt(metadata.get("fileCount"));
        byteCount = Long.parseLong(metadata.get("byteCount"));
        errorCount = Integer.parseInt(metadata.get("errorCount"));
        try {
            duration = Duration.parse(metadata.get("duration"));
        } catch(DateTimeParseException e){
            duration = Duration.ofSeconds(0);
        }
    }

    /**
     * Create FileSystemHashMetadata from the metadata in a FileSystemHash
     * @param fileSystemHash    The FileSystemHash from where to extract the metadata
     */
    public FileSystemHashMetadata(FileSystemHash fileSystemHash) {
        name = fileSystemHash.getName();
        datetime = fileSystemHash.getDatetime();
        fileSystem = fileSystemHash.getFileSystem();
        duration = fileSystemHash.getDuration();
        OS = fileSystemHash.getOS();
        fileSystemInput = fileSystemHash.getFileSystemInput();
        fileCount = fileSystemHash.getFileCount();
        byteCount = fileSystemHash.getByteCount();
        errorCount = fileSystemHash.getErrorCount();
    }

    public FileSystemHashMetadata() {
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  ATTRIBUTES                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The name of the hash project
     */
    protected String name;

    public String getName() {
        return name;
    }

    /**
     * The date the file system hash was generated
     */
    protected Date datetime;

    public Date getDatetime() {
        return datetime;
    }

    /**
     * The duration of the hash generation
     */
    protected Duration duration;

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration){
        this.duration = duration;
    }

    /**
     * The file system
     */
    protected String fileSystem;

    public String getFileSystem() {
        return fileSystem;
    }

    /**
     * The operating system used to compute the hashes
     * This might be important for conversion between path formats
     */
    protected String OS;

    public String getOS() {
        return OS;
    }

    /**
     * The input that was used to generate the file system hash
     */
    protected FileSystemInput fileSystemInput;

    public FileSystemInput getFileSystemInput() {
        return fileSystemInput;
    }

    public Path getRootPath() {
        return fileSystemInput.getPath();
    }

    public void setRootPath(String rootPath){
        fileSystemInput.setPath(rootPath);
    }

    public InputType getInputType() {
        return fileSystemInput.getInputType();
    }

    /**
     * The number of files hashed
     */
    protected int fileCount;

    public int getFileCount() {
        return fileCount;
    }

    /**
     * The number of bytes hashed
     */
    protected long byteCount;

    public long getByteCount() {
        return byteCount;
    }

    /**
     * The number of errors encountered
     */
    protected int errorCount;

    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Format the "duration" attribute in a human readable format :
     *  3 days 1 h 10 min 43 s 331 ms
     * @return  The formatted duration
     */
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
            output += hours + " h";
        if(minutes>0 || days>0 || hours >0)
            output += minutes + " min";
        if(seconds>0 || days>0 || hours>0 || minutes>0)
            output += seconds + " s";
        if(millis>0 || days>0 || hours>0 || minutes>0 || seconds>0)
            output += millis + " ms";
        return output;
    }

    /**
     * Format the byte count in a human readable format
     * @return  The formatted byte count
     */
    public String formatByteCount() {
        String result;
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
