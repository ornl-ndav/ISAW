/*
 * File:  HTMLizer.java
 *
 * Copyright (C) 2002, Christopher M. Bouzek
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
 * Contact : Christopher M. Bouzek <coldfusion78@yahoo.com> or
 *           Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.18  2003/07/03 16:25:56  bouzekc
 * Added all missing javadocs and rearranged methods according
 * to access privilege.
 *
 * Revision 1.17  2003/06/25 20:37:28  bouzekc
 * Reformatted for indenting and spacing consistency.
 *
 * Revision 1.16  2003/06/25 20:33:12  bouzekc
 * Removed several unused private variables.  Removed
 * unnecessary instantiation of a String.
 *
 * Revision 1.15  2003/06/13 22:59:28  bouzekc
 * Added processing of date information for scripts.  Removed
 * yet more embedded tabs.
 *
 * Revision 1.14  2003/06/10 20:57:51  bouzekc
 * Fixed ArrayIndexOutOfBoundsException which occurred if the
 * number of @param's did not match the number or actual
 * parameters.  Removed some embedded tabs and fixed some
 * indenting problems.
 *
 * Revision 1.13  2003/03/06 23:25:41  pfpeterson
 * Changed call from fixSeparator to appropriate method.
 *
 * Revision 1.12  2003/01/29 17:47:34  dennis
 * Removed found_param and found_error conditionals to fix problem with
 * <br> tag and simplify code for bulleted lists. (Chris Bouzek)
 *
 * Revision 1.11  2002/12/10 17:55:52  pfpeterson
 * Speed improvements. This is done by moving everything out of the constructor and
 * making variables static. op_vector and help_dir are only filled in right before
 * they are used (which is never from the ISAW main window). This cuts the time for
 * normal use of this operator from a couple of seconds to a couple of mili-seconds.
 *
 * Revision 1.10  2002/12/10 16:49:41  pfpeterson
 * Fixed formating and some javadocs.
 *
 * Revision 1.9  2002/12/10 15:12:10  pfpeterson
 * Added method to clean up operator documentation before turning them into HTML.
 *
 * Revision 1.8  2002/12/09 16:45:10  pfpeterson
 * Put parameter type in command line in italics.
 *
 * Revision 1.7  2002/12/09 15:50:45  pfpeterson
 * Small changes in the formatting of the HTML code and shortened some lines
 * in the source. Changes to the HTML are:
 *  - closing parenthesis in the command name in the case when there are no
 * parameters
 *  - commented out HTML comments in the source code.
 *
 * Revision 1.6  2002/12/06 20:07:00  dennis
 * -No longer creates a file when createHTML() is called.
 * -Puts bullet points in the Parameter and Error lists.
 * -Puts the data type of the parameter in the parameter
 *  list, both for operators that have documentation and
 *  those that have only the default documentation.
 * -Put the title of the operator in one of the blue
 *  heading tables.
 * -Only the class name and command are listed under the
 *  top heading table.
 * -Put the parameter value (i.e. String) and name (i.e.
 *  "Sample Composition") in the Command: part of the
 *  documentation. (Chris Bouzek)
 *
 * Revision 1.5  2002/12/02 19:56:16  pfpeterson
 * Fixed bug where wasn't appropriately using the IsawHelp directory.
 *
 * Revision 1.4  2002/12/02 17:26:16  pfpeterson
 * Made the html file creation *slightly* more robust. Also checks for empty/default
 * documentation and returns more appropriate message.
 *
 * Revision 1.3  2002/11/27 23:27:28  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/11/26 22:15:26  dennis
 * HTML format of documentation improved.
 * Added method to return HTML formatted string for dynamically
 * generated help set. (Chris Bouzek, Mike Miller)
 *
 * Revision 1.1  2002/10/28 23:40:59  dennis
 * Generate html help documentation from the getDocumentation() method
 * for operators.  The documentation will be created in one of the directories:
 *   <Isaw_Help>/HelpSystem/html              (if Isaw_Help is defined)
 *   <ISAW_HOME>/IsawHelp/HelpSystem/html     (if ISAW_HOME is defined)
 *   ./HelpSystem/html                        (default if previous not defined)
 * The target directory MUST exist before running this program.  An operator
 * class may be specified on the command line.  If none is specified, the html
 * pages will be generated for ALL operators.
 *
 *
 */
