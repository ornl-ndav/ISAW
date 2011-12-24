/* File: ScalarUtils.java 
 *
 * Copyright (C) 2011, Dennis Mikkelson
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

package Operators.TOF_SCD;

import java.util.*;
import gov.anl.ipns.MathTools.Geometry.*;


/**
 * This class contains static methods that use the ReducedCellInfo class
 * to help deterimine Conventional cells corresponding to Niggli reduced cells.
 */
public class ScalarUtils
{
  public static final float NO_CELL_FOUND = 100000;

  /**
   * Get the best fitting conventional cell, if any, with the specified
   * cell_type and centering type, using only the lattice parameters from
   * the specified UB matrix.  No related cell parameters are tested.
   *
   *  @param UB               The UB transform corresponding to a Niggli 
   *                          reduced cell for the lattice.
   *  @param cell_type        String specifiying the cell type: Cubic, 
   *                          Rhombohedral, Tetragonal, Orthorhombic,
   *                          Monoclinic, Hexagonal or Triclinic.
   *  @param centering        String specifying the centering: F, I, C, P, R 
   *  @param new_UB           This is set to the new UB matrix, if the 
   *                          function returns an error less than
   *                          ScalarUtils.NO_CELL_FOUND, and is just set to
   *                          the input UB otherwise.
   *
   *  @return The error in the cell scalars if a new UB matrix is found, 
   *          or the value SclaraUtils.NO_CELL_FOUND, if there was no
   *          match for the specified cell type and centering..
   *
   * @see getBestConventionalCellUB
   */
  private static float getConventionalCellUB( Tran3D UB, 
                                              String cell_type, 
                                              String centering, 
                                              Tran3D new_UB )
  {
    new_UB.set(UB);

    Vector3D a_vec = new Vector3D();
    Vector3D b_vec = new Vector3D();
    Vector3D c_vec = new Vector3D();
    IndexingUtils.getABC( UB, a_vec, b_vec, c_vec );
    double a = a_vec.length();
    double b = b_vec.length();
    double c = c_vec.length();
    double alpha = IndexingUtils.angle( b_vec, c_vec );
    double beta  = IndexingUtils.angle( c_vec, a_vec );
    double gamma = IndexingUtils.angle( a_vec, b_vec );

    ReducedCellInfo[] list = ReducedCellInfo.Table(a, b, c, alpha, beta, gamma);

    int best_form = ReducedCellInfo.BestMatch( list, cell_type, centering );
    if ( best_form > 0 )
    {
      double[][] cell_tran = list[best_form].getTransformation();
      float[][]  cell_tran_f = new float[3][3];
      for ( int i = 0; i < 3; i++ )
        for ( int j = 0; j < 3; j++ )
          cell_tran_f[i][j] = (float)cell_tran[i][j];
      Tran3D new_tran = new Tran3D( cell_tran_f );
      new_tran.invert();
      new_UB.multiply_by( new_tran );
      if ( !IndexingUtils.isRightHanded( new_UB ) )
      {
        IndexingUtils.getABC( new_UB, a_vec, b_vec, c_vec );
        Vector3D minus_c = new Vector3D( c_vec );
        minus_c.multiply( -1 );
        IndexingUtils.getUB ( new_UB, a_vec, b_vec, minus_c );
      }
      return (float)list[best_form].weighted_distance(list[0]);
    }
    return NO_CELL_FOUND;
  }


