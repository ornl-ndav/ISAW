/*
 * File:  IndexJ.java   
 *
 * Copyright (C) 2003, Peter F. Peterson
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
 * Revision 1.15  2003/06/10 22:32:24  bouzekc
 * Added parameters for delta (h), delta (k), and delta (l).
 *
 * Revision 1.14  2003/06/09 21:59:43  bouzekc
 * getResult() now returns the name of the IndexJ log file
 * and prints the number of peaks indexed to SharedData.
 *
 * Revision 1.13  2003/06/05 22:05:28  bouzekc
 * Fixed incorrect parameter setting in constructor.
 *
 * Revision 1.12  2003/05/20 19:42:19  pfpeterson
 * Added parameter to restrict run number of indexed peaks.
 *
 * Revision 1.11  2003/05/14 21:01:46  pfpeterson
 * Removed a debug print statement.
 *
 * Revision 1.10  2003/05/07 18:37:53  dennis
 * removed redundant code that created parameters vector twice.
 *
 * Revision 1.9  2003/03/14 22:51:47  pfpeterson
 * More appropriately sets the reflection flag.
 *
 * Revision 1.8  2003/02/28 20:58:18  pfpeterson
 * Changed a Parameter to a BooleanPG.
 *
 * Revision 1.7  2003/02/26 22:22:07  pfpeterson
 * Fixed bug where was not indexing peaks that were close to integer
 * values because index%1>.5
 *
 * Revision 1.6  2003/02/18 22:59:00  pfpeterson
 * Updated calls to deprecated method fixSparator.
 *
 * Revision 1.5  2003/02/13 20:34:24  pfpeterson
 * Added debug messages, made updating peaks file an option, and added
 * writing information out to a log file, index.log.
 *
 * Revision 1.4  2003/02/11 21:03:39  pfpeterson
 * Uses routine to read orientation matrix from LoadOrientation.
 *
 * Revision 1.3  2003/02/10 15:27:33  pfpeterson
 * Changed the 'title' and 'command' to be 'JIndex'.
 *
 * Revision 1.2  2003/02/03 19:28:15  pfpeterson
 * Added a file filter to the matrix file parameter.
 *
 * Revision 1.1  2003/01/31 23:03:01  pfpeterson
 * Added to CVS.
 *
 *
 */

package Operators.TOF_SCD;

import  java.io.*;
import  java.text.DecimalFormat;
import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.operator.DataSet.Attribute.LoadOrientation;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.Generic.TOF_SCD.*;
import  DataSetTools.parameter.*;

/**
 * This operator is intended to run A.J. Schultz's "index"
 * program. This is not heavily tested but works fairly well.
 */
public class IndexJ extends    GenericTOF_SCD {
  public static String command=null;
  private static final boolean DEBUG=false;

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct an operator with a default parameter list.
   */
  public IndexJ( ){
    super( "JIndex" );
  }
  
  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct operator to execute index.
   *
   *  @param peaksfile The name of the peaks file to index. This name is
   *  used to generate the experiment name used by index.
   *  @param matrixfile The name of the file containing the
   *  orientation matrix. Currently must be a '.mat' file.
   *  @param delta The error parameter for indexing peaks (h = k = l)
   */
  
  public IndexJ( String peaksfile, String matrixfile, float delta,
                 boolean update){
    this();
    
    getParameter(0).setValue(peaksfile);
    getParameter(1).setValue(matrixfile);
    getParameter(3).setValue(new Float(delta));
    getParameter(4).setValue(new Float(delta));
    getParameter(5).setValue(new Float(delta));
    getParameter(6).setValue(new Boolean(update));
  }
  
