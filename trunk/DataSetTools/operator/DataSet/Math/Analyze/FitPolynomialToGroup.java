/*
 * File:  FitPolynomialToGroup.java 
 *             
 * Copyright (C) 2000, Dennis Mikkelson
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
 * $Log$
 * Revision 1.3  2002/09/19 16:01:55  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/03/13 16:19:17  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/02/22 21:02:30  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.3  2001/06/01 21:18:00  rmikk
 * Improved documentation for getCommand() method
 *
 * Revision 1.2  2001/04/26 19:09:17  dennis
 * Added copyright and GPL info at the start of the file.
 *
 * Revision 1.1  2000/12/07 22:36:58  dennis
 * Operator to replace or extend a Data block with a least squares polynomial
 *
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  *  This operator fits a polynomial to the data values of one Data block over
  *  a specified domain.  A new DataSet is produced that contains a new Data
  *  block that is a modified version of the original Data block.  The modified
  *  Data block has the fitted polynomial values over a specified x interval
  *  and the original Data block values over the rest of the original domain
  *  of the original Data block.  The polynomial calculated is the least
  *  squares approximation of the specified degree. 
  *  
  */

public class  FitPolynomialToGroup  extends    AnalyzeOp 
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

  public FitPolynomialToGroup( )
  {
    super( "Fit polynomial to a Group" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  group_id    The group_id of the Data block that to which a  
   *                      polynomial is to be fit
   *  @param  a           Left hand endpoint of interval [a,b] used to 
   *                      calculate the least squares approximation 
   *  @param  b           Right hand endpoint of interval [a,b] used to 
   *                      calculate the least squares approximation 
   *  @param  degree      The degree of the polynomial to use
   *  @param  new_a       Left hand endpoint of interval [new_a, new_b] where
   *                      the least squares polynomial is to be evaluated.
   *  @param  new_b       Right hand endpoint of interval [new_a, new_b] where
   *                      the least squares polynomial is to be evaluated.
   *  @param  n_points    Number of evenly spaced points on [new_a, new_b] where
   *                      the polynomial is evaluated.  If n_points == 0, 
   *                      The number of evaluation points used will be chosen 
   *                      to match the orignal Data block's sampling rate. 
   */

  public FitPolynomialToGroup( DataSet      ds,
                         int          group_id,
                         float        a,
                         float        b,
                         int          degree,
                         float        new_a,
                         float        new_b,
                         int          n_points  )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter = getParameter(0);
    parameter.setValue( new Integer( group_id ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( a ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( b ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Integer( degree ) );

    parameter = getParameter( 4 );
    parameter.setValue( new Float( new_a ) );

    parameter = getParameter( 5 );
    parameter.setValue( new Float( new_b ) );

    parameter = getParameter( 6 );
    parameter.setValue( new Integer( n_points ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, FitPoly
   */
   public String getCommand()
   {
     return "FitPoly";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group ID to fit polynomial to",
                                         new Integer(0));
    addParameter( parameter );

    parameter = new Parameter("Start of interval to use for the fit", 
                               new Float(0));
    addParameter( parameter );

    parameter = new Parameter("End of interval to use for the fit", 
                               new Float(1000));
    addParameter( parameter );

    parameter = new Parameter("Degree of polynomial to fit", new Integer(1));
    addParameter( parameter );

    parameter = new Parameter("Start of interval where fit is used",
                               new Float(0));
    addParameter( parameter );

    parameter = new Parameter("End of interval where fit is used",
                               new Float(1000));
    addParameter( parameter );

    parameter = new Parameter("Number of points where fit is evaluated",
                               new Integer(0));
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    int group_id   = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float a        = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float b        = ( (Float)(getParameter(2).getValue()) ).floatValue();
    int   degree   = ( (Integer)(getParameter(3).getValue()) ).intValue();
    float new_a    = ( (Float)(getParameter(4).getValue()) ).floatValue();
    float new_b    = ( (Float)(getParameter(5).getValue()) ).floatValue();
    int   n_points = ( (Integer)(getParameter(6).getValue()) ).intValue();

                                     // get the current data set and do the 
                                     // operation
    DataSet ds = this.getDataSet();

    Data data = ds.getData_entry_with_id( group_id );
    if ( data == null )
    {
      ErrorString message = new ErrorString( 
                           "ERROR: no data entry with the group_ID "+group_id );
      System.out.println( message );
      return message;
    }

    boolean is_histogram = data.isHistogram();

                                             // get the data we're approximating
    float x_vals[] = data.getX_scale().getXs();
    float y_vals[] = data.getY_values();
                                             // get valid indices for the points
                                             // to use to find approximation 
    if ( a >= b || b < x_vals[0] || a > x_vals[ x_vals.length-1 ] ) 
    {
      ErrorString message = new ErrorString(
                       "ERROR: interval invalid, [a,b] = [" + a + "," + b +"]");
      System.out.println( message );
      return message;
    }
    int first_index = arrayUtil.get_index_of( a, x_vals );
    if ( first_index < 0 )
      first_index = 0;

    int last_index  = arrayUtil.get_index_of( b, x_vals );
    if ( last_index < 0 )
      last_index = x_vals.length-1;

    if ( last_index == -1 || first_index == last_index )
      return new ErrorString("ERROR: no Data points in specified interval");

                                            // Now extract the x values to
                                            // use for the curve fitting
    double x[] = null;
    int n_samples = 0;
    if ( is_histogram )                        // histogram, so treat the
    {                                          // y values as being at bin 
                                               // centers
      n_samples = last_index - first_index;
      if ( n_samples <= degree )
        return new 
               ErrorString("ERROR: not enough data points to fit polynomial");

      x = new double[ n_samples ];
      for ( int i = 0; i < n_samples; i++ )
        x[i] = (x_vals[ first_index+i ] + x_vals[ first_index+i+1]) / 2.0;
    }
    else                                       // tabulated function, so
    {                                          // just use the data points 
      n_samples = last_index - first_index + 1;
      if ( n_samples <= degree )
        return new 
               ErrorString("ERROR: not enough data points to fit polynomial");
        
      x = new double[ n_samples ];
      for ( int i = 0; i < n_samples; i++ )
        x[i] = x_vals[ first_index+i ];
    }
                                                // Next, get the y values
    double y[] = new double[ n_samples ];
    for ( int i = 0; i < n_samples; i++ )
      y[i] = y_vals[ first_index+i ];
                                                // Then, do the curve fitting
    double coeff[] = new double [ degree + 1 ];
    double error   = CurveFit.Polynomial( x, y, coeff );
    if ( error == Double.NaN )
      return new ErrorString("ERROR: couldn't fit data with polynomial");

                                        // Now evaluate the fitted curve at
                                        // a set of points and put the result
                                        // in a DataSet.

    if ( n_points == 0 )                // fabricate a default number of points
    {                                   // with similar average delta_x, and
                                        // align the endpoints with the original
                                        // left hand endpoint.
      XScale scale   = data.getX_scale();
      float  start   = scale.getStart_x();
      double delta_x = (scale.getEnd_x()-start) / (scale.getNum_x()-1); 

      int n_steps = (int)Math.round((new_a - start)/delta_x);
      new_a       = (float)(start + n_steps * delta_x);

      n_steps = (int)Math.round((new_b - start)/delta_x);
      new_b   = (float)(start + n_steps * delta_x);

      n_points = (int)Math.round(( new_b - new_a )/delta_x ) + 1;
    }

    XScale new_x_scale = new UniformXScale( new_a, new_b, n_points );
    float  x_val[] = new_x_scale.getXs();
    float  p_val[] = null;
    if ( is_histogram )
      p_val = new float[ n_points - 1 ];          // one sample per bin
    else
      p_val = new float[ n_points ];              // one sample per x-value
     
    for ( int i = 0; i < p_val.length; i++ )      // evaluate the polynomial
    {                                             // at each point
      double p;
      double x_n,
             x_0;
      double val; 
      val = 0;
      if ( is_histogram )                         // use bin center
        x_0 = (x_val[i]+x_val[i+1])/2;
      else                                        // use x_scale point
        x_0 = x_val[i]; 
      x_n = 1.0;
      for ( int j = 0; j < coeff.length; j++ )
      {
        val += coeff[j] * x_n;
        x_n *= x_0;
      }
      p_val[i] = (float)val;
    }
                                                  // make the new Data block
                                                  // and "stitch" it together
                                                  // with the original Data
    Data new_data = Data.getInstance( new_x_scale, p_val, 111111 );
    new_data = data.stitch( new_data, IData.DISCARD );

//    new_data.print(0,200);

    DataSet new_ds = ds.empty_clone();
    new_ds.addData_entry( new_data );

    return new_ds;  
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current FitPolynomialToGroup Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    FitPolynomialToGroup new_op = new FitPolynomialToGroup( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
