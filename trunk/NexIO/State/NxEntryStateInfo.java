
/*
 * File:  NxEntryStateInfo.java
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
 * Revision 1.2  2003/12/12 17:27:48  rmikk
 * If an NXentry has no "description" SDS field, the "analysis"(old) field is also
 *   checked
 *
 * Revision 1.1  2003/11/16 21:43:46  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;
import NexIO.*;
import NexIO.Util.*;

/**
 *    This class contains state information needed to process an NXentry class
 *   of a NeXus file
 */ 
public class NxEntryStateInfo extends StateInfo{

  /**
   *   The analysis type like TOFNPD, etc
   */
  public String description;

  /**
   *   The Name of the NXentry node
   */
  public String Name;
   
  /**
   *   Constructor
   *   @param NxEntryNode  an NxNode containing information on a NeXus NXentry
   *                       class
   *   @param Params    a linked list of State information
   */
  public NxEntryStateInfo( NxNode NxEntryNode, 
                NxfileStateInfo Params ){
     Name = NxEntryNode.getNodeName();
     description = NexUtils.getStringFieldValue( NxEntryNode,"description");
     if( description == null)
       description = NexUtils.getStringFieldValue( NxEntryNode,"analysis");

  }
  
}


