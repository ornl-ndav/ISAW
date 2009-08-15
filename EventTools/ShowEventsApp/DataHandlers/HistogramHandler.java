
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;
import java.io.FileWriter;
import java.io.BufferedWriter;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.operator.Generic.TOF_SCD.PeakQ;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
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
import EventTools.ShowEventsApp.Command.FindPeaksCmd;


public class HistogramHandler implements IReceiveMessage
{
  private static int    NUM_THREADS = 6;
  private MessageCenter message_center;
  private Histogram3D   histogram;

  public HistogramHandler( MessageCenter message_center, int num_bins )
  {
    this.message_center = message_center;
    this.histogram      = DefaultHistogram( num_bins );;
    message_center.addReceiver( this, Commands.ADD_EVENTS );
    message_center.addReceiver( this, Commands.CLEAR_HISTOGRAM );
    message_center.addReceiver( this, Commands.SET_WEIGHTS_FROM_HISTOGRAM );
    message_center.addReceiver( this, Commands.ADD_HISTOGRAM_INFO );
    message_center.addReceiver( this, Commands.FIND_PEAKS );
  }


  public boolean receive( Message message )
  {
    System.out.println("***HistogramHandler in thread " 
                       + Thread.currentThread());

    if ( message.getName().equals(Commands.ADD_EVENTS) )
    {
      IEventList3D events = (IEventList3D)message.getValue();
      histogram.addEvents( events );
      System.out.println("MIN HISTOGRAM BIN " + histogram.minVal() );
      System.out.println("MAX HISTOGRAM BIN " + histogram.maxVal() );
    }

    else if (  message.getName().equals(Commands.CLEAR_HISTOGRAM) )
    {
      histogram.clear();
      System.out.println("CLEARED HISTOGRAM");
    }

    else if ( message.getName().equals(Commands.SET_WEIGHTS_FROM_HISTOGRAM))
    {
      IEventList3D events = (IEventList3D)message.getValue();
      SetWeightsFromHistogram( events, histogram );
    }

    else if ( message.getName().equals(Commands.ADD_HISTOGRAM_INFO))
    {
      Object val = message.getValue();
      if ( val instanceof SelectionInfoCmd )         // fill in counts field
      {
        SelectionInfoCmd select_info_cmd = (SelectionInfoCmd)val;

        AddHistogramInfo( select_info_cmd, histogram );

        Message info_message = new Message( Commands.ADD_HISTOGRAM_INFO_ACK, 
                                            select_info_cmd, 
                                            true );
        System.out.println("SET COUNT FROM HISTOGRAM");
        message_center.receive( info_message );
      }
    }

    else if ( message.getName().equals(Commands.FIND_PEAKS) )
    {
      Object val = message.getValue();
      if ( val instanceof FindPeaksCmd )  
      {
        FindPeaksCmd cmd = (FindPeaksCmd)message.getValue();
        System.out.println( "HistogramHandler processing: " +cmd.toString() );
        
        Vector<PeakQ> peakQs = FindPeaks( histogram,
                                          cmd.getSmoothData(),
                                          cmd.getMaxNumberPeaks(),
                                          cmd.getMinPeakIntensity(),
                                          cmd.getLogFileName() );

        System.out.println( "Back from find peaks, size = " + peakQs.size() );
        if ( peakQs != null && peakQs.size() > 0 || cmd.getMarkPeaks() )
        {                                                // mark the peaks
          Message mark_peaks  = new Message( Commands.MARK_PEAKS,
                                             peakQs,
                                             true );
          message_center.receive( mark_peaks );

          Message set_peak_Q_list = new Message( Commands.SET_PEAK_Q_LIST,
                                                 peakQs,
                                                 true );
          message_center.receive( set_peak_Q_list );
        }
      }
    }

    return false;
  }


  public static void SetWeightsFromHistogram( IEventList3D events, 
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

  
  public static void AddHistogramInfo( SelectionInfoCmd select_info_cmd,
                                       Histogram3D      histogram )
  {
    Vector3D Qxyz = select_info_cmd.getQxyz();

    float counts = histogram.valueAt( (float)(Qxyz.getX() * 2 * Math.PI), 
                                      (float)(Qxyz.getY() * 2 * Math.PI),
                                      (float)(Qxyz.getZ() * 2 * Math.PI) );
    select_info_cmd.setCounts( counts );
                                              // TODO MUST ALSO SET PAGE
  }


  public Histogram3D DefaultHistogram( int num_bins )
  {
    // Just make default histogram aligned with coord axes.

    long start_time = System.nanoTime();
    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);

/*   FOR SNAP:
*/
    IEventBinner x_bin1D = new UniformEventBinner( -16.0f,  0,   num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( -16.0f,  0,   num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( - 8.0f, 8.0f, num_bins );

/*   FOR ARCS:
    IEventBinner x_bin1D = new UniformEventBinner( -50.0f,    0,  num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( -10.0f, 40.0f, num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( -25.0f, 25.0f, num_bins );
*/

    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D histogram = new Histogram3D( x_binner,
                                             y_binner,
                                             z_binner );
    long run_time = System.nanoTime() - start_time;
    System.out.println("Time(ms) to allocate default histogram = " +
                        run_time/1.e6);

    return histogram;
  }


  public Vector<PeakQ> FindPeaks( Histogram3D histogram,
                                  boolean     smooth_data,
                                  int         num_peaks,
                                  float       min_intensity,
                                  String      log_file )
 
  {
     System.out.println("START OF FindPeaks");
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
      System.out.println("Exception writing log file " + log_file );
      ex.printStackTrace();
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
      System.out.println("FOUND 0 PEAKS " );
    else
      System.out.println("FOUND " + q_peaks.size() + " PEAKS " );

    return q_peaks;
  }                         

}
