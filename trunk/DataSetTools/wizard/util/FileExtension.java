/*
 * File:  FileExtension.java
 *
 * Copyright (C) 2003, Christopher M. Bouzek
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
 * Contact : Chris Bouzek <coldfusion78@yahoo.com>
 *           Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 */
package DataSetTools.wizard.util;

import javax.swing.filechooser.FileFilter;
import java.io.*;

/**
 *  This class puts a FileExtension on a File, using the given FileFilter.
 */
public class FileExtension
{
  /**
   *  Default constructor.
   */
  private FileExtension()
  {
  }

  /**
   *  This method changes a File extension by changing the
   *  File name itself.  This is pretty much only useful if
   *  you want to be sure that your files are being saved with 
   *  the correct extension when you are saving a new File.
   */
  public static String appendExtension(String filename, FileFilter filter)
  {
    String ext;
    int dotindex;

    dotindex = filename.indexOf('.');

    if(dotindex < 0) 
    {  
      ext = filter.getDescription();
      dotindex = ext.indexOf('.');
      //three character extension, including the period
      ext = ext.substring(dotindex, dotindex + 4);
      filename = filename + ext;
    }
    return filename;
  }
}
