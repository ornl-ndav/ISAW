/*
 * File:  Q_SliceExtractor.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.9  2004/05/10 20:42:23  dennis
 * Test program now just instantiates a ViewManager to diplay
 * calculated DataSet, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.8  2004/03/15 06:10:53  dennis
 * Removed unused import statements.
 *
 * Revision 1.7  2004/03/15 03:28:44  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.6  2004/03/10 17:54:22  dennis
 * Added method getCalculators() that returns the list of VecQToTOF
 * objects used to calculale that tranformations between Q and
 * points in a time of flight data set for a detector.
 *
 * Revision 1.5  2004/02/26 22:58:13  dennis
 * Added javadocs.
 *
 * Revision 1.4  2004/01/29 00:01:07  dennis
 * Put debug prints in if (debug).
 * Made constructor public.
 *
 * Revision 1.3  2004/01/24 23:41:15  dennis
 * Methods that extract slice planes and line cuts now
 * make copies of their parameters, so that the parameters
 * are not altered.
 * Fixed an error in the axis range specified.
 * Added code to main test program to calculate and
 * sum a series of slices, in an attempt to provide an
 * efficient volume rendering algorithm.
 *
 * Revision 1.2  2004/01/07 15:02:57  dennis
 * Added methods to allow extracting 1 & 2 dimensional slices, using
 * HKL values.  Refined main test program to produce these slices
 * with properly labeled axes.
 *
 * Revision 1.1  2004/01/06 16:01:18  dennis
 * Initial form of display of slices in Q space, based on new ViewComponents.
 * (Not Complete)
 */

package DataSetTools.trial;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Components.*;

import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.DataSet.Attribute.*;

/**
 *  This class takes a reference to a list of SCD time-of-flight DataSets 
 *  and allows the user to extract 1 or 2 dimensional cuts through Q-space, 
 *  by specifying a line or slice, either using Q vectors, or using 
 *  Miller indices.
 */
public class Q_SliceExtractor
{
  Vector calculators = null;

  boolean debug = false;

  /* -------------------------- constructor ------------------------------- */
  /**
   *  Construct a Q_SliceExtractor for the specified DataSets.
   *
   *  @param ds_array  Array of time-of-flight DataSets from which the 
   *                   Q or HKL slices will be calculated. 
   *
   */
  public Q_SliceExtractor( DataSet ds_array[] )
  {
    if ( ds_array == null )
    {
       System.out.println("ERROR: ds_array null in Q_SliceExtractor");
       return;
    }
    if ( ds_array == null )
    {
       System.out.println("ERROR: ds_array empty in Q_SliceExtractor");
       return;
    }

    calculators = new Vector();
    for ( int index = 0; index < ds_array.length; index++ )
    {
      try
      {
        int i = 1;                         // add the ith detector, while 
        while ( true )                     // there actually is an ith detector
        {
          VecQToTOF transformer = new VecQToTOF( ds_array[index], i );
          if ( debug )
          {
            System.out.println("Found Data Grid...................... " );
            System.out.println( transformer.getDataGrid() );
          }
          calculators.add( transformer );
          i++;
        }
      }
      catch (InstantiationError e )
      {
        if ( debug )
          System.out.println( e );
      }
   }
  }


  /* ------------------------ getCalculators ---------------------------- */
  /**
   *  Get the vector containing the list of references to the VecQToTOF objects 
   *  used to calculate the mapping from hkl or Q back to points on a 
   *  detector.  There is one VecQToTOF object for each detector in each 
   *  DataSet used to build this  Q_SliceExtractor.  
   *  NOTE: Since this returns the Vector of references to the VecQToTOF 
   *  objects used internally by this class, it must be used carefully.  
   *
   *  @return a reference to the list of VecQToTOF objects used to calculate 
   *          the mapping from hkl or Q back to points on a detector.
   */
  public Vector getCalculators()
  {
    return calculators;
  } 


