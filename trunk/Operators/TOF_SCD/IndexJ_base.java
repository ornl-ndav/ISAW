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
 * Revision 1.10  2006/06/16 18:16:01  rmikk
 * Added an output Vector argument to the parameter list .  This vector has two
 * Integer elements.  The first is the number indexed and the last is the number
 * attempted to be indexed
 *
 * Revision 1.9  2006/06/06 19:33:32  rmikk
 * Added documentation on the data written to a log file here.
 *
 * Revision 1.8  2006/02/25 23:01:06  rmikk
 * Fixed code that sets unindexed peaks with a null UB.
 * Fixed getNumIndexed peaks to only consider peaks in the given sequence
 *   of runs and sequence numbers.
 *
 * Revision 1.7  2006/01/17 22:41:24  rmikk
 * Set the UB matrix to null and hkl values to zero for peaks that are not
 *   indexed because they are not in selected runs or sequences
 *
 * Revision 1.6  2005/08/05 20:14:41  rmikk
 * Changed the ParameterGUI for one the UB matrix parameter
 * Improved Documentation on the return value
 * Changed displaying to status pane to writing to the system log file if one has
 *     been set up
 *
 * Revision 1.5  2005/06/20 00:43:26  rmikk
 * Used RealArrayPG instead of ArrayPG to get better type checking
 *
 * Revision 1.4  2005/01/04 17:00:59  rmikk
 * Eliminated unused imports
 * Made a hidden operator because result is not a string
 *
 * Revision 1.3  2004/07/31 23:07:25  rmikk
 * Removed unused imports.
 *
 * Revision 1.2  2004/07/29 14:01:20  rmikk
 * Fixed javadoc error
 *
 * Revision 1.1  2004/07/14 16:26:08  rmikk
 * Initial Checkin.
 * IndexJ with peak Vector input and  Orientation matrix output
 *
  */

package Operators.TOF_SCD;

import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

//import java.io.File;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Vector;

import DataSetTools.operator.DataSet.Attribute.LoadOrientation;
import DataSetTools.operator.Generic.TOF_SCD.GenericTOF_SCD;
import DataSetTools.operator.Generic.TOF_SCD.Peak;
import DataSetTools.parameter.*;
import DataSetTools.util.SharedData;
import gov.anl.ipns.Util.Sys.*;
/**
 * This operator is intended to run A.J. Schultz's "index"
 * program. This is not heavily tested but works fairly well.
 */
