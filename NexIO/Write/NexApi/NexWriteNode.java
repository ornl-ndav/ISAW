/*
 * File:  NexWriteNode.java 
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
 * Revision 1.16  2007/08/26 23:56:18  rmikk
 * Changed package name for the NeXus package
 *
 * Revision 1.15  2005/04/20 22:03:29  dennis
 * Temporarily modified canWrite(filename) method just return true.
 * Should still check to see if this method is really still needed
 * to work around problems with jnexus.  For now just return true,
 * since NeXus files were not being found if not fully qualified
 * with the path.
 *
 * Revision 1.14  2005/02/12 17:18:18  rmikk
 * Use CNexusFile class instead of its super class
 * Linearize multidimensional arrays instead of letting jnexus do it. Fixes an
 *    error if one of the dimensions of an array is 1
 *
 * Revision 1.13  2004/05/14 15:04:05  rmikk
 * Removed unused variables
 *
 * Revision 1.12  2004/03/15 03:36:03  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.11  2004/03/11 19:46:32  bouzekc
 * Removed java.lang import as this is included by default.
 *
 * Revision 1.10  2004/01/26 17:10:11  rmikk
 * Files are now written HDF5
 * The data is now compressed using the LZW compression
 *    algorithm
 *
 * Revision 1.9  2003/11/24 14:21:08  rmikk
 * Eliminated some commented out code
 * Added getNodeName method
 * Eliminated some debugging printing
 *
 * Revision 1.8  2003/10/15 02:37:50  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.7  2003/06/18 20:34:23  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.6  2002/11/27 23:29:29  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/11/20 16:15:53  pfpeterson
 * reformating
 *
 */
 

package NexIO.Write.NexApi;
//import neutron.nexus.*;
import org.nexusformat.*;
import NexIO.Write.*;
import NexIO.*;
import gov.anl.ipns.Util.Sys.StringUtil;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

/**
 * Class that handles writing information to a Nexus file using the
 * Nexus Api<br><br>
 *
 * NOTE: An internal representation is saved an only sent to a nexus
 * file when the write routine is executed.
 */
public class  NexWriteNode implements NexIO.Write.NxWriteNode{
  protected String errormessage;
  CNexusFile nf;
  String filename;
  Vector children;
  Vector attributes;
  String classname;
  String nodename;
  boolean Debug = false;
  Hashtable linkInfo;
  String LinkedName = null;//Not null means already recorded some other place
  public NexWriteNode parent;
  Object value;
  int ranks[] , type;
  boolean written;
  boolean childrenAdded;
  boolean attributesAdded;
  int num_nxEntries;
  NxFileOpenThread nxf;
  public static Vector<NexWriteNode> PosWriter = null;
  NexWriteNode TheEntryNode = null;
  static int SlabSize = -1;

  /**
   * @param filename the name of a file written in the Nexus API
   * format
   *
   * NOTE: This checks that the file can be successfully created
   */
  public NexWriteNode( String filename ){
    errormessage = "";
    nf = null;
    if( PosWriter == null)
       PosWriter = new Vector<NexWriteNode>();
    PosWriter.add( this );
    childrenAdded = attributesAdded = false;
    int open_mode = NexusFile.NXACC_CREATE5;
    num_nxEntries = 0;     
    if( filename == null){
      errormessage = " no Such File "+filename;
      return;
    }    
    if( new File( filename).exists()){
      open_mode = NexusFile.NXACC_RDWR;       
      num_nxEntries = -1;
    }else if( !canWrite( filename )){
      errormessage= "CANNOT WRITE";
      System.out.println(filename+" cannot write");
      return;
    }    
    try{
       nxf = new NxFileOpenThread( filename,open_mode);
       nxf.start(); 
       try{
          nxf.join(1500);
         
       }catch(Exception s){
          errormessage =""+s;
          nf = null;
       }  
      nf = nxf.getNxFile();
      nf.setIOThread( nxf );
      if( open_mode == NexusFile.NXACC_RDWR ){
        Hashtable HT = nf.groupdir();
        Enumeration E = HT.elements();
        num_nxEntries =0;
        Object X =HT.get("entry");
        if(X != null &&  "NXentry".equals(X)){
           TheEntryNode =(NexWriteNode) newChildNode("entry","NXentry");
           PosWriter.add(TheEntryNode);
        }
        
        if( E != null)
          for( ; E.hasMoreElements(); ){
            X = E.nextElement();
            if( X instanceof String)
              if( X != null)
                if( "NXentry".equals( X ))
                  num_nxEntries++;           
          }
      }     
    }catch( NexusException s ){
      errormessage = NxNodeUtils.ER_BADFILE ;      
      if( nf != null)
        try{
          nf.finalize();
        }catch( Throwable u){
          // let it drop on the floor
        }
      nf = null;
    }
     
    this.filename = filename;
    children = new Vector();
    attributes = new Vector();
    classname = "File";
    nodename = "File";
    written = false;
    value = ranks = null;
    type = -1;
    linkInfo = new Hashtable();
    parent = null;
    errormessage = "";
    try{
       SlabSize = Integer.parseInt( System.getProperty( "NexusSlabSize" ,"80000"));
    }catch(Exception s){
       SlabSize = 80000;
    }
    
  }
  String nodeName = null;
  public String getNodeName(){
     return nodename;
  }
  public void setNodeName( String nodeName){
     this.nodeName = nodeName;
  }

