/*
 * @(#) FocusIncidentSpectrum.java     
 *
 * Programmer:  Dennis Mikkelson
 *             
 * $Log$
 * Revision 1.1  2000/12/13 00:10:44  dennis
 * Added static method IncSpecFocus to focus the incident spectrum
 * to a bank of detectors for a powder diffractometer.
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
  *  This operator focusses the incident spectrum from a beam monitor to a
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
   *                        be focussed
   *  @param  group_id      The group_id of the monitor Data block 
   *  @param  t_min         Smallest tof to which the spectrum is focussed.
   *  @param  t_max         Largest tof to which the spectrum is focussed.
   *  @param  num_bins      Number of time bins to be used for the focussed 
   *                        spectrum.
   *  @param  path_length   The total flight path length for the focussed 
   *                        spectrum
   *  @param  theta         The nominal angle for the focussed spectrum 
   *  @param  theta_min     The minimum angle for the focussed spectrum 
   *  @param  theta_max     The maximum angle for the focussed spectrum 
   */

  public FocusIncidentSpectrum( DataSet      ds,
                                int          group_id,
                                float        t_min,
                                float        t_max,
                                int          num_bins,
                                float        path_length,
                                float        theta,
                                float        theta_min,
                                float        theta_max )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter(0);
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

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "IncSpecFocus";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
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
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    int   group_id    = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float t_min       = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float t_max       = ( (Float)(getParameter(2).getValue()) ).floatValue();
    int   num_bins    = ( (Integer)(getParameter(3).getValue()) ).intValue();
    float path_length = ( (Float)(getParameter(4).getValue()) ).floatValue();
    float theta       = ( (Float)(getParameter(5).getValue()) ).floatValue();
    float theta_min   = ( (Float)(getParameter(6).getValue()) ).floatValue();
    float theta_max   = ( (Float)(getParameter(7).getValue()) ).floatValue();

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
      monitor_data = (Data)monitor_data.clone();
      monitor_data.ConvertToHistogram( false );
    }

    XScale new_x_scale = new UniformXScale( t_min, t_max, num_bins+1 );

    Data new_data = tof_data_calc.IncSpecFocus( monitor_data,
                                                new_x_scale, 
                                                path_length,
                                                theta,
                                                theta_min,
                                                theta_max,
                                                group_id );

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
    DataSetOperator new_op = new FocusIncidentSpectrum( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
