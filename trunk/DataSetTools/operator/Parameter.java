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
 *  Revision 1.4  2002/05/28 22:24:37  pfpeterson
 *  Implements the IParameter interface.
 *
 *  Revision 1.3  2001/04/26 19:10:14  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.2  2000/07/10 22:36:13  dennis
 *  Now Using CVS 
 *
 *  Revision 1.3  2000/05/15 21:43:01  dennis
 *  removed DOS control-M characters.
 *
 *  Revision 1.2  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 */

package  DataSetTools.operator;
import java.io.*;
import DataSetTools.parameter.*;

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
  private boolean valid;

  /**
   *  Construct a Parameter object using the specified name an value
   */
  public Parameter( String name, Object value )
  {
    this.name  = name;
    this.value = value;
    this.valid = false;
  }

    /**
     * Specify the name of the parameter.
     */
    public void setName(String name){
        this.name=name;
    }
                        
  /**
   * Returns the name of the parameter
   */
  public String getName() 
  { 
    return name; 
  }

  /**
   * Returns the value of the parameter, as a generic object
   */
  public Object getValue() 
  {  
    return value;
  } 

  /**
   * Set the value for the parameter
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
   */
  public Object clone()
  {
    Parameter new_parameter = new Parameter( name, value );
    return new_parameter;
  }

    /**
     * Accessor method.
     */
    public void setValid(boolean valid){
        this.valid=valid;
    }

    /**
     * Mutator method.
     */
    public boolean getValid(){
        return this.valid;
    }

    /**
     * This returns null. Should be overridden by subclasses.
     */
    public String getType(){
        return null;
    }

}
