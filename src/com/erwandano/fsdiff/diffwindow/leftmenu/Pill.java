package com.erwandano.fsdiff.diffwindow.leftmenu;

import com.erwandano.fsdiff.core.PathDiff;
import com.erwandano.fsdiff.diffwindow.CssColor;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Info pill to display status counts.
 * Displayed as a circle for folders, will expand into a pill showing the status count when hovered.
 */
public class Pill extends StackPane {

    //Circle to pill animation
    private final Duration duration = Duration.millis(150);
    //Dimensions
    private final int circleWidth = 15;
    private final int circleHeight = 15;
    private final int circleArcWidth = 50;
    private final int circleArcHeight = 50;
    private final int pillArcWidth = 17;
    private final int pillArcHeight = 17;
    private final int squareSide = 17;
    private final int squareArc = 10;
    private final int pillWidthUnit = 8;
    private final double translateXUnit = 3.59;
    private Rectangle statusRectangle;
    private Label statusLabel;
    private Tooltip tooltip;
    private double translateX = 5;
    private int pillWidth = 40;

    /**
     * Create the pill and add it to the parent pane
     * @param parent        The parent container for the pill
     * @param pathDiff      The path differential
     * @param diffStatus    The status of the differential
     */
    public Pill(Pane parent, PathDiff pathDiff, DiffStatus diffStatus){
        statusRectangle = generateRectangle(diffStatus);
        statusLabel = generatePillLabel(diffStatus);
        int statusCount = pathDiff.getCount(diffStatus);
        int charNumber = getCharNumber(statusCount);
        pillWidth = charNumber*pillWidthUnit + 2*pillWidthUnit;
        translateX = (charNumber-1)*translateXUnit;
        if(statusCount > 0) {
            if(pathDiff.isDirectory()) {
                statusLabel.setText(String.valueOf(statusCount));
                this.setOnMouseEntered(new CircleToPillEvent());
                this.setOnMouseExited(new PillToCircleEvent());
                statusRectangle.setTranslateX(translateX);
            } else {
                statusRectangle.setWidth(squareSide);
                statusRectangle.setHeight(squareSide);
                statusRectangle.setArcHeight(squareArc);
                statusRectangle.setArcWidth(squareArc);
            }
            //Do not add info rectangle for matched file
            if(pathDiff.isDirectory()
                    || (!pathDiff.isDirectory() && diffStatus != DiffStatus.MATCHED)){
                this.getChildren().addAll(statusRectangle, statusLabel);
            }
            parent.getChildren().add(this);
            this.setVisible(true);
        } else {
            parent.getChildren().remove(this);
            this.setVisible(false);
        }
    }

    /**
     * Return the number of characters that will be used to display the status count
     * @param statusCount   The status count that will be displayed
     * @return  The number of character used to display statusCount
     */
    private int getCharNumber(int statusCount) {
        int charNumber = 1;
        int divisor = 1;
        int division;
        boolean finished = false;
        while(!finished){
            division =  statusCount / divisor;
            if(division<10 && division>=0){
                finished = true;
            } else {
                charNumber++;
                divisor *= 10;
            }
        }
        return charNumber;
    }

    /**
     * Create a compressed pill with the given color
     * @param color The color of the pill
     * @return  The compressed pill
     */
    private Rectangle generateRectangle(CssColor color) {
        Rectangle result = new Rectangle(circleWidth, circleHeight, Paint.valueOf(color.getBackgroundHexColor()));
        result.setArcHeight(circleArcHeight);
        result.setArcWidth(circleArcWidth);
        result.setOpacity(color.getOpacity());
        return result;
    }

    /**
     * Create a compressed pill and initialise the color according to the given status
     * @param status    The differential status
     * @return          The compressed pill
     */
    private Rectangle generateRectangle(DiffStatus status){
        Rectangle rectangle = new Rectangle(circleWidth, circleHeight);
        rectangle.setArcHeight(circleArcHeight);
        rectangle.setArcWidth(circleArcWidth);
        switch (status){
            case MATCHED:
                rectangle.setFill(Paint.valueOf(CssColor.MATCHED.getBackgroundHexColor()));
                rectangle.setOpacity(CssColor.MATCHED.getOpacity());
                break;
            case CREATED:
                rectangle.setFill(Paint.valueOf(CssColor.CREATED.getBackgroundHexColor()));
                rectangle.setOpacity(CssColor.CREATED.getOpacity());
                break;
            case DELETED:
                rectangle.setFill(Paint.valueOf(CssColor.DELETED.getBackgroundHexColor()));
                rectangle.setOpacity(CssColor.DELETED.getOpacity());
                break;
            case ERROR:
                rectangle.setFill(Paint.valueOf("white"));
                rectangle.strokeProperty().setValue(Paint.valueOf(CssColor.ERROR.getBackgroundHexColor()));
                rectangle.setOpacity(CssColor.ERROR.getOpacity());
                break;
            case MODIFIED:
                rectangle.setFill(Paint.valueOf(CssColor.MODIFIED.getBackgroundHexColor()));
                rectangle.setOpacity(CssColor.MODIFIED.getOpacity());
                break;
            default:
                rectangle.setFill(Paint.valueOf(CssColor.MATCHED.getBackgroundHexColor()));
                rectangle.setOpacity(CssColor.MATCHED.getOpacity());
                break;
        }
        return rectangle;
    }

