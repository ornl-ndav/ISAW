/* 
 * File: WriteMatrix.java
 *  
 * Copyright (C) 2007     Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson<Mikkelsonr@uwstout.edu>
 *            Department of Mathematics, Statistics and Computer Science
 *            Menomonie, WI 54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2007/08/23 21:05:03  dennis
 * Removed unused imports.
 *
 * Revision 1.1  2007/04/05 18:47:55  rmikk
 * An operator arount Util.WritMatrix. This writes the orientation matrix out
 *   given only the orientation matrix
 *
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class WriteMatrix extends GenericOperator implements HiddenOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public WriteMatrix(){
     super("WriteMatrix");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "WriteMatrix";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new SaveFilePG("File to Save matrix",new String("")));
      addParameter( new PlaceHolderPG("Orientation Matrix",new float[3][3]));
   }


   /**
    * Writes a string for the documentation of the operator provided by
    * the user.
    *
	 * @return  The documentation for the operator.
    */
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Write out the orientation matrix and lattice parameters to the matrix file.");
      S.append("@algorithm    "); 
      S.append("The lattice constants are calculated.  The errors are zero.");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("name of file to write to");
      S.append("@param   ");
      S.append("the orientation matrix");
      S.append("@error ");
      S.append("THe orientation matrix is null or not invertible.");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "Operator",
                     "Instrument Type",
                     "TOF_NSCD"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String Filename = getParameter(0).getValue().toString();
         float[][] UB = (float[][])(getParameter(1).getValue());
         gov.anl.ipns.Util.SpecialStrings.ErrorString Xres=DataSetTools.operator.Generic.TOF_SCD.Util.WriteMatrix(Filename,UB );

         return Xres;
       }catch( Throwable XXX){
        String[]Except = ScriptUtil.
            GetExceptionStackInfo(XXX,true,1);
        String mess="";
        if(Except == null) Except = new String[0];
        for( int i =0; i< Except.length; i++)
           mess += Except[i]+"\r\n            "; 
        return new ErrorString( XXX.toString()+":"
             +mess);
                }
   }
}



