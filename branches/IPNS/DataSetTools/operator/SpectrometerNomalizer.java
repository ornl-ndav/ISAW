/*
 * @(#)SpectrometerNomalizer.java   0.1  99/07/23  Dongfeng Chen, Dennis Mikkelson
 *             
 * This operator Nomalizer all data objects in a data set by a monitor value.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import ChopTools.*;

/**
  *  Divide a data set by a constant scalar value 
  */

public class SpectrometerNomalizer extends    DataSetOperator 
                                 implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public SpectrometerNomalizer( )
  {
    super( "Scale Using Monitor1 DataSet" );

    Parameter parameter;
    
    parameter = new Parameter( "Adjuster", new Float(1000000.0) );
    addParameter( parameter );
    
    parameter = new Parameter( "Monitor DataSet", 
                              new DataSet("Monitor DataSet", "Empty DataSet"));
    addParameter( parameter );
    
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // The concrete operation extracts the
                                     // current value of the scale factor
                                     // parameter and returns the result of 
                                     // dividing by that scale factor.
  public Object getResult()
  {                                  // get the scale factor parameter 
    
     DataSet ds            = this.getDataSet(); 
     DataSet monitor_ds    = (DataSet)(getParameter(1).getValue());

     if ( !ds.SameUnits( monitor_ds )  )    // units don't match so something
     return null;                           // is wrong, just quit

     if ( !ds.getX_units().equalsIgnoreCase("Time(us)")  ||
         !ds.getY_units().equalsIgnoreCase("Counts") )      // wrong units, so
     return null;                                          // just quit

                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
     DataSet new_ds = (DataSet)ds.empty_clone(); 
     new_ds.addLog_entry( "Normalize Using " + monitor_ds );
     float nomalizer=chop_calibraton.intergratedPeak1Intensity(monitor_ds);
     System.out.println("Nomalizer is:"+ nomalizer);
                                            // now do the normalization 
     
     float scale = nomalizer/( (Float)(getParameter(0).getValue()) ).floatValue();
     System.out.println("Scale is:"+ scale);
     
                                     // get the current data set

    if ( scale != 0 )                // do the operation if possible
    {                                // otherwise return empty data set 
      int num_data = ds.getNum_entries();
      Data data,
           new_data;
      for ( int i = 0; i < num_data; i++ )
      {
        data = ds.getData_entry( i );       // get reference to the data entry
        new_data = data.divide( scale );    // divide by scale factor, assuming
                                            // 0 error in the scale factor.
        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerNomalizer Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerNomalizer new_op    = new SpectrometerNomalizer( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }



}