  private boolean canWrite( String fileName ){
    //
    //System.out.println("canWrite returning true for " + fileName );
    //
    // TO DO.  Check if this method is really still needed to work around
    // problems with jnexus.  For now just return true, since NeXus files 
    // were not found if not fully qualified with the path.
    return true;
/*
    String F = fileName.replace('\\','/');
    int k = F.lastIndexOf('/');   
    if( k < 0 )
      return false;
    String Path = F.substring( 0, k);    
    if (!new File(Path).isDirectory())
      return false;     
    int j= F.lastIndexOf( '.' );
    if( j< 0) j = F.length();
    String Name = F.substring( k+1, j);
    String Extension ="";
    if( j < F.length())
      Extension = F.substring(j+1);
    
    for( int i=0; i < Name.length(); i++){
      char c = ( Name.charAt( i ) ) ;       
      if( Character.isLetter( c )){
        // do nothing
      }else if( Character.isDigit(c) && i > 0){
        // do nothing
      }else
        return false;
    }
  
    for( int i=0; i<Extension.length(); i++){
      char c = ( Extension.charAt( i ) ) ;       
      if( Character.isLetter( c )){
        // do nothing
      }else if( Character.isDigit( c ) && i > 0){
        // do nothing
      }else if( c=='_' && i>0){
        // do nothing
      }else
        return false;
    }
    return true;
*/
  }


  /**
   * Returns the number of NXentries in this file
   *
   * @param className Should be "NXentry"
   * @return the number of NXentries in this file<br>
   */ 
  public int getNumClasses( String className){
    if ( className.equals( "NXentry"))
      return num_nxEntries;
    return -1;
  }

  /**
   * Creates a new child node and makes in a child of the current node
   *
   * @param  node_name  the name used to refer to this node
   * @param  node_class  the classname of this node <br>
   *
   * Note: the node_class should be a Nexus class like NXentry,
   * NXdata,etc.
   */
  public NxWriteNode newChildNode(  String node_name , String node_class ){
     
    errormessage = "";
    if( classname.equals("File")&& nodename.equals("File")&&
          node_name.equals("entry")&& node_class.equals("NXentry"))
       if( TheEntryNode != null)
       return TheEntryNode;
    
    NexWriteNode nw = new NexWriteNode( filename , nf , linkInfo , this );
    nw.nodename = new String( node_name );
    nw.classname = new String( node_class );   
    addChildNode( nw );
    if( classname.equals("File"))
      if(node_class.equals("NXentry"))
        num_nxEntries++;
    childrenAdded= true;
    return nw;   
  }
   
  private NexWriteNode( String filename , CNexusFile nf , Hashtable linkInfo , 
                        NexWriteNode parent ){
    if( nf == null ) {
      errormessage = "Not initialized yes";
      return;
    }
    errormessage = "";
    this.filename = filename;
    this.nf = nf;
    children = new Vector();
    attributes = new Vector();
    this.linkInfo = linkInfo;
    classname = nodename =  null;
    written = false;
    value = ranks = null;
    type = -1;
    this.parent = parent;
    childrenAdded = attributesAdded= false;
  }
  
  /**
   * returns error message or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Returns the number of NXentries in this file
   */
  public int getNumNXentries(){
    if( nf == null ){
      errormessage = "Not initialized yes";
      return -1;
    }
    return num_nxEntries;
  }

  private void addChildNode( NexWriteNode x ){
    if( nf == null ){
      errormessage = "Not initialized yes";
      return;
    }
    errormessage = "";
    children.addElement( x );
  }
  
  /**
   * Sets this node's value. If an array Value will be linear This
   * routine will convert to the correct type and dimensions
   *
   * @param Value The value of the node.
   * @param type The NexIO type for this value
   * @param ranks the length of rank is the number of dimensions and
   * rank[i] is size of the array for dimension i.
   */
  public void setNodeValue( Object Value , int type , int ranks[] ){
    if( nf == null ){
      errormessage = "Not initialized yes";
      return;
    }
    errormessage = "";
    value = Value;
    this.type = type;
    int rank1[];
    rank1 = new int[ ranks.length ];
    System.arraycopy( ranks , 0 , rank1 , 0 , ranks.length );
    this.ranks = rank1;
  }

