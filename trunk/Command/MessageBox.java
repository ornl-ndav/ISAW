import QuickBoxes;
import javax.swing.*;
import java.lang.*;

public class MessageBox 
{ JPanel JP = new JPanel();

  public MessageBox( String Text)
    {JP.add( new JLabel( Text));
     JP.setSize( Text.length()*16 + 15 , 32);
     QuickBoxes QB = new QuickBoxes(JP, QuickBoxes.B_OK );
     int i = QB.showDialog();


    }



public static void main( String args[])
  {MessageBox B = new MessageBox( " HI THere");

  }




}
