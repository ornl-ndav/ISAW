
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

class QuickBoxes 
  { public static int  B_OK = 8;   
    public static int B_Yes    = 4;
    public static int B_No     = 2;
    public static int B_Cancel = 1;
    private JPanel Jp = null;
    private int buttons = 0;
    private JDialog Dialog = null;
  
    private int Result=0;
    public QuickBoxes( JPanel JP , int Buttons)
      { Jp = JP;
        buttons = Buttons;
      
        Dimension size = JP.getSize();
       
        
        Dialog = new JDialog(new JFrame() , true);
        
        Dialog.getContentPane().setLayout( new BorderLayout());
        JPanel Butts= new JPanel();
        int count = 0;
        for ( int i = 8; i > .5; i = i/2)
          {if( Buttons >= i )
              { String S = "OK";
                if( i == 4) S = "Yes";
                else if( i ==2) S = "No";
                else if( i ==1) S = "Cancel";
                JButton But = new JButton( S);
                But.addActionListener( new MyActionListener( i ));
                Butts.add( But);
                count++;
                Buttons = Buttons - i;
               
              }           
          }//for
        
       Dialog.getContentPane().add( Butts, BorderLayout.SOUTH);
       Dialog.getContentPane().add(JP , BorderLayout.CENTER);
       if( (size.width > 0) && (size.height > 0))
         Dialog.setSize( java.lang.Math.max(size.width , count*16*5), 
                         size.height + 4*16);
       else
         Dialog.setSize( 400,300);
      
     
      } 

  public int showDialog()
    {Result = 0;
     Dialog.setVisible(true);
     
     return Result;
    }
public static void main( String args[])
   { JPanel JP = new JPanel();
     QuickBoxes QB;
     JP.add( new JTextField(" Hi There"));
     JP.setSize( 100, 16*3);
     QB= new QuickBoxes( JP , B_OK+B_Yes+B_Cancel);
     System.out.println( "size="+ QB.Dialog.getSize());
     System.out.println( QB.showDialog());

   } 
private class MyActionListener implements java.awt.event.ActionListener
  { int Button;
    public MyActionListener( int But)
        {Button = But;
        }
   public void actionPerformed(ActionEvent e)
      { Result = Button;
        Dialog.dispose();
      }
  }
  }
