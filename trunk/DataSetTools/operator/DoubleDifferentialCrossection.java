/*
 * @(#)DoubleDifferentialCrossection.java   0.1  2000/07/25   Dennis Mikkelson
 *             
 *  $Log$
 *  Revision 1.3  2000/07/25 16:39:37  dennis
 *  Added monitor 1 peak area as a parameter and finished the calculation,
 *  including compensation for the scattered energy increment and the flux
 *
 *  Revision 1.2  2000/07/24 21:14:06  dennis
 *  Initial version, partially implemented
 *
 *  Revision 1.1  2000/07/24 16:05:17  dennis
 *  Operator to calculate the Double Differential Crossection for a Spectrometer.
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
  *  Compute the double differential crossection for a time-of-flight 
  *  spectrometer DataSet based on a sample with background subtracted,
  *  the area of the peak in monitor 1 and the number of atoms in the sample.
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class DoubleDifferentialCrossection extends    DataSetOperator 
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

  public DoubleDifferentialCrossection( )
  {
    super( "Double Differential Crossection" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Double Differential Crossection
   *  for a spectrometer DataSet.  It is assumed that the background DataSet
   *  has already been normalized and subtracted from the sample DataSet.
   *
   *  @param  ds               The sample DataSet for which the double 
   *                           differential crossection is to be calculated 
   *  @param  peak_area        The area of the peak in monitor 1.
   *  @param  atoms            The number of "scattering units" in the sample
   *                           exposed to the beam times 10 ** -24.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public DoubleDifferentialCrossection( DataSet    ds,
                                        float      peak_area,
                                        float      atoms,
                                        boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float(peak_area) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float(atoms) );

    parameter = getParameter( 2 );
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
     return "DSDODE";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Monitor 1 Peak Area",
                                         new Float(100000) );
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
    float   peak_area  = ((Float)(getParameter(0).getValue()) ).floatValue();
    float   atoms      = ((Float)(getParameter(1).getValue()) ).floatValue();
    boolean make_new_ds=((Boolean)getParameter(2).getValue()).booleanValue();

    if ( atoms <= 0 )
      return new ErrorString("ERROR: Number of atoms must be greater than 0");

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
          y_vals[],
          corr[],
          tof,
          delta_tof,
          def,
          eff,
          flux;
    float energy_in,
          velocity_in,
          mon_1_area = 0.00516f;
    int   num_data,
          num_pulses;
    Data  data,
          new_data;

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

      System.out.println("ID = " + data.getGroup_ID()+
                        " SA = " + Format.real( solid_angle, 12, 6 ));
      position = (DetectorPosition)
                  attr_list.getAttributeValue(Attribute.DETECTOR_POS);
      spherical_coords = position.getSphericalCoords();

                                        // need velocity in m/sec not m/us
      velocity_in = tof_calc.VelocityFromEnergy( energy_in ) * 1000000;
      flux = peak_area * velocity_in /
             ( mon_1_area * (num_pulses/30.0f) * 0.0022f );
            
                                        // compensate for solid angle subtended
                                        // the number of atoms in the, sample
                                        // and the incident beam flux 
      scale_factor = 1000.0f / (solid_angle * atoms * flux);

      new_data = data.multiply( scale_factor );
                                            
                                        // compensate for detector efficiency
                                        // as a function of neutron velocity   
      y_vals = new_data.getY_values();
      x_vals = new_data.getX_scale().getXs();

      

      for ( int i = 0; i < y_vals.length; i++ )
      {
        tof = (x_vals[i]+x_vals[i+1])/2;
        delta_tof = x_vals[i+1] - x_vals[i];
        corr = tof_data_calc.getEfficiencyFactor( spherical_coords[0]/tof, 1 );
        eff = corr[0];
        def = 2*tof_calc.Energy(spherical_coords[0]+corr[1], tof)*delta_tof/tof;
        y_vals[i] /= (def*eff);
      }

      if ( make_new_ds )
        new_ds.addData_entry( new_data );
      else
        new_ds.replaceData_entry( new_data, index );
    }

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
