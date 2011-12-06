/* 
 * File: WriteSlicesToHDF_5.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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


package EventTools.ShowEventsApp.DataHandlers;


import ncsa.hdf.object.*;     // the common object package
import ncsa.hdf.object.h5.*;  // the HDF5 implementation

import ncsa.hdf.hdf5lib.HDF5Constants;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class handles writing slices of reciprocal space to an HDF 5 file
 *  in the form required by the ZODS program.
 */
public class WriteSlicesToHDF_5
{

  public static H5File OpenH5_File( String filename ) throws Exception
  { 
    FileFormat file_format = 
                   FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

    if (file_format == null)
      throw new IllegalArgumentException("Couldn't get HDF5 FileFormat");

    H5File file = (H5File)file_format.createFile(filename, 
                                                 FileFormat.FILE_CREATE_DELETE);

    if ( file == null )
      throw new IllegalArgumentException("Could not create file " + filename );

    return file;
  }


  private static void AddVectorAttribute( Group    group, 
                                          Datatype type,
                                          String   name,
                                          Vector3D vec ) throws Exception
  {
    long[]    dims  = {3};
    Attribute attr  = new Attribute( name, type, dims );
    double[] coords = new double[3];
    coords[0] = vec.getX();
    coords[1] = vec.getY();
    coords[2] = vec.getZ();
    attr.setValue( coords );
    group.writeMetadata( attr );
  }


  private static void AddMatrixAttribute( Group     group,
                                          Datatype  type,
                                          String    name,
                                          float[][] matrix ) throws Exception
  {
    long[]    dims   = {3,3};
    Attribute attr   = new Attribute( name, type, dims );
    double[][] d_mat = new double[3][3];
    
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        d_mat[row][col] = matrix[row][col]; 

    attr.setValue( d_mat );
    group.writeMetadata( attr );
  }


  private static void AddSizeAttribute( Group    group,
                                        Datatype type,
                                        String   name,
                                        int[]    sizes ) throws Exception
  {
    long[]    dims  = { 3 };
    Attribute attr  = new Attribute( name, type, dims );
    attr.setValue( sizes );
    group.writeMetadata( attr );
  }


  private static void AddUintAttribute( Group    group,
                                        Datatype type,
                                        String   name,
                                        int      value ) throws Exception
  {
    long[] attrDims  = { 1 };    
    int[]  attrValue = { value }; 

    Attribute attr = new Attribute( name, type, attrDims );
    attr.setValue( attrValue ); 

    group.writeMetadata( attr );
  }


  /**
   * Write all of the "pages" of the specified 3D array to the specified
   * file in HDF 5 format.  The file will be overwritten if it already
   * exists.
   */
  public static void WriteFile( String       filename,
                                boolean      in_HKL,
                                float[][]    orientation_matrix,
                                Vector3D     origin,
                                Vector3D     dir_1_scaled,
                                Vector3D     dir_2_scaled,
                                Vector3D     dir_3_scaled,
                                float[][][]  data
                              ) throws Exception
  {
    H5File out_file = OpenH5_File( filename );
    out_file.open();

    Datatype le_double_type = out_file.createDatatype( Datatype.CLASS_FLOAT,
                                                       8,
                                                       Datatype.ORDER_LE,
                                                       -1 );

    Datatype le_uint_type   = out_file.createDatatype( Datatype.CLASS_INTEGER,
                                                       4,
                                                       Datatype.ORDER_LE,
                                                       Datatype.SIGN_NONE );

    Group root = (Group)
          ((javax.swing.tree.DefaultMutableTreeNode)out_file.getRootNode())
          .getUserObject();

    // create groups at the root
    Group coord_group =  out_file.createGroup("CoordinateSystem", root);
    
    if ( in_HKL )
      AddUintAttribute( coord_group, le_uint_type, "isLocal", 1 );
    else
      AddUintAttribute( coord_group, le_uint_type, "isLocal", 0 );

    if ( orientation_matrix != null )
      AddMatrixAttribute( coord_group, 
                          le_double_type, 
                          "orientation_matrix",
                          orientation_matrix );

    int    gzip_level = 0;

    Group data_group = out_file.createGroup("Data", root); 

    int n_pages = data.length;
    int n_rows  = data[0].length;
    int n_cols  = data[0][0].length;

    Vector3D slice_origin = new Vector3D( origin );
    for ( int page = 0; page < n_pages; page++ )
    {
      Group p_group = out_file.createGroup("Data_"+page, data_group);
      AddVectorAttribute( p_group, le_double_type, "origin", slice_origin );

      int[] sizes = { 1, n_rows, n_cols };
      AddSizeAttribute( p_group, le_uint_type, "size", sizes );

      AddVectorAttribute( p_group, le_double_type,"direction_1", dir_1_scaled );
      AddVectorAttribute( p_group, le_double_type,"direction_2", dir_2_scaled );
      AddVectorAttribute( p_group, le_double_type,"direction_3", dir_3_scaled );

      /* Code to write as 1D array
      
      int      data_size = n_rows * n_cols;
      long[]   data_dims = { data_size };
      double[] data_1D   = new double[n_rows * n_cols];

      int index = 0;
      for ( int row = 0; row < n_rows; row++ )
        for ( int col = 0; col < n_cols; col++ )
        {
          data_1D[index] = data[page][row][col];
          index++;
        }

      Dataset dataset = out_file.createScalarDS ("Data", 
                                                 p_group, 
                                                 le_double_type, 
                                                 data_dims, null, null, 
                                                 gzip_level, 
                                                 data_1D );
      */
      /* End 1D array code */

      /* Code to write as 2D array, so it can be viewed as an image in hdfview
      */
      long[] two_D_dims  = { n_rows, n_cols };
      double[][] data_2D = new double[n_rows][n_cols];
      for ( int row = 0; row < n_rows; row++ )
        for ( int col = 0; col < n_cols; col++ )
          data_2D[row][col] = data[page][row][col];

      out_file.createScalarDS ("Data_2D",
                                p_group,
                                le_double_type,
                                two_D_dims, null, null,
                                gzip_level,
                                data_2D );
      /* End 2D array code */

      slice_origin.add( dir_1_scaled );   // update origin for next slice
    }

    out_file.close();
  }


  /**
   *  This main program provides a simple unit test for writing a sequence
   *  of slices in the ZODS hdf5 file format.
   */
  public static void main( String args[] ) throws Exception
  {
    if ( args.length < 1 )
    {
      System.out.println("Enter the name of the file to write on command line");
      System.exit(1);
    }

    String filename = args[0];
    boolean in_HKL  = true;
    Vector3D origin = new Vector3D( -1.6f, -1.6f, 0 );
    Vector3D dir_1_scaled = new Vector3D( 0.04f, 0, 0 );
    Vector3D dir_2_scaled = new Vector3D( 0, 0.04f, 0 );
    Vector3D dir_3_scaled = new Vector3D( 0, 0, 0.004f );

    float[][] orientation_mat = { {1, 0, 3}, {0, 1, 2}, {0, 0, 1} };

    int n_pages = 10;
    int n_rows  = 9;
    int n_cols  = 9;
    float[][][] data = new float[n_pages][n_rows][n_cols];
    int index   = 0;
    for ( int page = 0; page < n_pages; page++ )
     for ( int row = 0; row < n_rows; row++ )
       for ( int col = 0; col < n_cols; col++ )
       {
         data[page][row][col] = index;
         index++; 
       }

    WriteFile( filename, 
               in_HKL,
               orientation_mat,
               origin,
               dir_1_scaled,
               dir_2_scaled,
               dir_3_scaled,
               data
             );
  }


}
