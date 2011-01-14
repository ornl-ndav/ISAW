

package Operators.TOF_SCD;

import java.util.*;
import java.io.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.MathTools.*;

public class HKL_to_SORTAV_calc
{
                      // The HKL file uses formatted write/read, so some of
                      // the columns are adjacent with no white space between
                      // them.  Reading this requires extracting specific
                      // parts of the string for each value to be read :-( 
  public static final int H_START    = 0;
  public static final int H_LENGTH   = 4;

  public static final int K_START    = H_START + H_LENGTH;
  public static final int K_LENGTH   = 4;

  public static final int L_START    = K_START + K_LENGTH;
  public static final int L_LENGTH   = 4;

  public static final int FSQ_START  = L_START + L_LENGTH;
  public static final int FSQ_LENGTH = 8;

  public static final int SIG_START  = FSQ_START + FSQ_LENGTH;
  public static final int SIG_LENGTH = 8;

  public static final int HST_NUM_START  = SIG_START + SIG_LENGTH;
  public static final int HST_NUM_LENGTH = 4;

  public static final int WL_START       = HST_NUM_START + HST_NUM_LENGTH;
  public static final int WL_LENGTH      = 8;

  public static final int TBAR_START     = WL_START + WL_LENGTH;
  public static final int TBAR_LENGTH    = 7;

  public static final int CUR_HST_START  = TBAR_START + TBAR_LENGTH;
  public static final int CUR_HST_LENGTH = 7;

  public static final int SEQ_NUM_START  = CUR_HST_START + CUR_HST_LENGTH;
  public static final int SEQ_NUM_LENGTH = 7;

  public static final int TRANSM_START  = SEQ_NUM_START + SEQ_NUM_LENGTH;
  public static final int TRANSM_LENGTH = 7;

  public static final int DET_NUM_START  = TRANSM_START + TRANSM_LENGTH;
  public static final int DET_NUM_LENGTH = 4;


  
  private static int getInt( String line, int start, int length )
  {
     String str = line.substring( start, start+length );
     str = str.trim();
     return Integer.parseInt(str);
  }

  private static double getDouble( String line, int start, int length )
  {
     String str = line.substring( start, start+length );
     str = str.trim();
     return Double.parseDouble(str);
  }

  private static int getH( String hkl_line )
  {
    return getInt( hkl_line, H_START, H_LENGTH );
  }

  private static int getK( String hkl_line )
  {
    return getInt( hkl_line, K_START, K_LENGTH );
  }

  private static int getL( String hkl_line )
  {
    return getInt( hkl_line, L_START, L_LENGTH );
  }

  private static double getFsq( String hkl_line )
  {
    return getDouble( hkl_line, FSQ_START, FSQ_LENGTH );
  }

  private static double getSig( String hkl_line )
  {
    return getDouble( hkl_line, SIG_START, SIG_LENGTH );
  }

  private static int getHstnum( String hkl_line )
  {
    return getInt( hkl_line, HST_NUM_START, HST_NUM_LENGTH );
  }

  private static double getWL( String hkl_line )
  {
    return getDouble( hkl_line, WL_START, WL_LENGTH );
  }

  private static double getTbar( String hkl_line )
  {
    return getDouble( hkl_line, TBAR_START, TBAR_LENGTH );
  }

  private static int getCurhst( String hkl_line )
  {
    return getInt( hkl_line, CUR_HST_START, CUR_HST_LENGTH );
  }

  private static int getSeqnum( String hkl_line )
  {
    return getInt( hkl_line, SEQ_NUM_START, SEQ_NUM_LENGTH );
  }

  private static double getTransm( String hkl_line )
  {
    return getDouble( hkl_line, TRANSM_START, TRANSM_LENGTH );
  }

  private static int getDetnum( String hkl_line )
  {
    return getInt( hkl_line, DET_NUM_START, DET_NUM_LENGTH );
  }


  private static double[][] readOrientationMatrix( String directory,
                                                   String matrix_file )
  {
                                 // NOTE: readOrient transposes the matrix 
                                 //       from the file, so it returns
                                 //       the actual orientation matrix
    String fname = directory + "/" + matrix_file;
    Object Res = Operators.TOF_SCD.IndexJ.readOrient( fname );

    if ( Res == null || !(Res instanceof float[][]) )
      throw new IllegalArgumentException( "Matrix file not loaded: " + fname ); 

    float[][]  fmat  = (float[][])Res;
    double[][] ormat = new double[3][3];
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        ormat[row][col] = fmat[row][col];
 
//  System.out.println("Orientation matrix = ");
//  LinearAlgebra.print( ormat );
    return ormat;
  }


