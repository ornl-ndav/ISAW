/*
 * @(#)ConvertHistogramToFunction.java   0.1  2000/08/03   Dennis Mikkelson
 *             
 * $Log$
 * Revision 1.2  2000/11/10 22:41:34  dennis
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
 * Revision 1.1  2000/08/03 15:45:35  dennis
 * Operator to convert a histogram to a function Data block
 *
 * 
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  * This operator converts the histograms in a DataSet to tabulated functions.  
  */

public class ConvertHistogramToFunction extends    AnalyzeOp 
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

  public ConvertHistogramToFunction( )
  {
    super( "Convert Histograms to Tabulated Functions" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  divide      Flag that indicates whether the histogram values
   *                      should be divided by the width of the histogram bin.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the original
   *                      DataSet are just altered.
   */

  public ConvertHistogramToFunction( DataSet  ds,
                                     boolean  divide,
                                     boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Boolean(divide) );

    parameter = getParameter( 1 );
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
     return "ConvHist";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Divide by histogram bin width?",
                                         new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Make new DataSet?", new Boolean(true) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                  // get the parameters specified by the user 

    boolean divide      = ((Boolean)getParameter(0).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(1).getValue()).booleanValue();

    DataSet ds     = getDataSet();
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Converted to function" );

    Data             data,
                     new_data;
    int              num_data = ds.getNum_entries();

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry

      if ( make_new_ds )
      {
        new_data = (Data)data.clone();
        new_data.ConvertToFunction( divide );
        new_ds.addData_entry( new_data );
      }
      else
        data.ConvertToFunction( divide );
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Data converted to functions" );
    }
 }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current ConvertHistogramToFunction Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    ConvertHistogramToFunction new_op = new ConvertHistogramToFunction( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
