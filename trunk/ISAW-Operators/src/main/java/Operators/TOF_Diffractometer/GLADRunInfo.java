/*
 * File:  GLADRunInfo.java
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
 *
 * Modified:
 * $Log$
 * Revision 1.2  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.1  2004/07/23 17:41:51  taoj
 * test version.
 *

 */
package Operators.TOF_Diffractometer;

import DataSetTools.dataset.Data;
import DataSetTools.util.SharedData;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.regex.Pattern;
import java.util.ArrayList;

/**
 * This class provides the IPNS GLAD instrument specific information.
 * It is now deprecated and replaced by GLADRunProps.java and GLADScatter.java.
 * 
 */
public class GLADRunInfo {
  
  public static final float TAU_DET = 8.0f; //detector dead time in microseconds;
  public static final float TAU_MON = 2.0f; //monitor dead time in microseconds;
  public static final int NLPSDID = 335; //max of LPSD index on GLAD (detID/64);
  public static final int NLPSD = 231; //number of GLAD detector LPSD;
  public static final int NSEGMENTS = 64; //# of segments on each LPSD;
  public static final int NDETCHANNEL = 2480; //# of detector time channels;
  public static final int NMONCHANNEL = 14950; //# of monitor time channels;
  public static final float DETCHANNELWIDTH = 5.0f; //detector time channel width;
  public static final float MONCHANNELWIDTH = 1.0f; //monitor time channel width;
  public static final int NSPECTRUM = 914; //# of data blocks in detector dataset;
  public static final int NMONITOR = 3; //#of monitors: [0]: beam monitor, 
                                                                     //[1] transmission monitor (fudge), [2] proton monitor;
  public static final float SOURCE_PERIOD = 3.33333e4f; //IPNS running at 30 Hz;
  public static final int DELAYED_NEUTRON_NFIT = 20; //first 20 channels used in delayed neutron correction fitting;
  public static final float DNFRACT = 0.00425f; //delayed neutron fraction;
  public static final float FACD = 0.41234502f; 
                          //for He3 detector: (number density)*(absorption cross section)*(detector radius)/1.8 Angstrom;
  public static final float[] AREAM = {6.4516f, 7.8e-3f, 0.0f}; //monitor areas;
  public static final float[] EFFM = {5.56e-5f, 1.0f, 0.0f}; //monitor efficiency coefficients;
  public static final int NUMQ = 40; //default number of Q bins in 1 /Angstrom, Qstep = 1/NUMQ;
  public static final float GLADQMAX = 40.0f; //max Q for data analysis;
//  public static final int NANGLES = 9;

//  public static Data dm_van_W = null; //vanadium calibration's beam monitor spectrum, needed for weighting;

// beam VAC info:

//  public static final float Vac1Bwid = 1.02f;
//  public static final float Vac1Bht = 2.30f;

    public static final float Vac1Bwid = 1.302f;
    public static final float Vac1Bht = 2.20f;
 
// vanadium calibration rods size info:
  public static final String[][] StdVan = {{"V"}};
  public static final float[][] StdFormula = {{1.0f}};
  public static final float[] Van1Size = {0.0f, 0.4763f, 6.0f}; //3/8" rod inner radius, outer radius, height;
  public static final float[] Van2Size = {0.0f, 0.3175f, 4.1f}; //1/4" rod 
  public static final float[] StdDensity = {0.07205f}; //number density of vanadium (1/Angstrom^2);
  public static final float StdSigma_a = 5.08f; //absorption crosssection of vanadium at 1.8 Angstrom;

// fused silica calibration rod:
  public static final float[] SilicaRodSize = {0.0f, 0.4415f, 6.0f};  
  public static final float SilicaRodDensity = 0.0662f;
  
// vanadium sample can info:
  
