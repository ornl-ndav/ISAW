/*
 * File:  DoubleDifferentialCrossection.java 
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
 *  Revision 1.5  2002/11/27 23:19:32  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/09/19 16:03:07  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.3  2002/03/13 16:19:17  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.2  2002/03/05 19:26:19  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 21:04:09  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.TOF_DG_Spectrometer;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.Operator;
import  DataSetTools.operator.DataSet.Math.Analyze.*;
import  DataSetTools.parameter.*;


/**
  *  Compute the double differential crossection for a time-of-flight 
  *  spectrometer DataSet based on a sample with background subtracted,
  *  the area of the peak in monitor 1 and the number of atoms in the sample.
  *  The initial DataSet must be a time-of-flight histogram.
  *
  *  <p><b>Title:</b> Double Differential Crossection 
  * 
  *  <p><b>Command:</b> DSDODE
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
  *  <p>def = Scattered energy increment, 2*E*delta_tof/tof
  *  <p>eff = Detector efficiency factor as a function of tof 
  *  <p>flux = flux at sample, calculated from monitor 1.
  *  <p>tsec = time of sample run, number of T0 pulses / 30.
  *
  *  </ul>
  *
  *  @see DataSetTools.operator.DataSet.DataSetOperator
  *  @see Operator
  */

public class DoubleDifferentialCrossection extends    DS_TOF_DG_Spectrometer
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
    IParameter parameter = getParameter( 0 );
    parameter.setValue( ff_ds );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( use_ff_ds ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float(peak_area) );

    parameter = getParameter( 3 );
    parameter.setValue( new Float(atoms) );

    parameter = getParameter( 4 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor: 
              in this case, DSDODE
   */
   public String getCommand()
   {
     return "DSDODE";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( 
                                     "Detector Normalization Factors DataSet",
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
                                                   // get the current data set
    DataSet ds  = getDataSet();
                                                    // get the parameters
    DataSet ff_ds      = (DataSet)(getParameter(0).getValue());
    boolean use_ff_ds  =((Boolean)getParameter(1).getValue()).booleanValue();
    float   peak_area  = ((Float)(getParameter(2).getValue()) ).floatValue();
    float   atoms      = ((Float)(getParameter(3).getValue()) ).floatValue();
    boolean make_new_ds=((Boolean)getParameter(4).getValue()).booleanValue();

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
          corr[],
          tof,
          delta_tof,
          def,
          tsec,
          eff,
          fpcorr,
          flux;
    float energy_in,
          velocity_in,
          mon_1_area = 0.00516f;
    int   num_data,
          num_pulses;
    Data  data,
          correction_data,
          new_data;
                                          // make table of eff and fpcorr
                                          // values and interpolate to get
                                          // faster calculation
    float speed_arr[]  = new float[1001];
    float eff_arr[]    = new float[1001];
    float fpcorr_arr[] = new float[1001];
    float result[];
    float final_speed;
    for ( int i = 0; i <= 1000; i++ )
    {
      final_speed = i * 0.00002f;
      result      = tof_data_calc.getEfficiencyFactor( final_speed, 1 );
      speed_arr[i]  = final_speed;
      eff_arr[i]    = result[0];
      fpcorr_arr[i] = result[1];
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
        System.out.println("ERROR: need histogram in DSDODE calculation");
        return new ErrorString( "Need histogram in DSDODE calculation" );
      }
      else
      {
        new_y_vals = new float[ num_y ]; 
        new_errors = new float[ num_y ]; 
                                       
        for ( int i = 0; i < num_y; i++ )
        {
          tof           = (x_vals[i]+x_vals[i+1])/2;
          delta_tof     = x_vals[i+1] - x_vals[i];
          new_errors[i] = 0;              // for now assume correction factor is
                                          // accurate.

          // interpolate in table or...
          eff    = arrayUtil.interpolate( spherical_coords[0]/tof, 
                                          speed_arr, eff_arr );
          fpcorr = arrayUtil.interpolate(spherical_coords[0]/tof,
                                          speed_arr, fpcorr_arr );

          //  recalculate each time
          //    result = tof_data_calc.getEfficiencyFactor( 
          //                           spherical_coords[0]/tof, 1 );
          //    eff    = result[0];
          //    fpcorr = result[1];

          def= 2*tof_calc.Energy(spherical_coords[0]+fpcorr, tof)*delta_tof/tof;
          new_y_vals[i] = scale_factor / (def*eff);
        }
                                          // correction Data block is also
                                          // histogram, for compatibility
        correction_data = Data.getInstance( data.getX_scale(),
                                            new_y_vals,
                                            new_errors,
                                            data.getGroup_ID() );

        new_data = data.multiply( correction_data ); 

        if ( make_new_ds )
          new_ds.addData_entry( new_data );
        else
          new_ds.replaceData_entry( new_data, index );
      }
    }

    new_ds.addOperator( new ScatteringFunction() );

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
    DoubleDifferentialCrossection new_op = new DoubleDifferentialCrossection( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
