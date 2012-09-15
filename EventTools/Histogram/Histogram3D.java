/* 
 * File: Histogram3D.java
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

package EventTools.Histogram;

import java.util.*;

import EventTools.EventList.IEventList3D;
import EventTools.Histogram.Operators.BinEvents;
import EventTools.Histogram.Operators.ClearPages;
import EventTools.Histogram.Operators.ScanHistogram3D;
import EventTools.Histogram.Operators.GetEventLists;

import gov.anl.ipns.MathTools.Geometry.Vector3D;

import gov.anl.ipns.Operator.Threads.*;

/**
 *   This class represents a 3D histogram.  Currently only a minimal set of
 * operations is provided, but some of those operations have been tuned and
 * run in multiple threads to take advantage of multi-core processors.
 * In most cases, up to four threads on a can be used with reasonable
 * efficiency on a system with four or more cores.  Use of more than four
 * threads will probably not be very helpful.    
 *   The bins of the 3D histogram are determined by three ProjectionBinner3D
 * objects. Each binner object has a direction vector, and the bin number 
 * (i.e. index) is determined by the dot product of the event with that 
 * direction vector.  The first, "X" binner maps the 3D event to a column
 * number of the underlying 3D array.  The second "Y" binner maps the 3D 
 * event to a row number, and the third "Z" binner, maps to a page number.
 * In simple cases, the vector for the "X" binner could be (1,0,0); the
 * vector for the "Y" binner could be (0,1,0); the vector for the "Z" binner 
 * could be (0,0,1)   
 *   The histogram also records basic information such as the min and max
 * value, and sum of all values in the histogram.
 */
public class Histogram3D
{
  private float  max;                        // max bin value (weighted)
  private float  min;                        // min bin value (weighted)
  private double sum;                        // sum of weighted events added
  private long   num_added;                  // total number of events added
                                             // to histogram (NOT weighted)

                                             // These determine the edges of
                                             // the parallelepipeds that are
                                             // the histogram bins
  private IProjectionBinner3D x_edge_binner,
                              y_edge_binner,
                              z_edge_binner;
                                             // These are normal to the planes 
                                             // that form the histogram bin
                                             // boundaries.

  private int cur_num_x = -1;                // keep track of current array
  private int cur_num_y = -1;                // sizes, so we don't spend
  private int cur_num_z = -1;                // time allocating a new array
                                             // if we already have one.

  private IProjectionBinner3D x_binner,
                              y_binner,
                              z_binner;

  private float[][][] histogram;
  private int         n_threads  = 4;
  private int         n_segments = 4;
  private int         max_time   = 6000000;

  private int[] page_1 = new int[n_segments];
  private int[] page_2 = new int[n_segments];


  /**
   * Construct a Histogram3D object covering a parallelepiped region 
   * of 3-dimensional real space.  The shape and number of subdivision 
   * of the region covered is determined by the direction vectors and
   * number of steps of the specified projection binners.  The specified
   * projection binners determine the edges of the histogram bins
   * (parallelepipeds).  Another set of three projection binners that
   * are perpendicular to the histogram bin faces is calculated and used
   * to place points in the correct histogram bin.
   * 
   * @param x_edge_binner  IProjectionBinner3D that determines the edges of 
   *                       the histogram bins (parallelepipeds) in the 
   *                       "x direction".
   * @param y_edge_binner  IProjectionBinner3D that determines the edges of 
   *                       the histogram bins (parallelepipeds) in the 
   *                       "y direction".
   * @param z_edge_binner  IProjectionBinner3D that determines the edges of 
   *                       the histogram bins (parallelepipeds) in the 
   *                       "z direction".
   */
  public Histogram3D( IProjectionBinner3D x_edge_binner, 
                      IProjectionBinner3D y_edge_binner,
                      IProjectionBinner3D z_edge_binner )
  {
    this.x_edge_binner = x_edge_binner;
    this.y_edge_binner = y_edge_binner;
    this.z_edge_binner = z_edge_binner;

    init_histogram();
  }


  /**
   *  Calculate the dual binners and make sure we have an cleared histogram
   *  of the correct size.
   */
  private void init_histogram()
  {
    IProjectionBinner3D[] dual_binners =
                         ProjectionBinner3D.getDualBinners( x_edge_binner, 
                                                            y_edge_binner, 
                                                            z_edge_binner );
    this.x_binner = dual_binners[0];
    this.y_binner = dual_binners[1];
    this.z_binner = dual_binners[2];

    int num_x = x_binner.numBins();
    int num_y = y_binner.numBins();
    int num_z = z_binner.numBins();
    if ( cur_num_x == num_x  &&
         cur_num_y == num_y  &&
         cur_num_z == num_z  )
    {
      clear();                                   // just clear current array
    } 
    else
    {                                            // get one the right size
      histogram = new float[num_z][num_y][];

      for ( int iz = 0; iz < num_z; iz++ )       // it's about 30% faster to
        for ( int iy = 0; iy < num_y; iy++ )     // allocate the rows ourselves
          histogram[iz][iy] = new float[num_x];  // than to just declare one 
                                                 // large 3D array on opteron
                                                 // system with 4 cores!

      cur_num_x = num_x;                         // record sizes for next time
      cur_num_y = num_y;
      cur_num_z = num_z;
    }

    max = Float.NEGATIVE_INFINITY;
    min = Float.POSITIVE_INFINITY;
    sum = 0;
  }


  /**
   * Get a reference to the ProjectionBinner3D that was passed into the
   * constructor to determine the edges of the histogram bins in the 
   * local "X" direction.
   *
   * @return A reference to the binner in the direction of the local "X"
   *         axis for this histogram.
   */
  public IProjectionBinner3D xEdgeBinner()
  {
    return x_edge_binner;
  }


