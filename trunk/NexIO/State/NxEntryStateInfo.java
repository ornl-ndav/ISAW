

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
 * Revision 1.6  2006/11/14 16:39:42  rmikk
 * Added fields for InstrumentNode and facility to this state
 *
 * Revision 1.5  2006/07/27 18:32:49  rmikk
 * Added extra spacing to make the code more readable
 *
 * Revision 1.4  2006/07/25 00:05:57  rmikk
 * Added code to update fields in a FixIt xml file in the same directory as
 * the NeXus file
 *
 * Revision 1.3  2004/12/23 13:06:41  rmikk
 * Updated to version 1.0.  Added a version field
 *
 * Revision 1.2  2003/12/12 17:27:48  rmikk
 * If an NXentry has no "description" SDS field, the "analysis"(old) field is also
 *   checked
 *
 * Revision 1.1  2003/11/16 21:43:46  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;

//NexIO.State.NxEntryStateInfo
import NexIO.*;
import NexIO.Util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import DataSetTools.dataset.*;


/**
 *    This class contains state information needed to process an NXentry class
 *   of a NeXus file
 */ 
public class NxEntryStateInfo extends StateInfo {

    /**
     *   The analysis type like TOFNPD, etc
     */
    public String description;
  
    public String version;
    
    NxNode InstrumentNode;

    /**
     *   The Name of the NXentry node
     */
    public String Name;
    public String facility;
  
    /**
     *   Constructor
     *   @param NxEntryNode  an NxNode containing information on a NeXus NXentry
     *                       class
     *   @param Params    a linked list of State information
     */
    public NxEntryStateInfo(NxNode NxEntryNode, 
        NxfileStateInfo Params) {
          
        Name = NxEntryNode.getNodeName();
        version = null;
     
        description = NexUtils.getStringFieldValue(NxEntryNode, "description");
        
        if (description == null )
            description = NexUtils.getStringFieldValue(NxEntryNode, "analysis");
        
        if (description == null )
           description = NexUtils.getStringFieldValue(NxEntryNode, "definition");
       
        else
            version = NexUtils.getStringAttributeValue(           
                        NxEntryNode.getChildNode("description"), "version");
        
       if( Params.xmlDoc != null ){
          Node N = Util.getNXInfo( Params.xmlDoc , "NXentry.definition" ,
                       Name + ".version" ,  null ,  null) ;
          if( N != null )
             version = N.getNodeValue();
       }
       InstrumentNode = null;
       for( int i =0; (i< NxEntryNode.getNChildNodes()) && (InstrumentNode == null) ; i++)
          if( NxEntryNode.getChildNode( i).getNodeClass().equals("NXinstrument"))
             InstrumentNode = NxEntryNode.getChildNode( i );
       
       facility = getFacility( InstrumentNode);
     
    }
    
