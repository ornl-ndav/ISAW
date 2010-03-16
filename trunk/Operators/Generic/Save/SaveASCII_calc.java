/* 
 * File: SaveASCII_calc.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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


package Operators.Generic.Save;

import java.io.*;

import gov.anl.ipns.Util.Numeric.*;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;

/**
 *  This class contains static methods to save selected Data blocks from
 *  a DataSet in two or three column ASCII format.  Separate methods are
 *  included that allow the Data blocks that are to be saved, to be 
 *  specified by an array of integers, a String, or from the selected
 *  Data blocks in the DataSet.
 */ 
public class SaveASCII_calc 
{

   private SaveASCII_calc()
   {
   }

   /**
    *  Save the currently selected Data blocks from the DataSet to the 
    *  specified file.
    *
    *  @param ds              The DataSet from which Data blocks will be 
    *                         printed.
    *  @param include_errors  Flag indicating whether to write three columns,
    *                         x, y and error estimates for y, or just two
    *                         columns, x, y.
    *  @param format          C style format string desribing how the two or
    *                         three columns should be formatted, such as:
    *                         "%12.7E  %12.7E".  If null, or blank, the
    *                         values will be formatted using exponential
    *                         notation.  NOTE: The number of format specifiers
    *                         MUST correspond to the number of values printed,
    *                         (2 or 3).
    *  @param out_file_name   The fully qualified name of the file where the 
    *                         data should be written.
    */
   public static String SaveASCII( DataSet ds,
                                   boolean include_errors,
                                   String  format,
                                   String  out_file_name )
                        throws IOException
   {
      int[] indices = ds.getSelectedIndices();
      return SaveASCII( ds, indices, include_errors, format, out_file_name );
   }


   /**
    *  Save the specified Data blocks from the DataSet to the specified file.
    *
    *  @param ds              The DataSet from which Data blocks will be 
    *                         printed.
    *  @param indices         String specifying the indices of the Data blocks
    *                         that are to be written, such as "0:3,10,40" 
    *  @param include_errors  Flag indicating whether to write three columns,
    *                         x, y and error estimates for y, or just two
    *                         columns, x, y.
    *  @param format          C style format string desribing how the two or
    *                         three columns should be formatted, such as:
    *                         "%12.7E  %12.7E".  If null, or blank, the
    *                         values will be formatted using exponential
    *                         notation.  NOTE: The number of format specifiers
    *                         MUST correspond to the number of values printed,
    *                         (2 or 3).
    *  @param out_file_name   The fully qualified name of the file where the 
    *                         data should be written.
    */
   public static String SaveASCII( DataSet ds,
                                   String  indices,
                                   boolean include_errors,
                                   String  format,
                                   String  out_file_name )
                        throws IOException
   {
     int[] indices_to_print = IntList.ToArray( indices );
     if ( indices_to_print == null || indices_to_print.length < 1 )
       throw new IllegalArgumentException("ERROR: couldn't convert indices "
                                           + indices );

      return SaveASCII( ds, 
                        indices_to_print,
                        include_errors, 
                        format, 
                        out_file_name );
   }


   /**
    *  Save the specified Data blocks from the DataSet to the specified file.
    *
    *  @param ds              The DataSet from which Data blocks will be 
    *                         printed.
    *  @param indices_to_print  Array with the indices of the Data blocks
    *                           that are to be written.
    *  @param include_errors  Flag indicating whether to write three columns,
    *                         x, y and error estimates for y, or just two
    *                         columns, x, y.
    *  @param format          C style format string desribing how the two or
    *                         three columns should be formatted, such as:
    *                         "%12.7E  %12.7E".  If null, or blank, the
    *                         values will be formatted using exponential
    *                         notation.  NOTE: The number of format specifiers
    *                         MUST correspond to the number of values printed,
    *                         (2 or 3).
    *  @param out_file_name   The fully qualified name of the file where the 
    *                         data should be written.
    */
   public static String SaveASCII( DataSet ds,
                                   int[]   indices_to_print,
                                   boolean include_errors,
                                   String  format, 
                                   String  out_file_name )
                        throws IOException
   {
     if ( ds == null || ds.getNum_entries() <= 0 )
       throw new IllegalArgumentException("ERROR: null or empty DataSet" );

     if ( indices_to_print.length <= 0 )
       throw new IllegalArgumentException("ERROR: No Data blocks selected" );

     if ( out_file_name == null )
       throw new IllegalArgumentException("ERROR: null file name");

     for ( int index = 0; index < indices_to_print.length; index++ )
     {
       Data data = ds.getData_entry( indices_to_print[ index ] );

       if ( data == null )
         throw new IllegalArgumentException("ERROR: No Data with index: " +
                                             indices_to_print[ index ] );

       if ( include_errors && !data.isSqrtErrors() )   // check further to be
       {                                               // sure data blocks 
         float[] errs = data.getErrors();              // have error estimates
         if ( errs == null )
           throw new IllegalArgumentException(
                "ERROR: Data block at index " + indices_to_print[ index ] +
                " doesn't have error estimates" );
       }
     } 

     if ( format == null || format.trim().length() == 0 )
       if ( include_errors )   
         format = "%12.7E  %12.7E  %12.7E";
       else
         format = "%12.7E  %12.7E";
        
     PrintStream out = new PrintStream( out_file_name );
     for ( int index = 0; index < indices_to_print.length; index++ )
     {
       Data data = ds.getData_entry( indices_to_print[ index ] );

       float[] xs   = data.getX_scale().getXs();
       float[] ys   = data.getY_values();
       float[] errs = null;

       out.println("DataSet   : " + ds.toString() );
       out.println("Data Index: " + indices_to_print[index] );
       out.println("Group ID  : " + data.getGroup_ID() );
       out.println("X-Units:    " + ds.getX_units() );
       out.println("Y-Units:    " + ds.getY_units() );
       out.println("Number of X values: " + xs.length );
       out.println("Number of Y values: " + ys.length );

       if ( include_errors )
         errs = data.getErrors();
       for ( int i = 0; i < ys.length; i++ )
         if ( include_errors && errs != null )
         {
           out.printf( format, xs[i], ys[i], errs[i] );
           out.println(); 
         }
         else
         {
           out.printf( format, xs[i], ys[i] );
           out.println(); 
         }

       if ( xs.length > ys.length )              // histogram has one extra x
       {
          String last_format = format.trim();

          int space = last_format.indexOf(" ");
          if ( space > 0 )
            last_format = last_format.substring(0,space);

          out.printf( last_format, xs[xs.length-1] );
          out.println(); 
          out.println(); 
       }
       out.println();                            // leave blank line betwee
     }

     out.close();
     return "Wrote data to file " + out_file_name;
   }

   public static void main( String args[] )  throws Exception
   {
      RunfileRetriever rr = new RunfileRetriever("/usr2/SCD_TEST/scd08336.run");
      DataSet ds = rr.getDataSet(2);
      SaveASCII( ds, 
                 "5050:5552", 
                 true, 
                 "%7.1f  %7.0f  %7.4f", 
                 "TestSaveASCII.dat" );
   }

}
