package gui.comparison;

import core.ComparisonStatus;
import core.PathComparison;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements a TreeItem<PathComparison> that can be filtered according to its status
 */
public class ComparisonTreeItem extends TreeItem<PathComparison> {

    public ComparisonTreeItem(PathComparison value){
        super(value);
    }

    /**
     * Filter only the files in the current branch. Returns a list of the filtered files.
     * @param comparisonStatusFilter    The comparison status filters
     * @param regex                     The regular expression to apply to the file path
     * @param useRegex                  Indicates if the regex should be used as a regex or not
     * @return                          A list of the filtered files
     */
    public List<ComparisonTreeItem> filterFiles(final HashMap<ComparisonStatus, Boolean> comparisonStatusFilter,
                                                String regex, boolean useRegex){

        ArrayList<ComparisonTreeItem> filteredList = new ArrayList<>();
        this.filterList(filteredList, comparisonStatusFilter, regex, useRegex);
        return filteredList;
    }

    /**
     * Recursive function to filter this branch
     * The list is updated with the filtered items
     * Only files are kept in this filter
     * @param list                      list holding the filtered items
     * @param comparisonStatusFilter    the list of statuses and which to filter
     * @param regex                     a regular expression to apply to the file path
     */
    public void filterList(List<ComparisonTreeItem> list,
                           final HashMap<ComparisonStatus, Boolean> comparisonStatusFilter,
                           String regex, boolean useRegex){
        ComparisonTreeItem readOnlyRoot = this;
        boolean passedFilter = false;
        //Are we filtering a leaf (== file)?
        if(readOnlyRoot.getChildren().size() <= 0){
            ComparisonStatus status = readOnlyRoot.getStatus();
            for (Map.Entry<ComparisonStatus, Boolean> entry : comparisonStatusFilter.entrySet()) {
                if (status.isEqual(entry.getKey()) && entry.getValue()) {
                    passedFilter = true;
                }
            }
            if (passedFilter) {
                PathComparison pathComparison = readOnlyRoot.getValue();
                if (regex.trim().compareTo("") == 0) {
                    list.add(readOnlyRoot);
                } else{
                    if(useRegex && (pathComparison.getPath().toString().matches(regex)))
                        list.add(readOnlyRoot);
                    else if(!useRegex && (pathComparison.getPath().toString().contains(regex)))
                        list.add(readOnlyRoot);
                }
            }
        } else if(readOnlyRoot.isDirectory()) {
            //We are filtering a branch, we will not add it to the list, but we might with its children
            int directoryStatus[] = readOnlyRoot.getValue().getDirectoryStatus();
            //The item passed the tests, it means that its children might pass them too
            for(Map.Entry<ComparisonStatus, Boolean> entry : comparisonStatusFilter.entrySet()){
                if (directoryStatus[entry.getKey().getIndex()] > 0 && entry.getValue()) {
                    passedFilter = true;
                }
            }
            if (passedFilter) {
                readOnlyRoot.getChildrenList().stream()
                        .filter(child -> child != null)
                        .forEach(child -> child.filterList(list, comparisonStatusFilter, regex, useRegex));
            }
        } else {
            //We are filtering the root that does not hold a proper PathComparison
            //Filter every child
            readOnlyRoot.getChildrenList().stream()
                    .filter(child -> child != null)
                    .forEach(child -> child.filterList(list, comparisonStatusFilter, regex, useRegex));
        }
    }

    /**
     * Filter this branch according to the specified options
     * The result is a new root containing the folders and files filtered
     * @param filterOptions    the list of statuses to filter
     * @return  A new ComparisonTreeItem filtered branch , where all its children are also filtered
     */
    public ComparisonTreeItem filter(final HashMap<ComparisonStatus, Boolean> filterOptions){
        ComparisonTreeItem readOnlyRoot = this;
        //The returned TreeItem
        ComparisonTreeItem filteredBranch = null;
        //Are we filtering a leaf (== file)?
        if(readOnlyRoot.getChildren().size() <= 0){
            ComparisonStatus status = readOnlyRoot.getStatus();
            for(Map.Entry<ComparisonStatus, Boolean> entry : filterOptions.entrySet()){
                if(status.isEqual(entry.getKey()) && entry.getValue()){
                    //The item passed the tests
                    filteredBranch = new ComparisonTreeItem(readOnlyRoot.getValue());
                }
            }
        } else if(readOnlyRoot.isDirectory()) {
            //We are filtering a branch
            int directoryStatus[] = readOnlyRoot.getValue().getDirectoryStatus();
            for(Map.Entry<ComparisonStatus, Boolean> entry : filterOptions.entrySet()){
                if(directoryStatus[entry.getKey().getIndex()]>0 && entry.getValue()){
                    //The item passed the tests
                    filteredBranch = new ComparisonTreeItem(readOnlyRoot.getValue());
                    for(ComparisonTreeItem child : readOnlyRoot.getChildrenList()){
                        //Filter every child
                        ComparisonTreeItem filteredChild = child.filter(filterOptions);
                        if(filteredChild!=null){
                            filteredBranch.getChildren().add(filteredChild);
                        }
                    }
                }
            }
        } else {
            //We are filtering the root element which does not hold a properly initialized PathComparison
            filteredBranch = new ComparisonTreeItem(readOnlyRoot.getValue());
            for(ComparisonTreeItem child : readOnlyRoot.getChildrenList()){
                //Filter every child
                ComparisonTreeItem filteredChild = child.filter(filterOptions);
                if(filteredChild!=null){
                    filteredBranch.getChildren().add(filteredChild);
                }
            }
        }
        return filteredBranch;
    }

    public PathComparison getPathComparison(){
        return this.getValue();
    }

    public Path getPath() {
        return this.getValue() == null ? null : this.getValue().getPath();
    }

    public String getName() {
        return this.getValue() == null ? null : this.getValue().getName();
    }

    public ComparisonStatus getStatus() {
        return this.getValue() == null ? null : this.getValue().getStatus();
    }

    public Boolean isDirectory(){
        return this.getValue() == null ? null : this.getValue().isDirectory();
    }

    public ObservableList<ComparisonTreeItem> getChildrenList(){
        ObservableList<TreeItem<PathComparison>> children = this.getChildren();
        ObservableList<ComparisonTreeItem> childrenList = FXCollections.observableArrayList();
        childrenList.addAll(children.stream().map(treeItem -> (ComparisonTreeItem) treeItem).collect(Collectors.toList()));
        return childrenList;
    }

    public void collapseBranch(){
        for(ComparisonTreeItem item : this.getChildrenList()){
            if(item.getChildren().size() != 0)
                item.collapseBranch();
            item.setExpanded(false);
        }
    }

    public void expandBranch(){
        for(ComparisonTreeItem item : this.getChildrenList()){
            if(item.getChildren().size() != 0)
                item.collapseBranch();
            item.setExpanded(true);
        }
    }
}
