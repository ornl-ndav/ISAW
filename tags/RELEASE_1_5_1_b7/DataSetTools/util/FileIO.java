
/*
 * File:  FileIO.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/09/16 16:12:33  rmikk
 * Improved End Condition response
 *
 * Revision 1.1  2003/07/14 16:49:58  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.util;
import java.io.*;
import java.util.*;
import DataSetTools.util.*;
import java.lang.reflect.*;
public class FileIO{
  public static final String NO_MORE_DATA = "No More Data";
  public static boolean debug = false;
  public FileIO(){
  }


 /**
 *    Writes Data to a file in the specified Format. 
 *
 *   @param   filename the name of the file.
 *   @param   append   true if data is appended to the end of the file.
 *   @param   ReturnAtEnd   will add a carriage return at the end of the file
 *                           if true.
 *   @param   V        A vector of data Objects.  If elements are arrays or
 *                     Vectors, the first elements of each are printed out then
 *                     the second elements, then the third, until there are no
 *                     more elements.
 *
 *   @param   Format   A simple FORTRAN-like format descriptors separated by 
 *                     commas.  Only I,F,E,S(for string) and /(for return) are
 *                     considered.  S5+ right justifies a string in a width of 
 *                     length 5.
 *   @return  The Integer value of the number of elements in the arrays written,
 *             or a descriptive ErrorString
 */

  public static Object Write( String filename,  boolean append, 
          boolean ReturnAtEnd,  Vector V,  String Format){
     
     File f = new File( filename);
     FileOutputStream fout;
     try{
        if( append) 
           fout = new FileOutputStream( filename, append);
        else
           fout = new FileOutputStream( filename);
        }
     catch( IOException ss)
       {
         return new ErrorString( ss.getMessage() );
       }

     //Setup Format Handelr
     FormatHandler FmtHandler = new FormatHandler( Format);
     int line = 0;
     boolean done = false;
     try{
     while( !done){
        for( int i = 0; i< V.size(); i++){
            Object F = V.elementAt( i );
            
            Object S = FmtHandler.write( F , line );
           
            if( S instanceof ErrorString)
               if( S.toString() != FileIO.NO_MORE_DATA )
                   return finishWrite(fout, S, ReturnAtEnd);
               else
                   return finishWrite(fout,new Integer( line), ReturnAtEnd);
            fout.write( ((String)S).getBytes() );
         }
        line++;
      }
   
    if( ReturnAtEnd ){            //For appending next file use this.
       fout.write( "\n".getBytes() );
       
     }
     fout.close();
        }
     catch( Exception ss){
         return new ErrorString( ss);
     }
     
     
        return new Integer( line);
  }//method Write 


  /**
  *    Finishes the write operations. Closes file and printe return at end if desired
  */
  private static Object finishWrite( FileOutputStream fout, Object S, boolean  ReturnAtEnd){
    try{
    if( ReturnAtEnd ){
       fout.write( "\n".getBytes() );
       
     }
     fout.close();
      }
     catch( Exception ss){}
     return S;
  }


  /**
  *    Reads Data sequentially out of a text file and assigns them to the array or Vector 
  *    variables described in in Vector V.  The Format is similar to the Write format except that
  *    a format specifier of B specifies free form reading( not implemented yet).
  *    @param   f    The fileInputStream to read from. Used for "append" like operations
  *    @param   V    Contains on input a list of data types for variables to be read.  On output, the
  *                  values are all set into the vector.
  *    @param  Format  Specifier at how subsequent entries in the file are to be read.
  *    @param  MaxLines  the maximum number of lines to be read or -1 if that is not an end condition
  *    @param  EndConditions  a Vector of end conditions of data type EndCond that describe whether 
  *            finished or not.  The first in the Vector refers to the first variable in V, the 2nd
  *            refers to the 2nd, etc. All conditions must be true to be through
  *    @see   EndCond
  *    @return  The length of the arrays filled up or an ErrorString.
  */
  public static Object Read( FileInputStream f, Vector V, String Format, int MaxLines,
         Vector EndConditions){
     Vector Copy = new Vector(); //Contains the copy of the Return values
     for( int i=0; i < V.size() ; i++)
        Copy.addElement( new Vector() );
     int omitLast = 0;
     FormatHandler Fhandler = new FormatHandler( Format);
     if( debug) System.out.println( "FileIO Read Format="+Format);
     int line = 0;
     boolean done = false;
    
     while( !done){
        Vector LastLine = new Vector();         //For comparison using end conditions
        omitLast = 0;
        for( int i = 0; i< V.size(); i++){
         
          Object S ;
          try{
             S= Fhandler.read( f );
             if( debug) System.out.println("line,entry,val="+line+","+i+","+S);
              }
          catch( Exception u){
             u.printStackTrace();
             return new ErrorString( "IOerror="+u.getMessage()+line );
          }
          if( S instanceof ErrorString)
             if( S.toString() != FileIO.NO_MORE_DATA)
                return S;
             else 
                done = true; 
          LastLine.addElement( S );
          
         }
       line++;
       if ( (MaxLines >= 0) &&(line >= MaxLines))
         done = true;
       else if( EndConditions == null){
       }
       else{
          boolean end = true;
          for( int j = 0;(j < LastLine.size()) && (j< EndConditions.size()) && end; j++)
            {end = end && ((EndCond)(EndConditions.elementAt( j ))).
                                done( LastLine.elementAt( j ));
            }
          if( end){
            done = true;
            omitLast = 1;
            line--;
          }
       }
     if( omitLast != 1){
         for( int jj=0; jj< V.size(); jj++)  
           ((Vector)Copy.elementAt(jj)).addElement( LastLine.elementAt(jj));
     }
     }//while !done
 
 
     //Now copy the values back to Vector V
     for( int i = 0; i< V.size() ; i++){

       Object O = V.elementAt( i);

       Vector C =(Vector)(Copy.elementAt( i));
       if( O == null){}

       else if( O instanceof Vector){
          V.setElementAt( C, i);
       
       }else if( O != null ){
        Class c = getBaseClass( O );
        Object arry = Array.newInstance( c, line );
        for( int j = 0; j< line; j++){
          try{
              Array.set( arry, j, C.elementAt(j) );
             }
          catch( Exception ss){
            return new ErrorString( "x"+ss.toString()+","+j+","+line+","+C.size()+","+i );
          }
         
        }
         V.setElementAt( arry, i);
          
       }


     }
    
     return new Integer( line);

   }//Method Read

  
  // Used to get a class so and array of elements of that
  // class can be created
  private static Class getBaseClass( Object O){
     if( O == null)
        return null;
     if( O instanceof Vector)
        return null;
     if( !O.getClass().isArray() )
        return O.getClass();
     return O.getClass().getComponentType();
        

  }

  /** 
  *    Test program for this module.  NO arguments
  */  
  public static void main( String args[] ){

    int i;
    Vector V;
    String[] x = new String[30];
    for(  i = 0; i< 30; i++)
        x[i] = ""+i;
    double[] y = new double[30];
    for(  i = 0; i< 30; i++)
        y[i] = 35+i;
    V = new Vector();
    V.addElement( "Hi There" );
    //V.addElement(y);
    System.out.println("Result="+ FileIO.Write("x.dat", false, true,
             V, "S8"));
   /* V = new Vector();
    V.addElement(y);
    System.out.println("Result="+ FileIO.Write("x.dat", true, false,
             V, "I8,I8,I8,I8,/"));
 
    V = new Vector();
    V.addElement( new double[0]);
    //V.addElement( new double[0]);
    try{
       FileInputStream fin = new FileInputStream( "x.dat");
    System.out.println( FileIO.Read( fin, V,"F8.2,F8.2,F8.2,F8.2,/", 30, null) );
    Vector V1 = new Vector();
     V1.addElement( new double[0] );
    
    System.out.println( FileIO.Read(fin, V1,"F8.2,F8.2,F8.2,F8.2,/", 30, null) );
    System.out.println("Res1="+ StringUtil.toString( V.elementAt(0) ) );
     System.out.println("Res2="+ StringUtil.toString( V1.elementAt(0) ) );
       }
     catch( IOException ss){
       System.out.println("Exception="+ss);
     }

*/


  }
  /**
  *    This method produces an EndCond that can be put into a Vector of
  *    end conditions
  *   @param  relation  Should be "<","<=",">",">=","<>","="
  *   @param  Value     The value the corresponding variable is compared to
  *   @return  an Ending Condition   
  */
  public static EndCond  getEndCondition( String relation,  Object Value){
     return new EndCond( relation, Value);


  }
}//end FileIO

