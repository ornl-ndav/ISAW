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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/03/18 21:10:14  dennis
 * Now uses atan2(,) to find angles
 *
 * Revision 1.1  2001/07/24 20:12:52  rmikk
 * Initial submition
 *
 */


package NexIO;

import DataSetTools.math.*;
import java.lang.*;
/** Contains equates for this modules data types
*  All others should map into these
*/
public class Types
{
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

/**  Converts from IPNS coordinates( z vert, x along beam, y back) to
*   Nexus format ( z beam, y up and x back).  
*@param  rho  the total distance to the detector
*@param  phi  Angle from IPNS's z axis. Not scattering angle
*@param  theta  the angle from x-axis(beam axis)in the horizontal plane.
*@ return the rho, phi, theta values in Nexus
*/
    public static float[] convertToNexus( float rho, float phi, float theta)
    { float r =(float)( rho* Math.sin( phi ));
      float z =(float)( rho* Math.cos( phi ));
      float x = (float)(r * Math.cos( theta ));
      float y = (float)(r * Math.sin( theta ));
     //System.out.println("Cartes="+x+","+y+","+z+","+r+","+phi+","+theta);
     float x1,y1,z1,r1;
     x1=  y;
     y1 = z;
     z1 = x;
     //System.out.println("Cartes="+x1+","+y1+","+z1);
     rho = (float)Math.sqrt( x1*x1+y1*y1+z1*z1);
     r = (float) Math.sqrt( x1*x1 + y1*y1);
     if( rho != 0)
         phi = (float)Math.acos( z1/rho);
     else
         phi = 0.0f;
     r1 = (float)Math.sqrt( x1*x1+y1*y1);
     
         theta = (float)Math.atan2( y1, x1);
     
     float coords[];
   
     coords = new float[3];
     coords[0] = rho;
     coords[1] = phi;
     
           coords[2] = theta;
     
     return coords;

    }
 /**  Converts from  Nexus format ( z beam, y up and x back)  
*  to IPNS coordinates( z vert, x along beam, y back) 
*@param  rho  the total distance to the detector
*@param  phi  Angle from Nexus's z axis(the beam). Scattering angle(?)
*@param  theta  angle from x-axis in plane perpendicular to beam
*@ return the rho, phi, theta values in Nexus
*/  
  public static float[] convertFromNexus( float rho, float phi, float theta)
   { float r =(float)( rho*Math.sin( phi ));
      float z = (float)(rho* Math.cos( phi ));
      float x =(float)( r * Math.cos( theta ));
      float y =(float)( r * Math.sin( theta ));
   //System.out.println("Cartes="+x+","+y+","+z+","+phi+","+theta+","+r);
      //  System.out.print("Start end r,p,t="+rho+","+phi+","+theta);
     float x1,y1,z1,r1;
     x1= z;
     y1 = x;
     z1 = y;
     //System.out.println("Cartes="+x1+","+y1+","+z1);
     rho =(float) Math.sqrt( x1*x1+y1*y1+z1*z1);
     r = (float) Math.sqrt( x1*x1+y1*y1);
     if( rho != 0)
         phi =(float) Math.acos( z1/rho);
     else
         phi = 0.0f;
     r1 = (float)Math.sqrt( x1*x1+y1*y1);
     
         theta =(float) Math.atan2(y1, x1);
     
     float coords[];
     coords = new float[3];
     coords[0] = rho;
     coords[1] = phi;
     
       coords[2] = theta;
     
    // System.out.println("::"+rho+","+phi+","+theta);
    return coords;
    }
  
/** Test program for the convert to and from Nexus routines
*@param  args[0]  pho
*@param args[1]  phi
*@param args[2]  theta
*@param args[3]  optional if absent From Nexus otherwise to Nexus
*@returns  coords[]  the rho,phi, theta in the other system
*/
public static void main( String args[])
  { float r ,t ,p, coords[];
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

}
