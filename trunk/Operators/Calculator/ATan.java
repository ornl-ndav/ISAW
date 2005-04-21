/*
 * File:  ATan.java 
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
 *  Revision 1.2  2005/04/21 02:32:47  hammonds
 *  Fix Copyright
 *
 *  Revision 1.1  2005/04/13 13:56:05  hammonds
 *  Add new function to ISAW
 *
 *
 *
 */
package Operators.Calculator;

import DataSetTools.operator.Generic.Calculator.*;
import DataSetTools.parameter.*;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.text.DecimalFormat;
import java.util.Vector;

/** 
 * This operator calculates the arc tangent of the given angle in degrees
 * sample.
 */
public class ATan extends GenericCalculator{
  /**
   * Creates operator with title "Arc Tangent of Angle in Radians"
   */  
  public ATan(){
    super( "Arc Tangent of Angle in Radians" );
  }

  /** 
   * Creates operator with title "Arc Tangent of Angle in Radians" and the
   * specified list of parameters. The getResult method must still be
   * used to execute the operator.
   *
   * @param a value whose arc tangent is to be returned
   */
  public ATan( float a ){
    this(); 
    
    getParameter(0).setValue(new Float(a));
  }

  /**
   * Returns description/attributes of ATan for a user
   * activating the Help System
   */
  public String getDocumentation(){
    StringBuffer sb = new StringBuffer();

    // overview
    sb.append("@overview This operator calculates the arc tangent of the given value.");
    // algorithm
    sb.append("@algorithm Value is passed to java's Math.acos");
    // parameters
    sb.append("@param float value whose arc tangent is to be returned");
    // return
    sb.append("@return arc tangent of specified value in the range 0.0 to pi");
    // errors
    sb.append("@error returns message string if out of range");
    
    return sb.toString();
  }

  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "ATan", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "ATan";
  }

  /** 
   * Sets default values for the parameters.
   */
  public void setDefaultParameters(){
    // new vector of parameters
    parameters = new Vector();
    // add the input parameter
    addParameter( new FloatPG("Input Value", "1.0"));
  }

  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns the arc tangent of the specified value.
   */
  public Object getResult(){
    // get the input value
    float value  = ((Float)getParameter(0).getValue()).floatValue();
    //Check to see if the input value is in the required range.
    if ( (value < -1.0f) || (value > 1.0f)) {
      return new ErrorString("Input value is outside of the range -1 to 1");
    }
    float angle = (float)Math.atan((double)value);
    
    return (new Float(angle));
  }

  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    ATan op = new ATan();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    // try the operator with the default settings
    ATan op = new ATan();
    System.out.println("RESULT="+op.getResult());

    // Print help information
    System.out.println( op.getDocumentation() );

    System.exit(0);
  } 
}
