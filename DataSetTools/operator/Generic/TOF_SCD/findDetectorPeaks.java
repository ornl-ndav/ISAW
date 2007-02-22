/* 
 * File: findDetectorPeaks.java
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
 * Revision 1.1  2007/02/22 15:13:17  rmikk
 * Operator wrapped around DataSetTools.operator.Generic.TOF_SCD.
 *    FindPeaks.findDetectorPeaks
 *
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;
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
public class findDetectorPeaks extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public findDetectorPeaks(){
     super("findDetectorPeaks");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "findDetectorPeaks";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("Data Set",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new IntegerPG("Detector ID",1));
      addParameter( new IntegerPG("Min Time Channel",1));
      addParameter( new IntegerPG("Max Time Channel",1000));
      addParameter( new IntegerPG("Max num Peaks",30));
      addParameter( new IntegerPG("min Peak intensity",0));
      addParameter( new StringPG("Pixel rows to keep","1:100"));
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
      S.append("This finds a bunch of peaks in a data set  for a given detector number.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The data set for which peaks will be found");
      S.append("@param   ");
      S.append("The ID of the detector on this data set where the peaks are to  be found");
      S.append("@param   ");
      S.append("The minimum time channel to use");
      S.append("@param   ");
      S.append("The maximum time channel to use");
      S.append("@param   ");
      S.append("The maximum number of peaks to find on this detector");
      S.append("@param   ");
      S.append("The minimum count for a cell to represent a peak");
      S.append("@param   ");
      S.append("The list of rows and columns to consider");
      S.append("@return   ");
      S.append("Vector of Old peaks objects");
      S.append("@error ");
      S.append("Data Set is null");
      S.append(" No such detector id for this DataSet");
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

         DataSetTools.dataset.DataSet DS = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int ID = ((IntegerPG)(getParameter(1))).getintValue();
         int MinTimeChan = ((IntegerPG)(getParameter(2))).getintValue();
         int MaxTimeChan = ((IntegerPG)(getParameter(3))).getintValue();
         int MaxNPeaks = ((IntegerPG)(getParameter(4))).getintValue();
         int mincount = ((IntegerPG)(getParameter(5))).getintValue();
         String Pixel_row = getParameter(6).getValue().toString();
         java.util.Vector Xres=DataSetTools.operator.Generic.TOF_SCD.FindPeaks.findDetectorPeaks(DS,ID,MinTimeChan,MaxTimeChan,MaxNPeaks,mincount,Pixel_row );

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



