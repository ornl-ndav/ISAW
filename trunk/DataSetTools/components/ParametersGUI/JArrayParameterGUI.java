/*
 * File:  JArrayParameterGUI.java
 *
 * Copyright (C) 2001, Ruth Mikkelson
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.6  2004/01/22 01:41:27  bouzekc
 * Removed unused variables and unused imports.
 *
 * Revision 1.5  2002/11/27 23:12:34  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/03/08 16:20:38  pfpeterson
 * Added method to disable the GUIs. This is to help out wizards.
 *
 */
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.io.*;
import Command.*;
import java.util.*;

public class JArrayParameterGUI extends    JParameterGUI 
                                implements Serializable
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


   public void setEnabled(boolean en){
       this.arrayv.setEnabled(en);
   }

   public Parameter getParameter()
   {
      String s = arrayv.getText();
      Vector V =StringtoArray(s);
      parameter.setValue(V);
      return parameter;
   }

/* ----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

   private String ArraytoString(Vector V)
   {
      execOneLine execLine = new execOneLine();
      String res = execLine.Vect_to_String(V);
      return res;
    }

   private Vector StringtoArray( String S)
   {
     execOneLine execLine = new execOneLine();
     execLine.execute(S, 0 , S.length());

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
