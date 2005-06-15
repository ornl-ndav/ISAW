/*
 * File:  GLADIoQ.java
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
 * Revision 1.1  2005/05/05 02:06:10  taoj
 * added into the cvs
 *

 */

package Operators.TOF_Diffractometer;

import java.lang.System;
import gov.anl.ipns.Util.Numeric.ClosedInterval;
import DataSetTools.util.SharedData;
import DataSetTools.dataset.Float1DAttribute;
import java.util.ArrayList;
import java.io.IOException;
import java.io.StringReader;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IData;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.VariableXScale;
import DataSetTools.dataset.XScale;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import DataSetTools.math.tof_calc;
import DataSetTools.operator.DataSet.Math.DataSet.DataSetDivide;
import DataSetTools.operator.DataSet.Math.DataSet.DataSetSubtract;
//import DataSetTools.operator.DataSet.Math.Scalar.DataSetScalarDivide;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.util.FilenameUtil;
import gov.anl.ipns.Util.Sys.StringUtil;
import gov.anl.ipns.Util.Numeric.arrayUtil;
import gov.anl.ipns.Util.Numeric.Format;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;
import IPNS.Runfile.InstrumentType;
import IPNS.Runfile.Runfile;
import IPNS.Runfile.Segment;
//import DataSetTools.operator.DataSet.EditList.DataSetMerge;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import Operators.Special.LowPassFilterDS0;
//import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;

/**
 * This class is the prototype for GLAD analysis but now completely replaced.
 * The glad_group2() (REDUCE part) and glad_group3() (GNORM part) show how
 * grouping was performed with Genie and are therefore worth keeping.
 * 
 */

public class GLADIoQ {
  
  public static final float TAU_DET = 8.0f; //detector dead time in microseconds;
  public static final float TAU_MON = 2.0f; //monitor dead time in microsectonds;
  public static final int NLPSDID = 335; //# of LPSD on GLAD;
  public static final int NLPSD = 231;
  public static final int NSEGMENTS = 64; //# of segments on each LPSD;
  public static final int NDETCHANNEL = 2480; //# of detector time channels;
  public static final int NMONCHANNEL = 14950; //# of monitor time channels;
  public static final float DETCHANNELWIDTH = 5.0f; //detector time channel width;
  public static final float MONCHANNELWIDTH = 1.0f; //monitor time channel width;
  public static final int NSPECTRUM = 914; //# of data blocks in detector dataset;
  public static final int NMONITOR = 3; //#of monitors: [0]: beam monitor, 
                                                                     //[1] transmission monitor (fudge), [2] proton monitor;
  public static final float SOURCE_PERIOD = 3.33333e4f; //IPNS running at 30 Hz;
  public static final int DELAYED_NEUTRON_NFIT = 20; //first 20 channels used in delayed neutron correction;
  public static final float DNFRACT = 0.00425f; //delayed neutron fraction;
  public static final float FACD = 0.41234502f; 
                          //for He3 detector: (number density)*(absorption cross section)*(detector radius)/1.8 Angstrom;
  public static final float[] AREAM = {6.4516f, 7.8e-3f, 0.0f}; //monitor areas;
  public static final float[] EFFM = {5.56e-5f, 1.0f, 0.0f}; //monitor efficiency coefficients;
  public static final int NUMQ = 40; //default number of Q bins in 1 /Angstrom, Qstep = 1/NUMQ;
  public static final float GLADQMAX = 40.0f; //max Q for data analysis;
//  public static final int NANGLES = 9;

  public static Data dm_van_W = null; //vanadium calibration's beam monitor spectrum, needed to generate weighting;
  public static ArrayList LDeadDet = null;
  public static ArrayList LRemovedDataID = null;
  
  public static final String GLAD_PARM = "GLAD Instrument Parameters";  

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
    
    if (t.length == 14951 ) System.out.println("sum of first 20 channels is: "+sum);
    sum += sum/channel_width/npts*t[0];
    
