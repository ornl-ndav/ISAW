package DataSetTools.parameter;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

public class LoadFileArrayPG extends VectorPG
  {

   public LoadFileArrayPG( String Prompt, Object value)
      { super( new LoadFilePG("Enter LoadFile",null),"Select LoadFile List");
        setValue( value);
       }

    public static void main( String args[] )
      {
         JFrame jf = new JFrame("Test");
         jf.getContentPane().setLayout( new GridLayout( 1,2));
         LoadFileArrayPG IaPg = new LoadFileArrayPG( "Enter File list", null);
         IaPg.init();
         jf.getContentPane().add(IaPg.getGUIPanel());
         JButton  jb = new JButton("Result");
         jf.getContentPane().add(jb);
         jb.addActionListener( new MyActionList( IaPg));
         jf.setSize( 500,100);
         jf.invalidate();
         jf.show();




      }      
public Object clone()
  {
    LoadFilePG faap = new LoadFilePG( getName(), getValue());
    return (Object)faap;

  }       

static class MyActionList implements ActionListener
  {
   LoadFileArrayPG  vpf;
   public MyActionList( LoadFileArrayPG vpg)
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
