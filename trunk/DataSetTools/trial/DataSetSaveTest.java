/*
 *  @(#)  DataSetSaveTest.java    1.0  2000/06/12    Dennis Mikkelson
 *
 *  Test binary i/o of DataSets
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import java.io.*;
import java.util.zip.*;


public class DataSetSaveTest 
{

/**
  *  The main program method for this object
  */
public static void main(String args[])
{
  DataSet      A_histogram_ds = null;  
  DataSet      B_histogram_ds = null;
  String       run_A = "/IPNShome/dennis/ARGONNE_DATA/gppd9902.run";

  // Get a DataSet from the runfile and show it.

  RunfileRetriever rr;   
  rr = new RunfileRetriever( run_A ); 
  A_histogram_ds = rr.getDataSet( 1 );
  rr = null;

  ViewManager view_manager_A = null;  
  view_manager_A = new ViewManager( A_histogram_ds, IViewManager.IMAGE );

  DataSet_IO.SaveDataSet( A_histogram_ds, "serialized_data.bin" );
  B_histogram_ds = DataSet_IO.LoadDataSet( "serialized_data.bin" );

  ViewManager view_manager_B = null;
  view_manager_B = new ViewManager( B_histogram_ds, IViewManager.IMAGE );
  }

} 
