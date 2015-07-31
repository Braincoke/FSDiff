package gui.comparison;

import core.ComparisonStatus;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.layout.HBox;

/**
 * A custom menu item for the filter drop down
 * /!\ Always specify the status before the checked property in the FXML file
 */
public class FilterDropDownMenuItem extends CustomMenuItem{

    /**
     * Comparison status that can be selected
     */
    private StringProperty status;

    public String getStatus(){
        return this.status.getValue();
    }

    public void setStatus(String status){
        this.status.setValue(status);
        FilterDropDownCell cell = new FilterDropDownCell(status);
        checked.bindBidirectional(checkBox.selectedProperty());
        this.setContent(cell);
    }

    /**
     * Indicates if the comparison status is selected to be filtered
     */
    private BooleanProperty checked;


    public BooleanProperty selectedProperty(){
        return checkBox==null ? null : checkBox.selectedProperty();
    }

    public void setSelected(Boolean selected){
        checkBox.setSelected(selected);
    }

    public Boolean isSelected(){
        return checkBox.isSelected();
    }

    public BooleanProperty checkedProperty(){
        return checked;
    }

    public Boolean getChecked(){
        return this.checked.getValue();
    }

    public void setChecked(Boolean selected){
        this.checked.setValue(selected);
    }

    /**
     * Checkbox to select the comparison status as a filter option
     */
    private CheckBox checkBox;

    /**
     * The comparison status that can be used as a filter option
     */
    private ComparisonStatus comparisonStatus;

    public FilterDropDownMenuItem(){
        super();
        this.setHideOnClick(false);
        this.checked = new SimpleBooleanProperty();
        this.status = new SimpleStringProperty();
    }

    class FilterDropDownCell extends HBox {

        public FilterDropDownCell() {
            status = new SimpleStringProperty();
            buildCell();
        }

        public FilterDropDownCell(ComparisonStatus statusArg) {
            status = new SimpleStringProperty();
            comparisonStatus = statusArg;
            status.setValue(comparisonStatus.name());
            buildCell();
        }

        public FilterDropDownCell(String statusArg) {
            status = new SimpleStringProperty();
            setStatus(statusArg);
            buildCell();
        }

        private void setStatus(String statusArg) {
            for (ComparisonStatus s : ComparisonStatus.values()) {
                if (s.name().compareToIgnoreCase(statusArg) == 0) {
                    comparisonStatus = s;
                    status.setValue(statusArg);
                }
            }
        }


        /**********************************************************
         *
         * Build the filter cell
         *   ____________________________________
         *  |   _                                |
         *  |  |_|  FilterText                   |
         *  |____________________________________|
         *
         * *******************************************************/
        private void buildCell(){
            String text = status.getValue().substring(0,1).toUpperCase() + status.getValue().substring(1).toLowerCase();
            checkBox = new CheckBox(text);
            checkBox.getStyleClass().add(comparisonStatus.name().toLowerCase());
            this.getChildren().add(checkBox);
        }

    }
}
