/*
 * File:  ScatteringFunction.java 
 *        (Generic operator adapted from the corresponding DataSetOperator)
 * Copyright (C) 2000-2002, Dennis Mikkelson
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
 *  Revision 1.8  2003/02/07 14:19:20  dennis
 *  Added getDocumentation() method. (Chris Bouzek)
 *
 *  Revision 1.7  2002/12/11 22:31:31  pfpeterson
 *  Removed the '_2' from getCommand() and its javadocs.
 *
 *  Revision 1.6  2002/11/27 23:30:33  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/09/19 15:58:08  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.4  2002/07/10 20:13:20  dennis
 *  Removed unused code for energy dependent flight path correction, since this
 *  is now done in the FlightPathCorrection operator.
 *
 *  Revision 1.3  2002/04/19 19:40:43  dennis
 *  Fixed "broken" @see javadoc comments.
 *
 *  Revision 1.2  2002/03/13 16:26:26  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.1  2002/02/22 20:43:52  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.1  2002/01/31 21:02:12  dennis
 *  Initial version as generic operator.
 *  Moved into Operators.TOF_DG_Spectrometer package.
 *
 *  Revision 1.1  2002/01/11 22:10:57  dennis
 *  Generic version of spectrometer DataSet operator
 *
 *  Revision 1.11  2001/12/21 17:31:17  dennis
 *  Minor fix to documentation.
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
import  DataSetTools.operator.Generic.TOF_DG_Spectrometer.*;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;

/**
  *  Compute the scattering function for a direct geometry spectrometer 
  *  based on the result from the DoubleDifferentialCrossection 
  *  operator, as a function of energy loss.  
  *
  *  @see DoubleDifferentialCrossection 
  */