public class IndexJ_base extends    GenericTOF_SCD implements 
                              DataSetTools.operator.HiddenOperator{
  public static String command=null;
  private static final boolean DEBUG=false;
  public String log = "";

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct an operator with a default parameter list.
   */
  public IndexJ_base( ){
    super( "JIndex base" );
  }
  
  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct operator to execute index.
   *
   *  @param peaks The Vector of peaks Objects to index. 
   *  @param UB   Orientation matrix 
   * @param RestrRuns   The int list of restricted runs
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
  
  
  /**
   *  Construct operator to execute index.
   *
   *  @param peaks The Vector of peaks Objects to index. 
   *  @param UB   Orientation matrix 
   * @param RestrRuns   The int list of restricted runs
   *  @param delta The error parameter for indexing peaks (h = k = l)
   *  @param  Stats  output only. Returns number indexed(first element) and
   *                  number that were tried(second element)
   */
  public IndexJ_base( Vector peaks, float[][] UB, String RestrRuns,float delta, Vector Stats){
	  this( peaks,UB,RestrRuns,delta);
	  getParameter(6).setValue( Stats );
  }
  
  /* ------------------------- setDefaultParmeters ----------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters = new Vector();  // must do this to create empty list of 
                                // parameters
    
    addParameter( new PlaceHolderPG("Peaks Vector", new Vector()));
    addParameter(new ArrayPG("Orientation matrix", null ));
    //2
    addParameter(new IntArrayPG("Restrict Runs",null));
    //3
    addParameter(new FloatPG("Delta h",0.20f));
    //4
    addParameter(new FloatPG("Delta k",0.20f));
    //5
    addParameter(new FloatPG("Delta l",0.20f));
    //6
    addParameter( new ArrayPG("n indexed,n used", null));// output values for this operator
   
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
    sb .append( "@param Stats  Vector for output values. The first element ");
    sb.append("is the number indexed and the 2nd is the number attempted\n");
    sb.append("NOTE: to get these values you MUST supply an Array variable");
    // return
    sb.append("@return The log information. The peaks of parameter 1");
    sb.append("  are also indexed and the log information is also written to");
    sb.append( "a log file");
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
    //String      logfile     = null;  // the index.log file 
    float       delta_h     = 0f;    // error in index (h) allowed
    float       delta_k     = 0f;    // error in index (k) allowed
    float       delta_l     = 0f;    // error in index (l) allowed
     int         crystallite = 1;     // placeholder for future feature
    int[]       runs        = null;  // run numbers to index
    int[]       seqs        = null;  // sequence numbers to index
    java.util.Vector   Stats ;
    
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

    
    Stats =(Vector)(getParameter(6).getValue());
    if( Stats.size()<1)
    	Stats.addElement( new Integer(0));
    else
    	Stats.setElementAt( new Integer(0),0);
    if( Stats.size() < 2)
    	Stats.addElement( new Integer(0));
    else
    	Stats.setElementAt( new Integer(0), 1);
  
    // have someone else read in our peaks file
    
    // read in the orientation matrix
    float[][] UB= matrix;
       // add the orientation matrix to all of the peaks with valid run numbers
    Peak peak=null;
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if(indexpeak(peak,runs,seqs))
        peak.UB(UB);
      /*else{
      
        peak.UB(null);
        peak.sethkl(0f,0f,0f,false);
        peak.reflag(0);
      }*/
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

    SharedMessages.LOGaddmsg( IndexJ_base.getNumIndexed( peaks,runs,seqs, Stats )+"\n");
    ShowLogInfo( log );
    
    // return the log file name and print the number of indexed peaks
    SharedData.addmsg( IndexJ_base.getNumIndexed( peaks,runs,seqs, Stats ));
    return IndexJ_base.getNumIndexed( peaks,runs,seqs, Stats)+"\n"+log.toString();
   }catch(Exception xx){
     xx.printStackTrace();
     return new ErrorString(xx);
   }  
  }  
  
  private void ShowLogInfo( StringBuffer log){
    this.log = log.toString();
    SharedMessages.LOGaddmsg("---------- IndexJ log------------\n");
    SharedMessages.LOGaddmsg(this.log+"\n");
    SharedMessages.LOGaddmsg("------------ End IndexJ log\n");
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
  private static String getNumIndexed(Vector peaks,int[]runs, int[]seqs, Vector Stats){
    Peak peak=null;
    int numIndexed=0;
    int numNotIndexed=0;
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if(indexpeak(peak,runs,seqs))
         if( peak.h()!=0 || peak.k()!=0 || peak.l()!=0 )
            numIndexed++;
         else
        	numNotIndexed++;
    }
    Stats.setElementAt( new Integer( numIndexed),0);
    Stats.setElementAt( new Integer( numIndexed + numNotIndexed) , 1);
    return "Indexed "+numIndexed+" of "+(numIndexed+numNotIndexed)+" peaks";
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
    //String dir="/IPNShome/pfpeterson/data/SCD/";

    IndexJ_base op = null;
    
    //op=new IndexJ_base(dir+"int_quartz.peaks",dir+"quartz.x",0.05f,false);
    //op=new IndexJ(dir+"int_quartz.peaks",dir+"int_quartz.mat",0.05f);
    System.out.println("RESULT: "+op.getResult());

    System.exit(0);
  }
}