/** 
*    Describes a relatively simple end condition so far
*/
class EndCond{
   String relation;
   Object Value;
   Condition Cond;
  
   /**
   *     Constructor
   *    @param   relation   <, >, <=, >=, =, <> 
   *    @param   Value     The value to compare the current value to. Value is the
   *                       right hand member of the above comparisons
   */
   public EndCond( String relation, Object Value){
      this.relation = relation;
      this.Value = Value;
      if( relation.charAt(0) =='!')
            this.relation = "<>";
      else if( relation.length()  < 2)
        relation = relation + ' ';
      Cond = new Condition( this.relation, Value);
   }

  /**
  *    Compares a given object to Val using the relation specified in the constructor
  *    @param  Val   the object to compare the given value to
  *    @return   true if the comparison is true otherwise false.  True comparisons indicate
  *           that this satisies a terminating read condition so you are done reading
  */ 
  boolean done( Object Val){
    return Cond.compare(Val);
    
    }
  public String toString(){
    return( "EndCond="+relation+","+Value);
  }
}//EndCond

/**
*     This class does the comparing for the EndCond class
*/
class Condition{
   String relation; //  <, >, <=, >=, =, <> 
   char strict, eq;
   Object Value;
   String Vvalue = null; //String form of Value

   /**
   *    Constructor 
   *    @param   relation   <, >, <=, >=, =, <> 
   *    @param   Value     The value to compare the current value to. Value is the
   *                       right hand member of the above comparisons
   */
   public Condition( String relation, Object Value){
      this.relation = relation;
       
      this.Value     = Value;
      Vvalue = Value.toString();
   }


