/*
 * File: ExpHistogram.java
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
 * Revision 1.2  2003/08/05 21:34:39  dennis
 * Commented out debug print.
 *
 * Revision 1.1  2003/05/20 18:36:40  pfpeterson
 * Added to CVS.
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.XScale;
import DataSetTools.instruments.SampleOrientation;
import DataSetTools.math.tof_calc;
import DataSetTools.operator.DataSet.Attribute.LoadSCDCalib;
import DataSetTools.operator.Operator;
import DataSetTools.util.Format;
import DataSetTools.util.StringUtil;
import DataSetTools.util.TextFileReader;
import DataSetTools.retriever.RunfileRetriever;
import java.io.IOException;

/** 
 * This class is for working with a detector in an SCD Experiement file.
 * @see WriteExp
 */
public class ExpHistogram implements Comparable{
  private int   detNum  = 0;  // detector number
  private int   histNum = 0;  // histogram number

  // sample orientation
  private float chi   = 0f;
  private float phi   = 0f;
  private float omega = 0f;

  // dates and times
  private String end_date        = null;
  private String end_time        = null;
  private String start_date      = null;
  private String start_time      = null;

  private float counts_per_pulse = 0f;
  private int    mon_chan_num = 0;
  private int    mon_sum      = 0;
  private int    num_mon      = 0;
  private int    num_pulse    = 0;
  private int    run_num      = 0;
  private int    total_count  = 0;
  private int    tmax         = 0;
  private int    tmin         = 0;
  private float  wlmax        = 0f;
  private float  wlmin        = 0f;
  private int    wlnum        = 0;
  private XScale xscale       = null;
  private int    nch          = 0;

  /**
   * Simple constructor used for cloning
   */
  private ExpHistogram(int histNum, int detNum){
    this.histNum = histNum;
    this.detNum  = detNum;
  }

  /**
   * Creates an ExpHistogram instance. The DataSet provided is used to
   * fill in information about the histogram, but no reference is
   * kept.
   */  
  public ExpHistogram(int detNum, DataSet ds, int monid, DataSet mon_ds){
    // give default histogram number and set detector number
    this(1,detNum);

    Object attr_val=null;

    // check that the detNum follows some rules
    if( detNum<=0 || detNum>999 )
      throw new InstantiationError("Invalid detNum ("+detNum
                                   +") must be between 0 and 1000");

    // ==================== DataSet attributes
    // get sample orientation
    attr_val=ds.getAttributeValue(Attribute.SAMPLE_ORIENTATION);
    if(attr_val!=null && attr_val instanceof SampleOrientation){
      SampleOrientation orient=(SampleOrientation)attr_val;
      this.chi   = orient.getChi();
      this.phi   = orient.getPhi();
      this.omega = orient.getOmega();
      orient=null;
    }

    // the run number
    attr_val=ds.getAttributeValue(Attribute.RUN_NUM);
    if(attr_val!=null)
      this.run_num=getIntValue(attr_val);

    // get times and dates
    attr_val=ds.getAttributeValue(Attribute.END_DATE);
    if(attr_val!=null)
      this.end_date=attr_val.toString().trim();
    else
      this.end_date="";
    attr_val=ds.getAttributeValue(Attribute.END_TIME);
    if(attr_val!=null)
      this.end_time=attr_val.toString().trim();
    else
      this.end_time="";
    attr_val=ds.getAttributeValue(Attribute.START_DATE);
    if(attr_val!=null)
      this.start_date=attr_val.toString().trim();
    else
      this.start_date="";
    attr_val=ds.getAttributeValue(Attribute.START_TIME);
    if(attr_val!=null)
      this.start_time=attr_val.toString().trim();
    else
      this.start_time="";

    // number of pulses
    attr_val=ds.getAttributeValue(Attribute.NUMBER_OF_PULSES);
    if(attr_val!=null)
      this.num_pulse=getIntValue(attr_val);

    // ==================== Data attributes
    this.setDetNum(this.detNum,ds);

    // ==================== Monitor attributes
    Data data=mon_ds.getData_entry(monid);
    if(data==null) return; // escape out if can't find monitor spectrum

    // number of channels in monitor spectrum
    this.mon_chan_num=data.getX_scale().getNum_x()-1;

    // integrated monitor counts
    attr_val=data.getAttributeValue(Attribute.TOTAL_COUNT);
    if(attr_val!=null)
      this.mon_sum=getIntValue(attr_val);

    // number of 'monitors'
    this.num_mon=mon_ds.getNum_entries();
  }
  
