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
 *  Revision 1.15  2006/07/10 22:28:39  dennis
 *  Removed unused imports after refactoring to use new Parameter GUIs
 *  in gov.anl.ipns.Parameters.
 *
 *  Revision 1.14  2006/07/10 16:26:11  dennis
 *  Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 *  Revision 1.13  2004/05/10 20:42:29  dennis
 *  Test program now just instantiates a ViewManager to diplay
 *  calculated DataSet, rather than keeping a reference to it.
 *  This removes an Eclipse warning about a local variable that is
 *  not read.
 *
 *  Revision 1.12  2004/05/03 16:22:58  dennis
 *  Removed unused local variables
 *
 *  Revision 1.11  2004/03/15 19:36:54  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.10  2004/03/15 03:37:00  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.9  2003/02/04 21:08:27  dennis
 *  Added getDocumentation() method. (Chris Bouzek)
 *
 *  Revision 1.8  2002/12/11 22:31:31  pfpeterson
 *  Removed the '_2' from getCommand() and its javadocs.
 *
 *  Revision 1.7  2002/12/09 23:40:04  pfpeterson
 *  Now checks for an the class that the number of pulses attribute is stored as.
 *
 *  Revision 1.6  2002/11/27 23:30:33  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/09/19 15:58:04  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.4  2002/04/19 19:40:39  dennis
 *  Fixed "broken" @see javadoc comments.
 *
 *  Revision 1.3  2002/03/13 16:26:26  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.2  2002/02/22 20:43:48  pfpeterson
 *  Operator reorganization.
 *
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
 */

