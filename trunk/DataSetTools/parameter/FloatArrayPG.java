package DataSetTools.parameter;

import DataSetTools.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import DataSetTools.util.PGActionListener;

public class FloatArrayPG extends VectorPG{

  public FloatArrayPG( String Prompt, Object value){ 
    super( new FloatPG("Enter Float",0.0f),"Enter Float List");
    setValue( value);
  }

  public static void main( String args[] ){
    JFrame jf = new JFrame("Test");
    jf.getContentPane().setLayout( new GridLayout( 1,2));
    FloatArrayPG IaPg = new FloatArrayPG( "Enter Int list", null);
    IaPg.init();
    jf.getContentPane().add(IaPg.getGUIPanel());
    JButton  jb = new JButton("Result");
    jf.getContentPane().add(jb);
    jb.addActionListener( new PGActionListener( IaPg));
    jf.setSize( 500,100);
    jf.invalidate();
    jf.show();
  }    
  public Object clone(){
    FloatArrayPG faap = new FloatArrayPG( getName(), getValue());
    return (Object)faap;
  }       
}
