/*
 * File:  GLADAnalyze.java
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
 * Revision 1.1  2004/07/23 17:44:03  taoj
 * test version.
 *

 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.Wrappable;
import java.io.StringReader;
import java.util.regex.Pattern;
import DataSetTools.math.tof_calc;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;

/**
 * This class preforms GLAD multiple scattering and attenuation correction;
 */
public class GLADAnalyze implements Wrappable {
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
  /* @param runfile absolute path of the runfile;
   * @param ISvan the vanadium calibration's beam monitor spectrum is needed for later use;
   */
  public boolean ISVac1 = true;
  public boolean ISEmptyCan = false;
  public DataSet ds;
  public boolean ISCanSize1 = true;
  public boolean ISCanSize2 = false;
  public float SmpHeight = 6.0f;
  public String SmpComposition;
  public float SmpNumberDensity = 0.05f;
  public float mulstep = 0.1f;
  public float absstep = 0.02f;
  public boolean ISSilicaRod = false;
  //~ Methods ******************************************************************

  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "GLAD_ANALYZE";
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

  public static void glad_coral (String[][] target, float[][] formula, 
                            float Sht, float[] radii, float[] density, float Bwid, float Bht,
                            boolean ISmul, float astep) {
 
    try {
      MutCross.run(MutCross.sigmatable);    
      if (ISmul) {
        String MulSetup = MutCross.MulAbsInputMaker(target, formula, Sht, radii, density, Bwid, Bht, astep);
        StringReader input_cylmulin = new StringReader(MulSetup);
        CylMulTof.run(input_cylmulin); 
      } else {
        String AbsSetup = MutCross.MulAbsInputMaker(target, formula, Sht, radii, density, Bwid, Bht, astep);
        StringReader input_cylabsin = new StringReader(AbsSetup);
        CylAbsTof.run(input_cylabsin, radii.length-1);       
      }
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace();
      }    
  }
  
