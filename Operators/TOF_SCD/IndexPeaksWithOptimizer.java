/* 
 * File: IndexPeaksWithOptimizer.java
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
 * This work was supported by the SNS division of ORNL.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
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
 */
public class IndexPeaksWithOptimizer extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public IndexPeaksWithOptimizer(){
     super("IndexPeaksWithOptimizer");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "IndexPeaksWithOptimizer";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Peaks file to index",""));
      addParameter( new SaveFilePG("Matrix file to write",""));
      addParameter( new SaveFilePG("File for NOT indexed peaks",""));
      addParameter( new FloatPG("Lattice parameter 'a'",4.9138));
      addParameter( new FloatPG("Lattice parameter 'b'",4.9138));
      addParameter( new FloatPG("Lattice parameter 'c'",5.4051));
      addParameter( new FloatPG("alpha",90));
      addParameter( new FloatPG("beta",90));
      addParameter( new FloatPG("gamma",120));
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
      S.append("Attempt to index the peaks in the");
      S.append(" specified file, given the lattice constants.");
      S.append(" This method proceeds in three stages.");
      S.append(" First the strongest peaks in the file are");
      S.append(" used with an optimization based method");
      S.append(" to find an initial indexing for a significant");
      S.append(" fraction of the strongest peaks, using");
      S.append(" the specified lattice constants.");
      S.append(" Next, the algorithm attempts to index");
      S.append(" all of the strongest peaks by adjusting");
      S.append(" the lattice parameters.");
      S.append(" Finally, the algorithm attempts to extend");
      S.append(" this indexing to all of the peaks.");
      S.append(" The peaks that are not indexed are written");
      S.append(" to a specified file, so that they can (possibly)");
      S.append(" be indexed latter.  This allows indexing twins");
      S.append(" or more complicated samples with several");
      S.append(" crystalites.");
      S.append("@algorithm    "); 
      S.append("The peak with the largest intensity is used");
      S.append(" together with a randomly chosen second peak");
      S.append(" to obtain an initial indexing.  Since the lattice");
      S.append(" parameters are known, factor B in UB is known.");
      S.append(" The factor U is found by adjusting U to");
      S.append(" minimize the distance  between the hkl values");
      S.append(" and integer values.");
      S.append(" Once an initial indexing is obtained, the algorithm");
      S.append(" gradually extends the indexing to larger groups");
      S.append(" of peaks using a linear least squares algorithm.");
      S.append("@assumptions    "); 
      S.append("The lattice constants should correspond to");
      S.append(" crystal which has the largest intensity");
      S.append(" reflection.");
      S.append("@param   ");
      S.append("The name of the file containing the");
      S.append(" original list of peaks.");
      S.append("@param   ");
      S.append("The name of the matrix file to write.");
      S.append("@param   ");
      S.append("The name of the file to write with peaks");
      S.append(" that could not be indexed.");
      S.append("@param   ");
      S.append("Lattice parameter 'a'");
      S.append("@param   ");
      S.append("Lattice parameter 'b'");
      S.append("@param   ");
      S.append("Lattice parameter 'c'");
      S.append("@param   ");
      S.append("Lattice parameter alpha");
      S.append("@param   ");
      S.append("Lattice parameter beta");
      S.append("@param   ");
      S.append("Lattice parameter gamma");
      S.append("@return A message indicating the result of the ");
      S.append("");
      S.append(" indexing.");
      S.append("@error ");
      S.append("An exception will be thrown if");
      S.append(" the peaks file does not exist or if the");
      S.append(" matrix or file of not-indexed peaks");
      S.append(" can't be written.");
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

         java.lang.String peaks_file_name = 
                               getParameter(0).getValue().toString();
         java.lang.String matrix_file_name = 
                               getParameter(1).getValue().toString();
         java.lang.String not_indexed_file_name = 
                               getParameter(2).getValue().toString();
         float a = ((FloatPG)(getParameter(3))).getfloatValue();
         float b = ((FloatPG)(getParameter(4))).getfloatValue();
         float c = ((FloatPG)(getParameter(5))).getfloatValue();
         float alpha = ((FloatPG)(getParameter(6))).getfloatValue();
         float beta = ((FloatPG)(getParameter(7))).getfloatValue();
         float gamma = ((FloatPG)(getParameter(8))).getfloatValue();
         java.lang.String Xres=Operators.TOF_SCD.IndexPeaks_Calc.
           IndexPeaksWithOptimizer(peaks_file_name,
                                   matrix_file_name,
                                   not_indexed_file_name,
                                   a, b, c,
                                   alpha, beta, gamma );

         return Xres;
       }catch(java.io.IOException S0){
         return new ErrorString(S0.getMessage());
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



