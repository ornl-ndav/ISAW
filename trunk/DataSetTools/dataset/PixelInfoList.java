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
 *  Revision 1.1  2003/02/05 22:16:33  dennis
 *  Initial Version (not complete).
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;

/**
 */  

public class PixelInfoList 
{
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
  }

  /**
   *  Construct a PixelInfoList object with only one entry, the specified 
   *  IPixelInfo object.
   *
   *  @param pixel_info  The IPixelInfo object record in this list.
   */
  public PixelInfoList( IPixelInfo pixel  )
  {
    list = new IPixelInfo[1];
    list[0] = pixel;
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
      angle[i] = position.getScatteringAngle(); 
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
    DetectorPixelInfo test_pixel = 
                          new DetectorPixelInfo( 2, row, col, test_grid );
 
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
  }

} 
