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
 * Revision 1.8  2003/12/15 14:40:30  rmikk
 * Eliminated some commented out code
 *
 * Revision 1.7  2003/11/24 00:05:51  rmikk
 * Made error messages include the work NXmonitor
 * Added the error fields to the monitor
 *
 * Revision 1.6  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/11/20 16:14:48  pfpeterson
 * reformating
 *
 * Revision 1.4  2002/04/01 20:28:37  rmikk
 * Eliminated unused code
 *
 * Revision 1.3  2002/03/13 16:24:23  dennis
 * Converted to new abstract Data class.
 *
 */

package NexIO;

import DataSetTools.dataset.*;
import NexIO.*;
import DataSetTools.math.*;
import java.lang.reflect.*;
import NexIO.Util.*;
/** 
 * This Class processes the NxMonitor Entries of a Nexus datasource
 */
public class NxMonitor{
  String errormessage;
  int monitor_num;

  public NxMonitor(){
    errormessage = "";
    monitor_num = -1;
  }

  /**
   * Returns error and warning messages or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }

  public void setMonitorNum( int num ){
    monitor_num = num;
  }

  public int getMonitorNum(){
    return monitor_num;
  }
  private boolean setErrorMessage( String error){
     errormessage = error;
     return true;
  }
  /**
   * Fills out an existing DataSet with information from the NXmonitor
   * section of a Nexus datasource
   *
   * @param node the current node positioned to an NXmonitor part of a
   * datasource
   * @param DS the existing DataSet that is to be filled out
   *
   * @return error status: true if there is an error otherwise false
   */
  public boolean processDS( NxNode node ,  DataSet DS ){
    errormessage = "null inputs in NxMonitor" ;
    if( node == null ) 
      return true;
    if( DS == null )
      return true;
    NxNode ntof , 
            ndata;
    ntof = node.getChildNode( "time_of_flight" );
    ndata = node.getChildNode( "data" );
    NxNode errors = node.getChildNode("errors");
    errormessage ="";
    if( ( ntof == null )||( ndata == null ) ){
      errormessage = "Cannot find the Monitor data here in NxMonitor";
      return true;
    }
   
    Object X1 = ndata.getNodeValue();
    Object X2 = ntof.getNodeValue();
    float[] errs = null;
    if( errors != null) errs = ConvertDataTypes.floatArrayValue( errors.getNodeValue());
    if( X1 == null ) 
      return setErrorMessage( "no values for data node in NxMonitor");
    if( X2 == null ) 
      return setErrorMessage( "no values in time of flight node in NxMonitor");
    NXData_util nd = new NXData_util();
    NxData_Gen nds = new NxData_Gen();
    float yvals[];
    yvals = nd.Arrayfloatconvert( X1 );
    float xvals[];
    xvals = nd.Arrayfloatconvert( X2 );
    if( ( xvals == null ) ||(  yvals==null ) ) 
      return setErrorMessage( "Cannot convert data to float array in NxMonitor");
    
    Data D;
    if( errs == null)
      D = Data.getInstance(new VariableXScale(xvals),yvals, monitor_num+1);
    else
      D = Data.getInstance( new VariableXScale(xvals),yvals, errs, monitor_num+1);

    DS.addData_entry( D );
    int index=DS.getNum_entries()-1;
    Object val = ntof.getAttrValue( "long_name" );
    if( val != null ){
      String S = nds.cnvertoString( val );
      if( S != null )
        DS.setX_label( S );
    }
    val = ntof.getAttrValue( "units" );
    if( val != null ){
      String S = nds.cnvertoString( val );
      if( S != null )
        DS.setX_units( S );
    }  
    val = ndata.getAttrValue( "long_name" );
    if( val != null ){
      String S = nds.cnvertoString( val );
      if( S != null )
        DS.setY_label( S );
    }       
    val = ndata.getAttrValue( "units" );
    if( val != null ){
      String S = nds.cnvertoString( val );
      if( S != null )
        DS.setY_units( S );
    } 
    (new NXData_util()).setOtherAttributes( node  ,DS , index ,index+1) ;
    
    return false;
  }
  
}
