/**
 * File: OneVarParameterizedFunction.java 
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
 *  Revision 1.1  2002/04/04 19:44:52  dennis
 *  Abstract base class for functions of one variable that are controlled
 *  by a list of parameters.
 *
 */

package DataSetTools.dataset;

import DataSetTools.math.*;
import DataSetTools.util.*;

/**
 *  This is an abstract base class for parameterized functions of one 
 *  variable.
 *  Derived classes must implement the methods that provide the y values
 *  at specified x values.
 */

abstract public class OneVarParameterizedFunction extends OneVarFunction 
{
  private float          parameters[];
  private String         parameter_names[];

  OneVarParameterizedFunction( String name, 
                               float  parameters[], 
                               String parameter_names[] )
  {
    super( name );
                                
    if ( parameters != null )                 // make a valid parameter list,
      this.parameters = parameters;      
    else
      this.parameters = new float[0];
                                              // copy any names that were given
                                              // into a new list of names, and
                                              // synthesize the rest. 
    this.parameter_names = new String[ parameters.length ]; 
    if ( parameter_names != null )           
    {
      for ( int i = 0; i < parameter_names.length; i++ )
        this.parameter_names[i] = parameter_names[i]; 

      for ( int i = parameter_names.length; i < parameters.length; i++ )
        this.parameter_names[i] = "P"+i; 
    }
    else
      for ( int i = 0; i < parameters.length; i++ )
        parameter_names[i] = "P"+i; 
  }
  
  abstract public double   getValue( double x );
  abstract public float    getValue( float x );
  abstract public float[]  getValues( float x[] );
  abstract public double[] getValues( double x[] );


  public int numParameters() 
  {
    return parameters.length;
  }


  public float[] getParameters()
  {
    return parameters;
  }


  public void setParameters( float parameters[] )
  {
    if ( parameters != null )
    { 
      int num_params = Math.min( this.parameters.length, parameters.length );
      for ( int i = 0; i < num_params; i++ )
        this.parameters[i] = parameters[i];
    }
  }
  
  public String[] getParameterNames()
  {
    return parameter_names;
  }


  public String toString()
  {
    String state = super.toString();
    for ( int i = 0; i < parameters.length; i++ )
      state += parameter_names[i] + ": " + parameters[i] + "\n";
    return state;
  } 

}
