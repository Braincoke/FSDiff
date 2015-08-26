package ui.diff;

import core.DiffStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.Controller;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parameters to create the exclusion file to export
 */
public class ExportExclusionDialog extends Controller {

    @FXML
    private CheckBox showMatched;
    @FXML
    private CheckBox showModified;
    @FXML
    private CheckBox showCreated;
    @FXML
    private CheckBox showDeleted;
    @FXML
    private TextField regex;
    @FXML
    private TextField filePath;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public static void display(DiffWindowController windowController){
        String fxml = "ExportExclusionDialog.fxml";
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Export");
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(ExportExclusionDialog.class.getResource(fxml));
        AnchorPane page;
        try (InputStream in = ExportExclusionDialog.class.getResourceAsStream(fxml)) {
            page = loader.load(in);
            ExportExclusionDialog controller = loader.getController();
            if(windowController==null){
                System.out.println("wc null");
            }
            if(controller == null){
                System.out.println("controller null");
            }
            controller.setWindowController(windowController);
            controller.setStage(stage);
            stage.setScene(new Scene(page, 300, 300));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DiffWindowController windowController;

    public void setWindowController(DiffWindowController windowController){
        this.windowController = windowController;
    }


    public void browse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save the file");
        fileChooser.setInitialDirectory(windowController.getOutputFile().getParent().toFile());
        File file = fileChooser.showSaveDialog(windowController.getApplication().getStage());
        if(file!=null){
            filePath.setText(file.getAbsolutePath());
        }
    }

    public void cancel(){
        stage.close();
    }

    public void export(){
        HashMap<DiffStatus, Boolean> options = new HashMap<>();
        options.put(DiffStatus.MATCHED, showMatched.isSelected());
        options.put(DiffStatus.MODIFIED, showModified.isSelected());
        options.put(DiffStatus.CREATED, showCreated.isSelected());
        options.put(DiffStatus.DELETED, showDeleted.isSelected());
        List<DiffTreeItem> filteredItems = windowController.getRootTreeItem().filterFiles(options, regex.getText(), true);
        List<String> filteredFiles = filteredItems.stream().map(item -> item.getPath().toString()).collect(Collectors.toList());
        //Save the file
        try {
            File fout = new File(filePath.getText());
            FileOutputStream fos = new FileOutputStream(fout);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for(String str : filteredFiles){
                bw.write(str);
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            showExceptionDialog(e);
        } finally {
            stage.close();
        }
    }


    private void showExceptionDialog(Exception e){

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText("Failed to save the file");

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
}
