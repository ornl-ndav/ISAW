/*
 * File:  HelloOperator.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
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
 * Revision 1.3  2005/08/24 19:14:47  dennis
 * Modified getCategoryList() to place operator in the
 * Macros, Examples, Operator(old style) menu.
 *
 * Revision 1.2  2004/05/12 14:04:35  dennis
 * Removed unused import.
 *
 * Revision 1.1  2004/05/07 16:10:41  dennis
 * Moved from Operators package to new Operators.Example package
 *
 * Revision 1.8  2003/10/15 02:37:54  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.7  2003/05/28 18:59:19  pfpeterson
 * Changed System.getProperty to SharedData.getProperty
 *
 * Revision 1.6  2003/04/02 15:04:37  pfpeterson
 * Now uses IParameterGUI rather than IParameter.
 *
 * Revision 1.5  2003/02/03 18:52:38  dennis
 * Added getDocumentation() operator. (Joshua Olson)
 *
 * Revision 1.4  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/02/22 20:45:02  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.2  2001/11/27 18:18:00  dennis
 * Added operator title to constructor java docs.
 *
 */
package Operators.Example;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Example.*;
import DataSetTools.parameter.StringPG;
import DataSetTools.util.SharedData;
import java.util.*;

/** 
 *    This operator provides an example of an operator that takes a 
 *  persons name as a parameter and returns a string that says hello.
 *
 *  <p>  In order to be used from Isaw, this operator must be compiled and the
 *  resulting class file must be placed in one of the directories that Isaw
 *  looks at for operators, such as the ../Operators subdirectory of the 
 *  Isaw home directory.  For details on what directories are searched, see
 *  the Operator-HOWTO file, or the Isaw user manual.
 *
 *  <p>
 *  NOTE: This operator can also be run as a separate program, since it
 *  has a main program for testing purposes.  The main program merely uses 
 *  the operator to load a simple test file and pops up a view of the 
 *  data.
 */

public class HelloOperator extends GenericExample
{
  private static final String TITLE = "Hello Operator";

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  *  Creates operator with title "Hello Operator" and a  default list of 
  *  parameters.
  */  
  public HelloOperator()
  {
    super( TITLE );
  }

 /* ----------------------------- Constructor ----------------------------- */
 /** 
  *  Creates operator with title "Hello Operator" and the specified list 
  *  of parameters.  The getResult method must still be used to execute 
  *  the operator.
  *  
  *  @param  user_name   The name of the person that the operator will say
  *                      hello to.
  */
  public HelloOperator( String user_name )
  {
    this();
    getParameter(0).setValue(user_name);
  }

 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "SayHello", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "SayHello";
  }


/* ---------------------------- getCategoryList -------------------------- */
/**
 *  Get the list of categories describing where this operator should appear
 *  in the menu system.
 *
 *  @return an array of strings listing the menu where the operator 
 *  should appear.
 */
   public String[] getCategoryList()
   {
     return new String[]{ "Macros", "Examples", "Operator(old style)" };
   }

  
 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                                 
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview This operator provides an example of an operator ");
    s.append("that takes a person's name as a parameter and returns a string ");
    s.append("that says hello. \n\n In order to be used from Isaw, this ");
    s.append("operator must be compiled and the resulting class file must ");
    s.append("be placed in one of the directories that Isaw looks at for ");
    s.append("operators, such as the ../Operators subdirectory of the Isaw ");
    s.append("home directory.  For details on what directories are ");
    s.append("searched, see the Operator-HOWTO file, or the Isaw user ");
    s.append("manual. \n\n NOTE: This operator can also be run as a ");
    s.append("separate program, since it has a main program for testing ");
    s.append("purposes.  The main program merely uses the operator to load ");
    s.append("a simple test file and pops up a view of the data. ");
    s.append("@assumptions The user will pass in their name as a string.\n");                                                               
    s.append("@algorithm A string (intended to be the user's name) is ");
    s.append("passed in by the user.  In the event that an empty string is ");
    s.append("passed, the operator will substitute 'John Doe'.  The ");
    s.append("operator then returns a string that prints 'Hello' to ");
    s.append("the user.");
    s.append("@param user_name The name for the operator to greet.");
    s.append("@return Returns the string 'Hello [name you passed in], ");
    s.append("how are you today?'");
    return s.toString();
  }  

 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new StringPG("Name", "John Doe") );
  }

 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
  *
  *  @return  This returns a new String that says hello to the person
  *           named by the Name parameter.
  */
  public Object getResult()
  {
    String user_name = (String)(getParameter(0).getValue());

    return "Hello " + user_name + ", how are you today?" ;
  }

 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.  Operators need a clone method, so 
  *  that Isaw can make copies of them when needed.
  */
  public Object clone()
  { 
    Operator op = new HelloOperator();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("Test of HelloOperator starting....");

                                                 // since we're not running
                                                 // in Isaw here, get the user
                                                 // name from the system.
    String name = SharedData.getProperty( "user.name" );

                                                 // make and run the operator
    Operator op  = new HelloOperator( name );
    Object   obj = op.getResult();
                                                 // display the string returned
    System.out.println("Operator returned: " + obj );

    System.out.println("Test of HelloOperator done.");
  }
}
