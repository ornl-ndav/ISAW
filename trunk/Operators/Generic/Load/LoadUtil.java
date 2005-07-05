/*
 * File:  LoadUtil.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.2  2005/07/05 17:10:19  dennis
 * Added method Load_GLAD_LPSD_Info() that will serve as the
 * "core" method for an Operator to add DataGrids corresponding
 * to the LPSDs on GLAD, to a GLAD DataSet as read from the IPNS
 * runfile.
 *
 * Revision 1.1  2005/06/27 05:05:33  dennis
 * File for static utility methods for loading missing information
 * into DataSets.  Currently, there is just one public method:
 * LoadDetectorInfo(), that will load information about area, LPSD or
 * single tube detectors, from a text file, into a DataSet, in the form
 * of "DataGrids".  Effective pixel positions are also set from the
 * DataGrids and references from the individual pixels will be set
 * pointing back to the spectrum (or group) with data from that pixel.
 * This is needed to "fix" the Data from LANSCE instruments AND can also
 * be used for the GLAD at IPNS.
 *
 */

package Operators.Generic.Load;

import java.io.*;
import java.util.*;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.instruments.*;

import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.MathTools.Geometry.*;


/**
 *  This class contains static method for Loading detector information
 *  into a DataSet.  This is necesary if the raw data file does not 
 *  contain enough information about the instrument and detector geometry 
 *  in a form that can be handled by the ISAW retrievers.
 */

public class LoadUtil 
{
  private static boolean debug_glad = false;       // debug flag for GLAD code

  private LoadUtil()
  {};

  /**
   *  Load detector information (and initial path, if present) for the
   *  specified DataSet from the specified file.  The file must list the
   *  values on separate lines, with a single identifier at the start of the 
   *  line.  Lines starting with "#" are ignored.  The accepted identifiers
   *  are:
   *        Initial_Path
   *        Num_Grids
   *        Grid_ID
   *        Num_Rows
   *        Num_Cols
   *        Width
   *        Height
   *        Depth
   *        Center
   *        X_Vector
   *        Y_Vector
   *        First_Index
   *  The identifier MUST appear at the start of the line, followed by spaces
   *  and  a single number, except for the Center, X_Vector and Y_Vector, which
   *  require three numbers separated by spaces.  
   *  NOTE: Values can be omitted, and the previous or default values will 
   *        be used.  This allows, for example, specifying the width, height
   *        and depth once for the first detector and omitting them for 
   *        later detectors that have the same dimensions.
   *
   *  @param  ds         DataSet for which the detector and initial path
   *                     info is read.
   *
   *  @param  file_name  Name of file containing the detector information 
   *
   */
  public static void LoadDetectorInfo( DataSet ds, String file_name )
  {
    try
    {
      TextFileReader f = new TextFileReader( file_name );

      Hashtable hash = new Hashtable( 2000 );
      Vector grid_names = LoadHashtable( f, hash );
      f.close();
                                    // set initial path iattribute in meters
                                    // if it's specified in the file
      Float Length_0 = getFloat( hash, "INITIAL_PATH" );
      if ( Length_0 != null )
      { 
        float length_0 = Length_0.floatValue();
        FloatAttribute initial_path = 
                               new FloatAttribute( "Initial Path", length_0 );
        ds.setAttribute( initial_path );
        int num_data = ds.getNum_entries();
        for ( int i = 0; i < num_data; i++ )
           ds.getData_entry(i).setAttribute( initial_path );
      }
                                                 // set up default values
      int      num_grids   = grid_names.size();
      int      grid_id     = -1;
      int      n_rows      = 1;
      int      n_cols      = 1;
      float    width       = 1;
      float    height      = 1;
      float    depth       = 1;
      Vector3D center      = new Vector3D( 0, 0.01f, 0 );
      Vector3D x_vec       = new Vector3D( 5, 0, 0 );
      Vector3D y_vec       = new Vector3D( 0, 0, -5 );
      int      first_index = 0;
                                                // now for each grid, change
                                                // current values if set
      for ( int i = 0; i < num_grids; i++ )
      {
        String prefix = (String)grid_names.elementAt(i);

        Integer Grid_id = getInteger( hash, prefix + "GRID_ID" );
        if ( Grid_id != null )
          grid_id = Grid_id.intValue();
       
        Integer N_rows = getInteger( hash, prefix + "NUM_ROWS" );
        if ( N_rows != null )
          n_rows = N_rows.intValue();

        Integer N_cols = getInteger( hash, prefix + "NUM_COLS" );
        if ( N_cols != null )
          n_cols = N_cols.intValue();
      
        Float Width = getFloat( hash, prefix + "WIDTH" );
        if ( Width != null )
          width = Width.floatValue();

        Float Height = getFloat( hash, prefix + "HEIGHT" );
        if ( Height != null )
          height = Height.floatValue();

        Float Depth = getFloat( hash, prefix + "DEPTH" );
        if ( Depth != null )
          depth = Depth.floatValue();
 
        Vector3D Center = getVector( hash, prefix + "CENTER" );
        if ( Center != null )
          center = new Vector3D( Center );

        Vector3D X_vector = getVector( hash, prefix + "X_VECTOR" );
        if ( X_vector != null )
          x_vec = new Vector3D( X_vector );

        Vector3D Y_vector = getVector( hash, prefix + "Y_VECTOR" );
        if ( Y_vector != null )
          y_vec = new Vector3D( Y_vector );

        Integer First_index = getInteger( hash, prefix + "FIRST_INDEX" );
        if ( First_index != null )
        {
          first_index = First_index.intValue();
          first_index--;    // shift index down by 1 to match the "C" style 
                            // numbering system, starting at 0.
        }

        UniformGrid grid = new UniformGrid( grid_id, "m",
                                            center, x_vec, y_vec,
                                            width, height, depth,
                                            n_rows, n_cols );
        grid.AddGridToDataSet( ds, first_index, true );

        first_index += n_rows * n_cols;
      }
    }
    catch ( Exception e )
    {
       System.out.println("Exception reading detecdtor file \n" +
                           file_name + "\n" + e );
    }
                                    // add the Diffractometer operators
    DataSetFactory.addOperators( ds, InstrumentType.TOF_DIFFRACTOMETER );
  }


