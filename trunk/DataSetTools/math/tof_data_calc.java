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



/**
 *  Calculate detector efficiency factor and correction to flight path for
 *  a detector, based on the neutron final speed.  This code was adapted from
 *  Chun Loong's FORTRAN version by Dongfeng Chen.
 *
 *  @param  finalSpeed    The speed of the detected neutron in meters per 
 *                        micro-second.
 *  @param  detectorType  Integer code for the detector type__________________
 *
 *  @return an array with two values, the detector efficiency factor at the
 *          specified final speed and a flight path correction to be added 
 *          to the flight path for the specified final speed.
 */
 public static float[] getEfficiencyFactor(float finalSpeed, 
                                           int   detectorType      )
 {
    finalSpeed *= 1000000;   // Originally this function used finalSpeed in
                             // meters per second.

                       // SIGMA(detectorType) is macroscopic absorption 
                       // cross section at 2200 m/sec

    float[] SIGMA = { 0.80017f, 0.80017f, 1.0669f, 1.0669f, 0.80017f };

    float R    = 1.27f*(1.0f-0.063f);
    float DR   = R/100.0f;
    float ABS  = 0.0f;

    float FPCORR = 0.0f;

    float Y     = 0.0f;
    float X     = 0.0f;
    float SIGX  = 0.0f;
    float TRANS = 0.0f;

    for (int i=0; i<100; i++)
    {
      Y      = DR * (i+1-0.5f);
      X      = (float)Math.sqrt( R * R - Y * Y );
      SIGX   = X * SIGMA[ detectorType ] * 2200.0f / finalSpeed;
      TRANS  = (float)Math.exp( -2.0f*SIGX );
      ABS    = ABS + ( 1.0f-TRANS );
      FPCORR = FPCORR - X *( 1.0f-1.0f/SIGX + (1.0f+1.0f/SIGX) * TRANS );
     }

     float [] result = new float[2];

     result[0] = ABS / 100.0f;           // the detector efficiency EFF
     result[1] = FPCORR / ABS / 100.0f;  // the flight path correction FPCORR

     return result;
    }

/* -------------------------------------------------------------------------
 *
 *  main program for test purposes only
 *
 */
  public static void main( String[] args )
  {
    float final_speed;
    float result[];
    for ( int i = 0; i < 50; i++ )
    {
      final_speed = 200 + i * 200;
      final_speed /= 1000000;
      System.out.print("speed(m/us) = " + final_speed + "  " );
      System.out.print("E(meV) = " + 
                        tof_calc.EnergyFromVelocity(final_speed));
      result = getEfficiencyFactor( final_speed, 1 );
      System.out.println(" EFF = " + result[0] + "  FPCORR = " + result[1] );
    }
  }

}
