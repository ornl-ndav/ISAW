/*
 * File:  DisplayOStream.java
 *
 * Copyright (C) 2003 Ruth Mikkelson
 *
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
 * number DMR-0218882.
 *
 * $Log$
 * Revision 1.1  2003/10/10 02:05:44  bouzekc
 * Added to CVS
 *
 */
package DataSetTools.util;

import java.io.*;


/**
 * Class to handle the Display information.  Works similarly to OutputStream
 * but sends its output to SharedData.
 */
public class DisplayOStream extends OutputStream {
  //~ Instance fields **********************************************************

  StringBuffer bytes = new StringBuffer(  );

  //~ Constructors *************************************************************

  /**
   * Creates a new DisplayOStream object.
   */
  public DisplayOStream(  ) {
    super(  );
  }

  //~ Methods ******************************************************************

  /**
   * "Writes" one byte to a buffer.
   *
   * @param b The byte to write to the buffer.
   */
  public void write( int b ) throws IOException {
    bytes.append( ( char )b );
  }

  /**
   * "Writes" an array of bytes and dumps it out.
   *
   * @param b The array of bytes to write.
   */
  public void write( byte[] b ) throws IOException {
    write( b, 0, b.length );
  }

  /**
   * "Writes" a subarray of bytes to a buffer, using an offset  and length.
   *
   * @param b Array of bytes to write.
   * @param off Offset to use.
   * @param len Length of subarray.
   */
  public void write( byte[] b, int off, int len ) throws IOException {
    if( off < 0 ) {
      off = 0;
    }

    if( ( off + len ) > b.length ) {
      len = b.length - off;
    }

    for( int i = off; i < ( off + len ); i++ ) {
      int c = ( int )( ( char )b[i] );

      write( c );
    }

    DataSetTools.util.SharedData.addmsg( bytes.toString(  ).trim(  ) );

    bytes = new StringBuffer(  );
  }
}
