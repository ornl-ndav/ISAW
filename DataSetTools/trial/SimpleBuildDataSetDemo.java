/*
 *  @(#)  SimpleBuildDataSetDemo.java    1.0  2000/10/30    Dennis Mikkelson
 *
 */
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;

/**
  *  This class provides a basic demo of how to construct a DataSet with
  *  no operators and no attributes on the DataSet or its Data blocks.
  */  
public class SimpleBuildDataSetDemo
{
   public static final int   NUM_SINE_WAVES = 10;
   public static final int   NUM_SAMPLES    = 500;
   public static final float DELTA_X        = 0.002f;

  /**
   *  This method builds a simple DataSet with a collection of 10 sine waves.
   *  NO OPERATIONS are included with the DataSet.
   *
   *  @return  A sample DataSet with 10 sine waves.
   */
  public DataSet BuildDataSet()
  {
    // Use the DataSet constructor to make a DataSet with no operators.  The
    // constructor takes a title, log message and labels and units for the
    // x and y axes.

    DataSet new_ds = new DataSet( "Collection of Sine Waves",
                                  "Initial Version",
                                  "time",
                                  "milli-seconds",
                                  "signal level",
                                  "volts" );;

    Data    data;                               // data block that will hold 
                                                // info on one signal 
    float[] y_values = new float[NUM_SAMPLES];  // y-values for the signal 
    float[] x_values = new float[NUM_SAMPLES];  // x-values for the signal 
    XScale  x_scale;                            // time channels for the signal 

    // Build list of time channels for this id. Here, all the Data blocks have 
    // the same time channels.  For uniformly space points, we could have used
    // a UniformXScale. 

    for ( int channel = 0; channel < NUM_SAMPLES; channel++ )
      x_values[ channel ] = (float)(channel * DELTA_X * 2 * Math.PI);

    x_scale = new VariableXScale( x_values );

    // Now, for each id construct a Data object by evaluating a sine wave with
    // frequency dependent on the id and add the Data object to the DataSet.

    for ( int id = 1; id < 10; id++ ) 
    {
      for ( int channel = 0; channel < NUM_SAMPLES; channel++ )
        y_values[ channel ] = (float)Math.sin( id * x_values[channel] );

      data = new Data( x_scale, y_values, id ); 
      new_ds.addData_entry( data );
    }
   
    return new_ds; 
  }

  /* ---------------------------------------------------------------------- */
  /**
    *  The main program method for this object
    */
  public static void main(String args[])
  {
    SimpleBuildDataSetDemo demo_prog = 
                           new SimpleBuildDataSetDemo();// create the class

    DataSet test_ds = demo_prog.BuildDataSet();         // call the method to
                                                        // construct a DataSet

                                                        // create a viewer for
                                                        // the DataSet 
    ViewManager view_manager = new ViewManager( test_ds, IViewManager.IMAGE );
  } 

}
