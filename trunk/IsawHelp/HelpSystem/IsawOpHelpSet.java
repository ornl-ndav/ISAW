/*
 * File:  IsawOpHelpSet.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <Mikkelsonr@uwstout.edu>
 *           University of Wisconsin-Stout
 *           Menomonie, Wisconsin 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2002/11/18 17:57:14  dennis
 * Form set for Operator documentation using "virtual" html pages.(Ruth)
 *
 */

package IsawHelp.HelpSystem;
import javax.help.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import Command.*;
import DataSetTools.operator.*;

/** A javax.help.HelpSet that gets all information on the data sets from Memory
 */
public class IsawOpHelpSet extends HelpSet
 {
  Script_Class_List_Handler  sh;
  TOCView toc;

  public IsawOpHelpSet()
    { 
     setTitle( "Isaw Operations" );
     setHomeID( "IsawOpHelp" );
     sh = new Script_Class_List_Handler();
     setLocalMap( new opMap( "IsawOpHelp" ,  sh , this ) );
     toc = new IsawTOC( this , "TOC" , "Operators" , null , sh );
    }

 /** Returns the Navigator view for this helpset.
  *
  * @param   name  the name of the view. Currently only the namd TOC returns 
  *                a non null view
  * @return   The Navigator view
  */
  public NavigatorView getNavigatorView( java.lang.String name )
    {
     if( name.equals( "TOC" ) ) 
        return toc;
     else 
        return null;
    }


 /** Returns an array of navigator views
  *  Currently only one TOC view is supported
  */
  public NavigatorView[] getNavigatorViews()
    {
     NavigatorView[] Res = new NavigatorView[ 1 ];
     Res[ 0 ] = toc;
     return Res;
    }

 /** This class implements the Map for this help set.
  *  The Id's are either Genxx or Datxx where xx is the index of the
  *  appropriate index in the Generic operator list or DataSetOperator
  *  List.  These are mapped to URL's with the Memory url handler, that 
  *  calculates and gets the appropriate information out of memory
  */
  public class opMap implements javax.help.Map
    {
     Script_Class_List_Handler sh;
     int ngeneric , ndataset;
     String HomeID;
     URLStreamHandler urlh;
     HelpSet hs;
     Vector IDs;

    /** Constructor for this Map
     */
     public opMap( String HomeID , Script_Class_List_Handler SH , HelpSet hs )
       {
        sh = SH;
        this.HomeID = HomeID;
        this.hs = hs;
        urlh = ( URLStreamHandler)( new IsawHelp.HelpSystem.Memory.Handler() );
        ngeneric = SH.getNum_operators();
        ndataset = SH.getNumDataSetOperators(); 
        IDs = null;
        this.hs = hs;
       }


    /** Determines if the id and helpset represent a valid ID.
     *  The helpset must be an instance of IsawOpHelpSet and
     *  the id must be of the form Genxx or Datxx where xx represents
     *  the index of a Generic operator or DataSetOperator
     */
     public boolean isValidID( java.lang.String id , HelpSet hs )
       {
        if( !( hs instanceof IsawOpHelpSet ) ) 
           return false;
        if( !id.startsWith( "Dat" ) && !id.startsWith( "Gen" ) ) 
           return false;
        try{
           int n = new Integer( id.substring( 3 ).trim() ).intValue();
           if( n < 0 ) 
              return false;
           if(( id.startsWith( "Gen" ) ) && ( n >= ngeneric ) )
              return false;
           if(( id.startsWith( "Dat" ) ) && ( n >= ndataset ) )
              return false;
           return true;
            }
        catch( Exception ss )
          {
            return false;
          }
       }


    /** Returns an enumeration of all id's
     */
     public Enumeration getAllIDs()
       {
        if( IDs == null )
          {
           IDs = new Vector( ngeneric + ndataset );
           for( int i = 0 ; i < ngeneric ; i++ )
             {
              String S = "Gen" + i;
              IDs.addElement( javax.help.Map.ID.create( S.trim() , hs ) );
             }

           for( int i = 0 ; i < ndataset ; i++ )
             {
              String S = "Dat" + i;
              IDs.addElement( javax.help.Map.ID.create( S.trim() , hs ) );
             }
          }

         return IDs.elements();
       }

    /** Returns a URL from the id value
     */
     public URL getURLFromID( javax.help.Map.ID id ) 
                              throws java.net.MalformedURLException
       {
        String host = "";
         
        if( id.id.startsWith( "Gen" ) ) 
           host = "Generic";
        else if( id.id.startsWith( "Dat" ) ) 
           host = "DataSet";
        else if( id.id.equals( "IsawOpHelp" ) ) 
           return null;
        else 
           throw new java.net.MalformedURLException( "improper id" );
        
        if( !( id.hs instanceof IsawOpHelpSet ) )
          throw new java.net.MalformedURLException( "improper helpset" );
       
        try{
           int opnum = ( new Integer( id.id.substring(3).trim() ) ).intValue();
          
           return new URL( "Memory" , host , opnum , "x" , urlh );
            }
        catch( Exception ss )
          {
            throw new java.net.MalformedURLException( "improper id number" );
          }
       }


    /** Determines if a given URL has an associated ID in this map
     */
     public boolean isID( java.net.URL url )
       {
        if( !url.getProtocol().equals( "Memory" ) ) 
           return false;
        if( url.getHost().equals( "Generic" ) )
          {
           int port = url.getPort();
           if( port < 0 ) 
              return false;
           if( port >= ngeneric ) 
              return false;
           return true;

          }
        else if( url.getHost().equals( "DataSet")  )
          {
           int port = url.getPort();
           if( port < 0 ) 
              return false;
           if( port >= ndataset ) 
              return false;
           return true;
           }
        return false;
       }


    /** Returns the id associated with a given URL in this map, if any
     */
     public javax.help.Map.ID getIDFromURL( java.net.URL url )
       {
        if( url == null ) 
          return null;
        if( !url.getProtocol().equals( "Memory" ) ) 
          return null;
        String host = url.getHost();
        int port = url.getPort();
        return  javax.help.Map.ID.create( host + port ,  hs );
       }


    /** Same as getIDFromURL
     */
     public javax.help.Map.ID getClosestID( java.net.URL url )
       {
        return getIDFromURL( url );
       }


    /** Returns an Enumeration of javax.help.Map.ID's. I hope this is what 
     *  they want
     */
     public Enumeration getIDs( java.net.URL url ) 
       {
        Vector V = new Vector();
        if( url == null ) 
           return null;
        if( !url.getProtocol().equals( "Memory" ) )
           return null;
        String host = url.getHost();
        if( ( host.equals( "Generic" ) ) || ( host.equals( "DataSet" ) ) )
        {
           host = host.substring( 0 , 3 );
           int port = url.getPort();
           if( port < 0 )
              return null;
           if( ( port >= ndataset ) &&( host.equals( "Dat" ) ) )
              return null;
           if( ( port >= ngeneric ) &&( host.equals( "Gen" ) ) )
              return null;
           V.addElement( javax.help.Map.ID.create( host + port , hs ) );
           return V.elements();
         }
        else 
           return null;
       }
    }//class opMap


 /** Creates this help set and viewer
  */
  public static void main( String args[] )
    {
     JFrame jf = new JFrame( "Test" );
     JHelp jh = new JHelp( new IsawOpHelpSet() );
     jf.setSize( 500 , 500 );
     jf.getContentPane().add( jh );
     jf.show();
    }
 }//class IsawOpHelpSet


 /** This is the TOC view used by IsawOpHelpSet.  There is NO file.   
  *  The information is created in memory
  */
 class IsawTOC  extends TOCView
 {
  HelpSet hs;
  static Script_Class_List_Handler sh;
  int ngeneric , ndatasets;
  static DefaultMutableTreeNode  Node;

 /** Constructor.  sh contains all the information needed for the operators
  */
  public IsawTOC( HelpSet                   hs, 
                  String                    name,  
                  String                    label, 
                  java.util.Hashtable       params,
                  Script_Class_List_Handler sh   )
    { 
     super( hs, name, label, params );
     this.hs = hs;
     this.sh = sh;
     ngeneric = sh.getNum_operators();
     ndatasets = sh.getNumDataSetOperators();   
     Node = getTOC( ngeneric , ndatasets , hs );
     }
   
 /** Returns the TOC contents as a tree
  */
  public DefaultMutableTreeNode getDataAsTree()
    {
     return Node;
    }

  
 /** Creates a navigator view for this TOC view
  */
  public Component createNavigator( HelpModel model )
    {
     return new JHelpTOCNavigator( this , model );
    }

 
 /** Returns the node associated with this view
  */
  public static DefaultMutableTreeNode parse( java.net.URL    url, 
                                              HelpSet         hs, 
                                              Locale          locale,
                                              TreeItemFactory factory )
    {
     return Node;
    }


  /** Recursive Binary search. end not an option, start has not been checked*/
  private static int find( DefaultMutableTreeNode parent, 
                           String Name, 
                           int    start, 
                           int    end )
    {
     if( start < 0 ) 
        return -1;
     if( end < 0 ) 
        return -1;
     if( start >= parent.getChildCount() ) 
        return parent.getChildCount();
     
   
     int mid = ( start + end )/2;
     String S =((TOCItem) ((DefaultMutableTreeNode)(parent.getChildAt( mid ))).
                                                    getUserObject()).getName();
     if( S.equals( Name ) ) 
        return mid;
     if( S.compareTo( Name ) < 0)
       {
        if( mid + 1 >= end )
           return end;
        else
           return find( parent , Name , mid + 1 , end );
        }
     else if( S.compareTo( Name ) > 0 )
       {
        if( start >= mid - 1 )
           return start;
        else
           return find( parent , Name , start , mid - 1 );
       }
     else 
        return mid;
    }


  /** Inserts a toc item at the end of a category list of an operator. 
   *  New Nodes are created if the category is not present.
   */
  private static void insert( DefaultMutableTreeNode parent, 
                              TOCItem  toc, 
                              String[] cat, 
                              int      catposition )
    {
     DefaultMutableTreeNode node;
    
     if( catposition >= cat.length )
       {node = new DefaultMutableTreeNode( toc );
        parent.add( node );
        return;
       }
     int pos = find( parent, cat[ catposition ], 0, parent.getChildCount() );
      
     if( pos < 0 ) 
        return;
     if( pos >= parent.getChildCount( ) )
        node = addNode( parent , cat[ catposition ] , null , null );
     else if( pos < 0 )
        node = null;
     else 
       {
        TreeNode tn = parent.getChildAt( pos );
        TOCItem tocx = (TOCItem)(( DefaultMutableTreeNode )tn ).getUserObject();
        if( tocx.getName().equals( cat[ catposition ] ) )
           node = ( DefaultMutableTreeNode )tn;
        else
          {tocx = new TOCItem();
           tocx.setName( cat[ catposition ] );
           node = new DefaultMutableTreeNode( tocx );
           parent.insert( node , pos );
          }
       }
     if( node != null )
        insert( node , toc , cat , catposition + 1 );
    }


 /** Utility method to add a node at the end of its parent's children
  */
  private static  DefaultMutableTreeNode addNode( DefaultMutableTreeNode parent,
                                                  String Name,
                                                  javax.help.Map.ID id, 
                                                  HelpSet           hs )
    {
     TOCItem toc;
     if( id == null )
       toc = new TOCItem();
     else
       toc = new TOCItem( id , null , hs , null );
     toc.setName( Name );
     DefaultMutableTreeNode Res = new DefaultMutableTreeNode( toc );
     parent.add( Res );
     return Res;
    }


  /** Calculated the Table of Contents(TOC) from the Script_Class_List_Handler
  */
  public static DefaultMutableTreeNode getTOC( int ngeneric, 
                                               int ndatasets, 
                                               HelpSet hs )
    {
     DefaultMutableTreeNode topNode , parentNode;
     parentNode = new DefaultMutableTreeNode();
     TOCItem top = new TOCItem();
     top.setName( "Top" );
     topNode = new DefaultMutableTreeNode( top );

      
     DefaultMutableTreeNode operators = 
                            addNode( topNode, "operators", null, null );
     DefaultMutableTreeNode Alphabetic = 
                            addNode( operators, "Alphabetic", null, null );
     DefaultMutableTreeNode Category = 
                            addNode( operators, "Category", null, null );
     DefaultMutableTreeNode Generic = 
                            addNode( Alphabetic, "Generic", null, null );
     DefaultMutableTreeNode DataSet = 
                            addNode( Alphabetic, "DataSet", null, null );
     DefaultMutableTreeNode letters = 
                            addNode( Generic, "A-E", null, null );
     char lastletter = 'E';  
       
     for( int i = 0 ; i < ngeneric ; i++  )
       {
        Operator op  = sh.getOperator( i );
        String title = op.getCommand();
        if( title == null ) title = "";
        if( title.length() >= 1 )
           while( title.toUpperCase().charAt(0 ) > lastletter )
             {
              String range = ((char)( lastletter + 1 ) + "-" + 
                              (char)( lastletter + 5 ) );
              letters = addNode( Generic, range, null, null );
              
              lastletter = (char)( lastletter + 5 ); 
             }
        String idd = "Gen" + i;
        TOCItem ttt = (TOCItem)addNode( letters , title ,  
              javax.help.Map.ID.create( "Gen" + i, hs ), hs ).getUserObject();
        String[] cat = op.getCategoryList();
        insert( Category, ttt, cat, 1 );
       }

     //Now Add to the Data set nodes
     letters = addNode( DataSet, "A-E", null, null );
     lastletter = 'E';  
      
     for( int i = 0 ;i < ndatasets ; i++ )
       {
        Operator op  = sh.getDataSetOperator( i );
        String title = op.getCommand();
        if( title == null ) title = "";
          
        if( title.length() >= 1 )
           while( title.toUpperCase().charAt(0 ) > lastletter )
             {
              String range = ((char)( lastletter + 1 ) + "-" + 
                              (char)( lastletter + 5 ) );
              letters = addNode( DataSet, range, null, null );
              lastletter = (char)( lastletter + 5 ); 
             }
        String idd = "Dat" + i;
        TOCItem ttt = (TOCItem)addNode(  letters , title ,  
             javax.help.Map.ID.create( "Dat" + i ,  hs ) , hs ).getUserObject();
        String[] cat = op.getCategoryList();
        insert( Category , ttt , cat , 1 );
       }
   
     return topNode;

    }//getTOC

 }//IsawTOCView
