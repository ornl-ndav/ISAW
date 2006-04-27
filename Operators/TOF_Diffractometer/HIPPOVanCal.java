/*
 * File:  HIPPOVanCal.java
 *
 * Copyright (C) 2006 J. Tao
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
 * For further information, see <http://www.pns.anl.gov/computing/ISAW/>
 *
 *
 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.*;
//import java.io.StringReader;
import DataSetTools.math.tof_calc;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import Operators.Special.LowPassFilterDS0;


/**
 * This class calculates the vanadium calibration function from a dataset of normalized vanadium intensity in
 * Q. It corresponds to VANCOR of the ATLAS package.
 */
public class HIPPOVanCal implements Wrappable, IWrappableWithCategoryList {
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
//  private boolean isCylinder = true;

  /* @param ISVac1 Variable Aperture Collimator 1, which determines beam width 
   *               and height;
   * @param nrm_van normalized vanadium dataset with background subtracted;
   * @param DOsmooth smooth the vanadium spectra or not;
   * @param temperature vanadium rod temperature.
   */
//  public boolean ISVac1 = true;
  public DataSet ds0;
//  private HIPPOScatter vanrun = (HIPPOScatter) runprops.get(1);
  public DataSet nrm_van;
  public boolean DOsmooth = false;
  public float temperature = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.TEMP");
  public float mulstep = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.MSTEP");

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
    return "HIPPO_VANCAL";
  }

  /**
   * Returns the documentation for this method as a String.  The format follows
   * standard JavaDoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer( "" );
    s.append( "@overview This operator converts a dataset of normalized and");
    s.append( "background subtracted vanadium intensity in Q into a dataset");
    s.append( "of the vanadium calibration function.\n" );
    s.append( "@assumptions The specified DataSet nrm_van is valid.\n" );
    s.append( "@algorithm Please refer to the ATLAS manual.\n");
    s.append( "@param ISVac1 VAC1 beam width and height.\n" );
    s.append( "@param nrm_van input dataset.\n" );
    s.append( "@param DOSmooth toggle on and off vanadium data smoothing.\n" );
    s.append( "@param ISVanSize1 vanadium rod size 1 (\"3/8 ).\n" );
    s.append( "@param ISVanSize2 vanadium rod size 2 (\"1/4 ).\n" );
    s.append( "@return nrm_van the altered dataset as output.\n " );
    s.append( "@error Error messages.\n" );

    return s.toString(  );
  }

  /**
   * Removes dead detectors from the specified DataSet.
   *
   * @return The crunched DataSet.
   */
  public Object calculate(  ) {

    System.out.println("Vanadium multiple scattering calculation...");       
    HIPPOScatter vanrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[1];
    vanrun.mstep = mulstep;
//    isCylinder = !vanrun.isFlatPlate;    
//    CylMulTof vanrunmul = null;
//    FltMulTof vanrunmul_fp = null;
    
    CoralMulTof vanrunmul = null;
    vanrunmul = new CoralMulTof(vanrun);
    vanrunmul.runMulCorrection();
/*
    if (isCylinder) {
      vanrunmul = new CylMulTof(vanrun);
      vanrunmul.runMulCorrection();
    } else {
      vanrunmul_fp = new FltMulTof(vanrun);
      vanrunmul_fp.runMulCorrection();
    }     
*/
    System.out.println("Done.");

/*    
    System.out.println("testing vanadium absorption correction:");
    CylAbsTof vanrunabs = new CylAbsTof(vanrun);
    vanrunabs.runAbsCorrection();
    System.out.println("Done.");
*/
    
    if (DOsmooth) {
      System.out.println("\n"+"STARTING FOURIER FILTERING...");
      (new LowPassFilterDS0(nrm_van, 0.05f, 2)).getResult();
      System.out.println("COMPLETE"+"\n");
    }
    
    System.out.println("Applying multiple scattering correction...");
      
    Data dt;
    AttributeList attr_list_d;
    float scattering_angle, d1, d2, lambda, q, p, mulp;
    float[] Q_vals_d, y_vals_n, e_vals_n, mul = new float[2], data_params = new float[4];
    int ndetchannel;
    String[] target = vanrun.symbol;
    float[] formula = vanrun.formula;
    
    for (int i = 0; i < nrm_van.getNum_entries(); i++){
      dt = nrm_van.getData_entry(i);
      attr_list_d = dt.getAttributeList();
      data_params = (float[])attr_list_d.getAttributeValue(HIPPORunProps.HIPPO_PARM);
      scattering_angle = data_params[0];
      d1 = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
      d2 = data_params[1];
      
      Q_vals_d = dt.getX_scale().getXs();
      y_vals_n = dt.getY_values();
      e_vals_n = dt.getErrors();
      ndetchannel = Q_vals_d.length -1 ;
      for (int k = 0; k < ndetchannel; k++){
        q = 0.5f*(Q_vals_d[k]+Q_vals_d[k+1]);
        lambda = tof_calc.WavelengthofDiffractometerQ(scattering_angle, q);
        p = Platom.plaatom(lambda, target, formula, temperature, scattering_angle, d1, d2, true);
        mul = vanrunmul.getCoeff(scattering_angle, lambda);
//        if (isCylinder) mul = vanrunmul.getCoeff(scattering_angle, lambda);
//        else mul = vanrunmul_fp.getCoeff(scattering_angle, lambda);
//        y_vals_n[k] /= lambda*lambda/(4*Math.PI*Math.sin(scattering_angle/2))*(mul[0]*p+mul[1]);
          mulp = mul[0]*p+mul[1];
          y_vals_n[k] /= mulp;
          e_vals_n[k] /= mulp;

      }      
    }
    nrm_van.setTitle(nrm_van.getTitle()+" "+"--->VANCAL");
    nrm_van.setY_label("vanadium calibration function");
//    nrm_van.addLog_entry("applying vancal() to calculate vanadium calibration function");
    System.out.println("Done.");
    System.out.println("Vanadium dataset converted into the calibration function.\n");
    return nrm_van;
  }

  public static void main (String[] args) {
     
    HIPPOCrunch testcrunch = new HIPPOCrunch();
      
    HIPPOConfigure testconf = new HIPPOConfigure();
    DataSet ds0 = (DataSet)testconf.calculate();      
    testcrunch.ds0 = ds0;
    testcrunch.runfile = new gov.anl.ipns.Util.SpecialStrings.LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/HIPPO_E000002_R012066.nx.hdf");
//    testcrunch.noDeadDetList = true;
//    testcrunch.redpar = new gov.anl.ipns.Util.SpecialStrings.LoadFileString("/IPNShome/taoj/HIPPO/gladrun.par");
    testcrunch.numberofpulses = 38425332;
    java.util.Vector monnrm = (java.util.Vector) testcrunch.calculate();
    
    HIPPOVanCal testvancal = new HIPPOVanCal();
    testvancal.ds0 = ds0;
    testvancal.nrm_van = (DataSet)monnrm.get(1);
    testvancal.calculate();
    DataSetTools.viewer.ViewManager view_nrmvan = new DataSetTools.viewer.ViewManager(testvancal.nrm_van, DataSetTools.viewer.IViewManager.IMAGE);

  }

}
