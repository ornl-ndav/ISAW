package Command;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 * Interface for a Script Processor<P>
 *
 * NOTE: Implementations of this should also implement Generic Operator
 */
public interface IScriptProcessor {

    //ISAW Interface routines
    /**
     * ISaw's Data Sets can be directly accessed by the script as
     * ISAWDS1, ISAWDS2,etc., where the 1, 2, .. are the tag numbers
     *
     * NOTE: If ISAW deletes a data set the script handler must delete
     * reference to it also.
     */
    public void  addDataSet(DataSet dss);
 
    /**
     * The IObserver is usually ISAW. If the script creates a new Data
     * Set it can be sent * to "all IOBSERVERS"
     */
    public void  addIObserver(IObserver iobs);
    public  void  deleteIObserver(IObserver iobs) ;
    public void deleteIObservers() ;
    
    /**
     * ISAW only uses the property "Display". The new Value is the
     * value(.toString()) that is displayed
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener P); 

 /**
  * This is the document to log actions performed by the script.
  * Hopefully the log document will eventually be executable to redo a
  * session
  */
    public void setLogDoc(javax.swing.text.Document doc) ;

    //Execution routines
    /**
     * Executes the one line of script in Doc form. 
     */
    public void execute1(javax.swing.text.Document Doc, int line); 

    /**
     * Must be set before getResult executes the code in this
     * document.<BR> The document does not have to be reset if only
     * the text in it is changed
     */
    public void setDocument( javax.swing.text.Document Doc);

    /**
     * Executes the script in the last document set by setDocument
     */
    public Object getResult() ;

    /**
     * Resets the variable namespace and values so a script can be
     * rerun from the start.  The data sets from addDataSet and the
     * IObservers are not reset
     */
    public void  reset(); 

    /**
     * Resets only the error status so execution of individual lines
     * can continue
     */
    public void resetError();

    /**
     * If at least zero, this method returns the position on the line
     * of the error
     */
 public int getErrorCharPos();

    /**
     * If at least zero, this method returns the line number(starting at
     * 0) of the error
     */
    public int getErrorLine() ;

    /**
     * Returns a message about the error
     */
    public String getErrorMessage() ;

    //utilities

}
