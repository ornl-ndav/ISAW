/* 
 * File: Convert2Tran3D.java
 *  
 * Copyright (C) 2011     Ruth Mikkelson
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
 *            University of Wisconsin-Stout
 *            Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date: 2010-10-25 09:37:07 -0500 (Mon, 25 Oct 2010) $$
 * $Revision: 21066 $
 */

package Operators.TOF_SCD;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 * This operator is a wrapper around 
@see Operators.TOF_SCD.IndexingUtils#Convert2Tran3D(java.util.Vector)
 */
public class Convert2Tran3D extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public Convert2Tran3D(){
     super("Convert2Tran3D");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "Convert2Tran3D";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new ArrayPG("UB Matrix",new java.util.Vector()));
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
      S.append("This operator converts a UB matrix to a Tran3D object so it can be used by methods requiring a");
      S.append(" Tran3D argument");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.IndexingUtils#Convert2Tran3D(java.util.Vector)");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The input is a Vector of 9 floats or a Vector or 3 Vector of 3 floats.");
      S.append("@param   ");
      S.append("A vector of float values corresponding to a UB matrix.");
      S.append("@return A Tran3D object corresponding to this UB matrix");
      S.append("@error ");
      S.append("");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "HIDDENOPERATOR"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.util.Vector UB = (java.util.Vector)(getParameter(0).getValue());
         gov.anl.ipns.MathTools.Geometry.Tran3D Xres=Operators.TOF_SCD.IndexingUtils.Convert2Tran3D(UB );

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



