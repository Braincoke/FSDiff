package loaders;

import core.ComparisonStatus;
import core.FileSystemComparison;
import core.FileSystemHashMetadata;
import core.PathComparison;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * Loads a FileSystemComparison from a .fscx file
 */
public class FscxLoader extends Service<FileSystemComparison>{

    public FscxLoader(String fscxPath){
        this.fscxPath = fscxPath;
    }

    /**
     * The path to the .fscx file
     */
    private String fscxPath;


    @Override
    protected Task<FileSystemComparison> createTask() {
        return new LoadTask();
    }


    class LoadTask extends Task<FileSystemComparison> {
        @Override
        protected FileSystemComparison call() throws Exception {
            return loadFscx();
        }

        /**
         * Loads the file system comparison
         * @return The FileSystemComparison object
         * @throws JDOMException
         * @throws IOException
         */
        private FileSystemComparison loadFscx() throws JDOMException, IOException {
            //Load the XML file
            Document document;
            Element fscomparisonElement;
            SAXBuilder sxb = new SAXBuilder();
            document = sxb.build(new File(fscxPath));
            fscomparisonElement = document.getRootElement();

            //Parse the document
            //******************* Metadata ***************************//
            updateMessage("Loading comparison metadata");
            Element metadataElement = fscomparisonElement.getChild("metadata");
            List<Integer> metadata = new ArrayList<>();
            String nameText = metadataElement.getChildText("name");
            String dateText = metadataElement.getChildText("date");
            String timeText = metadataElement.getChildText("time");
            String matchedText = metadataElement.getChildText("matchedCount");
            String modifiedText = metadataElement.getChildText("modifiedCount");
            String createdText = metadataElement.getChildText("createdCount");
            String deletedText = metadataElement.getChildText("deletedCount");
            String errorText = metadataElement.getChildText("errorCount");
            String datetimeString = dateText + "_" + timeText;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
            Date datetime = null;
            try {
                datetime = dateFormat.parse(datetimeString);
            } catch (ParseException e) {
                //TODO log
            }
            metadata.add(ComparisonStatus.MATCHED.getIndex(), Integer.parseInt(matchedText));
            metadata.add(ComparisonStatus.MODIFIED.getIndex(), Integer.parseInt(modifiedText));
            metadata.add(ComparisonStatus.CREATED.getIndex(), Integer.parseInt(createdText));
            metadata.add(ComparisonStatus.DELETED.getIndex(), Integer.parseInt(deletedText));
            metadata.add(ComparisonStatus.ERROR.getIndex(), Integer.parseInt(errorText));

            //******************* FSHMetadata **************************//
            updateMessage("Loading file system metadata");
            Element refFSElement = metadataElement.getChild("referenceFS");
            Element comFSElement = metadataElement.getChild("comparedFS");
            FileSystemHashMetadata refFS = FSXmlHandler.loadFileSystemHashMetadata(refFSElement);
            FileSystemHashMetadata comFS = FSXmlHandler.loadFileSystemHashMetadata(comFSElement);

            //******************* Comparison ***************************//
            updateMessage("Extracting comparison");
            Element comparisonElement = fscomparisonElement.getChild("comparison");
            List<Element> pList = comparisonElement.getChildren();
            TreeSet<PathComparison> comparisonSet = new TreeSet<>();
            PathComparison p;
            long workMax = pList.size();
            long workDone = 0;
            for(Element pElement : pList){
                updateProgress(workDone, workMax);
                p = FSXmlHandler.loadPathComparison(pElement);
                comparisonSet.add(p);
                workDone++;
            }

            return new FileSystemComparison(refFS,
                    comFS,
                    metadata,
                    datetime,
                    comparisonSet,
                    nameText);
        }
    }


}
