package com.erwandano.fsdiff.diffwindow.toppane;

import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.core.PathDiff;
import com.erwandano.fsdiff.diffwindow.DiffTreeItem;
import com.erwandano.fsdiff.diffwindow.DiffWindowController;
import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.controlsfx.control.BreadCrumbBar;

/**
 * Breadcrumbs for the selected file or directory
 */
public class BreadcrumbsController extends Controller {

    private DiffWindowController windowController;
    private BreadCrumbBar<PathDiff> breadCrumbBar;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private HBox hbox;

    public void setWindowController(DiffWindowController windowController){
        this.windowController = windowController;
        breadCrumbBar = new BreadCrumbBar<>(windowController.getRootTreeItem());
        AnchorPane anchorPane = new AnchorPane(breadCrumbBar);
        /* Resize the anchorPane to the breadCrumbBar width */
        double buttonPadding = 20;
        double charWidth = 6;
        breadCrumbBar.selectedCrumbProperty().addListener((observable, oldValue, newValue) -> {
            int[] l = getBreadcrumbLength();
            anchorPane.setPrefWidth(l[0]*2*buttonPadding + l[1]*charWidth);
        });
        scrollPane.setContent(anchorPane);
        /* Allow mouse wheel scrolling to let the user see every breadCrumb */
        double smoothFactor = 0.1;
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                scrollPane.setHvalue(scrollPane.getHvalue() - event.getDeltaY()*smoothFactor);
                event.consume();
            }
        });
        //Display path name in breadcrumbs
        breadCrumbBar.setCrumbFactory(crumb ->
                new CustomBreadCrumbButton(crumb.getValue() != null ? crumb.getValue().getName():""));
        //Update windowController selectedPath on click
        breadCrumbBar.setOnCrumbAction(event -> windowController.setSelectedPath((DiffTreeItem) event.getSelectedCrumb()));
//        breadCrumbBar.setTranslateY(-1);
    }

    public void updateBreadcrumbs(DiffTreeItem node){
        breadCrumbBar.setSelectedCrumb(node);
    }

    /**
     * Get the number of buttons in the breadcrumb bar, and the total number of chars
     */
    public int[] getBreadcrumbLength(){
        TreeItem<PathDiff> selected = breadCrumbBar.getSelectedCrumb();
        int nbItems = 1;
        int nbChars = selected.getValue().getPath().getFileName().toString().length();
        while( (selected = selected.getParent()) != null){
            nbItems++;
            nbChars += selected.getValue().getPath().getFileName().toString().length();
        }
        int[] result = new int[2];
        result[0] = nbItems;
        result[1] = nbChars;
        return result;
    }



    private class BreadCrumbBarCustomSkin extends BreadCrumbBarSkin<PathDiff> {
        public BreadCrumbBarCustomSkin(BreadCrumbBar<PathDiff> control) {
            super(control);
        }
    }


    /**
     * Represents a BreadCrumb Button
     *
     * <pre>
     * ----------
     *  \         \
     *  /         /
     * ----------
     * </pre>
     *
     *
     */
    private class CustomBreadCrumbButton extends BreadCrumbBarSkin.BreadCrumbButton {

        private static final String STYLE_CLASS_FIRST = "first"; //$NON-NLS-1$
        private final ObjectProperty<Boolean> first = new SimpleObjectProperty<>(this, "first"); //$NON-NLS-1$
        private final double arrowWidth = 5;
        private final double arrowHeight = 20;

        /**
         * Create a BreadCrumbButton
         *
         * @param text Buttons text
         */
        public CustomBreadCrumbButton(String text){
            this(text, null);
        }

        /**
         * Create a BreadCrumbButton
         * @param text Buttons text
         * @param gfx Gfx of the Button
         */
        public CustomBreadCrumbButton(String text, Node gfx){
            super(text, gfx);
            first.set(false);

            getStyleClass().addListener((Observable arg0) -> {
                updateShape();
            });

            updateShape();
        }

        private void updateShape(){
            this.setShape(createButtonShape());
        }


        /**
         * Gets the crumb arrow with
         * @return
         */
        public double getArrowWidth(){
            return arrowWidth;
        }



        private javafx.scene.shape.Path createButtonShape1(){

            javafx.scene.shape.Path path = new javafx.scene.shape.Path();
            // begin in the upper right corner
            MoveTo e1 = new MoveTo(0, 0);
            // bind the width of the shape to the width of the button
            //e1.xProperty().bind(this.widthProperty().subtract(arrowWidth));
            path.getElements().add(e1);

            VLineTo vLineTo = new VLineTo(arrowHeight);
            path.getElements().add(vLineTo);

            /*LineTo e2 = new LineTo();
            // the x endpoint of this line depends on the x property of line e1
            e2.setX(arrowWidth + 60);
            e2.setY(arrowHeight / 2.0);
            path.getElements().add(e2);

            // draw lower part of right arrow
            LineTo e3 = new LineTo();
            // the x endpoint of this line depends on the x property of line e1
            e3.setX(arrowWidth+60);
            e3.setY(arrowHeight);
            path.getElements().add(e3);*/

            return path;
        }


        /**
         * Create an arrow path
         *
         * Based upon Uwe / Andy Till code snippet found here:
         * see http://ustesis.wordpress.com/2013/11/04/implementing-breadcrumbs-in-javafx/
         * @return
         */
        private javafx.scene.shape.Path createButtonShape(){
            // build the following shape (or home without left arrow)

            //   --------
            //  \         \
            //  /         /
            //   --------
            Path path = new Path();

            // begin in the upper left corner
            MoveTo e1 = new MoveTo(0, 0);
            path.getElements().add(e1);

            // draw a horizontal line that defines the width of the shape
            HLineTo e2 = new HLineTo();
            // bind the width of the shape to the width of the button
            e2.xProperty().bind(this.widthProperty().subtract(arrowWidth));
            path.getElements().add(e2);

            // draw upper part of right arrow
            LineTo e3 = new LineTo();
            // the x endpoint of this line depends on the x property of line e2
            e3.xProperty().bind(e2.xProperty().add(arrowWidth));
            e3.setY(arrowHeight / 2.0);
            path.getElements().add(e3);

            // draw lower part of right arrow
            LineTo e4 = new LineTo();
            // the x endpoint of this line depends on the x property of line e2
            e4.xProperty().bind(e2.xProperty());
            e4.setY(arrowHeight);
            path.getElements().add(e4);

            // draw lower horizontal line
            HLineTo e5 = new HLineTo(0);
            path.getElements().add(e5);

            if(! getStyleClass().contains(STYLE_CLASS_FIRST)){
                // draw lower part of left arrow
                // we simply can omit it for the first Button
                LineTo e6 = new LineTo(arrowWidth, arrowHeight / 2.0);
                path.getElements().add(e6);
            }else{
                // draw an arc for the first bread crumb
                LineTo lineTo = new LineTo();
                lineTo.setX(0);
                lineTo.setY(0);
                path.getElements().add(lineTo);
            }

            // close path
            ClosePath e7 = new ClosePath();
            path.getElements().add(e7);
            // this is a dummy color to fill the shape, it won't be visible
            path.setFill(Color.BLACK);

            return path;
        }
    }
}
