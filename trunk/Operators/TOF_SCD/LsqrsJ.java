/*
 * File:  LsqrsJ.java   
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
 * Revision 1.2  2003/04/23 15:52:33  pfpeterson
 * Fixed calculation of lattice parameters and implemented own method
 * for calculating chisq.
 *
 * Revision 1.1  2003/04/11 15:36:16  pfpeterson
 * Added to CVS.
 *
 */

package Operators.TOF_SCD;

import DataSetTools.math.LinearAlgebra;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.parameter.*;
import DataSetTools.util.ErrorString;
import DataSetTools.util.FilenameUtil;
import DataSetTools.util.Format;
import DataSetTools.util.SharedData;
import DataSetTools.util.TextFileReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Class designed to analyze cell scalars from BLIND to determine Laue
 * symmetry. Originally written by R. Goyette.
 */
public class LsqrsJ extends GenericTOF_SCD{
  private static final boolean DEBUG = false;
  private static final double  SMALL = 1.525878906E-5;

  /**
   * Construct an operator with a default parameter list.
   */
  public LsqrsJ(){
    super("JLsqrs");
  }

  /**
   * Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters=new Vector();

    addParameter(new DataDirPG("Path",null));
    addParameter(new StringPG("Experiment Name",null));
    addParameter(new IntArrayPG("Restrict Histgrams (blank for all)",null));
    addParameter(new IntArrayPG("Restrict Sequence Numbers (blank for all)",null));
    addParameter(new SaveFilePG("Matrix file to write to",null));
  }

  /**
   * This returns the help documentation
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer(80*5);

    // overview
    sb.append("@overview This is a java version of \"LSQRS\" maintained by A.J.Schultz and originally written by J.Marc Overhage in 1979. This version is updated to use different methods for finding determining the best UB matrix.");
    // parameters
    //sb.append("@param int restrictions on the resulting symmetry");
    // return
    //sb.append("@return tells user to look at the console for results");
    // error
    //sb.append("@error anything wrong with the specified directory");

    return sb.toString();
  }

  /**
   * Returns the command name to be used with script processor, in
   * this case JLsqrs.
   */
  public String getCommand(){
    return "JLsqrs";
  }