  /* ------------------------- setDefaultParmeters ----------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters = new Vector();  // must do this to create empty list of 
                                // parameters
    LoadFilePG peaksfilepg=new LoadFilePG("Peaks file",null);
    peaksfilepg.setFilter(new PeaksFilter());
    //0
    addParameter(peaksfilepg);
    LoadFilePG matfilepg=new LoadFilePG("Matrix file",null);
    matfilepg.setFilter(new MatrixFilter());
    //1
    addParameter(matfilepg);
    //2
    addParameter(new IntArrayPG("Restrict Runs",null));
    //3
    addParameter(new FloatPG("Delta h",0.20f));
    //4
    addParameter(new FloatPG("Delta k",0.20f));
    //5
    addParameter(new FloatPG("Delta l",0.20f));
    //6
    addParameter(new BooleanPG("update peaks file",true));
  }
  
  /**
   * This returns the help documentation
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer(100);

    // overview
    sb.append("@overview This is a java version of \"INDEX\" maintained by A.J.Schultz. This depends on two other operators being present: ReadPeaks and WritePeaks. Eventually this operator should not call these directly, but have the user do it through a script.");
    // parameters
    sb.append("@param String name of the peaks file");
    sb.append("@param String name of the file containing the orientation matrix. This can either be a matrix file \".mat\" or an experiment file \".x\".");
    sb.append("@param float the allowable uncertainty in the calculated hkl values. This is the same for all three.");
    // return
    sb.append("@return The number of peaks indexed out of the total number present.");
    // error
    sb.append("@error If anything goes wrong while reading either the peaks or matrix file");

    return sb.toString();
  }
  
  /* --------------------------- getCommand ------------------------------ */
  /**
   * @return the command name to be used with script processor, in
   * this case Index.
   */
  public String getCommand(){
    return "JIndex";
  }
  
  /* --------------------------- getResult ------------------------------- */
  /*
   * Runs scalar using the specified parameters
   */
  public Object getResult(){
    ErrorString eString     = null;
    String      peaksfile   = null;  // peaks filename
    String      matrixfile  = null;  // matrix filename
    String      dir         = null;  // directory that the files are all in
    String      expname     = null;  // the experiment name
    String      logfile     = null;  // the index.log file
    File        file        = null;  // for tests
    int         index       = 0;     // for chopping up strings
    float       delta_h     = 0f;    // error in index (h) allowed
    float       delta_k     = 0f;    // error in index (k) allowed
    float       delta_l     = 0f;    // error in index (l) allowed
    boolean     update      = false; // update the peaks file
    int         crystallite = 1;     // placeholder for future feature
    int[]       runs        = null;  // run numbers to index
    int[]       seqs        = null;  // sequence numbers to index

    // get the peaks file name from the script and test it out
    peaksfile=getParameter(0).getValue().toString();
    if(peaksfile!=null && peaksfile.length()>0){
      peaksfile=FilenameUtil.setForwardSlash(peaksfile);
      file=new File(peaksfile);
      if( !file.exists() )
        return new ErrorString("file does not exist: "+peaksfile);
      else if( !file.isFile() )
        return new ErrorString("not a regular file "+peaksfile);
      else if( !file.canRead() )
        return new ErrorString("file is not readable "+peaksfile);
    }

    // get the matrix filename
    matrixfile=getParameter(1).getValue().toString();
    if(matrixfile!=null && matrixfile.length()>0){
      matrixfile=FilenameUtil.setForwardSlash(matrixfile);
      file=new File(matrixfile);
      if( !file.exists() )
        return new ErrorString("file does not exist: "+matrixfile);
      else if( !file.isFile() )
        return new ErrorString("not a regular file "+matrixfile);
      else if( !file.canRead() )
        return new ErrorString("file is not readable "+matrixfile);
    }

    IParameter iparm=null;

    // get the run numbers
    iparm=getParameter(2);
    if(iparm instanceof IntArrayPG){
      runs=((IntArrayPG)iparm).getArrayValue();
    }else{
      String value=iparm.getValue().toString();
      runs=IntList.ToArray(value);
    }
    if(runs==null || runs.length==0) runs=null;

    // get the delta (h) parameter
    iparm=getParameter(3);
    if(iparm instanceof FloatPG){
      delta_h=((FloatPG)iparm).getfloatValue();
    }else{
      Object value=iparm.getValue();
      if(value instanceof Float ){
        delta_h=((Float)value).floatValue();
      }else{
        return new ErrorString("invalid value "+value);
      }
    }

    // get the delta (k) parameter
    iparm=getParameter(4);
    if(iparm instanceof FloatPG){
      delta_k=((FloatPG)iparm).getfloatValue();
    }else{
      Object value=iparm.getValue();
      if(value instanceof Float ){
        delta_k=((Float)value).floatValue();
      }else{
        return new ErrorString("invalid value "+value);
      }
    }

    // get the delta (l) parameter
    iparm=getParameter(5);
    if(iparm instanceof FloatPG){
      delta_l=((FloatPG)iparm).getfloatValue();
    }else{
      Object value=iparm.getValue();
      if(value instanceof Float ){
        delta_l=((Float)value).floatValue();
      }else{
        return new ErrorString("invalid value "+value);
      }
    }

    // find out if we want to update peaks file
    iparm=getParameter(6);
    if(iparm instanceof BooleanPG){
      update=((BooleanPG)iparm).getbooleanValue();
    }else{
      Object value=iparm.getValue();
      if(value instanceof Boolean){
        update=((Boolean)value).booleanValue();
      }else{
        return new ErrorString("invalid value of update: "+value);
      }
    }

    // have someone else read in our peaks file
    Vector peaks=null;
    {
      ReadPeaks readPeaksOp=new ReadPeaks(peaksfile);
      Object res=readPeaksOp.getResult();
      if( res==null ){
        return new ErrorString("Could not read in peaks from "+peaksfile);
      }else if( res instanceof ErrorString){
        return res;
      }else if( res instanceof Vector){
        peaks=(Vector)res;
        if(peaks.size()<=0)
          return new ErrorString("Could not read in peaks from "+peaksfile);
      }else{
        return new ErrorString("Something went wrong: "+res);
      }
      res=null;
      readPeaksOp=null;
    }

    // read in the orientation matrix
    float[][] UB=null;
    {
      Object res=readOrient(matrixfile);
      if(res instanceof ErrorString)
        return res;
      else
        UB=(float[][])res;
      res=null;
    }

    // add the orientation matrix to all of the peaks with valid run numbers
    Peak peak=null;
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if(indexpeak(peak,runs,seqs))
        peak.UB(UB);
    }

