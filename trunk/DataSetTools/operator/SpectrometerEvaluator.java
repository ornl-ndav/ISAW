/*
 * @(#)NormalizeChopperData.java   0.1  99/07/15   Dennis Mikkelson
 *
 * This operator carries out the basic normalization of Chopper Spectrometer
 * data for HRMCS & LRMCS at IPNS. 
 *
 */

package  DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.retriever.*;
import  ChopTools.*;

/**
  *  Normalize Chopper Spectrometer Data, given 
  *
  *    1. The current DataSet containing histograms for a sample run.
  *    2. The monitor DataSet corresponding to the current DataSet.
  *    3. A DataSet containing histograms for a background run.
  *    4. The monitor DataSet for the background run.
  */

public class SpectrometerEvaluator extends    DataSetOperator 
                                  implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public SpectrometerEvaluator( )
  {
    super( "Remove Bad Detectors Data" );
    Parameter parameter;
 
    parameter = new Parameter( "Monitors", 
                              new DataSet("Monitor DataSet", "Empty DataSet") );
    /*
    RunfileRetriever data_retriever =null;
    try{
    data_retriever = new RunfileRetriever(
                            //     ".\\ChopTools\\HRCS0976.RUN;20");
                           ".\\ChopTools\\HRCS2712.RUN;1");
                              //  ".\\ChopTools\\HRCS2447.RUN;24");
    }catch(Exception e){}
    
    DataSet monitor_ds    = data_retriever.getDataSet(0);  
                          // (DataSet)(getParameter(0).getValue());
    
    parameter = new Parameter( "Monitors", 
                              monitor_ds );
    
    //*/
                              
                              
                              
    addParameter( parameter );
    
    
    parameter = new Parameter( "Uplevel", new Float(50.0) );
    addParameter( parameter );
    
    parameter = new Parameter( "Lowlevel", new Float(1.0) );
    addParameter( parameter );
    
    
  }

  /* ---------------------------- getResult ------------------------------- */

                                     // The concrete operation extracts the
                                     // current value of the scalar to add 
                                     // and returns the result of adding it
                                     // to each point in each data block.
  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
    DataSet new_ds = (DataSet)ds.empty_clone();

    
    
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Evaluator works" );
    
    
    DataSet monitor_ds    =    (DataSet)(getParameter(0).getValue());
    
    
  System.out.println("The H1 and M1 are : "+ds+" and"+  monitor_ds );
    
    if ( !ds.SameUnits( monitor_ds ) )    // units don't match so something
      return null;                           // is wrong, just quit

    if ( !ds.getX_units().equalsIgnoreCase("Time(us)")  ||
         !ds.getY_units().equalsIgnoreCase("Counts") )      // wrong units, so
      return null;        
      
  
     //  DataSet new_ds = ds;
    float uplevel = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float lowlevel = ( (Float)(getParameter(2).getValue()) ).floatValue();
    
    new_ds  = chop_evaluation.evaluator(ds, monitor_ds, uplevel, lowlevel );//,monitor_ds);
    return new_ds;
  }  

    public Object clone()
  {
    SpectrometerEvaluator new_op = new SpectrometerEvaluator( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}