  /**
   * Get a reference to the ProjectionBinner3D that was passed into the
   * constructor to determine the edges of the histogram bins in the 
   * local "Y" direction.
   *
   * @return A reference to the binner in the direction of the local "Y"
   *         axis for this histogram.
   */
  public IProjectionBinner3D yEdgeBinner()
  {
    return y_edge_binner;
  }


  /**
   * Get a reference to the ProjectionBinner3D that was passed into the
   * constructor to determine the edges of the histogram bins in the 
   * local "Z" direction.
   *
   * @return A reference to the binner in the direction of the local "Z"
   *         axis for this histogram.
   */
  public IProjectionBinner3D zEdgeBinner()
  {
    return z_edge_binner;
  }


  /**
   * Get a reference to the ProjectionBinner3D used to determine in which
   * bin in the local "X" direction a 3D point should be placed.  The
   * direction of this binner is perpendicular to the local "YZ" plane.  If
   * the original edge binners are orthogonal, then the binner returned
   * by this method  will be the same as the binner returned by the
   * xEdgeBinner() method.
   *
   * @return A reference to the binner used to categorize points in 
   *         the direction of the local "X" axis for this histogram.
   */
  public IProjectionBinner3D xBinner()
  {
    return x_binner;
  }


  /**
   * Get a reference to the ProjectionBinner3D used to determine in which
   * bin in the local "Y" direction a 3D point should be placed.  The
   * direction of this binner is perpendicular to the local "XZ" plane.  If
   * the original edge binners are orthogonal, then the binner returned
   * by this method  will be the same as the binner returned by the
   * yEdgeBinner() method.
   *
   * @return A reference to the binner used to categorize points in 
   *         the direction of the local "X" axis for this histogram.
   */
  public IProjectionBinner3D yBinner()
  {
    return y_binner;
  }


  /**
   * Get a reference to the ProjectionBinner3D used to determine in which
   * bin in the local "Z" direction a 3D point should be placed.  The
   * direction of this binner is perpendicular to the local "XY" plane.  If
   * the original edge binners are orthogonal, then the binner returned
   * by this method  will be the same as the binner returned by the
   * zEdgeBinner() method.
   *
   * @return A reference to the binner used to categorize points in 
   *         the direction of the local "X" axis for this histogram.
   */
  public IProjectionBinner3D zBinner()
  {
    return z_binner;
  }

 
  /**
   * Get the maximum value of any bin in the histogram.
   * @return  The max bin value.
   */
  public double maxVal()
  {
    return max;
  }

  
  /**
   * Get the minimum value of any bin in the histogram.
   * @return  The min bin value.
   */
  public double minVal()
  {
    return min;
  }

  
  /**
   * Get the total of all bins in the histogram.
   * @return  The total value.
   */
  public double total()
  {
    return sum;
  }


  /**
   * Get the number of distinct events that were added to the histogram,
   * NOT counting the weight of the events.
   *
   * @return  The total number of distinct events added to the histogram.
   */
  public long numAdded()
  {
    return num_added;
  }


  /**
   * Change the Histogram3D position to cover a new parallelepiped region 
   * of 3-dimensional real space.  The shape and number of subdivision 
   * of the region covered is determined by the direction vectors and
   * number of steps of the specified projection binners.  The specified
   * projection binners determine the edges of the histogram bins
   * (parallelepipeds).  Another set of three projection binners that
   * are perpendicular to the histogram bin faces is calculated and used
   * to place points in the correct histogram bin.
   * 
   * @param x_edge_binner  IProjectionBinner3D that determines the edges of 
   *                       the histogram bins (parallelepipeds) in the 
   *                       "x direction".
   * @param y_edge_binner  IProjectionBinner3D that determines the edges of 
   *                       the histogram bins (parallelepipeds) in the 
   *                       "y direction".
   * @param z_edge_binner  IProjectionBinner3D that determines the edges of 
   *                       the histogram bins (parallelepipeds) in the 
   *                       "z direction".
   */
  public void setHistogramPosition( IProjectionBinner3D x_edge_binner,
                                    IProjectionBinner3D y_edge_binner,
                                    IProjectionBinner3D z_edge_binner )
  {
    synchronized(histogram)
    {
      this.x_edge_binner = x_edge_binner;
      this.y_edge_binner = y_edge_binner;
      this.z_edge_binner = z_edge_binner;

      init_histogram();
    }
  }

  
  /**
   *  Set all bins of the histogram to 0.
   */
  public void clear()
  {
    synchronized(histogram)
    {
      SplitPages();

      Vector  ops = new Vector();
      for ( int i = 0; i < n_segments; i++ )
        ops.add( new ClearPages( histogram, page_1[i], page_2[i] ) );

      ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
      pe.runOperators();
      num_added = 0;
    }
    min = 0;
    max = 0;
    sum = 0;
  }  


