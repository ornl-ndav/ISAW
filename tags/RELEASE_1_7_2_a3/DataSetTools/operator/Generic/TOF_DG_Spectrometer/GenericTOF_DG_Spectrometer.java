/*
 * File:  GenericTOF_DG_Spectrometer.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *  $Log$
 *  Revision 1.6  2005/01/10 15:14:08  dennis
 *  Updated java docs for getCategoryList method.
 *
 *  Revision 1.5  2005/01/07 17:52:52  dennis
 *  Now sets category list based on instrument type String from Operator
 *  base class.
 *
 *  Revision 1.4  2003/06/16 19:07:16  pfpeterson
 *  Removed old code and updated to work with new getCategoryList() code
 *  in base operator class.
 *
 *  Revision 1.3  2002/11/27 23:21:55  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/03/05 19:28:17  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:58:51  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.1  2002/01/11 22:13:44  dennis
 *  Abstract base class for Generic TOF Direct Geometry
 *  Spectrometer operators.
 *
 */

package DataSetTools.operator.Generic.TOF_DG_Spectrometer;

import java.io.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.GenericOperator;

/**
 *   Base class for generic operators that work with time-of-flight direct
 * geometry spectrometers. 
 *
 * @see GenericOperator
 * @see DataSetTools.operator.Generic.Save.WriteNexus 
 *
 */

abstract public class GenericTOF_DG_Spectrometer extends    GenericOperator 
                                                 implements Serializable
{
   private static String[] categoryList=null;

   protected GenericTOF_DG_Spectrometer( String title )
   {
      super( title );
   } 

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get a list of strings giving the categories to be used when placing
   * the operator in menus.  The first entry in the array must be the string:
   *
   *      Operator.OPERATOR
   *
   * @return  An array of Strings specifying the category names to use 
   *          for this operator. 
   */
  public String[] getCategoryList()
  {
    if(categoryList==null)
      categoryList = Operator.TOF_NDGS;

    return categoryList;
  }
}
