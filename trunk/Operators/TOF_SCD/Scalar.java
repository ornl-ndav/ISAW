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
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.8  2003/12/15 02:33:25  bouzekc
 * Removed unused imports.
 *
 * Revision 1.7  2003/02/20 17:49:45  dennis
 * Added getDocumentation() method. (Joshua Olson)
 *
 * Revision 1.6  2002/11/27 23:31:01  pfpeterson
 * standardized header
 *
 */

package Operators.TOF_SCD;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import DataSetTools.operator.Generic.TOF_SCD.GenericTOF_SCD;
import DataSetTools.parameter.ChoiceListPG;
import DataSetTools.parameter.DataDirPG;
import DataSetTools.parameter.FloatPG;
import DataSetTools.parameter.IParameter;
import DataSetTools.util.ErrorString;
import DataSetTools.util.SharedData;
import DataSetTools.util.StringUtil;
import DataSetTools.util.SysUtil;

/**
 * This operator is intended to run A.J. Schultz's "scalar"
 * program. This is not heavily tested but works fairly well.
 */
public class Scalar extends    GenericTOF_SCD {
  private static Vector       choices   =null;
  private static String       command   =null;

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
   *  @param dir    The directory to run in
   *  @param delta  The error parameter for finding higher symmetry
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
  
  /* --------------------------- getCommand ------------------------------ */
  /**
   * @return the command name to be used with script processor, in
   * this case Scalar.
   */
  public String getCommand(){
    return "Scalar";
  }

 /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */                                                                             
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                        
    s.append("@overview This operator is intended to run A.J. Schultz's ");
    s.append("\"scalar\" program. This is not heavily tested but works ");
    s.append("fairly well.");

    s.append("@assumptions The criteria a) through d) in the first paragrah ");
    s.append("of the 'Algorithm' section are met. \n");
    s.append("'delta' can be successfully stored as a float. \n");
    s.append("The parameter 'choice' is an integer that corresponds to one ");
    s.append("of the symmetries in the vector 'choices'. \n");
    s.append("The operating system is windows or linux. \n");
    s.append("Scalar executable can be found. \n");

    s.append("@algorithm The parameter 'dir' is a string indicating the ");
    s.append("directory to run in.  The operator checks the following ");
    s.append("conditions: \n ");
    s.append("a) 'dir' is a non-empty string \n ");
    s.append("b) the directory indicated by 'dir' exists and is valid\n ");
    s.append("c) the log file in this directory exists \n ");
    s.append("d) the log file can be read \n ");
    s.append("If any of these conditions are not met, then an error string is");
    s.append(" returned and execution of the operator terminates.  Otherwise ");
    s.append("the operator continues. \n\n ");

    s.append("The operator then checks to see if 'delta' can be successfully ");
    s.append("stored as a float.  If it cannot, then an error string is ");
    s.append("returned and execution of the operator terminates.  Otherwise ");
    s.append("the operator continues. \n\n ");

    s.append("The vector 'choices' is an existing variable of the class.  ");
    s.append("This vector contains different types of symmetries.  These ");
    s.append("symmetries are as follows: \n ");
    s.append("1)  Highest Symmetry \n ");
    s.append("2)  P - Cubic  \n ");
    s.append("3)  F - Cubic  \n ");
    s.append("4)  R - Hexagonal  \n ");
    s.append("5)  I - Cubic  \n ");
    s.append("6)  I - Tetragonal  \n ");
    s.append("7)  I - Orthorombic  \n ");
    s.append("8)  P - Tetragonal  \n ");
    s.append("9)  P - Hexagonal  \n ");
    s.append("10) C - Orthorombic \n ");
    s.append("11) C - Monoclinic  \n ");
    s.append("12) F - Orthorombic  \n ");
    s.append("13) P - Orthorombic  \n ");
    s.append("14) P - Monoclinic  \n ");
    s.append("15) P - Triclinic \n ");
    s.append("The parameter 'choice' is an integer that corresponds to one of "); 
    s.append("the symmetries.  The operator determines at what position the ");
    s.append("symmetry corresponding to 'choice' appears in the vector ");
    s.append("'choices'. This position (ie maybe the 3rd or 8th position) is ");
    s.append("stored as an int.  If that integer position is less than zero, ");
    s.append("then it is made equal to zero. \n\n ");

    s.append("'dir', 'delta', and the integer position and its ");
    s.append("corresponding symmetry are printed to the console. \n\n ");

    s.append("The operator then checks if the operating system is either ");
    s.append("windows or linux.  If it is not, then an error string is ");
    s.append("returned and execution of the operator terminates.  Otherwise ");
    s.append("the operator continues. \n\n ");

    s.append("The operator searches for scalar executable.  If none is ");
    s.append("found, then an error string is returned and execution of the ");
    s.append("operator terminates.  Otherwise the operator continues. \n\n ");

    s.append("The scalar's name is printed to the console. \n\n\n ");

    s.append("( The steps described in the next paragraph are an ");
    s.append("overview of the remainder of the operator, and are all ");
    s.append("done in a try block.  The types of errors that are caught ");
    s.append("are described in the last paragraph of this section. ) \n\n ");

    s.append("The value of 'delta' is entered ( by using SysUtil.writeline");
    s.append("() ).  The symmetry we are searching for, indicated ");
    s.append("by 'choice', is entered.  Now the results are calculated in ");
    s.append("the form of a transformation matrix.  If these results cannot ");
    s.append("be found, then an error string is returned and execution of the");
    s.append(" operator terminates.  Otherwise a matrix is constructed, ");
    s.append("containing the results. \n\n\n");

