/*
 * File:  NxDetector.java 
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
 * Revision 1.9  2004/12/23 18:43:21  rmikk
 * ELiminated a few warnings
 *
 * Revision 1.8  2004/05/14 15:03:26  rmikk
 * Removed unused variables
 *
 * Revision 1.7  2004/02/16 02:15:55  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.6  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/11/20 16:14:46  pfpeterson
 * reformating
 *
 * Revision 1.4  2002/04/01 20:25:08  rmikk
 * Improved Documentation
 *
 */
package NexIO;
import DataSetTools.dataset.*;
//import DataSetTools.math.*;

/**
 * Processor for NXdetector parts of the Nexus source
 */
public class NxDetector{
  String errormessage;

  public NxDetector(){
    errormessage = "";
  }

  /**
   * Returns error and or warning messages or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Fills out an existing DataSet with information from the
   * NXdetector section of a Nexus datasource. NOT USED. Most of the
   * NXdetector field read by setOtherAttributes in NXData_util.java
   * @param node the current node positioned to an NXdata part of a
   * datasource
   * @param DS the existing DataSet that is to be filled out
   * 
   * @return error status: true if there is an error otherwise false
   */
  public boolean processDS( NxNode node ,  DataSet DS ){
    errormessage = "Improper NxDetector inputs";
    if( node == null ) 
      return true;
    if( DS == null ) 
      return true;
    if(  !node.getNodeClass().equals( "NXdetector" ) ) 
      return true;
    NXData_util ut = new NXData_util();
    float rho[];
    rho  = null;
    //distance
    NxNode X = node.getChildNode( "distance" );
   
    if( X != null ){
      Object val = X.getNodeValue();
      if( val != null )
        rho = ut.Arrayfloatconvert( val );
    }
    //phi
    X = node.getChildNode( "phi" );
    
    if( X != null ){
     // Object val = X.getNodeValue();
     // if( val != null )
     //   phi = ut.Arrayfloatconvert( val );
    }
    //theta
    X = node.getChildNode( "theta" );
   
    if( X != null){
      //Object val = X.getNodeValue();
      //if( val != null )
       // theta = ut.Arrayfloatconvert( val );
    }
    int l;
    if( rho == null ){
      errormessage = "Not enuf detector info " + "distance";
      return true;
    }
    l = rho.length;
    if( l != DS.getNum_entries() ){
      errormessage += ":" + "incorrect number of datablocks in NxDetector";
      l = java.lang.Math.min( l , DS.getNum_entries() );
    }
    /* float min_angle, max_angle;
  
    min_angle=max_angle=0.0f;
    if( phi != null) min_angle = phi[0];
    int nbins = l;
    for( int i = 0 ; i < l ; i++ )
    {float r , 
    t , 
    p;
    r = rho[i];
    if( phi == null )
    p = 0.0f;
    else 
    p = phi[i];
    if(theta == null ) 
    t = 0.0f; 
    else 
    t = theta[i];
    Data D = DS.getData_entry( i );
    Position3D P = new Position3D();  
    if( p> max_angle) max_angle =p;
    if( p< min_angle) min_angle =p;
    P.setSphericalCoords( r ,  t ,  p );   
    D.setAttribute(  new DetPosAttribute( Attribute.DETECTOR_POS , 
    new DetectorPosition( P ) ) );           
    
    }
    
    
    //efficiency
    NxData_Gen ng = new NxData_Gen();
    NxNode n3 = node.getChildNode("efficiency");
    if(n3!= null)
    {Object O = n3.getNodeValue();
    if( n3.getErrorMessage() !="")
    errormessage +=":"+n3.getErrorMessage();
    if( O!= null)
    {  Float XX = ng.cnvertoFloat( O);
    if( XX != null)
    {  float u = XX.floatValue();
    if( u != Float.NaN)
    { FloatAttribute FA = new FloatAttribute(Attribute.EFFICIENCY_FACTOR, u);
    
    DS.setAttribute( FA );                    
    }
    }
    
    }
    }
    //end efficiency
    
    //raw angle
    for( int i = 0; i< l; i++)
    {Data DB = DS.getData_entry( i );
    float f = i*(max_angle-min_angle)/nbins + min_angle;
    DB.setAttribute(new FloatAttribute( Attribute.RAW_ANGLE, f)); 
    
    }
    */
    //Solid angle : need detector ID for ...Help
    return false;
  }

}

