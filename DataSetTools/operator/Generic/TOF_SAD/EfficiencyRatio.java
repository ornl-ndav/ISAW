/*
 * File:  EfficiencyRatio.java 
 *
 * Copyright (C) 2003, Dennis Mikkelson 
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.7  2003/08/19 19:05:22  rmikk
 * -Fixed an error
 * -Renamed  Data from the flood run, sensory data.
 *
 * Revision 1.6  2003/07/31 15:46:27  dennis
 * Set titles on returned DataSets containing the summed area detector
 * spectrum and the efficiency ratio.  Added log messages to these
 * DataSets.  Removed unneeded clone method.
 *
 * Revision 1.5  2003/07/28 14:15:43  rmikk
 * Changed Prompt and initial value for Neutron Delay input
 * Used the DataSetTools.parameter parameters for input
 *
 * Revision 1.4  2003/07/22 18:18:39  dennis
 * Added java docs to getResult() and added getDocumentation method.
 * Now returns the spdxxxxx.dat and efrxxxxx.dat DataSets as the
 * first two elements in the return vector.
 *
 * Revision 1.3  2003/07/21 22:56:58  dennis
 * Now uses methods from Grid_util to get data grids.
 * Essentially complete.
 *
 * Revision 1.2  2003/07/18 20:16:06  dennis
 * Now calls tof_data_calc.SubtractDelayedNeutrons(,,)
 *
 * Revision 1.1  2003/07/18 14:20:27  dennis
 * Initial form of efficiency ratio operator, not complete.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.retriever.*;
import DataSetTools.math.*;
import java.util.*;
import java.util.*;
import DataSetTools.parameter.*;
/** 
 * This operator calculates the efficiency (with errors) between the first
 * monitor and the area detector for SAND.  It implements the concepts from
 * the FORTRAN program:
 *   
 *   efratio_v3 
 *
 * developed by the small angle group at the Intense Pulsed Neutron Source
 * division at ArgonneNationalLaboratory.
 */
public class EfficiencyRatio extends GenericTOF_SAD
{
  private static final String  TITLE = "Efficiency Ratio";

  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Efficiency Ratio" and a default 
   *  list of parameters.
   */  
  public EfficiencyRatio()
  {
    super( TITLE );
  }
  
