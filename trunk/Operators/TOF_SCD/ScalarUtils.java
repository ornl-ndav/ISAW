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
 *
 * Possible public methods to get:
 *
 * getCells(UB,ty,ce)        - all cells, with specified type and centering,   
 *                             for all reflections, best per form, with
 *                             error < cutoff
 *
 * getCellsUB_Only(UB,ty,ce) - all cells, with specified type and centering,
 *                             for UB only (no reflections), best per form 
 *                             error < cutoff
 *
 * getCells(UB,flag)         - all cells, optionally including triclinic
 *                             for all reflections, best per form, with
 *                             error < cutoff
 *
 * removeBadForms            - remove cells in list that don't match well
 *
 * removeHighErrorForms      - remove cells in list that have error > cutoff
 *
 * getCellForForm            - one cell, for specified form,    
 *                             for all reflections, best per form, no cutoff
 *
 * getCellBestNonTri ?       - one cell, any non-triclinic type 
 *                             for all reflections, best error
 *
 * getCellHighestSym ?       - one cell,  
 *                             for all reflections, highest symmetry,      
 *
 * getCellShortestSides(l)   - one cell, shortest sum of sides, 
 *                             for all reflections 
 *
 */
public class ScalarUtils
{
  public static final float NO_CELL_FOUND = 100000;

  public static final String[] BRAVAIS_TYPE = 
                                {
                                  ReducedCellInfo.CUBIC,         // F
                                  ReducedCellInfo.CUBIC,         // I
                                  ReducedCellInfo.CUBIC,         // P

                                  ReducedCellInfo.HEXAGONAL,     // P

                                  ReducedCellInfo.RHOMBOHEDRAL,  // R

                                  ReducedCellInfo.TETRAGONAL,    // I
                                  ReducedCellInfo.TETRAGONAL,    // P

                                  ReducedCellInfo.ORTHORHOMBIC,  // F
                                  ReducedCellInfo.ORTHORHOMBIC,  // I
                                  ReducedCellInfo.ORTHORHOMBIC,  // C
                                  ReducedCellInfo.ORTHORHOMBIC,  // P

                                  ReducedCellInfo.MONOCLINIC,    // C
                                  ReducedCellInfo.MONOCLINIC,    // I
                                  ReducedCellInfo.MONOCLINIC,    // P

                                  ReducedCellInfo.TRICLINIC      // P
                                };

   public static final String[] BRAVAIS_CENTERING = 
                                 {
                                   ReducedCellInfo.F_CENTERED,  // cubic
                                   ReducedCellInfo.I_CENTERED,  // cubic
                                   ReducedCellInfo.P_CENTERED,  // cubic

                                   ReducedCellInfo.P_CENTERED,  // hexagonal

                                   ReducedCellInfo.R_CENTERED,  // rhombohedral 

                                   ReducedCellInfo.I_CENTERED,  // tetragonal
                                   ReducedCellInfo.P_CENTERED,  // tetragonal

                                   ReducedCellInfo.F_CENTERED,  // orthorhombic
                                   ReducedCellInfo.I_CENTERED,  // orthorhombic
                                   ReducedCellInfo.C_CENTERED,  // orthorhombic
                                   ReducedCellInfo.P_CENTERED,  // orthorhombic

                                   ReducedCellInfo.C_CENTERED,  // monoclinic 
                                   ReducedCellInfo.I_CENTERED,  // monoclinic 
                                   ReducedCellInfo.P_CENTERED,  // monoclinic 

                                   ReducedCellInfo.P_CENTERED   // triclinic
                                 };