    // create a StringBuffer for the log
    StringBuffer log=new StringBuffer((1+peaks.size())*122);

    // unindex peaks outside of the given deltas
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if( ! indexpeak(peak,runs,seqs) ) continue;

      float hMod=Math.abs(peak.h()-Math.round(peak.h()));
      float kMod=Math.abs(peak.k()-Math.round(peak.k()));
      float lMod=Math.abs(peak.l()-Math.round(peak.l()));

      log.append(formatHKL(peak.h(),peak.k(),peak.l()));

      if( (delta_h<=hMod) || (delta_k<=kMod) || (delta_l<=lMod) ){
        peak.sethkl(0f,0f,0f,false);
        peak.reflag(0);
      }else{
        peak.reflag(crystallite);
      }
      log.append(peak.toString().substring(2)+"\n");
    }

    // write out the log file
    OutputStreamWriter out=null;
    try{
      if(DEBUG){
        out=new OutputStreamWriter(System.out);
      }else{
        logfile=FilenameUtil.setForwardSlash(peaksfile);
        index=logfile.lastIndexOf("/");
        if(index>=0){
          logfile=logfile.substring(0,index+1)+"index.log";
        }else{
          logfile="index.log";
        }
        FileOutputStream fos=new FileOutputStream(logfile,false);
        out=new OutputStreamWriter(fos);
      }
      out.write(" calc h  calc k  calc l   seq   h   k   l      ");
      out.write("x      y      z    xcm    ycm      wl  ");
      out.write("Iobs     intI    DintI flag   run det\n");
      out.write(log.toString());
      out.flush();
      out.close();
    }catch(IOException e){
      // let it drop on the floor
    }
    //System.out.println(log.toString());

    // print the results to peaksfile
    if(update){
      WritePeaks writePeaksOp=new WritePeaks(peaksfile,peaks,Boolean.FALSE);
      Object res=writePeaksOp.getResult();
      if(res instanceof ErrorString)
        return res;
    }


    // return the log file name and print the number of indexed peaks
    SharedData.addmsg(this.getNumIndexed(peaks));
    return logfile;
  }  
  
  /**
   * Determine whether or not to update the peak by checking it
   * against the list of allowed run numbers and sequence numbers
   */
  private static boolean indexpeak(Peak peak, int[] runs, int[] seqs){
    boolean good_run=(runs==null);
    boolean good_seq=(seqs==null);

    if(! good_run){
      for( int i=0 ; i<runs.length ; i++ ){
        if( runs[i]==peak.nrun() ){
          good_run=true;
          break;
        }
      }
    }

    if(! good_seq){
      for( int i=0 ; i<seqs.length ; i++ ){
        if( seqs[i]==peak.seqnum() ){
          good_seq=true;
          break;
        }
      }
    }

    return ( good_run && good_seq );
  }

  /**
   * Get a copy of the current SpectrometerEvaluator Operator.  The
   * list of parameters is also copied.
   */
  public Object clone(){
    Index new_op = new Index( );
    
    new_op.CopyParametersFrom( this );
    
    return new_op;
  }
  
  /**
   * This method goes through a vector of peaks and counts the number
   * of peaks that have indices.
   */
  private static String getNumIndexed(Vector peaks){
    Peak peak=null;
    int numIndexed=0;

    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if( peak.h()!=0 || peak.k()!=0 || peak.l()!=0 )
        numIndexed++;
    }

    return "Indexed "+numIndexed+" of "+peaks.size()+" peaks";
  }

  /**
   * Read the orientation matrix out of the specified file.
   */
  private static Object readOrient(String matrixfile){
    TextFileReader tfr=null;
    ErrorString eString=null;
    float[][] orient=new float[3][3];

    boolean isexpfile=matrixfile.endsWith(".x");

    try{
      tfr=new TextFileReader(matrixfile);
      Object res=LoadOrientation.readOrient(tfr,isexpfile);
      if(res instanceof ErrorString)
        eString=(ErrorString)res;
      else
        orient=(float[][])res;
    }catch(IOException e){
      eString=new ErrorString("Error while reading matrix: "+e.getMessage());
    }catch(NumberFormatException e){
      eString=new ErrorString("Error while reading matrix: "+e.getMessage());
    }finally{
      if(tfr!=null){
        try{
          tfr.close();
        }catch( IOException e ){
          // let it drop on the floor
        }
      }
    }

    if(eString!=null)
      return eString;
    else
      return orient;
  }

  /**
   * Format text for a line of the log file
   */
  private static String formatHKL(float h, float k, float l){
    DecimalFormat format=new DecimalFormat("###0.00");
    StringBuffer res=new StringBuffer(8*3);
    
    StringBuffer temp=new StringBuffer(8);

    temp.append(format.format(h));
    while(temp.length()<7)
      temp.insert(0," ");
    res.append(temp.toString()+" ");
    temp.delete(0,temp.length());

    temp.append(format.format(k));
    while(temp.length()<7)
      temp.insert(0," ");
    res.append(temp.toString()+" ");
    temp.delete(0,temp.length());

    temp.append(format.format(l));
    while(temp.length()<7)
      temp.insert(0," ");
    res.append(temp.toString()+" ");

    return res.toString();
  }

  /* --------------------------- MAIN METHOD --------------------------- */
  public static void main(String[] args){
    String dir="/IPNShome/pfpeterson/data/SCD/";

    IndexJ op;
    
    op=new IndexJ(dir+"int_quartz.peaks",dir+"quartz.x",0.05f,false);
    //op=new IndexJ(dir+"int_quartz.peaks",dir+"int_quartz.mat",0.05f);
    System.out.println("RESULT: "+op.getResult());

    System.exit(0);
  }
}
