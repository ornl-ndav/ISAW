/*
 * File:  Util.java 
 *
 * Copyright (C) 2003, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.21  2007/04/05 18:47:14  rmikk
 * Added a static method to write an orientation matrix given just an orientation
 *   matrix
 *
 * Revision 1.20  2005/01/10 15:30:45  dennis
 * Removed empty statement.
 *
 * Revision 1.19  2004/06/16 19:56:21  pfpeterson
 * Fixed bug that appears in createIdMap when not using a square (in pixels)
 * detector. This was discovered using by Gayle Green using an SNS detector
 * on QUIP.
 *
 * Revision 1.18  2004/04/12 21:31:29  dennis
 * The method to get the detector ID now will first try to get
 * the ID from the data blocks PixelInfoList.
 *
 * Revision 1.17  2004/03/15 03:28:39  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.16  2004/01/24 20:31:16  bouzekc
 * Removed/commented out unused variables/imports.
 *
 * Revision 1.15  2003/12/15 02:38:18  bouzekc
 * Removed unused imports.
 *
 * Revision 1.14  2003/05/12 19:23:08  pfpeterson
 * Changed number of significant digits written to matrix file to
 * match fortran.
 *
 * Revision 1.13  2003/05/06 16:39:28  pfpeterson
 * Fixed small bug in detector_angle(DataSet,int) which was not checking
 * for detector number.
 *
 * Revision 1.12  2003/05/06 16:00:22  pfpeterson
 * Added new methods for determining information about data from two
 * detectors. Old methods are deprecated to ease debugging.
 *
 * Revision 1.11  2003/04/30 19:48:53  pfpeterson
 * Added methods to calculate lattice parameters for a given orientation
 * matrix and cell scalars for a given set of lattice parameters.
 *
 * Revision 1.10  2003/04/28 21:11:57  pfpeterson
 * Added ability to write orientation matrix to experiment files.
 *
 * Revision 1.9  2003/04/22 14:32:14  pfpeterson
 * Changed formatting when writting a matrix file.
 *
 * Revision 1.8  2003/04/21 14:15:02  pfpeterson
 * Changed detector_angle, and detector_distance to look at the attributes
 * of one of the spectra rather than the DataSet.
 *
 * Revision 1.7  2003/04/09 16:21:26  pfpeterson
 * Moved the code to write a matrix file here from BlindJ.
 *
 * Revision 1.6  2003/03/26 20:52:45  pfpeterson
 * Finished implementing the setting of reflection flags as disscussed
 * with A.Schultz.
 *
 * Revision 1.5  2003/03/20 21:56:54  pfpeterson
 * Centroid now deals with ArrayOutOfBoundsException in an appropriate manner.
 *
 * Revision 1.4  2003/02/13 17:03:44  pfpeterson
 * Methods to determine detector center angle and distance now try to
 * get attribute before calculating.
 *
 * Revision 1.3  2003/02/12 20:03:11  dennis
 * Switched to use PixelInfoList instead of SegmentInfoList
 *
 * Revision 1.2  2003/02/10 23:04:21  pfpeterson
 * Changed the constructor from being public and throwing an
 * InstantiationError to being private and doing nothing.
 *
 * Revision 1.1  2003/01/30 21:07:23  pfpeterson
 * Added to CVS.
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Position3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.Numeric.Format;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.io.FileWriter;
import java.io.IOException;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.IntAttribute;
import DataSetTools.dataset.IntListAttribute;
import DataSetTools.dataset.PixelInfoList;
import DataSetTools.dataset.PixelInfoListAttribute;
import DataSetTools.dataset.XScale;

public class Util{
  /**
   * Don't let anyone try to instantiate the class
   */
  private Util(){
  }

  /**
   * Determine the detector id from a given spectrum
   */
  static public int detectorID(Data data){
    Object obj = data.getAttributeValue( Attribute.PIXEL_INFO_LIST );
    if ( obj != null && obj instanceof PixelInfoList )
    {
      PixelInfoList pil = (PixelInfoList)obj; 
      if ( pil.num_pixels() > 0 )
        return pil.pixel(0).gridID();
    }

    // if no valid PixelInfoList, try to get the detector ID attribute
    Attribute attr=data.getAttribute(Attribute.DETECTOR_IDS);
    if( attr==null )
      return -1;
    else if(attr instanceof IntAttribute)
      return ((IntAttribute)attr).getIntegerValue();
    else if( attr instanceof IntListAttribute)
      return (((IntListAttribute)attr).getIntegerValue())[0];
    else
      return -1;
  }

  /**
   * Determine the detector center angle (in plane).
   * 
   * @deprecated use {@link #detector_angle(DataSet,int)
   * detector_angle} instead
   *
   * @return the in plane angle in degrees
   */
  static public float detector_angle(DataSet ds){
    return detector_angle(ds,detectorID(ds.getData_entry(0)));
  }

  /**
   * Determine the detector center angle (in plane).
   * 
   * @return the in plane angle in degrees
   */
  static public float detector_angle(DataSet ds, int detNum){
    Data data=null;
    int detID=-1;
    for( int i=0 ; i< ds.getNum_entries() ; i++ ){
      detID=-1;
      data=ds.getData_entry(i);
      if(data==null) continue;
      detID=detectorID(data);

      // stop looping b/c we found something
      if(detID==detNum) break;
    }
    if( data == null)
       return Float.NaN;
    Object attr_val=data.getAttributeValue(Attribute.DETECTOR_CEN_ANGLE);
    if(attr_val!=null && attr_val instanceof Float)
      return ((Float)attr_val).floatValue();

    PixelInfoListAttribute pil_attr =
          (PixelInfoListAttribute)data.getAttribute(Attribute.PIXEL_INFO_LIST);
    PixelInfoList pil  = (PixelInfoList)pil_attr.getValue();   
    Vector3D      vec  = pil.pixel(0).DataGrid().position();
    Position3D    position     = new Position3D( vec );
    float         cyl_coords[] = position.getCylindricalCoords(); 
    
    return (float)(cyl_coords[1]*180./Math.PI);
  }

  /**
   * Find the detector center angle (out of plane). This currently
   * just returns zero.
   *
   * @deprecated use {@link #detector_angle2(DataSet,int)
   * detector_angle2} instead
   *
   * @return the out of plane angle in degrees
   */
  static public float detector_angle2(DataSet ds){
    return 0f;
    /*Data data=ds.getData_entry(0); 

      PixelInfoListAttribute pil_attr =
      (PixelInfoListAttribute)data.getAttribute(Attribute.PIXEL_INFO_LIST);
      PixelInfoList pil  = (PixelInfoList)pil_attr.getValue();   
      Vector3D      vec  = pil.pixel(0).DataGrid().position();
      Position3D    position     = new Position3D( vec );
      float         sph_coords[] = position.getSphericalCoords(); 
    
      return (float)(90.-180.*sph_coords[2]/Math.PI);*/
  }

  /**
   * Find the detector center angle (out of plane). This currently
   * just returns zero.
   *
   * @return the out of plane angle in degrees
   */
  static public float detector_angle2(DataSet ds, int detID){
    return 0f;
  }

  /**
   * Determine the detector center distance from the sample
   *
   * @deprecated use {@link #detector_distance(DataSet,int)
   * detector_distance} instead
   *
   * @return the distance in cm
   */
  static public float detector_distance(DataSet ds,float avg_angle){
    // determine the detector id
    Data data=ds.getData_entry(0);
    if(data==null) return Float.NaN;
    int detID=detectorID(data);

    // get the detector distance
    if(detID==-1)
      return Float.NaN;
   
    return detector_distance(ds,detID);
  }
  
  /**
   * Determine the detector center distance from the sample
   *
   * @return the distance in cm
   */
  static public float detector_distance(DataSet ds,int detNum){
    // NOTE: avg_angle is no longer needed.
  
    PixelInfoListAttribute pil_attr;
    Data data=null;
    int detID=-1;
    for( int i=0 ; i<ds.getNum_entries() ; i++ ){
      detID=-1;
      data=ds.getData_entry(i);
      if(data==null) continue;
      detID=detectorID(data);

      // stop looping b/c we found something
      if(detID==detNum) break;
    }

    if(data==null) return Float.NaN;
   
    Object attr_val=data.getAttributeValue(Attribute.DETECTOR_CEN_DISTANCE);
    if(attr_val!=null && attr_val instanceof Float)
      return 100f*((Float)attr_val).floatValue();

    pil_attr =
          (PixelInfoListAttribute)data.getAttribute(Attribute.PIXEL_INFO_LIST);
    PixelInfoList pil  = (PixelInfoList)pil_attr.getValue();
    Vector3D      vec  = pil.pixel(0).DataGrid().position();
    float         dist = vec.length();

    return dist*100f;
  }

  /**
   * From the given dataset create a 2D array to map row and column to
   * id. This assumes that there is only one detector
   *
   * @deprecated use {@link #createIdMap(DataSet,int)
   * createIdMap} instead
   *
   * @return a 2D matrix of ids from detector column and row (in
   * order). Elements that do not have an id are set to -1.
   */
  public static int[][] createIdMap(DataSet ds){
    int detID=detectorID(ds.getData_entry(0));

    return createIdMap(ds,detID);
  }

  /**
   * From the given dataset create a 2D array to map row and column to
   * id. This assumes that there is only one detector
   *
   * @return a 2D matrix of ids from detector column and row (in
   * order). Elements that do not have an id are set to -1.
   */
  public static int[][] createIdMap(DataSet ds, int detID){
    Data data=null;
    int row,col;
    int rowMax=-1;
    int colMax=-1;
    int dataDetID=-1;

    // determine the maximum number of rows and columns
    Attribute attr=null;
    for( int i=0 ; i<ds.getNum_entries() ; i++ ){
      data=ds.getData_entry(i);
      if(detectorID(data)==detID){
        attr=data.getAttribute(Attribute.PIXEL_INFO_LIST);
        break;
      }
    }
    if( attr == null || !(attr instanceof PixelInfoListAttribute) )
      return null;

    PixelInfoList  pil  = (PixelInfoList)attr.getValue();
    IDataGrid      grid = pil.pixel(0).DataGrid();
 
    rowMax = grid.num_rows();
    colMax = grid.num_cols();
    // check that we got sensible values
    if( rowMax < 1 || colMax<1 ) return null;

    // initialize the return matrix
    int[][] ids = new int[colMax+1][rowMax+1];
    for( int i=0 ; i<=colMax ; i++ ){
      for( int j=0 ; j<=rowMax ; j++ ){
        ids[i][j]=-1;
      }
    }

    // create a reference 2D array to locate the proper group for the
    // given row and column
    for( int id=0; id<ds.getNum_entries(); id++){
      data=ds.getData_entry(id);
      dataDetID=detectorID(data);
      if(dataDetID!=detID) continue;
      attr = data.getAttribute(Attribute.PIXEL_INFO_LIST);
      if( attr!=null && attr instanceof PixelInfoListAttribute ){
        pil = (PixelInfoList)attr.getValue();
        row = (int)pil.row();
        col = (int)pil.col();
        if( (row>=0) && (col>=0) )
          ids[col][row] = id;
      }
    }

    return ids;
  }

  /**
   * Find the centroided location of a peak.
   *
   * @param peak the peak to centroid which already has an initial
   * position
   * @param ds the dataset to use for centroiding
   * @param ids a 2D array of ids where the indices are column and row
   * of the data block.
   */
  public static Peak centroid(Peak peak, DataSet ds, int[][] ids){
    
    double asum  = 0.;
    double xsum  = 0.;
    double ysum  = 0.;
    double zsum  = 0.;
    double back  = 0.;
    double count = 0.;
    
    // create the new reflection flag
    float x,y,z;
    int reflag=peak.reflag();
    reflag=(reflag/100)*100+reflag%10;
    
    // check that the peak isn't too close to the edge
    if( peak.nearedge()<=4.0f ){
      peak.reflag(reflag+20);
      return peak;
    }
    
    // create the surrounding area
    float[][][] surround=new float[7][7][3];
    int col= Math.round(peak.x());
    int row= Math.round(peak.y());
    int time=Math.round(peak.z());
    Data data=null;
    try{
      for( int i=col-3 ; i<=col+3 ; i++ ){
        for( int j=row-3 ; j<=row+3 ; j++ ){
          data=ds.getData_entry(ids[i][j]);
          if(data==null) return null;
          surround[i-col+3][j-row+3][0]=data.getY_values()[time-1];
          surround[i-col+3][j-row+3][1]=data.getY_values()[time  ];
          surround[i-col+3][j-row+3][2]=data.getY_values()[time+1];
        }
      }
    }catch(ArrayIndexOutOfBoundsException e){
      peak.reflag(reflag+20);
      return peak;
    }
                                              

    for( int k=0 ; k<3 ; k++ ){
      // determine the background for this time slice
      back=0.;
      for( int j=0 ; j<7 ; j++ ){
        for( int i=0 ; i<7 ; i++ ){
          if( i==0 || i==6 ){ // left and right borders
            back=back+surround[i][j][k];
          }else{
            if( j==0 || j==6 ){ // top and bottom borders
              back=back+surround[i][j][k];
            }
          }
        }
      }
      back=back/24f; // normalize the background by number of points
      // find the sums for the centroid
      for( int j=2 ; j<5 ; j++ ){
        for( int i=2 ; i<5 ; i++ ){
          count=surround[i][j][k]-back;
          xsum=xsum+count*(i+1.);
          ysum=ysum+count*(j+1.);
          zsum=zsum+count*(peak.z()+(double)k);
          asum=asum+count;
        }
      }
    }
    
    // total count must be greater than zero for this to make sense
    if(asum<=0){
      peak.reflag(reflag+20);
      return peak;
    }
    // centroid the peaks
    x=(float)(xsum/asum)+peak.x()-4f;
    y=(float)(ysum/asum)+peak.y()-4f;
    z=(float)(zsum/asum)-1f; // -1 is to convert to java counting
    
    // find out how far the peaks were moved
    float dx=Math.abs(x-peak.x());
    float dy=Math.abs(y-peak.y());
    float dz=Math.abs(z-peak.z());
    
    if( dx>1.0 || dy>1.0 || dz>1.0 ){
      // don't shift positions if it is moving more than one bin
      peak.reflag(reflag+20);
      return peak;
    }
      // update the peak
    peak.pixel(x,y,z);
     //IPeak Res = peak.createNewPeakxyz( x , y , z );
     //peak = Res;
    // return the updated peak
    peak.reflag(reflag+10);
    return peak;
  }

  /**
   * Find the centroided location of a peak.
   *
   * @param peak  the peak to centroid which already has an initial
   *              position
   * @param ds    the dataset to use for centroiding
   * @param grid  IDataGrid with references to the Data blocks for each
   *              column and row of an area detector.
   */
  public static IPeak centroid(IPeak peak, DataSet ds, IDataGrid grid){
    
    double asum  = 0.;
    double xsum  = 0.;
    double ysum  = 0.;
    double zsum  = 0.;
    double back  = 0.;
    double count = 0.;
    
    // create the new reflection flag
    float x,y,z;
    int reflag=peak.reflag();
    reflag=(reflag/100)*100+reflag%10;
    
    // check that the peak isn't too close to the edge
    if( peak.nearedge()<=4.0f ){
      peak.reflag(reflag+20);
      return peak;
    }
    
    // create the surrounding area
    float[][][] surround=new float[7][7][3];
    int col=  Math.round(peak.x());
    int row=  Math.round(peak.y());
    int time=  Math.round(peak.z());
    Data data=null;
    try{
      for( int i=col-3 ; i<=col+3 ; i++ ){
        for( int j=row-3 ; j<=row+3 ; j++ ){
          data=grid.getData_entry( j , i );//ds.getData_entry(ids[i][j]);
          if(data==null) return null;
          surround[i-col+3][j-row+3][0]=data.getY_values()[time-1];
          surround[i-col+3][j-row+3][1]=data.getY_values()[time  ];
          surround[i-col+3][j-row+3][2]=data.getY_values()[time+1];
        }
      }
    }catch(ArrayIndexOutOfBoundsException e){
      peak.reflag(reflag+20);
      return peak;
    }
                                              
    for( int k=0 ; k<3 ; k++ ){
      // determine the background for this time slice
      back=0.;
      for( int j=0 ; j<7 ; j++ ){
        for( int i=0 ; i<7 ; i++ ){
          if( i==0 || i==6 ){ // left and right borders
            back=back+surround[i][j][k];
          }else{
            if( j==0 || j==6 ){ // top and bottom borders
              back=back+surround[i][j][k];
            }
          }
        }
      }
      back=back/24f; // normalize the background by number of points
      // find the sums for the centroid
      for( int j=2 ; j<5 ; j++ ){
        for( int i=2 ; i<5 ; i++ ){
          count=surround[i][j][k]-back;
          xsum=xsum+count*(i+1.);
          ysum=ysum+count*(j+1.);
          zsum=zsum+count*(peak.z()+k);
          asum=asum+count;
        }
      }
    }
    
    // total count must be greater than zero for this to make sense
    if(asum<=0){
      peak.reflag(reflag+20);
      return peak;
    }
    // centroid the peaks
    x=(float)(xsum/asum)+peak.x()-4f;
    y=(float)(ysum/asum)+peak.y()-4f;
    z=(float)(zsum/asum)-1f; // -1 is to convert to java counting
    
    // find out how far the peaks were moved
    float dx=Math.abs(x-peak.x());
    float dy=Math.abs(y-peak.y());
    float dz=Math.abs(z-peak.z());
    
    if( dx>1.0 || dy>1.0 || dz>1.0 ){
      // don't shift positions if it is moving more than one bin
      peak.reflag(reflag+20);
      return peak;
    }
      // update the peak
    XScale x_scale = data.getX_scale();
    float tof = x_scale.getInterpolatedX( z );
    IPeak ResPeak = peak.createNewPeakxyz( x , y , z, tof );
    //ResPeak is close to peak so bring values in
    ResPeak.sethkl(peak.h(),peak.k(),peak.l());
    ResPeak.ipkobs( peak.ipkobs());
    ResPeak.inti( peak.inti());
    ResPeak.sigi( peak.sigi());
   
    
    // return the updated peak
    ResPeak .reflag(reflag+10);
    return ResPeak ;
  }
  
  /**
   * Calculates approximate number of pixels on detector that spans an
   * "error" of dQ
   * 
   * @param dQ       The error in Q( Q-reg/2PI  or 1/d)
   * @param Q        The Q(or 1/d) value for the pixel.
   * @param ScatAng  The scattering angle of the pixel in radians
   * @param D        The distance the pixel is from the sample,(m)
   * @param pixelW_H The minimum of width and height of the pixel,(m)
   * @return  Approximate number of pixels that will have Q values 
   *          within Q +/- dQ
   * <HR width="100%">
   * <H1><Center>FORMULAS</center></H1><P>
   * <table><tr>
   *   <td> Q = </td>
   *   <td> <table><tr><td> mv sin(scatAng/2)</td></tr>
   *               <tr><td><hr width=100%></td></tr>
   *               <tr><td align=CENTER> &pi; </td></tr>
   *         </table> </td>
   * </tr></table><P>
   * 
   * <table border = 1><CAPTION> Partial wrt ScatAngle</CAPTION><tr>
   *    <td> dQ =</td>
   *    <td> <table>
   *            <tr><td> mv cos(scatAng/2) d ScatAng</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align = CENTER> 2&pi; </td></tr> 
   *    
   *         </table>
   *    </td>
   *   <td> =</td>
   *   <td><table>
   *         <tr><td align=center> Q dScatAng</td></tr>
   *         <tr><td><hr width="100%"></td></tr>
   *         <tr><td>  2 tan(ScatAng/2)</td></tr>
   *   
   *       </table>
   *   </td>
   * </tr></table><P>
   * <table border=1><CAPTION> Solve for dScatAng</CAPTION><tr>
   *   <td> dScatAng =</td>
   *   <td> <table> 
   *          <tr> <td align=center>2 tan(ScatAng/2) dQ</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *          <tr> <td align=center> Q</td></tr>
   *        </table>
   *   </td><P>
   *   
   * 
   * </tr></table><P>  
   *        
    * <CENTER>Now transfer changed to dRow,dCol. </CENTER><P>
   * <CENTER>Assume Span of peak well with in dQ*.3 where 
   *               dQ=1/Max Crystal cell side</CENTER><P>
   *               
   * <U>dDist =  Dist*dScatAng</u> &nbsp; &nbsp; &nbsp; (s =r &theta; formula)<P>
   * <U> dNrows = dDist/(height of center pixel)</u><P>
   * <U> dNcols = dDist/(width of center pixel)</u><P>          
   */
  public static float dPixel(float dQ, float Q, float ScatAng, float D, 
                                                             float pixelW_H ){
     
     return (float)(dQ*2*Math.tan( ScatAng/2 )*D / (pixelW_H * Q));
     
  }
  
  /**
     * Calculates approximate number of time channels that spans an
   * "error" of dQ in Q
  * @param dQ       The error in Q( Q-reg/2PI  or 1/d)
   * @param Q       The Q( Q-reg/2PI  or 1/d) value for the pixel.
   * @param Time    The total time in us from moderator to detector
   * @param dT_Chan The time in us in the time channel.
   * @return     The approximate number of time channels that span an error
   *             of dQ in Q.
     * <HR width="100%">
   * <H1><Center>FORMULAS</center></H1><P>  
   * 
   * <table border = 1><CAPTION> Partial wrt to time. v = path_lenTot/Time</CAPTION>
   *    <tr>
   *      <td> dQ =</td>
   *      <td> 
   *         <table> 
   *            <tr><td>[-]m* sin(ScatAng/2)path_lenTot*dTime</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align=center> &pi; Time<sup>2</sup>
   *         
   *         </table>
   *      </td>
   *      <td> =</td>
   *      <td><table>
   *            <tr> <td>[-]Q*dTime</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align=center>  Time</td></tr>
   *            
   *      
   *          </table>
   *      </td>
   * 
   * </tr></table><P>
   * 
   * <table border=1><CAPTION> Solve for dTime</CAPTION><tr>
   *    <td> dTime =</td>
   *     <td><table>
   *           <tr><td>[-] Time*dQ</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align=center> Q</td></tr>
   *         </table>
   *     </td>
   * </tr></table><P>  <P>
   * <U> dNChan = dTime/( length of time bin center)         
   */
  public static float dTChan( float dQ, float Q, float Time, float dT_Chan ){
     
     return Time*dQ/Q/dT_Chan;
     
  }
  /**
   * Find the centroided location of a peak. Algorithm not good
   *
   * @param peak  the peak to centroid which already has an initial
   *              position
   * @param ds    the dataset to use for centroiding
   * @param grid  IDataGrid with references to the Data blocks for each
   *              column and row of an area detector.
   * @param MaxXtallength  The maximum length of a side in a unit cell
   * <HR width="100%">
   * <H1><Center>FORMULAS</center></H1><P>
   * <table><tr>
   *   <td> Q = </td>
   *   <td> <table><tr><td> mv sin(scatAng/2)</td></tr>
   *               <tr><td><hr width=100%></td></tr>
   *               <tr><td align=CENTER> &pi; </td></tr>
   *         </table> </td>
   * </tr></table><P>
   * 
   * <table border = 1><CAPTION> Partial wrt ScatAngle</CAPTION><tr>
   *    <td> dQ =</td>
   *    <td> <table>
   *            <tr><td> mv cos(scatAng/2) d ScatAng</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align = CENTER> 2&pi; </td></tr> 
   *    
   *         </table>
   *    </td>
   *   <td> =</td>
   *   <td><table>
   *         <tr><td align=center> Q dScatAng</td></tr>
   *         <tr><td><hr width="100%"></td></tr>
   *         <tr><td>  2 tan(ScatAng/2)</td></tr>
   *   
   *       </table>
   *   </td>
   * </tr></table><P>
   * <table border=1><CAPTION> Solve for dScatAng</CAPTION><tr>
   *   <td> dScatAng =</td>
   *   <td> <table> 
   *          <tr> <td align=center>2 tan(ScatAng/2) dQ</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *          <tr> <td align=center> Q</td></tr>
   *        </table>
   *   </td><P>
   *   
   * 
   * </tr></table><P>
   * 
   * <table border = 1><CAPTION> Partial wrt to time. v = path_lenTot/Time</CAPTION>
   *    <tr>
   *      <td> dQ =</td>
   *      <td> 
   *         <table> 
   *            <tr><td>[-]m* sin(ScatAng/2)path_lenTot*dTime</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align=center> &pi; Time<sup>2</sup>
   *         
   *         </table>
   *      </td>
   *      <td> =</td>
   *      <td><table>
   *            <tr> <td>[-]Q*dTime</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align=center>  Time</td></tr>
   *            
   *      
   *          </table>
   *      </td>
   * 
   * </tr></table><P>
   * 
   * <table border=1><CAPTION> Solve for dTime</CAPTION><tr>
   *    <td> dTime =</td>
   *     <td><table>
   *           <tr><td>[-] Time*dQ</td></tr>
   *            <tr><td><hr width="100%"></td></tr>
   *            <tr><td align=center> Q</td></tr>
   *         </table>
   *     </td>
   * </tr></table><P>
   * 
   * <CENTER>Now transfer changed to dRow,dCol. </CENTER><P>
   * <CENTER>Assume Span of peak well with in dQ*.3 where 
   *               dQ=1/Max Crystal cell side</CENTER><P>
   *               
   * <U>dDist =  Dist*dScatAng</u> &nbsp; &nbsp; &nbsp; (s =r &theta; formula)<P>
   * <U> dNrows = dDist/(height of center pixel)</u><P>
   * <U> dNcols = dDist/(width of center pixel)</u><P>
   * <U> dNChan = dTime/( length of time bin center) 
   * 
   * 
   */
  public static IPeak centroidA(IPeak peak, DataSet ds, IDataGrid grid,
            float MaxXtallength){
     
    //--------------------CAlC size of one peak---------------- 
     float DQ= .3f/MaxXtallength;
     float Q = (new Vector3D(peak.getQ())).length();
     float L1 = peak.L1();
     float DT = (L1+peak.time())*DQ/Q;
     
     Vector3D pos = grid.position( peak.y(), peak.x());
 
     float D = pos.length();
     float ScatAng = (float) Math.acos(  pos.get()[0]/D );
     
     float dScat =(float)( 2*DQ*Math.tan(ScatAng/2)/Q);
     float Ddist =(D*dScat);
        
    double asum  = 0.;
    double xsum  = 0.;
    double ysum  = 0.;
    double zsum  = 0.;
    double back  = 0.;
    double count = 0.;
    
    // create the new reflection flag
    float x,y,z;
    int reflag=peak.reflag();
    reflag=(reflag/100)*100+reflag%10;
    
    // check that the peak isn't too close to the edge
    if( peak.nearedge()<=4.0f ){
      peak.reflag(reflag+20);
      return peak;
    }
    
    // create the surrounding area
    // min is in case peak.x = ncols+.5, It could be rounded up
    int col =  Math.min( grid.num_cols(), Math.round(peak.x()));
    int row =  Math.min( grid.num_rows(), Math.round(peak.y()));
    int time = Math.round(peak.z());
    
    float dPixels = Ddist/Math.min( grid.width(row,col) ,
             grid.height(row, col));
    dPixels = (int)Math.max( 4 , dPixels );
    
    XScale xscl = grid.getData_entry(  row , col ).getX_scale();
    int delt=1;
    if( time >= xscl.getNum_x())
       delt = -1;
    
    float TimeOneChan= Math.abs( xscl.getX( time )- xscl.getX( time + delt ) );
    
    int dNChan = (int)(.5+ DT/TimeOneChan);
    dNChan = Math.max( dNChan , 2 );
    
    
    int nback=0;
    int Srow=0, Scol=0, Schan=0;
    try{
       
      for( int i = col - (int) dPixels ; i <= col + (int) dPixels ; i++ ) {
            for( int j = row - (int) dPixels ; j <= row + (int) dPixels ; j++ ) {
               Data data = grid.getData_entry( j , i );// ds.getData_entry(ids[i][j]);
               if( data != null ) {
                  
                  float[] yvals = data.getY_values();
                 
                  for( int k = time - dNChan ; k <= time + dNChan ; k++ ) 
                  if(k>=0 && k <yvals.length){
                     xsum += i * yvals[ k ];
                     ysum += j * yvals[ k ];
                     zsum += k * yvals[ k ];
                     asum += yvals[ k ];
                     count++ ;
                     Srow +=j;
                     Scol +=i;
                     Schan +=k;
                     if( i == col - (int) dPixels || i == col + (int) dPixels
                              || j == row - (int) dPixels
                              || j == row + (int) dPixels  
                        || k == time-dNChan|| k == time+dNChan) { 
                       
                        back += yvals[ k ];
                        nback++ ;
                     }

                  }//for k
               }//if data != null
            }//for j
         }//for i
    }catch(ArrayIndexOutOfBoundsException e){
       
      peak.reflag(reflag+20);
      return peak;
      
    }
    
    double backPerCell =0;
    if( nback > 0)
       backPerCell = back/nback;  
    
    xsum = xsum - Scol*backPerCell;                                      
    ysum = ysum - Srow*backPerCell;                                      
    zsum = zsum - Schan*backPerCell;
    asum = asum - count*backPerCell;
  
    
    // total count must be greater than zero for this to make sense
    if(asum<=0){
      peak.reflag(reflag+20);
      return peak;
    }
    // centroid the peaks
    x = (float)(xsum /asum);
    y = (float)(ysum /asum);
    z = (float)(zsum /asum); 
    
    // find out how far the peaks were moved
    float dx = Math.abs(x - peak.x());
    float dy = Math.abs(y - peak.y());
    float dz = Math.abs(z - peak.z());
    
    // Another Scaling problem.  May be a scattered peak
    
    if( dx > Math.max( dPixels / 2  , 1 ) || 
        dy > Math.max( dPixels / 2 , 1 ) || 
        dz > Math.max( dNChan / 2 , 1 ) ){
      // don't shift positions if it is moving more than one bin
     System.out.println("x,y,z orig, new="+peak.x()+","+peak.y()+","+peak.z()+"::"+
              x+","+y+","+z+"::"+ dPixels+","+dNChan+","+peak.seqnum());
      peak.reflag(reflag+20);
      return peak;
    }
    
    // update the peak
    float tof = xscl.getInterpolatedX( z );
    IPeak ResPeak = peak.createNewPeakxyz( x , y , z, tof );
    
    //ResPeak is close to the original peak so bring values in
    ResPeak.sethkl( peak.h(), peak.k(), peak.l() );
    ResPeak.ipkobs( peak.ipkobs() );
    ResPeak.inti( peak.inti() );
    ResPeak.sigi( peak.sigi() );
    
    // return the updated peak
    ResPeak .reflag( reflag + 10 );
    return ResPeak ;
  }

  /**
   * Write out the orientation matrix and lattice parameters to the
   * matrix file.
   *
   * @param filename name of file to write to
   * @param UB the orientation matrix. What is actually written is the
   * transpose of what is used in memory.
   *
   * @return A descriptive ErrorString if anything goes wrong or null
   * if all is well
   */
  public static ErrorString WriteMatrix( String filename, float[][]UB){
    if( UB== null)
       return new ErrorString("Orientation Matrix is null");
    double[][] dUB= new double[3][3];
    for( int i=0; i<3;i++)
       for( int j=0;j<3;j++)
          dUB[i][j]=UB[i][j];
    double[] ABC = abc(dUB); 
    if(ABC == null)
       return new ErrorString(" Orientation Matrix is Singular");
    float[]Abc = new float[ABC.length];
    for( int i=0; i< ABC.length; i++)
       Abc[i]=(float)ABC[i];
    return writeMatrix( filename, UB, Abc, new float[7]);
    
  }
  /**
   * Write out the orientation matrix and lattice parameters to the
   * matrix file.
   *
   * @param filename name of file to write to
   * @param UB the orientation matrix. What is actually written is the
   * transpose of what is used in memory.
   * @param abc a float[7] with members (in order) being a, b, c,
   * alpha, beta, gamma, Dvolume
   * @param sig a float[8] with members (in order) being Da, Db, Dc,
   * Dalpha, Dbeta, Dgamma, and Dvolume
   *
   * @return A descriptive ErrorString if anything goes wrong or null
   * if all is well
   */
  public static ErrorString writeMatrix( String filename, float[][] UB,
                                         float[] abc, float[] sig){
    StringBuffer sb;
    if(filename.endsWith(".x")){ // writing an experiment file
      sb=new StringBuffer(81*8);
      TextFileReader tfr=null;
      try{
        tfr=new TextFileReader(filename);
        String line=null;
        while(!tfr.eof()){
          line=tfr.read_line();
          if(line.startsWith("CRS0  VSIGV")){ // cell volume
            sb.append("CRS0  VSIGV"+Format.real(abc[6],11,4)
                      +Format.real(sig[6],10,4)+"\n");
          }else if(line.startsWith("CRS1  ABC   ")){ // lattice param lengths
            sb.append("CRS1  ABC   ");
            for(int i=0 ; i<3 ; i++ )
              sb.append(Format.real(abc[i],10,4));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS1  ABCSIG")){ // uncertainties in param lengths
            sb.append("CRS1  ABCSIG");
            for(int i=0 ; i<3 ; i++ )
              sb.append(Format.real(sig[i],10,4));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS1  ANGLES")){ // lattice param angles
            sb.append("CRS1  ANGLES");
            for(int i=3 ; i<6 ; i++ )
              sb.append(Format.real(abc[i],10,3));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS1  ANGSIG")){ // uncertainties in param angles
            sb.append("CRS1  ANGSIG");
            for(int i=3 ; i<6 ; i++ )
              sb.append(Format.real(sig[i],10,3));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS11 UBMAT1")){ // orientation matrix
            sb.append("CRS11 UBMAT1");
            for(int j=0 ; j<3 ; j++)
              sb.append(Format.real(UB[j][0],10,6));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS11 UBMAT2")){ // orientation matrix
            sb.append("CRS11 UBMAT2");
            for(int j=0 ; j<3 ; j++)
              sb.append(Format.real(UB[j][1],10,6));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS11 UBMAT3")){ // orientation matrix
            sb.append("CRS11 UBMAT3");
            for(int j=0 ; j<3 ; j++)
              sb.append(Format.real(UB[j][2],10,6));
            sb.append(Format.string("\n",39));
          }else{
            sb.append(line+"\n");
          }
        }
      }catch( IOException e ){
        return new ErrorString("Reading Experiment File: "+e.getMessage());
      }finally{
        try{
          if(tfr!=null) tfr.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }else{ // writing a matrix file
      sb= new StringBuffer(10*3+1);

      // the UB matrix
      for( int i=0 ; i<3 ; i++ ){
        for (int j=0 ; j<3 ;j++ )
          sb.append(Format.real(UB[j][i],10,6));
        sb.append("\n");
      }

      // lattice parameters
      for( int i=0 ; i<7 ; i++ )
        sb.append(Format.real(abc[i],10,3));
      sb.append("\n");
      // sigmas
      for( int i=0 ; i<7 ; i++)
        sb.append(Format.real(sig[i],10,3));
      sb.append("\n\n\n");
      sb.append("The above matrix is the TRANSPOSE of the UB Matrix that ");
      sb.append( "maps the column \n");
      sb.append( "vector (h,k,l ) to the column vector (q'x,q'y,q'z).\n");
      sb.append( "|Q'|=1/dspacing and its coordinates are \"currently\" " );
      sb.append( "relative to IPNS's \n");
      sb.append( "right-hand coordinate system where x is the beam ");
      sb.append( "direction and z is \n");
      sb.append( "vertically upward.\n" );
    }
    //Write results to the matrix file
    FileWriter fw=null;
    try{
      fw=new FileWriter(filename,false);
      fw.write(sb.toString());
      fw.flush();
    }catch(IOException e){
      return new ErrorString("Writing Matrix File: "+e.getMessage());
    }finally{
      if(fw!=null){
        try{
          fw.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }
    return null;
  }

  /**
   * Method to calculate the lattice parameters from a given UB matrix
   */
  public static double[] abc(double[][] UB){
    double[] abc=new double[7];
    double[][] UBtrans=new double[3][3];
    for( int i=0 ; i<3 ; i++ )
      for( int j=0 ; j<3 ; j++ )
        UBtrans[i][j]=UB[j][i];
    double[][] UBsquare=LinearAlgebra.mult(UBtrans,UB);
     if(UBsquare==null) return null;
    double[][] invUBsquare=LinearAlgebra.getInverse(UBsquare);
    if(invUBsquare==null) return null;

    // calculate a, b, c
    abc[0]=Math.sqrt(invUBsquare[0][0]);
    abc[1]=Math.sqrt(invUBsquare[1][1]);
    abc[2]=Math.sqrt(invUBsquare[2][2]);

    // calculate alpha, beta, gamma
    abc[3]=invUBsquare[1][2]/(abc[1]*abc[2]);
    abc[4]=invUBsquare[0][2]/(abc[0]*abc[2]);
    abc[5]=invUBsquare[0][1]/(abc[0]*abc[1]);
    abc[3]=Math.acos(abc[3])*180./Math.PI;
    abc[4]=Math.acos(abc[4])*180./Math.PI;
    abc[5]=Math.acos(abc[5])*180./Math.PI;

    // calculate the cell volume
    abc[6]=abc[0]*abc[1]*Math.sin(abc[5]*Math.PI/180.);
    abc[6]=abc[6]/Math.sqrt(UBsquare[2][2]);

    return abc;
  }

  /**
   * Method to calculate the 'cell scalars' for a given set of lattice
   * parameters.
   */
  public static double[] scalars(double[] abc){
    double[] scalars=new double[6];

    // the first three scalars are the square of the lattice parameters
    for( int i=0 ; i<3 ; i++ )
      scalars[i]=abc[i]*abc[i];

    // the other three are the dot products of the different lattice vectors
    scalars[3]=abc[1]*abc[2]*Math.cos(abc[3]*Math.PI/180.); // b dot c
    scalars[4]=abc[0]*abc[2]*Math.cos(abc[4]*Math.PI/180.); // a dot c
    scalars[5]=abc[0]*abc[1]*Math.cos(abc[5]*Math.PI/180.); // a dot b

    // return the result
    return scalars;
  }
}
