
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/11/16 21:46:43  rmikk
 * Initial Checkin
 *
 */

package NexIO.Util;
import NexIO.*;
import NexIO.State.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;
import java.lang.reflect.*;
/**
*    This class contains methods to convert Objects to a specified
*    data type(including dates), Change units for data, convert Nexus axis 
*    to Isaw axes.
*    Also, there are routines that pass through null values without causing
*    null pointer exceptions.  These save programming lines.
*/
public class ConvertDataTypes{

  /**
   *  returns a float array corresponding to the object O or null if not possible
   */
  public static float[] floatArrayValue( Object O){
     if( O == null)
        return null;
     try{
        if( O.getClass().isArray()){
           float[] Res = new float[ Array.getLength(O)];
           for( int i=0; i< Res.length; i++){
              Object El = Array.get( O,i);
              if( El instanceof Number)
                 Res[i] = ((Number)El).floatValue();
              else if( El instanceof String)
                 Res[i] = (new Float( (String)El)).floatValue(); 
           }
           return Res; 
        }else if( O instanceof Number){
           float[] Res = new float[1];
           Res[0] = ((Number)O).floatValue();
           return Res;
        }else if( O instanceof String){
           float[] Res = new float[1];
           Res[0] = (new Float( (String)O)).floatValue(); 
           return Res;
  
        }
     }catch( Exception S){}
     return null;

        
  };
 
  /**
   *  returns an int array corresponding to the object O or null if not possible
   */
  public static int[]   intArrayValue( Object O) {
     if( O == null)
        return null;
     try{
        if( O.getClass().isArray()){
           int[] Res = new int[ Array.getLength(O)];
           for( int i=0; i< Res.length; i++){
              Object El = Array.get( O,i);
              if( El instanceof Number)
                 Res[i] = ((Number)El).intValue();
              else if( El instanceof String)
                 Res[i] = (new Integer( (String)El)).intValue(); 
           }
           return Res; 
        }else if( O instanceof Number){
           int[] Res = new int[1];
           Res[0] = ((Number)O).intValue();
           return Res;
        }else if( O instanceof String){
           int[] Res = new int[1];
           Res[0] = (new Integer( (String)O)).intValue(); 
           return Res;
  
        }
     }catch( Exception S){}
     return null;

    
   };

   /**
   *  returns a float corresponding to the object O or Float.NaN if not possible.
   *  if O is an array, only the first element will be returned
   */
  public static float  floatValue( Object O){
     float[] Res = floatArrayValue( O);
     if( Res == null)
        return Float.NaN;
     if( Res.length < 1)
        return Float.NaN;
     return Res[0];

  };

   /**
   *  returns an int corresponding to the object O or Integer.MIN_VALUE if 
   *  not possible. If O is an array, only the first element will be returned
   */
  public static int intValue( Object O){
     int[] Res = intArrayValue( O);
     if( Res == null)
        return java.lang.Integer.MIN_VALUE;
     if( Res.length < 1)
        return java.lang.Integer.MIN_VALUE;
     return Res[0];

  };

  private static char[] cnvertTochar( byte[] L){
     if( L == null)
        return null;
     char[] Res = new char[ L.length];
     for( int i=0; i< L.length; i++)
        Res[i] = (char)L[i];
     return Res;

  }
  private static int findNext( char[] L, char[] F, int start){
     int n = 0;
     for( int i = start; (i >=0) && (i < L.length); i++){
        boolean match = true;
        for( int j = 0; (j< F.length) &&(match); j++)
           if( i+j >L.length) match = false;
           else if( L[i+j] != F[j]) match = false;
        if(match) return i;
     }
     return -1;
  }
  /** end is not included
  */
  private static int countt( char[]L, char [] F, int start, int end){
     int n = 0;
     for( int i = start; (i >=0) && (i < end); i++){
        boolean match = true;
        for( int j = 0; (j< F.length) &&(match); j++)
           if( i+j >L.length) match = false;
           else if( L[i+j] != F[j]) match = false;
        if(match) n++;
     }
     return n;
  }


   /**
   *  returns a String Array corresponding to L or null if 
   *  not possible.
   */
  public static String[] charArraystoStrings( char[] L){
    char[] two0= new char[2];
    two0[0]=0; two0[1] = 0;
    char[] one0= new char[1];
    one0[0] = 0;
    int n = countt( L,one0,0,L.length);
    String[] Res = new String[n];
    int k=0;
    for( int i=0; i < n; i++){
       int k1 = findNext(L,one0,k);
       if( k1 <0) k1 = L.length;
       Res[i] = new String (L,k,k1 - k );
       k =k1+1;
    }
   return Res;
  }

