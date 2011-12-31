/* 
 * File: FindUB_UsingFFT.java
 *  
 * Copyright (C) 2011     Dennis Mikkelson
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
@see Operators.TOF_SCD.IndexingUtils#FindUB_UsingFFT(gov.anl.ipns.MathTools.Geometry.Tran3D,java.util.Vector,float,float,float,float)
 */
public class FindUB_UsingFFT extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public FindUB_UsingFFT(){
     super("FindUB_UsingFFT");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "FindUB_UsingFFT";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("UB Matrix",new gov.anl.ipns.MathTools.Geometry.Tran3D()));
      addParameter( new ArrayPG("Q Values",new java.util.Vector()));
      addParameter( new FloatPG("Min real space cell edge",3));
      addParameter( new FloatPG("Max real space cell edge",15));
      addParameter( new FloatPG("Tolerance",0.12));
      addParameter( new FloatPG("Degrees per step",1.5));
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
      S.append("This method will attempt to calculate the");
      S.append(" matrix that most nearly indexes the specified q_vectors, given only a range");
      S.append(" of possible unit cell edge lengths,");
      S.append(" ");
      S.append(" The resolution of the search through possible orientations is specified");
      S.append(" by the degrees_per_step parameter.  Approximately 1-3 degrees_per_step is");
      S.append(" usually adequate.  NOTE: This can be an expensive calculation and the execution");
      S.append(" time is O(n^3) so decreasing the resolution by a factor of 2 can require 8 times");
      S.append(" as much time to execute.  It should not be necessary to decrease this value");
      S.append(" below 1 degree per step, and users may have to be VERY patient, if it is");
      S.append(" decreased much below 1 degree per step.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.IndexingUtils#FindUB_UsingFFT(gov.anl.ipns.MathTools.Geometry.Tran3D,java.util.Vector,float,float,float,float)");
      S.append("@algorithm    "); 
      S.append("Projections of the q_vectors are made on a large collection of direction vectors");
      S.append(" over one hemisphere.  The direction vectors are separated by about the");
      S.append(" specified number of degrees per step.  The FFT of the projections is calculated");
      S.append(" and used to identify directions in which there are periodic patterns.  Directions with");
      S.append(" strong period patterns are examined and the shorest three directions which are");
      S.append(" linearly independent are used to form an initial UB matrix.  A Niggli reduced cell");
      S.append(" is then found, and the corresponding UB matrix is constructed.");
      S.append("@assumptions    "); 
      S.append("The list of q-vectors for peaks should have at least 4 entries as an");
      S.append(" absolute minimum for this to have a chance to work and preferably");
      S.append(" should have many more.  There must be several q-vectors in each");
      S.append(" lattice direction.");
      S.append("@param   ");
      S.append("3x3 matrix that will be set to the UB matrix");
      S.append("@param   ");
      S.append("Vector of new Vector3D objects that contains");
      S.append(" the list of q_vectors that are to be indexed");
      S.append(" NOTE: There must be at least 4 q_vectors and it");
      S.append(" really should have at least 10 or more");
      S.append(" peaks for this to work quite consistently.");
      S.append("@param   ");
      S.append("Lower bound on shortest unit cell edge length.");
      S.append(" This does not have to be specified exactly but");
      S.append(" must be strictly less than the smallest edge");
      S.append(" length, in Angstroms.");
      S.append("@param   ");
      S.append("Upper bound on longest unit cell edge length.");
      S.append(" This does not have to be specified exactly but");
      S.append(" must be strictly more than the longest edge");
      S.append(" length in angstroms.");
      S.append("@param   ");
      S.append("The maximum allowed deviation of Miller indices");
      S.append(" from integer values for a peak to be indexed.");
      S.append("@param   ");
      S.append("The number of degrees between directions that");
      S.append(" are checked while scanning for an initial");
      S.append(" indexing of the peaks with lowest |Q|.");
      S.append("@return This will return the sum of the squares of the residual errors.");
      S.append("@error ");
      S.append("An illegal argument exception is thrown if there");
      S.append(" are not enough q-vectors, or if the algorithm");
      S.append(" can't determine enough linearly independent");
      S.append(" directions.");
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
         java.util.Vector q_vectors = (java.util.Vector)(getParameter(1).getValue());
         float min_d = ((FloatPG)(getParameter(2))).getfloatValue();
         float max_d = ((FloatPG)(getParameter(3))).getfloatValue();
         float required_tolerance = ((FloatPG)(getParameter(4))).getfloatValue();
         float degrees_per_step = ((FloatPG)(getParameter(5))).getfloatValue();
         float Xres=Operators.TOF_SCD.IndexingUtils.FindUB_UsingFFT(UB,q_vectors,min_d,max_d,required_tolerance,degrees_per_step );

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