  /**
   *  Get the best fitting conventional cell, if any, with the specified
   *  cell_type and centering type.  In addition to the Niggli cell 
   *  corresponding to the specified UB, up to three related "almost" Niggli
   *  cells will also be used.  In particular, for each cell angle that is
   *  "near" 90 degrees, an additional cell obtained by negating the two
   *  corresponding cell edges will be tried.  This is done, since there 
   *  are errors in the data, and if a positive Niggli cell was obtained with
   *  an angle near 90 degrees, the choice of the positive cell might be in
   *  error, and the correct choice might have been a negative cell.  For
   *  example if the Niggli cell angles were 89.9, 70, 70 (a positive cell),
   *  due to errors in the data, it could be that the angles should have 
   *  been 90, 110, 110 (a negative cell).  By reversing the signs on the
   *  b and c edge vectors, the "almost" Niggli cell with angles 89.9, 110, 110
   *  will also be tested.  The small difference in scalar values due to
   *  using 89.9 degrees instead of 90 is insignificant, but the scalar values
   *  using 110 degrees for beta and gamma will be quite different
   *  from the scalar values using 70 degrees for beta and gamma.
   *
   *  @param UB               The UB transform corresponding to a Niggli 
   *                          reduced cell for the lattice.
   *  @param cell_type        String specifiying the cell type: Cubic, 
   *                          Rhombohedral, Tetragonal, Orthorhombic,
   *                          Monoclinic, Hexagonal or Triclinic.
   *  @param centering        String specifying the centering: F, I, C, P, R 
   *  @param angle_tolerance  This parameter specifies the tolerance on an
   *                          angle to be considered about 90 degrees.  If a
   *                          cell angle is within this tolerance of 90 degrees
   *                          a cell with the corresponding sides negated will
   *                          also be tested.
   *  @param new_UB           This is set to the new UB matrix, if the 
   *                          function returns true, and is just set to UB if
   *                          this function returns false.
   *
   *  @return true if a new UB matrix is found, false otherwise.
   */
  public static float getBestConventionalCellUB( Tran3D UB,
                                                 String cell_type,
                                                 String centering,
                                                 float  angle_tolerance,
                                                 Tran3D new_UB )
  {
    new_UB.set( UB );

    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();

    Vector3D a_vec = new Vector3D();
    Vector3D b_vec = new Vector3D();
    Vector3D c_vec = new Vector3D();

    IndexingUtils.getABC( UB, a_vec, b_vec, c_vec );

    Vector3D minus_a = new Vector3D( a_vec );
    Vector3D minus_b = new Vector3D( b_vec );
    Vector3D minus_c = new Vector3D( c_vec );

    minus_a.multiply( -1 );
    minus_b.multiply( -1 );
    minus_c.multiply( -1 );

    Vector3D[][] reflections = { {   a_vec,   b_vec,   c_vec },
                                 { minus_a, minus_b,   c_vec },
                                 { minus_a,   b_vec, minus_c },
                                 {   a_vec, minus_b, minus_c } };

    float alpha = IndexingUtils.angle( b_vec, c_vec );
    float beta  = IndexingUtils.angle( c_vec, a_vec );
    float gamma = IndexingUtils.angle( a_vec, b_vec );

    float[] angles = { 90, gamma, beta, alpha };

    float error;
    float min_error = NO_CELL_FOUND;
    Tran3D temp_UB  = new Tran3D();
    Tran3D conv_UB  = new Tran3D();

    for ( int row = 0; row < reflections.length; row++ )
    {
      if ( Math.abs(angles[row] - 90) < angle_tolerance )
      {
        a.set( reflections[row][0] );
        b.set( reflections[row][1] );
        c.set( reflections[row][2] );

        IndexingUtils.getUB( temp_UB, a, b, c );

        error = getConventionalCellUB(temp_UB, cell_type, centering, conv_UB);

        if ( error < min_error )
        {
          min_error = error;
          new_UB.set( conv_UB );

          if ( ReducedCellInfo.ORTHORHOMBIC.startsWith( cell_type ) )
            SetSidesIncreasing( new_UB );

          else if ( ReducedCellInfo.TETRAGONAL.startsWith( cell_type ) )
            StandardizeTetragonal( new_UB );

          else if ( ReducedCellInfo.HEXAGONAL.startsWith( cell_type ) ||
                    ReducedCellInfo.RHOMBOHEDRAL.startsWith( cell_type ) )
            StandardizeHexagonal( new_UB );
        }
      }
    }
    return min_error;
  }


