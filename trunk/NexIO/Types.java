/*
 * File: Types.java 
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
 * Revision 1.12  2005/02/12 17:00:39  rmikk
 * Fixed an error in data type corresponding to Long and Int
 *
 * Revision 1.11  2004/12/23 18:54:41  rmikk
 * Added extra spacing between lines of code.
 *
 * Revision 1.10  2004/03/15 19:37:54  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.9  2004/03/15 03:36:01  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.8  2004/03/11 16:45:03  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.7  2004/02/28 17:18:17  rmikk
 * Added static methods to linearlize a multidimensioned Java array and to
 * convert a linear array to a multidimensioned array
 *
 * Revision 1.6  2004/02/16 02:15:56  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.5  2003/10/15 02:52:58  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.4  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/20 16:14:52  pfpeterson
 * reformating
 *
 * Revision 1.2  2002/03/18 21:10:14  dennis
 * Now uses atan2(,) to find angles
 *
 */

package NexIO;

import gov.anl.ipns.Util.Sys.*;

/**
 * Contains equates for this modules data types. All others should map
 * into these
 */
public class Types{
  public static int Int =325;
  public static int UInt =326;
  public static int Byte =400;
  public static int UByte =401;
  public static int Char =410;
  public static int Short =512;
  public static int UShort =513;
  public static int Long =600;
  public static int Float =620;
  public static int Double =626;
  /**
   *  Contains "" unless there was an error from one of the methods
   */
  public static String errormessage ="";

  /**
   * Converts from IPNS coordinates( z vert, x along beam, y back) to
   * Nexus format ( z beam, y up and x back).
   *
   * @param rho the total distance to the detector
   * @param phi Angle from IPNS's z axis. Not scattering angle
   * @param theta the angle from x-axis(beam axis)in the horizontal
   * plane.
   *
   * @return the rho, phi, theta values in Nexus
   */
  public static float[] convertToNexus( float rho, float phi, float theta){
    
     errormessage = "";
     float r =(float)( rho* Math.sin( phi ));
     float z =(float)( rho* Math.cos( phi ));
     float x = (float)(r * Math.cos( theta ));
     float y = (float)(r * Math.sin( theta ));
    
     float x1,
           y1,
           z1;
          
     x1=  y;
     y1 = z;
     z1 = x;
    
     rho = (float)Math.sqrt( x1*x1+y1*y1+z1*z1);
     r = (float) Math.sqrt( x1*x1 + y1*y1);
     if( rho != 0)
        phi = (float)Math.acos( z1/rho);
     else
        phi = 0.0f;
     //float r1 = (float)Math.sqrt( x1*x1+y1*y1);
    
     theta = (float)Math.atan2( y1, x1);
    
     float coords[];
    
     coords = new float[3];
     coords[0] = rho;
     coords[1] = phi;
    
     coords[2] = theta;
    
     return coords;
    
  }

  /**
   * Converts from Nexus format ( z beam, y up and x back) to IPNS
   * coordinates( z vert, x along beam, y back)
   *
   * @param rho the total distance to the detector
   * @param phi Angle from Nexus's z axis(the beam). Scattering
   * angle(?)
   * @param theta angle from x-axis in plane perpendicular to beam
   *
   * @return the rho, phi, theta values in Nexus
   */  
  public static float[] convertFromNexus( float rho, float phi, float theta){
     errormessage = "";
     float r =(float)( rho*Math.sin( phi ));
     float z = (float)(rho* Math.cos( phi ));
     float x =(float)( r * Math.cos( theta ));
     float y =(float)( r * Math.sin( theta ));
    
     float x1,
           y1,
           z1;
          
     x1= z;
     y1 = x;
     z1 = y;
    
     rho =(float) Math.sqrt( x1*x1+y1*y1+z1*z1);
     r = (float) Math.sqrt( x1*x1+y1*y1);
     if( rho != 0)
       phi =(float) Math.acos( z1/rho);
     else
       phi = 0.0f;
     //float r1 = (float)Math.sqrt( x1*x1+y1*y1);
    
     theta =(float) Math.atan2(y1, x1);
     
     float coords[];
     coords = new float[3];
     coords[0] = rho;
     coords[1] = phi;
    
     coords[2] = theta;
    
 
     return coords;
  }
  
  /**
   * Test program for the convert to and from Nexus routines
   * Prints coords[] the rho,phi, theta in the other system.
   *
   * @param args args[0] = pho
   *             args[1] = phi
   *             args[2] = theta
   *             args[3] = optional. if absent, taken to be From Nexus
   *                       otherwise to Nexus
   */
  public static void main1( String args[]){
    
     float r ,t ,p, coords[];
     r = new Float( args[0]).floatValue();
     p = new Float(args[1]).floatValue();
     t = new Float(args[2]).floatValue();
   
     if( args.length>3)
       coords = convertToNexus( r ,p, t);
       
     else 
       coords= convertFromNexus( r , p, t);
       
     if( args.length > 3)
       System.out.print( "Nexus coords=");
       
     else
       System.out.print(" IPNS coords=");
       
     System.out.println(coords[0]+","+coords[1]+","+coords[2]);
     System.exit(0); 
  }
  
  
  public static void main( String args[]){
    
     int[] dims ={3,2,3};
     float[][][] data = new float[3][2][3];
     for( int i = 0; i < 3;i++)
        for( int j=0;j<2;j++)
           for( int k = 0; k<3;k++)
              data[i][j][k] = i*6+j*3+k;
              
     System.out.println("array1="+StringUtil.toString(data));
     System.out.println("");

     float[]data1 = (float[])Types.linearlizeArray(data, 3, dims,Types.Float);
     
     System.out.println("array-lin="+StringUtil.toString(data1));
     System.out.println("");

     Object data2 = Types.MultiPackLinear( data1, data1.length,  dims);
     System.out.println("array-repacked="+StringUtil.toString(data2));
                                 


  }//main

