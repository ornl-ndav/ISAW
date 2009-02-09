/**
 * File:  ElapsedTime.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-08-21 15:34:58 -0500 (Thu, 21 Aug 2008) $            
 *  $Revision: 306 $
 *
 *  $Log: ElapsedTime.java,v $
 *
 *  2008/08/21  Updated to latest version from UW-Stout repository.
 *
 *  Revision 1.4  2007/10/29 03:00:16  dennis
 *  Added methods to pause and resume the timer, and check whether or
 *  not the timer is currently paused.
 *
 *  Revision 1.3  2006/09/05 01:43:33  dennis
 *  Updated to use System.nanoTime() instead of System.currentTimMillis,
 *  to get more accurate timing for short time intervals.
 *
 *  Revision 1.2  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.1  2004/10/25 21:45:30  dennis
 *  Added to SSG_Tools CVS repository
 *
 */ 

package SSG_Tools.Utils;

/**
 *  This timer object is intended to be used for performance testing and
 *  animation control.  The timer can be reset or paused.
 */

public class ElapsedTime
{
  long      base_time;
  long      paused_time;
  boolean   paused = false;
  

  /**
   *  Construct an ElapsedTime object, and start measuring elapsed time from
   *  the time it was constructed.
   */
  public ElapsedTime()
  {
    reset();
  }

  /**
   *  Get the elapsed time since this timer was constructed, or was last
   *  reset.  Time during which the timer is paused does NOT add to the
   *  total elapsed time.
   *
   *  @return   The elapsed time in seconds.
   */
  public float elapsed()
  {
    if ( paused )
      return (float)((paused_time - base_time ) / 1.0e9);
    
    return (float)(( System.nanoTime() - base_time ) / 1.0e9);
  }

  /**
   * Pause the elapsed time.  This has no effect if called when the timer
   * is already paused.
   */
  public void pause()
  {
    if ( !paused )     // If not already paused, record the current time so
    {                  // the base time can be adjusted when counting resumes. 
      paused_time = System.nanoTime();
      paused = true;
    }
  }
  
  /**
   * Resume counting the elapsed time.  This has no effect if called when
   * the timer is still counting and has not been paused.
   */
  public void resume()
  {
    if ( paused )      // Advance the base time by the length of time we've 
    {                  // been paused, then trip paused to false.
      base_time += System.nanoTime() - paused_time;
      paused     = false;
    }
  }
  
  
  /**
   * Check whether or not this timer is paused.
   * 
   *  @return true if this timer is currently paused and false if this timer
   *          is currently running.
   */
  public boolean isPaused()
  {
    return paused;
  }
  
  
  /**
   *  Reset the elapsed time to zero, and set the paused flag to false.
   *  The timer will immediately start running. 
   */
  public void reset()
  {
    base_time = System.nanoTime();
    paused    = false;
  }

  /*
   *   Main program for testing purposes only
   */
  public static void main( String args[] )
  {
    float        time;
    ElapsedTime  timer = new ElapsedTime();

    timer.reset();
    time = timer.elapsed();
    System.out.println("Initially, elapsed time = " + time );

    timer.reset();
    double x = 0;
    for ( int i = 0; i < 10000000; i++ )
      x = Math.cos(x);

    time = timer.elapsed();
    System.out.println("After ten million double precision cosines, " +
                       "elapsed time = " + time );

    timer.reset();
    time = timer.elapsed();
    System.out.println("After reset, elapsed time = " + time );

  }
}
