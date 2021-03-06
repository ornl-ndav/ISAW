/*
 * File:  WrapperTemplate.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.3  2006/03/15 19:31:59  rmikk
 * Changed the command name from (null) JavaWrappedOperator
 *
 * Revision 1.2  2005/08/25 15:53:09  dennis
 * No longer implements HiddenOperator, so it will now appear in menu.
 * Added getCategoryList() method to control where it appears.
 * getCategoryList() returns the logical menu WRAPPED_OP_EXAMPLES.
 *
 * Revision 1.1  2004/05/07 17:49:00  dennis
 * Moved WrapperTemplate from Operators to Operators/Example
 *
 * Revision 1.4  2004/03/01 18:32:48  dennis
 * Fixed acknowlegement in documentation.
 *
 * Revision 1.3  2004/02/16 19:46:26  bouzekc
 * Now implements HiddenOperator to keep it out of the menus.
 *
 * Revision 1.2  2003/12/15 02:44:08  bouzekc
 * Removed unused imports.
 *
 * Revision 1.1  2003/10/29 01:16:16  bouzekc
 * Added to CVS.
 *
 */
/*
 * If you place this file in, for example,
 * DataSetTools/operator/Generic/Special, change the line below to
 *
 * package.DataSetTools.operator.Generic.Special;
 *
 * This will allow you to have your Operator show up in the correct ISAW menu
 * (in this case, under Generic -> Special.
 */
package Operators.Example;

import DataSetTools.operator.*;


/**
 * This class is a template for IPNS users to code their own Java routines.
 * This wrapper is used by the JavaWrapperOperator when it creates an
 * Operator.
 */

/*
 * You should change the name "WrapperTemplate" to the name you want (e.g.
 * Crunch, Integrate, etc.).  The HiddenOperator implementation is used so that
 * Operator will not show up in the menus.  You will generally not use it.
 *
 * To repeat:  You will generally not implement HiddenOperator unless you
 * do not want your Operator to show up in the menu.
 */
public class WrapperTemplate implements Wrappable, IWrappableWithCategoryList
{
  //~ Instance fields **********************************************************

  /*
   * Place the variables you are going to use here.
   * Each MUST HAVE the "public" before it.
   */
  public float tof = 10.0f;
  public int bins  = 10;

  //~ Methods ******************************************************************

  /**
   * Uncomment the lines below if you want to use your own command name.
   * Normally the command name is the name of your class file (e.g.
   * ArtsIntegrate) in all capital letters.
   */
  public String getCommand(  ) {
    return "WrapperTemplate";
  }


/**
  * Get an array of strings listing the operator category names  for 
  * this operator. The first entry in the array is the 
  * string: Operator.OPERATOR. Subsequent elements of the array determine
  * which submenu this operator will reside in.
  * 
  * @return  A list of Strings specifying the category names for the
  *          menu system 
  */
   public String[] getCategoryList()
   {
     return Operator.WRAPPED_OP_EXAMPLES;
   }


  /**
   * Please document what your Operator does.  It can be beneficial to
   * everyone.
   */
  public String getDocumentation(  ) {
    //uncomment the lines below and fill in what each asks for when you are
    //writing your documentation

    /*StringBuffer s = new StringBuffer(  );
       s.append( "@overview Place the overall description of your Operator in");
       s.append( "here." );
       s.append( "@algorithm How does your Operator work?  Please provide a " );
       s.append( "succinct description here." );
       s.append( "@param Parameter 1 name and description." );
       s.append( "@param Parameter 2 name and description." );
       // more parameter names and descriptions
       // .
       // .
       // .
       s.append( "@return What result does your Operator return? For example: ");
       s.append( "A DataSet that has had its X-axis converted to time-of-flight." );
       s.append( "@error Describe any unusual or notable errors that could " );
       s.append( "occur. For example: Error occurs if number of bins is zero." );
       return s.toString(  );*/

    //remove this line if you have added documentation
    return null;
  }

  /**
   * This method/function is where you will do the majority of your
   * calculations.  Do not pass in any parameters.  Use the variables you
   * declared as "public" at the top of the file.
   */
  public Object calculate(  ) {
    /* This method returns a result.  At some point, you should assign the end
     * result of your calculations to this variable (e.g. result =
     * calculatedResult).  Do not remove this line.
     */
    Object result = null;

    /* Do your work here.  You may refer to the parameters passed in
     * directly( e.g. param1, param2).
     */

    //do not remove this line
    return result;
  }
}