  /**
   * Adds an attribute to this node
   *
   * @param AttrName the name of this attribute
   * @param AttrValue the value of this attribute
   * @param Type the type in NexIO.Types.java for this attribute
   * @param Ranks the length of rank is the number of dimensions and
   * rank[i] is size of the array for dimension i.
   *
   * @see NexIO.Types#Int Type names
   */
  public void addAttribute(String AttrName, Object AttrValue, int Type,
                           int Ranks[] ){
    if( nf == null ){
      errormessage = "Not initialized yes";
      return;
    }
    errormessage = "";
    Object AttrValue1 = AttrValue;
    int rank1[];
    rank1 = new int[ Ranks.length ];
    System.arraycopy( Ranks , 0 ,  rank1 , 0 , Ranks.length );
    Vector V = new Vector( );
    V.addElement( new String( AttrName ) );
    V.addElement( AttrValue1 );
    V.addElement( new Integer( Type ) );
    V.addElement( rank1 );
    attributes.addElement( V );
    attributesAdded = true;
  }

  //------------------ Links ----------------------------
  
  /**
   * Adds a link to information as a child to this node
   *
   * @param linkhandle the name used to refer to this linked
   * 
   * @param sourceNode the name with the data source
   * information
   */
  public void addLink( String linkhandle, NxWriteNode sourceNode ){
    if( nf == null ){
      errormessage = "Not initialized yes";
      return;
    }
    errormessage = "";
    children.addElement( linkhandle );
    linkInfo.put( linkhandle, sourceNode);
  }

  /**
   * Set a name to refer to a piece of linked data
   * @param handleName the name that will be used to refer to this
   * linked information
   */
  public void setLinkHandle( String handleName ){
    errormessage = "";
    if( nf == null ){
      errormessage = "Not initialized yes";
      return;
    }
    //need to open
    Vector V = new Vector();
    V.addElement( handleName );
    V.addElement( this );
    children.addElement( handleName );
    linkInfo.put( handleName, this);
    //children.addElement( handleName );
  }
  
  private boolean Position( NexWriteNode to){
     Vector<NexWriteNode> V = new Vector<NexWriteNode>();
     V.add( this);
     for( NexWriteNode par = parent; par != null; par =par.parent){
        V.insertElementAt( par,0);
     }
     boolean done = false;
     int i;
     for( i=0; i < Math.min( V.size(), PosWriter.size()) && !done; i++)
        if( V.elementAt(i)!= PosWriter.elementAt(i))
           done = true;
     
     for( int k= PosWriter.size()-1; k>=i; k--)
       try{
        NexWriteNode elt = PosWriter.elementAt(k);
        if( elt.classname.equals("SDS"))
           nf.closedata();
        else if( !elt.classname.equals("File"))
           nf.closegroup();
        PosWriter.remove(PosWriter.size()-1);
      }catch(Exception s1){
         errormessage+="Nexus file out of kilter;";
         return true;
      }
      
    
     NexWriteNode elt = V.elementAt(i-1);
     Hashtable tab = null;
     try{
     
     tab= nf.groupdir();
     }catch(Exception s3){
        errormessage +="NexusFile out ofkilter;";
        tab = new Hashtable();
     }
     if( i <=0)
        errormessage +="Nexus file out of kilter;";
     
     
     for( int k=i; k+1< V.size(); k++){
        elt = V.elementAt(k);
        try{
           boolean exists = tab.containsKey(elt.nodename);
           tab = new Hashtable();
           if( elt.classname.equals("SDS")){
              if(exists)
                 nf.opendata(elt.nodename);
              else{
                
                 nf.makedata(elt.nodename, elt.type,elt.ranks.length,
                       elt.ranks);
                 nf.opendata( elt.nodename);
              }
              
           }else if( !elt.classname.equals("File")){
              if( exists){
                 nf.opengroup(elt.nodename,elt.classname);
                 tab = nf.groupdir();
              } else{
                 nf.makegroup( elt.nodename,elt.classname);
                 nf.opengroup(elt.nodename,elt.classname);
              }
              
           }
          if(! elt.classname.equals("File"))
           PosWriter.addElement( elt);
           
        }catch(Exception s1){
           errormessage+="Nexus file out of kilter;";
           return true;
           
        }
     }
     return false;
  }

//------------------ Saving --------------------------

