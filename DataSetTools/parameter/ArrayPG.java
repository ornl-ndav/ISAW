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
 *  Revision 1.36  2003/12/15 02:51:31  bouzekc
 *  Changed call to execOneLine's Vect_to_String to call upon the class name
 *  rather than an instance, as the method is static.  Removed creation of
 *  an execOneLine instance immmediately before this call, as it was no longer
 *  needed.
 *
 *  Revision 1.35  2003/12/15 01:39:13  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.34  2003/11/19 04:13:21  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.33  2003/10/11 18:59:06  bouzekc
 *  Removed clone() as ParameterGUI now implements it.
 *
 *  Revision 1.32  2003/09/16 22:46:50  bouzekc
 *  Removed addition of this as a PropertyChangeListener.  This is already done
 *  in ParameterGUI.  This should fix the excessive events being fired.
 *
 *  Revision 1.31  2003/09/16 19:57:12  bouzekc
 *  Removed the protected variable "value" which interfered with the superclass
 *  constructor and methods.  Modified existing methods to cast "value" to a
 *  Vector.
 *
 *  Revision 1.30  2003/09/15 22:51:08  bouzekc
 *  Fixed commenting problem which prevented compilation.
 *
 *  Revision 1.29  2003/09/15 22:48:54  bouzekc
 *  initGUI(Vector) now calls setValue() if the passed-in value is not null.
 *
 *  Revision 1.28  2003/09/13 23:29:45  bouzekc
 *  Moved calls from setValid(true) to validateSelf().
 *
 *  Revision 1.27  2003/09/09 23:06:25  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.26  2003/08/28 02:28:08  bouzekc
 *  Removed setEnabled() method.
 *
 *  Revision 1.25  2003/08/28 01:34:44  bouzekc
 *  Modified to work with new ParameterGUI.
 *
 *  Revision 1.24  2003/08/22 20:12:08  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.23  2003/08/16 01:38:26  bouzekc
 *  Fixed incorrect comment.
 *
 *  Revision 1.22  2003/08/15 23:50:03  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.21  2003/08/06 17:12:39  bouzekc
 *  Removed debugging println() in stringtoArray().  Now prints the message
 *  rather than the stacktrace when a ParseException is caught.
 *
 *  Revision 1.20  2003/08/05 23:10:11  bouzekc
 *  StringtoArray() now uses JavaCC to parse input.
 *
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

import Command.JavaCC.*;

import Command.execOneLine;

import DataSetTools.components.ParametersGUI.EntryWidget;
import DataSetTools.dataset.DataSet;

