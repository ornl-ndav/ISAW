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

import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.operator.Generic.TOF_SCD.PeakQ;
import DataSetTools.operator.Generic.TOF_SCD.BasicPeakInfo;
import DataSetTools.operator.Generic.TOF_SCD.FindPeaksViaSort;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;
import EventTools.Histogram.*;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.FindPeaksCmd;
import EventTools.ShowEventsApp.Command.Util;


/**
 *  This class manages the 3D histogram that accumulates the events
 *  in reciprocal space.  It processes messages that clear and add
 *  events to the histogram, and adjusts the region of reciprocal space
 *  that is binned when the instrument type is changed.  It also provides
 *  information from the histogram, such as the max value in the histogram,
 *  the number of counts at a point and finds peaks in the histogram.
 *
 *  @param message_center       The message center from which events to
 *                              process data are received.
 *  @param view_message_center  The message center to which messages 
 *                              about data to viewed is sent.
 *  @param num_bins             The number of bins to use in each direction
 *                              for the histogram in reciprocal space.
 */
public class HistogramHandler implements IReceiveMessage
{
  private MessageCenter message_center;
  private MessageCenter view_message_center;

  private String        current_instrument = "";
  private Histogram3D   histogram = null;
  private int           num_bins;
  private long          num_to_load;
  private double        max_hist_value_sent;
  private long          lastTimeShown;

  public HistogramHandler( MessageCenter message_center, 
                           MessageCenter view_message_center,
                           int           num_bins )
  {
    this.num_bins = num_bins;
    this.histogram = DefaultSNAP_Histogram( num_bins );
    this.current_instrument = SNS_Tof_to_Q_map.SNAP;
    this.num_to_load = 0;
    this.max_hist_value_sent = 0;

    this.message_center      = message_center;
    this.view_message_center = view_message_center;

    message_center.addReceiver( this, Commands.LOAD_FILE_DATA );
    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_HISTOGRAMS );
    message_center.addReceiver( this, Commands.INIT_HISTOGRAM );
    message_center.addReceiver( this, Commands.SET_WEIGHTS_FROM_HISTOGRAM );
    message_center.addReceiver( this, Commands.ADD_HISTOGRAM_INFO );
    message_center.addReceiver( this, Commands.GET_HISTOGRAM_MAX );
    message_center.addReceiver( this, Commands.FIND_PEAKS );
    
