/*
 * File:  GLADCombine.java
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
 * Modified:
 * $Log$
 * Revision 1.2  2005/01/10 15:36:00  dennis
 * Added getCategoryList method to place operator in menu system.
 *
 * Revision 1.1  2004/07/23 17:45:11  taoj
 * test version.
 *
 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.*;
import DataSetTools.math.tof_calc;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IData;
import DataSetTools.dataset.VariableXScale;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.XScale;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.Util.Numeric.arrayUtil;


/**
 * This class preforms deadtime and delayed neutron corrections, calculates 
 * detector efficiency, normalizes detector counts to beam monitor counts, 
 * convert TOF to wavelength then rebin to Q.
 */
public class GLADCombine implements Wrappable, IWrappableWithCategoryList {
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
  /* @param runfile absolute path of the runfile;
   * @param ISvan the vanadium calibration's beam monitor spectrum is needed for later use;
   */
  public DataSet int_smp;
  public DataSet flx_van;
  
  //~ Methods ******************************************************************

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get an array of strings listing the operator category names  for 
   * this operator. The first entry in the array is the 
   * string: Operator.OPERATOR. Subsequent elements of the array determine
   * which submenu this operator will reside in.
   * 
   * @return  A list of Strings specifying the category names for the
   *          menu system 
   *        
   */
  public String[] getCategoryList()
  {
    return Operator.TOF_NGLAD;
  }


  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "GLAD_COMBINE";
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
    
    System.out.println("Merge data by flux weighting...");
    Data dt, dv, dioq, dsum;
    XScale Q_scale;
    int numq;
    float flag;
    float[] Q_vals, y_vals_int, y_vals_flx, y_vals_ioq, y_vals_sum;

    numq = (int)GLADRunInfo.GLADQMAX*GLADRunInfo.NUMQ+1;   
    Q_scale = new UniformXScale(1.0f/GLADRunInfo.NUMQ, GLADRunInfo.GLADQMAX+1.0f/GLADRunInfo.NUMQ, numq);
    dioq = Data.getInstance(Q_scale, new float[numq-1], 1000);
    dsum = Data.getInstance(Q_scale, new float[numq-1], 1001);    
    float scattering_angle;
        
    for (int i = 0; i < flx_van.getNum_entries(); i++){
      dv = flx_van.getData_entry(i);
      dsum = dsum.stitch(dv, Data.SUM);
      dt = int_smp.getData_entry(i);
      Q_vals = dt.getX_scale().getXs();
      y_vals_int = dt.getY_values();
      y_vals_flx = dv.getY_values();
      for (int k = 0; k < Q_vals.length-1; k++){
        y_vals_flx[k] *= y_vals_int[k];
      }
      dioq = dioq.stitch(dv, Data.SUM);
    }    
    
    y_vals_ioq = dioq.getY_values();
    y_vals_sum = dsum.getY_values();
    for (int i = 0; i < numq-1; i++){
      if (y_vals_sum[i] != 0){
        y_vals_ioq[i] /= y_vals_sum[i];  
      }
    }
    
    int_smp.addData_entry(dioq);
    int_smp.setTitle(int_smp.getTitle()+" "+"--->COMBINE");
    int_smp.setY_label("distinct scattering");
    System.out.println("Done.");
    return int_smp;
  }    

}


