/*
 * File:  NxWriter.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.6  2002/11/20 16:15:44  pfpeterson
 * reformating
 *
 * Revision 1.5  2002/04/01 20:56:56  rmikk
 * Fixed so it only writes one NXentry which now can contain several histograms
 *
 * Revision 1.4  2002/03/18 20:59:02  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 * Revision 1.3  2001/08/17 19:01:04  rmikk
 * Added error checking in case the file could not be created
 *
 * Revision 1.2  2001/07/30 20:11:42  rmikk
 * No longer implements Writer
 *
 * Revision 1.1  2001/07/25 21:23:20  rmikk
 * Initial checkin
 *
*/
package NexIO.Write;

import NexIO.Write.*;
import NexIO.Write.NexApi.*;
import NexIO.*;
import java.lang.*;
import DataSetTools.dataset.*;
import NexIO.NexApi.*;
import IsawGUI.*;
import IPNS.Runfile.*;
import DataSetTools.util.*;

/** Writes Nexus formatted files from DataSets
 */
public class NxWriter{
  String errormessage; 
  NxWriteNode node;
  NxNodeUtils nn;
  NxData_Gen nd;
  
  /** 
   * @param node  The Root node of the file to write
   */ 
  public NxWriter( NxWriteNode node){
    this.node = node;
    errormessage= "";
    if( node == null)
      errormessage = NxNodeUtils.ER_BADFILE;
    nn= new NxNodeUtils();
    nd = new NxData_Gen();    
    //get Global Attributes here and append to 
  } 
 
  /**
   * Returns an errormessage or "" if none
   */ 
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Gets the number of hisograms in this file so far Needed when
   * appending files
   */
  public int getNumHistograms(){
    int res = 0;
    return node.getNumClasses( "NXentry" );
  }


