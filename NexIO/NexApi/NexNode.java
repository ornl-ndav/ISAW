/*
 * File:  NexNode.java 
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
 * Revision 1.11  2003/06/18 20:33:59  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.10  2002/11/27 23:29:07  pfpeterson
 * standardized header
 *
 * Revision 1.9  2002/11/20 16:15:21  pfpeterson
 * reformating
 *
 * Revision 1.8  2002/06/19 15:27:20  rmikk
 * Eliminated commented out code and fixed code alignment
 * and spacings
 *
 * Revision 1.7  2002/04/01 21:36:39  rmikk
 * Used the CNexusFile to load large Nexus files faster
 * Added one more character to Strings to get last letter
 *
 * Revision 1.6  2002/02/26 15:49:08  rmikk
 * Add the getDimension routine
 * Added a debug field
 *
 */
package NexIO.NexApi;

import DataSetTools.util.StringUtil;
import neutron.nexus.*;
import NexIO.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;


/**
 * Implementation of NxNode that gets the Nexus info from a local
 * nexus file
 */
public class NexNode implements NxNode{
  CNexusFile NF;
  Hashtable dirinfo;
  Hashtable attrlist;
  Vector Nodelistinfo;
  Vector currentOpenNode;
  String filename;
  public String errormessage = "";
  Hashtable typeToString;
  Hashtable LinkInfo;
  static int NameInt = 0;
  boolean debug = false;

  /**
   *@param filename  the nexus filename
   */
  public NexNode( String filename ){
    errormessage = "";
    this.filename = filename;
    try{
      NF = new CNexusFile( filename, NexusFile.NXACC_READ );
    }catch( NexusException s ){
      errormessage = NxNodeUtils.ER_BADFILE;//s.getMessage();
      NF = null;
    }
    Nodelistinfo = new Vector();
    currentOpenNode = new Vector();
    dirinfo = null;
    attrlist = null;
    initHashtable();
    LinkInfo = new Hashtable();
  }

  private void initHashtable(){
    typeToString = new Hashtable();
    typeToString.put( new Integer( NexusFile.NX_CHAR ), "byte" );
    typeToString.put( new Integer( NexusFile.NX_FLOAT32 ), "float" );
    typeToString.put( new Integer( NexusFile.NX_FLOAT64 ), "double" );
    typeToString.put( new Integer( NexusFile.NX_INT16 ), "short" );;
    typeToString.put( new Integer( NexusFile.NX_INT32 ), "int" );
    typeToString.put( new Integer( NexusFile.NX_INT8 ), "byte" );
    typeToString.put( new Integer( NexusFile.NX_UINT16 ), "short" );
    typeToString.put( new Integer( NexusFile.NX_UINT32 ), "int" );
    typeToString.put( new Integer( NexusFile.NX_UINT8 ), "byte" );
  }


  private NexNode( Vector Nodelist, Vector openNode, String filename,
                   CNexusFile NF, Hashtable LinkInfo ){
    errormessage = "";
    dirinfo = null;
    attrlist = null;
    this.NF = NF;
    Nodelistinfo = new Vector();
    Vector V;
    
    this.LinkInfo = LinkInfo;
    
    for( int i = 0; i < Nodelist.size(); i++ ){
      V = ( Vector )( Nodelist.elementAt( i ) );
      String S1, S2;
      
      S1 = ( String )( V.firstElement() );
      S2 = ( String )( V.lastElement() );
      
      V = new Vector();
      V.add( new String( S1 ) );
      V.add( new String( S2 ) );
      Nodelistinfo.add( V );
    }
 
    this.filename = filename;
    currentOpenNode = openNode;
    initHashtable();
  }


