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
   *  @param delta The error parameter for indexing peaks
   */
  
  public IndexJ( String peaksfile, String matrixfile, float delta,
                 boolean update){
    this();
    
    getParameter(0).setValue(peaksfile);
    getParameter(1).setValue(matrixfile);
    getParameter(2).setValue(new Float(delta));
    getParameter(3).setValue(new Boolean(update));
  }
  
  /* ------------------------- setDefaultParmeters ----------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters = new Vector();  // must do this to create empty list of 
                                // parameters
    
    parameters=new Vector();
    LoadFilePG peaksfilepg=new LoadFilePG("Peaks file",null);
    peaksfilepg.setFilter(new PeaksFilter());
    addParameter(peaksfilepg);
    LoadFilePG matfilepg=new LoadFilePG("Matrix file",null);
    matfilepg.setFilter(new MatrixFilter());
    addParameter(matfilepg);
    addParameter(new FloatPG("Delta",0.05f));
    addParameter(new Parameter("update peaks file",Boolean.TRUE));
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
    ErrorString eString    = null;
    String      peaksfile  = null;  // peaks filename
    String      matrixfile = null;  // matrix filename
    String      dir        = null;  // directory that the files are all in
    String      expname    = null;  // the experiment name
    File        file       = null;  // for tests
    int         index      = 0;     // for chopping up strings
    float       delta      = 0f;    // error in index allowed
    boolean     update     = false; // update the peaks file

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

    // get the delta parameter
    IParameter iparm=getParameter(2);
    if(iparm instanceof FloatPG){
      delta=((FloatPG)iparm).getfloatValue();
    }else{
      Object value=iparm.getValue();
      if(value instanceof Float ){
        delta=((Float)value).floatValue();
      }else{
        return new ErrorString("invalid value "+value);
      }
    }

    // find out if want to update peaks file
    iparm=getParameter(3);
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

    // add the orientation matrix to all of the peaks
    Peak peak=null;
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      peak.UB(UB);
    }

    // create a StringBuffer for the log
    StringBuffer log=new StringBuffer((1+peaks.size())*122);

    // unindex peaks outside of the given deltas
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      log.append(formatHKL(peak.h(),peak.k(),peak.l()));
      if( (delta<=Math.abs(peak.h()%1)) || (delta<=Math.abs(peak.h()%1))
          || (delta<=Math.abs(peak.h()%1)) ){
        peak.sethkl(0f,0f,0f,false);
      }else{
        peak.reflag(1);
      }
      log.append(peak.toString().substring(2)+"\n");
    }

    // write out the log file
    OutputStreamWriter out=null;
    try{
      if(DEBUG){
        out=new OutputStreamWriter(System.out);
      }else{
        String logfile=FilenameUtil.setForwardSlash(peaksfile);
        index=logfile.lastIndexOf("/");
        if(index>=0){
          logfile=logfile.substring(0,index+1)+"index.log";
        }else{
          logfile="index.log";
        }
        System.out.println(logfile);
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


    // return the number of indexed peaks
    return this.getNumIndexed(peaks);
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
