/*
 * @(#)SpectrometerScatteringFunction.java   0.1  2000/07/26   Dennis Mikkelson
 *             
 *  $Log$
 *  Revision 1.7  2000/10/03 22:09:55  dennis
 *  Now adds the operators:
 *    SpectrometerFrequencyDistributionFunction
 *    SpectrometerImaginaryGeneralizedSusceptibility
 *    SpectrometerSymmetrizedScatteringFunction
 *
 *  Revision 1.6  2000/08/09 17:09:35  dennis
 *  Removed extra clone of each Data block that was no longer needed since
 *  the CLSmooth operation is no longer done as part of this operator.
 *
 *  Revision 1.5  2000/08/08 21:14:05  dennis
 *  Now adds the GeneralizedEnergyDistribution function operator to the
 *  DataSet.
 *
 *  Revision 1.4  2000/08/03 21:42:48  dennis
 *  This version has been checked and works ok.
 *
 *  Revision 1.3  2000/08/03 16:18:09  dennis
 *  Now works for both functions and histograms
 *
 *  Revision 1.2  2000/07/28 13:56:45  dennis
 *  Added missing factor of 4PI in calculation
 *
 *  Revision 1.1  2000/07/26 22:36:56  dennis
 *  Initial version of Scattering Crossection function for Spectrometers
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
  *  Compute the scattering function for a direct geometry spectrometer 
  *  based on the result of applying the DoubleDifferentialCrossection 
  *  operator.  
  *
  *  @see DoubleDifferentialCrossection 
  *  @see DataSetOperator
  *  @see Operator
  */

public class SpectrometerScatteringFunction extends    DataSetOperator 
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

  public SpectrometerScatteringFunction( )
  {
    super( "Spectrometer Scattering Function" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Scattering Function
   *  for a spectrometer DataSet.  It is assumed that the 
   *  DoubleDifferentialCrossection operator has already been applied.
   *
   *  @param  ds               The sample DataSet for which the scattering 
   *                           function is to be calculated 
   *
   *  @param  crossection      The scattering crossection of the sample
   *
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public SpectrometerScatteringFunction( DataSet    ds,
                                         float      crossection, 
                                         boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float(crossection) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "ScatFun";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Sample scattering crossection",
                                         new Float(1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    final float four_PI = (float)(4.0*Math.PI);
    final float WVCON   = 1588.5f; // conversion factor between inverse velocity
                                   // and wave vector
                                                   // get the current data set
    DataSet ds  = getDataSet();
                                                    // get the parameters
    float   sccs       = ((Float)(getParameter(0).getValue()) ).floatValue();
    boolean make_new_ds=((Boolean)getParameter(1).getValue()).booleanValue();

    if ( sccs <= 0 )
      return new ErrorString(
                "ERROR: scattering crossection must be greater than 0");

    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Calculated Scattering Function" );

    AttributeList attr_list;
    Float   Float_val;

    DetectorPosition position;
    float spherical_coords[];

    float x_vals[],
          y_vals[],
          new_y_vals[],
          new_errors[],
          tof,
          eff,
          fpcorr;
    float energy_in,
          velocity_in,
          velocity_final,
          wvi,              // incident wave vector magnitude
          wvf;              // final    wave vector magnitude
    int   num_data;
    Data  data,
          conversion_data,
          new_data;
                                          // make table of fpcorr
                                          // values and interpolate to get
                                          // faster calculation
    float speed_arr[]  = new float[1001];
    float fpcorr_arr[] = new float[1001];
    float result[];
    float final_speed;
    for ( int i = 0; i <= 1000; i++ )
    {
      final_speed = i * 0.00002f;
      result      = tof_data_calc.getEfficiencyFactor( final_speed, 1 );
      speed_arr[i]  = final_speed;
      fpcorr_arr[i] = result[1];
    }

    num_data = ds.getNum_entries();
    for ( int index = 0; index < num_data; index++ )
    {
      data = ds.getData_entry( index );
                                               // get the needed attributes
      attr_list   = data.getAttributeList();

      Float_val   = (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);
      energy_in   = Float_val.floatValue();

      position = (DetectorPosition)
                  attr_list.getAttributeValue(Attribute.DETECTOR_POS);
      spherical_coords = position.getSphericalCoords();

      velocity_in = tof_calc.VelocityFromEnergy( energy_in );
      wvi = WVCON * velocity_in;
                                        // compensate for detector efficiency
                                        // as a function of neutron velocity   
      y_vals = data.getY_values();
      x_vals = data.getX_scale().getXs();

      int num_y = y_vals.length;
      new_y_vals = new float[ num_y ];
      new_errors = new float[ num_y ];

      for ( int i = 0; i < y_vals.length; i++ )
      {
        if ( x_vals.length > y_vals.length )  // histogram
          tof = (x_vals[i]+x_vals[i+1])/2;
        else                                  // function
          tof = x_vals[i];

        // interpolate in table or....         
        fpcorr = arrayUtil.interpolate(spherical_coords[0]/tof, 
                                       speed_arr, 
                                       fpcorr_arr );

        //  recalculate each time
        //    result = tof_data_calc.getEfficiencyFactor( 
        //                           spherical_coords[0]/tof, 1 );
        //    fpcorr = result[1];

        velocity_final = (spherical_coords[0]+fpcorr) / tof;
        wvf = WVCON * velocity_final;

        new_y_vals[i] = four_PI*wvi/wvf/sccs;
      }

      conversion_data = new Data( data.getX_scale(),
                                  new_y_vals,
                                  new_errors,
                                  data.getGroup_ID() );
    
      new_data = data.multiply( conversion_data );

      if ( make_new_ds )
        new_ds.addData_entry( new_data );
      else
        new_ds.replaceData_entry( new_data, index );
    }

    new_ds.addOperator(new SpectrometerGeneralizedEnergyDistributionFunction());
    new_ds.addOperator( new SpectrometerFrequencyDistributionFunction() );
    new_ds.addOperator(
            new SpectrometerImaginaryGeneralizedSusceptibility() );
    new_ds.addOperator( new SpectrometerSymmetrizedScatteringFunction() );


    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Calculated Scattering Function" );
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    SpectrometerScatteringFunction new_op = 
                                   new SpectrometerScatteringFunction( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
