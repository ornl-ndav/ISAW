/*
 * File:  Sin.java 
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
 *  Revision 1.1  2005/04/21 02:46:00  hammonds
 *  Add in methods to provide trig to scrips
 *
 *  Revision 1.1  2005/04/12 21:51:23  hammonds
 *  Add trig methods for easy use in scripts.
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
 * This operator calculates the sin of the given angle
 */
public class Sin extends GenericCalculator{
  /**
   * Creates operator with title "Sin of Angle in Radians"
   */  
  public Sin(){
    super( "Sin of Angle in Radians" );
  }

  /** 
   * Creates operator with title "Sin of Angle in Radians" and the
   * specified list of parameters. The getResult method must still be
   * used to execute the operator.
   *
   * @param angle value in radians whose sine is to be returned
   */
  public Sin( float angle ){
    this(); 
    
    getParameter(0).setValue(new Float(angle));
  }

  /**
   * Returns description/attributes of Sin for a user
   * activating the Help System
   */
  public String getDocumentation(){
    StringBuffer sb = new StringBuffer();

    // overview
    sb.append("@overview This operator calculates the sine of the given value.");
    // algorithm
    sb.append("@algorithm Value is passed to java's Math.sin");
    // parameters
    sb.append("@param float value in radians whose sine is to be returned");
    // return
    sb.append("@return sine of specified angle");
    // errors
    sb.append("@error ");
    
    return sb.toString();
  }

  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "Sin", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "Sin";
  }

  /** 
   * Sets default values for the parameters.
   */
  public void setDefaultParameters(){
    // new vector of parameters
    parameters = new Vector();
    // add the input parameter
    addParameter( new FloatPG("Input Value", (new Float(((double)Math.PI)/2).toString())));
  }

  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns the sine of the specified value.
   */
  public Object getResult(){
    // get the input value
    float value  = ((Float)getParameter(0).getValue()).floatValue();
    //Check to see if the input value is in the required range.
    float angle = (float)Math.sin((double)value);
    
    return (new Float(angle));
  }

  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Sin op = new Sin();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    // try the operator with the default settings
    Sin op = new Sin();
    System.out.println("RESULT="+op.getResult());

    // Print help information
    System.out.println( op.getDocumentation() );

    System.exit(0);
  } 
}
