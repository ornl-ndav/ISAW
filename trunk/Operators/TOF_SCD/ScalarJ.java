/*
 * File:  ScalarJ.java   
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
 * Revision 1.5  2003/05/01 19:35:15  pfpeterson
 * Created a method to add found matrices into the index.
 *
 * Revision 1.4  2003/05/01 16:38:02  pfpeterson
 * Changed some variable names to add to readability and added LOTS
 * of comments.
 *
 * Revision 1.3  2003/04/30 19:50:49  pfpeterson
 * Added ability to work with orientation matrix, in addition to cell
 * scalars from 'blind.log'.
 *
 * Revision 1.2  2003/03/27 23:11:22  pfpeterson
 * Added clause to deal with NumberFormatException when reading the log file.
 *
 * Revision 1.1  2003/02/14 20:40:31  pfpeterson
 * Added to CVS.
 *
 */

/*
 *  Initial code produced using:
 *
 *  Produced by f2java.  f2java is part of the Fortran-
 *  -to-Java project at the University of Tennessee Netlib
 *  numerical software repository.
 *
 *  Original authorship for the BLAS and LAPACK numerical
 *  routines may be found in the Fortran source, available at
 *  www.netlib.org.
 *
 *  Fortran input file: scalar.f
 *
 *  The f2j compiler code was written by
 *  David M. Doolin (doolin@cs.utk.edu) and
 *  Keith  Seymour (seymour@cs.utk.edu)
 */

package Operators.TOF_SCD;

import DataSetTools.math.LinearAlgebra;
import DataSetTools.operator.DataSet.Attribute.LoadOrientation;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.parameter.*;
import DataSetTools.util.ErrorString;
import DataSetTools.util.FilenameUtil;
import DataSetTools.util.Format;
import DataSetTools.util.SharedData;
import DataSetTools.util.TextFileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * Class designed to analyze cell scalars from BLIND to determine Laue
 * symmetry. Originally written by R. Goyette.
 */
public class ScalarJ extends GenericTOF_SCD{
  private static final boolean DEBUG=false;

  // valid values of nchoice (generic symmetry type)
  private static final int NO_RESTRICTION   = 0;
  private static final int HIGHEST_SYMMETRY = 1;
  private static final int KNOWN_CELL       = 2;
  private static final int SYMMETRIC        = 3;

  // valid values of ncell (cell type)
  private static final int P_CUBIC       = 1;
  private static final int F_CUBIC       = 2;
  private static final int R_HEXAGONAL   = 3;
  private static final int I_CUBIC       = 4;
  private static final int I_TETRAGONAL  = 5;
  private static final int I_ORTHOROMBIC = 6;
  private static final int P_TETRAGONAL  = 7;
  private static final int P_HEXAGONAL   = 8;
  private static final int C_ORTHOROMBIC = 9;
  private static final int C_MONOCLINIC  = 10;
  private static final int F_ORTHOROMBIC = 11;
  private static final int P_ORTHOROMBIC = 12;
  private static final int P_MONOCLINIC  = 13;
  private static final int P_TRICLINIC   = 14;

  // valid values of nequal (specify family of valid cell types)
  private static final int A_EQ_B_EQ_C = 1;
  private static final int A_EQ_B_NE_C = 2;
  private static final int A_EQ_C_NE_B = 3;
  private static final int A_NE_B_NE_C = 4;

