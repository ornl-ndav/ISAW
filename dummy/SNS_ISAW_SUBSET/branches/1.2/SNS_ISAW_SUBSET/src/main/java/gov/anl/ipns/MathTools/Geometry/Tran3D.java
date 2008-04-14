/*
 * File:  Tran3D.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log: Tran3D.java,v $
 * Revision 1.15  2006/11/12 05:31:53  dennis
 * Switched 3D vector representation to use separate fields for
 * x,y,z,w, instead of using an array to hold the four values.
 * This saves both time and space for "most" vector operations
 * since only one object (the vector) is created, rather than
 * two (the vector and the array in the vector).  Applications
 * that just frequently get all of the values out of the vector
 * may not see any improvment.
 *
 * Revision 1.14  2006/09/17 21:45:02  rmikk
 * Made the class serializatble so the data sets can now be saved as isd.
 *
 * Revision 1.13  2004/06/16 21:29:08  dennis
 * Added constructor to construct the transformation matrix from a
 * one dimensional array of values.
 *
 * Revision 1.12  2004/05/06 20:53:49  dennis
 * The setOrientation() method now recalculates the up vector to force
 * it to be orthogonal to the base and normal directions.  The vectors
 * are also normalized, so the transformation is now guaranteed to be
 * an orthogonal transformation, combined with a translation, as long
 * as the vectors are non-null and the up and base vectors have non-zero
 * length and are not collinear.
 *
 * Revision 1.11  2004/03/19 17:24:27  dennis
 * Removed unused variables
 *
 * Revision 1.10  2004/03/12 00:22:47  dennis
 * Put in MathTools.Geometry package
 * Removed dependency on tof_calc.
 *
 * Revision 1.9  2004/03/11 23:06:13  dennis
 * Changed to MathTools.Geometry package
 *
 * Revision 1.8  2004/01/28 21:58:00  dennis
 * Made set() method, and constructor more flexible, so that the
 * transformation can be initialized from either a 3x3 or 4x4
 * array.
 *
 * Revision 1.7  2003/01/08 17:12:01  dennis
 * Added method invert() to calculate the inverse of a matrix.
 *
 * Revision 1.6  2003/01/07 16:03:11  dennis
 * Added method to calculate transpose.
 *
 * Revision 1.5  2002/11/27 23:15:47  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/10/04 20:17:35  dennis
 * Fixed typo in javadoc comment.
 *
 */

package gov.anl.ipns.MathTools.Geometry;

import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.MathTools.*;

/**
 *  This class represents basic transformations of Vector3D objects, such
 *  as translation, rotation, scaling, 3D viewing & projection, as well as
 *  arbitrary 4x4 linear transformations.  Methods to apply the transformation
 *  to a single Vector3D or an array of Vector3D objects are also provided.
 */
public class Tran3D implements java.io.Serializable
{
  public static final long serialVersionUID = 1L;

  public static final float Identity[][] = { { 1, 0, 0, 0 },
                                             { 0, 1, 0, 0 },
                                             { 0, 0, 1, 0 },
                                             { 0, 0, 0, 1 } };
  protected  float a[][] = { { 1, 0, 0, 0 },
                             { 0, 1, 0, 0 },
                             { 0, 0, 1, 0 },
                             { 0, 0, 0, 1 } };
  
  

  /*------------------------- default constructor ----------------------*/
  /**
   *  Construct the 3D identity transform as a 4x4 matrix.
   */
  public Tran3D()
  {
  }

  /*------------------------ copy constructor ---------------------------*/
  /**
   *  Construct a copy of the given transformation.
   *
   *  @param  tran  the transformation to copy.
   */
  public Tran3D( Tran3D tran )
  {
    set( tran );
  }

  /*---------------------------- constructor -------------------------- */
  /**
   *  Construct this transformation using the specified matrix.  The matrix 
   *  must have at least four rows and each of them must have at least four 
   *  columns.
   *
   *  @param matrix the matrix that is to be used for the transform. 
   */
  public Tran3D( float matrix[][] )
  {
    set( matrix );
  }

