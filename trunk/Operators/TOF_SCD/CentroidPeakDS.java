/* 
 * File: CentroidPeakDS.java
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
 * Revision 1.1  2007/02/22 16:37:04  rmikk
 * Operator wrapped around Operators.TOF_SCD.GetCentroidPeaks1.
 *   CentroidPeaksDS
 *
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
public class CentroidPeakDS extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public CentroidPeakDS(){
     super("CentroidPeakDS");
     }

 
   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "CentroidPeakDS";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new IntegerPG("Grid ID",1));
      addParameter( new IntegerPG("row for peak",1));
      addParameter( new IntegerPG("column for peak",1));
      addParameter( new IntegerPG("Time Channel",0));
      addParameter( new PlaceHolderPG("Centroid values",new float[3]));
      addParameter( new IntegerPG("peak Span",-1));
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
      S.append("Finds the Centroid of the peak at row, col, and timeChan for the given Data Set Grid.");
      S.append(" Various heuristics are used find the best cutoff points for these peaks");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The Data Set with the peak to");
      S.append("@param   ");
      S.append("The id of the gird in the data set with the peak");
      S.append("@param   ");
      S.append("The row with the peak");
      S.append("@param   ");
      S.append("The column with the peak");
      S.append("@param   ");
      S.append("The time channel with the peak");
      S.append("@param   ");
      S.append("A float[3] that stores the row, col, and time channel of the centroided peak. THE RESULT");
      S.append("@param   ");
      S.append("The maximum span of the peak in rows, cols, and  time channels or -1 if none");
      S.append("@return   ");
      S.append("null or error message.  Ther result is stored in parameter 6");
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

         DataSetTools.dataset.DataSet DS = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int ID = ((IntegerPG)(getParameter(1))).getintValue();
         int row = ((IntegerPG)(getParameter(2))).getintValue();
         int col = ((IntegerPG)(getParameter(3))).getintValue();
         int chan = ((IntegerPG)(getParameter(4))).getintValue();
         float[] Res = (float[])(getParameter(5).getValue());
         int PeakSpan = ((IntegerPG)(getParameter(6))).getintValue();
         Operators.TOF_SCD.GetCentroidPeaks1.CentroidPeakDS(DS,ID,row,col,chan,Res,PeakSpan );
;
         return "Success";
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



