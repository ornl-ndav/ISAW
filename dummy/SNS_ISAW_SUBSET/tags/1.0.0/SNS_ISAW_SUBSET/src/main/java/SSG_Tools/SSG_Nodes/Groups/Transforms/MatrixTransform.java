/*
 * File:  MatrixTransform.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * Modified:
 *
 *  $Log: MatrixTransform.java,v $
 *  Revision 1.5  2006/07/20 15:23:52  dennis
 *  Updated from CVS repository at isaw.mscs.uwstout.edu.
 *  This added a method apply_to() to apply the transformation
 *  to a particular vector.
 *  Also added get methods for the basic information that
 *  can be set.
 *
 *  Revision 1.7  2006/07/20 15:18:12  dennis
 *  Added method to get a copy of the current transformation
 *  matrix used by this node.
 *
 *  Revision 1.6  2005/11/29 04:54:59  dennis
 *  Removed unused variable.
 *
 *  Revision 1.5  2005/11/23 00:17:08  dennis
 *  Added public method, apply_to(), that will apply the transformation
 *  to a specified vector.
 *  Added main program to provide basic test of apply_to() method.
 *  Made minor revisions to some javadoc comments.
 *
 *  Revision 1.4  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.3  2004/11/22 19:48:37  dennis
 *  Fixed parameter name in javadoc comment.
 *
 *  Revision 1.2  2004/11/22 19:44:38  dennis
 *  Added default constructor.
 *
 *  Revision 1.1  2004/11/22 18:51:42  dennis
 *  Initial version of transform that uses an arbitrary 4x4 matrix.
 */

package SSG_Tools.SSG_Nodes.Groups.Transforms;

import net.java.games.jogl.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  Instances of this class are Group nodes that transform their children using
 *  multiplication by a specified 4x4 matrix.  In particular, this class
 *  will call glMultMatrix to multiply the ModelView matrix stack by the 
 *  specified 4x4 matrix. 
 */

public class MatrixTransform extends TransformGroup
{
  private float matrix[] = { 1, 0, 0, 0,       // we keep the 4x4 matrix in 
                             0, 1, 0, 0,       // a one dimensional array
                             0, 0, 1, 0,
                             0, 0, 0, 1 };
  

  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a matrix transformation node using the identity matrix, by
   *  default.
   */
  public MatrixTransform() 
  {
    // Just use the identity matrix, by default. 
  }


  /* --------------------------- constructor ----------------------------- */
  /**
   *  Construct a transformation node using the specified 4x4 matrix.
   *
   *  @param  new_matrix   4X4 matrix containing the matrix for this transform
   */
  public MatrixTransform( float new_matrix[][] )
  {
    setMatrix( new_matrix );
  }


  /* ------------------------------- setMatrix ----------------------------- */
  /**
   *  Set a new matrix to use for this transform.
   *
   *  @param  new_matrix   4X4 matrix containing the matrix for this transform
   */
  public void setMatrix( float new_matrix[][] )
  {
    if ( new_matrix == null || new_matrix.length != 4 )
    {
      System.out.println("null matrix, or not 4 rows in matrix in setMatrix()");
      return;
    }
    for ( int row = 0; row < 4; row++ )
      if ( new_matrix[row] == null || new_matrix[row].length != 4 )
      {
        System.out.println(
                   "null row, or row not length 4 in matrix in setMatrix()");
        return;
      }
  
    int index = 0;                             // copy matrix to 1D array in
    for ( int col = 0; col < 4; col++ )        // column major order for OpenGL
      for ( int row = 0; row < 4; row++ )
      {
        matrix[index] = new_matrix[row][col];
        index++;
      }
  }


  /* ------------------------------- getMatrix ----------------------------- */
  /**
   *  Get a copy of the matrix used by this transform.
   *
   *  @return a 4X4 matrix containing a copy of the matrix for this transform
   */
  public float[][] getMatrix()
  {
    float copy[][] = new float[4][4];

    int index = 0;
    for ( int col = 0; col < 4; col++ )
      for ( int row = 0; row < 4; row++ )
      {
        copy[row][col] = matrix[index];
        index++;
      }

    return copy;
  }


  /* -------------------------- applyTransform ---------------------------- */
  /**
   *  Call glMultMatrix to actually apply the transformation specified by the 
   *  matrix. 
   */
  protected void applyTransform( GL gl )
  {
    gl.glMultMatrixf( matrix );
  }


  /* --------------------------- apply_to --------------------------- */
  /**
   *  Multiply the specified Vector3D object by this matrix transform.   
   *  This method allows an application to track the current position of 
   *  an object.
   *
   *  @param  in_vec   The vector that the transform is applied to.
   *  @param  out_vec  The value of this vector is set to the result 
   *                   of applying the transform to in_vec.
   */
  public void apply_to( Vector3D in_vec, Vector3D out_vec )
  {
     if ( in_vec == null || out_vec == null )
       throw new IllegalArgumentException(
                                "null vector in MatrixTransform.apply_to()");

     out_vec.set( in_vec );

     float mat_arr[][] = new float[4][4];
     int index = 0;                             // copy matrix from 1D array in
     for ( int col = 0; col < 4; col++ )        // column major, to 2D array 
       for ( int row = 0; row < 4; row++ )
       {
         mat_arr[row][col] = matrix[index];
         index++;
       }

     Tran3D mat_tran = new Tran3D( mat_arr );
     mat_tran.apply_to( in_vec, out_vec );
  }


  /* ----------------------------- main -------------------------------- */
  /**
   *  Main program to test basic functionality of this class.
   */
  public static void main( String args[] )
  {
    float arr[][] = { { 1, 2, 3, 0 },
                      { 4, 5, 6, 0 },
                      { 7, 8, 9, 0 },
                      { 0, 0, 0, 1 } }; 

    MatrixTransform tran = new MatrixTransform( arr );

    Vector3D temp = new Vector3D( 1, 2, 3 );
    Vector3D temp2 = new Vector3D();

    tran.apply_to( temp, temp2 );
    System.out.println("Transformed vector is " + temp2 );
    System.out.println("Should be 14, 32, 50 : 1");
  }


}
