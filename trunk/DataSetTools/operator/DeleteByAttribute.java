/*
 * @(#)DeleteByAttribute.java   0.1  2000/06/06   Dennis Mikkelson
 *             
 * $Log$
 * Revision 1.3  2000/11/10 22:41:34  dennis
 *    Introduced additional abstract classes to better categorize the operators.
 * Existing operators were modified to be derived from one of the new abstract
 * classes.  The abstract base class hierarchy is now:
 *
 *  Operator
 *
 *   -GenericOperator
 *      --GenericLoad
 *      --GenericBatch
 *
 *   -DataSetOperator
 *     --DS_EditList
 *     --DS_Math
 *        ---ScalarOp
 *        ---DataSetOp
 *        ---AnalyzeOp
 *     --DS_Attribute
 *     --DS_Conversion
 *        ---XAxisConversionOp
 *        ---YAxisConversionOp
 *        ---XYAxesConversionOp
 *     --DS_Special
 *
 *    To allow for automatic generation of hierarchial menus, each new operator
 * should fall into one of these categories, or a new category should be
 * constructed within this hierarchy for the new operator.
 *
 * Revision 1.2  2000/11/07 15:54:16  dennis
 * Replaced "Data blocks" with "Groups" in operator title.
 *
 * Revision 1.1  2000/07/10 22:36:02  dennis
 * July 10, 2000 version... many changes
 *
 * Revision 1.2  2000/06/09 16:12:35  dennis
 * Added getCommand() method to return the abbreviated command string for
 * this operator
 *
 * Revision 1.1  2000/06/08 15:27:11  dennis
 * Initial revision
 *
 * 
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  * This operator removes (or keeps) Data blocks with a specified attribute 
  * in a specified range.  The operator can either make a new DataSet, or
  * modify the current DataSet.
  */

public class DeleteByAttribute extends    DS_EditList 
                               implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this 
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult() 
   * to apply the operator to the DataSet this operator was added to. 
   */

  public DeleteByAttribute( )
  {
    super( "Delete Groups by Attribute" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *
   *  @param  attr_name   The name of that attribute to be used for the 
   *                      selection criterion from the data set.
   *
   *  @param  min         The lower bound for the selection criteria.  The
   *                      selected Data blocks satisfy:
   *                          min <= attribute value <= max
   *
   *  @parm   max         The upper bound for the selection criteria.
   *
   *  @param  status      Flag that indicates whether Data blocks that meet 
   *                      the selection criteria are to be kept or removed 
   *                      from the data set.
   *                      If status==true, the selected blocks are deleted.
   *                      If status==false, the un-selected blocks are deleted.
   *
   *  @param  make_new_ds Flag that determines whether removing the Data
   *                      blocks makes a new DataSet and returns the new
   *                      DataSet as a value, or just removes the selected
   *                      blocks from the current DataSet and returns a
   *                      message indicating that the remove operation was
   *                      done.
   */

  public DeleteByAttribute( DataSet  ds,
                            String   attr_name,
                            float    min,
                            float    max,
                            boolean  status,
                            boolean  make_new_ds   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new AttributeNameString(attr_name) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( min ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( max ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Boolean( status ) );

    parameter = getParameter( 4 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "DelAtt";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group Attribute to use for selection",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter( "Lower bound", new Float(-1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Upper bound", new Float(1.0) );
    addParameter( parameter );

    parameter = new Parameter("Delete (or keep) selected groups?",
                               new Boolean(true) );
    addParameter( parameter );
    
    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                  // get the parameters specified by the user 

    String  attr_name = 
            ((AttributeNameString)getParameter(0).getValue()).toString();
    float   min       = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float   max       = ( (Float)(getParameter(2).getValue()) ).floatValue();
    boolean status    = ((Boolean)getParameter(3).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(4).getValue()).booleanValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();

    DataSet new_ds = ds;             // set new_ds to either a reference to ds
    if ( make_new_ds )               // or a clone of ds
      new_ds = (DataSet)ds.clone();

    if ( status )
      new_ds.addLog_entry( "deleted groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
    else
      new_ds.addLog_entry( "kept groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
                                            // do the operation
    int num_data = new_ds.getNum_entries();
    Data data;

    for ( int i = num_data-1; i >= 0; i-- )
    {
      data = new_ds.getData_entry( i );    // get reference to the data entry
                                           // keep or reject it based on the
                                           // attribute value.
      Attribute attr = data.getAttributeList().getAttribute( attr_name );
      float val = (float)attr.getNumericValue(); 
      if (attr_name == Attribute.DETECTOR_POS )        // convert to degrees
        val *= (float) 180.0/Math.PI;

      if ( status && min <= val && val <= max  ||
          !status && (min > val || val > max)   ) 
        new_ds.removeData_entry( i );      
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
      new_ds.notifyIObservers( IObserver.DATA_DELETED );
      return new String("Specified Data blocks REMOVED");
    } 
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DeleteByAttribute Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DeleteByAttribute new_op = new DeleteByAttribute( );
                                               // copy the data set associated
                                               // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
