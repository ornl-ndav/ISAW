/*
 * File:  DataValueAccessMethods.java
 *
 * Copyright (C) 2006, Dennis Mikkelson 
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2006/07/19 17:49:20  dennis
 * Fixed minor javadoc error.
 *
 * Revision 1.2  2006/07/17 03:19:41  dennis
 * Now adds message to the Operations log if the values are set.
 * Cast vector entries to Number, rather than Float, so
 * setValue() will work with arbitrary Vectors of numbers,
 * not just Floats.
 * Fixed javadoc on setValue() method.
 *
 * Revision 1.1  2006/07/14 22:19:58  dennis
 * Class with utility methods for getting X, Y and Error
 * values from a Data block, and for setting the Y and Error
 * values.
 *
 */
package Operators.Generic;

import java.util.Vector;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;


/**
 *  This class contains some basic methods for accessing the data values
 *  and errors stored in a Data block of a DataSet.  These methods are
 *  wrapped by operators getValues and setValues.
 */
public class DataValueAccessMethods
{

  /**
   *  Get a list of X-values, Y-values or errors from the specified Data block
   *  in the specified DataSet.  If X-values are obtained, they will start
   *  with the last X-value in the XScale that is less than or equal to the 
   *  requested min_x and end with the first X-value in the XScale that is 
   *  greater than or equal to the requested max_x.  If Y-values or errors
   *  are obtained, they will correspond to the X-values as follows.  If
   *  the Data block is a histogram, the Y-values or errors will correspond
   *  to the histogram values between the bin boundaries given by the X-values.
   *  If the Data block is a function, the Y-values or errors will correspond
   *  to the values at the X-values.
   *
   *  @param  ds          The DataSet from which the values will be obtained.
   *  @param  index       The index of the Data block from which the values
   *                      will be obtained.
   *  @param  min_x       The requested minimum X value. 
   *  @param  max_x       The requested maximum X value. 
   *  @param  which_vals  String "X", "Y" or "Error" specifying which values
   *                      are to be obtained.
   *
   *  @return a Vector containing the requested list of values.
   */
  public static Vector getValues( DataSet ds, 
                                  int     index, 
                                  float   min_x, 
                                  float   max_x,
                                  String  which_vals )
  {
    int num_entries = ds.getNum_entries() - 1;

    if ( index < 0 || index > num_entries-1 )
      throw new IllegalArgumentException(
        "Data block index " + index + " out of range 0 to " + (num_entries-1) +
        " in getXvalues()" );

    IData  data_block = ds.getData_entry( index );
    XScale xscale     = data_block.getX_scale();

    int min_index = xscale.getI_GLB( min_x );
    if ( min_index < 0 )
      min_index = 0;

    int max_index = xscale.getI( max_x );
    if ( max_index >= xscale.getNum_x() )
      max_index = xscale.getNum_x() - 1;

    if ( (data_block.isHistogram() && (min_index >= max_index)) ||
        (!data_block.isHistogram() && (min_index >  max_index)) )
      throw new IllegalArgumentException("Degenerate X range in getValues()");
   
    int num_x = max_index - min_index + 1;

                                            // getting Xs so get all Xs in range
    if ( which_vals.equalsIgnoreCase( "X" ) )
    {
      Vector x_vec = new Vector( num_x );

      float xs[] = xscale.getXs();
      for ( int i = min_index; i <= max_index; i++ )
        x_vec.add( new Float( xs[i] ) );
  
      return x_vec;
    }
                                            // otherwise, we're getting Ys or 
                                            // Errors, so we may need to
    if ( data_block.isHistogram() )         // adjust the last index and 
    {                                       // number of values;
      max_index = max_index - 1;
      num_x = num_x - 1;
    }

    Vector vec = new Vector( num_x );
      
    float[] vals = null;

    if ( which_vals.equalsIgnoreCase("Y") )
      vals = data_block.getY_values();

    else if ( which_vals.equalsIgnoreCase("Error") )
      vals = data_block.getErrors();

    else
      throw new IllegalArgumentException(
                          "Must specify 'X', 'Y' or 'Error' in getValues()"); 

    for ( int i = min_index; i <= max_index; i++ )
      vec.add( new Float( vals[i] ) );

    return vec;
  }


