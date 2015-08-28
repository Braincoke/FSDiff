package com.erwandano.fsdiff.diffwindow.toppane;

import com.erwandano.fsdiff.components.Controller;
import com.erwandano.fsdiff.diffwindow.DiffWindowController;
import com.erwandano.fsdiff.settingswindow.ExportExclusionDialog;
import com.erwandano.fsdiff.welcomescreen.NewDiffController;
import com.erwandano.fsdiff.wizard.hash.HashWizard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
/**
 * Menu bar for the DiffWindow
 */
public class MenuBarController extends Controller {

    private DiffWindowController windowController;

    public DiffWindowController getWindowController() {
        return windowController;
    }

    public void setWindowController(DiffWindowController windowController) {
        this.windowController = windowController;
        this.application = windowController.getApplication();
    }

    public void newProject() {
        //TODO find a way to open a new INDEPENDENT window instead of replacing this one
        NewDiffController.init(application);

    }

    public void newHash() {
        //TODO find a way to open a new INDEPENDENT window instead of replacing this one
        new HashWizard(application);
    }

    public void about() {
        //TODO write things about FSDiff here, add hyperlink to github page
    }

    public void open() {
        windowController.openFSC();
    }

    public void save() {
        windowController.saveFSC();
    }

    public void saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save diff");
        fileChooser.setInitialDirectory(windowController.getOutputFile().toFile().getParentFile());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Diff file", "*.fscx"));
        fileChooser.setInitialFileName(windowController.getOutputFile().getFileName().toString());
        File file = fileChooser.showSaveDialog(application.getStage());
        if (file != null) {
            windowController.setOutputFile(file.toPath());
            windowController.saveFSC();
        }
    }

    /**
     * Import an exclusion file
     */
    public void importExclusion() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(windowController.getOutputFile().getParent().toFile());
        fileChooser.setTitle("Choose an exclusion file");
        File file = fileChooser.showOpenDialog(application.getStage());
        if(file!=null){
            windowController.getExclusionFiles().add(file.toPath());
            windowController.resetExcludedFiles();
        }
    }

    /**
     * Export an exclusion file
     */
    public void exportExclusion() {
        ExportExclusionDialog.display(windowController);
    }

    /**
     * Remove an exclusion file from the list of exclusion files
     */
    public void removeExclusionFile(){
        List<Path> exclusionFiles = windowController.getExclusionFiles();
        if(exclusionFiles.size()>0) {
            ChoiceDialog<Path> dialog = new ChoiceDialog<>();
            dialog.getItems().setAll(exclusionFiles);
            dialog.setSelectedItem(exclusionFiles.get(0));
            dialog.setTitle("Choose a file to remove");
            Optional<Path> chosen = dialog.showAndWait();
            if (chosen.get() != null) {
                windowController.getExclusionFiles().remove(chosen.get());
                windowController.resetExcludedFiles();
            }
        } else {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setContentText("No exclusion file loaded yet");
            info.showAndWait();
        }
    }

    /**
     * Rebase one of the root directories
     */
    public void rebaseRootDir(){
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Rebase");
        dialog.setHeaderText("Rebase one of the root directories");
        dialog.setContentText("Please choose the system to rebase and the new root");
        VBox vBox = new VBox();
        ComboBox<String> comboBox = new ComboBox<>();
        ObservableList<String> systems = FXCollections.observableArrayList("Reference", "Compared");
        comboBox.setItems(systems);
        comboBox.getSelectionModel().selectFirst();
        HBox hbox = new HBox();
        TextField textField = new TextField();
        Button browse = new Button("...");
        hbox.getChildren().setAll(textField, browse);
        HBox.setHgrow(textField, Priority.ALWAYS);
        vBox.getChildren().setAll(comboBox, hbox);
        dialog.getDialogPane().setExpandableContent(vBox);

        hbox.setSpacing(5);
        vBox.setSpacing(10);
        dialog.getDialogPane().setExpanded(true);
        //Actions
        browse.setOnAction(event -> {
            FileChooser fileChooser  = new FileChooser();
            fileChooser.setTitle("Choose the new root directory");
            fileChooser.setInitialDirectory(windowController.getOutputFile().getParent().toFile());
            File file = fileChooser.showOpenDialog(application.getStage());
            if(file!=null){
                textField.setText(file.getAbsolutePath());
            }
        });

        Optional<ButtonType> choice = dialog.showAndWait();
        if(choice.get() == ButtonType.OK){
            boolean isReference = comboBox.getSelectionModel().getSelectedIndex() == 0;
            windowController.rebaseRootDirectory(isReference, textField.getText());
        }
    }
}
