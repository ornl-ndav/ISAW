/* 
 * File: NormalizeByDetectorEfficiency.java
 *  
 * Copyright (C) 2006     Dennis Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2006/07/14 21:16:50  dennis
 * Example of an operator accessing values and attributes in
 * DataSets, generated by applying the Method2OperatorWizard
 * to a static method in ExampleOperatorMethods.java
 *
 *
 */

package Operators.Example;
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
public class NormalizeByDetectorEfficiency extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public NormalizeByDetectorEfficiency(){
     super("NormalizeByDetectorEfficiency");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "NormalizeByDetectorEfficiency";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("SAND DataSet",null));
      addParameter( new LoadFilePG("Efficiency File","/home/dennis/WORK/ISAW/SampleRuns/EFR22227.dat"));
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
      S.append("Normalize the counts in a spectrum by multiplying each count by a");
      S.append(" wavelength dependent efficiency correction.  The error estimates are");
      S.append(" also adjusted.");
      S.append("@algorithm    "); 
      S.append("The mid point of each time-of-flight histogram bin is");
      S.append(" mapped to wavelength.  The detector efficiency at the");
      S.append(" nearest wavelength, less than or equal to that wavelength");
      S.append(" is used to normalize the spectrum by dividing the measured");
      S.append(" counts in that bin by the efficiency ratio.  The error estimate");
      S.append(" for the counts is also adjusted, using the original error estimate");
      S.append(" and the error estimate for the efficiency ratio.");
      S.append("@assumptions    "); 
      S.append("A time-of-flight DataSet, with histograms");
      S.append(" and detector positionattributes is required.");
      S.append(" The Data blocks must have properly set");
      S.append(" PixelInfoList attributes.");
      S.append(" Also, the detector efficiency file is assumed to be three");
      S.append(" column ASCII, with each line containing the wavelength,");
      S.append(" efficiency ratio and error estimate for one wavelength, in");
      S.append(" order of increasing wavelength.  The first line of the file");
      S.append(" must just contain the number of wavelengths recorded");
      S.append(" in the file.");
      S.append("@param   ");
      S.append("The DataSet to be normalized.");
      S.append("@param   ");
      S.append("The file name for the ASCII file listing the");
      S.append(" detector efficiency as a function of wavelength.");
      S.append("@return A new DataSet with counts normalized by the wavelength dependent");
      S.append("");
      S.append(" detector efficiency");
      S.append("@error ");
      S.append("Exceptions will be thrown If the DataSet does not contain");
      S.append(" histograms, or is missing attributes, or if the efficiency");
      S.append(" file can't be opened and read.");
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
                     "Examples"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         DataSetTools.dataset.DataSet ds = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         java.lang.String eff_file = getParameter(1).getValue().toString();
         DataSetTools.dataset.DataSet Xres=Operators.Example.ExampleOperatorMethods.NormalizeByDetectorEfficiency(ds,eff_file );

         return Xres;
       }catch(java.io.IOException S0){
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