  /**
   *  Get a list of cell info objects, corresponding to all forms that match
   *  the specified UB, or related UBs, with pairs of edges reflected.  If the
   *  same form number matches several times when different pairs of edges are
   *  reflected, only the one with the smallest error value will be included.
   *  A pair of edges will be reflected if the angle between the edges 
   *  is within 2 degrees of 90 degrees.  This is needed to take care
   *  of the case where a positive Niggli cell was found, but due to errors in
   *  the data, a negative Niggli cell should have been found, and visa-versa. 
   *
   *  @param UB         The lattice parameters for this UB matrix and matrices
   *                    related to it by reflecting pairs of sides, are 
   *                    used to form the list of possible conventional cells.
   *  @param cell_type  String specifying the cell type, as listed in the
   *                    ReducedCellInfo class.
   *  @param centering  String specifying the centering, as listed in the
   *                    ReducedCellInfo class.
   *  @return a vector of conventional cell info objects, corresponding to the
   *          best matching forms for UB and cells related to UB by reflections
   *          of pairs of cell edges.
   */
  public static Vector<ConventionalCellInfo> getCells( Tran3D UB,
                                                       String cell_type,
                                                       String centering )
  {
    float angle_tolerance = 2.0f;

    Vector<ConventionalCellInfo> result = new Vector<ConventionalCellInfo>();
    Vector<ConventionalCellInfo> temp   = new Vector<ConventionalCellInfo>();

    Vector<Tran3D> UB_list = getSignRelatedUBs( UB, angle_tolerance );

    for ( int k = 0; k < UB_list.size(); k++ )
    {
      temp = getCellsUB_Only( UB_list.elementAt(k), cell_type, centering );

      for ( int i = 0; i < temp.size(); i++ )
        addIfBest( result, temp.elementAt(i) );
    }
    return result;
  }


  /**
   *  Get a list of cell info objects, corresponding to all forms that match
   *  the specified UB, or related UBs, with pairs of edges reflected.  If the
   *  same form number matches several times when different pairs of edges are
   *  reflected, only the one with the smallest error value will be included.
   *  A pair of edges will be reflected if the angle between the edges 
   *  is within 2 degrees of 90 degrees.  This is needed to take care
   *  of the case where a positive Niggli cell was found, but due to errors in
   *  the data, a negative Niggli cell should have been found, and visa-versa. 
   *
   *  @param UB         The lattice parameters for this UB matrix and matrices
   *                    related to it by reflecting pairs of sides, are 
   *                    used to form the list of possible conventional cells.
   *  @param triclinic  Flag indicating whether or not to include triclinic 
   *                    cells.  Set true to include triclinic cells and false
   *                    to omit triclinic cells.
   *  @return a vector of conventional cell info objects, corresponding to the
   *          best matching forms for UB and cells related to UB by reflections
   *          of pairs of cell edges.
   */

  public static Vector<ConventionalCellInfo> getCells( Tran3D  UB,
                                                       boolean triclinic )
  {
    Vector<ConventionalCellInfo> list = new Vector<ConventionalCellInfo>();

    Vector<ConventionalCellInfo> temp = new Vector<ConventionalCellInfo>();

    int num_lattices;
    if ( triclinic )
      num_lattices = BRAVAIS_TYPE.length;
    else
      num_lattices = BRAVAIS_TYPE.length - 1;

    for ( int i = 0; i < num_lattices; i++ )
    {
      temp = getCells( UB, BRAVAIS_TYPE[i], BRAVAIS_CENTERING[i] );
      if ( temp != null )
        for ( int k = 0; k < temp.size(); k++ )
          addIfBest( list, temp.elementAt(k) );
    }

    return list;
  }


  /**
   *  Get a list of cell info objects that correspond to the specific given
   *  UB matrix.  The list will have at most one instance of any matching form
   *  since only the specified UB matrix is used (no reflections or other 
   *  related cell modifications).  For any form with the specified cell type
   *  and centering, a cell info object will be added to the list, regardless
   *  of the error in cell scalars.  As a result, the list returned by this
   *  method will often have forms that don't fit well.  However, the list 
   *  will be a complete list of matching forms for the specified UB, cell_type
   *  and centering.  Poorly matching entries can be removed subsequently by
   *  the calling code, if need be.
   *
   *  @param UB         The lattice parameters for this UB matrix are 
   *                    used to form the list of possible conventional cells.
   *  @param cell_type  String specifying the cell type, as listed in the
   *                    ReducedCellInfo class.
   *  @param centering  String specifying the centering, as listed in the
   *                    ReducedCellInfo class.
   */
  public static Vector<ConventionalCellInfo> getCellsUB_Only
                                                         ( Tran3D UB,
                                                           String cell_type,
                                                           String centering )
  {
    Vector<ConventionalCellInfo> result = new Vector<ConventionalCellInfo>();

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

    boolean centering_OK;
    boolean cell_type_OK;
    for ( int i = 1; i < list.length; i++ )
    {
      if ( centering == null || list[i].getCentering().startsWith( centering ))
        centering_OK = true;
      else
        centering_OK = false;

      if ( cell_type == null || 
           list[i].getCellType().equalsIgnoreCase(cell_type))
        cell_type_OK = true;
      else
        cell_type_OK = false;

      if ( centering_OK && cell_type_OK )
      {
        ConventionalCellInfo cell_info =
                            new ConventionalCellInfo( UB, list[0], list[i] );
        result.add( cell_info );
      }
    }

    return result;
  }


