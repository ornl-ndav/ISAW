
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
 * Revision 1.3  2002/12/02 15:23:21  rmikk
 * Includes ALL of the commandPane help.
 *
 * Revision 1.2  2002/11/27 23:27:28  pfpeterson
 * standardized header
 *
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
import java.io.*;

import DataSetTools.operator.*;

/** A javax.help.HelpSet that gets all information on the data sets from Memory
*/
public class IsawOpHelpSet extends HelpSet
 {
  Script_Class_List_Handler  sh;
  TOCView toc;
  boolean operatorsOnly;

  public IsawOpHelpSet( boolean operatorsOnly) 
    { 
     setTitle( "Isaw Operations" );
     setHomeID( "IsawOpHelp" );
     sh = new Script_Class_List_Handler();
     setLocalMap( new opMap( "IsawOpHelp" ,  sh , this ) );
     this.operatorsOnly = operatorsOnly;
     toc = new IsawTOC( this , "TOC" , "Operators" , null , sh , operatorsOnly);
    }

  /** Returns the Navigator view for this helpset.
  *
  * @param   name  the name of the view. Currently only the namd TOC returns a non null
  *                 view
  *@return   The Navigator view
  */
  public NavigatorView getNavigatorView( java.lang.String name )
    {
     if( name.equals( "TOC" ) ) 
        return toc;
     else 
        return null;
    }


  /** Returns an array of navigator views
  *Currently only one TOC view is supported
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
  *  List or is htm.xxx[.yy] where xxx is the base filename and yy is the reference.
  *  The Gen abd Dat id's are mapped to URL's with the Memory url handler, that 
  *  calculates and gets the appropriate information out of memory.  The others are mapped to
  *  the associated file.
  */
  public class opMap implements javax.help.Map
    {
     Script_Class_List_Handler sh;
     int ngeneric , ndataset;
     String HomeID;
     URLStreamHandler urlh;
     HelpSet hs;
     Vector IDs;  
     String helpPath = null;

     static final String hostStrings = ";CommandPane;Grammer;Attributes;Interfaces;ComDes;Examples1;";
     static final String ComDesRefStrings = ";Display;Load;Return;Save;Send;Load;";
     static final String ExampRefStrings = 
       ";Aritmetic;Arrays;For;if;error;Parameters;Batch;DataSetsSimple;DataSetAdvance;";


     /** Constructor for this Map
     *   @param HomeID  the home is for the help system to refer to this helpset
     *   @param SH      the Script_Class_List_Handler that has access to all the installed operators
     *   @param hs      the helpset for which this maps the id's to URL's
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
        helpPath = null;
        if( operatorsOnly) 
          return;
        helpPath =fixUpHelpPath( System.getProperty( "Help_Directory"));
        
        if( helpPath == null )
          {helpPath = fixUpHelpPath(System.getProperty( "ISAW_HOME" ));
           if( helpPath != null )
             if( "/\\".indexOf(helpPath.charAt(helpPath.length() - 1)) < 0)
                helpPath += '/';
           if( helpPath != null)
               helpPath+="IsawHelp";
           }
         if( helpPath == null)
          helpPath = fixUpHelpPath(System.getProperty( "user.home" ) );
          
         if( helpPath == null)
           helpPath ="http://www.pns.anl.gov/isaw/IsawHelp/";
          else
            helpPath = "file:///"+helpPath+"Command/";
       }


    //Fixes the / \ problem and check if the Command/CommandPane.html exists at the end of the path
     private String fixUpHelpPath(  String hpath)
       {
         if( hpath == null )
            return null;
         if( hpath.length() < 1)
            return null;        
         if( "/\\".indexOf(hpath.charAt( hpath.length() -1 )) < 0)
            hpath +='/';
         String S = DataSetTools.util.StringUtil.fixSeparator( hpath + "Command/CommandPane.html" );
       
         if( !(new File( S )).exists())
            return null;
         return hpath;

        }

     /** Determines if the id and helpset represent a valid ID.
     * The helpset must be an instance of IsawOpHelpSet and
     * the id must be of the form Genxx or Datxx where xx represents
     * the index of a Generic operator or DataSetOperator  or htm.xxx[.yy] where
     * xxx is the base name of the file and yy is the reference string if there is one
     */
     public boolean isValidID( java.lang.String id , HelpSet hs )
       {
        if( !( hs instanceof IsawOpHelpSet ) ) 
           return false;
        
        if( !id.startsWith( "Dat" ) && !id.startsWith( "Gen" ) &&
              !id.startsWith( "htm.")) 
           return false;
        
        if( (id.startsWith( "Dat" )) || (id.startsWith( "Gen" )))
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
          {return false;
           }
      
        if( operatorsOnly) 
           return false;
        
        int i = id.indexOf('.' , 4);
        if( i < 0)
          i = id.length();
        String host = id.substring(4, i);
        String ref ="";
        if( i < id.length())
           ref = id.substring( i+1);

        if( hostStrings.indexOf( ";"+host+";") < 0)
          return false;
        if( ref.equals( "")) 
          return true;
        if( host.equals("CommandPane") )
          if( ref.equals("OverView")) return true;
          else return false;

        if( host.equals("ComDes"))
         if( ref.equals( "")) return true;
         else if( ComDesRefStrings.indexOf(";"+ref+";") < 0 )
            return false;
         else
            return true;

       if( host.equals( "Examples1" ))
         if( ExampRefStrings.indexOf( ";"+ref+";") < 0 )
            return false;
         else return true;
 
       if( !ref.equals("" ) )
          return false;
       else
           return true;
       
       }


     /** Returns an enumeration of all id's
     */
     public Enumeration getAllIDs()
       {
        if( IDs == null )
          {IDs = new Vector( ngeneric + ndataset );
           for( int i = 0 ; i < ngeneric ; i++ )
             {String S = "Gen" + i;
              IDs.addElement( javax.help.Map.ID.create( S.trim() , hs ) );
             }

           for( int i = 0 ; i < ndataset ; i++ )
             {String S = "Dat" + i;
              IDs.addElement( javax.help.Map.ID.create( S.trim() , hs ) );
             }
           if( ! operatorsOnly )
           {IDs.addElement( javax.help.Map.ID.create( "htm.ComDes", hs));
           for( int i=0;  i + 1 < hostStrings.length() ; i = hostStrings.indexOf(';', i+1 ))
             {int j = hostStrings.indexOf(';', i+1);
              String S = "htm."+hostStrings.substring( i + 1, j);
              i = j;
              if( S.equals( "htm.CommandPane"))
                 IDs.addElement( javax.help.Map.ID.create( S+".OverView", hs));
              else if( S.equals("htm.Gramer") || S.equals( "htm.Attributes") ||
                       S.equals( "htm.Interfaces" ) )
                 IDs.addElement( javax.help.Map.ID.create( S, hs ));
              else
                {String refs= null;
                 if( S.equals( "htm.ComDes"))
                     refs = ComDesRefStrings;
                 else
                     refs = ExampRefStrings;
                 for( j = 0; j + 1 < refs.length() ; j = refs.indexOf( ';', j+1 ))
                   {int k = refs.indexOf(';', j + 1 );
                    String ref1 = refs.substring( j + 1 , k );
                    j = k;
                    IDs.addElement( javax.help.Map.ID.create( S+ "." + ref1, hs));
                   }
                 } 
              }//for strings in hostStrings
            }//if ! operators Only
          }//if IDs != null

         return IDs.elements();
       }

   

     /** Returns a URL from the id value
     */
     public URL getURLFromID( javax.help.Map.ID id ) throws java.net.MalformedURLException
       {
        String host = "";
        
        if( id.id.startsWith( "Gen" ) ) 
           host = "Generic";
        else if( id.id.startsWith( "Dat" ) ) 
           host = "DataSet";
        else if( id.id.equals( "IsawOpHelp" ) ) 
           return null;
        
        else if( id.id.startsWith( "htm.") && !operatorsOnly)
           { int k = id.id.indexOf( '.',4);
             
             String ref ="";
             if( k < 0)
               k = id.id.length();
             else
                ref = "#" + id.id.substring( k + 1 );
             
             return new URL( helpPath + id.id.substring( 4 , k ) + ".html" + ref );
            }
        else 
           throw new java.net.MalformedURLException( "improper id" );
        
        if( !( id.hs instanceof IsawOpHelpSet ) )
          throw new java.net.MalformedURLException( "improper helpset" );
       
        try{
           int opnum = ( new Integer( id.id.substring( 3 ).trim() ) ).intValue();
          
           return new URL( "Memory" , host , opnum , "x" , urlh );
            }
        catch( Exception ss )
          {throw new java.net.MalformedURLException( "improper id number" );
          }
       }



     //Returns the Name part of the filename, stripping off extension and path
     private String getFname( String longfilename)
       {
        int i = longfilename.lastIndexOf( '.');
        if( i < 0)
           return longfilename;
        if( !longfilename.substring( i+1).toUpperCase().equals("HTML"))
           return null;
        int j = longfilename.lastIndexOf(".\\/",i-1);
        if( j < 0) 
           return longfilename.substring(0, i);
        else 
           return longfilename.substring( j+1,i);

       }



     //Returns the Id associated with a given url.  It does not check if it is valid
     private String getIdfrURL( URL url )
       {String S;

        if( url.getProtocol() .equals( "http") && !operatorsOnly)

          {
           S = "htm.";
           String File =getFname( url.getFile());
           String Ref = url.getRef();
           if( File == null) 
              return null;
           if( ! url.getHost().equals("www.pns.anl.gov"))
              return null;
           if( ! url.getFile().startsWith("isaw/"))
              return null;
           S += File;
           if( Ref == null)
              return S;
           if( Ref.length() < 1)
               return S;
           return S + "." + Ref;
             
          }


        else if( url.getProtocol().equals( "file")&& !operatorsOnly)
           S ="htm."+getFname( url.getFile());

        else if( url.getProtocol().equals( "Memory" ))

           if( url.getHost().equals("Generic"))
             S ="Gen"+url.getPort();

           else if( url.getHost().equals( "DataSet"))
             S ="Dat"+url.getPort();
         return null;
          
       }

     /** Determines if a given URL has an associated ID in this map
     */
     public boolean isID( java.net.URL url )
       {
        if( !url.getProtocol().equals( "Memory" ) && !operatorsOnly) 

           if( getIdfrURL( url) == null)
              return false;
           else
              return isValidID( getIdfrURL( url), hs) ;
                 
        if( url.getHost().equals( "Generic" ) )

          {int port = url.getPort();
           if( port < 0 ) 
              return false;
           if( port >= ngeneric ) 
              return false;
           return true;

          }

        else if( url.getHost().equals( "DataSet")  )

          {int port = url.getPort();
           if( port < 0 ) 
              return false;
           if( port >= ndataset ) 
              return false;
           return true;
           }
        return false;
       }


     /**Returns the id associated with a given URL in this map, if any
     */
     public javax.help.Map.ID getIDFromURL( java.net.URL url )
       {
        if( url == null ) 
           return null;
         
        if( !url.getProtocol().equals( "Memory" ))

           if(!operatorsOnly) 

              return null;

           else

            {
              String id = getIdfrURL( url);
              if( id == null)
                 return null;

              return javax.help.Map.ID.create( id, hs);
             }

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


     /** Returns an Enumeration of javax.help.Map.ID's. I hope this is what they want
     */
     public Enumeration getIDs( java.net.URL url ) 
       {
        Vector V = new Vector();
        if( url == null ) 
           return null;

        if( !url.getProtocol().equals( "Memory" ))

           if(!operatorsOnly )

              return null;

           else

             { 
              String id = getIdfrURL( url);
              if( id == null)
                 return null;
 
              if( !id.startsWith("htm."))
                 return null;

              int j = id.indexOf('.',4);
              String host,ref=null;
              if( j < 0)
                 host= id.substring(4);
              else
                {host = id.substring( 4,j);
                 ref = id.substring( j+1);
                }

              if( host.equals( "Gramer" ) || host.equals( "Attributes" ) ||
                  host.equals( "Interfaces" ) || (ref != null))
                {
                 V.addElement( id );
                 return V.elements();
                }
             }

        String host = url.getHost();
        if( ( host.equals( "Generic" ) ) || ( host.equals( "DataSet" ) ) )

          {host = host.substring( 0 , 3 );
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
     JHelp jh = new JHelp( new IsawOpHelpSet( false ) );
     jf.setSize( 500 , 500 );
     jf.getContentPane().add( jh );
     jf.show();
    }
 }//class IsawOpHelpSet


/** This is the TOC view used by IsawOpHelpSet.  There is NO file.  The information is
*   created in memory
*/
class IsawTOC  extends TOCView
 {HelpSet hs;
  static Script_Class_List_Handler sh;
  int ngeneric , ndatasets;
  static DefaultMutableTreeNode  Node = null;
  static boolean operatorsOnly = false;


  /** Constructor.  sh contains all the information needed for the operators
  */
  public IsawTOC( HelpSet hs , String name , String label , java.util.Hashtable params ,
         Script_Class_List_Handler sh , boolean operatorsOnly)
    {
     super( hs , name , label , params );
     this.hs = hs;
     this.sh = sh;
     this.operatorsOnly = operatorsOnly;
     ngeneric = sh.getNum_operators();
     ndatasets = sh.getNumDataSetOperators(); 
  
     Node = getTOC( ngeneric , ndatasets , hs , operatorsOnly );
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
  public static DefaultMutableTreeNode parse( java.net.URL url , HelpSet hs , Locale locale ,
                                                            TreeItemFactory factory )
    {if( Node == null)
       {
         sh = new Script_Class_List_Handler();
         int ngeneric = sh.getNum_operators();
         int ndatasets = sh.getNumDataSetOperators();  
         Node = getTOC( ngeneric, ndatasets, hs , operatorsOnly);  

        }

     return Node;
    }


  /** Recursive Binary search. end not an option, start has not been checked*/
  private static int find( DefaultMutableTreeNode parent , String Name , int start , int end )
    {
     if( start < 0 ) 
        return -1;

     if( end < 0 ) 
        return -1;
     
     if( start >= parent.getChildCount() ) 
        return parent.getChildCount();

     if( end <= start )
        return -1;

     
     int mid = ( start + end - 1 )/2;
     String S =( ( TOCItem) ( ( DefaultMutableTreeNode )( parent.getChildAt( mid ))).
                                                                      getUserObject()).getName();
     if( S.equals( Name ) ) 
        return mid;

     if( S.compareTo( Name ) < 0)

       {if( mid + 1 >= end )
           return end ;

        else
           return find( parent , Name , mid + 1 , end );
        }
     else if( S.compareTo( Name ) > 0 )
       {if( start >= mid - 1 )

           return start;

        else

           return find( parent , Name , start , mid  );
       }
     else 

        return mid;

    }


  /** Inserts a toc item at the end of a category list of an operator. New Nodes are created
  *   if the category is not present.
  */
  private static void insert( DefaultMutableTreeNode parent , TOCItem toc , String[] cat , 
                              int catposition )
    {
     DefaultMutableTreeNode node;
     if( catposition >= cat.length )
       {
        node = new DefaultMutableTreeNode( toc );
        parent.add( node );
        return;
       }

     int pos = find( parent , cat[ catposition ] , 0 , parent.getChildCount( ) );
     if( pos < 0 ) 
        return;

     if( pos >= parent.getChildCount( ) )
        {
         node = addNode( parent , cat[ catposition ] , null , null );
        }

     else if( pos < 0 )
        node = null;

     else 
       {
        TreeNode tn = parent.getChildAt( pos );
        TOCItem tocx = (TOCItem)(( DefaultMutableTreeNode )tn ).getUserObject();

        if( tocx.getName().equals( cat[ catposition ] ) )

           node = ( DefaultMutableTreeNode )tn;
        else

          {
           tocx = new TOCItem();
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
  private static  DefaultMutableTreeNode addNode( DefaultMutableTreeNode parent , String Name ,
                         javax.help.Map.ID id , HelpSet hs )
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


  /**
  * Calculated the Table of Contents(TOC) from the Script_Class_List_Handler
  */
  public static DefaultMutableTreeNode getTOC( int ngeneric , int ndatasets , HelpSet hs , 
        boolean operatorsOnly )
    {
     DefaultMutableTreeNode topNode , parentNode;
     parentNode = new DefaultMutableTreeNode();
     TOCItem top = new TOCItem();
     top.setName( "Top" );
     topNode = new DefaultMutableTreeNode( top );
     DefaultMutableTreeNode opParent = topNode;

     if( ! operatorsOnly)
       {
        DefaultMutableTreeNode command = addNode( topNode , "Scripting/Operators", null, null );
        addNode( command, "OverView", javax.help.Map.ID.create( "htm.CommandPane.OverView" , hs ) ,
                hs );

        DefaultMutableTreeNode  operations = addNode( command, "Commands", null, null);
             
           addNode( operations, "Arithmetic,Logic and Assignment Operations" ,
                 javax.help.Map.ID.create( "htm.ComDes", hs ) , hs );

          
           DefaultMutableTreeNode subs = addNode( operations, "Subroutines" , null , null );
                 
           addNode( subs, "Display" ,
                 javax.help.Map.ID.create( "htm.ComDes.Display", hs ) , hs );

           addNode( subs, "Load" ,
                 javax.help.Map.ID.create( "htm.ComDes.Load", hs ) , hs );

           addNode( subs, "Return" ,
                 javax.help.Map.ID.create( "htm.ComDes.Return", hs ) , hs );

           addNode( subs, "Save" ,
                 javax.help.Map.ID.create( "htm.ComDes.Save", hs ) , hs ); 

           addNode( subs, "Send" ,
                 javax.help.Map.ID.create( "htm.ComDes.Send", hs ) , hs ); 
         

           opParent = operations;

        addNode( command, "Syntax", javax.help.Map.ID.create( "htm.Grammer" , hs ) ,
                hs );

        addNode( command, "Interfaces", javax.help.Map.ID.create( "htm.Interfaces" , hs ) ,
                hs );

        DefaultMutableTreeNode  Examples= addNode( command, "Examples", null, null);
           
           addNode( Examples , "Arithmetic, Assignment, Strings" ,
                 javax.help.Map.ID.create( "htm.Examples1.Aritmetic", hs ) , hs );
           addNode( Examples, "Arrays" ,
                 javax.help.Map.ID.create( "htm.Examples1.Arrays", hs ) , hs );
           addNode( Examples, "For loops" ,
                 javax.help.Map.ID.create( "htm.Examples1.For", hs ) , hs );
           addNode( Examples, "if-then-else" ,
                 javax.help.Map.ID.create( "htm.Examples1.if", hs ) , hs );
           addNode( Examples, "on error" ,
                 javax.help.Map.ID.create( "htm.Examples1.error", hs ) , hs );
           addNode( Examples, "Parameters" ,
                 javax.help.Map.ID.create( "htm.Examples1.Parameters", hs ) , hs );
           addNode( Examples, "Batch" ,
                 javax.help.Map.ID.create( "htm.Examples1.Batch", hs ) , hs );
           addNode( Examples, "Data Sets (simple)" ,
                 javax.help.Map.ID.create( "htm.Examples1.DataSetsSimple", hs ) , hs );
           addNode( Examples, "Data Sets (advanced)" ,
                 javax.help.Map.ID.create( "htm.Examples1.DataSetAdvance", hs ) , hs );



        addNode( command, "Attributes", javax.help.Map.ID.create( "htm.Attributes" , hs ) ,
                hs );

       }
      
     DefaultMutableTreeNode operators = addNode( opParent , "operators" , null , null );
     DefaultMutableTreeNode Alphabetic = addNode( operators , "Alphabetic" , null , null );
     DefaultMutableTreeNode Category = addNode( operators , "Category" , null , null );
     DefaultMutableTreeNode Generic = addNode( Alphabetic , "Generic" , null , null );
     DefaultMutableTreeNode DataSet = addNode( Alphabetic , "DataSet" , null , null );
     DefaultMutableTreeNode letters = addNode( Generic , "A-E" , null , null );
     char lastletter = 'E';  
       
     for( int i = 0 ; i < ngeneric ; i++  )
       {
        Operator op  = sh.getOperator( i );
        String title = op.getCommand();
        if( title == null ) title = "";
        if( title.length() >= 1 )
           while( title.toUpperCase().charAt(0 ) > lastletter )
             {
              String range = ((char)( lastletter + 1 ) + "-" + (char)( lastletter + 5 ) );
              letters = addNode( Generic , range , null , null );
              
              lastletter = (char)( lastletter + 5 ); 
             }

        String idd = "Gen" + i;
        TOCItem ttt = (TOCItem)addNode( letters , title ,  
                     javax.help.Map.ID.create( "Gen" + i , hs ) , hs ).getUserObject();
        String[] cat = op.getCategoryList();
        insert( Category , ttt , cat , 1 );
           
       }

     //Now Add to the Data set nodes
     letters = addNode( DataSet , "A-E" , null , null );
     lastletter = 'E';  
      
     for( int i = 0 ;i < ndatasets ; i++ )
       {Operator op  = sh.getDataSetOperator( i );
        String title = op.getCommand();
        if( title == null ) 
           title = "";
          
        if( title.length() >= 1 )
           while( title.toUpperCase().charAt(0 ) > lastletter )
             {
              String range = ((char)( lastletter + 1 ) + "-" + (char)( lastletter + 5 ) );
              letters = addNode( DataSet , range , null , null );
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
