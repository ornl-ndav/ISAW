/*
 * File:  ArrayPG.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 *  Revision 1.19  2003/07/15 22:57:22  bouzekc
 *  getVectorValue() now calls getValue(), rather than just
 *  returning the value.  Modified StringtoArray to handle
 *  colons in non-numeric Strings.
 *
 *  Revision 1.18  2003/07/11 21:29:36  bouzekc
 *  Removed a call to setValue() that prevented displaying the
 *  ArrayPG's value on occasion.
 *
 *  Revision 1.17  2003/07/10 18:15:28  bouzekc
 *  Added missing constructor documentation.
 *
 *  Revision 1.16  2003/07/10 15:25:33  bouzekc
 *  Now handles colons (:) within Strings.
 *
 *  Revision 1.15  2003/07/09 16:53:47  bouzekc
 *  Added all missing javadocs.  init() now actually sets the
 *  value that is passed in as the ArrayPG's value.
 *
 *  Revision 1.14  2003/07/09 16:32:22  bouzekc
 *  Removed unnecessary "this" qualifiers (clone() still has
 *  its "this" qualifiers).  Sets a small preferred size on
 *  the entry widget to avoid oversizing the GridLayout GUI
 *  that it sits in.
 *
 *  Revision 1.13  2003/07/01 21:06:18  bouzekc
 *  Uses execOneLine again.  Fixed a bug where execOneLine is
 *  sent a String array without quotes around the individual
 *  elements.  Added documentation to StringtoArray.
 *
 *  Revision 1.12  2003/06/27 18:52:53  bouzekc
 *  Now uses a simple string parsing routine rather than
 *  execOneLine to parse the array in stringToArray().
 *
 *  Revision 1.11  2003/06/27 16:35:26  bouzekc
 *  Reformatted for consistency.
 *
 *  Revision 1.10  2003/06/12 22:39:42  bouzekc
 *  Fixed bug where an array of DataSets was not returned
 *  properly in getValue().  Added code to complete the
 *  setEnabled() method.
 *
 *  Revision 1.9  2003/06/12 21:54:22  bouzekc
 *  Fixed class cast problem in setValue().  Changed non-class
 *  "value" variables to "val" to avoid confusion.
 *
 *  Revision 1.8  2003/06/10 14:40:42  rmikk
 *  Now implements ParamUsesString
 *  ArraytoString and StringtoArray are now public static
 *  ArraytoString works with null Vectors
 *
 *  Revision 1.7  2003/06/03 21:59:37  rmikk
 *  -Created an entrywidget for text entry of Vectors
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2003/02/24 21:01:36  pfpeterson
 *  Major reworking. This version is completely incompatible with previous
 *  versions. Value changed to a vector which cannot be changed in the GUI.
 *  This should be subclassed for particular types of vectors in order to
 *  make the value changable in the GUI.
 *
 *  Revision 1.4  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/10 22:11:49  pfpeterson
 *  Fixed a bug with the clone method not getting the choices copied over.
 *
 *  Revision 1.2  2002/09/19 16:07:20  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.1  2002/08/01 18:40:01  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.parameter;

import Command.execOneLine;

import DataSetTools.components.ParametersGUI.HashEntry;

import DataSetTools.dataset.DataSet;

import java.awt.*;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;


/**
 * This is a superclass to take care of many of the common details of Array
 * Parameter GUIs.
 */
public class ArrayPG extends ParameterGUI implements ParamUsesString {
  //~ Static fields/initializers ***********************************************

  private static String TYPE    = "Array";
  protected static int DEF_COLS = 20;

  //~ Instance fields **********************************************************

  protected Vector value = null;

  //~ Constructors *************************************************************

  /**
   * Creates a new ArrayPG object.
   *
   * @param name Name of this ArrayPG.
   * @param val Value of this ArrayPG.
   */
  public ArrayPG( String name, Object val ) {
    this( name, val, false );
    setDrawValid( false );
    type = TYPE;
  }

  /**
   * Creates a new ArrayPG object.
   *
   * @param name Name of this ArrayPG.
   * @param val Initial value of this ArrayPG.
   * @param valid Whether it is valid or not.
   */
  public ArrayPG( String name, Object val, boolean valid ) {
    setName( name );
    setValue( val );
    setEnabled( true );
    setValid( valid );
    setDrawValid( true );
    type                 = TYPE;
    initialized          = false;
    ignore_prop_change   = false;
  }

  //~ Methods ******************************************************************

  /**
   * Sets the editable state of the entry widget.
   *
   * @param enableMe Whether to make the entry widget enabled.
   */
  public void setEnabled( boolean enableMe ) {
    enabled = enableMe;

    if( getEntryWidget(  ) != null ) {
      ( ( JTextField )entrywidget ).setEditable( enabled );
    }
  }

  /**
   * Sets the String value of this ArrayPG.
   *
   * @param val The String value to set it to.
   */
  public void setStringValue( java.lang.String val ) {
    setValue( val );
  }

