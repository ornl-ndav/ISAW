/*
 * File:  Qbins1PG.java
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
 * Revision 1.1  2003/10/11 19:29:21  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.*;

import DataSetTools.util.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.lang.*;

import java.util.*;

import javax.swing.*;


/**
 * This class is used to enter start, end, and number of Q values for a
 * sublist.  The constant dQ or dQ/Q choice is also supported.  It was
 * extracted out of QbinsPG because it did not properly support clone() with
 * the reflection set up in ParameterGUI when it was an inner class.
 */
class Qbins1PG extends ParameterGUI implements Concatenator {
  //~ Instance fields **********************************************************

  private JPanel Container;
  private StringEntry start;
  private StringEntry end;
  private StringEntry steps;
  private JRadioButton dQ;
  private JButton Add;
  private JButton Help;

  //~ Constructors *************************************************************

  /**
   * Creates a new Qbins1PG object.
   *
   * @param Prompt DOCUMENT ME!
   * @param val DOCUMENT ME!
   */
  public Qbins1PG( String Prompt, Object val ) {
    super( Prompt, val );
    this.type = "Qbins1";
  }

  /**
   * Creates a new Qbins1PG object.
   *
   * @param Prompt DOCUMENT ME!
   * @param val DOCUMENT ME!
   * @param valid DOCUMENT ME!
   */
  public Qbins1PG( String Prompt, Object val, boolean valid ) {
    super( Prompt, val, valid );
    this.type = "Qbins1";
  }

  //~ Methods ******************************************************************

  /**
   * DOCUMENT ME!
   *
   * @param V DOCUMENT ME!
   */
  public void setValue( Object V ) {
    if( V instanceof Vector ) {
      this.value = V;
    } else {
      Vector temp = new Vector(  );

      if( V != null ) {
        temp.addElement( V );
      }
      this.value = temp;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Object getValue(  ) {
    if( !this.initialized ) {
      return new Vector(  );
    }

    float s     = ( new Float( start.getText(  ) ) ).floatValue(  );
    float e     = ( new Float( end.getText(  ) ) ).floatValue(  );
    int n       = ( new Integer( steps.getText(  ) ) ).intValue(  );
    Vector temp = new Vector(  );
    String R;

    if( dQ.isSelected(  ) ) {
      R = "dQ";
    } else {
      R = "dQ/Q";
    }

    if( n <= 0 ) {
      temp.add( new Float( s ) );

      return temp;
    }

    if( R.equals( "dQ/Q" ) ) {
      if( ( s <= 0 ) || ( e <= 0 ) ) {
        return new Vector(  );
      }
    }

    boolean mult = false;

    if( R.equals( "dQ/Q" ) ) {
      mult = true;
    }

    float stepSize;

    if( mult ) {
      stepSize = ( float )Math.pow( e / s, 1.0 / n );
    } else {
      stepSize = ( e - s ) / n;
    }

    for( int i = 0; i <= n; i++ ) {
      temp.add( new Float( s ) );

      if( mult ) {
        s = s * stepSize;
      } else {
        s = s + stepSize;
      }
    }

    return temp;
  }

  /**
   * DOCUMENT ME!
   *
   * @param V DOCUMENT ME!
   */
  public void initGUI( Vector V ) {
    this.entrywidget = new EntryWidget(  );
    this.entrywidget.setLayout( new GridLayout( 2, 3 ) );
    start   = new StringEntry( ".0035", 7, new FloatFilter(  ) );
    end     = new StringEntry( "1.04", 7, new FloatFilter(  ) );
    steps   = new StringEntry( "117", 5, new IntegerFilter(  ) );
    dQ      = new JRadioButton( "dQ" );

    JRadioButton dQQ = new JRadioButton( "dQ/Q" );
    dQQ.setSelected( true );

    ButtonGroup Group = new ButtonGroup(  );
    Group.add( dQ );
    Group.add( dQQ );

    //dQ.setSelected( true );
    JPanel jp = new JPanel( new GridLayout( 1, 2 ) );
    jp.add( dQ );
    jp.add( dQQ );
    this.entrywidget.add( new Comb( "Start Q", start ) );
    this.entrywidget.add( new Comb( "N Steps", steps ) );
    this.entrywidget.add( new Comb( "End Q", end ) );
    this.entrywidget.add( new Comb( "Constant", jp ) );
    this.entrywidget.validate(  );
    super.initGUI(  );
  }

  /**
   * Validates this Qbins1PG.  A Qbins1PG is considered valid if its getValue(
   * ) returns a non-null, non-empty Vector.
   */
  public void validateSelf(  ) {
    Object val = getValue(  );

    if( ( val != null ) && val instanceof Vector ) {
      Vector elems = ( Vector )val;
      this.setValid( elems.size(  ) > 0 );
    } else {
      this.setValid( false );
    }
  }

  //~ Inner Classes ************************************************************

  //Utility to add a prompt to the left of text boxes, etc.
  private class Comb extends JPanel {
    //~ Constructors ***********************************************************

    /**
     * Creates a new Comb object.
     *
     * @param Prompt DOCUMENT ME!
     * @param Comp DOCUMENT ME!
     */
    public Comb( String Prompt, JComponent Comp ) {
      super( new GridLayout( 1, 2 ) );
      add( new JLabel( Prompt, SwingConstants.CENTER ) );
      add( Comp );
    }
  }
    //Comb
}
  //Qbins1
