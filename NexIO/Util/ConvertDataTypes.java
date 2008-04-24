
/*
 * File:  ConvertDataTypes.java
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see  <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.15  2007/08/16 17:37:25  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.14  2007/07/11 18:23:17  rmikk
 * Added a lot of documentation
 *
 * Revision 1.13  2007/06/28 15:25:00  rmikk
 * Eliminated a debug print
 *
 * Revision 1.12  2006/11/14 16:26:22  rmikk
 * Added a routine to parse a date that is in the full ISO format
 *
 * Revision 1.11  2006/10/22 18:15:47  rmikk
 * Convert to float Array and int Array now converts lists of strings separated
 *   by spaces, comma, or semicolon to the corresponding array, if possible
 *
 * Revision 1.10  2006/07/25 00:04:28  rmikk
 * Added metre as a unit.
 * checked cm  before m
 *
 * Revision 1.9  2005/03/23 01:56:49  dennis
 * Removed unnecessary semicolons.
 *
 * Revision 1.8  2004/12/23 13:25:57  rmikk
 * Fixed indentations and spacings between lines
 *
 * Revision 1.7  2004/03/15 19:37:54  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.6  2004/03/15 03:36:02  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.5  2004/03/06 19:10:17  rmikk
 * Convert to float Array now attempts to convert Vectors to float Arrays
 *
 * Revision 1.4  2004/02/16 02:18:48  bouzekc
 * Removed unused imports.
 *
 * Revision 1.3  2003/12/12 17:29:19  rmikk
 * Fixed an error that assumed ALL strings end in (char)0
 *
 * Revision 1.2  2003/11/23 23:49:22  rmikk
 * Implemented the method CreateDetectorPositionAttribute
 *
 * Revision 1.1  2003/11/16 21:46:43  rmikk
 * Initial Checkin
 *
 */

package NexIO.Util;
import NexIO.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.MathTools.Geometry.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

/**
*    This class contains methods to convert Objects to a specified
*    data type(including dates ), Change units for data, convert Nexus axis 
*    to Isaw axes.
*    Also, there are routines that pass through null values without causing
*    null pointer exceptions.  These save programming lines.
*/
public class ConvertDataTypes{

  /**
   *  returns a float array corresponding to the object O or null if not possible.
   *  String values are converted if possible.
   *  
   *  @param O  the object to be converted or null
   *  
   *  @return  the float Array value corresponding to the object or null if
   *           it cannot be converted.
   */
  public static float[] floatArrayValue( Object O ){
     
     if( O == null )
        return null;
     
     try{
        
        if( O.getClass().isArray() ){
           
           if( O instanceof float[] )
              return (float[] )O;
           
           if( O instanceof int[] )
              return intArray2floatArray( (int[])O );
           
           if( O instanceof double[] )
              return doubleArray2floatArray( (double[])O );
           
           if( O instanceof long[] )
              return longArray2floatArray( (long[])O );
           
           if( O instanceof short[] )
              return shortArray2floatArray( (short[])O );
           
           float[] Res = new float[ Array.getLength( O ) ];
           for( int i = 0; i < Res.length; i++ ){
              
              Object El = Array.get( O, i );
              if( El instanceof Number )
                 Res[ i ] = ( (Number)El ).floatValue();
              else if( El instanceof String )
                 Res[ i ] = ( new Float( (String)El ) ).floatValue(); 
           }
           return Res; 
           
        }else if( O instanceof Number ){
           
           float[] Res = new float[ 1 ];
           Res[ 0 ] = ( (Number)O ).floatValue();
           return Res;
           
        }else if( O instanceof String ){
           
           String[] substrings = ( (String)O ).trim().split( "(\\s*[,;]\\s*)|\\s+" );
           
           if( substrings == null )
              return null;
           
           float[] Res = new float[ substrings.length ];
           
           for( int i= 0; i < Res.length ; i++ )
              Res[ i ] = ( new Float( substrings[ i ] ) ).floatValue();
          
           return Res;
  
  
        }else if( O instanceof Vector ){
            return floatArrayValue( ( (Vector)O).toArray() );
  
        }
        
     }catch( Exception S ){}
     
     return null;

        
  }
  
