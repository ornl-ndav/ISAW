/*
 * File:  LoadGladData.java
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
 * Revision 1.4  2006/07/25 05:42:01  dennis
 * Modified main test program to test dataChanged()
 * method.
 *
 * Revision 1.3  2005/08/03 16:43:26  dennis
 * Minor fix of javadocs.
 *
 * Revision 1.2  2005/08/02 13:57:51  dennis
 * Removed unused import.
 *
 * Revision 1.1  2005/07/26 21:24:36  dennis
 * Demo code to load data from GLAD into a list of PhysicalArray3DList
 * objects.
 *
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
 *  This file contains a static method to load data from the GLAD 
 *  instrument at IPNS into a list of PhysicalArray3DList objects.
 *  It is just a quick proof of concept class for the PhysicalArray3DList
 *  and Display3D.
 */

public class LoadGladData
{

  public static PhysicalArray3DList[] getTOF_Histogram( String hdf_file,
                                                        String detector_file )
  {
     Retriever retriever = new RunfileRetriever( hdf_file );
     DataSet   ds = retriever.getDataSet(1);

     LoadUtil.Load_GLAD_LPSD_Info( ds, detector_file );

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
        int      size;
                                // count the number of live pixels and only
                                // get enough space for those.
        size = 0;
        for ( int row = 1; row <= grid[i].num_rows(); row++ )
          for ( int col = 1; col <= grid[i].num_cols(); col++ )
             if ( grid[i].getData_entry(row,col) != null )
               size++;

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
                index++;
             }
             else            // what do we do with missing Data blocks???
             {
                System.out.println("ERROR: missing Data block in grid" ); 
             }
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
    String data_path = "/usr2/ARGONNE_DATA/";
    String hdf_file  = data_path + "glad6942.run";

    String detector_path = "/home/dennis/WORK/ISAW/Databases/";
    String detector_file = detector_path + "gladdets6.par";

    PhysicalArray3DList[] data = getTOF_Histogram( hdf_file, detector_file );

    System.out.println( "Number of  PhysicalArray3DLists found = " +
                         data.length );
//
    PhysicalArray3D[] data_1 = new PhysicalArray3D[ data.length ];

    for ( int i = 0; i < data.length; i++ )
    {
      int n_points = data[i].getNumPoints();
      Vector3D extents[] = data[i].getExtents(0,n_points-1);
      Vector3D xaxes[]   = data[i].getXOrientations(0,n_points-1);
      Vector3D yaxes[]   = data[i].getYOrientations(0,n_points-1);
      Vector3D points[]  = data[i].getPoints();
      float    values[]  = data[i].getValuesAtFrame( 1000 );
      data_1[i] = new PhysicalArray3D( n_points, points, values,
                                       extents, xaxes, yaxes );
    }
    Display3D display = new Display3D( data_1, 0, Display3D.CTRL_ALL, false );
    WindowShower.show(display);

    for (int i = 0; i < 200; i++ )
    {
      try
      {
        Thread.sleep( 100 );
      }
      catch (Exception e)
      {}
      System.out.println("**** Changing data");

      float values[] = new float[100];
      for ( int j = 0; j < 100; j++ )
        values[j] = 200 + 2*j; 

      data_1[i].setValues( 0, 40, values );

      display.dataChanged();
    }
    System.out.println("Done changing");

    
//

/*
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

    Display3D display = new Display3D( data, 0, Display3D.CTRL_ALL, true );    
    WindowShower.show(display);
    for (int i = 0; i < 10; i++ )
    {
      try
      {
        Thread.sleep( 3000 );
      }
      catch (Exception e)
      {}
      System.out.println("**** Changing data");
      display.dataChanged( data );
    }
    System.out.println("Done changing");
*/
  }

}
