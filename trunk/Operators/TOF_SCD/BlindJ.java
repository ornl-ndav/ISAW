/*
 * File:  BlindJ.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson<Mikkelsonr@UWstout.edu>
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2003/02/24 15:58:16  pfpeterson
 * Renaming from Blindd
 *
 * Revision 1.6  2003/02/21 16:57:52  pfpeterson
 * Now creates instance of blind and works with instance variables
 * and methods.
 *
 * Revision 1.5  2003/02/20 21:41:23  pfpeterson
 * Simplified code by extracting some functionality out into private
 * methods and using the ReadPeaks operator to load in the peaks
 * information.
 *
 * Revision 1.4  2003/02/14 21:19:26  dennis
 * Changed javadocs on getCommand() to the new command name.
 *
 * Revision 1.3  2003/02/14 21:14:52  pfpeterson
 * Set indent level to two and split lines longer than 80 characters.
 *
 * Revision 1.2  2003/02/10 15:36:35  pfpeterson
 * Added FileFilter to two of the parameters and modified the full
 * constructor to be slightly cleaner.
 *
 * Revision 1.1  2003/02/10 13:32:45  rmikk
 * Initial Checkin for the Java Blind
 */
package Operators.TOF_SCD;

import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Information.XAxis.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import java.util.*;
import DataSetTools.parameter.*;
import java.text.*;
import java.io.*;
import IPNSSrc.*;

/** 
 * This operator takes a peaks file and a list of sequence numbers in
 * this file.  It then calculates the orientation matrix and other
 * parameters and stores them in a matrix file
 */
public class BlindJ extends  GenericTOF_SCD {
  private static final String  TITLE = "JBlind";
  private static final boolean DEBUG = false;

  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Operator Template" and a default
   *  list of parameters.
   */  
  public BlindJ()
  {
    super( TITLE );
  }
    
  /* ---------------------------- Constructor ----------------------------- */ 
  /** 
   *  
   */
  public BlindJ( String PeaksFilename, String SeqNums, String MatFilename)
  {
    this();
    getParameter(0).setValue(PeaksFilename);
    getParameter(1).setValue(SeqNums);
    getParameter(2).setValue(MatFilename);
  }
    
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Returns JBlind, the name of this operator to use in scripts
   * 
   * @return  "JBlind", the command used to invoke this operator in Scripts
   */
  public String getCommand(){
    return "JBlind";
  }
    

  /*----------------------- getDocumentation -----------------------------*/
  public String getDocumentation(){
    StringBuffer S= new StringBuffer( 1200);
    S.append("@overview This operator take a peaks file(produced by ");
    S.append("FindPeaks) and a list of sequence numbers from this file to ");
    S.append("create a file with the orientation matrix for the crystal ");
    S.append("along with several other parameters. A blind.log file is also ");
    S.append("produced");
    S.append("@algorithm The peaks are sent through the Blind program. This ");
    S.append("is a program used at the IPNS division at Argonne National ");
    S.append("Laboratory for this task.");
    S.append("@assumptions The peak file must be of the standard format for ");
    S.append("peak files supported by the IPNS division at Argonne National ");
    S.append("Laboratory");
    S.append("@param   PeaksFilename- The name of the file with peak ");
    S.append("information");
    S.append("@param  SeqNums- The list of sequence numbers to use. ");
    S.append("Eg 33:36,47,56");
    S.append("@param  MatFilename- The filename to store the orientation ");
    S.append("matrix and the other cell parameters ");
    S.append("@return  Success or one of the errormessages below ");
    S.append("@error Improper Peak filename ");
    S.append("@error Improper sequence numbers");
    S.append("@error Improper save matrix filename");
    S.append("@error No sequence numbers selected");
    S.append("@error  No peaks");
    S.append("@error  Several I/O errors");
    S.append("@error   ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING \n");
    S.append("  All the peaks were in one plane or on one line");
    S.append("@error INITIAL NON-INTEGER INDICES \n");
    S.append(" Cannot get basis where all peaks have integer coefficients");
     
    return S.toString();

  }
    
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the
   * data types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();

    LoadFilePG peaksfilepg=new LoadFilePG("Peak filename","" );
    peaksfilepg.setFilter(new PeaksFilter());
    addParameter(peaksfilepg);

    int[] intAr= new int[5];
    intAr[0]=30;intAr[1]=31;intAr[2]=32; intAr[3]=40;intAr[4]=42; 
    addParameter( new IntArrayPG("Seq nums",intAr ) );

