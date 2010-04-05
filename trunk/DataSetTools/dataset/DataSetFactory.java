/*
 * File:  DataSetFactory.java 
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *  Revision 1.54  2007/04/26 20:36:18  dennis
 *  The addOperators() method now throws an IllegalArgumentException
 *  if the requested instrument type is not supported.
 *
 *  Revision 1.53  2006/07/11 17:00:45  dennis
 *  Now adds ToQ2E operator, as well as the ToQE operator.
 *
 *  Revision 1.52  2006/02/02 23:06:33  dennis
 *  Now adds operators to add, subtract, multiply and divide
 *  a specified Data block by a specified constant.
 *
 *  Revision 1.51  2005/11/19 15:47:01  dennis
 *  Now adds ResampleOnGeometricProgression operator to DataSet.
 *
 *  Revision 1.50  2005/10/27 20:56:24  dennis
 *  Added SelectByID() operator.
 *
 *  Revision 1.49  2005/10/13 18:54:06  dennis
 *  Added operators to multiply and divide y-values by delta_X.
 *
 *  Revision 1.48  2005/10/03 04:06:09  dennis
 *  Added GetSelectedIndices operator to DataSet.
 *
 *  Revision 1.47  2005/10/03 02:43:10  dennis
 *  Added SelectByIndex() operator to DataSet.
 *
 *  Revision 1.46  2005/08/05 21:00:27  dennis
 *  Added more descriptive error message if an invalid
 *  instrument type is passed to the addOperators() method.
 *
 *  Revision 1.45  2005/04/04 22:32:49  dennis
 *  Now also adds GetPixelInfo operator for TOF_Diffractometers,
 *  TOF_DG_Spectrometers, TOF_Reflectometers and TOF_IDG_Spectrometers,
 *  since such instruments either have, or will likely have LPSDs or
 *  area detectors in the future.
 *
 *  Revision 1.44  2004/08/17 15:13:35  rmikk
 *  Added code to prevent output from an operator to show up in strange places
 *
 *  Revision 1.43  2004/08/02 20:05:00  rmikk
 *  Added the integrate operator to SCD data sets
 *
 *  Revision 1.42  2004/03/15 06:10:35  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.41  2003/10/15 02:30:44  bouzekc
 *  Updated to correspond with correct spelling of reflectometer in IPNS
 *  files.
 *
 *  Revision 1.40  2003/09/20 19:14:58  dennis
 *  Added SumByAttributeNormSA() operator to DG_Spectrometer
 *  DataSets. (Alok)
 *
 *  Revision 1.39  2003/07/05 18:11:29  rmikk
 *  Added two new Attribute operators, ClearSelect and
 *  SelectGroups, to every DataSet.
 *
 *  Revision 1.38  2003/03/03 16:49:16  pfpeterson
 *  Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 *  Revision 1.37  2003/02/12 20:07:42  dennis
 *  Now adds PixelInfo_op instead of SegmentInfo_op
 *
 *  Revision 1.36  2003/01/15 20:54:25  dennis
 *  Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 *  Revision 1.35  2002/12/11 22:25:26  pfpeterson
 *  Removed reference to DoubleDifferentialCrossSection.
 *
 *  Revision 1.34  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.33  2002/10/24 16:51:08  pfpeterson
 *  Removed references to old DGCS operators.
 *
 *  Revision 1.32  2002/10/09 21:13:33  dennis
 *  Added method getTestDataSet() to fabricate a simple DataSet for
 *  test purposes.
 *
 *  Revision 1.31  2002/09/25 16:47:47  pfpeterson
 *  Now adds LoadSCDCalib operator to SCD data.
 *
 *  Revision 1.30  2002/09/17 20:29:50  dennis
 *  Now adds operator SetGroupIDs to all DataSets
 *
 *  Revision 1.29  2002/09/10 22:39:54  dennis
 *  Now adds operator SetDataLabel to all DataSets.
 *
 *  Revision 1.28  2002/08/22 15:11:17  pfpeterson
 *  Moved LoadGsasCalib and LoadOffsets to only Diffractometers.
 *  Added LoadOrientation to SCD.
 *
 *  Revision 1.27  2002/08/01 22:52:00  dennis
 *  Re-inserted changes removed by Ruth.
 *
 *  Revision 1.25  2002/08/01 19:40:34  dennis
 *    Added methods to "refurbish" DataSets with appropriate sets of operators.
 *    addOperators( ds )
 *    addOperators( ds, instrument_type )
 *    addMonitorOperators( ds, instrument_type )
 *
 *  Revision 1.24  2002/07/31 16:33:59  dennis
 *  Now adds SCDQxyz operator to SCD DataSets and
 *  adds DiffractometerQxyz operator to SAD DataSets
 *
 *  Revision 1.23  2002/07/17 20:36:56  dennis
 *  Added DataSetAdd_1, DataSetSubtract_1, DataSetMultiply_1,
 *  DataSetDivide_1 operators.
 *
 *  Revision 1.22  2002/07/12 22:27:35  dennis
 *  Added FitExpressionToGroup operator
 *
 *  Revision 1.21  2002/07/10 15:59:55  pfpeterson
 *  Added new operator to data.
 *
 *  Revision 1.20  2002/04/03 20:41:16  pfpeterson
 *  Added the LoadOffsets DataSet operator to the DataSets.
 *
 *  Revision 1.19  2002/02/26 21:17:01  pfpeterson
 *  Unsupported instrument type error appears in status pane now.
 *
 *  Revision 1.18  2002/02/22 20:35:07  pfpeterson
 *  Operator Reorganization.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Special.*;
import DataSetTools.operator.DataSet.TOF_DG_Spectrometer.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.operator.DataSet.Conversion.YAxis.*;
import DataSetTools.operator.DataSet.Conversion.XYAxis.*;
import DataSetTools.operator.DataSet.Information.XAxis.*;
import DataSetTools.operator.DataSet.Math.Scalar.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.EditList.*;
import DataSetTools.instruments.*;

/**
 * The concrete root class for "Factory" objects that produce properly 
 * configured, empty DataSets.
 *
 * @version 0.1  
 */

