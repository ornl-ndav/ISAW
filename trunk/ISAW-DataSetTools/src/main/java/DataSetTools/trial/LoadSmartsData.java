/*
 * File:  LoadSmartsData.java
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
 * Revision 1.3  2005/08/24 19:45:53  dennis
 * Changed default path to detector info file.
 *
 * Revision 1.2  2005/08/02 13:59:11  dennis
 * Added code to pop up a Display3D.
 *
 * Revision 1.1  2005/07/19 22:02:04  dennis
 * This class contains a static method to load data from the SMARTS
 * instrument at LANSCE into a list of PhysicalArray3DList objects,
 * to demonstrate and test the new VirtualArray3D viewer.
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.ViewTools.Components.*;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import Operators.Generic.Load.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Displays.Display3D;
import gov.anl.ipns.Util.Sys.WindowShower;

/**
 *  This file contains a static method to load data from the SMARTS 
 *  instrument at LANSCE into a list of PhysicalArray3DList objects.
 */

public class LoadSmartsData
{

  public static PhysicalArray3DList[] getTOF_Histogram( String hdf_file, 
                                                        String detector_file )
  {
     Retriever retriever = new NexusRetriever( hdf_file );

     DataSet   ds = retriever.getDataSet(2);

     LoadUtil.LoadDetectorInfo( ds, detector_file );
 
     new ViewManager( ds, "3D View" );     // temporarily pop up a couple 
     new ViewManager( ds, "Image View" );  // of views for sanity test.

     ReferenceGrid grid[] = ReferenceGrid.MakeDataReferenceGrids( ds );
     IDataGrid     dgrid;
     System.out.println("Number of Data Grids = " + grid.length );

     PhysicalArray3DList[] det_data = new PhysicalArray3DList[ grid.length ];
     Data    d;
     for ( int i = 0; i < grid.length; i++ )
     {
        int      num_values = number_of_values( grid[i] ); 
        int      size       = grid[i].num_rows() * grid[i].num_cols();
        XScale   x_scale    = null;
        float    values[][] = new float[size][num_values];
        Vector3D points[]   = new Vector3D[ size ];
        Vector3D extents[]  = new Vector3D[size];
        Vector3D x_axes[]   = new Vector3D[size];
        Vector3D y_axes[]   = new Vector3D[size];
        int id    = 0;
        int index = 0;
        for ( int row = 1; row <= grid[i].num_rows(); row++ )
          for ( int col = 1; col <= grid[i].num_cols(); col++ )
          {
             d = grid[i].getData_entry(row,col);
             if ( d != null )
             {        
                x_scale = d.getX_scale();
                dgrid   = getDataGrid( d );    // for now we assume 1 Grid
                id      = dgrid.ID();
                points[index] = dgrid.position( row, col );
                extents[index] = new Vector3D( dgrid.width( row, col ),
                                               dgrid.height( row, col ),
                                               dgrid.depth( row, col ) );
                x_axes[index]  = dgrid.x_vec( row, col );
                y_axes[index]  = dgrid.y_vec( row, col );
                values[index]  = d.getY_values();
             }
             else            // what do we do with missing Data blocks???
             {
                System.out.println("ERROR: missing Data block in grid" ); 
             }
             index++;
          }
    
        det_data[i] = new PhysicalArray3DList( size, 
                                               points, 
                                               values, 
                                               extents,
                                               x_axes,
                                               y_axes,
                                               x_scale );
        det_data[i].setArrayID( id );
     } 

     return det_data;
  }


  private static IDataGrid getDataGrid( Data d )
  {
     PixelInfoList pil = AttrUtil.getPixelInfoList( d );
     return pil.pixel(0).DataGrid();
  }


  private static int number_of_values( ReferenceGrid detector )
  { 
    Data d;
    int  length;
    int  max_length = 0;
    for ( int row = 1; row <= detector.num_rows(); row++ )
      for ( int col = 1; col <= detector.num_cols(); col++ )
      {
         d = detector.getData_entry(row,col);
         if ( d != null )
         {
           length = d.getY_values().length;
           if ( length > max_length )
             max_length = length;
         }
       }
    return max_length;
  }


  public static void main( String args[] )
  {
    String data_path = "/usr2/LANSCE_DATA/smarts/";
    String hdf_file  = data_path + "/SMARTS_E2005004_R020983.nx.hdf";

    String detector_path = "/home/dennis/WORK/ISAW/InstrumentInfo/LANSCE/";
    String detector_file = detector_path + "smarts_detectors.dat";

    PhysicalArray3DList[] data = getTOF_Histogram( hdf_file, detector_file );
    System.out.println( "Number of  PhysicalArray3DLists found = " +
                         data.length );
    for ( int i = 0; i < data.length; i++ )
    {
       int      n_points         = data[i].getNumPoints();
       System.out.println( "Number of points = " + n_points );
       Vector3D extents[]        = data[i].getExtents(0,n_points-1);
       Vector3D x_orientations[] = data[i].getXOrientations(0,n_points-1);
       Vector3D y_orientations[] = data[i].getYOrientations(0,n_points-1);
       System.out.println( "lengths of bounds = " + extents.length + ", " 
                                                  + x_orientations.length + ", "
                                                  + y_orientations.length  );

       int n_frames = data[i].getNumFrames();
       System.out.println( "Number of frames = " + n_frames );

       System.out.println("position of pixel 0 " + data[i].getPoint(0) );
    }
    
    float vals[] = data[0].getValuesAtPoint(0);
    System.out.println("length of values = " + vals.length );
    for ( int i = 0; i < vals.length; i+=100 )
      System.out.println(" i, val = " + i + ", " + vals[i] );

    Display3D display = new Display3D( data, 0, Display3D.CTRL_ALL );    
    WindowShower.show(display);
  }
}
