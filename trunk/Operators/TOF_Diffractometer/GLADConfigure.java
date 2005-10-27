/*
 * File:  GLADConfigure.java
 *
 * Copyright (C) 2005 J. Tao
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
 */

package Operators.TOF_Diffractometer;

import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Attribute;
import DataSetTools.operator.*;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import gov.anl.ipns.Util.SpecialStrings.StringChoiceList;
import java.io.File;

/**
 * This operator sets up the GLAD instrument and experimental configuration
 * using GLADRunProps and GLADScatter classes;
 */
public class GLADConfigure implements Wrappable, IWrappableWithCategoryList { 
  //~ Instance fields **********************************************************
  
  /* @param configfile use ISAW GUI to input the absolute path of the configfile;
   */
  
//  public StringChoiceList calibopt = getCalibOptions();
//  public String calibopt = getCalibOptions().toString();
  public String calibopt = "1: 3/8 inch vanadium rod";
  public String smpcomposition = getComposition(1);
  public float smpdensity = GLADRunProps.getfloatKey(GLADRunProps.defGLADProps, "GLAD.EXP.SMP.DENSITY"); 
  public String canopt = "1: 3/8 inch vanadium can";
  public float scc;
  public LoadFileString configfile = new LoadFileString(GLADRunProps.GLADDefInstProps);
  public boolean hasCan = true;
  public boolean hasFur = false;
  public boolean isFP = false;

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
    return "GLAD_CONFIGURE";                      //operator name used in ISAW scripts;
  }

  /**
   * Returns the documentation for this method as a String.  The format follows
   * standard JavaDoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer( "" );
    s.append( "@overview This operator accepts a GLAD configuration file " );
    s.append( "in the format of Java program property file " );
    s.append( "(for an example/template, see the \"gladprops.dat\" under /Databases " );
    s.append( "subdirectory), parses, translates and stores the GLAD instrument and ");
    s.append( "experimental configuration information in a Java HashMap. ");
    s.append( "It also sets up one GLADScatter object for each experimental ");
    s.append( "configuration containing information used by the following analysis steps.\n");
    s.append( "@assumptions The specified configfile exists and is valid.\n" );
    s.append( "@algorithm Please refer to the comments in the body of source codes.\n" );
    s.append( "@param configfile configfile.\n" );
    s.append( "@error ...\n" );

    return s.toString(  );
  }

  /**
   * Converts run file data to a dataset of normalized counts in Q.
   *
   * @return The processed DataSet.
   */
  public Object calculate(  ) {
    int iflag = (isFP)?8:0;
    DataSet ds0 = new DataSet ("DS0", 
      "Construct a dummy dataset holding instrument and experiment infomation.");

    Object[] props = new Object[5]; //0: run info; 1: vanadium; 2: smp (rod or w/t can); 3: can; 4: furnace;
    GLADRunProps runinfo;
    String configfilestr = configfile.toString();
    if (!(new File(configfilestr)).equals(new File(GLADRunProps.GLADDefInstProps))) runinfo = GLADRunProps.getExpProps(configfilestr);
    else runinfo = GLADRunProps.getExpProps();
    props[0] = runinfo;    

    String calibstr = calibopt.toString();
    String calibstr0 = (String) GLADRunProps.defGLADProps.get("GLAD.EXP.CALIB");
    char sizechar = calibstr.charAt(0);
    if ( sizechar != calibstr0.charAt(calibstr0.length()-1) && sizechar != 'x') {
      runinfo.ExpConfiguration.put("GLAD.EXP.CALIB.SIZE", runinfo.ExpConfiguration.get("GLAD.CALIB.VAN.SIZE."+sizechar));
    }
    GLADScatter van = new GLADScatter(runinfo, 0);
    van.setMutTable();
    van.setAbsInput();
    van.setMulInput();
//    van.anmask = 0;
    props[1] = van;
//    van.printScatterInfo();
    
    Object[] scainfo = GLADScatter.parseComposition(smpcomposition);
    runinfo.ExpConfiguration.put("GLAD.EXP.SMP.SYMBOL", scainfo[0]);
    runinfo.ExpConfiguration.put("GLAD.EXP.SMP.FORMULA", scainfo[1]);
    runinfo.ExpConfiguration.put("GLAD.EXP.SMP.DENSITY", new Float(smpdensity));
    
    GLADScatter smp = new GLADScatter(runinfo, 1+iflag);
    smp.setMutTable();
    smp.setAbsInput();
    smp.setMulInput();
    
    if (hasCan) {
      String canstr0 = (String) GLADRunProps.defGLADProps.get("GLAD.EXP.CAN");
      sizechar = canopt.charAt(0);
      if ( sizechar != calibstr0.charAt(calibstr0.length()-1) && sizechar != 'x') {
        runinfo.ExpConfiguration.put("GLAD.EXP.CAN.SIZE", runinfo.ExpConfiguration.get("GLAD.CAN.VAN.SIZE."+sizechar));
      }
      GLADScatter can = new GLADScatter(runinfo, 2+iflag);
      can.setMutTable();
      can.setAbsInput();
      can.setMulInput();

      if (hasFur) {
        GLADScatter fur = new GLADScatter(runinfo, 4);
        fur.setMutTable();
        fur.setAbsInput();
        fur.setMulInput();
        props[4] = fur;
        fur.printScatterInfo();
        can = can.insideScatter(fur);
      }

      props[3] = can;
//      can.printScatterInfo();
      smp = smp.insideScatter(can);
    }
    if (scc != 0.0f) smp.scatterern = scc;    
    props[2] = smp;
//    smp.printScatterInfo();    

    Attribute runprops = runinfo.getAttribute(GLADRunProps.GLAD_PROP, props);
    ds0.setAttribute(runprops, 0);
//    System.out.println("smp.muttable:\n"+smp.muttable);
    return ds0;    
  }
  
  public static String getComposition (int imask) {
    
    String[] symbol = null;
    float[] formula = null;
    StringBuffer composition = new StringBuffer();
    
    switch (imask) {
      case 0:
        symbol = (String[])GLADRunProps.defGLADProps.get("GLAD.EXP.CALIB.SYMBOL");
        formula = (float[])GLADRunProps.defGLADProps.get("GLAD.EXP.CALIB.FORMULA");
        break;
      case 1:
        symbol = (String[])GLADRunProps.defGLADProps.get("GLAD.EXP.SMP.SYMBOL");
        formula = (float[])GLADRunProps.defGLADProps.get("GLAD.EXP.SMP.FORMULA");
        break;
      case 2:
        symbol = (String[])GLADRunProps.defGLADProps.get("GLAD.EXP.CAN.SYMBOL");
        formula = (float[])GLADRunProps.defGLADProps.get("GLAD.EXP.CAN.FORMULA");
        break;
//furnace:
      case 4:

      default:        
    }
        
    for (int i = 0; i < symbol.length; i++) {
      composition.append(symbol[i]+" "+formula[i]+" ");
    }
    return composition.toString();
  }
  
  public static StringChoiceList getCalibOptions () {
    String calibval = (String) GLADRunProps.defGLADProps.get("GLAD.EXP.CALIB");
    if (calibval.endsWith("1")) return new StringChoiceList (new String[] 
                       {"1: \"3/8 vanadium rod", "2: \"1/4 vanadium rod", "x: others"});
    if (calibval.endsWith("2")) return new StringChoiceList (new String[] 
                       {"2: \"1/4 vanadium rod", "1: \"3/8 vanadium rod", "x: others"});
    else return new StringChoiceList (new String[] 
    {"x: others", "1: \"3/8 vanadium rod", "2: \"1/4 vanadium rod"});
  }
  
  /* --------------------------- MAIN METHOD --------------------------- */
  public static void main (String[] args) {      
    //unit testing;
    GLADConfigure testconf = new GLADConfigure();
    testconf.calibopt = "2:";
    testconf.canopt = "2:";
    testconf.smpcomposition = "C 1.0 Cl 4.0";
    testconf.smpdensity = 0.0319f;
    DataSet ds0 = (DataSet)testconf.calculate();      
  
  }


}
