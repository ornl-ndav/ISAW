/*
 * @(#)CalculateMoment.java   0.2  99/07/27   Dennis Mikkelson
 *                                 99/08/16   Added constructor to allow 
 *                                            calling operator directly
 *             
 * This operator calculates a specified moment of a selected Data block over 
 * a specified inteval and returns a single real value.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
  *  Calculate the specified moment of the selected Data block over the  
  *  sppecified inteval. This operator calculates the integral of the data 
  *  values times a power of x.  The Group ID of the data block to be
  *  integrated is specified by the parameter "Group ID".  The power of x is 
  *  specified by the parameter "Moment".  The interval [a,b] over which the 
  *  integration is done is specified by the two endpoints a, b where it is 
  *  assumed that a < b.
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class  CalculateMoment  extends    DataSetOperator 
                               implements Serializable
{
  /* ----------------------- DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public CalculateMoment( )
  {
    super( "Calculate Moment" );

    Parameter parameter = new Parameter("Group ID",new Integer(0));
    addParameter( parameter );

    parameter = new Parameter("Left end point (a)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Right end point (b)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Center point for moment calculation", 
                               new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Moment ( 1, 2, 3 ... )", new Integer(1));
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct a CalculateMoment  operator for a specified DataSet and 
   *  with the specified parameter values so that the operation can be 
   *  invoked immediately by calling getResult()
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  group_id    The id of the group for which the moment is to be
   *                      calculated 
   *                      which the moment is to be calculated.
   *  @param  a           The left hand endpoint of the interval [a, b] over
   *                      which the moment is to be calculated.
   *  @param  b           The right hand endpoint of the interval [a, b] over
   *                      which the moment is to be calculated.
   *                      from the data set.
   *  @param  center      The center point for the moment calculation.
   *  @parm   moment      The moment to be calculated.
   */

  public CalculateMoment( DataSet             ds,
                          int                 group_id,
                          float               a,
                          float               b,
                          float               center,
                          int                 moment   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Integer( group_id ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( a ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( b ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Float( center ) );

    parameter = getParameter( 4 );
    parameter.setValue( new Integer( moment ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    int group_ID = ( (Integer)(getParameter(0).getValue()) ).intValue();
    int moment   = ( (Integer)(getParameter(4).getValue()) ).intValue();

    float a      = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float b      = ( (Float)(getParameter(2).getValue()) ).floatValue();
    float center = ( (Float)(getParameter(3).getValue()) ).floatValue();

                                     // get the current data set and do the 
                                     // operation
    DataSet ds = this.getDataSet();

    Data data = ds.getData_entry_with_id( group_ID );
    if ( data == null )
    {
      ErrorString message = new ErrorString( 
                          "ERROR: no data entry with the group_ID "+group_ID );
      System.out.println( message );
      return message;
    }
    else
    {
      float x_vals[] = data.getX_scale().getXs();
      float y_vals[] = data.getY_values();

      float result = NumericalAnalysis.HistogramMoment( x_vals, y_vals, 
                                                        a,      b, 
                                                        center,
                                                        moment     );
      return new Float( result );
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current CalculateMoment Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    CalculateMoment new_op    = new CalculateMoment( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}
