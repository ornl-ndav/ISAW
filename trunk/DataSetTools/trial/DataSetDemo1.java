/*
 *  @(#)  DataSetDemo1.java    1.0  2000/02/03    Dennis Mikkelson
 *                             1.1  2000/03/01    Dennis Mikkelson
 *
 *  1.1  Modified this demo to use ViewManagers, rather than just placing 
 *       viewers in a frame.  This simplifies the demo AND allows the user
 *       to switch the viewer between an IMAGE and SCROLLED_GRAPHS view for
 *       a particular DataSet. 
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

/**
  *    This class provides a basic demo of reading DataSets from runfiles, 
  *  showing the DataSets as images and scrolled graphs and invoking operators
  *  on the DataSets.  It loads and displays monitors and sample spectra from
  *  two runfiles. It then adds the monitor data together and adds the sample 
  *  spectra from the two runs together to form new DataSets and displays the
  *  combined monitor and sample spectra DataSets.
  *
  *    IMPORTANT!! Since this demo does NOT provide for any user interaction, 
  *  you will have to edit this source file to specify different runfiles to 
  *  load, or to call different operators, etc. 
  *
  *  To see how to write new operators, see the following examples in the
  *  package DataSetTools/operators
  * 
  *          Integrate.java                produces one value
  *          DataSetAdd.java               produces new DataSet
  *          SpectrometerTofToEnergy.java  produces new DataSet
  *
  *  Note that where possible, the basic calculations themselves (adding 
  *  spectra, doing a numerical integration, calculating energy at a particular
  *  distance/tof) have been implemented at a lower level, to allow reuse.
  */  

public class DataSetDemo1
{

  /**
    *  The main program method for this object
    */
  public static void main(String args[])
  {
    DataSet      A_monitor_ds,      // We'll fill out these DataSets from
                 B_monitor_ds,      // two runfiles A & B and add the 
                 A_histogram_ds,    // resulting monitor and histogram
                 B_histogram_ds;    // data sets.

    DataSet      monitor_ds,        // These DataSets will hold the sum
                 histogram_ds;      // of the data from runsfiles A & B 

    String       run_A = "/usr/home/dennis/ARGONNE_DATA/gppd9898.run";
    String       run_B = "/usr/home/dennis/ARGONNE_DATA/gppd9899.run";


    // Get the DataSets from the runfiles.  Show the monitors as scrolled 
    // graphs and the sample spectra as an image.  When getting the DataSets 
    // from the runfile, DataSet 0, should be the monitors, DataSet 1, the 
    // first histogram, etc.  There are methods to check this, but for now 
    // this should work.

    RunfileRetriever rr;    // The RunfileRetriever object calls John's runfile
                            // package and returns the data as DataSets

    ViewManager view_manager;  // Variable to hold reference to a ViewManager
                               // that will display a DataSet

                                           // Load and show run A monitors &
    rr = new RunfileRetriever( run_A );    // histograms
    A_monitor_ds = rr.getDataSet( 0 );
    view_manager = new ViewManager( A_monitor_ds, IViewManager.SCROLLED_GRAPHS);

    A_histogram_ds = rr.getDataSet( 1 );
    view_manager = new ViewManager( A_histogram_ds, IViewManager.IMAGE );

                                            // Load and show run B monitors &
    rr = new RunfileRetriever( run_B );     // histograms
    B_monitor_ds = rr.getDataSet( 0 );
    view_manager = new ViewManager( B_monitor_ds, IViewManager.SCROLLED_GRAPHS);

    B_histogram_ds = rr.getDataSet( 1 );
    view_manager = new ViewManager( B_histogram_ds, IViewManager.IMAGE );

                                            // add the monitors together
    DataSetOperator adder;                  // and show the results
    adder = new DataSetAdd( A_monitor_ds, B_monitor_ds, true );
    monitor_ds = (DataSet)adder.getResult();
    view_manager = new ViewManager( monitor_ds, IViewManager.SCROLLED_GRAPHS);

                                            // add the histograms together
                                            // and show the results
    adder = new DataSetAdd( A_histogram_ds, B_histogram_ds, true );
    histogram_ds = (DataSet)adder.getResult();
    view_manager = new ViewManager( histogram_ds, IViewManager.IMAGE );
  } 
} 
