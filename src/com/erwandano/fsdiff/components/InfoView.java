package com.erwandano.fsdiff.components;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


/**
 * A component to easily view basic information in the following format :
 *
 * ----------------------------------------------------------------------
 * Label         Information about the label
 *               that can be a long text tha
 *               t will wrap.
 * ----------------------------------------------------------------------
 *
 * The couple (label, info) is held in an InfoItem object.
 * The InfoView will list all InfoItem object and resize all labels width to
 * the largest width for a better readability.
 *
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
        init();
    }

    public InfoView(double spacing) {
        super(spacing);
        init();
    }

    public InfoView(Node... children) {
        super(children);
        init();
    }

    public InfoView(double spacing, Node... children) {
        super(spacing, children);
        init();
    }

    private void init(){
        this.getStyleClass().add("info-view");
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
        //Now bind properties for auto resize
        int length = children.size();
        for (Node node : children) {
            InfoItem item = (InfoItem) node;
            Text info = item.getInfoNode();
            Text label = item.getLabelNode();
            label.setWrappingWidth(labelMaxWidth);
            info.wrappingWidthProperty().bind(readOnlyDoubleProperty.subtract(labelMaxWidth * 1.5));
        }

        if(getChildren().size() > 0 && !initDone) {
            getChildren().get(0).getStyleClass().add("first-item");
            getChildren().get(getChildren().size() - 1).getStyleClass().add("last-item");
            for(int i=0; i<length; i++){
                //children.get(i).getStyleClass().add("infoItem");
                if(i%2==0){
                    children.get(i).getStyleClass().add("even");
                } else {
                    children.get(i).getStyleClass().add("odd");
                }
            }
            initDone = true;
        }
    }
}
