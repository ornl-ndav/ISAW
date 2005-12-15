/*
 * File:  Ftr.java
 *
 * Copyright (C) 2004 J. Tao
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Genernal Public License
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
 * Revision 1.3  2005/12/15 20:52:55  dennis
 * Added tag for CVS logging so that future modifications can be tracked.
 *
 */


package Operators.TOF_Diffractometer;

import DataSetTools.dataset.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jnt.FFT.*;

/**
 * @author taoj
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Ftr { 
  
  public static final int NF = 16384; //NUMQ = 40;

  private float qstart, qend, delq;
  private int npt;
  private float[] qs, ioqs, soqs;
  private float[] rs, dors;
  private float NumDensity;
  
  public Ftr(Data dioq, float bbarsq, float numq) {
    float[] xs = dioq.getX_values();
    float[] ys = dioq.getCopyOfY_values();
    int n = ys.length;
    
    int istart = 0;
    while (ys[istart] == 0.0f) istart++;
    n -= istart;
    qs =  new float[n];
    ioqs = new float[n];
    
    System.arraycopy(xs, istart, qs, 0, n);
    System.arraycopy(ys, istart, ioqs, 0, n); 
         
    qstart = qs[0];
    delq = 1.0f/numq;
    npt = n;
    convertIofQ2SofQ(bbarsq);
    System.out.println("qstart: "+qstart+" delq: "+delq+" npt: "+npt);
  }
  
 private Ftr (String file_isoq) {
    try {
      getGLADISOQ (file_isoq);      
    } catch(Throwable t) {
      System.out.println("unexpected error: getGLADISOQ()");
      t.printStackTrace();
    }
  }
  
  public static Ftr parseGLADISOQ (String file_isoq) {
    return new Ftr(file_isoq);
  }
  
  private void getGLADISOQ (String file_isoq) throws IOException, InterruptedException{
         
    BufferedReader fr_input = new BufferedReader(new FileReader(file_isoq));
    String line;
    String[] list;
    
    line = fr_input.readLine();
    Matcher m = Pattern.compile("start\\s=\\s+(\\d*\\.?\\d*)\\s+end\\s=\\s+(\\d*\\.?\\d*)\\s+npt=\\s+(\\d+)").matcher(line);
    if (m.find()) {
      qstart = (new Float(m.group(1))).floatValue();
      qend = (new Float(m.group(2))).floatValue();
      npt = (new Integer(m.group(3))).intValue();
      delq = (qend-qstart)/(npt-1);
      System.out.println("start="+qstart);
      System.out.println("end="+qend);
      System.out.println("delq="+delq);
      System.out.println("npt="+npt);
    }
    else System.out.println("\n******UNEXPECTED ERROR******\n");
    for (int i = 0; i<3; i++){
      fr_input.readLine();
    }
    
    qs = new float[npt];
    soqs = new float[npt];     
    for (int i = 0; i<npt; i++) {
      line = fr_input.readLine();
      list = Pattern.compile("\\s+").split(line.trim());
      qs[i] = (new Float(list[1])).floatValue();
      soqs[i] = (new Float(list[2])).floatValue(); 
    }

    fr_input.close();
  }
  
  float[] getQ () {
    return qs;
  }
  
  float[] getSofQ () {
    return soqs;      
  }
  
  void convertIofQ2SofQ (float bbarsq) {
//    Object[] list_composition = GLADScatter.parseComposition(composition);
//    String[] list_elements = (String[])list_composition[0];
//    float[] list_fractions = (float[])list_composition[1];
//    float i2s = MutCross.loadSigmaTable().getTargetSigmas(list_elements, list_fractions)[2];
    //  System.out.println("i2s: "+i2s);
    
    int N = ioqs.length;
    soqs = new float[N];
    if (N != npt) System.out.println("******UNEXPECTED ERROR******");
    for (int i=0; i<N; i++){
      soqs[i] = ioqs[i]/bbarsq;
      soqs[i] += 1;
    }
       
    float y = soqs[0], tmp;
    for (int i = 1; i < N; i++){
      y += soqs[i];
      y /= 2.0f;
      tmp = soqs[i];
      soqs[i] = y;
      y = tmp;
    } 
  
  }
  
  float[][] getDofR () {
    return new float[][] {rs, dors};
  }
  
  float[][] getTofR () {
    int N = dors.length;
    float[] tors = new float[N];
    for (int i = 0; i<N; i++) {
      tors[i] = dors[i] + (float) (4*Math.PI*NumDensity)*rs[i];
    }
    return new float[][] {rs, tors};
  }
  
  float[][] getNofR () {
    int N = dors.length;
    float[] nors = new float[N];
    for (int i = 0; i<N; i++) {
      nors[i] = (dors[i] + (float) (4*Math.PI*NumDensity*rs[i]) )*rs[i];
    }
    return new float[][] {rs, nors};
  }
  
  float[][] getGofR () {
    int N = dors.length;
    float[] gors = new float[N];
    for (int i = 0; i<N; i++) {
      gors[i] = 1+dors[i]/(float)(4*Math.PI*rs[i]*NumDensity);
    }
    return new float[][] {rs, gors};
  }
  
  float[][] getCofR () {
    int N = dors.length;
    float[] cors = new float[N];   
    float nor0 = (dors[0] + (float) (4*Math.PI*NumDensity*rs[0]) )*rs[0];
    cors[0] = 0.5f*nor0*rs[0];
    float nor, cor = cors[0];
    float r, r0 = rs[0];

    for (int i = 1; i<N; i++) {
      nor = (dors[i] + (float) (4*Math.PI*NumDensity*rs[i]) )*rs[i];
      nor0 += nor;
      r = rs[i];
      r -= r0;
      cor += 0.5f*nor0*r; 
      r0 = rs[i]; 
      nor0 = nor;
      cors[i] = cor;
    }
    return new float[][] {rs, cors};
  }    
  
  void calculateDofR (float qcut, int iwf, float rcut, float density) {
    NumDensity = density;
    calculateDofR (qcut, iwf, rcut);
  }
  
  void calculateDofR (float qcut, int iwf, float rcut) {
    int index=0, N;
    while (qs[index]<=qcut) {
      index++;  
    }
    N = index;
    index = Math.round(qstart/delq);
    
//    float[] qs0 = new float[N+index];
//    float[] isoqs0 = new float[N+index];
    float[] qs0 = new float[NF];
    float[] soqs0 = new float[NF];
    float yatindex=soqs[0];
    if (yatindex >0) {
      for (int i=0; i<index; i++) {
        qs0[i] = i*delq;
        soqs0[i] = (float) Math.pow(i*delq, 2)*yatindex/(float) Math.pow(index*delq, 2);    
      }       
    } else {
      for (int i=0; i<index; i++) {
        qs0[i] = i*delq;
        soqs0[i] = 0.0f;    
      }
    }
    for (int i=0; i<N; i++) {
      qs0[i+index] = qs[i];
      soqs0[i+index] = soqs[i];    
    }
    for (int i=N+index; i<NF; i++) {
//      qs0[i] = i*delq;
      soqs0[i] = 0.0f;    
    }
    
//    qs = qs0;
//    soqs = soqs0;

    float y;
//    N += index;
    int n = NF;    
    float[] Zdata = new float[4*n];
    float[] wf = getWMOD(iwf, qcut);   
    for (int i = 1; i<n; i++){
      y = qs0[i]*(soqs0[i]-1)*wf[i];
      Zdata[2*i] = y;
      Zdata[2*i+1] = 0;
      Zdata[4*n-2*i] = -y;  
      Zdata[4*n-2*i+1] = 0;
    }
    Zdata[2*n] = 0;
    Zdata[2*n+1] = 0;
    
    ComplexFloatFFT q2d = new ComplexFloatFFT_Mixed(2*n);
    q2d.transform(Zdata);
    
    String output = "";
    float[] rs0 = new float[n];
    float[] dors0 = new float[n];
    for (int i = 0; i<n; i++){
//      rs[i] = (float) (i*Math.PI/qcut);
      rs0[i] = (float) (i*Math.PI/NF/delq);
      dors0[i] = (float) (Zdata[2*i+1]*-delq/Math.PI);
//      if (i<100) output += "["+rs[i]+","+dors[i]+"]"+",";
    }
    
    N = Math.round(rcut*NF*delq/(float)Math.PI)+2;
    rs = new float[N];
    dors = new float[N];    
    System.arraycopy(rs0, 0, rs, 0, N); 
    System.arraycopy(dors0, 0, dors, 0, N); 
//    System.out.println("DofR: "+output);
  }
  
  float[] getWMOD (int iwf, float qcut) {
    float[] wfs = new float[NF];
    double y;
    
    for (int i = 0; i<NF; i++){
      switch (iwf) {
        case 0:
          wfs[i] = 1.0f;
          break;
        case 1:
          y = qs[i]*Math.PI/qcut;
          wfs[i] =(float) (Math.sin(y)/y);
          break;
        case 2:
          wfs[i] = 1.0f-(float)Math.pow(qs[i]/qcut, 2);
          break;
        case 3:
          wfs[i] = 1.0f-(float)Math.pow(qs[i]/qcut, 3);
          break;
        case 4:
          wfs[i] = (1.0f+(float)Math.cos(qs[i]/qcut*Math.PI))/2.0f;
          break;
        default:
          wfs[i] = 1.0f;  
      }
    }
    
    return wfs;
  }
  
  public static void main(String[] args) {
    
    String fin = "/IPNShome/taoj/GLAD/ftr/8095ioq.txt";
    Ftr f1 = Ftr.parseGLADISOQ(fin);
//    f1.convertIofQ2SofQ("Si 1.0 O 2.0");
 
//    float[] xs = f1.qs;        
//    float[] ys = f1.getSofQ("Si 1.0 O 2.0");
//    float[] ys = f1.getSofQ();

//   f1.NumDensity = 0.0662f;

    DataSet ds = new DataSet ("DS", 
           "Construct a dataset holding the distribution functions.",
           "1/Angstrom", "Q",
           "", "");
    Data sofq = new HistogramTable(XScale.getInstance(f1.getQ()),
                                       f1.getSofQ(),
                                       10000);
    ds.addData_entry(sofq);
    DataSetTools.viewer.ViewManager nrm_smp_view = new DataSetTools.viewer.ViewManager(ds, DataSetTools.viewer.IViewManager.IMAGE);

    f1.calculateDofR(25.0f, 0, 0.0662f);
    
    long t0 = System.currentTimeMillis();
    float[][] test = f1.getGofR();
    StringBuffer output = new StringBuffer("GofR:\n");
    String entry;
    for (int i = 0; i<f1.rs.length; i++){
          if (i<100) {
            entry= "["+test[0][i]+","+test[1][i]+"]"+",";
            output.append(entry);

          } 
//          System.out.println("r: "+dors[0][i]+" Dr: "+dors[1][i]);
        }
    System.out.println(output);        

    long t1 = System.currentTimeMillis();
        System.out.println("time: "+(t1-t0));     
//   for (int i = 0; i<f1.qs.length; i++){
//      System.out.println(f1.qs[i]+" "+f1.isoqs[i]);
//    }
  
  }

}
