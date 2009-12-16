/* 
 *  * File: DumpGrids.java
 * 
 * Copyright (C) 2009, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.EventList;

import java.util.*;
import java.io.*;

import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

/**
 * Write out the detector grid information from a NeXus file, in the
 * for required by the QMapper (.DetCal).
 */
public class DumpGrids
{
  
  /**
   * Dump out a file with Detector Position information from the specified
   * NeXus file.  The file that is written will have the name of the input
   * .NeXus file plus the extension ".grids".  This file has the form of
   * Detector Calibration file (.DetCal), but just has the geometry info
   * from the NeXus, and a T0_SHIFT value of 0.
   *
   * @param args    Array of command line arguments.
   *                args[0] must be the fully qualified name of the .nxs
   *                file that has the required geometry information.
   */
  public static void main( String args[] ) throws Exception
  {
    Vector<IDataGrid> grids = new Vector<IDataGrid>();
    float   initial_path = 0;
    boolean have_initial_path = false;

    if ( args.length <= 0 )
      throw new IllegalArgumentException(
                                   "First argument must be the file name");
    String filename = args[0];

    NexusRetriever nr = new NexusRetriever( filename );
//  nr.RetrieveSetUpInfo( null );

    int num_ds = nr.numDataSets();
    System.out.println("Number of DataSets = " + num_ds );

    for ( int i = 1; i < num_ds; i++ )
    {
      DataSet ds = nr.getDataSet( i );
      //System.out.println("DataSet " + i + " has title " + ds.getTitle() );
      if ( ds.getNum_entries() <= 0 )
        System.out.println("NO DATA ENTRIES IN " + ds.getTitle() );
      else
      {
        Data data = ds.getData_entry(0);

        if ( !have_initial_path )
        {
          float temp = AttrUtil.getInitialPath(data);
          if ( !Float.isNaN(temp) )
          {
            initial_path = temp;
            have_initial_path = true;
          }
        }

        PixelInfoList pil = AttrUtil.getPixelInfoList( data );
        if ( pil == null )
          System.out.println("NO PIXEL INFO IN " + ds.getTitle() ); 
        {
          IPixelInfo pi = pil.pixel(0);
          if ( pi == null )
            System.out.println("NULL PIXEL INFO IN " + ds.getTitle() );
          else
          {
            IDataGrid grid = pi.DataGrid();
            System.out.println("For DS : " + ds.getTitle() + 
                               " Got grid " + grid.ID() );
            grids.add(grid);
          }
        }
      }
    }

    nr.close();

    String outfilename = filename + ".grids";
    PrintStream out = new PrintStream( outfilename );

    out.println("#");
    out.println("# Detector Position Information Extracted From NeXus file: ");
    out.println("# " + filename );
    out.println("#");
    out.println("# Lengths are in centimeters."); 
    out.println("# Base and up give directions of unit vectors for a local");
    out.println("# x,y coordinate system on the face of the detector.");
    out.println("#");
    out.println("#");
    out.println("# " + (new Date()).toString() ); 
    out.println("6         L1     T0_SHIFT");
    out.printf ("7 %10.4f            0\n", initial_path * 100 );
    out.println("4 DETNUM  NROWS  NCOLS  WIDTH   HEIGHT   DEPTH   DETD   " +
                "CenterX   CenterY   CenterZ    BaseX    BaseY    BaseZ    " +
                "  UpX      UpY      UpZ" );

    for ( int i = 0; i < grids.size(); i++ )
      out.println( Peak_new_IO.GridString(grids.elementAt(i)) );
    out.close();

    System.exit(0);
  }

}
