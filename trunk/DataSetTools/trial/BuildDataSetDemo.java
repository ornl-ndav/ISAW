/*
 * File:  BuildDataSetDemo.java 
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2001/04/26 15:28:15  dennis
 * Added copyright and GPL info at the start of the file.
 *
 *
 */
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;

/**
  *  This class provides a basic demo of how to construct a DataSet.
  */  
public class BuildDataSetDemo
{

  /**
   *  This method builds a simple DataSet with a collection of 10 sine waves.
   *
   *  @return  A sample DataSet with 10 sine waves.
   */
  public DataSet BuildDataSet()
  {
   //
   // 1. Use a "factory" to construct a DataSet with operators ---------------
   // 
    DataSetFactory factory = new DataSetFactory( "Collection of Sine Waves",
                                                 "time",
                                                 "milli-seconds",
                                                 "signal level",
                                                 "volts" );
    DataSet new_ds = factory.getDataSet();

    //
    // 2. Add attributes, as needed to the DataSet ---------------------------
    //
    new_ds.setAttribute( new StringAttribute( Attribute.FILE_NAME, 
                                             "BuildDataSetDemo.java" ) );
    new_ds.setAttribute( new IntAttribute(Attribute.NUMBER_OF_PULSES, 10000) );
    
    //
    // Now, repeatedly construct and add Data blocks to the DataSet
    //
    Data          data;         // data block that will hold info on one signal 
    float[]       y_values;     // array to hold the y-values for that signal 
    XScale        x_scale;      // "time channels" for the signal 

    for ( int id = 1; id < 10; id++ )            // for each id
    {
      // 
      // 3. Construct a Data object
      //
      x_scale = new UniformXScale( 1, 5, 50 );   // build list of time channels

      y_values = new float[50];                  // build list of counts
      for ( int channel = 0; channel < 50; channel++ )
        y_values[ channel ] = 100*(float)Math.sin( id * channel / 10.0 );

      data = new Data( x_scale, y_values, id ); 

      //
      // 4. Add attributes as needed to the Data block
      //
                                                // "simple" energy in attribute
      data.setAttribute( new FloatAttribute( Attribute.ENERGY_IN, 120.0f ) );

                                               // more complicated, position
                                               // attribute has a position 
                                               // object as it's value
      DetectorPosition position = new DetectorPosition();
      float angle      = 50.0f * (float)(Math.PI / 180.0);
      float final_path = 4.0f;
      float height     = 0.1f;
      position.setCylindricalCoords( final_path, angle, height );
      data.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS, 
                                              position ) );

      //
      // 5. Add the Data object to the DataSet
      //
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
    BuildDataSetDemo demo_prog = new BuildDataSetDemo();// create the class

    DataSet test_ds = demo_prog.BuildDataSet();         // call the method to
                                                        // construct a DataSet

                                                        // create a viewer for
                                                        // the DataSet 
    ViewManager view_manager = new ViewManager( test_ds, IViewManager.IMAGE );
  } 

}