  /**
   *  Change UB to a new matrix corresponding to a unit cell with the sides
   *  in increasing order of magnitude.  This is used to arrange the UB matrix
   *  for an orthorhombic cell into a standard order.
   *  @param UB on input this should correspond to an orthorhombic cell. 
   *            On output, it will correspond to an orthorhombic cell with
   *            sides in increasing order.
   */
  private static void SetSidesIncreasing( Tran3D UB )
  {
    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();
    IndexingUtils.getABC( UB, a, b, c );
    Vector<Vector3D> edges = new Vector<Vector3D>();
    edges.add( a );
    edges.add( b );
    edges.add( c );
    edges = IndexingUtils.SortOnVectorMagnitude( edges ); 
    
    a = edges.elementAt(0);
    b = edges.elementAt(1);
    c = edges.elementAt(2);

    Vector3D cross = new Vector3D();
    cross.cross( a, b );
    if ( cross.dot( c ) < 0 )     // if left handed, reflect the c vector
      c.multiply(-1);

    IndexingUtils.getUB( UB, a, b, c );   
  }


  /**
   *  Change UB to a new matrix corresponding to a unit cell with the first 
   *  two sides approximately equal in magnitude.  This is used to arrange 
   *  the UB matrix for a tetragonal cell into a standard order.
   *
   *  @param UB on input this should correspond to a tetragonal cell.  
   *            On output, it will correspond to a tetragonal cell with the 
   *            first two sides, a and b, set to the two sides that are most
   *            nearly equal in length. 
   */
  private static void StandardizeTetragonal( Tran3D UB )
  {
    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();
    IndexingUtils.getABC( UB, a, b, c );

    float a_b_diff = Math.abs( a.length() - b.length() ) / 
                     Math.min( a.length(), b.length() );

    float a_c_diff = Math.abs( a.length() - c.length() ) / 
                     Math.min( a.length(), c.length() );

    float b_c_diff = Math.abs( b.length() - c.length() ) / 
                     Math.min( b.length(), c.length() );

                          // if needed, change UB to have the two most nearly
                          // equal sides first.
    if ( a_c_diff <= a_b_diff && a_c_diff <= b_c_diff )  
      IndexingUtils.getUB( UB, c, a, b );
    else if ( b_c_diff <= a_b_diff && b_c_diff <= a_c_diff )
      IndexingUtils.getUB( UB, b, c, a );
  }


  /**
   *  Change UB to a new matrix corresponding to a hexagonal unit cell 
   *  angles approximately 90, 90, 120.  This is used to arrange 
   *  the UB matrix for a hexagonal or rhombohedral cell into a standard order.
   *
   *  @param UB on input this should correspond to a hexagonal or rhombohedral
   *            On output, it will correspond to a hexagonal cell with angles
   *            approximately 90, 90, 120.
   */
  private static void StandardizeHexagonal( Tran3D UB )
  {
    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();
    IndexingUtils.getABC( UB, a, b, c );

    float alpha = IndexingUtils.angle( b, c );
    float beta  = IndexingUtils.angle( c, a );
                                                // first, make the non 90 
                                                // degree angle last
    if ( Math.abs(alpha-90) > 20 )
      IndexingUtils.getUB( UB, b, c, a );
    else if ( Math.abs(beta-90) > 20 )
      IndexingUtils.getUB( UB, c, a, b );

                                                // if the non 90 degree angle
                                                // is about 60 degrees, make
                                                // it about 120 degrees.
    IndexingUtils.getABC( UB, a, b, c );
    float gamma = IndexingUtils.angle( a, b );
    if ( Math.abs( gamma - 60 ) < 10 )
    {
      a.multiply( -1 );                         // reflect a and c to change
      c.multiply( -1 );                         // alpha and gamma to their
      IndexingUtils.getUB( UB, a, b, c );       // supplementary angle
    }
  }