   /**
   *   Compares V2 to the Value specified in the constructor 
   *   @param    V2   The value used in the comparison.  This is the left member of
   *                  the comparison relationships
   *   @return   true if the comparison is true( v2 relation Value) otherwise false
   */
   boolean compare( Object V2){
      if( V2 == null)
         return false;
      if( Value == null)
         return false;
     
      if( Value instanceof Number)
        if( V2 instanceof Number)      
          return NResult(((Number)V2).doubleValue() -
                             ((Number)Value).doubleValue() );
        else
          return false;
      else if( (Value instanceof String) || (V2 instanceof String))
         return SResult( V2);

      return false;


   }

  // A utility for numeric comparisons
  boolean NResult(  double Value){
     if( Value < 0)
       if( relation.charAt(0) == '<' ) 
         return true;
       else
         return false;
     else if( Value > 0)
       if( relation.charAt(0) == '>' ) 
         return true;
       else if( relation.equals("<>") )
         return true; 
       else
          return false;
     else 
       if( relation.charAt(0) == '=')
         return true;
       else if( relation.charAt(1) == '=')
          return true;
       else
         return false;     

  }
 
 // A utility to deal with String comparisons
 boolean SResult( Object V){
    if( V == null)
       return false;
    if( Vvalue == null)
       return false;
    String VV = V.toString();
    int r = VV.compareTo( Vvalue);
    if( (r < 0) )
        if( relation.charAt(0) == '<' ) 
         return true;
       else
         return false;
     else if( r > 0)
       if( relation.charAt(0) == '>' ) 
         return true;
       else if( relation.equals("<>") )
         return true; 
       else
          return false;
    else 
       if( relation.charAt(0) == '=')
         return true;
       else if( relation.charAt(1) == '=')
          return true;
       else
         return false;   

      
 }
}

