/*

 * @(#)JObjectParameterGUI.java     1.0  99/09/02  Alok Chatterjee

 *

 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI

 * 

 */

 

package DataSetTools.components.ParametersGUI;



import javax.swing.*;

//import javax.swing.*;

//import DataSetTools.*;

import DataSetTools.operator.*;

import java.awt.*;

import javax.swing.border.*;

import java.io.Serializable;

import java.awt.event.*;

import Command.*;

/**

 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 

 * ChopTools and graph packages.

 *

 * @version 1.0  

 */

 

public class JObjectParameterGUI extends JParameterGUI implements Serializable,

                                                                  ItemListener

{   public static final String ER__NumberFormat_Error = "Number Format Error";

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

       P.add(new JLabel(parameter.getName(), javax.swing.SwingConstants.CENTER));

       P.add(new JLabel( "Select DataType" , javax.swing.SwingConstants.CENTER ) );

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

       // P.setBorder( new LineBorder( Color.black) ); 

       segment.add( P );

       segment.setBorder( new LineBorder( Color.black) ); 

       DataType.addItemListener( this);

   

    }

    

    

    

    public JPanel getGUISegment()

    {

        return segment;

        

    }





    public Parameter getParameter()

    {   int i;

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

           { int c = 0;

             for( i = intText.getText().indexOf(","); 

                        (i >= 0) && (i < intText.getText().length()); )

               {c++;

                i =intText.getText().indexOf(",", i + 1);

              }

             Object X[];

             if( dt == 0)

               { X = new Integer[c+1];

               }

             else if( dt == 1)

               { X = new Float[c+1];

               }         

             else if( dt == 2)

               { X = new String[c+1];

               }

             else

               X = null;

             int st = 0;

             String S = intText.getText() ;

             int en;

             for( i = 0; i < c+1; i++)

               { if( i < c)

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

        { parameter.setValue( null );

          serror = "Number Format Error";

       }

      

       

       // parameter.setValue(value);

        return parameter;

    }

 public void itemStateChanged(ItemEvent e)

   { if( DataType.getSelectedIndex() == 2)

         Array.setEnabled( false);

     else

         Array.setEnabled( true);

   }

/*   public static void main( String args[])

    { JFrame F; JObjectParameterGUI JO;



      F = new JFrame(" JObjectParameterGUI");

     F.setSize( 400, 200 );

     JO = new JObjectParameterGUI( new Parameter("Joe", null));

     F.getContentPane().add(JO.getGUISegment());

     F.show();

     F.validate();

     char c='g';

     while( c >= ' ' )

     {try{

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















