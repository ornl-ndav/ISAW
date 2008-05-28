/* 
 * File: SNSDetCal.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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

package Operators.Special;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Scanner;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import DataSetTools.dataset.AttrUtil;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.DetPosAttribute;
import DataSetTools.dataset.DetectorPixelInfo;
import DataSetTools.dataset.Grid_util;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.IPixelInfo;
import DataSetTools.dataset.PixelInfoList;
import DataSetTools.dataset.PixelInfoListAttribute;
import DataSetTools.dataset.UniformGrid;
import DataSetTools.retriever.RunfileRetriever;

/**
 * This class applies detector grid information from SNS .DetCal files 
 * to update the effective pixel position and data grid information in
 * a DataSet.  The DataSet is assumed to have data from only one detector
 * and that detector is assumed to be listed in the supplied .DetCal
 * file.
 */
public class SNSDetCal
{
   /**
    * Apply the area detector calibration information from the specified file
    * to the specified DataSet.  Assuming that the file contains calibration
    * information for all of the area detectors in the DataSet, all of the
    * UniformGrids describing detectors will be changed to match the
    * information from the calibration file.  The effective positions of
    * all pixels will also be adjusted based on the calibration information.
    * The calibration file must contain the detector position information 
    * in the form of line types 4 and 5 listed at the top of the new 
    * (SNS style) peaks file.
    *
    * @param  ds        The DataSet that is to be calibrated.
    * @param  file_name The name of the file with the calibration information
    * @param  debug     If true, print the data grid information from the
    *                   calibration file AND from the DataSet's grids after
    *                   applying the calibration.
    * @return An error string if something went wrong, or null if the
    *         calibration succeeded.
    */
   public static Object ApplySNSDetectorCalibration( DataSet ds, 
                                                     String  file_name,
                                                     boolean debug ) 
   {
     if ( ds == null )
       return new ErrorString("ds is null in ApplySNSDetectorCalibration");
     
     int[] ids = Grid_util.getAreaGridIDs( ds );
     if ( ids == null || ids.length == 0 )
       return new ErrorString("NO area grids in ApplySNSDetectorCalibration");
         
     Hashtable grids = new Hashtable();
     
     try
     {
       FileReader f_in = new FileReader( file_name );
       BufferedReader buff = new BufferedReader( f_in );
       Scanner scanner = new Scanner( buff );
       
       while ( !scanner.next().equals("5") )    // skip to line type 5
         scanner.nextLine();

       boolean more_grids = true;
       while ( more_grids )
       {
          int id = scanner.nextInt();
          int nrows    = scanner.nextInt();
          int ncols    = scanner.nextInt();
          float width  = scanner.nextFloat()/100; // file values in cm
          float height = scanner.nextFloat()/100;
          float depth  = scanner.nextFloat()/100;
          scanner.next(); // skip the redundant DETD
          float center_x = scanner.nextFloat()/100;
          float center_y = scanner.nextFloat()/100;
          float center_z = scanner.nextFloat()/100;
          float base_x   = scanner.nextFloat();
          float base_y   = scanner.nextFloat();
          float base_z   = scanner.nextFloat();
          float up_x     = scanner.nextFloat();
          float up_y     = scanner.nextFloat();
          float up_z     = scanner.nextFloat();
          Vector3D center = new Vector3D( center_x, center_y, center_z );
          Vector3D base   = new Vector3D( base_x, base_y, base_z );
          Vector3D up     = new Vector3D( up_x, up_y, up_z );
          IDataGrid grid = new UniformGrid( id, "m",
                                            center, base, up,
                                            width, height, depth,
                                            nrows, ncols );
          grids.put( new Integer(id), grid );
          if ( scanner.hasNext() )
          {  
            if ( !scanner.next().equals("5") )
              more_grids = false;
          }
          else
            more_grids = false;
          
          if ( debug )
        	System.out.println("CALIBRATED GRID " + grid);
       }
       f_in.close();
     }
     catch ( IOException exception )
     {
       return new ErrorString( "Error reading calibration file " + file_name);
     }
/*     
     for ( int i = 0; i < ids.length; i++ )
     { 
       UniformGrid raw_grid = (UniformGrid)Grid_util.getAreaGrid( ds, ids[i] );
       
       IDataGrid calib_grid = (IDataGrid)grids.get( raw_grid.ID() );
       if ( calib_grid != null )
       {
         raw_grid.setWidth(  calib_grid.width() );
         raw_grid.setHeight( calib_grid.height() );
         raw_grid.setDepth( calib_grid.depth() );
         raw_grid.setCenter( calib_grid.position() );
         raw_grid.setOrientation( calib_grid.x_vec(), calib_grid.y_vec() );
         Grid_util.setEffectivePositions( ds, raw_grid.ID() );
       }
       else
         return new ErrorString( "NO Calibration information for detector " 
                                  + ids[i]);
     }
*/     
     // replace all of the grids in all of the pixel info lists with with
     // one of the new calibrated uniform grids, and set the effective
     // position of each pixel.
     Data             data;
     PixelInfoList    old_pil;
     PixelInfoList    new_pil;
     IPixelInfo       old_pi;
     int              pixel_id;
     int              grid_id;
     short            row;
     short            col;
     IDataGrid        grid;
     DetectorPosition pos;
     int num_data = ds.getNum_entries();
     for ( int i = 0; i < num_data; i++ )
     {
       data     = ds.getData_entry( i );
       old_pil  = AttrUtil.getPixelInfoList( data );
       old_pi   = old_pil.pixel( 0 );
       pixel_id = old_pi.ID();
       grid_id  = old_pi.gridID();
       row      = (short)old_pi.row();
       col      = (short)old_pi.col();
       grid     = (IDataGrid)grids.get( grid_id );
       new_pil  = 
         new PixelInfoList(new DetectorPixelInfo(pixel_id, row, col, grid)); 

       data.setAttribute(
            new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST, new_pil) );

       pos = new DetectorPosition( grid.position( row, col ) );
       data.setAttribute( new DetPosAttribute(Attribute.DETECTOR_POS, pos) );
     } 

     for ( int i = 0; i < ids.length; i++ )   // NOTE: This is inefficient
     {
        UniformGrid new_grid = (UniformGrid)grids.get( ids[i] );
        new_grid.setData_entries( ds );
     }
     
     if ( debug )
       for ( int i = 0; i < ids.length; i++ )
       { 
         UniformGrid raw_grid = (UniformGrid)Grid_util.getAreaGrid(ds, ids[i]);
         System.out.println( "DataSet GRID " + raw_grid );
       }
     return null;
   }
   
   /**
    *  Crude functionality test for the ApplySNSDetectorCalibration method.
    */
   public static void main( String args[] )
   {
     String run_file = "/usr2/SCD_TEST/scd08336.run";
     String calib_file = "/usr2/SNS_SCD_TEST/IPNS_8336-7.DetCal";
     RunfileRetriever rr = new RunfileRetriever( run_file );
     DataSet ds = rr.getDataSet( 2 );
     ApplySNSDetectorCalibration( ds, calib_file, true );
   }
}
