/*
 * File:  Convert.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.1  2005/10/04 20:12:17  dennis
 * Initial version of file with static methods to do type conversions
 * for the ISAW scripting language.  The methods in this class will be
 * used to form Operators using the method to operator wizard.  Currently
 * there are methods:
 *
 *   IntListToVector
 *   VectorToIntListString
 *   VectorTo_intArray
 *   VectorTo_floatArray
 *   GsasCalibToVector
 *   VectorToGsasCalib
 *
 * That will be used to generate "Generic" operators of the same names.
 * Conversions between DetectorPosition objects and Vectors containing
 * (x,y,z), (r,theta,z) or (r,theta,phi) should also be added in the future.
 *
 */

package Operators.Generic.TypeConversion;

import java.util.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import DataSetTools.gsastools.*;

/**
 *  This class contains static methods for converting between different
 *  data types needed by the ISAW scripting language.
 */

public class Convert
{

  /* --------------------- private constructor ---------------------- */
  /**
   * Private constructor, since this class only has static methods
   */
  private Convert()
  {};


  /* ---------------------- IntListToVector ------------------------- */
  /**
   * Convert a specially formatted String, or an int array to a
   * Vector of integers, for use by the ISAW scripting system. 
   *
   * @param  list  A String describing an increasing sequence of 
   *               integers, in the form "3:5,10,15,20:25", or an
   *               array of integers.  If the input list is an
   *               array of integers, they will be sorted and duplicates
   *               will be removed when the values are placed in the Vector.
   *
   * @return  A Vector containing the increasing sequence of Integer objects,
   *          specified by in input list.  If the String description of the
   *          list of integers is improperly formatted, the values may not
   *          be copied to the Vector completely, or at all.  The only error
   *          that is detected at this time is passing in null, or an Object
   *          that is not a String or an array of ints.
   */
  public static Object IntListToVector( Object list )
  {
    int int_vals[];

    if ( list == null ) 
      return new ErrorString( "Parameter was null in IntListToVector" );

    if ( list instanceof String )
    {
      int_vals = IntList.ToArray( (String)list );
      if ( int_vals == null || int_vals.length == 0 )// deal with degenerate
        return new Vector();                         // case, so later we know
    }                                                // we have at least 1 int

    else if ( list instanceof int[] )
      int_vals = extractIncreasingValues( (int[])list );

    else
      return new ErrorString("list must be String or int[] in " +
                             "IntListToVector" );

    Vector result = new Vector( int_vals.length );
    for ( int i = 0; i < int_vals.length; i++ )
      result.add( new Integer( int_vals[i] ) );

    return result; 
  }


  /* ---------------------- VectorTo_intArray ----------------------- */
  /**
   *  Convert a list of numeric values in a Vector to an array 
   *  of integers in the same ordera as the values were stored in the
   *  Vector, by rounding the values stored in the Vector..
   *
   *  @param obj  A Vector containing numeric values.  If there are non-numeric
   *              values in the Vector, an ErrorString will be returned.
   *
   *  @return A array of ints obtained by rounding the numeric values fromt
   *          the Vector.  Return an error string if the input parameter is 
   *          not a Vector containing numeric values.
   */ 
  public static Object VectorTo_intArray( Object obj )
  {
    if ( obj == null ) 
      return new ErrorString( "Parameter was null in VectorToIntListString" );

    if ( !(obj instanceof Vector) )
      return new ErrorString("Parameter must be a Vector");

    if ( ((Vector)obj).size() <= 0 )
      return "";

    double numbers[] = getNumericArray( (Vector)obj );
    
    int ints[] = new int[ numbers.length ];
    for ( int i = 0; i < ints.length; i++ )
      ints[i] = (int)Math.round( numbers[i] ); 

    return ints;
  }


  /* ---------------------- VectorTo_floatArray ------------------------- */
  /**
   *  Convert a list of numeric values in a Vector to an array 
   *  of floats, in the same order the values were stored in the vector.
   *
   *  @param obj  A Vector containing numeric values.  If there are non-numeric
   *              values in the Vector, an ErrorString will be returned.
   *
   *  @return A array of ints obtained by rounding the numeric values fromt
   *          the Vector.  Return an error string if the input parameter is 
   *          not a Vector containing numeric values.
   */
  public static Object VectorTo_floatArray( Object obj )
  {
    if ( obj == null )
      return new ErrorString( "Parameter was null in VectorToIntListString" );

    if ( !(obj instanceof Vector) )
      return new ErrorString("Parameter must be a Vector");

    if ( ((Vector)obj).size() <= 0 )
      return "";

    double numbers[] = getNumericArray( (Vector)obj );

    float floats[] = new float[ numbers.length ];
    for ( int i = 0; i < floats.length; i++ )
      floats[i] = (float)numbers[i];

    return floats;
  }