  /**
   * Add all events from the specified IEventList3D to corresponding
   * bins in this histogram.  Note: first creating an empty histogram
   * then adding events to it will allow for stepping through a huge 
   * list of events one portion at a time.  If the event list is too 
   * large to conveniently fit in memory, then parts of the event list
   * can be loaded and passed to addEvents(), sequentially.
   * 
   * @param events       The list of events to be added to this histogram.
   * @param use_weights  Flag indicating wheter to add the event weight to
   *                     the histogram bin, or to just add one to the 
   *                     histogram bin, for each event.
   *
   * @return  The total weighted event count that was added.  
   */
  public double addEvents( IEventList3D events, boolean use_weights )
  {
    if ( events == null || events.numEntries() == 0 )    // empty list
      return 0;                                          // so just return 0

    synchronized(histogram)
    {
      SplitPages();

      Vector  ops = new Vector();
      for ( int i = 0; i < n_segments; i++ )
        ops.add( new BinEvents( histogram, 
                                use_weights,
                                min, max, 
                                page_1[i], page_2[i], 
                                x_binner, y_binner, z_binner,  
                                events ) );

      ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
      Vector results = pe.runOperators();

      double old_sum = sum;
      extract_scan_info( results, old_sum );

      num_added += events.numEntries();
      return sum - old_sum;
    }
  }


  /** Get the value recorded at the histogram bin corresponding to the 
   *  point (x,y,z).  If the position is outside of the region covered 
   *  by the histogram, zero is returned.
   *
   *  @param  x   The x-coordinate of the point
   *  @param  y   The y-coordinate of the point
   *  @param  z   The z-coordinate of the point
   *
   *  @return The value recorded in the histogram bin containing (x,y,z), if
   *          the point (x,y,z) is in the region covered by the histogram,
   *          or zero, if (x,y,z) is outside the region. 
   */
  public float valueAt( float x, float y, float z )
  {
    int z_index = z_binner.index(x,y,z);
    if ( z_index < 0 || z_index >= histogram.length )
      return 0;

    int y_index = y_binner.index(x,y,z);
    if ( y_index < 0 || y_index >= histogram[0].length )
      return 0;

    int x_index = x_binner.index(x,y,z);
    if ( x_index < 0 || x_index >= histogram[0][0].length )
      return 0;

    return histogram[z_index][y_index][x_index];
  }


  /**
   *  Find the total of the histogram bins at the specified x,y,z location
   *  plus or minus the specified number of bins in each direction.
   *
   *  @param  x      The x-coordinate of the center bin 
   *  @param  y      The y-coordinate of the center bin 
   *  @param  z      The z-coordinate of the center bin
   *  @param  n_bins The number of bins on each side of the center bin that
   *                 should be summed.  
   *
   *  @return The sum of the values in the histogram bins, around and 
   *          including the bin containing (x,y,z).  If the full set of bins
   *          is NOT included in the histogram, this functon will return 0.
   */
  public float totalNear( float x, float y, float z, int n_bins )
  {
    int z_index = z_binner.index(x,y,z);
    if ( z_index < n_bins || z_index >= histogram.length - n_bins )
      return 0;

    int y_index = y_binner.index(x,y,z);
    if ( y_index < n_bins || y_index >= histogram[0].length - n_bins )
      return 0;

    int x_index = x_binner.index(x,y,z);
    if ( x_index < n_bins || x_index >= histogram[0][0].length - n_bins )
      return 0;

    float sum = 0;
    for ( int x_i = x_index-n_bins; x_i <= x_index+n_bins; x_i++ )
      for ( int y_i = y_index-n_bins; y_i <= y_index+n_bins; y_i++ )
        for ( int z_i = z_index-n_bins; z_i <= z_index+n_bins; z_i++ )
          sum += histogram[z_i][y_i][x_i];

    return sum;
  }


  /** Check whether or not the specified point (x,y,z) is in the region covered
   *  by the histogram.
   *
   *  @param  x   The x-coordinate of the point
   *  @param  y   The y-coordinate of the point
   *  @param  z   The z-coordinate of the point
   *
   *  @return true if the point is in the region covered by the histogram,
   *          and false otherwise.
   */
  public boolean isPointIn( float x, float y, float z )
  {
    int z_index = z_binner.index(x,y,z);
    if ( z_index < 0 || z_index >= histogram.length )
      return false;

    int y_index = y_binner.index(x,y,z);
    if ( y_index < 0 || y_index >= histogram[0].length )
      return false;

    int x_index = x_binner.index(x,y,z);
    if ( x_index < 0 || x_index >= histogram[0][0].length )
      return false;

    return true;
  }


  /**
   * Get the location in 3D space of the bin with the specified indexes.
   * The bin indexes determine what multiples of the histograms edge vectors
   * are combined to find the bin center.
   *
   * @param  x_index    The index of the requested bin, in the histograms
   *                    local x direction.
   * @param  y_index    The index of the requested bin, in the histograms
   *                    local y direction.
   * @param  z_index    The index of the requested bin, in the histograms
   *                    local z direction.
   *
   * @return  A Vector3D object containing the position of the center of the
   *          requested bin.
   */
  public Vector3D binLocation( int x_index, int y_index, int z_index )
  {
    float[] bin_center_coords = new float[3];

    ProjectionBinner3D.centerPoint( x_index, y_index, z_index,
                                    x_edge_binner, y_edge_binner, z_edge_binner,
                                    bin_center_coords );
    Vector3D bin_center = new Vector3D( bin_center_coords );

    return bin_center;
  }


