/*
 * File:  util.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2004/08/02 20:06:02  rmikk
 * The WRITE methods now go to the status pane
 *
 * Revision 1.2  2004/07/30 14:49:46  rmikk
 * Removed unused imports
 *
 * Revision 1.1  2004/06/16 21:02:32  rmikk
 * Initial Checkin in this directory. 
 * Moved from the Command.JavaCC directory
 * Added documentation and GPL
 *
 */
package Command.JavaCC.Fortran;
import java.util.*;
import gov.anl.ipns.Util.SpecialStrings.*;
//import gov.anl.ipns.Util.Numeric.*;
import DataSetTools.operator.*;
import java.text.*;
import DataSetTools.util.*;

/**
 * This class contains compile and run-time utility methods used by Fcvrt.java
 *   and the resulting Java code.
 * 
 * @author mikkelsonr
 *
 */
public class util{

   /**
    * Returns -1 if the argument is negative, otherwise +1 is returned
    * @param f  The argument whose sign is to be determined
    * @return   the sign of the argument f, either +1 or -1
    */
   public static int Sign( float f){

       if( f <0) return -1;
       return 1;
   }


  /**
   * Returns -1 if the argument is negative, otherwise +1 is returned
   * @param f  The argument whose sign is to be determined
   * @return   the sign of the argument f, either +1 or -1
   */  
   public static int Sign( double f){

       if( f <0) return -1;
       return 1;
   }
   

  /**
   * Returns -1 if the argument is negative, otherwise +1 is returned
   * @param f  The argument whose sign is to be determined
   * @return   the sign of the argument f, either +1 or -1
   */
   public static int Sign( int f){

       if( f <0) return -1;
       return 1;
   }
   

  /**
   * Returns -1 if the argument is negative, otherwise +1 is returned
   * @param f  The argument whose sign is to be determined
   * @return   the sign of the argument f, either +1 or -1
   */
   public static int Sign( long f){

       if( f <0) return -1;
       return 1;
   }
   

  /**
   * Returns -1 if the argument is negative, otherwise +1 is returned
   * @param f  The argument whose sign is to be determined
   * @return   the sign of the argument f, either +1 or -1
   */
   public static int Sign( short f){

       if( f <0) return -1;
       return 1;
   }
   

  /**
   * Returns -1 if the argument is negative, otherwise +1 is returned
   * @param f  The argument whose sign is to be determined
   * @return   the sign of the argument f, either +1 or -1
   */
   public static int Sign( byte f){

       if( f <0) return -1;
       return 1;
   }

  /**
   * The FORTRAN mod function
   * @param arg1
   * @param arg2
   * @return  returns arg1 mod arg2
   */
  public static float  Mod( double arg1, double arg2){
     if( arg2 == 0)
         return (float)arg1;
     int quo = (int)(arg1/arg2);
     return (float)(arg1 - arg2*quo);
  }

  /**
   *  The FORTRAN INT intrinsic function
   * @param arg1  The argument
   * @return  The greatest integer <= arg1
   */
  public static float  Int( double arg1){
     double res =Sign(arg1)*Math.floor( Math.abs(arg1));
     return (float) res;

  }
  
  /**
   *  The FORTRAN SIGN intrinsic function
   * @param arg1  1st argument
   * @param arg2  2nd argument
   * @return  Tranfers arg2's sign to arg1 and returns that value
   */
  public static float  SIGN( double arg1, double arg2){
    if( arg2 > 0)
       return (float)Math.abs( arg1);
    return (float)(-1*Math.abs(arg1));
  }

  /**
   * FORTRAN's IDIM intrinsic function
   * @param arg1
   * @param arg2
   * @return  max of 0 and arg1-arg2
   */
  public static float  DIM( double arg1, double arg2){
     return (float) Math.max(0.0, arg1-arg2);
  }
  
  /**
   * FORTRAN's ALOG10 function
   * @param arg1   argument
   * @return   log base 10 of the argument
   */
  public static float  ALOG10( double arg1){
     return (float)(Math.log(arg1)*0.4342944819032518276511);

  }
  
  
  /**
    * Returns the array of directories in the filename with any leading
    * path in the java.io.File.pathSeparator separated list of paths
    * @param  filename the file name to break up
    * @param  Paths  the java.io.File.PathSeparator separated list of paths
    * @return an array of directories in the given full filename
    */
  public static String[] dirList( String filename, String Paths){
     if(Paths == null)
        Paths = "";
     Paths=Paths.replace('\\','/');
     Paths=Paths.replace(java.io.File.pathSeparatorChar,';');
     if( filename == null)
       return new String[0]; 
     filename = filename.replace('\\','/');
     filename = filename.replace(java.io.File.pathSeparatorChar,';');
    
    // -----Take off leading parts of the filename-----
     int i=0;
     int j=0;
     for( int j1= Fixx(Paths.length(),Paths.indexOf(";"));
           j+1 < Paths.length();
           j1= Fixx(Paths.length(),Paths.indexOf(";",j1+1))){
       if( filename.indexOf( Paths.substring(j,j1)) ==0)
            i= Math.max(i,j1-j);
       j=j1+1;
     }
     filename = filename.substring( i);

    // --- Take off trailing non directory info-----------

     j= filename.lastIndexOf('/');
     if( j < 0)
        j = filename.length();
     filename = filename.substring(0,j);

     //-------------- Eliminate stuff preceding colon(Windows)----
     j = filename.indexOf(':');
     filename = filename.substring( j+1);
     //------------ Eliminate trailing and leading /---------
     while( filename.startsWith("/"))
        filename=filename.substring(1);
     while(filename.endsWith("/"))
        filename = filename.substring(0, filename.length()-1);
     //------------ eliminate // ---------------------------
     while( filename.indexOf("//")>=0){
        int k = filename.indexOf("//");
        filename = filename.substring(0,k)+filename.substring(k+1);
     }


     return filename.split("/");
    
  }
  
