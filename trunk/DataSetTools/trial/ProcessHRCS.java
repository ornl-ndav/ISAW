/*
 * File:  ProcessHRCS.java 
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
 * $Log$
 * Revision 1.4  2002/02/28 19:57:44  dennis
 * Modified import statements due to operator reorganization.
 *
 * Revision 1.3  2001/04/26 15:28:59  dennis
 * Added copyright and GPL info at the start of the file.
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.dataset.Data;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Math.Scalar.*;
import DataSetTools.operator.DataSet.Special.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import DataSetTools.operator.DataSet.TOF_DG_Spectrometer.*;
import DataSetTools.operator.Generic.Load.*;
import DataSetTools.peak.*;
import DataSetTools.math.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

/**
 *  Test / develop algorithms for processing HRMECS data
 */
public class ProcessHRCS 
{

  public static void main(String args[])
  {
      String runs = "2447,2451";
      String mask = "";
      SumRunfiles loader = new SumRunfiles(
                 new DataDirectoryString("/IPNShome/dennis/ARGONNE_DATA/"),
                 new InstrumentNameString("hrcs"),
                 new IntListString(runs),
                 new IntListString(mask),
                     true );

      Object result = loader.getResult();
      DataSet sample_dss[] = (DataSet[])result;

      runs = "2444";
      loader = new SumRunfiles(
                 new DataDirectoryString("/IPNShome/dennis/ARGONNE_DATA/"),
                 new InstrumentNameString("hrcs"),
                 new IntListString(runs),
                 new IntListString(mask),
                     true );
      result = loader.getResult();
      DataSet background_dss[] = (DataSet[])result;
      
  
      ViewManager viewmanager;
//      viewmanager = new ViewManager( sample_dss[0], IViewManager.IMAGE );
//      viewmanager = new ViewManager( sample_dss[1], IViewManager.IMAGE );
//      viewmanager = new ViewManager( background_dss[0], IViewManager.IMAGE );
//      viewmanager = new ViewManager( background_dss[1], IViewManager.IMAGE );

                              // calculate energy from the monitor Data blocks 

      Operator op = new EnergyFromMonitorDS( sample_dss[0] );
      float energy = ((Float)(op.getResult())).floatValue();

                                                // set e_in on monitor DS
      op = new SetDSDataAttributes( 
                            sample_dss[0],
                            new AttributeNameString( Attribute.ENERGY_IN ),
                            new Float( energy ) );
      op.getResult();          
                                                // set e_in on histogram DS
      op = new SetDSDataAttributes( 
                            sample_dss[1],
                            new AttributeNameString( Attribute.ENERGY_IN ),
                            new Float( energy ) );
      op.getResult();          

      op = new MonitorPeakArea( sample_dss[0], 1, 8.5f );
      Float Float_val = (Float)op.getResult();
      float sample_mon_1_area = Float_val.floatValue(); 

      op = new MonitorPeakArea( background_dss[0], 1, 8.5f );
      Float_val = (Float)op.getResult();
      float background_mon_1_area = Float_val.floatValue(); 
 
      float scale = sample_mon_1_area / background_mon_1_area;
      op = new DataSetScalarMultiply( background_dss[0], scale, false );
      op.getResult();

      op = new DataSetScalarMultiply( background_dss[1], scale, false );
      op.getResult();

      op = new DataSetSubtract( sample_dss[1], background_dss[1], true );
      DataSet difference_ds = (DataSet)op.getResult();
//      viewmanager = new ViewManager( difference_ds, IViewManager.IMAGE );

      float atoms = 1.0f;
      op = new DoubleDifferentialCrossection( difference_ds, 
                                              new DataSet("dummy","Empty"),
                                              false,
                                              sample_mon_1_area, 
                                              atoms, 
                                              true    );

      DataSet double_diff_cross_ds = (DataSet)op.getResult();
      viewmanager = new ViewManager(double_diff_cross_ds, IViewManager.IMAGE);

      float wave_len = tof_calc.WavelengthFromEnergy(energy);
      float wave_num = (float)(2*Math.PI/wave_len);
      float velocity = tof_calc.VelocityFromEnergy(energy);
      System.out.println("Energy     = "+ energy );
      System.out.println("Wavelength = "+ wave_len );
      System.out.println("Wavenumber = "+ wave_num );
      System.out.println("Velocity = " + velocity );

      System.out.println("Sample Monitor Data Set Log......................");
      sample_dss[0].getOp_log().Print();
      System.out.println("Sample Histogram Data Set Log....................");
      sample_dss[1].getOp_log().Print();

      System.out.println("Background Monitor Data Set Log..................");
      background_dss[0].getOp_log().Print();
      System.out.println("Background Histogram Data Set Log................");
      background_dss[1].getOp_log().Print();
  }

} 
