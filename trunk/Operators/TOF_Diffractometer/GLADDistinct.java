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
 * Revision 1.6  2006/01/05 23:00:45  taoj
 * new code handling the self scattering problem for hydrogenous samples
 *
 * Revision 1.5  2005/10/27 17:58:08  taoj
 * new version
 *
 * Revision 1.4  2005/08/11 20:36:12  taoj
 * new error analysis code
 *
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
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import java.util.Vector;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import gov.anl.ipns.MathTools.Functions.OrthoPolyFit;
import gov.anl.ipns.MathTools.Functions.ChebyshevSum;

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
  public float temperature = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.TEMP");
  public float wmin = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.COMB.MINW");
  public float wmax = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.ANALYSIS.COMB.MAXW");
  public boolean doPLATOM = true;
  public float rho = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.EXP.SMP.DENSITY");
  public int npoly = 3;
  public float qsteph = 0.05f;
  public float qminh = 0.05f;
  public float Rmin;
  

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
    Data dt, dv, dmv;
    AttributeList attr_list_d, attr_list_v;
    float scattering_angle_d, scattering_angle_v, d1, d2, lambda_d, q_d, lambda_v, q_v, p;
    float eout, yv, ev, yvm, evm;
    float tenfactor, ymax;
    float[] Q_vals_d, y_vals_n, Q_vals_v, y_vals_v, e_vals_v, 
            W_vals_vm, Q_vals_vm, 
            y_vals_vm, e_vals_vm,
            y_vals_vmr, e_vals_vmr;
    float[] data_params_d = new float[4], data_params_v = new float[4];
    XScale Q_scale_vm;
    int ngrps, nmonchannel;
    int istart, iend;
    float qk, qmin, qmax, qdif, qfac;
    int n1, n2;
    
    GLADScatter smprun = (GLADScatter)((Object[])ds0.getAttributeValue(GLADRunProps.GLAD_PROP))[2];
    String[] list_elements = smprun.symbol;
    float[] list_fractions = smprun.formula;

    ngrps = dcs_smp.getNum_entries();
    if (smo_van.getNum_entries()!=ngrps) System.out.println("\n***UNEXPECTED ERROR***---dcs and smo have different number of groups***");
    
    dmv = dm_van.getData_entry(0);    
    W_vals_vm = dmv.getX_values();
    y_vals_vm = dmv.getY_values();
    e_vals_vm = dmv.getErrors();