  /* --------------------------- HKL_Slice ------------------------------- */
  /**
   *  Calculate a list of interpolated intensity values from the DataSets 
   *  at evenly spaced points along a line through HKL space.  If a point 
   *  in HKL corresponds to more than one DataSet, the intensity values from
   *  those DataSets will be averaged.
   *
   *  @param  orientation_matrix   The orientation matrix used to map from 
   *                               HKL to Q.
   *  @param  start_point          The first endpoint of the line in HKL
   *  @param  end_point            The last  endpoint of the line in HKL
   *  @param  n_points             The number of points along the line.
   *
   *  @return An array of intensity values the "n_points" evenly space points
   *          along the line from start_point to end_point.
   */
  public float[]  HKL_Slice( Tran3D   orientation_matrix,
                             Vector3D start_point, 
                             Vector3D end_point,
                             int      n_points  )
  {
    start_point = new Vector3D( start_point );
    end_point   = new Vector3D( end_point );

    orientation_matrix.apply_to( start_point, start_point );
    orientation_matrix.apply_to( end_point,   end_point );

    return Q_Slice( start_point, end_point, n_points );
  }


  /* --------------------------- Q_Slice ------------------------------- */
  /**
   *  Calculate a list of interpolated intensity values from the DataSets 
   *  at evenly spaced points along a line through Q space.  If a point 
   *  in Q corresponds to more than one DataSet, the intensity values from
   *  those DataSets will be averaged.
   *
   *  @param  start_point          The first endpoint of the line in HKL
   *  @param  end_point            The last  endpoint of the line in HKL
   *  @param  n_points             The number of points along the line.
   *
   *  @return An array of intensity values the "n_points" evenly space points
   *          along the line from start_point to end_point.
   */
  public float[]  Q_Slice( Vector3D start_point, 
                           Vector3D end_point,
                           int      n_points  )
  {
    start_point = new Vector3D( start_point );
    end_point   = new Vector3D( end_point );

    if ( n_points < 2 )
      n_points = 2;

    Vector3D step_vec = new Vector3D( end_point );
    step_vec.subtract( start_point );
    step_vec.multiply( 1.0f/(n_points-1) ); 
    Vector3D q = new Vector3D();

    float value;
    float slice[] = new float[n_points];
    VecQToTOF transformer;
    float sum;
    int n_non_neg;
    for ( int j = 0; j < n_points; j++ )
    {
      q.set( step_vec );
      q.multiply( j );
      q.add( start_point );

      sum = 0;
      n_non_neg = 0;
      for ( int i = 0; i < calculators.size(); i++ )
      {
        transformer = (VecQToTOF)(calculators.elementAt(i));
        value = transformer.intensityAtQ( q );
        if ( value >= 0 )
        {
          sum += value;
          n_non_neg++;
        }
      }
      if ( n_non_neg > 0 )
        slice[j] = sum / n_non_neg;
      else
        slice[j] = 0;
    }
    return slice;
  }

 
  /* --------------------------- HKL_Slice ------------------------------- */
  /**
   *  Calculate a two dimensional array of interpolated intensity values 
   *  from the DataSets at points on a plane in HKL space.  If a point 
   *  in HKL corresponds to more than one DataSet, the intensity values from
   *  those DataSets will be averaged.  The basis vectors u, v should usually
   *  be perpendicular.  In any case the sample points are all of the form
   *  origin + i*dx*u + j*dy*v
   *
   *  @param  orientation_matrix   The orientation matrix used to map from 
   *                               HKL to Q.
   *  @param  origin               Center of the planar slice through HKL space
   *  @param  u                    Vector in HKL space that will both serve as
   *                               local x-axis direction and deterimine the
   *                               distance from the origin to the "right" 
   *                               edge of the plane. 
   *  @param  v                    Vector in HKL space that will both serve as
   *                               local y-axis direction and deterimine the
   *                               distance from the origin to the "top" 
   *                               edge of the plane. 
   *  @param  n_u_steps            The number of intervals along the line from 
   *                               the origin to (origin+u).  For example, if
   *                               50 steps are specified, the intensity will
   *                               be interpolated at 101 steps in the +-u
   *                               directions.
   *  @param  n_v_steps            The number of intervals along the line from 
   *                               the origin to (origin+v).  For example, if
   *                               50 steps are specified, the intensity will
   *                               be interpolated at 101 steps in the +-v
   *                               directions.
   *
   *  @return A two dimensional array of interpolated intensity values of size 
   *          (2*n_u_steps+1)*(2*n_v_steps+1) evaluated at points of the form 
   *          origin + i*dx*u + j*dy*v. 
   */

