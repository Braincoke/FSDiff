package com.erwandano.fsdiff.diffwindow.leftmenu.explorertab;

import com.erwandano.fsdiff.Main;
import com.erwandano.fsdiff.core.DiffStatus;
import com.erwandano.fsdiff.core.PathDiff;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import static com.erwandano.fsdiff.core.DiffStatus.SIZE;
import static com.erwandano.fsdiff.core.DiffStatus.values;

/**
 * TreeCells for the explorer in the left sidebar.
 * Implements a file tree as well as information about the differential.
 */
public class PathDiffTreeCell extends TreeCell<PathDiff> {


    static {
        // Register a custom default font
        GlyphFontRegistry.register("FontAwesome", Main.class.getClassLoader().getResourceAsStream("com/erwandano/fsdiff/resources/fontawesome.ttf"), 16);
    }
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Node folderIcon ;
    private final Node openFolderIcon;
    private final Node matchedFileIcon ;
    private final Node modifiedFileIcon ;
    private final Node createdFileIcon ;
    private final Node deletedFileIcon;
    private final Node errorFileIcon;


    /**
     * A filter display to choose which pills to display according to their DiffStatus
     */
    private boolean[] display;

    /**
     * Choose to display or not a pill with the given DiffStatus
     * @param status    The DiffStatus
     * @param display   true to display the pill and false to hide it
     */
    public void setDisplay(DiffStatus status, boolean display){
        this.display[status.getIndex()] = display;
    }


    public PathDiffTreeCell(){
        display = new boolean[SIZE];
        for(int i=1; i<SIZE; i++){
            display[i] = true;
        }
        /* Create icons */
        folderIcon = fontAwesome.create(FontAwesome.Glyph.FOLDER.getChar());
        openFolderIcon = fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN.getChar());
        matchedFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar());
        modifiedFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar());
        createdFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar());
        deletedFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar());
        errorFileIcon = fontAwesome.create(FontAwesome.Glyph.TIMES.getChar());

        /* Add fa-icon class */
        folderIcon.getStyleClass().add("fa-icon");
        openFolderIcon.getStyleClass().add("fa-icon");
        matchedFileIcon.getStyleClass().add("fa-icon");
        modifiedFileIcon.getStyleClass().add("fa-icon");
        createdFileIcon.getStyleClass().add("fa-icon");
        deletedFileIcon.getStyleClass().add("fa-icon");
        errorFileIcon.getStyleClass().add("fa-icon");

        /* Add file class */
        matchedFileIcon.getStyleClass().add("file");
        modifiedFileIcon.getStyleClass().add("file");
        createdFileIcon.getStyleClass().add("file");
        deletedFileIcon.getStyleClass().add("file");
        errorFileIcon.getStyleClass().add("file");

        /* Add custom class */
        folderIcon.getStyleClass().add("folder");
        openFolderIcon.getStyleClass().add("open-folder");
        matchedFileIcon.getStyleClass().add("matched");
        modifiedFileIcon.getStyleClass().add("modified");
        createdFileIcon.getStyleClass().add("created");
        deletedFileIcon.getStyleClass().add("deleted");
        errorFileIcon.getStyleClass().add("error");
    }

    /**
     *  Create the cell
     *
     *  --------------------------------------------------------------------------------------------------------------
     *  |  --------------------   ------------                        ----------------------------------             |
     *  | |                    |  |           |                       |                                |             |
     *  | | Folder / File icon |  | Path name |                       |       STATUS PILLS             |             |
     *  | |____________________|  |___________|                       |________________________________|             |
     *  |____________________________________________________________________________________________________________|
     *
     *
     */
    @Override
    protected void updateItem(PathDiff item, boolean empty) {
        super.updateItem(item, empty);

        if(empty) {
            setText(null);
            setGraphic(null);
        } else {
            HBox itemGraphics = new HBox();
            HBox iconHbox = new HBox();
            HBox textHbox = new HBox();
            Label nameLabel = new Label();
            HBox infoHbox = new HBox();
            //Configure Nodes
            HBox.setHgrow(itemGraphics, Priority.ALWAYS);
            HBox.setHgrow(textHbox, Priority.ALWAYS);
            itemGraphics.setSpacing(10);
            iconHbox.setSpacing(10);
            textHbox.setSpacing(10);
            infoHbox.setSpacing(10);
            itemGraphics.setAlignment(Pos.CENTER_LEFT);
            iconHbox.setAlignment(Pos.CENTER_LEFT);
            textHbox.setAlignment(Pos.CENTER_LEFT);
            infoHbox.setAlignment(Pos.CENTER_RIGHT);

            //Add icon
            if(item.isDirectory()) {
                iconHbox.getChildren().add(folderIcon);
            } else {
                Node fileIcon;
                switch (item.getStatus()){
                    case MATCHED:
                        fileIcon = matchedFileIcon;
                        break;
                    case MODIFIED:
                        fileIcon = modifiedFileIcon;
                        break;
                    case CREATED:
                        fileIcon = createdFileIcon;
                        break;
                    case DELETED:
                        fileIcon = deletedFileIcon;
                        break;
                    case ERROR:
                        fileIcon = errorFileIcon;
                        break;
                    default:
                        fileIcon = matchedFileIcon;
                        break;
                }
                iconHbox.getChildren().add(fileIcon);
            }

            //Set name
            nameLabel.setText(item.getName());
            textHbox.getChildren().add(nameLabel);

            //Add pills
            for(DiffStatus diffStatus : values()){
                if(display[diffStatus.getIndex()]){
                    new Pill(infoHbox, item, diffStatus);
                }
            }

            //Add all HBoxes to itemGraphics
            itemGraphics.getChildren().addAll(iconHbox, textHbox, infoHbox);
            setGraphic(itemGraphics);
        }
    }

}