  // gets this node to be the currently open node
  private boolean open(){
    int i, i1, k;
    boolean done = false;

    i1 = 0;
    for( i = 0; ( i < Nodelistinfo.size() ) && ( i < currentOpenNode.size() ) 
           && !done; i++ ){
         Vector X1 = ( Vector )( Nodelistinfo.elementAt( i ) );
         Vector X2 = ( Vector )( currentOpenNode.elementAt( i ) );

         done = true;
         i1 = i;
         if( X1.firstElement().equals( X2.firstElement() ) )
           if( X1.lastElement().equals( X2.lastElement() ) ){
             done = false;
           }
    }
    if( !done )
      i1++;
    if( i1 >= Nodelistinfo.size() )
      i1 = Nodelistinfo.size();
    if( i1 >= currentOpenNode.size() )
      i1 = currentOpenNode.size();
    int N = currentOpenNode.size();
    
    for( k = i1; k < N; k++ ){
      Vector VV = ( Vector )currentOpenNode.remove( i1 );
      
      attrlist = null;
      dirinfo = null;
      try{
        if( ( ( String )( VV.lastElement() ) ).equals( "SDS" ) )
          NF.closedata();
        else
          NF.closegroup();
      }catch( NexusException s ){
        errormessage = s.getMessage();
        return false;
      }
    }
    N = Nodelistinfo.size();
    for( k = i1; k < N; k++ ){
      currentOpenNode.addElement( Nodelistinfo.elementAt( k ) );
      
      attrlist = null;
      dirinfo = null;
      Vector X = ( Vector )( Nodelistinfo.elementAt( k ) );
      
      try{
        if( ( ( String )( X.lastElement() ) ).equals( "SDS" ) )
          NF.opendata( ( String )( X.firstElement() ) );
        else
          NF.opengroup( ( String )( X.firstElement() ), ( String )( X.lastElement() ) );
      }catch( NexusException s ){
        errormessage = s.getMessage();
        return false;
      }
    }
    
    return true;
  }


  /**
   * Returns the number of child nodes of this child
   */
  public int getNChildNodes(){
      errormessage = "";
      if( !open() )
        return -1;
      if( dirinfo == null )
        try{
          dirinfo = NF.groupdir();
        }catch( NexusException s ){
          errormessage = s.getMessage();
          dirinfo = null;
        }
      
      if( dirinfo == null )
        return 0;
      return dirinfo.size();
  }


  /**
   * Returns the index-th child node or null.
   *@see #getErrorMessage()
   */
  public NxNode getChildNode( int index ){
    dirinfo = null;
    if( !open() )
      return null;
    errormessage = "";
    int n = getNChildNodes();
    
    if( ( index < 0 ) || ( index >= n ) ){
      errormessage = "index out of range";
      return null;
    }
    Enumeration keys = dirinfo.keys();
    Object Result = keys.nextElement();
    
    for( int i = 0; i < index; i++ )
      Result = keys.nextElement();
    String key = ( String )Result;
    
    return getChildNode( key );
  }


  /**
   * Returns the child whose node name is "NodeName"
   *@see #getErrorMessage()
   */
  public NxNode getChildNode( String NodeName ){
    errormessage = "";
    if( !open() )
      return null;
    int n = getNChildNodes();
    
    if( NodeName == null ){
      errormessage = "Null child not allowed in " + NodeName;
      return null;
    }
    String keyValue = ( String )( dirinfo.get( NodeName ) );
    
    if( keyValue == null ){
      errormessage = "No Such Child " + NodeName;
      return null;
    }
    Vector Nodeinfo = ( Vector )( Nodelistinfo.clone() );
    Vector X = new Vector();
    
    X.addElement( NodeName );
    X.addElement( keyValue );
    Nodeinfo.addElement( X );

    return new NexNode( Nodeinfo, currentOpenNode, filename, NF, LinkInfo );
  }

  /**
   * Returns the name for this node
   */
  public String getNodeName(){
    errormessage = "";
    if( Nodelistinfo.size() < 1 )
      return "File";
    else{
      Vector X = ( Vector )( Nodelistinfo.lastElement() );
      return( String )( X.firstElement() );
    }
  }

   /**
    * Returns the class for this node.<P>
    *
    * NOTE: It should be a Nexus class or SDS with the same structure
    * as int the Nexus standard
    */
  public String getNodeClass(){
    errormessage = "";
    if( Nodelistinfo.size() < 1 )
      return "File";
    else{
      Vector X = ( Vector )( Nodelistinfo.lastElement() );
      return( String )( X.lastElement() );
    }
  }

  /**
   * Returns the number of attributes for this node
   */
  public int getNAttributes(){
    errormessage = "";
    if( !open() )
      return -1;
    if( attrlist == null )
      try{
        attrlist = NF.attrdir();
      }catch( NexusException s ){
        errormessage = s.getMessage();
        attrlist = null;
      }

    if( attrlist == null )
      return 0;
    return attrlist.size();

  }
  

