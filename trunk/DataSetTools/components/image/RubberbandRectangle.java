/*
 * @(#) RubberbandRectangle.java  1.0    1998/07/29   Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.3  2000/07/10 22:17:02  dennis
 *  minor format change to documentation
 *
 *  Revision 1.2  2000/07/10 22:11:53  dennis
 *  7/10/2000 version, many changes and improvements
 *
 *  Revision 1.3  2000/05/11 16:53:19  dennis
 *  Added RCS logging
 *
 */

package DataSetTools.components.image;

import javax.swing.*;
import java.io.*;
import java.awt.Graphics;
import java.awt.Rectangle;

/** 
 * A Rubberband that does rectangles.
 *
 * @version 1.00, 12/27/95
 * @author  David Geary
 * @see     Rubberband
 */
public class RubberbandRectangle extends    Rubberband 
                                 implements Serializable
{
    public RubberbandRectangle(JPanel component) {
        super(component);
    }
    public void drawLast(Graphics graphics) {
        Rectangle rect = lastBounds();
        graphics.drawRect(rect.x, rect.y, 
                          rect.width, rect.height);
    }
    public void drawNext(Graphics graphics) {
        Rectangle rect = bounds();
        graphics.drawRect(rect.x, rect.y, 
                          rect.width, rect.height);
    }
}