public class DataSetFactory implements Serializable
{
  private String        title;
  private String        x_units;
  private String        x_label;
  private String        y_units;
  private String        y_label;
  private String        log_info;

  /**
   * Constructs a data set "factory" that will produce empty DataSets with 
   * the specified title, units, labels and a list of operators suitable for 
   * any data set. 
   *
   * @param   title     String giving a title for the DataSets produced.
   * @param   x_units   String specifying the units for the "X" axis.  This 
   *                    should be specified in a standard form. 
   * @param   x_label   String identifying the quantity measured in the "X"
   *                    direction. 
   * @param   y_units   String specifying the units for the "Y" axis.  This 
   *                    should be specified in a standard form. 
   * @param   y_label   String identifying the quantity measured in the "Y"
   *                    direction. 
   */
  public DataSetFactory( String  title, 
                         String  x_units,
                         String  x_label,
                         String  y_units,
                         String  y_label )
  {
    this.title    = title;
    this.x_units  = x_units;
    this.x_label  = x_label;
    this.y_units  = y_units;
    this.y_label  = y_label;
    this.log_info = null;
  }

  /**
   * Constructs a data set "factory" that will produce empty DataSets with 
   * specified title and default values for the units and labels for a 
   * Time-of-flight DataSet, together with a list of operators suitable 
   * for any data set.
   * 
   * @param   title     String giving a title for the DataSets produced.
   *
   */
  public DataSetFactory( String title )
  {
    this( title,
         "Time(us)", "Time-of-flight",
         "Counts", "Scattering Intensity" );
  }

  /**
   * Sets the title to be applied to subsequent DataSets produced by this 
   * factory.
   *
   * @param  title   The String to use for the title for new DataSets 
   */
  public void setTitle( String title ) { this.title = title; }


  /**
   * Sets a log message to be used as the first log entry for subsequent 
   * DataSets produced by this factory.
   *
   * @param log_info   The String to be used for the first log entry for new 
   *                   DataSets
   */
  public void setLog_entry( String log_info )
  {
    this.log_info = log_info;
  }

  /**
   * Sets the units for the "X" axis to be applied to subsequent DataSets 
   * produced by this factory. 
   *
   * @param  units   String giving the units for the "X" axis
   */
  public void setX_units( String units ) { this.x_units = units; }

  /**
   * Sets the label for the "X" axis to be applied to subsequent DataSets 
   * produced by this factory.
   *
   * @param  label  String giving the label for the "X" axis
   */
  public void setX_label( String label ) { this.x_label = label; }

