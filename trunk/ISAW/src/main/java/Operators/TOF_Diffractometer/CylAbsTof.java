/*
 * File:  CylAbsTof.java
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
 * $Log$
 * Revision 1.4  2005/12/15 20:52:55  dennis
 * Added tag for CVS logging so that future modifications can be tracked.
 *
 *
 */
 
package Operators.TOF_Diffractometer;

import java.io.Reader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import gov.anl.ipns.Util.Numeric.arrayUtil;
import DataSetTools.util.SharedData;
import java.io.File;

/**
 * This class feeds input to the cylabstof executable and parses its output using
 * Runtime.exec();
 */
public class CylAbsTof {
  
  public static String cylabstof = null;
  
  float[] angles;
  int nw, nan;
  float[] lambdas;
  float[][][] abscoeffs;
  Reader absinput;
  private int imask;

  static{
    cylabstof = SharedData.getProperty( "ISAW_HOME" );
    if (cylabstof==null){
    cylabstof = SharedData.getProperty("user.home");
    cylabstof += java.io.File.separator+"ISAW";
    }
    cylabstof = cylabstof.trim();
    cylabstof += java.io.File.separator+"Operators"+java.io.File.separator+"TOF_Diffractometer";
    String os = SharedData.getProperty("os.name");
    if (os.equals("Linux")) cylabstof += java.io.File.separator+"cylabstof_x";
    else if (os.startsWith("Windows")) cylabstof += java.io.File.separator+"cylabstof_w";
    if( new File(cylabstof).exists()){
        // do nothing
    } else{
      cylabstof = null;
    }
  }
  
  CylAbsTof (GLADScatter sca) {
    nan = sca.nan;
    angles = sca.angles;
    nw = sca.nlambda;
    absinput = new StringReader(sca.absinput);
    imask = sca.anmask;
  }

  public void runAbsCorrection () {
    try {
      exeAbsCorrection();
    } catch(Throwable t) {
      t.printStackTrace();
    }  
  }

  private void exeAbsCorrection() throws IOException, InterruptedException{
 
    Process proc = (Runtime.getRuntime()).exec(cylabstof);

    BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    BufferedReader fr_input = new BufferedReader(absinput);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String line = null;
    
    System.out.println(GLADScatter.SMASK[imask]+" attenuation calculation input:");    
    while((line = fr_input.readLine()) != null ) {
      System.out.println("<<<"+line);
      bw.write(line, 0, line.length());
      bw.newLine();
      bw.flush();
    }
    fr_input.close();
      
    if(!br.ready()) {
    }
            
    String s = (br.readLine()).trim();
    int index, nlambda, nangle = angles.length;
    if ( (nlambda=(new Integer(s)).intValue()) != nw ) 
       System.out.println("***UNEXPECTED ERROR***\ncheck number of lambdas in cylabstof output.\n");
    abscoeffs = new float[nan+2][nangle][nlambda];
    lambdas = new float[nlambda];
//        System.out.println("# of wavelength: "+nlambda);
    String[] list;
    float A_s_s, A_c_sc, A_c_c;
    float[] A_s_sc = new float[nan-1];
    
    Pattern token = Pattern.compile("\\s+");    
    for (int i = 0; i < nangle; i++) {
      if((s =br.readLine())!=null){
//            System.out.println(">>>"+s);
        list = token.split(s.trim());
//            System.out.println("# of records: "+list.length);
        index = (new Integer(list[0])).intValue();
        angles[i] = (new Float(list[1])).floatValue();
//            System.out.println("index: "+index+" scattering_angle: "+angle_list[i]); 
         
        for (int j=0; j < nlambda; j++){ 
          s=br.readLine();
          list = token.split(s.trim());
          if (list.length!=(nan+3)) System.out.println("******ERROR*******");
//              System.out.println(">>>"+s);
            if (i == 0) lambdas[j] =  (new Float(list[0])).floatValue();
              A_s_s = (new Float(list[1])).floatValue();
              abscoeffs[0][i][j] = A_s_s;
              for (int k=0; k < nan-1; k++) {
                A_s_sc[k] = (new Float(list[k+2])).floatValue();
                abscoeffs[k+1][i][j] = A_s_sc[k];
              }
              A_c_sc =  (new Float(list[nan+1])).floatValue();
              abscoeffs[nan][i][j] = A_c_sc;
              A_c_c = (new Float(list[nan+2])).floatValue();
              abscoeffs[nan+1][i][j] = A_c_c;
              String A = " A_s_s: "+A_s_s;
              for (int k=0; k < nan-1; k++){
                A += " A_s_sc["+k+"]: "+A_s_sc[k]+" ";
              }
              A += " A_c_sc: "+A_c_sc+" A_c_c "+A_c_c+"\n";
//               System.out.println("wavelength_list "+"["+i+"]"+"["+j+"]: "+wavelength_list[j]+A);
            }
       
          } 
        }

        if ((line = er.readLine()) != null) System.out.println("***WARNING***: error msg generated."+"\n"
                                                                                                     +"ERROR>>>"+line);
        while ((line = er.readLine()) != null) {
          System.out.println("ERROR>>>"+line);
        } 
      
        System.out.println("running cylabstof executable..."); 
        int exitVal = proc.waitFor();
        System.out.println("exitVal (0 is successful): "+exitVal);
        br.close();
  }
  