  /**
   * Converts an integer array to a float array. Fast Utility method.
   * @param data The integer array
   * @return   The corresponding float array
   */
  public static float[] intArray2floatArray( int[] data ){
     
     if( data == null )
        return null;
     
     float[] Res = new float[ data.length ];
     
     for( int i = 0; i < data.length; i++ )
        Res[ i ] = data[ i ];
     
     return Res;
     
  } 
  
  
  /**
   * Converts a  double array to a float array. Fast Utility method.
   * @param data The double array
   * @return   The corresponding float array
   */
  public static float[] doubleArray2floatArray( double[] data ){
     
     if( data == null )
        return null;
     
     float[] Res = new float[ data.length ];
     
     for( int i = 0; i < data.length; i++ )
        Res[ i ] = (float)data[ i ];
     
     return Res;
  }
  
  
  /**
   * Converts an long array to a float array. Fast Utility method.
   * @param data The long array
   * @return   The corresponding float array
   */
  public static float[] longArray2floatArray( long[] data ){
     
     if( data == null )
        return null;
     
     float[] Res = new float[ data.length ];
     
     for( int i = 0; i < data.length; i++ )
        Res[ i ] = data[ i ];
     
     return Res;
  }
  
  
  
  /**
   * Converts an short array to a float array. Fast Utility method.
   * @param data The array of shorts
   * @return   The corresponding float array
   */
  public static float[] shortArray2floatArray( short[] data ){
     
     if( data == null )
        return null;
     
     float[] Res = new float[ data.length ];
     
     for( int i = 0; i < data.length; i++ )
        Res[ i ] = data[ i ];
     
     return Res;
  }
  
 
 
  /**
   *  returns a int array corresponding to the object O or null if not possible.
   *  String values are converted if possible.
   *  
   *  @param O  the object to be converted or null
   *  
   *  @return  the int Array value corresponding to the object or null if
   *           it cannot be converted.
   */
  public static int[] intArrayValue( Object O ) {
     
     if( O == null )
        return null;
        
     try{
        
        if( O.getClass().isArray() ){
          
           int[] Res = new int[ Array.getLength( O ) ];
           for( int i = 0; i < Res.length; i++ ){
             
              Object El = Array.get( O, i );
              if( El instanceof Number )              
                 Res[ i ] = ( (Number)El ).intValue();
                 
              else if( El instanceof String )              
                 Res[ i ] = ( new Integer( (String)El ) ).intValue(); 
                 
           }
           return Res; 
           
        }else if( O instanceof Number ){
          
           int[] Res = new int[ 1 ];
           Res[ 0 ] = ( (Number)O ).intValue();
           return Res;
           
        }else if( O instanceof String ){
           
           String[] substrings = ( (String)O).trim().split( "(\\s*[,;]\\s*)|\\s+" );
           
           if( substrings == null )
              return null;
           
           int[] Res = new int[ substrings.length ];
           
           for( int i = 0; i < Res.length ; i++ )
              Res[ i ] = ( new Integer( substrings[ i ] ) ).intValue();
          
           return Res;
  
        }
     }catch( Exception S ){}
     
     return null;

    
   }

  /**
   *  returns a float corresponding to the object O or null if not possible.
   *  String values are converted if possible.
   *  
   *  @param O  the object to be converted or null
   *  
   *  @return  the float value corresponding to the object or Float.NaN if
   *           it cannot be converted.
   */
  public static float  floatValue( Object O ){
     float[] Res = floatArrayValue( O );
     
     if( Res == null )
        return Float.NaN;
        
     if( Res.length < 1 )
        return Float.NaN;
        
     return Res[ 0 ];

  }

  /**
   *  returns a int corresponding to the object O or null if not possible.
   *  String values are converted if possible.
   *  
   *  @param O  the object to be converted or null
   *  
   *  @return  the int value corresponding to the object or Integer.MIN_VALUE
   *            if it cannot be converted.
   */
  public static int intValue( Object O ){
     int[] Res = intArrayValue( O );
     
     if( Res == null )
        return java.lang.Integer.MIN_VALUE;
        
     if( Res.length < 1 )
        return java.lang.Integer.MIN_VALUE;
        
     return Res[ 0 ];

  }

  
  
  private static char[] cnvertTochar( byte[] L ){
     if( L == null )
        return null;
        
     char[] Res = new char[ L.length ];
     for( int i = 0; i < L.length; i++ )
        Res[ i ] = (char)L[ i ];
        
     return Res;

  }
  
  
  
