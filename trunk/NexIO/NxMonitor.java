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
 * Revision 1.11  2007/07/11 17:56:13  rmikk
 * Added white space.
 * Collapses some lines using Utility routines that can chain with null values
 * Fixed the specified ID's
 *
 * Revision 1.10  2007/07/04 18:04:04  rmikk
 * Fixed reading of errors.  Also added a PixelInfo attribute to monitor data
 *    blocks
 *
 * Revision 1.9  2004/02/16 02:15:55  bouzekc
 * Removed unused import statements.
 *
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
import NexIO.Util.*;
import gov.anl.ipns.MathTools.Geometry.*;
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
    float histOff = ConvertDataTypes.floatValue( ntof.getAttrValue( "histogram_offset"));
    
    if( histOff < 0)
       histOff = 0;
    
    float[] errs = null;
    if( errors != null) 
       errs = ConvertDataTypes.floatArrayValue( errors.getNodeValue());
    
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
    
    if( histOff > 0){ //Calculate Histogram boundaries
       float[] xvals1 = new float[xvals.length + 1];
       float xleft = xvals[0]-histOff;
       for( int i=0; i <xvals.length; i++ ){
          xvals1[i] = xleft;
          xleft = 2*xvals[i]-xleft;
       }
       xvals1[ xvals1.length-1] = xleft;
       xvals = xvals1;
    }
    
    if( ( xvals == null ) ||(  yvals==null ) ) 
      return setErrorMessage( "Cannot convert data to float array in NxMonitor");
    
    float TotalCount =0;
    for( int i=0; i< yvals.length; i++)
    	TotalCount +=yvals[i];
    
    DS.setAttribute(new FloatAttribute( Attribute.TOTAL_COUNT, TotalCount));
    
    String Xlabel = NexUtils.getStringAttributeValue( ntof, "long_name");
    if( Xlabel != null)
       DS.setX_label( Xlabel );
    

    String Xunits = NexUtils.getStringAttributeValue( ntof, "units");
    
    
    String Ylabel = NexUtils.getStringAttributeValue( ndata, "long_name");
    if( Ylabel != null)
       DS.setY_label( Ylabel );
    

    String Yunits = NexUtils.getStringAttributeValue( ndata, "units");
    if( Yunits != null)
       DS.setY_units( Yunits );
    
    
    Data D;
    if( Xunits != null )
       ConvertDataTypes.UnitsAdjust( xvals , Xunits , "meters" ,1f , 0f );
    if( errs == null)
       D = Data.getInstance(new VariableXScale(xvals),yvals, monitor_num);
    else
      D = Data.getInstance( new VariableXScale(xvals),yvals, errs, monitor_num);
 
    D.setSqrtErrors( true );
    DS.addData_entry( D );
    int index = DS.getNum_entries()-1;
    
   
    
    (new NXData_util()).setOtherAttributes( node  ,DS , index ,index+1) ;
  
    //-------- determine non-default GroupID ---------------------
    int[] ids = ConvertDataTypes.intArrayValue( NexUtils.getSubNodeValue( node, "id"));
    
    if( ids == null)
       ids = ConvertDataTypes.intArrayValue( NexUtils.getSubNodeValue( node, "detector_number"));
    
    if( ids != null && ids.length > 0)
       D.setGroup_ID( ids[0]);
    else
       D.setGroup_ID( monitor_num);
    
    
    //-------------Create a PixelInfo list for this entry-----------------
    float distance = ConvertDataTypes.floatValue( 
                            NexUtils.getFloatFieldValue( node,"distance"));
    
    if( Float.isNaN( distance)){
       return false;
    }
    float width = Float.NaN,
          height = Float.NaN,
          depth = Float.NaN;
    NxNode Geom = node.getChildNode("goemetry");
    if(Geom != null && Geom.getNodeClass() == "NxGeometry"){
       NxNode shape = Geom.getChildNode("shape");
       if( shape != null){
          float[] size = NexUtils.getFloatArrayFieldValue( shape,"size");
          if( size != null && size.length == 3){
             width = size[0];
             height = size[1];
             depth = size[2];
          }
       }
    }
    
    if( Float.isNaN(width))
       width =ConvertDataTypes.floatValue( NexUtils.getFloatFieldValue( node,
                                                                     "width"));

    if( Float.isNaN(height))
       height =ConvertDataTypes.floatValue( NexUtils.getFloatFieldValue( node,
                                                                    "height"));

    if( Float.isNaN(depth))
       depth =ConvertDataTypes.floatValue( NexUtils.getFloatFieldValue( node,
                                                                     "depth"));
    
    if( Float.isNaN( height))
       height = .05f;
    if( Float.isNaN( width))
       width = .05f;
    if( Float.isNaN( depth))
       depth = .05f;
    Vector3D center = new Vector3D( distance,0,0);
    Vector3D xdir = new Vector3D(1,0,0);
    Vector3D ydir = new Vector3D( 0,1,0);

    int id = ConvertDataTypes.intValue( NexUtils.getFloatFieldValue( node,
                                                           "detector_number"));
    if( id < 0)
       id = D.getGroup_ID();
       
    if( id < 0)
       id = index+1;
    UniformGrid grid = new UniformGrid(id,"m",center, xdir ,ydir, width, 
                                                          height, depth,1,1);
    
    DetectorPixelInfo detPix = new DetectorPixelInfo( id, (short)1,(short)1, 
                                                                     grid);
    
    D.setAttribute( new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST,
                                                 new PixelInfoList( detPix)));
    
    D.setAttribute(  new DetPosAttribute( Attribute.DETECTOR_POS, 
             new DetectorPosition( new Vector3D(distance,0f,0f))) );
    float Totcount = 0;
    if( yvals != null && D.getAttribute(  Attribute.TOTAL_COUNT)== null){
       
       for( int i=0; i< yvals.length;i++)
          Totcount +=yvals[i];
       
       D.setAttribute(  new FloatAttribute( Attribute.TOTAL_COUNT, Totcount) );
    }
    
    return false;
  }
  
}
