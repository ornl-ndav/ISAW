/* 
 * File: ViewerTest.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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

package EventTools.Viewers;

import java.io.IOException;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.ViewTools.Panels.Image.*;

import EventTools.EventList.ByteFile16EventList3D;
import EventTools.Histogram.Histogram3D;
import EventTools.Histogram.IEventBinner;
import EventTools.Histogram.ProjectionBinner3D;
import EventTools.Histogram.UniformEventBinner;

/**
 *  This class provides a basic test of some of the capabilities of
 *  the new 3D event and histogram viewers.
 */

public class ViewerTest
{

  public static void main( String args[] ) throws IOException
  {
    int NUM_BINS = 512;

    long start = System.nanoTime();
    long elapsed;
    
    ByteFile16EventList3D events = new ByteFile16EventList3D(args[0]);
    int num_events = events.numEntries();

    // Check event access methods
    double[] event_xyz = new double[3]; 
    int small_count = 0;
    for ( int i = 0; i < num_events; i++ )
    {
      events.eventVals( i, event_xyz );
      if ( events.eventX(i) != event_xyz[0] )
        System.out.println("Error in X value " + event_xyz[0] + " " +
                                                 events.eventX(i) );
      if ( events.eventY(i) != event_xyz[1] )
        System.out.println("Error in Y value " + event_xyz[1] + " " +
                                                 events.eventY(i) );
      if ( events.eventZ(i) != event_xyz[2] )
        System.out.println("Error in Z value " + event_xyz[2] + " " +
                                                 events.eventZ(i) );

      if ( events.eventX(i) <    0 && events.eventX(i) > -10 &&
           events.eventY(i) >    0 && events.eventY(i) <  10 &&
           events.eventZ(i) > -.01 && events.eventZ(i) <  .01    )
         small_count++;
    }
    System.out.println("In ViewerTest "+small_count+" |z|'s less than 0.01" );

    elapsed = System.nanoTime()-start;
    System.out.println("Time(ms) to read file = " + elapsed/1.0E6);
    
    start = System.nanoTime();
    ShowEventList.show_events( events );
    elapsed = System.nanoTime() - start;
    System.out.println("Time(ms) to show events = " + elapsed/1.0E6);

    System.out.println("Number of event records = " + events.numEntries() );

    start = System.nanoTime();
    int sum = 0;
    for ( int i = 0; i < num_events; i++ )
      sum += events.eventCode(i);
    
    elapsed = System.nanoTime() - start;
    System.out.println("Time(ms) to sum events = " + elapsed/1.0E6);
    System.out.println("Total events = " + sum);

    start = System.nanoTime();
    
    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);
/*
    IEventBinner x_bin1D = new UniformEventBinner( -10,  0, NUM_BINS );
    IEventBinner y_bin1D = new UniformEventBinner(   0, 10, NUM_BINS );
    IEventBinner z_bin1D = new UniformEventBinner(  -5,  5, NUM_BINS );
*/
    IEventBinner x_bin1D = new UniformEventBinner( -25,  0, NUM_BINS );
    IEventBinner y_bin1D = new UniformEventBinner(   0, 25, NUM_BINS );
    IEventBinner z_bin1D = new UniformEventBinner( -12.5f, 12.5f, NUM_BINS );

    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D hist_3D = new Histogram3D(x_binner, y_binner, z_binner); 
    elapsed = System.nanoTime()-start;
    System.out.println("Time(ms) to allocate histogram = " + elapsed/1.0E6);

    start = System.nanoTime();
    hist_3D.addEvents( events );
    elapsed = System.nanoTime()-start;
    System.out.println("Time(ms) to fill histogram = " + elapsed/1.0E6);
    
    float [][] image = null;
/*
    start = System.nanoTime();
    for ( int i = 0; i < NUM_BINS; i++ )
      image = hist_3D.pageSlice(i);
    elapsed = System.nanoTime()-start;
    System.out.println("Time(ms) to get all pages = " + elapsed/1.0E6);

    start = System.nanoTime();
    for ( int i = 0; i < NUM_BINS; i++ )
      image = hist_3D.rowSlice(i);
    elapsed = System.nanoTime()-start;
    System.out.println("Time(ms) to get all rows = " + elapsed/1.0E6);

//  image = hist_3D.getPage(NUM_BINS/2);
*/
    image = hist_3D.rowSlice(NUM_BINS/2);

    JFrame f = new JFrame("Test for ImageJPanel");
    f.setBounds(0,0,500,500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ImageJPanel panel = new ImageJPanel();
    panel.setData( image, true );
    panel.setNamedColorModel( IndexColorMaker.HEATED_OBJECT_SCALE_2,
                              true,
                              true );
    f.getContentPane().add(panel);
    f.setVisible(true);
    panel.changeLogScale(50,true);
/*
    for ( int i = 0; i < NUM_BINS; i++ )
    {
      image = hist_3D.rowSlice(i);
      panel.setData( image, true );
      try
      {
        Thread.sleep(50);
      }
      catch ( Exception ex ) 
      {
      }
    }
*/
    panel.setData( hist_3D.pageSlice(NUM_BINS/2), true );

    float min  =  25;
    float max  =  1000;
    int   bins =  20;
    UniformEventBinner binner = new UniformEventBinner( min, max, bins );
    ShowHistogram.show_histogram( hist_3D, binner );
/* 
    start = System.nanoTime();
    hist_3D.clear();
    elapsed = System.nanoTime()-start;
    System.out.println("Time(ms) to clear histogram = " + elapsed/1.0E6);
*/

    start = System.nanoTime();
    ShowEventList.show_events( events, hist_3D, binner );    
    elapsed = System.nanoTime()-start;
    System.out.println("Time(ms) to show_events /w histogram = "+elapsed/1.0E6);
  }

}