    s.append("Some errors are caught at the end of the operator's execution.");
    s.append("  Any input/output exception or InterruptedException is ");
    s.append("caught.  An error string is returned if the process is not ");
    s.append("ready to properly exit.  If an error string was established ");
    s.append(" at some earlier time, then that string is returned.  Otherwise");
    s.append(" it is safe to assume that everything went well, and the ");
    s.append("transformation matrix is returned. ");

    s.append("@param dir The directory to run in ");
    s.append("@param delta The error parameter for finding higher symmetry ");
    s.append("@param choice The number indicating what type of search to do ");
    s.append("(part of a choicelist)");

    s.append("@return If successful, this returns a matrix consisting of the ");
    s.append("results. \n");
    s.append("If the transformation matrix cannot be created or found, then ");
    s.append("'null' is returned.");

    s.append("@error Returns an appropriate error string when any of the ");
    s.append("'Assumptions' are not true. ");
    s.append("@error A few other errors are discussed in the last paragraph");
    s.append(" of the 'Algorithm' section.");

    return s.toString();
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
    StringBuffer matrix  = null;
    ErrorString  eString = null;
    
    // the first parameter is the directory to run in
    String     dir     = getParameter(0).getValue().toString();
    if( dir==null || dir.length()==0){
      return new ErrorString("null/empty directory specified");
    }else{
      // check that the directory is okay
      File dirF=new File(dir);
      if(!dirF.exists())
        return new ErrorString(dir+" does not exist");
      if(!dirF.isDirectory())
        return new ErrorString(dir+" is not a directory");
      /*if(!dirF.canWrite())   // DOES NOT WORK ON WIN32
	return new ErrorString("cannot write in "+dir);*/

      // now check the log file
      File log=new File(dir+"/blind.log");
      if(!log.exists())
        return new ErrorString("blind.log does not exist");
      if(!log.canRead())
        return new ErrorString("cannot read blind.log");
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
        return new ErrorString("First parameter of incompatible type");
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
      return new ErrorString("must be using linux or windows system");
      
    // declare some things
    Process process=null;
    String output=null;
    if(command==null){
      command=this.getFullScalarName();
    }
    
    // exit out early if no scalar executable found
    if(command==null)
      return new ErrorString("could not find scalar executable");
    System.out.println("EXE:"+command);
      
    // ------------------------------------------------------------
    try{
      String temp;
      process=SysUtil.startProcess(command,dir);
      BufferedReader in=SysUtil.getSTDINreader(process);
      BufferedReader err=SysUtil.getSTDERRreader(process);
      BufferedWriter out=SysUtil.getSTDOUTwriter(process);
      
      // skip over the first couple of lines
      SysUtil.jumpline(in,err,"Scalars obtained");
      
      // enter the value of delta
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("PARAMETER DELTA")<0 ){
        if( output!=null && output.length()>0){
          System.out.println(output);
        }
        output=SysUtil.readline(in,err);
      }
      temp=Float.toString(delta);
      SysUtil.writeline(out,temp);
      System.out.println(output+temp);
      
      // enter the symmetry we are searching for
      output=SysUtil.readline(in,err);
      while( output==null || output.indexOf("METHOD OF SEARCH")<0 ){
        if(output!=null) System.out.println(output);
        output=SysUtil.readline(in,err);
      }
      if(choice==0){
        temp=Integer.toString(1);
        SysUtil.writeline(out,temp);
        System.out.println(output+temp);
      }else{
        temp=Integer.toString(2);
        SysUtil.writeline(out,temp);
        System.out.println(output+temp);
        SysUtil.jumpline(in,err,"EXIT");
        output=SysUtil.readline(in,err);
        temp=Integer.toString(choice);
        SysUtil.writeline(out,temp);
        System.out.println(output+temp);
      }

      // then skip down to the results and print them
      output=SysUtil.readline(in,err);
      while( output==null || (output.indexOf("TRANSFORMATION MATRIX")<0 
                                    && output.indexOf("NO MATCHES FOUND")<0) ){
        if(output!=null) System.out.println(output);
        output=SysUtil.readline(in,err);
      }
      if(output.indexOf("NO MATCHES FOUND")>=0){
        System.out.println(output);
        eString=new ErrorString("NO MATCHES FOUND");
        return null;
      }
      output=SysUtil.readline(in,err); // first line is empty
      System.out.println(output);

      // construct the matrix
      matrix=new StringBuffer(40);
      for( int i=0 ; i<3 ; i++ ){
        output=SysUtil.readline(in,err);
        matrix.append(output.trim()+"  ");
        System.out.println(output);
      }
      matrix=formMatrix(matrix);

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
            return matrix.toString();
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
  
  /**
   * format the matrix from scalar in a way that Lsqrs likes.
   */
  private static StringBuffer formMatrix(StringBuffer orig){
    StringBuffer result=new StringBuffer(40);
    float[][] matrix=new float[3][3];

    // first get rid of extra spaces
    StringUtil.trim(orig);
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        matrix[j][i]=StringUtil.getFloat(orig);
        StringUtil.trim(orig);
      }
    }

    result.append("[");
    for( int i=0 ; i<3 ; i++ ){
      result.append("[");
      for( int j=0 ; j<3 ; j++ ){
        result.append(Float.toString(matrix[i][j]));
        if(j!=2) result.append(",");
      }
      result.append("]");
    }
    result.append("]");

    return result;
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
