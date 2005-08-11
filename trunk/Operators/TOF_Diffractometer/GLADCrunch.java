/*
 * File:  GLADCrunch.java
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
 * Revision 1.4  2005/08/11 20:34:54  taoj
 * Use the DataGrid to set up detector information & new error analysis code
 *
 * Revision 1.3  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.2  2005/01/10 15:35:59  dennis
 * Added getCategoryList method to place operator in menu system.
 *
 * Revision 1.1  2004/07/23 17:42:46  taoj
 * test version.
 *
 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.*;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import DataSetTools.math.tof_calc;
import java.io.IOException;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Float1DAttribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.TabulatedData;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IData;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.VariableXScale;
import DataSetTools.dataset.XScale;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.util.FilenameUtil;
import gov.anl.ipns.Util.Sys.StringUtil;
import gov.anl.ipns.Util.Numeric.arrayUtil;
import IPNS.Runfile.InstrumentType;
import IPNS.Runfile.Runfile;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import java.util.ArrayList;
import java.util.Vector;
import Operators.Special.LowPassFilterDS0;
import DataSetTools.viewer.ViewManager;
import DataSetTools.viewer.IViewManager;
import Operators.Generic.Load.LoadUtil;

/**
 * This class preforms deadtime and delayed neutron corrections, calculates 
 * detector efficiency, normalizes detector counts to beam monitor counts, 
 * and convert TOF to wavelength then rebin data to Q, i.e., the first
 * step of the S(Q) data analysis that derives normalized counts in Q from 
 * IPNS run files. It corresponds to the
 * CRUNCH routine in th 1999 GLAD manual;
 */
