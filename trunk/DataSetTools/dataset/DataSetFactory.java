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
 *  Revision 1.20  2002/04/03 20:41:16  pfpeterson
 *  Added the LoadOffsets DataSet operator to the DataSets.
 *
 *  Revision 1.19  2002/02/26 21:17:01  pfpeterson
 *  Unsupported instrument type error appears in status pane now.
 *
 *  Revision 1.18  2002/02/22 20:35:07  pfpeterson
 *  Operator Reorganization.
 *
 *  Revision 1.17  2001/08/16 19:36:19  dennis
 *  Added Ruth's PlotterOp instead of Dongfengs SpectrometerPlotter.
 *
 *  Revision 1.16  2001/08/16 19:20:05  dennis
 *  Removed DongFeng's SpectrometerPlotter.
 *
 *  Revision 1.15  2001/08/15 21:46:18  dennis
 *  Added DongFeng's SpectrometerPlotter to all DataSets.
 *
 *  Revision 1.14  2001/08/14 20:29:40  dennis
 *  Added ExtractCurrentlySelected operator.
 *
 *  Revision 1.13  2001/08/14 19:44:47  dennis
 *  Removed IntervalSelectionOp since it was not working correctly.
 *
 *  Revision 1.12  2001/07/11 19:09:26  neffk
 *  updated to automatically add the IntervalSelectionOp operator to
 *  all newly constructed DataSet objects.
 *
 *  Revision 1.11  2001/04/25 19:03:38  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.10  2001/04/02 20:48:53  dennis
 *  Now adds TofToChannel operator for any DataSet.
 *
 *  Revision 1.9  2000/12/07 22:23:37  dennis
 *  Added operators FitPolynomialToGroup(),
 *                  ConvertFunctionToHistogram()
 *
 *  Revision 1.8  2000/10/03 21:34:02  dennis
 *  Modified this factory to handle different types of time-of-flight
 *  instruments.
 *
 *  Revision 1.7  2000/08/03 15:49:36  dennis
 *  Added ResampleDataSet() and ConvertHistogramToFunction() operators
 *
 *  Revision 1.6  2000/08/03 03:16:32  dennis
 *  Added ResampleDataSet() operator
 *
 *  Revision 1.5  2000/07/17 20:59:02  dennis
 *  Added SetDSDataAttributes() operator
 *
 *  Revision 1.4  2000/07/17 13:38:31  dennis
 *  Added operators to get/set attributes & fields
 *
 *  Revision 1.3  2000/07/10 22:23:55  dennis
 *  Now using CVS 
 *
 *  Revision 1.18  2000/06/15 14:12:25  dennis
 *  Replaced 4 operators with renamed versions for consistency:
 *    Integrate()         replaced by    IntegrateGroup()
 *    CalculateMoment()   replaced by    CalculateMomentOfGroup()
 *    SumSelectedData()   replaced by    SumByAttribute()
 *    SelectData()        replaced by    ExtractByAttribute()
 *
 *  Revision 1.17  2000/06/08 15:10:16  dennis
 *  Added new operator DeleteByAttribute
 *
 *  Revision 1.16  2000/05/23 18:51:50  dennis
 *  removed sort on one attribute operator.
 *
 *  Revision 1.15  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Special.*;
import DataSetTools.operator.DataSet.TOF_DG_Spectrometer.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.operator.DataSet.Conversion.YAxis.*;
import DataSetTools.operator.DataSet.Conversion.XYAxis.*;
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
   * Constructs a data set "factory" that will produce empty DataSets with the 
   * specified title, default values for the units and labels together with a 
   * list of operators suitable for any data set.
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
    new_ds.addOperator( new DataSetScalarAdd() );
    new_ds.addOperator( new DataSetScalarSubtract() );
    new_ds.addOperator( new DataSetScalarMultiply() );
    new_ds.addOperator( new DataSetScalarDivide() );

    new_ds.addOperator( new DataSetAdd() );
    new_ds.addOperator( new DataSetSubtract() );
    new_ds.addOperator( new DataSetMultiply() );
    new_ds.addOperator( new DataSetDivide()   );

    new_ds.addOperator( new IntegrateGroup() );
    new_ds.addOperator( new CalculateMomentOfGroup() );
    new_ds.addOperator( new DataSetCrossSection() );
    new_ds.addOperator( new FitPolynomialToGroup() );

    new_ds.addOperator( new DeleteByAttribute() );
    new_ds.addOperator( new SumByAttribute() );
    new_ds.addOperator( new ExtractByAttribute() );

    new_ds.addOperator( new DataSetSort() );
    new_ds.addOperator( new DataSetMultiSort() );

    new_ds.addOperator( new DeleteCurrentlySelected() );
    new_ds.addOperator( new SumCurrentlySelected() );
    new_ds.addOperator( new ExtractCurrentlySelected() );

    new_ds.addOperator( new DataSetMerge() );
    new_ds.addOperator( new ResampleDataSet() );
    new_ds.addOperator( new ConvertHistogramToFunction() );
    new_ds.addOperator( new ConvertFunctionToHistogram() );

    new_ds.addOperator( new TofToChannel() );     // convert to channel for any
                                                  // DataSet
