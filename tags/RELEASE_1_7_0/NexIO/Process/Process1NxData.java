
/*
 * File:  Process1NxData.java
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
 * Revision 1.2  2003/12/09 14:40:18  rmikk
 * Fixed javadoc warnings
 *
 * Revision 1.1  2003/11/16 21:39:38  rmikk
 * Initial Checkin
 *
 */

package NexIO.Process;
import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import NexIO.Util.*;

/**
 *   This class processes ONE NXdata class in a NeXus file, updating a DataSet.
 *   This is the generic Process1NxData. It assumes that the NXdetector is 
 *   associated with the NXdata is via a link attribute on one of its axes. The
 *   names of the axes are not used( they are determined by their axis attributes).
 *   The associated NXdetector is processed with this classes processDS method.
 *   Currently only a generic processing for NXdetector is used. This processes
 *   multidimensional arrays and puts them into uniform grids( may change).
 */
 
public class Process1NxData implements IProcessNxData {
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
   *     node.
   *     @param NxEntryNode An NxNode with information on the NeXus NXentry class.
   *     @param NxDataNode  An NxNode with information on the NeXus NXdata class.
   *     @param NxInstrument An NxNode with information on the NeXus 
   *                           NXinstrument class.
   *     @param DS    The DataSet(not null) that is to have info added to it
   *     @param States The linked list of state information
   *     @param startGroupID The starting Group ID for the NEW data blocks that are
   *                          added
   */
  public boolean processDS( NxNode NxEntryNode ,NxNode NxDataNode , 
              NxNode NxInstrument, DataSet DS , NxfileStateInfo States,
              int startGroupID ){
     int firstIndex = DS.getNum_entries();

     NxDataStateInfo DataState = new NxDataStateInfo( NxDataNode, 
             NxInstrument , States, startGroupID);

     States.Push( DataState);
     NxInstrumentStateInfo InstrumentState = new NxInstrumentStateInfo(
             NxInstrument, States);

     States.Push( InstrumentState);
       
     
     NxNode NxDetectorNode = NexUtils.getCorrespondingNxDetector( DataState.linkName,
              NxInstrument);
     NxDetectorStateInfo DetState = null;
     if( NxDetectorNode == null){
        DataSetTools.util.SharedData.addmsg( "no NxDetector Node for "+
                       NxDataNode.getNodeName());
       
     }
     else 
        DetState = new NxDetectorStateInfo(NxDetectorNode, States);
     if( DetState != null)
        States.Push( DetState);
     NexUtils  nxut = new NexUtils();
     boolean res= nxut.setUpNxData (DS,NxDataNode,DataState.startGroupID,States);
     if( res) 
        return setErrorMessage(nxut.getErrorMessage( ));

     // Here get the correct setUpNxDetectorAttribg  
     
     if( NxDetectorNode != null)
        res = nxut.setUpNXdetectorAttributes( DS, NxDataNode,NxDetectorNode,
                                         firstIndex, States);
  
     if( res)
         return setErrorMessage(nxut.getErrorMessage( ));
     
     return false;
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