  private double delta= 0.0;
  private int i= 0;
  private int [] k= new int[60];
  private int [] l= new int[60];
  private double[] scalars=null;
  private double sig11= 0.0;
  private double sig22= 0.0;
  private double sig33= 0.0;
  private double sig23= 0.0;
  private double sig31= 0.0;
  private double sig12= 0.0;
  private double sig11sq= 0.0;
  private double sig22sq= 0.0;
  private double sig33sq= 0.0;
  private double sig23sq= 0.0;
  private double sig31sq= 0.0;
  private double sig12sq= 0.0;
  private int nequal= 0;
  private int nchoice= NO_RESTRICTION;
  private int ncell= 0;
  private int Goto= 0;
  private int npick= 0;
  private static String [] cell = {"P, CUBIC", "F, CUBIC", "R, HEXANONAL",
                        "I, CUBIC", "I, TETRAGONAL", "I, ORTHORHOMBIC",
                        "P, TETRAGONAL", "P, HEXAGONAL", "C, ORTHORHOMBIC",
                        "C, MONOCLINIC", "F, ORTHORHOMBIC", "P, ORTHORHOMBIC", 
                        "P, MONOCLINIC", "P, TRICLINIC" };
  private static double [] trans =
                           {1., 0., 0.,   0., 1., 0.,   0., 0., 1.,
                            1., 1., 1.,  -1., 1., 1.,   0., 0., 2.,
                            0., 0., 2.,   1., 1., 1.,  -1., 1., 1.,
                           -1., 1., 1.,   0., 0., 2.,   1., 1., 1.,
                            1., 1.,-1.,   1.,-1., 1.,   1.,-1.,-1.,
                           -1., 1., 1.,   1., 0., 1.,   0., 1.,-1.,
                            1., 0., 1.,  -1., 1., 1.,   0.,-1., 1.,
                            1., 0., 1.,   1., 1., 0.,   0., 1., 1.,
                            1., 1., 0.,   0., 1., 1.,   1., 0., 1.,
                            1., 0., 1.,   1., 1., 0.,   0., 1., 1.,
                            0., 1., 1.,   1., 0., 1.,   1., 1., 0.,
                            1., 0., 1.,   1., 1., 0.,   0., 1., 1.,
                            0., 1., 1.,   1., 0., 1.,   1., 1., 0.,
                            1., 1., 0.,   0., 1., 1.,   1., 0., 1.,
                            1., 0., 0.,   0., 1., 0.,   0., 0., 1.,
                            1.,-1., 0.,   0., 1., 0.,   0., 0., 1.,
                            1., 0., 0.,   0., 1., 0.,   0., 0., 1.,
                           -1., 1., 0.,   1., 1., 0.,   0., 0.,-1.,
                            1.,-1., 0.,   1., 1., 0.,   0., 0.,-1.,
                            1., 1., 0.,  -1., 1., 0.,   0., 1., 1.,
                            1.,-1., 0.,   1., 1., 0.,   1., 0., 1.,
                            1., 1., 0.,  -1., 1., 0.,   0., 1., 1.,
                            2.,-1., 0.,   1., 1., 0.,   1., 0., 1.,
                            1.,-1., 0.,   1., 1., 0.,   0., 0., 1.,
                            1.,-1., 0.,   1., 1., 0.,   0., 0., 1.,
                            0., 1., 0.,   0., 0., 1.,   1., 0., 0.,
                            0., 1., 0.,   0., 0., 1.,   1., 0., 0.,
                            1., 1., 0.,   0., 0., 1.,   1.,-1., 0.,
                           -1., 0.,-1.,   0., 0., 2.,   0., 1., 1.,
                            0., 1., 1.,   0., 0., 2.,   1., 0., 1.,
                           -1., 1., 0.,   0., 0., 1.,   1., 1., 0.,
                            1., 1., 0.,   0., 0., 1.,   1.,-1., 0.,
                            0., 1.,-1.,   0., 0., 3.,   1., 0., 1.,
                           -1., 1.,-1.,   0., 2., 0.,   1., 1.,-1.,
                            1., 1.,-1.,   0., 0.,-1.,  -1., 1., 0.,
                            1., 0., 0.,   0., 1., 0.,   0., 0., 1.,
                            1.,-1., 0.,   0., 2., 0.,   0., 0., 1.,
                            1., 1., 0.,   0., 2., 0.,   0., 0., 1.,
                           -1., 0., 0.,   0., 0., 1.,   0., 1., 0.,
                            1., 0., 0.,   0., 0., 1.,   0.,-1., 0.,
                            0., 2., 0.,   0., 0., 1.,   1., 1., 0.,
                            1., 0., 0.,   0., 1., 0.,   0., 0., 1.,
                            2., 0., 0.,   0., 0.,-1.,   1., 1., 0.,
                            2., 0., 0.,   0., 0., 1.,   1.,-1., 0.,
                            0., 0., 0.,   0.,-2., 1.,   1.,-1., 0.,
                            0., 1., 0.,   0., 0., 1.,   1., 0., 0.,
                            1.,-1., 0.,   0., 2., 0.,   0., 1., 1.,
                            1., 1., 0.,   0., 2., 0.,   0., 1., 1.,
                            0., 0.,-1.,   2., 0., 0.,   1.,-1., 0.,
                            0., 0., 1.,   2., 0., 0.,   1., 1., 0.,
                           -1.,-1., 0.,   2., 0., 0.,   0., 0., 1.,
                            1.,-1., 0.,   2., 0., 0.,   0., 0., 1.,
                            2., 0., 0.,   0., 2., 0.,   1., 1., 1.,
                            2., 0.,-1.,   0., 0., 1.,   1.,-1., 0.,
                            0., 0., 1.,   0.,-2., 0.,   1.,-1., 0.,
                            1.,-1., 0.,   0., 2., 0.,   0., 0., 1.,
                            0., 2., 0.,   0., 0., 1.,   1., 1., 0.,
                            1.,-1., 0.,   0.,-2., 0.,   1.,-1.,-1.,
                            1., 0., 0.,   0., 1., 0.,   0., 0., 1.,
                            1., 0., 0.,   0., 1., 0.,   0., 0., 1. };

  private static Vector choices=null;


  /**
   * Construct an operator with a default parameter list.
   */
  public ScalarJ(){
    super("JScalar");
  }

  /**
   * Constructs a fully configured scalar operator
   *
   * @param dir directory to find the blind log file
   * @param delta error parameter for finding higher symmetry
   * @param choice number for the type of search to do
   */
  public ScalarJ(String dir, float delta, int choice){
    this();

    getParameter(0).setValue(dir);
    getParameter(1).setValue(new Float(delta));
    getParameter(2).setValue(choices.elementAt(choice));
  }

