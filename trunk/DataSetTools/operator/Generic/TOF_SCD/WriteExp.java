/*
 * File: WriteExp.java
 *
 * Copyright (C) 2002 Peter F. Peterson
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
 * Revision 1.1  2002/12/09 18:26:59  pfpeterson
 * Added to CVS.
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.retriever.RunfileRetriever;
//import java.util.*;
import java.util.Vector;
//import java.lang.reflect.Array;
//import java.text.DecimalFormat;
import java.io.*;

/** 
 * This operator is a building block of an ISAW version of
 * A.J.Schultz's data reduction suite. This writes out an experiment
 * file in a format similar to GSAS.
 */
public class WriteExp extends GenericTOF_SCD{
  private static final String  TITLE      = "Write SCD Exp File";
  private static final boolean DEBUG      = false;
  private              int     ncol       = 0;
  private              int     nrow       = 0;
  private              float   width      = 0f;
  private              float   height     = 0f;
  private              DataSet ds         = null;
  private              DataSet mon        = null;
  private              String  crystinfo  = null;
  private              String  instinfo   = null;
  private              String  exp_title  = null;
  private              int     run_one    = 0;
  private              int     run_num    = 0;
  private              int[]   runs       = null;
  private              String  user       = null;
  private              String  exist_hist = null;
  
  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Write SCD Exp File" and a default
   *  list of parameters.
   */  
  public WriteExp(){
    super( TITLE );
  }
  
