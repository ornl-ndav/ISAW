/* 
 * File: ReducedCellInfo.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  $Author: $
 *  $Date: $            
 *  $Revision: $
 */

package Operators.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;

/**
 *  Instances of this class represent information about reduced cell types
 *  including the transformation required to transform the reduced cell to
 *  a conventional cell.  Essentially, each instance of this class represents
 *  one row of information from Table 2 in the paper:
 *  "Lattice Symmetry and Identification -- The Fundamental Role of Reduced
 *   Cells in Materials Characterization", Alan D. Mighell, Vol. 106, Number 6,
 *  Nov-Dec 2001, Journal of Research of the National Institute of Standards
 *  and Technology.
 */
public class ReducedCellInfo
{
  /**
   * Number of actual rows in Table 2, 1..44.
   */
  public static final int    NUM_CELL_TYPES = 44;

  /**
   *  Strings representing the cell types.
   */
  public static final String NONE         = "None";
  public static final String CUBIC        = "Cubic";
  public static final String RHOMBOHEDRAL = "Rhombohedral";
  public static final String TETRAGONAL   = "Tetragonal";
  public static final String ORTHORHOMBIC = "Orthorhombic";
  public static final String MONOCLINIC   = "Monoclinic";
  public static final String HEXAGONAL    = "Hexagonal";
  public static final String TRICLINIC    = "Triclinic";

  /**
   *  Strings representing the centering types.
   */
  public static final String F_CENTERED   = "F Centered";
  public static final String I_CENTERED   = "I Centered";
  public static final String C_CENTERED   = "C Centered";
  public static final String P_CENTERED   = "P Centered";
  public static final String R_CENTERED   = "R Centered";

  /**
   *  Array of basic transformations from reduced cell to conventional cell
   *  for rows 1 to 44 of Table 2.  This array is indexed by the row number
   *  1 to 44.  Entry 0 is the identity matrix. 
   */
  public static final double[][][] transforms =                        // row
     {  { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } },         //  0

