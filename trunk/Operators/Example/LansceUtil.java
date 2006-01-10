/*
 * File:  LansceUtil.java
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
 * Revision 1.7  2006/01/10 17:40:36  dennis
 * Fixed row vs. column ordering in method to "fix" the
 * LANSCE SCD data.  The order is now known to be correct
 * and was verified using run SCD_E000005_R000781.nx.hdf
 * which is a flood pattern with a rectangular mask in
 * the upper left corner and a circular mask in the
 * lower right corner (when looking at the detector
 * from the sample position).
 *
 * Revision 1.6  2005/08/14 21:44:47  dennis
 * Now adds SampleOrientation to each Data block.
 *
 * Revision 1.5  2005/08/11 21:50:29  dennis
 * Expanded main program to make it easier to view multiple runs
 * in 3D for testing purposes.  Phi, chi & omega values for LANCE
 * SCD are are still not interpreted properly.
 *
 * Revision 1.4  2005/08/10 15:52:28  dennis
 * Swapped rows and cols, hopefully to the order used by LANSCE.
 * Went back to default interpretation of phi, chi & omega
 * rotation angles.
 * NOTE: This still does not give consistent results from
 * multiple runs.
 *
 * Revision 1.3  2005/08/10 15:02:16  dennis
 * Added test code to load three LANSCE SCD runs and put them
 * in the 3D Reciporcal Lattice view, in an attempt to verify
 * that the values of phi, chi and omega are being interpreted
 * correctly.
 *
 * Revision 1.2  2005/06/20 15:50:29  dennis
 * Minor reformatting.
 *
 * Revision 1.1  2005/06/20 03:16:29  dennis
 * Initial checkin.  Currently just contains method to fix
 * Lansce SCD DataSets, which are ordered by column and don't
 * have detector position informaiton.
 *
 */

package Operators.Example;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.instruments.*;
import DataSetTools.trial.*;
 
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Displays.*;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class contains a static method for converting a DataSet from the
 *  LANSCE SCD to a form that can be used by ISAW.
 */

public class LansceUtil 
{
  private LansceUtil()
  {};

  /**
   *  Rearrange the data from the SCD at LANSCE and add detector position
   *  information to the file.
   *
   *  @param  ds         LANSCE SCD DataSet where each Data block holds Data 
   *                     from one column of pixels on the area detector, at one 
   *                     time-of-flight
   *
   *  @param  t_min      Minimum time-of-flight for this data
   *
   *  @param  t_max      Maximum time-of-flight for this data
   *
   *  @param  det_width  Detector width in meters
   *
   *  @param  det_height Detector width in meters
   *
   *  @param  det_dist   Sample to detector distance
   *
   *  @param  length_0   Initial flight path length
   *
   *  @return A new DataSet with each Data block corresponding to the
   *          time-of-flight spectrum for one pixel on the area detector.
   */
  public static DataSet FixSCD_Data( DataSet ds, 
                                     float t_min,     float t_max,
                                     float det_width, float det_height,
                                     float det_dist,
                                     float length_0 )
  {
    System.out.println(ds);
    System.out.println("x label = " + ds.getX_label() );
    System.out.println("x units = " + ds.getX_units() );
    System.out.println("x range = " + ds.getXRange() );
    System.out.println("y label = " + ds.getY_label() );
    System.out.println("y units = " + ds.getY_units() );
    System.out.println("y range = " + ds.getYRange() );
                        // assume data comes in as sequences of values from
                        // columns of the area detector, starting with column 1
                        // column 2, etc of the first time slice.
    int N_PAGES = 325;
    int N_COLS  = 256;
    int N_ROWS  = 256; 
    int AREA_DET_ID = 5;

    float counts[][][] = new float[N_PAGES][N_COLS][N_ROWS];

    String error = null;
    if ( ds.getNum_entries() != N_COLS * N_PAGES )
    {
      error = "Need DataSet with " + (N_COLS * N_PAGES) + " entries " + 
              "got " + ds.getNum_entries(); 
      throw ( new IllegalArgumentException( error ) );  
    }
                       // first, extract all of the data values into a 3D array
    int index = 0;
    float ys[];
    for ( int page = 0; page < N_PAGES; page++ )
      for ( int col = 0; col < N_COLS; col++ )
      {
         Data d = ds.getData_entry( index );
         index++;

         ys = d.getY_values();
         if ( ys.length != N_ROWS )
         {
           error = "Need Data blocks with " + N_ROWS + " y-values " + 
              "got " + ys.length; 
           throw ( new IllegalArgumentException( error ) );  
         } 
         counts[page][col] = ys;
      }
                                       // now form a new DataSet by extracting 
                                       // the values in the 3D array of counts
                                       // in a different order, omitting edge 
                                       // pixels
    DataSetFactory factory = new DataSetFactory( ds.getTitle() );
    DataSet new_ds = factory.getTofDataSet( InstrumentType.TOF_SCD );
    XScale x_scale = new UniformXScale( t_min, t_max, N_PAGES + 1 );
    index = 0;
    for ( int row = 0; row < N_ROWS; row++ )
      for ( int col = 0; col < N_COLS; col++ )
      {
        ys = new float[N_PAGES];
        if ( row > 0 && col > 0 && row < N_ROWS-1 && col < N_COLS-1 )
          for ( int page = 0; page < N_PAGES; page++ )
            ys[page] = counts[page][col][row];

        index++; 

        Data d = new HistogramTable( x_scale, ys, index );
        new_ds.addData_entry( d );
      }
                                      // set some basic attributes
                                      // set initial path attribute after
                                      // converting to positive value in meters
    Attribute run_num = ds.getAttribute("Run Number");

    FloatAttribute initial_path = new FloatAttribute("Initial Path", length_0 );
    new_ds.setAttribute( initial_path );
    int num_data = new_ds.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
       new_ds.getData_entry(i).setAttribute( initial_path );
       new_ds.getData_entry(i).setAttribute( run_num );
    }