  public float[][]  HKL_Slice( Tran3D   orientation_matrix,
                               Vector3D origin,
                               Vector3D u,
                               Vector3D v,
                               int      n_u_steps,
                               int      n_v_steps )
  {
    origin = new Vector3D( origin );
    u      = new Vector3D( u );
    v      = new Vector3D( v );

    orientation_matrix.apply_to( origin, origin );
    orientation_matrix.apply_to( u, u );
    orientation_matrix.apply_to( v, v );

    return Q_Slice( origin, u, v, n_u_steps, n_v_steps );
  }

  
  /* ---------------------------- Q_Slice ------------------------------- */
  /**
   *  Calculate a two dimensional array of interpolated intensity values 
   *  from the DataSets at points on a plane in Q space.  If a point 
   *  in Q corresponds to more than one DataSet, the intensity values from
   *  those DataSets will be averaged.  The basis vectors u, v should usually
   *  be perpendicular.  In any case the sample points are all of the form
   *  origin + i*dx*u + j*dy*v
   *
   *  @param  origin               Center of the planar slice through Q space
   *  @param  u                    Vector in Q space that will both serve as
   *                               local x-axis direction and deterimine the
   *                               distance from the origin to the "right" 
   *                               edge of the plane. 
   *  @param  v                    Vector in Q space that will both serve as
   *                               local y-axis direction and deterimine the
   *                               distance from the origin to the "top" 
   *                               edge of the plane. 
   *  @param  n_u_steps            The number of intervals along the line from 
   *                               the origin to (origin+u).  For example, if
   *                               50 steps are specified, the intensity will
   *                               be interpolated at 101 steps in the +-u
   *                               directions.
   *  @param  n_v_steps            The number of intervals along the line from 
   *                               the origin to (origin+v).  For example, if
   *                               50 steps are specified, the intensity will
   *                               be interpolated at 101 steps in the +-v
   *                               directions.
   *
   *  @return A two dimensional array of interpolated intensity values of size 
   *          (2*n_u_steps+1)*(2*n_v_steps+1) evaluated at points of the form 
   *          origin + i*dx*u + j*dy*v. 
   */

