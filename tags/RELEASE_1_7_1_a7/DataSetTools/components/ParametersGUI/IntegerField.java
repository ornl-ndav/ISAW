/*
 * File: IntegerField.java
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
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.7  2004/01/24 22:00:06  bouzekc
 *  Now inherits from StringField.
 *
 *  Revision 1.6  2003/12/14 19:20:41  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.5  2003/08/14 19:11:49  bouzekc
 *  Made inner Toolkit transient.
 *
 *  Revision 1.4  2002/11/27 23:12:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/06/06 16:06:50  pfpeterson
 *  Reorganized some of the code and class hierarchy.
 *
 *  Revision 1.2  2002/05/31 19:32:46  pfpeterson
 *  Now fire PropertyChangeEvent when the value in widget is changed.
 *
 *  Revision 1.1  2002/03/08 16:19:49  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.components.ParametersGUI;

/**
 * This class is intended to be used as a replacement for JTextField whan a
 * integer value is to be entered. The major difference is an overridden
 * insertString method which beeps when something that isn't found in an
 * integer is entered.
 */
public class IntegerField extends StringField {
  //~ Static fields/initializers ***********************************************

  private static Character MINUS = new Character( 
      ( new String( "-" ) ).charAt( 0 ) );
  private static Character ZERO  = new Character( 
      ( new String( "0" ) ).charAt( 0 ) );

  //~ Constructors *************************************************************

  /**
   * Constructs an IntegerField with the appropriate number of columns and the
   * default value of zero.
   */
  public IntegerField( int columns ) {
    this( 0, columns );
  }

  /**
   * Constructs an IntegerField with a specified default value and number of
   * columns.
   */
  public IntegerField( int value, int columns ) {
    super( ( new Integer( value ) ).toString(  ), columns );
  }

  //~ Methods ******************************************************************

  /**
   * Internal method to confirm that the text can be added.  This checks that
   * number is an integer.
   *
   * @param offset The offset to use for inserting.
   * @param insertString The String to insert.
   * @param currentString The String that currently exists in the StringField
   */
  protected boolean isOkay( 
    int offset, String insertString, String currentString ) {
    char[] source = insertString.toCharArray(  );

    for( int i = 0; i < source.length; i++ ) {
      if( Character.isDigit( source[i] ) ) {
        if( ZERO.compareTo( new Character( source[i] ) ) == 0 ) {
          if( ( ( offset + i ) == 0 ) && ( currentString.length(  ) > 0 ) ) {
            return false;
          } else if( currentString.startsWith( MINUS.toString(  ) ) ) {
            if( ( offset + i ) == 1 ) {
              return false;
            } else {
              // do nothing
            }
          } else {
            // do nothing
          }
        } else {
          // do nothing
        }
      } else if( MINUS.compareTo( new Character( source[i] ) ) == 0 ) {
        if( ( offset + i ) == 0 ) {
          if( currentString.startsWith( MINUS.toString(  ) ) ) {
            return false;
          } else {
            // do nothing
          }
        } else {
          return false;
        }
      } else {
        return false;
      }
    }

    return true;
  }
}