public class GLADCrunch implements Wrappable, IWrappableWithCategoryList { 
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
  /* @param runfile use ISAW GUI to input the absolute path of the runfile;
   * @param redpar GLAD bad detector list file;
   */
  public DataSet ds0;
  public LoadFileString runfile = new LoadFileString();
  public boolean noDeadDetList = false;
  public LoadFileString redpar = new LoadFileString();
  public float lcutoff = 200.0f;
  public boolean MonSmoothing = false;
  public boolean DetSmoothing = false;
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
    return "GLAD_CRUNCH";                      //operator name used in ISAW scripts;
  }

  /**
   * Returns the documentation for this method as a String.  The format follows
   * standard JavaDoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer( "" );
    s.append( "@overview This operator accepts an IPNS run file, performs deadtime " );
    s.append( "and delayed neutron corrections, calculates detector efficiency, normalizes " );
    s.append( "detector counts to beam monitor counts, and converts TOF to wavelength then " );
    s.append("rebin data to Q. It also extracts a vanadium calibration run's beam monitor spectrum.\n");
    s.append( "@assumptions The specified runfile exists and is valid.\n" );
    s.append( "@algorithm Please refer to the comments in the body of source codes.\n" );
    s.append( "@param runfile runfile.\n" );
    s.append( "@param ISvan It is a vanadium calibration run or not.\n" );
    s.append( "@param redpar Bad detector list file");
    s.append( "@return DataSet of normalized counts in Q\n" );
    s.append( "@error ...\n" );

    return s.toString(  );
  }

//delayed neutron correction routine, a porting of IPNS gdel_neut.for code;
//constant time binning assumed;
  public static float gdel_neut (float t[], float y[], float tof_length, float dn_fraction, float source_period, int nfit) {
    
    int npts = y.length;    

    if (t.length != npts+1) {
      System.out.println("***ERROR***: data is not a histogram"+" x.length: "+t.length+" y.length: "+y.length);
      return 0.0f;
    }

    if (nfit > npts) {
      System.out.println("***ERROR***: nfit > npts");
      return 0.0f;
    }
    
    float channel_width = t[1]-t[0];
    float sum = 0.0f, ssum = 0.0f;
    for (int i = 0; i < npts; i++) {
      ssum += y[i];
      if (i == nfit-1) sum = ssum;     
    }

//    if (t.length == 14951 ) System.out.println("sum of first 20 channels is: "+sum);
    ssum += sum/channel_width/nfit*t[0];
    
    ssum *= dn_fraction*channel_width/source_period;
//    if (t.length == 14951 ) System.out.println("corrected sum: "+sum+" channel_width: "+channel_width+" source period: "+source_period);
    return ssum;
  }

  public static float[] tvals2wvals (float[] tvals, float tof_length){
    int nw = tvals.length-1;
    float[] wvals = new float[nw];
    for (int i = 0; i < nw; i++){
      wvals[i] = .5f*(tof_calc.Wavelength(tof_length, tvals[i])+tof_calc.Wavelength(tof_length, tvals[i+1]));
    }
    return wvals;
  }

//detector efficiency calculation, a porting of geffcyl.for code based on the method by J.M. Carpenter (refer to IPNS note 17);
  public static float[] geffcyl (GLADRunProps run, float[] lambdas, float psi) {
 
    float facd = ((Float) run.ExpConfiguration.get("GLAD.DET.FACD")).floatValue();    
    int nw = lambdas.length; //# of wavelength;
    float[] effd = new float[nw];
       
    double sigma_lim = 10.0f, sigma, sum, u, v;
    int mmax;
    double c0=0.0, c1=1.0, cv2=0.5, PI_cv2=Math.PI*cv2, C_DEG_RAD=1.7453293e-2;
    double[] Y = {0.1000000000000e01, 0.00000000000000e0, 0.403796460925e-01,
                            0.440394547446e-02, 0.949488143763e-03, 0.122813415699e-03,
                            0.157663640377e-04, 0.172571309815e-05, 0.172654155389e-06,
                            0.156412040096e-07, 0.130153437081e-08, 0.999622854450e-10,
                            0.713210170391e-11, 0.475015309979e-12, 0.296650966771e-13,
                            0.174384792757e-14, 0.968261140543e-16, 0.509366791498e-17,
                            0.254578257147e-18, 0.121184326883e-19, 0.550665620396e-21,
                            0.239354077396e-22, 0.997066793809e-24, 0.398740830930e-25,
                            0.153332352146e-26, 0.567799039551e-28};
    
    double sl = facd/Math.cos(psi); //"effective" radius  
//    double sigma_max = sl*.5*(tof_calc.Wavelength(tof_length, t[ns-1])+tof_calc.Wavelength(tof_length, t[ns]));
    double sigma_max = lambdas[nw-1];
    if (sigma_max > sigma_lim) System.out.println("***ERROR***: SIGMAR out of range in detector efficient calculation");
    for (int i = 0; i < nw; i++){
//      sigma = sl*.5*(tof_calc.Wavelength(tof_length, t[i])+tof_calc.Wavelength(tof_length, t[i+1]));
      sigma = sl*lambdas[i];
      u = sigma*PI_cv2;
      sum = c0;
      v = c1;
      mmax = (int)(7.0+1.8*sigma);
      
      for (int j =0; j <= mmax; j++){
        sum += Y[j]*v;
        v *= u;
      }
      effd[i] = (float)(c1-Math.exp(-u)*sum);
    }
      
    return effd;
  }

  public static void correctDeadTime(GLADRunProps runinfo, DataSet ds) {
    
    float npulses = ((Float)ds.getAttributeValue(Attribute.NUMBER_OF_PULSES)).floatValue();
    int ndata = ds.getNum_entries();
    String ds_type = (String)(ds.getAttributeValue(Attribute.DS_TYPE));
//    int NLPSD = ((Integer)runinfo.ExpConfiguration.get("GLAD.DET.NLPSD")).intValue(); 
    
    if (ds_type.equals("Monitor Data")) {
      System.out.println("\nMonitor data dead time correction...");
      float TAU_MON = ((Float)runinfo.ExpConfiguration.get("GLAD.MON.TAU")).floatValue();
      Data dm = ds.getData_entry(0);
      float t_vals_m[] = dm.getX_scale().getXs(),
            y_vals_m[] = dm.getY_values(),
            monchannelwidth = t_vals_m[1]-t_vals_m[0];

      int nmonchannel = y_vals_m.length;      
      float monChannelCorrectionCoeff[] = new float[nmonchannel],
            sumcount_m = TAU_MON/monchannelwidth;
      int ntau = (int)Math.floor(sumcount_m);
      float ftau = sumcount_m - (float)ntau;
      if (ntau < 1) System.out.println("***Unexpected error: ntau < 1***");
       
      for (int k = ntau+1; k < nmonchannel; k++){
        sumcount_m = y_vals_m[k];
        for (int j = 0; j < ntau; j++) {
          sumcount_m += y_vals_m[k-j-1];
        }
        sumcount_m += ftau*y_vals_m[k-ntau-1];
        monChannelCorrectionCoeff[k] = 1.0f/(1-TAU_MON*sumcount_m/npulses/monchannelwidth);
      }      
      for (int k = 0; k < ntau+1; k++) {
        monChannelCorrectionCoeff[k] = 1.0f;
      }

      ((TabulatedData)dm).setErrors(y_vals_m); //storing raw counts into the errors[];
      for (int k = 0; k < nmonchannel; k++){
        y_vals_m[k] *= monChannelCorrectionCoeff[k];
      }
      
      System.out.println("Done.\n");
    }
    
    else if (ds_type.equals("Sample Data")) {
      System.out.println("\nDetector data dead time correction...");
      float TAU_DET = ((Float)runinfo.ExpConfiguration.get("GLAD.DET.TAU")).floatValue();
      Data dt;
      int dtid, dtids[];

      int ndt, ngid = runinfo.gridID2dataID.length;
      int ndetchannel, ntau;
      float ftau, lpsd_channel_counts[], sumcount, detchannelwidth, 
            t_vals_d[], y_vals_d[];

      for (int i = 0; i < ngid; i++) {
        dtids = runinfo.gridID2dataID[i];
        if (dtids != null) {
          ndt = dtids.length;
          dt = ds.getData_entry(runinfo.dataID2index[dtids[0]]);
          t_vals_d = dt.getX_scale().getXs();
          y_vals_d = dt.getY_values();
          ndetchannel = y_vals_d.length;
          lpsd_channel_counts = new float[ndetchannel];
          detchannelwidth = t_vals_d[1] - t_vals_d[0];
          sumcount = TAU_DET/detchannelwidth;
          ntau = (int)Math.floor(sumcount);
          ftau = sumcount - (float)ntau;
          if (ntau < 1) System.out.println("***Unexpected error: ntau < 1***");
                    
          for (int k = 0; k < ndetchannel; k++){ 
            sumcount = 0.0f;           
            for (int m = 0; m < ndt; m++) {
              dt = ds.getData_entry(runinfo.dataID2index[dtids[m]]);
              y_vals_d = dt.getY_values();
              sumcount += y_vals_d[k];
              if (k > ntau) {
                for (int l = 0; l < ntau; l++) sumcount += y_vals_d[k-l-1];                                
                sumcount += ftau*y_vals_d[k-ntau-1];  
              }                          
            }                          
            lpsd_channel_counts[k] = sumcount;                                  
          }
            
          for (int m = 0; m < ndt; m++){
            dt = ds.getData_entry(runinfo.dataID2index[dtids[m]]);
            y_vals_d = dt.getY_values();
            ((TabulatedData)dt).setErrors(y_vals_d);
            for (int k = 0; k < ndetchannel; k++){
              y_vals_d[k] *= 1/(1-TAU_DET*lpsd_channel_counts[k]/npulses/detchannelwidth);
            }             
          }
        }
      }
      System.out.println("Done.\n");            

      int nbadgrp;
      StringBuffer removedDataIDList = new StringBuffer();
      //removed dead detector data groups;
      nbadgrp = runinfo.removedDataID.length;
      for (int i = 0; i < nbadgrp; i++){
        removedDataIDList.append(runinfo.removedDataID[i] + " ");
        ds.removeData_entry_with_id(runinfo.removedDataID[i]);        
      }
      System.out.println("Remove data with ID:\n"+removedDataIDList);    
      System.out.println("Use "+(ndata-nbadgrp)+" data groups from this runfile.\n");
      
    }
    
    else System.out.println("***UNEXPECTED ERROR***---dataset is neither \"Monitor Data\" nor \"Sample Data\"");
     
  }

  public static void correctDeadTime0(GLADRunProps runinfo, DataSet ds, boolean needList, float lcutoff) {
    
    float npulses = ((Float)ds.getAttributeValue(Attribute.NUMBER_OF_PULSES)).floatValue();
    int ndata = ds.getNum_entries();
    int nmonchannel, ndetchannel;
    String ds_type = (String)(ds.getAttributeValue(Attribute.DS_TYPE));

    int NMONCHANNEL = ((Integer)runinfo.ExpConfiguration.get("GLAD.MON.NCHANNEL")).intValue();
    int NDETCHANNEL = ((Integer)runinfo.ExpConfiguration.get("GLAD.DET.NCHANNEL")).intValue();
    int NLPSDID = ((Integer)runinfo.ExpConfiguration.get("GLAD.DET.MAXLPSDID")).intValue();
    int NLPSD = ((Integer)runinfo.ExpConfiguration.get("GLAD.DET.NLPSD")).intValue(); 
    int NSEGMENTS = ((Integer)runinfo.ExpConfiguration.get("GLAD.DET.NSEGMENT")).intValue();
    float TAU_MON = ((Float)runinfo.ExpConfiguration.get("GLAD.MON.TAU")).floatValue();
    float TAU_DET = ((Float)runinfo.ExpConfiguration.get("GLAD.DET.TAU")).floatValue();
    
    if (ds_type.equals("Monitor Data")) {
      System.out.println("\nmonitor data dead time correction...");
      Data dm = ds.getData_entry(0);
      float[] t_vals_m = dm.getX_scale().getXs();
      float[] y_vals_m = dm.getY_values();
      nmonchannel = y_vals_m.length;
      float monchannelwidth = t_vals_m[1]-t_vals_m[0];
      
      if (nmonchannel != NMONCHANNEL) System.out.println("**WARNING**: nmonchannel not equal to "+NMONCHANNEL);
      float[] monChannelCorrectionCoeff = new float[nmonchannel];
      float sumcount_m;
       
      for (int k = 2; k < nmonchannel; k++){
//        if (k%1000 == 1) System.out.println("monitor channel "+k+": "+dm.getY_values()[k]);     
       sumcount_m = y_vals_m[k]+y_vals_m[k-1]+y_vals_m[k-2];
//        sumcount_m = y_vals_m[k];
        monChannelCorrectionCoeff[k] = 1.0f/(1-TAU_MON*sumcount_m/npulses/monchannelwidth);
//        if (k < 100) System.out.println("monitor channel: "+k+" counts: "+y_vals_m[k]+" "+y_vals_m[k-1]+" "+y_vals_m[k-2]+" sumcount_m: "+sumcount_m+" coeff: "+monChannelCorrectionCoeff[k]);        
      }
      
      monChannelCorrectionCoeff[0] = 1.0f;
      monChannelCorrectionCoeff[1] = 1.0f;
      ((TabulatedData)dm).setErrors(y_vals_m); //storing raw counts into the errors[];
      for (int k = 0; k < nmonchannel; k++){
        y_vals_m[k] *= monChannelCorrectionCoeff[k];
      }
      
      System.out.println("Done.\n");
    }
    
    else if (ds_type.equals("Sample Data")) {
      System.out.println("\ndetector data dead time correction...");
      Data dt;
      int[] spectrumID2lpsdID = new int[ndata], detIDs;
      int[][] lpsdID2spectrumID = new int[NLPSDID][NSEGMENTS]; 
      float[][] lpsdChannelCounts = new float[NLPSDID][NDETCHANNEL];
//      float[][] dataChannelCorrectionCoeff = new float[ndata][GLADRunInfo.NDETCHANNEL];
      float[] detChannelCorrectionCoeff;
//      float[] lpsdTotalCounts = new float[GLADRunInfo.NLPSDID];
      float sumcount_d;
      int idet, gid;

/*      
      //setup the GLAD detector mapping table from bank/det number to the lpsd index;
      if (runinfo.BankDet2lpsdID == null){
        try {
          GLADRunInfo.SetupDets(GLADRunInfo.GLADDetsTable);
        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
      }
*/      
      //read in the bad detector list;
