/*
 * File:  ActivateStorage.java 
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
 *  Revision 1.5  2003/02/04 16:28:14  pfpeterson
 *  Now uses IParameterGUI and changed returns on errors to ErrorString.
 *
 *  Revision 1.4  2002/11/27 23:30:05  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/29 16:02:24  dennis
 *  Added getDocumentation method, and $Log$
 *  Added getDocumentation method, and Revision 1.5  2003/02/04 16:28:14  pfpeterson
 *  Added getDocumentation method, and Now uses IParameterGUI and changed returns on errors to ErrorString.
 *  Added getDocumentation method, and
 *  Added getDocumentation method, and Revision 1.4  2002/11/27 23:30:05  pfpeterson
 *  Added getDocumentation method, and standardized header
 *  Added getDocumentation method, and tag. (Mike Miller)
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
public class ActivateStorage extends GenericCalculator
{
  private static final String TITLE = "Activate Storage Time";

 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "ActivateStorage" and a default list
  *  of parameters.
  */  
  public ActivateStorage()
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
  *  @param  current     Facility beam current
  *  @param  inst_fac    Instrument factor
  */
  public ActivateStorage( String sample,
                           float  current,
                           float  inst_fac)
  {
    this(); 
    getParameter(0).setValue(sample);
    getParameter(1).setValue(new Float(current));
    getParameter(2).setValue(new Float(inst_fac));
  }

/* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns description/attributes of ActivateStorage
  *   for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator calculates and returns the ");
    Res.append("storage time (in days) for a given sample\n");
    Res.append("@algorithm Given a sample, its beam current, and the ");
    Res.append("instrument factor, the storage time will be calculated\n");
    Res.append("@param String sample\n");
    Res.append("@param float  current\n");
    Res.append("@param float  inst_fac\n");
    Res.append("@return the String containing the numerical value ");
    Res.append("of the storage time followed by units (days)\n"); 
    Res.append("@error sample string is null, no set\n");
    Res.append("@error sample not valid\n");
    
    return Res.toString();
    
  }


 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "ActivateStorage", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "ActivateStorage";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters. During testing the
  * parameters are set for SEPD at IPNS.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new MaterialPG("Sample Composition", "La,Mn,O_3"));
    addParameter( new FloatPG("Beam Current (in microAmp)", 15f)  );
    addParameter( new FloatPG("Instrument Factor (LANSCE HIPD=1.0)", 0.5f) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return If successful, this operator returns the storage time of
  *  the activated sample.
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

        // get the current
        float current=Float.NaN;
        if( getParameter(1) instanceof FloatPG ){
          current=((FloatPG)getParameter(1)).getfloatValue();
        }else{
          Object val=getParameter(1).getValue();
          if( val instanceof Float )
            current=((Float)val).floatValue();
          else if( val instanceof Integer )
            current=((Integer)val).floatValue();
          else if( val instanceof Double )
            current=((Double)val).floatValue();
          else
            return new ErrorString("current parameter of unknown type");
          val=null;
        }
        if( Float.isNaN(current) )
          return new ErrorString("Current is not a number");

        // get the instrument factor
        float inst_fac=Float.NaN;
        if( getParameter(2) instanceof FloatPG ){
          inst_fac=((FloatPG)getParameter(2)).getfloatValue();
        }else{
          Object val=getParameter(2).getValue();
          if( val instanceof Float )
            inst_fac=((Float)val).floatValue();
          else if( val instanceof Integer )
            inst_fac=((Integer)val).floatValue();
          else if( val instanceof Double )
            inst_fac=((Double)val).floatValue();
          else
            return new ErrorString("instrument factor parameter of unknown "
                                   +"type");
          val=null;
        }
        if( Float.isNaN(inst_fac) )
          return new ErrorString("instrument factor is not a number");

        // set up the return string
	String rs=null;


	// the storage time of the sample is the longest element
	// storage time
	float time=0.0f;
	float tempT=0.0f;
	String atom=material.atomAt(0).toString();
	float C0=inst_fac*current/100.0f;
	for( int i=0 ; i<material.numAtoms() ; i++ ){
	    tempT=getT(material.storeT(i),material.massFrac(i)*C0);

	    // check if the new value is larger than the previous
	    // largest value
	    if(tempT>time){
		time=tempT;
		atom=material.atomAt(i).element();
	    }
	}

	// format the result
	if(time>0.0f){
	    rs=(new DecimalFormat("#######0.00")).format(time)+" days";
	    if(material.numAtoms()>1){
		rs=rs+" due to "+atom;
	    }
	}else{
	    rs="no activation";
	}
	
	//rs=rs+" ["+material.toString()+"]";
	return rs;
  }

 /* ------------------------------- getT -------------------------------- */ 
 /** 
  *  Calculate the storage time for a single atom.
  */
  private float getT( float Ts, float C){
      //System.out.println("Ts="+Ts+" C="+C);
      return Ts*(0.693f+0.1f*(float)Math.log((double)C));
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    ActivateStorage op = new ActivateStorage();
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
     System.out.println("Test of ActivateStorage starting...");
     String material=null;

     // Test the operator by constructing and running it, specifyinge
     // values for all of the parameters.
     material="Ge";
     ActivateStorage op = new ActivateStorage( material, 15.0f, 0.5f );
     String output = (String)op.getResult();
     System.out.println("Using "+material+", the operator returned: ");
     System.out.println( output );

     // Test the operator by constructing and running it, specifyinge
     // values for all of the parameters.
     material="Si,Ge";
     op = new ActivateStorage( material, 15.0f, 0.5f );
     output = (String)op.getResult();
     System.out.println("Using "+material+", the operator returned: ");
     System.out.println( output );

     // Test the operator by constructing and running it, specifyinge
     // values for all of the parameters.
     material="Y,Ba_2,Cu_3,O_7";
     op = new ActivateStorage( material, 75.0f, 0.025f );
     output = (String)op.getResult();
     System.out.println("Using "+material+", the operator returned: ");
     System.out.println( output );
     
     // Will dump raw help information about the ActivateStorage class to screen
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