  public String getLinkName(){
    if( !open() )
      return null;
    NXlink l = null;
    
    try{
      if( getNodeClass().equals( "SDS" ) )
        l = NF.getdataID();
      else
        l = NF.getgroupID();
    }catch( NexusException s ){
      errormessage += s.getMessage();
      return null;
    }
    if( l == null ){
      errormessage = "Link could not be established";
      return null;
    }
    String S = "linkk" + NameInt;
    
    NameInt++;
    LinkInfo.put( S, l );
    
    return S;
  }


  public boolean equals( String linkName ){
    if( !open() )
      return false;
    
    NXlink l = null;
    
    try{
      if( getNodeClass().equals( "SDS" ) )
        l = NF.getdataID();
      else
        l = NF.getgroupID();
    }catch( NexusException s ){
      errormessage += s.getMessage();
      System.out.println( "NexNode,Equal Error=" + errormessage );
      return false;
    }

    if( l == null ){
      errormessage = "Link could not be established";
      return false;
    }

    Object X = LinkInfo.get( linkName );

    if( X == null ){
      errormessage = "No Link established";
      return false;
    }

    if( X instanceof NXlink ){
      NXlink l2 = ( NXlink )X;

      if( l2.tag != l.tag )
        return false;
      if( l2.ref != l.ref )
        return false;
      return true;
    }

    errormessage = "Improper Link value";
    return false;
  }


  /**
   * Gets the dimesions of the Node's value
   *
   * @return an array whose length is the number of dimensions and
   * whose entries represent the length in that dimension
   */
  public int[] getDimension(){
    if( !open() )
      return null;
    if( !( getNodeClass().equals( "SDS" ) ) )
      return null;
    
    int iDim[], args[];
    
    iDim = new int[7];
    args = new int[2];
    try{
      NF.getinfo( iDim, args );
      int[] res = new int[args[0]];
      
      for( int i = 0; i < args[0]; i++ )
        res[i] = iDim[i];
      return res;
    }
    catch( NexusException s ){
      errormessage = s.getMessage();
      return null;
    }
  }

   /**
    * Returns the value(data-not attribute) of this node.<P>
    * <OL>Note:<LI> mulitdimensioned arrays are linearlized.
    *  <LI> Unsigned data types are "fixed" by copying to the
    *       next higher data type
    *  <LI> Char arrays go to strings
    *  </ol>
    */
  public Object getNodeValue(){
    errormessage = "";

    if( !open() )
      return null;
    int iDim[], args[];

    iDim = new int[7];
    args = new int[2];
    
    try{
      if( debug )
        System.out.println( "----NxApNode: start----" );
      NF.getinfo( iDim, args );

      if( args[0] <= 0 ){
        errormessage = "No Data Here";
        return null;
      }

      // **Object array=CreateMultiArray(0,args[0],iDim,args[1]);
      int TotLength = 1;

      if( ( args[0] < 1 ) || ( iDim == null ) )
        TotLength = 0;
      else
        for( int i = 0; i < args[0]; i++ )
          TotLength = TotLength * iDim[i];
      
      Object O = NF.getData( args[1], TotLength );
      
      NxNodeUtils ND = new NxNodeUtils();

      Object array = ND.fixUnsignedArray( O, args[1], TotLength );

      return array;
    }catch( NexusException s ){
      errormessage = s.getMessage();
      return null;
    }
  }


  private Object linearlizeArray(Object array, int ndims, int lengths[],
                                 int type){
    errormessage = "improper dimensions linearlize";
    if( ndims < 1 )
      return null;
    if( lengths == null )
      return null;
    if( ndims > lengths.length )
      return null;
    if( ndims == 1 )
      return array;
    
    int l = 1;
    
    for( int i = 0; i < ndims; i++ )
      l = l * lengths[i];
    if( l <= 0 )
      return null;
    errormessage = "";
    NxNodeUtils ND = new NxNodeUtils();
    Object buff = ND.CreateArray( type, l );
    int n = linearlizeR( array, 0, ndims, lengths, type, buff, 0 );
    
    if( n < 0 )
      return null;
    return buff;
  }

  private int linearlizeR(Object array, int dimoffset, int ndims,int lengths[],
                          int type, Object buff, int buffOffset ){
    if( dimoffset == ndims - 1 ){
      try{
        System.arraycopy( array, 0, buff, buffOffset, lengths[ndims - 1] );
        return buffOffset + lengths[ndims - 1];
      }catch( Exception s ){
        errormessage = "arraycopy error " + s;
        return -1;
      }
    }

    Object Res[];
    
    Res = ( Object[] )array;

    int n = buffOffset;
    
    for( int i = 0; i < Res.length; i++ ){
      n = linearlizeR( Res[i], dimoffset + 1, ndims, lengths, type, buff, n );
      if( n < 0 )
        return -1;
    }
    return n;
  }

