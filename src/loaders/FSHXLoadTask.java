package loaders;

import core.FileSystemHash;
import core.FileSystemHashMetadata;
import core.HashedFile;
import javafx.concurrent.Task;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Load a FSHX file
 */
public class FSHXLoadTask  extends Task<FileSystemHash> {


    private String fshx;

    private File file;

    private int workMax;

    private XMLStreamReader reader;

    private XMLInputFactory factory;

    private TreeMap<Path, HashedFile> digests;

    private FileSystemHashMetadata metadata;


    public FSHXLoadTask(String fshx){
        this.fshx = fshx;
        this.factory = XMLInputFactory.newInstance();
    }

    @Override
    protected FileSystemHash call() throws Exception {
        if(initStream()){
            while(reader.hasNext()){
                int type =  reader.next();
                process(type);
            }
            return new FileSystemHash(metadata, digests);
        }
        return null;
    }

    /**
     * Initialise the stream and indicates if the stream was correctly initialised
     * @return  true if the stream was correctly initialised
     */
    private boolean initStream(){
        boolean success = true;
        file = new File(fshx);
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
                    metadata = loadFileSystemHashMetadata();
                    workMax = metadata.getFileCount();
                    break;
                case "hashes":
                    updateMessage("Extracting the digests");
                    loadHashes();
                    break;
            }
        }
    }
    /**
     * Extract the metadata of a file system hash
     */
    public FileSystemHashMetadata loadFileSystemHashMetadata()
            throws XMLStreamException {
        String endTag = "metadata";
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
                        type = reader.next();
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
     * Retrieve the digests
     */
    public void loadHashes() throws XMLStreamException {
        int workDone = 0;
        String endTag = "hashes";
        digests = new TreeMap<>();
        String currentElement;
        int type;
        boolean done = false;
        while(!done && reader.hasNext()){
            type = reader.next();
            if(type == XMLStreamReader.END_ELEMENT){
                done = reader.getLocalName().compareTo(endTag) == 0;
            } else if (type == XMLStreamReader.START_ELEMENT){
                currentElement = reader.getLocalName();
                if(currentElement.compareTo("file")==0){
                    HashedFile digest = loadHashedFile();
                    digests.put(digest.getPath(), digest);
                }
                workDone++;
                updateProgress(workDone, workMax);
            }
        }
    }

    public HashedFile loadHashedFile() throws XMLStreamException {
        String endTag = "file";
        Path path = Paths.get("");
        String md5 = "";
        int type;
        String currentElement;
        boolean done = false;
        while(!done && reader.hasNext()){
            type = reader.next();
            if(type == XMLStreamReader.END_ELEMENT){
                done = reader.getLocalName().compareTo(endTag)==0;
            } else if (type == XMLStreamReader.START_ELEMENT){
                currentElement = reader.getLocalName();
                do {
                    type = reader.next();
                } while (type != XMLStreamReader.CHARACTERS && type != XMLStreamReader.END_ELEMENT);
                if(type == XMLStreamReader.CHARACTERS){
                    switch (currentElement){
                        case "md5":
                            md5 = reader.getText();
                            break;
                        case "filePath":
                            path = Paths.get(reader.getText());
                            break;
                    }
                    //Look for the end of the current element and throw an exception if it cannot be found
                    do {
                        type = reader.next();
                    } while (type != XMLStreamReader.END_ELEMENT);
                    verifyClosing(currentElement);
                } else {
                    verifyClosing(currentElement);
                }
            }
        }
        HashedFile result = new HashedFile(path);
        result.setMd5(md5);
        return result;
    }

    private void verifyClosing(String currentElement) throws XMLStreamException {
        if (reader.getLocalName().compareTo(currentElement) != 0) {
            throw new XMLStreamException("The element <"
                    + currentElement + "> was not closed properly.");
        }
    }
}