//    new_ds.addOperator( new IntervalSelectionOp() );
    new_ds.addOperator( new GetDataAttribute() );
    new_ds.addOperator( new SetDataAttribute() );
    new_ds.addOperator( new GetDSAttribute() );
    new_ds.addOperator( new SetDSAttribute() );
    new_ds.addOperator( new SetDSDataAttributes() );
    new_ds.addOperator( new GetField() );
    new_ds.addOperator( new SetField() );
    new_ds.addOperator( new LoadOffsets() );

    new_ds.addOperator( new PlotterOp() );
    return new_ds;
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
   *                          InstrumentType.TOF_REFLECTROMETER
   *
   * @return An empty DataSet with operators appropriate to a time-of-flight
   *         DataSet for the specified instrument type. 
   */
  public DataSet getTofDataSet( int instrument_type )
  {
    DataSet new_ds = getDataSet();   // Get a DataSet with generic operators
                                     // then add any special purpose operators

    if ( instrument_type == InstrumentType.TOF_DIFFRACTOMETER )
    {
      new_ds.addOperator( new DiffractometerTofToD() );
      new_ds.addOperator( new DiffractometerTofToQ() );
      new_ds.addOperator( new DiffractometerTofToEnergy() );
      new_ds.addOperator( new DiffractometerTofToWavelength() );
      new_ds.addOperator( new TrueAngle() );
    }
    else if ( instrument_type == InstrumentType.TOF_SCD )  // will be different
    {                                                      // when SCD properly
      new_ds.addOperator( new DiffractometerTofToD() );    // supported
      new_ds.addOperator( new DiffractometerTofToQ() );
      new_ds.addOperator( new DiffractometerTofToEnergy() );
      new_ds.addOperator( new DiffractometerTofToWavelength() );
      new_ds.addOperator( new TrueAngle() );
    }
    else if ( instrument_type == InstrumentType.TOF_SAD )  // will be different
    {                                                      // when SAD properly
      new_ds.addOperator( new DiffractometerTofToD() );    // supported
      new_ds.addOperator( new DiffractometerTofToQ() );
      new_ds.addOperator( new DiffractometerTofToEnergy() );
      new_ds.addOperator( new DiffractometerTofToWavelength() );
      new_ds.addOperator( new TrueAngle() );
    }
    else if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
    {
      new_ds.addOperator( new SpectrometerEvaluator() );
      new_ds.addOperator( new SpectrometerNormalizer());
      new_ds.addOperator( new SpectrometerMacro() );
      new_ds.addOperator( new SpectrometerTofToEnergyLoss() );
      new_ds.addOperator( new SpectrometerTofToEnergy() );
      new_ds.addOperator( new SpectrometerTofToWavelength() );
      new_ds.addOperator( new DoubleDifferentialCrossection() );
//      new_ds.addOperator( new SpectrometerTofToQ() );
      new_ds.addOperator( new SpectrometerTofToQE() );
      new_ds.addOperator( new TrueAngle() );
    }
    else if ( instrument_type == InstrumentType.TOF_IDG_SPECTROMETER )
    {                                                    // will be different
                                                         // when IDG_S properly
      new_ds.addOperator( new TrueAngle() );             // supported  
    }
    else if ( instrument_type == InstrumentType.TOF_REFLECTROMETER )
    {                                                    // will be different
                                                         // when REFLT properly
      new_ds.addOperator( new TrueAngle() );             // supported  
    }
    else
        DataSetTools.util.SharedData.status_pane.add(
                 //System.out.println(
                 "WARNING: Unsupported instrument type in DataSetFactory" );

    return new_ds;
  }
}
