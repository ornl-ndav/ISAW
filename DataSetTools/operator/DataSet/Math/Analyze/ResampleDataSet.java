/*
 * File:  ResampleDataSet.java 
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
 *  Revision 1.6  2004/01/24 19:35:08  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.5  2002/11/27 23:18:38  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/11/26 20:41:04  dennis
 *  Added getDocumentation() method and simple main program.(Mike Miller)
 *
 *  Revision 1.3  2002/09/19 16:01:57  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/03/13 16:19:17  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.1  2002/02/22 21:02:32  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
 * This operator will resample the Data blocks of a DataSet on a uniformly 
 * spaced grid.  If the Data blocks are histograms, a rebinning process is 
 * used.  If the Data blocks are tabulated functions, averaging and 
 * interpolation are used.
 */

public class ResampleDataSet extends AnalyzeOp 
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

  public ResampleDataSet( )
  {
    super( "Resample" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_X       The left hand end point of the interval over which
   *                      the function is to be resampled 
   *  @param  max_X       The right hand end point of the interval over which
   *                      the function is to be resampled 
   *  @param  num_X       For histogram Data, this specifies the number of 
   *                      "bins" to be used between min_X and max_X.  For 
   *                      Tabulated functions, this specifies the number of
   *                      sample points to use.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the original
   *                      DataSet are just altered.
   */

  public ResampleDataSet( DataSet     ds,
                          float       min_X,
                          float       max_X,
                          int         num_X,
                          boolean     make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_X ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_X ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_X ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

/* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns a string of the description/attributes of ResampleDataSet
  *   for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator resamples a dataset. Resampling ");
    Res.append("is dependent on whether the dataset was newly \n");
    Res.append("constructed or just the data blocks in the dataset \n");
    Res.append("were altered.\n");
    Res.append("@algorithm Given a dataset, a new copy of the dataset ");
    Res.append("will be created and resampled(boolean = true), or the ");
    Res.append("old dataset will be resampled and altered ");
    Res.append("(boolean = false).\n");
    Res.append("@param ds\n");
    Res.append("@param min_X\n");
    Res.append("@param max_X\n");
    Res.append("@param num_X\n");
    Res.append("@param make_new_ds\n");
    Res.append("@return If make_new_ds_flag = true, return new dataset. ");
    Res.append("If make_new_ds_flag = false, return confirmation "); 
    Res.append("message that original dataset was altered.\n");
    Res.append("@error Invalid X interval\n");    
    
    return Res.toString();
    
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
@return	the command name to be used with script processor: in this case, Resample
   */
   public String getCommand()
   {
     return "Resample";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    parameter = new Parameter( "Min X", new Float(0) );
    addParameter( parameter );

    parameter = new Parameter("Max X", new Float(1000) );
    addParameter( parameter );

    parameter = new Parameter( "Num X", new Integer( 200 ) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();

                                     // get the new x scale parameters
    float min_X         = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_X         = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_X         = ( (Integer)(getParameter(2).getValue()) ).intValue();
    boolean make_new_ds = ((Boolean)getParameter(3).getValue()).booleanValue();

                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Resampled" );

                                     // validate interval bounds
    if ( min_X > max_X )             // swap bounds to be in proper order
    {
      float temp = min_X;
      min_X = max_X;
      max_X = temp;
    }

    UniformXScale new_x_scale;
    if ( num_X < 2 || min_X >= max_X )      // no valid scale set
    {
      return new ErrorString("ERROR: invalid interval in ResampleDataSet");
    }
    else
      new_x_scale = new UniformXScale( min_X, max_X, num_X );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    int              num_data = ds.getNum_entries();

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry

      if ( make_new_ds )
      {
        new_data = (Data)data.clone();
        new_data.resample( new_x_scale, IData.SMOOTH_NONE );
        new_ds.addData_entry( new_data );
      }
      else
        data.resample( new_x_scale, IData.SMOOTH_NONE );
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Data resampled uniformly" );
    }

  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current ResampleDataSet Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    ResampleDataSet new_op = new ResampleDataSet( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

/* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will compile and run ok.  
  *
  */
  
  public static void main( String args[] )
  {

     System.out.println("Test of ResampleDataSet starting...");
     DataSet ds = DataSetFactory.getTestDataSet();
     
     ResampleDataSet testgroup = 
       			new ResampleDataSet(ds, 1, 10, 10, true);   
     System.out.println("New Dataset: " + testgroup.getResult() );
     
     System.out.println("Raw Help Info: " + testgroup.getDocumentation() );
    
     System.out.println("Test of ResampleDataSet done.");   
  }

}
