
/*
 * File:  IProcessNxEntry.java
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.2  2003/12/09 14:40:03  rmikk
 * Fixed javadoc warnings
 *
 * Revision 1.1  2003/11/23 23:43:21  rmikk
 * Initial Checkin
 *
 * Revision 1.1  2003/11/16 21:38:09  rmikk
 * Initial Checkin
 *
 */

package NexIO.Process;
import NexIO.*;
import NexIO.Util.*;
import NexIO.State.*;
import DataSetTools.dataset.*;


/**
 *   Interface describing methods needed by a class to process a NeXus NXdata
 *   node
 */
public interface IProcessNxEntry {
   
   /**
    *     Method that fills out the DataSet DS from information in the NXdata
    *     node.
    *     @param DS  the DataSet(not null) that is to be updated
    *     @param NxEntryNode An NxNode with information on the NeXus NXentry class.
    *     @param NxDataNode  An NxNode with information on the NeXus NXdata class.
    *     @param States The linked list of state information
    *     @param startGroupID The starting Group ID for the NEW data blocks that are
    *                          added
    */
   public boolean processDS( DataSet DS, NxNode NxEntryNode , 
              NxNode NxDataNode,  NxfileStateInfo States, int startGroupID ); 

   /**
    *   Hook to add new fields to the class that are not in the processDS methods
    *   parameters
    */
   public void setNewInfo( String Name, Object value);

   /**
    *   @return the current value of the new info associated with Name
    */
   public Object getNewInfo( String Name);

   /**
    *  @return an errormessage or an empty string if there is no error
    */
   public String getErrorMessage();
  
 }


