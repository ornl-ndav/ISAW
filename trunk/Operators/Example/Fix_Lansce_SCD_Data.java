/* 
 * File: Fix_Lansce_SCD_Data.java
 *  
 * Copyright (C) 2005     Dennis Mikkelson
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
 * Revision 1.1  2005/06/20 03:52:38  dennis
 * Initial version of Operator to fix the LANSCE SCD data
 * as read from NeXus files.
 *
 *
 */

package Operators.Example;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class Fix_Lansce_SCD_Data extends GenericOperator{
   public Fix_Lansce_SCD_Data(){
     super("Fix_Lansce_SCD_Data");
     }

   public String getCommand(){
      return "Fix_Lansce_SCD_Data";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("Lansce,NeXus,SCD DataSet",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new FloatPG("Min time-of-flight(microsec)",1500));
      addParameter( new FloatPG("Max time-of-flight(microsec)",8000));
      addParameter( new FloatPG("Detector width(meters)",0.2f));
      addParameter( new FloatPG("Detector height(meters)",0.2f));
      addParameter( new FloatPG("Sample to detector dist(meters)",0.45f));
      addParameter( new FloatPG("Initial Flight Path(meters)",9.00f));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This operator adjusts the format of data from the SCD at the MLNSC,");
      S.append("and adds information about the time-of-flight range for the data,");
      S.append("initial flight path, sample orientation and the detector position and size.");
      S.append("@algorithm    "); 
      S.append("The data from the NeXus file is arranged by column, with data from");
      S.append("the first column of pixels at the first time-of-flight, followed by the");
      S.append("data from the second column of pixels at the second time-of-flight, etc.");
      S.append("We fill a 3-dimensional array with these values and form a new DataSet");
      S.append("with time-of-flight spectra for each pixel of the area detector.");
      S.append("@assumptions    "); 
      S.append("The DataSet is assumed to be in the form currently (6/14/2005)");
      S.append("produced by the ISAW NeXus reader system for the NeXus files");
      S.append("currently written for the SCD at MLNSC.  This has data organized");
      S.append("by column from the area detector and does not have information");
      S.append("about the position of the detector, the orientation of the sample, etc.");
      S.append("@param   ");
      S.append("LANSCE SCD DataSet where each Data block holds Data from");
      S.append("one column of pixels on the area detector, at one time-of-flight.");
      S.append("@param   ");
      S.append("Minimum time-of-flight, in micro-seconds, for this data");
      S.append("@param   ");
      S.append("Maximum time-of-flight, in micro-seconds, for this data");
      S.append("@param   ");
      S.append("Detector width in meters");
      S.append("@param   ");
      S.append("Detector height in meters");
      S.append("@param   ");
      S.append("Sample to detector distance");
      S.append("@param   ");
      S.append("Initial flight path length");
      S.append("@return A new DataSet is returned with the additional information");
      S.append("");
      S.append("that was required.");
      S.append("@error ");
      S.append("Wrong number of rows and columns.");
      S.append("@error ");
      S.append("Data block has wrong length.");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "Examples"
                     };
   }


   public Object getResult(){
      try{

         DataSetTools.dataset.DataSet ds = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         float t_min = ((FloatPG)(getParameter(1))).getfloatValue();
         float t_max = ((FloatPG)(getParameter(2))).getfloatValue();
         float det_width = ((FloatPG)(getParameter(3))).getfloatValue();
         float det_height = ((FloatPG)(getParameter(4))).getfloatValue();
         float det_dist = ((FloatPG)(getParameter(5))).getfloatValue();
         float length_0 = ((FloatPG)(getParameter(6))).getfloatValue();
         DataSetTools.dataset.DataSet Xres=Operators.Example.LansceUtil.FixSCD_Data(ds,t_min,t_max,det_width,det_height,det_dist,length_0);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