  private static int findNext( char[] L, char[] F, int start ){
    
     for( int i = start; ( i >= 0 ) && ( i < L.length ); i++ ){
       
        boolean match = true;
        for( int j = 0; ( j < F.length ) && match; j++ )
        
           if( i + j > L.length )
              match = false;
              
           else if( L[ i + j ] != F[ j ] )
              match = false;
              
        if( match ) 
           return i;
     }
     
     return -1;
  }
  
  
  
  /** end is not included
  */
  private static int countt( char[]L, char [] F, int start, int end ){
     int n = 0;
     for( int i = start; ( i >= 0 ) && ( i < end ); i++ ){
       
        boolean match = true;
        for( int j = 0; ( j < F.length ) && match ; j++ )
        
           if( i + j > L.length )           
             match = false;
             
           else if( L[ i + j ] != F[ j ] )            
              match = false;
              
        if( match )
           n++;
     }
     
     return n;
  }


  /**
   *  Converts a char array containing a concatenation of null terminated 
   *  strings to a String array 
   *  
   *  @param L  the char array or null
   *  
   *  @return  an array of Strings corresponding to the null terminated char
   *           sequences in L
   */
  public static String[] charArraystoStrings( char[] L ){
     
    if( L == null )
       return null;
    
    char[] two0 = new char[ 2 ];
    two0[ 0 ] = 0; 
    two0[ 1 ] = 0;
    
    char[] one0 = new char[ 1 ];
    one0[ 0 ] = 0;
    int n = countt( L, one0, 0, L.length );
    if( n <= 0 )
      n++;
    
    String[] Res = new String[ n ];
    int k = 0;
    
    for( int i = 0; i < n; i++ ){
      
       int k1 = findNext( L, one0, k );
       if( k1 < 0 ) 
          k1 = L.length;
          
       Res[ i ] = new String ( L, k, k1 - k );
       k = k1 + 1;
    }
    
   return Res;
  }

  /**
   *  returns a String array corresponding to the object O or null if not possible.
   *  String values are converted if possible.
   *  
   *  @param O  the object to be converted or null
   *  
   *  @return  the String Array value corresponding to the object or null if
   *           it cannot be converted.
   */
  public static String[] StringArrayValue( Object O ){
     if( O == null )
        return null;
        
     if( O instanceof char[] ){
        return charArraystoStrings( (char[])O );

     }
     
     if( O instanceof byte[] )
        return charArraystoStrings( cnvertTochar( (byte[])O));
        
     else if( O.getClass().isArray() ){
       
        String[] Res = new String[ Array.getLength( O ) ];
        for( int i = 0; i < Res.length; i++ ){
          
           Object El = Array.get( O, i );
           Res[ i ] = El.toString();
        }
        
        return Res; 
        
     }else {
       
        String[] Res = new String[ 1 ];
        Res[ 0 ] = O.toString();
        return Res;
     }
  }

  /**
   *  returns a String corresponding to the object O or null if not possible.
   *  
   *  @param O  the object to be converted or null
   *  
   *  @return  the String value corresponding to the object or null if
   *           it cannot be converted.
   */
  public static String StringValue( Object O ){
     if( O == null ) 
        return null;
        
     String[] X = StringArrayValue( O );
     
     if( X == null )
       return null;
       
     if( X.length < 1 )
       return null;
       
     return X[ 0 ]; 

  }


  /**
   * Gives Factor to mulitply OldUnits to get NewUnits(ISAW units )
   * 
   * @param  OldUnits  the units that are non ISAW units
   * 
   * @param StdUnits  the ISAW units. Must be radians, meters,Kelvin,us,grams,
   *                   Mev,or steradian
   *                   
   * @return the factor to multiply a quantity in old units to get to the new units
   
   */
  public static float getUnitMultiplier( String OldUnits, String StdUnits ){
     
    String NewUnits = StdUnits;
    
    if( NewUnits.equals( "radians" ) )
      return AngleConversionFactor( OldUnits.trim() );
    
    if( NewUnits.equals( "meters" ) )
      return LengthConversionFactor( OldUnits.trim() );
    
    if( NewUnits.equals( "Kelvin" ) )
      return TempConversionFactor( OldUnits.trim() );

    //Solid angle
    if( NewUnits.equals( "us" ) )
      return TimeConversionFactor( OldUnits.trim() );

    if( NewUnits.equals( "grams" ) )
      return MassConversionFactor( OldUnits.trim() );
    
    if( NewUnits.equals( "Mev" ) )
      return EnergyConversionFactor( OldUnits.trim() );

    if( NewUnits.equals("steradian" ) )  //"sr is another symbol but if not this
       return 1.0f;                    //??????????????????????????????????
    
    else return 1.0f;
  }

  
  
