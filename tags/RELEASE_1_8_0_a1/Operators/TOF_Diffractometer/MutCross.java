/*
 * File:  MutCross.java
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
 * Revision 1.4  2006/01/05 22:59:22  taoj
 * minor changes
 *
 * Revision 1.3  2005/12/15 20:52:56  dennis
 * Added tag for CVS logging so that future modifications can be tracked.
 *
 */
 
package Operators.TOF_Diffractometer;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.*;
import DataSetTools.util.SharedData;


/**
 * This class sets up a static HashMap containing atomic scattering parameter information,
 * and also calculates a sample's scattering parameters. It corresponds to the CROSS
 * program on GLAD.
 */
public class MutCross {
  public static final float RTLAMBDA = 1.7979f; //the reference wavelength at 2200 m/s neutron
  public static String defSigmaTable = null;

  static {
    if (GLADRunProps.ISAWDBDirectory != null) {
      defSigmaTable = GLADRunProps.ISAWDBDirectory;
      defSigmaTable += java.io.File.separator+"sears91.dat";
    } else {
      defSigmaTable = SharedData.getProperty( "ISAW_HOME" );
      if (defSigmaTable==null){
        defSigmaTable = SharedData.getProperty("user.home");
        defSigmaTable += java.io.File.separator+"ISAW";
      }
      defSigmaTable = defSigmaTable.trim();
      defSigmaTable += java.io.File.separator+"Databases";
      defSigmaTable += java.io.File.separator+"sears91.dat";      
    }
    if( new File(defSigmaTable).exists()) {
            // do nothing
    } else{
      defSigmaTable = null;
    }
  }  
  
  public static HashMap element_params = null;
//  public static float Nscatterers;
  
  private MutCross() {
    if (element_params == null)
      try {
        getSigmaTable(defSigmaTable);      
      } catch(Throwable t) {
        System.out.println("\n***MutCross() ERROR***\n"+"defSigmaTable: "+defSigmaTable);
        t.printStackTrace();
      }
  }
  
  private MutCross(String sigmatable) {
    try {
      getSigmaTable(sigmatable);      
    } catch(Throwable t) {
      System.out.println("\n***MutCross(sigmatable) ERROR***\n");
      t.printStackTrace();
    }
  }
  
  public static MutCross loadSigmaTable () {
    return new MutCross ();
  }
  
  public static MutCross loadSigmaTable (String sigmatable) {
    return new MutCross (sigmatable);
  }
  
  private void getSigmaTable(String sigmatable) throws IOException, InterruptedException{
         
    BufferedReader fr_input = new BufferedReader(new FileReader(sigmatable));
    String element_symbol, line = null;
    String[] list;
    float[] scattering_params;    
    Pattern token = Pattern.compile("\\s+");
    element_params = new HashMap();
      
    while((line = fr_input.readLine()) != null ) {
      if(line.charAt(0) != '#') {
        list = token.split(line.trim());
//          System.out.println("<<"+line);
        scattering_params = new float[] {(new Float(list[1])).floatValue(), (new Float(list[3])).floatValue(),
                                                    (new Float(list[4])).floatValue(), (new Float(list[5])).floatValue()}; 
                                                    //an array that contains atomic mass, b_bar, sigma_s, sigma_a in that order;
        element_params.put(list[2], scattering_params);
      }
    }
//      System.out.println(element_params.containsKey("O"));
//      System.out.println(((float[])element_params.get("O"))[0]);
    fr_input.close();
  }      
/*  
  static String MutReader (String mutfile) throws IOException, InterruptedException{
    String MutTable = "";
    BufferedReader fr_input = new BufferedReader(new FileReader(mutfile));
    String line = null;
    while( (line = fr_input.readLine()) != null ) {
//      if(line.charAt(0) != '#') {
        MutTable += line+"\n";          
//      }
    }

    fr_input.close();
    return MutTable;
  }
*/
  public float[] getScatteringParams (String element_symbol) {
    float[] scattering_params;
    if (!element_params.containsKey(element_symbol)){
      System.out.println("!!!!!!WARNING!!!!!!"+"\n"+"\""+element_symbol+"\""+" CAN'T BE FOUND.");
      return null;
    }
    else {
//    an array that contains atomic mass, b_bar, sigma_s, sigma_a in that order;
      return (float[])element_params.get(element_symbol);
    }
  }
  
  public float[] getTargetSigmas (String[] elements, float[] formula) {
    float[] sigma = {0.0f, 0.0f, 0.0f}, scattering_params = new float[4]; 
    float b2f, bbarsq = 0.0f, sum_formula = 0.0f;
    if (elements.length != formula.length) {
      System.out.println("******ERROR******WRONG INPUT\n"+elements.length+" "+formula.length);
      return null;
    }
    else {
      for (int i =0; i < elements.length; i++){
        sum_formula += formula[i];
        if ((scattering_params = getScatteringParams(elements[i])) != null){
          b2f = scattering_params[0]/(1+scattering_params[0]);
          sigma[0] += formula[i]*scattering_params[2]*b2f*b2f;
          sigma[1] += formula[i]*scattering_params[3];
          bbarsq += formula[i]*scattering_params[1];
        }
        else return null;
      }
      sigma[0] /= sum_formula;
      sigma[1] /= sum_formula;
      bbarsq /= sum_formula;
      sigma[2] = bbarsq*bbarsq/100.0f; //sigma[2] is (sum(f[i]*b[i],i=1..n)/sum(f[i],i=1..n))^2 in bars;
//    an array that contains sigma_s, sigma_a, <b_bar>^2 in that order;
      return sigma;  
    }
  }
       
  public static void main(String[] args) {
    MutCross mc = new MutCross ();
    System.out.println("Default crosssection data file is "+MutCross.defSigmaTable+"\n");
 /*
    try {
      run();
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace();
    }
 */      
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

//    System.out.println(mc.makeMut(targets[0], formulas[0], 0.1f, 4.3f, 0.1f));
 

 //   System.out.println(MulAbsInputMaker(targets, formulas, Sht, radii, density, Bwid, Bht, mul_astep, new String[] {null, null}, 0.1f, 4.0f, 2.0f));
 //   System.out.println("\n"+"samcal: "+Nscatterers+"\n");
//    System.out.println(MulAbsInputMaker(targets_can, formulas_can, Sht, radii_can, density_can, Bwid, Bht, mul_astep));
//    System.out.println("\n"+"samcal: "+Nscatterers);

/*
    System.out.println(MutMaker(targets[0], formulas[0], 0.1f, 4.3f, 0.1f));
    System.out.println("\n");
    System.out.println(MutMaker(targets[1], formulas[1], 0.1f, 4.3f, 0.1f));
*/
/*
    try{
      System.out.println(MutReader("/IPNShome/taoj/GLAD/coral/cylindrical0/GLD08094.MUT")+"done.");
    } catch(Throwable t) {
      System.out.println("unexpected error");
      t.printStackTrace(); 
    }
*/    
//    System.out.println("Done.");  
  
  }

}
