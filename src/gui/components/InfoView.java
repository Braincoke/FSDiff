package gui.components;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


/**
 * A component to easily view basic information in the following format :
 * <p>
 * ----------------------------------------------------------------------
 * Label         Information about the label
 * that can be a long text tha
 * t will wrap.
 * ----------------------------------------------------------------------
 * <p>
 * The couple (label, info) is held in an InfoItem object.
 * The InfoView will list all InfoItem object and resize all labels width to
 * the largest width for a better readability.
 * <p>
 * The information can be a long text and will wrap accordingly to the observable value
 * passed as a parameter of the resize() function
 * TODO Make sure that children are all InfoItem objects
 */
public class InfoView extends VBox {

    /**
     * Indicates if the initialization has already been done
     */
    private boolean initDone = false;

    public InfoView() {
        initPadding();
    }

    public InfoView(double spacing) {
        super(spacing);
        initPadding();
    }

    public InfoView(Node... children) {
        super(children);
        initPadding();
    }

    public InfoView(double spacing, Node... children) {
        super(spacing, children);
        initPadding();
    }

    private void initPadding() {
        setPadding(new Insets(10, 0, 10, 0));
    }

    /**
     * Resize all InfoItem children to the new width of the parent container of the InfoView object
     *
     * @param readOnlyDoubleProperty The width of the parent container of the InfoView object
     */
    public void resize(ReadOnlyDoubleProperty readOnlyDoubleProperty){
        ObservableList<Node> children = getChildren();
        //Find the label max width
        double labelMaxWidth = 150;
        double padding = 20;
        //Now bind properties for auto resize
        for(Node node : children){
            InfoItem item = (InfoItem) node;
            Text info =  item.getInfoNode();
            Text label = item.getLabelNode();
            label.setWrappingWidth(labelMaxWidth);
            info.wrappingWidthProperty().bind(readOnlyDoubleProperty.subtract(labelMaxWidth * 1.5));
        }

        if(getChildren().size() > 0 && !initDone) {
            getChildren().get(0).getStyleClass().add("firstItem");
            getChildren().get(getChildren().size() - 1).getStyleClass().add("lastItem");
            initDone = true;
        }
    }
}
