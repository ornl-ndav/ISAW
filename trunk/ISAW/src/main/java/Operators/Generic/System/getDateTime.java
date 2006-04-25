/*
 * File:  getDateTime.java
 *
 * Copyright (C) 2004 Thomas Worlton
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
 * Contact : Thomas Worlton <tworlton@anl.gov>
 *           IPNS Division
 *           Argonne National Laboratory
 *           9700 South Cass Ave
 *           Argonne, IL 60439, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4814, USA.
 * 
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2005/01/28 16:17:47  dennis
 * Added getCategoryListMethod() to place operator in the menu system.
 * Changed command name from gDT to getDateTime.
 * Finished documentation of getResult().
 * Added to CVS.
 *
 *
 */
package Operators.Generic.System;

import DataSetTools.operator.*;
import java.util.*;
import java.text.*;

public class getDateTime implements IWrappableWithCategoryList{

  //~ Methods ******************************************************************

  /**
   *  Get the command name to be used in scripts: getDateTime. 
   *
   *  @return the string getDateTime 
   */
  public String getCommand() {
    return "getDateTime";
  }

  /**
   * Get an array of strings listing the operator category names  for 
   * this operator. The first entry in the array is the 
   * string: Operator.OPERATOR. Subsequent elements of the array determine
   * which submenu this operator will reside in.
   * 
   * @return  A list of Strings specifying the category names for the
   *          menu system 
   *        
   */
  public String[] getCategoryList(){

    return Operator.UTILS_SYSTEM;
  }


  public String getDocumentation(  ) {

    StringBuffer s = new StringBuffer(  );
       s.append( "@overview The getDateTime operator returns the current " );
       s.append( "date and time for annotating batch processing." );

       s.append( "@algorithm The getDateTime operator constructs an instance ");
       s.append( "of the current date, then uses DateFormat " );
       s.append( "methods to construct a string with the date " );
       s.append( "in SHORT format and the time in MEDIUM format." );

       s.append("@return getDateTime returns a string containing the current ");
       s.append( "Date and Time." );
       s.append( "@error Describe any unusual or notable errors that could " );
       s.append( "occur. For example: Error occurs if number of bins is zero.");
       return s.toString(  );
  }

  /**
   *  Get the date and time from the system.
   *
   *  @return the date and time in a String. 
   */
  public Object calculate() {
    Date myDate = new Date();
    String dtnow = DateFormat.getDateTimeInstance( DateFormat.SHORT, 
                   DateFormat.MEDIUM).format(myDate); 
    return dtnow;
  }

}
