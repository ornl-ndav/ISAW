package Operators.TOF_SCD;

import java.util.*;
import gov.anl.ipns.MathTools.Geometry.*;


public class ConventionalCellInfo
{
  private int    form_num;
  private float  scalars_error;
  private float  scalars_error_2;
  private String cell_type;
  private String centering;
  private Tran3D original_UB;
  private Tran3D adjusted_UB;

  public ConventionalCellInfo( Tran3D          UB, 
                               ReducedCellInfo form_0, 
                               ReducedCellInfo form_i )
  {
    form_num = form_i.getFormNum();
    scalars_error = (float)form_0.weighted_distance( form_i );
    scalars_error_2 = (float)form_0.distance( form_i );
    cell_type = form_i.getCellType();
    centering = form_i.getCentering();
    original_UB = new Tran3D( UB );

    adjusted_UB = new Tran3D( UB );
    double[][] cell_tran = form_i.getTransformation();
    float[][]  cell_tran_f = new float[3][3];
    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        cell_tran_f[i][j] = (float)cell_tran[i][j];

    Tran3D new_tran = new Tran3D( cell_tran_f );
    new_tran.invert();
    adjusted_UB.multiply_by( new_tran );
    if ( !IndexingUtils.isRightHanded( adjusted_UB ) )
    {
      Vector3D a_vec = new Vector3D();
      Vector3D b_vec = new Vector3D();
      Vector3D c_vec = new Vector3D();
      IndexingUtils.getABC( adjusted_UB, a_vec, b_vec, c_vec );
      Vector3D minus_c = new Vector3D( c_vec );
      minus_c.multiply( -1 );
      IndexingUtils.getUB ( adjusted_UB, a_vec, b_vec, minus_c );
    }

    if ( ReducedCellInfo.ORTHORHOMBIC.startsWith( cell_type ) )
      SetSidesIncreasing( adjusted_UB );

    else if ( ReducedCellInfo.TETRAGONAL.startsWith( cell_type ) )
      StandardizeTetragonal( adjusted_UB );

    else if ( ReducedCellInfo.HEXAGONAL.startsWith( cell_type ) ||
              ReducedCellInfo.RHOMBOHEDRAL.startsWith( cell_type ) )
     StandardizeHexagonal( adjusted_UB );
  }


  /**
   *  Change UB to a new matrix corresponding to a unit cell with the sides
   *  in increasing order of magnitude.  This is used to arrange the UB matrix
   *  for an orthorhombic cell into a standard order.
   *  @param UB on input this should correspond to an orthorhombic cell. 
   *            On output, it will correspond to an orthorhombic cell with
   *            sides in increasing order.
   */
  private static void SetSidesIncreasing( Tran3D UB )
  {
    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();
    IndexingUtils.getABC( UB, a, b, c );
    Vector<Vector3D> edges = new Vector<Vector3D>();
    edges.add( a );
    edges.add( b );
    edges.add( c );
    edges = IndexingUtils.SortOnVectorMagnitude( edges );

    a = edges.elementAt(0);
    b = edges.elementAt(1);
    c = edges.elementAt(2);

    Vector3D cross = new Vector3D();
    cross.cross( a, b );
    if ( cross.dot( c ) < 0 )     // if left handed, reflect the c vector
      c.multiply(-1);

    IndexingUtils.getUB( UB, a, b, c );
  }

  /**
   *  Change UB to a new matrix corresponding to a unit cell with the first 
   *  two sides approximately equal in magnitude.  This is used to arrange 
   *  the UB matrix for a tetragonal cell into a standard order.
   *
   *  @param UB on input this should correspond to a tetragonal cell.  
   *            On output, it will correspond to a tetragonal cell with the 
   *            first two sides, a and b, set to the two sides that are most
   *            nearly equal in length. 
   */
  private static void StandardizeTetragonal( Tran3D UB )
  {
    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();
    IndexingUtils.getABC( UB, a, b, c );

    float a_b_diff = Math.abs( a.length() - b.length() ) /
                     Math.min( a.length(), b.length() );

    float a_c_diff = Math.abs( a.length() - c.length() ) /
                     Math.min( a.length(), c.length() );

    float b_c_diff = Math.abs( b.length() - c.length() ) /
                     Math.min( b.length(), c.length() );

                          // if needed, change UB to have the two most nearly
                          // equal sides first.
    if ( a_c_diff <= a_b_diff && a_c_diff <= b_c_diff )
      IndexingUtils.getUB( UB, c, a, b );
    else if ( b_c_diff <= a_b_diff && b_c_diff <= a_c_diff )
      IndexingUtils.getUB( UB, b, c, a );
  }


  /**
   *  Change UB to a new matrix corresponding to a hexagonal unit cell 
   *  angles approximately 90, 90, 120.  This is used to arrange 
   *  the UB matrix for a hexagonal or rhombohedral cell into a standard order.
   *
   *  @param UB on input this should correspond to a hexagonal or rhombohedral
   *            On output, it will correspond to a hexagonal cell with angles
   *            approximately 90, 90, 120.
   */
  private static void StandardizeHexagonal( Tran3D UB )
  {
    Vector3D a = new Vector3D();
    Vector3D b = new Vector3D();
    Vector3D c = new Vector3D();
    IndexingUtils.getABC( UB, a, b, c );

    float alpha = IndexingUtils.angle( b, c );
    float beta  = IndexingUtils.angle( c, a );
                                                // first, make the non 90 
                                                // degree angle last
    if ( Math.abs(alpha-90) > 20 )
      IndexingUtils.getUB( UB, b, c, a );
    else if ( Math.abs(beta-90) > 20 )
      IndexingUtils.getUB( UB, c, a, b );

                                                // if the non 90 degree angle
                                                // is about 60 degrees, make
                                                // it about 120 degrees.
    IndexingUtils.getABC( UB, a, b, c );
    float gamma = IndexingUtils.angle( a, b );
    if ( Math.abs( gamma - 60 ) < 10 )
    {
      a.multiply( -1 );                         // reflect a and c to change
      c.multiply( -1 );                         // alpha and gamma to their
      IndexingUtils.getUB( UB, a, b, c );       // supplementary angle
    }
  }


  public int getFormNum()
  {
    return form_num;
  }

  public float getError()
  {
    return scalars_error;
  }

  public String getCellType()
  {
    return cell_type;
  }

  public String getCentering()
  {
    return centering;
  }

  public Tran3D getOriginalUB()
  {
    return new Tran3D( original_UB );
  }

  public Tran3D getNewUB()
  {
    return new Tran3D( adjusted_UB );
  }

  public float getSumOfSides()
  {
    float[] l_par = IndexingUtils.getLatticeParameters( adjusted_UB );
    return l_par[0] + l_par[1] + l_par[2];
  }

  public String toString()
  {
    String result = String.format( "Form # %2d  %-13s %10s %9.7f %10.7f ",
                  getFormNum(), getCellType(), getCentering(), getError(),
                  scalars_error_2 );

    float[] l_par = IndexingUtils.getLatticeParameters( adjusted_UB ); 
    result += String.format("%8.4f %8.4f %8.4f  %8.4f %8.4f %8.4f  %8.4f",
        l_par[0], l_par[1], l_par[2], l_par[3], l_par[4], l_par[5], l_par[6] );

    result += " RH = " + IndexingUtils.isRightHanded( adjusted_UB );
    return result; 
  }

}