  /**
   *  Load the hashtable with all of the "name", "value string" pairs from
   *  the file containing the detector information.  The "name" from the
   *  file has GRID_IDN prepended where N is ID number for the grid.  This
   *  allows the information for a particular detector to accessed from
   *  the hashtable.
   *
   *  @param     f      The open TextFileReader object
   *  @param     hash   The empty hashtable that is loaded from the file.
   *
   *  @return  A vector containing Strings identifying all of the DataGrids
   *           read from the file, in the form GRID_IDN where N is the 
   *           Grid ID assigned in the file.
   */
  private static Vector LoadHashtable( TextFileReader f, Hashtable hash ) 
                        throws IOException
  {
     Vector grid_names = new Vector( 1000 );
     String grid_name  = "";
     String name;
     String val_string;
     while ( !f.end_of_data() )
     {
       f.SkipLinesStartingWith( "#" ); 
       name = f.read_String();
       val_string = f.read_line();
       val_string = val_string.trim(); 
       if ( name.equalsIgnoreCase( "Grid_ID" ) )
       {
         grid_name = name + val_string;
         grid_names.add( grid_name.toUpperCase() ); 
       }
       hash.put( (grid_name +  name).toUpperCase(), val_string );
     }
     return grid_names;
  }


  /**
   *  Get the specified entry from the hashtable and interpret it as
   *  a Float value.
   *
   *  @param  hash   the hashtable that may or may not contain the named
   *                 entry.
   *  @param  name   the name of the entry to be returned as a Float
   *
   *  @return  A Float object giving the value of the entry specified by
   *           the name, or null if the named entry is not present, or 
   *           can not be interpreted as a Float.
   */
  private static Float getFloat( Hashtable hash, String name )
  {
    String  val_string = (String)hash.get( name );
    try
    {
       return new Float( val_string );
    }
    catch ( Exception e )
    {
       return null;
    }
  }


