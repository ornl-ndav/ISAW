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

import java.util.*;
import java.io.*;
import Command.Script_Class_List_Handler;
import DataSetTools.operator.Generic.Batch.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 * This class takes a String input from an Operator's getDocumentation() 
 * method and converts it into an HTML file to be used with JavaHelp 1.1.3.
 *
 */

public class HTMLizer
{
    private String         help_dir;
    private File           help_out;
    private FileWriter     out;
    private BufferedReader reader;
    private String         op_name;
    private Vector         op_vector;

    /* -----------------CONSTRUCTOR----------------------------------------- */
    /**
     * Constructs an HTMLizer which writes to a file Help.htm using a 
     * FileWriter.
     */

    public HTMLizer()
    {
	//get a list of all Operators
	op_vector = createOperatorVector();

	try
	{ 
            PropertiesLoader loader = new PropertiesLoader("IsawProps.dat");
            String isaw_help = SharedData.getProperty("Help_Directory");
            if ( isaw_help != null )
              help_dir = isaw_help + "HelpSystem/html";
            else
            {
              isaw_help = SharedData.getProperty("ISAW_HOME");
              if ( isaw_help != null )
                help_dir = isaw_help + "IsawHelp/HelpSystem/html";
              else
                help_dir = "HelpSystem/html";
            }
	    // SharedData.addmsg("Writing files to " + help_dir);
	}//try
	catch(RuntimeException e)
	{
	    e.printStackTrace();
	}//catch
    }//constructor()

    /* ------------------------- createAllHelpFiles ---------------------------- */
    /** 
     * Creates the HTML files which consists of the information in an Operator's
     * getDocumentation() method.  Documentation for all Operators is produced.
     */
    public void createAllHelpFiles()
    {
	int op_vector_size = op_vector.size();
	Operator op;
	String op_class;

	for( int i = 0; i < op_vector_size; i++ )
	{
	    op = (Operator)op_vector.get(i);  //get the jth Operator
	    op_class = trimClassName( op.getClass().toString() );

	    writeFile( op_class, createHTML(op) );
	}

    }//createHelpFile()


    /* ------------------------- dynamicDocCreation ------------------------- */
    /** 
     * Creates the HTML formatted documentation for the Operator op_in.
     * @param The Operator class name for which you wish to create documentation.
     * @return The String which consists of the HTML formatted documentation
     * for op_in.  This can be written, as-is, to a file to produce an 
     * HTML page.
     * @return null if the Operator op_in is not found
     */
    public String dynamicDocCreation(String op_in_class)
    {
	int op_vector_size = op_vector.size();
	Operator op;
	String op_class;
	boolean found = false;

	for( int i = 0; i < op_vector_size; i++ )
	{
	    op = (Operator)op_vector.get(i);  //get the jth Operator

	    op_class = trimClassName( op.getClass().toString() );

	    //found our operator
	    if( op_class.equals( op_in_class ) )
		return createHTML(op);
	}
	
	return null;

    }

    /* ------------------------- trimClassName ------------------------- */
    /** 
     * Trims the full class path given by class_name.
     * @param The full class name which you wish to trim.
     * @return The String which consists of the trimmed down class name.
     */
    public String trimClassName(String class_name)
    {
	StringTokenizer st = new StringTokenizer(class_name, ".");
	String name = class_name;

	while( st.hasMoreTokens() )
	    name = st.nextToken(".");
	return name;
    }

    /* ------------------------- createOneHelpFile --------------------- */
    /** 
     *  Creates a help file for a single Operator op.
     *  @param The Operator class name which denotes the Operator you
     *  wish to create documentation for.
     */
    public void createOneHelpFile(String op_class)
    {
	String s = dynamicDocCreation(op_class);
	
	if( s != null )
	    writeFile( op_class, s );
    }

