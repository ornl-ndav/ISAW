/*
 * File: LoadScriptArray.java
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
 *  Revision 1.7  2004/03/15 19:36:52  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.6  2004/03/15 03:36:58  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.5  2004/01/30 02:19:38  bouzekc
 *  Removed unused imports and variables.
 *
 *  Revision 1.4  2003/04/22 21:51:41  hammonds
 *  Fixed problem with CastClassExeption due to inconsistant use of Parameter vs iParameter(LoadFilePG) Class.
 *
 *  Revision 1.3  2003/03/03 20:12:54  hammonds
 *  Documentation cleanup.  Convert to new Parameter types.
 *
 *  Revision 1.2  2003/02/26 23:02:14  hammonds
 *  Comment change only.
 *
 *  Revision 1.1  2003/02/13 14:14:54  hammonds
 *  Added as a method to read an array into a Script.
 *
 *
 */

package Operators;

import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Sys.*;

import  java.io.*;
import  java.util.*;
import  DataSetTools.operator.*;
import  DataSetTools.parameter.LoadFilePG;
import  DataSetTools.parameter.IntegerPG;
import  DataSetTools.operator.Generic.Batch.*;
//import  IPNS.Runfile.*;
/**
 *   This class supports reading of Strings, floats, ints etc. from an
 *   ordinary text file.  In addition to methods to read each of the 
 *   basic data types from the file, there is an "unread" method that 
 *   restores the last non-blank item that was read.  Error handling and
 *   end of file detection are done using exceptions.
 */

public class LoadScriptArray extends GenericBatch implements Serializable
{    
  
	
  /* -------------------------- Constructor -------------------------- */
  /**
   *  Construct a LoadScriptArray operator to read from the specified file.  
   *  The constructor will throw an exception if the file can't be opened.  The
   *  other methods of this class should not be used if the file can't be
   *  opened.
   *
   *  @param file_name  The fully qualified file name.
   *  @param num_head The number of header lines that should be
   *  skipped while reading in the file
   *  @param num_data The number of data lines to read in.
   *  @param num_col The number of data columns to read in
   */

  public LoadScriptArray( String file_name,Integer num_head, Integer num_data,
			  Integer num_col )
  {  
    this();
    getParameter(0).setValue(file_name);
    getParameter(1).setValue(num_head);
    getParameter(2).setValue(num_data);
    getParameter(3).setValue(num_col);
  }


  public LoadScriptArray(  )
  {
    super( "LoadScriptArray");
    setDefaultParameters();
  } 




  /** 
   * Get the name of this operator, used in scripts
   * @return "LoadScriptArray", the command used to invoke this operator
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
    LoadFilePG inFile=new LoadFilePG("Input File", new LoadFileString(""));
    addParameter( inFile );
    IntegerPG head_in = new IntegerPG(" *# of header lines", 0);
    addParameter( head_in );
    IntegerPG data_in = new IntegerPG("# of data lines", 0);
    addParameter( data_in );
    IntegerPG col_in = new IntegerPG("# of columns",0 );
    addParameter( col_in );
  }

  /** 
   *  Executes this operator using the current values of the
   *  parameters.
   */

  public Object getResult()
  {
    String  file_name = ((LoadFilePG)getParameter(0)).getStringValue();
    int num_head = 
      ((Integer)((IntegerPG)getParameter(1)).getValue()).intValue();
    int num_data = 
      ((Integer)((IntegerPG)getParameter(2)).getValue()).intValue();
    int max_col = 
      ((Integer)((IntegerPG)getParameter(3)).getValue()).intValue();

    TextFileReader f = null;
    String line;
    Vector[] data_out = new Vector[0];;
    Vector data_out_V = new Vector();
   
    try
      {
	f = new TextFileReader( file_name );
	for( int i=0 ; i<num_head ; i++ )
	  {
	    line=f.read_line();
	  }

	data_out = new Vector[num_data];
	data_out_V = new Vector();
	 
	for ( int i=0; i<num_data; i++){
	  data_out[i] = new Vector();
	  StringBuffer in_line = new StringBuffer( f.read_line() );
	  StringUtil.trim(in_line);
	  for ( int j=0; j<max_col; j++ ) {
		 
	    {
	      try {
		int iVal = StringUtil.getInt( in_line );
		data_out[i].add( (new Integer(iVal) ) );
	      }
	      catch (NumberFormatException ex){
		try {
		  float fVal = StringUtil.getFloat( in_line );
		  data_out[i].add( (new Float(fVal) ) );
			     
		}
		catch( NumberFormatException ex2 ) {
		  try{
		    boolean bVal = StringUtil.getBoolean(in_line);
		    data_out[i].add(new Boolean(bVal));
		  }
		  catch ( IllegalArgumentException ex3 ){
		    String sVal = StringUtil.getString( in_line );
		    char[] dq = {'"'};
		    String quote = new String(dq);
		    if (sVal.startsWith( quote ) ) {
		      String tempString = sVal.substring(1);
		      sVal = tempString;
		      if ( sVal.endsWith( quote )) {
			tempString = sVal.substring(0,sVal.length()-1);
			sVal = tempString.trim();
		      }

		      else{ 
			int pIndex = 
			  StringUtil.nth_index_of( 1, quote,
						   in_line.toString() );
			String restOf = 
			  StringUtil.getString( in_line, pIndex );
			tempString = new String( sVal + " " + restOf );
			sVal = tempString.trim();
			StringUtil.getString( in_line);
		      }
		    }
		    data_out[i].add( sVal );
		  }
		}
			 
	      }
	    }
	  }
	  data_out_V.add(data_out[i].clone());
	}


      } //end of try
    catch ( IOException e )
      {
        //throw e;
	return new ErrorString("LoadScriptArray: ERROR on file read");
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
    s.append("This routine loads reads the ASCII file, skips the specified");
    s.append("number of header lines and then reads num_columns of data from");
    s.append("num_lines.  It tries to map each entry to an Integer then a " );
    s.append("Float, Boolean and then String.  If a string is found, a test ");
    s.append("is done to see if the string starts with a double quote(\").  ");
    s.append("If it does it looks for a matching ending double quote as a ");
    s.append("terminating character for the string" );
    s.append("@param file_name The fully qualified ASCII file name");
    s.append("@param num_head The number of header lines to skip");
    s.append("@param num_lines The number of lines of data in the array");
    s.append("@param num_col The number of columns in the array");
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

      String filename = new String();;
      Integer num_head = new Integer (0);
      Integer num_data = new Integer (0);
      Integer num_col = new Integer (0);
    try {

      filename = args[0];
      num_head = new Integer (args[1]);
      num_data = new Integer (args[2]);
      num_col = new Integer (args[3]);
      System.out.println(filename +" "+  num_head+" "+ num_data+" "+  num_col );
    }
    catch ( Exception ex) {
      System.out.println( "ERROR*******");
      System.out.print( "Command requires four arguments: filename, num_head, "
 );
      System.out.println( "num_data, num_col" );
      ex.printStackTrace();
      System.exit(0);
   }
      Operator op  = new LoadScriptArray(filename, num_head, num_data, num_col );
      Object   obj = op.getResult();
                                                 // display any message string
                                                 // that might be returned    
                                                 // if the operator produced a
      System.out.println("Operator returned: " + obj );
      
      System.exit(0);
  }


}

