/*
 * File:  NxNodeUtils.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.10  2002/06/19 15:55:22  rmikk
 * Fixed the order of Date formats so that the seconds, when
 * there, is found
 *
 * Revision 1.9  2002/04/01 20:45:41  rmikk
 * Fixed Date Format exception report in jdk1.4
 * Added some support for the Nexus NXChar type
 *
 * Revision 1.8  2002/02/26 15:46:41  rmikk
 * Fixed the utility Showw routine
 * Added a utility routine to getConversion factors
 *
 * Revision 1.7  2001/08/10 19:58:47  rmikk
 * Added Constant strings to refer to a few errors
 *
 * Revision 1.6  2001/08/09 16:44:56  rmikk
 * Eliminated System.out's in the Showw routines
 *
 * Revision 1.5  2001/07/26 20:51:19  rmikk
 * Used StringFromBytes Method to fix titles, etc.
 *
 * Revision 1.4  2001/07/25 21:18:30  rmikk
 * Added features to Showw vectors
 *
 * Revision 1.3  2001/07/24 20:11:12  rmikk
 * Eliminated a debug print
 *
 * Revision 1.2  2001/07/17 15:05:19  rmikk
 * Updated Show for arbitrary dim arrays and for large
 *    arrays(truncated after 100 entries)
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;
import neutron.nexus.*;
import java.lang.*;
import java.io.*;
import java.text.*;
import java.util.*;

/** Utilities for inputting data from various datasources
 */
public class NxNodeUtils
{String errormessage = "";
    // ERROR CODES
    public static final String ER_BADFILE    ="Bad File name";
    public static final String ER_OPEN       ="Cannot open or Connect";
   
    
    private void showConst()
   {     System.out.println( "CHAR="+NexusFile.NX_CHAR );
         System.out.println( "FLOAT32="+NexusFile.NX_FLOAT32 );
          
	System.out.println( "FLOAT64="+NexusFile.NX_FLOAT64 );
           
	System.out.println( "INT16="+NexusFile.NX_INT16 );
            ;
  	System.out.println( "INT32="+NexusFile.NX_INT32 );
          
	 System.out.println( "INT8="+NexusFile.NX_INT8 ); 
          
	System.out.println( "UINT16="+NexusFile.NX_UINT16 );
           
	System.out.println( "UINT32="+ NexusFile.NX_UINT32 );
           
	 System.out.println( "UINT8="+NexusFile.NX_UINT8 );
          
   }
    /**
   * Attempts to parse a date string with various formats including
   * - ,/, and . separators for month,year,day specifiers
   */
 public static Date parse( String DateString )
     {Date Result;
      SimpleDateFormat fmt = new SimpleDateFormat();
      fmt.setLenient( true );

      String Date_formats[] = {"yyyy-MM-dd" , "yyyy.MM.dd" , 
                               //"yyyy-MMM-dd" , "yyyy.MMM.dd" , 
                               "yyyy-M-d" , "yyyy.M.d" , "yy.MM.dd" , "yy.N.d" , 
                               "MM/dd/yyyy" ,  "MM/dd/yy" ,  "M/d/yyyy" ,  "M/d/yy" , 
                               "MMM/dd/yyyy" , "MMM/d/yyyy" , "MMM/dd/yy" , "MMM/d/yy",
                               "dd-MMM-yyyy","dd/MMM/yyyy", "dd-MMM-yy","d/MMM/yy",
                                "MMM dd,yyyy", };
                              
                              
      String Time_formats[] =  { "HH:mm:ss", "HH:mm" , "hh:mm a" , "hh:mma" , 
                                "H:mm:ss" ,"H:mm" ,  "h:mm a" , "h:mma" ,  
                                 "H:m:ss" , "H:m" , "h:m a" , "h:ma", "" };
     
      for( int i = 0; i<Date_formats.length; i++ )    
          {         
           for( int k = 0; k<Time_formats.length; k++ )
             {String pattern = Date_formats[ i]+" "+Time_formats[  k ];
             pattern= pattern.trim();              
             
              try{
                  fmt.applyPattern(  pattern  );
                 Result = fmt.parse( DateString );
                 if( Result != null )
		     {//System.out.println("format="+pattern);
                   return Result;
                  }
                 }
               catch( Exception s )
                 {}
           /*    fmt.applyPattern( Date_formats[ i]    );
              try{
                 Result = fmt.parse( DateString );
                 if( Result != null )
                  {System.out.println("format="+pattern);
                   return Result;
                  }
                 }
               catch( ParseException s )
                 {}
            */
              }        
        }//for i
       return null;

     }
    /**  Attempts to create an array of the given type and length
   *@param type  the NexusFile type
   *@param length  the length of the array
   *@return an array of the appropriate type and length or null
   */
 public Object CreateArray( int type ,  int length )
   { errormessage = "";
     Object X;
     if( type == NexusFile.NX_FLOAT32 )
             X = new float[ length];
     else if( type == NexusFile.NX_CHAR )
             X = new byte[ length];
      else if( type == NexusFile.NX_FLOAT64 )
             X = new double[ length];
	else if( type == NexusFile.NX_INT16 )
             X = new short[ length];
  	else if( type == NexusFile.NX_INT32 )
             X = new int[ length];
	else if( type == NexusFile.NX_INT8 ) 
            X = new byte[ length];
	else if( type == NexusFile.NX_UINT16 )
            X = new short[ length];
	else if( type == NexusFile.NX_UINT32 )
            X = new int[ length];
	else if( type == NexusFile.NX_UINT8 )
            X = new byte[ length];
        else
            {errormessage = "CAtype not supported"+type;
            return null; 
           }
        return X;
      }

