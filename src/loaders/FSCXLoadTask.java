package loaders;

import core.DiffStatus;
import core.FileSystemDiff;
import core.FileSystemHashMetadata;
import core.PathDiff;
import javafx.concurrent.Task;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Load a FSCX file
 */
public class FSCXLoadTask extends Task<FileSystemDiff> {

    /**
     * Path to the fscx file
     */
    private String fscx;

    /**
     * The fscx file
     */
    private File file;

    /**
     * The XML reader
     */
    private XMLStreamReader reader;

    /**
     * The factory
     */
    private XMLInputFactory factory;

    /**
     * The list of metadata extracted
     */
    private List<Integer> metadata;

    /**
     * The metadata of the reference file system
     */
    private FileSystemHashMetadata referenceFS;

    /**
     * The metadata of the compared file system
     */
    private FileSystemHashMetadata comparedFS;

    /**
     * The name given to the differential
     */
    private String name;

    /**
     * The time the differential was produced
     */
    private Date datetime;

    /**
     * The differentials
     */
    private ArrayList<PathDiff> comparisonList;

    /**
     * The differentials
     */
    private TreeSet<PathDiff> comparisonSet;


    /**
     * Index pointing to the last parent path in the list of path differentials.
     * Used to speed up the discovery of the parent in the list of path differentials.
     */
    private int index;

    /**
     * The indexes of the directories found
     * The key is the level of the directory
     * The value is the list of indexes for directory with that level
     * The indexes refer to positions in the list of PathDiff
     */
    private HashMap<Integer, ArrayList<Integer>> directories;

    /**
     * The number of path to parse. Used to give an indication of the progress.
     */
    private int workMax;

    public FSCXLoadTask(String fscx) {
        this.fscx = fscx;
        this.factory = XMLInputFactory.newInstance();
    }

    @Override
    protected FileSystemDiff call() throws Exception {
        if(initStream()){
            while(reader.hasNext()){
                int type =  reader.next();
                process(type);
            }
            return new FileSystemDiff(
                    referenceFS,
                    comparedFS,
                    metadata,
                    datetime,
                    comparisonSet,
                    name
            );
        }
        return null;
    }

    /**
     * Initialise the stream and indicates if the stream was correctly initialised
     * @return  true if the stream was correctly initialised
     */
    private boolean initStream(){
        boolean success = true;
        file = new File(fscx);
        workMax = 0;
        try {
            reader = factory.createXMLStreamReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            updateMessage("File not found. " + e.getMessage());
            success  = false;
        } catch (XMLStreamException e) {
            updateMessage("Error when creating the stream. " + e.getMessage());
            success = false;
        }
        return success;
    }

    /**
     * Process the new XML Element read in the stream
     * @param type  The XMLStreamReader type
     */
    private void process(int type) throws XMLStreamException {
        if(XMLStreamReader.START_ELEMENT == type){
            switch (reader.getLocalName()){
                case "metadata":
                    updateMessage("Extracting metadata");
                    loadMetadata();
                    break;
                case "comparison":
                    updateMessage("Extracting the differential");
                    loadDiff();
                    updateMessage("Loading the user interface");
                    break;
            }
        }
    }

