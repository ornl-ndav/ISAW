/*
 * @(#)DataSetTools.viewer.util.DS_Util 0.1  2000/05/15  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.1  2000/07/10 23:04:18  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.1  2000/05/16 22:27:07  dennis
 *  Initial revision
 *
 *
 */

package DataSetTools.viewer.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 *  The DS_Util class implements static methods that manipulate DataSeta
 *  and are required by the viewers.
 */   

public final class DS_Util implements Serializable
{

  /**
   *  Don't let anyone instantiate this class
   */
   private DS_Util(){}

  /**
   *  Set the number of x bins to be used in each X-axis conversion operator
   *  in this DataSet.  
   *
   *  @param  ds         The DataSet in which the number of bins for the 
   *                     conversion operators are to be set.
   *
   *  @param  num_bins   The number of bins to use for the conversion operators.
   */
  public static void SetNumXConversionBins( DataSet ds, int num_bins ) 
  {
    if ( ds == null  || num_bins < 1 )
      return;

    DataSetOperator  op;
    int              n_params;
    Parameter        param;

    int n_ops         = ds.getNum_operators();
    for ( int i = 0; i < n_ops; i++ )
    {
      op = ds.getOperator(i);
      if ( op.getCategory() == DataSetOperator.X_AXIS_CONVERSION )
      {
        n_params = op.getNum_parameters();
        for ( int j = 0; j < n_params; j++ )
        {
          param = op.getParameter( j );
          if ( param.getName() == Parameter.NUM_BINS ) 
          {
            param.setValue( new Integer(num_bins) );
            op.setParameter( param, j );
          }
        }
      }
    }
  }

  /**
   *  Get the max number of x bins currently specified for the X-axis 
   *  conversion operators in this DataSet.
   *
   *  @param  ds         The DataSet for which the max number of bins used by
   *                     it's conversion operators is to be obtained.
   */
  public static int GetMaxNumXConversionBins( DataSet ds )
  {
    if ( ds == null )
    { 
      System.out.println("ERROR: ds null in GetMaxNumXConversionBins"); 
      return -1;
    }

    DataSetOperator  op;
    int              n_params;
    Parameter        param;
    int              max_bins = -2;
    int              n_bins;

    int n_ops         = ds.getNum_operators();
    for ( int i = 0; i < n_ops; i++ )
    {
      op = ds.getOperator(i);
      if ( op.getCategory() == DataSetOperator.X_AXIS_CONVERSION )
      {
        n_params = op.getNum_parameters();
        for ( int j = 0; j < n_params; j++ )
        {
          param = op.getParameter( j );
          if ( param.getName() == Parameter.NUM_BINS )
          {
            n_bins = ((Integer)param.getValue()).intValue();
            if ( n_bins > max_bins )
              max_bins = n_bins;
          }
        }
      }
    }
    return max_bins;
  }


} 