                                      // Add the detector position info.
                                      // First add a Data grid for the detector
    float     depth  =  0.001f;
//  float     center_x = 0.0f;
    float     center_x = -0.05f;
    float     center_y = (float)(-det_dist / Math.sqrt(2));
    float     center_z = (float)( det_dist / Math.sqrt(2));
    Vector3D  center =  new Vector3D(  center_x, center_y, center_z );
    Vector3D  x_vec  =  new Vector3D( -1,      0f,      0f );
    Vector3D  y_vec  =  new Vector3D(  0,  .7071f,  .7071f );
    IDataGrid grid   = new UniformGrid( AREA_DET_ID, "m", 
                                        center, x_vec, y_vec,
                                        det_width, det_height, depth,
                                        N_ROWS, N_COLS );
                       
                                      // Next add the pixel info to each 
                                      // Data block
    IPixelInfo              list[];
    PixelInfoList           pil;
    PixelInfoListAttribute  pil_attr;
    int seg_id = 0;                                       
    for ( int row = 0; row < N_ROWS; row++ )
      for ( int col = 0; col < N_COLS; col++ )
      {
        list     = new IPixelInfo[1];
        list[0]  = new DetectorPixelInfo( seg_id, 
                                         (short)(row+1), (short)(col+1),grid);
        pil      = new PixelInfoList( list );
        pil_attr = new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST, pil );
        new_ds.getData_entry( seg_id ).setAttribute( pil_attr );
        seg_id++;
      }
                                      // finally, use some utilities to set
                                      // references to the Data blocks to the
                                      // DataGrid, and to fill out 
                                      // position info for each pixel 
    grid.setData_entries( new_ds );
    Grid_util.setEffectivePositions( new_ds, AREA_DET_ID );

                                      // Add sample orientation information
    float phi   = 0;
    float chi   = -135;
    float omega = 0;

    SampleOrientation orientation = 
                                new IPNS_SCD_SampleOrientation(phi,chi,omega);
    Attribute orientation_attr = new SampleOrientationAttribute(
                                        Attribute.SAMPLE_ORIENTATION, 
                                        orientation );
    new_ds.setAttribute( orientation_attr );
    for ( int i = 0; i < new_ds.getNum_entries(); i++ )
      new_ds.getData_entry(i).setAttribute( orientation_attr );

    System.out.println("**** just set sample orientation");
    return new_ds;
  }

  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
//    String prefix = "SCD_E000005_R0000";
    String prefix = "SCD_E000005_R000";
    String suffix = ".nx.hdf";

    // NOVEMBER LANSCE RUNS, detector distance 0.265 meter
    int START  = 0;
    int N_RUNS = 5;
    float det_dist = 0.265f;
//  float det_dist = 0.275f;
    int run_[]   = {  725,  726,  727,  728,  729,  730,  731,  732,  733 };
    int omega_[] = {  125,   90,   60,   85,   72,  108,   35,  100,   78 };
    int phi_[]   = {  320,  335,    0,   10,   42,   50,    0,  300,  290 };
    int chi_[]   = { -120, -120, -120, -120, -120, -120, -120, -120, -120 };
