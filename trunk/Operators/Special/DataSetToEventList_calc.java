/* 
 * File: FormHistogram.java
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
 *  $Author: eu7 $
 *  $Date: 2008-07-31 20:35:22 -0500 (Thu, 31 Jul 2008) $            
 *  $Revision: 19280 $
 */

package Operators.Special;

import java.util.*;
import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.retriever.*;
import EventTools.EventList.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class contains static methods to write a time-of-flight DataSet to
 *  an event list in reciprocal space.
 */
public class DataSetToEventList_calc
{
  public enum BeamDirection{ X, Y, Z };

  /**
   *  Make an event list in reciprocal space, from the bins of the 
   *  specified DataSet that have 1 or more counts.
   *  
   *  @param ds   The DataSet whose bins are mapped to reciprocal space
   *              to form the event list.
   *  @param dir  The neutron beam direction, BeamDirection.X for IPNS,
   *              coordinates, and BeamDirection.Z for SNS coordinates.
   *
   *  @return A list of the events in reciprocal space, or null if all of the
   *          bins of the DataSet have zero counts.
   */
  public static FloatArrayEventList3D 
                    MakeDiffractometerQEventList( DataSet       ds,
                                                  BeamDirection dir )
  {
     if ( ds == null )
       throw new IllegalArgumentException("ERROR: ds null");

     float initial_path = AttrUtil.getInitialPath( ds );

     if ( Float.isNaN( initial_path ) )
     {
       Data data = ds.getData_entry( 0 );
       initial_path = AttrUtil.getInitialPath( data );
     }

     if ( Float.isNaN( initial_path ) )
       throw new IllegalArgumentException("ERROR: missing initial path " +
                                          "in DataSet");
     Vector3D beam_dir = null;
     if ( dir == BeamDirection.X )
       beam_dir = new Vector3D( 1, 0, 0 );
     else if ( dir == BeamDirection.Y )
       beam_dir = new Vector3D( 0, 1, 0 );
     else 
       beam_dir = new Vector3D( 0, 0, 1 );
 
     Vector3D q_dir, 
              vec,
              scatt_vec;
     float    angle_radians,
              dot_prod,
              path_len_m,
              mag_Q;
     DetectorPosition pos;
     Vector<Vector3D> q_vectors = new Vector<Vector3D>();
     Vector<Integer>  counts    = new Vector<Integer>();

     int num_data = ds.getNum_entries();
     for ( int j = 0; j < num_data; j++ )
     {
       Data data = ds.getData_entry( j );
       pos  = AttrUtil.getDetectorPosition( data );
       if ( pos == null )
         throw new IllegalArgumentException("ERROR: missing detector " +
                       "position for DataSet entry " + j );

       float[] scatt_coords = pos.getCartesianCoords();

       scatt_vec = new Vector3D( scatt_coords );
       scatt_vec.normalize();
       dot_prod = scatt_vec.dot(beam_dir);
       angle_radians = (float)Math.acos( dot_prod );

       path_len_m = initial_path + pos.getDistance();
  
       q_dir = new Vector3D(scatt_coords);
       q_dir.normalize();                      // normalize to subtract unit
                                               // vectors
       q_dir.subtract( beam_dir );

       q_dir.normalize();                      // normalize again to get unit
                                               // vector in direction of Q
 
       float[] ys = data.getY_values();
       float[] xs = data.getX_scale().getXs();
       for ( int k = 0; k < ys.length; k++ )
       {
//       if ( xs[k] < 16500 && ys[k] >= 1 )    // stay before next pulse 
         if ( ys[k] >= 1 )                     // add event to list 
         {
           mag_Q = tof_calc.DiffractometerQ( angle_radians, path_len_m, xs[k] );
           vec = new Vector3D( q_dir );
           vec.multiply( mag_Q );   
           q_vectors.add( vec );
           counts.add( (int)ys[k] );
//         counts.add( (int)Math.max( mag_Q/20 * ys[k],1) );
//         counts.add( (int)Math.max( mag_Q*mag_Q/400 * ys[k],1) );
         }
       }         
     } 
    
     int[]   codes  = new int[ counts.size() ]; 
     float[] x_vals = new float[ q_vectors.size() ];
     float[] y_vals = new float[ q_vectors.size() ];
     float[] z_vals = new float[ q_vectors.size() ];
     
     for ( int i = 0; i < codes.length; i++ )
     {
        codes[i] = counts.elementAt(i);

        vec = q_vectors.elementAt(i);
        x_vals[i] = vec.getX();
        y_vals[i] = vec.getY();
        z_vals[i] = vec.getZ();
     }
     System.out.println("Found events # " + codes.length );

     if ( codes.length > 0 )
       return new FloatArrayEventList3D(codes, x_vals, y_vals, z_vals);
     else
       return null; 
  }


  public static void main( String args[] ) throws IOException
  {
/*
    String indir  = "/usr2/SCD_TEST/";
    String infile = "scd08336";
    String inname = indir + infile + ".run";
    Retriever rr = new RunfileRetriever( inname );
    int min_ds_num = 2;
    int max_ds_num = 2;
*/
    String indir  = "/usr2/ARCS_SCD/";
    String infile = "ARCS_419";
    String inname = indir + infile + ".nxs";
    Retriever rr = new NexusRetriever( inname );
    int min_ds_num = 1;
    int max_ds_num = 114;

    BeamDirection beam_dir = BeamDirection.X; // currently SNS NeXus files are
                                              // are remapped into IPNS coords
    FloatArrayEventList3D events = null;
    int num_datasets = rr.numDataSets();
    Vector<IEventList3D> all_events = new Vector<IEventList3D>();
    for ( int i = min_ds_num; i <= max_ds_num; i++ )
    {
      DataSet ds = rr.getDataSet(i);
      events = MakeDiffractometerQEventList( ds, beam_dir );
      if ( events != null )
        all_events.add( events );
    }

    events = FloatArrayEventList3D.merge( all_events );    

    String outdir   = "/home/dennis/";

    String outname = outdir + infile + ".events";
    
    ByteFile16EventList3D.MakeByteFile16_3D( events, outname );
  }


}
