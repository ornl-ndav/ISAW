/*
 * File:  SumByAttributeNormSA.java
 *
 * Copyright (C) 1999, Dennis Mikkelson, Alok Chatterjee
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
 *  Revision 1.6  2006/07/10 21:48:01  dennis
 *  Removed unused imports after refactoring to use New Parameter
 *  GUIs in gov.anl.ipns.Parameters
 *
 *  Revision 1.5  2006/07/10 16:25:58  dennis
 *  Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 *  Revision 1.4  2004/03/15 19:33:51  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.3  2004/03/15 03:28:32  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.2  2004/01/24 19:43:33  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.1  2003/09/19 22:47:40  chatterjee
 *  Operator written for Sasha. Adapted from SumByAttribute.java
 *  This operator takes a product of specified groups with their corresponding
 *  solid angles and then sums them up. This sum is then divided by
 *  the sum of the solid angles of these groups.
 *
 *  Revision 1.6  2003/01/09 17:55:46  dennis
 *  Added '\n' to docs from getDocumentation(). (Chris Bouzek)
 *
 *  Revision 1.5  2002/12/06 14:40:55  dennis
 *  getDocumentation() now includes name of parameter. (Chris Bouzek)
 *
 *  Revision 1.4  2002/11/27 23:18:49  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/11/21 22:33:48  dennis
 *  Added getDocumentation() method and documentation on getResult().
 *  (Chris Bouzek)
 *
 *  Revision 1.2  2002/09/19 16:02:21  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:02:59  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.TOF_DG_Spectrometer;

import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Util.SpecialStrings.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.DataSet.DSOpsImplementation;

/**
  *  Sum Data blocks specified by an attribute to form a new DataSet
  *  with one Data block.  The new data set is formed by multiplying
  *  selected Data blocks with a specified attribute in a specified range
  *  with its solid angle and then summing the product and finally dividing 
  *  the resultant sum by the sum of the total solid angle in the specified range.
  *  
  *  @see DataSetTools.dataset.DataSet#getAttributeValue(String)
  *  @see DataSetTools.dataset.AttrUtil
  *  @see DataSetTools.dataset.DataSet#addData_entry(Data)
  */

public class SumByAttributeNormSA extends    DS_TOF_DG_Spectrometer
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

  public SumByAttributeNormSA( )
  {
    super( "Sum Groups based on Attribute & Normalize by Solid Angle" );
   
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
   *                      the selection criteria are to be included in the
   *                      sum, or omitted from the sum
   *  @param  min         The lower bound for the selection criteria.  The
   *                      selected Data blocks satisfy:
   *                          min <= attribute value <= max
   *  @param   max         The upper bound for the selection criteria.
   */

  public SumByAttributeNormSA( DataSet   ds,
                         String    attr_name,
                         boolean   keep,
                         float     min,
                         float     max   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s)

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
   * @return  the command name to be used with script processor:
   *          in this case, SumAtt
   */
   public String getCommand()
   {
     return "SumAttNormSA";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Attribute to use for Selection",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Sum (or omit) selected groups?",
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter( "Lower bound", new Float(-1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Upper bound", new Float(1.0) );
    addParameter( parameter );
  }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator sums a selection of data blocks in a DataSet ");
    s.append("according to selection criteria, which are a user specified attribute ");
    s.append("and a lower and upper range for the attribute.\n");
    s.append("@assumptions The attribute is one that can be used to sum the ");
    s.append("data blocks given.  The number of data blocks which meet the ");
    s.append("selection criteria (i.e. within the range given for the attribute ");
    s.append(" is greater than zero.\n");
    s.append("@algorithm Constructs a new empty DataSet with the same title, ");
    s.append("units, and operations as the current DataSet.  Multiplies the data ");
    s.append("blocks in the current DataSet with the corresponding solid angle and then sums" );
    s.append("up the products over the specified range according to the attribute, and ");
    s.append("then divides the above sum by the sum of the solid angle over the specified");
    s.append("range and uses the sum to fill the new DataSet.\n");
    s.append("@param ds The DataSet for the operation.\n");
    s.append("@param attr_name The name of the attribute to use for the selection ");
    s.append("criteria.\n");
    s.append("@param keep A value of true if you want the data blocks which meet the ");
    s.append("selection criteria to be included in the sum, or false if ");
    s.append("you do not.\n");
    s.append("@param min The lower bound of the selection criteria.\n");
    s.append("@param max The upper bound of the selection criteria.\n");
    s.append("@return A DataSet with one data block which consists of the summed ");
    s.append("values which met the selection criteria.\n");
    s.append("@error Returns an error message if no selected data blocks meet the ");
    s.append("selection criteria.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   * Constructs a new empty DataSet with the same title. units, and operations
   * as the current DataSet.  Sums the data blocks in the current DataSet
   * according to the attribute, after multiplying them with the correspoding solid angle
   * and divinding the total sum by the sum of the solid angle in the specified range
   * and uses the sum to fill the new DataSet.
   *
   * @return A DataSet with the selected data blocks summed and put into
   * one data block.
   */

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
      new_ds.addLog_entry( "summed groups with " + attr_name +
                           " in [" + min + ", " + max + "]" );
    else
      new_ds.addLog_entry( "summed groups except those with " + attr_name +
                           " in [" + min + ", " + max + "]" );

                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data, new_data;
    Attribute attr, attr_sa;
    float tot_sa = 0.0f;

   
  for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );   
																				   // get reference to the data entry
                                           // keep or reject it based on the
                                           // attribute value.
      attr = data.getAttribute( attr_name );
      float val = (float)attr.getNumericValue();
      if (attr_name.equals( Attribute.DETECTOR_POS ))     // convert to degrees
        val *= (float) 180.0/Math.PI;

      if ( keep && min <= val && val <= max  ||
          !keep && (min > val || val > max)   )
      {

        attr_sa = data.getAttribute( Attribute.SOLID_ANGLE ); 
        float val_sa = (float)attr_sa.getNumericValue();
        tot_sa = tot_sa + val_sa;

      }
    }

    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );   
																				   // get reference to the data entry
                                           // keep or reject it based on the
                                           // attribute value.
      attr = data.getAttribute( attr_name );
      float val = (float)attr.getNumericValue();
      if (attr_name.equals( Attribute.DETECTOR_POS ))     // convert to degrees
        val *= (float) 180.0/Math.PI;

      if ( keep && min <= val && val <= max  ||
          !keep && (min > val || val > max)   )
      {
        new_data = (Data)data.clone();

        attr_sa = data.getAttribute( Attribute.SOLID_ANGLE ); 
        float val_sa = (float)attr_sa.getNumericValue();
        new_data = new_data.multiply(val_sa, 1);
        new_data = new_data.multiply(1/tot_sa,1);
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

    SpecialString result = DSOpsImplementation.AddDataBlocks( new_ds );
    if ( result == null )
      return new_ds;
    else
      return result;

  }

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SumByAttributeNormSA Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SumByAttributeNormSA new_op    = new SumByAttributeNormSA( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
      SumByAttributeNormSA sba = new SumByAttributeNormSA();
      String s = sba.getDocumentation();
      System.out.println(s);
  }

}
