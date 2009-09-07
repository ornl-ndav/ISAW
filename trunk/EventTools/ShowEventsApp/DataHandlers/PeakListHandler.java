
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.MathTools.LinearAlgebra;

import DataSetTools.components.ui.Peaks.OrientMatrixControl;
import DataSetTools.operator.Generic.TOF_SCD.*;

import Operators.TOF_SCD.IndexPeaks_Calc;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

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
    message_center.addReceiver( this, Commands.INDEX_PEAKS_ROSS );
    message_center.addReceiver( this, 
                                Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX);
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
         message_center.send( get_peak_new_list );

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
        Util.sendError( "ERROR: No Peaks to write" );
      }
      
      String file_name = (String)message.getValue();
      try 
      {
        Peak_new_IO.WritePeaks_new( file_name, (Vector)peakNew_list, false );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: could not write peaks to file " +
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
        Util.sendError( "ERROR: No Peaks Found Yet ");
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
        UB= LinearAlgebra.getTranspose(UB);
        indexAllPeaks( peakNew_list, UB);
        Util.sendInfo( "Finished Indexing" );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: Failed to index Peaks ");
        return false;
      } 

      Message set_peaks = new Message( Commands.SET_PEAK_NEW_LIST,
                                       peakNew_list,
                                       true );
      message_center.send( set_peaks );

      Message set_or = new Message( Commands.SET_ORIENTATION_MATRIX,
                                    UB, true );
      message_center.send( set_or );

      System.out.println("Indexing results are: " );

      int total_peaks = peakNew_list.size();

      int count = numIndexed( peakNew_list, tolerance );

      Util.sendInfo( "Indexed " + count + 
                     " of " + total_peaks + 
                     " within " + tolerance );
      return false;
    }

    else if( message.getName().equals( 
             Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX ))
    {
       float[][] orientationMatrix = (float[][])message.getValue();
       
       indexAllPeaks( peakNew_list, orientationMatrix);
       Message set_peaks = new Message( Commands.SET_PEAK_NEW_LIST,
                                        peakNew_list,
                                        true );
       message_center.send( set_peaks );
       return false;
    } 

    else if( message.getName().equals(  Commands.INDEX_PEAKS_ROSS ))
    {
       float[] value = (float[])message.getValue();
       if( value == null || value.length < 2)
          return false;
         
       message_center.send(  new Message( Commands.DISPLAY_INFO, 
                "Starting long calculation. Wait", false) );
        
        Vector<float[][]> OrientationMatrices =
                GetUB.getAllOrientationMatrices( peakNew_list , null , 
                                                .02f , value[1] );
        if( OrientationMatrices == null)
        {
           message_center.send( new Message( Commands.DISPLAY_ERROR,
                    "No Orientation Matrix found in Auto no Crystal Parameters",
                    false));
           return true;
        }
        Vector<IPeak> Peaks = Convert2IPeak(peakNew_list);
        float[][]UB = OrientMatrixControl.showCurrentOrientationMatrices(
                 Peaks , OrientationMatrices );
        
        if( UB == null)
        {
           message_center.send( new Message( Commands.DISPLAY_ERROR,
                    "No Orientation Matrix was selected",
                    false));
           return true;
        }
        
        message_center.send( new Message( Commands.SET_ORIENTATION_MATRIX,
                 LinearAlgebra.getTranspose( UB ), false));
        return true;
    }

    return false;
  }

  
  private Vector<IPeak> Convert2IPeak( Vector<Peak_new> Peaks)
  {
     if( Peaks == null)
        return null;
     
     Vector<IPeak> Res = new Vector<IPeak>( Peaks.size());
     for( int i=0; i< Peaks.size(); i++)
        Res.add( (IPeak )Peaks.elementAt(i));
     
     return Res;
  }


  private void indexAllPeaks( Vector Peaks, float[][]UBT )
  {
    float[][]UB = LinearAlgebra.getTranspose( UBT );
    for( int i=0; i<Peaks.size(); i++)
    { 
       Object peak = Peaks.elementAt(i);
       if(peak != null && peak instanceof IPeak)
         {
           ((IPeak)peak).sethkl( 0f,0f,0f);
           ((IPeak)peak).UB( UB );
           ((IPeak)peak).UB( null );
         }
    }
  }


  private int numIndexed( Vector peaks, float tolerance )
  {
    if ( peaks == null )
      return 0;
    
    if ( tolerance <= 0 )
      return 0;

    int total_peaks = peaks.size();
    int count = 0;
    for ( int i = 0; i < total_peaks; i++ )
    {
      IPeakQ peak = peakNew_list.elementAt(i);
      if ( distanceToInt( peak.h() ) <= tolerance &&
           distanceToInt( peak.k() ) <= tolerance &&
           distanceToInt( peak.l() ) <= tolerance  )
        count++;
    }
    return count;
  }


  private float distanceToInt( float val )
  {
    float rounded = Math.round(val);
    return Math.abs( val - rounded );
  }

}
