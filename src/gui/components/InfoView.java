package gui.components;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class InfoView extends VBox {

    public InfoView() {
    }

    public InfoView(double spacing) {
        super(spacing);
    }

    public InfoView(Node... children) {
        super(children);
    }

    public InfoView(double spacing, Node... children) {
        super(spacing, children);
    }

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
            info.wrappingWidthProperty().bind(readOnlyDoubleProperty.subtract(labelMaxWidth*1.5));
        }
    }
}