/*//      if(GLADRunInfo.LPSDBad == null){ run this for each GLADCrunch operation;
        try {
          GLADRunInfo.SetupDets(GLADRunInfo.GLADDetsTable);
          getBadLPSD(redpar.toString());
        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
//      }
*/

      
      for (int i = 0; i < ndata; i++){
        dt = ds.getData_entry(i);
        gid = dt.getGroup_ID();
        detIDs = (int[])dt.getAttributeValue(Attribute.DETECTOR_IDS);
        idet = detIDs[0];
        spectrumID2lpsdID[i] = idet/64; //lpsdID starting at 0;
        lpsdID2spectrumID[idet/64][idet%64] = i+1; //spectrumID starting at 1 to differentiate from empty elements of the 2-D array;
      }

      int index;
      float[] t_vals_d, y_vals_d;
      float detchannelwidth;
      for (int i = 0; i < NLPSDID; i++){
        for (int j = 0; j < NSEGMENTS; j++){
          index = lpsdID2spectrumID[i][j];
          if (index != 0) {
            dt = ds.getData_entry(index-1);
            y_vals_d = dt.getY_values();
            for (int k = 2; k < dt.getY_values().length; k++){
            sumcount_d = y_vals_d[k]+y_vals_d[k-1]+3/5*y_vals_d[k-2]; //counts that induce deadtime in one time bin, hardcoded;
            lpsdChannelCounts[i][k] += sumcount_d;
//            lpsdTotalCounts[i] += count;
            }
            lpsdChannelCounts[i][0] += y_vals_d[0];
            lpsdChannelCounts[i][1] += y_vals_d[1]+y_vals_d[0];
//          System.out.println("i: "+i+" j: "+j+" gid: "+dt.getGroup_ID());
          }
        }
      }
      
      
      for (int i = 0; i < ndata; i++){
        dt = ds.getData_entry(i);
        t_vals_d = dt.getX_scale().getXs();
        y_vals_d = dt.getY_values();
        ((TabulatedData)dt).setErrors(y_vals_d); //storing raw counts into the errors[];
        ndetchannel = y_vals_d.length;
//        detChannelCorrectionCoeff = new float[ndetchannel];
        detchannelwidth = t_vals_d[1] - t_vals_d[0];
        if (ndetchannel != NDETCHANNEL) System.out.println("**WARNING**: ndetchannel not equal to "+NDETCHANNEL);
//          if (i == 0) System.out.println("runfile t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
        for (int k = 0; k < ndetchannel; k++){
//        if (k == 0 && i%100 == 0) System.out.println("detChannel "+i+": "+y_vals_d[k]);
          y_vals_d[k] *= 1/(1-TAU_DET*lpsdChannelCounts[spectrumID2lpsdID[i]][k]/npulses/detchannelwidth);
//            y_vals_d[k] *= detChannelCorrectionCoeff[i][k];
//        if (k == 0 && i%100 == 0) System.out.println("detChannel "+i+": "+" dead time correction: "+y_vals_d[k]);
        }    
      }
      System.out.println("Done.\n");
      
      int nbadgrp;
      StringBuffer removedDataIDList = new StringBuffer();
      if (needList) {       
//      if (GLADRunInfo.LRemovedDataID == null) { set up a LRemovedDataID for each GLADCrunch op;
        ArrayList LDeadDet = new ArrayList();
        ArrayList LRemovedDataID = new ArrayList();
        System.out.println("\nSetting up dead detector and corresponding data group ID lists...");
       
        StringBuffer deadDetList = new StringBuffer();
        int index_lpsd, id, nbaddet;
        int index_last = -1;
        for (int i = 0; i < ndata; i++){
          index_lpsd = spectrumID2lpsdID[i];
          if (runinfo.badLPSD[index_lpsd] == true ||
                             ((Float)(ds.getData_entry(i).getAttributeValue(Attribute.TOTAL_COUNT))).floatValue() <= lcutoff ) {
            if(index_lpsd!=index_last){
              LDeadDet.add(new Integer(index_lpsd) );
              deadDetList.append(index_lpsd + " "); 
            } 
            id = ds.getData_entry(i).getGroup_ID();
            LRemovedDataID.add(new Integer(id));
            removedDataIDList.append(id + " ");
            index_last = index_lpsd;
          } 
        }
        nbaddet = LDeadDet.size();
        nbadgrp = LRemovedDataID.size();
        System.out.println(nbaddet+" detectors dead:\n"+"deadDetList: "+deadDetList+"\n"
                              +nbadgrp+" out of "+ndata+" data groups removed;\n"+"removedDataList: "+removedDataIDList+"\n"
                              +"Analyze "+(NLPSD-nbaddet)+" detectors and "+(ndata-nbadgrp)+" data groups.\n");     
        runinfo.deadDet = new int[nbaddet];
        runinfo.removedDataID = new int[nbadgrp];
        for (int i = 0; i < nbaddet; i++) {
          runinfo.deadDet[i] = ((Integer)LDeadDet.get(i)).intValue();
        }
        for (int i = 0; i < nbadgrp; i++) {
          runinfo.removedDataID[i] = ((Integer)LRemovedDataID.get(i)).intValue();
        }
      }  
      //removed dead detector data groups;
      nbadgrp = runinfo.removedDataID.length;
      for (int i = 0; i < nbadgrp; i++){
        removedDataIDList.append(runinfo.removedDataID[i] + " ");
        ds.removeData_entry_with_id(runinfo.removedDataID[i]);        
      }
      System.out.println("Remove data with ID:\n"+removedDataIDList);    
      System.out.println("Use "+(ndata-nbadgrp)+" data groups from this runfile.\n");
      
    }
    
    else System.out.println("***UNEXPECTED ERROR***---dataset is neither \"Monitor Data\" nor \"Sample Data\"");
     
  }


