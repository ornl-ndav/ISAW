/*
 * File: IViewControl.java
 *
 * Copyright (C) 2003, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2004/01/05 18:14:06  millermi
 *  - Replaced show()/setVisible(true) with WindowShower.
 *  - Removed excess imports.
 *
 *  Revision 1.3  2003/06/13 19:44:05  serumb
 *  Added messages for the LabelCombobox and ButtonControl classes.
 *
 *  Revision 1.2  2003/05/24 17:34:39  dennis
 *  Changed action event string for ControlSlider and added the
 *  action event string for ControlCheckbox. (Mike Miller)
 *
 *  Revision 1.1  2003/05/20 19:44:46  dennis
 *  Initial version of standardized controls for viewers. (Mike Miller)
 *
 *
 */
 
 package DataSetTools.components.View.ViewControls;

 import java.awt.event.ActionListener;
 
/**
 * Any class that implements this interface will be used to adjust
 * settings on the IViewComponent.
 */
public interface IViewControl
{
 /*
  * These variables are messaging strings for use by action listeners.
  */
  // Used by ControlSlider.java
  public static final String SLIDER_CHANGED  = "SLIDER_CHANGED";
  // Used by ControlCheckBox.java
  public static final String CHECKBOX_CHANGED  = "CHECKBOX_CHANGED";
  // Used by LabelCombobox.java
  public static final String COMBOBOX_CHANGED  = "COMBOBOX_CHANGED";
  // Used by ButtonControl.java
  public static final String BUTTON_PRESSED  = "BUTTON_PRESSED";
  
 /**
  * Add a listener to this view control. A listener will be notified
  * when this control is modified.
  */
  public void addActionListener( ActionListener act_listener );
  
 /**
  * Remove a specified listener from this view control.
  */ 
  public void removeActionListener( ActionListener act_listener );
 
 /**
  * Remove all listeners from this view control.
  */ 
  public void removeAllActionListeners();
 
 /**
  * Get title of the view control.
  */ 
  public String getTitle();
  
 /**
  * Set title of the view control.
  */ 
  public void setTitle(String title);  
}
