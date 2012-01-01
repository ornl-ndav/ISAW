/* 
 * File: MakeTopazDetectors.java
 *
 * Copyright (C) 2009-2011, Dennis Mikkelson
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
 * Last Modified:
 * 
 * $Author: eu7 $
 * $Date: 2009-09-04 11:53:49 -0500 (Fri, 04 Sep 2009) $            
 * $Revision: 19982 $
 */

package EventTools.EventList;

import java.util.*;
import java.io.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 * Class to generate detector placement information for TOPAZ.
 * This version supports arbitrary detector numbering and placement, as
 * specified in arrays in the method getTOPAZ_Detectors.  The translation and
 * rotations that position and orient a detector are specified in the IPNS 
 * coordinate system.  The resulting detector information is converted to SNS 
 * coordinates in the lower-level Peak_new_IO class. 
 */
public class MakeTopazDetectors
{

  /**
   *  Rotate the specified detector grid by the specified angle around
   *  the specified axis.
   *  @param grid   The detector grid being rotated 
   *  @param angle  The rotation angle in degrees, following the right hand
   *                rule
   *  @param axis   The axis of rotation.
   */ 
  private static void Rotate( UniformGrid grid, float angle, Vector3D axis )
  {
    Vector3D x_vec  = grid.x_vec(); 
    Vector3D y_vec  = grid.y_vec(); 
    Vector3D center = grid.position(); 

    Tran3D tran = new Tran3D();
    tran.setRotation( angle, axis );

    tran.apply_to( x_vec, x_vec );
    tran.apply_to( y_vec, y_vec );
    tran.apply_to( center, center );

    grid.setOrientation( x_vec, y_vec );
    grid.setCenter( center );
  }


  /**
   *  Shift the specified detector grid by the specified translation vector.
   *  @param grid         The detector grid being shifted
   *  @param translation  The vector describing the amount the detector will 
   *                      be shifted.
   */
  private static void Translate( UniformGrid grid, Vector3D translation )
  {
    Vector3D center = grid.position();

    Tran3D tran = new Tran3D();
    tran.setTranslation( translation );

    tran.apply_to( center, center );

    grid.setCenter( center );
  }


  /**
   *  Print out the information about each of the detector grids to the
   *  specified file in the .DetCal format.
   *  @param grids     Vector of detector grid objects.
   *  @param filename  Name of DetCal file to write.
   */
  private static void PrintGrids( Vector<UniformGrid> grids, String filename )
               throws IOException
  {
    System.out.println("Printing " + grids.size() + 
                       " Grids to File: " + filename);

    PrintStream out = new PrintStream( filename );
    out.println("# NEW CALIBRATION FILE FORMAT (in NeXus/SNS coordinates):");
    out.println("# Lengths are in centimeters.");
    out.println("# Base and up give directions of unit vectors for a local");
    out.println("# x,y coordinate system on the face of the detector.");
    out.println("#");
    out.println("# " + (new Date()).toString() );
    out.println("#");
    out.println("6         L1     T0_SHIFT");
    out.println("7    1800.00        0.000");
    out.println("4 DETNUM  NROWS  NCOLS   WIDTH  HEIGHT   DEPTH   DETD   CenterX   CenterY   CenterZ    BaseX    BaseY    BaseZ      UpX      UpY      UpZ");

    for ( int i = 0; i < grids.size(); i++ )
      out.println( Peak_new_IO.GridString(grids.elementAt(i)) );
    out.close();
  }


/**
 * Get the list of detector grids for the TOPAZ configuration specified in
 * the constants and arrays of this method.
 * NOTE: Internally, we need to use IPNS coordinates and the IO code
 * will translate this to SNS/NeXus coordinates. 
 */
  private static Vector<UniformGrid> getTOPAZ_Detectors()
  {
    float DET_WIDTH  = 0.15819f;
    float DET_HEIGHT = 0.15819f;
    float DET_DEPTH  = 0.002f;
    int   N_ROWS     = 256;
    int   N_COLS     = 256;

    Vector<UniformGrid> grids = new Vector<UniformGrid>();

    Vector3D center    = new Vector3D(0,  0, 0);
    Vector3D base_vec  = new Vector3D(0, -1, 0);
    Vector3D up_vec    = new Vector3D(0,  0, 1);

    Vector3D vertical_axis   = new Vector3D( 0, 0, 1 );
    Vector3D beam_axis       = new Vector3D( 1, 0, 0 );
    Vector3D horiz_perp_axis = new Vector3D( 0, 1, 0 );
    Vector3D translation;

    int[]   ids  = { 17, 18, 
                     22, 23, 26, 27,
                     33, 36, 37, 38, 39,
                     46, 47, 48, 49,
                     57, 58 };

    // NOTE: The Omega angle called theta in the TOPAZ documentation
    float[] omega = {-1.884960f, -1.25664f,
                      1.570800f,  2.19911f, -2.199120f, -1.570800f,
                      1.884960f, -2.51327f, -1.884960f, -1.256640f, -0.628319f,
                     -2.199120f, -1.57080f, -0.942478f, -0.314159f,
                     -1.884960f, -1.25664f };

    float[] chi  = { 0.558505f,  0.558505f,
                     0.279253f,  0.279253f,  0.279253f,  0.279253f,
                     0,          0,          0,          0,         0, 
                    -0.279253f, -0.279253f, -0.279253f, -0.279253f,
                    -0.558505f, -0.558505f };

    float[] dist = { 45.5f, 45.5f,
                     42.5f, 42.5f, 42.5f, 42.5f,
                     39.5f, 39.5f, 39.5f, 39.5f, 39.5f,
                     42.5f, 42.5f, 42.5f, 42.5f,
                     45.5f, 45.5f };

    
    for ( int i = 0; i < chi.length; i++ )
    {
      UniformGrid grid = new UniformGrid( ids[i], "m",
                                          center, base_vec, up_vec,
                                          DET_WIDTH, DET_HEIGHT, DET_DEPTH,
                                          N_ROWS, N_COLS );

      // NOTES: 1. Grid radius must be in meters
      //        2. Rotation by -45 degrees about places 1,1 pixel at lower 
      //           corner of detector with x (column numbers) increasing up
      //           to the right from the point of view of the sample.
      //        3. Second rotation by chi about the horiz_perp_axis, "raises" 
      //           the detector above the plane and tilts the detector
      //           by the required amount.
      //        4. Third rotation by omega, rotates about the vertical axis to
      //           place the detector where it belongs.
      //        5. The rotations MUST be done in this order.

      translation = new Vector3D( dist[i]/100, 0, 0 );
      Translate( grid, translation );

      Rotate( grid, -45, beam_axis );

      float chi_angle = (float)(chi[i] * 180 / Math.PI);
      Rotate( grid, chi_angle, horiz_perp_axis );

      float omega_angle = (float)(omega[i] * 180 / Math.PI);
      Rotate( grid, omega_angle, vertical_axis );

      grids.add( grid );
    }

    return grids;
  }