    /* ------------------------- createOperatorVector --------------------- */
    /** 
     * Create a list of Operators comprised of all "add-on" operators and 
     * all of the standard DataSet Operators.
     * @return A Vector representation of the Operator list.
     */
    public static Vector createOperatorVector()
    {
	final int TOTAL_OPERATORS = 100;
	final int INC_AMOUNT = 20;

	//script handler for the operator list
	Script_Class_List_Handler base_op_handler = 
                                               new Script_Class_List_Handler();

	Vector op_vector = new Vector(TOTAL_OPERATORS, INC_AMOUNT);
	int num_add_on_ops= base_op_handler.getNum_operators();
	int num_dataset_ops = base_op_handler.getNumDataSetOperators();

	//get all add-on operators
	for( int j = 0; j < num_add_on_ops; j++)
	    op_vector.add(j, base_op_handler.getOperator(j));

	int num_operators = op_vector.size();

	//get the DataSet operators
	for( int k = 0; k < num_dataset_ops; k++ )
	   op_vector.add( k + num_operators, 
                          base_op_handler.getDataSetOperator(k));

	return op_vector;
    }//createOperatorVector()



    /* ----------------------------- createHTML ------------------------- */
    /**
     *  Formats the documentation of the Operator op by using HTML tags.
     *  @param The Operator for which you wish to create HTML documentation
     *  for.
     *  @return the String consisting of the HTML formatting
     */
    public String createHTML(Operator op)
    {
	StringBuffer html = new StringBuffer(); 
	String class_name = op.getClass().toString();
	String title = op.getTitle();
	StringTokenizer st = new StringTokenizer( class_name, ".");

	//trim the class name down
	while( st.hasMoreTokens() )
	    class_name = st.nextToken(".");

	html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Frameset//EN\"");
	html.append("\"http://www.w3.org/TR/REC-html40/frameset.dtd\">\n");
        html.append("<!--NewPage-->\n");
	html.append("<html>\n");
	html.append("<head>\n");
	html.append("<title>\n");
	html.append("ISAW ");
	html.append(class_name);
	html.append("</title>\n");
	html.append("</head>\n");
	html.append("<body BGCOLOR=#FFFFFF>\n");

	html.append("<!-- ======== START OF OPERATOR DATA ======== -->\n");
	html.append("<h2><hr><br>\n");

	html.append("Operator Title: ");
	html.append(title + "<br><br>\n");
	html.append("Operator Class: ");
	html.append(class_name);
	html.append("</h2>\n");
	
	html.append("Command: ");
	html.append(op.getCommand());
	html.append(" <br>");
	html.append("Class path: ");
	html.append(op.getClass().toString());
	html.append(" <br>");

	html.append("<p><hr><p>\n");

	//get the actual documentation
	html.append( convertToHTML( op.getDocumentation() ) );

	html.append("<!-- ========= END OF OPERATOR DATA ========= -->");
	html.append("</body>\n");
	html.append("</html>");

	//try to write the file and print a message if operation failed
	if ( !writeFile(class_name, html.toString()) )
	    System.out.println("\nFailed to write to file");

	return html.toString();
      
    }//createHTML()

    /* ------------------------------ writeFile() ------------------------------ */
    /**
     *  Writes a file consisting of the String body.  The name of the file is 
     *  operator_class +"Help.html".
     *  @param The String which denotes the Operator class.  Used to 
     *  create the file name.
     *  @param The body of the file you wish to create.
     *  @return A boolean indicating whether or not the file write was 
     *  successful
     */
    public boolean writeFile(String operator_class, String body)
    {
      try{
        help_out = new File(help_dir, operator_class + "Help.html"); 
        out = new FileWriter(help_out);
        out.write(body);
        out.close();
        return true;
      }catch( FileNotFoundException e){
        return false;
      }catch(Exception e){
        e.printStackTrace();
        return false;
      }
    }