  /**
   * Appends the histograms with their monitors to a nexus file
   *
   * @param Monitors The set of monitor datasets associated with All
   * the Histograms
   * @param Histogram The set of histograms to be added to a Nexus
   * formatted file.
   *
   * NOTE: To Save an experiment, call the Append function twice
   */
  private void Append1( DataSet[] Monitors , DataSet[] Histogram){
    int n;
    String runTitle = null;
    Object run_title = null;
   
    //if( Monitors==null) System.out.print("null  : NHist=");
    // else System.out.print( Monitors.length+"  :NHist=");
    // if( Histogram == null) System.out.println("null  YYYYYYYYYYYYYYy");
    // else System.out.println( Histogram.length+"YYYYYYYYYYYY");
    /*  if( Histogram.length > 0)
        {run_title = Histogram[0].getAttributeValue(
        Attribute.RUN_TITLE );
        }
        else if( Monitors.length > 0 )
        run_title = Monitors[0].getAttributeValue(
        Attribute.RUN_TITLE );
        
        runTitle = nd.cnvertoString( run_title );
        
        if( runTitle != null )
        { byte b[];
        b = runTitle.getBytes();
        int rank[];
        rank = new int[1];
        rank[0] = b.length;
        node.addAttribute( "run_title" , b , Types.Char , rank );
        // System.out.println( "added run title =" + runTitle );
        }
    */
    int instrType = getInstrumentType( Monitors, Histogram);
    NxWriteMonitor nm = new NxWriteMonitor(instrType);
    NxWriteNode n1, n2;
    
    if( Histogram == null ) 
      n = 1;
    else 
      n = Histogram.length;
    n1 = n2 = null;
    NxWriteInstrument nw = new NxWriteInstrument( instrType);
    
    int kNxentries = getNumHistograms();
    NxWriteNode nxentry = node.newChildNode("Entry"+kNxentries, "NXentry");

    NxWriteNode nxInstr= nxentry.newChildNode("Instrument","NXinstrument");
    if( nw.processDS(nxInstr, Histogram[0])){
      errormessage +=";"+nw.getErrorMessage();
    }

    //There should only be one monitor
    if( Monitors !=null)
      if( Monitors.length > 0){
        if( Monitors.length >1)
          DataSetTools.util.SharedData.addmsg("Only one monitor is allowed "
                                   +"for a set of data sets in Writer.Append");
        for( int k = 0; k < Monitors[0].getNum_entries(); k++){
          String S =  (new Inst_Type()).getMonitorName( instrType, k);
          NxWriteNode nmonitor = nxentry.newChildNode(S, "NXmonitor");
          
          NxWriteMonitor nmon= new NxWriteMonitor(instrType) ; 

          if( nmon.processDS(nmonitor, Monitors[0], k)){
            errormessage += ";"+nmon.getErrorMessage();
          }
        }
      }
    for( int i = 0; i < n ; i++ ){
      String S;       
      if( Histogram != null ){
        n1 = nxentry.newChildNode( //"Histogram" + 
                                   // new Integer( i+kNxentries ).toString() ,
                                  Histogram[i].getTitle()+":"+i,"NXdata" );
        /*NxWriteNode nwNode = n1.newChildNode( "instrument" , 
          "NXinstrument" );
          if( nwNode == null )
          {errormessage = n1.getErrorMessage();	      
          return;
          }
          if( nw.processDS( nwNode , Histogram[i]   ) )
          { errormessage += ";" + nw.getErrorMessage();	     
          return;
          }
        */
        int kk =1;
        /* if( Monitors != null )
           if( i == 0 )
           for( int j = 0; j<Monitors.length; j++ )
           if(Monitors[j]!=null)
           for( int k = 0; k< Monitors[j].getNum_entries(); k++ )
           {n2 = n1.newChildNode( getMonitorName( instrType,kk,
           Monitors[j].getData_entry(k))
           ,"NXmonitor" );
                
           kk++;
           if( !nm.processDS( n2 , Monitors[j] , k ) )
           { n2.setLinkHandle( "MonLink" + ( j ) + "_" + k ); 
		
           }
           else errormessage += ";" + nm.getErrorMessage();
              
           }
           else//if not the first
           { for( j = 0; j<Monitors.length; j++ )
           if(Monitors[j] != null)
           for( int k = 0; k < Monitors[j].getNum_entries(); k++ )
           { n1.addLink( "MonLink" + ( j ) + "_" + k ); 
           // System.out.println( "in NxWriter-Wrote links" +  j  + "," );
           }
           }
        */
        NxWriteData nxd = new NxWriteData(instrType);
        // if( nxd.processDS( n1 ,nwInstr , Histogram[i] , true ) )  
        //     {errormessage += ";" +  nxd.getErrorMessage();
        //     }
           
      }else if ( Monitors != null ){
        n1 = node.newChildNode( "Histogram0", "NXentry" );
        NxWriteNode nwNode = node.newChildNode( "instrument", "NXinstrument" );
        if( nw.processDS( nwNode , Histogram[i]  ) )
          errormessage += ";" + nw.getErrorMessage();
        int kk = 1 ;
        for( int j = 0; j<Monitors.length; j++ )
          for( int k = 0; k< Monitors[j].getNum_entries(); k++ ){
            n2 = n1.newChildNode( "monitor" + kk,//( j + 1 ) + "_" + k ,
                                  "NXmonitor" );
            nm = new NxWriteMonitor(instrType);
            
            if( nm.processDS( n2 , Monitors[j] , k ) )
              errormessage += ";" + nm.getErrorMessage();
          }
      }

      NxWriteEntry ne = new NxWriteEntry(instrType);  
      if( ne.processDS( n1 , Histogram[i] ) )
        errormessage +=  ";" + ne.getErrorMessage();

      NxWriteSample ns = new NxWriteSample(instrType);
      if( ns.processDS( n1 , Histogram[i] ) )
        errormessage +=  ";" + ns.getErrorMessage();
      
      NxWriteBeam nb = new NxWriteBeam(instrType);
      if( nb.processDS( n1 , Histogram[i] ) )
        errormessage += ";" + nb.getErrorMessage();
      
    }//For each histogram
    //     (( NexWriteNode )node ).show();
    if(errormessage !="")
      DataSetTools.util.SharedData.status_pane.add(errormessage);
  }