   /**
   *  returns a String Array corresponding to the Object O or null if 
   *  not possible.
   */
  public static String[] StringArrayValue( Object O){
     if( O == null)
        return null;
     if( O instanceof char[]){
        return charArraystoStrings( (char[])O);

     }
     if( O instanceof byte[])
        return charArraystoStrings( cnvertTochar( (byte[])O));
     else if( O.getClass().isArray()){
        String[] Res = new String[ Array.getLength(O)];
        for( int i=0; i< Res.length; i++){
           Object El = Array.get( O,i);
           Res[i] = El.toString();
        }
        return Res; 
        
     }else {
        String[] Res = new String[1];
        Res[0] = O.toString();
        return Res;
  
     }
  };

  /**
   *  returns a String corresponding to the Object O or null if 
   *  not possible.
   */
  public static String StringValue( Object O){
     if( O == null) return null;
     String[] X = StringArrayValue( O);
     if( X == null)
       return null;
     if( X.length < 1)
       return null;
     return X[0]; 

  };


  /**
   * Gives Factor to mulitply OldUnits to get NewUnits(ISAW units)
   * @param  OldUnits  the units that are non ISAW units
   * @param StdUnits  the ISAW units. Must be radians, meters,Kelvin,us,grams,
   *                   Mev,or steradian
   * @return the factor to multiply a quantity in old units to get to the new units
   
   */
  public static float getUnitMultiplier( String OldUnits, String StdUnits){
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

    if( NewUnits.equals("steradian"))  //"sr is another symbol but if not this
       return 1.0f;                    //??????????????????????????????????
    
    else return 1.0f;
  }

  /**
   *   Adjusts all values in an array to the proper units
   *   @param  d   the array to be adjusted
   *   @param oldUnits  the units that the array is currently in
   *   @param StdUnits  the Isaw Standard units{us,m,etc.}
   *   @param mult   should be 1 unless StdUnits are not desired. The conversion
   *                 factor to StdUnits is multiplied by mult
   *   @param add    should be 0 unless StdUnits are not desired. The result after
   *                 multiplying conversion factor by mult has this quantity added to
   *                 it.
   */
  public static void UnitsAdjust( float[] d,String oldUnits,String StdUnits, 
                 float mult, float add){ //y = mx+b
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
      d[i] =mult* f * d[i]+add;
  } 

  /**
   * Nexus position to ISaw positiom
   */
  public static DetectorPosition convertToIsaw(float distance,
                                             float phi,
                                             float theta){
     DetectorPosition dp = new DetectorPosition();
     float[] f =Types.convertFromNexus(distance, phi, theta);
     dp.setSphericalCoords( f[0],f[2],f[1]);
     return dp;
  }


  /**
   *  @see NexIO.NxNodeUtils#parse(String)
  */
  public static java.util.Date parse(java.lang.String DateString){
     return NxNodeUtils.parse( DateString);
  }

  /**
   *    DataSet.addAttribute but att can be null. In the case where att
   *    or DS is null nothing happens.
   */
  public static void addAttribute( DataSet DS, Attribute att){
     if( att == null)
       return;
     DS.setAttribute( att);
  }

    
  /**
   *    DB.addAttribute but att can be null. In the case where att
   *    or DS is null nothing happens.
   */
  public static void addAttribute(Data DB, Attribute att){
     if( att == null)
        return;
     DB.setAttribute( att);
  }

  /**
   *   Not implemented yet
   */
  public  static DetPosAttribute CreateDetPosAttribute( String AttributeName,
                                                    Object value)
        {return null;}

  /** 
   *  Creates a FloatAttribute if possible, otherwise null is returned
   */
  public  static FloatAttribute CreateFloatAttribute( String AttributeName, 
                           Object value){
     float V = floatValue(value);
     if( Float.isNaN( V))
        return null;
     return new FloatAttribute( AttributeName, V);
        
  }

  /**
   *   Not implemented yet
   */
  public  static Float1DAttribute CreateFloat1DAttribute( String AttributeName, 
                                                    Object value)
        {return null;}



  /** 
   *  Creates a IntAttribute if possible, otherwise null is returned
   */
  public  static IntAttribute CreateIntAttribute( String AttributeName, 
                  Object value){
     int V = intValue(value);
     if( V== Integer.MIN_VALUE)
        return null;
     return new IntAttribute( AttributeName, V);
        
  }

  
  /** 
   *  Creates a StringAttribute if possible, otherwise null is returned
   */
  public  static StringAttribute CreateStringAttribute( String AttributeName, 
                           Object value){
     String V = StringValue(value);
     if( V == null)
        return null;
     return new StringAttribute( AttributeName, V);
        
  }