  /**
   *  Get a list of all the UB matrices corresponding to possible conventional 
   *  cells for the specified UB, for which the error computed in the 
   *  ReducedCellInfo class is less than the specified cutoff value.  Each
   *  possible cell type and centering is passed in to the 
   *  getConventionalCellUB method and the resulting matrix is kept if the
   *  error returned is less than the cutoff value.
   *
   *  @param UB      The original UB matrix that corresponds to a Niggli
   *                 reduced cell. 
   *  @param cutoff  The error in the cell scalars must not exceed this
   *                 cutoff value.
   *  @param angle_tolerance  If a cell angle is within this tolerance of
   *                          90 degrees, a related cell, with the 
   *                          corresponding sides negated will also be
   *                          tested.
   *
   *  @return A Vector of Tran3D objects that holds the list of conventional
   *          cell UB matrices.
   */
  public static Vector<Tran3D>getConventionalCellUB_List(Tran3D UB, 
                                                         float cutoff,
                                                         float angle_tolerance)
  {
    Vector<Tran3D> UBs = new Vector<Tran3D>();

    String[] types = { ReducedCellInfo.CUBIC,
                       ReducedCellInfo.HEXAGONAL,
                       ReducedCellInfo.RHOMBOHEDRAL,
                       ReducedCellInfo.TETRAGONAL,
                       ReducedCellInfo.ORTHORHOMBIC,
                       ReducedCellInfo.MONOCLINIC,
                       ReducedCellInfo.TRICLINIC };

    String[] centerings = { ReducedCellInfo.F_CENTERED,
                            ReducedCellInfo.I_CENTERED,
                            ReducedCellInfo.C_CENTERED,
                            ReducedCellInfo.P_CENTERED,
                            ReducedCellInfo.R_CENTERED };

    System.out.println();
    float error;
    for ( int i = 0; i < types.length; i++ )
      for ( int j = 0; j < centerings.length; j++ )
      {
        Tran3D new_tran = new Tran3D();

        error = getBestConventionalCellUB( UB,
                                           types[i],
                                           centerings[j],
                                           angle_tolerance,
                                           new_tran );
        if ( error < cutoff )
        {
          UBs.add( new_tran );
          System.out.printf( "%-13s %10s %6.4f  ", 
                             types[i], centerings[j], error );
          IndexingUtils.ShowLatticeParameters( new_tran );
        }
    }

    System.out.println();

    return UBs;
  }


  /**
   *  Get the UB matrix corresponding to a unit cell with the highest symmetry
   *  with cell scalars that match the specified unit cell within the given
   *  cutoff.
   *
   *  @param UB       The original UB matrix that corresponds to a Niggli 
   *                  reduced cell. 
   *  @param cutoff   Maximum allowed error in the cell scalars. 
   *  @param new_UB   This will be set to the new UB matrix if the
   *                  returned error is less than NO_CELL_FOUND.
   *
   *  @return The error in the cell parameters, or ScalarUtils.NO_CELL_FOUND
   *          if no match was obtained.
   */
  public static float getConventionalCellUB(Tran3D UB,
                                            float  cutoff,
                                            Tran3D new_UB )
  {
    new_UB.set(UB);

    String[] types = { ReducedCellInfo.CUBIC,
                       ReducedCellInfo.HEXAGONAL,
                       ReducedCellInfo.RHOMBOHEDRAL,
                       ReducedCellInfo.TETRAGONAL,
                       ReducedCellInfo.ORTHORHOMBIC,
                       ReducedCellInfo.MONOCLINIC,
                       ReducedCellInfo.TRICLINIC };

    String[] centerings = { ReducedCellInfo.F_CENTERED,
                            ReducedCellInfo.I_CENTERED,
                            ReducedCellInfo.C_CENTERED,
                            ReducedCellInfo.P_CENTERED,
                            ReducedCellInfo.R_CENTERED };

    String type      = null;
    String centering = null;

    float  error;
    float  min_error = NO_CELL_FOUND;
    float  angle_tol = 1;
    Tran3D temp_UB   = new Tran3D();

    boolean done = false; 
    int i = 0;
    while ( !done && i < types.length )  
    {
      for ( int j = 0; j < centerings.length; j++ )
      {
        error = getBestConventionalCellUB( UB,
                                           types[i],
                                           centerings[j],
                                           angle_tol,
                                           temp_UB );
        if ( error < min_error )
        {
          min_error = error;
          new_UB.set( temp_UB );
          type      = types[i];
          centering = centerings[j];
        }
      }
      i++; 
      if ( min_error < cutoff )
        done = true;
    }

    if ( min_error < NO_CELL_FOUND )
    {
      System.out.printf( "%-13s %10s %6.4f  ",
                          type, centering, min_error );
      IndexingUtils.ShowLatticeParameters( new_UB );
      System.out.println();
    }

    return min_error;
  }



/**
 * THE FOLLOWING METHOD IS JUST EXPERIMENTAL, NOT COMPLETE OR CORRECT.
 */

