/*
 *  @(#)  PeakDemo.java    1.0  2000/03/07    Dennis Mikkelson
 *  ( Derived from SimpleDataSet.java )
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

public class PeakDemo
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

    GaussianPeak peak = new GaussianPeak( 0, 1, 1, 0, 0 );
    for ( int monitor = 0; monitor < 2; monitor++ )
    {
      Data monitor_data = A_monitor_ds.getData_entry(monitor);

      float x1    = 0;
      float x2    = 5000;
      float width = 100;
      for ( int i = 0; i < 20; i++ )
      {
        peak.FitPeakToData( monitor_data, x1, x2 );
        width = peak.getFWHM();
        if ( width > 500 )
          width = 500;
        x1 = peak.getPosition() - 1.1f * width;
        x2 = peak.getPosition() + 1.1f * width; 
        System.out.println("Position: " + peak.getPosition() + 
                            " FWHM: " + peak.getFWHM() +
                            " sigma " + peak.getSigma() );
      }

      Data peak_data = peak.PeakData( monitor_data.getX_scale() );

      System.out.println("Adding new peak data " + peak_data );
      A_monitor_ds.addData_entry( peak_data );

      System.out.println("Area of GaussianPeak = " + 
                        peak.Area(peak.getPosition() - 1.5f * peak.getFWHM(),
                                  peak.getPosition() + 1.5f * peak.getFWHM()) );
      System.out.println("Area of Data peak = " +
      NumericalAnalysis.IntegrateHistogram( monitor_data.getX_scale().getXs(),
                                            monitor_data.getY_values(),
                                  peak.getPosition() - 1.5f * peak.getFWHM(),
                                  peak.getPosition() + 1.5f * peak.getFWHM() ));
    }
    view_manager = new ViewManager( A_monitor_ds, IViewManager.IMAGE );
  }
} 
