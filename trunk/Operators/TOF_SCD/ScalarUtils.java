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


  public static float getConventionalCellUB( Tran3D UB, 
                                             String cell_type, 
                                             String centering, 
                                             Tran3D new_UB )
  {
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

    new_UB.set(UB);
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
   *  @param angle_tolerance
   *  @param new_UB
   *
   *  @return
   */
  public static float getBestConventionalCellUB( Tran3D UB,
                                                 String cell_type,
                                                 String centering,
                                                 float  angle_tolerance,
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
//          System.out.print("KEEP   ");
//          IndexingUtils.ShowLatticeParameters( new_UB );
        }
      }
    }
    return min_error;
  }



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
  public static Vector<Tran3D>getConventionalCellUBs( Tran3D UB, 
                                                      float cutoff,
                                                      float angle_tolerance )
  {
    Vector<Tran3D> UBs = new Vector<Tran3D>();

    String[] types = { ReducedCellInfo.CUBIC,
                       ReducedCellInfo.RHOMBOHEDRAL,
                       ReducedCellInfo.TETRAGONAL,
                       ReducedCellInfo.ORTHORHOMBIC,
                       ReducedCellInfo.MONOCLINIC,
                       ReducedCellInfo.HEXAGONAL,
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
                       ReducedCellInfo.RHOMBOHEDRAL,
                       ReducedCellInfo.TETRAGONAL,
                       ReducedCellInfo.ORTHORHOMBIC,
                       ReducedCellInfo.MONOCLINIC,
                       ReducedCellInfo.HEXAGONAL,
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
