/*
 * File: ExpDetector.java
 *
 * Copyright (C) 2003 Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
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
 * Revision 1.1  2003/05/20 18:36:40  pfpeterson
 * Added to CVS.
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.operator.DataSet.Attribute.LoadSCDCalib;
import DataSetTools.operator.Operator;
import DataSetTools.util.Format;
import DataSetTools.util.TextFileReader;
import DataSetTools.retriever.RunfileRetriever;
import java.io.IOException;

/** 
 * This class is for working with a detector in an SCD Experiement file.
 * @see WriteExp
 */
public class ExpDetector implements Comparable{
  private int   detNum  = 0;  // detector number
  private float detA    = 0f; // scattering angle w/ right side facing
                              // downstream being negative
  private float detA2   = 0f; // angle out of plane w/ up being positive
  private float detD    = 0f; // secondary flight path
  private float T0      = 0f; // from calibration
  private float x2cm    = 0f; // from calibration
  private float xbias   = 0f; // =width/2+xleft
  private float xbox85  = 0f; // =1/x2cm
  private float xleft   = 0f; // from calibration
  private int   xnum    = 0;  // number of columns
  private float xright  = 0f; // right extent of the detector from calibration
  private float width   = 0f; // from calibration
  private float y2cm    = 0f; // from calibration
  private float ybias   = 0f; // =yhgt/2+ylower
  private float ybox85  = 0f; // =1/y2cm 
  private float height = 0f; // from calibration
  private float ylower  = 0f; // from calibration
  private int   ynum    = 0;  // number of rows
  private float yupper  = 0f; // upper extent of the detector from calibration
  private float xdis    = 0f; // mystery
  private float ydis    = 0f; // mystery

  /**
   * Creates an ExpDetector instance. The DataSet provided is used to
   * fill in information about the detector, but no reference is kept.
   */  
  public ExpDetector(int detNum, DataSet ds){
    this.detNum=detNum;
    Object attr_val=null;

    // check that the detNum follows some rules
    if( detNum<=0 || detNum>999 )
      throw new InstantiationError("Invalid detNum ("+detNum
                                   +") must be between 0 and 1000");

    // create a map of the group ids in the detector
    int[][] ids=Util.createIdMap(ds,detNum);
    if(ids==null)
      throw new InstantiationError("Could not find detNum="+detNum);

    // find a data block with the correct detector number
    Data data=ds.getData_entry(ids[1][1]);
    if(Util.detectorID(data)!=detNum)
      throw new InstantiationError("Could not find detNum="+detNum);

    
    // scattering angle w/ right side facing downstream being negative
    attr_val=data.getAttributeValue(Attribute.DETECTOR_CEN_ANGLE);
    if(attr_val!=null && attr_val instanceof Float)
      this.detA=((Float)attr_val).floatValue();

    // angle out of plane w/ up being positive
    this.detA2=0f; 

    // secondary flight path in cm
    attr_val=data.getAttributeValue(Attribute.DETECTOR_CEN_DISTANCE);
    if(attr_val!=null && attr_val instanceof Float)
      this.detD=100f*((Float)attr_val).floatValue();

    // get the number of rows and columns
    IDataGrid datagrid=null;
    attr_val=data.getAttributeValue(Attribute.DETECTOR_DATA_GRID);
    if(attr_val!=null && attr_val instanceof IDataGrid)
      datagrid=(IDataGrid)attr_val;
    if(datagrid!=null){
      // number of columns
      this.xnum=datagrid.num_cols();
      // number of rows
      this.ynum=datagrid.num_rows();
      datagrid=null;
    }else{ // couldn't find the IDataGrid
      // number of columns
      this.xnum=ids.length-1; // these are 1-indexed arrays
      // number of rows
      this.ynum=ids[0].length-1; // these are 1-indexed arrays
    }      

    // get the calibration attribute
    attr_val=data.getAttributeValue(Attribute.SCD_CALIB);
    if(attr_val!=null && attr_val instanceof float[]){
      float[] calib = (float[])attr_val;
      this.T0       = calib[0]; // time offset
      this.x2cm     = calib[1]; // pixel slope 
      this.y2cm     = calib[2]; // pixel slope
      this.xleft    = calib[3]; // left side
      this.ylower   = calib[4]; // lower end
    }

    // detector dimensions
    this.width  = this.xnum*this.x2cm;
    this.height = this.ynum*this.y2cm;

    // detector center
    // (x/y)bias=(width/height)/2+(x/y)left
    this.xbias   = this.width/2f+this.xleft;
    this.ybias   = this.height/2f+this.ylower;

    // (x/y)box85=1/(x/y)2cm
    if(this.x2cm!=0f) this.xbox85=1f/this.x2cm;
    if(this.y2cm!=0f) this.ybox85=1f/this.y2cm;

    // right extent and upper extent of the detector
    this.xright  = this.xleft+this.width;
    this.yupper  = this.ylower+this.height;
  }
  
