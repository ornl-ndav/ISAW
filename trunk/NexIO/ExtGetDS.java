/*
 * File:  ExtGetDS.java 
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
 * Revision 1.16  2003/12/08 17:29:13  rmikk
 * Added the DataSet operations to the data set
 *
 * Revision 1.15  2003/11/23 23:53:19  rmikk
 * If a NxData does not have a link attribute one one of its fields
 *   the old system is used
 *
 * Revision 1.14  2003/11/16 21:52:23  rmikk
 * -The default GroupIDs are now different for every Spectra in a NeXus file
 * -Incorporated the new methods that follow the State model
 *
 * Revision 1.13  2003/10/15 03:05:49  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.12  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.11  2002/11/20 16:14:36  pfpeterson
 * reformating
 *
 * Revision 1.10  2002/10/02 19:56:54  dennis
 * The instrument type attribute is now set to an array with one element.
 * This maintains compatibility with the retrievers and allows
 * NeXus files to be read properly from the RemoteFileRetriever, using
 * the readObject() method in DataSet.java
 *
 * Revision 1.9  2002/07/29 18:48:31  rmikk
 * Added code to determine if the Data Set Type is pulse height
 *
 * Revision 1.8  2002/04/01 20:05:56  rmikk
 * Each NXdata without a label attribute on its data field gives rise to a new data set.  The NXdata in a NXentry with the same label values are merged
 *
 * Revision 1.7  2002/02/26 15:27:10  rmikk
 * Added a debug field.
 * Added code for the TOFNDGS instrument definition
 *
 */
package NexIO ;

//import NdsSvNode ;
import java.io.* ;
import NexIO.NexApi.* ;
//import NexIO.NDS.* ;
import NexIO.* ;
import NexIO.NexApi.* ;
import DataSetTools.dataset.* ;
import DataSetTools.retriever.* ;
import DataSetTools.viewer.* ;
import DataSetTools.math.*;
import IPNS.Runfile.* ;
import java.util.* ;
import java.lang.reflect.*;
import NexIO.State.*;
import NexIO.Process.*;
import NexIO.Util.*;
import NexIO.Query.*;
import DataSetTools.util.*;

/**  Class that retrieves DataSets from sources with an NxNode interface AND
 *  which
 * <ol>
 *<li> Satisfy the NxNode interface
 *<li> The node class structure and return types satisfy the Nexus Standard
 *</ol> 
 * This class has been used with a Nexus API module( NexIO.NexApi ) and
 * a nexus dataserver module( NexIO.NDS ). Future plans are to extend this to
 * retrieve XML documents
 */
public class ExtGetDS{ 
  NxNode node ;//Top Node
  String filename ;
  String errormessage ;
  Vector EntryToDSs;
  boolean setupDSs;
  boolean debug=false;

  /**
   *  Constructor 
   *  @param node the NxNode used to communicate with the underlying
   *                 datasource
   *  @param filename the filename
   */
  public ExtGetDS( NxNode node , String filename ){
    this.node = node ;
    this.filename = filename ;
    errormessage = "" ;
    EntryToDSs= new Vector() ;
  }

  /**
   * Returns the type of the DataSet. 
   * @param data_set_num  the data set whose type is desired
   *
   * @see DataSetTools.retriever.dataSource
   */
  public int getType(  int data_set_num ){
    if(!setupDSs ) 
      setUpDataSetList() ;
    
    if( data_set_num < 0)
      return Retriever.INVALID_DATA_SET ;
    if( data_set_num >= EntryToDSs.size())
      return Retriever.INVALID_DATA_SET ;

    DataSetInfo VV =(DataSetInfo)(EntryToDSs.elementAt(data_set_num));
    NxNode nd= (VV.NxdataNode);
    if(nd == null)
      return Retriever.MONITOR_DATA_SET ;
    else if( nd.getNodeName().toUpperCase().indexOf("Pulse")>=0)
      return Retriever.PULSE_HEIGHT_DATA_SET;
    else
      return Retriever.HISTOGRAM_DATA_SET ;
  }

  /**
   *    Sets the id's of a data set that are to be retrieved
   *    @param  ids   the list of id's to be retrieved. If null all are to
   *                  be retrieved
   */
  public void setIDs( int[] ids){
  }


