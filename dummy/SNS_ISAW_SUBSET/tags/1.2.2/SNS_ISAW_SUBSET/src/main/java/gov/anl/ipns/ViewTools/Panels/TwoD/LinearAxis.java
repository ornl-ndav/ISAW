/*
 * File: LinearAxis.java 
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
 *  $Date: 2008-12-05 00:42:15 -0600 (Fri, 05 Dec 2008) $            
 *  $Revision: 5714 $
 */

package gov.anl.ipns.ViewTools.Panels.TwoD;


import java.awt.*;
import javax.swing.*;

/**
 * This class is a Drawable that draws a calibrated axis with the
 * specified points marked.
 * 
 * @author Dennis Mikkelson
 *
 */
public class LinearAxis extends Axis
{
   /**
   * Construct an axis of the specified dimensions, with
   * the specified calibrations and orientation.
   * 
   * @param x0          The x-coordinate, of the lower left hand corner of the 
   *                    rectangle containing the axis, specified in pixel 
   *                    coordinates with y  INCREASING UPWARD 
   * @param y0          The y-coordinate, of the lower left hand corner of the 
   *                    rectangle containing the axis, specified in pixel 
   *                    coordinates with y  INCREASING UPWARD 
   * @param width       The width of the axis box in pixels 
   * @param height      The height of the axis box in pixels 
   * @param min         The real number associated with the
   *                    left hand end point of the axis
   * @param max         The real number associated with the
   *                    right hand end point of the axis
   * @param points      The points to mark along the axis
   * @param orientation Specifies whether the axis should be extended in
   *                    the horizontal or vertical direction
   */
  public LinearAxis( int         x0,
                     int         y0,
                     int         width, 
                     int         height, 
                     double      min, 
                     double      max, 
                     double[]    points,
                     Orientation orientation )
  {
    super( x0, y0, width, height, min, max, points, orientation );
 
    if ( points == null )
      throw new IllegalArgumentException( "points array is NULL" );

    if ( points.length < 2 )
      throw new IllegalArgumentException( "Less than two points in array" );

    double border = (points[1] - points[0])/2;   // space for label at first
                                                 // and last tick marks
    int    last   = points.length - 1;

    String format = makeFormat( points ); 

    labels = new TextDrawable[points.length];

    for ( int i = 0; i < points.length; i++ )
    {
       String text = String.format(format, points[i] );
       TextDrawable label = new TextDrawable( text.trim() );

                                                  // adjust first and last if
                                                  // vertical
       if ( orientation == Orientation.VERTICAL )
       {
         label.setAlignment( TextDrawable.Horizontal.RIGHT,
                             TextDrawable.Vertical.CENTER  );

         if ( i == 0 && (points[0] - min) < border )
           label.setAlignment( TextDrawable.Horizontal.RIGHT,
                               TextDrawable.Vertical.BOTTOM  );

         else if ( i == points.length - 1 && (max - points[last]) < border )
           label.setAlignment( TextDrawable.Horizontal.RIGHT,
                               TextDrawable.Vertical.TOP  );
       }
       else                                       // adjust first and last if
       {                                          // horizontal
         label.setAlignment( TextDrawable.Horizontal.CENTER, 
                             TextDrawable.Vertical.TOP  );

         if ( i == 0 && (points[0] - min) < border )
           label.setAlignment( TextDrawable.Horizontal.LEFT, 
                               TextDrawable.Vertical.TOP  );

         else if ( i == points.length - 1 && (max - points[last]) < border )
           label.setAlignment( TextDrawable.Horizontal.RIGHT, 
                               TextDrawable.Vertical.TOP  );
       }

       Point position = WorldToPixel( points[i], 0.79 );

       label.setPosition( position );
       label.setFont( font );
       labels[i] = label;
    }
  }


