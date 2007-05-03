

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
 * Revision 1.9  2007/01/19 19:17:25  rmikk
 * Fixed an error in setting data entries.  Used incorrect grid.
 *
 * Revision 1.8  2007/01/12 14:48:46  dennis
 * Removed unused imports.
 *
 * Revision 1.7  2006/11/14 16:50:23  rmikk
 * Incorporated using the xml Fixit file
 * Grouped detectors for LANSCE SCD data
 *
 * Revision 1.6  2004/12/23 13:20:48  rmikk
 * Fixed indentations and spacings between lines
 *
 * Revision 1.5  2003/12/12 15:24:19  rmikk
 * The names of monitor data sets now start with Mon_ instead of M:
 *
 * Revision 1.4  2003/12/08 23:05:36  rmikk
 * Eliminate call to addOperator
 *
 * Revision 1.3  2003/12/08 20:54:31  rmikk
 * Changed saving the NeXus NXentry.title field to the Attribute.RUN_TITLE  instead
 *  of to Attribute.TITLE.
 *
 * Revision 1.2  2003/11/23 23:45:01  rmikk
 * Now implements the interface IProcessNxEntry
 *
 * Revision 1.1  2003/11/16 21:40:57  rmikk
 * Initial Checkin
 *
 */

package NexIO.Process;


//import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import NexIO.Util.*;
import NexIO.Query.*;
//import javax.xml.parsers.*;
import org.w3c.dom.*;


/**
 *   This class processes an NXentry.  There is only one of these. It uses
 *   Queries to get the correct IProcessNxData. It also calls the processors
 *   for the NXbeam, NXsample, etc. to fill out attributes and fields of the
 *   Data Set.
 */

public class ProcessNxEntry  implements IProcessNxEntry {
    String errormessage = "";

    /**
     *  @return an errormessage or an empty string if there is no error
     */
    public String getErrorMessage() {
        return errormessage;
    }

