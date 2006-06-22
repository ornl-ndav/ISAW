/*
 * File:  CylMulTof.java
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
 */

package Operators.TOF_Diffractometer;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import gov.anl.ipns.Util.Numeric.arrayUtil;
import DataSetTools.util.SharedData;
import java.io.File;

/**
 * This class feeds input to the cylmultof executable and parses its output using
 * Runtime.exec();
 */
public class CylMulTof {

  public static String cylmultof = null;

  float[] angles;
  int nw;
  float[] lambdas;
  float[][][] mulcoeffs;
  Reader mulinput;
  private int imask;

  static{
    cylmultof = SharedData.getProperty( "ISAW_HOME" );
    if (cylmultof==null){
    cylmultof = SharedData.getProperty("user.home");
    cylmultof += java.io.File.separator+"ISAW";
    }
    cylmultof = cylmultof.trim();
    cylmultof += java.io.File.separator+"Operators"+java.io.File.separator+"TOF_Diffractometer";
    String os = SharedData.getProperty("os.name");
    if (os.equals("Linux")) cylmultof += java.io.File.separator+"cylmultof_x";
    else if (os.startsWith("Windows")) cylmultof += java.io.File.separator+"cylmultof_w";
    if( new File(cylmultof).exists()){
        // do nothing
    } else{
      cylmultof = null;
    }
  }
  
  CylMulTof (GLADScatter sca) {
    angles = sca.angles;
    nw = sca.nlambda;
    mulinput = new StringReader(sca.mulinput);
    imask = sca.anmask;
  }

  public void runMulCorrection () {
    try {
      exeMulCorrection();
    } catch(Throwable t) {
      t.printStackTrace();
    }  
  }
    
  private void exeMulCorrection() throws IOException, InterruptedException{
    
    Process proc = (Runtime.getRuntime()).exec(cylmultof);

    BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    BufferedReader fr_input = new BufferedReader(mulinput);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String line = null;
    
    System.out.println(GLADScatter.SMASK[imask]+" multiple scattering calculation input:");  
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
      System.out.println("***UNEXPECTED ERROR***\ncheck number of lambdas in cylmultof output.\n");
    mulcoeffs = new float[2][angles.length][nlambda];
    lambdas = new float[nlambda];
//      System.out.println("# of wavelength: "+nlambda);
      
//    float primary_scattering, multiple_scattering;
    Pattern token = Pattern.compile("\\s+");
    for (int i = 0; i < nangle; i++) {
      if((s =br.readLine())!=null){
//            System.out.println(">>>"+s);
        String[] list = token.split(s.trim());
//            System.out.println("# of records: "+list.length);
        index = (new Integer(list[0])).intValue();
        angles[i] = (new Float(list[1])).floatValue();
//            System.out.println("index: "+index+" scattering_angle: "+angle_list[i]); 
         
        for (int j = 0; j < nlambda; j++){ 
          s = br.readLine();
          list = token.split(s.trim());
          if (list.length!=4) System.out.println("***file error***");
//            System.out.println(">>>"+s);
          if (i == 0) lambdas[j] =  (new Float(list[0])).floatValue();
          mulcoeffs[0][i][j] =  (new Float(list[1])).floatValue();
          mulcoeffs[1][i][j] = (new Float(list[2])).floatValue();
//            System.out.println(">>>wavelength_list "+"["+i+"]"+"["+j+"]: "+" primary_scattering: "+cylmultof_coeff[0][i][j]+
//                            " multiple_scattering: "+cylmultof_coeff[1][i][j]);
        }
       
      } 
    }

    if ((line = er.readLine()) != null) System.out.println("***WARNING***: error msg generated."+"\n"
                                                                                                     +"ERROR>>>"+line);
    while ((line = er.readLine()) != null) {
      System.out.println("ERROR>>>"+line);
    } 
    
    System.out.println("running cylmultof executable...");  
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
    return mulcoeffs;
  }

//the double interpolation is based on individual values rather than well
//behaved 2-D arrays, a second look to improve efficiency here?  
  public float[] getCoeff(float angle, float lambda) { //angle in radians;
    int nangles = angles.length;
    float degree = angle*180.0f/(float)Math.PI;
    float[] y = new float[nangles], z = new float[2];
    for (int k = 0; k < 2; k++){
      for (int i = 0; i < nangles; i++) {
        y[i] = arrayUtil.interpolate(lambda, lambdas, mulcoeffs[k][i]);
      }
      z[k] =   arrayUtil.interpolate(degree, angles, y);
    }
    return z;
  }
  
  
  
  public static void main(String[] args) {
//    String input_cylmulin = "/IPNShome/taoj/GLAD/coral/cylindrical/cylmul.in";

/*        
      String[][] targets = {{"D", "O"}, {"Ti", "Zr"}};
       float[][] formulas = {{2.0f, 1.0f}, {0.68f, 0.32f}};
       float Sht = 6.00f, Bwid =1.02f, Bht = 1.27f;
       float[] radii = {0.0f, 0.40f, 0.45f};
       float[] density = {0.1f, 0.0542f};
       String[] mutfiles = new String[] {null, null};
       float astep = 0.1f;
       float minW = 0.1f, maxW = 4.3f, dW = 0.1f;



      String[][] targets = {{"V"}};
      float[][] formulas = {{1.0f}};
      float Sht = 6.00f, Bwid = 1.02f, Bht = 1.27f;
      float[] radii = {0.0f, 0.4763f};
      float[] density = {0.07205f};
      float[] sigma_a = {5.08f};
      float astep = 0.1f;
      String[] mutfiles = new String[] {null};
      float minW = 0.1f, maxW = 4.3f, dW = 0.1f;
     
 
      String[][] targets = {{"C", "Cl"}, {"V"}};
      float[][] formulas = {{1.0f, 4.0f}, {1.0f}};
      float[] radii = {0.0f, 0.3048f, 0.3175f};
      float[] density = {0.0319f, 0.07205f};
      float Sht = 4.1f, Bwid =1.02f, Bht = 1.27f;
      float astep = 0.1f;
      String[] mutfiles = new String[] {null, null};
      float minW = 0.1f, maxW = 4.3f, dW = 0.1f; 
*/
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
    
    CylMulTof smprunmul = new CylMulTof(smprun);
    smprunmul.runMulCorrection();

    System.out.println("# of angles: "+smprunmul.getAngles().length);
    System.out.println("# of wavelength: "+smprunmul.getWavelength().length);
//    System.out.println("wavelength at 0,63: "+getWavelength()[63]);

    float lambda = 0.1f;
    StringBuffer output = new StringBuffer(2000);
    while (lambda < 5.0f) {
      output = output.append("["+lambda+","+smprunmul.getCoeff(140.0f*3.1416f/180.0f, lambda)[0]+"]"+",");
      lambda += 0.1f; 
    }
    float[] a = smprunmul.getCoeff(140.0f, 4.25f);
    System.out.println(output);
    
    
    float theta = 140.0f/360*(float)Math.PI;
        float q;
        float m0, m1, delta;
        
        System.out.println("cylmultof_coeff: "+smprunmul.mulcoeffs.length);
        for (int i = 0; i < 30; i++) {
//          q = i * 0.05f;
//          lambda = (float) (4 *Math.PI*Math.sin(theta)/q);
            lambda = (i+1)*0.1f;
          m0 = smprunmul.getCoeff(2*theta, lambda)[0];
          m1 = smprunmul.getCoeff(2*theta, lambda)[1];
          delta = m1/(m0+m1);
          System.out.println(lambda+" "+m0+" "+m1);
        }
    
  }
  
  
}
