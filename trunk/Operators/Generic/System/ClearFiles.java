/* 
 * File: ClearFiles.java
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

package Operators.Generic.System;
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
public class ClearFiles extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public ClearFiles(){
     super("ClearFiles");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "ClearFiles";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Enter file prefix (blank for *)",""));
      addParameter( new StringPG("Enter file suffix (blank for *)","*.isd"));
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
      S.append("This operator is used to remove files from the ISAW");
      S.append(" scratch directory ~/ISAW/tmp.  This allow scripts to");
      S.append(" first remove any old temporary files of the type they");
      S.append(" create, to make sure that their computation does");
      S.append(" not accidentally bring in old files created by previous");
      S.append(" runs of the script.");
      S.append("@algorithm    "); 
      S.append("The list of files in the ISAW scratch directory is");
      S.append(" checked for names that begin with the specified");
      S.append(" prefix and ends with the specified suffix.  Any");
      S.append(" file that meets this criterion will be deleted.  In");
      S.append(" particular, if both the prefix and suffix are empty");
      S.append(" Strings then all files are deleted.  To delete all");
      S.append(" ISAW DataSet files, set the prefix to an empty");
      S.append(" String and set the suffix to \".isd\".");
      S.append("@assumptions    "); 
      S.append("The directory ISAW/tmp should exist in the user's");
      S.append(" home directory.  If not this operator will have no");
      S.append(" effect.");
      S.append("@param   ");
      S.append("The prefix of the names of the files to delete.");
      S.append("@param   ");
      S.append("The suffix of the names of the files to delete.");
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
                     "Macros",
                     "Utils",
                     "System"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String prefix = getParameter(0).getValue().toString();
         java.lang.String suffix = getParameter(1).getValue().toString();
         Wizard.TOF_SCD.Util.ClearFiles(prefix,suffix );
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



