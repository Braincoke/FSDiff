package gui.wizard.comparison;

import core.FileSystemComparison;
import core.FileSystemHash;

/**
 * Compare the two file systems and show progress of the comparison
 */
public class ComparisonProgressController extends ComparisonWizardPane {

    private FileSystemComparison comparison;


    public void compare(){
        FileSystemHash refFSH = wizard.getReferenceFSH();
        FileSystemHash comFSH = wizard.getComparedFSH();
        comparison =  comFSH.compareTo(refFSH);
        wizard.setComparison(comparison);
        wizard.gotoComparisonInterface();
    }
}