  /**
   * Uses the current values of the parameters to generate a result.
   */
  public Object getResult(){
    // get the parameters
    String dir=getParameter(0).getValue().toString();
    String expname=getParameter(1).getValue().toString();
    int[] hist_nums=((IntArrayPG)getParameter(2)).getArrayValue();
    int[] seq_nums=((IntArrayPG)getParameter(3)).getArrayValue();
    String matfile=getParameter(4).getValue().toString();
    int[] run_nums=null;

    // local variables
    String expfile=null;
    String peaksfile=null;

    // confirm the parameters
    {
      if(dir==null || dir.length()<=0)
        return new ErrorString("Empty path");
      dir=FilenameUtil.setForwardSlash(dir+"/");

      File file=new File(dir);
      if(! file.isDirectory())
        return new ErrorString("Invalid path: "+dir);

      if(hist_nums!=null){
        if(expname==null || expname.length()<=0)
          return new ErrorString("Empty experiment name");
        expfile=dir+expname+".x";
        file=new File(expfile);
        if(! file.canRead())
          return new ErrorString(expfile+" is not user readable");
      }

      peaksfile=dir+expname+".peaks";
      file=new File(peaksfile);
      if(! file.canRead())
        return new ErrorString(peaksfile+" is not user readable");

      if(matfile==null || matfile.length()<=0){
        matfile=null;
      }else{
        file=new File(matfile);
        if(file.isDirectory())
          matfile=null;
      }
      if(matfile==null)
        SharedData.addmsg("WARN("+getCommand()
                          +"): not writing to matrix file");

      if(hist_nums!=null && hist_nums.length>0)
        run_nums=histToRun(hist_nums,expfile);

      file=null;
    }

    if(DEBUG){
      System.out.println("DIR="+dir);
      System.out.println("EXP="+expname);
      System.out.println("HST="+arrayToString(hist_nums));
      System.out.println("SEQ="+arrayToString(seq_nums));
      System.out.println("EFL="+expfile);
      System.out.println("PFL="+peaksfile);
      System.out.println("RUN="+arrayToString(run_nums));
    }

    // read in the reflections from the peaks file
    Vector peaks=null;
    {
      DataSetTools.operator.Operator readpeaks=new ReadPeaks(peaksfile);
      Object res=readpeaks.getResult();
      if(res instanceof ErrorString)
        return res;
      else if(res instanceof Vector)
        peaks=(Vector)res;
      else
        return new ErrorString("Something went wrong reading peaks file: "
                               +peaksfile);
      readpeaks=null;
      res=null;
    }

    Peak peak=null;
    // trim out the peaks that are not in the list of selected sequence numbers
    if(seq_nums!=null){
      for( int i=peaks.size()-1 ; i>=0 ; i-- ){
        peak=(Peak)peaks.elementAt(i);
        if(binsearch(seq_nums,peak.seqnum())==-1)
          peaks.remove(i);
      }
    }

    // trim out the peaks with out hkl listed
    for( int i=peaks.size()-1 ; i>=0 ; i-- ){
      peak=(Peak)peaks.elementAt(i);
      if(peak.h()==0 && peak.k()==0 && peak.l()==0)
        peaks.remove(i);
    }

    // trim out the ones that are not in the selected histograms
    if(run_nums!=null){
      for( int i=peaks.size()-1 ; i>=0 ; i-- ){
        peak=(Peak)peaks.elementAt(i);
        if(binsearch(run_nums,peak.nrun())==-1)
          peaks.remove(i);
      }
    }

    // create the hkl-matrix and q-matrix (q=1/d)
    double[][] q=new double[peaks.size()][3];
    double[][] hkl=new double[peaks.size()][3];
    for( int i=0 ; i<hkl.length ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      hkl[i][0]=Math.round(peak.h());
      hkl[i][1]=Math.round(peak.k());
      hkl[i][2]=Math.round(peak.l());
      q[i]=peak.getUnrotQ();
    }
/* REMOVE
    System.out.println("HKL="+arrayToString(hkl));
    System.out.println("Q="+arrayToString(q));
*/

    // calculate ub
    double[][] UB=new double[3][3];
    double chisq=0.;
    {
      double[][] Thkl = new double[peaks.size()][3];
      double[][] Tq   = new double[peaks.size()][3];
      for( int i=0 ; i<hkl.length ; i++ ){
        for( int j=0 ; j<3 ; j++ ){
          Thkl[i][j]=hkl[i][j];
          Tq[i][j]=q[i][j];
        }
      }
      chisq=LinearAlgebra.BestFitMatrix(UB,Thkl,Tq);
      chisq=0.; // reset chisq
      Tq=new double[3][peaks.size()];
      for( int i=0 ; i<hkl.length ; i++ )
        for( int j=0 ; j<3 ; j++ )
          Tq[j][i]=q[i][j];
          Thkl=LinearAlgebra.mult(UB,Tq);
      for( int i=0 ; i<peaks.size() ; i++ )
        for( int j=0 ; j<3 ; j++ )
          chisq=chisq+Math.sqrt((hkl[i][j]-Thkl[j][i])*(hkl[i][j]-Thkl[j][i]));
    }
/* REMOVE
    System.out.println("UB="+arrayToString(UB));
    System.out.println("CHISQ="+chisq);
*/

/* REMOVE
    // reset UB for a quick test
    UB=new double[][]{{-0.010955,-0.008296,-0.375428},
                      { 0.202690, 0.201401, 0.000154},
                      { 0.126577,-0.120317, 0.005069}};
*/

    // calculate lattice parameters and cell volume
    double[] abc=abc(UB);
/* REMOVE
    System.out.println("ABC="+arrayToString(abc));
*/

    // determine uncertainties
    double[] sig_abc=new double[7];
    {
      double     numFreedom=3.*(peaks.size()-3.);
      double[]   temp_abc=null;
      double[][] derivatives=new double[3][7];
      double[][] VC=generateVC(peaks);

      for( int i=0 ; i<3 ; i++ ){
        // determine derivatives
        for( int j=0 ; j<3 ; j++ ){
          UB[i][j]=UB[i][j]+SMALL;
          temp_abc=abc(UB);
          UB[i][j]=UB[i][j]-SMALL;
          for( int k=0 ; k<7 ; k++ )
            derivatives[j][k]=(temp_abc[k]-abc[k])/SMALL;
        }

        // accumulate sigmas
        for( int l=0 ; l<7 ; l++ )
          for( int m=0 ; m<3 ; m++ )
            for( int n=0 ; n<3 ; n++ )
              sig_abc[l]+=derivatives[m][l]*VC[m][n]*derivatives[n][l];
      }

      // turn the 'sigmas' into actual sigmas
      //System.out.println("CHI="+chisq+" FREE="+numFreedom); REMOVE
      double delta=chisq/numFreedom;
      for( int i=0 ; i<sig_abc.length ; i++ )
        sig_abc[i]=Math.sqrt(delta*sig_abc[i]);
    }

    // print out the results
    toConsole(UB,abc,sig_abc);
    if(matfile!=null){
      ErrorString error=Util.writeMatrix(matfile,double2float(UB),
                                      double2float(abc),double2float(sig_abc));
      if(error!=null)
        return error;
      else
        return "Wrote file: "+matfile;
    }else{
      return "Success";
    }
  }