  /**
   *   Returns a string describing the given data set
   *   @param  data_set_num  the index of the data set to be retrieved
   *   @return a String with the name and type of data set this is
   */
  public String getDataSetInfo( int data_set_num){
    if( !setupDSs ) 
      setUpDataSetList() ;
    if( data_set_num < 0)
        return null;
    if( data_set_num >= EntryToDSs.size())
       return null;
    DataSetInfo dsInf = ((DataSetInfo)( EntryToDSs.elementAt( data_set_num)));
    String S;
    if( dsInf.NxdataNode == null)
       S = "(NxMonitor)";
    else
       S = "("+dsInf.NxdataNode.getNodeClass()+")";
    S += dsInf.NxentryNode.getNodeName();
    if( dsInf.NxdataNode != null)
       S += "."+ dsInf.NxdataNode.getNodeName();
    //System.out.println("node "+ data_set_num+"has groupids from"+dsInf.startGroupID+
     //         " to "+ dsInf.endGroupID);
    return S;
  }


 public DataSet getDataSet( int data_set_num ){
   if( !setupDSs ) 
     setUpDataSetList() ;
   if( (data_set_num <0) ||( data_set_num >= EntryToDSs.size())){
     DataSetTools.util.SharedData.addmsg("invalid data set number "
                                         +data_set_num);
     return  null;

   }
   DataSetInfo dsInf = (DataSetInfo)(EntryToDSs.elementAt(data_set_num));
    
   NxNode EntryNode= dsInf.NxentryNode;
   AttributeList AL = getGlobalAttributes() ;
   NxfileStateInfo FileState = new NxfileStateInfo( node);
   NxEntryStateInfo EntryState = new NxEntryStateInfo( EntryNode,FileState);
   DataSet DS;
   int instrType = -1;
   if( dsInf.NxdataNode != null){
      Inst_Type it = new Inst_Type();
    
      instrType = (new Inst_Type()).getIsawInstrNum( EntryState.description );
 
  
      DataSetFactory DSF = new DataSetFactory( "" ) ;
      DS = DSF.getTofDataSet(instrType) ; 
      DS.setAttribute( new IntAttribute( Attribute.INST_TYPE, instrType)); 
       
   }else
      DS = new MonitorDataSet();
   DS.setAttributeList( AL ) ;
 
   FileState.Push( EntryState);
   IProcessNxEntry entry = QueryNxEntry.getNxEntryProcessor(FileState, dsInf.NxdataNode,
                 null, dsInf.startGroupID);
   boolean res = entry.processDS( DS, EntryNode, dsInf.NxdataNode, FileState,
           dsInf.startGroupID);
   if( res){
      errormessage =  entry.getErrorMessage();
      DataSetTools.util.SharedData.addmsg("Nexus Input Error:"+errormessage);
      System.out.println("In ExtGetDS, errormessga="+errormessage);
      return null;
   }
   DataSetFactory.addOperators( DS);
   if( instrType >=0) DataSetFactory.addOperators( DS,instrType );
   return DS;

  }

  /**
   * Returns the DataSet or null if a fatal error occurs Obsolete
   */
  private DataSet getDataSet1( int data_set_num ){
    if( !setupDSs ) 
      setUpDataSetList() ;

    if( (data_set_num <0) ||( data_set_num >= EntryToDSs.size())){
      DataSetTools.util.SharedData.addmsg("invalid data set number "
                                          +data_set_num);
      return  null;
    }
    
    NxNode nd2 ;
    nd2= (((DataSetInfo)(EntryToDSs.elementAt(data_set_num))).NxentryNode);
    AttributeList AL = getGlobalAttributes() ;
    
    String Analysis = getAnalysis( nd2 );
    
    Inst_Type it = new Inst_Type();
    int instrType = (new Inst_Type()).getIsawInstrNum( Analysis );
  
   
   DataSetFactory DSF = new DataSetFactory( "" ) ;
   DataSet DS = DSF.getTofDataSet(instrType) ;   
   DS.setAttributeList( AL ) ;

   int list[] = new int[1];
   list[0] = instrType;
   DS.setAttribute( new IntListAttribute( Attribute.INST_TYPE, list ));

   NxNode nDS = (((DataSetInfo)(EntryToDSs.elementAt(data_set_num))).NxdataNode);
   if( nDS == null)
     DS.setAttribute( new StringAttribute(Attribute.DS_TYPE, Attribute.MONITOR_DATA));
   else if( isPulseHeight( nDS.getNodeName()))
     DS.setAttribute( new StringAttribute(Attribute.DS_TYPE, Attribute.PULSE_HEIGHT_DATA));
   else
     DS.setAttribute( new StringAttribute(Attribute.DS_TYPE, Attribute.SAMPLE_DATA));
   
   if( !ProcessNxentryNode( nd2 ,  DS , nDS ) ){
    
     String S = nDS.getNodeName();
     DS.setTitle( S ) ;
     
     DS.setAttribute( new StringAttribute( Attribute.FILE_NAME , filename ) ) ;
     if( nDS.getNodeClass().equals("NXmonitor")){
       //ReOrderGroups(DS);
     }
       
          
   }else{
     DataSetTools.util.SharedData.addmsg( "ERROR:"+errormessage+" ds num="
                                          +data_set_num);
     return null; 
   }
   return DS;
  }

