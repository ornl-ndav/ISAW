/*
 * File:  CNexusFile.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.4  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/20 16:14:35  pfpeterson
 * reformating
 *
 * Revision 1.2  2002/06/19 15:01:47  rmikk
 * Added GPL and initial documentation
 * Eliminated extra dos returns
 *
 */

package NexIO;


import  neutron.nexus.*;
import ncsa.hdf.hdflib.HDFArray;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFConstants;

/** 
 * This class extends the NexusFile class to getData that is
 * multidimensional as a linear string. <P> This saves an immense
 * amount of time for retrieving this type of data
 */
public class CNexusFile extends NexusFile{

   /**
    *Constructor that just calls the super constructor
    */
  public CNexusFile( String filename, int access ) throws NexusException{
    super( filename, access );
  }

  /** 
   * The new getData that gets multidimensional data as a linear array
   *
   * @param  NxType  the type of data
   * @param  length   the length of the array
   * @return  the "linear" array of values
   */
  public Object getData( int NxType, int length ) throws NexusException{
    byte bdata[];

    if( handle < 0 ) 
      throw new NexusException( "NAPI-ERROR: File not open" );

    try{
      int L = -1;
      
      if( NxType == NexusFile.NX_FLOAT32 )
        L = 4;
      else if( NxType == NexusFile.NX_CHAR ) 
        L = 1;
      else if( NxType == NexusFile.NX_FLOAT64 )
        L = 8;
      else if( NxType == NexusFile.NX_INT16 )
        L = 4;
      else if( NxType == NexusFile.NX_INT32 )
        L = 8;
      else if( NxType == NexusFile.NX_INT8 )
        L = 2;
      else if( NxType == NexusFile.NX_UINT16 )
        L = 4;
      else if( NxType == NexusFile.NX_UINT32 )
        L = 8;
      else if( NxType == NexusFile.NX_UINT8 )
        L = 2;
      else 
        throw new NexusException( "NAPI-ERROR: Improper Data Type" );
      
      bdata = new byte[L * length];
      //HDFArray ha = new HDFArray(array);
      //bdata = ha.emptyBytes();
      
      nxgetdata( handle, bdata );
      
      //array = ha.arrayify(bdata);
      
      if( NxType == NexusFile.NX_FLOAT32 )
        return ncsa.hdf.hdflib.HDFNativeData.byteToFloat( bdata );
      else if( NxType == NexusFile.NX_FLOAT64 )
        return ncsa.hdf.hdflib.HDFNativeData.byteToDouble( bdata );
      else if( ( NxType == NexusFile.NX_INT16 ) || ( NxType == NexusFile.NX_UINT16 ) )
        return ncsa.hdf.hdflib.HDFNativeData.byteToShort( bdata );
      else if( ( NxType == NexusFile.NX_INT32 ) || ( NxType == NexusFile.NX_UINT32 ) )
        return ncsa.hdf.hdflib.HDFNativeData.byteToInt( bdata );
      else if( ( NxType == NexusFile.NX_INT8 ) || ( NxType == NexusFile.NX_UINT8 ) )
        return bdata;
      
      if( ( NxType == NexusFile.NX_INT32 ) ||
          ( NxType == NexusFile.NX_UINT32 ) )
        return ncsa.hdf.hdflib.HDFNativeData.byteToInt( bdata );
      if( NxType == NexusFile.NX_CHAR ){
        char[] cdata = new char[bdata.length];
        
        for( int i = 0; i < bdata.length; i++ )
          cdata[i] = ( char )bdata[i];
        return cdata;
      }

    }catch( Exception he ){
      throw new NexusException( he.getMessage() );
    }

    return null;
  }
}
