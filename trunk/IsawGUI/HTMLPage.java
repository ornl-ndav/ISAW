/*
 * File: HTMLPage.java 
 *             
 * Copyright (C) 1999-2001, Alok Chatterjee, Ruth Mikkelson
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
 * Contact : Alok Chatterjee achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
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
 * Revision 1.9  2002/06/11 21:37:57  rmikk
 * Used StringBuffer to optimize and speed up creating
 *   HTML pages
 *
 * Revision 1.8  2002/05/03 15:24:57  rmikk
 * The pages describing specific function have the prompt for the parameter
 *    included with the data type of the parameter
 *
 * The pages describing ScriptOperators can now view the given script
 *
 * Revision 1.7  2002/05/03 14:33:26  rmikk
 * The help pages describing specific operators now has the prompt string
 *   to the right of the data type for each argument
 *
 * The help pages for ScriptOperators allow for the display of the script.
 *
 * Revision 1.6  2001/11/12 21:36:54  dennis
 *   1. Fixed the "BACK" in The help page with the list of all
 *      installed commands.
 *
 *   2. Added GPL and some java-doc's.
 *
 */

package IsawGUI;


import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;
import java.net.*;
import Command.*;
import DataSetTools.operator.*;


/** Creates a pop up window that can display rudimentary HTML pages or pages that are
 *   customized to help on the operators.
 */
public
class HTMLPage extends JFrame
{
   private
   JEditorPane editorPane = new JEditorPane();
   String OperatorPane = null;

   /** Constructor and creator of the HTML viewer.
    *
    *@param  url  The url of the original page
    *
    *NOTE: This HTML Page has been customized as follows <OL Type = "A">
    *<LI> If the href contains XX$netscape, the browser will appear pointing to the java docs for
    *     the data set operators
    *<LI> If the href contains XX$Panel1, Text will appear listing all the installed Generic 
    *     operators with links for each of the operators containg further information
    *<LI> if the href is XX$Panel2 with reference ref, text will appear giving further information
    *     on the ref's operator.  This information includes the full class name or filename if
    *     it is a script, the command, the title, and the argument data types.     
    */
   public
   HTMLPage( String url ) {
      Container contentPane = getContentPane ();
		
      try
      { 
         editorPane.setPage ( url );
      }
      catch( IOException ex )
      {
         ex.printStackTrace (); 
         return;
      }

      contentPane.add ( new JScrollPane( editorPane ), 
         BorderLayout.CENTER );

      editorPane.setEditable ( false );

      editorPane.addHyperlinkListener ( new HyperlinkListener()
         {
            public
            void hyperlinkUpdate( HyperlinkEvent e ) {
               if( e.getEventType () == HyperlinkEvent.EventType.ACTIVATED ) 
               {
                  try
                  {
                     URL U = e.getURL ();

                     if( U.getFile ().indexOf ( "XX$netscape" ) >= 0 )
                     {
                        BrowserControl BC = new BrowserControl();
                        String Fname = U.getFile ();

                        Fname = Fname.replace ( '\\', '/' );
                        int k = Fname.lastIndexOf (
                              "IsawHelp/Command/XX$netscape" );

                        if( k < 0 ) return;
                        Fname = Fname.substring ( 0, k );
                        Fname += "docs/html/DataSetTools/operator/Operator.html";
                        //U = new URL( "file://"+Fname);
                        //System.out.println("URL=file://"+Fname);
                        BC.displayURL ( "file://" + Fname );
                        return;
                     }
                     else if( U.getFile ().indexOf ( "XX$Panel1" ) >= 0 )
                     {
                        SetText ( editorPane );
                     }
                     else if( U.getFile ().indexOf ( "XX$Panel2" ) >= 0 )
                     { 
                        SetText ( editorPane, U.getRef () );
                     }
                     else if( U.getFile ().indexOf ( "XX$ScriptFile" ) >= 0 )
                     {
                        ShowFile ( U.getRef () );
                     }
                     else if( U.getFile ().indexOf ( "XX$Panel3" ) >= 0 )
                        SetTextBack ( editorPane );
                     else
                        editorPane.setPage ( e.getURL () );
                  }
                  catch( IOException ex )
                  {
                     ex.printStackTrace ();
                  }
               }
            }
         }
      );
   }

   private
   void SetText( JEditorPane JP, String ref ) {
      Script_Class_List_Handler SH = new Script_Class_List_Handler();
      String Textt = "<HTML><BODY><A href=\"XX$Panel1\">BACK</a><P>";

      Textt += " <Center><H1>Operator</H1></Center><P>";
      int k;

      try
      {
         k = new Integer( ref ).intValue ();
      }
      catch( Exception s )
      {
         return;
      }
      Operator O = SH.getOperator ( k );
      String filename = null;

      if( O instanceof ScriptOperator )
      {
         filename = ( ( ScriptOperator ) O ).getFileName ();
         Textt += "ScriptOperator from file <A href=XX$ScriptFile#" + filename +
               ">" + ( ( ScriptOperator ) O ).getFileName () + "</a><BR>";
      }
      else
         Textt += "Java Operator. Class=" + O.getClass ().toString () + "<BR>";
      Textt += "Title( in Menu's etc.) =" + O.getTitle () + "<BR>";
      Textt += "Command(in commandPane) =" + O.getCommand () + "<P><P>";
      Textt += "<Center><H3> Object " + O.getCommand () + "( <BR>";
     
      int n = SH.getNumParameters ( k );
    
      for( int i = 0; i < n; i++ )
      {
         Object XX = SH.getOperatorParameter ( k, i );
         Parameter P = O.getParameter ( i );
         String Prompt = P.getName ();

         if( XX == null )
            Textt += " <U>Object</U> " + Prompt;
         else
            Textt += "<U>" + XX.getClass ().toString () + "</U>  " + Prompt;
         if( i < n - 1 )
            Textt += " , ";   
         Textt += "<BR>";   
      
      }
      Textt += " ) ";
      Textt += "</h3></Center></body></html>";
    
      JP.setText ( Textt );
   }

