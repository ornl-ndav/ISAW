/*
 * File:  ArrayEntryJPanel.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.9  2003/07/07 22:41:37  bouzekc
 * Now uses VectorPG's class constant for the "Data Changed"
 * event.
 *
 * Revision 1.8  2003/07/07 22:20:46  bouzekc
 * Now gives focus to the entry widget when the panel is
 * shown.  Highlights all text within an entrywidget if it
 * is a JTextComponent.  Saves values when window is closed
 * (bug fix from conversion back to JDialog).
 *
 * Revision 1.7  2003/07/07 21:21:20  bouzekc
 * Reorganized methods according to access privilege.
 *
 * Revision 1.6  2003/06/30 22:28:11  bouzekc
 * Changed "Show Items" to "Show Item"
 *
 * Revision 1.5  2003/06/30 22:22:53  bouzekc
 * Now implements KeyListener and listens for <Enter> key
 * presses in the data entry text field.
 *
 * Revision 1.4  2003/06/30 22:10:02  bouzekc
 * Now uses ParameterViewer to show values of the elements in
 * its list.  Now implements PropertyChangeListener rather than
 * using an inner class.  Changed the edit button to be a value
 * changing button, and updated the label to reflect this.
 * Removed inner SetValueActionListener class.  Added method
 * comments.
 *
 * Revision 1.3  2003/06/30 21:09:22  bouzekc
 * Removed "Set Value" button and moved its functionality to
 * the "Add" button.  Changed private method newVal() to
 * setInnerParameterValue().  "Add" button now has
 * SetValueActionListener rather than the ArrayEntryJPanel
 * as its ActionListener.
 *
 * Revision 1.2  2003/06/30 20:34:23  bouzekc
 * Moved button names into final instance variables.
 *
 * Revision 1.1  2003/06/24 20:24:36  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.components.ParametersGUI;

import DataSetTools.parameter.ParameterGUI;
import DataSetTools.parameter.ParameterViewer;
import DataSetTools.parameter.VectorPG;

import DataSetTools.util.PropertyChanger;
import DataSetTools.util.StringUtil;

import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.Vector;

import javax.swing.*;
import javax.swing.text.JTextComponent;


/**
 * This is a class to allow entry of values into one or two dimensional arrays.
 * It uses String entry methods to enter floats, ints, Strings, etc.  This
 * class was extracted from VectorPG and redesigned.
 */