 /** Returns error message or "" if none
*/
   public String getErrorMessage()
     {return errormessage;
     }

 /** Fixes arrays whose type is Unsigned or NX_CHAR to an appropriate type
 */
  public Object fixUnsignedArray( Object X , int type , int length )
    {  errormessage="";
        if( type == NexusFile.NX_FLOAT32 )
            return X;
	else if( type == NexusFile.NX_FLOAT64 )
            return X;
	else if( type == NexusFile.NX_INT16 )
             return X;
  	else if( type == NexusFile.NX_INT32 )
            return X;
	else if( type == NexusFile.NX_INT8 ) 
            return X;
        else if( type == NexusFile.NX_CHAR )
            {//yte u[]; u =X;
             return X;
             //return DataSetTools.nexus.NexusUtils.StringFromBytes( u );
	     /*if( u== null) return null;
             if( u.length < 1)return "";
            
             int offset =u.length;
             String SS;
             if( u[ u.length -1] != (byte)0)
               SS =new String( (byte[])X);
             else
               SS = new String( u, 0, u.length-1);
             return ( SS );
	     */
            }

	else if( type == NexusFile.NX_UINT16 )
           { int u[] = new int[ length];
             int  x;
             for(int i = 0; i<length;i++ )
                 {x = ( ( short[] )X )[ i];
                 if( x>= 0 )
                   u[ i] = x;
                 else 
                   u[ i] = 2*( ( int )java.lang.Short.MAX_VALUE+1 )+x;
               
                 }
             return u;
           }
	else if( type == NexusFile.NX_UINT32 )
            {long u[] = new long[ length];
             long  x;
             for( int i = 0; i<length;i++ )
                 {x = ( ( int[] )X )[ i];
                 if( x>= 0 )
                   u[ i] = x;
                 else 
                   {long y = ( long )( java.lang.Integer.MAX_VALUE );
                    u[ i]= 2*( long )( y+1 ) + ( long )x;
                   }
		 // if(i == 0 )System.out.println("x="+x+","+u[ i]+","+
                 //                    java.lang.Integer.MAX_VALUE );
                 }
             return u;
            
            }
	else if( type == NexusFile.NX_UINT8 )
           {short u[] = new short[ length];
            short x;
             for( int i = 0; i<length;i++ )
                 {x = ( ( byte[] )X )[ i];
                 if( x>= 0 )
                   u[ i] = x;
                 else 
                   {short y = java.lang.Byte.MAX_VALUE;
                    y++;
                    y = ( short )( y+y );                    
                    y = ( short )( y + x ) ;
                    u[ i] = y; //(byte )2*(java.lang.Byte.MAX_VALUE+(byte )1 ) + (byte )x;
                   }
                 }
           
            return u;
           }
        else
            {errormessage = "fUtype not supported"+type;
            return null; 
             } 
    }  //fixUnsignedArray

/** Utility to view array objects
*/
public String ShowwArr( Object X )
  {String Res="["; 
  //System.out.println("in ShowArr"+X.getClass());

   if( X instanceof int[]) 
    {int u[] = (int[])X;
     //Res =("int"+u.length+":")+Res;
     for( int i = 0; i < java.lang.Math.min(100, u.length); i++)    
      {Res =Res+u[i];
       if( i<u.length-1)Res = Res+",";
      }     
     if( u.length >100)
         Res += (",......");
     
    // System.out.println("int length="+u.length);
     }
   else if( X instanceof char[])
      {return new String( (char[])X);
        }
   else if( X instanceof short[])
    {short u[] = (short[])X;
     //Res =("short"+u.length+":")+Res;
     for( int i = 0; i <java.lang.Math.min(100, u.length); i++)    
      {Res =Res+u[i];
       if( i<u.length-1)Res = Res+",";
      } 
         if( u.length >100)
           Res += (",......");
     //System.out.println("short length="+u.length);
     }
   else if( X instanceof Object[])
    {Object u[] = (Object[])X;
     //Res =("Object"+u.length+":")+Res;
     for( int i = 0; i < java.lang.Math.min(100, u.length); i++)        
      {Res =Res+Showw(u[i]);
       if( i<u.length-1)Res = Res+",";
      } 
         if( u.length >100)
          Res += (",......");
      //System.out.println("short length="+u.length);  
     } 
    else if( X instanceof byte[])
    {byte u[] = (byte[])X;
     //Res=("byte"+u.length+":")+Res;
     for( int i = 0; i < java.lang.Math.min(100, u.length); i++)     
      {Res =Res+(char)u[i];
       if( i<u.length-1)Res = Res+",";
      }
        if( u.length >100)
          Res += (",......");
      //System.out.println("byte length="+u.length);    
     } 
   else if( X instanceof long[]) 
    {long u[] = (long[])X;
     //Res =("long"+u.length+":")+Res;
     for( int i = 0; i < java.lang.Math.min(100, u.length); i++)    
      {Res =Res+u[i];
       if( i<u.length-1)Res = Res+",";
      } 
         if( u.length >100)
            Res+=(",......");
     //System.out.println("long length="+u.length);        
     }
   else if( X instanceof float[])
    {float u[] = (float[])X;
    // Res= "float"+u.length+":"+Res;
     for( int i = 0; i < java.lang.Math.min(100, u.length); i++)    
      {Res =Res+u[i];
       if( i<u.length-1)Res = Res+",";
      }
       if( u.length >100)
          Res += (",......"); 
     //System.out.println("float length="+u.length);    
     } 
   else if( X instanceof double[]) 
    {double u[] = (double[])X;
       //Res=("double"+u.length+":")+Res;
     for( int i = 0; i < java.lang.Math.min(100, u.length); i++)    
      {Res =Res+u[i];
       if( i<u.length-1)Res = Res+",";
      } 
        if( u.length >100)
          Res += (",......");
     //System.out.println("double length="+u.length); 
    }   
   else if( X instanceof Vector )
     {int n=((Vector)X).size();
      for( int i = 0; i < java.lang.Math.min( 100, n ); i++)
       { Res =Res+Showw( ((Vector)X).elementAt( i ));
       if( i<n-1)Res = Res+",";
        } 
       if( n  >=100)
          Res += (",......");
      //System.out.println("Vector length="+ n);  
     } 
     
   else 
    {//System.out.println("SHnot supported"+X.getClass());
     return X.toString();
    }
   return Res+"]";
     
  }
/** Shows the value of many, many types of Objects
*/
public String Showw( Object X  )
  {/*if( X instanceof Integer )
      return X.toString();
   else if( X instanceof Float )
      return X.toString();
   else  if( X instanceof Short )
      return X.toString();
  else if( X instanceof String )
     return (String)X;
  else if( X == null )
      return "(null)";
   else return ShowwArr( X );
  */
   if( X == null) return "(null)";
   if( X.getClass().isArray())
     return ShowwArr(X);
   if( X instanceof Vector)
     return ShowwArr(X);
   return X.toString();
  
   }
/** test array for parse dates
*/
 public static void main( String args[])
  {Object X;
  String[] ss={"abc","cde","efg"};
  System.out.println("String Arry="+new NxNodeUtils().Showw(ss));
   NxNodeUtils NU= new NxNodeUtils();
   Calendar C = new GregorianCalendar();
   while(true)
    { System.out.println("Enter String form for a data");
      char c=0;
      String S ="";
      try{
          while( c<32)
            c=(char) System.in.read();
           while( c>=32)
             {S = S+c;
              c=(char)System.in.read();
             }
         }
       catch(Exception s){}
       Date D = NU.parse( S);
       if( D == null)
         System.out.println("Result is null");
       else
         {System.out.println("Result is"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(D));
          System.out.println("Date.toString="+D);
          System.out.println("year="+D.getYear());
          C.setTime(D);
          int year = C.get(Calendar.YEAR);
          if( year <500) 
              {C.set(Calendar.YEAR, year+1900);
               }
             
          System.out.println( "cal Year="+C.get(Calendar.YEAR)+","+C.get(Calendar.MONTH));
          }


    }
   }
/** Gives Factor to mulitply OldUnits to get NewUnits(ISAW units)
*/
public static float getConversionFactor( String OldUnits, String NewUnits)
  { if( NewUnits.equals("radians"))
          return AngleConversionFactor( OldUnits.trim());
    if( NewUnits.equals("meters"))
          return LengthConversionFactor(OldUnits.trim());
    if(NewUnits.equals("Kelvin"))
          return TempConversionFactor(OldUnits.trim());
    //Solid angle
    if( NewUnits.equals("second")) return TimeConversionFactor( OldUnits.trim() );
    if( NewUnits.equals("grams")) return MassConversionFactor( OldUnits.trim());
    if( NewUnits.equals("Mev")) return EnergyConversionFactor( OldUnits .trim());
    
    else return 1.0f;
  }

public static float AngleConversionFactor(String OldUnits) //base radians
  {boolean hasDegree=false;
   
   if(";rad;radian;r;".indexOf(";"+OldUnits+";")>=0)return 1.0f;
   if(";deg;degree;degrees;d;".indexOf(";"+OldUnits+";")>=0) return (float)(java.lang.Math.PI/180.0);
   return 1.0f;
 
  }
public static float LengthConversionFactor(String OldUnits)//base m
  { if( "m;meter;met;".indexOf(OldUnits+";")>=0) return 1;
    if("cm;centim;centimeter;cmeter;cmet;100mm;100millim;100millimeter;".indexOf(OldUnits+";")>=0)
            return .01f;
    if("mm;millim;millimeter;100um;100microm;".indexOf(OldUnits+";")>=0)return .001f;
    if("um;umet;umeter;umeters;".indexOf(OldUnits)>=0) return .000001f;
 
    if("in;inch;".indexOf(OldUnits+";")>=0) return (float)(1.0/254.0);
    if("ft;foot;feet;".indexOf(OldUnits+";")>=0)return (float)(12.0/254.0);
    return 1.0f;
    
  }
public static float TempConversionFactor(String OldUnits)//base Kelvin
  { if("kelvin;k;".indexOf(OldUnits.toLowerCase()+";")>=0)return 1.0f;
    if( "deg;degrees;d;".indexOf(OldUnits+";")>=0) return (float)(100.0/212.0);
    return 1.0f;
  }
public static float TimeConversionFactor(String OldUnits)//seconds
  {return 1.0f;
  }
public static float MassConversionFactor(String OldUnits)//grams
  {return 1.0f;
  }
public static float EnergyConversionFactor(String OldUnits)//Mev
  {return 1.0f;
  }
/** Test program for NxNodeUtils.java
*/
public static void main1( String args[] )
 {Object X;

  
 NxNodeUtils NU= new NxNodeUtils();
  System.out.println( "Byte="+java.lang.Byte.MAX_VALUE );
 System.out.println( "Short="+java.lang.Short.MAX_VALUE );
 System.out.println( "int="+java.lang.Integer.MAX_VALUE );
 System.out.println( "long="+java.lang.Long.MAX_VALUE );
  char c = 0;
  X = null;
 NU.showConst();
  c='x';
  while( c!= 'x' )
   { System.out.println( "Enter option desired" );
     System.out.println( "   1. Create int array" );
    System.out.println( "     2. Create float array" );
    System.out.println( "     3. Create byte array" );
    System.out.println( "     4. Create short array" );
    System.out.println( "     5. fix unsigned int array" );
    System.out.println( "      6. show fixed array" );
    System.out.println( "     7.show error" );

   c = 0;
   try{
    while( c< 32 )
     c = ( char )System.in.read();
     }
   catch( IOException s ){c = 0;}
   if( c == '1' )
    { X = NU.CreateArray( NexusFile.NX_INT32 , 10 );
     for( int i = 0;i<10;i++ ) ( ( int[] )X )[ i] = ( int )i;
    }
   else if(  c == '2' )
    { X = NU.CreateArray(  NexusFile.NX_FLOAT32 , 10 );
     for( int i = 0;i<10;i++ ) ( ( float[ ] )X )[ i] = i;
    }
   else if(  c == '3' )
    { X = NU.CreateArray( NexusFile.NX_INT8 , 10 );
     for( int i = 0;i<10;i++ ) ( ( long[] )X )[ i] = i;
    }
   else if(  c == '4' )
    { X = NU.CreateArray( NexusFile.NX_INT16 , 10 );
     for( int i = 0;i<10;i++ ) ( ( short[] )X )[ i] = ( short )i;
    }
   else if( c == '5' )
    { X = NU.CreateArray( NexusFile.NX_UINT32 , 10 );
     for( int i = 0;i<10;i++ ) ( ( int[] )X )[ i] = i;
     ( ( int[] )X )[ 0] = -3000;
     X =  NU.fixUnsignedArray( X , NexusFile.NX_UINT32 , 10 );
    }
   else if( c == '6' ) 
    {System.out.println( NU.Showw( X ) );
    }
   else if( c == '7' )
     System.out.println(  NU.getErrorMessage( ) );



   }


 }

}

