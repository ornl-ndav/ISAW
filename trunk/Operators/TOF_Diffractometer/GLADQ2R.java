/*
 * File:  GLADQ2R.java
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
 *
 * Modified:
 * $Log$
 * Revision 1.5  2006/04/27 22:35:08  taoj
 * Output other real space functions and pack them into a seperate data set.
 *
 * Revision 1.4  2005/11/21 19:11:19  taoj
 * rewritten
 *
 * Revision 1.3  2005/10/27 17:56:54  taoj
 * new version
 *
 * Revision 1.2  2005/08/25 18:14:43  dennis
 * Moved to menu category Instrument Type, TOF_NGLAD
 *
 * Revision 1.1  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.1  2005/02/15 17:45:11  taoj
 * test version.
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

/**
 * This class uses Ftr.java to convert I(Q) to S(Q) and preform the Fourier transformation from S(Q)
 * to various distribution functions. It corresponds to the FTR routine on GLAD.
 */
public class GLADQ2R implements Wrappable, IWrappableWithCategoryList
{
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false; 
  /* @param ioq_smp sample IofQ dataset;
   * 
   */
  public DataSet ds0;
  public DataSet ioq_smp;
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
    return "GLAD_Q2R";
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
    
    System.out.println("Extracting IofQ...");
    GLADScatter smprun = (GLADScatter)((Object[])ds0.getAttributeValue(GLADRunProps.GLAD_PROP))[2]; 
    System.out.println("Done.");
    System.out.println("Convert IofQ to SofQ...");
    float bbarsq = smprun.bbarsq;
    if (NumberDensity != 0.0f) smprun.density = NumberDensity;
    Data dioq = ioq_smp.getData_entry(ioq_smp.getNum_entries()-1);
    Ftr f = new Ftr(dioq, bbarsq, NUMQ);
    System.out.println("Done.");
    System.out.println("Fourier transform to real space correlation functions...");  
    f.calculateDofR(QStart, QCut, iwf, RCut, smprun.density);

/*    
    DataSet ds = new DataSet ("DS", 
       "Construct a dataset holding the distribution functions.",
       "1/Angstrom", "Q",
       "", "");
*/
  
    Data sofq = new HistogramTable(XScale.getInstance(f.getQ()),
                                       f.getSofQ(),
                                       100000);
    sofq.setLabel("SofQ");
    ioq_smp.addData_entry(sofq);
    
    DataSet ds_result = ioq_smp.empty_clone();
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

}