  private boolean isPulseHeight( String S){
    int i1=S.toUpperCase().indexOf("PULSE");
  
    if( i1 < 0)
      return false;

    int i2= S.toUpperCase().indexOf("HEIGHT");
    
    if( i2 < 0)
      return false;
    
    if( i2<i1)
      return false;
    
    if( i2 > i1+4)
      return true;
    
    return false;
  }
  
  //  Returns an attribute list of the NeXus Global attributes that can then
  //   be added to a data set
  private AttributeList getGlobalAttributes(){
    AttributeList Res = new AttributeList() ;
   
    Res.addAttribute( new StringAttribute(  Attribute.FILE_NAME , filename ) ) ;
   
    return Res ;
  }

  private String getAnalysis( NxNode node){
    if( node == null) return "";
    if( !node.getNodeClass().equals("NXentry"))
      return "";
    NxNode n1 = node.getChildNode( "analysis");
    if( n1 == null) 
      return "";
    Object O = n1.getNodeValue();
    
    NxData_Gen ng = new NxData_Gen();
    String S = ng.cnvertoString( O);
    if( S == null) 
      return "";
    else 
      return S;
  } 

  /**
   * returns the number of datasets in this file
   */
  public int numDataSets( ){
    if(!setupDSs) 
      setUpDataSetList() ;
    int u = EntryToDSs.size();
    
    if( u >=0)
      return EntryToDSs.size();
    else
      return 0;
    
  }

  // Method to which(0,1,2..) label in labels a given string is
  private int Position( String labels, String label){
    int Res = 0;
    int i = labels.indexOf( ";"+label+";") ;
    if( i < 0)
       return i;
    for( int k =0; k < i  ; ){
      if( k < i) Res++;
      k++;
      k = labels.indexOf( ";",k);
     
    }
    return Res;
  }