  /**
   * Writes node to file if it can. The node cannot have attributes,
   * children, or links incorporated after this method is executed.
   * assumes current node is opened
   */
  public void write(){
    
    
    if(Position ( this))
       return;
    Hashtable Kids = null;
    try{
       Kids = nf.groupdir();
    }catch(Exception s3){
       Kids = new Hashtable();
    }
   
    boolean closed = false;
    if( nf == null ){
      errormessage ="file Not created";
      return;
    }
    if( Debug)System.out.println( "Writing"+ nodename+","+classname );
    if( classname.equals( "File" ) )
      errormessage = "";
    if( errormessage != "" )
      return;
    Object R;
    if (written && !(classname.equals("File")))
         if (LinkedName != null) {
            R = linkInfo.get(LinkedName);
            if (R != null && R instanceof NXlink)
               try{
               nf.makelink((NXlink) R);
               written = true;
               return;
              }catch(Exception ss){
                 errormessage += ss.toString()+" in makeLink for "+ nodename+"("+
                     classname+"):";
                 return;
              }
         } else {
            //errormessage = "already written;";
            return;
         }
     
    //close all first
    if( !written )
      try{
        if( LinkedName != null){
          R = linkInfo.get(LinkedName);
          if( R != null && R instanceof NXlink){
             nf.makelink( (NXlink)R);
             written = true;
             return;
          }
        }
        if( Debug)
          System.out.println( "C"+classname+classname.equals( "SDS" ) );
        if( classname.equals( "SDS" ) ){
          if( Debug ) System.out.print( "in SDS"+type );
          if( ranks ==  null ){
            if( Debug) System.out.println( "null ranks" );}
          else if( Debug) 
            System.out.println("ranks leng 1st val = "+ranks.length+","
                               +ranks[0]);
          
          if( (value == null ) ||( ranks == null )  ){
            if( Debug)
              System.out.println( "END null Writing"+ nodename+","+classname );
            return;
          }     
          ranks = fixRankArray( ranks );
          if(! Kids.containsKey(nodename)){
             if( Okay2Compress(type ,ranks))
                 nf.compmakedata(nodename,TypeConv.convertFrom(type),ranks.length,ranks,
                      NexusFile.NX_COMP_LZW, ranks);
             else nf.makedata(nodename,TypeConv.convertFrom(type),ranks.length,ranks);
          }
         
          nf.opendata( nodename );
          PosWriter.add(this);
           //nf.compress( NexusFile.NX_COMP_LZW);
          
        }else if(( ! classname.equals( "File" ) ) ){
          if(! Kids.containsKey(nodename))
             nf.makegroup( nodename , classname );
          nf.opengroup( nodename , classname );
          PosWriter.add(this);
        }
      }catch( NexusException s ){
        errormessage += "NexExceptionA:"+s.getMessage( )+ "in "+ nodename+"("+
              classname+"):";
        if( Debug) System.out.println( errormessage );
        try{
          if( classname.equals( "SDS" ) ){
            nf.closedata();
          }else if( !classname.equals( "File" ) ){
            nf.closegroup();
          }
          PosWriter.remove(PosWriter.size()-1);
          closed = true;
        }catch( Exception s3 ){
          // let it drop on the floor
        }
        return;
     }else{
        try{
        if( classname.equals("SDS")){
          nf.opendata(nodename);
          
        } else if( !classname.equals("File")){
           nf.opengroup( nodename, classname);
        }
        PosWriter.add( this );
        }catch(Exception ss){
           errormessage +="cannot open node in "+ nodename+"("+
           classname+"):";;
           return;
        }
     }
        
     
   
     try{
       
       for( int i= 0 ; i < attributes.size() ; i++ ){
         Vector V = ( Vector )( attributes.elementAt(  i  ) );
         String Name = ( String )( V.elementAt( 0 ) );
         Object Value = V.elementAt( 1 );
         int Type = ( ( Integer )( V.elementAt( 2 ) ) ).intValue();
         nf.putattr( Name , Value , TypeConv.convertFrom( Type ) );
       }
        attributesAdded = false;
        attributes.clear();
       if( Debug )System.out.println( "  end put attr" );
       //linking
       if (!written) {
            if (classname.equals("SDS"))
               if ((value == null) || (ranks == null)) {
                  SetDataLink(nf, linkInfo, children);
                  nf.closedata();

                  PosWriter.remove(PosWriter.size()-1);
                  closed = true;
                  written = true;
                  return;
               } else if (value != null) { // data
                  if (ranks == null)
                     if (Debug)
                        System.out.print("ranks null");
                  int[] ranks1 = util.setRankArray(value, false);
                  Object array = Types.linearlizeArray(value, ranks1.length,
                        ranks1, (type));
                  value = null;
                  // convertArray( value , TypeConv.convertFrom( type ) ,
                  // ranks.length , ranks );
                  written = true;
                  
                  if (array == null) {
                     nf.closedata();

                     errormessage += "cannot linearlize data in "+nodename+"("+
                           classname+");";
                     if (value == null)
                        if (Debug)
                           System.out.println("Data null");
                     if (ranks == null)
                        if (Debug)
                           System.out.println("ranks  is null");
                    

                     PosWriter.remove(PosWriter.size()-1);
                     closed = true;
                     array = null;
                     return;
                  }

                  if (Debug)
                     System.out.print("ere putdata info" + ranks.length + ","
                           + ranks[0]);
                  String SS = PutArray( nf,array, type,ranks1, SlabSize);
                  if( SS != null && SS.length()>0)
                     errormessage += SS +"in "+ nodename+"("+classname+");";
                 
                  SetDataLink(nf, linkInfo, children);
                  nf.closedata();

                  PosWriter.remove(PosWriter.size()-1);
                  closed = true;
                  if (Debug)
                     System.out.println("   end put data");
                  return;
               }
         }
     }catch( NexusException s ){
       errormessage += "ExceptionB:"+s.getMessage()+ "in "+ nodename+"("+
       classname+"):";;
       if( Debug )System.out.println( "Error A "+errormessage );
       try{
         if( !closed )
           if( classname.equals( "SDS" ) )
             nf.closedata();
           else if( !classname.equals( "File" ) )
             nf.closegroup();

         PosWriter.remove(PosWriter.size()-1);
       }catch( Exception s1 ){
         // let it drop on the floor
       }
  
       return;
     }
    
    
     if( childrenAdded)
     for( int i = 0; i < children.size() ; i++ ){
       Object X = children.elementAt( i );
       try{
         if (X instanceof NexWriteNode) {
            
             ((NexWriteNode) X).write();
             if (((NexWriteNode) X).getErrorMessage() != "")
                     errormessage += ";" + ((NexWriteNode) X).getErrorMessage();
         } else if (X instanceof String) {
             String S = (String) X;
             X = linkInfo.get(S);
             if (X == null)
                errormessage += "Linking not paired for " + S +"in "+ nodename+"("+
                classname+"):";
             else if ((X instanceof NXlink) && (X != null))
                nf.makelink((NXlink) X);
             else if (X == this) {
                NXlink lnk = null;
                if (classname.equals("SDS"))
                   lnk = nf.getdataID();
                else if (!classname.equals("File"))
                   lnk = nf.getgroupID();
                if( lnk != null)
                   linkInfo.put(S, lnk);

             } else if (!(X instanceof NexWriteNode))
                     errormessage += "Linking not correct for " + S+ "in "+ nodename+"("+
                     classname+"):";
               else {
                  NexWriteNode newNode = (NexWriteNode) X;
                  NexWriteNode par = newNode.parent;
                  newNode.parent= parent;
                  newNode.write();
                  newNode.parent = par;
                  NXlink lnk;
                  if (newNode.classname.equals("SDS")) {
                     nf.opendata(newNode.nodename);
                     
                     lnk = nf.getdataID();
                     nf.closedata();
                  } else {
                     nf.opengroup(newNode.nodename, newNode.classname);
                     lnk = nf.getgroupID();
                     nf.closegroup();
                 }

                  //PosWriter.remove(PosWriter.size()-1);
                  linkInfo.put(S, lnk);
                  newNode.LinkedName = S;
               }
              
            } else
                  errormessage += "Linking incorrect;"+ "in "+ nodename+"("+
                  classname+"):";
        
         
       }catch( Exception s ){
         errormessage += "ExceptionC:"+s.getMessage()+ "in "+ nodename+"("+
         classname+"):";
         if( Debug ) System.out.println( "ErrorB = "+s );
         try{
           if( !closed )
             if( classname.equals( "SDS" ) )
               nf.closedata();
             else if( !classname.equals( "File" ) )
               nf.closegroup();

           PosWriter.remove(PosWriter.size()-1);
         }catch( Exception s2 ){
           // let it drop on the floor
         }
         return;
       }
     }
     childrenAdded = false;
     children.clear();
     
     try{
       
       if( !classname.equals( "SDS" ) )
         if( !classname.equals( "File" ) ){
           nf.closegroup();

           PosWriter.remove(PosWriter.size()-1);
         }
       closed = true;
     }catch( NexusException s ){
       errormessage += "ExceptionD:"+s.getMessage()+ "in "+ nodename+"("+
       classname+"):";
       if( Debug )System.out.println( "cannot close group"+errormessage );
     }
     try{
       if( !closed ){
         if( classname.equals( "SDS" ) )
           nf.closedata();
         else if( !classname.equals( "File" ) )
           nf.closegroup( );

       PosWriter.remove(PosWriter.size()-1);
       }
     }catch(Exception s ){
       // let it drop on the floor
     }
  }//write
  
