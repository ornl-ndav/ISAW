/*
 * File: IGraphableDataGraph.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>

 *
 * $Log$
 * Revision 1.2  2002/11/27 23:25:12  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/07/18 22:06:19  dennis
 * Moved separate OverplotView hiearchy into DataSetTools/viewer
 * hierarchy.
 *
 */

package DataSetTools.viewer.OverplotView;

import DataSetTools.dataset.AttributeList;
import java.util.Vector;
import javax.swing.JComponent;

/**
 * defines a way for arbitrary graphics packages to play nice with
 * OverplotView.
 */
public interface IGraphableDataGraph
{
  public static final String TITLE      = "Graph Title";
  public final static String TITLE_SUB1 = "First Graph Subtitle";
  public final static String TITLE_SUB2 = "Second Graph Subtitle";

  public final static String X_UNITS    = "X-Axis Units";
  public final static String Y_UNITS    = "Y-Axis Units";

  public final static String X_LABEL    = "X-Axis Label";
  public final static String Y_LABEL    = "Y-Axis Label";


  /**
   * converts from GraphableData (the internal format for this viewer)
   * to another format.  this allows future developers/maintainers of 
   * ISAW to plug in a new graphic front end.  
   *
   * this method replaces all previous data.  it does NOT
   * graph the data--graphics are handled by the redraw() method.
   *
   * @param gd - a vector of GraphabldData objects
   */
  public void init( Vector gd );

 
  /**
   * draws all current data on the graphics object, whatever it
   * might be.  
   */
  public JComponent redraw();


  /**
   * s
   */
  public void setAttributeList( AttributeList l );
}  


