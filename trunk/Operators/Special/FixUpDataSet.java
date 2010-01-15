/* 
 * File: FixUpDataSet.java
 *  
 * Copyright (C) 2010     Ruth Mikkelson
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
 * $Date: 2009-06-01 10:26:25 -0500 (Mon, 01 Jun 2009) $$
 * $Revision: 19721 $
 */

package Operators.Special;
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
public class FixUpDataSet extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public FixUpDataSet(){
     super("FixUpDataSet");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "FixUpDataSet";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to Change",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new IntegerPG("DataSet Number",1));
      addParameter( new LoadFilePG("NeXus FileName",""));
      addParameter( new LoadFilePG("CacheFileName",""));
      addParameter( new IntegerPG("Mode",1));
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
      S.append("Fixes information in a DataSet where the Fixup file is a NeXus file");
      S.append("@algorithm    "); 
      S.append("The retriever sends the DataSet through given parts of the NeXus retrieval code");
      S.append("@assumptions    "); 
      S.append("The DataSet must correspond to the indicated dataset number in the NeXus file. The number of");
      S.append(" rows, cols, etc. must match. Nexus file must be version 2 or better.");
      S.append("@param   ");
      S.append("The DataSe that is to be fixed");
      S.append("@param   ");
      S.append("The data set number in the NeXus file corresponding to this data set.");
      S.append("@param   ");
      S.append("The name of the NeXus file with the fixed up information");
      S.append("@param   ");
      S.append("The name of a file with Cached information on the NeXus file");
      S.append("@param   ");
      S.append("The mode(summed) describing what information in the Nexus file that is to be used.");
      S.append(" 1-NXdetector info,");
      S.append(" 2-NXsample");
      S.append(" , 4-NXbean,");
      S.append(" 8-Global Attributes,");
      S.append(" 16-NXinstrument,");
      S.append(" 32- NXentry");
      S.append( "@return  An object true if there is an error, false if there is");
      S.append( " no error, or an ErrorString");
      S.append("@error ");
      S.append("Numerous");
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
                     "DataSet",
                     "Tweak"
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
         int dsNum = ((IntegerPG)(getParameter(1))).getintValue();
         java.lang.String NxFilename = getParameter(2).getValue().toString();
         java.lang.String CacheFileName = getParameter(3).getValue().toString();
         int Mode = ((IntegerPG)(getParameter(4))).getintValue();
         boolean Xres=DataSetTools.retriever.NexusRetriever.FixUpDataSet(DS,dsNum,NxFilename,CacheFileName,Mode );

         return new Boolean(Xres);
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