  //Utility that returns val if positive otherwise length is returned
  private static int Fixx( int length, int val ){
      if( val <0)
       return length;
      return val;
  }
  
  
 /**
   *  Appends the newValue to Vector V and returns the result.
   *   The Argument is also changed
   
   */
 public static Vector appendVect( Vector V, Object newValue){
     V.addElement( newValue);
     return V;
  }


 private static String getLocalRoutine( String name){
    if( name.indexOf("MOD")>=0)
       return "util.Mod";
    if( name.indexOf("INT")>=0)
       return "util.Int";
    if( name.indexOf("SIGN")>=0)
       return "util.SIGN";
    if(name.equals("IFIX"))
       return "util.Int";
    if( name.equals("IDIM"))
       return "util.DIM";
    return "util."+name;


 }
 
 
 
 private static final String IntrinsNames =
   ";ABS;IABS;MAX0;MAX1;AMAX0;AMAX1;MIN0;MIN1;AMIN0;AMIN1;SQRT;EXP;LOG;ALOG;"+
    "SIN;COS;TAN;ASIN;ACOS;ATAN;ATAN2;ALOG10;INT;IFIX;AINT;ANINT;NINT;"+
    "MOD;AMOD;ISIGN;SIGN;IDIM;DIM;";
 
   
 /**
  * Returns java call corresponding to the FORTRAN instrinsic function 
  * @param name  The name of the FORTRAN instrinsic function 
  * @param params params to be passed to the java routine.
  * @return  String representation of the java code corresponding to the 
  *       intrinsic FORTRAN function
  */ 
 public static String JavaInvokeIntrinsic( String name, String[] params){
   
   if( params == null)
      return null;
   if( params.length >2)
      return null;
   name = name.toUpperCase();
   int i = IntrinsNames.indexOf(";"+name+";");
   if( i < 0)
     return null;
   String dt ="float";       
      if("IJKLMNO".indexOf( name.charAt(0))>=0)
   
          dt = "int";
   String S;
   if( i < 105){//can map to Math routines
    
      if( name.length() >3)
        if( name.startsWith("A"))
            name = name.substring(1);
      if( !name.equals("SQRT"))
      if( name.length() > 3)
        name = name.substring(0,3);

      name = name.toLowerCase(); 
      S = "("+dt+")Math."+name +"(";

   }else{
    
   S = "(dt)util."+getLocalRoutine( name)+"(";
   }
   for(  i = 0; i< params.length; i++){
      S += "(double)("+params[i]+")";
      if( i+1 <params.length)
         S +=",";
      else
         S += ")";
   }
  return S;
 }

 
 
 private static final String LocalNames =
  ";WRITESTRING;WRITELN;WRITEINT;WRITEFLOAT;";
  
 /**
  * Creates java code for special add-on or Intrinsic functions(etc)
  * that are not handled by JavaInvokeIntrinsic
  * @param name  The name of the FORTRAN sub
  * @param params Parameters(in Java) for this FORTRAN(local)/function
  * @return Java code to invoke this function
  */
 public static String LocalInvokeFxnCode( String name, String[] params){
    if(name.toUpperCase().equals("FLOAT"))
       if( params==null)
         return null;
       else if( params.length != 1)
         return null;
       else
         return "(float)("+params[0]+")";
    if( LocalNames.indexOf(";"+name+";") < 0)
      return null;
    String Res = "util."+name+"(";
    if(params != null)
    for(int i=0; i < params.length; i++){
        Res +=params[i];
        if( i+1 < params.length)
          Res +=",";
        
    }
    Res +=")";
    return Res;
}