  /*---------------------------- constructor -------------------------- */
  /**
   *  Construct this transformation using the specified list of 16 values.
   *
   *  @param array  the array of 16 values to be used for this transform
   *                in row major order.
   */
  public Tran3D( float array[] )
  {
    if ( array == null || array.length < 16 )
      return;

    int index = 0;
    for ( int row = 0; row < 4; row++ )
      for ( int col = 0; col < 4; col++ )
      {
        a[row][col] = array[index];
        index++;
      }
  }

  /* ----------------------------- equals ------------------------------- */
  /**
   *  Check whether or not the current transform is exactly the same
   *  transformation as the specified transform.
   *
   *  @param  tran   The transform object to compare with the current transform
   *                 object.
   * 
   *  @return Returns true if the current transform object has the same matrix
   *  as the specified transform object.
   */ 
  public boolean equals( Tran3D tran )
  {
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        if ( a[row][col] != tran.a[row][col] )
          return false;

     return true;
  }

  /*------------------------------- get ---------------------------------*/
  /**
   *  Set a reference to the 4x4 array containing the matrix for this
   *  transformation.
   *
   *  return reference to the 4x4 array containing the matrix.
   */
  public float[][] get()
  {
    return a;
  }


  /*------------------------------ set --------------------------------- */
  /**
   *  Set this transformation to the specified transformation.
   *
   *  @param tran  the transformation that the current transform is to be
   *               set from.
   */
  public void set( Tran3D tran )
  {
    for ( int row = 0; row < 4; row++ )
      for ( int col = 0; col < 4; col++ )
        a[row][col] = tran.a[row][col];
  }


  /*------------------------------ set --------------------------------- */
  /**
   *  Set this transformation to the specified matrix.  The matrix must 
   *  have at least three rows and each of them must have at least three 
   *  columns.  In most cases, the matrix should be 4x4.  If it is at least
   *  3X3 but not 4x4, then the last row and column will be filled out with
   *  the values 0,0,0,1.  
   *
   *  @param  matrix  the matrix that is to be used for the transform, padded
   *                  by 0,0,0,1 in the last row and column, if it is at least
   *                  3x3 but not a full 4x4 matrix.
   */
  public void set( float matrix[][] )
  {
    int n_rows = Math.min( matrix.length, 4 );
    if ( n_rows < 3 )
    {
      System.out.println("ERROR: too few rows in matrix in Tran3D.set()" );
      return;
    }

    int n_cols = 4;
    for ( int row = 0; row < n_rows; row++ )
    {
      n_cols = Math.min( matrix[row].length, n_cols );
      if ( n_cols < 3 )
      {
        System.out.println("ERROR: too few columns in Tran3D.set()" );
        return;
      }
    }

    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        a[row][col] = matrix[row][col];

    if ( n_rows == 3 || n_cols == 3 )           // we didn't assign full matrix
    {                                           // so fill last row and col 
      a[3][3] = 1f;                             // with 0,0,0,1
      for ( int k = 0; k < 3; k++ )
      {
         a[3][k] = 0;
         a[k][3] = 0;
      }
    }
  }


  /*--------------------------- setIdentity -----------------------------*/
  /** 
   *  Resets this transform to the identity transform.
   */
  public void setIdentity( )
  {
    set( Identity );
  }

  /*--------------------------- setTranslation ---------------------------*/
  /**
   *  Sets this transform to the transform representing a translation in 
   *  3 Dimensions through the homogeneous vector v.  It is assumed that v
   *  has been "Standardized" so that the 4th component is 1. 
   *
   *  @param   v      the homogenous representation of the translation vector 
   *                  { x, y, z, 1 }
   */
  public void setTranslation( Vector3D v )
  {
    set( Identity );
    a[0][3] = v.x;
    a[1][3] = v.y;
    a[2][3] = v.z;
  }


  /*----------------------------- setScale -------------------------------*/
  /**
   *  Set this transform to the transform representing scaling in 3 
   *  Dimensions by the x, y, z components of the homogeneous vector v.  
   *  It is assumed that v has been "Standardized" so that the 4th 
   *  component is 1.
   *
   *  @param   v      the homogenous representation of the scaling vector 
   *                  { x, y, z, 1 }
   */
  public void setScale( Vector3D v )
  {
    set( Identity );
    a[0][0] = v.x;
    a[1][1] = v.y;
    a[2][2] = v.z;
  }