  /**
   * Uses the experiment file to convert histogram numbers into run
   * numbers
   */
  private static int[] histToRun(int[] hist_nums,String expfile){
    TextFileReader tfr=null;
    int[] runs=new int[hist_nums.length];
    String[] match=new String[hist_nums.length];

    // generate the tags we are looking for in the file
    for( int i=0 ; i<hist_nums.length ; i++ )
      match[i]="HST"+Format.real(hist_nums[i],3)+"RUN#";

    int array_index=0;
    int match_length=match[0].length();
    try{
      tfr=new TextFileReader(expfile);
      String line=null;
      // go through the whole file or the whole array of histogram numbers
      while(!tfr.eof() && array_index<match.length){
        // read a line
        line=tfr.read_line();
        if(! (line.indexOf(match[array_index])==0) )
          continue; // go to next iteration

        // shorten the line to just the run number
        line=line.substring(match_length,match_length+20).trim();

        // turn the run number into an int
        runs[array_index]=Integer.parseInt(line);

        // increase the index
        array_index++;
      }
    }catch(IOException e){
      runs=null;
    }catch(NumberFormatException e){
      SharedData.addmsg("Encountered NumberFormatException while parsing "
                        +"experiment file - setting run numbers to null");
      runs=null;
    }finally{
      if(tfr!=null){
        try{
          tfr.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }

    return runs;
  }

  /**
   * This search tries to find the value in the provided
   * <U>ORDERED</U> array. If the value does not appear in an index it
   * returns -1.
   */
  private static int binsearch(int[] array, int value){
    // check for impossibles
    if(array==null || array.length<=0) return -1;
    if(value<array[0]) return -1;
    if(value>array[array.length-1]) return -1;

    // set up indices
    int first=0;
    int last=array.length-1;
    int index=(int)((last+first)/2);

    // do the search
    while(first<last){
      if(array[index]==value){
        return index;
      }else if(array[index]<value){
        first=index+1;
      }else if(array[index]>value){
        last=index-1;
      }
      index=(int)((last+first)/2);
    }

    if(array[index]==value) // check where it ended
      return index;
    else                    // or return not found
      return -1;
  }

  /**
   * Print the orientation matrix and lattice parameters to the console.
   */
  private static void toConsole(double[][] UB, double[] abc, double[] sig_abc){
    // print the UB matrix
    if(UB==null) return;
    StringBuffer sb=new StringBuffer(31*3+71*2);
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ )
        sb.append(Format.real(UB[j][i],10,6));
      sb.append("\n");
    }

    // print the lattice parameters
    if(abc==null){
      SharedData.addmsg(sb.toString());
      return;
    }
    for( int i=0 ; i<7 ; i++ )
      sb.append(Format.real(abc[i],10,3));
    sb.append("\n");

    // print the uncertainties for lattice parameters
    if(sig_abc==null){
      SharedData.addmsg(sb.toString());
      return;
    }
    for( int i=0 ; i<7 ; i++ )
      sb.append(Format.real(sig_abc[i],10,3));
    sb.append("\n");

    SharedData.addmsg(sb.toString());
  }

