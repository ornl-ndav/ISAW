/*
 * File:  DS_Util.java
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
 *  $Log$
 *  Revision 1.8  2002/07/15 19:35:56  dennis
 *  getData_ID_String() Now uses Data getLabel() method to form part
 *  of the ID String.  The ID String now consists of:
 *  1. the ID.
 *  2. the time stamp if set
 *  3. the label from Data.getLabel() if set
 *  4. the word (Selected) if it's selected.
 *
 *  Revision 1.7  2002/02/22 20:38:17  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.6  2001/07/23 16:39:26  dennis
 *  Fixed error: no longer using "==" for String comparison.
 *
 *  Revision 1.5  2001/07/02 16:43:59  dennis
 *  getData_ID_String() now gets an entire attribute and
 *  uses it's toString method.  This fixed a bug when the
 *  attribute value was an array of integers.
 *
 *  Revision 1.4  2001/06/04 22:42:24  dennis
 *  Added getData_ID_String() to construct border labels for the
 *  graphs in ImageView and GraphView
 *
 *  Revision 1.3  2001/04/26 14:38:57  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.2  2000/11/10 22:49:19  dennis
 *  Changed to use new Operator hierarchy.
 *
 *  Revision 1.1  2000/07/10 23:04:18  dennis
 *  Now Uses CVS 
 *
 *  Revision 1.1  2000/05/16 22:27:07  dennis
 *  Initial revision
 *
 */

package DataSetTools.viewer.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
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
      if ( op.getCategory().equals( Operator.X_AXIS_CONVERSION ))
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
      if ( op.getCategory().equals( Operator.X_AXIS_CONVERSION ))
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


  /**
   *  Get an identifing string for a Data block in a DataSet, based on the
   *  last sort that was performed.
   *
   *  @param ds    The DataSet containing the Data block to identify
   *  @param index The index of the Data block in the DataSet
   *
   *  @return An identifying string for the specified Data block. 
   */
  public static String getData_ID_String( DataSet ds, int index )
  {
    if ( index < 0 || index >= ds.getNum_entries() )
      return "ID_";

    Data data = ds.getData_entry( index );

    String s = "ID_" + data.getGroup_ID();

    String time = (String)(data.getAttributeValue(Attribute.UPDATE_TIME));
    if ( time != null )
      s += ", " + time;

    String label = data.getLabel();
    if ( label != null && ! label.startsWith(Attribute.GROUP_ID) )
      s += ", " + label;
   
    if ( data.isSelected() )
      s += " (Selected)";

    return s;
  }
  

} 