    SaveFilePG matfilepg=new SaveFilePG("Matrix filename","" );
    matfilepg.setFilter(new MatrixFilter());
    addParameter( matfilepg );
   
    
  }
    
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   *  Gets the desired peaks from the input peak file, runs the
   *  blind method, then stores the resultant orientation matrix and
   *  other parameters in the given matrix file.  A blind.log file
   *  is also created.
   *
   *  @return If successful, this operator returns the word 'Success'.
   *  @see IPNSSrc.blind
   */
  public Object getResult(){
    // get the value of the parameters
    String peakfile=((LoadFilePG)getParameter(0)).getStringValue();
    int[] seq= IntList.ToArray(((IntArrayPG)getParameter(1)).getStringValue());
    String matrixfile=((SaveFilePG)getParameter(2)).getStringValue();
     
    // check that there is valid value for all of the parameters
    if( peakfile==null || peakfile.length()<=0 )
      return new ErrorString("Improper Peak filename: "+peakfile);
    if( seq==null)
      return new ErrorString("Improper sequence numbers");
    if( matrixfile==null || matrixfile.length()<=0 )
      return new ErrorString("Improper save matrix filename: "+matrixfile);
    if( seq.length<=0)
      return new ErrorString(" No sequence numbers selected");
     
    // put information into the 'peaks' vector
    Vector peaks=null;
    {
      Object res=getPeaks(peakfile,seq);
      if(res instanceof ErrorString)
        return res;
      else if(res instanceof Vector)
        peaks=(Vector)res;
      else
        return new ErrorString("Something went wrong");
    }

    // final setup for calculation
    if( peaks==null || peaks.size()<=0) 
      return new ErrorString("No peaks");
    double[] xx = new double[peaks.size()+3];
    double[] yy = new double[peaks.size()+3];
    double[] zz = new double[peaks.size()+3];
    intW LMT = new intW(0);
     
    // perform the calculation
    blind BLIND=new blind();
    BLIND.blaue( peaks,xx,yy,zz,LMT,seq);
    double[] b= new double[9];
    doubleW dd= new doubleW(.08);
    intW mj= new intW(0);
    BLIND.bias(peaks.size()+3,xx,yy,zz,b,0,3,dd,4.0,mj,seq,123,0);

    // write the log file
    int index=matrixfile.lastIndexOf("/");
    if(index>=0){
      String logfile=matrixfile.substring(0,index+1)+"blind.log";
      if(!BLIND.writeLog(logfile))
        SharedData.addmsg("WARNING: Failed to create logfile");
    }else{
      SharedData.addmsg("WARNING: Could not create logfile, bad filename");
    }
        

    // write the matrix file
    return writeMatFile(matrixfile,BLIND);
  }
    
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){ 
    GenericTOF_SCD op = new BlindJ();
    op.CopyParametersFrom( this );
    return op;
  }
    
  /* -------------------------- private methods --------------------------- */ 
  /**
   * Pad a string with spaces on the left
   */
  static private String format(String rs, int length){
    while(rs.length()<length){
      rs=" "+rs;
    }
    return rs;
  }

  /**
   * Read in the peaks from the specified file. This returns the
   * selected peaks (in proper format) to a vector that is returned to
   * the caller.
   */
  private static Object getPeaks(String filename,int[] seq){
    if(filename==null || filename.length()<=0)
      return new ErrorString("Improper peak filename: "+filename);
    if(seq==null || seq.length<=0)
      return new ErrorString("No sequence numbers selected");

    Vector peaks=new Vector();
    Vector rawpeaks=null;
    Operator readpeaks=new ReadPeaks(filename);
    Object res=readpeaks.getResult();
    if(res instanceof ErrorString)
      return res;
    else if(res instanceof Vector)
      rawpeaks=(Vector)res;
    else
      return new ErrorString("Something went wrong reading peaks file: "
                             +filename);
    Peak peak=null;
    int seqnum_num=0;
    for( int i=0 ; i<rawpeaks.size()&&seqnum_num<seq.length ; i++ ){
      peak=(Peak)rawpeaks.elementAt(i);
      if(peak.seqnum()==seq[seqnum_num]){
        float[] dat=new float[9];
        dat[5]=peak.xcm();
        dat[6]=peak.ycm();
        dat[7]=peak.wl();
        dat[0]=peak.chi();
        dat[1]=peak.phi();
        dat[2]=peak.omega();
        dat[3]=peak.detA();
        dat[4]=peak.detD();
        peaks.add(dat);
        seqnum_num++;
      }
    }

    return peaks;
  }

  /**
   * Write out the orientation matrix and lattice parameters to the
   * matrix file.
   */
  private Object writeMatFile(String filename, blind BLIND){
    // create matrix file contents
    DecimalFormat df = new DecimalFormat("##0.000000;#0.000000");
    StringBuffer sb= new StringBuffer(10*3+1);
    for( int i=0;i<3;i++){
      for (int j=0;j<3;j++)
        sb.append(format(df.format( BLIND.u[3*j+i]),10));
      sb.append("\n");
    }
    df = new DecimalFormat("#####0.000;####0.000");
    sb.append(format(df.format( BLIND.D1),10));
    sb.append(format(df.format( BLIND.D2),10));
    sb.append(format(df.format( BLIND.D3),10));
    sb.append(format(df.format( BLIND.D4),10));
    sb.append(format(df.format( BLIND.D5),10));
    sb.append(format(df.format( BLIND.D6),10));
    sb.append(format(df.format( BLIND.cellVol),10));
    sb.append("\n");
    for( int i=0; i < 7; i++)
      sb.append(format(df.format(0.0),10));
    sb.append("\n");

    //Write results to the matrix file
    FileOutputStream fout = null;
    try{
      fout = new FileOutputStream( filename );
      fout.write( sb.toString().getBytes());
      fout.flush();
    }catch( IOException e){
      return new ErrorString("Writing Matrix File: "+e.getMessage());
    }finally{
      if(fout!=null){
        try{
          fout.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }

    return "Wrote file: "+filename;
  }

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Standalong program to carry out the operations
   * @param   args[0] The name of the file with peak information
   * @param   args[1] The list of sequence numbers to use. Eg 33:36,47,56
   * @param   args[2] The name of the file to write the orientation matrix
   *
   */
  public static void main( String args[] )
  {
    if( args != null)
      if( args.length >2)
        {
          BlindJ bl= new BlindJ( args[0], args[1], args[2]);
          System.out.println("Result="+ bl.getResult() );
          System.exit( 0 );
        }
    System.out.println(" This program requires three arguments");
    System.out.println("   Arg 1:The name of the file with peak information");
    System.out.println("   Arg 2:The list of sequence numbers to use. "
                       +"Eg 33:36,47,56");
    System.out.println("   Arg 3:The name of the file to write the "
                       +"orientation matrix");
    System.exit( 0);

  }
}