  /**
   *  Add the conventional cell info record to the list if there is not
   *  already an entry with the same form number, or replace the entry
   *  that has the same form number with the specified cell info, if
   *  the specified cell info has a smaller error.
   *
   *  @param list   The initial list of cell info objects.
   *  @param info   The new cell info object that might be added to the list.
   */
  public static void addIfBest( Vector<ConventionalCellInfo> list, 
                                ConventionalCellInfo         info )
  {
    int     form_num  = info.getFormNum();
    float   new_error = info.getError();
    boolean done = false;
    int     i = 0;
    while ( !done && i < list.size() )
    {
      ConventionalCellInfo list_info = list.elementAt(i);
      if ( list_info.getFormNum() == form_num )  // if found, replace if better
      {                  
        done = true;
        if ( list_info.getError() > new_error )
          list.set( i, info );
      }
      else
        i++;
    }

    if ( !done )                          // if never found, add to end of list
      list.add( info );
  }

  
  /**
   *  Remove any forms from the list that have errors that are too large. 
   *  An error is considered to be too large if it is at least 50 times 
   *  larger than the minimum positive error in the list.  If the max
   *  error is zero, the list will not be altered.
   */
  public static void removeBadForms( Vector<ConventionalCellInfo> list )
  {
    if ( list.size() <= 0 )
      return;
    
    String type;
    float  error;
    float  min_error = Float.POSITIVE_INFINITY;
    float  max_error = 0;

    for ( int i = 0; i < list.size(); i++ )
    {
      error = list.elementAt(i).getError();
      type  = list.elementAt(i).getCellType();
      if ( !type.equals(ReducedCellInfo.TRICLINIC) && error < min_error )
        min_error = error;
      if ( error > max_error )
        max_error = error;
    }

    if ( max_error <= 0 )
      return;

    float threshold;
    threshold = 50 * min_error;

    Vector<ConventionalCellInfo> new_list = new Vector<ConventionalCellInfo>();
    for ( int i = 0; i < list.size(); i++ )
      if ( list.elementAt(i).getError() <= threshold )
        new_list.add( list.elementAt(i) );

    list.clear();
    for ( int i = 0; i < new_list.size(); i++ )
      list.add( new_list.elementAt(i) );
  }


  /**
   * Get the cell info object that has the shortest sum of sides |a|+|b|+|c|
   * of any of the cells in the list.  If the list is empty, this returns null. 
   *
   * @param list  The list of conventional cell info objects.
   *
   * @return The entry in the list with the shortest sum of sides.
   */
  public static ConventionalCellInfo getCellShortestSides( 
                                            Vector<ConventionalCellInfo> list )
  {
    if ( list == null || list.size() == 0 )
      return null;

    if ( list.size() == 1 )
      return list.elementAt(0);

    ConventionalCellInfo best_cell = list.elementAt(0);
    float min_sum = best_cell.getSumOfSides();

    for ( int i = 1; i < list.size(); i++ )
    {
      float sum = list.elementAt(i).getSumOfSides();
      if ( sum < min_sum )
      {
        min_sum   = sum;
        best_cell = list.elementAt(i);
      }
    }
    return best_cell; 
  }


