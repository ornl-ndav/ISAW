/*
 * File: AxisOverlay2D.java
 *
 * Copyright (C) 2003, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.8  2003/06/18 22:14:28  dennis
 *  (Mike Miller)
 *  - Restructured the paint method to allow linear or log axis display.
 *    Paint method now calls private methods to paint the axis.
 *
 *  Revision 1.7  2003/06/18 13:35:25  dennis
 *  (Mike Miller)
 *  - Added method setDisplayAxes() to turn on/off x and/or y axes.
 *
 *  Revision 1.6  2003/06/17 13:22:14  dennis
 *  - Updated help menu. No functional changes made. (Mike Miller)
 *
 *  Revision 1.5  2003/06/09 22:33:33  dennis
 *  - Added static method help() to display commands via the HelpMenu.
 *    (Mike Miller)
 *
 *  Revision 1.4  2003/05/22 17:51:05  dennis
 *  Corrected problem of missing calibratons at beginning and end
 *  of y-axis and beginning of x-axis. (Mike Miller)
 *
 *  Revision 1.3  2003/05/16 14:56:12  dennis
 *  Added calibration intervals on the X-Axis when resizing. (Mike Miller)
 *
 */
 
//******************************Mental Reminders*******************************
// Y axis doesn't display end ranges
// ImageJPanel currently draws over the overlay.
// X axis has a constant position 50 for it's label
//*****************************************************************************
// 4/26/03 -Added calibration intervals on x-axis when resizing

package DataSetTools.components.View.Transparency;

import javax.swing.*; 
import java.awt.*;
import java.awt.event.*;
import java.util.*; 
import DataSetTools.components.image.*; //ImageJPanel & CoordJPanel
import DataSetTools.components.View.*;
import DataSetTools.components.View.TwoD.*;
import DataSetTools.util.*; 
import java.lang.Math;

public class AxisOverlay2D extends OverlayJPanel 
{
   public static final int NO_AXES = 0;
   public static final int X_AXIS = 1;
   public static final int Y_AXIS = 2;
   public static final int DUAL_AXES = 3;
   public static final boolean LINEAR = true;
   public static final boolean LOG    = false;
   // these variables simulate the interval of values of the data
   private float xmin;
   private float xmax;
   private float ymin;
   private float ymax;
   private int xaxis = 0;
   private int yaxis = 0;
   private int xstart = 0;
   private int ystart = 0;
   private IAxisAddible2D component;
   private int precision;
   private Font f;
   private int axesdrawn;
   private boolean drawXLinear;
   private boolean drawYLinear;
   
   public AxisOverlay2D(IAxisAddible2D iaa)
   {
      super();
      component = iaa; 
      precision = iaa.getPrecision();
      f = iaa.getFont();
      xmin = 0;
      xmax = 1;
      ymin = 0;
      ymax = 1;
      axesdrawn = DUAL_AXES;
      drawXLinear = component.getAxisInfo(true).getIsLinear();
      drawYLinear = component.getAxisInfo(false).getIsLinear();       
   }

  /**
   * Contains/Displays control information about this overlay.
   */
   public static void help()
   {
      JFrame helper = new JFrame("Help for Axes Overlay");
      helper.setBounds(0,0,600,400);
      JTextArea text = new JTextArea("Commands for Axes Overlay\n\n");
      helper.getContentPane().add(text);
      text.setEditable(false);
      text.setLineWrap(true);

      text.append("Note:\n" +
                  "- The Axes Overlay has no commands associated with it. " +
                  "Instead, it allows the commands of the underlying image.\n" +
                  "- These commands will NOT work if any other overlay " +
		  "is checked.\n\n");
      text.append("Commands for Underlying image\n");
      text.append("Click/Drag/Release MouseButton2>ZOOM IN\n");
      text.append("Click/Drag/Release Mouse w/Shift_Key>ZOOM IN ALTERNATE\n");
      text.append("Double Click Mouse>RESET ZOOM\n");
      text.append("Single Click Mouse>SELECT CURRENT POINT\n\n");
      
      helper.setVisible(true);
   }
   
  /**
   * Sets the significant digits to be displayed.
   *
   *  @param digits
   */ 
   public void setPrecision( int digits )
   {
      precision = digits;
   } 
   