  /**
   *   Adjusts all values in an array to the proper units
   *   
   *   @param  d   the array to be adjusted
   *   
   *   @param oldUnits  the units that the array is currently in
   *   
   *   @param StdUnits  the Isaw Standard units{must be radians, meters,Kelvin,   *                                                    
   *                   us,grams,Mev,or steradian}
   *                   
   *   @param mult   should be 1 unless StdUnits are not desired. The conversion
   *                 factor to StdUnits is multiplied by mult
   *                 
   *   @param add    should be 0 unless StdUnits are not desired. The result after
   *                 multiplying conversion factor by mult has this quantity added to
   *                 it.(For Temperature)
   */
  public static void UnitsAdjust( float[] d, String oldUnits, String StdUnits, 
                 float mult, float add ){ //y = mx+b
     
    if( oldUnits == null )
      return ;
      
    if( d == null )
      return;
      
    if( d.length < 1 )
      return;

    float f = NxNodeUtils.getConversionFactor( oldUnits, StdUnits );

    if( f == Float.NaN )
      return;
    
    for( int i = 0; i < d.length; i++ )
      d[ i ] = mult * f * d[ i ] + add;
      
  } 
  

  /**
   * Converts the Nexus position to ISaw positiom
   * 
   * @param distance  the distance from the sample
   * 
   * @param phi  The angle from the McStas z axis(bean direction) to the position 
   *       vector
   * 
   * @param theta  the angle the position vector projected to the plane perpedicular
   *         to the z axis makes with the positive x=axis in this plane
   *         
   * @return  the ISAW detector position corresponding to the given position 
   */
  public static DetectorPosition convertToIsaw( float distance,
                                             float phi,
                                             float theta ){
                                               
     DetectorPosition dp = new DetectorPosition();
     float[] f = Types.convertFromNexus( distance, phi, theta );
     dp.setSphericalCoords( f[ 0 ], f[ 2 ], f[ 1 ] );
     return dp;
  }


  /**
   *  @see NexIO.NxNodeUtils#parse(String )
  */
  public static java.util.Date parse( java.lang.String DateString ){
    
     return NxNodeUtils.parse( DateString );
  }
  
