/*
 * Created on Mar 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
 * @author taoj
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CylAbsTof {
  
  public static String cylabstof = null;
  
  static final int NANGLES = 9;
  static float[] angle_list = new float[NANGLES];
  static float[] wavelength_list;
  static float[][][] cylabstof_coeff;
  
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

  static void run(Reader input_cylabsin, int nan) throws IOException, InterruptedException{
 
    Process proc = (Runtime.getRuntime()).exec(cylabstof);

    BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    BufferedReader fr_input = new BufferedReader(input_cylabsin);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String line = null;
    
    System.out.println("\nAttenuation calculation input:");    
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
        int nlambda = (new Integer(s)).intValue();
        cylabstof_coeff = new float[nan+2][NANGLES][nlambda];
        wavelength_list = new float[nlambda];
//        System.out.println("# of wavelength: "+nlambda);
        String[] list;
        int index;
        float A_s_s, A_c_sc, A_c_c;
        float[] A_s_sc = new float[nan-1];
        
        for (int i=0; i < NANGLES; i++) {
          if((s =br.readLine())!=null){
//            System.out.println(">>>"+s);
            list = Pattern.compile("\\s+").split(s.trim());
//            System.out.println("# of records: "+list.length);
            index = (new Integer(list[0])).intValue();
            angle_list[i] = (new Float(list[1])).floatValue();
//            System.out.println("index: "+index+" scattering_angle: "+angle_list[i]); 
         
            for (int j=0; j < nlambda; j++){ 
              s=br.readLine();
              list = Pattern.compile("\\s+").split(s.trim());
              if (list.length!=(nan+3)) System.out.println("******ERROR*******");
//              System.out.println(">>>"+s);
              if (i == 0) wavelength_list[j] =  (new Float(list[0])).floatValue();
              A_s_s = (new Float(list[1])).floatValue();
              cylabstof_coeff[0][i][j] = A_s_s;
              for (int k=0; k < nan-1; k++) {
                A_s_sc[k] = (new Float(list[k+2])).floatValue();
                cylabstof_coeff[k+1][i][j] = A_s_sc[k];
              }
              A_c_sc =  (new Float(list[nan+1])).floatValue();
              cylabstof_coeff[nan][i][j] = A_c_sc;
              A_c_c = (new Float(list[nan+2])).floatValue();
              cylabstof_coeff[nan+1][i][j] = A_c_c;
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
      
        int exitVal = proc.waitFor();
        System.out.println("exitVal (0 is successful): "+exitVal);
        br.close();
  }
  
  public static float[] getAngles() {
    return angle_list;
  }
  
  public static float[] getWavelength() {
    return wavelength_list;
  }
  
  public static float[][][] getCoeffList () {
    return cylabstof_coeff;
  }
  
  public static float[] getCoeff(float angle, float lambda) {//angle in radians;
    int nangles = angle_list.length;
    float degree = angle*180.0f/(float)Math.PI;
    int nabs = cylabstof_coeff.length;
    if (nangles != NANGLES) System.out.println("***WARNING***: nangles != NANGLES");
    float[] y = new float[nangles], z = new float[nabs];
    for (int k = 0; k < nabs; k++){
      for (int i = 0; i < nangles; i++) {
        y[i] = arrayUtil.interpolate(lambda, wavelength_list, cylabstof_coeff[k][i]);
      }
      z[k] = arrayUtil.interpolate(degree, angle_list, y);
    }
    return z;
  }
  
  
  public static void main(String[] args) {
//    String input_cylmulin = "/IPNShome/taoj/GLAD/coral/cylindrical/cylabs.in";
//    String input_mut = "/IPNShome/taoj/GLAD/coral/cylindrical/GLD08094.MUT";
    try {
/*      String[][] targets = {{"Si", "O"}, {"V"}};
      float[][] formulas = {{1.0f, 2.0f}, {1.0f}};
      float Sht = 6.00f, Bwid =1.02f, Bht = 1.27f;
      float[] radii = {0.0f, 0.4415f, 0.4763f};
      float[] density = {0.0662f, 0.07205f};
      float[] sigma_a = {0.0058f, 5.08f};
      float astep = 0.02f;
*/
      String[][] targets = {{"V"}};
      float[][] formulas = {{1.0f}};
      float[] radii = {0.0f, 0.4763f};
      float[] density = {0.07205f};
      float[] sigma_a = {5.08f};
      float Sht = 6.00f, Bwid =1.02f, Bht = 1.27f;
      float astep = 0.02f;
      
     
      MutCross.run(MutCross.sigmatable);
      String AbsSetup = MutCross.MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, astep);
      StringReader input_cylabsin = new StringReader(AbsSetup);
      run(input_cylabsin, radii.length-1);
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace();
    }
    System.out.println("# of angles: "+getAngles().length);
    System.out.println("# of wavelength: "+getWavelength().length);
    System.out.println("first angle: "+getAngles()[0]+" ninth angle: "+getAngles()[8]);
    System.out.println("wavelength at 0,63: "+getWavelength()[43]);
    System.out.println("coeff at 140 degree, 4.21 angstrom: "+getCoeff(140.0f, 4.20f)[0]+" "+getCoeff(140.0f, 4.20f)[1]
            +" "+getCoeff(140.0f, 4.20f)[2]+" "+getCoeff(140.0f, 4.20f)[3]);
  }
}