  /**
   * Appends the histograms with their monitors to a nexus file
   *
   * @param Monitors The set of monitor datasets associated with All
   * the Histograms
   * @param Histogram The set of histograms to be added to a Nexus
   * formatted file.
   *
   * NOTE: To Save an experiment, call the Append function twice
   */
  public void Append( DataSet[] Monitors , DataSet[] Histogram){
    int n;
    String runTitle = null;
    Object run_title = null;
    System.out.println("In NxWriter.Append");
   
    int instrType = getInstrumentType( Monitors, Histogram);
    NxWriteMonitor nm = new NxWriteMonitor(instrType);
    NxWriteNode n1, n2;
    
    if( Histogram == null ) 
      n = 1;
    else 
      n = Histogram.length;
    n1 = n2 = null;
    NxWriteInstrument nw = new NxWriteInstrument( instrType);
    
    int kNxentries = getNumHistograms();
    NxWriteNode nxentry = node.newChildNode("Entry"+kNxentries, "NXentry");

    NxWriteNode nxInstr= nxentry.newChildNode("Instrument","NXinstrument");
    if( nw.processDS(nxInstr, Histogram[0])){
      errormessage +=";"+nw.getErrorMessage();
    }
    
    //There should only be one monitor
    if( Monitors !=null)
      if( Monitors.length > 0){
        if( Monitors.length >1)
          DataSetTools.util.SharedData.status_pane.add("Only one monitor is "
                           +"allowed for a set of data sets in Writer.Append");
        for( int k = 0; k < Monitors[0].getNum_entries(); k++){
          NxWriteNode nmonitor =
            nxentry.newChildNode((new NexIO.Inst_Type()).getMonitorName( 
                                                   instrType, k), "NXmonitor");
          
          NxWriteMonitor nmon= new NxWriteMonitor( instrType) ; 

          if( nmon.processDS(nmonitor, Monitors[0], k)){
            errormessage += ";"+nmon.getErrorMessage();
          }
        }
      }
    for( int i = 0; i < n ; i++ ){
      String S;       
      if( Histogram != null ){
        //n1 = nxentry.newChildNode( 
        //                     Histogram[i].getTitle()+":"+i;
        //                    "NXdata" );
           
        NxWriteData nxd = new NxWriteData(instrType);
        if( nxd.processDS( nxentry ,nxInstr , Histogram[i] , true ) ){
          errormessage += ";" +  nxd.getErrorMessage();
        }
      }
    }//For each histogram

    NxWriteEntry ne = new NxWriteEntry(instrType);  
    if( ne.processDS( nxentry , Histogram[0] ) )
      errormessage +=  ";" + ne.getErrorMessage();
    
    NxWriteSample ns = new NxWriteSample(instrType);
    if( ns.processDS( nxentry , Histogram[0] ) )
      errormessage +=  ";" + ns.getErrorMessage();

    NxWriteBeam nb = new NxWriteBeam(instrType);
    if( nb.processDS( nxentry , Histogram[0] ) )
      errormessage += ";" + nb.getErrorMessage();
  }

  /**
   * Append the Monitor/DataSet pair to the Nexus formatted file
   *
   * NOTE: one or both arguments can be null
   */
  public void Append( DataSet Monitor , DataSet Histogram ){
    DataSet DS[], Mon[];
    if( Monitor == null)
      Mon = null;
    else{
      Mon = new DataSet[1];
      Mon[0] = Monitor;
    }

    if( Histogram == null)
      DS = null;
    else{
      DS = new DataSet[1];
      DS[0] = Histogram;
    }
    Append( Mon, DS );
    
  }
  
  /**
   * Closes the file: The file may not be written if this is not
   * called
   */ 
  public void close(){
    node.write();
    errormessage = node.getErrorMessage();
    node.close();
  }