  /*--------------------------- setRotation ------------------------------*/
  /**
   *  Set this transform to the transform representing rotation in 3
   *  Dimensions through a specified angle about and axis given by the 
   *  x, y, z components of the homogeneous vector v.  It is assumed that 
   *  v has been "Standardized" so that the 4th component is 1.  This 
   *  function was adapted from "matrix.c" that is part of the Mesa OpenGL
   *  code, copyright Brian Paul, 1999-2000.  It was contributed to Mesa by
   *  Erich Boleyn (erich@uruk.org) and is freely redistributable.
   *
   *  @param   angle  The angle to rotate by, specified in degrees.
   *  @param   v      The homogenous representation of the axis vector
   *                  { x, y, z, 1 }
   */
  public void setRotation( float angle, Vector3D v )
  {
    double radians = angle * Math.PI / 180.0;
    double s       = Math.sin( radians ); 
    double c       = Math.cos( radians ); 
    double mag     = Math.sqrt( v.x*v.x + v.y*v.y + v.z*v.z );

    if ( mag < 1.0e-10 )                       // axis is essentially zero
    {
      System.out.println("Error in Tran3D.setRotation: axis is essentially 0");
      set( Identity );
      return;
    }
   
    double x = v.x / mag;
    double y = v.y / mag;
    double z = v.z / mag;

    double xx    = x * x;
    double yy    = y * y;
    double zz    = z * z;
    double xy    = x * y;
    double yz    = y * z;
    double zx    = z * x;
    double xs    = x * s;
    double ys    = y * s;
    double zs    = z * s;
    double one_c = 1.0F - c;

    a[0][0] = (float)((one_c * xx) + c);
    a[0][1] = (float)((one_c * xy) - zs);
    a[0][2] = (float)((one_c * zx) + ys);
    a[0][3] = 0.0F;

    a[1][0] = (float)((one_c * xy) + zs);
    a[1][1] = (float)((one_c * yy) + c);
    a[1][2] = (float)((one_c * yz) - xs);
    a[1][3] = 0.0F;

    a[2][0] = (float)((one_c * zx) - ys);
    a[2][1] = (float)((one_c * yz) + xs);
    a[2][2] = (float)((one_c * zz) + c);
    a[2][3] = 0.0F;

    a[3][0] = 0.0F;
    a[3][1] = 0.0F;
    a[3][2] = 0.0F;
    a[3][3] = 1.0F;
  }


  /*----------------------------- transpose -------------------------------*/
  /**
   *  Set this transform to the transpose of the current ransform.
   */
  public void transpose()
  {
    float temp;
    for ( int row = 0; row < 4; row++ )
      for ( int col = 0; col < row; col++ )
      {
        temp        = a[row][col];
        a[row][col] = a[col][row];
        a[col][row] = temp;
      }
  }


  /*--------------------------- invert ---------------------------------- */
  /**
   *  Replace this transformation with it's inverse, if possible.
   *
   *  @return  If this transformation was non-singular, it is replaced by
   *           by it's inverse and true is returned.  Otherwise, false is
   *           returned and this transformation is not altered.
   */
  public boolean invert()
  {
    double temp[][] = new double[4][4];
    for ( int row = 0; row < 4; row++ )
      for ( int col = 0; col < 4; col++ )
        temp[row][col] = a[row][col];

    if ( LinearAlgebra.invert( temp ) )
    {
      for ( int row = 0; row < 4; row++ )
        for ( int col = 0; col < 4; col++ )
          a[row][col] = (float)temp[row][col];

      return true;
    }
    else
      return false;
  }



