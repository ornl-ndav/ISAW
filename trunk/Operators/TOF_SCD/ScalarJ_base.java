/*
 * File:  ScalarJ_base.java.java   
 *
 * Copyright (C) 2004, Ruth MIkkelson, Peter F. Peterson
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
 * Revision 1.3  2004/07/31 23:11:20  rmikk
 * Removed unused imports
 *
 * Revision 1.2  2004/07/29 14:02:12  rmikk
 * Fixed javadoc error
 *
 * Revision 1.1  2004/07/14 16:28:19  rmikk
 * Initial Checkin
 * ScalarJ with orientation matrix input and Transformation output
 *
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

//import DataSetTools.operator.DataSet.Attribute.LoadOrientation;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.parameter.*;
//import DataSetTools.util.FilenameUtil;
import DataSetTools.util.SharedData;
import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.Numeric.Format;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Class designed to analyze cell scalars from BLIND to determine Laue
 * symmetry. Originally written by R. Goyette.
 */
public class ScalarJ_base extends GenericTOF_SCD{
  private static final boolean DEBUG=false;

  // valid values of nchoice (generic symmetry type)
  private static final int NO_RESTRICTION   = 0;
  private static final int HIGHEST_SYMMETRY = 1;
  private static final int KNOWN_CELL       = 2;
  private static final int SYMMETRIC        = 3;

  // valid values of nequal (specify family of valid cell types)
  private static final int A_EQ_B_EQ_C = 1;
  private static final int A_EQ_B_NE_C = 2;
  private static final int A_EQ_C_NE_B = 3;
  private static final int A_NE_B_NE_C = 4;

  private StringBuffer logBuffer=null;
  private double delta= 0.0;
  private int i= 0;
  private int [] k= new int[60];
  private int [] l= new int[60];
  private double[] scalars = null;
  private double[] sig     = new double[6];
  private double[] sigsq   = new double[6];
  private double[][] UB    = new double[3][3];
  private int nequal= 0;
  private int nchoice= NO_RESTRICTION;
  private int ncell= 0;
  private boolean freePass=false;
  private int npick= 0;

  private double[][] newmat=null;
  double[][] transf=null;
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
  public ScalarJ_base(){
    super("JScalar");
    this.logBuffer=new StringBuffer();
  }

  /**
   * Constructs a fully configured scalar operator
   *
   * @param UB the name of the blind.log, .matrix, or .x file
   * @param delta error parameter for finding higher symmetry
   * @param choice number for the type of search to do
   */
  public ScalarJ_base(float[][]UB, float delta, int choice){
    this();

    getParameter(0).setValue(UB);
    getParameter(1).setValue(new Float(delta));
    getParameter(2).setValue(choices.elementAt(choice));
  }

  /**
   * Set the parameters to default values.
   */
  public void setDefaultParameters(){
    parameters=new Vector();

    if(choices==null || choices.size()==0) init_choices();

    addParameter( new ArrayPG( "UB matrix", new float[0][0]));
    addParameter(new FloatPG("Delta",0.01f));
    ChoiceListPG clpg=new ChoiceListPG("Symmetry Constraints", 
                                       choices.elementAt(0));
    clpg.addItems(choices);
    addParameter(clpg);
  }

