/* 
 * File: MergeQE.java
 *  
 * Copyright (C) 2009     Dennis Mikkelson
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
 *            Department of Mathematics, Statistics and Computer Science
 *            University of Wisconsin-Stout
 *            Menomonie, WI 54751, USA
 *
 * This work was supported by the SNS division of Oakridge National Laboratory.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date$$
 * $Revision$
 */

package Operators.TOF_DG_Spectrometer;
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
public class MergeQE extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public MergeQE(){
     super("MergeQE");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "MergeQE";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("List of QE DataSets to Merge",null));
      addParameter( new StringPG("Text File to Write Result to",""));
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
      S.append("This method merges a list (Vector) of DataSets,");
      S.append(" each of which is an S(Q,E) (or S(Q^2,E)) DataSet");
      S.append(" and produces a new DataSet combining the");
      S.append(" information from all of the DataSets.   The");
      S.append(" resulting combined QE DataSet");
      S.append(" can be written to a text file,");
      S.append("@algorithm    "); 
      S.append("The ToQE and ToQ2E operators record");
      S.append(" an average S(Q,E) array normalized by the");
      S.append(" number of pixels that contribute to an");
      S.append(" S(Q,E) bin.  (The contribution from an");
      S.append(" individual pixel is first normalized by");
      S.append(" by the solid angle subtended by the pixel.)");
      S.append(" These operators also record the number");
      S.append(" of pixels that contributed to each S(Q,E) bin.");
      S.append(" This allow merging information from multiple");
      S.append(" S(Q,E) DataSets by first multiplying the");
      S.append(" averaged S(Q,E) values by the number of");
      S.append(" pixels used, summing across all DataSets,");
      S.append(" then dividing by the new total count.");
      S.append(" The errors are treated similarly.");
      S.append("@assumptions    "); 
      S.append("The DataSets must cover the same range of Q");
      S.append(" and E values with the same number of");
      S.append(" subdivisions in Q and E.  In most cases the");
      S.append(" DataSets will come from making a QE DataSet,");
      S.append(" using the same parmeters but using different");
      S.append(" detectors.");
      S.append(" The specified filename must not be null");
      S.append(" and the file must be writeable by the user.");
      S.append("@param   ");
      S.append("List (i.e.Vector) of QE or Q2E DataSets to merge");
      S.append("@param   ");
      S.append("The name of the file to write.  If null or blank,");
      S.append(" no attemp will be made to write the file.");
      S.append("@error ");
      S.append("Errors are thrown if the DataSets are null,");
      S.append(" empty, or not of equal sizes.");
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
                     "Instrument Type",
                     "TOF_NDGS"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.util.Vector dss = (java.util.Vector)(getParameter(0).getValue());
         java.lang.String filename = getParameter(1).getValue().toString();
         DataSetTools.dataset.DataSet Xres=Operators.TOF_DG_Spectrometer.TOF_NDGS_Calc.MergeQE(dss,filename );

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



