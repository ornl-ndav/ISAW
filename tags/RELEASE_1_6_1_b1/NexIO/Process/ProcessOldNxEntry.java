
/*
 * File:  ProcessOldNxEntry.java
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
 * Revision 1.2  2003/12/12 15:23:11  rmikk
 * Set the title of the DataSet
 *
 * Revision 1.1  2003/11/23 23:43:57  rmikk
 * Initial Checkin
 *
 * Revision 1.1  2003/11/16 21:40:57  rmikk
 * Initial Checkin
 *
 */

package NexIO.Process;
import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import NexIO.Util.*;
import NexIO.Query.*;

/**
 *   This class processes an NXentry.  There is only one of these. It uses
 *   Queries to get the correct IProcessNxData. It also calls the processors
 *   for the NXbeam, NXsample, etc. to fill out attributes and fields of the
 *   Data Set.
 */

public class ProcessOldNxEntry implements IProcessNxEntry {
  String errormessage="";


   /**
    *  @return an errormessage or an empty string if there is no error
    */
  public String getErrorMessage(){
     return errormessage;
  }

  
  private boolean setErrorMessage( String message){
     errormessage = message;
     return true;
  }

   /**
    *     Method that fills out the DataSet DS from information in the NXdata
    *     or NXmonitor node. NXlog nodes will be added later
    *     @param DS    The DataSet(not null) that is to have info added to it
    *     @param NxEntryNode An NxNode with information on the NeXus NXentry class.
    *     @param NxDataNode  An NxNode with information on the NeXus NXdata class.
    *     @param States The linked list of state information
    *     @param startGroupID The starting Group ID for the NEW data blocks that are
    *                          added
    */
  public boolean processDS( DataSet DS, NxNode NxEntryNode , 
              NxNode NxDataNode,  NxfileStateInfo States, int startGroupID ){
     if( NxEntryNode == null){
        errormessage = "null Entry node in ProcessNxEntry";
        return true;
     }
     if( DS == null){
        errormessage = "No Data Set in ProcessNxEntry";
        return true;
     }

      NXentry_TOFNDGS Entry ;
      DS.setTitle( NxDataNode.getNodeName());
      Entry = new NXentry_TOFNDGS( NxEntryNode , DS ,NxDataNode) ;
      Entry.setNxData( new NxData_Gen() ) ;
      boolean res = Entry.processDS(  DS , NxDataNode ) ;
      if( Entry.getErrorMessage()!= "" )
        errormessage  += ";" + Entry.getErrorMessage() ;
      if( !res )
         errormessage = Entry.getErrorMessage() ;
       return res ;      
    
  } 
 /**
    *   Hook to add new fields to the class that are not in the processDS methods
    *   parameters
    */
   public void setNewInfo( String Name, Object value){}

   /**
    *   @return the current value of the new info associated with Name
    */
   public Object getNewInfo( String Name){return null;}
 
 }


