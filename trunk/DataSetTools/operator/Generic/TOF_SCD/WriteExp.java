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
 * Revision 1.13  2003/06/10 20:23:23  pfpeterson
 * Removed debug statements.
 *
 * Revision 1.12  2003/05/20 18:39:11  pfpeterson
 * Major rewrite to allow for a second detector. This was done by shifting
 * the code into a oop framework where the histograms and detectors are
 * their own objects rather than contained in strings within this class.
 *
 * Revision 1.11  2003/03/25 22:36:08  pfpeterson
 * Fixed the histogram renumbering when inserting at the beginning.
 *
 * Revision 1.10  2003/03/24 19:22:37  pfpeterson
 * Fixed histogram renumbering bug. Still exists if the run being appended
 * to the file should be placed before the first run.
 *
 * Revision 1.9  2003/03/21 20:13:52  pfpeterson
 * Fixed a memory leak with the operator keeping a reference to the
 * dataset and monitor.
 *
 * Revision 1.8  2003/02/18 20:21:01  dennis
 * Switched to use SampleOrientation attribute instead of separate
 * phi, chi and omega values.
 *
 * Revision 1.7  2003/02/12 22:56:30  pfpeterson
 * Writes out as much of the crystal symmetry and orientation matrix
 * as possible rather than all or nothing.
 *
 * Revision 1.6  2003/02/12 20:03:11  dennis
 * Switched to use PixelInfoList instead of SegmentInfoList
 *
 * Revision 1.5  2003/01/15 20:54:26  dennis
 * Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 * Revision 1.4  2002/12/20 20:26:48  pfpeterson
 * Now tries to get the user name from the AttributeList of the DataSet.
 *
 * Revision 1.3  2002/12/16 21:24:10  pfpeterson
 * Added a parameter to specify the calibration file and added the 'WLNUM' to
 * the output.
 *
 * Revision 1.2  2002/12/11 22:17:35  pfpeterson
 * Fixed formatting problem in TOF section of output file.
 *
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
import java.util.Vector;
import java.io.*;

/** 
 * This operator is a building block of an ISAW version of
 * A.J.Schultz's data reduction suite. This writes out an experiment
 * file in a format similar to GSAS.
 *
 * @see ExpDetector
 * @see ExpHistogram
 */
