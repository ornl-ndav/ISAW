/*
 * File:  Lsqrs.java   
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2002/11/21 21:54:59  pfpeterson
 * Added to CVS.
 *
 */

package Operators.TOF_SCD;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.Generic.TOF_SCD.*;
import  DataSetTools.parameter.*;

/**
 * This operator is intended to run A.J. Schultz's "lsqrs"
 * program. This is not heavily tested but works fairly well.
 */
public class Lsqrs extends    GenericTOF_SCD {
  public  static String command=null;
  private static final String identmat="[[1,0,0][0,1,0][0,0,1]]";

  /**
   * Construct an operator with a default parameter list.
   */
  public Lsqrs( ){
    super( "Lsqrs" );
  }
  
  /**
   *  Construct operator to execute index.
   *
   *  @param file The name of the peaks file to index. This name is
   *  used to generate the experiment name used by index.
   *  @param matrix The transformation matrix to turn this into the
   *  desired symmetry group.
   */
  
  public Lsqrs( String file, String matrix){
    this();
    
    getParameter(0).setValue(file);
    getParameter(1).setValue(matrix);
  }
  
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters = new Vector();  // must do this to create empty list of 
                                // parameters
    
    parameters=new Vector();
    LoadFilePG lfpg=new LoadFilePG("Peaks file",null);
    lfpg.setFilter(new PeaksFilter());
    addParameter(lfpg);
    addParameter(new StringPG("Transform Matrix",identmat));
  }
  
  /**
   * This returns the help documentation
   */
  public String getDocumentation(){
    return super.getDocumentation();
  }
  
  /**
   * @return the command name to be used with script processor, in
   * this case Lsqrs.
   */
  public String getCommand(){
    return "Lsqrs";
  }
  
  /*
   * Runs lsqrs using the specified parameters
   */
  public Object getResult(){
    ErrorString eString   = null;
    String      fullfile  = null; // full filename of peaksfile
    String      dir       = null; // directory that the files are all in
    String      expname   = null; // the experiment name
    File        file      = null; // for tests
    int         index     = 0;    // for chopping up strings
    float[][]   matrix    = null; // error in index allowed

    // get the full file name from the script and test it out
    fullfile=getParameter(0).getValue().toString();
    fullfile=FilenameUtil.fixSeparator(fullfile);
    file=new File(fullfile);
    if( !file.exists() )
      return new ErrorString("file does not exist: "+fullfile);
    else if( !file.isFile() )
      return new ErrorString("not a regular file "+fullfile);
    else if( !file.canRead() )
      return new ErrorString("file is not readable "+fullfile);
    
    // determine the directory we are working in and the peaksfilename
    index=fullfile.lastIndexOf("/");
    if(index>=0){
      dir=fullfile.substring(0,index+1);
      expname=fullfile.substring(index+1);
    }else{
      return new ErrorString("bad filename "+fullfile);
    }
    if(!SysUtil.isDirectory(dir))
      return new ErrorString("cannot find directory "+dir);
    index=expname.lastIndexOf(".peaks");
    if(index>=0){
      expname=expname.substring(0,index);
    }else{
      return new ErrorString("could not create experiment name from "
                             +expname);
    }
      
    // determine the experiment file
    file=new File(dir+expname+".x");
    if( !file.exists() )
      return new ErrorString("file does not exist: "+dir+expname+".x");
    else if( !file.isFile() )
      return new ErrorString("not a regular file "+dir+expname+".x");
    else if( !file.canRead() )
      return new ErrorString("file is not readable "+dir+expname+".x");
    
    // find lsqrs (with full path)
    if(command==null)
      command=this.getFullLsqrsName();
    
    // exit out early if no index executable found
    if(command==null)
      return new ErrorString("could not find index executable");
      

    // get the transformation matrix

    IParameter iparm=getParameter(1);
    if(iparm.getValue() == null){
      matrix=null;
    }else{
      try{
        matrix=stringTo2dArray(iparm.getValue().toString());
      }catch(NumberFormatException e){
        return new ErrorString("Improper format in matrix");
      }
    }
    iparm=null;
    printmat("MAT=",matrix);
      
    // declare some things
    Process process=null;
    String output=null;
      
    // ------------------------------------------------------------
    try{
      process=SysUtil.startProcess(command,dir);
      BufferedReader in=SysUtil.getSTDINreader(process);
      BufferedReader err=SysUtil.getSTDERRreader(process);
      BufferedWriter out=SysUtil.getSTDOUTwriter(process);

      // first we tell it the experiment name
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Experiment name")<0 ){
        if(output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,expname);
      System.out.println(output+expname);

      // get to the first choice
      SysUtil.jumpline(in,err,"INTEGRATE output file");
      output=SysUtil.readline(in,err);
      SysUtil.writeline(out,"1");
      System.out.println(output+"1");

      // we do not want to select individual histograms
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("individual histograms")<0 ){
        if(output!=null&&output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"n");
      System.out.println(output+"n");

      // we do not want to reduce wavelength range
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("WLMIN and WLMAX")<0 ){
        if(output!=null&&output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"");
      System.out.println(output+"");

      // we do not want to set a minimum peak count
      output=SysUtil.readline(in,err);
      SysUtil.writeline(out,"");
      System.out.println(output+"");

      // we do not want to set a crystal number
      output=SysUtil.readline(in,err);
      SysUtil.writeline(out,"");
      System.out.println(output+"");

      // where to list the reflections
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Input 1, 2")<0 ){
        if(output!=null&&output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"2");
      System.out.println(output+"2");
      

      // the transformatin matrix question
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("transformation matrix")<0 ){
        if(output!=null&&output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      if(matrix==null){
        // we don't have a matrix to enter
        SysUtil.writeline(out,"n");
        System.out.println(output+"n");
      }else{
        //put in the transformation matrix
        SysUtil.writeline(out,"y");
        System.out.println(output+"y");
        output=SysUtil.readline(in,err);
        System.out.println(output);
        StringBuffer row=new StringBuffer(10);
        for( int i=0 ; i<3 ; i++ ){
          output=SysUtil.readline(in,err);
          if(row.length()>0) row.delete(0,row.length());
          for( int j=0 ; j<3 ; j++ ){
            row.append(matrix[i][j]+" ");
          }
          row.delete(row.length()-1,row.length());
          SysUtil.writeline(out,row.toString());
          System.out.println(output+row.toString());
        }
        // answer no this time
        output=SysUtil.readline(in,err);
        while( output==null || output.indexOf("transformation matrix")<0 ){
          if(output!=null&&output.length()>0){
            System.out.println(output);
          }
          output=SysUtil.readline(in,err);
        }
        SysUtil.writeline(out,"n");
        System.out.println(output+"n");
        // set the matrix parameter to be the identity matrix
        getParameter(1).setValue(identmat);
      }

      // we do not want to select sequence numbers
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("select individual SEQNUM")<0 ){
        if(output!=null&&output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"n");
      System.out.println(output+"n");

      // do we want to the store the matrix in a separate file
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("parameters in a matrix file")<0 ){
        if(output!=null&&output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"n");
      System.out.println(output+"n");

      // do we want to the store the in the experiment file
      output=SysUtil.readline(in,err);
      while(output==null||output.indexOf("parameters in the expnam.x file")<0){
        if(output!=null&&output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"y");
      System.out.println(output+"y");

      // wait for the process to terminate
      process.waitFor();
      if(process.exitValue()!=0)
        return new ErrorString("BAD EXIT("+process.exitValue()+")");
    }catch(IOException e){
      SharedData.addmsg("IOException reported: "+e.getMessage());
    }catch(InterruptedException e){
      SharedData.addmsg("InterruptedException reported: "+e.getMessage());
    }
    if(process==null){
      return new ErrorString("Something went wrong");
    }else if(eString!=null){
      return eString;
    }else{
      return "updated "+dir+expname+".x";
    }
  }  
  
  /**
   * Get a copy of the current SpectrometerEvaluator Operator.  The
   * list of parameters is also copied.
   */
  
  public Object clone(){
    Lsqrs new_op = new Lsqrs( );
    
    new_op.CopyParametersFrom( this );
    
    return new_op;
  }
  
  /* ------------------------------ PRIVATE METHODS -------------------- */
  /**
   * Method to get the location of the lsqrs executable. Assumed to
   * be right next to the class file.
   */
  private String getFullLsqrsName(){
    if(SysUtil.isOSokay(SysUtil.LINUX_ONLY)){
      return SysUtil.getBinLocation(this.getClass(),"lsqrs");
    }else if(SysUtil.isOSokay(SysUtil.WINDOWS_ONLY)){
      return SysUtil.getBinLocation(this.getClass(),"lsqrs.exe");
    }else{
      return null;
    }
  }
  
  /**
   * This takes a String representing a 3x3 matrix and turns it into a
   * float[3][3]. The method does assume that the matrix needs to be
   * transposed which is true when going from Java to FORTRAN
   * notation.
   *
   * @param text A String to be converted
   *
   * @return The matrix as a float[3][3]
   */
  private static float[][] stringTo2dArray(String text) 
                                                  throws NumberFormatException{
    // check that there is something to parse
    if(text==null || text.length()==0) return null;

    // now take up some memory
    float[][] matrix=new float[3][3];
    int index;
    float temp;
    
    // start with a StringBuffer b/c they are nicer to parse
    StringBuffer sb=new StringBuffer(text);
    sb.delete(0,2);
    try{
      // repeat for each row
      for( int i=0 ; i<3 ; i++ ){
        // parse the first two columns which are ended by ','
        for( int j=0 ; j<2 ; j++ ){
          index=sb.toString().indexOf(",");
          if(index>0){
            temp=Float.parseFloat(sb.substring(0,index));
            sb.delete(0,index+1);
            matrix[i][j]=temp;
          }else{
            return null;
          }
        }
        // the third column is ended by ']'
        index=sb.toString().indexOf("]");
        if(index>0){
          temp=Float.parseFloat(sb.substring(0,index));
          sb.delete(0,index+2);
          matrix[i][2]=temp;
        }else{
          return null;
        }
      }
    }catch(NumberFormatException e){
      // something went wrong so exit out
      throw e;
    }

    // if it is the identity matrix then we should just return null
    boolean isident=true;
    for( int i=0 ; i<3 ; i++ ){
      if(isident){ // breakout if it is not the identity matrix
        for( int j=0 ; j<3 ; j++ ){
          if(i==j){ // should be one
            if(matrix[i][j]!=1f){
              isident=false;
              break;
            }
          }else{    // should be zero
            if(matrix[i][j]!=0f){
              isident=false;
              break;
            }
          }
        }
      }
    }
    if(isident) return null;

    // if we get here we need to transpose the matrix for FORTRAN
    float[][] transmat=new float[3][3];
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        transmat[j][i]=matrix[i][j];
      }
    }

    // return the FORTRAN friendly result
    return transmat;
  }

  /**
   * Utility method to encapsulate the printing of the 3x3 float array
   * for debuging purposes.
   */
  private static void printmat(String label, float[][] matrix){
    System.out.print(label);
    if(matrix==null){
      System.out.println("null");
      return;
    }else{
      System.out.print("[");
      for( int i=0 ; i<3 ; i++ ){
        System.out.print("[");
        for( int j=0 ; j<3 ; j++ ){
          System.out.print(matrix[i][j]);
          if(j!=2)
          System.out.print(",");
        }
        System.out.print("]");
      }
      System.out.println("]");
    }
  }

  /* --------------------------- MAIN METHOD --------------------------- */
  public static void main(String[] args){
    Lsqrs op;
    
    System.out.println("********** null constructor");
    op=new Lsqrs();
    System.out.println("RESULT: "+op.getResult());
    
    System.out.println("********** full constructor");
    op=new Lsqrs("/IPNShome/pfpeterson/data/SCD/test/quartz.peaks",
                 "[[1,0,0][0,1,0][0,0,1]]");
    System.out.println("RESULT: "+op.getResult());
    System.exit(0);
  }
}