  /* ---------------------- VectorToIntListString ----------------------- */
  /**
   *  Convert a list of numeric values in a Vector to a String, specifying
   *  an increasing sequence of integers.  NOTE: Floats or Doubles will
   *  be rounded to the nearest integer.  Also, the resulting list of
   *  integers will be sorted, duplicates removed and converted to a String
   *  representation like  3:6,18,20:25
   *
   *  @param obj  A Vector containing numeric values.  If there are non-numeric
   *              values in the Vector, an ErrorString will be returned.
   *
   *  @return A String specifying an increasing sequence of integers, or an
   *            error string if the input parameter is not a Vector containing
   *            numeric values.
   */
  public static Object VectorToIntListString( Object obj )
  {
    if ( obj == null )
      return new ErrorString( "Parameter was null in VectorToIntListString" );

    if ( !(obj instanceof Vector) )
      return new ErrorString("Parameter must be a Vector");

    if ( ((Vector)obj).size() <= 0 )
      return "";

    double numbers[] = getNumericArray( (Vector)obj );

    int ints[] = new int[ numbers.length ];
    for ( int i = 0; i < ints.length; i++ )
      ints[i] = (int)Math.round( numbers[i] );

    ints = extractIncreasingValues( (ints) );
    return IntList.ToString( ints );
  }


  /* ------------------------- GsasCalibToVector ------------------------- */
  /**
   * Convert an object of type gsas_calib, that contains values for 
   * dif_C, dif_A and t_zero, to a Vector, containing these three values
   * in that order.
   *
   * @param  gsas_calib  A gsas_calib object. 
   *
   * @return  A Vector containing dif_C, dif_A and t_zero in that order.
   *          If something other than a gsas_calib object is passed in,
   *          an ErrorString will be returned.
   */
  public static Object GsasCalibToVector( Object gsas_calib )
  {
    if ( gsas_calib == null ) 
      return new ErrorString( "Parameter was null in GsasCalibToVector" );

    if ( !(gsas_calib instanceof GsasCalib) ) 
      return new ErrorString("Argument MUST be of type GsasCalib");

    Vector calib_vector = new Vector(3);
    GsasCalib cal = (GsasCalib)gsas_calib;

    calib_vector.add( new Float( cal.dif_c() ) );
    calib_vector.add( new Float( cal.dif_a() ) );
    calib_vector.add( new Float( cal.t_zero() ) );

    return calib_vector;
  }


  /* ----------------------- VectorToGsasCalib ------------------------- */
  /**
   * Convert the first three values in a Vector containing numeric values
   * into a gsas_calib object.  
   *
   * @param  calib_vector  A Vector containing dif_C, dif_A and t_zero in 
   *                       that order.
   *
   * @return A gsas_calib object, with values filled from the first three
   *         positions in the Vector.  If the Vector passed in does not start
   *         with three numeric values, then an ErrorString will be returned.
   */
  public static Object VectorToGsasCalib( Object calib_vector )
  {
    if ( calib_vector == null )
      return new ErrorString( "Parameter was null in VectorToGsasCalib" );

    String error = null;

    if ( !(calib_vector instanceof Vector) ||
         ((Vector)calib_vector).size() != 3 )
      error = "Argument MUST be a Vector starting with three numeric values.";

    double diff_c = getDouble( ((Vector)calib_vector).elementAt( 0 ) );
    double diff_a = getDouble( ((Vector)calib_vector).elementAt( 1 ) );
    double t_zero = getDouble( ((Vector)calib_vector).elementAt( 2 ) );

    if ( Double.isNaN( diff_c ) ||
         Double.isNaN( diff_a ) ||
         Double.isNaN( t_zero )  )
      error = "Vector components must be numeric";

    if ( error != null )
      return new ErrorString( error );

    return new GsasCalib( (float)diff_c, (float)diff_a, (float)t_zero );
  }


  /* ---------------------------- getDouble ---------------------------- */
  /**
   *  Get the value of an Integer, Float or Double object, as a double
   *  precision value.
   *
   *  @param  obj  An Integer, Float or Double object.
   *
   *  @return The value stored in the numeric object, OR Double.NaN
   *          if the object is not numeric. 
   */
  public static double getDouble( Object obj )
  {
    if ( obj == null )
      return Double.NaN;

    if ( obj instanceof Float )
      return ((Float)obj).floatValue();

    if ( obj instanceof Integer )
      return ((Integer)obj).intValue();

    if ( obj instanceof Double )
      return ((Double)obj).doubleValue();

    return Double.NaN; 
  }


