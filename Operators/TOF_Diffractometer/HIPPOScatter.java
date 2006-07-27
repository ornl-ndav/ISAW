/*
 * File:  HIPPOScatter.java
 *
 * Copyright (C) 2006 J. Tao
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
 * Revision 1.2  2006/07/27 15:11:16  dennis
 * Added logging tag, so future CVS commits will be
 * recorded in the file.
 *
 */
 
package Operators.TOF_Diffractometer;

import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.StringWriter;

/**
 * This class provides the LANSCE HIPPO instrument and experiment specific information
 * related to one "sample", which could be a vanadium/sample rod, a sample with can,
 * an empty can etc. 
 */

public class HIPPOScatter implements Cloneable {
  public static final String[] SMASK = {"calibration/van",
                                        "sample(rod)",
                                        "can",
                                        "sample with can",
                                        "furnace",
                                        "sample(rod) with furnace",
                                        "can with furnace",
                                        "sample with can with furance",
                                        "flat plate setup"
                                        };
  boolean isFlatPlate = false;
  float s2bangle;
  int nplanes;
  float profile[], angles[];
  float astep, mstep;
  float beamparams[];
  float scatterern;
  int nan, anmask, nlambda = 0;
  float rad[];
  float tss[];
  float density;
  float effdensity;
  String symbol[];
  float formula[];
  float sigma_a;
  float sigma_s;
  float bbarsq;
  float minw, maxw, dw;
//  int iftag; //2 column or 3 column MUT table;
  MutCross sigmawiz;
  String muttable;
  String absinput;
  String mulinput;
  private float bwid, bht;
//  CylAbsTof abs_run;
//  FltAbsTof abs_run_fp;
  CoralAbsTof abs_run;
  
