/*

 * @(#)JArrayParameterGUI.java    2-7-2001 R. Mikkelson

 *

 * 
 * 

 */

 

package DataSetTools.components.ParametersGUI;

import javax.swing.*;


import DataSetTools.operator.*;
import java.awt.*;
import java.util.zip.*;
import java.io.Serializable;
import Command.*;
import java.util.*;
/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *

 * @version 1.0  
 */


public class JArrayParameterGUI extends JParameterGUI implements Serializable

{
    private JPanel segment;
    private JTextField arrayv;
    
    public JArrayParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       
       Vector value = (Vector)(parameter.getValue());
       JLabel label= new JLabel(parameter.getName());
       
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(label);
       arrayv= new JTextField(25);
       String S = ArraytoString(value);
       arrayv.setText(S);
       segment.add(arrayv);
     
    }
    
    
    
    public JPanel getGUISegment()

    {
        return segment;
        
    }


    public Parameter getParameter()
    {
        String s = arrayv.getText();
        Vector V =StringtoArray(s);
        parameter.setValue(V);
        return parameter;
    }
    private String ArraytoString(Vector V)
    {execOneLine execLine = new execOneLine();
     String res = execLine.Vect_to_String(V);
     return res;
    }

   private Vector StringtoArray( String S)
     {execOneLine execLine = new execOneLine();
      int r=execLine.execute(S, 0 , S.length());
      if( execLine.getErrorCharPos()>=0)
          return new Vector();
       Object O = execLine.getResult();
       if( O==null)
          return new Vector();
       if( !(O instanceof Vector))
          return new Vector();
       return (Vector) O;
      
      }
}