  /**
   * Puts an array onto a file using slabs if the array is too large
   * @param nf   The Nexus file pointer at the openned data position
   * @param array  The array to be saved
   * @param type   The NexIO.Types data tpe
   * @param ranks  The ranks of the array
   * @param SlabSize  The slab size( -1 will not slab)
   * @return  An error string
   */
  public static String PutArray( CNexusFile nf , Object array ,int type, int[] ranks, int SlabSize ) {

     
      try {
         int n = ranks.length;

         if( ranks == null || n < 2 || ranks[ 0 ] < 50 ||
                  SlabSize <=100) {
            nf.putdata( array );
            return "";
         }
         
         int incrPos=-1;
         int incrSize =1;
         int[] start= new int[ranks.length];
         int[] increment = new int[ ranks.length ];
         int Prod = ranks[ ranks.length-1];
         for( incrPos= n-2; Prod < SlabSize && incrPos >=0 ; ){
            Prod *= ranks[incrPos];
            if( Prod < SlabSize)
               incrPos--;
         }
         if( incrPos <0){
            nf.putdata( array );
            return "";
         }
         
         incrSize = Math.max( 1, SlabSize*ranks[incrPos]/Prod);
         Arrays.fill( start , 0 );
         System.arraycopy( ranks,0,increment,0,increment.length);
         for( int i=0; i< incrPos; i++)
            increment[i]=1;
         int karray =0;
         
         Prod = Prod/ranks[incrPos];
         int ntimes = ranks[incrPos]/incrSize;
         if( ntimes * incrSize < ranks[incrPos]) ntimes ++;
         for( int i=0; i< incrPos; i++)ntimes *= ranks[i];
         int time =0;
         Object buffer = NexIO.Types.CreateArray( type , Prod*incrSize );
         for( boolean done = false; !done;){
            if( time > 0 && time % 10 ==0  ){
               System.out.println("     Saved "+(100*time/(float)ntimes)+"% of data."+
                            " Set NexusSlabSize to -1 for speed");
            }
            time ++;
            increment[incrPos]= Math.min(  incrSize , ranks[incrPos]- start[incrPos]  );
            int length =Prod*increment[incrPos]; 
            System.arraycopy( array , karray, buffer , 0 ,length );
            karray += length;
            nf.putslab( buffer , start , increment );
            int j = incrPos;
            start[incrPos]+=increment[incrPos];
            for(; j>=0&&start[j]>=ranks[j];j--){
               
               if( j-1 >=0){
                  start[j]=0;
                  start[j-1]++;
               }else
                  done = true;
            }
            
            
         }
         return "";
         
      }
      catch( Exception s ) {
         return s.toString();
      }

   }

