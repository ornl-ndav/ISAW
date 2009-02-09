/*
 * File: LogAxis.java 
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
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-12-05 00:41:28 -0600 (Fri, 05 Dec 2008) $            
 *  $Revision: 5713 $
 */

package gov.anl.ipns.ViewTools.Panels.TwoD;


import java.awt.*;
import javax.swing.*;

/**
 * This class is a Drawable that draws a calibrated logarithmic axis with the
 * specified points marked.
 * 
 * @author Dennis Mikkelson
 *
 */
public class LogAxis extends Axis
{
  private double  log_min,
                  log_max;

  private String linear_format = null;  // Only set non-null if the division
                                        // points are actually linear.
  
  /**
   * Construct a log axis of the specified dimensions, with
   * the specified calibrations and orientation.
   * 
   * @param x0          The x-coordinate, of the lower left hand corner of the 
   *                    rectangle containing the axis, specified in pixel 
   *                    coordinates with y  INCREASING UPWARD 
   * @param y0          The y-coordinate, of the lower left hand corner of the 
   *                    rectangle containing the axis, specified in pixel 
   *                    coordinates with y  INCREASING UPWARD 
   * @param width       The width of the rectangle containing the axis in 
   *                    pixels 
   * @param height      The height of the rectangle conatining axis in pixels 
   * @param min         The real number associated with the
   *                    left hand end point of the axis
   * @param max         The real number associated with the
   *                    right hand end point of the axis
   * @param points      The points to mark along the axis
   * @param orientation Specifies whether the axis should be extended in
   *                    the horizontal or vertical direction
   */
  public LogAxis( int         x0,
                  int         y0,
                  int         width, 
                  int         height, 
                  double      min, 
                  double      max, 
                  double[]    points,
                  Orientation orientation )
  {
    super( x0, y0, width, height, min, max, points, orientation );

    log_min = Math.log10( min );
    log_max = Math.log10( max );
 
    if ( max / min <= 10 )                       // points will be linear
      linear_format = LinearAxis.makeFormat( points );
    else
      linear_format = null;                      // use default format 

    labels = new TextDrawable[points.length];
    for ( int i = 0; i < points.length; i++ )
    {
       String text = Format( linear_format, points[i] );
       TextDrawable label = new TextDrawable( text.trim() );

                                                  // adjust first and last if
                                                  // vertical
       if ( orientation == Orientation.VERTICAL )
       {
         if ( i == 0 )
           label.setAlignment( TextDrawable.Horizontal.RIGHT, 
                               TextDrawable.Vertical.BOTTOM  );

         else if ( i == points.length - 1 )
           label.setAlignment( TextDrawable.Horizontal.RIGHT, 
                               TextDrawable.Vertical.TOP  );

         else label.setAlignment( TextDrawable.Horizontal.RIGHT, 
                                  TextDrawable.Vertical.CENTER );
       }
       else                                       // adjust first and last if
       {                                          // horizontal
         if ( i == 0 )
           label.setAlignment( TextDrawable.Horizontal.LEFT,
                               TextDrawable.Vertical.TOP  );

         else if ( i == points.length - 1 )
           label.setAlignment( TextDrawable.Horizontal.RIGHT,
                               TextDrawable.Vertical.TOP  );

         else label.setAlignment( TextDrawable.Horizontal.CENTER,
                                  TextDrawable.Vertical.TOP  );
       }

       Point position = WorldToPixel( points[i], 0.79 );
       label.setPosition( position );
       label.setFont( font );
       labels[i] = label;
    }
  }

  /**
   * Construct a horizontal log axis of the specified dimensions, with
   * the specified calibrations.
   * 
   * @param x0      The x-coordinate, of the lower left hand corner of the 
   *                rectangle containing the axis, specified in pixel 
   *                coordinates with y  INCREASING UPWARD 
   * @param y0      The y-coordinate, of the lower left hand corner of the 
   *                rectangle containing the axis, specified in pixel 
   *                coordinates with y  INCREASING UPWARD 
   * @param width   The width of the rectangle containing the axis in pixels 
   * @param height  The height of the rectangle conatining axis in pixels 
   * @param min     The real number associated with the
   *                left hand end point of the axis
   * @param max     The real number associated with the
   *                right hand end point of the axis
   * @param points  The points to mark along the axis
   */
  public LogAxis( int      x0,
                  int      y0,
                  int      width,
                  int      height,
                  double   min,
                  double   max,
                  double[] points )
  {
    this( x0, y0, width, height, min, max, points, Orientation.HORIZONTAL );
  }