//A class to represent null
class Nulll{

}

  /**
  *       An interface for subutilities to read and write data to files
  */
   interface FOps{
        public Object write( Object O, int line); 
        public Object read( FileInputStream fin) throws IOException;
        public boolean hasReadReturn();
    
     }

  /**
  *    Handles formats.  It keeps track of which format specifier is current and calls
  *    the proper FOps to handle the read or write operation
  */
  class FormatHandler{
 
     String Format;
     int currentFormat;
     FOps[] f_operations;
     boolean LineBreak = true; //line oriented or not.  NOT implemented yet
     boolean haveReadReturn = false;
     /**
     *    Constructor- sets up variables to handle the Format
     *   @param   Format  A  simple Fortran-like Format string
     */
     public FormatHandler( String Format ){
        this.Format = Format.trim();
        currentFormat = 0;
        int n_commas=0;
        for( int i =this.Format.indexOf(','); i >=0 ; 
             i =this.Format.indexOf(',', i + 1) )
           n_commas++;
        f_operations = new FOps[ n_commas + 1 ];

        if( this.Format.startsWith("(") )
           this.Format = this.Format.substring( 1);
        if( this.Format.endsWith(")" ))
           this.Format = this.Format.substring( 0, this.Format.length() -1);

        int j= 0;
      
        for( int i =0; i< n_commas + 1; i++ ){
           int j1 = this.Format.indexOf( ',',j ) ;
           if( j1 <0)  
             j1 = this.Format.length();
           f_operations[i] = getOp( this.Format.substring( j,j1) );
           j = j1+1;
            
        }
     }

     /**
     *     Write the line-th member of the Array or Vector object
     *     @param   O    The Object containing the value to be written
     *     @param   line  the element number of an array or Vector to be written. If
     *                    Object is not an array or Vector and line=0 O will be written
     *     @ return  the Sring representation of the value to be written or and ErrorString
     */
     public Object write( Object O, int line){

        Vector V = executeDirectives( currentFormat);
        currentFormat = ((Integer)(V.firstElement())).intValue();
        String S = V.lastElement().toString();
        Object O1 = f_operations[ currentFormat ].write( O, line);
       
        currentFormat++;
        currentFormat = fixup( currentFormat);
        if( O1 instanceof String)
           {O1 = S +(String)O1;
           }
        return O1;
     }

   /**
     *     Reads the next item from the input stream
  
     *     @ return  the value read or an ErrorString
     */
    public Object read(FileInputStream fin )throws IOException{
       currentFormat = fixup( currentFormat);
       boolean done = false;
       newLineOp  nl = new newLineOp();
       LineBreakModeOp lbmop= new LineBreakModeOp();
       while( !done)
          if( f_operations[currentFormat]  instanceof newLineOp){
               Object O;
               if(!haveReadReturn)
                  O = f_operations[ currentFormat ].read(fin);
               haveReadReturn = false;
               currentFormat++;
               currentFormat = fixup(currentFormat);
               //return O;
          }else if( f_operations[ currentFormat ] instanceof LineBreakModeOp){
            LineBreak = false;
            currentFormat++;
            currentFormat = fixup(currentFormat);
          }else
           done =true;
       
       Object O = f_operations[ currentFormat].read(fin);
       haveReadReturn = f_operations[currentFormat].hasReadReturn();
       currentFormat++;
       currentFormat = fixup( currentFormat);
       return O;

    }//read
     

     // execute directive format statements
     private Vector executeDirectives( int currentFormat ){
        if( currentFormat < 0)
           currentFormat =0;
        
        while( currentFormat >= f_operations.length)
           currentFormat -= f_operations.length;
        String S ="";
        
        while( (currentFormat < f_operations.length) &&
               (f_operations[ currentFormat] instanceof newLineOp)){
          
            S += f_operations[currentFormat].write( null, -1 );
            currentFormat++;
            currentFormat = fixup(currentFormat);
        }
        Vector V = new Vector();
        V.addElement( new Integer( fixup(currentFormat)));
        V.addElement( S); 
        return V;
     }

     // Gets the currentFormat variable to cycle around rereading the firat
     private int fixup( int currentFormat){
        if( currentFormat < 0)
           return 0;
        if( f_operations.length == 0)
           return 0;
        while( currentFormat >= f_operations.length)
           currentFormat -= f_operations.length;
        return currentFormat;

     }


     //  Get utility FOps that handles a given Format Specifier in a Format Statement
     private FOps  getOp( String S){
        S = S.trim();
        int j = S.indexOf('.');
      
        if( S.startsWith( "I")){
          int width = (new Integer( S.substring(1) )).intValue();
          return new IntWrite( width);
        }
        else if( S.startsWith("F")){
          int width = (new Integer( S.substring(1,j) )).intValue();
          int dec = ( new Integer( S.substring(j+1) )).intValue();
          return new FloatWrite( width, dec );
        }
        else if( S.startsWith("E")){
          int width = (new Integer( S.substring(1,j) ) ).intValue();
          int dec = ( new Integer( S.substring(j+1) ) ).intValue();
          return new ExpWrite( width, dec );
        }
        else if( S.startsWith("S")){
          boolean left = true;
          char c = S.charAt(S.length() -1);
          if( c == '+')
            left = false;
          else
            left = true;
          if( "+-".indexOf( c ) >= 0)
             S = S.substring(0, S.length()-1);
          int width;
          try{
            width = (new Integer( S.substring(1 ) ) ).intValue();
          }catch( Exception uu){
            return new ErrorOp();
          }
       
          return new StringWrite(width, left);
        }
        else if( S.startsWith("/")){
           return new newLineOp();
        }
        else if( S.startsWith("B") ){
           return new LineBreakModeOp();
        }else
           return new ErrorOp();
 
     }//getOp

     
    // Utility to read the next characters that are in the proper form for numbers
    //  It returns an ErrorString or the string representation of the number
    public static Object getNextNumericChars( FileInputStream fin, int width)
                      throws IOException{
       String S ="";
        int c;
       
       if( width < 0){
         
          for( c = fin.read(); (c != -1) && ( c <= 32); c = fin.read()){};

          if( c == -1)
             return new ErrorString( FileIO.NO_MORE_DATA );
           
          boolean E_found = false;  //Can have only one E
          boolean leadingSign = true;// Can start with leading + or -
          boolean decimal = false;// only one decimal allowed
          char cc =(char)c;
          while( Character.isDigit(cc)  || (leadingSign && ("+-".indexOf(cc) >=0)) ||
                 ( (cc=='.') && !decimal && !E_found) || 
                 (("Ee".indexOf(cc)>=0) && !E_found) ){
             S +=cc;
             if( "+-".indexOf(cc) >=0 )
               leadingSign = false;
             if( cc== '.')
                decimal = false;
             if("Ee".indexOf(cc)>=0)
               E_found = true;
             
             c = fin.read();
             if( c == -1)
                break;
             cc =(char)c;
          }
         if( c >32)
           return new ErrorString( "improper Numeric Format " + S);
         
         else if( c < 32)
            S+="\n";    
         
         return S;


       }else{
         
          //read past unreadable characters
          for( c = fin.read(); (c != -1) && ( c < 32); c = fin.read()){};
          S +=(char)c;
          for( int i=0; i< width-1; i++){
             c = fin.read();
             if( c == -1){
               break;

             }
             else 
                 S +=(char)c;

          }
  
          return S;
       }

    
       
    }

    // Utility to get the line-th entry in Object O and return it as a double
    //   value or ErrorString errorS is set.
    public static ErrorString errorS;
    
    public static double getDouble( Object O, int line ){
        errorS = null;
        if( O == null){
          errorS = new ErrorString("null value");
          return Double.NaN;
        }
        try{
           if( O instanceof Vector)
              if( ((Vector)O).size()>= line)
                 if( ((Vector)O).elementAt(line) instanceof Number)
                    return ((Number)((Vector)O).elementAt(line) ).doubleValue();
                 else{
                   errorS = new ErrorString( "Entry is not a Number at line "+line);
                   return Double.NaN;
                 }
                  
              else{
                 errorS = new ErrorString(FileIO.NO_MORE_DATA);
                 return Double.NaN;
              }
           else if( O.getClass().isArray() )
              if( Array.getLength( O) > line)
                if( Array.get( O, line) instanceof Number)
                   return ((Number)Array.get( O,line)).doubleValue();
                 else{
                   errorS = new ErrorString( "Entry is not a Number at line "+line);

                   return Double.NaN;
                 }
              else{
                 errorS =new ErrorString(FileIO.NO_MORE_DATA);
                 return Double.NaN;
              }

           else if( line > 0){
              errorS = new ErrorString(FileIO.NO_MORE_DATA);
              return Double.NaN;
           }
           else if( O instanceof Number)
              return ((Number)O).doubleValue();
           else{
             errorS = new ErrorString( "Entry is not a Number at line "+line);

             return Double.NaN;
           }
             
            }
        catch( Exception ss){
           errorS = new ErrorString("Data Type Error");
           return Double.NaN;
        } 

     }
 
     
  }//FormatHandler

     /**  
     *    An public that handles the I format specifier
     */
     class IntWrite implements FOps{
        int width;
        boolean ReadReturn;
        public IntWrite( int width){
           this.width = width;
        }
        public Object write( Object O, int line){
           double N =FormatHandler.getDouble( O, line);
           if( FormatHandler.errorS != null)
              return  FormatHandler.errorS;
           
           return DataSetTools.util.Format.integer( N , width);
           
        }
       public boolean hasReadReturn(){
          return ReadReturn;
       }
      
         
       public Object read(FileInputStream fin )throws IOException{
        ReadReturn =false;
        Object S = FormatHandler.getNextNumericChars( fin,width);
         if( S instanceof ErrorString)
           return S;
         if( ((String)S).endsWith( "\n")) ReadReturn = true;
         String SS = ((String)S).trim();
         if( SS.length() <1)
            if( fin.available() ==0)
               return new ErrorString( FileIO.NO_MORE_DATA );
         try{   
            return new Integer((new Double( SS )).intValue());
            }
          catch( Exception ss){
            return new ErrorString( ss.getMessage() );
          }
       }
     }

     /**  
     *    An FOps that handles the F format specifier
     */
     class FloatWrite implements FOps{
        int width;
        int dec;
        boolean ReadReturn;
        public FloatWrite( int width, int dec){
           this.width = width;
           this.dec  = dec;
           
        }
        public Object write( Object O, int line){
           double N =FormatHandler.getDouble( O, line);
           if( FormatHandler.errorS != null)
              return  FormatHandler.errorS;
           return DataSetTools.util.Format.real( N , width, dec);
        }

        public boolean hasReadReturn(){
           return ReadReturn;
        }
       public Object read(FileInputStream fin )throws IOException{
         ReadReturn = false;
         Object S = FormatHandler.getNextNumericChars(fin, width);
         
         if( S instanceof ErrorString)
           return S;
         if( ((String)S).endsWith("\n")) ReadReturn = true;
         String SS = ((String)S).trim();
         if( SS.length() <1)
            if( fin.available() ==0)
               return new ErrorString( FileIO.NO_MORE_DATA );
         try{   
            return new Float( (new Double( SS )).floatValue());
            }
          catch( Exception ss){
            return new ErrorString( ss.getMessage() );
          }
       }

     }

 /**  
     *    An FOps that handles the E format specifier
     */
    class ExpWrite implements FOps{
        int width;
        int dec;
        boolean ReadReturn;
        public ExpWrite( int width,int dec){
           this.width = width;
           this.dec  = dec;
        }
        public Object write( Object O, int line){
           double N =FormatHandler.getDouble( O, line);
           if( FormatHandler.errorS != null)
              return  FormatHandler.errorS;;
           return DataSetTools.util.Format.singleExp( N , width);
        }

       public boolean hasReadReturn(){
         return ReadReturn;
       }
       public Object read(FileInputStream fin )throws IOException{
        Object S = FormatHandler.getNextNumericChars(fin, width);
         if( S instanceof ErrorString)
           return S;
         if( ((String)S).endsWith("\n")) ReadReturn = true;
         String SS = ((String)S).trim();
         if( SS.length() <1)
            if( fin.available() ==0)
               return new ErrorString( FileIO.NO_MORE_DATA );
         try{   
            return new Float((new Double(SS )).floatValue());
            }
          catch( Exception ss){
            return new ErrorString( ss.getMessage() );
          }
         
       }
     }

 /**  
     *    An FOps that handles the S or String format specifier
     */
    class StringWrite implements FOps{
        int width;
        boolean left;
        byte[] buff;
        public StringWrite(int width, boolean left){
           this.width = width;
           this.left = left;
           if( width > 0)
             buff= new byte[width];
        }
        public Object write( Object O, int line){
           if( O == null)
              return new ErrorString( "null value");
           String S ;
           if( O instanceof Vector){
             if(((Vector)O).size() > line)
                S = ((Vector)O).elementAt( line).toString();
             else
                return new ErrorString( FileIO.NO_MORE_DATA );
           }
           else if( O.getClass().isArray( ))
             if( Array.getLength( O ) > line)
                 S =""+ Array.get(O,line).toString();
             else
                return new ErrorString( FileIO.NO_MORE_DATA );
           else if( line > 0)
              return new ErrorString( FileIO.NO_MORE_DATA );   
           else
             S = O.toString();
              
           
           return DataSetTools.util.Format.string( S ,width, left);
        }
       boolean ReadReturn = false;

       public boolean hasReadReturn(){

         return ReadReturn;

       }
       public Object read(FileInputStream fin ) throws IOException{
         String S ="";
         ReadReturn = false;
         if( width < 0){
           int c  = fin.read();;
           while( (c <= 32) && (c != -1))
              c =(char)fin.read();
           if( c == -1)
              return new ErrorString( FileIO.NO_MORE_DATA);
           boolean quotes = false;
           if( ((char)c) == '\"')
              quotes = true;
           if( !quotes) S = S+(char)c;
           c= fin.read();
           while( ((c > 32) && !quotes) &&( c!= -1) && 
                 ( (((char)c)!='\"') &&quotes  )  ){
              S+=(char)c;
              c = fin.read();
           }
           if( c < 32) ReadReturn = true;
         }else{
           S ="";
           int c =fin.read();
           int nn = 0;
           while( (c <= 32) && (c != -1))
              c =(char)fin.read();
           if( c == -1)
              return new ErrorString( FileIO.NO_MORE_DATA);
           while( (c >=32) && nn < width){
              S +=(char)c;
              nn++;
              if( nn< width)c=fin.read();
           }
          if( c< 32) ReadReturn = true;
         }
         return S;

       }

     }


    /**  
     *    An FOps that handles the the new line, / , format specifier
     */
    class newLineOp implements FOps{
    
        public newLineOp(){
        }
        public Object write( Object O, int line){
           return "\n";
        }

       public boolean hasReadReturn(){
           return false;
       }
       public Object read(FileInputStream fin )throws IOException{
          int c = fin.read();
          while( (c != -1)&& ( ((char)c) != '\n') )
             c = fin.read();
          if( c == -1)
            return new ErrorString( FileIO.NO_MORE_DATA);
          return null;
       }

     }


    /**  
     *    An FOps that handles a format specifier that is not legitimate
     */
     class ErrorOp implements FOps{
    
        public ErrorOp(){
        }  
        public Object write( Object O, int line){
           return new ErrorString("Improper Format specifier");

        }
       public boolean hasReadReturn(){return false;}

       public Object read(FileInputStream fin )throws IOException{
           return new ErrorString( "Improper Format specifier");
       }

     }
    
    /**  
     *    An FOps that handles the B format specifier that sets everything to Free Form mode
     */
     class LineBreakModeOp implements FOps{
    
        public LineBreakModeOp(){
        }
        public Object write( Object O, int line){
           return new ErrorString("Improper Format specifier for write");

        }
        public boolean hasReadReturn(){return false;}
       public Object read(FileInputStream fin )throws IOException{
            return "OK";
       }

     }
    
