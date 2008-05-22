/* 
 * File: LogXScale.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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

package DataSetTools.dataset;

/**
 * This class represents an XScale with x_values placed on intervals that
 * increase in length APPROXIMATELY in a geometric progression.  However,
 * the x_values are adjusted to be multiples of 0.1, as is frequently 
 * done for neutron scattering histogram bin boundaries, where the x_values
 * should fall on 100ns time boundaries. 
 */
public class LogXScale extends VariableXScale
{
  public  static final String IPNS_SCD = "IPNS_SCD";
  private static float[] default_array = {0,1};

  /**
   * Construct an XScale with values at multiples of 0.1, for which the
   * interval lengths increase approximately by a fixed ratio at each step.
   * The ratio of lengths of successive intervals is calculated as
   * (start_x + first_step)/start_x.  Two possible rounding modes are 
   * supported.  If the mode is specified as LogXScale.IPNS_SCD, then 
   * the rounded value of the current x_value is used for calculating the 
   * subsequent x_value.  Otherwise, x_values are rounded as they are stored
   * in the array of x_values, but the calculation of later x_values uses
   * the full precision (not rounded) x_value.
   * 
   * @param start_x    The first x_value in the XScale
   * @param end_x      The approximate last x_value in the XScale.  Points
   *                   are calculated and added to the XScale so that end_x
   *                   is in the last sub-interval of the XScale.
   * @param first_step The length of the first sub-interval of the XScale.
   * @param mode       If mode = LogXScale.IPNS_SCD, then rounded values are
   *                   used in calculating later values, otherwise the grid
   *                   points are calculated to full precision, and are just
   *                   rounded as they are stored in the array of x_values.
   */
  public LogXScale( double start_x, 
                    double end_x, 
                    double first_step,
                    String mode )
  {
    super( default_array );   // keep the super class constructor happy by
                              // giving it a valid initial array.  This
                              // constructor will change it.
    if ( end_x < start_x )
      throw new IllegalArgumentException( "start_x > end_x " + start_x +
                                           " > " + end_x);

    double ratio = (start_x + first_step)/start_x;

    if ( ratio <= 1 )
      throw new IllegalArgumentException( "ratio not greater than 1: "+ratio);

    System.out.println("Ratio = " + ratio );

    this.start_x = (float)start_x;

    double num_vals = Math.log(end_x/start_x) / Math.log( ratio );
    this.num_x = (int)Math.ceil( num_vals ) + 1;

    x = new float[ this.num_x ];
    double temp_x = start_x * 10;

    if ( mode.equalsIgnoreCase(IPNS_SCD) )
      for ( int i = 0; i < num_x; i++ )
      {                                 // round temp_x to nearest step of 0.1
        temp_x = (int)(temp_x + 0.499); // this approximate rounding operation
        x[i] = (float)(temp_x/10.0);    // essentially replicates the x values
        temp_x *= ratio;                // used in scd08336.run from IPNS
      }
    else
      for ( int i = 0; i < num_x; i++ )
      {                                           // round temp_x to nearest 
        x[i] = (float)(Math.round(temp_x)/10.0);  // 0.1 for the x_value, but
        temp_x *= ratio;                          // continue calculation
      }                                           // at full precision

    this.end_x = getX( num_x - 1 );
  }
}