  /**
   * Read in input from the user when not running as an operator. This
   * is currently commented out code until the real implementation is
   * done.
   */
/*  private boolean readUser(){

    String ans=null;
    double delta = 0.01;
    System.out.println(" ENTER ERROR PARAMETER DELTA (DEFAULT=0.01): " );
    ans=readans();
    try{
      if( ans==null || ans.length()<=0)
        delta=0.01;
      else
        delta=Double.parseDouble(ans);
    }catch(NumberFormatException e){
      System.err.println(e);
      return false;
    }
    this.getParameter(1).setValue(new Float(delta));

    nequal = 0;
    nchoice = 0;
    ncell = 0;
    System.out.println(" ");
    System.out.println("HOW WOULD YOU LIKE TO RESTRICT THE SEARCH?");
    System.out.println("    1=LOOK FOR HIGHEST SYMMETRY");
    System.out.println("    2=INPUT A KNOWN CELL TYPE ");
    System.out.println("    3=INPUT SYMMETRIC SCALAR EQUALITIES ");
    System.out.println("<RET>=NO RESTRICTION");
    System.out.println(" ENTER METHOD OF SEARCH: " );

    ans=readans();
    try{
      if(ans==null || ans.length()<=0)
        nchoice=0;
      else
        nchoice=Integer.parseInt(ans);
    }catch(NumberFormatException e){
      System.err.println(e);
      return false;
    }

    System.out.println(" ");
    if (nchoice == 0){  
      System.out.println("NO RESTRICTIONS");
      this.getParameter(2).setValue(choices.elementAt(0));
      return true;
    }else if (nchoice == 1){
      System.out.println("SEARCHING FOR HIGHEST SYMMETRY MATCH");
      this.getParameter(2).setValue(choices.elementAt(1));
      return true;
    }else if (nchoice == 2){
      System.out.println("POSSIBLE CELL TYPES ARE:");
      System.out.println("    1=P, CUBIC");
      System.out.println("    2=F, CUBIC");
      System.out.println("    3=R, HEXAGONAL");
      System.out.println("    4=I, CUBIC");
      System.out.println("    5=I, TETRAGONAL");
      System.out.println("    6=I, ORTHOROMBIC");
      System.out.println("    7=P, TETRAGONAL");
      System.out.println("    8=P, HEXAGONAL");
      System.out.println("    9=C, ORTHOROMBIC");
      System.out.println("   10=C, MONOCLINIC");
      System.out.println("   11=F, ORTHOROMBIC");
      System.out.println("   12=P, ORTHOROMBIC");
      System.out.println("   13=P, MONOCLINIC");
      System.out.println("   14=P, TRICLINIC");
      System.out.println("<RET>=EXIT");
      System.out.println(" ENTER CELL TYPE: " );
      // 
      ans=readans();
      try{
        if(ans==null || ans.length()<=0)
          ncell=0;
        else
          ncell=Integer.parseInt(ans);
      }catch(NumberFormatException e){
        System.err.println(e);
        return false;
      }

      if( ncell==0 || ncell>14)  
        return false;

      System.out.println(" ");
      System.out.println(" SEARCHING FOR CELL TYPE = "+cell[ncell-1]+" ");
      System.out.println(" ");
      this.getParameter(2).setValue(choices.elementAt(ncell+1));
      return true;
    }else if (nchoice == 3){
      System.out.println("THE POSSIBLE SYMMETRIC SCALAR EQUALITIES ARE:");
      System.out.println("    1=(R11 EQ R22 EQ R33)");
      System.out.println("    2=(R11 EQ R22 NE R33)");
      System.out.println("    3=(R11 EQ R33 NE R22)");
      System.out.println("    4=(R11 NE R22 NE R33)");
      System.out.println("<RET>=EXIT");
      // 
      System.out.println(" ENTER EQUALITY: ");
      ans=readans();
      try{
        if(ans==null || ans.length()<=0)
          nequal=0;
        else
          nequal=Integer.parseInt(ans);
      }catch(NumberFormatException e){
        System.err.println(e);
        return false;
      }
      if( nequal==0 || nequal>4)  
        return false;

      this.getParameter(2).setValue(choices.elementAt(nequal+15));
      return true;
    }else{
      return false;
    }

    return true;
  }*/

  /**
   * Read in result from STDIN.
   */
  private static String readans(){
    char c=0;
    String Res="";
    try{
      c =(char) System.in.read();
      while( c >=32){
        Res+=c;
        c =(char) System.in.read();
      }

    }catch(Exception ss){
      return Res;
    }
    return Res;
  }