  /**
   *  Set a list of Y-values or errors from the specified Data block
   *  into the specified DataSet.  The specified min_x value will be 
   *  mapped to the last X in the XScale that is less than or equal to
   *  the specified X-value.  If the specified min_x is less than the 
   *  smallest value in the XScale, then it will be mapped to the smallest
   *  value in the XScale.  NOTE: The X-values in the XScale cannot be set.
   *
   *    When the Y-values or errors are set, they will correspond to the 
   *  X-values as follows.  If the Data block is a histogram, the Kth Y-value 
   *  or error will correspond to the histogram value or error for the 
   *  bin that is K bins to the right of the specified X-value.  If the Data
   *  block is a function, the Kth Y-value will correspond to the X-value that
   *  is K steps to the right of the specified X-value.  In either case,
   *  a contiguous block of values will be assigned, starting with the value
   *  corresponding to the specified X-value.
   *
   *  @param  ds          The DataSet into which the values will be set.
   *  @param  index       The index of the Data block into which the values
   *                      will be set.
   *  @param  min_x       The requested minimum X value. 
   *  @param  new_vals    Vector of float specifying a Contiguous list of 
   *                      values to be assigned to this Data block, starting
   *                      with the value corresponding to the specified min_x. 
   *  @param  which_vals  String "Y" or "Error" specifying which values
   *                      are to be set.
   *
   */
  public static String setValues( DataSet ds,
                                  int     index,
                                  float   min_x,
                                  Vector  new_vals,
                                  String  which_vals )
  {
    int num_entries = ds.getNum_entries() - 1;

    if ( index < 0 || index > num_entries-1 )
      throw new IllegalArgumentException(
        "Data block index " + index + " out of range 0 to " + (num_entries-1) +
        " in getXvalues()" );

    TabulatedData data_block = (TabulatedData)ds.getData_entry( index );
    XScale        xscale     = data_block.getX_scale();

    int min_index = xscale.getI_GLB( min_x );
    if ( min_index < 0 )
      min_index = 0;

    float[] vals = null;

    if ( which_vals.equalsIgnoreCase("Y") )
      vals = data_block.getY_values();           // Note: this is a reference
                                                 // to the array of Y-values

    else if ( which_vals.equalsIgnoreCase("Error") )
      vals = data_block.getErrors();             // This may be a list
                                                 // of calculated errors
    else
      throw new IllegalArgumentException(
                          "Must specify 'Y' or 'Error' in setValues()");

    for ( int i = 0; i < new_vals.size(); i++ )
      vals[ min_index + i ] = ((Number)(new_vals.elementAt(i))).floatValue(); 

    int n_set = new_vals.size();
    if ( which_vals.equalsIgnoreCase("Error") )
    {
      data_block.setErrors( vals );
      ds.addLog_entry("" + n_set +
                       " errors set in Data block " + index +
                       " starting at X = " + min_x ); 
      return "Error estimates set";
    }
    else
    {
      ds.addLog_entry("" + n_set +
                       " Y-values set in Data block " + index +
                       " starting at X = " + min_x ); 
      return "Y-Values set";                     // no need to set Y-values,
    }                                            // since we got a reference
  }


  /**
   *  This main method just has a basic functionality test for the 
   *  methods of this class.
   */

  public static void main( String args[] )
  {
    String dir  = "/usr2/ARGONNE_DATA/";
    String file = "gppd12358.run";

    RunfileRetriever rr = new RunfileRetriever( dir + file );
    DataSet ds = rr.getDataSet(1);

    Vector Xs = getValues( ds, 58, 14491, 14578, "X" );
    Vector Ys = getValues( ds, 58, 14491, 14578, "Y" );
    Vector Es = getValues( ds, 58, 14491, 14578, "Error" );

    System.out.println("X size = " + Xs.size() );
    System.out.println("Y size = " + Ys.size() );
    System.out.println("E size = " + Es.size() );

    for ( int i = 0; i < Ys.size(); i++ )
      System.out.println("" + Xs.elementAt(i) + ", " +
                              Ys.elementAt(i) + ", " +
                              Es.elementAt(i) );

    for ( int i = 1; i < Ys.size()-1; i++ )
    {
      Ys.setElementAt( new Float(10*i + 0.1f), i );
      Es.setElementAt( new Float(i + 0.2f), i );
    }

    setValues( ds, 58, 14491, Ys, "Y" );
    setValues( ds, 58, 14491, Es, "Error" );

    System.out.println();
    System.out.println("After Setting the intermediate values to a Ramp");
    System.out.println();
    Xs = getValues( ds, 58, 14491, 14578, "X" );
    Ys = getValues( ds, 58, 14491, 14578, "Y" );
    Es = getValues( ds, 58, 14491, 14578, "Error" );

    System.out.println("X size = " + Xs.size() );
    System.out.println("Y size = " + Ys.size() );
    System.out.println("E size = " + Es.size() );

    for ( int i = 0; i < Ys.size(); i++ )
      System.out.println("" + Xs.elementAt(i) + ", " +
                              Ys.elementAt(i) + ", " +
                              Es.elementAt(i) );
  }

}