    lastTimeShown = System.currentTimeMillis();
  }


 /**
  *  Receive and process messages: 
  *      ADD_EVENTS_TO_HISTOGRAMS, 
  *      CLEAR_HISTOGRAM, 
  *      SET_NEW_INSTRUMENT, 
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

      AddEventsToHistogram( events );

//      Util.sendInfo( "ADDED " + events.numEntries() + " to HISTOGRAM");

      SetWeightsFromHistogram( events, histogram );
      Message add_to_view = new Message( Commands.ADD_EVENTS_TO_VIEW,
                                         events,
                                         false,
                                         true );
//      Util.sendInfo( "SENDING MESSGE, ADD TO VIEW");
      view_message_center.send( add_to_view );

      if ( histogram.numAdded() >= num_to_load )
      {
        Message done_loading = new Message( Commands.LOAD_FILE_DONE,
                                            null,
                                            true,
                                            true );
       message_center.send( done_loading );

       double max = histogram.maxVal();
       long time =  System.currentTimeMillis();
       if( time - lastTimeShown > 15000)
       {
       String max_message = String.format(
                "Max Histogram Value : %4.2f,  Total Events: %d",
                 histogram.maxVal(), histogram.numAdded()  );
          Util.sendInfo( max_message );
          lastTimeShown = time;
       }
       if ( max > 2 * max_hist_value_sent )
       {
         max_hist_value_sent = max;
         Message hist_max = new Message( Commands.SET_HISTOGRAM_MAX,
                                         new Float( max ),
                                         true,
                                         true );
         view_message_center.send( hist_max );

        
       }
      }
      return false;
    }

    else if ( message.getName().equals(Commands.LOAD_FILE_DATA) )
    {
      LoadEventsCmd cmd = (LoadEventsCmd)message.getValue();
      num_to_load = cmd.getEventsToLoad(); 
    }

    else if (  message.getName().equals(Commands.INIT_HISTOGRAM) )
    {
      boolean set_ok = SetNewInstrument( message.getValue() );
      if ( set_ok )
      {
        num_to_load = 0; 
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

    else if (  message.getName().equals(Commands.SET_NEW_INSTRUMENT) )
    {
      Object obj = message.getValue();

      SetNewInstrument( obj );

      Util.sendInfo( "Histogram set up for " + current_instrument );
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
      if ( val instanceof FindPeaksCmd )  
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
          Message set_peak_Q_list = new Message( Commands.SET_PEAK_Q_LIST,
                                                 peakQs,
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

    return false;
  }



  private boolean SetNewInstrument( Object obj )
  {
    if ( obj == null || ! (obj instanceof SetNewInstrumentCmd) )
      return false;

    SetNewInstrumentCmd cmd = (SetNewInstrumentCmd)obj;

    String inst = cmd.getInstrumentName();

    if ( inst == null || inst.trim().length() <= 0 )
    {
      Util.sendError("ERROR: SET_NEW_INSTRUMENT name is " + inst );
      return false;
    }

    if ( inst.equals( current_instrument ) )
    {
      histogram.clear();
      Util.sendInfo( "Histogram set up for " + current_instrument );
      return true;
    }

    if ( inst.equals(SNS_Tof_to_Q_map.SNAP) )
    {
      SetHistogramForSNAP();
      current_instrument = inst;
      return true;
    }
    else if ( inst.startsWith("TOP") ) 
    {
      SetHistogramForTOPAZ();
      current_instrument = inst;
      return true;
    }
    else if ( inst.equals(SNS_Tof_to_Q_map.ARCS) ||
              inst.equals(SNS_Tof_to_Q_map.SEQ)  )
    {
      SetHistogramForARCS();
      current_instrument = inst;
      return true;
    }
    else
    {
      histogram.clear();                 // at least clear the histogram
                                         // if starting a new file
      Util.sendWarning( inst + " not supported yet. " +
                        "Detector position info needed." );
      return false;
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
  }


  /**
   *  Set histogram to be a new empty histogram covering a region
   *  of reciprocal space appropriate for the SNAP instrument at the
   *  SNS.
   *
   *  @param num_bins  The number of bins to use in each direction
   *                   for the histogram.
   */
  synchronized private Histogram3D DefaultSNAP_Histogram( int num_bins )
  {
    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);

    IEventBinner x_bin1D = new UniformEventBinner( -16.0f,  0,   num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( -16.0f,  0,   num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( - 8.0f, 8.0f, num_bins );

    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D histogram = new Histogram3D( x_binner,
                                             y_binner,
                                             z_binner );
    return histogram;
  }


  /**
   *  Set up the histogram to be new empty histogram covering a region
   *  of reciprocal space appropriate for the SNAP instrument at the
   *  SNS, WITHOUT reallocating memory, if possible.
   */
  synchronized private void SetHistogramForSNAP()
  {
    if ( histogram == null )
      histogram = DefaultSNAP_Histogram( num_bins );
    else
    {
      Vector3D xVec = new Vector3D(1,0,0);
      Vector3D yVec = new Vector3D(0,1,0);
      Vector3D zVec = new Vector3D(0,0,1);

      IEventBinner x_bin1D = new UniformEventBinner( -16.0f,  0,   num_bins );
      IEventBinner y_bin1D = new UniformEventBinner( -16.0f,  0,   num_bins );
      IEventBinner z_bin1D = new UniformEventBinner( - 8.0f, 8.0f, num_bins );

      ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
      ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
      ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

      histogram.setHistogramPosition( x_binner, y_binner, z_binner );
      histogram.clear();
    }
  }


/**
 * Set up the histogram to be new empty histogram covering a region
 * of reciprocal space appropriate for the TOPAZ instrument at the
 * SNS, WITHOUT reallocating memory, if possible.
 */
  synchronized private void SetHistogramForTOPAZ()
  {
    if ( histogram == null )
      histogram = DefaultSNAP_Histogram( num_bins );
    else
    {
      Vector3D xVec = new Vector3D(1,0,0);
      Vector3D yVec = new Vector3D(0,1,0);
      Vector3D zVec = new Vector3D(0,0,1);

      IEventBinner x_bin1D = new UniformEventBinner( -36.0f,  0,   num_bins );
      IEventBinner y_bin1D = new UniformEventBinner( -24.0f,  0,   num_bins );
      IEventBinner z_bin1D = new UniformEventBinner( -18.0f, 6.0f, num_bins );

      ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
      ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
      ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

      histogram.setHistogramPosition( x_binner, y_binner, z_binner );
      histogram.clear();
    }
  }


  /**
   *  Set histogram to be a new empty histogram covering a region
   *  of reciprocal space appropriate for the ARCS instrument at the
   *  SNS.
   *
   *  @param num_bins  The number of bins to use in each direction
   *                   for the histogram.
   */
  synchronized private Histogram3D DefaultARCS_Histogram( int num_bins )
  {
    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);

    IEventBinner x_bin1D = new UniformEventBinner( -50.0f,    0,  num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( -10.0f, 40.0f, num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( -25.0f, 25.0f, num_bins );

    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D histogram = new Histogram3D( x_binner,
                                             y_binner,
                                             z_binner );
    return histogram;
  }


  /**
   *  Set up the histogram to be new empty histogram covering a region
   *  of reciprocal space appropriate for the ARCS instrument at the
   *  SNS, WITHOUT reallocating memory, if possible.
   */
  synchronized private void SetHistogramForARCS()
  {
    if ( histogram == null )
      histogram = DefaultARCS_Histogram( num_bins );
    else
    {
      Vector3D xVec = new Vector3D(1,0,0);
      Vector3D yVec = new Vector3D(0,1,0);
      Vector3D zVec = new Vector3D(0,0,1);

      IEventBinner x_bin1D = new UniformEventBinner( -50.0f,    0,  num_bins );
      IEventBinner y_bin1D = new UniformEventBinner( -10.0f, 40.0f, num_bins );
      IEventBinner z_bin1D = new UniformEventBinner( -25.0f, 25.0f, num_bins );

      ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
      ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
      ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

      histogram.setHistogramPosition( x_binner, y_binner, z_binner );
      histogram.clear();
    }
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
   
     smooth_data = false;                  // NOTE: Don't smooth the data!!!!

     int num_pages = histogram.zBinner().numBins();
     int num_rows  = histogram.yBinner().numBins();
     int num_cols  = histogram.xBinner().numBins();
     
     float[][][] histogram_array = new float[num_pages][][];
     for ( int page = 0; page < num_pages; page++ )
       histogram_array[page] = histogram.pageSlice( page );

     int[] val_histogram = new int[10000];

   
     int[] row_list = new int[num_rows];
     for ( int k = 0; k < num_rows; k++ )
       row_list[k] = k + 1;

     int[] col_list = new int[num_cols];
     for ( int k = 0; k < num_cols; k++ )
       col_list[k] = k + 1;

     StringBuffer log = new StringBuffer();

                                    // TODO: change FindPeaksViaSort.getPeaks
                                    //       to use a float min_intensity 
     BasicPeakInfo[] peaks = FindPeaksViaSort.getPeaks( histogram_array,
                                                        smooth_data,
                                                        num_peaks,
                                                        (int)min_intensity,
                                                        row_list,
                                                        col_list,
                                                        0,
                                                        num_pages-1,
                                                        val_histogram,
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
//      System.out.println("Exception writing log file " + log_file );
//      ex.printStackTrace();
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
      histogram.addEvents( events );
    }
  }

}