  public static final String[] CanVan = {"V"};
  public static final float[] CanFormula = {1.0f};
  public static final float CanDensity = 0.07205f;
  public static final float[] Can1Size = {0.4636f, 0.4763f, 6.0f}; //3/8" can inner radius, outer radius, height;
  public static final float[] Can2Size = {0.3048f, 0.3175f, 4.1f}; //1/4" can;

/*
//fudge for the TiZr can case:
  public static final String[] CanVan = {"Ti", "Zr"};
  public static final float[] CanFormula = {0.68f, 0.32f};
  public static final float CanDensity = 0.0542f;
  public static final float[] Can1Size = {0.40f, 0.45f, 6.0f}; //3/8" can inner radius, outer radius, height;
  public static final float[] Can2Size = {0.3048f, 0.3175f, 4.1f}; //1/4" can;
*/
/*  
//  fudge for the vanadium nuzzle case:
  public static final String[] CanVan = {"V"};
  public static final float[] CanFormula = {1.0f};
  public static final float CanDensity = 0.0542f;
  public static final float[] Can1Size = {0.5f, 0.7f, 0.15f}; //van nuzzle inner radius, outer radius, height;
  public static final float[] Can2Size = {0.3048f, 0.3175f, 4.1f}; //1/4" can;
*/  
  public static final String[] NullCan = {"Ti", "Zr"};
  public static final float[] NullCanFormula = {0.68f, 0.32f};
  public static final float NullCanDensity = 0.0542f;
  public static final float[] NullCanSize = {0.40f, 0.45f, 6.0f}; //TiZr "null" scattering alloy can, I/D 8 mm, O/D 9mm

  
  public static int[][] BankDet2lpsdID =null; //the mapping table from bank/det to a LPSD index;
  public static boolean[] LPSDBad = null;
  public static ArrayList LDeadDet = null;
  public static ArrayList LRemovedDataID = null;
  public static final String GLAD_PARM = "GLAD Instrument Parameters";
/*defns of GLAD_PARM:  
  scattering_angle = position.getScatteringAngle();
  domega = ((Float)attr_list_d.getAttributeValue(Attribute.SOLID_ANGLE)).floatValue();
  d2 = position.getDistance();
  psi = (float)(Math.PI/2-(position.getSphericalCoords())[2]);      
  dt_grp_params = new Float1DAttribute (GLADRunInfo.GLAD_PARM, new float[] {scattering_angle, d2, domega, psi});
*/    
  static String GLADDetsTable = null;
// find the pathname of GLAD detector setup file gladdets6.par;
  static{
    GLADDetsTable = SharedData.getProperty( "ISAW_HOME" );
    if (GLADDetsTable==null){
    GLADDetsTable = SharedData.getProperty("user.home");
    GLADDetsTable += java.io.File.separator+"ISAW";
    } 
    GLADDetsTable = GLADDetsTable.trim();
    GLADDetsTable += java.io.File.separator+"Databases";
    GLADDetsTable += java.io.File.separator+"gladdets6.par";
    if( new File(GLADDetsTable).exists()){
      // do nothing
    } else{
    GLADDetsTable = null;
    }
  }

//set up the GLAD detector mapping table from bank and detector number to the lpsd index (0..334); 
//NOTE: At the moment the only place this info is needed is for an user to input bad detector list in terms of bank/det number,
//               ISAW uses BankDet2lpsdID array to tranlate bank/det number to the lpsd index;   
  static void SetupDets(String gladdetstable) throws IOException, InterruptedException{
    
    BankDet2lpsdID = new int[11][53]; //10 banks (the extra dimension is for monitors), each bank has maximum 53 LPSD;
         
    BufferedReader fr_input = new BufferedReader(new FileReader(gladdetstable));
    String line = null;
    String[] list;
    int ibank, idet, ndet, id;
    
    for (int i = 0; i <= 10; i++){
      line = fr_input.readLine();
      list = Pattern.compile("\\s+").split(line.trim());
      ibank = (new Integer(list[0])).intValue();
      ndet = (new Integer(list[1])).intValue();
      for (int j = 0; j < ndet; j++){
        line = fr_input.readLine();
        list = Pattern.compile("\\s+").split(line.trim());
        idet = (new Integer(list[0])).intValue()-1;           
        id = (new Integer(list[4])).intValue();
        if (i != 0 && id != 0) {
          if ((id-1)%64 !=0) System.out.println("\n****unexpected error: unusual det ID***\n");
          BankDet2lpsdID[i][idet] = id/64+1; //bank index starts at 1, det index starts at 0, lpsdID starts at 1;
//          System.out.println("bank "+i+" det "+idet+" lpsdID: "+BankDet2lpsdID[i][idet]);
        }
      }
    }
     
    fr_input.close();
    }      

//test;    
  public static void main(String[] args){
    try {
          SetupDets(GLADDetsTable);
        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
  }
}

  
   