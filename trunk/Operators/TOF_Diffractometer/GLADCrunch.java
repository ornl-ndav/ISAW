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
import IPNS.Runfile.Segment;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;
import java.util.ArrayList;


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
   * @param ISvan the vanadium calibration's beam monitor spectrum is needed 
   *              for later use;
   * @param redpar GLAD bad detector list file;
   */
  public LoadFileString runfile = new LoadFileString();
  public boolean ISvan = false;
  public LoadFileString redpar = new LoadFileString();

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
  public static float gdel_neut (float t[], float y[], float tof_length, float dn_fraction, float source_period, int npts) {
        
    if (t.length != y.length+1) {
      System.out.println("***ERROR***: data is not a histogram"+" x.length: "+t.length+" y.length: "+y.length);
      return 0.0f;
    }
    
    float channel_width = t[1]-t[0];
    float sum = 0.0f;
    for (int i = 0; i < npts; i++) {
      sum += y[i];    
    }
    
//    if (t.length == 14951 ) System.out.println("sum of first 20 channels is: "+sum);
    sum += sum/channel_width/npts*t[0];
    
    sum *= dn_fraction*channel_width/source_period;
//    if (t.length == 14951 ) System.out.println("corrected sum: "+sum+" channel_width: "+channel_width+" source period: "+source_period);
    return sum;
  }

