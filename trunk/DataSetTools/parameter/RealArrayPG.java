/*
 * File: RealArrayPG.java
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2004/06/16 21:45:31  rmikk
 *  Initial Checkin. This ParameterGUI is used for transfering references
 *    to large, possibly multidimensional, arrays.  These arrays are then
 *    input/output parameters.
 *  SHOULD NOT BE USED  as Script Parameters
 *
  */
package DataSetTools.parameter;

import Command.JavaCC.*;

import Command.execOneLine;

import DataSetTools.components.ParametersGUI.EntryWidget;

import DataSetTools.dataset.DataSet;

import java.awt.*;

import java.lang.reflect.*;

import java.util.Vector;

import javax.swing.*;


/**
 * This is a superclass to take care of many of the common details of Array
 * Parameter GUIs.
 */
public class RealArrayPG extends ParameterGUI implements ParamUsesString {
  //~ Static fields/initializers ***********************************************

  private static String TYPE    = "RealArray";
  protected static int DEF_COLS = 20;
  private Class valClass = null;
  //~ Constructors *************************************************************

  /**
   * Creates a new RealArrayPG object.
   *
   * @param name Name of this RealArrayPG.
   * @param val Value of this RealArrayPG.
   */
  public RealArrayPG( String name, Object val ) {
    super( name, val );

    if( val == null)
        throw new IllegalArgumentException("Initial value cannot be null");
    if( !isMultiArray( val))
        throw new IllegalArgumentException("Initial value must be"+
           " mult dim array of numbers or Strings");
    setType( TYPE );
    valClass = val.getClass();
  }

  /**
   * Creates a new RealArrayPG object.
   *
   * @param name Name of this RealArrayPG.
   * @param val Initial value of this RealArrayPG.
   * @param valid Whether it is valid or not.
   */
  public RealArrayPG( String name, Object val, boolean valid ) 
                 throws  IllegalArgumentException{
    super( name, val, valid );
    if( val == null)
        throw new IllegalArgumentException("Initial value cannot be null");
    if( !isMultiArray( val))
        throw new IllegalArgumentException("Initial value must be"+
           " mult dim array of numbers or Strings");
    setType( TYPE );
  }

  //~ Methods ******************************************************************

  /**
   * Sets the String value of this RealArrayPG.
   *
   * @param val The String value to set it to.
   */
  public void setStringValue( java.lang.String val ) {
    setValue( val );
  }

  /**
   * Accessor method for this RealArrayPG's String value.
   *
   * @return The String value associated with this RealArrayPG.
   */
  public String getStringValue(  ) {
    return ArraytoString( unwrapArray(getValue(  )) );
  }

  /**
   * Sets the value of this RealArrayPG.
   *
   * @param val The value to set.
   */
  public void setValue( Object val ) {
    Vector vecVal = null;
   
   Object val1 = val;
   if( val == null)
     val1 = getValue();
       
    if( val1 == null)//initial set up called from ParameterGUI
      return;
   
    else if( val1 instanceof Vector ) {
      val1 = Arrayify(val);
    } else if( val1 instanceof String ) {
      val1=Arrayify( StringtoArray( ( String )val ));
    } else if( val1.getClass(  ).isArray(  ) ) {
      if( !RealArrayPG.isMultiArray(val1))
        
        val1 = Arrayify( val );
    } else {
      return;
    }
    if( !RealArrayPG.isMultiArray(val1))
       return;
    if( val1 == null)
       return;
   
    if( getInitialized(  ) ) {
      String Res = gov.anl.ipns.Util.Sys.StringUtil.toString(val1);
      
      ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).setText( 
        Res );
    }

