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
 * This is an interface to be implemented by all parameters that can
 * produce a GUI. Since it extends IParameter the methods required by
 * it are also required here.
 */
public interface IParameterGUI extends IParameter{
    /**
     * Allows for initialization of the GUI after instantiation.
     */
    void                   init(java.util.Vector init_values);

    /**
     * Convenince method for initialization of the GUI after
     * instantiation with null vector.
     */
    void                   init();

    /**
     * Method for producing an alternative layout of the GUI.
     */
    javax.swing.JLabel     getLabel();
    /**
     * Method for producing an alternative layout of the GUI.
     */
    javax.swing.JComponent getEntryWidget();
    /**
     * Method for obtaining the default layout of the GUI.
     */
    javax.swing.JPanel     getGUIPanel();
    /**
     * Determine if the entry widget is enabled.
     */
    boolean                getEnabled();
    /**
     * Set the enabled state of the EntryWidget. This produces a more
     * pleasant effect that the default setEnabled of the widget.
     */
    void                   setEnabled(boolean enable);
    /**
     * Determine if the 'valid' checkbox will be drawn.
     */
    boolean                getDrawValid();
    /**
     * Specify if the valid checkbox will be drawn.
     */
    void                   setDrawValid(boolean draw);
}