   private
   void setUpOpPanel() {
      Script_Class_List_Handler SH = new Script_Class_List_Handler();
      StringBuffer SB= new StringBuffer(5000);
      SB.append( "<HTML><BODY><A href=\"XX$Panel3\">BACK</a><P>");

      SB.append(" <Center><H1>Current Operators</H1></Center>");
      int n = SH.getNum_operators ();
      int nrows = n / 4;
      int ncols = 4;

      if( nrows > 15 )
      {
         nrows = 15;
         ncols = n / nrows;
         if( nrows * ncols < n ) 
            ncols++;
      }
      else if( nrows < 5 )
      {
         nrows = 5;
         ncols = n / nrows;
         if( nrows * ncols < n ) 
             ncols++;       
      }
      SB.append( "<table> <tr><td>");
      for( int i = 0; i < n; i++ )
      {
         Operator O = SH.getOperator ( i );
         String command = O.getCommand ();

         SB.append( "<A href=\"XX$Panel2#" + i + "\">" + command + "</a><BR>");
         if( nrows * ( ( i + 1 ) / nrows ) == i + 1 )
         {
            SB.append( "</TD>");
            if( i < n - 1 )
               SB.append( "<TD>");
         }
      }
      if( nrows * n / nrows != n )
         SB.append( "</TD>");
      SB.append("</TR></table></body></html>");
      OperatorPane = SB.toString();
   }

   private
   void SetText( JEditorPane JP ) {
      if( OperatorPane == null )
         setUpOpPanel ();   
      JP.setText ( OperatorPane );
     
   }

   private
   void SetTextBack( JEditorPane JP ) {
      String P = "<HTML><BODY><A href=\"CommandPane.html\"> CONTENTS </a><P>";

      P += "<h3><Center> Commands</center></H3>";

      P += "<OL Type=\"A\">";
      P += "<LI> Arithmetic,Logic and Assignment Operations; <A href=\"ComDes.html\">+,-, *, /, & ,";
      P += " ^(power) and = </a>, AND, Or, Not<P>";
      P += "<LI> Subroutines:";
      P += "<ul>";
      P += "<A href=\"ComDes.html#Load\">Load</a>   ,    <A href=\"ComDes.html#Display\">Display</a> ,";
      P += " <A href=\"ComDes.html#Send\">Send</a> ,  <A href=\"ComDes.html#Save\">Save</a><P>";
      P += "</ul>";
      P += "<LI> Possible Data Set Operations.  Not all are available for a specific data set";
   
      P += "<ul> <li><A href = \"XX$netscape\">Browse documentation for operators written in Java</a>";
      P += "<li> <A href =\"XX$Panel1\">List of available commands with argument data types</a>";
      P += "       <BR><Center> Wait the first time </Center>";
      P += "</ul>";

      P += "</ol>";

      P += "</body></html>";

      JP.setText ( P );
   }

   private
   void ShowFile( String filename ) {
      JFrame jf = new JFrame( filename );
      JTextArea jt = new JTextArea( 20, 60 );

      jt.setEditable ( false );
      jt.setDocument ( new IsawGUI.Util().openDoc ( filename ) );
      jf.getContentPane ().add ( new JScrollPane( jt ) );
      jf.setSize ( 600, 500 );
      jf.show ();
      jf.validate ();

   }

   public static
   void main( String args[] ) {
      GJApp.launch ( new HTMLPage( "http://www.pns.anl.gov/gppd/index.htm" ), 
         "JEditorPane", 300, 300, 450, 300 );

   }
}


class GJApp extends WindowAdapter
{
   static private
   JPanel statusArea = new JPanel();
   static private
   JLabel status = new JLabel( " " );
   static private
   ResourceBundle resources;

   public static
   void launch( final JFrame f, String title,
      final int x, final int y, 
      final int w, int h ) {
      launch ( f, title, x, y, w, h, null );	
   }

   public static
   void launch( final JFrame f, String title,
      final int x, final int y, 
      final int w, int h,
      String propertiesFilename ) {
      f.setTitle ( title );
      f.setBounds ( x, y, w, h );
      f.setVisible ( true );

      statusArea.setBorder ( BorderFactory.createEtchedBorder () );
      statusArea.setLayout ( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      statusArea.add ( status );
      status.setHorizontalAlignment ( JLabel.LEFT );

      f.setDefaultCloseOperation (
         WindowConstants.DISPOSE_ON_CLOSE );

      if( propertiesFilename != null )
      {
         resources = ResourceBundle.getBundle (
                  propertiesFilename, Locale.getDefault () );
      }

      f.addWindowListener ( new WindowAdapter()
         {
            public
            void windowClosed( WindowEvent e ) {
               System.exit ( 0 );
            }
         }
      );
   }

   static public
   JPanel getStatusArea() {
      return statusArea;
   }

   static public
   void showStatus( String s ) {
      status.setText ( s );
   }

   static
   Object getResource( String key ) {
      if( resources != null )
      {
         return resources.getString ( key );
      }
      return null;
   }
      
}

