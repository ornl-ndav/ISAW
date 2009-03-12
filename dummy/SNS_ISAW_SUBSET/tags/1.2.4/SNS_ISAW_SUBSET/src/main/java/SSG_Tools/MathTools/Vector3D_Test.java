package SSG_Tools.MathTools;

import gov.anl.ipns.MathTools.Geometry.*;

public class Vector3D_Test
{

  public static void main( String args[] )
  {
    int SIZE = 10000000;

    double elapsed = 0;
    double base_time = System.nanoTime()/1.0e6;
/*
    Vector3D[] old_vec = new Vector3D[SIZE];
    Vector3D old_sum = new Vector3D();

    for ( int i = 0; i < SIZE; i++ )
      old_vec[i] = new Vector3D(1,2,3);

    for ( int i = 0; i < SIZE; i++ )
      old_sum.add( old_vec[i] );

    elapsed = System.nanoTime()/1.0e6 - base_time;
    System.out.println("For old style vectors, add took " + elapsed );
*/
    base_time = System.nanoTime()/1.0e6;

    Vector3D[] new_vec = new Vector3D[SIZE];
    Vector3D   new_sum = new Vector3D();

    for ( int i = 0; i < SIZE; i++ )
      new_vec[i] = new Vector3D(1,2,3);

    for ( int i = 0; i < SIZE; i++ )
      new_sum.add( new_vec[i] );

    elapsed = System.nanoTime()/1.0e6 - base_time;
    System.out.println("For new style vectors, add took " + elapsed );
  }

}
