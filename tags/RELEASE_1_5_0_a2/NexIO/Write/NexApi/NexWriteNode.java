/*
 * File:  NxWriteNode.java 
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
 * Revision 1.6  2002/11/27 23:29:29  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/11/20 16:15:53  pfpeterson
 * reformating
 *
 */
 

package NexIO.Write.NexApi;
import neutron.nexus.*;
import NexIO.Write.*;
import NexIO.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.*;
import java.io.*;

/**
 * Class that handles writing information to a Nexus file using the
 * Nexus Api<P>
 *
 * NOTE: An internal representation is saved an only sent to a nexus
 * file when the write routine is executed.
 */
public class  NexWriteNode implements NexIO.Write.NxWriteNode{
  protected String errormessage;
  NexusFile nf;
  String filename;
  Vector children;
  Vector attributes;
  String classname;
  String nodename;
  boolean Debug = false;
  Hashtable linkInfo;
  NexWriteNode parent;
  Object value;
  int ranks[] , type;
  boolean written;
  int num_nxEntries;

  /**
   * @param filename the name of a file written in the Nexus API
   * format
   *
   * NOTE: This checks that the file can be successfully created
   */
  public NexWriteNode( String filename ){
    errormessage = "";
    nf = null;
    int open_mode = NexusFile.NXACC_CREATE;
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
      nf = new NexusFile( filename , open_mode );       
      if( open_mode == NexusFile.NXACC_RDWR ){
        Hashtable HT = nf.groupdir();
        Enumeration E = HT.elements();
        num_nxEntries =0;
        Object X;
        if( E != null)
          for( ; E.hasMoreElements(); ){
            X = E.nextElement();
            if( X instanceof String)
              if( X != null)
                if( "NXentry".equals((String) X ))
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
  }

  private boolean canWrite( String filename ){
    String F = filename.replace('\\','/');
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
  }

  /**
   * Returns the number of NXentries in this file
   *
   * @param classname Should be "NXentry"
   * @return the number of NXentries in this file<br>
   */ 
  public int getNumClasses( String classname){
    if ( classname.equals( "NXentry"))
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
    NexWriteNode nw = new NexWriteNode( filename , nf , linkInfo , this );
    nw.nodename = new String( node_name );
    nw.classname = new String( node_class );   
    addChildNode( nw );
    if( classname.equals("File"))
      if(node_class.equals("NXentry"))
        num_nxEntries++;
    return ( NxWriteNode )nw;   
  }
   
  private NexWriteNode( String filename , NexusFile nf , Hashtable linkInfo , 
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
   * @param type the type in NexIO.Types.java for this attribute
   * @param rank the length of rank is the number of dimensions and
   * rank[i] is size of the array for dimension i.
   *
   * @see NexIO.Types#Int Type names
   */
  public void addAttribute(String AttrName, Object AttrValue, int type,
                           int ranks[] ){
    if( nf == null ){
      errormessage = "Not initialized yes";
      return;
    }
    errormessage = "";
    Object AttrValue1 = AttrValue;
    int rank1[];
    rank1 = new int[ ranks.length ];
    System.arraycopy( ranks , 0 ,  rank1 , 0 , ranks.length );
    Vector V = new Vector( );
    V.addElement( new String( AttrName ) );
    V.addElement( AttrValue1 );
    V.addElement( new Integer( type ) );
    V.addElement( rank1 );
    attributes.addElement( V );
  }

  //------------------ Links ----------------------------
  
  /**
   * Adds a link to information as a child to this node
   *
   * @param linkhandle the name used to refer to this linked
   * information
   */
  public void addLink( String linkhandle ){
    if( nf == null ){
      errormessage = "Not initialized yes";
      return;
    }
    errormessage = "";
    children.addElement( linkhandle );
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
    children.addElement( V );
    //children.addElement( handleName );
  }

//------------------ Saving --------------------------

  /**
   * Writes node to file if it can. The node cannot have attributes,
   * children, or links incorporated after this method is executed.
   * assumes current node is opened
   */
  public void write(){
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
    if( written && !( classname.equals( "File" ) ) ){
      errormessage = "already written";
      return;
    }
     
    //close all first
    if( !written )
      try{
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
       
          nf.makedata(nodename,TypeConv.convertFrom(type),ranks.length,ranks);
          nf.opendata( nodename );
          
        }else if(( ! classname.equals( "File" ) ) ){
          nf.makegroup( nodename , classname );
          nf.opengroup( nodename , classname );
        }
      }catch( NexusException s ){
        errormessage = "init write:"+s.getMessage( );
        if( Debug) System.out.println( errormessage );
        try{
          if( classname.equals( "SDS" ) )
            nf.closedata();
          else if( !classname.equals( "File" ) )
            nf.closegroup();
          closed = true;
        }catch( Exception s3 ){
          // let it drop on the floor
        }
        return;
     }
     written = true;
   
     try{
       for( int i= 0 ; i < attributes.size() ; i++ ){
         Vector V = ( Vector )( attributes.elementAt(  i  ) );
         String Name = ( String )( V.elementAt( 0 ) );
         Object Value = V.elementAt( 1 );
         int type = ( ( Integer )( V.elementAt( 2 ) ) ).intValue();
         nf.putattr( Name , Value , TypeConv.convertFrom( type ) );
       }
       if( Debug )System.out.println( "  end put attr" );
       
       if( classname.equals( "SDS" ) )
         if( ( value == null )||( ranks == null ) ){
           SetDataLink( nf , linkInfo , children );
           nf.closedata();
           closed = true;
           return;
         }else if ( value != null ){ //data
           if( ranks == null ) 
             if( Debug ) System.out.print( "ranks null" );
           Object array = convertArray( value , TypeConv.convertFrom( type ) ,
                                        ranks.length , ranks );     
           if( array == null ){
             nf.closedata( );
             if( value == null ) 
               if( Debug)System.out.println( "Data null" );
             if( ranks == null ) 
               if( Debug) System.out.println( "ranks  is null" );
             return;
           }
      
           if( Debug )
             System.out.print( "ere putdata info"+ranks.length+","+ranks[ 0] );
    
/*
  if( Debug)
  System.out.print( "Array length = "+Array.getLength( array )+" " );
  
  Object XX1 = array;
  if( array instanceof Object[] )        
  {if( Debug) 
  System.out.print( "element leng =" +
  Array.getLength( ( ( Object[] )array )[ 0 ] ) );
  XX1 = ( ( Object[] )array )[ 0 ];          
  }
  
  // if( XX1.getClass().isArray( ) )
  //   if( !( XX1 instanceof Object[] ) )
  //    System.out.print( Array.getFloat( XX1 , 0 )+","+Array.getFloat( XX1 , 1 ) );
  */
           nf.putdata( array );
           SetDataLink( nf , linkInfo , children );
           nf.closedata( );
           closed = true;
           if( Debug )System.out.println( "   end put data" );
           return;
           
         }
     }catch( NexusException s ){
       errormessage = "write:"+s.getMessage();
       if( Debug )System.out.println( "Error A "+errormessage );
       try{
         if( !closed )
           if( classname.equals( "SDS" ) )
             nf.closedata();
           else if( !classname.equals( "File" ) )
             nf.closegroup();
       }catch( Exception s1 ){
         // let it drop on the floor
       }
  
       return;
     }
     
     errormessage = "";
     for( int i = 0; i < children.size() ; i++ ){
       Object X = children.elementAt( i );
       try{
         if( X instanceof NexWriteNode ){
           (( NexWriteNode )X).write();
           if( (( NexWriteNode ) X).getErrorMessage() != "")
             errormessage +=";"+ ( ( NexWriteNode )X ).getErrorMessage();
         }else if( X instanceof String ){
           String S = ( String )X;
           X = linkInfo.get( S );
           //System.out.print( "S ,X = "+S+"," );
           if( Debug )
             if( X == null )
               System.out.println( "null" );
             else 
               System.out.println( X.getClass() );
           if( ( X instanceof NXlink )&&( X !=  null ) ){
             nf.makelink( ( NXlink )X );
           }else{
             errormessage+= "v:No link defined up to this point "+S;
             if( !closed )
               if( classname.equals( "SDS" ) )
                 nf.closedata();
               else if( !classname.equals( "File" ) )
                 nf.closegroup();
             return;
           }
         }else{
           Vector V = ( Vector )X;
           //if( V.lastElement() instanceof NexWriteNode ){
           NXlink nlink ;
           //System.out.println( "in getlink" );
           if( classname.equals( "SDS" ) )
             nlink = nf.getdataID();
           else
             nlink = nf.getgroupID();
           V.setElementAt( nlink , 1 );
           //System.out.println( "after link made"+V.firstElement() );
           linkInfo.put( V.firstElement() , nlink );
         }
       }catch( Exception s ){
         errormessage += s.getMessage();
         if( Debug ) System.out.println( "ErrorB = "+s );
         try{
           if( !closed )
             if( classname.equals( "SDS" ) )
               nf.closedata();
             else if( !classname.equals( "File" ) )
               nf.closegroup();
         }catch( Exception s2 ){
           // let it drop on the floor
         }
         return;
       }
     }
     //System.out.println( "PP after for loop"+nodename );
     try{
       //Hashtable HT = nf.attrdir();
       //Showw( "attributes"+nodename , HT );
       //HT = nf.groupdir();
       //Showw( "groups"+nodename , HT );
       
       if( !classname.equals( "SDS" ) )
         if( !classname.equals( "File" ) )
           nf.closegroup();
       closed = true;
     }catch( NexusException s ){
       errormessage += ";"+s.getMessage();
       if( Debug )System.out.println( "cannot close group"+errormessage );
     }
     try{
       if( !closed )
         if( classname.equals( "SDS" ) )
           nf.closedata();
         else if( !classname.equals( "File" ) )
           nf.closegroup( );
     }catch(Exception s ){
       // let it drop on the floor
     }
  }//write

  public void Showw( String prompt , Hashtable HT ){
    System.out.println( prompt );
    Enumeration E = HT.keys(); 
    while( E.hasMoreElements() ){
      String S = ( String )(  E.nextElement() );
      Object X = HT.get( S );
      System.out.println( "  key = "+S+","+X.toString() );
    }
  }    

  private void SetDataLink(NexusFile nf,Hashtable LinkInfo,Vector children){
    if( children.size() < 1 )
      return;
    Object O = children.elementAt( 0 );
    if( O instanceof Vector ){
      String ident = ( String )(( (Vector )O ).firstElement() );
      NXlink nl = null;
      try{
        nl = nf.getdataID();
      }catch( NexusException s ){
        errormessage += ";" +s.getMessage();
        return;
      }
      LinkInfo.put( ident , nl );
    }
  }

  /**
   * Needed to actually save this type of Nexus file
   */
  public void close(){
    errormessage = "";
    
    try{
      nf.flush();
      nf.finalize();
    }catch( Throwable s ){
      errormessage = s.getMessage();
      if( Debug )System.out.println( "close:"+s );
    }
    nf = null;
  }

  public void show(){
  }

  private int convertArrayR( int rankoffset, Object value, int valueoffset,
                             int type, int ndims, int ranks[], Object Result ){
    int i;
    if( rankoffset == ndims - 1 ){
      System.arraycopy( value, valueoffset, Result, 0, ranks[ndims-1] );
      return valueoffset+ranks[ ndims-1 ];
    }
   
    String S = TypeConv.Classname( type );
   
    S = S.substring( 5 ).trim();  
    
    for( i = rankoffset+1  ; i <= ndims - 3 ; i++ )
      S = "["+S;                    //Class type for Result[ i ]

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
     
    for( i = 0 ; i < ranks[  rankoffset ] ; i++ ){
      try{
        if( rankoffset+1 < ndims-1 )
          R[ i ] = (Object)Array.newInstance( C, ranks[  rankoffset+1 ] );
        else 
          R[ i ] = ( new NxNodeUtils() ).CreateArray( type, ranks[ndims-1] );
      }catch( Exception s ){
        errormessage =  "DD"+s.toString();
        return -1;
      }
 
      n = convertArrayR( rankoffset+1, value, n, type, ndims, ranks, R[i] );
       
      if( n < 0 ) return -1;
    }
    return n;
  }

  /**
   * Converts a linear array to a multidimesioned array.  Non-linear
   * values are just returned.
   */
  private Object convertArray(Object value, int type1, int ndims, int ranks[]){
    errormessage = "";
    if( ndims <= 1 )
      return value;
    errormessage = "improper inputs to convertArray";
    errormessage += "1";
    
    if( value instanceof Object[] )
      return value;
    errormessage += "1";
     
    if( ranks == null )
      return null;

    if( ranks.length < ndims-1 )
      return null;
   
    int XX[]; 
    XX = new int[ 2 ]; 
    errormessage = "";
  
    //System.out.print( "Z"+type+","+type1 );
    String S = TypeConv.Classname( type1  );
    
    S = S.substring( 5 ).trim();//First letters are "class "
   
    String S1 = S;
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
   
    Object R = Array.newInstance( C , ranks[ 0 ] );
    
    errormessage = "Improper null value";
    if( value == null )
      return null;
    if( R == null ) 
      return null;
    errormessage = "";
    
    int n = convertArrayR( 0 , value , 0 , type1  , ndims , ranks , R );
    
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
	NxNodeUtils nu = new NxNodeUtils();
        nu.Showw( value );
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
    char c = 0;
    String S1 = null;
    String S2 = null;
    int ndims , type = NexIO.Types.Int;
    int ranks[] , values[];
    ranks = values = null;
    NexWriteNode n1 , n2 , nnn;
    nnn = nw;
    n1 = nw;
    n2 = null;
    ranks = new int[ 1 ];
    ranks[ 0 ] = 9;
    ndims = 1;
    nw.addAttribute( "filenamee", (Object)(new String("Hi There").getBytes()),
                     NexIO.Types.Char , ranks );
    n1 = ( NexWriteNode )nw.newChildNode( "entry1" , "NXentry" );
    n2 = ( NexWriteNode )n1.newChildNode( "det1" , "NXdetector" );
    n2.setLinkHandle("RRR" );
    nw.write();
    n1 = ( NexWriteNode )nw.newChildNode( "entry2" , "NXentry" );
    ranks[ 0 ] = 8;
    n1.addAttribute("Testing",("xxxxxxx"+0).getBytes(),NexIO.Types.Char,ranks);
  
    n1.addLink( "RRR" );
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
    NexWriteNode n1 , n2 , nnn;
    nnn = nw;
    n1 = nw;
    n2 = null;
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
        n1.addLink( S1 );
      }else if( c== 'c' ){
        nnn.close();
        System.exit( 0 );
      }else if( c == 'A' ){
        Object Res =  ( nnn.convertArray (  values , type ,  ndims , ranks ) );
        if( Res == null )
          System.out.println( "Error = "+nnn.getErrorMessage() );
        else
          System.out.println( Res.getClass()+"::"
                              +( new NxNodeUtils() ).Showw(  Res ) );
      }else if( c == 'n' )
	System.out.println( nnn.getNumNXentries());
      
    }//while c!= 'x'
  }//main
}
