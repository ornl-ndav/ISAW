/*
 * File:  IndexJ_base.java   
 *
 * Copyright (C) 2004, Ruth Mikkelson,Peter F. Peterson
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
 
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2004/07/14 16:26:08  rmikk
 * Initial Checkin.
 * IndexJ with peak Vector input and  Orientation matrix output
 *
  */

package Operators.TOF_SCD;

import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Vector;

import DataSetTools.operator.DataSet.Attribute.LoadOrientation;
import DataSetTools.operator.Generic.TOF_SCD.GenericTOF_SCD;
import DataSetTools.operator.Generic.TOF_SCD.MatrixFilter;
import DataSetTools.operator.Generic.TOF_SCD.Peak;
import DataSetTools.operator.Generic.TOF_SCD.ReadPeaks;
import DataSetTools.operator.Generic.TOF_SCD.WritePeaks;
import DataSetTools.parameter.*;
import DataSetTools.util.FilenameUtil;
import DataSetTools.util.SharedData;

/**
 * This operator is intended to run A.J. Schultz's "index"
 * program. This is not heavily tested but works fairly well.
 */
public class IndexJ_base extends    GenericTOF_SCD {
  public static String command=null;
  private static final boolean DEBUG=false;
  public String log = "";

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct an operator with a default parameter list.
   */
  public IndexJ_base( ){
    super( "JIndex" );
  }
  
  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct operator to execute index.
   *
   *  @param peaksfile The name of the peaks file to index. This name is
   *  used to generate the experiment name used by index.
   *  @param Orientation matrix 
   *  @param delta The error parameter for indexing peaks (h = k = l)
   */
  
  public IndexJ_base( Vector peaks, float[][] UB, String RestrRuns,float delta){
    this();
    
    getParameter(0).setValue(peaks);
    getParameter(1).setValue(UB);
    getParameter(2).setValue(RestrRuns);
    getParameter(3).setValue(new Float(delta));
    getParameter(4).setValue(new Float(delta));
    getParameter(5).setValue(new Float(delta));
   
  }
  
  /* ------------------------- setDefaultParmeters ----------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters = new Vector();  // must do this to create empty list of 
                                // parameters
    
    addParameter( new PlaceHolderPG("Peaks Vector", new Vector()));
    addParameter(new ArrayPG("Orientation matrix", null));
    //2
    addParameter(new IntArrayPG("Restrict Runs",null));
    //3
    addParameter(new FloatPG("Delta h",0.20f));
    //4
    addParameter(new FloatPG("Delta k",0.20f));
    //5
    addParameter(new FloatPG("Delta l",0.20f));
    //6
   
     }
  
  /**
   * This returns the help documentation
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer(100);

    // overview
    sb.append("@overview This is a java version of \"INDEX\" maintained by ");
    sb.append("A.J.Schultz. This depends on two other operators being ");
    sb.append("present: ReadPeaks and WritePeaks. Eventually this operator ");
    sb.append("should not call these directly, but have the user do it ");
    sb.append("through a script.");
    // parameters
    sb.append("@param the Vector of Peak[_new] Objects ");
    sb.append("@param Orientation matrix  ");
    sb.append("@param restrict_runs The run numbers to restrict the indexing ");
    sb.append("of peaks to.");
    sb.append("@param delta_h the allowable uncertainty in the calculated ");
    sb.append("h value.");
    sb.append("@param delta_k the allowable uncertainty in the calculated ");
    sb.append("k value.");
    sb.append("@param delta_l the allowable uncertainty in the calculated ");
    sb.append("l value.");
    // return
    sb.append("@return The peaks indexed with the orientation matrix");
    // error
   

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
 
  public Object getResult(){
    Vector      peaks   = null;  // peaks filename
    float[][]   matrix  = null;  // matrix filename
    String      logfile     = null;  // the index.log file 
    float       delta_h     = 0f;    // error in index (h) allowed
    float       delta_k     = 0f;    // error in index (k) allowed
    float       delta_l     = 0f;    // error in index (l) allowed
     int         crystallite = 1;     // placeholder for future feature
    int[]       runs        = null;  // run numbers to index
    int[]       seqs        = null;  // sequence numbers to index
   try{
    // get the peaks file name from the script and test it out
    peaks=(Vector)(getParameter(0).getValue());
   
    // get the matrix filename
    Vector V =(((ArrayPG)getParameter(1)).getVectorValue());
    matrix = new float[3][3];
    if( V.size() != 3)
      return new ErrorString("Incorrect dimension(1) for Orientation mattrix");
    for( int i=0; i<3;i++){
       Object O = V.elementAt(i);
       if( !(O instanceof Vector))
         return new ErrorString("Incorrect Orientation matrix format");
       Vector V1 =(Vector)O;
       if( V1.size() != 3)
          return new ErrorString("Incorrect dimension(2) for Orientation mattrix");
       try{
          for( int j=0; j<3; j++)
            matrix[i][j]= ((Number)(V1.elementAt(j))).floatValue();
       }catch(Exception s){
          return new ErrorString("Improper Orientation matrix ");
       }
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

    

  
    // have someone else read in our peaks file
    
    // read in the orientation matrix
    float[][] UB= matrix;
       // add the orientation matrix to all of the peaks with valid run numbers
    Peak peak=null;
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if(indexpeak(peak,runs,seqs))
        peak.UB(UB);
    }

    // create a StringBuffer for the log
    StringBuffer log=new StringBuffer((1+peaks.size())*122);
    log.append(" calc h  calc k  calc l   seq   h   k   l      ");
    log.append("x      y      z    xcm    ycm      wl  ");
    log.append("Iobs     intI    DintI flag   run det\n");
    // unindex peaks outside of the given deltas
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if( ! indexpeak(peak,runs,seqs) ) continue;

      float hMod=Math.abs(peak.h()-Math.round(peak.h()));
      float kMod=Math.abs(peak.k()-Math.round(peak.k()));
      float lMod=Math.abs(peak.l()-Math.round(peak.l()));

      log.append(formatHKL(peak.h(),peak.k(),peak.l()));

      if( (delta_h<=hMod) || (delta_k<=kMod) || (delta_l<=lMod) ){
        peak.UB(null);
        peak.sethkl(0f,0f,0f,false);
        peak.reflag(0);
      }else{
        peak.reflag(crystallite);
      }
      log.append(peak.toString().substring(2)+"\n");
    }

    ShowLogInfo( log );
    // return the log file name and print the number of indexed peaks
    SharedData.addmsg( IndexJ_base.getNumIndexed( peaks ));
    return log.toString();
   }catch(Exception xx){
     xx.printStackTrace();
     return new ErrorString(xx);
   }  
  }  
  
  private void ShowLogInfo( StringBuffer log){
    this.log = log.toString();
    SharedData.addmsg("---------- IndexJ log------------");
    SharedData.addmsg(this.log);
    SharedData.addmsg("------------ End IndexJ log");
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
    IndexJ_base new_op = new IndexJ_base( );
    
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

    IndexJ_base op = null;
    
    //op=new IndexJ_base(dir+"int_quartz.peaks",dir+"quartz.x",0.05f,false);
    //op=new IndexJ(dir+"int_quartz.peaks",dir+"int_quartz.mat",0.05f);
    System.out.println("RESULT: "+op.getResult());

    System.exit(0);
  }
}
