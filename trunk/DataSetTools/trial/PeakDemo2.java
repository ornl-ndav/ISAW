/*
 * File:   PeakDemo2.java
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2002/11/27 23:23:30  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/02/28 19:57:41  dennis
 * Modified import statements due to operator reorganization.
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.dataset.Data;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.math.*;
import DataSetTools.peak.*;

/**
  *  Fit peak in monitor data ( Derived from PeakDemo.java )
  *
  *    IMPORTANT!! Since this demo does NOT provide for any user interaction, 
  *  you will have to edit this source file to specify different runfiles to 
  *  load, or to call different operators, etc. 
  *
  */  

public class PeakDemo2
{
  /**
    *  The main program method for this object
    */
  public static void main(String args[])
  {
    DataSet      A_monitor_ds;  
    String       run_A = "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run";

    // Get the DataSet from the runfile and show it.

    RunfileRetriever rr;    // The RunfileRetriever object calls John's runfile
                            // package and returns the data as DataSets

    ViewManager view_manager;  // Variable to hold reference to a ViewManager
                               // that will display a DataSet

                                           // Load and run A monitors  
    rr = new RunfileRetriever( run_A ); 
    A_monitor_ds = rr.getDataSet( 0 );
    Data monitor_data,
         peak_data,
         background_data;
    HistogramDataPeak peak;
    XScale x_scale;

    for ( int monitor = 0; monitor < 2; monitor ++ )
    {
      monitor_data = A_monitor_ds.getData_entry(monitor);
      peak = new HistogramDataPeak( monitor_data ); 

      System.out.println("Position: " + peak.getPosition() + 
                         " FWHM:    " + peak.getFWHM()           ); 

      x_scale = monitor_data.getX_scale();
      peak.setEvaluationMode( IPeak.PEAK_ONLY );
      peak_data = peak.PeakData( x_scale );
      A_monitor_ds.addData_entry( peak_data );
      System.out.println("Area of Peak Only ....................... " + 
                        peak.Area(peak.getPosition() - 1.5f * peak.getFWHM(),
                                  peak.getPosition() + 1.5f * peak.getFWHM()) );

      peak.setEvaluationMode( IPeak.BACKGROUND_ONLY );
      peak_data = peak.PeakData( x_scale );
      A_monitor_ds.addData_entry( peak_data );
      System.out.println("Area of Background Only ..................... " +
                        peak.Area(peak.getPosition() - 1.5f * peak.getFWHM(),
                                  peak.getPosition() + 1.5f * peak.getFWHM()) );

      peak.setEvaluationMode( IPeak.PEAK_PLUS_BACKGROUND );
      peak_data = peak.PeakData( x_scale );
      A_monitor_ds.addData_entry( peak_data );
      System.out.println("Area of Peak Plus Background ....................."+
                        peak.Area(peak.getPosition() - 1.5f * peak.getFWHM(),
                                  peak.getPosition() + 1.5f * peak.getFWHM()) );
    }
    view_manager = new ViewManager( A_monitor_ds, IViewManager.IMAGE );
  }
} 
