/** File: SCDcal_util.java
 *
 * Copyright (C) 2003-2009, Dennis Mikkelson
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
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 */

package  DataSetTools.trial;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Sys.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;

/**
 * This class has various common convenience and I/O methods used in the
 * calibration of SCD instruments.
 */

public class SCDcal_util
{
   private SCDcal_util()
   {
     // Don't instantiate this class
   }


  /**
   *  Multiply the matrix A[][] times the vector x[], storing result in b[]
   */
  public static void mult_vector( double A[][], double x[], double b[] )
  {
    for ( int row = 0; row < x.length; row++ )
    {
      b[row] = 0.0;
      for ( int col = 0; col < x.length; col++ )
        b[row] += A[row][col] * x[col];
    }
  }


  /**
   *  Make a copy of a two-dimensional array of doubles
   */
  public static double[][] copy( double A[][] )
  {
    double M[][] = new double[ A.length ][ A[0].length ];
    for ( int i = 0; i < A.length; i++ )
      for ( int j = 0; j < A[0].length; j++ )
        M[i][j] = A[i][j];
    return M;
  }


  /** 
   *  Get an array listing all of the grid IDs (detector IDs) in
   *  order of increasing grid ID from the specified Hashtable.
   *
   *  @param  grid_table  Hashtable of detector grids (UniformGrid_d).
   *
   *  @return an array of the detector IDs.
   */
  public static int[] getAllGridIDs( Hashtable grid_table )
  {
     Object grid_objects[] = grid_table.values().toArray();
     int ids[] = new int[ grid_objects.length ];

     for ( int i = 0; i < ids.length; i++ )
       ids[i] = ((UniformGrid_d)grid_objects[i]).ID();

     Arrays.sort( ids );
     
     return ids;
  }


  /** 
   *  Get an array listing all of data grids in order of increasing
   *  grid ID, from the specified Hashtable.
   *
   *  @param  grid_table  Hashtable of detector grids (UniformGrid_d).
   *
   *  @return an array of the detector double precision data grids.
   */
  public static UniformGrid_d[] getAllGrids( Hashtable grid_table )
  {
    int[] ids = getAllGridIDs( grid_table );
    UniformGrid_d all_grids[] = new UniformGrid_d[ ids.length ]; 

    for ( int i = 0; i < ids.length; i++ )
      all_grids[i] = (UniformGrid_d)(grid_table.get( ids[i] ));

    return all_grids;
  }