    public String getFacility( NxNode InstrNode){
       NxNode NODE = null;
       if( InstrNode == null)
          return null;
       for( int i=0;( i< InstrNode.getNChildNodes()) &&(NODE == null); i++){
          NxNode node= InstrNode.getChildNode(i);
          if( node.getNodeClass().equals("NXsource")){
             NODE=node;
             for( int k=0; k< NxfileStateInfo.Names.length; k++)
                for( int m=0; m< NxfileStateInfo.Names[k].length; m++){
                   int kk = node.getNodeName().indexOf( NxfileStateInfo.Names[k][m]);
                   if( kk==0)
                      return NxfileStateInfo.Names[k][0];
                   if( kk > 0)if( kk +NxfileStateInfo.Names[k][m].length()+1 < node.getNodeName().length())
                      if( " ;,.[+-(])&%$\t\n".indexOf( node.getNodeName().charAt( kk + 
                               NxfileStateInfo.Names[k][m].length()+1))>=0)
                         return NxfileStateInfo.Names[k][0];
                 }
            
           }
       
            
       }
          
       if( NODE != null)
          for( int i=0; i < NODE.getNChildNodes(); i++)
             if( NODE.getChildNode(i).getNodeName().equals("name")){
                String nm = NODE.getChildNode(i).getNodeName();
                String nm1 = ConvertDataTypes.StringValue( NODE.getChildNode(i).getNodeValue());
                for( int k=0; k< NxfileStateInfo.Names.length; k++)
                   for( int m=0; m< NxfileStateInfo.Names[k].length; m++){
                      int kk = nm.indexOf( NxfileStateInfo.Names[k][m]);
                      if( kk==0)
                         return NxfileStateInfo.Names[k][0];
                      if( kk > 0)if( kk +NxfileStateInfo.Names[k][m].length()+1 < nm.length())
                         if( " ;,.[+-(])&%$\t\n".indexOf(nm.charAt( kk + NxfileStateInfo.Names[k][m].length()+1))>=0)
                            return NxfileStateInfo.Names[k][0];

                      kk = nm1.indexOf( NxfileStateInfo.Names[k][m]);
                      if( kk==0)
                         return NxfileStateInfo.Names[k][0];
                      if( kk > 0)if( kk +NxfileStateInfo.Names[k][m].length()+1 < nm1.length())
                         if( " ;,.[+-(])&%$\t\n".indexOf(nm1.charAt( kk + NxfileStateInfo.Names[k][m].length()+1))>=0)
                            return NxfileStateInfo.Names[k][0];
                    }  
                return null;
              } 
       return  null;

    }
    
    
    public static void Compare( DataSet DS ){
       int C1 =0;
       int C2 = 0;
       IDataGrid grid1 = NexIO.Write.NxWriteData.getDataGrid( DS.getData_entry(0));
       IDataGrid grid2;
       XScale xscl1,xscl2;
       xscl1 = grid1.getData_entry(1,1).getX_scale();
       for( int i = 0; i +1< DS.getNum_entries(); i++){
           grid2 =  NexIO.Write.NxWriteData.getDataGrid( DS.getData_entry(i+1));
           xscl2 = grid2.getData_entry(1,1).getX_scale();
           
           if( xscl1 == xscl2 ) C1++; else C2++;
           grid1=grid2;
           xscl1=xscl2;
       }
      System.out.println("C1 true, C2=false"+ C1+":"+C2);
    }
    public static void main( String[] args){
       int C1=0, C2=0, D=0;
       try{
          DataSet[] DSS = Command.ScriptUtil.load( args[0]);
          DataSet DS = DSS[DSS.length -1];
          System.out.println("---------------Compare----------");
          NxEntryStateInfo.Compare(DS);
          System.out.println("--------------------------");
          
          int[] grids = NexIO.Write.NxWriteData.getAreaGrids( DS );
          
          IDataGrid grid1,grid2;
          grid1 = NexIO.Write.NxWriteData.getAreaGrid( DS, grids[0]);
          System.out.println(" grid1 is set"+ grid1.isData_entered() );
          grid1.setData_entries( DS );
          XScale xscl1,xscl2;
          xscl1 = grid1.getData_entry(1,1).getX_scale();
          int indx1,indx2;
          indx1= DS.getIndex_of_data( grid1.getData_entry(1,1));
          Data db1,db2;
          db1= grid1.getData_entry(1,1);
          for( int i = 0; i +1< grids.length; i++){
              grid2 =  NexIO.Write.NxWriteData.getAreaGrid( DS, grids[i+1]);
              grid2.setData_entries( DS );
              indx2 = DS.getIndex_of_data( grid2.getData_entry(1,1));
              xscl2 = grid2.getData_entry(1,1).getX_scale();

              db2= grid2.getData_entry(1,1);
              if( xscl1==xscl2) C1++; else C2++;
              
              if( (DS.getData_entry(indx2)!=db2)){
                 System.out.println("Not equal "+ i);
              }else{
                 XScale xxscl1 = DS.getData_entry(indx2).getX_scale();
                 if( xxscl1 != xscl2) D++;
              }
              grid1=grid2;
              xscl1=xscl2;
              indx1=indx2;
          }
          System.out.println("XXX"+C1+"::"+C2+"::"+D);
       }catch( Exception s){
          System.out.println("XXX"+C1+"::"+C2+"::"+D);
          s.printStackTrace();
          System.exit( 0 );
       }
    }
}

