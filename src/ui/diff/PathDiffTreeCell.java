package ui.diff;

import core.DiffStatus;
import core.PathDiff;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import ui.Main;

/**
 * TreeCells for the explorer in the left sidebar.
 * Implements a file tree as well as information about the differential.
 */
public class PathDiffTreeCell extends TreeCell<PathDiff> {


    static {
        // Register a custom default font
        GlyphFontRegistry.register("FontAwesome", Main.class.getResourceAsStream("fontawesome.ttf"), 16);
    }
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Node folderIcon = fontAwesome.create(FontAwesome.Glyph.FOLDER.getChar());
    private final Node openFolderIcon = fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN.getChar());
    private final Node matchedFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar())
            .color(Color.valueOf(CssColor.MATCHED.getBackgroundHexColor()));
    private final Node modifiedFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar())
            .color(Color.valueOf(CssColor.MODIFIED.getBackgroundHexColor()));
    private final Node createdFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar())
            .color(Color.valueOf(CssColor.CREATED.getBackgroundHexColor()));
    private final Node deletedFileIcon = fontAwesome.create(FontAwesome.Glyph.FILE.getChar())
            .color(Color.valueOf(CssColor.DELETED.getBackgroundHexColor()));
    private final Node errorFileIcon = fontAwesome.create(FontAwesome.Glyph.TIMES.getChar())
            .color(Color.valueOf(CssColor.ERROR.getBackgroundHexColor()));


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
        display = new boolean[DiffStatus.SIZE];
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
            for(DiffStatus diffStatus : DiffStatus.values()){
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
