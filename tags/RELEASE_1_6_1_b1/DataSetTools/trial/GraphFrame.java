/*
 * File:  GraphFrame.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2004/01/07 14:57:44  dennis
 * Initial version of utility class to pop up a ViewManager, starting
 * with the SelectedGraphView, given just arrays of x,y values and a title.
 *
 */

package DataSetTools.trial;

import javax.swing.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.*;

/**
 *  This class provides a very simple way to display arrays of x,y coordinates
 *  as a Graph in a ViewManager, which allows selecting different view types.  
 */
public class GraphFrame
{
  ViewManager vm = null; 

 /**
  * Construct a ViewManager starting as a SelectedGraphView, with the 
  * specified graph and title
  */  
  public GraphFrame( float[]  x_array, 
                     float[]  y_array, 
		     String   title   )
  {
    setData( x_array, y_array, title );
  }


 /**
  * Construct a ViewManager starting as a SelectedGraphView, with the 
  * specified graph and title, using a sequence of integers for the x scale.
  */
  public GraphFrame( float[]  y_array,
                     String   title   )
  {
    float x_array[] = new float[ y_array.length ];
    for ( int i = 0; i < x_array.length; i++ )
      x_array[i] = i;
    setData( x_array, y_array, title );
  }

  
 /**
  * Reset the data used for the ViewManager. 
  */ 
  public void setData(  float[] x_array, float[] y_array, String title )
  {
    DataSet ds = new DataSet( title, "Initial Version");
    ds.setX_units( "Test X Units" );
    ds.setX_label("Text X Label" );
    ds.setY_units( "Test Y Units" );
    ds.setY_label("Text Y Label" );

    XScale x_scale = new VariableXScale( x_array );
    Data slice = new FunctionTable( x_scale, y_array, 1 );
    ds.addData_entry( slice );
    ds.setSelectFlag( 0, true );
    if ( vm == null )
      vm = new ViewManager( ds, IViewManager.SELECTED_GRAPHS ); 
    else
      vm.setDataSet( ds );
  }
  
 /*
  * Testing purposes only
  */
  public static void main( String args[] )
  {
    int N_POINTS = 360;
    float x[] = new float[N_POINTS];
    float y[] = new float[N_POINTS];

    for ( int i = 0; i < N_POINTS; i++ )
    {
      x[i] = i;
      y[i] = (float)( Math.sin( i*Math.PI/180.0 ));
    } 

    GraphFrame gf = new GraphFrame( x, y, "Sine Function" );    
  }

}
