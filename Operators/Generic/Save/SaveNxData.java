/* 
 * File: SaveNxData.java
 *  
 * Copyright (C) 2009     Ruth Mikkelson
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
 *            Menomonie, WI 54751
 *
 * This work was supported by the Spallation Neutron Source, Oak  Ridge National
 * Laboratory
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date: 2009-06-01 10:26:25 -0500 (Mon, 01 Jun 2009) $$
 * $Revision: 19721 $
 */

package Operators.Generic.Save;
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
public class SaveNxData extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public SaveNxData(){
     super("SaveNxData");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "SaveNxData";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("NeXus filename",null));
      addParameter( new StringPG("NXentry name","entry"));
      addParameter( new ArrayPG("DataSets",new java.util.Vector()));
      addParameter( new BooleanPG("Append",false));
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
      S.append("Writes DataSets to part of a NeXus file under the  entry with the  given name.");
      S.append(" Previous data in existing NXdata will be retained if append is false and none of");
      S.append(" the new datasets have the same title as an existing NXdata.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The Vector is a Vector of non null data sets.");
      S.append("@param   ");
      S.append("File to create or change with the information in the given data sets");
      S.append("@param   ");
      S.append("The name of an existing or new NXentry in the NeXus file");
      S.append("@param   ");
      S.append("The list of DataSets to be saved in the given NXentry");
      S.append("@param   ");
      S.append("If true, will append to existing file, otherwise the file will be deleted");
      S.append("@error ");
      S.append("File creation errors and NeXus errors.");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "HiddenOperator"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String fileName = getParameter(0).getValue().toString();
         java.lang.String EnttryName = getParameter(1).getValue().toString();
         java.util.Vector Data_Sets = (java.util.Vector)(getParameter(2).getValue());
         boolean append = ((BooleanPG)(getParameter(3))).getbooleanValue();
         java.lang.Object Xres=NexIO.Write.NxWriter.SaveNxData(fileName,EnttryName,Data_Sets,append );

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



