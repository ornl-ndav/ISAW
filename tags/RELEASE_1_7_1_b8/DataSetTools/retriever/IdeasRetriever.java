/*
 * File: IdeasRetriever.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.7  2004/05/10 20:42:21  dennis
 *  Test program now just instantiates a ViewManager to diplay
 *  calculated DataSet, rather than keeping a reference to it.
 *  This removes an Eclipse warning about a local variable that is
 *  not read.
 *
 *  Revision 1.6  2004/03/19 17:22:05  dennis
 *  Removed unused variable(s)
 *
 *  Revision 1.5  2004/03/15 06:10:51  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.4  2004/03/15 03:28:42  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.3  2003/10/17 02:49:45  bouzekc
 *  Fixed javadoc errors and updated javadocs.
 *
 *  Revision 1.2  2003/08/28 17:26:50  dennis
 *  First complete version.  This now produces DataSets, with the attributes
 *  needed for the SCD analysis routines.  The input file MUST be a concatenation
 *  of a specific type of file from the Ideas MC Simulation package, with a
 *  header added that includes information about the detector position and size.
 *  (Some of the information is NOT present in the raw files, and some is added
 *  to the header as a convenience.)
 *
 *  Revision 1.1  2003/08/27 22:32:30  dennis
 *  Initial, incomplete form of reader for the specific type
 *  of file output from the IDEAS MC simulation codes for the
 *  proposed protein crystallography instrument for the SNS.
 *
 */

package DataSetTools.retriever;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Numeric.*;

import java.io.*;
import java.util.Vector;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import DataSetTools.viewer.*;

/**
 * Class that retrieves IDEAS files and produces ISAW DataSets.
 */

public class IdeasRetriever extends Retriever{

  public  static String   IDEAS_MC = "IDEAS MC";

  private static boolean  debug    = true;

  private int     numDataSets = 0;
  private DataSet data_set    = null;

  /* ------------------------ Constructor -------------------------- */
  /**
   * Construct the retriever for the specified file name.
   *
   * @param filename   The fully qualified file name of the data source.
   */
  public IdeasRetriever( String filename )
  {
     super(filename);
     TextFileReader tfr = null;
     try
     {                             // make sure the file exists and loads ok 
        tfr = new TextFileReader(filename);

                                         // read in and check the header line 
        String line = tfr.read_line();
        if ( !line.startsWith( IDEAS_MC ) ) 
        {
          System.out.println("Specified file does not look like a ");
          System.out.println("concatenated IDEAS MC simulation file" );
          throw new InstantiationError("Wrong kind of file");
        }
                                                   // Skip Comments
        line = tfr.read_line();
        while ( line.trim().startsWith("//") )
          line = tfr.read_line();
        tfr.unread();

        float l1     = ReadValue( tfr );
        float detd   = ReadValue( tfr );
        float deta   = ReadValue( tfr );
        float detw   = ReadValue( tfr );
        float deth   = ReadValue( tfr );
        int   n_cols = (int)ReadValue( tfr );
        int   n_rows = (int)ReadValue( tfr );

        double angle = Math.PI * deta / 180.0;
        System.out.println("angle = " + angle );
        Vector3D center = new Vector3D( (float)(detd * Math.cos( angle )),
                                        (float)(detd * Math.sin( angle )),
                                         0 );
        Vector3D y_vec = new Vector3D( 0, 0, 1 );
        Vector3D x_vec = new Vector3D();
        x_vec.cross( center, y_vec );
        x_vec.normalize(); 
        UniformGrid grid = new UniformGrid( 1, "m", 
                                            center, x_vec, y_vec, 
                                            detw, deth, 0.001f,
                                            (int)n_rows, (int)n_cols );
        if ( debug )
          System.out.println("grid = " + grid );

        Vector time_ranges = new Vector();
        Vector slices      = new Vector();
        ClosedInterval time_range = GetTimeRange( tfr );
        while ( time_range != null )
        {
          float slice[][] = GetSlice( tfr, n_rows, n_cols );
          time_ranges.add( time_range );
          slices.add( slice );
          time_range = GetTimeRange( tfr );
        }

        if ( debug )
        {
          System.out.println("Read " + slices.size() + " slices");
          for ( int i = 0; i < time_ranges.size(); i++ )
          {
            ClosedInterval interval = (ClosedInterval)time_ranges.elementAt(i);
            System.out.print("from " + interval.getStart_x() + 
                             " to "  + interval.getEnd_x() );
            float slice[][] = (float[][])slices.elementAt(i);
            System.out.println( " " + slice.length + 
                                " by " + slice[0].length );
          }
        }

        data_set = BuildDataSet( time_ranges, slices, grid, l1, detd, deta );
        if ( data_set != null )
          numDataSets = 1; 
     }
     catch ( Throwable th )
     {
       System.out.println("Could not load the file " + filename );
       th.printStackTrace();
       throw new InstantiationError("Wrong kind of file");
     }
  }