  public static java.util.Date parse2Date( java.lang.String DateString ){
     
     long T = parse_new( DateString );
     if( T < 0 )
        return null;
     

     GregorianCalendar GCal = new GregorianCalendar();
     GCal.setTimeInMillis( T );
     return GCal.getTime();
  }
  
  
  private static String Year = "[12][0-9][0-9][0-9]";
  private static String MonthDay = "([0-1][0-9])[ \\-:/\\\\]([0-3][0-9])";
  private static String Time = "([0-5][0-9])[ \\-:]([0-5][0-9])[ \\-:]([0-5][0-9])([.][0-9]*)?";
  private static String Zone = "[+\\-]([0-2][0-9])[:]?([0-5][0-9])";
  private static String pattern = "(("+Year+")[ \\-:/\\\\]("+MonthDay+"))(([ T]"+Time+")("+Zone+")?)?";
  
  
  private static long parse_neww( String DateString ){
     
     Matcher M = Pattern.compile( pattern ).matcher( DateString );
     if( !M.find() )
        return -1;
     
     for( int i = 0; i <= M.groupCount(); i++ )
        System.out.println( i + "::"  + M.group( i ) );
     
     return 0;
     
  }
  /**
   * parses ISO dates only. They can have fractional seconds
   * 
   * @param DateString  The String representing the Date
   * 
   * @return  The time in milliseconds (GMT)
   */
  public static long parse_new( String DateString ) {

      if( DateString == null ) 
         return - 1;

      DateString = DateString.trim();
      int year = - 1;
      int month = - 1;
      int day = - 1;
      int hour = 0;
      int minute = 0;
      int second = 0;
      float frac_second = 0;

      // ----- Find Year ----------
      Pattern P1 = Pattern.compile( "[12][09][0-9][0-9][ :\\-/]" );
      Matcher M = P1.matcher( DateString );

      if( ! M.find() ) 
         return - 1;

      year = ( new Integer( DateString.substring( 0 , 4 ) ) ).intValue();


      // ----- Find Month ----------
      DateString = DateString.substring( 5 );
      if( DateString.length() < 1 ) 
         return - 1;

      M = Pattern.compile( "[0-1][0-9][ :\\-/]" ).matcher( DateString );
      if( ! M.find() ) 
         return - 1;

      month = ( new Integer( DateString.substring( 0 , 2 ) ) ).intValue();


      // ----- Find Day ----------

      DateString = DateString.substring( 3 );
      if( DateString.length() < 1 ) 
         return - 1;


      M = Pattern.compile( "[0-1][0-9][ :\\-/T]" ).matcher( DateString );
      if( ! M.find() ) 
         return - 1;

      day = ( new Integer( DateString.substring( 0 , 2 ) ) ).intValue();


      // ----- Find Hour, minute, and second ----------
      DateString = DateString.substring( 3 );
      if( DateString.length() > 1 ) {
         M = Pattern.compile( "[0-2][0-9][ :\\-/]" ).matcher( DateString );
         if( M.find() ) {

            hour = ( new Integer( DateString.substring( 0 , 2 ) ) ).intValue();

            DateString = DateString.substring( 3 );
            if( DateString.length() > 1 ) {

               M = Pattern.compile( "[0-2][0-9][ :\\-/]" ).matcher( DateString );
               if( M.find() ) {

                  minute = ( new Integer( DateString.substring( 0 , 2 ) ) )
                           .intValue();


                  DateString = DateString.substring( 3 );
                  if( DateString.length() > 1 ) {

                     M = Pattern.compile( "[0-2][0-9]" ).matcher( DateString );
                     if( M.find() ) {

                        second = ( new Integer( DateString.substring( 0 , 2 ) ) )
                                 .intValue();


                        DateString = DateString.substring( 2 );
                        if( DateString.length() > 0 ) {

                           M = Pattern
                                    .compile(
                                             "(.[0-9]+)?([+-][0-2][0-9][:-]?[0-6][0-9])?" )
                                    .matcher( DateString );
                           if( M.find() ) {

                              try {
                                 String S = M.group( 1 );
                                 if( S.length() > 1 ) {

                                    int nn = ( new Integer( S.substring( 1 ) ) )
                                             .intValue();

                                    frac_second = (float) ( nn / Math.pow( 10f ,
                                             S.length() - 1 ) );
                                 }

                              }
                              catch( Exception s3 ) {
                                 frac_second = 0;
                              }
                              // adjust for things after the :
                              try {
                                 String S = M.group( 2 );
                                 if( S.length() > 1 ) {
                                    int k = S.indexOf( ":" , 1 );
                                    if( k < 0 ) 
                                       k = S.indexOf( "-" , 1 );
                                    
                                    if( k >= 0 )
                                       k++ ;
                                    
                                    else
                                       k = 3;
                                    
                                    int h = ( new Integer( S.substring( 1 , 3 ) ) )
                                             .intValue();
                                    int m = ( new Integer( S.substring( k ) ) )
                                             .intValue();
                                    int sgn = 1;
                                    if( S.startsWith( "-" ) ) 
                                       sgn = - 1;
                                    
                                    hour += sgn * h;
                                    minute += sgn * m;
                                    // while( hour <0){hour +=24;
                                    // while( minute < 0) minute +=60;
                                 }
                              }
                              catch( Exception s4 ) {
                                 // No adjustments to get GMT
                              }
                           }
                        }
                     }
                  }

               }
            }
         }
      }
      
      GregorianCalendar GCal = new GregorianCalendar( year , month-1 , day ,
               hour , minute , second );
      GCal.set( Calendar.MILLISECOND , (int) ( frac_second * 1000 ) );
      return GCal.getTimeInMillis();


   }

  /**
   *    DataSet.addAttribute but att can be null. In the case where att
   *    or DS is null nothing happens.
   */
  public static void addAttribute( DataSet DS, Attribute att ){
    
     if( att == null )
       return;
       
     DS.setAttribute( att );
  }

    
  /**
   *    DB.addAttribute but att can be null. In the case where att
   *    or DB is null nothing happens.
   */
  public static void addAttribute( Data DB, Attribute att ){
    
     if( att == null )
        return;
        
     DB.setAttribute( att );
  }

  /**
   *   Not implemented yet
   */
  public  static DetPosAttribute CreateDetPosAttribute( String AttributeName,
                                                    Object value )
   {
     if( value == null ) 
        return null;
        
     if( !( value instanceof DetectorPosition ) )
        return null;

     return new DetPosAttribute( AttributeName, (DetectorPosition)value );
   }

