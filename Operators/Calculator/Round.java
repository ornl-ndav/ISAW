/*
 * File:  Round.java 
 *
 * Copyright (C) 2005, John Hammonds
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
 * Contact : John P. Hammonds <jphamonds@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2005/05/25 18:39:21  dennis
 *  Removed unused imports.
 *
 *  Revision 1.1  2005/04/26 21:14:45  hammonds
 *  New method for scripting language.
 *
 *
 *
 *
 */
package Operators.Calculator;

import DataSetTools.operator.Generic.Calculator.*;
import DataSetTools.parameter.*;

import java.util.Vector;

/** 
 * This operator rounds a float to the nearest int
 */
public class Round extends GenericCalculator{
  /**
   * Creates operator with title "Round Float to Int"
   */  
  public Round(){
    super( "Round Float to Int." );
  }

  /** 
   * Creates operator with title "Round Float to int" and the
   * specified list of parameters. The getResult method must still be
   * used to execute the operator.
   *
   * @param a value whose arc cosine is to be returned
   */
  public Round( float a ){
    this(); 
    
    getParameter(0).setValue(new Float(a));
  }

  /**
   * Returns description/attributes of Round for a user
   * activating the Help System
   */
  public String getDocumentation(){
    StringBuffer sb = new StringBuffer();

    // overview
    sb.append("@overview This operator rounds a float to an int.");
    // algorithm
    sb.append("@algorithm Value is passed to java's Math.round method.  This takes a float adds .5 and then uses floor method to round down");
    // parameters
    sb.append("@param float value to be rounded");
    // return
    sb.append("@return rounded value");
    // errors
    
    return sb.toString();
  }

  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "Round", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "Round";
  }

  /** 
   * Sets default values for the parameters.
   */
  public void setDefaultParameters(){
    // new vector of parameters
    parameters = new Vector();
    // add the input parameter
    addParameter( new FloatPG("Input Value", "0.707107"));
  }

  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns the arc cosine of the specified value.
   */
  public Object getResult(){
    // get the input value
    float value  = ((Float)getParameter(0).getValue()).floatValue();
    //Check to see if the input value is in the required range.
    int newVal = Math.round(value);
    
    return (new Integer(newVal));
  }

  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Round op = new Round();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    // try the operator with the default settings
    Round op = new Round();
    System.out.println("RESULT="+op.getResult());

    // Print help information
    System.out.println( op.getDocumentation() );

    System.exit(0);
  } 
}