  /**
   * Accessor method for this ArrayPG's String value.
   *
   * @return The String value associated with this ArrayPG.
   */
  public String getStringValue(  ) {
    return ArraytoString( value );
  }

  /**
   * Sets the value of this ArrayPG.
   *
   * @param val The value to set.
   */
  public void setValue( Object val ) {
    if( val == null ) {
      value = null;
    } else if( val instanceof Vector ) {
      value = ( Vector )val;
    } else if( val instanceof String ) {
      value = StringtoArray( ( String )val );
    } else {
      return;
    }

    if( initialized ) {
      ( ( JTextField )( entrywidget ) ).setText( ArraytoString( value ) );
    }

    setValid( true );
  }

  /**
   * Accessor method to retrieve the value of this ArrayPG.
   *
   * @return The value of this ArrayPG.
   */
  public Object getValue(  ) {
    //Vector of DataSets
    if( 
      ( value != null ) && ( value.size(  ) > 0 ) &&
        ( value.elementAt( 0 ) instanceof DataSet ) ) {
      return value;
    }

    Object val = null;

    if( initialized ) {
      String StringValue = ( ( JTextField )entrywidget ).getText(  );

      val = StringtoArray( StringValue );
    } else {
      val = value;
    }

    return val;
  }

  /**
   * Fast-access method to get the type cast value.
   *
   * @return The Vector cast value of this ArrayPG.
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

    Command.execOneLine execLine = new Command.execOneLine(  );
    String res                   = execLine.Vect_to_String( V );

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
    if( S == null ) {
      return null;
    }

    //now, the array may come in in a "bad" format...i.e. [String1, String2],
    //rather than ["String1","String2"] which is what execOneLine expects.  So,
    //we'll parse the String into a Vector
    String temp          = S;
    String expansion;
    String frontTemp;
    String backTemp;
    Vector elements      = new Vector(  );
    Vector expanded;
    execOneLine execLine = new execOneLine(  );
    int colIndex;

    //pull out the brackets
    temp   = temp.replace( '[', ' ' );
    temp   = temp.replace( ']', ' ' );

    //pull out the quotes
    temp = temp.replace( '"', ' ' );

    //extract the elements
    StringTokenizer st = new StringTokenizer( temp, "," );

    while( st.hasMoreTokens(  ) ) {
      //trim out the white space
      elements.add( st.nextToken(  ).trim(  ) );
    }

    //expand on a:b...if this is here, it will be an element in the Vector.
    //Backwards traverse so we can remove a:b elements and parse them
    for( int k = elements.size(  ) - 1; k >= 0; k-- ) {
      //ignore bad types...we will assume that they wanted a String
      expansion   = elements.elementAt( k )
                            .toString(  );
      colIndex = expansion.indexOf( ":" );

      if( colIndex > 0 ) {
        //we need to trap things like C:\\windows
        frontTemp   = expansion.substring( 0, colIndex );
        backTemp    = expansion.substring( colIndex + 1, expansion.length(  ) );

        try {
          //try this as a digit:digit pair.  We don't care about the parse,
          //just whether or not we can
          Integer.parseInt( frontTemp );
          Integer.parseInt( backTemp );

          //if we got this far, we are good to go, so remove the element
          elements.remove( k );

          //assuming this is an integer expansion, and execOneLine needs "[]"
          expansion = "[" + expansion + "]";

          //add the Collection (i.e. new Vector)
          elements.addAll( k, parseLine( execLine, expansion ) );
        } catch( NumberFormatException nfe ) {
          //not an expandable integer list, so just drop this exception on the
          //floor
        }
      }
    }

    //now return it to the correct String format
    S = ArrayPG.ArraytoString( elements );

    //now we can send it to execOneLine
    return parseLine( execLine, S );
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
   * Add a single item to the Vector.
   *
   * @param val The item to add.
   */
  public void addItem( Object val ) {
    if( value == null ) {
      value = new Vector(  );  // initialize if necessary
    }

    if( val == null ) {
      return;  // don't add null to the vector
    }

    if( value.indexOf( val ) < 0 ) {
      value.add( val );  // add if unique
    }

    if( initialized ) {
      ( ( JTextField )entrywidget ).setText( ArraytoString( value ) );
    }
  }

  /**
   * Add a set of items to the Vector at once.
   *
   * @param values The Vector to add.
   */
  public void addItems( Vector values ) {
    for( int i = 0; i < values.size(  ); i++ ) {
      addItem( values.elementAt( i ) );
    }
  }

  /**
   * Calls Vector.clear() on the value.
   */
  public void clearValue(  ) {
    if( value == null ) {
      return;
    }

    value.clear(  );

    if( initialized ) {
      ( ( JTextField )entrywidget ).setText( ArraytoString( ( Vector )value ) );
    }
  }

  /**
   * Definition of the clone method.
   */
  public Object clone(  ) {
    ArrayPG apg = new ArrayPG( this.name, this.value, this.valid );

    apg.setDrawValid( this.getDrawValid(  ) );
    apg.initialized = false;

    return apg;
  }

