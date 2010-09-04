/*
 * File:  TofToChannel.java
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
 * $Log$
 * Revision 1.10  2006/07/10 21:28:21  dennis
 * Removed unused imports, after refactoring the PG concept.
 *
 * Revision 1.9  2006/07/10 16:25:54  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.8  2004/03/15 06:10:46  dennis
 * Removed unused import statements.
 *
 * Revision 1.7  2004/03/15 03:28:28  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.6  2004/01/24 19:10:47  bouzekc
 * Removed unused variables from main().  Removed unused imports.
 *
 * Revision 1.5  2003/01/09 17:15:04  dennis
 * Added getDocumentation(), main test program and java docs on getResult()
 * (Chris Bouzek)
 *
 * Revision 1.4  2002/11/27 23:17:04  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 16:00:39  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/03/13 16:19:17  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/02/22 21:00:59  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Util.Numeric.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.viewer.*;
import  DataSetTools.retriever.*;

/**
 * This operator converts neutron time-of-flight DataSet to channel.  The
 * DataSet must contain spectra with an attribute giving the detector position.
 * In addition, it is assumed that the XScale for the spectra represents the
 * time-of-flight from the sample to the detector.
 * 
 *  Basic operations are @see DataSetTools.math.tof_calc
 */

public class TofToChannel extends  XAxisConversionOp
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

  public TofToChannel( )
  {
    super( "Convert to Channel" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_chan    The minimum channel number to be binned
   *  @param  max_chan    The maximum channel number to be binned
   *  @param  n_bins      The number of "bins" to be used between min_chan and
   *                      max_chan.  That is, the data values between the
   *                      min and max channels will be rebinned into the
   *                      specified number of bins.  If num_bins <= 0, the data
   *                      will not be rebinned, but only data values between
   *                      the specified min and max channels will be kept.
   */

  public TofToChannel( DataSet     ds,
                       float       min_chan,
                       float       max_chan,
                       int         n_bins )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter(0);
    parameter.setValue( new Float(min_chan) );

    parameter = getParameter(1);
    parameter.setValue( new Float(max_chan) );

    parameter = getParameter(2);
    parameter.setValue( new Integer(n_bins) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case, ToChan
   */
   public String getCommand()
   {
     return "ToChan";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    UniformXScale scale = getXRange();
    float min_chan,
          max_chan;

    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    if ( scale == null )
    {
      min_chan = 0;
      max_chan = 1000;
    }
    else
    {
      min_chan = scale.getStart_x();
      max_chan = scale.getEnd_x();
    }

    parameter = new Parameter( "Min Channel Number", new Float(min_chan) );
    addParameter( parameter );

    parameter = new Parameter( "Max Channel Number", new Float(max_chan));
    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer(0) );
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
     return new String( "Channel" );
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
    float  channel;

    DataSet ds = this.getDataSet();          // make sure we have a DataSet
    if ( ds == null )
      return Float.NaN;

    int num_data = ds.getNum_entries();      // make sure we have a valid Data
    if ( i < 0 || i >= num_data )            // index
      return Float.NaN;

    Data   data  = ds.getData_entry( i );
    XScale scale = data.getX_scale();
    float  min_x = scale.getStart_x();
    float  max_x = scale.getEnd_x();
    int    num_x = scale.getNum_x();

    if ( num_x < 2 || x < min_x || x > max_x )
      return Float.NaN;

    if ( scale instanceof UniformXScale )               // linear interpolation
    {
      channel = (x - min_x) / (max_x - min_x) * num_x;
      channel = (int)( channel );
    }
    else                                                // use binary search
    {
      float y_vals[] = scale.getXs();
      channel = arrayUtil.get_index_of( x, y_vals );
    }
    return channel;
  }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator converts the X-axis units on a ");
    s.append("DataSet from neutron time-of-flight to channel numbers.\n");
    s.append("@assumptions The DataSet must contain spectra with an ");
    s.append("attribute giving the detector position.  In addition, it ");
    s.append("is assumed that the XScale for the spectra represents the ");
 		s.append("time-of-flight from the sample to the detector. \n");
    s.append("@algorithm Creates a new DataSet which has the same title ");
    s.append("as the input DataSet, the same y-values as the input DataSet, ");
    s.append("and whose X-axis units have been converted to channel ");
    s.append("numbers.  The new DataSet also has a message appended to its ");
    s.append("log indicating that a conversion to channel numbers on ");
    s.append("the X-axis was done.\n");
    s.append("@param ds The DataSet to which the operation is applied.\n");
    s.append("@param min_chan The minimum channel number to be binned.\n");
    s.append("@param max_chan The maximum channel number to be binned.\n");
    s.append("@param n_bins The number of \"bins\" to be used between ");
    s.append("min_chan and max_chan.  That is, the data values between the ");
    s.append("min and max channels will be rebinned into the specified ");
    s.append("number of bins.  If num_bins <= 0, the data will not be ");
    s.append("rebinned, but only data values between the specified min ");
    s.append("and max channels will be kept.\n");
    s.append("@return A new DataSet which is the result of converting the ");
    s.append("input DataSet's X-axis units to channel numbers.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
  	*  Converts the input DataSet to a DataSet which is identical except that
  	*  the new DataSet's X-axis units have been converted to channel numbers.
  	*
		*	 @return DataSet whose X-axis units have been converted to channel
		*  numbers.
		*/

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, but new units, and operations.
    DataSetFactory factory = new DataSetFactory(
                                     ds.getTitle(),
                                     "Channel",
                                     "Number",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet();
    new_ds.removeOperator( new TofToChannel() );  // remove redundant operator
                                                  // that would convert channel
                                                  // to channel
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Channel Number" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

                                     // get the channel scale parameters
    float min_chan = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_chan = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_chan = ( (Integer)(getParameter(2).getValue()) ).intValue();

    min_chan = Math.round( min_chan );
    max_chan = Math.round( max_chan );
                                     // validate channel bounds
    if ( min_chan > max_chan )       // swap bounds to be in proper order
    {
      float temp = min_chan;
      min_chan = max_chan;
      max_chan = temp;
    }

    UniformXScale new_channel_scale;                 // create a new common
                                                     // channel scale, if
                                                     // specified by parameters
    if ( num_chan <= 1.0 || min_chan >= max_chan )
      new_channel_scale = null;                      // no valid scale set
    else
      new_channel_scale = new UniformXScale( min_chan,
                                             max_chan,
                                             num_chan );

                                            // now proceed with the operation
                                            // on each data block in DataSet
    Data             data,
                     new_data;
    float            y_vals[];               // y_values from one spectrum
    float            errors[];               // errors from one spectrum
    XScale           channel_scale;
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );         // get reference to the data entry

      attr_list = data.getAttributeList();  // copy the Data attributes

      y_vals = data.getY_values();
      errors = data.getErrors();
      channel_scale = new UniformXScale( 0,
                                         y_vals.length,
                                         y_vals.length + 1 );

                                                 // create new data block with
                                                 // time-channel XScale and
                                                 // the original y_vals.
      new_data = Data.getInstance( channel_scale,
                                   y_vals,
                                   errors,
                                   data.getGroup_ID() );
      new_data.setAttributeList( attr_list );

                                                 // resample if a valid
      if ( new_channel_scale != null )           // scale was specified
          new_data.resample( new_channel_scale, IData.SMOOTH_NONE );

      new_ds.addData_entry( new_data );
    }

    return new_ds;
  }

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current TofToChannel Operator.  The list
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    TofToChannel new_op = new TofToChannel( );
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
		float min_1 = 0.0f, max_1 = 4000f;
		String file_name =
		"/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
		//"D:\\ISAW\\SampleRuns\\GPPD12358.RUN";

		try
		{
			RunfileRetriever rr = new RunfileRetriever( file_name );
			DataSet ds1 = rr.getDataSet(1);
    	new ViewManager(ds1, IViewManager.IMAGE);
    	TofToChannel op = new TofToChannel(ds1, min_1, max_1, 4000);
    	DataSet new_ds = (DataSet)op.getResult();
    	new ViewManager(new_ds, IViewManager.IMAGE);
			System.out.println(op.getDocumentation());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
  }

}
