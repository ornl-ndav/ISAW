/* 
 * File: InterActiveExec.java
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
 * Modified:
 *
 * $Log:$
 *
 */

package DataSetTools.operator.Generic.Special;
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
public class InterActiveExec extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public InterActiveExec(){
     super("InterActiveExec");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "InterActiveExec";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      String DirName = System.getProperty( "ISAW_HOME" ,"");
      DirName = DirName.replace( '\\' , '/' );
      if( !DirName.endsWith( "/"))
               DirName +='/';
      DirName +="bin/";
      DirName = DirName.replace( '/' , java.io.File.separatorChar );
      
      System.out.println("DirName= "+ DirName);
      addParameter( new LoadFilePG("Executable Filename",DirName));
      addParameter( new StringPG("Short Name","short name"));
      addParameter( new ArrayPG("Command line args",new java.util.Vector()));
      addParameter( new DataDirPG("Working Dir",null));
      addParameter( new LoadFilePG("Help File Name",null));
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
      S.append("Creates a window to execute non-graphical applications. It displays output from the executable and sends input to the executable.");
      S.append("@algorithm    "); 
      S.append("Creates a process from the executable, causes text output from executable to appear in a text area.");
      S.append(" ");
      S.append(" Allows for input from user which is fed back to the executab le");
      S.append("@param   ");
      S.append("The whole filename of the executable");
      S.append("@param   ");
      S.append("Short name for the executable");
      S.append("@param   ");
      S.append("Vector of arguments for executable(Not DONE yet)");
      S.append("@param   ");
      S.append("Working directory for the executable");
      S.append("@param   ");
      S.append("Help filename without the IsawHelp path prefix");
      S.append("@error ");
      S.append("Errors from executable will be reported.");
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

         java.lang.String ExFile = getParameter(0).getValue().toString();
         java.lang.String ShortName = getParameter(1).getValue().toString();
         java.util.Vector args = (java.util.Vector)(getParameter(2).getValue());
         java.lang.String WorkDir = getParameter(3).getValue().toString();
         java.lang.String HelpFile = getParameter(4).getValue().toString();
         java.lang.Object Xres=gov.anl.ipns.Util.Sys.RunExecInteractive.InterActiveExec(ExFile,ShortName,args,WorkDir,HelpFile );

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