        { {  1,  -1,  1 }, {  1,  1,  -1 }, {  -1,  1,  1 } },         //  1
        { {  1,  -1,  0 }, { -1,  0,   1 }, {  -1, -1, -1 } },         //  2
        { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } },         //  3
        { {  1,  -1,  0 }, { -1,  0,   1 }, {  -1, -1, -1 } },         //  4
        { {  1,   0,  1 }, {  1,  1,   0 }, {   0,  1,  1 } },         //  5

        { {  0,   1,  1 }, {  1,  0,   1 }, {   1,  1,  0 } },         //  6
        { {  1,   0,  1 }, {  1,  1,   0 }, {   0,  1,  1 } },         //  7
        { { -1,  -1,  0 }, { -1,  0,  -1 }, {   0, -1, -1 } },         //  8
        { {  1,   0,  0 }, { -1,  1,   0 }, {  -1, -1,  3 } },         //  9
        { {  1,   1,  0 }, {  1, -1,   0 }, {   0,  0, -1 } },         // 10 

        { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } },         // 11 
        { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } },         // 12
        { {  1,   1,  0 }, { -1,  1,   0 }, {   0,  0,  1 } },         // 13 
        { {  1,   1,  0 }, { -1,  1,   0 }, {   0,  0,  1 } },         // 14 
        { {  1,   0,  0 }, {  0,  1,   0 }, {   1,  1,  2 } },         // 15 

        { { -1,  -1,  0 }, {  1, -1,   0 }, {   1,  1,  2 } },         // 16 
        { { -1,   0, -1 }, { -1, -1,   0 }, {   0,  1,  1 } },         // 17 
        { {  0,  -1,  1 }, {  1, -1,  -1 }, {   1,  0,  0 } },         // 18 
        { { -1,   0,  0 }, {  0, -1,   1 }, {  -1,  1,  1 } },         // 19 
        { {  0,   1,  1 }, {  0,  1,  -1 }, {  -1,  0,  0 } },         // 20 

        { {  0,   1,  0 }, {  0,  0,   1 }, {   1,  0,  0 } },         // 21 
        { {  0,   1,  0 }, {  0,  0,   1 }, {   1,  0,  0 } },         // 22 
        { {  0,   1,  1 }, {  0, -1,   1 }, {   1,  0,  0 } },         // 23 
        { {  1,   2,  1 }, {  0, -1,   1 }, {   1,  0,  0 } },         // 24 
        { {  0,   1,  1 }, {  0, -1,   1 }, {   1,  0,  0 } },         // 25 

        { {  1,   0,  0 }, { -1,  2,   0 }, {  -1,  0,  2 } },         // 26 
        { {  0,  -1,  1 }, { -1,  0,   0 }, {   1, -1, -1 } },         // 27 
        { { -1,   0,  0 }, { -1,  0,   2 }, {   0,  1,  0 } },         // 28 
        { {  1,   0,  0 }, {  1, -2,   0 }, {   0,  0, -1 } },         // 29 
        { {  0,   1,  0 }, {  0,  1,  -2 }, {  -1,  0,  0 } },         // 30 

        { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } },         // 31 
        { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } },         // 32 
        { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } },         // 33 
        { { -1,   0,  0 }, {  0,  0,  -1 }, {   0, -1,  0 } },         // 34 
        { {  0,  -1,  0 }, { -1,  0,   0 }, {   0,  0, -1 } },         // 35 

        { {  1,   0,  0 }, { -1,  0,  -2 }, {   0,  1,  0 } },         // 36 
        { {  1,   0,  2 }, {  1,  0,   0 }, {   0,  1,  0 } },         // 37 
        { { -1,   0,  0 }, {  1,  2,   0 }, {   0,  0, -1 } },         // 38 
        { { -1,  -2,  0 }, { -1,  0,   0 }, {   0,  0, -1 } },         // 39 
        { {  0,  -1,  0 }, {  0,  1,   2 }, {  -1,  0,  0 } },         // 40 

        { {  0,  -1, -2 }, {  0, -1,   0 }, {  -1,  0,  0 } },         // 41 
        { { -1,   0,  0 }, {  0, -1,   0 }, {   1,  1,  2 } },         // 42 
        { { -1,   0,  0 }, { -1, -1,  -2 }, {   0, -1,  0 } },         // 43 
        { {  1,   0,  0 }, {  0,  1,   0 }, {   0,  0,  1 } }  };      // 44 

  /**
   *  These transforms pre-multiply the basic transforms in certain cases,
   *  as listed in the footnotes to Table 2.
   */
  public static final double[][][] transform_modifier = 
     {  { {  0,   0, -1 }, {  0,  1,   0 }, {   1,  0,  1 } },         //  0
        { { -1,   0, -1 }, {  0,  1,   0 }, {   1,  0,  0 } }  };      //  1

  /**
   *  Array of Strings specifying the cell type for reduced cells for rows
   *  1 to 44 of Table 2.  This array is indexed by the row number 1 to 44.  
   *  Entry 0 is the String "None". 
   */
  public static final String[] lattice_types =
                                 { NONE,                               //  0
                                   CUBIC,                              //  1
                                   RHOMBOHEDRAL,                       //  2
                                   CUBIC,                              //  3
                                   RHOMBOHEDRAL,                       //  4
                                   CUBIC,                              //  5

                                   TETRAGONAL,                         //  6
                                   TETRAGONAL,                         //  7
                                   ORTHORHOMBIC,                       //  8 
                                   RHOMBOHEDRAL,                       //  9
                                   MONOCLINIC,                         // 10

                                   TETRAGONAL,                         // 11 
                                   HEXAGONAL,                          // 12 
                                   ORTHORHOMBIC,                       // 13 
                                   MONOCLINIC,                         // 14
                                   TETRAGONAL,                         // 15 

                                   ORTHORHOMBIC,                       // 16 
                                   MONOCLINIC,                         // 17
                                   TETRAGONAL,                         // 18 
                                   ORTHORHOMBIC,                       // 19 
                                   MONOCLINIC,                         // 20

                                   TETRAGONAL,                         // 21
                                   HEXAGONAL,                          // 22 
                                   ORTHORHOMBIC,                       // 23 
                                   RHOMBOHEDRAL,                       // 24
                                   MONOCLINIC,                         // 25

                                   ORTHORHOMBIC,                       // 26 
                                   MONOCLINIC,                         // 27
                                   MONOCLINIC,                         // 28
                                   MONOCLINIC,                         // 29
                                   MONOCLINIC,                         // 30 

                                   TRICLINIC,                          // 31 
                                   ORTHORHOMBIC,                       // 32 
                                   MONOCLINIC,                         // 33 
                                   MONOCLINIC,                         // 34 
                                   MONOCLINIC,                         // 35 

                                   ORTHORHOMBIC,                       // 36 
                                   MONOCLINIC,                         // 37 
                                   ORTHORHOMBIC,                       // 38 
                                   MONOCLINIC,                         // 39 
                                   ORTHORHOMBIC,                       // 40 

                                   MONOCLINIC,                         // 41 
                                   ORTHORHOMBIC,                       // 42 
                                   MONOCLINIC,                         // 43 
                                   TRICLINIC     };                    // 44 


  /**
   *  Array of Strings specifying the centering for reduced cells for rows
   *  1 to 44 of Table 2.  This array is indexed by the row number 1 to 44.  
   *  Entry 0 is the String "None". 
   */
  public static final String[] center_types =
                                 { NONE,                               //  0
                                   F_CENTERED,                         //  1
                                   R_CENTERED,                         //  2 
                                   P_CENTERED,                         //  3
                                   R_CENTERED,                         //  4
                                   I_CENTERED,                         //  5

                                   I_CENTERED,                         //  6
                                   I_CENTERED,                         //  7
                                   I_CENTERED,                         //  8
                                   R_CENTERED,                         //  9
                                   C_CENTERED,                         // 10

                                   P_CENTERED,                         // 11
                                   P_CENTERED,                         // 12
                                   C_CENTERED,                         // 13
                                   C_CENTERED,                         // 14
                                   I_CENTERED,                         // 15

                                   F_CENTERED,                         // 16
                                   I_CENTERED,                         // 17
                                   I_CENTERED,                         // 18
                                   I_CENTERED,                         // 19
                                   C_CENTERED,                         // 20
 
                                   P_CENTERED,                         // 21
                                   P_CENTERED,                         // 22
                                   C_CENTERED,                         // 23
                                   R_CENTERED,                         // 24
                                   C_CENTERED,                         // 25

                                   F_CENTERED,                         // 26
                                   I_CENTERED,                         // 27
                                   C_CENTERED,                         // 28
                                   C_CENTERED,                         // 29
                                   C_CENTERED,                         // 30 

                                   P_CENTERED,                         // 31
                                   P_CENTERED,                         // 32
                                   P_CENTERED,                         // 33
                                   P_CENTERED,                         // 34
                                   P_CENTERED,                         // 35

                                   C_CENTERED,                         // 36 
                                   C_CENTERED,                         // 37 
                                   C_CENTERED,                         // 38 
                                   C_CENTERED,                         // 39 
                                   C_CENTERED,                         // 40 

                                   C_CENTERED,                         // 41 
                                   I_CENTERED,                         // 42 
                                   I_CENTERED,                         // 43 
                                   P_CENTERED   };                     // 44 

  /*
   * Private data recording the information for one row of Table 2.
   */
  private double[]   scalars     = new double[6];
  private double[][] transform   = new double[3][3];
  private String     cell_type   = NONE;
  private String     centering   = NONE;
  private int        form_num    = 0;


  /**
   *  Construct a ReducedCellInfo object representing the specified row of 
   *  Table 2 for a reduced cell with the specified lattice parameters,
   *  if the form number is between 1 and 44 exclusive.  If the form number
   *  is specified to be zero, the scalar values will be calculated according
   *  to the column headers for Table 2, for comparison purposes.
   *
   *  @param  form_num  The row number from Table 2, that specifies the
   *                    reduced form number.
   *  @param  a         Real space unit cell length "a".
   *  @param  b         Real space unit cell length "b".
   *  @param  c         Real space unit cell length "c".
   *  @param  alpha     Resl space unit cell angle "alpha", in degrees.
   *  @param  beta      Resl space unit cell angle "beta", in degrees.
   *  @param  gamma     Resl space unit cell angle "gamma", in degrees.
   */
  public ReducedCellInfo ( int    form_num, 
                           double a,     double b,    double c,
                           double alpha, double beta, double gamma )
  {
    if ( a <= 0 || b <= 0 || c <= 0 )
      throw new IllegalArgumentException(
         "Lattice lengths a, b, c must be positive: " +
         "a = " + a + " b = " + b + " c = " + c );

    if ( alpha <= 0 || alpha >= 180 ||
         beta  <= 0 || beta  >= 180 ||
         gamma <= 0 || gamma >= 180 )
      throw new IllegalArgumentException(
        "Lattice angles alpha, beta, gamma must be between 0 and 180 degrees "+
        "alpha = " + alpha + " beta = " + beta + " gamma = " + gamma );

    alpha = alpha * Math.PI / 180;
    beta  = beta  * Math.PI / 180;
    gamma = gamma * Math.PI / 180;
    init( form_num,
          a*a, b*b, c*c, 
          b*c*Math.cos( alpha), a*c*Math.cos(beta), a*b*Math.cos(gamma) );
  }


  /**
   *  Initialize all private data to represent one row of Table 2, for the
   *  row specified by the form number and for the given lattice parameters.
   *  The form number must be between 1 and 44 to represent an actual row of
   *  the table and must be 0 to represent the column header scalars, for
   *  comparison purposes.
   */
  private void init( int    form_num,
                     double a_a, double b_b, double c_c,
                     double b_c, double a_c, double a_b )
  {
    if ( form_num < 0 || form_num > 44 )
      throw new IllegalArgumentException(
         "Reduced form number must be between 0 and 44");

    this.form_num = form_num;

    transform = transforms[ form_num ];
    cell_type = lattice_types[ form_num ];
    centering = center_types[ form_num ];

    if ( form_num == 0 )
    {
      scalars[0] = a_a;
      scalars[1] = b_b;
      scalars[2] = c_c;
    }
    else if ( form_num <= 8 )
    {
      scalars[0] = a_a;
      scalars[1] = a_a;
      scalars[2] = a_a;
    }
    else if ( form_num <= 17 )
    {
      scalars[0] = a_a;
      scalars[1] = a_a;
      scalars[2] = c_c;
    }
    else if ( form_num <= 25 )
    {
      scalars[0] = a_a;
      scalars[1] = b_b;
      scalars[2] = b_b;
    }
    else 
    {
      scalars[0] = a_a;
      scalars[1] = b_b;
      scalars[2] = c_c;
    }

    double value;
    switch( form_num )
    {
    case 0:
            scalars[3] = b_c;
            scalars[4] = a_c;
            scalars[5] = a_b;
            break;
    case 1:
            scalars[3] = a_a/2;
            scalars[4] = a_a/2;
            scalars[5] = a_a/2;
            break;
    case 2:
            scalars[3] = b_c;
            scalars[4] = b_c;
            scalars[5] = b_c;
            break;
    case 3:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 4:
            value = -Math.abs(b_c); 
            scalars[3] = value;
            scalars[4] = value;
            scalars[5] = value;
            break;
    case 5:
            scalars[3] = -a_a/3;
            scalars[4] = -a_a/3;
            scalars[5] = -a_a/3;
            break;
    case 6:
            value = (-a_a + Math.abs(a_b))/2;
            scalars[3] = value;
            scalars[4] = value;
            scalars[5] = -Math.abs(a_b);;
            break;
    case 7:
            value = (-a_a + Math.abs(b_c))/2;
            scalars[3] = -Math.abs(b_c);
            scalars[4] = value;
            scalars[5] = value;
            break;
    case 8:
            scalars[3] = -Math.abs(b_c);
            scalars[4] = -Math.abs(a_c);
            scalars[5] = -(Math.abs(a_a) - Math.abs(b_c) - Math.abs(a_c));
            break;
    case 9:
            scalars[3] = a_a/2;
            scalars[4] = a_a/2;
            scalars[5] = a_a/2;
            break;
    case 10: 
            scalars[3] = b_c;
            scalars[4] = b_c;
            scalars[5] = a_b;
            foot_note_d( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 11:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 12:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = -a_a/2;
            break;
    case 13:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = -Math.abs(a_b);
            break;
    case 14:
            value = -Math.abs(b_c);
            scalars[3] = value;
            scalars[4] = value;
            scalars[5] = -Math.abs(a_b);
            foot_note_d( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 15:
            scalars[3] = -a_a/2;
            scalars[4] = -a_a/2;
            scalars[5] = 0;
            break;
    case 16:
            value = -Math.abs(b_c);
            scalars[3] = value;
            scalars[4] = value;
            scalars[5] = -(a_a - 2*Math.abs(b_c) );
            break;
    case 17:
            scalars[3] = -Math.abs(b_c);
            scalars[4] = -Math.abs(a_c);
            scalars[5] = -(a_a - Math.abs(b_c) - Math.abs(a_c));
            foot_note_e( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 18:
            scalars[3] = a_a/4;
            scalars[4] = a_a/2;
            scalars[5] = a_a/2;
            break;
    case 19:
            scalars[3] = b_c;
            scalars[4] = a_a/2;
            scalars[5] = a_a/2;
            break;
    case 20:
            scalars[3] = b_c;
            scalars[4] = a_c;
            scalars[5] = a_c;
            foot_note_b( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 21:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 22:
            scalars[3] = -b_b/2;
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 23:
            scalars[3] = -Math.abs(b_c);
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 24:
            scalars[3] = -(b_b - a_a/3)/2;
            scalars[4] = -a_a/3;
            scalars[5] = -a_a/3;
            break;
    case 25:
            value = -Math.abs(a_c);
            scalars[3] = -Math.abs(b_c);
            scalars[4] = value;
            scalars[5] = value;
            foot_note_b( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 26:
            scalars[3] = a_a/4;
            scalars[4] = a_a/2;
            scalars[5] = a_a/2;
            break;
    case 27:
            scalars[3] = b_c;
            scalars[4] = a_a/2;
            scalars[5] = a_a/2;
            foot_note_f( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 28:
            scalars[3] = a_b/2;
            scalars[4] = a_a/2;
            scalars[5] = a_b;
            break;
    case 29:
            scalars[3] = a_c/2;
            scalars[4] = a_c;
            scalars[5] = a_a/2;
            break;
    case 30:
            scalars[3] = b_b/2;
            scalars[4] = a_b/2;
            scalars[5] = a_b;
            break;
    case 31:
            scalars[3] = b_c;
            scalars[4] = a_c;
            scalars[5] = a_b;
            break;
    case 32:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 33:
            scalars[3] = 0;
            scalars[4] = -Math.abs(a_c);
            scalars[5] = 0;
            break;
    case 34:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = -Math.abs(a_b);
            break;
    case 35:
            scalars[3] = -Math.abs(b_c);
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 36:
            scalars[3] = 0;
            scalars[4] = -a_a/2;
            scalars[5] = 0;
            break;
    case 37:
            scalars[3] = -Math.abs(b_c);
            scalars[4] = -a_a/2;
            scalars[5] = 0;
            foot_note_c( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 38:
            scalars[3] = 0;
            scalars[4] = 0;
            scalars[5] = -a_a/2;
            break;
    case 39:
            scalars[3] = -Math.abs(b_c);
            scalars[4] = 0;
            scalars[5] = -a_a/2;
            foot_note_d( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 40:
            scalars[3] = -b_b/2;
            scalars[4] = 0;
            scalars[5] = 0;
            break;
    case 41:
            scalars[3] = -b_b/2;
            scalars[4] = -Math.abs(a_c);
            scalars[5] = 0;
            foot_note_b( a_a, b_b, c_c, b_c, a_c, a_b );
            break;
    case 42:
            scalars[3] = -b_b/2;
            scalars[4] = -a_a/2;
            scalars[5] = 0;
            break;
    case 43:
            scalars[3] = -(b_b - Math.abs(a_b))/2;
            scalars[4] = -(a_a - Math.abs(a_b))/2;
            scalars[5] = -Math.abs(a_b);
            break;
    case 44:
            scalars[3] = -Math.abs(b_c);
            scalars[4] = -Math.abs(a_c);
            scalars[5] = -Math.abs(a_b);
            break;
    }
  }


  /**
   *  Adjust tranform and centering according to foot note b
   */
  private void foot_note_b( double a_a, double b_b, double c_c,
                            double b_c, double a_c, double a_b )
  {
    if ( a_a < 4 * Math.abs(a_c) )          // foot note b
    {
      premultiply(0);                       // use matrix modification 0
      centering = I_CENTERED;
    }
  }


  /**
   *  Adjust tranform and centering according to foot note c
   */
  private void foot_note_c( double a_a, double b_b, double c_c,
                            double b_c, double a_c, double a_b )
  {
    if ( b_b < 4 * Math.abs(b_c) )          // foot note c
    {
      premultiply(0);                       // use matrix modification 0
      centering = I_CENTERED;
    }
  }


  /**
   *  Adjust tranform and centering according to foot note d
   */
  private void foot_note_d( double a_a, double b_b, double c_c,
                            double b_c, double a_c, double a_b )
  {
    if ( c_c < 4 * Math.abs(b_c) )          // foot note d
    {
      premultiply(0);                       // use matrix modification 0
      centering = I_CENTERED;
    }
  }


  /**
   *  Adjust tranform and centering according to foot note e
   */
  private void foot_note_e( double a_a, double b_b, double c_c,
                            double b_c, double a_c, double a_b )
  {
    if ( 3 * a_a < c_c + 2 * Math.abs(a_c) )     // foot note e
    {
      premultiply(1);                            // use matrix modification 1
      centering = C_CENTERED;
    }
  }


  /**
   *  Adjust tranform and centering according to foot note f
   */
  private void foot_note_f( double a_a, double b_b, double c_c,
                            double b_c, double a_c, double a_b )
  {
    if ( 3 * b_b < c_c + 2 * Math.abs(b_c) )     // foot note f
    {
      premultiply(1);                            // use matrix modification 1
      centering = C_CENTERED;
    }
  }


  /**
   * Adjust the tranformation for this reduced cell by premultiplying 
   * by modification transform 0 or 1.
   */
   private void premultiply( int index )
   {
     transform = LinearAlgebra.copy( transform );   // make copy so we can
                                                    // change it
     transform = LinearAlgebra.mult( transform_modifier[ index ], transform );
   }


  /**
   *  Return the transformation to map the reduced cell to the conventional 
   *  cell, as listed in Table 2 if the form number is between 1 and 44.
   *  If the form number is 0, this returns the identity transformation.
   *
   *  @return A reference to a 2D array containing a copy of the 
   *          transformation.
   */
  public double[][] getTransformation()
  {
    double[][] copy = new double[3][3];
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        copy[row][col] = transform[row][col];
    return copy;
  }


  /**
   * Get the string representing the centering type for this row of Table 2.
   *
   * @return one of the Strings representing the centering types.
   */
  public String getCentering()
  {
    return centering;
  }


  /**
   * Get the string representing the cell type for this row of Table 2.
   *
   * @return one of the Strings representing the cell types.
   */
  public String getCellType()
  {
    return cell_type;
  }


  /**
   * Get the reduced form number represented by this object.  If the
   * number is between 1 and 44, this object represents a row of Table 2.
   * if the number is 0, this object contains the scalars for the current
   * reduced cell.
   *
   * @return 1-44 or the value 0.
   */
  public int getFormNum()
  {
    return form_num;
  }


  /**
   * Get a String showing the form number, scalars, cell type, centering
   * and transformation values stored in this object.
   *
   * @return A String form of the info in this object.
   */
  public String toString()
  {
    String result =
      String.format("%d  %6.3f %6.3f %6.3f %6.3f %6.3f %6.3f ",
                     form_num, scalars[0], scalars[1], scalars[2],
                               scalars[3], scalars[4], scalars[5] );

    result += " " + cell_type + " " + centering ;

    for ( int row = 0; row < 3; row++ )
    {
      for ( int col = 0; col < 3; col++ )
        result += String.format("%2.0f", transform[row][col] );

      if ( row < 2 )
        result += " | ";
    }

    return result;
  }


  /**
   * Get the maximum absolute difference between the scalars (columns of table 
   * 2) for the specifed ReducedCellInfo object and this ReducedCellInfo.
   *
   * @param info  The ReducedCellInfo object to compare with the current
   *              object.
   *
   * @return  The maximum absolute difference between the scalars.
   */
  public double distance( ReducedCellInfo info )
  {
    double max = 0;

    for ( int i = 0; i < scalars.length; i++ )
    {
      double difference = Math.abs( info.scalars[i] - scalars[i] );
      System.out.printf( "difference %d = %8.5f\n",
                          i, difference );
      if ( difference > max )
        max = difference;
    }
    return max;
  }


  /**
   * Get the maximum absolute weighted difference between the scalars 
   * for the specifed ReducedCellInfo object and this ReducedCellInfo.
   * A fairly complicated weighting is used to make the effect of a
   * difference in cell edge length on lattice corner positions is 
   * comparable to the effect of a difference in the angles.
   *
   * @param info  The ReducedCellInfo object to compare with the current
   *              object.
   *
   * @return  The maximum absolute difference between the scalars.
   */
  public double weighted_distance( ReducedCellInfo info )
  {
    double[] vals_1 = getNormVals( this );
    double[] vals_2 = getNormVals( info );

    double max = 0;

    for ( int i = 0; i < vals_1.length; i++ )
    {
      double difference = Math.abs( vals_1[i] - vals_2[i] );
      System.out.printf( "weighted difference %d = %8.5f\n",
                          i, difference );
      if ( difference > max )
        max = difference;
    }
    return max;
  }


  /**
   * Get array of six values, related to the six scalars, but adjusted so
   * that changes in these values represent changes of positions of the 
   * lattice corners of approximately the same magnitude.  This is useful
   * when comparing how close the lattice for one cell is to the lattice 
   * for another cell.
   */
  private double[] getNormVals( ReducedCellInfo info )
  {
    double[] vals = new double[6];

    double a = Math.sqrt( info.scalars[0] );
    double b = Math.sqrt( info.scalars[1] );
    double c = Math.sqrt( info.scalars[2] );

                // Use the side lengths themselves, instead of squares of sides.
    vals[0] = a;
    vals[1] = b; 
    vals[2] = c;
                // Since b.c = b c cos(alpha) changes in b.c correspond to 
                // changes in alpha via the arccos() function.  If the side
                // lengths are assumed to be correct, since changes in position
                // vary as s = r * theta, we scale this by the average of 
                // the sides affected (b+c)/2.
                // Each of the dot products, b.c, a.c and a.b are treated
                // analogously.
    vals[3] = Math.acos( info.scalars[3] / ( b * c ) ) * ( b + c ) / 2;
    vals[4] = Math.acos( info.scalars[4] / ( a * c ) ) * ( a + c ) / 2;
    vals[5] = Math.acos( info.scalars[5] / ( a * b ) ) * ( a + b ) / 2;

    return vals;
  }


  /**
   * Basic test of constructor and toString method.
   */
  public static void main( String args[] )
  {
                            // experimental values:
     double a = 4.9215;
     double b = 4.9128;
     double c = 5.4003;
     double alpha = 90.0893;
     double beta  = 90.0578;
     double gamma = 120.0390;

     // info_0 = measured quartz
     ReducedCellInfo info_0  = new ReducedCellInfo( 0, a, b, c,
                                                    alpha, beta, gamma );
     System.out.println("Using Measured Lattice parameters: ");
     System.out.printf("%5.3f  %5.3f  %5.3f   %7.2f  %7.2f  %7.2f\n\n",
                           a,    b,    c, alpha, beta, gamma );

     // info_12 = theoretical quartz
     // theoretical values:
     a = 4.913;
     b = 4.913;
     c = 5.40;
     alpha = 90;
     beta  = 90;
     gamma = 120;
     System.out.println("Using Theoretical Lattice parameters: ");
     System.out.printf("%5.2f  %5.2f  %5.2f   %7.2f  %7.2f  %7.2f\n\n",
                           a,    b,    c, alpha, beta, gamma );
     ReducedCellInfo info_12 = new ReducedCellInfo( 12, a, b, c,
                                                    alpha, beta, gamma );

     System.out.println( "Measured:\n"+ info_0.toString() );

     System.out.println( "\nTheoretical:\n" + info_12.toString() );

     System.out.printf( "\nDistance          = %8.6f\n",
                         info_12.distance(info_0) );

     System.out.printf( "\nWeighted Distance = %8.6f\n",
                         info_12.weighted_distance(info_0) );
  }

}
