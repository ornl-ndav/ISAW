/* 
 * File: IntegrateHKL.java
 *  
 * Copyright (C) 2006     Dennis Mikkelson
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
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2006/03/16 18:04:42  dennis
 * Added suffix "f" to float parameters to FloatPG so it would
 * still compile and work under earlier versions.
 * Converted dos to unix text.
 *
 * Revision 1.1  2006/03/16 17:54:07  dennis
 * Operator generated around the IntegrateHKL() static method
 * from IntegrateHKLRegion.java, using the Method2OperatorWizard.
 *
 *
 */

package Operators.TOF_SCD;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class IntegrateHKL extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public IntegrateHKL(){
     super("IntegrateHKL");
     }


   /**
    * Gives the user the command for the operator.
    *
	* @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "IntegrateHKL";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("Select DataSet",DataSet.EMPTY_DATA_SET));
      addParameter( new IntegerPG("Detector ID",17));
      addParameter( new FloatPG("Region min h",0.9f));
      addParameter( new FloatPG("Region max h",1.1f));
      addParameter( new IntegerPG("Num steps in h",10));
      addParameter( new FloatPG("Region min k",0.9f));
      addParameter( new FloatPG("Reginon max k",1.1f));
      addParameter( new IntegerPG("Num steps in k",10));
      addParameter( new FloatPG("Region min l",0.9f));
      addParameter( new FloatPG("Region max l",1.1f));
      addParameter( new IntegerPG("Num steps in l",10));
      addParameter( new SaveFilePG("Log File Name",""));
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
      S.append("Calculate an approximate integral over a rectangluar region in h,k,l.");
      S.append(" The specified range of h, k and l values is subdivided into the");
      S.append(" specified number of steps in the h, k and l directions.  The three");
      S.append(" dimensional array of division points, (h,k,l) are mapped back to");
      S.append(" points in the raw data and a value is interpolated at each point.");
      S.append(" The sum of these values is returned, along with other information.");
      S.append(" Specifically, the number of sample points that mapped into the");
      S.append(" detector data, and the number that mapped outside the detector");
      S.append(" data are returned.  Finally, DataSets containing the profile functions");
      S.append(" along the h, k, and l directions are returned.  If a valid filename");
      S.append(" is specified, the sample points and values will also be written to");
      S.append(" the specified file.");
      S.append("@algorithm    "); 
      S.append("The specified rectangular region in (h,k,l) will be subdivided");
      S.append(" using the specified number of steps in each direction.  If the");
      S.append(" number of steps in h is specified to be 10, the 11 evenly");
      S.append(" spaced planes of constant h will be used spanning the");
      S.append(" interval [min_h,max_h].  The k and l directions are treated");
      S.append(" similarly.  This determines a set of sample points across the");
      S.append(" rectangular region.  Each of these sample points is mapped");
      S.append(" back to \"Q\", and ultimately back to a point in the raw detector");
      S.append(" data, (COL,ROW,CHAN).  The raw data values are assumed");
      S.append(" to be known at voxel centers, and the values at the eight");
      S.append(" nearest voxel centers are interpolated to obtain an intensity");
      S.append(" at the specified point.  If the sample point mapped outside");
      S.append(" of the detector data, a value of -1 is taken as the value at");
      S.append(" that point.  All points with non-negative intensities are");
      S.append(" summed.");
      S.append(" In addition to interpolating values and summing the non-");
      S.append(" negative values, this operator also calculates the profiles");
      S.append(" of the volume of data in the h,k,l directions.  The profile");
      S.append(" in the h direction is obtained by summing in the k and l");
      S.append(" directions on each plane of constant h.  These sums");
      S.append(" form a function of h, which is returned in the \"h profile\"");
      S.append(" DataSet.  The \"k profile\" and \"l profile\" DataSets are");
      S.append(" calculated and returned in analogous ways.");
      S.append("@assumptions    "); 
      S.append("The DataSet must have had an orientation matrix loaded using");
      S.append(" the LoadOrientaton() operator, in order for the h,k,l values to");
      S.append(" be meaningful.  In addition, for IPNS runfiles, it should have had");
      S.append(" the calibration information loaded, using the LoadSCDCalib()");
      S.append(" operator, or the specified regions will not be accurately");
      S.append(" identified.");
      S.append("@param   ");
      S.append("The DataSet for this run, containing all");
      S.append(" needed attributes, including the orientation");
      S.append(" matrix.");
      S.append("@param   ");
      S.append("The id of the detector to use");
      S.append("@param   ");
      S.append("The minimum h value");
      S.append("@param   ");
      S.append("The maximum h value");
      S.append("@param   ");
      S.append("The number of intervals in the h direction");
      S.append("@param   ");
      S.append("The minimum k value");
      S.append("@param   ");
      S.append("The maximum k value");
      S.append("@param   ");
      S.append("The number of intervals in the k direction");
      S.append("@param   ");
      S.append("The minimum l value");
      S.append("@param   ");
      S.append("The maximum l value");
      S.append("@param   ");
      S.append("The number of intervals in the l direction");
      S.append("@param   ");
      S.append("The name of the file to write the list of");
      S.append(" of interpolated intensities to.  If this");
      S.append(" is blank, or the specified file cannot be");
      S.append(" created, no file will be written.");
      S.append("@return This method returns a Vector with six pairs of entries.  The");
      S.append("");
      S.append(" pairs consist of a descriptive String followed by a value.");
      S.append(" The first pair of entries are a String name and Float containing");
      S.append(" the sum of all of the interpolated intensities in and on the boundary");
      S.append(" of the specified region.");
      S.append(" The second pair of entries are a String name and an Integer giving");
      S.append(" the number of sample points in the specified region that mapped");
      S.append(" INSIDE of the specified detector's data.");
      S.append(" The third pair of entries are a String name and an Integer giving");
      S.append(" the number of sample points in the specified region that mapped");
      S.append(" OUTSIDE of the specified detector's data.");
      S.append(" The fourth pair of entries are a String name and a DataSet containing");
      S.append(" the \"h profile\", i.e. sums of the interpolated intensities in the");
      S.append(" directions of k and l, as a function of h.");
      S.append(" The fifth pair of entries are a String name and a DataSet containing");
      S.append(" the \"k profile\", i.e. sums of the interpolated intensities in the");
      S.append(" directions of h and l, as a function of k.");
      S.append(" The sixth pair of entries are a String name and a DataSet containing");
      S.append(" the \"l profile\", i.e. sums of the interpolated intensities in the");
      S.append(" directions of h and k, as a function of l.");
      S.append("@error ");
      S.append("If an orientation matrix has not been loaded,");
      S.append(" an Exception will be thrown, and the operator");
      S.append(" will terminate.");
      S.append(" If the DataGrid for the specified detector ID");
      S.append(" is not found in the DataSet, an Exception");
      S.append(" will be thrown and the operator will terminate.");
      S.append(" If a file is specified that cannot be written to, and");
      S.append(" IO Exception will be generated, but this will only");
      S.append(" prevent the file from being written.  The rest of");
      S.append(" the operator will complete normally.");
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

         DataSetTools.dataset.DataSet ds = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int det_id = ((IntegerPG)(getParameter(1))).getintValue();
         float min_h = ((FloatPG)(getParameter(2))).getfloatValue();
         float max_h = ((FloatPG)(getParameter(3))).getfloatValue();
         int n_h_steps = ((IntegerPG)(getParameter(4))).getintValue();
         float min_k = ((FloatPG)(getParameter(5))).getfloatValue();
         float max_k = ((FloatPG)(getParameter(6))).getfloatValue();
         int n_k_steps = ((IntegerPG)(getParameter(7))).getintValue();
         float min_l = ((FloatPG)(getParameter(8))).getfloatValue();
         float max_l = ((FloatPG)(getParameter(9))).getfloatValue();
         int n_l_steps = ((IntegerPG)(getParameter(10))).getintValue();
         java.lang.String filename = getParameter(11).getValue().toString();
         java.lang.Object Xres=Operators.TOF_SCD.IntegrateHKLRegion.IntegrateHKL(ds,det_id,min_h,max_h,n_h_steps,min_k,max_k,n_k_steps,min_l,max_l,n_l_steps,filename);
         return Xres;
       }catch(java.lang.Exception S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