  // Goes thru a NeXus files and finds all Data Sets.
  //  The Node, DefaultID's and NXentry are saved in a DataSetInfo Structure
  //  The DataSetInfo Structure is an internal class in this file
  private void setUpDataSetList(){
    String labels=";";
    setupDSs = true;
    int startID = 1;
    //--------------------- Get all Monitors ------------------------
    for( int i = 0; i< node.getNChildNodes();i++){
      NxNode nn=node.getChildNode(i);
      if( nn.getNodeClass().equals( "NXentry" ) ){
        // Get monitors first
        int nmonitors = 0;
        for( int j=0; j < nn.getNChildNodes(); j++){
           NxNode mm = nn.getChildNode( j ) ;
           if( mm.getNodeClass().equals( "NXmonitor" ) ){
              nmonitors ++;
           }
        }
        
        if( nmonitors > 0)
          EntryToDSs.addElement( new DataSetInfo( nn, null, startID,
                         startID+nmonitors -1, null));

        
        startID += nmonitors;

        // ----------  Now get all NXdata with ---------------------------
        //------------label attributes on their data field(merged)--------------
        
        int start = EntryToDSs.size();
        for( int j = 0 ; j < nn.getNChildNodes() ; j++ ){
          NxNode mm = nn.getChildNode( j ) ;
           if( mm.getNodeClass().equals( "NXdata" ) ){
            NxNode dat=mm.getChildNode("data");
            if( dat != null){
              Object O= dat.getAttrValue("label");
              String S = new NxData_Gen().cnvertoString(O);
              if( S != null){
                if(  labels.indexOf(";"+S+";") < 0){
                   labels+=S.trim()+";";
                   EntryToDSs.addElement( new DataSetInfo(nn,mm,-1,0, S));
                }
                int k =Position(labels,S.trim());
                int[] dim= dat.getDimension();
                
                int nGroups = 1;
                for( int ii=0; ii+1< dim.length;ii++) nGroups *= dim[ii];
                DataSetInfo dsInf = (DataSetInfo)(EntryToDSs.elementAt(k+start));
                dsInf.endGroupID +=nGroups;
              }//has label field
            }//has data field 

           }//class is NXdata
          }//for child nodes
        
            // Determine ID ranges for each data set
        for( int k = startID; k < EntryToDSs.size(); k++){
          DataSetInfo dsInf = (DataSetInfo)(EntryToDSs.elementAt(k));
          dsInf.startGroupID = startID;
          dsInf.endGroupID +=startID;
          dsInf.endGroupID--;
          startID = dsInf.endGroupID;
        }
        
        
        //--------Now get DataSets that are not to be merged---------
        for( int j = 0 ; j < nn.getNChildNodes() ; j++ ){
          NxNode mm = nn.getChildNode( j ) ;
           if( mm.getNodeClass().equals( "NXdata" ) ){
            NxNode dat=mm.getChildNode("data");
            if(dat != null){
              Object O= dat.getAttrValue("label");
              String S = new NxData_Gen().cnvertoString(O);
              if( (O == null)||(S ==null)){
                int[] dim = dat.getDimension();
                int nGroups = 1;
                for( int ii=0; ii+1< dim.length;ii++) nGroups *= dim[ii];
                EntryToDSs.addElement(new DataSetInfo(nn,mm,startID,startID+nGroups -1,null));
                startID  += nGroups;
              }
            }
          }//if NXdata node
        }//for  NxEntries children
       }//if node Class is NXentry
      }//if( node class == NXentry
    
  //------------------ need to get the NXlogs ------------------
  } 

  /**
   * Returns any errormessage or "" if none
   */
  public String getErrorMessage(){
    return errormessage ;
  }
 
  //  Obsolete
  private boolean ProcessNxentryNode( NxNode node, DataSet DS, NxNode nxdata ){
    int i , nchildren ;
    boolean res ;
    NXentry_TOFNDGS Entry ;
    errormessage = "" ;
   
    nchildren = node.getNChildNodes() ;
    
    if( nchildren < 0 ){
      errormessage = node.getErrorMessage() ;
      return false ;
    }
    //System.out.println( "start Find analysis" ) ;
    NxNode child = node.getChildNode( "analysis" ) ;
    
    Object X ;
    if( child == null )
      X = null ;
    else 
      X = child.getNodeValue() ;
    
    String S = "" ;
    if( X == null ) 
      S = "" ;
    else if( X instanceof String ) 
      S  = ( String ) X ;
    else if( X instanceof byte[] ) 
      S = new String( ( byte[] )X ) ;
    else
      S = "" ;
    
 
    if( S.equals( "TOFNDGS" ) ){
      Entry = new NXentry_TOFNDGS( node , DS ,nxdata) ;
      Entry.setNxData( new NxData_Gen() ) ;
      //DS.setAttribute( new IntAttribute( Attribute.INST_TYPE ,
      //                  InstrumentType.TOF_DG_SPECTROMETER ) ) ;      
    }else if( S.equals( "TOFNGS")){
      Entry = new NXentry_TOFNDGS( node , DS ,nxdata) ;
      Entry.setNxData( new NXdata_Fields("time_of_flight","phi","data"  ) ) ;
      Entry.monitorNames[0]="upstream";
      Entry.monitorNames[1]="downstream";
    }else{
      Entry = new NXentry_TOFNDGS( node , DS ,nxdata ) ;
      Entry.setNxData( new NxData_Gen() ) ;
    }
    if(debug)
      System.out.println("Start Process NXEntry");
    res = Entry.processDS(  DS , nxdata ) ;
    if( Entry.getErrorMessage()!= "" )
      errormessage  += ";" + Entry.getErrorMessage() ;
    if( !res )
      errormessage = Entry.getErrorMessage() ;
    return res ;      
      
  }