  private Object fixTotarray( Object array, int type ){
    return array;

    /* Eliminated when we used linear arrays
       NxNodeUtils ND = new NxNodeUtils();
       Object X=null;
       if( array == null)
       {errormessage= "No array";
       return null;
       }
       else if( array instanceof int[])
       X=ND.fixUnsignedArray(array , type, ((int[])array).length);
       else if( array instanceof float[])
       X=ND.fixUnsignedArray(array , type, ((float[])array).length);
       else if( array instanceof byte[])
       X=ND.fixUnsignedArray(array , type, ((byte[])array).length);
       else if( array instanceof short[])
       X=ND.fixUnsignedArray(array , type, ((short[])array).length);
       else if( array instanceof double[])
       X=ND.fixUnsignedArray(array , type, ((double[])array).length);
       else if( array instanceof Object[])
       {
       }
       else if( 3==2)
       {Object U[];
       int n1 = ((Object[])array).length;
       U= new Object[n1];
       
       for( int i = 0; i<((Object[])array).length; i++)
       { U[i] = fixTotarray( ((Object[])array)[i], type);
       if( U[i]==null)
       {errormessage=ND.getErrorMessage();
       return null;
       }
       }
       return U;
       
       }
       else 
       return array;
       if( X==null)
       errormessage=ND.getErrorMessage();
       if( X instanceof String)
       System.out.println("String");
       else
       System.out.println("orig length");
       return X; 
       */
  }

  //deprecated
  private Object CreateMultiArray1( int ndims, int length[], int type ){
    NxNodeUtils ND = new NxNodeUtils();
    
    errormessage = "";
    if( ndims == 1 )
      return ND.CreateArray( type, length[0] );
    if( ndims == 2 ){
      if( type == NexusFile.NX_FLOAT32 )
        return new float[length[0]][length[1]];
      else if( type == NexusFile.NX_CHAR )
        return new byte[length[0]][length[1]];
      else if( type == NexusFile.NX_FLOAT64 )
        return new double[length[0]][length[1]];
      else if( type == NexusFile.NX_INT16 )
        return new short[length[0]][length[1]];
      else if( type == NexusFile.NX_INT32 )
        return new int[length[0]][length[1]];
      else if( type == NexusFile.NX_INT8 )
        return new byte[length[0]][length[1]];
      else if( type == NexusFile.NX_UINT16 )
        return new short[length[0]][length[1]];
      else if( type == NexusFile.NX_UINT32 )
        return new int[length[0]][length[1]];
      else if( type == NexusFile.NX_UINT8 )
        return new byte[length[0]][length[1]];
      errormessage = "Undefined data type" + type;
      return null;
    }
    if( ndims == 3 ){
      if( type == NexusFile.NX_FLOAT32 )
        return new float[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_CHAR )
        return new byte[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_FLOAT64 )
        return new double[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_INT16 )
        return new short[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_INT32 )
        return new int[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_INT8 )
        return new byte[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_UINT16 )
        return new short[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_UINT32 )
        return new int[length[0]][length[1]][length[2]];
      else if( type == NexusFile.NX_UINT8 )
        return new byte[length[0]][length[1]][length[2]];
      
      errormessage = "Undefined data type" + type;
      return null;
    }
    errormessage = "Too many Dimensions";
    return null;
  }


  private Object CreateMultiArray(int offset,int ndims,int lengths[],int type){
    NxNodeUtils ND = new NxNodeUtils();

    if( offset >= ndims )
      return null;
    if( offset == ndims - 1 )
      return ND.CreateArray( type, lengths[offset] );
    String S = ( String )typeToString.get( new Integer( type ) );
    
    S = S.substring( 0, 1 ).toUpperCase();
    for( int i = 0; i < ndims - offset - 1; i++ )
      S = "[" + S;
    Class C = null;

    try{
      C = Class.forName( S );
    }catch( Exception s ){
      errormessage = "Class=" + C + " creation error=" + s;
      //System.out.println("Class="+C+" creation error="+s);
      return null;
    }
    Object Res1;
    
    Res1 = Array.newInstance( C, lengths[offset] );

    Object Res[];

    Res = ( Object[] )Res1;

    for( int i = 0; i < lengths[offset]; i++ ){
      Res[i] = CreateMultiArray( offset + 1, ndims, lengths, type );
      if( Res[i] == null )
        return null;
    }

    return Res;

  }


