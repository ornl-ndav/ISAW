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

import java.util.*;
import java.io.*;
import Command.Script_Class_List_Handler;
import DataSetTools.operator.Generic.Batch.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.parameter.IParameter;

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
              help_dir = isaw_help + "/HelpSystem/html";
            else
            {
              isaw_help = SharedData.getProperty("ISAW_HOME");
              if ( isaw_help != null )
                help_dir = isaw_help + "/IsawHelp/HelpSystem/html";
              else
                help_dir = "/HelpSystem/html";
            }
	    // SharedData.addmsg("Writing files to " + help_dir);
	}//try
	catch(RuntimeException e)
	{
	    e.printStackTrace();
	}//catch
        help_dir=FilenameUtil.fixSeparator(help_dir);
    }//constructor()

    /* ---------------------- createAllHelpFiles --------------------------- */
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


    /* ------------------------- dynamicDocCreation ------------------------ */
    /** 
     * Creates the HTML formatted documentation for the Operator op_in.
     * @param The Operator class name for which you wish to create
     * documentation.
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

    /* -------------------------- getParameterInfoList --------------------- */
    /**
     *  Gets the parameter class names of an Operator.  If the Operator is an
     *  instance of a DataSetOperator, the first element will
     *  be the String "DataSet".  The class names are trimmed to base names
     *	and are indexed according to the order they go into the Operator's
     *	constructor
     *  @param Operator for which you wish to retrieve the parameter
     *	list
     *  @return Vector which contains Strings representing the class
     *	names of the parameters
     */
    private Vector[] getParameterInfoList(Operator op)
    {
    	String class_s, name_s;
	Vector v[];
	final int info = 2;
	int num_params;
	Object ob;
	
	v = new Vector[info];
	
	for( int i = 0; i < info; i++ )
	    v[i] = new Vector(10, 2);
	    
	num_params = op.getNum_parameters();
    
    	//get parameter list; differentiate between DataSetOperators
	//and generic operators			
	if( op instanceof DataSetOperator )
	{
	    v[0].addElement("DataSet");
	    v[1].addElement("DataSet for Operator");
    	}
	    	  
	for( int i = 0; i < num_params; i++ )
	{
	    //for some reason, EchoObject and maybe others
	    //return "null" when getValue() is called.
	    //this takes care of it temporarily, although this 
	    //should be researched
	    ob = op.getParameter(i).getValue();
	    
	    if( ob == null )
	    	class_s = "";
	    else
	    	class_s = ob.getClass().toString();
		
	    name_s = op.getParameter(i).getName().toString();
	    v[0].addElement(this.trimClassName(class_s));
	    v[1].addElement(name_s);
    	}
	
	return v;
    }


    /* ----------------------------- createHTML ------------------------- */
    /**
     *  Formats the documentation of the Operator op by using HTML tags.
     *  @param The Operator for which you wish to create HTML documentation
     *  for.
     *  @return the String consisting of the HTML formatting
     */
    public String createHTML(Operator op)
    {
	StringBuffer html; 
	String class_name, title, docs;
	Vector v[];
	int num_params;
	
	html = new StringBuffer(); 
	class_name = op.getClass().toString();
	title = op.getTitle();
	v = this.getParameterInfoList(op);
	docs = op.getDocumentation();
	num_params = v[0].size();

	//remove the "class" from class_name
	class_name = class_name.substring(class_name.indexOf(" ") + 1, 
					  class_name.length());

	html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Frameset//EN\"");
	html.append("\"http://www.w3.org/TR/REC-html40/frameset.dtd\">\n");
        //html.append("<!--NewPage-->\n");
	html.append("<html>\n");
	html.append("<head>\n");
	html.append("<title>\n");
	html.append("ISAW ");
	html.append(class_name);
	html.append("</title>\n");
	html.append("</head>\n");
	html.append("<body BGCOLOR=#FFFFFF>\n");

	//html.append("<!-- ======== START OF OPERATOR DATA ======== -->\n");

	html.append("<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n");
	html.append("<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
	html.append("<td COLSPAN=1 ALIGN=CENTER><font SIZE=\"+3\">\n");
	html.append("<b>");
	html.append(title);
	html.append("</b></font></td>\n");
	html.append("</tr>\n");
	html.append("</table>\n\n");

	html.append("<b>Class:</b> ");
	html.append(class_name);
	html.append("<br>\n");
	
	html.append("<b>Command:</b> ");
	html.append(op.getCommand());
	
	html.append("(");
	
	
	for( int i = 0; i < num_params; i++ )
	{
	    html.append("<I>"+v[0].elementAt(i) + "</I> ");
	    html.append(v[1].elementAt(i));
	    
	    if( i == (num_params - 1) )
	    	html.append(")\n");
	    else
	    	html.append(", ");
	}
        if(num_params==0) html.append(")\n");
	    	
	html.append(" <br><br>\n");

	//get the actual documentation
	
	// check that there is something to work with
        if( docs == null || docs.length() == 0 || docs.equals(Operator.DEFAULT_DOCS) )
	    html.append(createDefaultDocs(v));  
        else
	    html.append( convertToHTML( op.getDocumentation(), v) );

	//html.append("<!-- ========= END OF OPERATOR DATA ========= -->");
	html.append("</body>\n");
	html.append("</html>");

        return html.toString();
      
    }//createHTML()

    /* ---------------------------- writeFile() ---------------------------- */
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
    
    /* ------------------------- createDefaultDocs() ----------------------- */
    /**
     *  Creates the default documentation for an operator in the event that the
     *  documentation has not yet been written
     *  @param Vector consisting of the Strings representing parameter names 
     *  for an operator
     *  @return String which consists of operator name and title
     */
    private String createDefaultDocs(Vector[] v)
    {
    	StringBuffer s = new StringBuffer();
	/*s.append("\n<!-- ========= ");
          s.append("Parameters");
          s.append(" detail ======== -->\n\n");*/

	s.append("<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n");
	s.append("<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
	s.append("<td COLSPAN=1><font SIZE=\"+2\">\n");
	s.append("<b>Parameters");
	s.append("</b></font></td>\n");
	s.append("</tr>\n");
	s.append("</table>\n\n");
	s.append("<ul>\n");
	
	if( v[0] != null )
	{
	    for( int i = 0; i < v[0].size(); i++ )
	    {
	    	s.append("<li><b>" + v[0].elementAt(i));
	    	s.append("</b> " + v[1].elementAt(i));
	    	s.append(" </li>\n");
	    }
	
	    s.append("</ul>\n");
	
	    return s.toString();
	}
	else
	    return "";
    }
    	

    /* -------------------------- convertToHTML() -------------------------- */
    /**
     *  This method converts a String that follows the @ conventions in the 
     *  JavaDoc specifications into an HTML page.  Although other purposes may 
     *  be found for this method, its primary use is for the JavaHelp System.
     *  @param The String to convert to HTML
     */
    private String convertToHTML(String m, Vector[] v)
    {
	//eliminate "garbage" information
	m = m.substring(m.indexOf('@'));

	StringBuffer s = new StringBuffer();
	int header = m.indexOf('@');
	int space = m.indexOf(' ');
	int newline = m.indexOf("\n");
	String header_name, table_title = "";
	int param_count = 0, error_count = 0;
	boolean found_param = false, found_error = false;

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
		param_count += 1;
		found_param = true;
		table_title = "Parameters";
	    }
	    else if( header_name.equals("@algorithm") )
		table_title = "Algorithm";
	    else if( header_name.equals("@return") )
		table_title = "Returns";
	    else if( header_name.equals("@error") )
	    {
		error_count += 1;
		found_error = true;
		table_title = "Errors";
	    }
	
	    if( ( found_param && param_count > 1 )
		|| ( found_error && error_count > 1) )
	    {
		s.append("<li>");
		
		if( found_param )
		{
		    s.append("<b>");
		    s.append(v[0].elementAt(param_count - 1).toString());
		    s.append("</b> ");
	    	}
	    }
	    else
	    {
                /*s.append("\n<!-- ========= ");
                  s.append(table_title);
                  s.append(" detail ======== -->\n\n");*/

		s.append("<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n");
		s.append("<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
		s.append("<td COLSPAN=1><font SIZE=\"+2\">\n");
		s.append("<b>");
		s.append(table_title);
		s.append("</b></font></td>\n");
		s.append("</tr>\n");
		s.append("</table>\n\n");

		if( ( found_param && param_count <= 1 )
		    || ( found_error && error_count <= 1) )
		{
		    //start the unordered list
		    s.append("<ul><li>");
		    
		    if( found_param )
		    {
		    	s.append("<b>");
		    	s.append(v[0].elementAt(param_count - 1).toString());
		    	s.append("</b> ");
	    	    }
		}		 
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
	    if( ( found_param && param_count >=  1 )
		|| ( found_error && error_count >= 1) )
	    {
	      s.append("</li>\n");
	    }
	    
	    //close the unordered list
	    if( found_param && param_count >= v[0].size() )
	    {
	    	s.append("</ul>\n"); 
		found_param = false;
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
