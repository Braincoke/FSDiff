package com.erwandano.fsdiff.diffwindow;

import com.erwandano.fsdiff.core.DiffStatus;
import com.erwandano.fsdiff.core.PathDiff;
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
 * Implements a TreeItem<PathDiff> that can be filtered according to its status
 */
public class DiffTreeItem extends TreeItem<PathDiff> {

    public DiffTreeItem(PathDiff value){
        super(value);
    }

    /**
     * Filter only the files in the current branch. Returns a list of the filtered files.
     * @param diffStatusFilter          Filters to select only specific DiffStatus
     * @param regex                     The regular expression to apply to the file path
     * @param useRegex                  Indicates if the regex should be used as a regex or not
     * @return                          A list of the filtered files
     */
    public List<DiffTreeItem> filterFiles(final HashMap<DiffStatus, Boolean> diffStatusFilter,
                                                String regex, boolean useRegex){

        ArrayList<DiffTreeItem> filteredList = new ArrayList<>();
        this.filterList(filteredList, diffStatusFilter, regex, useRegex);
        return filteredList;
    }

    /**
     * Recursive function to filter this branch
     * The list is updated with the filtered items
     * Only files are kept in this filter
     * @param list                      list holding the filtered items
     * @param diffStatusFilter    the list of statuses and which to filter
     * @param regex                     a regular expression to apply to the file path
     */
    public void filterList(List<DiffTreeItem> list,
                           final HashMap<DiffStatus, Boolean> diffStatusFilter,
                           String regex, boolean useRegex){
        DiffTreeItem readOnlyRoot = this;
        boolean passedFilter = false;
        //Are we filtering a leaf (== file)?
        if(readOnlyRoot.getChildren().size() <= 0){
            DiffStatus status = readOnlyRoot.getStatus();
            for (Map.Entry<DiffStatus, Boolean> entry : diffStatusFilter.entrySet()) {
                if (status.isEqual(entry.getKey()) && entry.getValue()) {
                    passedFilter = true;
                }
            }
            if (passedFilter) {
                PathDiff pathDiff = readOnlyRoot.getValue();
                if (regex.trim().compareTo("") == 0) {
                    list.add(readOnlyRoot);
                } else{
                    if(useRegex && (pathDiff.getPath().toString().matches(regex)))
                        list.add(readOnlyRoot);
                    else if(!useRegex && (pathDiff.getPath().toString().contains(regex)))
                        list.add(readOnlyRoot);
                }
            }
        } else if(readOnlyRoot.isDirectory()) {
            //We are filtering a branch, we will not add it to the list, but we might with its children
            int directoryStatus[] = readOnlyRoot.getValue().getDirectoryStatus();
            //The item passed the tests, it means that its children might pass them too
            for(Map.Entry<DiffStatus, Boolean> entry : diffStatusFilter.entrySet()){
                if (directoryStatus[entry.getKey().getIndex()] > 0 && entry.getValue()) {
                    passedFilter = true;
                }
            }
            if (passedFilter) {
                readOnlyRoot.getChildrenList().stream()
                        .filter(child -> child != null)
                        .forEach(child -> child.filterList(list, diffStatusFilter, regex, useRegex));
            }
        } else {
            //We are filtering the root that does not hold a proper PathDiff
            //Filter every child
            readOnlyRoot.getChildrenList().stream()
                    .filter(child -> child != null)
                    .forEach(child -> child.filterList(list, diffStatusFilter, regex, useRegex));
        }
    }

    /**
     * Filter this branch according to the specified options
     * The result is a new root containing the folders and files filtered
     * @param filterOptions    the list of statuses to filter
     * @return  A new DiffTreeItem filtered branch , where all its children are also filtered
     */
    public DiffTreeItem filter(final HashMap<DiffStatus, Boolean> filterOptions){
        DiffTreeItem readOnlyRoot = this;
        //The returned TreeItem
        DiffTreeItem filteredBranch = null;
        //Are we filtering a leaf (== file)?
        if(readOnlyRoot.getChildren().size() <= 0){
            DiffStatus status = readOnlyRoot.getStatus();
            for(Map.Entry<DiffStatus, Boolean> entry : filterOptions.entrySet()){
                if(status.isEqual(entry.getKey()) && entry.getValue()){
                    //The item passed the tests
                    filteredBranch = new DiffTreeItem(readOnlyRoot.getValue());
                }
            }
        } else if(readOnlyRoot.isDirectory()) {
            //We are filtering a branch
            int directoryStatus[] = readOnlyRoot.getValue().getDirectoryStatus();
            for(Map.Entry<DiffStatus, Boolean> entry : filterOptions.entrySet()){
                if(directoryStatus[entry.getKey().getIndex()]>0 && entry.getValue()){
                    //The item passed the tests
                    filteredBranch = new DiffTreeItem(readOnlyRoot.getValue());
                    for(DiffTreeItem child : readOnlyRoot.getChildrenList()){
                        //Filter every child
                        DiffTreeItem filteredChild = child.filter(filterOptions);
                        if(filteredChild!=null){
                            filteredBranch.getChildren().add(filteredChild);
                        }
                    }
                }
            }
        } else {
            //We are filtering the root element which does not hold a properly initialized PathDiff
            filteredBranch = new DiffTreeItem(readOnlyRoot.getValue());
            for(DiffTreeItem child : readOnlyRoot.getChildrenList()){
                //Filter every child
                DiffTreeItem filteredChild = child.filter(filterOptions);
                if(filteredChild!=null){
                    filteredBranch.getChildren().add(filteredChild);
                }
            }
        }
        return filteredBranch;
    }

    public PathDiff getPathDiff(){
        return this.getValue();
    }

    public Path getPath() {
        return this.getValue() == null ? null : this.getValue().getPath();
    }

    public String getName() {
        return this.getValue() == null ? null : this.getValue().getName();
    }

    public DiffStatus getStatus() {
        return this.getValue() == null ? null : this.getValue().getStatus();
    }

    public Boolean isDirectory(){
        return this.getValue() == null ? null : this.getValue().isDirectory();
    }

    public ObservableList<DiffTreeItem> getChildrenList(){
        ObservableList<TreeItem<PathDiff>> children = this.getChildren();
        ObservableList<DiffTreeItem> childrenList = FXCollections.observableArrayList();
        childrenList.addAll(children.stream().map(treeItem -> (DiffTreeItem) treeItem).collect(Collectors.toList()));
        return childrenList;
    }

    public void collapseBranch(){
        for(DiffTreeItem item : this.getChildrenList()){
            if(item.getChildren().size() != 0)
                item.collapseBranch();
            item.setExpanded(false);
        }
    }

    public void expandBranch(){
        for(DiffTreeItem item : this.getChildrenList()){
            if(item.getChildren().size() != 0)
                item.expandBranch();
            item.setExpanded(true);
        }
    }
}
