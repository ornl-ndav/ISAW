/*
 * File:  NexusUtils.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:16:01  pfpeterson
 * standardized header
 *
 */

package  DataSetTools.nexus;
 
import   neutron.nexus.*;
import   java.util.Hashtable;
import   java.util.Enumeration;
import   DataSetTools.dataset.*;

public class NexusUtils 
{

public static final int INVALID_DATA_TYPE = -Integer.MAX_VALUE;


static public AttributeList getAttributes( NexusFile nf )
{
  Attribute     attr;
  AttributeList list       = new AttributeList();
  Hashtable     hash_table = null;

  try 
  {
    hash_table = nf.attrdir();
    Enumeration e = hash_table.keys();
    while(e.hasMoreElements())
    {
      String         attname = (String)e.nextElement();
      AttributeEntry atten   = (AttributeEntry)hash_table.get(attname);
      int length_type_info[] = new int[2];
      length_type_info[0] = atten.length;
      length_type_info[1] = atten.type;

      if ( atten.type == NexusFile.NX_CHAR ||
           atten.type == NexusFile.NX_INT8 ||
           atten.type == NexusFile.NX_UINT8  )
      {
        byte bytes[] = new byte[ atten.length ];
        nf.getattr( attname, bytes, length_type_info );
        String attr_string = StringFromBytes( bytes );
        if ( attr_string != null )
        {
          attr = new StringAttribute( attname, attr_string );
          list.addAttribute( attr );
        }
      }

      else if ( atten.type == NexusFile.NX_INT32 ||
                atten.type == NexusFile.NX_UINT32  )
      {
        int ints[] = new int[ atten.length ];
        nf.getattr( attname, ints, length_type_info );
    
        if ( ints.length > 1 ) 
          attr = new IntListAttribute( attname, ints );
        else
          attr = new IntAttribute( attname, ints[0] );
        list.addAttribute( attr );
      }

      else if ( atten.type == NexusFile.NX_FLOAT32 )
      {
        float floats[] = new float[ atten.length ];
        nf.getattr( attname, floats, length_type_info );
        attr = new FloatAttribute( attname, floats[0] );   // lists of floats
        list.addAttribute( attr );                         // not supported
      }

      else if ( atten.type == NexusFile.NX_FLOAT64 )
      {
        double doubles[] = new double[ atten.length ];
        nf.getattr( attname, doubles, length_type_info );
        attr = new DoubleAttribute( attname, doubles[0] ); // lists of floats
        list.addAttribute( attr );                         // not supported
      }
     
      else
      {
        attr = new StringAttribute( attname, "Attr Type NOT SUPPORTED" );
        list.addAttribute( attr );
      }
    }
  }
  catch ( Exception e )
  {
    System.out.println("EXCEPTION calling nf.attrdir() " +
                       "in NexusUtils.getAttributes() "+e);
    return list;
  }

  return list;
}


static public Object getFloat32Data( NexusFile nf )
{
   Object obj = null;

   try
   {
     int idim[]           = new int[3];            // limits us to 3 dimensions
     int rank_type_info[] = new int[2];
     nf.getinfo( idim, rank_type_info );


     if ( rank_type_info[0] == 1 )                 // one dimensional array
     {
       float one_d_array[] = new float[ idim[0] ];
       nf.getdata( one_d_array );
       obj = one_d_array;
     }
     else if ( rank_type_info[0] == 2 )            // two dimensional array
     {
       float two_d_array[][] = new float[ idim[0]][ idim[1] ];
       nf.getdata( two_d_array );
       obj = two_d_array;
     }
     else if ( rank_type_info[0] == 3 )            // three dimensional array
     {
       float three_d_array[][][] = new float[ idim[0]][ idim[1] ][ idim[2] ];
       nf.getdata( three_d_array );
       obj = three_d_array;
     }
   }
   catch ( Exception e )
   {
     System.out.println("EXCEPTION in NexusUtils.getFloat32Data() " + e );
   }

   return obj;
}


static public Object getFloat64Data( NexusFile nf )
{
   Object obj = null;

   try
   {
     int idim[]           = new int[3];            // limits us to 3 dimensions
     int rank_type_info[] = new int[2];
     nf.getinfo( idim, rank_type_info );

     if ( rank_type_info[0] == 1 )                 // one dimensional array
     {
       double one_d_array[] = new double[ idim[0] ];
       nf.getdata( one_d_array );
       obj = one_d_array;
     }
     else if ( rank_type_info[0] == 2 )            // two dimensional array
     {
       double two_d_array[][] = new double[ idim[0]][ idim[1] ];
       nf.getdata( two_d_array );
       obj = two_d_array;
     }
     else if ( rank_type_info[0] == 3 )            // three dimensional array
     {
       double three_d_array[][][] = new double[ idim[0]][ idim[1] ][ idim[2] ];
       nf.getdata( three_d_array );
       obj = three_d_array;
     }
   }
   catch ( Exception e )
   {
     System.out.println("EXCEPTION in NexusUtils.getFloat64Data() " + e );
   }

   return obj;
}




static public Object getInt32Data( NexusFile nf )
{
   Object obj = null;

   try
   {
     int idim[]           = new int[3];            // limits us to 3 dimensions
     int rank_type_info[] = new int[2];
     nf.getinfo( idim, rank_type_info );


     if ( rank_type_info[0] == 1 )                 // one dimensional array
     {
       int one_d_array[] = new int[ idim[0] ];
       nf.getdata( one_d_array );
       obj = one_d_array;
     }
     else if ( rank_type_info[0] == 2 )            // two dimensional array
     {
       int two_d_array[][] = new int[ idim[0]][ idim[1] ];
       nf.getdata( two_d_array );
       obj = two_d_array;
     }
     else if ( rank_type_info[0] == 3 )            // three dimensional array
     {
       int three_d_array[][][] = new int[ idim[0]][ idim[1] ][ idim[2] ];
       nf.getdata( three_d_array );
       obj = three_d_array;
     }
   }
   catch ( Exception e )
   {
     System.out.println("EXCEPTION in NexusUtils.getInt32Data() " + e );
   }

   return obj;
}


static public Object getInt16Data( NexusFile nf )
{
   Object obj = null;

   try
   {
     int idim[]           = new int[3];            // limits us to 3 dimensions
     int rank_type_info[] = new int[2];
     nf.getinfo( idim, rank_type_info );

     if ( rank_type_info[0] == 1 )                 // one dimensional array
     {
       short one_d_array[] = new short[ idim[0] ];
       nf.getdata( one_d_array );
       obj = one_d_array;
     }
     else if ( rank_type_info[0] == 2 )            // two dimensional array
     {
       short two_d_array[][] = new short[ idim[0]][ idim[1] ];
       nf.getdata( two_d_array );
       obj = two_d_array;
     }
     else if ( rank_type_info[0] == 3 )            // three dimensional array
     {
       short three_d_array[][][] = new short[ idim[0]][ idim[1] ][ idim[2] ];
       nf.getdata( three_d_array );
       obj = three_d_array;
     }
   }
   catch ( Exception e )
   {
     System.out.println("EXCEPTION in NexusUtils.getInt16Data() " + e );
   }

   return obj;
}



static public Object getByteData( NexusFile nf )
{
   Object obj = null;

   try
   {
     int idim[]           = new int[3];            // limits us to 3 dimensions
     int rank_type_info[] = new int[2];
     nf.getinfo( idim, rank_type_info );


     if ( rank_type_info[0] == 1 )                 // one dimensional array
     { 
       byte one_d_array[] = new byte[ idim[0] ];
       nf.getdata( one_d_array );
       obj = one_d_array;
     }
     else if ( rank_type_info[0] == 2 )            // two dimensional array
     {
       byte two_d_array[][] = new byte[ idim[0]][ idim[1] ];
       nf.getdata( two_d_array );
       obj = two_d_array;
     }
     else if ( rank_type_info[0] == 3 )            // three dimensional array
     {
       byte three_d_array[][][] = new byte[ idim[0]][ idim[1] ][ idim[2] ];
       nf.getdata( three_d_array );
       obj = three_d_array;
     }
   }
   catch ( Exception e )
   {
     System.out.println("EXCEPTION in NexusUtils.getByteData() " + e );
   }

   return obj;
}


static public Object getData( NexusFile nf )
{
   Object obj = null;

   int type = getDataType( nf );

   if ( type == NexusFile.NX_FLOAT32 )
     obj = getFloat32Data( nf );

   else if ( type == NexusFile.NX_FLOAT64 )
     obj = getFloat64Data( nf );

   else if ( type == NexusFile.NX_INT32  ||
             type == NexusFile.NX_UINT32  )
     obj = getInt32Data( nf );

   else if ( type == NexusFile.NX_INT16  ||
             type == NexusFile.NX_UINT16  )
     obj = getInt16Data( nf );

   else if ( type == NexusFile.NX_INT8   ||
             type == NexusFile.NX_UINT8  ||
             type == NexusFile.NX_CHAR    )
     obj = getByteData( nf );

   return obj; 
}



static public String getString( NexusFile nf )
{
   String result = null;

   int type = getDataType( nf );
   int rank = getDataRank( nf );
   if ( rank == 1 )
     if ( type == NexusFile.NX_INT8   ||
          type == NexusFile.NX_UINT8  ||
          type == NexusFile.NX_CHAR    )
     {
       byte bytes[] = (byte[])getByteData( nf );
       result = StringFromBytes( bytes );
     }

   return result;
}



static public String getString( NexusFile nf, String name )
{
   String result = null;

   try
   {
     nf.opendata( name );
     result = getString( nf );
     nf.closedata();
   }
   catch ( Exception e )
   {
     // no such data name, or wrong type, so just return null
   }

   return result;
}


static public float[] getFloatArray1( NexusFile nf )
{
   float result[] = null;

   int rank = getDataRank( nf );
                                          // we'll interpret any 1D array
   if ( rank != 1 )                       // as an array of floats
     return null;

   int type = getDataType( nf );
   if ( type == NexusFile.NX_INT8   ||
        type == NexusFile.NX_UINT8  ||
        type == NexusFile.NX_CHAR    )
   {
     byte bytes[] = (byte[])getByteData( nf );
     if ( bytes != null )
     {
       result = new float[ bytes.length ];
       for ( int i = 0; i < bytes.length; i++ )
         result[i] = bytes[i];
     }
   }

   else if ( type == NexusFile.NX_INT16   ||
             type == NexusFile.NX_UINT16  )
   {
     short shorts[] = (short[])getInt16Data( nf );
     if ( shorts != null )
     {
       result = new float[ shorts.length ];
       for ( int i = 0; i < shorts.length; i++ )
         result[i] = shorts[i];
     }
   }

   else if ( type == NexusFile.NX_INT32   ||
             type == NexusFile.NX_UINT32  )
   {
     int ints[] = (int[])getInt32Data( nf );
     if ( ints != null )
     {
       result = new float[ ints.length ];
       for ( int i = 0; i < ints.length; i++ )
         result[i] = ints[i];
     }
   } 

   else if ( type == NexusFile.NX_FLOAT32 )
   {
     return (float[])getFloat32Data( nf );
   }

   else if ( type == NexusFile.NX_FLOAT64 )
   {
     int doubles[] = (int[])getFloat64Data( nf );
     if ( doubles != null )
     {
       result = new float[ doubles.length ];
       for ( int i = 0; i < doubles.length; i++ )
         result[i] = (float)doubles[i];
     }
   }

   return result;
}


static public float[] getFloatArray1( NexusFile nf, String name )
{
   float result[] = null;

   try
   {
     nf.opendata( name );
     result = getFloatArray1( nf );
     nf.closedata();
   }
   catch ( Exception e )
   {
     // no such data name, or wrong type, so just return null
   }

   return result;
}



static public float[][] getFloatArray2( NexusFile nf )
{
   float result[][] = null;

   int rank = getDataRank( nf );
                                          // we'll interpret any 1D array
   if ( rank != 2 )                       // as an array of floats
     return null;

   int type = getDataType( nf );
   if ( type == NexusFile.NX_INT8   ||
        type == NexusFile.NX_UINT8  ||
        type == NexusFile.NX_CHAR    )
   {
     byte bytes[][] = (byte[][])getByteData( nf );
     if ( bytes != null )
     {
       result = new float[ bytes.length ][ bytes[0].length ];
       for ( int row = 0; row < bytes.length; row++ )
         for ( int col = 0; col < bytes[0].length; col++ )
           result[row][col] = bytes[row][col];
     }
   }

   else if ( type == NexusFile.NX_INT16   ||
             type == NexusFile.NX_UINT16  )
   {
     short shorts[][] = (short[][])getInt16Data( nf );
     if ( shorts != null )
     {
       result = new float[ shorts.length ][ shorts[0].length ];
       for ( int row = 0; row < shorts.length; row++ )
         for ( int col = 0; col < shorts[0].length; col++ )
           result[row][col] = shorts[row][col];
     }
   }

   else if ( type == NexusFile.NX_INT32   ||
             type == NexusFile.NX_UINT32  )
   {
     int ints[][] = (int[][])getInt32Data( nf );
     if ( ints != null )
     {
       result = new float[ ints.length ][ ints[0].length ];
       for ( int row = 0; row < ints.length; row++ )
         for ( int col = 0; col < ints[0].length; col++ )
           result[row][col] = ints[row][col];
     }
   } 

   else if ( type == NexusFile.NX_FLOAT32 )
   {
     return (float[][])getFloat32Data( nf );
   }

   else if ( type == NexusFile.NX_FLOAT64 )
   {
     double doubles[][] = (double[][])getFloat64Data( nf );
     if ( doubles != null )
     {
       result = new float[ doubles.length ][ doubles[0].length ];
       for ( int row = 0; row < doubles.length; row++ )
         for ( int col = 0; col < doubles[0].length; col++ )
           result[row][col] = (float)doubles[row][col];
     }
   }

   return result;
}


static public float[][] getFloatArray2( NexusFile nf, String name )
{
   float result[][] = null;

   try
   {
     nf.opendata( name );
     result = getFloatArray2( nf );
     nf.closedata();
   }
   catch ( Exception e )
   {
     // no such data name, or wrong type, so just return null
   }

   return result;
}


static public int getDataType( NexusFile nf )
{
  int type = INVALID_DATA_TYPE;

   try
   {
     int idim[]           = new int[20];      // limits it to 20 dimensions
     int rank_type_info[] = new int[2];
     nf.getinfo( idim, rank_type_info );

     type = rank_type_info[1];
   }
   catch ( Exception e )
   {
     System.out.println("EXCEPTION in NexusUtils.getDataType() " + e );
   }

   return type;
}



static public int getDataRank( NexusFile nf )
{
   int rank = 0;

   try
   {
     int idim[]           = new int[20];      // limits it to 20 dimensions
     int rank_type_info[] = new int[2];
     nf.getinfo( idim, rank_type_info );

     rank = rank_type_info[0];
   }
   catch ( Exception e )
   {
     System.out.println("EXCEPTION in NexusUtils.getDataRank()" + e );
   }

   return rank;
}


public static String StringFromBytes( byte bytes[] )
{
  if ( bytes == null )
    return null;

  if ( bytes.length == 0 )
    return new String("");

  int     i     = 0;
  String  result = new String( bytes );

  boolean found = false;                 // look for null, to shorten
  while ( i < bytes.length && !found )   // string
  {
    if ( bytes[i] == 0 )
      found = true;
    else
      i++;
  }
  if ( found )
    result = result.substring( 0, i );

  return result;
}


static public void main(String args[])
{

  String file_name = "/IPNShome/dennis/lrcs3000.nxs";
//  String file_name = "/IPNShome/dennis/ISAW/NDS_DATA/sans030101999.hdf";
  String attname;
  AttributeEntry atten;      

  System.out.println("Code for NX_CHAR    is " + NexusFile.NX_CHAR );
  System.out.println("Code for NX_INT8    is " + NexusFile.NX_INT8 );
  System.out.println("Code for NX_INT16   is " + NexusFile.NX_INT16 );
  System.out.println("Code for NX_INT32   is " + NexusFile.NX_INT32 );
  System.out.println("Code for NX_UINT8   is " + NexusFile.NX_UINT8 );
  System.out.println("Code for NX_UINT16  is " + NexusFile.NX_UINT16 );
  System.out.println("Code for NX_UINT32  is " + NexusFile.NX_UINT32 );
  System.out.println("Code for NX_FLOAT32 is " + NexusFile.NX_FLOAT32 );
  System.out.println("Code for NX_FLOAT64 is " + NexusFile.NX_FLOAT64 );
  try
  {
    NexusFile nf = new NexusFile( file_name, NexusFile.NXACC_READ );

   System.out.println("\nThe GLOBAL attributes are ...........");
   AttributeList attr_list = getAttributes( nf );
   for ( int i = 0; i < attr_list.getNum_attributes(); i++ )
   {
     System.out.println( attr_list.getAttribute(i).getName() +":    "+
                         attr_list.getAttribute(i).getValue() );
   }
   System.out.println();


// test reading top level vGroup directory
    System.out.println("TOP LEVEL vGroup entries............................");
    Hashtable    h = nf.groupdir();
    Enumeration e = h.keys();
    while(e.hasMoreElements())
    {
       String vname = (String)e.nextElement();
       String vclass = (String)h.get(vname);
       System.out.println("     Item: " + vname + " class: " + vclass);
    }


// test reading "Histogram1" vGroup directory
   nf.opengroup("Histogram1","NXentry");
   System.out.println("\nThe Histogram1 attributes are ...........");
   attr_list = getAttributes( nf );
   for ( int i = 0; i < attr_list.getNum_attributes(); i++ )
   {
     System.out.println( attr_list.getAttribute(i).getName() +":    "+
                         attr_list.getAttribute(i).getValue() );
   }
   System.out.println();

    System.out.println("Histogram1, NXentry, vGroup entries.................");
    h = nf.groupdir();
    e = h.keys();
    System.out.println("Found in 'Histogram1' vGroup entry:");
    while(e.hasMoreElements())
    {
       String vname = (String)e.nextElement();
       String vclass = (String)h.get(vname);
       System.out.println("     Item: " + vname + " class: " + vclass);
    }

   nf.opendata( "analysis" );
   System.out.println("DataRank = " + getDataRank( nf ) );
   System.out.println("DataType = " + getDataType( nf ) );
   System.out.println("Analysis = " + getData(nf) );

// test reading "data" vGroup directory
    System.out.println("data, NXdata, vGroup entries.................");
    nf.opengroup("data","NXdata");
    h = nf.groupdir();
    e = h.keys();
    System.out.println("Found in 'Histogram1/data' vGroup entry:");
    while(e.hasMoreElements())
    {
       String vname = (String)e.nextElement();
       String vclass = (String)h.get(vname);
       System.out.println("     Item: " + vname + " class: " + vclass);
    }


   // test reading attributes of "phi" SDS
    nf.opendata("phi");
   System.out.println("\nThe attributes of 'phi' are ...........");
   attr_list = getAttributes( nf );
   for ( int i = 0; i < attr_list.getNum_attributes(); i++ )
   {
     System.out.println( attr_list.getAttribute(i).getName() +":    "+ 
                         attr_list.getAttribute(i).getValue() );
   }
   System.out.println();


   float phi[] = (float[])getData(nf);
   System.out.println("PHI length is " + phi.length);
   System.out.println("DataRank = " + getDataRank( nf ) );
   int type = getDataType(nf);
   System.out.println("'phi' data type is : " + type );
   System.out.println("PHI values are ");
   for ( int i = 0; i < phi.length; i++ )
     System.out.print( phi[i] + "  " );
   System.out.println();

   nf.closedata();

   nf.opendata("time_of_flight");
   System.out.println("\nThe attributes of 'time_of_flight' are ...........");
   attr_list = getAttributes( nf );
   for ( int i = 0; i < attr_list.getNum_attributes(); i++ )
   {
     System.out.println( attr_list.getAttribute(i).getName() +":    "+ 
                         attr_list.getAttribute(i).getValue() );
   }
   System.out.println();

   System.out.println("DataRank = " + getDataRank( nf ) );
   type = getDataType(nf);
   System.out.println("'TOF' data type is : " + type );
   nf.closedata();

   float tof[] = getFloatArray1(nf,"time_of_flight");
   if ( tof == null )
     System.out.println("ERROR tof == null" );
   System.out.println("TOF length is " + tof.length);
   System.out.println("TOF values are ");
   for ( int i = 0; i < tof.length; i++ )
     System.out.print( tof[i] + "  " );
   System.out.println();


   nf.opendata("data");
   System.out.println("\nThe attributes of 'data' are ...........");
   attr_list = getAttributes( nf );
   for ( int i = 0; i < attr_list.getNum_attributes(); i++ )
   {
     System.out.println( attr_list.getAttribute(i).getName() +":    "+
                         attr_list.getAttribute(i).getValue() );
   }
   System.out.println();

   System.out.println("DataRank = " + getDataRank( nf ) );
   type = getDataType(nf);
   System.out.println("'data' data type is : " + type );
   nf.closedata();

   float data[][] = (float[][])getFloatArray2( nf, "data" );
   System.out.println("data size is " + data.length + " rows " + 
                                     data[0].length + " cols" );

   for ( int i = 0; i < data[0].length; i++ )
     System.out.print( " " + data[30][i] ); 

  }
  catch ( Exception e )
  {
    System.out.println("Exception in NexusUtils "+e);
  }



}

}