  /**
   * Returns the index-th attribute  or null
   */
  public Attr getAttribute( int index ){
    errormessage = "";
    if( !open() )
      return null;
    attrlist = null;
    int n = getNAttributes();
    
    if( ( index < 0 ) || ( index >= n ) ){
      errormessage = "index out of range";
      return null;
    }

    Enumeration keys = attrlist.keys();
    Object Result = keys.nextElement();

    for( int i = 0; i < index; i++ )
      Result = keys.nextElement();
    String key = ( String )Result;

    Object keyVal = getAttrValue( key );

    if( keyVal == null )
      return null;
         
    if( !( keyVal instanceof AttributeEntry ) )
      return( Attr )( new AttrImp( key, keyVal ) );

    AttributeEntry AE = ( AttributeEntry )keyVal;
    int L = AE.length;

    if( AE.type == NexusFile.NX_CHAR )
      L++;
    Object X = getData( key, AE.type, L );

    if( X == null )
      return null;
 
    return( Attr )( new AttrImp( key, X ) );

  }


  /**
   * Returns error messages or "" if none
   */
  public String getErrorMessage(){
      return errormessage;
  }

  private Object getData( String AttrName, int type, int length ){
    NxNodeUtils NN = new NxNodeUtils();

    Object X = NN.CreateArray( type, length );

    if( X == null ){
      errormessage = NN.getErrorMessage();
      return null;
    }

    int args[];

    args = new int[2];
    args[0] = length;
    args[1] = type;

    try{
      NF.getattr( AttrName, X, args );
    }catch( NexusException s ){
      errormessage = s.getMessage();
      return null;
    }

    return  NN.fixUnsignedArray( X, args[1], args[0] );
  }

  /**
   * Returns the value of the attribute with the given name
   */
  public Object getAttrValue( String AttrName ){
    if( !open() )
      return null;
    if( AttrName == null )
      return null;
    int n = getNAttributes();
    Object keyValue = ( attrlist.get( AttrName ) );
    
    if( keyValue == null ){
      errormessage = "No such Attribute";
      return null;
    }
    if( !( keyValue instanceof AttributeEntry ) )
      return keyValue;
    AttributeEntry AE = ( AttributeEntry )keyValue;
    
    int L = AE.length;

    if( AE.type == NexusFile.NX_CHAR )
      L++;
    Object X = getData( AttrName, AE.type, L );

    return X;
  }

  /**
   * shows this node for debugging purposes
   */
  public String show(){
    return "naem=" + getNodeName() + "::class=" + getNodeClass();
  }

  /**
   * Another debug show
   */
  public String show1(){ //for Debug purposes
    if( Nodelistinfo.size() < 1 )
      return "Top level";
    else{
      Vector X = ( Vector )( Nodelistinfo.lastElement() );
      
      return "name=" + X.firstElement() + ", classname=" + X.lastElement();
    }
  }

  /**
   * Returns the parent of this node or the top node
   */
  public NexNode getParent(){
    errormessage = "";
    if( Nodelistinfo.size() < 1 )
      return this;
    Vector X = ( Vector )( Nodelistinfo.clone() );

    X.remove( Nodelistinfo.size() - 1 );

    return new NexNode( X, currentOpenNode, filename, NF, LinkInfo );
  }


  /**
   * utility for the test program
   */
  public String readLine(){
    String Res = "";
    char c = 0;
    
    try{
      while( c < 32 )
        c = ( char )System.in.read();
      Res = Res + c;
      while( c >= 32 ){
        c = ( char )System.in.read();
        if( c >= 32 )
          Res = Res + c;
      }
      System.out.println( "Res=" + Res );
      return Res;
    }catch( Exception s ){
      return "";
    }
  }

  /**
   * Another debugging utility
   */
  private void showVector( Vector V ){
    if( V == null ){
      System.out.println( "null" );
      return;
    }
    System.out.print( "[" );
    
    for( int i = 0; i < V.size(); i++ ){
      Object X = V.elementAt( i );

      if( X instanceof Vector )
        showVector( ( Vector )X );
      else
        System.out.print( X.toString() );
      if( i < V.size() - 1 )
        System.out.print( "," );
    }
    System.out.print( "]" );
  }

  /**
   * closes the connection
   */
  public void close(){
    NF = null;
  }


