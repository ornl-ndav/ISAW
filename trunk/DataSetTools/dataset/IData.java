/*
 * File: IData.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *  $Log$
 *  Revision 1.3  2002/04/19 15:42:31  dennis
 *  Revised Documentation
 *
 *  Revision 1.2  2002/04/04 18:01:00  dennis
 *  Removed setErrors( errors[] ) which can only be applied to
 *  TabulatedData objects.
 *
 */

package  DataSetTools.dataset;

import DataSetTools.dataset.XScale;
import DataSetTools.dataset.IAttributeList;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Attribute;

public interface IData extends IAttributeList 
{
  public static final int KEEP    = 1;           // constants to control how
  public static final int AVERAGE = 2;           // stitching is done where two
  public static final int DISCARD = 3;           // Data blocks overlap

  public static final int SMOOTH_NONE   = 0; 
  public static final int SMOOTH_LINEAR = 1;

  // These "book keeping" methods will be implemented in the Data class, so that
  // the implementations don't need to be repeated in each derived class.  

  public void    setGroup_ID( int id );
  public int     getGroup_ID();

  public void    setSelected( boolean flag );
  public boolean isSelected();
  public void    toggleSelected();
  public boolean isMostRecentlySelected();
  public long    getSelectionTagValue();

  public void    setHide( boolean flag );
  public boolean isHidden();
  public void    toggleHide();

  public void    combineAttributeList( Data d );

  // Each Data object will have a current set of X values.  The base class
  // can implement the methods dealing with them for all derived classes.
  public float[]       getX_values();
  public XScale        getX_scale();

  // These are convenience methods that get a reference to the lower level 
  // array and then copy it and return a copy of it.  These are implemented
  // in the base class.
  public float[]       getCopyOfY_values();
  public float[]       getCopyOfErrors();

  // The arithmetic operators and stitching are implemented in the Data
  // class.  In all cases they return TabulatedData.
  //
  public Data    add( Data d );
  public Data    add( float y, float err );
  public Data    subtract( Data d );
  public Data    subtract( float y, float err );
  public Data    multiply( Data d );
  public Data    multiply( float y, float err );
  public Data    divide( Data d );
  public Data    divide( float y, float err );

  public Data    stitch( Data other_data, int overlap );

  // The remaining methods are abstract and will have to be implemented in 
  // each derived class.  Consequently, the semantics of the methods will vary
  // from class to class.
  // NOTE: There is not a setY_values() or a setX_values().  The x and y 
  //       values must remain coordinated, and are only directly set in the 
  //       constructor or indirectly adjusted using the resample method. 
 
  public boolean isHistogram();
  public float[] getY_values();
  public float[] getY_values( XScale x_scale, int smooth_flag ); 
  public float   getY_value( float x, int smooth_flag ); 

  public float[] getErrors();
  public void    setSqrtErrors();

  public void    resample( XScale x_scale, int smooth_flag );

  public String  toString();
  public Object  clone();
}
