
/*
 * File:  NexUtils.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.2  2003/11/23 23:51:31  rmikk
 * Eliminated some debugging prints
 * DataSets are not saved as Grids unless there is more than 1 row
 *   or more than 1 column
 *
 * Revision 1.1  2003/11/16 21:48:02  rmikk
 * Initial Checkin
 *
 */

package NexIO.Util;
import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import java.util.*;
import DataSetTools.util.*;

/**
  *  This class contains the Generic implementations of the methods in 
  *  the interface INexUtils. Also it has other utility routines related
  *  to NeXus files.
  *  @see INexUtils
  */
public class NexUtils implements INexUtils{
 
  public String xUnits = "us";
  public String angleUnits  ="radians";

  String errormessage = "";
  /**
  *  return the NXdetector in NxInstrument with the given Name, LinkName. 
  *  If there is no node, null is returned.
  */
  public static NxNode getCorrespondingNxDetector( String LinkName, 
                                                    NxNode NxInstrumentNode)
    {
     if( LinkName == null)
        return null;
     if( NxInstrumentNode == null)
        return null;
     return NxInstrumentNode.getChildNode( LinkName.trim());
    }

  /**
    *  Currently invoked by the standard Process1Nxdata. It adds all the 
    *  data from the data field of NXdetector to the DataSet DS 
    *  @param DS  the DataSet that is having information added to
    *  @param NxDataNode The NxNode containing information about an NeXus
    *                      NXdata class
    *  @param NxDetector The NxNode containing information about an NeXus
    *                      NXdetector class
    *  @param startDSindex  the first GroupIndex to process
    *  @param States   The linked list of state information
    *  This method sets up grids with default ID's of 1,2,3,...
    */
   public  boolean setUpNXdetectorAttributes( DataSet DS,NxNode NxDataNode, 
       NxNode NxDetector, int startDSindex, NxfileStateInfo States){
    
     NxDataStateInfo dataState = NexUtils.getDataStateInfo( States);
     NxDetectorStateInfo detState = NexUtils.getDetectorStateInfo( States);
     if( dataState == null)
        return setErrorMessage( " No state for NXdata "+NxDataNode.getNodeName());
     if( detState == null)
        return setErrorMessage( " No state for NXdector "+NxDetector.getNodeName());

     
        
     float[] crate = NexUtils.getFloatArrayFieldValue( NxDetector, "crate");    
     float[] slot =  NexUtils.getFloatArrayFieldValue( NxDetector, "slot");   
     float[] input = NexUtils.getFloatArrayFieldValue( NxDetector, "input"); 
     float[] solAng = NexUtils.getFloatArrayFieldValue( NxDetector, "solid_angle");
     
     float[] width=null, height=null, depth = null, orientation = null;
     NxNode dd =NxDetector.getChildNode("width");
     if( dd != null){ 
        width  =ConvertDataTypes.floatArrayValue(dd.getNodeValue());
        ConvertDataTypes.UnitsAdjust(width, (String)dd.getAttrValue("units"),"m",
        1.0f,0.0f); 
     }
     
     dd =NxDetector.getChildNode("height");
     if( dd != null){ 
        height  =ConvertDataTypes.floatArrayValue(dd.getNodeValue());
        ConvertDataTypes.UnitsAdjust(height, (String)dd.getAttrValue("units"),"m",
        1.0f,0.0f); 
     }
     dd =NxDetector.getChildNode("depth");
     if( dd != null){ 
        depth  =ConvertDataTypes.floatArrayValue(dd.getNodeValue());
        ConvertDataTypes.UnitsAdjust(depth, (String)dd.getAttrValue("units"),"m",
        1.0f,0.0f); 
     }
     dd =NxDetector.getChildNode("orientation");
     
     if( dd != null){ 
        orientation  =ConvertDataTypes.floatArrayValue(dd.getNodeValue());
        ConvertDataTypes.UnitsAdjust(orientation,(String)dd.getAttrValue("units"),"rad",
        1.0f,0.0f); 
        
     }
     int[] ids   = NexUtils.getIntArrayFieldValue( NxDetector, "id");
              
     int NGroups = getNGroups( dataState.dimensions);
     if( NGroups < 0)
        return setErrorMessage(" Improper dimensions in " +
                                        NxDataNode.getNodeName());
     NxNode dist = NxDetector.getChildNode( "distance");
     int[] distDimensions = dist.getDimension();
     NxNode az = null;
     float[] distance, azimuth, polar;
     distance = azimuth=polar = null;
     if( dist != null){
        distance = ConvertDataTypes.floatArrayValue(dist.getNodeValue());
        String Xunits = ConvertDataTypes.StringValue( dist.getAttrValue("units"));
        
        if( distance != null){
              ConvertDataTypes.UnitsAdjust( distance, Xunits,"m",1.0f,0.0f);
              az = NxDetector.getChildNode( "azimuthal_angle");
              if( az != null){
                 String units = ConvertDataTypes.StringValue( az.getAttrValue("units"));
                 azimuth =ConvertDataTypes.floatArrayValue(az.getNodeValue());
                 ConvertDataTypes.UnitsAdjust( azimuth, Xunits,"radians",1.0f,0.0f);
                 az = NxDetector.getChildNode( "polar_angle");
                 if( az != null){
                    units = ConvertDataTypes.StringValue( az.getAttrValue("units"));
                    polar=ConvertDataTypes.floatArrayValue(az.getNodeValue());
                    ConvertDataTypes.UnitsAdjust( polar, Xunits,"radians",1.0f,0.0f);
                 }
              }

           }
        if(az == null)
           distance = azimuth = polar = null;
     }//dist != null
     if( (distance == null) || (azimuth == null) || (polar == null))
         distance = azimuth=polar = null;
     else if( (distance.length != azimuth.length) || (distance.length != polar.length))
         distance = azimuth=polar = null;
     for( int i = startDSindex; i < DS.getNum_entries(); i++){
        Data db = DS.getData_entry( i);
        int TotPos = i-startDSindex;
        if( States.Spectra != null)
           TotPos = States.Spectra[ i-startDSindex];
        setFloatAttr( db, Attribute.CRATE, crate, TotPos);
        setFloatAttr( db, Attribute.INPUT, input, TotPos);
        setFloatAttr( db, Attribute.SLOT, slot, TotPos);
        setFloatAttr( db, Attribute.SOLID_ANGLE , solAng, TotPos);
        if( ids != null)
           if( ids.length >= TotPos)
              db.setGroup_ID( ids[TotPos]);
        /*if( distance != null){
           DetectorPosition dp = ConvertDataTypes.convertToIsaw( 
              distance[TotPos], polar[TotPos],azimuth[TotPos]);
           db.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS,dp));
        }*/
     }
     
    //------------------ set up grids and pixel info list attributes ---------
     int ngrids, ngrid_dimensions, nrows, ncols;
     if( distance == null)
        return false;
     int startGridNum = 1+ Maxx(DS, startDSindex);//Grid_util.getAreaGridIDs(DS));
     int nDataDims, nDistDims;
     if( dataState == null){
         System.out.println("dataState is null");
         return false;
     }
     if( distDimensions == null){
         System.out.println("distDimensions is null");
         return false;
     }
     nDataDims = dataState.dimensions.length;
     nDistDims = distDimensions.length;
     if( (nDistDims +3 < nDataDims) ||( nDistDims +1 > nDataDims))
        return setErrorMessage("Dimensions of the data and positions are out of line");
     ncols = 1;
     nrows = 1;
     int c = 0;
     if( nDistDims ==1) if( distDimensions[0] == 1) nDistDims --;
     if( nDataDims -1 -nDistDims >=2){
        ncols = dataState.dimensions[ nDistDims+1 ];
        nrows = dataState.dimensions[ nDistDims ];
     
     }else if( nDataDims -1 -nDistDims >=1)
        nrows = dataState.dimensions[ nDistDims  ];
     
     ngrids = (DS.getNum_entries() -startDSindex)/nrows/ncols;
     int row = 1, col= 1, grid =0;
     UniformGrid Grid = null;
     Tran3D Matrix ;
     for( int i = startDSindex; i < DS.getNum_entries(); i++){
        Data db = DS.getData_entry( i);
        if( (row ==1) && ( col ==1)&&((nrows>1) ||(ncols > 1))){  //set up new grid
           if( Grid != null){
              Grid.setData_entries( DS);
           }
          
           DetectorPosition center = ConvertDataTypes.convertToIsaw( 
                    distance[grid],polar[grid], azimuth[grid]);
           //System.out.println("rotate xvec:" +orientation);
           Vector3D x_dir = Rotate( orientation,grid, 0,-1,0);
           //System.out.println("rotate yvec:");
           Vector3D y_dir = Rotate( orientation, grid, 0,0,1);
           Grid = new UniformGrid(startGridNum+grid,"m", new Vector3D(center), 
                  x_dir, y_dir, Aval(width,grid), 
                  Aval(height,grid), Aval(depth,grid),nrows, ncols);
        }
        if( Grid != null){
          DetectorPixelInfo detPix = new DetectorPixelInfo(startGridNum+grid,
                                          (short)row,(short)col, Grid);
          DetectorPixelInfo[] piList = new DetectorPixelInfo[1];
          piList[0] = detPix;
          db.setAttribute(  
               new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST, 
                        new PixelInfoList(piList)));
           db.setAttribute( new DetPosAttribute(Attribute.DETECTOR_POS,
                     new DetectorPosition(Grid.position(row,col) )  ));
        }else 
          ConvertDataTypes.addAttribute( db, ConvertDataTypes.CreateDetPosAttribute(
                Attribute.DETECTOR_POS, ConvertDataTypes.convertToIsaw( distance[grid],
                    polar[grid], azimuth[grid])));
          
        col++;
        if( col > ncols){
           col = 1;
           row++;
           if( row > nrows){
              row = 1;
              grid++;
           }
        }

     }
     
     
      
