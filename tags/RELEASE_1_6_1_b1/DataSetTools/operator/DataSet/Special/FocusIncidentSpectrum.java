/*
 * File:   FocusIncidentSpectrum.java
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
 * Programmer:  Dennis Mikkelson
 *
 * $Log$
 * Revision 1.6  2004/01/24 19:41:13  bouzekc
 * Removed unused variables from main().  Removed unused imports.
 *
 * Revision 1.5  2003/01/13 17:20:56  dennis
 * Added getDocumentation(), main test program and javadocs on getResult()
 *
 * Revision 1.4  2002/11/27 23:19:19  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 16:02:51  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/03/13 16:19:17  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/02/22 21:03:42  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Special;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;
import  DataSetTools.retriever.*;

/**
  *  This operator focuses the incident spectrum from a beam monitor to a
  *  bank of detectors at a specified total flight path length and range
  *  of angles. This based on the FORTRAN SUBROUTINE inc_spec_focus from IPNS.
  */

public class  FocusIncidentSpectrum  extends   DS_Special
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

  public FocusIncidentSpectrum( )
  {
    super( "Focus Incident Spectrum" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds            The DataSet containing the monitor Data that is to
   *                        be focused
   *  @param  group_id      The group_id of the monitor Data block
   *  @param  t_min         Smallest tof to which the spectrum is focused.
   *  @param  t_max         Largest tof to which the spectrum is focused.
   *  @param  num_bins      Number of time bins to be used for the focused
   *                        spectrum.
   *  @param  path_length   The total flight path length for the focused
   *                        spectrum
   *  @param  theta         The nominal angle for the focused spectrum
   *  @param  theta_min     The minimum angle for the focused spectrum
   *  @param  theta_max     The maximum angle for the focused spectrum
   *  @param  new_group_id  The group_id to be used for the focused spectrum
   */

  public FocusIncidentSpectrum( DataSet      ds,
                                int          group_id,
                                float        t_min,
                                float        t_max,
                                int          num_bins,
                                float        path_length,
                                float        theta,
                                float        theta_min,
                                float        theta_max,
                                int          new_group_id )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter = getParameter(0);
    parameter.setValue( new Integer( group_id ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( t_min ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( t_max ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Integer( num_bins ) );

    parameter = getParameter( 4 );
    parameter.setValue( new Float( path_length ) );

    parameter = getParameter( 5 );
    parameter.setValue( new Float( theta ) );

    parameter = getParameter( 6 );
    parameter.setValue( new Float( theta_min ) );

    parameter = getParameter( 7 );
    parameter.setValue( new Float( theta_max ) );

    parameter = getParameter( 8 );
    parameter.setValue( new Integer( new_group_id ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case,
   * IncSpecFocus
   */
   public String getCommand()
   {
     return "IncSpecFocus";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group ID of monitor", new Integer(0));
    addParameter( parameter );

    parameter = new Parameter("Start of interval to focus to", new Float(2500));
    addParameter( parameter );

    parameter = new Parameter("End of interval to focus to", new Float(22500));
    addParameter( parameter );

    parameter = new Parameter("Number of Bins", new Integer(2500));
    addParameter( parameter );

    parameter = new Parameter("New flight path length", new Float(20));
    addParameter( parameter );

    parameter = new Parameter("Nominal angle", new Float(144.845));
    addParameter( parameter );

    parameter = new Parameter("Minimum angle", new Float(134.79));
    addParameter( parameter );

    parameter = new Parameter("Maximum angle", new Float(154.9));
    addParameter( parameter );

    parameter = new Parameter("New Group ID", new Integer(0));
    addParameter( parameter );
  }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator focuses the incident spectrum from ");
    s.append("a beam monitor to a bank of detectors at a specified total ");
    s.append("flight path length and range of angles.\n");
    s.append("@assumptions The specified group ID corresponds to a valid ");
    s.append("data entry.  Furthermore, it is assumed that the absolute ");
    s.append("values of the Y and Z cartesian coordinates of the detector ");
    s.append("position are less than or equal to 0.01.\n");
    s.append("@algorithm First this operator acquires the monitor data ");
    s.append("corresponding to the group_id given.\n");
    s.append("Then it creates a new XScale using tmin, tmax, and num_bins.\n");
    s.append("Finally it uses the new XScale, the monitor data, path_length, ");
    s.append("theta, theta_min, theta_max, and new_group_ID to create a new ");
    s.append("DataSet.\n");
    s.append("It does this by using the IncSpecFocus method from the ");
    s.append("DataSetTools math library.   Note that this method is based on ");
    s.append("the FORTRAN SUBROUTINE inc_spec_focus from IPNS.\n");
    s.append("@param ds The DataSet containing the monitor Data that is to ");
    s.append("be focused.\n");
    s.append("@param group_id The group_id of the monitor Data block.\n");
    s.append("@param t_min Smallest tof to which the spectrum is focused.\n");
    s.append("@param t_max Largest tof to which the spectrum is focused.\n");
    s.append("@param num_bins Number of time bins to be used for the ");
    s.append("focused spectrum.\n");
    s.append("@param path_length The total flight path length for the ");
    s.append("focused spectrum.\n");
    s.append("@param theta The nominal angle for the focused spectrum.\n");
    s.append("@param theta_min The minimum angle for the focused spectrum.\n");
    s.append("@param theta_max The maximum angle for the focused spectrum.\n");
    s.append("@param new_group_id The group_id to be used for the focused ");
    s.append("spectrum.\n");
    s.append("@return DataSet which consists of the focused incident spectrum");
    s.append(" from the specified beam monitor data.\n");
    s.append("@error Returns an error if the specified group ID does not ");
    s.append("correspond to a valid data entry.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Calculates the incident energy of a neutron beam based on the pulse
   *  data from the two beam monitors contained in the input DataSet.
   *
   *  @return DataSet which consists of the focused incident spectrum from
   *  the specified beam monitor data.
   */
  public Object getResult()
  {                                  // get the parameters

    int   group_id     = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float t_min        = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float t_max        = ( (Float)(getParameter(2).getValue()) ).floatValue();
    int   num_bins     = ( (Integer)(getParameter(3).getValue()) ).intValue();
    float path_length  = ( (Float)(getParameter(4).getValue()) ).floatValue();
    float theta        = ( (Float)(getParameter(5).getValue()) ).floatValue();
    float theta_min    = ( (Float)(getParameter(6).getValue()) ).floatValue();
    float theta_max    = ( (Float)(getParameter(7).getValue()) ).floatValue();
    int   new_group_id = ( (Integer)(getParameter(8).getValue()) ).intValue();

                                     // get the current data set and do the
                                     // operation
    DataSet ds = this.getDataSet();

    Data monitor_data = ds.getData_entry_with_id( group_id );
    if ( monitor_data == null )
    {
      ErrorString message = new ErrorString(
                           "ERROR: no data entry with the group_ID "+group_id );
      System.out.println( message );
      return message;
    }

    boolean is_histogram = monitor_data.isHistogram();
    if ( !is_histogram )
    {
      monitor_data = new HistogramTable( monitor_data,
                                         false,
                                         monitor_data.getGroup_ID() );
    }

    XScale new_x_scale = new UniformXScale( t_min, t_max, num_bins+1 );


    Data new_data = tof_data_calc.IncSpecFocus( monitor_data,
                                                new_x_scale,
                                                path_length,
                                                theta,
                                                theta_min,
                                                theta_max,
                                                new_group_id );

    DataSet new_ds = ds.empty_clone();
    new_ds.addData_entry( new_data );

    return new_ds;
  }

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current FocusIncidentSpectrum Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    FocusIncidentSpectrum new_op = new FocusIncidentSpectrum( );
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
  DataSet ds1 = null, new_ds;
  int groupID, numBins, newGroupID;
  float tMin, tMax, pathLength, thetaG, thetaMin, thetaMax;
  StringBuffer m = new StringBuffer();
  FocusIncidentSpectrum op;

  groupID = 26;
  numBins = 4;
  newGroupID = 26;

  tMin = (float) 10.0;
  tMax = (float) 90.0;
  pathLength = (float) 100.0;
  thetaG = (float) 0.5;
  thetaMin = (float) 0.0;
  thetaMax = (float) 3.14;

  String file_name = "/home/groups/SCD_PROJECT/SampleRuns/hrcs2447.run";
                      //"D:\\ISAW\\SampleRuns\\hrcs2447.run";

  try
  {
    RunfileRetriever rr = new RunfileRetriever( file_name );
    ds1 = rr.getDataSet(1);
    //ViewManager viewer = new ViewManager(ds1, IViewManager.IMAGE);
  }
  catch(Exception e)
  {
    System.err.println("Error opening file");
  }

  try
  {
     op = new FocusIncidentSpectrum( ds1, groupID, 
                                     tMin, tMax, numBins, 
                                     pathLength,
                                     thetaG, 
                                     thetaMin, thetaMax,
                                     newGroupID );

     Object result = op.getResult();



     //display the DataSet
     if( result instanceof DataSet )
     {
       new_ds = (DataSet) result;
       System.out.println(new_ds.getData_entry(1).toString());
       new ViewManager(new_ds, IViewManager.IMAGE);
     }

     else
     {
       m.append("\nAn error occurred when calling getResult() for ");
       m.append("FocusIncidentSpectrum:\n\n");
       m.append(result.toString());
     }

     m.append("\nThe results of calling getDocumentation() for ");
     m.append("FocusIncidentSpectrum are:\n\n");
     m.append(op.getDocumentation());

     System.out.print(m.toString());
   }
   catch(Exception e)
   {
     System.err.println("An error occurred while running the operator.");
   }
  }

}