  HIPPOScatter (HIPPORunProps expsetup, int imask) {    
    
    String key, expkeyh, analykeyh = "HIPPO.ANALYSIS";
    float sigmavals[];
    Object obj;
    
    if (imask >= 8) {
      isFlatPlate = true;
      imask -= 8;
    }
    switch (imask) {
      case 0:
        expkeyh = "HIPPO.EXP.CALIB";
        break;
      case 1:
        expkeyh = "HIPPO.EXP.SMP";
        break;
      case 2:
        expkeyh = "HIPPO.EXP.CAN";
        break;
//furnace:
      case 4:
        expkeyh = "HIPPO.EXP.FUR";
        break;
      default:
        expkeyh = null;
      
    }
    
    anmask = imask;
    nan = 1;    

    profile = (float[]) expsetup.ExpConfiguration.get(analykeyh+HIPPORunProps.PROFILE);
    angles = (float[]) expsetup.ExpConfiguration.get(analykeyh+HIPPORunProps.ANGLES);
 
    astep = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, analykeyh+HIPPORunProps.ASTEP);
    mstep = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, analykeyh+HIPPORunProps.MSTEP);
    minw = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, analykeyh+".MUT.MINW");
    maxw = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, analykeyh+".MUT.MAXW");
    dw = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, analykeyh+".MUT.DW");
    symbol = (String[]) expsetup.ExpConfiguration.get(expkeyh+HIPPORunProps.SYMBOL);
    formula = (float[]) expsetup.ExpConfiguration.get(expkeyh+HIPPORunProps.FORMULA);
    density = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, expkeyh+HIPPORunProps.DENSITY);
    effdensity = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, expkeyh+HIPPORunProps.EFFDENSITY);
    if (effdensity == 0.0f) effdensity = density;

    sigma_a = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, expkeyh+HIPPORunProps.SIGMA_A);    
    bbarsq = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, analykeyh+".B_BAR_SQR");

    System.out.println("\nCalculate cross sections for "+SMASK[anmask]+".");
    if (symbol == null || formula == null)
      throw new NullPointerException("!!!!!!Unexpected error: missing symbol or formula information.!!!!!!");
    else {
      sigmavals = MutCross.loadSigmaTable().getTargetSigmas(symbol, formula);
      sigma_s = sigmavals[0];
      sigma_a = sigmavals[1];
      if (bbarsq == 0.0f) bbarsq = sigmavals[2];
    }

    rad = (float[]) expsetup.ExpConfiguration.get(expkeyh+HIPPORunProps.SIZE);    
    if (rad == null) {
      rad = new float[] {0.0f, 0.0f, 0.0f};
    }
    if (!isFlatPlate) {
      float sht = rad[2];
      rad = new float[] { rad[0], rad[1] };           
      bwid = ((Float) expsetup.ExpConfiguration.get("HIPPO.VAC1.BWID")).floatValue();
      bht = ((Float) expsetup.ExpConfiguration.get("HIPPO.VAC1.BHT")).floatValue();
      
      if ( (obj = expsetup.ExpConfiguration.get("HIPPO.BEAM.PARAMS")) == null) { 
        beamparams = new float[9];
        beamparams[0] = sht;
        beamparams[1] =  bwid/2.0f;
        beamparams[2] =  -bwid/2.0f;
        beamparams[3] =  2.0f;
        beamparams[4] = -2.0f;
        beamparams[5] = (sht-bht)/2.0f;
        beamparams[6] =  (sht+bht)/2.0f;
        beamparams[7] = 0.0f;
        beamparams[8] = sht;
      } else beamparams = (float[]) obj;                    
       
    } else {
      
      tss = rad;
      s2bangle = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, "HIPPO.EXP.FLT.SMP_ANGLE_BEAM");
      nplanes = HIPPORunProps.getintKey(expsetup.ExpConfiguration, "HIPPO.EXP.FLT.NPLANES");

    }
    
    if ((imask & 1) == 1) {
      scatterern = HIPPORunProps.getfloatKey(expsetup.ExpConfiguration, analykeyh+".SCC");    
      if (scatterern == 0.0f) {
        if (isFlatPlate)  
          scatterern = (float)(effdensity*bwid*bht*tss[0]);
        else      
          scatterern = (float)(effdensity*bht*Math.PI*(rad[1]*rad[1]-rad[0]*rad[0])); 
//use scatterern from this point on;                
//        expsetup.ExpConfiguration.put("HIPPO.ANALYSIS.SCC", new Float(scatterern));
      } 
      if (scatterern != 0.0f)  System.out.println("sample effective density: "+effdensity
                                                +" number of scatterers (10^24): "+scatterern);      
    }
    
  }//constructor;
  
  HIPPOScatter insideScatter (HIPPOScatter outers) {
    
    HIPPOScatter inners = this, combos = null;
    int i, j;
    
    System.out.println("\nSetting up "+SMASK[inners.anmask]+" inside "+SMASK[outers.anmask]+"...");
    try {
//note: clone() here is probably a shallow copy;
      combos = (HIPPOScatter) inners.clone();      
    } catch(CloneNotSupportedException e) {
      System.err.println(this.getClass().getName()+" inners "+"can't clone");
      e.printStackTrace();
    }
    combos.beamparams = outers.beamparams;
    combos.anmask = inners.anmask + outers.anmask;
    i = inners.nan+1;
    j = outers.nan+1;
    combos.nan = i+j-2;  

    float comborad[] = null; 
    if (!isFlatPlate) { 
      
      if (inners.rad[i-1] != outers.rad[0]) System.out.println("******WARNINGS******\n" +
                       "input: outside radiu of "+SMASK[inners.anmask]+" and inside radius of "+SMASK[outers.anmask]+" don't match.");
      comborad = new float[i+j-1];
      while (j-- > 0) comborad[i+j-1] = outers.rad[j];
      while (i-- > 1) comborad[i-1] = inners.rad[i-1];
      combos.rad = comborad;

    } else {
      
      float combotss[];      
      if (outers.anmask == 4); //displex/furnace case:
      if (outers.anmask == 2) combotss = outers.tss;
      else {
        System.out.println("******Unexpected error: outer is neither a can nor a furnace.******");
        combotss = null;
        throw new NullPointerException();
      }      
      combos.tss = combotss; 

    }
    
    if (scatterern == 0.0f) {
      if (isFlatPlate)  
        scatterern = (float)(effdensity*bwid*bht*tss[0]);
      else      
        scatterern = (float)(effdensity*bht*Math.PI*(comborad[1]*comborad[1]-comborad[0]*comborad[0])); 
    } 
    if (scatterern != 0.0f)  System.out.println("sample effective density: "+effdensity
                                              +" number of scatterers (10^24): "+scatterern);      
    combos.scatterern = scatterern;
    combos.muttable = inners.muttable + outers.muttable;
    combos.setAbsInput();
    combos.setMulInput();
    System.out.println("Done.\n");
    
    return combos;    
  }
  
  void printScatterInfo () {
    if (isFlatPlate) System.out.println(SMASK[8]+":\n");
    System.out.println(SMASK[anmask]);
    System.out.println("absinput:\n"+absinput);
    System.out.println("mulinput:\n"+mulinput);
  }
  
  public static Object[] parseComposition (String composition_input){
    String[] list_composition = Pattern.compile("[\\s,]+").split(composition_input);
    int nelements = list_composition.length;
    if (nelements%2 != 0) {
      throw new RuntimeException("!!!!!!Unexpected error in parsing the composition input!!!!!!");
    }
    else { 
      nelements /= 2;
      String[] list_elements = new String[nelements];
      float[] list_fractions = new float[nelements];
      while (nelements-- > 0){
        list_elements[nelements] = list_composition[2*nelements];
        list_fractions[nelements] = (new Float(list_composition[2*nelements+1])).floatValue();
      }
//return an array with first element a string array of element symbols, second element
//a float array of formula numbers;     
    return new Object[] {list_elements, list_fractions};  
    }   
  }
  
  public String makeMut () {
    StringBuffer muttable = new StringBuffer ();
    String oneline;
    int nw=0;
    float lambda, sigma_t;  
    BufferedWriter bw = (new BufferedWriter(new StringWriter()));
    while ( (lambda = minw + dw * nw++) <  (maxw+0.000001f) ) {
      sigma_t = sigma_s+sigma_a*lambda/MutCross.RTLAMBDA;
      oneline = lambda + " " + sigma_t+"\n";
      muttable.append(oneline);
    }
    nlambda = nw - 1;  
    oneline = nlambda+" "+"0"+"\n";
    muttable.insert(0, oneline);  //"0" is the flag for 2 column (lambda, sigma_t) MUT table;
    
//      System.out.println(MutTable);
    return muttable.toString();
  }

  public void setMutTable () {
    muttable = effdensity+" "+sigma_a+"\n" 
              +makeMut();
  }
  
  public void setMutTable (String mutfile) {
    try {
      readMutTable(mutfile);      
    } catch(Throwable t) {
      System.err.println("error in reading mutfile\n");
      t.printStackTrace();
    }
  }  
  
  private void readMutTable (String mutfile) throws IOException, InterruptedException {
    StringBuffer input = new StringBuffer();
    BufferedReader fr_input = new BufferedReader(new FileReader(mutfile));
    String line = null;
    while( (line = fr_input.readLine()) != null ) {
      input.append(line+"\n");          
    }
    fr_input.close();
    input.insert(0, effdensity+" "+sigma_a+"\n");
    muttable = input.toString();
  }
  
  private String getAbsMulInputHead () {
    
    int n;
    StringBuffer input;
    
    if (!isFlatPlate) {    
      n = profile.length; 
      input = new StringBuffer(n+"\n");
      for(int i=0; i<n; i++){
        input.append(profile[i]+" ");
      }
      input.append("\n");
    } else {
      input = new StringBuffer(s2bangle+"\n");
    }
    
    n = angles.length;
    input.append(n+"\n");
    for(int i=0; i<n; i++){
      input.append(angles[i]+" ");
    }
    input.append("\n");
    
    if (!isFlatPlate) {    
      n = beamparams.length;
      for(int i=0; i<n; i++){
        input.append(beamparams[i]+" ");
      }
      input.append("\n");
    }
    
    return input.toString();
  }
  
  private String getAbsMulInputBody () {
    
    StringBuffer input = new StringBuffer(nan+"\n");
    int n;
    
    if (!isFlatPlate) {    
      n = rad.length; 
      for(int i=0; i<n; i++){
        input.append(rad[i]+" ");
      }
    } else {
      n = tss.length; 
      for(int i=0; i<n; i++){
        input.append(tss[i]+" ");
      }
    }    
    input.append("\n");
    
    return input.toString();
  }
  
  public void setAbsInput () {
    absinput = getAbsMulInputHead();
    if (!isFlatPlate) absinput += astep +"\n";
    else absinput += astep + " " + nplanes + "\n";
    absinput += getAbsMulInputBody() + muttable;    
  }
  
  public void setMulInput () {
    mulinput = getAbsMulInputHead();
    if (!isFlatPlate) mulinput += mstep +"\n";
    else mulinput += mstep + " " + nplanes +"\n";
    mulinput += getAbsMulInputBody() + muttable;
  }
  
