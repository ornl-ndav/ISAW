
package DataSetTools.parameter;

import DataSetTools.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import DataSetTools.util.PGActionListener;

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
         jb.addActionListener( new PGActionListener( IaPg));
         jf.setSize( 500,100);
         jf.invalidate();
         jf.show();




      }      
public Object clone()
  {
    LoadFilePG faap = new LoadFilePG( getName(), getValue());
    return (Object)faap;

  }       
  }