  /**
   *  Construct the rotated UB matrix, UgUB, where Ug represents the
   *  rotation of the goniometer and UB is the basic UB matrix that 
   *  corresponds to all zero goniometer angles.
   */
  private static double[][] getRotatedUB( Peak_new peak, double[][] basicUB )
  {
    float phi   = peak.phi();
    float chi   = peak.chi();
    float omega = peak.omega();

    Tran3D_d gonio_rotation = tof_calc_d.makeEulerRotation( phi, chi, omega );
    double[][] gonio_mat_4 = gonio_rotation.get();
    double[][] gonio_mat_3 = new double[3][3];
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        gonio_mat_3[row][col] = gonio_mat_4[row][col];

    double[][] UgUB = LinearAlgebra.mult( gonio_mat_3, basicUB );
/*
    System.out.println( "phi,chi,omega = " + phi + ", " + chi + ", " + omega );
    System.out.println("U = ");
    LinearAlgebra.print( lattice_calc.getU( UgUB ) );
    System.out.println("B = ");
    LinearAlgebra.print( lattice_calc.getB( UgUB ) );
*/
    return UgUB;
  }


  /**
   *  Return the direction cosines of the specified vector in the directions
   *  of the column vectors of the specified matrix U.  The columns of U must
   *  all have magnitude 1, as vectors and be mutually perpendicular.  That
   *  is, U must be an orthogonal transformation.
   *
   *  @param U     3x3 matrix whose columns represent three ortho-normal
   *               vectors.  This will typically be the "U" factor in an
   *               orientation matrix, UB.
   *  @param vec   The vector whose direction cosines will be calculated.
   */
  private static Vector3D_d getDirectionCosines( double[][] U, Vector3D_d vec )
  {
    Vector3D_d[] columns = new Vector3D_d[3];
    for ( int col = 0; col < 3; col++ )
    {
      columns[col] = new Vector3D_d( U[0][col], U[1][col], U[2][col] );
      columns[col].normalize();    // just to be sure!
    }
    vec = new Vector3D_d( vec );
    vec.normalize();               // just to be sure here too!

    Vector3D_d comps = new Vector3D_d( columns[0].dot( vec ),
                                       columns[1].dot( vec ),
                                       columns[2].dot( vec ) );
//    comps.normalize();
    return comps;
  }


  /**
   *  Get the lab coordinates of the detector pixel corresponding to
   *  the specified peak, in the IPNS coordinate system.
   *
   *  @param peak
   */
  private static Vector3D_d getIPNS_coords( Peak_new peak )
  {
    IDataGrid grid = peak.getGrid();
    Vector3D pixel_pos = grid.position( peak.y(), peak.x() );
    
    return new Vector3D_d( pixel_pos.getX(), 
                           pixel_pos.getY(),
                           pixel_pos.getZ() );
  }


