/*
 * File: NexWriteUtil.java
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
 * $Log$
 * Revision 1.7  2004/03/11 19:46:35  bouzekc
 * Removed java.lang import as this is included by default.
 *
 * Revision 1.6  2003/11/24 14:22:14  rmikk
 * Implemented the setNodeName and getNodeName methods
 *
 * Revision 1.5  2003/10/15 02:37:51  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.4  2003/06/18 20:34:23  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.3  2002/11/27 23:29:29  pfpeterson
 * standardized header
 *
 */
package NexIO.Write.NexApi;
import DataSetTools.util.StringUtil;
import java.util.*;
import NexIO.Write.*;
import java.lang.reflect.*;

public abstract class NexWriteUtil implements NxWriteNode {
  String errormessage;
  String filename;
  Vector children;
  Vector attributes;
  String classname;
  String nodename;
  boolean Debug = false;
  Hashtable linkInfo;
  // NxWriteNode parent;
  Object value;
  int ranks[] , type;
  boolean written;
  int num_nxEntries;
  
  /**
   * @param filename the name of a file written in the Nexus API
   * format
   */
  public NexWriteUtil( String filename ){
    errormessage = "";    
    num_nxEntries = 0;
    if( filename == null){
      errormessage = " no Such File "+filename;
      return;
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
    // parent = null;
    num_nxEntries = 0;
  }

  public String getNodeName(){
     return nodename;
  }

  public void setNodeName( String args){
  }
  /**
   * Returns the number of NXentries in this file
   *
   * @param classname   Should be "NXentry"
   * @return  the number of NXentries in this file
   */ 
  public int getNumClasses( String classname){
    if ( classname.equals( "NXentry"))
      return num_nxEntries;
    return -1;
  }

  /** 
   * ABSTRACTCreates a new child node and makes in a child of the
   * current node
   *
   * @param node_name the name used to refer to this node
   * @param node_class the classname of this node <br>
   *
   * Note: the node_class should be a Nexus class like NXentry,
   * NXdata,etc.
   */
  /* public NxWriteNode newChildNode(  String node_name , String node_class )
     {errormessage = "";
     NxWriteNode nw = new NexWriteUtil( filename , nf , linkInfo , this );
     nw.nodename = new String( node_name );
     nw.classname = new String( node_class );   
     addChildNode( nw );
     if( classname.equals("File"))
     if(node_class.equals("NXentry"))
     num_nxEntries++;
     return ( NxWriteNode )nw;   
     }
   
     private NexWrite( String filename , NexusFile nf , Hashtable linkInfo , 
     NexWriteNode parent )
     {
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
  */

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
    return num_nxEntries;
  }

  private void addChildNode( NexWriteNode x ){
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
   * @param ranks the length of rank is the number of dimensions and
   * rank[i] is size of the array for dimension i.
   *
   * @see NexIO.Types#Int Type names
   */
  public void addAttribute( String AttrName, Object AttrValue, int type,
                            int ranks[] ){
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
   * @param linkhandle the name used to refer to this linked
   * information
   */
  public void addLink( String linkhandle ){
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
      
    //need to open
    Vector V = new Vector();
    V.addElement( handleName );
    V.addElement( this );
    children.addElement( V );
    //children.addElement( handleName );
  }

  public void show( ){
    System.out.println( "Start Name"+nodename+"     class "+classname );
    boolean showData = true;
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
        (( NxWriteNode )X ).show( );
      else if( X instanceof Vector )
        System.out.println( "link,  handle = "+ ((Vector)X).firstElement() );
      else if( X instanceof String )
        System.out.println( "link to add"+X );
      else System.out.println( "undefined child" );
    }

    System.out.println( "End Name"+nodename+"     class "+classname );
  }
}
