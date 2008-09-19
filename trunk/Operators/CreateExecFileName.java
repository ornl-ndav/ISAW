/* 
 * File: CreateExecFileName.java
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

package Operators;
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
public class CreateExecFileName extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public CreateExecFileName(){
     super("CreateExecFileName");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "CreateExecFileName";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataDirPG("Path",""));
      addParameter( new StringPG("Name of Executable file",""));
      addParameter( new BooleanPG("Add opSystem tail", false));
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
      S.append("Creates the filename for an executable that can be used be java's Runnable.exec. The");
      S.append(" name differes for each operating system as follows:");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("Os.name and os.arch java properties are as advertised for common systems in");
      S.append(" http://lopica.sourceforge.net/os.html");
      S.append("@param   ");
      S.append("The Path to the directory with the executable file");
      S.append("@param   ");
      S.append("The base name of the executable file");
      S.append("@param   ");
      S.append("Add opSystem tail to file name");
      S.append("@return String representing the path plus name of an executable file in the current operating system with the");
      S.append("");
      S.append(" current architecture or null.");
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

         java.lang.String Path = getParameter(0).getValue().toString();
         java.lang.String ExecFile = getParameter(1).getValue().toString();
         boolean AddOpTail = ((BooleanPG)getParameter(2)).getbooleanValue();
         java.lang.String Xres=gov.anl.ipns.Util.File.FileIO.CreateExecFileName(Path,ExecFile, AddOpTail );

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



