/*
 * File:  PixelInfoList.java
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.4  2003/10/15 23:50:11  dennis
 *  Fixed javadocs to build cleanly with jdk 1.4.2
 *
 *  Revision 1.3  2003/02/20 19:45:00  dennis
 *  Now implements Serializable.
 *
 *  Revision 1.2  2003/02/07 18:41:55  dennis
 *  Added merge() method for combining lists of pixels.
 *  Added serial version ID and ReadObject methods needed for "stable"
 *  serialization.
 *
 *  Revision 1.1  2003/02/05 22:16:33  dennis
 *  Initial Version (not complete).
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;
import java.util.*;

/**
 */  

public class PixelInfoList implements Serializable
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;


  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.
  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  private IPixelInfo list[] = null; 

  /**
   *  Construct a PixelInfoList object by copying IPixelInfo references from
   *  the specified list to an internal list.
   *
   *  @param list  The list of IPixelInfo objects to use for this 
   *               PixelInfoList. 
   */
  public PixelInfoList( IPixelInfo list[]  )
  {
    this.list = new IPixelInfo[ list.length ];
    for ( int i = 0; i < list.length; i++ )
      this.list[i] = list[i];

    if ( list.length > 1 )
      Arrays.sort( list, new PixelInfoComparator() );

  }

  /**
   *  Construct a PixelInfoList object with only one entry, the specified 
   *  IPixelInfo object.
   *
   *  @param  pixel_info  The IPixelInfo object record in this list.
   */
  public PixelInfoList( IPixelInfo pixel_info  )
  {
    list = new IPixelInfo[1];
    list[0] = pixel_info;
  }

  /**
   *  Get the average (in Cartesian coordinates) of the positions of all 
   *  of the pixels in this pixel list. 
   *
   *  @return a DetectorPosition object with the average of the positions
   *          of the pixels.
   */
  public DetectorPosition average_position()
  {
    int   n = list.length;
    float x = 0, 
          y = 0, 
          z = 0;
    float position[];

    for ( int i = 0; i < n; i++ )
    {
      position = list[i].position().get();
      x += position[0]; 
      y += position[1]; 
      z += position[2]; 
    }

    DetectorPosition ave_position = new DetectorPosition();
    ave_position.setCartesianCoords( x/n, y/n, z/n );
    return ave_position;
  }

  /**
   *  Get the weighted average (in polar coordinates) of the positions of 
   *  all of the pixels in this pixel list, weighted by the solid angle 
   *  that they subtend.  The average is computed in polar coordinates, 
   *  so that if multiple detectors are at the same distance from the 
   *  origin, their average will also be at that distance. 
   *
   *  @return a DetectorPosition object with the weighted average of the 
   *          positions of the pixels.
   */
  public DetectorPosition effective_position()
  {
    int   n = list.length;

    DetectorPosition position[]    = new DetectorPosition[n];
    float            solid_angle[] = new float[n];

    for ( int i = 0; i < n; i++ )
    {
      position[i] = new DetectorPosition( list[i].position() );
      solid_angle[i] = list[i].SolidAngle(); 
    }

    return DetectorPosition.getAveragePosition( position, solid_angle );
  }

  /**
   *  Get the row number of the first pixel in the list.
   *
   *  @return the row number of the fist pixel.
   */
  public float row()
  {
    return list[0].row();
  }

  /**
   *  Get the column number of the first pixel in the list.
   *
   *  @return the column number of the fist pixel.
   */
  public float col()
  {
    return list[0].col();
  }

  /**
   *  Get the average of the row numbers of the all pixels in the list.
   *
   *  @return the average row number.
   */
  public float average_row()
  {
    int n = list.length;

    if ( n <= 0 )
      return 0; 

    float total = 0f;
    for ( int i = 0; i < n; i++ )
      total += list[i].row();

    return total/n;
  }

  /**
   *  Get the average of the column numbers of the all pixels in the list.
   *
   *  @return the average column number.
   */
  public float average_col()
  {
    int n = list.length;

    if ( n <= 0 )
      return 0;

    float total = 0f;
    for ( int i = 0; i < n; i++ )
      total += list[i].col();
    
    return total/n;
  }

  /**
   *  Get the total number of pixels in this list.
   *
   *  @return the number of pixels in this list.
   */
  public int num_pixels()
  {
    return list.length;
  }

  /**
   *  Get the pixel that is in position 'i' of the list.  If 'i' is not a
   *  a valid index, null is returned.
   *
   *  @return the 'ith' IPixelInfo object in the list.
   */
  public IPixelInfo pixel( int i )
  {
    if ( i >= 0 && i < list.length )
      return list[i];

    return null;
  }

  /**
   *  Get the total solid angle for this list of pixels.
   *
   *  @return the total solid angle.
   */
  public float SolidAngle()
  {
    int n = list.length;

    float total = 0f;
    for ( int i = 0; i < n; i++ )
      total += list[i].SolidAngle();

    return total;
  }

  /**
   *  Get range of scattering angles covered by this list of pixels.  This
   *  is calculated as:
   *      max scattering angle - min scattering angle
   *      + half of the Delta2Theta values of the
   *        pixels with max and min scattering angles 
   *
   *  @return the range of scattering angles.
   */
  public float Delta2Theta()
  {
    int n = list.length;

    if ( n <= 0 )
      return 0;
                                    // get lists of delta 2 theta values
                                    // and scattering angles
    float delta[] = new float[n];  
    float angle[] = new float[n];         

    DetectorPosition position;            
    for ( int i = 0; i < n; i++ )
    {
      delta[i] = list[i].Delta2Theta();
      position = new DetectorPosition( list[i].position() );
      angle[i] = (float)(180.0/Math.PI) * position.getScatteringAngle(); 
    }
                                    // find the min & max scattering angles
    int min_index = 0;
    int max_index = 0;  
    for ( int i = 1; i < n; i++ )
    {
      if ( angle[i] < angle[min_index] )
        min_index = i;
      if ( angle[i] > angle[max_index] )
        max_index = i;
    }

    return  angle[max_index] - angle[min_index] + 
           (delta[max_index] + delta[min_index])/2;
  }


  /**
   *  Form a PixelInfoList by merging the current list with the specified list,
   *  recording each pixel only once and keeping the list ordered by ID.
   *  If no changes are needed to the current list, this just returns the
   *  current list. 
   *
   *  @param  other_pil  the list to merge with the current list.
   * 
   *  @return true if a non-trivial merge operation was done, return false
   *          if the new list is the same as the old list.
   */
  public PixelInfoList merge( PixelInfoList other_pil )
  {            
                                    // do it quickly in various special cases
    if ( other_pil == null || other_pil.list.length == 0 )
      return this;

    if ( list.length == other_pil.list.length )
    {
      boolean same_ids = true;
      int i = 0;
      while ( same_ids && i < list.length )
      {
        if ( list[i].ID() != other_pil.list[i].ID() )
          same_ids = false;
        i++;
      }
      if ( same_ids )
        return this;
    }
                          // in general, merge the lists, only keeping distinct
                          // pixel IDs.
    IPixelInfo temp[] = new IPixelInfo[ list.length + other_pil.list.length ];
    int i        = 0,
        j        = 0,
        num_used = 0;
    int last_pix = -1;
    int pix_i;
    int pix_j;
    boolean used_other_list = false;

    while ( i < list.length && j < other_pil.list.length )
    {
      pix_i = list[ i ].ID();
      pix_j = other_pil.list[ j ].ID();
      if ( pix_i < pix_j )
      {
        if ( pix_i != last_pix )
        {
          temp[ num_used ] = list[i];
          last_pix = pix_i;
          num_used++;
        }
        i++;
      }
      else if ( pix_i > pix_j )
      {
        if ( pix_j != last_pix )
        {
          temp[ num_used ] = other_pil.list[ j ];
          last_pix = pix_j;
          num_used++;
          used_other_list = true;
        }
        j++;
      }
      else     // pix_i == pix_j, so use seg_i if it is not already in the list
      {
        if ( pix_i != last_pix )
        {
          temp[ num_used ] = list[i];
          last_pix = pix_i;
          num_used++;
        }
        i++;
        j++;
      }
    }

    if ( i < list.length )                   // leftover values in current list
    {
      System.arraycopy( list, i, temp, num_used, list.length - i );
      num_used += list.length - i;
    }

    else if (j < other_pil.list.length )      // leftover values in other list
    {
      System.arraycopy( other_pil.list, j, temp, num_used,
                        other_pil.list.length - j );
      num_used += other_pil.list.length - j;
      used_other_list = true;
    }

    if ( !used_other_list )
      return this;
                             // otherwise copy the values into a new array
                             // if needed and return a new PixelInfoList object
    IPixelInfo new_list[];
    if ( num_used < temp.length )
    {
      new_list = new IPixelInfo[ num_used ];
      System.arraycopy( temp, 0, new_list, 0, num_used );
    }
    else
      new_list = temp;

    return new PixelInfoList( new_list );
  }