  public ExpHistogram(TextFileReader tfr) throws IOException{
    String line=tfr.read_line();
    String tag=line.substring(0,6);
    this.histNum=Integer.parseInt(tag.substring(3,6).trim());

    StringBuffer tof_buffer=new StringBuffer(81);

    String[] split=null;
    while(!tfr.eof() && line.startsWith(tag)){
      split=splitline(line);

      if("CHI   ".equals(split[0]))
        this.chi=Float.parseFloat(split[1].trim());
      else if("CTSPLS".equals(split[0]))
        this.counts_per_pulse=Float.parseFloat(split[1].trim());
      else if("DETNUM".equals(split[0]))
        this.detNum=Integer.parseInt(split[1].trim());
      else if("ENDDAT".equals(split[0]))
        this.end_date=split[1].trim();
      else if("ENDTIM".equals(split[0]))
        this.end_time=split[1].trim();
      else if("MNNUM ".equals(split[0]))
        this.mon_chan_num=Integer.parseInt(split[1].trim());
      else if("MNSUM ".equals(split[0]))
        this.mon_sum=Integer.parseInt(split[1].trim());
      else if("NCH   ".equals(split[0]))
        this.nch=Integer.parseInt(split[1].trim());
      else if("NMON  ".equals(split[0]))
        this.num_mon=Integer.parseInt(split[1].trim());
      else if("OMEGA ".equals(split[0]))
        this.omega=Float.parseFloat(split[1].trim());
      else if("PHI   ".equals(split[0]))
        this.phi=Float.parseFloat(split[1].trim());
      else if("PLSNUM".equals(split[0]))
        this.num_pulse=Integer.parseInt(split[1].trim());
      else if("RUN#  ".equals(split[0]))
        this.run_num=Integer.parseInt(split[1].trim());
      else if("STRDAT".equals(split[0]))
        this.start_date=split[1].trim();
      else if("STRTIM".equals(split[0]))
        this.start_time=split[1].trim();
      else if("SUMTOT".equals(split[0]))
        this.total_count=Integer.parseInt(split[1].trim());
      else if("TMAX  ".equals(split[0]))
        this.tmax=Integer.parseInt(split[1].trim());
      else if("TMIN  ".equals(split[0]))
        this.tmin=Integer.parseInt(split[1].trim());
      else if(split[0].startsWith("TOF"))
        tof_buffer.append(" "+split[1]);
      else if("WLMAX ".equals(split[0]))
        this.wlmax=Float.parseFloat(split[1].trim());
      else if("WLMIN ".equals(split[0]))
        this.wlmin=Float.parseFloat(split[1].trim());
      else if("WLNUM ".equals(split[0]))
        this.wlnum=Integer.parseInt(split[1].trim());

      line=tfr.read_line();
    }
    getTOF(tof_buffer);

    tfr.unread();
  }

  private static String[] splitline(String line){
    String[] result=new String[2];
    result[0]=line.substring(6,12);
    result[1]=line.substring(13);
    return result;
  }

  /**
   * Mutator method for setting the histogram number
   */
  public void setHistNum(int histNum){
    if(histNum<=0 || histNum>999)
      throw new IllegalArgumentException("Invalid histogram number "+histNum);
    this.histNum=histNum;
  }

  /**
   * Accessor method for getting the run number
   */
  public int getRunNumber(){
    return this.run_num;
  }

  /**
   * Accessor method for getting the detector number
   */
  public int getDetNumber(){
    return this.detNum;
  }

