
/*
 * File:  QueryNxEntry.java
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
 * Revision 1.5  2006/11/14 16:45:09  rmikk
 * Used and xml Fixit file.
 *
 * Revision 1.4  2006/07/25 00:07:36  rmikk
 * Set the axis =1 if it was incorrect, so the time axis correct
 *
 * Revision 1.3  2004/02/16 02:17:55  bouzekc
 * Removed unused imports.
 *
 * Revision 1.2  2003/12/09 14:39:33  rmikk
 * Fixed javadoc warnings
 *
 * Revision 1.1  2003/11/23 23:47:35  rmikk
 * Initial Checkin
 *
 * Revision 1.1  2003/11/16 21:37:15  rmikk
 * Initial Checkin
 *
 */

package NexIO.Query;
import org.w3c.dom.Node;

import NexIO.*;
import NexIO.State.*;
import NexIO.Process.*;
import NexIO.Util.*;

/**
 *   This class's getNxDataProcessor returns the correct IProcessNxData 
 *   depending on the State information
 */
public class QueryNxEntry {

  
  /**
   *   Returns the proper IProcessNxData class that is also properly configured
   *   @param State The linked list of state information, Both file and NxEntry info's
   *                 must be present
   *   @param NxDataNode  An NxNode with information on the NeXus NXdata class.
   *   @param NxInstrumentNode An NxNode with information on the NeXus 
   *                        NXinstrument class.
   */
  public static IProcessNxEntry getNxEntryProcessor( NxfileStateInfo State ,
            NxNode NxDataNode , NxNode NxInstrumentNode , int startGroupID ) {

      if( State == null )
         return null;
      NxDataStateInfo dataState = NexUtils.getDataStateInfo( State );
      NxEntryStateInfo EntryInfo = NexUtils.getEntryStateInfo( State );
      NxDetectorStateInfo detStateInfo = NexUtils.getDetectorStateInfo( State );
      NxfileStateInfo fileInfo = State;

      if( NxDataNode == null )
         return new ProcessNxEntry();
      if( dataState == null ) {
         dataState = new NxDataStateInfo( NxDataNode , NxInstrumentNode ,
                  State , startGroupID );
         State.Push( dataState );
      }

      if( fileInfo.xmlDoc != null ) {

         Node xx = Util
                  .getNXInfo( fileInfo.xmlDoc , "axis" , "1" , null , null );

         if( xx != null ) {

            String S = xx.getNodeValue().trim();
            if( dataState.axisName == null )
               dataState.axisName = new String[ 3 ];
            dataState.axisName[ 0 ] = S;

         }
         xx = Util.getNXInfo( fileInfo.xmlDoc , "layout" , null , null , null );
         if( xx != null )
            if( detStateInfo != null ) {

               String S = xx.getNodeValue().trim();
               detStateInfo.hasLayout = S;
            }

      }

      if( ( dataState.linkName == null ) && ( fileInfo.xmlDoc == null ) )
         return new ProcessOldNxEntry();
      else
         return new ProcessNxEntry();
   }
  
 }


