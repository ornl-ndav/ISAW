/* 
 * File: RotateDetectors.java
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

package Operators.TOF_SCD;
import java.util.Vector;

import DataSetTools.components.ParametersGUI.JParametersDialog;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 *  
 *  This operator is a wrapper around 
 *   @see Operators.TOF_SCD.General_Utils#RotateDetectors( java.lang.String, int ,java.lang.String ,float , float ,float ,java.util.Vector)
 */
public class RotateDetectors extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public RotateDetectors(){
     super("RotateDetectors");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "RotateDetectors";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Original Detector Calibration File",System.getProperty("Data_Directory","")));
      addParameter( new IntegerPG("ID Center Bank",14));
      addParameter( new SaveFilePG("New Detector Calibration File",System.getProperty("Data_Directory","")));
      addParameter( new FloatPG("New Angle of Center(degr)",0));
      addParameter( new FloatPG("Sample x-offset(m)",0));
      addParameter( new FloatPG("Sample beam offset(m)",0));
      addParameter( new ArrayPG("Detector IDs to use(blank for all)",null));
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
      S.append("Creates a new DetCal file if the detectors are rotated to the specified angle in the scattering plane.");
      S.append("\r\n");
      S.append(" This operator wraps the method Operators.TOF_SCD.General_Utils#RotateDetectors\n");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The rotation is about an axis perpendicular to the scattering plane");
      S.append("@param   ");
      S.append("The name of the original DetCal or new Peaks file");
      S.append("@param   ");
      S.append("The ID of the bank considered as the center");
      S.append("@param   ");
      S.append("The name of the new DetCal file where the");
      S.append(" *                          rotated detector information is stored.");
      S.append(" The name of the new DetCal file where the   rotated detector information is stored.");
      S.append("@param   ");
      S.append("The angle in degrees where the new center will  rotate to along a vertical axis.");
      S.append( "@param " );
      S.append( " the sample offset in the x direction(back is positive)");
      S.append(" @param Sample offset in beam direction");
      S.append(" @param IDs The IDs to apply the rotation to. If blank or [], all detectors "+
               "will be rotated");
      
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
                     "Instrument Type",
                     "TOF_NSCD"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String CalibOr = getParameter(0).getValue().toString();
         int CentID = ((IntegerPG)(getParameter(1))).getintValue();
         java.lang.String CalibNew = getParameter(2).getValue().toString();
         float CentAngle = ((FloatPG)(getParameter(3))).getfloatValue();
         float xOffset = ((FloatPG)(getParameter(4))).getfloatValue( );
         float SampOffset = ((FloatPG)(getParameter(5))).getfloatValue( );
         Vector IDs2Use = (Vector)((ArrayPG)getParameter(6)).getValue( );
         if( IDs2Use != null && IDs2Use.size() < 1)
            IDs2Use = null;
         java.lang.Object Xres=Operators.TOF_SCD.General_Utils.
         RotateDetectors(CalibOr,CentID,CalibNew,CentAngle,xOffset,SampOffset, IDs2Use );
         
         
         
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
   
   public static void main( String[] args)
   {
      RotateDetectors op = new RotateDetectors();
      JParametersDialog x =(new JParametersDialog( op, null,null,null,false));
      System.out.println("Result = "+x.getLastResult( ));
   }
}