  /** 
   * Creates operator with title "Write SCD Exp File" and the
   * specified list of parameters. The getResult method must still be
   * used to execute the operator.
   *
   * @param ds Sample DataSet to write out
   * @param mon Monitor DataSet to write out
   * @param filename File to create
   * @param monid Upstream monitor group id
   * @param append Whether or not to overwrite the file (if it exists)
   */
  public WriteExp( DataSet ds, DataSet mon, String filename, int monid,
                   boolean append){

    this(); 
    getParameter(0).setValue(ds);
    getParameter(1).setValue(mon);
    getParameter(2).setValue(filename);
    getParameter(3).setValue(new Integer(monid));
    getParameter(4).setValue(new Boolean(append));
  }
  
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "WriteSCDExp", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "WriteSCDExp";
  }
  
  /* ------------------------ getDocumentation ---------------------------- */ 
  /**
   * The formatted document string for use in the help system.
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer(100);

    sb.append("@overview This operator is a building block of an ISAW version "
              +"of A.J.Schultz's data reduction suite. This writes out an "
              +"experiment file in a format similar to GSAS.\n");
    sb.append("@assumptions There are several important assumptions made by "
              +"this operator: there is only one detector, all of the needed "
              +"attributes are present in the sample DataSet, and the user "
              +"has write permission to the directory selected. In append "
              +"mode the existing file is assumed to have the correct "
              +"user-name, crystal symmetry, orientation matrix and "
              +"instrument information.\n");
    sb.append("@param The DataSet containing most of information to be "
              +"written out to the file.\n");
    sb.append("@param The DataSet containing the monitor spectrum and "
              +"information about it.\n");
    sb.append("@param The name of the file to write to (with path).\n");
    sb.append("@param The group ID of the upstream monitor.\n");
    sb.append("@param Whether not to append to an existing file. If the file "
              +"does not exist it will create it.\n");
    sb.append("@return The name of the file written.\n");
    sb.append("@error If there is an error in reading the existing experiment "
              +"file during an append.\n");
    sb.append("@error When there is an IOException during the writting of the "
              +"file.\n");

    return sb.toString();
  }

  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets reasonable default values for the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new Parameter("Data Set", new SampleDataSet() ) );
    addParameter( new Parameter("Monitor", new MonitorDataSet() ) );
    addParameter( new Parameter("Filename", new SaveFileString("")));
    addParameter( new Parameter("Upstream Monitor ID", new Integer(1 )) );
    addParameter( new Parameter("Append",   Boolean.FALSE));
  }
  
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns the name of the
   *  file written.
   */
  public Object getResult(){
    // get the parameters
    this.ds  = (DataSet)(getParameter(0).getValue());
    this.mon = (DataSet)(getParameter(1).getValue());
    String  filename = getParameter(2).getValue().toString();
    int     monid    = ((Integer)getParameter(3).getValue()).intValue();
    boolean append   = ((Boolean)(getParameter(4).getValue())).booleanValue();

    // determine what run number we are dealing with
    run_num=getIntValue(ds,Attribute.RUN_NUM);

    // get information out of the existing file
    if(append){
      ErrorString exist=readExisting(filename);
      if(exist!=null) return exist;
    }

    // fill in missing information
    if(exp_title==null)
      exp_title=(String)ds.getAttributeValue(Attribute.RUN_TITLE);
    if(run_one==0)
      run_one=run_num;
    else{
      if(run_num<run_one){
        run_one=run_num;
        exp_title=(String)ds.getAttributeValue(Attribute.RUN_TITLE);
      }
    }
    if(user==null || user.length()==0 )
      user=null;
    if(crystinfo==null)
      getCrystSymmAndOrient(ds);
    if(instinfo==null)
      getInstrument(monid);

    // start writting the file
    OutputStreamWriter out;
    try{
      // select writting to STDOUT if in debug mode
      if(DEBUG){
        out=new OutputStreamWriter(System.out);             // FOR TESTING ONLY
      }else{
        FileOutputStream fos=new FileOutputStream(filename,append);
        out=new OutputStreamWriter(fos);
      }

      // -------------------- generic information
      out.write(general());
      if(crystinfo!=null) out.write(crystinfo);

      // -------------------- previous data's information
      if(append && exist_hist!=null && exist_hist.length()>0){
        int num_early=0;
        for( int i=0 ; i<runs.length ; i++ ){
          if(runs[i]<run_num){
            num_early++;
          }
        }
        if(num_early>0){
          String match="HST"+Format.real(num_early+1,3);
          int index=exist_hist.indexOf(match);
          if(DEBUG) System.out.println("*****"+index+"*****"+match);
          if(index<0){
            out.write(exist_hist);
            exist_hist=null;
          }else if(index==0){
            // something wrong
            return new ErrorString("Could not find earlier data");
          }else{
            out.write(exist_hist.substring(0,index-1));
            exist_hist=exist_hist.substring(index);
          }
        }
      }

      // -------------------- this data's information
      parseDetInfoList(ds);
      out.write(histogram(run_num-run_one+1,monid));

      // -------------------- successor data's information
      if(append && exist_hist!=null && exist_hist.length()>0){
        out.write(fix_old(exist_hist));
      }

      // -------------------- instrument information
      if(instinfo!=null) out.write(instinfo);

      out.flush();
      out.close();
    }catch(IOException e){
      return new ErrorString("IOException: "+e.getMessage());
    }

    return filename;
  }
  
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    WriteExp op = new WriteExp();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /* ------------------------- private methods ---------------------------- */ 
  /**
   * Reformats a string containg the histogram section of an experment
   * file. The string is assumed to already be in the appropriate
   * format.
   *
   * @param old_hist The string to have the histogram number 'fixed'.
   */
  private String fix_old(String old_hist){
    int index=old_hist.indexOf("RUN#");
    int delHist=0;

    if(index<=0){
      return old_hist;
    }else{
      try{
        delHist=Integer.parseInt(old_hist.substring(index+4,index+25).trim());
        delHist=delHist-run_num;
      }catch(NumberFormatException e){
        System.out.println("Invalid Number: "+e.getMessage());
        return old_hist;
      }
    }
    if(delHist==0) return old_hist;

    StringBuffer new_sb=new StringBuffer(old_hist.length());
    StringBuffer old_sb=new StringBuffer(old_hist);
    int histnum=0;
    String hsttag="HST";

    try{
      while(old_sb.length()>0){
        if(old_sb.substring(0,3).equals(hsttag)){
          new_sb.append(hsttag);
          old_sb.delete(0,3);
          histnum=delHist+Integer.parseInt(old_sb.substring(0,3).trim());
          old_sb.delete(0,3);
          new_sb.append(Format.real(histnum,3)+old_sb.substring(0,75));
          old_sb.delete(0,75);
        }else{
          new_sb.append(old_sb.substring(0,81));
          old_sb.delete(0,81);
        }
      }
    }catch(NumberFormatException e){
      System.out.println("Invalid Number: "+e.getMessage());
      return old_hist;
    }

    return new_sb.toString();
  }

  /**
   * Create the first three lines of the experiment file.
   */
  private String general(){
    StringBuffer sb=new StringBuffer(3*80);
    String temp=null;

    // first line is the title
    sb.append("      DESCR   "+Format.string(exp_title,66,false)+"\n");
    // the second line is what the first run number is
    sb.append("      RUN1    "+Format.real(run_one,8)+Format.string("\n",59));
    // the third line is the user name
    sb.append("      USER  "+Format.string(user,68)+"\n");

    return sb.toString();
  }

  /**
   * Gets the crystal summetry and orientation matrix from the DataSet
   * and put it (formatted) into the 'crystinfo' instance variable.
   */
  private void getCrystSymmAndOrient(DataSet ds){
    StringBuffer sb     = new StringBuffer(3*80);
    Object       value  = null;
    float        vol    = 0f;
    float[]      latt   = new float[6];
    float[][]    orient = new float[3][3];
    
    // return crystinfo that is already in the file
    if( crystinfo!=null ) return;

    // get the unit cell volume
    value=ds.getAttributeValue(Attribute.CELL_VOLUME);
    if(value!=null && value instanceof Float){
      vol=((Float)value).floatValue();
    }else{
      crystinfo=null;
      return;
    }

    // get the lattice parameters
    value=ds.getAttributeValue(Attribute.LATTICE_PARAM);
    if(value!=null && value instanceof float[]){
      latt=(float[])value;
    }else{
      crystinfo=null;
      return;
    }

    // get the orientation matrix
    value=ds.getAttributeValue(Attribute.ORIENT_MATRIX);
    if(value!=null && value instanceof float[][]){
      orient=(float[][])value;
    }else{
      crystinfo=null;
      return;
    }

    // format the unit cell volume
    sb.append("CRS0  VSIGV  "+Format.real(vol,9,4)+"  "+Format.real(0f,8,4)
              +Format.string("\n",49));

    // format the lattice parameters
    sb.append("CRS1  ABC   "+Format.real(latt[0],10,4)
              +Format.real(latt[1],10,4)+Format.real(latt[2],10,4)
              +Format.string("\n",39));
    sb.append("CRS1  ABCSIG"+Format.real(0f,10,4)+Format.real(0f,10,4)
              +Format.real(0f,10,4)+Format.string("\n",39));
    sb.append("CRS1  ANGLES"+Format.real(latt[3],10,4)
              +Format.real(latt[4],10,4)+Format.real(latt[5],10,4)
              +Format.string("\n",39));
    sb.append("CRS1  ANGSIG"+Format.real(0f,10,4)+Format.real(0f,10,4)
              +Format.real(0f,10,4)+Format.string("\n",39));

    // format the orientation matrix
    sb.append("CRS11 UBMAT1"+Format.real(orient[0][0],10,6)
              +Format.real(orient[1][0],10,6)+Format.real(orient[2][0],10,6)
              +Format.string("\n",39));
    sb.append("CRS11 UBMAT2"+Format.real(orient[0][1],10,6)
              +Format.real(orient[1][1],10,6)+Format.real(orient[2][1],10,6)
              +Format.string("\n",39));
    sb.append("CRS11 UBMAT3"+Format.real(orient[0][2],10,6)
              +Format.real(orient[1][2],10,6)+Format.real(orient[2][2],10,6)
              +Format.string("\n",39));

    // set the cystal symmetry info
    crystinfo=sb.toString();
    return;
  }

  /**
   * Gets a simple float from the DataSet's attribute list. If it is
   * not found in the DataSet then this looks in the first Data of the
   * DataSet.
   */
  private static float getFloatValue(DataSet ds, String attr){
    Object value=null;
    
    value=ds.getAttributeValue(attr);
    if(value==null)
      value=ds.getData_entry(0).getAttributeValue(attr);

    if(value==null)
      return 0f;

    if(value instanceof Float)
      return ((Float)value).floatValue();
      
    if(value instanceof Integer)
      return (float)(((Integer)value).intValue());

    return 0f;
  }

  /**
   * Gets a simple int from the DataSet's attribute list. If it is not
   * found in the DataSet then this looks in the first Data of the
   * DataSet.
   */
  private static int getIntValue(DataSet ds, String attr){
    Object value=null;

    value=ds.getAttributeValue(attr);
    if(value==null)
      value=ds.getData_entry(0).getAttributeValue(attr);

    if(value==null)
      return 0;
    
    if(value instanceof Float)
      return (int)(((Float)value).floatValue());
    
    if(value instanceof Integer)
      return ((Integer)value).intValue();
    
    if(value instanceof int[])
      return ((int[])value)[0];

    return 0;
  }

  /**
   * Does all of the work with the Detector info list for getting the
   * size of the detector in rows and columns.
   */
  private void parseDetInfoList( DataSet ds ){
    Object           val        = null;
    DetectorInfo     detInfo    = null;
    //DetectorPosition lowerleft  = null;
    //DetectorPosition upperleft  = null;
    //DetectorPosition lowerright = null;
    int              col        = 0;
    int              row        = 0;
    int              mincol     = 10000;
    int              minrow     = 10000;

    for( int i=0 ; i<ds.getNum_entries() ; i++ ){
      val=ds.getData_entry(i).getAttributeValue(Attribute.DETECTOR_INFO_LIST);
      if(val!=null){
        if(val instanceof DetectorInfo[])
          detInfo=((DetectorInfo[])val)[0];
        else if(val instanceof DetectorInfo)
          detInfo=(DetectorInfo)val;
        else
          detInfo=null;
      }

      if(detInfo!=null){
        // get the row and column
        col=detInfo.getColumn();
        row=detInfo.getRow();

        // check for special positions
        if(col<=mincol){ // could be lowerleft or upperleft
          mincol=col;
          if(row<=minrow){ // is lowerleft
            minrow=row;
            //lowerleft=detInfo.getPosition();
            //}else if(row>=nrow){ // is upperleft
            //upperleft=detInfo.getPosition();
          }
        }else if(col>ncol){ // could be lowerright
          ncol=col;
          if(row<=minrow){ // is lowerright
            minrow=row;
            //lowerright=detInfo.getPosition();
          }
        }

        // check for the maximum row
        if(row>nrow) nrow=row;
      }
    }

    // set the number of rows and columns
    nrow=nrow-minrow+1;
    ncol=ncol-mincol+1;

    // calculate the height and width (the 100 is to convert to cm)
    //height = 100f*lowerleft.distance(upperleft);
    //width  = 100f*lowerleft.distance(lowerright);

    // clear out some references
    detInfo    = null;
    //lowerleft  = null;
    //upperleft  = null;
    //lowerright = null;
  }

  /**
   * Format the TOF entries for the experiment file.
   */
  private static String getTOF(String start_card, XScale xscale){
    StringBuffer sb         = new StringBuffer(10*80);
                 start_card = start_card+"TOF";
    int          ncolPerRow = 8;
    int          num_bin    = xscale.getNum_x()-1;// subtract one for histgrams
    int          num_row    = num_bin/ncolPerRow;

    if(num_bin%ncolPerRow>0) num_row++;

    int col=0;
    int row=0;
    for( int i=0 ; i<num_bin ; i++ ){
      if(col==0){
        sb.append(start_card+Format.real(row+1,3));
      }
      sb.append(Format.real(xscale.getX(i),8));
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
   * Determine the total counts in the entire detector.
   */
  private static float getTotalCount(DataSet ds){
    float count=0f;
    Object value=null;

    for( int i=0 ; i<ds.getNum_entries() ; i++ ){
      value=ds.getData_entry(i).getAttributeValue(Attribute.TOTAL_COUNT);
      if(value!=null && value instanceof Float)
        count=count+((Float)value).floatValue();
    }

    return count;
  }

  /**
   * Format the histogram section of the experiment file for the
   * DataSet being placed in it.
   */
  private String histogram(int histnum, int monid){
    StringBuffer sb         = new StringBuffer(40*80);
    String       start_card = "HST"+Format.string(Integer.toString(histnum),3);
    String       end_line   = Format.string("\n",59);
    Object       value      = null;
    String       tempS      = null;
    float        tempF      = 0f;
    int          tempI      = 0;
    float        sumtot     = getTotalCount(ds);
    float        plsnum     = getFloatValue(ds,Attribute.NUMBER_OF_PULSES);
    XScale       xscale     = ds.getData_entry(0).getX_scale();
    float        tmin       = xscale.getX(0);
    float        tmax       = xscale.getX(xscale.getNum_x()-1);
    float        detd       = 
      getFloatValue(ds,Attribute.DETECTOR_CEN_DISTANCE);
    float        l1         =
      getFloatValue(ds,Attribute.INITIAL_PATH);

    // the position of the chi motor
    tempF=getFloatValue(ds,Attribute.SAMPLE_CHI);
    sb.append(start_card+"CHI   "+Format.real(tempF,10,3)+end_line);
    // counts per pulse
    if( plsnum>0f )
      tempF=sumtot/plsnum;
    else
      tempF=0f;
    sb.append(start_card+"CTSPLS"+Format.real(tempF,10,2)+end_line);
    //  the angle of the detector center
    tempF=getFloatValue(ds,Attribute.DETECTOR_CEN_ANGLE);
    sb.append(start_card+"DETA  "+Format.real(tempF,10,3)+end_line);
    // the secondary flight path of the detector center
    sb.append(start_card+"DETD  "+Format.real(detd*100f,10,3)+end_line);
    // the end date of the measurement
    value=ds.getAttributeValue(Attribute.END_DATE);
    if(value!=null && value instanceof String)
      tempS=(String)value;
    else
      tempS=null;
    sb.append(start_card+"ENDDAT"+Format.string(tempS,68,false)+"\n");
    // the end time of the measurement
    value=ds.getAttributeValue(Attribute.END_TIME);
    if(value!=null && value instanceof String)
      tempS=(String)value;
    else
      tempS=null;
    sb.append(start_card+"ENDTIM"+Format.string(tempS,68,false)+"\n");
    // number of channels in monitor spectrum
    tempI=mon.getData_entry_with_id(monid).getX_scale().getNum_x();
    sb.append(start_card+"MNNUM "+Format.real(tempI,10)+end_line);
    // integrated monitor counts
    value=
     mon.getData_entry_with_id(monid).getAttributeValue(Attribute.TOTAL_COUNT);
    if(value!=null){
      if(value instanceof Float)
        tempI=(int)(((Float)value).floatValue());
      else if(value instanceof Integer)
        tempI=((Integer)value).intValue();
      else
        tempI=0;
    }else{
      tempI=0;
    }
    sb.append(start_card+"MNSUM "+Format.real(tempI,10)+end_line);
    // 
    tempI=0;
    sb.append(start_card+"NCH   "+Format.real(tempI,10)+end_line);
    // number of 'monitors'
    tempI=mon.getNum_entries();
    sb.append(start_card+"NMON  "+Format.real(tempI,10)+end_line);
    // the position of the omega motor
    tempF=getFloatValue(ds,Attribute.SAMPLE_OMEGA);
    sb.append(start_card+"OMEGA "+Format.real(tempF,10,3)+end_line);
    // the position of the phi motor
    tempF=getFloatValue(ds,Attribute.SAMPLE_PHI);
    sb.append(start_card+"PHI   "+Format.real(tempF,10,3)+end_line);
    // the number of pulses
    sb.append(start_card+"PLSNUM"+Format.real((int)plsnum,10)+end_line);
    // the run number of this dataset
    tempI=getIntValue(ds,Attribute.RUN_NUM);
    sb.append(start_card+"RUN#  "+Format.real(tempI,10)+end_line);
    // the start date of the measurement
    value=ds.getAttributeValue(Attribute.START_DATE);
    if(value!=null && value instanceof String)
      tempS=(String)value;
    else
      tempS=null;
    sb.append(start_card+"STRDAT"+Format.string(tempS,68,false)+"\n");
    // the start time of the measurement
    value=ds.getAttributeValue(Attribute.START_TIME);
    if(value!=null && value instanceof String)
      tempS=(String)value;
    else
      tempS=null;
    sb.append(start_card+"STRTIM"+Format.string(tempS,68,false)+"\n");
    // the total counts
    sb.append(start_card+"SUMTOT"+Format.real((int)sumtot,10)+end_line);
    // the maximum time
    sb.append(start_card+"TMAX  "+Format.real((int)tmax,10)+end_line);
    // the minimum time
    sb.append(start_card+"TMIN  "+Format.real((int)tmin,10)+end_line);
    // the bin boundaries
    sb.append(getTOF(start_card,xscale));
    // maximum wavelength
    tempF=tof_calc.Wavelength(detd+l1,tmax);
    sb.append(start_card+"WLMAX "+Format.real(tempF,10,3)+end_line);
    // minimum wavelength
    tempF=tof_calc.Wavelength(detd+l1,tmin);
    sb.append(start_card+"WLMIN "+Format.real(tempF,10,3)+end_line);
    // the number of columns
    sb.append(start_card+"XNUM  "+Format.real(ncol,10)+end_line);
    // the number of rows
    sb.append(start_card+"YNUM  "+Format.real(nrow,10)+end_line);

    return sb.toString();
  }

  /**
   * Produce the instrument section of the experiment file.
   */
  private void getInstrument(int monid){
    StringBuffer sb         = new StringBuffer(24*80);
    String       start_card = "INST  ";
    String       end_line   = Format.string("\n",59);

    Object       value      = null;
    String       tempS      = null;
    float        tempF      = 0f;
    int          tempI      = 0;
    float[]      calib      = null;
    XScale       mon_xscale = mon.getData_entry_with_id(monid).getX_scale();

    // the calibration
    value=ds.getAttributeValue(Attribute.SCD_CALIB);
    if(value==null){
      value=ds.getData_entry(0).getAttributeValue(Attribute.SCD_CALIB);
    }
    if(value!=null && value instanceof float[]){
      calib=(float[])value;
    }else
      calib=new float[5];

    // the width and height
    width=ncol*calib[1];
    height=nrow*calib[2];

    // detector center angle
    tempF=getFloatValue(ds,Attribute.DETECTOR_CEN_ANGLE);
    sb.append(start_card+"DETA  "+Format.real(tempF,10,2)+end_line);
    // detector center distance
    tempF=getFloatValue(ds,Attribute.DETECTOR_CEN_DISTANCE);
    sb.append(start_card+"DETD  "+Format.real(tempF*100f,10,2)+end_line);
    // primary flight path
    tempF=getFloatValue(ds,Attribute.INITIAL_PATH);
    sb.append(start_card+"L1    "+Format.real(tempF*100f,10,3)+end_line);
    // tmax of monitor spectrum
    tempI=mon_xscale.getNum_x();
    tempI=(int)mon_xscale.getX(tempI-1);
    sb.append(start_card+"TMAX  "+Format.real(tempI,10)+end_line);
    // tmin of monitor spectrum
    tempI=(int)mon_xscale.getX(0);
    sb.append(start_card+"TMIN  "+Format.real(tempI,10)+end_line);
    // TZERO from the calibration
    sb.append(start_card+"TZERO "+Format.real(calib[0],10,3)+end_line);
    // X2CM from the calibration
    sb.append(start_card+"X2CM  "+Format.real(calib[1],10,4)+end_line);
    // XBIAS=XWDTH/2+XLEFT
    tempF=width/2f+calib[3];
    sb.append(start_card+"XBIAS "+Format.real(tempF,10,3)+end_line);
    // XBOX85=1/X2CM
    if(calib[1]!=0f) tempF=1/calib[1];
    sb.append(start_card+"XBOX85"+Format.real(tempF,10,3)+end_line);
    //
    sb.append(start_card+"XDIS  "+Format.real(0f,10,3)+end_line);
    // XOFFSET from the calibration
    sb.append(start_card+"XLEFT "+Format.real(calib[3],10,3)+end_line);
    // right extent of detector from calibration
    sb.append(start_card+"XRIGHT"+Format.real(calib[3]+width,10,3)+end_line);
    // detector width from calibration
    sb.append(start_card+"XWDTH "+Format.real(width,10,3)+end_line);
    // Y2CM from the calibration
    sb.append(start_card+"Y2CM  "+Format.real(calib[2],10,4)+end_line);
    // YBIAS=YHGT/2+YLOWER
    tempF=height/2f+calib[4];
    sb.append(start_card+"YBIAS "+Format.real(tempF,10,3)+end_line);
    // YBOX85=1/Y2CM
    if(calib[2]!=0f) tempF=1/calib[2];
    sb.append(start_card+"YBOX85"+Format.real(tempF,10,3)+end_line);
    //
    sb.append(start_card+"YDIS  "+Format.real(0f,10,3)+end_line);
    // detector height from calibration
    sb.append(start_card+"YHGT  "+Format.real(height,10,3)+end_line);
    // YOFFSET from the calibration
    sb.append(start_card+"YLOWER"+Format.real(calib[4],10,3)+end_line);
    // upper extent of detector from calibration
    sb.append(start_card+"YUPPER"+Format.real(calib[4]+height,10,3)+end_line);

    instinfo=sb.toString();
    return;
  }

  /**
   * Read in the existing experiment file. Tags that are not
   * understood are brounght along and placed appropriately in the new
   * version of the file.
   */
  private ErrorString readExisting(String filename){
    File file=new File(filename);

    if(!file.exists()) return null;
    if(!file.canRead()) return new ErrorString("Cannot read "+filename);
    file=null;
    
    String temp=null;
    StringBuffer sb=new StringBuffer(8*81);
    int index=0;
    try{
      TextFileReader tfr=new TextFileReader(filename);
      // read in the experiment title
      if(!tfr.eof()){
        temp=tfr.read_line().trim();
        index=temp.indexOf("DESCR");
        if( index==0 )
          exp_title=temp.substring(5).trim();
        else
          tfr.unread();
      }
      
      // read in the first run number
      if(!tfr.eof()){
        temp=tfr.read_line().trim();
        index=temp.indexOf("RUN1");
        if( index==0 ){
          temp=temp.substring(4).trim();
          run_one=Integer.parseInt(temp);
        }else{
          tfr.unread();
        }
      }
        
      // read in the experiment title
      if(!tfr.eof()){
        temp=tfr.read_line().trim();
        index=temp.indexOf("USER");
        if( index==0 )
          user=temp.substring(4).trim();
        else
          tfr.unread();
      }
      
      // read in the crystal symmetry information
      sb.delete(0,sb.length());
      while(!tfr.eof()){
        temp=tfr.read_line();
        if(temp.indexOf("CRS")==0){
          sb.append(temp+"\n");
        }else{
          tfr.unread();
          break;
        }
      }
      crystinfo=sb.toString();

      // read in the histograms and all other stuff before INST information
      Vector runV=new Vector();
      sb.delete(0,sb.length());
      while(!tfr.eof()){
        temp=tfr.read_line();
        if(temp.indexOf("INST")==0){
          tfr.unread();
          break;
        }else{
          if( temp.indexOf("HST")==0 && temp.indexOf("RUN#")==6){
            runV.add(temp.substring(10).trim());
          }
          sb.append(temp+"\n");
        }
      }
      exist_hist=sb.toString();
      try{
        runs=new int[runV.size()];
        for( int i=0 ; i<runV.size() ; i++ ){
          runs[i]=Integer.parseInt((String)runV.elementAt(i));
        }
      }catch(NumberFormatException e){
        return new ErrorString(e.getMessage());
      }
      runV=null;

      // read in the instrument information and everything untill then EOF
      sb.delete(0,sb.length());
      while(!tfr.eof()){
        sb.append(tfr.read_line()+"\n");
      }
      instinfo=sb.toString();

      // close the file
      tfr.close();
    }catch(IOException e){
      return new ErrorString(e.getMessage());
    }catch(NumberFormatException e){
      return new ErrorString(e.getMessage());
    }


    return null;
  }

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run with some
   * default files. WARNING: this will NOT work for most people
   * without editing the 'datadir' varible in the source.
   *
   */
  public static void main( String args[] ){
    
    String datadir      = "/IPNShome/pfpeterson/data/SCD/";
    String datfile      = datadir+"SCD06496.RUN";
    RunfileRetriever rr = new RunfileRetriever(datfile);
    DataSet mds         = rr.getDataSet(0);
    DataSet rds         = rr.getDataSet(1);
    Operator op         = null;

    op=new LoadSCDCalib(rds,datadir+"instprm.dat",2,null);
    System.out.println("LoadSCDCalib:"+op.getResult());
    op=new LoadOrientation(rds,new LoadFileString(datadir+"blind.mat"));
    System.out.println("LoadOrientation:"+op.getResult());
    
    op = new WriteExp( rds,mds,datadir+"test.x", 1, true );
    System.out.println("RESULT: "+op.getResult());
    
    System.exit(0);
  }
}