  private boolean Okay2Compress( int Nextype, int[] ranks1){

     return false;
  /*
     if( Nextype == NexusFile.NX_CHAR)
        return false;
     if( Nextype == NexusFile.NX_BINARY)
        return false;
     if( Nextype == NexusFile.NX_BOOLEAN)
        return false;
    
     if( ranks1 == null || ranks1.length < 1)
        return false;
     if( ranks1.length <=1 && ranks1[0]< 500)
        return false;
     
     return true;
  */
   
  }
  
  /**
   * 
   * @param Ranks
   * @return
   */
  private int[] fixRankArray( int[] Ranks ){
     if(0==0)
        return Ranks;
     if( Ranks == null)
        return null;
     if( Ranks.length <= 1)
        return Ranks;
     int k=0;
     for( int i=0; i<Ranks.length; i++)     
        if( Ranks[i] <=1)
           k++;
     if( k >= Ranks.length){
       int[] R = new int[1];
       R[0]=1;
       return R;
     }
       
     int[] Result = new int[Ranks.length-k];
     
     k=0;
     for( int i=0; i< Ranks.length; i++){
       if( Ranks[i] <=0) 
          k++;
       else
          Result[i-k] =Ranks[i];
     }
     return Result;
     
  }
  private String ShwDims( Object X){
    if( X == null) return "";
    if( ! X.getClass().isArray())
       return null;
    int n = Array.getLength( X);
    if( n <=0)
       return ""+n;
    return ""+n+","+ShwDims( Array.get(X,0));
  }
  public void Showw( String prompt , Hashtable HT ){
    System.out.println( prompt );
    Enumeration E = HT.keys(); 
    while( E.hasMoreElements() ){
      String S = ( String )(  E.nextElement() );
      Object X = HT.get( S );
      System.out.println( "  key = "+S+","+X.toString() );
    }
  }    

  private void SetDataLink(NexusFile nexf,Hashtable LinkInfo,Vector Children){
    if( Children.size() < 1 )
      return;
    Object O = Children.elementAt( 0 );
    if( O instanceof Vector ){
      String ident = ( String )(( (Vector )O ).firstElement() );
      NXlink nl = null;
      try{
        nl = nexf.getdataID();
      }catch( NexusException s ){
        errormessage += "Exception" +s.getMessage()+ "in "+ nodename+"("+
        classname+"):";
        return;
      }
      LinkInfo.put( ident , nl );
    }
  
  }

  /**
   * Needed to actually save this type of Nexus file
   */
  public void close(){
     if(errormessage == null)
       errormessage = "";
    
    try{
      nf.flush();
      nf.finalize();
    }catch( Throwable s ){
      errormessage += s.getMessage();
      if( Debug )System.out.println( "close:"+s );
      
    }
    nf = null;
  }