  /**
   *  Form an int[][] with valid groups, as specified by the input Vector 
   *  of Vectors defining the groups.  The groups are "validated" by 
   *  removing any IDs that are not valid detector IDs, and by removing any
   *  duplicate group IDs.  The is_used flags array is also updated 
   *  by setting the position and orientation flags to false for each member
   *  of a group that is not the "key" member, and by setting all
   *  flags to false for any detector NOT listed in a group.  
   *
   *  @param groups_vec  Each entry in this Vector must be another Vector
   *                     which contains the Integer objects for one group
   *                     of detectors.
   *  @param grids       Hashtable of the grid objects.
   *  @param is_used     Vector of flags indicating which parameters are
   *                     used in the calibration.  These flags are updated
   *                     based on the final group information in the int[][]
   *                     that is built by this method.
   *
   *  @return An int[][] where each row contains the IDs of a valid group
   *          of detectors.  The is_used flags are also updated by setting
   *          the position and orientation flags to false for each member
   *          of a group that is not the "key" member, and setting all
   *          flags false for any detector NOT listed in a group.
   */
  public static int[][] formGroups( Vector groups_vec, 
                                    Hashtable grids,
                                    boolean[] is_used )
  {
    for ( int i = 0; i < groups_vec.size(); i++ )
    {
      Vector this_group = (Vector)groups_vec.elementAt(i);
      for ( int j = 0; j < this_group.size(); j++ )
        if ( ! ( (Integer)this_group.elementAt(j) instanceof Integer ) )
          throw new IllegalArgumentException( 
            "Need Vector of Vector of Integers in SCDcal_util.formGroups()" ); 
    }
                                         // First "filter" the groups_vec
                                         // it only has valid detector IDs
    for ( int i = groups_vec.size() - 1; i >= 0; i-- )
    {
      Vector this_group = (Vector)groups_vec.elementAt(i);
      for ( int j = this_group.size()-1; j >= 0; j-- )
      {
        Integer id = (Integer)this_group.elementAt(j);
        UniformGrid_d grid = (UniformGrid_d)grids.get(id);
        if ( grid == null )
        {
          this_group.removeElementAt(j); // discard invalid detector_id
          SharedMessages.addmsg("WARNING: Removed invalid detector_id: "+id );
          System.out.println("WARNING: Removed invalid detector_id: "+id );
        }
      }
      if ( this_group.size() <= 0 )
        groups_vec.removeElementAt(i);   // discard empty group
    }

                                         // Next, get rid of any duplicates
    Hashtable id_hash = new Hashtable();
    for ( int i = groups_vec.size() - 1; i >= 0; i-- )
    {
      Vector this_group = (Vector)groups_vec.elementAt(i);
      for ( int j = this_group.size()-1; j >= 0; j-- )
      {
        Integer id = (Integer)this_group.elementAt(j);
        Integer previous_id = (Integer)id_hash.get(id); 
        if ( previous_id != null )
        {
          this_group.removeElementAt(j); // discard previously used group_id
          SharedMessages.addmsg("WARNING: Removed duplicate detector_id: "+id );
          System.out.println("WARNING: Removed duplicate detector_id: "+id );
        }
        else
          id_hash.put( id, id );
      }
      if ( this_group.size() <= 0 )
        groups_vec.removeElementAt(i);   // discard empty group
    }
    
                                         // Now form the groups array.
    int[][] groups = new int[groups_vec.size()][];
    for ( int i = 0; i < groups_vec.size(); i++ )
    {
      Vector this_group = (Vector)groups_vec.elementAt(i);
      groups[i] = new int[ this_group.size() ];
      for ( int j = 0; j < this_group.size(); j++ )
        groups[i][j] = (Integer)this_group.elementAt(j);
    }
                                         // Adjust the is_used flags
                                         // so detectors NOT in a group are NOT
                                         // used at all.
    UniformGrid_d[] grid_array = getAllGrids( grids );
    for ( int det_index = 0; det_index < grid_array.length; det_index++ )
    {
      Integer grid_id = (Integer)id_hash.get( grid_array[det_index].ID() );

      if ( grid_id == null )             // grid not in group, so don't modify
      {
        int index = SCDcal.DET_BASE_INDEX + det_index * SCDcal.N_DET_PARAMS;
        is_used[ index + SCDcal.DET_WIDTH_INDEX  ] = false;
        is_used[ index + SCDcal.DET_HEIGHT_INDEX ] = false;
        is_used[ index + SCDcal.DET_X_OFF_INDEX  ] = false;
        is_used[ index + SCDcal.DET_Y_OFF_INDEX  ] = false;
        is_used[ index + SCDcal.DET_D_INDEX      ] = false;
        is_used[ index + SCDcal.DET_PHI_INDEX    ] = false;
        is_used[ index + SCDcal.DET_CHI_INDEX    ] = false;
        is_used[ index + SCDcal.DET_OMEGA_INDEX  ] = false;
      }
    }
                                         // Adjust the is_used flags so
                                         // detectors that are not the
                                         // "key" member are not moved.
    for ( int i = 0; i < groups.length; i++ )
    {
      for ( int j = 1; j < groups[i].length; j++ )   // grid not a key grid so
      {                                              // don't move it
        int member_id = groups[i][j];
        int det_index = detArrayIndex( member_id, grid_array ); 
        int index = SCDcal.DET_BASE_INDEX + det_index * SCDcal.N_DET_PARAMS;
        is_used[ index + SCDcal.DET_X_OFF_INDEX  ] = false;
        is_used[ index + SCDcal.DET_Y_OFF_INDEX  ] = false;
        is_used[ index + SCDcal.DET_D_INDEX      ] = false;
        is_used[ index + SCDcal.DET_PHI_INDEX    ] = false;
        is_used[ index + SCDcal.DET_CHI_INDEX    ] = false;
        is_used[ index + SCDcal.DET_OMEGA_INDEX  ] = false;
      } 
    }

    return groups;
  }


