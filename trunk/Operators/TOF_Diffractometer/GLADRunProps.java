/*
 * File:  GLADRunProps.java
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
 *
 */

package Operators.TOF_Diffractometer;

import DataSetTools.dataset.Attribute;
import DataSetTools.util.SharedData;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;
import java.lang.StringBuffer;

/**
 * This class provides the IPNS GLAD instrument, experimental
 * and analysis specific information.
 * 
 */
public class GLADRunProps {
  
  public static final String GLAD_PARM = "GLAD_Detector_Parameters";
  public static final String GLAD_PROP = "GLAD_Running_Info";
  
  public static String ISAWDBDirectory = null;
  public static String GLADDefInstProps = null;  
  private static String GLADDetTable = null;
  
  static final String SYMBOL = ".SYMBOL", FORMULA = ".FORMULA", DENSITY = ".DENSITY",
             SIZE = ".SIZE", SIGMA_A = ".SIGMA_A", PROFILE = ".PROFILE", ASTEP = ".ASTEP",
             MSTEP = ".MSTEP", ANGLES = ".ANGLES";    

//   find the pathname of GLAD property file gladprops.dat (default)
//   and detector setup file gladdets6.par;
  static {
    ISAWDBDirectory = SharedData.getProperty( "ISAW_HOME" );
    if (ISAWDBDirectory == null){
      ISAWDBDirectory = SharedData.getProperty("user.home");
      ISAWDBDirectory += java.io.File.separator+"ISAW";
    } 
    ISAWDBDirectory = ISAWDBDirectory.trim();
    ISAWDBDirectory += java.io.File.separator+ "Databases";
    GLADDefInstProps = ISAWDBDirectory + java.io.File.separator + "gladprops.dat";
    GLADDetTable = ISAWDBDirectory + java.io.File.separator+ "gladdets6.par";

    if( new File(GLADDefInstProps).exists()){
      // do nothing
    } else{
      GLADDefInstProps = null;
    }
    if( new File(GLADDetTable).exists()){
      // do nothing
    } else{
      GLADDetTable = null;
    }
  }

//storing the info;
  HashMap ExpConfiguration;
  static int[][] BankDet2lpsdID;
  boolean badLPSD[] = null;
  int deadDet[] = null, removedDataID[] = null; 

