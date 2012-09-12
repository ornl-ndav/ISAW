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

public class TestTofToInfo
{

  public static void main( String[] args ) throws Exception
  {
    String dir        = "/usr2/TOPAZ_SAPPHIRE_JUNE_2012/";
    String input_dir  = dir + "/NEW_MANTID_SCRIPT/TEST_5/FOUND/";
//    String input_dir  = dir + "/NEW_MANTID_SCRIPT/TEST_5/PREDICTED/";
    String output_dir = "/home/dennis/NEW_EV_INTEGRATE_RESULTS/";

    String run_number = args[0];

    String ev_file    = dir + "TOPAZ_" + run_number + "_neutron_event.dat";
    String peaks_file = input_dir + run_number + "_Niggli.integrate";
//    String peaks_file = input_dir + run_number + "_fixed_Niggli.integrate";
//    String peaks_file = dir + "/NEW_MANTID_SCRIPT/TEST_5/TEST_" + run_number + "_Niggli.integrate";
    String out_file   = output_dir + "SAPPHIRE_Cylinder_4_Sigma_Radius_Recenter_CenterOfMass_" + run_number + ".integrate";

    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );
    int max_id = -1;
    // remove un-indexed peaks
    for ( int i = peaks.size()-1; i >= 0; i-- )
    {
      Peak_new peak = peaks.elementAt(i);
      if ( peak.h() == 0 && peak.k() == 0 && peak.l() == 0 )
        peaks.remove(i);
      else
        if ( peak.detnum() > max_id )
          max_id = peak.detnum();
    }

    float tol = 0.30f;

    SampleOrientation samp_or = peaks.elementAt(0).getSampleOrientation();
    Tran3D gonio_inv = samp_or.getGoniometerRotationInverse();

    Tran3D UB = PeaksFileUtils.FindUB_FromIndexing( peaks, 0.12f );
    System.out.println("UB = " + UB );

    Tran3D UB_inverse = new Tran3D( UB );
    UB_inverse.invert();

    String instrument = FileIO.getSNSInstrumentName( ev_file );

    String DetCal_filename  = "";
    String bank_filename    = "";
    String mapping_filename = "";
    SNS_Tof_to_Q_map mapper = new SNS_Tof_to_Q_map( instrument,
                                                    DetCal_filename,
                                                    bank_filename,
                                                    mapping_filename,
                                                    null );

    SNS_TofEventList tof_evl = new SNS_TofEventList( ev_file );

    int num_events = 1000000000;
    float[] info_list = mapper.MapEventsTo_Q_ID_Row_Col(tof_evl, 0, num_events); 
    System.out.println("Requested  " + num_events + " events");
    System.out.println("I Size/8 = " + info_list.length/8 );
    num_events = info_list.length/8;

    Vector<Vector<EventInfo>> lists_by_id = 
                       EV_IntegrateUtils.SplitEventsByID( max_id+1, info_list );

    for ( int i = 0; i < lists_by_id.size(); i++ )
      if ( lists_by_id.elementAt(i).size() > 0 )
        System.out.println("ID: " + i + "  Num Events: " + 
          lists_by_id.elementAt(i).size() );

//   int n_tested = 80;
    int n_tested = peaks.size();

    Vector<EventInfo> ev_list_for_det = null;
    Hashtable         table           = null;
    int last_det_id = -1;

    Vector<PeakDisplayInfo> peak_infos = null;
    Vector id_with_peak_infos = new Vector(); 
    int    peak_count = 0;
    for ( int peak_index = 0; peak_index < n_tested; peak_index++ )
    {
      Peak_new peak = peaks.elementAt( peak_index );
      int det_id = peak.detnum();
      Vector3D target_hkl = new Vector3D( Math.round(peak.h()),
                                          Math.round(peak.k()),
                                          Math.round(peak.l()) );
      if ( det_id != last_det_id )
      {
        peak_infos = new Vector<PeakDisplayInfo>();
        id_with_peak_infos.add( det_id );
        id_with_peak_infos.add( peak_infos );

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

      String key = "" + Math.round( target_hkl.getX() ) + "," +
                        Math.round( target_hkl.getY() ) + "," +
                        Math.round( target_hkl.getZ() );

      Vector<EventInfo> ev_list_for_hkl = ( Vector<EventInfo>)table.get( key );
      PeakEventList pev_list = EV_IntegrateUtils.GetPeakEventList( 
                                                              peak,
                                                              tol,
                                                              UB_inverse,
                                                              ev_list_for_hkl,
                                                              ev_list_for_det );
      if ( pev_list == null )
      {
        System.out.println("ERROR: No events in tolerance for: \n " + peak );
        continue;
      }

      peak_count++;
      float rc_radius = 30;
 
      for ( int repeat = 0; repeat < 3; repeat++ ) 
        pev_list.setCenterRowColToCenterOfMass( rc_radius );

      float sigma = pev_list.getStandardDeviation( rc_radius );
      if ( sigma <= 5 )
        sigma = 5;  

      float[] IsigI = EV_IntegrateUtils.Integrate( pev_list, 4.0f*sigma );
      peak.inti( IsigI[0] );
      peak.sigi( IsigI[1] );
      System.out.println("Current peak: " + peak );
 
      PeakDisplayInfo peak_info = 
                      EV_IntegrateUtils.GetPeakDisplayInfo( pev_list, 
                                                            4.0f*sigma, 
                                                            peak_count+1 );
      peak_infos.add( peak_info );
    }

    Peak_new_IO.WritePeaks_new( out_file, peaks, false );

    int run_num = peaks.elementAt(0).nrun();
    EV_IntegrateUtils.ShowPeakImages( run_num, id_with_peak_infos );
  }
}