  /**
   *  Get the specified entry from the hashtable and interpret it as
   *  an Integer value.
   *
   *  @param  hash   the hashtable that may or may not contain the named
   *                 entry.
   *  @param  name   the name of the entry to be returned as an Integer 
   *
   *  @return  A Float object giving the value of the entry specified by
   *           the name, or null if the named entry is not present, or 
   *           can not be interpreted as an Integer.
   */
  private static Integer getInteger( Hashtable hash, String name )
  {
    String  val_string = (String)hash.get( name );
    try
    {
       return new Integer( val_string );
    }
    catch ( Exception e )
    {
       return null;
    }
  }

  /**
   *  Get the specified entry from the hashtable and interpret it as
   *  a Vector3D object.
   *
   *  @param  hash   the hashtable that may or may not contain the named
   *                 entry.
   *  @param  name   the name of the entry to be returned as a Vector3D
   *
   *  @return  A Float Vector3D giving the value of the entry specified by
   *           the name, or null if the named entry is not present, or 
   *           can not be interpreted as a Vector3D.
   */
  private static Vector3D getVector( Hashtable hash, String name )
  {
    String  val_string = (String)hash.get( name );
    try
    {
      int first_space = val_string.indexOf( " " ); 
      int last_space  = val_string.lastIndexOf( " " ); 
      String x_str = val_string.substring( 0, first_space );
      String y_str = val_string.substring( first_space, last_space );
      String z_str = val_string.substring( last_space, val_string.length() );
      float x = (float)Double.parseDouble( x_str.trim() ); 
      float y = (float)Double.parseDouble( y_str.trim() ); 
      float z = (float)Double.parseDouble( z_str.trim() ); 
      return new Vector3D( x, y, z );
    }
    catch ( Exception e )
    {
       return null;
    }
  }


  /**
   *  Load LPSD information for the IPNS GLAD instrument from the 
   *  configuration file, gladdets6.par, as used on the old VAX systems.
   *
   *  @param  ds         DataSet for IPNS GLAD, in the form it is 
   *                     currently (7/1/05) read from the IPNS Runfile.
   *
   *  @param  file_name  Name of file containing the GLAD detector information 
   */
  public static void Load_GLAD_LPSD_Info( DataSet ds, String file_name )
  {
    int N_ROWS      = 64;
    int N_COLS      = 1;
    float LPSD_HEIGHT = 0.64f;                  // NOTE: 64 needed to match 
                                                // results from IPNS package
    float LPSD_WIDTH  = 0.01074f;
    float LPSD_DEPTH  = 0.01074f;

    int grid_id = 1;
    Vector3D y_vec  = new Vector3D(  0, 0, 1 );
    Vector3D x_vec  = new Vector3D( -1, 1, 0 ); 
    Vector3D center = new Vector3D(  0, 1, 0 ); 
    int      bank;
    int      n_det;
    int      first_segment;
    try
    {
      TextFileReader f = new TextFileReader( file_name );
      while ( !f.end_of_data() )
      {
        bank  = f.read_int();
        n_det = f.read_int();
        if ( bank > 0 && n_det > 0 )       // ignore bank 0, which are monitors 
        {                                  // and banks with 0 detectors
           System.out.println("Processing bank " + bank + ", " + n_det );
           for ( int i = 0; i < n_det; i++ )
           {
             f.read_int();                 // skip det_in_bank
             f.read_int();                 // skip crate
             f.read_int();                 // skip slot
             f.read_int();                 // skip input
             first_segment = f.read_int(); 
             if ( first_segment >0 )
             {
               // for now, synthesize some positions
               float x = (float)(2 * Math.cos( grid_id/2000f * 8 * Math.PI ));
               float y = (float)(2 * Math.sin( grid_id/2000f * 8 * Math.PI ));
               center = new Vector3D( x, y, 0.5f );
               UniformGrid grid = new UniformGrid( grid_id, "m",
                                         center, x_vec, y_vec,
                                         LPSD_WIDTH, LPSD_HEIGHT, LPSD_DEPTH,
                                         N_ROWS, N_COLS );
               grid_id++;

               if ( first_segment == 10817 )  // NOTE: this is fix for segment 
                 first_segment = 10815;       // IDs in this particular detector

               AssignGridForPixels( ds, grid, first_segment, N_ROWS ); 
             }  
           }
         }
         else
         {               
            f.read_line();
            for ( int i = 0; i < n_det; i++ )    // skip lines for monitors
              f.read_line(); 
         }
       }
     }
     catch ( Exception e )
     {
        System.out.println("Exception reading file " + file_name + "\n" + e );
        e.printStackTrace();
     }
  }


