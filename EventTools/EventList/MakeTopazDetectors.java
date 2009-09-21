/* 
 * File: MakeTopazDetectors.java
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
 * Currently, this just supports the initial configuration with
 * four detectors.  Detector numbering starts at 1. 
 */
public class MakeTopazDetectors
{

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


  private static void Translate( UniformGrid grid, Vector3D translation )
  {
    Vector3D center = grid.position();

    Tran3D tran = new Tran3D();
    tran.setTranslation( translation );

    tran.apply_to( center, center );

    grid.setCenter( center );
  }


  private static void PrintGrids( Vector<UniformGrid> grids, String filename )
               throws IOException
  {
    String outfilename = filename + ".grids";
    PrintStream out = new PrintStream( outfilename );
    System.out.println("Printing " + grids.size() + " Grids" );
    for ( int i = 0; i < grids.size(); i++ )
      out.println( Peak_new_IO.GridString(grids.elementAt(i)) );
    out.close();
  }


/**
 * NOTE: Internally, we need to use IPNS coordinates and the IO code
 * will translate this to SNS/NeXus coordinates. 
 */
  private static Vector<UniformGrid> getGrids_version_1()
  {
    float DET_WIDTH  = 0.150f;
    float DET_HEIGHT = 0.150f;
    float DET_DEPTH  = 0.002f;
    int   N_ROWS     = 256;
    int   N_COLS     = 256;

    Vector<UniformGrid> grids = new Vector<UniformGrid>();

    Vector3D center    = new Vector3D(0,  0, 0);
    Vector3D base_vec  = new Vector3D(0, -1, 0);
    Vector3D up_vec    = new Vector3D(0,  0, 1);

    Vector3D vertical_axis = new Vector3D( 0, 0, 1 ); 
    Vector3D beam_axis     = new Vector3D( 1, 0, 0 ); 
    Vector3D translation   = new Vector3D( 0.19f, 0, 0 );

    float[] radius = { 0, 19, 19, 19, 19 };
    float[] theta  = { 0,  0,  0,  0, .785398f };
    float[] phi    = { 0, -.785398f, -1.5708f, -2.35619f, -1.5708f };
    for ( int ID = 1; ID <=4; ID++ )
    {
      UniformGrid grid = new UniformGrid( ID, "m",
                                          center, base_vec, up_vec,
                                          DET_WIDTH, DET_HEIGHT, DET_DEPTH,
                                          N_ROWS, N_COLS );

      Translate( grid, translation );

      float phi_angle = (float)(phi[ID] * 180 / Math.PI);
      Rotate( grid, phi_angle, vertical_axis );

      float theta_angle = (float)(theta[ID] * 180 / Math.PI);
      Rotate( grid, theta_angle, beam_axis );

      grids.add( grid );
    }

    return grids;
  }


  public static void main( String args[] ) throws IOException
  {
    Vector<UniformGrid> grids = getGrids_version_1();
    System.out.println("There are " + grids.size() + " Grids" );
    PrintGrids( grids, "TOPAZ_Grids" );
  }


}