  /**
   * Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters=new Vector();

    if(choices==null || choices.size()==0) init_choices();

    LoadFilePG lfpg=new LoadFilePG("Matrix File",null);
    lfpg.setFilter(new MatrixFilter());
    addParameter(lfpg);
    addParameter(new FloatPG("Delta",0.01f));
    ChoiceListPG clpg=new ChoiceListPG("Search Method",choices.elementAt(0));
    clpg.addItems(choices);
    addParameter(clpg);
  }

  /**
   * This returns the help documentation
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer(80*5);

    // overview
    sb.append("@overview This is a java version of \"SCALAR\" maintained by A.J.Schultz and originally written by R.Goyette. JScalar will read in information from blind.log and write out its results to the console that spawned the process.");
    // parameters
    sb.append("@param String file to get scalars from. If it is \"blind.log\" then the scalars are read directly. This will also calculate the scalars from an orientation matrix found in either a \"matrix\" or \"experiment\" file.");
    sb.append("@param float delta parameter");
    sb.append("@param int restrictions on the resulting symmetry");
    // return
    sb.append("@return tells user to look at the console for results");
    // error
    sb.append("@error anything wrong with the specified directory");
    sb.append("@error when reading \"blind.log\"");
    sb.append("@error delta<=0");
    sb.append("@error restriction value is not understood");
    sb.append("@error initializing variables involved in the calculation");

    return sb.toString();
  }

  /**
   * Returns the command name to be used with script processor, in
   * this case JScalar.
   */
  public String getCommand(){
    return "JScalar";
  }

  /**
   * Uses the current values of the parameters to generate a result.
   */
  public Object getResult(){
    String dir=getParameter(0).getValue().toString();
    delta=(double)((Float)getParameter(1).getValue()).floatValue();
    String Schoice=getParameter(2).getValue().toString();
    int choice=choices.indexOf(Schoice);

    // check that the directory is okay
    if(dir==null || dir.length()==0)
      return new ErrorString("no directory specified");

    // check that delta is reasonable
    if(delta<=0.)
      return new ErrorString("delta must be greater than zero");

    // check that the choice is viable
    if(!parseChoice(choice))
      return new ErrorString("Error undestanding restriction");
    
    // read in the blind logfile
    String blindfile=FilenameUtil.setForwardSlash(dir);//+"/blind.log");
    {
      ErrorString error=readScalars(blindfile);
      if(error!=null) return error;
    }

    // initialize all of the other parameters
    if(!init())
      return new ErrorString("Failed to initialize calculation");

    // do the mystery calculation
    mark01();
    mark02();
    mark03();
    mark04();

    // print the result
    printResult();

    return "See console for result";
  }

  /**
   * Set up a vector for use inside the ChoiceListPG.
   */
  private void init_choices(){
    choices=new Vector(20);
    choices.add("No Restriction");
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
    choices.add("R11 == R22 == R33");
    choices.add("R11 == R22 != R33");
    choices.add("R11 == R33 != R22");
    choices.add("R11 != R22 != R33");
  }

  /**
   * Initialize all of the parameters to test.
   */
  private boolean init(){
    for( i=0 ; i<60 ; i++ ){
      k[i]=0;
      l[i]=0;
    }

    // lattice parameters
    double a = sqrt(scalars[0]);
    double b = sqrt(scalars[1]);
    double c = sqrt(scalars[2]);

    // lattice angles
    double alpha = Math.acos(scalars[3]/(b*c));
    double beta  = Math.acos(scalars[4]/(a*c));
    double gamma = Math.acos(scalars[5]/(a*b));

    sig11 = 2.*delta*scalars[0];
    sig22 = 2.*delta*scalars[1];
    sig33 = 2.*delta*scalars[2];
    sig11sq=sig11*sig11;
    sig22sq=sig22*sig22;
    sig33sq=sig33*sig33;
    sig23sq=2.*(delta*scalars[3]*delta*scalars[3])
                              +Math.pow((b*c*Math.sin(alpha)*0.017),2);
    sig31sq=2.*(delta*scalars[4]*delta*scalars[4])
                              +Math.pow((a*c*Math.sin(beta)*0.017),2);
    sig12sq=2.*(delta*scalars[5]*delta*scalars[5])
                              +Math.pow((a*b*Math.sin(gamma)*0.017),2);
    sig23 = sqrt(sig23sq);
    sig31 = sqrt(sig31sq);
    sig12 = sqrt(sig12sq);

    i = 1;
    k[i-1] = 0;
    l[i-1] = 0;

    System.out.println(Format.real(scalars[0],10,3)+Format.real(sig11,11,3));
    System.out.println(Format.real(scalars[1],10,3)+Format.real(sig22,11,3));
    System.out.println(Format.real(scalars[2],10,3)+Format.real(sig33,11,3));
    System.out.println(Format.real(scalars[3],10,3)+Format.real(sig23,11,3));
    System.out.println(Format.real(scalars[4],10,3)+Format.real(sig31,11,3));
    System.out.println(Format.real(scalars[5],10,3)+Format.real(sig12,11,3));

    return true;
  }

  /**
   * Moves information from the specified file into the scalars, r_ij.
   */
  private ErrorString readScalars(String filename){
    if(filename.endsWith("blind.log"))
       return readBlindLog(filename);

    // determine if this is an experiment file
    boolean isexpfile=!(filename.endsWith(".mat"));
    TextFileReader tfr=null;
    float[][] UB=null;

    // read in the orientation matrix
    try{
      tfr=new TextFileReader(filename);
      {
        Object result=LoadOrientation.readOrient(tfr,isexpfile);
        if(result==null)
          return new ErrorString("Failed to read orientation from "+filename);
        else if(result instanceof ErrorString)
          return (ErrorString)result;
        else if(result instanceof float[][])
          UB=(float[][])result;
        else
          return new ErrorString("Failed to read orientation from "+filename);
        result=null;
      }
    }catch(IOException e){
      return new ErrorString("Error Reading "+filename+": "+e.getMessage());
    }finally{
      if(tfr!=null)
        try{
          tfr.close();
        }catch(IOException e){
          // let it drop on the floor
        }
    }

    // generate the scalars from it
    double[] abc=Util.abc(LinearAlgebra.float2double(UB));
    if(abc==null)
      return new ErrorString("Could not get lattice parameters from UB");
    scalars=Util.scalars(abc);
    if(scalars==null)
      return new ErrorString("Could not calculate scalars from "
                             +"lattice parameters");

    return null;
  }