  /**
   * Get a new Histogram3D with values in a neighborhood of the specified 
   * point.  The histogram size is set to include centers of bins in
   * a sphere of the specified radius around the specified point,
   * BUT will be restricted to lie within the bounds of this histogram,
   * and to include bins that are not further away from the center bin by
   * more than the specified offsets.
   * NOTE: In general, the returned histogram will not have equal sizes in
   *       all dimensions. Also, in general the new histogram will correspond
   *       to a parallelopiped in 3D space, since the binner directions may
   *       not be mutually perpendicular.
   * NOTE: The number of events added for this smaller histogram can not
   *       be determined, so it is just set to the sum of the values in
   *       the new histogram
   *
   *  @param x            The x coodinate of the center of the region
   *  @param y            The y coodinate of the center of the region
   *  @param z            The z coodinate of the center of the region
   *  @param radius       The radius of a sphere that determines the size
   *                      of the region that is returned.
   *  @param max_x_offset The maximum allowed number of bins the index in
   *                      the x direction can differ from the center index.
   *                      Pass in -1 if the x-offset should not be resticted.
   *  @param max_y_offset The maximum allowed number of bins the index in
   *                      the y direction can differ from the center index.
   *                      Pass in -1 if the y-offset should not be resticted.
   *  @param max_z_offset The maximum allowed number of bins the index in
   *                      the z direction can differ from the center index.
   *                      Pass in -1 if the y-offset should not be resticted.
   *
   *  @return a new Histogram3D containing a portion of the original
   *          histogram.
   */
  public Histogram3D getSubHistogram( float x, 
                                      float y, 
                                      float z, 
                                      float radius,
                                      int   max_x_offset,
                                      int   max_y_offset,
                                      int   max_z_offset )
  {
    int[][] ranges = getIndexRanges( x, y, z, 
                                     radius, 
                                     max_x_offset, 
                                     max_y_offset,
                                     max_z_offset );
    int min_x_index = ranges[0][0];
    int max_x_index = ranges[0][1];

    int min_y_index = ranges[1][0];
    int max_y_index = ranges[1][1];

    int min_z_index = ranges[2][0];
    int max_z_index = ranges[2][1];

    IProjectionBinner3D new_x_binner = (IProjectionBinner3D)
                        x_edge_binner.getSubBinner( min_x_index, max_x_index );

    IProjectionBinner3D new_y_binner = (IProjectionBinner3D)
                        y_edge_binner.getSubBinner( min_y_index, max_y_index );

    IProjectionBinner3D new_z_binner = (IProjectionBinner3D)
                        z_edge_binner.getSubBinner( min_z_index, max_z_index );

    Histogram3D new_histogram = new Histogram3D( new_x_binner,
                                                 new_y_binner,
                                                 new_z_binner );
    int page,
        row,
        col;

    float value;

    new_histogram.max = Float.NEGATIVE_INFINITY;
    new_histogram.min = Float.POSITIVE_INFINITY;

    for ( int z_index = min_z_index; z_index <= max_z_index; z_index++ )
      for ( int y_index = min_y_index; y_index <= max_y_index; y_index++ )
        for ( int x_index = min_x_index; x_index <= max_x_index; x_index++ )
        {
          page = z_index - min_z_index;
          row  = y_index - min_y_index;
          col  = x_index - min_x_index;

          value = histogram[z_index][y_index][x_index];

          new_histogram.histogram[page][row][col] = value; 

          if ( new_histogram.min > value )
            new_histogram.min = value;

          if ( new_histogram.max < value )
            new_histogram.max = value;

          new_histogram.sum += value;
        }

    new_histogram.num_added = (long)new_histogram.sum;

    return new_histogram;
  }


  /**
   * Find the centroid of a spherical region with the specified radius
   * around the specified point.  Currently, the coordinates of the
   * centroid will be the coordinates of the center of the bin that
   * contains the centroid.
   *
   *  @param x      The x coodinate of the center of the sphere 
   *  @param y      The y coodinate of the center of the sphere
   *  @param z      The z coodinate of the center of the sphere 
   *  @param radius Radius of the sphere
   *
   *  @return a Vector3D giving the location (real coordinates) of the 
   *          centroid of the sphere.  If all counts are zero, or if 
   *          the specified point is not in the histogram, 
   *          this will return null. 
   */
  public Vector3D centroid( float x, float y, float z, float radius )
  {
    int[][] ranges = getIndexRanges( x, y, z, radius, -1, -1, -1 );
    if ( ranges == null )
      return null;

    int min_x_index = ranges[0][0];
    int max_x_index = ranges[0][1];

    int min_y_index = ranges[1][0];
    int max_y_index = ranges[1][1];

    int min_z_index = ranges[2][0];
    int max_z_index = ranges[2][1];

    float  counts = 0;
    float  total  = 0;
    float  x_sum  = 0;
    float  y_sum  = 0;
    float  z_sum  = 0;
    float  distance;

    Vector3D diff_vec   = new Vector3D();
    Vector3D center_vec = new Vector3D( x, y, z );

    float[] bin_center_coords = new float[3];

    for ( int x_index = min_x_index; x_index <= max_x_index; x_index++ )
      for ( int y_index = min_y_index; y_index <= max_y_index; y_index++ )
        for ( int z_index = min_z_index; z_index <= max_z_index; z_index++ )
        {
          ProjectionBinner3D.centerPoint( x_index, y_index, z_index,
                                          x_edge_binner,
                                          y_edge_binner,
                                          z_edge_binner,
                                          bin_center_coords );
          diff_vec.set( bin_center_coords );
          diff_vec.subtract( center_vec );
          distance = diff_vec.length();

          if ( distance < radius )
          {
            counts = histogram[z_index][y_index][x_index];
            total += counts;
            x_sum += x_index * counts;
            y_sum += y_index * counts;
            z_sum += z_index * counts;
          }
        }

    if ( total == 0 )
      return null;

    float xmax_index = x_sum/total;
    float ymax_index = y_sum/total;
    float zmax_index = z_sum/total;

    Vector3D vec = x_binner.Vec( xmax_index );
    vec.add( y_binner.Vec( ymax_index ) );
    vec.add( z_binner.Vec( zmax_index ) );
    return vec;
  }



