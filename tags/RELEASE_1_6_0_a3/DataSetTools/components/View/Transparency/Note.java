/*
 * File: Note.java
 *
 * Copyright (C) 2003, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2003/10/17 23:00:25  millermi
 *  - Removed this private class from the AnnotationOverlay
 *    and made it a separate public class.
 *
 */
package DataSetTools.components.View.Transparency;

import javax.swing.JTextField; 
import java.awt.Point;

import DataSetTools.components.View.Cursor.Line;
import DataSetTools.components.image.CoordBounds;
import DataSetTools.util.floatPoint2D;

/**
 * This class creates a datastructure to easily group together a String 
 * annotation, its location to be displayed, and the bounds it was
 * created in. This class is primarily used by the AnnotationOverlay.
 *
 *  @see DataSetTools.components.View.Transparency.AnnotationOverlay
 */
public class Note
{
   private JTextField textfield; // actual note being drawn
   private Line arrow;  	 // location to draw this note (p1, p2)
   private CoordBounds scale;	 // the bounds of the overlay when this 
        			 // note was created
   private floatPoint2D wcp1;	 // the world coordinate associated with p1
   private floatPoint2D wcp2;	 // the world coordinate associated with p2
  
  /**
   * This constructor creates a Note object with bottom-left corner at point
   * p2 in pixel coordinates and wc_p2 in world coordinates.
   *
   *  @param  t - the actual text message
   *  @param  l - the line from point of interest to the annotation (in pixel
   *              coordinates)
   *  @param  s - the image bounds in which the annotation was created
   *  @param  wc_p1 - the world coordinate value of p1 of the line.
   *  @param  wc_p2 - the world coordinate value of p2 of the line.
   */ 
   public Note(String t, Line l, CoordBounds s, floatPoint2D wc_p1,
        				      floatPoint2D wc_p2 )
   {
      textfield = new JTextField(t);
      arrow = new Line(l.getP1(), l.getP2());
      scale = s;
      wcp1 = new floatPoint2D( wc_p1 );
      wcp2 = new floatPoint2D( wc_p2 );
   }
  
  /**
   * Get the text message displayed as an annotation.
   *
   *  @return the actual note itself
   */ 
   public String getText()
   {
      return textfield.getText();
   }
   
  /**
   * Get the pixel coordinates of the bottom-left corner of the annotation. 
   *
   *  @return the location the annotation will be drawn at.
   */ 
   public Point getLocation()
   {
      return arrow.getP2();
   }
  
  /**
   * Since zooming is allowed, each annotation could be created in a different
   * world coordinate scale. This returns the CoordBounds at the time the
   * annotation was created.
   *
   *  @return the rectangle bounds this annotation was created in.
   */ 
   public CoordBounds getScale()
   {
      return scale;
   }
  
  /**
   * The textfield that was originally used to enter the annotation.
   *
   *  @return the textfield containing the string. A textfield was chosen
   *	      instead of just a string because when editing is allowed,
   *	      a textfield will be required anyways.
   */ 
   public JTextField getTextField()
   {
      return textfield;
   }
  
  /**
   * Get the Line object that was created along with this annotation.
   *
   *  @return the line representing the arrow from p1 ( the point near
   *	      the intended area ) to p2 ( the location of the annotation ).
   *	      This also allows the paint to draw a line from p1 to p2. 
   */ 
   public Line getLine()
   {
      return arrow;
   }
  
  /**
   * Get the world coordinate point of the first point in the line.
   *
   *  @return the world coordinate point p1 refers to.
   */ 
   public floatPoint2D getWCP1()
   {
      return wcp1;
   } 
  
  /**
   * Set the world coordinate point of the first point in the line.
   *
   *  @param p1 - the world coordinate point where the line began.
   */ 
   public void setWCP1( floatPoint2D p1 )
   {
      wcp1 = p1;
   }
  
  /**
   * Get the world coordinate point of the second point in the line.
   *
   *  @return the world coordinate point p2 refers to.
   */ 
   public floatPoint2D getWCP2()
   {
      return wcp2;
   }
  
  /**
   * Set the world coordinate point of the second point in the line.
   *
   *  @param p2 - the world coordinate point where the line ends.
   */ 
   public void setWCP2( floatPoint2D p2 )
   {
      wcp2 = p2;
   }
} // end of Note

