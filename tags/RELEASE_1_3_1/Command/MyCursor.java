/*
 * File:  MyCursor.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Revision 1.3  2001/06/01 21:14:13  rmikk
 * Added Documentation for javadocs etc.
 *

  5-15-2001  Created
 */
package Command;
import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;

/** Creates a cusor that covers letters
 */
public class MyCursor extends DefaultCaret

{  int w, h;
  
  /** Paints the cursor over the letters
  */
    public void paint(Graphics g)
    {  
     if(!isVisible()) return;
     try{
	JTextComponent c = getComponent();
        int dot =getDot();
        Rectangle r = c.modelToView(dot);
        g.setColor(new Color(1,1,1,100));//c.getCaretColor());
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

   /** Specifies the region that is damaged when the cursor moves
   */
    protected synchronized void damage(Rectangle r)
    { 
     if(r==null) return;
     x=r.x;
     y=r.y;
     width=w+1;
     height=h+1;
      repaint();
    }

}