  /**
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The Vector of values to initialize this ArrayPG to.
   */
  public void init( Vector init_values ) {
    if( initialized ) {
      return;  // don't initialize more than once
    }

    entrywidget = new JTextField( ArraytoString( value ) );

    //we'll set a really small preferred size and let the Layout Manager take
    //over at that point
    entrywidget.setPreferredSize( new Dimension( 2, 2 ) );
    super.initGUI(  );
  }

  /**
   * Since this is an array parameter, better allow an array to initialize the
   * GUI.
   *
   * @param init_values The array of Objects to initialize this ArrayPG to.
   */
  public void init( Object[] init_values ) {
    Vector init_vec = new Vector(  );

    for( int i = 0; i < init_values.length; i++ ) {
      init_vec.add( init_values[i] );
    }

    init( init_vec );
  }

  /**
   * Remove an item based on its key.
   *
   * @param val The key of the item to remove.
   */
  public void removeItem( Object val ) {
    int index = value.indexOf( val );

    removeItem( index );
  }

  /**
   * Remove an item based on its index.
   *
   * @param index The index of the item to remove.
   */
  public void removeItem( int index ) {
    if( ( index >= 0 ) && ( index < value.size(  ) ) ) {
      value.remove( index );
    }
  }

  /**
   * Main method for testing purposes.
   */
  static void main( String[] args ) {
    ArrayPG fpg;
    int y  = 0;
    int dy = 70;

    Vector vals = new Vector(  );

    vals.add( "C:\\\\Windows\\System" );
    vals.add( "/home/myhome/atIPNS" );
    vals.add( "some/more\\random@garbage!totry2" );
    vals.add( "10:15" );

    fpg = new ArrayPG( "a", vals );
    System.out.println( "Before calling init, the ArrayPG is " );
    System.out.println( fpg );
    fpg.init(  );
    System.out.print( fpg.getValue(  ) + "\n" );
    System.exit( 0 );

    /*fpg.showGUIPanel( 0, y );
       y += dy;
       vals.add( new StringBuffer( "tim" ) );
       fpg = new ArrayPG( "b", vals );
       System.out.println( fpg );
       fpg.setEnabled( false );
       fpg.init(  );
       fpg.showGUIPanel( 0, y );
       y += dy;
       vals = new Vector(  );
       for( int i = 1; i <= 20; i++ ) {
         vals.add( new Integer( i ) );
       }
       fpg = new ArrayPG( "c", vals, false );
       System.out.println( fpg );
       fpg.setEnabled( false );
       fpg.init(  );
       fpg.showGUIPanel( 0, y );
       y += dy;
       vals = new Vector(  );
       for( float f = 1f; f < 100; f *= 2 ) {
         vals.add( new Float( f ) );
       }
       fpg = new ArrayPG( "d", vals, true );
       System.out.println( fpg );
       fpg.setDrawValid( true );
       fpg.init( vals );
       fpg.showGUIPanel( 0, y );
       y += dy;*/
  }

  /**
   * Determines how many elements are of the same class.
   *
   * @param vals Vector of values to check.
   * @param start The index of the item you wish to check against the other
   *        items.
   */
  private int checkSame( Vector vals, int start ) {
    if( ( vals == null ) || ( vals.size(  ) <= 0 ) ) {
      return -1;
    }

    if( start >= vals.size(  ) ) {
      return -1;
    }

    int same = 1;

    String first = vals.elementAt( start )
                       .getClass(  )
                       .getName(  );

    for( int i = 1; i < vals.size(  ); i++ ) {
      if( first.equals( vals.elementAt( i ).getClass(  ).getName(  ) ) ) {
        same++;
      } else {
        return same;
      }
    }

    return same;
  }

  /**
   * Create a short version of a class name based on the object provided.
   *
   * @param obj The object to get the class name of.
   *
   * @return The shortened class name.
   */
  private String shortName( Object obj ) {
    // get the name of the class
    String res = obj.getClass(  )
                    .getName(  );

    // determine what to trim off
    int start = res.lastIndexOf( "." );
    int end   = res.length(  );

    if( res.endsWith( ";" ) ) {
      end--;
    }

    // return the trimmed version
    if( ( start >= 0 ) && ( end >= 0 ) ) {
      return res.substring( start + 1, end );
    } else {
      return res;
    }
  }

  /**
   * Creates the string to be placed in the label for the GUI.
   *
   * @return The String label.
   */
  private String stringVersion(  ) {
    if( ( value == null ) || ( value.size(  ) <= 0 ) ) {
      return "";
    } else {
      StringBuffer result = new StringBuffer(  );
      int numElements     = value.size(  );
      int start           = 0;
      int index           = 0;

      while( start < numElements ) {
        index = checkSame( value, start );
        result.append( 
          shortName( value.elementAt( ( index + start ) - 1 ) ) + "[" + index +
          "]" );
        start = start + index;

        if( start < numElements ) {
          result.append( ", " );
        }

        index = 0;
      }

      if( result.length(  ) > 0 ) {
        return '[' + result.toString(  ) + ']';
      } else {
        return '[' + value.toString(  ) + ']';
      }
    }
  }
}
