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
 *  Revision 1.10  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.9  2001/04/26 19:11:21  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.8  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the 
 *     operators.
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
   * in this case, ScatFun_2
   */
   public String getCommand()
   {
     return "ScatFun_2";
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

    parameter = new Parameter("Sample scattering crossection",
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
        if ( x_vals.length > y_vals.length )  // histogram
          energy_loss = (x_vals[i]+x_vals[i+1])/2;
        else                                  // function
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
