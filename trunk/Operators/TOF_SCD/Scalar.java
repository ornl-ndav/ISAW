/*
 * File:  Scalar.java   
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
 */

package Operators.TOF_SCD;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.Generic.TOF_SCD.*;
import  DataSetTools.parameter.*;

/**
 * This operator is intended to run A.J. Schultz's "scalar"
 * program. This is not heavily tested but works fairly well.
 */
public class Scalar extends    GenericTOF_SCD {
  private static Vector       choices   =null;

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct an operator with a default parameter list.
   */
  public Scalar( ){
    super( "Scalar" );
    //if(choices==null) init_choices();
    //if(choiceparm!=null) choiceparm.addItems(choices);
  }
  
  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct operator to execute scalar.
   *
   *  @param delta The error parameter for finding higher symmetry
   *  @param choice The number of the type of search to do
   */
  
  public Scalar( String dir, float delta, int choice ){
    this();
    
    //if(choices==null) init_choices();
    if(choice <0 || choice>=choices.size()) choice=0;
    
    if(getParameter(0) instanceof DataDirPG)
      getParameter(0).setValue(dir);
    if(getParameter(1) instanceof FloatPG)
      getParameter(1).setValue(new Float(delta));
    if(getParameter(2) instanceof ChoiceListPG)
      getParameter(2).setValue(choices.elementAt(choice));
    
    //if(choice <0 || choice>=choices.size()) choice=0;
    //choiceparm=new ChoiceListPG("SearchMethod",choices.elementAt(0));
    //choiceparm.addItems(choices);
    //choiceparm.setValue(choices.elementAt(choice));
  }
  
  /* ------------------------- setDefaultParmeters ----------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters = new Vector();  // must do this to create empty list of 
    // parameters
    
    // initialize the choice parameter
    //System.out.println("(sdp1)CHOICES="+choices);
    if(choices==null || choices.size()==0) init_choices();
    ChoiceListPG choiceparm=new ChoiceListPG("SearchMethod",
                                             choices.elementAt(0));
    choiceparm.addItems(choices);
    //System.out.println("(sdp2)CHOICES"+choices);
    
    /*if( parameters==null || getNum_parameters()!=3){
      parameters=new Vector();
      addParameter(new DataDirPG("Working Directory",null));
      addParameter(new FloatPG("Delta",0.01f));
      addParameter(choiceparm);
      }else{
      if(!(getParameter(0) instanceof DataDirPG))
      setParameter(new DataDirPG("Working Directory",null),0);
      if(!(getParameter(1) instanceof FloatPG))
      setParameter(new FloatPG("Delta",0.01f),1);
      if(!(getParameter(2) instanceof ChoiceListPG))
      setParameter(choiceparm,2);
      }*/
    
    parameters=new Vector();
    addParameter(new DataDirPG("Working Directory",null));
    addParameter(new FloatPG("Delta",0.01f));
    //choiceparm=new ChoiceListPG("SearchMethod",choices.elementAt(0));
    //choiceparm.addItems(choices);
    addParameter(choiceparm);
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
   * this case Scalar.
   */
  public String getCommand(){
    return "Scalar";
  }
  
