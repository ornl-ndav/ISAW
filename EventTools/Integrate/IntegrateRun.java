/* 
 * File: IntegrateRun.java
 *
 * Copyright (C) 2012, Dennis Mikkelson
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
 *  $Author: dennis $
 *  $Date: 2012/11/09 15:40:55 $            
 *  $Revision: 1.2 $
 */

package EventTools.Integrate;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakDisplayInfo;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeaksDisplayPanel;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakArrayPanels;

import java.io.*;
import java.util.*;
import gov.anl.ipns.Util.File.*;
import EventTools.EventList.*;
import EventTools.Histogram.*;
import Operators.TOF_SCD.*;
import gov.anl.ipns.MathTools.Geometry.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.instruments.*;

public class IntegrateRun 
{

/**
 *  Integrate the peaks in specified peaks file, using data from the 
 *  specified SNS raw event file.
 *
 *  @param ev_file      Fully qualified event file name
 *  @param peaks_file   File of peaks to integrate
 *  @param out_file     Name of file where the integrated peaks are
 *                      written.
 */
  public static void IntegrateRun( String ev_file, 
                                   String peaks_file, 
                                   String out_file ) throws Exception
  {
    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );

    IntegrateRun( ev_file, peaks );

    Peak_new_IO.WritePeaks_new( out_file, peaks, false );
  }


/**
 *  Integrate the specified list of peaks, using data from the specified
 *  SNS raw event file.
 *
 *  @param ev_file      Fully qualified event file name
 *  @param peaks        Vector of peaks to integrate
 */
  public static void IntegrateRun( String ev_file, Vector<Peak_new> peaks )
                     throws Exception
  {
    int max_id = -1;
                                                // remove un-indexed peaks and
                                                // scan for the maximum det ID
    for ( int i = peaks.size()-1; i >= 0; i-- )
    {
      Peak_new peak = peaks.elementAt(i);
      if ( peak.h() == 0 && peak.k() == 0 && peak.l() == 0 )
        peaks.remove(i);
      else
        if ( peak.detnum() > max_id )
          max_id = peak.detnum();
    }

    SampleOrientation samp_or = peaks.elementAt(0).getSampleOrientation();
    Tran3D gonio_inv = samp_or.getGoniometerRotationInverse();

    Tran3D UB = PeaksFileUtils.FindUB_FromIndexing( peaks, 0.12f );
    System.out.println("UB = " + UB );

    Tran3D UB_inverse = new Tran3D( UB );
    UB_inverse.invert();

    String instrument = FileIO.getSNSInstrumentName( ev_file );

    String DetCal_file  = "";
    String bank_file    = "";
    String mapping_file = "";
    SNS_Tof_to_Q_map mapper = new SNS_Tof_to_Q_map( instrument,
                                                    DetCal_file,
                                                    bank_file,
                                                    mapping_file,
                                                    null );

    SNS_TofEventList tof_evl = new SNS_TofEventList( ev_file );

    int num_events = 2000000000;
    float[] info_list = mapper.MapEventsTo_Q_ID_Row_Col(tof_evl, 0, num_events); 
    num_events        = info_list.length/8;

                                        // get separate lists of event objects
                                        // for each different detector ID
    Vector<Vector<EventInfo>> lists_by_id = 
                       EV_IntegrateUtils.SplitEventsByID( max_id+1, info_list );

    int n_tested = peaks.size();

    Vector<EventInfo> ev_list_for_det = null;
    Hashtable         table           = null;
    int last_det_id = -1;

    int    peak_count = 0;
    Vector<Peak_new> integrated_peaks = new Vector<Peak_new>();

    for ( int peak_index = 0; peak_index < n_tested; peak_index++ )
    {
      Peak_new peak = peaks.elementAt( peak_index );
                                   // if first peak in a detector, get a new
                                   // list for display and split the events
                                   // for this detector into lists per hkl
                                   // with events closest to each hkl
      int det_id = peak.detnum();
      if ( det_id != last_det_id )
      {
        ev_list_for_det = lists_by_id.elementAt( det_id );
        table = EV_IntegrateUtils.SplitEventsByHKL( UB_inverse, 
                                                    gonio_inv, 
                                                    ev_list_for_det );
        last_det_id = det_id;
      }

      if ( ev_list_for_det == null || ev_list_for_det.size() <= 0 )
      {
        System.out.println("ERROR: No events found for " + det_id );
        continue;
      }

      Vector3D target_hkl = new Vector3D( Math.round(peak.h()),
                                          Math.round(peak.k()),
                                          Math.round(peak.l()) );

      String key = "" + Math.round( target_hkl.getX() ) + "," +
                        Math.round( target_hkl.getY() ) + "," +
                        Math.round( target_hkl.getZ() );

      Vector<EventInfo> ev_list_for_hkl = ( Vector<EventInfo>)table.get( key );
      if ( ev_list_for_hkl.size() == 0 )
        continue;

      float tol = 0.30f;
      PeakEventList pev_list = EV_IntegrateUtils.GetPeakEventList( 
                                           peak, tol, UB_inverse,
                                           ev_list_for_hkl, ev_list_for_det );
      if ( pev_list == null )
      {
        System.out.println("ERROR: No events in tolerance for: \n " + peak );
        continue;
      }

      float rc_radius = 20; 
      int   border    = 5;
 
      peak.seqnum( peak_count+1 );

      for ( int repeat = 0; repeat < 2; repeat++ )
        pev_list.setCenterRowColToCenterOfMass( rc_radius/2 );

      float sigma = pev_list.getStandardDeviation( rc_radius );
      if ( sigma <= 5 )
        sigma = 5; 
      else if ( sigma > 20 )
        sigma = 20;

      PeakEventList.PeakType type = pev_list.GetPeakType( rc_radius/2, border );
                                 // treat as edge peak if within 15 of border
                 
      if ( type == PeakEventList.PeakType.INTERIOR )
      { 
        float[] IsigI = EV_IntegrateUtils.IntegrateSlices( pev_list, 3*sigma );

        peak_count++;

        peak.inti( IsigI[0] );
        peak.sigi( IsigI[1] );
        integrated_peaks.add( peak );
        System.out.println("Peak NUM " + peak_count + " sigma = " + sigma + " intI = " + IsigI[0]);
      }
    }

                                // now copy the integrated peaks back to 
    peaks.clear();              // the original list
    for ( int i = 0; i < integrated_peaks.size(); i++ )
      peaks.add( integrated_peaks.elementAt(i) );
  }


  public static void main( String args[] ) throws Exception
  {
    String dir        = "/usr2/TOPAZ_SAPPHIRE_JUNE_2012/";
    String input_dir  = dir + "/NEW_MANTID_SCRIPT/TEST_5/FOUND/";
    String output_dir = "/home/dennis/NEW_EV_INTEGRATE_RESULTS/";

    String run_number = args[0];

    String ev_file    = dir + "TOPAZ_" + run_number + "_neutron_event.dat";
    String peaks_file = input_dir + run_number + "_Niggli.integrate";
    String out_file   = output_dir + "SAPPH_Slice_Cylinder_30_Radius_NO_Recenter_NO_Edge_" +
                        run_number + ".integrate";

    IntegrateRun( ev_file, peaks_file, out_file );
  }

}