public class WriteExp extends GenericTOF_SCD{
  private static final String  TITLE      = "Write SCD Exp File";
  private static final boolean DEBUG      = false;
  
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
              +"this operator: all of the needed attributes are present in "
              +"the sample DataSet, the user has write permission to the "
              +"directory selected, and the information in the file takes "
              +"preceidence (instrument, orientation matrix, etc.) . In "
              +"append mode the existing file is assumed to have the correct "
              +"user-name, crystal symmetry, orientation matrix and "
              +"instrument information.\n");
    sb.append("@param The DataSet containing most of information to be "
              +"written out to the file.\n");
    sb.append("@param The DataSet containing the monitor spectrum and "
              +"information about it.\n");
    sb.append("@param The name of the file to write to (with path).\n");
    sb.append("@param The name of the calibration file (assumes first "
              +"line).\n");
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
    DataSet ds        = (DataSet)(getParameter(0).getValue());
    DataSet mon       = (DataSet)(getParameter(1).getValue());
    String  filename  = getParameter(2).getValue().toString();
    int     monid     = ((Integer)getParameter(3).getValue()).intValue();
    boolean append    = ((Boolean)(getParameter(4).getValue())).booleanValue();


    // ==================== create info for this dataset
    // get the user name
    String user=ds.getAttributeValue(Attribute.USER).toString();
    if(user==null) user="";

    // get the run title
    String exp_title=ds.getAttributeValue(Attribute.RUN_TITLE).toString();
    if(exp_title==null) exp_title="";

    // create the crystal symmetry infomation
    String crystinfo=getCrystSymmAndOrient(ds);
    if(crystinfo==null) crystinfo="";

    // create the instrument information
    String instinfo=getInstrument(ds);

    // determine list of detector numbers
    int[] detNums=getDetNums(ds);
    if( detNums==null )
      return new ErrorString("Could not find any detector numbers");
    if(DEBUG){
      System.out.print("DET=");
      for( int i=0 ; i<detNums.length ; i++ )
        System.out.print(detNums[i]+" ");
      System.out.println();
    }

    // create the detector and histogram objects
    Vector detectors=new Vector();
    Vector histograms=new Vector();
    {
      ExpDetector  det  = new ExpDetector(detNums[0],ds);
      ExpHistogram hist = new ExpHistogram(detNums[0],ds,monid,mon);
      detectors.add(det);
      histograms.add(hist);
      for( int i=1 ; i<detNums.length ; i++ ){
        det  = new ExpDetector(detNums[i],ds);
        hist = (ExpHistogram)hist.clone();
        hist.setDetNum(detNums[i],ds);
        detectors.add(det);
        histograms.add(hist);
      }
    }

    // ==================== read in the existing if necessary
    if(append){
      TextFileReader tfr=null;
      String line=null;
      int index=0;

      try{
        tfr=new TextFileReader(filename);
        // get the title
        line=tfr.read_line();
        index=line.indexOf("DESCR");
        if(index>=0)
          exp_title=line.substring(index+5).trim();
        else
          throw new IOException("Invalid file while reading DESCR");
        // skip the first run number
        line=tfr.read_line();
        // get the user name
        line=tfr.read_line();
        index=line.indexOf("USER");
        if(index>=0)
          user=line.substring(index+4).trim();
        else
          throw new IOException("Invalid file while reading USER");
        // read in the crystal symmetry
        {
          String temp=getCrystSymmAndOrient(tfr);
          if(temp!=null && temp.length()>0)
            crystinfo=temp;
        }
        // read in the detectors
        ExpDetector det=null;
        while(!tfr.eof() && tfr.read_line().startsWith("DET")){
          tfr.unread();
          det=new ExpDetector(tfr);
          addElement(det,detectors);
        }
        // read in the histograms
        ExpHistogram hist=null;
        int num_hist=0;
        while(!tfr.eof() && tfr.read_line().startsWith("HST") && num_hist<100){
          tfr.unread();
          hist=new ExpHistogram(tfr);
          if(DEBUG) System.out.println("hist"+hist.getRunNumber()+" "
                                       +hist.getDetNumber());
          addElement(hist,histograms);
          num_hist++;
        }
        // read in the instrument
        if(!tfr.eof()){
          StringBuffer sb=new StringBuffer(81);
          line=tfr.read_line();
          if(DEBUG) System.out.print("try:"+line);
          while(line.indexOf("INST  ")>=0 && ! tfr.eof()){
            sb.append(line+"\n");
            line=tfr.read_line();
          }
          instinfo=sb.toString();
        }
      }catch(IOException e){
        return new ErrorString(e.toString());
      }finally{
        if(tfr!=null){
          try{
            tfr.close();
          }catch(IOException e){
            // let it drop on the floor
          }
        }
      }
    }

    // set the first run number
    int first_run=((ExpHistogram)histograms.elementAt(0)).getRunNumber();
    if(DEBUG){
      System.out.println(">"+exp_title+"<");
      System.out.println(">"+first_run+"<");
      System.out.println(">"+user+"<");
    }

    // ==================== start writting the file
    OutputStreamWriter out;
    try{
      // select writting to STDOUT if in debug mode
      if(DEBUG){
        out=new OutputStreamWriter(System.out);             // FOR TESTING ONLY
      }else{
        FileOutputStream fos=new FileOutputStream(filename,false);
        out=new OutputStreamWriter(fos);
      }

      // -------------------- generic information
      // first line is the title
      out.write("      DESCR   "+Format.string(exp_title,66,false)+"\n");
      // the second line is what the first run number is
      out.write("      RUN1    "+Format.real(first_run,8)
                +Format.string("\n",59));
      // the third line is the user name
      out.write("      USER  "+Format.string(user,68,false)+"\n");

      // -------------------- orientation matrix and lattice parameters
      out.write(crystinfo);

      // -------------------- detectors
      for( int i=0 ; i<detectors.size() ; i++ )
        out.write(detectors.elementAt(i).toString());

      // -------------------- histograms
      for( int i=0 ; i<histograms.size() ; i++ ){
        ExpHistogram hist=(ExpHistogram)histograms.elementAt(i);
        hist.setHistNum(i+1);
        out.write(hist.toString());
      }

      // -------------------- instrument
      out.write(instinfo);

      // ==================== cleanup
      out.flush();
      out.close();
    }catch(IOException e){
      return new ErrorString("IOException: "+e.getMessage());
    }

    // release the DataSets
    ds  = null;
    mon = null;

    return filename;
  }
  
  /**
   * Inserts a comparable object into a vector. If two items are equal
   * than the new replaces the old.
   */
  private static void addElement(Comparable obj, Vector vec){
    for( int i=0 ; i<vec.size() ; i++ ){
      int compare=obj.compareTo(vec.elementAt(i));
      if(compare<0){
        vec.insertElementAt(obj,i);
        if(DEBUG)System.out.println("inserting object");
        return;
      }else if(compare==0){
        vec.set(i,obj);
        if(DEBUG)System.out.println("replacing object");
        return;
      }else{
        if(i==vec.size()-1){
          vec.add(obj);
          if(DEBUG)System.out.println("adding object");
          return;
        }
      }
    }
  }

  /**
   * Determine the detector numbers available in the dataset.
   */
  private static int[] getDetNums(DataSet ds){
    // determine the unique detector numbers
    Integer detNum=null;
    Vector detNumbers=new Vector();
    for( int i=0 ; i<ds.getNum_entries() ; i++ ){
      detNum=new Integer(Util.detectorID(ds.getData_entry(i)));
      if( ! detNumbers.contains(detNum) ) detNumbers.add(detNum);
    }
    if(detNumbers.size()<=0) return null;

    // copy over to the return array
    int[] detNums=new int[detNumbers.size()];
    if(detNumbers.size()==1){
      detNums[0]=((Integer)detNumbers.elementAt(0)).intValue();
    }else{
      // sort the results
      while(! detNumbers.isEmpty()){
        int minDet=1000;
        int minEle=-1;
        int tempDet=1000;
        for( int i=0 ; i<detNumbers.size() ; i++ ){
          tempDet=((Integer)detNumbers.elementAt(i)).intValue();
          if(tempDet<minDet){
            minDet=tempDet;
            minEle=i;
          }
        }
        detNums[detNums.length-detNumbers.size()]=minDet;
        detNumbers.removeElementAt(minEle);
      }
    }

    return detNums;
  }

  /**
   * Determines the starting index of the first histogram that is
   * after the one being added. Returns -1 if it can't find the right
   * place
   */
  private static int findSplit(String old_hist, int[] runs,int run_num,
                                                                 int run_zero){
    int num_early=0;
    for( int i=0 ; i<runs.length ; i++ ){
      if(runs[i]<run_num)
        num_early++;
    }
    
    if(num_early>0){
      if(num_early>=runs.length) return -1; // nothing special to do

      int index=-1;
      String match=null;
      for( int i=num_early ; i<runs.length ; i++ ){
        
        match="HST"+Format.real(runs[i]-run_zero+1,3);
        index=old_hist.indexOf(match);
        if(index>0)
          return index;
      }
    }else{
      return 0;
    }
    return -1;
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
  private String fix_old(String old_hist, int change_run_one){
    // confirm that it makes sense to do this
    if(old_hist==null || old_hist.length()<=0) return old_hist;
    if(change_run_one==0) return old_hist;

    // some local variables
    int index=0;
    int histNum=-1;
    StringBuffer buffer=new StringBuffer(old_hist);

    // do the replacing
    try{
      while(index>=0 && index<buffer.length()){
        histNum=Integer.parseInt(buffer.substring(index+3,index+6).trim());
        buffer.replace(index+3,index+6,Format.real(histNum+change_run_one,3));
        index=old_hist.indexOf("\n",index);
        if(index>0) index++;
      }
    }catch(NumberFormatException e){
      SharedData.addmsg("WARNING(WriteSCDExp): NumberFormatException "
                                                             +e.getMessage());
      return old_hist;
    }

    //return new_sb.toString();
    return buffer.toString();
  }

  /**
   * Just returns any line that starts with "CRS"
   */
  private static String getCrystSymmAndOrient(TextFileReader tfr)
                                                            throws IOException{
    String tag="CRS";
    StringBuffer sb=new StringBuffer(81*5);
    String line=tfr.read_line();
    while( !tfr.eof() && line.startsWith(tag) ){
      sb.append(line+"\n");
      line=tfr.read_line();
    }
    tfr.unread();
    return sb.toString();
  }

  /**
   * Gets the crystal summetry and orientation matrix from the DataSet
   * and put it (formatted) into the 'crystinfo' instance variable.
   */
  private static String getCrystSymmAndOrient(DataSet ds){
    StringBuffer sb     = new StringBuffer(3*80);
    Object       value  = null;
    float        vol    = 0f;
    float[]      latt   = new float[6];
    float[][]    orient = new float[3][3];
    
    // get the unit cell volume
    value=ds.getAttributeValue(Attribute.CELL_VOLUME);
    if(value!=null && value instanceof Float){
      vol=((Float)value).floatValue();
      sb.append("CRS0  VSIGV  "+Format.real(vol,9,4)+"  "+Format.real(0f,8,4)
                +Format.string("\n",49));
    }

    // get the lattice parameters
    value=ds.getAttributeValue(Attribute.LATTICE_PARAM);
    if(value!=null && value instanceof float[]){
      latt=(float[])value;
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
    }

    // get the orientation matrix
    value=ds.getAttributeValue(Attribute.ORIENT_MATRIX);
    if(value!=null && value instanceof float[][]){
      orient=(float[][])value;
      sb.append("CRS11 UBMAT1"+Format.real(orient[0][0],10,6)
                +Format.real(orient[1][0],10,6)+Format.real(orient[2][0],10,6)
                +Format.string("\n",39));
      sb.append("CRS11 UBMAT2"+Format.real(orient[0][1],10,6)
                +Format.real(orient[1][1],10,6)+Format.real(orient[2][1],10,6)
                +Format.string("\n",39));
      sb.append("CRS11 UBMAT3"+Format.real(orient[0][2],10,6)
                +Format.real(orient[1][2],10,6)+Format.real(orient[2][2],10,6)
                +Format.string("\n",39));
    }

    // return the cystal symmetry info
    return sb.toString();
  }

  /**
   * Produce the instrument section of the experiment file.
   */
  private String getInstrument(DataSet ds){
    String tag="INST  ";
    StringBuffer sb= new StringBuffer(81);
    Object attr_val=null;

    // get primary flight path in cm
    float l1=0f;
    attr_val=ds.getAttributeValue(Attribute.INITIAL_PATH);
    if(attr_val!=null && attr_val instanceof Float){
      l1=((Float)attr_val).floatValue();
    }else{
      attr_val=ds.getData_entry(0).getAttributeValue(Attribute.INITIAL_PATH);
      if(attr_val!=null && attr_val instanceof Float)
        l1=((Float)attr_val).floatValue();
    }
    sb.append(tag+"L1    "+Format.real(100f*l1,10,3)+Format.string("\n",59));

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
    DataSet mds         = rr.getDataSet(0);
    DataSet rds         = rr.getDataSet(2);
    Operator op         = null;

    op=new LoadSCDCalib(rds,datadir+"new/instprm.dat",2,null);
    System.out.println("LoadSCDCalib:"+op.getResult());
    op=new LoadSCDCalib(rds,datadir+"new/instprm.dat",1,null);
    System.out.println("LoadSCDCalib:"+op.getResult());
    op=new LoadOrientation(rds,new LoadFileString(datadir+"/8299/java/quartz.mat"));
    System.out.println("LoadOrientation:"+op.getResult());
    
    op = new WriteExp( rds, mds, datadir+"/test.x", 1, true );
    System.out.println("RESULT: "+op.getResult());
    
    System.exit(0);
  }
}
