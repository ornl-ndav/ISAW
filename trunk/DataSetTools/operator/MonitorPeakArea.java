/*
 * @(#)MonitorPeakArea.java   0.1  2000/07/24   Dennis Mikkelson
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
 * Revision 1.1  2000/07/24 15:51:26  dennis
 * Operator to calculate the area under the peak of a monitor Data block
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.peak.*;

/**
  *  This operator calculates the area under a peak in a specified monitor
  *  using a specified extent factor. The Group ID of the monitor to be 
  *  integrated is specified by the parameter "Group ID".  The interval 
  *  [a,b] over which the integration is determined based on the "extent 
  *  factor".  Specifically, the peak is used over the interval of length
  *  (extent_factor * FWHM) centered on the peak.
  *  This operator just produces a numerical result that is displayed 
  *  in the operator dialog box.
  */

public class MonitorPeakArea  extends    DS_Special 
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

  public MonitorPeakArea( )
  {
    super( "Monitor Peak Area" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to find the area under a monitor peak for
   *  the specified monitor DataSet using the specified group_id and 
   *  extent factor. 
   *
   *  @param  ds              The monitor DataSet from which a peak area is to
   *                          be found. 
   *  @param  group_id        The group_id of the monitor Data block that is
   *                          to be integrated.
   *  @param  extent_factor   The peak is integrated over an interval of
   *                          length extent_factor*FWHM centered on the peak. 
   */

  public MonitorPeakArea( DataSet      ds,
                          int          group_id,
                          float        extent_factor ) 
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter(0);
    parameter.setValue( new Integer( group_id ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( extent_factor ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "PeakA";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Monitor Group ID",new Integer(1));
    addParameter( parameter );

    parameter = new Parameter("Extent factor", new Float(8.5));
    addParameter( parameter );

  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    int group_id        = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float extent_factor = ( (Float)(getParameter(1).getValue()) ).floatValue();

                                     // get the current data set and do the 
                                     // operation
    DataSet ds = this.getDataSet();

    Data data = ds.getData_entry_with_id( group_id );
    if ( data == null )
    {
      ErrorString message = new ErrorString( 
                           "ERROR: no data entry with the group_ID "+group_id );
      System.out.println( message );
      return message;
    }
    else
    {
      HistogramDataPeak peak = new HistogramDataPeak( data, extent_factor );
      peak.setEvaluationMode( IPeak.PEAK_ONLY );
      float result = peak.Area();
      return new Float( result );  
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current MonitorPeakArea Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    MonitorPeakArea new_op = new MonitorPeakArea( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    Operator op = new MonitorPeakArea();

    String list[] = op.getCategoryList();
    System.out.println( "Categories are: " );
    for ( int i = 0; i < list.length; i++ )
      System.out.println( list[i] );
  }


}