  /**
   *  Print the collection of group IDs used for calibration to the
   *  specified stream, in human readable form.
   *
   *  @param  out     The PrintStream to which the ids will be printed 
   *  @param  groups  The list of arrays giving the group structure
   *                  used for the calibration.
   */
  public static void showGroups( PrintStream out, 
                                 int[][]     groups )
  {
    if ( out == null || groups == null )
      return;

    out.println();
    out.println("================================================");
    out.println("Detector Groups used in calibration:");
    out.println("================================================");
    for ( int i = 0; i < groups.length; i++ )
    {
      if ( groups[i] != null )
      {
        out.print("Group " + i + ": " );
        for ( int j = 0; j < groups[i].length; j++ )
          out.print(" " + groups[i][j] );
        out.println();
      } 
    }
    out.println();
  }


  /**
   *  Find the index of a specified grid ID in the specified array
   *  of UniformGrid_d objects.
   *
   *  @return If a grid with the specified ID is present in the array,
   *          this returns the position in the array where it occurs.
   *          If there is no grid with the specified id in the array, then
   *          this throws an illegal argument exception.
   */
  public static int detArrayIndex( int id, UniformGrid_d[] array )
  {
    int det_count = 0;
    while ( det_count < array.length &&
            array[det_count].ID() != id )
      det_count++;

    if ( det_count > array.length )
      throw new IllegalArgumentException("Did not find detector ID " + id );

    return det_count;
  }


  /**
   *  Write all of the parameters names and values to the specified stream
   *
   *  @param  out      The print stream to write to
   *  @param  names    The list of parameter names
   *  @param  values   The list of parameter values
   *  @param  is_used  The list of flags indicating whether or not the 
   *                   parameter is refined.
   */
  public static void ShowInitalValues( PrintStream out,
                                       String      names[],
                                       double      values[],
                                       boolean     is_used[]  )
  {
    if ( out == null )
      return;

    out.println("------------- Initial Parameter Values ---------------");
    for ( int i = 0; i < values.length; i++ )
    {
      if ( is_used[i] )
        out.print("  ");
      else
        out.print("* ");
      out.println( names[i] +" = " + values[i] );
    }
  }


  /**
   *  Utility method to write all of the parameters names with an 
   *  indication of whether or not they are used.
   *
   *  @param  out     The print stream to write to
   *  @param  name    The list of parameter names
   *  @param  is_used List of flags indicating whether or not the 
   *                  corresponding parameter is used for calibration
   */
  public static void ShowIfUsed( PrintStream  out,
                                 String[]     name,
                                 boolean[]    is_used )
  {
    out.println(); 
    out.println("================================================="); 
    out.println("PARAMETERS USED FOR CALIBRATION:"); 
    out.println("================================================="); 
    for ( int i = 0; i < is_used.length; i++ )
      if ( is_used[i] )
        out.println( "USED " + name[i] );
      else
        out.println( "     " + name[i] );
  }



  /**
   *  Utility method to write all of the parameters names and values to 
   *  a specified stream
   *
   *  @param  out    The print stream to write to
   *  @param  names  The list of parameter names
   *  @param  values The list of parameter values
   */
  public static void WriteAllParams( PrintStream    out,
                                     String         names[],
                                     double         values[] )
  {
    if ( out == null )
    {
      SharedMessages.addmsg("WARNING: Can't write to .log or .results file!" );
      return;
    }

    out.println("#");
    out.println("# ALL POSSIBLE CALIBRATION PARAMETERS (IPNS Coordinates)");
    out.println("# " + (new Date()).toString() );
    out.println("# Lengths in meters");
    out.println("# Times in microseconds");
    out.println("# Angles in degrees");
    out.println("#");
    int max_label_length = 0;
    for ( int i = 0; i < names.length; i++ )
      if ( max_label_length < names[i].length() )
        max_label_length = names[i].length();

    for ( int i = 0; i < values.length; i++ )
    {
      out.print( names[i] + ": " );
      int pad = max_label_length - names[i].length();
      for (int space = 0; space < pad; space ++ )
        out.print(" ");
      if ( values[i] >= 0 )
        out.println ( " " + values[i] );
      else
        out.println ( values[i] );
    }
  }


