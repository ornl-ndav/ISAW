/*
 * File:  PeaksFilter.java   
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2002/11/27 23:31:01  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/10/23 19:05:18  pfpeterson
 *  Added to CVS.
 *
 */

package Operators.TOF_SCD;

import javax.swing.filechooser.*;
import java.io.File;

/**
 * This operator is intended to run A.J. Schultz's "blind"
 * program. This is not heavily tested but works fairly well.
 */
public class PeaksFilter extends    FileFilter {
  /**
   * Constructure that does almost nothing
   */
  public PeaksFilter(){
    super();
  }

  /**
   * Determines if the given file will be displayed when this filter
   * is active
   */
  public boolean accept(File file){
    if(file.isDirectory())
      return true;

    String name=file.toString();
    if(name.endsWith(".peaks"))
      return true;
    else if(name.endsWith(".peak"))
      return true;
    else
      return false;
  }

  /**
   * Returns a description that will appear in the dialog
   */
  public String getDescription(){
    return "peaks files (*.peaks)";
  }

}
