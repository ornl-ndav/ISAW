
/*
 * File:  NxInstrumentStateInfo.java
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
 * Revision 1.1  2003/11/16 21:45:04  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;
import NexIO.*;
import NexIO.Util.*;

/**
 *   This class contains state information needed to process an NXinstrument
 *   entry of a NeXus file. The NXdetector fields are processed elsewhere
 */ 
public class NxInstrumentStateInfo extends StateInfo{
   /**
   *  try for names like IPNS,ISIS,LANSCE
   */
   public String facility;

   /**
   *  Examples Pulsed, Reactor etc
   */
   public String type; 
  
   /**
    *   Constructor
    *   @param NxInstrumentNode  An NxNode containing information on a NeXus  
    *                     NXinstrument class 
    *   @param Params   a linked list of State information
    */
   public NxInstrumentStateInfo( NxNode NxInstrumentNode , 
                NxfileStateInfo Params){


   }
  
 }