  private static Vector<UniformGrid> getTOPAZ_Detectors( String file_name )
          throws IOException
  {
    Scanner sc = new Scanner( new File(file_name) );
    float  width;
    float  height;
    float  depth;
    int    n_rows;
    int    n_cols;
    int    id;
    float  omega;
    float  chi;
    float  dist;
    Vector<UniformGrid> grids = new Vector<UniformGrid>();

    Vector3D center    = new Vector3D(0,  0, 0);
    Vector3D base_vec  = new Vector3D(0, -1, 0);
    Vector3D up_vec    = new Vector3D(0,  0, 1);

    Vector3D vertical_axis   = new Vector3D( 0, 0, 1 );
    Vector3D beam_axis       = new Vector3D( 1, 0, 0 );
    Vector3D horiz_perp_axis = new Vector3D( 0, 1, 0 );
    Vector3D translation;

    String token;
    while ( sc.hasNext() )
    {
      token = sc.next();
      if ( token.equalsIgnoreCase("Det") )
      {
        id     = sc.nextInt();
        width  = sc.nextFloat();
        height = sc.nextFloat();
        depth  = sc.nextFloat();
        n_rows = sc.nextInt();
        n_cols = sc.nextInt();
        omega  = sc.nextFloat();
        chi    = sc.nextFloat(); 
        dist   = sc.nextFloat();
        UniformGrid grid = new UniformGrid( id, "m",
                                          center, base_vec, up_vec,
                                          width, height, depth,
                                          n_rows, n_cols );
        translation = new Vector3D( dist/100, 0, 0 );
        Translate( grid, translation );

        Rotate( grid, -45, beam_axis );

        float chi_angle = (float)(chi * 180 / Math.PI);
        Rotate( grid, chi_angle, horiz_perp_axis );

        float omega_angle = (float)(omega * 180 / Math.PI);
        Rotate( grid, omega_angle, vertical_axis );

        grids.add( grid );
      }
    }
    return grids;
  }

  public static String MakeIntialTopazDetCal( String det_data_file, 
                                              String det_cal_file )
         throws IOException
  {
    Vector<UniformGrid> grids = getTOPAZ_Detectors( det_data_file);
    System.out.println("There are " + grids.size() + " Detectors" );
    PrintGrids( grids, det_cal_file );
    return "Wrote file: " + det_cal_file;
  }

  public static void main( String args[] ) throws IOException
  {
    Vector<UniformGrid> grids = getTOPAZ_Detectors();
    System.out.println("There are " + grids.size() + " Detectors" );
    PrintGrids( grids, "TOPAZ_Detectors.DetCal" );

    System.out.println( MakeIntialTopazDetCal( "/home/dennis/SNS_ISAW/ISAW_ALL/Operators/TOF_SCD/SAMPLE_TOPAZ_DETECTOR_DATA.txt",
                            "TOPAZ_from_file.DetCal") );
  }


}