  /* ------------------------ numDataSets -------------------------- */
  /**
   * Get the number of distinct DataSets that can be obtained from the
   * current data source.
   *
   *  @return The number of distinct DataSets available.  This function
   *          may return values such as 0 as an error code if there are no
   *          DataSets available.
   */
  public int numDataSets()
  {
     return numDataSets;
  }

    
  /* -------------------------- getDataSet ---------------------------- */
  /**
   * Get the specified DataSet from the current data source.
   *
   * @param data_set_num  The number of the DataSet in this runfile
   *                      that is to be read from the runfile.  data_set_num
   *                      must be between 0 and numDataSets()-1
   *
   * @return The specified DataSet, if it exists, or null if no such
   *         DataSet exists.
   */
  public DataSet getDataSet( int data_set_num )
  {
    if( data_set_num == 0 && numDataSets == 1 )
      return data_set;
    else
      return null;
  }


  /* ---------------------------- getType ------------------------------ */
  /**
   *  Get the type code of a particular DataSet in this runfile.
   *  The type codes include:
   *
   *     Retriever.INVALID_DATA_SET
   *     Retriever.MONITOR_DATA_SET
   *     Retriever.HISTOGRAM_DATA_SET
   *     Retriever.PULSE_HEIGHT_DATA_SET
   *
   *  @param  data_set_num  The number of the DataSet in this runfile whose
   *                        type code is needed.  data_set_num must be between
   *                        0 and numDataSets()-1
   *
   *  @return the type code for the specified DataSet.
   */
  public int getType( int data_set_num )
  {
     if( data_set_num == 0 && numDataSets == 1 && data_set != null )
       return HISTOGRAM_DATA_SET;
     else
       return INVALID_DATA_SET;
  }


  /* ----------------------------- ReadValue --------------------------- */
  /**
   *  Get the numeric value for a line of the type:  NAME = VALUE ....
   *
   *  @param tfr The TextFileReader to use for reading.
   *
   *  @return The numeric value for a line of NAME = VALUE...
   *
   *  @throws IOException if the TextFileReader encounters a read error.
   */
  private float ReadValue( TextFileReader tfr ) throws IOException
  {
     String value_name = tfr.read_String();
     float  value      = tfr.read_float();
     tfr.read_line();
     System.out.println( value_name + " = " + value );
     return value;
  }


  /* --------------------------- GetTimeRange --------------------------- */
  /**
   *  Get the time range for the next slice in the file.
   *
   *  @param tfr The TextFileReader to use for reading.
   *
   *  @return The time range (in microseconds) for the next slice in the file, 
   *  null if EOF is reached.
   *
   *  @throws IOException if the TextFileReader encounters a read error.
   */
  private ClosedInterval GetTimeRange( TextFileReader tfr ) throws IOException
  {
     String line = tfr.read_line();
     while ( ! line.startsWith("Z-axis=time-of-flight") && !tfr.eof() )
       line = tfr.read_line();

     if ( tfr.eof() )
       return null;

     int index = line.indexOf( "Range=(" );
     if ( index < 0 )
       return null;
 
     line = line.substring( index + 7 ); 
     index = line.indexOf( "," );
     
     String val_1_str = line.substring( 0, index );
     if ( debug )
       System.out.println( "Time range start: " + val_1_str );

     line = line.substring( index+1 );
     index = line.indexOf( ")" );
  
     String val_2_str = line.substring( 0, index );
     if ( debug )
       System.out.println( "Time range end:   " + val_2_str );

                                          // get time-of-flight in microseconds
     float val_1 = 1000000 * Float.parseFloat( val_1_str );
     float val_2 = 1000000 * Float.parseFloat( val_2_str );
     return new ClosedInterval( val_1, val_2 );
  }


  /* ------------------------------- GetSlice --------------------------- */
  /**
   *  DOCUMENT ME!.
   *
   *  @param tfr The TextFileReader to use for reading.
   *  @param n_rows DOCUMENT ME!.
   *  @param n_cols DOCUMENT ME!.
   *
   *  @return DOCUMENT ME! 
   *
   *  @throws IOException if the TextFileReader encounters a read error.
   */
  private float[][] GetSlice( TextFileReader tfr, int n_rows, int n_cols ) 
                                                            throws IOException
  {
    String line = tfr.read_line();
    while ( ! line.startsWith("----------------------") && !tfr.eof() )
      line = tfr.read_line();
    
    if ( tfr.eof() )
      return null;

    float slice[][] = new float[n_rows][n_cols];

    for ( int row = 0; row < n_rows; row++ )
    {
       for ( int i = 0; i < 8; i++ )              // skip over the axis info
         tfr.read_float();

       for ( int col = 0; col < n_cols; col++ )
         slice[row][col] = tfr.read_float();
    }
    
    return slice;
  }