    sum *= dn_fraction*channel_width/source_period;
//    if (t.length == 14951 ) System.out.println("corrected sum: "+sum+" channel_width: "+channel_width+" source period: "+source_period);
    return sum;
  }

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
    
    double sl = FACD/Math.cos(psi); //"effective" radius  
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
  
  public static boolean[] LPSDBad = null;
  static void getBadLPSD(String GladRunPar) throws IOException, InterruptedException{
         
      BufferedReader fr_input = new BufferedReader(new FileReader(GladRunPar));
      String element_symbol, line = null;
      String[] list;
      int[] nbaddets = new int[10];
      LPSDBad = new boolean[NLPSDID];
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
            LPSDBad[GLADRunInfo.BankDet2lpsdID[i+1][idet]-1] = true;
//            System.out.println(" BAD: bank "+(i+1)+" idet "+idet);
            }            
            break;
          } 
        }        
      }

      fr_input.close();
    }      
  
  
  public static void correct_DeadTime(DataSet ds) {
    
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
      
      if (nmonchannel != NMONCHANNEL) System.out.println("**WARNING**: nmonchannel not equal to "+NMONCHANNEL);
      float[] monChannelCorrectionCoeff = new float[nmonchannel];
       
      for (int k = 0; k < nmonchannel; k++){
//           if (k%1000 == 1) System.out.println("monitor channel "+k+": "+dm.getY_values()[k]);     
           monChannelCorrectionCoeff[k] = 1/(1-TAU_MON*y_vals_m[k]/npulses/monchannelwidth);
           y_vals_m[k] *= monChannelCorrectionCoeff[k];
//           if (k%1000 == 1) System.out.println("monitor channel "+k+": "+dm.getY_values()[k]);
      }
    }
    
    else if (ds_type.equals("Sample Data")) {
      System.out.println("\ndetector data dead time correction...\n");
      Data dt;
      int[] spectrumID2lpsdID = new int[ndata], detIDs;
      int[][] lpsdID2spectrumID = new int[NLPSDID][NSEGMENTS]; 
      float[][] lpsdChannelCounts = new float[NLPSDID][NDETCHANNEL];
      float[][] dataChannelCorrectionCoeff = new float[ndata][NDETCHANNEL];
      float[] lpsdTotalCounts = new float[NLPSDID];
      float count;
//      int[] GIDBankDet;
//      ArrayList index2BankDet = new ArrayList();
//      ArrayList[][] BankDet2GID = new ArrayList[11][54];
      int idet, gid;
      
      
      if (GLADRunInfo.BankDet2lpsdID == null){
        try {
          GLADRunInfo.SetupDets(GLADRunInfo.GLADDetsTable);
        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
      }
      
      if(LPSDBad == null){
        try {
          getBadLPSD("/IPNShome/taoj/GLAD/gladrun.par");
        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
      }

//find out and print out the mapping between bank/det and group ID;
      
      for (int i = 0; i < ndata; i++){
        dt = ds.getData_entry(i);
        gid = dt.getGroup_ID();
        detIDs = (int[])dt.getAttributeValue(Attribute.DETECTOR_IDS);
        idet = detIDs[0];
        spectrumID2lpsdID[i] = idet/64; //lpsdID starting at 0;
        lpsdID2spectrumID[idet/64][idet%64] = i+1; //spectrumID starting at 1 to differentiate from empty elements of the 2-D array;

/*find the mapping between bank/det and group ID;
        for (int j = 1; j <=10; j++){
          for (int k = 0; k < 53; k++){
            if (idet/64 == GLADRunInfo.BankDet2lpsdID[j][k]-1){
              GIDBankDet = new int[3];
              GIDBankDet[0] = gid;
              GIDBankDet[1] = j; //starting at 1;
              GIDBankDet[2] = k+1; //starting at 1;
              index2BankDet.add(GIDBankDet);
              
              if (BankDet2GID[j][k+1] == null) BankDet2GID[j][k+1] = new ArrayList();
              BankDet2GID[j][k+1].add(new Integer(gid));
            }
          }
        }
*/        
        
      }
      
/* print out the mapping between bank/det and GID;      
      System.out.println("\nsize of index2BankDet: "+index2BankDet.size());
      for (int i =0; i < index2BankDet.size(); i++){
        GIDBankDet = (int[])index2BankDet.get(i);
        System.out.println("gid: "+Format.integer(GIDBankDet[0],4)+"\tbank: "+GIDBankDet[1]+"\t"+"detector: "+GIDBankDet[2]); 
      }
      
      String BankDet2GIDList = null;
      for (int j =1; j<=10; j++){
        for (int k = 0; k < 53; k++){
          if (BankDet2GID[j][k+1] != null){
            BankDet2GIDList = "bank: "+j+"\tdetector: "+(k+1)+"\tGID: ";
            for (int l = 0; l < BankDet2GID[j][k+1].size(); l++){
              BankDet2GIDList += (Integer)BankDet2GID[j][k+1].get(l)+" ";
            }
          System.out.println(BankDet2GIDList);  
          }
          
        }
      }
*/
      
      int index;
      for (int i = 0; i < NLPSDID; i++){
        for (int j = 0; j < NSEGMENTS; j++){
          index = lpsdID2spectrumID[i][j];
          if (index != 0) {
            dt = ds.getData_entry(index-1);
            for (int k =0; k < NDETCHANNEL; k++){
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
        if (ndetchannel != NDETCHANNEL) System.out.println("**WARNING**: ndetchannel not equal to "+NDETCHANNEL);
//          if (i == 0) System.out.println("runfile t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
        for (int k = 0; k < ndetchannel; k++){
          dataChannelCorrectionCoeff[i][k] = 1/(1-TAU_DET*lpsdChannelCounts[spectrumID2lpsdID[i]][k]/npulses/detchannelwidth);
//            if (k == 0 && i%100 == 0) System.out.println("dataChannel "+i+": "+y_vals_d[k]);
          y_vals_d[k] *= dataChannelCorrectionCoeff[i][k];
//            if (k == 0 && i%100 == 0) System.out.println("dataChannel "+i+": "+" dead time correction: "+y_vals_d[k]);
        }    
      }
        
      
/*      
      if (LRemovedDataID == null) {
        LDeadDet = new ArrayList();
        LRemovedDataID = new ArrayList();
        System.out.println("\nCreating new dead detector and corresponding data group ID lists...\n");
       
        String deadDetList = "", removedDataIDList = "";
        int index_lpsd, id;
        index_last = -1;
        for (int i = 0; i < ndata; i++){
          index_lpsd = spectrumID2lpsdID[i];
          if (lpsdTotalCounts[index_lpsd] == 0.0f 
          || ((Float)(ds.getData_entry(i).getAttributeValue(Attribute.TOTAL_COUNT))).floatValue() < 100.0f ) {
            if(index_lpsd!=index_last){
              LDeadDet.add(new Integer(index_lpsd) );
              deadDetList += index_lpsd + " "; 
            }
            id = ds.getData_entry(i).getGroup_ID();
            LRemovedDataID.add(new Integer(id));
            removedDataIDList += id + " ";
            index_last = index_lpsd;
          }  
        }
        System.out.println(LDeadDet.size()+" detectors dead\n"+"deadDetList: "+deadDetList+"\n"
                                    +LRemovedDataID.size()+" out of "+ndata+" data groups removed\n"+"removedDataList: "+removedDataIDList+"\n");    
      }  
*/
      String removedDataIDList = "";
      int nbadgrp; 
      if (LRemovedDataID == null) {
        LDeadDet = new ArrayList();
        LRemovedDataID = new ArrayList();
        System.out.println("\nCreating new dead detector and corresponding data group ID lists...");
       
        String deadDetList = "";
        int index_lpsd, id, nbaddet;
        int index_last = -1;
        for (int i = 0; i < ndata; i++){
          index_lpsd = spectrumID2lpsdID[i];
          if (LPSDBad[index_lpsd] == true ||
              ((Float)(ds.getData_entry(i).getAttributeValue(Attribute.TOTAL_COUNT))).floatValue() == 0.0f) {
            if(index_lpsd!=index_last){
              LDeadDet.add(new Integer(index_lpsd) );
              deadDetList += index_lpsd + " "; 
            } 
            id = ds.getData_entry(i).getGroup_ID();
            LRemovedDataID.add(new Integer(id));
            removedDataIDList += id + " ";
            index_last = index_lpsd;
          }       
        }
        nbaddet = LDeadDet.size();
        nbadgrp = LRemovedDataID.size();
        System.out.println(nbaddet+" detectors dead:\n"+"deadDetList: "+deadDetList+"\n"
                              +nbadgrp+" out of "+ndata+" data groups bad;\n"+"removedDataList: "+removedDataIDList+"\n"
                              +"Analyze "+(NLPSD-nbaddet)+" detectors and "+(ndata-nbadgrp)+" data groups.\n");    
      }  
      
      removedDataIDList = "";
      nbadgrp = LRemovedDataID.size();
      for (int i = 0; i < nbadgrp; i++){
        removedDataIDList += (Integer)LRemovedDataID.get(i) + " ";
        ds.removeData_entry_with_id(((Integer)LRemovedDataID.get(i)).intValue());
      }
      System.out.println("Remove data with ID:\n"+removedDataIDList);
      System.out.println("Use "+(ndata-nbadgrp)+" data groups.\n");
        
      
    }
    
    else System.out.println("***WARNING***: dataset not a \"Monitor Data\" or \"Sample Data\"");
     
  }
  
  
  public static DataSet glad_crunch (String runfile, boolean ISvan){
    Runfile rf;
    String    file_name     = StringUtil.setFileSeparator( runfile );
        file_name = FilenameUtil.fixCase( file_name );
        System.out.println("read from "+file_name);
      
        try {
          rf = new Runfile(file_name);
       
          int instrument_type  = rf.InstrumentType();
          if ( instrument_type == InstrumentType.TOF_DIFFRACTOMETER )
          System.out.println("TOF_DIFFRACTOMETER type runfile");
                
          if (rf == null) System.out.println("runfile is null;");
          System.out.println("runfile title: "+rf.RunTitle());
          System.out.println("# of histograms: "+rf.NumOfHistograms()+"\n"); 
               
          int first_id = rf.MinSubgroupID(1 );
          int last_id  = rf.MaxSubgroupID( 1 );   
                  
       
          for (int j = first_id; j <= last_id; j++){
          Segment[] group_segments = rf.SegsInSubgroup(j);
//        System.out.println("# of segments in group "+j+": "+group_segments.length);
                                              
          double fp, angle = 0.0f;
          int element_id, nsegments = group_segments.length;  
          for (int i = 0; i < nsegments; i++){
            element_id = group_segments[i].SegID();
            fp = (float)rf.RawFlightPath(element_id);
//            if(j==first_id || j==first_id+1) System.out.println(rf.RawDetectorAngle(group_segments[i]));
            if ( fp < 0.0) System.out.println("*******goup "+j+" SegID: "+element_id+" fp: "+(float)fp+"******");
                  
            }
//            angle /= nsegments;
//            System.out.println("groupID "+j+" nsegments: "+nsegments+" t2: "+angle);
          }
        }
        catch (IOException e) {
        System.out.println("runfile reading error.");
        System.out.println("exception is "+e);
        e.printStackTrace();
        }      
    
    RunfileRetriever rr = new RunfileRetriever( file_name );
     
    DataSet beam_monitor = rr.getDataSet(0);    
    DataSet ds = rr.getDataSet(1);

        String dm_type = (String)beam_monitor.getAttributeValue(Attribute.DS_TYPE);
        System.out.println("DataSet beam_monitor type is "+dm_type+", with "+beam_monitor.getNum_entries()
                                                      +" spectrums.");
                                      
        Data dm = beam_monitor.getData_entry(0);
        DetectorPosition mon_position =  (DetectorPosition)dm.getAttributeValue(Attribute.DETECTOR_POS);
//        float[] mon_at = mon_position.getSphericalCoords();
//     mon_position.setSphericalCoords(-1.0f*mon_at[0], mon_at[1], mon_at[2]);
    
        System.out.println("mon_position: "+mon_position+" sample to monitor distance: "+mon_position.getSphericalCoords()[0]);

        AttributeList attr_list_m = dm.getAttributeList();
        float[] t_vals_m = dm.getX_scale().getXs();
        float[] y_vals_m = dm.getY_values();
        int ndetchannel, nmonchannel = y_vals_m.length;
        float tof_length_m = -(mon_position.getDistance())+((Float)attr_list_m.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
        System.out.println("tof_length: "+tof_length_m+" t_vals.length: "+t_vals_m.length);
         
        correct_DeadTime(beam_monitor);
        
        float delayed_neutron_sum = gdel_neut(t_vals_m, y_vals_m, tof_length_m, DNFRACT, SOURCE_PERIOD, DELAYED_NEUTRON_NFIT);
        for (int i = 0; i< nmonchannel; i++){
//          if (i%1000 == 1) System.out.println("Mon Channel "+i+": "+dm.getY_values()[i]);      
          y_vals_m[i] -= delayed_neutron_sum;
//          if (i%1000 == 1) System.out.println("Mon Channel "+i+" delayed neutron corrected: "+dm.getY_values()[i]);      
        }
                
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
          dt_grp_params = new Float1DAttribute (GLAD_PARM, new float[] {scattering_angle, d2, domega, psi});
          dt.setAttribute(dt_grp_params);
          tof_length_d = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue()+d2;
          t_vals_d = dt.getX_scale().getXs();
          y_vals_d = dt.getY_values();
          ndetchannel = y_vals_d.length;

//          if (i == 0) System.out.println("deadtime t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
          delayed_neutron_sum = gdel_neut(t_vals_d, y_vals_d, tof_length_d, DNFRACT, SOURCE_PERIOD, DELAYED_NEUTRON_NFIT);
          for (int k =0; k < ndetchannel; k++){
//            if (i==409 && k%100 == 1) System.out.println("Det Channel "+k+": "+dt.getY_values()[k]); 
            y_vals_d[k] -= delayed_neutron_sum;
//            if (i==409 && k%100 == 1) System.out.println("Det Channel "+k+" delayed neutron corrected: "+dt.getY_values()[k]);  
        
          }
//          if (i == 0) System.out.println("del_neu t_vals_d[1078]: "+t_vals_d[1078]+" y_vals_d[1077]: "+y_vals_d[1077]);       
        }
        
//        DataSet ds_grp2 = glad_group2(ds);
        glad_norm (ds, dm, ISvan);
//        DataSet ds_grp3 = glad_group3(ds_grp2);
        ds.setTitle(ds.getTitle()+" "+"--->CRUNCH");
        return ds;
        
  }
  
  public static DataSet glad_group2 (DataSet ds){
    DataSet ds_grp = ds.empty_clone();
  	float dtheta = 0.6f;
  	float t2, d2, domega, psi;
  	
  	Data dt;
    AttributeList attr_list_d;
    float[] data_params = new float[4];   
  	
  	int ndata = ds.getNum_entries();
    float[] t2_list = new float[ndata], domega_list = new float[ndata], d2_list = new float[ndata], psi_list = new float[ndata];
    for (int i = 0; i < ndata; i++) {
      dt = ds.getData_entry(i);
      attr_list_d = dt.getAttributeList();
      data_params = (float[])attr_list_d.getAttributeValue(GLAD_PARM);
      t2_list[i] = data_params[0]*180.0f/(float)Math.PI;
      d2_list[i] = data_params[1];
      domega_list[i] = data_params[2];
      psi_list[i] = data_params[3];
    }
    
    float t2_min = 90, t2_max = 90;
    for (int i=0; i < ndata; i++){
      t2 = t2_list[i];
      if (t2 < t2_min) t2_min = t2;
      else if (t2 > t2_max) t2_max = t2;
    }
    
    float t2_lbd = (float)(Math.floor(t2_min*10.0f))/10.0f;
    int ngrp = (int)Math.ceil((t2_max-t2_lbd)/dtheta);
    System.out.println("\nt2_min: "+t2_min+" t2_max:"+t2_max);
    System.out.println("2theta grouping "+ndata+" dataset groups to "+ngrp+" groups.");
    System.out.println("starting at "+t2_lbd+"\n");
  	
    ArrayList[] grp2DS = new ArrayList[ngrp];
    for (int i=0; i<ngrp; i++){
      grp2DS[i] = new ArrayList();
    }
    int index_grp;
    for (int i=0; i < ndata; i++){
      index_grp = ((int)Math.ceil((t2_list[i]-t2_lbd)/dtheta))-1;
      grp2DS[index_grp].add((new Integer(i)));
    }
    
    int[][] grp2DSindex = new int[ngrp][];   
    String ds_grp_list;
    int msgrp, index_msgrp;
    for (int i=0; i <ngrp; i++){
      ds_grp_list = "";
      if (grp2DS[i].isEmpty()) {
        grp2DSindex[i] = null;
        ds_grp_list = "empty";
      } else {
        msgrp = grp2DS[i].size();
        grp2DSindex[i] = new int[msgrp];
        for (int j=0; j <msgrp; j++){
          index_msgrp = ((Integer) grp2DS[i].get(j)).intValue();
          grp2DSindex[i][j] = index_msgrp;
          ds_grp_list += ds.getData_entry(index_msgrp).getGroup_ID()+" ";  
        }
      }
      System.out.println("2theta group "+i+": "+ds_grp_list);    
    }
    
    Data dt_grp;
    Float1DAttribute dt_grp_params;
    String dt_grp_params_list;
    float[] y_vals_grp, y_vals;
    int ndetchannels;
    int index;
    for (int i=0; i<ngrp; i++){
      if(grp2DSindex[i]!=null){
        dt_grp_params_list = "";
        msgrp = grp2DSindex[i].length;
        index = grp2DSindex[i][0];
        dt_grp = ds.getData_entry(index);
        y_vals_grp = dt_grp.getY_values();
        ndetchannels = y_vals_grp.length;
        t2 = t2_list[index];
        d2 = d2_list[index];
        domega = domega_list[index];
        psi = psi_list[index];
        if(msgrp > 1) {
          for (int j=1; j<msgrp; j++){
            index = grp2DSindex[i][j];
            dt = ds.getData_entry(index);
            y_vals = dt.getY_values();
            t2 += t2_list[index];
            d2 += d2_list[index];
            domega += domega_list[index];
            psi += psi_list[index];
            for (int k=0; k < ndetchannels; k++) {
                y_vals_grp[k] += y_vals[k];  
            }
          }
          t2 /= msgrp;
          d2 /= msgrp; 
          psi /= msgrp;
        }
        dt_grp_params_list += "2theta group "+i+" t2: "+t2+" d2: "+d2+" domega: "+domega+" psi: "+psi;
        dt_grp_params = new Float1DAttribute (GLAD_PARM, new float[] {t2*(float)Math.PI/180, d2, domega, psi});
        dt_grp.setAttribute(dt_grp_params);
        ds_grp.addData_entry(dt_grp);
        System.out.println("dt_grp "+i+": "+dt_grp_params_list);
      }
    }
    
  return ds_grp;
  }
  
  public static DataSet glad_group3 (DataSet ds) {
    DataSet ds_grp = ds.empty_clone();
    float t2min = 8.0f, t2max =125.0f;
    float wmin = 0.1f, wmax = 6.0f, Qlow = 0.0f, Qhigh = 1000.0f;
    float gapsz = 10.0f*(float)Math.PI/180.0f;
    int nclst = 19;
    ArrayList[] grp2ds = new ArrayList[nclst];
  
    Data dt;
    AttributeList attr_list_d;
    
    int ndata = ds.getNum_entries(), index_min = 0, index_max = ndata-1;
    float[] cos_list = new float[ndata];
    float csz = 0.0f, t2, cos, temp1, temp2;
    for (int i = 0; i < ndata; i++) {
      dt = ds.getData_entry(i);
      attr_list_d = dt.getAttributeList();
      t2 = ( (float[]) attr_list_d.getAttributeValue(GLAD_PARM) )[0]*180/(float)Math.PI;
      if (t2 < t2min) {
        index_min++;
        continue;
      }
      if (t2 > t2max) {
        index_max = i;
        break; 
      }
      cos = (float)Math.cos( t2*(float)Math.PI/360 );
      cos_list[i] = cos;
      csz += cos;
    }
    csz /= nclst;
    temp1 = cos_list[index_min];
    temp2 = cos_list[index_min];
    System.out.println("\nUsing groups between index "+index_min+" and "+index_max+" out of "+ndata+" groups");
    
    
    int index = index_min, index_grp;
    for (int i=0; i < nclst; i++){
      grp2ds[i] = new ArrayList();
      temp1 += csz;

      while (temp2 <= temp1) {
        grp2ds[i].add(new Integer(index));
        if (index == index_max) break;
        temp2 += cos_list[++index];
      }
    }
    System.out.println("\n2theta grouping "+ndata+" dataset groups to "+nclst+" clusters.\n");
 
    int[][] grp2dsindex = new int[nclst][];   
    String ds_grp_list;
    int msgrp, index_msgrp;
    for (int i=0; i < nclst; i++){
    if (grp2ds[i].isEmpty()) System.out.println("\nUNEXPECTED ERROR: cluster empty\n");
    msgrp = grp2ds[i].size();
    ds_grp_list = "size "+msgrp+": ";
    grp2dsindex[i] = new int[msgrp];
    for (int j=0; j <msgrp; j++){
      index_msgrp = ((Integer) grp2ds[i].get(j)).intValue();
      grp2dsindex[i][j] = index_msgrp;
      ds_grp_list += ds.getData_entry(index_msgrp).getGroup_ID()+" ";  
    }
          
    System.out.println("2theta group "+i+", "+ds_grp_list);    
    }
    
    Data dt_grp, dm;
    float[] data_params = new float[4];
    Float1DAttribute dt_grp_params;
    AttributeList attr_list;
    String dt_grp_params_list;
    ClosedInterval Q_range_grp;
    XScale Q_scale = new UniformXScale(1.0f/NUMQ, GLADQMAX+1.0f/NUMQ, (int)GLADQMAX*NUMQ+1), Q_scale_grp;
    float[] Q_vals_m, W_vals_v, y_vals_v, y_vals, y_vals_sum, y_vals_grp,
                                                       y_vals_m, y_vals_m_sum;
    int ndetchannel = (int)GLADQMAX*NUMQ, nmonchannel, IDm, IDt;
    float Qval, dcs, wspec, sum, scattering_angle, scattering_angle_sum, d2, domega, psi;
    
    IDm = dm_van_W.getGroup_ID();
    W_vals_v = dm_van_W.getX_values();
    y_vals_v = dm_van_W.getCopyOfY_values();
    nmonchannel = y_vals_v.length;
    Q_vals_m = new float[nmonchannel+1];
    arrayUtil.Reverse( y_vals_v );
    
    for (int i=0; i<nclst; i++){
      dt_grp_params_list = "";
      msgrp = grp2dsindex[i].length;

      y_vals_sum = new float[ndetchannel];
      y_vals_m_sum = new float[ndetchannel];
      sum=0.0f;
      scattering_angle_sum = 0.0f;
      d2 = 0.0f;
      domega = 0.0f;
      psi = 0.0f;

      dt = ds.getData_entry(grp2dsindex[i][0]);
      attr_list = dt.getAttributeList();
      IDt = dt.getGroup_ID();   

      for (int j=0; j<msgrp; j++){
        Qlow = 0.0f;
        Qhigh = 1000.0f;
        index = grp2dsindex[i][j];
        dt = ds.getData_entry(index);
        data_params = (float[])dt.getAttributeValue(GLAD_PARM);
        wspec = 0.0f;
        scattering_angle = data_params[0];
        
        for (int k=0; k <= nmonchannel; k++){
          Q_vals_m[k] = tof_calc.DiffractometerQofWavelength(scattering_angle, W_vals_v[k]);
        }
        arrayUtil.Reverse(Q_vals_m);
        dm = Data.getInstance((new VariableXScale(Q_vals_m)), y_vals_v, IDm);
        dm.resample(Q_scale, IData.SMOOTH_NONE);
        y_vals_m = dm.getY_values();
/*        
        Qval = dt.getX_scale().getStart_x();
        if (Qlow < Qval) Qlow = Qval;
        Qval = dt.getX_scale().getEnd_x();
        if (Qhigh > Qval) Qhigh = Qval;
*/
        Qlow = dt.getX_scale().getStart_x();
        Qhigh = dt.getX_scale().getEnd_x();
         
        dt.resample(Q_scale, IData.SMOOTH_NONE);
        y_vals = dt.getY_values();
                
        for (int k=0; k < ndetchannel; k++) {
//          Qval = dt.getX_scale().getX(k);
//          if (Qval < Qlow || Qval > Qhigh) dcs = 0.0f;
//          else dcs = y_vals_m[k];
//          if (y_vals[k] == 0.0f && y_vals_m[k] !=0.0f) System.out.println("averaging: unexpected error at k: "+k);
          dcs = y_vals_m[k];
          y_vals_sum[k] += dcs*y_vals[k];
          y_vals_m_sum[k] += dcs;
          wspec += dcs;  
        }
        sum += wspec;
        scattering_angle_sum += wspec*data_params[0];
        d2 += wspec*data_params[1];
        domega += wspec*data_params[2];
        psi += wspec*data_params[3];
      }
      
      scattering_angle = scattering_angle_sum/sum;
      d2 /= sum;
      domega /= sum;
      psi /= sum;
      
      Qval = tof_calc.DiffractometerQofWavelength(scattering_angle, wmax);
      if (Qlow < Qval) Qlow = Qval;
      Qval = tof_calc.DiffractometerQofWavelength(scattering_angle, wmin);
      if (Qhigh > Qval) Qhigh = Qval;      
//      System.out.println("cluster "+i+" Qlow: "+Qlow+" Qhigh: "+Qhigh);
      
      for (int k=0; k < ndetchannel; k++){
        Qval = Q_scale.getX(k);
        if (Qval < Qlow || Qval > Qhigh) y_vals_sum[k] = 0.0f;
        else y_vals_sum[k] /= y_vals_m_sum[k];
      }
//      System.out.println("cluster "+i+" starting at "+kmin);
      

//      Q_range_grp = new ClosedInterval(kmin/NUMQ, GLADQMAX);
//      Q_scale_grp = new UniformXScale(kmin/NUMQ, GLADQMAX, ndetchannel-kmin+1);
//      y_vals_grp = new float[ndetchannel-kmin];
//      System.arraycopy(y_vals_sum, kmin, y_vals_grp, 0, ndetchannel-kmin);
//      System.out.println("#Q: "+Q_scale_grp.getNum_x()+" #y: "+y_vals_grp.length);
//      dt_grp = Data.getInstance(Q_scale_grp, y_vals_grp, IDt);
      dt_grp = Data.getInstance(Q_scale, y_vals_sum, IDt);
      dt_grp.setAttributeList(attr_list);
      dt_grp_params = new Float1DAttribute (GLAD_PARM, new float[] {scattering_angle, d2, domega, psi});     
      dt_grp.setAttribute(dt_grp_params);
      ds_grp.addData_entry(dt_grp);
            
      dt_grp_params_list += "2theta group "+i+" t2: "+scattering_angle*180/(float)Math.PI+" d2: "+d2+" domega: "+domega+" psi: "+psi;
      System.out.println("dt_grp "+i+": "+dt_grp_params_list);
    }
  return ds_grp;
  }

  public static void glad_norm (DataSet ds, Data dm, boolean ISvan){
    Data dt, new_dt_Q, new_dm_Q, new_dt_W, new_dm_W;
    DetectorPosition position;
    int nmonchannel, ndetchannel;
    float            tof_length_m, tof_length_d, scattering_angle, psi, domega_d;
    float            min_Q, max_Q;
    int               num_Q, IDm, IDd;
    float            t_vals_d[], y_vals_d[], W_vals_d[], Q_vals_d[],
                        t_vals_m[], y_vals_m[], W_vals_m[], Q_vals_m[], 
                        y_vals_n[]; 
    float[] data_params = new float[4];   
    XScale        W_scale_d, W_scale_m, Q_scale_d, Q_scale_m, new_Q_scale;
    AttributeList    attr_list_d, attr_list_m;
    
    float [] effd;
    float cc;
    float min_W = 2.0f, max_W = 2.0f;                        
    
    IDm = dm.getGroup_ID();   
    DetectorPosition mon_position =  (DetectorPosition)dm.getAttributeValue(Attribute.DETECTOR_POS);
//    float[] mon_at = mon_position.getSphericalCoords();
//    mon_position.setSphericalCoords(-1.0f*mon_at[0], mon_at[1], mon_at[2]);
    
    System.out.println("mon_position: "+mon_position+" sample to monitor distance: "+mon_position.getSphericalCoords()[0]);

    attr_list_m = dm.getAttributeList();
    t_vals_m = dm.getX_scale().getXs();
    y_vals_m = dm.getY_values();
    nmonchannel = y_vals_m.length;
//    tof_length_m = -(mon_position.getSphericalCoords())[0]+((Float)attr_list_m.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
    tof_length_m = -(mon_position.getDistance())+((Float)attr_list_m.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
    System.out.println("tof_length_m: "+tof_length_m+" t_vals.length_m: "+t_vals_m.length);
        
    W_vals_m = new float[nmonchannel+1];   
//    Q_vals_m = new float[nmonchannel+1]; 
    
    for (int k = 0; k <= nmonchannel; k++){
      W_vals_m[k] = tof_calc.Wavelength(tof_length_m, t_vals_m[k]);
    }
//      System.out.println("W_vals_m[1]: "+W_vals_m[1]+" y_vals_m: "+y_vals_m[1]);
//      System.out.println("W_vals_m[14950]: "+W_vals_m[14950]+" y_vals_m[14949]: "+y_vals_m[14949]);
//      System.out.println("y_vals_m at 1.8: "+arrayUtil.interpolate(1.8f, W_vals_m, y_vals_m));
    
    min_Q = 1.0f/NUMQ;
    max_Q = GLADQMAX + 1.0f/NUMQ; 
    num_Q = (int)GLADQMAX*NUMQ+1;
    new_Q_scale = new UniformXScale(min_Q, max_Q, num_Q);
    
    for (int i = 0; i < ds.getNum_entries(); i++){
      dt = ds.getData_entry(i);
      IDd = dt.getGroup_ID();
      attr_list_d = dt.getAttributeList();
/*      domega_d = ((Float)attr_list_d.getAttributeValue(Attribute.SOLID_ANGLE)).floatValue();
      position = (DetectorPosition)attr_list_d.getAttributeValue(Attribute.DETECTOR_POS);
      scattering_angle = position.getScatteringAngle();
      psi = (float)(Math.PI/2-(position.getSphericalCoords())[2]);     
      tof_length_d = (position.getSphericalCoords())[0]+((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
*/
      data_params = (float[])attr_list_d.getAttributeValue(GLAD_PARM);
      scattering_angle = data_params[0];
      tof_length_d = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue()+data_params[1];
      domega_d = data_params[2];
      psi = data_params[3];
      
      t_vals_d = dt.getX_scale().getXs();
      y_vals_d = dt.getY_values();
      effd = geffcyl(t_vals_d, tof_length_d, psi); 
      
      ndetchannel = y_vals_d.length;
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

            if (W_vals_d[0] < min_W ) min_W = W_vals_d[0];
            if (W_vals_d[ndetchannel] > max_W ) max_W = W_vals_d[ndetchannel];
      W_scale_d = new VariableXScale( W_vals_d );
      W_scale_m = new VariableXScale(W_vals_m);
      new_dt_W = Data.getInstance(W_scale_d, y_vals_d, IDd);
      new_dm_W = Data.getInstance(W_scale_m, y_vals_m, IDm);
      if (ISvan) dm_van_W = (Data)new_dm_W.clone();
      new_dm_W.resample(W_scale_d,IData.SMOOTH_NONE);
      y_vals_n = new_dt_W.getY_values();
          
      for(int k = 0; k < ndetchannel; k++){
        cc = AREAM[0]*EFFM[0]*.5f*(W_vals_d[k]+W_vals_d[k+1])/(domega_d*effd[k]);
//        y_vals_n[k] *= cc;
        y_vals_n[k] /= (new_dm_W.getY_values()[k]/cc);
//             y_vals_n[k] /= CylAbsTof.getCoeff(scattering_angle, W_vals_d[k]);
//              y_vals_n[k] *= CylMulTof.getCoeff(scattering_angle, W_vals_d[k]);
           
      }
                
      arrayUtil.Reverse( Q_vals_d );
      arrayUtil.Reverse(y_vals_n);
      Q_scale_d = new VariableXScale( Q_vals_d );
//      Q_scale_m = new VariableXScale(Q_vals_m);
//      min_Q = (float)Math.ceil(Q_vals_d[0]*NUMQ)/NUMQ;
//      max_Q = (float)Math.floor(Q_vals_d[ndetchannel]*NUMQ)/NUMQ;

//      if (max_Q > GLADQMAX) max_Q = GLADQMAX;
//      System.out.println("Q_vals_d[0]: "+Q_vals_d[0]+" Q_vals_d[ndetchannel]: "+Q_vals_d[ndetchannel]+" min_Q: "+min_Q+" max_Q: "+max_Q);
//      num_Q = (int)(NUMQ*(max_Q-min_Q)+1);
//      new_Q_scale = new UniformXScale(min_Q, max_Q, num_Q);
      new_dt_Q = Data.getInstance(Q_scale_d, y_vals_n, IDd);
//      new_dm_Q = Data.getInstance(Q_scale_m, y_vals_m, 1000+Igrp);
      new_dt_Q.setAttributeList( attr_list_d );
      new_dt_Q.resample( new_Q_scale, IData.SMOOTH_NONE );
                    
      ds.replaceData_entry(new_dt_Q, i);       
    }
    ds.setTitle(ds.getTitle()+" "+(String)ds.getAttributeValue(Attribute.RUN_TITLE));
    ds.setX_units("/"+FontUtil.ANGSTROM);
    ds.setX_label( "Q" );
//    ds.setX_units("FontUtil.ANGSTROM");
//    ds.setX_label( "wavelength" ); 
    ds.setY_label("Normalized Intensity");
          System.out.println("min_W: "+min_W+" max_W: "+max_W);
  }
  
  public static void glad_coral (String[][] targets, float[][] formulas, 
                            float Sht, float[] radii, float[] density, float Bwid, float Bht,
                            float mul_astep, float abs_astep, boolean IScan) {
    
/*    
    try {
      MutCross.run(MutCross.sigmatable);    
      
      if (IScan) {
        String[][] targets_can = new String[][] {targets[1]};
        float[][] formulas_can = new float[][]  {formulas[1]};
        float[] radii_can = new float[] {radii[1], radii[2] };
        float[] density_can = new float[] {density[1]};

        String MulSetup = MutCross.MulAbsInputMaker(targets_can, formulas_can, 
                                       Sht, radii_can, density_can, Bwid, Bht, mul_astep, null, 0.1f, 4.3f, 0.1f);
        StringReader input_cylmulin = new StringReader(MulSetup);
        CylMulTof.run(input_cylmulin);
        
        String AbsSetup = MutCross.MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, abs_astep, null, 0.1f, 4.3f, 0.1f);
        StringReader input_cylabsin = new StringReader(AbsSetup);
        CylAbsTof.run(input_cylabsin, radii.length-1);
      } else{
        
        String MulSetup = MutCross.MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, mul_astep, null, 0.1f, 4.3f, 0.1f);
        StringReader input_cylmulin = new StringReader(MulSetup);
        CylMulTof.run(input_cylmulin); 
        
        String AbsSetup = MutCross.MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, abs_astep, null, 0.1f, 4.3f, 0.1f);
        StringReader input_cylabsin = new StringReader(AbsSetup);
        CylAbsTof.run(input_cylabsin, radii.length-1);       
        
      }
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace();
      }    
*/
    }
  

  public static void glad_analyse (DataSet ds, int ITYPE) {
//  String input_cylmulin = "/IPNShome/taoj/GLAD/coral/cylindrical/cylmul.in";
//  String input_cylabsin = "/IPNShome/taoj/GLAD/coral/cylindrical/cylabs.in";
//  String input_mut = "/IPNShome/taoj/GLAD/coral/cylindrical/GLD08094.MUT";
   
/*        
    Data dt;
//    DetectorPosition position;
    AttributeList attr_list_d;
    float scattering_angle, q, lambda;
    float[] qlist, y_vals_n, mul, abs;
    int istart, iend;
    if (ds.getX_label() != "Q") System.out.println("******ERROR******");
    for (int i = 0; i < ds.getNum_entries(); i++) {
      dt = ds.getData_entry(i);
      attr_list_d = dt.getAttributeList();
//      position = (DetectorPosition)attr_list_d.getAttributeValue(Attribute.DETECTOR_POS);
//      scattering_angle = position.getScatteringAngle();
      scattering_angle = ((float[])attr_list_d.getAttributeValue(GLAD_PARM))[0];
 
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
//          if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);
          mul = CylMulTof.getCoeff(scattering_angle, lambda);
          abs = CylAbsTof.getCoeff(scattering_angle, lambda);
      
//        y_vals_n[k] *= mul[0]/(mul[0]+mul[1]);
//        if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);

        
        if (ITYPE == 2) {//outer shells: (can or furnace);
//          if (y_vals_n[k] != 0.0f) y_vals_n[k] -= mul[1];
          y_vals_n[k] -= mul[1];
//        if (k==0) System.out.println("cell mul[1]: "+mul[1]+" abs[3]: "+abs[3]+" abs[2]: "+abs[2]);     
          y_vals_n[k] /= abs[3];
          y_vals_n[k] *= abs[2];
        }
        else if (ITYPE == 1) {//inner core:  calibration rod (vanadim, fused silica), or (sample+can) with (can) part subtracted;
//        if (k==0) System.out.println("core mul[1]"+mul[1]+" abs[1]: "+abs[1]);
//          if (y_vals_n[k] != 0.0f) y_vals_n[k] -= mul[1];
          y_vals_n[k] -= mul[1];
          y_vals_n[k] /= abs[1];
          y_vals_n[k] /= MutCross.Nscatterers;
//          if (y_vals_n[k] < y_vals_min) y_vals_min=y_vals_n[k];
//
//         if (y_vals_n[k] > 10.0f || y_vals_n[k] < -10.0f) {
//            y_vals_n[k]=0.0f;
//            System.out.println("\n"+"abnormal values: "+i+"\n");
//          } //temporary fix;
//
                }
//        if (k == 0) System.out.println("i: "+i+" y_vals_n[0]: "+y_vals_n[k]);
      } 
    }
        
    ds.setTitle(ds.getTitle()+" "+"--->CORAL/ANALYSE");
//    ds.setY_units("barns");
    ds.setY_label("Time-Of-Flight differential crosssection");
*/       
  }
  
  public static void glad_vancal (DataSet nrm_van, float temperature, boolean ISsmooth ){
    
    if (ISsmooth) {
      System.out.println("******STARTING FOURIER FILTERING******");
      if (ISsmooth) (new LowPassFilterDS0(nrm_van, 0.05f, 2)).getResult();
      System.out.println("******COMPLETE******");
    }
/*    
    try{
      String[][] targets = {{"V"}};
      float[][] formulas = {{1.0f}};
      float Sht = 6.00f, Bwid = 1.02f, Bht = 2.30f;
      float[] radii = {0.0f, 0.4763f};
//      float[] radii = {0.0f, 0.3175f};
//      Sht = 4.1f;
      float[] density = {0.07205f};
      float[] sigma_a = {5.08f};
      float astep = 0.1f;
            
      MutCross.run(MutCross.sigmatable);    
      String MulSetup = MutCross.MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, astep, null, 0.1f, 4.3f, 0.1f);
      StringReader input_cylmulin = new StringReader(MulSetup);
      CylMulTof.run(input_cylmulin);
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace();

      }
*/      
    String[] target =  {"V"};
    float[] formula = {1.0f};
    Data dt;
    AttributeList attr_list_d;
//    DetectorPosition position;
    float scattering_angle, d1, d2, lambda, q, p;
    float[] Q_vals_d, y_vals_n, mul = new float[2], data_params = new float[4];
    int ndetchannel;
    
    for (int i = 0; i < nrm_van.getNum_entries(); i++){
      dt = nrm_van.getData_entry(i);
      attr_list_d = dt.getAttributeList();
      
      
//      position = (DetectorPosition)attr_list_d.getAttributeValue(Attribute.DETECTOR_POS);
//      scattering_angle = position.getScatteringAngle();
//      d1 =  ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
//      d2 =  (position.getSphericalCoords())[0];
 //     System.out.println("d1: "+d1+" d2: "+d2); 
       data_params = (float[])attr_list_d.getAttributeValue(GLAD_PARM);
       scattering_angle = data_params[0];
       d1 = ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
       d2 = data_params[1];
       
      Q_vals_d = dt.getX_scale().getXs();
      y_vals_n = dt.getY_values();
      ndetchannel = y_vals_n.length ;
//      System.out.println("ndetchannel: "+ndetchannel);
      for (int k = 0; k < ndetchannel; k++){
        q = 0.5f*(Q_vals_d[k]+Q_vals_d[k+1]);
        lambda = tof_calc.WavelengthofDiffractometerQ(scattering_angle, q);
        p = Platom.plaatom(lambda, target, formula, temperature, scattering_angle, d1, d2, true);
//        mul = CylMulTof.getCoeff(scattering_angle, lambda);
        y_vals_n[k] /= mul[0]*p+mul[1];
      }
      
      
       
    }
    nrm_van.setTitle(nrm_van.getTitle()+" "+"--->VANCAL");
//    nrm_van.setY_units("1/barns");
    nrm_van.setY_label("vanadium calibration function");
  }
  
  public static void glad_interefere_flux (DataSet dcs_smp, DataSet smo_van,
                                                                             String[] target, float[] formula, float temperature,
                                                                             float Wmin, float Wmax) {
    
    
        Data dt, dv, van_dm_Q;
        AttributeList attr_list_d, attr_list_v;
//        DetectorPosition position_d, position_v;
        float scattering_angle_d, scattering_angle_v, d1, d2, lambda_d, q_d, lambda_v, q_v, p;
        float tenfactor, ymax;
        float[] Q_vals_d, y_vals_n, Q_vals_v, y_vals_v, W_vals_vm, Q_vals_vm, y_vals_vm;
        float[] data_params_d = new float[4], data_params_v = new float[4];
        XScale Q_scale_vm;
        int ngrps, ndetchannel, nmonchannel;
        int istart, iend;
    
        ngrps = dcs_smp.getNum_entries();
        if (smo_van.getNum_entries()!=ngrps) System.out.println("***ERROR---dcs and smo have different number of groups***");
        
        W_vals_vm = dm_van_W.getX_values();
        y_vals_vm = dm_van_W.getCopyOfY_values();
        nmonchannel = y_vals_vm.length;
        Q_vals_vm = new float[nmonchannel+1];
        arrayUtil.Reverse( y_vals_vm );
        
        for (int i = 0; i < ngrps; i++){
          dt = dcs_smp.getData_entry(i);
          dv = smo_van.getData_entry(i);
          attr_list_d = dt.getAttributeList();
          attr_list_v = dv.getAttributeList();
//          position_d = (DetectorPosition)attr_list_d.getAttributeValue(Attribute.DETECTOR_POS);
//          position_v = (DetectorPosition)attr_list_v.getAttributeValue(Attribute.DETECTOR_POS);
//          scattering_angle_d = position_d.getScatteringAngle();
 //         scattering_angle_v = position_v.getScatteringAngle();
//          System.out.println("scattering_angle_v: "+scattering_angle_v);
          d1 =  ((Float)attr_list_d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
//          d2 =  (position_d.getSphericalCoords())[0];
     //     System.out.println("d1: "+d1+" d2: "+d2); 
          
          data_params_v = (float[])attr_list_v.getAttributeValue(GLAD_PARM);
          scattering_angle_v = data_params_v[0];
          data_params_d = (float[])attr_list_d.getAttributeValue(GLAD_PARM);
          scattering_angle_d = data_params_d[0];
          d2 = data_params_v[1];
          if (data_params_d[1] != d2) System.out.println("\n******ERROR: vanadium and sample data params not matched.******\n"+
                                                                                           "data_params_d[1]: "+data_params_d[1]+" d2: "+d2);
          
          Q_vals_d = dt.getX_scale().getXs();
          Q_vals_v = dv.getX_scale().getXs();
          y_vals_n = dt.getY_values();
          y_vals_v = dv.getY_values();
          
          istart = 0;
          iend = y_vals_n.length-1;
          if (iend != (y_vals_v.length-1)) System.out.println("***ERROR---dcs and smo unmatched***");
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
          van_dm_Q = Data.getInstance(Q_scale_vm, y_vals_vm, dv.getGroup_ID());
          van_dm_Q.resample( dv.getX_scale(), IData.SMOOTH_NONE );
          
//          ymax = 0.0f;
          
          for (int k = istart; k < iend; k++){
            q_d = 0.5f*(Q_vals_d[k]+Q_vals_d[k+1]);
            lambda_d = tof_calc.WavelengthofDiffractometerQ(scattering_angle_d, q_d);
            p = Platom.plaatom(lambda_d, target, formula, temperature, scattering_angle_d, d1, d2, false);
//            if (y_vals_n[k] != 0.0f) y_vals_n[k] -= p;
            y_vals_n[k] -= p;
            
            q_v = 0.5f*(Q_vals_v[k]+Q_vals_v[k+1]);
            lambda_v = tof_calc.WavelengthofDiffractometerQ(scattering_angle_v, q_v);            
            if (lambda_v < Wmin || lambda_v > Wmax) y_vals_v[k] = 0.0f;
            else y_vals_v[k] *= van_dm_Q.getY_values()[k];
//           p = y_vals_v[k];
//            if (p > ymax) ymax = p;
           
          }
/*          
          tenfactor = 1.0f;
          while (ymax > 1.0e5) {
            ymax /= 10.0f;
            tenfactor *= 10.0f;     
          }
          
          for (int k = 0; k < ndetchannel; k++){
            y_vals_v[k] /= tenfactor;
          }
          System.out.println("grp "+i+"'s flux scaled by "+tenfactor);
*/          
        }

                                

        dcs_smp.setTitle(dcs_smp.getTitle()+" "+"--->INTERFERE");
        smo_van.setTitle(smo_van.getTitle()+" "+"--->FLUX");
        dcs_smp.setY_label("distinct scattering");
        smo_van.setY_units("counts");
        smo_van.setY_label("calculated vanadium scattering");
  }
  
  public static void glad_combine(DataSet int_smp, DataSet flx_van){
    Data dt, dv, dioq, dsum;
    XScale Q_scale;
    int numq;
    float flag;
    float[] Q_vals, y_vals_int, y_vals_flx, y_vals_ioq, y_vals_sum;
/*   
    Q_scale = new UniformXScale(0.0f, GLADQMAX, 1601);
    dioq = Data.getInstance(Q_scale, new float[1600], 1000);
    dsum = Data.getInstance(Q_scale, new float[1600], 1001);
*/  
    numq = (int)GLADQMAX*NUMQ+1;   
    Q_scale = new UniformXScale(1.0f/NUMQ, GLADQMAX+1.0f/NUMQ, numq);
    dioq = Data.getInstance(Q_scale, new float[numq-1], 1000);
    dsum = Data.getInstance(Q_scale, new float[numq-1], 1001);
    
//    AttributeList attr_list_v;
//    DetectorPosition position;
//    float scattering_angle;
//    int ngrps = 0;
    
    for (int i = 0; i < flx_van.getNum_entries(); i++){
      dv = flx_van.getData_entry(i);
      
//      attr_list_v = dv.getAttributeList();
//      position = (DetectorPosition)attr_list_v.getAttributeValue(Attribute.DETECTOR_POS);
//      scattering_angle = position.getScatteringAngle();
//      scattering_angle = ((float[])attr_list_v.getAttributeValue(GLAD_PARM))[0];

//      ngrps += 1;
      
//if (scattering_angle < 17*Math.PI/180) {    
      
      dsum = dsum.stitch(dv, Data.SUM);
      
      dt = int_smp.getData_entry(i);
      Q_vals = dt.getX_scale().getXs();
      y_vals_int = dt.getY_values();
      y_vals_flx = dv.getY_values();
      for (int k = 0; k < Q_vals.length-1; k++){
        y_vals_flx[k] *= y_vals_int[k];
      }
      dioq = dioq.stitch(dv, Data.SUM);
//}
    }
    
//    System.out.println("\n"+"number of groups weighted: "+ngrps+"\n");
    
    y_vals_ioq = dioq.getY_values();
    y_vals_sum = dsum.getY_values();
    
    for (int i = 0; i < numq-1; i++){
      if (y_vals_sum[i] != 0){
        y_vals_ioq[i] /= y_vals_sum[i];  
      }
    }
    
    int_smp.addData_entry(dioq);
    int_smp.setTitle(int_smp.getTitle()+" "+"--->COMBINE");
    int_smp.setY_label("distinct scattering");
  }
  
  public static void main(String[] args) {
      
    String runfilefolder = SharedData.getProperty( "Data_Directory" ) + java.io.File.separator;   
    
    
    String background_run = runfilefolder+"glad8093.run";
    String vanadium_run = runfilefolder+"glad8094.run";
    String sample_run = runfilefolder+"glad8095.run";

/*
    String background_run = runfilefolder+"glad9006.run";
    String vanadium_run = runfilefolder+"glad9007.run";
    String can_run = runfilefolder+"glad9010.run";
    String sample_run = runfilefolder+"glad9011.run";
*/
/*
     String background_run = runfilefolder+"glad7821.run";
     String vanadium_run = runfilefolder+"glad7867.run";
     String can_run = runfilefolder+"glad8266.run";
     String sample_run = runfilefolder+"glad8086.run";
*/ 
/* 
      String background_run = runfilefolder+"glad9006.run";
      String vanadium_run = runfilefolder+"glad9007.run";
      String can_run = runfilefolder+"glad9033.run";
      String sample_run = runfilefolder+"glad9027.run";
*/          

    DataSet nrm_van = glad_crunch(vanadium_run, true);

    DataSet nrm_smp = glad_crunch(sample_run, false);
 

    
    DataSet nrm_bkg = glad_crunch(background_run, false);
    
    String msg = (String)(new DataSetSubtract(nrm_van, nrm_bkg, false)).getResult();
    System.out.println(msg);
    glad_vancal(nrm_van, 300, false);   
//    ViewManager view_manager0 = new ViewManager(ds_van, IViewManager.IMAGE);     

    msg = (String) (new DataSetSubtract(nrm_smp, nrm_bkg, false)).getResult();
    msg += "\n"+(String) (new DataSetDivide(nrm_smp, nrm_van, false)).getResult();
    System.out.println(msg);
    nrm_smp.setY_units("barns");
//   ViewManager view_manager1 = new ViewManager(nrm_smp, IViewManager.IMAGE);
    
//   DataSet nrm_can = glad_crunch(can_run, false);
//   msg = (String) (new DataSetSubtract(nrm_can, nrm_bkg, false)).getResult();
//   msg += "\n"+(String) (new DataSetDivide(nrm_can, nrm_van, false)).getResult();
   System.out.println(msg);
//    nrm_can.setY_units("barns");
//  ViewManager view_manager2 = new ViewManager(nrm_can, IViewManager.IMAGE);
    
/*                         
                         String[][] targets = {{"V"}};
                         float[][] formulas = {{1.0f}};
                         float[] radii = {0.0f, 0.4763f};
                         float[] density = {0.07205f};
                         
                         float mul_astep = 0.1f, abs_astep = 0.02f;
                         float Sht = 6.00f, Bwid = 1.02f, Bht = 1.27f;
*/
    
/*
                      int ITYPE = 1;
                     String[][] targets = {{"Si", "O"}};
                     float[][] formulas = {{1.0f, 2.0f}};
                     float[] radii = {0.0f, 0.4415f};
                     float[] density = {0.0662f};
//                     float[] sigma_a = {0.0058f};
                     float mul_astep = 0.1f, abs_astep = 0.02f;
                     float Sht = 6.00f, Bwid = 1.02f, Bht = 2.30f;
*/
      
/*                   
                    String[][] targets = {{"C", "Cl"},{"V"}};
                    float[][] formulas = {{1.0f, 4.0f},{1.0f}};
                    float[] radii = {0.0f, 0.3048f, 0.3175f};
                    float[] density = {0.0319f, 0.07205f};
                    float[] sigma_a = {26.83183f, 5.08f};
                    float mul_astep = 0.1f, abs_astep = 0.02f;
                    float Sht = 4.1f, Bwid = 1.02f, Bht = 2.30f;
*/
/* 
                    String[][] targets = {{"C"}, {"V"}};
                    float[][] formulas = {{1.0f}, {1.0f}};
                    float[] radii = {0.0f, 0.4636f, 0.4763f};
                    float[] density = {0.0207f, 0.07205f};
                    float[] sigma_a = {0.0035f, 5.08f};  
                    float mul_astep = 0.1f, abs_astep = 0.02f;
                    float Sht = 6.0f, Bwid = 1.02f, Bht = 2.30f;
*/                       
                              
                    String[][] targets = {{"Ni"}, {"V"}};
                    float[][] formulas = {{1.0f}, {1.0f}};
                    float[] radii = {0.0f, 0.4636f, 0.4763f};
                    float[] density = {0.01f, 0.07205f};
                    float[] sigma_a = {4.49f, 5.08f};  
                    float mul_astep = 0.1f, abs_astep = 0.02f;
                    float Sht = 6.0f, Bwid = 1.02f, Bht = 2.30f;
                    
      glad_coral (targets,  formulas, Sht, radii, density, Bwid, Bht, mul_astep, abs_astep, true);   
           
//      glad_analyse(nrm_can, 2);
      
//    ViewManager view_manager3 = new ViewManager(nrm_can, IViewManager.IMAGE);
      
    
                        
                        
    glad_coral (targets,  formulas, Sht, radii, density, Bwid, Bht, mul_astep, abs_astep, false);                     
//      nrm_smp = (DataSet) (new DataSetSubtract(nrm_smp, nrm_can, true)).getResult();
      glad_analyse(nrm_smp, 1);
      
//  ViewManager view_manager4 = new ViewManager(nrm_smp, IViewManager.IMAGE);
    
    glad_interefere_flux (nrm_smp, nrm_van, targets[0], formulas[0], 300, 0.1f, 6.0f);
//    ViewManager view_manager5 = new ViewManager(nrm_van, IViewManager.IMAGE);
    
    glad_combine (nrm_smp, nrm_van);
     
    

  ViewManager view_manager6 = new ViewManager(nrm_smp, IViewManager.IMAGE);
   

  }

}
