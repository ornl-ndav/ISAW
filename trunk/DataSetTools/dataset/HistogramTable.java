/*
 * File: HistogramTable.java 
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
 *  Revision 1.1  2002/03/13 16:08:35  dennis
 *  Data class is now an abstract base class that implements IData
 *  interface. FunctionTable and HistogramTable are concrete derived
 *  classes for storing tabulated functions and frequency histograms
 *  respectively.
 *
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

/**
 * The concrete root class for a tabulated frequency histogram data object.  
 * This class bundles together the basic data necessary to describe a frequency
 * histogram.  An object of this class contains a list of "X" values
 * and a list of "Y" values together with an extensible list of attributes
 * for the object.  A list of errors for the "Y" values can also be kept.
 *  
 * @see DataSetTools.dataset.IData
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.TabulatedData
 * @see DataSetTools.dataset.FunctionTable
 *
 */

public class HistogramTable extends    TabulatedData
                            implements Serializable
{
  /**
   * Constructs a Data object containing a table of frequency histogram values 
   * specifying an "X" scale, "Y" values and a group id for that data object.
   * The X scale specifies bin boundaries and should have one more value than 
   * the list of Y values.  If too many Y values are given, the extra values 
   * will be ignored.  If too few Y values are given, the saved Y values will 
   * be padded with trailing zero values.
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this data object 
   * @param   group_id  an integer id for this data object
   *
   */
  public HistogramTable( XScale x_scale, float y_values[], int group_id )
  {
    super( x_scale, y_values, group_id );
    init( y_values );  
  }

  /**
   * Constructs a Data object by specifying an "X" scale, 
   * "Y" values and an array of error values.
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this data object 
   * @param   errors    the list of error values for this data object.  The
   *                    length of the error list should be the same as the
   *                    length of the list of y_values.  
   * @param   group_id  an integer id for this data object
   *
   */
  public HistogramTable( XScale  x_scale, 
                         float   y_values[], 
                         float   errors[], 
                         int     group_id )
  {
    super( x_scale, y_values, group_id );
    init( y_values );  
    this.setErrors( errors );
  }


  /**
   * Constructs a HistogramTable Data object from another Data object.
   *
   *  @param  width_1   Width of the first bin.  For VariableXScales, this is
   *                    is used to determine the bin boundaries.  NOTE: this
   *                    process is subject to rounding errors and should be
   *                    avoided if possible.  If width_1 is less than or equal
   *                    to zero, the distance between the first two x values
   *                    will be used as a default.
   *
   *  @param  multiply  Flag that indicates whether the function values
   *                    should be multiplied by the width of the histogram bin.
   * @param   group_id  an integer id for this data object
   *
   */
  public HistogramTable( Data d, boolean multiply, int group_id )
  {
    super( d.x_scale, null, group_id );

    init( d.getY_values() );
    this.setErrors( d.getErrors() );

    if ( !d.isHistogram() )                  // we need to convert to histogram 
    {
      float temp_x_values[] = d.getX_values();
      int n_bins = temp_x_values.length;

      float new_x_values[]  = new float[ n_bins + 1 ];
      new_x_values[0] = temp_x_values[0] 
                            - (temp_x_values[1] - temp_x_values[0]) / 2; 
      for ( int i = 1; i < n_bins; i++ )
        new_x_values[i] = (temp_x_values[i-1] + temp_x_values[i]) / 2.0f;
      new_x_values[n_bins] = temp_x_values[n_bins-1] 
                      + (temp_x_values[n_bins-1] - temp_x_values[n_bins-2]) / 2; 
      x_scale = new VariableXScale( new_x_values );     //###########uniform?

      if ( multiply )
      {
        if ( errors == null )
          for ( int i = 0; i < n_bins; i++ )
          {
            float dx = new_x_values[i+1] - new_x_values[i];
            y_values[i] = y_values[i] * dx;
          }
          else
          for ( int i = 0; i < n_bins; i++ )
          {
            float dx = new_x_values[i+1] - new_x_values[i];
            y_values[i] = y_values[i] * dx;
            errors[i]   = errors[i] * dx;        //############### how should
          }                                      // errors be treated?
       }
    }
  }


  /**
   *  Set the specified array of y_values as the y_values for this Data 
   *  object.  If there are more y_values than there are x_values in the
   *  XScale for this object, the excess y_values are discarded.  If there
   *  are not as many y_values as there are x_values, the new y_values array
   *  is padded with 0s.
   */
  private void init( float y_values[] )
  {
    int n_bins = x_scale.getNum_x() - 1;
    this.y_values = new float[ n_bins ];

    if ( y_values.length >= n_bins )
      System.arraycopy( y_values, 0, this.y_values, 0, n_bins );
    else
    {
      System.arraycopy( y_values, 0, this.y_values, 0, y_values.length );
      for ( int i = y_values.length; i < n_bins; i++ )
        this.y_values[i] = 0;
    }
  }



/**
 *  Get an approximate y value corresponding to the specified x_value in this 
 *  Data block. If the x_value is outside of the interval of x values
 *  for the Data, this returns 0.  In other cases, the approximation used is
 *  controlled by the smooth_flag.  
 *
 *  @param  x_value      the x value for which the corresponding y value is to
 *                       be interpolated
 *
 *  @param  smooth_flag  Currently, for a HistogramTable, the smooth_flag has
 *                       no effect.  The count within the bin containing the
 *                       the specified x is returned.
 *
 *  @return approximate y value at the specified x value
 */
public float getY_value( float x_value, int smooth_flag ) //################
{
  
  if ( x_value < x_scale.getStart_x() || 
       x_value > x_scale.getEnd_x()    )
    return 0.0f;

  float x_vals[] = x_scale.getXs();
  int index = arrayUtil.get_index_of( x_value, x_vals );

  return y_values[index]; 
}


  /**
    * Determine whether or not the current Data block has HISTOGRAM data.
    * HISTOGRAM data records bin boundaries and a number of counts in each
    * bin, so the number of x-values is one more than the number of y-values.
    *
    * @return  true if the number of x-values is one more than the number
    *          of y-values.
    */
  public boolean isHistogram()
  {
    return true;
  }  
 

  /**
   * Return a new Data object containing a copy of the x_scale, y_values
   * errors, group_id and attributes from the current Data object.
   */
  public Object clone()
  {
    Data temp = new HistogramTable( x_scale, y_values, errors, group_id );

                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    temp.setAttributeList( attr_list );
    temp.selected = selected;
    temp.hide     = hide;

    return temp;
  }

  /**
   *    "Stitch" another Data block together with the current Data block to form
   *  a new Data block with the same attributes as the current Data block,
   *  but whose data is a combination of the two.  
   *
   *    The other Data block will first be converted to the same type of
   *  Data ( HISTOGRAM or FUNCTION ) as the current Data block.  Then a new
   *  XScale is formed and the y-values from the two Data blocks are "stitched"
   *  together over the new XScale.  The XScale for the new Data object covers 
   *  an interval containing the union of the current Data's XScale and the 
   *  other Data's XScale.
   *
   *    If the XScale of the current Data block is a uniform XScale, the new  
   *  XScale will be a uniform XScale with the same spacing as the current 
   *  XScale, aligned with the current XScale.  If the XScale of the current
   *  Data block is a VariableXScale, the new XScale will be a VariableXScale
   *  using the same x-values as the current Data's for the full extent of the
   *  current Data's XScale.  In this case, the new VariableXScale will only
   *  use the other_data's x-values for the interval covered by it's XScale
   *  and NOT covered by the current Data's XScale.
   *
   *    The other_data object will be resampled over the new XScale before 
   *  forming the y-values.  For the portion of the new XScale covered by 
   *  only the current Data, the current Data's y-values are used.  For the  
   *  portion of the new XScale covered by only the other Data's XScale, the 
   *  other Data's y-values are used.  The y-values for portions of the 
   *  intervals that overlap are selected from the current Data or the other 
   *  Data's y-values, or the average of the y-values, as determined by the 
   *  "overlap" parameter. 
   *
   *  @param  other_data  The other Data block whose data is to be combined
   *                      with the current data. 
   *  @param  overlap     Flag that indicates what should be done on the
   *                      interval where the two Data blocks overlap ( if any ).
   *                      This must be one of the constants:
   * 
   *                              Data.KEEP 
   *                              Data.AVERAGE 
   *                              Data.DISCARD
   *
   *                      indicating that the original Data's values should be
   *                      kept, averaged with the other Data's values or 
   *                      discarded and the other Data's values used instead.
   *
   *  @return  A new Data object with the same attributes as the current 
   *           Data object, but whose data is a combination of the two.  The
   *           XScale for the new DataObject 
   */
  public Data stitch( Data other_data, int overlap )
  {
                                                      // ##################
    System.out.println("HistogramTable.stitch() NOT IMPLEMENTE YET");
    return new HistogramTable( other_data, false, other_data.getGroup_ID() );
  }



  /**
   *  Resample the Data block on an arbitrarily spaced set of points given by
   *  the new_X scale parameter.  If the Data block is a tabulated function,
   *  the function will just be interpolated at the specified points.  If the 
   *  Data block is a histogram, the histogram will be re-binned to form a 
   *  new histogram with the specified bin sizes.
   *
   *  @param new_X  The x scale giving the set of x values to use for the
   *                 resampling and/or rebinning operation.
   */
  public void resample( XScale new_X, int smooth_flag ) //###################
  {
     ReBin( new_X );
     return;
  }


  /**
   * Alter this Data object by "rebinning" the y values of the current 
   * object to correspond to a new set of x "bins" given by the parameter
   * "new_X".  Also, rebin the error array.
   *
   * @param  new_X    This specifies the new set of "x" values to be used
   */
  public void ReBin( XScale new_X )
  {
                                           // Rebin the y_values
    float old_ys[] = arrayUtil.getPortion( y_values, x_scale.getNum_x() - 1 );
    float new_ys[] = new float[ new_X.getNum_x() - 1 ];

    if ( errors != null )
    {
      float new_errs[] = new float[ new_X.getNum_x() - 1 ];
      Sample.ReBin( x_scale.getXs(), old_ys, errors,
                    new_X.getXs(),   new_ys, new_errs );
      y_values = new_ys;
      errors   = new_errs;
    }
    else
    {
      Sample.ReBin( x_scale.getXs(), old_ys, new_X.getXs(), new_ys );
      y_values = new_ys;
    }

    x_scale  = new_X;
  }


  /**
    * Returns true or false depending on whether the two Data objects are
    * capable of being added, etc., based on the size of their value arrays,
    * and the size and extent of their x_scales.  If the XScales are NOT
    * uniform, each corresponding point of the XScales should probably be
    * compared, however, this is not currently done.  
    *
    *  @param  d     The Data object to be compared with the current data
    *                object.
    */

  public boolean compatible( Data d )
  {
//    System.out.println("y lengths: " + y_values.length+
//                       ", " +        d.y_values.length);
    if ( this.getY_values().length != d.getY_values().length )
      return false;

//    System.out.println("x lengths: " + x_scale.getNum_x()+
//                       ", " +        d.x_scale.getNum_x() );
    if ( this.x_scale.getNum_x() != d.x_scale.getNum_x() )
      return false;

//    System.out.println("Start x: " + x_scale.getStart_x()+
//                       ", " +      d.x_scale.getStart_x() );
    if ( this.x_scale.getStart_x() != d.x_scale.getStart_x() )
      return false;

//    System.out.println("End x: " + x_scale.getEnd_x()+
//                       ", " +      d.x_scale.getEnd_x() );
    if ( this.x_scale.getEnd_x() != d.x_scale.getEnd_x() )
      return false;

    return true;  
  }

  /**
    * Construct a new Data object by ADDING corresponding "y" values of the
    * current Data object and the specified Data object d.  If both the 
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be added (as determined by method "compatible") this method returns null.
    * Also see the documentation for the method "compatible".
    * The attributes for the resulting Data object are a combination of the 
    * attributes of the current Data object and the specified Data object d.
    *
    * The combination is treated differently, depending on whether or not
    * the two data blocks have the same group ID and on what the attribute is.
    * Specifically the treatment of some of the most important attributes is
    * as follows:
    *
    * Same Group ID:
    *   TOTAL_COUNT       summed 
    *   NUMBER_OF_PULSES  summed 
    *   SOLID_ANGLE       averaged
    *   RAW_ANGLE         averaged
    *   DELTA_TWO_THETA   averaged
    *   DETECTOR_POS      averaged
    *   DETECTOR_POS      average, weighted by SOLID_ANGLES if present 
    *
    * Different Group ID:
    *   TOTAL_COUNT       summed 
    *   NUMBER_OF_PULSES  averaged 
    *   SOLID_ANGLE       summed 
    *   RAW_ANGLE         keep raw angle of current Data object 
    *   DELTA_TWO_THETA   max of current value or difference of RAW_ANGLEs 
    *   DETECTOR_POS      average, weighted by SOLID_ANGLES if present 
    *
    * @param   other_d   The Data object to be added to the current data object
    *
    */

  public Data add( Data other_d )
  {
    HistogramTable d = null;

    if ( other_d instanceof HistogramTable )   
      d = (HistogramTable)other_d;
    else  
      d = new HistogramTable(other_d, true, other_d.getGroup_ID() );

    if ( ! this.compatible( d ) )       
    {
      d = (HistogramTable)d.clone();        // make a clone and resample it
      d.resample( x_scale, SMOOTH_LINEAR ); // to match the current Data block
    }

    HistogramTable temp = (HistogramTable)this.clone();   // ###############

    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] += d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( this.errors[i] * this.errors[i] +
                                               d.errors[i] *    d.errors[i] ); 
    else
      temp.errors = null;
                                         // now take care of attributes... most
                                         // will use the default combine method
                                         // but some be treated differently 
    Attribute attr, 
              attr1,
              attr2;
    temp.combineAttributeList( d );
    temp.attr_list.add( Attribute.TOTAL_COUNT,
                        this.getAttributeList(),  
                        d.getAttributeList()    );

    // do weighted sum of DetectorPosition, weighted by solid angle.
    attr1 = this.attr_list.getAttribute(Attribute.DETECTOR_POS);
    attr2 = d.attr_list.getAttribute(Attribute.DETECTOR_POS);
    if ( attr1 != null && attr2 != null )
    {
      DetectorPosition points[] = new DetectorPosition[2];
      points[0] = (DetectorPosition)attr1.getValue();
      points[1] = (DetectorPosition)attr2.getValue();
      attr1 = this.attr_list.getAttribute(Attribute.SOLID_ANGLE);
      attr2 = d.attr_list.getAttribute(Attribute.SOLID_ANGLE);
      if ( attr1 != null && attr2 != null )
      {
        float weights[] = new float[2];
        weights[0] = (float)attr1.getNumericValue();
        weights[1] = (float)attr2.getNumericValue();
        DetectorPosition ave_pos =
                  DetectorPosition.getAveragePosition( points, weights );
        attr = new DetPosAttribute( Attribute.DETECTOR_POS, ave_pos );
        temp.attr_list.setAttribute(attr);
      }
    }
                                         // special cases for adding same 
                                         // groups from different runs, or
                                         // different groups ( same run? )
    if ( this.group_id == d.group_id )               
      temp.attr_list.add( Attribute.NUMBER_OF_PULSES,
                          this.getAttributeList(),  
                          d.getAttributeList()    );
    else
    {
      temp.attr_list.add( Attribute.SOLID_ANGLE,
                          this.getAttributeList(),  
                          d.getAttributeList()    );

                                           // keep raw angle of first Data block
      attr = this.attr_list.getAttribute(Attribute.RAW_ANGLE);
      if ( attr != null )
        temp.attr_list.setAttribute( attr );
                                                    // approximate maximum for
                                                    // delta two theta
      attr1 = this.attr_list.getAttribute(Attribute.RAW_ANGLE);
      attr2 = d.attr_list.getAttribute(Attribute.RAW_ANGLE);
      if ( attr1 != null && attr2 != null )
      {
        float new_delta = (float)Math.abs( attr1.getNumericValue() - 
                                           attr2.getNumericValue() );
        attr = this.attr_list.getAttribute(Attribute.DELTA_2THETA);
        if ( attr != null )
        {
          float delta = (float)attr.getNumericValue(); 
          attr = new FloatAttribute( Attribute.DELTA_2THETA,
                                     Math.max( delta, new_delta ) );
          temp.attr_list.setAttribute(attr);
        }
        else
        {
          attr = new FloatAttribute( Attribute.DELTA_2THETA,
                                     Math.max( new_delta, new_delta ) );
          temp.attr_list.setAttribute(attr);
        }
      }
    }

    return temp; 
  }

  /**
    * Construct a new Data object by SUBTRACTING corresponding "y" values of 
    * the current Data object and the specified Data object d.  If both the 
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be subtracted (as determined by method "compatible") this method 
    * returns null.  The attributes for the resulting Data object are the 
    * same as the attributes of the current Data object.
    * Also see the documentation for the method "compatible".
    *
    * @param   other_d   The Data object to be subtracted from the current data 
    *                    object
    *
    */

  public Data subtract( Data other_d )
  {
    HistogramTable d = null;

    if ( other_d instanceof HistogramTable )
      d = (HistogramTable)other_d;
    else
      d = new HistogramTable(other_d, true, other_d.getGroup_ID() );

    if ( ! this.compatible( d ) )       
    {
      d = (HistogramTable)d.clone();        // make a clone and resample it
      d.resample( x_scale, SMOOTH_LINEAR ); // to match the current Data block
    }

    HistogramTable temp = (HistogramTable)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] -= d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( this.errors[i] * this.errors[i] +
                                               d.errors[i] *    d.errors[i] ); 
    else
      temp.errors = null;

    return temp; 
  }

  /**
    * Construct a new Data object by MULTIPLYING corresponding "y" values of 
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be multiplied (as determined by method "compatible") this method 
    * returns null.  The attributes for the resulting Data object are the
    * same as the attributes of the current Data object.
    * Also see the documentation for the method "compatible".
    *
    * @param   other_d   The Data object to be multiplied times the current data
    *                    object
    */

  public Data multiply( Data other_d )
  {
    HistogramTable d = null;

    if ( other_d instanceof HistogramTable )
      d = (HistogramTable)other_d;
    else
      d = new HistogramTable(other_d, true, other_d.getGroup_ID() );

    if ( ! this.compatible( d ) )       
    {
      d = (HistogramTable)d.clone();        // make a clone and resample it
      d.resample( x_scale, SMOOTH_LINEAR ); // to match the current Data block
    }

    HistogramTable temp = (HistogramTable)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] *= d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( 
              this.errors[i] * d.y_values[i] * this.errors[i] * d.y_values[i] + 
              d.errors[i] * this.y_values[i] * d.errors[i] * this.y_values[i] ); 
    else
      temp.errors = null;

    return temp; 
  }

  /**
    * Construct a new Data object by DIVIDING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be divided (as determined by method "compatible") this method
    * returns null.  The attributes for the resulting Data object are the
    * same as the attributes of the current Data object.
    * Also see the documentation for the method "compatible".
    *
    * @param   other_d  The Data object to be divided into the current data
    *                   object
    */

  public Data divide( Data other_d )
  {
    HistogramTable d = null;

    if ( other_d instanceof HistogramTable )
      d = (HistogramTable)other_d;
    else
      d = new HistogramTable(other_d, true, other_d.getGroup_ID() );

    if ( ! this.compatible( d ) )       
    {
      d = (HistogramTable)d.clone();        // make a clone and resample it
      d.resample( x_scale, SMOOTH_LINEAR ); // to match the current Data block
    }

    HistogramTable temp = (HistogramTable)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      if ( d.y_values[i] > 0.01 )                           // D.M. 6/7/2000
        temp.y_values[i] /= d.y_values[i];
      else
        temp.y_values[i] = 0;
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = temp.y_values[i] * (float) Math.sqrt( 
          this.errors[i] / this.y_values[i] * this.errors[i] / this.y_values[i]+
          d.errors[i] / d.y_values[i] * d.errors[i] / d.y_values[i] ); 
    else
      temp.errors = null;

    return temp;
  }


  public static void main( String argv[] )
  {
  }

}
