/*
 * File:  DoubleDifferentialCrossection.java 
 *        (Generic operator adapted from the corresponding DataSetOperator)
 * Copyright (C) 2000-2002 Dennis Mikkelson
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
 *  Revision 1.1  2002/01/31 20:59:53  dennis
 *  Calculation simplified. Flight path correction should now
 *  be made by calling the FlightPathCorrection operator BEFORE
 *  using this DoubleDifferentialCrossection operator.  Also the
 *  calculation previously divided by def=2*E*delta_tof/tof and
 *  now just divides by delta_E.
 *
 *  Revision 1.1  2002/01/11 22:10:52  dennis
 *  Generic version of spectrometer DataSet operator
 *
 *  Revision 1.15  2001/09/27 19:19:29  dennis
 *  Improved Documentation.
 *  Return ErrorString if we don't have a histogram.
 *  Removed unused variable.
 *
 *  Revision 1.14  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.13  2001/04/26 19:08:58  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.12  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the 
 *     operators.
 *
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *  Revision 1.11  2000/10/03 21:29:36  dennis
 *  Renamed FudgeFactor to DetectorNormalizationFactor.
 *
 *  Revision 1.10  2000/09/11 23:05:01  dennis
 *  Added fudge factor parameter and boolean to control it.
 *
 *  Revision 1.9  2000/08/08 21:15:58  dennis
 *  Commented out some debug/informational prints.
 *
 *  Revision 1.8  2000/08/03 21:43:01  dennis
 *  This version has been checked and works ok.
 *
 *  Revision 1.7  2000/08/02 01:46:48  dennis
 *  Calculate the correction factors for detector efficiency and path length
 *  by interpolation in a table, for faster calculation.  Also, put the
 *  correction factors in a Data block then multiply one time so that the
 *  errors are calculated by Data.multiply().
 *
 *  Revision 1.6  2000/07/26 22:38:31  dennis
 *  Fixed problem with interpolating in wrong table.
 *  Also, now adds the Scattering Function operator to the result of the
 *  Double Differential Crossection calculation.
 *
 *  Revision 1.5  2000/07/26 20:50:27  dennis
 *  now interpolates in tables of eff[] and fpcorr[] values to avoid
 *  recalculating these values for each point of the spectrum
 *
 *  Revision 1.4  2000/07/25 18:10:33  dennis
 *  Fixed error with tsec in calculation
 *
 *  Revision 1.3  2000/07/25 16:39:37  dennis
 *  Added monitor 1 peak area as a parameter and finished the calculation,
 *  including compensation for the scattered energy increment and the flux
 *
 *  Revision 1.2  2000/07/24 21:14:06  dennis
 *  Initial version, partially implemented
 *
 *  Revision 1.1  2000/07/24 16:05:17  dennis
 *  Operator to calculate the Double Differential Crossection for a 
 *  Spectrometer.
 *
 */

package Operators.TOF_DG_Spectrometer;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.retriever.*;
import  DataSetTools.operator.*;
import  DataSetTools.viewer.*;

