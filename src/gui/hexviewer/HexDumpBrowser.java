package gui.hexviewer;

import gui.components.buttons.IconButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDumpBrowser extends AnchorPane {

    public static final int DEFAULT_LINE_NUMBER_PER_PAGE = 15;
    private static final Double ZOOM_FACTOR = 0.10;

    public HexDumpBrowser(){
        super();
        linesPerPage = DEFAULT_LINE_NUMBER_PER_PAGE;
        this.toolBar = initToolBar();
        this.webView = new HexDumpWebView();
        VBox vBox = new VBox(toolBar, webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
        this.getChildren().add(vBox);
        setRightAnchor(vBox, 0d);
        setBottomAnchor(vBox, 0d);
        setLeftAnchor(vBox, 0d);
        setTopAnchor(vBox, 0d);

        //CTRL + SCROLL WHEEL TO ZOOM IN OR OUT
        webView.getWebView().setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });

        linesPerPageTextField.setText(String.valueOf(linesPerPage));
    }

    /**
     * The file which will be displayed
     */
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        setOffsetMax(file.length());
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
        setPageMax((int) (( offsetMax/ (linesPerPage*32) ) + 1));
        setPage((int) ((offset/(linesPerPage*32))+1));
    }

    /**
     * The hex dump viewer
     */
    private HexDumpWebView webView;

    public HexDumpWebView getWebView() {
        return webView;
    }

    public void setWebView(HexDumpWebView webView) {
        this.webView = webView;
    }

    /**
     * Load a file in the hex dump viewer
     * @param file      The file to load
     * @param offset    The starting point of the hex dump
     */
    public void loadFile(File file, long offset){
        setFile(file);
        setOffset(offset);
        this.webView.loadLines(file, offset, linesPerPage);
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
        pageIndicator.setPadding(new Insets(0,10,0,10));
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
            reloadWebView();
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
            reloadWebView();
        }
    }

    /**
     * Go to the previous hex dump page
     */
    private void previousPage(){
        if(offset>=linesPerPage*32){
           setOffset(offset - linesPerPage*32);
            reloadWebView();
        }
    }

    /**
     * Go to the specified offset
     * @param newOffset    The offset to use as a start point when loading the hex view
     */
    private void gotoOffset(long newOffset){
        if(newOffset<offsetMax && newOffset>=0){
            setOffset(newOffset);
            reloadWebView();
        }
    }

    /**
     * Go to the specified page
     * @param page  The page to load in the hex view
     */
    private void gotoPage(int page) {
        if(page<=pageMax && page>0){
            setPage(page);
            reloadWebView();
        }
    }

    /**
     * Reload the hex view
     */
    private void reloadWebView(){
        webView.loadLines(file, offset, linesPerPage);
    }

    /**
     * Zoom in : enlarge the webview's font
     */
    private void zoomIn(){
        Double currentZoom = webView.getWebView().getZoom();
        webView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
    }


    /**
     * Zoom out : shrink the webview's font
     */
    private void zoomOut(){
        Double currentZoom = webView.getWebView().getZoom();
        webView.getWebView().zoomProperty().setValue(currentZoom-ZOOM_FACTOR);
    }


}
