
/*
 * File:  ProcessNxEntry.java
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

public class ProcessNxEntry  {
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
     boolean monitorDS = false;
     if( NxDataNode == null){ // Monitor DataSet
        int k = 0;
        monitorDS = true;
        DS.setTitle("M:"+ NxEntryNode.getNodeName());
        Integer instType = ((Integer) DS.getAttributeValue( Attribute.INST_TYPE));
        if( instType != null)
        DataSetFactory.addMonitorOperators( DS,instType.intValue());
        for( int child =0; child < NxEntryNode.getNChildNodes(); child++){
           NxNode Child = NxEntryNode.getChildNode( child);
           if( Child.getNodeClass().equals("NXmonitor")){
              NxMonitor nxMon = new NxMonitor();
              nxMon.setMonitorNum( startGroupID+k);
              k++;
              boolean res = nxMon.processDS(Child, DS);
              if( res){
                 errormessage = nxMon.getErrorMessage();
                 return res;
             }
           }
        }
     }else{  //Histogram Data Set  check if node class is NXlog
        
        DS.setTitle( NxDataNode.getNodeName());
        Integer instType = ((Integer) DS.getAttributeValue( Attribute.INST_TYPE));
        if( instType != null)
        DataSetFactory.addOperators( DS,  instType.intValue());
        NxNode NxInstrumentNode = null;
        NxNode NxBeamNode = null;
        for( int child =0; (child < NxEntryNode.getNChildNodes()); child++){
           NxNode Child = NxEntryNode.getChildNode( child);
           if( Child.getNodeClass().equals("NXinstrument"))
              NxInstrumentNode = Child;
           else if( Child.getNodeClass().equals("NXbeam"))
              NxBeamNode = Child;
        }

        //Query for process NxData
        //get instrument Node

        //Process1NxData proc = new Process1NxData();
        IProcessNxData proc = QueryNxData.getNxDataProcessor( States, 
               NxDataNode, NxInstrumentNode);
        boolean res = proc.processDS(NxEntryNode, NxDataNode, NxInstrumentNode, 
                                         DS, States, startGroupID);
        if( res){
           errormessage = proc.getErrorMessage();
           return res;
        } 
       
     }
     //--------------- Run Number DataSet Attribute ---------------
     Integer run_num= NexUtils.getIntFieldValue( NxEntryNode, "run_number");
     if( run_num != null ){
        int[] runs = new int[1];
        runs[0] = run_num.intValue();
        DS.setAttribute( new IntListAttribute( Attribute.RUN_NUM, runs));
     }

     //------------------ Title DataSet Attribute?? -----------------
     ConvertDataTypes.addAttribute( DS,
         ConvertDataTypes.CreateStringAttribute( Attribute.TITLE,
               NexUtils.getStringFieldValue( NxEntryNode, "title")));
   
    //---------------- Number of pulses DataSet attribute ------------------
    Float X = NexUtils.getFloatFieldValue(NxEntryNode, "duration" );
    if(X !=null )
       if(! X.isNaN())
        DS.setAttribute( new FloatAttribute( Attribute.NUMBER_OF_PULSES,
                                             X.floatValue()*30.f ) );

    //--------------Attributes from other NXentry children -----------
      for( int i = 0; i < NxEntryNode.getNChildNodes(); i++ ){
      NxNode datanode = NxEntryNode.getChildNode( i );
      String C = datanode.getNodeClass();
      
      if( C.equals( "NXinstrument" ) ){
        NxInstrument nx = new NxInstrument();
        
        if( !monitorDS )
          if( !nx.processDS( datanode, DS ) ){
            // do nothing
          }else
            errormessage += ":" + nx.getErrorMessage();
      }else  if( C.equals( "NXsample" ) ){
        NxSample ns = new NxSample();
        
        if( !ns.processDS( datanode, DS ) ){
          // do nothing
        }else
          errormessage += ";" + ns.getErrorMessage();
      }else if( C.equals( "NXbeam" ) ){
        NxBeam nb = new NxBeam();
        
        if( !monitorDS )
          if( nb.processDS( datanode, DS ) )
            errormessage += ";" + nb.getErrorMessage();
      }

    }
    if( errormessage != null)
      if( errormessage.length() >1)
        return true;
    Object X1 = DS.getAttributeValue( Attribute.RUN_NUM );
    int[] run_numm = null;
    
    if( X1 != null )
      if( X1 instanceof int[] )
        run_numm = (int[])X1;
    int npulses = -1;

    X1 = DS.getAttributeValue( Attribute.NUMBER_OF_PULSES );
    if( X1 instanceof Number )
      npulses = ( ( Number )X1 ).intValue();
    
    float initial_path = -1;
    boolean  initialPathSet = false;
    X1 = DS.getAttributeValue( Attribute.INITIAL_PATH );
    if( X1 != null ) if( X1 instanceof Number ){
      initial_path = ( ( Number )X1 ).floatValue();
      initialPathSet = true  ;
    }

    for( int i = 0; i < DS.getNum_entries(); i++ ){
      Data DB = DS.getData_entry( i );

      if( run_numm != null )
        DB.setAttribute( new IntListAttribute( Attribute.RUN_NUM, run_numm ) );
      if( npulses >= 0 )
        DB.setAttribute(new IntAttribute(Attribute.NUMBER_OF_PULSES, npulses));
      if( initialPathSet)
        DB.setAttribute(new FloatAttribute(
                                         Attribute.INITIAL_PATH,initial_path));
    }
  
     return false;
    //Now Process Beam, etc.
  } 

 

  
  
 }