    /* --------------------------- convertToHTML() -------------------------- */
    /**
     *  This method converts a String that follows the @ conventions in the 
     *  JavaDoc specifications into an HTML page.  Although other purposes may 
     *  be found for this method, its primary use is for the JavaHelp System.
     *  @param The String to convert to HTML
     */
    private String convertToHTML(String m)
    {
        // check that there is something to work with
        if( m==null || m.length()==0 || m.equals(Operator.DEFAULT_DOCS) )
          return "Documentation not written";

	//eliminate "garbage" information
	m = m.substring(m.indexOf('@'));

	StringBuffer s = new StringBuffer();
	int header = m.indexOf('@');
	int space = m.indexOf(' ');
	int newline = m.indexOf("\n");
	String header_name, table_title = "";
	int found_param = 0, found_error = 0;

	//check for valid index information
	if( space < 0 )
	    return new String("Please insert spaces.");
	
	if( newline < 0 )
	    newline = m.length();

	//while we have information and the '@' exists
	while( header < m.length() && header >= 0 )
	{
	    header_name = m.substring(header, space);
	
	    if( header_name.equals("@overview") )
		table_title = "Overview";
	    else if( header_name.equals("@assumptions") )
		table_title = "Assumptions";
	    else if( header_name.equals("@param") )
	    {
		found_param += 1;
		table_title = "Parameters";
	    }
	    else if( header_name.equals("@algorithm") )
		table_title = "Algorithm";
	    else if( header_name.equals("@return") )
		table_title = "Returns";
	    else if( header_name.equals("@error") )
	    {
		found_error += 1;
		table_title = "Errors";
	    }
	
	    if( (header_name.equals("@param") && found_param > 1)
		|| (header_name.equals("@error") && found_error > 1) )
	    {
		s.append("<ul>");
	    }
	    else
	    {
		s.append("\n<!-- ========= ");
		s.append(table_title);
		s.append(" detail ======== -->\n\n");

		s.append("<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n");
		s.append("<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
		s.append("<td COLSPAN=1><font SIZE=\"+2\">\n");
		s.append("<b>");
		s.append(table_title);
		s.append("</b></font></td>\n");
		s.append("</tr>\n");
		s.append("</table>\n\n");

		if( (header_name.equals("@param") && found_param <= 1 )
		    || (header_name.equals("@error") && found_error <= 1) )
		    s.append("<ul>");
	    }

	    //update the header index-for the loop and for the next conditional
	    header = m.indexOf('@', header + 1);
	
	    if( header < 0 )
		header = m.length();
	
	    //check to see if @ or newline comes first
	    while( header > newline && newline > 0)
	    {
		s.append("<br>");
		//grab text between \n and \n or \n and @
		if( space != newline )
		    s.append(m.substring(space + 1, newline));

		//trick-need to go from newline to newline
		space = newline;
		newline = m.indexOf("\n", newline + 1);
	    }
	    
	    if( header >= space && space < (m.length() - 1))//use our tricked out space index
	    s.append(m.substring(space + 1, header));

	    else if( header < (m.length() - 1) )//use the regular space index
		s.append(m.substring(header + 1, space));
	    else //end of string, explicitly kill the loop
		header = m.length();

	    //reset/update the space
	    space = m.indexOf(' ', header);
	    if( space < 0 )
		space = m.length();

	    s.append("<p>");
	    if( header_name.equals("@param") && found_param >=  1 
		|| (header_name.equals("@error") && found_error >= 1) )
	    {
	      s.append("</ul>");
	    }
	}

	return s.toString();

    }
     
    /* ------------------------- main ----------------------------------- */
    /**
     * Main method for running class as a standalone program.
     *
     */

    public static void main(String args[])
    {

	System.out.println();
	System.out.println("Creating JavaHelp HTML documentation....please wait.");
	HTMLizer helpfile = new HTMLizer();	
	//create documentation for all operators
	if( args.length <= 0 )
	    helpfile.createAllHelpFiles();
	//create documentation for one operator
	else
	    helpfile.createOneHelpFile(args[0]);

        System.exit(0);
    }//main

}//HTMLizer