  /**
   *  read in the info from the default property file.
   */  
  private GLADRunProps() {
    try {
      setExpProps(GLADDefInstProps);     
    } catch(Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   *  read in the info from a user given file.
  */  
  private GLADRunProps(String fprops) {
    try {
      setExpProps(fprops);      
    } catch(Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   *  Set the parameters to default values.
   */ 
  public GLADRunPropAttribute getAttribute (String name, Object[] value) {
    return new GLADRunPropAttribute (name, value);
  }

//GLADRunPropAttribute object will be used to ship the info within ISAW;    
  protected class GLADRunPropAttribute extends Attribute {
    private Object[] value;
    
    public GLADRunPropAttribute( String name, Object[] value ){
      super( name );
      this.value = value;
    }

    public Object getValue( ){
      return value.clone();
    }
    
    public String getStringValue(){
      StringBuffer svs = new StringBuffer();
      for (int i = 0; i < value.length; i++) svs.append(i+" ");
      return svs.toString();
    }
    
    public double getNumericValue(){
      return 0.0;
    }
    
    public String toString(){
      return getName() + "::" + getStringValue();
    }    
  }
  
  public static GLADRunProps getExpProps () {
    return new GLADRunProps();
  }
  
  public static GLADRunProps getExpProps (String fin) {
     return new GLADRunProps(fin);
   }
  
  private void setExpProps (String fin) throws IOException, InterruptedException {
    Properties props = new Properties();
    FileInputStream in = new FileInputStream(fin);
    props.load(in);
    in.close(); 
    props.list(System.out);

    ExpConfiguration = trExpProps(props);
    System.out.println("\nHashMap ExpConfiguration:");
    printExpHashMap (ExpConfiguration);

    setDetTable();
  }       

  public static HashMap trExpProps (Properties expprop) {
    HashMap expHmap = new HashMap(expprop);
    Iterator ekeys = expHmap.keySet().iterator();

    Float Fvalue;
    Integer Ivalue;
    float fvals[];
    String key, svalue, list[];
    int n;
    Pattern regi = Pattern.compile("^(\\d+)$");
    Pattern regf = Pattern.compile("^(\\d*\\.?\\d*e?\\d*)$");
    Pattern regfvs = Pattern.compile("^\\[(.+)\\]$");
    Pattern regsvs = Pattern.compile("^\\{(.+)\\}$");
    Pattern token = Pattern.compile("[\\s,]+");
    Pattern pxe = Pattern.compile("(.+?)\\.?(\\d*)$");
    Matcher m;
    
    while ( ekeys.hasNext() ) {
      key = (String) ekeys.next();
      svalue = ((String) expHmap.get(key)).trim();
      if (!svalue.equals("")) {
        
        m = regi.matcher(svalue);
        if (m.matches()) {
          svalue = m.group(1);
          Ivalue = new Integer ( svalue );
          expHmap.put(key, Ivalue);
//          System.out.println("key: "+key+" Integer: "+Ivalue);
          continue;
        }
        
        m = regf.matcher(svalue);
        if (m.matches()) {
          svalue = m.group(1);
          Fvalue = new Float (svalue);
          expHmap.put(key, Fvalue);
//          System.out.println("key: "+key+" Float: "+Fvalue);
          continue;
        }
        
        m = regfvs.matcher(svalue);
        if (m.matches()) {
          svalue = m.group(1);
          list = token.split(svalue);
          n = list.length;
          fvals = new float[n];
//          System.out.print("key: "+key+" ");
          while (--n >= 0) {fvals[n] = (new Float(list[n])).floatValue(); /*System.out.print("fvals["+n+"]: "+fvals[n]+" ");*/}
//          System.out.print("\n");
          expHmap.put(key, fvals);
          continue;
        }
        
        m = regsvs.matcher(svalue);
        if (m.matches()) {
          svalue = m.group(1);
          list = token.split(svalue);
          n = list.length;
//          System.out.print("key: "+key+" ");
//          while (--n>=0) System.out.print("list["+n+"]: "+list[n]+" ");
//          System.out.print("\n");
          expHmap.put(key, list);
          continue;
        }
        
 //       System.out.println("key: "+key+" svalue: "+svalue);
        expHmap.put(key, svalue);
      }
      else {/*System.out.println("key: "+key+" value: null");*/ expHmap.put(key, null);} 
    }
    
    setExpKeys (expHmap, "GLAD.EXP.CALIB", pxe);
    setExpKeys (expHmap, "GLAD.EXP.SMP", pxe);
    
    if (expHmap.get("GLAD.EXP.CAN") != null)
      setExpKeys (expHmap, "GLAD.EXP.CAN", pxe);
    
    return expHmap;
  }
    
  private static boolean setExpKeys (HashMap hmap, String expkeyh, Pattern p) {    

    if ( hmap.get(expkeyh) != null ) {
      String dbkeyh = (String) hmap.get(expkeyh);
      Matcher m = p.matcher(dbkeyh);
      if (m.matches()) {
        dbkeyh = m.group(1);
        String sizetag = m.group(2);
        hmap.put(expkeyh+SYMBOL, hmap.get(dbkeyh+SYMBOL));
        hmap.put(expkeyh+FORMULA, hmap.get(dbkeyh+FORMULA));
        hmap.put(expkeyh+DENSITY, hmap.get(dbkeyh+DENSITY));
        hmap.put(expkeyh+SIGMA_A, hmap.get(dbkeyh+SIGMA_A)); 
//        System.out.print("dbkeyh: "+dbkeyh+" sizetag: "+sizetag);
        dbkeyh += (sizetag.equals(""))?SIZE:(SIZE+"."+Integer.parseInt(sizetag));
//        System.out.println(" expkeyh+size: "+expkeyh+sizetag+" dbkeyh: "+dbkeyh);
        hmap.put(expkeyh+SIZE, hmap.get(dbkeyh));
      }
      return true;
    }
    else if (hmap.get(expkeyh+SYMBOL) == null) {
      System.out.println("\n******WARNINGS******\n"+expkeyh+" KEYS NOT SET IN EXP CONFIG FILE\n");
      return false;
    } 

    return false;
  }
  
  public static void printExpHashMap (HashMap hmap) {
    Iterator keys = hmap.keySet().iterator();
    String key, aboutv; //maybe change key to a stringbuffer if this method is used often?
    Object value, vs[];
    float fvs[];
    Class vclass;
    
    while (keys.hasNext()) {
      key = (String) keys.next();
      value = hmap.get(key);
      
      if (value != null) {
        vclass = value.getClass();
        
        if ((vclass).isArray()) {
          aboutv = "";
//float[] case;
          if (vclass.getName().equals("[F")) {
            fvs = (float[]) value;
            for (int i = 0; i < fvs.length; i++) aboutv += fvs[i]+" ";
          } 
//(nonprimitive) Object[];        
          else {
            vs = (Object[]) value;
            for (int i = 0; i < vs.length; i++) aboutv += vs[i]+" ";
          }     
        }
//regular object;              
        else aboutv = String.valueOf(value);
        aboutv += "::"+vclass.getName();
      } 
      else aboutv = String.valueOf(value); //"null" string;

      System.out.println(key+"--->"+aboutv);    
    }

  }
  
  public void setDetTable () throws IOException, InterruptedException {
//  set up the GLAD detector mapping table from bank and detector number to the lpsd index (0..334); 
//  NOTE: At the moment the only place this info is needed is for an user to input bad detector list in terms of bank/det number,
//                 ISAW uses BankDet2lpsdID array to translate bank/det number to the lpsd index;       
 
    int nbank = ((Integer) ExpConfiguration.get("GLAD.DET.NBANK")).intValue();
    int mlpsd = ((Integer) ExpConfiguration.get("GLAD.DET.MAX_LPSD_BANK")).intValue();
    BankDet2lpsdID = new int[nbank+1][mlpsd];   //10 banks (the extra dimension is for monitors), each bank has maximum 53 LPSD;

    BufferedReader fr_input = new BufferedReader(new FileReader(GLADDetTable));
    String line = null;
    String[] list;
    int ibank, idet, ndet, id;
    Pattern token = Pattern.compile("\\s+");
    
    for (int i = 0; i <= 10; i++){
      line = fr_input.readLine();
      list = token.split(line.trim());
      ibank = (new Integer(list[0])).intValue();
      ndet = (new Integer(list[1])).intValue();
      for (int j = 0; j < ndet; j++){
        line = fr_input.readLine();
        list = token.split(line.trim());
        idet = (new Integer(list[0])).intValue()-1;           
        id = (new Integer(list[4])).intValue();
        if (i != 0 && id != 0) {
          if ((id-1)%64 !=0) System.out.println("\n****unexpected error: unusual det ID***\n");
          BankDet2lpsdID[i][idet] = id/64+1; //bank index starts at 1, det index starts at 0, lpsdID starts at 1;
//              System.out.println("bank "+i+" det "+idet+" lpsdID: "+BankDet2lpsdID[i][idet]);
        }
      }
    }
    fr_input.close();    
  }
  
  public static void setBadLPSD (GLADRunProps runinfo, String gladrunpar) throws IOException, InterruptedException {
         
    BufferedReader fr_input = new BufferedReader(new FileReader(gladrunpar));
    String element_symbol, line = null;
    String[] list;
    int nbank = ((Integer) runinfo.ExpConfiguration.get("GLAD.DET.NBANK")).intValue();
    int[] nbaddets = new int[nbank];
    int nlpsd = ((Integer) runinfo.ExpConfiguration.get("GLAD.DET.MAXLPSDID")).intValue();
    runinfo.badLPSD = new boolean[nlpsd];
    int idet, nbad;
    Pattern token = Pattern.compile("\\s+");
      
    while((line = fr_input.readLine()) != null ) {
      if(line.charAt(0) == '#') continue;
      list = token.split(line.trim());
      for (int i = 0; i < 10; i++){
        nbaddets[i] = (new Integer(list[i])).intValue();
      }
      break;
    }
      
    for (int i = 0; i < 10; i++){
      nbad = nbaddets[i];
      if (nbad != 0){
        while((line = fr_input.readLine()) != null ) {
          if(line.charAt(0) == '#') continue;
          list = token.split(line.trim());
          if (list.length != nbad) System.out.println("\n****unexpected error***\n");
          for (int j = 0; j < nbad; j++){
            idet =  (new Integer(list[j])).intValue()-1;
            runinfo.badLPSD[BankDet2lpsdID[i+1][idet]-1] = true;
//            System.out.println(" BAD: bank "+(i+1)+" idet "+idet);
          }            
          break;
        } 
      }        
    }

    fr_input.close();
  }
  
//test;    
  public static void main(String[] args){
    try {
      GLADRunProps exp = getExpProps();
//      exp.getExpProps();      

        } catch(Throwable t) {
          System.out.println("unexpected error");
          t.printStackTrace();
        }
  }
}

  
   