/* 
 * File: Find_UB1.java
 *  
 * Copyright (C) 2011     Ruth Mikkelson
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
 * $Date: 2010-10-25 09:37:07 -0500 (Mon, 25 Oct 2010) $$
 * $Revision: 21066 $
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
 * This operator is a wrapper around 
@see Operators.TOF_SCD.IndexingUtils#Find_UB(gov.anl.ipns.MathTools.Geometry.Tran3D,java.util.Vector,float,float,float,int,int,float)
 */
public class Find_UB1 extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public Find_UB1(){
     super("Find_UB");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "Find_UB";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("UB Matrix",new gov.anl.ipns.MathTools.Geometry.Tran3D()));
      addParameter( new ArrayPG("Q Values",new java.util.Vector()));
      addParameter( new FloatPG("Min Dspacing",1));
      addParameter( new FloatPG("Max Dspacing",20));
      addParameter( new FloatPG("tolerance",.12));
      addParameter( new IntegerPG("base index",-1));
      addParameter( new IntegerPG("num initial",15));
      addParameter( new FloatPG("degrees per step",3));
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
      S.append("This method will attempt to calculate the matrix    that most nearly indexes the specified q_vectors,");
      S.append(" given a range of   possible unit cell edge lengths.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.IndexingUtils#Find_UB(gov.anl.ipns.MathTools.Geometry.Tran3D,java.util.Vector,float,float,float,int,int,float)");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("3x3 matrix that will be set to the UB matrix");
      S.append("@param   ");
      S.append("Vector of new Vector3D objects that contains  the list of q_vectors that are to be indexed");
      S.append(" NOTE: There must be at least 3 linearly   independent q_vectors.  If there are only 3");
      S.append(" q_vectors, no least squares optimization of  the UB matrix will be done.");
      S.append("@param   ");
      S.append("Lower bound on shortest unit cell edge length.  This does not have to be specified exactly but");
      S.append(" must be strictly less than the smallest edge  length, in Angstroms.");
      S.append("@param   ");
      S.append("Upper bound on longest unit cell edge length.  This does not have to be specified exactly but");
      S.append(" must be strictly more than the longest edge  length in angstroms.");
      S.append("@param   ");
      S.append("The maximum allowed deviation of Miller indices   from integer values for a peak to be indexed.");
      S.append("@param   ");
      S.append("The sequence number of the peak that should    be used as the central peak.  On the first");
      S.append(" scan for directions representing plane normals   in reciprocal space, the peaks in the list of");
      S.append(" q_vectors will be shifted by -base_peak, where   base_peak is the q_vector with the specified base index.");
      S.append(" If fewer than 4 peaks are specified in the");
      S.append(" q_vectors list, this parameter is ignored.");
      S.append(" If this parameter is -1, and there are at least");
      S.append(" four peaks in the q_vector list, then a base");
      S.append(" index will be calculated internally.  In most");
      S.append(" cases, it should suffice to set this to -1.");
      S.append("@param   ");
      S.append("The number of low |Q| peaks that are used  when scanning for an initial indexing.");
      S.append("@param   ");
      S.append("The number of degrees between directions that  are checked while scanning for an initial");
      S.append(" indexing of the peaks with lowest |Q|.");
      S.append("@return the sum of the squares of the residual errors.");
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

         gov.anl.ipns.MathTools.Geometry.Tran3D UB = (gov.anl.ipns.MathTools.Geometry.Tran3D)(getParameter(0).getValue());
         java.util.Vector QVec = (java.util.Vector)(getParameter(1).getValue());
         float min_dSpacing = ((FloatPG)(getParameter(2))).getfloatValue();
         float max_dspacing = ((FloatPG)(getParameter(3))).getfloatValue();
         float tolerance = ((FloatPG)(getParameter(4))).getfloatValue();
         int base_index = ((IntegerPG)(getParameter(5))).getintValue();
         int Ninit = ((IntegerPG)(getParameter(6))).getintValue();
         float degrees_per_step = ((FloatPG)(getParameter(7))).getfloatValue();
         float Xres=Operators.TOF_SCD.IndexingUtils.Find_UB(UB,QVec,min_dSpacing,max_dspacing,tolerance,base_index,Ninit,degrees_per_step );

         return new Float(Xres);
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



