/*
 *  @(#)  TestDataSetCopy.java    1.0  2000/07/14    Dennis Mikkelson
 *  ( Derived from DataSetDemo1.java )
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

/**
  *    This class tests the DataSet copy method, 
  */  

public class TestDataSetCopy 
{

/**
  *  The main program method for this object
  */
public static void main(String args[])
{
  DataSet      A_histogram_ds,
               B_histogram_ds;  
  String       run_A = "/IPNShome/dennis/ARGONNE_DATA/gppd9902.run";
  String       run_B = "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run";



  // Get the DataSet from the runfile and show it.

  RunfileRetriever rr;    // The RunfileRetriever object calls John's runfile
                          // package and returns the data as DataSets

  ViewManager view_manager;  // Variable to hold reference to a ViewManager
                             // that will display a DataSet

                                         // Load and show run A histograms 
  rr = new RunfileRetriever( run_A ); 
  A_histogram_ds = rr.getDataSet( 1 );
  rr = null;
  view_manager = new ViewManager( A_histogram_ds, IViewManager.IMAGE );

  rr = new RunfileRetriever( run_B ); 
  B_histogram_ds = rr.getDataSet( 1 );
  rr = null;
  view_manager = new ViewManager( B_histogram_ds, IViewManager.IMAGE );

  B_histogram_ds.copy( A_histogram_ds );

  }

} 
