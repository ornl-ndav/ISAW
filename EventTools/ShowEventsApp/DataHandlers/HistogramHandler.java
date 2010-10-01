/* 
 * File: HistogramHandler.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2009-08-07 00:06:43 -0500 (Fri, 07 Aug 2009) $            
 *  $Revision: 19810 $
 */

package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;
import java.io.FileWriter;
import java.io.BufferedWriter;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.operator.Generic.TOF_SCD.IPeakQ;
import DataSetTools.operator.Generic.TOF_SCD.PeakQ;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.BasicPeakInfo;
import DataSetTools.operator.Generic.TOF_SCD.FindPeaksViaSort;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;
import EventTools.Histogram.*;

import EventTools.EventList.IEventList3D;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;
import EventTools.ShowEventsApp.Command.ConfigLoadCmd;
import EventTools.ShowEventsApp.Command.PeaksCmd;
import EventTools.ShowEventsApp.Command.FindPeaksCmd;
import EventTools.ShowEventsApp.Command.PeakImagesCmd;
import EventTools.ShowEventsApp.Command.IntegratePeaksCmd;
import EventTools.ShowEventsApp.Command.Util;
import EventTools.Integrate.IntegrateTools;


/**
 *  This class manages the 3D histogram that accumulates the events
 *  in reciprocal space.  It processes messages that clear and add
 *  events to the histogram, and adjusts the region of reciprocal space
 *  that is binned when the instrument type is changed.  It also provides
 *  information from the histogram, such as the max value in the histogram,
 *  the number of counts at a point and finds peaks in the histogram.
 */
public class HistogramHandler implements IReceiveMessage
{
  private float         DEFAULT_RADIUS     = 0.2f;
  private float         MAX_DEFAULT_RADIUS = 2.0f;
  private MessageCenter message_center;
  private MessageCenter view_message_center;

  private String        current_instrument = "";

  private Histogram3D   histogram = null;
  private int           num_bins;
  private double        max_hist_value_sent;
  private long          lastTimeShown;
  private boolean       receiving_events;
  private int           updates_since_events;

  private float         max_Q         = 20;
  private final float   MAX_Q_ALLOWED = 50;

  private float[][]     orientation_matrix = null;

/**
 *  Construct a histogram handler using the specified MessageCenters and
 *  number of bins in each direction of the histogram.
 *
 *  @param message_center       The message center from which events to
 *                              process data are received.
 *  @param view_message_center  The message center to which messages 
 *                              about data to viewed is sent.
 *  @param num_bins             The number of bins to use in each direction
 *                              for the histogram in reciprocal space.
 */