  /* ---------------------------- constructor ---------------------------- */ 
  /** 
   *  Creates operator with title "Efficiency Ratio" and the 
   *  specified list of parameters. The getResult method must still be 
   *  used to execute the operator.
   *
   *  @param  ds       DataSet containing data from run with cadmium mask
   *                   upstream from monitor 1 and the beam stop removed.
   *
   *  @param  mon_ds   DataSet containing monitor data for the specified 
   *                   run.
   *
   *  @param  sens_ds  DataSet containing the sensitivities of the area
   *                   detector pixels.
   *
   *  @param  x_center The offset in the x direction of the beam center
   *                   from the center of the detector, in centimeters.
   *
   *  @param  y_center The offset in the y direction of the beam center
   *                   from the center of the detector, in centimeters.
   *
   *  @param  radius   The radius of the circle to use, around the
   *                   beam center, for calculating the efficiency ratio.
   *
   *  @param  dn_fraction  The fraction of the neutrons that are "delayed"
   *                       neutrons and are subtracted from the count.
   */
  public EfficiencyRatio( DataSet ds, 
                          DataSet mon_ds,
                          DataSet sens_ds,
                          float   x_center, 
                          float   y_center, 
                          float   radius,
                          float   dn_fraction )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Cadmium Mask Histogram", ds) );
    addParameter( new Parameter("Cadmium Mask Monitor", mon_ds) );
    addParameter( new Parameter("Detector Sensitivities", sens_ds) );
    addParameter( new Parameter("X(cm) offset of beam", new Float(x_center)));
    addParameter( new Parameter("Y(cm) offset of beam", new Float(y_center)));
    addParameter( new Parameter("Radius to use", new Float(radius)));
    addParameter( new Parameter("Delayed Neutron Fraction", 
                                 new Float(dn_fraction)) );
  }

  
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "EffRatio", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand()
  {
    return "EffRatio";
  }


  /* ------------------------ getDocumentation ---------------------------- */
  /**
   *  Get the documentation to be displayed by the help system. 
   */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator calculates the efficiency ratio " );
    Res.append(" between area detector pixels in a disk around the");
    Res.append(" and the first beam monitor.");
    Res.append("@algorithm This first subtracts delayed neutrons from the ");
    Res.append(" specified area detector DataSet and from the specified");
    Res.append(" monitor DataSet.  The area detector data grid is then");
    Res.append(" so that the beam center is at (DetD,0,0) where" );
    Res.append(" DetD is the distance from the sample position to the");
    Res.append(" detector.  Both the monitor DataSet and area detector");
    Res.append(" DataSet are converted to wavelength, and the monitor");
    Res.append(" DataSet is resampled on a wavelength grid corresponding");
    Res.append(" to the wavelengths at the area detector center.");
    Res.append(" The individual spectra from the area detector are then");
    Res.append(" divided by the sensitivity values calculated by the");
    Res.append(" DetectorSensitivity operator.  Finally, the area detector");
    Res.append(" spectra corresponding to pixels that are within the");
    Res.append(" specified radius of the beam center are summed and divided");
    Res.append(" by the monitor spectrum.");
    Res.append("@param ds - DataSet containing data from run with cadmium");
    Res.append(" mask upstream from monitor 1 and the beam stop removed.");
    Res.append("@param mon_ds - ataSet containing monitor data for");
    Res.append(" the specified run.");
    Res.append("@param sens_ds - DataSet containing the sensitivities of");
    Res.append(" the area detector pixels.");
    Res.append("@param x_center - The offset in the x direction of the beam");
    Res.append(" center from the center of the detector, in centimeters.");
    Res.append("@param y_center - The offset in the y direction of the beam");
    Res.append(" center from the center of the detector, in centimeters.");
    Res.append("@param radius - The radius of the circle to use, around the");
    Res.append(" beam center, for calculating the efficiency ratio.");
    Res.append("@param dn_fraction - the fraction of the neutrons that are");
    Res.append(" delayed neutrons and are subtracted from the count.");
    Res.append("@return A vector of four DataSets are returned.");
    Res.append(" The first DataSet is the sum of the specified spectra");
    Res.append(" from the area detector, with respect to wavelength,");
    Res.append(" which should be written to spdxxxxx.dat.");
    Res.append(" The second DataSet is the ratio of the summed");
    Res.append(" spectrum to the monitor 1 spectrum, which should");
    Res.append(" be written to efrxxxxx.dat.");
    Res.append(" he third DataSet is a clone of the original area detector");
    Res.append(" DataSet, converted to wavelength and divided by the");
    Res.append(" sensitivity data.  This is returned for information");
    Res.append(" purposes.  If the unselected Data are deleted and the");
    Res.append(" resulting DataSet is viewed in the ThreeD view, a quick");
    Res.append(" visual check on the center and disk of included spectra");
    Res.append(" can be made.  Finally, the monitor DataSet, converted to");
    Res.append(" wavelength is returned, so that comparisons can be made.");
    Res.append(" ");

    return Res.toString();
  }


  
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data types 
   * of the parameters.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new SampleDataSetPG("Cadmium Mask Histogram", 
                                 DataSet.EMPTY_DATA_SET) );
    addParameter( new MonitorDataSetPG("Cadmium Mask Monitor", 
                                 DataSet.EMPTY_DATA_SET) );
    addParameter( new DataSetPG("Detector Efficiencies", 
                                 DataSet.EMPTY_DATA_SET) );
    addParameter( new FloatPG("X(cm) offset of beam", new Float(0)) );
    addParameter( new FloatPG("Y(cm) offset of beam", new Float(0)) );
    addParameter( new FloatPG("Radius(cm) to use", new Float(5)) );
    addParameter( new FloatPG("Delayed Neutron Fraction", new Float(0.0011)) );
  }

  
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   *
   *  Execute the operator using the current values of the parameters.
   *  This first subtracts delayed neutrons from the specified area detector
   *  DataSet and from the specified monitor DataSet.  The area detector 
   *  data grid is the shifted so that the beam center is at (DetD,0,0) where
   *  DetD is the distance from the sample position to the detector. 
   *  Both the monitor DataSet and area detector DataSet are converted to
   *  wavelength, and the monitor DataSet is resampled on a wavelength grid
   *  corresponding to the wavelengths at the area detector center.  The 
   *  individual spectra from the area detector are then divided by the
   *  sensitivity values calculate by the DetectorSensitivity operator.
   *  Finally, the area detector spectra corresponding to pixels that are
   *  within the specified radius of the beam center are summed and divided
   *  by the monitor spectrum.
   *
   *  @return A vector of four DataSets are returned.  The first DataSet is
   *            the sum of the specified spectra from the area detector,
   *            with respect to wavelength, which should be written to
   *            spdxxxxx.dat.  The second DataSet is the ratio of the summed
   *            spectrum to the monitor 1 spectrum, which should be written
   *            to efrxxxxx.dat.  The third DataSet is a clone of the
   *            original area detector DataSet, converted to wavelength and
   *            divided by the sensitivity data.  This is returned for 
   *            information purposes.  If the unselected Data are deleted
   *            and the resulting DataSet is viewed in the ThreeD view, a
   *            quick visual check on the center and disk of included spectra
   *            can be made.  Finally, the monitor DataSet, converted to 
   *            wavelength is returned, so that comparisons can be made.
   */
  public Object getResult()
  {
    final int   MONITOR_ID = 1;

    DataSet ds         = (DataSet)(getParameter(0).getValue());
    DataSet mon_ds     = (DataSet)(getParameter(1).getValue());
    DataSet sens_ds    = (DataSet)(getParameter(2).getValue());
    float   x_offset   = ((Float)(getParameter(3).getValue())).floatValue();
    float   y_offset   = ((Float)(getParameter(4).getValue())).floatValue();
    float   radius     = ((Float)(getParameter(5).getValue())).floatValue();
    float   delayed_n  = ((Float)(getParameter(6).getValue())).floatValue();

    float   frequecy   = 30;                      // this could be a parameter

                                                  // clone the DataSets so we
                                                  // don't damage the original
    ds     = (DataSet)ds.clone();
    mon_ds = (DataSet)mon_ds.clone();

    mon_ds.removeData_entry(2);
    mon_ds.removeData_entry(1);
                                                 // subtract of delayed netrons

    tof_data_calc.SubtractDelayedNeutrons( 
                                      (TabulatedData)mon_ds.getData_entry(0), 
                                       frequecy, 
                                       delayed_n );

    for ( int i = 0; i < ds.getNum_entries(); i++ )
      tof_data_calc.SubtractDelayedNeutrons((TabulatedData)ds.getData_entry(i),
                                             frequecy, 
                                             delayed_n );
    //
    // Find the DataGrid for this detector and make sure that we have a 
    // segmented detector. 
    //

    int grid_ids[] = Grid_util.getAreaGridIDs( ds );

    if ( grid_ids.length < 1 )
      return new ErrorString( "No Area Detectors in DataSet" );

    if ( grid_ids.length > 1 )
      return new ErrorString("Too many Area Detectors in DataSet: " + 
                              IntList.ToString( grid_ids )          );

    UniformGrid grid = (UniformGrid)Grid_util.getAreaGrid( ds, grid_ids[0] );

    // 
    // Fix the UniformGrid to point to the cloned Data blocks
    // 
    grid.setData_entries( ds ); 

    // 
    // Shift the data grid and set the positions of the pixels from the
    // shifted grid.
    //
    Vector3D detector_position = grid.position();
    System.out.println("detector position is " + detector_position );
    detector_position.get()[1] += x_offset/100;         // shift detector so
    detector_position.get()[2] -= y_offset/100;         // beam is centered
    grid.setCenter( detector_position );

    Grid_util.setEffectivePositions( ds, grid.ID() );

    Operator to_wavelength = new MonitorTofToWavelength( mon_ds, 0, 1000, 0 );
    Object obj = to_wavelength.getResult();
    if ( obj instanceof DataSet )
      mon_ds = (DataSet)obj;
    else
      return new ErrorString("ERROR converting monitor DataSet to wavelength "
                              + obj ); 

    to_wavelength = new DiffractometerTofToWavelength( ds, 0, 1000, 0 );
    obj = to_wavelength.getResult();
    if ( obj instanceof DataSet )
      ds = (DataSet)obj;
    else
      return new ErrorString("ERROR converting DataSet to wavelength " + obj); 

    //
    // Fix the UniformGrid to point to the cloned Data blocks
    //
    grid.setData_entries( ds );

    XScale wl_scale = ds.getData_entry( 0 ).getX_scale(); 
    Data mon_1_data = mon_ds.getData_entry_with_id( MONITOR_ID );
    mon_1_data.resample( wl_scale, IData.SMOOTH_NONE );
    
    // 
    // Get the efficiency Data (stored in array, indexed starting at 0
    //
    int eff_grid_ids[] = Grid_util.getAreaGridIDs( sens_ds );

    if ( eff_grid_ids.length < 1 )
      return new ErrorString( "No Area Detectors in Sensitivitiy DataSet" );

    if ( eff_grid_ids.length > 1 )
      return new ErrorString("Too many Area Detectors in Sensitivity DataSet: " +
                              IntList.ToString( eff_grid_ids )          );

    UniformGrid eff_grid = 
                   (UniformGrid)Grid_util.getAreaGrid(sens_ds, eff_grid_ids[0]);

    int n_rows = eff_grid.num_rows();
    int n_cols = eff_grid.num_cols();
    float eff[][] = new float[n_rows][n_cols];
    for ( int row = 1; row <= n_rows; row++ )
      for ( int col = 1; col <= n_cols; col++ )
        eff[row-1][col-1] = eff_grid.getData_entry( row, col ).getY_values()[0];

    //
    //  reconstruct the ifgood array
    //
    boolean ifgood[][] = new boolean[n_rows][n_cols];
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        if ( eff[row][col] == 0 )
          ifgood[row][col] = false;
        else
          ifgood[row][col] = true;

    //
    //  set ifgood false for grid elements outside of the desired radius.
    //  NOTE: radius in cm, grid units in meters.  Also, mark the pixels
    //  as selected, if they are in the radius and ifgood was true.
    // 
    System.out.println("Shifted Grid is " + grid );
    Vector3D beam_center = grid.position();
    beam_center.get()[1] -= x_offset/100;  // in sample centered coords, det y
    beam_center.get()[2] += y_offset/100;  // "up" and det x in -y direction
    for ( int row = 1; row <= n_rows; row++ )
      for ( int col = 1; col <= n_cols; col++ )
      {
        Vector3D position = grid.position(row,col);
        position.subtract( beam_center );
        if ( position.length() > radius/100 )
        {
          ifgood[row-1][col-1] = false; 
          grid.getData_entry(row,col).setSelected(false);
        }
        else
          grid.getData_entry(row,col).setSelected(ifgood[row-1][col-1]);
      }

    int n_good = 0;
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        if ( ifgood[row][col] )
          n_good++;

    System.out.println("Number of pixels used = " + n_good );

    //
    //  Now do the efficiency calculation, by first multiplying the selected
    //  spectra by 1/eff(pixel), then summing the selected spectra
    // 
    for ( int row = 1; row <= n_rows; row++ )
      for ( int col = 1; col <= n_cols; col++ )
      {
        Data d = grid.getData_entry(row,col);
        if ( d.isSelected() )
          d.divide( eff[row-1][col-1], 0 ); 
      } 

    
    int run_num = -1;
    Attribute attr = ds.getAttribute( Attribute.RUN_NUM );
    if ( attr != null )
      run_num = (int)attr.getNumericValue();

    Operator sum_sel_op = new SumCurrentlySelected( ds, true, true ); 
    DataSet sum_ds;
    obj = sum_sel_op.getResult();
    if ( obj instanceof DataSet )
      sum_ds = (DataSet)obj; 
    else
      return obj;
    sum_ds.clearSelections();
    sum_ds.setTitle( "AD Sum(" + run_num + ")" );
    sum_ds.addLog_entry( "Summed central pixels in radius " + radius + " cm");

    sum_ds.getData_entry(0).setGroup_ID(MONITOR_ID);
    Operator divide_op = new DataSetDivide( sum_ds, mon_ds, true );
    DataSet efr_ds;
    obj = divide_op.getResult();
    if ( obj instanceof DataSet )
      efr_ds = (DataSet)obj; 
    else
      return obj;
    efr_ds.clearSelections();
    efr_ds.setTitle( "Eff Ratio(" + run_num + ")" );
    efr_ds.addLog_entry("Efficiency Ratio of central portion of Area " +
                        "Detector to M1 monitor");

    Vector result = new Vector();
    result.addElement( sum_ds );   // The "raw" spectrum to go to spdxxxxx.dat

    result.addElement( efr_ds );   // The "efficiency ratio" to go to 
                                   // efrxxxxx.dat

    result.addElement( ds );       // for information purposes, return area ds
                                   // with the spectra selected that were
                                   // summed to find the area detector spectrum

    result.addElement( mon_ds );   // for information purposes, return the 
                                   // monitor spectrum
    return result;
  }

  
  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] )
  {
    
  }
}
