/* 
 * File: FileUtil.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  $Author:$
 *  $Date:$            
 *  $Revision:$
 */

package EventTools.EventList;

import java.io.*;
import java.util.*;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;


/**
 * This class contains static methods to read files containing information
 * about SNS detectors.
 */
public class FileUtil 
{
  private static int BUFFER_SIZE = 32768;


  /**
   *  Load the specified .DetCal file and return a Vector containing the 
   *  array of grids, L1 and T0 values.  The array of grids is sorted based
   *  on the grid ID.
   *
   *  @param det_cal_file_name  Fully qualified name of a .DetCal file 
   *                            containing the geometric information 
   *                            about the detector grids.
   *
   *  @return A vector with three elements, the sorted array of detector grids,
   *          L1 and T0, respectively.
   */
  public static Vector LoadDetCal( String det_cal_file_name ) throws IOException
  {
                                                  // Bring in the grids
     FileReader     f_in        = new FileReader( det_cal_file_name );
     BufferedReader buff_reader = new BufferedReader( f_in );
     Scanner        sc          = new Scanner( buff_reader );

     String version_title = sc.next();
     while ( version_title.startsWith("#") )      // Skip any comment lines
     {                                            // and the version info
       sc.nextLine();
       version_title = sc.next();
     }

     float[]   L1_t0 = Peak_new_IO.Read_L1_T0( sc );
     Hashtable grids = Peak_new_IO.Read_Grids( sc );
     sc.close();

     float L1 = L1_t0[0];
     float t0 = L1_t0[1]; 

                                                  // Sort the grids on ID
     Object[] obj_arr = (grids.values()).toArray();

     IDataGrid[] grid_arr = new IDataGrid[ obj_arr.length ];
     for ( int i = 0; i < obj_arr.length; i++ )
       grid_arr[i] = (IDataGrid)obj_arr[i];

     Arrays.sort( grid_arr, new GridID_Comparator() );

     Vector result = new Vector();
     result.add( grid_arr );
     result.add( new Float(L1) );
     result.add( new Float(t0) );

     return result;
  }


  public static class  GridID_Comparator implements Comparator
  {
   /**
    *  Compare two IDataGrid objects based on their IDs.
    *
    *  @param  obj_1   The first grid 
    *  @param  obj_2   The second grid 
    *
    *  @return An integer indicating whether grid_1's ID is greater than,
    *          equal to or less than grid_2's ID.
    */
    public int compare( Object obj_1, Object obj_2 )
    {
      int id_1 = ((IDataGrid)obj_1).ID();
      int id_2 = ((IDataGrid)obj_2).ID();

      if ( id_1 > id_2 )
        return 1;
      else if ( id_1 == id_2 )
        return 0;
      else
        return -1;
    }
  }


  /**
   *  Convenience method to get the first String value with a particular
   *  tag name in an XML document element. 
   */
  private static String getText( Element element, String name )
  {
    NodeList node_list = element.getElementsByTagName( name );
    NodeList element_list  = ((Element)node_list.item(0)).getChildNodes();
    return ( element_list.item(0)).getNodeValue();
  }

