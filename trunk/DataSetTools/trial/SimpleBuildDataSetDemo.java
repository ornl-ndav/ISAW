/*
 * File:  SimpleBuildDataSetDemo.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.5  2002/11/27 23:23:30  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/03/13 16:13:40  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.3  2002/02/28 19:57:46  dennis
 * Modified import statements due to operator reorganization.
 *
 */
import DataSetTools.dataset.*;
import DataSetTools.dataset.Data;
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

      data = Data.getInstance( x_scale, y_values, id ); 
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