  /**
   * Specify which axes should be drawn. Options are NO_AXES, X_AXIS, Y_AXIS,
   * or DUAL_AXES.
   *
   *  @param  display_scheme
   */ 
   public void setDisplayAxes( int display_scheme )
   {
      axesdrawn = display_scheme;
   }
   
  /**
   * Specify x axis as linear or logarithmic. Options are LINEAR or LOG.
   *
   *  @param  display_scheme
   */ 
   public void setXAxisLinearOrLog( boolean isXLinear )
   {
      drawXLinear = isXLinear;
   }   
   
  /**
   * Specify y axis as linear or logarithmic. Options are LINEAR or LOG.
   *
   *  @param  display_scheme
   */ 
   public void setYAxisLinearOrLog( boolean isYLinear )
   {
      drawYLinear = isYLinear;
   }   
  
  /**
   * This method creates tick marks and numbers for this transparency.
   * These graphics will overlay onto a jpanel.
   *
   *  @param  graphic
   */  
   public void paint(Graphics g) 
   {  
      Graphics2D g2d = (Graphics2D)g; 
           
      g2d.setFont(f);
      FontMetrics fontdata = g2d.getFontMetrics();
      // System.out.println("Precision = " + precision);
      
      xmin = component.getAxisInfo(true).getMin();
      xmax = component.getAxisInfo(true).getMax();
      
      // ymin & ymax swapped to adjust for axis standard
      ymax = component.getAxisInfo(false).getMin();
      ymin = component.getAxisInfo(false).getMax();
      
      // get the dimension of the center panel (imagejpanel)
      // all of these values are returned as floats, losing precision!!!
      xaxis = (int)( component.getRegionInfo().getWidth() );
      yaxis = (int)( component.getRegionInfo().getHeight() );
      // x and y coordinate for upper left hand corner of component
      xstart = (int)( component.getRegionInfo().getLocation().getX() );
      ystart = (int)( component.getRegionInfo().getLocation().getY() );
           
      // System.out.println("X,Y axis = " + xaxis + ", " + yaxis );
      // System.out.println("X,Y start = " +  xstart + ", " + ystart );  
      
      // draw title on the overlay if one exists
      if( component.getTitle() != IVirtualArray2D.NO_TITLE )
         g2d.drawString( component.getTitle(), xstart + xaxis/2 -
                      fontdata.stringWidth(component.getTitle())/2, 
     	              ystart/2 + (fontdata.getHeight())/2 );

      if( axesdrawn == X_AXIS || axesdrawn == DUAL_AXES )
      {
         if( drawXLinear )
            paintLinearX( g2d );
	 else
	    paintLogX( g2d );
      }
      if( axesdrawn == Y_AXIS || axesdrawn == DUAL_AXES )
      {
         if( drawYLinear )
            paintLinearY( g2d );
	 else
	    paintLogY( g2d );
      }
   } // end of paint()
   
  /* ***********************Private Methods************************** */
   
