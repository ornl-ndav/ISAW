/* 
 * File: QuickIntegrateHandler.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;
import java.util.Arrays;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;
import EventTools.Histogram.*;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.FloatArrayEventList3D;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;

/**
 *  This class manages the 3D histogram that accumulates integrated
 *  intensities centered on predicted peak positions in reciprocal space.  
 *  It processes messages that clear and add events to the histogram, 
 *  and adjusts the region of reciprocal space that is binned when the
 *  instrument type, or orientation transformation.
 *
 *  @param message_center       The message center from which events to
 *                              process data are received.
 *  @param num_bins             The number of bins to use in each direction
 *                              for the histogram in reciprocal space.
 */
public class QuickIntegrateHandler implements IReceiveMessage
{
  private MessageCenter message_center;

  private Histogram3D   histogram = null;
  private float[][]     orientation_matrix;
//  private float[][]     orientation_inverse;

  private float         max_Q = 20;

  public static final float MAX_Q_ALLOWED  = 40;

  public static final float GOOD_THRESHOLD =  3; // required I/sigI

  public static final int   STEPS_PER_MI   =  5; // in each miller index 
                                                 // direction, take 5 steps
                                                 // for one integer step in hkl

  public QuickIntegrateHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    
    message_center.addReceiver( this, Commands.INIT_HISTOGRAM );
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );

    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_HISTOGRAMS );
    message_center.addReceiver( this, Commands.CLEAR_INTEGRATED_INTENSITIES );

    message_center.addReceiver( this, Commands.COUNT_INTEGRATED_INTENSITIES );
    message_center.addReceiver( this, Commands.WRITE_INTEGRATED_INTENSITIES );
  }


 /**
  *  Receive and process messages: 
  *
  *      INIT_HISTOGRAM
  *      SET_ORIENTATION_MATRIX,
  *
  *      ADD_EVENTS_TO_HISTOGRAMS, 
  *      CLEAR_INTEGRATED_INTENSITIES, 
  *
  *      COUNT_INTEGRATED_INTENSITIES
  *      WRITE_INTEGRATED_INTENSITIES
  *
  *  @param message  The message to be processed.
  *
  *  @return true If processing the message has altered something that
  *               requires a redraw of any updateable objects.
  */
  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.INIT_HISTOGRAM ) )
    {
      System.out.println("GOT INIT_HISTOGRAM IN QuickIntegrate");
      Object val = message.getValue();
      SetMaxQ( val );
    }

    else if ( message.getName().equals(Commands.SET_ORIENTATION_MATRIX) )
    {
      System.out.println("GOT NEW ORIENTATION MATRIX IN QuickIntegrate");
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

      send_stats( 0, 0 );
      SetNewHistogram();
    }

    else if ( message.getName().equals(Commands.ADD_EVENTS_TO_HISTOGRAMS) )
    {
      if ( histogram == null )             // orientation matrix not set,
        return false;                      // so no histogram yet

      IEventList3D events = (IEventList3D)message.getValue();
      if ( events == null )
        return false;

      float[] new_weights = new float[ events.numEntries() ];
      Arrays.fill( new_weights, 1 );

      float[] xyz = events.eventVals();
      IEventList3D new_events = new FloatArrayEventList3D( new_weights, xyz );

      AddEventsToHistogram( new_events );
    }

    else if ( message.getName().equals(Commands.INIT_HISTOGRAM) )
    {
      if ( histogram != null )
        histogram.clear();

      send_stats( 0, 0 );
      SetMaxQ( message.getValue() );
    }

    else if ( message.getName().equals(Commands.CLEAR_INTEGRATED_INTENSITIES) )
    {
      if ( histogram != null )
        histogram.clear();

      send_stats( 0, 0 );
    }

    else if ( message.getName().equals(Commands.COUNT_INTEGRATED_INTENSITIES) )
    {
      System.out.println("QUICK INTEGRATE GOT COUNT_INTEGRATED_INTENSITIES");
      if ( histogram != null )
      {
        Vector3D h_vec = new Vector3D( orientation_matrix[0][0],
                                       orientation_matrix[1][0],
                                       orientation_matrix[2][0] );

        Vector3D k_vec = new Vector3D( orientation_matrix[0][1],
                                       orientation_matrix[1][1],
                                       orientation_matrix[2][1] );

        Vector3D l_vec = new Vector3D( orientation_matrix[0][2],
                                       orientation_matrix[1][2],
                                       orientation_matrix[2][2] );

        System.out.println("************ Start Dumping Integrate Info");
        long start = System.nanoTime();
        int  num_integrated = 0;
        int  num_good       = 0;
        for ( int h = -20; h <= 20; h++ )       // TODO, calculate range based
          for ( int k = -20; k <= 20; k++ )     // on MaxQ
            for ( int l = -20; l <= 20; l++ )
            {
              float value = intensity_at( h, k, l, h_vec, k_vec, l_vec );
              if ( value > 0 )
              {
                float[] int_vals = getI_and_sigI(h, k, l, h_vec, k_vec, l_vec);
                System.out.printf("%3d  %3d  %3d   %8.2f  %8.2f  %8.2f\n", 
                        h, k, l, value, int_vals[0], int_vals[1] );
                num_integrated++;
                if ( int_vals[1] >= GOOD_THRESHOLD )
                  num_good++;
              }
            } 
         System.out.println("************ Done Dumping Integrate Info");
         System.out.printf("Time to integrate = %6.0fms\n",
                            (System.nanoTime() - start)/1e6 ); 
         System.out.println("Integrated " + num_integrated + " peaks.");
         System.out.println("There were " + num_good + 
                            " with I/sigI >= " + GOOD_THRESHOLD );

         send_stats( num_integrated, num_good );
      }
    }

    return false;
  }


  /**
   * Send a message with the specified information about how many peaks
   * were > 0 and how many peaks had I/sigI better than the GOOD_THRESHOLD.
   */
  private void send_stats( int num_integrated, int num_good )
  {
    int[] stats = { num_integrated, num_good };
    Message stats_mess = new Message( Commands.SET_INTEGRATED_INTENSITY_STATS,
                                      stats, true, true );
    message_center.send( stats_mess );
  }


  /**
   *  Get the intensity in the histogram at the specified (possibly fractional)
   *  h, k, l values, given the basis vectors for the reciprocal lattice.
   */
  private float intensity_at( float h, float k, float l, 
                              Vector3D h_vec, Vector3D k_vec, Vector3D l_vec )
  {
    float x = h * h_vec.getX() + k * k_vec.getX() + l * l_vec.getX();
    float y = h * h_vec.getY() + k * k_vec.getY() + l * l_vec.getY();
    float z = h * h_vec.getZ() + k * k_vec.getZ() + l * l_vec.getZ();
    float value = histogram.valueAt( x, y, z );
    return value;
  }


  /**
   *  Return the adjusted peak value and I/sigI as the first two entries
   *  in an array of floats.
   */
  private float[] getI_and_sigI( int h, int k, int l,
                                 Vector3D h_vec, 
                                 Vector3D k_vec, 
                                 Vector3D l_vec )
  {
    int   neighbor_count = 0;
    float neighbor_sum   = 0;
    float neighbor_val   = 0;
    float raw_value      = 0;
    float step_size      = 1.0f/STEPS_PER_MI;

    for ( int h_step = -1; h_step <= 1; h_step++ )
      for ( int k_step = -1; k_step <= 1; k_step++ )
        for ( int l_step = -1; l_step <= 1; l_step++ )
        {
          float new_h = h + h_step * step_size;
          float new_k = k + k_step * step_size;
          float new_l = l + l_step * step_size;
          if ( h_step == 0 && k_step == 0 && l_step == 0 )
            raw_value = intensity_at( new_h, new_k, new_l, 
                                      h_vec, k_vec, l_vec );
          else
          {
            neighbor_val = intensity_at( new_h, new_k, new_l, 
                                         h_vec, k_vec, l_vec );
            neighbor_sum += neighbor_val;
            neighbor_count++;
          } 
        }

    float signal = raw_value - neighbor_sum/neighbor_count;
    float ratio  = 1.0f / (float)neighbor_count;

    float sigma_signal = (float)
                         Math.sqrt( raw_value + ratio * ratio * neighbor_sum );

    float I_by_sigI = signal / sigma_signal;

    float[] result = { signal, I_by_sigI }; 
    return result;
  }



  private boolean SetMaxQ( Object obj )
  {
    if ( obj == null || ! (obj instanceof SetNewInstrumentCmd) )
      return false;

    SetNewInstrumentCmd cmd = (SetNewInstrumentCmd)obj;

    max_Q = cmd.getMaxQValue();
     return true;
  }


  private void SetNewHistogram()
  {
    System.out.println("QuickIntegrate now allocating histogram space....");
    if ( max_Q < .5f )           // clamp max_Q to reasonable values
      max_Q = .5f;

    if ( max_Q > MAX_Q_ALLOWED )
      max_Q = MAX_Q_ALLOWED;

    Vector3D h_vec = new Vector3D( orientation_matrix[0][0], 
                                   orientation_matrix[1][0],
                                   orientation_matrix[2][0] );

    Vector3D k_vec = new Vector3D( orientation_matrix[0][1],
                                   orientation_matrix[1][1],
                                   orientation_matrix[2][1] );

    Vector3D l_vec = new Vector3D( orientation_matrix[0][2],
                                   orientation_matrix[1][2],
                                   orientation_matrix[2][2] );

    ProjectionBinner3D h_binner = MakeBinner( h_vec );
    ProjectionBinner3D k_binner = MakeBinner( k_vec );
    ProjectionBinner3D l_binner = MakeBinner( l_vec );

    System.out.println("h_binner    = " + h_binner );
    System.out.println("k_binner    = " + k_binner );
    System.out.println("l_binner    = " + l_binner );

    histogram = new Histogram3D( h_binner, k_binner, l_binner );
    System.out.println("QuickIntegrate DONE allocating histogram space.");
  }


  private ProjectionBinner3D MakeBinner( Vector3D basis_vec )
  {
    float one_step = basis_vec.length();
    Vector3D unit_vec = new Vector3D( basis_vec );
    unit_vec.normalize();

    int   max_index = Math.round( max_Q / one_step );
    float max_dist  = (max_index + 0.5f) * one_step;

    IEventBinner bin1D = new UniformEventBinner( -max_dist, max_dist,
                                   STEPS_PER_MI * (2*max_index + 1) );

    return new ProjectionBinner3D(bin1D, unit_vec);
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