/**
  *  Compute the double differential crossection for a time-of-flight 
  *  spectrometer DataSet based on a sample with background subtracted,
  *  the area of the peak in monitor 1 and the number of atoms in the sample.
  *  The initial DataSet must be a time-of-flight histogram.
  *
  *  <p><b>Title:</b> Double Differential Crossection 
  * 
  *  <p><b>Command:</b> DSDODE_2
  *
  *  <p><b>Usage:</b><ul> 
  *    When used in a script, the parameters are as described in the 
  *    documentation for the constructor, as listed below.
  *  </ul>
  * 
  *  <p><b>Returns:</b><ul>
  *     If a new DataSet was requested, this returns a new DataSet
  *     containing the Double Differential Crossection.  If an 
  *     error occurs, or a new DataSet was not requested, this returns
  *     a message String.
  *   </ul>
  *
  *  <p><b>Algorithm:</b><ul>
  *
  *     DSDODE = sig*ff/(sangle*atoms*def*eff*flux*tsec)
  *
  *  <p>sig = the sample minus background "signal"
  *  <p>ff = the detector normalization factors 
  *  <p>sangle = the solid angle for a group 
  *  <p>atoms = the number of "scattering units" in the sample exposed to 
  *             the beam times 10 ** -24. 
  *  <p>def = Scattered energy increment, deltaE 
  *  <p>eff = Detector efficiency factor as a function of tof 
  *  <p>flux = flux at sample, calculated from monitor 1.
  *  <p>tsec = time of sample run, number of T0 pulses / 30.
  *
  *  </ul>
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class DoubleDifferentialCrossection extends    GenericTOF_DG_Spectrometer
                                           implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator to calculate the Double Differential Crossection
   * using a default parameter list.  If this constructor is used, the 
   * operator must be subsequently added to the list of operators of a 
   * particular DataSet.  Also, meaningful values for the parameters should 
   * be set ( using a GUI ) before calling getResult() to apply the operator 
   * to the DataSet this operator was added to.
   */

  public DoubleDifferentialCrossection( )
  {
    super( "Double Differential Crossection" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Double Differential Crossection
   *  for a spectrometer TOF DataSet.  It is assumed that the background DataSet
   *  has already been normalized and subtracted from the sample DataSet.
   *
   *  @param  ds               The sample DataSet for which the double 
   *                           differential crossection is to be calculated 
   *  @param  ff_ds            DataSet containing detector normalization
   *                           factors calculated from a vanadium run.
   *  @param  use_ff_ds        Boolean flag indicating whether ff_ds contains
   *                           normalization factors to use, or to just use
   *                           "1" for normalization in the calculation.
   *  @param  peak_area        The area of the peak in monitor 1.
   *  @param  atoms            The number of "scattering units" in the sample
   *                           exposed to the beam times 10 ** -24.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public DoubleDifferentialCrossection( DataSet    ds,
                                        DataSet    ff_ds,
                                        boolean    use_ff_ds,
                                        float      peak_area,
                                        float      atoms,
                                        boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter( 0 );
    parameter.setValue( ds );

    parameter = getParameter( 1 );
    parameter.setValue( ff_ds );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( use_ff_ds ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Float(peak_area) );

    parameter = getParameter( 4 );
    parameter.setValue( new Float(atoms) );

    parameter = getParameter( 5 );
    parameter.setValue( new Boolean( make_new_ds ) );
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor: 
   *          in this case, DSDODE_2
   */
   public String getCommand()
   {
     return "DSDODE_2";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Sample Data", DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "Detector Normalization Factors DataSet",
                                DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "use FF_DataSet?", new Boolean(false) );
    addParameter( parameter );

    parameter = new Parameter("Monitor 1 Peak Area", new Float(100000) );
    addParameter( parameter );

    parameter = new Parameter("Atoms in sample (times 10**-24)",new Float(1.0));
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
                                                    // get the parameters
    DataSet ds         = (DataSet)(getParameter(0).getValue());
    DataSet ff_ds      = (DataSet)(getParameter(1).getValue());
    boolean use_ff_ds  =((Boolean)getParameter(2).getValue()).booleanValue();
    float   peak_area  = ((Float)(getParameter(3).getValue())).floatValue();
    float   atoms      = ((Float)(getParameter(4).getValue())).floatValue();
    boolean make_new_ds=((Boolean)getParameter(5).getValue()).booleanValue();

    if ( atoms <= 0 )
      return new ErrorString("ERROR: Number of atoms must be greater than 0");

                     // if we are using normalization factors, get the ff values
                     // check that there are the correct number of them
    float ff_vals[] = null; 
    if ( use_ff_ds )
    {
      if ( ff_ds.getNum_entries() != 3 )
        return new ErrorString("ERROR: ff_ds should have 3 Data blocks");
      
      Data ff_data;
      ff_data = ff_ds.getData_entry( 2 );
      ff_vals = ff_data.getY_values();
      if ( ff_vals.length != ds.getNum_entries() )
        return new ErrorString("ERROR: wrong number of ff_vals, " + 
                                ff_vals.length );
    }

    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Calculated Double Differential Cross-section" );

    AttributeList attr_list;
    Float   Float_val;
    Integer Int_val;
    float   solid_angle,
            scale_factor;

    DetectorPosition position;
    float spherical_coords[];

    float x_vals[],
          new_y_vals[],
          new_errors[],
          tof,
          delta_tof,
          def,
          tsec,
          eff,
          flux;
    float energy_in,
          velocity_in,
          mon_1_area = 0.00516f;
    int   num_data,
          num_pulses;
    Data  data,
          correction_data,
          new_data;
                                          // make table of eff values and 
                                          // interpolate to save time 
    float speed_arr[]  = new float[1001];
    float eff_arr[]    = new float[1001];
    float result[];
    float final_speed;
    for ( int i = 0; i <= 1000; i++ )
    {
      final_speed = i * 0.00002f;
      result      = tof_data_calc.getEfficiencyFactor( final_speed, 1 );
      speed_arr[i]  = final_speed;
      eff_arr[i]    = result[0];
    }

    num_data = ds.getNum_entries();
    for ( int index = 0; index < num_data; index++ )
    {
      data = ds.getData_entry( index );
                                               // get the needed attributes
      attr_list   = data.getAttributeList();

      Float_val   = (Float)attr_list.getAttributeValue(Attribute.SOLID_ANGLE);
      solid_angle = Float_val.floatValue();

      Float_val   = (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);
      energy_in   = Float_val.floatValue();

      Int_val     = (Integer)
                    attr_list.getAttributeValue(Attribute.NUMBER_OF_PULSES);
      num_pulses  = Int_val.intValue();

//   System.out.println("ID = " + data.getGroup_ID()+
//                      " SA = " + Format.real( solid_angle, 12, 6 ));
      position = (DetectorPosition)
                  attr_list.getAttributeValue(Attribute.DETECTOR_POS);
      spherical_coords = position.getSphericalCoords();

      tsec = num_pulses/30.0f;
      velocity_in = tof_calc.VelocityFromEnergy( energy_in );
      flux = peak_area * velocity_in / ( mon_1_area * tsec * 0.0022f );
            
                                        // compensate for solid angle subtended
                                        // the number of atoms in the, sample
                                        // and the incident beam flux 
      scale_factor = 1000.0f / (solid_angle * atoms * flux * tsec);
      if ( use_ff_ds )
        scale_factor *= ff_vals[index];

                                        // compensate for detector efficiency
                                        // as a function of neutron velocity   
                                        // the result will be a function with
                                        // equal number of x&y values
      x_vals     = data.getX_scale().getXs();
      int num_y  = data.getY_values().length;

      if ( num_y >= x_vals.length )
      {
        System.out.println("ERROR: need histogram in DSDODE_2 calculation");
        return new ErrorString( "Need histogram in DSDODE_2 calculation" );
      }
      else
      {
        new_y_vals = new float[ num_y ]; 
        new_errors = new float[ num_y ]; 
                                       
        for ( int i = 0; i < num_y; i++ )
        {
          new_errors[i] = 0;                     // for now assume correction 
                                                 // factor is accurate.
          tof = (x_vals[i]+x_vals[i+1])/2;
          eff = arrayUtil.interpolate( spherical_coords[0]/tof, 
                                       speed_arr, eff_arr );
          new_y_vals[i] = scale_factor / eff;
        }
                                          // correction Data block is also
                                          // histogram, for compatibility
        correction_data = new Data ( data.getX_scale(),
                                     new_y_vals,
                                     new_errors,
                                     data.getGroup_ID() );

        new_data = data.multiply( correction_data );  
        // at this point we've multiplied by the scale factors:
        // 1000*FF/(solid_angle * atoms * flux * tsec * eff)
        // but have NOT divided by DEF.  We are still dealing with "counts" in
        // a particular time interval.

        if ( make_new_ds )
          new_ds.addData_entry( new_data );
        else
          new_ds.replaceData_entry( new_data, index );
      }
    }
                                       // Convert to "raw" energy loss spectrum
                                       // with non-uniform bins. 
    Operator op = new SpectrometerTofToEnergyLoss( new_ds, 0, 0, 0 );
    new_ds = (DataSet)op.getResult();
                                       // We still have "counts", but they are
                                       // now counts in non-uniform E-LOSS bins
                                       // the conversion to a density function
                                       // will divide by deltaE, to get "counts"
                                       // per Energy as a probability density
                                       // function (if second parameter==true)
    op = new ConvertHistogramToFunction( new_ds, true, false );
    op.getResult();    

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Calculated Double Differential Cross-section" );
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    Operator new_op = new DoubleDifferentialCrossection( );
    new_op.CopyParametersFrom( this );
    return new_op;
  }

 /* ------------------------------- main ---------------------------------- */
 /**
  *  Main program for testing purposes.
  */
  public static void main( String args[] )
  {
    System.out.println("Test of DoubleDifferentialCrossection" );
    
    String    run_name = "/home/dennis/ARGONNE_DATA/hrcs2447.run";
    Retriever rr       = new RunfileRetriever( run_name );

    DataSet     ds  = rr.getDataSet(1);
    ViewManager vm1 = new ViewManager(ds,IViewManager.IMAGE);
 
    Operator    op     = new FlightPathCorrection(ds, true );
    DataSet     ds_cor = (DataSet)op.getResult();
    ViewManager vm2    = new ViewManager(ds_cor,IViewManager.IMAGE);

    op = new DoubleDifferentialCrossection(ds_cor, null, false, 10000, 1, true);

    Object ddif_ds = op.getResult();
    if ( ddif_ds == null )
      System.out.println("Error in calculating DSDODE_2... returned null");
    else
    {
      System.out.println("DSDODE_2 returned:" + ddif_ds );
      if ( ddif_ds instanceof DataSet )
      {
        ViewManager vm3 = new ViewManager((DataSet)ddif_ds,IViewManager.IMAGE);

        op = new ResampleDataSet( (DataSet)ddif_ds, -100, 100, 200, true );
        DataSet smooth_ds = (DataSet)op.getResult();
        ViewManager vm4=new ViewManager( smooth_ds,IViewManager.IMAGE );
      }
    }
   System.out.println("End of test of DoubleDifferentialCrossection");
  }
}