  /**
   * Reads the log file from blind to get 'scalars'.
   */
  private ErrorString readBlindLog(String logfile){
    TextFileReader tfr=null;

    try{
      tfr=new TextFileReader(logfile);

      System.out.println((" "));
      System.out.println((" Scalars obtained from the blind.log file."));
      System.out.println((" "));

      while(!tfr.eof()){
        if(tfr.read_line().trim().equalsIgnoreCase("*** CELL SCALARS ***"))
          break;
      }
      scalars[0]=tfr.read_double();
      scalars[1]=tfr.read_double();
      scalars[2]=tfr.read_double();
      scalars[3]=tfr.read_double();
      scalars[4]=tfr.read_double();
      scalars[5]=tfr.read_double();
    }catch( IOException e){
      return new ErrorString("Error Reading "+logfile+": "+e.getMessage());
    }catch( NumberFormatException e){
      SharedData.addmsg("NumberFormatException: "+e.getMessage());
      return new ErrorString("NumberFormatException: "+e.getMessage());
    }finally{
      if(tfr!=null)
        try{
          tfr.close();
        }catch(IOException e){
          // let it drop on the floor
        }
    }

    return null;
  }

  /**
   * Read in input from the user when not running as an operator.
   */
  private boolean readUser(){
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
    nchoice = NO_RESTRICTION;
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
        nchoice=NO_RESTRICTION;
      else
        nchoice=Integer.parseInt(ans);
    }catch(NumberFormatException e){
      System.err.println(e);
      return false;
    }

    System.out.println(" ");
    if (nchoice == NO_RESTRICTION){  
      System.out.println("NO RESTRICTIONS");
      this.getParameter(2).setValue(choices.elementAt(0));
      return true;
    }else if (nchoice == HIGHEST_SYMMETRY){
      System.out.println("SEARCHING FOR HIGHEST SYMMETRY MATCH");
      this.getParameter(2).setValue(choices.elementAt(1));
      return true;
    }else if (nchoice == KNOWN_CELL){
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

      if( ncell<=0 || ncell>14)  
        return false;

      System.out.println(" ");
      System.out.println(" SEARCHING FOR CELL TYPE = "+cell[ncell-1]+" ");
      System.out.println(" ");
      this.getParameter(2).setValue(choices.elementAt(ncell+1));
      return true;
    }else if (nchoice == SYMMETRIC){
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
  }

  /**
   * Split out the user's choice when calling the operator into all
   * the necessary values for execution.
   */
  private boolean parseChoice(int choice){
    nchoice = NO_RESTRICTION;
    ncell = 0;
    nequal = 0;

    if(choice==0){ // no restrictions
      nchoice=NO_RESTRICTION;
      return true;
    }else if(choice==1){ // highest symmetry
      nchoice=HIGHEST_SYMMETRY;
      return true;
    }else if( choice>1 && choice<16 ){ // choose symmetry type
      nchoice=KNOWN_CELL;
      ncell=choice-1;
      return true;
    }else if( choice>15 && choice<20 ){ // choice symmetric scalar equalities
      nchoice=SYMMETRIC;
      nequal=choice-15;
      return true;
    }else{
      return false;
    }
  }

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
   * Just calls Math.sqrt but makes code easier to read.
   */
  private static double sqrt(double a){
    return Math.sqrt(a);
  }

  /**
   * Just calls Math.abs but makes code easier to read.
   */
  private static double abs(double a){
    return Math.abs(a);
  }