  /**
   * Constructor using a TextFileReader. This assumes that the
   * TextFileReader is already at the beginning of the detector.
   */
  public ExpDetector(TextFileReader tfr) throws IOException{
    String line=tfr.read_line();
    String tag=line.substring(0,6);

    this.detNum=Integer.parseInt(tag.substring(3,6).trim());

    String[] split=null;
    while(!tfr.eof() && line.startsWith(tag)){
      split=splitline(line);
      if("DETA  ".equals(split[0]))
        this.detA=Float.parseFloat(split[1].trim());
      else if("DETA2 ".equals(split[0]))
        this.detA2=Float.parseFloat(split[1].trim());
      else if("DETD  ".equals(split[0]))
        this.detD=Float.parseFloat(split[1].trim());
      else if("TZERO ".equals(split[0]))
        this.T0=Float.parseFloat(split[1].trim());
      else if("X2CM  ".equals(split[0]))
        this.x2cm=Float.parseFloat(split[1].trim());
      else if("XBIAS ".equals(split[0]))
        this.xbias=Float.parseFloat(split[1].trim());
      else if("XBOX85".equals(split[0]))
        this.xbox85=Float.parseFloat(split[1].trim());
      else if("XDIS  ".equals(split[0]))
        this.xdis=Float.parseFloat(split[1].trim());
      else if("XLEFT ".equals(split[0]))
        this.xleft=Float.parseFloat(split[1].trim());
      else if("XNUM  ".equals(split[0]))
        this.xnum=Integer.parseInt(split[1].trim());
      else if("XRIGHT".equals(split[0]))
        this.xright=Float.parseFloat(split[1].trim());
      else if("XWDTH ".equals(split[0]))
        this.width=Float.parseFloat(split[1].trim());
      else if("Y2CM  ".equals(split[0]))
        this.y2cm=Float.parseFloat(split[1].trim());
      else if("YBIAS ".equals(split[0]))
        this.ybias=Float.parseFloat(split[1].trim());
      else if("YBOX85".equals(split[0]))
        this.ybox85=Float.parseFloat(split[1].trim());
      else if("YDIS  ".equals(split[0]))
        this.ydis=Float.parseFloat(split[1].trim());
      else if("YHGT  ".equals(split[0]))
        this.height=Float.parseFloat(split[1].trim());
      else if("YLOWER".equals(split[0]))
        this.ylower=Float.parseFloat(split[1].trim());
      else if("YNUM  ".equals(split[0]))
        this.ynum=Integer.parseInt(split[1].trim());
      else if("YUPPER".equals(split[0]))
        this.yupper=Float.parseFloat(split[1].trim());

      line=tfr.read_line();
    }
    tfr.unread();
  }

  /**
   * Splits a line into three parts (the first of which is dropped)
   * into 6 characters, 6 characters, and the rest.
   */
  private static String[] splitline(String line){
    String[] result=new String[2];
    result[0]=line.substring(6,12);
    result[1]=line.substring(13);
    return result;
  }

  /**
   * Accessor method for the detector number.
   */
  public int detNum(){
    return this.detNum;
  }

