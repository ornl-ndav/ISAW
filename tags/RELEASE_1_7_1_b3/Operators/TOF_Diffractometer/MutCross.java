/*
 * Created on Mar 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package Operators.TOF_Diffractometer;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.*;
import DataSetTools.util.SharedData;


/**
 * @author taoj
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MutCross {
  static final float LAMBDA = 1.7979f; //the reference wavelength at 2200 m/s neutron
  public static String sigmatable = null;
  
  static{
    sigmatable = SharedData.getProperty( "ISAW_HOME" );
    if (sigmatable==null){
    sigmatable = SharedData.getProperty("user.home");
    sigmatable += java.io.File.separator+"ISAW";
  }
    sigmatable = sigmatable.trim();
    sigmatable += java.io.File.separator+"Databases";
    sigmatable += java.io.File.separator+"sears91.dat";
    if( new File(sigmatable).exists()){
        // do nothing
    } else{
      sigmatable = null;
    }
  }

  private static HashMap element_params;
  public static float Nscatterers;
  
  static void run(String sigma_table) throws IOException, InterruptedException{
         
    BufferedReader fr_input = new BufferedReader(new FileReader(sigma_table));
    String element_symbol, line = null;
    String[] list;
    
    element_params = new HashMap();
      
      while((line = fr_input.readLine()) != null ) {
        if(line.charAt(0) != '#') {
          list = Pattern.compile("\\s+").split(line.trim());
//          System.out.println("<<"+line);
          float[] scattering_params ={(new Float(list[1])).floatValue(), (new Float(list[3])).floatValue(),
                                                    (new Float(list[4])).floatValue(), (new Float(list[5])).floatValue()}; 
                                                    //an array that contains atomic mass, b_bar, sigma_s, sigma_a in that order;
          element_params.put(list[2], scattering_params);
          
        }
      }
//      System.out.println(element_params.containsKey("O"));
//      System.out.println(((float[])element_params.get("O"))[0]);
    fr_input.close();
  }      

  public static float[] getScatteringParams (String element_symbol) {
    float[] scattering_params;
    if (!element_params.containsKey(element_symbol)){
      System.out.println("!!!!!!WARNING!!!!!!"+"\n"+"\""+element_symbol+"\""+" CAN'T BE FOUND.");
      return null;
    }
    else {
      return (float[])element_params.get(element_symbol);
    }
  }
  
  public static float[] getTargetSigmas (String[] elements, float[] formula) {
    float[] sigma = {0.0f, 0.0f}, scattering_params = new float[4];
    float b2f, sum_formula = 0.0f;
    if (elements.length != formula.length) {
      System.out.println("******ERROR******WRONG INPUT");
      return null;
    }
    else {
      for (int i =0; i < elements.length; i++){
        sum_formula += formula[i];
        if ((scattering_params = getScatteringParams(elements[i])) != null){
          b2f = scattering_params[0]/(1+scattering_params[0]);
          sigma[0] += formula[i]*scattering_params[2]*b2f*b2f;
          sigma[1] += formula[i]*scattering_params[3];
        }
        else return null;
      }
      sigma[0] /= sum_formula;
      sigma[1] /= sum_formula;
      return sigma;  
    }
  }
  
  public static String MutMaker (String[] target, float[] formula, float minW, float maxW, float dW){
    String MutTable = "";
    int nW=1;
    float lambda = minW, sigma_t;
    float[] sigma = getTargetSigmas(target, formula); 
    if (sigma == null) System.out.println("*******ERROR******SIGMAS NOT FOUND."); 
    else {
      BufferedWriter bw = (new BufferedWriter(new StringWriter()));
      while (lambda < maxW){
        sigma_t = sigma[0]+sigma[1]*lambda/LAMBDA;
        MutTable += lambda+" "+sigma_t+"\n";
        nW++;
        lambda += dW;
      }
      
        lambda = maxW;
        sigma_t = sigma[0]+sigma[1]*lambda/LAMBDA;
        MutTable += lambda+" "+sigma_t+"\n";
        MutTable = nW+"\n"+MutTable;
      }
//      System.out.println(MutTable);
      return MutTable;
    }
    
  public static String MulAbsInputMaker(String[][] targets, float[][] formulas, 
                                                           float Sht, float[] radii, float[] density, 
                                                           float Bwid, float Bht, float astep) {
                                                             
        float[] beam_profile = {1.0f, 1.0f};
//        float[] target_profile = {6.000f, 0.510f, -0.510f, 2.000f, -2.000f, 2.365f, 3.635f, 0.000f, 6.000f};
        float[] target_profile = new float[9];
        target_profile[0] = Sht;
        target_profile[1] =  Bwid/2.0f;
        target_profile[2] =  -Bwid/2.0f;
        target_profile[3] =  2.0f;
        target_profile[4] = -2.0f;
        target_profile[5] = (Sht-Bht)/2.0f;
        target_profile[6] =  (Sht+Bht)/2.0f;
        target_profile[7] = 0.0f;
        target_profile[8] = Sht;
        
        float[] det_angles = {2.00f, 5.00f, 10.00f, 20.00f, 40.00f, 60.00f, 80.00f, 100.00f, 140.00f};
//        float minW = 0.03f, maxW = 4.21f, dW = 0.02f;
        float minW = 0.1f, maxW = 4.3f, dW = 0.1f;
        int nan;
        String MulAbsInput;
        
        nan = radii.length-1;
        if((targets.length != nan) || (formulas.length != nan) || (density.length != nan)){
          System.out.println("******ERROR******CHECK INPUT TO MulAbsInputMaker.");
          return null;
        }

        float[] sigma_a = new float[nan];
        for (int i = 0; i < nan; i++){
          sigma_a[i] = getTargetSigmas(targets[i], formulas[i])[1];
        }
         
        MulAbsInput = beam_profile.length+"\n";
        for(int i=0; i<beam_profile.length; i++){
          MulAbsInput += beam_profile[i]+" ";
        }
        MulAbsInput += "\n";
        MulAbsInput += astep+"\n";
        MulAbsInput += det_angles.length+"\n";
        for(int i=0; i<det_angles.length; i++){
          MulAbsInput += det_angles[i]+" ";
        }
        MulAbsInput += "\n";
        for(int i=0; i<target_profile.length; i++){
          MulAbsInput += target_profile[i]+" ";
        }
        MulAbsInput += "\n";
        MulAbsInput += nan+"\n";
        for(int i=0; i<nan+1; i++){
          MulAbsInput += radii[i]+" ";  
        }
        MulAbsInput += "\n";
        
        for(int i=0; i<nan; i++){
          MulAbsInput += density[i]+" "+sigma_a[i]+"\n";  
          MulAbsInput += MutMaker (targets[i], formulas[i], minW, maxW, dW);
        }
        
        Nscatterers = (float)(density[0]*Bht*Math.PI*(radii[1]*radii[1]-radii[0]*radii[0]));

        return MulAbsInput;
      }
  
  public static void main(String[] args) {
    System.out.println("Default crosssection data file is "+"\""+sigmatable+"\".");
    try {
      run(sigmatable);
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace();
    }
       
    String[][] targets = {{"C", "Cl"},{"V"}};
                       float[][] formulas = {{1.0f, 4.0f},{1.0f}};
                       float[] radii = {0.0f, 0.3048f, 0.3175f};
                       float[] density = {0.0319f, 0.07205f};
//                       float[] sigma_a = {26.83183f, 5.08f};
                       float mul_astep = 0.1f, abs_astep = 0.02f;
                       float Sht = 4.1f, Bwid = 1.02f, Bht = 1.27f;
                       
    String[][] targets_can = new String[][] {targets[1]};
            float[][] formulas_can = new float[][]  {formulas[1]};
            float[] radii_can = new float[] {radii[1], radii[2] };
            float[] density_can = new float[] {density[1]};
//            float[] sigma_a_can = new float[] {sigma_a[1] };
    System.out.println(MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, mul_astep));
    System.out.println("\n"+"samcal: "+Nscatterers+"\n");
    System.out.println(MulAbsInputMaker(targets_can, formulas_can, Sht, radii_can, density_can, Bwid, Bht, mul_astep));
    System.out.println("\n"+"samcal: "+Nscatterers);
  }

}
