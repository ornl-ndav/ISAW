/*
 * File:  GLADDistinct.java
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
 * Revision 1.3  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.2  2005/01/10 15:35:59  dennis
 * Added getCategoryList method to place operator in menu system.
 *
 * Revision 1.1  2004/07/23 17:44:45  taoj
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
import DataSetTools.dataset.XScale;
import gov.anl.ipns.Util.Numeric.arrayUtil;

/**
 * This class subtracts the self-scattering part from sample differential 
 * cross section and converts vanadium
 * calibration function to flux function for weighting;
 */

public class GLADDistinct implements Wrappable, IWrappableWithCategoryList {
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
  /* @param runfile absolute path of the runfile;
   * @param ISvan the vanadium calibration's beam monitor spectrum is needed for later use;
   */
  public DataSet ds0;
  public DataSet dcs_smp;
  public DataSet smo_van;
  public DataSet dm_van;
  public float temperature = 300.0f;
  public float wmin = 0.1f;
  public float wmax = 6.0f;

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
    return "GLAD_DISTINCT";
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

    System.out.println("Subtract the self-scattering part of differential cross section for sample and prepare the flux weighting function from vanadium data...");
    Data dt, dv, van_dm_Q;
    AttributeList attr_list_d, attr_list_v;
    float scattering_angle_d, scattering_angle_v, d1, d2, lambda_d, q_d, lambda_v, q_v, p;
    float tenfactor, ymax;
    float[] Q_vals_d, y_vals_n, Q_vals_v, y_vals_v, W_vals_vm, Q_vals_vm, y_vals_vm;
    float[] data_params_d = new float[4], data_params_v = new float[4];
    XScale Q_scale_vm;
    int ngrps, ndetchannel, nmonchannel;
    int istart, iend;
    
    GLADScatter smprun = (GLADScatter)((Object[])ds0.getAttributeValue(GLADRunProps.GLAD_PROP))[2];
    String[] list_elements = smprun.symbol;
    float[] list_fractions = smprun.formula;

    ngrps = dcs_smp.getNum_entries();
    if (smo_van.getNum_entries()!=ngrps) System.out.println("\n***UNEXPECTED ERROR***---dcs and smo have different number of groups***");
        
    W_vals_vm = (dm_van.getData_entry(0)).getX_values();
    y_vals_vm = (dm_van.getData_entry(0)).getCopyOfY_values();
    nmonchannel = y_vals_vm.length;
    Q_vals_vm = new float[nmonchannel+1];
    arrayUtil.Reverse( y_vals_vm );
        
    for (int i = 0; i < ngrps; i++){
      dt = dcs_smp.getData_entry(i);
      dv = smo_van.getData_entry(i);
      attr_list_d = dt.getAttributeList();
      attr_list_v = dv.getAttributeList();
      d1 =  ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
          
      data_params_v = (float[])attr_list_v.getAttributeValue(GLADRunProps.GLAD_PARM);
      scattering_angle_v = data_params_v[0];
      data_params_d = (float[])attr_list_d.getAttributeValue(GLADRunProps.GLAD_PARM);
      scattering_angle_d = data_params_d[0];
      d2 = data_params_v[1];
      if (data_params_d[1] != d2) System.out.println("\n***UNEXPECTED ERROR***---vanadium and sample data params not matched.******\n"+
                                                                                           "data_params_d[1]: "+data_params_d[1]+" d2: "+d2);
          
      Q_vals_d = dt.getX_scale().getXs();
      Q_vals_v = dv.getX_scale().getXs();
      y_vals_n = dt.getY_values();
      y_vals_v = dv.getY_values();
          
      istart = 0;
      iend = y_vals_n.length-1;
      if (iend != (y_vals_v.length-1)) System.out.println("***UNEXPECTED ERROR***---dcs and smo unmatched***");
//          System.out.println("ndetchannel: "+ndetchannel);

      while (y_vals_n[istart] == 0.0f) {
        istart++;
      }
      while (y_vals_n[iend] == 0.0f) {
        iend--;
      }
                           
      for (int k = 0; k <= nmonchannel; k++){
        Q_vals_vm[k] = tof_calc.DiffractometerQofWavelength(scattering_angle_v, W_vals_vm[k]);
      }
          
      arrayUtil.Reverse( Q_vals_vm );
      Q_scale_vm = new VariableXScale(Q_vals_vm);
      van_dm_Q = Data.getInstance(Q_scale_vm, y_vals_vm, dv.getGroup_ID());
      van_dm_Q.resample( dv.getX_scale(), IData.SMOOTH_NONE );
 
      for (int k = istart; k <= iend; k++){
        q_d = 0.5f*(Q_vals_d[k]+Q_vals_d[k+1]);
        lambda_d = tof_calc.WavelengthofDiffractometerQ(scattering_angle_d, q_d);
        p = Platom.plaatom(lambda_d, list_elements, list_fractions, temperature, scattering_angle_d, d1, d2, false);
        y_vals_n[k] -= p;
            
        q_v = 0.5f*(Q_vals_v[k]+Q_vals_v[k+1]);
        lambda_v = tof_calc.WavelengthofDiffractometerQ(scattering_angle_v, q_v);            
        if (lambda_v < wmin || lambda_v > wmax) y_vals_v[k] = 0.0f;
        else y_vals_v[k] *= van_dm_Q.getY_values()[k];
//        else y_vals_v[k]  = 1.0f;
//        else y_vals_v[k]  = (float) Math.pow(q_v*q_v*Math.sin(scattering_angle_v/2),2);   
//        else y_vals_v[k] *= 4*Math.PI*Math.sin(scattering_angle_v/2)/lambda_v/lambda_v;
//        else y_vals_v[k] *= van_dm_Q.getY_values()[k]/(4*Math.PI*Math.sin(scattering_angle_v/2))*lambda_v*lambda_v;
      }
    }                               

    dcs_smp.setTitle(dcs_smp.getTitle()+" "+"--->INTERFERE");
    smo_van.setTitle(smo_van.getTitle()+" "+"--->FLUX");
    dcs_smp.setY_label("distinct scattering");
    smo_van.setY_units("counts");
    smo_van.setY_label("calculated vanadium scattering");
    System.out.println("Done.");
    return Boolean.TRUE;
  }

}

