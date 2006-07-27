/*
 * File:  HIPPOAnalyze.java
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.2  2006/07/27 15:11:15  dennis
 * Added logging tag, so future CVS commits will be
 * recorded in the file.
 *
 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.*;
import DataSetTools.math.tof_calc;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;

/**
 * This class performs the multiple scattering and attenuation correction.
 * It corresponds to ANALYSE/CORAL of the ATLAS package.
 */
public class HIPPOAnalyze implements Wrappable, IWrappableWithCategoryList {
  //~ Instance fields **********************************************************
  
  /* @param runfile absolute path of the runfile;
   * @param ISvan the vanadium calibration's beam monitor spectrum is needed for later use;
   */

  public DataSet ds0;
  public DataSet ds;
  public int imask; //1 for sample rod, 2 for empty can, 3 for smp+can;
//  public float scattererm = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.SCC");
  public float mulstep = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.MSTEP");
  public float absstep = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.ASTEP");
  public boolean usemutfile = false;
  public LoadFileString mutfile = new LoadFileString();
  public float minw = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.MUT.MINW");
  public float maxw = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.MUT.MAXW");
  public float dw = HIPPORunProps.getfloatKey(HIPPORunProps.defHIPPOProps, "HIPPO.ANALYSIS.MUT.DW");
  private boolean isCylinder = true;
  
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
    return "HIPPO_ANALYZE";
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
   * Performs multiple scattering and attenuation correction.
   *
   * @return The crunched DataSet.
   */
  public Object calculate(  ) {

    HIPPOScatter thisrun = null;    
/*
    if (imask == 1) thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[2];     
    else if (imask == 2) thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[3];
    else if (imask == 3) thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[2];
    else if (imask == 4) thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[4];
    else if (imask == 6) thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[3];
    else if (imask == 7) thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[2];    
    else System.out.println("\n***UNEXPECTED ERROR***\n"+"imask: "+imask);
*/
    switch (imask) {
      case 1:
      case 3:
      case 7:
        thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[2];
        break;
      case 2:
      case 6:
        thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[3];
        break;
      case 4:
        thisrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[4];
        break;
      default:
        System.out.println("\n***UNEXPECTED ERROR***\n"+"imask: "+imask);
        break;
    }

    isCylinder = !thisrun.isFlatPlate;

    thisrun.mstep = mulstep;
    thisrun.astep = absstep;
    thisrun.minw = minw;
    thisrun.maxw = maxw;
    thisrun.dw = dw;
    
    if(usemutfile == true) {
      thisrun.setMutTable(mutfile.toString());
      thisrun.setAbsInput();
      thisrun.setMulInput();
    }
    
    System.out.println("\n---"+HIPPOScatter.SMASK[imask]+"---\n"); 
        
    System.out.println("multiple scattering calculation...");
    CoralMulTof thisrunmul = new CoralMulTof(thisrun);
    thisrunmul.runMulCorrection();
//    FltMulTof thisrunmul_fp = null;

/*    
    if (isCylinder) {
      thisrunmul = new CylMulTof(thisrun);
      thisrunmul.runMulCorrection();
    } else {
      thisrunmul_fp = new FltMulTof(thisrun);
      thisrunmul_fp.runMulCorrection();
    }
*/
    
    System.out.println("Done.\n");
    
    CoralAbsTof abs_smp = null, abs_can = null;
//    FltAbsTof abs_smp_fp = null, abs_can_fp = null;
    HIPPOScatter smprun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[2];
    HIPPOScatter canrun = (HIPPOScatter)((Object[])ds0.getAttributeValue(HIPPORunProps.HIPPO_PROP))[3];   
//    if( (imask & 1) == 1 && scattererm != 0.0f) smprun.scatterern = scattererm;
//    if (isCylinder) {
      if (smprun.abs_run == null) {
        abs_smp = new CoralAbsTof(smprun);
        smprun.abs_run = abs_smp;
        System.out.println("Attenuation calculation for sample...");      
        abs_smp.runAbsCorrection();
        System.out.println("Done.");
      
        if (imask >= 4) {
          abs_can = new CoralAbsTof(canrun);
          canrun.abs_run = abs_can;    
          System.out.println("Attenuation calculation for can...");
          abs_can.runAbsCorrection();
          System.out.println("Done.");
        }            
      } else {
        abs_smp = smprun.abs_run;
        if (imask >= 4) abs_can = canrun.abs_run;
      }   

//    } 
/*    
    else {
      if (smprun.abs_run_fp == null) {
        abs_smp_fp = new FltAbsTof(smprun);
        smprun.abs_run_fp = abs_smp_fp;
        System.out.println("Attenuation calculation for flat plate sample...");      
        abs_smp_fp.runAbsCorrection();
        System.out.println("Done.");
      
        if (imask >= 4) {
          abs_can_fp = new FltAbsTof(canrun);
          canrun.abs_run_fp = abs_can_fp;    
          System.out.println("Attenuation calculation for flat plate can...");
          abs_can_fp.runAbsCorrection();
          System.out.println("Done.");
        }            
      } else {
        abs_smp_fp = smprun.abs_run_fp;
        if (imask >= 4) abs_can_fp = canrun.abs_run_fp;
      }   
    }
*/
    System.out.println("Applying "+HIPPOScatter.SMASK[imask]+" multiple scattering and attenuation correction...");
    
    Data dt;
    AttributeList attr_list_d;
    float scattering_angle, q, lambda, alpha;
    float[] qlist, y_vals_n, e_vals_n, mul = null, abs_s = null, abs_c = null;
    int istart, iend;
    if (ds.getX_label() != "Q") System.out.println("******ERROR******");
    if ((imask & 1) == 1) System.out.println("Sample effective density: "+smprun.effdensity+" Sample calibration constant: "+smprun.scatterern+"\n");
    for (int i = 0; i < ds.getNum_entries(); i++) {
      dt = ds.getData_entry(i);
      attr_list_d = dt.getAttributeList();
      scattering_angle = ((float[])attr_list_d.getAttributeValue(HIPPORunProps.HIPPO_PARM))[0];
      qlist = dt.getX_scale().getXs();
      y_vals_n = dt.getY_values();
      e_vals_n = dt.getErrors();
      istart = 0;
      iend = y_vals_n.length-1;

      try {
        while (y_vals_n[istart] == 0.0f) istart++;  
      } catch(IndexOutOfBoundsException e) {
        e.printStackTrace();
        System.out.println("!!!!!!Total number of counts is zero. This" +
          " occurs in the rare case that a data group is alive with vanadium" +
          " but goes dead with the current runfile/dataset. The dead detector" +
          " list needs to be updated.!!!!!!");
        System.out.println("dataset: "+ds.getTitle());
        System.out.println("datablock: "+dt.getGroup_ID());
      }      
      while (y_vals_n[iend] == 0.0f) iend--;
      
      for (int k = istart; k <= iend; k++){
        q = .5f*(qlist[k]+qlist[k+1]);
        lambda = tof_calc.WavelengthofDiffractometerQ(scattering_angle, q);
//      if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);
//        if (isCylinder) {
          mul = thisrunmul.getCoeff(scattering_angle, lambda);
          abs_s = abs_smp.getCoeff(scattering_angle, lambda);
          if (imask >= 4) abs_c = abs_can.getCoeff(scattering_angle, lambda);
//        } 
/*        
        else {
          mul = thisrunmul_fp.getCoeff(scattering_angle, lambda);
          abs_s = abs_smp_fp.getCoeff(scattering_angle, lambda);
          if (imask >= 4) abs_c = abs_can_fp.getCoeff(scattering_angle, lambda);
        }
*/
//        delta = mul[0]/(mul[0]+mul[1]);
      
//            if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);
        if (imask == 2) {//empty can;
          y_vals_n[k] -= mul[1];
//          y_vals_n[k] *= delta;
//            if (k==0) System.out.println("cell mul[1]: "+mul[1]+" abs[3]: "+abs[3]+" abs[2]: "+abs[2]);     
          alpha = abs_s[2]/abs_s[3];
//          y_vals_n[k] /= abs[3];
//          y_vals_n[k] *= abs[2];
          y_vals_n[k] *= alpha;
          e_vals_n[k] *= alpha;
        }
        else if (imask == 4) {//empty furnace;
          y_vals_n[k] -= mul[1];
          alpha = abs_s[3]/abs_s[4]-abs_s[2]/abs_c[1]*abs_c[2]/abs_s[4];
          y_vals_n[k] *= alpha;
          e_vals_n[k] *= alpha;
        }
        else if (imask == 6) {//empty can with furnace;
          y_vals_n[k] -= mul[1];
          alpha = abs_s[2]/abs_c[1];
          y_vals_n[k] *= alpha;
          e_vals_n[k] *= alpha;
        }
        else if ( (imask & 1) == 1 ) {          //inner core:  calibration rod (vanadim, fused silica), or (sample+can) with (can) part subtracted;
//            if (k==0) System.out.println("core mul[1]"+mul[1]+" abs[1]: "+abs[1]);
          y_vals_n[k] -= mul[1];
//          y_vals_n[k] *= delta;
          alpha = abs_s[1]*smprun.scatterern;
//          y_vals_n[k] /= abs[1];
//          y_vals_n[k] /= smpincanrun.scatterern;
          y_vals_n[k] /= alpha;
          e_vals_n[k] /= alpha;
//              if (y_vals_n[k] < y_vals_min) y_vals_min=y_vals_n[k];
    /*          if (y_vals_n[k] > 10.0f || y_vals_n[k] < -10.0f) {
                y_vals_n[k]=0.0f;
                System.out.println("\n"+"abnormal values: "+i+"\n");
              }*/
        }
//            if (k == 0) System.out.println("i: "+i+" y_vals_n[k]: "+y_vals_n[k]);
      } 
    }
        
    ds.setTitle(ds.getTitle()+" "+"--->CORAL/ANALYSE");
//    ds.setY_units("barns"); //unit can't be set/changed here as HIPPO_CRUNCH() result will subtract that of HIPPO_ANALYZE() in a script;
    ds.setY_label("Time-Of-Flight differential crosssection");
    System.out.println("Done.");

    return ds;
  }

  public static void main(String[] args) {

 
  }

}
