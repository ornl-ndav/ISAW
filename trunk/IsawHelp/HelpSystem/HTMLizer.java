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
 * Contact : Chris Bouzek <coldfusion78@yahoo.com>  or
 *           Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
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
    String         help_dir;
    File           help_out;
    FileWriter     out;
    BufferedReader reader;
    String         op_name;

    /* -----------------CONSTRUCTOR----------------------------------------- */
    /**
     * Constructs an HTMLizer which writes to a file Help.htm using a 
     * FileWriter.
     */

    public HTMLizer()
    {
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
                help_dir = "HelpSystem/html";
            }
            SharedData.addmsg("Writing files to" + help_dir);
	}//try
	catch(RuntimeException e)
	{
	    e.printStackTrace();
	}//catch
    }//constructor()

    /* ------------------------- createHelpDocs ---------------------------- */
    /** 
     * Creates the HTML files which consists of the information in an Operator's
     * getDocumentation() method.  If one_file is true, it creates 
     * documentation only for the operator named by class_name.  Otherwise, 
     * it creates files for all operators. 
     * @param The class name of the operator to create documentation for.
     * @param Set to true if there is only one operator to create documentation
     *             for.
     */
    public void createHelpDocs(String class_name, boolean one_file)
    {
	String op_name;
	String op_class_name;
	int delim_index, name_length;
	Vector op_vector = createOperatorVector();
	int op_vector_size = op_vector.size();
	Operator op;

	if( !op_vector.isEmpty()) //make sure we have operators to work with
	{  

	for( int i = 0; i < op_vector_size; i++ )
	{
	    op = (Operator)op_vector.get(i);  //get the jth Operator
		
	    //get operator class name which includes path
	    op_class_name = op.getClass().toString(); 

	    //remove all leading information in op_class so we have
	    //just the class name
	    delim_index = op_class_name.indexOf('.');
	    name_length = op_class_name.length();

	    while( delim_index > 0 && delim_index < name_length )
	    {
		op_class_name = op_class_name.substring(delim_index + 1);
		//find next delimiter
		delim_index = op_class_name.indexOf('.');

	    }//while(delim_index...)

	    if( !one_file )  //create help files for all operators
		createHTMLFile(op, op_class_name);
	    //only one operator needs a help file
	    else if (class_name.equals(op_class_name) ) //we have found our 
                                                        //operator
		createHTMLFile(op, op_class_name);
	}//for
	}//if

    }//createHelpFile()


    /* ------------------------- createOperatorVector --------------------- */
    /** 
     * Create a Vector of Operators comprised of all "add-on" operators and 
     * all of the tandard DataSet Operators, and returns this array.
     */

    public Vector createOperatorVector()
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

    /* ----------------------------- createHTMLFile ------------------------- */
    /**
     * This method creates an HTML file for the Operator which matches the 
     * class name given by operator_class_name.
     */
    private void createHTMLFile(Operator op, String op_class)
    {
	String op_name;
	try
	{
	    op_name = op.toString();//get plain English operator name
	    //create file with operator class name
	    help_out = new File(help_dir, op_class + "Help.html"); 
	    out = new FileWriter(help_out);
	    out.write("<html><body>");  //start HTML page
	    out.write("<h1><center>" + op_name + "</h1></center><p>"); 
                                                        //print operator name
		
	    //get documentation from operator and convert to HTML format
	    out.write( convertJDocToHTML( op.getDocumentation() ) );
	    out.write("</body></html>");  //end HTML formatting
	    out.close();  //close file
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }//createHTMLFile()


    /* ------------------------- convertJDocToHTML ------------------------ */
    /**
     * This method converts a String that follows the @ conventions in the 
     * JavaDoc specifications into an HTML page.  Although other purposes may 
     * be found for this method, its primary use is for the JavaHelp System.
     *
     */
    public String convertJDocToHTML( String JDocString)
    {
	boolean start = true;

	StringBuffer S = new StringBuffer("");  // create beginning of 
                                                // unordered list
	int space_index = 0; //substring index; start creation of string at [0]
	int newline_index = 0;  //newline character index

	int header_index = JDocString.indexOf('@');

	//create beginning of HTML string, from index 0 to one before index of @
	S.append( JDocString.substring(space_index,header_index));

	//create rest of HTML string
	while( (header_index >= 0) && 
               (space_index  >= 0) && 
               (space_index < JDocString.length()) 
	    && (header_index < JDocString.length()) )
	{ 
	    if( !start)     //not start of JDocString
		S.append( "</ul>");   //end the unordered list from previous 
                                      // heading
	    else S.append("<p>");  //else start a new paragraph for the new file

	    start = false;  // if we get this far, we have already started 
                            // the HTML page

            // increment space_index to first occurrence of a space after the 
            // @ symbol
	    space_index = JDocString.indexOf(' ',header_index); //index of space
	    newline_index = JDocString.indexOf('\n',header_index); 
                                                    //index of newline character

	    //if a space is not found
	    if( space_index < 0 )
		if( newline_index > 0 ) //but a newline character is
                  space_index = newline_index; //there is more to the JDocString
		else  //we have reached the end of the JDocString
		    space_index = JDocString.length();
 
	    //create Operator help heading
	    S.append( "<b>" );  //bold for operator headings
	    S.append( JDocString.substring(header_index + 1, space_index) );  
                                        //add operator heading name
	    S.append( "</b><p><ul>" );  //end operator heading, start new 
                                        //unordered list  
   
	    //create the subject text
	    header_index = JDocString.indexOf('@',space_index);
	    if( header_index < 0 ) //no more headers
		header_index = JDocString.length(); //end of JDocString
	    S.append( JDocString.substring(space_index, header_index) );
	    space_index++;
	}

    	return  S.toString();
    }//convertJDocToHTML

    /* ------------------------- main ----------------------------------- */
    /**
     * Main method for running class as a standalone program.
     *
     */

    public static void main(String args[])
    {
	boolean only_one_op; 
	String op_class_name = ("");
	HTMLizer helpfile = new HTMLizer();
	
	//System.out.println(args[0]);
	//create documentation for all operators
	if( args.length <= 0 )
	    only_one_op = false; 
	//create documentation for one operator
	else
	{
	    only_one_op = true;
	    op_class_name = args[0];
	}

	System.out.println();
	System.out.println("Creating JavaHelp HTML documentation.");
	helpfile.createHelpDocs(op_class_name, only_one_op);

        System.exit(0);
    }//main

}//HTMLizer
