/*
 * @(#)ConvertFunctionToHistogram.java
 *
 * Programmer:  Dennis Mikkelson
 *             
 * $Log$
 * Revision 1.1  2000/12/07 22:35:06  dennis
 * Operator to convert tabulated function data to histogram data.
 *
 * 
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  * This operator converts the tabulated functions in a DataSet to histograms.  
  */

public class ConvertFunctionToHistogram extends    AnalyzeOp 
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

  public ConvertFunctionToHistogram()
  {
    super( "Convert Tabulated Functions to Histograms" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  width_1     Width of the first histogram bin to be created.
   *                      All later histogram bin widths will be generated from
   *                      this and the current tabluated x-coordinates assuming
   *                      that the tabluated x-coordinates are the centers of
   *                      the corresponding histogram bins.  This process is 
   *                      error prone, so if a first bin width is specified,
   *                      be sure to do it accurately.  If the first bin width
   *                      is specified as zero, the distance between the first
   *                      two x values is used by default.  This works properly
   *                      for uniformly spaced x values.     
   *  @param  multiply    Flag that indicates whether the function values
   *                      should be multiplied by the width of the new
   *                      histogram bins that are created.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the original
   *                      DataSet are just altered.
   */

  public ConvertFunctionToHistogram( DataSet  ds,
                                     float    width_1,
                                     boolean  multiply,
                                     boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float(width_1) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean(multiply) );

    parameter = getParameter( 2 );
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
     return "ConvFunc";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Width of first bin", new Float(0) );
    addParameter( parameter );

    parameter = new Parameter("Multiply by histogram bin width?",
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Make new DataSet?", new Boolean(true) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                  // get the parameters specified by the user 

    float width_1       = ((Float)getParameter(0).getValue()).floatValue();
    boolean multiply    = ((Boolean)getParameter(1).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(2).getValue()).booleanValue();

    DataSet ds     = getDataSet();
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Converted to histogram" );

    Data             data,
                     new_data;
    int              num_data = ds.getNum_entries();

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry

      if ( make_new_ds )
      {
        new_data = (Data)data.clone();
        new_data.ConvertToHistogram( width_1, multiply );
        new_ds.addData_entry( new_data );
      }
      else
        data.ConvertToHistogram( width_1, multiply );
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Data converted to histograms" );
    }
 }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current ConvertFunctionToHistogram Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataSetOperator new_op = new ConvertFunctionToHistogram( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
