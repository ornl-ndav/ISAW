/* 
 * File: ConvertToConventionalCell.java
 *  
 * Copyright (C) 2012     Dennis Mikkelson
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
@see Operators.TOF_SCD.PeaksFileUtils#ConvertToConventionalCell(java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String)
 */
public class ConvertToConventionalCell extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public ConvertToConventionalCell(){
     super("ConvertToConventionalCell");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "ConvertToConventionalCell";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Peaks File to Change to Conventional Cell",""));
      ChoiceListPG cell_choice = new ChoiceListPG("Cell Type","Cubic");
      cell_choice.addItem("Rhombohedral");
      cell_choice.addItem("Tetragonal");
      cell_choice.addItem("Orthorhombic");
      cell_choice.addItem("Monoclinic");
      cell_choice.addItem("Hexagonal");
      cell_choice.addItem("Triclinic");
      addParameter( cell_choice );
      ChoiceListPG centering_choice = new ChoiceListPG("Centering","F Centered");
      centering_choice.addItem("I Centered");
      centering_choice.addItem("C Centered");
      centering_choice.addItem("P Centered");
      centering_choice.addItem("R Centered");
      addParameter( centering_choice );
      addParameter( new BooleanPG("Remove Peaks That Are NOT Indexed",false));
      addParameter( new SaveFilePG("Output File ( Blank to Write to Input File)",""));
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
      S.append("Re-index the peaks in the specified file of peaks, so that the Miller");
      S.append(" indicies correspond to the specified conventional cell type and centering.");
      S.append(" The original indexing MUST correspond to a Niggli reduced cell.  The");
      S.append(" re-indexed peaks can be written back to the original file or to a new");
      S.append(" peaks file.  This operator just updates the indices by multiplying by");
      S.append(" the 3x3 transformation that maps to the conventional cell, so all peaks");
      S.append(" that were originally indexed will also be indexed according to the");
      S.append(" conventional cell.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#ConvertToConventionalCell(java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("An average UB matrix is found based on all of the");
      S.append(" indexed peaks in the file.  The transformation to the");
      S.append(" conventional cell is then found and applied to all of");
      S.append(" the Miller indices in the original list of peak.");
      S.append("@assumptions    "); 
      S.append("The original indexing MUST correspond to a Niggli reduced cell.");
      S.append("@param   ");
      S.append("The peaks file to re-index.");
      S.append("@param   ");
      S.append("The requested cell_type for the new conventional cell.");
      S.append("@param   ");
      S.append("he requested centering for the new conventional cell.");
      S.append("@param   ");
      S.append("Flag that selects whether or not to remove any");
      S.append(" peaks from the file that were originally not");
      S.append(" indexed.");
      S.append("@param   ");
      S.append("Name of the new file to write.  If this is");
      S.append(" blank or a zero length string, the updated list");
      S.append(" of peaks will be written back to the input file.");
      S.append("@error ");
      S.append("This operator will fail if the peaks file can't be");
      S.append(" read or written.  If the original indexing does not");
      S.append(" correspond to a Niggli reduced cell, or if the cell");
      S.append(" does not properly map to a conventional cell of the");
      S.append(" requested type and centering, it may also fail, or");
      S.append(" just produce useless results.");
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
                     "TOF_NSCD",
                     "PEAKS_FILE_UTILS"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String peaks_file = getParameter(0).getValue().toString();
         java.lang.String cell_type = getParameter(1).getValue().toString();
         java.lang.String centering = getParameter(2).getValue().toString();
         boolean remove_unindexed = ((BooleanPG)(getParameter(3))).getbooleanValue();
         java.lang.String out_file = getParameter(4).getValue().toString();
         Operators.TOF_SCD.PeaksFileUtils.ConvertToConventionalCell(peaks_file,cell_type,centering,remove_unindexed,out_file );

         return "Success";
      }catch(java.lang.Exception S0){
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