//  int chi_[]   = { +120, +120, +120, +120, +120, +120, +120, +120, +120 };
/*
    // NOVEMBER LANSCE RUNS, detector distance 0.465 meter
    int N_RUNS = 9;
    int run_[]   = {  783,  784,  785,  786,  787,  788,  789,  790,  791 };
    int omega_[] = {  125,   90,   60,   85,   72,  108,   35,  100,   78 };
    int phi_[]   = {  320,  335,    0,   10,   42,   50,    0,  300,  290 };
    int chi_[]   = { +120, +120, +120, +120, +120, +120, +120, +120, +120 };
*/
/*
    int run_[]   = {  96,    98,   97,   95,   94,   92 };
    int phi_[]   = {  245,  290,  290,  245,  325,  325 };
    int chi_[]   = { -135, -135, -135, -135, -135, -135 };
    int omega_[] = {   90,  135,   90,  135,  135,   90 };
*/
/*
    int run_[]   = {  96, 96, 96, 96 };
    int phi_[]   = {   0,    0,   0,   245 };
    int chi_[]   = {   0,    0, -135, -135 };
    int omega_[] = {   0,   90,   90,   90 };
*/
    DataSet ds[] = new DataSet[ run_.length ];
                                              
//  String dir_name  = "/home/dennis/LANSCE_DATA/RUBY/";
    String dir_name  = "/home/dennis/LANSCE_1_9_06/RUBY_11_x_05/";
    String file_name;
    Retriever retriever;

                                              // fix the data 
//    float det_width  = 0.20f; 
//    float det_height = 0.20f; 
    float det_width  = 0.192f; 
    float det_height = 0.192f; 
    float length_0   = 7.499858f;
    float phi,
          chi,
          omega;
    float phi_offset   = 0;                   // set these to adjust for 
    float chi_offset   = 0;                   // different zero positions
    float omega_offset = 0;

    float phi_sign   = 1;                     // set these to +-1 to change
    float chi_sign   = 1;                     // the direction of rotation
    float omega_sign = 1;
    for ( int i = START; i < START+N_RUNS; i++ )
    {
      file_name = dir_name + prefix + run_[i] + suffix;
      retriever = new NexusRetriever( file_name );
      ds[i-START] = retriever.getDataSet(3);

      ds[i-START] = FixSCD_Data( ds[i-START], 
                         1500, 8000, 
                         det_width, det_height, 
                         det_dist,
                         length_0 ); 

      phi   = phi_sign   * phi_[i]   + phi_offset;
      chi   = chi_sign   * chi_[i]   + chi_offset;
      omega = omega_sign * omega_[i] + omega_offset;

      System.out.println("-----------------------------------");
      System.out.println(" run = " + run_[i] + 
                         ", phi = " + phi +
                         ", chi = " + chi +
                         ", omega = " + omega );

      SampleOrientation samp_or = 
                           new LANSCE_SCD_SampleOrientation( phi, chi, omega );
      SampleOrientationAttribute attr =
        new SampleOrientationAttribute( Attribute.SAMPLE_ORIENTATION, samp_or );

      ds[i-START].setAttribute( attr );

      for ( int db_index = 0; db_index < ds[i-START].getNum_entries(); db_index++ )
        ds[i-START].getData_entry(db_index).setAttribute( attr );
    }

/*
                                              // make a huge virtual array
                                              // to hold all of the spectra
                                              // as rows of the iamge
    int n_groups = ds1.getNum_entries();
    int n_times  = ds1.getData_entry(0).getY_values().length;
    XScale x_scale = ds1.getData_entry(0).getX_scale();

    IVirtualArray2D va2D = new VirtualArray2D( n_groups, n_times );
    va2D.setAxisInfo(AxisInfo.X_AXIS, x_scale.getStart_x(), x_scale.getEnd_x(),
                      "Time-of-Flight","microseconds", AxisInfo.LINEAR );
    va2D.setAxisInfo( AxisInfo.Y_AXIS, 1f, n_groups,
                        "Group ID","", AxisInfo.LINEAR );
    va2D.setTitle(file_name);
                                               // copy the data to the 
                                               // virtual array
    for ( int i = 0; i < n_groups; i++ )
    {
      float[] ys = ds1.getData_entry(i).getY_values();
      va2D.setRowValues( ys, i, 0 );
    }
                                               // give the data to a display
    Display2D display = new Display2D( va2D, 
                                       Display2D.IMAGE,
                                       Display2D.CTRL_ALL);
*/
    DataSet ds_arr[] = new DataSet[ N_RUNS ];
    for ( int i = 0; i < N_RUNS; i++ )
      ds_arr[i] = ds[i];
    new GL_RecipPlaneView( ds_arr, 100 );

//  new ViewManager( ds1, "3D View" );

//    WindowShower.show(display);
  }
}
