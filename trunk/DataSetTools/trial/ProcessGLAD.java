/*
 *  @(#)  ProcessGLAD.java    1.0  2000/06/07    Dennis Mikkelson
 *  ( Derived from SimpleDataSetDemo.java )
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

public class ProcessGLAD 
{

  static private float TotalCount( Data d )
  {
    double total = 0;
    float y[] = d.getY_values();
    for ( int i = 0; i < y.length; i++ )
      total += y[i];

    return (float)total;
  }

  public static void main(String args[])
  {
    DataSet      sample_ds;  
    DataSet      sample_monitors;

    DataSet      vanadium_ds;  
    DataSet      vanadium_monitors;

    DataSet      calibration_ds;  
    DataSet      calibration_monitors;

    DataSet      processed_ds;
    DataSet      Q_ds;

    String   sample_run      = "/IPNShome/dennis/ARGONNE_DATA/GLAD4699.RUN";
    String   vanadium_run    = "/IPNShome/dennis/ARGONNE_DATA/GLAD4696.RUN";
    String   calibration_run = "/IPNShome/dennis/ARGONNE_DATA/GLAD4701.RUN";

    RunfileRetriever rr; 
    ViewManager view_manager; 

    // Load the empty can run and monitors...................................
    rr = new RunfileRetriever( calibration_run ); 
    calibration_monitors = rr.getDataSet( 0 );
    calibration_ds       = rr.getDataSet( 1 );
    rr = null;
    float calib_count = 
             ProcessGLAD.TotalCount( calibration_monitors.getData_entry(0));
    System.out.println("Loaded calibration run...");

    // Load the vanadium run and monitors....................................
    rr = new RunfileRetriever( vanadium_run );
    vanadium_monitors = rr.getDataSet( 0 );
    vanadium_ds       = rr.getDataSet( 1 );
    rr = null;
    float vanadium_count = 
             ProcessGLAD.TotalCount( vanadium_monitors.getData_entry(0));
    System.out.println("Loaded vanadium run...");

    // Load the sample run and monitors......................................
    rr = new RunfileRetriever( sample_run );
    sample_monitors = rr.getDataSet( 0 );
    sample_ds       = rr.getDataSet( 1 );
    rr = null;
    float sample_count = 
             ProcessGLAD.TotalCount( sample_monitors.getData_entry(0));
    System.out.println("Loaded sample run...");

    // Scale to empty can and vanadium runs to the sample run ...............
    DataSetOperator op;
    op = new DataSetScalarMultiply( calibration_ds, 
                                    sample_count/calib_count,
                                    true ); 
    calibration_ds = (DataSet)op.getResult();

    op = new DataSetScalarMultiply( vanadium_ds, 
                                    sample_count/vanadium_count,
                                    true );
    vanadium_ds = (DataSet)op.getResult();


    // Subtract the empty can run from the vanadium run ....................
    op = new DataSetSubtract( vanadium_ds, calibration_ds, true );
    vanadium_ds = (DataSet)op.getResult();
    calibration_ds       = null;
    calibration_monitors = null;
    System.out.println("Subtracted calibration run...");


    // Divide sample run by the normalized vanadium run ....................  
    op = new DataSetDivide( sample_ds, vanadium_ds, true );
    processed_ds = (DataSet)op.getResult();
    sample_ds            = null;
    sample_monitors      = null;
    vanadium_ds          = null;
    vanadium_monitors    = null;

    System.out.println("Divided sample run by vanadium-calibration run...");

    op = new DataSetSort( processed_ds, 
                          Attribute.DETECTOR_POS, 
                          true, 
                          false );
    op.getResult();
    view_manager = new ViewManager( processed_ds, IViewManager.IMAGE );

    op = new DiffractometerTofToQ( processed_ds, 0, 30, 1000 );
    Q_ds = (DataSet)op.getResult();
    view_manager = new ViewManager( Q_ds, IViewManager.IMAGE );

    System.out.println();
    System.out.println("=================================================");
    System.out.println("RUNS PROCESSED:");
    System.out.println("=================================================");
    System.out.println("Calibration Run:           " + calibration_run );
    System.out.println("Vanadium Run:              " + vanadium_run );
    System.out.println("Sample Run:                " + sample_run );
    System.out.println("=================================================");
    System.out.println("TOTAL MONITOR COUNTS:");
    System.out.println("=================================================");
    System.out.println("Calibration Monitor Count: " + calib_count );
    System.out.println("Vanadium Monitor Count:    " + vanadium_count );
    System.out.println("Sample Monitor Count:      " + sample_count );
  }

} 
