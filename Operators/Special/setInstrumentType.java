/* 
 * File: setInstrumentType.java
 *  
 * Copyright (C) 2007     Dennis Mikkelson
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
 *            MSCS Deparatment
 *            HH237H
 *            Menomonie, WI. 54751
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2007/07/05 14:35:05  dennis
 * Switched to use static method from SetInstrumentTypeCalc,
 * instead of from SetInstrumentType.
 *
 * Revision 1.2  2007/05/14 01:58:50  dennis
 * Converted from DOS to UNIX text format.
 *
 * Revision 1.1  2007/04/26 21:50:16  dennis
 * Operator to change the INST_TYPE attribute and configure the
 * DataSet's operators for a new instrument type.  This should be
 * useful to re-configure the DataSet with the QENS diffraction
 * data as a TOF_NPD instrument, instead of a TOF_NDGS instrument.
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
public class setInstrumentType extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public setInstrumentType(){
     super("setInstrumentType");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "setInstrumentType";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to Modify",
                                  DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new StringPG("New Type (eg. TOF_NPD.)", "TOF_NPD"));
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
      S.append("This method will attempt to configure the specified DataSet with the");
      S.append(" operators required for the specified instrument type.  Support for");
      S.append(" some of the listed instrument types is NOT complete.  In addition,");
      S.append(" other methods may need to be used to set appropriate attributes.  For");
      S.append(" example, if configuring a DataSet as a direct geometry spectrometer,");
      S.append(" it will be necessary to set the incident energy as an attribute.");
      S.append("@algorithm    "); 
      S.append("All operators are first removed from the DataSet then Operators");
      S.append(" appropriate for all DataSets and for the newly specified instrument");
      S.append(" type are added to the DataSet.  Finally, the INST_TYPE attribute is");
      S.append(" set to the integer type code corresponding to the new instrument");
      S.append(" type name.  The DataSet is modfied \"in place\" without making a");
      S.append(" copy.");
      S.append("@assumptions    "); 
      S.append("The DataSet is a raw time-of-flight DataSet.");
      S.append("@param   ");
      S.append("The DataSet to modify");
      S.append("@param   ");
      S.append("The new instrument type.  This must be one of the following Strings:");
      S.append(" TOF_NPD, TOF_NGLAD, TOF_NSCD, TOF_NSAS, TOF_NDGS, TOF_NIGS, TOF_NREFL");
      S.append("@error ");
      S.append("An IllegalArgumentException is thrown if the specified instrument");
      S.append(" name is not one of the allowed instrument names.  An exception");
      S.append(" is also thrown if the DataSet or type name String is null.");
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
         DataSetTools.dataset.DataSet ds = 
                   (DataSetTools.dataset.DataSet)(getParameter(0).getValue());

         java.lang.String type = getParameter(1).getValue().toString();

         java.lang.Object Xres = 
           Operators.Special.SetInstrumentTypeCalc.setInstrumentType(ds,type );

         return Xres;
       }
       catch( Throwable XXX){

        String[]Except = ScriptUtil.GetExceptionStackInfo(XXX,true,1);

        String mess="";

        if(Except == null) 
          Except = new String[0];

        for( int i =0; i< Except.length; i++)
           mess += Except[i]+"\r\n            "; 

        return new ErrorString( XXX.toString()+":"
             +mess);
       }
   }
}
