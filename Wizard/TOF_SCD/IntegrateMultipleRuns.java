/* 
 * File: IntegrateMultipleRuns.java
 *  
 * Copyright (C) 2008     Ruth Mikkelson
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

package Wizard.TOF_SCD;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.operator.Generic.TOF_SCD.Integrate_new;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;
import java.util.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import Operators.TOF_SCD.IntegrateUtils;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class IntegrateMultipleRuns extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public IntegrateMultipleRuns(){
     super("IntegrateMultipleRuns");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "IntegrateMultipleRuns";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataDirPG("path to data files",""));
      addParameter( new DataDirPG("output path",""));
      addParameter( new ArrayPG("run numbers","[]"));
      addParameter( new IntArrayPG("Data set nums","[0]"));
      addParameter( new StringPG("experiment name",""));
      addParameter( new ChoiceListPG("Centering Type", IntegrateUtils.CenteringNames ) );
      addParameter( new BooleanPG("Calibrate the data sets(yes/no)",new Boolean(false)));
      addParameter( new LoadFilePG("Calibration file",null));
      addParameter( new IntegerPG("line or mode",-1));
      addParameter( new IntArrayPG("Offsets from peak time","-1:3"));
      addParameter( new IntegerPG("Increment slice amount",1));
      addParameter( new StringPG("Instrument","SCD0"));
      addParameter( new StringPG("File extension",".run"));
      addParameter( new FloatPG("Minimum d-spacing",0));
      addParameter( new FloatPG("Max Unit Cell Length",12));
      addParameter( new IntArrayPG("Pixel Rows to keep(blank for all)","1,3000"));
      addParameter( new IntArrayPG("Pixel Cols to keep(blank for all)","1:3000"));
      addParameter( new ChoiceListPG("Peak Algorithm",new String[]{"MaxIToSigI","Shoe Box", "MaxIToSigI-old","TOFINT",Integrate_new.FIT_PEAK,"EXPERIMENTAL"}));
      addParameter( new IntArrayPG("Box Delta x (col) Range","-2:2"));
      addParameter( new IntArrayPG("Box Delta y (row) Range","-2:2"));
      addParameter( new FloatPG("Use Shoe Box integration for peaks below this I/sig(I) ratio",0));
      addParameter( new IntegerPG("Max running threads",1));
      addParameter( new BooleanPG("Pop Up Log Info",new Boolean(false)));
      addParameter( new BooleanPG("Pop Up integrate file",new Boolean(false)));
      addParameter( new BooleanPG("Append to prev integrate file",new Boolean(false)));
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
      S.append("For each detector in multiple files,finds theoretical positions of peaks");
      S.append(" and integrates them. Uses threads to integrate different data set\n");
      S.append("@algorithm    "); 
      S.append("Several integrate algorithms are available.");
      S.append("@assumptions    "); 
      S.append("The orientation matrix files are already created and in outpath +\"ls\"+expName+runnum+\".mat\"");
      S.append("@param   ");
      S.append("The path where the multiple data set files are stored");
      S.append("@param   ");
      S.append("The path where all the outputs go");
      S.append("@param   ");
      S.append("The Run numbers of the data set files");
      S.append("@param   ");
      S.append("The data set numbers in a file to \"integrate\"");
      S.append("@param   ");
      S.append("The name of the experiment");
      S.append("@param   ");
      S.append("Centering type: \n");
      for ( int i = 0; i < IntegrateUtils.CenteringNames.length; i++ )
        S.append(IntegrateUtils.CenteringNames[i] + "\n" );
      S.append("@param   ");
      S.append("Calibrate the data sets(yes/no)");
      S.append("@param   ");
      S.append("The calibration file used to calibrate the  data sets");
      S.append("@param   ");
      S.append("The line in the calibration file to use or mode");
      S.append("@param   ");
      S.append("time-slice range around peak center");
      S.append("@param   ");
      S.append("Increment slice size by");
      S.append("@param   ");
      S.append("Instrument name(Prefix after path for a file)");
      S.append("@param   ");
      S.append("Extension for filename");
      S.append("@param   ");
      S.append("minimum d-spacing to consider");
      S.append( "@param " );
      S.append( "Maximum unit cell length in real space" );
      S.append( "@param " );
      S.append( "Pixel rows to keep or blank for all. Currently the range of" );
      S.append( "   rows from min in list to max in list are kept" );
      S.append( "@param " );
      S.append( "Pixel cols to keep or blank for all" );
      S.append("@param   ");
      S.append("Peak Algorithm: MaxIToSigI, Shoe_Box, MaxIToSigI-old, TOFINT or EXPERIMENTAL");
      S.append("@param   ");
      S.append("Box Delta x (col) Range (-2:2)");
      S.append("@param   ");
      S.append("Box Delta y (row) Range (-2:2)");
      S.append("@param   ");
      S.append("maximum peak for Shoe box intgration");
      S.append("@param   ");
      S.append("The maximum number of threads to run");
      S.append("@param   ");
      S.append("Pop up the log file");
      S.append("@param   ");
      S.append("Pop up the Peaks file");
      S.append("@param   ");
      S.append("append to previous integrate file");
      S.append("@error ");
      S.append("No Data Seets to integrate");
      S.append(" No span around a peak to integrate");
      S.append(" Cannot read data files or there is some error in the file");
      S.append(" Threads take too long to run.");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "HiddenOperator"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String path = getParameter(0).getValue().toString();
         java.lang.String outpath = getParameter(1).getValue().toString();
         Vector run_numbers = (Vector)(getParameter(2).getValue());
         java.lang.String DataSetNums = (java.lang.String)(getParameter(3).getValue());
         java.lang.String expName = getParameter(4).getValue().toString();
         java.lang.String centering = getParameter(5).getValue().toString();
         boolean useCalibFile = ((BooleanPG)(getParameter(6))).getbooleanValue();
         java.lang.String calibfile = getParameter(7).getValue().toString();
         int line2use = ((IntegerPG)(getParameter(8))).getintValue();
         java.lang.String time_slcie_range = (java.lang.String)(getParameter(9).getValue());
         int increase = ((IntegerPG)(getParameter(10))).getintValue();
         java.lang.String instr = getParameter(11).getValue().toString();
         java.lang.String FileExt = getParameter(12).getValue().toString();
         float d_min = ((FloatPG)(getParameter(13))).getfloatValue();
         float MaxUnitCellLength = ((FloatPG)(getParameter(14))).getfloatValue();
         java.lang.String PixRows = ((IntArrayPG)getParameter(15)).getStringValue( );
         java.lang.String PixCols = ((IntArrayPG)getParameter(16)).getStringValue( );
         java.lang.String PeakAlg = getParameter(17).getValue().toString();
         java.lang.String Xrange = (java.lang.String)(getParameter(18).getValue());
         java.lang.String Yrange = (java.lang.String)(getParameter(19).getValue());
         float max_shoebox = ((FloatPG)(getParameter(20))).getfloatValue();
         int MaxThreads = ((IntegerPG)(getParameter(21))).getintValue();
         boolean ShowLog = ((BooleanPG)(getParameter(22))).getbooleanValue();
         boolean ShowPeaks = ((BooleanPG)(getParameter(23))).getbooleanValue();
         boolean append    =(( BooleanPG)(getParameter(24))).getbooleanValue( );
         java.lang.Object Xres=Wizard.TOF_SCD.Util.IntegrateMultipleRuns(path,outpath,run_numbers,DataSetNums,expName,
                  centering,useCalibFile,calibfile,line2use,time_slcie_range,increase,instr,FileExt,d_min,
                  MaxUnitCellLength, PixRows,PixCols,PeakAlg,Xrange,Yrange,max_shoebox,MaxThreads,ShowLog, ShowPeaks, append );

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



