package gui.hexviewer;

import gui.components.buttons.IconButton;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import utils.HexDiff;

import java.io.File;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDiffBrowser extends AnchorPane {

    public static final int DEFAULT_LINE_NUMBER_PER_PAGE = 15;
    private static final Double ZOOM_FACTOR = 0.10;

    public HexDiffBrowser(){
        super();
        linesPerPage = DEFAULT_LINE_NUMBER_PER_PAGE;
        this.toolBar = initToolBar();
        this.referenceView = new HexDiffWebView();
        this.comparedView = new HexDiffWebView();
        this.splitPane = new SplitPane(referenceView, comparedView);
        VBox vBox = new VBox(toolBar, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(vBox);
        setRightAnchor(vBox, 0d);
        setBottomAnchor(vBox, 0d);
        setLeftAnchor(vBox, 0d);
        setTopAnchor(vBox, 0d);

        //CTRL + SCROLL WHEEL TO ZOOM IN OR OUT
        EventHandler<ScrollEvent> scrollZoom = new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (event.isControlDown()) {
                    if (event.getDeltaY() > 0) {
                        zoomIn();
                    } else {
                        zoomOut();
                    }
                }
            }
        };

        referenceView.getWebView().setOnScroll(scrollZoom);
        comparedView.getWebView().setOnScroll(scrollZoom);
        linesPerPageTextField.setText(String.valueOf(linesPerPage));
    }

    /**
     * The file used as a reference in the diff
     */
    private File referenceFile;

    public File getReferenceFile() {
        return referenceFile;
    }

    public void setReferenceFile(File referenceFile) {
        this.referenceFile = referenceFile;
        if(comparedFile!=null)
            setOffsetMax(Math.max(referenceFile.length(), comparedFile.length()));
        else
            setOffsetMax(referenceFile.length());
    }

    /**
     * The file compared to the reference
     */
    private File comparedFile;

    public File getComparedFile() {
        return comparedFile;
    }

    public void setComparedFile(File comparedFile) {
        this.comparedFile = comparedFile;
        if(referenceFile!=null)
            setOffsetMax(Math.max(referenceFile.length(),comparedFile.length()));
        else
            setOffsetMax(comparedFile.length());
    }

    /**
     * The current page being displayed
     * page = ( offset / (nbLinesPerPage*32) ) + 1
     */
    private int page;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.offset = (page-1)*linesPerPage*32;
        this.currentPageLabel.setText(String.valueOf(page));
        this.page = page;
    }

    /**
     * The current offset
     */
    private long offset;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        setPage((int) ((offset/(linesPerPage*32))+1));
        this.offset = offset;
    }

    /**
     * The last offset we can go to according to
     * the file length
     */
    private long offsetMax;

    private void setOffsetMax(long offsetMax) {
        this.offsetMax = offsetMax;
        setPageMax((int) (( offsetMax/ (linesPerPage*32) ) + 1));
    }

    /**
     * The last page we can go to
     */
    private int pageMax;

    private void setPageMax(int pageMax){
        this.pageMax = pageMax;
        this.lastPageLabel.setText(" / " + String.valueOf(pageMax));
    }

    /**
     * The number of lines per page
     */
    private int linesPerPage;

    public int getLinesPerPage() {
        return linesPerPage;
    }

    public void setLinesPerPage(int linesPerPage) {
        this.linesPerPage = linesPerPage;
        linesPerPageTextField.setText(String.valueOf(linesPerPage));
        setPageMax((int) ((offsetMax / (linesPerPage * 32)) + 1));
        setPage((int) ((offset / (linesPerPage * 32)) + 1));
    }

    /**
     * The hex viewer for the reference file
     */
    private HexDiffWebView referenceView;

    public HexDiffWebView getReferenceView() {
        return referenceView;
    }

    public void setReferenceView(HexDiffWebView referenceView) {
        this.referenceView = referenceView;
    }

    /**
     * The hex viewer for the compared file
     */
    private HexDiffWebView comparedView;

    public HexDiffWebView getComparedView() {
        return comparedView;
    }

    public void setComparedView(HexDiffWebView comparedView) {
        this.comparedView = comparedView;
    }

    public void loadDiff(File reference, File compared, long offset){
        setReferenceFile(reference);
        setComparedFile(compared);
        setOffset(offset);
        HexDiff diff = new HexDiff(reference,compared,offset,linesPerPage);
        this.referenceView.loadLines(diff, true);
        this.comparedView.loadLines(diff,false);
    }

    /**
     * The splitpane separing the web views
     */
    private SplitPane splitPane;

    public SplitPane getSplitPane() {
        return splitPane;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * TOOLBAR                                                                                                         *
     *  ______________________________________________________________________________________________________________ *
     * |                                                ______                           ______                       |*
     * | <-  currentPage / pageMax ->   |  Go to page |______| [Go]   |  Go to offset |______| [Go] | Lines per page  |*
     * |______________________________________________________________________________________________________________|*
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The toolbar used to change pages
     */
    private ToolBar toolBar;

    public ToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(ToolBar toolBar) {
        this.toolBar = toolBar;
    }

    /**
     * The current page displayed in the toolbar
     */
    private Label currentPageLabel;

    /**
     * The last page that can be displayed
     */
    private Label lastPageLabel;

    /**
     * The number of lines displayed per page
     */
    private TextField linesPerPageTextField;

    /**
     * Initialize the toolbar
     * @return the initialized toolbar
     */
    private ToolBar initToolBar() {
        ToolBar toolBar = new ToolBar();

        //Page indicator
        IconButton previousPageBtn = new IconButton();
        previousPageBtn.setIcon("CHEVRON_LEFT");
        HBox pageIndicator = new HBox();
        currentPageLabel  = new Label();
        lastPageLabel = new Label();
        IconButton nextPageBtn = new IconButton();
        nextPageBtn.setIcon("CHEVRON_RIGHT");
        pageIndicator.getChildren().addAll(currentPageLabel, lastPageLabel);
        pageIndicator.setAlignment(Pos.CENTER);
        toolBar.getItems().addAll(previousPageBtn, pageIndicator, nextPageBtn, new Separator());

        //Go to page
        Label gotoPageLabel = new Label("Go to page");
        TextField gotoPageTxt = new TextField();
        gotoPageTxt.setPrefWidth(50);
        gotoPageTxt.setAlignment(Pos.BASELINE_RIGHT);
        Button gotoPageBtn = new Button("Go");
        toolBar.getItems().addAll(gotoPageLabel, gotoPageTxt, gotoPageBtn, new Separator());

        //Go to offset
        Label gotoOffsetLabel = new Label("Go to offset");
        TextField gotoOffsetTxt = new TextField();
        gotoOffsetTxt.setPromptText("Offset in hex");
        gotoOffsetTxt.setAlignment(Pos.BASELINE_RIGHT);
        Button gotoOffsetBtn = new Button("Go");
        toolBar.getItems().addAll(gotoOffsetLabel, gotoOffsetTxt, gotoOffsetBtn, new Separator());

        //Set lines per page
        Label linesPerPageLabel = new Label("Lines per page");
        linesPerPageTextField = new TextField();
        linesPerPageTextField.setAlignment(Pos.BASELINE_RIGHT);
        linesPerPageTextField.setPrefWidth(40);
        toolBar.getItems().addAll(linesPerPageLabel, linesPerPageTextField, new Separator());

        //Zoom
        IconButton zoomInBtn = new IconButton();
        zoomInBtn.setIcon("SEARCH_PLUS");
        IconButton zoomOutBtn = new IconButton();
        zoomOutBtn.setIcon("SEARCH_MINUS");
        toolBar.getItems().addAll(zoomOutBtn, zoomInBtn);

        //Set actions
        previousPageBtn.setOnAction(event -> previousPage());
        nextPageBtn.setOnAction(event -> nextPage());
        gotoPageTxt.setOnAction(event -> gotoPage(Integer.parseInt(gotoPageTxt.getText())));
        gotoPageBtn.setOnAction(event -> gotoPage(Integer.parseInt(gotoPageTxt.getText())));
        gotoOffsetTxt.setOnAction(event -> gotoOffset(Long.parseLong(gotoOffsetTxt.getText(), 16)));
        gotoOffsetBtn.setOnAction(event -> gotoOffset(Long.parseLong(gotoOffsetTxt.getText(), 16)));
        linesPerPageTextField.setOnAction(event1 -> {
            setLinesPerPage(Integer.valueOf(linesPerPageTextField.getText()));
            reloadWebViews();
        });
        zoomInBtn.setOnAction(event -> zoomIn());
        zoomOutBtn.setOnAction(event -> zoomOut());
        return toolBar;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * NAVIGATION                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Go to the hex dump next page
     */
    private void nextPage(){
        long newOffset = offset + linesPerPage*32;
        if(newOffset<offsetMax) {
            setOffset(offset + linesPerPage*32);
            reloadWebViews();
        }
    }

    /**
     * Go to the previous hex dump page
     */
    private void previousPage(){
        if(offset>=linesPerPage*32){
           setOffset(offset - linesPerPage*32);
            reloadWebViews();
        }
    }

    /**
     * Go to the specified offset
     * @param newOffset    The offset to use as a start point when loading the hex view
     */
    private void gotoOffset(long newOffset){
        if(newOffset<offsetMax && newOffset>=0){
            setOffset(newOffset);
            reloadWebViews();
        }
    }

    /**
     * Go to the specified page
     * @param page  The page to load in the hex view
     */
    private void gotoPage(int page) {
        if(page<=pageMax && page>0){
            setPage(page);
            reloadWebViews();
        }
    }

    /**
     * Reload the hex view
     */
    private void reloadWebViews(){
        HexDiff diff = new HexDiff(referenceFile, comparedFile, offset, linesPerPage);
        this.referenceView.loadLines(diff, true);
        this.comparedView.loadLines(diff, false);
    }

    /**
     * Zoom in : enlarge the webview's font
     */
    private void zoomIn(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
    }


    /**
     * Zoom out : shrink the webview's font
     */
    private void zoomOut(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
    }


}
