package com.erwandano.fsdiff.loaders;

import com.erwandano.fsdiff.core.*;
import com.erwandano.fsdiff.diffwindow.leftmenu.DiffStatus;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Save objects to XML and load from saved XML
 */
public class XMLHandler {


    /*******************************************************************************************************************
     *                                                                                                                 *
     *  SAVE FUNCTIONS                                                                                                 *
     *                                                                                                                 *
     *******************************************************************************************************************/

    public static void saveToXML(Element element, Path savePath){
        org.jdom2.Document document = new Document(element);
        try {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            xmlOutputter.output(document, new FileOutputStream(savePath.toString()));
        } catch (java.io.IOException e) {
            //TODO log
        }
    }

    public static void saveToXML(FileSystemHash fsh, Path savePath) {
        Element fshash = XMLHandler.toXMLElement(fsh);
        org.jdom2.Document document = new Document(fshash);
        Path formattedSavePath = savePath;
        if (!(savePath.toString().endsWith(".fshx"))) {
            formattedSavePath = Paths.get(savePath.toString() + ".fshx");
        }
        try {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            xmlOutputter.output(document, new FileOutputStream(formattedSavePath.toString()));
        } catch (java.io.IOException e) {
            //TODO log
        }
    }

    public static void saveToXML(FileSystemDiff fsc, Path savePath) {
        Element comparison = XMLHandler.toXMLElement(fsc);
        org.jdom2.Document document = new Document(comparison);
        Path formattedSavePath = savePath;
        if (!(savePath.toString().endsWith(".fscx"))) {
            formattedSavePath = Paths.get(savePath.toString() + ".fscx");
        }
        try {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            xmlOutputter.output(document, new FileOutputStream(formattedSavePath.toString()));
        } catch (java.io.IOException e) {
            //TODO log
        }
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  FILE SYSTEM HASH METADATA                                                                                      *
     *                                                                                                                 *
     *******************************************************************************************************************/
    public static FileSystemHashMetadata loadFileSystemHashMetadata(Element fsElement){
        return new FileSystemHashMetadata(loadFSHMetadata(fsElement));
    }

    public static HashMap<String, String> loadFSHMetadata(Element fsElement) {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("name", fsElement.getChildText("name"));
        metadata.put("date", fsElement.getChildText("date"));
        metadata.put("time", fsElement.getChildText("time"));
        metadata.put("duration", fsElement.getChildText("duration"));
        metadata.put("fileSystem", fsElement.getChildText("fileSystem"));
        metadata.put("OS", fsElement.getChildText("OS"));
        metadata.put("rootPath", fsElement.getChildText("rootPath"));
        metadata.put("inputType", fsElement.getChildText("inputType"));
        metadata.put("fileCount", fsElement.getChildText("fileCount"));
        metadata.put("byteCount", fsElement.getChildText("byteCount"));
        metadata.put("errorCount", fsElement.getChildText("errorCount"));
        return metadata;
    }

    public static Element toXMLElement(FileSystemHashMetadata fsh, String elementName){
        Element fsElement = new Element(elementName);
        Element nameElement = new Element("name");
        nameElement.setText(fsh.getName());
        fsElement.addContent(nameElement);
        //Date and time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String dateText = dateFormat.format(fsh.getDatetime());
        String timeText = timeFormat.format(fsh.getDatetime());
        Element dateElement = new Element("date");
        Element timeElement = new Element("time");
        Element durationElement = new Element("duration");
        dateElement.setText(dateText);
        timeElement.setText(timeText);
        durationElement.setText(String.valueOf(fsh.getDuration()));
        fsElement.addContent(dateElement)
                .addContent(timeElement)
                .addContent(durationElement);
        //Computer config
        Element filesystemElement = new Element("fileSystem");
        Element osElement = new Element("OS");
        Element rootDirectoryElement = new Element("rootPath");
        Element inputTypeElement = new Element("inputType");
        Element fileCountElement = new Element("fileCount");
        Element byteCountElement = new Element("byteCount");
        Element errorCountElement = new Element("errorCount");
        filesystemElement.setText(fsh.getFileSystem()); //TODO verify on Linux that this outputs NTFS
        osElement.setText(fsh.getOS());
        rootDirectoryElement.setText(fsh.getFileSystemInput().getPath().toString());
        inputTypeElement.setText(fsh.getFileSystemInput().getInputType().name());
        fileCountElement.setText(String.valueOf(fsh.getFileCount()));
        byteCountElement.setText(String.valueOf(fsh.getByteCount()));
        errorCountElement.setText(String.valueOf(fsh.getErrorCount()));
        fsElement
                .addContent(filesystemElement)
                .addContent(osElement)
                .addContent(inputTypeElement)
                .addContent(rootDirectoryElement)
                .addContent(fileCountElement)
                .addContent(byteCountElement)
                .addContent(errorCountElement);
        return fsElement;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  FILE SYSTEM HASH                                                                                               *
     *                                                                                                                 *
     *******************************************************************************************************************/

    /**
     * Loads a FileSystemHash object from a given XML file
     */
    public static FileSystemHash loadFileSystemHash (String xmlFile) throws JDOMException, IOException {
        //Load the XML file
        org.jdom2.Document document;
        Element fshashElement;
        SAXBuilder sxb = new SAXBuilder();
        document = sxb.build(new File(xmlFile));
        fshashElement = document.getRootElement();

        //Parse the document
        //Starting with the metadata
        Element metadataElement = fshashElement.getChild("metadata");
        HashMap<String, String> metadata = loadFSHMetadata(metadataElement);

        //Now the hashes
        TreeMap<Path, HashedFile> loadedFileHashes = new TreeMap<>();
        Element hashesElement = fshashElement.getChild("hashes");
        List fileElementList = hashesElement.getChildren("file");
        for (Object aFileElementList : fileElementList) {
            Element fileElement = (Element) aFileElementList;
            String filePathText = fileElement.getChildText("filePath");
            Path filePath = Paths.get(filePathText);
            HashedFile hashedFile = new HashedFile(filePath);
            hashedFile.setMd5(fileElement.getChildText("md5"));
            loadedFileHashes.put(filePath, hashedFile);
        }

        //Create FileSystemHash from the data
        return new FileSystemHash(metadata, loadedFileHashes);
    }

    /**
     * Transform this file system hash into an xml element <fshash></fshash>
     * @return
     */
    public static Element toXMLElement(FileSystemHash fsh) {
        Element fshash = new Element("FSHash");

        //*********************Metadata*******************************//
        Element metadataElement = toXMLElement(fsh, "metadata");
        fshash.addContent(metadataElement);

        //***********************File hashes**********************************//
        Element hashesElement = new Element("hashes");
        fshash.addContent(hashesElement);
        for (Map.Entry entry : fsh.getFileHashes().entrySet()) {
            String path = entry.getKey().toString();
            HashedFile hashedFile = (HashedFile) entry.getValue();
            Element fileElement = new Element("file");
            Element filepathElement = new Element("filePath");
            Element md5Element = new Element("md5");
            filepathElement.setText(path);
            md5Element.setText(hashedFile.getMd5());
            hashesElement.addContent(fileElement);
            fileElement.addContent(filepathElement)
                    .addContent(md5Element);
        }
        return fshash;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     *  FILE SYSTEM COMPARISON                                                                                         *
     *                                                                                                                 *
     *******************************************************************************************************************/

    public static Element toXMLElement(FileSystemDiff fsc) {


        Element fscomparison = new Element("FSComparison");

        //******************* Metadata ******************************//
        Element metadataElement = new Element("metadata");
        fscomparison.addContent(metadataElement);
        Element nameElement = new Element("name");
        nameElement.setText(fsc.getName());
        metadataElement.addContent(nameElement);
        //Date and time
        Date datetime = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String dateText = dateFormat.format(datetime);
        String timeText = timeFormat.format(datetime);
        Element dateElement = new Element("date");
        Element timeElement = new Element("time");
        dateElement.setText(dateText);
        timeElement.setText(timeText);
        metadataElement.addContent(dateElement)
                .addContent(timeElement);
        //Reference file system
        Element referenceFSElement = toXMLElement(fsc.getReferenceFS(), "referenceFS");
        metadataElement.addContent(referenceFSElement);
        //Compared file system
        Element comparedFSElement = toXMLElement(fsc.getComparedFS(), "comparedFS");
        metadataElement.addContent(comparedFSElement);
        //Counts
        Element matchedCountElement = new Element("matchedCount");
        Element modifieddCountElement = new Element("modifiedCount");
        Element createdCountElement = new Element("createdCount");
        Element deletedCountElement = new Element("deletedCount");
        Element errorCountElement = new Element("errorCount");
        matchedCountElement.setText(String.valueOf(fsc.getMatchedCount()));
        modifieddCountElement.setText(String.valueOf(fsc.getModifiedCount()));
        createdCountElement.setText(String.valueOf(fsc.getCreatedCount()));
        deletedCountElement.setText(String.valueOf(fsc.getDeletedCount()));
        errorCountElement.setText(String.valueOf(fsc.getErrorCount()));
        metadataElement.addContent(matchedCountElement)
                .addContent(modifieddCountElement)
                .addContent(createdCountElement)
                .addContent(deletedCountElement)
                .addContent(errorCountElement);

        //***************** Comparison **************************************//
        Element comparisonElement = new Element("comparison");
        fscomparison.addContent(comparisonElement);
        for(PathDiff p : fsc.getDiff()){
            Element pElement = toXMLElement(p);
            comparisonElement.addContent(pElement);
        }
        return fscomparison;
    }



    /**
     * Load a FileSystemDiff object from a given XML file
     */
    public static FileSystemDiff loadFileSystemComparison(String xmlFile) throws JDOMException, IOException {
        //Load the XML file
        Document document;
        Element fscomparisonElement;
        SAXBuilder sxb = new SAXBuilder();
        document = sxb.build(new File(xmlFile));
        fscomparisonElement = document.getRootElement();

        //Parse the document
        //******************* Metadata ***************************//
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
        metadata.add(DiffStatus.MATCHED.getIndex(), Integer.parseInt(matchedText));
        metadata.add(DiffStatus.MODIFIED.getIndex(), Integer.parseInt(modifiedText));
        metadata.add(DiffStatus.CREATED.getIndex(), Integer.parseInt(createdText));
        metadata.add(DiffStatus.DELETED.getIndex(), Integer.parseInt(deletedText));
        metadata.add(DiffStatus.ERROR.getIndex(), Integer.parseInt(errorText));

        //******************* FSHMetadata **************************//
        Element refFSElement = metadataElement.getChild("referenceFS");
        Element comFSElement = metadataElement.getChild("comparedFS");
        FileSystemHashMetadata refFS = loadFileSystemHashMetadata(refFSElement);
        FileSystemHashMetadata comFS = loadFileSystemHashMetadata(comFSElement);

        //******************* Comparison ***************************//
        Element comparisonElement = fscomparisonElement.getChild("comparison");
        List<Element> pList = comparisonElement.getChildren();
        TreeSet<PathDiff> comparisonSet = new TreeSet<>();
        for(Element pElement : pList){
            PathDiff p = loadPathComparison(pElement);
            comparisonSet.add(p);
        }
        //Update parent linking
        for(PathDiff p : comparisonSet){
            PathDiff parent = p.getParent();
            for(PathDiff potentialParent : comparisonSet){
                if(parent.comparePath(potentialParent) == 0){
                    p.setParent(potentialParent);
                }
            }
        }

        return new FileSystemDiff(refFS,
                comFS,
                metadata,
                datetime,
                comparisonSet,
                nameText);
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     *  PATH COMPARISON                                                                                                *
     *                                                                                                                 *
     *******************************************************************************************************************/


    public static Element toXMLElement(PathDiff p){
        Element pathComparisonElement = new Element("pathComparison");
        Element pathElement = new Element("path");
        Element parentElement = new Element("parent");
        String parent = p.getParentPath()==null? "null" : p.getParentPath().toString();
        pathElement.setText(p.getPath().toString());
        parentElement.setText(parent);
        pathComparisonElement.addContent(pathElement)
                .addContent(parentElement);
        if(p.isDirectory()){
            pathComparisonElement.setAttribute("type", "directory");
            Element directoryStatusElement = new Element("directoryStatus");
            Element matchedElement = new Element("matched");
            Element modifiedElement = new Element("modified");
            Element createdElement = new Element("created");
            Element deletedElement = new Element("deleted");
            Element errorElement = new Element("error");
            matchedElement.setText(String.valueOf(p.getDirectoryStatus(DiffStatus.MATCHED)));
            modifiedElement.setText(String.valueOf(p.getDirectoryStatus(DiffStatus.MODIFIED)));
            createdElement.setText(String.valueOf(p.getDirectoryStatus(DiffStatus.CREATED)));
            deletedElement.setText(String.valueOf(p.getDirectoryStatus(DiffStatus.DELETED)));
            errorElement.setText(String.valueOf(p.getDirectoryStatus(DiffStatus.ERROR)));
            directoryStatusElement.addContent(matchedElement)
                    .addContent(modifiedElement)
                    .addContent(createdElement)
                    .addContent(deletedElement)
                    .addContent(errorElement);
            pathComparisonElement.addContent(directoryStatusElement);
        } else {
            pathComparisonElement.setAttribute("type", "file");
            Element statusElement = new Element("status");
            statusElement.setText(p.getStatus().name());
            pathComparisonElement.addContent(statusElement);
        }

        return pathComparisonElement;
    }

    /**
     *
     * @param pElement
     * @return
     */
    public static PathDiff loadPathComparison(Element pElement){
        String pathText = pElement.getChildText("path");
        PathDiff p = new PathDiff(Paths.get(pathText));
        String parentText = pElement.getChildText("parent");
        p.setParent(new PathDiff(Paths.get(parentText)));
        String directoryAttribute = pElement.getAttributeValue("type");
        if(directoryAttribute.compareTo("directory")==0){
            p.setIsDirectory(true);
            Element directoryStatusElement = pElement.getChild("directoryStatus");
            int matched = Integer.parseInt(directoryStatusElement.getChildText("matched"));
            int modified = Integer.parseInt(directoryStatusElement.getChildText("modified"));
            int created = Integer.parseInt(directoryStatusElement.getChildText("created"));
            int deleted = Integer.parseInt(directoryStatusElement.getChildText("deleted"));
            int error = Integer.parseInt(directoryStatusElement.getChildText("error"));
            int[] directoryStatus = new int[DiffStatus.SIZE];
            directoryStatus[DiffStatus.MATCHED.getIndex()] = matched;
            directoryStatus[DiffStatus.MODIFIED.getIndex()] = modified;
            directoryStatus[DiffStatus.CREATED.getIndex()] = created;
            directoryStatus[DiffStatus.DELETED.getIndex()] = deleted;
            directoryStatus[DiffStatus.ERROR.getIndex()] = error;
            p.setDirectoryStatus(directoryStatus);
        } else if (directoryAttribute.compareTo("file")==0){
            p.setIsDirectory(false);
            String statusText = pElement.getChildText("status");
            for(DiffStatus s : DiffStatus.values()){
                if(s.name().compareToIgnoreCase(statusText)==0){
                    p.setStatus(s);
                }
            }
        }
        return p;
    }



}
