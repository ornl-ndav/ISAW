package  DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.retriever.*;
import  ChopTools.*;

/**
 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 * Macro to Calibrate,    Evaluate, Normalize Chopper Spectrometer Data
 * After tiny modification , it can be partialy used for diffractometer 
 * too.
 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 * 
 * @author Dongfeng Chen
 * @author Chun Loong
 * @author Alok Chatterjee
 * @author Dennis Mikkelson
 * @author Tom Worlton
 * @version 1.0
 * @since August 10, 1999
 *        August 16, 1999 Added constructor to allow calling operator directly
 * @see IsawGUI.Isaw
 * @see SpectrometerEvaluator
// * @see SpectrometerGrouper
 * @see SpectrometerNormalizer
 * @see SpectrometerTofToEnergyLoss
 */

public class SpectrometerMacro extends    DataSetOperator 
                                  implements Serializable
{
  /**
   * Constructor of macro
   */
  public SpectrometerMacro( )
  {
    super( "HRMECS Macro: Calibration, Evaluation, Grouping & Tof~E " );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  monitor_ds  The DataSet containing the monitors for this
   *                      histogram
   *  @param  up_level    Upper cut off for the detector quality measure
   *  @param  up_level    Lower cut off for the detector quality measure
   */

  public SpectrometerMacro( DataSet    ds,
                            DataSet    monitor_ds,
                            float      up_level,
                            float      low_level  )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( monitor_ds );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( up_level ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( low_level ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "SpecMacro";
   }



 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    parameter = new Parameter( "Select Proper Monitor",
                              new DataSet("Monitor DataSet", "Empty DataSet") );
  
    addParameter( parameter );
   
    parameter = new Parameter( "Uplevel for Evaluation", new Float(50.0) );
    addParameter( parameter );

    parameter = new Parameter( "Lowlevel for Evaluation", new Float(1.0) );
    addParameter( parameter );
  }


  /**
   * Run the macro use chop_MacroTools
   */

  /* ---------------------------- getResult ------------------------------- */


  public Object getResult()
  {
                                     
    DataSet ds = this.getDataSet();     // get the current data set
    DataSet new_ds = 
           (DataSet)ds.clone();         // (DataSet)ds.empty_clone();
    
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Macro start!(HRMECS Macro)" );
    
    DataSet monitor_ds    =    (DataSet)(getParameter(0).getValue());    
    
    System.out.println("The H1 and M1 are : "+ds+" and"+  monitor_ds );
    
    new_ds.addLog_entry( "Bring a monitors dataset "+ monitor_ds+"!(HRMECS Macro)");
    
    if ( !ds.SameUnits( monitor_ds ) )       
      {
        ErrorString message = new ErrorString(
                           "ERROR: monitor_ds has different units " );
        System.out.println( message );
        return message;
      }
    
    if ( !ds.getX_units().equalsIgnoreCase("Time(us)")  ||  !ds.getY_units().equalsIgnoreCase("Counts") )      
      {
        ErrorString message = new ErrorString(
                           "ERROR: DataSet units not Time(us) & Counts " );
        System.out.println( message );
        return message;
      }
 
    new_ds.addLog_entry( "Finish Comparing the units between "+ds+" and "+monitor_ds +"!(HRMECS Macro)" );
      
    float uplevel = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float lowlevel = ( (Float)(getParameter(2).getValue()) ).floatValue();
    
    new_ds.addLog_entry( "Bring uplevel "+uplevel+" and lowlevel "+lowlevel+" for evaluation "+new_ds+"!(HRMECS Macro)");
    
    new_ds  = chop_evaluation.evaluator(ds, monitor_ds, uplevel, lowlevel );//,monitor_ds);
    
    new_ds.addLog_entry( "Calibration and HRMECS DataSet by chop_evaluation.evaluator!(HRMECS Macro)" );
     
    new_ds = (DataSet)chop_MacroTools.macroNomalizer(new_ds, monitor_ds);
     
    new_ds.addLog_entry( "Calibration and Nomalized HRMECS data by macroNomalizer object!(HRMECS Macro)" );
    
    Data groupd1 = chop_MacroTools.grouperformacro(new_ds, 45.0f, 25.0f );
    Data groupd2 = chop_MacroTools.grouperformacro(new_ds, 80.0f, 45.0f );
    Data groupd3 = chop_MacroTools.grouperformacro(new_ds, 120.0f, 80.0f );
    
    new_ds.addLog_entry( "Group HRMECS detectors to LQ, Mq ans HQ dataset!(HRMECS Macro)" );
    
    groupd1.setGroup_ID(1000);
    groupd2.setGroup_ID(1001);
    groupd3.setGroup_ID(1002);
    
    new_ds.addLog_entry( "Set data groupID 1000, 1001 and 1002!(HRMECS Macro)" );
    
    /*
    try{
    chop_MacroTools.datawriterID(groupd1,new_ds,1 );
    }catch(Exception e){} 
    try{
    chop_MacroTools.datawriterID(groupd2,new_ds,2 );
    }catch(Exception e){} 
    try{
    chop_MacroTools.datawriterID(groupd3,new_ds,3 );
    }catch(Exception e){} 
      
    new_ds.addLog_entry( "Write LQ, MQ and HQ TOF data to file as g_Q(1~3)X-Yh(RunNumber).opt!(HRMECS Macro)" );
    //*/     
         
    int num_data = new_ds.getNum_entries();
    int sumRemovedDetector=0;
    for (int j= num_data-1; j>=0; j--)
        {
            if(j<1000)
            {
            new_ds.removeData_entry(j);
            sumRemovedDetector++;
            }
        }
                 
    new_ds.addLog_entry( "Remove " + sumRemovedDetector+" original detectors from dataset "+ new_ds+"!(HRMECS Macro)" );
    
    new_ds.addData_entry( groupd1 );      
    new_ds.addData_entry( groupd2 );      
    new_ds.addData_entry( groupd3 );  
   
    new_ds.addLog_entry( "Add new Data object into empty DataSet object!(HMECS Macro)" );
    
    AttributeList    attr_list = groupd1.getAttributeList();
    float energy_in =
               ((Float)attr_list.getAttributeValue(Attribute.ENERGY_IN)).floatValue();
    float min = -energy_in/5;
    float max = energy_in-energy_in/20;
    
    new_ds.addLog_entry( "Gain Energy min and "+min+" Max "+max+" from data Attribute!(HRMECS Macro)" );
    
    new_ds = (DataSet)chop_MacroTools.toEnergyLoss(new_ds,  min,  max);
    
    new_ds.addLog_entry( "Calculation for tof data to S-E data bby toEnergyLoss object!(HRMECS Macro)" );
    
    /*//writing to a file
    for (int j=0; j< new_ds.getNum_entries(); j++)
    {
        if(new_ds.getData_entry_with_id(j+1000)!=null)
         {
            System.out.println("Got s entry. The entry is: "+new_ds.getData_entry_with_id(j+1000));
            try{
                //chop_dataX_YplotWriter.
                chop_MacroTools.datawriterID(new_ds.getData_entry_with_id(j+1000) ,ds,4+j );
            }catch(Exception e){}
         }
    }
     
    
    new_ds.addLog_entry( "Save S-E LQ, Mq, Hq data in file as g_Q(4~6)X-Yh(RunNumber).opt!(HRMECS Macro)" );
    //*/

    //chop_MacroTools.drawAlldata(new_ds);   
    
    new_ds.addLog_entry( "Draw LQ, Mq , HQ data by graphview!(HRMECS Macro)" );
    
    new_ds.addLog_entry( "Macro End!(HRMECS Macro)" );
    
    return new_ds;
  }  

    /**
     * Clone current dateset
     */
    public Object clone()
  {
    SpectrometerMacro new_op = new SpectrometerMacro( );
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );
    return new_op;
  }

}