  /**
   * Find the point in a cubical region for which the projections of the data 
   * on the x, y and z axes are the largest.  The coordinates returned are
   * the coordinates of the center of the bin contaning the point with maximum
   * projected counts.
   *
   *  @param x      The x coodinate of the center of the region
   *  @param y      The y coodinate of the center of the region
   *  @param z      The z coodinate of the center of the region
   *  @param radius Size of the cubical region that will be checked, expressed
   *                as the radius of an inscribed spehere 
   *
   *  @return a Vector3D giving the location (real coordinates) of the point 
   *          in the region for which the projections have the maximum counts.  
   *          If several points have the maximum counts, then the point which
   *          is closest to the specified x, y, z location is returned.  If
   *          all counts are zero, or if the specified point is not in the
   *          histogram, this will return null. 
   */
  public Vector3D maxPoint( float x, float y, float z, float radius )
  {
    int[][] ranges = getIndexRanges( x, y, z, radius, -1, -1, -1 );
    if ( ranges == null )
      return null;

    int min_x_index = ranges[0][0];
    int max_x_index = ranges[0][1];

    int min_y_index = ranges[1][0];
    int max_y_index = ranges[1][1];

    int min_z_index = ranges[2][0];
    int max_z_index = ranges[2][1];

    float[] x_proj = new float[ max_x_index - min_x_index + 1 ];
    float[] y_proj = new float[ max_y_index - min_y_index + 1 ];
    float[] z_proj = new float[ max_z_index - min_z_index + 1 ];

    float count;
    for ( int x_index = min_x_index; x_index <= max_x_index; x_index++ )
      for ( int y_index = min_y_index; y_index <= max_y_index; y_index++ )
        for ( int z_index = min_z_index; z_index <= max_z_index; z_index++ )
        {
          count = histogram[z_index][y_index][x_index];
          x_proj[x_index - min_x_index] += count;
          y_proj[y_index - min_y_index] += count;
          z_proj[z_index - min_z_index] += count;
        }

    int center_x_index = x_binner.index(x,y,z) - min_x_index;
    int center_y_index = y_binner.index(x,y,z) - min_y_index;
    int center_z_index = z_binner.index(x,y,z) - min_z_index;

    int xmax_index = 0;
    int ymax_index = 0;
    int zmax_index = 0;

    float xmax_count = x_proj[0];
    int   distance   = center_x_index;
    for ( int i = 1; i < x_proj.length; i++ )
    {
      count = x_proj[i];
      if (   count  > xmax_count || 
           ( count == xmax_count && (Math.abs(i-center_x_index) < distance) ) )
      {
        xmax_count  = count;
        distance   = Math.abs(i-center_x_index);
        xmax_index = i;
      } 
    }

    float ymax_count = y_proj[0];
    distance   = center_y_index;
    for ( int i = 1; i < y_proj.length; i++ )
    {
      count = y_proj[i];
      if (   count  > ymax_count || 
           ( count == ymax_count && (Math.abs(i-center_y_index) < distance) ) )
      {
        ymax_count  = count;
        distance   = Math.abs(i-center_y_index);
        ymax_index = i;
      }
    }

    float zmax_count = z_proj[0];
    distance   = center_z_index;
    for ( int i = 1; i < z_proj.length; i++ )
    {
      count = z_proj[i];
      if (   count  > zmax_count ||
           ( count == zmax_count && (Math.abs(i-center_z_index) < distance) ) )
      {
        zmax_count = count;
        distance   = Math.abs(i-center_z_index);
        zmax_index = i;
      }
    }

    int n_x = x_proj.length; 
    int n_y = y_proj.length; 
    int n_z = z_proj.length; 
    System.out.println( "nx, ny, nz = " + n_x + ", " + n_y + ", " + n_z + 
                        " min x, y, z = " + 
                         xmax_index + ", " + ymax_index + ", " + zmax_index +
                        " proj_mx, proj_mx, proj_mz = " +
                         xmax_count + ", " + ymax_count + ", " + zmax_count ); 

    for ( int i = 0; i < x_proj.length; i++ )
      System.out.printf("%5.1f ", x_proj[i] );
    System.out.println();

    for ( int i = 0; i < y_proj.length; i++ )
      System.out.printf("%5.1f ", y_proj[i] );
    System.out.println();

    for ( int i = 0; i < z_proj.length; i++ )
      System.out.printf("%5.1f ", z_proj[i] );
    System.out.println();

    xmax_index += min_x_index;
    ymax_index += min_y_index;
    zmax_index += min_z_index;

    float[] coords = new float[3];
    ProjectionBinner3D.centerPoint( xmax_index, ymax_index, zmax_index,
                                    x_edge_binner, y_edge_binner, z_edge_binner,
                                    coords );
    Vector3D result = new Vector3D( coords );
    return result;
  }


