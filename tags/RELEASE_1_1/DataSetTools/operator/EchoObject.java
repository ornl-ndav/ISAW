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
 *  $Log$
 *  Revision 1.4  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.3  2001/04/26 19:09:04  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.2  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *  Revision 1.1  2000/11/07 16:23:10  dennis
 *  Operator to display messages on the console terminal.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;

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
    Parameter parameter = getParameter(0);
    parameter.setValue( ob );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, Echo
   */
   public String getCommand()
   {
     return "Echo";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
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

  public Object getResult()
  {
    Object ob     = getParameter(0).getValue();

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
    Operator op = new EchoObject();
 
    String list[] = op.getCategoryList();
    System.out.println( "Categories are: " );
    for ( int i = 0; i < list.length; i++ )
      System.out.println( list[i] );
  }

}
