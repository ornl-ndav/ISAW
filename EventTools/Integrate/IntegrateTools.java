


package EventTools.Integrate;

import EventTools.EventList.*;
import gov.anl.ipns.MathTools.LinearAlgebra;


public class IntegrateTools
{

  public static float[]  IntegrateSpheres( float[]      q_vec,
                                           float        max_radius, 
                                           int          num_radii,
                                           boolean      use_weights,
                                           IEventList3D event_list )
  {
    float qx = q_vec[0];
    float qy = q_vec[1];
    float qz = q_vec[2];

    float[] sums       = new float[ num_radii ];
    float[] d_squareds = new float[ num_radii ];

    float delta_radius = max_radius / num_radii;
    float radius;
    for ( int i = 0; i < num_radii; i++ )
    {
      sums[i] = 0;
      radius = (i+1) * delta_radius;
      d_squareds[i] = radius * radius;
    } 

    int     num_events = event_list.numEntries();
    float[] xyz        = event_list.eventVals();
    float[] weights    = event_list.eventWeights();
    float   x,
            y,
            z;
    float   d_squared;

    int index = 0;
    for ( int i = 0; i < num_events; i++ )
    {
      x = qx - xyz[ index++ ];
      y = qy - xyz[ index++ ];
      z = qz - xyz[ index++ ];
      d_squared = x*x + y*y + z*z;
      for ( int k = 0; k < num_radii; k++ )
        if ( d_squared <= d_squareds[k] )
        {
          if ( use_weights )
            sums[ k ] += weights[ i ];
          else
            sums[ k ] += 1;
        }
    }
    return sums;
  }


  public static float[] getSphereRadii( float max_radius, int num_radii )
  {
    double max_volume = 4 * Math.PI * Math.pow( max_radius, 3 ) / 3;
    double d_vol = max_volume / num_radii;
    
    float[] radii = new float[ num_radii ];
    for ( int i = 0; i < num_radii; i++ )
    {
      double vol = (i+1) * d_vol;
      radii[i] = (float)Math.pow( vol * 3. / (4. * Math.PI), 1./3. );
    }

    return radii;
  }


  public static float[] hkl_to_q( float[][] mat, float[] hkl )
  {
    float[][] or_mat = LinearAlgebra.getTranspose( mat );
    float[]   q      = LinearAlgebra.mult( or_mat, hkl );
    return q;
  }

  
  public static void main( String args[] ) throws Exception
  {
    SNS_Tof_to_Q_map mapper = new SNS_Tof_to_Q_map( null, null, "SNAP" );

    String filename = "/usr2/DEMO/SNAP_240_neutron_event.dat";
    SNS_TofEventList tof_evl = new SNS_TofEventList( filename );
   
    int num_events = (int)tof_evl.numEntries();
    IEventList3D Q_evl = mapper.MapEventsToQ( tof_evl, 0, num_events );

    float   max_radius   = 0.2f;
    int     num_radii    = 20;
    float[] radii        = new float[ num_radii ];
    float[] volumes      = new float[ num_radii ];
    float   delta_radius = max_radius / num_radii;
    float   radius;
    for ( int i = 0; i < num_radii; i++ )
    {
      radius = (i+1) * delta_radius; 
      radii[i]   = radius;
      volumes[i] = (float)(4./3. * Math.PI * radius * radius * radius);
    }

    float[] shell_volumes = new float[num_radii];
    shell_volumes[0] = volumes[0];
    for ( int i = 1; i < num_radii; i++ )
      shell_volumes[i] = volumes[i] - volumes[i-1];

    // peak with h,k,l = -3,-1,0 in run SNAP_240, 
    // detector 12, col = 116.89, row = 160.08, chan = 546.8
    //
    // Orientation matrix  0.121733  0.162127 -0.118117
    //                     0.128067  0.158202  0.116424
    //                     0.145964 -0.113030 -0.005984
    float qx = -.49f;
    float qy = -.64f;
    float qz =  .24f;
    float[] q_vec = new float[3];
    q_vec[0] = qx;
    q_vec[1] = qy;
    q_vec[2] = qz;

    float[][] mat = { { 0.121733f,  0.162127f, -0.118117f },
                      { 0.128067f,  0.158202f,  0.116424f },
                      { 0.145964f, -0.113030f, -0.005984f } };

//    float[] hkl = { -3, -1, 0 };
//    float[] hkl = { -4, -1, 0 };
    float[] hkl = { -4, -2, -1 };
    float[] temp_q = hkl_to_q( mat, hkl );
    System.out.println("Calculated Q = " + temp_q[0] + 
                                    ", " + temp_q[1] +
                                    ", " + temp_q[2]  );
    q_vec = temp_q;

    q_vec[0] *= (float)Math.PI * 2;
    q_vec[1] *= (float)Math.PI * 2;
    q_vec[2] *= (float)Math.PI * 2;
    float[] integrals  = IntegrateSpheres( 
                                 q_vec, max_radius, num_radii, false, Q_evl );

    float[] delta_i = new float[ num_radii ];
    delta_i[0] = integrals[0];
    for ( int i = 1; i < num_radii; i++ )
      delta_i[i] = integrals[i] - integrals[i-1];

    System.out.println("  i          int       radii   d_int/d_vol");
    for ( int i = 0; i < num_radii; i++ )
      System.out.printf( "%3d   %10.4f  %10.4f  %10.4f\n", 
                           i, 
                           integrals[i], 
                           radii[i], 
                           delta_i[i]/shell_volumes[i] );
  }

}
