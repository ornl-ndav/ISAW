/* 
 * File: GetUBMatrix.java
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
 * Revision 1.1  2007/04/05 18:46:35  rmikk
 * An operator made using the static method in GetUB that calculates an
 *   orientation matrix via autocorrelations
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
public class GetUBMatrix extends GenericOperator implements HiddenOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public GetUBMatrix(){
     super("GetUBMatrix");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "GetUBMatrix";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Peaks Vector",new java.util.Vector()));
      addParameter( new FloatPG("Max Side crystal(Real)",20f));
      addParameter( new FloatPG("Min Dx,Dy for new direction",.5f));
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
      S.append("Automatically determines the UB matrix given a peaks file.");
      S.append("@algorithm    "); 
      S.append("Uses a combination of autocorrelation and fraction of peaks that will fall on a plane within .2. The");
      S.append(" fraction is weighted twice the autocorrelation.");
      S.append(" ");
      S.append(" The resultant UB matrix has been run through blind to get standardized with the common conventions");
      S.append(" for the UB matrices");
      S.append("@assumptions    "); 
      S.append("There are peaks in the peak file and that there is enough information to get legitimate Q values for");
      S.append(" each peak");
      S.append("@param   ");
      S.append("A Vector of Peaks");
      S.append("@param   ");
      S.append("The maximum length of crystal lattice in real space or -1 for default and adjustable");
      S.append("@param   ");
      S.append("Minimum distance in x direction or y direction of the projection of a unit direction on he xy plane for 2 directions to be");
      S.append(" considered different");
      S.append("@error ");
      S.append("Not enough directions have been found to create a UB matrix");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "MyMenu"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.util.Vector Peaks = (java.util.Vector)(getParameter(0).getValue());
         float Xlength = ((FloatPG)(getParameter(1))).getfloatValue();
         float DNewDir = ((FloatPG)(getParameter(2))).getfloatValue();
         float[][] Xres=DataSetTools.operator.Generic.TOF_SCD.GetUB.GetUBMatrix(Peaks,Xlength,DNewDir );

         return Xres;
       }catch(java.lang.IllegalArgumentException S0){
         return new ErrorString(S0.getMessage());
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



