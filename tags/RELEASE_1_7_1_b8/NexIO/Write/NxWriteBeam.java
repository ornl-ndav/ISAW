/*
 * File:  NxWriteBeam.java 
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
 * Revision 1.6  2005/01/10 16:17:16  rmikk
 * Eliminated unused code
 *
 * Revision 1.5  2003/12/08 20:56:41  rmikk
 * Moved the duration from NXbeam to NXentry
 *
 * Revision 1.4  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/20 16:15:36  pfpeterson
 * reformating
 *
 * Revision 1.2  2002/03/18 20:58:09  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 */


package NexIO.Write;

import DataSetTools.dataset.*;
import NexIO.*;

/**
 *Handles writing information to the NxBeam portion of the Nexus Standard
 */
public class NxWriteBeam{
  String errormessage;

  public NxWriteBeam(int instrType){
    errormessage = "";
  }

  /**
   * Returns an error message or "" if there is no error
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Extracts the information from the Data Set and writes it to the
   * NxBeam portion of a Nexus file
   *
   * @param  node    A node whose class is NXbeam..
   * @param  DS      The data set whose information is to be written
   */
  public boolean processDS( NxWriteNode node, DataSet DS ){
    errormessage = "Improper inputs to Write Beam ";
    if( node == null ) 
      return true;
    if( DS == null )
      return true;
    int rank[];
    float ff[];
    NxWriteNode n1;
    Object O = DS.getAttributeValue( Attribute.ENERGY_IN );
    if( O != null ){
      if( O instanceof Float ){
        float f = ( ( Float ) O ).floatValue();
        ff = new float[1];
        ff[0] = f;
        rank = new int[1];
        rank[0] = 1;
        n1 = node.newChildNode( "incident_energy","SDS" );
        n1.setNodeValue( ff, Types.Float, rank ); 
        if( n1.getErrorMessage() != "" );
        errormessage += ":"+n1.getErrorMessage();
      }  
    }
    //Energy out
    O = DS.getAttributeValue( Attribute.ENERGY_OUT );
    if( O != null ){
      if( O instanceof Float ){
        float f = ( ( Float )O ).floatValue();
        ff = new float[1];
        ff[0] = f;
        rank = new int[1];
        rank[0] = 1;
        n1 = node.newChildNode( "final_energy","SDS" );
        n1.setNodeValue( ff, Types.Float, rank ); 
        if( n1.getErrorMessage() != "" );
        errormessage += ":"+n1.getErrorMessage();
        n1.addAttribute("units",("meV"+(char)0).getBytes(),Types.Char,
                        Inst_Type.makeRankArray(4,-1,-1,-1,-1));
      }  
    } 
   
     NxWriteNode NxLognode = node.newChildNode("Log_2","NXlog");
    NxWriteLog writelog = new NxWriteLog( 5);
    writelog.processDS( NxLognode, null, 2); 
    return false;
  }//processDS
}//class NxWriteBeam