  /*
   * Draw the x axis with horizontal numbers and ticks spaced linearly.
   */	
   private void paintLinearX( Graphics2D g2d )
   {
      FontMetrics fontdata = g2d.getFontMetrics();
      
      // info for putting tick marks and numbers on transparency   
      String num = "";
      int xtick_length = 5;

      CalibrationUtil util = new CalibrationUtil( xmin, xmax, precision, 
        					  Format.ENGINEER );
      float[] values = util.subDivide();
      float step = values[0];
      float start = values[1];    // the power of the step
      int numxsteps = (int)values[2];	      
   //   System.out.println("X ticks = " + numxsteps );        
      int pixel = 0;
      int subpixel = 0;
      /* xaxis represents Pmax - Pmin
      float Pmin = start;
      float Pmax = start + xaxis;
      float Amin = xmin;
      */
      float A = 0;   
      int exp_index = 0;
      //boolean drawn = false;
      int prepix = (int)( (float)xaxis*(start - xmin)/
        		  (xmax-xmin) + xstart); 
      int skip = 0;
      
      for( int steps = 0; steps < numxsteps; steps++ )
      {  
         A = (float)steps*step + start; 	      
         pixel = (int)( 
     	     	 (float)xaxis*(A - xmin)/
     	     	 (xmax-xmin) + xstart);       
         //System.out.println("Pixel " + pixel );
       //System.out.println("Xmin/Xmax " + xstart + "/" + (xstart + xaxis) );
         subpixel = (int)( 
     	     	 ( (float)xaxis*(A - xmin - step/2 ) )/
     	     	 (xmax-xmin) + xstart);      
 
         num = util.standardize( (step * (float)steps + start) );
         exp_index = num.indexOf('E');        


         if( (prepix + 2 + 
              fontdata.stringWidth(num.substring(0,exp_index))/2) >
             (pixel - fontdata.stringWidth(num.substring(0,exp_index))/2) )
         {
            skip++;
         }

         if( steps%skip == 0 )
         {
            g2d.drawString( num.substring(0,exp_index), 
        	 pixel - fontdata.stringWidth(num.substring(0,exp_index))/2, 
        	 yaxis + ystart + xtick_length + fontdata.getHeight() );
         }	   

         //System.out.println("Subpixel/XStart " + subpixel + "/" + xstart );
         if( subpixel > xstart && subpixel < (xstart + xaxis) )
         {
            g2d.drawLine( subpixel, yaxis + ystart, 
     	     		  subpixel, yaxis + ystart + xtick_length-2 );
         }

         g2d.drawLine( pixel, yaxis + ystart, 
     	     	       pixel, yaxis + ystart + xtick_length );  
         //System.out.println("Y Position: " + (yaxis + ystart + 
         //					xtick_length) );
         if( steps == (numxsteps - 1) && 
             ( xaxis + xstart - pixel) > xaxis/(2*numxsteps) )
         { 
            g2d.drawLine( pixel + (pixel - subpixel), yaxis + ystart, 
     	     		  pixel + (pixel - subpixel), 
     	     		  yaxis + ystart + xtick_length-2 );
            steps++;
            A = (float)steps*step + start;	      
            pixel = (int)( (float)xaxis*(A - xmin)/(xmax-xmin) + xstart); 
            if( steps%skip == 0 && pixel <= (xstart + xaxis) )
            {
               num = util.standardize( (step * (float)steps + start) );
               exp_index = num.indexOf('E');
            
               g2d.drawString( num.substring(0,exp_index), pixel - 
	     	    fontdata.stringWidth(num.substring(0,exp_index))/2, 
        	    yaxis + ystart + xtick_length + fontdata.getHeight() );
               g2d.drawLine( pixel, yaxis + ystart, 
     	     	    pixel, yaxis + ystart + xtick_length );
            }
        		 
         }   
      } // end of for
    
   // This will display the x label, x units, and common exponent (if not 0).
      
      String xlabel = "";
      if( component.getAxisInfo(true).getLabel() != 
          IVirtualArray2D.NO_XLABEL )
         xlabel = xlabel + component.getAxisInfo(true).getLabel();
      if( component.getAxisInfo(true).getUnits() != 
          IVirtualArray2D.NO_XUNITS )
         xlabel = xlabel + "  " + component.getAxisInfo(true).getUnits();
      if( Integer.parseInt( num.substring( exp_index + 1) ) != 0 )
         xlabel = xlabel + "  " + num.substring( exp_index );
      if( xlabel != "" )
         g2d.drawString( xlabel, xstart + xaxis/2 -
        	   fontdata.stringWidth(xlabel)/2, 
        	   yaxis + ystart + fontdata.getHeight() * 2 + 6 );
   } // end of paintLinearX()
   