  /** 
   *  Creates a FloatAttribute if possible, otherwise null is returned
   *  
   *  @param AttributeName  The name of the attribute or null
   *  
   *  @param  value  the value the attribute will have. It will be
   *                 converted to a float
   *                 
   *  @return A FloatAttribute or null if value is null or cannot be converted
   *          to a float or if the AttributeName is null.
   */
  public  static FloatAttribute CreateFloatAttribute( String AttributeName, 
                           Object value ){
       
     if( AttributeName == null )
        return null;
     
     float V = floatValue( value );
     
     if( Float.isNaN( V ) )
        return null;
        
     return new FloatAttribute( AttributeName, V );
        
  }

  /**
   *   Not implemented yet
   */
  public  static Float1DAttribute CreateFloat1DAttribute( String AttributeName, 
                                                    Object value )
     {
       return null;
     }



  /** 
   *  Creates a IntAttribute if possible, otherwise null is returned
   *  
   *  @param AttributeName  The name of the attribute or null
   *  
   *  @param  value  the value the attribute will have. It will be
   *                 converted to a int
   *                 
   *  @return A IntAttribute or null if value is null or cannot be converted
   *          to a int or if the AttributeName is null.
   */
  public  static IntAttribute CreateIntAttribute( String AttributeName, 
                  Object value ){
      
     if( AttributeName == null )
        return null;
     
     int V = intValue( value );
     
     if( V == Integer.MIN_VALUE )
        return null;
        
     return new IntAttribute( AttributeName, V );
        
  }

  
  /** 
   *  Creates a StringAttribute if possible, otherwise null is returned
   *  
   *  @param AttributeName  The name of the attribute or null
   *  
   *  @param  value  the value the attribute will have. It will be
   *                 converted to a String
   *                 
   *  @return A StringAttribute or null if value is null or cannot be converted
   *          to a String or if the AttributeName is null.
   */
  public  static StringAttribute CreateStringAttribute( String AttributeName, 
                           Object value ){
     
     if( AttributeName == null )
        return null;
     
     String V = StringValue( value );
     if( V == null )
        return null;
     
     return new StringAttribute( AttributeName, V );
        
  }


  /**
   *   Not implemented yet
   */
  public  static SampleOrientationAttribute CreateSampleOrientationAttribute( 
                        String AttributeName, Object value )
    {
     return null;
    }


  /**
   *   Not implemented yet
   */
  public static  PixelInfoListAttribute CreatePixelInfoListAttribute( 
                 String AttributeName, Object value )
    {
     return null;
    }

  /**
   *   Converts the NeXus InstrumentName( Description) to the corresponding
   *   Isaw Instrument Number
   *   
   *   @param NeXusAnalysisCode The InstrumentType in NeXus files
   *   
   *   @return the integer corresponding to the InstrumentType in ISAW
   */
  public int getIsawInstrumentNumber( String NeXusAnalysisCode ){
    
     return ( new Inst_Type() ).getIsawInstrNum( NeXusAnalysisCode );
     
  }


  private static float AngleConversionFactor( String OldUnits ){ //base radians   
    
     if( ";rad;radian;r;".indexOf( ";" + OldUnits + ";" ) >= 0 )
        return 1.0f;
    
     if( ";deg;degree;degrees;d;".indexOf( ";" + OldUnits + ";" ) >= 0 )
        return( float )( java.lang.Math.PI / 180.0 );
    
     return 1.0f;
  }


  private static float LengthConversionFactor( String OldUnits ){ //base m
    
     int n = getNumericStart( OldUnits );
     float factor = 1;
     if( n > 0 )
        try{
           factor = ( new Float( OldUnits.substring( 0 , n ) ) ).floatValue();
 
        }catch( Exception ss ){
           factor = 1;
        }
     OldUnits = OldUnits.substring( n );

     if( "mm;millim;millimeter;100um;100microm;".indexOf( OldUnits + ";" ) >= 0 )
        return factor*.001f;
     
     if( "m;meter;met;".indexOf( OldUnits + ";" ) >= 0 )
        return factor*1;
    
     if( "cm;centim;centimeter;cmeter;cmet;100mm;100millim;100millimeter;"
        .indexOf( OldUnits + ";" ) >= 0 )
        return factor*.01f;
    
    
     if( "um;umet;umeter;umeters;".indexOf( OldUnits ) >= 0 )
        return factor*.000001f;
    
    
     if( "in;inch;".indexOf( OldUnits + ";" ) >= 0 )
        return  factor*( float )( 1.0 / 254.0 );
    
     if( "ft;foot;feet;".indexOf( OldUnits + ";" ) >= 0 )
        return factor*( float )( 12.0 / 254.0 );
    
     return factor*1.0f;
    
  }


