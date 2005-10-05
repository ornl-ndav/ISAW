/*
 * File:  NPDF_Translate.java
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
 * Revision 1.1  2005/09/05 22:05:33  dennis
 * Utility to translate Thomas Proffen's calibration file for NPDF
 * into detector position files that can be read by ISAW.
 *
 *
 */

package DataSetTools.trial;

import java.io.*;

import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.ViewTools.Components.*;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import Operators.Generic.Load.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Displays.Display3D;
import gov.anl.ipns.Util.Sys.WindowShower;

/**
 *  This file contains static methods to translate the "npdf_stick_1066.cal" 
 *  file, made by NXproc into detector position files that can be read by ISAW.
 */

public class NPDF_Translate
{
  public static final String IN_FILE_NAME = "npdf_stick_1066.cal";

  public static final float INITIAL_PATH  = 32.0f;

  public static final int   FIRST_LPSD_GROUP_ID = 1001; 
  public static final int   NUM_LPSD            = 160;
  public static final int   NUM_SEGS_IN_LPSD    = 50;
  public static final float LPSD_DIAMETER = 0.0254f;

  public static final int   NUM_SED      = 124;
  public static final float SED_HEIGHT   = 0.5f;
  public static final float SED_DIAMETER = 0.01f;

  /**
   *  Calculate the position (in IPNS coordinates) that corresponds to 
   *  the "R", "2 theta" and "phi" values stored in the npdf data file.
   *  NOTE: The interpretation of two_theta and phi are not clear, so 
   *  the interpretation can be controlled by setting different values
   *  for the local boolean variables: phi_measured_from_sample and
   *  two_theta_measured_in_plane.
   */
  public static Vector3D position( double two_theta, double phi, double l2 )
  {
     boolean phi_measured_from_sample    = true;
     boolean two_theta_measured_in_plane = true;
     double  x, y, z;

     x = l2 * Math.cos( two_theta * Math.PI/180 );
                         
    if ( phi_measured_from_sample )
    {                                          // phi = elevation angle from
       z = l2 * Math.sin( phi * Math.PI/180 ); // sample position
     }
     else                                      // phi = elevation angle from
     {                                         // nearest point on beam
       double r2 = l2 * Math.sin( two_theta * Math.PI/180 );
       z = r2 * Math.sin( phi * Math.PI/180 );
     }

     if ( two_theta_measured_in_plane )               // 2theta measure in 
       y = l2 * Math.sin( two_theta * Math.PI/180 );  // scattering plane
     else
     {
       y = Math.sqrt( l2*l2 - x*x - z*z );            // 2theta in 3D
       if ( two_theta < 0 )
         y = -y;
     }

     return new Vector3D( (float)x, (float)y, (float)z );
  }


  /**
   *  Read the next line from the input file and form the vector giving the
   *  position of that pixel.
   */
  public static Vector3D next_position( TextFileReader f ) throws Exception
  {
    int    id        = f.read_int();
    double two_theta = f.read_double();
    double phi       = f.read_double();
    double l2        = f.read_double();
    f.read_line();

    return position( two_theta, phi, l2 );
  }


  /**
   *  Write a list of detector position information, initially in the
   *  the form of a list of UniformGrids to an ISAW "DetectorPosition" 
   *  file.
   *
   *  @param  out_file_name   Name of the detector position file to write
   *  @param  grids[]         Array of UniformGrids containint the detector
   *                          position.
   *
   */
  public static void WriteDetectorInfo( String      out_file_name, 
                                        UniformGrid grids[]       ) 
  {
    try
    {
      FileOutputStream fos = new FileOutputStream( out_file_name );
      PrintStream      out = new PrintStream( fos );

      out.println("Instrument_Type   1");
      out.println("Initial_Path     " + INITIAL_PATH );
      out.println("Num_Grids  " + grids.length );
      int first_index = 1; 
      for ( int i = 0; i < grids.length; i++ )
      {
        out.println("#");
        float center[] = grids[i].position().get();
        float x_vec[]  = grids[i].x_vec().get();
        float y_vec[]  = grids[i].y_vec().get();
        out.println("Grid_ID   " + grids[i].ID() );
        out.println("Num_Rows  " + grids[i].num_rows() );
        out.println("Num_Cols  " + grids[i].num_cols() );
        out.println("Width     " + grids[i].width()  );
        out.println("Height    " + grids[i].height() );
        out.println("Depth     " + grids[i].depth()  );
        out.println("Center    " + center[0]+" "+center[1]+" "+center[2]);
        out.println("X_vector  "  + x_vec[0]+" "+ x_vec[1]+" "+ x_vec[2]); 
        out.println("Y_vector  "  + y_vec[0]+" "+ y_vec[1]+" "+ y_vec[2]); 
        out.println("First_Index " + first_index );
        first_index += grids[i].num_rows() * grids[i].num_cols();
      }
      out.close();
    }
    catch ( Exception e )
    {
      System.out.println("Exception writing " + out_file_name );
      System.out.println("Exception is " + e );
      e.printStackTrace();
    }
  }


