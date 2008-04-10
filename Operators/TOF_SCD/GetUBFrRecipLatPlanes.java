/* 
 * File: GetUBFrRecipLatPlanes.java
 *  
 * Copyright (C) 2008     Ruth Mikkelson
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
 * This work was supported by the Spallation Neutron Source, Oak  Ridge National
 * Laboratory
 *
 *
 * Modified:
 *
 * $Log:$
 *
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
 */
public class GetUBFrRecipLatPlanes extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public GetUBFrRecipLatPlanes(){
     super("GetUBFrRecipLatPlanes");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "GetUBFrRecipLatPlanes";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Peaks",new java.util.Vector()));
      addParameter( new FloatPG("Max Crystal cell dimension",15));
      addParameter( new PlaceHolderPG("Status",new java.util.Vector()));
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
      S.append("create operator to display the peaks and allow a user  to select three planes which will determine");
      S.append(" a UB matrix.");
      S.append(" The UB matrix is optimized and passed through Blind to put the  basis Q vectors in a standard");
      S.append(" crystllographic form");
      S.append(" ");
      S.append("@algorithm    "); 
      S.append("Display Peaks in a reciprocal lattice viewer and let the user select three non parallel planes.");
      S.append(" Get the planes, calculate their normals, project peaks onto normals to find the best distance");
      S.append(" between planes.  Use this info to index peaks, then optimize an orientation matrix to fit these.");
      S.append(" Last, send the resultant basis vectors through blind to standardize the format.");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The Vector of Peaks with the information");
      S.append("@param   ");
      S.append("The maximum lenth for this crystal's unit cell in real space");
      S.append("@param   ");
      S.append("Output for goodness of fit. The first element is the fraction of peaks that are indexed to within .1,");
      S.append(" The second element is the fraction indexed within .2, ... up to the fraction that are indexed within");
      S.append(" .4");
      S.append("@error ");
      S.append("-Not enough planes selected or some planes are parallel");
      S.append(" -Some of the directions selected are not good enough to get a good spacing");
      S.append(" -The Vector does not have any peaks");
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

         java.util.Vector Peaks = (java.util.Vector)(getParameter(0).getValue());
         float MaxXtallength = ((FloatPG)(getParameter(1))).getfloatValue();
         java.util.Vector Status = (java.util.Vector)(getParameter(2).getValue());
         float[][] Xres=DataSetTools.trial.SCDRecipLat.GetUBFrRecipLattice(Peaks,MaxXtallength,Status );

         return Xres;
       }catch( Throwable XXX){
        String[]Except = ScriptUtil.
            GetExceptionStackInfo(XXX,true,1);
        String mess="";
        if(Except == null) Except = new String[0];
        for( int i =0; i< Except.length; i++)
           mess += Except[i]+"\r\n   "; 
        return new ErrorString( XXX.toString()+":"
             +mess);
                }
   }
}