  /**
   * Construct a horizontal axis of the specified dimensions, with
   * the specified calibrations.
   * 
   * @param x0      The x-coordinate, of the lower left hand corner of the 
   *                rectangle containing the axis, specified in pixel 
   *                coordinates with y  INCREASING UPWARD 
   * @param y0      The y-coordinate, of the lower left hand corner of the 
   *                rectangle containing the axis, specified in pixel 
   *                coordinates with y  INCREASING UPWARD 
   * @param width   The width of the axis box in pixels 
   * @param height  The height of the axis box in pixels 
   * @param min     The real number associated with the
   *                left hand end point of the axis
   * @param max     The real number associated with the
   *                right hand end point of the axis
   * @param points  The points to mark along the axis
   */
  public LinearAxis( int      x0,
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
   *  Construct a format string describing an appropriate format to use
   *  for the specified points.
   *
   *  @param  points  an array of floating point values in increasing order
   *
   *  @return A string, such as %6.3E" describing a format to use for the
   *          points.
   */
  public static String makeFormat( double[] points )
  {
    String format = "%3.2f";
    if ( points.length > 1 )
    {
      int n_decimal = 0;

      double m_step = 1.1*(points[1] - points[0]); // modified step size allows
                                                   // some rounding error
      int exponent = (int)Math.floor( Math.log10( m_step ) );
      if ( exponent < 0 )
        n_decimal = -exponent;

      double first = points[0];
      double last  = points[ points.length - 1 ];
      int n_digits = (int)Math.max( Math.log10( Math.abs(first) ),
                                    Math.log10( Math.abs(last) )  );

      if ( n_digits >= 6 || n_decimal > 6 )        // use scientific notation
      {
        int sig_fig = n_digits - exponent;
        if ( n_digits < 0 )
          sig_fig -= 1;
        format = "%" + (sig_fig+4) + "." + sig_fig + "E";
      }
      else
        format = "%" + (n_digits + n_decimal + 2) + "." + n_decimal + "f";
    }

    return format;
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
      if ( i % 2 == 0 )
        bottom = WorldToPixel( points[i], 0.80 );
      else
        bottom = WorldToPixel( points[i], 0.90 );

      graphics.drawLine( top.x, top.y, bottom.x, bottom.y );
    }

    for ( int i = 0; i < labels.length; i++ )
      if ( i % 2 == 0 )
        labels[i].draw( graphics );

    graphics.dispose();                       // get rid of the new 
                                              // graphics context 
  }


  /**
   *  Calculate the point in pixel coordinates that corresponds to the
   *  specified x, y in "world coordinates".
   *
   *  @param  x   The relative position along the line from the min to max 
   *              scale value.
   *  @param  y   The position between 0 and 1.
   *
   *  @param the pixel point in the panel corresponding to the specified (x,y).
   */
  private Point WorldToPixel( double x, double y )
  {
    int pix_x,
        pix_y;

    if ( orientation == Orientation.HORIZONTAL )
    {
      pix_x = (int)( ( x - min ) * (width-1) / ( max - min ) );
      pix_y = (int)( y * (height-1) );
    }
    else                                      // switch x,y for vertical
    {
      pix_y = (int)( ( x - min ) * (height-1) / ( max - min ) );
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
    int WIDTH  = 300;
    int HEIGHT = 100; 
    JFrame frame = new JFrame("Axis Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(new Dimension(WIDTH,HEIGHT));
                                                  // make a TwoD_JPanel to use
    TwoD_JPanel panel = new TwoD_JPanel();        // for displaying IDrawables
    frame.add( panel );
    frame.setVisible( true );
    
    int     width  = frame.getContentPane().getWidth();
    int     height = frame.getContentPane().getHeight();
    double  min = -10;
    double  max = 110;
    double[] points = {0, 25, 50, 75, 100};
    LinearAxis axis = new LinearAxis( 0, 0, width, height, min, max, points,
                                      Orientation.VERTICAL );

    panel.AddObject( axis );
    panel.draw();
  }

}