  /**
   * Sets the units for the "Y" scale 
   *
   * @param  units   String giving the units for the "Y" axis
   */
  public void setY_units( String units ) { this.y_units = units; }


  /**
   * Sets the label for the "Y" axis to be applied to subsequent DataSets 
   * produced by this factory.
   *
   * @param  label  String giving the label for the "Y" axis
   */
  public void setY_label( String label ) { this.y_label = label; }


  /**
   * Get a new empty data set with the title, units, label, ID and initial log 
   * info determined by the parameters stored in the DataSetFactory.  The new 
   * DataSet also contains a list of operators suitable for use with any 
   * DataSet.
   *
   * @return An empty DataSet with operators appropriate for a generic 
   *         DataSet.
   */
  public DataSet getDataSet()
  {
    DataSet new_ds = new DataSet( title,             // construct new data set
                                  new OperationLog(), 
                                  x_units, 
                                  x_label, 
                                  y_units, 
                                  y_label );
                                                    // set initial log entry
    if ( log_info != null )
      new_ds.addLog_entry( log_info );
                                                    // add the list of generic
                                                    // data set operations
    addOperators( new_ds );
    return new_ds;
  }

  /**
   * Configure an existing DataSet by adding the set of operators
   * appropriate to all DataSets.
   *
   * @param  ds               The DataSet to which the operators are added.
   */
  static public void addOperators( DataSet ds )
  {
    ds.addOperator( new DataSetScalarAdd() );
    ds.addOperator( new DataSetScalarSubtract() );
    ds.addOperator( new DataSetScalarMultiply() );
    ds.addOperator( new DataSetScalarDivide() );

    ds.addOperator( new DataBlockScalarAdd() );
    ds.addOperator( new DataBlockScalarSubtract() );
    ds.addOperator( new DataBlockScalarMultiply() );
    ds.addOperator( new DataBlockScalarDivide() );

    ds.addOperator( new DataSetAdd() );
    ds.addOperator( new DataSetSubtract() );
    ds.addOperator( new DataSetMultiply() );
    ds.addOperator( new DataSetDivide()   );

    ds.addOperator( new DataSetAdd_1() );
    ds.addOperator( new DataSetSubtract_1() );
    ds.addOperator( new DataSetMultiply_1() );
    ds.addOperator( new DataSetDivide_1() );

    ds.addOperator( new IntegrateGroup() );
    ds.addOperator( new CalculateMomentOfGroup() );
    ds.addOperator( new DataSetCrossSection() );
    ds.addOperator( new FitPolynomialToGroup() );
    ds.addOperator( new FitExpressionToGroup() );

    ds.addOperator( new DeleteByAttribute() );
    ds.addOperator( new SumByAttribute() );
    ds.addOperator( new ExtractByAttribute() );

    ds.addOperator( new DataSetSort() );
    ds.addOperator( new DataSetMultiSort() );

    ds.addOperator( new DeleteCurrentlySelected() );
    ds.addOperator( new SumCurrentlySelected() );
    ds.addOperator( new ExtractCurrentlySelected() );

    ds.addOperator( new DataSetMerge() );
    ds.addOperator( new DataSetFastMerge() );
    ds.addOperator( new ResampleDataSet() );
    ds.addOperator( new ResampleOnGeometricProgression() );
    ds.addOperator( new ConvertHistogramToFunction() );
    ds.addOperator( new ConvertFunctionToHistogram() );
    ds.addOperator( new DivideByDeltaX() );
    ds.addOperator( new MultiplyByDeltaX() );

    ds.addOperator( new TofToChannel() );     // convert to channel for any
                                              // DataSet
//  ds.addOperator( new IntervalSelectionOp() );
    ds.addOperator( new SetGroupIDs() );
    ds.addOperator( new SetDataLabel() );
    ds.addOperator( new GetDataAttribute() );
    ds.addOperator( new SetDataAttribute() );
    ds.addOperator( new GetDSAttribute() );
    ds.addOperator( new SetDSAttribute() );
    ds.addOperator( new SetDSDataAttributes() );
    ds.addOperator( new GetField() );
    ds.addOperator( new SetField() );
    ds.addOperator( new SelectGroups() );
    ds.addOperator( new SelectByID() );
    ds.addOperator( new SelectByIndex() );
    ds.addOperator( new GetSelectedIndices() );
    ds.addOperator( new ClearSelect() );
    ds.addOperator( new PlotterOp() );
  }

