/*
 * File:  Rubberband.java  
 *
 *  Adapted from "graphic JAVA", David M. Geary, Alan L. McClellan,
 *  SunSoft Press/Prentice Hall, 1997
 *
 *  $Log$
 *  Revision 1.4  2001/04/23 21:14:10  dennis
 *  Added citation of source: the text "Graphic Java".
 *
 *  Revision 1.3  2000/07/10 22:17:01  dennis
 *  minor format change to documentation
 *
 *  Revision 1.2  2000/07/10 22:11:52  dennis
 *  7/10/2000 version, many changes and improvements
 *
 *  Revision 1.5  2000/05/11 16:53:19  dennis
 *  Added RCS logging
 */

package DataSetTools.components.image;

import java.awt.*;
import java.io.*;
import javax.swing.*;

/** 
 * A abstract base class for rubberbands.<p>
 *
 * Rubberbands do their rubberbanding inside of a JPanel, 
 * which must be specified at construction time.<p>
 * 
 * Subclasses are responsible for implementing 
 * <em>void drawLast(Graphics g)</em> and 
 * <em>void drawNext(Graphics g)</em>.  
 *
 * drawLast() draws the appropriate geometric shape at the last 
 * rubberband location, while drawNext() draws the appropriate 
 * geometric shape at the next rubberband location.  All of the 
 * underlying support for rubberbanding is taken care of here, 
 * including handling XOR mode setting; extensions of Rubberband
 * need not concern themselves with anything but drawing the 
 * last and next geometric shapes.<p>
 *
 * Modified 2/28/2000 by Dennis Mikkelson
 *          Added method move( increment ) to allow the cursor to be easily 
 *          moved by a specific amount in each direction ( say using arrow i
 *          keys )
 *
 * Modified 6/2/98 by Dennis Mikkelson
 *          Added "active" flag that is set true at the start of a 
 *          rubberband operation and set false at the end of the
 *          operation.  This was needed to avoid problems when resizing
 *          a region on which rubberbanding could be done.  In some
 *          circumstances, immediately after making the region smaller,
 *          the release event was sent to the object and a rubberband
 *          was drawn by a call to the "end" method.  Since the "anchor"
 *          method had not been called and no previous rubberband figure
 *          had been called, this damaged the drawing area.
 *
 * @version 1.00, 12/27/95
 * @author  David Geary
 * @see     RubberbandRectangle
 */
abstract public class Rubberband implements Serializable
{
    protected Point anchor    = new Point(0,0); 
    protected Point stretched = new Point(0,0);
    protected Point last      = new Point(0,0); 
    protected Point end       = new Point(0,0);

    protected JPanel component;
    private boolean   firstStretch = true;
    private boolean   active = false;

    abstract public void drawLast(Graphics g);
    abstract public void drawNext(Graphics g);
    private  Color  color = Color.gray;

    public Rubberband(JPanel component) {
        this.component = component;
    }
    public Point getAnchor   () { return anchor;    }
    public Point getStretched() { return stretched; }
    public Point getLast     () { return last;      }
    public Point getEnd      () { return end;       }

    public void anchor(Point p) {
      if ( !active )                   // only set anchor point if we have 
      {                                // not already started the rubberband
        active = true;
        firstStretch = true;
        anchor.x = p.x;
        anchor.y = p.y;

        stretched.x = last.x = anchor.x;
        stretched.y = last.y = anchor.y;
      }
    }

    public boolean stretch(Point p) {
        if ( !active )                  // don't do stretching if we haven't
          return( false );              // started yet. D.M.

        last.x      = stretched.x;
        last.y      = stretched.y;
        stretched.x = p.x;
        stretched.y = p.y;

        Graphics g = component.getGraphics();
        if(g != null) {
            g.setXORMode( color );

            if(firstStretch == true) 
              firstStretch = false;
            else                     
              drawLast(g);

            drawNext(g);
        }
        return ( true );                // stretch OK, D.M.
    }

    public boolean move ( Point increment )    // 2/28/2000 D.M.
    {
      if ( !active )                  // don't do the move if we haven't
        return( false );              // started yet. D.M.

      Point temp = new Point(0,0);

      temp.x = stretched.x + increment.x;
      temp.y = stretched.y + increment.y;
      
      return( stretch(temp) );
    }

    public boolean end(Point p) {
      if ( !active )                    // ignore ending request if rubberband 
        return ( false );               // not active. 

      else
      {
        last.x = end.x = p.x;
        last.y = end.y = p.y;

        if ( !firstStretch )            // only redraw to erase last cursor IF
        {                               // we've already drawn something
          Graphics g = component.getGraphics();
          if(g != null) {
//            g.setXORMode(component.getBackground());
              g.setXORMode( color );
              drawLast(g);
          }
        }
        active = false;
        return ( true );
      }
    }

    public Rectangle bounds() {
      return new Rectangle(stretched.x < anchor.x ? 
                           stretched.x : anchor.x,
                           stretched.y < anchor.y ? 
                           stretched.y : anchor.y,
                           Math.abs(stretched.x - anchor.x),
                           Math.abs(stretched.y - anchor.y));
    }

    public Rectangle lastBounds() {
      return new Rectangle(
                  last.x < anchor.x ? last.x : anchor.x,
                  last.y < anchor.y ? last.y : anchor.y,
                  Math.abs(last.x - anchor.x),
                  Math.abs(last.y - anchor.y));
    }
}
