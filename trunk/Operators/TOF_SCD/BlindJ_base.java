/*
 * File:  BlindJ_base.java 
 *
 * Copyright (C) 2004, Ruth Mikkelson, Peter Peterson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.2  2005/01/06 16:43:09  rmikk
 * Made a Hidden operator
 *
 * Revision 1.1  2004/07/14 16:21:40  rmikk
 * BlindJ except input is the Peaks Vector and output is the orientation 
 *   matrix
 *
 * 
 */
package Operators.TOF_SCD;

import DataSetTools.operator.Operator;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.util.SharedData;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.util.Vector;
import DataSetTools.parameter.*;
import IPNSSrc.blind;
//import DataSetTools.operator.Generic.TOF_SCD.*;
/** 
 * This operator takes a peaks file and a list of sequence numbers in
 * this file.  It then calculates the orientation matrix and other
 * parameters and stores them in a matrix file
 */
public class BlindJ_base extends  GenericTOF_SCD implements
                             DataSetTools.operator.HiddenOperator{
                               
  private static final String  TITLE = "JBlind";
  float[][] UB=new float[3][3];
  /* ------------------------ Default constructor ------------------------- */ 
 
  public BlindJ_base()
  {
    super( TITLE );
  }
    
  /* ---------------------------- Constructor ----------------------------- */ 
  /** 
   *  
   */
  public BlindJ_base( Vector Peaks, String SeqNums, String MatFilename)
  {
    this();
    getParameter(0).setValue( Peaks);
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
    S.append("@overview This operator take a peaks Vector (produced by ");
    S.append("FindPeaks) and a list of sequence numbers from this file to ");
    S.append("create a file with the orientation matrix for the crystal ");
    S.append("along with several other parameters. A blind.log file is also ");
    S.append("produced");
    S.append("@algorithm The peaks are sent through the Blind program. This ");
    S.append("is a program used at the IPNS division at Argonne National ");
    S.append("Laboratory for this task.");
    S.append("@param   Peaks  A Vector of peaks ");
    S.append("@param  SeqNums- The list of sequence numbers to use. ");
    S.append("Eg 33:36,47,56");
    S.append("@param  MatFilename- The filename to store the orientation ");
    S.append("matrix and the other cell parameters ");
    S.append("@return  The UB matrix. It is not added to each of the Peaks ");
    S.append("@error No PEAKS ");
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

    addParameter( new PlaceHolderPG("Peaks", new Vector()));
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
   *  @return If successful, this operator returns the orientation matrix
   *           is returned otherwise an error message will be returned. Also,
   *           a log file, blind.log, and a orientation matrix file will be
   *           produced.
   *  @see IPNSSrc.blind
   */
  public Object getResult(){
    // get the value of the parameters
    try{
    Vector peaks = (Vector)(getParameter(0).getValue());
   
    
    int[] seq= IntList.ToArray(((IntArrayPG)getParameter(1)).getStringValue());
    String matrixfile=((SaveFilePG)getParameter(2)).getStringValue();
     
    // check that there is valid value for all of the parameters
   
    if( seq==null)
      return new ErrorString("Improper sequence numbers");
    if( matrixfile==null || matrixfile.length()<=0 )
      matrixfile = null;
      //return new ErrorString("Improper save matrix filename: "+matrixfile);
    if( seq.length<=0)
      return new ErrorString(" No sequence numbers selected");
     
    // put information into the 'peaks' vector
    
    Vector newPeaks = new Vector();
    int seqNumindx = 0;
    for(int i=0; (i < peaks.size()) && (seqNumindx <seq.length);i++){
       Peak p =(Peak) peaks.elementAt(i);
       if(p.seqnum() == seq[seqNumindx]){
         newPeaks.addElement( p);
         seqNumindx ++;
       }
    }
   System.out.println("newPeaks size="+newPeaks.size());
    // final setup for calculation
    if( newPeaks==null || newPeaks.size()<=0) 
      return new ErrorString("No peaks");
    double[] xx = new double[newPeaks.size()+3];
    double[] yy = new double[newPeaks.size()+3];
    double[] zz = new double[newPeaks.size()+3];
     
    // perform the calculation
    blind BLIND=new blind();
    ErrorString error=BLIND.blaue( newPeaks,xx,yy,zz,seq);
    if(error!=null) return error;
    error=BLIND.bias(newPeaks.size()+3,xx,yy,zz,seq);
    if(error!=null) return error;

    // write the log file
    int index= -1;
    if(matrixfile != null)
       index = matrixfile.lastIndexOf("/");
    String dir="";

    if(index>=0)
      dir=matrixfile.substring(0,index+1);

    String logfile = null;
    if( matrixfile != null) 
       logfile = dir + "blind.log";
    if((logfile != null) && !BLIND.writeLog(logfile))
        SharedData.addmsg("WARNING: Failed to create logfile");

    // write the matrix file
    writeMatFile(matrixfile,BLIND);
   
    //return the log file
    return UB;
    }catch(Exception xx){
      xx.printStackTrace();
      return new ErrorString(xx.toString());
    }
  }
    
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){ 
    BlindJ_base op = new BlindJ_base();
    op.CopyParametersFrom( this );
    return op;
  }
    
  /* -------------------------- private methods --------------------------- */ 

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

  /*
   *  Writes out the UB results in standard form with crystal lengths
   *  and angles
   *  @param filename  the file to write this information to
   *  @param UB        the matrix of bases for Q vectors.
   *  @return     true if successful otherwise false
   */
  public boolean writeMatFile( String filename, float[][] UB){
       if( filename == null)
          return false;
       float[] abc=new float[7];
       double[][] UB1= new double[3][3];
       for( int i=0;i<3;i++)
         for( int j=0; j< 3; j++)
            UB1[i][j]= (double)UB[i][j];
       {
         double[] myABC=Util.abc(UB1);
         for( int i=0 ; i<7 ; i++ )
           abc[i]=(float)myABC[i];
       }

       // create a sigma matrix
       float[] sig={0f,0f,0f,0f,0f,0f,0f};

       // write out the file
       ErrorString error=Util.writeMatrix(filename,UB,abc,sig);
       if(error!=null)
         return false;
       else
       {
         SharedData.addmsg("Wrote file: " + filename);
         return true;
       }
   
  }
  /**
   * Write out the orientation matrix and lattice parameters to the
   * matrix file.
   */
  private Object writeMatFile(String filename, blind BLIND){
    // create a UB matrix
   
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
   * Main test program to execute BlindJ_base using a file of peaks, a list
   * of sequence numbers and the name of the the file where the orientation
   * matrix should be written.
   *
   * @param   args    The array of arguments containing the peak file name,
   *                  list of sequence numbers (Eg 33:36,47,5) and the matrix 
   *                  file name. 
   */
  public static void main( String args[] )
  {
    if( args != null)
      if( args.length >2)
        { 
          //BlindJ_base bl= new BlindJ_base( args[0], args[1], args[2]);
          //System.out.println("Result="+ bl.getResult() );
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