  /* ----------------------------- BuildDataSet --------------------------- */
  /**
   *  Build a data set from the vector of time ranges, slice data and 
   *  data grid.
   *
   *  @param time_ranges The Vector of time ranges to use.
   *  @param slices The Vector of slice data to use.
   *  @param grid The data grid to use.
   *  @param initial_path DOCUMENT ME!
   *  @param detd Detector distance.
   *  @param deta Detector angle.
   *  
   */
  private DataSet BuildDataSet( Vector      time_ranges, 
                                Vector      slices, 
                                UniformGrid grid,
                                float       initial_path,
                                float       detd,
                                float       deta   )
  {
    DataSetFactory dsf = new DataSetFactory( "Simulated Data" );
    DataSet ds = dsf.getTofDataSet( InstrumentType.TOF_SCD );

    if ( slices.size() <= 0 || slices.size() != time_ranges.size() )
    {
       System.out.println("Error reading time ranges and slices: sizes wrong");
       System.out.println("Number of slices     = " + slices.size() );
       System.out.println("Number of tof ranges = " + time_ranges.size() );
       return null;
    }

    int n_slices = slices.size();                    // put all slices into
    int n_rows   = grid.num_rows();                  // a 3D array
    int n_cols   = grid.num_rows();
    float vol_data[][][] = new float[n_slices][][];
    for ( int i = 0; i < n_slices; i++ )
      vol_data[i] = (float[][])slices.elementAt(i);

    ClosedInterval interval = null;                  // extract the x-scale
    float xs[] = new float[ n_slices + 1 ];
    for ( int i = 0; i < n_slices; i++ )
    {
      interval = (ClosedInterval)time_ranges.elementAt(i);
      xs[i] = interval.getStart_x();
    }
    xs[n_slices] = interval.getEnd_x();
    VariableXScale xscale = new VariableXScale( xs );

    Attribute initial_path_attr = new FloatAttribute( Attribute.INITIAL_PATH,
                                                      initial_path );
    Attribute detd_attr = new FloatAttribute( Attribute.DETECTOR_CEN_DISTANCE,
                                              detd );
    Attribute deta_attr = new FloatAttribute( Attribute.DETECTOR_CEN_ANGLE,
                                              deta );
    SampleOrientation orient = new IPNS_SCD_SampleOrientation( 0, 0, 0 );
    Attribute orient_attr = new SampleOrientationAttribute
                                ( Attribute.SAMPLE_ORIENTATION, orient ); 
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
      {
        float ys[] = new float[n_slices];
        for ( int i = 0; i < n_slices; i++ )
          ys[i] = vol_data[i][row][col];

        int id = row * n_cols + col + 1;
        Data d = new HistogramTable( xscale, ys, id );

        IPixelInfo  pixel = new DetectorPixelInfo( id, 
                                                  (short)(row+1), 
                                                  (short)(col+1), 
                                                  grid );
        PixelInfoList          pil      = new PixelInfoList( pixel );
        PixelInfoListAttribute pil_attr = 
                     new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST,pil);

        d.setAttribute( pil_attr );
        d.setAttribute( initial_path_attr );
        d.setAttribute( detd_attr );
        d.setAttribute( deta_attr );
        d.setAttribute( orient_attr );
        
        ds.addData_entry( d );
      }
    
    ds.setAttribute( initial_path_attr );
    ds.setAttribute( detd_attr );
    ds.setAttribute( deta_attr );
    ds.setAttribute( orient_attr );
    grid.setData_entries( ds );
    Grid_util.setEffectivePositions( ds, grid.ID() );        

    return ds;
  }


  /**
   * Main method for testing purposes only.
   */
  public static void main(String args[])
  {
    if ( args.length != 1 )
    {
      System.out.print("You must enter the name of a IDEAS MC simulation ");
      System.out.println("file");
      System.exit(0);
    }
    String filename = args[0];

    System.out.println( "FILE: " + filename );
    IdeasRetriever sim_ret = new IdeasRetriever( filename );
    System.out.println( "NUMDS: "+ sim_ret.numDataSets() );

    DataSet ds = sim_ret.getDataSet(0);
    new ViewManager( ds, ViewManager.IMAGE );    
  }
}