  /**
   * Mutator method for setting information dependent on the detector
   * number (reinitialize them using the DataSet).
   */
  public void setDetNum(int detNum,DataSet ds){
    Object attr_val=null;

    // set the detector number
    this.detNum=detNum;

    // create a map of the group ids in the detector
    int[][] ids=Util.createIdMap(ds,detNum);
    if(ids==null)
      throw new InstantiationError("Could not find detNum="+detNum);

    // find a data block with the correct detector number
    Data data=ds.getData_entry(ids[1][1]);
    if(Util.detectorID(data)!=detNum)
      throw new InstantiationError("Could not find detNum="+detNum);

    // the xscale of the detector
    this.xscale=data.getX_scale();
    this.wlnum=this.xscale.getNum_x()-1;
    this.tmin=(int)Math.round(this.xscale.getX(0));
    this.tmax=(int)Math.round(this.xscale.getX(this.wlnum));

    // the wavelength range of the center of the detector
    float detd=0f;
    attr_val=data.getAttributeValue(Attribute.DETECTOR_CEN_DISTANCE);
    if(attr_val!=null && attr_val instanceof Float)
      detd=((Float)attr_val).floatValue();
    float l1=0f;
    attr_val=data.getAttributeValue(Attribute.INITIAL_PATH);
    if(attr_val!=null && attr_val instanceof Float)
      l1=((Float)attr_val).floatValue();
    if(l1!=0f && detd!=0f){
      this.wlmin = tof_calc.Wavelength(detd+l1,this.tmin);
      this.wlmax = tof_calc.Wavelength(detd+l1,this.tmax);
    }

    // total counts in the detector
    this.total_count=getTotalCount(ds,ids);

    // counts per pulse
    if(this.num_pulse>0)
      this.counts_per_pulse=(float)this.total_count/(float)this.num_pulse;


  }

  /**
   * Clone method provides a 'deep copy' of the object. The XScale
   * field is coppied by reference.
   */
  public Object clone(){
    ExpHistogram hist=new ExpHistogram(this.histNum,this.detNum);

    // sample orientation
    hist.chi   = this.chi;
    hist.phi   = this.phi;
    hist.omega = this.omega;

    // dates and times
    hist.end_date   = this.end_date;
    hist.end_time   = this.end_time;
    hist.start_date = this.start_date;
    hist.start_time = this.start_time;

    hist.counts_per_pulse = this.counts_per_pulse;
    hist.mon_chan_num     = this.mon_chan_num;
    hist.mon_sum          = this.mon_sum;
    hist.num_mon          = this.num_mon;
    hist.num_pulse        = this.num_pulse;
    hist.run_num          = this.run_num;
    hist.total_count      = this.total_count;
    hist.tmax             = this.tmax;
    hist.tmin             = this.tmin;
    hist.wlmax            = this.wlmax;
    hist.wlmin            = this.wlmin;
    hist.wlnum            = this.wlnum;
    hist.xscale           = this.xscale;

    return hist;
  }

  /**
   * Method to compare two ExpHistograms. This is done by comparing
   * the run and detector numbers.
   *
   * Note: this class has a natural ordering that is inconsistent with
   * equals.
   */
  public int compareTo(Object obj){
    ExpHistogram hist=null;
    if( obj instanceof ExpHistogram)
      hist=(ExpHistogram)obj;
    else
      throw new ClassCastException();

    // run number takes precedence
    if(this.run_num==hist.run_num){
      if(this.detNum==hist.detNum)
        return 0;
      else if(this.detNum<hist.detNum)
        return -1;
      else
        return 1;
    }else if(this.run_num<hist.run_num){
      return -1;
    }else{
      return 1;
    }
  }

