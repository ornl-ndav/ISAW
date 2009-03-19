/*
 * File:  SCD_Grouped_cal.java
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *  This version was adapted from SCDcal.java to treat SNAP 3X3 panel as 
 *  one rigid object containing nine individual detectors that move with 
 *  3x3 panel.  The implemention actually is much more flexible and allows
 *  specifying groups of detectors that will be moved as one.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 */

package  DataSetTools.trial;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;

/**
 * This class extends the SCDcal class to allow treating groups of 
 * detectors as being rigidly attached to the first member in the 
 * group.  During the optimization process, moving the first member in
 * the group will cause all other members in the group to be moved as
 * though they were rigidly attached to the first member.
 */

public class SCD_Grouped_cal  extends    SCDcal 
                              implements Serializable
{
  private int[][]   groups;             // Each row in this array contains
                                        // a list of the detector IDs that
                                        // belong to one group.
  private Hashtable relative_transf;    // Table of tranformations to position
                                        // detectors relative to the key
                                        // detector for their group.  This will
                                        // be the identity matrix for the key
                                        // detectors.
  /**
   *  Construct a function defined on the grid of (x,y) values specified, 
   *  using the parameters and parameter names specified.  The grid points
   *  are numbered in a sequence and the point's index in the sequence is 
   *  used as the one variable.
   *
   *  @param  peaks_vector        Vector contianing indexed peaks in the form
   *                              of double precision PeakData_d objects.
   *  @param  grids               Hashtable of detector grids, using the grid 
   *                              id as the hash key.
   *  @param  groups              Ragged array of arrays of grid IDs
   *                              defining groups of detectors.  Each
   *                              entry in the array is an array of ints
   *                              specifying the members of one group.
   *                              All detectors from the grids hashtable 
   *                              MUST be listed in precisely one group and
   *                              the groups must only contain IDs of
   *                              detectors listed in the grids hashtable.
   *                              A group can have size one. The first detector
   *                              in the group will serve as the "key".  The
   *                              key's position and orientation will be
   *                              adjusted (if those parameters are specified
   *                              as used) and the other detectors in the
   *                              group will be moved as though rigidly 
   *                              attached to it.
   *  @param  params              Full list of possible parameters
   *  @param  param_names         Full list of possible parameter names
   *  @param  n_used              The number of parameters actually used
   *  @param  is_used             Array of flags indicating whether or not
   *                              the corresponding parameter is used.
   *                              NOTE: Only the "key" detectors can have
   *                              their position and orientation parameters
   *                              marked as used.
   *  @param  lattice_params      Array listing the lattice parameters of
   *                              the sample used for calibration.
   *  @param  log_file            The file where the progress of the 
   *                              calibration is logged.
   */
   public SCD_Grouped_cal( Vector      peaks_vector,
                           Hashtable   grids,
                           int         groups[][],
                           double      params[], 
                           String      param_names[],
                           int         n_used,
                           boolean     is_used[],
                           double      lattice_params[],
                           PrintStream log_file )
   {
     super( PeakData_d.SNS_SCD, 
            peaks_vector, 
            grids, 
            params, 
            param_names,
            n_used,
            is_used,
            lattice_params,
            log_file );

     this.groups = groups;
                                         // initialize table of transforms to
                                         // position grids relative to key grid
     relative_transf = new Hashtable();
     for ( int group_num = 0; group_num < groups.length; group_num++ )
     {
       int ids[] = groups[group_num];                  // deal with this group
       int key_id = ids[0];
       relative_transf.put( key_id, new Tran3D_d() );  
                                                       // key gets identity
                                                       // for relative transf
       UniformGrid_d key_grid = (UniformGrid_d)grids.get( key_id );
       Tran3D_d key_tran = new Tran3D_d();
       key_tran.setOrientation( key_grid.x_vec(),
                                key_grid.y_vec(),
                                key_grid.position() );

       for ( int member_num = 1; member_num < ids.length; member_num++ )
       {                                               // make relative transf
         Tran3D_d key_tran_inverse = new Tran3D_d( key_tran );
         key_tran_inverse.invert();

         int member_id = ids[ member_num ];
         UniformGrid_d grid = (UniformGrid_d)grids.get( member_id );
         Tran3D_d member_tran = new Tran3D_d();
         member_tran.setOrientation( grid.x_vec(),
                                     grid.y_vec(),
                                     grid.position() );
         key_tran_inverse.multiply_by( member_tran );
         relative_transf.put( member_id, key_tran_inverse );

//       System.out.println("Relative transf for Detector ======" + member_id );
//       System.out.println( key_tran_inverse );
       }
     }
/*
     System.out.println("Key    grid = " + groups[0][0] );
     System.out.println("Member grid = " + groups[0][1] );
     UniformGrid_d moved_grid = (UniformGrid_d)grids.get( groups[0][1] );
     System.out.println("BEFORE RELATIVE MOVE GRID AT ====================" );
     System.out.println( moved_grid );
     MoveGridRelativeToKey( groups[0][0], groups[0][1] );
     System.out.println("AFTER RELATIVE MOVE GRID AT *********************" );
     System.out.println( moved_grid );
     System.exit(0);
*/
     setParameters(parameters);   // This may seem circular, but it forces the
                                  // local version to set up initial values
                                  // for qxyz_theoretical, observed, UB, etd.
   }


  /**
   *  Set the new values for the list of parameters for this function.
   *  Set up the new position of the data grid.
   *
   *  @param  parameters  Array containing values to copy into the list of
   *                      parameter values for this function.
   */
  public void setParameters( double parameters[] )
  {
    if ( groups == null )    // this is null when called from the super class
      return;                // constructor, so we just return now.  This 
                             // method is called again from this classes' 
                             // constructor.
    super.setParameters( parameters );

    // copy the parameters that are being used for optimization into to master
    // list of parameters and then actually use all values from the master list.

    for ( int i = 0; i < parameters.length; i++ )
      all_parameters[ all_p_index[i] ] = parameters[i];

    // now that any parameters that can be changed have been updated in 
    // the master all_parameters list, update ALL grid information that will 
    // be used to recalculate the observed and predicted Q values for peaks. 
    // Process each group of detectors by first processing the key grid for
    // that group, then move the other members of the group relative to the
    // key grid.
    int det_index  = DET_BASE_INDEX;

    for ( int group_num = 0; group_num < groups.length; group_num++ )
    {
                                              // *** first adjust the key grid
      int[] group_ids = groups[ group_num ];
      int key_id = group_ids[0];

      UniformGrid_d key_grid = (UniformGrid_d)grids.get( key_id );

                            // find position of the key grid in array of dets
      int det_count = SCDcal_util.detArrayIndex( key_id, grid_array );

      det_index = DET_BASE_INDEX + det_count * N_DET_PARAMS;

      Vector3D_d nom_pos      = nominal_position[det_count];
      Vector3D_d nom_base_vec = nominal_base_vec[det_count];
      Vector3D_d nom_up_vec   = nominal_up_vec  [det_count];
      double width  = all_parameters[ det_index + DET_WIDTH_INDEX  ];
      double height = all_parameters[ det_index + DET_HEIGHT_INDEX ];
      double x_off  = all_parameters[ det_index + DET_X_OFF_INDEX  ];
      double y_off  = all_parameters[ det_index + DET_Y_OFF_INDEX  ];
      double det_d  = all_parameters[ det_index + DET_D_INDEX      ];
      double phi    = all_parameters[ det_index + DET_PHI_INDEX    ];
      double chi    = all_parameters[ det_index + DET_CHI_INDEX    ];
      double omega  = all_parameters[ det_index + DET_OMEGA_INDEX  ];

      key_grid.setWidth( width );
      key_grid.setHeight( height );

      // NOTE: The following should work OK in most cases.  det_d, x,y 
      //       offsets, and Euler angles are interpreted as explained below.
      //       The x,y offsets and Euler angles are applied to the nominal
      //       position and orientation obtained from the initial detector 
      //       grid that is passed in to the constructor.  
      //       det_d is the length of the line from the sample to the shifted
      //       grid center.  In general the grid's base(x_vec) and up(y_vec)
      //       vectors will NOT be perpendicular to this line, so the x and
      //       y offsets can't be interpreted as shifts in these directions,
      //       To avoid this problem, a new vector, x_perp, is constructed
      //       so that it is in the same general direction as the grid's
      //       x_vec, and is in the same plane as x_vec and the line from 
      //       the grid center to the the sample, but is perpendicular to
      //       the line from the center to the sample.  Similarly, a new
      //       vector y_perp is constructed to be in the same general direction
      //       as the grid's y_vec, but is perpendicular to the line from
      //       the sample to the grid center.  x_perp and y_perp are based
      //       on the grid's initial position.
      //       x offset is an offset in the direction of x_perp.
      //       y offset is an offset in the direction of y_perp.
      //       AFTER shifting the center according to the x and y offset
      //       values, the center is scaled to have length det_d.  This 
      //       will change the x and y offsets slightly.
      //
      Vector3D_d center      = new Vector3D_d( nom_pos );
      Vector3D_d minus_z_vec = new Vector3D_d( center );
      minus_z_vec.normalize();

      Vector3D_d x_perp = new Vector3D_d( nom_base_vec );
      double axial_comp = x_perp.dot( minus_z_vec );
      Vector3D_d delta  = new Vector3D_d( minus_z_vec );
      delta.multiply( -axial_comp );
      x_perp.add( delta );
      x_perp.normalize();
      x_perp.multiply( x_off );

      Vector3D_d y_perp = new Vector3D_d( nom_up_vec );
      axial_comp = y_perp.dot( minus_z_vec );
      delta  = new Vector3D_d( minus_z_vec );
      delta.multiply( -axial_comp );
      y_perp.add( delta );
      y_perp.normalize();
      y_perp.multiply( y_off );

      center.add( x_perp );
      center.add( y_perp );
      center.normalize();
      center.multiply( det_d );
      key_grid.setCenter( center );

      if ( phi != 0 || chi != 0 || omega != 0 )
      {
        Vector3D_d new_base = new Vector3D_d( nom_base_vec ); 
        Vector3D_d new_up   = new Vector3D_d( nom_up_vec ); 
        Tran3D_d euler_rotation = tof_calc_d.makeEulerRotation(phi,chi,omega);
        euler_rotation.apply_to( new_base, new_base );
        euler_rotation.apply_to( new_up, new_up );
        key_grid.setOrientation( new_base, new_up );
      }

                                              // now adjust the other grids in
                                              // the group relative to the key 
                                              // for the group
      Tran3D_d key_tran = new Tran3D_d();
      key_tran.setOrientation( key_grid.x_vec(),
                               key_grid.y_vec(),
                               key_grid.position() );

      for ( int member_num = 1; member_num < group_ids.length; member_num++ )
      {
        int member_id = group_ids[ member_num ];

        MoveGridRelativeToKey( key_id, member_id );
        UniformGrid_d member_grid = (UniformGrid_d)grids.get( member_id );

                           // find position of the member grid in array of dets
        det_count = SCDcal_util.detArrayIndex( member_id, grid_array );

        det_index = DET_BASE_INDEX + det_count * N_DET_PARAMS;
        width  = all_parameters[ det_index + DET_WIDTH_INDEX  ];
        height = all_parameters[ det_index + DET_HEIGHT_INDEX ];

        member_grid.setWidth( width );
        member_grid.setHeight( height );
      }
    }
   
    find_qxyz_observed();
    find_U_and_B_observed();
    find_qxyz_theoretical();

    eval_count++;
    if ( eval_count % 500 == 0 )
    {
      String message = "After " + eval_count + " steps... params are";
      ShowProgress( message, System.out );
      ShowProgress( message, log_file );
    }
  } 


  /**
   *  Move the specified member grid to it's correct position relative
   *  to the key grid for it's group, after adjusting the position of
   *  the key grid.
   */
  private void MoveGridRelativeToKey( int key_id, int member_id )
  {
    UniformGrid_d key_grid = (UniformGrid_d)grids.get( key_id );
    Tran3D_d key_tran = new Tran3D_d();
    key_tran.setOrientation( key_grid.x_vec(),
                             key_grid.y_vec(),
                             key_grid.position() );

    UniformGrid_d member_grid = (UniformGrid_d)grids.get( member_id );

    Tran3D_d relative_tran = (Tran3D_d)relative_transf.get(member_id);

    Tran3D_d grid_tran = new Tran3D_d( key_tran );
/*
    System.out.println("relative_tran = \n" + relative_tran );
    System.out.println("key_tran = \n" + key_tran );
    System.out.println("grid_tran = \n" + grid_tran );
*/    
    grid_tran.multiply_by( relative_tran );
//  System.out.println("grid_tran * relative_tran = \n " + grid_tran );

    Vector3D_d x_vec   = new Vector3D_d( 1, 0, 0 );
    Vector3D_d y_vec   = new Vector3D_d( 0, 1, 0 );
    Vector3D_d pos_vec = new Vector3D_d( 0, 0, 0 );

    grid_tran.apply_to( x_vec, x_vec );
    grid_tran.apply_to( y_vec, y_vec );
    grid_tran.apply_to( pos_vec, pos_vec );

    x_vec.subtract( pos_vec );
    y_vec.subtract( pos_vec );
    member_grid.setOrientation( x_vec, y_vec );
    member_grid.setCenter( pos_vec );
  }


  /**
   *  Show the progress of the calibration calculation by printing the
   *  observed lattice parameters, standard deviation in the current 
   *  function values (i.e. differences between Q theoretical and
   *  Q observed) and the current detector parameter estimates.
   *
   *  @param message  A message String to print before the progress info
   *  @param out      The PrintStream to which the message and info is
   *                  to be sent.
   */
  public void ShowProgress( String message, PrintStream out )
  {
    if ( out == null )
      return;

    out.println("==================================================");
    out.println(message);
    out.println("==================================================");
    out.println();
    out.println( "Number of evaluations = " + eval_count );
    out.println();

                                       // first show observed cell parameters
                                       // for current stage of calibration
    double my_B[][] = SCDcal_util.copy ( B_observed );
    for ( int k = 0; k < 3; k++ )
      for ( int j = 0; j < 3; j++ )
        my_B[k][j] /= (2*Math.PI);

    double cell_params[] = lattice_calc.LatticeParamsOfUB( my_B );
    out.println("Observed cell parameters now are.... ");
    for ( int i = 0; i < 3; i++ )
      out.print( " " + Format.real( cell_params[i], 10, 6 ) );
    for ( int i = 3; i < 6; i++ )
      out.print( " " + Format.real( cell_params[i], 10, 5 ) );
    out.println( " " + Format.real( cell_params[6], 10, 5 ) );

                                       // then show the standard deviation of
                                       // the error in q, from all zeros
    double index[] = new double[n_peaks];
    for ( int i = 0; i < index.length; i++ )
      index[i] = i;

    double vals[] = getValues( index );
    double sum_sq_errors = 0;
    for ( int i = 0; i < vals.length; i++ )
      sum_sq_errors += vals[i] * vals[i];

    standard_dev_in_Q = Math.sqrt( sum_sq_errors/vals.length );

    out.println();
    out.println("One standard dev error distance in Q = " + standard_dev_in_Q );
    out.println();

    double L1 = parameters[ SCDcal.L1_INDEX ];
    double t0 = parameters[ SCDcal.T0_INDEX ];
    SCDcal_util.WriteNewCalibrationInfo( out, L1, t0, grid_array );
  }



 /* -------------------------------------------------------------------------
  *
  * MAIN  ( Basic main program for testing purposes only. )
  *
  */
    public static void main(String[] args)
    {
     // Lattice Parameters for Quartz
      double lattice_params[] = new double[6];
      lattice_params[0] = 4.9138;
      lattice_params[1] = 4.9138;
      lattice_params[2] = 5.4051;
      lattice_params[3] = 90;
      lattice_params[4] = 90;
      lattice_params[5] = 120;
                                                    // load the vector of peaks
      Vector peaks = PeakData_d.ReadPeaks( args[0], null, PeakData_d.SNS_SCD ); 
      PeakData_d peak = (PeakData_d)peaks.elementAt(0);
      double l1 = peak.l1; 
      System.out.println( "l1 = " + l1 );

      Hashtable grids = new Hashtable();
      for ( int i = 0; i < peaks.size(); i++ )
      {
        UniformGrid_d grid = ((PeakData_d)peaks.elementAt(i)).grid;  
        Integer key = new Integer( grid.ID() );
        if ( grids.get(key) == null )            // new detector, so add it
        {
          grids.put( key, grid );
          System.out.println("grid = " + grid );
        }
      }
                                                   // set up the list of 
                                                   // parameters and names

      int n_params = DET_BASE_INDEX + N_DET_PARAMS * grids.size();
      System.out.println("NUMBER OF PARAMETERS = " + n_params );

      String parameter_names[]     = new String[n_params];
      parameter_names[ L1_INDEX ]  = "L1";
      parameter_names[ T0_INDEX ]  = "T0";
      parameter_names[ A_INDEX  ]  = "A(tof=A*t+T0)";
      parameter_names[ SX_INDEX ]  = "SX";
      parameter_names[ SY_INDEX ]  = "SY";
      parameter_names[ SZ_INDEX ]  = "SZ";

      double parameters[] = new double[n_params];
      parameters[ L1_INDEX ]  = l1;
      parameters[ T0_INDEX ]  = 0.0;
      parameters[ A_INDEX  ]  = 1.0;
      parameters[ SX_INDEX ]  = 0.0;
      parameters[ SY_INDEX ]  = 0.0;
      parameters[ SZ_INDEX ]  = 0.0;

      int index     = DET_BASE_INDEX;

      UniformGrid_d[] grid_arr = SCDcal_util.getAllGrids( grids );
      for ( int det_count = 0; det_count < grid_arr.length; det_count++ )
      {
        index = DET_BASE_INDEX + det_count * N_DET_PARAMS;

        UniformGrid_d grid = grid_arr[ det_count ];
        int id = grid.ID();
        parameter_names[index + DET_WIDTH_INDEX ] = "Det " + id + "    Width";
        parameters     [index + DET_WIDTH_INDEX ] = grid.width();

        parameter_names[index + DET_HEIGHT_INDEX] = "Det " + id + "   Height";
        parameters     [index + DET_HEIGHT_INDEX] = grid.height();

        parameter_names[index + DET_X_OFF_INDEX ] = "Det " + id + " x_offset";
        parameters     [index + DET_X_OFF_INDEX ] = 0;

        parameter_names[index + DET_Y_OFF_INDEX ] = "Det " + id + " y_offset";
        parameters     [index + DET_Y_OFF_INDEX ] = 0;

        parameter_names[index + DET_D_INDEX     ] = "Det " + id + " distance";
        parameters     [index + DET_D_INDEX     ] = grid.position().length();

        parameter_names[index + DET_PHI_INDEX   ] = "Det " + id + "      phi";
        parameters     [index + DET_PHI_INDEX   ] = 0;

        parameter_names[index + DET_CHI_INDEX   ] = "Det " + id + "      chi";
        parameters     [index + DET_CHI_INDEX   ] = 0;

        parameter_names[index + DET_OMEGA_INDEX ] = "Det " + id + "    omega";
        parameters     [index + DET_OMEGA_INDEX ] = 0;
      } 

      boolean is_used[] = new boolean[n_params];
      for ( int i = 0; i < n_params; i++ )
        is_used[i] = true;
                              // Insert code at this point to mark some params.
                              // First turn off any params shared by all dets.
   // is_used[ L1_INDEX ] = false;
   // is_used[ T0_INDEX ] = false;
      is_used[ A_INDEX  ] = false;
      is_used[ SX_INDEX ] = false;
      is_used[ SY_INDEX ] = false;
      is_used[ SZ_INDEX ] = false;
                              // then turn off some params for all detectors.
/*
      for ( int i = 0; i < grid_arr.length; i++ )   
      {
        index = DET_BASE_INDEX + i * N_DET_PARAMS;
    //  is_used[ index + DET_WIDTH_INDEX  ] = false;
    //  is_used[ index + DET_HEIGHT_INDEX ] = false;
    //  is_used[ index + DET_X_OFF_INDEX  ] = false;
    //  is_used[ index + DET_Y_OFF_INDEX  ] = false;
        is_used[ index + DET_D_INDEX      ] = false;
        is_used[ index + DET_PHI_INDEX    ] = false;
        is_used[ index + DET_CHI_INDEX    ] = false;
        is_used[ index + DET_OMEGA_INDEX  ] = false;
      }
*/
/*
      int[][] groups = new int[grid_arr.length][1];  // these groups all have
      for ( int i = 0; i < groups.length; i++ )      // just one detector
        groups[i][0] = grid_arr[i].ID();
*/
      int[][] groups = { { 2, 3, 4, 5, 6, 7, 8, 9, 10 } };
/*    int[][] groups = { { 2, 3, 4 }, 
                         { 5, 6, 7 }, 
                         { 8, 9, 10 } };

      int[][] groups = { { 2 }, { 3 }, { 4 }, 
                         { 5 }, { 6 }, { 7 }, 
                         { 8 }, { 9 }, { 10 } };
*/
                                                  // turn off everything but
                                                  // width & height for NON-KEY
      for ( int i = 0; i < grid_arr.length; i++ )   
      {
        int id = grid_arr[i].ID();
        boolean key_detector = false;              
        for ( int k = 0; k < groups.length; k++ )  
          if ( id == groups[k][0] )
            key_detector = true;
          
        if ( !key_detector )
        {
          index = DET_BASE_INDEX + i * N_DET_PARAMS;
    //    is_used[ index + DET_WIDTH_INDEX  ] = false;
    //    is_used[ index + DET_HEIGHT_INDEX ] = false;
          is_used[ index + DET_X_OFF_INDEX  ] = false;
          is_used[ index + DET_Y_OFF_INDEX  ] = false;
          is_used[ index + DET_D_INDEX      ] = false;
          is_used[ index + DET_PHI_INDEX    ] = false;
          is_used[ index + DET_CHI_INDEX    ] = false;
          is_used[ index + DET_OMEGA_INDEX  ] = false;
        }
      }

                              // now count the number that were used
      int n_used = 0;
      for ( int i = 0; i < n_params; i++ )
        if ( is_used[i] )
          n_used++;
                                                     // build the one variable
                                                     // function
      File log_name = new File( "SCD_Grouped_cal.log" );
      OutputStream log_os   = null;
      PrintStream  log_file = null;
      try
      {
        log_os = new FileOutputStream( log_name );
        log_file = new PrintStream( log_os );
      }
      catch ( IOException ex )
      {
        System.out.println("Can't find file " + log_name );
      }

     for ( int i = 0; i < is_used.length; i++ )
       if ( is_used[i] )
         System.out.println( "USED " + parameter_names[i] );
       else
         System.out.println( "     " + parameter_names[i] );

      SCD_Grouped_cal error_f = new SCD_Grouped_cal( peaks, 
                                     grids,
                                     groups,
                                     parameters, 
                                     parameter_names,
                                     n_used, 
                                     is_used,
                                     lattice_params,
                                     log_file ); 

      for ( int i = 0; i < parameters.length; i++ )
      {
        if ( is_used[i] )
          System.out.print("  ");
        else
          System.out.print("* ");
        System.out.println( parameter_names[i] +" = " + parameters[i] ); 
      }
      String message = "Before fit... params are";
      error_f.ShowProgress( message, System.out );
      error_f.ShowProgress( message, error_f.log_file );
                                                // build the arrays of x values
                                                // target function values 
                                                // (z_vals) and "fake"
      double z_vals[] = new double[ peaks.size() ]; 
      double sigmas[] = new double[ peaks.size() ]; 
      double x_index[] = new double[ peaks.size() ];
      for ( int i = 0; i < peaks.size(); i++ )
      {
        z_vals[i] = 0;
//      sigmas[i] = Math.sqrt( counts[i] );
        sigmas[i] = 1.0;
        x_index[i]  = i;
      }
                                           // build the data fitter and display 
                                           // the results.
      MarquardtArrayFitter fitter = 
      new MarquardtArrayFitter(error_f, x_index, z_vals, sigmas, 1.0e-12, 500);

      System.out.println( fitter.getResultsString() );

      System.out.println();
      System.out.println("RESULTS -----------------------------------------"); 
      System.out.println("observed U matrix:");
      LinearAlgebra.print( error_f.U_observed );
      System.out.println("observed B matrix:");
      LinearAlgebra.print( error_f.B_observed );
      System.out.println("observed UB matrix:");
      double UB[][] = LinearAlgebra.mult( error_f.U_observed, 
                                          error_f.B_observed );
      LinearAlgebra.print( UB );

      System.out.println("Transpose of observed UB/(2PI)");
      for ( int i = 0; i < 3; i++ )
        for ( int j = 0; j < 3; j++ )
          UB[i][j] /= (2*Math.PI);
      UB = LinearAlgebra.getTranspose( UB );
      LinearAlgebra.print( UB );

      message = "After fit... params are";
      error_f.ShowProgress( message, System.out );
      error_f.ShowProgress( message, error_f.log_file );

      double s_dev = error_f.getStandardDeviationInQ();
      SCDcal_util.WriteAllParams( System.out,
                                  parameter_names, 
                                  parameters );
    }
}
