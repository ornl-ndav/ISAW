/*
 * File:  GLADQf2R.java
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
 *
 */

package Operators.TOF_Diffractometer;

import Operators.TOF_Diffractometer.Ftr;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Wrappable;
import DataSetTools.operator.IWrappableWithCategoryList;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.FunctionTable;
import DataSetTools.dataset.HistogramTable;
import DataSetTools.dataset.XScale;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;

/**
 * This operator uses Ftr.java to preform the Fourier transformation from S(Q)
 * to various distribution functions. The S(Q) data are read in from an ascii file,
 * which is assumed to have one line header at the top followed by three columns of
 * Q, S(Q), error bar values:
 * _______________________________________________________
 *  start =       0.300    end =      40.000 npt=  1589
 *  0.3000000    -0.3288881E-06 0.1009184E-04
 *  0.3250000    -0.2758914     0.9243053E-02
 *  0.3500001    -0.2692041     0.8844816E-02
 *  0.3750001    -0.2636399     0.8816870E-02
 *  0.4000001    -0.2578393     0.8681898E-02
 *  0.4250001    -0.2508371     0.8442139E-02
 *  ...
 *   39.97514     0.1314703E-01  1.839243
 *   40.00000     0.0000000E+00 0.0000000E+00
 *--------------------------------------------------------
 * 
 */
public class GLADQf2R implements Wrappable, IWrappableWithCategoryList
{
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false; 
  public LoadFileString sofqfile = new LoadFileString();
  public int iwf = 0; //window function flag: 0 = square, 1 = Lorch, 2 = Welch, 3 = Modified Welch, 4 = cosine;
  public boolean doTofR = true;
  public boolean doGofR = true;
  public boolean doNofR = true;
  public boolean doCofR = true;
  public boolean showDofR = true;
  public float NumberDensity = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.EXP.SMP.DENSITY");
  public float RCut = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.RCUT");
  public float QCut = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.QCUT");
  public float QStart = 0.0f;
  public int NUMQ = GLADRunProps.getintKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.NUMQ");
  
  //~ Methods ******************************************************************

  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "GLAD_Qf2R";
  }


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
    
    String soq_file = sofqfile.toString();
    System.out.println("Extracting S(Q) data from "+soq_file+" ...");
    Ftr f = Ftr.parseSOQFile(sofqfile.toString());
    System.out.println("Done.");
    System.out.println("Fourier transform to real space correlation functions...");  
    f.calculateDofR(QStart, QCut, iwf, RCut, NumberDensity);

    
    DataSet ds = new DataSet ("S(Q)", 
       "Construct a dataset holding the S(Q) data.",
       "Angstrom", "R",
       "", "");

  
    Data sofq = new HistogramTable(XScale.getInstance(f.getQ()),
                                       f.getSofQ(),
                                       100000);
    sofq.setLabel("SofQ");
    ds.addData_entry(sofq);
    
    DataSet ds_result = ds;
    ds_result.setTitle("Analysis Result");
    ds_result.setX_units("Angstrom");
    ds_result.setX_label("r");
    ds_result.setY_units("");
    ds_result.setY_label("f(r)");  
    if (doTofR) {
      float tofrs[][];
      tofrs = f.getTofR();
      Data tofr = new FunctionTable(XScale.getInstance(tofrs[0]),
                                        tofrs[1],
                                        100001);
      tofr.setLabel("TofR");                       
      ds_result.addData_entry(tofr);                                  
    }
    
    if (doGofR) {
      float gofrs[][];
      gofrs = f.getGofR();
      Data gofr = new FunctionTable(XScale.getInstance(gofrs[0]),
                                        gofrs[1],
                                        100002);
      gofr.setLabel("GofR");                                  
      ds_result.addData_entry(gofr);                                  
    }
    
    if (doNofR) {
      float nofrs[][];
      nofrs = f.getNofR();
      Data nofr = new FunctionTable(XScale.getInstance(nofrs[0]),
                                        nofrs[1],
                                        100003);
      nofr.setLabel("NofR");
      ds_result.addData_entry(nofr);                                  
    }
    
    if (doCofR) {
      float cofrs[][];
      cofrs = f.getCofR();
      Data cofr = new FunctionTable(XScale.getInstance(cofrs[0]),
                                        cofrs[1],
                                        100004);
      cofr.setLabel("CofR");
      ds_result.addData_entry(cofr);                                  
    }
    
    if (showDofR) {
      float dofrs[][];
      dofrs = f.getDofR();
      Data dofr = new FunctionTable(XScale.getInstance(dofrs[0]),
                                        dofrs[1],
                                        100005);
      dofr.setLabel("DofR");      
      ds_result.addData_entry(dofr);                                  
    }
          
    System.out.println("Done.");

/*    
    for (int i = 0; i<fofrs[0].length; i++){
      if (i<1100) {
//        entry= "["+gofrs[0][i]+","+gofrs[1][i]+"]"+",";
//        output.append(entry);
        System.out.println("r:\t"+fofrs[0][i]+"\t"+"tor:\t"+fofrs[1][i]);
      } 
//             System.out.println("r: "+dors[0][i]+" Dr: "+dors[1][i]);
    }
//    System.out.println(output);     
*/
    return ds_result;
  } 
  
  public static void main(String[] args) {
    
    String fin = "/IPNShome/taoj/tmp/glad/fsilica_soq.txt";
    GLADQf2R testqf2r = new GLADQf2R();
    testqf2r.sofqfile = new LoadFileString(fin);
//    testqf2r.NumberDensity = 0.04610856f;
    
    long t0 = System.currentTimeMillis();
    DataSet ds = (DataSet) testqf2r.calculate();    

    DataSetTools.viewer.ViewManager ds_view = new DataSetTools.viewer.ViewManager(ds, DataSetTools.viewer.IViewManager.IMAGE);

    long t1 = System.currentTimeMillis();
        System.out.println("time: "+(t1-t0)+"ms");     
  
  }   

}