  /*
   * Draw the y axis with horizontal numbers and ticks spaced linearly.
   */
   private void paintLinearY( Graphics2D g2d )
   {
      FontMetrics fontdata = g2d.getFontMetrics();   
      String num = "";
      int xtick_length = 5;
      
      CalibrationUtil yutil = new CalibrationUtil( ymin, ymax, precision, 
        					   Format.ENGINEER );
      float[] values = yutil.subDivide();
      float ystep = values[0];
      float starty = values[1];
      int numysteps = (int)values[2];
      
      //   System.out.println("Y Start/Step = " + starty + "/" + ystep);
      int ytick_length = 5;	// the length of the tickmark is 5 pixels
      int ypixel = 0;		// where to place major ticks
      int ysubpixel = 0;	// where to place minor ticks
        
      int exp_index = 0;
      	     	 
      float pmin = ystart + yaxis;
      float pmax = ystart;
      float a = 0;
      float amin = ymin - starty;
      
      // yskip is the space between calibrations: 1 = every #, 2 = every other
      
      int yskip = 1;
      while( (yaxis*yskip/numysteps) < 
             fontdata.getHeight() && yskip < numysteps)
         yskip++;
      int mult = (int)(numysteps/yskip);
      int rem = numysteps%yskip;
   //   System.out.println("numysteps/yskip: (" + numysteps + "/" + yskip + 
   //                      ") = " + mult + "R" + rem);
 
      for( int ysteps = numysteps - 1; ysteps >= 0; ysteps-- )
      {   
         a = ysteps * ystep;
     
         ypixel = (int)( (pmax - pmin) * ( a - amin) /
     	     		 (ymax - ymin) + pmin);
   //         System.out.println("YPixel " + ypixel ); 

         //System.out.println("Ymin/Ymax " + ymin + "/" + ymax );
     
         ysubpixel = (int)( (pmax - pmin) * ( a - amin  + ystep/2 ) /
     	     		 (ymax - ymin) + pmin); 
     
         num = yutil.standardize(ystep * (float)ysteps + starty);
         exp_index = num.indexOf('E');

         /*
         System.out.println("Ypixel/Pmin = " + ypixel + "/" + pmin );
         System.out.println("Ypixel/Pmax = " + ypixel + "/" + pmax );
         System.out.println("Num = " + num );
         */
         // if pixel is between top and bottom of imagejpanel, draw it  
         if( ypixel <= pmin && ypixel >= pmax )
         {
            if( ((float)(ysteps-rem)/(float)yskip) == ((ysteps-rem)/yskip) )
            {
               g2d.drawString( num.substring(0,exp_index), 
        		 xstart - ytick_length - 
	     		 fontdata.stringWidth(num.substring(0,exp_index)),
        		 ypixel + fontdata.getHeight()/4 );
            }		   

            g2d.drawLine( xstart - ytick_length, ypixel - 1, 
        		  xstart - 1, ypixel - 1 );   
         }
         // if subpixel is between top and bottom of imagejpanel, draw it
         if( ysubpixel <= pmin && ysubpixel >= pmax )
         {
            g2d.drawLine( xstart - (ytick_length - 2), ysubpixel - 1, 
     	     		  xstart - 1, ysubpixel - 1 );
         }
         // if a tick mark should be drawn at the end, draw it
         // since the above "if" takes care of all subtick marks before the
         // actual numbered ticks, there may be a tick mark needed after the 
         // last tick. 
         if( ysteps == 0 && 
             (pmin - ypixel) > yaxis/(2*numysteps) ) 
         {
            g2d.drawLine( xstart - (ytick_length - 2), 
               (int)(ysubpixel + ( (pmin - pmax) * ystep / (ymax - ymin) ) ),
	       xstart - 1, 
     	       (int)( ysubpixel + ( (pmin - pmax) * ystep / (ymax - ymin))));
         }
      }
     
   // This will display the y label, y units, and common exponent (if not 0).
      
      String ylabel = "";
      if( component.getAxisInfo(false).getLabel() != 
          IVirtualArray2D.NO_YLABEL )
         ylabel = ylabel + component.getAxisInfo(false).getLabel();
      if( component.getAxisInfo(false).getUnits() != 
          IVirtualArray2D.NO_YUNITS )
         ylabel = ylabel + "  " + component.getAxisInfo(false).getUnits();
      if( Integer.parseInt( num.substring( exp_index + 1) ) != 0 )
         ylabel = ylabel + "  " + num.substring( exp_index );
      if( ylabel != "" )
      {
         g2d.rotate( -Math.PI/2, xstart, ystart + yaxis );    
         g2d.drawString( ylabel, xstart + yaxis/2 -
        		 fontdata.stringWidth(ylabel)/2, 
        		 yaxis + ystart - xstart + fontdata.getHeight() );
         g2d.rotate( Math.PI/2, xstart, ystart + yaxis );
      }
   } // end of paintLinearY()
   
  /*
   * Draw the x axis with horizontal numbers and ticks spaced logarithmically.
   */   
   private void paintLogX( Graphics2D g2d )
   {
   
   }
   
  /*
   * Draw the y axis with horizontal numbers and ticks spaced logarithmically.
   */    
   private void paintLogY( Graphics2D g2d )
   {
   
   }
}
