/**
 * File:  LoadTexture.java 
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
 *  $Log: LoadTexture.java,v $
 *  Revision 1.3  2006/07/20 14:05:42  dennis
 *  Reversed the order of the rows in the returned array of bytes.
 *  The last row of the image is now returned first in the array
 *  of bytes.
 *  Made minor additions to the javadocs.
 *
 *
 *  Revision 1.2  2004/11/22 18:47:06  dennis
 *  Removed redundant else clause.
 *
 *  Revision 1.1  2004/11/15 19:04:19  dennis
 *  Initial version.
 */ 

package SSG_Tools.Utils;

import java.awt.*;
import java.awt.image.*;

/**
 *  Utilities for loading texture maps from files.  The static method,
 *  LoadImage(), is used to extract an array of packed RGB pixels, in the
 *  form required by the SSG texture objects.  
 */

public class LoadTexture
{

 /**
  *  Load an image, resample it to the specified size, and return an
  *  array of RGB bytes containing the pixel data.  The image must be in
  *  a form that is supported by the AWT Toolkit.getImage() method.  
  *  Currently, the forms supported are JPEG, PNG and GIF.
  *
  *  @param  filename  The file name from which the image should be loaded
  *  @param  n_rows    The number of rows to which the image will be resampled.
  *                    This must be a power of 2.
  *  @param  n_cols    The number of columns to which the image will be 
  *                    resampled. This must be a power of 2.
  *
  *  @return An array of bytes containing the RGB values for the image read.
  *          The pixel information is stored in the array, in row major order,
  *          with the order of the rows reversed.  That is, the first three 
  *          bytes contain the RGB values for the first pixel in the last row, 
  *          etc.
  */
  public static byte[] LoadImage( String filename, int n_rows, int n_cols )
  {
    System.out.println("Loading Texture Image " + filename );
    Image image;
    image = Toolkit.getDefaultToolkit().getImage( filename );
    image = image.getScaledInstance( n_cols, n_rows, Image.SCALE_SMOOTH );
    
    int packed_pix[] = new int [ n_rows * n_cols ];
    PixelGrabber pg = new  PixelGrabber( image, 
                                         0, 0, n_cols, n_rows, 
                                         packed_pix, 0, n_cols );

    int timeOut = 5000;                  // wait for up to five seconds to load
    try 
    {
      if ( pg.grabPixels( timeOut ) )
      {
        System.out.println("Got the pixels from " + filename );
        byte vals[] = new byte[ n_rows * n_cols * 3 ];
        int  index1,
             index2;
        for ( int row = 0; row < n_rows; row++ )
          for ( int col = 0; col < n_cols; col++ )
          {                                                //extract rgb bytes
            index1 = row * n_cols + col;
            index2 = (n_rows - 1 - row) * n_cols + col;
            vals[3*index1 + 0] = (byte)((packed_pix[index2] >> 16) & 0xFF); 
            vals[3*index1 + 1] = (byte)((packed_pix[index2] >>  8) & 0xFF);
            vals[3*index1 + 2] = (byte)( packed_pix[index2]        & 0xFF);
          }
        return vals;
      }

      System.out.println("ERROR: Couldn't get the pixels from " + filename );
      return null;
    }
    catch ( InterruptedException e )
    {
      System.out.println("ERROR: Getting image " + filename + " FAILED");
      System.out.println("e.getStackTrace()");
      return null;
    }
  }

}