/*
    for (int i = 0; i < e_vals_vm.length; i++) {
      eout = (float)Math.sqrt(e_vals_vm[i]);
      e_vals_vm[i] = eout;
    }
*/    
    nmonchannel = y_vals_vm.length;
    Q_vals_vm = new float[nmonchannel+1];
    arrayUtil.Reverse( y_vals_vm );
    arrayUtil.Reverse( e_vals_vm );
        
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
      e_vals_v = dv.getErrors();
          
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
//      van_dm_Q = Data.getInstance(Q_scale_vm, y_vals_vm, dv.getGroup_ID());
      dmv = Data.getInstance(Q_scale_vm, y_vals_vm, e_vals_vm, dv.getGroup_ID());
      dmv.resample( dv.getX_scale(), IData.SMOOTH_NONE );
      y_vals_vmr = dmv.getY_values();
      e_vals_vmr = dmv.getErrors();
      
      n1 = istart; n2 = iend;      
      for (int k = istart; k <= iend; k++){
        q_d = 0.5f*(Q_vals_d[k]+Q_vals_d[k+1]);
        lambda_d = tof_calc.WavelengthofDiffractometerQ(scattering_angle_d, q_d);        
        if (doPLATOM) {
          p = Platom.plaatom(lambda_d, list_elements, list_fractions, temperature, scattering_angle_d, d1, d2, false); 
          y_vals_n[k] -= p;
        } else {
          if (lambda_d > wmin) n1 = k;
          else n2 = k;
          if (lambda_d < wmax) break;          
        }
      }
      
      if (!doPLATOM) {
        qmin = Q_vals_d[n1];
        qmax = Q_vals_d[n2];
        n1 = (int)Math.ceil((qmax-qmin)/qsteph)+1;
        float[] qhs = new float[n1];
        for (int k = 0; k < n1; k++) qhs[k] = qmin + k*qsteph;
        XScale qhscale = XScale.getInstance(qhs);
        dt.resample( qhscale, IData.SMOOTH_NONE );
        
        n1 = y_vals_n.length;
        double A3[] = OrthoPolyFit.opolyfit_h(Q_vals_d, y_vals_n, npoly+1)[npoly];
        A3[0] /= 2;
        ChebyshevSum ycheby3 = new ChebyshevSum(A3);
        qmin = Q_vals_d[0];
        qmax = Q_vals_d[n1-1];
        for (int k = 0; k < n1; k++) {
          qk = (2*Q_vals_d[k]-qmax-qmin)/(qmax-qmin);
          y_vals_n[k] -= ycheby3.getValue(qk);
        }
        
        float anfac = smprun.bbarsq;
        if (anfac == 0.0f) throw new RuntimeException("!!!!!!sample bbarsq can't be zero!!!!!!");
        float rmax = Rmin;
        float rstep = (float)Math.PI/qmax;
        int nr = Math.min((int)(rmax/rstep), 1000);
        float pifac = (float) (0.5f*Math.PI/qmax);
        float acons = 0.0f;
        n2 = (int)(0.2f/qsteph);
        for (int k = 0; k < n2; k++) acons += y_vals_n[n1-1-k];
        acons /= n2;
        float qs[] = new float[n1];
        float sofq[] = new float[n1];
        float r[] = null, gofr[] = null;
        for (int k = 0; k < n1; k++) {
          qs[k] = 0.5f*(Q_vals_d[k] + Q_vals_d[k+1]);
          qdif = Q_vals_d[k+1] - Q_vals_d[k];
          qfac = qs[k]*pifac;
          sofq[k] = qs[k]*(y_vals_n[k]-acons)*qdif;          
        }
        float trfac = (float)(0.5f/Math.PI/Math.PI/rho);
        float qr, sum;
        if (nr > 0) {
          pifac = (float) (0.5f*Math.PI/(nr*rstep));
          r = new float[nr];
          gofr = new float[nr];
          float rfac, wfac, sinqr;
          for (int j = 0; j < nr; j++) {
            r[j] = (j+1)*rstep;
            rfac = trfac;
            wfac = r[j]*pifac;
            sum = 0.0f;
            for (int k = 0; k < n1; k++) {
              qr = qs[k]*r[j];
              sinqr = (float)Math.sin(qr);
              sum += sofq[k]*sinqr;
            }
            gofr[j] = (float)(rfac*sum*Math.cos(wfac) + r[j]*anfac);
          }                                 
        }
        trfac = (float) (4*Math.PI*rho*rstep);        
        for (int k = 0; k < n1; k++) {
          qfac = trfac/qs[k];
          sum = 0.0f;
          if (nr > 0)
            for (int j = 0; j < nr; j++) {
              qr = qs[k]*r[j];
              sum += gofr[j]*Math.sin(qr);
            }                           
          else sum = 0.0f;
          y_vals_n[k] -= sum*qfac+acons;
        }
        
      }
       
      for (int k = istart; k <= iend; k++){      
        q_v = 0.5f*(Q_vals_v[k]+Q_vals_v[k+1]);
        lambda_v = tof_calc.WavelengthofDiffractometerQ(scattering_angle_v, q_v);            
        if (lambda_v < wmin || lambda_v > wmax) {
          y_vals_v[k] = 0.0f;
//          e_vals_v[k] = 0.0f;
        } 
        else {
          yv = y_vals_v[k];
          ev = e_vals_v[k];
          yvm = y_vals_vmr[k];  
          evm = e_vals_vmr[k];          
          y_vals_v[k] = yv*yvm;
          e_vals_v[k] = (float)(yv*yvm*Math.sqrt(evm*evm/yvm/yvm+ev*ev/yv/yv)); 
        } 
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

  public static void main(String[] args) {
    GLADConfigure testconf = new GLADConfigure();
    testconf.hasCan = false;
    DataSet runinfo = (DataSet)testconf.calculate(); 
    
    GLADCrunch testcrunch = new GLADCrunch();           
    testcrunch.ds0 = runinfo;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8094.run");
    testcrunch.noDeadDetList = true;
    testcrunch.redpar = new LoadFileString("/IPNShome/taoj/GLAD/gladrun.par");
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
    
    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(nrm_smp, DataSetTools.viewer.IViewManager.IMAGE);
    DataSetTools.viewer.ViewManager nrm_van_view = new DataSetTools.viewer.ViewManager(nrm_van, DataSetTools.viewer.IViewManager.IMAGE);

  }

}

