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

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.parameter.*;
import DataSetTools.util.ErrorString;
import DataSetTools.util.FilenameUtil;
import DataSetTools.util.TextFileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * Class designed to analyze cell scalars from BLIND to determine Laue
 * symmetry. Originally written by R. Goyette.
 */
public class ScalarJ extends GenericTOF_SCD{
  private static final boolean DEBUG=false;
  private double delta= 0.0;
  private int i= 0;
  private int [] k= new int[60];
  private int [] l= new int[60];
  private double r31= 0.0;
  private double r12= 0.0;
  private double r11= 0.0;
  private double r22= 0.0;
  private double r33= 0.0;
  private double r23= 0.0;
  private double sig31= 0.0;
  private double sig23= 0.0;
  private double sig33= 0.0;
  private double sig22= 0.0;
  private double sig11= 0.0;
  private double sig12= 0.0;
  private double sig31sq= 0.0;
  private double sig23sq= 0.0;
  private double sig33sq= 0.0;
  private double sig22sq= 0.0;
  private double sig11sq= 0.0;
  private double sig12sq= 0.0;
  private int nequal= 0;
  private int nchoice= 0;
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

    addParameter(new DataDirPG("Working Director",null));
    addParameter(new FloatPG("Delta",0.01f));
    ChoiceListPG clpg=new ChoiceListPG("SearchMethod",choices.elementAt(0));
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
    sb.append("@param String directory to find the file \"blind.log\"");
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
    String blindfile=FilenameUtil.setForwardSlash(dir+"/blind.log");
    if(!readBlindLog(blindfile))
      return new ErrorString("Error Reading "+blindfile);

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
    double a = sqrt(r11);
    double b = sqrt(r22);
    double c = sqrt(r33);

    // lattice angles
    double alpha = Math.acos(r23/(b*c));
    double beta  = Math.acos(r31/(a*c));
    double gamma = Math.acos(r12/(a*b));

    sig11 = 2.*delta*r11;
    sig22 = 2.*delta*r22;
    sig33 = 2.*delta*r33;
    sig11sq=sig11*sig11;
    sig22sq=sig22*sig22;
    sig33sq=sig33*sig33;
    sig23sq=2.*(delta*r23*delta*r23)+Math.pow((b*c*Math.sin(alpha)*0.017),2);
    sig31sq=2.*(delta*r31*delta*r31)+Math.pow((a*c*Math.sin(beta)*0.017),2);
    sig12sq=2.*(delta*r12*delta*r12)+Math.pow((a*b*Math.sin(gamma)*0.017),2);
    sig23 = sqrt(sig23sq);
    sig31 = sqrt(sig31sq);
    sig12 = sqrt(sig12sq);

    i = 1;
    k[i-1] = 0;
    l[i-1] = 0;

    System.out.println(r11+" "+sig11+" ");
    System.out.println(r22+" "+sig22+" ");
    System.out.println(r33+" "+sig33+" ");
    System.out.println(r23+" "+sig23+" ");
    System.out.println(r31+" "+sig31+" ");
    System.out.println(r12+" "+sig12+" ");

