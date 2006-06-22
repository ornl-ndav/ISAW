/*
 * File:  ImageFrame.java
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2005/05/25 18:01:14  dennis
 * Replaced direct call to .show() method for window,
 * since .show() is deprecated in java 1.5.
 * Now calls WindowShower.show() to create a runnable
 * that is run from the Swing thread and sets the
 * visibility of the window true.
 *
 * Revision 1.6  2004/05/10 22:43:10  dennis
 * Test program now just instantiates a Frame to diplay
 * calculated result, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.5  2004/03/15 19:33:58  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.4  2004/03/15 03:28:43  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.3  2003/06/05 14:35:09  dennis
 * Added method to set a new image and change the title on the frame.
 *
 * Revision 1.2  2003/01/08 20:11:37  dennis
 * Now shows blank frame and writes error message if a null image is
 * specified.
 *
 * Revision 1.1  2003/01/08 19:23:05  dennis
 * Initial version
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Panels.Image.*;
import javax.swing.*;

/**
 *  Simple class to display an image, specified by a 2D array of floats,
 *  in a frame.
 */
public class ImageFrame extends JFrame
{
  ImageJPanel panel;

  /**
   *  Construct a frame with the specified image and title
   */
  public ImageFrame( float values[][], String title )
  {
    setTitle(title);
    setBounds(0,0,500,500);
    panel = new ImageJPanel();
    panel.changeLogScale(25, true);
    setData( values );
    getContentPane().add(panel);
    setVisible(true);
  }

  public void setData( float values[][] )
  {
    if ( values == null )
    {
      float empty_array[][] = new float[1][1];
      panel.setData( empty_array, true );
      System.out.println("No Image Plane Defined...!!");
    }
    else
      panel.setData( values, true );
     
    WindowShower.show(this);
  }


  public void setData( float values[][], String title )
  {
    setTitle( title );
    if ( values == null )
    {
      float empty_array[][] = new float[1][1];
      panel.setData( empty_array, true );
      System.out.println("No Image Plane Defined...!!");
    }
    else
      panel.setData( values, true );

    WindowShower.show(this);
  }


  public static void main( String args[] )
  {
    float test_array[][] = new float[500][500];
    for ( int i = 0; i < 500; i++ )
      for ( int j = 0; j < 500; j++ )
        test_array[i][j] = i + j;

    new ImageFrame( test_array, "Test" );
  }

}
