/*
 * File: StringField.java
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
 *  Revision 1.5  2004/01/23 15:33:49  bouzekc
 *  Reformatted for clarity.
 *
 *  Revision 1.4  2003/08/14 19:11:49  bouzekc
 *  Made inner Toolkit transient.
 *
 *  Revision 1.3  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/06/06 16:06:51  pfpeterson
 *  Reorganized some of the code and class hierarchy.
 *
 *  Revision 1.1  2002/05/31 19:32:10  pfpeterson
 *  Added to CVS.
 *
 *  Revision 1.1  2002/03/08 16:19:49  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.components.ParametersGUI;

import DataSetTools.parameter.*;

import java.awt.Toolkit;

import java.beans.*;

import java.util.Locale;

import javax.swing.*;
import javax.swing.text.*;


/**
 * This class is intended to be used as a replacement for JTextField whan a
 * integer value is to be entered. The major difference is an overridden
 * insertString method which beeps when something that isn't found in an
 * integer is entered.
 */
public class StringField extends JTextField {
  //~ Instance fields **********************************************************

  private transient Toolkit toolkit;
  private PropertyChangeSupport propBind = new PropertyChangeSupport( this );

  //~ Constructors *************************************************************

  /**
   * Constructs an StringField with the appropriate number of columns and the
   * default value of zero.
   */
  public StringField( int columns ) {
    this( "", columns );
  }

  /**
   * Constructs an StringField with a specified default value and number of
   * columns.
   */
  public StringField( String value, int columns ) {
    super( value, columns );
    toolkit = Toolkit.getDefaultToolkit(  );
  }

  //~ Methods ******************************************************************

  /**
   * DOCUMENT ME!
   *
   * @param prop DOCUMENT ME!
   * @param pcl DOCUMENT ME!
   */
  public void addPropertyChangeListener( 
    String prop, PropertyChangeListener pcl ) {
    super.addPropertyChangeListener( prop, pcl );

    if( propBind != null ) {
      propBind.addPropertyChangeListener( prop, pcl );
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param pcl DOCUMENT ME!
   */
  public void addPropertyChangeListener( PropertyChangeListener pcl ) {
    super.addPropertyChangeListener( pcl );

    if( propBind != null ) {
      propBind.addPropertyChangeListener( pcl );
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param pcl DOCUMENT ME!
   */
  public void removePropertyChangeListener( PropertyChangeListener pcl ) {
    super.removePropertyChangeListener( pcl );

    if( propBind != null ) {
      propBind.removePropertyChangeListener( pcl );
    }
  }

  /**
   * A hook to override the insertString method.
   */
  protected Document createDefaultModel(  ) {
    return new StringDocument( this );
  }

  /**
   * Internal method to confirm that the text can be added.
   */
  private boolean isOkay( int offs, String inString, String curString ) {
    return true;
  }

  //~ Inner Classes ************************************************************

  /**
   * Internal class to do all of the formatting checks.
   */
  protected class StringDocument extends PlainDocument {
    //~ Instance fields ********************************************************

    private StringField textBox;

    //~ Constructors ***********************************************************

    /**
     * Creates a new StringDocument object.
     *
     * @param T DOCUMENT ME!
     */
    public StringDocument( StringField T ) {
      super(  );
      textBox = T;
    }

    //~ Methods ****************************************************************

    /**
     * Overrides the default insertString method. Insert if okay, beep if not.
     */
    public void insertString( int offs, String str, AttributeSet a )
      throws BadLocationException {
      String oldText = textBox.getText(  );

      if( textBox.isOkay( offs, str, textBox.getText(  ) ) ) {
        super.insertString( offs, str, a );

        if( propBind != null ) {
          propBind.firePropertyChange( 
            IParameter.VALUE, oldText, textBox.getText(  ) );
        }
      } else {
        toolkit.beep(  );
      }
    }

    /**
     * Overrides the default remove method.
     */
    public void remove( int offs, int len ) throws BadLocationException {
      String oldText = textBox.getText(  );

      super.remove( offs, len );

      //System.out.println("value:"+oldText+"->"+textBox.getText());
      if( propBind != null ) {
        propBind.firePropertyChange( 
          IParameter.VALUE, oldText, textBox.getText(  ) );
      }
    }
  }
}
