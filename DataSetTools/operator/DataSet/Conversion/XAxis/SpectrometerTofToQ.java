/*
 * File:  SpectrometerTofToQ.java 
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
 *  Revision 1.4  2002/07/02 17:06:54  pfpeterson
 *  Now uses string constants defined in IsawGUI.Isaw.
 *
 *  Revision 1.3  2002/03/18 21:33:24  dennis
 *  Now checks whether or not the errors array is null before attempting
 *  to reverse the array.
 *
 *  Revision 1.2  2002/03/13 16:19:17  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.1  2002/02/22 21:00:57  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.7  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.6  2001/04/26 19:11:38  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.5  2000/11/10 22:41:34  dennis
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
 *  Revision 1.4  2000/08/08 21:20:20  dennis
 *  Now propagate errors, rather than set them to SQRT(counts)
 *
 *  Revision 1.3  2000/08/02 01:43:55  dennis
 *  Changed to use Data.ResampleUniformly() so that the operation can be
 *  applied to functions as well as to histograms.
 *
 *  Revision 1.2  2000/07/17 18:18:14  dennis
 *  Changed call to VelocityOfEnergy() to VelocityFromEnergy() since some
 *  math.tof_calc.java method names were changed for consistency.
 *
 *  Revision 1.1  2000/07/10 22:36:20  dennis
 *  Now Using CVS 
 *  
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  IsawGUI.Isaw;

/**
 * This operator converts neutron time-of-flight DataSet to Q.  The
 * DataSet must contain spectra with an attribute giving the detector position.
 * In addition, it is assumed that the XScale for the spectra represents the
 * time-of-flight from the sample to the detector.
 */

public class SpectrometerTofToQ extends    XAxisConversionOp
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

  public SpectrometerTofToQ( )
  {
    super( "Convert to Q" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_Q       The minimum Q value to be binned
   *  @param  max_Q       The maximum Q value to be binned
   *  @param  num_Q       The number of "bins" to be used between min_Q and
   *                      max_Q
   */

  public SpectrometerTofToQ( DataSet     ds,
                             float       min_Q,
                             float       max_Q,
                             int         num_Q )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_Q ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_Q ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_Q ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case, ToQ
   */
   public String getCommand()
   {
     return "ToQ";
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
      parameter = new Parameter( "Min Q("+Isaw.InvAngstrom+")",
                                 new Float(5.0) );
    else
      parameter = new Parameter( "Min Q("+Isaw.InvAngstrom+")", 
                                  new Float(scale.getStart_x()) );
    addParameter( parameter );

    if ( scale == null )
      parameter = new Parameter("Max Q("+Isaw.InvAngstrom+")",
                                new Float(500.0) );
    else
      parameter = new Parameter("Max Q("+Isaw.InvAngstrom+")",
                                new Float(scale.getEnd_x()));

    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer( 500 ) );
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
     return new String( "Q("+Isaw.InvAngstrom+")" );
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

                                             // get the detector position
    DetectorPosition position=(DetectorPosition)
                       attr_list.getAttributeValue( Attribute.DETECTOR_POS);

    Float energy_in_obj=(Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);

    if( position == null || energy_in_obj == null )    // make sure it has the
      return Float.NaN;                                // needed attributes
                                                       // to convert it to Q

    float spherical_coords[] = position.getSphericalCoords();
    float e_in               = energy_in_obj.floatValue();

    float e_out = tof_calc.Energy( spherical_coords[0], x );

    float Q1 = tof_calc.SpectrometerQ( e_in, 
                                       e_out, 
                                       position.getScatteringAngle() );

    float v_in  = tof_calc.VelocityFromEnergy( e_in ); 
    float v_out = tof_calc.VelocityFromEnergy( e_out );

    float Qx = (float) ( tof_calc.MN_KG / tof_calc.H_BAR_ES *
                     ( v_in - v_out * Math.sin( spherical_coords[2] ) *
                                      Math.cos( spherical_coords[1] )));
    float Qy = (float)( -tof_calc.MN_KG / tof_calc.H_BAR_ES *
                      v_out * Math.sin( spherical_coords[2] ) *
                              Math.sin( spherical_coords[1] ));
                                    
    float Qz = (float)( -tof_calc.MN_KG / tof_calc.H_BAR_ES *
                      v_out * Math.cos( spherical_coords[2] ));
    float Q = (float)Math.sqrt( Qx*Qx + Qy*Qy + Qz*Qz );

//   System.out.println("Q calculations: " + Q + " " + Q1 );

    return Q;
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
                                     "inv A",
                                     "Q",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Q" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

                                     // get the Q scale parameters 
    float min_Q = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_Q = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_Q = ( (Integer)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate Q bounds
    if ( min_Q > max_Q )             // swap bounds to be in proper order
    {
      float temp = min_Q;
      min_Q = max_Q;
      max_Q = temp;
    }

    UniformXScale new_Q_scale;
    if ( num_Q < 2 || min_Q >= max_Q )      // no valid scale set
      new_Q_scale = null;
    else
      new_Q_scale = new UniformXScale( min_Q, max_Q, num_Q );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            y_vals[];              // y_values from one spectrum
    float            errors[];              // errors from one spectrum
    float            Q_vals[];              // Q values at bin boundaries
                                            // calculated from tof bin bounds
    XScale           Q_scale;
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

      Float energy_in_obj=
                      (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);

      if( position != null && energy_in_obj != null )     
      {                               // has needed attributes so convert to Q 
                                      // calculate Qs at bin boundaries
        spherical_coords = position.getSphericalCoords();
        float e_in       = energy_in_obj.floatValue();

        Q_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < Q_vals.length; i++ )
        {
          float e_out      = tof_calc.Energy( spherical_coords[0], Q_vals[i] );

         /*
          Q_vals[i] = tof_calc.SpectrometerQ( e_in, 
                                              e_out, 
                                              position.getScatteringAngle() );
          */

          float v_in  = tof_calc.VelocityFromEnergy( e_in ); 
          float v_out = tof_calc.VelocityFromEnergy( e_out ); 

          float Qx = (float) ( tof_calc.MN_KG / tof_calc.H_BAR_ES *
                     ( v_in - v_out * Math.sin( spherical_coords[2] ) *
                                      Math.cos( spherical_coords[1] )));
          float Qy = (float)( -tof_calc.MN_KG / tof_calc.H_BAR_ES *
                      v_out * Math.sin( spherical_coords[2] ) *
                              Math.sin( spherical_coords[1] ));
                  
          float Qz = (float)( -tof_calc.MN_KG / tof_calc.H_BAR_ES *
                      v_out * Math.cos( spherical_coords[2] ));
          Q_vals[i] = (float)Math.sqrt( Qx*Qx + Qy*Qy + Qz*Qz );
        } 
                                               // reorder values to keep in
                                               // increasing order
        arrayUtil.Reverse( Q_vals );
        Q_scale = new VariableXScale( Q_vals );

        y_vals = data.getCopyOfY_values();     // need copy, since we alter it
        errors = data.getCopyOfErrors();
        arrayUtil.Reverse( y_vals );
        if ( errors != null )
          arrayUtil.Reverse( errors );

        new_data = Data.getInstance( Q_scale, 
                                     y_vals, 
                                     errors, 
                                     data.getGroup_ID() ); 
                                                // create new data block with 
                                                // non-uniform Q_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

                                                // resample if a valid
        if ( new_Q_scale != null )              // scale was specified
          new_data.resample( new_Q_scale, IData.SMOOTH_NONE );  

        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerTofToQ Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerTofToQ new_op = new SpectrometerTofToQ( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