    /**
     * Generate the label that will hold the status count
     * @param color The color of the text
     * @return  The label
     */
    private Label generatePillLabel(CssColor color) {
        Label result = new Label();
        result.setTextFill(Paint.valueOf(color.getBackgroundHexColor()));
        result.setOpacity(color.getOpacity());
        return result;
    }

    /**
     * Generate a label holding a status count given a DiffStatus
     * @param status    The DiffStatus
     * @return  The generated label
     */
    private Label generatePillLabel(DiffStatus status){
        switch(status){
            case MATCHED:
            case CREATED:
            case DELETED:
                return generatePillLabel(CssColor.LIGHT_PILL_LABEL);
            case ERROR:
                return generatePillLabel(CssColor.ERROR);
            case MODIFIED:
                return generatePillLabel(CssColor.DARK_PILL_LABEL);
            default:
                return generatePillLabel(CssColor.LIGHT_PILL_LABEL);
        }
    }

    /**
     *  Transform the pill from a colored circle to a pill containing a Label
     *     ____                         _________________
     *   /      \                     /                  \
     *  |        |       --------->  |      1 6 8         |
     *   \ _____/                     \ _________________/
     */
    class CircleToPillEvent implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            StackPane sourceStackPane = (StackPane) event.getSource();
            Rectangle sourceRectangle = (Rectangle) sourceStackPane.getChildren().get(0);
            Label sourceLabel = (Label) sourceStackPane.getChildren().get(1);
            Timeline timeline = new Timeline();
            timeline.setCycleCount(1);
            timeline.setAutoReverse(false);
            sourceRectangle.widthProperty();

            //create a keyValue with factory
            KeyValue keyValueX = new KeyValue(sourceRectangle.translateXProperty(), 1);
            KeyValue keyValueWidth = new KeyValue(sourceRectangle.widthProperty(), pillWidth);
            KeyValue keyValueArcHeight = new KeyValue(sourceRectangle.arcHeightProperty(), pillArcHeight);
            KeyValue keyValueArcWidth = new KeyValue(sourceRectangle.arcWidthProperty(), pillArcWidth);
            KeyValue keyValueLabelOpacity= new KeyValue(sourceLabel.opacityProperty(), 1f);

            //create a keyFrame, the keyValue is reached at time 150ms
            KeyFrame keyFrame = new KeyFrame(duration,
                    keyValueX,
                    keyValueWidth,
                    keyValueArcHeight,
                    keyValueArcWidth,
                    keyValueLabelOpacity);

            //add the keyframe to the timeline
            timeline.getKeyFrames().add(keyFrame);

            timeline.play();


        }
    }

    /**
     *  Transform the pill from a colored circle to a pill containing a Label
     *    _________________                  ____
     *  /                  \               /      \
     * |      1 6 8         | --------->  |        |
     *  \ _________________/               \ _____/
     */
    class PillToCircleEvent implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            StackPane sourceStackPane = (StackPane) event.getSource();
            Rectangle sourceRectangle = (Rectangle) sourceStackPane.getChildren().get(0);
            Label sourceLabel = (Label) sourceStackPane.getChildren().get(1);

            Timeline timeline = new Timeline();
            timeline.setCycleCount(1);
            timeline.setAutoReverse(false);
            sourceRectangle.widthProperty();

            //create a keyValue with factory: scaling the circle 2times
            KeyValue keyValueXrectangle = new KeyValue(sourceRectangle.translateXProperty(), translateX);
            KeyValue keyValueWidth = new KeyValue(sourceRectangle.widthProperty(), circleWidth);
            KeyValue keyValueArcHeight = new KeyValue(sourceRectangle.arcHeightProperty(), circleArcHeight);
            KeyValue keyValueArcWidth = new KeyValue(sourceRectangle.arcWidthProperty(), circleArcWidth);
            KeyValue keyValueLabelOpacity= new KeyValue(sourceLabel.opacityProperty(), 0f);

            //create a keyFrame, the keyValue is reached at time 150ms
            KeyFrame keyFrame = new KeyFrame(duration,
                    keyValueXrectangle,
                    keyValueWidth,
                    keyValueArcHeight,
                    keyValueArcWidth,
                    keyValueLabelOpacity);

            //add the keyframe to the timeline
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
        }
    }
}