     return false;
   }

  private Vector3D Rotate( float[] orientations, int grid, float x,float y,
                                             float z){
     //System.out.println( "    orientations="+StringUtil.toString(orientations));
     if( orientations == null)
        return new Vector3D( x,y,z);
     if( orientations.length < 3)
        return new Vector3D( x,y,z);
 
     int p = 3*grid;
     if( p+2 >=orientations.length){
        p = orientations.length/3-1;
     }

     Tran3D Matrix = new Tran3D();
     Matrix.setRotation( (float)(orientations[p]*180/java.lang.Math.PI), 
                        new Vector3D( 1,0,0));
     

     Vector3D res = new Vector3D();
     Tran3D next = new Tran3D();
     next.setRotation( (float)(orientations[p+1]*180/java.lang.Math.PI), 
                              new Vector3D(0,1,0));
     next.multiply_by(Matrix);
     Matrix=next;
     

     next = new Tran3D();
     next.setRotation( (float)(orientations[p+2]*180/java.lang.Math.PI), 
          new Vector3D(1,0,0));
     next.multiply_by(Matrix);
     Matrix=next;
    
     Matrix.apply_to( new Vector3D( x,y,z), res);
     return res;
     
  }

  /**
  *     Grid_util.getAreaGridIDs does NOT WORK if each detector has
  *     only one row and one column
  */
  private int Maxx( DataSet DS, int maxIndex){
     int res = 0;
     for( int i=0; i< maxIndex; i++){
        Data db = DS.getData_entry( i);
        Object v = db.getAttributeValue( Attribute.PIXEL_INFO_LIST);
        if( v != null){
           IPixelInfo ipInf =((PixelInfoList)v).pixel(0);
           if( ipInf != null)
             if( ipInf.gridID()> res)
                res = ipInf.gridID();

        }
     }
    return res;

  }

  private int Maxx( int[] array){
     if( array == null)
        return 0;
     int res = 0;
     for( int i = 0; i< array.length; i++)
        if( array[i] > res)
           res = array[i];

     return res;

  }


  private float Aval( float[] array, int index){
     if( array == null)
        return 0.01f;
     if( array.length < 1)
        return 0.01f;
     if( index < 0)
        return 0.01f;
     if( index > array.length)
        return array[ array.length-1];
     return array[index];   
  }


  /**
    *  Currently invoked by the standard ProcessNXentry. It adds all the 
    *  data the NXmonitor class to the DataSet DS 
    *  @param DS  the DataSet that is having information added to
    *  @param NxMonitorNode The NxNode containing information about an NeXus
    *                      NXmonitor class
    *  @param startDSindex  the first GroupIndex to process
    *  @param States   The linked list of state information
    */
   public  boolean setUpNXmonitorAttributes( DataSet DS, NxNode NxMonitorNode, 
      int startDSindex, NxfileStateInfo States){
       Data DB = DS.getData_entry( startDSindex);
       if( DB == null)
         return setErrorMessage("No Data block "+startDSindex);
        
      
       float x =NexUtils.getFloatFieldValue( NxMonitorNode, "distance").floatValue();
       if( ! Float.isNaN(x))
          ConvertDataTypes.addAttribute( DB, new DetPosAttribute(
               Attribute.DETECTOR_POS, new DetectorPosition(
                  new Position3D( new Vector3D( x,0f,0f))) ));
       
     return false;
   }


 
  /**
    *  Currently invoked by the standard Process1Nxdata. It adds all the 
    *  data from the data field of NXdata to the DataSet DS using default
    *  GroupID's.  Units are changed to ISaw units
    *  @param DS  the DataSet that is having information added to
    *  @param NxDataNode The NxNode containing information about an NeXus
    *                      NXdata class
    *  @param startGroupID  the default GroupID for the first new DataBlock 
    *                       added by this method
    *  @param States   The linked list of state information
    */
  public boolean setUpNxData( DataSet DS, NxNode NxDataNode, 
          int startGroupID,  NxfileStateInfo States){

     int startDSindex = DS.getNum_entries();
     NxDataStateInfo DataInf = NexUtils.getDataStateInfo( States);
     if( DataInf == null)
        return setErrorMessage("No State info for NXdata "+NxDataNode.getNodeName());
     NxNode dataNode = NxDataNode.getChildNode( "data");
     int[] dimensions = dataNode.getDimension();
     int NGroups =getNGroups(  dimensions);
     
     if( NGroups < 0)
        return setErrorMessage(" Improper Dimensions for NXdata "+ 
                             NxDataNode.getNodeName());
     int length = dimensions[ dimensions.length -1];
     float[] data = ConvertDataTypes.floatArrayValue(dataNode.getNodeValue());
     String YUnits = ConvertDataTypes.StringValue(
                                      dataNode.getAttrValue("units"));
     
     if( data == null) return setErrorMessage( "No data in NxData");
     if( (DataInf.axisName == null) || (DataInf.axisName.length < 1) ) 
        return setErrorMessage( "No axis 1 information in NXnode "+ 
                      NxDataNode.getNodeName());
     NxNode tofNode = NxDataNode.getChildNode( DataInf.axisName[0]);
     if( tofNode == null)
        return setErrorMessage("No tof axis named "+ DataInf.axisName[0] +
                 "in SetupNxData");
     float[] xvals = ConvertDataTypes.floatArrayValue( tofNode.getNodeValue());
     xvals = MakeHistogram( xvals , tofNode);
     String Xunits =ConvertDataTypes.StringValue(
                                      tofNode.getAttrValue("units")); 

     ConvertDataTypes.UnitsAdjust( xvals, Xunits,xUnits,1.0f,0.0f);

     NxNode errNode = NxDataNode.getChildNode( "errors");
     float[] evals = null;
     
     if( errNode != null){
        evals = ConvertDataTypes.floatArrayValue( errNode.getNodeValue());
        evals = MakeHistogram( evals , errNode);
        Xunits =ConvertDataTypes.StringValue(
                                       errNode.getAttrValue("units")); 

        ConvertDataTypes.UnitsAdjust( evals, Xunits,xUnits,1.0f,0.0f);
     }
     
     if( xvals == null)
        return setErrorMessage( "no x values");
     
    
     if( xvals.length != length)
        if( xvals.length != length+1)
          return setErrorMessage("Improper length for axis 1");
    
     XScale xsc = new VariableXScale( xvals);

     for( int i = 0; i < NGroups; i++){
        int id = startGroupID+i;  //will have NXdetector change groupID
        if( (States.Spectra == null) ||
             (Arrays.binarySearch(States.Spectra, id )>=0)){
           float[] yvals = new float[length];
           System.arraycopy( data, i*length, yvals,0, length);
           HistogramTable DB = new HistogramTable(xsc,yvals,evals,id);
           DS.addData_entry( DB);
        }
        startGroupID++;
     }  
     
     return false;
   }

  private int getGroupsID( int[] IDS,int startGroupID,int offset){
     if( IDS == null)
        return startGroupID + offset;
     if( IDS.length <= offset)
        return startGroupID + offset;
     if( offset < 0)
        return startGroupID + offset;
     return IDS[ offset ];
  }
  //public void setUpNX...Attributes, where ...=Beam,Sample, etc.


  public static Object getSubNodeValue( NxNode parentNode, String subNodeName){
     NxNode subNode = parentNode.getChildNode( subNodeName);
     if( subNode == null)
        return null;
     return subNode.getNodeValue();
  }



  private boolean setErrorMessage( String err){
     errormessage = err;
     return true;
  }

  /**
   *  returns an errormessage or an empty string if there is no error 
   */
  public String getErrorMessage(){
     return errormessage;
   }


  /**
   *    returns null if no Attribute Name or cannot convert to Integer
   */
  public static Integer getIntAttributeValue( NxNode Node, String AttributeName){
     if( Node == null)
        return null;
     if( AttributeName == null)
        return null;
     Object Val = Node.getAttrValue( AttributeName);
     int n =ConvertDataTypes.intValue( Val);
     if( n == Integer.MIN_VALUE)
        return null;
     return new Integer( n);
  }


  /**
   *    returns null if no Attribute Name or cannot convert to Float
   */
  public static Float getFloatAttributeValue( NxNode Node, String AttributeName){
     if( Node == null)
        return null;
     if( AttributeName == null)
        return null;
     Object Val = Node.getAttrValue( AttributeName);
     float f=  ConvertDataTypes.floatValue( Val);
     if( Float.isNaN(f))
        return null;
     return new Float( f);

  }


  /**
   *    returns null if no Attribute Name or cannot convert to String
   */
  public static String getStringAttributeValue( NxNode Node, String AttributeName){
     if( Node == null)
        return null;
     if( AttributeName == null)
        return null;
     Object Val = Node.getAttrValue( AttributeName);
     return  ConvertDataTypes.StringValue( Val);

  }    
  
  /**
   *    returns null if no Field Name or cannot convert to Integer
   */
  public static Integer getIntFieldValue( NxNode Node, String FieldName){
     if( Node == null)
        return null;
     if( FieldName == null)
        return null;
     NxNode Child = Node.getChildNode( FieldName);
     if( Child == null)
        return null;
     int n= ConvertDataTypes.intValue( Child.getNodeValue());

     if( n == Integer.MIN_VALUE)
        return null;
     return new Integer( n);

  }