 /**
  * Functions not handled by LocalInvokeFxnCode or JavaInvokeIntrinsic are
  *   linked into the operator system via Script_Class_List_Handler
  * @param name  name of FORTRAN(ISAW) subroutine/Function
  * @param params Parameters(in Java) for the operator
  * @return  the Java String form to handle this operation 
  */
 public static String OperatorInvokeFxnCode( String name,String[] params){
    String Res ="util.exec(\""+name+"\",";
    String S1 = "new Vector()";
    if(params != null)
      for( int i=0; i< params.length; i++){
         S1 = "util.appendVect("+S1+", util.cvrtObject("+params[i]+"))";

     }
    Res += S1+")";
    return Res;

  }


/**
 *  OperatorInvokeFxnCode writes out code to execute this method. This method 
 * invokes routines from the Command Package to find the appropriate operator
 * @param name  The name of the FORTRAN(ISAW) subroutine
 * @param Args  The arguments for the subroutine
 * @return      The result of the operator's getResult method along with any 
 *              side effects
 * @throws Throwable java.lang.UnsupportedOperationException if the operation 
 *     is not found or an ErrorMessage is returned
 */
public static Object exec( String name, Vector Args) throws Throwable{
  Object[] args;
  if( Args == null)
     args = new Object[0];
  else
     args = new Object[ Args.size()];
  for( int i=0; i< args.length; i++)
     args[i] = Args.elementAt(i);
  try{
    Operator op = Command.ScriptUtil.getNewOperator( name, args);
    Object Res = op.getResult();
    if( Res instanceof ErrorString)
       throw new java.lang.UnsupportedOperationException(Res.toString());
    return Res;
  } catch( Throwable ss){
      throw ss;
  }

}

/**
 *  Converts an int to an Object(Integer)
 * @param x   int value
 * @return    Corresponding Integer value
 */
public static Object cvrtObject( int x){
    return new Integer(x);
 }


 /**
  *  Converts an float to an Object(Float)
  * @param x   float value
  * @return    Corresponding Float value
  */
public static Object cvrtObject( float x){
    return new Float(x);
 }
 

 /**
  *  Converts an long  to an Object(Long )
  * @param x   long  value
  * @return    Corresponding Long  value
  */
public static Object cvrtObject( long x){
    return new Long(x);
 }
 

 /**
  *  Converts an short to an Object(Short)
  * @param x   int short
  * @return    Corresponding Short value
  */
public static Object cvrtObject( short x){
    return new Short(x);
 }
 
 

 /**
  *  Converts an double to an Object(Double)
  * @param x   double value
  * @return    Corresponding Double value
  */
public static Object cvrtObject( double x){
    return new Double(x);
 }
 
 

 /**
  *  Converts an Object to an Object(Integer)
  * @param x   int value
  * @return    Corresponding Object value
  */
public static Object cvrtObject( Object x){
    return x;
 }


/**
 *  Test program for the exec method in this class
 * @param args   not used
 */
public static void main( String[] args){
   String[] params={"35","\"I5\""};

   System.out.println(util.LocalInvokeFxnCode("WRITEINT", params));

   Vector V = new Vector();
   V.addElement( new Integer(5));
   V.addElement( "Hi There");
  try{
    System.out.println("exec="+util.exec("XXX",V));
  }catch( Throwable ss){
   System.out.println("Error="+ss);
  }
  
}

 /**
  *  Implements the WRITESTRING FORTRAN CALL
  * @param S  The String to be read
  */
 public static void WRITESTRING( String S){
     addmsg(S);
 }
 

 /**
  *  Implements the WRITELN FORTRAN CALL
  */
public static void WRITELN(){
  SharedData.addmsg( lineBuff);
  lineBuff="";
  
}


/**
 *  Implements the WRITEINT FORTRAN CALL
 * @param i  The int value to be written out
 * @param format  the FORTRAN-LIKE format for writing this integer
 */
public static void WRITEINT( int i, String format){
  int width=-1;
  try{
     width = new Integer(format.trim().substring(1)).intValue();
  }catch( Exception s){
    width = -1;
  }
  if( width > 0)
     addmsg( gov.anl.ipns.Util.Numeric.Format.integer((double)i,width));
 else
    addmsg(""+i);

}



/**
 *  Implements the WRITEFLOAT FORTRAN CALL
 * @param i  The float value to be written out
 * @param format  the FORTRAN-LIKE format for writing this integer
 */
public static void WRITEFLOAT( float i, String format){
  format = format.trim();
  boolean exponential= false;
  if( format == null)
      System.out.print(i);
  if( format.toUpperCase().startsWith("E"))
    exponential = true;
  format = format.substring(1);
  int dot = format.indexOf('.');
  if( dot < 0)
     addmsg(""+i);
  int width = -1;
  int aftDec = -1;
  try{
    width = new Integer( format.substring(0,dot)).intValue();
    aftDec = new Integer( format.substring(dot+1)).intValue();
  }catch( Exception s){
	addmsg( ""+i);
  }
  if( !exponential){
	addmsg(gov.anl.ipns.Util.Numeric.Format.real((double)i,width, aftDec));
    return;
  }
  char[] javaFormatString = new char[width];
  System.arraycopy( "E+##".toCharArray(),0,javaFormatString,width-4,4);
  javaFormatString[0]= '-';
  Arrays.fill( javaFormatString,1,width-4,'#');
  javaFormatString[ width-1-4-aftDec]='.';
  String S = (new DecimalFormat( new String( javaFormatString))).format((double)i);
  addmsg( gov.anl.ipns.Util.Numeric.Format.string( S,width,true));

}
static String  lineBuff="";
public static void addmsg( String S){
  lineBuff += S;
}
}
