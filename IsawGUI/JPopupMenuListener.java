/*
 * $Id$
 *
 * inplementation of the ActionListener interface to be used
 * for listening to OptionMenu.build()'s menu 
 * 
 * $Log$
 * Revision 1.1  2001/06/25 22:12:35  neffk
 * handles item events for the OperatorMenu.build(...)'s menu
 *
 */
 
package IsawGUI;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.components.ParametersGUI.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import javax.swing.text.*; 
import Command.*;


public class JPopupMenuListener 
  implements ActionListener, Serializable
{
  private DataSet[] ds;

  /**
   * default constructor
   */ 
  public JPopupMenuListener( DataSet[] ds_ )
  { 
    ds = ds_;
  }
 
  /**
   * traps appropriate events.  since this particular ActionListener
   * is used in conjunction w/ OperationsMenu, most events invoke 
   * DataSet operators.
   */
  public void actionPerformed( ActionEvent e ) 
  {
    System.out.println(  "popup listener> " + e.getActionCommand()  );
  }

}