  /**
   * This returns the help documentation
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer(80*5);

    // overview
    sb.append("@overview This is a java version of \"SCALAR\" " );
    sb.append( "maintained by A.J.Schultz and originally written by " );
    sb.append( "R.Goyette. JScalar will read in information from " );
    sb.append( "blind.log and write out its results to the console that " );
    sb.append( "spawned the process.");
    // parameters
    sb.append("@param UB matrix");
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
    Vector V =((ArrayPG)getParameter(0)).getVectorValue();
    float[][] UB1= new float[3][3];
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
            UB1[i][j]= ((Number)(V1.elementAt(j))).floatValue();
       }catch(Exception s){
          return new ErrorString("Improper Orientation matrix ");
       }
          
   
    }
    String logfile = null;
    delta=(double)((Float)getParameter(1).getValue()).floatValue();
    String Schoice=getParameter(2).getValue().toString();
    int choice=choices.indexOf(Schoice);

    // add information to the log
    //logBuffer.append("FILE   = "+matfile+"\n");
    logBuffer.append("DELTA  = "+Format.real(delta,5,3)+"\n");
    logBuffer.append("CHOICE = "+Schoice+"\n\n");

    // check that the matrix file is okay
   
    // check that delta is reasonable
    if(delta<=0.)
      return new ErrorString("delta must be greater than zero");

    // check that the choice is viable
    if(!parseChoice(choice))
      return new ErrorString("Error undestanding restriction");
    
    // read in the blind logfile
   
    {
      ErrorString error=readScalars(UB1);
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

    //return the logfile name and print a message
    SharedData.addmsg("See Status pane for result");
    if( transf != null)
      return gov.anl.ipns.Util.Sys.StringUtil.toString(LinearAlgebra.getTranspose(
                               LinearAlgebra.double2float(transf)));
    else
      return "[[1,0,0],[0,1,0],[1,0,0]]";
  }

  /**
   * Writes the contents of logBuffer to the specified filename.
   */
  private boolean writeLog( String filename ){
    FileOutputStream fout=null;
    try{
      fout=new FileOutputStream(filename);
      fout.write(logBuffer.toString().getBytes());
      fout.flush();
    }catch(IOException e){
      return false;
    }finally{
      if(fout!=null){
        try{
          fout.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }

    return true;
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

    {
      int start=logBuffer.length();
      logBuffer.append(" LATTICE PARAMETERS\n");
      logBuffer.append(Format.real(a,10,3)+Format.real(b,10,3)+Format.real(c,10,3)+"\n");
      logBuffer.append(Format.real(alpha*180./Math.PI,10,3)+Format.real(beta*180./Math.PI,10,3)+Format.real(gamma*180./Math.PI,10,3)+"\n");
      logBuffer.append("\n");
      System.out.print(logBuffer.substring(start));
    }

    sig[0] = 2.*delta*scalars[0];
    sig[1] = 2.*delta*scalars[1];
    sig[2] = 2.*delta*scalars[2];
    for( int index=0 ; index<3 ; index++ )
      sigsq[index]=sig[index]*sig[index];
    sigsq[3]=2.*(delta*scalars[3]*delta*scalars[3])
                              +Math.pow((b*c*Math.sin(alpha)*0.017),2);
    sigsq[4]=2.*(delta*scalars[4]*delta*scalars[4])
                              +Math.pow((a*c*Math.sin(beta)*0.017),2);
    sigsq[5]=2.*(delta*scalars[5]*delta*scalars[5])
                              +Math.pow((a*b*Math.sin(gamma)*0.017),2);
    for( int index=3 ; index<6 ; index++ )
      sig[index]=sqrt(sigsq[index]);

    i = 1;
    k[i-1] = 0;
    l[i-1] = 0;

    {
      int start=logBuffer.length();
      logBuffer.append(" SCALARS");
      for( int index=0 ; index<6 ; index++ ){
        logBuffer.append(Format.real(scalars[index],10,3)
                         +Format.real(sig[index],11,3)+"\n");
      }
      System.out.print(logBuffer.substring(start));
    }

    return true;
  }

  /**
   * Moves information from the specified file into the scalars, r_ij.
   */
  private ErrorString readScalars(float[][] myUB){
    
    this.UB=LinearAlgebra.float2double(myUB);
    // generate the scalars from it
    double[] abc=Util.abc(this.UB);
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

      {
        int start=logBuffer.length();
        logBuffer.append("\n Scalars obtained from the blind.log file.\n\n");
        System.out.print(logBuffer.substring(start));
      }

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
   * Compares two scalars using indices to see if they are equal
   * within uncertainties. Specify b=-1 to compare with zero.
   */
  private boolean compare(int a, int b){
    if(b>0)
      return compare(scalars[a],scalars[b],sigsq[a],sigsq[b]);
    else
      return (abs(scalars[a])<(3.*sig[a]));
  }

  /**
   * Compares two numbers to see if they are equal within uncertainties
   */
  private boolean compare(double a, double b, double sqDa, double sqDb){
    return (abs(a-b)<(3.*sqrt(sqDa+sqDb)));
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
    // are looking for symmetry OR |A|==|B|==|C|
    if( (nchoice == SYMMETRIC) || ( compare(0,1) && compare(0,2) ) ){
      if(DEBUG)System.out.println("MARK01");
    }else{
      return;
    }

    // can't look for anything other than |A|==|B|==|C|
    if (nchoice == SYMMETRIC && nequal != A_EQ_B_EQ_C){
      freePass=true;
      return;
    }

    // BdotC=0
    if( compare(3,-1) ){
      // AdotC=0 AND AdotB=0
      if( compare(4,-1) && compare(5,-1) )
        appendMatrix(1,1);
      // AdotC=-AdotA/2 AND AdotB=-AdotA/2
      if( compare(scalars[4],-.5*scalars[0],sigsq[4],0.25*sigsq[0])
         && compare(scalars[5],-.5*scalars[0],sigsq[5],0.25*sigsq[0]) )
        appendMatrix(3,2);
    }

    // BdotC=-AdotA/2
    if( compare(scalars[3],-.5*scalars[0],sigsq[3],0.25*sigsq[0]) ){
      // AdotC=BdotC AND AdotB=0
      if( compare(4,3) && compare(5,-1) )
        appendMatrix(2,2);
      // AdotB=BdotC AND AdotC=0
      if( compare(5,3) && compare(4,-1) )
        appendMatrix(4,2);
      // AdotC=BdotC AND AdotB=BdotC
      if( compare(4,3) && compare(5,3) )
        appendMatrix(5,2);
    }

    // BdotC==AdotA/3 AND AdotC=BdotC AND AdotB=BdotC
    if( compare(scalars[3],-scalars[0]/3.,sigsq[3],sigsq[0]/9.)
        && compare(4,3) && compare(5,3) ){
      appendMatrix(8,4);
    }

    // BdotC=(-AdotA-AdotC)/2 AND AdotB=BdotC AND AdotC<0
    if( compare(scalars[3],0.5*(-scalars[0]-scalars[4]),sigsq[3],(sigsq[0]+sigsq[4])/4.)
        && compare(5,3) && scalars[4]<0. ){
      appendMatrix(10,5);
    }

    // BdotC=(AdotA-AdotB)/2 AND AdotC=BdotC AND AdotB<0
    if( compare(scalars[3],-.5*(scalars[0]+scalars[5]),sigsq[3],(sigsq[0]+sigsq[5])/4.)
        && compare(4,3) && scalars[5]<0. ){
      appendMatrix(11,5);
    }

    // BdotC=AdotA-AdotC-AdotB AND AdotC<0 AND AdotB<0
    if( compare(scalars[3],-(scalars[0]+scalars[4]+scalars[5]),sigsq[3],sigsq[0]+sigsq[4]+sigsq[5])
        && scalars[4]<0. && scalars[5]<0.  ){
      appendMatrix(12,6);
    }

    // BdotC<0
    if( scalars[3]<0. ){
      // AdotC=BdotC AND AdotB=-BdotC
      if( compare(4,3) && compare(scalars[5],-scalars[3],sigsq[5],sigsq[3]) )
        appendMatrix(6,3);
      // AdotC=BdotC AND AdotB=BdotC
      if( compare(4,3) && compare(5,3) )
        appendMatrix(7,3);
      // AdotC=-(AdotA+BdotC)/2 AND AdotB=-(AdotA+BdotC)/2
      if( compare(scalars[4],-.5*(scalars[0]+scalars[3]),sigsq[4],(sigsq[0]+sigsq[3])/4.)
          && compare(scalars[5],-.5*(scalars[0]+scalars[3]),sigsq[5],(sigsq[0]+sigsq[3])/4.) )
        appendMatrix(9,5);
      // AdotC=-(AdotA+BdotC+AdotB) && AdotB<0
      if( compare(scalars[4],-(scalars[0]+scalars[3]+scalars[5]),sigsq[4],sigsq[0]+sigsq[3]+sigsq[5])
          && scalars[5]<0. )
        appendMatrix(13,6);
      // AdotB=-(AdotA+BdotC+AdotC) AND AdotC<0
      if( compare(scalars[5],-scalars[0]-scalars[3]-scalars[4],sigsq[5],sigsq[0]+sigsq[3]+sigsq[4])
          && scalars[4]<0. )
        appendMatrix(14,6);
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
    // free pass OR |A|==|B| AND |A|!=|C|
    if( freePass || ( compare(0,1) && !compare(0,2) ) ){
      if(DEBUG) System.out.println("MARK02");
    }else{
      return;
    }

    // can't look for anything other than |A|==|B|!=|C|
    freePass=false;
    if (nchoice == SYMMETRIC && nequal != A_EQ_B_NE_C){
      freePass=true;
      return;
    }

    // BdotC=0
    if( compare(3,-1) ){
      // AdotC=0 AND AdotB=0
      if( compare(4,-1) && compare(5,-1) )
        appendMatrix(15,7);
      // AdotB=AdotA/2 AND AdotC=0
      if( compare(scalars[5],.5*scalars[0],sigsq[5],0.25*sigsq[0])
          && compare(4,-1) )
        appendMatrix(16,8);
      // AdotB=-AdotA/2 AND AdotC=0
      if( compare(scalars[5],-.5*scalars[0],sigsq[5],0.25*sigsq[0])
          && compare(4,-1) )
        appendMatrix(17,8);
      // AdotC=0 AND AdotB>0
      if( compare(4,-1) && scalars[5]>0. )
        appendMatrix(18,9);
      // AdotC=0 and AdotB<0
      if( compare(4,-1) && scalars[5]<0. )
        appendMatrix(19,9);
    }

    // BdotC=CdotC/2
    if( compare(scalars[3],-.5*scalars[2],sigsq[3],0.25*sigsq[2]) ){
      // AdotC=BdotC AND AdotB=CdotC/2
      if( compare(4,3)
          && compare(scalars[5],0.25*scalars[2],sigsq[5],0.0625*sigsq[2]) )
        appendMatrix(20,5);
      // AdotC=BdotC AND AdotB>0
      if( compare(4,3) && scalars[5]>0. )
        appendMatrix(21,6);
      // AdotC=BdotC AND AdotB<0
      if( compare(4,3) && scalars[5]<0. )
        appendMatrix(22,6);
    }

    // BdotC=-CdotC/3 AND AdotC=BdotC AND AdotB=CdotC/6-AdotA/2
    if( compare(scalars[3],-scalars[2]/3.,sigsq[3],sigsq[2]/9.) && compare(4,3)
        && compare(scalars[5],-scalars[0]/2.+scalars[2]/6.,sigsq[5],sigsq[0]/4.+sigsq[2]/36.) ){
      appendMatrix(23,3);
    }

    // BdotC<0
    if( scalars[3]<0. ){
      // AdotC=BdotC
      if( compare(4,3) ){
        // AdotB>0
        if( scalars[5]>0. )
          appendMatrix(24,10);
        // AdotB<0
        if( scalars[5]<0. )
          appendMatrix(25,10);
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
    // free pass OR  |A|=|C|!=|B|
    if( freePass || ( compare(0,2) && !compare(0,1) ) ){
      if(DEBUG) System.out.println("MARK03");
    }else{
      return;
    }

    // can't look for anything other than |A|==|C|!=|B|
    freePass=false;
    if (nchoice == SYMMETRIC && nequal != A_EQ_C_NE_B){
        freePass=true;
        return;
    }

    // BdotC=0
    if( compare(3,-1) ){
      // AdotC=0 AND AdotB=0
      if( compare(4,-1) && compare(5,-1) )
        appendMatrix(26,7);
      // AdotC=-AdotA/2 AND AdotB=0
      if( compare(scalars[4],-scalars[0]/2.,sigsq[4],sigsq[0]/4.)
          && compare(5,-1) )
        appendMatrix(27,8);
      // AdotC<0 and AdotB=0
      if( scalars[4]<0 && compare(5,-1) )
        appendMatrix(28,9);
    }

    // BdotC=-AdotA/2
    if( compare(scalars[3],-0.50*scalars[0],sigsq[3],0.25*sigsq[0]) ){
      // AdotC=0 AND AdotB=AdotA/2
      if( compare(4,-1)
          && compare(scalars[5],scalars[0]/2.,sigsq[5],sigsq[0]/4.) )
        appendMatrix(29,5);
      // AdotC=0 AND AdotB=-AdotA/2
      if( compare(4,-1)
          && compare(scalars[5],-scalars[0]/2.,sigsq[5],sigsq[0]/4.) )
        appendMatrix(30,5);
      // AdotC=BdotC AND AdotB=-BdotC
      if( compare(4,3)
          && compare(scalars[5],-scalars[3],sigsq[5],sigsq[3]) )
        appendMatrix(33,3);
    }

    // BdotC<0
    if( scalars[3]<0. ){
      // AdotC=-AdotA-2*BdotC AND AdotB=BdotC
      if( compare(scalars[4],-scalars[0]-2.*scalars[3],sigsq[4],sigsq[0]+4.*sigsq[3])
          && compare(5,3) )
        appendMatrix(34,11);
      // AdotC=-(AdotA+BdotC+AdotB) AND AdotB<0
      if( compare(scalars[4],-scalars[0]-scalars[3]-scalars[5],sigsq[4],sigsq[0]+sigsq[3]+sigsq[5])
          && scalars[5]<0. )
        appendMatrix(35,10);
      // AdotC<0
      if( scalars[4]<0. ){
        // AdotB=-BdotC
        if( compare(scalars[5],-scalars[3],sigsq[5],sigsq[3]) )
          appendMatrix(31,10);
        // AdotB=BdotC
        if( compare(5,3) )
          appendMatrix(32,10);
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
    // free pass OR |A|!=|B|!=|C|
    if( freePass || !compare(0,1) && !compare(0,2) && !compare(1,2) ){
      if(DEBUG) System.out.println("MARK04");
    }else{
      return;
    }

    // can't look for anything other than |A|!=|B|!=|C|
    freePass=false;
    if (nchoice == SYMMETRIC && nequal != A_NE_B_NE_C){
      freePass=true;
      return;
    }

    // BdotC=0
    if( abs(scalars[3])<(3.*sig[3]) ){
      // AdotC=0 AND AdotB=0
      if( compare(4,-1) && compare(5,-1) )
        appendMatrix(36,12);
      // AdotC=0 AND AdotB=AdotA/2
      if( compare(4,-1)
          && compare(scalars[5],0.5*scalars[0],sigsq[5],0.25*sigsq[0]) )
        appendMatrix(37,9);
      // AdotC=0 AND AdotB=-AdotA/2
      if( compare(4,-1)
          && compare(scalars[5],-.5*scalars[0],sigsq[5],0.25*sigsq[0]) )
        appendMatrix(38,9);
      // AdotC=0 AND AdotB>0
      if( compare(4,-1) && scalars[5]>0. )
        appendMatrix(39,13);
      // AdotC=0 and AdotB<0
      if( compare(4,-1) && scalars[5]<0. )
        appendMatrix(40,13);
      // AdotC=-BdotB/2 AND AdotB=0
      if( compare(scalars[4],-scalars[2]/2.,sigsq[4],sigsq[2]/4.)
          && compare(5,-1) )
        appendMatrix(41,9);
      // AdotC<0 AND AdotB=0
      if( scalars[4]<0 && compare(5,-1) )
        appendMatrix(42,13);
      // AdotC=BdotC AND AdotB>0
      if( compare(4,3) && scalars[5]>0. )
        appendMatrix(43,10);
      // AdotC=BdotC AND AdotB<0
      if( compare(4,3) && scalars[5]<0. )
        appendMatrix(44,10);
    }

    // BdotC=CdotC/2
    if( compare(scalars[3],.5*scalars[2],sigsq[3],0.25*sigsq[2]) ){
      // AdotC=0 AND AdotB=0
      if( compare(4,-1) && compare(5,-1) )
        appendMatrix(45,9);
      // AdotC=0 AND AdotB=-AdotA/2
      if( compare(4,-1)
          && compare(scalars[5],-scalars[0]/2.,sigsq[5],sigsq[0]/4) )
        appendMatrix(48,6);
    }

    // BdotC=-CdotC/2
    if( compare(scalars[3],-0.50*scalars[2],sigsq[3],0.25*sigsq[2]) ){
      // AdotC=0 AND AdotB=AdotA/2
      if( compare(4,-1)
          && compare(scalars[5],scalars[0]/2.,sigsq[5],sigsq[0]/4.) )
        appendMatrix(47,6);
      // AdotC=0 AND AdotB>0
      if( compare(4,-1) && scalars[5] > 0. )
        appendMatrix(49,10);
      // AdotC=0 AND AdotB<0
      if( compare(4,-1) && scalars[5]<0. )
        appendMatrix(50,10);
      // AdotC=BdotC AND AdotB=CtodC/4
      if( compare(4,3)
          && compare(scalars[5],0.25*scalars[2],sigsq[5],0.25*sigsq[2]) )
        appendMatrix(53,11);
      // AdotC=BdotC AND AdotB>0
      if( compare(4,3) && scalars[5]>0. )
        appendMatrix(54,10);
      // AdotC<0 AND AdotB=-AdotC/2
      if( scalars[4]<0.
          && compare(scalars[5],-.5*scalars[4],sigsq[5],0.25*sigsq[4]) )
        appendMatrix(55,10);
    }

    // BdotC=AdotC/2 AND AdotC<0 AND AdotB=AdotA/2
    if( compare(scalars[3],.5*scalars[4],sigsq[3],0.25*sigsq[4])
        && scalars[4]<0.
        && compare(scalars[5],.5*scalars[0],sigsq[5],0.25*sigsq[0]) ){
      appendMatrix(56,10);
    }

    // BdotC=-BdotB-AdotC/2 AND AdotC<0 AND AdotB=-(AdotA+AdotC)/2
    if( compare(scalars[3],-(scalars[2]+scalars[4])/2.,sigsq[3],(sigsq[2]+sigsq[4])/4.) && scalars[4]<0.
        && compare(scalars[5],-.5*(scalars[0]+scalars[4]),sigsq[5],(sigsq[0]+sigsq[4])/4.) ){
      appendMatrix(58,10);
    }

    // BdotC<0
    if( scalars[3]< 0. ){
      // AdotC=0 AND AdotB=0
      if( compare(4,-1) && compare(5,-1) )
        appendMatrix(46,13);
      // AdotB=AdotA/2 AND AdotC=0
      if( compare(scalars[5],scalars[0]/2.,sigsq[5],sigsq[0]/4.)
          && compare(4,-1) )
        appendMatrix(51,10);
      // AdotB=-AdotA/2 AND AdotC=0
      if( compare(scalars[5],-scalars[0]/2.,sigsq[5],sigsq[0]/4.)
          && compare(4,-1) )
        appendMatrix(52,10);
      // AdotC=-CdotC/2 AND AdotB=-BdotC/2
      if( compare(scalars[4],-.5*scalars[2],sigsq[4],sigsq[2]/4.)
          && compare(scalars[5],-.5*scalars[3],sigsq[5],sigsq[3]/4.) )
        appendMatrix(57,10);
      // AdotC<0
      if( scalars[4]<0. ){
        // AdotB>0
        if( scalars[5]>0. )
          appendMatrix(59,14);
        // AdotB<0
        if( scalars[5]<0. )
          appendMatrix(60,14);
      }
    }
  }  // ==================== end of mark04

  /**
   * Prints the results of the search to STDOUT.
   */
  private void printResult(){
    if(DEBUG) System.out.println("MARK05");
    if( k[0] == 0 || l[0] == 0 ){
      int start=logBuffer.length();
      logBuffer.append("NO MATCH\n");
      logBuffer.append(" FOR SCALARS "
                         +scalars[0]+" "+scalars[1]+" "+scalars[2]+"\n");
      logBuffer.append("             "
                         +scalars[3]+" "+scalars[4]+" "+scalars[5]+"\n");
      SharedData.addmsg(logBuffer.substring(start));
      newmat = null;
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


    transf=new double[3][3];
    double[] abc=null;
    for( int n=0 ; n<imax ; n++ ){
      if( l[n]==npick || nchoice==NO_RESTRICTION || nchoice==SYMMETRIC ){
        int start=logBuffer.length();
        logBuffer.append("\n CELL "+(n+1)+"  TYPE IS \n\n");
        logBuffer.append(" "+cell[l[n]-1]+"\n\n");
        logBuffer.append(" TRANSFORMATION MATRIX "+i+"  IS\n\n" );
        i++;
        nflag = true;
        for( int m=1 ; m<=3 ; m++ ){ // create transformation matrix
          for( int j=1 ; j<=3 ; j++ )
            transf[j-1][m-1]=trans[m+((j+3*k[n])*3)-13];
        }
        newmat=LinearAlgebra.mult(this.UB,transf);
        for( int ii=0 ; ii<3 ; ii++ ){ // print the matrices
          for( int jj=0 ; jj<3 ; jj++ ){
            logBuffer.append(Format.real(transf[jj][ii],4)+" ");
          }
          if(ii==1)
            logBuffer.append(" x UB = ");
          else
            logBuffer.append("        ");
          for( int jj=0 ; jj<3 ; jj++ ){
            logBuffer.append(Format.real(newmat[jj][ii],10,6)+" ");
          }
          logBuffer.append("\n");
        }
        abc=Util.abc(LinearAlgebra.mult(this.UB,transf));
        logBuffer.append("\n");
        logBuffer.append("      a          b          c        alpha       "
                           +"beta      gamma     cellvol\n");
        for( int ii=0 ; ii<abc.length ; ii++ )
          logBuffer.append(Format.real(abc[ii],10,3)+" ");
        logBuffer.append("\n");
        SharedData.addmsg(logBuffer.substring(start));
      }
    }

    int start=logBuffer.length();
    if(!nflag)
      logBuffer.append(" NO MATCHES FOUND");
    logBuffer.append("\n");
    SharedData.addmsg(logBuffer.substring(start));

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

    ScalarJ_base scal=new ScalarJ_base();
    scal.getParameter(0).setValue(SharedData.getProperty("user.dir"));

    if(!scal.readUser())
      System.exit(-1);

    Object obj=scal.getResult();
    System.out.println("RESULT:"+obj);
    if(obj instanceof ErrorString)
      System.exit(-1);
    else
      System.exit(0);
  }
  public Object clone(){
    ScalarJ_base Res = new ScalarJ_base();
    Res.CopyParametersFrom(this);
    return Res;
  }
}