//test;    
  public static void main(String[] args){
/*
    HIPPORunProps runinfo = HIPPORunProps.getExpProps();

    HIPPOScatter smp = new HIPPOScatter(runinfo, 1);
    smp.setMutTable();
    smp.setAbsInput();
    smp.setMulInput();
    System.out.println("\n******smp******");
    smp.printScatterInfo();
    HIPPOScatter can = new HIPPOScatter(runinfo, 2);
    can.setMutTable();
    can.setAbsInput();
    can.setMulInput();
    System.out.println("\n******can******");
    can.printScatterInfo();  
    HIPPOScatter smprun = smp.insideScatter(can);    
    System.out.println("\n******smp run******");
    smprun.printScatterInfo();
*/ 

//test for flatplate case:
   
    System.out.println("\n\n\n");
    HIPPORunProps runinfo_fp = HIPPORunProps.getExpProps("/IPNShome/taoj/cvs/ISAW/Databases/hippoprops_fp.dat");
    HIPPOScatter smp_fp = new HIPPOScatter(runinfo_fp, 011);
    smp_fp.setMutTable();
    smp_fp.setAbsInput();
    smp_fp.setMulInput();
    System.out.println("\n******smp_fp******");
    smp_fp.printScatterInfo();
    HIPPOScatter can_fp = new HIPPOScatter(runinfo_fp, 012);
    can_fp.setMutTable();
    can_fp.setAbsInput();
    can_fp.setMulInput();
    System.out.println("\n******can_fp******");
    can_fp.printScatterInfo();  
    HIPPOScatter smprun_fp = smp_fp.insideScatter(can_fp);    
    System.out.println("\n******smp run fp******");
    smprun_fp.printScatterInfo();

  }

}  




  
   