  /**
   * This runs if the following conditions exist:
   * <UL><LI>looking for symmetry (nchoice=SYMMETRIC)</LI>
   *     <LI>A is same length as B AND A is same length as C (within
   *     allowable limits)</LI>
   *     <LI>looking for symmetry other than |A|=|B|=|C|. This gives
   *     automatic entrance into mark02</LI>
   * </UL>
   */
  private void mark01(){
    // are looking for symmetry
    if( (nchoice == SYMMETRIC)
        // or |A|==|B| && |A|==|C| within uncertainties
          || ( abs(scalars[0]-scalars[1]) < (3.*sqrt(sig11sq+sig22sq)) )
        && ( abs(scalars[0]-scalars[2]) < (3.*sqrt(sig11sq+sig33sq)) ) ){
      if(DEBUG)System.out.println("MARK01");
    }else{
      return;
    }

    // can't look for anything other than |A|==|B|==|C|
    if (nchoice == SYMMETRIC && nequal != A_EQ_B_EQ_C){
      Goto = 40;
      return;
    }

    if( abs(scalars[3])<(3.*sig23) ){ // BdotC=0
      // AdotC=0 AND AdotB=0
      if( abs(scalars[4])<(3.*sig31) && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(1,1);
      }
      // AdotC=-AdotA/2 AND AdotB=-AdotA/2
      if( abs(scalars[4]+(0.5*scalars[0]))<(3.0*sqrt(sig31sq+0.25*sig11sq))
         && abs(scalars[5]+(0.5*scalars[0]))<(3.0*sqrt(sig12sq+0.25*sig11sq))){
        appendMatrix(3,2);
      }
    }

    // BdotC=-AdotA/2
    if( abs(scalars[3]-(-0.5*scalars[0]))<(3.0*sqrt(sig23sq+0.25*sig11sq)) ){
      // AdotC=BdotC AND AdotB=0
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(2,2);
      }
      // AdotB=BdotC AND AdotC=0
      if( abs(scalars[5]-scalars[3])<(3.*sqrt(sig12sq+sig23sq))
          && abs(scalars[4])<(3.*sig31) ){
        appendMatrix(4,2);
      }
      // AdotC=BdotC AND AdotB=BdotC
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq)) 
          && abs(scalars[5]-(-scalars[3]))<(3.*sqrt(sig12sq+sig23sq)) ){
        appendMatrix(5,2);
      }
    }

    // BdotC==AdotA/3 AND AdotC=BdotC AND AdotB=BdotC
    if( abs(scalars[3]-(-scalars[0]/3.))<(3.*sqrt(sig23sq+(sig11sq/9.)))
        && abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
        && abs(scalars[5]-scalars[3]) < (3.*sqrt(sig12sq+sig23sq)) ){
      appendMatrix(8,4);
    }

    // BdotC=(AdotA-AdotC)/2 AND AdotB=BdotC AND AdotC<0
    if( abs(scalars[3]-0.5*(-scalars[0]-scalars[4]))<3.*sqrt(sig23sq+0.25*sig11sq+0.25*sig31sq)
        && abs(scalars[5]-scalars[3])<(3.*sqrt(sig12sq+sig23sq))
        && scalars[4]<0. ){
      appendMatrix(10,5);
    }

    // BdotC=(AdotA-AdotB)/2 AND AdotC=BdotC AND AdotB<0
    if( abs(scalars[3]-0.5*(-scalars[0]-scalars[5]))<3.*sqrt(sig23sq+0.25*sig11sq+0.25*sig12sq)
        && abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq)) && scalars[5]<0.    ){
      appendMatrix(11,5);
    }

    // BdotC=AdotA-AdotC-AdotB AND AdotC<0 AND AdotB<0
    if( abs(scalars[3]-(-scalars[0]-scalars[4]-scalars[5]))<3.*sqrt(sig23sq+sig11sq+sig31sq+sig12sq)
        && scalars[4]<0. && scalars[5]<0.  ){
      appendMatrix(12,6);
    }

    // BdotC<0
    if( scalars[3]<0. ){
      // AdotC=BdotC AND AdotB=-BdotC
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && abs(scalars[5]-(-scalars[3]))<(3.*sqrt(sig12sq+sig23sq))  ){
        appendMatrix(6,3);
      }
      // AdotC=BdotC AND AdotB=BdotC
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && abs(scalars[5]-scalars[3])<(3.*sqrt(sig12sq+sig23sq))  ){
        appendMatrix(7,3);
      }
      // AdotC=-(AdotA+BdotC)/2 AND AdotB=-(AdotA+BdotC)/2
      if( abs(scalars[4]-(0.5*(-scalars[0]-scalars[3])))<3.*sqrt(sig31sq+0.25*sig11sq+0.25*sig23sq)
          && abs(scalars[5]-(0.5*(-scalars[0]-scalars[3])))<3.*sqrt(sig12sq+0.25*sig11sq+0.25*sig23sq)){
        appendMatrix(9,5);
      }
      // AdotC=-(AdotA+BdotC+AdotB) && AdotB<0
      if( abs(scalars[4]-(-scalars[0]-scalars[3]-scalars[5]))<3.*sqrt(sig31sq+sig11sq+sig23sq+sig12sq)
          && scalars[5]<0. ){
        appendMatrix(13,6);
      }
      // AdotB=-(AdotA+BdotC+AdotC) AND AdotC<0
      if( abs(scalars[5]-(-scalars[0]-scalars[3]-scalars[4]))<3.*sqrt(sig12sq+sig11sq+sig23sq+sig31sq)
          && scalars[4]<0. ){
        appendMatrix(14,6);
      }
    }
  }  // ==================== end of mark01

  /**
   * This runs if the following conditions exist:
   * <UL><LI>has a free pass from mark01</LI>
   *     <LI>A is same length as B AND A is not same length as C
   *     (within allowable limits)</LI>
   *     <LI>looking for symmetry other than |A|=|B|!=|C|. This gives
   *     automatic entrance into mark03</LI>
   * </UL>
   */
  private void mark02(){
    // free pass
    if( Goto == 40
        // |A|==|B| AND |A|!=|C|
        || ( abs(scalars[0]-scalars[1])<(3.*sqrt(sig11sq+sig22sq) ) 
             && abs(scalars[0]-scalars[2])>=(3.*sqrt(sig11sq+sig33sq))) ){
      if(DEBUG) System.out.println("MARK02");
    }else{
      return;
    }

    // can't look for anything other than |A|==|B|!=|C|
    Goto = 0;
    if (nchoice == SYMMETRIC && nequal != A_EQ_B_NE_C){
      Goto = 41;
      return;
    }

    // BdotC=0
    if( abs(scalars[3])<(3.*sig23)  ){
      // AdotC=0 AND AdotB=0
      if( abs(scalars[4])<(3.*sig31) && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(15,7);
      }
      // AdotB=AdotA/2 AND AdotC=0
      if( abs(scalars[5]-0.50*scalars[0])<3.*sqrt(sig12sq+0.25*sig11sq) 
          && abs(scalars[4])<(3.*sig31) ){
        appendMatrix(16,8);
      }
      // AdotB=AdotA/2 AND AdotC=0
      if( abs(scalars[5]-0.50*(-scalars[0]))<3.*sqrt(sig12sq+0.25*sig11sq)
          && abs(scalars[4])<(3.*sig31) ){
        appendMatrix(17,8);
      }
      // AdotC=0 AND AdotB>0
      if( abs(scalars[4])<(3.*sig31) && scalars[5]>0. ){
        appendMatrix(18,9);
      }
      // AdotC=0 and AdotB<0
      if( abs(scalars[4])<(3.*sig31) && scalars[5]<0. ){
        appendMatrix(19,9);
      }
    }

    // BdotC=CdotC/2
    if( abs(scalars[3]-(-0.50*scalars[2]))<3.*sqrt(sig23sq+0.25*sig33sq) ){
      // AdotC=BdotC AND AdotB=CdotC/2
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq)) 
          && abs(scalars[5]-(0.25*scalars[2]))<3.*sqrt(sig12sq+0.0625*sig33sq) ){
        appendMatrix(20,5);
      }
      // AdotC=BdotC AND AdotB>0
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && scalars[5]>0. ){
        appendMatrix(21,6);
      }
      // AdotC=BdotC AND AdotB<0
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && scalars[5]<0. ){
        appendMatrix(22,6);
      }
    }

    // BdotC=-CdotC/3 AND AdotC=BdotC AND AdotB=CdotC/6-AdotA/2
    if( abs(scalars[3]+scalars[2]/3.)<3.*sqrt(sig23sq+sig33sq/9.)
        && abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
        && abs(scalars[5]-(-scalars[0]/2.+scalars[2]/6.))<3.*sqrt(sig12sq+sig11sq/4.+sig33sq/36.) ){
      appendMatrix(23,3);
    }

    // BdotC<0
    if( scalars[3]<0. ){
      // AdotC=BdotC
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq)) ){
        // AdotB>0
        if( scalars[5]>0. ){
          appendMatrix(24,10);
        }
        // AdotB<0
        if( scalars[5]<0. ){
          appendMatrix(25,10);
        }
      }
    }
    
  }  // ==================== end of mark02

  /**
   * This runs if the following conditions exist:
   * <UL><LI>has a free pass from mark02</LI>
   *     <LI>A is same length as C AND A is not same length as B
   *     (within allowable limits)</LI>
   *     <LI>looking for symmetry other than |A|=|C|!=|B|. This gives
   *     automatic entrance into mark04</LI>
   * </UL>
   */
  private void mark03(){
    // free pass
    if( Goto == 41 
        // |A|=|C|!=|B|
        || (abs(scalars[0]-scalars[2])<(3.*sqrt(sig11sq+sig33sq))
            && abs(scalars[0]-scalars[1]) >= (3.*sqrt(sig11sq+sig22sq)))  ){
      if(DEBUG) System.out.println("MARK03");
    }else{
      return;
    }

    // can't look for anything other than |A|==|C|!=|B|
    Goto = 0;
    if (nchoice == SYMMETRIC && nequal != A_EQ_C_NE_B){
        Goto = 42;
        return;
    }

    // BdotC=0
    if( abs(scalars[3])<(3.*sig23) ){
      // AdotC=0 AND AdotB=0
      if( abs(scalars[4])<(3.*sig31) && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(26,7);
      }
      // AdotC=-AdotA/2 AND AdotB=0
      if( abs(scalars[4]+scalars[0]/2.)<3.*sqrt(sig31sq+sig11sq/4.)
          && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(27,8);
      }
      // AdotC<0 and AdotB=0
      if( scalars[4]<0 && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(28,9);
      }
    }

    // BdotC=-AdotA/2
    if( abs(scalars[3]-(-0.50*scalars[0]))<3.*sqrt(sig23sq+0.25*sig11sq) ){
      // AdotC=0 AND AdotB=AdotA/2
      if( abs(scalars[4])<(3.*sig31)
          && abs(scalars[5]-scalars[0]/2.)<3.*sqrt(sig12sq+sig11sq/4.) ){
        appendMatrix(29,5);
      }
      // AdotC=0 AND AdotB=-AdotA/2
      if( abs(scalars[4])<(3.*sig31)
          && abs(scalars[5]+scalars[0]/2.)<3.*sqrt(sig12sq+sig11sq/4.) ){
        appendMatrix(30,5);
      }
      // AdotC=BdotC AND AdotB=BdotC
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq)) 
          && abs(scalars[5]+scalars[3])<(3.*sqrt(sig12sq+sig23sq)) ){
        appendMatrix(33,3);
      }
    }

    // BdotC<0
    if( scalars[3]<0. ){
      // AdotC=-AdotA-2*BdotC AND AdotB=BdotC
      if( abs(scalars[4]-(-scalars[0]-2.*scalars[3]))<3.*sqrt(sig31sq+sig11sq+4.*sig23sq)
          && abs(scalars[5]-scalars[3])<(3.*sqrt(sig12sq+sig23sq)) ){
        appendMatrix(34,11);
      }
      // AdotC=-(AdotA+BdotC+AdotB) AND AdotB<0
      if( abs(scalars[4]-(-scalars[0]-scalars[3]-scalars[5]))<3.*sqrt(sig31sq+sig11sq+sig23sq+sig12sq)
          && scalars[5]<0. ){
        appendMatrix(35,10);
      }
      // AdotC<0
      if( scalars[4]<0. ){
        // AdotB=-BdotC
        if( abs(scalars[5]-(-scalars[3]))<(3.*sqrt(sig12sq+sig23sq)) ){
          appendMatrix(31,10);
        }
        // AdotB=BdotC
        if( abs(scalars[5]-scalars[3])<(3.*sqrt(sig12sq+sig23sq)) ){
          appendMatrix(32,10);
        }
      }
    }
  }  // ==================== end of mark03

  /**
   * This runs if the following conditions exist:
   * <UL><LI>has a free pass from mark03</LI>
   *     <LI>A is not same length as B AND A is not same length as C
   *     AND B is not the same length as C (within allowable
   *     limits)</LI>
   *     <LI>looking for symmetry other than |A|!=|C|!=|B|. This gives
   *     automatic entrance into mark05</LI>
   * </UL>
   */
  private void mark04(){
    // free pass
    if( Goto == 42
        // |A|!=|B|!=|C|
        || (abs(scalars[0]-scalars[1])>=(3.*sqrt(sig11sq+sig22sq)) 
            && abs(scalars[0]-scalars[2])>=(3.*sqrt(sig11sq+sig33sq)) 
            && abs(scalars[1]-scalars[2]) >= (3.*sqrt(sig22sq+sig33sq))) ){
      if(DEBUG) System.out.println("MARK04");
    }else{
      return;
    }

    // can't look for anything other than |A|!=|B|!=|C|
    Goto = 0;
    if (nchoice == SYMMETRIC && nequal != A_NE_B_NE_C){
      Goto = 43;
      return;
    }

    // BdotC=0
    if( abs(scalars[3])<(3.*sig23) ){
      // AdotC=0 AND AdotB=0
      if( abs(scalars[4])<(3.*sig31) && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(36,12);
      }
      // AdotC=0 AND AdotB=AdotA/2
      if( abs(scalars[4])<(3.*sig31) 
          && abs(scalars[5]-0.5*scalars[0])<3.*sqrt(sig12sq+0.25*sig11sq) ){
        appendMatrix(37,9);
      }
      // AdotC=0 AND AdotB=-AdotA/2
      if( abs(scalars[4])<(3.*sig31)
          && abs(scalars[5]-0.50*(-scalars[0]))<3.*sqrt(sig12sq+0.25*sig11sq)){
        appendMatrix(38,9);
      }
      // AdotC=0 AND AdotB>0
      if( abs(scalars[4])<(3.*sig31) && scalars[5]>0. ){
        appendMatrix(39,13);
      }
      // AdotC=0 and AdotB<0
      if( abs(scalars[4])<(3.*sig31) && scalars[5]<0. ){
        appendMatrix(40,13);
      }
      // AdotC=-BdotB/2 AND AdotB=0
      if( abs(scalars[4]+scalars[2]/2.)<3.*sqrt(sig31sq+sig33sq/4.)
          && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(41,9);
      }
      // AdotC<0 AND AdotB=0
      if( scalars[4]<0 && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(42,13);
      }
      // AdotC=BdotC AND AdotB>0
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && scalars[5]>0. ){
        appendMatrix(43,10);
      }
      // AdotC=BdotC AND AdotB<0
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && scalars[5]<0. ){
        appendMatrix(44,10);
      }
    }

    // BdotC=CdotC/2
    if( abs(scalars[3]-(0.5*scalars[2]))<3.*sqrt(sig23sq+0.25*sig33sq) ){
      // AdotC=0 AND AdotB=0
      if( abs(scalars[4])<(3.*sig31) && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(45,9);
      }
      // AdotC=0 AND AdotB=-AdotA/2
      if( abs(scalars[4])<(3.*sig31)
          && abs(scalars[5]+scalars[0]/2.)<3.*sqrt(sig12sq+sig11sq/4) ){
        appendMatrix(48,6);
      }
    }

    // BdotC=-CdotC/2
    if( abs(scalars[3]-(-0.50*scalars[2]))<3.*sqrt(sig23sq+0.25*sig33sq) ){
      // AdotC=0 AND AdotB=AdotA/2
      if( abs(scalars[4])<(3.*sig31)
          && abs(scalars[5]-scalars[0]/2.)<3.*sqrt(sig12sq+sig11sq/4.) ){
        appendMatrix(47,6);
      }
      // AdotC=0 AND AdotB>0
      if( abs(scalars[4])<(3.*sig31) && scalars[5] > 0. ){
        appendMatrix(49,10);
      }
      // AdotC=0 AND AdotB<0
      if( abs(scalars[4])<(3.*sig31) && scalars[5]<0. ){
        appendMatrix(50,10);
      }
      // AdotC=BdotC AND AdotB=CtodC/4
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && abs(scalars[5]-(0.25*scalars[2]))<3.*sqrt(sig12sq+0.25*sig33sq) ){
        appendMatrix(53,11);
      }
      // AdotC=BdotC AND AdotB>0
      if( abs(scalars[4]-scalars[3])<(3.*sqrt(sig31sq+sig23sq))
          && scalars[5]>0. ){
        appendMatrix(54,10);
      }
      // AdotC<0 AND AdotB=-AdotC/2
      if( scalars[4]<0.
          && abs(scalars[5]-(-0.5*scalars[4]))<3.*sqrt(sig12sq+0.25*sig31sq) ){
        appendMatrix(55,10);
      }
    }

    // BdotC=AdotC/2 AND AdotC<0 AND AdotB=AdotA/2
    if( abs(scalars[3]-(0.50*scalars[4]))<3.*sqrt(sig23sq+0.25*sig31sq)
        && scalars[4]<0.
        && abs(scalars[5]-0.5*scalars[0]) < 3.*sqrt(sig12sq+0.25*sig11sq) ){
      appendMatrix(56,10);
    }

    // BdotC=-BdotB-AdotC/2 AND AdotC<0 AND AdotB=-(AdotA+AdotC)/2
    if( abs(scalars[3]+(scalars[2]+scalars[4])/2.)<3.*sqrt(sig23sq+sig33sq/4.+sig31sq/4.) && scalars[4]<0.
        && abs(scalars[5]-(0.5*(-scalars[0]-scalars[4])))<3.*sqrt(sig12sq+sig11sq/4.+sig31sq/4.) ){
      appendMatrix(58,10);
    }

    // BdotC<0
    if( scalars[3]< 0. ){
      // AdotC=0 AND AdotB=0
      if( abs(scalars[4])<(3.*sig31) && abs(scalars[5])<(3.*sig12) ){
        appendMatrix(46,13);
      }
      // AdotB=AdotA/2 AND AdotC=0
      if( abs(scalars[5]-scalars[0]/2.)<3.*sqrt(sig12sq+sig11sq/4.)
          && abs(scalars[4])<(3.*sig31) ){
        appendMatrix(51,10);
      }
      // AdotB=-AdotA/2 AND AdotC=0
      if( abs(scalars[5]+scalars[0]/2.)<3.*sqrt(sig12sq+sig11sq/4.)
          && abs(scalars[4])<(3.*sig31) ){
        appendMatrix(52,10);
      }
      // AdotC=-CdotC/2 AND AdotB=-BdotC/2
      if( abs(scalars[4]+0.5*scalars[2])<3.0*sqrt(sig31sq+sig33sq/4.)
          && abs(scalars[5]+0.5*scalars[3])<3.*sqrt(sig12sq+sig23sq/4.) ){
        appendMatrix(57,10);
      }
      // AdotC<0
      if( scalars[4]<0. ){
        // AdotB>0
        if( scalars[5]>0. ){
          appendMatrix(59,14);
        }
        // AdotB<0
        if( scalars[5]<0. ){
          appendMatrix(60,14);
        }
      }
    }
  }  // ==================== end of mark04

  /**
   * Prints the results of the search to STDOUT.
   */
  private void printResult(){
    if(DEBUG) System.out.println("MARK05");
    if( k[0] == 0 || l[0] == 0 ){
      System.out.println("NO MATCH");
      System.out.println(" FOR SCALARS "
                         +scalars[0]+" "+scalars[1]+" "+scalars[2]+" ");
      System.out.println("             "
                         +scalars[3]+" "+scalars[4]+" "+scalars[5]+" ");
      return;
    }

    i = i-1;
    int minsym = 20;
    for( int j=0 ; j<i ; j++ ){
      minsym = (int)(Math.min(minsym, l[j]) );
    }

    if (nchoice == HIGHEST_SYMMETRY)  
      npick = minsym;
    if (nchoice == KNOWN_CELL)  
      npick = ncell;
    boolean nflag = false;
    int imax = i;
    i = 1;


    for( int n=0 ; n<imax ; n++ ){
      if( l[n]==npick || nchoice==NO_RESTRICTION || nchoice==SYMMETRIC ){
        System.out.println(" ");
        System.out.println(" CELL "+(n+1)+"  TYPE IS ");
        System.out.println(" ");
        System.out.println(" "+cell[l[n]-1]+" ");
        System.out.println(" ");
        System.out.println(" TRANSFORMATION MATRIX "+i+"  IS" );
        System.out.println(" ");
        i++;
        nflag = true;
        for( int m=1 ; m<=3 ; m++ ){
          System.out.print("");
          for( int j=1 ; j<=3 ; j++ )
            System.out.print(Format.real(trans[m+((j+3*k[n])*3)-13],4)+" ");

          System.out.println();
        }
      }
    }

    if(!nflag)
      System.out.println((" NO MATCHES FOUND"));
    System.out.println((" "));


  }  // ==================== end of printResult

  /**
   * Put a new set of indices for a transformation matrix into the
   * list
   */
  private void appendMatrix(int k, int l){
    this.k[this.i-1]=k;
    this.l[this.i-1]=l;
    this.i=this.i+1;
  }

  /**
   * Main method for testing purposes and running outside of ISAW.
   */
  public static void main (String [] args)  {

    ScalarJ scal=new ScalarJ();
    scal.getParameter(0).setValue(System.getProperty("user.dir"));

    if(!scal.readUser())
      System.exit(-1);

    Object obj=scal.getResult();
    System.out.println("RESULT:"+obj);
    if(obj instanceof ErrorString)
      System.exit(-1);
    else
      System.exit(0);
  }
}
