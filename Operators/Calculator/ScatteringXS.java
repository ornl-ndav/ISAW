/*
 * File:  ScatteringXS.java 
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
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
 *  Revision 1.1  2003/02/04 16:02:59  pfpeterson
 *  Added to CVS.
 *
 */
package Operators.Calculator;

import DataSetTools.materials.*;
import DataSetTools.operator.Generic.Calculator.*;
import DataSetTools.parameter.*;
import DataSetTools.util.ErrorString;
import java.text.DecimalFormat;
import java.util.Vector;

/** 
 * This operator calculates the scattering cross section of the given
 * sample.
 */
public class ScatteringXS extends GenericCalculator{
  /**
   * Creates operator with title "Scattering Cross Section" and a
   * default list of parameters.
   */  
  public ScatteringXS(){
    super( "Scattering Cross Section" );
  }

  /** 
   * Creates operator with title "Scattering Cross Section" and the
   * specified list of parameters. The getResult method must still be
   * used to execute the operator.
   *
   * @param sample Sample material composition
   * @param type The type of cross section to calculate (total=0,
   * coherent=1, incoherent=2)
   */
  public ScatteringXS( String sample, int type ){
    this(); 
    
    getParameter(0).setValue(sample);
    if(type==0)
      getParameter(1).setValue("Total Scattering");
    else if(type==1)
      getParameter(1).setValue("Coherent Scattering");
    else if(type==2)
      getParameter(1).setValue("Incoherent Scattering");
  }

  /**
   * Returns description/attributes of ScatteringXS for a user
   * activating the Help System
   */
  public String getDocumentation(){
    StringBuffer sb = new StringBuffer();

    // overview
    sb.append("@overview This operator calculates the scattering cross section of the given sample.");
    // algorithm
    sb.append("@algorithm Given a sample the cross section is calculated by summing the cross section of each atom times its normalized concentration.");
    // parameters
    sb.append("@param String sample to calculate information for");
    sb.append("@param int type which of the three types of cross sections to calculate");
    // return
    sb.append("@return A formatted string containing the scattering cross section of the sample.");
    // errors
    sb.append("@error Anything wrong with the sample");
    
    return sb.toString();
  }

  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "ScatteringXS", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "ScatteringXS";
  }

  /** 
   * Sets default values for the parameters. During testing the
   * parameters are set for SEPD at IPNS.
   */
  public void setDefaultParameters(){
    // new vector of parameters
    parameters = new Vector();
    // add the material parameter
    addParameter( new MaterialPG("Sample Composition", "La,Mn,O_3"));
    // add the type parameter
    ChoiceListPG clpg=new ChoiceListPG("Type","Total Scattering");
    clpg.addItem("Coherent Scattering");
    clpg.addItem("Incoherent Scattering");
    addParameter( clpg );
  }

  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns the prompt activity
   *  of the activated sample.
   */
  public Object getResult(){
    // get the material
    String matString  = getParameter(0).getValue().toString();
    if( matString==null || matString.length()<=0 ){
      return new ErrorString("Invalid sample: "+matString);
    }
    Material material = null;
    try{
      material=new Material(matString);
    }catch(InstantiationError e){
      return new ErrorString("Invalid sample: "+matString);
    }
    if(material.numAtoms()<=0){
      return new ErrorString("Invalid sample: "+matString);
    }

    // get the type of scattering cross section to calculate
    String typeString = getParameter(1).getValue().toString();
    int type=0;
    if( typeString.indexOf("Total Scattering")>=0 )
      type=0;
    else if( typeString.indexOf("Coherent Scattering")>=0 )
      type=1;
    else if( typeString.indexOf("Incoherent Scattering")>=0 )
      type=2;
    else
      return new ErrorString("Unknown type selected: "+typeString);
        

    // calculate the cross section
    float xs=0f;
    float atom_xs=0f;
    for( int i=0 ; i<material.numAtoms() ; i++ ){
      // get the appropriate cross section for each atom
      if(type==0)
        atom_xs=material.atomAt(i).xs_tot();
      else if(type==1)
        atom_xs=material.atomAt(i).xs_coh();
      else if(type==2)
        atom_xs=material.atomAt(i).xs_inc();

      // weighted addition 
      xs=xs+atom_xs*material.normConc(i);
    }

    return (new DecimalFormat("#######0.000")).format(xs)+" barn";
  }

  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    ScatteringXS op = new ScatteringXS();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    // try the operator with the default settings
    ScatteringXS op = new ScatteringXS();
    System.out.println("RESULT="+op.getResult());

    // Print help information
    System.out.println( op.getDocumentation() );

    System.exit(0);
  } 
}
