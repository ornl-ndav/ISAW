/*
 * File:  NxMonitor.java 
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
 * Revision 1.3  2002/03/13 16:24:23  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.2  2001/07/24 20:10:04  rmikk
 * Added a lot of attributes
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;
import DataSetTools.dataset.*;
import NexIO.*;
import DataSetTools.math.*;
import java.lang.reflect.*;
/** This Class processes the NxMonitor Entries of a Nexus datasource
 */
public class NxMonitor 
{String errormessage;
 int monitor_num;
  public NxMonitor()
    {
     errormessage = "";
     monitor_num = -1;
    }

    /** Returns error and warning messages or "" if none
   */
 public String getErrorMessage()
   {
    return errormessage;
  }


 public void setMonitorNum( int num )
  {
   monitor_num = num;
  }
 public int getMonitorNum()
  {
   return monitor_num;
  }
  /** Fills out an existing DataSet with information from the NXmonitor
   * section of a Nexus datasource
  *@param node  the current node positioned to an NXmonitor part of a datasource
  *@param  DS  the existing DataSet that is to be filled out
  *@return  error status: true if there is an error otherwise false
  */
 public boolean processDS( NxNode node ,  DataSet DS )
   {errormessage = "null inputs";
    if( node == null ) 
       return true;
    if( DS == null )
        return true;
    NxNode ntof , 
          ndata;
    ntof = node.getChildNode( "time_of_flight" );
    ndata = node.getChildNode( "data" );
    if( ( ntof == null ) ||( ndata == null ) )
      for( int i = 0; i < node.getNChildNodes() ; i++ )
        {NxNode mm = node.getChildNode( i );
         if( ntof == null )
           if( mm != null )
            {Object X = mm.getAttrValue( "axis" );
             if( X != null )
               ntof = mm;
            }
         if( ndata == null )
          if( mm != null )
            {Object X = mm.getAttrValue( "signal" );
             if( X != null )
               ndata = mm;
            }           
        }
    if( ( ntof == null )||( ndata == null ) )
       {errormessage = "Cannot find the Monitor data here";
        return true;
       }
   
    Object X1 = ndata.getNodeValue();
    Object X2 = ntof.getNodeValue();
    if( X1 == null ) 
      return true;
    if( X2 == null ) 
      return true;
    NXData_util nd = new NXData_util();
    NxData_Gen nds = new NxData_Gen();
    float yvals[];
    yvals = nd.Arrayfloatconvert( X1 );
    float xvals[];
    xvals = nd.Arrayfloatconvert( X2 );
    if( ( xvals == null ) ||(  yvals==null ) ) 
       return true;
    Data D = Data.getInstance( new VariableXScale( xvals ) , 
                               yvals, 
                               monitor_num+1 );
    
    DS.addData_entry( D );
    Object val = ntof.getAttrValue( "long_name" );
    if( val != null )
      {String S = nds.cnvertoString( val );
       if( S != null )
	 DS.setX_label( S );
        }
     val = ntof.getAttrValue( "units" );
      if( val != null )
	  {String S = nds.cnvertoString( val );
	  if( S != null )
	      DS.setX_units( S );
          }  
      val = ndata.getAttrValue( "long_name" );
      if( val != null )
	  {String S = nds.cnvertoString( val );
	  if( S != null )
	      DS.setY_label( S );
          }       
      val = ndata.getAttrValue( "units" );
      if( val != null )
	  {String S = nds.cnvertoString( val );
	  if( S != null )
	      DS.setY_units( S );
          } 
       NXData_util.setOtherAttributes( node  ,D , 0 ) ;
/*     NxNode ndist = node.getChildNode( "distance" );
       float phi, theta;
       
      if( ndist == null )
        return false;
      Object A = ndist.getNodeValue(  );
      if( A == null )
        return false;
      float dist[];
      dist = nd.Arrayfloatconvert( A );
      if( dist == null )
          return false;
      A = ndist.getAttrValue( "phi");
      phi = ( float )java.lang.Math.PI;
      if( A != null) if( A instanceof float[])
        if( Array.getLength( A) == 1)
        phi = ((float[]) A)[0];

      theta = 0;
      A = ndist.getAttrValue( "theta");
      theta = ( float )java.lang.Math.PI;
      if( A != null) if( A instanceof float[])
        if( Array.getLength( A) == 1)
        theta = ((float[]) A)[0];

     float coords[];
     coords = Types.convertFromNexus( dist[0], phi, theta );
     DetectorPosition DP = new DetectorPosition();
     DP.setSphericalCoords( coords[0], coords[2], coords[1]);
     D.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS , 
                                       DP) );
*/
  /*
      Position3D p = new Position3D();
      float phi;
      if( monitor_num == 0 ) 
          phi = ( float )java.lang.Math.PI;
      else 
         phi = 0.0f;
      p.setSphericalCoords( dist[ 0 ] ,  0.0f ,  phi );
      D.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS , 
                                       new DetectorPosition( p  ) ) );
  */

      //group_id
/*      A = ndata.getAttrValue( "group_id");
      if( A != null )
        if( A instanceof int[] )
         if( Array.getLength( A) == 1)
	  {int group_id = ( (int[])A)[0];
           D.setGroup_ID( group_id);
           System.out.println("Set Monitor ="+group_id);
          }
*/
    return false;
   }

}