  /**
   *  Find the total counts that are enclosed within spheres with the 
   *  specified radii around the specified point.  
   *
   *  @param x      The x coodinate of the center of the spheres
   *  @param y      The y coodinate of the center of the spheres
   *  @param z      The z coodinate of the center of the spheres
   *  @param radii  Array containing the radii of the spheres
   *
   *  @return A Vector containing an array of floats giving counts in 
   *          a sphere and an array of floats giving the number of bins
   *          contributing to the counts.  The ith entry in the array of
   *          counts contains the total counts of the bins whose centers 
   *          are within the ith radius of the specified (x,y,z) point.  
   *          The ith entry in the second array contains the number of 
   *          bins whose centers are within the ith radius.
   *          NOTE: If the specified point is not in the histogram, this
   *                method will return null.
   */
  public Vector sphereIntegrals( float x, float y, float z, float[] radii )
  {
    float max_radius = 0;
    for ( int i = 0; i < radii.length; i++ )
      if ( max_radius < radii[ i ] )
        max_radius = radii[i];

    int[][] ranges = getIndexRanges( x, y, z, max_radius, -1, -1, -1 );
    if ( ranges == null )
      return null;

    int min_x_index = ranges[0][0];
    int max_x_index = ranges[0][1];

    int min_y_index = ranges[1][0];
    int max_y_index = ranges[1][1];

    int min_z_index = ranges[2][0];
    int max_z_index = ranges[2][1];

//  System.out.println("Value at = " + valueAt( x, y, z ) );
//  System.out.println("min/max x index = " + min_x_index + ", "+max_x_index);
//  System.out.println("min/max y index = " + min_y_index + ", "+max_y_index);
//  System.out.println("min/max z index = " + min_z_index + ", "+max_z_index);

    float[]  counts = new float[ radii.length ];
    float[]  n_bins = new float[ radii.length ];
    float    distance;
    Vector3D x_vec,
             y_vec,
             z_vec;
    Vector3D diff_vec = new Vector3D();
                                         // For each bin, find it's distance
                                         // from the center vec and add its
                                         // value to any sphere containing it.
                                         // NOTE: this will also work with
                                         //       skewed axes.
    Vector3D center_vec = new Vector3D( x, y, z );

    float[] bin_center_coords = new float[3];

    for ( int x_index = min_x_index; x_index <= max_x_index; x_index++ )
      for ( int y_index = min_y_index; y_index <= max_y_index; y_index++ )
        for ( int z_index = min_z_index; z_index <= max_z_index; z_index++ )
        {
          ProjectionBinner3D.centerPoint( x_index, y_index, z_index,
                                          x_edge_binner, 
                                          y_edge_binner,
                                          z_edge_binner,
                                          bin_center_coords );
          diff_vec.set( bin_center_coords );
          diff_vec.subtract( center_vec ); 
          distance = diff_vec.length();

          for ( int i = 0; i < radii.length; i++ )
            if ( distance < radii[i] )
            {
              counts[i] += histogram[z_index][y_index][x_index];
              n_bins[i] += 1;
            }
        }

     Vector result = new Vector(2);
     result.add( counts );
     result.add( n_bins );
     return result;
  }


  /**
   * Get ranges of x, y and z indices that cover a sphere of the specified
   * radius around the specified point BUT are restricted to lie within
   * the histogram, and restricted to be no more than the specifed number of
   * bins from the center bin, in the x, y and z directions.
   *
   * @param x            The x coordinate of the "center" point.
   * @param y            The y coordinate of the "center" point.
   * @param z            The z coordinate of the "center" point.
   * @param radius       The radius of the sphere that the region should cover.
   * @param max_x_offset The maximum allowed number of bins the index in
   *                     the x direction can differ from the center index.
   *                     Pass in -1 if the x-offset should not be resticted.
   * @param max_y_offset The maximum allowed number of bins the index in
   *                     the y direction can differ from the center index.
   *                     Pass in -1 if the y-offset should not be resticted.
   * @param max_z_offset The maximum allowed number of bins the index in
   *                     the z direction can differ from the center index.
   *                     Pass in -1 if the y-offset should not be resticted.
   *
   * @return A 2D array of ints.  The first row has the min and max index
   *         in the "x" direction, the second row has the min and max index
   *         in the "y" direction and the third row has the min and max index
   *         in the "z" direction.
   */
  private int[][] getIndexRanges( float x, 
                                  float y, 
                                  float z, 
                                  float radius,
                                  int   max_x_offset,
                                  int   max_y_offset,
                                  int   max_z_offset )
  {
    int center_z_index = z_binner.index(x,y,z);
    if ( center_z_index < 0 || center_z_index >= histogram.length )
    {
/*
      System.out.println("ERROR: center_z_index invalid : " + center_z_index );
      System.out.println("Point coordinates, x, y, z " +x+ ", " +y+ ", " +z );
      System.out.println("Histogram size, (x,y,z) : " +
                         histogram[0][0].length + ", " +
                         histogram[0].length + ", " +
                         histogram.length ); 
*/
      return null;
    }

    int center_y_index = y_binner.index(x,y,z);
    if ( center_y_index < 0 || center_y_index >= histogram[0].length )
    {
/*
      System.out.println("ERROR: center_y_index invalid : " + center_y_index );
      System.out.println("       center_z_index was :     " + center_z_index );
      System.out.println("Point coordinates, x, y, z " +x+ ", " +y+ ", " +z );
      System.out.println("Histogram size, (x,y,z) : " +
                         histogram[0][0].length + ", " +
                         histogram[0].length + ", " +
                         histogram.length ); 
*/
      return null;
    }

    int center_x_index = x_binner.index(x,y,z);
    if ( center_x_index < 0 || center_x_index >= histogram[0][0].length )
    {
/*
      System.out.println("ERROR: center_x_index invalid : " + center_x_index );
      System.out.println("       center_y_index was :     " + center_y_index );
      System.out.println("       center_z_index was :     " + center_z_index );
      System.out.println("Point coordinates, x, y, z " +x+ ", " +y+ ", " +z );
      System.out.println("Histogram size, (x,y,z) : " +
                         histogram[0][0].length + ", " +
                         histogram[0].length + ", " +
                         histogram.length ); 
*/
      return null;
    }

    Vector3D center_vec = new Vector3D( x, y, z );

    int[][] ranges = new int[3][];
    ranges[0] = getMinMaxIndex( x_binner, center_vec, radius, max_x_offset );
    ranges[1] = getMinMaxIndex( y_binner, center_vec, radius, max_y_offset );
    ranges[2] = getMinMaxIndex( z_binner, center_vec, radius, max_z_offset );

    return ranges;
  }

 
  /**
   *  Get the range of indexes required to cover a sphere of the specified
   *  radius in the direction of the specified binner, restricted to be
   *  no more than "max_offset" bins away from the center bin.
   *
   *  @param  binner      The binner object for the required axis.
   *  @param  center_vec  The position in 3D of the sphere center.
   *  @param  radius      The radius of the sphere that will be included.
   *  @param  max_offset  The maximum number of bins that the min and max
   *                      index is allowed to differ from the index of the
   *                      center of the sphere.  If no max_offset should
   *                      be set, pass in a negative value.
   *
   *  @return  An array containing the min and max index for bins along
   *           the binner direction that overlap the specified sphere.
   */
  private int[] getMinMaxIndex( IProjectionBinner3D binner, 
                                Vector3D            center_vec,
                                float               radius,
                                int                 max_offset )
  {
    int      n_bins = binner.numBins();
    Vector3D d_vec  = binner.directionVec();
    d_vec.multiply( radius );

    Vector3D temp = new Vector3D( center_vec );
    temp.add( d_vec );
    int index_1 = binner.index( temp.getX(), temp.getY(), temp.getZ() );
    if ( index_1 < 0 )
      index_1 = 0;
    if ( index_1 >= n_bins )
      index_1 = n_bins - 1;

    temp.set( center_vec );
    temp.subtract( d_vec );
    int index_2 = binner.index( temp.getX(), temp.getY(), temp.getZ() );
    if ( index_2 < 0 )
      index_2 = 0;
    if ( index_2 >= n_bins )
      index_2 = n_bins - 1;

    if ( max_offset > 0 )
    {
      int center_index = binner.index( center_vec.getX(), 
                                       center_vec.getY(), 
                                       center_vec.getZ() );
      index_1 = clampIndex( index_1, center_index, max_offset );
      index_2 = clampIndex( index_2, center_index, max_offset );
    }


    int[] range = {Math.min( index_1, index_2 ), Math.max( index_1, index_2 )};
    return range;
  }


