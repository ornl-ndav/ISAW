/*
 *  @(#)  SimpleDataSetDemo.java    1.0  2000/02/03    Dennis Mikkelson
 *  ( Derived from DataSetDemo1.java )
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

/**
  *    This class provides a basic demo of reading DataSets from runfiles, 
  *  showing the DataSets as images and scrolled graphs.
  *
  *    IMPORTANT!! Since this demo does NOT provide for any user interaction, 
  *  you will have to edit this source file to specify different runfiles to 
  *  load, or to call different operators, etc. 
  *
  */  

public class SimpleDataSetDemo
{

/**
  *  The main program method for this object
  */
public static void main(String args[])
{
  DataSet      A_histogram_ds;  
//  String       run_A = "/usr/home/dennis/ARGONNE_DATA/glad0816.run";
//  String       run_A = "/usr/home/dennis/ARGONNE_DATA/GLAD4811.RUN";
//  String       run_A = "/usr/home/dennis/ARGONNE_DATA/gppd9902.run";
//  String       run_A = "/usr/home/dennis/ARGONNE_DATA/hrcs2848.run";
//  String       run_A = "/IPNShome/dennis/ARGONNE_DATA/hrcs2445.run";
//  String       run_A = "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run";
//  String       run_A = "/IPNShome/dennis/ARGONNE_DATA/hrcs2451.run";
  String       run_A = "/usr/home/dennis/ARGONNE_DATA/hrcs2936.run";

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
  }

} 
