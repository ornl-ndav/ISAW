package Command;
import javax.swing.*;

import java.awt.*;

import javax.swing.text.*;


public class MyCursor extends DefaultCaret

{  int w, h;

    public void paint(Graphics g)

    {

    

     if(!isVisible()) return;

   

    try{

	JTextComponent c = getComponent();

        int dot =getDot();

        Rectangle r = c.modelToView(dot);

        g.setColor(new Color(1,1,1,100));//c.getCaretColor());

        //height = char's height

	//width = char's width

         

	//a: g.drawRect( r.x,r.y+4,8,8);

        try{

           String cc= c.getDocument().getText(dot,1);

           FontMetrics fm= g.getFontMetrics();

           w= fm.charWidth(cc.charAt(0));

          if(w<=0) w=5;

           h=fm.getHeight();

            g.fillRect(r.x, r.y,w,h);

            

	}

       catch(Exception s)

         {System.out.println("cursor paint exc="+s);

           }

      }

    catch(BadLocationException e)

	{System.err.println(e);

	}



    }

    protected synchronized void damage(Rectangle r)

    {

    

     if(r==null) return;

     

     //a:x = r.x;

     //a: y = r.y+4 ;//probably r.x,r.y top left of char block

     //a:width=9;//one bigger

     //a:height= 9;

    x=r.x;

    y=r.y;

    width=w+1;

   height=h+1;

    repaint();

    }

}



