/*
 * @(#)VariableXScale.java     1.0  98/06/08  Dennis Mikkelson
 *
 */

package DataSetTools.dataset;
import java.io.*;

/**
 * The class for variable "X" scales that consist of a sequence of points
 * in an interval with arbitrary spacing between points.  The points must
 * be monotonic.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.XScale
 * @see DataSetTools.dataset.UniformXScale
 *
 * @version 1.0  
 */


public class VariableXScale extends XScale implements Serializable 
{
  private float x[] = { 0, 1 }; 

  /**
   * Constructs a VariableXScale object by specifying a list of x values.
   * The list of "X" values must be monotonic.
   */
  public VariableXScale( float x[] )
  {
    super( x[0], x[x.length-1], x.length );

    int num_x = x.length;
    if ( num_x > 1 )
    {
      boolean x_increasing = true;
      boolean x_decreasing = true;
      for ( int i = 0; i < num_x - 1; i++ )
      {
        if ( x[i] >= x[i+1] )
          x_increasing = false;
        if ( x[i] <= x[i+1] )
          x_decreasing = false;
      }

      if ( !x_increasing && !x_decreasing )
        System.out.println("ERROR: x_values not monotonic in " +
                           "VariableXScale constructor" );
      else  
      {
        this.x = null;                  // valid X scale, so make a copy of it
        this.x = new float[ x.length ];
        System.arraycopy( x, 0, this.x, 0, x.length ); 
      } 
    }  
  }

  /**
   * Returns the array of "X" values specified when the constructor was called.
   */
  public float[] getXs()
  {
    float copy[] = new float[ x.length ];
    System.arraycopy( x, 0, copy, 0, x.length );
    return( copy );
  }

  /**
   * Creates a new VariableXScale object with the same data as the original
   * VariableXScale object.
   */
  public Object clone()
  {
    VariableXScale copy = new VariableXScale( x );
    return copy;
  }
}
