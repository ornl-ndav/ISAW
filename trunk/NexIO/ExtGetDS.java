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
 * Revision 1.34  2007/07/11 18:00:35  rmikk
 * Fixed "Pulse" to "PULSE" in  uppercase comparison  code
 * Caught an exception that occurs with an undefined instrument type in the
 *    datasetFactory constructor
 * Used more sub calls in a  method with a lot of code
 *
 * Revision 1.33  2007/06/28 15:26:24  rmikk
 * added the general operators to all data sets
 *
 * Revision 1.32  2006/11/14 16:53:56  rmikk
 * Used routine to parse an ISO time
 * Checked if new xml Fixit file is present  before trying the old fix file
 *
 * Revision 1.31  2006/07/25 00:11:25  rmikk
 * Added a filename to the parameter list for a new NxfileStateInfo
 *
 * Revision 1.30  2006/07/19 18:07:15  dennis
 * Removed unused imports.
 *
 * Revision 1.29  2006/07/13 20:05:26  dennis
 * Replaced code using old style tagging subclasses SampleDataSet
 * and MonitorDataSet.
 *
 * Revision 1.28  2006/01/18 21:50:50  rmikk
 * Did not add the "dirrectory" / if there was no directory
 *
 * Revision 1.27  2006/01/12 19:06:01  rmikk
 * Has special code to handle/fix  LANSCE SCD Nexus files
 *
 * Revision 1.26  2005/03/28 22:47:40  dennis
 * Removed TITLE attribute, since the DataSet already has a
 * field for the title.
 *
 * Revision 1.25  2005/02/10 00:20:12  kramer
 *
 * Now this class will find and return DataSets made from NXlog nodes.
 *
 * Revision 1.24  2005/01/10 16:41:28  rmikk
 * Added blank spacing
 *
 * Revision 1.23  2004/05/14 15:03:25  rmikk
 * Removed unused variables
 *
 * Revision 1.22  2004/02/16 02:15:54  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.21  2003/12/19 14:30:37  rmikk
 * Fixed error when a nexus file has two NXentries.
 *
 * Revision 1.20  2003/12/15 14:39:22  rmikk
 * Fixed a null pointer exception
 * Changed the getDataSetInfo method to correspond to the hasInformation interface
 *
 * Revision 1.19  2003/12/12 15:18:41  rmikk
 * Returned an empty DataSet instead of null when an error occurred
 *
 * Revision 1.18  2003/12/08 23:04:58  rmikk
 * Reads in data for Attribute.USER attribute
 *
 * Revision 1.17  2003/12/08 20:48:28  rmikk
 * Added code to retrieve instrumentType, DataSet Type,Run title, title, Number of
 *     pulses, End date, end time, etc
 *
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
//import NexIO.NDS.* ;
import DataSetTools.dataset.* ;
import DataSetTools.retriever.* ;
import DataSetTools.viewer.* ;
import java.util.* ;
import NexIO.State.*;
import NexIO.Process.*;
import NexIO.Util.*;
import NexIO.Query.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

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
  /** Used to search through a NeXus file for all of its NXlog nodes. */
  NxlogLocator nxLogLocator;

  public ExtGetDS( NxNode node , String filename ){
     this( node, filename, false);
  }
  
  /**
   *  Constructor 
   *  @param node the NxNode used to communicate with the underlying
   *                 datasource
   *  @param filename the filename
   */
  public ExtGetDS( NxNode node , String filename, boolean UsePreSet ){
    this.node = node ;
    this.filename = filename ;
    errormessage = "" ;
    EntryToDSs= new Vector() ;
    this.nxLogLocator = new NxlogLocator(this.node);
    if( UsePreSet)
       RestoreStartUpInfo( null );
  }

  /**
   * Restores the saved startup information for a NeXus file. If successful
   * EntryToDSs and setupDSs variables will be set.
   * If unsuccessful the two variables will be set appropriately
   * 
   * @param filename the name of the file with the start up information
   * 
   */
   public void RestoreStartUpInfo(  String fname ){
      
     String F = NameFile(filename);
     if( F == null || F.length() < 3)
        return;
     String Fname = System.getProperty( "user.home" );
     if( Fname.length() > 0 && !(Fname.endsWith( "\\" ) || Fname.endsWith( "/" ) ))
        Fname =Fname+"/";

     Fname = Fname+"ISAW/"+F.substring( 0,3 )+".startup";
     
     if( fname != null )
        Fname = fname;
     this.EntryToDSs = new Vector();
     this.setupDSs = true;
     EntryToDSs = new Vector();
     NxNode entry,instr,source,moderator,beam, sample;
     entry = instr = source = moderator = beam = sample =null;
     try{
        FileInputStream fin = new FileInputStream( Fname );
        for( String line= Peak.getLine(fin); line !=null; line = Peak.getLine(fin)){
           int endDetectorID = Integer.parseInt( line.trim() );
           int endGroupID= Integer.parseInt( Peak.getLine(fin).trim() );;
           int startDetectorID= Integer.parseInt(Peak.getLine(fin).trim() );;
           int startGroupID= Integer.parseInt( Peak.getLine(fin).trim() );;
           int nelts = Integer.parseInt( Peak.getLine(fin).trim() );
           int ndetectors = Integer.parseInt( Peak.getLine( fin ).trim());
           String label = Peak.getLine(fin).trim();
           if( label != null && label.length() < 1)
              label = null;
           String NodeNames  = Peak.getLine(fin).trim();

           String NxentryName = Peak.getLine(fin).trim();
           String NxdataName = Peak.getLine(fin).trim(); 
           String NxInstrumentName = Peak.getLine(fin).trim();
           String NxBeamName = Peak.getLine(fin).trim();
           String NxInstrModeratorName = Peak.getLine(fin).trim();
           String NxInstrSourceName = Peak.getLine(fin).trim();
           String NxSampleName = Peak.getLine(fin).trim();
           if( entry == null || !entry.getNodeName().equals(  NxentryName )){
              entry = node.getChildNode( NxentryName );
              if( entry == null ){
                 setupDSs=false; 
                 return;
              }
              instr = entry.getChildNode( NxInstrumentName );
              source = NexUtils.GetSubNode( instr, NxInstrSourceName );
              moderator =NexUtils.GetSubNode( instr, NxInstrModeratorName);   
              beam = entry.getChildNode( NxBeamName );
              sample = entry.getChildNode(  NxSampleName );
            
           }
           DataSetInfo dsInf =CreateSetUpEntry( endDetectorID, endGroupID, 
                    startDetectorID, startGroupID, nelts, ndetectors,  label,
                    NodeNames, entry,  NxdataName, instr, 
                    beam, moderator,  source,sample );
           if( dsInf != null)
              EntryToDSs.addElement(  dsInf );
           else{
             EntryToDSs = new Vector();
             setupDSs = false;
           }
           
           
        }  
        
     }catch( Exception ss){
        this.EntryToDSs = new Vector();
        this.setupDSs = false;
     }
     
   }
   
   /**
    * Creates an internal block describin a data set
    * 
    * @param endDetectorID   Last detectorID( not needed)
    * @param endGroupID      Last GroupID( not needed)
    * @param startDetectorID Start detector ID( necessary))
    * @param startGroupID     Start Group ID( necessary))
    * @param nspectra        Number of spectra in this data set
    * @param label           label name of an attribute in an NXdata.data to 
    *                         be merged with other NXdata with this label name
    * @param NodeNames       List of monitor names(NXdataName=null) or NXdata names
    *                         with the same label
    * @param NXentryName    The NXentry name for this data set
    * @param NXdataName     The name of an NXdata for this data set
    * @param NxInstrumentName  The NXinstrument name for this NXentry
    * @param NxBeamName        The NXbeam node name in this NXentyr
    * @param NXModeratorName    the name of the NXmoderator node in NXinstrument
    * @param NxSourceName     The name of the NXsource entry
    * @param NxSampleName    The name of the NXsample entry.
    * 
    * @return  The above information transformed to the internal form.
    */
   public DataSetInfo CreateSetUpEntry( int endDetectorID, int endGroupID, 
                                int startDetectorID, int startGroupID, 
                                int nspectra, int ndet, String label,String NodeNames,
                                String NXentryName, String NXdataName,
                                String   NxInstrumentName, String NxBeamName,
                                String NXModeratorName, String NxSourceName,
                                String NxSampleName ){
      
      NxNode entry = node.getChildNode( NXentryName );
      if( entry == null )
         return null;
      NxNode instr = entry.getChildNode(  NxInstrumentName );
      NxNode data  = entry.getChildNode(  NXdataName );
      DataSetInfo dsInf= new DataSetInfo( entry, data, startGroupID, endGroupID , label );
      dsInf.NxInstrumentNode = instr;
      dsInf.startDetectorID = startDetectorID;
      dsInf.endDetectorID = endDetectorID;
      dsInf.nelts = nspectra;
      if( NodeNames.trim().length()>0)
         dsInf.NodeNames = NodeNames;
      else 
         dsInf.NodeNames = null;
      
      dsInf.NxBeamNode = entry.getChildNode( NxBeamName );
      dsInf.NxInstrModeratorNode = NexUtils.GetSubNode( instr, NXModeratorName );
      dsInf.NxInstrSourceNode = NexUtils.GetSubNode( instr,NxSourceName );
      dsInf.NxSampleNode = entry.getChildNode( NxSampleName );
      return dsInf;
   }
   
   /**
    * Faster create setUpEntry when previous DataSetInfo is in the same NXentry 
    * of a NeXus file.
    * 
    * @param dataInf      Previous DataSetInfo in the same NXentry
     
    * @param endDetectorID   Last detectorID( not needed)
    * @param endGroupID      Last GroupID( not needed)
    * @param startDetectorID Start detector ID( necessary))
    * @param startGroupID     Start Group ID( necessary))
    * @param nspectra        Number of spectra in this data set
    * @param label           label name of an attribute in an NXdata.data to 
    *                         be merged with other NXdata with this label name
    * @param NodeNames       List of monitor names(NXdataName=null) or NXdata names
    ** @param NXdataName  The name of an NXdata for this data set
    * @return   The above information transformed to the internal form.
    */
   public DataSetInfo CreateSetUpEntry( DataSetInfo dataInf, int endDetectorID , int endGroupID ,
            int startDetectorID , int startGroupID , int nspectra , int ndet ,
            String label , String NodeNames , String NXdataName ){
      
      return CreateSetUpEntry( endDetectorID ,  endGroupID , startDetectorID , 
               startGroupID ,  nspectra , ndet , label ,  NodeNames, 
               dataInf.NxentryNode, NXdataName , dataInf.NxInstrumentNode,
               dataInf.NxBeamNode, dataInf.NxInstrModeratorNode, 
               dataInf.NxInstrSourceNode, dataInf.NxSampleNode);
      
   }
   
   private DataSetInfo CreateSetUpEntry( int endDetectorID , int endGroupID ,
            int startDetectorID , int startGroupID , int nspectra , int ndet ,
            String label , String NodeNames , NxNode entry , String NXdataName ,
            NxNode instr , NxNode NxBeam , NxNode NXModerator ,
            NxNode NxSource , NxNode NxSample ) {


      if( entry == null )
         return null;
      
      NxNode data = entry.getChildNode( NXdataName );
      DataSetInfo dsInf = new DataSetInfo( entry , data , startGroupID ,
               endGroupID , label );
      dsInf.NxInstrumentNode = instr;
      dsInf.startDetectorID = startDetectorID;
      dsInf.endDetectorID = endDetectorID;
      dsInf.nelts = nspectra;
      if( NodeNames.trim().length() > 0 )
         dsInf.NodeNames = NodeNames;
      else
         dsInf.NodeNames = null;

      dsInf.NxBeamNode = NxBeam;
      dsInf.NxInstrModeratorNode = NXModerator;
      dsInf.NxInstrSourceNode =  NxSource;
      dsInf.NxSampleNode = NxSample;
      return dsInf;
   }

   
   
   
  /**
   * Saves the start up information to the ISAW subdirectory in the user's home directory in
   * a file derived from the first 3 letters in the filename( -path)
   * 
   * @param filename  the name of the file to have the start up information
   * 
   * @return true if successful otherwise false.
   */
  public boolean SaveStartUpInfo( String fname){
     if(!setupDSs ) 
        setUpDataSetList() ;
     
     if( filename == null)
        return false;
     
     String Fname = System.getProperty( "user.home" );
     if( Fname == null)
        Fname ="";
     if( Fname.length() > 0 && !(Fname.endsWith( "\\" ) || Fname.endsWith( "/" ) ))
          Fname =Fname+"/";
    
     String F = NameFile( filename);
     if( F == null || F.length() < 3)
        return false;
     Fname = Fname+"ISAW/"+F.substring( 0,3 )+".startup";
     if( fname != null)
        Fname = fname;
     
     try{
        FileOutputStream fout = new FileOutputStream( Fname );
        for( int i=0; i< this.EntryToDSs.size(); i++){
           DataSetInfo dsInf = (DataSetInfo)EntryToDSs.elementAt( i );
           fout.write((dsInf.endDetectorID+"\n").getBytes());
           fout.write((dsInf.endGroupID+"\n").getBytes());
           fout.write((dsInf.startDetectorID+"\n").getBytes());
           fout.write((dsInf.startGroupID+"\n").getBytes());
           fout.write((dsInf.nelts+"\n").getBytes());
           fout.write((dsInf.ndetectors+"\n").getBytes());
           fout.write((FixUpNull(dsInf.label)+"\n").getBytes());
           fout.write((FixUpNull(dsInf.NodeNames)+"\n").getBytes());

           if( dsInf.NxentryNode == null)
              fout.write(" \n".getBytes());
           else         
              fout.write((dsInf.NxentryNode.getNodeName()+"\n").getBytes());

           if( dsInf.NxdataNode == null)
              fout.write(" \n".getBytes());
           else
              fout.write((dsInf.NxdataNode.getNodeName()+"\n").getBytes()); 

           if( dsInf.NxInstrumentNode == null)
              fout.write(" \n".getBytes());
           else
              fout.write((dsInf.NxInstrumentNode.getNodeName()+"\n").getBytes());
           
           if( dsInf.NxBeamNode == null)
              fout.write(" \n".getBytes());
           else
              fout.write((dsInf.NxBeamNode.getNodeName()+"\n").getBytes());

           if( dsInf.NxInstrModeratorNode == null)
              fout.write(" \n".getBytes());
           else
              fout.write((dsInf.NxInstrModeratorNode.getNodeName()+"\n").getBytes());
           if( dsInf.NxInstrSourceNode == null)
              fout.write(" \n".getBytes());
           else
              fout.write((dsInf.NxInstrSourceNode.getNodeName()+"\n").getBytes());
           if( dsInf.NxSampleNode == null)
              fout.write(" \n".getBytes());
           else
              fout.write((dsInf.NxSampleNode.getNodeName()+"\n").getBytes());
           
           
        }
        fout.close();
     }catch( Exception s){
         
       return false;
     }
    
     
    return true;    
     
  }
  
  private String FixUpNull( String S){
     if( S == null)
        return " ";
     if( S.length()<1)
        return " ";
     return S;
  }
  /**
   * Returns the filename with the path removed
   * 
   * @param wholeFileName  Whole filename including path
   * 
   * @return the filename with the path removed or null if filename is null
   */
  public String NameFile( String wholeFileName ){
     
       if( wholeFileName == null )
          return null;
       int i= wholeFileName.lastIndexOf('\\');
       i = Math.max(  i ,wholeFileName.lastIndexOf('/')  );
     
       String F = filename;
       if( i >=0)
          F = filename.substring( i+1);
       return F;
          
  
     
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
    else if( nd.getNodeName().toUpperCase().indexOf("PULSE")>=0)
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
  public String[] getDataSetInfo( int data_set_num){
    if( !setupDSs ) 
      setUpDataSetList() ;
      
    if( data_set_num < 0)
        return null;
        
    if( data_set_num >= EntryToDSs.size())
       return null;
       
    DataSetInfo dsInf = ((DataSetInfo)( EntryToDSs.elementAt( data_set_num)));
    String[] S = new String[3];
    S[1]  = getType( data_set_num)+"";
    
    if (dsInf.NxdataNode!= null)
         S[0] = dsInf.NxdataNode.getNodeName();
         
    else 
          S[0] =dsInf.NxentryNode.getNodeName();
   
     S[2] = ""+dsInf.startGroupID+"-"+dsInf.endGroupID;
   
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
    
   return getDataSet( dsInf);
 }
 
 
  /**
   * Gets a data set from the information in the DataSetInfo argument
   * 
   * @param dsInf   Contains information on where to get the information in 
   *                the NeXus file to create the Data set
   * @return   The Data set corresponding to dsInf
   * 
   * @see CreateSetUpEntry( int, int,int,int,int,String,String,String,String,String,String,String,String,String)
   * @seen CreateSetUpEnry( DataSetInfo, int, int, int, int, int, int,String,String)
   */
 public DataSet getDataSet( DataSetInfo dsInf){
 
   if( dsInf == null)
      return null;
   
   NxNode EntryNode= dsInf.NxentryNode;
   AttributeList AL = getGlobalAttributes( EntryNode ) ;
   
   NxfileStateInfo FileState = new NxfileStateInfo( node , filename,
            dsInf.NxInstrSourceNode);

  
   NxEntryStateInfo EntryState = new NxEntryStateInfo( EntryNode,FileState,
               dsInf.NxInstrumentNode, dsInf.NxSampleNode, dsInf.NxBeamNode,
               dsInf.NxInstrSourceNode);
   EntryState.InstrModeratorNode=dsInf.NxInstrModeratorNode;
   EntryState.NodeNames = dsInf.NodeNames;
   
   DataSet DS;
   int instrType = -1; 
   
   instrType = (new Inst_Type()).getIsawInstrNum( EntryState.description );
   
   if( dsInf.NxdataNode != null){
      
      if (NxlogLocator.isNxLog(dsInf.NxdataNode))
      {
         //TODO Verify that this should still return 'dataSet' 
         //     even if processDS(....) encounters and error and quits
         DataSet dataSet = new DataSet();
         (new Nxlog()).processDS(dsInf.NxdataNode,dataSet);
         return dataSet;
      }
  
      DataSetFactory DSF = new DataSetFactory( "" ) ;
      try{
        DS = DSF.getTofDataSet(instrType) ;
      }catch( IllegalArgumentException ss){
         DS = DSF.getDataSet();
      }
      DS.setAttributeList( AL ) ;
      DataSetFactory.addOperators( DS );
      if( instrType >0)
         DataSetFactory.addOperators( DS,instrType );
      DS.setAttribute( new IntAttribute( Attribute.INST_TYPE, instrType)); 
      DS.setAttribute( new StringAttribute( Attribute.DS_TYPE,Attribute.SAMPLE_DATA));
     
       
   }else{
      DS = new DataSet();
      DS.setAttributeList( AL ) ;

      DataSetFactory.addOperators( DS );
      if( instrType >= 0)
          DataSetFactory.addMonitorOperators( DS, instrType);
      DS.setAttribute( new IntAttribute( Attribute.INST_TYPE, instrType)); 
      DS.setAttribute( new StringAttribute( Attribute.DS_TYPE,Attribute.MONITOR_DATA));
   }
   
   DS.setTitle( EntryNode.getNodeName() );

  
   FileState.Push( EntryState);
   FileState.Push( new NxInstrumentStateInfo(dsInf.NxInstrumentNode, FileState));
   IProcessNxEntry entry = QueryNxEntry.getNxEntryProcessor(FileState, 
                     dsInf.NxdataNode,  null, dsInf.startGroupID);
   
   if( dsInf.NxdataNode != null){
      NxDataStateInfo DataState = new NxDataStateInfo( dsInf.NxdataNode, 
               FileState.InstrumentNode, FileState, dsInf.startGroupID);
      DataState.startDetectorID = dsInf.startDetectorID;
      FileState.Push( DataState );
      
   }
  
   boolean res = entry.processDS( DS, EntryNode, dsInf.NxdataNode, FileState,
           dsInf.startGroupID);
           
   if( res){
     
      errormessage =  entry.getErrorMessage();
      DataSetTools.util.SharedData.addmsg("Nexus Input Error:"+errormessage);
      System.out.println("In ExtGetDS, errormessga="+errormessage);
      //return DS;
      
   }
   
   String DSType = (String)DS.getAttributeValue( Attribute.DS_TYPE);
   if( DSType == null)
     return DS;
   else if( DSType.toUpperCase().indexOf("SAMPLE") <0)
     return DS;
   
   if(!filename.toUpperCase().endsWith(".NX.HDF"))
     return DS;
     
   int startFileName = filename.lastIndexOf('/');
   if( startFileName < 0) startFileName = filename.lastIndexOf('\\');
   if( !filename.substring( startFileName+1, filename.length()-7).toUpperCase().startsWith(
                 "SCD_E00000"))
      return DS;
   NxNode NN = EntryNode.getChildNode( "program_name");
   if( NN == null)
      return DS;
   DS.setAttribute( new IntAttribute( Attribute.INST_TYPE, 
               DataSetTools.instruments.InstrumentType.TOF_SCD));
   {
       Object OprogName = NN.getNodeValue();
       if( OprogName != null){
           String progName = NexIO.Util.ConvertDataTypes.StringValue( OprogName );
           if( progName != null) progName=progName.toUpperCase();
           else progName ="";
           String dt = DS.getAttribute( Attribute.END_DATE).getValue().toString();
           Date dt1 = NexIO.Util.ConvertDataTypes.parse(dt);
           Calendar Cal =  Calendar.getInstance();
           Cal.set(2006,2,1);
           Date dt2 = new Date();
           dt2.setTime( Cal.getTimeInMillis());
           String dir ="";
           if(startFileName >=0)
              dir = filename.substring(0,startFileName);
           if( dir != null){
             dir= dir.replace('\\','/');
           if( !dir.endsWith("/"))if( dir.trim().length()>0)
               dir +="/";
          
           }
           if( FileState.xmlDoc == null)
           if( dt1.before(dt2))if( dir != null) 
           if( (progName.indexOf("LANSCE")>=0) ||(progName.indexOf("lanl")>=0))
             Operators.Special.Calib.FixLansceSCDDataFiles( DS,dir+"SCDfix.lanl" );
           
       }
       
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
  
  //  Returns an attribute list of the NeXus  attributes that can then
  //   be added to a data set
  private AttributeList getGlobalAttributes( NxNode EntryNode ){
    AttributeList Res = new AttributeList() ;
   
    Res.addAttribute( new StringAttribute(  Attribute.FILE_NAME , filename ) );
    NxNode userNode = EntryNode.getChildNode("user");
    
    String user= null;
    if( userNode != null)
          user = NexIO.Util.ConvertDataTypes.StringValue( 
                                       userNode.getNodeValue( ));
          
    if( user != null)
      Res.setAttribute(new StringAttribute(Attribute.USER, user));
    
    
    NxNode ET = EntryNode.getChildNode("end_time");
    String S = null;
    if( ET != null)
         S= ConvertDataTypes.StringValue(ET.getNodeValue());
         
    if( S != null){
      
       Date D = null;
       D = NexIO.Util.ConvertDataTypes.parse2Date( S );
       if( D== null)
           D = NexIO.Util.ConvertDataTypes.parse(S);
       
       if( D != null){
         
         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat();
         sdf.applyPattern("MMM dd,yyyy");
         Res.setAttribute( new StringAttribute( Attribute.END_DATE,
               sdf.format( D)));
         sdf.applyPattern("HH:mm:ss"); 
         Res.setAttribute( new StringAttribute( Attribute.END_TIME,
               sdf.format( D)));

          
       }
    }
    
   // add user when I print it out
    return Res ;
  }




  private String getAnalysis( NxNode nnode){
    if( nnode == null) 
       return "";
    if( !nnode.getNodeClass().equals("NXentry"))
      return "";
    NxNode n1 = nnode.getChildNode( "analysis");
    if( n1 == null)
       n1 = nnode.getChildNode( "description" );
       
    if( n1 == null) 
      return "";
      
    Object O = n1.getNodeValue();
    
    NxData_Gen ng = new NxData_Gen();
    String S = ng.cnvertoString( O);
    if( S == null) 
      return "";
   
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
      
   
    return 0;
      
    
  }
  
  

  // Method to which(0,1,2..) label in labels a given string is
  private int Position( String labels, String label){
    
    int Res = 0;
    int i = labels.indexOf( ";"+label+";") ;
    if( i < 0)
       return i;
       
    for( int k =0; k < i  ; ){
      if( k < i) 
         Res++;
      k++;
      k = labels.indexOf( ";",k);
     
    }
    return Res;
  }



  // Goes thru a NeXus files and finds all Data Sets.
  //  The Node, DefaultID's and NXentry are saved in a DataSetInfo Structure
  //  The DataSetInfo Structure is an internal class in this file
  private void setUpDataSetList1() {

      setupDSs = true;
      // --------------------- Get all Monitors ------------------------
      for( int i = 0 ; i < node.getNChildNodes() ; i++ ) {

         
         NxNode nn = node.getChildNode( i );

         if( nn.getNodeClass().equals( "NXentry" ) ) {
            // Get monitors first
            NxNode InstrumentNode = AddMonitorDataSetInfos( nn, EntryToDSs);

            // ---------- Now get all NXdata with ---------------------------
            // ------------label attributes(merge common labels) on their data
           
            AddLabeledHistogramDataSets( nn, EntryToDSs);


            AddUnMergedHistogramDataSets( nn, EntryToDSs);

            
            RecordGivenGroupDetIDs(nn, EntryToDSs, InstrumentNode);

            // Set up Default IDS;
            SetDefaultGroupDetIDs( nn, EntryToDSs);

         }// if( node class == NXentry


      }// for @ child node


      // ------------------ now to get the NXlogs ------------------
      int numLogDS = nxLogLocator.getNumNxLogDataSets();
      for( int i = 0 ; i < numLogDS ; i++ )
         EntryToDSs.add( nxLogLocator.getNxLogDataSet( i ) );
   
   }
  
  private void propogate( NxNode instrumentNode, NxNode SampleNode,
                                            NxNode beamNode, int StartIndex ){
     for( int i=StartIndex; i< EntryToDSs.size(); i++){
        DataSetInfo dsInf =(DataSetInfo)(EntryToDSs.elementAt( i ));
        if( dsInf != null){
           upDate(dsInf, instrumentNode, SampleNode,beamNode);
        }
     }
  }
  private void upDate( DataSetInfo dsInf, NxNode instrumentNode, NxNode SampleNode,
                                            NxNode beamNode ){

     dsInf.NxInstrumentNode = instrumentNode;
     dsInf.NxSampleNode  = SampleNode;
     dsInf.NxBeamNode = beamNode;
  }
   private void setUpDataSetList(){
      setupDSs= true;
      for( int i = 0 ; i < node.getNChildNodes() ; i++ ) {
         NxNode nn = node.getChildNode( i );
         if( nn.getNodeClass().equals( "NXentry" ) ) {
            NxNode InstrumentNode = null;
            NxNode SampleNode = null;
            NxNode BeamNode = null;
            
            int startEntryToDSsElement = EntryToDSs.size();
            int nChildren =nn.getNChildNodes();
            DataSetInfo MonitorDSinf = null;
            for( int child=0; child <nChildren; child++){
               NxNode childNode = nn.getChildNode( child );
               if( childNode != null && childNode.getNodeClass()!= null){
                  String nodeClass = childNode.getNodeClass();
                  if( nodeClass.equals("NXinstrument")){
                     InstrumentNode = childNode;
                    
                  }else if(nodeClass.equals("NXbeam")){
                     BeamNode = childNode;
                     nxLogLocator.scanForNxLogUnderNode( childNode );
                  
                  }else if( nodeClass.equals("NXsample")){
                     SampleNode = childNode;
                     nxLogLocator.scanForNxLogUnderNode( childNode );
                  
                  }else if( nodeClass.equals( "NXmonitor" )){
                     MonitorDSinf = this.AddOneMonitor( nn, childNode, MonitorDSinf );

                     nxLogLocator.scanForNxLogUnderNode( childNode );
                
                  }else if( nodeClass.equals("NXdata")){
                     AddOneLabeledHistogramDataSet( nn, childNode,startEntryToDSsElement);
                     AddOneUnMergedHistogramDataSets( nn, childNode);
                     nxLogLocator.scanForNxLogUnderNode( childNode );
                  }else if( !nodeClass.equals("SDS"))
                     nxLogLocator.scanForNxLogUnderNode( childNode );
               }//if child node is not null
            }//for each NXentry child
            if( MonitorDSinf != null)
               EntryToDSs.insertElementAt( MonitorDSinf ,startEntryToDSsElement  );
            propogate(InstrumentNode,SampleNode ,BeamNode ,startEntryToDSsElement);
            GetPropogateInstSource( InstrumentNode, startEntryToDSsElement);
            RecordGivenGroupDetIDs(nn, EntryToDSs, InstrumentNode);
            SetDefaultGroupDetIDs( nn, EntryToDSs);
         }//NXentry not null
         }// for each NXentry
      

      // ------------------ now to get the NXlogs ------------------
 /*     int numLogDS = nxLogLocator.getNumNxLogDataSets();
      for( int i = 0 ; i < numLogDS ; i++ )
         EntryToDSs.add( nxLogLocator.getNxLogDataSet( i ) );
 */
   }
   
   private void GetPropogateInstSource( NxNode InstrumentNode, 
                                                                  int startEntryIndx){
      if( InstrumentNode == null)
         return;
      NxNode InstrSource = null;
      NxNode InstrModerator = null;
      for( int i=0; i< InstrumentNode.getNChildNodes()  ; i++){
         NxNode childNode = InstrumentNode.getChildNode( i );
         nxLogLocator.scanForNxLogUnderNode( childNode );
         if( childNode != null && childNode.getNodeClass()!=  null)
               if( childNode.getNodeClass().equals( "NXsource" ))
                    InstrSource = childNode;
               else if( childNode.getNodeClass().equals("NXmoderator"))
                  InstrModerator = childNode;
      }
      
      if( InstrSource == null && InstrModerator == null)
         return;
      for( int i=0; i< EntryToDSs.size(); i++){
         DataSetInfo dsInf =(DataSetInfo)(EntryToDSs.elementAt( i ));
         if( dsInf != null){
            dsInf.NxInstrSourceNode = InstrSource;
            dsInf.NxInstrModeratorNode = InstrModerator;
         }
      }
      
   }

  // inserting ranges
  private void Insert( Vector V, int[] element){
     if( V == null)
        return;
     if( V.size() < 1){
       V.addElement( element);
        return;
     }
     for( int i=0; i< V.size(); i++){
        int[] elt = (int[]) V.elementAt(i);
        if( elt[0] > element[0]){
           V.insertElementAt( element, i);
           return;
        } else if( elt[0] == element[0] && elt[1]> element[1]){
              V.insertElementAt( element, i);
              return;
        }
              
        }
        V.addElement( element);   
     }

  // negative numbers are undefined
   private int update( int n1, int n2, boolean minimize){
      if( n1 < 0)
         return n2;
      if( n2 < 0)
         return n1;
      if( n1 < n2 ){
         if( minimize)
            return n1;
         
         return n2;
      }else if( minimize)
         return n2;
      else
         return n1;
   }  
      
 
   // returns null if there are no fields set
  // last element is the number of detectors
   private int[] GetMinMaxSetGroupDetectorIDs( NxNode DataNode, NxNode InstrumentNode){
       
      NxNode dataNode = DataNode.getChildNode("data");
      int[] dims = dataNode.getDimension();
      int NGroups = 1;
      if( dims != null)
         for( int i=0; i< dims.length -1; i++)
           NGroups*= dims[i];
      else
           NGroups = 0;
      
      String Link = null;
      for(int ik =0; ik < DataNode.getNChildNodes(); ik++){
         NxNode nnode = DataNode.getChildNode(ik);
         String S = ConvertDataTypes.StringValue(
                 
                          nnode.getAttrValue("link"));
         if( S == null)
            S =ConvertDataTypes.StringValue(
                    
                              nnode.getAttrValue("target")); 
         if( S != null)
            Link = NexIO.State.NxDataStateInfo.FixUp(S, nnode);
         
      } 
      int[] Res= new int[5];
      
      Res[0]=Res[1]=Res[2] = Res[3]=Res[4]=-1;
      if( Link == null)
         return Res;
     
      NxNode detNode = NexUtils.getCorrespondingNxDetector( Link,  
                                                    InstrumentNode);
      if( detNode == null )
         return Res;
      
      int[] ids = NexUtils.getIntArrayFieldValue( detNode,"id");
      int[] detNums = NexUtils.getIntArrayFieldValue( detNode , "detector_number");
      if(ids == null && detNums!=null && detNums.length >= NGroups){
         ids = detNums;
         detNums = null;
      }
  
      Res[2] = 0;
      if( detNums != null)
         if( detNums.length <= NGroups)
            Res[2] = NGroups/detNums.length;
      if( ids != null  && ids.length > 0){
         Res[0] = ids[0];
         Res[1] = Math.max(  Res[0]+ NGroups-1,ids[ids.length-1]);
      }
      if( detNums != null && detNums.length >0){
         Res[3] = detNums[0];
         Res[4] = Math.max( detNums[detNums.length-1], Res[3]+ Res[2]-1);
         
      
      }
      return Res;
      
     
   }
   private int updateMinValue( int previous , Integer newVal){
      if( newVal == null)
         return previous;
     int nn = newVal.intValue();
     if( nn== Integer.MIN_VALUE)
        return previous;
     if( nn < previous)
        return nn;
     return previous;
   }
   


   private int updateMaxValue( int previous , Object newVal){
      if( newVal == null)
         return previous;
     int nn = ConvertDataTypes.intValue( newVal);
     if( nn== Integer.MIN_VALUE)
        return previous;
     if( nn > previous)
        return nn;
     return previous;
   }
   
   /**
    * Adds the information on monitors to the DataSet info list. This is done
    * first If there is a detector_number field or id field, the start and/or
    * end Group and/or detector ID's will be set.
    * 
    * @param nn
    *           An NXentry node
    * @param EntryToDSs
    *           The vector of DataSetInfo structures
    * 
    * @return The Instrument Node
    */
   private NxNode AddMonitorDataSetInfos( NxNode nn , Vector EntryToDSs ) {

      int nmonitors = 0;
      NxNode InstrumentNode = null;

      int startID = 1;
      int minGroupID = 1;
      int maxGroupID = 1;
      boolean foundGroupIDs = false;
      int minDetectorID = 1;
      int maxDetectorID = 1;
      boolean foundDetectorIDs = false;
      
      for( int j = 0 ; j < nn.getNChildNodes() ; j++ ) {
         
         NxNode mm = nn.getChildNode( j );
         if( mm.getNodeClass().equals( "NXinstrument" ) )
            
            InstrumentNode = mm;
         
         else if( mm.getNodeClass().equals( "NXmonitor" ) ) {
            
            nmonitors++ ;
            Integer detID = NexUtils.getIntFieldValue( mm , "detector_Number" );
            
            Integer grID = NexUtils.getIntFieldValue( mm , "id" );
            
            if( grID == null ) 
               grID = detID;
            
            minGroupID = updateMinValue( minGroupID , grID );
            minDetectorID = updateMinValue( minDetectorID , detID );
            maxGroupID = updateMaxValue( maxGroupID , grID );
            maxDetectorID = updateMaxValue( maxDetectorID , detID );
            
            if( grID != null ) 
               foundGroupIDs = true;
            
            if( detID != null ) 
               foundGroupIDs = foundDetectorIDs = true;

         }
      }

      if( nmonitors > 0 ) {
         DataSetInfo Dinf = new DataSetInfo( nn , null , 1 , nmonitors , null );
         Dinf.nelts = nmonitors;
         Dinf.ndetectors = nmonitors;
         if( foundGroupIDs ) {
            Dinf.startGroupID = minGroupID;
            Dinf.endGroupID = maxGroupID;
         }
         else {
            Dinf.startGroupID = - 1;
            Dinf.endGroupID = - 1;
         }

         if( foundDetectorIDs ) {
            Dinf.startDetectorID = minDetectorID;
            Dinf.endDetectorID = maxDetectorID;

         }
         else {
            Dinf.startDetectorID = - 1;
            Dinf.endDetectorID = - 1;

         }

         EntryToDSs.addElement( Dinf );
      }

      if( foundGroupIDs )
         startID = Math.max( startID + 1 , maxGroupID + 1 );
      else
         startID = nmonitors + 1;
      return InstrumentNode;
   }

   
   /**
    * Updates this NxMonitor node into the data set Info, 
    * @param mm The node.  It checks to determine if it is an
    *                  NXmonitor Node;
    * @param dsInf  the DataSetInfo so far on the monitor nodes
    *               in this NXentry
    * @return  The updated DataSetInfo structure or null if not
    *          found
    */
   private DataSetInfo AddOneMonitor( NxNode entryNode,NxNode mm, DataSetInfo dsInf){
      if( mm == null)
         return dsInf;
      if( mm.getClass() == null)
         return dsInf;
      if( !mm.getNodeClass().equals("NXmonitor"))
         return dsInf;
      //------------Is a monitor node ---------
     
      Integer detID = NexUtils.getIntFieldValue( mm , "detector_Number" );
      
      Integer grID = NexUtils.getIntFieldValue( mm , "id" );
      
      if( grID == null ) 
         grID = detID;
      int minGroupID,minDetectorID, maxGroupID, maxDetectorID;
      if( dsInf == null){
         minGroupID=minDetectorID=maxGroupID=maxDetectorID=-1;
         dsInf = new DataSetInfo( entryNode , null , 1 , 1 , null );
         dsInf.nelts = dsInf.ndetectors = 1;
      }else{
         minGroupID=dsInf.startGroupID;
         minDetectorID = dsInf.startDetectorID;
         maxDetectorID =dsInf.endDetectorID;
         maxGroupID = dsInf.endGroupID;
         dsInf.nelts++;
         dsInf.ndetectors++;
      }
     
      dsInf.startGroupID = updateMinValue( minGroupID , grID );
      dsInf.endDetectorID = updateMinValue( minDetectorID , detID );
      dsInf.endGroupID = updateMaxValue( maxGroupID , grID );
      dsInf.startDetectorID = updateMaxValue( maxDetectorID , detID );
      if( dsInf.NodeNames.length() > 0)
         dsInf.NodeNames +=";";
      dsInf.NodeNames += mm.getNodeName();
      return dsInf;
     
      
   }
   /**
    * Adds labeled( to be merged) histogram info to EntryToDSs
    * 
    * @param nn
    *           NxEntry node
    * @param EntryToDSs
    *           Vector of DataSet Information
    */
   private void AddLabeledHistogramDataSets( NxNode nn , Vector EntryToDSs ) {

      String labels = ";";
      int start = EntryToDSs.size();
      for( int j = 0 ; j < nn.getNChildNodes() ; j++ ) {

         NxNode mm = nn.getChildNode( j );
         if( mm.getNodeClass().equals( "NXdata" ) ) {

            NxNode dat = mm.getChildNode( "data" );
            if( dat != null ) {

               Object O = dat.getAttrValue( "label" );
               String S = new NxData_Gen().cnvertoString( O );
               if( S != null ) {

                  if( labels.indexOf( ";" + S + ";" ) < 0 ) {

                     labels += S.trim() + ";";
                     EntryToDSs.addElement( new DataSetInfo( nn , mm , - 1 ,
                              - 1 , S ) );

                  }
                  int k = Position( labels , S.trim() );
                  int[] dim = dat.getDimension();

                  int nGroups = 1;
                  for( int ii = 0 ; ii + 1 < dim.length ; ii++ )
                     nGroups *= dim[ ii ];
                  DataSetInfo dsInf = (DataSetInfo) ( EntryToDSs.elementAt( k
                           + start ) );
                  dsInf.nelts += nGroups;

                  dsInf.ndetectors = - 1; // Will be done when NXdetector
                  // is
                  // available


               }// has label field
            }// has data field

         }// class is NXdata
      }// for child nodes

   }
   /**
    * Will look at one NXdata in an NXenty and determine if it is a labeled
    * (to be merged) data set. If so, updates EntryToDS's to add this NXdata 
    * to the proper element of EntryToDS's
    * 
    * @param entryNode   The NXentry node
    * @param mm          The node for a child of this NXentry node. Will check that
    *                    it is a NXdata node
    * @param StartThisNXentryElement  index into EntryToDSs where this NXentry's
    *                                 information starts
    */
   private void AddOneLabeledHistogramDataSet( NxNode entryNode, NxNode mm, 
                                                      int StartThisNXentryElement){
      if( entryNode == null || mm == null || StartThisNXentryElement < 0 
            || EntryToDSs== null||   StartThisNXentryElement > EntryToDSs.size()
            || mm.getNodeClass()== null || !mm.getNodeClass().equals( "NXdata" ))
            	return;
      
      NxNode dat = mm.getChildNode( "data" );
      if( dat == null ) 
         return;

     Object O = dat.getAttrValue( "label" );
     String S = new NxData_Gen().cnvertoString( O );
     if( S == null || S.length()<1 ) 
        return;
     //There is a label now see if it is new or otherwise which entry it is in.
     
     int indx = -1;
     DataSetInfo dsInf =null;
     for( int i = StartThisNXentryElement; i < EntryToDSs.size() && indx < 0 ; i++ ){
       dsInf = (DataSetInfo)(EntryToDSs.elementAt( i ));
       if( dsInf.label != null&& dsInf.label.length()> 0 && dsInf.label.equals(S))
          indx = i;
     }
     
     int nGroups = 1;
     int[] dim = dat.getDimension();
     for( int ii = 0 ; ii + 1 < dim.length ; ii++ )
        nGroups *= dim[ ii ];
     
     
     if( indx < 0){// No entry for this label yet.
        dsInf = new DataSetInfo( entryNode, mm, -1, -1, S);
        EntryToDSs.addElement( dsInf);
     }
     if( dsInf == null)
        return;
     dsInf.nelts += nGroups;
     if( dsInf.NodeNames.length() > 0)
        dsInf.NodeNames +=";";
     dsInf.NodeNames += mm.getNodeName();
     dsInf.ndetectors = 0; // Will be done when NXdetector

      
   }

   private void AddUnMergedHistogramDataSets( NxNode nn , Vector EntryToDSs ) {

      // --------Now get DataSets that are not to be merged---------
      for( int j = 0 ; j < nn.getNChildNodes() ; j++ ) {

         NxNode mm = nn.getChildNode( j );
         if( mm.getNodeClass().equals( "NXdata" ) ) {

            NxNode dat = mm.getChildNode( "data" );
            if( dat != null ) {

               Object O = dat.getAttrValue( "label" );
               String S = new NxData_Gen().cnvertoString( O );
               if( ( O == null ) || ( S == null ) ) {
                  int[] dim = dat.getDimension();
                  int nGroups = 1;
                  for( int ii = 0 ; ii + 1 < dim.length ; ii++ )
                     nGroups *= dim[ ii ];
                  DataSetInfo DatInf = new DataSetInfo( nn , mm , - 1 , - 1 ,
                           null );
                  DatInf.nelts = nGroups;
                  EntryToDSs.addElement( DatInf );
               }
            }
         }// if NXdata node
      }

   }
   
   /**
    * Adds one DataSet to EntryToDSs corresponding to One NXdata
    * 
    * @param nn   The NXentry node
    * @param mm   The NXdata node. It will be checked
    */
   private void AddOneUnMergedHistogramDataSets( NxNode nn , NxNode mm ){
      if( nn== null || mm == null || EntryToDSs ==null|| 
           mm.getNodeClass() == null || !mm.getNodeClass().equals( "NXdata" ))
         return;

      NxNode dat = mm.getChildNode( "data" );
      if( dat != null ) {

         Object O = dat.getAttrValue( "label" );
         String S = new NxData_Gen().cnvertoString( O );
         if( ( O == null ) || ( S == null ) ) {
            int[] dim = dat.getDimension();
            int nGroups = 1;
            for( int ii = 0 ; ii + 1 < dim.length ; ii++ )
               nGroups *= dim[ ii ];
            DataSetInfo DatInf = new DataSetInfo( nn , mm , - 1 , - 1 ,
                     null );
            DatInf.nelts = nGroups;
            EntryToDSs.addElement( DatInf );
         }
      }
      
   }

   public void RecordGivenGroupDetIDs( NxNode nn , Vector EntryToDSs ,
            NxNode InstrumentNode ) {

      // Now get the associated NXdetector to get set ID's and Number of
      // detectors
      for( int kk = 0 ; kk < EntryToDSs.size() ; kk++ ) {
         DataSetInfo DatInf = (DataSetInfo) EntryToDSs.elementAt( kk );
         if( DatInf.NxentryNode == nn && DatInf.NxdataNode != null ) {
            int[] dat = GetMinMaxSetGroupDetectorIDs( DatInf.NxdataNode ,
                     InstrumentNode );
            DatInf.startGroupID = dat[ 0 ];
            DatInf.endGroupID = dat[ 1 ];
            DatInf.ndetectors = dat[ 2 ];
            DatInf.startDetectorID = dat[ 3 ];
            DatInf.endDetectorID = dat[ 4 ];
         }// 
         if( DatInf.label != null ) {// Now get the other data nodes
            String label = DatInf.label;
            for( int ii = 0 ; ii < nn.getNChildNodes() ; ii++ ) {

               if( nn.getChildNode( ii ).getNodeClass().equals( "NXdata" ) ) {
                  NxNode mm = nn.getChildNode( ii );
                  String L = NexUtils.getStringAttributeValue( nn
                           .getChildNode( "data" ) , "label" );
                  if( L != null && L.equals( label ) ) {
                     int[] Res = GetMinMaxSetGroupDetectorIDs( mm ,
                              InstrumentNode );
                     DatInf.startGroupID = update( DatInf.startGroupID ,
                              Res[ 0 ] , true );
                     DatInf.endGroupID = update( DatInf.endGroupID , Res[ 1 ] ,
                              false );
                     if( DatInf.ndetectors > 0 && Res[ 2 ] > 0 )
                        DatInf.ndetectors += Math.max( 0 , Res[ 2 ] );
                     else
                        DatInf.ndetectors = 1;
                     DatInf.startDetectorID = update( DatInf.startDetectorID ,
                              Res[ 3 ] , true );
                     DatInf.endDetectorID = update( DatInf.endDetectorID ,
                              Res[ 4 ] , false );
                  }
               }
            }// end search for all other nodes with same label

         }
      } // Setting up defined id's

   }

  /**
   *  Set defaults omitting those id's already set
   * @param nn    An NxEntry node
   * @param EntryToDSs  The Vector of dataSet information
   * TODO Id's should be different no just for one NXentry but for all in dataset
   *     omit nn
   */
   private void SetDefaultGroupDetIDs( NxNode nn , Vector EntryToDSs ) {

      //Record the set ones
      int[] Res = new int[ 2 ];
      Vector SetGroupIDRanges = new Vector();
      Vector SetDetectorIDRanges = new Vector();
      for( int im = 0 ; im < EntryToDSs.size() ; im++ ) {
         DataSetInfo DatInf = (DataSetInfo) EntryToDSs.elementAt( im );
         if( DatInf.NxentryNode == nn ) {
            if( DatInf.startGroupID >= 0 ) {
               Res[ 0 ] = DatInf.startGroupID;
               Res[ 1 ] = Math.max( DatInf.endGroupID , Res[ 0 ] + DatInf.nelts
                        - 1 );
               DatInf.endGroupID = Res[ 1 ];
               Insert( SetGroupIDRanges , Res );
               Res = new int[ 2 ];
            }
            if( DatInf.startDetectorID >= 0 ) {
               Res[ 0 ] = DatInf.startDetectorID;
               Res[ 1 ] = Math.max( DatInf.endDetectorID , Res[ 0 ]
                        + DatInf.ndetectors - 1 );
               Insert( SetDetectorIDRanges , Res );
               DatInf.endDetectorID = Res[ 1 ];
            }
         }
      }

      // Now set all the others as defaults
      int startGroupID = 1;
      int GroupElt = 0;
      int DetectorElt = 0;
      int startDetectorID = 1;
      for( int im = 0 ; im < EntryToDSs.size() ; im++ ) {
         DataSetInfo DatInf = (DataSetInfo) EntryToDSs.elementAt( im );
         if( DatInf.NxentryNode == nn ) {

            if( DatInf.startGroupID < 0 )
               if( GroupElt >= SetGroupIDRanges.size()
                        || startGroupID + DatInf.nelts - 1 < ( (int[]) SetGroupIDRanges
                                 .elementAt( GroupElt ) )[ 0 ] ) {
                  DatInf.startGroupID = startGroupID;
                  DatInf.endGroupID = startGroupID + DatInf.nelts - 1;
                  startGroupID = DatInf.endGroupID + 1;
               }
               else {
                  while( GroupElt < SetGroupIDRanges.size()
                           && startGroupID + DatInf.nelts - 1 >= ( (int[]) SetGroupIDRanges
                                    .elementAt( GroupElt ) )[ 0 ] )
                     startGroupID = ( (int[]) SetGroupIDRanges
                              .elementAt( GroupElt++ ) )[ 1 ] + 1;

                  DatInf.startGroupID = startGroupID;
                  DatInf.endGroupID = startGroupID + DatInf.nelts - 1;
                  startGroupID = DatInf.endGroupID + 1;

               }
            if( DatInf.startDetectorID < 0 )
               if( DetectorElt >= SetDetectorIDRanges.size()
                        || startDetectorID + DatInf.ndetectors - 1 < ( (int[]) SetDetectorIDRanges
                                 .elementAt( DetectorElt ) )[ 0 ] ) {
                  DatInf.startDetectorID = startDetectorID;
                  DatInf.endDetectorID = startDetectorID
                           + Math.max( 1 , DatInf.ndetectors ) - 1;
                  startDetectorID = DatInf.endDetectorID + 1;
               }
               else {
                  while( DetectorElt < SetDetectorIDRanges.size()
                           && startDetectorID + DatInf.ndetectors - 1 >= ( (int[]) SetDetectorIDRanges
                                    .elementAt( DetectorElt ) )[ 0 ] )
                     startDetectorID = ( (int[]) SetDetectorIDRanges
                              .elementAt( DetectorElt++ ) )[ 1 ] + 1;

                  DatInf.startDetectorID = startDetectorID;
                  DatInf.endDetectorID = startDetectorID
                           + Math.max( 1 , DatInf.ndetectors ) - 1;
                  startDetectorID = DatInf.endDetectorID + 1;


               }
         }
      }

   }

  
  
  
  /**
   * Returns any errormessage or "" if none
   */
  public String getErrorMessage(){
    return errormessage ;
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
      node = ( new NexIO.NexApi.NexNode(  filename ) ) ;
    }else
      System.exit( 0 ) ;
    if( node == null){
       System.exit(0);
       return;
    }
    ExtGetDS X = null ;
    if( node.getErrorMessage() == "" )
      X = new ExtGetDS( node , filename ) ;
    //trics00151999.hdf" ) ;
    //lrcs3000.nxs" ) ;
                       
    else{
      System.out.println( "Node create error =" + node.getErrorMessage() ) ;
      System.exit( 0 ) ;
    }
    if( X == null)
       return;
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
//---------------------- OBSOLETE-----------------------
  /**
    * Returns the DataSet or null if a fatal error occurs Obsolete
    */
 /*  private DataSet getDataSet1( int data_set_num ){
     if( !setupDSs ) 
       setUpDataSetList() ;

     if( (data_set_num <0) ||( data_set_num >= EntryToDSs.size())){
       DataSetTools.util.SharedData.addmsg("invalid data set number "
                                           +data_set_num);
       return  null;
     }
    
     NxNode nd2 ;
     nd2= (((DataSetInfo)(EntryToDSs.elementAt(data_set_num))).NxentryNode);
     AttributeList AL = getGlobalAttributes( nd2 ) ;
    
     String Analysis = getAnalysis( nd2 );
    
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
  //  Obsolete
   private boolean ProcessNxentryNode( NxNode nnode, DataSet DS, NxNode nxdata ){
     int  nchildren ;
     boolean res ;
     NXentry_TOFNDGS Entry ;
     errormessage = "" ;
   
     nchildren = nnode.getNChildNodes() ;
    
     if( nchildren < 0 ){
       errormessage = nnode.getErrorMessage() ;
       return false ;
     }
     //System.out.println( "start Find analysis" ) ;
     NxNode child = nnode.getChildNode( "analysis" ) ;
    
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
       Entry = new NXentry_TOFNDGS( nnode , DS ,nxdata) ;
       Entry.setNxData( new NxData_Gen() ) ;
       //DS.setAttribute( new IntAttribute( Attribute.INST_TYPE ,
       //                  InstrumentType.TOF_DG_SPECTROMETER ) ) ;      
     }else if( S.equals( "TOFNGS")){
       Entry = new NXentry_TOFNDGS( nnode , DS ,nxdata) ;
       Entry.setNxData( new NXdata_Fields("time_of_flight","phi","data"  ) ) ;
       Entry.monitorNames[0]="upstream";
       Entry.monitorNames[1]="downstream";
     }else{
       Entry = new NXentry_TOFNDGS( nnode , DS ,nxdata ) ;
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
*/

}
