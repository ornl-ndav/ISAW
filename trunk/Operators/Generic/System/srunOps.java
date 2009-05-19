/* 
 * File: srunOps.java
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
 * This work was supported by the SNS division of Oak Ridge
 * National Laboratory.
 *
 *
 * Modified:
 *
 * $Log:$
 *
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
public class srunOps extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public srunOps(){
     super("srunOps");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "srunOps";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("SLURM Queue Name", "sequoiaq"));
      addParameter( new IntegerPG("Max Simultaneous Processes",16));
      addParameter( new IntegerPG("Max Run Time (seconds)",600));
      addParameter( new IntegerPG("Memory Size per Process(MB)",4000));
      addParameter( new StringArrayPG("List of Commands with Parameters",null));
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
      S.append("This operator will execute a specified list of operators");
      S.append(" (or scripts) on the specified SLURM queue using the");
      S.append(" specified maximum number of processes, time and");
      S.append(" memory.  The operators to execute must only take");
      S.append(" simple parameters such as integer, float, boolean and");
      S.append(" String (NO DataSets!).  Any output of the individual");
      S.append(" operators and scripts will be written to a temporary");
      S.append(" file in <user_home>/ISAW/tmp/*_returned.txt.  These");
      S.append(" temporary files are intended only for debugging purposes");
      S.append(" and will be erased each time the srunOps() operator is");
      S.append(" run.");
      S.append("@algorithm    "); 
      S.append("SLURM is used to execute each of the operator/parameter");
      S.append(" command strings in parallel, using up to the specified");
      S.append(" number of processes simultaneously.");
      S.append("@assumptions    "); 
      S.append("All of the parameters of the operators must be specified");
      S.append(" in the correct order and be of the correct data types.");
      S.append(" The individual operators must not require an X-Window");
      S.append(" display, but must communicate results via files.  The");
      S.append(" file names are controlled by the user and must be");
      S.append(" coordinated between the low-level operators & scripts");
      S.append(" and the script using this srunOps( ) operator.");
      S.append("@param   ");
      S.append("The SLURM queue on which to run the processes.  You");
      S.append(" must have the proper account settings to use the");
      S.append(" specified queue.");
      S.append("@param   ");
      S.append("The maximum number of processes to launch");
      S.append(" simultaneously.  This should be less than or");
      S.append(" equal to the number of cores available in the");
      S.append(" SLURM queue.");
      S.append("@param   ");
      S.append("The maximum allowed total run time as an");
      S.append(" integer number of seconds");
      S.append("@param   ");
      S.append("The amount of memory to allocate for each process");
      S.append(" specified by an integer, giving the number of");
      S.append(" megabytes.");
      S.append("@param   ");
      S.append("A list of Strings, each of which specifies an ISAW operator");
      S.append(" or script command name followed by a list of parameters");
      S.append(" required by the operator or script.");
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

         java.lang.String queue_name = getParameter(0).getValue().toString();
         int max_processes = ((IntegerPG)(getParameter(1))).getintValue();
         int max_time = ((IntegerPG)(getParameter(2))).getintValue();
         int mem_size = ((IntegerPG)(getParameter(3))).getintValue();
         java.util.Vector op_commands = 
                              (java.util.Vector)(getParameter(4).getValue());
         boolean Xres=gov.anl.ipns.Operator.Processes.ProcessMethod.srunOps
                     (queue_name,max_processes,max_time,mem_size,op_commands );

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