  public HistogramHandler( MessageCenter message_center, 
                           MessageCenter view_message_center,
                           int           num_bins )
  {
    this.num_bins = num_bins;    
    this.max_hist_value_sent = 0;

    this.message_center      = message_center;
    this.view_message_center = view_message_center;
    
    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_HISTOGRAMS );
    message_center.addReceiver( this, Commands.INIT_HISTOGRAM );
    message_center.addReceiver( this, Commands.LOAD_CONFIG_INFO );
    message_center.addReceiver( this, Commands.SET_WEIGHTS_FROM_HISTOGRAM );
    message_center.addReceiver( this, Commands.ADD_HISTOGRAM_INFO );
    message_center.addReceiver( this, Commands.GET_HISTOGRAM_MAX );
    message_center.addReceiver( this, Commands.FIND_PEAKS );
    message_center.addReceiver( this, Commands.GET_PEAK_IMAGE_REGIONS );
    message_center.addReceiver( this, Commands.SPHERE_INTEGRATE_PEAKS );
    message_center.addReceiver( this, Commands.GET_PEAKS_TO_SPHERE_INTEGRATE );
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );
    
    view_message_center.addReceiver( this, Commands.UPDATE );

    updates_since_events = 0;
    receiving_events     = false;
    lastTimeShown        = System.currentTimeMillis();

    Set_Histogram( current_instrument );

    Message resized = new Message( Commands.HISTOGRAM_RESIZED,
                                   new Integer(this.num_bins),
                                   true,
                                   true );
    message_center.send( resized );
  }


 /**
  *  Receive and process messages: 
  *      ADD_EVENTS_TO_HISTOGRAMS, 
  *      CLEAR_HISTOGRAM, 
  *      SET_WEIGHTS_FROM_HISTOGRAM, 
  *      ADD_HISTOGRAM_INFO,
  *      GET_HISTOGRAM_MAX,
  *      FIND_PEAKS.
  *
  *  @param message  The message to be processed.
  *
  *  @return true If processing the message has altered something that
  *               requires a redraw of any updateable objects.
  */
  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.ADD_EVENTS_TO_HISTOGRAMS) )
    {
      IEventList3D events = (IEventList3D)message.getValue();
      if ( events == null )
        return false;

      receiving_events = true;
      AddEventsToHistogram( events );

//    Util.sendInfo( "ADDED " + events.numEntries() + " to HISTOGRAM");

      SetWeightsFromHistogram( events, histogram );
      Message add_to_view = new Message( Commands.ADD_EVENTS_TO_VIEW,
                                         events,
                                         false,
                                         true );
//    Util.sendInfo( "SENDING MESSGE, ADD TO VIEW");
      view_message_center.send( add_to_view );

      updates_since_events = 0;

      double max = histogram.maxVal();
                                               // send informational message
                                               // every 15 seconds, if getting
                                               // events
      long time =  System.currentTimeMillis();
      if( time - lastTimeShown > 15000 )
      {
        String max_message = String.format(
                "Max Histogram Value : %4.2f,  Total Events: %d",
                 histogram.maxVal(), histogram.numAdded()  );
        Util.sendInfo( max_message );
        lastTimeShown = time;
      }
                                              // Update color scale whenever
                                              // we've doubled max
      if ( max > 2 * max_hist_value_sent )
      {
        max_hist_value_sent = max;
        Message hist_max = new Message( Commands.SET_HISTOGRAM_MAX,
                                        new Float( max ),
                                        true,
                                        true );
        view_message_center.send( hist_max );
       }

      return false;
    }

    else if ( message.getName().equals(Commands.LOAD_CONFIG_INFO) )
    {                                          // MAY NEED TO RESIZE HISTOGRAM
      Object obj = message.getValue();
      if ( obj instanceof ConfigLoadCmd )
      {
        ConfigLoadCmd cmd = (ConfigLoadCmd)obj;
        num_bins = cmd.getNbins();

        if (histogram == null || histogram.xEdgeBinner().numBins() != num_bins)
          Set_Histogram( current_instrument );

        Message resized = new Message( Commands.HISTOGRAM_RESIZED,
                                       new Integer(num_bins),
                                       true,
                                       true );    
        message_center.send( resized );
      }
    }

    else if ( message.getName().equals(Commands.INIT_HISTOGRAM) )
    {
      boolean set_ok = SetNewInstrument( message.getValue() );
      if ( set_ok )
      {
        receiving_events = false;  // no timed update, until we get more events
        max_hist_value_sent = 0;
        Message init_hist_done = new Message( Commands.INIT_HISTOGRAM_DONE,
                                              null,
                                              true,
                                              true );
        message_center.send( init_hist_done );
      }
      else
        message_center.send( new Message( Commands.LOAD_FAILED,
                                          null, true, true ) );
      return false;
    }

    else if ( message.getName().equals(Commands.SET_WEIGHTS_FROM_HISTOGRAM))
    {
      IEventList3D events = (IEventList3D)message.getValue();
      SetWeightsFromHistogram( events, histogram );
      return false;
    }

    else if ( message.getName().equals(Commands.ADD_HISTOGRAM_INFO))
    {
      Object val = message.getValue();
      if ( val instanceof SelectionInfoCmd )         // fill in counts field
      {
        SelectionInfoCmd select_info_cmd = (SelectionInfoCmd)val;

        AddHistogramInfo( select_info_cmd, histogram );
        Message info_message = new Message( 
                                       Commands.ADD_ORIENTATION_MATRIX_INFO, 
                                       select_info_cmd, 
                                       true,
                                       true );

        message_center.send( info_message );
      }
      return false;
    }

    else if ( message.getName().equals(Commands.FIND_PEAKS) )
    {
      Object val = message.getValue();
      if ( val != null && val instanceof FindPeaksCmd )  
      {
        FindPeaksCmd cmd = (FindPeaksCmd)message.getValue();

        Util.sendInfo("Searching for peaks, PLEASE WAIT ...");

        Vector<PeakQ> peakQs = null; 
                                     // don't search for peaks and change
        synchronized ( histogram )   // the histogram at the same time !
        { 
          peakQs = FindPeaks( histogram,
                              cmd.getSmoothData(),
                              cmd.getMaxNumberPeaks(),
                              cmd.getMinPeakIntensity(),
                              cmd.getLogFileName() );
        }

        if ( peakQs != null && peakQs.size() > 0 )       // send out the peaks
        { 
          PeaksCmd peaks_cmd = new PeaksCmd( peakQs, 
                                             cmd.getShowImages(),
                                             cmd.getImageSize(),
                                             cmd.getMaxOffset() );
          Message set_peak_Q_list = new Message( Commands.SET_PEAK_Q_LIST,
                                                 peaks_cmd,
                                                 true,
                                                 true );
          message_center.send( set_peak_Q_list );

          if ( cmd.getMarkPeaks() )                     // mark the peaks
          {
             Message mark_peaks  = new Message( Commands.MARK_PEAKS,
                                                peakQs,
                                                true,
                                                true );
             message_center.send( mark_peaks );
          }
        }
        else
          Util.sendInfo("Failed to find any peaks!");
      }
      return false;
    }
    
    else if ( message.getName().equals(Commands.GET_PEAK_IMAGE_REGIONS) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof PeaksCmd) )
        return false;

      PeaksCmd cmd = (PeaksCmd)obj;
      Vector<Peak_new> peaks   = (Vector<Peak_new>)cmd.getPeaks();
      if ( peaks != null )
      {
        float size       = cmd.getImageSize();
        int   max_offset = cmd.getMaxOffset();

        Vector regions = new Vector( peaks.size() );
        for ( int i = 0; i < peaks.size(); i++ )
        {
          float[] q_arr = peaks.elementAt(i).getUnrotQ();
          float qx = (float)(q_arr[0] * 2 * Math.PI);
          float qy = (float)(q_arr[1] * 2 * Math.PI);
          float qz = (float)(q_arr[2] * 2 * Math.PI);
          regions.add( histogram.getSubHistogram( qx, qy, qz, 
                                                  size, -1, -1, max_offset ) );
        }
      PeakImagesCmd peak_image_cmd = new PeakImagesCmd( peaks, regions );
      Message peak_images_message =
           new Message(Commands.SHOW_PEAK_IMAGES, peak_image_cmd, true, true);
      message_center.send( peak_images_message );
      }
    }

    else if ( message.getName().equals(Commands.GET_HISTOGRAM_MAX) )
    {
       Message hist_max = new Message( Commands.SET_HISTOGRAM_MAX,
                                       new Float( histogram.maxVal() ),
                                       true,
                                       true );
       view_message_center.send( hist_max );

       String max_message = String.format("Max Histogram Value : %4.2f",
                                           histogram.maxVal() );
       Util.sendInfo( max_message );

       return false;
    }


    else if ( message.getName().equals(Commands.SET_ORIENTATION_MATRIX) )
    {
      Object val = message.getValue();
      if ( val == null || !( val instanceof Vector ) )
        return false;

      Vector vec = (Vector)val;
      if ( vec.size() < 1 || !( vec.elementAt(0) instanceof float[][] ) )
        return false;

      float[][] UBT = (float[][]) vec.elementAt(0);
      orientation_matrix = LinearAlgebra.getTranspose( UBT );
      for ( int row = 0; row < 3; row++ )
        for ( int col = 0; col < 3; col++ )
           orientation_matrix[row][col] *= (float)(2*Math.PI);
/*
      System.out.println("HistogramHandler, orientation_matrix = " );
      LinearAlgebra.print( orientation_matrix );
*/
    }

    else if ( message.getName().equals(Commands.GET_PEAKS_TO_SPHERE_INTEGRATE))
    {
       Object value = message.getValue();
       if ( value instanceof IntegratePeaksCmd )
       {
         IntegratePeaksCmd cmd = (IntegratePeaksCmd)value;
         if ( !cmd.getCurrent_peaks_only() &&
              histogram != null            &&
              orientation_matrix != null     )  // HistogramHandler provides
         {
           Vector peaks = getPeakQsToIntegrate(histogram, orientation_matrix);
           cmd = new IntegratePeaksCmd( peaks,
                                        cmd.getSphere_radius(),
                                        cmd.getCurrent_peaks_only(),
                                        cmd.getRecord_as_peaks_list() );
           Message integrate = new Message( Commands.SPHERE_INTEGRATE_PEAKS,
                                            cmd, true, true );
           message_center.send( integrate );
         }
       }
    }

    else if ( message.getName().equals(Commands.SPHERE_INTEGRATE_PEAKS) )
    {
      Object obj = message.getValue();
      if ( obj != null && obj instanceof IntegratePeaksCmd )
      {
        IntegratePeaksCmd cmd = (IntegratePeaksCmd)obj;

        Vector<IPeakQ>  peaks       = cmd.getPeaks();
        Vector<float[]> i_isigi_vec = new Vector<float[]>();
        Vector<IPeakQ>  peaks_kept  = new Vector<IPeakQ>();

        float peak_radius = cmd.getSphere_radius();
        if ( peak_radius < 0 )                       // no change if bad radius
        {
          peak_radius = DEFAULT_RADIUS;
          Util.sendInfo( "Negative radius reset to " + peak_radius );
        }
        if ( peak_radius > MAX_DEFAULT_RADIUS )
        {
          peak_radius = MAX_DEFAULT_RADIUS;
          Util.sendInfo( "Integration radius limited to " + peak_radius );
        }
        
        DEFAULT_RADIUS    = peak_radius;             // update radius used for
                                                     // selected point radius
        float bkg_radius  = 2 * peak_radius;
        float[] radii = { peak_radius, bkg_radius };

        for ( int i = 0; i < peaks.size(); i++ )
        {
          float[] q_arr = peaks.elementAt(i).getUnrotQ();
          float qx = (float)(q_arr[0] * 2 * Math.PI);
          float qy = (float)(q_arr[1] * 2 * Math.PI);
          float qz = (float)(q_arr[2] * 2 * Math.PI);
          float[] i_isigi = getI_and_sigI( qx, qy, qz, radii );
          if ( i_isigi != null )
          {
            peaks_kept.add( peaks.elementAt(i) );
            i_isigi_vec.add( i_isigi );
/*            
            System.out.print( peaks.elementAt(i) );
            System.out.printf("I = %10.2f  IsigI = %5.1f\n", 
                               i_isigi[0], i_isigi[1] );
*/
          }
        }

        if ( peaks_kept.size() > 0 )
        {
          cmd = new IntegratePeaksCmd( peaks_kept,
                                       i_isigi_vec,
                                       cmd.getSphere_radius(),
                                       cmd.getCurrent_peaks_only(),
                                       cmd.getRecord_as_peaks_list() );
          Message request = new Message( Commands.REVERSE_WEIGHT_INTEGRALS, 
                                         cmd, true, true );
          message_center.send( request );         
        }
      }
    }

    else if( message.getName().equals( Commands.UPDATE ) )
    {
       updates_since_events++;
       
       if( updates_since_events < 2 )
          return false;
                                      // at least four updates passed since we
                                      // got events, so assume we are done
                                      // do one last update, then send 
                                      // LOAD_FILE_DONE message, and set
                                      // receiving events to false
       if ( receiving_events )       
       {
         lastTimeShown = System.currentTimeMillis();
         double max = histogram.maxVal();
       
         String max_message = String.format(
                  "Max Histogram Value : %4.2f,  Total Events: %d",
                   histogram.maxVal(), histogram.numAdded()  );
         Util.sendInfo( max_message );

         Message hist_max = new Message( Commands.SET_HISTOGRAM_MAX,
                                         new Float( max ),
                                         true,
                                         true );
         view_message_center.send( hist_max );

         Message done_loading = new Message( Commands.LOAD_FILE_DONE,
                                             null,
                                             true,
                                             true );
         message_center.send( done_loading );

         receiving_events = false;
       }
    }

    return false;
  }


  private boolean SetNewInstrument( Object obj )
  {
    if ( obj == null || ! (obj instanceof SetNewInstrumentCmd) )
      return false;

    SetNewInstrumentCmd cmd = (SetNewInstrumentCmd)obj;

    String inst  = cmd.getInstrumentName();
    float  max_Q = cmd.getMaxQValue();
    if ( max_Q < 0.5 )
      max_Q = 0.5f;
    if ( max_Q > MAX_Q_ALLOWED )
      max_Q = MAX_Q_ALLOWED;

    if ( inst == null || inst.trim().length() <= 0 )
    {
      Util.sendError("ERROR: SET_NEW_INSTRUMENT name is " + inst );
      return false;
    }

    current_instrument = inst;
    Set_Histogram( inst );

     return true;
  }


  /**
   *  Adjust region of reciprocal space covered to match the requirements
   *  of the specified instrument.
   */
  synchronized private void Set_Histogram( String instrument )
  {
    if ( instrument.equals("SNAP") ||
         instrument.equals("TOPAZ") )
      Set_Histogram(num_bins, max_Q, -40.0f, 0, -25.0f, 25.0f, -25.0f, 25.0f);

    else if ( instrument.equals("ARCS") ||
              instrument.equals("SEQ")  )
      Set_Histogram(num_bins, max_Q, -40.0f, 0, -15.0f, 40.0f, -15.0f, 15.0f);

    else
      Set_Histogram(num_bins, max_Q, -40.0f, 0, -25.0f, 25.0f, -25.0f, 25.0f);
  }


  /**
   *  Set histogram to be a new empty histogram covering the specified
   *  region of reciprocal space.  Allocate the histogram if it was not
   *  previously allocated to have the requested size.  If allocating the
   *  memory fails, keep trying with reduced size histograms, until the
   *  allocation succeeds.
   *
   *  @param num_bins  The number of bins to use in half of the region
   *                   for the histogram.
   *  @param max_Q     The maximum Q value set by the user.  This is
   *                   used to limit the size of the histogram, so that
   *                   when smaller regions of Q are covered, the histogram
   *                   will have finer resolution.
   *  @param qx_min    The minimum qx value
   *  @param qx_max    The maximum qx value
   *  @param qy_min    The minimum qy value
   *  @param qy_max    The maximum qy value
   *  @param qz_min    The minimum qz value
   *  @param qz_max    The maximum qz value
   */
  synchronized private void Set_Histogram( int   num_bins,
                                           float max_Q,
                                           float qx_min,  float qx_max,
                                           float qy_min,  float qy_max,
                                           float qz_min,  float qz_max  )
  {
                                // clamp bounds on histogram to be between
    if ( qx_min < -max_Q )      // -max_Q and +max_Q
      qx_min = -max_Q;

    if ( qx_max > max_Q )
      qx_max = max_Q;

    if ( qy_min < -max_Q )
      qy_min = -max_Q;

    if ( qy_max > max_Q )
      qy_max = max_Q;

    if ( qz_min < -max_Q )
      qz_min = -max_Q;

    if ( qz_max > max_Q )
      qz_max = max_Q;

    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);

    IEventBinner x_bin1D = new UniformEventBinner( qx_min, qx_max, num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( qy_min, qy_max, num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( qz_min, qz_max, num_bins );

    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    if ( histogram == null || histogram.xEdgeBinner().numBins() != num_bins )
    {
      boolean allocated_OK = false;
      while ( num_bins > 0 && !allocated_OK )
      {
        try
        {
          long size = 4L * num_bins * num_bins * num_bins / 1000000L;
          histogram = null;         // let the original histogram be freed
          Util.sendInfo("Allocating histogram with " + num_bins + " steps " +
                         size + " Mega Bytes...");
          Util.sendInfo("A large histogram may take a while...PLEASE WAIT");
          histogram = new Histogram3D( x_binner, y_binner, z_binner );
          allocated_OK = true;
          Util.sendInfo( "Histogram allocated OK" );
        }
        catch ( Throwable ex )
        {
/*
          try
          {
            Thread.sleep( 5000 );    // pause a bit to let the system 
                                     // recover from trying to allocate all
                                     // of the memory.
          }
          catch ( Exception sleep_exception )
          {
            System.out.println("Thread sleep interrupted");
          }
*/
          Util.sendError( "FAILED to ALLOCATE HISTOGRAM..." );
          num_bins /= 2;
          x_bin1D = new UniformEventBinner( qx_min, qx_max, num_bins );
          y_bin1D = new UniformEventBinner( qy_min, qy_max, num_bins );
          z_bin1D = new UniformEventBinner( qz_min, qz_max, num_bins );

          x_binner = new ProjectionBinner3D(x_bin1D, xVec);
          y_binner = new ProjectionBinner3D(y_bin1D, yVec);
          z_binner = new ProjectionBinner3D(z_bin1D, zVec);
        }
      }
      this.num_bins = num_bins;
    }
    else
    {
      histogram.setHistogramPosition( x_binner, y_binner, z_binner );
      histogram.clear();
    }
  }


  /**
   *  Set the weight value of the specified events to the value of
   *  the histogram bin that contains the event.  The histogram bin
   *  values are used to control the color map for the 3D event viewer.
   */
  private void SetWeightsFromHistogram( IEventList3D events, 
                                        Histogram3D histogram )
  {
    int n_events = events.numEntries();

    float[] weights = events.eventWeights();
    if ( weights == null || weights.length != n_events )
      weights = new float[ n_events ];

    float[] xyz = events.eventVals();

    float eventX,
          eventY,
          eventZ;

    int index = 0;
    for ( int i = 0; i < n_events; i++ )
    {
      eventX     = xyz[ index++ ];
      eventY     = xyz[ index++ ];
      eventZ     = xyz[ index++ ];
      weights[i] = histogram.valueAt( eventX, eventY, eventZ );
    }
  }

  
  /**
   * Add the selected point's histogram counts and page number to the 
   * select_info_command. 
   */
  private void AddHistogramInfo( SelectionInfoCmd select_info_cmd,
                                 Histogram3D      histogram )
  {
    Vector3D Qxyz = select_info_cmd.getQxyz();

    float x = (float)(Qxyz.getX() * 2 * Math.PI);
    float y = (float)(Qxyz.getY() * 2 * Math.PI);
    float z = (float)(Qxyz.getZ() * 2 * Math.PI);

    float counts = histogram.valueAt( x, y, z );
    select_info_cmd.setCounts( counts );

    IProjectionBinner3D z_binner = histogram.zBinner();
    int page = z_binner.index( x, y, z );
    select_info_cmd.setHistPage( page );

    float BACKGR_RADIUS  = 2 * DEFAULT_RADIUS;
    float[] radii = { DEFAULT_RADIUS, BACKGR_RADIUS };

    float[] result = getI_and_sigI( x, y, z, radii );
    if ( result != null )
    {
      float net_signal = result[0];
      float I_sigI     = result[1];

      float weight = select_info_cmd.getWeight();
    
      if ( weight > 0 )
      {
        net_signal /= weight;
        I_sigI     /= (float)Math.sqrt(weight);

        select_info_cmd.setIntegral( net_signal );
        select_info_cmd.setI_sigI( I_sigI ); 
      }
    }
  }
 

  private float[] getI_and_sigI( float x, float y, float z, float[] radii )
  {
    if ( histogram == null )
      return null;

    float center_val = histogram.valueAt( x, y, z );
    if ( center_val <= 0 )                           // skip peaks with zero
      return null;                                   // counts at center

    Vector result = histogram.sphereIntegrals( x, y, z, radii );

    if ( result == null )
      return null;
    
    float[] sums    = (float[])result.elementAt(0);
    float[] volumes = (float[])result.elementAt(1);

    float peak_count = sums[0];
    float bkg_count  = sums[1] - peak_count;

    float peak_volume = volumes[0];
    float bkg_volume  = volumes[1] - peak_volume;

    float[] i_isigi = IntegrateTools.getI_and_sigI( peak_count, peak_volume,
                                                    bkg_count, bkg_volume );
    return i_isigi;
  }


  private Vector<PeakQ> getPeakQsToIntegrate( Histogram3D histogram, 
                                              float[][]   orientation_matrix )
  {
    Vector peakQs = new Vector(); 
    if ( orientation_matrix != null && histogram != null )
    {
      Vector3D[] basis_vecs = 
            QuickIntegrateHandler.getLatticeBasisVectors( orientation_matrix );

      Vector3D   h_vec = basis_vecs[0];
      Vector3D   k_vec = basis_vecs[1];
      Vector3D   l_vec = basis_vecs[2];

      int max_h = (int)( max_Q / h_vec.length() );
      int max_k = (int)( max_Q / k_vec.length() );
      int max_l = (int)( max_Q / l_vec.length() );
/*
      System.out.println("Max h, k, l = " + max_h + 
                         ", " + max_k + 
                         ", " + max_l );
*/
      float  two_PI = (float)(2 * Math.PI);

      for ( int h = -max_h; h <= max_h; h++ )      // TODO, calculate range 
        for ( int k = -max_k; k <= max_k; k++ )    // on MaxQ
          for ( int l = -max_l; l <= max_l; l++ )
          {
            Vector3D q_vec = 
                QuickIntegrateHandler.q_vector( h, k, l, h_vec, k_vec, l_vec );
 
            if (histogram.isPointIn(q_vec.getX(), q_vec.getY(), q_vec.getZ()))
            {
              int value = (int)histogram.valueAt( q_vec.getX(), 
                                                  q_vec.getY(), 
                                                  q_vec.getZ() );

              PeakQ peak = new PeakQ( q_vec.getX() / two_PI,
                                      q_vec.getY() / two_PI,
                                      q_vec.getZ() / two_PI,
                                      value );

              peak.sethkl( h, k, l );
              peakQs.add( peak );
            }
          }
    }
    return peakQs;
  }


  /**
   *  Find the peaks in the specified histogram, using the specified
   *  search parameters.
   *
   *  @param  histogram        The histogram to scan for peaks
   *  @param  smooth_data      Flag indicating whether or not to smooth the
   *                           the data first.  This should be FALSE since
   *                           the data is already smoothed and the smoothing
   *                           process takes a lot of time and space.
   *  @param  num_peaks        The number of peaks to return
   *  @param  min_intensity    The minimum intensity to consider in the peak
   *                           search
   *  @param  log_file         The name of the file to write the logging
   *                           information to.
   */
  private Vector<PeakQ> FindPeaks( Histogram3D histogram,
                                  boolean     smooth_data,
                                  int         num_peaks,
                                  float       min_intensity,
                                  String      log_file )
 
  {
     // System.out.println("START OF FindPeaks");
   
     int num_pages = histogram.zBinner().numBins();
     int num_rows  = histogram.yBinner().numBins();
     int num_cols  = histogram.xBinner().numBins();
     
     float[][][] histogram_array = new float[num_pages][][];
     for ( int page = 0; page < num_pages; page++ )
       histogram_array[page] = histogram.pageSlice( page );

     int[] row_list = new int[num_rows];
     for ( int k = 0; k < num_rows; k++ )
       row_list[k] = k + 1;

     int[] col_list = new int[num_cols];
     for ( int k = 0; k < num_cols; k++ )
       col_list[k] = k + 1;

     StringBuffer log = new StringBuffer();

     float min_val = (float)histogram.minVal();
     float max_val = (float)histogram.maxVal();
     BasicPeakInfo[] peaks = FindPeaksViaSort.getPeaks( histogram_array,
                                                        smooth_data,
                                                        num_peaks,
                                                        min_intensity,
                                                        row_list,
                                                        col_list,
                                                        0,
                                                        num_pages-1,
                                                        min_val,
                                                        max_val,
                                                        log );

    if ( log_file == null || log_file.trim().length() <= 0 )
    {
      log_file = Wizard.TOF_SCD.Util.ISAW_SCRATCH_DIRECTORY +
                                                "ShowEventsApp_FindPeaks.log";

      Wizard.TOF_SCD.Util.CheckTmpDirectory();   // make dir if it's not there
    }

    try
    {  
      FileWriter     writer  = new FileWriter( log_file );
      BufferedWriter output  = new BufferedWriter( writer );
      output.write( log.toString() );
      output.close();
    }
    catch ( Exception ex )
    {
      Util.sendWarning( "Can't write Find Peaks log file: " + log_file );
//    System.out.println("Exception writing log file " + log_file );
//    ex.printStackTrace();
    }

    IProjectionBinner3D x_binner = histogram.xEdgeBinner();
    IProjectionBinner3D y_binner = histogram.yEdgeBinner();
    IProjectionBinner3D z_binner = histogram.zEdgeBinner();

    Vector3D   zero    = new Vector3D();
    Vector3D[] verts   = new Vector3D[ peaks.length ];
    int        counter = 0;
    for ( int k = 0; k < verts.length; k++ )
    {
      if ( peaks[k].isValid() )
      {
        counter++;

        float col  = peaks[k].getColMean();
        float row  = peaks[k].getRowMean();
        float page = peaks[k].getChanCenter();

        Vector3D point = x_binner.Vec( page );
        Vector3D temp  = y_binner.Vec( col );
        point.add( temp );
        temp = z_binner.Vec( row );
        point.add( temp );
        verts[k] = point;
      }
      else
        verts[k] = zero;
    }

    Vector<PeakQ> q_peaks = new Vector<PeakQ>();
    for ( int k = 0; k < verts.length; k++ )
    {
      if ( verts[k] != zero )
      {
        float qx = verts[k].getX();
        float qy = verts[k].getY();
        float qz = verts[k].getZ();
        float ipk_f = histogram.valueAt( qx, qy, qz );
        qx = (float)(qx / (2 * Math.PI)) ;
        qy = (float)(qy / (2 * Math.PI)) ;
        qz = (float)(qz / (2 * Math.PI)) ;
        q_peaks.add( new PeakQ( qx, qy, qz, (int)ipk_f ) );
      }
    }

    if ( q_peaks == null )
      Util.sendInfo( "FOUND 0 PEAKS" );
    else
      Util.sendInfo( "FOUND " + q_peaks.size() + " PEAKS " );

    return q_peaks;
  }                         


  synchronized public void AddEventsToHistogram( IEventList3D events )
  {
                                     // don't search for peaks and change
    synchronized( histogram )        // the histogram at the same time
    {
      histogram.addEvents( events, true );
    }
  }

}