/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- readObject ------------------------------- */
/**
 *  The readObject method is called when objects are read from a serialized
 *  ojbect stream, such as a file or network stream.  The non-transient and
 *  non-static fields that are common to the serialized class and the
 *  current class are read by the defaultReadObject() method.  The current
 *  readObject() method MUST include code to fill out any transient fields
 *  and new fields that are required in the current version but are not
 *  present in the serialized version being read.
 */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
      System.out.println("Warning:PixelInfoList IsawSerialVersion != 1");
  }



  /* ------------------------------------------------------------------- */
  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
    int      id       =  1;
    String   units    = "meters";
    Vector3D center   = new Vector3D( 0,-.32f, 0 );
    Vector3D x_vector = new Vector3D(-1,   0, 0 );
    Vector3D y_vector = new Vector3D( 0,   0, 1 );
    float    width    = .25f;
    float    height   = .25f;
    float    depth    = 0.002f;
    int      n_rows   = 85;
    int      n_cols   = 85;

    UniformGrid test_grid = new UniformGrid( id, units,
                                             center, x_vector, y_vector,
                                             width, height, depth,
                                             n_rows, n_cols );


    System.out.print( test_grid.toString() );     // show basic grid info

    short row = 9;                                  // show info on one pixel
    short col = 4;
    
    DetectorPixelInfo test_pixel;
   
    for ( int i = 0; i < 4; i++ )
    {
      test_pixel = new DetectorPixelInfo( i+2, row, col, test_grid );
 
      System.out.print( test_pixel.toString() );    // show basic pixel info
      System.out.println("---------------------------------------");
      System.out.println("At row = " + row + " col = " + col );
      System.out.println("position() = " + test_pixel.position() );
      System.out.println("x_vec()    = " + test_pixel.x_vec() );
      System.out.println("y_vec()    = " + test_pixel.y_vec() );
      System.out.println("z_vec()    = " + test_pixel.z_vec() );
      System.out.println("width()    = " + test_pixel.width() );
      System.out.println("depth()    = " + test_pixel.depth() );
      System.out.println("height()   = " + test_pixel.height() );
      System.out.println("SolidAngle()  = " + test_pixel.SolidAngle() );
      System.out.println("Delta2Theta() = " + test_pixel.Delta2Theta() );
      if ( i == 0 )
      {
        row = 0;
        col = 0;
      }
      else if ( i == 1 )
      {
        row = 84;
        col = 84;
      }
      else if ( i == 2 )
      {
        row = 42;
        col = 42;
      }
    }

    IPixelInfo list[] = new IPixelInfo[4];
    for ( row = 0; row < 2; row++ )
      for ( col = 0; col < 2; col++ )
        list[row * 2 + col] = new DetectorPixelInfo( 
                                          (short)((row+5)*2+col+5+100),
                                          (short)(row+5),
                                          (short)(col+5),
                                          test_grid );


    PixelInfoList pi_list = new PixelInfoList( list );
    System.out.println("................................");
    System.out.println("ave_pos = " + pi_list.average_position() );
    System.out.println("eff_pos = " + pi_list.effective_position() );
    System.out.println("row     = " + pi_list.row() );
    System.out.println("col     = " + pi_list.col() );
    System.out.println("ave_row = " + pi_list.average_row() );
    System.out.println("ave_col = " + pi_list.average_col() );
    System.out.println("num_pix = " + pi_list.num_pixels() );
    System.out.println("sol_ang = " + pi_list.SolidAngle() );
    System.out.println("del 2 t = " + pi_list.Delta2Theta() );

    System.out.println("The first list's IDs are: " );
    for ( int i = 0; i < pi_list.num_pixels(); i++ )
      System.out.println( "Pixel ID is: " + pi_list.pixel(i).ID() );

                                           // make a second list an test merge
    list = new IPixelInfo[9];
    for ( row = 0; row < 3; row++ )
      for ( col = 0; col < 3; col++ )
        list[row * 3 + col] = new DetectorPixelInfo( 
                                          (short)((row+2)*3+col+2+100),
                                          (short)(row+2),
                                          (short)(col+2),
                                          test_grid );

    PixelInfoList pi_list_2 = new PixelInfoList( list );
    System.out.println("The second list's IDs are: " );
    for ( int i = 0; i < pi_list_2.num_pixels(); i++ )
      System.out.println( "Pixel ID is: " + pi_list_2.pixel(i).ID() );

    PixelInfoList merged_list = pi_list_2.merge( pi_list );
    System.out.println("The merged list's IDs are: " );
    for ( int i = 0; i < merged_list.num_pixels(); i++ )
      System.out.println( "Pixel ID is: " + merged_list.pixel(i).ID() );

    if ( pi_list_2.equals(merged_list) )
      System.out.println("Used same list");
    else
      System.out.println("Made new list");
  }

} 
