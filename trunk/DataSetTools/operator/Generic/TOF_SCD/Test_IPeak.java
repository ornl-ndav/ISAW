package DataSetTools.operator.Generic.TOF_SCD;

import gov.anl.ipns.MathTools.Geometry.Vector3D;
//import DataSetTools.dataset.Data;
//import DataSetTools.dataset.DataSet;
//import DataSetTools.dataset.GeometricProgressionXScale;
import DataSetTools.dataset.IDataGrid;
//import DataSetTools.dataset.LogXScale;
import DataSetTools.dataset.UniformGrid;
//import DataSetTools.dataset.XScale;
import DataSetTools.instruments.IPNS_SCD_SampleOrientation;
import DataSetTools.instruments.SampleOrientation;
//import DataSetTools.retriever.RunfileRetriever;

public class Test_IPeak 
{

  public static IPeak make_test_Peak_new()
  {
    int   run_num = 8336;
    float mon_ct  = 10101f;
    float col   = 14.64f;
    float row   = 42.12f;
    float chan  = 24.55f;
    float phi   = 0;
    float chi   = 167;
    float omega = 45;
    int   id    = 17;
    Vector3D center = new Vector3D(  0.064748f, -.253230f,  -0.011417f );
    Vector3D x_vec  = new Vector3D( -0.954441f, -0.298400f, -0.000457f );
    Vector3D y_vec  = new Vector3D(  0.000054f, -0.001706f,  0.999999f );
    float    width  = 17.1228f / 100;
    float    height = 17.1218f / 100;
    float    depth  = 0.2f / 100;
    int      n_rows = 100;
    int      n_cols = 100;
    IDataGrid grid = new UniformGrid( id, "m",
                                      center, x_vec, y_vec,
                                      width, height, depth,
                                      n_rows, n_cols );
    SampleOrientation orientation = 
                         new IPNS_SCD_SampleOrientation( phi, chi, omega );
    float tof = 1263;
    float initial_path = 9.3777f;
    float t0 = 0;

    IPeak peak = new Peak_new( run_num,
                               mon_ct,
                               col, row, chan, 
                               grid,
                               orientation, 
                               tof,
                               initial_path,
                               t0             );
    peak.setFacility( "IPNS" );
    peak.setInstrument( "SCD0" );
    peak.seqnum( 46 );
    peak.sethkl( -2, 9, 3 );
    peak.ipkobs(21);
    peak.inti( 104.99f );
    peak.sigi( 20.35f );
    peak.reflag( 10 );

    return peak;
  }
  
  
  public static void show_peak( IPeak peak )
  {
    IDataGrid grid = ((Peak_new)peak).getGrid();
    int det_num = peak.detnum();
    int n_rows  = grid.num_rows();
    int n_cols  = grid.num_cols();
    float width = grid.width()  * 100;    // convert to cm
    float height= grid.height() * 100;
    float depth = grid.depth()  * 100;
    Vector3D center = grid.position();
    Vector3D base   = grid.x_vec();
    Vector3D up     = grid.y_vec();
    float det_d = center.length() * 100;
	
    System.out.println( "4 DETNUM  NROWS  NCOLS  WIDTH HEIGHT  DEPTH " +
                        "  DETD CenterX CenterY CenterZ   BaseX  " +
                        " BaseY   BaseZ     UpX     UpY     UpZ" );
    System.out.printf("5 %6d %6d %6d %6.3f %6.3f %6.3f %6.2f ",
                       det_num, n_rows, n_cols, width, height, depth, det_d );
    System.out.printf("%7.3f %7.3f %7.3f ", 
                       center.getX()*100, center.getY()*100, center.getZ()*100);
    System.out.printf("%7.3f %7.3f %7.3f ", 
                       base.getX(), base.getY(), base.getZ());
    System.out.printf("%7.3f %7.3f %7.3f\n", 
                       up.getX(), up.getY(), up.getZ());
	
    int run_num = peak.nrun();
    SampleOrientation orientation = ((Peak_new)peak).getSampleOrientation();
    float chi = peak.chi();
    float phi = peak.phi();
    float omega = peak.omega();
    float monct = peak.monct();
    float l1    = peak.L1() * 100;
    System.out.println();
    System.out.println("0 NRUN DETNUM    CHI    PHI  OMEGA MONCNT     L1");
    System.out.printf( "0 %4d %6d %6.2f %6.2f %6.2f %6.0f %6.1f\n",
                        run_num, det_num, chi, phi, omega, monct, l1 );
    
    int seqn   = peak.seqnum();
    float h    = peak.h();
    float k    = peak.k();
    float l    = peak.l();
    float col  = peak.x();
    float row  = peak.y();
    float chan = peak.z();
    float[] spherical_coords = ((Peak_new)peak).NeXus_coordinates();
    float l2        = spherical_coords[0] * 100;
    float two_theta = spherical_coords[2];
    float azimuth   = spherical_coords[1];
    float wl        = peak.wl();
    float dspacing  = ((Peak_new)peak).d();
    float ipk       = peak.ipkobs();
    float inti      = peak.inti();
    float sigi      = peak.sigi();
    int   reflag    = peak.reflag();
    
    System.out.println();
    System.out.println("2   SEQN    H    K    L     COL     ROW    CHAN  " +
    		           "     L2  2_THETA       AZ        WL        D " + 
    		           "  IPK      INTI   SIGI RFLG");
    System.out.printf("3 %6d %4.0f %4.0f %4.0f %7.2f %7.2f %7.2f " +
    		          "%8.3f %8.5f %8.5f %9.6f %8.4f " +
    		          "%5.0f %9.2f %6.2f %4d\n",
    		          seqn, h, k, l, col, row, chan, 
    		          l2, two_theta, azimuth, wl, dspacing,
    		          ipk, inti, sigi, reflag );
    
    float[] q        = peak.getQ();
    float[] un_rot_q = peak.getUnrotQ();
    System.out.println();
    System.out.printf( "       Qxyz = %7.3f %7.3f %7.3f\n", 
                               q[0], q[1], q[2]);
    System.out.printf( "Un Rot Qxyz = %7.3f %7.3f %7.3f\n", 
                       un_rot_q[0], un_rot_q[1], un_rot_q[2] );
    
    float tof = peak.time();
    float xcm = peak.xcm();
    float ycm = peak.ycm(); 
    System.out.printf("tof         = %7.1f\nxcm, ycm    =  %6.3f  %6.3f\n", 
                       tof, xcm, ycm );
    System.out.println( "nearedge    =  " + peak.nearedge() );
  }
  
  
  public static void main( String args[] )
  {
    IPeak peak = make_test_Peak_new();
    show_peak( peak );
    
    IPeak peak_2 = peak.createNewPeakxyz( 76, 81, 104.5f, 2828.2f );
    show_peak( peak_2 );
    
    peak_2.sethkl( 0, 0, 0 );
    float[][] UB = { { 0.001592f, 0.105113f, -0.158811f },
                     {-0.003425f, 0.172983f,  0.094881f },
                     { 0.234962f, 0.119320f,  0.002397f }
                   };
    peak_2.UB( UB );
    peak_2.seqnum( 37 );
    peak_2.ipkobs( 44 );
    peak_2.inti( 262.3f );
    peak_2.sigi( 23.68f );
    show_peak( peak_2 );
    
    IPeak peak_3 = peak_2.createNewPeakxyz( 63.89f, 88.77f, 44.07f, 1516 ); 
    peak_3.seqnum( 12 );
    peak_3.ipkobs( 50 );
    peak_3.inti( 290.52f );
    peak_3.sigi( 24.83f );
    show_peak( peak_3 );
   /* 
    String directory = "/usr2/SCD_TEST/";
    String full_name = directory + "scd08336.run";
    RunfileRetriever rr = new RunfileRetriever( full_name );
    DataSet ds = rr.getDataSet( 2 );
    Data data = ds.getData_entry( 0 );
    XScale xscale = data.getX_scale();
    float[] x_vals = xscale.getXs();

    XScale  log_scale = new LogXScale(1000, 14974.3f, 10, LogXScale.IPNS_SCD); 
    float[] log_vals  = log_scale.getXs();
    for ( int i = 0; i < x_vals.length; i++ )
      System.out.printf("%12.3f  %12.3f \n", x_vals[i], log_vals[i] );
    */
  }
}
