/*
 * File:  Index.java   
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
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.4  2002/11/27 23:31:01  pfpeterson
 * standardized header
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
 * This operator is intended to run A.J. Schultz's "index"
 * program. This is not heavily tested but works fairly well.
 */
public class Index extends    GenericTOF_SCD {
  public static String command=null;
  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct an operator with a default parameter list.
   */
  public Index( ){
    super( "Index" );
  }
  
  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct operator to execute index.
   *
   *  @param file The name of the peaks file to index. This name is
   *  used to generate the experiment name used by index.
   *  @param delta The error parameter for indexing peaks
   */
  
  public Index( String file, float delta){
    this();
    
    getParameter(0).setValue(file);
    getParameter(1).setValue(new Float(delta));
  }
  
  /* ------------------------- setDefaultParmeters ----------------------- */
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
    addParameter(new FloatPG("Delta",0.05f));
  }
  
  /**
   * This returns the help documentation
   */
  public String getDocumentation(){
    return super.getDocumentation();
  }
  
  /* --------------------------- getCommand ------------------------------ */
  /**
   * @return the command name to be used with script processor, in
   * this case Index.
   */
  public String getCommand(){
    return "Index";
  }
  
  /* --------------------------- getResult ------------------------------- */
  /*
   * Runs scalar using the specified parameters
   */
  public Object getResult(){
    ErrorString eString   = null;
    String      fullfile  = null; // full filename of peaksfile
    String      dir       = null; // directory that the files are all in
    String      expname   = null; // the experiment name
    File        file      = null; // for tests
    int         index     = 0;    // for chopping up strings
    float       delta     = 0f;   // error in index allowed

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

    // find index (with full path)
    if(command==null)
      command=this.getFullIndexName();
      
    // exit out early if no index executable found
    if(command==null)
      return new ErrorString("could not find index executable");

    // get the delta parameter
    IParameter iparm=getParameter(1);
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

    // declare some things
    Process process=null;
    String output=null;
    
    // ------------------------------------------------------------
    try{
      process=SysUtil.startProcess(command,dir);
      BufferedReader in=SysUtil.getSTDINreader(process);
      BufferedReader err=SysUtil.getSTDERRreader(process);
      BufferedWriter out=SysUtil.getSTDOUTwriter(process);
      
      // skip over the first couple of lines
      SysUtil.jumpline(in,err,"PEAKS.LOG file");

      // we want to index a peaks file
      output=SysUtil.readline(in,err);
      SysUtil.writeline(out,"1");
      System.out.println(output+"1");

      // enter the experiment name
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Experiment name")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,expname);
      System.out.println(output+expname);

      // answer no to restricting sequence numbers
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Restrictions")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"n");
      System.out.println(output+"n");
      
      // answer no to restricting histograms
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Restrictions")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"n");
      System.out.println(output+"n");
      
      // tell it where to get the orientation matrix from
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Enter 1, 2")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"3");
      System.out.println(output+"3");
      
      // tell it to use the default crystal
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Crystal No.")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"");
      System.out.println(output+"");

      // tell it to use the default error bars
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("Delta h,k,l")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,delta+" "+delta+" "+delta);
      System.out.println(output+delta+" "+delta+" "+delta);

      // we want to retain the new file
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("DO YOU WISH TO RETAIN")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      SysUtil.writeline(out,"y");
      System.out.println(output+"y");

      // wait for the process to terminate
      process.waitFor();
    }catch(IOException e){
      SharedData.addmsg("IOException reported: "+e.getMessage());
    }catch(InterruptedException e){
      SharedData.addmsg("InterruptedException reported: "+e.getMessage());
    }finally{
      if(process!=null){
        if(process.exitValue()!=0){
          return new ErrorString("BAD EXIT("+process.exitValue()+")");
        }else{
          if(eString!=null)
            return eString;
          else
            return this.getNumIndexed(dir+expname+".peaks");
        }
      }else{
        return new ErrorString("Something went wrong");
      }
    }
  }  
  
  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerEvaluator Operator.  The
   * list of parameters is also copied.
   */
  
  public Object clone(){
    Index new_op = new Index( );
    
    new_op.CopyParametersFrom( this );
    
    return new_op;
  }
  
  /* ------------------------------ PRIVATE METHODS -------------------- */
  /**
   * Method to get the location of the blind executable. Assumed to
   * be right next to the class file.
   */
  private String getFullIndexName(){
    if(SysUtil.isOSokay(SysUtil.LINUX_ONLY)){
      return SysUtil.getBinLocation(this.getClass(),"index");
    }else if(SysUtil.isOSokay(SysUtil.WINDOWS_ONLY)){
      return SysUtil.getBinLocation(this.getClass(),"index.exe");
    }else{
      return null;
    }
  }
  
  private Object getNumIndexed(String filename){
    int tempI=0;
    int maxSeqNum=0;
    int numIndexed=0;

    TextFileReader tfr=null;
    try{
      tfr=new TextFileReader(filename);
      while(! tfr.eof()){
        tempI=tfr.read_int();
        if(tempI==3){ // peak record
          tempI=tfr.read_int();
          if(tempI>maxSeqNum) maxSeqNum=tempI;
          if( tfr.read_int()!=0 || tfr.read_int()!=0 || tfr.read_int()!=0 )
            numIndexed++;
          tfr.read_line(); // skip the rest of the line
        }else{ // skip the line
          tfr.read_line();
        }
      }
    }catch(IOException e){
      return new ErrorString("IOException: "+e.getMessage());
    }catch(NumberFormatException e){
      return new ErrorString("NumberFormatException: "+e.getMessage());
    }finally{
      if(tfr!=null){
        try{
          tfr.close();
        }catch(IOException e){
          System.err.println("Could not close "+filename+":"+e.getMessage());
        }
      }
    }

    return "Indexed "+numIndexed+" of "+maxSeqNum+" peaks";
  }

  /* --------------------------- MAIN METHOD --------------------------- */
  public static void main(String[] args){
    Index op;
    
    System.out.println("********** null constructor");
    op=new Index();
    System.out.println("RESULT: "+op.getResult());
    
    System.out.println("********** full constructor");
    op=new Index("/IPNShome/pfpeterson/data/SCD/test/quartz.peaks",0.05f);
    System.out.println("RESULT: "+op.getResult());
    System.exit(0);
  }
}
