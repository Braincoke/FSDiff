package gui.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * A progress bar with a text field to indicate the progress thanks to a string
 */
public class TextProgressBar extends StackPane {

    public TextProgressBar(){
        text = new SimpleStringProperty();
        progress = new SimpleDoubleProperty();
        progressBar = new ProgressBar();
        label = new Label();
        hBox = new HBox();
        hBox.getChildren().add(label);
        hBox.setAlignment(Pos.CENTER);
        this.getChildren().addAll(progressBar, label);
        prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            hBox.setPrefWidth(newValue.doubleValue());
            progressBar.setPrefWidth(newValue.doubleValue());
        });
        maxWidthProperty().addListener((observable, oldValue, newValue) -> {
            hBox.setMaxWidth(newValue.doubleValue());
            progressBar.setMaxWidth(newValue.doubleValue());
        });
    }

    public TextProgressBar(double progress, String labelText){
        this();
        setProgress(progress);
        setText(labelText);
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * ATTRIBUTES                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The progress bar
     */
    private ProgressBar progressBar;

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }


    /**
     * The node holding the value
     */
    private Label label;

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }


    /**
     * A HBox that helps centering the value
     */
    private HBox hBox;

    public HBox gethBox() {
        return hBox;
    }

    /**
     * Property holding the progress
     */
    private DoubleProperty progress;

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double value){
        this.progress.setValue(value);
        this.progressBar.setProgress(value);
    }

    /**
     * Property holding the text
     */
    private StringProperty text;

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text){
        this.text.setValue(text);
        this.label.setText(text);
    }

    public void setPercentage(int percentage) {
        if(percentage>100){
            setText("100%");
            setProgress(-1d);
        } else {
            setText(String.valueOf(percentage) + "%");
            setProgress((double) percentage / 100d);
        }
    }

    public void setPercentage(double percentage) {
        if(percentage>100d){
            setText("100%");
            setProgress(-1d);
        } else {
            setText(String.valueOf(percentage) + "%");
            setProgress(percentage);
        }
    }
}
