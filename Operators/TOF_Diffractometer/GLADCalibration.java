/*
 * File:  GLADCalibration.java
 *
 * Copyright (C) 2005 J. Tao
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
 * Revision 1.2  2005/12/15 20:52:56  dennis
 * Added tag for CVS logging so that future modifications can be tracked.
 *
 *
 * test version.
 *
 */

package Operators.TOF_Diffractometer;

import DataSetTools.operator.Operator;
import DataSetTools.operator.Wrappable;
import DataSetTools.operator.IWrappableWithCategoryList;
import DataSetTools.dataset.DataSet;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import gov.anl.ipns.Util.SpecialStrings.SaveFileString;
import Operators.Special.FitGaussianPeak;
import gov.anl.ipns.Util.File.TextFileReader;
import java.util.Vector;
import java.io.*;
import DataSetTools.math.tof_calc;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import DataSetTools.retriever.RunfileRetriever;
import IPNS.Runfile.Runfile;
import IPNS.Runfile.InstrumentType;

/**
 * This class carries out detector TOF distances calibration.
 */
public class GLADCalibration implements Wrappable, IWrappableWithCategoryList
{
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
  private float d0 = 10.5f; //moderator to sample distance;
  private float dspacing0 = 2.038f; //Ni (111) peak, fcc a = 3.520 angstrom;
  public LoadFileString calibfile0 = new LoadFileString();
  public SaveFileString calibfile1 = new SaveFileString();
  public DataSet calibds;
  
  //~ Methods ******************************************************************

  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "GLAD_CALIBRATE";
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
    s.append( "@overview This operator carries out GLAD detector TOF distance " );
    s.append( "calibration.\n" );
    s.append( "@assumptions Data groups are independent from each other.\n" );
    s.append( "@algorithm Variation of distances with fixed angle values\n" );
    s.append( "@param ds0 \n" );
    s.append( "@param smp_\n" );
    s.append( "@return calibrated distance values in an array indexed by GID.\n " );
    s.append( "@error \n" );

    return s.toString(  );
  }


  
  /**
   * TOF distance calibration through peak fitting.
   *
   * @return The new ds0.
   */
  public Object calculate(  ) {    
    
    int gid;
    float tthd, distd0, distd1, min_x, max_x;
    float fit_x = Float.NaN, chisq = Float.NaN;
    Object params;
    String re = "";
    
    FitGaussianPeak pfer = new FitGaussianPeak();
    pfer.data_set = calibds;
    
    try{

      TextFileReader fr = new TextFileReader( calibfile0.toString() );
      PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(calibfile1.toString())), true);
      fw.println("#gid tthd     distd0    distd1     min_x     max_x\n");
                  
      while ( !fr.end_of_data() ) {
        fr.SkipLinesStartingWith("#");
        gid = fr.read_int();
        tthd = fr.read_float();
        distd0 = fr.read_float();
        distd1 = fr.read_float();
        min_x = fr.read_float();
        max_x = fr.read_float();
        fr.read_float();
        
        pfer.group_id = gid;
        pfer.min_x = min_x;
        pfer.max_x = max_x;
        params = pfer.calculate();
        if (ErrorString.class.isInstance(params)) re += "GID "+gid+": "+params+"\n";
        else {
          chisq = ((Float)((Vector)params).get(1)).floatValue();
          fit_x = ((Float)((Vector)params).get(3)).floatValue();
          distd1 = tof_calc.VelocityFromWavelength(2*dspacing0*(float)Math.sin(tthd*(float)Math.PI/360))*fit_x - d0;          
          fw.println(gid+"\t"+tthd+"\t"+distd0+"\t"+distd1+"\t"+min_x+"\t"+max_x+"\t"+chisq);
          System.out.println(gid+"\t"+tthd+"\t"+distd0+"\t"+distd1+"\t"+min_x+"\t"+max_x+"\t"+chisq);
        }
                     
      }
      
      fw.close();
      if (fw.checkError()) re += "Error in writing " + calibfile1;

    }
    catch ( Exception e ) {
      System.out.println("Exception in reading file " + calibfile0 + e );
      e.printStackTrace();
    }
    
    if (re.equals("")) re = "Sucess";
    System.out.println("Errors: "+re);
    return re;
  }
  
  public static void main(String[] args) {
    
    String file_name = "/IPNShome/taoj/cvs/ISAW/SampleRuns/glad9027.run";
    Runfile rf = null;

    try {
      rf = new Runfile(file_name);
       
      int instrument_type  = rf.InstrumentType();
      if ( instrument_type != InstrumentType.TOF_DIFFRACTOMETER )
        System.out.println("***UNEXPECTED ERROR***---runfile type is not TOF_DIFFRACTOMETER");
                
      if (rf == null) System.out.println("runfile is null;");
      System.out.println("runfile title: "+rf.RunTitle());
      System.out.println("# of histograms: "+rf.NumOfHistograms()+"\n"); 
    }
    catch (Exception e) {
    System.out.println("runfile reading error.");
    System.out.println("exception is "+e);
    e.printStackTrace();
    }      
    
    RunfileRetriever rr = new RunfileRetriever( file_name );
//    DataSet dm = rr.getDataSet(0);    //DataSet 0 are monitor spectra: 0 is beam monitor, 1 the transmission monitor, 2 the proton on target
//    DataSet ds = rr.getDataSet(1);     //DataSet 1 are the detector data;

    
    GLADCalibration gladcalib = new GLADCalibration();
    gladcalib.calibfile0 = new LoadFileString("/IPNShome/taoj/cvs/ISAW/calibf0");
    gladcalib.calibfile1 = new SaveFileString("/IPNShome/taoj/cvs/ISAW/calibf1");
    gladcalib.calibds = rr.getDataSet(1);
    gladcalib.calculate();    
  }

}