import java.awt.*;

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

  //~ Constructors *************************************************************

  /**
   * Creates a new ArrayPG object.
   *
   * @param name Name of this ArrayPG.
   * @param val Value of this ArrayPG.
   */
  public ArrayPG( String name, Object val ) {
    super( name, val );
    setType( TYPE );
  }

  /**
   * Creates a new ArrayPG object.
   *
   * @param name Name of this ArrayPG.
   * @param val Initial value of this ArrayPG.
   * @param valid Whether it is valid or not.
   */
  public ArrayPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    setType( TYPE );
  }

  //~ Methods ******************************************************************

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
    return ArraytoString( ( Vector )getValue(  ) );
  }

  /**
   * Sets the value of this ArrayPG.
   *
   * @param val The value to set.
   */
  public void setValue( Object val ) {
    Vector vecVal = null;

    if( val == null ) {
      vecVal = new Vector(  );
    } else if( val instanceof Vector ) {
      vecVal = ( Vector )val;
    } else if( val instanceof String ) {
      vecVal = StringtoArray( ( String )val );
    } else {
      return;
    }

    if( getInitialized(  ) ) {
      ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).setText( 
        ArraytoString( vecVal ) );
    }

    //always update internal value
    super.setValue( vecVal );
  }

  /**
   * Accessor method to retrieve the value of this ArrayPG.  If this is called
   * and this ArrayPG has been initialized, it also sets the internal value to
   * the GUI value.
   *
   * @return The value of this ArrayPG.
   */
  public Object getValue(  ) {
    //Vector of DataSets
    Object val = super.getValue(  );

    if( 
      ( val != null ) && ( ( ( Vector )val ).size(  ) > 0 ) &&
        ( ( ( Vector )val ).elementAt( 0 ) instanceof DataSet ) ) {
      return val;
    }

    if( getInitialized(  ) ) {
      val = ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).getText(  );
      val = StringtoArray( val.toString(  ) );
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

    String res                   = execOneLine.Vect_to_String( V );

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
   * Add a single item to the Vector.
   *
   * @param val The item to add.
   */
  public void addItem( Object val ) {
    if( val == null ) {
      return;  // don't add null to the vector
    }

    if( getValue(  ) == null ) {
      setValue( new Vector(  ) );  // initialize if necessary
    }

    Vector values = ( Vector )getValue(  );

    if( values.indexOf( val ) < 0 ) {
      values.add( val );  // add if unique
      setValue( values );
    }

    if( getInitialized(  ) ) {
      ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).setText( 
        ArraytoString( values ) );
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
    if( getValue(  ) == null ) {
      return;
    }
    ( ( Vector )getValue(  ) ).clear(  );

    if( getInitialized(  ) ) {
      ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).setText( 
        ArraytoString( ( Vector )getValue(  ) ) );
    }
  }

  /**
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The Vector of values to initialize this ArrayPG to.
   */
  public void initGUI( Vector init_values ) {
    if( getInitialized(  ) ) {
      return;  // don't initialize more than once
    }

    if( init_values != null ) {
      setValue( init_values );
    }
    setEntryWidget( 
      new EntryWidget( 
        new JTextField( ArraytoString( ( Vector )getValue(  ) ) ) ) );

    //we'll set a really small preferred size and let the Layout Manager take
    //over at that point
    getEntryWidget(  )
      .setPreferredSize( new Dimension( 2, 2 ) );
    super.initGUI(  );
  }

  /**
   * Since this is an array parameter, better allow an array to initialize the
   * GUI.
   *
   * @param init_values The array of Objects to initialize this ArrayPG to.
   */
  public void initGUI( Object[] init_values ) {
    Vector init_vec = new Vector(  );

    for( int i = 0; i < init_values.length; i++ ) {
      init_vec.add( init_values[i] );
    }

    //call the "regular" initGUI( Vector )
    initGUI( init_vec );
  }

  /**
   * Remove an item based on its key.
   *
   * @param val The key of the item to remove.
   */
  public void removeItem( Object val ) {
    int index = ( ( Vector )getValue(  ) ).indexOf( val );
    removeItem( index );

    if( getInitialized(  ) ) {
      ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).setText( 
        ArraytoString( ( Vector )getValue(  ) ) );
    }
  }

  /**
   * Remove an item based on its index.
   *
   * @param index The index of the item to remove.
   */
  public void removeItem( int index ) {
    if( ( index >= 0 ) && ( index < ( ( Vector )getValue(  ) ).size(  ) ) ) {
      ( ( Vector )getValue(  ) ).remove( index );
    }

    if( getInitialized(  ) ) {
      ( ( JTextField )( getEntryWidget(  ).getComponent( 0 ) ) ).setText( 
        ArraytoString( ( Vector )getValue(  ) ) );
    }
  }

  /**
   * Validates this ArrayPG.  An ArrayPG is considered valid if getValue() does
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

  /*
   * Main method for testing purposes.
   */
  /*public static void main( String[] args ) {
     ArrayPG fpg;
     int y  = 0;
     int dy = 70;
     Vector vals = new Vector(  );
     vals.add( "C:\\\\Windows\\System" );
     vals.add( "C:\\\\Windows\\System\\My Documents" );
     vals.add( "/home/myhome/atIPNS" );
     vals.add( "some/more\\random@garbage!totry2" );
     vals.add( "10:15" );
     fpg = new ArrayPG( "a", vals, true );
     System.out.println( "Before calling init, the ArrayPG is " );
     System.out.println( fpg );
     fpg.initGUI( vals );
     System.out.print( fpg.getValue(  ) + "\n" );
     fpg.showGUIPanel( 0, y );
        y += dy;
        vals.add( new StringBuffer( "tim" ) );
        fpg = new ArrayPG( "b", vals );
        System.out.println( fpg );
        fpg.setEnabled( false );
        fpg.initGUI( null  );
        fpg.showGUIPanel( 0, y );
        y += dy;
        vals = new Vector(  );
        for( int i = 1; i <= 20; i++ ) {
          vals.add( new Integer( i ) );
        }
        fpg = new ArrayPG( "c", vals, false );
        System.out.println( fpg );
        fpg.setEnabled( false );
        fpg.initGUI( null );
        fpg.showGUIPanel( 0, y );
        y += dy;
        vals = new Vector(  );
        for( float f = 1f; f < 100; f *= 2 ) {
          vals.add( new Float( f ) );
        }
        fpg = new ArrayPG( "d", vals, true );
        System.out.println( fpg );
        fpg.setDrawValid( true );
        fpg.initGUI( vals );
        fpg.showGUIPanel( 0, y );
        y += dy;
     }*/

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

    int same     = 1;
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
    Object obj = getValue(  );

    if( ( obj == null ) || ( ( ( Vector )obj ).size(  ) <= 0 ) ) {
      return "";
    } else {
      StringBuffer result = new StringBuffer(  );
      Vector val          = ( Vector )getValue(  );
      int numElements     = val.size(  );
      int start           = 0;
      int index           = 0;

      while( start < numElements ) {
        index = checkSame( val, start );
        result.append( 
          shortName( val.elementAt( ( index + start ) - 1 ) ) + "[" + index +
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
        return '[' + val.toString(  ) + ']';
      }
    }
  }
}
