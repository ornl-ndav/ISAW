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
 * Revision 1.10  2005/03/16 17:51:29  rmikk
 * Used static reference for static method
 * Eliminated a lot of commented out code.
 *
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
    //NXData_util ut = new NXData_util();
    float rho[];
    rho  = null;
    //distance
    NxNode X = node.getChildNode( "distance" );
   
    if( X != null ){
      Object val = X.getNodeValue();
      if( val != null )
        rho = NXData_util.Arrayfloatconvert( val );
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
       
    return false;
  }

}