  public static float getReflectedConventionalCellUB( Tran3D UB,
                                                      String cell_type,
                                                      String centering,
                                                      Tran3D new_UB )
  {
    float min_error = NO_CELL_FOUND;
    float error;

    new_UB.set( UB );

    Vector3D a     = new Vector3D();
    Vector3D b     = new Vector3D();
    Vector3D c     = new Vector3D();
    Tran3D refl_UB = new Tran3D();
    Tran3D temp_UB = new Tran3D();

    for ( int count = 0; count <= 3; count++ )
    {
      IndexingUtils.getABC( UB, a, b, c );
                                 // no reflection if count == 0 
      if ( count == 1 )          // reflect a, b
      {
        a.multiply( -1 );
        b.multiply( -1 );
      }
      else if ( count == 2 )     // reflect b, c
      {
        b.multiply( -1 );
        c.multiply( -1 );
      }
      else if ( count == 3 )     // reflect c, a
      {
        c.multiply( -1 );
        a.multiply( -1 );
      }
      IndexingUtils.getUB( refl_UB, a, b, c );
      error = getConventionalCellUB( refl_UB, cell_type, centering, temp_UB );
      if ( error < min_error )
      {
        min_error = error;
        new_UB.set( temp_UB );
      }
    }
    return min_error;
  }


/**
 * THE FOLLOWING METHOD IS JUST EXPERIMENTAL, NOT COMPLETE OR CORRECT.
 */
  public static float getSwappedConventionalCellUB( Tran3D UB,
                                                    String cell_type,
                                                    String centering,
                                                    Tran3D new_UB )
  {
    float min_error = NO_CELL_FOUND;
    float error;

    new_UB.set( UB );

    Vector3D a   = new Vector3D();
    Vector3D b   = new Vector3D();
    Vector3D c   = new Vector3D();
    IndexingUtils.getABC( UB, a, c, b );

    Vector3D minus_a = new Vector3D(a);
    Vector3D minus_b = new Vector3D(b);
    Vector3D minus_c = new Vector3D(c);

    minus_a.multiply( -1 );
    minus_b.multiply( -1 );
    minus_c.multiply( -1 );

    Tran3D swapped_UB = new Tran3D();
    Tran3D temp_UB    = new Tran3D();

      for ( int count = 0; count < 6; count++ )            // swap all sides
      {
        if ( count == 0 )
          IndexingUtils.getUB( swapped_UB, a, b, c );
        else if ( count == 1 )
          IndexingUtils.getUB( swapped_UB, minus_a, c, b );
        else if ( count == 2 )
          IndexingUtils.getUB( swapped_UB, b, c, a );
        else if ( count == 3 )
          IndexingUtils.getUB( swapped_UB, minus_b, a, c );
        else if ( count == 4 )
          IndexingUtils.getUB( swapped_UB, c, a, b );
        else if ( count == 5 )
          IndexingUtils.getUB( swapped_UB, minus_c, b, a );

        error = getReflectedConventionalCellUB( swapped_UB,
                                                cell_type,
                                                centering,
                                                temp_UB );
        if ( error < min_error )
        {
          min_error = error;
          new_UB.set( temp_UB );
        }
      }

    return min_error;
  }


/**
 * THE FOLLOWING METHOD IS JUST EXPERIMENTAL, NOT COMPLETE OR CORRECT.
 */
  public static float getBestConventionalCellUB_PERMUTE( Tran3D UB,
                                                         String cell_type,
                                                         String centering,
                                                         Tran3D new_UB )
  {
    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();

    Vector3D a_vec = new Vector3D();
    Vector3D b_vec = new Vector3D();
    Vector3D c_vec = new Vector3D();

    IndexingUtils.getABC( UB, a_vec, b_vec, c_vec );

    Vector3D minus_a = new Vector3D( a_vec );
    Vector3D minus_b = new Vector3D( b_vec );
    Vector3D minus_c = new Vector3D( c_vec );

    minus_a.multiply( -1 );
    minus_b.multiply( -1 );
    minus_c.multiply( -1 );

    Vector3D[][] permutations = { {   a_vec,   b_vec,   c_vec },
                                  { minus_a,   c_vec,   b_vec },
                                  {   b_vec,   c_vec,   a_vec },
                                  { minus_b,   a_vec,   c_vec },
                                  {   c_vec,   a_vec,   b_vec },
                                  { minus_c,   b_vec,   a_vec } };
    float min_error = NO_CELL_FOUND;
    float error;
    float factor = 1.05f;
    for ( int row = 0; row < 6; row++ )
    {
      a.set( permutations[row][0] );
      b.set( permutations[row][1] );
      c.set( permutations[row][2] );
      if ( a.length() < factor * b.length() &&
           b.length() < factor * c.length() )   // could be Niggli
      {
        Tran3D temp_UB   = new Tran3D();
        Tran3D niggli_UB = new Tran3D();
        Tran3D conv_UB   = new Tran3D();
        IndexingUtils.getUB( temp_UB, a, b, c );
        System.out.print("TRYING ");
        IndexingUtils.ShowLatticeParameters( temp_UB );
        if ( IndexingUtils.ChooseUB_WithNiggliAngles( temp_UB, niggli_UB ) )
        {
          System.out.print("NIGGLI ");
          IndexingUtils.ShowLatticeParameters( niggli_UB );
          error = getConventionalCellUB( niggli_UB,
                                         cell_type, centering, conv_UB );
          if ( error < min_error )
          {
            min_error = error;
            new_UB.set( conv_UB );
            System.out.print("KEEP   ");
            IndexingUtils.ShowLatticeParameters( new_UB );
          }
        }
      }
    }
    return min_error;
  }



