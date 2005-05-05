/*
 * File:  GLADSumRun.java
 *
 * Copyright (C) 2004 J. Tao
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
 * Contact : Julian Tao <taoj@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 *
 * Modified:
 * $Log$
 * Revision 1.1  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.1  2004/07/23 17:44:45  taoj
 * test version.
 *

 */
package Operators.TOF_Diffractometer;

import java.util.Vector;
import DataSetTools.operator.DataSet.Math.Scalar.DataSetScalarMultiply;
import DataSetTools.operator.DataSet.Math.DataSet.DataSetAdd;
import DataSetTools.operator.Wrappable;
import DataSetTools.math.tof_calc;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IData;
import DataSetTools.dataset.VariableXScale;
import DataSetTools.dataset.XScale;
import gov.anl.ipns.Util.Numeric.arrayUtil;
import java.util.regex.Pattern;


/**
 * This class combines datasets (both detector and monitor ones) from a number of runfiles with a weighting proportion to the average
 * counts of corresponding beam monitor data;
 */
public class GLADSumRun implements Wrappable {
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
  /* @param mon_nrm_list a list of 2-dimension vectors containing the monitor and corresponding detector dataset in that order;
   */
  public Vector mon_nrm_list;
  
  //~ Methods ******************************************************************

  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "GLAD_SUMRUN";
  }

  /**
   * Returns the documentation for this method as a String.  The format follows
   * standard JavaDoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer( "" );
    s.append( "@overview This operator removes detectors from a DataSet " );
    s.append( "according to three criteria, all of which involve the total " );
    s.append( "counts.\n" );
    s.append( "@assumptions The specified DataSet ds is not null.\n" );
    s.append( "@algorithm First this operator removes detectors with zero " );
    s.append( "counts from the specified DataSet. Next it removes detectors " );
    s.append( "below the user specified threshold. Finally the average and " );
    s.append( "standard deviation is found for the total counts, then " );
    s.append( "detectors outside of the user specified number of sigma are " );
    s.append( "removed (generally too many counts).  It also appends a log " );
    s.append( "message indicating that the Crunch operator was applied to " );
    s.append( "the DataSet.\n" );
    s.append( "@param ds Sample DataSet to remove dead detectors from.\n" );
    s.append( "@param min_count Minimum counts to keep.\n" );
    s.append( "@param width How many sigma around the average to keep.\n" );
    s.append( "@param new_ds Whether to make a new DataSet.\n" );
    s.append( "@return DataSet containing the the original DataSet minus the " );
    s.append( "dead detectors.\n" );
    s.append( "@error Returns an error if the specified DataSet ds is null.\n" );

    return s.toString(  );
  }


  
  /**
   * Removes dead detectors from the specified DataSet.
   *
   * @return The crunched DataSet.
   */
  public Object calculate(  ) {

    int nds = mon_nrm_list.size();
    float[] ratio = new float[nds];    
    
    Data bm;
    DataSet dt;
    float[] y_vals;
    Data[] bms = new Data[nds];
    DataSet[] dts = new DataSet[nds];
    
    for (int i = 0; i < nds; i++){
      bms[i] = ((DataSet)((Vector)(mon_nrm_list.get(i))).get(0)).getData_entry(0);
      dts[i] = (DataSet)((Vector)(mon_nrm_list.get(i))).get(1);
    }
    DataSet dm = (DataSet)((Vector)(mon_nrm_list.get(0))).get(0);
    Data bm_sum = bms[0];    
    XScale W_scale_m = bm_sum.getX_scale();
    float[] y_vals_sum = bm_sum.getCopyOfY_values();
    int nmonchannels = y_vals_sum.length;
    
    for (int i = 1; i < nds; i++){
      bm = bms[i];
      bm.resample(W_scale_m, IData.SMOOTH_NONE);
      y_vals = bm.getY_values();
      if (y_vals.length != nmonchannels) System.out.println("******UNEXPECTED ERROR******");
      for (int k = 0; k < nmonchannels; k++){
        y_vals_sum[k] += y_vals[k];  
      }
    }
    
    for (int i = 0; i < nds; i++){
      bm = bms[i];
      y_vals = bm.getCopyOfY_values();
      for (int k = 0; k < nmonchannels; k++){
        if (y_vals_sum[k] == 0.0f) y_vals[k] = 0.0f;
        else y_vals[k] /= y_vals_sum[k];  
      }
      ratio[i] = avespec(y_vals);      
      System.out.println("i: "+i+" ratio: "+ratio[i]);
    }
    
    bm_sum = (Data)bms[0].clone();
    y_vals_sum = bm_sum.getY_values();
    for (int k = 0; k < nmonchannels; k++){
      y_vals_sum[k] = 0.0f;  
    }
    
    System.out.println("\nSumming datasets...");       
    for (int i = 0; i < nds; i++){
      bm = bms[i];  
      y_vals = bm.getY_values();
      for (int k = 0; k < nmonchannels; k++){
        y_vals_sum[k] += ratio[i]*y_vals[k];  
      }
    }
    dm.replaceData_entry(bm_sum, 0);
    
    String msg;
    for (int i = 0; i < nds; i++){
      msg = (String)(new DataSetScalarMultiply(dts[i], ratio[i], false)).getResult();
    }
    
    dt = dts[0];
    for (int i = 1; i < nds; i++){
      msg = (String)(new DataSetAdd(dt, dts[i], false)).getResult();      
    }
    System.out.println("Done.");
    
    Vector mon_nrm_sum = new Vector(2);
    mon_nrm_sum.add(0, dm);
    mon_nrm_sum.add(1, dt);
    
    return mon_nrm_sum ;
    
  }
  
  public static float avespec(float[] y_vals) {
    int istart, iend, isize;
    float ave = 0.0f;
    
    istart = 0;
    iend = y_vals.length-1;
    while (y_vals[istart] == 0.0f) {
      istart++;
    }
    while (y_vals[iend] == 0.0f) {
      iend--;
    }
    isize = iend-istart+1;
    if (isize < 0) {
      System.out.println("***UNEXPECTED ERROR***");
      return 0.0f; 
    }
    
    for (int i = 0; i < isize; i++){
      ave += y_vals[istart+i];        
    }
    ave /= isize;
    
    return ave;
  }


}