  /**
   *  Return the banking information from the specified SNS bank file in
   *  a two dimensional array of ints, bank_data[][].  The array will have
   *  five rows with the kth entry in each row storing the information about
   *  the kth bank listed in the file in the following format:
   *  bank_data[0][k] -- bank ID of the kth bank
   *  bank_data[1][k] -- "x_size" (number of columns) in the kth bank 
   *  bank_data[2][k] -- "y_size" (number of rows) in the kth bank
   *  bank_data[3][k] -- first id used for pixels in kth bank
   *  bank_data[4][k] -- last id used for pixels in kth bank
   *  NOTE: Pixel numbers for the bank increase in steps of one, increasing
   *        along COLUMNS of the detector, bottom to top, left to right.
   *        The id of the pixel at the lower left corner(1,1) is the first
   *        id used.  "lower left" is in terms of the view of the detector
   *        from the sample, with the observer's frame rotated so the detector
   *        "x" increased to the right and "y" increases upward relative to the
   *        observer.
   *
   * @param bank_file_name  The fully qualified name of the XML file containing
   *                        the banking information in the form currently used
   *                        at the SNS. (2/15/2010).
   *
   * @return a two-dimensional array of ints holding the bank information.
   */
  public static int[][] LoadBankFile( String bank_file_name )
  {
    int[][] bank_data = new int[0][0];

    try
    {
      File file = new File(bank_file_name);

      DocumentBuilder loader =
                      DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = loader.parse(file);
      doc.getDocumentElement().normalize();

      Element root = doc.getDocumentElement();

      NodeList bank_list = doc.getElementsByTagName("bank");

      int num_banks = bank_list.getLength();

      bank_data = new int[5][num_banks];

      for (int i = 0; i < num_banks; i++)
      {
        Node bank_node = bank_list.item(i);

        if (bank_node.getNodeType() == Node.ELEMENT_NODE)
        {
          Element bank = (Element)bank_node;

          int bank_id = Integer.parseInt( getText( bank, "number" ) );
          int x_size  = Integer.parseInt( getText( bank, "x_size" ) );
          int y_size  = Integer.parseInt( getText( bank, "y_size" ) );

          NodeList index_list = bank.getElementsByTagName("continuous_list");

          Element indices = (Element)index_list.item(0);
          int start_id = Integer.parseInt( getText( indices, "start" ) );
          int stop_id  = Integer.parseInt( getText( indices, "stop"  ) );

          bank_data[0][i] = bank_id;
          bank_data[1][i] = x_size;
          bank_data[2][i] = y_size;
          bank_data[3][i] = start_id;
          bank_data[4][i] = stop_id;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return bank_data;
  }


  /**
   * Convenience method to print out the banking information from an SNS
   * detector bank XML file.
   *
   * @param bank_file_name  The fully qualified name of the XML file containing
   *                        the banking information in the form currently used
   *                        at the SNS. (2/15/2010).
   */
  public static void ShowBankFile( String bank_file_name )
  {
    try
    {
      File file = new File(bank_file_name);

      DocumentBuilder loader =
                      DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = loader.parse(file);
      doc.getDocumentElement().normalize();

      Element root = doc.getDocumentElement();
      System.out.println("Root element " + root.getNodeName());

      System.out.println( "Facility   = "+getText(root,"facility") );
      System.out.println( "Instrument = "+getText(root,"instrument") );
      System.out.println( "Date       = "+getText(root,"date") );

      NodeList bank_list = doc.getElementsByTagName("bank");
      for (int i = 0; i < bank_list.getLength(); i++)
      {
        Node bank_node = bank_list.item(i);

        if (bank_node.getNodeType() == Node.ELEMENT_NODE)
        {
          Element bank = (Element)bank_node;

          System.out.printf("BANK ID = %4s  ", getText( bank, "number" ) );
          System.out.printf("size = %4s X %4s ", 
                             getText( bank, "x_size" ),
                             getText( bank, "y_size" ) );

          NodeList index_list = bank.getElementsByTagName("continuous_list");

          Element indices = (Element)index_list.item(0);
          System.out.printf  (" id range = %7s : %7s\n",
                                getText( indices, "start" ),
                                getText( indices, "stop" ) );
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }


  /**
   *  Get an array of integer values from a file of bytes.  The number of
   *  integers returned in the array is the integer part of filesize/4.
   *
   *  @param filename  The name of the file of binary integers to read
   *
   *  @return An array containing integer values obtained by converting
   *          sequences of four bytes into the corresponding integer value.
   */
  public static int[] LoadIntFile( String filename )
  {
    try
    {
      RandomAccessFile r_file = new RandomAccessFile( filename, "r" );

      long num_bytes = r_file.length();

      if ( num_bytes > 4l * (long)(Integer.MAX_VALUE) )
        throw new IllegalArgumentException("File has more than " +
                       Integer.MAX_VALUE + 
                     " integer values and can't be loaded in array in array" );

      int   num_ints = (int)(num_bytes/4);
      int[] list     = new int[ num_ints ];

      r_file.seek( 0 );
      long num_loaded = 0;
      long seg_size = BUFFER_SIZE;
      long bytes_read;
      byte[] buffer    = new byte[ BUFFER_SIZE ];
      while ( num_loaded < num_ints )
      {
        bytes_read = r_file.read( buffer );
        seg_size   = Math.min( seg_size, bytes_read );

        seg_size   = (seg_size/4) * 4;      // make sure it's a multiple of 4

        if ( bytes_read > 0 )
        {
          UnpackBuffer( buffer, seg_size, num_loaded, list );
          num_loaded += bytes_read/4;
        }
        else
        {
          System.out.println("ERROR: Unexpected end of integer file: " +
                              filename + 
                             " after reading " + num_loaded +
                             " out of " + num_ints +
                             " events." );
          num_loaded = num_ints;
        }
      }
      r_file.close();
      return list;
    }
    catch ( IOException ex )
    {
      throw new IllegalArgumentException("Failed to load events from file " +
                                          filename );
    }
  }


  /**
   *  Get the integer values stored in the input file buffer and put the
   *  values into the proper positions in the event[] array.  
   *
   *  @param buffer      The array of bytes as read in one segment from the
   *                     event data file.
   *  @param bytes_read  The number of bytes that were read in from the file
   *                     and are to be extracted and placed in the events[]
   *                     array.
   *  @param num_loaded  The number of events that have already been loaded. 
   *                     This provides the position where the first tof and
   *                     pixel id from the buffer should be stored.
   *  @param list        The list being filled with integers from the file.
   */
  public static void UnpackBuffer( byte[] buffer, 
                                   long   bytes_read, 
                                   long   num_loaded,
                                   int[]  list )
  {
    int index = (int)(num_loaded);
    for ( int i = 0; i < bytes_read; i += 4 )
      list[ index++ ] = SNS_TofEventList.getValue_32( buffer, i );
  }


  /**
   *  Get the table of (id,weight) pairs from the specified file.  The
   *  information is returned as two, two dimensional arrays in a Vector.  
   *  The first array is an NxM array of ints and the second array is an NxM
   *  array of doubles.  N is the number of detector pixels for which the
   *  ghost mapping table was made.  M is the number of affected pixels 
   *  for each event.  The kth row of the array of ints lists the DAS
   *  ID numbers of pixels that affected when an event is detected in 
   *  DAS pixel k.  The kth row of the array of doubles lists the fractional
   *  weight of the event to be added to the histogram for that DAS ID.
   *
   *  @param filename  The name of the binary file containing the ghost
   *                   mapping table.  The file MUST be stored in little
   *                   endian format as written by PYTHON on a PC.  This
   *                   is NOT the form of a JAVA binary file, so this 
   *                   method reverses the bytes before converting to
   *                   int and double values. 
   *
   *  @param n_ids     The number of ids listed in the file.  NOTE: the
   *                   file must contain information for all ids from
   *                   0 to n_ids-1.  The information for each id must
   *                   be n_ghosts pairs of (int,double) in that order.
   *
   *  @param n_ghosts  The number of DAS ids that are affected by each
   *                   event.  This corresponds to the number of "columns"
   *                   of ghost histograms in Jason Hodges Python code.
   *
   *  @return A vector containing a two dimensional array of ints in the
   *          first entry and a two-dimensional array of doubles in the
   *          second entry.  The arrays have the number of rows specified
   *          by n_ids and the number of columns specified by n_ghosts.
   */
  public static Vector LoadGhostMapFile( String filename, 
                                         int    n_ids,
                                         int    n_ghosts )
  {
    int bytes_per_record = n_ghosts * 12;       // assuming each pair
                                                // is a 4-byte int and
                                                // an 8-byte double
    File ghost_file = new File( filename );
    if ( !ghost_file.exists() )
      throw new IllegalArgumentException( filename + " does not exist.");

    long file_size = ghost_file.length();
    if ( file_size % bytes_per_record != 0 )
      throw new IllegalArgumentException( filename + " is not a ghost map.");

    if ( file_size < bytes_per_record * n_ghosts )
      throw new IllegalArgumentException( filename + " only has records for " +
                 file_size / bytes_per_record + " but needs records for " +
                 n_ids + " detectors" );

    byte[]     buffer  = new byte[(int)file_size];

    int   [][] ids     = new int   [n_ids][n_ghosts];
    double[][] weights = new double[n_ids][n_ghosts];

    try
    {
      RandomAccessFile r_file = new RandomAccessFile( filename, "r" );
      r_file.seek( 0 );
      long bytes_read = r_file.read( buffer );
      if ( bytes_read != file_size )
        throw new IllegalArgumentException( filename + " NOT read properly.");
    }
    catch ( Exception ex )
    {
      throw  new IllegalArgumentException("Error loading Ghosts: " +filename);
    }

    int index = 0;
    for ( int id = 0; id < ids.length; id++ )   
      for ( int ghost = 0; ghost < n_ghosts; ghost++ )
      {
        ids[ id ][ ghost ] = getInt_32( buffer, index );
        index += 4;
        
        weights[ id ][ ghost ] = getDouble_64( buffer, index );
        index += 8;
      }
    
    Vector result = new Vector();
    result.add( ids );
    result.add( weights );

    return result;
  }


  /**
   * Decode the double value stored in a sequence of eight bytes in 
   * the buffer.  The eight bytes determining the double value are 
   * stored in the file and buffer in the sequence: b0, ... b7, with 
   * the lowest order byte, b0, first and the the highest order byte, 
   * b7, last.
   * 
   * @param i  The index of the first byte in the buffer
   *                    
   * @return The double value represented by eight successive bytes from
   *         the file. 
   */
  public static double getDouble_64( byte[] buffer, int i )
  {
    long long_val = 0;

    for ( int shift = 0; shift < 64; shift += 8)
      long_val |= ((long)buffer[ i++ ] & 0xFF) << shift;  

    return Double.longBitsToDouble( long_val );
  }


  /**
   * Decode the integer value stored in a sequence of 
   * four bytes in the buffer.  The four bytes determining
   * the Integer value are stored in the file and buffer in the 
   * sequence: b0, b1, b2, b3, with the lowest order byte, b0, first
   * and the the highest order byte, b3, last.
   * 
   * @param i  The index of the first byte in the buffer
   *                    
   * @return The integer value represented by four successive bytes from
   *         the file. 
   */
  public static int getInt_32( byte[] buffer, int i )
  {
    int val = 0;

    i += 3;
                                   // NOTE: When the signed byte is
    val |= buffer[ i-- ] & 0xFF;   // converted to int, it is sign
    val <<= 8;                     // extended, so the $0xFF is
                                   // needed.
    val |= buffer[ i-- ] & 0xFF;
    val <<= 8;

    val |= buffer[ i-- ] & 0xFF;
    val <<= 8;

    val |= buffer[ i ] & 0xFF;
    return val;
   }


  /**
   * Check that the specified name is the name of a file that exists and
   * can be read by the user.  Throw an exception if the file can't be
   * read.
   *
   * @param filename
   *
   * @throws IllegalArgumentException if the file name is null, the 
   *         file doesn't exist, or the file can't be read. 
   */
  public static void CheckFile( String filename )
  {
    if ( filename == null )
      throw new IllegalArgumentException("Filename String is NULL");

    File file = new File( filename );

    if ( !file.exists() )
      throw new IllegalArgumentException("File doesn't exist: " + filename );

    if ( !file.canRead() )
      throw new IllegalArgumentException("File can't be read: " + filename );
  }


  /**
   *  main program providing basic test for this class
   */
  public static void main(String[] args)
  {
    String inst_name = "PG3";
    String info_dir  = "/home/dennis/SNS_ISAW/ISAW_ALL/InstrumentInfo/SNS/";
           info_dir += inst_name + "/";
    String map_file  = info_dir + inst_name +"_TS.dat";
    String bank_file = info_dir + inst_name +"_bank.xml";

    long start = System.nanoTime();
/*
    int[]   map = LoadIntFile( map_file );
    boolean all_match = true;
    int     num_dont = 0;
    for ( int i = 0; i < map.length; i++ )
      if ( i != map[i] )
      {
        all_match = false;
        num_dont++;
      }

    System.out.println("ALL MATCH = " + all_match );
    System.out.println("NUM NOT MATCHING = " + num_dont );
    System.out.println("FILE SIZE = " + map.length );

    for ( int i = 0; i < 20; i++ )
      System.out.println("i, map[i] = " + i + ", " + map[i] );
*/

/*
    int[] raw_ints = FileUtil.LoadIntFile( map_file );
    double time = (System.nanoTime() - start) / 1e6;
    System.out.printf( "\nMapping file load time = %6.3f ms\n", time );

    System.out.println( "\nNUMBER OF INTEGERS = " + raw_ints.length );

    for ( int i = 0; i < 20; i++ )
      System.out.printf( "i = %6d,  val = %6d\n", i, raw_ints[i] );

    System.out.println("\n TEXT FORM OF BANK DATA = " );
    ShowBankFile( bank_file );

    start = System.nanoTime();
    int[][] bank_data = LoadBankFile( bank_file );
    time = (System.nanoTime() - start) / 1e6;
    System.out.printf( "\nBank data load time = %6.3f ms\n", time );

    System.out.println("\n ARRAY RESULT FROM LoadBankFile = " );
    for ( int i = 0; i < bank_data[0].length; i++ )
    {
      System.out.printf("BANK ID = %4s  ", bank_data[0][i] );
      System.out.printf("size = %4s X %4s ", bank_data[1][i], bank_data[2][i] );
      System.out.printf(" id range = %7s : %7s\n",
                          bank_data[3][i], bank_data[4][i] );
    }
*/

    start = System.nanoTime();
    int n_ghosts = 16;
    int n_ids    = 300000;
    String ghost_file = info_dir + "PG3_GhostPks.dat";
    Vector ghost_info = LoadGhostMapFile( ghost_file, n_ids, n_ghosts ); 
    double time = (System.nanoTime() - start) / 1e6;
    System.out.printf( "\nGhost file load time = %6.3f ms\n", time );

    int[][] ids        = (int[][])(ghost_info.elementAt(0));
    double[][] weights = (double[][])(ghost_info.elementAt(1));

    for ( int i = 55600; i < 55630; i++ )
    {
      for ( int g = 0; g < 16; g++ )
        System.out.printf("%5d  %6.2e | ", ids[i][g], weights[i][g] );
      System.out.println();
    }   

  }

}
