package DataSetTools.parameter;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

public class StringArrayPG extends VectorPG
  {

   public StringArrayPG( String Prompt, Object value)
      { super( new StringPG("Enter String",""),"Enter String List");
        setValue( value);
       }

      public static void main( String args[] )
      {
         JFrame jf = new JFrame("Test");
         jf.getContentPane().setLayout( new GridLayout( 1,2));
         StringArrayPG IaPg = new StringArrayPG( "Enter String list", null);
         IaPg.init();
         jf.getContentPane().add(IaPg.getGUIPanel());
         JButton  jb = new JButton("Result");
         jf.getContentPane().add(jb);
         jb.addActionListener( new MyActionList( IaPg));
         jf.setSize( 500,100);
         jf.invalidate();
         jf.show();




      }      

static class MyActionList implements ActionListener
  {
   StringArrayPG  vpf;
   public MyActionList( StringArrayPG vpg)
     {

       vpf = vpg;
     }

    public void actionPerformed( ActionEvent evt )
      { 
        (new JOptionPane()).showMessageDialog(null,"Result="+
       (new NexIO.NxNodeUtils()).Showw(vpf.getValue()));

      }

   



   }
  }
