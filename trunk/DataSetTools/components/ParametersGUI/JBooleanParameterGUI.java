/*

 * @(#)JBooleanParameterGUI.java     1.0  99/09/02  Alok Chatterjee

 *

 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI

 * 

 */

 

package DataSetTools.components.ParametersGUI;



import javax.swing.*;

//import javax.swing.*;

import DataSetTools.*;

import DataSetTools.operator.*;

import java.awt.*;

import java.util.zip.*;

import java.io.Serializable;



/**

 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 

 * ChopTools and graph packages.

 *

 * @version 1.0  

 */



public class JBooleanParameterGUI extends JParameterGUI implements Serializable

{

    private JPanel segment;

    private JCheckBox jcb;

    

    public JBooleanParameterGUI(Parameter parameter)

    { 

       super(parameter);

       

       

       boolean value = ((Boolean)parameter.getValue()).booleanValue();

       

       

       segment = new JPanel();

       segment.setLayout(new GridLayout(1,2));

       segment.add(new JLabel(""));

       jcb = new JCheckBox(parameter.getName(),value);

       segment.add(jcb);

       

       

    

   

    }

    

    

    

    public JPanel getGUISegment()

    {

        return segment;

        

    }





    public Parameter getParameter()

    {

        boolean val = jcb.isSelected();

        Boolean value = new Boolean(val);

        parameter.setValue(value);

        return parameter;

    }



}