  public float[][]  Q_Slice( Vector3D origin, 
                             Vector3D u,  
                             Vector3D v, 
                             int      n_u_steps, 
                             int      n_v_steps )
  {
    if ( debug )
      System.out.println("Start of make_slice......");

    origin = new Vector3D( origin );
    u      = new Vector3D( u );
    v      = new Vector3D( v );

    if( origin == null || u == null || v == null )
      return null;

    if ( n_u_steps < 1 )                     // use at least two steps in each
      n_u_steps = 1;                         // direction
    if ( n_v_steps < 1 )
      n_v_steps = 1;

    int n_rows = 2*n_v_steps + 1;
    int n_cols = 2*n_u_steps + 1;
    float image[][] = new float[n_rows][n_cols];

    Vector3D base1 = new Vector3D( u );      // make two unit basis vectors
    Vector3D base2 = new Vector3D( v );
    base1.normalize();
    base2.normalize();

    float b1[] = base1.get();
    float b2[] = base2.get();

    if ( debug )
    {
      System.out.println("Origin = " + origin );
      System.out.println("base1  = " + base1 );
      System.out.println("base2  = " + base2 );
    }

    float orig[] = origin.get();
    Vector3D q = new Vector3D();

    float col_step = u.length()/n_u_steps;
    float row_step = v.length()/n_v_steps;

    float d_row, d_col;
    float value;
    int   n_non_neg;
                                             // for each point in the plane...
    VecQToTOF transformer;
    float sum;
    for ( int row = -n_v_steps; row <= n_v_steps; row++ )
      for ( int col = -n_u_steps; col <= n_u_steps; col++ )
      {
        d_row = -row * row_step;
        d_col =  col * col_step;

        q.set( orig[0] + d_row * b2[0] + d_col * b1[0],
               orig[1] + d_row * b2[1] + d_col * b1[1],
               orig[2] + d_row * b2[2] + d_col * b1[2]  );

        sum = 0;
        n_non_neg = 0;
        for ( int i = 0; i < calculators.size(); i++ )
        {
          transformer = (VecQToTOF)(calculators.elementAt(i));
          value = transformer.intensityAtQ( q );
          if ( value >= 0 )
          {
            sum += value;
            n_non_neg++;
          }
        }
        if ( n_non_neg > 0 )
          image[row + n_v_steps][col + n_u_steps] = sum / n_non_neg;
        else
          image[row + n_v_steps][col + n_u_steps] = 0;
      }
    return image;
  }

 /* ---------------------------- main ------------------------------ */
 /** 
  *  Simple main program for testing purposes.
  *
  */

