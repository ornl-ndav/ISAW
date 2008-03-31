
/*
 * File:  ProcessmNxData.java
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
 * Revision 1.4  2007/07/11 18:09:08  rmikk
 * Fixed labeled case so that the DataStateInfo corresponds to the other
 *    NXdata's.  These may not have the same dimensions, etc.
 *
 * Revision 1.3  2003/12/09 14:40:34  rmikk
 * Fixed javadoc warnings
 *
 * Revision 1.2  2003/11/23 23:46:16  rmikk
 * Eliminated some Debugging prints
 *
 * Revision 1.1  2003/11/16 21:40:17  rmikk
 * Initial Checkin
 *
 */

package NexIO.Process;
import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import NexIO.Util.*;

/**
 *   This class processes merges all NXdata in an NXentry with the same label 
 *   attribute on their data field. 
 *   This is the generic ProcessmNxData. It assumes that the NXdetector is 
 *   associated with the NXdata is via a link attribute on one of its axes. The
 *   names of the axes are not used( they are determined by their axis attributes).
 *   The associated NXdetector is processed with this classes processDS method.
 *   Currently only a generic processing for NXdetector is used. This processes
 *   multidimensional arrays and puts them into uniform grids( may change).
 */
public class ProcessmNxData implements IProcessNxData {
  String label;
  String errormessage = "";
  public ProcessmNxData( String label){
     this.label = label.trim(); 
  }

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
    *     @param NxinstrumentNode An NxNode with information on the NeXus 
    *                           NXinstrument class.
    *     @param DS    The DataSet(not null) that is to have info added to it
    *     @param State The linked list of state information
    *     @param startGroupID The starting Group ID for the NEW data blocks that are
    *                          added
    */

  public boolean processDS( NxNode NxEntryNode, NxNode NxDataNode , 
              NxNode NxinstrumentNode, DataSet DS , NxfileStateInfo State,
              int startGroupID ){
     
     NxDataStateInfo dsInf = NexUtils.getDataStateInfo( State );
     NxEntryStateInfo entryInf= NexUtils.getEntryStateInfo( State );
     
     if( startGroupID <= 0 && dsInf != null)
        startGroupID = dsInf.startGroupID;
     
     int NDetectors =Grid_util.getAllDataGrids( DS ).size();

     NxDataStateInfo datState = null;
     if( dsInf != null)
        datState = new NxDataStateInfo( dsInf);
     else
        datState =dsInf = new NxDataStateInfo( NxDataNode, State.InstrumentNode,
                             State, startGroupID);

     NxfileStateInfo StateSav =new NxfileStateInfo( State );
     NxDataStateInfo datStateInfo = null;
     NxEntryStateInfo entryStateInfo = null;
     
     //StateSav.Push( datState);
     int lastDetectorID= dsInf.startDetectorID;
     for( int i = 0; i< NxEntryNode.getNChildNodes(); i++){
        NxNode Child = NxEntryNode.getChildNode(i);
        
        if( Child.getNodeClass().equals("NXdata")){
           NxNode datanode = (Child.getChildNode( "data"));
           if( datanode != null)
              if( datanode.getAttrValue("label") != null)
                 if( label.equals( ConvertDataTypes.StringValue
                             ( datanode.getAttrValue("label")).trim())){
                    Process1NxData proc =new Process1NxData();
                   
                    
                    int NDetectors1 = Grid_util.getAllDataGrids( DS ).size();
                    datState.startDetectorID =dsInf.startDetectorID + NDetectors1-NDetectors;
                    NDetectors = NDetectors1;
                    //StateSav.Push( datState );
                    int N = DS.getNum_entries();
                    StateSav = new NxfileStateInfo( State);
                    
                 
                    entryStateInfo = new NxEntryStateInfo(entryInf);
                   
                    if(entryStateInfo != null)
                       StateSav.Push( entryStateInfo );
                    datStateInfo = new NxDataStateInfo(Child,NxinstrumentNode,StateSav,startGroupID);
                    datStateInfo.startDetectorID= lastDetectorID;
                    if(datStateInfo != null){
                       datStateInfo.startDetectorID=lastDetectorID;
                       StateSav.Push( datStateInfo );
                    }
                    boolean res = true;
                    if( StateSav != null){
                        res = proc.processDS(NxEntryNode, Child,
                            NxinstrumentNode, DS, StateSav, startGroupID);
                        lastDetectorID= datStateInfo.endDetectorID+1;
                    }else
                       return setErrorMessage("Not enough State information");
                    if( res)
                       return setErrorMessage( proc.getErrorMessage()); 
                    int N2 = DS.getNum_entries();
                    startGroupID += N2-N;

                 }
        }  
     }
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