  /**
   *  Load the LPSD detector position information from the specified NPDF 
   *  calibration file, to the specified ISAW detector position file.
   */
  public static void TranslateLPSDs(String in_file_name, String out_file_name)
  {
     System.out.println("Loading file " + in_file_name );
     System.out.println("Writing file " + out_file_name );
     
     Vector3D pos[]  = new Vector3D[ NUM_SEGS_IN_LPSD ];
     UniformGrid grids[] = new UniformGrid[ NUM_LPSD ];

     int offset = 7;   // note an offset of 7 gives the minmum maximum
                       // discrepancy between what is in the file from
                       // Thomas, and the positions along the grid we
                       // construct. 
     //for ( offset = 0; offset < 25; offset++ )
     try 
     {
       float max_max_err = 0;
       TextFileReader f = new TextFileReader( in_file_name );
       f.SkipLinesStartingWith( "#" );

       for ( int lpsd = 1; lpsd <= NUM_LPSD; lpsd++ )
       {
         for ( int seg = 0; seg < NUM_SEGS_IN_LPSD; seg++ )
           pos[seg] = next_position(f); 
         
         Vector3D diff = new Vector3D( pos[ NUM_SEGS_IN_LPSD - 1 ] );
         diff.subtract( pos[0] );
         float length = diff.length() *
                        NUM_SEGS_IN_LPSD / (float)( NUM_SEGS_IN_LPSD - 1 );

         Vector3D y_vec = new Vector3D( diff );
         y_vec.normalize();

         Vector3D center = new Vector3D( pos[0 + offset ] );
         center.add( pos[ NUM_SEGS_IN_LPSD - 1 - offset ] );
         center.multiply( 0.5f);

         Vector3D z_vec = new Vector3D( center );
         z_vec.normalize();
 
         Vector3D x_vec = new Vector3D();
         x_vec.cross( y_vec, z_vec );
       
         grids[lpsd-1] = new UniformGrid( 1000*lpsd, "m", center, x_vec, y_vec,
                                          LPSD_DIAMETER, length, LPSD_DIAMETER,
                                          NUM_SEGS_IN_LPSD, 1 );
         float max_err = 0;
         float err;
         for ( int row = 1; row < 50; row++ )
         {
           Vector3D err_vec = new Vector3D( pos[row-1] );
           err_vec.subtract( grids[lpsd-1].position(row,1) );
           err = err_vec.length();
           if ( err > max_err )
             max_err = err;
         } 
         if ( max_err > max_max_err )
           max_max_err = max_err;
       }
       System.out.println("offset = "+offset+", max_max_err = "+max_max_err );
       f.close();
     }
     catch ( Exception e )
     {
       System.out.println("Exception trying to read file " + in_file_name );
       System.out.println("Exception is : " + e );
       e.printStackTrace();
     }

     WriteDetectorInfo( out_file_name, grids );
  }


  /**
   *  Load the SED detector position information from the specified NPDF 
   *  calibration file, to the specified ISAW detector position file.
   */
  public static void TranslateSEDs(String in_file_name, String out_file_name)
  {
     System.out.println("Loading file " + in_file_name );
     System.out.println("Writing file " + out_file_name );

     Vector3D center = new Vector3D();
     Vector3D x_vec  = new Vector3D();
     Vector3D y_vec  = new Vector3D( 0, 0, 1 );
     Vector3D n_vec  = new Vector3D();

     UniformGrid grids[] = new UniformGrid[ NUM_SED ];

     try
     {
       TextFileReader f = new TextFileReader( in_file_name );
       f.SkipLinesStartingWith( "#" );
                                                       // skip over LPSDs
       for ( int lpsd = 1; lpsd <= NUM_LPSD * NUM_SEGS_IN_LPSD; lpsd++ )
         f.read_line();

       for ( int sed = 1; sed <= NUM_SED; sed++ )
       {
         center = next_position(f);
                                                    // construct nomal to det
         n_vec = new Vector3D( center );            // pointing back to sample
         n_vec.normalize();
         n_vec.multiply( -1 );
                                                    // make x_vec perpendiular
         x_vec.cross( y_vec, n_vec );               // to y_vec and n_vec.

//       System.out.println("id = " + sed + " center = " + center );

         grids[sed-1] = new UniformGrid( sed + NUM_LPSD, 
                                         "m", center, x_vec, y_vec,
                                         SED_DIAMETER, SED_HEIGHT, SED_DIAMETER,
                                         1, 1 );
       }
       f.close();
     }
     catch ( Exception e )
     {
       System.out.println("Exception trying to read file " + in_file_name );
       System.out.println("Exception is : " + e );
       e.printStackTrace();
     }

     WriteDetectorInfo( out_file_name, grids );
  }


  /**
   *  Translate the NPDF calibration file into two ISAW detector position
   *  files for the LPSDs and SEDs.
   */
  public static void main( String args[] )
  {
     String directory =  "/home/dennis/WORK/ISAW/InstrumentInfo/LANSCE/"; 

     String in_file_name = directory + IN_FILE_NAME;
     String out_file_name = directory + "NPDFpsd_y_vs_tof.dat";

     TranslateLPSDs( in_file_name, out_file_name );

     out_file_name = directory + "NPDFsed_tof.dat";
     TranslateSEDs( in_file_name, out_file_name );
  }

}