  /**
   *  This method will write a file of peak information with integrated 
   *  intensities AND direction cosines for the incident and scattered
   *  beams, in the form needed by SORTAV.  This requires three files as
   *  input, a .integrate file and .mat file from ISAW and the .hkl file
   *  produced by ANVRED.  The .integrate, .hkl and .mat files must ALL 
   *  be in one specified directory.  The input files must follow the 
   *  naming conventions:
   *
   *  Integrate file: <exp_name>.integrate
   *  HKL file:       <exp_name>.hkl
   *
   *  The file created for the SORTAV program is saved in the specified
   *  directory with the name:
   *
   *  Created file:  <exp_name>.sortav
   *
   *  The output file is written as a DOS text file with lines terminated
   *  by a carriage return and line feed pair.  This is needed so that the
   *  file can be read properly by WinGX on a windows machine.
   *
   *  @param directory   The name of the directory containing the input
   *                     files and to which the resulting .sortav file
   *                     will be written.
   *  @param exp_name    The experiment name and base name for all files
   *
   *  @param matrix_file The name of the matrix file, in the specified
   *                     directory.
   *
   */ 
  public static void HKL_to_SORTAV( String directory,
                                    String exp_name,
                                    String matrix_file )
                     throws IOException
  {
    double[][] basicUB = readOrientationMatrix( directory, matrix_file );
    double[][] UB      = null;
    double[][] U       = null;

    Vector3D_d minus_u0       = new Vector3D_d( -1, 0, 0 );  // in IPNS coords
    Vector3D_d minus_u0_comps = new Vector3D_d();
    Vector3D_d u1             = new Vector3D_d();
    Vector3D_d u1_comps       = new Vector3D_d();

    String integrate_file = directory + "/" + exp_name + ".integrate";
    String hkl_file       = directory + "/" + exp_name + ".hkl";
    String sortav_file    = directory + "/" + exp_name + ".sortav";

    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( integrate_file );
    
    Scanner sc = new Scanner( new File( hkl_file ) );
    String line = sc.nextLine();

    FileWriter     fstream = new FileWriter( sortav_file );
    BufferedWriter out     = new BufferedWriter(fstream);

    int old_runnum = 0;
    int seq_num    = getSeqnum( line );
    while ( seq_num != 0 )
    {
      int    histnum = getHstnum( line );
      int    runnum  = getCurhst( line );
      int    h       = getH( line );
      int    k       = getK( line );
      int    l       = getL( line );
      double fsq     = getFsq( line );
      double sig     = getSig( line );
      double tbar    = getTbar( line );
      
      Peak_new peak = peaks.elementAt( seq_num - 1 );
      if ( runnum != old_runnum )     // new goniometer settings
      {
        UB = getRotatedUB( peak, basicUB );             // NOTE: U, UB in IPNS
        U  = lattice_calc.getU( UB );                   //       coordinates
        old_runnum = runnum;

        minus_u0_comps = getDirectionCosines( U, minus_u0 );
/*
        System.out.println("Run number = " + runnum );
        System.out.println( "-u0 components = " + minus_u0_comps );
*/
      }

      u1 = getIPNS_coords( peak ); 
      u1.normalize();
      u1_comps = getDirectionCosines( U, u1 );

      String result = String.format( "%5d%5d%5d%10.2f%10.2f%5d" +
                                     "%9.5f%9.5f%9.5f" +
                                     "%9.5f%9.5f%9.5f" + "%9.3f\r\n",
                                      h,k,l, fsq, sig, histnum, 
                                      minus_u0_comps.getX(), 
                                      minus_u0_comps.getY(), 
                                      minus_u0_comps.getZ(),
                                      u1_comps.getX(), 
                                      u1_comps.getY(), 
                                      u1_comps.getZ(),
                                      tbar );
      out.write( result );
      /*
      double cos_ang_1 = u1.dot( minus_u0 );
      double cos_ang_2 = u1_comps.dot( minus_u0_comps );
      System.out.println( result + ", " + runnum + ", " + seq_num +
                          ", " + cos_ang_1 + ", " + cos_ang_2 );
      */
      line = sc.nextLine();           // advance to the next peak, if any
      seq_num = getSeqnum( line );
    }
    out.close();

  } 
                                     

  public static void main( String args[] ) throws IOException
  {
    // first test reading of .hkl file
    String directory   = "/usr2/TOPAZ_20/";
    String exp_name    = "newPeakFitBox6by6";
    String matrix_file = "lsnewPeakFitBox6by6.mat";

    String hkl_file    = directory + exp_name + ".hkl";
    Scanner sc = new Scanner( new File( hkl_file ) );

    int line_count = 0;
    int err_count  = 0;
    int seq_num    = 1;
    while ( seq_num != 0 ) 
    {
      String line = sc.nextLine();
      line_count++;

      String result = String.format( "%4d",    getH( line ) );
      result += String.format( "%4d",    getK( line ) );
      result += String.format( "%4d",    getL( line ) );
      result += String.format( "%8.2f",  getFsq( line ) );
      result += String.format( "%8.2f",  getSig( line ) );
      result += String.format( "%4d",    getHstnum( line ) );
      result += String.format( "%8.4f",  getWL( line ) );
      result += String.format( "%7.4f",  getTbar( line ) );
      result += String.format( "%7d",    getCurhst( line ) );
      result += String.format( "%7d",    getSeqnum( line ) );
      result += String.format( "%7.4f",  getTransm( line ) );
      result += String.format( "%4d",    getDetnum( line ) );
      if ( line.compareTo( result ) != 0 )
      {
        System.out.println("ERROR PARSING FILE LINE:");
        System.out.println(line);
        System.out.println(result);
        err_count++;
      }
      seq_num = getSeqnum( line );
    }
    System.out.println("Lines Read = " + line_count + " ERRORS = " + err_count);

    // Now test writing the .sortav file
    HKL_to_SORTAV( directory, exp_name, matrix_file );

  }

}
