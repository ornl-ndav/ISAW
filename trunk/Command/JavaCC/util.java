package Command.JavaCC;
import java.util.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Numeric.*;
import DataSetTools.operator.*;
import java.text.*;

public class util{


   public static int Sign( float f){

       if( f <0) return -1;
       return 1;
   }

   
   public static int Sign( double f){

       if( f <0) return -1;
       return 1;
   }
   public static int Sign( int f){

       if( f <0) return -1;
       return 1;
   }
   public static int Sign( long f){

       if( f <0) return -1;
       return 1;
   }
   public static int Sign( short f){

       if( f <0) return -1;
       return 1;
   }
   public static int Sign( byte f){

       if( f <0) return -1;
       return 1;
   }

  public static float  Mod( double arg1, double arg2){
     if( arg2 == 0)
         return (float)arg1;
     int quo = (int)(arg1/arg2);
     return (float)(arg1 - arg2*quo);
  }

  public static float  Int( double arg1){
     double res =Sign(arg1)*Math.floor( Math.abs(arg1));
     return (float) res;

  }
  public static float  SIGN( double arg1, double arg2){
    if( arg2 > 0)
       return (float)Math.abs( arg1);
    return (float)(-1*Math.abs(arg1));
  }

  public static float  DIM( double arg1, double arg2){
     return (float) Math.max(0.0, arg1-arg2);
  }
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

 
 public static final String LocalNames =
  ";WRITESTRING;WRITELN;WRITEINT;WRITEFLOAT;";
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
public static Object cvrtObject( int x){
    return new Integer(x);
 }

public static Object cvrtObject( float x){
    return new Float(x);
 }
public static Object cvrtObject( long x){
    return new Long(x);
 }
public static Object cvrtObject( short x){
    return new Short(x);
 }
public static Object cvrtObject( double x){
    return new Double(x);
 }
public static Object cvrtObject( Object x){
    return x;
 }

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

 public static void WRITESTRING( String S){
     System.out.print(S);
 }
public static void WRITELN(){
  System.out.println("");
}
public static void WRITEINT( int i, String format){
  int width=-1;
  try{
     width = new Integer(format.trim().substring(1)).intValue();
  }catch( Exception s){
    width = -1;
  }
  if( width > 0)
   System.out.print( gov.anl.ipns.Util.Numeric.Format.integer((double)i,width));
 else
    System.out.print(""+i);

}

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
     System.out.print(i);
  int width = -1;
  int aftDec = -1;
  try{
    width = new Integer( format.substring(0,dot)).intValue();
    aftDec = new Integer( format.substring(dot+1)).intValue();
  }catch( Exception s){
    System.out.print( ""+i);
  }
  if( !exponential){
    System.out.print(gov.anl.ipns.Util.Numeric.Format.real((double)i,width, aftDec));
    return;
  }
  char[] javaFormatString = new char[width];
  System.arraycopy( "E+##".toCharArray(),0,javaFormatString,width-4,4);
  javaFormatString[0]= '-';
  Arrays.fill( javaFormatString,1,width-4,'#');
  javaFormatString[ width-1-4-aftDec]='.';
  String S = (new DecimalFormat( new String( javaFormatString))).format((double)i);
  System.out.print( gov.anl.ipns.Util.Numeric.Format.string( S,width,true));

}
}