  /**
   *  Utility method to write the information about the detector data grids
   *  to the specified output stream.
   *
   *  @param  out    The print stream to write to
   *  @param  grids  The array of detector grids to print.
   */
  public static void WriteGridInfo( PrintStream out, UniformGrid_d[] grids )
  {
    out.println("#");
    out.println("#");
    out.println("#  DETECTOR POSITION AND ORIENTATION VECTORS");
    out.println("#");
    out.println("#");
    for ( int i = 0; i < grids.length; i++ )
    {
      out.println("# Orientation of Detector " + grids[i].ID() + 
                  " (IPNS coordinates)");
      double comp[] = grids[i].position().get();
      out.println("# Center position:  ( " +
                           Format.real(comp[0], 10, 6, false) + ", " +
                           Format.real(comp[1], 10, 6, false) + ", " +
                           Format.real(comp[2], 10, 6, false) + ") "
                           );

      comp = grids[i].x_vec().get();
      out.println("#  local x_vector:  ( " +
                           Format.real(comp[0], 10, 6, false) + ", " +
                           Format.real(comp[1], 10, 6, false) + ", " +
                           Format.real(comp[2], 10, 6, false) + ") "
                           );

      comp = grids[i].y_vec().get();
      out.println("#  local y_vector:  ( " +
                           Format.real(comp[0], 10, 6, false) + ", " +
                           Format.real(comp[1], 10, 6, false) + ", " +
                           Format.real(comp[2], 10, 6, false) + ") "
                           );

      comp = grids[i].z_vec().get();
      out.println("#  detector normal: ( " +
                           Format.real(comp[0], 10, 6, false) + ", " +
                           Format.real(comp[1], 10, 6, false) + ", " +
                           Format.real(comp[2], 10, 6, false) + ") "
                           );
      out.println("#");
    }
  }


  /**
   *  Utility method to write the new calibration file information as 
   *  used at the SNS
   *
   *  @param  out    The print stream to write to
   *  @param  L1     The calculated L1 length, in meters
   *  @param  t0     The calculated t_zero shift, in microseconds 
   *  @param  grids  Array of the data grids
   */
  public static void WriteNewCalibrationInfo( PrintStream    out,
                                              double         L1,
                                              double         t0,
                                              UniformGrid_d  grids[]    )
  {
    out.println("#");
    out.println("# NEW CALIBRATION FILE FORMAT (in NeXus/SNS coordinates):");
    out.println("# Lengths are in centimeters. "); 
    out.println("# Base and up give directions of unit vectors for a local ");
    out.println("# x,y coordinate system on the face of the detector.");
    out.println("#");
    out.println("#");
    out.println("# " + (new Date()).toString() );
    out.println("6         L1     T0_SHIFT");
    out.printf ("7 %10.4f   %10.3f\n", L1 * 100, t0 );

    out.println("4 DETNUM  NROWS  NCOLS    WIDTH   HEIGHT   DEPTH   DETD   "+
                "CenterX   CenterY   CenterZ    "+
                "BaseX    BaseY    BaseZ      "+
                "UpX      UpY      UpZ" );
    for ( int i = 0; i < grids.length; i++ )
    {
      int        det_num = grids[i].ID();
      int        n_rows  = grids[i].num_rows();
      int        n_cols  = grids[i].num_cols();
      double     width   = grids[i].width()  * 100;    // convert to cm
      double     height  = grids[i].height() * 100;
      double     depth   = grids[i].depth()  * 100;
      Vector3D_d center  = grids[i].position();
      Vector3D_d base    = grids[i].x_vec();
      Vector3D_d up      = grids[i].y_vec();
      double     det_d   = center.length() * 100;

      out.printf("5 %6d %6d %6d %8.4f %8.4f %7.4f %6.2f ",
                  det_num, n_rows, n_cols, width, height, depth, det_d );
      out.printf("%9.4f %9.4f %9.4f ",
                  center.getY()*100, center.getZ()*100, center.getX()*100);
      out.printf("%8.5f %8.5f %8.5f ",
                  base.getY(), base.getZ(), base.getX());
      out.printf("%8.5f %8.5f %8.5f\n",
                  up.getY(), up.getZ(), up.getX());
    }
    out.println("#");
    out.println("#");
  }