/**
   *    returns null if no Field Name or cannot convert to Integer
   */
  public static int[] getIntArrayFieldValue( NxNode Node, String FieldName){
     if( Node == null)
        return null;
     if( FieldName == null)
        return null;
     NxNode Child = Node.getChildNode( FieldName);
     if( Child == null)
        return null;
     return ConvertDataTypes.intArrayValue( Child.getNodeValue());


  }


  /**
   *    returns null if no Field Name or cannot convert to Float
   */
  public static Float getFloatFieldValue( NxNode Node, String FieldName){
     if( Node == null)
        return null;
     if( FieldName == null)
        return null;
     NxNode Child = Node.getChildNode( FieldName);
     if( Child == null)
        return null;
     float f=  ConvertDataTypes.floatValue( Child.getNodeValue());

     if( Float.isNaN(f))
        return null;
     return new Float( f);

  }

 /**
   *    returns null if no Field Name or cannot convert to Float
   */
  public static float[] getFloatArrayFieldValue( NxNode Node, String FieldName){
     if( Node == null)
        return null;
     if( FieldName == null)
        return null;
     NxNode Child = Node.getChildNode( FieldName);
     if( Child == null)
        return null;
     return ConvertDataTypes.floatArrayValue( Child.getNodeValue());
  }


  /**
   *    returns null if no Field Name or cannot convert to String
   */
  public static String getStringFieldValue( NxNode Node, String FieldName){
     if( Node == null)
        return null;
     if( FieldName == null)
        return null;
     NxNode Child = Node.getChildNode( FieldName);
     if( Child == null)
        return null;
     return ConvertDataTypes.StringValue( Child.getNodeValue());

  }    


  private void setFloatAttr( Data db, String AttributeName, float[] crate,
          int TotPos){
     if( crate == null)
        return;
     if( db == null)
        return;
     if( AttributeName == null)
        return;
     if( TotPos < 0)
        return;
     if( crate.length <= TotPos)
        return;
     db.setAttribute( new FloatAttribute( AttributeName, crate[TotPos]));

  }

  /**
   *    Makes a histogram from function values. tofNode has an attribute
   *    histogram_offset that can be used to get the left boundary
   */
  public static float[] MakeHistogram( float[] xvals , NxNode tofNode){
     if( xvals == null)
        return null;
     float histOffset = ConvertDataTypes.floatValue(
                          tofNode.getAttrValue( "histogram_offset"));
     if( Float.isNaN( histOffset))
        return xvals;
     if( histOffset == 0)
        return xvals;

     float[] Res = new float[ xvals.length+1];
     
     float left = xvals[0] - histOffset;
     Res[0] = left;
     for( int i = 0; i < xvals.length; i++){
        float right = xvals[i]-left +xvals[i];
        Res[i+1] = right;
        left = right;
     }   
     return Res;
  }
  
  
  private int getNGroups( int[] dimension){
     if( dimension == null)
        return -1;
     if( dimension.length < 1) 
        return -1;
     int NGroups = 1;
     for( int i = 0; i < dimension.length-1; i++)
        NGroups *=dimension[i];
     return NGroups;

  }

 /**
  *   Returns the first NxDataStateInfo in the fileState link list of state
  *   information
  */
 public static NxDataStateInfo getDataStateInfo( NxfileStateInfo fileState){
     if( fileState == null)
        return null;
     StateInfo inf = fileState.getNext();
     if( inf == null)
        return null;
     if( inf instanceof NxDataStateInfo)
        return (NxDataStateInfo)inf;
     inf = inf.getNext();
     if( inf == null)
        return null;
     if( inf instanceof NxDataStateInfo)
        return (NxDataStateInfo)inf;
     inf = inf.getNext();
     if( inf == null)
        return null;
     if( inf instanceof NxDataStateInfo)
        return (NxDataStateInfo)inf;
     return null;
    
     

 }

 
 /**
  *   Returns the first NxDetectorStateInfo in the fileState link list of state
  *   information
  */
 public static NxDetectorStateInfo getDetectorStateInfo( NxfileStateInfo fileState){
     if( fileState == null)
        return null;
     StateInfo inf = fileState.getNext();
     if( inf == null)
        return null;
     if( inf instanceof NxDetectorStateInfo)
        return (NxDetectorStateInfo)inf;
     inf = inf.getNext();
     if( inf == null)
        return null;
     if( inf instanceof NxDetectorStateInfo)
        return (NxDetectorStateInfo)inf;
     inf = inf.getNext();
     if( inf == null)
        return null;
     if( inf instanceof NxDetectorStateInfo)
        return (NxDetectorStateInfo)inf;
     return null;


 }
}