    //always update internal value
    super.setValue( val1 );
  }

  /**
   * Accessor method to retrieve the value of this RealArrayPG.  If this is called
   * and this RealArrayPG has been initialized, it also sets the internal value to
   * the GUI value.
   *
   * @return The value of this RealArrayPG.
   */
  public Object getValue(  ) {
    //Vector of DataSets
    Object val = super.getValue(  );

   
    if( getInitialized(  ) ) {
     String val0   = ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).getText(  );
     Vector val1   = StringtoArray( val0.toString(  ) );
     Object val2 = Arrayify( val1);
     if(val2 != null)
     if( val2.getClass() == val.getClass())
        val = val2;
    }

    return val;
  }

  /**
   * Fast-access method to get the type cast value.
   *
   * @return The Vector cast value of this RealArrayPG.
   */
  public Vector getVectorValue(  ) {
    return ( Vector )getValue(  );
  }

  /**
   * Converts an array (Vector) to a String.
   *
   * @param V The Vector to convert
   *
   * @return A String representation of the Vector.
   */
  public static String ArraytoString( Vector V ) {
    if( V == null ) {
      return "[]";
    }

    String res = execOneLine.Vect_to_String( V );

    return res;
  }

  /**
   * Method to turn a String into a Vector array.  It can handle nifty things
   * like [ISAWDS1, ISAWDS2] which, if a runfile is loaded, will actually be
   * turned into an array of DataSets.
   *
   * @param S The String to turn into an array.
   *
   * @return A Vector of Objects corresponding to the Strings.
   */
  public static Vector StringtoArray( String S ) {
    try {
      //prep the string a little
      S = S.trim(  );

      return ParameterGUIParser.parseText( S );
    } catch( ParseException pe ) {
      System.out.println( pe.getMessage(  ) );

      return new Vector(  );
    }
  }

  /**
   * Utility to return Vectors parsed from Strings using execOneLine.  This is
   * a convenience to handle the errors that execOneLine can return by hiding
   * them behind an empty Vector.
   *
   * @param executor The execOneLine Object to return things from.
   * @param line The line to execute.
   *
   * @return The result from execOneLine.  If an error is hit, it returns an
   *         empty Vector.
   */
  public static Vector parseLine( execOneLine executor, String line ) {
    //execute the line
    executor.execute( line, 0, line.length(  ) );

    if( executor.getErrorCharPos(  ) >= 0 ) {
      return new Vector(  );
    }

    //parse the string and try to get a result
    Object result = executor.getResult(  );

    //no dice...either no result, or we got a result, but it was not useful 
    //to us.
    if( ( result == null ) || !( result instanceof Vector ) ) {
      return new Vector(  );
    }

    return ( Vector )result;
  }

  
  /**
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The Vector of values to initialize this RealArrayPG to.
   */
  public void initGUI( Vector init_values ) {
    if( getInitialized(  ) ) {
      return;  // don't initialize more than once
    }

    //i.e. if it is not null AND it is not an empty Vector, then reset the values.
    if( 
      ( init_values != null ) &&
        ( init_values instanceof Vector &&
        ( ( ( Vector )init_values ).size(  ) > 0 ) ) ) {
      setValue( init_values );
    }

    setEntryWidget( 
      new EntryWidget( 
        new JTextField( ArraytoString(unwrapArray( getValue(  ) )) ) ) );

    //we'll set a really small preferred size and let the Layout Manager take
    //over at that point
    getEntryWidget(  ).setPreferredSize( new Dimension( 2, 2 ) );
    super.initGUI(  );
  }

  /**
   * Since this is an array parameter, better allow an array to initialize the
   * GUI.
   *
   * @param init_values The array of Objects to initialize this RealArrayPG to.
   */
  public void initGUI( Object[] init_values ) {
    Vector init_vec = new Vector(  );

    for( int i = 0; i < init_values.length; i++ ) {
      init_vec.add( init_values[i] );
    }

    //call the "regular" initGUI( Vector )
    initGUI( init_vec );
  }

  /*
   * Main method for testing purposes.
   */
  public static void main( String[] args ) {
    RealArrayPG fpg;
    int y       = 0;
    int dy      = 70;
    int[][] vals = {  {1,3,5,7},
                      {2,4,6,8},
                      {5,5,5,5}};

    System.out.println("is array?"+ RealArrayPG.isMultiArray(vals));
    fpg = new RealArrayPG( "a", vals, true );
    System.out.println( "Before calling init, the RealArrayPG is " );
    System.out.println( fpg );
    fpg.initGUI( vals );
    System.out.print( "YYY="+gov.anl.ipns.Util.Sys.
    StringUtil.toString(fpg.getValue(  )) + "\n" );
    fpg.showGUIPanel( 0, y );
    y += dy;
    fpg = new RealArrayPG( "b", vals );
    System.out.println( fpg );
    fpg.setEnabled( false );
    fpg.initGUI( new Vector(  ) );
    fpg.showGUIPanel( 0, y );
    y += dy;
    Object vall = fpg.getValue();
    System.out.println("Val="+gov.anl.ipns.Util.Sys.
                        StringUtil.toString(vall));
    if(vall != null)
    System.out.println("Val Data Type="+vall.getClass());


   
    int[][][] array = new int[3][3][3];

    for( int i = 0; i < 3; i++ ) {
      for( int j = 0; j < 3; j++ ) {
        for( int k = 0; k < 3; k++ ) {
          array[i][j][k] = k;
        }
      }
    }

    fpg = new RealArrayPG( "e", array, true );
    System.out.println( fpg );
    fpg.setDrawValid( true );
    fpg.initGUI( new Vector(  ) );
    fpg.showGUIPanel( 0, y ); 

    System.out.println("Val="+gov.anl.ipns.Util.Sys.
                        StringUtil.toString(fpg.getValue()));
    y += dy;
    System.out.println("-----------------");
    Class C = (new float[0][0]).getClass();
    Object OO =RealArrayPG.getZeroLengthedArray(C);
    System.out.println("0 length inst="+OO.getClass());
    System.out.println("Val="+gov.anl.ipns.Util.Sys.
                           StringUtil.toString(OO));
    
  }

  /**
   * Used to clear out the RealArrayPG.  This resets the Vector to an empty Vector
   * and clears the display.
   */
  public void clear(  ) {
    setValue( null);
    //Should set up a zero lengthed array of the same class as the initial array
  }

 
  /**
   * Validates this RealArrayPG.  An RealArrayPG is considered valid if getValue() does
   * not return either a null, and empty Vector, or a non-Vector.
   */
  public void validateSelf(  ) {
    Object o = getValue(  );

    if( ( o != null ) && o instanceof Vector ) {
      Vector v = ( Vector )o;

      if( ( v == null ) || v.isEmpty(  ) ) {
        setValid( false );
      } else {  //assume it is valid, then test that assumption
        setValid( true );
      }
    } else {
      setValid( false );
    }
  }

  /**
   * Recursively unwraps the given Object which must be an array (e.g. int[]).
   *
   * @param array The array to unwrap.
   *
   * @return A Vector consisting of the unwrapped array.  If it is a
   *         multidimensional array, then the Vector's elements will
   *         themselves be Vectors.
   */
  private Vector unwrapArray( Object array ) {
    Vector vecVal  = new Vector(  );
    Object element = null;

    for( int i = 0; i < Array.getLength( array ); i++ ) {
      element = Array.get( array, i );

      if( element.getClass(  ).isArray(  ) ) {
        vecVal.add( unwrapArray( element ) );
      } else {
        vecVal.add( element );
      }
    }

    return vecVal;
  }
  
 /**
  *   Determines if the val is a multi dimension array with the same
  *   length.  The base type must be a number or String
  * @param val  The value to be tested
  * @return   true if the class of val is like int[], int[][], etc. 
  *           where int could be float, double, long, byte, short or
  *           String
  */
 public static boolean isMultiArray( Object val){
     if( val == null)
        return false;
     if(!val.getClass().isArray()) 
        return false;
    
    for(int i=1; i < Array.getLength(val); i++){
       System.out.println("X"+ (Array.get(val,i).getClass()==
      Array.get(val,i-1).getClass()) );
       if(Array.get(val,i).getClass() == Array.get(val,i-1).getClass()){} 
       else  return false;
    }
    if( val.getClass().getComponentType() == String.class)
      return true;
    try{

    if( val.getClass().getComponentType().isArray()){
       Class CC =val.getClass().getComponentType(); 
       return isMultiArray(Array.newInstance(CC.getComponentType(),0) )  ;
    }
    }catch(Exception ss){
       return false;
    }
    Class C = val.getClass().getComponentType();
    if( !C.isPrimitive())
      return false;
    if( C == int.class)
       return true;
    if( C== float.class)
        return true;
   if( C== long.class)
       return true;
   if( C== short.class)
       return true;
   if( C== byte.class)
       return true;
   if( C== double.class)
       return true;    
     return false;
 }
 
 /**
  *  Converts to an n dimensional array if possible.
  * @param V   The Object to be converted
  * @return  The ndimensional array containing the values of V or
  *          null if not possible
  *  NOTE: This method works with zero lengthed arrays
  */
 public static Object Arrayify( Object V){
   if( V == null)
     return null;
   if( isMultiArray(V))
     return V;
   if( !(V instanceof Vector))
     if( !V.getClass().isArray())
        return V;
   if( V instanceof Vector){
     if( ((Vector)V).size() <1)
        return null;
     
     Object O;
     Class EltClass = null ;
     int size ;
     if( V instanceof Vector)
       size = ((Vector)V).size();
     else
       size = Array.getLength(V);
     if(isList(EltAt(V,0))){
     
       O = Arrayify(EltAt(V,0));
       if(O == null)
         return null;
       EltClass = O.getClass();
     }
     else{
    
       O = EltAt(V,0);
       if( O == null)
         return null;
      if( O instanceof Integer)
         EltClass = int.class;
      else if( O instanceof Float)
         EltClass = float.class;
       else if( O instanceof Short)
         EltClass = short.class;
       else if( O instanceof Long)
         EltClass = long.class;
       else if( O instanceof Double)
         EltClass = double.class;
       else if( O instanceof  Byte)
         EltClass = byte.class;
       else if( O instanceof String)
         EltClass = String.class;
       else
         return null;
     }
    Object Res = null;
    try{
     Res = Array.newInstance(EltClass,size);
    
     Array.set(Res,0, O);
    for( int i = 1; i< size; i++){
      Object O1=Arrayify(EltAt(V,i));
      if(O1 == null)
        return null;
      Array.set(Res,i, O1);
    }
    }catch( Exception ss){
       return null; 
    }
    return Res;
   }
   
   return null;
 }
 
 /**
  *   For zero length arrays it returns an instance of subcomponent with 
  *   zero length if indx ==0
  * @param V
  * @param indx
  * @return
  */
 private static Object EltAt( Object V, int indx){
    if( V == null)
      return null;
    if( indx < 0)
      return null;
    if( V instanceof Vector)
      if( indx >= ((Vector)V).size())
        return null;
      else
         return ((Vector)V).elementAt(indx);
    if( !V.getClass().isArray())
      return null;
   int L = Array.getLength(V);
   if( indx < L)
     return Array.get(V,indx);
   if( V.getClass().getComponentType().isPrimitive())
     return null;
   if( indx ==0)
      if( L == 0)
        return Array.newInstance(V.getClass().getComponentType(),0);
   return null;
       
 }
 private static boolean isList( Object val){
    if( val == null)
      return false;
    if( val instanceof Vector)
      return true;
    if( val.getClass().isArray())
       return true;
    return false;
 }
 public static Object getZeroLengthedArray( Class C){
    if(!C.isArray())
      return null;
    if( C.getComponentType().isPrimitive())
     return Array.newInstance(C.getComponentType(),0);
    Object O = getZeroLengthedArray( C.getComponentType());
    return Array.newInstance(O.getClass(),0);
 }

 public Object clone(){
    RealArrayPG Res = new RealArrayPG(getName(), getValue());
    
    return Res;
 }
}