//detector efficiency calculation, a porting of geffcyl.for code based on the method by J.M. Carpenter (refer to IPNS note 17);
  public static float[] geffcyl (float[] t, float tof_length, float psi) {
    int ns = t.length-1; //# of time channels;
    float[] effd = new float[ns];
       
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
    
    double sl = GLADRunInfo.FACD/Math.cos(psi); //"effective" radius  
    double sigma_max = sl*.5*(tof_calc.Wavelength(tof_length, t[ns-1])+tof_calc.Wavelength(tof_length, t[ns]));
    if (sigma_max > sigma_lim) System.out.println("***ERROR***: SIGMAR out of range in detector efficient calculation");
    for (int i = 0; i < ns; i++){
      sigma = sl*.5*(tof_calc.Wavelength(tof_length, t[i])+tof_calc.Wavelength(tof_length, t[i+1]));
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

  static void getBadLPSD(String GladRunPar) throws IOException, InterruptedException{
         
    BufferedReader fr_input = new BufferedReader(new FileReader(GladRunPar));
    String element_symbol, line = null;
    String[] list;
    int[] nbaddets = new int[10];
    GLADRunInfo.LPSDBad = new boolean[GLADRunInfo.NLPSDID];
    int idet, nbad;
      
    while((line = fr_input.readLine()) != null ) {
      if(line.charAt(0) == '#') continue;
      list = Pattern.compile("\\s+").split(line.trim());
      for (int i = 0; i < 10; i++){
        nbaddets[i] = (new Integer(list[i])).intValue();
      }
      break;
    }
      
    for (int i = 0; i < 10; i++){
      nbad = nbaddets[i];
      if (nbad != 0){
        while((line = fr_input.readLine()) != null ) {
          if(line.charAt(0) == '#') continue;
          list = Pattern.compile("\\s+").split(line.trim());
          if (list.length != nbad) System.out.println("\n****unexpected error***\n");
          for (int j = 0; j < nbad; j++){
            idet =  (new Integer(list[j])).intValue()-1;
            GLADRunInfo.LPSDBad[GLADRunInfo.BankDet2lpsdID[i+1][idet]-1] = true;
//            System.out.println(" BAD: bank "+(i+1)+" idet "+idet);
          }            
          break;
        } 
      }        
    }

    fr_input.close();
  }      
  
//dead time correction routine; data groups from dead detectors also removed;
  public void correct_DeadTime(DataSet ds) {
    
    float npulses = ((Float)ds.getAttributeValue(Attribute.NUMBER_OF_PULSES)).floatValue();
    int ndata = ds.getNum_entries();
    int nmonchannel, ndetchannel;
    String ds_type = (String)(ds.getAttributeValue(Attribute.DS_TYPE));
    
    if (ds_type.equals("Monitor Data")) {
      System.out.println("\nmonitor data dead time correction...\n");
      Data dm = ds.getData_entry(0);
      float[] t_vals_m = dm.getX_scale().getXs();
      float[] y_vals_m = dm.getY_values();
      nmonchannel = y_vals_m.length;
      float monchannelwidth = t_vals_m[1]-t_vals_m[0];
      
      if (nmonchannel != GLADRunInfo.NMONCHANNEL) System.out.println("**WARNING**: nmonchannel not equal to "+GLADRunInfo.NMONCHANNEL);
      float[] monChannelCorrectionCoeff = new float[nmonchannel];
       
      for (int k = 0; k < nmonchannel; k++){
//        if (k%1000 == 1) System.out.println("monitor channel "+k+": "+dm.getY_values()[k]);     
        monChannelCorrectionCoeff[k] = 1/(1-GLADRunInfo.TAU_MON*y_vals_m[k]/npulses/monchannelwidth);
        y_vals_m[k] *= monChannelCorrectionCoeff[k];
//        if (k%1000 == 1) System.out.println("monitor channel "+k+": "+dm.getY_values()[k]);
      }
      System.out.println("Done.");
    }
    
    else if (ds_type.equals("Sample Data")) {
      System.out.println("\ndetector data dead time correction...\n");
      Data dt;
      int[] spectrumID2lpsdID = new int[ndata], detIDs;
      int[][] lpsdID2spectrumID = new int[GLADRunInfo.NLPSDID][GLADRunInfo.NSEGMENTS]; 
      float[][] lpsdChannelCounts = new float[GLADRunInfo.NLPSDID][GLADRunInfo.NDETCHANNEL];
      float[][] dataChannelCorrectionCoeff = new float[ndata][GLADRunInfo.NDETCHANNEL];
      float[] lpsdTotalCounts = new float[GLADRunInfo.NLPSDID];
      float count;
      int idet, gid;
      
      //setup the GLAD detector mapping table from bank/det number to the lpsd index;
      if (GLADRunInfo.BankDet2lpsdID == null){
        try {
          GLADRunInfo.SetupDets(GLADRunInfo.GLADDetsTable);
        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
      }
      
      //read in the bad detector list;
      if(GLADRunInfo.LPSDBad == null){
        try {
          GLADRunInfo.SetupDets(GLADRunInfo.GLADDetsTable);
          getBadLPSD(redpar.toString());
        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
      }
      
      for (int i = 0; i < ndata; i++){
        dt = ds.getData_entry(i);
        gid = dt.getGroup_ID();
        detIDs = (int[])dt.getAttributeValue(Attribute.DETECTOR_IDS);
        idet = detIDs[0];
        spectrumID2lpsdID[i] = idet/64; //lpsdID starting at 0;
        lpsdID2spectrumID[idet/64][idet%64] = i+1; //spectrumID starting at 1 to differentiate from empty elements of the 2-D array;
      }

      int index;
      for (int i = 0; i < GLADRunInfo.NLPSDID; i++){
        for (int j = 0; j < GLADRunInfo.NSEGMENTS; j++){
          index = lpsdID2spectrumID[i][j];
          if (index != 0) {
            dt = ds.getData_entry(index-1);
            for (int k =0; k < dt.getY_values().length; k++){
            count = dt.getY_values()[k];
            lpsdChannelCounts[i][k] += count;
            lpsdTotalCounts[i] += count;
            }
//          System.out.println("i: "+i+" j: "+j+" gid: "+dt.getGroup_ID());
          }
        }
      }
      
      float[] t_vals_d, y_vals_d;
      float detchannelwidth;
      for (int i = 0; i < ndata; i++){
        dt = ds.getData_entry(i);
        t_vals_d = dt.getX_scale().getXs();
        y_vals_d = dt.getY_values();
        ndetchannel = y_vals_d.length;
        detchannelwidth = t_vals_d[1] - t_vals_d[0];
        if (ndetchannel != GLADRunInfo.NDETCHANNEL) System.out.println("**WARNING**: ndetchannel not equal to "+GLADRunInfo.NDETCHANNEL);
//          if (i == 0) System.out.println("runfile t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
        for (int k = 0; k < ndetchannel; k++){
          dataChannelCorrectionCoeff[i][k] = 1/(1-GLADRunInfo.TAU_DET*lpsdChannelCounts[spectrumID2lpsdID[i]][k]/npulses/detchannelwidth);
//            if (k == 0 && i%100 == 0) System.out.println("dataChannel "+i+": "+y_vals_d[k]);
          y_vals_d[k] *= dataChannelCorrectionCoeff[i][k];
//            if (k == 0 && i%100 == 0) System.out.println("dataChannel "+i+": "+" dead time correction: "+y_vals_d[k]);
        }    
      }
      System.out.println("Done");
      
      String removedDataIDList = "";
      int nbadgrp;   
      if (GLADRunInfo.LRemovedDataID == null) {
        GLADRunInfo.LDeadDet = new ArrayList();
        GLADRunInfo.LRemovedDataID = new ArrayList();
        System.out.println("\nCreating new dead detector and corresponding data group ID lists...");
       
        String deadDetList = "";
        int index_lpsd, id, nbaddet;
        int index_last = -1;
        for (int i = 0; i < ndata; i++){
          index_lpsd = spectrumID2lpsdID[i];
          if (GLADRunInfo.LPSDBad[index_lpsd] == true ||
              ((Float)(ds.getData_entry(i).getAttributeValue(Attribute.TOTAL_COUNT))).floatValue() == 0.0f ) {
            if(index_lpsd!=index_last){
              GLADRunInfo.LDeadDet.add(new Integer(index_lpsd) );
              deadDetList += index_lpsd + " "; 
            } 
            id = ds.getData_entry(i).getGroup_ID();
            GLADRunInfo.LRemovedDataID.add(new Integer(id));
            removedDataIDList += id + " ";
            index_last = index_lpsd;
          }       
        }
        nbaddet = GLADRunInfo.LDeadDet.size();
        nbadgrp = GLADRunInfo.LRemovedDataID.size();
        System.out.println(nbaddet+" detectors dead:\n"+"deadDetList: "+deadDetList+"\n"
                              +nbadgrp+" out of "+ndata+" data groups removed;\n"+"removedDataList: "+removedDataIDList+"\n"
                              +"Analyze "+(GLADRunInfo.NLPSD-nbaddet)+" detectors and "+(ndata-nbadgrp)+" data groups.\n");    
      }  
      
      //removed dead detector data groups;
      removedDataIDList = "";
      nbadgrp = GLADRunInfo.LRemovedDataID.size();
      for (int i = 0; i < nbadgrp; i++){
        removedDataIDList += (Integer)GLADRunInfo.LRemovedDataID.get(i) + " ";
        ds.removeData_entry_with_id(((Integer)GLADRunInfo.LRemovedDataID.get(i)).intValue());        
      }
      System.out.println("Remove data with ID:\n"+removedDataIDList);    
      System.out.println("Use "+(ndata-nbadgrp)+" data groups from this runfile.\n");
    }
    
    else System.out.println("***UNEXPECTED ERROR***---dataset is neither \"Monitor Data\" nor \"Sample Data\"");
     
  }

//normalization and rebin;  
  public static void glad_norm (DataSet ds, Data dm, boolean ISvan){
    Data dt, new_dt_Q, new_dm_Q, new_dt_W, new_dm_W;
    DetectorPosition position;
    int nmonchannel, ndetchannel;
    float            tof_length_m, tof_length_d, scattering_angle, psi, domega_d;
    float            min_Q, max_Q;
    int               num_Q, IDm, IDd;
    float            t_vals_d[], y_vals_d[], W_vals_d[], Q_vals_d[],
                        t_vals_m[], y_vals_m[], W_vals_m[], 
                        y_vals_n[]; 
    float[] data_params = new float[4];   
    XScale        W_scale_d, W_scale_m, Q_scale_d, Q_scale_m, new_Q_scale;
    AttributeList    attr_list_d, attr_list_m;
    float[] effd;
    float cc;
                            
    IDm = dm.getGroup_ID();   
    DetectorPosition mon_position =  (DetectorPosition)dm.getAttributeValue(Attribute.DETECTOR_POS);
//    System.out.println("mon_position: "+mon_position+" sample to monitor distance: "+mon_position.getSphericalCoords()[0]);

    attr_list_m = dm.getAttributeList();
    t_vals_m = dm.getX_scale().getXs();
    y_vals_m = dm.getY_values();
    nmonchannel = y_vals_m.length;
    tof_length_m = -(mon_position.getDistance())+((Float)attr_list_m.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
//    System.out.println("tof_length_m: "+tof_length_m+" t_vals.length_m: "+t_vals_m.length);
        
    W_vals_m = new float[nmonchannel+1];       
    for (int k = 0; k <= nmonchannel; k++){
      W_vals_m[k] = tof_calc.Wavelength(tof_length_m, t_vals_m[k]);
    }

    min_Q = 1.0f/GLADRunInfo.NUMQ;
    max_Q = GLADRunInfo.GLADQMAX + min_Q; 
    num_Q = (int)GLADRunInfo.GLADQMAX*GLADRunInfo.NUMQ+1;
    new_Q_scale = new UniformXScale(min_Q, max_Q, num_Q);
    
    for (int i = 0; i < ds.getNum_entries(); i++){
      dt = ds.getData_entry(i);
      IDd = dt.getGroup_ID();
      attr_list_d = dt.getAttributeList();
      data_params = (float[])attr_list_d.getAttributeValue(GLADRunInfo.GLAD_PARM);
      scattering_angle = data_params[0];
      tof_length_d = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue()+data_params[1];
      domega_d = data_params[2];
      psi = data_params[3];
      
      t_vals_d = dt.getX_scale().getXs();
      y_vals_d = dt.getY_values();
      effd = geffcyl(t_vals_d, tof_length_d, psi); 
      
      ndetchannel = y_vals_d.length;
      float detchannelwidth = t_vals_d[1]-t_vals_d[0];
      if (ndetchannel != GLADRunInfo.NDETCHANNEL) System.out.println("**WARNING**: detector time channel number: "+ndetchannel);
      if (detchannelwidth != GLADRunInfo.DETCHANNELWIDTH) System.out.println("**WARNING**: monitor time channel width (us): "+detchannelwidth);
     
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
      W_scale_m = new VariableXScale(W_vals_m);
      new_dt_W = Data.getInstance(W_scale_d, y_vals_d, IDd);
      new_dm_W = Data.getInstance(W_scale_m, y_vals_m, IDm);
      if (ISvan) GLADRunInfo.dm_van_W = (Data)new_dm_W.clone();
      new_dm_W.resample(W_scale_d,IData.SMOOTH_NONE);
      y_vals_n = new_dt_W.getY_values();
          
      for(int k = 0; k < ndetchannel; k++){
        cc = GLADRunInfo.AREAM[0]*GLADRunInfo.EFFM[0]*.5f*(W_vals_d[k]+W_vals_d[k+1])/(domega_d*effd[k]);
        y_vals_n[k] /= (new_dm_W.getY_values()[k]/cc);
      }
                
      arrayUtil.Reverse( Q_vals_d );
      arrayUtil.Reverse(y_vals_n);
      Q_scale_d = new VariableXScale( Q_vals_d );
      new_dt_Q = Data.getInstance(Q_scale_d, y_vals_n, IDd);
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
    DataSet beam_monitor = rr.getDataSet(0);    
    DataSet ds = rr.getDataSet(1);

    Data dm = beam_monitor.getData_entry(0);
    DetectorPosition mon_position =  (DetectorPosition)dm.getAttributeValue(Attribute.DETECTOR_POS);

    AttributeList attr_list_m = dm.getAttributeList();
    float[] t_vals_m = dm.getX_scale().getXs();
    float[] y_vals_m = dm.getY_values();
    int ndetchannel, nmonchannel = y_vals_m.length;
    float tof_length_m = -(mon_position.getDistance())+((Float)attr_list_m.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
    System.out.println("beam monitor TOF length: "+tof_length_m+" number of time channels: "+y_vals_m.length);
        
    correct_DeadTime(beam_monitor);
        
    float delayed_neutron_sum = gdel_neut(t_vals_m, y_vals_m, tof_length_m, GLADRunInfo.DNFRACT, GLADRunInfo.SOURCE_PERIOD, GLADRunInfo.DELAYED_NEUTRON_NFIT);
    for (int i = 0; i< nmonchannel; i++){
//      if (i%1000 == 1) System.out.println("Mon Channel "+i+": "+dm.getY_values()[i]);      
      y_vals_m[i] -= delayed_neutron_sum;
//      if (i%1000 == 1) System.out.println("Mon Channel "+i+" delayed neutron corrected: "+dm.getY_values()[i]);      
    }
    System.out.println(delayed_neutron_sum+" subtracted from the monitor counts as the delayed neutron amount");
                
    correct_DeadTime(ds);
    
    float scattering_angle, domega, d2, psi, tof_length_d;
    float[] t_vals_d, y_vals_d;
    Float1DAttribute dt_grp_params;    
    for (int i = 0; i < ds.getNum_entries(); i++){
      Data dt = ds.getData_entry(i);
      AttributeList attr_list_d = dt.getAttributeList();
      
      DetectorPosition position = (DetectorPosition)attr_list_d.getAttributeValue(Attribute.DETECTOR_POS);
      
      scattering_angle = position.getScatteringAngle();
      domega = ((Float)attr_list_d.getAttributeValue(Attribute.SOLID_ANGLE)).floatValue();
      d2 = position.getDistance();
      psi = (float)(Math.PI/2-(position.getSphericalCoords())[2]);      
      dt_grp_params = new Float1DAttribute (GLADRunInfo.GLAD_PARM, new float[] {scattering_angle, d2, domega, psi});
      dt.setAttribute(dt_grp_params);
      tof_length_d = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue()+d2;
      t_vals_d = dt.getX_scale().getXs();
      y_vals_d = dt.getY_values();
      ndetchannel = y_vals_d.length;

//      if (i == 0) System.out.println("deadtime t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
      delayed_neutron_sum = gdel_neut(t_vals_d, y_vals_d, tof_length_d, GLADRunInfo.DNFRACT, GLADRunInfo.SOURCE_PERIOD, GLADRunInfo.DELAYED_NEUTRON_NFIT);
      for (int k =0; k < ndetchannel; k++){
//        if (i==409 && k%100 == 1) System.out.println("Det Channel "+k+": "+dt.getY_values()[k]); 
        y_vals_d[k] -= delayed_neutron_sum;
//        if (i==409 && k%100 == 1) System.out.println("Det Channel "+k+" delayed neutron corrected: "+dt.getY_values()[k]);  
        
      }
//      if (i == 0) System.out.println("del_neu t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
    }
        
    glad_norm (ds, dm, ISvan);
    ds.setTitle(ds.getTitle()+" "+"--->CRUNCH");
    return ds;

    }

  }