  /* ------------------------ getNumericArray ------------------------- */
  /**
   *  Get an array of numeric values from a Vector containing only 
   *  numeric objects.
   *
   *  @param  vec   A Vector containing only objects of type Integer,
   *                Float or Double
   *
   *  @return An array of doubles, containing the values from the Vector,
   *          or null, if any of the Vector entries are non-numeric.
   */
  public static double[] getNumericArray( Vector vec )
  {
    if ( vec == null || vec.size() <= 0 )
      return new double[0];

    double array[] = new double[ vec.size() ];
    double val;
    for ( int i = 0; i < array.length; i++ )
    {
      val =  getDouble( vec.elementAt(i) );
      if ( Double.isNaN( val ) )
        return null;
      else
        array[i] = val;
    }

    return array;
  }


  /* ---------------------- extractIncreasingValues ----------------------- */
  /**
   *  Extract an increasing sequence of distinct integers that are present
   *  in an arbitrary list of integer values.
   */
  public static int[] extractIncreasingValues( int list[] )
  {
    if ( list == null || list.length <= 0 )     // deal with degenerate
      return new int[0];                        // case, so later we know
                                                // we have at least 1 int
 
    int my_vals[] = new int[ ((int[])list).length ];
    System.arraycopy( (int[])list, 0, my_vals, 0, my_vals.length );
    Arrays.sort( my_vals );

    int num_duplicates = 0;
    for ( int i = 1; i< my_vals.length; i++ )
      if ( my_vals[i] == my_vals[i-1] )
        num_duplicates++;

    int int_vals[] = new int[ my_vals.length - num_duplicates ];
    int_vals[0] = my_vals[0];
    int index = 1;
    for ( int i = 1; i < my_vals.length; i++ )
      if ( my_vals[i] != my_vals[i-1] )
      {
        int_vals[index] = my_vals[i];
        index++;
      }

    return int_vals;
  }


  /* ----------------------------- main --------------------------------- */
  /**
   *  Basic tests of the methods in the Convert class.
   */
  public static void main( String args[] )
  {
    System.out.println("Converting String 1:5,10,20:25 to vector:" );
    Object result = IntListToVector( "1:5,10,20:25" );
    if ( result instanceof ErrorString )
      System.out.println( (ErrorString)result );
    else
      for ( int i = 0; i < ((Vector)result).size(); i++ )
        System.out.println( (((Vector)result).elementAt(i) ) );


    System.out.println("Converting array 3,4,5,-10,-20,-20,3,4,3 to vector:" );
    int list_1[] = { 3,4,5,-10,-20,-20,3,4,3 };
    result = IntListToVector( list_1 );
    if ( result instanceof ErrorString )
      System.out.println( (ErrorString)result );
    else
      for ( int i = 0; i < ((Vector)result).size(); i++ )
        System.out.println( (((Vector)result).elementAt(i) ) );

    System.out.println("Converting Float to vector");
    result = IntListToVector( new Float(10) );
    if ( result instanceof ErrorString )
      System.out.println( (ErrorString)result );
    else
      for ( int i = 0; i < ((Vector)result).size(); i++ )
        System.out.println( (((Vector)result).elementAt(i) ) );

    GsasCalib gsas_calib = new GsasCalib( 1, 2, 3 );
    Vector vector = (Vector)GsasCalibToVector( gsas_calib );
    gsas_calib = (GsasCalib)VectorToGsasCalib( vector );
    System.out.println( "GsasCalib should be 1, 2, 3 " + gsas_calib );

    vector.clear();
    vector.add( new Integer(4) );
    vector.add( new Float(5.3) );
    vector.add( new Double(6.8) );
    gsas_calib = (GsasCalib)VectorToGsasCalib( vector );
    System.out.println( "GsasCalib should be 4, 5.3, 6.8 " + gsas_calib );

    System.out.println( "VectorToIntListString should be 4:5,7 " +
                         VectorToIntListString( vector ) );

    int int_arr[] = (int[])VectorTo_intArray( vector );
    System.out.println( "VectorTo_intArray....." );
    for ( int i = 0; i < int_arr.length; i++ )
      System.out.println( "int val = " + int_arr[i] );

    float float_arr[] = (float[])VectorTo_floatArray( vector );
    System.out.println( "VectorTo_floatArray....." );
    for ( int i = 0; i < float_arr.length; i++ )
      System.out.println( "flat val = " + float_arr[i] );
  }

}
