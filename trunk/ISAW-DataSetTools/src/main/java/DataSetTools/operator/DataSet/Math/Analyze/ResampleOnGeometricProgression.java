/*
 * File:  ResampleOnGeometricProgression.java 
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2005/11/18 18:38:41  dennis
 *  Operator to resample a DataSet using a new set of "x" values that
 *  form a geometric progression: xk = x0 * r^k.
 *  The required ratio, r, is calculated from specified values for
 *  the initial point, x0, and the length of the first interval.
 *  The division points, xk, are generated up to and including the
 *  first point that equals or exceeds the specified ending point.
 *
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
 * This operator will resample the Data blocks of a DataSet using x-values
 * that form a geometric progression.  The first x value, length of the 
 * first interval, and approximate last value can be specified.  The x-values
 * used for the resampling are calculated as xk=x0*r^k where 
 * r = (x0 + first_interval)/x0.
 *   If the Data blocks are measured histograms, a rebinning process is used to 
 * obtain the resampled histogram values.  If the Data blocks are tabulated
 * functions, interpolation is used.
 */

public class ResampleOnGeometricProgression extends    AnalyzeOp 
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
  public ResampleOnGeometricProgression( )
  {
    super( "ResampleOnGeometricProgression" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds              The DataSet to be resampled 
   *  @param  min_X           The left hand end point of the interval over
   *                          the function is to be resampled 
   *  @param  max_X           The nominal right hand end point of the 
   *                          interval over which the function is to be 
   *                          resampled.  Points will be generated in a 
   *                          geometric progression, up to and including
   *                          the first point that is greater than or equal
   *                          to max_X.
   *  @param  first_interval  This specifies the length of the first 
   *                          subinterval.
   *  @param  make_new_ds     Flag that determines whether a new DataSet is
   *                          constructed, or the Data blocks of the original
   *                          DataSet are just altered.
   */
  public ResampleOnGeometricProgression( DataSet     ds,
                                         float       min_X,
                                         float       max_X,
                                         float       first_interval,
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
    parameter.setValue( new Float( first_interval ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

/* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns a string description of the ResampleOnGeometricProgression
  *  operator for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator resamples a DataSet using a set ");
    Res.append("of x-values that form a geometric progression: xk=x0*r^k.  ");
    Res.append("The resampling can be done ");
    Res.append("in place, by altering the current DataSet, or a new ");
    Res.append("DataSet can be created to hold the resampled vaules ");
    Res.append("without changing the current DataSet. ");
    
    Res.append("@algorithm  Each Data block in the DataSet is resampled ");
    Res.append("using the newly specified x-values.  ");
    Res.append("If a Data block contains a tabulated histogram, the ");
    Res.append("counts of the histogram will be split (or summed) into ");
    Res.append("the newly specified set of bins.  If a Data block ");
    Res.append("contains a table of function values at discrete points, ");
    Res.append("values will be interpolated to obtain new values at the ");
    Res.append("newly specified list of uniformly spaced x-values.  ");
    Res.append("If a Data block contains a function or histogram model ");
    Res.append("or event data, the new set of x-values will be saved ");
    Res.append("to be used when the 'y' values are requested.  ");
    Res.append("If the Create new DataSet option is selected, the current ");
    Res.append("DataSet is not changed but a new copy of the dataset ");
    Res.append("will be created and resampled.  Otherwise the the current ");
    Res.append("dataset will changed by being resampled 'in place'.  ");
    Res.append("If the current DataSet is modified, observers of the ");
    Res.append("DataSet will be notified that the Data was changed.  ");

    Res.append("@param ds   The DataSet to resample.");

    Res.append("@param min_X  The first x-value in the new list of ");
    Res.append("uniformly spaced x-values.  This must be strictly less ");
    Res.append("than the last x-value.");

    Res.append("@param max_X  A 'nominal' last x-value in the new list of ");
    Res.append("uniformly spaced x-values.  This must be strictly greater ");
    Res.append("than the first x-value.  Points of the geometric ");
    Res.append("progression xk=x0^r^k will be calculated starting at ");
    Res.append("x0=min_X, continuing up to and including the first x ");
    Res.append("value that is greater than or equal to the specified max_X.  ");

    Res.append("@param first_interval  The length of the first interval. ");
    Res.append("This must be at large enough so that min_X + first_interval ");
    Res.append("is strictly more the min_X to within the precision of ");
    Res.append("floating point arithmetic.  This length is used to ");
    Res.append("calculate the ratio for the geometric progression as ");
    Res.append("r = (min_X + first_interval)/min_X.");

    Res.append("@param make_new_ds  Flag indicating whether to create ");
    Res.append("a new DataSet for the resampled values, or to change the ");
    Res.append("current DataSet 'in place'.");

    Res.append("@return If make_new_ds_flag = true, return a new dataset. ");
    Res.append("If make_new_ds_flag = false, return a confirmation "); 
    Res.append("message that the original dataset was altered.");

    Res.append("@error Invalid X interval");    
    Res.append("  This is returned if it is not possible to construct ");
    Res.append("a geometric progression using the specified parameters. ");
    
    return Res.toString();
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   *  Get the command name to be used in scripts.
   *
   *  @return  the command name to be used with the script processor: 
   *           in this case, Resample
   */
   public String getCommand()
   {
     return "ResampleOnGeometricProgression";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    parameter = new Parameter( "Min X", new Float(1000) );
    addParameter( parameter );

    parameter = new Parameter( "Max X", new Float(20000) );
    addParameter( parameter );

    parameter = new Parameter( "First interval length", new Float( 2.0f ) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /** 
   *  Resample this DataSet to the UniformXScale specified by the 
   *  paramters. 
   *
   *  @return If make_new_ds is true, this will return the new DataSet.
   *          Otherwise it will return a message indicating whether or not
   *          the DataSet was successfully modified.
   */
  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();

                                     // get the new x scale parameters
    float min_X         = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_X         = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float first_interval= ( (Float)(getParameter(2).getValue()) ).floatValue();
    boolean make_new_ds = ((Boolean)getParameter(3).getValue()).booleanValue();

    GeometricProgressionXScale new_x_scale;
    try 
    {
      new_x_scale = new GeometricProgressionXScale( min_X, 
                                                    max_X, 
                                                    first_interval );
    }
    catch ( Exception e )
    {
       return new ErrorString("ERROR: invalid interval in " +
                             "ResampleOnGeometricProgression");
    }
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Resampled on geometric progression on interval ["
                          + min_X + ", " + max_X + "] with first interval " 
                          + first_interval );

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
   * Get a copy of the current ResampleOnGeometricProgression Operator.
   * The list of parameters and the reference to the DataSet to which it 
   * applies are also copied.
   */
  public Object clone()
  {
    ResampleOnGeometricProgression new_op = 
                                   new ResampleOnGeometricProgression( );
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

     System.out.println("Test of ResampleOnGeometricProgression " +
                        "starting...");

     DataSet ds = DataSetFactory.getTestDataSet();
     
     ResampleOnGeometricProgression testgroup = 
                    new ResampleOnGeometricProgression(ds, 1, 10, 0.1f, true);
     System.out.println("New Dataset: " + testgroup.getResult() );
     
     System.out.println("Raw Help Info: " + testgroup.getDocumentation() );
    
     System.out.println("Test of ResampleOnGeometricProgression done.");   
  }

}