  /**
   * Another debugging utility
   */
  public void showVectors(){
    System.out.println( "CurrentOpen=" );
    showVector( currentOpenNode );
    System.out.println( "" );
    System.out.println( "this Node" );
    showVector( Nodelistinfo );
    System.out.println( "" );
  }


  /**
   * Test program for this module: NexNode
   */
  public static void main( String args[] ){
    DataSetTools.util.SharedData UU = new DataSetTools.util.SharedData();
    String filename = "C:\\SampleRuns\\Nex\\lrcs3000.nxs";
    
    if( args != null )if( args.length > 0 )
      filename = args[0];
    System.out.println( "filename=" + filename );
    NexNode NN = new NexNode( filename );

    // "C:\\SampleRuns\\Nex\\trics00151999.hdf"NexTest.nxs);

    //trics00151999.hdf");//lrcs3000.nxs");
    System.out.println( "Error=" + NN.getErrorMessage() );
    System.out.println( NN.getNChildNodes() + "," + NN.getNAttributes() );
    NexNode Node1, Node2, NN1;

    Node1 = NN;
    Node2 = NN;
    char c = 0;
    int n = 0;
    int N = 0;
    String S = "";;
    while( c != 'x' ){
      System.out.println("Select Option Desired" );
      System.out.println("  n: Enter number argument         1.Act->Node1");
      System.out.println("  N: Enter 2nd Number arguemt      2. Act->Node2");
      System.out.println("  s: enter string argument         3. Node1->Act");
      System.out.println("  S: show this node                4. Node2->Act");
      System.out.println("  a: Show number of Attributes     E.error?");
      System.out.println("  A: show the nth attribute        5.Nodelists");
      System.out.println("  c:Show number of children        6.GetNodeValue");
      System.out.println("  C: Show and set ith child        7.Compare links");
      System.out.println("  p: open parent new node ");
      try{
        c = 0;
        while( c < 32 ){
          c = ( char )System.in.read();
        }
      }catch( Exception s ){
        c = 0;
      }
      if( c == 'n' ){
        try{
          n = ( new Integer( NN.readLine() ) ).intValue();
        }catch( Exception s ){
          n = 0;
        }
      }else if( c == 'N' ){
        try{
          N = ( new Integer( NN.readLine() ) ).intValue();
        }catch( Exception s ){
          N = 0;
        }
      }else if( c == 's' ){
        try{
          S = NN.readLine();
        }catch( Exception s ){
          n = 0;
        }
      }else if( c == 'a' ){
        System.out.println( NN.getNAttributes() );
      }else if( c == 'A' ){
        AttrImp NA = ( AttrImp )( NN.getAttribute( n ) );

        if( NA != null ){
          System.out.println("key="+NA.getItemName());
          System.out.println("Val="
                             +StringUtil.toString(NA.getItemValue()));
        }else
          System.out.println( "Error=" + NN.getErrorMessage() );
      }else if( c == 'c' ){
        System.out.println( NN.getNChildNodes() );
      }else if( c == 'C' ){
        NN1 = ( NexNode )( NN.getChildNode( n ) );
        if( NN1 != null ){
          NN = NN1;
        }else
          System.out.println( "Error=" + NN.getErrorMessage() );
      }else if( c == 'S' ){
        System.out.println( NN.show() );
        System.out.println( "-----------Node1" );
        System.out.println( Node1.show() );
      }else if( c == 'p' ){
        NN = NN.getParent();
      }else if( c == 'E' ){
        System.out.println( "Error=" + NN.getErrorMessage() );
      }else if( c == '5' ){
        NN.showVectors();
        System.out.println( "-----------Node1" );
        Node1.showVectors();
      }else if( c == '1' ){
        Node1 = NN;
      }else if( c == '2' ){
        Node2 = NN;
      }else if( c == '3' ){
        NN = Node1;
      }else if( c == '4' ){
        NN = Node2;
      }else if( c == '6' ){
        Object X = NN.getNodeValue();
        
        if( ( NN != null ) && ( X != null ) ){
          System.out.println( "Class&Val=" + X.getClass() );
          System.out.println( StringUtil.toString( X ) );
        }else
          System.out.println( "Check error message please" );
      }else if( c == '7' ){
        String SS = Node1.getLinkName();
        
        if( SS != null )
          System.out.println( Node2.equals( SS ) );
        else
          System.out.println( "Error=" + Node1.getErrorMessage() );
      }
    }
  }
}