  /**
   */
  public static ConventionalCellInfo getBestConventionalCellInfo( Tran3D UB )
  {
    Vector<ConventionalCellInfo> list = new Vector<ConventionalCellInfo>();

    String[] types = { ReducedCellInfo.CUBIC,
                       ReducedCellInfo.HEXAGONAL,
                       ReducedCellInfo.RHOMBOHEDRAL,
                       ReducedCellInfo.TETRAGONAL,
                       ReducedCellInfo.ORTHORHOMBIC,
                       ReducedCellInfo.MONOCLINIC };

    String[] centerings = { ReducedCellInfo.F_CENTERED,
                            ReducedCellInfo.I_CENTERED,
                            ReducedCellInfo.C_CENTERED,
                            ReducedCellInfo.P_CENTERED,
                            ReducedCellInfo.R_CENTERED };

    float tol = 2;
    ConventionalCellInfo cell;
    for ( int i = 0; i < types.length; i++ )
      for ( int j = 0; j < centerings.length; j++ )
      {
        cell = getBestConventionalCellInfo( UB, 
                                            types[i], 
                                            centerings[j],
                                            tol );
        if ( cell != null )
          list.add( cell );
      }

    removeBadForms( list );
    return getCellShortestSides( list );
  }

  
  /**
   *  Get the best matching conventional cell of the specified type, with
   *  the shortest total cell edge sum.  This method first calls the
   *  getConventionalCellInfo_List method to get all cells that match, at 
   *  least to some extent.  The cells with larger errors are then removed
   *  from the list by calling the removeBadForms() method.  There can
   *  still be several matching forms with essentially the same error in 
   *  the scalars, so to resolve this ambiguity, the remaining cell with
   *  the shortest sum a+b+c.
   *
   *  @param UB         The lattice parameters for this UB matrix are 
   *                    used to form the list of possible conventional cells.
   *  @param cell_type  String specifying the cell type, as listed in the
   *                    ReducedCellInfo class.
   *  @param centering  String specifying the centering, as listed in the
   *                    ReducedCellInfo class.
   *  @param tol        This specifies how close (in degrees) a cell angle 
   *                    must be to 90 degrees, to also include matching forms
   *                    for the cell using the supplementary angles of the
   *                    other two cell angles.
   *  @return conventional cell info object, corresponding to the best 
   *          matching form for UB and cells related to UB by reflections
   *          of pairs of cell edges. If no form matches, this returns null.
   */
  public static ConventionalCellInfo 
                            getBestConventionalCellInfo( Tran3D UB, 
                                                         String cell_type, 
                                                         String centering,
                                                         float  tol )
  {
    Vector<ConventionalCellInfo> list = getCells( UB, cell_type, centering );

    removeBadForms( list );

    return getCellShortestSides( list );
  }