  /*------------------------- setOrientation -----------------------------*/
  /**
   *  Set this transform to position and orient an object in 3D.
   *  An object is assumed to be initially centered at the origin
   *  with it's "base" direction in the x direction and it's "up"
   *  direction in the y direction.  The matrix created by this method
   *  will first orient the object so that it's base and up directions
   *  are in the directions given by the base and up parameters, and
   *  then translate the object by the specified translation.  If the base and
   *  up vectors are not orthogonal, a new up vector will be calculated 
   *  that is in the same plane as the original up and base vectors, but
   *  is orthogonal to the base vector.  If any of the vectors are null,
   *  or if the base or up vectors have zero length, or are collinear,
   *  then a error message will be printed and the current transformation
   *  will not be altered.
   *
   *  @param   base         The direction the x-axis is mapped to.
   *  @param   up           The direction the y-axis is mapped to.
   *  @param   translation  The point the origin is transformed to.
   */
  public void setOrientation( Vector3D base, Vector3D up, Vector3D translation )
  {
    if ( base == null || up == null || translation == null )   // nothing to do
    {
      System.out.println("ERROR: null parameter to setOrientation()");
      return;
    }
                                        // build the orientation matrix
    Vector3D n = new Vector3D(); 
    n.cross( base, up );

    if ( n.length() <= 0 )
    {
      System.out.println("ERROR:base,up zero,or collinear in setOrientation()");
      return;
    }
    
    base = new Vector3D( base );        // get copies, so we don't damage
    up   = new Vector3D();              // the original parameters

    n.normalize();                      // keep n perpendicular to base & up
    base.normalize();                   
    up.cross( n, base );                // make up perapendicular to n and base

    Tran3D orient = new Tran3D();   
    orient.a[0][0] = base.x;
    orient.a[1][0] = base.y;
    orient.a[2][0] = base.z;

    orient.a[0][1] = up.x;
    orient.a[1][1] = up.y;
    orient.a[2][1] = up.z;

    orient.a[0][2] = n.x;
    orient.a[1][2] = n.y;
    orient.a[2][2] = n.z;

    setTranslation( translation );
    multiply_by( orient );             // combine orientation with tranlation
  }

  /*--------------------------- setViewMatrix ------------------------------*/
  /**
   *  Set this transform to the transform representing the "view transformation"
   *  cooresponding to a specified observers position, orientation and 
   *  view direction.
   *
   *  @param   cop    The "center of projection", ie. the position of the
   *                  observer.
   *  @param   vrp    The "view reference point", ie. the point that the
   *                  observer is looking at.
   *  @param   vuv    The "view up vector", ie. the direction that is "up"
   *                  for the observer.
   *
   *  @param   perspective  Flag indicating whether or not to make a
   *                        perspective projection.  If false, the projection
   *                        will be an orthographic projection.
   */
  public void setViewMatrix( Vector3D cop, 
                             Vector3D vrp, 
                             Vector3D vuv, 
                             boolean  perspective )
  {
     Vector3D  u = new Vector3D();           // these are set to an orthonormal
     Vector3D  v = new Vector3D();           // coordinate system at the cop
     Vector3D  n = new Vector3D( cop );
                                             // set n to a unit vector pointing
     n.subtract( vrp );                      // from the observer in the
     n.normalize();                          // direction they are looking

     if ( n.length() < 0.0001 )              // cop and vrp cooincide
     {
       System.out.println("Error: cop and vrp cooincide in " +
                          "Tran3D.setViewMatrix()");
       setIdentity();
       a[3][2] = 1;
       return;
     } 

     u.cross( vuv, n );                      // set u to the local "x" axis
     u.normalize();  

     if ( u.length() < 0.0001 )
     {
       System.out.println("Error: view direction and vuv cooincide in " +
                          "Tran3D.setViewMatrix()");
       setIdentity();
       return;
     }

     v.cross( n, u );                       // set v to the local "y" axis

     setIdentity();
     a[0][3] = -u.dot( vrp );               
     a[1][3] = -v.dot( vrp );
     a[2][3] = -n.dot( vrp );
     
     a[0][0] = u.x;
     a[1][0] = v.x;
     a[2][0] = n.x;

     a[0][1] = u.y;
     a[1][1] = v.y;
     a[2][1] = n.y;

     a[0][2] = u.z;
     a[1][2] = v.z;
     a[2][2] = n.z;
                     
     if ( perspective )                   // multiply on the left by the
     {                                    // perspective proj matrix
       Tran3D   perspec  = new Tran3D();
       Vector3D distance = new Vector3D( cop );
        
       distance.subtract( vrp );
       perspec.a[3][2] = -1/distance.length();
       
       perspec.multiply_by( this );
       this.set( perspec );
     }
  }


