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
  public static DataSet LoadDetectorInfo( DataSet ds, String file_name )
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
    return ds;
  }


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
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
                                              // load the file as best we can
    String path      ="/home/dennis/WORK/ISAW/LANSCE/SMARTS/";
    String datapath  ="/usr2/LANSCE_DATA/smarts/";
    String file_name =datapath + "/SMARTS_E2005004_R020983.nx.hdf";
    Retriever retriever = new NexusRetriever( file_name );
    DataSet ds = retriever.getDataSet(2);

                                              // fix the data 
    ds = LoadDetectorInfo( ds, path+"smarts_detectors.dat" );

    new ViewManager( ds, "Image View" );
//  new ViewManager( ds, "3D View" );
  }
}