  public float[] getAngles() {
    return angles;
  }
  
  public float[] getWavelength() {
    return lambdas;
  }
  
  public float[][][] getCoeffList () {
    return abscoeffs;
  }
  
  public float[] getCoeff(float angle, float lambda) {//angle in radians;
    float degree = angle*180.0f/(float)Math.PI;
    int nabs = abscoeffs.length;
    int nangle = angles.length;
    float[] y = new float[nangle], z = new float[nabs];
    for (int k = 0; k < nabs; k++){
      for (int i = 0; i < nangle; i++) {
        y[i] = arrayUtil.interpolate(lambda, lambdas, abscoeffs[k][i]);
      }
      z[k] = arrayUtil.interpolate(degree, angles, y);
    }
    return z;
  }
  
  
  public static void main(String[] args) {

    GLADRunProps runinfo = GLADRunProps.getExpProps();

    GLADScatter smp = new GLADScatter(runinfo, 1);
    smp.setMutTable();
    smp.setAbsInput();
    smp.setMulInput();
//    System.out.println("******smp******\n");
    smp.printScatterInfo();
    GLADScatter can = new GLADScatter(runinfo, 2);
    can.setMutTable();
    can.setAbsInput();
    can.setMulInput();
//    System.out.println("******can******\n");
    can.printScatterInfo();  
    GLADScatter smprun = smp.insideScatter(can);    
    System.out.println("******smp run******\n");
    smprun.printScatterInfo();
    
    CylAbsTof smprunabs = new CylAbsTof(smprun);
    smprunabs.runAbsCorrection();

   
    float theta = 140.0f/360*(float)Math.PI;
    float q, lambda;
    
    
    for (int i = 0; i < 43; i++) {
//      q = i * 0.05f;
//      lambda = (float) (4 *Math.PI*Math.sin(theta)/q);
      lambda = (i+1)*0.1f;
//      System.out.println("q:\t"+q+"\tAs,s\t"+getCoeff(theta, lambda)[0]+"\tAs,sc\t"+getCoeff(theta, lambda)[1]
//      +"\tAc,sc\t"+getCoeff(theta, lambda)[2]+"\tAc,c\t"+getCoeff(theta, lambda)[3]);
      System.out.println(lambda+" "+smprunabs.getCoeff(2*theta, lambda)[0]+" "+smprunabs.getCoeff(2*theta, lambda)[1]+" "+smprunabs.getCoeff(2*theta, lambda)[2]);
    }
  }
}