  /**
   *  Utility method to read parameter values from a file, in the form
   *  written by WriteAllParams(). 
   *
   *  @param  filename   The name of the file to read from. 
   *  @param  names      The list of parameter names
   *  @param  values     The list of parameter values
   */
  public static void ReadParams( String filename, 
                                 String names[], 
                                 double values[] )
  {
    FileReader fr = null;
    BufferedReader br = null;
    System.out.println("Trying to read parameters: " + filename );
    try
    {
      fr = new FileReader( filename );
      if ( fr == null )
        System.out.println("ERROR: couldn't make FileReader " + filename );
      else
        System.out.println("SUCCESS: made FileReader " + filename );

      br = new BufferedReader( fr );
      String line;
                                                // skip leading comment lines
      line = br.readLine(); 
      System.out.println("READ -> " + line );
      while ( line != null && line.startsWith("#") )
      {
        line = br.readLine(); 
        System.out.println("READ -> " + line );
      }
                                              // make array of NaNs so we
                                              // can check if all values were
                                              // initialized from the file
      double[] file_values = new double[values.length];
      for ( int i = 0; i < file_values.length; i++ )
        file_values[i] = Double.NaN;
      
      String name;
      String val_string;
      double value;
      int    colon_index;
      while ( line != null )
      {
        colon_index = line.indexOf( ":" );
        if ( colon_index > 0 )               // try to find the parameter name
        {                                    // ignore line if no ":" on line
          name = line.substring( 0, colon_index );
          name.trim();
          for ( int i = 0; i < names.length; i++ )
            if ( name.equalsIgnoreCase( names[i] ) )
            {
               val_string = line.substring( colon_index + 1 ); 
               val_string.trim();
               value = Double.parseDouble( val_string );      
               file_values[i] = value;
            }
        }
        line = br.readLine();
        System.out.println("READ -> " + line );
      } 
        br.close();
      fr.close();
      boolean missing_values = false;
      for ( int i = 0; i < values.length; i++ )
        if ( Double.isNaN( file_values[i] ) )
          missing_values = true;
        else
          values[i] = file_values[i];
      
      if ( missing_values )
        throw new IllegalArgumentException("Failed to read some parameter");
    }
    catch ( Exception e )
    {
      SharedMessages.addmsg("WARNING: Problem reading parameter from file: " );
      SharedMessages.addmsg(" " + filename );
      e.printStackTrace();
    }
  }


  /*
   *  Extract list of (theory,measured) pairs, in a list of float point 2D
   *  objects.  If k = 0, this will extract the row numbers.  If k = 1, this
   *  will extract the column numbers.  If k = 2, this will extract the 
   *  times-of-flight
   *
   *  @param  k          Flag indicating which column to use from the 
   *                     theory and measured arrays.  
   *  @param  theory     List of predicted peak positions.  Each row contains
   *                     three entries, the predicted x, y and TOF.
   *  @param  measured   List of measured peak positions.  Each row contains
   *                     three entries, the predicted x, y and TOF.
   */
  public static floatPoint2D[] getPairs( int   k, 
                                         float theory[][], 
                                         float measured[][] )
  {
     if ( k < 0 || k > 2 )
     {
       System.out.println("ERROR: invalid index in SCDcalib.getPairs() " + k );
       return null;
     }
                                                    // Build a vector keeping
                                                    // only non-null points
     Vector pairs_vector = new Vector( measured.length );
     for ( int i = 0; i < measured.length; i++ )
       if ( theory[i] != null && measured[i] != null )
         pairs_vector.add( new floatPoint2D( theory[i][k], measured[i][k] ) );

     floatPoint2D pairs[] = new floatPoint2D[ pairs_vector.size() ];
     for ( int i = 0; i < pairs.length; i++ )
       pairs[i] = (floatPoint2D)pairs_vector.elementAt( i );

     arrayUtil.SortOnX( pairs );
     return pairs;
  }


