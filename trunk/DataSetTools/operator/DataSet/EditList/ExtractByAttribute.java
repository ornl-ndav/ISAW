/*
 * File:  ExtractByAttribute.java 
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
 * $Log$
 * Revision 1.3  2002/11/27 23:17:40  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/09/19 16:01:14  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/02/22 21:01:55  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.EditList;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator forms a new data set by selecting Data blocks with a 
  * specified attribute in a specified range.
  */

public class ExtractByAttribute extends    DS_EditList 
                                implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this 
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult() 
   * to apply the operator to the DataSet this operator was added to. 
   */

  public ExtractByAttribute( )
  {
    super( "Extract Groups based on Attribute" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  attr_name   The name of that attribute to be used for the 
   *                      selection criterion
   *  @param  keep        Flag that indicates whether Data blocks that meet 
   *                      the selection criteria are to be kept or removed 
   *                      from the data set.
   *  @param  min         The lower bound for the selection criteria.  The
   *                      selected Data blocks satisfy:
   *                          min <= attribute value <= max
   *  @param  max         The upper bound for the selection criteria.
   */

  public ExtractByAttribute( DataSet  ds,
                             String   attr_name,
                             boolean  keep,
                             float    min,
                             float    max   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new AttributeNameString(attr_name) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( keep ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( min ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Float( max ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, ExtAtt
   */
   public String getCommand()
   {
     return "ExtAtt";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group Attribute to use for Selection",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Keep (or delete) selected groups?",
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter( "Lower bound", new Float(-1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Upper bound", new Float(1.0) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                  // get the parameters specified by the user 

    String attr_name = 
           ((AttributeNameString)getParameter(0).getValue()).toString();
    boolean keep     = ((Boolean)getParameter(1).getValue()).booleanValue();

    float min = ( (Float)(getParameter(2).getValue()) ).floatValue();
    float max = ( (Float)(getParameter(3).getValue()) ).floatValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
    if ( keep )
      new_ds.addLog_entry( "kept groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
    else
      new_ds.addLog_entry( "omitted groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry
                                           // keep or reject it based on the
                                           // attribute value.
      Attribute attr = data.getAttributeList().getAttribute( attr_name );
      float val = (float)attr.getNumericValue(); 
      if (attr_name.equals( Attribute.DETECTOR_POS ))    // convert to degrees
        val *= (float) 180.0/Math.PI;

      if ( keep && min <= val && val <= max  ||
          !keep && (min > val || val > max)   ) 
      {
        new_data = (Data)data.clone();
        new_ds.addData_entry( new_data );      
      } 
   
    }

    if ( new_ds.getNum_entries() <= 0 )
    {
      ErrorString message = new ErrorString(
                         "ERROR: No Data blocks satisfy the condition" );
      System.out.println( message );
      return message;
    }
    else
      return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current ExtractByAttribute Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    ExtractByAttribute new_op = new ExtractByAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