  /**
   *  THE FOLLOWING METHOD IS JUST EXPERIMENTAL, NOT COMPLETE OR CORRECT.
   *  Get a list of all the UB matrices corresponding to conventional cells
   *  obtained from permutations and reflections of real-space cell edge 
   *  vectors for the specified UB that are right-handed.   Each possible 
   *  cell type and centering is passed in to the getSwappedConventionalCellUB 
   *  method and the resulting matrix is kept if the error is less than the
   *  specified cutoff value.
   *
   *  @param UB      The original UB matrix that corresponds to a Niggli 
   *                 reduced cell. 
   *  @param cutoff  The error in the cell scalars must not exceed this
   *                 cutoff value.
   *
   *  @return A Vector of Tran3D objects that holds the list of conventional
   *          cell UB matrices.
   */
  public static Vector<Tran3D>getSwappedConventionalCellUBs( Tran3D UB,
                                                             float  cutoff )
  {
    Vector<Tran3D> UBs = new Vector<Tran3D>();

    String[] types = { ReducedCellInfo.CUBIC,
                       ReducedCellInfo.HEXAGONAL,
                       ReducedCellInfo.RHOMBOHEDRAL,
                       ReducedCellInfo.TETRAGONAL,
                       ReducedCellInfo.ORTHORHOMBIC,
                       ReducedCellInfo.MONOCLINIC,
                       ReducedCellInfo.TRICLINIC };

    String[] centerings = { ReducedCellInfo.F_CENTERED,
                            ReducedCellInfo.I_CENTERED,
                            ReducedCellInfo.C_CENTERED,
                            ReducedCellInfo.P_CENTERED,
                            ReducedCellInfo.R_CENTERED };

    System.out.println();
    float error;
    for ( int i = 0; i < types.length; i++ )
      for ( int j = 0; j < centerings.length; j++ )
      {
        Tran3D new_tran = new Tran3D();

        error = getSwappedConventionalCellUB( UB,
                                              types[i],
                                              centerings[j],
                                              new_tran );
        if ( error < cutoff )
        {
          UBs.add( new_tran );
          System.out.printf( "%-13s %10s %6.4f  ", 
                             types[i], centerings[j], error );
          IndexingUtils.ShowLatticeParameters( new_tran );
        }
    }

    System.out.println();

    return UBs;
  }


}