  /**
   * Method build to return false as soon as possible.
   */
  public boolean equals(Object obj){
    // if it is the same object they must be equal
    if(obj==this) return true;

    ExpHistogram hist=null;
    if(obj instanceof ExpHistogram)
      hist=(ExpHistogram)obj;
    else
      return false;

    // try the 'easy' ones
    if(hist.histNum != this.histNum) return false;
    if(hist.run_num != this.run_num) return false;
    if(hist.detNum  != this.detNum)  return false;

    // sample orientation
    if(hist.chi   != this.chi)   return false;
    if(hist.phi   != this.phi)   return false;
    if(hist.omega != this.omega) return false;

    // dates and times
    if( ! hist.end_date.equals(this.end_date) ) return false;
    if( ! hist.end_time.equals(this.end_time) ) return false;
    if( ! hist.start_date.equals(this.start_date) ) return false;
    if( ! hist.start_time.equals(this.start_time) ) return false;

    if(hist.counts_per_pulse != this.counts_per_pulse) return false;
    if(hist.mon_chan_num     != this.mon_chan_num)     return false;
    if(hist.mon_sum          != this.mon_sum)          return false;
    if(hist.num_mon          != this.num_mon)          return false;
    if(hist.num_pulse        != this.num_pulse)        return false;
    if(hist.total_count      != this.total_count)      return false;
    if(hist.tmax             != this.tmax)             return false;
    if(hist.tmin             != this.tmin)             return false;
    if(hist.wlmax            != this.wlmax)            return false;
    if(hist.wlmin            != this.wlmin)            return false;
    if(hist.wlnum            != this.wlnum)            return false;

    if(! hist.xscale.equals(this.xscale)) return false;

    // all of the instance variables are the same so they must be the same
    return true;
  }

  /**
   * Override the default method to provide a string representation
   * ready for puting in a file.
   */
  public String toString(){
    String tag="HST"+Format.integer(this.histNum,3);
    String end_line=Format.string("\n",59);
    StringBuffer sb=new StringBuffer(21*81);

    sb.append(tag+"CHI   " +Format.real(this.chi,10,3)             +end_line);
    sb.append(tag+"CTSPLS" +Format.real(this.counts_per_pulse,10,2)+end_line);
    sb.append(tag+"DETNUM" +Format.integer(this.detNum,10)         +end_line);
    sb.append(tag+"ENDDAT "+Format.string(this.end_date,67,false)  +"\n");
    sb.append(tag+"ENDTIM "+Format.string(this.end_time,67,false)  +"\n");
    sb.append(tag+"MNNUM " +Format.integer(this.mon_chan_num,10)   +end_line);
    sb.append(tag+"MNSUM " +Format.integer(this.mon_sum,10)        +end_line);
    sb.append(tag+"NCH   " +Format.integer(this.nch,10)+end_line); // mystery
    sb.append(tag+"NMON  " +Format.integer(this.num_mon,10)        +end_line);
    sb.append(tag+"OMEGA " +Format.real(this.omega,10,3)           +end_line);
    sb.append(tag+"PHI   " +Format.real(this.phi,10,3)             +end_line);
    sb.append(tag+"PLSNUM" +Format.integer(this.num_pulse,10)      +end_line);
    sb.append(tag+"RUN#  " +Format.integer(this.run_num,10)        +end_line);
    sb.append(tag+"STRDAT "+Format.string(this.start_date,67,false)+"\n");
    sb.append(tag+"STRTIM "+Format.string(this.start_time,67,false)+"\n");

    sb.append(tag+"SUMTOT" +Format.integer(this.total_count,10)    +end_line);
    sb.append(tag+"TMAX  " +Format.integer(this.tmax,10)           +end_line);
    sb.append(tag+"TMIN  " +Format.integer(this.tmin,10)           +end_line);
    sb.append(getTOF(tag,this.xscale));
    sb.append(tag+"WLMAX " +Format.real(this.wlmax,10,3)           +end_line);
    sb.append(tag+"WLMIN " +Format.real(this.wlmin,10,3)           +end_line);
    sb.append(tag+"WLNUM " +Format.integer(this.wlnum,10)          +end_line);

    return sb.toString();
  }

  private static int getIntValue(Object attr_val){
    if(attr_val==null)
      return 0;
    else if(attr_val instanceof Integer)
      return ((Integer)attr_val).intValue();
    else if(attr_val instanceof Float)
      return (int)Math.round(((Float)attr_val).floatValue());
    else if(attr_val instanceof int[])
      return ((int[])attr_val)[0];
    else if(attr_val instanceof Integer[])
      return ((Integer[])attr_val)[0].intValue();
    else
      return 0;
  }