  /*-------------------------- multiply_by -------------------------------*/
  /**
   *  Sets this transform to the transform representing the product of
   *  itself and the specified transform.  Specifically, this = this*tran,
   *  so conceptually, the specified transform acts first, followed by the
   *  current value of this transform, to get the resulting value for this
   *  transform.
   *
   *  @param  tran  the transform that the current transform is to be 
   *                multiplied by.
   */
  public void multiply_by( Tran3D tran )
  {
    float temp[] = new float[4];   // temporary storage for one row of product
    float sum;
    int   i, j, k;                 

    for ( i = 0; i < 4; i++ )
    {
      for ( j = 0; j < 4; j++ )
      {
        sum = 0.0f;                              // find product of row i
        for ( k = 0; k < 4; k++ )                // with the columns of "tran".
          sum = sum + a[i][k] * tran.a[k][j];    // This uses all row i entries.
        temp[j] = sum;
      }

      for ( j = 0; j < 4; j++ )                  // now that we are done with
        a[i][j] = temp[j];                       // row i, we can copy the temp
                                                 // row into row i.
    }
  }

  /*-------------------------- apply_to -------------------------------*/
  /**
   *  Multiply the transformation times vector v1, placing the result in
   *  vector v2.
   *
   *  @param  v1    The vector being transformed ( i.e. multiplied by this 
   *                transform.)
   *  @param  v2    Set to the result of transforming v1.
   */
  public void apply_to( Vector3D v1, Vector3D v2 )
  {
    if ( v1 == null || v2 == null )
    {
      System.out.println("Error in Tran3D.apply_to: vector is null" );
      return;
    }

    int   row;

    float[] temp = new float[4];
    for ( row = 0; row < 4; row++ )
    {
      temp[row] = a[row][0] * v1.x +
                  a[row][1] * v1.y +
                  a[row][2] * v1.z +
                  a[row][3] * v1.w;
    }
    v2.set(temp);
  }

  /*-------------------------- apply_to -------------------------------*/
  /**
   *  Multiply the transformation times each vector in the array v1, 
   *  placing the result in the corresponding vector in the array v2.
   *
   *  @param  v1    The array of vectors being transformed 
   *                ( i.e. multiplied by this transform.)
   *  @param  v2    Set to the result of transforming the vectors in v1.
   */
  public void apply_to( Vector3D v1[], Vector3D v2[] )
  {
    if ( v1 == null || v2 == null || v1.length > v2.length )
    {
      System.out.println("Error in Tran3D.apply_to: invalid arrays" );
      return;
    }

    for ( int k = 0; k < v1.length; k++ )
      apply_to( v1[k], v2[k] );
  }


  /*------------------------------ toString ------------------------------ */
  /**
   *  Return a string form of this matrix.
   */
  public String toString()
  {
    return "{ "+a[0][0]+", "+a[0][1]+", "+a[0][2]+", "+a[0][3]+ "\n" +
           "  "+a[1][0]+", "+a[1][1]+", "+a[1][2]+", "+a[1][3]+ "\n" +
           "  "+a[2][0]+", "+a[2][1]+", "+a[2][2]+", "+a[2][3]+ "\n" +
           "  "+a[3][0]+", "+a[3][1]+", "+a[3][2]+", "+a[3][3]+ " }";
  }


  /* ----------------------------- main ---------------------------------- */
  /*
   *  main program for testing purposes only.
   *
   */
 
