/*
 * File: JParameterGUI.java
 *
 * Copyright (C) 1999-2002, Alok Chatterjee, Dennis Mikkelson
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
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
 *  Revision 1.9  2004/03/15 03:27:21  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.8  2003/05/28 20:53:39  pfpeterson
 *  Changed System.getProperty to SharedData.getProperty
 *
 *  Revision 1.7  2003/02/25 19:46:32  pfpeterson
 *  Changed reference to deprecated fixSeparator to new method.
 *
 *  Revision 1.6  2002/11/27 23:12:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/03/08 16:20:48  pfpeterson
 *  Added method to disable the GUIs. This is to help out wizards.
 *
 *  Revision 1.4  2002/02/27 16:15:05  dennis
 *  Added the factory method: getInstance() to return an instance of an
 *  appropriate subclass of a JParameterGUI based on the data type of the
 *  parameter value.
 *  Added java doc comments.
 *
 */
 
package DataSetTools.components.ParametersGUI;

import gov.anl.ipns.Util.SpecialStrings.*;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 *  This is the abstract base class for GUI components that allow
 *  the user to input different types of parameters for operators.
 *  Derived classes will prompt for particular types of parameters,
 *  such as integer, boolean, float, DataSet, etc.  These individual
 *  components should be placed in a dialog box as is done in 
 *  JParametersDialog.  
 */
abstract public class JParameterGUI
{
   protected Parameter parameter;

   /** 
    *  Constructor for a generic JParameterGUI.  Since this is an abstract
    *  class, this just provides the service of recording the current parameter
    *  for the constructors of concrete derived classes.
    *
    *  @param  parameter  the Parameter object for this GUI component
    */
   public JParameterGUI(Parameter parameter)
   { 
     this.parameter = parameter;
   }
    

   /**
    *  Get the Parameter object for this JParameterGUI.  
    *
    *  @return the Parameter object for this JParameterGUI.  The user may 
    *          have altered the value of the Parameter.
    */
   public Parameter getParameter()
   {
     return parameter;
   }
    

   /**
    *  Get the GUI segement for the current JParameterGUI object.  
    *
    *  @return A JPanel containing the GUI component for the current 
    *          JParameterGUI object.  The exact type of component returned
    *          is determined by the concrete derived class.
    */
   abstract public JPanel getGUISegment();

    /**
     * Enable the Parameter GUI.
     */
    abstract public void setEnabled(boolean en);


   /**
    *  Create an appropriate instance of a JParameterGUI object based on 
    *  the type of the Parameter.
    *
    *  @param  param  The parameter for which a JParameterGUI is to be
    *                 constructed.
    *  @return An instance of the correct sub class of JParameterGUI for the
    *          specified parameter.
    */
   public static JParameterGUI getInstance( Parameter param )
   {
     JParameterGUI paramGUI = null;

     if (param.getValue() instanceof Float)
       paramGUI = new JFloatParameterGUI(param);

     else if(param.getValue() instanceof Integer)
       paramGUI = new JIntegerParameterGUI(param);

     else if(param.getValue() instanceof Boolean)
       paramGUI = new JBooleanParameterGUI(param);

     else if(param.getValue() instanceof String)
       paramGUI = new JStringParameterGUI(param);

     else if(param.getValue() instanceof IntListString)
       paramGUI = new JStringParameterGUI(param);
 
     else if(param.getValue() instanceof Vector)
       paramGUI = new JArrayParameterGUI(param);

     else if( param.getValue() instanceof DataDirectoryString )
     {
       String DirPath = SharedData.getProperty("Data_Directory");
       if( DirPath != null )
         DirPath = gov.anl.ipns.Util.Sys.StringUtil.setFileSeparator(DirPath+"\\");
       else
         DirPath = "";

       param.setValue( new DataDirectoryString(DirPath) );
       paramGUI = new JOneFileChooserParameterGUI( param ) ;
     }

     else if (param.getValue() instanceof IStringList )
       paramGUI = new JIStringListParameterGUI( 
                                  param, (IStringList)(param.getValue()));

     else if( param.getValue() instanceof InstrumentNameString)
     {
       String XX = SharedData.getProperty("DefaultInstrument");

       if ( XX == null )
         XX = "";

       param.setValue(new InstrumentNameString( XX ));
       paramGUI= new JStringParameterGUI( param);
     }

     else
     {  
       Frame frame = new Frame();
       JOptionPane.showMessageDialog(
                    frame, 
                   "Un-handled parameter type in JParameterGUI.createInstance ",
                   "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
     }
     return paramGUI;
   }
}
