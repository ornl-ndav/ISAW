/*
 *  @(#)  LiveData.java    0.1  2000/10/12    Dennis Mikkelson
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

/**
  *    This demonstrates repeatedly reading and processing files from a 
  *    sequence of runfiles.
  */  

public class LiveData 
{

/**
  *  The main program method for this object
  */
public static void main(String args[])
{
  DataSet          histogram_ds;             // we'll reuse these two DataSets
  DataSet          sum_ds; 

  ViewManager      hist_view_manager = null; // we'll reuse these view managers 
  ViewManager      sum_view_manager  = null; 

  DataSetOperator  op;
  RunfileRetriever rr; 

  String           path = "/usr/home/dennis/ARGONNE_DATA/GPPD_DATA/";
  String           file; 
  int              first = 11471;      // read runfiles with these run numbers
  int              last  = 11602;
  float            min_time = 0;       // sum over this time interval;
  float            max_time = 33000; 

                                               // repeatedly read a new file
                                               // and update the views
  for ( int num = first; num <= last; num++ )
  { 
    file = path + "GPPD" + num + ".RUN";       // get the new file
    System.out.println( "Opening" + file );
    rr = new RunfileRetriever( file ); 
    histogram_ds = rr.getDataSet( 2 );
                                              // form the total counts function 
    op = new DataSetCrossSection( histogram_ds,    
                                  min_time, 
                                  max_time, 
                                  Attribute.GROUP_ID );
    sum_ds = (DataSet)op.getResult();
                                              // construct the view managers
                                              // the first time, other times,
                                              // reuse them.
    if ( num == first )
    {
      hist_view_manager = new ViewManager(histogram_ds, IViewManager.IMAGE );
      sum_view_manager  = new ViewManager(sum_ds, IViewManager.SCROLLED_GRAPHS);
    }
    else
    {
      hist_view_manager.setDataSet( histogram_ds );
      sum_view_manager.setDataSet( sum_ds );
    }
                                             // pause.... this would be replace
                                             // by code to check for a new file
    try
    {
      Thread.sleep( 10000 );
    }
    catch ( InterruptedException e )
    {
      System.out.println("ERROR in sleep "+e );
    }

  }
} 

}
