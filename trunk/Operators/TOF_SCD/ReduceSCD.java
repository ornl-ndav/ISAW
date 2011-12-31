/* 
 * File: ReduceSCD.java
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
@see Operators.TOF_SCD.AutoReduceSCD#ReduceSCD(java.lang.String,java.lang.String,int,float,float,java.lang.String,int,float,java.lang.String,float,float,float,float,float,float,float,java.lang.String,java.lang.String,float,boolean,java.lang.String)
 */
public class ReduceSCD extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public ReduceSCD(){
     super("ReduceSCD");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "ReduceSCD";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Instrument Name","TOPAZ"));
      addParameter( new LoadFilePG(".DetCal file (or blank)",null));
      addParameter( new IntegerPG("Histogram Resolution (Steps)",768));
      addParameter( new FloatPG("Max Q to load (|Q|=2pi/d)",14));
      addParameter( new FloatPG("Wavelength power",2.4));
      addParameter( new LoadFilePG("Raw Event File Name","TOPAZ_2480_neutron_event.dat"));
      addParameter( new IntegerPG("Max # of Peaks",100));
      addParameter( new FloatPG("Min Peak Intensity",5));
      addParameter( new SaveFilePG("Peaks file name","Demo.peaks"));
      addParameter( new FloatPG("Lattice Parameter 'a'",4.75));
      addParameter( new FloatPG("Lattice Parameter 'b'",4.75));
      addParameter( new FloatPG("Lattice Parameter 'c'",12.99));
      addParameter( new FloatPG("Lattice Parameter 'alpha'",90));
      addParameter( new FloatPG("Lattice Parameter 'beta'",90));
      addParameter( new FloatPG("Lattice Parameter 'gamma'",120));
      addParameter( new FloatPG("h,k,l tolerance",.12));
      addParameter( new SaveFilePG("Idexed Peaks File","DemoIndexed.peaks"));
      addParameter( new SaveFilePG("Matrix File","Demo.mat"));
      addParameter( new FloatPG("Sphere Radius( |Q|=2pi/d)",0.15));
      addParameter( new BooleanPG("Integrate All Predicted Positions",false));
      addParameter( new SaveFilePG("Integrate File","Demo.integrate"));
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
      S.append("This operator will carry out all of the initial SCD data reduction steps");
      S.append(" on ONE event file, producing a file of integrated intensities.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.AutoReduceSCD#ReduceSCD(java.lang.String,java.lang.String,int,float,float,java.lang.String,int,float,java.lang.String,float,float,float,float,float,float,float,java.lang.String,java.lang.String,float,boolean,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("The raw event file is loaded into a 3D histogram with the specified");
      S.append(" resolution (number of steps) in each direction.  Peaks are then found");
      S.append(" in this 3D histogram.  The peaks are indexed with a matrx determined");
      S.append(" using the specified lattice parameters.  Finally, the peaks are integrated");
      S.append(" by summing the histogram bins whose centers are within the specified");
      S.append(" radius of the peak centers.  A parameter controls whether integration");
      S.append(" should be done for all predicted peak locations, or only for the peaks");
      S.append(" that were actually found.  (This is not currently implemented... only");
      S.append(" found peaks will be integrated.)");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The instrument name, such as TOPAZ or SNAP.  This");
      S.append(" must be the name of a supported instrument at");
      S.append(" the SNS.  The name determines which default");
      S.append(" instrument geometry files (.DetCal, bank and");
      S.append(" mapping files) from the ISAW distribution will");
      S.append(" be used.");
      S.append("@param   ");
      S.append("The .DetCal file to use for this data reduction.");
      S.append(" If null is passed in, the default .DetCal file will be used.");
      S.append("@param   ");
      S.append("The number of steps in each direction to use for");
      S.append(" the underlying 3D histogram in reciprocal space.");
      S.append(" The required storage is 4*num_bins^3.  A num_bins");
      S.append(" value of 768 requires 1.8 Gb of memory and is a");
      S.append(" reasonable compromize between resolution and");
      S.append(" memory size and compute time.");
      S.append("@param   ");
      S.append("The maximum |Q| to use for the histogram region");
      S.append(" and for events loaded from the raw event file.");
      S.append(" NOTE: the value for max_Q is specified here using");
      S.append(" the physics convention that Q = 2*PI/d,");
      S.append(" NOT the crystallographic conventions that");
      S.append(" Q = 1/d.");
      S.append("@param   ");
      S.append("The power on the wavelength term in the Lorentz");
      S.append(" correction factor.  Theoretically, this should");
      S.append(" be 4, but a value of 2.4 works better for finding");
      S.append(" peaks in small molecule data and a value of 1");
      S.append(" works better for large molecules, such as");
      S.append(" protiens.  The Lorentz correction is \"backed out\"");
      S.append(" before writing the .integrate file, since it is");
      S.append(" applied in ANVRED, so this parameter is only a");
      S.append(" fudge factor to even out the data for finding");
      S.append(" peaks.");
      S.append("@param   ");
      S.append("The name of an SNS raw neutron_event.dat file.");
      S.append("@param   ");
      S.append("The maximum number of peaks to look for in the");
      S.append(" histogram.");
      S.append("@param   ");
      S.append("The minimum histogram value that will be");
      S.append(" considered for a possible peak.");
      S.append("@param   ");
      S.append("The name of the file to which the initial list of");
      S.append(" found peaks will be written.");
      S.append("@param   ");
      S.append("Lattice parameter 'a'.");
      S.append("@param   ");
      S.append("Lattice parameter 'b'.");
      S.append("@param   ");
      S.append("Lattice parameter 'c'.");
      S.append("@param   ");
      S.append("Lattice parameter 'alpha'.");
      S.append("@param   ");
      S.append("Lattice parameter 'beta'.");
      S.append("@param   ");
      S.append("Lattice parameter 'gamma'.");
      S.append("@param   ");
      S.append("The required tolerance on h,k and l for a peak to");
      S.append(" be considered to be indexed.  If a peak is not");
      S.append(" indexed the h,k,l values are recorded as 0,0,0.");
      S.append("@param   ");
      S.append("The name of the file to which the indexed peaks");
      S.append(" will be written.");
      S.append("@param   ");
      S.append("The name of the file to which the orientation");
      S.append(" matrix will be written.");
      S.append("@param   ");
      S.append("The radius of the peak region to integrate,");
      S.append(" specified in Q, using the physics convention");
      S.append(" that |Q| = 2pi/d.");
      S.append("@param   ");
      S.append("If true, predict peak positions from the UB");
      S.append(" matrix and integrate all predicted peak");
      S.append(" positions. (THIS IS NOT CURRENTLY IMPLEMENTED).");
      S.append(" If false, just integrate the peaks that were");
      S.append(" actually found.");
      S.append("@param   ");
      S.append("The name of the .integrate file that will be");
      S.append(" written, containing the integrated intensities.");
      S.append("@error ");
      S.append("Exceptions will be thrown if the calculation");
      S.append(" fails at some point, or if specified files cannot");
      S.append(" be read or written.");
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

         java.lang.String instrument = getParameter(0).getValue().toString();
         java.lang.String DetCal_file = getParameter(1).getValue().toString();
         int num_bins = ((IntegerPG)(getParameter(2))).getintValue();
         float max_Q = ((FloatPG)(getParameter(3))).getfloatValue();
         float wavelength_power = ((FloatPG)(getParameter(4))).getfloatValue();
         java.lang.String event_file = getParameter(5).getValue().toString();
         int num_to_find = ((IntegerPG)(getParameter(6))).getintValue();
         float threshold = ((FloatPG)(getParameter(7))).getfloatValue();
         java.lang.String peaks_file = getParameter(8).getValue().toString();
         float a = ((FloatPG)(getParameter(9))).getfloatValue();
         float b = ((FloatPG)(getParameter(10))).getfloatValue();
         float c = ((FloatPG)(getParameter(11))).getfloatValue();
         float alpha = ((FloatPG)(getParameter(12))).getfloatValue();
         float beta = ((FloatPG)(getParameter(13))).getfloatValue();
         float gamma = ((FloatPG)(getParameter(14))).getfloatValue();
         float tolerance = ((FloatPG)(getParameter(15))).getfloatValue();
         java.lang.String indexed_file = getParameter(16).getValue().toString();
         java.lang.String matrix_file = getParameter(17).getValue().toString();
         float radius = ((FloatPG)(getParameter(18))).getfloatValue();
         boolean integrate_all = ((BooleanPG)(getParameter(19))).getbooleanValue();
         java.lang.String integrate_file = getParameter(20).getValue().toString();
         Operators.TOF_SCD.AutoReduceSCD.ReduceSCD(instrument,DetCal_file,num_bins,max_Q,wavelength_power,event_file,num_to_find,threshold,peaks_file,a,b,c,alpha,beta,gamma,tolerance,indexed_file,matrix_file,radius,integrate_all,integrate_file );

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



