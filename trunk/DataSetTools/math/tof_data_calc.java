/*
 * @(#)tof_data_calc.java    0.01 2000/07/17  Dennis Mikkelson
 *
 *  Basic time-of-flight calculations on DataSets and Data blocks
 *
 *  $LOG:$
 */

package DataSetTools.math;

import DataSetTools.peak.*;
import DataSetTools.dataset.*;

public final class tof_data_calc
{

public static final float  MONITOR_PEAK_EXTENT_FACTOR = 8.5f;
                                  // determines interval over which the 
                                  // monitor peaks are evaluated
 
  /**
   * Don't let anyone instantiate this class.
   */
  private tof_data_calc() {}


/* ------------------------- EnergyFromMonitorData ------------------------- */
/**
 *  Calculate the incident energy of a neutron beam based on the pulse data
 *  for two beam monitors. 
 *
 *  @param  mon_1_data   The Data block for the first beam monitor
 *  @param  mon_2_data   The Data block for the second beam monitor
 *
 *  @return  The energy of the beam in meV
 */

 public static float EnergyFromMonitorData( Data mon_1_data, Data mon_2_data )
  {
     Data mon[] = new Data[2];
     mon[0] = mon_1_data;
     mon[1] = mon_2_data;
                                       // for each monitor, find the peak
                                       // centroid and x position of the
                                       // detector
     float centroid[] = new float[2];
     float x[]        = new float[2];
     for ( int i = 0; i < 2; i++ )
     {
       HistogramDataPeak peak = 
                  new HistogramDataPeak( mon[i], MONITOR_PEAK_EXTENT_FACTOR );

       float area        = peak.Area();
       centroid[i] = peak.Moment( 0, 1) / area;

       DetectorPosition position = (DetectorPosition)
                         mon[i].getAttributeValue(Attribute.DETECTOR_POS);
       float coords[] = position.getCartesianCoords();
       x[i] = coords[0];
     }

     float energy = tof_calc.Energy( x[1]-x[0], centroid[1]-centroid[0] );
     return energy;
  }


}
