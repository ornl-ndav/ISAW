/*
 * File LoadScriptArray2.java
 *
 * Copyright (C) 1999, Alok Chatterjee
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
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2003/02/26 23:03:02  hammonds
 *  Fixed problem caused by leading white space.
 *
 *  Revision 1.1  2003/02/26 17:47:39  hammonds
 *  Added this second version to allow ragged arrays to be loaded into a script.  This takes only a filename, it automatically skips lines starting with # and figures out when it hits the end of file and end of a line.
 *
 *  Revision 1.1  2003/02/13 14:14:54  hammonds
 *  Added as a method to read an array into a Script.
 *
 *
 */

package Operators;

import  java.io.*;
import  java.util.*;
import  DataSetTools.operator.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Generic.Batch.*;
import  IPNS.Runfile.*;
/**
 *   This class supports reading of Strings, floats, ints etc. from an
 *   ordinary text file.  In addition to methods to read each of the 
 *   basic data types from the file, there is an "unread" method that 
 *   restores the last non-blank item that was read.  Error handling and
 *   end of file detection are done using exceptions.
 */

public class LoadScriptArray2 extends GenericBatch implements Serializable
{    
  
	
  /* -------------------------- Constructor -------------------------- */
  /**
   *  Construct a SetupReader to read from the specified file.  The
   *  constructor will throw an exception if the file can't be opened.  The
   *  other methods of this class should not be used if the file can't be
   *  opened.
   *
   *  @param file_name  The fully qualified file name.
   *  @param num_head The number of header lines that should be
   *  skipped while reading in the file
   *  @param num_data The number of data lines to read in. If set to
   *  zero all lines until the end of file are read.

  */

  public LoadScriptArray2( String file_name )
  {  
    this();
    parameters = new Vector();
    addParameter(new Parameter("Filename", file_name) );
  }


  public LoadScriptArray2(  )
  {
    super( "LoadScriptArray");
  } 




  /** 
   * Get the name of this operator, used in scripts
   * @return "SetupReader", the command used to invoke this operator
   * in Scripts
   */
  public String getCommand()
  {
    return "LoadScriptArray";
  }
  /** 
   * Sets default values for the parameters. The parameters set must
   * match the data types of the parameters used in the constructor.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Filename", null));
  }

  /** 
   *  Executes this operator using the current values of the
   *  parameters.
   */

  public Object getResult() {
    String  file_name = getParameter(0).getValue().toString();
    /*    int     num_head = ((Integer)getParameter(1).getValue()).intValue();
    int     num_data = ((Integer)getParameter(2).getValue()).intValue();
    int     max_col = ((Integer)getParameter(3).getValue()).intValue();
    */
    TextFileReader f = null;
    String line;
    Vector data_out = new Vector();
    Vector data_out_V = new Vector();
   
    try {
      f = new TextFileReader( file_name );
     
      while ( !f.eof() ) {
	StringBuffer in_line = new StringBuffer();
	in_line = new StringBuffer( f.read_line() );
	StringUtil.trim(in_line);
	if ( in_line.toString().startsWith("#") ){
	  //Skip this line it is a comment.
	}
	else {

	  //	  System.out.println( in_line.toString() );
	  data_out = new Vector();
	  while ( in_line.toString().length() > 0 ){
	    try { // Is it an integer
	      int iVal = StringUtil.getInt( in_line );
	      data_out.add( (new Integer(iVal) ) );
	    } // end try for Integer
	    catch (NumberFormatException ex){  // Not an Integer
	      try {  // Is it a Float
		float fVal = StringUtil.getFloat( in_line );
		data_out.add( (new Float(fVal) ) );
			     
	      } // End try for Float
	      catch( NumberFormatException ex2 ) { // Not a Float
		try{  // Is it a Boolean 
		  boolean bVal = StringUtil.getBoolean(in_line);
		  data_out.add(new Boolean(bVal));
		}  // End try for Boolean
		catch ( IllegalArgumentException ex3 ){  //  Must be a string
		  String sVal = StringUtil.getString( in_line );
		  char[] dq = {'"'};
		  String quote = new String(dq);
		  if (sVal.startsWith( quote ) ) { // If string starts with quote
		                                   // must assume look past white
		                                   // space
		    String tempString = sVal.substring(1);
		    sVal = tempString;
		    if ( sVal.endsWith( quote )) {  // This is the last part
		                                    // of the quoted string
		      tempString = sVal.substring(0,sVal.length()-1);
		      sVal = tempString.trim();
		    }

		    else{ // This is text in the middle of the quoted string 
		          // keep looking for the end of the string
		      int pIndex = 
			StringUtil.nth_index_of( 1, quote,
						 in_line.toString() );
		      String restOf = 
			StringUtil.getString( in_line, pIndex );
		      tempString = new String( sVal + " " + restOf );
		      sVal = tempString.trim();
		      String paren = StringUtil.getString( in_line);
		    } 
		  }  // End look for text inside quote
		  data_out.add( sVal );
		} // End of Catch for proccesing strings
	      }// End of Float Catch	    
	    }  // End of Integer Catch

	  }  //End of while that tests for end of line

	  data_out_V.add(data_out.clone());
	} // end of line go get the next
      }// End of file reached

      
    } //end of try
    catch ( IOException e )
      {
	//throw e;
      }
      
    return data_out_V;

  }
    
 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                                    
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview  This operator provides method to get an array of ");
    s.append("values from a text file.  The values are passed back as a " );
    s.append("vector of vectors. As a return value in an ISAW script, this");
    s.append("vector is treated as a two dimensional Array.\n\n");
    s.append("Values allowed in the table can be:\n" );
    s.append("<OL><LI>Integer</LI><LI>Float</LI><LI>Boolean</LI>");
    s.append("<LI>String (Any string with spaces must be enclosed by double" );
    s.append("quotes.  The double quote character is NOT a legal part of a " );
    s.append("string.</LI></OL>" );

    s.append("@assumptions The given file 'file_name' is an ASCII text file, ");
    s.append("and is EXACTLY formatted as discussed in the 'Overview' section.");                                                                       //                                                              
    s.append("@algorithm ");
    s.append("This routine loads reads the ASCII file, skips any line that");
    s.append("begins with a # and then reads the data from each line and ");
    s.append("parses this into a Vector of Vectors.  It tries to map each entry ");
    s.append("in a line into an Integer then a " );
    s.append("Float, Boolean and then String.  If a string is found, a test ");
    s.append("is done to see if the string starts with a double quote(\").  ");
    s.append("If it does it looks for a matching ending double quote as a ");
    s.append("terminating character for the string" );
    s.append("@param file_name The fully qualified ASCII file name");
    s.append("@return If successful, this returns a vector of vectors " );
    s.append(" which contain the data from the file ");
    return s.toString();
  }  

 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    Operator op  = new LoadScriptArray2(args[0]);
    Object   obj = op.getResult();
                                                 // display any message string
                                                 // that might be returned
    System.out.println("Operator returned: " + obj );

                                                 // if the operator produced a

  }
}

