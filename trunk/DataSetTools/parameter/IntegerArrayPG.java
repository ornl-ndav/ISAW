package DataSetTools.parameter;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

public class IntegerArrayPG extends VectorPG
  {

   public IntegerArrayPG( String Prompt, Object value)
      { super( new IntegerPG("Enter Integer",0),"Enter Integer List");
        setValue( value);
       }

      public static void main( String args[] )
      {
         JFrame jf = new JFrame("Test");
         jf.getContentPane().setLayout( new GridLayout( 1,2));
         IntegerArrayPG IaPg = new IntegerArrayPG( "Enter Int list", null);
         IaPg.init();
         jf.getContentPane().add(IaPg.getGUIPanel());
         JButton  jb = new JButton("Result");
         jf.getContentPane().add(jb);
         jb.addActionListener( new MyActionList( IaPg));
         jf.setSize( 500,100);
         jf.invalidate();
         jf.show();




      }      

  }
class MyActionList implements ActionListener
  {
   IntegerArrayPG  vpf;
   public MyActionList( IntegerArrayPG vpg)
     {

       vpf = vpg;
     }

    public void actionPerformed( ActionEvent evt )
      { 
        (new JOptionPane()).showMessageDialog(null,"Result="+
       (new NexIO.NxNodeUtils()).Showw(vpf.getValue()));

      }

   



   }
