package ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

/**
 * An element of InfoView, simply displays a label and an info related
 * The text will wrap automatically thanks to the resize() function of InfoView
 */
public class InfoItem extends HBox{

    public InfoItem(){
        text = new SimpleStringProperty();
        label = new SimpleStringProperty();
        labelNode = new Text();
        infoNode = new Text();
        this.getChildren().addAll(labelNode, infoNode);
        setHgrow(this, Priority.ALWAYS);
        setHgrow(labelNode, Priority.NEVER);
        setHgrow(infoNode, Priority.ALWAYS);
        setSpacing(10);
        this.getStyleClass().add("infoItem");
    }

    public InfoItem(String label){
        this();
        setLabel(label);
    }

    /**
     * Label of the info to display
     */
    private Text labelNode;

    public Text getLabelNode() {
        return labelNode;
    }

    public void setLabelNode(Text labelNode) {
        this.labelNode = labelNode;
    }

    /**
     * Info to display
     */
    private Text infoNode;

    public Text getInfoNode() {
        return infoNode;
    }

    public void setInfoNode(Text text) {
        this.infoNode = text;
    }

    /**
     * Text to display inside the labelNode node
     */
    private StringProperty label;

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String label) {
        this.label.set(label);
        if(labelNode !=null)
            labelNode.setText(label);
    }

    public StringProperty labelProperty() {
        return label;
    }

    /**
     * Text to display inside the info node
     */
    private StringProperty text;

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
        if(infoNode !=null)
            infoNode.setText(text);
    }

    public StringProperty textProperty() {
        return text;
    }

}
