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
 * Revision 1.12  2003/07/17 21:45:28  bouzekc
 * Removed initial values for peak sequence numbers.
 *
 * Revision 1.11  2003/07/08 22:46:51  bouzekc
 * Now returns the fully qualified name of the blind.log file.
 *
 * Revision 1.10  2003/06/20 16:02:45  bouzekc
 * Changed "Matrix filename" to "Matrix file" for
 * consistency in viewing parameters in the Wizards.
 *
 * Revision 1.9  2003/06/09 22:03:20  bouzekc
 * Changed label on sequence numbers parameter to
 * "Peak Sequence Numbers."
 *
 * Revision 1.8  2003/06/05 22:04:15  bouzekc
 * Now prints a "Wrote filename: " to SharedData and returns
 * the fully qualified filename.
 *
 * Revision 1.7  2003/05/14 20:15:00  pfpeterson
 * Now deals with possible ErrorString being returned by blind.bias.
 *
 * Revision 1.6  2003/05/13 20:17:46  pfpeterson
 * Changed to work with most recent version of IPNSSrc.blind
 *
 * Revision 1.5  2003/05/12 19:25:06  pfpeterson
 * Small changes to work with changes in IPNSsrc.blind
 *
 * Revision 1.4  2003/04/30 19:52:52  pfpeterson
 * Changed to reflect changes in IPNSsrc.blind.
 *
 * Revision 1.3  2003/04/09 16:23:48  pfpeterson
 * Moved the code to write a matrix file from here to
 * DataSetTools.operator.Generic.TOF_SCD.Util. Also cleaned up imports.
 *
 * Revision 1.2  2003/02/24 22:06:07  pfpeterson
 * Changed the means of initializing the IntArrayPG.
 *
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

import DataSetTools.operator.Operator;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.util.ErrorString;
import DataSetTools.util.IntList;
import DataSetTools.util.SharedData;
import java.util.Vector;
import DataSetTools.parameter.*;
import IPNSSrc.blind;

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
    S.append("@return  The log file name or one of the errormessages below ");
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

    LoadFilePG peaksfilepg=new LoadFilePG("Peaks filename","" );
    peaksfilepg.setFilter(new PeaksFilter());
    addParameter(peaksfilepg);

    addParameter( new IntArrayPG("Peak sequence numbers",
                                 "") );

    SaveFilePG matfilepg=new SaveFilePG("Matrix file","" );
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
     
    // perform the calculation
    blind BLIND=new blind();
    ErrorString error=BLIND.blaue( peaks,xx,yy,zz,seq);
    if(error!=null) return error;
    error=BLIND.bias(peaks.size()+3,xx,yy,zz,seq);
    if(error!=null) return error;

    // write the log file
    int index=matrixfile.lastIndexOf("/");
    String dir="";

    if(index>=0)
      dir=matrixfile.substring(0,index+1);

    String logfile = dir + "blind.log";
    if(!BLIND.writeLog(logfile))
        SharedData.addmsg("WARNING: Failed to create logfile");

    // write the matrix file
    writeMatFile(matrixfile,BLIND);

    //return the log file
    return logfile;
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
        peaks.add(peak);
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
    // create a UB matrix
    float[][] UB=new float[3][3];
    for( int i=0 ; i<3 ; i++ )
      for( int j=0 ; j<3 ; j++ )
        UB[i][j]=(float)BLIND.UB[i][j];

    // create a lattice parameters vector
    float[] abc=new float[7];
    {
      double[] myABC=Util.abc(BLIND.UB);
      for( int i=0 ; i<7 ; i++ )
        abc[i]=(float)myABC[i];
    }

    // create a sigma matrix
    float[] sig={0f,0f,0f,0f,0f,0f,0f};

    // write out the file
    ErrorString error=Util.writeMatrix(filename,UB,abc,sig);
    if(error!=null)
      return error;
    else
    {
      SharedData.addmsg("Wrote file: " + filename);
      return filename;
    }
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