  /**
   *  Go through all Data blocks in the DataSet, to find the Data blocks
   *  for the specified grid, as determined by the first segment id for
   *  this grid and the number of segments for this grid.
   *  NOTE: This method assumes that all pixels contributing to one
   *  Data block come from the same detector. 
   */
  private static void AssignGridForPixels( DataSet     ds, 
                                           UniformGrid grid,
                                           int         first_seg_id,
                                           int         num_segs_in_det )
  {
     int n_cols = grid.num_cols();

     for ( int i = 0; i < ds.getNum_entries(); i++ )
     {
       IData d = ds.getData_entry(i);
       PixelInfoList pil = AttrUtil.getPixelInfoList( d );
       if ( pil != null )
       {
         IPixelInfo pixel = pil.pixel(0);        // check first pixel, if it is
         int pixel_id = pixel.ID();              // from this detector, assume
         if ( pixel_id >= first_seg_id &&        // other pixeis in pil do too.
              pixel_id < first_seg_id + num_segs_in_det ) 
         {
           IPixelInfo new_pix_arr[] = new IPixelInfo[ pil.num_pixels() ]; 
           for ( int k = 0; k < new_pix_arr.length; k++ )
           {
             pixel = pil.pixel(k);
             pixel_id = pixel.ID();
             short row = (short)((pixel_id - first_seg_id) / n_cols + 1);
             short col = (short)((pixel_id - first_seg_id) % n_cols + 1);
             DetectorPixelInfo new_pixel = 
                              new DetectorPixelInfo( pixel_id, row, col, grid );
             new_pix_arr[k] = new_pixel;

             if ( row == 5 )                     // set grid position based on
             {                                   // the pixel in row 5, 
                                                 // projected onto the x,y plane
               DetectorPosition pos = AttrUtil.getDetectorPosition( d );
               Vector3D center = new Vector3D(pos);
               float xyz[] = center.get();
               xyz[2] = 0;
               
               grid.setCenter( center );
               Vector3D x_vec = new Vector3D( -xyz[1], xyz[0], 0 );
               Vector3D y_vec = new Vector3D( 0, 0, 1 );
               grid.setOrientation( x_vec, y_vec );
             }
           }
           pil      = new PixelInfoList( new_pix_arr );
           PixelInfoListAttribute pil_attr = 
                   new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST, pil );
           d.setAttribute( pil_attr );
/*
  PROPER CALCULATON OF EFFECTIVE POSITIONS, STILL NEEDS WORK.

           Attribute attr = new DetPosAttribute( 
                                    Attribute.DETECTOR_POS,
                                    pil.effective_position() );
           d.setAttribute( attr );
*/
           if ( debug_glad )
             if ( grid.ID() >= 205 && grid.ID() <= 209 )
             {
               System.out.println("CHECK GRID_ID = " + grid.ID() );
               for ( int k = 0; k < new_pix_arr.length; k++ )
                 System.out.println( new_pix_arr[k] );
             }
         }
       }
     }
  }

  
  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
                                              // load the file as best we can
    String path      ="/home/dennis/WORK/ISAW/LANSCE/SMARTS/";
    String datapath  ="/usr2/LANSCE_DATA/smarts/";
    String file_name =datapath + "/SMARTS_E2005004_R020983.nx.hdf";
    Retriever retriever;
    DataSet   ds;
/*
    retriever = new NexusRetriever( file_name );
    ds = retriever.getDataSet(2);
                                              // fix the data 
    LoadDetectorInfo( ds, path+"smarts_detectors.dat" );

    new ViewManager( ds, "Image View" );
//  new ViewManager( ds, "3D View" );
*/
    path = "/usr2/ARGONNE_DATA/";
    file_name = "glad6942.run";
    retriever = new RunfileRetriever( path + file_name );
    ds        = retriever.getDataSet(1); 

    path = "/home/dennis/WORK/ISAW/Databases/";
    file_name = "gladdets6.par";
    
    Load_GLAD_LPSD_Info( ds, path+file_name );

    new ViewManager( ds, "3D View" );
  }
}
