/**
 * File:  SpectrometerMacro.java
 *
 * Copyright (C) 1999, Dongfeng Chen,
 *                     Chun Loong,
 *                     Alok Chatterjee,
 *                     Dennis Mikkelson,
 *                     Tom Worlton
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.8  2001/06/01 21:18:00  rmikk
 * Improved documentation for getCommand() method
 *
 * Revision 1.7  2001/04/26 19:11:09  dennis
 * Added copyright and GPL info at the start of the file.
 *
 * Revision 1.6  2000/11/10 22:41:34  dennis
 *    Introduced additional abstract classes to better categorize the operators.
 * Existing operators were modified to be derived from one of the new abstract
 * classes.  The abstract base class hierarchy is now:
 *
 *  Operator
 *
 *   -GenericOperator
 *      --GenericLoad
 *      --GenericBatch
 *
 *   -DataSetOperator
 *     --DS_EditList
 *     --DS_Math
 *        ---ScalarOp
 *        ---DataSetOp
 *        ---AnalyzeOp
 *     --DS_Attribute
 *     --DS_Conversion
 *        ---XAxisConversionOp
 *        ---YAxisConversionOp
 *        ---XYAxesConversionOp
 *     --DS_Special
 *
 *    To allow for automatic generation of hierarchial menus, each new operator
 * should fall into one of these categories, or a new category should be
 * constructed within this hierarchy for the new operator.
 *
 * August 16, 1999 Added constructor to allow calling operator directly
 *
 */

package  DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.retriever.*;
import  ChopTools.*;


/**
 * Macro to Calibrate, Evaluate, Normalize Chopper Spectrometer Data
 * After tiny modification , it can be partially used for diffractometer 
 * too.
 * @see SpectrometerEvaluator
 * @see SpectrometerGrouper
 * @see SpectrometerNormalizer
 * @see SpectrometerTofToEnergyLoss
 */

public class SpectrometerMacro extends    DS_Special 
                               implements Serializable
{
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
   * @return the command name to be used with script processor: in this case, SpecMacro
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

    parameter = new Parameter( "Monitor DataSet",
                                DataSet.EMPTY_DATA_SET );
  
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
     * Clone current operator
     */
    public Object clone()
  {
    SpectrometerMacro new_op = new SpectrometerMacro( );
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );
    return new_op;
  }

}
