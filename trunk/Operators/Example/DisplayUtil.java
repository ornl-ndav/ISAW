/*
 * File:  DisplayUtil.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.1  2005/06/20 15:19:11  dennis
 * Initial commit of file with several utility methods for
 * displaying one or more DataSets.
 *
 *
 */

package Operators.Example;

import java.util.*;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.trial.*;
 
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Displays.*;

/**
 *  This class contains some static methods that display one or more
 *  DataSets in the newer viewers.
 */

public class DisplayUtil 
{
  private DisplayUtil()
  {};


  /**
   *  This method will take a list of SCD DataSets which include sample
   *  orientation and detector position information, and display a 3D
   *  view of reciprocal space.
   *
   *  @param  data_sets   Vector containing one or more valid SCD DataSets
   *
   *  @param  threshold   Number of counts above which the bin will be be
   *                      counted as a peak
   */
  public static void Display_SCD_Reciprocal_Space( Vector data_sets, 
                                                   float  threshold )
  {
    String error = null;
    if ( data_sets == null )
    {
      error = "null Vector of DataSets in Show_SCD_Reciprocal_Space";
      throw ( new IllegalArgumentException( error ) );
    }

    int num_ds = 0;
    for ( int i = 0; i < data_sets.size(); i++ )
      if ( data_sets.elementAt(i) != null &&
           data_sets.elementAt(i) instanceof DataSet )
        num_ds++;

    if ( num_ds == 0 )
    {
      error = "no DataSets in Vector in Show_SCD_Reciprocal_Space";
      throw ( new IllegalArgumentException( error ) );
    }

    DataSet ds_array[] = new DataSet[num_ds];
    int index = 0;
    for ( int i = 0; i < data_sets.size(); i++ )
      if ( data_sets.elementAt(i) != null &&
           data_sets.elementAt(i) instanceof DataSet )
      {
        ds_array[index] = (DataSet)(data_sets.elementAt(i));
        index++;
      }

    new GL_RecipPlaneView( ds_array, (int)threshold );
  }


  /**
   *  This method generates a display of the Data blocks of a DataSet as
   *  one large image.
   *
   *  @param  ds   The DataSet whose Data blocks will be used to form the
   *               image.
   */
  public static void DisplayAsImage( DataSet ds )
  {
    String error = null;
    if ( ds == null )
    {
      error = "DataSet is null in DisplayAsImage";
      throw ( new IllegalArgumentException( error ) );
    }
                                              // make a huge virtual array
                                              // to hold all of the spectra
                                              // as rows of the iamge
    int n_groups = ds.getNum_entries();
    int n_times  = ds.getData_entry(0).getY_values().length;
    XScale x_scale = ds.getData_entry(0).getX_scale();

    IVirtualArray2D va2D = new VirtualArray2D( n_groups, n_times );
    va2D.setAxisInfo(AxisInfo.X_AXIS, x_scale.getStart_x(), x_scale.getEnd_x(),
                      "Time-of-Flight","microseconds", AxisInfo.LINEAR );
    va2D.setAxisInfo( AxisInfo.Y_AXIS, 1f, n_groups,
                        "Group ID","", AxisInfo.LINEAR );
    va2D.setTitle( ds.toString() );
                                               // copy the data to the 
                                               // virtual array
    for ( int i = 0; i < n_groups; i++ )
    {
      float[] ys = ds.getData_entry(i).getY_values();
      va2D.setRowValues( ys, i, 0 );
    }
                                               // give the data to a display
    Display2D display = new Display2D( va2D,
                                       Display2D.IMAGE,
                                       Display2D.CTRL_ALL);
    WindowShower.show(display);
  }


  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
                                              // load the file as best we can

    String file_name = "/usr2/LANSCE_DATA/SCD/SCD_E000005_R000053.nx.hdf";
    Retriever retriever = new NexusRetriever( file_name );
    DataSet ds = retriever.getDataSet(3);

                                              // fix the data 
    float det_width  = 0.20f; 
    float det_height = 0.20f; 
    float det_dist   = 0.45f;
    float length_0   = 9.00f;
    ds = LansceUtil.FixSCD_Data( ds, 
                                 1500, 8000, 
                                 det_width, det_height, 
                                 det_dist,
                                 length_0 ); 
                                              // make a huge virtual array
                                              // to hold all of the spectra
                                              // as rows of the iamge

    DisplayAsImage( ds );

    Vector ds_list = new Vector();
    ds_list.addElement( ds ); 
    Display_SCD_Reciprocal_Space( ds_list, 20 );

    new ViewManager( ds, "3D View" );
  }
}
