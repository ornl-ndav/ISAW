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
 * Revision 1.1  2003/07/18 14:20:27  dennis
 * Initial form of efficiency ratio operator, not complete.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.retriever.*;
import DataSetTools.math.*;
import java.util.*;
import java.util.*;

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
   *  Creates operator with title ""Efficiency Ratio" and the 
   *  specified list of parameters. The getResult method must still be 
   *  used to execute the operator.
   *
   *  @param  ds       DataSet containing data from run with cadmium mask
   *                   upstream from monitor 1 and the beam stop removed.
   *
   *  @param  mon_ds   DataSet containing monitor data for the specified 
   *                   run.
   *
   *  @param  eff_ds   DataSet containing the efficiencies of the area
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
                          DataSet eff_ds,
                          float   x_center, 
                          float   y_center, 
                          float   radius,
                          float   dn_fraction )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Cadmium Mask Histogram", ds) );
    addParameter( new Parameter("Cadmium Mask Monitor", mon_ds) );
    addParameter( new Parameter("Detector Efficiencies", eff_ds) );
    addParameter( new Parameter("X(cm) offset of beam", new Float(x_center)));
    addParameter( new Parameter("Y(cm) offset of beam", new Float(y_center)));
    addParameter( new Parameter("Radius to use", new Float(radius)));
    addParameter( new Parameter("Delayed Neutron Percent", 
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
  
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data types 
   * of the parameters.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Cadmium Mask Histogram", 
                                 DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("Cadmium Mask Monitor", 
                                 DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("Detector Efficiencies", 
                                 DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("X(cm) offset of beam", new Float(0)) );
    addParameter( new Parameter("Y(cm) offset of beam", new Float(0)) );
    addParameter( new Parameter("Radius(cm) to use", new Float(5)) );
    addParameter( new Parameter("Delayed Neutron Percent", new Float(0.11)) );
  }
  
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   */
  public Object getResult()
  {
    final int   MONITOR_ID = 1;

    DataSet ds         = (DataSet)(getParameter(0).getValue());
    DataSet mon_ds     = (DataSet)(getParameter(1).getValue());
    DataSet eff_ds     = (DataSet)(getParameter(2).getValue());
    float   x_offset   = ((Float)(getParameter(3).getValue())).floatValue();
    float   y_offset   = ((Float)(getParameter(4).getValue())).floatValue();
    float   radius     = ((Float)(getParameter(5).getValue())).floatValue();
    float   delayed_n  = ((Float)(getParameter(6).getValue())).floatValue();

    //
    // Find the DataGrid for this detector and make sure that we have a 
    // segmented detector. 
    //
                                                  // clone the DataSet so we
                                                  // don't damage the original
    ds = (DataSet)ds.clone();

    UniformGrid grid = null;
    Data d = null;
    PixelInfoList pil;
    Attribute attr;
    boolean segmented_detector_found  = false;
    int data_index = 0;
    int n_data     = ds.getNum_entries();

    while ( !segmented_detector_found && data_index < n_data )
    {
     d = ds.getData_entry( data_index );
     attr = d.getAttribute( Attribute.PIXEL_INFO_LIST );
     if ( attr != null && attr instanceof PixelInfoListAttribute )
     {
        pil  = (PixelInfoList)attr.getValue();
        grid = (UniformGrid)pil.pixel(0).DataGrid();
        if ( grid.num_rows() > 1 || grid.num_cols() > 1 )
          segmented_detector_found = true;
      }
      else
        return new ErrorString("Need PixelInfoList attribute.");

      data_index++;
    }

    if ( !segmented_detector_found )
      return new ErrorString("Need an Area Detector or LPSD");

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
    detector_position.get()[1] += x_offset/100;         // shift detector to
    detector_position.get()[2] -= y_offset/100;         // beam center
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

    XScale wl_scale = ds.getData_entry( 0 ).getX_scale(); 
    Data mon_1_data = mon_ds.getData_entry_with_id( MONITOR_ID );
    mon_1_data.resample( wl_scale, IData.SMOOTH_NONE );
    
    Vector result = new Vector();
    result.addElement( ds );
    result.addElement( mon_ds );

    return result;
  }

  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone()
  {
    Operator op = new EfficiencyRatio();
    op.CopyParametersFrom( this );
    return op;
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
