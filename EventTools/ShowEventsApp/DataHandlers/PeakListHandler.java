
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;

import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

import Operators.TOF_SCD.IndexPeaks_Calc;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.IndexPeaksCmd;


public class PeakListHandler implements IReceiveMessage
{
  private MessageCenter message_center;
  private Vector<PeakQ>    peakQ_list   = new Vector<PeakQ>();
  private Vector<Peak_new> peakNew_list = new Vector<Peak_new>();

  public PeakListHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SET_PEAK_Q_LIST );
    message_center.addReceiver( this, Commands.SET_PEAK_NEW_LIST );
    message_center.addReceiver( this, Commands.WRITE_PEAK_FILE );
    message_center.addReceiver( this, Commands.INDEX_PEAKS );
  }


  public boolean receive( Message message )
  {
    System.out.println("***PeakListHandler in thread " 
                       + Thread.currentThread());

    if ( message.getName().equals(Commands.SET_PEAK_Q_LIST) )
    {
      Object obj = message.getValue();
      if ( obj == null )
        return( false );

      if ( obj instanceof Vector ) 
      {
         Vector<PeakQ> new_peaks = (Vector<PeakQ>)obj;
         peakQ_list = new Vector<PeakQ>();
         for ( int i = 0; i < new_peaks.size(); i++ )
           peakQ_list.add( new_peaks.elementAt(i) );

         Message get_peak_new_list = new Message( Commands.GET_PEAK_NEW_LIST,
                                                 peakQ_list,
                                                 true );
         message_center.receive( get_peak_new_list );

         System.out.println("IN PeakListHandler set PEAK Q, #PeakQ = " + 
                             peakQ_list.size() +
                             " #Peak_new = " + peakNew_list.size() );
      } 
    }
    else if ( message.getName().equals(Commands.SET_PEAK_NEW_LIST) )
    {
      Object obj = message.getValue();
      if ( obj == null )
        return false;

      if ( obj instanceof Vector )
      {
         Vector<Peak_new> new_peaks = (Vector<Peak_new>)obj;
         peakNew_list = new Vector<Peak_new>();
         for ( int i = 0; i < new_peaks.size(); i++ )
           peakNew_list.add( new_peaks.elementAt(i) );

         System.out.println("IN PeakListHandler set NEW PEAKS, #PeakQ = " + 
                             peakQ_list.size() +
                             " #Peak_new = " + peakNew_list.size() );
      }
    }
    else if ( message.getName().equals(Commands.WRITE_PEAK_FILE ) )
    {
      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Message error_message = new Message( Commands.DISPLAY_ERROR,
                                             "ERROR: No Peaks to write", 
                                              false );
        message_center.receive( error_message );
        return false;
      }
      
      String file_name = (String)message.getValue();
      try 
      {
        Peak_new_IO.WritePeaks_new( file_name, (Vector)peakNew_list, false );
      }
      catch ( Exception ex )
      {
        Message error_message = new Message( Commands.DISPLAY_ERROR,
                 "ERROR: could not write peaks to file " + file_name,
                  false );
        message_center.receive( error_message );
      }
    }

    else if ( message.getName().equals(Commands.INDEX_PEAKS ) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof IndexPeaksCmd) )
        return false;

      // TODO  Make sure the vector of peaks exists and send error 
      //       message  and return if not. 

      IndexPeaksCmd cmd = (IndexPeaksCmd)obj;
    
      float[][] UB = null;
      try
      {
        UB = IndexPeaks_Calc.IndexPeaksWithOptimizer( peakNew_list,
                                                      cmd.getA(),
                                                      cmd.getB(),
                                                      cmd.getC(),
                                                      cmd.getAlpha(),
                                                      cmd.getBeta(),
                                                      cmd.getGamma() );
      }
      catch ( Exception ex )
      {
        Message error_message = new Message( Commands.DISPLAY_ERROR,
                                            "ERROR: failed to index peaks",
                                             false );
        message_center.receive( error_message );
      } 

      Message set_peaks = new Message( Commands.SET_PEAK_NEW_LIST,
                                       peakNew_list,
                                       true );
      message_center.receive( set_peaks );

    }

    return false;
  }

}
