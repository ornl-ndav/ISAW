/*
 * File:  Parameter.java 
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.9  2006/07/10 21:28:19  dennis
 *  Removed unused imports, after refactoring the PG concept.
 *
 *  Revision 1.8  2006/07/10 16:25:52  dennis
 *  Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 *  Revision 1.7  2006/06/05 21:31:02  dennis
 *  Added/fixed some java doc comments.
 *
 *  Revision 1.6  2003/04/14 21:28:36  pfpeterson
 *  No longer stores information about valid state.
 *
 *  Revision 1.5  2002/11/27 23:16:15  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/05/28 22:24:37  pfpeterson
 *  Implements the IParameter interface.
 *
 */

package  DataSetTools.operator;
import gov.anl.ipns.Parameters.IParameter;

import java.io.*;


/**
 * The class Parameter groups a name and generic object that is used to 
 * pass parameters between a GUI and an operator.
 *
 */

public class Parameter extends Object implements Serializable, IParameter
{
  public static final String NUM_BINS = "Number of Bins";

  private String name;
  private Object value;

  /**
   *  Construct a Parameter object using the specified name an value
   *
   * @param name  The new name string for this Parameter.
   * @param object  An object of the correct type to use as the value of this
   *                parameter.
   */
  public Parameter( String name, Object value )
  {
    this.name  = name;
    this.value = value;
  }

  /**
   * Change the name of the parameter.
   *
   * @param name  The new name string for this Parameter.
   */
  public void setName(String name)
  {
    this.name=name;
  }
                        
  /**
   * Get the name of the parameter
   *
   * @return The string that holds the name of this parameter. 
   */
  public String getName() 
  { 
    return name; 
  }

  /**
   * Returns the value of the parameter, as a generic object
   *
   * @return An object with the value of this parameter.
   */
  public Object getValue() 
  {  
    return value;
  } 

  /**
   * Set the value for the parameter
   *
   * @param object  An object of the correct type to use as the value of this
   *                parameter.
   */
  public void setValue( Object object ) 
  { 
    value = object;
  }

  /**
   * Return a copy of the parameter object.  Note: Currently this is not a
   * "deep copy", but it does create a new Parameter object.  When the value
   * is set for this new parameter object, it will not affect the value of
   * the current parameter object.  Consequently, this "clone" method will
   * meet our current needs.  A "deep copy" would require the ability to 
   * copy an Object of any type.
   *
   * @return a new Parameter object with the same name and value as this
   *         Parameter object.
   */
  public Object clone()
  {
    Parameter new_parameter = new Parameter( name, value );
    return new_parameter;
  }

  /**
   * This returns null.  It must be overridden by subclasses to
   * return a meaningful type String, if that behavior is needed.
   *
   * @return  null, by default.  
   */
  public String getType()
  {
    return null;
  }

}