  private static float TempConversionFactor( String OldUnits ){ //base Kelvin
    
     if( "kelvin;k;".indexOf( OldUnits.toLowerCase() + ";" ) >= 0 )
        return 1.0f;

     if( "deg;degrees;d;".indexOf( OldUnits + ";" ) >= 0 )
        return( float )( 100.0 / 212.0 );

     return 1.0f;
  }


  private static float TimeConversionFactor( String OldUnits ){ //us
     
     int n = getNumericStart( OldUnits );
     float factor = 1;
     
     if( n > 0 )
        try{
          factor = ( new Float( OldUnits.substring( 0 , n ) ) ).floatValue();
 
        }catch( Exception ss ){
           factor = 1;
        }
     OldUnits = OldUnits.substring( n );
     if( OldUnits == null )
        return factor;
        
     if( OldUnits.length() < 1 )
        return factor;
        
     if( OldUnits.charAt(0 )== '*' )
        OldUnits = OldUnits.substring( 1 );
   
     if( "s;sec;second;seconds;".indexOf( OldUnits + ";" ) >= 0 )
        return factor*1000000.0f;
        
     if( "ms;msec;mseconds;msecond;millis;millisec;milliseconds".
                                                  indexOf( OldUnits + ";" ) >= 0 )
        return factor*1000.0f;
        
     if( "us;usec;useconds;usecond;micros;microsec;microseconds;".
                                                  indexOf( OldUnits + ";" ) >= 0 )
     
        return factor;
        
     if( "ns;nsec;nseconds;nanos;nanosec;nanosecond;nanoseconds;".
                                                  indexOf( OldUnits + ";" ) >= 0 )
        return factor*.001f;
        
     if( "min;minute;minutes;".indexOf( OldUnits + ";" ) >= 0 )
        return factor*60*1000000.0f;
        
     if( "hr;hour;hrs;hours;".indexOf( OldUnits + ";" ) >= 0 )
        return 60*factor*1000000.0f;
        
     if( "day;days;".indexOf( OldUnits + ";" ) >= 0 )
        return 24*60*factor*1000000.0f;
        

     return 1.0f;
    
  }


  private static float MassConversionFactor( String OldUnits ){ //grams
    
     return 1.0f;
  }


  private static float EnergyConversionFactor( String OldUnits ){ //Mev
    
     return 1.0f;
  }

 private static int getNumericStart( String S ){
    
     boolean decimalDone = false, 
             expOn = false, 
             leadSign = true;

     for( int i = 0; i < S.length(); i++ ){
       
      char c = S.charAt( i );
      if( Character.isDigit( c ) ){
         
          leadSign = false;}
          
      else if( ( c== '.' ) && !decimalDone && !expOn ){
        
          leadSign = false;
          decimalDone = true;
      }
      else if( ( "+-".indexOf( c ) >= 0 ) && leadSign )
      
          leadSign = false;
          
      else if( ( "Ee^".indexOf( c ) >= 0 ) && !expOn ){
        
          expOn = true;
          leadSign = true;
          decimalDone = true;
          
      }else
      
         return i;
     }
     
     return S.length();
  }
 
 public static void main( String[] args ){
    long T = ConvertDataTypes.parse_neww( args[ 0 ]);
    
    System.out.println( "Time in milliseconds is " + T );
    //GregorianCalendar calendar = new GregorianCalendar();
    //calendar.setTimeInMillis(T);
    //System.out.println("YEAR: " + calendar.get(Calendar.YEAR)); System.out.println("MONTH: " + calendar.get(Calendar.MONTH)); System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR)); System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH)); System.out.println("DATE: " + calendar.get(Calendar.DATE)); System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH)); System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR)); System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK)); System.out.println("DAY_OF_WEEK_IN_MONTH: " + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH)); System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM)); System.out.println("HOUR: " + calendar.get(Calendar.HOUR)); System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY)); System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE)); System.out.println("SECOND: " + calendar.get(Calendar.SECOND)); System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
    
 }
 
}
  


