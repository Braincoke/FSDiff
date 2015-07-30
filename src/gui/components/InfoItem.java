package gui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

/**
 * Created by Erwan Dano on 28/07/2015.
 */
public class InfoItem extends HBox{

    /**
     * Label of the info to display
     */
    private Text labelNode;
    /**
     * Info to display
     */
    private Text infoNode;
    /**
     * Text to display inside the labelNode node
     */
    private StringProperty label;
    /**
     * Text to display inside the info node
     */
    private StringProperty text;

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
    }

    public Text getLabelNode() {
        return labelNode;
    }

    public void setLabelNode(Text labelNode) {
        this.labelNode = labelNode;
    }

    public Text getInfoNode() {
        return infoNode;
    }

    public void setInfoNode(Text text) {
        this.infoNode = text;
    }

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
