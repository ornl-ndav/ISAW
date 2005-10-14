

/*
 * File:  GenericFileFilter.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2004/03/17 20:24:12  dennis
 * Fixed @see tag that was broken when view components, math and
 * util were moved to gov package.
 *
 * Revision 1.3  2004/03/15 19:38:22  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.2  2004/03/15 03:37:40  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.1  2003/11/11 20:47:16  rmikk
 * Initial Checkin
 *
 */
package Wizard.TOF_SAD;
import gov.anl.ipns.Util.File.*;

/**
*    This class creates arbitrary File Filters with arbitrary extensions
*/
public class GenericFileFilter extends RobustFileFilter{
   
   /**
    *  Constructor
    *  @param  Extension the filename extension that will yield a positive accept
    *  @param  Description  A Description for this extension
    *  @see    gov.anl.ipns.Util.File.RobustFileFilter
    */
   public GenericFileFilter(String Extension, String Description){
      super();
      add( Extension, Description);
   }

   /**
    *    Adds further filename Extensions that will be accepted by this file
    *    Filter
    *  @param  FileExtension the filename extension that will yield a positive accept
    *  @param  Description  A Description for this extension
    */
   public void add( String FileExtension, String Description){
        super.addExtension( FileExtension);
        super.setDescription( Description);


   }



}
