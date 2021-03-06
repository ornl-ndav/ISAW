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
 * Revision 1.7  2006/04/27 22:27:15  taoj
 * Minor changes. Extra code in main() for testing purpose.
 *
 * Revision 1.6  2006/02/04 03:01:08  taoj
 * Unit testing in the main() for a 3 annuli case. It is an equivalent of a corresponding data analysis script.
 *
 * Revision 1.5  2005/10/27 17:57:21  taoj
 * new version
 *
 * Revision 1.4  2005/08/11 20:37:07  taoj
 * new error analysis code
 *
 * Revision 1.3  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.2  2005/01/10 15:36:00  dennis
 * Added getCategoryList method to place operator in menu system.
 *
 * Revision 1.1  2004/07/23 17:45:11  taoj
 * test version.
 *
 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.*;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.XScale;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import java.util.Vector;
import DataSetTools.operator.DataSet.Math.DataSet.*;

/**
 * This class merges differential cross section of the same Q from different
 * data block. It corresponds to the MERGE routine in the ATLAS package.
 */
public class GLADCombine implements Wrappable, IWrappableWithCategoryList {
  //~ Instance fields **********************************************************
  
  /* @param int_smp sample IofQ dataset;
   * @param flx_van weighting function calculated from vanadium in a dataset ;
   */
//  public DataSet ds0;
  public DataSet int_smp;
  public DataSet flx_van;
  public int NUMQ = GLADRunProps.getintKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.NUMQ");
  public float GLADQMAX = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.QMAX");
  
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
    s.append( "@overview This class merges the differential cross section ");
    s.append( "of the same Q from different data blocks. It corresponds ");
    s.append( "to the MERGE routine in the ATLAS package." );
    return s.toString(  );
  }


  
  /**
   * Removes dead detectors from the specified DataSet.
   *
   * @return The crunched DataSet.
   */
  public Object calculate(  ) {
    
//    GLADRunProps runinfo = (GLADRunProps)((Object[])ds0.getAttributeValue(GLADRunProps.GLAD_PROP))[0];
//    int NUMQ = ((Integer)runinfo.ExpConfiguration.get("GLAD.ANALYSIS.NUMQ")).intValue();
//    float GLADQMAX = ((Float)runinfo.ExpConfiguration.get("GLAD.ANALYSIS.QMAX")).floatValue();
    
    System.out.println("Merge data by flux weighting...");
    Data dt, dv, dioq, dsum;
    XScale Q_scale;
    int ndetchannel, numq;
    float[] y_vals_int, e_vals_int, y_vals_flx, e_vals_flx, 
            y_vals_ioq, e_vals_ioq, y_vals_sum, e_vals_sum;
    float yt, et, yv, ev;

    numq = (int)GLADQMAX*NUMQ+1;   
//Q_scale has to be the same as the new_Q_scale used in GLADCrunch (room for better design?);    
    Q_scale = new UniformXScale(1.0f/NUMQ, GLADQMAX+1.0f/NUMQ, numq);
    dioq = Data.getInstance(Q_scale, new float[numq-1], new float[numq-1], 99999);
    dsum = Data.getInstance(Q_scale, new float[numq-1], new float[numq-1], 99998);    
    float scattering_angle;
        
    for (int i = 0; i < flx_van.getNum_entries(); i++){
      dv = flx_van.getData_entry(i);
      dsum = dsum.stitch(dv, Data.SUM);
      dt = int_smp.getData_entry(i);
      y_vals_int = dt.getY_values();
      e_vals_int = dt.getErrors();
      y_vals_flx = dv.getY_values();
      e_vals_flx = dv.getErrors();
      ndetchannel = y_vals_int.length;
      for (int k = 0; k < ndetchannel; k++){

        yt = y_vals_int[k];
//        if (yt > 100) System.out.println("GID: "+dt.getGroup_ID()+" k: "+k+" "+yt);
        et = e_vals_int[k];
        yv = y_vals_flx[k];  
        ev = e_vals_flx[k];         
        y_vals_flx[k] = yv*yt;
        if (yv == 0.0f) e_vals_flx[k] = 0.0f;
        else e_vals_flx[k] = (float)(Math.abs(yv*yt)*Math.sqrt(et*et/yt/yt+ev*ev/yv/yv));
      }
      dioq = dioq.stitch(dv, Data.SUM);
    }    
    
    y_vals_ioq = dioq.getY_values();
    e_vals_ioq = dioq.getErrors();
    y_vals_sum = dsum.getY_values();
    e_vals_sum = dsum.getErrors();    
    for (int i = 0; i < numq-1; i++){
      if ((yv = y_vals_sum[i]) != 0){

        yt = y_vals_ioq[i];
        et = e_vals_ioq[i];
//        yv = y_vals_sum[i];  
        ev = e_vals_sum[i];         
        y_vals_ioq[i] = yt/yv;  
        e_vals_ioq[i] = (float)(Math.abs(yt/yv)*Math.sqrt(et*et/yt/yt+ev*ev/yv/yv));

//        y_vals_ioq[i] /= y_vals_sum[i];
      }
    }
    
    dioq.setLabel("IofQ");
    int_smp.addData_entry(dioq);
    int_smp.setTitle(int_smp.getTitle()+" "+"--->COMBINE");
//    int_smp.setY_label("distinct scattering");
    System.out.println("Done.");
    return int_smp;
  }    
  
  public static void main(String[] args) {    

    GLADConfigure testconf = new GLADConfigure();
    testconf.configfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/smpH.dat");
    testconf.doDebug = true;
    testconf.isawMode = 0;
    DataSet runinfo = (DataSet)testconf.calculate(); 
    
    GLADCrunch testcrunch = new GLADCrunch();           
    testcrunch.ds0 = runinfo;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5298.run");
    testcrunch.noDeadDetList = true;
    testcrunch.redpar = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/gladrun.par");
    Vector monnrm = (Vector) testcrunch.calculate();
    DataSet mon_van = (DataSet) monnrm.get(0); 
    DataSet nrm_van = (DataSet) monnrm.get(1);
    testcrunch.noDeadDetList = false;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5296.run");
    DataSet nrm_can = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5299.run");
    DataSet nrm_smp = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5295.run");
    DataSet nrm_bkg = (DataSet)(((Vector)testcrunch.calculate()).get(1));

    if (new DataSetSubtract(nrm_van, nrm_bkg, false).getResult()
          instanceof ErrorString) throw new RuntimeException("!!!!!!Unexpected Error!!!!!!");
    if (new DataSetSubtract(nrm_can, nrm_bkg, false).getResult()
          instanceof ErrorString) throw new RuntimeException("!!!!!!Unexpected Error!!!!!!");
    if (new DataSetSubtract(nrm_smp, nrm_bkg, false).getResult()
          instanceof ErrorString) throw new RuntimeException("!!!!!!Unexpected Error!!!!!!");
    
    GLADVanCal testvancal = new GLADVanCal();
    testvancal.ds0 = runinfo;
    testvancal.nrm_van = nrm_van;
    testvancal.calculate();
    if (new DataSetDivide(nrm_can, nrm_van, false).getResult()
          instanceof ErrorString) throw new RuntimeException("!!!!!!Unexpected Error!!!!!!");
    if (new DataSetDivide(nrm_smp, nrm_van, false).getResult()
          instanceof ErrorString) throw new RuntimeException("!!!!!!Unexpected Error!!!!!!");

    
    GLADAnalyze testanalyze = new GLADAnalyze();
    testanalyze.ds0 = runinfo;
    testanalyze.ds = nrm_can;
    testanalyze.imask = 2;
    testanalyze.calculate();

    if (new DataSetSubtract(nrm_smp, nrm_can, false).getResult()
          instanceof ErrorString) throw new RuntimeException("!!!!!!Unexpected Error!!!!!!");
    testanalyze.ds = nrm_smp;
    testanalyze.imask = 3;
    testanalyze.calculate();

    GLADDistinct testdistinct = new GLADDistinct();
    testdistinct.ds0 = runinfo;
    testdistinct.dcs_smp = nrm_smp;
    testdistinct.smo_van = nrm_van;
    testdistinct.dm_van = mon_van;
    testdistinct.doPLATOM = false;
    testdistinct.npoly = 3;
//    testdistinct.wmax = 4.0f;
    testdistinct.calculate();
    
    GLADCombine testcombine = new GLADCombine();
    testcombine.int_smp = nrm_smp;
    testcombine.flx_van = nrm_van;
    testcombine.NUMQ = 20;
    testcombine.calculate();    
    
    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(nrm_smp, DataSetTools.viewer.IViewManager.IMAGE);

//"subself"/no-platom test;
/*
    GLADConfigure testconf = new GLADConfigure();
    testconf.configfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/smpH.dat");
    testconf.doDebug = true;
    testconf.isawMode = 0;
    DataSet runinfo = (DataSet)testconf.calculate(); 
    
    GLADCrunch testcrunch = new GLADCrunch();           
    testcrunch.ds0 = runinfo;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5298.run");
    testcrunch.noDeadDetList = true;
    testcrunch.redpar = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/gladrun.par");
    Vector monnrm = (Vector) testcrunch.calculate();
    DataSet mon_van = (DataSet) monnrm.get(0); 
    DataSet nrm_van = (DataSet) monnrm.get(1);
    testcrunch.noDeadDetList = false;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5296.run");
    DataSet nrm_can = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5299.run");
    DataSet nrm_smp = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad5295.run");
    DataSet nrm_bkg = (DataSet)(((Vector)testcrunch.calculate()).get(1));

    new DataSetSubtract(nrm_van, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_can, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_smp, nrm_bkg, false).getResult();
    
    GLADVanCal testvancal = new GLADVanCal();
    testvancal.ds0 = runinfo;
    testvancal.nrm_van = nrm_van;
    testvancal.calculate();
    new DataSetDivide(nrm_can, nrm_van, false).getResult();
    new DataSetDivide(nrm_smp, nrm_van, false).getResult();

    
    GLADAnalyze testanalyze = new GLADAnalyze();
    testanalyze.ds0 = runinfo;
    testanalyze.ds = nrm_can;
    testanalyze.imask = 2;
    testanalyze.calculate();
    new DataSetSubtract(nrm_smp, nrm_can, false).getResult();
    testanalyze.ds = nrm_smp;
    testanalyze.imask = 3;
    testanalyze.calculate();

    GLADDistinct testdistinct = new GLADDistinct();
    testdistinct.ds0 = runinfo;
    testdistinct.dcs_smp = nrm_smp;
    testdistinct.smo_van = nrm_van;
    testdistinct.dm_van = mon_van;
    testdistinct.doPLATOM = false;
    testdistinct.npoly = 0;
    testdistinct.calculate();
    DataSetTools.viewer.ViewManager nrm_van_view = new DataSetTools.viewer.ViewManager(nrm_van, DataSetTools.viewer.IViewManager.IMAGE);
    
    GLADCombine testcombine = new GLADCombine();
    testcombine.int_smp = nrm_smp;
    testcombine.flx_van = nrm_van;
    testcombine.NUMQ = 20;
    testcombine.calculate();    
    
    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(nrm_smp, DataSetTools.viewer.IViewManager.IMAGE);

/*
//HDO, flat plate
    GLADConfigure testconf = new GLADConfigure();
    testconf.configfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/fpHDO.dat");
    testconf.doDebug = true;
    testconf.isawMode = 0;
    testconf.isFP = true;
    DataSet runinfo = (DataSet)testconf.calculate(); 
    
    GLADCrunch testcrunch = new GLADCrunch();           
    testcrunch.ds0 = runinfo;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad9733.run");
//    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8094.run");
    testcrunch.noDeadDetList = true;
    testcrunch.redpar = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/gladrun.par");
    Vector monnrm = (Vector) testcrunch.calculate();
    DataSet mon_van = (DataSet) monnrm.get(0); 
    DataSet nrm_van = (DataSet) monnrm.get(1);
    testcrunch.noDeadDetList = false;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad9740.run");
    DataSet nrm_can = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad9735.run");
//    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8095.run");
    DataSet nrm_smp = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad9734.run");
//    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8093.run");
    DataSet nrm_bkg = (DataSet)(((Vector)testcrunch.calculate()).get(1));

//    DataSetTools.viewer.ViewManager nrm_can_view = new DataSetTools.viewer.ViewManager(nrm_can, DataSetTools.viewer.IViewManager.IMAGE);
//    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(nrm_smp, DataSetTools.viewer.IViewManager.IMAGE);

    new DataSetSubtract(nrm_van, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_can, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_smp, nrm_bkg, false).getResult();
    
    GLADVanCal testvancal = new GLADVanCal();
    testvancal.ds0 = runinfo;
//    testvancal.DOsmooth = true;
    testvancal.nrm_van = nrm_van;
    testvancal.calculate();
    new DataSetDivide(nrm_can, nrm_van, false).getResult();
    new DataSetDivide(nrm_smp, nrm_van, false).getResult();

    
    GLADAnalyze testanalyze = new GLADAnalyze();
    testanalyze.ds0 = runinfo;

    testanalyze.ds = nrm_can;
    testanalyze.imask = 2;
    testanalyze.calculate();
    new DataSetSubtract(nrm_smp, nrm_can, false).getResult();

    testanalyze.ds = nrm_smp;
    testanalyze.imask = 3;
    testanalyze.calculate();

    GLADDistinct testdistinct = new GLADDistinct();
    testdistinct.ds0 = runinfo;
    testdistinct.dcs_smp = nrm_smp;
    testdistinct.smo_van = nrm_van;
    testdistinct.dm_van = mon_van;
    testdistinct.doPLATOM = false;
    testdistinct.wmin = 0.48f;
    testdistinct.wmax = 0.52f;
    testdistinct.calculate();
    
    GLADCombine testcombine = new GLADCombine();
    testcombine.int_smp = nrm_smp;
    testcombine.flx_van = nrm_van;
//    testcombine.NUMQ = 20;
    testcombine.calculate();
    
    GLADQ2R testq2r = new GLADQ2R();
    testq2r.ds0 = runinfo;
    testq2r.ioq_smp = nrm_smp;
    testq2r.calculate();
    
    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(nrm_smp, DataSetTools.viewer.IViewManager.IMAGE);


/* 3 annuli;
    GLADConfigure testconf = new GLADConfigure();
    testconf.configfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/smptubecan.dat");
    testconf.hasFur = true;
    testconf.isawMode = 0;
    testconf.doDebug = true;
    DataSet runinfo = (DataSet)testconf.calculate(); 
    
    GLADCrunch testcrunch = new GLADCrunch();           
    testcrunch.ds0 = runinfo;
    testcrunch.runfile = new LoadFileString("/IPNShome/glad/data/glad10088.run");
    testcrunch.noDeadDetList = true;
    testcrunch.redpar = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/gladrun10068.par");
    Vector monnrm = (Vector) testcrunch.calculate();
    DataSet mon_van = (DataSet) monnrm.get(0); 
    DataSet nrm_van = (DataSet) monnrm.get(1);

    testcrunch.runfile = new LoadFileString("/IPNShome/glad/data/glad10100.run");
    testcrunch.noDeadDetList = false;
    DataSet nrm_smp = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    
    testcrunch.runfile = new LoadFileString("/IPNShome/glad/data/glad10068.run");
    testcrunch.noDeadDetList = false;
    DataSet nrm_can = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    
    testcrunch.runfile = new LoadFileString("/IPNShome/glad/data/glad10107.run");
    testcrunch.noDeadDetList = false;
    DataSet nrm_fur = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    
    testcrunch.runfile = new LoadFileString("/IPNShome/glad/data/glad10091.run");        
    testcrunch.noDeadDetList = false;
    DataSet nrm_bkg = (DataSet)(((Vector)testcrunch.calculate()).get(1));

    new DataSetSubtract(nrm_van, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_smp, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_can, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_fur, nrm_bkg, false).getResult();
    
    GLADVanCal testvancal = new GLADVanCal();
    testvancal.ds0 = runinfo;
//    testvancal.DOsmooth = true;
    testvancal.nrm_van = nrm_van;
    testvancal.calculate();
    new DataSetDivide(nrm_smp, nrm_van, false).getResult();
    new DataSetDivide(nrm_can, nrm_van, false).getResult();
    new DataSetDivide(nrm_fur, nrm_van, false).getResult();
    
    GLADAnalyze testanalyze = new GLADAnalyze();
    testanalyze.ds0 = runinfo;
    testanalyze.ds = nrm_fur;
    testanalyze.imask = 4;
    testanalyze.calculate();
    new DataSetSubtract(nrm_smp, nrm_fur, false).getResult();
    testanalyze.ds = nrm_can;
    testanalyze.imask = 6;
    testanalyze.calculate();
    new DataSetSubtract(nrm_smp, nrm_can, false).getResult();
    testanalyze.ds = nrm_smp;
    testanalyze.imask = 7;
    testanalyze.calculate();        

    GLADDistinct testdistinct = new GLADDistinct();
    testdistinct.ds0 = runinfo;
    testdistinct.dcs_smp = nrm_smp;
    testdistinct.smo_van = nrm_van;
    testdistinct.dm_van = mon_van;
    testdistinct.calculate();
    
    GLADCombine testcombine = new GLADCombine();
    testcombine.int_smp = nrm_smp;
    testcombine.flx_van = nrm_van;
    testcombine.calculate();
    
    GLADQ2R testq2r = new GLADQ2R();
    testq2r.ds0 = runinfo;
    testq2r.ioq_smp = nrm_smp;
    testq2r.calculate();
    
    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(nrm_smp, DataSetTools.viewer.IViewManager.IMAGE);
*/

/*
//fused silica rod standard test
    long start = System.currentTimeMillis(); 
  
    GLADConfigure testconf = new GLADConfigure();
    testconf.hasCan = false;
    testconf.doDebug = true;
    testconf.isawMode = 0;
    DataSet runinfo = (DataSet)testconf.calculate(); 
    
    GLADCrunch testcrunch = new GLADCrunch();           
    testcrunch.ds0 = runinfo;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8094.run");
    testcrunch.noDeadDetList = true;
    testcrunch.redpar = new LoadFileString("/IPNShome/taoj/cvs/ISAW/InstrumentInfo/IPNS/gladrun.par");
    Vector monnrm = (Vector) testcrunch.calculate();
    DataSet mon_van = (DataSet) monnrm.get(0); 
    DataSet nrm_van = (DataSet) monnrm.get(1);

    testcrunch.noDeadDetList = false;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8095.run");
    DataSet nrm_smp = (DataSet)(((Vector)testcrunch.calculate()).get(1));
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8093.run");
    DataSet nrm_bkg = (DataSet)(((Vector)testcrunch.calculate()).get(1));

    new DataSetSubtract(nrm_van, nrm_bkg, false).getResult();
    new DataSetSubtract(nrm_smp, nrm_bkg, false).getResult();
    
    GLADVanCal testvancal = new GLADVanCal();
    testvancal.ds0 = runinfo;
//    testvancal.DOsmooth = true;
    testvancal.nrm_van = nrm_van;
    testvancal.calculate();
    new DataSetDivide(nrm_smp, nrm_van, false).getResult();
    
    GLADAnalyze testanalyze = new GLADAnalyze();
    testanalyze.ds0 = runinfo;
    testanalyze.ds = nrm_smp;
    testanalyze.imask = 1;
    testanalyze.calculate();

    GLADDistinct testdistinct = new GLADDistinct();
    testdistinct.ds0 = runinfo;
    testdistinct.dcs_smp = nrm_smp;
    testdistinct.smo_van = nrm_van;
    testdistinct.dm_van = mon_van;
    testdistinct.calculate();
    
    GLADCombine testcombine = new GLADCombine();
    testcombine.int_smp = nrm_smp;
    testcombine.flx_van = nrm_van;
    testcombine.calculate();
    
    GLADQ2R testq2r = new GLADQ2R();
    testq2r.ds0 = runinfo;
    testq2r.ioq_smp = nrm_smp;
    testq2r.iwf = 1;
    DataSet ds_result = (DataSet)testq2r.calculate();

    DataSetTools.viewer.ViewManager nrm_van_view = new DataSetTools.viewer.ViewManager(nrm_van, DataSetTools.viewer.IViewManager.IMAGE);    
    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(nrm_smp, DataSetTools.viewer.IViewManager.IMAGE);
//  DataSetTools.viewer.ViewManager ds_result_view = new DataSetTools.viewer.ViewManager(ds_result, DataSetTools.viewer.IViewManager.IMAGE);
    start -= System.currentTimeMillis(); 
    System.out.println("running time (s): "+(-start));
*/
  }

}


