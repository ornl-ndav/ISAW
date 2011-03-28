/* 
 * File: SliceHistogramHandler.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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
 *  $Author: $
 *  $Date:  $            
 *  $Revision: $
 */

package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;
import java.util.Arrays;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.MathTools.Geometry.Tran3D;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.Histogram.*;
import EventTools.EventList.IEventList3D;
import EventTools.EventList.FloatArrayEventList3D;
import EventTools.ShowEventsApp.Controls.SliceControls.SliceSelectorPanel;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.PeakImagesCmd;
import EventTools.ShowEventsApp.Command.InitSlicesCmd;
import EventTools.ShowEventsApp.Command.Util;

import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

/**
 *  This class manages the 3D histogram that accumulates counts in a 
 *  collection of slices in reciprocal space.
 *  It processes messages that clear and add events to the histogram, 
 *  and adjusts the region of reciprocal space that is binned.
 */
public class SliceHistogramHandler implements IReceiveMessage
{
  private MessageCenter message_center;

  private float[][]     orientation_matrix;

  private Histogram3D   histogram = null;

  private boolean       use_weights = false;

/**
 * Construct a SliceHistogramHandler that will use the specified message
 * center to send and receive messages.
 *
 *  @param message_center       The message center from which events to
 *                              process data are received.
 */
  public SliceHistogramHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    
    message_center.addReceiver( this, Commands.INIT_HISTOGRAM );
    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_HISTOGRAMS );
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );

    message_center.addReceiver( this, Commands.FREE_SLICES_HISTOGRAM );
    message_center.addReceiver( this, Commands.INIT_SLICES_HISTOGRAM );
    message_center.addReceiver( this, Commands.SHOW_SLICES_HISTOGRAM );
  }


 /**
  *  Receive and process messages: 
  *
  *      INIT_HISTOGRAM
  *      SET_ORIENTATION_MATRIX,
  *      ADD_EVENTS_TO_HISTOGRAMS, 
  *
  *      INIT_SLICES_HISTOGRAM, 
  *      FREE_SLICES_HISTOGRAM, 
  *
  *  @param message  The message to be processed.
  *
  *  @return true If processing the message has altered something that
  *               requires a redraw of any updateable objects.
  */
  public boolean receive( Message message )
  {
    if ( message == null )
      return false;

    if ( message.getName().equals(Commands.SET_ORIENTATION_MATRIX) )
    {
      Object val = message.getValue();
      if ( val == null || !( val instanceof Vector ) )
      {
        Util.sendError( "NULL orientation matrix in command " +
                         Commands.SET_ORIENTATION_MATRIX );
        return false;
      }

      Vector vec = (Vector)val;
      if ( vec.size() < 1 || !( vec.elementAt(0) instanceof float[][] ) )
      {
        Util.sendError( "NO orientation matrix in command " +
                         Commands.SET_ORIENTATION_MATRIX );
        return false;
      }

      float[][] UBT = (float[][]) vec.elementAt(0);
      orientation_matrix = LinearAlgebra.getTranspose( UBT );
      for ( int row = 0; row < 3; row++ )
        for ( int col = 0; col < 3; col++ )
           orientation_matrix[row][col] *= (float)(2*Math.PI);
/*
      System.out.println("SliceHistogramHandler, orientation_matrix = " );
      LinearAlgebra.print( orientation_matrix );
*/
      histogram = null;
      Message freed = new Message( Commands.SLICES_HISTOGRAM_FREED,
                                   null, true, true );
      message_center.send( freed );
    }

    else if ( message.getName().equals(Commands.INIT_HISTOGRAM) )
    {
      if ( histogram != null )
      {
        histogram.clear();
        Message allocated = new Message( Commands.SLICES_HISTOGRAM_READY,
                                         null, true, true );
        message_center.send( allocated );
      }
    }

    else if ( message.getName().equals(Commands.INIT_SLICES_HISTOGRAM) )
    {
      boolean histogram_ok = false;
      Object obj = message.getValue();
      if ( obj != null && obj instanceof InitSlicesCmd )
      {
        InitSlicesCmd cmd = (InitSlicesCmd)obj;
        this.use_weights = cmd.useWeights();
        boolean use_HKL  = cmd.useHKL();
        Vector3D center  = cmd.getCenterPoint();
        Vector3D dir_1   = cmd.getDirection_1().getDirection();
        double   step_1  = cmd.getDirection_1().getStepSize();
        int      num_1   = cmd.getDirection_1().getNumBins();
        Vector3D dir_2   = cmd.getDirection_2().getDirection();
        double   step_2  = cmd.getDirection_2().getStepSize();
        int      num_2   = cmd.getDirection_2().getNumBins();
        Vector3D dir_3   = cmd.getDirection_3().getDirection();
        double   step_3  = cmd.getDirection_3().getStepSize();
        int      num_3   = cmd.getDirection_3().getNumBins();
        String   shape   = cmd.getShape();
        histogram_ok = SetNewHistogram( use_HKL, center,
                                        dir_1, step_1, num_1,
                                        dir_2, step_2, num_2,
                                        dir_3, step_3, num_3,
                                        shape );
      }
    
      if ( histogram_ok )
      {
        Message allocated = new Message( Commands.SLICES_HISTOGRAM_READY,
                                         null, true, true );
        message_center.send( allocated );
      }
      else
      {
        histogram = null;
        Message freed = new Message( Commands.SLICES_HISTOGRAM_FREED,
                                     null, true, true );
        message_center.send( freed );
      }
    }

    else if ( message.getName().equals(Commands.FREE_SLICES_HISTOGRAM ))
    {
      histogram = null;
      Message freed = new Message( Commands.SLICES_HISTOGRAM_FREED,
                                   null, true, true );
      message_center.send( freed );
    }

    else if ( message.getName().equals(Commands.SHOW_SLICES_HISTOGRAM ))
    {
      if ( histogram != null )
      {
        Vector regions = new Vector();
        regions.add( histogram );
        PeakImagesCmd peak_image_cmd = new PeakImagesCmd( null, regions );

        Message peak_images_message =
          new Message(Commands.SHOW_PEAK_IMAGES, peak_image_cmd, true, true);
        message_center.send( peak_images_message );
      }
    }

    else if ( message.getName().equals(Commands.ADD_EVENTS_TO_HISTOGRAMS) )
    {
      if ( histogram == null )             // no histogram yet
        return false; 

      IEventList3D events = (IEventList3D)message.getValue();
      if ( events == null )
        return false;

      AddEventsToHistogram( events, use_weights );
      Message added = new Message(Commands.ADDED_EVENTS_TO_SLICES_HISTOGRAM,
                                  null, true, true );
      message_center.send( added );
    }

    return false;
  }


  /**
   * Set up the new histogram for accumulating integrated intensities.
   * @return the total number of bins required.
   */
  private boolean SetNewHistogram( boolean  use_HKL, 
                                   Vector3D center, 
                                   Vector3D dir_1, 
                                   double   step_1, 
                                   int      num_1,
                                   Vector3D dir_2, 
                                   double   step_2, 
                                   int      num_2,
                                   Vector3D dir_3, 
                                   double   step_3, 
                                   int      num_3,
                                   String   shape )

  {
    if ( use_HKL )                        // map vectors and sizes to Q first
    {
      if ( orientation_matrix != null )
      {
        float[] steps = SwitchHKL_to_Q( center,
                                        dir_1, step_1,
                                        dir_2, step_2,
                                        dir_3, step_3 );
        step_1 = steps[0];
        step_2 = steps[1];
        step_3 = steps[2];
      }
      else
        Util.sendError("NO ORIENTATION MATRIX, INTERPRETING REGION IN Q");
    }

    if ( shape.equalsIgnoreCase( SliceSelectorPanel.SHAPE_COLS_PERP_ROWS ) ||
         shape.equalsIgnoreCase( SliceSelectorPanel.SHAPE_ALL_PERP )  )
    {
                                            // Adjust dir_2 to be perpendicular
                                            // to dir_3 if not already so
      float component = dir_2.dot( dir_3 );
      if ( component != 0 )     
      {
        Vector3D new_dir_2 = new Vector3D( dir_2 );
        Vector3D temp      = new Vector3D( dir_3 );
        temp.multiply( component );
        new_dir_2.subtract( temp );  
        new_dir_2.normalize();
        float scale = dir_2.dot( new_dir_2 );    // adjust step along new_dir_2
        step_2 *= scale;
        dir_2 = new_dir_2;
      }
    }

    if ( shape.equalsIgnoreCase( SliceSelectorPanel.SHAPE_ALL_PERP ) ) 
    {
                                         // also make dir_1 to be perpendicular
                                         // to dir_2 AND dir_3 if not already 
      Vector3D perp_vec = new Vector3D(); 
      perp_vec.cross( dir_2, dir_3 );
      perp_vec.normalize();

      if ( perp_vec.dot( dir_1 ) < 0 )       // flip perp_vec to be in same
        perp_vec.multiply( -1 );             // general direction as dir_1

      if ( perp_vec.dot( dir_1 ) != 1.0f )   // need to adjust dir_1
      {
        float scale = perp_vec.dot( dir_1 ); // adjust step along new_dir_1
        step_1 *= scale;
        dir_1   = perp_vec;
      }
    } 

    histogram = null;
                                   // make the corner vector from the center
                                   // vector by subtracting 1/2 edge vectors
                                   // PLUS 1/2 step, to put corner on OUTER
                                   // edge of the corner bin!
    Vector3D delta_1 = new Vector3D( dir_1 );
    Vector3D delta_2 = new Vector3D( dir_2 );
    Vector3D delta_3 = new Vector3D( dir_3 );
    delta_1.multiply( (float)( ((num_1 / 2) + 0.5) * step_1) );
    delta_2.multiply( (float)( ((num_2 / 2) + 0.5) * step_2) );
    delta_3.multiply( (float)( ((num_3 / 2) + 0.5) * step_3) );
    Vector3D corner = new Vector3D( center );
    corner.subtract( delta_1 );
    corner.subtract( delta_2 );
    corner.subtract( delta_3 );

                                    // get dual directions to use to determine 
                                    // coordinates along the direction vectors
    IProjectionBinner3D[] dual_binners = MakeDualBinners( dir_1, dir_2, dir_3 );
    Vector3D dir_1_dual = dual_binners[0].directionVec();
    Vector3D dir_2_dual = dual_binners[1].directionVec();
    Vector3D dir_3_dual = dual_binners[2].directionVec();

                                    // NOTE: directions x,y,z are reversed from
                                    // directions 1,2,3, since for Synergia
                                    // convention direction 1 must have the
                                    // varying index.  This is z in histogram
    ProjectionBinner3D x_binner = MakeBinner( corner, 
                                              dir_3, dir_3_dual,
                                              step_3, num_3 );
    ProjectionBinner3D y_binner = MakeBinner( corner,
                                              dir_2, dir_2_dual,
                                              step_2, num_2 );
    ProjectionBinner3D z_binner = MakeBinner( corner, 
                                              dir_1, dir_1_dual,
                                              step_1, num_1 );
    try
    {
      histogram = new Histogram3D( x_binner, y_binner, z_binner );
      
      Message allocated = new Message( Commands.SLICES_HISTOGRAM_READY,
                                       null, true, true );
      message_center.send( allocated );
    }
    catch ( Exception ex )
    {
      Util.sendError("Failed to allocate Histogram! \n " +
                     " You reduce the number of slices\n " +
                     " and/or use smaller slices");
      return false;
    }

    return true;
  }


  /**
   *  Change the specified center and direction vectors to be in terms of
   *  Q, rather than in terms of h,k,l.  These vector parameters are changed
   *  by this method.  The new sizes required are returned in an array of
   *  floats. 
   */
  private float[] SwitchHKL_to_Q( Vector3D center,
                                  Vector3D dir_1,
                                  double   step_1,
                                  Vector3D dir_2,
                                  double   step_2,
                                  Vector3D dir_3,
                                  double   step_3 )
  { 
    Tran3D or_tran = new Tran3D( orientation_matrix );

    or_tran.apply_to( center, center );    // map center from HKL to Q

    dir_1.normalize();                     // map dir vectors from HK to Q
    dir_2.normalize();    
    dir_3.normalize();    

    Vector3D q1 = new Vector3D();
    Vector3D q2 = new Vector3D();
    Vector3D q3 = new Vector3D();

    or_tran.apply_to( dir_1, q1 );
    or_tran.apply_to( dir_2, q2 );
    or_tran.apply_to( dir_3, q3 );

    float new_step_1 = (float)(step_1 * q1.length()); 
    float new_step_2 = (float)(step_2 * q2.length()); 
    float new_step_3 = (float)(step_3 * q3.length()); 

    q1.normalize();                        // make sure we have unit 
    q2.normalize();                        // direction vectors 
    q3.normalize();

    dir_1.set( q1 );                       // now update direction vectors
    dir_2.set( q2 );                       // to the unit vectors in Q
    dir_3.set( q3 );

    float[] new_steps = { new_step_1, new_step_2, new_step_3 }; 

    return new_steps;                      // return the new step sizes
  }


  /**
   *  NOTE: At this point we only care about the direction vector of the
   *  dual binners!
   */
  private IProjectionBinner3D[] MakeDualBinners( Vector3D dir_1,
                                                 Vector3D dir_2,
                                                 Vector3D dir_3 )
  {
    IEventBinner unif_binner  = new UniformEventBinner( 0, 1, 10 ); 
    ProjectionBinner3D temp_1 = new ProjectionBinner3D( unif_binner, dir_1 ); 
    ProjectionBinner3D temp_2 = new ProjectionBinner3D( unif_binner, dir_2 ); 
    ProjectionBinner3D temp_3 = new ProjectionBinner3D( unif_binner, dir_3 ); 
    return ProjectionBinner3D.getDualBinners( temp_1, temp_2, temp_3 );
  }

  /**
   *  Get a valid ProjectionBinner3 object to represent the edge of a 3D
   *  histogram, starting at the specified corner.  Determining the correct
   *  range of values along that edge is a bit tricky.  Since binning events
   *  is carried out relative to the dual binners, coordinates along the edge
   *  must first be determined relative to the dual binner and then scaled
   *  to correspond to lengths along the binner direction.  Coordinates along
   *  the edge will start at the origin and proceed for the determined length.
   *  However, in general, these coordinates will not start at zero, but will
   *  be shifted to start a the coordinate of the corner point.  The required
   *  coordinate of the corner point relative to the dual direction is just
   *  corner dot dual_direction.  However, this needs to be modified to 
   *  correspond to the coordinate along the edge direction by multiplying
   *  by 1/(dual_direction dot direction).
   */
  private ProjectionBinner3D MakeBinner( Vector3D corner,
                                         Vector3D direction,
                                         Vector3D dual_direction,
                                         double   step,
                                         int      num_bins )
  {
    direction.normalize();               // Make sure the direction vectors
    dual_direction.normalize();          // are unit vectors

    double length = num_bins * step;
    double shift  = (double)dual_direction.dot( corner ) / 
                    (double)dual_direction.dot( direction );
    double min = 0 + shift;
    double max = length + shift;
    IEventBinner scalar_binner = new UniformEventBinner( min, max, num_bins );

    return new ProjectionBinner3D( scalar_binner, direction );
  }


  /**
   *  Add a list of events to the current histogram.
   *
   *  @param events      The list of events in reciprocal space
   *  @param use_weights Flag indicating whether the events should be weighted
   *                     using the weights determined from choices on the
   *                     Load Data screen, or if each event should count 1.
   */
  synchronized public void AddEventsToHistogram( IEventList3D events,
                                                 boolean      use_weights )
  {
                                     // don't search for peaks and change
    synchronized( histogram )        // the histogram at the same time
    {
      histogram.addEvents( events, use_weights );
    }
  }

}