    /**
     * Extracts the metadata
     */
    private void loadMetadata() throws XMLStreamException {
        metadata = new ArrayList<>();
        boolean done = false;
        int type;
        String currentElement;
        String dateText = null;
        String timeText = null;
        while(!done && reader.hasNext()) {
            type = reader.nextTag();
            currentElement = reader.getLocalName();
            if (type == XMLStreamReader.START_ELEMENT) {
                type = reader.next();
                if (type == XMLStreamReader.CHARACTERS) {
                    switch (currentElement) {
                        case "name":
                            this.name = reader.getText();
                            break;
                        case "date":
                            dateText = reader.getText();
                            break;
                        case "time":
                            timeText = reader.getText();
                            break;
                        case "matchedCount":
                            int matchedCount = Integer.parseInt(reader.getText());
                            workMax += matchedCount;
                            metadata.add(DiffStatus.MATCHED.getIndex(),
                                    matchedCount);
                            break;
                        case "modifiedCount":
                            int modifiedCount = Integer.parseInt(reader.getText());
                            workMax += modifiedCount;
                            metadata.add(DiffStatus.MODIFIED.getIndex(),
                                    modifiedCount);
                            break;
                        case "createdCount":
                            int createdCount = Integer.parseInt(reader.getText());
                            workMax += createdCount;
                            metadata.add(DiffStatus.CREATED.getIndex(),
                                    createdCount);
                            break;
                        case "deletedCount":
                            int deletedCount = Integer.parseInt(reader.getText());
                            workMax += deletedCount;
                            metadata.add(DiffStatus.DELETED.getIndex(),
                                    deletedCount);
                            break;
                        case "errorCount":
                            metadata.add(DiffStatus.ERROR.getIndex(),
                                    Integer.parseInt(reader.getText()));
                            break;
                        case "referenceFS":
                            updateMessage("Loading the reference file system");
                            referenceFS = loadFileSystemHashMetadata(true);
                            break;
                        case "comparedFS":
                            updateMessage("Loading the compared file system");
                            comparedFS = loadFileSystemHashMetadata(false);
                            break;
                    }
                    //Look for the end of the current element and throw an exception if it cannot be found
                    if(currentElement.compareTo("comparedFS")!=0 && currentElement.compareTo("referenceFS")!=0) {
                        do {
                            type = reader.nextTag();
                        } while (type != XMLStreamReader.END_ELEMENT && reader.hasNext());
                        verifyClosing(currentElement);
                    }
                } else if (type == XMLStreamReader.END_ELEMENT){
                    verifyClosing(currentElement);
                }
            } else if (type == XMLStreamReader.END_ELEMENT){
                done = reader.getLocalName().compareTo("metadata") == 0;
            }
        }
        String datetimeString = dateText + "_" + timeText;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        this.datetime = null;
        try {
            datetime = dateFormat.parse(datetimeString);
        } catch (ParseException e) {
            datetime = Date.from(Instant.now());
        }
    }

    /**
     * Extract the metadata of a file system hash
     * @param isReference    Indicates if we are dealing with the reference file system
     */
    public FileSystemHashMetadata loadFileSystemHashMetadata(boolean isReference)
            throws XMLStreamException {
        String endTag = isReference ? "referenceFS" : "comparedFS";
        HashMap<String, String> fshMetadata = new HashMap<>();
        int type;
        String currentElement;
        boolean done = false;

        while(!done && reader.hasNext()){
            type = reader.next();
            if(type == XMLStreamReader.START_ELEMENT){
                currentElement = reader.getLocalName();
                type = reader.next();
                if(type == XMLStreamReader.CHARACTERS) {
                    fshMetadata.put(currentElement, reader.getText());
                    //Look for the end of the current element and throw an exception if it cannot be found
                    do {
                        type = reader.nextTag();
                    } while (type != XMLStreamReader.END_ELEMENT);
                    verifyClosing(currentElement);
                } else if (type == XMLStreamReader.END_ELEMENT){
                    verifyClosing(currentElement);
                }
            } else if( type == XMLStreamReader.END_ELEMENT){
                done = reader.getLocalName().compareTo(endTag) == 0;
            }
        }
        return new FileSystemHashMetadata(fshMetadata);
    }

    /**
     * Load the differential of the two file systems
     */
    private void loadDiff() throws XMLStreamException {
        index = 0;
        directories = new HashMap<>();
        String endTag = "comparison";
        comparisonList = new ArrayList<>();
        boolean done = false;
        int type;
        int workDone = 0;
        updateProgress(workDone, workMax);
        PathDiff p;
        while(!done && reader.hasNext()) {
            type = reader.nextTag();
            if(type == XMLStreamReader.START_ELEMENT){
                if(reader.getLocalName().compareTo("pathComparison")==0) {
                    p = loadPathDiff();
                    comparisonList.add(p);
                    if(!p.isDirectory())
                        workDone++;
                    p = null;
                    updateProgress(workDone, workMax);
                }
            } else if (type == XMLStreamReader.END_ELEMENT){
                done = reader.getLocalName().compareTo(endTag) == 0;
            }
        }
        comparisonSet = new TreeSet<>(comparisonList);
        comparisonList = null;
    }