    return true;
  }

  /**
   * Reads the log file from blind to get 'scalars'.
   */
  private boolean readBlindLog(String logfile){
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
      r11=tfr.read_double();
      r22=tfr.read_double();
      r33=tfr.read_double();
      r23=tfr.read_double();
      r31=tfr.read_double();
      r12=tfr.read_double();
    }catch( IOException e){
      return false;
    }finally{
      if(tfr!=null)
        try{
          tfr.close();
        }catch(IOException e){
          // let it drop on the floor
        }
    }

    return true;
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
  }

  /**
   * Split out the user's choice when calling the operator into all
   * the necessary values for execution.
   */
  private boolean parseChoice(int choice){
    nchoice = 0;
    ncell = 0;
    nequal = 0;

    if(choice==0){ // no restrictions
      nchoice=0;
      return true;
    }else if(choice==1){ // highest symmetry
      nchoice=1;
      return true;
    }else if( choice>1 && choice<16 ){ // choose symmetry type
      nchoice=2;
      ncell=choice-1;
      return true;
    }else if( choice>15 && choice<20 ){ // choice symmetric scalar equalities
      nchoice=3;
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
   * I don't know what this does but is one of the main 'if' clauses.
   */
  private void mark01(){
    if( (nchoice == 3)
          || ( abs(r11-r22) < (3.*sqrt(sig11*sig11+sig22*sig22)) )
        && ( abs(r11-r33) < (3.*sqrt(sig11*sig11+sig33*sig33)) ) ){
      if(DEBUG)System.out.println("MARK01");
    }else{
      return;
    }

    if (nchoice == 3 && nequal != 1){
      Goto = 40;
      return;
    }

    if(Goto!=0)
      return;

    if( abs(r23)<(3.*sig23) ){
      if( abs(r31)<(3.*sig31) && abs(r12)<(3.*sig12) ){
        k[i-1] = 1;
        l[i-1] = 1;
        i = i+1;
      }
      if( abs(r31+(0.5*r11))<(3.0*sqrt(sig31sq+0.25*sig11sq))
          && abs(r12+(0.5*r11)) < (3.0*sqrt(sig12sq+0.25*sig11sq)) ){
        k[i-1] = 3;
        l[i-1] = 2;
        i = i+1;
      }
    }

    if( abs(r23-(-0.5*r11))<(3.0*sqrt(sig23sq+0.25*sig11sq)) ){
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) && abs(r12)<(3.*sig12) ){
        k[i- 1] = 2;
        l[i- 1] = 2;
        i=i+1;
      }
      if( abs(r12-r23)<(3.*sqrt(sig12sq+sig23sq)) && abs(r31)<(3.*sig31) ){
        k[i-1] = 4;
        l[i-1] = 2;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) 
          && abs(r12-(-r23))<(3.*sqrt(sig12sq+sig23sq)) ){
        k[i-1] = 5;
        l[i-1] = 2;
        i = i+1;
      }
    }

    if( abs(r23-(-r11/3.))<(3.*sqrt(sig23sq+(sig11sq/9.)))
        && abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq))
        && abs(r12-r23) < (3.*sqrt(sig12sq+sig23sq)) ){
      k[i-1] = 8;
      l[i-1] = 4;
      i = i+1;
    }

    if( abs(r23-0.5*(-r11-r31))<3.*sqrt(sig23sq+0.25*sig11sq+0.25*sig31sq)
        && abs(r12-r23)<(3.*sqrt(sig12sq+sig23sq)) && r31<0.  ){
      k[i-1] = 10;
      l[i-1] = 5;
      i = i+1;
    }

    if( abs(r23-0.5*(-r11-r12))<3.*sqrt(sig23sq+0.25*sig11sq+0.25*sig12sq)
        && abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) && r12<0.    ){
      k[i-1] = 11;
      l[i-1] = 5;
      i = i+1;
    }

    if( abs(r23-(-r11-r31-r12))<3.*sqrt(sig23sq+sig11sq+sig31sq+sig12sq)
        && r31<0. && r12<0.  ){
      k[i-1] = 12;
      l[i-1] = 6;
      i = i+1;
    }

    if( r23<0.00 ){
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq))
          && abs(r12-(-r23))<(3.*sqrt(sig12sq+sig23sq))  ){
        k[i-1] = 6;
        l[i-1] = 3;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq))
          && abs(r12-r23)<(3.*sqrt(sig12sq+sig23sq))  ){
        k[i-1] = 7;
        l[i-1] = 3;
        i = i+1;
      }
      if( abs(r31-(0.5*(-r11-r23)))<3.*sqrt(sig31sq+0.25*sig11sq+0.25*sig23sq)
      && abs(r12-(0.5*(-r11-r23)))<3.*sqrt(sig12sq+0.25*sig11sq+0.25*sig23sq)){
        k[i-1] = 9;
        l[i-1] = 5;
        i = i+1;
      }
      if( abs(r31-(-r11-r23-r12))<3.*sqrt(sig31sq+sig11sq+sig23sq+sig12sq)
          && r12<0.  ){
        k[i-1] = 13;
        l[i-1] = 6;
        i = i+1;
      }
      if( abs(r12-(-r11-r23-r31))<3.*sqrt(sig12sq+sig11sq+sig23sq+sig31sq)
          && r31<0.  ){
        k[i-1] = 14;
        l[i-1] = 6;
        i = i+1;
      }
    }
  }  // ==================== end of mark01

  /**
   * I don't know what this does but is one of the main 'if' clauses.
   */
  private void mark02(){
    if( Goto == 40
        || ( abs(r11-r22)<(3.*sqrt(sig11sq+sig22sq) ) 
             && abs(r11-r33)>=(3.*sqrt(sig11sq+sig33sq)))   ){
      if(DEBUG) System.out.println("MARK02");
    }else{
      return;
    }

    Goto = 0;
    if (nchoice == 3 && nequal != 2){
      Goto = 41;
      return;
    }

    if(Goto!=0)
      return;

    if( abs(r23)<(3.*sig23)  ){
      if( abs(r31)<(3.*sig31) && abs(r12)<(3.*sig12) ){
        k[i-1] = 15;
        l[i-1] = 7;
        i = i+1;
      }
      if( abs(r12-0.50*r11)<3.*sqrt(sig12sq+0.25*sig11sq) 
          && abs(r31)<(3.*sig31) ){
        k[i-1] = 16;
        l[i-1] = 8;
        i = i+1;
      }
      if( abs(r12-0.50*(-r11))<3.*sqrt(sig12sq+0.25*sig11sq)
          && abs(r31)<(3.*sig31) ){
        k[i-1] = 17;
        l[i-1] = 8;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && r12>0. ){
        k[i-1] = 18;
        l[i-1] = 9;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && r12<0. ){
        k[i-1] = 19;
        l[i-1] = 9;
        i = i+1;
      }
    }

    if( abs(r23-(-0.50*r33))<3.*sqrt(sig23sq+0.25*sig33sq) ){
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) 
          && abs(r12-(0.25*r33))<3.*sqrt(sig12sq+0.0625*sig33sq) ){
        k[i-1] = 20;
        l[i-1] = 5;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) && r12>0. ){
        k[i-1] = 21;
        l[i-1] = 6;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) && r12<0. ){
        k[i-1] = 22;
        l[i-1] = 6;
        i = i+1;
      }
    }

    if( abs(r23+r33/3.)<3.*sqrt(sig23sq+sig33sq/9.)
        && abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq))
        && abs(r12-(-r11/2.+r33/6.))<3.*sqrt(sig12sq+sig11sq/4.+sig33sq/36.) ){
      k[i-1] = 23;
      l[i-1] = 3;
      i = i+1;
    }

    if( r23<0. ){
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) ){
        if( r12>0. ){
          k[i-1] = 24;
          l[i-1] = 10;
          i = i+1;
        }
        if( r12<0. ){
          k[i-1] = 25;
          l[i-1] = 10;
          i = i+1;
        }
      }
    }
    
  }  // ==================== end of mark02

  /**
   * I don't know what this does but is one of the main 'if' clauses.
   */
  private void mark03(){
    if( Goto == 41 
        || (abs(r11-r33)<(3.*sqrt(sig11sq+sig33sq))
            && abs(r11-r22) >= (3.*sqrt(sig11sq+sig22sq)))  ){
      if(DEBUG) System.out.println("MARK03");
    }else{
      return;
    }

    Goto = 0;

    if (nchoice == 3 && nequal != 3){
        Goto = 42;
        return;
    }

    if(Goto!=0)
      return;

    if( abs(r23)<(3.*sig23) ){
      if( abs(r31)<(3.*sig31) && abs(r12)<(3.*sig12) ){
        k[i-1] = 26;
        l[i-1] = 7;
        i = i+1;
      }
      if( abs(r31+r11/2.)<3.*sqrt(sig31sq+sig11sq/4.) && abs(r12)<(3.*sig12) ){
        k[i-1] = 27;
        l[i-1] = 8;
        i = i+1;
      }
      if( r31<0 && abs(r12)<(3.*sig12) ){
        k[i-1] = 28;
        l[i-1] = 9;
        i = i+1;
      }
    }

    if( abs(r23-(-0.50*r11))<3.*sqrt(sig23sq+0.25*sig11sq) ){
      if( abs(r31)<(3.*sig31) && abs(r12-r11/2.)<3.*sqrt(sig12sq+sig11sq/4.) ){
        k[i-1] = 29;
        l[i-1] = 5;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && abs(r12+r11/2.)<3.*sqrt(sig12sq+sig11sq/4.) ){
        k[i-1] = 30;
        l[i-1] = 5;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) 
          && abs(r12+r23)<(3.*sqrt(sig12sq+sig23sq)) ){
        k[i-1] = 33;
        l[i-1] = 3;
        i = i+1;
      }
    }

    if( r23<0. ){
      if( abs(r31-(-r11-2.*r23))<3.*sqrt(sig31sq+sig11sq+4.*sig23sq)
          && abs(r12-r23)<(3.*sqrt(sig12sq+sig23sq)) ){
        k[i-1] = 34;
        l[i-1] = 11;
        i = i+1;
      }
      if( abs(r31-(-r11-r23-r12))<3.*sqrt(sig31sq+sig11sq+sig23sq+sig12sq)
          && r12<0. ){
        k[i-1] = 35;
        l[i-1] = 10;
        i = i+1;
      }
      if( r31<0. ){
        if( abs(r12-(-r23))<(3.*sqrt(sig12sq+sig23sq)) ){
          k[i-1] = 31;
          l[i-1] = 10;
          i = i+1;
        }
        if( abs(r12-r23)<(3.*sqrt(sig12sq+sig23sq)) ){
          k[i-1] = 32;
          l[i-1] = 10;
          i = i+1;
        }
      }
    }
  }  // ==================== end of mark03

  /**
   * I don't know what this does but is one of the main 'if' clauses.
   */
  private void mark04(){
    if( Goto == 42
        || (abs(r11-r22)>=(3.*sqrt(sig11sq+sig22sq)) 
            && abs(r11-r33)>=(3.*sqrt(sig11sq+sig33sq)) 
            && abs(r22-r33) >= (3.*sqrt(sig22sq+sig33sq))) ){
      if(DEBUG) System.out.println("MARK04");
    }else{
      return;
    }
    Goto = 0;

    if (nchoice == 3 && nequal != 4){
      Goto = 43;
      return;
    }

    if(Goto!=0)
      return;

    if( abs(r23)<(3.*sig23) ){
      if( abs(r31)<(3.*sig31) && abs(r12)<(3.*sig12) ){
        k[i-1] = 36;
        l[i-1] = 12;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) 
          && abs(r12-0.50*r11)<3.*sqrt(sig12sq+0.25*sig11sq) ){
        k[i-1] = 37;
        l[i-1] = 9;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31)
          && abs(r12-0.50*(-r11))<3.*sqrt(sig12sq+0.25*sig11sq) ){
        k[i-1] = 38;
        l[i-1] = 9;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && r12>0. ){
        k[i-1] = 39;
        l[i-1] = 13;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && r12<0. ){
        k[i-1] = 40;
        l[i-1] = 13;
        i = i+1;
      }
      if( abs(r31+r33/2.)<3.*sqrt(sig31sq+sig33sq/4.) && abs(r12)<(3.*sig12) ){
        k[i-1] = 41;
        l[i-1] = 9;
        i = i+1;
      }
      if( r31<0 && abs(r12)<(3.*sig12) ){
        k[i-1] = 42;
        l[i-1] = 13;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) && r12>0. ){
        k[i-1] = 43;
        l[i-1] = 10;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) && r12<0. ){
        k[i-1] = 44;
        l[i-1] = 10;
        i = i+1;
      }
    }

    if( abs(r23-(0.50*r33))<3.*sqrt(sig23sq+0.25*sig33sq) ){
      if( abs(r31)<(3.*sig31) && abs(r12)<(3.*sig12) ){
        k[i-1] = 45;
        l[i-1] = 9;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && abs(r12+r11/2.)<3.*sqrt(sig12sq+sig11sq/4) ){
        k[i-1] = 48;
        l[i-1] = 6;
        i = i+1;
      }
    }

    if( abs(r23-(-0.50*r33))<3.*sqrt(sig23sq+0.25*sig33sq) ){
      if( abs(r31)<(3.*sig31) && abs(r12-r11/2.)<3.*sqrt(sig12sq+sig11sq/4.) ){
        k[i-1] = 47;
        l[i-1] = 6;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && r12 > 0. ){
        k[i-1] = 49;
        l[i-1] = 10;
        i = i+1;
      }
      if( abs(r31)<(3.*sig31) && r12<0. ){
        k[i-1] = 50;
        l[i-1] = 10;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq))
          && abs(r12-(0.25*r33))<3.*sqrt(sig12sq+0.25*sig33sq) ){
        k[i-1] = 53;
        l[i-1] = 11;
        i = i+1;
      }
      if( abs(r31-r23)<(3.*sqrt(sig31sq+sig23sq)) && r12>0. ){
        k[i-1] = 54;
        l[i-1] = 10;
        i = i+1;
      }
      if( r31<0. && abs(r12-(-0.5*r31))<3.*sqrt(sig12sq+0.25*sig31sq) ){
        k[i-1] = 55;
        l[i-1] = 10;
        i = i+1;
      }
    }

    if( abs(r23-(0.50*r31))<3.*sqrt(sig23sq+0.25*sig31sq) && r31<0.
        && abs(r12-0.5*r11) < 3.*sqrt(sig12sq+0.25*sig11sq) ){
      k[i-1] = 56;
      l[i-1] = 10;
      i = i+1;
    }

    if( abs(r23+(r33+r31)/2.)<3.*sqrt(sig23sq+sig33sq/4.+sig31sq/4.) && r31<0.
        && abs(r12-(0.5*(-r11-r31)))<3.*sqrt(sig12sq+sig11sq/4.+sig31sq/4.) ){
      k[i-1] = 58;
      l[i-1] = 10;
      i = i+1;
    }

    if( r23< 0. ){
      if( abs(r31)<(3.*sig31) && abs(r12)<(3.*sig12) ){
        k[i-1] = 46;
        l[i-1] = 13;
        i = i+1;
      }
      if( abs(r12-r11/2.)<3.*sqrt(sig12sq+sig11sq/4.) && abs(r31)<(3.*sig31) ){
        k[i-1] = 51;
        l[i-1] = 10;
        i = i+1;
      }
      if( abs(r12+r11/2.)<3.*sqrt(sig12sq+sig11sq/4.) && abs(r31)<(3.*sig31) ){
        k[i-1] = 52;
        l[i-1] = 10;
        i = i+1;
      }
      if( abs(r31+0.5*r33)<3.0*sqrt(sig31sq+sig33sq/4.)
          && abs(r12+0.5*r23)<3.*sqrt(sig12sq+sig23sq/4.) ){
        k[i-1] = 57;
        l[i-1] = 10;
        i = i+1;
      }
      if( r31<0. ){
        if( r12>0. ){
          k[i-1] = 59;
          l[i-1] = 14;
          i = i+1;
        }
        if( r12<0. ){
          k[i-1] = 60;
          l[i-1] = 14;
          i = i+1;
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
      System.out.println(" FOR SCALARS "+r11+" "+r22+" "+r33+" ");
      System.out.println("             "+r23+" "+r31+" "+r12+" ");
      return;
    }

    i = i-1;
    int minsym = 20;
    for( int j=0 ; j<i ; j++ ){
      minsym = (int)(Math.min(minsym, l[j]) );
    }

    if (nchoice == 1)  
      npick = minsym;
    if (nchoice == 2)  
      npick = ncell;
    boolean nflag = false;
    int imax = i;
    i = 1;


    for( int n=0 ; n<imax ; n++ ){
      if( l[n]==npick || nchoice==0 || nchoice==3 ){
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
            System.out.print(trans[m+((j+3*k[n])*3)-13]+" ");

          System.out.println();
        }
      }
    }

    if(!nflag)
      System.out.println((" NO MATCHES FOUND"));
    System.out.println((" "));


  }  // ==================== end of mark05

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
