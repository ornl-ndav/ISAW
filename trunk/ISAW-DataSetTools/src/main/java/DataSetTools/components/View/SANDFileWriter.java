/*
 * File:  SANDFileWriter.java
 *
 * Copyright (C) 2004, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.5  2004/07/10 06:37:27  millermi
 * - Added public static class FileInfo, to allow the four fields
 *   required to be put in a wrapper.
 * - Added capability for multiple selections to be written in
 *   one file.
 *
 * Revision 1.4  2004/03/15 03:27:28  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.3  2004/03/09 22:01:57  millermi
 * - Format numbers so they output in nice columns.
 * - Added column titles to describe each column.
 *
 * Revision 1.2  2004/01/29 18:16:22  dennis
 * Fixed javadoc error.
 *
 * Revision 1.1  2004/01/24 02:27:01  millermi
 * - Initial Version - This class writes the results from
 *   the SANDWedgeViewer to a file. The file has three
 *   columns: Q, Intensity, and Error bounds.
 * - Additional information is attached to the top of
 *   the file via the header parameter.
 *
 */ 
package DataSetTools.components.View;

import gov.anl.ipns.Util.File.TextWriter;
import gov.anl.ipns.Util.Numeric.Format;
import DataSetTools.dataset.Data;
import DataSetTools.util.SharedData;

/**
 * This class will write out the results of the SANDWedgeViewer to a file.
 * The file has three columns: Q, Intensity, and Error, IN THAT ORDER.
 */
public class SANDFileWriter
{
 /*
  * This constructor cannot be called by the user, it is called by
  * static methods of the class.
  */
  private SANDFileWriter( String filename, FileInfo[] fi )
  {
    String  header;
    float[] q;
    float[] intensity;
    float[] error;
    
    StringBuffer output = new StringBuffer();
    for( int fi_index = 0; fi_index < fi.length; fi_index++ )
    {
      header = fi[fi_index].getHeader();
      q = fi[fi_index].getQ();
      intensity = fi[fi_index].getIntensity();
      error = fi[fi_index].getError();
      
      // if the header is valid, add it to the output with one line of space.
      if( header != null && !header.equals("") )
        output.append( header );
      // If the header does not go to the next line, add a newline char for it.
      if( !header.endsWith("\n") )
        output.append('\n');
      // Add labels to the columns
      output.append("#          Q      Intensity          Error\n");
      output.append('\n');
      //make sure arrays are of same size, if not, use length of smallest array.
      int length = q.length;
      if( length > intensity.length )
        length = intensity.length;
      if( length > error.length )
        length = error.length;
      // create file with 3 columns: Q, Intensity, Error
      // Columns are separated by a 3 space delimiter.
      String DELIM = "   "; // 3 space delimiter.
      int COLUMN_WIDTH = 12; //width of each column of numbers, excluding delim.
      for( int i = 0; i < length; i++ )
      {
        // Q
        output.append(Format.singleExp( (double)q[i], 
        				COLUMN_WIDTH ) ).append(DELIM);
        // Intensity
        output.append(Format.singleExp( (double)intensity[i],
        				COLUMN_WIDTH ) ).append(DELIM);
        // Error
        output.append(Format.singleExp( (double)error[i],
        				COLUMN_WIDTH ) ).append('\n');
      }
      
      // As long as it's not the last selection, add a spacer line.
      if( fi_index != (fi.length-1) )
        output.append('\n');
    }
    TextWriter.writeASCII( filename, output.toString() );
  }
 
 /**
  * Use this method to make a 200 x 200 file.
  *
  *  @param  filename Includes path
  *  @param  header   Additional information placed at the top of the file.
  *  @param  data     Data block that contains the Q, Intensity, 
  *                   and Error values.
  */ 
  public static void makeFile( String filename, String header, Data data )
  {
    FileInfo[] info = new FileInfo[1];
    info[0] = new FileInfo( header, data.getX_scale().getXs(),
                            data.getY_values(), data.getErrors() );
    new SANDFileWriter( filename, info );
  }
 