  // Deprecated
  private DataSet[] retrieveDS(){
    int nchildren , i ; 
    Vector V = new Vector() ;
    NxNode child ;
    if( node == null ) 
      return null ;
    nchildren = node.getNChildNodes() ;
    //System.out.println( "retriever #children = " + nchildren ) ;
    
    for( i = 0 ; i < nchildren ; i++  ){
      child = node.getChildNode( i ) ;
      int ndatasets = 0 ;
      //if( child!= null ) System.out.println( "NXentry child = " + child.show() ) ;
      if( child!= null )
        if( child.getNodeClass().equals( "NXentry" ) ){
          DataSet DS = new DataSet( "","" ) ;
          if( !ProcessNxentryNode( child , DS , null)){//ndatasets ) )
            //System.out.println( "ProcessNxEntry error " + errormessage ) ;
            if( errormessage.equals( "No more DataSets" ) ){}
            else return null ;
          }
          ndatasets++  ;
          V.addElement( DS ) ; 
          //System.out.println( "added a Data Set" ) ;
        }
    }
    DataSet DSS[] ;
    DSS = new DataSet[V.size()] ;
    for( i = 0 ; i < V.size() ; i++ )
      DSS[i] = ( DataSet )( V.elementAt( i ) ) ;
    //System.out.println( "Number of created DataSets = " + V.size() ) ;
    return DSS ;
  }
  
  /**
   * Test program for the module ExtGetDS
   */
  public static void main( String args[] ){
   // System.out.println( "Enter Option desired" ) ;
    //System.out.println( "   s: from nds server" ) ;
    //System.out.println( "   a. from Nexus API-local file" ) ;
    char c = 'a' ;
   
    
    char d = c ;
    System.out.println( "now enter filename" ) ;
    String filename = "" ;
    try{
      c = 0 ;
      while( c <= 32 )
        c = ( char ) System.in.read() ;
      while(  c> 32 ){
        filename = filename + c ;
        c = ( char ) System.in.read() ;
      }
    }catch( IOException s ){
      System.out.println( "Cannot get filename " + s ) ;
      System.exit( 0 ) ;
    }
    System.out.println( "filename = " + filename + "::command= " + d ) ;
  
    NxNode node = null ;
    // NDSClient nds =null ;
     
    if( d == 's' ){
      /*nds = new NDSClient( "dmikk.mscs.uwstout.edu" , 6008 , 6081998 ) ;
        nds.connect() ;
        node = ( NxNode )( new NdsSvNode( filename , nds ) ) ;
      */
    }else if( d == 'a' ){
      node = ( NxNode )( new NexIO.NexApi.NexNode(  filename ) ) ;
    }else
      System.exit( 0 ) ;
    System.out.println("A");
    ExtGetDS X = null ;
    if( node.getErrorMessage() == "" )
      X = new ExtGetDS( node , filename ) ;
    //trics00151999.hdf" ) ;
    //lrcs3000.nxs" ) ;
                       
    else{
      System.out.println( "Node create error =" + node.getErrorMessage() ) ;
      System.exit( 0 ) ;
    }
    System.out.println("B");
    System.out.println( "num data sets = " + X.numDataSets() ) ;
    // System.out.println( X.nEntries.size() + "," + 
    //                 X.nmonitors.size() + "," + X.ndatasets.size() ) ;
   
    /*for( int i = 0 ; i < X.numDataSets() ; i++  ){
      System.out.println("ds num="+i+":"+X.getDataSetInfo(i));
      System.out.println( "Typexx = " + i ) ;
      DataSet DS = X.getDataSet( i ) ;
      if( DS == null )
        System.out.println( "DS cReate ERROR = " +  X.getErrorMessage() ) ;
      else  DataSet_IO.SaveDataSet( DS , "C:\\Test" 
                                    +new Integer(i).toString().trim()+".isd");
     
    }*/

   // Test new stuff
    System.out.println("ere get data set");
     while( 3==3){
        for( int i = 0; i< X.numDataSets(); i++)
           System.out.println( i+":"+X.getDataSetInfo(i)); 
      c =0;
      try{
         while( c < 33)
              c = (char)System.in.read();
     }catch( Exception sss){}
      
     int dsnum = new Integer( new String( ""+c)).intValue();
     DataSet DS = X.getDataSet( dsnum);
     if( DS == null)
        System.out.println( X.getErrorMessage());
     else
        new ViewManager( DS, IViewManager.IMAGE);


    }
  }//end main

}
