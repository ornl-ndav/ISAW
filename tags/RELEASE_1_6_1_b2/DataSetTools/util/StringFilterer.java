/*
 * File: StringFilterer.java
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
 *  Revision 1.5  2004/01/24 21:03:47  bouzekc
 *  Added javadocs.
 *
 *  Revision 1.4  2003/11/16 18:56:06  bouzekc
 *  Added javadoc comments to isOkay().
 *
 *  Revision 1.3  2003/08/14 18:51:35  bouzekc
 *  Now extends Serializable.
 *
 *  Revision 1.2  2002/11/27 23:23:49  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/06/06 16:03:18  pfpeterson
 *  Added to CVS.
 *
 *
 *
 */
 
package DataSetTools.util;

import java.io.Serializable;

/**
 * Internal class to do all of the formatting checks and pass out
 * PropertChange events to listeners. Should only be used from within
 * the package.
 */
public interface StringFilterer extends Serializable{
    /**
     * This method is designed to test whether or not a given String
     * would be accepted by this StringFilter.
     * 
     * @param  offs                 The offset of the entry point in the
     *                              existing String curString.
     * @param  inString             The String you want to insert.
     * @param  curString            The String which currently exists.
     *
     * @return true if it would be OK to insert inString into curString based
     * on the rules of this filter.
     */
    public boolean isOkay(int offs, String inString, String curString);
    
	/**
	 * Utility to return the inString turned into upper case.
	 *
	 * @param offs Unused.
	 * @param inString The String to change to uppercase.
	 * @param curString Unused.
	 *
	 * @return inString changed to uppercase.
	 */
    public String modifyString(int offs, String inString, String curString);
}