 /**
  * Use this method to create a file of a specified size.
  *
  *  @param  filename  Includes path
  *  @param  header    Additional information placed at the top of the file.
  *  @param  q         Array of Q values
  *  @param  intensity Array of intensity values
  *  @param  error     Array of error values
  */
  public static void makeFile( String filename, String header, float[] q,
                               float[] intensity, float[] error )
  {
    FileInfo[] info = new FileInfo[1];
    info[0] = new FileInfo( header, q, intensity, error );
    new SANDFileWriter( filename, info );
  }
 
 /**
  * Use this method to create a file of a specified size.
  *
  *  @param  filename  Includes path
  *  @param  fi FileInfo containing header, q, intensity, and errors.
  */
  public static void makeFile( String filename, FileInfo fi )
  {
    FileInfo[] info = new FileInfo[1];
    info[0] = fi;
    new SANDFileWriter( filename, info );
  }
 
 /**
  * Use this method to create a file of a specified size.
  *
  *  @param  filename  Includes path
  *  @param  fi List of FileInfo, each containing header, q, intensity,
  *             and errors.
  */
  public static void makeFile( String filename, FileInfo[] fi )
  {
    new SANDFileWriter( filename, fi );
  }
  
 /**
  * This static class was added so multiple selections could be written to
  * the same file. By using this as a wrapper, multiple array groups can be
  * passed.
  */ 
  public static class FileInfo
  {
    private String header;
    private float[] q;
    private float[] intensity;
    private float[] error;
    
    public FileInfo( String header, float[] q, float[] intensity, float[] err )
    {
      this.header = header;
      this.q = q;
      this.intensity = intensity;
      this.error = err;
    }
    
    public String getHeader() { return header; }
    
    public float[] getQ() { return q; }
    
    public float[] getIntensity() { return intensity; }
    
    public float[] getError() { return error; }
  }
 
 /**
  * For use in testing purposes only.
  *
  *  @param  args The filename, rows, and columns, IF SPECIFIED
  */ 
  public static void main( String args[] )
  {
    String filepath = SharedData.getProperty("Data_Directory");
    String separator = System.getProperty("file.separator");
    if( !filepath.endsWith(separator) )
      filepath = filepath + separator;
    String filename = "wegtest.dat";
    if( args.length > 0 )
    {
      filename = args[0];
    }
    String head = "This is a test output file for the SANDWedgeViewer";
    float[] q = {.1f,.2f,.3f,.4f,.5f,.9f,.8f,.7f,.6f};
    float[] intensity = {.01f,.02f,.03f,.04f,.05f,.09f,.08f,.07f,.06f};
    float[] error = {.001f,.002f,.003f,.004f,.005f,.009f,.008f,.007f,.006f};
    
    String head2 = "This is another test output file for the SANDWedgeViewer";
    float[] q2 = {.1f,.2f,.3f,.4f,.5f,.9f,.8f,.7f,.6f};
    float[] intensity2 = {.01f,.02f,.03f,.04f,.05f,.09f,.08f,.07f,.06f};
    float[] error2 = {.001f,.002f,.003f,.004f,.005f,.009f,.008f,.007f,.006f};
    
    String head3 = "This is a third test output file for the SANDWedgeViewer";
    float[] q3 = {.1f,.2f,.3f,.4f,.5f,.9f,.8f,.7f,.6f};
    float[] intensity3 = {.01f,.02f,.03f,.04f,.05f,.09f,.08f,.07f,.06f};
    float[] error3 = {.001f,.002f,.003f,.004f,.005f,.009f,.008f,.007f,.006f};
    
    SANDFileWriter.FileInfo[] info = new SANDFileWriter.FileInfo[3];
    info[0] = new SANDFileWriter.FileInfo(head,q,intensity,error);
    info[1] = new SANDFileWriter.FileInfo(head2,q2,intensity2,error2);
    info[2] = new SANDFileWriter.FileInfo(head3,q3,intensity3,error3);
    
    String file = filepath + filename;
    SANDFileWriter.makeFile( file, info );

    System.out.println("***File [" + filename + "] saved in " + 
    		       SharedData.getProperty("Data_Directory") + "***" );
  }
}
