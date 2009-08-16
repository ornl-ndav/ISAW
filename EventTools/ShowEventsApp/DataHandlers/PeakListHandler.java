
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
import EventTools.ShowEventsApp.Command.Util;


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
    message_center.addReceiver( this, Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX); ;
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
        Util.sendError( message_center, "ERROR: No Peaks to write" );
      }
      
      String file_name = (String)message.getValue();
      try 
      {
        Peak_new_IO.WritePeaks_new( file_name, (Vector)peakNew_list, false );
      }
      catch ( Exception ex )
      {
        Util.sendError( message_center, 
                       "ERROR: could not write peaks to file " +
                        file_name );
        return false;
      }
    }

    else if ( message.getName().equals(Commands.INDEX_PEAKS ) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof IndexPeaksCmd) )
        return false;

      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Util.sendError( message_center, "ERROR: No Peaks Found Yet ");
        return false;
      }

      IndexPeaksCmd cmd = (IndexPeaksCmd)obj;
      float tolerance = .12f;                    // TODO: use tolerance for
                                                 //       Index w/optimizer
 
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
        Util.sendInfo( message_center, "Finished Indexing" );
      }
      catch ( Exception ex )
      {
        Util.sendError( message_center, "ERROR: Failed to index Peaks ");
        return false;
      } 

      Message set_peaks = new Message( Commands.SET_PEAK_NEW_LIST,
                                       peakNew_list,
                                       true );
      message_center.receive( set_peaks );

      Message set_or = new Message( Commands.SET_ORIENTATION_MATRIX,
                                    UB, true );
      message_center.receive( set_or );

      System.out.println("Indexing results are: " );

      int total_peaks = peakNew_list.size();
      int count = 0;
      for ( int i = 0; i < total_peaks; i++ )
      {
        Peak_new peak = peakNew_list.elementAt(i);
        if ( peak.h() != 0 || peak.k() != 0 || peak.l() != 0 )
          count++;
      }       
      Util.sendInfo( message_center, "Indexed " + count + 
                                      " of " + total_peaks + 
                                      " within " + tolerance );
      return true;
    }else if( message.getName().equals( Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX ))
    {
       float[][] orientationMatrix = (float[][])message.getValue();
       for( int i=0; i< peakNew_list.size(); i++)
          peakNew_list.elementAt(i).UB( orientationMatrix );
       Message set_peaks = new Message( Commands.SET_PEAK_NEW_LIST,
                peakNew_list,
                true );
        message_center.receive( set_peaks );
        return true;
       
    }

    return false;
  }

}