public class ArrayEntryJPanel extends JPanel implements ActionListener,
  PropertyChanger, PropertyChangeListener, KeyListener {
  //~ Instance fields **********************************************************

  private final String UP_LABEL       = new String( "Move Item Up" );
  private final String DOWN_LABEL     = new String( "Move Item Down" );
  private final String DELETE_LABEL   = new String( "Delete Item" );
  private final String ADD_LABEL      = new String( "Add Item" );
  private final String CHANGE_LABEL   = new String( "Change Value" );
  private final String DONE_LABEL     = new String( "Done" );
  private final String SHOW_LABEL     = new String( "Show Item" );
  private JList jlist;
  private DefaultListModel jlistModel;
  private JButton Delete;
  private JButton Add;
  private JButton Up;
  private JButton Down;
  private JButton Change;
  private JButton Done;
  private JButton Show;
  private PropertyChangeSupport pcs;
  private Vector oldVector;
  private ParameterGUI param;
  private JFrame jf                   = null;
  private boolean isShowing           = false;
  private int position                = -1;

  //~ Constructors *************************************************************

  /**
   * ArrayEntryJPanel constructor.
   *
   * @param param ParameterGUI that determines the resultant type of the
   *        elements stored in the ArrayEntryPanel.
   */
  public ArrayEntryJPanel( ParameterGUI param ) {
    super( new BorderLayout(  ) );
    oldVector    = getValues(  );
    jlistModel   = new DefaultListModel(  );
    jlist        = new JList( jlistModel );
    this.param   = param;

    if( oldVector != null ) {
      for( int i = 0; i < oldVector.size(  ); i++ ) {
        jlistModel.addElement( oldVector.elementAt( i ) );
      }
    }

    add( jlist, BorderLayout.CENTER );

    JPanel jp = new JPanel( new GridLayout( 7, 1 ) );

    Up       = new JButton( UP_LABEL );
    Down     = new JButton( DOWN_LABEL );
    Delete   = new JButton( DELETE_LABEL );
    Add      = new JButton( ADD_LABEL );
    Change   = new JButton( CHANGE_LABEL );
    Done     = new JButton( DONE_LABEL );
    Show     = new JButton( SHOW_LABEL );
    jp.add( Up );
    jp.add( Down );
    jp.add( Show );
    jp.add( Add );
    jp.add( Delete );
    jp.add( Change );
    jp.add( Done );
    add( jp, BorderLayout.EAST );

    Up.addActionListener( this );
    Down.addActionListener( this );
    Show.addActionListener( this );
    Add.addActionListener( this );
    Delete.addActionListener( this );
    Change.addActionListener( this );
    Done.addActionListener( this );

    JPanel dataPanel = new JPanel( new BorderLayout(  ) );

    param.init(  );

    //use the inner parameter's entrywidget for entering values
    dataPanel.add( param.getEntryWidget(  ), BorderLayout.CENTER );

    //add a key listener to the parameter
    param.getEntryWidget(  )
         .addKeyListener( this );

    this.add( dataPanel, BorderLayout.NORTH );

    //just changed the value, so invalidate the parameter.
    invalidate(  );

    //if we happen to have a VectorPG as an element in our ArrayJPanel, we will
    //need to add the SetValueActionListener to it
    if( param instanceof VectorPG ) {
      ( ( VectorPG )param ).addPropertyChangeListener( this );
    }

    pcs = new PropertyChangeSupport( this );
  }

  //~ Methods ******************************************************************

  /**
   * Sets the value of the GUI elements.
   *
   * @param newVal The new value to set the GUI elements to.
   */
  public void setValue( Object newVal ) {
    if( jlistModel != null ) {
      jlistModel.clear(  );

      if( ( newVal != null ) && newVal instanceof Vector ) {
        for( int i = 0; i < ( ( Vector )newVal ).size(  ); i++ ) {
          jlistModel.addElement( ( ( Vector )newVal ).elementAt( i ) );
        }
      }
    }

    position = -1;
  }

  /**
   * Accessor method to get the values in the GUI.
   *
   * @return A Vector of String representations of the GUI elements.
   */
  public Vector getValues(  ) {
    if( jlist == null ) {
      return new Vector(  );
    }

    ListModel lmodel = jlist.getModel(  );
    Vector V         = new Vector( lmodel.getSize(  ) );

    for( int i = 0; i < lmodel.getSize(  ); i++ ) {
      V.addElement( lmodel.getElementAt( i ) );
    }

    return V;
  }

  // ActionListener requirements.
  public void actionPerformed( ActionEvent evt ) {
    JButton actionButton = ( JButton )( evt.getSource(  ) );

    if( actionButton == Up ) {
      move( -1 );
    } else if( actionButton == Down ) {
      move( +1 );
    } else if( actionButton == Add ) {
      updateData(  );
    } else if( actionButton == Change ) {
      int pos = jlist.getSelectedIndex(  );

      if( ( pos >= 0 ) && ( pos < jlistModel.getSize(  ) ) ) {
        jlistModel.setElementAt( param.getValue(  ), pos );
      }
    } else if( actionButton == Delete ) {
      int j = jlist.getSelectedIndex(  );

      position = -1;

      if( j < 0 ) {
        //this should throw an exception at some point
        return;
      }

      //found an element, so delete it
      jlistModel.removeElementAt( j );

      if( j < jlistModel.getSize(  ) ) {
        jlist.setSelectedIndex( j );
      }
    } else if( actionButton == Show ) {
      int index = jlist.getSelectedIndex(  );

      if( index < 0 ) {
        return;
      }

      this.setInnerParameterValue( index );

      //display the parameter
      new ParameterViewer( param ).showParameterViewer(  );
    } else if( actionButton == Done ) {
      Vector newVector = getValues(  );

      //let any property listeners know that the values have changed
      pcs.firePropertyChange( VectorPG.DATA_CHANGED, oldVector, newVector );
      oldVector = newVector;
    }
  }

  // PropertyChanger requirements.
  public void addPropertyChangeListener( PropertyChangeListener listener ) {
    pcs.addPropertyChangeListener( listener );
  }

  /**
   * Adds a PropertyChangeListener
   *
   * @param property The name of the property to listen for.
   * @param listener The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener( 
    String property, PropertyChangeListener listener ) {
    pcs.addPropertyChangeListener( property, listener );
  }

  // KeyListener requirements.

  /**
   * We are interested in listening for the Enter key here.
   */
  public void keyPressed( KeyEvent evt ) {
    if( evt.getKeyCode(  ) == KeyEvent.VK_ENTER ) {
      updateData(  );
    }
  }

  /**
   * Needed for implementation of KeyListener, but unnecessary here.
   */
  public void keyReleased( KeyEvent evt ) {}

  /**
   * Needed for implementation of KeyListener, but unnecessary here.
   */
  public void keyTyped( KeyEvent evt ) {}

  /**
   * Overridden to set the focus to the entrywidget when this JPanel is shown.
   *
   * @param g Graphics object that is drawn on.
   */
  public void paint( Graphics g ) {
    JComponent wijit = param.getEntryWidget(  );

    wijit.requestFocus(  );

    if( wijit instanceof JTextComponent ) {
      ( ( JTextComponent )wijit ).selectAll(  );
    }

    super.paint( g );
  }

  /**
   * Needed for PropertyChangeListener implementation.
   *
   * @param evt The PropertyChangeEvent to listen for.
   */
  public void propertyChange( PropertyChangeEvent evt ) {
    actionPerformed( null );
  }

  /**
   * Removes a PropertyChangeListener
   *
   * @param listener The PropertyChangeListener to remove.
   */
  public void removePropertyChangeListener( PropertyChangeListener listener ) {
    pcs.removePropertyChangeListener( listener );
  }

  /**
   * Sets the value of the parameter to the value at the position given in the
   * list.
   *
   * @param pos The index of the position where the new value is at.
   */
  private void setInnerParameterValue( int pos ) {
    position = pos;

    if( ( pos >= 0 ) && ( pos < jlistModel.getSize(  ) ) ) {
      param.setValue( jlistModel.elementAt( pos ) );
    }

    if( param instanceof VectorPG ) {
      ( ( VectorPG )param ).actionPerformed( 
        new ActionEvent( this, ActionEvent.ACTION_PERFORMED, "NEW" ) );
    }
  }

  /**
   * Utility method to navigate through the GUI display.
   *
   * @param i The direction and magnitude to move.
   */
  private void move( int i ) {
    int j = jlist.getSelectedIndex(  );

    if( j <= 0 ) {
      if( i == -1 ) {
        return;
      }
    }

    if( j < 0 ) {
      return;
    }

    if( j >= jlist.getModel(  )
                    .getSize(  ) ) {
      return;
    }

    if( i > 0 ) {
      if( j == ( jlist.getModel(  )
                        .getSize(  ) - 1 ) ) {
        return;
      }
    }

    Object V = jlistModel.elementAt( j );

    jlistModel.removeElementAt( j );
    jlistModel.insertElementAt( V, j + i );
    jlist.setSelectedIndex( j + i );
  }

  /**
   * Attempts to make entering data as easy as possible for the user by working
   * with the entry widget to re-highlight text, etc. as well as actually
   * entering data into the ParameterGUI.
   */
  private void updateData(  ) {
    JComponent wijit = param.getEntryWidget(  );

    //get the value from the data entry panel and add it
    jlistModel.addElement( param.getValue(  ) );

    if( wijit instanceof JTextComponent ) {
      //re-highlight the text
      ( ( JTextComponent )wijit ).selectAll(  );
    }
  }
}