  /**
   * Method to compare two ExpDetectors. This is done by comparing
   * the detector numbers.
   *
   * Note: this class has a natural ordering that is inconsistent with
   * equals.
   */
  public int compareTo(Object obj){
    ExpDetector det=null;
    if( obj instanceof ExpDetector)
      det=(ExpDetector)obj;
    else
      throw new ClassCastException();

    if(this.detNum==det.detNum)
      return 0;
    else if(this.detNum<det.detNum)
      return -1;
    else
      return 1;
  }

  /**
   * Override the default method to provide a string representation
   * ready for puting in a file.
   */
  public String toString(){
    String tag="DET"+Format.integer(this.detNum,3);
    String end_line=Format.string("\n",59);
    StringBuffer sb=new StringBuffer(21*81);

    sb.append(tag+"DETA  "+Format.real(this.detA,10,3)  +end_line);
    sb.append(tag+"DETA2 "+Format.real(this.detA2,10,3) +end_line);
    sb.append(tag+"DETD  "+Format.real(this.detD,10,3)  +end_line);
    sb.append(tag+"TZERO "+Format.real(this.T0,10,3)    +end_line);
    sb.append(tag+"X2CM  "+Format.real(this.x2cm,10,4)  +end_line);
    sb.append(tag+"XBIAS "+Format.real(this.xbias,10,3) +end_line);
    sb.append(tag+"XBOX85"+Format.real(this.xbox85,10,3)+end_line);
    sb.append(tag+"XDIS  "+Format.real(this.xdis,10,3)  +end_line);
    sb.append(tag+"XLEFT "+Format.real(this.xleft,10,3) +end_line);
    sb.append(tag+"XNUM  "+Format.integer(this.xnum,10) +end_line);
    sb.append(tag+"XRIGHT"+Format.real(this.xright,10,3)+end_line);
    sb.append(tag+"XWDTH "+Format.real(this.width,10,3) +end_line);
    sb.append(tag+"Y2CM  "+Format.real(this.y2cm,10,4)  +end_line);
    sb.append(tag+"YBIAS "+Format.real(this.ybias,10,3) +end_line);
    sb.append(tag+"YBOX85"+Format.real(this.ybox85,10,3)+end_line);
    sb.append(tag+"YDIS  "+Format.real(this.ydis,10,3)  +end_line);
    sb.append(tag+"YHGT  "+Format.real(this.height,10,3)+end_line);
    sb.append(tag+"YLOWER"+Format.real(this.ylower,10,3)+end_line);
    sb.append(tag+"YNUM  "+Format.integer(this.ynum,10) +end_line);
    sb.append(tag+"YUPPER"+Format.real(this.yupper,10,3)+end_line);

    return sb.toString();
  }

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run with some
   * default files. WARNING: this will NOT work for most people
   * without editing the 'datadir' varible in the source.
   *
   */
  public static void main( String args[] ){
    String datadir      = "/IPNShome/pfpeterson/data/SCD";
    //String datfile      = datadir+"/SCD06496.RUN";
    String datfile      = datadir+"/scd08299.run";
    RunfileRetriever rr = new RunfileRetriever(datfile);
    //DataSet mds         = rr.getDataSet(0);
    //DataSet rds         = rr.getDataSet(1);
    DataSet rds         = rr.getDataSet(2);
    Operator op         = null;

    //op=new LoadSCDCalib(rds,datadir+"/instprm.dat",1,null);
    op=new LoadSCDCalib(rds,datadir+"new/instprm.dat",1,null);
    System.out.println("LoadSCDCalib:"+op.getResult());
    op=new LoadSCDCalib(rds,datadir+"new/instprm.dat",2,null);
    System.out.println("LoadSCDCalib:"+op.getResult());

    ExpDetector det=null;
    //det=new ExpDetector(5,rds);
    det=new ExpDetector(17,rds);
    System.out.print(det.toString());
    det=new ExpDetector(18,rds);
    System.out.print(det.toString());

    System.exit(0);
  }
}