  /**
   * Get a new empty data set with the title, units, label, ID and initial log
   * info determined by the parameters stored in the DataSetFactory.  The new
   * DataSet also contains a list of operators suitable for use a time of 
   * flight DataSet for instruments of the specified type.
   *
   * @param  instrument_type  Code for the type of instrument for which
   *                          the DataSet is to be constructed.  The codes
   *                          are in DataSetTools/instrument/InstrumentType.java
   *                          InstrumentType.TOF_DIFFRACTOMETER
   *                          InstrumentType.TOF_SCD
   *                          InstrumentType.TOF_SAD
   *                          InstrumentType.TOF_DG_SPECTROMETER
   *                          InstrumentType.TOF_IDG_SPECTROMETER
   *                          InstrumentType.TOF_REFLECTOMETER
   *
   * @return An empty DataSet with operators appropriate to a time-of-flight
   *         DataSet for the specified instrument type. 
   */
  public DataSet getTofDataSet( int instrument_type )
  {
    DataSet new_ds = getDataSet();   // Get a DataSet with generic operators
                                     // then add any special purpose operators

    addOperators( new_ds, instrument_type );
    return new_ds;
  }


  /**
   *  Get a simple test data set with a collection of sine waves of different
   *  frequencies.
   */
  static public DataSet getTestDataSet()
  {
    DataSetFactory factory = new DataSetFactory( "Test Data Set",
                                                 "time",
                                                 "milli-seconds",
                                                 "signal level",
                                                 "volts" );
    DataSet new_ds = factory.getDataSet();
    new_ds.setAttribute( new StringAttribute( Attribute.RUN_TITLE,
                                             "Test Data Set" ) );

    Data          data;         // data block that will hold info on one signal
    float[]       y_values;     // array to hold the y-values for that signal
    XScale        x_scale;      // "time channels" for the signal

    for ( int id = 1; id < 10; id++ )            // for each id
    {
      float  frequency = id;
      x_scale = new UniformXScale( 0, 1000, 500 );//build list of time channels
      y_values = new float[500];                  // build list of counts
      for ( int channel = 0; channel < 500; channel++ )
        y_values[ channel ] = 100*(float)
                                 Math.sin( channel/500.0*2*Math.PI*frequency );

      data = Data.getInstance( x_scale, y_values, id );

      data.setAttribute( new FloatAttribute( "Frequency", frequency ) );
      new_ds.addData_entry( data );
    }

    return new_ds;
  }