  /**
   *  Clamp the specified index to be no more than the specified offset away
   *  from the specified center index.
   *
   *  @param  index        The index to adjust if needed.
   *  @param  center_index The index of the bin containing the center of 
   *                       the interval.
   *  @param  max_offset   The maximum allowed difference between the index
   *                       and the center index.
   *  @return a (possibly) adjusted index value that is on the same side 
   *          of the center index, but is no more than max_offset units away
   *          from the center index.
   */
  private int clampIndex( int index, 
                          int center_index, 
                          int max_offset )
  {
    int offset;
    if ( index < center_index )
    {
      offset = center_index - index;
      if ( offset > max_offset )            // index too far below center, so
        return center_index - max_offset;   // return clamped value
    }

    if ( index > center_index )
    {
      offset = index - center_index;
      if ( offset > max_offset )            // index too far above center, so
        return center_index + max_offset;   // return clamped value
    } 

    return index;                           // index OK, so return it
  }



  /**
   * Get event lists from bins of this histogram with values in intervals
   * determined by the specified IEventBinner.  Since the event lists are
   * obtained by multiple threads working on different sections of the 
   * underlying array, the event lists are returned in many small sections.
   * Specifically, a Vector of Vectors is returned, with one entry in the
   * top level Vector, for each segment of the array that was processed
   * separately.  The "inner" Vectors, corresponding to one segment of the
   * array, contains multiple IEventList3D objects, one for each bin of the
   * specified binner, and one for 3D histogram bins with values larger than
   * the maximum binner value.  Specifically, if the specified binner splits 
   * the interval [10,100) into 9 uniform bins, then 10 lists of events will 
   * be returned.  The first list of events will have x,y,z values at bin 
   * centers, for bins in the segment with counts in the interval [10,20).
   * The event codes will be 0 for these events. The second list of events 
   * will have x,y,z values at bin centers, for bins with counts in the 
   * interval [20,30), etc.  The event codes will be 1 for these events.
   * Finally, 10th list will have the x,y,z values at bin centers, for bins
   * with counts greater than 100.  If there were no bins with counts in a 
   * particular interval, in a particular segment of the array, then that 
   * IEventList3D will be NULL. 
   *   For example, if the binner had 9 bins and the array was split 
   * into 4 segments, then there could be as many as 40 IEventList3D 
   * objects.  The top level vector would have 4 entries, one for each 
   * segment that was processed.  Each of these 4 entries would be a
   * Vector with 10 entries.  Each of those 10 entries would either be 
   * an IEventList3D, or null.  
   *   
   * @param binner  The IEventBinner specifying the histogram levels
   *                of bins that should be returned.
   *                
   * @return  a Vector of Vectors containing IEventList3D object.
   */
  public Vector getEventLists( IEventBinner binner )
  {
    synchronized(histogram)
    {
      SplitPages();

      Vector  ops = new Vector();
      for ( int i = 0; i < n_segments; i++ )
        ops.add( new GetEventLists( histogram, 
                                    page_1[i], page_2[i],
                                    x_edge_binner,
                                    y_edge_binner,
                                    z_edge_binner,
                                    binner ) );

      ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
      Vector results = pe.runOperators();
      return results;
    }
  }


