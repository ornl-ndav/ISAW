/*
 * File:  NxWriteMonitor.java 
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
 * Revision 1.5  2003/11/24 14:09:01  rmikk
 * Eliminated some commented out code
 * Wrote out errors
 *
 * Revision 1.4  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/20 16:15:41  pfpeterson
 * reformating
 *
 * Revision 1.2  2002/03/18 20:58:51  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 */

package NexIO.Write;

import DataSetTools.dataset.*;
import DataSetTools.math.*;
import NexIO.*;

/**
 * A Class responsible for writing the NXmonitor data to a Nexus file
 */
public class NxWriteMonitor{
  String errormessage;
  NxNodeUtils nu;
  int instrType;

  public NxWriteMonitor( int instrType){
    errormessage = "";
    nu = new NxNodeUtils();
    this.instrType=instrType;
  }

  /**
   * Returns an errormessage or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }
  
  

  /**
   * Writes the information from the specified datablock to a
   * NXMonitor section of a Nexus file.
   *
   * @param node a NXmonitor node
   * @param DS the data set with the information to be saved
   * @param datablock the index of the Data block monitor
   */
  public boolean processDS(  NxWriteNode node , DataSet DS , int datablock ){
    int ndatablocks;
    int rank1[]  = new int[1];
    int intval[] = new int[1];

    errormessage = "Null inputs to Monitor";
    if( node == null )
      return true;
    if( DS == null )
      return true;
    errormessage = "No data Block";
    ndatablocks = DS.getNum_entries();
    if( datablock < 0 )
      return true;
    if( datablock >= ndatablocks )
      return true;
    errormessage = "";
    int i = datablock;
    // --------- Time_of Flight node( Centered) --------------------------
    NxWriteNode n1 = Inst_Type.makeXvalnode( DS,datablock,datablock+1, node);

    n1.addAttribute( "axis",Inst_Type.makeRankArray(1,-1,-1,-1,-1), Types.Int,
                     Inst_Type.makeRankArray(1,-1,-1,-1,-1));
 
    //------------- data Node ---------------------------
    NxWriteNode n2 = node.newChildNode( "data" , "SDS" );
    String units = DS.getY_units();
    String longname = DS.getY_label();
    if( units != null ){
      rank1[ 0 ] = units.length( ) + 1;
      n2.addAttribute("units",(units+(char)0).getBytes(),Types.Char,rank1);
    }
    if( longname != null ){
      byte LN[];
      LN = ( longname + (char)0 ).getBytes();
      rank1[ 0 ] = LN.length;       
      n2.addAttribute( "long_name" , LN , Types.Char , rank1 );
    } 

    rank1[ 0 ] = 1;
    intval = new int[ 1 ];
    intval[ 0 ] = 1;
    n2.addAttribute( "signal" , intval , Types.Int , rank1 ); 
    float[] xvals = new float[ 0 ];
    xvals = DS.getData_entry(datablock).getY_values();
    rank1[ 0 ] = xvals.length;
      
    n2.setNodeValue( xvals , Types.Float , rank1 );
    //--------------------- error node ---------------------------

    NxWriteNode n3 = node.newChildNode( "errors" , "SDS" );
    units = DS.getX_units();
    if( units != null ){
      rank1[ 0 ] = units.length( ) + 1;
      n3.addAttribute("units",(units+(char)0).getBytes(),Types.Char,rank1);
    }

    rank1[ 0 ] = 1;
    intval = new int[ 1 ];
    intval[ 0 ] = 1;
    n3.addAttribute( "signal" , intval , Types.Int , rank1 ); 
    xvals = new float[ 0 ];
    xvals = DS.getData_entry(datablock).getErrors();
    rank1[ 0 ] = xvals.length;
    n3.setNodeValue( xvals , Types.Float , rank1 ); 
   
    //range[2]
    //---------------- Other Attributes --------------------------  
    XScale uxs = DS.getData_entry(datablock).getX_scale();
    new NxWriteDetector(instrType).processDS( node,DS,datablock,datablock+1);
    float[] range = new float[2];
    range[0]= uxs.getStart_x();
    range[1] = uxs.getEnd_x();
    NxWriteNode nrange = node.newChildNode("range","SDS");
    nrange.setNodeValue(range,Types.Float,
                        Inst_Type.makeRankArray(2,-1,-1,-1,-1));
    nrange.addAttribute("units",(DS.getX_units()+(char)0).getBytes(),
              Types.Char,
              Inst_Type.makeRankArray(DS.getX_units().length()+1,-1,-1,-1,-1));

          
    return false;

  }
}
