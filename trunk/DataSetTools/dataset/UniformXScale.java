/*
 * @(#)UniformXScale.java     1.0  98/06/08  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.3  2000/07/10 22:24:06  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.3  2000/05/12 15:41:35  dennis
 *  Made UniformXScales IMMUTABLE so that they may be shared.  This required
 *  two changes:
 *  1. Removed method:  setNum_x()
 *  2. Modified method:  expand()  so that it creates and returns a new
 *     UniformXScale rather than altering the contents of the current one.
 *
 *  Revision 1.2  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *
 */

package DataSetTools.dataset;
import java.io.*;

/**
 * The class for "X" scales that consist of evenly spaced points along a
 * a specified interval.  The evenly spaced points are specified by giving
 * the end points of the interval and the number of points to create.
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.XScale
 * @see DataSetTools.dataset.VariableXScale
 *
 * @version 1.0  
 */

public class UniformXScale extends XScale implements Serializable
{
  /**
   * Constructs a UniformXScale object by specifying the starting x, ending x 
   * and number of x values to be used.   For example, if the scale was to run 
   * from 0 to 1000 in steps of 10, the values 0, 1000, 101 would be passed 
   * to this constructor.  The number of x values must be at least 1.  If
   * the number of x values is greater than 1, then the start and end x values
   * must be unequal.  If the number of x values is equal to 1, then the
   * start and end x values should be the same.  If they are not, only the
   * start value will actually be used.
   *
   * @param   start_x  the starting x
   * @param   end_x    the ending x  
   * @param   num_x    the number of x values.  
   *
   * @see DataSetTools.dataset.XScale
   */
   public UniformXScale( float start_x, float end_x, int num_x )
   {
     super( start_x, end_x, num_x );
   }


  /**
   * Returns the array of "X" values.  The array will have num_x entries.   
   * The "X" values are uniformly spaced and are calculated from start_x, 
   * end_x and num_x.
   */
  public float[] getXs()
  {
    float step    = 0;
    float start_x = getStart_x();
    float end_x   = getEnd_x();
    int   num_x   = getNum_x();

    float x[]  = new float[num_x];
    if ( num_x > 1 )
      step = (end_x - start_x) / (num_x - 1);

    for ( int i = 0; i < num_x; i++ )
      x[i] = start_x + i * step;

    return x;
  }

  /**
   *  Constructs a new UniformXScale that extends over the smallest interval
   *  containing both this XScale and the specifed XScale.  The number of 
   *  x-values to use is extended to the larger of the numbers of x-values 
   *  in this XScale and the specified XScale. 
   */
   public UniformXScale expand( XScale scale )
   {
     float temp_start_x = Math.min( this.start_x, scale.start_x );
     float temp_end_x   = Math.max( this.end_x,   scale.end_x );
     int   temp_num_x   = Math.max( this.num_x,   scale.num_x );

     return new UniformXScale( temp_start_x, temp_end_x, temp_num_x ); 
   }

  /**
   * Creates a new UniformXScale object with the same data as the original
   * UniformXScale object.
   */
  public Object clone()
  {
    UniformXScale copy = new UniformXScale( getStart_x(), 
                                            getEnd_x(),
                                            getNum_x()  );
    return( copy );
  }
}
