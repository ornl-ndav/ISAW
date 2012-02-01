/* File: ConventionalCellInfo.java 
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
 *  This class is a convenience class to hold information about a 
 *  selected conventional cell, such as the cell type, centering, form
 *  number, transformation, etc.
 */
public class ConventionalCellInfo
{
  private int    form_num;
  private float  scalars_error;
  private String cell_type;
  private String centering;
  private Tran3D original_UB;
  private Tran3D adjusted_UB;

  /**
   *  Construct a conventional cell info object corresponding to the 
   *  specified UB matrix and form number.
   *
   *  @param UB       The orientation transformation used when the 
   *                  conventional cell was selected.  NOTE: This might
   *                  not be the original matrix corresponding to a Niggli
   *                  cell, but might correspond to a related cell with two
   *                  edges of the Niggli cell reflected.
   *
   *  @param form_num The number of the form corresponding to the
   *                  desired conventional cell.
   */
  public ConventionalCellInfo( Tran3D UB, int form_number )
  {
    float[] lat_par = IndexingUtils.getLatticeParameters( UB );
    ReducedCellInfo form_0 = new ReducedCellInfo( 0,
                                          lat_par[0], lat_par[1], lat_par[2],
                                          lat_par[3], lat_par[4], lat_par[5] );
    ReducedCellInfo form_i = new ReducedCellInfo( form_number,
                                          lat_par[0], lat_par[1], lat_par[2],
                                          lat_par[3], lat_par[4], lat_par[5] );
    init( UB, form_0, form_i );
  }


  /**
   *  Initialize this conventional cell info object using the 
   *  specified UB matrix, base form (form_0) and selected form (form_i).
   *
   *  @param UB      The orientation transformation used when the 
   *                 conventional cell was selected.  NOTE: This might
   *                 not be the original matrix corresponding to a Niggli
   *                 cell, but might correspond to a related cell with two
   *                 edges of the Niggli cell reflected.
   *  @param form_0  The 0th form used for comparison, constructed from the
   *                 lattice parameters of UB.
   *  @param form_i  The form corresponding to the selected conventional cell.
   *
   */
  private void init( Tran3D          UB, 
                     ReducedCellInfo form_0, 
                     ReducedCellInfo form_i )
  {
    form_num = form_i.getFormNum();
    scalars_error = (float)form_0.weighted_distance( form_i );
    cell_type = form_i.getCellType();
    centering = form_i.getCentering();
    original_UB = new Tran3D( UB );

    adjusted_UB = new Tran3D( UB );
    double[][] cell_tran = form_i.getTransformation();
    float[][]  cell_tran_f = new float[3][3];
    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        cell_tran_f[i][j] = (float)cell_tran[i][j];

    Tran3D new_tran = new Tran3D( cell_tran_f );
    new_tran.invert();
    adjusted_UB.multiply_by( new_tran );

/*                             // Force UB to right-handed.  This should not
 *                             // be necessary.
    if ( !IndexingUtils.isRightHanded( adjusted_UB ) )
    {
      Vector3D a_vec = new Vector3D();
      Vector3D b_vec = new Vector3D();
      Vector3D c_vec = new Vector3D();
      IndexingUtils.getABC( adjusted_UB, a_vec, b_vec, c_vec );
      Vector3D minus_c = new Vector3D( c_vec );
      minus_c.multiply( -1 );
      IndexingUtils.getUB ( adjusted_UB, a_vec, b_vec, minus_c );
    }
*/
    if ( ReducedCellInfo.ORTHORHOMBIC.startsWith( cell_type ) )
      SetSidesIncreasing( adjusted_UB );

    else if ( ReducedCellInfo.TETRAGONAL.startsWith( cell_type ) )
      StandardizeTetragonal( adjusted_UB );

    else if ( ReducedCellInfo.HEXAGONAL.startsWith( cell_type ) ||
              ReducedCellInfo.RHOMBOHEDRAL.startsWith( cell_type ) )
     StandardizeHexagonal( adjusted_UB );
  }


  /**
   *  Change UB to a new matrix corresponding to a unit cell with the sides
   *  in increasing order of magnitude.  This is used to arrange the UB matrix
   *  for an orthorhombic cell into a standard order.
   *
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
   *  Change UB to a new matrix corresponding to a hexagonal unit cell with
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
   * Get the form number specified when this was constructed.
   * @return the form number
   */
  public int getFormNum()
  {
    return form_num;
  }

  /**
   * Get the measure of error for the scalars for this cell.
   *
   * @return the error in the scalars for this form
   */
  public float getError()
  {
    return scalars_error;
  }

  /**
   *  Get the cell type for this conventional cell.
   */
  public String getCellType()
  {
    return cell_type;
  }

  /**
   *  Get the centering for this conventional cell.
   */
  public String getCentering()
  {
    return centering;
  }


  /**
   *  Get a copy of the orientation matrix used to construct this conventional
   *  cell.
   *  @return a copy of the original orientation matrix.
   */
  public Tran3D getOriginalUB()
  {
    return new Tran3D( original_UB );
  }

  /**
   *  Get a copy of the new orientation matrix that will index the
   *  peaks, corresponding to the specified conventional cell (form number).
   *  @return a copy of the original orientation matrix.
   */
  public Tran3D getNewUB()
  {
    return new Tran3D( adjusted_UB );
  }


  /**
   *  Get the sum of the sides, |a|+|b|+|c| of the conventional cell.
   *
   *  @return The sum of the sides of the conventional cell.
   */
  public float getSumOfSides()
  {
    float[] l_par = IndexingUtils.getLatticeParameters( adjusted_UB );
    return l_par[0] + l_par[1] + l_par[2];
  }

  public String toString()
  {
    String result = String.format( "Form # %2d  %-13s %10s %11.7f ",
                  getFormNum(), getCellType(), getCentering(), getError() );
                  

    float[] l_par = IndexingUtils.getLatticeParameters( adjusted_UB ); 
    result += String.format("%8.4f %8.4f %8.4f  %8.4f %8.4f %8.4f  %8.4f",
        l_par[0], l_par[1], l_par[2], l_par[3], l_par[4], l_par[5], l_par[6] );

//  result += " RH = " + IndexingUtils.isRightHanded( adjusted_UB );
    return result; 
  }

}