package IsawHelp.HelpSystem;

import Command.Script_Class_List_Handler;

import DataSetTools.operator.*;

import DataSetTools.operator.DataSet.*;

import DataSetTools.util.*;

import java.io.*;

import java.util.*;


/**
 * This class takes a String input from an Operator's getDocumentation() method
 * and converts it into an HTML file to be used with JavaHelp 1.1.3.
 */
public class HTMLizer {
  //~ Static fields/initializers ***********************************************

  private static String help_dir  = null;
  private static Vector op_vector = null;

  //~ Instance fields **********************************************************

  private File help_out  = null;
  private FileWriter out = null;

  //~ Constructors *************************************************************

  /* -----------------CONSTRUCTOR----------------------------------------- */

  /**
   * Constructs an HTMLizer. This constructor does nothing.
   */
  public HTMLizer(  ) {
    // does nothing
  }

  //~ Methods ******************************************************************

  /* ---------------------- createAllHelpFiles --------------------------- */

  /**
   * Creates the HTML files which consists of the information in an Operator's
   * getDocumentation() method.  Documentation for all Operators is produced.
   */
  public void createAllHelpFiles(  ) {
    // create the list of operators if it doesn't already exist
    if( ( op_vector == null ) || ( op_vector.size(  ) == 0 ) ) {
      op_vector = createOperatorVector(  );
    }

    int op_vector_size = op_vector.size(  );
    Operator op;
    String op_class;

    for( int i = 0; i < op_vector_size; i++ ) {
      op         = ( Operator )op_vector.get( i );  // get the jth Operator
      op_class   = trimClassName( op.getClass(  ).toString(  ) );

      writeFile( op_class, createHTML( op ) );
    }
  }