  /**
   * Configure an existing DataSet by adding the set of operators 
   * appropriate to a particular instrument type to the DataSet.
   *
   * @param  ds               The DataSet to which the operators are added.
   *
   * @param  instrument_type  Code for the type of instrument for which
   *                          the DataSet is to be configured.  The codes
   *                          are in DataSetTools/instrument/InstrumentType.java 
   *                          InstrumentType.TOF_DIFFRACTOMETER
   *                          InstrumentType.TOF_SCD
   *                          InstrumentType.TOF_SAD
   *                          InstrumentType.TOF_DG_SPECTROMETER
   *                          InstrumentType.TOF_IDG_SPECTROMETER
   *                          InstrumentType.TOF_REFLECTOMETER
   */
  static public void addOperators( DataSet ds, int instrument_type ) 
                     throws IllegalArgumentException
  {
    if ( instrument_type == InstrumentType.TOF_DIFFRACTOMETER )
    {
      ds.addOperator( new GetPixelInfo_op() );   
      ds.addOperator( new DiffractometerTofToD() );
      ds.addOperator( new DiffractometerTofToQ() );
      ds.addOperator( new DiffractometerTofToEnergy() );
      ds.addOperator( new DiffractometerTofToWavelength() );
      ds.addOperator( new TrueAngle() );
      ds.addOperator( new LoadOffsets() );
      ds.addOperator( new LoadGsasCalib() );
    }
    else if ( instrument_type == InstrumentType.TOF_SCD )  // will be different
    {                                                      // when SCD properly
      ds.addOperator( new GetPixelInfo_op() );             // supported  
      ds.addOperator( new DiffractometerTofToD() ); 
      ds.addOperator( new DiffractometerTofToQ() );
      ds.addOperator( new SCDQxyz() );
      ds.addOperator( new DiffractometerTofToEnergy() );
      ds.addOperator( new DiffractometerTofToWavelength() );
      ds.addOperator( new TrueAngle() );
      ds.addOperator( new LoadOrientation() );
      ds.addOperator( new LoadSCDCalib() );

      IntegratePt intPk = new IntegratePt();
      ds.addOperator(intPk);
      intPk.setIntgratePkOp(new Operators.TOF_SCD.INTEG(),1,1,1);
    }
    else if ( instrument_type == InstrumentType.TOF_SAD )  // will be different
    {                                                      // when SAD properly
      ds.addOperator( new GetPixelInfo_op() );             // supported  
      ds.addOperator( new DiffractometerTofToD() );
      ds.addOperator( new DiffractometerTofToQ() );
      ds.addOperator( new DiffractometerQxyz() );
      ds.addOperator( new DiffractometerTofToEnergy() );
      ds.addOperator( new DiffractometerTofToWavelength() );
      ds.addOperator( new TrueAngle() );
    }
    else if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
    {
      ds.addOperator( new GetPixelInfo_op() );   
      ds.addOperator( new SpectrometerTofToEnergyLoss() );
      ds.addOperator( new SpectrometerTofToEnergy() );
      ds.addOperator( new SpectrometerTofToWavelength() );
      ds.addOperator( new SumByAttributeNormSA() );
//      ds.addOperator( new SpectrometerTofToQ() );
      ds.addOperator( new SpectrometerTofToQE() );
      ds.addOperator( new SpectrometerTofToQ2E() );
      ds.addOperator( new TrueAngle() );
    }
    else if ( instrument_type == InstrumentType.TOF_IDG_SPECTROMETER )
    {                                                    // will be different
                                                         // when IDG_S properly
      ds.addOperator( new GetPixelInfo_op() );           // supported  
      ds.addOperator( new TrueAngle() );        
    }
    else if ( instrument_type == InstrumentType.TOF_REFLECTOMETER )
    {                                                    // will be different
                                                         // when REFLT properly
      ds.addOperator( new GetPixelInfo_op() );           // supported  
      ds.addOperator( new TrueAngle() );                
    }
    else
    {
        DataSetTools.util.SharedData.addmsg(
        "WARNING: Unsupported instrument type in DataSetFactory.addOperators "+
                  "for DataSet " + ds );
        DataSetTools.util.SharedData.addmsg( 
        "Requested type " + instrument_type );
        throw new IllegalArgumentException(instrument_type + " NOT SUPPORTED");
    }
  }


  /**
   * Configure an existing DataSet that has been loaded or converted to
   * "d-spacing" by adding those operators that convert from d-spacing
   * to other x-axis scales such as TOF or Q.
   *
   * @param ds   The DataSet to which the operators will be added.
   *
   */
  static public void add_d_Operators( DataSet ds ) 
  {
    String x_units = ds.getX_units();
    if ( x_units.equalsIgnoreCase("Angstroms") )
    {
      ds.addOperator(new GetPixelInfo_op());
      ds.addOperator(new DiffractometerDToTof());
      ds.addOperator(new DiffractometerDToQ());
    }
  }


  /**
   * Configure an existing DataSet by adding the set of operators
   * appropriate to the monitors on a particular instrument type,
   * to the DataSet.
   *
   * @param  ds               The DataSet to which the operators are added.
   *
   * @param  instrument_type  Code for the type of instrument for which
   *                          the DataSet is to be configured.  
   *                          The integer codes are in 
   *                          DataSetTools/instrument/InstrumentType.java   
   *                          InstrumentType.TOF_DIFFRACTOMETER
   *                          InstrumentType.TOF_SCD
   *                          InstrumentType.TOF_SAD
   *                          InstrumentType.TOF_DG_SPECTROMETER
   *                          InstrumentType.TOF_IDG_SPECTROMETER
   *                          InstrumentType.TOF_REFLECTOMETER
   */
  static public void addMonitorOperators( DataSet ds, int instrument_type )
  {
    if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
    {
      ds.addOperator( new EnergyFromMonitorDS() );
      ds.addOperator( new MonitorPeakArea() );
    }
    else if ( instrument_type == InstrumentType.TOF_DIFFRACTOMETER )
      ds.addOperator( new FocusIncidentSpectrum() );

    ds.addOperator( new MonitorTofToEnergy() );
    ds.addOperator( new MonitorTofToWavelength() );
  }

}