  public static void main( String args[] )
  {
     String   CONST_H = "Constant H";
     String   CONST_K = "Constant K";
     String   CONST_L = "Constant L";

     String   file_name = null;
     int      hist_num = 1;

     // FIND PEAKs IN 8336, det 19, r,c,chan = 15,26,189, seq #59
     file_name = "/usr2/ARGONNE_DATA/SCD_QUARTZ_2_DET/scd08336.run";
     hist_num = 2;

     System.out.println("----------LOADING FILE " + file_name );
     RunfileRetriever rr = new RunfileRetriever( file_name );
     DataSet ds = rr.getDataSet(hist_num);

     String calib_file_name = 
                      "/usr2/ARGONNE_DATA/SCD_QUARTZ_2_DET/instprm.dat";
     LoadSCDCalib load_cal = new LoadSCDCalib( ds, calib_file_name, -1, null );
     load_cal.getResult();
 
     new ViewManager(ds, IViewManager.IMAGE );

     DataSet ds_array[] = new DataSet[1];
     ds_array[0] = ds;
     Q_SliceExtractor slicer = new Q_SliceExtractor( ds_array );

     float image_array[][] = null;

     float orient_mat[][] = { { -0.101476f, -0.179852f,  0.114093f, 0 },
                              {  0.002035f, -0.002749f,  0.233230f, 0 },
                              { -0.162069f,  0.089732f,  0.001447f, 0 },
                              {      0,           0,          0,    0 } };
     orient_mat = LinearAlgebra.getTranspose( orient_mat );
     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         orient_mat[i][j] *= ((float)Math.PI * 2);

     Tran3D orientation_matrix = new Tran3D( orient_mat );
 
     Vector3D origin = null,
              u      = null, 
              v      = null,
              start  = null,
              end    = null;
     String   hkl_cut = CONST_K;
     if ( hkl_cut.equals( CONST_H ) )
     {
       origin = new Vector3D( -4, 4, 4 );
       u      = new Vector3D(  0, 3, 0 );
       v      = new Vector3D(  0, 0, 3 );
       start  = new Vector3D( -3.5f, 2, 3 );
       end    = new Vector3D( -2.5f, 2, 3 );
     }
     if ( hkl_cut.equals( CONST_K ) )
     {
       origin = new Vector3D( -4, 4, 4 );
       u      = new Vector3D(  3, 0, 0 );
       v      = new Vector3D(  0, 0, 3 );
       start  = new Vector3D( -3.5f, 2, 3 );
       end    = new Vector3D( -2.5f, 2, 3 );
     }
     if ( hkl_cut.equals( CONST_L ) )
     {
       origin = new Vector3D( -4, 4, 4 );
       u      = new Vector3D(  3, 0, 0 );
       v      = new Vector3D(  0, 3, 0 );
       start  = new Vector3D( -3.5f, 2, 3 );
       end    = new Vector3D( -2.5f, 2, 3 );
     }

     image_array = slicer.HKL_Slice( orientation_matrix, 
                                     origin, u, v, 500, 500 );

     float slice_array[] = slicer.HKL_Slice( orientation_matrix, 
                                             start, end, 500);

     Vector3D test_vec = new Vector3D( -9, 7, 3 );
     System.out.println("+++++Test Vector in HKL  = " + test_vec );
     orientation_matrix.apply_to( test_vec, test_vec );
     System.out.println("+++++Test Vector in Qxyz = " + test_vec );
     

     GraphFrame gf = new GraphFrame( slice_array, "Linear Cut" ); 
     VirtualArray2D va2D = new VirtualArray2D( image_array );

     if ( hkl_cut.equals( CONST_H ) )
     {  
       va2D.setTitle( "H = -4" );
       va2D.setAxisInfo( AxisInfo.X_AXIS, 1f, 7f,
                        "K","(Index)", true );
       va2D.setAxisInfo( AxisInfo.Y_AXIS, 1f, 7f,
                        "L","(Index)", true );
     }
     if ( hkl_cut.equals( CONST_K ) )
     {  
       va2D.setTitle( "K = 4" );
       va2D.setAxisInfo( AxisInfo.X_AXIS, -7f, -1f,
                        "H","(Index)", true );
       va2D.setAxisInfo( AxisInfo.Y_AXIS, 1f, 7f,
                        "L","(Index)", true );
     }
     if ( hkl_cut.equals( CONST_L ) )
     {
       va2D.setTitle( "L = 4" );
       va2D.setAxisInfo( AxisInfo.X_AXIS, -7f, -1f,
                        "H","(Index)", true );
       va2D.setAxisInfo( AxisInfo.Y_AXIS, 1f, 7f,
                        "K","(Index)", true );
     }

     ImageFrame2 frame = new ImageFrame2( va2D );

     origin = new Vector3D( -5, 3.85f, 5 );
     u      = new Vector3D( .15f, 0, 0 );
     v      = new Vector3D(  0, 0, 0.15f );
     Vector3D delta_k = new Vector3D( 0, 0.01f, 0 );
     float vol_array[][] = new float[51][51];
/*
     for ( int row = 0; row < 51; row++ )
       for ( int col = 0; col < 51; col++ )
         vol_array[row][col] = 0;
*/
     for ( int slice = -15; slice <= 15; slice+=1 )
     {
       origin.add( delta_k );
       System.out.println("Slice = " + slice + " origin = " + origin );
       if ( slice == -15 )
         vol_array = slicer.HKL_Slice( orientation_matrix, 
                                       origin, u, v, 25, 25 );
       else
       {
         image_array = slicer.HKL_Slice( orientation_matrix, 
                                         origin, u, v, 25, 25 );
         for ( int row = 0; row < 51; row++ )
           for ( int col = 0; col < 51; col++ )
           {
//             vol_array[row][col] *= 0.95f;
             vol_array[row][col] += image_array[row][col];
           }
       } 
     }
     va2D = new VirtualArray2D( vol_array );
     va2D.setTitle( "K = 4" );
     va2D.setAxisInfo( AxisInfo.X_AXIS, -5.15f, -4.85f,
                      "H","(Index)", true );
     va2D.setAxisInfo( AxisInfo.Y_AXIS, 4.85f, 5.15f,
                      "L","(Index)", true );
     frame = new ImageFrame2( va2D );
  }

}
