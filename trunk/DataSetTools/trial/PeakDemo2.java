/*
 *  @(#)  PeakDemo2.java    1.0  2000/06/13    Dennis Mikkelson
 *  ( Derived from PeakDemo.java )
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.math.*;
import DataSetTools.peak.*;

/**
  *    This class provides a basic demo of reading DataSets from runfiles, 
  *  showing the DataSets as images and scrolled graphs.
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
