/*
 * @(#)Integrate.java   0.1  99/07/26   Dennis Mikkelson
 *             
 * This operator integrates a selected Data block over a specified inteval and
 * returns a single real value.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
  *  Integrate a selected Data block over a specified inteval. 
  */

public class  Integrate  extends    DataSetOperator 
                         implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public Integrate( )
  {
    super( "Integrate a Group" );

    Parameter parameter = new Parameter("Group ID to Integrate",new Integer(0));
    addParameter( parameter );

    parameter = new Parameter("Left end point (a)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Right end point (b)", new Float(0));
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    int group_ID = ( (Integer)(getParameter(0).getValue()) ).intValue();

    float a = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float b = ( (Float)(getParameter(2).getValue()) ).floatValue();

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

      float result = NumericalAnalysis.IntegrateHistogram(x_vals, y_vals, a, b);

      System.out.println("Integral = " + result );    
      return new Float( result );  
    }
  }  

 /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Integrate Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    Integrate new_op    = new Integrate( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }



}