  /** 
   *  Get a reference to the underlying 3D array for this histogram.
   *  This violates encapsulation, but since the arrays can be very large
   *  it is sometimes necessary to do this for efficiency.
   *
   *  NOTE: This must be used carefully.  In particular, the size of the
   *  array MUST NOT BE CHANGED, and the values in the array should not
   *  be changed!
   *
   *  @return a reference to the 3D array maintained by this histogram.
   */
  public float[][][] getHistogramArray()
  {
    return histogram;
  }


  /**
   * Get a reference to one page of data (ie. values at constant "z") 
   * from this 3D histogram.  NOTE: Since this returns a reference to the 
   * internal histogram array, code calling this method must NOT modify the 
   * array.  Columns and rows in the slice correspond to columns and rows,
   * respectively, in the original array, as if an observer was looking
   * in the -z direction, with y "up".
   *
   * @param page The page number in the array for the slice that will 
   *             be returned.  This must be at least 0 but less than
   *             the number of pages (number of bins in the z-binner).  
   *
   * @return A reference to one page of this 3D histogram
   */
  public float[][] pageSlice( int page )
  {
    return histogram[page];
  }
  

  /**
   * Get a copy of a 2D slice at constant "y" from this histogram
   * consisting of all bins with the same row number.   Since this
   * method copies the data, it is much slower than the pageSlice()
   * method.  Columns and rows in the slice correspond to pages and
   * columns repectively, in the original array, as if an observer was
   * looking -y direction, with x "up"
   *
   * @param row The row number in the array for the slice that will be
   *            returned.  This must be at least 0 but less than the
   *            number of rows (number of bins in the y-binner).
   *
   * @return A copy of the data from one row of this 3D histogram.
   */
  public float[][] rowSlice( int row )
  {
    int num_pages = z_binner.numBins();
    int num_cols  = x_binner.numBins();
    float[][] row_slice = new float[num_cols][num_pages];

    for ( int page = 0; page < num_pages; page++ )
     for ( int col = 0; col < num_cols; col++ )
       row_slice[col][page] = histogram[page][row][col];

    return row_slice;
  }


  /**
   * Get a copy of a 2D slice at constant "x" from this histogram
   * consisting of all bins with the same col number.   Since this
   * method copies the data, it is much slower than the pageSlice()
   * method.  Columns and rows in the slice correspond to row and pages 
   * respectively, in the original array, as if an observer was looking
   * in the -x direction with z "up".
   *
   * @param col The column number in the array for the slice that will be
   *            returned.  This must be at least 0 but less than the
   *            number of columns (number of bins in the x-binner).
   *
   * @return A copy of the data from one column of this 3D histogram.
   */
  public float[][] colSlice( int col )
  { 
    int num_rows  = y_binner.numBins();
    int num_pages = z_binner.numBins();
    float[][] col_slice = new float[num_pages][num_rows];

    for ( int page = 0; page < num_pages; page++ )
      for ( int row = 0; row < num_rows; row++ )
        col_slice[page][row] = histogram[page][row][col];

    return col_slice;
  }

  
  /**
   * Scan across ALL bins of this histogram and set the min, max and total
   * value information.  NOTE: This method should NOT generally have to be
   * used, since methods that change the histogram entries update the min,
   * max and total value information.
   */
  public void scanHistogram()
  {
    synchronized(histogram)
    {
      SplitPages();

      Vector  ops = new Vector();
      for ( int i = 0; i < n_segments; i++ )
        ops.add( new ScanHistogram3D( histogram, page_1[i], page_2[i] ) );

      ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
      Vector results = pe.runOperators();

      extract_scan_info( results, 0 );
    }
  }


  /**
   * Update the min, max and total value information from the result 
   * Vector returned from executing the BinEvents and ScanHistogram3D 
   * operators in multiple threads.
   *  
   * @param results  A vector containing three Doubles, a sum of events
   *                 and min and max bin values
   * @param old_sum  The total sum from the histogram, before executing
   *                 the scan or bin operators.  This should be passed in
   *                 as zero when ScanHistogram is used, to properly 
   *                 get the new total sum.  It should be passed in as
   *                 the old_sum when adding extra events to a non-zero
   *                 histogram using BinEvents.
   */
  private void extract_scan_info( Vector results, double old_sum )
  {
    double   new_sum = old_sum;
    Object   one_result;
    double[] answers = new double[3];
    for ( int i = 0; i < results.size(); i++ )
    {
      one_result = results.elementAt(i);
      if ( one_result instanceof Vector )
        if ( ((Vector)one_result).size() == 3 )
        {
          Vector result_v = (Vector)one_result;
          for ( int k = 0; k < 3; k++ )
            if ( result_v.elementAt(k) instanceof Double )
              answers[k] = (Double)result_v.elementAt(k);
            else
              throw new ExecFailException( FailState.NOT_DONE, result_v );

          new_sum += answers[0];

          if ( answers[1] < min )
            min = (float)answers[1];

          if ( answers[2] > max )
            max = (float)answers[2];
        }
        else
          throw new ExecFailException( FailState.NOT_DONE, null );
    }

    sum = new_sum;
  }

  
  /**
   * Calculate how to split the array into roughly equal sized segments
   * based on the number of segments requested and the number of pages
   * in the histogram.  NOTE: the number of segments is forced to be 
   * less than or equal to the number of pages.
   */
  private void SplitPages()
  {
    if ( n_segments > histogram.length )
      n_segments = histogram.length;
    
    page_1 = new int[n_segments];
    page_2 = new int[n_segments];

    int   step = histogram.length / n_segments;
    for ( int i = 0; i < n_segments; i++ )
    {
      page_1[i] = i*step;
      page_2[i] = (i+1) * step - 1;
    }
    page_2[n_segments-1] = histogram.length - 1;
  }

}
