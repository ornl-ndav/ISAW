/*
 * File:  SpectrometerTofToEnergyLoss.java 
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
 *  Revision 1.1  2002/02/22 21:00:56  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.10  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.9  2001/04/26 19:11:35  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.8  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *  Revision 1.7  2000/08/08 21:20:19  dennis
 *  Now propagate errors, rather than set them to SQRT(counts)
 *
 *  Revision 1.6  2000/08/02 01:42:23  dennis
 *  Changed to use Data.ResampleUniformly() so that the operation can be
 *  applied to functions as well as to histograms.
 *
 *  Revision 1.5  2000/07/10 22:36:19  dennis
 *  Now Using CVS 
 *
 *  Revision 1.13  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.12  2000/05/25 18:49:06  dennis
 *  Fixed bug: DataSet attributes were not copied properly.
 *
 *  Revision 1.11  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.10  2000/05/15 21:43:45  dennis
 *  now uses constant Parameter.NUM_BINS rather than the string
 *  "Number of Bins"
 *
 *  Revision 1.9  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *   
 *  2000/04/21  Added methods to set better
 *              default parameters. Now it
 *              is derived from the class
 *              XAxisConversionOp
 *   
 *  99/08/16    Added constructor to allow
 *              calling operator directly
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;

/**
 * This operator converts a neutron time-of-flight DataSet to energy loss.  The
 * DataSet must contain spectra with attributes giving the detector position
 * and initial energy. In addition, it is assumed that the XScale for the
 * spectra represents the time-of-flight from the sample to the detector.
 * 
 */

public class SpectrometerTofToEnergyLoss extends    XAxisConversionOp 
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

  public SpectrometerTofToEnergyLoss( )
  {
    super( "Convert to Energy Loss" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_E       The minimum energy loss value to be binned
   *  @param  max_E       The maximum energy loss value to be binned
   *  @param  num_E       The number of "bins" to be used between min_E and
   *                      max_E
   */

  public SpectrometerTofToEnergyLoss( DataSet     ds,
                                      float       min_E,
                                      float       max_E,
                                      int         num_E )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_E ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_E ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_E ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case, ToEL
   */
   public String getCommand()
   {
     return "ToEL";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    UniformXScale scale = getXRange();

    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;
    
    if ( scale == null )
      parameter = new Parameter( "Min Energy Loss(meV)", new Float(-50.0) );
    else
      parameter = new Parameter( "Min Energy Loss(meV)",
                                  new Float(scale.getStart_x()) );
    addParameter( parameter );

    if ( scale == null )
      parameter = new Parameter( "Max Energy Loss(meV)", new Float(50.0) );
    else
      parameter = new Parameter( "Max Energy Loss(meV)",
                                  new Float(scale.getEnd_x()) );
    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer( 500) );
    addParameter( parameter );
  }


  /* -------------------------- new_X_label ---------------------------- */
  /**
   * Get string label for converted x values.
   *
   *  @return  String describing the x label and units for converted x values.
   */
   public String new_X_label()
   {
     return new String( "E Loss(meV)" );
   }


  /* ---------------------- convert_X_Value ------------------------------- */
  /**
   * Evaluate the axis conversion function at one point only.
   *
   *  @param  x    the x-value where the axis conversion function is to be
   *               evaluated.
   *
   *  @param  i    the index of the Data block for which the axis conversion
   *               function is to be evaluated.
   *
   *  @return  the value of the axis conversion function at the specified x.
   */
  public float convert_X_Value( float x, int i )
  {
    DataSet ds = this.getDataSet();          // make sure we have a DataSet
    if ( ds == null )
      return Float.NaN;

    int num_data = ds.getNum_entries();      // make sure we have a valid Data
    if ( i < 0 || i >= num_data )            // index
      return Float.NaN;

    Data data               = ds.getData_entry( i );
    AttributeList attr_list = data.getAttributeList();

                                             // get the detector position and
                                             // initial energy 
    DetectorPosition position=(DetectorPosition)
                       attr_list.getAttributeValue( Attribute.DETECTOR_POS);

    Float energy_in_obj=(Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);

    if( position == null || energy_in_obj == null)     // make sure it has the
      return Float.NaN;                                // needed attributes
                                                       // to convert it to D

    float spherical_coords[] = position.getSphericalCoords();
    float energy_in        = energy_in_obj.floatValue();

    return (energy_in - tof_calc.Energy( spherical_coords[0], x ));
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "meV",
                                     "EnergyLoss",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Energy Loss" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

                                     // get the energy scale parameters 
    float min_E = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_E = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_E = ( (Integer)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate energy bounds
    if ( min_E > max_E )             // swap bounds to be in proper order
    {
      float temp = min_E;
      min_E = max_E;
      max_E = temp;
    }

    UniformXScale new_e_scale;
    if ( num_E <= 1.0 || min_E >= max_E )       // no valid scale set
      new_e_scale = null;
    else
      new_e_scale = new UniformXScale( min_E, max_E, num_E );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            energy_in;
    Float            energy_in_obj;
    float            y_vals[];              // y_values from one spectrum
    float            errors[];
    float            e_vals[];              // energy values at bin boundaries
                                            // calculated from tof bin bounds
    XScale           E_scale;
    float            spherical_coords[];
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry
      attr_list = data.getAttributeList();
                                           // get the detector position and
                                           // initial path length 
      position=(DetectorPosition)
                   attr_list.getAttributeValue(Attribute.DETECTOR_POS);

      energy_in_obj=(Float)
                      attr_list.getAttributeValue(Attribute.ENERGY_IN);

      if( position != null && energy_in_obj != null)
                                                       // has needed attributes 
      {                                                // so convert it to E
                                       // calculate energies at bin boundaries
        energy_in        = energy_in_obj.floatValue();

        spherical_coords = position.getSphericalCoords();
        e_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < e_vals.length; i++ )
          e_vals[i] = energy_in - 
                      tof_calc.Energy( spherical_coords[0], e_vals[i] );
  
        E_scale = new VariableXScale( e_vals );
        y_vals  = data.getY_values();
        errors  = data.getErrors();

        new_data = new Data( E_scale, y_vals, errors, data.getGroup_ID() ); 
                                                // create new data block with 
                                                // non-uniform E_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

        if ( new_e_scale != null )                    // resample if a valid
          new_data.ResampleUniformly( new_e_scale );  // scale was specified

        new_ds.addData_entry( new_data );      
      }
    }
//    ChopTools.chop_dataDrawer.drawgraphDataSet(new_ds);
    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerTofToEnergyLoss Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerTofToEnergyLoss new_op = new SpectrometerTofToEnergyLoss( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }



}
