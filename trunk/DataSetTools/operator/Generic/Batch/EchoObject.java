/*
 * File:  EchoObject.java 
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 *  $Log$
 *  Revision 1.5  2006/07/10 16:25:58  dennis
 *  Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 *  Revision 1.4  2002/12/03 22:10:53  dennis
 *  Added getDocumentation() method and java docs for getResult().
 *  (Shannon Hintzman)
 *
 *  Revision 1.3  2002/11/27 23:20:52  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:05:18  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 20:57:19  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic.Batch;

import gov.anl.ipns.Parameters.IParameter;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
 * This operator prints the result of calling an object's toString() method
 * on the standard output and returns the string as the result of the operation.
 *
 */

public class EchoObject extends    GenericBatch 
                                   implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct a default EchoObject operator with a default Object.
   */

  public EchoObject( )
  {
    super( "Echo Object" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an EchoObject operator that will print the given object using
   *  the toString() method.
   *
   *  @param  ob   The object to print. 
   */

  public EchoObject( Object ob )
  {
    this();
    IParameter parameter = getParameter(0);
    parameter.setValue( ob );
  }

  /*----------------------------getDocumentation-----------------------------*/
  
   public String getDocumentation()
   {
   	StringBuffer Res = new StringBuffer();
	
	Res.append("@overview This operator prints the result of calling an ");
	Res.append("object's toString() method on the standard output.");
		
	Res.append("@algorithm Calls the toString() method of an object.");
	
	Res.append("@param ob - The object to print.");
	
	Res.append("@return Returns a String that is the result of calling ");
	Res.append("the object's toString() method.");
	
	return Res.toString();
   
   }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: 
   * in this case, Echo
   */
   public String getCommand()
   {
     return "Echo";
   }


 /* -------------------------- setDefaultParameters ------------------------ */
 /**
  *  Set the object to an default Object.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters

     Parameter parameter= new Parameter("Object to Echo ", null );
     addParameter( parameter );
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
  *  @return Returns a String.  The String is the result that comes from 
  *  calling the object's toString() method.
  **/
  
  public Object getResult()
  {
    Object ob = getParameter(0).getValue();

    String result = ob.toString();

    System.out.println( result );

    return result;
  }  

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */ 
  public static void main( String[] args )
  {
    EchoObject op = new EchoObject();
 
    String list[] = op.getCategoryList();
    System.out.println( "Categories are: " );
    for ( int i = 0; i < list.length; i++ )
      System.out.println( list[i] );
      
    System.out.println(op.getDocumentation());
    
    //System.out.println(op.getResult());
    //Calling the getResult() method creates a NullPointerException
  }

}