  /**
   *  Get a list of UB matrices that are related to the specified UB.
   *  The related UBs are generated by both permuting the corresponding unit
   *  cell side vectors a,b,c and reflecting pairs of the sides.  A 
   *  permutation of the sides is used provided they are still "essentially"
   *  in increasing order.  The specified factor provides a tolerance to
   *  relax the restriction that |a|<=|b|<=|c|.  As long as a side is 
   *  less than or equal to the following side times the specified factor,
   *  it is considered less than or equal to the following side, for purposes
   *  of checking whether or not the list of sides could form a Niggli cell. 
   *  Two sides will be reflected across the origin if the angle between
   *  them is within the specified angle_tolerance of 90 degrees.  This will
   *  help find the correct Niggli cell for cases where the angle is near
   *  90 degrees.  In particular, if a positive Niggli cell was found with
   *  an angle near 90 degrees, due to errors in the data, it possibly should
   *  have been a negative Niggli cell.  Adding the cases with reflected sides
   *  for angles near 90 degrees will include the opposite sign Niggli cell
   *  so it is also checked.
   *
   *  @param UB               The original matrix (should be for Niggli cell)
   *  @param factor           Tolerance for lengths to be considered increasing
   *  @param angle_tolerance  Tolerance for angles near 90 degree. 
   *
   *  @return  A vector of UB matrices related to the original UB matrix
   *           by reflections and permuations of the side vectors a, b, c.
   */
  private static Vector<Tran3D> getRelatedUBs( Tran3D UB,
                                               float  factor,
                                               float  angle_tolerance )
  {
    Vector<Tran3D> result = new Vector<Tran3D>();

    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();

    Vector3D a_temp = new Vector3D();
    Vector3D b_temp = new Vector3D();
    Vector3D c_temp = new Vector3D();

    Vector3D m_a_temp = new Vector3D();
    Vector3D m_b_temp = new Vector3D();
    Vector3D m_c_temp = new Vector3D();

    Vector3D a_vec = new Vector3D();
    Vector3D b_vec = new Vector3D();
    Vector3D c_vec = new Vector3D();

    IndexingUtils.getABC( UB, a_vec, b_vec, c_vec );

    Vector3D m_a_vec = new Vector3D( a_vec );
    Vector3D m_b_vec = new Vector3D( b_vec );
    Vector3D m_c_vec = new Vector3D( c_vec );

    m_a_vec.multiply( -1 );
    m_b_vec.multiply( -1 );
    m_c_vec.multiply( -1 );

    Vector3D[][] reflections = { {   a_vec,   b_vec,   c_vec },
                                 { m_a_vec, m_b_vec,   c_vec },
                                 { m_a_vec,   b_vec, m_c_vec },
                                 {   a_vec, m_b_vec, m_c_vec } };

    float alpha = IndexingUtils.angle( b_vec, c_vec );
    float beta  = IndexingUtils.angle( c_vec, a_vec );
    float gamma = IndexingUtils.angle( a_vec, b_vec );

    float[] angles = { 90, gamma, beta, alpha };

    for ( int row = 0; row < reflections.length; row++ )
    {
      if ( Math.abs(angles[row] - 90) < angle_tolerance )
      {
        a_temp.set( reflections[row][0] );
        b_temp.set( reflections[row][1] );
        c_temp.set( reflections[row][2] );

        m_a_temp.set( a_temp );
        m_b_temp.set( b_temp );
        m_c_temp.set( c_temp );

        m_a_temp.multiply( -1 );
        m_b_temp.multiply( -1 );
        m_c_temp.multiply( -1 );

        Vector3D[][] permutations = { {   a_temp,   b_temp,   c_temp },
                                      { m_a_temp,   c_temp,   b_temp },
                                      {   b_temp,   c_temp,   a_temp },
                                      { m_b_temp,   a_temp,   c_temp },
                                      {   c_temp,   a_temp,   b_temp },
                                      { m_c_temp,   b_temp,   a_temp } };
        for ( int perm = 0; perm < 6; perm++ )
        {
          a.set( permutations[perm][0] );
          b.set( permutations[perm][1] );
          c.set( permutations[perm][2] );
          if ( a.length() <= factor * b.length() &&
               b.length() <= factor * c.length() )   // could be Niggli
          {
            Tran3D temp_UB = new Tran3D();
            IndexingUtils.getUB( temp_UB, a, b, c );
            result.add( temp_UB );
          }
        }
      }
    }
    return result;
  }

  /**
   *  Get a list of UB matrices that are related to the specified UB
   *  by reflecting pairs of the unit cell side vectors, a, b, c. 
   *  Two sides will be reflected across the origin if the angle between
   *  them is within the specified angle_tolerance of 90 degrees.  This will
   *  help find the correct Niggli cell for cases where the angle is near
   *  90 degrees.  In particular, if a positive Niggli cell was found with
   *  an angle near 90 degrees, due to errors in the data, it possibly should
   *  have been a negative Niggli cell.  Adding the cases with reflected sides
   *  for angles near 90 degrees will include the opposite sign Niggli cell
   *  so it is also checked.
   *
   *  @param UB               The original matrix (should be for Niggli cell)
   *  @param angle_tolerance  Tolerance for angles near 90 degree. 
   *
   *  @return  A vector of UB matrices related to the original UB matrix
   *           by reflections and permuations of the side vectors a, b, c.
   */

