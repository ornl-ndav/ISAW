/*
 * File: ColorScaleImage.java
 *
 * Copyright (C) 2001-2003 Dennis Mikkelson
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
 * $Log$
 * Revision 1.4  2003/07/05 19:39:45  dennis
 * - Altered 2nd constructor to take in integer code instead of
 *   boolean codes.  These new integer codes allow for specification
 *   of vertical/horizontal and one-/two-sided color models. (Mike Miller)
 * - Merged previous log message. (dennis)
 *
 *
 * Revision 1.3  2003/06/18 13:40:46  dennis
 * (Mike Miller)
 * - Added constructor and static variables to allow color
 *   scale image to be displayed vertically or horizontally.
 *
 * Revision 1.2  2002/11/27 23:13:34  pfpeterson
 * standardized header
 *
 */

package DataSetTools.components.ui;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import DataSetTools.components.image.*;

/**
 *
 *  Objects of this class are a simple "ramp" color scale giving the 
 *  correlation between an index and a color for the current pseudo-color
 *  scale.
 */

public class ColorScaleImage extends    ImageJPanel 
                             implements Serializable 
{
  public static final int HORIZONTAL_SINGLE = 0; // single color model
  public static final int HORIZONTAL_DUAL   = 1; // dual color model
  public static final int VERTICAL_SINGLE   = 2; // single color model
  public static final int VERTICAL_DUAL     = 3; // dual color model
 
 /* ------------------------------ CONSTRUCTOR ---------------------------- */
 /** 
  *
  *  Construct a ramp color scale showing the default colors.  The base class's
  *  setNamedColorModel() method can be used to change the color scale.
  *
  */
  public ColorScaleImage( )
  { 
    float color_scale_data[][] = new float[1][255];
    for ( int i = -127; i <= 127; i++ )
      color_scale_data[0][i+127] = i;

    setData( color_scale_data, false );  
  }
  
 /**
  *  This constructor allows for four types of color scale images. The four
  *  types are: HORIZONTAL_SINGLE, HORIZONTAL_DUAL, VERTICAL_SINGLE, and
  *  VERTICAL_DUAL.
  * 
  *  @param  type
  */
  public ColorScaleImage( int type  )
  { 
    super(); 
    
    // Horizontal color scale
    if( type < 2 )
    {
      // Horizontal with single color model
      if( type == 0 )
      {   
        float color_scale_data[][] = new float[1][127];
        for ( int i = 0; i < 127; i++ )
          color_scale_data[0][i] = i;
      
        setData( color_scale_data, false );
      }
      // Horizontal with dual color model
      else
      {
        float color_scale_data[][] = new float[1][255];
        for ( int i = -127; i <= 127; i++ )
          color_scale_data[0][i+127] = i;

        setData( color_scale_data, false );
      }
    }
    // Vertical color scale
    else
    {
      // Verticl with single color model
      if( type == 2 )
      {
        float color_scale_data[][] = new float[127][1];
        for ( int i = 126; i >= 0; i-- )
          color_scale_data[i][0] = 126 - i;

        setData( color_scale_data, false );
      }
      // Vertical with dual color model
      else
      {      
        float color_scale_data[][] = new float[255][1];
        for ( int i = -127; i <= 127; i++ )
          color_scale_data[i+127][0] = -i;

        setData( color_scale_data, false );
      }
    }      
  }

}