  public void show(){
    System.out.println( "parent :" + nodename);
    for( int i = 0; i< children.size(); i++){
        if( children.elementAt(i) instanceof NexWriteNode){
           NexWriteNode node = (NexWriteNode)(children.elementAt(i));
           node.show();
        }
    }
  }

  private int convertArrayR( int rankoffset, Object Value, int valueoffset,
                             int Type, int ndims, int Ranks[], Object Result ){
    int i;
    if( rankoffset == ndims - 1 ){
      System.arraycopy( Value, valueoffset, Result, 0, Ranks[ndims-1] );
      return valueoffset+Ranks[ ndims-1 ];
    }
   
    String S = TypeConv.Classname( Type );
   
    S = S.substring( 5 ).trim();  
    
    for( i = rankoffset+1  ; i <= ndims - 3 ; i++ )
      S = "["+S;                    //Class Type for Result[ i ]

    Class C = null;
    try{
      C = Class.forName( S );
    }catch( ClassNotFoundException s ){
      errormessage = s.getMessage();
      return -1;
    }
   
    Object R[] ;
    R = (Object [] ) Result;
     
    int n = valueoffset;
     
    for( i = 0 ; i < Ranks[  rankoffset ] ; i++ ){
      try{
        if( rankoffset+1 < ndims-1 )
          R[ i ] = Array.newInstance( C, Ranks[  rankoffset+1 ] );
        else 
          R[ i ] = ( new NxNodeUtils() ).CreateArray( Type, Ranks[ndims-1] );
      }catch( Exception s ){
        errormessage =  "DD"+s.toString();
        return -1;
      }
 
      n = convertArrayR( rankoffset+1, Value, n, Type, ndims, Ranks, R[i] );
       
      if( n < 0 ) return -1;
    }
    return n;
  }

  /**
   * Converts a linear array to a multidimesioned array.  Non-linear
   * values are just returned.
   */
  private Object convertArray(Object Value, int type1, int ndims, int Ranks[]){
    errormessage = "";
    if( ndims <= 1 )
      return Value;
    errormessage = "improper inputs to convertArray";
    errormessage += "1";
    
    if( Value instanceof Object[] )
      return Value;
    errormessage += "1";
    if( Ranks == null )
      return null;

    if( Ranks.length < ndims-1 )
      return null;
   
 
    errormessage = "";
  
    //System.out.print( "Z"+type+","+type1 );
    String S = TypeConv.Classname( type1  );
    
    S = S.substring( 5 ).trim();//First letters are "class "
   
    //Create MultiDimensioned array
    
    for( int i = 1 ; i< ndims-1 ; i++ )
      S = "["+S;
   
    Class C = null;
    try{
      C = Class.forName( S );
    }catch( ClassNotFoundException s ){
      errormessage = s+":"+S+"::";
      return null;
    }
   
    Object R = Array.newInstance( C , Ranks[ 0 ] );
    
    errormessage = "Improper null Value";
    if( Value == null )
      return null;
    if( R == null ) 
      return null;
    errormessage = "";
    
    int n = convertArrayR( 0 , Value , 0 , type1  , ndims , Ranks , R );
    
    if( n < 0 ) 
      return null;
    return R;
  }

  public String readString(){
    char c = 0;
    String S = null;
    try{
      while( c <= 32 )
        c = ( char )System.in.read();
      S = "";
      while( c> 32 ){
        S = S+c;
        c = ( char )System.in.read();
      }
    }catch( Exception s ){
      // let it drop on the floor
    }
    return S;   
  }

  public void show( boolean showData ){
    System.out.println( "Start Name"+nodename+"     class "+classname );
    if( value != null ){
      if( showData ){
        StringUtil.toString( value );
      }else{
        System.out.print( "Value Data Type = "+value.getClass()+ 
                          value.getClass().isArray() );
        if( value.getClass().isArray() )
          System.out.println( Array.getLength( value) );
        else 
          System.out.println( "" );
      }
    }else
      System.out.println( "Value = null" );
    for( int i = 0 ; i < children.size() ; i++ ){
      Object X = children.elementAt( i );
      if( X instanceof NxWriteNode )
        (( NexWriteNode )X ).show( showData );
      else if( X instanceof Vector )
        System.out.println( "link,  handle = "+((Vector)X).firstElement() ); 
      else if( X instanceof String )
        System.out.println( "link to add"+X );
      else System.out.println( "undefined child" );
    }

    System.out.println( "End Name"+nodename+"     class "+classname );
  }

