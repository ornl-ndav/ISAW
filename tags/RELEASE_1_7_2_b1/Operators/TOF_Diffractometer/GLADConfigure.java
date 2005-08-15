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

/**
 * This operator sets up the GLAD instrument and experimental configuration
 * using GLADRunProps and GLADScatter classes;
 */
public class GLADConfigure implements Wrappable, IWrappableWithCategoryList { 
  //~ Instance fields **********************************************************
  
  /* @param configfile use ISAW GUI to input the absolute path of the configfile;
   */
  public LoadFileString configfile = new LoadFileString(GLADRunProps.GLADDefInstProps);
  public boolean hasCan = true;

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
    DataSet ds0 = new DataSet ("DS0", 
      "Construct a dummy dataset holding instrument and experimental infomation.");

    Object[] props = new Object[5];
    GLADRunProps runinfo = GLADRunProps.getExpProps(configfile.toString());
    props[0] = runinfo;
    
    GLADScatter van = new GLADScatter(runinfo, 0);
    van.setMutTable();
    van.setAbsInput();
    van.setMulInput();
    van.anmask = 0;
    props[1] = van;
    
    GLADScatter smp = new GLADScatter(runinfo, 1);
    smp.anmask = 1;
    smp.setMutTable();
    smp.setAbsInput();
    smp.setMulInput();
    
    if (hasCan) {
      GLADScatter can = new GLADScatter(runinfo, 2);
      can.setMutTable();
      can.setAbsInput();
      can.setMulInput();
//      System.out.println("can.muttable:\n"+can.muttable);
      can.anmask = 2;
      props[3] = can;
      smp = smp.insideScatter(can);
    }
    
    props[2] = smp;

    Attribute runprops = runinfo.getAttribute(GLADRunProps.GLAD_PROP, props);
    ds0.setAttribute(runprops, 0);
//    System.out.println("smp.muttable:\n"+smp.muttable);
    return ds0;    
  }

  /* --------------------------- MAIN METHOD --------------------------- */
  public static void main (String[] args) {      
    //unit testing;
    GLADConfigure testconf = new GLADConfigure();
    DataSet ds0 = (DataSet)testconf.calculate();      
  
  }


}
