/* File: TextPrompt.java
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
 * Contact : John Hammonds   jphammonds@anl.gov>
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
 *  Revision 1.1  2003/03/04 15:21:07  hammonds
 *  Added this operator to accept on the text terminal for ISAW Scripts.
 *
 */

package Operators;

import java.io.*;
import java.util.*;
import  DataSetTools.operator.Operator;
import  DataSetTools.util.ErrorString;
import  DataSetTools.operator.Generic.Batch.*;
import  DataSetTools.parameter.StringPG;



/**
 *   This class is intended to provide support for prompting users for input on 
 *   terminal which launched the script.  This is useful for operations that run 
 *   on remote computers and where GUI based input is slow.
 *
 */

public class TextPrompt extends GenericBatch implements Serializable
{

  /* ----------------------------Constructor -----------------------*/
  /** 
   *   Construct a prompt line that from input parameter and then reads back
   *   text input from the command line.
   *
   */

  public TextPrompt( String inPrompt, String inType, String inDef ) {
    this();
    getParameter(0).setValue(inPrompt);
    getParameter(1).setValue(inType);
    getParameter(2).setValue(inDef);
    
  }

  public TextPrompt() {
    super( "TextPrompt" );
    setDefaultParameters();
  }

  /** 
   * Get the name of this operator.
   * @return "TextPrompt".  This is the command used to invoke this operator in
   * scripts.
   */
  public String getCommand(){
    return "TextPrompt";
  }

  /**
   * Set the default parameters
   */
  public void setDefaultParameters() {
    parameters = new Vector();
    StringPG inPrompt = new StringPG("Prompt", "Input Text");
    addParameter( inPrompt);
    StringPG inType = new StringPG("Type", "String");
    addParameter( inType);
    StringPG inDef = new StringPG("Default", "None");
    addParameter( inDef);
  }

  public Object getResult() {
    String inPrompt = ((StringPG)getParameter(0)).getStringValue();
    String inType = ((StringPG)getParameter(1)).getStringValue();
    String inDef = ((StringPG)getParameter(2)).getStringValue();

    System.out.print(inPrompt);
    if ( !inDef.trim().equals("") ) {
      System.out.print( "( Default: " + inDef + " )");
    }
    System.out.println(":");

    if ( !(inType.equals("String") || inType.equals("Integer") || 
	   inType.equals("Float") )) {
      return new ErrorString("TextPrompt: Invalid Type:" + inType );
    }
    //    byte inB = ' ';
    String inLine = new String();

    try {
	 InputStreamReader inStr = new InputStreamReader(System.in);
	 BufferedReader br = new BufferedReader(inStr);
	 inLine = br.readLine();
	 if ( inLine.length() == 0 ) inLine = inDef;
	 }
    catch (IOException IoEx) {
      
    }
    if ( inType.equals("String") ){
	return inLine;
    }
    if ( inType.equals("Float") ){
      try {
	Float inFloat = new Float(inLine);
	return inFloat;
      }
      catch ( NumberFormatException FlEx) {
	return new ErrorString( "TextPrompt: Value: " + inLine 
				+ " could not be cast as a Float");
      }
    }
    if ( inType.equals("Integer") ){
      try {
	Integer inInteger = new Integer( inLine );
	return inInteger;
      }
      catch ( NumberFormatException IntEx ) {
	return new ErrorString( "TextPrompt: Value: " + inLine 
				+ " could not be cast as an Integer");
      }
    }
    return new ErrorString("TextPrompt: ERROR");

  }

  /* ---------------------- getDocumentation -------------------------*/
  public String getDocumentation() {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator provides a method to get input from a ");
    s.append("user from the command line text window.  This operator takes ");
    s.append("a Prompt string, type string and default string as arguments ");
    s.append("and reads back a line of user input from the standard input");
    s.append("@assumptions The input text is either a Float, Integer or ");
    s.append("String.");
    s.append("@algorithm ");
    s.append("The input string is written to standard out and input is read from ");
    s.append("standard in.  The routine tries to convert the input to the ");
    s.append("specified type.  If the value does not convert to this type than an ");
    s.append("ErrorString is returned");
    s.append("@param prompt Prompt text for the user input");
    s.append("@param type Valid options are String, Float, and Integer. ");
    s.append("this is case sensitive.");
    s.append("@param default A value, shown with the prompt, to be uses as the ");
    s.append("default if only white space is input.");
    s.append("@return input value as Integer, Float or String as appropriate from ");
    s.append("input type.  If the value is not converted to the specified type " );
    s.append("an ErrorString is returned");
    return s.toString();
  }

  public static void main ( String args[] ) {

    String prompt = args[0];
    String type = args[1];
    String def = args[2];
    
    Operator op = new TextPrompt( prompt, type, def );
    Object obj = op.getResult();
   
    System.out.println ( "Object returned: \n" + obj);
    System.exit(0);
  }

}
