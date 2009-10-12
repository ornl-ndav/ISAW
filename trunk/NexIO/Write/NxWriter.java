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
 * Revision 1.14  2007/07/04 17:56:31  rmikk
 * Code was adjusted to write NeXus file for DataSets consistiing of only a Monitor data sel.
 *
 * Revision 1.13  2006/10/10 15:29:43  rmikk
 * Fixed a null pointer exception when the monitor is null
 *
 * Revision 1.12  2004/05/14 15:03:51  rmikk
 * Removed unused variables
 *
 * Revision 1.11  2004/02/16 02:19:23  bouzekc
 * Removed unused imports.
 *
 * Revision 1.10  2003/11/24 14:14:25  rmikk
 * Deleted segments of commented out code
 * Eliminated debugging prints
 * Moved the creation of the class NXbeam to NXinstrument
 *
 * Revision 1.9  2003/10/15 02:52:57  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.8  2003/03/05 20:44:19  pfpeterson
 * Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 * Revision 1.7  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
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
*/
package NexIO.Write;

import NexIO.Write.NexApi.*;
import NexIO.*;
import DataSetTools.dataset.*;
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
   * Gets the number of histograms in this file so far Needed when
   * appending files
   */
  public int getNumHistograms(){
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
   * NOT USED
   */
  /*
  private void Append1( DataSet[] Monitors , DataSet[] Histogram){
    int n;  
    
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
    DataSet DS = null;
    if( Histogram != null && Histogram.length > 0)
       DS = Histogram[0];
    else if( Monitors != null && Monitors.length > 0)
       DS = Monitors[0];
    if( nw.processDS(nxInstr, DS)){
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
      if( Histogram != null ){
        n1 = nxentry.newChildNode( //"Histogram" + 
                                   // new Integer( i+kNxentries ).toString() ,
                                  Histogram[i].getTitle()+":"+i,"NXdata" );
        
        
           
      }else if ( Monitors != null ){
        n1 = node.newChildNode( "Histogram0", "NXentry" );
        //NxWriteNode nwNode = node.newChildNode( "instrument", "NXinstrument" );
        //if( nw.processDS( nwNode , Histogram[i]  ) )
        //  errormessage += ";" + nw.getErrorMessage();
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
      NxWriteNode n1Sampe = n1.newChildNode("Sample","NXsample");
      if( ns.processDS( n1Sampe , Histogram[i] ) )
        errormessage +=  ";" + ns.getErrorMessage();
      
     
    }//For each histogram
    //     (( NexWriteNode )node ).show();
    if(errormessage !="")
      SharedData.addmsg(errormessage);
  }
*/
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
    int instrType = getInstrumentType( Monitors, Histogram);
    
    if( Histogram == null ) 
      n = 1;
    else 
      n = Histogram.length;
    NxWriteInstrument nw = new NxWriteInstrument( instrType);
    
    int kNxentries = getNumHistograms();
    NxWriteNode nxentry = node.newChildNode("Entry"+kNxentries, "NXentry");

    NxWriteNode nxInstr= null; 
   
    nxInstr = nxentry.newChildNode("Instrument","NXinstrument");

    DataSet DS = null;
    if( Histogram != null && Histogram.length > 0)
       DS = Histogram[0];
    else if( Monitors != null && Monitors.length > 0)
       DS = Monitors[0];
    
    if( nw.processDS(nxInstr,DS)){
      errormessage +=";"+nw.getErrorMessage();
    }
    
    //There should only be one monitor
    if( Monitors !=null)
      if( (Monitors.length > 0) &&(Monitors[0] != null)){
        if( Monitors.length >1)
          SharedData.addmsg("Only one monitor is "
                           +"allowed for a set of data sets in Writer.Append");
        for( int k = 0; k < Monitors[0].getNum_entries(); k++){
          NxWriteNode nmonitor =
            nxentry.newChildNode((new NexIO.Inst_Type()).getMonitorName( 
                                                   instrType, k), "NXmonitor");
          
          NxWriteMonitor nmon= new NxWriteMonitor( instrType) ; 

          if( nmon.processDS(nmonitor, Monitors[0], k)){
            errormessage += ";"+nmon.getErrorMessage();
           
          }
          Write( nmonitor);
        }
      }
    for( int i = 0; i < n ; i++ ){     
      if( Histogram != null ){
        
           
        NxWriteData nxd = new NxWriteData(instrType);
        nxd.write = true;
        if( nxd.processDS( nxentry ,nxInstr , Histogram[i] , true ) ){
          errormessage += ";" +  nxd.getErrorMessage();
        }
       
      }
    }//For each histogram
    
    NxWriteEntry ne = null;
        ne = new NxWriteEntry(instrType); 
    
    if( ne.processDS( nxentry ,DS ))
      errormessage +=  ";" + ne.getErrorMessage();
    
    NxWriteSample ns = new NxWriteSample(instrType);
    if( ns.processDS( nxentry ,DS ) )
      errormessage +=  ";" + ns.getErrorMessage();
   
    Write( nxentry);
  }
  
  private void Write(NxWriteNode Node)
   {

      Node.write();
      if( Node.getErrorMessage() != null && Node.getErrorMessage().length() > 0 )
         errormessage += ";"+Node.getErrorMessage();
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
/*
  private String getMonitorName( int instrType, int MonitorNum, Data DB){
    if( instrType == InstrumentType.TOF_DIFFRACTOMETER)
      if( MonitorNum ==1)
        return "upstream";
      else if( MonitorNum == 2)
        return "downstream";
   
    return "monitor"+MonitorNum;
  }
*/
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
   * A new file with an extension .nxs will be created from
   * filename.run
   *
   * @param  args  The filename sans extension
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
    NexIO.Write.NxWriteNode nwrx =  nwr;
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
      if( i+1 < n &&  args != null)
        filename = args [ i+1 ];
    }
    nwrx.show();
    Writer.close();
  } 
}