  /**
   *  Attempt to format the specified value in a "nice" compact String form, 
   *  without excessive trailing zeros.
   *
   *  @param  format  If not null, use this format to convert the value into
   *                  string form, otherwise an internally generated format 
   *                  will be used.
   *  @param  point   The value to format
   *
   *  @return A String form of the value.
   */
  public static String Format( String format, double point )
  {
    String text;

    if ( format != null )
      return String.format( format, point ); 

    if ( point < 0.00001 )
      text = String.format("%4.1E", point );
    else if ( point < 0.0001 )
      text = String.format("%4.5f", point );
    else if ( point < 0.001 )
      text = String.format("%4.4f", point );
    else if ( point < 0.01 )
      text = String.format("%4.3f", point );
    else if ( point < 0.1 )
      text = String.format("%4.2f", point );
    else if ( point < 1 )
      text = String.format("%4.1f", point );
    else if ( point < 1000000 )
      text = String.format("%2.0f", point );
    else
      text = String.format("%4.0E", point );

    return text;
  }
  
  
  /**
   * Draw this axis using the specified graphics context, 
   * with any specified color, position and rotation angle.
   * 
   * @param graphics  The graphics context for drawing this axis.
   */
  public void draw(Graphics2D graphics)
  {
    graphics = setAttributes( graphics );     // Use super class method 
                                              // to set up the color, etc. and
                                              // get a new graphics context
                                              // with those attributes set.

    Point left = WorldToPixel( min, 0.99 );
    Point right = WorldToPixel( max, 0.99 );
    graphics.drawLine( left.x, left.y, right.x, right.y ); 
    for ( int i = 0; i < points.length; i++ )
    {
      Point top    = WorldToPixel( points[i], 0.99 );
      Point bottom;
      if ( isPowerOfTen( points[i] ) ) 
        bottom = WorldToPixel( points[i], 0.80 );
      else
        bottom = WorldToPixel( points[i], 0.90 );

      graphics.drawLine( top.x, top.y, bottom.x, bottom.y );
    }


    if ( max / min > 500000 )                // label alternate powers of 10
    {
      int count = 0;
      for ( int i = 0; i < labels.length; i++ )
        if ( isPowerOfTen( points[i] ) )
        {
          if ( count % 2 == 0 )
            labels[i].draw( graphics );
          count++;
        }
    }

    else if ( max / min > 5000 )             // just label powers of 10 
    {
      for ( int i = 0; i < labels.length; i++ )
        if ( isPowerOfTen( points[i] ) )
          labels[i].draw( graphics );
    }

    else if ( max / min >= 10 )               // label 5's and 10's points
    {
      for ( int i = 0; i < labels.length; i++ )
        if ( isPowerOfTen(points[i])           || 
             isThreeTimesPowerOfTen(points[i]) )
          labels[i].draw( graphics );
    }

    else                                      // linear scale, label alternate 
    {                                         // points
      for ( int i = 0; i < labels.length; i++ )
        if ( i % 2 == 0 )
          labels[i].draw( graphics );
    }
 
    graphics.dispose();                       // get rid of the new 
                                              // graphics context 
  }

 
  /**
   * Check whether the specified value is a power of ten, by checking
   * whether its log is an integer, to within a tolerance of 0.00001.
   *
   * @param  x  The number to check.
   *
   * @return true if the parameter is essentially a power of ten.
   */
  public static boolean isPowerOfTen( double x )
  {
    double exponent = Math.log10( x );
    if ( Math.abs((int)exponent - exponent) < 0.00001 )
      return true;
    else
      return false;
  }


  /**
   * Check whether the specified value is 5 times a power of ten.
   *
   * @param  x  The number to check.
   *
   * @return true if the parameter is essentially a 5 times power of ten.
   */
  public static boolean isFiveTimesPowerOfTen( double x )
  {
    double exponent = Math.log10( x );
    double fraction = exponent - Math.floor(exponent);

    if ( Math.abs(fraction - Math.log10(5)) < 0.00001 )
      return true;
    else
      return false;
  }


  /**
   * Check whether the specified value is 3 times a power of ten.
   *
   * @param  x  The number to check.
   *
   * @return true if the parameter is essentially a 3 times power of ten.
   */
  public static boolean isThreeTimesPowerOfTen( double x )
  {
    double exponent = Math.log10( x );
    double fraction = exponent - Math.floor(exponent);

    if ( Math.abs(fraction - Math.log10(3)) < 0.00001 )
      return true;
    else
      return false;
  }


  /**
   *  Calculate the point in pixel coordinates that corresponds to the
   *  specified x, y in "log world coordinates".
   *
   *  @param  x   The horizontal position along the line from the min to max 
   *              scale value.
   *  @param  y   The vertical position between 0 and 1.
   *
   *  @param the pixel point in the panel corresponding to the specified (x,y).
   */
  private Point WorldToPixel( double x, double y )
  {
    int pix_x,
        pix_y;

    double log_x = Math.log10( x );

    if ( orientation == Orientation.HORIZONTAL )
    {
      pix_x = (int)(( log_x - log_min ) * (width-1) / ( log_max - log_min ));
      pix_y = (int)( y * (height-1) );
    }
    else 
    {
      pix_y = (int)(( log_x - log_min ) * (height-1) / ( log_max - log_min ));
      pix_x = (int)( y * (width-1) );
    }

    pix_x += x0;
    pix_y += y0;

    return new Point( pix_x, pix_y );
  }


  /**
   *  Basic functionality test.
   */
  public static void main( String args[] )
  {                              
    int WIDTH  = 500;
    int HEIGHT = 50; 
    JFrame frame = new JFrame("Axis Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(new Dimension(WIDTH,HEIGHT));
                                                  // make a TwoD_JPanel to use
    TwoD_JPanel panel = new TwoD_JPanel();        // for displaying IDrawables
    frame.add( panel );
    frame.setVisible( true );
    
    int     width  = frame.getContentPane().getWidth();
    int     height = frame.getContentPane().getHeight();
 
    double[] points = {1, 3, 10, 30, 100, 300, 1000, 3000, 10000};

    for ( int i = 0; i < points.length; i++ )
      points[i] *= 100;

    double   min = points[0];
    double   max = points[points.length-1]; 

    LogAxis axis = new LogAxis( 100, 100, width, height, min, max, points );

    panel.AddObject( axis );
    panel.draw();
  }

}