  /* --------------------------- getResult ------------------------------- */
  /*
   * Runs scalar using the specified parameters
   */
  public Object getResult(){
    float        delta   = 0f;
    int          choice  = 0;
    String       choiceS = null;
    IParameter   iparm   = null;
    Object       value   = null;
    String       fail    = "FAILED";
    StringBuffer matrix  = null;
    ErrorString  eString = null;
    
    // the first parameter is the directory to run in
    String     dir     = getParameter(0).getValue().toString();
    if( dir==null || dir.length()==0){
      return new ErrorString(fail+": null/empty directory specified");
    }else{
      // check that the directory is okay
      File dirF=new File(dir);
      if(!dirF.exists())
        return new ErrorString(fail+": "+dir+" does not exist");
      if(!dirF.isDirectory())
        return new ErrorString(fail+": "+dir+" is not a directory");
      if(!dirF.canWrite())
        return new ErrorString(fail+": cannot write in "+dir);

      // now check the log file
      File log=new File(dir+"/blind.log");
      if(!log.exists())
        return new ErrorString(fail+": blind.log does not exist");
      if(!log.canRead())
        return new ErrorString(fail+": cannot read blind.log");
    }


    // the second parameter should be a float
    iparm=getParameter(1);
    if( iparm instanceof FloatPG ){
      delta=((FloatPG)iparm).getfloatValue();
    }else{
      value=iparm.getValue();
      if( value instanceof Float ){
        delta=((Float)value).floatValue();
      }else{
        return new ErrorString(fail+": First parameter of incompatible type");
      }
    }
    
    // the third parameter is part of a choicelist
    choiceS=getParameter(2).getValue().toString();
    choice=choices.indexOf(choiceS);
    if(choice<0) choice=0;
    
    // delete the parameter and its value
    iparm=null;
    value=null;
    
    System.out.println("dir="+dir+" delta="+delta
                       +" choice("+choice+")="+choices.elementAt(choice));

    System.out.println("========================================");
    // first check if the OS is acceptable
    if(! SysUtil.isOSokay(SysUtil.LINUX_WINDOWS) )
      return new ErrorString(fail+": must be using linux or windows system");
      
    // declare some things
    Process process=null;
    String output=null;
    String command=this.getFullScalarName();
    
    // exit out early if no scalar executable found
    if(command==null)
      return new ErrorString(fail+": could not find scalar executable");
    System.out.println("EXE:"+command);
      
    // ------------------------------------------------------------
    try{
      String temp;
      process=SysUtil.startProcess(command,dir);
      BufferedReader in=SysUtil.getSTDINreader(process);
      BufferedWriter out=SysUtil.getSTDOUTwriter(process);
      
      // skip over the first couple of lines
      SysUtil.jumpline(in,"Scalars obtained");
      
      // enter the value of delta
      output=SysUtil.readline(in);
      while( output==null || output.indexOf("PARAMETER DELTA")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in);
      }
      temp=Float.toString(delta);
      SysUtil.writeline(out,temp);
      System.out.println(output+temp);
      
      // enter the symmetry we are searching for
      output=SysUtil.readline(in);
      while( output==null || output.indexOf("METHOD OF SEARCH")<0 ){
        if(output!=null) System.out.println(output);
        output=SysUtil.readline(in);
      }
      if(choice==0){
        temp=Integer.toString(1);
        SysUtil.writeline(out,temp);
        System.out.println(output+temp);
      }else{
        temp=Integer.toString(2);
        SysUtil.writeline(out,temp);
        System.out.println(output+temp);
        SysUtil.jumpline(in,"EXIT");
        output=SysUtil.readline(in);
        temp=Integer.toString(choice);
        SysUtil.writeline(out,temp);
        System.out.println(output+temp);
      }

      // then skip down to the results and print them
      output=SysUtil.readline(in);
      while( output==null || (output.indexOf("TRANSFORMATION MATRIX")<0 
                                    && output.indexOf("NO MATCHES FOUND")<0) ){
        if(output!=null) System.out.println(output);
        output=SysUtil.readline(in);
      }
      if(output.indexOf("NO MATCHES FOUND")>=0){
        System.out.println(output);
        eString=new ErrorString("NO MATCHES FOUND");
        return null;
      }
      output=SysUtil.readline(in); // first line is empty
      System.out.println(output);

      // construct the matrix
      matrix=new StringBuffer();
      matrix.append("[");
      for( int i=0 ; i<3 ; i++ ){
        output=SysUtil.readline(in);
        matrix.append("["+output.trim()+"]");
        if(i!=2) matrix.append(",");
        System.out.println(output);
      }
      matrix.append("]");

      // wait for the process to terminate
      process.waitFor();
    }catch(IOException e){
      SharedData.addmsg("IOException reported: "+e.getMessage());
    }catch(InterruptedException e){
      SharedData.addmsg("InterruptedException reported: "+e.getMessage());
    }finally{
      if(process!=null){
        if(process.exitValue()!=0){
          return new ErrorString(fail+"("+process.exitValue()+")");
        }else{
          if(eString!=null)
            return eString;
          else
            return matrix.toString();
        }
      }else{
        return new ErrorString(fail);
      }
    }
  }  
  
  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerEvaluator Operator.  The
   * list of parameters is also copied.
   */
  public Object clone(){
    Scalar new_op = new Scalar();
    new_op.CopyParametersFrom( this );
    
    return new_op;
  }
  
  /* ------------------------------ PRIVATE METHODS -------------------- */
  /**
   * Method to get the location of the blind executable. Assumed to
   * be right next to the class file.
   */
  private String getFullScalarName(){
    if(SysUtil.isOSokay(SysUtil.LINUX_ONLY)){
      return SysUtil.getBinLocation(this.getClass(),"scalar");
    }else if(SysUtil.isOSokay(SysUtil.WINDOWS_ONLY)){
      return SysUtil.getBinLocation(this.getClass(),"scalar.exe");
    }else{
      return null;
    }
  }

  /**
   * Initialize the static vector of possible choices
   */
  private void init_choices(){
    choices=new Vector();
    choices.add("Highest Symmetry");
    choices.add("P - Cubic");
    choices.add("F - Cubic");
    choices.add("R - Hexagonal");
    choices.add("I - Cubic");
    choices.add("I - Tetragonal");
    choices.add("I - Orthorombic");
    choices.add("P - Tetragonal");
    choices.add("P - Hexagonal");
    choices.add("C - Orthorombic");
    choices.add("C - Monoclinic");
    choices.add("F - Orthorombic");
    choices.add("P - Orthorombic");
    choices.add("P - Monoclinic");
    choices.add("P - Triclinic");
  }
  
  /* --------------------------- MAIN METHOD --------------------------- */
  public static void main(String[] args){
    Scalar op;
    
    System.out.println("********** null constructor");
    op=new Scalar();
    System.out.println("RESULT: "+op.getResult());
    
    System.out.println("********** full constructor");
    //op=new Scalar("/IPNShome/pfpeterson/data/SCD/",.2f,3);
    op=new Scalar("/IPNShome/pfpeterson/data/SCD/",.01f,3);
    System.out.println("RESULT: "+op.getResult());
    System.exit(0);
  }
}
