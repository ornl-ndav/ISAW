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
 * Revision 1.7  2004/03/12 21:13:49  bouzekc
 * Added clear() method.
 *
 * Revision 1.6  2004/03/12 21:12:11  bouzekc
 * Added javadocs.
 *
 * Revision 1.5  2004/01/21 18:01:39  bouzekc
 * Removed unused local variables.
 *
 * Revision 1.4  2003/12/16 00:06:00  bouzekc
 * Removed unused imports.
 *
 * Revision 1.3  2003/11/19 04:13:22  bouzekc
 * Is now a JavaBean.
 *
 * Revision 1.2  2003/11/09 22:25:13  rmikk
 * -Changed the class to be a public class
 * -This class is now valid if its value is a Vector with no elements in it
 *
 * Revision 1.1  2003/10/11 19:29:21  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.EntryWidget;
import DataSetTools.components.ParametersGUI.StringEntry;

import DataSetTools.util.FloatFilter;
import DataSetTools.util.IntegerFilter;

import java.awt.GridLayout;

import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;


/**
 * This class is used to enter start, end, and number of Q values for a
 * sublist.  The constant dQ or dQ/Q choice is also supported.  It was
 * extracted out of QbinsPG because it did not properly support clone() with
 * the reflection set up in ParameterGUI when it was an inner class.
 */
public class Qbins1PG extends ParameterGUI implements Concatenator {
  //~ Instance fields **********************************************************

  private StringEntry start;
  private StringEntry end;
  private StringEntry steps;
  private JRadioButton dQ;

  //~ Constructors *************************************************************

  /**
   * Creates a new Qbins1PG object.
   *
   * @param Prompt The name of this PG.
   * @param val The initial value of this PG.
   */
  public Qbins1PG( String Prompt, Object val ) {
    super( Prompt, val );
    this.setType( "Qbins1" );
  }

  /**
   * Creates a new Qbins1PG object.
   *
   * @param Prompt The name of this PG.
   * @param val The initial value of this PG.
   * @param valid Whether this PG should be considered initially valid.
   */
  public Qbins1PG( String Prompt, Object val, boolean valid ) {
    super( Prompt, val, valid );
    this.setType( "Qbins1" );
  }

  //~ Methods ******************************************************************

  /**
   * Sets the value of this QBinsPG.  This really wants a Vector.
   *
   * @param V The new value.
   */
  public void setValue( Object V ) {
    if( V instanceof Vector ) {
      super.setValue( V );
    } else {
      Vector temp = new Vector(  );

      if( V != null ) {
        temp.addElement( V );
      }

      super.setValue( temp );
    }
  }

  /**
   * @return A Vector representation of the Qbins.
   */
  public Object getValue(  ) {
    if( !this.getInitialized(  ) ) {
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
   * Used to clear out the PG.  This resets the Vector to an empty one.
   */
  public void clear(  ) {
    setValue( new Vector(  ) );
  }

  /**
   * Initializes the GUI.
   *
   * @param V The new value.
   */
  public void initGUI( Vector V ) {
    this.setEntryWidget( new EntryWidget(  ) );
    this.getEntryWidget(  ).setLayout( new GridLayout( 2, 3 ) );
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
    this.getEntryWidget(  ).add( new Comb( "Start Q", start ) );
    this.getEntryWidget(  ).add( new Comb( "N Steps", steps ) );
    this.getEntryWidget(  ).add( new Comb( "End Q", end ) );
    this.getEntryWidget(  ).add( new Comb( "Constant", jp ) );
    this.getEntryWidget(  ).validate(  );
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

      this.setValid( elems.size(  ) >= 0 );
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
