/*
 * File: JParameterGUI.java
 *
 * Copyright (C) 1999, Alok Chatterjee, Dennis Mikkelson
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
 * Contact : Alok Chatterjee achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
 *           USA
 *
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2001/06/26 18:37:37  dennis
 *  Added Copyright and GPL license.
 *  Removed un-needed imports and improved
 *  code format.
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.operator.*;

/**
 *  This is the abstract base class for GUI components that allow
 *  the user to input different types of parameters for operators.
 *  Derived classes will prompt for particular types of parameters,
 *  such as integer, boolean, float, DataSet, etc.  These individual
 *  components should be placed in a dialog box via a
 *  JParametersDialog object.  
 */
abstract public class JParameterGUI
{
   Parameter parameter;
   public JParameterGUI(Parameter parameter)
   { 
      this.parameter = parameter;
   }
    
   public Parameter getParameter()
   {
       return parameter;
   }
    
   abstract public JPanel getGUISegment();
}