  public static void main ( String args[] )
  {
    float   test_1[][] =  { { 1, 2, 3, 4 },
                            { 2, 1, 3, 1 },
                            { 3, 2, 1, 5 },
                            { 2, 3, 4, 1 } };
 
    float   test_2[][] =  { { 4, 5, 1, 2 },
                            { 2, 3, 2, 1 },
                            { 5, 1, 2, 4 },
                            { 3, 4, 1, 5 } };

    float   test_3[][] =  { { 4, 5, 1, 1 },
                            { 2, 3, 2, 1 },
                            { 5, 1, 2, 1 },
                            { 0, 0, 0, 1 } };

    Tran3D  tran = new Tran3D();
    Tran3D  tran_1 = new Tran3D( test_1 );
    Tran3D  tran_2 = new Tran3D( test_2 );

    Tran3D  tran_3 = new Tran3D();
    tran_3.setScale( new Vector3D(2,3,4) );

    Tran3D  tran_4 = new Tran3D();
    tran_4.setTranslation( new Vector3D(2,3,4) );

    Tran3D  tran_5 = new Tran3D( test_1 );
    tran_5.setIdentity();

    Tran3D  tran_6 = new Tran3D( tran_2 );

    System.out.println( "tran   = \n" + tran ); 
    System.out.println( "tran_1 = \n" + tran_1 ); 
    System.out.println( "tran_2 = \n" + tran_2 ); 
    System.out.println( "tran_3 = \n" + tran_3 ); 
    System.out.println( "tran_4 = \n" + tran_4 ); 
    System.out.println( "tran_5 = \n" + tran_5 ); 
    System.out.println( "tran_6 = \n" + tran_6 ); 

    tran_1.multiply_by( tran_2 );
    System.out.println( "Product tran_1 * tran_2 = \n" + tran_1 );

    Vector3D v  = new Vector3D();
    Vector3D v1 = new Vector3D( -2, 3, 1 );
    tran_1.set( test_3 );
    tran_1.apply_to( v1, v );
    System.out.println( "Applying " );
    System.out.println( tran_1 );
    System.out.println( "to    " + v1 );
    System.out.println( "gives " + v );

    tran_1.setTranslation( new Vector3D( 1, 2, 3 ) );
    tran_1.apply_to( v1, v );
    System.out.println( "Applying " );
    System.out.println( tran_1 );
    System.out.println( "to    " + v1 );
    System.out.println( "gives " + v );

    tran_1.setScale( new Vector3D( 2, 3, 4 ) );

    int N_POINTS = 20000;
    ElapsedTime timer = new ElapsedTime();
    for ( int i = 0; i < N_POINTS; i++ )
      tran_1.apply_to( v1, v );
    float time = timer.elapsed();
    System.out.println( "Time for "+ N_POINTS +" transforms is " + time );

    Vector3D v1_list[] = new Vector3D[ N_POINTS ];
    Vector3D v2_list[] = new Vector3D[ N_POINTS ];
    for ( int i = 0; i < N_POINTS; i++ )
    {
      v1_list[i] = new Vector3D( 1, 2, 3 );
      v2_list[i] = new Vector3D( 1, 2, 3 );
    }
    timer.reset();
      tran_1.apply_to( v1_list, v2_list );
    time = timer.elapsed();
    System.out.println( "Time for "+N_POINTS+" transforms using array:"+time );

    System.out.println( "Applying " );
    System.out.println( tran_1 );
    System.out.println( "to    " + v1 );
    System.out.println( "gives " + v );

    tran_1.setRotation( 45, new Vector3D( 1, 0, 0 ) );
    System.out.println( "Rotation by 45 deg about x-axis : \n" + tran_1 );

    tran_1.setRotation( 45, new Vector3D( 0, 1, 0 ) );
    System.out.println( "Rotation by 45 deg about y-axis : \n" + tran_1 );

    tran_1.setRotation( 45, new Vector3D( 0, 0, 1 ) );
    System.out.println( "Rotation by 45 deg about z-axis : \n" + tran_1 );

    tran_1.setViewMatrix( new Vector3D( 0, 0, 1 ),
                          new Vector3D( 0, 0, 0 ),
                          new Vector3D( 0, 1, 0 ),
                          true );
    System.out.println( "View matrix is : \n" + tran_1 );

    System.out.println( "Testing inverse..." );
    tran_1 = EulerAngles.makeEulerRotation( 20, 30, 40 );
    tran_2 = EulerAngles.makeEulerRotationInverse( 20, 30, 40 );

    System.out.println("tran 1 = ");
    System.out.println("" + tran_1 );

    System.out.println("inverse = ");
    System.out.println("" + tran_2 );

    tran_1.invert();
    System.out.println("calculated inverse = ");
    System.out.println("" + tran_1 );

  }

}