  public static void main1( String args[] ){
    NexWriteNode nw = new NexWriteNode( "C:\\NexTest.nxs" );
    if( nw.getErrorMessage()!= "" ){
      System.out.println( "ERRORA:"+nw.getErrorMessage() );
      System.exit( 0 );
    }
    int ranks[] ;
    ranks = null;
    NexWriteNode n1 , n2 ;
    //nnn = nw;
    n1 = nw;
    n2 = null;
    ranks = new int[ 1 ];
    ranks[ 0 ] = 9;
    //ndims = 1;
    nw.addAttribute( "filenamee", (new String("Hi There").getBytes()),
                     NexIO.Types.Char , ranks );
    n1 = ( NexWriteNode )nw.newChildNode( "entry1" , "NXentry" );
    n2 = ( NexWriteNode )n1.newChildNode( "det1" , "NXdetector" );
    n2.setLinkHandle("RRR" );
    nw.write();
    n1 = ( NexWriteNode )nw.newChildNode( "entry2" , "NXentry" );
    ranks[ 0 ] = 8;
    n1.addAttribute("Testing",("xxxxxxx"+0).getBytes(),NexIO.Types.Char,ranks);
  
    n1.addLink( "RRR", n2 );
    nw.write();
    System.out.println( "Error1 = "+nw.getErrorMessage( ) );
    nw.close();
  }

  public static void main( String args[] ){
    NexWriteNode nw =  new NexWriteNode( "C:\\SampleRuns\\gppd9899.nxs" );
    if( nw.getErrorMessage()!= "" ){
      System.out.println( "ERRORA:"+nw.getErrorMessage() );
      System.exit( 0 );
    }
    char c = 0;
    String S1 = null;
    String S2 = null; 
  
    int ndims , type = NexIO.Types.Int;
    int ranks[] , values[];
    ranks  = values = null;
    NexWriteNode n1 ,  nnn;
    nnn = nw;
    n1 = nw;
    ndims = 1;
    while( c!= 'x' ){
      System.out.println("Enter option desired" );
      System.out.println(" 1.Enter String1                L:set Link handle");
      System.out.println(" 2.Enter String2                l: add link");
      System.out.println(" 3.Enter type( NXNode ) =int    A: conv to array");
      System.out.println(" 4.Enter ndims and ranks        n.#of nodes");
      System.out.println(" 5.Enter int array value");
      System.out.println(" 6. Create new node");
      System.out.println(" 7. add attrib new node");
      System.out.println(" 8.add value for new node");
      System.out.println(" S. show value");
      System.out.println(" p. parent");
      System.out.println(" w.write");
      System.out.println(" c.close");
      c = 0;
      try{
        while( c <= 32)
          c = ( char )System.in.read();
      }catch( IOException s ){
        c = 0;
      }
      if( c == '1' ){
        S1 = nw.readString();
      }else if( c == '2' ){
        S2 = nw.readString();
      }else if( c == '3' ){
        // do nothing
      }else if( c == '4' ){
        try{
          ndims = ( new Integer( nw.readString() ) ).intValue();
          ranks = new int[ ndims ];
          for( int i = 0 ; i < ndims ; i++ )
            ranks[ i ] = ( new Integer( nw.readString() ) ).intValue();
        }catch( Exception s ){
          ranks = null;
        }
      }else if( c == '5' ){
        try{
          ndims = ( new Integer( nw.readString() ) ).intValue();
          values = new int[ ndims ];
          for( int i = 0 ; i < ndims ; i++ )
            values[ i ] = ( new Integer( nw.readString() ) ).intValue();
        }catch( Exception s ){
          values = null;
        }
      }else if( c == '6' ){
        n1 = ( NexWriteNode )n1.newChildNode( S1 , S2 );
        System.out.println( "Error = "+nw.getErrorMessage() );
      }else if( c == '7' ){
        n1.addAttribute( S1 , values , type , ranks );
        System.out.println( "Error = "+nw.getErrorMessage() );
      }else if( c == '8' ){
        n1.setNodeValue( values , type , ranks );
        System.out.println( "Error = "+nw.getErrorMessage() );
      }else if( c == 'S' ){
        //Object X  = n1.convertArray( values , type , ranks.length , ranks );
        // if( X  == null ) System.out.println( "null"+nw.getErrorMessage() );
        // else System.out.println( X.getClass() );
        
        nnn.show( true );
      }else if( c == 'p' ){
        n1 = n1.parent;
        System.out.println( n1.nodename+","+n1.classname );
      }else if( c == 'w' ){
        nnn.write();
        System.out.println( "Error = "+nnn.getErrorMessage() );
      }else if( c == 'L' ){
        n1.setLinkHandle( S1 );
      }else if( c == 'l' ){
        //n1.addLink( S1 );
      }else if( c== 'c' ){
        nnn.close();
        System.exit( 0 );
      }else if( c == 'A' ){
        Object Res =  ( nnn.convertArray (  values , type ,  ndims , ranks ) );
        if( Res == null )
          System.out.println( "Error = "+nnn.getErrorMessage() );
        else
          System.out.println( Res.getClass()+"::"
                              +StringUtil.toString(  Res ) );
      }else if( c == 'n' )
	System.out.println( nnn.getNumNXentries());
   n1.ShwDims( null ) ;  
    }//while c!= 'x'
  }//main
}
