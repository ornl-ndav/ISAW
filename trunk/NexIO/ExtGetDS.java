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
 * Revision 1.6  2001/09/20 17:36:44  dennis
 * Fixed @see javadoc comment
 *
 * Revision 1.5  2001/08/01 14:36:23  rmikk
 * Isaw's instrument type is Now tied to the NXentry
 * analysis field
 *
 * Revision 1.4  2001/07/26 13:52:34  rmikk
 * Removed Dependence on NDS package
 *
 * Revision 1.3  2001/07/24 20:01:41  rmikk
 * Major Reorganization to allow several NXdata's for one
 * DataSet ( Group those with like x values)
 *
 * Revision 1.2  2001/07/17 13:52:50  rmikk
 * Added Changes to get more fields
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
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
/**  Class that retrieves DataSets from sources that NxNode interface
* <ol>
*<li> Satisfy the NxNode interface
*<li> The node class structure and return types satisfy the Nexus Standard
*</ol> 
* This class has been used with a Nexus API module( NexIO.NexApi ) and
* a nexus dataserver module( NexIO.NDS ). Future plans are to extend this to
* retrieve XML documents
*/
public class ExtGetDS //extends Retriever 
 {NxNode node ;
  String filename ;
  String errormessage ;
  Vector EntryDSs;
  boolean setupDSs;
  boolean debug=false;
/**
*@param node   the NxNode used to communicate with the underlying datasource
*@param  filename  the filename
*/
  public ExtGetDS( NxNode node , String filename )
    {
      this.node = node ;
      this.filename = filename ;
      errormessage = "" ;
      EntryDSs= new Vector() ;
      }
/** Returns the type(  Histogram , Monitor , Invalid , etc. ) of the dataset
*@param data_set_num  the data set whose type is desired
*
*@see DataSetTools.retriever.dataSource
*/
 public int getType(  int data_set_num )
   {if(!setupDSs ) 
        setUpPointers() ;
   /* if( debug) System.out.println("in getType for "+data_set_num);
    int x , 
        p ;
    x = 0 ;
    p = 0 ;
    while( p< nEntries.size() )
     {
      x = x + ( ( Integer )nmonitors.elementAt( p ) ).intValue() ;
      if( data_set_num < x ) 
          return Retriever.MONITOR_DATA_SET ;
      x = x + ( ( Integer )ndatasets.elementAt( p ) ).intValue() ;
      if( data_set_num < x ) 
           return Retriever.HISTOGRAM_DATA_SET ;
      p++  ;
     }
   */
   if( data_set_num < 0)
     return Retriever.INVALID_DATA_SET ;
   if( data_set_num >= EntryDSs.size())
      return Retriever.INVALID_DATA_SET ;
   Vector VV =(Vector)(EntryDSs.elementAt(data_set_num));
   NxNode nd= (NxNode)(VV.lastElement());
   if(nd.getNodeClass().equals("NXmonitor"))
          return Retriever.MONITOR_DATA_SET ;
   else
           return Retriever.HISTOGRAM_DATA_SET ;
   }

/** Returns the DataSet or null if a fatal error occurs
*
*/
public DataSet getDataSet( int data_set_num )
   {if( !setupDSs ) 
          setUpPointers() ;
   /* int nxentry ,  
         offset ;
    boolean monitor ;
    int x , 
        p , 
        nhists ;
    x = 0 ;
    p = 0 ;
    if(debug) System.out.println("in getDataSet "+data_set_num);
    nhists =0 ;
    nxentry = -1 ;
    monitor =  false ;
    offset = -1 ;
    int x1 ;
    while( ( p< nEntries.size()&&( nxentry < 0 ) ) )
     {int u = ( ( Integer )nmonitors.elementAt( p ) ).intValue() ;
      x1 = x ;
      if( x + u <= data_set_num )
        { x = x + u ;
          u = ( ( Integer )ndatasets.elementAt( p ) ).intValue() ;
          if( x + u<= data_set_num )
            { x = x + u ;
	      nhists  += u ;
            }
          else
             {monitor = false ;
               nxentry = p ;               
               offset = data_set_num-x1 ;
               nhists += offset ;
              }
          }
       else
        {monitor = true ;
         nxentry = p ;
         offset = data_set_num - x ; 
        }
     
      p++  ;
    
     }
    if( ( nxentry < 0 ) ||(  offset < 0 ) )
      {errormessage = "Improper Dataset number" + data_set_num ;
       return null ;
      } 
   
   NxNode nd = node.getChildNode( 
                    ( String )( nEntries.elementAt( nxentry ) ) ) ;
   */
   if( (data_set_num <0) ||( data_set_num >= EntryDSs.size()))
      {DataSetTools.util.SharedData.status_pane.add("invalid data set number "+data_set_num);
       return  null;
       }

   NxNode nd2 ;
   nd2= (NxNode)(((Vector)(EntryDSs.elementAt(data_set_num))).firstElement());
   AttributeList AL = getGlobalAttributes() ;
  
   String Analysis = getAnalysis( nd2 );
   
   Inst_Type it = new Inst_Type();
   int instrType = (new Inst_Type()).getIsawInstrNum( Analysis );
  
   /*if( (Analysis == null) ||(Analysis =="")|| 
       (instrType ==InstrumentType.UNKNOWN))
     {

        NxNode n2 = nd2.getChildNode( "analysis");
        if( n2 != null)            
          { Object OO = n2.getAttrValue( "isaw_instr_type");
	    if( OO instanceof int[])
              if( Array.getLength( OO) ==1)
               instrType = ((int[])OO)[0];
           
          }      
         }
   */
   DataSetFactory DSF = new DataSetFactory( "" ) ;
   DataSet DS = DSF.getTofDataSet(instrType) ;   
   DS.setAttributeList( AL ) ;

   int list[] = new int[1];
   list[0] = instrType;
   DS.setAttribute( new IntListAttribute( Attribute.INST_TYPE, list ));

   NxNode nDS = (NxNode)(((Vector)(EntryDSs.elementAt(data_set_num))).lastElement());
   if( nDS.getNodeClass().equals("NXmonitor"))
      DS.setAttribute( new StringAttribute(Attribute.DS_TYPE, Attribute.MONITOR_DATA));
   else if( isPulseHeight( nDS.getNodeName()))
      DS.setAttribute( new StringAttribute(Attribute.DS_TYPE, Attribute.PULSE_HEIGHT_DATA));
   else
      DS.setAttribute( new StringAttribute(Attribute.DS_TYPE, Attribute.SAMPLE_DATA));
      
   if( !ProcessNxentryNode( nd2 ,  DS , nDS ) )
     {/*String S = "M" ;
      if( getType( data_set_num ) == ( Retriever.HISTOGRAM_DATA_SET ) )
          S = "H" ;
      else nhists++;
       S = S + nhists ;
       String F = filename ;
       int k = filename.lastIndexOf( "." ) ;
       if( k>0 )
          F = filename.substring( 0 , k ) ;
       F = F.replace( '\\' , '/' ) ;
       k = F.lastIndexOf(  '/' ) ;
       if( k>= 0 )
         F = F.substring( k + 1 ) ;
       S = S + "_" + F ;
       */
       String S = nDS.getNodeName();
       DS.setTitle( S ) ;
      
       DS.setAttribute( new StringAttribute( Attribute.FILE_NAME , filename ) ) ;
       if( nDS.getNodeClass().equals("NXmonitor"))
          { //ReOrderGroups(DS);
            
          }
       
          
     }
    else
     { DataSetTools.util.SharedData.status_pane.add( "ERROR:"+errormessage+" ds num="
              +data_set_num);
      return DS; 
      }
     return DS;
    }

private boolean isPulseHeight( String S)
  { int i1=S.toUpperCase().indexOf("PULSE");
  
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
//For monitor data sets, the groups with the largest phi is 1
private void ReOrderGroups( DataSet DS)
 {/* boolean done=false;
   Data sav;
   while(!done)
    { done=true;
      for(int i=0;i+1<DS.getNum_entries();i++)
       { Data D1= DS.getData_entry(i);
         Data D2=DS.getData_entry(i+1);
         if( D1.getX_scale().getXs().length>D2.getX_scale().getXs().length)
          {done=false;
           sav=D1;
           D1=D2;
           D2=sav;
          }
         else 
          {DetPosAttribute A1,A2;
           A1=(DetPosAttribute)D1.getAttribute(Attribute.DETECTOR_POS);
           A2=(DetPosAttribute)D1.getAttribute(Attribute.DETECTOR_POS);
           if( (A1 !=null) &&(A2 !=null))
            if( java.lang.Math.abs(((DetectorPosition)A1.getValue()).getSphericalCoords()[2] ) >
                java.lang.Math.abs(((DetectorPosition)A1.getValue()).getSphericalCoords()[2] ))
              {done = false;
               sav= D2;
               D2=D1;
               D1=sav;
              }
               
  
          }
        }
     }
   */
 }
private AttributeList getGlobalAttributes()
   {AttributeList Res = new AttributeList() ;
   //Object X = node.getAttrValue( "file_name" ) ;
   // NxData_Gen ndd = new NxData_Gen() ;
   // if( X != null )
     {//String S = ndd.cnvertoString( X ) ;
        Res.addAttribute( new StringAttribute(  Attribute.FILE_NAME , filename ) ) ;
       }
    return Res ;
    
   }
private String getAnalysis( NxNode node)
  {if( node == null) return "";
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
/** returns the number of datasets in this file
*/
 public int numDataSets( )
  { if(!setupDSs) 
       setUpPointers() ;
   /* System.out.println("#data sets="+EntryDSs.size());
    for(int i=0;i<EntryDSs.size();i++)
      { Vector V;
        V=(Vector)(EntryDSs.elementAt(i));
         NxNode nd= (NxNode)(V.firstElement());
         System.out.println(nd.show());
         System.out.println("                 -------------------   ");
          V=(Vector)(EntryDSs.elementAt(i));
          nd= (NxNode)(V.lastElement());
         System.out.println(nd.show());
         System.out.println("==========================");
      
       }
     */
    int u = EntryDSs.size();
    
    if( u >=0)
      return EntryDSs.size();
    else
      return 0;
    /*int x , 
        p ;
    x = 0 ; 
    p = 0 ;
    while( p< nEntries.size() )
     {
      x = x + ( ( Integer )nmonitors.elementAt( p ) ).intValue() ;
      
      x = x + ( ( Integer )ndatasets.elementAt( p ) ).intValue() ;
    
      p++ ;
     }
   return  x ;
   */
  }

 private void setUpPointers()
  {String labels=";";
   setupDSs = true;
   for( int i = 0; i<node.getNChildNodes();i++)
     { NxNode nn=node.getChildNode(i);
       if( nn.getNodeClass().equals( "NXentry" ) )
        {boolean monitor=false;
         for( int j = 0 ; j < nn.getNChildNodes() ; j++ )
          {NxNode mm = nn.getChildNode( j ) ;
           if( mm.getNodeClass().equals( "NXmonitor" ) )
              { 
               if(!monitor)
               {Vector V = new Vector();
               V.addElement(nn); V.addElement(mm);
               EntryDSs.addElement( V);
               monitor=true;
                } 
               }
           else if( mm.getNodeClass().equals( "NXdata" ) )
               { NxNode dat=mm.getChildNode("data");
                 if(dat != null)
                   { Object O= dat.getAttrValue("label");
                     String S = new NxData_Gen().cnvertoString(O);
                     Vector V = new Vector();
                      V.addElement(nn); 
                     if( (O == null)||(S ==null))
                       V.addElement(mm);
                     else if( labels.indexOf(";"+S+";") <0)
                       {V.addElement(mm);
                        labels+=S+";";
                       }
                     if( V.size() ==2)
                        EntryDSs.addElement(V);
                     }//if dat != null
                else
                  {Vector V=new Vector();
                   V.addElement( nn);
                   V.addElement( mm);
                   EntryDSs.addElement(V);

                   }


                }//if NXdata node
          }//for  NxEntries children
         }//if( node class == NXentry
     }

  /*int nmon ,  
       ndats ;
   nEntries = new Vector() ;
   nmonitors = new Vector() ;
   ndatasets = new Vector() ;  
   nmon=ndats=0; 
   for( int i = 0 ; i < node.getNChildNodes() ; i++ )
     {NxNode nn = node.getChildNode( i ) ;
      if( nn.getNodeClass().equals( "NXentry" ) )
       {nEntries.addElement( nn.getNodeName() ) ;
        NxNode mm ;
        nmon = 0 ;
        ndats = 0 ;
        for( int j = 0 ; j < nn.getNChildNodes() ; j++ )
          {mm = nn.getChildNode( j ) ;
           if( mm.getNodeClass().equals( "NXmonitor" ) )
               nmon++  ;
           if( mm.getNodeClass().equals( "NXdata" ) )
              ndats++  ;
          }
	if( nmon > 0 )
            nmon = 1 ;
        if( ndats > 0)
            ndats = 1;
       nmonitors.addElement( new Integer( nmon ) ) ;
       ndatasets.addElement( new Integer( ndats ) ) ;
       }
     }
    if(debug) System.out.println("nmonitors, ndatasets="+nmon+","+ndats);
   */
   } 

/** Returns any errormessage or "" if none
*/
 public String getErrorMessage()
   {
     return errormessage ;
    }
 
 private boolean ProcessNxentryNode( NxNode node , DataSet DS ,  NxNode nxdata )
    {int i , 
        nchildren ;
     boolean res ;
     NXentry_TOFNDGS Entry ;
     errormessage = "" ;
   /*if( index < 0 ) 
       {
        errormessage = "improper index" + index ;
        return false ;
        }*/
     nchildren = node.getNChildNodes() ;
    
     if( nchildren < 0 )
        {
         errormessage = node.getErrorMessage() ;
         return false ;
        }
     //System.out.println( "start Find analysis" ) ;
      NxNode child = node.getChildNode( "analysis" ) ;

      Object X ;
      if( child == null ) X = null ;
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
     
 
       if( S.equals( "TOFNDGS" ) )
           {Entry = new NXentry_TOFNDGS( node , DS ,nxdata) ;
            Entry.setNxData( new NxData_Gen() ) ;
	   //DS.setAttribute( new IntAttribute( Attribute.INST_TYPE ,
	   //                  InstrumentType.TOF_DG_SPECTROMETER ) ) ;      
           }
       else if( S.equals( "TOFNGS"))
          {Entry = new NXentry_TOFNDGS( node , DS ,nxdata) ;
           Entry.setNxData( new NXdata_Fields("time_of_flight","phi","data"  ) ) ;
           Entry.monitorNames[0]="upstream";
           Entry.monitorNames[1]="downstream";
           }
      
       else
           {Entry = new NXentry_TOFNDGS( node , DS ,nxdata ) ;
            Entry.setNxData( new NxData_Gen() ) ;
           }
       if(debug)System.out.println("Start Process NXEntry");
       res = Entry.processDS(  DS , nxdata ) ;
       if( Entry.getErrorMessage()!= "" )
	   errormessage  += ";" + Entry.getErrorMessage() ;
       if( !res )
            errormessage = Entry.getErrorMessage() ;
        return res ;      
      
     }
 // Deprecated
private DataSet[] retrieveDS()
   {int nchildren , i ; 
    Vector V = new Vector() ;
    NxNode child ;
    if( node == null ) 
          return null ;
    nchildren = node.getNChildNodes() ;
    //System.out.println( "retriever #children = " + nchildren ) ;
    
    for( i = 0 ; i < nchildren ; i++  )
      {child = node.getChildNode( i ) ;
       int ndatasets = 0 ;
       //if( child!= null ) System.out.println( "NXentry child = " + child.show() ) ;
       if( child!= null )
         if( child.getNodeClass().equals( "NXentry" ) )
            { 
              DataSet DS = new DataSet( "","" ) ;
              if( !ProcessNxentryNode( child , DS , null))//ndatasets ) )
		  {//System.out.println( "ProcessNxEntry error " + errormessage ) ;
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
  
/** Test program for the module ExtGetDS
*/
public static void main( String args[] )
  {
      System.out.println( "Enter Option desired" ) ;
      System.out.println( "   s: from nds server" ) ;
      System.out.println( "   a. from Nexus API-local file" ) ;
     char c = 'a' ;
    try
     { 
      while ( c<=  32 )
         c = ( char ) System.in.read() ;
     }
    catch( IOException s )
      {System.out.println( "IOExceptio =" + s ) ;
       System.exit( 0 ) ;
      }
    char d = c ;
   System.out.println( "now enter filename" ) ;
   String filename = "" ;
     try
      {c = 0 ;
      while( c <= 32 )
          c = ( char ) System.in.read() ;
      while(  c> 32 )
        {filename = filename + c ;
          c = ( char ) System.in.read() ;
        }
      }
   catch( IOException s )
     {System.out.println( "Cannot get filename " + s ) ;
     System.exit( 0 ) ;
     }
   System.out.println( "filename = " + filename + "::command= " + d ) ;
  
  NxNode node = null ;
  // NDSClient nds =null ;

   if( d == 's' )
   {/*nds = new NDSClient( "dmikk.mscs.uwstout.edu" , 6008 , 6081998 ) ;
    nds.connect() ;
    node = ( NxNode )( new NdsSvNode( filename , nds ) ) ;
    */
   }
  else if( d == 'a' )
    { node = ( NxNode )( new NexIO.NexApi.NexNode(  filename ) ) ;
    }
  else
    System.exit( 0 ) ;
  
   ExtGetDS X = null ;
   if( node.getErrorMessage() == "" )
        X = new ExtGetDS( node , filename ) ;
           //trics00151999.hdf" ) ;
           //lrcs3000.nxs" ) ;
                       
   else
     {System.out.println( "Node create error =" + node.getErrorMessage() ) ;
      System.exit( 0 ) ;
     }
   System.out.println( "num data sets = " + X.numDataSets() ) ;
  // System.out.println( X.nEntries.size() + "," + 
  //                 X.nmonitors.size() + "," + X.ndatasets.size() ) ;
   
   for( int i = 0 ; i < X.numDataSets() ; i++  )
     {System.out.println( "Typexx = " + i ) ;
      DataSet DS = X.getDataSet( i ) ;
      if( DS == null )
	  System.out.println( "DS cReate ERROR = " +  X.getErrorMessage() ) ;
       else  DataSet_IO.SaveDataSet( DS , 
               "C:\\Test" + new Integer( i ).toString().trim() + 
			      ".isd" ) ;
      }
   

 }//end main

}

