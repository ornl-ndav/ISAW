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
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO ;

//import NdsSvNode ;
import java.io.* ;
import NexIO.NexApi.* ;
 import NexIO.NDS.* ;
import NexIO.* ;
import NexIO.NexApi.* ;
import DataSetTools.dataset.* ;
import DataSetTools.retriever.* ;
import DataSetTools.viewer.* ;
import IPNS.Runfile.* ;
import java.util.* ;

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
  Vector nEntries ,  
         nmonitors , 
         ndatasets ;

/**
*@param node   the NxNode used to communicate with the underlying datasource
*@param  filename  the filename
*/
  public ExtGetDS( NxNode node , String filename )
    {
      this.node = node ;
      this.filename = filename ;
      errormessage = "" ;
      nEntries = nmonitors = ndatasets = null ;
      }
/** Returns the type(  Histogram , Monitor , Invalid , etc. ) of the dataset
*@param data_set_num  the data set whose type is desired
*
*@see DataSetTools.retriever.Retriever.data_source_name
*/
 public int getType(  int data_set_num )
   {if( nEntries == null ) 
        setUpPointers() ;
  
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
   return Retriever.INVALID_DATA_SET ;
   }

/** Returns the DataSet or null if a fatal error occurs
*
*/
public DataSet getDataSet( int data_set_num )
   {if( nEntries == null ) 
          setUpPointers() ;
    int nxentry ,  
         offset ;
    boolean monitor ;
    int x , 
        p , 
        nhists ;
    x = 0 ;
    p = 0 ;
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
   AttributeList AL = getGlobalAttributes() ;
   DataSetFactory DSF = new DataSetFactory( "" ) ;
   DataSet DS = DSF.getDataSet() ;
   DS.setAttributeList( AL ) ;
   if( !ProcessNxentryNode( nd ,  DS ,  offset ) )
     {String S = "M" ;
      if( getType( data_set_num ) == ( Retriever.HISTOGRAM_DATA_SET ) )
          S = "H" ;
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
       DS.setTitle( S ) ;
       DS.setAttribute( new StringAttribute( Attribute.FILE_NAME , filename ) ) ;
       
       return DS ;
     }
    else
     return null ;
    
    }
private AttributeList getGlobalAttributes()
   {AttributeList Res = new AttributeList() ;
    Object X = node.getAttrValue( "file_name" ) ;
    NxData_Gen ndd = new NxData_Gen() ;
    if( X != null )
       {String S = ndd.cnvertoString( X ) ;
        Res.addAttribute( new StringAttribute(  Attribute.RUN_TITLE , S ) ) ;
       }
    return Res ;
   }

/** returns the number of datasets in this file
*/
 public int numDataSets( )
  { if( nEntries == null ) 
       setUpPointers() ;
    int x , 
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
  }

 private void setUpPointers()
  {int nmon ,  
       ndats ;
   nEntries = new Vector() ;
   nmonitors = new Vector() ;
   ndatasets = new Vector() ;   
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
       nmonitors.addElement( new Integer( nmon ) ) ;
       ndatasets.addElement( new Integer( ndats ) ) ;
       }
     }
   } 

/** Returns any errormessage or "" if none
*/
 public String getErrorMessage()
   {
     return errormessage ;
    }
 
 private boolean ProcessNxentryNode( NxNode node , DataSet DS , int index )
    {int i , 
        nchildren ;
     boolean res ;
     NXentry_TOFNDGS Entry ;
     errormessage = "" ;
     if( index < 0 ) 
       {
        errormessage = "improper index" + index ;
        return false ;
        }
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
           {Entry = new NXentry_TOFNDGS( node , DS ) ;
            DS.setAttribute( new IntAttribute( Attribute.INST_TYPE ,
                              InstrumentType.TOF_DG_SPECTROMETER ) ) ;      
           }
       else 
           {Entry = new NXentry_TOFNDGS( node , DS ) ;
            Entry.setNxData( new NxData_Gen(  ) ) ;
           }
       res = Entry.processDS( DS , index ) ;
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
              if( !ProcessNxentryNode( child , DS , ndatasets ) )
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
     char c = 0 ;
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
  NDSClient nds =null ;

   if( d == 's' )
   {nds = new NDSClient( "dmikk.mscs.uwstout.edu" , 6008 , 6081998 ) ;
    nds.connect() ;
    node = ( NxNode )( new NdsSvNode( filename , nds ) ) ;
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
   System.out.println( X.nEntries.size() + "," + 
                   X.nmonitors.size() + "," + X.ndatasets.size() ) ;
   for( int i = 0 ; i < X.nEntries.size() ;i ++  )
      System.out.println( X.nEntries.elementAt( i )+ "," + 
                         X.nmonitors.elementAt( i ) + "," + 
                         X.ndatasets.elementAt( i ) ) ;
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