  /**
   * Formats the documentation of the Operator op by using HTML tags.
   *
   * @param op The Operator for which you wish to create HTML documentation
   *        for.
   *
   * @return the String consisting of the HTML formatted documentation
   */
  public String createHTML( Operator op ) {
    StringBuffer html;
    String class_name;
    String title;
    String docs;
    Vector[] v;
    int num_params;

    html         = new StringBuffer(  );
    class_name   = op.getClass(  )
                     .toString(  );
    title        = op.getTitle(  );
    v            = this.getParameterInfoList( op );
    docs         = cleanDocumentation( op.getDocumentation(  ) );
    num_params   = v[0].size(  );

    // remove the "class" from class_name
    class_name = class_name.substring( 
        class_name.indexOf( " " ) + 1, class_name.length(  ) );

    html.append( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Frameset//EN\"" );
    html.append( "\"http://www.w3.org/TR/REC-html40/frameset.dtd\">\n" );
    html.append( "<html>\n" );
    html.append( "<head>\n" );
    html.append( "<title>\n" );
    html.append( "ISAW " );
    html.append( class_name );
    html.append( "</title>\n" );
    html.append( "</head>\n" );
    html.append( "<body BGCOLOR=#FFFFFF>\n" );

    html.append( 
      "<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n" );
    html.append( "<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n" );
    html.append( "<td COLSPAN=1 ALIGN=CENTER><font SIZE=\"+3\">\n" );
    html.append( "<b>" );
    html.append( title );
    html.append( "</b></font></td>\n" );
    html.append( "</tr>\n" );
    html.append( "</table>\n\n<br>\n" );

    html.append( "<b>Class:</b> " );
    html.append( class_name );
    html.append( "<br>\n" );

    html.append( "<b>Command:</b> " );
    html.append( op.getCommand(  ) );

    html.append( "(" );

    for( int i = 0; i < num_params; i++ ) {
      html.append( "<I>" + v[0].elementAt( i ) + "</I> " );
      html.append( v[1].elementAt( i ) );

      if( i == ( num_params - 1 ) ) {
        html.append( ")\n" );
      } else {
        html.append( ", " );
      }
    }

    if( num_params == 0 ) {
      html.append( ")\n" );
    }

    html.append( " <br><br>\n" );

    // get the actual documentation
    // check that there is something to work with
    if( docs == null ) {
      html.append( createDefaultDocs( v ) );
    } else {
      html.append( convertToHTML( docs, v ) );
    }

    html.append( "</body>\n" );
    html.append( "</html>" );

    return html.toString(  );
  }

  /**
   * Creates a help file for a single Operator op.
   *
   * @param op_class The Operator class name which denotes the Operator you
   *        wish to create documentation for.
   */
  public void createOneHelpFile( String op_class ) {
    String s = dynamicDocCreation( op_class );

    if( s != null ) {
      writeFile( op_class, s );
    }
  }

  /**
   * Create a list of Operators comprised of all "add-on" operators and all of
   * the standard DataSet Operators.
   *
   * @return A Vector representation of the Operator list.
   */
  public static Vector createOperatorVector(  ) {
    final int TOTAL_OPERATORS = 100;
    final int INC_AMOUNT      = 20;

    // script handler for the operator list
    Script_Class_List_Handler base_op_handler = new Script_Class_List_Handler(  );

    Vector op_vector    = new Vector( TOTAL_OPERATORS, INC_AMOUNT );
    int num_add_on_ops  = base_op_handler.getNum_operators(  );
    int num_dataset_ops = base_op_handler.getNumDataSetOperators(  );

    // get all add-on operators
    for( int j = 0; j < num_add_on_ops; j++ ) {
      op_vector.add( j, base_op_handler.getOperator( j ) );
    }

    int num_operators = op_vector.size(  );

    // get the DataSet operators
    for( int k = 0; k < num_dataset_ops; k++ ) {
      op_vector.add( 
        k + num_operators, base_op_handler.getDataSetOperator( k ) );
    }

    return op_vector;
  }

  /**
   * Dynamically creates documentation for a single Operator.
   *
   * @param op_in_class The Operator to create documentation for.
   *
   * @return HTML formatted Operator documentation.
   */
  public String dynamicDocCreation( String op_in_class ) {
    // create the list of operators if it doesn't already exist
    if( ( op_vector == null ) || ( op_vector.size(  ) == 0 ) ) {
      op_vector = createOperatorVector(  );
    }

    int op_vector_size = op_vector.size(  );
    Operator op;
    String op_class;

    for( int i = 0; i < op_vector_size; i++ ) {
      op   = ( Operator )op_vector.get( i );  // get the jth Operator

      op_class = trimClassName( op.getClass(  ).toString(  ) );

      // found our operator
      if( op_class.equals( op_in_class ) ) {
        return createHTML( op );
      }
    }

    return null;
  }

  /**
   * Main method for running class as a standalone program.
   */
  public static void main( String[] args ) {
    System.out.println(  );
    System.out.println( "Creating JavaHelp HTML documentation....please wait." );

    HTMLizer helpfile = new HTMLizer(  );

    if( args.length <= 0 ) {  //create documentation for all operators
      helpfile.createAllHelpFiles(  );
    } else {  //create documentation for one operator
      helpfile.createOneHelpFile( args[0] );
    }

    System.exit( 0 );
  }

  /**
   * Trims the full class path given by class_name.
   *
   * @param class_name The full class name which you wish to trim.
   *
   * @return The String which consists of the trimmed down class name.
   */
  public String trimClassName( String class_name ) {
    StringTokenizer st = new StringTokenizer( class_name, "." );
    String name        = class_name;

    while( st.hasMoreTokens(  ) ) {
      name = st.nextToken( "." );
    }

    return name;
  }

  /**
   * Utility to writes the HTML Operator documentation file.  You must send
   * this method the html-formatted documentation.
   *
   * @param operator_class The Operator to write the HTML-formatted
   *        documentation file for.
   * @param body The HTML-formatted documentation.
   *
   * @return boolean indicating success or failure.
   */
  public boolean writeFile( String operator_class, String body ) {
    if( ( help_dir == null ) || ( help_dir.length(  ) == 0 ) ) {
      try {
        String isaw_help = SharedData.getProperty( "Help_Directory" );

        if( isaw_help != null ) {
          help_dir = isaw_help + "/HelpSystem/html";
        } else {
          isaw_help = SharedData.getProperty( "ISAW_HOME" );

          if( isaw_help != null ) {
            help_dir = isaw_help + "/IsawHelp/HelpSystem/html";
          } else {
            help_dir = "/HelpSystem/html";
          }
        }

        // SharedData.addmsg("Writing files to " + help_dir);
      } catch( RuntimeException e ) {
        e.printStackTrace(  );
      }

      //catch
      help_dir = FilenameUtil.setForwardSlash( help_dir );
    }

    try {
      help_out   = new File( help_dir, operator_class + "Help.html" );
      out        = new FileWriter( help_out );
      out.write( body );
      out.close(  );

      return true;
    } catch( FileNotFoundException e ) {
      return false;
    } catch( Exception e ) {
      e.printStackTrace(  );

      return false;
    }
  }

  /**
   * Checks to see if the Operator documentation is an empty tag.
   *
   * @param tag The tag to check.
   *
   * @return true if the tag is empty, false otherwise
   */
  private boolean isEmptyTag( String tag ) {
    if( ( tag == null ) || ( tag.length(  ) == 0 ) ) {
      return true;
    }

    if( tag.indexOf( "@overview" ) == 0 ) {
      if( tag.length(  ) == 9 ) {
        return true;
      }

      tag = tag.substring( 9 )
               .trim(  );

      if( tag.length(  ) == 0 ) {
        return true;
      } else {
        return false;
      }
    } else if( tag.indexOf( "@assumptions" ) == 0 ) {
      if( tag.length(  ) == 12 ) {
        return true;
      }

      tag = tag.substring( 12 )
               .trim(  );

      if( tag.length(  ) == 0 ) {
        return true;
      } else {
        return false;
      }
    } else if( tag.indexOf( "@algorithm" ) == 0 ) {
      if( tag.length(  ) == 10 ) {
        return true;
      }

      tag = tag.substring( 10 )
               .trim(  );

      if( tag.length(  ) == 0 ) {
        return true;
      } else {
        return false;
      }
    } else if( tag.indexOf( "@param" ) == 0 ) {
      if( tag.length(  ) == 6 ) {
        return true;
      }

      tag = tag.substring( 6 )
               .trim(  );

      if( tag.length(  ) == 0 ) {
        return true;
      } else {
        return false;
      }
    } else if( tag.indexOf( "@return" ) == 0 ) {
      if( tag.length(  ) == 7 ) {
        return true;
      }

      tag = tag.substring( 7 )
               .trim(  );

      if( tag.length(  ) == 0 ) {
        return true;
      } else {
        return false;
      }
    } else if( tag.indexOf( "@error" ) == 0 ) {
      if( tag.length(  ) == 6 ) {
        return true;
      }

      tag = tag.substring( 6 )
               .trim(  );

      if( tag.length(  ) == 0 ) {
        return true;
      } else {
        return false;
      }
    } else {
      return true;
    }
  }

  /**
   * Gets information about an Operator's parameters.
   *
   * @param op The Operator to get the information list for.
   *
   * @return an array of Vectors which hold: v[0] = parameter class names v[1]
   *         = parameter names
   */
  private Vector[] getParameterInfoList( Operator op ) {
    String class_s;
    String name_s;
    Vector[] v;
    final int info = 2;
    int num_params;
    Object ob;

    v = new Vector[info];

    for( int i = 0; i < info; i++ ) {
      v[i] = new Vector( 10, 2 );
    }

    num_params = op.getNum_parameters(  );

    // get parameter list; differentiate between DataSetOperators and
    // generic operators
    if( op instanceof DataSetOperator ) {
      v[0].addElement( "DataSet" );
      v[1].addElement( "DataSet for Operator" );
    }

    for( int i = 0; i < num_params; i++ ) {
      //catch any getValue() == null stuff
      ob = op.getParameter( i )
             .getValue(  );

      if( ob == null ) {
        class_s = "";
      } else {
        class_s = ob.getClass(  )
                    .toString(  );
      }

      name_s = op.getParameter( i )
                 .getName(  )
                 .toString(  );
      v[0].addElement( this.trimClassName( class_s ) );
      v[1].addElement( name_s );
    }

    return v;
  }

  /**
   * This method is intended to clean up poorly formatted documentation
   * Strings.
   *
   * @param orig The original String to clean up.
   *
   * @return the cleaned up String.
   */
  private String cleanDocumentation( String orig ) {
    // return null in the cases that this is truly bad
    if( 
      ( orig == null ) || ( orig.length(  ) == 0 ) ||
        orig.equals( Operator.DEFAULT_DOCS ) ) {
      return null;
    }

    // turn the docs into a StringBuffer for easy manipulation
    StringBuffer sb = new StringBuffer( orig );
    int start       = 0;
    int end         = 0;

    // deal with a preamble (if it exists)
    start = sb.toString(  )
              .indexOf( "@" );

    if( start < 0 ) {  // just add an overview tag in front
      sb.insert( 0, "@overview " );
    } else if( start > 0 ) {  // deal with preamble appropriately

      if( sb.toString(  )
              .indexOf( "@overview" ) >= 0 ) {
        sb.delete( 0, start );
      } else {
        sb.insert( 0, "@overview " );
      }
    }

    // cut out empty tags
    start   = sb.toString(  )
                .indexOf( "@" );
    end = sb.toString(  )
            .indexOf( "@", start + 1 );

    while( ( start >= 0 ) && ( end >= 0 ) && ( start < end ) ) {
      if( isEmptyTag( sb.substring( start, end ) ) ) {
        sb.delete( start, end );
        end = sb.toString(  )
                .indexOf( "@", start + 1 );
      } else {
        start   = end;  //sb.toString().indexOf("@",end+1);
        end     = sb.toString(  )
                    .indexOf( "@", start + 1 );
      }

      if( end == -1 ) {
        end = sb.length(  );
      }
    }

    // return what is appropriate
    if( sb.length(  ) == 0 ) {
      return null;
    } else {
      return sb.toString(  );
    }
  }

  /**
   * This method converts a String that follows the conventions in the JavaDoc
   * specifications into an HTML page.  Although other purposes may be found
   * for this method, its primary use is for the JavaHelp System.
   *
   * @param m The String to convert to HTML.
   * @param paramsVec The Vector of parameters.
   *
   * @return the javadoc HTML formatted String.
   */
  private String convertToHTML( String m, Vector[] paramsVec ) {
    // eliminate "garbage" information
    m = m.substring( m.indexOf( '@' ) );

    StringBuffer s = new StringBuffer(  );

    //Date info for the scripts - has format "$ Date ... $"
    int firstDateIndex = m.indexOf( '$' );

    //Date info for the scripts - has format "$ Date ... $"
    int secondDateIndex;

    int header         = m.indexOf( '@' );
    int space          = m.indexOf( ' ' );
    int newline        = m.indexOf( "\n" );
    String header_name;
    String date        = null;
    String table_title = "";
    int param_count    = 0;
    int error_count    = 0;

    //don't do a .equals() twice.  This is supposed to be fast, but we need to
    //check if it is the overview tag for the "$ Date $" stuff
    boolean isOverview = false;

    // check for valid index information
    if( space < 0 ) {
      return "Please insert spaces.";
    }

    if( newline < 0 ) {
      newline = m.length(  );
    }

    //found the "$ Date $" information so process it
    if( firstDateIndex >= 0 ) {
      secondDateIndex   = m.indexOf( '$', firstDateIndex + 1 );

      //get all the date information from between the "$"'s
      date   = m.substring( firstDateIndex + 1, secondDateIndex );

      //now toss that info - we don't want it in the String
      m = m.substring( 0, firstDateIndex ) +
        m.substring( secondDateIndex + 1 );
    }

    // while we have information and the '@' exists
    while( ( header < m.length(  ) ) && ( header >= 0 ) ) {
      header_name = m.substring( header, space );

      if( header_name.equals( "@overview" ) ) {
        table_title   = "Overview";
        param_count   = 0;
        error_count   = 0;
        isOverview    = true;
      } else if( header_name.equals( "@assumptions" ) ) {
        table_title   = "Assumptions";
        param_count   = 0;
        error_count   = 0;
        isOverview    = false;
      } else if( header_name.equals( "@param" ) ) {
        param_count += 1;
        table_title   = "Parameters";
        error_count   = 0;
        isOverview    = false;
      } else if( header_name.equals( "@algorithm" ) ) {
        table_title   = "Algorithm";
        param_count   = 0;
        error_count   = 0;
        isOverview    = false;
      } else if( header_name.equals( "@return" ) ) {
        table_title   = "Returns";
        param_count   = 0;
        error_count   = 0;
        isOverview    = false;
      } else if( header_name.equals( "@error" ) ) {
        error_count += 1;
        table_title   = "Errors";
        param_count   = 0;
        isOverview    = false;
      }

      //determine whether the table has been created already for parameter
      //and error lists
      if( ( param_count <= 1 ) && ( error_count <= 1 ) ) {
        s.append( "<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" " );
        s.append( "WIDTH=\"100%\">\n" );
        s.append( "<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n" );
        s.append( "<td COLSPAN=1><font SIZE=\"+2\">\n" );
        s.append( "<b>" );
        s.append( table_title );
        s.append( "</b></font></td>\n" );
        s.append( "</tr>\n" );
        s.append( "</table>\n\n<br>\n" );

        //start the unordered list if necessary
        if( ( param_count == 1 ) || ( error_count == 1 ) ) {
          s.append( "<ul>" );
        }
      }

      // update the header index-for the loop and for the next conditional
      header = m.indexOf( '@', header + 1 );

      if( header < 0 ) {
        header = m.length(  );
      }

      //put in list items
      if( ( param_count >= 1 ) || ( error_count >= 1 ) ) {
        s.append( "<li>" );

        if( param_count >= 1 ) {
          s.append( "<b>" );

          //this traps errors where the number of @param's
          //is greater than the actual number of parameters.
          if( ( param_count ) <= paramsVec[0].size(  ) ) {
            s.append( paramsVec[0].elementAt( param_count - 1 ).toString(  ) );
          }

          s.append( "</b> " );
        }
      }

      // put in line breaks
      // check to see if '@' or newline comes first
      while( ( header > newline ) && ( newline > 0 ) ) {
        //grab text before the newline character
        if( space < newline ) {
          s.append( m.substring( space + 1, newline ) );
        }

        s.append( "<br>" );  // converts '\n' to '<br>'

        // trick-need to go from newline to newline
        space     = newline;
        newline   = m.indexOf( "\n", newline + 1 );
      }

      // use our tricked out space index
      if( ( header >= space ) && ( space < ( m.length(  ) - 1 ) ) ) {
        s.append( m.substring( space + 1, header ) );
      } else if( header < ( m.length(  ) - 1 ) ) {  // use the regular space index
        s.append( m.substring( header + 1, space ) );
      } else {  // end of string, explicitly kill the loop
        header = m.length(  );
      }

      // reset/update the space
      space = m.indexOf( ' ', header );

      if( space < 0 ) {
        space = m.length(  );
      }

      if( ( param_count >= 1 ) || ( error_count >= 1 ) ) {
        s.append( "</li>\n" );
      }

      // close the unordered parameter list
      if( param_count >= paramsVec[0].size(  ) ) {
        s.append( "</ul>\n" );
      }

      //aesthetic spacing
      s.append( "\n<br>\n" );

      //if we have date info, we need to put in a last modified table
      if( isOverview && ( date != null ) ) {
        s.append( "<br><b><font color=#FF0000>" );
        s.append( "Last Modified: " );
        s.append( date );
        s.append( "</b><br>" );
      }
    }

    //note: this is not a perfect fix.  If any tags come after the
    //@error tag, this will not work as intended, since it relies on the
    //@error tag being the last one.  Repeat: this is a temporary fix.
    //close the unordered error list
    if( error_count >= 1 ) {
      s.append( "</ul>\n" );
    }

    return s.toString(  );
  }

  /**
   * Creates the default documentation for an operator in the event that the
   * documentation has not yet been written
   *
   * @param v Vector consisting of the Strings representing parameter names for
   *        an operator
   *
   * @return String which consists of operator name and title
   */
  private String createDefaultDocs( Vector[] v ) {
    StringBuffer s = new StringBuffer(  );

    s.append( 
      "<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n" );
    s.append( "<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n" );
    s.append( "<td COLSPAN=1><font SIZE=\"+2\">\n" );
    s.append( "<b>Parameters" );
    s.append( "</b></font></td>\n" );
    s.append( "</tr>\n" );
    s.append( "</table>\n\n" );
    s.append( "<ul>\n" );

    if( v[0] != null ) {
      for( int i = 0; i < v[0].size(  ); i++ ) {
        s.append( "<li><b>" + v[0].elementAt( i ) );
        s.append( "</b> " + v[1].elementAt( i ) );
        s.append( " </li>\n" );
      }

      s.append( "</ul>\n" );

      return s.toString(  );
    } else {
      return "";
    }
  }
}