package Operators.TOF_DG_Spectrometer;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.retriever.*;
import  DataSetTools.operator.*;
import  DataSetTools.operator.Generic.TOF_DG_Spectrometer.*;
import  DataSetTools.operator.DataSet.Conversion.XAxis.*;
import  DataSetTools.operator.DataSet.Math.Analyze.*;
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
    IParameter parameter = getParameter( 0 );
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
   *          in this case, DSDODE
   */
   public String getCommand()
   {
     return "DSDODE";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
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


  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator computes the double differential ");
    s.append("cross section for a time-of-flight spectrometer DataSet ");
    s.append("based on a sample with background subtracted, the area of ");
    s.append("the peak in monitor 1 and the number of atoms in the ");
    s.append("sample.\n");
    s.append("@assumptions The initial DataSet must be a time-of-flight ");
    s.append("histogram.\n");
    s.append("The number of scattering units (i.e. atoms) specified must ");
    s.append("be greater than zero.\n");
    s.append("Also, if using the normalization factors from the DataSet ");
    s.append("ff_ds, the DataSet ff_ds should have three data blocks.  ");
    s.append("In addition, the number of normalization factors should match ");
    s.append("the number of data entries in the DataSet ds.\n");
    s.append("@algorithm First this operator determines if it is supposed ");
    s.append("to use normalization factors from ff_ds, and retrieves the ");
    s.append("factors if necessary.\n");
    s.append("Then for each data entry, it retrieves the solid angle, ");
    s.append("the energy in, the number of pulses, and the detector ");
    s.append("position.  It then uses these to calculate the velocity in ");
    s.append("and the flux.\n");
    s.append("It then calculates a compensation factor for the solid angle, ");
    s.append("the number of atoms in the sample, and the incident beam ");
    s.append("flux.\n");
    s.append("Another compensation is calculated for detector efficiency as ");
    s.append("a function of neutron velocity.\n");
    s.append("After the data entries have been changed, the ");
    s.append("SpectrometerTofToEnergyLoss operator is applied in order to");
    s.append("convert to a raw energy loss spectrum with non-uniform bins.\n");
    s.append("Next the ConvertHistogramToFunction operator is applied to");
    s.append("get counts per energy as a probability density function.\n");
    s.append("Finally a message is appended to the DataSet's log indicating ");
    s.append("that a double differential cross section was performed.\n");
    s.append("@param ds The sample DataSet for which the double ");
    s.append("differential crossection is to be calculated.\n");
    s.append("@param ff_ds DataSet containing detector normalization ");
    s.append("factors calculated from a vanadium run.\n");
    s.append("@param use_ff_ds Boolean flag indicating whether ff_ds ");
    s.append("contains normalization factors to use, or to just use ");
    s.append("\"1\" for normalization in the calculation.\n");
    s.append("@param peak_area The area of the peak in monitor 1.\n");
    s.append("@param atoms The number of \"scattering units\" in the ");
    s.append("sample exposed to the beam times 10 ** -24.\n");
    s.append("@param make_new_ds Flag that determines whether a new DataSet ");
    s.append("is constructed, or the Data blocks of the original DataSet ");
    s.append("are just altered.\n");
    s.append("@return If make_new_ds is true, this operator returns a ");
    s.append("DataSet which has had a double differential cross section ");
    s.append("performed on it.  Otherwise, it returns a String indicating ");
    s.append("that a double differential cross section was performed.\n");
    s.append("@error Returns an error if the number of atoms is zero or ");
    s.append("less.\n");
    s.append("@error Returns an error if the DataSet ff_ds does not have ");
    s.append("three data blocks.\n");
    s.append("@error Returns an error if the number of normalization ");
    s.append("factors does not match the number of data entries in the ");
    s.append("DataSet ds.\n");
    s.append("@error Returns an error if any of the data entries in the ");
    s.append("DataSet are not histograms.\n");
    return s.toString();
  }    

  /* ---------------------------- getResult ------------------------------- */
  /*
   *  Computes the double differential cross section using peak area, 
   *  number of scattering units, solid angle, energy in, number of pulses,
   *  and detector position.  Two compensation factors are used: one for 
   *  detector efficiency, and one for incident beam flux, solid angle, 
   *  and the number of atoms in the sample.  Two other operators are also
   *  used for the calculation: SpectrometerTofToEnergyLoss and 
   *  ConvertHistogramToFunction.
   *
   *  @return If make_new_ds is true, this operator returns a DataSet which 
   *  has had a double differential cross section performed on it.  Otherwise, 
   *  it returns a String indicating that a double differential cross 
   *  section was performed.
   */

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
    float   solid_angle,
            scale_factor;

    DetectorPosition position;
    float spherical_coords[];

    float x_vals[],
          new_y_vals[],
          new_errors[],
          tof,
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
    Object val=null;
    for ( int index = 0; index < num_data; index++ )
    {
      data = ds.getData_entry( index );
                                               // get the needed attributes
      solid_angle = AttrUtil.getSolidAngle(data);
      if ( Float.isNaN(solid_angle) )
      {
        PixelInfoList pil = AttrUtil.getPixelInfoList(data);
        
        if ( pil != null )
        {
          solid_angle = pil.SolidAngle();
          data.setAttribute( 
                     new FloatAttribute(Attribute.SOLID_ANGLE,solid_angle) );
        }
      }

      if ( Float.isNaN(solid_angle) )
        throw new IllegalArgumentException(
          "ERROR: no SolidAngle or PixelInfoList attribute, needed by DSDODE");

      attr_list   = data.getAttributeList();
      Float_val   = (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);
      energy_in   = Float_val.floatValue();

      val=attr_list.getAttributeValue(Attribute.NUMBER_OF_PULSES);
      if( val instanceof Integer ){
        num_pulses = ((Integer)val).intValue();
      }else if(val instanceof Float ){
        num_pulses = (int)(((Float)val).floatValue());
      }else{
        num_pulses=0;
      }

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

      if ( !data.isHistogram() ) 
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
        correction_data = Data.getInstance ( data.getX_scale(),
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
    new ViewManager(ds,IViewManager.IMAGE);
 
    Operator    op     = new FlightPathCorrection(ds, true );
    DataSet     ds_cor = (DataSet)op.getResult();
    new ViewManager(ds_cor,IViewManager.IMAGE);

    op = new DoubleDifferentialCrossection(ds_cor, null, false, 10000, 1, true);
    
    /*------- added by Chris Bouzek ---------- */
    System.out.println("Documentation: " + op.getDocumentation() + "\n");
    /*---------------------------------------*/

    Object ddif_ds = op.getResult();
    if ( ddif_ds == null )
      System.out.println("Error in calculating DSDODE_2... returned null");
    else
    {
      System.out.println("DSDODE_2 returned:" + ddif_ds );
      if ( ddif_ds instanceof DataSet )
      {
        new ViewManager((DataSet)ddif_ds,IViewManager.IMAGE);

        op = new ResampleDataSet( (DataSet)ddif_ds, -100, 100, 200, true );
        DataSet smooth_ds = (DataSet)op.getResult();
        new ViewManager( smooth_ds,IViewManager.IMAGE );
      }
    }
   System.out.println("End of test of DoubleDifferentialCrossection");
  }
}