  /**
  * Attempts to create an array of the given type and length
  *
  * @param type Types.Int,Types.UInt,etc/
  * @param length the length of the array
  *
  * @return an array(linear) of the appropriate type and length or null
  */
  public static Object CreateArray( int type, int length ){
     errormessage = "";
     Object X;
    
     if( type == Types.Float)
       X = new float[ length];
       
     else if( type == Types.Char)
       X = new byte[ length];
       
     else if( type ==Types.Double)
       X = new double[ length];
       
     else if( type == Types.Int)
       X = new int[ length];
       
     else if( type ==Types.Long)
       X = new long[ length];
       
     else if( type == Types.Short)
       X = new byte[ length];
       
     else if( type == Types.UInt)
       X = new short[ length];
       
     else if( type == Types.UShort)
       X = new byte[ length];
       
     else{
      
       return null;
     }
     return X;
  }
 
  /**
  *    Linearlizes a multidimension array
  *    @param array the multi-dimensioned array
  *    @param ndims  the number of dimensions
  *    @param lengths the size of each dimension
  *    @return  a linear array of the same type as array
  */
  public static Object linearlizeArray(Object array, int ndims, int lengths[],
                                 int type){
     errormessage = "improper dimensions linearlize";
     if( ndims < 1 )
        return null;

     if( lengths == null )
        return null;

     if( ndims > lengths.length )
        return null;

     if( ndims == 1 )
        return array;
    
     int l = 1;
    
     for( int i = 0; i < ndims; i++ )
        l = l * lengths[i];

     if( l <= 0 )
        return null;   //Cannot really have multidimensioned arrays with
                     // a dimension that is zero or negative

     errormessage = "";
   
     Object buff = CreateArray( type, l );
     int n = linearlizeR( array, 0, ndims, lengths, type, buff, 0 );
    
     if( n < 0 )
        return null;
     return buff;
  }

  // The recursive linearlize
  //  @param array  the multidimensional array
  //  @param dimoffset  the position in lengths that is being unraveled
  //  @param ndims  the number of dimensions in the multidimensioned array
  //  @param lengths  the array for the size of each dimension in the multidimensioned array
  //  @param  buffs the linearized array
  //  @param  buffOffset the position in buff to start filling with elements from array
  //  @return  the next position in buff to be filled
  // NOTE: the length of buff must = the product of the lengths
  private static int linearlizeR(Object array, int dimoffset, int ndims,int lengths[],
                          int type, Object buff, int buffOffset ){

     if( dimoffset == ndims - 1 ){
       
        try{
          
           System.arraycopy( array, 0, buff, buffOffset, lengths[ndims - 1] );
           return buffOffset + lengths[ndims - 1];
           
        }catch( Exception s ){
          
           errormessage = "arraycopy error " + s;
           return -1;
           
        }
     }

     Object Res[];
    
     Res = ( Object[] )array;

     int n = buffOffset;
    
     for( int i = 0; i < Res.length; i++ ){
       
        n = linearlizeR( Res[i], dimoffset + 1, ndims, lengths, type, buff, n );
        
        if( n < 0 )
           return -1;
           
     }
     return n;
  }


  /**
  *    Packs a linear array into a multidimensional array with dimensions dims
  *    @param LinearArray  the linear Array
  *    @param length  the length of the linear Array
  *    @param dims    the number and size of the dimensions to pack the linear
  *                    array into
  *    @return the multidimensional array containing the elements in LinearArray
  *    NOTE: The length of LinearArray must = the product of the dims( 
  *                                not Checked)
  */
  public static Object MultiPackLinear( Object LinearArray, int length,  
                                                             int dims[]){
                                                               
     errormessage = "";
     Class C = LinearArray.getClass().getComponentType();
     Object Res = java.lang.reflect.Array.newInstance( C, dims);
     FillElements( Res, LinearArray, 0);
     return Res;
     
  }


  /**
  *    Fills the elements of the multidimensioned array with the elements of
  *    the linear Array
  *    @param Res  The multidimensioned array
  *    @param LinearArray the linearly dimensioned array
  *    @param n the next position in the Linear Array to be set into the 
  *               multidimensioned array
  *    @return  the next position in the Linear Array to be set into the 
  *                       multidimensioned array
  *   NOTE:The length of the LinearArray and the number of entries in Res must
  *                          be identical
  *   NOTE: LinearArray must have elements that are not Arrays(otherwise it is 
  *                         not a linear array)
  */
  private static int  FillElements( Object Res, Object LinearArray, int n){
    
     errormessage = "";
     Class ResClass = Res.getClass();
     if( ResClass.isArray()){
         try{
         
           if( !(Res instanceof Object[])){
             
              int nn =java.lang.reflect.Array.getLength(Res);
              for( int i=0; i< nn; i++)
                 System.arraycopy( LinearArray, n, Res,0,nn);
                 
              n +=nn;
              return n;
         
           }     

        }catch( Exception ss){
          
           errormessage +=";"+ss.toString();
           return -1;
           
         }
        int nn = java.lang.reflect.Array.getLength(Res);
        for( int i=0; ( i < nn ) &&( n >= 0); i++)
           n = FillElements( ((Object[])Res)[i], LinearArray, n);
           
        return n;
      
     }
     return -1;
  }

}
