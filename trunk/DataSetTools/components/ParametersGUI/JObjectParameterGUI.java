/*
 * File:  JObjectParameterGUI.java
 *
 * Copyright (C) 2000, Ruth Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2001/06/26 18:37:35  dennis
 * Added Copyright and GPL license.
 * Removed un-needed imports and improved
 * code format.
 *
 */

package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.operator.*;
import java.awt.*;
import javax.swing.border.*;
import java.io.*;
import java.awt.event.*;
import Command.*;


public class JObjectParameterGUI extends    JParameterGUI 
                                 implements Serializable, 
                                            ItemListener
{   
    public static final String ER__NumberFormat_Error = "Number Format Error";
    private JPanel segment;
    private JTextField intText;
    private JComboBox  DataType;
    private JCheckBox  Array;
    public String serror = "";


    public JObjectParameterGUI(Parameter parameter)
    { 
       super(parameter);

       serror = "";
       String value = null; //((Integer)parameter.getValue()).toString();

       intText = new JTextField();
       intText.setText("");

       segment = new JPanel();
       segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5)); 

       JPanel P = new JPanel();
       P.setLayout( new GridLayout( 3 , 1 ));
       P.add(new JLabel(parameter.getName(),javax.swing.SwingConstants.CENTER));
       P.add(new JLabel("Select DataType", javax.swing.SwingConstants.CENTER));
       P.add( new JLabel("Form Array ", javax.swing.SwingConstants.CENTER ) );
       segment.add( P );

       P = new JPanel();
       P.setLayout(new GridLayout( 3,1));
       P.add(intText);

       DataType= new JComboBox();
       DataType.addItem("Integer");
       DataType.addItem("Float");
       DataType.addItem("String");

       DataType.setEditable(false);
       P.add(DataType);

       Array= new JCheckBox("Array" , false);
       P.add(Array);
       segment.add( P );
       segment.setBorder( new LineBorder( Color.black) ); 
       DataType.addItemListener( this);
    }
    

    public JPanel getGUISegment()

    {
        return segment;
    }


    public Parameter getParameter()
    {   
        int i;
        String s = intText.getText();
        int dt = DataType.getSelectedIndex();

        if( dt < 0 )
            return parameter;

        Object U[] =Array.getSelectedObjects();

        boolean Ar ;

        if( U== null)
          Ar = false;

        else 
          Ar = true;

        try{
          if( Ar)
           {
              int c = 0;

             for( i = intText.getText().indexOf(","); 
                (i >= 0) && (i < intText.getText().length()); )
             {
               c++;
               i =intText.getText().indexOf(",", i + 1);
             }

             Object X[];

             if( dt == 0)
               X = new Integer[c+1];

             else if( dt == 1)
               X = new Float[c+1];

             else if( dt == 2)
               X = new String[c+1];

             else
               X = null;

             int st = 0;
             String S = intText.getText() ;
             int en;

             for( i = 0; i < c+1; i++)
             { 
               if( i < c)
                 en = S.indexOf("," , st );
               else 
                 en = S.length(); 

               if( dt == 0)
                 X[i] = new Integer( S.substring(st, en));
               else if( dt == 1)
                 X[i] = new Float( S.substring(st, en));
               else if( dt == 2)
                 X[i] = new String( S.substring(st, en));

               st = en + 1;
             } 

             parameter.setValue( X);        
             return parameter;
           }

         if(dt == 0)
            parameter.setValue( new Integer( intText.getText()));

         else if( dt == 1)
           parameter.setValue( new Float( intText.getText()));

         else if( dt == 2)
           parameter.setValue( intText.getText());
      }

       catch( NumberFormatException z)
       { 
         parameter.setValue( null );
         serror = "Number Format Error";
       }

        return parameter;
    }

  public void itemStateChanged(ItemEvent e)
  { if( DataType.getSelectedIndex() == 2)
      Array.setEnabled( false);
    else
      Array.setEnabled( true);
   }

/*   public static void main( String args[])
    { 
     JFrame F; JObjectParameterGUI JO;

     F = new JFrame(" JObjectParameterGUI");
     F.setSize( 400, 200 );
     JO = new JObjectParameterGUI( new Parameter("Joe", null));
     F.getContentPane().add(JO.getGUISegment());
     F.show();
     F.validate();
     char c='g';

     while( c >= ' ' )
     {
     try
     {
         for( c=0; c< ' ';       c = (char) System.in.read()){}
     }

     catch(java.io.IOException s){}
     Parameter P= JO.getParameter();
     System.out.print(P.getName()+",");

     if(P.getValue() == null )
        System.out.println( "null");
     else
       System.out.println(P.getValue());
     }
    }
*/

}