  private static Vector<Tran3D> getSignRelatedUBs( Tran3D UB,
                                                  float  angle_tolerance )
  {
    Vector<Tran3D> result = new Vector<Tran3D>();

    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();

    Vector3D a_vec = new Vector3D();
    Vector3D b_vec = new Vector3D();
    Vector3D c_vec = new Vector3D();

    IndexingUtils.getABC( UB, a_vec, b_vec, c_vec );

    Vector3D m_a_vec = new Vector3D( a_vec );
    Vector3D m_b_vec = new Vector3D( b_vec );
    Vector3D m_c_vec = new Vector3D( c_vec );

    m_a_vec.multiply( -1 );
    m_b_vec.multiply( -1 );
    m_c_vec.multiply( -1 );

    Vector3D[][] reflections = { {   a_vec,   b_vec,   c_vec },
                                 { m_a_vec, m_b_vec,   c_vec },
                                 { m_a_vec,   b_vec, m_c_vec },
                                 {   a_vec, m_b_vec, m_c_vec } };

    float alpha = IndexingUtils.angle( b_vec, c_vec );
    float beta  = IndexingUtils.angle( c_vec, a_vec );
    float gamma = IndexingUtils.angle( a_vec, b_vec );

    float[] angles = { 90, gamma, beta, alpha };

    for ( int row = 0; row < reflections.length; row++ )
    {
      if ( Math.abs(angles[row] - 90) < angle_tolerance )
      {
        a.set( reflections[row][0] );
        b.set( reflections[row][1] );
        c.set( reflections[row][2] );

        Tran3D temp_UB = new Tran3D();
        IndexingUtils.getUB( temp_UB, a, b, c );
        result.add( temp_UB );
      }
    }
  return result;
  }

  /**
   *  Get a list of UB matrices that are related to the specified UB
   *  by permuting the corresponding unit cell side vectors a,b,c. 
   *  A permutation of the sides is used provided they are still "essentially"
   *  in increasing order.  The specified factor provides a tolerance to
   *  relax the restriction that |a|<=|b|<=|c|.  As long as a side is 
   *  less than or equal to the following side times the specified factor,
   *  it is considered less than or equal to the following side, for purposes
   *  of checking whether or not the list of sides could form a Niggli cell. 
   *
   *  @param UB               The original matrix (should be for Niggli cell)
   *  @param factor           Tolerance for lengths to be considered increasing
   *
   *  @return  A vector of UB matrices related to the original UB matrix
   *           by reflections and permuations of the side vectors a, b, c.
   */
  private static Vector<Tran3D> getPermuteRelatedUBs( Tran3D UB,
                                                      float  factor )
  {
    Vector<Tran3D> result = new Vector<Tran3D>();

    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();

    Vector3D a_temp = new Vector3D();
    Vector3D b_temp = new Vector3D();
    Vector3D c_temp = new Vector3D();

    Vector3D m_a_temp = new Vector3D();
    Vector3D m_b_temp = new Vector3D();
    Vector3D m_c_temp = new Vector3D();

    IndexingUtils.getABC( UB, a_temp, b_temp, c_temp );

    m_a_temp.set( a_temp );
    m_b_temp.set( b_temp );
    m_c_temp.set( c_temp );

    m_a_temp.multiply( -1 );
    m_b_temp.multiply( -1 );
    m_c_temp.multiply( -1 );

    Vector3D[][] permutations = { {   a_temp,   b_temp,   c_temp },
                                  { m_a_temp,   c_temp,   b_temp },
                                  {   b_temp,   c_temp,   a_temp },
                                  { m_b_temp,   a_temp,   c_temp },
                                  {   c_temp,   a_temp,   b_temp },
                                  { m_c_temp,   b_temp,   a_temp } };
    for ( int perm = 0; perm < 6; perm++ )
    {
      a.set( permutations[perm][0] );
      b.set( permutations[perm][1] );
      c.set( permutations[perm][2] );
      if ( a.length() <= factor * b.length() &&
           b.length() <= factor * c.length() )   // could be Niggli
      {
        Tran3D temp_UB = new Tran3D();
        IndexingUtils.getUB( temp_UB, a, b, c );
        result.add( temp_UB );
      }
    }
    return result;
  }

}
