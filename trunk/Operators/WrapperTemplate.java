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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
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
package Operators;

import DataSetTools.operator.Wrappable;


/**
 * This class is a template for IPNS users to code their own Java routines.
 * This wrapper is used by the JavaWrapperOperator when it creates an
 * Operator.
 */

/*
 * You should change the name "WrapperTemplate" to the name you want (e.g.
 * Crunch, Integrate, etc.).
 */
public class WrapperTemplate implements Wrappable {
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
    return null;
  }

  /**
   * Please document what your Operator does.  It can be beneficial to
   * everyone.
   */
  public String getDocumentation(  ) {
    //uncomment the lines below and fill in what each asks for when you are
    //writing your documentation

    /*StringBuffer s = new StringBuffer(  );
       s.append( "@overview Place the overall description of your Operator in" );
       s.append( "here." );
       s.append( "@algorithm How does your Operator work?  Please provide a " );
       s.append( "succinct description here." );
       s.append( "@param Parameter 1 name and description." );
       s.append( "@param Parameter 2 name and description." );
       // more parameter names and descriptions
       // .
       // .
       // .
       s.append( "@return What result does your Operator return? For example: " );
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