public class ScatteringFunction extends GenericTOF_DG_Spectrometer
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

  public ScatteringFunction( )
  {
    super( "Spectrometer Scattering Function" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Scattering Function
   *  for a spectrometer DataSet.  It is assumed that the 
   *  DoubleDifferentialCrossection operator has already been applied, to
   *  obtain the double differential crossection as a function of energy loss.
   *
   *  @param  ds               The sample DataSet for which the scattering 
   *                           function is to be calculated 
   *
   *  @param  crossection      The scattering crossection of the sample
   *
   *  @param  make_new_ds      Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public ScatteringFunction( DataSet    ds,
                                         float      crossection, 
                                         boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds );

    parameter = getParameter( 1 );
    parameter.setValue( new Float(crossection) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( make_new_ds ) );
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   * in this case, ScatFun
   */
   public String getCommand()
   {
     return "ScatFun";
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

    parameter = new Parameter("Sample scattering crossection",
                               new Float(1.0) );
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
   s.append("@overview This operator computes the scattering function for ");
   s.append("a direct geometry spectrometer based on the result from the ");
   s.append("DoubleDifferentialCrossection operator, as a function of ");
   s.append("energy loss.\n");
   s.append("@assumptions It is assumed that the ");
   s.append("DoubleDifferentialCrossection operator has already been ");
   s.append("applied.\n");
   s.append("Furthermore, the specified cross section must be greater ");
   s.append("than zero.\n");
   s.append("@algorithm For each data entry in the DataSet ds, this ");
   s.append("operator calculates the velocity in and the incident ");
   s.append("wave vector magnitude.\n");
   s.append("Then it compensates for detector efficiency as a function of ");
   s.append("neutron velocity.\n");
   s.append("Next it calculates energy loss, and uses this to find the ");
   s.append("final energy and final velocity.\n");
   s.append("From these the final wave vector magnitude is calculated.");
   s.append("Next the incident wave vector magnitude, final wave vector ");
   s.append("magnitude, and cross section are used to calculate new Y ");
   s.append("values.\n");
   s.append("The new Y-values are used to get conversion data, which is ");
   s.append("used to multiply the original data.\n");
   s.append("Once the operator has finished doing this for each data entry, ");
   s.append("an entry is added to the DataSet's log indicating that the ");
   s.append("scattering function was calculated.\n");
   s.append("@param ds The sample DataSet for which the scattering function ");
   s.append("is to be calculated.\n");
   s.append("@param crossection The scattering cross section of the ");
   s.append("sample.\n");
   s.append("@param make_new_ds Flag that determines whether a new DataSet ");
   s.append("is constructed, or the Data blocks of the original DataSet are ");
   s.append("just altered.\n");
   s.append("@return Returns a new DataSet which has entries made up of the ");
   s.append("original data entries which have had the scattering function ");
   s.append("applied if make_new_ds is true.  Otherwise, it returns a String ");
   s.append("indicating that the scattering function was applied to the ");
   s.append("original DataSet.\n");
   s.append("@error Returns an error if the cross section is not greater ");
   s.append("than zero.\n");
   return s.toString();
 }
  
  /* ---------------------------- getResult ------------------------------- */
  /** 
    *  Calculates the scattering function for the input DataSet ds.
    *
    *  @return If make_new_ds = true, a new DataSet which has had a scattering
    *  function applied is returned.  Otherwise, a String is returned 
    *  indicating that the scattering function was applied.
    */

  public Object getResult()
  {       
    final float four_PI = (float)(4.0*Math.PI);
    final float WVCON   = 1588.5f; // conversion factor between inverse velocity
                                   // and wave vector
                                                    // get the parameters
    DataSet ds         = (DataSet)(getParameter(0).getValue());
    float   sccs       = ((Float)(getParameter(1).getValue())).floatValue();
    boolean make_new_ds=((Boolean)getParameter(2).getValue()).booleanValue();

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
          final_energy,
          energy_loss;
    float energy_in,
          velocity_in,
          velocity_final,
          wvi,              // incident wave vector magnitude
          wvf;              // final    wave vector magnitude
    int   num_data;
    Data  data,
          conversion_data,
          new_data;

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
        if ( data.isHistogram() )                  // histogram, use bin center
          energy_loss = (x_vals[i]+x_vals[i+1])/2;
        else                                       // function, use point
          energy_loss = x_vals[i];

        final_energy = energy_in - energy_loss; 
        velocity_final = tof_calc.VelocityFromEnergy( final_energy );

        wvf = WVCON * velocity_final;

        new_y_vals[i] = four_PI*wvi/wvf/sccs;
      }

      conversion_data = Data.getInstance( data.getX_scale(),
                                          new_y_vals,
                                          new_errors,
                                          data.getGroup_ID() );
    
      new_data = data.multiply( conversion_data );

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
    Operator new_op = new ScatteringFunction( );
    new_op.CopyParametersFrom( this );
    return new_op;
  }

 /* ------------------------------- main ---------------------------------- */
 /**
  *  Main program for testing purposes.
  */
  public static void main( String args[] )
  {
    System.out.println("Test of ScatteringFunction" );
   
    String    run_name = "/home/dennis/ARGONNE_DATA/hrcs2447.run";
    Retriever rr       = new RunfileRetriever( run_name );
    DataSet   ds       = rr.getDataSet(1);

    Operator op = new DoubleDifferentialCrossection( ds, null, false,
                                                     10000, 1, true );
                                                                                                      
    System.out.println("Documentation: " + op.getDocumentation());

    Object ddif_ds = op.getResult();
    if ( ddif_ds == null )
      System.out.println("Error in calculating DSDODE... returned null");
    else
    {
      System.out.println("DSDODE returned:" + ddif_ds );
      if ( ddif_ds instanceof DataSet )
      {
        ViewManager vm1 = new ViewManager((DataSet)ddif_ds,IViewManager.IMAGE);
      }
    }

   op = new ScatteringFunction( (DataSet)ddif_ds, 1, true );
   Object scat_ds = op.getResult();
   if ( scat_ds == null )
      System.out.println("Error in calculating SCAT... returned null");
    else
    {
      System.out.println("SCAT returned:" + scat_ds );
      if ( scat_ds instanceof DataSet )
      {
        ViewManager vm3 = new ViewManager((DataSet)scat_ds,IViewManager.IMAGE);
      }
    }

   System.out.println("End of test of ScatteringFunction");
  }

}