  //check for upstream and downstream angle
  private String getMonitorName( int instrType, int MonitorNum, Data DB){
    if( instrType == InstrumentType.TOF_DIFFRACTOMETER)
      if( MonitorNum ==1)
        return "upstream";
      else if( MonitorNum == 2)
        return "downstream";
    /* else
       {SharedData.status_pane.add("TOF DIFFRACTOMETERS only have 2 monitors");
       return "ERROR";
       }
    */
    return "monitor"+MonitorNum;
  }

  private int getInstrumentType( DataSet[] Monitors, DataSet[] Histograms){
    int type = InstrumentType.UNKNOWN;
   
    if( Monitors != null)
      if( Monitors.length >0)
        for(int i=0;(i<Monitors.length)&&(type ==InstrumentType.UNKNOWN );i++)
          if(Monitors[i] != null){
            Attribute A =  Monitors[i].getAttribute( Attribute.INST_TYPE);
            if( A != null) 
              if( A instanceof IntAttribute)
                type = ((Integer)(A.getValue())).intValue();
              else if( A instanceof IntListAttribute)
                if(((IntListAttribute) A).getIntegerValue() != null)
                  if( ((IntListAttribute) A).getIntegerValue().length ==1)
                    type = ((IntListAttribute) A).getIntegerValue()[0];
          }
    if( type != InstrumentType.UNKNOWN)
      return type;
    if( Histograms != null)
      if( Histograms.length > 0)
        for(int i = 0; (i < Histograms.length)&&(type == -2);i++){
          Attribute A =  Histograms[i].getAttribute( Attribute.INST_TYPE);
          if( A != null) 
            if( A instanceof IntAttribute)
              type = ((Integer)(A.getValue())).intValue();
            else if( A instanceof IntListAttribute)
              if(((IntListAttribute) A).getIntegerValue() != null)
                if( ((IntListAttribute) A).getIntegerValue().length ==1)
                  type = ((IntListAttribute)A).getIntegerValue()[0];
        }
    if( type != InstrumentType.UNKNOWN)
      return type;
    return IPNS.Runfile.InstrumentType.UNKNOWN;
  }

  /**
   * Test program for this NxWriter module
   *
   * @param  args[0]  The filename sans extension
   *
   * @result a new file with an extension .nxs will be created from
   * filename.run
   */
  public static void main( String args[] ){
    DataSet DSS[], DSH[], DSM[];
    Util UT = new Util();
    int i;
    String filename = "C:\\SampleRuns\\gppd9898"; 
    int n;
    if( args == null)
      n = 1;
    else if( args.length <= 1)
      n = 1;
    else
      n= args.length;
    if(args != null) if( args.length > 0)
      filename = args[0];
  
    //NexWriteNode nwr = new NexWriteNode( filename  + ".nxs" );
    XmlWriteNode nwr = new XmlWriteNode( filename + ".xml" );
    if( nwr.getErrorMessage() != "")
      System.out.println( "Error 1="+nwr.getErrorMessage());
    //  if( nwr instanceof NexIO.Write.NxWriteNode)
    //     System.out.print("is instance of");
    //  System.out.println(nwr.getClass() +":"+nwr.getClass().getSuperclass()+":");
    //  System.out.print("interfaces:");
    //  Class x[];
    //   x= nwr.getClass().getInterfaces();
    //  if( x!= null)
    // for( i=0; i<  x.length; i++)
    //   System.out.print( x[i]+":: ");
    // System.out.println("");
    NexIO.Write.NxWriteNode nwrx =  (NexIO.Write.NxWriteNode)nwr;
    NxWriter Writer = new NxWriter( nwrx  );
    //Has one monitor and one histogram
    for(  i = 0; i < n ; i++ ){
      DSS = UT.loadRunfile( filename + ".run" );
      if( DSS != null){
        DSM = new DataSet[1];
        DSH = new DataSet[ DSS.length - 1 ];
        DSM[0] = DSS[0];
        for( int k = 1; k < DSS.length; k++ )
          DSH[k-1] = DSS[k];
        Writer.Append( DSM , DSH );
      }
      System.out.println( " Error =" + Writer.getErrorMessage() ) ; 
      if( i+1 < n)
        filename = args [ i+1 ];
    }
    nwrx.show();
    Writer.close();
  } 
}
