/*
 * File: GraphableData.java
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

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import java.awt.Color;
import java.lang.String;

/**
 * container for data and state information associated with data that
 * is intended for visualization.
 */
public class GraphableData
{

  public static final String COLOR    = "Color";
  public static final String MARKER   = "Marker Type";
  public static final String NAME     = "Name of this data";
  public static final String LINETYPE = "Line Type";
  public static final String OFFSET   = "Absolute (addivite) Offset";

  private Data          data;
  private AttributeList attrs;
  private float         offset;
 

  /**
   * default constructor
   */
  public GraphableData( Data d )
  {
    data = d;
    offset = 0.0f;
    attrs = new AttributeList();
  }


  /**
   * associate information with this data.  use the fields provided
   * by GraphableData as the 'name' parameter of each (sub class of) Attribute
   * added to this data to provide a uniform way for recipients to find
   * the information that they need.
   */
  public void addAttribute( Attribute attr )
  {
    attrs.addAttribute( attr );
  }


  /**
   * returns a clone of the data.
   */
  public Data getData()
  {
    return (Data)data.clone();
  }


  /**
   * returns a clone of the attributes for this data
   */
  public AttributeList getAttributeList()
  {
    return (AttributeList)attrs.clone();
  }


  /**
   * set the attributes for this data.  use this mechanism to set colors,
   * marker types, line types, etc.
   */
  public void setAttributeList( AttributeList l )
  {
    attrs = l;
  }
 

  /**
   * provides a string representation of this GraphableData object.
   */
  public String toString()
  {
    return data.toString();
  }
}