  /**
   * This method empties the StringBuffer.
   */
  private void getTOF(StringBuffer sb){
    this.xscale=null;

    float[] x_val=new float[this.wlnum+1];
    for(int i=0 ; i<x_val.length-1 ; i++ ){
      StringUtil.trim(sb);
      x_val[i]=StringUtil.getFloat(sb);
    }
    x_val[x_val.length-1]=this.tmax;

    this.xscale=XScale.getInstance(x_val);
//  System.out.println("XSCALE="+this.xscale.getNum_x()+","+this.wlnum+" "+sb
//                     +" "+x_val[x_val.length-1]);
  }

  /**
   * Create the TOF section of the String returned by {@link
   * ExpHistogram.toString()}
   */
  private static String getTOF(String tag, XScale xscale){
    if(xscale==null) return "";

    StringBuffer sb         = new StringBuffer(10*80);
    int          ncolPerRow = 8;
    int          num_bin    = xscale.getNum_x()-1;// subtract one for histgrams
    int          num_row    = num_bin/ncolPerRow;

    tag=tag+"TOF";

    if(num_bin%ncolPerRow>0) num_row++;

    int col=0;
    int row=0;
    for( int i=0 ; i<num_bin ; i++ ){
      if(col==0){
        sb.append(tag+Format.real(row+1,3));
      }
      sb.append(Format.real(xscale.getX(i),8,1));
      if(col==ncolPerRow-1){
        col=-1;
        row++;
        sb.append("    \n"); // bin boundaries
      }
      col++;
    }

    if(col>0)
      sb.append(Format.string("\n",5+(ncolPerRow-col)*8));

    return sb.toString();
  }

  /**
   * Determine the total count in a detector as defined by the ids.
   */
  private static int getTotalCount(DataSet ds,int[][] ids){
    if(ds==null || ds==DataSet.EMPTY_DATA_SET)
      return 0;
    if(ids==null || ids.length<=0 || ids[0].length<=0)
      return 0;

    int    count = 0;
    Data   data  = null;

    for( int i=0 ; i<ids.length ; i++ ){
      for( int j=0 ; j<ids[i].length ; j++ ){
        if(ids[i][j]==-1) continue; // don't bother getting value
        data=ds.getData_entry(ids[i][j]);
        if(data==null) continue;    // don't bother getting value
        count=count+getIntValue(data.getAttributeValue(Attribute.TOTAL_COUNT));
      }
    }

    return count;
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
    DataSet mds         = rr.getDataSet(0);
    //DataSet rds         = rr.getDataSet(1);
    DataSet rds         = rr.getDataSet(2);
    Operator op         = null;

    //op=new LoadSCDCalib(rds,datadir+"/instprm.dat",1,null);
    //System.out.println("LoadSCDCalib:"+op.getResult());
    op=new LoadSCDCalib(rds,datadir+"new/instprm.dat",1,null);
    System.out.println("LoadSCDCalib:"+op.getResult());
    op=new LoadSCDCalib(rds,datadir+"new/instprm.dat",2,null);
    System.out.println("LoadSCDCalib:"+op.getResult());

    ExpHistogram hist=null;
    ExpHistogram hist2=null;
    //hist=new ExpHistogram(5,rds,1,mds);
    //System.out.print(hist.toString());
    hist=new ExpHistogram(17,rds,1,mds);
    //System.out.print(hist.toString());
    hist2=(ExpHistogram)hist.clone();
    System.out.println("00:"+hist.equals(hist2));
    System.out.println("01:"+hist.compareTo(hist2));
    hist2.setDetNum(18,rds);
    System.out.println("02:"+hist.equals(hist2));
    System.out.println("03:"+hist.compareTo(hist2));
    //hist2.setHistNum(2);
    //System.out.print(hist.toString());

    System.exit(0);
  }
}