  /**
   *  Make a graph showing where the specified (x,y) pairs lie relative to
   *  the line y=x.  NOTE: This uses a Runnable and SwingUtilities.invokeLater
   *  actually call the swing code that constructs the graph, so that the
   *  swing code executes in the event thread.  
   *
   *  @param title    The title to put on the graph
   *  @param label    The measured quantity (Time, Row, Column) used as
   *                  part of the axis labels.
   *  @param units    The units in which the quantity was measured, eg. 
   *                  microseconds.
   *  @param pairs    Array of x,y pairs, where x is the calculated value and
   *                  y is the measured value.
   */
  public static void MakeDisplay( String          title,
                                  String          label,
                                  String          units,
                                  floatPoint2D[]  pairs )
  {
    SwingUtilities.invokeLater(
                          new DisplayRunnable( title, label, units, pairs ) );
  }


  /**
   *  Runnable that forces the swing code to run in the swing thread.
   *  (See: Make Display.)
   */
  private static class DisplayRunnable implements Runnable
  {
    String          title;
    String          label;
    String          units;
    floatPoint2D[]  pairs;
 
    public DisplayRunnable( String          title,
                            String          label,
                            String          units,
                            floatPoint2D[]  pairs )
    {
      this.title = title;
      this.label = label;
      this.units = units;
      this.pairs = pairs;
    }

    public void run()
    {
      ConstructGraph( title, label, units, pairs );
    }

  }


  /**
   *  This does the actual work of constructing the graph for the MakeDisplay
   *  method.  It must be executed in the event thread, so it is passed in
   *  to SwingUtilities.invokeLater().  See: Make Display(), DisplayRunnable().
   */
  private static void ConstructGraph( String          title,
                                      String          label,
                                      String          units,
                                      floatPoint2D[]  pairs  )
  {
//  System.out.println("Making Display from thread " + Thread.currentThread());

    if ( pairs == null || pairs.length <= 0 )
    {
      System.out.println("ERROR: No pairs of points for " + title );
      return;
    }

    DataSetFactory ds_factory = new DataSetFactory( title,
                                                    units,
                                                   "Theoretical " + label,
                                                    units,
                                                   "Measured " + label );

                                           // one entry measured vs theoretical
    DataSet ds = ds_factory.getDataSet();
    Vector unique = new Vector( pairs.length );
    unique.add( pairs[0] );
    for ( int i = 1; i < pairs.length; i++ )
      if ( pairs[i].x > pairs[i-1].x )             // ok to add if increasing
        unique.add( pairs[i] );

    float x[] = new float[ unique.size() ];
    float y[] = new float[ unique.size() ];
    for ( int i = 0; i < x.length; i++ )
    {
      floatPoint2D point = (floatPoint2D)unique.elementAt( i );
      x[i] = point.x;
      y[i] = point.y;
    }
    XScale x_scale = new VariableXScale( x );
    Data d = Data.getInstance( x_scale, y, 1 );
    ds.addData_entry( d );

                                           // make second entry with y = x 
    for ( int i = 0; i < y.length; i++ )
      y[i] = x[i];
    d = Data.getInstance( x_scale, y, 2 );
    ds.addData_entry( d );

    ds.setSelectFlag( 0, true );
    ds.setSelectFlag( 1, true );

    new ViewManager( ds, IViewManager.SELECTED_GRAPHS );
  }


}
