/*
 * File:  IParameterGUI.java
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
 *  Revision 1.10  2004/05/11 18:23:50  bouzekc
 *  Added/updated javadocs and reformatted for consistency.
 *
 *  Revision 1.9  2004/03/12 21:13:49  bouzekc
 *  Added clear() method.
 *
 *  Revision 1.8  2004/03/12 19:54:38  bouzekc
 *  Code reformat.
 *
 *  Revision 1.7  2003/09/09 22:59:07  bouzekc
 *  Added a method to self-validate the IParameterGUIs.
 *
 *  Revision 1.6  2003/08/22 20:12:05  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.5  2003/08/15 23:15:32  bouzekc
 *  Removed init() from interface.
 *
 *  Revision 1.4  2003/04/14 21:26:47  pfpeterson
 *  Moved valid state from IParameter.
 *
 *  Revision 1.3  2002/11/27 23:22:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/05/31 19:43:47  pfpeterson
 *  Added empty init() requirement to implementing classes.
 *
 *  Revision 1.1  2002/05/28 22:24:13  pfpeterson
 *  added to cvs
 *
 *
 */
package DataSetTools.parameter;

/**
 * This is an interface to be implemented by all parameters that can produce a
 * GUI. Since it extends IParameter the methods required by it are also
 * required here.
 */
public interface IParameterGUI extends IParameter {
  //~ Methods ******************************************************************

  /**
   * Specify if the valid checkbox will be drawn.
   *
   * @param draw True if the checkbox should be drawn.
   */
  void setDrawValid( boolean draw );

  /**
   * Determine if the 'valid' checkbox will be drawn.
   *
   * @return True if the checkbox will be drawn.
   */
  boolean getDrawValid(  );

  /**
   * Set the enabled state of the EntryWidget.
   *
   * @param enable Whether to enable this IParameterGUI.
   */
  void setEnabled( boolean enable );

  /**
   * Determine if the EntryWidget is enabled.
   *
   * @return true if the EntryWidget is disable.
   */
  boolean getEnabled(  );

  /**
   * @return The EntryWidget for this IParameterGUI.
   */
  DataSetTools.components.ParametersGUI.EntryWidget getEntryWidget(  );

  /**
   * @return The GUI JPanel for this IParameterGUI.
   */
  javax.swing.JPanel getGUIPanel(  );

  /**
   * @return The JLabel for this IParameterGUI.
   */
  javax.swing.JLabel getLabel(  );

  /**
   * Set the valid state of the parameter.
   *
   * @param valid True if this IParameterGUI should be considered valid.
   */
  void setValid( boolean valid );

  /**
   * @return whether or not the parameter is valid.
   */
  boolean getValid(  );

  /**
   * Used to clear out the IParameterGUI, cleaning up data structures and
   * releasing Objects along the way.
   */
  void clear(  );

  /**
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The new values to use.
   */
  void initGUI( java.util.Vector init_values );

  /**
   * Used to let a parameter validate itself.  This is helpful for any instance
   * where the "valid" checkbox is drawn, as it removes the need to
   * individually validate ParameterGUIs.
   */
  void validateSelf(  );
}
