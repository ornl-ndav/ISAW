/*
 * File:  ActivatePrompt.java 
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
 * $Log$
 * Revision 1.7  2003/10/14 21:50:58  dennis
 * Fixed javadoc error so that it builds cleanly on jdk 1.4.2
 *
 * Revision 1.6  2003/02/04 16:28:14  pfpeterson
 * Now uses IParameterGUI and changed returns on errors to ErrorString.
 *
 * Revision 1.5  2002/11/27 23:30:05  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/10/29 16:00:23  dennis
 * Added getDocumentation method, and $Log$
 * Added getDocumentation method, and Revision 1.7  2003/10/14 21:50:58  dennis
 * Added getDocumentation method, and Fixed javadoc error so that it builds cleanly on jdk 1.4.2
 * Added getDocumentation method, and
 * Added getDocumentation method, and Revision 1.6  2003/02/04 16:28:14  pfpeterson
 * Added getDocumentation method, and Now uses IParameterGUI and changed returns on errors to ErrorString.
 * Added getDocumentation method, and
 * Added getDocumentation method, and Revision 1.5  2002/11/27 23:30:05  pfpeterson
 * Added getDocumentation method, and standardized header
 * Added getDocumentation method, and tag. (Mike Miller)
 *
 *
 */
package Operators.Calculator;

import DataSetTools.dataset.*;
import DataSetTools.materials.*;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.Calculator.*;
import DataSetTools.parameter.*;
import DataSetTools.util.ErrorString;
import java.text.DecimalFormat;
import java.util.*;

/** 
 *  
 */
public class ActivatePrompt extends GenericCalculator
{
  private static final String TITLE = "Activate Prompt Activity";

 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "ActivatePrompt" and a default list
  *  of parameters.
  */  
  public ActivatePrompt()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator with title "ActivateStorage" and the specified
  *  list of parameters.  The getResult method must still be used to
  *  execute the operator.
  *
  *  @param  sample      Sample material composition
  */
  public ActivatePrompt( String sample ){//,
      this(); 
      getParameter(0).setValue(sample);
  }

 /* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns description/attributes of ActivatePrompt
  *   for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator calculates and returns the ");
    Res.append("prompt activity (nCi/g) for a given sample\n");
    Res.append("@algorithm Given a sample of type Material ");
    Res.append("the prompt activity will be calculated\n");
    Res.append("@param String sample\n");
    Res.append("@return the String containing the numerical value ");
    Res.append("of the prompt activity followed by units (nCi/g)\n"); 
    Res.append("@error sample string is null, no set\n");
    Res.append("@error sample not valid\n");
    
    return Res.toString();
    
  }


 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "ActivatePrompt", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "ActivatePrompt";
  }
  
 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters. During testing the
  * parameters are set for SEPD at IPNS.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter(new MaterialPG("Sample Composition", "La,Mn,O_3"));
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return If successful, this operator returns the prompt activity
  *  of the activated sample.
  */
  public Object getResult(){
        // get the material
        String sample=getParameter(0).getValue().toString();
        if( sample==null || sample.length()<=0 )
          return new ErrorString("Invalid sample: "+sample);
        Material material=null;
        try{
          material=new Material(sample);
        }catch(InstantiationError e){
          return new ErrorString("Invalid sample: "+sample);
        }
        if(material==null || material.numAtoms()<=0)
          return new ErrorString("Invalid sample: "+sample);

        // set up the return string
	String rs=null;

	// calculate the prompt activity
	float prompt=0.0f;
	for( int i=0 ; i<material.numAtoms() ; i++ ){
	    prompt=prompt+material.promptAct(i)*material.massFrac(i);
	}

	// format the result
	if(prompt>0.0f){
	    if(prompt==Float.POSITIVE_INFINITY){
		rs="radioactive sample";
	    }else{
		rs=(new DecimalFormat("#######0.")).format(prompt)+" nCi/g";
	    }
	}else{
	    rs="no activation";
	}

	//rs=rs+" ["+material.toString()+"]";
	return rs;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    ActivatePrompt op = new ActivatePrompt();
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
     System.out.println("Test of ActivatePrompt starting...");
     String material=null;

     // Test the operator by constructing and running it, specifying
     // values for all of the parameters.
     material="Ge";
     ActivatePrompt op = new ActivatePrompt( material );
     String output = (String)op.getResult();
     System.out.println("Using "+material+", the operator returned: ");
     System.out.println( output );

     // Test the operator by constructing and running it, specifying
     // values for all of the parameters.
     material="Si,Ge";
     op = new ActivatePrompt( material );
     output = (String)op.getResult();
     System.out.println("Using "+material+", the operator returned: ");
     System.out.println( output );

     // Test the operator by constructing and running it, specifying
     // values for all of the parameters.
     material="Y,Ba_2,Cu_3,O_7";
     op = new ActivatePrompt( material );
     output = (String)op.getResult();
     System.out.println("Using "+material+", the operator returned: ");
     System.out.println( output );

     // Will dump raw help information about the ActivatePrompt class to screen
     System.out.println( op.getDocumentation() );

     // Test the operator by constructing and running it, this time with the
     // default constructor.
     /* op = new ActivateStorage();
	obj = op.getResult();
	System.out.println("Using default parameters, the operator returned: ");
	System.out.println( (String)obj ); */

     System.out.println("Test of ActivateStorage done.");
  }
}
