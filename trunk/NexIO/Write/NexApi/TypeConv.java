/*
 * File: TypeConv.java
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
 * $Log$
 * Revision 1.3  2002/11/27 23:29:29  pfpeterson
 * standardized header
 *
 */
package NexIO.Write.NexApi;
import NexIO.*;
import neutron.nexus.*;

public class TypeConv{
  /**
   * returns the NexIO type that corresponds to the given Nexus type
   */
  public static int convertTo(int Nexus_type){
    if( Nexus_type == NexusFile.NX_INT32 )return NexIO.Types.Int;
    if( Nexus_type == NexusFile.NX_UINT32 )return  NexIO.Types.UInt; 
    if( Nexus_type == NexusFile.NX_INT8 )return NexIO.Types.Byte ;
    if( Nexus_type == NexusFile.NX_UINT8 ) return NexIO.Types.UByte; 
    if( Nexus_type == NexusFile.NX_CHAR )return NexIO.Types.Char; 
    if(Nexus_type == NexusFile.NX_INT16 )return NexIO.Types.Short;
    if (Nexus_type == NexusFile.NX_UINT16 )return NexIO.Types.UShort;  
    // Created by us if( Nexus_type == NexusFile.  ) return NexIO.Long; 
    if( Nexus_type == NexusFile.NX_FLOAT32 ) return NexIO.Types.Float ;
    if( Nexus_type == NexusFile.NX_FLOAT64 ) return NexIO.Types.Double; 
    return -1;
  }

  /**
   * Returns the Nexus type that corresponds to a NexIO type
   */
  public static int convertFrom( int NexIO_type){
    if( NexIO_type ==NexIO.Types.Int  )return NexusFile.NX_INT32 ;
    if( NexIO_type == NexIO.Types.UInt )return NexusFile.NX_UINT32 ; 
    if( NexIO_type ==  NexIO.Types.Byte )return NexusFile.NX_INT8;
    if( NexIO_type ==NexIO.Types.UByte  ) return NexusFile.NX_UINT8; 
    if( NexIO_type ==  NexIO.Types.Char)return NexusFile.NX_CHAR ; 
    if(NexIO_type == NexIO.Types.Short )return NexusFile.NX_INT16;
    if (NexIO_type == NexIO.Types.UShort  )return NexusFile.NX_UINT16;  
    // Created by us if( NexIO_type ==  return NexIO.LongNexusFile.  ); 
    if( NexIO_type ==NexIO.Types.Float ) return NexusFile.NX_FLOAT32  ;
    if( NexIO_type == NexIO.Types.Double ) return NexusFile.NX_FLOAT64; 
    return -1;
  }

  /**
   * Assumes that it the unsigned types were "fixed" to be one length
   * more
   */
  public static String Classname( int Nexus_type){
    if( Nexus_type == NexusFile.NX_INT32 )
      return (new int[0]).getClass().toString();
    if( Nexus_type == NexusFile.NX_UINT32 )
      return (new long[0]).getClass().toString();                 
    if( Nexus_type == NexusFile.NX_INT8 )
      return (new byte[0]).getClass().toString(); 
    if( Nexus_type == NexusFile.NX_UINT8 )
      return (new short[0]).getClass().toString();  
    if( Nexus_type == NexusFile.NX_CHAR )
      return (new byte[0]).getClass().toString();
    if(Nexus_type == NexusFile.NX_INT16 )
      return (new short[0]).getClass().toString();
    if (Nexus_type == NexusFile.NX_UINT16 )
      return (new int[0]).getClass().toString(); 
    // Created by us if( Nexus_type == NexusFile.  )  
    if( Nexus_type == NexusFile.NX_FLOAT32 ) 
      return (new float[0]).getClass().toString();
    if( Nexus_type == NexusFile.NX_FLOAT64 ) 
      return (new double[0]).getClass().toString();
    return null;
  }

  public static void main( String args[]){
    System.out.println(convertTo( NexusFile.NX_INT32)); 
    System.out.println(convertTo(NexusFile.NX_UINT32));
    System.out.println(convertTo(NexusFile.NX_INT8 ));
    System.out.println(convertTo(NexusFile.NX_UINT8)); 
    System.out.println(convertTo(NexusFile.NX_CHAR )); 
    System.out.println(convertTo(NexusFile.NX_INT16));
    System.out.println(convertTo(NexusFile.NX_UINT16));  
    System.out.println(convertTo(NexusFile.NX_FLOAT32)); 
    System.out.println(convertTo( NexusFile.NX_FLOAT64));
    System.out.println( Classname( NexusFile.NX_INT32 ));
    System.out.println( Classname( NexusFile.NX_UINT32));
    System.out.println( Classname( NexusFile.NX_INT8)); 
    System.out.println( Classname( NexusFile.NX_UINT8 ));
    System.out.println( Classname(  NexusFile.NX_CHAR));  
    System.out.println( Classname( NexusFile.NX_INT16));
    System.out.println( Classname( NexusFile.NX_UINT16));  
    System.out.println( Classname( NexusFile.NX_FLOAT32)); 
    System.out.println( Classname( NexusFile.NX_FLOAT64));
  }
}