    private boolean setErrorMessage(String message) {
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
    public boolean processDS(DataSet DS, NxNode NxEntryNode, 
        NxNode NxDataNode, NxfileStateInfo States, int startGroupID) {
        if (NxEntryNode == null) {
            errormessage = "null Entry node in ProcessNxEntry";
            return true;
        }
        if (DS == null) {
            errormessage = "No Data Set in ProcessNxEntry";
            return true;
        }
        
        boolean monitorDS = false;

        if (NxDataNode == null) { // Monitor DataSet
       
            int k = 0;

            monitorDS = true;
            DS.setTitle("Mon_" + NxEntryNode.getNodeName());
        
            Integer instType = ((Integer) DS.getAttributeValue(Attribute.INST_TYPE));

            if (instType != null)
                DataSetFactory.addMonitorOperators(DS, instType.intValue());
               
            for (int child = 0; child < NxEntryNode.getNChildNodes(); child++) {
          
                NxNode Child = NxEntryNode.getChildNode(child);

                if (Child.getNodeClass().equals("NXmonitor")) {
             
                    NxMonitor nxMon = new NxMonitor();

                    nxMon.setMonitorNum(startGroupID + k);
                    k++;
              
                    boolean res = nxMon.processDS(Child, DS);

                    if (res) {
                        errormessage = nxMon.getErrorMessage();
                        return res;
                    }
                }
            }
        } else {  //Histogram Data Set  check if node class is NXlog
        
            DS.setTitle(NxDataNode.getNodeName());
      
            NxNode NxInstrumentNode = null;
            NxNode NxBeamNode = null;

            for (int child = 0; (child < NxEntryNode.getNChildNodes()); child++) {
                NxNode Child = NxEntryNode.getChildNode(child);

                if (Child.getNodeClass().equals("NXinstrument"))
                    NxInstrumentNode = Child;
                else if (Child.getNodeClass().equals("NXbeam"))
                    NxBeamNode = Child;
            }

            IProcessNxData proc = QueryNxData.getNxDataProcessor(States, 
                    NxDataNode, NxInstrumentNode);
            boolean res = proc.processDS(NxEntryNode, NxDataNode, NxInstrumentNode, 
                    DS, States, startGroupID);

            if (res) {
                errormessage = proc.getErrorMessage();
                return res;
            } 
       
        }
        //--------------- Run Number DataSet Attribute ---------------
        Integer run_num = NexUtils.getIntFieldValue(NxEntryNode, "run_number");

        if (run_num != null) {
            int[] runs = new int[1];

            runs[0] = run_num.intValue();
            DS.setAttribute(new IntListAttribute(Attribute.RUN_NUM, runs));
        }

        //------------------ Title DataSet Attribute?? -----------------
        ConvertDataTypes.addAttribute(DS,
            ConvertDataTypes.CreateStringAttribute(Attribute.RUN_TITLE,
                NexUtils.getStringFieldValue(NxEntryNode, "title")));
   
        //---------------- Number of pulses DataSet attribute ------------------
        Float X = NexUtils.getFloatFieldValue(NxEntryNode, "duration");

        if (X != null)
            if (!X.isNaN())
                DS.setAttribute(new FloatAttribute(Attribute.NUMBER_OF_PULSES,
                        X.floatValue() * 30.f));

            //--------------Attributes from other NXentry children -----------
        boolean checkedSample = false,
                checkedBeam =false,
                checkedInstr = false;
        for (int i = 0; i < NxEntryNode.getNChildNodes(); i++) {
        
            NxNode datanode = NxEntryNode.getChildNode(i);
            String C = datanode.getNodeClass();
      
            if (C.equals("NXinstrument")) {
                NxInstrument nx = new NxInstrument();
        
                if (!monitorDS)
                    if (!nx.processDS(datanode, DS, States)) {// do nothing
                       checkedInstr = true;
                    } else
                        errormessage += ":" + nx.getErrorMessage();
            } else  if (C.equals("NXsample")) {
                NxSample ns = new NxSample();
        
                if (!ns.processDS(datanode, DS, States)) {// do nothing
                   checkedSample = true;
                } else
                    errormessage += ";" + ns.getErrorMessage();
            } else if (C.equals("NXbeam")) {
                NxBeam nb = new NxBeam();
        
                if (!monitorDS)
                    if (nb.processDS(datanode, DS, States))
                        errormessage += ";" + nb.getErrorMessage();
                    else
                       checkedBeam = true;
            }

        }
        // ------- In case the corresponding parts are not in the NeXus file -----
        if(!checkedSample){
           NxSample ns = new NxSample();
           if( ns.processDS( null, DS, States))
              errormessage +=";"+ns.getErrorMessage();
        }
        if(!checkedInstr){
           NxInstrument nx = new NxInstrument();
           if( nx.processDS( null, DS, States))
              errormessage +=";"+nx.getErrorMessage();
        }
        if(!checkedBeam){
           NxBeam nb = new NxBeam();
           if( nb.processDS( null, DS, States))
              errormessage +=";"+nb .getErrorMessage();
        }
        //----------------------------
       
        
        Object X1 = DS.getAttributeValue(Attribute.RUN_NUM);
        int[] run_numm = null;
    
        if (X1 != null)
            if (X1 instanceof int[])
                run_numm = (int[]) X1;
        int npulses = -1;

        X1 = DS.getAttributeValue(Attribute.NUMBER_OF_PULSES);
        if (X1 instanceof Number)
            npulses = ((Number) X1).intValue();
    
        float initial_path = -1;
        boolean  initialPathSet = false;

        X1 = DS.getAttributeValue(Attribute.INITIAL_PATH);
        if (X1 != null) if (X1 instanceof Number) {
                initial_path = ((Number) X1).floatValue();
                initialPathSet = true;
            }
       if( States != null){
         if( States.xmlDoc != null){
            Node NN = NexIO.Util.Util.getNXInfo( States.xmlDoc , "NXentry.NXsource.distance",
                         null , null , null );
            float initPath= Float.NaN;
            if( NN != null )
               initPath = ConvertDataTypes.floatValue( NexIO.Util.Util.getLeafNodeValues(NN));
             if( !Float.isNaN( initPath)){
                initialPathSet = true;
                initial_path = initPath;
                DS.setAttribute( new FloatAttribute( Attribute.INITIAL_PATH, initial_path));
             }
            
         }
       }
        for (int i = 0; i < DS.getNum_entries(); i++) {
      
            Data DB = DS.getData_entry(i);

            if (run_numm != null)
                DB.setAttribute(new IntListAttribute(Attribute.RUN_NUM, 
                                                              run_numm));
        
            if (npulses >= 0)
                DB.setAttribute(new IntAttribute(Attribute.NUMBER_OF_PULSES, 
                                                                     npulses));
        
            if (initialPathSet)
                DB.setAttribute(new FloatAttribute(
                        Attribute.INITIAL_PATH, initial_path));
        }
  
        //Now fixup LANSCE SCD data
        String facility = States.facility;
        NxEntryStateInfo NxEntryState = NexUtils.getEntryStateInfo( States);
        if( facility == null)if( NxEntryState != null)
           facility = NxEntryState.facility;
        
        if( States != null)if( facility != null)if( facility.equals("LANL"))
           if( States.InstrumentName != null)if( States.InstrumentName.equals("SCD"))
              FixUpLANL_SCD( DS );
        
        if (errormessage != null)
           if (errormessage.length() > 1)
               return true;
        return false;
        //Now Process Beam, etc.
    } 
    
    private void FixUpLANL_SCD( DataSet DS ){
       int[] IDs = Grid_util.getAreaGridIDs( DS);
       if( IDs == null)
          return;
       for( int i=0; i < IDs.length; i++){
          IDataGrid grid = Grid_util.getAreaGrid( DS, IDs[i]);
          if( !grid.isData_entered())
             grid.setData_entries( DS );
          UniformGrid grid1;
          if( 2*(grid.num_cols()/2)!= grid.num_cols())
             return;
          if( 2*(grid.num_rows()/2)!= grid.num_rows())
             return;
          grid1= new UniformGrid(grid.ID(), grid.units(),grid.position(),
                    grid.x_vec(),grid.y_vec(),grid.width(),grid.height(),
                    grid.depth(),grid.num_rows()/2,grid.num_cols()/2);
          for( int row =1; row<= grid1.num_rows();row++ )
             for( int col=1; col<= grid1.num_cols();col++){
                int r = 1+(row-1)*2;
                int c = 1+(col-1)*2;
                Data DB = grid.getData_entry( r,c);
                float[] yvalues = DB.getY_values();
                Data DB1=grid.getData_entry(r, c+1);
                Data DB2=grid.getData_entry(r+1, c+1);
                Data DB3=grid.getData_entry(r+1, c);
                for( int t=0; t< yvalues.length; t++){
                   yvalues[t] += DB1.getY_values()[t];
                   yvalues[t] += DB2.getY_values()[t];
                   yvalues[t] += DB3.getY_values()[t];
                }
                DS.removeData_entry_with_id( DB1.getGroup_ID());
                DS.removeData_entry_with_id( DB2.getGroup_ID());
                DS.removeData_entry_with_id( DB3.getGroup_ID());
                
                DB.setAttribute( new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST,
                         new PixelInfoList( new DetectorPixelInfo( grid.ID(),
                         (short) row,(short)col, grid1))));
             }
          grid1.setData_entries( DS );
          
       }//for IDs
       
    }

    /**
     *   Hook to add new fields to the class that are not in the processDS 
     *   methods parameters
     */
    public void setNewInfo(String Name, Object value) {}

    /**
     *   @return the current value of the new info associated with Name
     */
    public Object getNewInfo(String Name) {
        return null;
    }
  
}
