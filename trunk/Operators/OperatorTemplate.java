/*
 * File:  OperatorTemplate.java 
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
 * Revision 1.3  2002/02/22 20:45:05  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.2  2001/11/27 18:20:13  dennis
 * Added operator title to constructor java docs.
 *
 * Revision 1.1  2001/11/21 21:27:42  dennis
 * Example of user-supplied add-on operator.
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.dataset.*;
import java.util.*;

/** 
 *  This operator provides a "template" for writing operator "plug-ins" for 
 *  Isaw.  To make a custom operator for use with ISAW, rename this file, then
 *  modify the class name, and title to an appropriate name, such as 
 *  MyOperator.java for the file and MyOperator for the class. Place the 
 *  new file in the Operators subdirectory of the Isaw home directory, or of
 *  your home directory.  Next, modify the parameters, command name and 
 *  the calculation performed as needed.  This template includes a main 
 *  program that can be used to test the operator separately.  You should also
 *  modify the main program appropriately, to separately test your operator.
 *  The operator must be compiled before Isaw is started, so that Isaw can 
 *  load the class file.  PLEASE ALSO REPLACE THE TEMPLATE COMMENTS WITH 
 *  COMMENTS APPROPRIATE FOR YOUR OPERATOR.
 */
public class OperatorTemplate extends GenericSpecial
{
  private static final String TITLE = "Operator Template";

 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "Operator Template" and a  default list of
  *  parameters.
  */  
  public OperatorTemplate()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator with title "Operator Template" and the specified list
  *  of parameters.  The getResult method must still be used to execute
  *  the operator.
  *
  *  @param  ds          Sample DataSet to process.
  *  @param  int_val     Sample integer parameter
  *  @param  float_val   Sample float parameter
  *  @param  bool_val    Sample boolean parameter
  *  @param  string_val  Sample String parameter
  */
  public OperatorTemplate( DataSet ds, 
                           int     int_val,
                           float   float_val,
                           boolean bool_val,
                           String  string_val )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", ds) );
    addParameter( new Parameter("integer parameter", new Integer(int_val) ) );
    addParameter( new Parameter("float parameter", new Float(float_val) ) );
    addParameter( new Parameter("boolean parameter", new Boolean(bool_val) ) );
    addParameter( new Parameter("String parameter", new String(string_val) ) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "OperatorTemplate", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "OperatorTemplate";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", DataSet.EMPTY_DATA_SET ) );
    addParameter( new Parameter("integer parameter", new Integer(0) ) );
    addParameter( new Parameter("float parameter", new Float(0) ) );
    addParameter( new Parameter("boolean parameter", new Boolean(false) ) );
    addParameter( new Parameter("String parameter", new String("Template") ) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return  If successful, this template just returns a String indicating
  *           what the paramters were, and that the operator executed.  The
  *           code that does the work of the operator goes here. 
  */
  public Object getResult()
  {
    DataSet ds        =  (DataSet)(getParameter(0).getValue());
    int     int_val   = ((Integer)(getParameter(1).getValue())).intValue();
    float   float_val = ((Float)  (getParameter(2).getValue())).floatValue();
    boolean bool_val  = ((Boolean)(getParameter(3).getValue())).booleanValue();
    String  string_val=  (String)  getParameter(4).getValue();

    // Here is where calculations would be done, using the parameters, to 
    // produce some result.  In this OperatorTemplate, we just return a 
    // String with the parameters.  Operators that work with DataSets can
    // add an entry to the list of operations applied to the DataSet, using
    // the addLog_entry() method, as shown below:

    ds.addLog_entry("Applied the OperatorTemplate");

    return "Operator Template called using " + ds +
           ", " + int_val   +
           ", " + float_val +
           ", " + bool_val  +
           ", " + string_val;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new OperatorTemplate();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
     System.out.println("Test of OperatorTemplate starting...");

     // Test the template by constructing and running it, specifyinge
     // values for all of the parameters.
     OperatorTemplate op = new OperatorTemplate( DataSet.EMPTY_DATA_SET, 
                                             1, 3.14159f, true, "Test String");
     Object obj = op.getResult();
     System.out.println("Using test parameters, the operator returned: ");
     System.out.println( (String)obj );

     // Test the template by constructing and running it, this time with the
     // default constructor.
     op = new OperatorTemplate();
     obj = op.getResult();
     System.out.println("Using default parameters, the operator returned: ");
     System.out.println( (String)obj );

     System.out.println("Test of OperatorTemplate done.");
  }
}