//normalization and rebin;  
  public static void glad_norm (GLADRunProps runinfo, DataSet ds, DataSet dm){
    Data dt, bm, bm_W, bm_Wr, bm_Wc, new_dt_Q, new_dt_W;
    DetectorPosition position;
    int nmonchannel, ndetchannel;
    float            tof_length_m, tof_length_d, scattering_angle, psi, domega_d;
    float            min_Q, max_Q;
    int               num_Q, IDm, IDd;
    float            t_vals_d[], y_vals_dr[], y_vals_dc[], W_vals_d[], Q_vals_d[],
                     t_vals_m[], y_vals_mr[], y_vals_mc[], y_vals_mrr[], y_vals_mcr[],
                     e_vals_m[], W_vals_m[], 
                     y_vals_n[], e_vals_n[]; 
    float[] data_params = new float[4];   
    XScale        W_scale_d, W_scale_m, Q_scale_d, Q_scale_m, new_Q_scale;
    AttributeList    attr_list_d, attr_list_m;
    float[] wvals, effd;
    float cc, cd, cd0, cm, cm0;


    int NUMQ = ((Integer)runinfo.ExpConfiguration.get("GLAD.ANALYSIS.NUMQ")).intValue();
    float QMAX = ((Float)runinfo.ExpConfiguration.get("GLAD.ANALYSIS.QMAX")).floatValue();
    int NDETCHANNEL = ((Integer)runinfo.ExpConfiguration.get("GLAD.DET.NCHANNEL")).intValue();
    float DETCHANNELWIDTH = ((Float)runinfo.ExpConfiguration.get("GLAD.DET.CHANNELWIDTH")).floatValue();
    float aream = ((float[])runinfo.ExpConfiguration.get("GLAD.MON.AREA"))[0];
    float effm = ((float[])runinfo.ExpConfiguration.get("GLAD.MON.EFF"))[0];
 
                            
    bm = dm.getData_entry(0);                        
    IDm = bm.getGroup_ID();   
    DetectorPosition mon_position =  (DetectorPosition)bm.getAttributeValue(Attribute.DETECTOR_POS);
//    System.out.println("mon_position: "+mon_position+" sample to monitor distance: "+mon_position.getSphericalCoords()[0]);

    attr_list_m = bm.getAttributeList();
    t_vals_m = bm.getX_scale().getXs();
    y_vals_mc = bm.getY_values();
    y_vals_mr = bm.getErrors();

    nmonchannel = y_vals_mc.length;
    tof_length_m = -(mon_position.getDistance())+((Float)attr_list_m.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
//    System.out.println("tof_length_m: "+tof_length_m+" t_vals.length_m: "+t_vals_m.length);        
    W_vals_m = new float[nmonchannel+1];       
    for (int k = 0; k <= nmonchannel; k++){
      W_vals_m[k] = tof_calc.Wavelength(tof_length_m, t_vals_m[k]);
    }
    W_scale_m = new VariableXScale(W_vals_m);
    
    e_vals_m = new float[nmonchannel];
    for (int k = 0; k < nmonchannel; k++){
      e_vals_m[k] = (float)Math.sqrt(y_vals_mr[k]);
    }
    bm_W = Data.getInstance(W_scale_m, y_vals_mc, e_vals_m, IDm);     
    dm.replaceData_entry(bm_W, 0);

    min_Q = 1.0f/NUMQ;
    max_Q = QMAX + min_Q; 
    num_Q = (int)QMAX*NUMQ+1;
    new_Q_scale = new UniformXScale(min_Q, max_Q, num_Q);
    
    for (int i = 0; i < ds.getNum_entries(); i++){
      dt = ds.getData_entry(i);
      IDd = dt.getGroup_ID();
      attr_list_d = dt.getAttributeList();
      data_params = (float[])attr_list_d.getAttributeValue(GLADRunProps.GLAD_PARM);
      scattering_angle = data_params[0];
      tof_length_d = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue()+data_params[1];
      domega_d = data_params[2];
      psi = data_params[3];
      
      t_vals_d = dt.getX_scale().getXs();
      y_vals_dc = dt.getY_values();
      y_vals_dr = dt.getErrors();
      wvals = tvals2wvals(t_vals_d, tof_length_d);
      effd = geffcyl(runinfo, wvals, psi); 
      
      ndetchannel = y_vals_dc.length;
      float detchannelwidth = t_vals_d[1]-t_vals_d[0];
      if (ndetchannel != NDETCHANNEL) System.out.println("**WARNING**: detector time channel number: "+ndetchannel);
      if (detchannelwidth != DETCHANNELWIDTH) System.out.println("**WARNING**: monitor time channel width (us): "+detchannelwidth);
     
      W_vals_d = new float[ndetchannel+1];
      Q_vals_d = new float[ndetchannel+1];
/*                
      for (int k = 0; k <= NMONCHANNEL; k++){
        Q_vals_m[k] = tof_calc.DiffractometerQ(scattering_angle, tof_length_m, t_vals_m[k]);
      }
*/
      for (int k = 0; k <= ndetchannel; k++){
        W_vals_d[k] = tof_calc.Wavelength(tof_length_d, t_vals_d[k]);
        Q_vals_d[k] = tof_calc.DiffractometerQ(scattering_angle, tof_length_d, t_vals_d[k]); 
      }

      W_scale_d = new VariableXScale( W_vals_d );     
      new_dt_W = Data.getInstance(W_scale_d, y_vals_dc, IDd);
      y_vals_n = new_dt_W.getY_values();
      e_vals_n = new float[ndetchannel];   
//      new_bm_W = (Data)bm_W.clone();
//      new_bm_W.resample(W_scale_d,IData.SMOOTH_NONE);
      bm_Wr = Data.getInstance(W_scale_m, y_vals_mr, IDm); //raw counts;
      bm_Wc = Data.getInstance(W_scale_m, y_vals_mc, IDm+1); //corrected;
      bm_Wc.resample(W_scale_d, IData.SMOOTH_NONE);
      y_vals_mcr = bm_Wc.getY_values(); //rebined values;
      bm_Wr.resample(W_scale_d, IData.SMOOTH_NONE);
      y_vals_mrr = bm_Wr.getY_values();
      
//      y_vals_m = new_bm_W.getY_values();
//      e_vals_m = new_bm_W.getErrors();
       
      for(int k = 0; k < ndetchannel; k++){
        cc = aream*effm*.5f*(W_vals_d[k]+W_vals_d[k+1])/(domega_d*effd[k]);
        cd = y_vals_n[k];
        cm = y_vals_mcr[k];
        cd0 = y_vals_dr[k];
        cm0 = y_vals_mrr[k];
        y_vals_n[k] = (float) (cc*cd/cm);
        e_vals_n[k] = (float) (cc*cd/cm*Math.sqrt(cd0/cd/cd+cm0/cm/cm));

//        y_vals_n[k] /= (new_bm_W.getY_values()[k]/cc);
      }
                
      arrayUtil.Reverse(Q_vals_d);
      arrayUtil.Reverse(y_vals_n);
      arrayUtil.Reverse(e_vals_n);
      Q_scale_d = new VariableXScale( Q_vals_d );
      new_dt_Q = Data.getInstance(Q_scale_d, y_vals_n, e_vals_n, IDd);
      new_dt_Q.setAttributeList( attr_list_d );
      new_dt_Q.resample( new_Q_scale, IData.SMOOTH_NONE );
                    
      ds.replaceData_entry(new_dt_Q, i);       
    }
    ds.setTitle(ds.getTitle()+" "+(String)ds.getAttributeValue(Attribute.RUN_TITLE));
    ds.setX_units("/"+FontUtil.ANGSTROM);
    ds.setX_label( "Q" );
    ds.setY_label("Normalized Intensity");
  }

  /**
   * Converts run file data to a dataset of normalized counts in Q.
   *
   * @return The processed DataSet.
   */
  public Object calculate(  ) {
    String file_name = StringUtil.setFileSeparator( runfile.toString() );
    file_name = FilenameUtil.fixCase( file_name );
    System.out.println("\n"+"read from "+file_name);
    Runfile rf = null;
    GLADRunProps runinfo = (GLADRunProps)((Object[])ds0.getAttributeValue(GLADRunProps.GLAD_PROP))[0];
    float dnfract = ((Float)runinfo.ExpConfiguration.get("GLAD.SOURCE.DNEUTRON_BETA")).floatValue();
    float sourceT = ((Float)runinfo.ExpConfiguration.get("GLAD.SOURCE.PERIOD")).floatValue();
    int nfit = ((Integer)runinfo.ExpConfiguration.get("GLAD.SOURCE.DNEUTRON_NFIT")).intValue();

    try {
      rf = new Runfile(file_name);
       
      int instrument_type  = rf.InstrumentType();
      if ( instrument_type != InstrumentType.TOF_DIFFRACTOMETER )
        System.out.println("***UNEXPECTED ERROR***---runfile type is not TOF_DIFFRACTOMETER");
                
      if (rf == null) System.out.println("runfile is null;");
      System.out.println("runfile title: "+rf.RunTitle());
      System.out.println("# of histograms: "+rf.NumOfHistograms()+"\n"); 
    }
    catch (IOException e) {
    System.out.println("runfile reading error.");
    System.out.println("exception is "+e);
    e.printStackTrace();
    }      
    
    RunfileRetriever rr = new RunfileRetriever( file_name );
    DataSet dm = rr.getDataSet(0);    //DataSet 0 are monitor spectra: 0 is beam monitor, 1 the transmission monitor, 2 the proton on target
    DataSet ds = rr.getDataSet(1);     //DataSet 1 are the detector data;

    Data beam_monitor = dm.getData_entry(0); //beam monitor data;
    DetectorPosition mon_position =  (DetectorPosition)beam_monitor.getAttributeValue(Attribute.DETECTOR_POS);

    AttributeList attr_list_m = beam_monitor.getAttributeList();
    float[] t_vals_m = beam_monitor.getX_scale().getXs();
    float[] y_vals_m = beam_monitor.getY_values();
    int ndetchannel, nmonchannel = y_vals_m.length;
    float tof_length_m = -(mon_position.getDistance())+((Float)attr_list_m.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
    System.out.println("beam monitor TOF length: "+tof_length_m+" number of time channels: "+y_vals_m.length);

    correctDeadTime(runinfo, dm);        
    float delayed_neutron_sum = gdel_neut(t_vals_m, y_vals_m, tof_length_m, dnfract, sourceT, nfit);

    for (int i = 0; i< nmonchannel; i++){
//      if (i%1000 == 1) System.out.println("Mon Channel "+i+": "+dm.getY_values()[i]);      
      y_vals_m[i] -= delayed_neutron_sum;
//      if (i%1000 == 1) System.out.println("Mon Channel "+i+" delayed neutron corrected: "+dm.getY_values()[i]);      
    }
    System.out.println(delayed_neutron_sum+" subtracted from the monitor counts as the delayed neutron amount.");

/*
    if (noDeadDetList) 
      try {
        GLADRunProps.setDetTable(runinfo);
        GLADRunProps.setBadLPSD(runinfo, redpar.toString());
      } catch(Throwable t) {
        System.out.println("unexpected error");
        t.printStackTrace();
      }
*/

    if (noDeadDetList) {
      LoadUtil.Load_GLAD_LPSD_Info(ds, GLADRunProps.GLADDetTable);    
      runinfo.linkLPSDtoDataSet(ds, redpar.toString());
      runinfo.setBadDataGroups(ds, lcutoff);
    }

//    long start = System.currentTimeMillis();                
    correctDeadTime(runinfo, ds);
//    long time = System.currentTimeMillis()-start;
//    System.out.println("correctDeadTime() takes "+time+" ms.");
    
    float scattering_angle, domega, d2, psi, tof_length_d;
    float[] t_vals_d, y_vals_d;
    Float1DAttribute dt_grp_params;
    Data dt;    
    AttributeList attr_list_d;
    DetectorPosition position;
    for (int i = 0; i < ds.getNum_entries(); i++){
      dt = ds.getData_entry(i);
      attr_list_d = dt.getAttributeList();
      
      position = (DetectorPosition)attr_list_d.getAttributeValue(Attribute.DETECTOR_POS);
      
      scattering_angle = position.getScatteringAngle();
      domega = ((Float)attr_list_d.getAttributeValue(Attribute.SOLID_ANGLE)).floatValue();
      d2 = position.getDistance();
      psi = (float)(Math.PI/2-(position.getSphericalCoords())[2]);      
      dt_grp_params = new Float1DAttribute (GLADRunProps.GLAD_PARM, new float[] {scattering_angle, d2, domega, psi});
      dt.setAttribute(dt_grp_params);
      tof_length_d = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue()+d2;
      t_vals_d = dt.getX_scale().getXs();
      y_vals_d = dt.getY_values();
      ndetchannel = y_vals_d.length;

//      if (i == 0) System.out.println("deadtime t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
      delayed_neutron_sum = gdel_neut(t_vals_d, y_vals_d, tof_length_d, dnfract, sourceT, nfit);
      for (int k =0; k < ndetchannel; k++){
//        if (i==409 && k%100 == 1) System.out.println("Det Channel "+k+": "+dt.getY_values()[k]); 
        y_vals_d[k] -= delayed_neutron_sum;
//        if (i==409 && k%100 == 1) System.out.println("Det Channel "+k+" delayed neutron corrected: "+dt.getY_values()[k]);  
//        if (i==409 && k%100 == 1) System.out.println(delayed_neutron_sum+" subtracted from the detector counts as the delayed neutron amount");
        
      }
//      if (i == 0) System.out.println("del_neu t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
    }

// testing monitor spectrum smoothing:     
    if (MonSmoothing) {
      (new LowPassFilterDS0(dm, 0.05f, 2)).getResult();
      System.out.println("monitor smoothing...");
    }       
// testing vanadium smoothing: 
    if (DetSmoothing) {
      (new LowPassFilterDS0(ds, 0.05f, 2)).getResult();
      System.out.println("detector smoothing...");
    } 
       
    glad_norm (runinfo, ds, dm);
    ds.setTitle(ds.getTitle()+" "+"--->CRUNCH");
    
//    GLADScatter sca = new GLADScatter(runinfo, imask);
    
    Vector mon_nrm = new Vector(2);
//    mon_nrm.add(0, sca);
    mon_nrm.add(0, dm);
    mon_nrm.add(1, ds);
  
    return mon_nrm;
  }
    
  public static void main (String[] args) {
     
    GLADCrunch testcrunch = new GLADCrunch();
//      testCrunch.runprops[0] = GLADRunProps.getExpProps();
      
    GLADConfigure testconf = new GLADConfigure();
    testconf.hasCan = false;
    DataSet ds0 = (DataSet)testconf.calculate();      
    testcrunch.ds0 = ds0;
    testcrunch.runfile = new LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8094.run");
    testcrunch.noDeadDetList = true;
    testcrunch.redpar = new LoadFileString("/IPNShome/taoj/cvs/ISAW/Databases/gladrun2.par");
    Vector monnrm = (Vector) testcrunch.calculate();
    ViewManager view_nrm = new ViewManager((DataSet)monnrm.get(0), IViewManager.IMAGE);
    ViewManager view_mon = new ViewManager((DataSet)monnrm.get(1), IViewManager.IMAGE);

/*
      float[] wvals = new float[41];
      float psi = (float)(Math.PI/2-12.398/180.0*Math.PI); 
      for (int i = 0; i < 41; i++){
        wvals[i] =0.1f*(i+1);
      }
      float[] effd = geffcyl(wvals, psi);
      String output = "";
      for (int i = 0; i < effd.length; i++){
        output += "["+wvals[i]+","+effd[i]+"]"+",";      
      }
      System.out.println(output);
*/

/*
    RunfileRetriever rr = new RunfileRetriever( "/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8094.run" );
    DataSet ds = rr.getDataSet(1);
    Data dt;
    int k = 0;
    StringBuffer zeroDataIDList = new StringBuffer();
    for (int i = 0; i < ds.getNum_entries(); i++) {
      dt = ds.getData_entry(i);
      if (((Float)(dt.getAttributeValue(Attribute.TOTAL_COUNT))).floatValue() == 0.0f) {
        zeroDataIDList.append(dt.getGroup_ID()+" ");
        k++;
      }
    }
    System.out.println("zeroDataIDList:\n"+zeroDataIDList);
    System.out.println(ds.getNum_entries()+" data blocks, "+k+" of them are dead");
*/
  
  }

}