    /**
     * Extract a pathDiff from the XML
     * @return  The path differential
     */
    private PathDiff loadPathDiff() throws XMLStreamException {
        boolean done = false;
        int type;
        PathDiff pathDiff = new PathDiff();
        String currentElement;
        if(reader.getAttributeCount()>0){
            for(int i=0; i<reader.getAttributeCount(); i++){
                if (reader.getAttributeLocalName(i).compareTo("type")==0){
                    String value = reader.getAttributeValue(i);
                    if(value.compareTo("file")==0){
                        pathDiff.setIsDirectory(false);
                    } else if (value.compareTo("directory")==0){
                        pathDiff.setIsDirectory(true);
                    }
                }
            }
        }
        while(!done && reader.hasNext()){
            type = reader.nextTag();
            if (type == XMLStreamReader.START_ELEMENT){
                if(reader.getLocalName().compareTo("directoryStatus")==0){
                    loadDirectoryStatus(pathDiff);
                } else {
                    currentElement = reader.getLocalName();
                    type = reader.next();
                    if(type == XMLStreamReader.CHARACTERS) {
                        switch (currentElement) {
                            case "path":
                                pathDiff.setPath(Paths.get(reader.getText()));
                                if(pathDiff.isDirectory()){
                                    int level = pathDiff.getPath().getNameCount();
                                    if(!directories.containsKey(level)){
                                        directories.put(level, new ArrayList<>());
                                    }
                                    directories.get(level).add(comparisonList.size());
                                }
                                break;
                            case "parent":
                                String parentPath = reader.getText();
                                if(parentPath.compareToIgnoreCase("null")!=0){
                                    boolean found = false;
                                    int level = Paths.get(parentPath).getNameCount();
                                    ArrayList<Integer> indexes = directories.get(level);
                                    int i = indexes.size()-1;
                                    while(!found && i>0){
                                        PathDiff p = comparisonList.get(indexes.get(i));
                                        if (p.getPath().toString().compareTo(parentPath) == 0) {
                                            pathDiff.setParent(p);
                                            found = true;
                                        }
                                        i--;
                                    }
                                } else {
                                    pathDiff.setParent(new PathDiff(
                                            Paths.get("")
                                    ));
                                }
                                break;
                            case "status":
                                String statusText = reader.getText();
                                for (DiffStatus s : DiffStatus.values()) {
                                    if (s.name().compareToIgnoreCase(statusText) == 0) {
                                        pathDiff.setStatus(s);
                                    }
                                }
                                break;
                        }
                        while(reader.hasNext() && type!= XMLStreamReader.END_ELEMENT){
                            type = reader.next();
                        }
                        verifyClosing(currentElement);
                    } else if( type == XMLStreamReader.END_ELEMENT){
                        verifyClosing(currentElement);
                    }
                }
            } else if (type == XMLStreamReader.END_ELEMENT){
                done = reader.getLocalName().compareTo("pathComparison")==0;
            }
        }
        return pathDiff;
    }

    private void loadDirectoryStatus(PathDiff pathDiff) throws XMLStreamException {
        boolean done = false;
        String currentElement;
        int type;
        int[] directoryStatus = new int[DiffStatus.SIZE];
        while(!done && reader.hasNext()){
            type = reader.nextTag();
            currentElement = reader.getLocalName();
            if(type == XMLStreamReader.END_ELEMENT){
                done = currentElement.compareTo("directoryStatus") == 0;
            } else if(type == XMLStreamReader.START_ELEMENT){
                type = reader.next();
                if(type == XMLStreamReader.CHARACTERS) {
                    int value = 0;
                    try {
                        value = Integer.parseInt(reader.getText());
                    } catch( NumberFormatException ignore){}
                    switch (currentElement) {
                        case "matched":
                            directoryStatus[DiffStatus.MATCHED.getIndex()] = value;
                            break;
                        case "modified":
                            directoryStatus[DiffStatus.MODIFIED.getIndex()] = value;
                            break;
                        case "created":
                            directoryStatus[DiffStatus.CREATED.getIndex()] = value;
                            break;
                        case "deleted":
                            directoryStatus[DiffStatus.DELETED.getIndex()] = value;
                            break;
                        case "error":
                            directoryStatus[DiffStatus.ERROR.getIndex()] = value;
                            break;
                    }
                    do {
                        type = reader.nextTag();
                    } while (type != XMLStreamReader.END_ELEMENT);
                    verifyClosing(currentElement);
                } else if (type == XMLStreamReader.END_ELEMENT){
                    verifyClosing(currentElement);
                }
            }
        }
        pathDiff.setDirectoryStatus(directoryStatus);
    }

    private void verifyClosing(String currentElement) throws XMLStreamException {
            if (reader.getLocalName().compareTo(currentElement) != 0) {
                throw new XMLStreamException("The element <"
                + currentElement + "> was not closed properly.");
        }
    }

}