  /**
   * A method to make printing arrays easier
   */
  private static String arrayToString(Object array){
    if(array==null) return null;

    StringBuffer result=new StringBuffer("[");
    if(array instanceof int[]){
      for( int i=0 ; i<((int[])array).length ; i++ )
        result.append(((int[])array)[i]+", ");
    }else if(array instanceof int[][]){
      for( int i=0 ; i<((int[][])array).length ; i++ )
        result.append(arrayToString(((int[][])array)[i])+", ");
    }else if(array instanceof float[]){
      for( int i=0 ; i<((float[])array).length ; i++ )
        result.append(((float[])array)[i]+", ");
    }else if(array instanceof double[]){
      for( int i=0 ; i<((double[])array).length ; i++ )
        result.append(((double[])array)[i]+", ");
    }else if(array instanceof double[][]){
      for( int i=0 ; i<((double[][])array).length ; i++ )
        result.append(arrayToString(((double[][])array)[i])+", ");
    }else{
      return null;
    }
    result.delete(result.length()-2,result.length());
    result.append("]");

    return result.toString();
  }

  /**
   * Method to calculate the lattice parameters from a given UB matrix
   */
  private static double[] abc(double[][] UB){
    double[] abc=new double[7];
    double[][] UBtrans=new double[3][3];
    for( int i=0 ; i<3 ; i++ )
      for( int j=0 ; j<3 ; j++ )
        UBtrans[i][j]=UB[j][i];
    double[][] UBsquare=LinearAlgebra.mult(UBtrans,UB);
     if(UBsquare==null) return null;
    double[][] invUBsquare=LinearAlgebra.getInverse(UBsquare);
    if(invUBsquare==null) return null;

    // calculate a, b, c
    abc[0]=Math.sqrt(invUBsquare[0][0]);
    abc[1]=Math.sqrt(invUBsquare[1][1]);
    abc[2]=Math.sqrt(invUBsquare[2][2]);

    // calculate alpha, beta, gamma
    abc[3]=invUBsquare[1][2]/(abc[1]*abc[2]);
    abc[4]=invUBsquare[0][2]/(abc[0]*abc[2]);
    abc[5]=invUBsquare[0][1]/(abc[0]*abc[1]);
    abc[3]=Math.acos(abc[3])*180./Math.PI;
    abc[4]=Math.acos(abc[4])*180./Math.PI;
    abc[5]=Math.acos(abc[5])*180./Math.PI;

    // calculate the cell volume
    abc[6]=abc[0]*abc[1]*Math.sin(abc[5]*Math.PI/180.);
    abc[6]=abc[6]/Math.sqrt(UBsquare[2][2]);

    return abc;
  }

  /**
   * Method to generate the hkl sums matrix
   */
  private static double[][] generateVC(Vector peaks){
    if( peaks==null || peaks.size()<=0 ) return null;
    double[][] VC=new double[3][3];
    Peak peak=null;

    double[] hkl=new double[3];

    // find the sum
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      hkl[0]=Math.round(peak.h());
      hkl[1]=Math.round(peak.k());
      hkl[2]=Math.round(peak.l());
      for( int j=0 ; j<3 ; j++ )
        for( int k=0 ; k<3 ; k++ )
          VC[j][k]=VC[j][k]+hkl[j]*hkl[k];
    }

    // find the inverse of VC
    if( LinearAlgebra.invert(VC) )
      return VC;
    else
      return null;
  }

  private static float[][] double2float(double[][] array){
    return LinearAlgebra.double2float(array);
  }

  private static float[] double2float(double[] array){
    return LinearAlgebra.double2float((double[])array);
  }

  /**
   * Main method for testing purposes and running outside of ISAW.
   */
  public static void main (String [] args)  {

    LsqrsJ lsqrs=new LsqrsJ();
/* TEST VERSION
   lsqrs.getParameter(0).setValue(System.getProperty("user.dir"));
   lsqrs.getParameter(0).setValue("/IPNShome/pfpeterson/data/SCD");
   lsqrs.getParameter(1).setValue("quartz");
   lsqrs.getParameter(2).setValue("1:4"); // set histogram numbers
   lsqrs.getParameter(3).setValue("1,3:5,15:20"); // set sequence numbers
   lsqrs.getParameter(4).setValue("/IPNShome/pfpeterson/data/SCD/lookatme.mat");
*/

/*
    if(!lsqrs.readUser())
      System.exit(-1);
*/

    Object obj=lsqrs.getResult();
    if(obj instanceof ErrorString){
      System.out.println(obj);
      System.exit(-1);
    }else{
      System.out.println("RESULT:"+obj);
      System.exit(0);
    }
  }
}
