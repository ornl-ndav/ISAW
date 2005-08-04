/*
 * File:  DS_TOF_DG_Spectrometer.java 
 *
 * Copyright (C) 2002, Peter Peterson
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
 *  Revision 1.4  2003/06/16 19:05:05  pfpeterson
 *  Removed old code and updated to work with new getCategoryList() code
 *  in base operator class.
 *
 *  Revision 1.3  2002/11/27 23:19:32  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/03/05 19:26:18  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 21:04:08  pfpeterson
 *  Operator reorganization.
 *
 *
 */

package DataSetTools.operator.DataSet.TOF_DG_Spectrometer;

import java.io.*;
import DataSetTools.operator.DataSet.DataSetOperator;

/**
 * Base class for TOF direct geometry spectrometers that carry out
 * specialized operations on specific types of DataSets.
 *
 * @see DataSetOperator
 * @see DataSetTools.operator.DataSet.Special.MonitorPeakArea 
 *
 */

abstract public class DS_TOF_DG_Spectrometer extends    DataSetOperator 
                                 implements Serializable
{
   private static String[] categoryList=null;
   protected DS_TOF_DG_Spectrometer( String title )
   {
      super( title );
   } 

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get an array of strings listing the operator category names of base
   * classes for this operator.  The first entry in the array is the string:
   *
   *      Operator.OPERATOR
   *
   * The last entry is the category of the last abstract base class that is
   * is a base class for the current operator.
   * 
   * @return  A list of Strings specifying the category names of the abstract
   * base classes from which this operator is derived.
   */
  public String[] getCategoryList()
  {
    if(categoryList==null)
      categoryList=createCategoryList();

    return categoryList;
  }
} 