  /**
   *   Not implemented yet
   */
  public  static SampleOrientationAttribute CreateSampleOrientationAttribute( 
                        String AttributeName, Object value)
        {return null;}


  /**
   *   Not implemented yet
   */
  public static  PixelInfoListAttribute CreatePixelInfoListAttribute( 
                 String AttributeName, Object value)
        {return null;}

  /**
   *   Converts the NeXus InstrumentName( Description) to the corresponding
   *   Isaw Instrument Number
   */
  public int getIsawInstrumentNumber( String NeXusAnalysisCode){
     return (new Inst_Type()).getIsawInstrNum( NeXusAnalysisCode);
  }


  private static float AngleConversionFactor( String OldUnits ){ //base radians
     boolean hasDegree = false;
    
     if( ";rad;radian;r;".indexOf( ";" + OldUnits + ";" ) >= 0 )
        return 1.0f;
    
     if( ";deg;degree;degrees;d;".indexOf( ";" + OldUnits + ";" ) >= 0 )
        return( float )( java.lang.Math.PI / 180.0 );
    
     return 1.0f;
  }


  private static float LengthConversionFactor( String OldUnits ){ //base m
     int n = getNumericStart( OldUnits);
     float factor = 1;
     if( n >0)
        try{
           factor = (new Float( OldUnits.substring( 0,n))).floatValue();
 
        }catch( Exception ss){
           factor = 1;
        }
     OldUnits = OldUnits.substring(n);
     if( "m;meter;met;".indexOf( OldUnits + ";" ) >= 0 )
        return factor*1;
    
     if( "cm;centim;centimeter;cmeter;cmet;100mm;100millim;100millimeter;"
        .indexOf( OldUnits + ";" ) >= 0 )
        return factor*.01f;
    
     if("mm;millim;millimeter;100um;100microm;".indexOf( OldUnits + ";" ) >= 0)
        return factor*.001f;
    
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
     int n = getNumericStart( OldUnits);
     float factor = 1;
     if( n >0)
        try{
          factor = (new Float( OldUnits.substring( 0,n))).floatValue();
 
        }catch( Exception ss){
           factor = 1;
        }
     OldUnits = OldUnits.substring(n);
     if( OldUnits == null)
        return factor;
     if( OldUnits.length() < 1)
        return factor;
     if( OldUnits.charAt(0)=='*')
        OldUnits = OldUnits.substring(1);
   
     if("s;sec;second;seconds;".indexOf(OldUnits+";") >=0)
        return factor*1000000.0f;
     if( "ms;msec;mseconds;msecond;millis;millisec;milliseconds".indexOf(OldUnits+";")>=0)
        return factor*1000.0f;
     if("us;usec;useconds;usecond;micros;microsec;microseconds;".indexOf( OldUnits+";") >=0)
        return factor;
     if("ns;nsec;nseconds;nanos;nanosec;nanosecond;nanoseconds;".indexOf( OldUnits+";") >=0)
        return factor*.001f;
     if("min;minute;minutes;".indexOf(OldUnits+";")>=0)
        return factor*60*1000000.0f;
     if("hr;hour;hrs;hours;".indexOf(OldUnits+";")>=0)
        return 60*factor*1000000.0f;
     if("day;days;".indexOf(OldUnits+";")>=0)
        return 24*60*factor*1000000.0f;

     return 1.0f;
    
  }


  private static float MassConversionFactor( String OldUnits ){ //grams
     return 1.0f;
  }


  private static float EnergyConversionFactor( String OldUnits ){ //Mev
     return 1.0f;
  }

 private static int getNumericStart( String S){
     int n=0;
     boolean decimalDone = false, expOn = false, leadSign=true;

     for( int i = 0; i< S.length(); i++){
      char c= S.charAt(i);
      if( Character.isDigit( c)){ 
          leadSign = false;}
      else if( (c=='.') && !decimalDone&&!expOn){
          leadSign = false;
          decimalDone = true;
      }
      else if( ("+-".indexOf(c) >=0) && leadSign)
          leadSign = false;
      else if( ("Ee^".indexOf( c) >=0) && !expOn){
          expOn = true;
          leadSign = true;
          decimalDone = true;
      }else
         return i;
        

     }
     return S.length();
  }
 
}
  


