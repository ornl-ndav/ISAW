package OverplotView.util;

/**
 * $Id$
 *
 * allows easy conversion from float[] to double[] and vice versa
 *
 * $Log$
 * Revision 1.1  2000/07/06 16:17:44  neffk
 * Initial revision
 *
 */

public class NumberTypeConverter
{

  public NumberTypeConverter()
  {
  }


  public double[] toDouble( float[] f )
  {
    double[] d = new double[ f.length ];
    for( int i=0; i<f.length; i++ ) {
      d[i] = f[i];
//      System.out.println( d[i] );  //**dbg**
    }

    return d;
  }


  public float[] toFloat( double[] d )
  {
    float[] f = new float[ d.length ];
    for( int i=0; i<d.length; i++ ) {
      f[i] = (float)d[i];
//      System.out.println( f[i] );  //**dbg**
    }

    return f;
  }
}
    
