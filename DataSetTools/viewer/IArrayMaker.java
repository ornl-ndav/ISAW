
/*
 * File: IArrayMaker.java
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
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/10/27 15:05:36  rmikk
 * Initial Checkin
 *
 */


package DataSetTools.viewer;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.components.View.Menu.*;
import DataSetTools.components.View.*;


/**
*    This interface contains methods and fields necessary for Generators.
*    DataSetViewerMaker to use a VirtualArray with an arbitrary IViewComponent.
*    If the Data of the IVirtualArray and IViewComponent are incompatible
*    exceptions are thrown.
*
*    This interface only needs information for showing the DataSetXConversionsTable and
*    to communicate between the IVirtualArray and IVirtualComponent
*/
public interface IArrayMaker{

    /**
   *    This is the action command from an action event that indicates that
   *    the data has changed.
   */
  public static final String DATA_CHANGED = "DATA_CHANGED";

//----------- Constructor Methods -----------------
  /**
   * Return controls needed by the component.
   */ 
   public JComponent[] getSharedControls();

  /**
   * To be continued...
   */   
   public JComponent[] getPrivateControls();

  /**
   * Return view menu items needed by the component.
   */   
   public ViewMenuItem[] getSharedMenuItems( );
   
  /**
   * To be continued...
   */
   public ViewMenuItem[] getPrivateMenuItems( );
  
   public String[] getSharedMenuItemPath( );
   public String[] getPrivateMenuItemPath( );
  /**
  *    Adds an ActionListener to this VirtualArray. See above for
  *    action events that will be sent to the listeners
  */
  public void addActionListener( ActionListener listener);

  /**
   * Remove a specified listener from this view component.
   */ 
   public void removeActionListener( ActionListener act_listener );
  
  /**
   * Remove all listeners from this view component.
   */ 
   public void removeAllActionListeners();

  /**
  *    Invoked whenever there is an action event on and instance of
  *    a class which is being listened for.  Also, anyone can invoke the
  *    method.  See above the action commands that must be supported
  */
   public void actionPerformed( ActionEvent evt);

  public IVirtualArray getArray();
}
