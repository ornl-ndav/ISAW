/*
 * Created on Mar 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
 * @author taoj
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CylMulTof {

  public static String cylmultof = null;

  static final int NANGLES = 9;
  static float[] angle_list = new float[NANGLES];
  static float[] wavelength_list;
  static float[][][] cylmultof_coeff;

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

  static void run(Reader input_cylmulin) throws IOException, InterruptedException{

    Process proc = (Runtime.getRuntime()).exec(cylmultof);

    BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    BufferedReader fr_input = new BufferedReader(input_cylmulin);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String line = null;
    
    System.out.println("\nMultiple scattering calculation input:");  
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
        cylmultof_coeff = new float[2][NANGLES][nlambda];
        wavelength_list = new float[nlambda];
//        System.out.println("# of wavelength: "+nlambda);
        int index;
//    float primary_scattering, multiple_scattering;
     
        for (int i=0; i < NANGLES; i++) {
          if((s =br.readLine())!=null){
//            System.out.println(">>>"+s);
            String[] list = Pattern.compile("\\s+").split(s.trim());
//            System.out.println("# of records: "+list.length);
            index = (new Integer(list[0])).intValue();
            angle_list[i] = (new Float(list[1])).floatValue();
//            System.out.println("index: "+index+" scattering_angle: "+angle_list[i]); 
         
            for (int j=0; j < nlambda; j++){ 
            s=br.readLine();
            list = Pattern.compile("\\s+").split(s.trim());
            if (list.length!=4) System.out.println("***file error***");
//            System.out.println(">>>"+s);
            if (i == 0) wavelength_list[j] =  (new Float(list[0])).floatValue();
            cylmultof_coeff[0][i][j] =  (new Float(list[1])).floatValue();
            cylmultof_coeff[1][i][j] = (new Float(list[2])).floatValue();
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
    return cylmultof_coeff;
  }
  
  public static float[] getCoeff(float angle, float lambda) { //angle in radians;
    int nangles = angle_list.length;
    float degree = angle*180.0f/(float)Math.PI;
    if (nangles != NANGLES) System.out.println("***WARNING***: nangles != NANGLES");
    float[] y = new float[nangles], z = new float[2];
    for (int k =0; k < 2; k++){
      for (int i = 0; i < nangles; i++) {
        y[i] = arrayUtil.interpolate(lambda, wavelength_list, cylmultof_coeff[k][i]);
      }
    z[k] =   arrayUtil.interpolate(degree, angle_list, y);
    }
    return z;
  }
  
  
  
  public static void main(String[] args) {
//    String input_cylmulin = "/IPNShome/taoj/GLAD/coral/cylindrical/cylmul.in";
    try {
/*        
      String[][] targets = {{"Si", "O"}, {"V"}};
      float[][] formulas = {{1.0f, 2.0f}, {1.0f}};
      float[] radii = {0.0f, 0.4415f, 0.4763f};
      float[] density = {0.0662f, 0.07205f};
      float[] sigma_a = {0.0058f, 5.08f};
      float astep = 0.1f;
*/
      String[][] targets = {{"V"}};
      float[][] formulas = {{1.0f}};
      float Sht = 6.00f, Bwid = 1.02f, Bht = 1.27f;
      float[] radii = {0.0f, 0.4763f};
      float[] density = {0.07205f};
      float[] sigma_a = {5.08f};
      float astep = 0.1f;
      
      MutCross.run(MutCross.sigmatable);    
      String MulSetup = MutCross.MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, astep);
      StringReader input_cylmulin = new StringReader(MulSetup);
      run(input_cylmulin);
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace();
    }
    System.out.println("# of angles: "+getAngles().length);
    System.out.println("# of wavelength: "+getWavelength().length);
    System.out.println("first angle: "+getAngles()[0]+" ninth angle: "+getAngles()[8]);
//    System.out.println("wavelength at 0,63: "+getWavelength()[63]);
    float[] a = getCoeff(140.0f, 4.25f);
    System.out.println("coeff at 140 degree, 6.45 angstrom: "+a[0]+" "+a[1]);
    
  }
}
