package DataSetTools.components.image;

import java.awt.*;
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
 * @see     RubberbandLine
 * @see     RubberbandRectangle
 * @see     RubberbandEllipse
 * @see     gjt.test.RubberbandTest
 */
abstract public class Rubberband {
    protected Point anchor    = new Point(0,0); 
    protected Point stretched = new Point(0,0);
    protected Point last      = new Point(0,0); 
    protected Point end       = new Point(0,0);

    protected JPanel component;
    private boolean   firstStretch = true;
    private boolean   active = false;

    abstract public void drawLast(Graphics g);
    abstract public void drawNext(Graphics g);

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
            g.setXORMode(component.getBackground());

            if(firstStretch == true) 
              firstStretch = false;
            else                     
              drawLast(g);

            drawNext(g);
        }
        return ( true );                // stretch OK, D.M.
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
              g.setXORMode(component.getBackground());
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