  /**
   * Removes dead detectors from the specified DataSet.
   *
   * @return The crunched DataSet.
   */
  public Object calculate(  ) {
    
    String[][] target = null;
    float[][] formula = null;
    float Sht = 0.0f, Bwid = 0.0f, Bht = 0.0f;
    float[] radii = null, density = null;
    
    if (ISCanSize1 && ISCanSize2) {
      System.out.println("******ERROR: one can at a time please.******");
      return null;
    }
    
    if (ISVac1) {
      Bwid = GLADRunInfo.Vac1Bwid;
      Bht = GLADRunInfo.Vac1Bht;
    }
    
    String[] list_composition = Pattern.compile("[\\s,]+").split(SmpComposition);
    int nelements = list_composition.length;
    if (nelements%2 != 0) System.out.println("******INPUT ERROR******");
    nelements /= 2;
    String[] list_elements = new String[nelements];
    float[] list_fractions = new float[nelements];
    for (int i = 0; i < nelements; i++){
      list_elements[i] = list_composition[2*i];
      list_fractions[i] = (new Float(list_composition[2*i+1])).floatValue();
    }
        
    if(ISCanSize1) {
      radii = new float[] {0.0f, GLADRunInfo.Can1Size[0], GLADRunInfo.Can1Size[1]};
//      Sht = GLADRunInfo.Can1Size[2];
    }
    else if (ISCanSize2) { 
      radii = new float[] {0.0f, GLADRunInfo.Can2Size[0], GLADRunInfo.Can2Size[1]};
//      Sht = GLADRunInfo.Can2Size[2]; 
    }
    Sht = SmpHeight;
    target = new String[][] {list_elements, GLADRunInfo.CanVan};
    formula = new float[][] {list_fractions, GLADRunInfo.CanFormula};
    density = new float[] {SmpNumberDensity, GLADRunInfo.CanDensity};
    
    if (ISSilicaRod){
      radii = new float[] {GLADRunInfo.SilicaRodSize[0], GLADRunInfo.SilicaRodSize[1]};
      Sht = GLADRunInfo.SilicaRodSize[2]; 
      target = new String[][] {list_elements};
      formula = new float[][] {list_fractions};
      density = new float[] {SmpNumberDensity};
    }
    
    System.out.println("Sample Attenuation calculation..."); 
    glad_coral (target, formula, Sht, radii, density, Bwid,  Bht, false, absstep); //attenuation correction;
    System.out.println("Done.");
    
    if (ISEmptyCan) {
      if(ISCanSize1) {
        radii = new float[] {GLADRunInfo.Can1Size[0], GLADRunInfo.Can1Size[1]};
        Sht = GLADRunInfo.Can1Size[2];
      }
      else if (ISCanSize2) { 
        radii = new float[] {GLADRunInfo.Can2Size[0], GLADRunInfo.Can2Size[1]};
        Sht = GLADRunInfo.Can2Size[2]; 
      }
    target = new String[][] {GLADRunInfo.CanVan};
    formula = new float[][] {GLADRunInfo.CanFormula};
    density = new float[] {GLADRunInfo.CanDensity};  
    } 
    
    if(ISEmptyCan) System.out.println("Empty can multiple scattering calculation...");
    else System.out.println("Sample multiple scattering calculation...");
    glad_coral (target, formula, Sht, radii, density, Bwid,  Bht, true, mulstep); //multiple scattering correction;
    System.out.println("Done.");
    
    Data dt;
    DetectorPosition position;
    AttributeList attr_list_d;
    float scattering_angle, q, lambda;
    float[] qlist, y_vals_n, mul, abs;
    int istart, iend;
    if (ds.getX_label() != "Q") System.out.println("******ERROR******");
    for (int i = 0; i < ds.getNum_entries(); i++) {
    dt = ds.getData_entry(i);
    attr_list_d = dt.getAttributeList();
    scattering_angle = ((float[])attr_list_d.getAttributeValue(GLADRunInfo.GLAD_PARM))[0];
    qlist = dt.getX_scale().getXs();
    y_vals_n = dt.getY_values();
    istart = 0;
    iend = y_vals_n.length-1;
    
    while (y_vals_n[istart] == 0.0f) {
      istart++;
    }
    while (y_vals_n[iend] == 0.0f) {
      iend--;
    }
      
      for (int k = istart; k < iend; k++){
        q = .5f*(qlist[k]+qlist[k+1]);
        lambda = tof_calc.WavelengthofDiffractometerQ(scattering_angle, q);
//      if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);
        mul = CylMulTof.getCoeff(scattering_angle, lambda);
        abs = CylAbsTof.getCoeff(scattering_angle, lambda);
      
//            if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);
        if (ISEmptyCan) {//outer shells: (can);
          y_vals_n[k] -= mul[1];
//            if (k==0) System.out.println("cell mul[1]: "+mul[1]+" abs[3]: "+abs[3]+" abs[2]: "+abs[2]);     
          y_vals_n[k] /= abs[3];
          y_vals_n[k] *= abs[2];
        }
        else {          //inner core:  calibration rod (vanadim, fused silica), or (sample+can) with (can) part subtracted;
//            if (k==0) System.out.println("core mul[1]"+mul[1]+" abs[1]: "+abs[1]);
          y_vals_n[k] -= mul[1];
          y_vals_n[k] /= abs[1];
          y_vals_n[k] /= MutCross.Nscatterers;
//              if (y_vals_n[k] < y_vals_min) y_vals_min=y_vals_n[k];
    /*          if (y_vals_n[k] > 10.0f || y_vals_n[k] < -10.0f) {
                y_vals_n[k]=0.0f;
                System.out.println("\n"+"abnormal values: "+i+"\n");
              }*/
        }
//            if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);
      } 
    }
        
    ds.setTitle(ds.getTitle()+" "+"--->CORAL/ANALYSE");
//        ds.setY_units("barns");
    ds.setY_label("Time-Of-Flight differential crosssection");
    if(ISEmptyCan) System.out.println("Empty can multiple scattering and attenuation correction applied.");
    else System.out.println("Sample multiple scattering and attenuation correction applied.");
    return ds;
  }

  public static void main(String[] args) {
   
    String test = "Ni 1";
    String[] list_composition = Pattern.compile("[\\s,]+").split(test);
    int nelements = list_composition.length;
    if (nelements%2 != 0) System.out.println("******INPUT ERROR******");
    nelements /= 2;
    String[] list_elements = new String[nelements];
    float[] list_fractions = new float[nelements];
    

    for (int i = 0; i < nelements; i++){
      list_elements[i] = list_composition[2*i];
      list_fractions[i] = (new Float(list_composition[2*i+1])).floatValue();

    }
   
    for (int i = 0; i < nelements; i++){
        
         System.out.println(list_elements[i]);
         System.out.println(list_fractions[i]);
       }
 
  }

}
