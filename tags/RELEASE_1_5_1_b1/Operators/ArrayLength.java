/*
 * File: ArrayLength.java
 *
 * Copyright (C) 1999, John Hammonds
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
 * Contact : John Hammonds <jphammonds@@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
 *           USA
 *
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2003/03/03 18:54:31  dennis
 *  Syntax "fix" in case obj_in is an Array.
 *
 *  Revision 1.1  2003/02/26 23:04:30  hammonds
 *  New Class to determine the length of an array.  In Isaw Arrays are 
 *  actually Vectors.  Both Arrays and Vectors can be passed.
 */

package Operators;

import  java.io.*;
import  java.util.*;
import  java.lang.reflect.Array;
import  DataSetTools.operator.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Generic.Batch.*;

/**
 *   This class supports reading of Strings, floats, ints etc. from an
 *   ordinary text file.  In addition to methods to read each of the 
 *   basic data types from the file, there is an "unread" method that 
 *   restores the last non-blank item that was read.  Error handling and
 *   end of file detection are done using exceptions.
 */

public class ArrayLength extends GenericBatch implements Serializable
{    
  
	
  /* -------------------------- Constructor -------------------------- */
  /**
   *  Construct a SetupReader to read from the specified file.  The
   *  constructor will throw an exception if the file can't be opened.  The
   *  other methods of this class should not be used if the file can't be
   *  opened.
   *
   *  @param array_in  Input Vector whose length is to be returned.
   *
  */

  public ArrayLength( Vector array_in )
  {  
    this();
    Vector parameters = new Vector();
    addParameter(new Parameter("Array", array_in ) );
  }


  public ArrayLength(  )
  {
    super( "ArrayLength");
  } 




  /** 
   * Get the name of this operator, used in scripts
   * @return "ArrayLength", the command used to invoke this operator
   * in Scripts
   */
  public String getCommand()
  {
    return "ArrayLength";
  }
  /** 
   * Sets default values for the parameters. The parameters set must
   * match the data types of the parameters used in the constructor.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Array", null));
  }


  /** 
   *  Executes this operator using the current values of the
   *  parameters.
   */

  public Object getResult()
  {
    Object  obj_in = (Object)getParameter(0).getValue();
    int len=-1;
    Integer result = new Integer(len);
    if ( obj_in instanceof Vector ) {
      len = ((Vector)obj_in).size();
    }
    else if (obj_in instanceof Array) {
      len = Array.getLength(obj_in);
    }    
    result = new Integer(len);
    return result;
  }
    
 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                                    
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview ");

    s.append("@assumptions");
    s.append("@algorithm ");
    s.append("@param array_in The array whose length is to be returned");
    s.append("@return Lenght of the input array or vector" );
    //    s.append("If unsuccessful, an error string is returned.");
    //    s.append("@error If execution of the operator generates any exception, ");
    //s.append("an error string is returned.");
    return s.toString();
  }  


}
